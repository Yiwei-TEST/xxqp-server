package com.sy599.game.qipai.ahmj.bean;

import com.alibaba.fastjson.JSONArray;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.FirstmythConstants;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.*;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.GangMoMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.GangPlayMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.MoMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayMajiangRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.*;
import com.sy599.game.msg.serverPacket.TableRes.*;
import com.sy599.game.qipai.ahmj.tool.*;
import com.sy599.game.qipai.ahmj.constant.AhmjConstants;
import com.sy599.game.qipai.ahmj.constant.Ahmj;
import com.sy599.game.qipai.ahmj.rule.RobotAI;
import com.sy599.game.util.TingResouce;
import com.sy599.game.qipai.ahmj.tool.ting.TingTool;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class AhmjTable extends BaseTable {
	private List<Ahmj> nowDisCardIds = new ArrayList<>();

	/*** 0胡 1碰 2明刚 3暗杠(暗杠后来不需要了 暗杠也用3标记)4吃 */
	private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
	/*** 0胡 1碰 2明刚 3暗杠(暗杠后来不需要了 暗杠也用3标记)4吃 */
	private Map<Integer, Map<Integer, List<Integer>>> gangSeatMap = new ConcurrentHashMap<>();
	private int maxPlayerCount = 4;
	private List<Integer> leftMajiangs=new ArrayList<>();
	/*** 玩家map */
	private Map<Long, AhmjPlayer> playerMap = new ConcurrentHashMap<Long, AhmjPlayer>();
	/*** 座位对应的玩家 */
	private Map<Integer, AhmjPlayer> seatMap = new ConcurrentHashMap<Integer, AhmjPlayer>();
	private Map<Integer, Integer> huConfirmMap = new HashMap<>();
	/*** 抓鸟 */
	private int birdNum;
	/*** 计算庄闲 (1胡牌为庄 2上把为庄) */
	private int isCalcBanker = 1;
	/*** 摸麻将的seat */
	private int moMajiangSeat;
	/*** 摸杠的麻将 */
	private List<Ahmj> moGang;
	/*** 杠出来的麻将 */
	private Ahmj gangMajiang;
	/*** 摸杠胡(其实就是抢杠胡) */
	private List<Integer> moGangHuList = new ArrayList<>();
	/*** 杠后出的两张牌 */
	private List<Ahmj> gangDisMajiangs = new ArrayList<>();
	/*** 摸海底捞的座位 */
	private int moLastMajiangSeat;
	/*** 询问海底捞的座位 */
	private int askLastMajaingSeat;
	/*** 第一次出现海底的座位 */
	private int fristLastMajiangSeat;
	private Ahmj lastMajiang;
	private int gangHuMajiangId;
	private int disEventAction;
	private int  firstId;
	private List<Integer> wangValList;
	private long groupPaylogId = 0; // 俱乐部战绩记录id
	private int readyTime = 0 ;
	/**
	 * 首轮是否采用随机庄家模式：0：首轮默认是房主为庄家 1：首轮采用随机庄家模式
	 */
	private int randomBanker;
	/**
	 * 玩家位置对应临时操作 当同时存在多个可做的操作时 1当优先级最高的玩家最先操作 则清理所有操作提示 并执行该操作
	 * 2当操作优先级低的玩家先选择操作，则玩家先将所做操作存入临时操作中 当玩家优先级最高的玩家操作后 在判断临时操作是否可执行并执行
	 */
	private Map<Integer, AhmjTempAction> tempActionMap = new ConcurrentHashMap<>();


	//底分
	private int diFen=1;
	// 是否加倍：0否，1是
	private int jiaBei=0;
	// 加倍分数：低于xx分进行加倍
	private int jiaBeiFen=0;
	// 加倍倍数：翻几倍
	private int jiaBeiShu=0;
	//低于below加分
	private int belowAdd=0;
	private int below=0;
	//王 1:4王，2:7王
	private int wangType=0;
	//王代硬
	private int wangDaiYing=0;
	//一炮多响
	private int yiPaoDuoXiang=0;
	private int isAutoPlay = 0;// 是否开启自动托管
	/** 托管1：单局，2：全局 */
	private int autoPlayGlob;
	private int autoTableCount;
	/**
	 * 开局所有底牌
	 **/
	private volatile List<Integer> startLeftCards = new ArrayList<>();
	//159中鸟
	private int niao159=0;
	//庄闲分
	private int bankerFen=0;
	//版本号
	private int versionCode=10001;

	@Override
	public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams,
							Object... objects) throws Exception {
		long id = getCreateTableId(player.getUserId(), play);
		TableInf info = new TableInf();
		info.setMasterId(player.getUserId());
		info.setRoomId(0);
		info.setPlayType(play);
		info.setTableId(id);
		info.setTotalBureau(bureauCount);
		info.setPlayBureau(1);
		info.setServerId(GameServerConfig.SERVER_ID);
		info.setCreateTime(new Date());
		info.setDaikaiTableId(daikaiTableId);
		info.setExtend(buildExtend());
		TableDao.getInstance().save(info);
		loadFromDB(info);


		payType = StringUtil.getIntValue(params, 2, 1);// 支付方式
//		diFen = StringUtil.getIntValue(params, 3, 0);// 底分
		wangType = StringUtil.getIntValue(params, 4, 0);// 4王
		birdNum = StringUtil.getIntValue(params, 5, 0);// 鸟的算法
		bankerFen = StringUtil.getIntValue(params, 6, 0);// 庄闲算分
		maxPlayerCount = StringUtil.getIntValue(params, 7, 0);// 人数
		wangDaiYing = StringUtil.getIntValue(params, 8, 0);// 王硬庄
		yiPaoDuoXiang = StringUtil.getIntValue(params, 9, 0);// 一炮多响
		isAutoPlay = StringUtil.getIntValue(params, 10, 0);// 托管
//		isAutoPlay=20;
		if(isAutoPlay==1) {
			// 默认1分钟
			isAutoPlay=60;
		}
		autoPlayGlob=StringUtil.getIntValue(params, 11, 0);
		if(maxPlayerCount==2){
			jiaBei = StringUtil.getIntValue(params, 12, 0);
			jiaBeiFen = StringUtil.getIntValue(params, 16, 100);
			jiaBeiShu = StringUtil.getIntValue(params, 13, 1);

			int belowAdd = StringUtil.getIntValue(params, 14, 0);
			if(belowAdd<=100&&belowAdd>=0)
				this.belowAdd=belowAdd;
			int below = StringUtil.getIntValue(params, 15, 0);
			if(below<=100&&below>=0){
				this.below=below;
				if(belowAdd>0&&below==0)
					this.below=10;
			}
		}
        niao159 = StringUtil.getIntValue(params, 17, 0);// 159中鸟
		int diFen=StringUtil.getIntValue(params, 18, 1); // 底分
		if(diFen<=10&&diFen>0)
			this.diFen=diFen;
		changeExtend();
	}


	@Override
	public void initExtend0(JsonWrapper wrapper) {
		startLeftCards = loadStartLeftCards(wrapper.getString("startLeftCards"));
		for (AhmjPlayer player : seatMap.values()) {
			player.initExtend(wrapper.getString(player.getSeat()));
		}
		String huListstr = wrapper.getString(5);
		if (!StringUtils.isBlank(huListstr)) {
			huConfirmMap = DataMapUtil.implode(huListstr);
		}
		birdNum = wrapper.getInt(6, 0);
		moMajiangSeat = wrapper.getInt(7, 0);
		String moGangMajiangId = wrapper.getString(8);
		if (!StringUtils.isBlank(moGangMajiangId)) {
			moGang = MajiangHelper.explodeMajiang(moGangMajiangId, "_");
		}
		String moGangHu = wrapper.getString(9);
		if (!StringUtils.isBlank(moGangHu)) {
			moGangHuList = StringUtil.explodeToIntList(moGangHu);
		}
		String gangDisMajiangstr = wrapper.getString(10);
		if (!StringUtils.isBlank(gangDisMajiangstr)) {
			gangDisMajiangs = MajiangHelper.explodeMajiang(gangDisMajiangstr, ",");
		}
		int gangMajiang = wrapper.getInt(11, 0);
		if (gangMajiang != 0) {
			this.gangMajiang = Ahmj.getMajang(gangMajiang);
		}

		askLastMajaingSeat = wrapper.getInt(12, 0);
		moLastMajiangSeat = wrapper.getInt(13, 0);
		int lastMajiangId = wrapper.getInt(14, 0);
		if (lastMajiangId != 0) {
			this.lastMajiang = Ahmj.getMajang(lastMajiangId);
		}
		fristLastMajiangSeat = wrapper.getInt(15, 0);
		disEventAction = wrapper.getInt(16, 0);
		isCalcBanker = wrapper.getInt(17, 1);
		bankerFen = wrapper.getInt(18, 1);
		firstId = wrapper.getInt(19, 0);
		if (wrapper.getList(20) != null) {
			this.wangValList = (List<Integer>) wrapper.getList(20);

		}
		this.gangHuMajiangId = wrapper.getInt(21, 0);
		groupPaylogId = wrapper.getLong(22, 0);
		isAutoPlay = wrapper.getInt(23, 0);
		autoPlayGlob = wrapper.getInt(24, 0);
		autoTableCount = wrapper.getInt(25, 0);
		diFen = wrapper.getInt(26, 1);
		if(diFen<=0||diFen>10)
			diFen=1;
		jiaBei = wrapper.getInt(27, 0);
		jiaBeiFen = wrapper.getInt(28, 0);
		jiaBeiShu = wrapper.getInt(29, 0);
		belowAdd = wrapper.getInt(30, 0);
		below = wrapper.getInt(31, 0);
		wangType = wrapper.getInt(32, 0);
//		zhuaNiao = wrapper.getInt(33, 0);
		wangDaiYing = wrapper.getInt(34, 0);
		yiPaoDuoXiang = wrapper.getInt(35, 0);
		tempActionMap = loadTempActionMap(wrapper.getString("tempActions"));
		maxPlayerCount = wrapper.getInt(36, 2);
        niao159 = wrapper.getInt(37, 0);

	}

	public JsonWrapper buildExtend0(JsonWrapper wrapper) {
		for (AhmjPlayer player : seatMap.values()) {
			wrapper.putString(player.getSeat(), player.toExtendStr());
		}
		wrapper.putString("startLeftCards", startLeftCardsToJSON());
		wrapper.putString(5, DataMapUtil.explode(huConfirmMap));
		wrapper.putInt(6, birdNum);
		wrapper.putInt(7, moMajiangSeat);
		if (moGang != null) {
			wrapper.putString(8, MajiangHelper.implodeMajiang(moGang, "_"));

		} else {
			wrapper.putInt(8, 0);

		}
		wrapper.putString(9, StringUtil.implode(moGangHuList, ","));
		wrapper.putString(10, MajiangHelper.implodeMajiang(gangDisMajiangs, ","));
		if (gangMajiang != null) {
			wrapper.putInt(11, gangMajiang.getId());

		} else {
			wrapper.putInt(11, 0);

		}
		wrapper.putInt(12, askLastMajaingSeat);
		wrapper.putInt(13, moLastMajiangSeat);
		if (lastMajiang != null) {
			wrapper.putInt(14, lastMajiang.getId());
		} else {
			wrapper.putInt(14, 0);
		}
		wrapper.putInt(15, fristLastMajiangSeat);
		wrapper.putInt(16, disEventAction);
		wrapper.putInt(17, isCalcBanker);
		wrapper.putInt(18, bankerFen);
		wrapper.putInt(19, firstId);
		wrapper.putList(20, wangValList);
		wrapper.putInt(21, gangHuMajiangId);
		wrapper.putLong(22, groupPaylogId);
		wrapper.putInt(23, isAutoPlay);
		wrapper.putInt(24, autoPlayGlob);
		wrapper.putInt(25, autoTableCount);
		wrapper.putInt(26, diFen);
		wrapper.putInt(27, jiaBei);
		wrapper.putInt(28, jiaBeiFen);
		wrapper.putInt(29, jiaBeiShu);
		wrapper.putInt(30, belowAdd);
		wrapper.putInt(31, below);
		wrapper.putInt(32, wangType);
//		wrapper.putInt(33, zhuaNiao);
		wrapper.putInt(34, wangDaiYing);
		wrapper.putInt(35, yiPaoDuoXiang);
		JSONArray tempJsonArray = new JSONArray();
		for (int seat : tempActionMap.keySet()) {
			tempJsonArray.add(tempActionMap.get(seat).buildData());
		}
		wrapper.putString("tempActions", tempJsonArray.toString());
		wrapper.putInt(36, maxPlayerCount);
        wrapper.putInt(37, niao159);
		return wrapper;
	}


	private Map<Integer, AhmjTempAction> loadTempActionMap(String json) {
		Map<Integer, AhmjTempAction> map = new ConcurrentHashMap<>();
		if (json == null || json.isEmpty())
			return map;
		JSONArray jsonArray = JSONArray.parseArray(json);
		for (Object val : jsonArray) {
			String str = val.toString();
			AhmjTempAction tempAction = new AhmjTempAction();
			tempAction.initData(str);
			map.put(tempAction.getSeat(), tempAction);
		}
		return map;
	}

	@Override
	protected boolean quitPlayer1(Player player) {
		return false;
	}

	@Override
	protected boolean joinPlayer1(Player player) {
		return false;
	}

	@Override
	public int isCanPlay() {
		if (getPlayerCount() < getMaxPlayerCount()) {
			return 1;
		}
		return 0;
	}

	@Override
	public void calcOver() {
		List<Integer> winList = new ArrayList<>(huConfirmMap.keySet());
		boolean selfMo = false;
		int[] prickBirdMajiangIds = null;
		int[] seatBirds = null;
		Map<Integer, Integer> seatBridMap = new HashMap<>();
		boolean flow = false;

		int startseat = 0;
		if (winList.size() == 0) {
			// 流局
			flow = true;

		} else {
			// 先判断是自摸还是放炮
			AhmjPlayer winPlayer = null;
			if (winList.size() == 1) {
				winPlayer = seatMap.get(winList.get(0));
				if ((winPlayer.isAlreadyMoMajiang() || winPlayer.isGangshangHua()) && winList.get(0) == moMajiangSeat) {
					selfMo = true;
				}

				if (disCardRound == 0) {
					selfMo = true;
				}
			}

			// 如果通炮按放炮的座位开始算
			if (!winList.isEmpty()) {
				// 先砸鸟
				prickBirdMajiangIds = prickbird(selfMo, winList);
				// 通炮从放炮的开始算
				if (isCalcBanker == 1) {
					// 胡牌为抓鸟庄
					startseat = winList != null && winList.size() > 1 ? disCardSeat : winList.get(0);
				} else {
					// 上局赢家为抓鸟庄
					startseat = lastWinSeat;
				}
				// startseat = winList.get(0);
				// 抓到鸟的座位
				seatBirds = bridToSeat(prickBirdMajiangIds, startseat);
			}

			// 庄家
			AhmjPlayer masterPlayer = seatMap.get(startseat);
			if (selfMo) {
				// 看胡牌的人抓了几个大胡，有几个大胡 算几个庄家
				int bankerCount = 0;
				if (IsCalcBankerPoint()) {
					bankerCount = winPlayer.getDahuCount();
					if (bankerCount == 0) {
						// 没有大胡 算一个小胡的分
						bankerCount = 1;
					}
				}

				int winSeat = winList.get(0);
				// 赢家中鸟
				int winBridNum = calcBridNum(seatBirds, winSeat);
				seatBridMap.put(winList.get(0), winBridNum);
				int winPoint = 0;
				if (masterPlayer != null && masterPlayer.getSeat() == winSeat) {
					// 算庄的数量
					winPoint = bankerCount;
				}
				// winBridPoint = winBridPoint;
				// 自摸
				int loseTotalPoint = 0;
				AhmjPlayer winner=seatMap.get(winList.get(0));
				for (int seat : seatMap.keySet()) {
					if (!winList.contains(seat)) {
						// 除了赢家的其他人
						AhmjPlayer player = seatMap.get(seat);
						// 先算庄
						int losePoint = player.getLostPoint() - winPoint;
						// 输家中鸟
						int bridcount = calcBridNum(seatBirds, seat);
						if (bridcount != 0) {
							seatBridMap.put(seat, bridcount);
						}

						// 庄家
						if (masterPlayer != null && masterPlayer.getSeat() == seat) {
							losePoint -= bankerCount;
						}

						if (winPlayer.getDahu().isEmpty()) {
						}
						losePoint = calcBridPoint(losePoint, winBridNum + bridcount);
						if(bankerFen==1&&(winList.contains(lastWinSeat)||seat==lastWinSeat)){
							losePoint-=1;
							player.changePointArr(1,-1);
							winner.changePointArr(1,1);
						}
						//乘底分
						losePoint*=diFen;
						loseTotalPoint += losePoint;
						player.setLostPoint(losePoint);
						player.changePointArr(0,bridcount);
					}
				}
				for (int seat : winList) {
					AhmjPlayer player = seatMap.get(seat);
					player.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index8, 1);
					player.setLostPoint(-loseTotalPoint);
					player.changePointArr(0,winBridNum);
				}

			} else {
				// 小胡接炮 每人2分
				// 如果庄家输牌失分翻倍
				AhmjPlayer losePlayer = seatMap.get(disCardSeat);
				losePlayer.changeActionNum(2,1);
				losePlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index10, winList.size());
				int totalLosePoint = 0;
				int disCardBridNum = calcBridNum(seatBirds, disCardSeat);
				for (int seat : winList) {
					// 赢家中鸟
					int birdNum = calcBridNum(seatBirds, seat);
					seatBridMap.put(seat, birdNum);

					// 输家中鸟
					seatBridMap.put(disCardSeat, disCardBridNum);
					birdNum += disCardBridNum;

					// 胡牌
					winPlayer = seatMap.get(seat);
					int point = winPlayer.getLostPoint();

					point = calcBridPoint(-point, birdNum);
					if(bankerFen==1&&(losePlayer.getSeat()==lastWinSeat||seat==lastWinSeat)){
						point-=1;
						losePlayer.changePointArr(1,-1);
						winPlayer.changePointArr(1,1);
					}
					winPlayer.changePointArr(0,calcBridNum(seatBirds, seat));
					//乘底分
					point*=diFen;
					totalLosePoint += point;
					winPlayer.setLostPoint(-point);
				}
				losePlayer.setLostPoint(totalLosePoint);
				losePlayer.changePointArr(0,disCardBridNum);
			}

		}

		for (AhmjPlayer seat : seatMap.values()) {
			if (winList.contains(seat.getSeat())) {
				seat.calcWin();
			}
			seat.changePoint(seat.getLostPoint());
		}

		for (Integer seat: seatBirds) {
			if(seat!=0){
				AhmjPlayer player = seatMap.get(seat);
				player.changeActionNum(5,1);
			}
		}
		boolean over = playBureau == totalBureau;
		if(autoPlayGlob >0) {
			// //是否解散
			boolean diss = false;
			if(autoPlayGlob ==1) {
				for (AhmjPlayer seat : seatMap.values()) {
					if(seat.isAutoPlay()) {
						diss = true;
						break;
					}

				}
			} else if (autoPlayGlob == 3) {
				diss = checkAuto3();
			}
			if(diss) {
				autoPlayDiss= true;
				over =true;
			}
		}

		ClosingMjInfoRes.Builder res = sendAccountsMsg(over, selfMo, winList, prickBirdMajiangIds,
				seatBirds, seatBridMap, false);
		if (!winList.isEmpty()) {
			if (winList.size() > 1) {
				// 一炮多响设置放炮的人为庄家
				setLastWinSeat(disCardSeat);
			} else {
				setLastWinSeat(winList.get(0));
			}
		} else if (leftMajiangs.isEmpty()) {// 黄庄
			setLastWinSeat(moMajiangSeat);
		}
		calcAfter();
		saveLog(over, 0l, res.build());
		if (over) {
			calcOver1();
			calcOver2();
			calcOver3();
			diss();
		} else {
			initNext();
			calcOver1();
		}
		for (AhmjPlayer player : seatMap.values()) {
			if (player.isAutoPlaySelf()) {
				player.setAutoPlay(false, false);
			}
		}
		for (Player player : seatMap.values()) {
			player.saveBaseInfo();
		}

	}

	private boolean checkAuto3() {
		boolean diss = false;
		// if(autoPlayGlob==3) {
		boolean diss2 = false;
		for (AhmjPlayer seat : seatMap.values()) {
			if (seat.isAutoPlay()) {
				diss2 = true;
				break;
			}
		}
		if (diss2) {
			autoTableCount += 1;
		} else {
			autoTableCount = 0;
		}
		if (autoTableCount == 3) {
			diss = true;
		}
		// }
		return diss;
	}

	/**
	 * 计算鸟分加成
	 * 
	 * @param point
	 * @param brid
	 * @return
	 */
	private int calcBridPoint(int point, int brid) {
		if (brid <= 0) {
			return point;
		}
		point = (int) (point * (Math.pow(2, brid)));
		return point;
	}

	public void saveLog(boolean over, long winId, Object resObject) {
		ClosingMjInfoRes res = (ClosingMjInfoRes) resObject;
		LogUtil.d_msg("tableId:" + id + " play:" + playBureau + " over:" + res);
		String logRes = JacksonUtil.writeValueAsString(LogUtil.buildMJClosingInfoResLog(res));
		String logOtherRes = JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResOtherLog(res));
		Date now = TimeUtil.now();
		UserPlaylog userLog = new UserPlaylog();
		userLog.setLogId(playType);
		userLog.setTableId(id);
		userLog.setRes(extendLogDeal(logRes));
		userLog.setTime(now);
		userLog.setTotalCount(totalBureau);
		userLog.setCount(playBureau);
		userLog.setStartseat(lastWinSeat);
		userLog.setOutCards(playLog);
		userLog.setExtend(logOtherRes);
		userLog.setType(creditMode == 1 ? 2 : 1 );
		userLog.setMaxPlayerCount(maxPlayerCount);
		userLog.setGeneralExt(buildGeneralExtForPlaylog().toString());
		long logId = TableLogDao.getInstance().save(userLog);
		saveTableRecord(logId, over, playBureau);
		for (AhmjPlayer player : playerMap.values()) {
			player.addRecord(logId, playBureau);
		}
		UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
	}

	/**
	 * 中鸟个数
	 * 
	 * @param seatBridArr
	 * @param seat
	 * @return
	 */
	private int calcBridNum(int[] seatBridArr, int seat) {
		int point = 0;
		for (int seatBrid : seatBridArr) {
			if (seat == seatBrid) {
				point++;
			}
		}
		return point;
	}

	/**
	 * 抓鸟
	 * 
	 * @param selfMo
	 * @param winList
	 * @return
	 */
	private int[] prickbird(boolean selfMo, List<Integer> winList) {
		// 先砸鸟
		int birdNum = this.birdNum;
		if (birdNum > leftMajiangs.size()) {
			birdNum = leftMajiangs.size();
		}
		int[] bird = new int[birdNum];
		for (int i = 0; i < birdNum; i++) {
			Ahmj prickbirdMajiang = getLeftMajiang();
			if (prickbirdMajiang != null) {
				// 如果桌面上已经没有牌了拿桌面上最后一次摸的牌
				bird[i] = prickbirdMajiang.getId();
			} else {
				// bird[i] = lastMajiang.getId();
			}
		}
		// 算鸟砸中谁
		return bird;
	}

	/**
	 * 中鸟的麻将算出座位
	 * 
	 * @param prickBirdMajiangIds
	 * @param winSeat
	 * @return
	 */
	private int[] bridToSeat(int[] prickBirdMajiangIds, int winSeat) {
		int[] seatArr = new int[prickBirdMajiangIds.length];
        if(niao159==1){
            for (int i = 0; i < prickBirdMajiangIds.length; i++) {
                Ahmj mj = Ahmj.getMajang(prickBirdMajiangIds[i]);
                int yu=mj.getVal()%10;
                if(yu==1||yu==5||yu==9){
                    seatArr[i]=winSeat;
                }else {
                    seatArr[i]=0;
                }
            }
        }else {
            for (int i = 0; i < prickBirdMajiangIds.length; i++) {
                Ahmj majiang = Ahmj.getMajang(prickBirdMajiangIds[i]);
                int prickbirdPai = majiang.getPai();
                prickbirdPai = (prickbirdPai - 1) % getMaxPlayerCount();// 从自己开始算
                // 所以减1
                int prickbirdseat = prickbirdPai + winSeat > getMaxPlayerCount()
                        ? prickbirdPai + winSeat - getMaxPlayerCount() : prickbirdPai + winSeat;
                seatArr[i] = prickbirdseat;
            }
        }
		return seatArr;
	}

	@Override
	public Map<String, Object> saveDB(boolean asyn) {
		if (id < 0) {
			return null;
		}
		Map<String, Object> tempMap = loadCurrentDbMap();
		if (!tempMap.isEmpty()) {
			tempMap.put("tableId", id);
			tempMap.put("roomId", roomId);
			if (tempMap.containsKey("players")) {
				tempMap.put("players", buildPlayersInfo());
			}
			if (tempMap.containsKey("outPai1")) {
				tempMap.put("outPai1", seatMap.get(1).buildOutPaiStr());
			}
			if (tempMap.containsKey("outPai2")) {
				tempMap.put("outPai2", seatMap.get(2).buildOutPaiStr());
			}
			if (tempMap.containsKey("outPai3")) {
				tempMap.put("outPai3", seatMap.get(3).buildOutPaiStr());
			}
			if (tempMap.containsKey("outPai4")) {
				tempMap.put("outPai4", seatMap.get(4).buildOutPaiStr());
			}
			if (tempMap.containsKey("handPai1")) {
				tempMap.put("handPai1", StringUtil.implode(seatMap.get(1).getHandPais(), ","));
			}
			if (tempMap.containsKey("handPai2")) {
				tempMap.put("handPai2", StringUtil.implode(seatMap.get(2).getHandPais(), ","));
			}
			if (tempMap.containsKey("handPai3")) {
				tempMap.put("handPai3", StringUtil.implode(seatMap.get(3).getHandPais(), ","));
			}
			if (tempMap.containsKey("handPai4")) {
				tempMap.put("handPai4", StringUtil.implode(seatMap.get(4).getHandPais(), ","));
			}
			if (tempMap.containsKey("answerDiss")) {
				tempMap.put("answerDiss", buildDissInfo());
			}
			if (tempMap.containsKey("nowDisCardIds")) {
				tempMap.put("nowDisCardIds", StringUtil.implode(AhmjHelper.toMajiangIds(nowDisCardIds), ","));
			}
			if (tempMap.containsKey("leftPais")) {
				tempMap.put("leftPais", StringUtil.implode(leftMajiangs, ","));
			}
			if (tempMap.containsKey("nowAction")) {
				tempMap.put("nowAction", buildNowAction());
			}
			if (tempMap.containsKey("extend")) {
				tempMap.put("extend", buildExtend());
			}
		}
		return tempMap.size() > 0 ? tempMap : null;
	}

	@Override
	public int getPlayerCount() {
		return playerMap.size();
	}

	@Override
	protected void sendDealMsg() {
		sendDealMsg(0);
	}

	public List<Integer> getShowWangVal() {
		return wangValList;
	}

	@Override
	protected void sendDealMsg(long userId) {
		// 天胡或者暗杠
		for (AhmjPlayer tablePlayer : seatMap.values()) {
			DealInfoRes.Builder res = DealInfoRes.newBuilder();
			List<Integer> actionList = tablePlayer.checkMo(null, true);
			if (!actionList.isEmpty()) {
				addActionSeat(tablePlayer.getSeat(), actionList);
			}
			if (!tablePlayer.getXiaohu().isEmpty()) {
				res.addAllXiaohu(tablePlayer.getXiaohu());

			} else {
				if (!actionList.isEmpty()) {
					res.addAllSelfAct(actionList);

				}
			}
			res.addAllHandCardIds(tablePlayer.getHandPais());
			res.setNextSeat(getNextDisCardSeat());
			res.setGameType(getWanFa());// 1跑得快 2麻将
			res.setRemain(leftMajiangs.size());
			res.setBanker(lastWinSeat);
			res.setLaiZiVal(firstId);
			// res.set
			if (userId == tablePlayer.getUserId()) {
				continue;
			}
			tablePlayer.writeSocket(res.build());
			sendTingInfo(tablePlayer);
			if(tablePlayer.isAutoPlay()) {
				addPlayLog(tablePlayer.getSeat(), AhmjConstants.action_tuoguan + "",(autoPlay?1:0) + "");
			}
		}

	}

	/**
	 * 摸牌
	 * 
	 * @param player
	 */
	public void moMajiang(AhmjPlayer player, boolean isBuzhang) {
		if (state != table_state.play) {
			return;
		}
		if (player.isRobot()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (leftMajiangs.size() == 0) {
			calcOver();
			return;
		}

		// 摸牌
		Ahmj majiang = null;
		if (disCardRound != 0) {
			// 玩家手上的牌是双数，已经摸过牌了
			if (player.isAlreadyMoMajiang()) {
				return;
			}
			// 匹配测试牌型调牌
			if (zp != null && GameServerConfig.isDebug() && zp.size()==1) {
				majiang =Ahmj.getMajangByVal(zp.get(0).get(0));
				if (majiang != null) {
					leftMajiangs.remove(majiang);
					zp.clear();
				}
			}
			// 正常摸牌逻辑
			if (majiang == null) {
				// 普通摸牌，摸剩下第一张
				majiang = getLeftMajiang();
			}
		}
		if (majiang != null) {
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + MjDisAction.action_moMjiang + "_"
					+ majiang.getId());
			player.moMajiang(majiang);

			if (leftMajiangs.isEmpty()) {
				setMoLastMajiangSeat(player.getSeat());
			}
		}
		// 检查摸牌
		clearActionSeatMap();
		if (disCardRound == 0) {
			return;
		}
		setMoMajiangSeat(player.getSeat());
		List<Integer> arr = player.checkMo(majiang, false);
		if (!arr.isEmpty()) {
			addActionSeat(player.getSeat(), arr);
		}
		logMo(player,majiang,arr);
		MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
		res.setUserId(player.getUserId() + "");
		res.setRemain(getLeftMajiangCount());
		res.setSeat(player.getSeat());
		boolean disMajiang = false;
		for (AhmjPlayer seat : seatMap.values()) {
			if (seat.getUserId() == player.getUserId()) {
				if (!disMajiang) {
					MoMajiangRes.Builder copy = res.clone();
					copy.addAllSelfAct(arr);
					if (majiang != null) {
						copy.setMajiangId(majiang.getId());
					}
					seat.writeSocket(copy.build());
					sendTingInfo(seat);
				}

			} else {
				seat.writeSocket(res.build());
			}
		}

		if (disMajiang) {
			// 自动出牌
			List<Ahmj> disMjiang = new ArrayList<>();
			disMjiang.add(majiang);
			disMajiang2(player, disMjiang, 0);
		}
	}

	public void sendAskLastMajiangRes() {
		ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_asklastmajiang, askLastMajaingSeat);
		for (AhmjPlayer seatPlayer : seatMap.values()) {
			seatPlayer.writeSocket(res.build());
		}
	}

	/**
	 * 小胡
	 * 
	 * @param player
	 */
	private void xiaoHu(AhmjPlayer player) {
		if (!player.canXiaoHu()) {
			return;
		}
		List<Integer> xiaoHuList = player.getXiaohu();
		int index = xiaoHuList.indexOf(1);
		player.huXiaohu(index);
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		List<Ahmj> showMajiangs = player.showXiaoHuMajiangs(index);
		buildPlayRes(builder, player, MjDisAction.action_xiaohu, player.showXiaoHuMajiangs(index));
		// 0缺一色 1板板胡 2大四喜 3六六顺
		builder.addHuArray(index + 1);// 前台跟图片对应 所以加1位
		// 胡
		for (AhmjPlayer seat : seatMap.values()) {
			PlayMajiangRes.Builder copy = builder.clone();
			// if (player.getUserId() == seat.getUserId()) {
			if (seat.canXiaoHu()) {
				// 能小胡 继续小胡
				copy.addAllXiaohu(seat.getXiaohu());

			} else {
				// 不能小胡可以做其他操作
				if (actionSeatMap.containsKey(seat.getSeat())) {
					copy.addAllSelfAct(actionSeatMap.get(seat.getSeat()));
				}
			}

			// 推送消息
			seat.writeSocket(copy.build());
		}
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + MjDisAction.action_xiaohu + "_"
				+ MajiangHelper.toMajiangStrs(showMajiangs) + "_" + (index + 1));
		calcXiaoHuPoint(player, index);
	}

	/**
	 * 玩家表示胡
	 * 
	 * @param player
	 * @param majiangs
	 */
	private void hu(AhmjPlayer player, List<Ahmj> majiangs, int action) {
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return;
		}

		if (huConfirmMap.containsKey(player.getSeat())) {
			return;
		}

		if(yiPaoDuoXiang==0){
			if (!checkActionNew(player,majiangs, action)) {
				return;
			}
		}

		boolean zimo = player.isAlreadyMoMajiang() || disCardRound == 0;
		Ahmj disMajiang = null;
		AhmjHu huBean = null;
		boolean isQGanghu = false;
		if (!zimo) {
			if (moGangHuList.contains(player.getSeat())) {
				// 强杠胡
				disMajiang = moGang.get(0);
				isQGanghu = true;
				player.changeActionNum(1,1);
			} else if (isHasGangAction(player.getSeat())) {
				// 杠上炮 杠上花
				for (int majiangId : gangSeatMap.keySet()) {
					AhmjHu temp = player.checkHu(Ahmj.getMajang(majiangId), Ahmj.getMajang(firstId), isFourWang(),
							disCardRound == 0, true, player.getSeat() == moMajiangSeat);
					if (temp.isHu()) {
						if (moMajiangSeat == player.getSeat()) {
							temp.setGangShangHua(true);
						} else {
							temp.setGangShangPao(true);
						}
						temp.initDahuList(maxPlayerCount);
						if (isYzWang() && temp.getWangNum() > 0) {
							AhmjHu checkYzWang = player.checkHu(Ahmj.getMajang(majiangId), null, isFourWang(),
									disCardRound == 0, true, player.getSeat() == moMajiangSeat);
							if (checkYzWang.isHu()) {
								// 不需要王也是庄
								checkYzWang.initDahuList(maxPlayerCount);
								if (temp.compare(checkYzWang.getDahuList())) {
									temp.setYzWang(true);
									temp.calcDahuList(maxPlayerCount);
								}
							}
						}

						if (huBean == null) {
							huBean = temp;
							setGangHuMajiangId(majiangId);

						} else {
							if (temp.getDahuPoint() > huBean.getDahuPoint()) {
								huBean = temp;
								setGangHuMajiangId(majiangId);
							}
							// huBean.addToDahu(temp.getDahuList());
						}

					}
				}
				if (huBean.isHu()) {
					if(getGangHuMajiangId()!=0&&player.getHandPais().size()%3==1){
						player.moMajiang(Ahmj.getMajang(getGangHuMajiangId()));
					}
					if (disCardSeat == player.getSeat()) {
						zimo = true;
					}
				}

			} else if (!nowDisCardIds.isEmpty()) {
				disMajiang = nowDisCardIds.get(0);
			}
		}
		if (huBean == null) {
			// 自摸
			player.changeActionNum(0,1);
			huBean = player.checkHu(disMajiang, Ahmj.getMajang(firstId), isFourWang(), disCardRound == 0, isQGanghu);
			huBean.setQGangHu(isQGanghu);
			if (!huBean.isHu() && isYzWang() && huBean.getWangDahuNum() == 0) {
				// 三王以下
				huBean = player.checkHu(disMajiang, null, isFourWang(), disCardRound == 0, isQGanghu);
			}
			// 检查硬庄
			if (isYzWang() && huBean.getWangNum() > 0) {
				huBean.initDahuList(maxPlayerCount);
				AhmjHu checkYzWang = player.checkHu(disMajiang, null, isFourWang(), disCardRound == 0, isQGanghu);
				if (checkYzWang.isHu()) {
					// 不需要王也是庄
					checkYzWang.initDahuList(maxPlayerCount);
					if (huBean.compare(checkYzWang.getDahuList())) {
						huBean.setYzWang(true);
						huBean.calcDahuList(maxPlayerCount);
					}
				}
			}
		}
		if (!huBean.isHu()) {
			LogUtil.e(id + "目前不能胡牌");
			return;
		}
		if (huBean.isHu()) {
			huBean.setZimo(zimo);
			huBean.initDahuList(maxPlayerCount);
		}
		// 算牌型的分
		if (isQGanghu) {
			if (disEventAction != MjDisAction.action_buzhang) {
				huBean.initDahuList(maxPlayerCount);
				// 抢杠胡
			}
			AhmjPlayer moGangPlayer = getPlayerByHasMajiang(moGang.get(0));
			if (moGangPlayer == null) {
				moGangPlayer = seatMap.get(moMajiangSeat);
			}

			List<Ahmj> moGangMajiangs = new ArrayList<>();
			moGangMajiangs.add(moGang.get(0));

			if (moGang.size() == 1) {
				moGangPlayer.addOutPais(moGangMajiangs, 0);
				// 摸杠被人胡了 相当于自己出了一张牌
				recordDisMajiang(moGangMajiangs, moGangPlayer);
				addPlayLog(
						disCardRound + "_" + player.getSeat() + "_" + 0 + "_" + MajiangHelper.toMajiangStrs(majiangs));

			} else {
				recordDisMajiang(moGangMajiangs, moGangPlayer);
				List<Ahmj> majiangLogList = new ArrayList<>();
				majiangLogList.addAll(moGang);
				majiangLogList.addAll(nowDisCardIds);
				addPlayLog(disCardRound + "_" + moGangPlayer.getSeat() + "_" + MjDisAction.action_minggang + "_"
						+ MajiangHelper.toMajiangStrs(majiangLogList));
			}

		}

		if (huBean.getDahuPoint() > 0) {
			player.setDahu(huBean.buildDahuList());
			if (zimo) {
				int point = 0;
				for (AhmjPlayer seatPlayer : seatMap.values()) {
					if (seatPlayer.getSeat() != player.getSeat()) {
						point += huBean.getDahuPoint();
						seatPlayer.changeLostPoint(-huBean.getDahuPoint());
					}
				}
				player.changeLostPoint(point);

			} else {
				player.changeLostPoint(huBean.getDahuPoint());
				seatMap.get(disCardSeat).changeLostPoint(-huBean.getDahuPoint());

			}
		}

		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, action, huBean.getShowMajiangs());
		builder.addAllHuArray(player.getDahu());
		if (zimo) {
			builder.setZimo(1);
		}

		// 胡
		for (AhmjPlayer seat : seatMap.values()) {
			// 推送消息
			seat.writeSocket(builder.build());
		}


		if(yiPaoDuoXiang==0)
			removeOtherHuAction(player.getSeat());
		// 加入胡牌数组
		addHuList(player.getSeat(), disMajiang == null ? 0 : disMajiang.getId());
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_");
		if (isCalcOver(player)) {
			// 等待别人胡牌 如果都确认完了，胡
			calcOver();

		} else {
			//removeActionSeat(player.getSeat());
			player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip, action);
		}
	}

	/**
	 * 找出拥有这张麻将的玩家
	 * 
	 * @param majiang
	 * @return
	 */
	private AhmjPlayer getPlayerByHasMajiang(Ahmj majiang) {
		for (AhmjPlayer player : seatMap.values()) {
			if (player.getHandMajiang() != null && player.getHandMajiang().contains(majiang)) {
				return player;
			}
			if (player.getOutMajing() != null && player.getOutMajing().contains(majiang)) {
				return player;
			}
		}
		return null;
	}

	private boolean isCalcOver() {
		List<Integer> huActionList = getHuSeatByActionMap();
		boolean over = false;
		if (!huActionList.isEmpty()) {
			over = true;
			AhmjPlayer moGangPlayer = null;
			if (!moGangHuList.isEmpty()) {
				// 如果有抢杠胡
				moGangPlayer = getPlayerByHasMajiang(moGang.get(0));
				LogUtil.monitor_i("mogang player:" + moGangPlayer.getSeat() + " moGang:" + moGang);

			}
			for (int huseat : huActionList) {
				if (moGangPlayer != null) {
					// 被抢杠的人可以胡的话 跳过
					if (moGangPlayer.getSeat() == huseat) {
						continue;
					}
				}
				if (!huConfirmMap.containsKey(huseat)) {
					over = false;
					break;
				}
			}
		}

		if (!over) {
			AhmjPlayer disMajiangPlayer = seatMap.get(disCardSeat);
			for (int huseat : huActionList) {
				if (!huConfirmMap.containsKey(huseat)) {
					continue;
				}
				PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
				AhmjPlayer seatPlayer = seatMap.get(huseat);
				buildPlayRes(disBuilder, disMajiangPlayer, 0, null);
				List<Integer> actionList = actionSeatMap.get(huseat);
				disBuilder.addAllSelfAct(actionList);
				seatPlayer.writeSocket(disBuilder.build());
			}
		}

		for (AhmjPlayer player : seatMap.values()) {
			if (player.isAlreadyMoMajiang() && !huConfirmMap.containsKey(player.getSeat())) {
				over = false;
			}
		}

		return over;
	}



	private boolean isCalcOver(AhmjPlayer player) {
		List<Integer> huActionList = getHuSeatByActionMap();
		boolean over = false;
		if (!huActionList.isEmpty()) {
			over = true;
			AhmjPlayer moGangPlayer = null;
			if (!moGangHuList.isEmpty()) {
				// 如果有抢杠胡
				moGangPlayer = getPlayerByHasMajiang(moGang.get(0));
				LogUtil.monitor_i("mogang player:" + moGangPlayer.getSeat() + " moGang:" + moGang);

			}
			for (int huseat : huActionList) {
				if (moGangPlayer != null) {
					// 被抢杠的人可以胡的话 跳过
					if (moGangPlayer.getSeat() == huseat) {
						continue;
					}
				}
				if (!huConfirmMap.containsKey(huseat)) {
					over = false;
					break;
				}
			}
		}

		if (!over) {
			AhmjPlayer disMajiangPlayer = seatMap.get(disCardSeat);
			for (int huseat : huActionList) {
				if(huseat==player.getSeat())
					continue;
				PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
				AhmjPlayer seatPlayer = seatMap.get(huseat);
				buildPlayRes(disBuilder, disMajiangPlayer, 0, null);
				List<Integer> actionList = actionSeatMap.get(huseat);
				disBuilder.addAllSelfAct(actionList);
				seatPlayer.writeSocket(disBuilder.build());
			}
		}

		for (AhmjPlayer p : seatMap.values()) {
			if (player.isAlreadyMoMajiang() && !huConfirmMap.containsKey(player.getSeat())) {
				over = false;
			}
		}

		return over;
	}



	/**
	 * 吃碰杠
	 * 
	 * @param player
	 * @param majiangs
	 * @param action
	 */
	private void disMajiang1(AhmjPlayer player, List<Ahmj> majiangs, int action) {
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		// if (nowDisCardIds.size() > 1 && !isHasGangAction()) {
		// // 当前出牌不能操作
		// return;
		// }
		List<Integer> huList = getHuSeatByActionMap();
		huList.remove((Object) player.getSeat());
//		if (!huList.isEmpty()) {
//			// 胡最优先
//			return;
//		}

//		if (!checkAction(player, action)) {
//			return;
//		}

		if (!checkActionNew(player, majiangs, action)) {
			LogUtil.msg("有优先级更高的操作需等待！");
			player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
			return;
		}

		List<Ahmj> handMajiang = new ArrayList<>(player.getHandMajiang());
		Ahmj disMajiang = null;
		if (isHasGangAction()) {
			List<Integer> majiangIds = MajiangHelper.toMajiangIds(majiangs);
			for (int majiangId : gangSeatMap.keySet()) {
				if (majiangIds.contains(majiangId)) {
					disMajiang = Ahmj.getMajang(majiangId);
					handMajiang.add(disMajiang);
					if (majiangs.size() > 1) {
						majiangs.remove(disMajiang);

					}
					break;
				}
			}
			if (disMajiang == null) {
				return;
			}

		} else {
			if (!nowDisCardIds.isEmpty()) {
				disMajiang = nowDisCardIds.get(0);

			}
		}

		int sameCount = 0;
		if (majiangs.size() > 0) {
			sameCount = MajiangHelper.getMajiangCount(majiangs, majiangs.get(0).getVal());

		}
		if (action == MjDisAction.action_buzhang) {
			majiangs = MajiangHelper.getMajiangList(handMajiang, majiangs.get(0).getVal());
			sameCount = majiangs.size();

		} else if (action == MjDisAction.action_minggang) {
			// 如果是杠 后台来找出是明杠还是暗杠
			majiangs = MajiangHelper.getMajiangList(handMajiang, majiangs.get(0).getVal());
			sameCount = majiangs.size();
			if (sameCount == 4) {
				// 有4张一样的牌是暗杠
				action = MjDisAction.action_angang;
			}
			// 其他是明杠

		}
		// /////////////////////
		if (action == MjDisAction.action_chi) {
			boolean can = canChi(player, handMajiang, majiangs, disMajiang);
			if (!can) {
				return;
			}
		} else if (action == MjDisAction.action_peng) {
			boolean can = canPeng(player, majiangs, sameCount, disMajiang);
			if (!can) {
				return;
			}
		} else if (action == MjDisAction.action_angang) {
			boolean can = canAnGang(player, majiangs, sameCount);
			if (!can) {
				return;
			}
		} else if (action == MjDisAction.action_minggang) {
			boolean can = canMingGang(player, handMajiang, majiangs, sameCount, disMajiang);
			if (!can) {
				return;
			}
			// 特殊处理一张牌明杠的时候别人可以胡
			if (canGangHu()) {
				if (checkQGangHu(player, majiangs, action)) {
					setNowDisCardSeat(player.getSeat());
					return;
				}
			}
		} else if (action == MjDisAction.action_buzhang) {
			boolean can = false;
			if (sameCount == 4) {
				can = canAnGang(player, majiangs, sameCount);
			} else {
				can = canMingGang(player, handMajiang, majiangs, sameCount, disMajiang);
			}
			if (!can) {
				return;
			}
			// 特殊处理一张牌明杠的时候别人可以胡
			if (sameCount == 1 && canGangHu()) {
				if (checkQGangHu(player, majiangs, action)) {
					setNowDisCardSeat(player.getSeat());
					return;
				}
			}
		} else {
			return;
		}

		calcPoint(player, action, sameCount, majiangs);
		boolean disMajiangMove = false;
		if (disMajiang != null) {
			// 碰或者杠
			if ((action != MjDisAction.action_minggang && action != MjDisAction.action_angang
					&& action != MjDisAction.action_buzhang)
					|| (action == MjDisAction.action_minggang && sameCount != 1)
					|| (action == MjDisAction.action_buzhang && sameCount != 1 && sameCount != 4)) {
				disMajiangMove = true;

			}

		}

		if (disMajiangMove) {
			if (action == MjDisAction.action_chi) {
				// 吃的牌放第二位
				majiangs.add(1, disMajiang);

			} else {
				majiangs.add(disMajiang);
			}

			builder.setFromSeat(disCardSeat);
			List<Ahmj> disMajiangs = new ArrayList<>();
			disMajiangs.add(disMajiang);
			seatMap.get(disCardSeat).removeOutPais(disMajiangs, action);
		}

		disMajiangPengGang(builder, player, majiangs, action);

	}

	private void disMajiangPengGang(PlayMajiangRes.Builder builder, AhmjPlayer player, List<Ahmj> majiangs,
									int action) {
		player.addOutPais(majiangs, action);
		buildPlayRes(builder, player, action, majiangs);
		removeActionSeat(player.getSeat());
		clearGangActionMap();
		clearActionSeatMap();
		changeDisCardRound(1);
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MajiangHelper.toMajiangStrs(majiangs));
		// 不是普通出牌
		setNowDisCardSeat(player.getSeat());

		if (action == MjDisAction.action_chi || action == MjDisAction.action_peng) {
			List<Integer> arr = player.checkMo(null, false);
			if (!arr.isEmpty()) {
				addActionSeat(player.getSeat(), arr);
			}
		}
		for (AhmjPlayer seatPlayer : seatMap.values()) {
			// 推送消息
			PlayMajiangRes.Builder copy = builder.clone();
			if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
				copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
			}
			seatPlayer.writeSocket(copy.build());
		}

		
		// 取消漏炮
		player.setPassMajiangVal(0);
		if (action == MjDisAction.action_minggang || action == MjDisAction.action_angang) {
			// 明杠和暗杠摸牌
			gangMoMajiang(player, 2, majiangs.get(0));

		} else if (action == MjDisAction.action_buzhang) {
			// 补张
			moMajiang(player, true);

		}
		setDisEventAction(action);
		robotDealAction();
	}

	private void buildPlayRes1(PlayMajiangRes.Builder builder) {
		// builder
	}

	private String startLeftCardsToJSON() {
		JSONArray jsonArray = new JSONArray();
		for (int card : startLeftCards) {
			jsonArray.add(card);
		}
		return jsonArray.toString();
	}

	private List<Integer> loadStartLeftCards(String json) {
		List<Integer> list = new ArrayList<>();
		if (json == null || json.isEmpty()) return list;
		JSONArray jsonArray = JSONArray.parseArray(json);
		for (Object val : jsonArray) {
			list.add(Integer.valueOf(val.toString()));
		}
		return list;
	}

	/**
	 * 杠后摸三张牌
	 */
	private void gangMoMajiang(AhmjPlayer player, int majiangCount, Ahmj gangMajiang) {
		if (state != table_state.play) {
			return;
		}
		if (player.isRobot()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (leftMajiangs.size() == 0) {
			calcOver();
			return;
		}
		// 连摸两张牌
		List<Ahmj> moList = new ArrayList<>();


		while (moList.size() < 3) {
			Ahmj majiang = getLeftMajiang();
			if (majiang != null)
				moList.add(majiang);
		}

		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + MjDisAction.action_moGangMjiang + "_"
				+ MajiangHelper.implodeMajiang(moList, ","));

		// 检查摸牌
		clearActionSeatMap();
		clearGangActionMap();
		// 打出这两张牌
		setDisCardSeat(player.getSeat());
		setGangDisMajiangs(moList);
		setMoMajiangSeat(player.getSeat());
		player.setPassMajiangVal(0);
		// /////////////////////////////////////////////////////////////////////////////////////////
		// 摸了牌后可以胡牌
		boolean hu = false;
		for (Ahmj majiang : moList) {
			List<Integer> actionList = player.checkDisMajiang(majiang, true, true, true);
			if (!actionList.isEmpty() && actionList.get(0) == 1) {
				// 可以胡牌
				addActionSeat(player.getSeat(), actionList);
				// 复制一个List到杠牌map里,不共用一个内存list
				List<Integer> copy = new ArrayList<>(actionList);
				addGangActionSeat(majiang.getId(), player.getSeat(), copy);
				hu = true;
			}
		}

        // 发送摸牌消息res
        for (AhmjPlayer seatPlayer : seatMap.values()) {
            GangMoMajiangRes.Builder gangbuilder = GangMoMajiangRes.newBuilder();
            gangbuilder.setRemain(getLeftMajiangCount());
            gangbuilder.setGangId(gangMajiang.getId());
            gangbuilder.setUserId(player.getUserId() + "");
            gangbuilder.setName(player.getName() + "");
            gangbuilder.setSeat(player.getSeat());
            for (Ahmj majiang : moList) {
                GangPlayMajiangRes.Builder playBuilder = GangPlayMajiangRes.newBuilder();
                playBuilder.setMajiangId(majiang.getId());
                Map<Integer, List<Integer>> seatActionList = gangSeatMap.get(majiang.getId());
                if (seatActionList != null) {
                    if (seatActionList.containsKey(seatPlayer.getSeat())) {
                        playBuilder.addAllSelfAct(seatActionList.get(seatPlayer.getSeat()));
                        break;
                    }
                }
                gangbuilder.addGangActs(playBuilder);
            }
            seatPlayer.writeSocket(gangbuilder.build());
        }

        if (hu) {
            for (AhmjPlayer seatPlayer : seatMap.values()) {
                GangMoMajiangRes.Builder gangbuilder = GangMoMajiangRes.newBuilder();
                gangbuilder.setRemain(getLeftMajiangCount());
                gangbuilder.setGangId(gangMajiang.getId());
                gangbuilder.setUserId(player.getUserId() + "");
                gangbuilder.setName(player.getName() + "");
                gangbuilder.setSeat(player.getSeat());
                for (Ahmj majiang : moList) {
                    GangPlayMajiangRes.Builder playBuilder = GangPlayMajiangRes.newBuilder();
                    playBuilder.setMajiangId(majiang.getId());
                    gangbuilder.addGangActs(playBuilder);
                }
                seatPlayer.writeSocket(gangbuilder.build());
            }
            hu(player, null, MjDisAction.action_hu);
        }else {
            for (Ahmj majiang : moList) {
                for (AhmjPlayer seatPlayer : seatMap.values()) {
                    List<Integer> actionList = seatPlayer.checkDisMajiang(majiang, true, true);
                    if (!actionList.isEmpty()) {
                        addActionSeat(seatPlayer.getSeat(), actionList);
                    }
                    addGangActionSeat(majiang.getId(), seatPlayer.getSeat(), actionList);
                }
            }

            setNowDisCardSeat(calcNextSeat(player.getSeat()));
            setNowDisCardIds(moList);
            player.addOutPais(moList, 0);
            setGangMajiang(gangMajiang);
        }

		checkSendActionMsg();

		if (isHasGangAction()) {
			// 如果有人能做动作
			robotDealAction();

		} else {
			checkMo();
		}

	}

	private boolean checkQGangHu(AhmjPlayer player, List<Ahmj> majiangs, int action) {
		removeActionSeat(player.getSeat());
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		Map<Integer, List<Integer>> huListMap = new HashMap<>();
		for (AhmjPlayer seatPlayer : seatMap.values()) {
			if (seatPlayer.getUserId() == player.getUserId()) {
				continue;
			}
			// 推送消息
			List<Integer> hu = seatPlayer.checkDisMajiang(majiangs.get(0), false, true);
			if (!hu.isEmpty() && hu.get(0) == 1) {
				addActionSeat(seatPlayer.getSeat(), hu);
				huListMap.put(seatPlayer.getSeat(), hu);
			}
		}

		// 可以胡牌
		if (!huListMap.isEmpty()) {
			setDisEventAction(action);
			setMoGang(majiangs, new ArrayList<>(huListMap.keySet()));
			buildPlayRes(builder, player, action, majiangs);
//			for (Entry<Integer, List<Integer>> entry : huListMap.entrySet()) {
//				PlayMajiangRes.Builder copy = builder.clone();
//				AhmjPlayer seatPlayer = seatMap.get(entry.getKey());
//				copy.addAllSelfAct(entry.getValue());
//				seatPlayer.writeSocket(copy.build());
//			}
			for (Integer seat:seatMap.keySet()) {
				if(huListMap.containsKey(seat)){
					PlayMajiangRes.Builder copy = builder.clone();
					AhmjPlayer seatPlayer = seatMap.get(seat);
					copy.addAllSelfAct(huListMap.get(seat));
					seatPlayer.writeSocket(copy.build());
				}else {
					PlayMajiangRes.Builder copy = builder.clone();
					AhmjPlayer seatPlayer = seatMap.get(seat);
					seatPlayer.writeSocket(copy.build());
				}
			}

			return true;
		}
		return false;

	}

	public void checkSendGangRes(Player player) {

	}

	/**
	 * 普通出牌
	 * 
	 * @param player
	 * @param majiangs
	 * @param action
	 */
	private void disMajiang2(AhmjPlayer player, List<Ahmj> majiangs, int action) {
		if (majiangs.size() != 1) {
			return;
		}
		if (!player.isAlreadyMoMajiang()) {
			// 还没有摸牌
			return;
		}
		if (!actionSeatMap.isEmpty()) {
			return;
		}

		if (!player.getGang().isEmpty()) {
			// 已经杠过了牌
			if (player.getLastMoMajiang().getId() != majiangs.get(0).getId()) {
				return;
			}
		}
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, action, majiangs);
		// 普通出牌
		clearActionSeatMap();
		clearGangActionMap();
		setNowDisCardSeat(calcNextSeat(player.getSeat()));
		recordDisMajiang(majiangs, player);
		changeDisCardRound(1);
		player.addOutPais(majiangs, action);
		for (AhmjPlayer seat : seatMap.values()) {
			List<Integer> list = new ArrayList<>();
			if (seat.getUserId() != player.getUserId()) {
				list = seat.checkDisMajiang(majiangs.get(0));
				if (list.contains(1)) {
					addActionSeat(seat.getSeat(), list);
				}
			}
		}
		setDisEventAction(action);
		sendDisMajiangAction(builder);
		// 取消漏炮
		player.setPassMajiangVal(0);
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MajiangHelper.toMajiangStrs(majiangs));
		// 给下一家发牌
		checkMo();

	}

	public List<Integer> getPengGangSeatByActionMap() {
		List<Integer> huList = new ArrayList<>();
		for (int seat : actionSeatMap.keySet()) {
			List<Integer> actionList = actionSeatMap.get(seat);
			if (actionList.get(0) == 3) {
				// 胡
				huList.add(seat);
			}

		}
		return huList;
	}

	public List<Integer> getHuSeatByActionMap() {
		List<Integer> huList = new ArrayList<>();
		for (int seat : actionSeatMap.keySet()) {
			List<Integer> actionList = actionSeatMap.get(seat);
			if (actionList.get(0) == 1) {
				// 胡
				huList.add(seat);
			}

		}
		return huList;
	}

    private void checkSendActionMsg() {
        if (actionSeatMap.isEmpty()) {
            return;
        }
        AhmjPlayer disPlayer = seatMap.get(disCardSeat);
        if (disPlayer == null) {
            return;
        }

        PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
        buildPlayRes(disBuilder, disPlayer, 0, null);

        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            disBuilder.clearSelfAct();
            List<Integer> actionList = entry.getValue();
            disBuilder.addAllSelfAct(actionList);
            AhmjPlayer seatPlayer = seatMap.get(entry.getKey());
            seatPlayer.writeSocket(disBuilder.build());
        }
    }

	private void buildPlayRes(PlayMajiangRes.Builder builder, Player player, int action, List<Ahmj> majiangs) {
		MajiangResTool.buildPlayRes(builder, player, action, majiangs);
		buildPlayRes1(builder);
	}

	private void sendDisMajiangAction(PlayMajiangRes.Builder builder) {
		// 如果有人可以胡 优先胡
		// 把胡的找出来
		buildPlayRes1(builder);
		List<Integer> huList = getHuSeatByActionMap();
		if (huList.size() > 0) {
			// 有人胡,优先胡
			for (AhmjPlayer seatPlayer : seatMap.values()) {
				PlayMajiangRes.Builder copy = builder.clone();
				List<Integer> actionList;
				// 只推送给胡牌的人改成了推送给所有人但是必须等胡牌的人先答复
				if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
					// if (huList.contains(seatPlayer.getSeat())) {
					actionList = actionSeatMap.get(seatPlayer.getSeat());
				} else {
					// 其他碰杠先无视
					actionList = new ArrayList<>();
				}
				copy.addAllSelfAct(actionList);
				seatPlayer.writeSocket(copy.build());
			}

		} else {
			// 没人胡，推送普通碰杠
			for (AhmjPlayer seat : seatMap.values()) {
				PlayMajiangRes.Builder copy = builder.clone();
				List<Integer> actionList;
				if (actionSeatMap.containsKey(seat.getSeat())) {
					actionList = actionSeatMap.get(seat.getSeat());
				} else {
					actionList = new ArrayList<>();
				}
				copy.addAllSelfAct(actionList);
				seat.writeSocket(copy.build());
			}
		}

	}

	private void err(AhmjPlayer player, int action, String errMsg) {
		LogUtil.e("play:tableId-->" + id + " playerId-->" + player.getUserId() + " action-->" + action + " err:"
				+ errMsg);
	}

	private boolean hasXiaohu() {
		for (AhmjPlayer player : seatMap.values()) {
			if (player.canXiaoHu()) {
				if (player.isRobot()) {
					disMajiang(player, new ArrayList<Ahmj>(), MjDisAction.action_xiaohu);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * 出牌
	 * 
	 * @param player
	 * @param majiangs
	 * @param action
	 */
	public synchronized void disMajiang(AhmjPlayer player, List<Ahmj> majiangs, int action) {
		logAction(player,action, majiangs);
		if (MjDisAction.action_xiaohu == action) {
			xiaoHu(player);
			return;
		}

		if (hasXiaohu()) {
			err(player, action, "必须先小胡");
			return;
		}

		// 被人抢杠胡
		if (!moGangHuList.isEmpty()) {
			if (!moGangHuList.contains(player.getSeat())) {
				return;
			}
		}

		if (MjDisAction.action_hu == action) {
			hu(player, majiangs, action);
			return;
		}

		if (majiangs != null && !majiangs.isEmpty()) {
			if (checkWang(majiangs)) {
				player.writeErrMsg("不能出王");
				return;
			}
		}

		// 手上没有要出的麻将
		if (!isHasGangAction() && action != MjDisAction.action_minggang
				&& action != MjDisAction.action_buzhang)
			if (majiangs != null && !player.getHandMajiang().containsAll(majiangs)) {
				err(player, action, "没有找到出的牌" + majiangs + player.getHandMajiang());
				return;
			}
		if (action == MjDisAction.action_pass) {
			pass(player, majiangs, action);
		} else {
			if (action == MjDisAction.action_moMjiang) {
			} else if (action != 0) {
				disMajiang1(player, majiangs, action);
			} else {
				disMajiang2(player, majiangs, action);
			}
		}
		if (player.isAlreadyMoMajiang()) {
			sendTingInfo(player);
		}
		for (AhmjPlayer p : seatMap.values()) {
			p.setLastCheckTime(System.currentTimeMillis());
		}
	}

	/**
	 * 最后一张牌(海底捞)
	 * 
	 * @param player
	 * @param action
	 */
	public synchronized void moLastMajiang(AhmjPlayer player, int action) {
		if (getLeftMajiangCount() != 1) {
			return;
		}
		if (player.getSeat() != askLastMajaingSeat) {
			return;
		}

		if (action == MjDisAction.action_passmo) {
			int next = calcNextSeat(player.getSeat());
			if (next == fristLastMajiangSeat) {
				calcOver();
				return;
			}
			setAskLastMajaingSeat(next);
			// 发送下一个海底摸牌res
			sendAskLastMajiangRes();
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + MjDisAction.action_pass + "_");
		} else {
			clearActionSeatMap();
			setMoLastMajiangSeat(player.getSeat());
			Ahmj majiang = getLeftMajiang();
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + MjDisAction.action_moLastMjiang + "_"
					+ majiang.getId());
			setMoMajiangSeat(player.getSeat());
			setLastMajiang(majiang);
			setDisCardSeat(player.getSeat());

			// /////////////////////////////////////////////
			// 发送海底捞的牌

			PlayMajiangRes.Builder res = MajiangResTool.buildDisMajiangRes(player, majiang);
			for (AhmjPlayer seatPlayer : seatMap.values()) {
				seatPlayer.writeSocket(res.build());
			}
			// /////////////////////////////////////////

			List<Ahmj> disMajiangs = new ArrayList<>();
			disMajiangs.add(majiang);
			setNowDisCardIds(disMajiangs);
			// 先看看自己能不能胡
			List<Integer> hu = player.checkDisMajiang(majiang);
			// 自己能胡
			if (!hu.isEmpty() && hu.get(0) == 1) {
				// 优先自己胡
				player.moMajiang(majiang);
				addActionSeat(player.getSeat(), hu);

			} else {
				for (AhmjPlayer seatPlayer : seatMap.values()) {
					List<Integer> actionList = seatPlayer.checkDisMajiang(majiang);
					if (!actionList.isEmpty() && actionList.get(0) == 1) {
						addActionSeat(seatPlayer.getSeat(), actionList);
					}
				}
			}
			if (actionSeatMap.isEmpty()) {
				calcOver();
			}

			for (int seat : actionSeatMap.keySet()) {
				hu(seatMap.get(seat), null, action);
			}

		}

	}

	private void passMoHu(AhmjPlayer player, List<Ahmj> majiangs, int action) {
		if (!moGangHuList.contains(player.getSeat())) {
			return;
		}

		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, action, majiangs);
		builder.setSeat(nowDisCardSeat);
		removeActionSeat(player.getSeat());
		player.writeSocket(builder.build());
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MajiangHelper.toMajiangStrs(majiangs));
		if (isCalcOver(player)) {
			calcOver();
			return;
		}
		player.setPassMajiangVal(nowDisCardIds.get(0).getVal());

		AhmjPlayer moGangPlayer = getPlayerByHasMajiang(moGang.get(0));
		// AHMajiangPlayer moGangPlayer = seatMap.get(moMajiangSeat);
		if (moGangHuList.isEmpty()) {
			majiangs = new ArrayList<>(moGang);
			if (moGang.size() == 3) {
				majiangs.addAll(nowDisCardIds);

			}
			calcPoint(moGangPlayer, MjDisAction.action_minggang, 1, majiangs);
			builder = PlayMajiangRes.newBuilder();
			disMajiangPengGang(builder, moGangPlayer, majiangs, MjDisAction.action_minggang);
		}

	}

	/**
	 * pass
	 * 
	 * @param player
	 * @param majiangs
	 * @param action
	 */
	private void pass(AhmjPlayer player, List<Ahmj> majiangs, int action) {
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return;
		}

		if (!moGangHuList.isEmpty()) {
			// 有摸杠胡的优先处理
			passMoHu(player, majiangs, action);
			refreshTempAction(player);// 先过 后执行临时可做操作里面优先级最高的玩家操作
			return;
		}

		List<Integer> selfActionList = actionSeatMap.get(player.getSeat());
		if (selfActionList.get(0) == 1) {
			// 手上只有王了不能过
			if (player.getExceptWangMajiangCount(wangValList) == 0) {
				return;
			}
			// 这是可以胡的动作 如果已经杠过了牌 自摸 不能过掉 只能胡
			if (!player.getGang().isEmpty() && player.isAlreadyMoMajiang()) {
				// 自摸摸到的是一张王 不能过牌 因为王不能出
				if (wangValList.contains(player.getLastMoMajiang().getVal())) {
					player.writeErrMsg(LangMsg.code_23);
					return;

				}
			}
		}

		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, action, majiangs);
		builder.setSeat(nowDisCardSeat);
		List<Integer> removeActionList = removeActionSeat(player.getSeat());
		player.writeSocket(builder.build());
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MajiangHelper.toMajiangStrs(majiangs));
		if (isCalcOver(player)) {
			calcOver();
			return;
		}

		if (removeActionList.get(0) == 1 && disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
			// 漏炮
			player.setPassMajiangVal(nowDisCardIds.get(0).getVal());

		}
		if (!actionSeatMap.isEmpty()) {
			AhmjPlayer disCSMajiangPlayer = seatMap.get(disCardSeat);
			PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
			buildPlayRes(disBuilder, disCSMajiangPlayer, 0, null);
			for (int seat : actionSeatMap.keySet()) {
				List<Integer> actionList = actionSeatMap.get(seat);
				if (actionList.get(0) == 1) {
					// 胡牌略过，因为胡牌消息在出牌的时候已经推送过了
					continue;
				}
				if (isHasGangAction(seat)) {
					continue;
				}
				PlayMajiangRes.Builder copy = disBuilder.clone();
				copy.addAllSelfAct(actionList);
				AhmjPlayer seatPlayer = seatMap.get(seat);
				seatPlayer.writeSocket(copy.build());
			}
		}

		// 杠牌后自动出牌
		if (player.isAlreadyMoMajiang() && !player.getGang().isEmpty()) {
			List<Ahmj> disMjiang = new ArrayList<>();
			disMjiang.add(player.getLastMoMajiang());
			disMajiang2(player, disMjiang, 0);
		}
		refreshTempAction(player);
		checkMo();
	}

	private void calcPoint(AhmjPlayer player, int action, int sameCount, List<Ahmj> majiangs) {
		if (sameCount == 3) {
			AhmjPlayer disPlayer = seatMap.get(disCardSeat);
			disPlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index13, 1);
		}
	}

	private void calcXiaoHuPoint(AhmjPlayer player, int xiaoIndex) {
		int count = player.getXiaoHuCount(xiaoIndex);
		int lostPoint = -2 * count;
		int getPoint = 6 * count;
		if (lostPoint != 0) {
			for (AhmjPlayer seat : seatMap.values()) {
				if (seat.getUserId() == player.getUserId()) {
					seat.changeGangPoint(getPoint);
				} else {
					seat.changeGangPoint(lostPoint);
				}
				// System.out.println(seat.getSeat() + " " + seat.getGangPoint());
			}
		}
	}

	private void recordDisMajiang(List<Ahmj> majiangs, AhmjPlayer player) {
		setNowDisCardIds(majiangs);
		setDisCardSeat(player.getSeat());
	}

	public List<Ahmj> getNowDisCardIds() {
		return nowDisCardIds;
	}

	public void setDisEventAction(int disAction) {
		this.disEventAction = disAction;
		changeExtend();
	}

	public void setNowDisCardIds(List<Ahmj> nowDisCardIds) {
		if (nowDisCardIds == null) {
			this.nowDisCardIds.clear();

		} else {
			this.nowDisCardIds = nowDisCardIds;

		}
		dbParamMap.put("nowDisCardIds", JSON_TAG);
	}

	/**
	 * 检查摸牌
	 */
	public void checkMo() {
		if (actionSeatMap.isEmpty()) {
			if (nowDisCardSeat != 0) {
				moMajiang(seatMap.get(nowDisCardSeat), false);

			}
			robotDealAction();

		} else {
			for (int seat : actionSeatMap.keySet()) {
				AhmjPlayer player = seatMap.get(seat);
				if (player != null && player.isRobot()) {
					// 如果是机器人可以直接决定
					List<Integer> actionList = actionSeatMap.get(seat);
					if (actionList == null) {
						continue;
					}
					List<Ahmj> list = new ArrayList<>();
					if (!nowDisCardIds.isEmpty()) {
						list = QipaiTool.getVal(player.getHandMajiang(), nowDisCardIds.get(0).getVal());
					}
					if (actionList.get(0) == 1) {
						// 胡
						disMajiang(player, new ArrayList<Ahmj>(), MjDisAction.action_hu);

					} else if (actionList.get(3) == 1) {
						disMajiang(player, list, MjDisAction.action_angang);

					} else if (actionList.get(2) == 1) {
						disMajiang(player, list, MjDisAction.action_minggang);

					} else if (actionList.get(1) == 1) {
						disMajiang(player, list, MjDisAction.action_peng);

					} else if (actionList.get(4) == 1) {
						disMajiang(player, player.getCanChiMajiangs(nowDisCardIds.get(0)), MjDisAction.action_chi);

					} else {
						// System.out.println("---------->" + JacksonUtil.writeValueAsString(actionList));
					}
				}
				// else {
				// // 是玩家需要发送消息
				// player.writeSocket(builder.build());
				// }

			}

		}
	}

	@Override
	protected void robotDealAction() {
		if (isTest()) {
			// for (AHMajiangPlayer player : seatMap.values()) {
			// if (player.isRobot() && player.canXiaoHu()) {
			// disMajiang(player, new ArrayList<Majiang>(),
			// MajiangDisAction.action_xiaohu);
			// }
			// }

			int nextseat = getNextActionSeat();
			AhmjPlayer next = seatMap.get(nextseat);
			if (next != null && next.isRobot()) {
				List<Integer> actionList = actionSeatMap.get(next.getSeat());
				if (actionList != null) {
					List<Ahmj> list = null;
					if (actionList.get(0) == 1) {
						// 胡
						disMajiang(next, new ArrayList<Ahmj>(), MjDisAction.action_hu);

					} else if (actionList.get(3) == 1) {
						// 机器人暗杠
						Map<Integer, Integer> handMap = MajiangHelper.toMajiangValMap(next.getHandMajiang());
						for (Entry<Integer, Integer> entry : handMap.entrySet()) {
							if (entry.getValue() == 4) {
								// 可以暗杠
								list = MajiangHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
							}
						}
						disMajiang(next, list, MjDisAction.action_angang);

					} else if (actionList.get(2) == 1) {
						Map<Integer, Integer> pengMap = MajiangHelper.toMajiangValMap(next.getPeng());
						for (Ahmj handMajiang : next.getHandMajiang()) {
							if (pengMap.containsKey(handMajiang.getVal())) {
								// 有碰过
								list = new ArrayList<>();
								list.add(handMajiang);
								disMajiang(next, list, MjDisAction.action_minggang);
								break;
							}
						}

					} else if (actionList.get(1) == 1) {
						if (list == null) {
							disMajiang(next, list, MjDisAction.action_pass);
						}

					} else if (actionList.get(4) == 1) {
						Ahmj majiang = null;
						if (nowDisCardIds.size() == 1) {
							majiang = nowDisCardIds.get(0);

						} else {
							for (int majiangId : gangSeatMap.keySet()) {
								Map<Integer, List<Integer>> actionMap = gangSeatMap.get(majiangId);
								List<Integer> action = actionMap.get(next.getSeat());
								if (action != null) {
									// List<Integer> disActionList =
									// MajiangDisAction.parseToDisActionList(action);
									if (action.get(4) == 1) {
										majiang = Ahmj.getMajang(majiangId);
										break;
									}

								}

							}

						}

						disMajiang(next, next.getCanChiMajiangs(majiang), MjDisAction.action_chi);

					} else {
						//System.out.println("!!!!!!!!!!" + JacksonUtil.writeValueAsString(actionList));

					}

				} else {
					int maJiangId = 0;
					if (!next.getGang().isEmpty()) {
						maJiangId = next.getLastMoMajiang().getId();
					} else {
						List<Integer> handMajiangs = new ArrayList<>(next.getHandPais());
						QipaiTool.dropMajiangId(handMajiangs, wangValList);
						maJiangId = RobotAI.getInstance().outPaiHandle(0, handMajiangs, new ArrayList<Integer>());
					}

					List<Ahmj> majiangList = MajiangHelper.toMajiang(Arrays.asList(maJiangId));
					if (next.isRobot()) {
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					disMajiang(next, majiangList, 0);
				}

			}
		}
	}

	@Override
	protected synchronized void deal() {
		if (lastWinSeat == 0) {
			setLastWinSeat(playerMap.get(masterId).getSeat());
		}
		if (lastWinSeat == 0) {
			setLastWinSeat(1);
		}
		setDisCardSeat(lastWinSeat);
		setNowDisCardSeat(lastWinSeat);
		setMoMajiangSeat(lastWinSeat);

		List<Integer> copy = AhmjConstants.getMajiangList();
		addPlayLog(copy.size() + "");
		List<List<Ahmj>> list = null;
		if (zp != null) {
			list = AhmjTool.fapai(copy, getMaxPlayerCount(), zp);
		}
		if(list==null){
			list = AhmjTool.fapai(copy, getMaxPlayerCount());
		}
		int seat=lastWinSeat;

		for (int i = 0; i < getMaxPlayerCount(); i++) {
			AhmjPlayer player = seatMap.get(seat);
			player.changeState(player_state.play);
			player.dealHandPais(list.get(i));
//			player.getFirstPais().clear();
//			player.getFirstPais().addAll(PaohuziTool.toPhzCardIds(new ArrayList(list.get(i))));
			seat=calcNextSeat(seat);

			StringBuilder sb = new StringBuilder("Ahmj");
			sb.append("|").append(getId());
			sb.append("|").append(getPlayBureau());
			sb.append("|").append(player.getUserId());
			sb.append("|").append(player.getSeat());
			sb.append("|").append(player.getName());
			sb.append("|").append("fapai");
			sb.append("|").append(player.getHandPais());
			LogUtil.msgLog.info(sb.toString());
		}

		List<Ahmj> cardList = list.get(list.size() - 1);


		// 桌上剩余的牌
		setLeftMajiangs(AhmjHelper.toMajiangIds(cardList));
		setStartLeftCards(AhmjHelper.toMajiangIds(cardList));
		setWangMajiang(getLeftMajiang().getId());
	}

	public void setStartLeftCards(List<Integer> startLeftCards) {
		if (startLeftCards == null) {
			this.startLeftCards.clear();
		} else {
			this.startLeftCards = startLeftCards;

		}
		changeExtend();
	}

	/**
	 * 初始化桌子上剩余牌
	 * 
	 * @param leftMajiangs
	 */
	public void setLeftMajiangs(List<Integer> leftMajiangs) {
		if (leftMajiangs == null) {
			this.leftMajiangs.clear();
		} else {
			this.leftMajiangs = leftMajiangs;

		}
		dbParamMap.put("leftPais", JSON_TAG);
	}

	/**
	 * 剩余牌的第一张
	 * 
	 * @return
	 */
	public Ahmj getLeftMajiang() {
		if (this.leftMajiangs.size() > 0) {
			int majiang = this.leftMajiangs.remove(0);
			dbParamMap.put("leftPais", JSON_TAG);
			return Ahmj.getMajang(majiang);
		}
		return null;
	}

	/**
	 * 桌上剩余的牌数
	 * 
	 * @return
	 */
	public int getLeftMajiangCount() {
		return this.leftMajiangs.size();
		// return 1;
	}

	/**
	 * 综合动作得出下一个可以出牌的人的座位
	 * 
	 * @return
	 */
	public int getNextActionSeat() {
		if (actionSeatMap.isEmpty()) {
			return getNextDisCardSeat();

		} else {
			int seat = 0;
			for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
				if (seat == 0) {
					seat = entry.getKey();
				}
				if (entry.getValue().get(0) == 1) {// 胡
					return entry.getKey();
				}
				if (entry.getValue().get(2) == 1) {// 杠
					return entry.getKey();
				}
				if (entry.getValue().get(1) == 1) {// 碰
					return entry.getKey();
				}
				if (entry.getValue().get(4) == 1) {// 吃
					return entry.getKey();
				}
			}
			return seat;
		}
	}

	@Override
	public int getNextDisCardSeat() {
		if (state != table_state.play) {
			return 0;
		}
		if (disCardRound == 0) {
			return lastWinSeat;
		} else {
			return nowDisCardSeat;
		}
	}

	/**
	 * 计算seat右边的座位
	 * 
	 * @param seat
	 * @return
	 */
	public int calcNextSeat(int seat) {
		return seat + 1 > maxPlayerCount ? 1 : seat + 1;
	}

	@Override
	public Player getPlayerBySeat(int seat) {
		return seatMap.get(seat);
	}

	@Override
	public Map<Integer, Player> getSeatMap() {
		Object o = seatMap;
		return (Map<Integer, Player>) o;
	}

	/**
	 * 自动出牌
	 */
	public synchronized void autoPlay() {
		if (state != table_state.play) {
			return;
		}
		if (!actionSeatMap.isEmpty()) {
			List<Integer> huSeatList = getHuSeatByActionMap();
			if (!huSeatList.isEmpty()) {
				//有胡处理胡
				for (int seat : huSeatList) {
					AhmjPlayer player = seatMap.get(seat);
					if (player == null) {
						continue;
					}
					if (!player.checkAutoPlay(2, false)) {
						continue;
					}
					disMajiang(player, new ArrayList<>(), MjDisAction.action_hu);
				}
				return;
			} else {
				int action = 0, seat = 0;
				for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
					seat = entry.getKey();
					List<Integer> actList = MjDisAction.parseToDisActionList(entry.getValue());
					if (actList == null) {
						continue;
					}

					action = MjDisAction.getAutoMaxPriorityAction(actList);
					AhmjPlayer player = seatMap.get(seat);
					if (!player.checkAutoPlay(0, false)) {
						continue;
					}
					boolean chuPai = false;
					if (player.isAlreadyMoMajiang()) {
						chuPai = true;
					}
					if (action == MjDisAction.action_peng) {
						if (player.isAutoPlaySelf()) {
							//自己开启托管直接过
							disMajiang(player, new ArrayList<>(), MjDisAction.action_pass);
							if (chuPai) {
								autoChuPai(player);
							}
						} else {
							if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
								Ahmj mj = nowDisCardIds.get(0);
								List<Ahmj> mjList = new ArrayList<>();
								for (Ahmj handMj : player.getHandMajiang()) {
									if (handMj.getVal() == mj.getVal()) {
										mjList.add(handMj);
										if (mjList.size() == 2) {
											break;
										}
									}
								}
								disMajiang(player, mjList, MjDisAction.action_peng);
							}
						}
					} else {
						disMajiang(player, new ArrayList<>(), MjDisAction.action_pass);
						if (chuPai) {
							autoChuPai(player);
						}
					}
				}
			}
		} else {
			AhmjPlayer player = seatMap.get(nowDisCardSeat);
			if (player == null || !player.checkAutoPlay(0, false)) {
				return;
			}
			autoChuPai(player);
		}
	}

	public void autoChuPai(AhmjPlayer player) {
		if (!player.isAlreadyMoMajiang()) {
			return;
		}
		List<Integer> handMjIds = new ArrayList<>(player.getHandPais());
		int mjId = -1;
		if (moMajiangSeat == player.getSeat()) {
			mjId = getMjIdNoWang(handMjIds,1);
		} else {
			Collections.sort(handMjIds);
			mjId = getMjIdNoWang(handMjIds,1);
		}
		if (mjId != -1) {
			List<Ahmj> mjList = new ArrayList<>();
			mjList.add(Ahmj.getMajang(mjId));
			disMajiang(player, mjList, MjDisAction.action_chupai);
		}
	}

	private int getMjIdNoWang(List<Integer> handMjIds,int num){
		int mjId = handMjIds.get(handMjIds.size() - num);
		if(wangValList.contains(Ahmj.getMajang(mjId).getVal()))
			return getMjIdNoWang(handMjIds,num+1);
		return mjId;
	}


	@Override
	public CreateTableRes buildCreateTableRes(long userId, boolean isrecover, boolean isLastReady) {
		CreateTableRes.Builder res = CreateTableRes.newBuilder();
		buildCreateTableRes0(res);
		res.setNowBurCount(getPlayBureau());
		res.setTotalBurCount(getTotalBureau());
		res.setGotyeRoomId(gotyeRoomId + "");
		res.setTableId(getId() + "");
		res.setWanfa(playType);
		res.addExt(payType);                //0
		res.addExt(isCalcBanker);           //1
		res.addExt(isAutoPlay);             //2
		res.addExt(diFen);                  //3
		res.addExt(firstId);                  //4
		res.setMasterId(getMasterId() + "");
		if (leftMajiangs != null) {
			res.setRemain(leftMajiangs.size());
		} else {
			res.setRemain(0);
		}
		List<PlayerInTableRes> players = new ArrayList<>();
		for (AhmjPlayer player : playerMap.values()) {
			PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(isrecover);
			if (player.getUserId() == userId) {
				playerRes.addAllHandCardIds(player.getHandPais());
				if (!player.getHandMajiang().isEmpty() && player.getHandMajiang().size() % 3 == 1) {
					if (player.isOkPlayer() && AhmjTool.isTing(player.getHandMajiang(), true)) {
						playerRes.setUserSate(3);
					}
				}
			}
			if (player.getSeat() == disCardSeat && nowDisCardIds != null && moGangHuList.isEmpty()) {
				playerRes.addAllOutCardIds(AhmjHelper.toMajiangIds(nowDisCardIds));
			}
			playerRes.addRecover(player.getIsEntryTable());
			playerRes.addRecover(player.getSeat() == lastWinSeat ? 1 : 0);
			if (actionSeatMap.containsKey(player.getSeat())) {
				if (!tempActionMap.containsKey(player.getSeat()) && !huConfirmMap.containsKey(player.getSeat())) {// 如果已做临时操作
					// 则不发送前端可做的操作
					// 或者已经操作胡了
					playerRes.addAllRecover(actionSeatMap.get(player.getSeat()));
				}
			}
			players.add(playerRes.build());
		}
		res.addAllPlayers(players);
		if (actionSeatMap.isEmpty()) {
			int nextSeat = getNextDisCardSeat();
			if (nextSeat != 0) {
				res.setNextSeat(nextSeat);
			}
		} else if (!moGangHuList.isEmpty()) {
			for (AhmjPlayer player : seatMap.values()) {
				if (player.getmGang() != null && player.getmGang().contains(moGang)) {
					res.setNextSeat(player.getSeat());
					break;
				}
			}
		}
		res.setRenshu(getMaxPlayerCount());
		res.setLastWinSeat(getLastWinSeat());
		res.addTimeOut((int) AhmjConstants.AUTO_TIMEOUT);
		return res.build();
	}


	@SuppressWarnings("unchecked")
	@Override
	public <T> T getPlayer(long id, Class<T> cl) {
		return (T) playerMap.get(id);
	}

	@Override
	public int getMaxPlayerCount() {
		return maxPlayerCount;
	}

	@Override
	public Map<Long, Player> getPlayerMap() {
		Object o = playerMap;
		return (Map<Long, Player>) o;
	}

	@Override
	protected void initNext1() {
		clearHuList();
		clearActionSeatMap();
		clearGangActionMap();
		setLeftMajiangs(null);
		setNowDisCardIds(null);
		clearMoGang();
		clearGangDisMajiangs();
		setAskLastMajaingSeat(0);
		setFristLastMajiangSeat(0);
		setMoLastMajiangSeat(0);
		setDisEventAction(0);
		setLastMajiang(null);
		setWangMajiang(0);
		setFirstId(0);
		readyTime=0;
		firstId=0;
	}

	public void setFirstId(int firstId) {
		this.firstId = firstId;
	}

	@Override
	public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {
	}

	/**
	 * 除了胡牌的人 其他人的动作全部移掉
	 * 
	 * @param huSeat
	 */
	public void removeOtherHuAction(int huSeat) {
		Iterator<Integer> iterator = actionSeatMap.keySet().iterator();
		while (iterator.hasNext()) {
			int seat = (Integer) iterator.next();
			if (seat == huSeat) {
				continue;
			}
			if (moGangHuList.contains(seat)) {
				removeMoGang(seat);
			}
			removeGangActionSeat(0, seat);
			iterator.remove();
		}

	}

	/**
	 * 去掉seat的action
	 * 
	 * @param seat
	 * @return
	 */
	public List<Integer> removeActionSeat(int seat) {
		List<Integer> actionList = actionSeatMap.remove(seat);
		if (moGangHuList.contains(seat)) {
			removeMoGang(seat);
		}
		removeGangActionSeat(0, seat);
		saveActionSeatMap();
		return actionList;
	}

	public boolean isHasGangAction() {
		boolean has = false;
		if (gangSeatMap.isEmpty()) {
			has = false;
		}
		for (Map<Integer, List<Integer>> actionList : gangSeatMap.values()) {
			if (!actionList.isEmpty()) {
				has = true;
				break;
			}
		}
		return has;
	}

	public boolean isHasGangAction(int seat) {
		boolean has = false;
		for (Map<Integer, List<Integer>> actionMap : gangSeatMap.values()) {
			if (!actionMap.isEmpty() && actionMap.containsKey(seat)) {
				has = true;
				break;
			}
		}
		return has;
	}

	public boolean isHasGangAction(int majiang, int seat) {
		if (gangSeatMap.containsKey(majiang)) {
			if (gangSeatMap.get(majiang).containsKey(seat)) {
				return true;
			}
		}
		return false;
	}

	public void removeGangActionSeat(int majiangId, int seat) {
		if (majiangId != 0) {
			Map<Integer, List<Integer>> actionMap = gangSeatMap.get(majiangId);
			if (actionMap != null) {
				actionMap.remove(seat);
				saveActionSeatMap();

			}
		} else {
			for (Map<Integer, List<Integer>> actionMap : gangSeatMap.values()) {
				actionMap.remove(seat);
			}
			saveActionSeatMap();
		}

	}

	public void addGangActionSeat(int majiang, int seat, List<Integer> actionList) {
		Map<Integer, List<Integer>> actionMap;
		if (gangSeatMap.containsKey(majiang)) {
			actionMap = gangSeatMap.get(majiang);
		} else {
			actionMap = new HashMap<>();
			gangSeatMap.put(majiang, actionMap);
		}
		if (!actionList.isEmpty()) {
			actionMap.put(seat, actionList);

		}
		saveActionSeatMap();
	}

	public void clearGangActionMap() {
		if (!gangSeatMap.isEmpty()) {
			gangSeatMap.clear();
			saveActionSeatMap();
		}
	}

	public void addActionSeat(int seat, List<Integer> actionlist) {
		if (actionSeatMap.containsKey(seat)) {
			List<Integer> a = actionSeatMap.get(seat);
			DataMapUtil.appendList(a, actionlist);
			addPlayLog(
					disCardRound + "_" + seat + "_" + MjDisAction.action_hasAction + "_" + StringUtil.implode(a));
		} else {
			actionSeatMap.put(seat, actionlist);
			addPlayLog(disCardRound + "_" + seat + "_" + MjDisAction.action_hasAction + "_"
					+ StringUtil.implode(actionlist));
		}
		saveActionSeatMap();
	}

	public void clearActionSeatMap() {
		if (!actionSeatMap.isEmpty()) {
			actionSeatMap.clear();
			saveActionSeatMap();
		}
	}

	public void clearHuList() {
		huConfirmMap.clear();
		changeExtend();
	}

	public void addHuList(int seat, int majiangId) {
		if (!huConfirmMap.containsKey(seat)) {
			huConfirmMap.put(seat, majiangId);

		}
		changeExtend();
	}

	public void saveActionSeatMap() {
		dbParamMap.put("nowAction", JSON_TAG);
	}

	@Override
	protected void initNowAction(String nowAction) {
		JsonWrapper wrapper = new JsonWrapper(nowAction);
		String val1 = wrapper.getString(1);
		if (!StringUtils.isBlank(val1)) {
			actionSeatMap = DataMapUtil.toListMap(val1);

		}
		String val2 = wrapper.getString(2);
		if (!StringUtils.isBlank(val2)) {
			gangSeatMap = DataMapUtil.toListMapMap(val2);

		}
	}

	@Override
	protected void loadFromDB1(TableInf info) {
		if (!StringUtils.isBlank(info.getNowDisCardIds())) {
			nowDisCardIds = MajiangHelper.toMajiang(StringUtil.explodeToIntList(info.getNowDisCardIds()));
		}

		if (!StringUtils.isBlank(info.getLeftPais())) {
			leftMajiangs = StringUtil.explodeToIntList(info.getLeftPais());
		}
	}



	/**
	 * 是否能碰
	 * 
	 * @param player
	 * @param handMajiang
	 * @param majiangs
	 * @param disMajiang
	 * @return
	 */
	private boolean canChi(AhmjPlayer player, List<Ahmj> handMajiang, List<Ahmj> majiangs,
						   Ahmj disMajiang) {
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return false;
		}
		List<Integer> pengGangSeatList = getPengGangSeatByActionMap();
		pengGangSeatList.remove((Object) player.getSeat());
		if (!pengGangSeatList.isEmpty()) {
			return false;
		}

		if (disMajiang == null) {
			return false;
		}

		if (!handMajiang.containsAll(majiangs)) {
			return false;
		}

		List<Ahmj> chi = AhmjTool.checkChi(majiangs, disMajiang, wangValList);
		return !chi.isEmpty();
	}

	/**
	 * 是否能碰
	 * 
	 * @param player
	 * @param majiangs
	 * @param sameCount
	 * @return
	 */
	private boolean canPeng(AhmjPlayer player, List<Ahmj> majiangs, int sameCount, Ahmj disMajiang) {
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return false;
		}
		if (sameCount != 2) {
			return false;
		}
		if (disMajiang == null) {
			return false;
		}
		if (majiangs.get(0).getVal() != disMajiang.getVal()) {
			return false;
		}

		return true;
	}

	/**
	 * 是否能明杠
	 * 
	 * @param player
	 * @param majiangs
	 * @param sameCount
	 * @return
	 */
	private boolean canAnGang(AhmjPlayer player, List<Ahmj> majiangs, int sameCount) {
		if (sameCount != 4) {
			return false;
		}
		if (player.getSeat() != getNextDisCardSeat()) {
			return false;
		}
		if (!player.checkGang(player.getHandMajiang(), Ahmj.getMajang(firstId), isFourWang(), null, majiangs.get(0).getVal())) {
			player.writeErrMsg(LangMsg.code_24);
			return false;
		}
		return true;
	}


	/**
	 * 检查优先度 如果同时出现一个事件，按出牌座位顺序优先
	 */
	private boolean checkActionNew(AhmjPlayer player, List<Ahmj> cardList,  int action) {
		boolean canAction = checkCanAction(player, action);// 是否优先级最高 能执行操作
		if (!canAction) {// 不能操作时  存入临时操作
			int seat = player.getSeat();
			tempActionMap.put(seat, new AhmjTempAction(seat, action, cardList));
			// 玩家都已选择自己的临时操作后  选取优先级最高
			if (tempActionMap.size() == actionSeatMap.size()) {
				int maxAction = Integer.MAX_VALUE;
				int maxSeat = 0;
				Map<Integer, Integer> prioritySeats = new HashMap<>();
				int maxActionSize = 0;
				for (AhmjTempAction temp : tempActionMap.values()) {
					if (temp.getAction() < maxAction) {
						maxAction = temp.getAction();
						maxSeat = temp.getSeat();
					}
					prioritySeats.put(temp.getSeat(), temp.getAction());
				}
				Set<Integer> maxPrioritySeats = new HashSet<>();
				for (int mActionSet : prioritySeats.keySet()) {
					if (prioritySeats.get(mActionSet) == maxAction) {
						maxActionSize++;
						maxPrioritySeats.add(mActionSet);
					}
				}
				if (maxActionSize > 1) {
					maxSeat = getNearSeat(disCardSeat, new ArrayList<>(maxPrioritySeats));
					maxAction = prioritySeats.get(maxSeat);
				}
				AhmjPlayer tempPlayer = seatMap.get(maxSeat);
				List<Ahmj> tempCardList = tempActionMap.get(maxSeat).getCardList();
				List<Integer> tempHuCards = tempActionMap.get(maxSeat).getHucards();
				for (int removeSeat : prioritySeats.keySet()) {
					if (removeSeat != maxSeat) {
						removeActionSeat(removeSeat);
					}
				}
				clearTempAction();
				disMajiang(tempPlayer, tempCardList, maxAction);// 系统选取优先级最高操作
			} else {
				if (isCalcOver(player)) {
					calcOver();
				}
			}
		} else {// 能操作 清理所有临时操作
			clearTempAction();
		}
		return canAction;
	}


	private void clearTempAction() {
		if (!tempActionMap.isEmpty()) {
			tempActionMap.clear();
			changeExtend();
		}
	}


	/**
	 * 执行可做操作里面优先级最高的玩家操作
	 *
	 * @param player
	 */
	private void refreshTempAction(AhmjPlayer player) {
		tempActionMap.remove(player.getSeat());
		Map<Integer, Integer> prioritySeats = new HashMap<>();//各位置优先操作
		for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
			int seat = entry.getKey();
			List<Integer> actionList = entry.getValue();
			List<Integer> list = MjDisAction.parseToDisActionList(actionList);
			int priorityAction = MjDisAction.getMaxPriorityAction(list);
			prioritySeats.put(seat, priorityAction);
		}
		int maxPriorityAction = Integer.MAX_VALUE;
		int maxPrioritySeat = 0;
		boolean isSame = true;//是否有相同操作
		for (int seat : prioritySeats.keySet()) {
			if (maxPrioritySeat != Integer.MAX_VALUE && maxPrioritySeat != prioritySeats.get(seat)) {
				isSame = false;
			}
			if (prioritySeats.get(seat) < maxPriorityAction) {
				maxPriorityAction = prioritySeats.get(seat);
				maxPrioritySeat = seat;
			}
		}
		if (isSame) {
			maxPrioritySeat = getNearSeat(disCardSeat, new ArrayList<>(prioritySeats.keySet()));
		}
		Iterator<AhmjTempAction> iterator = tempActionMap.values().iterator();
		while (iterator.hasNext()) {
			AhmjTempAction tempAction = iterator.next();
			if (tempAction.getSeat() == maxPrioritySeat) {
				int action = tempAction.getAction();
				List<Ahmj> tempCardList = tempAction.getCardList();
				List<Integer> tempHuCards = tempAction.getHucards();
				AhmjPlayer tempPlayer = seatMap.get(tempAction.getSeat());
				iterator.remove();
				disMajiang(tempPlayer, tempCardList, action);// 系统选取优先级最高操作
				break;
			}
		}
		changeExtend();
	}

	/**
	 * 检查优先度，胡杠补碰吃 如果同时出现一个事件，按出牌座位顺序优先
	 *
	 * @param player
	 * @param action
	 * @return
	 */
	public boolean checkCanAction(AhmjPlayer player, int action) {
		// 优先度为胡杠补碰吃
		List<Integer> stopActionList = MjDisAction.findPriorityAction(action);
		for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
			if (player.getSeat() != entry.getKey()) {
				// 别人
				boolean can = MjDisAction.canDisMajiang(stopActionList, entry.getValue());
				if (!can) {
					return false;
				}
				List<Integer> disActionList = MjDisAction.parseToDisActionList(entry.getValue());
				if (disActionList.contains(action)) {
					// 同时拥有同一个事件 根据座位号来判断
					int actionSeat = entry.getKey();
					int nearSeat = getNearSeat(nowDisCardSeat, Arrays.asList(player.getSeat(), actionSeat));
					if (nearSeat != player.getSeat()) {
						return false;
					}
				}
			}
		}
		return true;
	}


	/**
	 * 是否能暗杠
	 * 
	 * @param player
	 * @param majiangs
	 * @param sameCount
	 * @return
	 */
	private boolean canMingGang(AhmjPlayer player, List<Ahmj> handMajiang, List<Ahmj> majiangs,
								int sameCount, Ahmj disMajiang) {
		List<Integer> pengList = MajiangHelper.toMajiangVals(player.getPeng());
		if (!player.checkGang(player.getHandMajiang(), Ahmj.getMajang(firstId), isFourWang(), null, majiangs.get(0).getVal())) {
			player.writeErrMsg(LangMsg.code_24);
			return false;
		}
		if (majiangs.size() == 1) {
			if (!isHasGangAction() && player.getSeat() != getNextDisCardSeat()) {
				return false;
			}
			if (handMajiang.containsAll(majiangs) && pengList.contains(majiangs.get(0).getVal())) {
				return true;
			}
		} else if (majiangs.size() == 3) {
			if (sameCount != 3) {
				return false;
			}
			if (!actionSeatMap.containsKey(player.getSeat())) {
				return false;
			}
			if (disMajiang == null || disMajiang.getVal() != majiangs.get(0).getVal()) {
				return false;
			}
			return true;
		}

		return false;
	}

	public Map<Integer, List<Integer>> getActionSeatMap() {
		robotDealAction();
		return actionSeatMap;
	}

	public int getIsAutoPlay() {
		return isAutoPlay;
	}

	public void setIsAutoPlay(int isAutoPlay) {
		this.isAutoPlay = isAutoPlay;
	}

	public int getBirdNum() {
		return birdNum;
	}

	public void setBirdNum(int birdNum) {
		this.birdNum = birdNum;
		changeExtend();
	}

	public void setMoMajiangSeat(int moMajiangSeat) {
		this.moMajiangSeat = moMajiangSeat;
		changeExtend();
	}

	public void setAskLastMajaingSeat(int askLastMajaingSeat) {
		this.askLastMajaingSeat = askLastMajaingSeat;
		changeExtend();
	}

	public void setFristLastMajiangSeat(int fristLastMajiangSeat) {
		this.fristLastMajiangSeat = fristLastMajiangSeat;
		changeExtend();
	}

	public void setLastMajiang(Ahmj lastMajiang) {
		this.lastMajiang = lastMajiang;
		changeExtend();
	}

	public void setMoLastMajiangSeat(int moLastMajiangSeat) {
		this.moLastMajiangSeat = moLastMajiangSeat;
		changeExtend();
	}

	public void setGangMajiang(Ahmj gangMajiang) {
		this.gangMajiang = gangMajiang;
		changeExtend();
	}

	/**
	 * 摸杠别人可以胡
	 * 
	 * @param moGang
	 *            杠的牌
	 * @param moGangHuList
	 *            可以胡的人的座位list
	 */
	public void setMoGang(List<Ahmj> moGang, List<Integer> moGangHuList) {
		this.moGang = moGang;
		this.moGangHuList = moGangHuList;
		changeExtend();
	}

	/**
	 * 清除摸刚胡
	 */
	public void clearMoGang() {
		this.moGang = null;
		this.moGangHuList.clear();
		changeExtend();
	}

	public void setGangDisMajiangs(List<Ahmj> gangDisMajiangs) {
		this.gangDisMajiangs = gangDisMajiangs;
		changeExtend();
	}

	/**
	 * 清理杠后摸的牌
	 */
	public void clearGangDisMajiangs() {
		this.gangMajiang = null;
		this.gangDisMajiangs.clear();
		changeExtend();
	}

	/**
	 * pass 摸杠胡
	 * 
	 * @param seat
	 */
	public void removeMoGang(int seat) {
		this.moGangHuList.remove((Object) seat);
		changeExtend();
	}

	public int getMoMajiangSeat() {
		return moMajiangSeat;
	}

	@Override
	protected String buildNowAction() {
		JsonWrapper wrapper = new JsonWrapper("");
		wrapper.putString(1, DataMapUtil.explodeListMap(actionSeatMap));
		wrapper.putString(2, DataMapUtil.explodeListMapMap(gangSeatMap));
		// w
		return wrapper.toString();
	}

	@Override
	public void setConfig(int index, int val) {

	}

	/**
	 * 只能自摸胡
	 * 
	 * @return
	 */
	public boolean moHu() {
		if (getConifg(0) == 2) {
			return true;

		}
		return false;
	}

	/**
	 * 能抢杠胡
	 * 
	 * @return
	 */
	public boolean canGangHu() {
		return true;
	}

	/**
	 * 4王
	 * 
	 * @return
	 */
	public boolean isFourWang() {
		return wangType==1;
	}

	/**
	 * 硬庄王
	 * 
	 * @return
	 */
	public boolean isYzWang() {
		return wangDaiYing==1;
	}

	public ClosingMjInfoRes.Builder sendAccountsMsg(boolean over, boolean selfMo, List<Integer> winList,
			int[] prickBirdMajiangIds, int[] seatBirds, Map<Integer, Integer> seatBridMap, boolean isBreak) {

		//大结算计算加倍分
		if (over && jiaBei == 1) {
			int jiaBeiPoint = 0;
			int loserCount = 0;
			for (AhmjPlayer player : seatMap.values()) {
				if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
					jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
					player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
				} else if (player.getTotalPoint() < 0) {
					loserCount++;
				}
			}
			if (jiaBeiPoint > 0) {
				for (AhmjPlayer player : seatMap.values()) {
					if (player.getTotalPoint() < 0) {
						player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
					}
				}
			}
		}

		//大结算低于below分+belowAdd分
		if(over&&belowAdd>0&&playerMap.size()==2){
			for (AhmjPlayer player : seatMap.values()) {
				int totalPoint = player.getTotalPoint();
				if (totalPoint >-below&&totalPoint<0) {
					player.setTotalPoint(player.getTotalPoint()-belowAdd);
				}else if(totalPoint < below&&totalPoint>0){
					player.setTotalPoint(player.getTotalPoint()+belowAdd);
				}
			}
		}


		List<ClosingMjPlayerInfoRes> list = new ArrayList<>();
		List<ClosingMjPlayerInfoRes.Builder> builderList = new ArrayList<>();
		int fangPaoSeat = selfMo ? 0 : disCardSeat;
		for (AhmjPlayer player : seatMap.values()) {
			ClosingMjPlayerInfoRes.Builder build = null;
			if (over) {
				build = player.bulidTotalClosingPlayerInfoRes();
			} else {
				build = player.bulidOneClosingPlayerInfoRes();
			}
			if (seatBridMap != null && seatBridMap.containsKey(player.getSeat())) {
				build.setBirdPoint(seatBridMap.get(player.getSeat()));
			} else {
				build.setBirdPoint(0);
			}
			if (winList != null && winList.contains(player.getSeat())) {
				if (!selfMo) {
					// 不是自摸
					if(player.isGangshangPao()){
						//抢杠胡
						build.setIsHu(player.getLastMoMajiang().getId());
					}else {
						Ahmj huMajiang = nowDisCardIds.get(0);
						if (!build.getHandPaisList().contains(huMajiang.getId())) {
							build.addHandPais(huMajiang.getId()   );
						}
						build.setIsHu(huMajiang.getId());
					}
				} else {
					build.setIsHu(player.getLastMoMajiang().getId());
				}
			}
			if (player.getSeat() == fangPaoSeat) {
				build.setFanPao(1);
				if(huConfirmMap.isEmpty()&&leftMajiangs.isEmpty())
					build.setFanPao(0);
			}
			if (winList != null && winList.contains(player.getSeat())) {
				// 手上没有剩余的牌放第一位为赢家
				builderList.add(0, build);
			} else {
				builderList.add(build);
			}
			// 信用分
			if (isCreditTable()) {
				player.setWinLoseCredit(player.getTotalPoint() * creditDifen);
			}
		}

		// 信用分计算
		if (isCreditTable()) {
			// 计算信用负分
			calcNegativeCredit();
			long dyjCredit = 0;
			for (AhmjPlayer player : seatMap.values()) {
				if (player.getWinLoseCredit() > dyjCredit) {
					dyjCredit = player.getWinLoseCredit();
				}
			}
			for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
				AhmjPlayer player = seatMap.get(builder.getSeat());
				calcCommissionCredit(player, dyjCredit);
				builder.setWinLoseCredit(player.getWinLoseCredit());
				builder.setCommissionCredit(player.getCommissionCredit());
			}
		} else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (AhmjPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                AhmjPlayer player = seatMap.get(builder.getSeat());
                builder.setWinLoseCredit(player.getWinGold());
            }
        }
        for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
            list.add(builder.build());
        }

		ClosingMjInfoRes.Builder res = ClosingMjInfoRes.newBuilder();
		res.addAllClosingPlayers(list);
		res.setIsBreak(isBreak ? 1 : 0);
		res.setWanfa(GameUtil.play_type_anhua);
		res.addAllExt(buildAccountsExt(over?1:0));
		res.addCreditConfig(creditMode);                         //0
		res.addCreditConfig(creditJoinLimit);                    //1
		res.addCreditConfig(creditDissLimit);                    //2
		res.addCreditConfig(creditDifen);                        //3ClosingMjInfoRes
		res.addCreditConfig(creditCommission);                   //4
		res.addCreditConfig(creditCommissionMode1);              //5
		res.addCreditConfig(creditCommissionMode2);              //6
		res.addCreditConfig(creditCommissionLimit);              //7
		if (seatBirds != null) {
			res.addAllBirdSeat(DataMapUtil.toList(seatBirds));
		}
		if (prickBirdMajiangIds != null) {
			res.addAllBird(DataMapUtil.toList(prickBirdMajiangIds));

		}
		res.addAllLeftCards(leftMajiangs);
		for (AhmjPlayer player : seatMap.values()) {
			player.writeSocket(res.build());
		}
		broadMsgRoomPlayer(res.build());
		return res;
	}

	/**
	 * 杠上花和杠上炮
	 * 
	 * @return
	 */
	public Ahmj getGangHuMajiang(int seat) {
		int majiangId = 0;
		for (Entry<Integer, Map<Integer, List<Integer>>> entry : gangSeatMap.entrySet()) {
			Map<Integer, List<Integer>> actionMap = entry.getValue();
			if (actionMap.containsKey(seat)) {
				List<Integer> actionList = actionMap.get(seat);
				if (actionList != null && !actionList.isEmpty() && actionList.get(0) == 1) {
					majiangId = entry.getKey();
					break;
				}
			}
		}
		return Ahmj.getMajang(majiangId);

	}

	public List<String> buildAccountsExt(int over) {
		List<String> ext = new ArrayList<>();
		if (isGroupRoom()) {
			ext.add(loadGroupId());
		} else {
			ext.add("0");
		}
		ext.add(id + "");                                  //1
		ext.add(masterId + "");                            //2
		ext.add(TimeUtil.formatTime(TimeUtil.now()));      //3
		ext.add(playType + "");                            //4
		ext.add(isCalcBanker + "");                        //5
		ext.add(lastWinSeat + "");                         //6
		ext.add(isAutoPlay + "");                          //7
		ext.add(diFen + "");                               //8
		ext.add(over+"");                                  //9
		ext.add(firstId+"");                               //10
		return ext;
	}



	@Override
	public void sendAccountsMsg() {
		ClosingMjInfoRes.Builder builder = sendAccountsMsg(true, false, null, null, null, null, true);
		saveLog(true, 0l, builder.build());
	}

	public Class<? extends Player> getPlayerClass() {
		return AhmjPlayer.class;
	}

	@Override
	public int getWanFa() {
		return SharedConstants.game_type_majiang;
	}

	@Override
	public boolean isTest() {
		return AhmjConstants.isTestAh;
	}

	@Override
	public void checkReconnect(Player player) {
		((AhmjPlayer) player).checkAutoPlay(0, true);
		seatMap.get(player.getSeat()).checkSendActionRes();
		checkSendGangRes(player);
		AhmjPlayer p = (AhmjPlayer) player;
		if (actionSeatMap.isEmpty()) {
			// 没有其他可操作的动作事件
			if (p != null) {
				if (p.isAlreadyMoMajiang()) {
					if (!p.getGang().isEmpty()) {
						List<Ahmj> disMajiangs = new ArrayList<>();
						disMajiangs.add(p.getLastMoMajiang());
						disMajiang2(p, disMajiangs, 0);
					}
				}
			}

		}
		if (state == table_state.play) {
			if (p.getHandPais() != null && p.getHandPais().size() > 0) {
				sendTingInfo(p);
			}
		}
	}

	@Override
	public void checkAutoPlay() {
		if (System.currentTimeMillis() - lastAutoPlayTime < 100) {
			return;
		}
		if (getSendDissTime() > 0) {
			for (AhmjPlayer player : seatMap.values()) {
				if (player.getLastCheckTime() > 0) {
					player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
				}
			}
			return;
		}

		if (isAutoPlayOff()) {
			// 托管关闭
			for (int seat : seatMap.keySet()) {
				AhmjPlayer player = seatMap.get(seat);
				player.setAutoPlay(false, false);
			}
			return;
		}

		for (AhmjPlayer player : seatMap.values()) {

			if (!player.getGang().isEmpty() && player.isAlreadyMoMajiang() && getMoMajiangSeat() == player.getSeat()) {
				// 能胡牌就不自动打出去
				List<Integer> actionList = actionSeatMap.get(player.getSeat());
				if (actionList != null && (actionList.get(0)==1||actionList.get(3)==1)) {
					continue;
				}
				if (nowDisCardSeat != player.getSeat()) {
					continue;
				}
				List<Ahmj> ahmjs = new ArrayList<>();
				ahmjs.add(player.getHandMajiang().get(player.getHandMajiang().size() - 1));
				disMajiang(player, ahmjs, MjDisAction.action_chupai);
				// 执行完一个就退出，防止出牌操作后有报听玩家摸牌
				setLastAutoPlayTime(System.currentTimeMillis());
				return;
			}
		}

		if (isAutoPlay < 1) {
			return;
		}
		if (state == table_state.play) {
			autoPlay();
		} else {
			if (getPlayedBureau() == 0) {
				return;
			}
			readyTime ++;
			//开了托管的房间，xx秒后自动开始下一局
			for (AhmjPlayer player : seatMap.values()) {
				if (player.getState() != player_state.entry && player.getState() != player_state.over) {
					continue;
				} else {
					if (readyTime >= 5 && player.isAutoPlay()) {
						// 玩家进入托管后，5秒自动准备
						autoReady(player);
					} else if (readyTime > 30) {
						autoReady(player);
					}
				}
			}
		}
	}

	public boolean IsCalcBankerPoint() {
		// 安化麻将不算庄闲
		return false;
	}

	public void logAction(AhmjPlayer player, int action, List<Ahmj> mjs) {
		StringBuilder sb = new StringBuilder();
		sb.append(versionCode+"|Ahmj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		String actStr = "unKnown-" + action;
		if (action == MjDisAction.action_peng) {
			actStr = "peng";
		} else if (action == MjDisAction.action_minggang) {
			actStr = "baoTing";
		} else if (action == MjDisAction.action_chupai) {
			actStr = "chuPai";
		} else if (action == MjDisAction.action_pass) {
			actStr = "guo";
		} else if (action == MjDisAction.action_angang) {
			actStr = "anGang";
		} else if (action == MjDisAction.action_chi) {
			actStr = "chi";
		}
		sb.append("|").append(player.isAutoPlay() ? 1 : 0);
		sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
		sb.append("|").append(actStr);
		if(mjs!=null)
			sb.append("|").append(mjs);
		sb.append("|").append(actionListToString(actionSeatMap.get(player.getSeat())));
		LogUtil.msg(sb.toString());
	}

	public void logMo(AhmjPlayer player, Ahmj mj,List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append(versionCode+"|Ahmj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append(player.isAutoPlay() ? 1 : 0);
		sb.append("|").append("mo[").append(mj.getPai()).append("]");
		sb.append("|").append(actionListToString(actList));

		LogUtil.msg(sb.toString());
	}

	public static String actionListToString(List<Integer> actionList) {
		if (actionList == null || actionList.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < actionList.size(); i++) {
			if (actionList.get(i) == 1) {
				switch (i) {
					case 0:
						sb.append("hu").append(",");
						break;
					case 1:
						sb.append("peng").append(",");
						break;
					case 2:
						sb.append("mingGang").append(",");
						break;
					case 3:
						sb.append("angGang").append(",");
						break;
					case 4:
						sb.append("chi").append(",");
						break;
				}
			}

		}
		sb.append("]");
		return sb.toString();
	}


	public void setIsCalcBanker(int isCalcBanker) {
		this.isCalcBanker = isCalcBanker;
		changeExtend();
	}

	public int getFirstId() {
		return firstId;
	}

	public void setWangMajiang(int  firstId) {
		this.firstId = firstId;
		if (isFourWang()) {
			if (this.firstId == 0) {
				setWangValList(null);
			} else {
				setWangValList(AhMajiangTool.findFourWangValList(Ahmj.getMajang(firstId)));

			}

		} else {
			if (this.firstId == 0) {
				setWangValList(null);
			} else {
				setWangValList(AhMajiangTool.findWangValList(Ahmj.getMajang(firstId)));
			}
		}
		changeExtend();
	}

	public List<Integer> getWangValList() {
		return wangValList;
	}

	public void setWangValList(List<Integer> wangValList) {
		this.wangValList = wangValList;
	}

	/**
	 * 检查麻将中是否有王
	 * 
	 * @param majiangs
	 * @return
	 */
	public boolean checkWang(List<Ahmj> majiangs) {
		return AhmjTool.checkWang(majiangs, wangValList);

	}

	public int getGangHuMajiangId() {
		return gangHuMajiangId;
	}

	public void setGangHuMajiangId(int gangHuMajiang) {
		this.gangHuMajiangId = gangHuMajiang;
		changeExtend();
	}

	public void sendTingInfo(AhmjPlayer player) {
		if (player.isAlreadyMoMajiang()) {
			if (actionSeatMap.containsKey(player.getSeat())) {
				return;
			}
			DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
			List<Ahmj> cards = new ArrayList<>(player.getHandMajiang());
			Map<Integer, List<Integer>> daTing = TingTool.getDaTing(Ahmj.getIdsByMjs(cards),wangValList,player.getAllMjWithoutHand());
			for(Map.Entry<Integer, List<Integer>> entry : daTing.entrySet()){
				DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
				ting.addAllTingMajiangIds(entry.getValue());
				ting.setMajiangId(entry.getKey());
				tingInfo.addInfo(ting);
			}
			if(daTing.size()>0)
				player.writeSocket(tingInfo.build());
			
		} else {
			List<Ahmj> cards = new ArrayList<>(player.getHandMajiang());
			TingPaiRes.Builder ting = TingPaiRes.newBuilder();
			List<Integer> tingP = TingTool.getTing(Ahmj.getIdsByMjs(cards), wangValList,player.getAllMjWithoutHand());
			ting.addAllMajiangIds(tingP);
			if(tingP.size()>0)
				player.writeSocket(ting.build());
		}
	}









	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_anhua);

	public static void loadWanfaTables(Class<? extends BaseTable> cls) {
		for (Integer integer : wanfaList) {
			TableManager.wanfaTableTypesPut(integer, cls);
		}
	}
}
