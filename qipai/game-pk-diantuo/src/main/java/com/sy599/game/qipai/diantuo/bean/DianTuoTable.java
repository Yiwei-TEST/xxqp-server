package com.sy599.game.qipai.diantuo.bean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.CommonUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.DataStatistics;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.dao.DataStatisticsDao;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.TableRes;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayCardRes;
import com.sy599.game.msg.serverPacket.TableRes.ClosingInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.ClosingPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.diantuo.constant.DianTuoConstants;
import com.sy599.game.qipai.diantuo.tool.CardTool;
import com.sy599.game.qipai.diantuo.util.CardType;
import com.sy599.game.qipai.diantuo.util.CardUtils;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class DianTuoTable extends BaseTable {
	public static final String GAME_CODE = "datuo";
	private static final int JSON_TAG = 1;
	/*** 当前牌桌上出的牌 */
	private volatile List<Integer> nowDisCardIds = new ArrayList<>();
	/*** 玩家map */
	private Map<Long, DianTuoPlayer> playerMap = new ConcurrentHashMap<Long, DianTuoPlayer>();
	/*** 座位对应的玩家 */
	private Map<Integer, DianTuoPlayer> seatMap = new ConcurrentHashMap<Integer, DianTuoPlayer>();
	/*** 最大玩家数量 */
	private volatile int maxPlayerCount = 3;

	// private volatile int showCardNumber = 0; // 是否显示剩余牌数量

	public static final int FAPAI_PLAYER_COUNT = 3;// 发牌人数

	private volatile int timeNum = 0;

	private int remove34;

	private int seeTeamCard;

	private int randomTeam;

	private int seeHandCards;

	private int noPlayShun;

	private int noSpeak;
	private int zhengWSK;

	private int feijiSD;

	private int boomNoWang;

	private int fourRfourB;
	private int boomYX;// 炸弹有喜

	private int xifenRate;// 喜分比例
	
	private int firstOJs;// 一游100分解散
	private int ztJsYouXi;//中途解散有喜
	
	
	
	
	

	/** 托管1：单局，2：全局 */
	private int autoPlayGlob;
	private int autoTableCount;

	// 新的一轮，3人为2人pass之后为新的一轮出牌
	private boolean newRound = true;
	// pass累计
	/**
	 * 托管时间
	 */
	private volatile int autoTimeOut = 5 * 24 * 60 * 60 * 1000;
	private volatile int autoTimeOut2 = 5 * 24 * 60 * 60 * 1000;

	// 是否已经发牌
	private int finishFapai = 0;

	// 一轮出的牌都会临时存在这里
	private volatile List<PlayerCard> noPassDisCard = new ArrayList<>();
	// 回放手牌
	private volatile String replayDisCard = "";

	private List<Integer> turnCards = new ArrayList<>();// 一个回合出的牌。

	// private List<Integer> currCards = new ArrayList<>();// 当前最后一个。

	private List<Integer> teamSeat = new ArrayList<>();// 队伍。
	
	
	
	private List<Integer> actionSeats = new ArrayList<>();// 可操作性的座位。

	// private List<Integer> zhuoFen = new ArrayList<>();// 捉的分
	// private List<Integer> turnWinSeats = new ArrayList<>();
	private int banker = 0;// 独战座位

	private int turnFirstSeat = 0;// 当前轮第一个出牌的座位
	private int turnNum;// 回合，每出一轮为1回合
	
	private int tianBoomFen;//

	/** 特殊状态 1 **/
	private int tableStatus = 0;
	// 低于below加分
	private int belowAdd = 0;
	private int below = 0;

	private int tRank;
	
	private int randomCardSeat;

	public String getReplayDisCard() {
		return replayDisCard;
	}

	public void setReplayDisCard(String replayDisCard) {
		this.replayDisCard = replayDisCard;
	}

	@Override
	protected void loadFromDB1(TableInf info) {
		if (!StringUtils.isBlank(info.getNowDisCardIds())) {
			this.nowDisCardIds = StringUtil.explodeToIntList(info.getNowDisCardIds());
		}

		if (!StringUtils.isBlank(info.getHandPai9())) {
			this.teamSeat = StringUtil.explodeToIntList(info.getHandPai9());
		}
		
		if (!StringUtils.isBlank(info.getHandPai10())) {
			this.actionSeats = StringUtil.explodeToIntList(info.getHandPai10());
		}
		// if (!StringUtils.isBlank(info.getHandPai10())) {
		// this.currCards = StringUtil.explodeToIntList(info.getHandPai10());
		// }
		if (!StringUtils.isBlank(info.getOutPai9())) {
			this.turnCards = StringUtil.explodeToIntList(info.getOutPai9());
		}

	}

	public long getId() {
		return id;
	}

	public DianTuoPlayer getPlayer(long id) {
		return playerMap.get(id);
	}

	/**
	 * 一局结束
	 */
	public void calcOver() {
		calcOverEnd(false);
	}

	private void calcOverEnd(boolean dissFlag) {
		int fristSeat = 0;
		boolean win = false;
		for (DianTuoPlayer dp : seatMap.values()) {
			if (dp.getRank() == 1&&!dissFlag) {
				fristSeat = dp.getSeat();
				lastWinSeat = fristSeat;
				break;
			}
		}
		
		boolean isOver = playBureau >= totalBureau;

		//结算 手上炸弹算喜分
		for (DianTuoPlayer dp : seatMap.values()) {
			if(dp.getHandPais().size()<4){
				continue;
			}
			calOverXifen(dp);
		}
		
		
		
		// 选了独战
		if (banker > 0&&fristSeat>0) {
			DianTuoPlayer winPlayer = seatMap.get(fristSeat);
			if (fristSeat == banker) {
				winPlayer.setFaFen(900);
				for (DianTuoPlayer dp : seatMap.values()) {
					if (dp.getSeat() == fristSeat) {
						continue;
					}
					dp.setFaFen(-300);
				}
			} else {
				DianTuoPlayer losePlayer = seatMap.get(banker);
				losePlayer.setFaFen(-900);
				for (DianTuoPlayer dp : seatMap.values()) {
					if (dp.getSeat() == banker) {
						continue;
					}
					dp.setFaFen(300);
				}
			}

			isOver = true;
		} else if(fristSeat>0) {
			
			List<Integer> list = getTeamPlayerSeat(fristSeat);
			int teamSeat = 0;
			DianTuoPlayer tp = null;
			if(maxPlayerCount==2){
				 teamSeat = fristSeat;
				 tp = seatMap.get(fristSeat);
				 for (DianTuoPlayer dp : seatMap.values()) {
						if (dp.getSeat() != fristSeat ) {
							dp.setFaFen(-40);
						}else{
							dp.setFaFen(40);
						}
					}
				 
			}else{
				 teamSeat = list.get(0);
				 tp = seatMap.get(teamSeat);
				
				if (tp.getRank() == 2) {
					for (DianTuoPlayer dp : seatMap.values()) {
						if (dp.getSeat() != fristSeat && dp.getSeat() != teamSeat) {
							dp.setFaFen(-100);
						} else {
							dp.setFaFen(100);
						}
					}
				} else if (tp.getRank() == 3) {
					for (DianTuoPlayer dp : seatMap.values()) {
						if (dp.getSeat() != fristSeat && dp.getSeat() != teamSeat) {
							dp.setFaFen(-50);
						} else {
							dp.setFaFen(50);
						}
					}
				}
			}
			
			
			DianTuoPlayer otherp = null;
			for (DianTuoPlayer dp : seatMap.values()) {
				if (dp.getSeat() != fristSeat && dp.getSeat() != teamSeat) {
					otherp = dp;
					break;
				}
			}

			int fenA = tp.getFaFen() + tp.getGameFen();
			int fenB = otherp.getFaFen() + otherp.getGameFen();

			if (fenA >= fenB) {
				win = true;
			}

		}

		for (DianTuoPlayer dtp : seatMap.values()) {
			dtp.changePoint(dtp.getFaFen() + dtp.getGameFen(), this);
			
			if (totalBureau >= 100) {
				if (dtp.getTotalPoint() >= totalBureau) {
					isOver = true;
				}
			}
		}

		if (autoPlayGlob > 0) {
			// //是否解散
			boolean diss = false;
			if (autoPlayGlob == 1) {
				for (DianTuoPlayer seat : seatMap.values()) {
					if (seat.isAutoPlay()) {
						diss = true;
						break;
					}
				}
			} else if (autoPlayGlob == 3) {
				diss = checkAuto3();
			}

			if (diss || autoPlayDiss) {
				autoPlayDiss = true;
				isOver = true;
			}
		}
		
		
		calcAfter();
		ClosingInfoRes.Builder res = sendAccountsMsg(isOver, false, fristSeat, win);
		saveLog(isOver, 0, res.build());
		// setLastWinSeat(bankPlayer.getSeat());
		if (isOver) {
			calcOver1();
			calcOver2();
			calcOver3();
			diss();
			// for (Player player : seatMap.values()) {
			// player.saveBaseInfo();
			// }
		} else {
			initNext();
			calcOver1();
		}
	}
	
	
	private void calOverXifen(DianTuoPlayer dp){
		if(boomYX!=1){
			return;
		}
		int totalXifen  = getHandXifen(dp);
		addGameActionLog(dp, "calOverXifen|"+dp.getHandPais()+"|"+totalXifen);
		dp.changeAction(0, totalXifen*(maxPlayerCount-1));
		for (DianTuoPlayer p : seatMap.values()) {
			if(p.getSeat()!=dp.getSeat()){
				p.changeAction(0, -totalXifen);
			}
		}
	}

	private int getHandXifen(DianTuoPlayer dp ) {
		if(boomYX!=1){
			return 0;
		}
		int totalXifen = 0;
		Map<Integer, Integer> map = CardUtils.countValue(CardUtils.loadCards(dp.getHandPais()));
		for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
			if (kv.getValue().intValue() >= 6) {
				int fen = kv.getValue().intValue() - 5;
				if(fen<0){
					fen=0;
				}
//				if (kv.getValue().intValue() == 8) {
//					fen += 3;
//				}
				totalXifen+=fen;
			}
			
			
			int addXifen = getOverFRFB(kv.getKey(), dp.getHandPais());
			totalXifen+=addXifen;
		}
		
		int wangCount= CardUtils.loadWangCount(dp.getHandPais());
		if(wangCount==4){
			totalXifen+=tianBoomFen;
		}
		return totalXifen;
	}
	
	private int getOverFRFB(int val,List<Integer> handPais){
		if(fourRfourB!=1){
			return 0;
		}
		int redCount = 0;
		int blackCount = 0;
		for(Integer id: handPais){
			int color=CardUtils.loadCardColor(id);
			int value = CardUtils.loadCardValue(id);
			if(value!=val){
				continue;
			}
			//方片 1 梅花2 洪涛3 黑桃4 5王
			if(color==1||color==3){
				redCount++;
			}else{
				blackCount++;
			}
		
		}
		if(redCount==4){
			return 1;
		}else if(blackCount==4){
			return 1;
		}
		return 0;
		
	}



	private boolean checkAuto3() {
		boolean diss = false;
		// if(autoPlayGlob==3) {
		boolean diss2 = false;
		for (DianTuoPlayer seat : seatMap.values()) {
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

	@Override
	public void calcDataStatistics2() {
		// 俱乐部房间 单大局大赢家、单大局大负豪、总小局数、单大局赢最多、单大局输最多 数据统计
		if (isGroupRoom()) {
			String groupId = loadGroupId();
			int maxPoint = 0;
			int minPoint = 0;
			Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
			// 俱乐部活动总大局数
			calcDataStatistics3(groupId);

			// Long dataDate, String dataCode, String userId, String gameType,
			// String dataType, int dataValue
			for (DianTuoPlayer player : playerMap.values()) {
				// 总小局数
				DataStatistics dataStatistics1 = new DataStatistics(dataDate, "group" + groupId,
						String.valueOf(player.getUserId()), String.valueOf(playType), "xjsCount", playedBureau);
				DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics1, 3);
				// 总大局数
				DataStatistics dataStatistics5 = new DataStatistics(dataDate, "group" + groupId,
						String.valueOf(player.getUserId()), String.valueOf(playType), "djsCount", 1);
				DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5, 3);
				// 总积分
				DataStatistics dataStatistics6 = new DataStatistics(dataDate, "group" + groupId,
						String.valueOf(player.getUserId()), String.valueOf(playType), "zjfCount", player.loadScore());
				DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics6, 3);
				if (player.loadScore() > 0) {
					if (player.loadScore() > maxPoint) {
						maxPoint = player.loadScore();
					}
					// 单大局赢最多
					DataStatistics dataStatistics2 = new DataStatistics(dataDate, "group" + groupId,
							String.valueOf(player.getUserId()), String.valueOf(playType), "winMaxScore",
							player.loadScore());
					DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics2, 4);
				} else if (player.loadScore() < 0) {
					if (player.loadScore() < minPoint) {
						minPoint = player.loadScore();
					}
					// 单大局输最多
					DataStatistics dataStatistics3 = new DataStatistics(dataDate, "group" + groupId,
							String.valueOf(player.getUserId()), String.valueOf(playType), "loseMaxScore",
							player.loadScore());
					DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics3, 5);
				}
			}

			for (DianTuoPlayer player : playerMap.values()) {
				if (maxPoint > 0 && maxPoint == player.loadScore()) {
					// 单大局大赢家
					DataStatistics dataStatistics4 = new DataStatistics(dataDate, "group" + groupId,
							String.valueOf(player.getUserId()), String.valueOf(playType), "dyjCount", 1);
					DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4, 1);
				} else if (minPoint < 0 && minPoint == player.loadScore()) {
					// 单大局大负豪
					DataStatistics dataStatistics5 = new DataStatistics(dataDate, "group" + groupId,
							String.valueOf(player.getUserId()), String.valueOf(playType), "dfhCount", 1);
					DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5, 2);
				}
			}
		}
	}

	public void saveLog(boolean over, long winId, Object resObject) {
		ClosingInfoRes res = (ClosingInfoRes) resObject;
		LogUtil.d_msg("tableId:" + id + " play:" + playBureau + " over:" + res);
		String logRes = JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResLog(res));
		Map<String, Object> map = LogUtil.buildClosingInfoResOtherLog(res);
		map.put("intParams", getIntParams());
		String logOtherRes = JacksonUtil.writeValueAsString(map);
		Date now = TimeUtil.now();

		UserPlaylog userLog = new UserPlaylog();
		userLog.setUserId(creatorId);
		userLog.setLogId(playType);
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

		if (!isGoldRoom()) {
			for (DianTuoPlayer player : playerMap.values()) {
				player.addRecord(logId, playBureau);
			}
		}

		UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
	}

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
				tempMap.put("outPai1", StringUtil.implodeLists(seatMap.get(1).getOutPais()));
			}
			if (tempMap.containsKey("outPai2")) {
				tempMap.put("outPai2", StringUtil.implodeLists(seatMap.get(2).getOutPais()));
			}
			if (tempMap.containsKey("outPai3")) {
				tempMap.put("outPai3", StringUtil.implodeLists(seatMap.get(3).getOutPais()));
			}
			if (tempMap.containsKey("outPai4")) {
				tempMap.put("outPai4", StringUtil.implodeLists(seatMap.get(4).getOutPais()));
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
				tempMap.put("nowDisCardIds", StringUtil.implode(nowDisCardIds, ","));
			}

			if (tempMap.containsKey("handPai9")) {
				tempMap.put("handPai9", StringUtil.implode(teamSeat, ","));
				// tempMap.put("handPai10", StringUtil.implode(currCards, ","));
			}
			
			if (tempMap.containsKey("handPai10")) {
				tempMap.put("handPai10", StringUtil.implode(actionSeats, ","));
			}

			if (tempMap.containsKey("outPai9")) {
				tempMap.put("outPai9", StringUtil.implode(turnCards, ","));
			}

			if (tempMap.containsKey("extend")) {
				tempMap.put("extend", buildExtend());
			}

			// TableDao.getInstance().save(tempMap);
		}
		return tempMap.size() > 0 ? tempMap : null;
	}

	@Override
	public JsonWrapper buildExtend0(JsonWrapper wrapper) {
		// JsonWrapper wrapper = new JsonWrapper("");

		// return wrapper.toString();
		// 1-4 玩家座位信息
		for (DianTuoPlayer player : seatMap.values()) {
			wrapper.putString(player.getSeat(), player.toExtendStr());
		}
		wrapper.putInt(5, maxPlayerCount);
		// wrapper.putString(5,replayDisCard);
		wrapper.putInt(6, autoTimeOut);
		wrapper.putInt(7, autoPlayGlob);
		wrapper.putInt(9, newRound ? 1 : 0);
		wrapper.putInt(10, finishFapai);
		wrapper.putInt(11, belowAdd);
		wrapper.putInt(12, below);

		wrapper.putInt(15, banker);
		wrapper.putInt(16, turnFirstSeat);
		wrapper.putInt(18, turnNum);
		wrapper.putInt(19, tableStatus);

		wrapper.putInt(20, remove34);
		wrapper.putInt(21, seeTeamCard);
		wrapper.putInt(22, randomTeam);
		wrapper.putInt(23, seeHandCards);
		wrapper.putInt(24, fourRfourB);
		wrapper.putInt(25, noPlayShun);
		wrapper.putInt(26, noSpeak);
		wrapper.putInt(27, zhengWSK);

		wrapper.putInt(28, feijiSD);
		wrapper.putInt(29, boomNoWang);

		wrapper.putInt(30, tRank);

		wrapper.putInt(31, boomYX);

		wrapper.putInt(32, xifenRate);
		wrapper.putInt(33, tianBoomFen);
		wrapper.putInt(34, randomCardSeat);
		
		
		wrapper.putInt(35, firstOJs);
		wrapper.putInt(36, ztJsYouXi);
		
		return wrapper;
	}

	protected String buildPlayersInfo() {
		StringBuilder sb = new StringBuilder();
		for (DianTuoPlayer pdkPlayer : playerMap.values()) {
			sb.append(pdkPlayer.toInfoStr()).append(";");
		}
		// playerInfos = sb.toString();
		return sb.toString();
	}

	public void changePlayers() {
		dbParamMap.put("players", JSON_TAG);
	}

	public void changeCards(int seat) {
		dbParamMap.put("outPai" + seat, JSON_TAG);
		dbParamMap.put("handPai" + seat, JSON_TAG);
	}

	/**
	 * 开始发牌
	 */
	public void fapai() {
		playLog = "";
		synchronized (this) {
			changeTableState(table_state.play);
			timeNum = 0;
			List<List<Integer>> list;

			list = CardTool.fapai(4, isRemove34(), zp);

			int i = 0;
			for (DianTuoPlayer player : playerMap.values()) {
				player.changeState(player_state.play);
				player.dealHandPais(list.get(i), this);
				i++;

				if (!player.isAutoPlay()) {
					player.setAutoPlay(false, this);
					player.setLastOperateTime(System.currentTimeMillis());
				}

				StringBuilder sb = new StringBuilder("Diantuo");
				sb.append("|").append(getId());
				sb.append("|").append(getPlayBureau());
				sb.append("|").append(player.getUserId());
				sb.append("|").append(player.getSeat());
				sb.append("|").append(player.getName());
				sb.append("|").append(player.isAutoPlay() ? 1 : 0);
				sb.append("|").append("fapai");
				sb.append("|").append(player.getHandPais());
				LogUtil.msgLog.info(sb.toString());
			}

			// dipai = ;
		}
		finishFapai = 1;
	}

	@Override
	public int getNextDisCardSeat() {
		if (disCardSeat == 0) {
			return banker;
		}
		return calcNextSeat(disCardSeat);
	}

	/**
	 * 计算seat右边的座位
	 *
	 * @param seat
	 * @return
	 */
	public int calcNextSeat(int seat) {
		int nextSeat = seat + 1 > maxPlayerCount ? 1 : seat + 1;
		return nextSeat;
	}

	public DianTuoPlayer getPlayerBySeat(int seat) {
		// int next = seat >= maxPlayerCount ? 1 : seat + 1;
		return seatMap.get(seat);

	}

	/**
	 * 获取下家
	 * 
	 * @param seat
	 * @return
	 */
	private DianTuoPlayer getNextPlayerBySeat(int seat) {
		int next = seat >= maxPlayerCount ? 1 : seat + 1;
		return seatMap.get(next);

	}

	public Map<Integer, DianTuoPlayer> getSeatMap() {
		//Object o = seatMap;
		return seatMap;
	}

	@Override
	public CreateTableRes buildCreateTableRes(long userId, boolean isrecover, boolean isLastReady) {
		CreateTableRes.Builder res = CreateTableRes.newBuilder();
		buildCreateTableRes0(res);
		synchronized (this) {
			res.setNowBurCount(getPlayBureau());
			res.setTotalBurCount(getTotalBureau());
			res.setGotyeRoomId(gotyeRoomId + "");
			res.setTableId(getId() + "");
			res.setWanfa(playType);
			List<PlayerInTableRes> players = new ArrayList<>();
			
		
			for (DianTuoPlayer player : playerMap.values()) {
				PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(userId, isrecover);
				if (playerRes == null) {
					continue;
				}
				
				
				if (player.getUserId() == userId) {
					// 如果是自己重连能看到手牌
					playerRes.addAllHandCardIds(player.getHandPais());
				} else {
					// 如果是别人重连，轮到出牌人出牌时要不起可以去掉
				}

				if (player.getSeat() == disCardSeat && nowDisCardIds != null && nowDisCardIds.size() > 0) {
					// playerRes.addAllOutCardIds(nowDisCardIds);
					// playerRes.addRecover(cardType);
				}

				if (player.getHandPais().isEmpty()) {
					List<Integer> teamSeats = getTeamPlayerSeat(player.getSeat());
					if (!teamSeats.isEmpty() && seeTeamCard == 1) {
						DianTuoPlayer tp = seatMap.get(teamSeats.get(0));
						playerRes.addAllMoldIds(tp.getHandPais());

					}
				}

				players.add(playerRes.build());
			}
			res.addAllPlayers(players);
			// int nextSeat = getNextDisCardSeat();

			// if(getTableStatus() == DianTuoConstants.TABLE_STATUS_JIAOFEN) {
			// nextSeat = getNextActionSeat();
			// }
			if (nowDisCardSeat != 0) {
				res.setNextSeat(nowDisCardSeat);
			}

			// 桌状态 1叫分2选主3埋牌
			res.setRemain(getTableStatus());
			res.setRenshu(this.maxPlayerCount);

			res.addExt(this.payType);// 0支付方式
			res.addExt(banker);// 1独战的座位号

			res.addExt(CommonUtil.isPureNumber(modeId) ? Integer.parseInt(modeId) : 0);// 2
			int ratio;
			int pay;

			ratio = 1;
			pay = consumeCards() ? loadPayConfig(payType) : 0;

			List<Integer> scoreCards = CardUtils.getScoreCards(getTurnCards());
			if (scoreCards.size() > 0) {
				// res.addAllScoreCard(scoreCards);
				int totalScore = CardUtils.loadCardScore(scoreCards);
				res.addExt(totalScore);// 6
			} else {
				res.addExt(0);// 6
			}

			res.addExt(ratio);// 6
			res.addExt(pay);// 7
			res.addExt(lastWinSeat);// 8

			res.addExtStr(String.valueOf(matchId));// 0
			res.addExtStr(cardMarkerToJSON());// 1
			res.addTimeOut(autoPlay ? autoTimeOut : 0);
			// if (autoPlay) {
			// if (disCardRound == 0) {
			// res.addTimeOut((autoTimeOut + 5000));
			// } else {
			// res.addTimeOut(autoTimeOut);
			// }
			// } else {
			// res.addTimeOut(0);
			// }

			res.addExt(playedBureau);// 11
			res.addExt(disCardRound);// 12
			res.addExt(creditMode); // 14
			res.addExt(creditCommissionMode1);// 19
			res.addExt(creditCommissionMode2);// 20
			res.addExt(autoPlay ? 1 : 0);// 21
			res.addExt(tableStatus);// 25
		}

		return res.build();
	}

	public int getOnTablePlayerNum() {
		int num = 0;
		for (DianTuoPlayer player : seatMap.values()) {
			if (player.getIsLeave() == 0) {
				num++;
			}
		}
		return num;
	}

	/**
	 * 出牌
	 *
	 * @param player
	 * @param cards
	 */
	public void disCards(DianTuoPlayer player, int action, List<Integer> cards) {

		List<Integer> chuPaiCards = new ArrayList<Integer>();
		if(!cards.isEmpty()){
			chuPaiCards.addAll(cards);
		}
		int code = 0;
		if (action == 0) {
			code = chupai(player, action, cards);
			if (code < 0) {
				return;
			}
			setTurnFirstSeat(player.getSeat());
			//其他该出牌的玩家座位
			List<Integer> chuPiaSeat = new ArrayList<Integer>();
			for (DianTuoPlayer p : seatMap.values()) {
				if (p.getSeat()==player.getSeat()|| p.getHandPais().isEmpty()) {
					continue;
				}
				chuPiaSeat.add(p.getSeat());
			}
			setActionSeats(chuPiaSeat);
		}else {
			removeActionSeat(player.getSeat());
		}
		player.setActionS(action);
		setDisCardSeat(player.getSeat());

		// 构建出牌消息
		PlayCardRes.Builder res = PlayCardRes.newBuilder() ;
		res.setIsClearDesk(0);
		res.setIsLet(code);
		res.setCardType(action);
		boolean isOver = false;
		int guos = 0;

		List<Integer> scoreCards = CardUtils.getScoreCards(getTurnCards());
		int totalScore = 0;
		if (scoreCards.size() > 0) {
			totalScore = CardUtils.loadCardScore(scoreCards);
		}
		res.setCurScore(totalScore);
		int seeTeamHand = 0;
		boolean turnOver = false;
		if (action == 0 && player.getHandPais().isEmpty()) {
			rankIncre();
			player.setRank(tRank);
			res.setIsBt(tRank);
			//||maxPlayerCount==2
			if(banker>0){
				turnOver = true;
				isOver = true;
			}else {
				List<Integer> teamSeats = getTeamPlayerSeat(player.getSeat());
				if (!teamSeats.isEmpty() && seeTeamCard == 1) {
					seeTeamHand = teamSeats.get(0);
				}
			}
		}
		
		
		if(!turnOver){
//			int winSeat = 0;
			
//			
//			if()
//			
//			boolean meiChu = false;
//			for (DianTuoPlayer p : seatMap.values()) {
//				if (p.getActionS() > 0 || p.getHandPais().isEmpty()) {
//					guos += 1;
//					continue;
//				}
//				if (p.getActionS() == -1) {
//					// 没出牌
//					meiChu = true;
//					continue;
//				}
//				winSeat = p.getSeat();
//			}
//			if (winSeat == 0) {// 没牌的那个人赢
//				DianTuoPlayer winPlayer = getPlayerByRank(tRank);
//				if (winPlayer != null) {
//					winSeat = winPlayer.getSeat();
//				}
//			}

			if (getActionSeats().isEmpty()|| checkTiqianOver(player, action, guos)) {// 一轮打完
				turnOver = true;
				DianTuoPlayer winPlayer = seatMap.get(getTurnFirstSeat());
				isOver = turnOver(res, winPlayer);
			} else {
				setNowDisCardSeat(getNextPlaySeat(player.getSeat()));
			}
		}

		addPlayLog(addSandhPlayLog2(player.getSeat(), action, chuPaiCards, turnOver, totalScore, scoreCards,
				getNowDisCardSeat(),code));

		addGameActionLog(player, "chupai|" + action + "|" + chuPaiCards+"|"+getNowDisCardSeat());
		// if (cards != null) {
		// noPassDisCard.add(new PlayerCard(player.getName(), cards));
		// }
		res.addAllCardIds(cards);
		res.setNextSeat(getNowDisCardSeat());
		res.setUserId(player.getUserId() + "");
		res.setSeat(player.getSeat());
		res.setIsPlay(2);
		setReplayDisCard();
		for (DianTuoPlayer p : seatMap.values()) {
			p.writeSocket(res.build());
		}

		if (seeTeamHand > 0) {
			DianTuoPlayer tp = seatMap.get(seeTeamHand);
			ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.DUIYOU_HAND, tp.getHandPais());
			player.writeSocket(builder.build());
		}

		if (cards.size() == 1) {
			checkDanzhang();
		}

		if (isOver) {
			state = table_state.over;
		}

	}

	private boolean checkTiqianOver(DianTuoPlayer player, int action, int guos) {
		if (checkFirstSecondTeam()) {
			DianTuoPlayer secondP = getPlayerByRank(tRank);
			if ((action == 0 && player.getSeat() != secondP.getSeat()) || (action > 0 && guos == maxPlayerCount)) {
				return true;
			}
		}
		return false;
	}

	/***
	 * if(checkTiqianOver())检查1 2名是不是同一队的
	 * 
	 * @return
	 */
	private boolean checkFirstSecondTeam() {
		if (tRank == 0) {
			return false;
		}
		int firstSeat = 0;
		for (DianTuoPlayer splayer : seatMap.values()) {
			if (splayer.getRank() == 1) {
				firstSeat = splayer.getSeat();
				break;
			}
		}
		if (firstSeat == 0) {
			return false;
		}
		List<Integer> list = getTeamPlayerSeat(firstSeat);
		if(list.isEmpty()){
			return false;
		}

		DianTuoPlayer pl = seatMap.get(list.get(0));
		if (pl == null || pl.getRank() != 2) {
			return false;
		}

		return true;

	}

	private void checkDanzhang() {
		JSONArray jarr = new JSONArray();
		for (DianTuoPlayer splayer : seatMap.values()) {
			if (splayer.getHandPais().isEmpty()) {// || splayer.getActionS() >= 0
				continue;
			}
			List<Integer> wangCards = new ArrayList<Integer>();
			for (Integer id : splayer.getHandPais()) {
				if (id == 501 || id == 502) {
					wangCards.add(id);
				}
			}
			if (wangCards.isEmpty()) {
				continue;
			}
			JSONObject json = new JSONObject();
			json.put("seat", splayer.getSeat());
			json.put("cards", wangCards);
			jarr.add(json);
		}

		if (jarr.size() == 0) {
			return;
		}
		ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.BAOWANG_HAND, jarr.toJSONString());
		for (DianTuoPlayer p : seatMap.values()) {
			p.writeSocket(builder.build());
		}
	}

	private int chupai(DianTuoPlayer player, int action, List<Integer> cards) {

		List<Integer> copy = new ArrayList<>(player.getHandPais());
		copy.removeAll(cards);
		// Collections.sort(cards);
		// int nextSeat = getNextDisCardSeat();
		CardType ct = CardTool.getCardType(cards, copy.isEmpty()&&feijiSD==1, true,boomNoWang==1);
		if (ct.getType() == 0) {
			player.writeErrMsg("出牌不符合规则。");
			return -1;
		}
		if (getNowDisCardIds().size() > 0) {

			if (!CardTool.canChuPai(getNowDisCardIds(), ct,boomNoWang==1,zhengWSK==1,feijiSD==1)) {
				player.writeErrMsg("出牌不符合规则。");
				return -1;
			}
		}
		
		player.addOutPais(cards, this);
		cleanActionState(player.getSeat());
		int xifen = checkXifen(player, ct);
		
		if (ct.getType() == CardType.BOOM || ct.getType() == CardType.TIAN_BOOM) {
			player.changeAction(1, 1);
			xifen +=checkFourRb(ct);
			
		} else if (ct.getType() == CardType.WU_SHI_K) {
			player.changeAction(2, 1);
		}
		
		if (xifen > 0) {
			player.changeAction(0, xifen*(maxPlayerCount-1));
			for (DianTuoPlayer p : seatMap.values()) {
				if(p.getSeat()!=player.getSeat()){
					p.changeAction(0, -xifen);
				}
			}
		}
		addTurnCards(cards, false);
		setNowDisCardIds(cards);
		return xifen*(maxPlayerCount-1);
	}

	private int checkFourRb(CardType ct) {
		int xifen=0;
		if(fourRfourB==1){
			int redCount =0;
			int blackCount =0;
			for(Integer id: ct.getCardIds()){
				int color =  CardUtils.loadCardColor(id);
				if(color==5){
					continue;
				}
				//方片 1 梅花2 洪涛3 黑桃4 5王
				if(color==1||color==3){
					redCount++;
				}else{
					blackCount++;
				}
				
				
			}
			if(redCount==4){
				xifen+=1;
			}
			if(blackCount==4){
				xifen+=1;
			}
		}
		return xifen;
	}

	private int checkXifen(DianTuoPlayer player, CardType ct) {
		if(boomYX==0){
			return 0;
		}
		int xifen = 0;
		if (ct.getType() == CardType.BOOM) {
			if (ct.getVal2() > 5) {
				int wangCount = 0;
				for (Integer id : ct.getCardIds()) {
					int val = CardUtils.loadCardValue(id);
					if (val == 1 || val == 2) {
						wangCount += 1;
					}
				}
				int fen = ct.getVal2() - 5 - wangCount;
				if(fen<0){
					fen=0;
				}
				
//				if (ct.getVal2() == 8&&wangCount==0) {
//					fen += 2;
//				}
				
				
				
				
				xifen = fen;
			}
		} else if (ct.getType() == CardType.TIAN_BOOM) {
			xifen = tianBoomFen;
			
		}
		return xifen;
	}

	private boolean turnOver(PlayCardRes.Builder res, DianTuoPlayer winPlayer) {
		List<Integer> scoreCards = CardUtils.getScoreCards(getTurnCards());
		if (scoreCards.size() > 0) {
			res.addAllScoreCard(scoreCards);
			winPlayer.addChiFenCards(scoreCards);
			int totalScore = CardUtils.loadCardScore(scoreCards);
			winPlayer.addGameFen(totalScore);
			List<Integer> teamSeats = getTeamPlayerSeat(winPlayer.getSeat());
			if (!teamSeats.isEmpty()) {
				for (Integer seat : teamSeats) {
					DianTuoPlayer tp = seatMap.get(seat);
					if (tp != null) {
						tp.addGameFen(totalScore);
					}
				}
			}
			res.setCurScore(totalScore);
		} else {
			res.setCurScore(0);
		}
		res.addAllScoreCard(scoreCards);
		res.setIsClearDesk(1);
		int nextSeat = winPlayer.getSeat();
		if (winPlayer.getHandPais().isEmpty()) {
			// tRank +=1;
			// winPlayer.setRank(tRank);
			// res.setIsBt(tRank);
			if(banker==0){
				List<Integer> sList = getTeamPlayerSeat(nextSeat);
				if(!sList.isEmpty()){
					nextSeat = sList.get(0);
				}
			}
		}

		// addPlayLog(addSandhPlayLog(winPlayer.getSeat(),
		// DianTuoConstants.ZHUO_FEN, null,false,0,scoreCards,nextSeat));
		setNowDisCardSeat(nextSeat);
		cleanActionState(0);
		addTurnCards(null, true);
		setNowDisCardIds(null);

		return checkOverGameFen();
	}

	private boolean checkOverGameFen() {

		// 独战
		if (tRank == 1 && banker > 0) {
			DianTuoPlayer first = getPlayerByRank(tRank);
			int totalScore = 0;
			if (first.getSeat() == banker) {
				for (DianTuoPlayer p : seatMap.values()) {
					if (p.getRank() == 0) {
						rankIncre();
						p.setRank(tRank);
						List<Integer> handSC = CardUtils.getScoreCards(p.getHandPais());
						if (!handSC.isEmpty()) {
							totalScore += CardUtils.loadCardScore(handSC);
						}
					}
				}
				first.addGameFen(totalScore);
				//addPlayerGameFen(first);

			} else {
				for (DianTuoPlayer p : seatMap.values()) {
					if (p.getRank() == 0) {

						if (p.getSeat() == banker) {
							p.setRank(4);
						} else {
							rankIncre();
							p.setRank(tRank);
						}
						List<Integer> handSC = CardUtils.getScoreCards(p.getHandPais());
						if (!handSC.isEmpty()) {
							totalScore += CardUtils.loadCardScore(handSC);
						}
					}
				}
				for (DianTuoPlayer p : seatMap.values()) {
					if (p.getRank() < 4) {
						p.addGameFen(totalScore);
					}
				}
			}
			return true;

		} else if (tRank == 2) {
			// 看第一名是不是队友哦
			if (checkFirstSecondTeam()) {
				// 三四名的分数
				int totalScore = 0;
				for (DianTuoPlayer p : seatMap.values()) {
					if (p.getRank() == 0) {
						rankIncre();
						p.setRank(tRank);
						List<Integer> handSC = CardUtils.getScoreCards(p.getHandPais());
						if (!handSC.isEmpty()) {
							totalScore += CardUtils.loadCardScore(handSC);
						}
					}
				}
				for (DianTuoPlayer p : seatMap.values()) {
					if (p.getRank() == 1 || p.getRank() == 2) {
						p.addGameFen(totalScore);
					}
				}
				return true;
			}
		} else if (tRank == 3) {
			// 要结束了
			rankIncre();
			int totalScore = 0;
			DianTuoPlayer first = getPlayerByRank(1);
			for (DianTuoPlayer p : seatMap.values()) {
				if (p.getRank() == 0) {
					p.setRank(tRank);
					List<Integer> handSC = CardUtils.getScoreCards(p.getHandPais());
					if (!handSC.isEmpty()) {
						totalScore = CardUtils.loadCardScore(handSC);
						List<Integer> teamList = getTeamPlayerSeat(p.getSeat());
						if (teamList.get(0) == first.getSeat()) {
							p.addGameFen(totalScore);
						} else {
							DianTuoPlayer thirdPlayer = getPlayerByRank(3);
							thirdPlayer.addGameFen(totalScore);
						}
					}
					break;
				}
			}
			first.addGameFen(totalScore);
			return true;
		} else if (tRank == 4) {
			return true;
		}
		
		if(maxPlayerCount==2&& tRank >= 1){
			DianTuoPlayer first = getPlayerByRank(tRank);
			addPlayerGameFen(first);
			return true;
		}

		return false;
	}

	private void addPlayerGameFen(DianTuoPlayer first) {
		int totalScore = 0;
//		if (first.getSeat() == banker) {
			for (DianTuoPlayer p : seatMap.values()) {
				if (p.getRank() == 0) {
					rankIncre();
					p.setRank(tRank);
				}
				List<Integer> handSC = CardUtils.getScoreCards(p.getHandPais());
				if (!handSC.isEmpty()) {
					totalScore += CardUtils.loadCardScore(handSC);
				}
			}
			first.addGameFen(totalScore);
//		}
	}

	private int getNextPlaySeat(int nextSeat) {
		for (int i = 0; i < maxPlayerCount - 1; i++) {
			nextSeat += 1;
			if (nextSeat > maxPlayerCount) {
				nextSeat = 1;
			}
			DianTuoPlayer nextPlayer = seatMap.get(nextSeat);
			if (nextPlayer.getRank() > 0) {
				continue;
			}
			break;
		}
		return nextSeat;
	}

	private DianTuoPlayer getPlayerByRank(int rank) {
		DianTuoPlayer player = null;
		for (DianTuoPlayer p : seatMap.values()) {
			if (p.getRank() == rank) {
				player = p;
			}
		}
		return player;
	}

	/**
	 * 
	 * @param disSeat
	 *            不清位置
	 */
	private void cleanActionState(int disSeat) {
		for (DianTuoPlayer p : seatMap.values()) {
			if (p.getSeat() == disSeat) {
				continue;
			}
			if (disSeat == 0) {
				p.setActionS(-1);
			} else {
				if (p.getActionS() > 0) {
					p.setActionS(0);
				}
			}
		}
	}

	public void setReplayDisCard() {
		List<PlayerCard> cards = new ArrayList<>();
		int size = noPassDisCard.size();
		for (int i = 0; i < 3 && i < size; i++) {
			cards.add(noPassDisCard.get(size - 1 - i));
		}
		setReplayDisCard(cards.toString());
		noPassDisCard.clear();
	}

	/**
	 * 打牌
	 *
	 * @param player
	 * @param cards
	 */
	public void playCommand(DianTuoPlayer player, int action, List<Integer> cards) {
		synchronized (this) {
			if (state != table_state.play) {
				return;
			}
			// 出牌阶段
			if (getTableStatus() != DianTuoConstants.TABLE_STATUS_PLAY) {
				return;
			}

			if (!containCards(player.getHandPais(), cards)) {
				return;
			}
			if (player.getHandPais().isEmpty()) {
				return;
			}

			// StringBuilder sb = new StringBuilder("Sandh");
			// sb.append("|").append(getId());
			// sb.append("|").append(getPlayBureau());
			// sb.append("|").append(player.getUserId());
			// sb.append("|").append(player.getSeat());
			// sb.append("|").append(player.isAutoPlay() ? 1 : 0);
			// sb.append("|").append("chuPai");
			// sb.append("|").append(cards);
			// LogUtil.msgLog.info(sb.toString());
			// if (cards != null && cards.size() > 0) {
			changeDisCardRound(1);
			// 出牌了
			disCards(player, action, cards);
			// } else {
			// if (disCardRound > 0) {
			// changeDisCardRound(1);
			// }
			// }
			setLastActionTime(TimeUtil.currentTimeMillis());
			if (isOver()) {
				calcOver();
			}

			// else {
			// int nextSeat = calcNextSeat(player.getSeat());
			// DianTuoPlayer nextPlayer = seatMap.get(nextSeat);
			// if (!nextPlayer.isRobot()) {
			// nextPlayer.setNextAutoDisCardTime(TimeUtil.currentTimeMillis() +
			// autoTimeOut);
			// }
			// }
		}
	}
	
	
	
	private List<Integer> getAutoPlayCards(List<Integer> preTurnCards,List<Integer> handPais){
		
		CardType proCT = CardTool.getCardType(preTurnCards,true,true,boomNoWang==1);
		
//		HashMap<Integer, Integer> map = CardTool.getCardMaps(handPais);
//		
//		List<Integer> res = new ArrayList<>();
//		int cardId = proCT.getCardIds().get(0);
//		int cardVal = CardUtils.loadCardValue(cardId);
//		switch (proCT.getType()) {
//		case CardType.DAN:
//			 for(Integer id: handPais){
//				int cardVal2 = CardUtils.loadCardValue(id);
//				if(cardVal2>cardVal){	
//					res.add(id);
//					break;
//				}
//			}
//			break;
//		case CardType.SHUNZI:
//			List<CardType> cts = CardTool.getWsk(handPais, map);
//			if(cts!=null){
//				CardType wsk = cts.get(cts.size()-1);
//				res.addAll(wsk.getCardIds());
//			}else{
//				List<Integer>  boom = CardTool.getBoom(handPais, 0, 4, map);
//				if(boom!=null){
//					res.addAll(boom);
//				}
//			}
//			break;
//		case CardType.DUI:
//			for(Map.Entry<Integer, Integer> entry: map.entrySet()){
//				int cout = entry.getValue();
//				int val= entry.getKey();
//				if(val>cardVal){
//					res.addAll(getHandCardType(handPais, CardType.DUI, val, cout));
//				}
//			}
//			
//			break;
//		case CardType.FEI_JI:
//
//			break;
//		case CardType.LIAN_DUI:
//
//			break;
//
//		case CardType.TIAN_BOOM:
//
//			break;
//
//		case CardType.WU_SHI_K:
//
//			break;
//
//		default:
//			break;
//		}
//		
		return null;
		
	}
	
	
	
	private List<Integer> getHandCardType(List<Integer> handPais,int type,int val,int count){
		List<Integer> res = new ArrayList<>();
		int num=0;
		for(Integer id: handPais){
			int cardVal2 = CardUtils.loadCardValue(id);
			if(cardVal2==val){
				res.add(id);
				num++;
			}
			if(num==count){
				break;
			}
			
		}
		
		return res;
		
		
	}

	private boolean containCards(List<Integer> handCards, List<Integer> cards) {
		for (Integer id : cards) {
			if (!handCards.contains(id)) {
				return false;
			}
		}
		return true;

	}

	public String addSandhPlayLog(int seat, int action, List<Integer> cards, boolean over, int fen,
			List<Integer> fenCards, int nextSeat) {
		JSONObject json = new JSONObject();
		json.put("seat", seat);
		json.put("action", action);
		json.put("vals", cards);
		json.put("fen", fen);
		if (fenCards != null && over) {
			json.put("fenCards", fenCards);
		}
		json.put("over", over ? 1 : 0);
		json.put("nextSeat", nextSeat);
//		json.put("xifen", nextSeat);
		return json.toJSONString();

	}
	
	
	public String addSandhPlayLog2(int seat, int action, List<Integer> cards, boolean over, int fen,
			List<Integer> fenCards, int nextSeat,int xifen) {
		JSONObject json = new JSONObject();
		json.put("seat", seat);
		json.put("action", action);
		json.put("vals", cards);
		json.put("fen", fen);
		if (fenCards != null && over) {
			json.put("fenCards", fenCards);
		}
		json.put("over", over ? 1 : 0);
		json.put("nextSeat", nextSeat);
		json.put("xifen", xifen);
		return json.toJSONString();

	}
	

	public void playChuPaiRecord(DianTuoPlayer player) {
		JSONArray jarr = new JSONArray();
		for (DianTuoPlayer splayer : seatMap.values()) {
			JSONObject json = new JSONObject();
			json.put("seat", splayer.getSeat());
			JSONArray jarr2 = new JSONArray();
			json.put("cardArr", jarr2);
			jarr.add(json);
		}
		ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.RES_CHUPAI_RECORD, jarr.toJSONString());
		player.writeSocket(builder.build());
	}

	public void playDuzhan(DianTuoPlayer player, int action) {

		if (getTableStatus() != DianTuoConstants.TABLE_STATUS_DUZHAN) {
			addGameActionLog(player, "NoDUZHANState");
			return;
		}
		if (action > 1 || action < 0) {
			return;
		}
		if (banker != 0) {
			addGameActionLog(player, "DUZHAN CUNZAI");
			return;
		}
		player.setDuzhan(action);

		int nextActionSeat = 0;
		boolean allocTeam = false;
		if (action == 1) {
			banker = player.getSeat();
			setNowDisCardSeat(player.getSeat());
			setDisCardSeat(player.getSeat());
			nextActionSeat = player.getSeat();
			allocTeam = true;
		} else {
			int nextS = player.getSeat();
			for (int i = 0; i < maxPlayerCount - 1; i++) {
				nextS += 1;
				if (nextS > maxPlayerCount) {
					nextS = 1;
				}
				DianTuoPlayer nextPlayer = seatMap.get(nextS);
				if (nextPlayer.getDuzhan() == 0) {
					continue;
				}
				nextActionSeat = nextPlayer.getSeat();
				break;
			}
			// 全部放弃独战
			if (nextActionSeat == 0) {
				allocTeam = true;
				nextActionSeat = RandomUtils.nextInt(maxPlayerCount) + 1;
				if(randomCardSeat!=0){
					nextActionSeat = randomCardSeat;
				}
				setNowDisCardSeat(nextActionSeat);
			}
		}

		List<Integer> vals = new ArrayList<>();
		vals.add(action);
		addPlayLog(addSandhPlayLog(player.getSeat(), DianTuoConstants.DUZHAN, vals, false, 0, null, nextActionSeat));

		addGameActionLog(player, "duzhan|" + action);

		setNowDisCardSeat(nextActionSeat);
		ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.DUZHAN_DIANTUO, player.getSeat(), action,
				nextActionSeat);
		for (DianTuoPlayer splayer : seatMap.values()) {
			splayer.writeSocket(builder.build());
		}
		if (allocTeam) {
			allocTeam(banker);
			addPlayLog(addSandhPlayLog(0, DianTuoConstants.FENZU, teamSeat, false, 0, null, getNowDisCardSeat()));
		}

	}

	public void playFenCards(DianTuoPlayer player, int type) {

		if (getTableStatus() != DianTuoConstants.TABLE_STATUS_PLAY) {
			addGameActionLog(player, "NoPlayState");
			return;
		}

		if (type == 2) {
			boolean canFen = false;
			for (DianTuoPlayer splayer : seatMap.values()) {
				if (splayer.getHandPais().isEmpty()) {
					canFen = true;
					break;
				}
			}
			if (!canFen) {
				return;
			}
		}

		if (type == 1) {
			JSONArray jarr = new JSONArray();
			for (DianTuoPlayer splayer : seatMap.values()) {
				JSONObject json = new JSONObject();
				json.put("seat", splayer.getSeat());
				json.put("cards", splayer.getChiFenCards());
				jarr.add(json);
			}

			ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.HUOFEN_DIANTUO, jarr.toJSONString());
			player.writeSocket(builder.build());

		} else {
			JSONArray jarr = new JSONArray();
			for (DianTuoPlayer splayer : seatMap.values()) {
				JSONObject json = new JSONObject();
				json.put("seat", splayer.getSeat());
				List<Integer> scoreCards = CardUtils.getScoreCards(splayer.getHandPais());
				json.put("cards", scoreCards);
				jarr.add(json);
			}

			ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.HUOFEN_DIANTUO, jarr.toJSONString());
			player.writeSocket(builder.build());

		}

	}

	private void allocTeam(int bankerSeat) {
		List<Integer> team = new ArrayList<>();
		if (bankerSeat > 0) {
			for (DianTuoPlayer splayer : seatMap.values()) {
				if (splayer.getSeat() == bankerSeat) {
					continue;
				}
				team.add(splayer.getSeat());
			}
			setTeamSeat(team);
			setTableStatus(DianTuoConstants.TABLE_STATUS_PLAY);
		} else{
			
			if(teamSeat.isEmpty()&&randomCardSeat!=0){
				int seat = randomCardSeat;
				team.add(seat);
				if(maxPlayerCount>2){
					int seat2 = calcNextSeat(seat);
					// 对家
					seat2 = calcNextSeat(seat2);
					team.add(seat2);
				}
				setTeamSeat(team);
				setTableStatus(DianTuoConstants.TABLE_STATUS_PLAY);
			}else{
				team.addAll(teamSeat);
			}
			
		}

		JSONArray jarr = new JSONArray();
		for (DianTuoPlayer splayer : seatMap.values()) {
			JSONObject json = new JSONObject();
			json.put("seat", splayer.getSeat());
			json.put("team", team.contains(splayer.getSeat()) ? 1 : 2);
			jarr.add(json);
		}
		
		ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.DUZHAN_TEAM, jarr.toJSONString());
		for (DianTuoPlayer splayer : seatMap.values()) {
			splayer.writeSocket(builder.build());
		}

		StringBuilder sb = new StringBuilder("Diantuo");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append("fenzu");
		sb.append("|").append(team);
		LogUtil.msgLog.info(sb.toString());
		
		
		
		
		
	}
	
	
	private boolean isDuiJia(int seat1,int seat2){
		int seat3 = calcNextSeat(seat1);
		// 对家
		seat3 = calcNextSeat(seat3);
		if(seat3==seat2){
			return true;
		}
		return false;
	}
	

	public int getAutoTimeOut() {
		return autoTimeOut;
	}

	/**
	 * 人数未满或者人员离线
	 *
	 * @return 0 可以打牌 1人数未满 2人员离线
	 */
	public int isCanPlay() {
		if (seatMap.size() < getMaxPlayerCount()) {
			return 1;
		}
		for (DianTuoPlayer player : seatMap.values()) {
			if (player.getIsEntryTable() != SharedConstants.table_online) {
				// 通知其他人离线
				broadIsOnlineMsg(player, player.getIsEntryTable());
				return 2;
			}
		}
		return 0;
	}

	@Override
	public <T> T getPlayer(long id, Class<T> cl) {
		return (T) playerMap.get(id);
	}

	@Override
	protected boolean quitPlayer1(Player player) {
		return false;
	}

	@Override
	protected boolean joinPlayer1(Player player) {
		return false;
	}

	private void addGameActionLog(Player player, String str) {

		StringBuilder sb = new StringBuilder("diantuo");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append(player.getName());
		sb.append("|").append(str);
		LogUtil.msgLog.info(sb.toString());
	}

	@Override
	protected void initNext1() {
		setNowDisCardIds(null);
		replayDisCard = "";
		timeNum = 0;
		newRound = true;
		finishFapai = 0;
		turnNum = 0;
		turnFirstSeat = 0;
		banker = 0;
		tRank = 0;
		randomCardSeat=0;
		setTableStatus(0);
	}

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

		int firstRandomCard = 0;
		if (playedBureau == 0) {
			if(maxPlayerCount==2){
				DianTuoPlayer player = seatMap.get(RandomUtils.nextInt(maxPlayerCount) + 1);
//				int masterseat = player != null ? player.getSeat() : seatMap.keySet().iterator().next();
				nowDisCardSeat = player.getSeat();
				firstRandomCard = player.getHandPais().get(RandomUtils.nextInt(10));
			}
//			  consume();
//			  calcAfter();
		} else {
			// if (nowDisCardSeat == 0) {
			nowDisCardSeat = lastWinSeat;
			if(nowDisCardSeat==0){
				nowDisCardSeat = RandomUtils.nextInt(maxPlayerCount) + 1;
			}
			setTableStatus(DianTuoConstants.TABLE_STATUS_PLAY);
					//
			// }
		}
		setDisCardSeat(nowDisCardSeat);
		setNowDisCardSeat(nowDisCardSeat);
		// 进入独占环节
		if (totalBureau == 100&&maxPlayerCount!=2) {
			setTableStatus(DianTuoConstants.TABLE_STATUS_DUZHAN);
		} 
		

		for (DianTuoPlayer tablePlayer : seatMap.values()) {
			DealInfoRes.Builder res = DealInfoRes.newBuilder();
			res.addAllHandCardIds(tablePlayer.getHandPais());
			// 叫分的人座位
			res.setNextSeat(nowDisCardSeat);
			res.setGameType(getWanFa());//
			res.setRemain(getTableStatus());
			if(maxPlayerCount==2&&firstRandomCard>0){
				res.setDealDice(firstRandomCard);
			}
			tablePlayer.writeSocket(res.build());
			
			if (tablePlayer.isAutoPlay()) {
 				ArrayList<Integer> val = new ArrayList<>();
 				val.add(1);
 				addPlayLog(addSandhPlayLog(tablePlayer.getSeat(), DianTuoConstants.action_tuoguan, val, false, 0, null,
 						0));
 			}

		}
		
		
		if(playedBureau==0){
			clearPlayLog();
			checkChangeSeat();
			 for (int i = 1; i <= getMaxPlayerCount(); i++) {
				 DianTuoPlayer player = getSeatMap().get(i);
	             addPlayLog(StringUtil.implode(player.getHandPais(), ","));
	         }
			 
			 for (DianTuoPlayer tablePlayer : seatMap.values()) {
					if (tablePlayer.isAutoPlay()) {
		 				ArrayList<Integer> val = new ArrayList<>();
		 				val.add(1);
		 				addPlayLog(addSandhPlayLog(tablePlayer.getSeat(), DianTuoConstants.action_tuoguan, val, false, 0, null,
		 						0));
		 			}
			}
			 
			 if(getTableStatus()!=DianTuoConstants.TABLE_STATUS_DUZHAN){
				 addPlayLog(addSandhPlayLog(0, DianTuoConstants.FENZU, teamSeat, false, 0, null, getNowDisCardSeat()));
			 }
			 
			 
		}

	}

	private void checkChangeSeat() {
		if(maxPlayerCount==2){
			randomCardSeat=1;
			allocTeam(0);
			return;
		}
		
		int randomColor = RandomUtils.nextInt(3) + 1;
		int randomVal= RandomUtils.nextInt(7) + 6;
		int randomId = randomColor*100+randomVal;
		
		List<Integer> randomPa = new ArrayList<Integer>();
		
		for (DianTuoPlayer tablePlayer : seatMap.values()) {
			 List<Integer>  handPais = tablePlayer.getHandPais();
			 for(Integer id : handPais){
				 if(id ==randomId){
					 if(!randomPa.contains(tablePlayer.getSeat())){
						 randomPa.add(tablePlayer.getSeat());
					 }
				 }
			 }
		}
		
		
		List<Integer> team = new ArrayList<>();
		
		
		if(randomPa.size()==1||(randomPa.size()==2&&isDuiJia(randomPa.get(0), randomPa.get(1)))){
			checkChangeSeat();
			return;
		}
		
		if(randomPa.size()==1){
			team.add(randomPa.get(0));
			int seat2 = calcNextSeat(randomPa.get(0));
			// 对家
			seat2 = calcNextSeat(seat2);
			team.add(seat2);
		}else if(randomPa.size()==2){
			boolean duijia = isDuiJia(randomPa.get(0), randomPa.get(1));
			if(duijia){
				team.addAll(randomPa);
			}else {
				team.add(randomPa.remove(0));
				int repDuij = calcNextSeat(team.get(0));
				repDuij = calcNextSeat(repDuij);
				team.add(repDuij);
				int repSeat = randomPa.remove(0);
				
				DianTuoPlayer repDuijPlayer = seatMap.get(repDuij);
				DianTuoPlayer repSeatPlayer = seatMap.get(repSeat);
				repDuijPlayer.setSeat(repSeat);
				repSeatPlayer.setSeat(repDuij);
				seatMap.put(repSeat, repDuijPlayer);
				seatMap.put(repDuij, repSeatPlayer);
				
			}
		}
		
		
		if(getTableStatus()!=DianTuoConstants.TABLE_STATUS_DUZHAN){
			setTableStatus(DianTuoConstants.TABLE_STATUS_PLAY);
		}
		
		
		randomCardSeat= team.get(0);
		setNowDisCardSeat(team.get(0));
//		nowDisCardSeat = team.get(0);
		
		
		
		 for (Player player0 : seatMap.values()) {
		        TableRes.CreateTableRes.Builder res0 = buildCreateTableRes(player0.getUserId(), true, false).toBuilder();
		        res0.setShowRenew(randomId);
		        res0.setFromOverPop(0);
		        player0.writeSocket(res0.build());
		 }
		 
		
		 
		 if(getTableStatus()!=DianTuoConstants.TABLE_STATUS_DUZHAN){
			 setTeamSeat(team);
				allocTeam(0);
		 }
	
	}

	@Override
	protected void robotDealAction() {
	}

	@Override
	protected void deal() {

	}

//	@Override
//	public boolean isFirstBureauOverConsume() {
//		return false;
////		return true;
//	}
	@Override
	public Map<Long, Player> getPlayerMap() {
		Object o = playerMap;
		return (Map<Long, Player>) o;
	}

	@Override
	public int getMaxPlayerCount() {
		return maxPlayerCount;
	}

	public void setMaxPlayerCount(int maxPlayerCount) {
		this.maxPlayerCount = maxPlayerCount;
		changeExtend();
	}

	public List<Integer> getNowDisCardIds() {
		return nowDisCardIds;
	}

	public void setNowDisCardIds(List<Integer> nowDisCardIds) {
		if (nowDisCardIds == null) {
			this.nowDisCardIds.clear();

		} else {
			this.nowDisCardIds = nowDisCardIds;

		}
		dbParamMap.put("nowDisCardIds", JSON_TAG);
	}

	public boolean saveSimpleTable() throws Exception {
		TableInf info = new TableInf();
		info.setMasterId(masterId);
		info.setRoomId(0);
		info.setPlayType(playType);
		info.setTableId(id);
		info.setTotalBureau(totalBureau);
		info.setPlayBureau(1);
		info.setServerId(GameServerConfig.SERVER_ID);
		info.setCreateTime(new Date());
		info.setDaikaiTableId(daikaiTableId);

		changeExtend();

		info.setExtend(buildExtend());
		TableDao.getInstance().save(info);
		loadFromDB(info);
		return true;
	}

	public boolean createSimpleTable(Player player, int play, int bureauCount, List<Integer> params,
			List<String> strParams, boolean saveDb) throws Exception {
		return createTable(player, play, bureauCount, params, saveDb);
	}

	public void createTable(Player player, int play, int bureauCount, List<Integer> params) throws Exception {
		createTable(player, play, bureauCount, params, true);
	}

	public boolean createTable(Player player, int play, int bureauCount, List<Integer> params, boolean saveDb)
			throws Exception {
		// objects对象的值列表
		// [局数,玩法（15或者16张）,this.niao,this.leixing,this.zhuang,this.niaoPoint,必出黑桃3,人数,显示剩余牌数
		long id = getCreateTableId(player.getUserId(), play);
		if (id <= 0) {
			return false;
		}
		if (saveDb) {
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
		} else {
			setPlayType(play);
			setDaikaiTableId(daikaiTableId);
			this.id = id;
			this.totalBureau = bureauCount;
			this.playBureau = 1;
		}

		payType = StringUtil.getIntValue(params, 2, 1);// 1AA,2房主

		remove34 = StringUtil.getIntValue(params, 3, 1);// 删除34
		seeTeamCard = StringUtil.getIntValue(params, 4, 2);// 看队友牌

		randomTeam = StringUtil.getIntValue(params, 5, 0);// 随机队友
		seeHandCards = StringUtil.getIntValue(params, 6, 1);// 看手牌数

		maxPlayerCount = StringUtil.getIntValue(params, 7, 3);// 人数
		noPlayShun = StringUtil.getIntValue(params, 8, 0);// 不玩顺子

		noSpeak = StringUtil.getIntValue(params, 9, 0);// 不打港
		zhengWSK = StringUtil.getIntValue(params, 10, 0);// 正五十K

		feijiSD = StringUtil.getIntValue(params, 11, 0);// 飞机三条可少带
		boomNoWang = StringUtil.getIntValue(params, 12, 0);// 炸弹不带王
		fourRfourB = StringUtil.getIntValue(params, 13, 0);// 四红四黑

		if (maxPlayerCount == 0) {
			maxPlayerCount = 3;
		}
		int time = StringUtil.getIntValue(params, 14, 0);

		this.autoPlay = time > 1;
		autoPlayGlob = StringUtil.getIntValue(params, 15, 0);

		if (time > 0) {
			autoTimeOut2 = autoTimeOut = (time * 1000);
		}

		boomYX = StringUtil.getIntValue(params, 16, 0);

		xifenRate = StringUtil.getIntValue(params, 17, 0);
		
		tianBoomFen = StringUtil.getIntValue(params, 18, 0);
		
		firstOJs = StringUtil.getIntValue(params, 19, 0);
		ztJsYouXi = StringUtil.getIntValue(params, 20, 0);
		
		if(tianBoomFen==0){
			tianBoomFen = 8;
		}
		setLastActionTime(TimeUtil.currentTimeMillis());

		return true;
	}

	@Override
	protected void initNowAction(String nowAction) {

	}

	@Override
	public void initExtend0(JsonWrapper wrapper) {
		// JsonWrapper wrapper = new JsonWrapper(info);
		for (DianTuoPlayer player : seatMap.values()) {
			player.initExtend(wrapper.getString(player.getSeat()));
		}

		maxPlayerCount = wrapper.getInt(5, 3);
		if (maxPlayerCount == 0) {
			maxPlayerCount = 3;
		}
		if (payType == -1) {
			String isAAStr = wrapper.getString("isAAConsume");
			if (!StringUtils.isBlank(isAAStr)) {
				this.payType = Boolean.parseBoolean(wrapper.getString("isAAConsume")) ? 1 : 2;
			} else {
				payType = 1;
			}
		}
		autoTimeOut = wrapper.getInt(6, 0);
		autoPlayGlob = wrapper.getInt(7, 0);
		newRound = wrapper.getInt(9, 1) == 1;
		finishFapai = wrapper.getInt(10, 0);
		belowAdd = wrapper.getInt(11, 0);
		below = wrapper.getInt(12, 0);
		autoTimeOut2 = autoTimeOut;
		// 设置默认值
		if (autoPlay && autoTimeOut <= 1) {
			autoTimeOut2 = autoTimeOut = 60000;
		}

		banker = wrapper.getInt(15, 0);
		turnFirstSeat = wrapper.getInt(16, 0);
		turnNum = wrapper.getInt(18, 0);
		tableStatus = wrapper.getInt(19, 0);

		remove34 = wrapper.getInt(20, 0);
		seeTeamCard = wrapper.getInt(21, 0);
		randomTeam = wrapper.getInt(22, 0);
		seeHandCards = wrapper.getInt(23, 0);
		fourRfourB = wrapper.getInt(24, 0);
		noPlayShun = wrapper.getInt(25, 0);
		noSpeak = wrapper.getInt(26, 0);
		zhengWSK = wrapper.getInt(27, 0);
		feijiSD = wrapper.getInt(28, 0);
		boomNoWang = wrapper.getInt(29, 0);
		tRank = wrapper.getInt(30, 0);

		boomYX = wrapper.getInt(31, 0);
		xifenRate = wrapper.getInt(32, 0);
		tianBoomFen = wrapper.getInt(33, 0);
		randomCardSeat = wrapper.getInt(34, 0);
		
		firstOJs = wrapper.getInt(35, 0);
		ztJsYouXi = wrapper.getInt(36, 0);
	}

	@Override
	protected String buildNowAction() {
		return null;
	}

	@Override
	public void setConfig(int index, int val) {

	}

	public ClosingInfoRes.Builder sendAccountsMsg(boolean over, Player winPlayer, boolean isBreak) {
		return sendAccountsMsg(over, isBreak, 0, false);
	}

	/**
	 * 发送结算msg
	 *
	 * @param over
	 *            是否已经结束
	 * @param winPlayer
	 *            赢的玩家
	 * @return
	 */
	public ClosingInfoRes.Builder sendAccountsMsg(boolean over, boolean isBreak, int firstSeat, boolean win) {
		List<ClosingPlayerInfoRes> list = new ArrayList<>();
		List<ClosingPlayerInfoRes.Builder> builderList = new ArrayList<>();
		//
		if(firstSeat==0){
			over = true;
			isBreak = true;
		}
		if (over) {
			List<Integer> teams = getTeamSeat();
			int teamA = 0;
			int teamB = 0;
			for (DianTuoPlayer player : seatMap.values()) {
				if (teams.contains(player.getSeat())) {
					if(teamA==0){
						teamA += player.getTotalPoint();
					}
				} else {
					if(teamB==0){
						teamB += player.getTotalPoint();
					}
				}
			}
			
			int winFen = 0;
			boolean team1 =false;
			if(teamA>teamB){
				winFen = teamA;
				team1 = true;
			}else if(teamA == teamB){
				winFen = teamA;
				if (teams.contains(firstSeat)) {
					team1 = true;
				}
			}else {
				winFen = teamB;
			}
			
			for (DianTuoPlayer player : seatMap.values()) {
				if(banker>0){
					player.setPlayPoint(player.getTotalPoint()+ player.getXifen(1) * xifenRate);
				}else{
					
					if (teams.contains(player.getSeat())) {
						if(team1){
							player.setPlayPoint(winFen  + player.getXifen(1) * xifenRate);
						}else{
							player.setPlayPoint(-winFen + player.getXifen(1) * xifenRate);
						}
					} else {
						
						if(!team1){
							player.setPlayPoint(winFen  + player.getXifen(1) * xifenRate);
						}else{
							player.setPlayPoint(-winFen + player.getXifen(1) * xifenRate);
						}
					//	player.setPlayPoint(-winFen + player.getXifen(1) * xifenRate);
					}
				}
				
			}
		}
//		if (over) {
//			List<Integer> teams = getTeamSeat(); 
//			int teamA = 0;
//			int teamB = 0;
//			for (DianTuoPlayer player : seatMap.values()) {
//				if (teams.contains(player.getSeat())) {
//					teamA += player.getTotalPoint();
//				} else {
//					teamB += player.getTotalPoint();
//				}
//			}
//			for (DianTuoPlayer player : seatMap.values()) {
//				if (teams.contains(player.getSeat())) {
//					player.setPlayPoint(teamA - teamB + player.getXifen() * xifenRate);
//				} else {
//					player.setPlayPoint(teamB - teamA + player.getXifen() * xifenRate);
//				}
//			}
//		}

		// 大结算低于below分+belowAdd分
		if (over && belowAdd > 0 && playerMap.size() == 2) {
			for (DianTuoPlayer player : seatMap.values()) {
				int totalPoint = player.getTotalPoint();
				if (totalPoint > -below && totalPoint < 0) {
					player.setTotalPoint(player.getTotalPoint() - belowAdd);
				} else if (totalPoint < below && totalPoint > 0) {
					player.setTotalPoint(player.getTotalPoint() + belowAdd);
				}
			}
		}

		for (DianTuoPlayer player : seatMap.values()) {
			ClosingPlayerInfoRes.Builder build = null;
			if (over) {
				build = player.bulidTotalClosingPlayerInfoRes();
			} else {
				build = player.bulidOneClosingPlayerInfoRes();

			}
			if (player.getSeat() == firstSeat) {
				build.setIsHu(1);//win ? 1 : 0
			} else {
				List<Integer> teamList = getTeamPlayerSeat(firstSeat);
				if (teamList.contains(player.getSeat())) {
					build.setIsHu(1);
				} else {
					build.setIsHu(0);
				}
			}

			builderList.add(build);
			// }

			// 信用分
			if (isCreditTable()) {
				player.setWinLoseCredit(player.getPlayPoint() * creditDifen);
			}

		}

		// 信用分计算
		if (isCreditTable()) {
			// 计算信用负分
			calcNegativeCredit();

			long dyjCredit = 0;
			for (DianTuoPlayer player : seatMap.values()) {
				if (player.getWinLoseCredit() > dyjCredit) {
					dyjCredit = player.getWinLoseCredit();
				}
			}
			for (ClosingPlayerInfoRes.Builder builder : builderList) {
				DianTuoPlayer player = seatMap.get(builder.getSeat());
				calcCommissionCredit(player, dyjCredit);

				builder.addExt(player.getWinLoseCredit() + ""); // 10
				builder.addExt(player.getCommissionCredit() + ""); // 11

				// 2019-02-26更新
				builder.setWinLoseCredit(player.getWinLoseCredit());
				builder.setCommissionCredit(player.getCommissionCredit());
			}
		} else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (DianTuoPlayer player : seatMap.values()) {
                player.setWinGold(player.getPlayPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingPlayerInfoRes.Builder builder : builderList) {
                DianTuoPlayer player = seatMap.get(builder.getSeat());
                builder.addExt(player.getWinLoseCredit() + ""); // 10
                builder.addExt(player.getCommissionCredit() + ""); // 11
                builder.setWinLoseCredit(player.getWinGold());
            }
        } else {
			for (ClosingPlayerInfoRes.Builder builder : builderList) {
				DianTuoPlayer player = seatMap.get(builder.getSeat());
				builder.addExt(0 + ""); // 10
				builder.addExt(0 + ""); // 11
			}
		}
		for (ClosingPlayerInfoRes.Builder builder : builderList) {
			DianTuoPlayer player = seatMap.get(builder.getSeat());
			builder.addExt(player.getPiaoFen() + ""); // 13
			list.add(builder.build());
		}

		ClosingInfoRes.Builder res = ClosingInfoRes.newBuilder();
		res.setIsBreak(isBreak ? 1 : 0);
		res.setWanfa(getWanFa());
		res.addAllClosingPlayers(list);
		res.addAllExt(buildAccountsExt(over ? 1 : 0));
		// if(koudi){
		// res.addAllCutCard(dipai);
		// }
		if (over && isGroupRoom() && !isCreditTable()) {
			res.setGroupLogId((int) saveUserGroupPlaylog());
		}
		
		for (DianTuoPlayer player : seatMap.values()) {
			if(over){
				player.setTotalPoint(player.getPlayPoint());
			}
			player.writeSocket(res.build());
		}
		return res;
	}

	public List<String> buildAccountsExt(int over) {
		List<String> ext = new ArrayList<>();
		ext.add(id + "");// 0
		ext.add(masterId + "");// 1
		ext.add(TimeUtil.formatTime(TimeUtil.now()));// 2
		ext.add(playType + "");// 3
		// 设置当前第几局
		ext.add(playBureau + "");// 4
		ext.add(isGroupRoom() ? "1" : "0");// 5
		// 金币场大于0
		ext.add(CommonUtil.isPureNumber(modeId) ? modeId : "0");// 6
		int ratio;
		int pay;
		ratio = 1;
		pay = loadPayConfig(payType);
		ext.add(String.valueOf(ratio));// 7
		ext.add(String.valueOf(pay >= 0 ? pay : 0));// 8
		ext.add(String.valueOf(payType));// 9
		ext.add(String.valueOf(playedBureau));// 10

		ext.add(String.valueOf(matchId));// 11
		ext.add(isGroupRoom() ? loadGroupId() : "");// 12

		ext.add(creditMode + ""); // 13
		ext.add(creditJoinLimit + "");// 14
		ext.add(creditDissLimit + "");// 15
		ext.add(creditDifen + "");// 16
		ext.add(creditCommission + "");// 17
		ext.add(creditCommissionMode1 + "");// 18
		ext.add(creditCommissionMode2 + "");// 19
		ext.add((autoPlay ? 1 : 0) + "");// 20
		ext.add(over + ""); // 21
		return ext;
	}

	@Override
	public String loadGameCode() {
		return GAME_CODE;
	}

	@Override
	public void sendAccountsMsg() {
		
		if(playedBureau==0&&getState()!=table_state.play){
    		return;
    	}
		//结算 手上炸弹算喜分
		
		
		boolean calFen = false;
		boolean calXiFen = false;
		for (DianTuoPlayer dp : seatMap.values()) {
			if ((dp.getGameFen() >= 100&&dp.getRank()==1) && firstOJs == 1) {
				calFen = true;
			}
			if (ztJsYouXi == 1 && (dp.getXifen(1) > 0 || getHandXifen(dp) > 0)) {
				calXiFen = true;
			}

		}
		if(calFen||calXiFen){
			if(!calFen){
				cleanGameFen();
			}
			Map<Integer, DianTuoPlayer> seatMap2 = new HashMap<Integer, DianTuoPlayer>();
			seatMap2.putAll(seatMap);
			calcOverEnd(true);;
			seatMap.putAll(seatMap2);
		}else{
			ClosingInfoRes.Builder builder = sendAccountsMsg(true, null, true);
			saveLog(true, 0l, builder.build());
		}
		
		
	}

	@Override
	public Class<? extends Player> getPlayerClass() {
		return DianTuoPlayer.class;
	}

	@Override
	public int getWanFa() {
		return GameUtil.game_type_DATUO;
	}

	@Override
	public void checkReconnect(Player player) {
		DianTuoTable table = player.getPlayingTable(DianTuoTable.class);
		// player.writeSocket(SendMsgUtil.buildComRes(WebSocketMsgType.req_code_pdk_playBack,
		// table.getReplayDisCard()).build());
		//
	}
	
	@Override
	public boolean isDissSendAccountsMsg() {
		return true;
	}

	@Override
	public void checkAutoPlay() {
		synchronized (this) {
			if (!autoPlay) {
				return;
			}
			// 发起解散，停止倒计时
			if (getSendDissTime() > 0) {
				for (DianTuoPlayer player : seatMap.values()) {
					if (player.getLastCheckTime() > 0) {
						player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
					}
				}
				return;
			}
			// 准备托管
			if (state == table_state.ready && playedBureau > 0) {
				++timeNum;
				for (DianTuoPlayer player : seatMap.values()) {
					// 玩家进入托管后，5秒自动准备
					if (timeNum >= 5 && player.isAutoPlay()) {
						autoReady(player);
					} else if (timeNum >= 30) {
						autoReady(player);
					}
				}
				return;
			}

			DianTuoPlayer player = seatMap.get(nowDisCardSeat);
			if (player == null) {
				return;
			}

			if (getTableStatus() == 0 || state != table_state.play) {
				return;
			}

			// 托管投降检查

			int timeout;

			if (autoPlay) {
				timeout = autoTimeOut;
				if (disCardRound == 0) {
					timeout = autoTimeOut;
				}
			} else if (player.isRobot()) {
				timeout = 3 * SharedConstants.SENCOND_IN_MINILLS;
			} else {
				return;
			}
			long autoPlayTime = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoPlayTimePdk", 2 * 1000);
			long now = TimeUtil.currentTimeMillis();
			boolean auto = player.isAutoPlay();
			if (!auto) {
				// if (GameServerConfig.isAbroad()) {
				// if (!player.isRobot() && now >=
				// player.getNextAutoDisCardTime()) {
				// auto = true;
				// player.setAutoPlay(true, this);
				// }
				// } else {
				auto = checkPlayerAuto(player, timeout);
				// }
			}

			if (auto || player.isRobot()) {
				boolean autoPlay = false;
				// if (GameServerConfig.isAbroad()) {
				// if (player.isRobot()) {
				// autoPlayTime = MathUtil.mt_rand(2, 6) * 1000;
				// } else {
				// autoPlay = true;
				// }
				// }
				// if (player.getAutoPlayTime() == 0L && !autoPlay) {
				// player.setAutoPlayTime(now);
				// } else if (autoPlay || (player.getAutoPlayTime() > 0L && now
				// - player.getAutoPlayTime() >= autoPlayTime)) {
				player.setAutoPlayTime(0L);
				if (state == table_state.play) {

					if (getTableStatus() == DianTuoConstants.TABLE_STATUS_PLAY) {
						List<Integer> curList = new ArrayList<>(player.getHandPais());
						if (curList.isEmpty()) {
							return;
						}
						int size = getTurnCards().size();
						int action = 0;
						List<Integer> disList = new ArrayList<Integer>();
						if (size != 0) {
							action = 1;
						} else {
							disList.add(curList.get(0));
						}

						// 轮首次出牌
						playCommand(player, action, disList);
					}else if (getTableStatus() == DianTuoConstants.TABLE_STATUS_DUZHAN) {
						playDuzhan(player, 0);
					}

					// //托管出牌
					// autoChuPai(player);
					// }else
					// if(getTableStatus()==DianTuoConstants.TABLE_STATUS_JIAOFEN){
					// playJiaoFen(player, 0, 0);
					//
					// }else
					// if(getTableStatus()==DianTuoConstants.TABLE_STATUS_XUANZHU){
					// playXuanzhu(player, 0);
					// }else
					// if(getTableStatus()==DianTuoConstants.TABLE_STATUS_MAIPAI){
					// List<Integer> disList = new ArrayList<Integer>();
					// int size = 0;
					// if(!isChouLiu()){
					// size= 8;
					// }else if(getMaxPlayerCount() == 3 && isChouLiu() ) {
					// size=9;
					// }
					// List<Integer> zCards =
					// CardUtils.getZhu(player.getHandPais(), zhuColor);
					// List<Integer> curList = new
					// ArrayList<>(player.getHandPais());
					// curList.removeAll(zCards);
					// if(curList.size()<size){
					// disList.addAll(curList);
					// disList.addAll(zCards.subList(0, size-curList.size()));
					// }else{
					// disList.addAll(curList.subList(0,size));
					// }
					// playMaipai(player, disList);
					//
					// }

				}
			}
		}
		// }
	}
	
	
	
	
	public void cleanGameFen() {
		for (DianTuoPlayer dp : seatMap.values()) {
			dp.setGameFen(0);
		}
	}
	


	public boolean checkPlayerAuto(DianTuoPlayer player, int timeout) {
		if (player.isAutoPlay()) {
			return true;
		}
		long now = TimeUtil.currentTimeMillis();
		boolean auto = false;
		if (player.isAutoPlayChecked()
				|| (player.getAutoPlayCheckedTime() >= timeout && !player.isAutoPlayCheckedTimeAdded())) {
			player.setAutoPlayChecked(true);
			timeout = autoTimeOut2;
		}
		if (player.getLastCheckTime() > 0) {
			int checkedTime = (int) (now - player.getLastCheckTime());
			if (checkedTime >= timeout) {
				auto = true;
			}
			if (auto) {
				player.setAutoPlay(true, this);
			}
		} else {
			player.setLastCheckTime(now);
			player.setAutoPlayCheckedTimeAdded(false);
		}

		return auto;
	}

	private String cardMarkerToJSON() {
		JSONObject jsonObject = new JSONObject();
		for (Map.Entry<Integer, DianTuoPlayer> entry : seatMap.entrySet()) {
			jsonObject.put("" + entry.getKey(), entry.getValue().getOutPais());
		}
		return jsonObject.toString();
	}

	@Override
	public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams,
			Object... objects) throws Exception {
		createTable(player, play, bureauCount, params);
	}

	@Override
	public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {

	}

	@Override
	public boolean isCreditTable(List<Integer> params) {
		return params != null && params.size() > 13 && StringUtil.getIntValue(params, 13, 0) == 1;
	}

	public String getGameName() {
		return "打坨";
	}

	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_DATUO);

	public static void loadWanfaTables(Class<? extends BaseTable> cls) {
		for (Integer integer : wanfaList) {
			TableManager.wanfaTableTypesPut(integer, cls);
		}
	}

	@Override
	public boolean allowRobotJoin() {
		return StringUtils.contains(ResourcesConfigsUtil.loadServerPropertyValue("robot_modes", ""),
				new StringBuilder().append("|").append(modeId).append("|").toString());
	}

	public void setTableStatus(int tableStatus) {
		this.tableStatus = tableStatus;
		changeExtend();
	}

	public int getTableStatus() {
		return tableStatus;
	}

	@Override
	public boolean isAllReady() {
		return isAllReady1();
		// }else {
		// return super.isAllReady();
		// }
	}

	public boolean isAllReady1() {
		if (super.isAllReady()) {
			if (playBureau != 1) {
				return true;
			}
			for (DianTuoPlayer player : seatMap.values()) {
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean canQuit(Player player) {
		if (state == table_state.play || playedBureau > 0 || isMatchRoom() || isGoldRoom()) {
			return false;
		} else if (state == table_state.ready) {
			return true;
		} else {
			return true;
		}
	}

	public boolean isNewRound() {
		return newRound;
	}

	public void setNewRound(boolean newRound) {
		this.newRound = newRound;
		changeExtend();
	}

	public int getBanker() {
		return banker;
	}

	@Override
	public int getDissPlayerAgreeCount() {
		return getPlayerCount();
	}

	public boolean isRemove34() {
		return remove34 == 1;
	}

	public String getTableMsg() {
		Map<String, Object> json = new HashMap<>();
		json.put("wanFa", "垫坨");
		if (isGroupRoom()) {
			json.put("roomName", getRoomName());
		}
		json.put("playerCount", getPlayerCount());
		json.put("count", getTotalBureau());
		if (autoPlay) {
			json.put("autoTime", autoTimeOut2 / 1000);
			if (autoPlayGlob == 1) {
				json.put("autoName", "单局");
			} else {
				json.put("autoName", "整局");
			}
		}
		return JSON.toJSONString(json);
	}

	public List<Integer> getTeamPlayerSeat(int seat) {

		List<Integer> seats = new ArrayList<Integer>(seatMap.keySet());
		List<Integer> teamSeats = new ArrayList<Integer>(getTeamSeat());
		if (getTeamSeat().contains(seat)) {
			teamSeats.remove((Integer) seat);
			return teamSeats;
		} else {
			seats.remove((Integer) seat);
			seats.removeAll(teamSeats);
			return seats;
		}

	}

	public List<Integer> getTurnCards() {
		return turnCards;
	}

	public void addTurnCards(List<Integer> cards, boolean isClean) {
		if (!isClean) {
			this.turnCards.addAll(cards);
		} else {
			turnCards.clear();
		}
		dbParamMap.put("outPai9", JSON_TAG);
	}

	public int getTurnFirstSeat() {
		return turnFirstSeat;
	}

	public void setTurnFirstSeat(int turnFirstSeat) {
		this.turnFirstSeat = turnFirstSeat;
		changeExtend();
	}

	public int getTurnNum() {
		return turnNum;
	}

	public List<Integer> getTeamSeat() {
		return teamSeat;
	}

	public List<Integer> getActionSeats() {
		return actionSeats;
	}

	public void setActionSeats(List<Integer> actionSeats) {
		this.actionSeats = actionSeats;
		dbParamMap.put("handPai10", JSON_TAG);
	}
	
	public void removeActionSeat(Integer seat) {
		actionSeats.remove(seat);
		dbParamMap.put("handPai10", JSON_TAG);
	}
	

	public void setTeamSeat(List<Integer> teamSeat) {
		this.teamSeat = teamSeat;
		dbParamMap.put("handPai9", JSON_TAG);
	}

	public void setTurnNum(int turnNum) {
		this.turnNum = turnNum;
	}

	public void addTurnNum(int turnNum) {
		this.turnNum += turnNum;
		changeExtend();
	}

	public void rankIncre() {
		tRank += 1;
		changeExtend();
	}

}
