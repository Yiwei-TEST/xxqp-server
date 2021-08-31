package com.sy599.game.qipai.hsth.bean;

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
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayCardRes;
import com.sy599.game.msg.serverPacket.TableRes.*;
import com.sy599.game.qipai.hsth.constant.HsthConstants;
import com.sy599.game.qipai.hsth.tool.CardTool;
import com.sy599.game.qipai.hsth.util.CardUtils;
import com.sy599.game.qipai.hsth.util.HsthSfNew;
import com.sy599.game.qipai.hsth.util.HsthUtil;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HsthTable extends BaseTable {
	public static final String GAME_CODE = "HengShanTongHua";
	private static final int JSON_TAG = 1;
	/*** 当前牌桌上出的牌 */
	private volatile List<Integer> nowDisCardIds = new ArrayList<>();
	/*** 玩家map */
	private Map<Long, HsthPlayer> playerMap = new ConcurrentHashMap<Long, HsthPlayer>();
	/*** 座位对应的玩家 */
	private Map<Integer, HsthPlayer> seatMap = new ConcurrentHashMap<Integer, HsthPlayer>();
	/*** 最大玩家数量 */
	private volatile int maxPlayerCount = 4;

	// private volatile int showCardNumber = 0; // 是否显示剩余牌数量

	public static final int FAPAI_PLAYER_COUNT = 4;// 发牌人数

	private volatile int timeNum = 0;
	private volatile  int jushu ; //
	private volatile  int wanfa ; //
	private volatile  int zhifu ; //
	private volatile  int difen ; // 1
	private volatile  int is3daidui=0;// 1三带对
	private volatile  int isFeijiDld=0;// 1飞机带连对
	private volatile  int isZwf; //0,不抓 1 抓尾分
	private volatile  int duiwu;//0= 铁队 ；1= 摸队：
	private int op_AI=1 ;//托管必打。
	private int op_syp;
	private int op_cardsnum;
	private int op_5tjf;
	private int op_csfgds;
	private volatile  long tuoguanTime;//
	private volatile  int tuoguanType;//
	private volatile  String showCardsUser ;
	private volatile  int randomFenZuCard=0;

//	private int remove34;

//	private int seeTeamCard;
//
//	private int randomTeam;
//
//	private int seeHandCards;
//
//	private int noPlayShun;

//	private int noSpeak;
//	private int zhengWSK;
//
//	private int feijiSD;
//
//	private int boomNoWang;
//
//	private int fourRfourB;
//	private int boomYX;// 炸弹有喜
//
//	private int xifenRate;// 喜分比例

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
	private int banker = 0;// 庄座位

	private int turnFirstSeat = 0;// 当前轮第一个出牌的座位
	private int turnNum;// 回合，每出一轮为1回合

	private int tianBoomFen;//

	/** 特殊状态 1 **/
	private int tableStatus = 0;
	// 低于below加分
	private int belowAdd = 0;
	private int below = 0;
    //是否加倍：0否，1是
    private int jiaBei;
    //加倍分数：低于xx分进行加倍
    private int jiaBeiFen;
    //加倍倍数：翻几倍
    private int jiaBeiShu;
	private int tRank;
	private List<Integer> outfencard  = new ArrayList<>();

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
		if (!StringUtils.isBlank(info.getHandPai8())) {
			//明牌的玩家
			String  showcardsuser = info.getHandPai8() ;
			for (HsthPlayer sb: seatMap.values()){
				if(showcardsuser.contains(String.valueOf(sb.getSeat()))){
					sb.setShowCards(1);
				}
			}
		}
		if (!StringUtils.isBlank(info.getHandPai7())) {
			//庄
			this.banker =Integer.valueOf(info.getHandPai7()) ;
		}
		if (!StringUtils.isBlank(info.getHandPai6())) {
			//出过的分牌
			String  cards =String.valueOf(info.getHandPai6()) ;
			if(cards.length()>0){
				String[] cardsary = cards.split(",");
				for (String c : cardsary){
					this.outfencard.add(Integer.valueOf(c));
				}
			}
		}
//		List<Integer> params =getIntParams();
//		if(null!=params){
//			payType = StringUtil.getIntValue(params, 2, 1);// 1AA,2房主
//			difen = StringUtil.getIntValue(params, 3, 1);//
//			is3daidui = StringUtil.getIntValue(params, 4, 0);//
//			isFeijiDld = StringUtil.getIntValue(params, 5, 0);// 飞机带连队
//			isZwf = StringUtil.getIntValue(params, 6, 0);//抓尾分
//			maxPlayerCount = StringUtil.getIntValue(params, 7, 4);// 人数
//			duiwu = StringUtil.getIntValue(params, 8, 0);//modui =2,zuodui=1
//			if (maxPlayerCount == 0) {
//				maxPlayerCount = 4;
//			}
//		}
	}

	public long getId() {
		return id;
	}

	public HsthPlayer getPlayer(long id) {
		return playerMap.get(id);
	}

	/**
	 * 一局结束
	 */
	public void calcOver() {
		boolean isOver = playBureau >= totalBureau;
		int fristSeat = 0;
		boolean win = false;
		for (HsthPlayer dp : seatMap.values()) {
			if (dp.getRank() == 1) {
				fristSeat = dp.getSeat();
				lastWinSeat = fristSeat;
				break;
			}
		}
		if(maxPlayerCount==4){
			List<Integer> list = getTeamPlayerSeat(fristSeat);//1
			HsthPlayer fristPlayer = seatMap.get(fristSeat);
			int teamSeat = list.get(0);
			HsthPlayer tp = seatMap.get(teamSeat);

			if (tp.getRank() == 2 || tp.getRank() == 3) { //winseat =12 || 13
				for (HsthPlayer dp : seatMap.values()) {
					if (dp.getSeat() != fristSeat && dp.getSeat() != teamSeat) {
						dp.setPmjlfen(-10*op_cardsnum);
						List<Integer> handSC = HsthUtil.getScoreCardsList(dp.getHandPais());
						if (!handSC.isEmpty() && op_csfgds ==1 ) {
							int shaofen = HsthUtil.loadCardScore(handSC);
							dp.setShaofen(-1*shaofen);
						}
					} else {
						dp.setPmjlfen(10*op_cardsnum);
					}
				}
			}else if(tp.getRank() == 4){
				List<Integer> handSC = HsthUtil.getScoreCardsList(tp.getHandPais());
				if (!handSC.isEmpty() && op_csfgds ==1 ) {
					int shaofen = HsthUtil.loadCardScore(handSC);
					tp.setShaofen(-1*shaofen);
				}
			}
			int fenB=0;
			int fenBshaofen =0;
			int fenBpmjlfen =0;
			int fenBChifen =0;
			int fenBxifen =0;
			for (HsthPlayer dp : seatMap.values()) {
				if (dp.getSeat() != fristSeat && dp.getSeat() != teamSeat) {
					fenBshaofen = fenBshaofen+dp.getShaofen();
					fenBpmjlfen = fenBpmjlfen+dp.getPmjlfen();
					fenBChifen =fenBChifen+dp.getChifen();
					fenBxifen =fenBxifen+dp.getXifen();
				}
			}

			int fenAshaofen =0;
			int fenApmjlfen =0;
			int fenAChifen =0;
			int fenAxifen = 0;
			for (HsthPlayer dp : seatMap.values()) {
				if (dp.getSeat() == fristSeat || dp.getSeat() == teamSeat) {
					fenAshaofen = fenAshaofen+dp.getShaofen();
					fenApmjlfen = fenApmjlfen+ dp.getPmjlfen();
					fenAChifen = fenAChifen+dp.getChifen();
					fenAxifen =fenAxifen +dp.getXifen();
				}
			}

			int fenA =  fenAChifen+ fenApmjlfen+Math.abs(fenBshaofen);
//			System.out.println(fenA+"==		fenAchi="+fenAChifen+"	fenApmjlfen="+ fenApmjlfen+"	fenBshaofen="+ fenBshaofen );
			fenB = fenBChifen+fenBpmjlfen + Math.abs(fenAshaofen)  ;
//			System.out.println(fenB+ "==	fenBChifen="+fenBChifen+"	fenBpmjlfen="+fenBpmjlfen);
			fenA = fenA -fenB;
			int tempshaoA = Math.abs(fenBshaofen);
			int tempshaoB = Math.abs(fenAshaofen);

			for (HsthPlayer dp : seatMap.values()) {
				if (dp.getSeat() != fristSeat && dp.getSeat() != teamSeat) {
					dp.setGameFen(fenBChifen);//
					dp.setPmjlfen(dp.getPmjlfen()*2);//
					dp.setShaofen(tempshaoB);//显示变更，结算烧分直接加给赢家
					dp.setZongfen(dp.getZongfen()+dp.getGameFen());
					dp.setPoint(-1*fenA+dp.getXifen());
					dp.changePlayPoint(-1*fenA+dp.getXifen());
				}else{
					dp.setGameFen(fenAChifen);
					dp.setShaofen(tempshaoA);//显示变更，结算烧分直接加给赢家
					dp.setZongfen(dp.getZongfen()+dp.getGameFen());//全局总拿分
					dp.setPmjlfen(dp.getPmjlfen()*2);
					dp.setPoint(fenA+dp.getXifen());
					dp.changePlayPoint(fenA+dp.getXifen());
				}

//			if (totalBureau >= 100) {
//				if (dp.getTotalPoint() >= totalBureau) {
//					isOver = true;
//				}
//			}
			}
		}else {
			//2人玩法
			int loseplayerseat=0 ;
			for (HsthPlayer dp : seatMap.values()) {
				if (dp.getRank() !=1  ) {
					loseplayerseat = dp.getSeat();
					dp.setPmjlfen(-10*op_cardsnum);
					dp.setRank(2);
					List<Integer> handSC = HsthUtil.getScoreCardsList(dp.getHandPais());
					if (!handSC.isEmpty() && op_csfgds ==1 ) {
						int shaofen = HsthUtil.loadCardScore(handSC);
						dp.setShaofen(-1*shaofen);
					}
				}else if(dp.getRank()==1){
					dp.setPmjlfen(10*op_cardsnum);
				}
			}
			HsthPlayer winp =seatMap.get(fristSeat);
			HsthPlayer losp =seatMap.get(loseplayerseat);
			int fenA = winp.getChifen()+winp.getPmjlfen() + Math.abs(losp.getShaofen());

			int fenB = losp.getChifen()+losp.getPmjlfen();
			int tempshao = Math.abs(losp.getShaofen());
			fenA = fenA -fenB;
			for (HsthPlayer dp : seatMap.values()) {
				if (dp.getSeat() != fristSeat  ) {
					dp.setGameFen(dp.getChifen());
					dp.setShaofen(0);//显示变更，结算烧分直接加给赢家
					dp.setZongfen(dp.getZongfen()+dp.getGameFen());
					dp.setPoint(-1*fenA+dp.getXifen());
					dp.changePlayPoint(-1*fenA+dp.getXifen());
				}else{
					dp.setGameFen(dp.getChifen());
					dp.setShaofen(tempshao);//显示变更，结算烧分直接加给赢家
					dp.setZongfen(dp.getZongfen()+dp.getGameFen());//全局总拿分
					dp.setPoint(fenA+dp.getXifen());
					dp.changePlayPoint(fenA+dp.getXifen());
				}
			}
		}
		for (HsthPlayer dp : seatMap.values()) {
			int[] action = dp.getInfoArr();
			if(dp.getPoint()>0){
				dp.changeAction(0,1);//赢次数
			}
			if(dp.getActionValue(1)<dp.getChifen()){
				action[1] = dp.getChifen();//最高抓分
			}
			if(dp.getRank()==1){
				dp.changeAction(2,1);//一游次数
			}
		}
		for (HsthPlayer dtp : seatMap.values()) {
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
				for (HsthPlayer seat : seatMap.values()) {
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
//			resetplayInfo();
		}
        outfencard.clear();
    }
	private void resetplayInfo() {
		for (HsthPlayer dp : seatMap.values()) {
			dp.setChifen(0);
			dp.setPmjlfen(0);
			dp.setShaofen(0);
		}
	}
	private void commonOver(int score) {
		boolean isWin = true;
	}

	private boolean checkAuto3() {
		boolean diss = false;
		// if(autoPlayGlob==3) {
		boolean diss2 = false;
		for (HsthPlayer seat : seatMap.values()) {
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
			for (HsthPlayer player : playerMap.values()) {
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

			for (HsthPlayer player : playerMap.values()) {
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
			for (HsthPlayer player : playerMap.values()) {
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
		for (HsthPlayer player : seatMap.values()) {
			wrapper.putString(player.getSeat(), player.toExtendStr());
		}
        wrapper.putInt(5, maxPlayerCount);
        // wrapper.putString(5,replayDisCard);
        wrapper.putInt(6, autoTimeOut);
        wrapper.putInt(7, autoPlayGlob);
        wrapper.putInt(8, 0);
        wrapper.putInt(9, newRound ? 1 : 0);
        wrapper.putInt(10, finishFapai);
        wrapper.putInt(11, belowAdd);
        wrapper.putInt(12, below);
        wrapper.putInt(13, banker);
        wrapper.putInt(14, turnFirstSeat);
        wrapper.putInt(15, turnNum);
        wrapper.putInt(16, tableStatus);
        wrapper.putInt(17, tRank);
		wrapper.putInt(18, op_syp);
		wrapper.putInt(19, op_cardsnum);
		wrapper.putInt(20, op_5tjf);
		wrapper.putInt(21, op_csfgds);
		wrapper.putInt(22, lastWinSeat);
		wrapper.putInt(23, isZwf);
		wrapper.putInt(24, op_AI);
		wrapper.putInt(25, belowAdd);
		wrapper.putInt(26, below);
        wrapper.putInt(27, jiaBei);
        wrapper.putInt(28, jiaBeiFen);
        wrapper.putInt(29, jiaBeiShu);
		return wrapper;
	}
    @Override
    public void initExtend0(JsonWrapper wrapper) {
        // JsonWrapper wrapper = new JsonWrapper(info);
        for (HsthPlayer player : seatMap.values()) {
            player.initExtend(wrapper.getString(player.getSeat()));
        }
        maxPlayerCount = wrapper.getInt(5, 4);
        if (maxPlayerCount == 0) {
            maxPlayerCount = 4;
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
        banker = wrapper.getInt(13, 0);
        turnFirstSeat = wrapper.getInt(14, 0);
        turnNum = wrapper.getInt(15, 0);
        tableStatus = wrapper.getInt(16, 0);
        tRank = wrapper.getInt(17, 0);
		op_syp = wrapper.getInt(18, 0);
		op_cardsnum = wrapper.getInt(19, 0);
		op_5tjf = wrapper.getInt(20, 0);
		op_csfgds = wrapper.getInt(21, 0);
		lastWinSeat= wrapper.getInt(22, 0);
		isZwf = wrapper.getInt(23, 0);
		op_AI = wrapper.getInt(24, 0);
		belowAdd=wrapper.getInt(25, 0);
		below=wrapper.getInt(26, 0);
        jiaBei=wrapper.getInt(27, 0);
        jiaBeiFen=wrapper.getInt(28, 0);
        jiaBeiShu=wrapper.getInt(29, 0);
	}

	protected String buildPlayersInfo() {
		StringBuilder sb = new StringBuilder();
		for (HsthPlayer pdkPlayer : playerMap.values()) {
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
			setTableStatus(HsthConstants.TABLE_STATUS_PLAY);
			timeNum = 0;
			List<List<Integer>> fplist = CardTool.fapai(4,zp,op_cardsnum);
//			List<Integer> a1 = new ArrayList<>();
//			a1.add(303);	a1.add(305);
//			List<Integer> a3= new ArrayList<>();
//			a3.add(304);	a3.add(310);
//			fplist.clear();
//			fplist.add(a1);
//			fplist.add(a3);
//			fplist.add(new ArrayList<>(a3));	fplist.add(new ArrayList<>(a3));
			int i = 0;
			for (HsthPlayer player :seatMap.values()) {
				player.changeState(player_state.play);
				player.dealHandPais(fplist.get(i), this);
// 				player.dealHandPais(fplist.get(i).subList(0,6), this);
				if (!player.isAutoPlay()) {
					player.setAutoPlay(false, this);
					player.setLastOperateTime(System.currentTimeMillis());
				}
				i++;
			}

		}

		for (HsthPlayer player : seatMap.values()) {
			StringBuilder sb = new StringBuilder("HengShanTongHua");
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

	public HsthPlayer getPlayerBySeat(int seat) {
		// int next = seat >= maxPlayerCount ? 1 : seat + 1;
		return seatMap.get(seat);

	}

	/**
	 * 获取下家
	 *
	 * @param seat
	 * @return
	 */
	private HsthPlayer getNextPlayerBySeat(int seat) {
		int next = seat >= maxPlayerCount ? 1 : seat + 1;
		return seatMap.get(next);

	}

	public Map<Integer, Player> getSeatMap() {
		Object o = seatMap;
		return (Map<Integer, Player>) o;
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
			HsthPlayer reconnet_player = null;
			for (HsthPlayer player : playerMap.values()) {
				if (player.getUserId() == userId) {
					reconnet_player = player;
					break;
				}
			}
			List<Integer> teamlist = getTeamPlayerSeat(reconnet_player.getSeat());
			int reconnet_player_teamseat = 0;
			if(!teamlist.isEmpty()){
				reconnet_player_teamseat = teamlist.get(0);
			}

			for (HsthPlayer player : playerMap.values()) {
				PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(userId, isrecover);
				if (playerRes == null) {
					continue;
				}
				if (player.getUserId() == userId) {
					// 如果是自己重连能看到手牌
					playerRes.addAllHandCardIds(player.getHandPais());
				} else {
					//  判断重连玩家是否已经明牌（明牌=1） 如果是。那么吧轮询到的队友的手牌设置进去
					if(reconnet_player.isShowCards()==1 && player.getSeat()== reconnet_player_teamseat ){
						 playerRes.addAllHandCardIds(player.getHandPais());
					}
				}
				if(player.isShowCards()==1 && player.getUserId() != userId && player.getSeat()!= reconnet_player_teamseat){
					playerRes.addAllHandCardIds(player.getHandPais());
				}
				if (player.getHandPais().isEmpty()) {
					List<Integer> teamSeats = getTeamPlayerSeat(player.getSeat());
					if (!teamSeats.isEmpty() ) {
						HsthPlayer tp = seatMap.get(teamSeats.get(0));
						playerRes.addAllMoldIds(tp.getHandPais());
					}
				}
				players.add(playerRes.build());
			}
			res.addAllPlayers(players);
			if (nowDisCardSeat != 0) {
				res.setNextSeat(nowDisCardSeat);
			}
			// 桌状态  2打牌中
			if(table_state.play==getState()){
				res.setRemain(2);
			}else{
				res.setRemain(getTableStatus());
			}
			res.setRenshu(this.maxPlayerCount);
			res.addExt(this.payType);// 0支付方式
			res.addExt(banker);// 1 庄座位号
			res.addExt(CommonUtil.isPureNumber(modeId) ? Integer.parseInt(modeId) : 0);// 2
			int ratio;
			int pay;
			ratio = 1;
			pay = consumeCards() ? loadPayConfig(payType) : 0;
			List<Integer> scoreCards = HsthUtil.getScoreCardsList(getTurnCards());
			if (scoreCards.size() > 0) {
			 	res.addAllScoreCard(scoreCards);
				int totalScore = HsthUtil.getScoreCards(getTurnCards());
				res.addExt(totalScore);// 6
			} else {
				res.addExt(0);// 6
			}
			// int nextSeat = getNextDisCardSeat();

			// if(getTableStatus() == DianTuoConstants.TABLE_STATUS_JIAOFEN) {
			// nextSeat = getNextActionSeat();
			// }
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
		for (HsthPlayer player : seatMap.values()) {
			if (player.getIsLeave() == 0) {
				num++;
			}
		}
		return num;
	}


	public void disCards(HsthPlayer player, int action, List<Integer> cards) {

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
			for (HsthPlayer p : seatMap.values()) {
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
//		res.setIsLet(code);
		res.setCardType(action);
		boolean isOver = false;
		int guos = 0;

		List<Integer> scoreCards = HsthUtil.getScoreCards2(getTurnCards());
		int totalScore = 0;
		totalScore= HsthUtil.getScoreCards(getTurnCards());
		res.setCurScore(totalScore);
		int seeTeamHand = 0;
		boolean turnOver = false;
		if(op_5tjf==1 && check5t(cards)){
			int xifen =0;
			player.changeAction(3,1);//打出同花次数
			xifen = 100+100*(cards.size()-5);
			int seat = player.getSeat();
			for(HsthPlayer p:seatMap.values()){
				if(p.getSeat()!=seat){
					p.changeXifen(-1*xifen);
				}
			}
			player.changeXifen((maxPlayerCount-1)*xifen);
			res.setIsFirstOut((maxPlayerCount-1)*xifen);
			code =xifen;
		}else{
			res.setIsFirstOut(0);
			code =0;
		}
		//出完
		if (action == 0 && player.getHandPais().isEmpty()) {
			rankIncre();
			player.setRank(tRank);
			res.setIsBt(tRank);
			seeTeamHand = getTeamFriend(player.getSeat());
		}

		int winseat = 0;
		if(!turnOver){
			if (getActionSeats().isEmpty()|| checkTiqianOver(player, action, guos) ) {// 一轮打完
				turnOver = true;
				HsthPlayer winPlayer = seatMap.get(getTurnFirstSeat());
				winseat =winPlayer.getSeat();
				isOver = turnOver(res, winPlayer);
			} else {
				setNowDisCardSeat(getNextPlaySeat(player.getSeat()));
			}
		}
		if(turnOver && action==1 && cards.isEmpty()){
			setNowDisCardSeat(getNextPlaySeat(player.getSeat()));
		}

		addGameActionLog(player, player.isAutoPlay()+"|chupai|" + action + "|" + chuPaiCards+"|"+getNowDisCardSeat());
//		res.addAllCardIds(cards);
		res.addAllCardIds(chuPaiCards);//turnover之后会清理掉cards数据 ，导致抓尾分出牌然后被置空。所以取录像数据即可
		res.setNextSeat(getNowDisCardSeat());
//      seatMap.get(getNowDisCardSeat()).getHandPais().isEmpty()
		res.setUserId(player.getUserId() + "");
		res.setSeat(player.getSeat());
		res.setIsPlay(2);
		res.setIsLet(winseat);//分加在谁身上
		setReplayDisCard();
		if(isZwf==1 &&  maxPlayerCount==4 && (checkFirstSecondTeam() || checkFirstThirdTeam())){
			turnOver =true;
			isOver = true;
			turnOver(res, player);
			res.setIsClearDesk(0);//补尾分最后一手牌不清理
		}else if(isZwf==1 && maxPlayerCount==2){
			turnOver =true;
			isOver = true;
			turnOver(res, player);
			res.setIsClearDesk(0);////补尾分最后一手牌不清理
		}


		for (HsthPlayer p : seatMap.values()) {
			p.writeSocket(res.build());
		}


		addPlayLog(addSandhPlayLog2(player.getSeat(), action, chuPaiCards, turnOver, totalScore, scoreCards,
				getNowDisCardSeat(),code,winseat));

		if (seeTeamHand > 0 && maxPlayerCount == 4) {
			HsthPlayer tp = seatMap.get(seeTeamHand);
			ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.DUIYOU_HAND, tp.getHandPais());
			player.writeSocket(builder.build());
		}

		if(maxPlayerCount==4){
			if(tRank>=2 && isZwf==0){
				if(checkFirstSecondTeam() && tRank==2){
					//12名为队友 游戏小结
					isZwf =1;
				}
				if(checkFirstThirdTeam() && tRank==3){
					//13名为队友
					isZwf=1;//开始抓尾分。
				}
			}
		}else if(maxPlayerCount==2){
			if(tRank==1 && isZwf==0){
				isZwf =1;
			}
		}


		if (isOver) {
			state = table_state.over;
		}

	}

	private boolean check5t(List<Integer> cards) {
		//10副及12副：5个或5个以上相同花色同样数字的牌才为同花
		if(op_cardsnum>=10){
			if(cards.size()<5){
				return false;
			}
		}else {
			if(cards.size()<4){
				return false;
			}
		}

		boolean istonghua = true;
		int val = cards.get(0);
		for(int c :cards){
			if(c!=val){
				istonghua = false;
				break;
			}
		}
		return istonghua;
	}

	private boolean checkTiqianOver(HsthPlayer player, int action, int guos) {
		if (checkFirstSecondTeam()) {
			HsthPlayer secondP = getPlayerByRank(tRank);
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
		if(maxPlayerCount==2){
			return true;
		}
		int firstSeat = 0;
		for (HsthPlayer splayer : seatMap.values()) {
			if (splayer.getRank() == 1) {
				firstSeat = splayer.getSeat();
				break;
			}
		}
		if (firstSeat == 0) {
			return false;
		}
		List<Integer> list = getTeamPlayerSeat(firstSeat);
		HsthPlayer pl = seatMap.get(list.get(0));
		if (pl == null || pl.getRank() != 2) {
			return false;
		}

		return true;

	}
	/***
	 * if(checkTiqianOver())检查1 2名是不是同一队的
	 *
	 * @return
	 */
	private boolean checkFirstThirdTeam() {
		if (tRank == 0) {
			return false;
		}
		if(maxPlayerCount==2){
			return true;
		}
		int firstSeat = 0;
		for (HsthPlayer splayer : seatMap.values()) {
			if (splayer.getRank() == 1) {
				firstSeat = splayer.getSeat();
				break;
			}
		}
		if (firstSeat == 0) {
			return false;
		}
		List<Integer> list = getTeamPlayerSeat(firstSeat);

		HsthPlayer pl = seatMap.get(list.get(0));
		if (pl == null || pl.getRank() != 3) {
			return false;
		}
		return true;
	}

	private void checkDanzhang() {
		JSONArray jarr = new JSONArray();
		for (HsthPlayer splayer : seatMap.values()) {
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
		for (HsthPlayer p : seatMap.values()) {
			p.writeSocket(builder.build());
		}
	}

	private int chupai(HsthPlayer player, int action, List<Integer> cards) {

		List<Integer> copy = new ArrayList<>(player.getHandPais());
		copy.removeAll(cards);
		// Collections.sort(cards);
		// int nextSeat = getNextDisCardSeat();
 		String type = HsthSfNew.getCpType2(HsthSfNew.intCardToStringCard(cards),op_cardsnum);

		List<Integer> desk = new ArrayList<>(getNowDisCardIds());
		List<Integer> otcards = new ArrayList<>(cards);
		if(!desk.isEmpty()){
			int deskcptype = HsthSfNew.getCpIntType(HsthSfNew.intCardToStringCard(getNowDisCardIds()),op_cardsnum);
			int ottype = HsthSfNew.getCpIntType(HsthSfNew.intCardToStringCard(otcards),op_cardsnum);
			if(deskcptype==5 && ottype==5){
				boolean result = CardUtils.CompareTongHua(desk,otcards,op_cardsnum);
				addGameActionLog(player,getId()+"|"+getPlayBureau()+"|HengShanTongHua|result="+result+" |deskcards|"+desk+" |type="+deskcptype+"|outcards|"+otcards+" | type="+ottype+"  ");
				if(!result){
					player.writeErrMsg("出牌不符合规则");
					return -1;
				}
			}
		}
		if("".equals(type)){
			player.writeErrMsg("出牌不符合规则。");
			return -1;
		}else{
			boolean re = true;
			String	type_now = HsthSfNew.getCpType2(HsthSfNew.intCardToStringCard(getNowDisCardIds()),op_cardsnum);
			if(!"".equals(type_now)){
				if("danz".equals(type) && type.equals(type_now)){
					re =CardUtils.compareCard(getNowDisCardIds(),cards,type_now,type);
				}else if("duiz".equals(type)&& type.equals(type_now)){
					re =CardUtils.compareCard(getNowDisCardIds(),cards,type_now,type);
				}else if("3z".equals(type) && type.equals(type_now)){
					re =CardUtils.compareCard(getNowDisCardIds(),cards,type_now,type);
				}else if("boom".equals(type)||"thboom".equals(type)){
					if(check5t(getNowDisCardIds())){
					   type_now ="tonghua";
					 }
					if(check5t(cards)){
						type ="tonghua";
					}
					re =CardUtils.compareCard(getNowDisCardIds(),cards,type_now,type);
				}
			}


			if(!re){
				player.writeErrMsg("服务端：出牌不符合规则！");
				return -1;
			}
		}

		player.addOutPais(cards, this);
		cleanActionState(player.getSeat());
		int xifen=1;
		addTurnCards(cards, false);
		setNowDisCardIds(cards);
		List<Integer> scoreCards = HsthUtil.getScoreCardsList(cards);
		if(!scoreCards.isEmpty()){
			setOutfencard(scoreCards);
		}
		return xifen;
	}

	//	private int checkFourRb(CardType ct) {
	private int checkFourRb( ) {
		int xifen=0;
//		if(fourRfourB==1){
//			int redCount =0;
//			int blackCount =0;
//			for(Integer id: ct.getCardIds()){
//				int color =  CardUtils.loadCardColor(id);
//				if(color==5){
//					continue;
//				}
//				//方片 1 梅花2 洪涛3 黑桃4 5王
//				if(color==1||color==3){
//					redCount++;
//				}else{
//					blackCount++;
//				}
//
//
//			}
//			if(redCount==4){
//				xifen+=1;
//			}
//			if(blackCount==4){
//				xifen+=1;
//			}
//		}
		return xifen;
	}

	//	private int checkXifen(NsbPlayer player, CardType ct) {
	private int checkXifen(HsthPlayer player) {
		int xifen = 0;
//		if (ct.getType() == CardType.BOOM) {
//			if (ct.getVal2() > 5) {
//				int wangCount = 0;
//				for (Integer id : ct.getCardIds()) {
//					int val = CardUtils.loadCardValue(id);
//					if (val == 1 || val == 2) {
//						wangCount += 1;
//					}
//				}
//				int fen = ct.getVal2() - 5 - wangCount;
//				if(fen<0){
//					fen=0;
//				}
//
//				if (ct.getVal2() == 8&&wangCount==0) {
//					fen += 3;
//				}
//				xifen = fen;
//			}
//		} else if (ct.getType() == CardType.TIAN_BOOM) {
//			xifen = tianBoomFen;
//
//		}
		return xifen;
	}
	//一轮回合结束
	private boolean turnOver(PlayCardRes.Builder res, HsthPlayer winPlayer) {
		List<Integer> scoreCards = HsthUtil.getScoreCardsList(getTurnCards());
		if (scoreCards.size() > 0) {
			res.addAllScoreCard(scoreCards);
			winPlayer.addChiFenCards(scoreCards);
			int totalScore = HsthUtil.loadCardScore(scoreCards);
			winPlayer.setChifen(winPlayer.getChifen()+totalScore);
			winPlayer.addGameFen(totalScore);
			if(maxPlayerCount==4){
				List<Integer> teamSeats = getTeamPlayerSeat(winPlayer.getSeat());
				if (!teamSeats.isEmpty()) {
					for (Integer seat : teamSeats) {
						HsthPlayer tp = seatMap.get(seat);
						if (tp != null) {
							tp.addGameFen(totalScore);
						}
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
//		if (winPlayer.getHandPais().isEmpty()) {
//			if(banker==0){
//				List<Integer> sList = getTeamPlayerSeat(nextSeat);
//				nextSeat = sList.get(0);
//			}
//		}
		setNowDisCardSeat(nextSeat);
		cleanActionState(0);
		addTurnCards(null, true);
		setNowDisCardIds(null);

		return checkOverGameFen();
	}

	private boolean checkOverGameFen() {
		if(maxPlayerCount==2){
			return false;
		}else {
			if (tRank == 2) {
				// 看第一名是不是队友哦
				if (checkFirstSecondTeam()) {
					for (HsthPlayer p : seatMap.values()) {
						if (p.getRank() == 0) { // 三四名
							rankIncre();
							p.setRank(tRank);
						}
					}
					return true;
				}
			} else if (tRank == 3) {
				// 要结束了
				rankIncre();
				for (HsthPlayer p : seatMap.values()) {
					if (p.getRank() == 0) { //4
						p.setRank(tRank);
						break;
					}
				}
				return true;
			} else if (tRank == 4) {
				return true;
			}
		}
		return false;
	}

	private int getNextPlaySeat(int nextSeat) {
		for (int i = 0; i < maxPlayerCount - 1; i++) {
			nextSeat += 1;
			if (nextSeat > maxPlayerCount) {
				nextSeat = 1;
			}
			HsthPlayer nextPlayer = seatMap.get(nextSeat);
			if (nextPlayer.getRank() > 0) {
				continue;
			}
			break;
		}
		return nextSeat;
	}

	private HsthPlayer getPlayerByRank(int rank) {
		HsthPlayer player = null;
		for (HsthPlayer p : seatMap.values()) {
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
		for (HsthPlayer p : seatMap.values()) {
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
	public void playCommand(HsthPlayer player, int action, List<Integer> cards) {
		synchronized (this) {
			if (state != table_state.play) {
				return;
			}
//			// 出牌阶段
//			if (getTableStatus() != HsthConstants.TABLE_STATUS_PLAY) {
//				return;
//			}

			if (!containCards(player.getHandPais(), cards)) {
				return;
			}
			if (player.getHandPais().isEmpty()) {
				return;
			}
			if (player.getSeat() != getNowDisCardSeat()) {
				// 没轮到出牌
				return;
			}
			changeDisCardRound(1);
			// 出牌了
			disCards(player, action, cards);
			setLastActionTime(TimeUtil.currentTimeMillis());

			if (isOver()) {
				calcOver();
			}
		}
	}

	public void playCommandAI(HsthPlayer player){
		synchronized (this) {
			if (state != table_state.play) {
				return;
			}
			if (player.getHandPais().isEmpty()) {
				return;
			}
			if (player.getSeat() != getNowDisCardSeat()) {
				// 没轮到出牌
				return;
			}
			int action =0;
			List<Integer> cards = new ArrayList<>();
			List<Integer> deskCards = new  ArrayList<>(getNowDisCardIds());
			List<Integer> hand = new  ArrayList<>();
			CardUtils.copyList(player.getHandPais(),hand);
			if(!deskCards.isEmpty()){
				// 托管 被动出牌
				int cptype = HsthSfNew.getCpIntType(HsthSfNew.intCardToStringCard(deskCards),op_cardsnum);
				List<List<Integer>>  result = CardUtils.getTip(deskCards,hand,cptype,op_cardsnum );

				if(!result.isEmpty()){
					action =0;
					cards.addAll(result.get(0));
				}else{
					//要不起
					action =1;
					cards= new ArrayList<>();
				}
			}else{
				//托管 主动出牌
				List<Integer> result = new ArrayList<>();
                result = CardUtils.getMinNumCards(hand,op_cardsnum);
                cards =result;
			}

			if (!containCards(player.getHandPais(), cards)) {
				return;
			}

			changeDisCardRound(1);
			// 出牌了
			disCards(player, action, cards);
			setLastActionTime(TimeUtil.currentTimeMillis());
			if (isOver()) {
				calcOver();
			}
		}
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
								   List<Integer> fenCards, int nextSeat,int xifen,int plusFenSeat) {
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
		json.put("islet",plusFenSeat);
		return json.toJSONString();

	}


	public void playChuPaiRecord(HsthPlayer player) {
		JSONArray jarr = new JSONArray();
		for (HsthPlayer splayer : seatMap.values()) {
			JSONObject json = new JSONObject();
			json.put("seat", splayer.getSeat());
			JSONArray jarr2 = new JSONArray();
			json.put("cardArr", jarr2);
			jarr.add(json);
		}
		ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.RES_CHUPAI_RECORD, jarr.toJSONString());
		player.writeSocket(builder.build());
	}

	public void playFenCards(HsthPlayer player, int type) {
        if (state != table_state.play){
            addGameActionLog(player, "NoPlayState");
            return;
        }
		if (type == 1) {
				JSONArray jarr = new JSONArray();
				JSONObject json = new JSONObject();
				json.put("cards",getOutfencard());
				jarr.add(json);
			ComRes.Builder builder = SendMsgUtil.buildComRes(4102, jarr.toJSONString());
			player.writeSocket(builder.build());
		}
	}

	public void playShowCards(HsthPlayer player) {
		if(player.isShowCards()==1){
			return;
		}
		//action =6  玩家明牌
		addPlayLog(addSandhPlayLog2(player.getSeat(), HsthConstants.MINGPAI, null, false, 0, null,
				getNowDisCardSeat(),0,0));
		player.setShowCards(1);
		setShowCardsUser(player.getSeat()+"");
		int teamseat = getTeamPlayerSeat(player.getSeat()).get(0);
		for (HsthPlayer splayer : seatMap.values()) {
				if(splayer.getSeat()==player.getSeat()){
					JSONArray jarr = new JSONArray();
					JSONObject json = new JSONObject();
					json.put("seat",teamseat);
					json.put("cards",getPlayerBySeat(teamseat).getHandPais());
					jarr.add(json);
//					WebSocketMsgType
					//给自己 我看队友的牌 。
					ComRes.Builder builder = SendMsgUtil.buildComRes(4200,player.getSeat(),jarr.toJSONString());
					splayer.writeSocket(builder.build());
				}else{
					JSONArray jarr = new JSONArray();
					JSONObject json = new JSONObject();
					json.put("seat",player.getSeat());
					json.put("cards",player.getHandPais());//别人看我的牌
					jarr.add(json);
					ComRes.Builder builder = SendMsgUtil.buildComRes(4200,player.getSeat(),jarr.toJSONString());
					splayer.writeSocket(builder.build());
				}
		}
	}
	public void allocTeam(int bankerSeat) {
		List<Integer> team = new ArrayList<>();
		int index = playerMap.get(masterId).getSeat();
		if(maxPlayerCount == 2){
			teamSeat.add(index);
		}else{
			teamSeat.add(1);
			teamSeat.add(3);
		}
		setTeamSeat(teamSeat);
		JSONArray jarr = new JSONArray();
		for (HsthPlayer splayer : seatMap.values()) {
			JSONObject json = new JSONObject();
			json.put("seat", splayer.getSeat());
			json.put("banker", banker);
			json.put("team", teamSeat.contains(splayer.getSeat()) ? 1 : 2);
			jarr.add(json);
		}
//		ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.DUZHAN_TEAM, jarr.toJSONString());
		ComRes.Builder builder = SendMsgUtil.buildComRes(4101, jarr.toJSONString());
		for (HsthPlayer splayer : seatMap.values()) {
			splayer.writeSocket(builder.build());
		}
		addPlayLog(addSandhPlayLog(0, HsthConstants.FENZU, teamSeat, false, 0, null, getNowDisCardSeat()));

		StringBuilder sb = new StringBuilder("HengShanTongHua");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append("fenzu");
		sb.append("|").append(team);
		LogUtil.msgLog.info(sb.toString());
		for (Player p : seatMap.values()) {
			p.writeSocket(buildCreateTableRes(p.getUserId(),false,false));
		}

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
		for (HsthPlayer player : seatMap.values()) {
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
		StringBuilder sb = new StringBuilder("HengShanTongHua");
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
		showCardsUser ="";
		isZwf=0;
		teamSeat.clear();
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
		if (playedBureau == 0) {
			HsthPlayer player = playerMap.get(masterId);
			int masterseat = seatMap.keySet().iterator().next();
			//	int masterseat = player != null ? player.getSeat() : seatMap.keySet().iterator().next();
			nowDisCardSeat = masterseat;
		} else {
			nowDisCardSeat = lastWinSeat;
		}

		setDisCardSeat(nowDisCardSeat);
		setNowDisCardSeat(nowDisCardSeat);
		for (HsthPlayer tablePlayer : seatMap.values()) {
			DealInfoRes.Builder res = DealInfoRes.newBuilder();
			res.addAllHandCardIds(tablePlayer.getHandPais());
			res.setNextSeat(nowDisCardSeat);
			res.setGameType(getWanFa());//
			res.setRemain(getTableStatus());//发牌之前 0.  打牌1
			tablePlayer.writeSocket(res.build());
			if (tablePlayer.isAutoPlay()) {
				ArrayList<Integer> val = new ArrayList<>();
				val.add(1);
				addPlayLog(addSandhPlayLog(tablePlayer.getSeat(), HsthConstants.action_tuoguan, val, false, 0, null,
						0));
			}
		}
		allocTeam(nowDisCardSeat);
	}

	@Override
	protected void robotDealAction() {
	}

	@Override
	protected void deal() {

	}

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
		op_syp = StringUtil.getIntValue(params, 3, 1);//1剩余牌
		op_cardsnum =StringUtil.getIntValue(params, 4, 8);// 8  9 10 12副牌;
		op_5tjf=StringUtil.getIntValue(params, 5, 0);// 1=5同奖分
		op_csfgds=StringUtil.getIntValue(params, 6, 0);// 1=沉死给队友。
		maxPlayerCount = StringUtil.getIntValue(params, 7, 4);// 人数
		duiwu =1;// StringUtil.getIntValue(params, 8, 0);//modui =2,zuodui=1
		op_AI =StringUtil.getIntValue(params, 8, 1);//智能托管
		if (maxPlayerCount == 0) {
			maxPlayerCount = 4;
		}
		int time = StringUtil.getIntValue(params, 9, 0);
		this.autoPlay = time > 1;
		autoPlayGlob = StringUtil.getIntValue(params, 10, 0);// 1单局  2整局  3三局
		if (time > 0) {
			autoTimeOut2 = autoTimeOut = (time * 1000);
		}
        this.jiaBei = StringUtil.getIntValue(params, 11, 0);
        this.jiaBeiFen = StringUtil.getIntValue(params, 12, 100);
        this.jiaBeiShu = StringUtil.getIntValue(params, 13, 1);
        if(this.getMaxPlayerCount() != 2){
            jiaBei = 0 ;
        }
		if(maxPlayerCount==2){
		 belowAdd = StringUtil.getIntValue(params, 14, 0);
//			if(belowAdd<=100&&belowAdd>=0){
//                this.belowAdd=belowAdd;
//            }
		  below = StringUtil.getIntValue(params, 15, 0);
//			if(below<=100&&below>=0){
//				this.below=below;
//				if(belowAdd>0&&below==0)
//					this.below=10;
//			}
		}
		setLastActionTime(TimeUtil.currentTimeMillis());
		return true;
	}

	@Override
	protected void initNowAction(String nowAction) {

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
	 * @param win
	 *            赢的玩家
	 * @return
	 */
	public ClosingInfoRes.Builder sendAccountsMsg(boolean over, boolean isBreak, int firstSeat, boolean win) {
		List<ClosingPlayerInfoRes> list = new ArrayList<>();
		List<ClosingPlayerInfoRes.Builder> builderList = new ArrayList<>();
        //大结算计算加倍分
        if(over && jiaBei == 1){
            int jiaBeiPoint = 0;
            int loserCount = 0;
            for (HsthPlayer player : seatMap.values()) {
                if (player.getPlayPoint() > 0 && player.getPlayPoint() < jiaBeiFen) {
                    jiaBeiPoint += player.getPlayPoint() * (jiaBeiShu - 1);
                    player.setPlayPoint(player.getPlayPoint() * jiaBeiShu);
                } else if (player.getPlayPoint() < 0) {
                    loserCount++;
                }
            }
            if (jiaBeiPoint > 0) {
                for (HsthPlayer player : seatMap.values()) {
                    if (player.getPlayPoint() < 0) {
                        player.setPlayPoint(player.getPlayPoint() - (jiaBeiPoint / loserCount));
                    }
                }
            }
        }
		// 大结算低于below分+belowAdd分
		if (over && belowAdd > 0 && playerMap.size() == 2) {
			for (HsthPlayer player : seatMap.values()) {
				int totalPoint = player.getPlayPoint();
				if (totalPoint > -below && totalPoint < 0) {
					player.setPlayPoint(player.getPlayPoint() - belowAdd);
				} else if (totalPoint < below && totalPoint > 0) {
					player.setPlayPoint(player.getPlayPoint() + belowAdd);
				}
			}
		}

		for (HsthPlayer player : seatMap.values()) {
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
			for (HsthPlayer player : seatMap.values()) {
				if (player.getWinLoseCredit() > dyjCredit) {
					dyjCredit = player.getWinLoseCredit();
				}
			}
			for (ClosingPlayerInfoRes.Builder builder : builderList) {
				HsthPlayer player = seatMap.get(builder.getSeat());
				calcCommissionCredit(player, dyjCredit);

				builder.addExt(player.getWinLoseCredit() + ""); // 10
				builder.addExt(player.getCommissionCredit() + ""); // 11

				// 2019-02-26更新
				builder.setWinLoseCredit(player.getWinLoseCredit());
				builder.setCommissionCredit(player.getCommissionCredit());
			}
		} else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (HsthPlayer player : seatMap.values()) {
                player.setWinGold(player.getPlayPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingPlayerInfoRes.Builder builder : builderList) {
                HsthPlayer player = seatMap.get(builder.getSeat());
                builder.addExt(player.getWinLoseCredit() + ""); // 10
                builder.addExt(player.getCommissionCredit() + ""); // 11
                builder.setWinLoseCredit(player.getWinGold());
            }
        } else {
			for (ClosingPlayerInfoRes.Builder builder : builderList) {
				HsthPlayer player = seatMap.get(builder.getSeat());
				builder.addExt(0 + ""); // 10
				builder.addExt(0 + ""); // 11
			}
		}
		for (ClosingPlayerInfoRes.Builder builder : builderList) {
			HsthPlayer player = seatMap.get(builder.getSeat());
//			builder.addExt(player.getPiaoFen() + ""); // 13
			builder.addExt( "");
			list.add(builder.build());
		}

		ClosingInfoRes.Builder res = ClosingInfoRes.newBuilder();
		res.setIsBreak(isBreak ? 1 : 0);
		res.setWanfa(getWanFa());
		res.addAllClosingPlayers(list);
		res.addAllExt(buildAccountsExt(over ? 1 : 0));
		if (over && isGroupRoom() && !isCreditTable()) {
			res.setGroupLogId((int) saveUserGroupPlaylog());
		}

		for (HsthPlayer player : seatMap.values()) {
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
		ClosingInfoRes.Builder builder = sendAccountsMsg(true, null, true);
		saveLog(true, 0l, builder.build());
	}

	@Override
	public Class<? extends Player> getPlayerClass() {
		return HsthPlayer.class;
	}

	@Override
	public int getWanFa() {
		return GameUtil.play_type_pk_hsth;
	}

	@Override
	public void checkReconnect(Player player) {
		HsthTable table = player.getPlayingTable(HsthTable.class);
		// player.writeSocket(SendMsgUtil.buildComRes(WebSocketMsgType.req_code_pdk_playBack,
		// table.getReplayDisCard()).build());
		//
	}

	@Override
	public void checkAutoPlay() {
		synchronized (this) {
			if (!autoPlay) {
				return;
			}
			// 发起解散，停止倒计时
			if (getSendDissTime() > 0) {
				for (HsthPlayer player : seatMap.values()) {
					if (player.getLastCheckTime() > 0) {
						player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
					}
				}
				return;
			}

			if (isAutoPlayOff()) {
				// 托管关闭
				for (int seat : seatMap.keySet()) {
					HsthPlayer player = seatMap.get(seat);
					player.setAutoPlay(false, this);
				}
				return;
			}
			// 准备托管
			if (state == table_state.ready && playedBureau > 0) {
				++timeNum;
				for (HsthPlayer player : seatMap.values()) {
					// 玩家进入托管后，5秒自动准备
					if (timeNum >= 5 && player.isAutoPlay()) {
						autoReady(player);
					} else if (timeNum >= 30) {
						autoReady(player);
					}
				}
				return;
			}

			HsthPlayer player = seatMap.get(nowDisCardSeat);
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
				player.setAutoPlayTime(0L);
				if (state == table_state.play) {
					if (getTableStatus() == HsthConstants.TABLE_STATUS_PLAY) {
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

						if(op_AI==1){
							//托管必出 AI出牌
							  playCommandAI(player);
						}else{
							if (player.getSeat() == getNowDisCardSeat() && getNowDisCardIds().isEmpty()) {
								// 轮到你主动出牌
								playCommandAI(player);
							}else{
								playCommand(player, action, disList);
							}
						}

					}
				}
			}
		}
		// }
	}


	public boolean checkPlayerAuto(HsthPlayer player, int timeout) {
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
		for (Map.Entry<Integer, HsthPlayer> entry : seatMap.entrySet()) {
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
		return "衡山同花";
	}

	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_pk_hsth);//GameUtil.play_type_pk_nsb=255

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
			for (HsthPlayer player : seatMap.values()) {
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

	public String getTableMsg() {
		Map<String, Object> json = new HashMap<>();
		json.put("wanFa", "牛十别");
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
	public int getTeamFriend(int seat){
		if(maxPlayerCount==2){
			return 0;
		}else{
			if(seat==1){
				return 3;
			}else if(seat==2){
				return 4;
			}else if(seat==3){
				return 1;
			}else if(seat==4){
				return 2;
			}else{
				return 0;
			}
		}

	}
	public List<Integer> getTeamPlayerSeat(int seat) {
		List<Integer> seats = new ArrayList<Integer>();
		if(teamSeat.isEmpty()){
			return seats;
		}
		if(maxPlayerCount==2){
			 return seats;
		}else{
			if(seat==1){
				seats.add(3);
			}else if(seat==2){
				seats.add(4);
			}else if(seat==3){
				seats.add(1);
			}else if(seat==4){
				seats.add(2);
			}else{
				return seats;
			}
		}
		return seats;
//		List<Integer> teamSeats = new ArrayList<Integer>(getTeamSeat());
//		if (getTeamSeat().contains(seat)) {
//			teamSeats.remove((Integer) seat);
//			return teamSeats;
//		} else {
//			seats.remove((Integer) seat);
//			return seats;
//		}

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

	public void playChangeSeat() {

	}
	public void setPlayerMap( Map<Long, HsthPlayer>  p) {
		this.playerMap = p;
	}

	public void setSeatMap(Map<Integer, HsthPlayer> map) {
		this.seatMap = map;
	}

	public int getRandomFenZuCard() {
		return randomFenZuCard;

	}

	public void setRandomFenZuCard(int randomFenZuCard) {
		this.randomFenZuCard = randomFenZuCard;
		changeExtend();
	}

	public int getDuiwu() {
		return duiwu;
	}

	public void setDuiwu(int duiwu) {
		this.duiwu = duiwu;
		changeExtend();
	}

	public String getShowCardsUser() {
		return showCardsUser;
	}

	public void setShowCardsUser(String showCardsUser) {
		this.showCardsUser += showCardsUser+",";
		dbParamMap.put("handPai8" , this.showCardsUser);
	}

	public void setBanker(int banker) {
		this.banker = banker;//庄
		dbParamMap.put("handPai7" , banker);
	}

	public List<Integer> getOutfencard() {
		return outfencard;
	}

	public void setOutfencard(List<Integer> outfencard) {
		this.outfencard.addAll(outfencard);
		StringBuffer sb = new StringBuffer();
		for (int card : this.outfencard){
			sb.append(card).append(",");
		}
		dbParamMap.put("handPai6" , sb.toString());
	}
}
