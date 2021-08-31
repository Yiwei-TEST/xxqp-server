package com.sy599.game.qipai.dehmj.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
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
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiInfo;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.GangMoMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.GangPlayMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.MoMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.TingPaiRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjInfoRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.dehmj.constant.DehMjAction;
import com.sy599.game.qipai.dehmj.constant.DehMjConstants;
import com.sy599.game.qipai.dehmj.rule.DehMj;
import com.sy599.game.qipai.dehmj.rule.DehMjHelper;
import com.sy599.game.qipai.dehmj.rule.DehMjRobotAI;
import com.sy599.game.qipai.dehmj.tool.DehMjQipaiTool;
import com.sy599.game.qipai.dehmj.tool.DehMjResTool;
import com.sy599.game.qipai.dehmj.tool.DehMjTool;
import com.sy599.game.qipai.dehmj.tool.hulib.util.HuUtil;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.PayConfigUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

/**
 * @author l 保山麻将牌桌信息
 */
public class DehMjTable extends BaseTable {
	/**
	 * 当前桌上打出的牌
	 */
	private List<DehMj> nowDisCardIds = new ArrayList<>();
	/**
	 * 所有玩家当前可作的操作
	 */
	private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
	/**
	 * 0胡 1碰 2明刚 3暗杠(暗杠后来不需要了 暗杠也用3标记)4吃 5补张(6缺一色 7板板胡 8大四喜 9六六顺 10节节高 11三同
	 * 12一枝花 13中途四喜 14中途六六顺)
	 */
	private Map<Integer, Map<Integer, List<Integer>>> gangSeatMap = new ConcurrentHashMap<>();
	/**
	 * 房间最大玩家人数上限
	 */
	private int maxPlayerCount = 4;
	/**
	 * 当前剩下的牌（庄上的牌）
	 */
	private List<DehMj> leftMajiangs = new ArrayList<>();
	/**
	 * 当前房间所有玩家信息map
	 */
	private Map<Long, DehMjPlayer> playerMap = new ConcurrentHashMap<Long, DehMjPlayer>();
	/**
	 * 座位对应的玩家信息MAP
	 */
	private Map<Integer, DehMjPlayer> seatMap = new ConcurrentHashMap<Integer, DehMjPlayer>();
	/**
	 * 胡确认信息
	 */
	private Map<Integer, Integer> huConfirmMap = new HashMap<>();
	/**
	 * 玩家位置对应临时操作 当同时存在多个可做的操作时 1当优先级最高的玩家最先操作 则清理所有操作提示 并执行该操作
	 * 2当操作优先级低的玩家先选择操作，则玩家先将所做操作存入临时操作中 当玩家优先级最高的玩家操作后 在判断临时操作是否可执行并执行
	 */
	private Map<Integer, DehMjTempAction> tempActionMap = new ConcurrentHashMap<>();
	/**
	 * 抓鸟
	 */
	private int birdNum;
	/**
	 * 计算庄闲
	 */
	private int isCalcBanker;
	/**
	 * 计算鸟的算法 1乘法 2加法
	 */
	private int calcBird;
	/**
	 * 是否买点 1.买死点上限1 2.买死点上限2 3买活点上限1 4.买活点上限2 5.买活点固定1
	 */
	private int buyPoint;

	private int chajiao;//

	private int isAutoPlay;// 是否开启自动托管
	private int readyTime = 0;

	/**
	 * 摸麻将的seat
	 */
	private int moMajiangSeat;
	/**
	 * 摸杠的麻将
	 */
	private DehMj moGang;
	/**
	 * 杠出来的麻将
	 */
	private DehMj gangMajiang;
	/**
	 * 摸杠胡
	 */
	private List<Integer> moGangHuList = new ArrayList<>();
	/**
	 * 杠后出的两张牌
	 */
	private List<DehMj> gangDisMajiangs = new ArrayList<>();
	/**
	 * 摸海底捞的座位
	 */
	private int moLastMajiangSeat;
	/**
	 * 询问海底捞的座位
	 */
	private int askLastMajaingSeat;
	/**
	 * 第一次出现海底的座位
	 */
	private int fristLastMajiangSeat;

	/**
	 * 摸海底的座位号
	 */
	private List<Integer> moLastSeats = new ArrayList<>();
	/**
	 * 最后一张麻将
	 */
	private DehMj lastMajiang;
	/**
	 *
	 */
	private int disEventAction;

	/*** GPS预警 */
	private int gpsWarn = 0;
	/*** 缺一色 */
	private int youfeng = 0;
	/*** 板板胡 */
	private int yitiaolong = 0;
	/*** 一枝花 */
	private int siguiyi = 0;
	/*** 报听 */
	private int baoting = 0;

	/*** 需要展示牌的玩家座位号 */
	private List<Integer> showMjSeat = new ArrayList<>();

	private int tableStatus;// 特殊状态 1飘分

	/** 托管1：单局，2：全局 */
	private int autoPlayGlob;
	
	private int autoTableCount;

	/*** 杠打色子 **/
	private int gangDice = -1;

	/*** 摸屁股的座标号 */
	private List<Integer> moTailPai = new ArrayList<>();

	/** 杠后摸的两张牌中被要走的 **/
	private DehMj gangActedMj = null;

	/** 是否是开局 **/
	private boolean isBegin = false;

	private int dealDice;

	private int gangSeat;

	
	private int daiGen;
	
	
	
    //是否加倍：0否，1是
    private int jiaBei;
    //加倍分数：低于xx分进行加倍
    private int jiaBeiFen;
    //加倍倍数：翻几倍
    private int jiaBeiShu;
	
	
    //belowAdd分
    private int belowAdd=0;
    //低于below分+
    private int below=0;
    
    
    private DehMj daiGenGangMj;
	
	@Override
	protected boolean quitPlayer1(Player player) {
		return false;
	}

	@Override
	protected boolean joinPlayer1(Player player) {

		DehMjPlayer pl = (DehMjPlayer) player;
		// 房间勾选买点必须买
		if (getBuyPoint() == 0) {
			pl.setPiaoPoint(0);
		}

		return false;
	}

	@Override
	public int isCanPlay() {
		return 0;
	}

	@Override
	public void calcOver() {
		List<Integer> winSeatList = new ArrayList<>(huConfirmMap.keySet());
		boolean selfMo = false;
		int[] birdMjIds = null;
		int[] seatBirds = null;
		// Map<Integer, Integer> seatBirdMap = new HashMap<>();
		boolean flow = false;

		if(getDaiGen()!=1){
			for (DehMjPlayer seat : seatMap.values()) {
				seat.setGangPoint(seat.getLostPoint());
				seat.setLostPoint(0);
				if (winSeatList.size() == 0) {
					seat.setGangPoint(0);
				}
			}
            logPointError("daiGen");
		}
		
		int fangpaoSeat = 0;
		int resGen = 0;
		if (winSeatList.size() == 0) {
			// 流局
			flow = true;
			checkChaJiao();

            logPointError("chaJiao");

		} else {
			// 先判断是自摸还是放炮
			DehMjPlayer winPlayer = null;
			if (winSeatList.size() == 1) {
				winPlayer = seatMap.get(winSeatList.get(0));
				if ((winPlayer.isAlreadyMoMajiang() || winPlayer.isGangshangHua())
						&& winSeatList.get(0) == moMajiangSeat) {
					selfMo = true;
				}
			}

			// 如果通炮按放炮的座位开始算
			if (!winSeatList.isEmpty()) {
				// 先砸鸟
				birdMjIds = zhuaNiao();
			}

			// 庄家
			if (selfMo || (winPlayer != null && winPlayer.getDahu().contains(14))) {
				//杠分
				int genNum = 0;
				if(getDaiGen()==1){
//					for (DehMjPlayer seat : seatMap.values()) {
//						if(seat.getSeat()!=winPlayer.getSeat()){
//							seat.setGangPoint(seat.getGangPointArr(winPlayer.getSeat()-1));
////							seat.setLostPoint(0);
//						}
//					}
					genNum = winPlayer.getGenNum();
					
				}
				int daHuCount = winPlayer.getDahuFan();
				int winPoint = 0;
				// 底分
				if (daHuCount > 0) {
					winPoint = daHuCount;
				}
				resGen = genNum;
				winPoint +=genNum;
				int totalWinPoint = 0;
				for (int loserSeat : seatMap.keySet()) {
					// 除了赢家的其他人
					if (winSeatList.contains(loserSeat)) {
						continue;
					}
					DehMjPlayer loser = seatMap.get(loserSeat);

					int losePoint = winPoint;
//					if (buyPoint == 1 || buyPoint == 2) {
//					} 
					losePoint += (1+loser.getPiaoPoint() + winPlayer.getPiaoPoint());
					
					
//					else if (buyPoint > 2) {
//						losePoint = (1 + loser.getPiaoPoint() + winPlayer.getPiaoPoint()) * (losePoint);
//					} else {
//						// losePoint -=gangfen;
//					}

					totalWinPoint += losePoint;
					loser.setLostPoint(-losePoint);
				}

				winPlayer.changeAction(7, 1);
				winPlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index8, 1);

				winPlayer.setLostPoint(totalWinPoint);

				// checkGangFen();
				winPlayer.setWinCount(winPlayer.getWinCount() + 1);

                logPointError("selfMo");

			} else {
				DehMjPlayer losePlayer = seatMap.get(disCardSeat);
				fangpaoSeat = losePlayer.getSeat();

				losePlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index10, winSeatList.size());
				int totalLosePoint = 0;
				for (int winSeat : winSeatList) {
					// 胡牌
					winPlayer = seatMap.get(winSeat);
					int genNum = 0;
					if(getDaiGen()==1){
						genNum = winPlayer.getGenNum();
						
					}
					resGen = genNum;
					int daHuCount = winPlayer.getDahuFan();

					// 底分
					int winPoint = 0;
					if (daHuCount > 0) {
						winPoint = daHuCount;
					}
					winPoint +=genNum;
					// int gangFen = player.ge

					//
					if (buyPoint == 1 || buyPoint == 2) {
					} 
					winPoint += (1+losePlayer.getPiaoPoint() + winPlayer.getPiaoPoint());

					totalLosePoint += winPoint;
					winPlayer.changeAction(6, 1);
					winPlayer.setWinCount(winPlayer.getWinCount() + 1);
					losePlayer.changeAction(0, 1);
					winPlayer.changeActionTotal(6, 1);
					losePlayer.changeActionTotal(0, 1);
					winPlayer.setLostPoint(winPoint);
				}

				losePlayer.setLostPoint(-totalLosePoint);

                logPointError("fangPao");
			}

		}

		if (!flow) {
			for (DehMjPlayer seat : seatMap.values()) {
				seat.changePoint(seat.getLostPoint() + seat.getGangPoint());
			}
		}

		boolean over = playBureau == totalBureau;
		if (autoPlayGlob > 0) {
			// //是否解散
			boolean diss = false;
			if (autoPlayGlob == 1) {
				for (DehMjPlayer seat : seatMap.values()) {
					if (seat.isAutoPlay()) {
						diss = true;
						break;
					}

				}
			}else if (autoPlayGlob == 3) {
				diss = checkAuto3();
			}
			if (diss) {
				autoPlayDiss = true;
				over = true;
			}
		}

		// 不管流局都加分
        ClosingMjInfoRes.Builder res = sendAccountsMsg(over, selfMo, winSeatList, birdMjIds, resGen, playBureau == totalBureau, 1, fangpaoSeat);
		// 没有流局
		if (!flow) {
			if (winSeatList.size() > 1) {
				// 一炮多响设置放炮的人为庄家
				setLastWinSeat(disCardSeat);
			} else {
				setLastWinSeat(winSeatList.get(0));
			}
		} else {
			if (moLastMajiangSeat != 0) {
				// 流局有人要了海底，要的人当庄
				setLastWinSeat(moLastMajiangSeat);
			} else if (fristLastMajiangSeat != 0) {
				// 流局了没有人要海底，第一个可以选择的人当庄
				setLastWinSeat(fristLastMajiangSeat);
			}
		}

		saveLog(over, 0l, res.build());

		calcAfter();
		if (playBureau >= totalBureau || over) {
			calcOver1();
			calcOver2();
			calcOver3();
			diss();
		} else {
			initNext();
			calcOver1();
		}

		for (Player player : seatMap.values()) {
			player.saveBaseInfo();
		}
	}
	
	private boolean checkAuto3() {
		boolean diss = false;
		// if(autoPlayGlob==3) {
		boolean diss2 = false;
		for (DehMjPlayer seat : seatMap.values()) {
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
	

	private void checkChaJiao() {
		if (chajiao < 1) {
			return;
		}
		List<Integer> copy = new ArrayList<>(DehMjConstants.baoshan_mjList);
		if (youfeng == 1) {
			copy.addAll(DehMjConstants.feng_mjList);
		}
		List<DehMj> allMjs = new ArrayList<>();
		for (int id : copy) {
			allMjs.add(DehMj.getMajang(id));
		}

		List<DehMj> tingList = new ArrayList<>();
		HashMap<Integer, DehMjiangHu> tingHuMap = new HashMap<>();
		for (int loserSeat : seatMap.keySet()) {
			DehMjPlayer player = seatMap.get(loserSeat);
			DehMjiangHu bigHu = null;
			for (DehMj mj : allMjs) {
				DehMjiangHu temp = player.checkHu(mj, false);
				DehMjTool.checkDahuMax(temp, this, player, mj, false);
				if (temp.isHu()) {
					if (bigHu == null) {
						bigHu = temp;
					}

					if (bigHu.getDahuFan() > 0) {
						if (bigHu.getDahuFan() < temp.getDahuFan()) {
							bigHu = temp;
						}
						bigHu.initDahuList();
						player.setDahu(bigHu.getDahuList(), bigHu.getDahuFan());
					}

					tingHuMap.put(loserSeat, bigHu);
				}
			}
		}

		if (tingHuMap.size() == seatMap.size()) {
			return;
		}
		Set<Integer> keys = tingHuMap.keySet();
		for (Entry<Integer, DehMjiangHu> entry : tingHuMap.entrySet()) {
			int seat = entry.getKey();
			// 胡牌
			DehMjPlayer winPlayer = seatMap.get(seat);

			
			int genNum = 0;
			if(getDaiGen()==1){
				genNum = winPlayer.getGenNum();
			}
			// 底分
			int winPoint = 0;
			if (chajiao == 2) {
				int daHuCount = winPlayer.getDahuFan();
				if (daHuCount > 0) {
					winPoint = daHuCount;
				}
			}else{
				winPoint = 1;
			}
			
			winPoint +=genNum;

			int totalWp = 0;
			for (DehMjPlayer seat2 : seatMap.values()) {
				if (keys.contains(seat2.getSeat())) {
					continue;
				}
				int losePoint = winPoint;
				if (chajiao == 1) {
					totalWp += winPoint;
				} else {
					losePoint += (1+seat2.getPiaoPoint() + winPlayer.getPiaoPoint());
					totalWp += losePoint;
				}
				seat2.changePoint(-losePoint);
			}

			winPlayer.changePoint(totalWp);
		}

	}

	public void saveLog(boolean over, long winId, Object resObject) {
		ClosingMjInfoRes res = (ClosingMjInfoRes) resObject;
		LogUtil.d_msg("tableId:" + id + " play:" + playBureau + " over:" + res);
		String logRes = JacksonUtil.writeValueAsString(LogUtil.buildMJClosingInfoResLog(res));
		String logOtherRes = JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResOtherLog(res));
		Date now = TimeUtil.now();
		UserPlaylog userLog = new UserPlaylog();
		userLog.setLogId(playType);
		userLog.setUserId(creatorId);
		userLog.setTableId(id);
		userLog.setRes(extendLogDeal(logRes));
		userLog.setTime(now);
		userLog.setTotalCount(totalBureau);
		userLog.setCount(playBureau);
		userLog.setStartseat(lastWinSeat);
		userLog.setOutCards(playLog);
		userLog.setExtend(logOtherRes);
		userLog.setMaxPlayerCount(maxPlayerCount);
		userLog.setType(creditMode == 1 ? 2 : 1);
		userLog.setGeneralExt(buildGeneralExtForPlaylog().toString());
		long logId = TableLogDao.getInstance().save(userLog);
		saveTableRecord(logId, over, playBureau);
		for (DehMjPlayer player : playerMap.values()) {
			player.addRecord(logId, playBureau);
		}
		UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);

		// LogUtil.d_msg("tableId:" + id + " play:" + playBureau + " over:" +
		// res);            
		// String logRes =
		// JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResLog(res));
		// String logOtherRes =
		// JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResOtherLog(res));
		// Date now = TimeUtil.now();
		// UserPlaylog userLog = new UserPlaylog();
		// userLog.setLogId(playType);
		// userLog.setTableId(id);
		// userLog.setRes(logRes);
		// userLog.setTime(now);
		// userLog.setTotalCount(totalBureau);
		// userLog.setCount(playBureau);
		// userLog.setStartseat(lastWinSeat);
		// userLog.setOutCards(playLog);
		// userLog.setExtend(logOtherRes);
		// long logId = TableLogDao.getInstance().save(userLog);
		// for (DehMjPlayer player : playerMap.values()) {
		// player.addRecord(logId, playBureau);
		// }
		// UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
	}

	/**
	 * 抓鸟
	 *
	 * @return
	 */
	private int[] zhuaNiao() {
		// 先砸鸟
		int realBirdNum = leftMajiangs.size() > birdNum ? birdNum : leftMajiangs.size();
		int[] bird = new int[realBirdNum];
		for (int i = 0; i < realBirdNum; i++) {
			DehMj prickbirdMajiang = getLeftMajiang();
			if (prickbirdMajiang != null) {
				bird[i] = prickbirdMajiang.getId();
			} else {
				break;
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
	private int[] birdToSeat(int[] prickBirdMajiangIds, int winSeat) {
		int[] seatArr = new int[prickBirdMajiangIds.length];
		for (int i = 0; i < prickBirdMajiangIds.length; i++) {
			DehMj majiang = DehMj.getMajang(prickBirdMajiangIds[i]);
			int prickbirdPai = majiang.getPai();
			prickbirdPai = (prickbirdPai - 1) % 4;// 从自己开始算 所以减1
			int prickbirdseat = prickbirdPai + winSeat > 4 ? prickbirdPai + winSeat - 4 : prickbirdPai + winSeat;
			seatArr[i] = prickbirdseat;
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
				tempMap.put("nowDisCardIds", StringUtil.implode(DehMjHelper.toMajiangIds(nowDisCardIds), ","));
			}
			if (tempMap.containsKey("leftPais")) {
				tempMap.put("leftPais", StringUtil.implode(DehMjHelper.toMajiangIds(leftMajiangs), ","));
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

	// public String buildExtend() {
	// JsonWrapper wrapper = new JsonWrapper("");
	// for (DehMjPlayer player : seatMap.values()) {
	// wrapper.putString(player.getSeat(), player.toExtendStr());
	// }
	// wrapper.putString(5, DataMapUtil.explode(huConfirmMap));
	// wrapper.putInt(6, birdNum);
	// wrapper.putInt(7, moMajiangSeat);
	// if (moGang != null) {
	// wrapper.putInt(8, moGang.getId());
	//
	// } else {
	// wrapper.putInt(8, 0);
	//
	// }
	// wrapper.putString(9, StringUtil.implode(moGangHuList, ","));
	// wrapper.putString(10, MajiangHelper.implodeMajiang(gangDisMajiangs,
	// ","));
	// if (gangMajiang != null) {
	// wrapper.putInt(11, gangMajiang.getId());
	//
	// } else {
	// wrapper.putInt(11, 0);
	//
	// }
	// wrapper.putInt(12, askLastMajaingSeat);
	// wrapper.putInt(13, moLastMajiangSeat);
	// if (lastMajiang != null) {
	// wrapper.putInt(14, lastMajiang.getId());
	// } else {
	// wrapper.putInt(14, 0);
	// }
	// wrapper.putInt(15, fristLastMajiangSeat);
	// wrapper.putInt(16, disEventAction);
	// wrapper.putInt(17, isCalcBanker);
	// wrapper.putInt(18, calcBird);
	// return wrapper.toString();
	// }

	@Override
	public int getPlayerCount() {
		return playerMap.size();
	}

	@Override
	protected void sendDealMsg() {
		sendDealMsg(0);
	}

	@Override
	protected void sendDealMsg(long userId) {
		Random r = new Random();
		int dealDice = (r.nextInt(6) + 1) * 10 + (r.nextInt(6) + 1);
		addPlayLog(disCardRound + "_" + lastWinSeat + "_" + DehMjDisAction.action_dice + "_" + dealDice);
		setDealDice(dealDice);
		logFaPaiTable();

		
		for (DehMjPlayer tablePlayer : seatMap.values()) {
			DealInfoRes.Builder res = DealInfoRes.newBuilder();
			List<Integer> actionList = tablePlayer.checkMo(null, true);
			if (!actionList.isEmpty()) {
				addActionSeat(tablePlayer.getSeat(), actionList);
				res.addAllSelfAct(actionList);
			}
			res.addAllHandCardIds(tablePlayer.getHandPais());
			res.setNextSeat(getNextDisCardSeat());
			res.setGameType(getWanFa());
			res.setRemain(leftMajiangs.size());
			res.setBanker(lastWinSeat);
			res.setDealDice(dealDice);
			if(getDaiGen()==1){
				res.addAllHorses(getLastTwoMj());
			}
			logFaPaiPlayer(tablePlayer, actionList);
			tablePlayer.writeSocket(res.build());
			sendTingInfo(tablePlayer);

			checkBaoTingMsg(tablePlayer);

		}
		
		if(getDaiGen()==1){
			List<DehMj> mjs = getGenMjs();
			addPlayLog(disCardRound + "_" + 0 + "_" +  DehMjDisAction.action_Gen_SHOW + "_" + DehMjHelper.toMajiangStrs(mjs));
		}

		isBegin = true;
		if (!hasBaoTing()) {
			// 没有操作的话通知庄家出牌
			DehMjPlayer bankPlayer = seatMap.get(lastWinSeat);
			ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// 推送庄家出牌
			bankPlayer.writeSocket(com.build());
			isBegin = false;
		}
	}

	private boolean checkBaoTingMsg(DehMjPlayer tablePlayer) {
		if (checkBaoting(tablePlayer)) {
			if(tablePlayer.getBaoTingS()==0){
				ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_code_ts_pao_ting,
						tablePlayer.getSeat(), tablePlayer.getBaoTingS());
				tablePlayer.writeSocket(com.build());
			}
			return true;
		}
		return false;
	}

	/**
	 * 摸牌
	 *
	 * @param player
	 */
	public void moMajiang(DehMjPlayer player, boolean isBuzhang) {
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
		if (disCardRound != 0 && player.isAlreadyMoMajiang()) {
			return;
		}
		if (leftMajiangs.size() == 0) {
			calcOver();
			return;
		}

		// 如果只剩下一张牌 问要不要
//		if (getLeftMajiangCount() == 1 && !isBuzhang) {
//			// calcMoLastSeats(player.getSeat());
//			// sendAskLastMajiangRes(0);
//			// if(moLastSeats == null || moLastSeats.size() == 0){
//			// calcOver();
//			// }
//			// return;
//		}
		if (isBuzhang) {
			addMoTailPai(-1);
		}
		// 摸牌
		DehMj majiang = null;
		if (disCardRound != 0) {
			// 玩家手上的牌是双数，已经摸过牌了
			if (player.isAlreadyMoMajiang()) {
				return;
			}
			if (GameServerConfig.isDebug() && !player.isRobot()) {
				if (zpMap.containsKey(player.getUserId()) && zpMap.get(player.getUserId()) > 0) {
					majiang = DehMjQipaiTool.findMajiangByVal(leftMajiangs, zpMap.get(player.getUserId()));
					if (majiang != null) {
						zpMap.remove(player.getUserId());
						leftMajiangs.remove(majiang);
					}
				}
			}
			// 不是庄家第一次出牌
			// 不是第一次出牌 ，摸牌
			// majiang=majiangt
			// majiang = MajiangHelper.findMajiangByVal(leftMajiangs, 25);
			// leftMajiangs.remove(majiang);
			if (majiang == null) {
				majiang = getLeftMajiang();
			}
		}
		if (majiang != null) {
			if(getDaiGen()==1&&isBuzhang&&getLeftMajiangCount()>0){
				//&& majiang.getVal()!=getDaiGenGangMj().getVal()
				if(getDaiGenGangMj()!=null){
					this.leftMajiangs.remove(getDaiGenGangMj());
					leftMajiangs.add(leftMajiangs.size()-1>0?leftMajiangs.size()-1:0, majiang);
					logdaiGenMoMj(player, getDaiGenGangMj(), majiang, getLastTwoMj());
					majiang = getDaiGenGangMj();//&&getLeftMajiangCount()>1
				}
				logdaiGenMsg(player, "MoDaiGen|"+majiang.getId()+"|"+getLastTwoMj());
				ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_deh_TMJ, getLastTwoMj());
				for (Player tableplayer : getSeatMap().values()) {// 推送客户端玩家抛分情况
					tableplayer.writeSocket(com.build());
				}
				
				if(getDaiGen()==1){
					List<DehMj> mjs = getGenMjs();
					addPlayLog(disCardRound + "_" + 0 + "_" + DehMjDisAction.action_Gen_SHOW + "_" + DehMjHelper.toMajiangStrs(mjs));
				}
			}
			
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + DehMjDisAction.action_moMjiang + "_"
					+ majiang.getId());
			player.moMajiang(majiang);
		}

		processHideMj(player);

		// 检查摸牌
		clearActionSeatMap();
		setGangSeat(0);
		if (disCardRound == 0) {
			return;
		}
		setMoMajiangSeat(player.getSeat());
		List<Integer> arr = player.checkMo(majiang, false);
		if (!arr.isEmpty()) {
			coverAddActionSeat(player.getSeat(), arr);
		}
		MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
		res.setUserId(player.getUserId() + "");
		res.setRemain(getLeftMajiangCount());
		res.setSeat(player.getSeat());
		if(isBuzhang){
			res.setNextDisCardIndex(1);
		}
		// boolean playCommand = !player.getGang().isEmpty() && arr.isEmpty();
		logMoMj(player, majiang, arr);
		for (DehMjPlayer seat : seatMap.values()) {
			if (seat.getUserId() == player.getUserId()) {
				MoMajiangRes.Builder copy = res.clone();
				copy.addAllSelfAct(arr);
				if (majiang != null) {
					copy.setMajiangId(majiang.getId());
				}
				seat.writeSocket(copy.build());
			} else {
				seat.writeSocket(res.build());
			}
		}
		if(arr.isEmpty()){
			if (player.getActionNum(9) ==0) {
				checkBaoTingMsg(player);
			}
		}
		sendTingInfo(player);
	}

	// TODO:查叫
	public void calcMoLastSeats(int firstSeat) {
		for (int i = 0; i < getMaxPlayerCount(); i++) {
			DehMjPlayer player = seatMap.get(firstSeat);
			// if(player.isTingPai(-1)){
			// setFristLastMajiangSeat(player.getSeat());
			// addMoLastSeat(player.getSeat());
			// }
			firstSeat = calcNextSeat(firstSeat);
		}
		if (moLastSeats != null && moLastSeats.size() > 0) {
			setFristLastMajiangSeat(moLastSeats.get(0));
			setAskLastMajaingSeat(moLastSeats.get(0));
		}
	}

	/**
	 * 推送摸海底消息
	 * 
	 * @param seat
	 *            0表示推送第一个，>0表示当前推送的是自己，就推送
	 * @return 返回当前推送的座位
	 */
	public void sendAskLastMajiangRes(int seat) {
		if (moLastSeats == null || moLastSeats.size() == 0) {
			return;
		}
		int sendSeat = moLastSeats.get(0);
		if (seat > 0 && sendSeat != seat) {
			return;
		}
		setAskLastMajaingSeat(sendSeat);
		DehMjPlayer player = seatMap.get(sendSeat);
		sendMoLast(player, 1);
	}

	private void buildPlayRes(PlayMajiangRes.Builder builder, Player player, int action, List<DehMj> majiangs) {
		DehMjResTool.buildPlayRes(builder, player, action, majiangs);
		buildPlayRes1(builder);
	}

	private void buildPlayRes1(PlayMajiangRes.Builder builder) {
		// builder
	}

	/**
	 * 如果是起手判断是否还有人可胡小胡，检查庄家发牌后有没有操作，没有的话通知庄家出牌
	 */
	public void checkBegin(DehMjPlayer player) {
		boolean isBegin = isBegin();
		if (isBegin && !hasBaoTing()) {
			DehMjPlayer bankPlayer = seatMap.get(lastWinSeat);
			List<Integer> actList = bankPlayer.checkMo(null, isBegin);
			if (!actList.isEmpty()) {
				PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
				buildPlayRes(builder, player, DehMjDisAction.action_pass, new ArrayList<>());
				if (!actList.isEmpty()) {
					addActionSeat(bankPlayer.getSeat(), actList);
					builder.addAllSelfAct(actList);
				}
				bankPlayer.writeSocket(builder.build());
			}
			ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// 推送庄家出牌
			bankPlayer.writeSocket(com.build());
		}
	}

	/**
	 * 玩家表示胡
	 *
	 * @param player
	 * @param majiangs
	 */
	private void hu(DehMjPlayer player, List<DehMj> majiangs, int action) {
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return;
		}
		if (huConfirmMap.containsKey(player.getSeat())) {
			return;
		}
		boolean zimo = player.isAlreadyMoMajiang();
		DehMj disMajiang = null;
		DehMjiangHu huBean = null;
		List<DehMj> huMjs = new ArrayList<>();
		int fromSeat = 0;
		boolean isGangShangHu = false;
		if (!zimo) {
			if (moGangHuList.contains(player.getSeat())) {// 强杠胡
				disMajiang = moGang;
				fromSeat = moMajiangSeat;
				huMjs.add(moGang);
			} else if (isHasGangAction(player.getSeat())) {// 杠上炮 杠上花
				fromSeat = moMajiangSeat;
				Map<Integer, DehMjiangHu> huMap = new HashMap<>();
				List<Integer> daHuMjIds = new ArrayList<>();
				List<Integer> huMjIds = new ArrayList<>();
				for (int majiangId : gangSeatMap.keySet()) {
					DehMjiangHu temp = player.checkHu(DehMj.getMajang(majiangId), disCardRound == 0);
					DehMjTool.checkDahuMax(temp, this, player, DehMj.getMajang(majiangId), zimo);
					if (!temp.isHu()) {
						continue;
					}
					temp.initDahuList();
					huMap.put(majiangId, temp);
					huMjIds.add(majiangId);
					if (temp.isDahu()) {
						daHuMjIds.add(majiangId);
					}
				}
				if (daHuMjIds.size() > 0) {
					// 有大胡
					for (int mjId : huMjIds) {
						DehMjiangHu temp = huMap.get(mjId);
						if (moMajiangSeat == player.getSeat()) {
							temp.setGangShangHua(true);
							isGangShangHu = true;
						} else {
							temp.setGangShangPao(true);
						}
						temp.initDahuList();
						if (huBean == null) {
							huBean = temp;
						} else {
							huBean.addToDahu(temp.getDahuList());
							huBean.getShowMajiangs().add(DehMj.getMajang(mjId));
						}
						player.addHuMjId(mjId);
						huMjs.add(DehMj.getMajang(mjId));
					}
				} else if (huMjIds.size() > 0) {
					// 没有大胡
					for (int mjId : huMjIds) {
						DehMjiangHu temp = huMap.get(mjId);
						if (moMajiangSeat == player.getSeat()) {
							temp.setGangShangHua(true);
							isGangShangHu = true;
						} else {
							temp.setGangShangPao(true);
						}
						temp.initDahuList();
						if (huBean == null) {
							huBean = temp;
						} else {
							huBean.addToDahu(temp.getDahuList());
							huBean.getShowMajiangs().add(DehMj.getMajang(mjId));
						}
						player.addHuMjId(mjId);
						huMjs.add(DehMj.getMajang(mjId));
					}
				} else {
					huBean = new DehMjiangHu();
				}

				if (huBean.isHu()) {
					if (disCardSeat == player.getSeat()) {
						zimo = true;
					}
				}

			} else if (lastMajiang != null) {
				huBean = player.checkHu(lastMajiang, disCardRound == 0);
				DehMjTool.checkDahuMax(huBean, this, player, lastMajiang, zimo);
				if (huBean.isHu()) {
					// if (moLastMajiangSeat == player.getSeat()) {
					// // huBean.setHaidilaoyue(true);
					// } else {
					// huBean.setHaidipao(true);
					// }
					huBean.initDahuList();
				}
				fromSeat = moLastMajiangSeat;
				huMjs.add(lastMajiang);

			} else if (!nowDisCardIds.isEmpty()) {

				disMajiang = nowDisCardIds.get(0);
				fromSeat = disCardSeat;
				huMjs.add(disMajiang);
			}
		} else {
			huMjs.add(player.getHandMajiang().get(player.getHandMajiang().size() - 1));
		}
		// if(isBegin()){
		// //天胡
		// if(huBean == null){
		// huBean = player.checkHu(null,true);
		// }
		// if(huBean.isHu()){
		// huBean.setTianhu(true);
		// huBean.initDahuList();
		// }
		// }else if(nowDisCardSeat == lastWinSeat &&
		// seatMap.get(lastWinSeat).getOutPais().size() == 0){
		// //地胡
		// if(huBean == null){
		// huBean = player.checkHu(lastMajiang,true);
		// }
		// if(huBean.isHu()){
		// huBean.setDihu(true);
		// huBean.initDahuList();
		// }
		// }

		if (huBean == null) {
			// 自摸
			huBean = player.checkHu(disMajiang, disCardRound == 0);
			if (disMajiang == null) {
				if (player.getHuMjIds().size() > 0) {
					disMajiang = DehMj.getMajang(player.getHuMjIds().get(0));
				}
				if (disMajiang == null) {
					disMajiang = player.getLastMoMajiang();
				}
				if (disMajiang == null) {
					disMajiang = nowDisCardIds.get(0);
				}

			}
			DehMjTool.checkDahuMax(huBean, this, player, disMajiang, zimo);
			if (huBean.isHu()) {
				if (huBean.isQuanqiuren() && zimo) {
					huBean.setQuanqiuren(false);
				}

				if (gangSeat > 0) {
					if (moMajiangSeat == player.getSeat()) {
						if (player.getConGangNum() == 2) {
							huBean.setShuangGSH(true);
						} else if (player.getConGangNum() == 3) {
							huBean.setSanGSH(true);
						} else {
							huBean.setGangShangHua(true);
						}

						// isGangShangHu = true;
					} else {
						huBean.setGangShangPao(true);
					}
				}
				huBean.initDahuList();
			}
		}
		if (!huBean.isHu()) {
			return;
		}
		// 算牌型的分
		if (moGangHuList.contains(player.getSeat())) {
			// 补张的时候不算抢杠胡
			if (disEventAction != DehMjDisAction.action_buzhang) {
				huBean.setQGangHu(true);
				huBean.initDahuList();
			}
			// 抢杠胡
			DehMjPlayer moGangPlayer = getPlayerByHasMajiang(moGang);
			if (moGangPlayer == null) {
				moGangPlayer = seatMap.get(moMajiangSeat);
			}
			List<DehMj> moGangMajiangs = new ArrayList<>();
			moGangMajiangs.add(moGang);
			moGangPlayer.addOutPais(moGangMajiangs, 0, 0);
			// 摸杠被人胡了 相当于自己出了一张牌
			recordDisMajiang(moGangMajiangs, moGangPlayer);
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + 0 + "_" + DehMjHelper.toMajiangStrs(majiangs));
		}
		
		
		
		if (huBean.getDahuFan() > 0) {
			//
			if (zimo && huBean.isLongZhuaBeiHu()) {
				//huBean.addFan(1);
			}
			player.setDahu(huBean.getDahuList(), huBean.getDahuFan());
		}
		// if (huBean.getDahuPoint() > 0) {
		// player.setDahu(huBean.getDahuList());
		// if (zimo) {
		// int point = 0;
		// for (DehMjPlayer seatPlayer : seatMap.values()) {
		// if (seatPlayer.getSeat() != player.getSeat()) {
		// point += huBean.getDahuPoint();
		// seatPlayer.changeLostPoint(-huBean.getDahuPoint());
		// }
		// }
		// player.changeLostPoint(point);
		// } else {
		// player.changeLostPoint(huBean.getDahuPoint());
		// seatMap.get(disCardSeat).changeLostPoint(-huBean.getDahuPoint());
		// }
		// }

		if (isGangShangHu) {
			// 杠上花，只胡一张牌时，另外一张牌需要打出
			List<DehMj> gangDisMajiangs = getGangDisMajiangs();
			List<DehMj> chuMjs = new ArrayList<>();
			if (gangDisMajiangs != null && gangDisMajiangs.size() > 0) {
				for (DehMj mj : gangDisMajiangs) {
					if (!huMjs.contains(mj)) {
						chuMjs.add(mj);
					}
				}
			}
			if (chuMjs != null) {
				PlayMajiangRes.Builder chuPaiMsg = PlayMajiangRes.newBuilder();
				buildPlayRes(chuPaiMsg, player, DehMjDisAction.action_chupai, chuMjs);
				chuPaiMsg.setFromSeat(-1);
				broadMsgToAll(chuPaiMsg.build());
				player.addOutPais(chuMjs, DehMjDisAction.action_chupai, player.getSeat());
			}
		}

		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, action, huBean.getShowMajiangs());
		builder.addAllHuArray(player.getDahu());
		if (zimo) {
			builder.setZimo(1);
		}
		builder.setFromSeat(fromSeat);
		// 胡
		for (DehMjPlayer seat : seatMap.values()) {
			// 推送消息
			seat.writeSocket(builder.build());
		}
		// 加入胡牌数组
		addHuList(player.getSeat(), disMajiang == null ? 0 : disMajiang.getId());
		
		if(huBean.isGangShangPao()&&getDaiGen()!=1){
			calcPointGangRemain(disCardSeat);
		}
		
		
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + DehMjHelper.toMajiangStrs(huMjs) + "_"
				+ StringUtil.implode(player.getDahu(), ","));
		if (isCalcOver()) {
			// 等待别人胡牌 如果都确认完了，胡
			calcOver();
		} else {
			player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
		}
	}

	/**
	 * 找出拥有这张麻将的玩家
	 *
	 * @param majiang
	 * @return
	 */
	private DehMjPlayer getPlayerByHasMajiang(DehMj majiang) {
		for (DehMjPlayer player : seatMap.values()) {
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
		// 起牌小胡可以继续打牌
		if (!huActionList.isEmpty()) {
			over = true;
			DehMjPlayer moGangPlayer = null;
			if (!moGangHuList.isEmpty()) {
				// 如果有抢杠胡
				moGangPlayer = getPlayerByHasMajiang(moGang);
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
			DehMjPlayer disCSMajiangPlayer = seatMap.get(disCardSeat);
			for (int huseat : huActionList) {
				if (huConfirmMap.containsKey(huseat)) {
					if (disCardRound == 0) {
						// 天胡
						removeActionSeat(huseat);
					}
					continue;
				}
				PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
				DehMjPlayer seatPlayer = seatMap.get(huseat);
				buildPlayRes(disBuilder, disCSMajiangPlayer, 0, null);
				List<Integer> actionList = actionSeatMap.get(huseat);
				disBuilder.addAllSelfAct(actionList);
				seatPlayer.writeSocket(disBuilder.build());
			}
		}
		return over;
	}

	// private boolean isCalcOver() {
	// return isCalcOver(null);
	// }

	/**
	 * 吃碰杠
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
	private void chiPengGang(DehMjPlayer player, List<DehMj> majiangs, int action) {
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		logAction(player, action, 0, majiangs, null);
		// if (nowDisCardIds.size() > 1 && !isHasGangAction()) {
		// // 当前出牌不能操作
		// return;
		// }
		List<Integer> huList = getHuSeatByActionMap();
		huList.remove((Object) player.getSeat());
		// if (!huList.isEmpty()) {
		// // 胡最优先
		// return;
		// }

		if (!checkAction(player, majiangs, new ArrayList<>(), action)) {
			player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
			return;
		}

		List<DehMj> handMajiang = new ArrayList<>(player.getHandMajiang());
		DehMj disMajiang = null;
		if (isHasGangAction()) {
			for (int majiangId : gangSeatMap.keySet()) {
				if (action == DehMjDisAction.action_chi) {
					List<Integer> majiangIds = DehMjHelper.toMajiangIds(majiangs);
					if (majiangIds.contains(majiangId)) {
						disMajiang = DehMj.getMajang(majiangId);
						gangActedMj = disMajiang;
						handMajiang.add(disMajiang);
						if (majiangs.size() > 1) {
							majiangs.remove(disMajiang);
						}
						break;
					}
				} else {
					DehMj mj = DehMj.getMajang(majiangId);
					if (mj != null && majiangs.get(0).getVal() == mj.getVal()) {
						disMajiang = mj;
						int removeIndex = -1;
						for (int i = 0; i < majiangs.size(); i++) {
							if (majiangs.get(i).getId() == majiangId) {
								removeIndex = i;
							}
						}
						if (removeIndex != -1) {
							majiangs.remove(removeIndex);
						}
					}
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
			sameCount = DehMjHelper.getMajiangCount(majiangs, majiangs.get(0).getVal());
		}
		if (action == DehMjDisAction.action_buzhang) {
			majiangs = DehMjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
			sameCount = majiangs.size();
		} else if (action == DehMjDisAction.action_minggang) {
			// 如果是杠 后台来找出是明杠还是暗杠
			majiangs = DehMjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
			sameCount = majiangs.size();
			if (sameCount == 4) {
				// 有4张一样的牌是暗杠
				action = DehMjDisAction.action_angang;
			}
			// 其他是明杠

		} else if (action == DehMjDisAction.action_buzhang_an) {
			// 暗杠补张
			majiangs = DehMjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
			sameCount = majiangs.size();
		}
		// /////////////////////
		if (action == DehMjDisAction.action_chi) {
			boolean can = canChi(player, player.getHandMajiang(), majiangs, disMajiang);
			if (!can) {
				return;
			}
		} else if (action == DehMjDisAction.action_peng) {
			boolean can = canPeng(player, majiangs, sameCount, disMajiang);
			if (!can) {
				return;
			}
		} else if (action == DehMjDisAction.action_angang) {
			boolean can = canAnGang(player, majiangs, sameCount, action);
			if (!can) {
				return;
			}
			// if (!player.isTingPai(majiangs.get(0).getVal())) {
			// return;
			// }
		} else if (action == DehMjDisAction.action_minggang) {
			boolean can = canMingGang(player, player.getHandMajiang(), majiangs, sameCount, disMajiang);
			if (!can) {
				return;
			}
			// if (!player.isTingPai(majiangs.get(0).getVal())) {
			// return;
			// }
			// 特殊处理一张牌明杠的时候别人可以胡
			if (sameCount == 1 && canGangHu()) {
				if (checkQGangHu(player, majiangs, action)) {
					return;
				}
			}
		} else if (action == DehMjDisAction.action_buzhang) {
			boolean can = false;
			if (sameCount == 4) {
				can = canAnGang(player, majiangs, sameCount, action);
			} else {
				can = canMingGang(player, player.getHandMajiang(), majiangs, sameCount, disMajiang);
			}
			if (!can) {
				return;
			}
			// 特殊处理一张牌明杠的时候别人可以胡
			if (sameCount == 1 && canGangHu()) {
				if (checkQGangHu(player, majiangs, action)) {
					return;
				}
			}
		} else if (action == DehMjDisAction.action_buzhang_an) {
			boolean can = false;
			if (sameCount == 4) {
				can = canAnGang(player, majiangs, sameCount, action);
			}
			if (!can) {
				return;
			}
		} else {
			return;
		}
		if(getDaiGen()!=1){
			calcPoint(player, action, sameCount, majiangs);
//			calcPointDaiGen(player, action, sameCount, majiangs);
		}
		boolean disMajiangMove = false;
		if (disMajiang != null) {
			// 碰或者杠
			if (action == DehMjDisAction.action_minggang && sameCount == 3) {
				// 接杠
				disMajiangMove = true;
			} else if (action == DehMjDisAction.action_chi) {
				// 吃
				disMajiangMove = true;
			} else if (action == DehMjDisAction.action_peng) {
				// 碰
				disMajiangMove = true;
			} else if (action == DehMjDisAction.action_buzhang && sameCount == 3) {
				// 自己三张补张
				disMajiangMove = true;
			}
		}
		if (disMajiangMove) {
			if (action == DehMjDisAction.action_chi) {
				majiangs.add(1, disMajiang);// 吃的牌放第二位
			} else {
				majiangs.add(disMajiang);
			}
			builder.setFromSeat(disCardSeat);
			List<DehMj> disMajiangs = new ArrayList<>();
			disMajiangs.add(disMajiang);
			seatMap.get(disCardSeat).removeOutPais(disMajiangs, action);
		}
		chiPengGang(builder, player, majiangs, action);
	}

	private void chiPengGang(PlayMajiangRes.Builder builder, DehMjPlayer player, List<DehMj> majiangs, int action) {
		setIsBegin(false);
		processHideMj(player);

		player.addOutPais(majiangs, action, disCardSeat);
		buildPlayRes(builder, player, action, majiangs);
		List<Integer> removeActList = removeActionSeat(player.getSeat());
		clearGangActionMap();
		clearActionSeatMap();
		setGangSeat(0);
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + DehMjHelper.toMajiangStrs(majiangs));
		// 不是普通出牌
		setNowDisCardSeat(player.getSeat());
		checkClearGangDisMajiang();
		if (action == DehMjDisAction.action_chi || action == DehMjDisAction.action_peng) {
			player.setConGangNum(0);
			 List<Integer> arr = player.checkMo(null, false);
			 if (!arr.isEmpty()) {
			 arr.set(DehMjAction.ZIMO,0);
			 arr.set(DehMjAction.HU,0);
			 addActionSeat(player.getSeat(), arr);
			 }
		}
		for (DehMjPlayer seatPlayer : seatMap.values()) {
			// 推送消息
			PlayMajiangRes.Builder copy = builder.clone();
			if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
				copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
			}
			seatPlayer.writeSocket(copy.build());
		}

		// 取消漏炮
		player.setPassMajiangVal(0);
		if (action == DehMjDisAction.action_minggang || action == DehMjDisAction.action_angang) {
			// 明杠和暗杠摸牌
			// gangMoMajiang(player,majiangs.get(0),action);
			// 补张
			moMajiang(player, true);
			gangSeat = player.getSeat();
			player.addConGangNum(1);
		} 
		
//		else if (action == DehMjDisAction.action_buzhang) {
//			// 补张
//			moMajiang(player, true);
//			gangSeat = player.getSeat();
//			player.addConGangNum(1);
//		} else if (action == DehMjDisAction.action_buzhang_an) {
//			// 补张
//			moMajiang(player, true);
//			gangSeat = player.getSeat();
//			player.addConGangNum(1);
//		}

		if (action == DehMjDisAction.action_chi || action == DehMjDisAction.action_peng) {
			sendTingInfo(player);
		}

		setDisEventAction(action);
		robotDealAction();
		logAction(player, action, 0, majiangs, removeActList);
		player.changeAction(9, 1);
	}

	private boolean checkQGangHu(DehMjPlayer player, List<DehMj> majiangs, int action) {
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		Map<Integer, List<Integer>> huListMap = new HashMap<>();
		for (DehMjPlayer seatPlayer : seatMap.values()) {
			if (seatPlayer.getUserId() == player.getUserId()) {
				continue;
			}
			// 推送消息
			List<Integer> hu = seatPlayer.checkDisMajiang(majiangs.get(0));
			if (!hu.isEmpty() && hu.get(0) == 1) {
				addActionSeat(seatPlayer.getSeat(), hu);
				huListMap.put(seatPlayer.getSeat(), hu);
			}
		}

		// 可以胡牌
		if (!huListMap.isEmpty()) {
			setDisEventAction(action);
			setMoGang(majiangs.get(0), new ArrayList<>(huListMap.keySet()));
			buildPlayRes(builder, player, action, majiangs);
			for (Entry<Integer, List<Integer>> entry : huListMap.entrySet()) {
				PlayMajiangRes.Builder copy = builder.clone();
				DehMjPlayer seatPlayer = seatMap.get(entry.getKey());
				copy.addAllSelfAct(entry.getValue());
				seatPlayer.writeSocket(copy.build());
			}
			return true;
		}
		return false;

	}

	public void checkSendGangRes(Player player) {
		if (isHasGangAction()) {
			List<DehMj> moList = getGangDisMajiangs();
			DehMjPlayer disPlayer = seatMap.get(disCardSeat);
			GangMoMajiangRes.Builder gangbuilder = GangMoMajiangRes.newBuilder();
			gangbuilder.setGangId(gangMajiang.getId());
			gangbuilder.setUserId(disPlayer.getUserId() + "");
			gangbuilder.setName(disPlayer.getName() + "");
			gangbuilder.setSeat(disPlayer.getSeat());
			gangbuilder.setRemain(leftMajiangs.size());
			gangbuilder.setReconnect(1);
			gangbuilder.setDice(gangDice);
			gangbuilder.setHasAct(isHasGangAction() ? 1 : 0);
			gangbuilder.setMjNum(moList.size());
			for (DehMj mj : moList) {
				GangPlayMajiangRes.Builder playBuilder = GangPlayMajiangRes.newBuilder();
				playBuilder.setMajiangId(mj.getId());
				Map<Integer, List<Integer>> seatActionList = gangSeatMap.get(mj.getId());
				if (seatActionList != null && seatActionList.containsKey(player.getSeat())) {
					playBuilder.addAllSelfAct(seatActionList.get(player.getSeat()));
				}
				gangbuilder.addGangActs(playBuilder);
			}
			if (isHasGangAction(disCardSeat) && player.getSeat() != disCardSeat) {
				// 庄家未操作，其他玩家不能看到杠后摸的两张牌
				gangbuilder.clearGangActs();
			}
			player.writeSocket(gangbuilder.build());
		}
	}

	/**
	 * 普通出牌
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
	private void chuPai(DehMjPlayer player, List<DehMj> majiangs, int action) {
		if (majiangs.size() != 1) {
			return;
		}
		if (!player.isAlreadyMoMajiang()) {
			// 还没有摸牌
			return;
		}
		if (!tempActionMap.isEmpty()) {
			LogUtil.e(player.getName() + "出牌清理临时操作！");
			clearTempAction();
		}
		if (!player.getGang().isEmpty()) {
			// 已经杠过了牌
			// if (player.getLastMoMajiang().getId() != majiangs.get(0).getId())
			// {
			// return;
			// }
		}
		if (!actionSeatMap.isEmpty()) {// 出牌自动过掉手上操作
			guo(player, null, DehMjDisAction.action_pass);
		}
		if (!actionSeatMap.isEmpty()) {
			player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
			return;
		}
		
		DehMj lasmj = player.getLastMoMajiang();
		
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, action, majiangs);
		// 普通出牌
		clearActionSeatMap();
		clearGangActionMap();
		player.setConGangNum(0);
		setNowDisCardSeat(calcNextSeat(player.getSeat()));
		recordDisMajiang(majiangs, player);
		player.addOutPais(majiangs, action, player.getSeat());
		player.clearPassHu();
		logAction(player, action, 0, majiangs, null);

		for (DehMjPlayer seat : seatMap.values()) {
			List<Integer> list = new ArrayList<>();
			if (seat.getUserId() != player.getUserId()) {
				list = seat.checkDisMajiang(majiangs.get(0));
				if (list.contains(1)) {
					addActionSeat(seat.getSeat(), list);
					logChuPaiActList(seat, majiangs.get(0), list);
				}
			}
		}
		// 风牌，连续打出
		if (majiangs.get(0).isFeng()) {
			player.changeAction(8, 1);
			if (player.getActionNum(8) >= 10) {
				List<Integer> list = new ArrayList<>();
				list.add(1);
				addActionSeat(player.getSeat(), list);
			}
		} else {
			player.setAction(8, 0);
		}

		player.changeAction(9, 1);

		
		if(player.getBaoTingS()==1){
			//打的不是摸得
			if(!checkBaoting(player)||(majiangs.get(0).getId()!=lasmj.getId()&&player.getActionNum(9) > 1)||player.isChiPengGang()){
				player.setBaoTingS(2);
				ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_bs_Baoting,
						player.getSeat(), player.getBaoTingS());
				for (DehMjPlayer seatPlayer : seatMap.values()) {
					seatPlayer.writeSocket(com.build());
				}
			}
			
		}
		
		
		setDisEventAction(action);
		sendDisMajiangAction(builder);
		// 取消漏炮
		player.setPassMajiangVal(0);
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + DehMjHelper.toMajiangStrs(majiangs));
		setIsBegin(false);
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
			if (actionList.get(DehMjAction.HU) == 1 || actionList.get(DehMjAction.ZIMO) == 1) {
				// 胡
				huList.add(seat);
			}

		}
		return huList;
	}

	private void sendDisMajiangAction(PlayMajiangRes.Builder builder) {
		// 如果有人可以胡 优先胡
		// 把胡的找出来
		buildPlayRes1(builder);
		List<Integer> huList = getHuSeatByActionMap();
		if (huList.size() > 0) {
			// 有人胡,优先胡
			for (DehMjPlayer seatPlayer : seatMap.values()) {
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
			for (DehMjPlayer seat : seatMap.values()) {
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

	private void err(DehMjPlayer player, int action, String errMsg) {
		LogUtil.e("play:tableId-->" + id + " playerId-->" + player.getUserId() + " action-->" + action + " err:"
				+ errMsg);
	}

	/**
	 * 出牌
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
	public synchronized void playCommand(DehMjPlayer player, List<DehMj> majiangs, int action) {
		if (!moGangHuList.isEmpty()) {// 被人抢杠胡
			if (!moGangHuList.contains(player.getSeat())) {
				return;
			}
		}

		if (DehMjDisAction.action_hu == action) {
			hu(player, majiangs, action);
			return;
		}
		
		if(action == DehMjDisAction.action_minggang||action == DehMjDisAction.action_angang){
			if(majiangs!=null &&majiangs.size()>=2&&getDaiGen()==1){
				DehMj moMjc  = majiangs.get(majiangs.size()-1);
				majiangs.remove(moMjc);
				if(moMjc!=null){
					logdaiGenMsg(player, "gangDaiGen|"+moMjc.getId()+"|"+getLastTwoMj());
				}
				//校验id
				DehMj moMjc2 = getLastTwoMjCId(moMjc);
				if(moMjc2 == null){
					err(player, action, "杠选的牌不存在,请选择另一张" + moMjc);
					return;
				}
				setDaiGenGangMj(moMjc2); 
				
			}
		}
		
		// 手上没有要出的麻将
		if (!isHasGangAction() && action != DehMjDisAction.action_minggang && action != DehMjDisAction.action_buzhang)
			if (!player.getHandMajiang().containsAll(majiangs)) {
				err(player, action, "没有找到出的牌" + majiangs);
				return;
			}
		changeDisCardRound(1);
		if (action == DehMjDisAction.action_pass) {
			guo(player, majiangs, action);
		} else if (action == DehMjDisAction.action_moMjiang) {
		} else if (action != 0) {

			chiPengGang(player, majiangs, action);
		} else {

			chuPai(player, majiangs, action);
		}

	}

	/**
	 * 最后一张牌(海底捞)
	 *
	 * @param player
	 * @param action
	 */
	public synchronized void moLastMajiang(DehMjPlayer player, int action) {
		if (getLeftMajiangCount() != 1) {
			return;
		}
		if (player.getSeat() != askLastMajaingSeat) {
			return;
		}

		if (action == DehMjDisAction.action_passmo) {
			// 发送下一个海底摸牌res
			sendMoLast(player, 0);
			removeMoLastSeat(player.getSeat());
			if (moLastSeats == null || moLastSeats.size() == 0) {
				calcOver();
				return;
			}
			sendAskLastMajiangRes(0);
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + DehMjDisAction.action_pass + "_");
		} else {
			sendMoLast(player, 0);
			clearMoLastSeat();
			clearActionSeatMap();
			setMoLastMajiangSeat(player.getSeat());
			DehMj majiang = getLeftMajiang();
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + DehMjDisAction.action_moLastMjiang + "_"
					+ majiang.getId());
			setMoMajiangSeat(player.getSeat());
			player.setPassMajiangVal(0);
			setLastMajiang(majiang);
			setDisCardSeat(player.getSeat());

			// /////////////////////////////////////////////
			// 发送海底捞的牌

			// /////////////////////////////////////////

			List<DehMj> disMajiangs = new ArrayList<>();
			disMajiangs.add(majiang);

			MoMajiangRes.Builder moRes = MoMajiangRes.newBuilder();
			moRes.setUserId(player.getUserId() + "");
			moRes.setRemain(getLeftMajiangCount());
			moRes.setSeat(player.getSeat());

			// 先看看自己能不能胡
			List<Integer> selfActList = player.checkDisMajiang(majiang);
			player.moMajiang(majiang);
			selfActList = DehMjAction.keepHu(selfActList);
			if (selfActList != null && !selfActList.isEmpty()) {
				addActionSeat(player.getSeat(), selfActList);
			}
			for (DehMjPlayer seatPlayer : seatMap.values()) {
				if (seatPlayer.getUserId() == player.getUserId()) {
					MoMajiangRes.Builder selfMsg = moRes.clone();
					selfMsg.addAllSelfAct(selfActList);
					selfMsg.setMajiangId(majiang.getId());
					player.writeSocket(selfMsg.build());
				} else {
					MoMajiangRes.Builder otherMsg = moRes.clone();
					seatPlayer.writeSocket(otherMsg.build());
				}
			}

			// 自己能胡
			if (DehMjAction.hasHu(selfActList)) {
				// 优先自己胡
				// hu(player, null, DehMjDisAction.action_moLastMjiang_hu);
				return;
			} else {
				chuLastPai(player);
			}
			// for (int seat : actionSeatMap.keySet()) {
			// hu(seatMap.get(seat), null, action);
			// }
		}

	}

	private void chuLastPai(DehMjPlayer player) {
		DehMj majiang = lastMajiang;
		List<DehMj> disMajiangs = new ArrayList<>();
		disMajiangs.add(majiang);
		PlayMajiangRes.Builder chuRes = DehMjResTool.buildPlayRes(player, DehMjDisAction.action_chupai, disMajiangs);
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + DehMjDisAction.action_chupai + "_"
				+ DehMjHelper.toMajiangStrs(disMajiangs));
		setNowDisCardIds(disMajiangs);
		setNowDisCardSeat(calcNextSeat(player.getSeat()));
		recordDisMajiang(disMajiangs, player);
		player.addOutPais(disMajiangs, DehMjDisAction.action_chupai, player.getSeat());
		player.clearPassHu();
		for (DehMjPlayer seatPlayer : seatMap.values()) {
			if (seatPlayer.getUserId() == player.getUserId()) {
				seatPlayer.writeSocket(chuRes.clone().build());
				continue;
			}
			List<Integer> otherActList = seatPlayer.checkDisMajiang(majiang);
			otherActList = DehMjAction.keepHu(otherActList);
			PlayMajiangRes.Builder msg = chuRes.clone();
			if (DehMjAction.hasHu(otherActList)) {
				addActionSeat(seatPlayer.getSeat(), otherActList);
				msg.addAllSelfAct(otherActList);
			}
			seatPlayer.writeSocket(msg.build());
		}
		if (actionSeatMap.isEmpty()) {
			calcOver();
		}
	}

	private void passMoHu(DehMjPlayer player, List<DehMj> majiangs, int action) {
		if (!moGangHuList.contains(player.getSeat())) {
			return;
		}

		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, action, majiangs);
		builder.setSeat(nowDisCardSeat);
		removeActionSeat(player.getSeat());
		player.writeSocket(builder.build());
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + DehMjHelper.toMajiangStrs(majiangs));
		if (isCalcOver()) {
			calcOver();
			return;
		}
		player.setPassMajiangVal(nowDisCardIds.get(0).getVal());

		DehMjPlayer moGangPlayer = seatMap.get(moMajiangSeat);
		if (moGangHuList.isEmpty()) {
			majiangs = new ArrayList<>();
			majiangs.add(moGang);
			
			if(getDaiGen()!=1){
				calcPoint(moGangPlayer, DehMjDisAction.action_minggang, 1, majiangs);
			}
			builder = PlayMajiangRes.newBuilder();
			chiPengGang(builder, moGangPlayer, majiangs, DehMjDisAction.action_minggang);
		}

	}

	/**
	 * guo
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
	private void guo(DehMjPlayer player, List<DehMj> majiangs, int action) {
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return;
		}
		if (!moGangHuList.isEmpty()) {
			// 有摸杠胡的优先处理
			passMoHu(player, majiangs, action);
			return;
		}
		List<Integer> removeActionList = removeActionSeat(player.getSeat());
		int xiaoHu = DehMjAction.getFirstXiaoHu(removeActionList);
		logAction(player, action, xiaoHu, majiangs, removeActionList);
		boolean isBegin = isBegin();
	

		if (moLastMajiangSeat == player.getSeat()) {
			// 摸海底可以胡的人点过，将海底牌打出
			chuLastPai(player);
			return;
		}
		checkClearGangDisMajiang();
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, action, majiangs);
		builder.setSeat(nowDisCardSeat);
		player.writeSocket(builder.build());
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + DehMjHelper.toMajiangStrs(majiangs));
		if (isCalcOver()) {
			calcOver();
			return;
		}
		if (DehMjAction.hasHu(removeActionList) && disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
			// 漏炮
			player.passHu(nowDisCardIds.get(0).getVal());
		}

		if (removeActionList.get(0) == 1 && disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
			player.setPassMajiangVal(nowDisCardIds.get(0).getVal());
		}
		
		if (removeActionList.get(1) == 1 && disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
			player.setPassPengMajVal(nowDisCardIds.get(0).getVal());
		}
		
		
		if (!actionSeatMap.isEmpty()) {
			DehMjPlayer disCSMajiangPlayer = seatMap.get(disCardSeat);
			PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
			buildPlayRes(disBuilder, disCSMajiangPlayer, 0, null);
			for (int seat : actionSeatMap.keySet()) {
				List<Integer> actionList = actionSeatMap.get(seat);
				PlayMajiangRes.Builder copy = disBuilder.clone();
				copy.addAllSelfAct(new ArrayList<>());
				if (actionList != null && !tempActionMap.containsKey(seat)) {
					if (actionList != null) {
						copy.addAllSelfAct(actionList);
					}
				}
				DehMjPlayer seatPlayer = seatMap.get(seat);
				seatPlayer.writeSocket(copy.build());
			}
		}
		
		

		/*
		 * if (player.isAlreadyMoMajiang() && !player.getGang().isEmpty()) { //
		 * 杠牌后自动出牌 List<DehMj> disMjiang = new ArrayList<>();
		 * disMjiang.add(player.getLastMoMajiang()); chuPai(player, disMjiang,
		 * 0); }
		 */

		if (isBegin && !hasBaoTing() && player.getSeat() == lastWinSeat) {
			// 庄家过非小胡，提示庄家出牌
			ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);
			player.writeSocket(com.build());
		} else {
			checkBegin(player);
		}
		
		
		boolean chekcmmo = true;
		if (player.isAlreadyMoMajiang() && player.getActionNum(9) ==0) {
			boolean baoting = checkBaoTingMsg(player);
			if(baoting){
				chekcmmo = false;
			}
		}
		

		// 先过 后执行临时可做操作里面优先级最高的玩家操作
		refreshTempAction(player);
		if(chekcmmo){
			checkMo();
		}

		if (player.isAlreadyMoMajiang()) {
			sendTingInfo(player);
		}
	}

	private void calcPoint(DehMjPlayer player, int action, int sameCount, List<DehMj> majiangs) {

		int lostPoint = 0;
		// int getPoint = 0;
		int[] seatPointArr = new int[getMaxPlayerCount() + 1];
		if (action == DehMjDisAction.action_peng) {
			return;

		} else if (action == DehMjDisAction.action_angang) {
			// 暗杠相当于自摸每人出2分
			lostPoint = -2;
			// getPoint = 2 * (getMaxPlayerCount() - 1);
			player.changeGangRemainArr(DehMjDisAction.action_angang,0);
		} else if (action == DehMjDisAction.action_minggang) {
			if (sameCount == 1) {
				// 碰牌之后再抓一个牌每人出1分
				// 放杠的人出3分

				if (player.isPassGang(majiangs.get(0))) {
					// 特殊处理 可以碰可以杠的牌 选择了碰 再杠不算分
					return;
				}
				
				int fangSeat = player.getFangPengSeat(majiangs.get(0).getVal());
				if(fangSeat>0){
					calFangGangFen(player, seatPointArr,fangSeat);
				}else{
					lostPoint = -1;
				}
				// getPoint = 1 * (getMaxPlayerCount() - 1);
			} else if (sameCount == 3) {
				// 放杠
				calFangGangFen(player, seatPointArr,disCardSeat);
			}
		}

		if (lostPoint != 0) {
			int totalPoint = 0;
			int lostPoint2 = lostPoint;
			for (DehMjPlayer seat : seatMap.values()) {
				if (seat.getUserId() != player.getUserId()) {
//					if (getBuyPoint() > 2) {
//						lostPoint2 = (seat.getPiaoPoint() + player.getPiaoPoint() + 1) * lostPoint;
//					}
					totalPoint += lostPoint2;
					seat.changeLostPoint(lostPoint2);
					seatPointArr[seat.getSeat()] = lostPoint2;
				}
			}
			player.changeLostPoint(-totalPoint);

		}

		// String seatPointStr = "";
		// for (int i = 1; i <= getMaxPlayerCount(); i++) {
		// seatPointStr += seatPointArr[i] + ",";
		// }
		// seatPointStr = seatPointStr.substring(0, seatPointStr.length() - 1);
		// ComMsg.ComRes.Builder res =
		// SendMsgUtil.buildComRes(WebSocketMsgType.res_code_gangFen,
		// seatPointStr);
		// GeneratedMessage msg = res.build();
		// broadMsgToAll(msg);

        logPointError("calcPoint");
	}
	
	
	private void calFangGangFen(DehMjPlayer player, int[] seatPointArr,int disSeat) {
		DehMjPlayer disPlayer = seatMap.get(disSeat);
		
		// disPlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index13,
		// 1);
		int point = 1;
//		if (getBuyPoint() > 2) {
//			point = disPlayer.getPiaoPoint() + player.getPiaoPoint() + 1;
//		}
		disPlayer.changeLostPoint(-(point));
		seatPointArr[disPlayer.getSeat()] = -point;
		player.changeLostPoint(point);
		seatPointArr[player.getSeat()] = point;
		
		
		player.changeGangRemainArr(DehMjDisAction.action_minggang,disPlayer.getSeat());
	}

	
	private void calcPointGangRemain(int gangSeat) {
		
		DehMjPlayer player = seatMap.get(gangSeat);
		if(player==null){
			return;
		}
		
		if(huConfirmMap.size()>1){
			return;
		}
		
		int[] gangRemainArr = player.getGangRemainArr();
		
//		int gangAction = gangRemainArr[0];
		int fangGangSeat = gangRemainArr[1];
		
		
		if(fangGangSeat>0){
			DehMjPlayer fPlayer = seatMap.get(fangGangSeat);
			if(fPlayer==null){
				return;
			}
			
			fPlayer.changeLostPoint(1);
			player.changeLostPoint(-1);
			
		}else{
			int totalPoint = 0;
			for (DehMjPlayer seat : seatMap.values()) {
				if (seat.getUserId() != player.getUserId()) {
					totalPoint += 2;
					seat.changeLostPoint(2);
				}
			}
			player.changeLostPoint(-totalPoint);
		}
		

	}

	private void recordDisMajiang(List<DehMj> majiangs, DehMjPlayer player) {
		setNowDisCardIds(majiangs);
		setDisCardSeat(player.getSeat());
	}

	public List<DehMj> getNowDisCardIds() {
		return nowDisCardIds;
	}

	public void setDisEventAction(int disAction) {
		this.disEventAction = disAction;
		changeExtend();
	}

	public void setNowDisCardIds(List<DehMj> nowDisCardIds) {
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
				DehMjPlayer player = seatMap.get(seat);
				if (player != null && player.isRobot()) {
					// 如果是机器人可以直接决定
					List<Integer> actionList = actionSeatMap.get(seat);
					if (actionList == null) {
						continue;
					}
					List<DehMj> list = new ArrayList<>();
					if (!nowDisCardIds.isEmpty()) {
						list = DehMjQipaiTool.getVal(player.getHandMajiang(), nowDisCardIds.get(0).getVal());
					}
					if (actionList.get(0) == 1) {
						// 胡
						playCommand(player, new ArrayList<DehMj>(), DehMjDisAction.action_hu);

					} else if (actionList.get(3) == 1) {
						playCommand(player, list, DehMjDisAction.action_angang);

					} else if (actionList.get(2) == 1) {
						playCommand(player, list, DehMjDisAction.action_minggang);

					} else if (actionList.get(1) == 1) {
						playCommand(player, list, DehMjDisAction.action_peng);

					} else if (actionList.get(4) == 1) {
						playCommand(player, player.getCanChiMajiangs(nowDisCardIds.get(0)), DehMjDisAction.action_chi);

					} else {
						System.out.println("---------->" + JacksonUtil.writeValueAsString(actionList));
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
			// for (DehMjPlayer player : seatMap.values()) {
			// if (player.isRobot() && player.canXiaoHu()) {
			// playCommand(player, new ArrayList<DehMj>(),
			// DehMjDisAction.action_xiaohu);
			// }
			// }

			int nextseat = getNextActionSeat();
			DehMjPlayer next = seatMap.get(nextseat);
			if (next != null && next.isRobot()) {
				List<Integer> actionList = actionSeatMap.get(next.getSeat());
				int xiaoHuAction = -1;
				if (actionList != null) {
					List<DehMj> list = null;
					if (actionList.get(0) == 1) {
						// 胡
						playCommand(next, new ArrayList<DehMj>(), DehMjDisAction.action_hu);

					} else if ((xiaoHuAction = DehMjAction.getFirstXiaoHu(actionList)) > 0) {

						playCommand(next, new ArrayList<DehMj>(), DehMjDisAction.action_pass);

					} else if (actionList.get(3) == 1) {
						// 机器人暗杠
						Map<Integer, Integer> handMap = DehMjHelper.toMajiangValMap(next.getHandMajiang());
						for (Entry<Integer, Integer> entry : handMap.entrySet()) {
							if (entry.getValue() == 4) {
								// 可以暗杠
								list = DehMjHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
							}
						}
						playCommand(next, list, DehMjDisAction.action_angang);

					} else if (actionList.get(5) == 1) {
						// 机器人补张
						Map<Integer, Integer> handMap = DehMjHelper.toMajiangValMap(next.getHandMajiang());
						for (Entry<Integer, Integer> entry : handMap.entrySet()) {
							if (entry.getValue() == 4) {
								// 可以补张
								list = DehMjHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
							}
						}
						if (list == null) {
							if (next.isAlreadyMoMajiang()) {
								list = DehMjQipaiTool.getVal(next.getHandMajiang(), next.getLastMoMajiang().getVal());

							} else {
								list = DehMjQipaiTool.getVal(next.getHandMajiang(), nowDisCardIds.get(0).getVal());
								list.add(nowDisCardIds.get(0));
							}
						}

						playCommand(next, list, DehMjDisAction.action_buzhang);

					} else if (actionList.get(2) == 1) {
						Map<Integer, Integer> pengMap = DehMjHelper.toMajiangValMap(next.getPeng());
						for (DehMj handMajiang : next.getHandMajiang()) {
							if (pengMap.containsKey(handMajiang.getVal())) {
								// 有碰过
								list = new ArrayList<>();
								list.add(handMajiang);
								playCommand(next, list, DehMjDisAction.action_minggang);
								break;
							}
						}

					} else if (actionList.get(1) == 1) {
						// playCommand(next, list, DehMjDisAction.action_peng);

					} else if (actionList.get(4) == 1) {
						DehMj majiang = null;
						List<DehMj> chiList = null;
						if (nowDisCardIds.size() == 1) {
							majiang = nowDisCardIds.get(0);
							chiList = next.getCanChiMajiangs(majiang);
						} else {
							for (int majiangId : gangSeatMap.keySet()) {
								Map<Integer, List<Integer>> actionMap = gangSeatMap.get(majiangId);
								List<Integer> action = actionMap.get(next.getSeat());
								if (action != null) {
									// List<Integer> disActionList =
									// MajiangDisAction.parseToDisActionList(action);
									if (action.get(4) == 1) {
										majiang = DehMj.getMajang(majiangId);
										chiList = next.getCanChiMajiangs(majiang);
										chiList.add(majiang);
										break;
									}

								}

							}

						}

						playCommand(next, chiList, DehMjDisAction.action_chi);

					} else {
						System.out.println("!!!!!!!!!!" + JacksonUtil.writeValueAsString(actionList));

					}

				} else {
					int maJiangId = DehMjRobotAI.getInstance().outPaiHandle(0, next.getHandPais(),
							new ArrayList<Integer>());
					List<DehMj> majiangList = DehMjHelper.toMajiang(Arrays.asList(maJiangId));
					if (next.isRobot()) {
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					playCommand(next, majiangList, 0);
				}

			}
		}
	}

	@Override
	protected void deal() {
		if (lastWinSeat == 0) {
			// 第一局丢筛子
			int masterseat = playerMap.get(masterId).getSeat();
			setLastWinSeat(masterseat);
		}
		setDisCardSeat(lastWinSeat);
		setNowDisCardSeat(lastWinSeat);
		setMoMajiangSeat(lastWinSeat);
		List<Integer> copy = new ArrayList<>(DehMjConstants.baoshan_mjList);
		if (youfeng == 1) {
			copy.addAll(DehMjConstants.feng_mjList);
		}
		addPlayLog(copy.size() + "");
		List<List<DehMj>> list;
		if (zp == null) {
			list = DehMjTool.fapai(copy, getMaxPlayerCount());
		} else {
			list = DehMjTool.fapai(copy, getMaxPlayerCount(), zp);
		}
		int i = 1;
		List<Integer> removeIndex = new ArrayList<>();
		for (DehMjPlayer player : playerMap.values()) {
			player.changeState(player_state.play);
			if (player.getSeat() == lastWinSeat) {
				player.dealHandPais(list.get(0));
				removeIndex.add(0);
				continue;
			}
			player.dealHandPais(list.get(i));
			removeIndex.add(i);
			i++;
		}

		// 桌上剩余的牌
		List<DehMj> leftMjs = new ArrayList<>();
		
//		// 没有发出去的牌退回剩余牌中
		for (int j = 0; j < list.size(); j++) {
			if (!removeIndex.contains(j)) {
				leftMjs.addAll(list.get(j));
			}
		}
		
		setLeftMajiangs(leftMjs);
	}

	/**
	 * 初始化桌子上剩余牌
	 *
	 * @param leftMajiangs
	 */
	public void setLeftMajiangs(List<DehMj> leftMajiangs) {
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
	public DehMj getLeftMajiang() {
		if (this.leftMajiangs.size() > 0) {
			DehMj majiang = this.leftMajiangs.remove(0);
			dbParamMap.put("leftPais", JSON_TAG);
			return majiang;
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

	//
	// private int getNearSeat(int nowSeat, List<Integer> seatList) {
	// if (seatList.contains(nowSeat)) {
	// // 出牌离自己是最近的
	// return nowSeat;
	// }
	// for (int i = 0; i < 3; i++) {
	// int seat = calcNextSeat(nowSeat);
	// if (seatList.contains(seat)) {
	// return seat;
	// }
	// nowSeat = seat;
	// }
	// return 0;
	// }

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

	public Map<Integer, DehMjPlayer> getSeatMap2() {
		return seatMap;
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
		res.setLastWinSeat(lastWinSeat);
		res.setMasterId(masterId + "");
		res.addExt(payType); // 0
		res.addExt(getConifg(0)); // 1
		res.addExt(calcBird); // 2
		res.addExt(birdNum); // 3
		res.addExt(gpsWarn); // 4
		res.addExt(youfeng); // 5
		res.addExt(yitiaolong); // 6
		res.addExt(baoting); // 7
		res.addExt(siguiyi); // 8
		res.addExt(buyPoint); // 15
		res.addExt(isCalcBanker); // 16
		res.addExt(isBegin() ? 1 : 0); // 17
		res.addExt(isAutoPlay); // 17

		res.addCreditConfig(creditMode); // 0
		res.addCreditConfig(creditJoinLimit); // 1
		res.addCreditConfig(creditDissLimit); // 2
		res.addCreditConfig(creditDifen); // 3
		res.addCreditConfig(creditCommission); // 4
		res.addCreditConfig(creditCommissionMode1); // 5
		res.addCreditConfig(creditCommissionMode2); // 6
		res.addCreditConfig(creditCommissionLimit); // 7

		res.addStrExt(StringUtil.implode(moTailPai, ",")); // 0
		res.setDealDice(dealDice);
		res.setRenshu(getMaxPlayerCount());
		
		if(getDaiGen()==1){
			List<Integer> mjs = getLastTwoMj();
			res.addAllScoreCard(mjs);
		}
		
		
		
		if (leftMajiangs != null) {
			res.setRemain(leftMajiangs.size());
		} else {
			res.setRemain(0);
		}
		List<PlayerInTableRes> players = new ArrayList<>();
		for (DehMjPlayer player : playerMap.values()) {
			PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(isrecover);
			if (player.getUserId() == userId || showMjSeat.contains(player.getSeat())) {
				playerRes.addAllHandCardIds(player.getHandPais());
			}
			if (player.getSeat() == disCardSeat && nowDisCardIds != null) {
				playerRes.addAllOutCardIds(DehMjHelper.toMajiangIds(nowDisCardIds));
			}
			playerRes.addRecover(player.getSeat() == lastWinSeat ? 1 : 0);
			if (!isHasGangAction(player.getSeat()) && actionSeatMap.containsKey(player.getSeat())
					&& !huConfirmMap.containsKey(player.getSeat())) {
				if (!tempActionMap.containsKey(player.getSeat())) {// 如果已做临时操作
																	// 则不发送前端可做的操作
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
		}
		return res.build();
	}
	
	
	public List<Integer> getLastTwoMj(){
		List<DehMj> mjs = getGenMjs();
		
		return DehMjHelper.toMajiangIds(mjs);
		// mjs;
		
	}

	private List<DehMj> getGenMjs() {
		List<DehMj> mjs = new ArrayList<>();
		int num = getLeftMajiangCount();
		if(num<=2){
			mjs.addAll(this.leftMajiangs);
		}else{
			mjs.addAll(this.leftMajiangs.subList(leftMajiangs.size()-2, leftMajiangs.size()));
		}
		return mjs;
	}
	
	/***
	 * 校验客户端下发牌id
	 * @param demj
	 * @return
	 */
	public DehMj getLastTwoMjCId(DehMj demj){
		List<DehMj> mjs = getGenMjs();
		for(DehMj mj:mjs){
//			if(mj.getVal()==demj.getVal()){
//				return mj;
//			}
			if(mj.getId()==demj.getId()){
				return mj;
			}
		}
		return null;
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
		setGangSeat(0);
		setLeftMajiangs(null);
		setNowDisCardIds(null);
		clearMoGang();
		clearGangDisMajiangs();
		setAskLastMajaingSeat(0);
		setFristLastMajiangSeat(0);
		setMoLastMajiangSeat(0);
		setDisEventAction(0);
		setLastMajiang(null);
		clearTempAction();
		clearShowMjSeat();
		clearMoLastSeat();
		setDealDice(0);
		clearMoTailPai();
		readyTime = 0;
	}

	@Override
	public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {
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
		info.setConfig(objects[1].toString());
		TableDao.getInstance().save(info);
		loadFromDB(info);
		// int birdNum = (int) objects[0];
		// if (birdNum > 2) {
		// birdNum = 2;
		// }
		setBirdNum(2);
		// setIsCalcBanker((int) objects[2]);
		// setCalcBird((int) objects[3]);
		setIsCalcBanker(1);
		setCalcBird(2);
	}

	private Map<Integer, DehMjTempAction> loadTempActionMap(String json) {
		Map<Integer, DehMjTempAction> map = new ConcurrentHashMap<>();
		if (json == null || json.isEmpty())
			return map;
		JSONArray jsonArray = JSONArray.parseArray(json);
		for (Object val : jsonArray) {
			String str = val.toString();
			DehMjTempAction tempAction = new DehMjTempAction();
			tempAction.initData(str);
			map.put(tempAction.getSeat(), tempAction);
		}
		return map;
	}

	private void clearTempAction() {
		tempActionMap.clear();
		changeExtend();
	}

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

	public void coverAddActionSeat(int seat, List<Integer> actionlist) {
		actionSeatMap.put(seat, actionlist);
		addPlayLog(disCardRound + "_" + seat + "_" + DehMjDisAction.action_hasAction + "_"
				+ StringUtil.implode(actionlist));
		saveActionSeatMap();
	}

	public void addActionSeat(int seat, List<Integer> actionlist) {
		if (actionSeatMap.containsKey(seat)) {
			List<Integer> a = actionSeatMap.get(seat);
			DataMapUtil.appendList(a, actionlist);
			addPlayLog(disCardRound + "_" + seat + "_" + DehMjDisAction.action_hasAction + "_" + StringUtil.implode(a));
		} else {
			actionSeatMap.put(seat, actionlist);
			addPlayLog(disCardRound + "_" + seat + "_" + DehMjDisAction.action_hasAction + "_"
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

	/**
	 * 是否有人可胡小胡
	 *
	 * @return
	 */
	public boolean canHuXiaohu() {
		for (List<Integer> list : actionSeatMap.values()) {
			List<Integer> xiaoHuActions = list.subList(6, 14);
			if (xiaoHuActions.contains(1)) {
				return true;
			}
		}
		return false;
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
			nowDisCardIds = DehMjHelper.toMajiang(StringUtil.explodeToIntList(info.getNowDisCardIds()));
		}

		if (!StringUtils.isBlank(info.getLeftPais())) {
			leftMajiangs = DehMjHelper.toMajiang(StringUtil.explodeToIntList(info.getLeftPais()));
		}

	}

	// @Override
	// public void initExtend(String info) {
	// if (StringUtils.isBlank(info)) {
	// return;
	// }
	// JsonWrapper wrapper = new JsonWrapper(info);
	// for (DehMjPlayer player : seatMap.values()) {
	// player.initExtend(wrapper.getString(player.getSeat()));
	// }
	// String huListstr = wrapper.getString(5);
	// if (!StringUtils.isBlank(huListstr)) {
	// huConfirmMap = DataMapUtil.implode(huListstr);
	// }
	// birdNum = wrapper.getInt(6, 0);
	// moMajiangSeat = wrapper.getInt(7, 0);
	// int moGangMajiangId = wrapper.getInt(8, 0);
	// if (moGangMajiangId != 0) {
	// moGang = Majiang.getMajang(moGangMajiangId);
	// }
	// String moGangHu = wrapper.getString(9);
	// if (!StringUtils.isBlank(moGangHu)) {
	// moGangHuList = StringUtil.explodeToIntList(moGangHu);
	// }
	// String gangDisMajiangstr = wrapper.getString(10);
	// if (!StringUtils.isBlank(gangDisMajiangstr)) {
	// gangDisMajiangs = MajiangHelper.explodeMajiang(gangDisMajiangstr, ",");
	// }
	// int gangMajiang = wrapper.getInt(11, 0);
	// if (gangMajiang != 0) {
	// this.gangMajiang = Majiang.getMajang(gangMajiang);
	// }
	//
	// askLastMajaingSeat = wrapper.getInt(12, 0);
	// moLastMajiangSeat = wrapper.getInt(13, 0);
	// int lastMajiangId = wrapper.getInt(14, 0);
	// if (lastMajiangId != 0) {
	// this.lastMajiang = Majiang.getMajang(lastMajiangId);
	// }
	// fristLastMajiangSeat = wrapper.getInt(15, 0);
	// disEventAction = wrapper.getInt(16, 0);
	// isCalcBanker = wrapper.getInt(17, 1);
	// calcBird = wrapper.getInt(18, 1);
	// // disAction = wrapper.getInt(11, 0);
	// // wrapper.putInt(17, isCalcBanker);
	// // wrapper.putInt(18, calcBird);
	//
	// }

	/**
	 * 是否能碰
	 *
	 * @param player
	 * @param majiangs
	 * @param disMajiang
	 * @return
	 */
	private boolean canChi(DehMjPlayer player, List<DehMj> handMajiang, List<DehMj> majiangs, DehMj disMajiang) {
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return false;
		}
		if (player.isAlreadyMoMajiang()) {
			return false;
		}
		List<Integer> pengGangSeatList = getPengGangSeatByActionMap();
		pengGangSeatList.remove((Object) player.getSeat());
		if (!pengGangSeatList.isEmpty()) {
			return false;
		}
		//
		// Majiang playCommand = null;
		// if (nowDisCardIds.size() == 1) {
		// playCommand = nowDisCardIds.get(0);
		//
		// } else {
		// for (int majiangId : gangSeatMap.keySet()) {
		// Map<Integer, List<Integer>> actionMap = gangSeatMap.get(majiangId);
		// List<Integer> action = actionMap.get(player.getSeat());
		// if (action != null) {
		// List<Integer> disActionList =
		// MajiangDisAction.parseToDisActionList(action);
		// if (disActionList.contains(MajiangDisAction.action_chi)) {
		// playCommand = Majiang.getMajang(majiangId);
		// break;
		// }
		//
		// }
		//
		// }
		//
		// }

		if (disMajiang == null) {
			return false;
		}

		if (!handMajiang.containsAll(majiangs)) {
			return false;
		}

		List<DehMj> chi = DehMjTool.checkChi(majiangs, disMajiang);
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
	private boolean canPeng(DehMjPlayer player, List<DehMj> majiangs, int sameCount, DehMj disMajiang) {
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return false;
		}
		if (player.isAlreadyMoMajiang()) {
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
	private boolean canAnGang(DehMjPlayer player, List<DehMj> majiangs, int sameCount, int action) {
		if (sameCount != 4) {
			return false;
		}
		if (player.getSeat() != getNextDisCardSeat() && action != DehMjDisAction.action_buzhang) {
			return false;
		}
		if (player.getSeat() != getNextDisCardSeat() && action != DehMjDisAction.action_buzhang_an) {
			return false;
		}
		return true;
	}

	/**
	 * 检查优先度 如果同时出现一个事件，按出牌座位顺序优先
	 */
	private boolean checkAction(DehMjPlayer player, List<DehMj> cardList, List<Integer> hucards, int action) {
		boolean canAction = checkCanAction(player, action);// 是否优先级最高 能执行操作
		if (canAction == false) {// 不能操作时 存入临时操作
			int seat = player.getSeat();
			tempActionMap.put(seat, new DehMjTempAction(seat, action, cardList, hucards));
			// 玩家都已选择自己的临时操作后 选取优先级最高
			if (tempActionMap.size() == actionSeatMap.size()) {
				int maxAction = Integer.MAX_VALUE;
				int maxSeat = 0;
				Map<Integer, Integer> prioritySeats = new HashMap<>();
				int maxActionSize = 0;
				for (DehMjTempAction temp : tempActionMap.values()) {
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
				DehMjPlayer tempPlayer = seatMap.get(maxSeat);
				List<DehMj> tempCardList = tempActionMap.get(maxSeat).getCardList();
				for (int removeSeat : prioritySeats.keySet()) {
					if (removeSeat != maxSeat) {
						removeActionSeat(removeSeat);
					}
				}
				clearTempAction();
				playCommand(tempPlayer, tempCardList, maxAction);// 系统选取优先级最高操作
			} else {
				if (isCalcOver()) {// 判断是否牌局是否结束
					calcOver();
					return canAction;
				}
			}
		} else {// 能操作 清理所有临时操作
			clearTempAction();
		}
		return canAction;
	}

	/**
	 * 执行可做操作里面优先级最高的玩家操作
	 *
	 * @param player
	 */
	private void refreshTempAction(DehMjPlayer player) {
		tempActionMap.remove(player.getSeat());
		Map<Integer, Integer> prioritySeats = new HashMap<>();
		for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
			int seat = entry.getKey();
			List<Integer> actionList = entry.getValue();
			List<Integer> list = DehMjDisAction.parseToDisActionList(actionList);
			int priorityAction = DehMjDisAction.getMaxPriorityAction(list);
			prioritySeats.put(seat, priorityAction);
		}
		int maxPriorityAction = Integer.MAX_VALUE;
		int maxPrioritySeat = 0;
		boolean isSame = true;
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
		Iterator<DehMjTempAction> iterator = tempActionMap.values().iterator();
		while (iterator.hasNext()) {
			DehMjTempAction tempAction = iterator.next();
			if (tempAction.getSeat() == maxPrioritySeat) {
				int action = tempAction.getAction();
				List<DehMj> tempCardList = tempAction.getCardList();
				DehMjPlayer tempPlayer = seatMap.get(tempAction.getSeat());
				playCommand(tempPlayer, tempCardList, action);// 系统选取优先级最高操作
				iterator.remove();
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
	public boolean checkCanAction(DehMjPlayer player, int action) {
		// 优先度为胡杠补碰吃
		List<Integer> stopActionList = DehMjDisAction.findPriorityAction(action);
		for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
			if (player.getSeat() != entry.getKey()) {
				// 别人
				boolean can = DehMjDisAction.canDisMajiang(stopActionList, entry.getValue());
				if (!can) {
					return false;
				}
				List<Integer> disActionList = DehMjDisAction.parseToDisActionList(entry.getValue());
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
	private boolean canMingGang(DehMjPlayer player, List<DehMj> handMajiang, List<DehMj> majiangs, int sameCount,
			DehMj disMajiang) {
		List<Integer> pengList = DehMjHelper.toMajiangVals(player.getPeng());

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

	public int getBirdNum() {
		return birdNum;
	}

	public void setBirdNum(int birdNum) {
		this.birdNum = birdNum;
		changeExtend();
	}

	public int getBuyPoint() {
		return buyPoint;
	}

	public void setKePiao(int kePiao) {
		this.buyPoint = kePiao;
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

	public void setLastMajiang(DehMj lastMajiang) {
		this.lastMajiang = lastMajiang;
		changeExtend();
	}

	public void setMoLastMajiangSeat(int moLastMajiangSeat) {
		this.moLastMajiangSeat = moLastMajiangSeat;
		changeExtend();
	}

	public void setGangMajiang(DehMj gangMajiang) {
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
	public void setMoGang(DehMj moGang, List<Integer> moGangHuList) {
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

	public void setGangDisMajiangs(List<DehMj> gangDisMajiangs) {
		this.gangDisMajiangs = gangDisMajiangs;
		changeExtend();
	}

	public List<DehMj> getGangDisMajiangs() {
		return gangDisMajiangs;
	}

	/**
	 * 清理杠后摸的牌
	 */
	public void clearGangDisMajiangs() {
		this.gangActedMj = null;
		this.gangMajiang = null;
		this.gangDisMajiangs.clear();
		this.gangDice = -1;
		changeExtend();
	}

	/**
	 * guo 摸杠胡
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

	public ClosingMjInfoRes.Builder sendAccountsMsg(boolean over, boolean selfMo, List<Integer> winList,
			int[] prickBirdMajiangIds, int genNum, boolean isBreak, int bankerSeat, int fangpaoSeat) {
		
		
		// 大结算计算加倍分
		if (over && jiaBei == 1&&playerMap.size()==2) {
			int jiaBeiPoint = 0;
			int loserCount = 0;
			for (DehMjPlayer player : seatMap.values()) {
				if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
					jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
					player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
				} else if (player.getTotalPoint() < 0) {
					loserCount++;
				}
			}
			if (jiaBeiPoint > 0) {
				for (DehMjPlayer player : seatMap.values()) {
					if (player.getTotalPoint() < 0) {
						player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
					}
				}
			}
		}

		//大结算低于below分+belowAdd分
		if(over&&belowAdd>0&&playerMap.size()==2){
			for (DehMjPlayer player : seatMap.values()) {
				int totalPoint = player.getTotalPoint();
				if (totalPoint >-below&&totalPoint<0) {
					player.setTotalPoint(player.getTotalPoint()-belowAdd);
				}else if(totalPoint < below&&totalPoint>0){
					player.setTotalPoint(player.getTotalPoint()+belowAdd);
				}
			}
		}

		
		List<ClosingMjPlayerInfoRes.Builder> list = new ArrayList<>();
		for (DehMjPlayer player : seatMap.values()) {
			ClosingMjPlayerInfoRes.Builder build = null;
			if (over) {
				build = player.buildTotalClosingPlayerInfoRes();
			} else {
				build = player.buildOneClosingPlayerInfoRes();
			}

			build.setBirdPoint(player.getPiaoPoint());
			if (winList != null && winList.contains(player.getSeat())) {
				if (!selfMo) {
					// 不是自摸
					List<Integer> huMjIds = player.getHuMjIds();
					if (huMjIds != null && huMjIds.size() > 0) {
						for (int mjId : huMjIds) {
							if (!build.getHandPaisList().contains(mjId)) {
								build.addHandPais(mjId);
							}
						}
						int isHu = 0;
						if (huMjIds.size() == 2) {
							isHu = huMjIds.get(0) * 1000 + huMjIds.get(1);
						} else {
							isHu = huMjIds.get(0);
						}
						build.setIsHu(isHu);
					} else {
						DehMj huMajiang = nowDisCardIds.get(0);
						if (!build.getHandPaisList().contains(huMajiang.getId())) {
							build.addHandPais(huMajiang.getId());
						}
						build.setIsHu(huMajiang.getId());
					}
				} else {
					List<Integer> huMjIds = player.getHuMjIds();
					if (huMjIds != null && huMjIds.size() > 0) {
						for (int mjId : huMjIds) {
							if (!build.getHandPaisList().contains(mjId)) {
								build.addHandPais(mjId);
							}
						}
						int isHu = 0;
						if (huMjIds.size() == 2) {
							isHu = huMjIds.get(0) * 1000 + huMjIds.get(1);
						} else {
							isHu = huMjIds.get(0);
						}
						build.setIsHu(isHu);
					} else {
						build.setIsHu(player.getLastMoMajiang().getId());
					}
				}
				// build.addAllDahus(player.getDahu());
				build.setTotalFan(player.getDahuFan());
			}
			if (player.getSeat() == fangpaoSeat) {
				build.setFanPao(1);
			}

			if (winList != null && winList.contains(player.getSeat())) {
				// 手上没有剩余的牌放第一位为赢家
				list.add(0, build);
			} else {
				list.add(build);
			}

			// 信用分
			if (isCreditTable()) {
				player.setWinLoseCredit(player.getTotalPoint() * creditDifen);
			}

		}

		ClosingMjInfoRes.Builder res = ClosingMjInfoRes.newBuilder();

		// 信用分计算
		if (isCreditTable()) {
			// 计算信用负分
			calcNegativeCredit();
			long dyjCredit = 0;
			for (DehMjPlayer player : seatMap.values()) {
				if (player.getWinLoseCredit() > dyjCredit) {
					dyjCredit = player.getWinLoseCredit();
				}
			}
			for (ClosingMjPlayerInfoRes.Builder builder : list) {
				DehMjPlayer player = seatMap.get(builder.getSeat());
				calcCommissionCredit(player, dyjCredit);
				builder.setWinLoseCredit(player.getWinLoseCredit());
				builder.setCommissionCredit(player.getCommissionCredit());
			}
		} else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (DehMjPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingMjPlayerInfoRes.Builder builder : list) {
                DehMjPlayer player = seatMap.get(builder.getSeat());
                builder.setWinLoseCredit(player.getWinGold());
            }
        }
        for (ClosingMjPlayerInfoRes.Builder builder : list) {
            res.addClosingPlayers(builder.build());
        }

		// res.addAllClosingPlayers(list);

		// 大结算 前端通过局数显示大结算
		if (bankerSeat > 0) {
			res.setIsBreak(0);
		} else {
			// 解散大结算
			res.setIsBreak(isBreak ? 1 : 0);
		}

		res.setWanfa(getWanFa());
		res.addAllExt(buildAccountsExt(bankerSeat, over ? 1 : 0,genNum));
		
		if (prickBirdMajiangIds != null) {
			res.addAllBird(DataMapUtil.toList(prickBirdMajiangIds));
		}
		// 比赛

		res.addCreditConfig(creditMode); // 0
		res.addCreditConfig(creditJoinLimit); // 1
		res.addCreditConfig(creditDissLimit); // 2
		res.addCreditConfig(creditDifen); // 3
		res.addCreditConfig(creditCommission); // 4
		res.addCreditConfig(creditCommissionMode1); // 5
		res.addCreditConfig(creditCommissionMode2); // 6
		res.addCreditConfig(creditCommissionLimit); // 7

		// res.setCatchBirdSeat(catchBirdSeat);
		res.addAllLeftCards(DehMjHelper.toMajiangIds(leftMajiangs));
		for (DehMjPlayer player : seatMap.values()) {
			player.writeSocket(res.build());
		}
		return res;

	}

	/**
	 * 杠上花和杠上炮
	 *
	 * @return
	 */
	public DehMj getGangHuMajiang(int seat) {
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
		return DehMj.getMajang(majiangId);

	}

	public List<String> buildAccountsExt(int bankerSeat, int over,int genNum) {
		List<String> ext = new ArrayList<>();
		if (isGroupRoom()) {
			ext.add(loadGroupId());
		} else {
			ext.add("0");
		}
		ext.add(id + "");
		ext.add(masterId + "");
		ext.add(TimeUtil.formatTime(TimeUtil.now()));
		ext.add(playType + "");
		ext.add(getMasterName() + "");

		// ext.add(getConifg(0) + "");
		// ext.add(bankerSeat + "");
		// ext.add(calcBird + "");
		// ext.add(gpsWarn + "");
		ext.add(youfeng + "");
		ext.add(baoting + "");
		ext.add(yitiaolong + "");
		ext.add(siguiyi + "");
		ext.add(buyPoint + "");
		ext.add(chajiao + "");
		ext.add(String.valueOf(playedBureau));// 12
		ext.add(String.valueOf(over));// 12
		ext.add(String.valueOf(genNum));// 12
		

		// ext.add(isCalcBanker + "");
		// ext.add(birdNum + "");
		return ext;
	}

	@Override
	public void sendAccountsMsg() {
		ClosingMjInfoRes.Builder builder = sendAccountsMsg(true, false, null, null, 0, true, 0, 0);
		saveLog(true, 0l, builder.build());
	}

	public Class<? extends Player> getPlayerClass() {
		return DehMjPlayer.class;
	}

	@Override
	public int getWanFa() {
		return GameUtil.game_type_dehmj;
	}

	@Override
	public void checkReconnect(Player player) {
		if (super.isAllReady() && getBuyPoint() > 0 && getTableStatus() == DehMjConstants.TABLE_STATUS_PIAO) {
			DehMjPlayer player1 = (DehMjPlayer) player;
			if (player1.getPiaoPoint() < 0) {
				if (getBuyPoint() >=3) {
					player1.setPiaoPoint(getBuyPoint()-2);
					// 推送
					ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_buy_point,
							player1.getSeat(), player1.getPiaoPoint());
					for (Player tableplayer : getSeatMap().values()) {// 推送客户端玩家买点情况
						tableplayer.writeSocket(com.build());
					}
				} else {
					ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_table_status_buy_point,
							getBuyPoint());
					player1.writeSocket(com.build());
				}
				return;
			}
		}
		checkSendGangRes(player);
		if (askLastMajaingSeat != 0) {
			sendAskLastMajiangRes(player.getSeat());
		}
		if (actionSeatMap.isEmpty()) {
			// 没有其他可操作的动作事件
			if (player instanceof DehMjPlayer) {
				DehMjPlayer BsMjPlayer = (DehMjPlayer) player;
				if (BsMjPlayer != null) {
					if (BsMjPlayer.isAlreadyMoMajiang()) {
						// if (!DehMjPlayer.getGang().isEmpty()) {
						// List<DehMj> disMajiangs = new ArrayList<>();
						// disMajiangs.add(DehMjPlayer.getLastMoMajiang());
						// chuPai(DehMjPlayer, disMajiangs, 0);
						// }
					}
				}
			}
		}
		if (isBegin() && player.getSeat() == lastWinSeat &&  !hasBaoTing()) {
			// 如果是起手判断是否还有人可胡小胡 没有的话通知庄家出牌
			DehMjPlayer bankPlayer = seatMap.get(lastWinSeat);
			ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// 推送庄家出牌
			bankPlayer.writeSocket(com.build());
		}

		if (state == table_state.play) {
			DehMjPlayer player1 = (DehMjPlayer) player;
			if (player1.getHandPais() != null && player1.getHandPais().size() > 0) {
				sendTingMsg(player1, true);
			}
		}

	}

	public static void main(String[] args) {
		System.out.println(Math.pow(3, 2));
	}

	@Override
	public boolean consumeCards() {
		return SharedConstants.consumecards;
	}

	@Override
	public void checkAutoPlay() {
		if (System.currentTimeMillis() - lastAutoPlayTime < 100) {
			return;
		}
		// 发起解散不自动打牌
		if (getSendDissTime() > 0) {
			for (DehMjPlayer player : seatMap.values()) {
				if (player.getLastCheckTime() > 0) {
					player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
				}
			}
			return;
		}

		// 有操作时不自动打牌
		if (!getActionSeatMap().isEmpty()) {
			for (DehMjPlayer player : seatMap.values()) {
				if (!player.isChiPengGang() && player.getActionNum(8) >= 10) {
					playCommand(player, new ArrayList<DehMj>(), DehMjDisAction.action_hu);
					return;
				}

			}

			// int leftCount=4;
			// if(getPlayerCount()<4) {
			// leftCount = 20;
			// }
			// if(getLeftMajiangCount()<=leftCount) {
			// for(DehMjPlayer player : seatMap.values()){
			// List<Integer> actionList = actionSeatMap.get(player.getSeat());
			// if(actionList!= null && actionList.size()>0 &&actionList.get(0)
			// == 1) {
			// // 胡
			// playCommand(player, new ArrayList<DehMj>(),
			// DehMjDisAction.action_hu);
			// }
			// }
			// }

		}

		if (isAutoPlay < 1) {
			return;
		}

		
		  if (isAutoPlayOff()) {
	            // 托管关闭
	            for (int seat : seatMap.keySet()) {
	            	DehMjPlayer player = seatMap.get(seat);
	                player.setAutoPlay(false, false);
	                player.setCheckAutoPlay(false);
	            }
	            return;
	        }
		
		if (state == table_state.play) {
			autoPlay();
		} else {
			if (getPlayedBureau() == 0) {
				return;
			}
			readyTime++;
			// for (DehMjPlayer player : seatMap.values()) {
			// if (player.checkAutoPlay(1, false)) {
			// autoReady(player);
			// }
			// }
			// 开了托管的房间，xx秒后自动开始下一局
			for (DehMjPlayer player : seatMap.values()) {
				if (player.getState() != player_state.entry && player.getState() != player_state.over) {
					continue;
				} else {
					if (readyTime >= 5 && player.isAutoPlay()) {
						// 玩家进入托管后，3秒自动准备
						autoReady(player);
					} else if (readyTime > 30) {
						autoReady(player);
					}
				}
			}
		}
	}

	public void sendTingInfo(DehMjPlayer player) {
		sendTingMsg(player, false);
	}

	private void sendTingMsg(DehMjPlayer player, boolean checkBaoting) {
		List<Integer> allMjs = new ArrayList<>(DehMjConstants.baoshan_mjList);

		if (youfeng == 1) {
			allMjs.addAll(DehMjConstants.feng_mjList);
		}

		if (player.isAlreadyMoMajiang()) {
			// if (actionSeatMap.containsKey(player.getSeat())) {
			// return;
			// }
			DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
			List<DehMj> cards = new ArrayList<>(player.getHandMajiang());

			for (DehMj card : player.getHandMajiang()) {
				cards.remove(card);
				List<DehMj> huCards = DehMjTool.getTingMjs(cards, player, this, allMjs);
				cards.add(card);
				if (huCards == null || huCards.size() == 0) {
					continue;
				}
				DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
				ting.setMajiangId(card.getId());
				for (DehMj mj : huCards) {
					ting.addTingMajiangIds(mj.getId());
				}
				tingInfo.addInfo(ting.build());
			}
			if (tingInfo.getInfoCount() > 0) {
				player.writeSocket(tingInfo.build());
			}
		} else {
			List<DehMj> cards = new ArrayList<>(player.getHandMajiang());
			List<DehMj> huCards = DehMjTool.getTingMjs(cards, player, this, allMjs);
			if (huCards == null || huCards.size() == 0) {
//				if (checkBaoting) {
//					if (player.getBaoTingS() == 1) {
//						player.setBaoTingS(0);
//						ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_code_ts_pao_ting,
//								player.getSeat(), player.getBaoTingS());
//						player.writeSocket(com.build());
//					}
//				}
				return;
			}

//			if (checkBaoting) {
//				if (player.getBaoTingS() < 0 && player.getActionNum(9) <= 1) {
//					ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_code_ts_pao_ting,
//							player.getSeat(), player.getBaoTingS());
//					player.writeSocket(com.build());
//				}
//			} else {
				TingPaiRes.Builder ting = TingPaiRes.newBuilder();
				for (DehMj mj : huCards) {
					ting.addMajiangIds(mj.getId());
				}
				player.writeSocket(ting.build());
//			}
		}
	}

	private boolean checkBaoting(DehMjPlayer player) {
		if(getBaoting()!=1||player.getBaoTingS()==0||player.getBaoTingS()==2){
			return false;
		}
		List<Integer> allMjs = new ArrayList<>(DehMjConstants.baoshan_mjList);
		if (youfeng == 1) {
			allMjs.addAll(DehMjConstants.feng_mjList);
		}
		if (player.isAlreadyMoMajiang()) {
			List<DehMj> cards = new ArrayList<>(player.getHandMajiang());
			for (DehMj card : player.getHandMajiang()) {
				cards.remove(card);
				List<DehMj> huCards = DehMjTool.getTingMjs(cards, player, this, allMjs);
				cards.add(card);
				if (huCards == null || huCards.size() == 0) {
					continue;
				}
				if (player.getBaoTingS() == -1) {
					player.setBaoTingS(0);
				}
				return true;

			}

		} else {
			List<DehMj> cards = new ArrayList<>(player.getHandMajiang());
			List<DehMj> huCards = DehMjTool.getTingMjs(cards, player, this, allMjs);
			if (huCards == null || huCards.size() == 0) {
				return false;
			}
			if (player.getBaoTingS() == -1) {
				player.setBaoTingS(0);
			}
			return true;
		}
		return false;
	}

	public void sendTingInfo2(DehMjPlayer player) {
		if (player.isAlreadyMoMajiang()) {
			if (actionSeatMap.containsKey(player.getSeat())) {
				return;
			}
			DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
			List<DehMj> cards = new ArrayList<>(player.getHandMajiang());
			int[] cardArr = HuUtil.toCardArray(cards);
			Map<Integer, List<DehMj>> checked = new HashMap<>();
			for (DehMj card : cards) {
				if (card.isHongzhong()) {
					continue;
				}
				List<DehMj> lackPaiList;
				if (checked.containsKey(card.getVal())) {
					lackPaiList = checked.get(card.getVal());
				} else {
					int cardIndex = HuUtil.getMjIndex(card);
					cardArr[cardIndex] = cardArr[cardIndex] - 1;
					lackPaiList = DehMjTool.getLackList(cardArr, 0, false);
					cardArr[cardIndex] = cardArr[cardIndex] + 1;
					if (lackPaiList.size() > 0) {
						checked.put(card.getVal(), lackPaiList);
					} else {
						continue;
					}
				}

				DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
				ting.setMajiangId(card.getId());
				if (lackPaiList.size() == 1 && null == lackPaiList.get(0)) {
					// 听所有
					ting.addTingMajiangIds(DehMj.mj201.getId());
				} else {
					for (DehMj lackPai : lackPaiList) {
						ting.addTingMajiangIds(lackPai.getId());
					}
					ting.addTingMajiangIds(DehMj.mj201.getId());
				}
				tingInfo.addInfo(ting.build());
			}
			if (tingInfo.getInfoCount() > 0) {
				player.writeSocket(tingInfo.build());
			}
		} else {
			List<DehMj> cards = new ArrayList<>(player.getHandMajiang());
			int hzCount = DehMjTool.dropHongzhong(cards).size();
			int[] cardArr = HuUtil.toCardArray(cards);
			List<DehMj> lackPaiList = DehMjTool.getLackList(cardArr, hzCount, false);
			if (lackPaiList == null || lackPaiList.size() == 0) {
				return;
			}
			TingPaiRes.Builder ting = TingPaiRes.newBuilder();
			if (lackPaiList.size() == 1 && null == lackPaiList.get(0)) {
				// 听所有
				ting.addMajiangIds(DehMj.mj201.getId());
			} else {
				for (DehMj lackPai : lackPaiList) {
					ting.addMajiangIds(lackPai.getId());
				}
				ting.addMajiangIds(DehMj.mj201.getId());
			}
			player.writeSocket(ting.build());
		}
	}

	public boolean IsCalcBankerPoint() {
		return isCalcBanker == 1;
	}

	public void setIsCalcBanker(int isCalcBanker) {
		this.isCalcBanker = isCalcBanker;
		changeExtend();
	}

	public int getCalcBird() {
		return calcBird;
	}

	public void setCalcBird(int calcBird) {
		this.calcBird = calcBird;
		changeExtend();
	}

	@Override
	public void initExtend0(JsonWrapper extend) {
		for (DehMjPlayer player : seatMap.values()) {
			player.initExtend(extend.getString(player.getSeat()));
		}
		String huListstr = extend.getString(5);
		if (!StringUtils.isBlank(huListstr)) {
			huConfirmMap = DataMapUtil.implode(huListstr);
		}
		birdNum = extend.getInt(6, 0);
		moMajiangSeat = extend.getInt(7, 0);
		int moGangMajiangId = extend.getInt(8, 0);
		if (moGangMajiangId != 0) {
			moGang = DehMj.getMajang(moGangMajiangId);
		}
		String moGangHu = extend.getString(9);
		if (!StringUtils.isBlank(moGangHu)) {
			moGangHuList = StringUtil.explodeToIntList(moGangHu);
		}
		String gangDisMajiangstr = extend.getString(10);
		if (!StringUtils.isBlank(gangDisMajiangstr)) {
			gangDisMajiangs = DehMjHelper.explodeMajiang(gangDisMajiangstr, ",");
		}
		int gangMajiang = extend.getInt(11, 0);
		if (gangMajiang != 0) {
			this.gangMajiang = DehMj.getMajang(gangMajiang);
		}

		askLastMajaingSeat = extend.getInt(12, 0);
		moLastMajiangSeat = extend.getInt(13, 0);
		int lastMajiangId = extend.getInt(14, 0);
		if (lastMajiangId != 0) {
			this.lastMajiang = DehMj.getMajang(lastMajiangId);
		}
		fristLastMajiangSeat = extend.getInt(15, 0);
		disEventAction = extend.getInt(16, 0);
		isCalcBanker = extend.getInt(17, 1);
		calcBird = extend.getInt(18, 1);
		buyPoint = extend.getInt(19, 0);
		tempActionMap = loadTempActionMap(extend.getString("tempActions"));

		gpsWarn = extend.getInt(20, 0);
		youfeng = extend.getInt(21, 0);
		yitiaolong = extend.getInt(22, 0);
		siguiyi = extend.getInt(23, 0);
		baoting = extend.getInt(24, 0);
		String showMj = extend.getString(31);
		if (!StringUtils.isBlank(showMj)) {
			showMjSeat = StringUtil.explodeToIntList(showMj);
		}
		maxPlayerCount = extend.getInt(32, 4);
		gangDice = extend.getInt(33, -1);
		String moTailPaiStr = extend.getString(34);
		if (!StringUtils.isBlank(moTailPaiStr)) {
			moTailPai = StringUtil.explodeToIntList(moTailPaiStr);
		}
		String moLastSeatsStr = extend.getString(35);
		if (!StringUtils.isBlank(moLastSeatsStr)) {
			moLastSeats = StringUtil.explodeToIntList(moLastSeatsStr);
		}
		isBegin = extend.getInt(36, 0) == 1;
		dealDice = extend.getInt(37, 0);

		isAutoPlay = extend.getInt(38, 0);
		autoPlayGlob = extend.getInt(39, 0);
		chajiao = extend.getInt(40, 0);
		if (isAutoPlay == 1) {
			isAutoPlay = 60;
		}
		
		 jiaBei = extend.getInt(41, 0);
	        jiaBeiFen = extend.getInt(42, 0);
	        jiaBeiShu = extend.getInt(43, 0);
	        below = extend.getInt(44, 0);
	        belowAdd = extend.getInt(45, 0);
	        daiGen = extend.getInt(46, 0);
	        

	}

	@Override
	public JsonWrapper buildExtend0(JsonWrapper wrapper) {
		// 1-4 玩家座位信息
		for (DehMjPlayer player : seatMap.values()) {
			wrapper.putString(player.getSeat(), player.toExtendStr());
		}
		wrapper.putString(5, DataMapUtil.explode(huConfirmMap));
		wrapper.putInt(6, birdNum);
		wrapper.putInt(7, moMajiangSeat);
		wrapper.putInt(8, moGang != null ? moGang.getId() : 0);
		wrapper.putString(9, StringUtil.implode(moGangHuList, ","));
		wrapper.putString(10, DehMjHelper.implodeMajiang(gangDisMajiangs, ","));
		wrapper.putInt(11, gangMajiang != null ? gangMajiang.getId() : 0);
		wrapper.putInt(12, askLastMajaingSeat);
		wrapper.putInt(13, moLastMajiangSeat);
		wrapper.putInt(14, lastMajiang != null ? lastMajiang.getId() : 0);
		wrapper.putInt(15, fristLastMajiangSeat);
		wrapper.putInt(16, disEventAction);
		wrapper.putInt(17, isCalcBanker);
		wrapper.putInt(18, calcBird);
		wrapper.putInt(19, buyPoint);
		JSONArray tempJsonArray = new JSONArray();
		for (int seat : tempActionMap.keySet()) {
			tempJsonArray.add(tempActionMap.get(seat).buildData());
		}
		wrapper.putString("tempActions", tempJsonArray.toString());

		wrapper.putInt(20, gpsWarn);
		wrapper.putInt(21, youfeng);
		wrapper.putInt(22, yitiaolong);
		wrapper.putInt(23, siguiyi);
		wrapper.putInt(24, baoting);

		wrapper.putString(31, StringUtil.implode(showMjSeat, ","));
		wrapper.putInt(32, maxPlayerCount);
		wrapper.putInt(33, gangDice);
		wrapper.putString(34, StringUtil.implode(moTailPai, ","));
		wrapper.putString(35, StringUtil.implode(moLastSeats, ","));
		wrapper.putInt(36, isBegin ? 1 : 0);
		wrapper.putInt(37, dealDice);
		wrapper.putInt(38, isAutoPlay);
		wrapper.putInt(39, autoPlayGlob);
		wrapper.putInt(40, chajiao);
		
		  wrapper.putInt(41, jiaBei);
	        wrapper.putInt(42, jiaBeiFen);
	        wrapper.putInt(43, jiaBeiShu);
	        wrapper.putInt(44, below);
	        wrapper.putInt(45, belowAdd);
	        wrapper.putInt(46, daiGen);
		

		return wrapper;
	}

	@Override
	public void createTable(Player player, int playType, int bureauCount, List<Integer> params, List<String> strParams,
			Object... objects) throws Exception {
		long id = getCreateTableId(player.getUserId(), playType);
		TableInf info = new TableInf();
		info.setMasterId(player.getUserId());
		info.setRoomId(0);
		info.setPlayType(playType);
		info.setTableId(id);
		info.setTotalBureau(bureauCount);
		info.setPlayBureau(1);
		info.setServerId(GameServerConfig.SERVER_ID);
		info.setCreateTime(new Date());
		info.setDaikaiTableId(daikaiTableId);
		info.setConfig(String.valueOf(0));
		TableDao.getInstance().save(info);
		loadFromDB(info);

		maxPlayerCount = StringUtil.getIntValue(params, 3, 4);// 比赛人数
		payType = StringUtil.getIntValue(params, 2, 1);// 支付方式
		// calcBird = StringUtil.getIntValue(params, 3, 0);
		// birdNum = StringUtil.getIntValue(params, 4, 0);
		gpsWarn = StringUtil.getIntValue(params, 4, 0);
		youfeng = StringUtil.getIntValue(params, 5, 0);
		yitiaolong = StringUtil.getIntValue(params, 6, 1);
		siguiyi = StringUtil.getIntValue(params, 7, 0);
		baoting = StringUtil.getIntValue(params, 8, 0);
		buyPoint = StringUtil.getIntValue(params, 9, 0);
		chajiao = StringUtil.getIntValue(params, 10, 0);

		isAutoPlay = StringUtil.getIntValue(params, 11, 0);

		autoPlayGlob = StringUtil.getIntValue(params, 12, 0);
		
		daiGen =  StringUtil.getIntValue(params, 13, 0);
		
		
		  this.jiaBei = StringUtil.getIntValue(params, 14, 0);
	        this.jiaBeiFen = StringUtil.getIntValue(params, 15, 0);
	        this.jiaBeiShu = StringUtil.getIntValue(params, 16, 0);
		
	        if(maxPlayerCount==2){
	            int belowAdd = StringUtil.getIntValue(params, 17, 0);
	            if(belowAdd<=100&&belowAdd>=0)
	                this.belowAdd=belowAdd;
	            int below = StringUtil.getIntValue(params, 18, 0);
	            if(below<=100&&below>=0){
	                this.below=below;
	                if(belowAdd>0&&below==0)
	                    this.below=10;
	            }
	        }
		

		if (isAutoPlay == 1) {
			isAutoPlay = 60;// 默认20s
		}

		autoPlay = (isAutoPlay > 1);

		playedBureau = 0;

		// getRoomModeMap().put("1", "1"); //可观战（默认）
	}


	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_dehmj);

	public static void loadWanfaTables(Class<? extends BaseTable> cls) {
		for (Integer integer : wanfaList) {
			TableManager.wanfaTableTypesPut(integer, cls);
		}
	}

	/**
	 * 自动出牌
	 */
	public synchronized void autoPlay() {
		if (state != table_state.play) {
			return;
		}
		
		
		if(isBegin()){
			boolean flag = false;
			for (DehMjPlayer robotPlayer : seatMap.values()) {
				if(robotPlayer.getSeat()==lastWinSeat||robotPlayer.getBaoTingS()!=0){
					continue;
				}
				flag = true;
				if (!robotPlayer.checkAutoPlay(0, false)) {
					continue;
				}
				playBaoting(robotPlayer, 2);
			}
			if(flag){
				return;
			}
			
		}
		
		
		if (!actionSeatMap.isEmpty()) {
			List<Integer> huSeatList = getHuSeatByActionMap();
			if (!huSeatList.isEmpty()) {
				// 有胡处理胡
				for (int seat : huSeatList) {
					DehMjPlayer player = seatMap.get(seat);
					if (player == null) {
						continue;
					}
					if (!player.checkAutoPlay(2, false)) {
						continue;
					}
					playCommand(player, new ArrayList<>(), DehMjDisAction.action_hu);
				}
				return;
			} else {
				int action, seat;
				for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
					List<Integer> actList = DehMjDisAction.parseToDisActionList(entry.getValue());
					if (actList == null) {
						continue;
					}
					seat = entry.getKey();
					action = DehMjDisAction.getAutoMaxPriorityAction(actList);
					DehMjPlayer player = seatMap.get(seat);
					if (!player.checkAutoPlay(0, false)) {
						continue;
					}
					boolean chuPai = false;
					if (player.isAlreadyMoMajiang()) {
						chuPai = true;
					}
					if (action == DehMjDisAction.action_peng) {
						if (player.isAutoPlaySelf()) {
							// 自己开启托管直接过
							playCommand(player, new ArrayList<>(), DehMjDisAction.action_pass);
							if (chuPai) {
								autoChuPai(player);
							}
						} else {
							if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
								DehMj mj = nowDisCardIds.get(0);
								List<DehMj> mjList = new ArrayList<>();
								for (DehMj handMj : player.getHandMajiang()) {
									if (handMj.getVal() == mj.getVal()) {
										mjList.add(handMj);
										if (mjList.size() == 2) {
											break;
										}
									}
								}
								playCommand(player, mjList, DehMjDisAction.action_peng);
							}
						}
					} else {
						playCommand(player, new ArrayList<>(), DehMjDisAction.action_pass);
						if (chuPai) {
							autoChuPai(player);
						}
					}
				}
			}
		} else {
			DehMjPlayer player = seatMap.get(nowDisCardSeat);
			if (player == null || !player.checkAutoPlay(0, false)) {
				return;
			}
			
			if(player.getBaoTingS()==0){
					playBaoting(player, 2);
			}
			
			autoChuPai(player);
		}
	}

	public void autoChuPai(DehMjPlayer player) {

		if (!player.isAlreadyMoMajiang()) {
			return;
		}
		List<Integer> handMjIds = new ArrayList<>(player.getHandPais());
		int index = handMjIds.size() - 1;
		int mjId = -1;
		if (moMajiangSeat == player.getSeat()) {
			mjId = handMjIds.get(index);
		} else {
			Collections.sort(handMjIds);
			mjId = handMjIds.get(index);
		}
		if (mjId != -1) {
			List<DehMj> mjList = DehMjHelper.toMajiang(Arrays.asList(mjId));
			playCommand(player, mjList, DehMjDisAction.action_chupai);
		}
	}

	
	public void playBaoting(DehMjPlayer player, int baoting) {
		player.setBaoTingS(baoting);
		ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_bs_Baoting, player.getSeat(), player.getBaoTingS());
		for (Player tableplayer : getSeatMap().values()) {// 推送客户端玩家抛分情况
			tableplayer.writeSocket(com.build());
		}
		checkBegin(player);
		checkMo();
	}
	/**
	 * 是否可以胡小胡
	 *
	 * @param actionIndex
	 *            CSMajiangConstants类定义
	 * @return
	 */
	public boolean canXiaoHu(int actionIndex) {
		// switch (actionIndex) {
		// case DehMjAction.QUEYISE:
		// return queYiSe == 1;
		// default:
		// return false;
		// }
		return false;
	}

	public void logFaPaiTable() {
		StringBuilder sb = new StringBuilder();
		sb.append("DehMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append("faPai");
		sb.append("|").append(playType);
		sb.append("|").append(maxPlayerCount);
		sb.append("|").append(getPayType());
		sb.append("|").append(calcBird);
		sb.append("|").append(birdNum);
		sb.append("|").append(buyPoint);
		sb.append("|").append(youfeng);
		sb.append("|").append(yitiaolong);
		sb.append("|").append(siguiyi);
		sb.append("|").append(baoting);
		sb.append("|").append(chajiao);
		sb.append("|").append(lastWinSeat);
		LogUtil.msg(sb.toString());
	}

	public void logFaPaiPlayer(DehMjPlayer player, List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append("DehMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append("faPai");
		sb.append("|").append(player.getHandMajiang());
		sb.append("|").append(actListToString(actList));
		LogUtil.msg(sb.toString());
	}

	public void logMoMj(DehMjPlayer player, DehMj mj, List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append("DehMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append("moPai");
		sb.append("|").append(getLeftMajiangCount());
		sb.append("|").append(mj);
		sb.append("|").append(actListToString(actList));
//		sb.append("|").append(player.getHandMajiang());
		LogUtil.msg(sb.toString());
	}
	
	
	
	public void logdaiGenMoMj(DehMjPlayer player, DehMj mj,DehMj mj2,List<Integer> gens ) {
		StringBuilder sb = new StringBuilder();
		sb.append("DehMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append("daigenHuan");
		sb.append("|").append(getLeftMajiangCount());
		sb.append("|").append(mj);
		sb.append("|").append(mj2);
		sb.append("|").append(player.getHandMajiang());
		sb.append("|").append(gens);
		LogUtil.msg(sb.toString());
	}
	
	
	public void logdaiGenMsg(DehMjPlayer player,String str ) {
		StringBuilder sb = new StringBuilder();
		sb.append("DehMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append("daigenMsg");
		sb.append("|").append(getLeftMajiangCount());
		sb.append("|").append(str);
		
		LogUtil.msg(sb.toString());
	}
	
	

	public void logChuPaiActList(DehMjPlayer player, DehMj mj, List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append("DehMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append("chuPaiActList");
		sb.append("|").append(mj);
		sb.append("|").append(actListToString(actList));
//		sb.append("|").append(player.getHandMajiang());
		LogUtil.msg(sb.toString());
	}

	public void logAction(DehMjPlayer player, int action, int xiaoHuType, List<DehMj> mjs, List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append("DehMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		String actStr = "unKnown-" + action;
		if (action == DehMjDisAction.action_peng) {
			actStr = "peng";
		} else if (action == DehMjDisAction.action_minggang) {
			actStr = "mingGang";
		} else if (action == DehMjDisAction.action_chupai) {
			actStr = "chuPai";
		} else if (action == DehMjDisAction.action_pass) {
			actStr = "guo";
		} else if (action == DehMjDisAction.action_angang) {
			actStr = "anGang";
		} else if (action == DehMjDisAction.action_chi) {
			actStr = "chi";
		} else if (action == DehMjDisAction.action_buzhang) {
			actStr = "buZhang";
		} else if (action == DehMjDisAction.action_xiaohu) {
			actStr = "xiaoHu";
		} else if (action == DehMjDisAction.action_buzhang_an) {
			actStr = "buZhangAn";
		}
		sb.append("|").append(xiaoHuType);
		sb.append("|").append(actStr);
		sb.append("|").append(mjs);
		sb.append("|").append(actListToString(actList));
		LogUtil.msg(sb.toString());
	}

	public String actListToString(List<Integer> actList) {
		if (actList == null || actList.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < actList.size(); i++) {
			if (actList.get(i) == 1) {
				if (sb.length() > 1) {
					sb.append(",");
				}
				if (i == DehMjAction.HU) {
					sb.append("hu");
				} else if (i == DehMjAction.PENG) {
					sb.append("peng");
				} else if (i == DehMjAction.MINGGANG) {
					sb.append("mingGang");
				} else if (i == DehMjAction.ANGANG) {
					sb.append("anGang");
				} else if (i == DehMjAction.CHI) {
					sb.append("chi");
				} else if (i == DehMjAction.BUZHANG) {
					sb.append("buZhang");
				} else if (i == DehMjAction.QUEYISE) {
					sb.append("queYiSe");
				} else if (i == DehMjAction.BUZHANG_AN) {
					sb.append("buZhangAn");
				}
			}
		}
		sb.append("]");
		System.out.println("操作  ===========" + sb.toString());
		return sb.toString();
	}

	/**
	 * 小胡展示的麻将需要隐藏起来
	 * 
	 * @param player
	 */
	public void processHideMj(DehMjPlayer player) {
		if (showMjSeat.contains(player.getSeat()) && disCardRound != 0) {
			PlayMajiangRes.Builder hideMj = PlayMajiangRes.newBuilder();
			buildPlayRes(hideMj, player, DehMjDisAction.action_hideMj, null);
			broadMsgToAll(hideMj.build());
			showMjSeat.remove(Integer.valueOf(player.getSeat()));
		}
	}

	public void clearShowMjSeat() {
		showMjSeat.clear();
		changeExtend();
	}

	public void addShowMjSeat(int seat, int xiaoHuType) {
		if (xiaoHuType == DehMjAction.QUEYISE) {
			if (!showMjSeat.contains(seat)) {
				showMjSeat.add(seat);
				changeExtend();
			}
		}

	}

	/**
	 * 庄家是否出过牌了
	 * 
	 * @return
	 */
	public boolean isBegin() {
		return isBegin && nowDisCardIds.size() == 0;
	}

	public void setIsBegin(boolean begin) {
		if (isBegin != begin) {
			isBegin = begin;
			changeExtend();
		}
	}

	@Override
	public boolean isAllReady() {
		if (super.isAllReady()) {
			if (getBuyPoint() > 0) {
				setTableStatus(DehMjConstants.TABLE_STATUS_PIAO);
				boolean bReturn = true;
				// 机器人默认处理
				if (this.isTest()) {
					for (DehMjPlayer robotPlayer : seatMap.values()) {
						if (robotPlayer.isRobot()) {
							robotPlayer.setPiaoPoint(1);
						}
					}
				}
				for (DehMjPlayer player : seatMap.values()) {
					if (player.getPiaoPoint() < 0) {
						if (getBuyPoint() >= 3) {
							int point = getBuyPoint();
							point -=2;
							player.setPiaoPoint(point);
							// 推送
							ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_buy_point,
									player.getSeat(), player.getPiaoPoint());
							for (Player tableplayer : getSeatMap().values()) {// 推送客户端玩家买点情况
								tableplayer.writeSocket(com.build());
							}
						} else {
							ComRes.Builder com = SendMsgUtil
									.buildComRes(WebSocketMsgType.res_code_table_status_buy_point, getBuyPoint());
							player.writeSocket(com.build());
							bReturn = false;
						}
					}
				}
				return bReturn;
			} else {
				for (DehMjPlayer player : seatMap.values()) {
					player.setPiaoPoint(0);
				}
				return true;
			}
		}
		return false;
	}

	public void setTableStatus(int tableStatus) {
		this.tableStatus = tableStatus;
	}

	public int getTableStatus() {
		return tableStatus;
	}

	public void addMoTailPai(int gangDice) {
		int leftMjCount = getLeftMajiangCount();
		int startIndex = 0;
		if (moTailPai.contains(0)) {
			int lastIndex = moTailPai.get(0);
			for (int i = 1; i < moTailPai.size(); i++) {
				if (moTailPai.get(i) == lastIndex + 1) {
					lastIndex++;
				} else {
					break;
				}
			}
			startIndex = lastIndex + 1;
		}
		if (gangDice == -1) {
			// 补张，取一张
			for (int i = 0; i < leftMjCount; i++) {
				int nowIndex = i + startIndex;
				if (!moTailPai.contains(nowIndex)) {
					moTailPai.add(nowIndex);
					break;
				}
			}

		} else {
			int duo = gangDice / 10 + gangDice % 10;
			// 开杠打色子，取两张
			for (int i = 0, j = 0; i < leftMjCount; i++) {
				int nowIndex = i + startIndex;
				if (nowIndex % 2 == 1) {
					j++; // 取到第几剁
				}
				if (moTailPai.contains(nowIndex)) {
					if (nowIndex % 2 == 1) {
						duo++;
						leftMjCount = leftMjCount + 2;
					}
				} else {
					if (j == duo) {
						moTailPai.add(nowIndex);
						moTailPai.add(nowIndex - 1);
						break;
					}

				}
			}

		}
		Collections.sort(moTailPai);
		changeExtend();
	}

	/**
	 * 清除摸屁股
	 */
	public void clearMoTailPai() {
		this.moTailPai.clear();
		changeExtend();
	}

	/**
	 * 杠后推送给玩家杠结束
	 */
	public void checkClearGangDisMajiang() {
		List<DehMj> moList = getGangDisMajiangs();
		if (moList != null && moList.size() > 0 && actionSeatMap.isEmpty()) {
			DehMjPlayer player = seatMap.get(getMoMajiangSeat());
			for (DehMjPlayer seatPlayer : seatMap.values()) {
				GangMoMajiangRes.Builder gangbuilder = GangMoMajiangRes.newBuilder();
				gangbuilder.setRemain(getLeftMajiangCount());
				gangbuilder.setGangId(gangMajiang.getId());
				gangbuilder.setUserId(player.getUserId() + "");
				gangbuilder.setName(player.getName() + "");
				gangbuilder.setSeat(player.getSeat());
				gangbuilder.setReconnect(0);
				gangbuilder.setDice(0);
				if (gangActedMj != null) {
					GangPlayMajiangRes.Builder playBuilder = GangPlayMajiangRes.newBuilder();
					playBuilder.setMajiangId(gangActedMj.getId());
					gangbuilder.addGangActs(playBuilder);
				}
				seatPlayer.writeSocket(gangbuilder.build());
			}
			clearGangDisMajiangs();
		}
	}

	public void clearMoLastSeat() {
		moLastSeats.clear();
		changeExtend();
	}

	public void addMoLastSeat(int seat) {
		if (moLastSeats == null) {
			moLastSeats = new ArrayList<>();
		}
		moLastSeats.add(seat);
		changeExtend();
	}

	public void removeMoLastSeat(int seat) {
		int removIndex = -1;
		for (int i = 0; i < moLastSeats.size(); i++) {
			if (moLastSeats.get(i) == seat) {
				removIndex = i;
				break;
			}
		}
		if (removIndex != -1) {
			moLastSeats.remove(removIndex);
		}
		changeExtend();
	}

	/**
	 * 询问玩家措海底
	 * 
	 * @param player
	 * @param state
	 *            1底单玩家摸海底，0通知玩家关闭摸海底界面
	 */
	public void sendMoLast(DehMjPlayer player, int state) {
		ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_asklastmajiang, state);
		player.writeSocket(res.build());
	}

	/**
	 * 是否玩家有小胡
	 * 
	 * @return
	 */
	public boolean hasBaoTing() {
		for (DehMjPlayer seat : seatMap.values()) {
			if (seat.getBaoTingS() == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 是否玩家有小胡
	 * 
	 * @return
	 */
	public boolean hasXiaoHu(List<Integer> actList) {
		if (DehMjAction.getFirstXiaoHu(actList) != -1) {
			return true;
		}
		return false;
	}

	public int getDealDice() {
		return dealDice;
	}

	public void setDealDice(int dealDice) {
		this.dealDice = dealDice;
	}

	public int getChajiao() {
		return chajiao;
	}

	public void setChajiao(int chajiao) {
		this.chajiao = chajiao;
	}

	public int getYoufeng() {
		return youfeng;
	}

	public void setYoufeng(int youfeng) {
		this.youfeng = youfeng;
	}

	public int getYitiaolong() {
		return yitiaolong;
	}

	public void setYitiaolong(int yitiaolong) {
		this.yitiaolong = yitiaolong;
	}

	public int getSiguiyi() {
		return siguiyi;
	}

	public void setSiguiyi(int siguiyi) {
		this.siguiyi = siguiyi;
	}

	public int getBaoting() {
		return baoting;
	}

	public void setBaoting(int baoting) {
		this.baoting = baoting;
	}

	public int getGangSeat() {
		return gangSeat;
	}

	public void setGangSeat(int gangSeat) {
		this.gangSeat = gangSeat;
	}

	public int getIsAutoPlay() {
		return isAutoPlay;
	}
	

	public int getDaiGen() {
		return daiGen;
	}
	
	

	public DehMj getDaiGenGangMj() {
		return daiGenGangMj;
	}

	public void setDaiGenGangMj(DehMj daiGenGangMj) {
		this.daiGenGangMj = daiGenGangMj;
	}

	public String getTableMsg() {
		Map<String, Object> json = new HashMap<>();
		json.put("wanFa", "德宏麻将");
		if (isGroupRoom()) {
			json.put("roomName", getRoomName());
		}
		json.put("playerCount", getPlayerCount());
		json.put("count", getTotalBureau());
		if (isAutoPlay > 0) {
			json.put("autoTime", isAutoPlay);
			if (autoPlayGlob == 1) {
				json.put("autoName", "单局");
			} else {
				json.put("autoName", "整局");
			}
		}
		return JSON.toJSONString(json);
	}

	@Override
	public String getGameName() {
		return "德宏麻将";
	}


    public void logPointError(String type) {
        int sum = 0;
        for (DehMjPlayer seat : seatMap.values()) {
            sum += seat.getTotalPoint() + seat.getLostPoint() + seat.getGangPoint();
        }
        if (sum != 0) {
            StringBuilder sb = new StringBuilder("DehMj");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append("totoPointError").append(type);
            LogUtil.msgLog.info(sb.toString());

            for (DehMjPlayer seat : seatMap.values()) {
                sb = new StringBuilder("DehMj");
                sb.append("|").append(getId());
                sb.append("|").append(getPlayBureau());
                sb.append("|").append(seat.getUserId());
                sb.append("|").append(seat.getSeat());
                sb.append("|").append("point").append(type);
                sb.append("|").append(seat.getTotalPoint());
                sb.append("|").append(seat.getLostPoint());
                sb.append("|").append(seat.getGangPoint());
                LogUtil.msgLog.info(sb.toString());
            }
        }
    }
}
