package com.sy599.game.qipai.ddz.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.CommonUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.bean.CreateTableInfo;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.*;
import com.sy599.game.db.dao.DataStatisticsDao;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayCardRes;
import com.sy599.game.msg.serverPacket.TableRes.*;
import com.sy599.game.qipai.ddz.constant.DdzConstants;
import com.sy599.game.qipai.ddz.tool.CardTool;
import com.sy599.game.qipai.ddz.util.CardUtils;
import com.sy599.game.qipai.ddz.util.DdzSfNew;
import com.sy599.game.qipai.ddz.util.DdzUtil;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DdzTable extends BaseTable {
	public static final String GAME_CODE = "Ddz";
	private static final int JSON_TAG = 1;
	/*** 当前牌桌上出的牌 */
	private volatile List<Integer> nowDisCardIds = new ArrayList<>();
	/*** 玩家map */
	private Map<Long, DdzPlayer> playerMap = new ConcurrentHashMap<Long, DdzPlayer>();
	/*** 座位对应的玩家 */
	private Map<Integer, DdzPlayer> seatMap = new ConcurrentHashMap<Integer, DdzPlayer>();
	/*** 最大玩家数量 */
	private volatile int maxPlayerCount = 4;

	// private volatile int showCardNumber = 0; // 是否显示剩余牌数量

	public static final int FAPAI_PLAYER_COUNT = 4;// 发牌人数

	private volatile int timeNum = 0;

	private volatile  int callSeat ; // 1
	private volatile  int is3dai2;// 1三带2
	private volatile  int is4dai2;// 1飞机带连对
	private volatile  int is3zhang; //0,不抓 1 抓尾分
	private volatile  int duiwu;//0= 铁队 ；1= 摸队：
	private volatile  String showCardsUser ;
	private volatile  int randomFenZuCard=0;
	private int op_gametype;//
	private int op_fengding;
	private int op_rangpai;
	private int op_bujiabei;
	private int op_dipaijiabei;
	private int op_lunliuJiao;
	private int jiaBei;
	private int jiaBeiFen;
	private int jiaBeiShu;

	//规则参数
	/**抢地主次数  最高4次*/
	private volatile int robTotal=1;

	/**底分倍数 每抢一次地主X2*/
	private volatile int qiangDzBeiShu=1;

	/**让牌张数 每抢一次+1*/
	private volatile int rangPaiNum=1;

	/**底牌 */
	private volatile List<Integer> dplist = new ArrayList<>();

	/**底牌倍数*/
	private volatile int dpBeiShu =1;

	/**勾选让牌 地主的让牌张数*/
	private volatile int bankRangPaiNum=0;

	private volatile int bankRangPaiNumBeiShu=1;

	/**重发牌次数 5次后解散*/
	private volatile int refapainum =0;

	/**炸弹番数*/
	private volatile int boomBeiShu =1;

	/**选择加倍番数*/
	private volatile int sel_jiabeiBeiShu =1;
	private volatile int isCt = 0;

	private volatile String  restcards ="";

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
			for (DdzPlayer sb: seatMap.values()){
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
	}

	public long getId() {
		return id;
	}

	public DdzPlayer getPlayer(long id) {
		return playerMap.get(id);
	}

	/**
	 * 一局结束
	 */
	public void calcOver() {
		boolean isOver = playBureau >= totalBureau;
		int fristSeat = lastWinSeat;
		boolean win = false;
		boolean dzwin = false;
		if(fristSeat==banker){
			dzwin = true;
		}
		//添加日志观察中途解散重启服务器分为0异常原因
		StringBuilder _log = new StringBuilder();
		_log.append("|calcOver|地主win="+dzwin+"|");
		_log.append("|dpBeiShu="+dpBeiShu);
		_log.append("|boomBeiShu="+boomBeiShu);
		_log.append("|getQiangDzBeiShu="+getQiangDzBeiShu());
		_log.append("|sel_jiabeiBeiShu="+sel_jiabeiBeiShu);
		_log.append("|bankRangPaiNumBeiShu="+bankRangPaiNumBeiShu);
		_log.append("|dpBeiShu="+dpBeiShu);
		_log.append("|op_fengding="+op_fengding);
		 addGameActionLog(seatMap.get(banker),_log.toString());
		if(dzwin){
			DdzPlayer nm = null;
			for (DdzPlayer dp : seatMap.values()) {
				if (dp.getSeat()!=fristSeat) {
					nm=dp;
				}
			}
			int nmsyp=0;
			if(null== nm || null==nm.getHandPais()){
				return;
			}else{
				nmsyp= nm.getHandPais().size();
			}

			//计算总番数，
			setCt(0);
			int ctbs =1;
			if((nmsyp==17 || nm.getCpnum()==0)){
				ctbs=2;
				isCt= 1;
				setCt(isCt);
			}
			if (bankRangPaiNum>0 && nm.getCpnum()==1){
				//地主让牌。农民只出一首。地主反春天
				ctbs=2;
				isCt= 2;
				setCt(isCt);
			}
			// 春天x炸弹x*（抢地主时翻得倍数）x底牌x(选择加倍番数)
			int zongBeiShu = ctbs*boomBeiShu*getQiangDzBeiShu()*dpBeiShu*sel_jiabeiBeiShu*bankRangPaiNumBeiShu;

			if(op_fengding>0 && zongBeiShu>op_fengding){
				zongBeiShu =op_fengding;
			}

			int socre = zongBeiShu;
			for (DdzPlayer dp : seatMap.values()) {
				if (dp.getSeat()==fristSeat) {
					dp.setPoint(socre);
					dp.changePlayPoint( socre);
					if(isGoldRoom()){
						long rate = getGoldRoom().getRate();
						dp.setWinGold(socre);
						dp.setPoint((int) (socre*rate));
						dp.setPlayPoint((int) (socre*rate));
					}
				}else{
					dp.setPoint(socre*-1);
					dp.changePlayPoint(socre*-1);
					if(isGoldRoom()){
						long rate = getGoldRoom().getRate();
						dp.setWinGold(socre*-1);
						dp.setPoint((int) (socre*-1*rate));
						dp.setPlayPoint((int) (socre*-1*rate));
					}
				}
			}
		}else{
			//农民赢
			DdzPlayer dz = seatMap.get(banker);
			//计算总番数，
			int fctbs =1;
			if(dz.getCpnum()==1 && bankRangPaiNum==0){
				fctbs=2;
				isCt= 2;
				setCt(isCt);
			}
			if(bankRangPaiNum>0 && dz.getCpnum()==0){
				fctbs=2;
				isCt= 1;
				setCt(isCt);
			}
			// 反春天x炸弹x*（抢地主时翻得倍数）x底牌x(选择加倍番数)
			int zongBeiShu = fctbs*boomBeiShu*getQiangDzBeiShu()*dpBeiShu*sel_jiabeiBeiShu*bankRangPaiNumBeiShu;
			if(op_fengding>0 && zongBeiShu>op_fengding){
				zongBeiShu =op_fengding;
			}


			int socre = zongBeiShu;
			for (DdzPlayer dp : seatMap.values()) {
				if (dp.getSeat()==fristSeat) {
					dp.setPoint(socre);
					dp.changePlayPoint( socre);
					if(isGoldRoom()){
						long rate = getGoldRoom().getRate();
						dp.setWinGold(socre);
                        dp.setPoint((int) (socre*rate));
                        dp.setPlayPoint( (int) (socre*rate));
					}
				}else{
					dp.setPoint(socre*-1);
					dp.changePlayPoint(socre*-1);
					if(isGoldRoom()){
						long rate = getGoldRoom().getRate();
						dp.setWinGold(socre*-1);
                        dp.setPoint((int) (socre*rate*-1));
                        dp.setPlayPoint( (int) (socre*rate*-1));
					}
				}
			}
		}

		for (DdzPlayer dp : seatMap.values()) {
			int[] action = dp.getInfoArr();
			if(dp.getSeat()==banker){
				dp.changeAction(0,1);//地主次数
			}
			if(dp.getPoint()>0){
				dp.changeAction(1,1);//赢次数
			}

		}

		for (DdzPlayer dtp : seatMap.values()) {
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
				for (DdzPlayer seat : seatMap.values()) {
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
		/// 算分提取于消息前
        if(isOver){
            calcPointBeforeOver();
        }

		if(isGoldRoom()){
			calcGoldRoom();
			for (DdzPlayer dtp : seatMap.values()) {
				dtp.setPlayPoint(dtp.getTotalPoint());
			}
			
		}
		calcAfter();
		ClosingInfoRes.Builder res = sendAccountsMsg(isOver, false, fristSeat, isCt>0);
		saveLog(isOver, 0, res.build());
		if (isOver) {
			calcOver1();
			calcOver2();
			calcOver3();
			diss();

		} else {
			initNext();
			calcOver1();
		}
		outfencard.clear();
	}

//	private void calcGoldRoomActivity(Player player,GoldRoom goldRoom)  {
//		if(isGroupTableGoldRoom() || !isGoldRoom()){
//			return;
//		}
//		try {
//			List<GoldRoomActivityUserItem> result  =   GoldRoomActivityDao.getInstance().loadItemByUserId(player.getUserId());
//			String datestr = DateFormatUtils.format(new Date(),"yyyyMMdd");
//			String cur_keyid =String.valueOf(goldRoom.getConfigId());
//			HashMap<String,Integer>  map = (HashMap<String, Integer>) ResourcesConfigsUtil.getGoldRoomActivityConfig(cur_keyid);
//			int bean =map.get("bean");
//			int rule =map.get("rule");
//			if(bean==0 || rule ==0){
//				return;
//			}
//			List<Activity>  ac = ActivityUtil.getActivityByThem(102);
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//			Date beginDate = ac.get(0).getBeginTime();
//			Date endDate = ac.get(0).getEndTime();
//			Date now = new Date();
//			if(now.compareTo(beginDate)>=0 && now.compareTo(endDate)<=0){
//
//			}else{
//				LogUtil.msgLog.error("GoldRoomActivityDateOutTime|gameConfig|now="+sdf.format(now)+"|ActivityDateBetween["+beginDate+"-"+endDate+"]");
//				return;
//			}
//
//			if(null==result || result.size() == 0){
//				GoldRoomActivityUserItem item = new GoldRoomActivityUserItem();
//				item.setDaterecord(datestr);
//				item.setActivityBureau(1);
//				item.setActivityItemNum(1);
//				item.setActivityDesc("粽子");
//				item.setUserid(player.getUserId());
//				GoldRoomActivityDao.getInstance().saveGoldRoomActivityUserItem(item);
//			}else{
//				GoldRoomActivityUserItem item = result.get(0);
//				long bureau = item.getActivityBureau() + 1;
//				boolean dayfisrtBureau =!item.getDaterecord().contains(datestr);
//				item.setActivityBureau(bureau);
//				if(dayfisrtBureau){
//					//每日首局奖励粽子 根据当前场次
//					item.setActivityItemNum(item.getActivityItemNum()+bean);
//					item.setDaterecord(item.getDaterecord()+","+datestr);
//					player.writeErrMsg(item.getActivityDesc()+" +"+bean);
//				}
//				if(bureau%rule == 0){
//					//场级
//					long beannum = item.getActivityItemNum()+bean;
//					item.setActivityItemNum(beannum);
//					player.writeErrMsg(item.getActivityDesc()+" +"+beannum);
//				}
//				GoldRoomActivityDao.getInstance().updateItem(item);
//			}
//		}catch (Exception e){
//			LogUtil.msgLog.error("GoldRoomActivity|error|"+e.getMessage());
//		}
//	}

	public void calcPointBeforeOver() {
		if(jiaBei == 1){
			int jiaBeiPoint = 0;
			int loserCount = 0;
			for (DdzPlayer player : seatMap.values()) {
				if (player.getPlayPoint() > 0 && player.getPlayPoint() < jiaBeiFen) {
					jiaBeiPoint += player.getPlayPoint() * (jiaBeiShu - 1);
					player.setPlayPoint(player.getPlayPoint() * jiaBeiShu);
				} else if (player.getPlayPoint() < 0) {
					loserCount++;
				}
			}
			if (jiaBeiPoint > 0) {
				for (DdzPlayer player : seatMap.values()) {
					if (player.getPlayPoint() < 0) {
						player.setPlayPoint(player.getPlayPoint() - (jiaBeiPoint / loserCount));
					}
				}
			}
		}
		// 大结算低于below分+belowAdd分
		if (belowAdd > 0 && playerMap.size() == 2) {
			for (DdzPlayer player : seatMap.values()) {
				int totalPoint = player.getPlayPoint();
				if (totalPoint > -below && totalPoint < 0) {
					player.setPlayPoint(player.getPlayPoint() - belowAdd);
				} else if (totalPoint < below && totalPoint > 0) {
					player.setPlayPoint(player.getPlayPoint() + belowAdd);
				}
			}
		}
	}


	/**主动重发牌达5次以上 直接解散*/
	private void dissRoom(){
		calcOver1();
		calcOver2();
		calcCreditNew();
		diss();
	}
	private void commonOver(int score) {
		boolean isWin = true;
	}

	private boolean checkAuto3() {
		boolean diss = false;
		// if(autoPlayGlob==3) {
		boolean diss2 = false;
		for (DdzPlayer seat : seatMap.values()) {
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
			for (DdzPlayer player : playerMap.values()) {
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

			for (DdzPlayer player : playerMap.values()) {
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
			for (DdzPlayer player : playerMap.values()) {
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
		for (DdzPlayer player : seatMap.values()) {
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

		wrapper.putInt(18, op_gametype);
		wrapper.putInt(19, op_fengding);
		wrapper.putInt(20, op_rangpai);
		wrapper.putInt(21, op_bujiabei);
		wrapper.putInt(22, op_dipaijiabei);
		wrapper.putInt(23, is3dai2);
		wrapper.putInt(24, is4dai2);
		wrapper.putInt(25, is3zhang);
		wrapper.putInt(26, op_lunliuJiao);
		wrapper.putInt(27, jiaBei);
		wrapper.putInt(28, jiaBeiFen);
		wrapper.putInt(29, jiaBeiShu);
		//牌桌数据
		wrapper.putInt(30, robTotal);
		wrapper.putInt(31, qiangDzBeiShu);
		wrapper.putInt(32, rangPaiNum);
		wrapper.putString(33, StringUtil.implode(dplist,","));
		wrapper.putInt(34, bankRangPaiNum);
		wrapper.putInt(35, refapainum);
		wrapper.putInt(36, boomBeiShu);
		wrapper.putInt(37, sel_jiabeiBeiShu);
		wrapper.putInt(38, bankRangPaiNumBeiShu);
		wrapper.putInt(39, callSeat);
		wrapper.putInt(40, dpBeiShu);
		wrapper.putString(41, restcards);
		return wrapper;
	}
	@Override
	public void initExtend0(JsonWrapper wrapper) {
		// JsonWrapper wrapper = new JsonWrapper(info);
		for (DdzPlayer player : seatMap.values()) {
			player.initExtend(wrapper.getString(player.getSeat()));
		}
		maxPlayerCount = wrapper.getInt(5, 4);
		if (maxPlayerCount == 0) {
			maxPlayerCount = 2;
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
		op_gametype = wrapper.getInt(18, 0);
		op_fengding = wrapper.getInt(19, 0);
		op_rangpai = wrapper.getInt(20, 0);
		op_bujiabei = wrapper.getInt(21, 0);
		op_dipaijiabei = wrapper.getInt(22, 0);
		is3dai2 = wrapper.getInt(23, 0);
		is4dai2 = wrapper.getInt(24, 0);
		is3zhang = wrapper.getInt(25, 0);
		op_lunliuJiao = wrapper.getInt(26, 0);
		jiaBei = wrapper.getInt(27, 0);
		jiaBeiFen = wrapper.getInt(28, 0);
		jiaBeiShu = wrapper.getInt(29, 0);

		robTotal=wrapper.getInt(30, 1);
		qiangDzBeiShu=wrapper.getInt(31, 1);
		rangPaiNum=wrapper.getInt(32, 1);
		String dp=wrapper.getString(33);
		dplist = StringUtil.explodeToIntList(dp);
		bankRangPaiNum=wrapper.getInt(34, 1);
		refapainum=wrapper.getInt(35, 1);
		boomBeiShu=wrapper.getInt(36, 1);
		sel_jiabeiBeiShu=wrapper.getInt(37, 1);
		bankRangPaiNumBeiShu=wrapper.getInt(38, 1);
		callSeat =wrapper.getInt(39, 0);
		dpBeiShu =wrapper.getInt(40, 0);
		restcards =wrapper.getString(41);

		// res.addExt( boomBeiShu*qiangDzBeiShu*dpBeiShu*sel_jiabeiBeiShu*bankRangPaiNumBeiShu);  // 22总倍数
	}

	protected String buildPlayersInfo() {
		StringBuilder sb = new StringBuilder();
		for (DdzPlayer pdkPlayer : playerMap.values()) {
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
			setTableStatus(DdzConstants.TABLE_STATUS_JIAODIZHU);
			timeNum = 0;
			List<List<Integer>> fplist = CardTool.fapai(2,zp);
			setDplist(fplist.get(3));
			int i = 0;
			randomFenZuCard =fplist.get(4).get(0);
			for (DdzPlayer player :seatMap.values()) {
				player.changeState(player_state.play);
				player.dealHandPais(fplist.get(i), this);
				if (!player.isAutoPlay() || isGoldRoom()) {
					player.setAutoPlay(false, this);
					player.setLastOperateTime(System.currentTimeMillis());
				}
				i++;
			}
			if(playBureau==1){
				if(finishFapai==0){
					//第一句第一次按随机牌先出。
					for (DdzPlayer player :seatMap.values()){
						if(player.getHandPais().contains(randomFenZuCard) && playBureau==1){
							setNowDisCardSeat(player.getSeat());
							setCallSeat(player.getSeat());
							break;
						}
					}
				}else{
					//重新发牌轮流先叫
					randomFenZuCard=0;
					int callseat = getNextSeat(getCallSeat());
					setNowDisCardSeat(callseat);
					setCallSeat(callseat);
				}
			}else{
				if(op_lunliuJiao>0){
					int callseat = getNextSeat(getCallSeat());
					setNowDisCardSeat(callseat);
					setCallSeat(callseat);
				}else{
					setNowDisCardSeat(lastWinSeat);
				}

			}
			finishFapai = 1;
			restcards = fplist.get(2).toString();
		}
//		for (DdzPlayer player :seatMap.values()) {
//			player.dealHandPais(player.getHandPais().subList(0,2), this);
//		}
		//小结算记录


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

	public DdzPlayer getPlayerBySeat(int seat) {
		// int next = seat >= maxPlayerCount ? 1 : seat + 1;
		return seatMap.get(seat);

	}

	/**
	 * 获取下家
	 *
	 * @param seat
	 * @return
	 */
	private DdzPlayer getNextPlayerBySeat(int seat) {
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
			DdzPlayer reconnet_player = null;
			for (DdzPlayer player : playerMap.values()) {
				if (player.getUserId() == userId) {
					reconnet_player = player;
					break;
				}
			}
			int reconnet_player_teamseat = 0;
			if(!isGoldRoom()){
				List<Integer> teamlist = getTeamPlayerSeat(reconnet_player.getSeat());
				if(!teamlist.isEmpty()){
					reconnet_player_teamseat = teamlist.get(0);
				}
			}

			for (DdzPlayer player : playerMap.values()) {
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
						DdzPlayer tp = seatMap.get(teamSeats.get(0));
						playerRes.addAllMoldIds(tp.getHandPais());
					}
				}
				players.add(playerRes.build());
			}
			res.addAllPlayers(players);
//			if (nowDisCardSeat != 0) {
			res.setNextSeat(nowDisCardSeat);
//			}
			// 桌状态  2打牌中
//			if(table_state.play==getState()){
//				res.setRemain(2);
//			}else{
			res.setRemain(getTableStatus());
//			}
			res.setRenshu(this.maxPlayerCount);
			res.addExt(this.payType);// 0支付方式
			res.addExt(banker);// 1 庄座位号
			res.addExt(CommonUtil.isPureNumber(modeId) ? Integer.parseInt(modeId) : 0);// 2
			int ratio;
			int pay;
			ratio = 1;
			pay = consumeCards() ? loadPayConfig(payType) : 0;
			List<Integer> scoreCards = DdzUtil.getScoreCardsList(getTurnCards());
			if (scoreCards.size() > 0) {
				res.addAllScoreCard(scoreCards);
				int totalScore = DdzUtil.getScoreCards(getTurnCards());
				res.addExt(totalScore);//2
			} else {
				res.addExt(0);// 2
			}
			// int nextSeat = getNextDisCardSeat();
			// if(getTableStatus() == DianTuoConstants.TABLE_STATUS_JIAOFEN) {
			// nextSeat = getNextActionSeat();
			// }
			res.addExt(ratio);// 3
			res.addExt(pay);// 4
			res.addExt(lastWinSeat);//5

			res.addExtStr(String.valueOf(matchId));// 0
			res.addExtStr(cardMarkerToJSON());// 1
			res.addTimeOut(autoPlay ? autoTimeOut : 0);//

			res.addExt(playedBureau);// 6
			res.addExt(disCardRound);// 7
			res.addExt(creditMode); // 8
			res.addExt(creditCommissionMode1);// 9
			res.addExt(creditCommissionMode2);// 10
			res.addExt(autoPlay ? 1 : 0);// 11
			res.addExt(tableStatus);// 12
			res.addExt( robTotal);//13 抢地主次数  最高4次
			res.addExt( qiangDzBeiShu);// 14 抢地主倍数 每抢一次地主X2
			res.addExt( rangPaiNum);// 15 让牌张数 每抢一次+1
			res.addExt( dpBeiShu);//16 底牌倍数
			res.addExt( bankRangPaiNum);//17 勾选让牌 地主的让牌张数
			res.addExt( refapainum);//18 重发牌次数 5次后解散
			res.addExt( boomBeiShu);//19 炸弹番数
			res.addExt( sel_jiabeiBeiShu);//21 选择加倍番数
			if(banker==0){
				res.addExt(0);  // 22总倍数
				res.addExt(0);  // 23 总让牌
			}else{
				res.addExt( boomBeiShu*getQiangDzBeiShu()*dpBeiShu*sel_jiabeiBeiShu*bankRangPaiNumBeiShu);  // 22总倍数
				res.addExt( bankRangPaiNum+rangPaiNum);//  23 总让牌
			}
			if(getTableStatus()==2 || getTableStatus()==5 || getTableStatus()==6){
//				res.addExtStr( StringUtil.implode(dplist,","));// 底牌
				res.addStrExt( StringUtil.implode(dplist,","));
			}else{
				List<Integer> d = new ArrayList<>();
				d.add(0);d.add(0);d.add(0);
				res.addStrExt( StringUtil.implode(d,","));//  底牌
			}


		}

		return res.build();
	}

	public int getOnTablePlayerNum() {
		int num = 0;
		for (DdzPlayer player : seatMap.values()) {
			if (player.getIsLeave() == 0) {
				num++;
			}
		}
		return num;
	}


	public void disCards(DdzPlayer player, int action, List<Integer> cards) {

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
			for (DdzPlayer p : seatMap.values()) {
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

		String type4 = DdzSfNew.getCpType2(DdzSfNew.intCardToStringCard(chuPaiCards),is3dai2,is4dai2,is3zhang);
		if("boom".equals(type4) || "wboom".equals(type4)){
			res.setCurScore(2);
		}else{
			res.setCurScore(0);
		}
		boolean turnOver = false;
		int winseat = 0;
		if(!turnOver){
//			if (getActionSeats().isEmpty()|| checkTiqianOver(player, action, guos)) {// 一轮打完
			if ( checkTiqianOver(player)||getActionSeats().isEmpty()) {// 一轮打完
				turnOver = true;
				DdzPlayer winPlayer = seatMap.get(getTurnFirstSeat());
				winseat =winPlayer.getSeat();
				isOver = turnOver(res, winPlayer);
			} else {
				setNowDisCardSeat(getNextPlaySeat(player.getSeat()));
			}
		}
		if(turnOver && action==1 && cards.isEmpty()){
			setNowDisCardSeat(getNextPlaySeat(player.getSeat()));
		}
		addPlayLog(addSandhPlayLog2(player.getSeat(), action, chuPaiCards, turnOver, res.getCurScore(), null,
				getNowDisCardSeat(),code,winseat));

		addGameActionLog(player, "chupai|" + action + "|" + chuPaiCards+"|"+getNowDisCardSeat());

		res.addAllCardIds(chuPaiCards);
		res.setNextSeat(getNowDisCardSeat());
		res.setUserId(player.getUserId() + "");
		res.setSeat(player.getSeat());
		res.setIsPlay(2);
//		res.setIsLet(winseat);//分加在谁身上
		setReplayDisCard();
		if(isOver){
			res.setIsClearDesk(0);
		}
		for (DdzPlayer p : seatMap.values()) {
			p.writeSocket(res.build());
		}


		if (isOver) {
			state = table_state.over;
		}
		if(!isOver){
			checkLaskOut(getNowDisCardIds(),getNowDisCardSeat());
		}

	}

	private void checkLaskOut(List<Integer> deskCards, int nowDisCardSeat) {
		if(null==deskCards || deskCards.isEmpty()){
			//主动出完
			List<Integer> myout = new  ArrayList<>(seatMap.get(nowDisCardSeat).getHandPais());
			String type =  DdzSfNew.getCpType2(DdzSfNew.intCardToStringCard(myout),is3dai2,is4dai2,is3zhang);
			if(!"".equals(type)){
				if("fjddan".equals(type)){
					for(int a:myout){
						int num =CardUtils.getNumByVal(myout,CardUtils.loadCardValue(a));
						if(num>=4){
							//33334444不当飞机甩
							return;
						}
					}
				}
				if("4d2".equals(type)){
					//4带2不主动甩
					return;
				}
				if(myout.size()>2 && myout.contains(501) && myout.contains(502)){
					//不甩王炸
					return;
				}
				addGameActionLog(seatMap.get(nowDisCardSeat),"主动 自动出最后手 牌:"+myout+" | type="+type);
				try {
					//玩家先于系统出最后手 myout =null;处理。
					if(null==myout){
						return;
					}
					playCommand(seatMap.get(nowDisCardSeat),0,myout);
				}catch (Exception e){
					return;
				}
			}
		}else{
			//被动接完
			List<Integer> cpdesk = new ArrayList<>(deskCards);
			String deskType =  DdzSfNew.getCpType2(DdzSfNew.intCardToStringCard(cpdesk),is3dai2,is4dai2,is3zhang);
			List<Integer> myout = new  ArrayList<>(seatMap.get(nowDisCardSeat).getHandPais());
			String type =  DdzSfNew.getCpType2(DdzSfNew.intCardToStringCard(myout),is3dai2,is4dai2,is3zhang);
			if(null==type || "".equals(type)){
				return;
			}
			if("4d2".equals(type)){
				//4带2不主动甩
				return;
			}
			if("fjddan".equals(type)){
				for(int a:myout){
					int num =CardUtils.getNumByVal(myout,CardUtils.loadCardValue(a));
					if(num>=4){
						//33334444不当飞机甩
						return;
					}
				}
			}
			if(myout.size()>2 && myout.contains(501) && myout.contains(502)){
				//不甩王炸
				return;
			}
			boolean result =CardUtils.canChuPai(cpdesk,myout,deskType,type);
			if(result){
				addGameActionLog(seatMap.get(nowDisCardSeat),"被动 自动出最后手 result="+result+"|上家："+getNowDisCardIds()+"|type="+deskType+" | 接牌:"+myout+" | type="+type);
				seatMap.get(nowDisCardSeat).setAutoLast(1);
			}
		}
	}

	private boolean checkTiqianOver( DdzPlayer player) {
		int dizhu = banker;
		int nongming =  getNextSeat(dizhu);
		int syp = seatMap.get(nongming).getHandPais().size();
		if(player.getHandPais().isEmpty()){
			setLastWinSeat(player.getSeat());
			return true;
		}
		if(syp<=(bankRangPaiNum+rangPaiNum)){
			lastWinSeat = nongming;
			setLastWinSeat(lastWinSeat);
			return true;
		}
		return false;
	}
	private int chupai(DdzPlayer player, int action, List<Integer> cards) {

		List<Integer> copy = new ArrayList<>(player.getHandPais());
		copy.removeAll(cards);

		String type = DdzSfNew.getCpType2(DdzSfNew.intCardToStringCard(cards),is3dai2,is4dai2,is3zhang);

		if("".equals(type)){
			player.writeErrMsg("出牌不符合规则。");
			return -1;
		}else{
			if (getNowDisCardIds().size() > 0) {
				List<Integer> cpdesk =new ArrayList<Integer>(getNowDisCardIds());
				List<Integer> myout =new ArrayList<Integer>(cards);
				String deskType =  DdzSfNew.getCpType2(DdzSfNew.intCardToStringCard(cpdesk),is3dai2,is4dai2,is3zhang);
				boolean result =CardUtils.canChuPai(cpdesk,myout,deskType,type);
				addGameActionLog(player,"result="+result+"|上家："+getNowDisCardIds()+"|type="+deskType+" | 接牌:"+myout+" | type="+type);
				if(!result){
					player.writeErrMsg("出牌不符合规则!");
					return -1;
				}
			}
		}

		if("boom".equals(type) || "wboom".equals(type)){
			setBoomBeiShu(boomBeiShu*2);
			player.changeAction(2,1);//炸弹次数
		}
		if(action!=1){
			player.changeCpNum();//出牌次数
		}
		player.addOutPais(cards, this);
		cleanActionState(player.getSeat());
		int xifen=1;
		addTurnCards(cards, false);
		setNowDisCardIds(cards);
		List<Integer> scoreCards = DdzUtil.getScoreCardsList(cards);
		if(!scoreCards.isEmpty()){
			setOutfencard(scoreCards);
		}
		return xifen;
	}



	private boolean turnOver(PlayCardRes.Builder res, DdzPlayer winPlayer) {
		res.setIsClearDesk(1);
		int nextSeat = winPlayer.getSeat();
		setNowDisCardSeat(nextSeat);
		cleanActionState(0);
		addTurnCards(null, true);
		setNowDisCardIds(null);
		return checkTiqianOver(winPlayer);
	}

	private int getNextPlaySeat(int nextSeat) {
		for (int i = 0; i < maxPlayerCount - 1; i++) {
			nextSeat += 1;
			if (nextSeat > maxPlayerCount) {
				nextSeat = 1;
			}
			DdzPlayer nextPlayer = seatMap.get(nextSeat);
			if (nextPlayer.getRank() > 0) {
				continue;
			}
			break;
		}
		return nextSeat;
	}

	/**
	 *
	 * @param disSeat
	 *            不清位置
	 */
	private void cleanActionState(int disSeat) {
		for (DdzPlayer p : seatMap.values()) {
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
	public void playCommand(DdzPlayer player, int action, List<Integer> cards) {
		synchronized (this) {
			if (state != table_state.play) {
				return;
			}
//			// 出牌阶段
//			if (getTableStatus() != DdzConstants.TABLE_STATUS_PLAY) {
//				return;
//			}
			if(null==cards){
				return;
			}
			if (!containCards(player.getHandPais(), cards)) {
				return;
			}
			if (player.getHandPais().isEmpty()) {
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

	public String addSandhPlayLog(int seat, int action, List<Integer> cards, boolean over, int paramfen,
								  List<Integer> fenCards, int nextSeat) {
		JSONObject json = new JSONObject();
		json.put("seat", seat);
		json.put("action", action);
		json.put("vals", cards);
		json.put("param", paramfen);
		if (fenCards != null && over) {
			json.put("fenCards", fenCards);
		}
		json.put("over", over ? 1 : 0);
		json.put("nextSeat", nextSeat);
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

	public int getAutoTimeOut() {
		return autoTimeOut;
	}

	/**
	 * 人数未满或者人员离线
	 *c
	 * @return 0 可以打牌 1人数未满 2人员离线
	 */
	public int isCanPlay() {
		if (seatMap.size() < getMaxPlayerCount()) {
			return 1;
		}
		for (DdzPlayer player : seatMap.values()) {
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

		StringBuilder sb = new StringBuilder("DouDiZhu");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append(player.getName());
		sb.append("|").append("tableType="+tableType);
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
		dplist.clear();
		dpBeiShu=1;
		bankRangPaiNum=0;
		refapainum=0;
		setTableStatus(0);
		robTotal=1;
		qiangDzBeiShu=1;
		rangPaiNum=1;
		dplist = new ArrayList<>();
		dpBeiShu =1;
		bankRangPaiNum=0;
		bankRangPaiNumBeiShu=1;
		refapainum =0;
		boomBeiShu =1;
		sel_jiabeiBeiShu=1;
		restcards ="";
		setCt(0);
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
		banker=0;
		setNowDisCardSeat(nowDisCardSeat);
		for (DdzPlayer tablePlayer : seatMap.values()) {
			DealInfoRes.Builder res = DealInfoRes.newBuilder();
			res.addAllHandCardIds(tablePlayer.getHandPais());
			res.setNextSeat(nowDisCardSeat);//先叫地主的人
			res.setGameType(getWanFa());//
			res.setRemain(getTableStatus());//发牌之前 0.  打牌1
			if(playBureau==1){
				res.setDealDice(getRandomFenZuCard());//第一局亮牌
			}else{
				res.setDealDice(0);//第一局亮牌
			}
			tablePlayer.writeSocket(res.build());
			if (tablePlayer.isAutoPlay()) {
				ArrayList<Integer> val = new ArrayList<>();
				val.add(1);
				addPlayLog(addSandhPlayLog(tablePlayer.getSeat(), DdzConstants.action_tuoguan, val, false, 0, null,
						0));
			}
		}
		setTableStatus(DdzConstants.TABLE_STATUS_JIAODIZHU);

	}

	private void AutoDissRoom() {
		//nowPlay -3
		//elsePlay +1;
		if(playBureau==0||playBureau==1){
			//playedBureau =1;//已结算局数
			for (DdzPlayer p:seatMap.values()){
				p.setPoint(0);
				p.changePlayPoint(0);
			}
		}else{
			for (DdzPlayer p:seatMap.values()){
				p.setPoint(0);
				p.changePlayPoint(0);
			}
		}

		consume();
		boolean isOver =true;
		autoPlayDiss = true;
		calcPointBeforeOver();
		ClosingInfoRes.Builder res = sendAccountsMsg(isOver, false, 0, false);
		saveLog(isOver, 0, res.build());
		calcOver1();
		calcOver2();
		calcCreditNew();
		setTiqianDiss(true);
		diss();
	}

	/**
	 * 叫地主
	 * @param player
	 * @param param 1 叫地主  0不叫
	 */
	public void playJiaoDiZhu(DdzPlayer player,int param) {
		if (nowDisCardSeat != player.getSeat()) {
			LogUtil.msgLog.info(" 操作位置 错误nowDisCardSeat = " + nowDisCardSeat + "actionSeat = " + player.getSeat());
		}
		if(getTableStatus()!=DdzConstants.TABLE_STATUS_JIAODIZHU){
			addGameActionLog(player,player.getName()+player.getSeat()+" |不在叫地主阶段");
			return;
		}
		if(param<0){
			return;
		}
		if(	player.getJiaodizhu()!=-1){
			LogUtil.msgLog.info(" 不能重复叫地主 = " + player.getName() + " getjiaodizhu = " + player.getJiaodizhu());
			return;
		}
		if(param==1){
			banker = player.getSeat();
		}
		player.setJiaodizhu(param);
		addGameActionLog(player,player.getName()+player.getSeat()+" 叫地主：param="+param);
		int isReFapi= 0;
		for (DdzPlayer splayer : seatMap.values()) {
			if(splayer.getJiaodizhu()==0){
				isReFapi++;
			}
		}

		//全不叫 需要重新发牌
		int nextseat =0;
		if(isReFapi==2){
			//叫地主消息
			//A先不叫的重新发牌后由B先选择叫地主 NowDisCardSeat=第一次叫地主的ren
			setNowDisCardSeat(player.getSeat());
			addPlayLog( addSandhPlayLog(player.getSeat(),DdzConstants.TABLE_STATUS_JIAODIZHU,null,false ,param,null,player.getSeat()));

			ComRes.Builder builder2 = SendMsgUtil.buildComRes(WebSocketMsgType.REQ_2RenDDZ_START_CallLandlord,player.getSeat(),param,player.getSeat());
			for (DdzPlayer splayer : seatMap.values()) {
				splayer.writeSocket(builder2.build());
			}

			for (DdzPlayer splayer : seatMap.values()) {
				splayer.setJiaodizhu(-1);
			}

			if(getRefapainum()>=5){
				//5次重发 后解散
				addGameActionLog(player,"|重新发牌次数="+getRefapainum()+"|解散房间");
				AutoDissRoom();
				return;
			}
			fapai();
			playLog="";
			for (int i = 1; i <= getMaxPlayerCount(); i++) {
				DdzPlayer p = seatMap.get(i);
				addPlayLog(StringUtil.implode(player.getHandPais(), ","));
			}
			sendDealMsg(0);
			for (DdzPlayer splayer : seatMap.values()) {
				splayer.setJiaodizhu(-1);
				splayer.setQiangdizhu(-1);
			}
			//重置部分数据
			setRobTotal(1);//抢地主次数  最高4次
			setQiangDzBeiShu(1);//底分倍数 每抢一次地主X2
			setRangPaiNum(1);//让牌张数 每抢一次+1
			setBankRangPaiNum(0);//勾选让牌 地主的让牌张数
			setRefapainum(++refapainum);//重发次数
		}else{
			//是否叫完地主
			int c =0;
			for (DdzPlayer splayer : seatMap.values()) {
				if(splayer.getJiaodizhu()>=1){
					c++;
				}
			}
			if(c>=1){
				//叫地主消息 下家抢
				nextseat = getNextSeat(player.getSeat()) ;
				setNowDisCardSeat(nextseat);
				ComRes.Builder builder3 = SendMsgUtil.buildComRes(WebSocketMsgType.REQ_2RenDDZ_START_CallLandlord,player.getSeat(),param,nextseat);
				for (DdzPlayer splayer : seatMap.values()) {
					splayer.writeSocket(builder3.build());
				}
				addPlayLog(addSandhPlayLog(player.getSeat(),DdzConstants.TABLE_STATUS_JIAODIZHU,null,false ,param,null,nextseat));
				setTableStatus(DdzConstants.TABLE_STATUS_QIANGDIZHU);
				return;
			}

			nextseat = getNextSeat(player.getSeat()) ;
			setNowDisCardSeat(nextseat);
			addPlayLog(addSandhPlayLog(player.getSeat(),DdzConstants.TABLE_STATUS_JIAODIZHU,null,false ,param,null,nextseat));
			if(nextseat==0){
				addGameActionLog(player,"叫地主获取位置错误，下家位置为0");
				return ;
			}else{
				//叫地主消息
				ComRes.Builder builder2 = SendMsgUtil.buildComRes(WebSocketMsgType.REQ_2RenDDZ_START_CallLandlord,player.getSeat(),param,nextseat);
				for (DdzPlayer splayer : seatMap.values()) {
					splayer.writeSocket(builder2.build());
				}
			}
		}
	}

	/**
	 * 抢地主
	 * @param player
	 * @param param -1=默认值 ；0=不抢；1=抢；
	 */
	public void playQiangDiZhu(DdzPlayer player,int param){
		synchronized (this){
			if(param<0){
				return;
			}
			if (nowDisCardSeat != player.getSeat()) {
				LogUtil.msgLog.info(" 操作位置 错误nowDisCardSeat = " + nowDisCardSeat + "actionSeat = " + player.getSeat());
				return;
			}
			if(getTableStatus()!=DdzConstants.TABLE_STATUS_QIANGDIZHU){
				addGameActionLog(player,player.getName()+player.getSeat()+" |不在抢地主阶段");
				return;
			}

			if(param==1){
				robTotal++;
				setRobTotal(robTotal);
				setQiangDzBeiShu(qiangDzBeiShu*2);
				rangPaiNum++;
				setRangPaiNum(rangPaiNum);
			}

			addGameActionLog(player,player.getName()+"|"+player.getSeat()+" 抢地主：param="+param+"|总抢次数="+robTotal+"|底分倍数="+qiangDzBeiShu+"|让牌张数="+rangPaiNum);
			if(param==1){
				player.setQiangdizhu(2);//抢
				banker = player.getSeat();
			}else if(param ==0){
				player.setQiangdizhu(3);//不抢
			}
			int nextseat =0;
			//rob finish
			int robfinish = 0;
			for (DdzPlayer splayer : seatMap.values()) {
				if(splayer.getQiangdizhu()==3){
					robfinish++;
					break;
				}
			}

			if(robfinish==1 || robTotal==5){
				//rob finish
				nextseat=0;
				//抢地主消息

				ComRes.Builder builder2 = SendMsgUtil.buildComRes(WebSocketMsgType.REQ_2RenDDZ_START_RobLandlord,player.getSeat(),param,qiangDzBeiShu,rangPaiNum,nextseat,banker);
				for (DdzPlayer splayer : seatMap.values()) {
					splayer.writeSocket(builder2.build());
				}
				//

				addPlayLog(addSandhPlayLog(player.getSeat(),DdzConstants.TABLE_STATUS_QIANGDIZHU,null,true ,param,null,nextseat));
				//亮底牌 消息。底牌给庄    再选择加倍  再选择让牌。
				setDpBeiShu(CardUtils.getDpBeiShu(getDplist(),op_dipaijiabei));
				List<Integer> handpai = seatMap.get(banker).getHandPais();
				handpai.addAll(dplist);
				seatMap.get(banker).dealHandPais(handpai,this);
				addGameActionLog(player,"底牌="+dplist+" 地主手牌："+handpai);
				setNowDisCardSeat(banker);
				ComRes.Builder dipaiMsg = SendMsgUtil.buildComRes(WebSocketMsgType.REQ_2RenDDZ_DIPAIMSG,banker,getDpBeiShu(),dplist);
				for (DdzPlayer splayer : seatMap.values()) {
					splayer.writeSocket(dipaiMsg.build());
				}

				addPlayLog(addSandhPlayLog(banker,DdzConstants.TABLE_REPLAY_DIPAI_CODE,dplist,true ,getDpBeiShu(),null,banker));
				if(op_bujiabei>0){//不带加倍 直接让牌
					if(op_rangpai>0){
						setTableStatus(DdzConstants.TABLE_STATUS_SELECT_RANGPAI);
					}else{
						setTableStatus(DdzConstants.TABLE_STATUS_PLAY);
					}
					// setTableStatus(DdzConstants.TABLE_STATUS_SELECT_RANGPAI);
				}else{
					setTableStatus(DdzConstants.TABLE_STATUS_SELECT_JIABEI);
				}
			}else{
				nextseat = getNextSeat(player.getSeat());
				setNowDisCardSeat(nextseat);
				//抢地主消息
				ComRes.Builder builder2 = SendMsgUtil.buildComRes(WebSocketMsgType.REQ_2RenDDZ_START_RobLandlord,player.getSeat(),param,qiangDzBeiShu,rangPaiNum,nextseat,0);
				for (DdzPlayer splayer : seatMap.values()) {
					splayer.writeSocket(builder2.build());
				}
				addPlayLog(addSandhPlayLog(player.getSeat(),DdzConstants.TABLE_STATUS_QIANGDIZHU,null,false ,param,null,nextseat));

			}


		}
	}

	/**
	 * 选择加倍
	 * @param player
	 * @param param -1=默认值 ；1=不加倍；2=2倍；
	 */
	public void playSelectJiaBei(DdzPlayer player,int param){
		if(param<0){
			return;
		}

		if(player.getSelJaiBei()>0){
			addGameActionLog(player,player.getName()+player.getSeat()+" |不能重复加倍");
			return;
		}
		if(getTableStatus()!=DdzConstants.TABLE_STATUS_SELECT_JIABEI){
			addGameActionLog(player,player.getName()+player.getSeat()+" |不在加倍阶段");
			return;
		}

		player.setSelJaiBei(param);
		setSel_jiabeiBeiShu(sel_jiabeiBeiShu*param);
		addGameActionLog(player,player.getName()+"|"+player.getSeat()+"|选择加倍="+param+"|加倍番="+sel_jiabeiBeiShu);
		int count=0;
		for(DdzPlayer p:seatMap.values()){
			if(p.getSelJaiBei()>0){
				count++;
			}
		}
		if(count==2){
			//选择加倍结束  再选择让牌
			int nextseat = banker;
			setNowDisCardSeat(nextseat);
			addPlayLog(addSandhPlayLog(player.getSeat(),DdzConstants.TABLE_STATUS_SELECT_JIABEI,null,true ,param,null,nextseat));
			if(op_rangpai>0){
				addGameActionLog(player,player.getName()+"|"+player.getSeat()+"|加倍结束，进入让牌");
				//若勾选让牌选项，地主可自由选择让n张牌或不让，地主选择不让则由地主先出；选择让牌则由农民先出；
				setTableStatus(DdzConstants.TABLE_STATUS_SELECT_RANGPAI);
				setNowDisCardSeat(banker);
				ComRes.Builder builder2 = SendMsgUtil.buildComRes(WebSocketMsgType.REQ_2RenDDZ_SelJiaBei,player.getSeat(),param,nextseat,op_rangpai);
				for (DdzPlayer splayer : seatMap.values()) {
					splayer.writeSocket(builder2.build());
				}
			}else{
				addGameActionLog(player,player.getName()+"|"+player.getSeat()+"|加倍结束 玩家出牌");
				setTableStatus(DdzConstants.TABLE_STATUS_PLAY);
				setNowDisCardSeat(banker);
				ComRes.Builder builder2 = SendMsgUtil.buildComRes(WebSocketMsgType.REQ_2RenDDZ_SelJiaBei,player.getSeat(),param,banker,1);
				for (DdzPlayer splayer : seatMap.values()) {
					splayer.writeSocket(builder2.build());
				}
			}
		}else{
			int nextseat = getNextSeat(player.getSeat());
			setNowDisCardSeat(nextseat);
			ComRes.Builder builder2 = SendMsgUtil.buildComRes(WebSocketMsgType.REQ_2RenDDZ_SelJiaBei,player.getSeat(),param,nextseat,0,0);
			for (DdzPlayer splayer : seatMap.values()) {
				splayer.writeSocket(builder2.build());
			}
			addPlayLog(addSandhPlayLog(player.getSeat(),DdzConstants.TABLE_STATUS_SELECT_JIABEI,null,false ,param,null,nextseat));
		}
	}


	/**
	 * 地主选择让牌
	 * @param player
	 * @param param -1=默认值 ；param=0=不让； ；
	 */
	public void bankerRangPai(DdzPlayer player,int param){
		if(op_rangpai==0){
			LogUtil.msgLog.info(player.getName()+"|"+player.getSeat()+" 操作 错误 不让牌玩法 ");
			return;
		}

		if (nowDisCardSeat != player.getSeat()) {
			LogUtil.msgLog.info(player.getName()+"|"+player.getSeat()+" 操作位置 错误nowDisCardSeat = " + nowDisCardSeat + "actionSeat = " + player.getSeat());
			return;
		}
		if(getTableStatus()!=DdzConstants.TABLE_STATUS_SELECT_RANGPAI){
			addGameActionLog(player,player.getName()+"|"+player.getSeat()+" |不在让牌阶段");
			return;
		}
		int nextseat =0;
		int bs =1;
		if(param>0){
			//让 =农民先出
			setBankRangPaiNum(param);
			//让牌翻倍
			setBankRangPaiNumBeiShu(param);
			bs =(int)Math.pow(2,param);
			setBankRangPaiNumBeiShu(bs);
			nextseat=getNextSeat(banker);
			setNowDisCardSeat(nextseat);
		}else if(param==0){
			//不让 =地主先出
			nextseat=banker;
			setNowDisCardSeat(banker);
		}
		addGameActionLog(player,"|让牌="+param+"|让牌番数="+getBankRangPaiNumBeiShu());
		//让牌消息
		ComRes.Builder builder2 = SendMsgUtil.buildComRes(WebSocketMsgType.REQ_2RenDDZ_RangPai,player.getSeat(),param,nextseat,bs);
		for (DdzPlayer splayer : seatMap.values()) {
			splayer.writeSocket(builder2.build());
		}
		addPlayLog(addSandhPlayLog(player.getSeat(),DdzConstants.TABLE_STATUS_SELECT_RANGPAI,null,true ,param,null,bs));
		setTableStatus(DdzConstants.TABLE_STATUS_PLAY);
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
		return createTable(new CreateTableInfo(player, TABLE_TYPE_NORMAL, play, bureauCount, params, strParams, true));
		//return createTable(player, play, bureauCount, params, saveDb);

	}

	public void createTable(Player player, int play, int bureauCount, List<Integer> params) throws Exception {
//		createTable(player, play, bureauCount, params, true);
		createTable(new CreateTableInfo(player, TABLE_TYPE_NORMAL, play, bureauCount, params, strParams, true));
	}


	public boolean createTable(CreateTableInfo createTableInfo)
			throws Exception {
		Player player = createTableInfo.getPlayer();
		int play = createTableInfo.getPlayType();
		int bureauCount =createTableInfo.getBureauCount();
		int tableType = createTableInfo.getTableType();
		List<Integer> params = createTableInfo.getIntParams();
		List<String> strParams = createTableInfo.getStrParams();
		boolean saveDb = createTableInfo.isSaveDb();

		long id = getCreateTableId(player.getUserId(), play);
		TableInf info = new TableInf();
		info.setTableId(id);
		info.setTableType(tableType);
		info.setMasterId(player.getUserId());
		info.setRoomId(0);
		info.setPlayType(play);
		info.setTotalBureau(bureauCount);
		info.setPlayBureau(1);
		info.setServerId(GameServerConfig.SERVER_ID);
		info.setCreateTime(new Date());
		info.setDaikaiTableId(daikaiTableId);
		info.setExtend(buildExtend());
		TableDao.getInstance().save(info);
		loadFromDB(info);

		payType = StringUtil.getIntValue(params, 2, 1);// 1AA,2房主
		op_fengding =StringUtil.getIntValue(params, 3, 24);// 封顶
		op_rangpai =StringUtil.getIntValue(params, 4, 0);//0,2,3 4
		is3zhang = StringUtil.getIntValue(params, 5, 0);// 3张
		is3dai2 = StringUtil.getIntValue(params, 6, 0);// 三带2
		maxPlayerCount = StringUtil.getIntValue(params, 7, 2);// 人数
		is4dai2 = StringUtil.getIntValue(params, 8, 0);// 4带2
		op_dipaijiabei =StringUtil.getIntValue(params, 9, 0);// 底牌翻倍
		op_bujiabei =StringUtil.getIntValue(params, 10, 0);// 0不带加倍
		op_lunliuJiao =StringUtil.getIntValue(params, 11, 0);// 轮流先叫
		if (maxPlayerCount == 0) {
			maxPlayerCount = 2;
		}
		int time = StringUtil.getIntValue(params, 12, 0);
		this.autoPlay = time > 1;
		autoPlayGlob = StringUtil.getIntValue(params, 13, 0);// 1单局  2整局  3三局
		if (time > 0) {
			autoTimeOut2 = autoTimeOut = (time * 1000);
		}
		op_gametype =1;//StringUtil.getIntValue(params, 11, 1);// 1=叫地主 2=叫分
		this.jiaBei = StringUtil.getIntValue(params, 14, 0);
		this.jiaBeiFen = StringUtil.getIntValue(params, 15, 100);
		this.jiaBeiShu = StringUtil.getIntValue(params, 16, 1);

		if(this.getMaxPlayerCount() != 2){
			jiaBei = 0 ;
		}
		if(maxPlayerCount==2){
			belowAdd = StringUtil.getIntValue(params, 17, 0);
			below = StringUtil.getIntValue(params, 18, 0);
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

	/**
	 * 发送结算msg
	 *
	 * @param over
	 *            是否已经结束
	 * @param isCt
	 *            赢的玩家
	 * @return
	 */
	public ClosingInfoRes.Builder sendAccountsMsg(boolean over, boolean isBreak, int firstSeat, boolean isCt) {
		List<ClosingPlayerInfoRes> list = new ArrayList<>();
		List<ClosingPlayerInfoRes.Builder> builderList = new ArrayList<>();

		for (DdzPlayer player : seatMap.values()) {
			ClosingPlayerInfoRes.Builder build = null;
			if (over) {
				build = player.bulidTotalClosingPlayerInfoRes();
			} else {
				build = player.bulidOneClosingPlayerInfoRes();

			}
			if (player.getSeat() == firstSeat) {
				build.setIsHu(1);//win ? 1 : 0
			} else {
				build.setIsHu(0);
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
			for (DdzPlayer player : seatMap.values()) {
				if (player.getWinLoseCredit() > dyjCredit) {
					dyjCredit = player.getWinLoseCredit();
				}
			}
			for (ClosingPlayerInfoRes.Builder builder : builderList) {
				DdzPlayer player = seatMap.get(builder.getSeat());
				//calcCommissionCreditDdz(player, dyjCredit);
				calcCommissionCredit(player, dyjCredit);
				builder.addExt(player.getWinLoseCredit() + ""); // 10
				builder.addExt(player.getCommissionCredit() + ""); // 11

				// 2019-02-26更新
				builder.setWinLoseCredit(player.getWinLoseCredit());
				builder.setCommissionCredit(player.getCommissionCredit());
			}
		} else if (isGroupTableGoldRoom()) {
			// -----------亲友圈金币场---------------------------------
			for (DdzPlayer player : seatMap.values()) {
				player.setWinGold(player.getPlayPoint() * gtgDifen);
			}
			calcGroupTableGoldRoomWinLimit();
			for (ClosingPlayerInfoRes.Builder builder : builderList) {
				DdzPlayer player = seatMap.get(builder.getSeat());
				builder.addExt(player.getWinLoseCredit() + ""); // 10
				builder.addExt(player.getCommissionCredit() + ""); // 11
				builder.setWinLoseCredit(player.getWinGold());
			}
		} else {
			for (ClosingPlayerInfoRes.Builder builder : builderList) {
				DdzPlayer player = seatMap.get(builder.getSeat());
				builder.addExt(0 + ""); // 10
				builder.addExt(0 + ""); // 11
			}
		}
		for (ClosingPlayerInfoRes.Builder builder : builderList) {
			DdzPlayer player = seatMap.get(builder.getSeat());
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
		res.addExt(restcards==null?"":restcards );//2020年12月10日 2人斗地主小结算显示未发出来的排
		for (DdzPlayer player : seatMap.values()) {
			if(over){
				player.setTotalPoint(player.getPlayPoint());
			}
			player.writeSocket(res.build());
		}


		return res;
	}

	public void calcCommissionCreditDdz(DdzPlayer player, long dyjCredit) {
		player.setCommissionCredit(0);
		if(AAScoure == 100 ){//AA赠送分=100
			// 2020年11月19日 10:01:07  AA赠送分   从每个玩家抽成
			long _AACreditSum = 0;
			for (DdzPlayer p:seatMap.values() ) {
				if(p.getPlayPoint()>0){
					_AACreditSum +=p.getPlayPoint();//斗地主 总分
				}
			}

			_AACreditSum = _AACreditSum * creditDifen;
			long commissionCredit = 0;
			// 总赢分 大于初始赠送
			if(_AACreditSum>creditCommissionLimit){
				//直接抽成 creditCommission
				commissionCredit =creditCommission;
			}else{
				//抽保底
				long baoDiCredit = calcBaoDi(_AACreditSum);
				if (baoDiCredit <= 0) {
					// 不抽
					return;
				}
				commissionCredit = baoDiCredit;
			}
			player.setCommissionCredit(commissionCredit);
			player.setWinLoseCredit(player.getWinLoseCredit()-commissionCredit);
			return;
		}
		long credit = player.getWinLoseCredit();
		long preCredit = credit;
		if (credit <= 0) {
			return;
		}
		if (creditCommissionMode2 == 1 && credit < dyjCredit) {
			return;
		}
		int tmpCount = 0;
		if (this.dyjCount == 0) {
			for (Player p : getSeatMap().values()) {
				if (p.getWinLoseCredit() == dyjCredit) {
					tmpCount++;
				}
			}
			this.dyjCount = tmpCount;
		}
		long commissionCredit = 0;
		isBaoDiCommission = false;
		if (credit <= creditCommissionLimit) {
			// 保底抽水
			long baoDiCredit = calcBaoDi(credit);
			if (baoDiCredit <= 0) {
				// 不抽
				return;
			}
			commissionCredit = credit > baoDiCredit ? baoDiCredit : credit;
			isBaoDiCommission = true;
		}else {
			//佣金
			if (creditCommissionMode1 == 1) {
				//固定数量佣金
				if (creditCommissionMode2 == 1) {
					//大赢家
					if (credit >= dyjCredit && dyjCredit > 0) {
						if (credit >= creditCommission) {
							commissionCredit = creditCommission;
						} else {
							commissionCredit = credit;
						}
					}
				} else {
					//全部赢家
					if (credit > 0) {
						if (credit >= creditCommission) {
							commissionCredit = creditCommission;
						} else {
							commissionCredit = credit;
						}
					}
				}
			} else {
				//按比例交佣金
				if (creditCommissionMode2 == 1) {
					//大赢家
					if (credit >= dyjCredit && dyjCredit > 0) {
						long commission = credit * creditCommission / 100;
						if (credit >= commission) {
							commissionCredit = commission;
						} else {
							commissionCredit = credit;
						}
					}
				} else {
					//全部赢家
					if (credit > 0) {
						long commission = credit * creditCommission / 100;
						if (credit >= commission) {
							commissionCredit = commission;
						} else {
							commissionCredit = credit;
						}
					}
				}
			}
		}
		if (preCredit == dyjCredit && dyjCount > 1) {
			commissionCredit = (long) Math.ceil((commissionCredit * 1d) / (this.dyjCount * 1d));
		}
		credit = credit > commissionCredit ? credit - commissionCredit : 0;
		player.setCommissionCredit(commissionCredit);
		player.setWinLoseCredit(credit);
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
        calcPointBeforeOver();
		ClosingInfoRes.Builder builder = sendAccountsMsg(true, true, 0,false);
		saveLog(true, 0l, builder.build());
	}

	@Override
	public Class<? extends Player> getPlayerClass() {
		return DdzPlayer.class;
	}

	@Override
	public int getWanFa() {
		return GameUtil.play_type_pk_2renddz;
	}

	@Override
	public void checkReconnect(Player player) {
		DdzTable table = player.getPlayingTable(DdzTable.class);
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
				for (DdzPlayer player : seatMap.values()) {
					if (player.getLastCheckTime() > 0) {
						player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
					}
				}
				return;
			}

			if (isAutoPlayOff()) {
				// 托管关闭
				for (int seat : seatMap.keySet()) {
					DdzPlayer player = seatMap.get(seat);
					player.setAutoPlay(false, this);
				}
				return;
			}
			// 准备托管
			if (state == table_state.ready && playedBureau > 0) {
				++timeNum;
				for (DdzPlayer player : seatMap.values()) {
					// 玩家进入托管后，5秒自动准备
					if (timeNum >= 5 && player.isAutoPlay()) {
						autoReady(player);
					} else if (timeNum >= 30) {
						autoReady(player);
					}
				}
				return;
			}

			DdzPlayer player = seatMap.get(nowDisCardSeat);
			if (player == null) {
				return;
			}

			if (getTableStatus() == 0 || state != table_state.play) {
				return;
			}

			//被动甩最后手
			if(player.getAutoLast()==1){
				List<Integer> ccp = new ArrayList<>(player.getHandPais());
				playCommand(player,0,ccp);
				player.setAutoLast(0);
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
				auto = checkPlayerAuto(player, timeout);
			}

			if (auto || player.isRobot()) {
				player.setAutoPlayTime(0L);
				if (state == table_state.play) {
					if (getTableStatus() == DdzConstants.TABLE_STATUS_PLAY) {
						List<Integer> curList = new ArrayList<>(player.getHandPais());
						if (curList.isEmpty()) {
							return;
						}
						int size = getTurnCards().size();
						int action = 0;
						List<Integer> disList = new ArrayList<Integer>();
						if (size != 0) {
							action = 1;//要不起
						} else {
							disList.add(curList.get(0));
						}
						// 轮首次出牌
						playCommand(player, action, disList);
					}else if(getTableStatus()==DdzConstants.TABLE_STATUS_JIAODIZHU){
						playJiaoDiZhu(player,0);
					}else if(getTableStatus()==DdzConstants.TABLE_STATUS_QIANGDIZHU){
//						if(banker==0){
//							playQiangDiZhu(player,1);
//						}else{
						playQiangDiZhu(player,0); //不抢
						//}
					}else if(getTableStatus()==DdzConstants.TABLE_STATUS_SELECT_JIABEI){
						playSelectJiaBei(player,1); //1不加倍
					}else if(getTableStatus()==DdzConstants.TABLE_STATUS_SELECT_RANGPAI){
						bankerRangPai(player,0);
					}
				}
			}
		}
	}


	public boolean checkPlayerAuto(DdzPlayer player, int timeout) {
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
		for (Map.Entry<Integer, DdzPlayer> entry : seatMap.entrySet()) {
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
		return "2人斗地主";
	}

	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_pk_2renddz);//GameUtil.play_type_pk_ddz=270

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
			for (DdzPlayer player : seatMap.values()) {
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

	public void playChangeSeat() {

	}
	public void setPlayerMap( Map<Long, DdzPlayer>  p) {
		this.playerMap = p;
	}

	public void setSeatMap(Map<Integer, DdzPlayer> map) {
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

	public int getRobTotal() {
		return robTotal;
	}

	public void setRobTotal(int robTotal) {
		this.robTotal = robTotal;
		changeExtend();
	}

	public int getQiangDzBeiShu() {
		return qiangDzBeiShu;
	}

	public void setQiangDzBeiShu(int qiangDzBeiShu) {
		this.qiangDzBeiShu = qiangDzBeiShu;
		changeExtend();
	}

	public int getRangPaiNum() {
		return rangPaiNum;
	}

	public void setRangPaiNum(int rangPaiNum) {
		this.rangPaiNum = rangPaiNum;
		changeExtend();
	}

	public List<Integer> getDplist() {
		return dplist;
	}

	public void setDplist(List<Integer> dplist) {
		this.dplist = dplist;
		changeExtend();
	}

	public int getBankRangPaiNum() {
		return bankRangPaiNum;
	}
	public int getBankRangPaiNumBeiShu() {
		return bankRangPaiNumBeiShu;
	}
	public void setBankRangPaiNum(int bankRangPaiNum) {
		this.bankRangPaiNum = bankRangPaiNum;
		changeExtend();
	}
	public void setBankRangPaiNumBeiShu(int bankRangPaiNumBeiShu) {
		this.bankRangPaiNumBeiShu = bankRangPaiNumBeiShu;
		changeExtend();
	}

	public int getRefapainum() {
		return refapainum;
	}

	public void setRefapainum(int refapainum) {
		this.refapainum = refapainum;
		changeExtend();
	}

	public int getDpBeiShu() {
		return dpBeiShu;
	}

	public void setDpBeiShu(int dpBeiShu) {
		this.dpBeiShu = dpBeiShu;
		changeExtend();
	}

	public int getBoomBeiShu() {
		return boomBeiShu;
	}

	public void setBoomBeiShu(int boomBeiShu) {
		this.boomBeiShu = boomBeiShu;
		changeExtend();
	}

	public int getSel_jiabeiBeiShu() {
		return sel_jiabeiBeiShu;
	}

	public void setSel_jiabeiBeiShu(int sel_jiabeiBeiShu) {
		this.sel_jiabeiBeiShu = sel_jiabeiBeiShu;
		changeExtend();
	}

	public int isCt() {
		return isCt;
	}

	public void setCt(int ct) {
		isCt = ct;
	}

	public int getCallSeat() {
		return callSeat;
	}

	public void setCallSeat(int callSeat) {
		this.callSeat = callSeat;
		changeExtend();
	}
}
