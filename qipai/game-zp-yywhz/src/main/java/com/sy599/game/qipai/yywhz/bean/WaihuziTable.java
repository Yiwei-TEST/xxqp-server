package com.sy599.game.qipai.yywhz.bean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserGroupPlaylog;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiInfo;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayPaohuziRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.TingPaiRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzInfoRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.yywhz.constant.GuihzCard;
import com.sy599.game.qipai.yywhz.constant.WaihuziConstant;
import com.sy599.game.qipai.yywhz.rule.GuihuziIndex;
import com.sy599.game.qipai.yywhz.rule.GuihuziMenzi;
import com.sy599.game.qipai.yywhz.rule.GuihzCardIndexArr;
import com.sy599.game.qipai.yywhz.rule.RobotAI;
import com.sy599.game.qipai.yywhz.tool.GuihuziHuLack;
import com.sy599.game.qipai.yywhz.tool.GuihuziResTool;
import com.sy599.game.qipai.yywhz.tool.GuihuziTool;
import com.sy599.game.staticdata.KeyValuePair;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.GameConfigUtil;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.PayConfigUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

/**
 * @author liuping
 * ???????????????
 */
public class WaihuziTable extends BaseTable {
	/**
	 * ??????map
	 */
	private Map<Long, WaihuziPlayer> playerMap = new ConcurrentHashMap<>();
	/**
	 * ?????????????????????
	 */
	private Map<Integer, WaihuziPlayer> seatMap = new ConcurrentHashMap<>();
	/**
	 * 0???  1??? 2??? 3??? 4 ??? 5??? 6???????????????
	 */
	private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
	/**
	 * ??????????????????????????????
	 * 	 ??????????????????????????????????????? 
	 *  1??????????????????????????????????????? ??????????????????????????? ??????????????????
	 *  2???????????????????????????????????????????????????????????????????????????????????????????????? ?????????????????????????????????????????? ?????????????????????????????????????????????
	 */
	private Map<Integer, WaihzTempAction> tempActionMap = new ConcurrentHashMap<>();
	/**
	 * ??????????????????
	 */
	private List<Integer> startLeftCards = new ArrayList<>();
	/**
	 * ??????????????????
	 */
	private List<GuihzCard> leftCards = new ArrayList<>();
	/**
	 * ??????????????????
	 */
	private List<GuihzCard> nowDisCardIds = new ArrayList<>();
	/**
	 * ??????flag
	 */
	private int moFlag;
	/**
	 * ??????????????????flag
	 */
	private int toPlayCardFlag;
	/**
	 * ????????????????????????
	 */
	private int moSeat;
	/**
	 * ?????????????????????
	 */
	private GuihzCard beRemoveCard;
	/**
	 * ????????????
	 */
	private int maxPlayerCount = 3;
	/**
	 * ????????????????????????
	 */
	private List<Integer> huConfirmList = new ArrayList<>();
	/**
	 * ????????????????????????
	 */
	private KeyValuePair<Integer, Integer> moSeatPair;
	/**
	 * ????????????????????????
	 */
	private KeyValuePair<Integer, Integer> checkMoMark;
	private volatile int timeNum = 0;
	
	private int tableStatus;// ???????????? 1??????
	
	
    /**
     * ????????????
     */
    private volatile int autoTimeOut = Integer.MAX_VALUE;
    private volatile int autoTimeOut2 = Integer.MAX_VALUE;
    private volatile int autoTimeOut3 = Integer.MAX_VALUE;
	
	/**
	 * ??????????????????
	 */
	private boolean firstCard = true;
	/**
	 * ????????????  100??? 200???
	 */
	private int maxhuxi;
	/**
	 * true?????? false?????????
	 */
	private boolean kepaop;
	/**
	 * true???????????? false???????????????
	 */
	private boolean wuxiping;
	/**
	 * true???????????? false??????
	 */
	private boolean diaodiaoshou;
	
	
	/**
	 * 1??????  2?????????
	 */
	private int kawai;
	
	/**?????? 1????????? 2??????10 3??????20 */
	private int maiPai;
	
	/**????????????*/
	private int xianjiaDiHu;
	
	/**????????????*/
	private int HuDaWai;
	
	
	/**????????????*/
	private int zhuangjiaDiHu;
	
	
	
	
	/**
	 * ????????????
	 */
	private boolean opreAc = false;
	
	
	/**???????????????*/
	private int suijiZhuang;
	
	
	/**?????? 1???10/20/30  2???20/30/40*/
	private int haoFen;
	
	/**?????? 1???60/80/100 2???80/100/120*/
	private int mingtang;
	
	
	/**?????? 0????????? */
    //?????? 1??????  2??? 1/2/3 3???2/3/5
    private int piaoFen=0;
	
	/**?????? 0?????????*/
	private int tuoguan;
	
	
	
	
    //???????????????0??????1???
    private int jiaBei;
    //?????????????????????xx???????????????
    private int jiaBeiFen;
    //????????????????????????
    private int jiaBeiShu;
    
    
    /**??????1????????????2?????????*/
    private int autoPlayGlob;
	
	
	public List<Integer> getStartLeftCards() {
		return startLeftCards;
	}

	private long groupPaylogId = 0;  //?????????????????????id

	/**
	 * ?????? ??????-????????????
	 */
//	private Map<Integer,List<Integer>> tingMap = new HashMap<>();
//
//	public Map<Integer, List<Integer>> getTingMap() {
//		return tingMap;
//	}
//
//	public void setTingMap(Map<Integer, List<Integer>> tingMap) {
//		this.tingMap = tingMap;
//	}
//
//	public void putTingMap(int seat, List<Integer> values){
//		this.tingMap.put(seat, values);
//	}

	@Override
	public void initExtend0(JsonWrapper wrapper) {
		String hu = wrapper.getString(1);
		if (!StringUtils.isBlank(hu)) {
			huConfirmList = StringUtil.explodeToIntList(hu);
		}
		moFlag = wrapper.getInt(2, 0);
		toPlayCardFlag = wrapper.getInt(3, 0);
		moSeat = wrapper.getInt(4, 0);
		String moSeatVal = wrapper.getString(5);
		if (!StringUtils.isBlank(moSeatVal)) {
			moSeatPair = new KeyValuePair<>();
			String[] values = moSeatVal.split("_");
			String idStr = StringUtil.getValue(values, 0);
			if (!StringUtil.isBlank(idStr)) {
				moSeatPair.setId(Integer.parseInt(idStr));
			}
			moSeatPair.setValue(StringUtil.getIntValue(values, 1));
		}
		firstCard = wrapper.getInt(6, 1) == 1 ? true : false;
		beRemoveCard = GuihzCard.getPaohzCard(wrapper.getInt(7, 0));
		maxPlayerCount = wrapper.getInt(8, 3);
		maxhuxi = wrapper.getInt(9, 200);// ???????????????
//		kepaop =  false;//wrapper.getInt(10, 1) == 1 ? true :
//		wuxiping = wrapper.getInt(11, 1) == 1 ? true : false;
//		isAAConsume = Boolean.parseBoolean(wrapper.getString(12));
//		diaodiaoshou = wrapper.getInt(13, 1) == 1 ? true : false;
		startLeftCards = loadStartLeftCards(wrapper.getString("startLeftCards"));
		tempActionMap = loadTempActionMap(wrapper.getString("tempActions"));
//		groupPaylogId = wrapper.getLong(14, 0);
		opreAc = wrapper.getInt(24, 0) == 1 ? true : false;
		
		
		
		
		kawai = wrapper.getInt(10, 0);
		maiPai = wrapper.getInt(11, 0);
		xianjiaDiHu = wrapper.getInt(12, 0);
		HuDaWai = wrapper.getInt(13, 0);
		zhuangjiaDiHu = wrapper.getInt(14, 0);
		suijiZhuang = wrapper.getInt(15, 0);
		haoFen = wrapper.getInt(16, 0);
		mingtang = wrapper.getInt(17, 0);
		piaoFen = wrapper.getInt(18, 0);
		tuoguan = wrapper.getInt(19, 0);
		autoPlayGlob = wrapper.getInt(20, 0);

		

		
		
		
        jiaBei = wrapper.getInt(21, 0);
        jiaBeiFen = wrapper.getInt(22, 0);
        jiaBeiShu = wrapper.getInt(23, 0);
        
        autoTimeOut2 =autoTimeOut = (tuoguan*1000);
		
		
//		String tingStr = wrapper.getString(15);
//		if (!StringUtils.isBlank(tingStr)){
//			String[] tings = tingStr.split(";");
//			for(String ting : tings){
//				String[] seatTing = ting.split("_");
//				int seat = Integer.parseInt(seatTing[0]);
//				List<Integer> values = new ArrayList<>();
//				if (!StringUtils.isBlank(seatTing[1])) {
//					values = StringUtil.explodeToIntList(seatTing[1]);
//				}
//				tingMap.put(seat, values);
//			}
//		}
	}
	
	private Map<Integer, WaihzTempAction> loadTempActionMap(String json) {
		Map<Integer, WaihzTempAction> map = new ConcurrentHashMap<>();
		if (json == null || json.isEmpty())
			return map;
		JSONArray jsonArray = JSONArray.parseArray(json);
		for (Object val : jsonArray) {
			String str = val.toString();
			WaihzTempAction tempAction = new WaihzTempAction();
			tempAction.initData(str);
			map.put(tempAction.getSeat(), tempAction);
		}
		return map;
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

    @SuppressWarnings("unchecked")
	@Override
	public <T> T getPlayer(long id, Class<T> cl) {
		return (T) playerMap.get(id);
	}

	@Override
	protected void initNowAction(String nowAction) {
		JsonWrapper wrapper = new JsonWrapper(nowAction);
		String val1 = wrapper.getString(1);
		if (!StringUtils.isBlank(val1)) {
			actionSeatMap = DataMapUtil.toListMap(val1);
		}
	}

	@Override
	protected String buildNowAction() {
		JsonWrapper wrapper = new JsonWrapper("");
		wrapper.putString(1, DataMapUtil.explodeListMap(actionSeatMap));
		return wrapper.toString();
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
	public void calcOver() {
		if (state == table_state.ready) {
			return;
		}
		boolean isHuangZhuang = false;
		List<Integer> winList = new ArrayList<>(huConfirmList);
		if (winList.size() == 0 && leftCards.size() == 0) {// ??????
			isHuangZhuang = true;
		}
		int maxFan = 1;
		
		int winPoint = 0;
		int totalPiao =0;
		for (int winSeat : winList) {// ????????????
			WaihuziPlayer winPlayer = seatMap.get(winSeat);
			winPoint = winPlayer.getLostPoint();
			int getPoint = 0;
			for (int seat : seatMap.keySet()) {
				if (!winList.contains(seat)) {
					WaihuziPlayer player = seatMap.get(seat);
					int piao = 0;
					
					// ??????
					if (piaoFen >= 1) {
						piao= (winPlayer.getPiaoPoint() + player.getPiaoPoint());
					}

					int lose = winPoint + piao;
					totalPiao +=piao;
					getPoint += lose;
					player.addPiaoPoint(-piao);
					player.calcResult(1, -lose, isHuangZhuang);
				}
			}
			winPlayer.addPiaoPoint(totalPiao);
			winPlayer.calcResult(1, getPoint, isHuangZhuang);
			winPlayer.changeDahuCounts(14, totalPiao);
		}
		WaihuziPlayer winPlayer = null;
		boolean selfMo = false;
		if (!winList.isEmpty()) {
			winPlayer = seatMap.get(winList.get(0));
			selfMo = winPlayer.getSeat() == moSeat;
		}
		boolean isOver = playBureau >= totalBureau;
		
		
        if(autoPlayGlob >0) {
//          //????????????
            boolean diss = false;
            if(autoPlayGlob ==1) {
            	 for (WaihuziPlayer seat : seatMap.values()) {
                 	if(seat.isAutoPlay()) {
                     	diss = true;
                     	break;
                     }
                 }
            }
            if(diss) {
            	 autoPlayDiss= true;
            	 isOver =true;
            }
        }
		
      
        List<Integer> mt = null;
        if(winPlayer!=null) {
//        	mt =  winPlayer.getDahu();
        	mt =winPlayer.getdahuList();
        	winPlayer.addMingtCount();
        	//.addAllDahuCounts(winPlayer.getdahuList());
        }
        ClosingPhzInfoRes.Builder res = sendAccountsMsg(isOver, selfMo, winList, maxFan, mt, winPoint, false);
		saveLog(isOver,0L, res.build());
		if (!winList.isEmpty()) {
			setLastWinSeat(winList.get(0));
		} else {
			int next = getNextSeat(lastWinSeat);
			setLastWinSeat(next);
		}
		calcAfter();
		if (isOver) {
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

	public void saveLog(boolean over, long winId, Object resObject) {
		LogUtil.d_msg("tableId:" + id + " play:" + playBureau + " over:" + resObject);
		ClosingPhzInfoRes res = (ClosingPhzInfoRes) resObject;
		 String logRes = JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResLog(res));
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
		userLog.setUserId(creatorId);
		userLog.setExtend(logOtherRes);

		long logId = TableLogDao.getInstance().save(userLog);
		if(isGroupRoom()){
			UserGroupPlaylog userGroupLog =  new UserGroupPlaylog();
			userGroupLog.setTableid(id);
			userGroupLog.setUserid(creatorId);
			userGroupLog.setCount(playBureau);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			userGroupLog.setCreattime(sdf.format(createTime));
			String players = "";
			String score = "";
			String diFenScore = "";
			for(int i = 1; i <= seatMap.size(); i++){
				if(i == seatMap.size()){
					players += seatMap.get(i).getUserId();
					score += seatMap.get(i).getTotalPoint();
					diFenScore += seatMap.get(i).getTotalPoint();
				}else{
					players += seatMap.get(i).getUserId()+",";
					score += seatMap.get(i).getTotalPoint()+",";
					diFenScore += seatMap.get(i).getTotalPoint()+",";
				}
			}
			userGroupLog.setPlayers(players);
			userGroupLog.setScore(score);
			userGroupLog.setDiFenScore(diFenScore);
			userGroupLog.setDiFen("1");
			userGroupLog.setOvertime(sdf.format(now));
			userGroupLog.setPlayercount(maxPlayerCount);
			String groupId = isGroupRoom()?loadGroupId() : 0+"";
			userGroupLog.setGroupid(Long.parseLong(groupId));
			userGroupLog.setGamename("?????????");
			userGroupLog.setTotalCount(totalBureau);
			if(playBureau == 1){
				groupPaylogId = TableLogDao.getInstance().saveGroupPlayLog(userGroupLog);
			}else if(playBureau > 1 && groupPaylogId != 0){
				userGroupLog.setId(groupPaylogId);
				TableLogDao.getInstance().updateGroupPlayLog(userGroupLog);
			}
		}
		saveTableRecord(logId, over, playBureau);
		for (WaihuziPlayer player : playerMap.values()) {
			player.addRecord(logId, playBureau);
		}
		UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
	}

	@Override
	protected void loadFromDB1(TableInf info) {
		if (!StringUtils.isBlank(info.getNowDisCardIds())) {
			this.nowDisCardIds = GuihuziTool.explodeGhz(info.getNowDisCardIds(), ",");
		}
		if (!StringUtils.isBlank(info.getLeftPais())) {
			this.leftCards = GuihuziTool.explodeGhz(info.getLeftPais(), ",");
		}
	}

	@Override
	protected void sendDealMsg() {
		sendDealMsg(0);
	}

	@Override
	protected void sendDealMsg(long userId) {// ??????????????????
		int lastCardIndex = RandomUtils.nextInt(20);
		for (WaihuziPlayer tablePlayer : seatMap.values()) {
			DealInfoRes.Builder res = DealInfoRes.newBuilder();
			res.addAllHandCardIds(tablePlayer.getHandPais());
			res.setNextSeat(lastWinSeat);
			res.setGameType(getWanFa());
			res.setRemain(leftCards.size());
			res.setBanker(lastWinSeat);
			res.addXiaohu(seatMap.get(lastWinSeat).getHandPais().get(lastCardIndex));
			tablePlayer.writeSocket(res.build());
			sendTingInfo(tablePlayer);
			
			if(tablePlayer.isAutoPlay()) {
	       		 addPlayLog(tablePlayer.getSeat(), WaihuziConstant.action_tuoguan + "",1 + "");
	       }
		}
		if(autoPlay&&playBureau==1){
			for (WaihuziPlayer tablePlayer : seatMap.values()) {
				if(!tablePlayer.isAutoPlay()){
					tablePlayer.setLastCheckTime(0);
				}
			}
		}
		
		checkAction();
		
	}

	@Override
	public int getWanFa() {
		 return SharedConstants.game_type_paohuzi;
	}

	@Override
	public void startNext() {
		//checkAction();
	}

	public void play(WaihuziPlayer player, List<Integer> cardIds, int action) {
		play(player, cardIds, action, false, false, false);
	}

	private void hu(WaihuziPlayer player, List<GuihzCard> cardList, int action, GuihzCard nowDisCard) {
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return;
		}
		if (huConfirmList.contains(player.getSeat())) {
			return;
		}
		if (!checkAction(player, cardList, nowDisCard, action)) {
//			player.writeComMessage(WebSocketMsgType.res_com_code_yj_guihz_skip);
			player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
			return;
		}
		if(action == WaihzDisAction.action_jiuduiban_hu)
			action = WaihzDisAction.action_hu;
		List<Integer> actionList = actionSeatMap.get(player.getSeat());
		if (actionList.get(0) != 1) {
			return;
		}
		GuihuziHuLack hu = null;
		if(nowDisCard == null && checkJiuDuiBanHu(player)) {// ??????
			hu = new GuihuziHuLack(0);
			hu.setHu(true);
			List<CardTypeHuxi> huxiList=getCardType10Dui(player);
//			List<CardTypeHuxi> huxiList = new ArrayList<>();
//			CardTypeHuxi handPaisType =  new CardTypeHuxi(-1, player.getHandPais(), 0, true);
//			huxiList.add(handPaisType);
			hu.setPhzHuCards(huxiList);		
		}
		if(hu == null)
			hu = player.checkHu(nowDisCard, isSelfMo(player));
		if (hu != null && hu.isHu()) {
			sendActionMsg(player, action, null, WaihzDisAction.action_type_action);// ???????????????
			addPlayLog(player.getSeat(), action + "", GuihuziTool.implodeGhz(cardList, ","));
			boolean isBegin = (nowDisCard == null) ? true : false;
			List<Integer> dahus = new ArrayList<>();
			player.setHu(hu);
			Map<Integer, Integer> yuanMap = player.initDahuList(dahus, isBegin);
			int totalHuPoint = player.initHuxiPoint(dahus, yuanMap, maxhuxi);
			hu.setYuanMap(yuanMap);
			player.setHu(hu);
			player.setLostPoint(totalHuPoint);
			player.addHuXi(totalHuPoint);
			huConfirmList.add(player.getSeat());
			clearAction();
			calcOver();
		} else {
			broadMsg(player.getName() + " ????????????");
		}

	}
	
	
	private List<CardTypeHuxi>  getCardType10Dui(WaihuziPlayer player){
		
		List<GuihzCard> handCardsCopy = new ArrayList<>(player.getHandGhzs());
		GuihzCardIndexArr arr = GuihuziTool.getGuihzCardIndexArr(player.getHandGhzs());
		List<CardTypeHuxi> huxiList = new ArrayList<>();
		List<List<GuihzCard>> duizis = arr.getDuizis();
		for(List<GuihzCard> duizi : duizis) {// 
			List<Integer> ids  =  GuihuziTool.toGhzCardIds(duizi);
			CardTypeHuxi huxi = null;
			if(ids.size() ==4){
				huxi = new CardTypeHuxi(WaihzDisAction.action_liu,ids,5,true);
				
			}else if(ids.size()==3){
				 huxi = new CardTypeHuxi(WaihzDisAction.action_kang,ids,3,true);
				
			}else {
				 huxi = new CardTypeHuxi(-1,ids,0,true);
			}
			huxiList.add(huxi);
			handCardsCopy.removeAll(duizi);
		}
		if(handCardsCopy.size()>0) {
			List<Integer> ids  =  GuihuziTool.toGhzCardIds(handCardsCopy);
			CardTypeHuxi huxi = new CardTypeHuxi(-1,ids,0,true);
			huxiList.add(huxi);
		}
		
		if(arr.getDuiziNum()==10) {
			player.changeDahuCounts(6, 1);
		}
		if(arr.getDuiziNum()==9) {
			player.changeDahuCounts(1, 1);
		}
		return huxiList;
		
		
		
	}

	/**
	 * ????????????
	 *
	 * @param player
	 * @return
	 */
	public boolean isSelfMo(WaihuziPlayer player) {
		if (moSeatPair != null) {
			return moSeatPair.getValue() == player.getSeat();
		}
		return false;
	}

	/**
	 * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
	 * @param player
	 * @param cardList ?????????
	 * @param nowDisCard ??????????????????
	 * @param action
	 */
	private void liu(WaihuziPlayer player, List<GuihzCard> cardList, GuihzCard nowDisCard, int action) {
		if (cardList == null||cardList.isEmpty()) {
			LogUtil.errorLog.info("????????????:" + cardList);
			return;
		}
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return;
		}
		if(isMoByPlayer(player)) {// ???????????????????????????????????????  
			 List<GuihzCard> cards = GuihuziTool.getSameCards(player.getHandGhzs(), nowDisCard);
			 if(cards.size() == 3 && nowDisCard.getVal() != cardList.get(0).getVal()) {// ???????????????????????????????????????  ?????????????????????????????????????????????????????? ???????????????????????????
				 cardList = GuihuziTool.getSameCards(player.getHandGhzs(), nowDisCard);
			 }
		}
		if (!checkAction(player, cardList, nowDisCard, action)) {
//			player.writeComMessage(WebSocketMsgType.res_com_code_yj_guihz_skip);
			player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
			return;
		}
		if(GuihuziTool.isHasCardVal(player.getWei(), cardList.get(0).getVal())) {// ??????????????????
			cardList = GuihuziTool.findGhzByVal(player.getWei(), cardList.get(0).getVal());
			if(nowDisCard.getVal() == cardList.get(0).getVal() && !cardList.contains(nowDisCard))
				cardList.add(nowDisCard);
			if(cardList.size() <= 3) {
				List<GuihzCard> handCards = GuihuziTool.findGhzByVal(player.getHandGhzs(), cardList.get(0).getVal());
				cardList.addAll(handCards);
			}
			action = WaihzDisAction.action_weiHouLiu;
		} else {// ???????????????
			cardList = GuihuziTool.getSameCards(player.getHandGhzs(), cardList.get(0));
			if(player.getHandGhzs().size() % 3 != 2 && cardList.size() <= 3) {
				cardList.add(nowDisCard);
				action = WaihzDisAction.action_weiHouLiu;
			}
			//&& (nowDisCardIds == null || nowDisCardIds.isEmpty()) && player.getSeat() == lastWinSeat
			if(player.getHandGhzs().size() % 3 == 2 && cardList.size() == 4 ) {
				action = WaihzDisAction.action_qishouLiu;
			}
		}
		if (cardList.size() != 4) {
			LogUtil.errorLog.info("????????????:" + cardList);
			return;
		}
		if (!GuihuziTool.isSameCard(cardList)) {
			LogUtil.errorLog.info("????????????:" + cardList);
			return;
		}
		addPlayLog(player.getSeat(), action + "", GuihuziTool.implodeGhz(cardList, ","));
		if (nowDisCard != null) {
			setBeRemoveCard(nowDisCard);
			getDisPlayer().removeOutPais(nowDisCard);
		}
		player.disCard(action, cardList);
		clearAction();
		boolean disCard = setDisPlayer(player, action, false,true);
		if(action == WaihzDisAction.action_weiHouLiu) {
			action = WaihzDisAction.action_liu;
		}
		sendActionMsg(player, action, cardList, WaihzDisAction.action_type_action);
		if (!disCard) {
			 checkMo();
		}
	}
	
	/**
	 * ???  ???????????????????????????????????????????????????????????????
	 * @param player
	 * @param cardList ????????? ??????????????????
	 * @param nowDisCard ??????????????????
	 * @param action
	 */
	private void piao(WaihuziPlayer player, List<GuihzCard> cardList, GuihzCard nowDisCard, int action) {
		if(!isKepiao()) {//??????????????????
			return;
		}
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return;
		}
		if (!checkAction(player, cardList, nowDisCard, action)) {
//			player.writeComMessage(WebSocketMsgType.res_com_code_yj_guihz_skip);
			player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
			return;
		}
		if(cardList.get(0).getVal() != nowDisCard.getVal()) {// ???????????????????????????
			LogUtil.msgLog.info("????????????:" + cardList);
			return;
		}
		if (!cardList.contains(nowDisCard)) {
			cardList.add(nowDisCard);
		}
		List<GuihzCard> peng = player.getPeng();
		cardList.addAll(GuihuziTool.getSameCards(peng, nowDisCard));
		if(cardList.size() != 4) {// ??????????????????????????????
			LogUtil.msgLog.info("????????????2:" + cardList + "nowDisCard:" + nowDisCard);
			return;
		}
		setBeRemoveCard(nowDisCard);
		getDisPlayer().removeOutPais(nowDisCard);
		addPlayLog(player.getSeat(), action + "", GuihuziTool.implodeGhz(cardList, ","));
		player.disCard(action, cardList);
		clearAction();// ????????????????????????
//		boolean disCard = setDisPlayer(player, action, false);
//		sendActionMsg(player, action, cardList, WaihzDisAction.action_type_action);
//		if (!disCard) {
//			checkMo();
//		}
	}

	/**
	 * ???
	 * @param player
	 * @param cardList ????????????
	 * @param nowDisCard
	 * @param action
	 */
	private void wei(WaihuziPlayer player, List<GuihzCard> cardList, GuihzCard nowDisCard, int action) {
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return;
		}
//		if(player.getPassPeng().contains(nowDisCard.getVal())) {
//			LogUtil.errorLog.info("?????????????????????");
//			return;
//		}
		if (!checkAction(player, cardList, nowDisCard, action)) {
//			player.writeComMessage(WebSocketMsgType.res_com_code_yj_guihz_skip);
			player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
			return;
		}
		boolean isFristDisCard = player.isFristDisCard();		
		cardList = player.getPengOrWeiList(nowDisCard, cardList);
		if (cardList == null) {
			LogUtil.errorLog.info("?????????");
			return;
		}
		if (!cardList.contains(nowDisCard)) {
			cardList.add(0, nowDisCard);
		}
		//player.setFirstLiu(1);
		List<GuihzCard> copyCardList = new ArrayList<>();
		copyCardList.addAll(cardList);
		copyCardList.remove(nowDisCard);
		boolean dis= true;
		if(!player.checkCanDiscard(copyCardList, false)) {
//			LogUtil.errorLog.info("????????????????????????????????????????????????");
//			return;
			dis = false;
		}
		
		 List<Integer> actionList = 	actionSeatMap.get(player.getSeat());
		if(actionList.get(0)==1){
			isFristDisCard = true;
		}
		setBeRemoveCard(nowDisCard);
		getDisPlayer().removeOutPais(nowDisCard);
		addPlayLog(player.getSeat(), action + "", GuihuziTool.implodeGhz(cardList, ","));
		player.disCard(action, cardList);
		clearAction();
		boolean disCard = setDisPlayer(player, action, isFristDisCard,dis);
		
		
		
		WaihuziCheckCardBean checkCard = player.checkCard(null, true, true, false);// ??????????????????
		List<Integer> list = checkCard.getActionList();
		if (list != null && !list.isEmpty()) {
			appendActionSeat(player.getSeat(), list);
		}
		
//		player.checkHu(nowDisCard, true);
		sendActionMsg(player, action, cardList, WaihzDisAction.action_type_action);
		if (!disCard) {
			 checkMo();
		}
	}

	/**
	 * ??????
	 */
	private void disCard(WaihuziPlayer player, List<GuihzCard> cardList, int action) {
		if (!actionSeatMap.isEmpty()) {
			LogUtil.e("??????:" + JacksonUtil.writeValueAsString(actionSeatMap));
			return;
		}
		if(!tempActionMap.isEmpty()) {
			LogUtil.e(player.getName() + "???????????????????????????");
			clearTempAction();
		}
		if (toPlayCardFlag != 1) {
			LogUtil.e(player.getName() + "?????? toPlayCardFlag:" + toPlayCardFlag + "??????");
			checkMo();
			return;
		}
		GuihzCard disCard = cardList.get(0);
		if(player.getHasPengOrWeiPais(null).contains(disCard.getVal())) {
			LogUtil.errorLog.info("???????????????????????????????????????");
			return;
		}
		if(player.getHasChiMenzi(null, disCard)) {
			LogUtil.errorLog.info("??????????????????????????????");
			return;
		}
		if (player.getSeat() != nowDisCardSeat) {
			LogUtil.errorLog.info("??????:" + nowDisCardSeat + "??????");
			return;
		}
		if (cardList.size() != 1) {
			LogUtil.errorLog.info("??????????????????:" + cardList);
			return;
		}
		if (player.isFristDisCard() && player.getSeat() == lastWinSeat) {
		} else {
			if (firstCard) {
				firstCard = false;
			}
			player.setFirstLiu(1);
		}
		player.pass(WaihzDisAction.action_chi, cardList.get(0), true);
		player.pass(WaihzDisAction.action_peng, cardList.get(0), false);
		addPlayLog(player.getSeat(), action + "", GuihuziTool.implodeGhz(cardList, ","));
		player.disCard(action, cardList);
		setMoFlag(0);
		markMoSeat(player.getSeat(), action);
		clearMoSeatPair();
		setToPlayCardFlag(0); // ??????????????????flag
		setDisCardSeat(player.getSeat());
		setNowDisCardIds(cardList);
		LogUtil.msgLog.info(player.getUserId() + "?????????????????????" + cardList);
		setNowDisCardSeat(getNextDisCardSeat());
		for (int seat : seatMap.keySet()) {
			if(seat == player.getSeat())
				continue;
			WaihuziCheckCardBean checkCard = seatMap.get(seat).checkCard(cardList.get(0), false, false);
			List<Integer> list = checkCard.getActionList();
			if (list != null && !list.isEmpty()) {
				addAction(checkCard.getSeat(), list);
			}
		}

		sendActionMsg(player, action, cardList, WaihzDisAction.action_type_dis);
//		boolean isTing = player.isTing(this, player);
//		if(isTing){
//			List<Integer> cards = player.getTingCards();
//			putTingMap(player.getSeat(), cards);
//			sendTingActionMsg(player, 8, player.getTingCards(), WaihzDisAction.action_type_ting);
//		}
		checkAutoMo();
	}

	private void checkAutoMo() {
		if (isTest()) {// ???????????????????????????
			checkMo();
		}
	}

	/**
	 * ???
	 */
	private void peng(WaihuziPlayer player, List<GuihzCard> cardList, GuihzCard nowDisCard, int action) {
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return;
		}
		if (!checkAction(player, cardList, nowDisCard, action)) {
//			player.writeComMessage(WebSocketMsgType.res_com_code_yj_guihz_skip);
			player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
			return;
		}
		cardList = player.getPengOrWeiList(nowDisCard, cardList);
		if (cardList == null) {
			LogUtil.errorLog.info("?????????");
			return;
		}
		// ????????????????????????
		if (!cardList.contains(nowDisCard)) {
			cardList.add(0, nowDisCard);
		} else {
			cardList.remove(nowDisCard);
			cardList.add(0, nowDisCard);
		}
//		if(!player.checkCanDiscard(cardList, true)) {
//			LogUtil.errorLog.info("????????????????????????????????????????????????");
//			return;
//		}
		boolean dis= true;
		if(!player.checkCanDiscard(cardList, false)) {
//			LogUtil.errorLog.info("????????????????????????????????????????????????");
//			return;
			dis = false;
		}
		if(!isOpreAc()){
			setOpreAc(true);
		}
		
		player.setFirstLiu(1);
		setBeRemoveCard(nowDisCard);
		getDisPlayer().removeOutPais(nowDisCard);
		addPlayLog(player.getSeat(), action + "", GuihuziTool.implodeGhz(cardList, ","));
		player.disCard(action, cardList);
		clearAction();
		boolean disCard = setDisPlayer(player, action, false,dis);
		sendActionMsg(player, action, cardList, WaihzDisAction.action_type_action);
		if (!disCard) {
			checkMo();
		}
		// ????????????,??????????????????????????????
		if (isMoFlag()) {
			for (WaihuziPlayer seatPlayer : seatMap.values()) {
				if (seatPlayer.getSeat() == player.getSeat()) {
					continue;
				}
				seatPlayer.removePassChi(nowDisCard.getVal());
			}
		}
	}

	/**
	 * ???
	 */
	private void pass(WaihuziPlayer player, List<GuihzCard> cardList, GuihzCard nowDisCard, int action) {		
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return;
		}

		if(action==WaihzDisAction.action_pass){
			addPlayLog(player.getSeat(), WaihzDisAction.action_guo + "",nowDisCard!=null?nowDisCard.getId()+"":"");
		}

		boolean passLiu = passLiu(player, cardList, action);
		if(passLiu) {
			return;
		}
		
		List<Integer> actionList = actionSeatMap.get(player.getSeat());
		if((nowDisCardIds != null && !nowDisCardIds.isEmpty()) && (cardList == null || cardList.isEmpty() || nowDisCardIds.get(0).getId() != cardList.get(0).getId())) {
//			if(actionList.get(6) != 1 && !(actionList.get(0) == 1 && player.getHandPais().size() % 3 == 2)) {
//				LogUtil.msgLog.info(player.getName() + "?????????????????????" + cardList);
//				return;
//			}
		}
		// ????????????????????????  ?????????????????????????????????????????????  ???????????????????????????  ?????????????????? ????????????????????????????????? ??????????????????????????????????????? 
		List<Integer> list = WaihzDisAction.parseToDisActionList(actionList);
		int priorityAction = WaihzDisAction.getMaxPriorityAction(list);// ??????????????????????????????
		boolean canAction = checkCanAction(player, cardList, nowDisCard, priorityAction);
		canAction = true;
//		if(canAction == false) {// ?????????  ?????????????????????????????????????????????
//			LogUtil.msgLog.info(player.getName() + "?????????  ?????????????????????????????????????????????");
//			updateTempAction(canAction, player, cardList, nowDisCard, action);
//			player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
////			player.writeComMessage(WebSocketMsgType.res_com_code_yj_guihz_skip);
//			return;
//		} else {
			if(actionSeatMap.get(player.getSeat()).get(3) == 1 && player.getHandPais().size() % 3 == 2) {// ??????????????????
				setNowDisCardSeat(player.getSeat());
				setToPlayCardFlag(1);// ???????????????
			}
			
			if(nowDisCard!=null){
				for (int passAction : list) {// ???pass?????????????????????passChi???passPeng???
					player.pass(passAction, nowDisCard, true);
				}	
			}
			removeAction(player.getSeat());
			markMoSeat(player.getSeat(), action);
			LogUtil.msgLog.info(player.getName() + "????????????" + cardList);
			cardList.clear();
			//addPlayLog(player.getSeat(), action + "", GuihuziTool.implodeGhz(cardList, ","));
			sendActionMsg(player, action, cardList, WaihzDisAction.action_type_action);
			refreshTempAction(player);// ?????? ???????????????????????????????????????????????????????????????
			if (WaihuziConstant.isAutoMo) {
				checkMo();
			} else {
				if (isTest()) {
					checkMo();
				}
			}
			// ????????????
//			if (this.leftCards.size() == 0 && !isHasSpecialAction()) {
//				calcOver();
//			}
//		}
		
	}

	private boolean passLiu(WaihuziPlayer player, List<GuihzCard> cardList, int action) {
		if(actionSeatMap.get(player.getSeat()).get(3) == 1 && player.getHandPais().size() % 3 == 2) {// ??????????????????
			setNowDisCardSeat(player.getSeat());
			setToPlayCardFlag(1);// ???????????????
			removeAction(player.getSeat());
			markMoSeat(player.getSeat(), action);
			
			if(!player.checkCanDiscard(cardList, false)) {
				setToPlayCardFlag(0);
				player.compensateCard();
				int next = calcNextSeat(player.getSeat());
				setNowDisCardSeat(next);
			}
			
			LogUtil.msgLog.info(player.getName() + "????????????" + cardList);
			cardList.clear();
			addPlayLog(player.getSeat(), action + "", GuihuziTool.implodeGhz(cardList, ","));
			sendActionMsg(player, action, cardList, WaihzDisAction.action_type_action);
			refreshTempAction(player);// ?????? ???????????????????????????????????????????????????????????????
				checkMo();
				return true;
		}
		return false;
	}

	/**
	 * ???
	 */
	private void chi(WaihuziPlayer player, List<GuihzCard> cardList, GuihzCard nowDisCard, int action) {
		List<Integer> actionList = actionSeatMap.get(player.getSeat());
		if (actionList == null) {
			return;
		}
		if (cardList != null) {
			List<GuihzCard> chiCards = GuihuziTool.checkChi(cardList, nowDisCard);
//			|| chiCards.isEmpty()
			if (cardList.size() % 3 != 0 ) {
				LogUtil.errorLog.info("?????????" + cardList);
				return;
			}
			if (!cardList.contains(nowDisCard)) {
				return;
			}
		}
		cardList = player.getChiList(nowDisCard, cardList);
		if (cardList == null) {
			LogUtil.errorLog.info("?????????");
			return;
		}
		if (player.getHandPais().size() <= cardList.size()) {
			LogUtil.errorLog.info("?????????????????????????????????????????????");
			return;
		}
		if(cardList.size() > 3) {
			LogUtil.errorLog.info("????????????" + cardList);
			return;
		}
		if (GuihuziTool.isGuihuziRepeat(cardList)) {
			LogUtil.errorLog.info("?????????");
			return;
		}
		List<Integer> cardPais = GuihuziTool.toGhzCardVals(cardList, false);
//		if (player.getChiSizeExcept2710() >= 2 && !GuihuziTool.c2710List.containsAll(cardPais)) {
//			LogUtil.errorLog.info("?????????????????????????????????????????????");
//			return;
//		}
		List<GuihzCard> copyChis = new ArrayList<>(cardList);
		copyChis.remove(nowDisCard);
		GuihuziMenzi menzi = new GuihuziMenzi(GuihuziTool.toGhzCardVals(copyChis, true),  0);
		if(player.getPassMenzi().contains(menzi)) {
			LogUtil.errorLog.info("?????????????????????????????????" + copyChis);
			return;
		}
		if (!checkAction(player, cardList, nowDisCard, action)) {
			player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
//			player.writeComMessage(WebSocketMsgType.res_com_code_yj_guihz_skip);
			return;
		}
		// ????????????????????????
		if (!cardList.contains(nowDisCard)) {
			cardList.add(0, nowDisCard);
		} else {
			cardList.remove(nowDisCard);
			cardList.add(0, nowDisCard);
		}
		player.setFirstLiu(1);
//		if(!player.checkCanDiscard(cardList, true)) {
//			LogUtil.errorLog.info("????????????????????????????????????????????????");
//			return;
//		}
		
		boolean dis= true;
		if(!player.checkCanDiscard(cardList, true)) {
//			LogUtil.errorLog.info("????????????????????????????????????????????????");
//			return;
			WaihuziCheckCardBean checkBean = player.checkLiu();
			if(!checkBean.isLiu()) {
				dis = false;
			}
		}
		if(!isOpreAc()){
			setOpreAc(true);
		}
		player.pass(WaihzDisAction.action_peng, nowDisCard, false);// ????????????????????????  ??????
		setBeRemoveCard(nowDisCard);
		getDisPlayer().removeOutPais(nowDisCard);
		addPlayLog(player.getSeat(), action + "", GuihuziTool.implodeGhz(cardList, ","));
		player.disCard(action, cardList);
		clearAction();
		boolean disCard = setDisPlayer(player, action, false,dis);
		sendActionMsg(player, action, cardList, WaihzDisAction.action_type_action);
		if (!disCard) {
			checkMo();
		}
	}
	
	private boolean checkLiu(WaihuziPlayer player) {
		WaihuziCheckCardBean checkBean = player.checkLiu();
		if(player.getHandPais().size() % 3 == 2 && checkBean.isLiu()) {// ?????????????????????
			List<Integer> list = checkBean.getActionList();
			if (list != null && !list.isEmpty()) {
				addAction(player.getSeat(), list);
			}
			//sendPlayerActionMsg(player, WaihzDisAction.action_liu);
			return true;
		}
		return false;
	}

	public synchronized void play(WaihuziPlayer player, List<Integer> cardIds, int action, boolean moPai, boolean isHu, boolean isPassHu) {
	    if (state != table_state.play) {// ??????play??????
			return;
		}
		GuihzCard nowDisCard = null;
		List<GuihzCard> cardList = new ArrayList<>();
		// ???????????????????????????????????????,??????????????????id????????????????????????
		if (action != WaihzDisAction.action_mo) {
			if (nowDisCardIds != null && nowDisCardIds.size() == 1) {
				nowDisCard = nowDisCardIds.get(0);
			}
			if (action != WaihzDisAction.action_pass) {
			    if (!player.isCanDisCard(cardIds, nowDisCard)) {
					return;
				}
			}
			if (cardIds != null && !cardIds.isEmpty()) {
				cardList = GuihuziTool.toGhzCards(cardIds);
			}
		}
		if(action != WaihzDisAction.action_mo && action != WaihzDisAction.action_pass) 
			LogUtil.msgLog.info("?????? :" + player.getName() + "-----" + WaihzDisAction.getActionName(action) + " ???????????????" + nowDisCard + " ??????????????????:" + cardList);
//		if(action == WaihzDisAction.action_piao) {
//			piao(player, cardList, nowDisCard, action);
//		} else 
		if (action == WaihzDisAction.action_wei) {
			wei(player, cardList, nowDisCard, action);
		} else if (action == WaihzDisAction.action_liu) {
			liu(player, cardList, nowDisCard, action);
		} else if (action == WaihzDisAction.action_hu || action == WaihzDisAction.action_jiuduiban_hu) {
			hu(player, cardList, action, nowDisCard);
		} else if (action == WaihzDisAction.action_peng) {
			peng(player, cardList, nowDisCard, action);
		} else if (action == WaihzDisAction.action_chi) {
			chi(player, cardList, nowDisCard, action);
		} else if (action == WaihzDisAction.action_pass) {
			pass(player, cardList, nowDisCard, action);
		} else if (action == WaihzDisAction.action_mo) {
			if (isTest()) {
				return;
			}
			if (checkMoMark != null) {
				int cAction = cardIds.get(0);
				if (checkMoMark.getId() == player.getSeat() && checkMoMark.getValue() == cAction) {
					checkMo();
				}
			}
		} else {// 0???????????? 
			disCard(player, cardList, action);
		}
		if (!moPai && !isHu) {// ????????????????????????????????????
			robotDealAction();
		}
		sendTingInfo(player);
	}

	/**
	 * ????????????????????????
	 */
	private boolean setDisPlayer(WaihuziPlayer player, int action, boolean isHu,boolean needChu) {
		if (this.leftCards.isEmpty()) {// ????????????????????????
			if (!isHu) {
				calcOver();
			}
			return false;
		}
		boolean canDisCard = true;
		if (player.getHandGhzs().isEmpty()) {
			canDisCard = false;
		}
		if (canDisCard && player.isNeedDisCard(action) &&needChu) {// (player.getSeat() == lastWinSeat && player.isFristDisCard())
			setNowDisCardSeat(player.getSeat());
			if(checkLiu(player)) {
				return false;
			}
			setToPlayCardFlag(1);// ???????????????
			return true;
		} else {// ??????????????? ?????????????????????
			setToPlayCardFlag(0);
			player.compensateCard();
			int next = calcNextSeat(player.getSeat());
			setNowDisCardSeat(next);
			if (actionSeatMap.isEmpty()) {
				markMoSeat(player.getSeat(), action);
			}
			return false;
		}
	}
	
	/**
	 * ?????????????????????
	 * @param player
	 * @param cardList
	 * @param nowDisCard
	 * @param action
	 * @return
	 */
	private boolean checkCanAction(WaihuziPlayer player, List<GuihzCard> cardList, GuihzCard nowDisCard, int action) {
		List<Integer> stopActionList = WaihzDisAction.findPriorityAction(action);
		boolean canAction = true;
		for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
			if (player.getSeat() != entry.getKey()) {// ??????
				boolean can = WaihzDisAction.canDis(stopActionList, entry.getValue());
				if (!can) {
					canAction = false;
					break;
				}
				List<Integer> disActionList = WaihzDisAction.parseToDisActionList(entry.getValue());
				if (disActionList.contains(action)) {
					// ??????????????????????????? ????????????????????????
					int actionSeat = entry.getKey();
					int nearSeat = getNearSeat(disCardSeat, Arrays.asList(player.getSeat(), actionSeat));
					if (nearSeat != player.getSeat()) {
						canAction = false;
						break;
					}
				}
			}
		}
		return canAction;
	}

	/**
	 * ???????????????????????????????????? ????????????????????????????????????????????????????????????
	 */
    private boolean checkAction(WaihuziPlayer player, List<GuihzCard> cardList, GuihzCard nowDisCard, int action) {
		boolean canAction = checkCanAction(player, cardList, nowDisCard, action);
		updateTempAction(canAction, player, cardList, nowDisCard, action);
		return canAction;
	}
    
    private void updateTempAction(boolean canAction, WaihuziPlayer player, List<GuihzCard> cardList, GuihzCard nowDisCard, int action) {
    	if(canAction == false) {// ???????????????  ??????????????????
    		int seat = player.getSeat();
    		if(action == WaihzDisAction.action_jiuduiban_hu) {// ??????????????????0action
    			action = 0;
			}
			WaihzTempAction tempAction = new WaihzTempAction(seat, action, cardList, nowDisCard);
			tempActionMap.put(seat, tempAction);
			if(tempActionMap.size() == actionSeatMap.size()) {// ??????????????????actionMap???
				int maxAction = Integer.MAX_VALUE;
				int maxSeat = 0;
		    	Map<Integer, Integer> prioritySeats = new HashMap<>();
		    	int maxActionSize = 0;
				for(WaihzTempAction temp : tempActionMap.values()) {
					if(temp.getAction() < maxAction) {
						maxAction = temp.getAction();
						maxSeat = temp.getSeat();
					}
		    		prioritySeats.put(temp.getSeat(), temp.getAction());
				}
				Set<Integer> maxPrioritySeats = new HashSet<>();
				for(int mActionSet : prioritySeats.keySet()) {
					if(prioritySeats.get(mActionSet) == maxAction) {
						maxActionSize ++;
						maxPrioritySeats.add(mActionSet);
					}
				}
				if(maxActionSize > 1) {
					maxSeat = getNearSeat(disCardSeat, new ArrayList<>(maxPrioritySeats));
					maxAction = prioritySeats.get(maxSeat);
		    	}	
				WaihuziPlayer tempPlayer = seatMap.get(maxSeat);
				List<GuihzCard> tempCardList = tempActionMap.get(maxSeat).getCardList();
				GuihzCard tempNowDisCard = tempActionMap.get(maxSeat).getNowDisCard();
				for(int removeSeat : prioritySeats.keySet()) {
					if(removeSeat != maxSeat) {
						removeAction(removeSeat);
					}
				}	
				tempActionMap.clear();
				if(maxAction == 0) {
					maxAction = WaihzDisAction.action_jiuduiban_hu;
				}
				if(maxAction == WaihzDisAction.action_piao) {
        			piao(tempPlayer, tempCardList, tempNowDisCard, maxAction);
        		} else if (maxAction == WaihzDisAction.action_wei) {
        			wei(tempPlayer, tempCardList, tempNowDisCard, maxAction);
        		} else if (maxAction == WaihzDisAction.action_liu) {
        			liu(tempPlayer, tempCardList, tempNowDisCard, maxAction);
        		} else if (maxAction == WaihzDisAction.action_hu || maxAction == WaihzDisAction.action_jiuduiban_hu) {// ???????????????
        			hu(tempPlayer, tempCardList, maxAction, tempNowDisCard);
        		} else if (maxAction == WaihzDisAction.action_peng) {
        			peng(tempPlayer, tempCardList, tempNowDisCard, maxAction);
        		} else if (maxAction == WaihzDisAction.action_chi) {
        			chi(tempPlayer, tempCardList, tempNowDisCard, maxAction);
        		} else if (maxAction == WaihzDisAction.action_pass) {
        			pass(tempPlayer, tempCardList, tempNowDisCard, maxAction);
        		}
			}
		} else {// ????????? ????????????????????????
			tempActionMap.clear();
		}
		changeExtend();
    }
    
    private void clearTempAction() {
		tempActionMap.clear();
		changeExtend();
    }
    
    /**
     * ??????????????????????????????????????????????????????
     * @param player
     */
    private void refreshTempAction(WaihuziPlayer player) {
    	tempActionMap.remove(player.getSeat());
    	Map<Integer, Integer> prioritySeats = new HashMap<>();
    	for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
    		int seat = entry.getKey();
    		List<Integer> actionList = entry.getValue();
    		List<Integer> list = WaihzDisAction.parseToDisActionList(actionList);
    		int priorityAction = WaihzDisAction.getMaxPriorityAction(list);
    		prioritySeats.put(seat, priorityAction);
       	}
    	int maxPriorityAction = Integer.MAX_VALUE;
    	int maxPrioritySeat = 0;
    	boolean isSame = true;
    	for(int seat : prioritySeats.keySet()) {
    		if(maxPrioritySeat != Integer.MAX_VALUE && maxPrioritySeat != prioritySeats.get(seat)) {
    			isSame = false;
    		}
    		if(prioritySeats.get(seat) < maxPriorityAction) {
    			maxPriorityAction = prioritySeats.get(seat);
    			maxPrioritySeat = seat;
    		}
    	}
    	if(isSame) {
			maxPrioritySeat = getNearSeat(disCardSeat, new ArrayList<>(prioritySeats.keySet()));
    	}
    	Iterator<WaihzTempAction> iterator = tempActionMap.values().iterator();
    	while(iterator.hasNext()) {
    		WaihzTempAction tempAction = iterator.next();
    		if(tempAction.getSeat() == maxPrioritySeat) {
    			int action = tempAction.getAction();
        		List<GuihzCard> cardList = tempAction.getCardList();
        		GuihzCard nowDisCard = tempAction.getNowDisCard();
				iterator.remove();
        		WaihuziPlayer tempPlayer = seatMap.get(tempAction.getSeat());
        		if(action == 0) {
        			action = WaihzDisAction.action_jiuduiban_hu;
				}
        		if(action == WaihzDisAction.action_piao) {
        			piao(tempPlayer, cardList, nowDisCard, action);
        		} else if (action == WaihzDisAction.action_wei) {
        			wei(tempPlayer, cardList, nowDisCard, action);
        		} else if (action == WaihzDisAction.action_liu) {
        			liu(tempPlayer, cardList, nowDisCard, action);
        		} else if (action == WaihzDisAction.action_hu || action == WaihzDisAction.action_jiuduiban_hu) {
        			hu(tempPlayer, cardList, action, nowDisCard);
        		} else if (action == WaihzDisAction.action_peng) {
        			peng(tempPlayer, cardList, nowDisCard, action);
        		} else if (action == WaihzDisAction.action_chi) {
        			chi(tempPlayer, cardList, nowDisCard, action);
        		} else if (action == WaihzDisAction.action_pass) {
        			pass(tempPlayer, cardList, nowDisCard, action);
        		}
        		break;
    		}
    	}
    	changeExtend();
    }
    
    /**
     * ???????????????????????????
     */
	private WaihuziPlayer getDisPlayer() {
		return seatMap.get(disCardSeat);
	}

	@Override
	public int isCanPlay() {
		if (getPlayerCount() < getMaxPlayerCount()) {
			return 1;
		}
		// for (SyPaohuziPlayer player : seatMap.values()) {
		// if (player.getIsEntryTable() != PdkConstants.table_online) {
		// // ?????????????????????
		// broadIsOnlineMsg(player, player.getIsEntryTable());
		// return 2;
		// }
		// }
		return 0;
	}

	/**
	 * ??????
	 */
	private synchronized void checkMo() {
		// 0??? 1??? 2??? 3??? 4 ??? 5???
		if (!actionSeatMap.isEmpty()) {
			return;
		}
		if (nowDisCardSeat == 0) {
			return;
		}
		// ????????????????????????
		WaihuziPlayer player = seatMap.get(nowDisCardSeat);// ??????????????????????????????????????????
		if(player == null) {
			return;
		}
		if(!tempActionMap.isEmpty()) {
			LogUtil.e(player.getName() + "???????????????????????????");
//			clearTempAction();
			return;
		}
		if (toPlayCardFlag == 1) {// ?????????????????????
			return;
		}
		if (leftCards == null) {
			return;
		}
		if (this.leftCards.size() == 0 && !isHasSpecialAction()) {// ?????????????????? ??????????????????
			calcOver();
			return;
		}
		clearMarkMoSeat();
		boolean isZp = false;
		GuihzCard card = null;
		if (GameServerConfig.isDeveloper()  && !player.isRobot()) {
			if(zpMap.containsKey(player.getUserId()) && zpMap.get(player.getUserId()) > 0) {
				card = getNextCard(zpMap.get(player.getUserId()));
				if(card != null) {
					zpMap.remove(player.getUserId());
					isZp = true;
				} else {
//					if(this.leftCards.size() == 1) {
//						card = getNextCard();
//						card = GuihzCard.getGuihzCard(zpMap.get(player.getUserId()));
//						isZp = true;
//					} else
					card = getNextCard();
				}
			}
		}
		if(isZp == false)
			card = getNextCard();
		addPlayLog(player.getSeat(), WaihzDisAction.action_mo + "", (card == null ? 0 : card.getId()) + "");
		if (card != null) {
//			if (isTest())
//			sleep();
			setMoFlag(1);
			setMoSeat(player.getSeat());
			markMoSeat(card, player.getSeat());
			player.moCard(card);
			setDisCardSeat(player.getSeat());
			LogUtil.msgLog.info(player.getUserId() + "??????????????????????????????" + card);
			setNowDisCardIds(new ArrayList<>(Arrays.asList(card)));
			setNowDisCardSeat(getNextDisCardSeat());
			boolean addAction = true;
			for (int seat : seatMap.keySet()) {
				
				WaihuziPlayer player2 = seatMap.get(seat);
				boolean canAction = true;
				
				if(player2.getHandGhzs().size() % 3 == 2)
					canAction = false;
				
				if(!canAction) {
					continue;
				}
				WaihuziCheckCardBean checkCard = seatMap.get(seat).checkCard(card, seat == player.getSeat(), false);
				List<Integer> list = checkCard.getActionList();
				if (list != null && !list.isEmpty() && addAction) {
					addAction(checkCard.getSeat(), list);
					//??????
					if(checkCard.isWei()&&kawai==1) {
						if(!(checkCard.isHu()&& HuDaWai==1)) {
							addAction = false;
							markMoSeat(player.getSeat(), WaihzDisAction.action_mo);
							sendActionMsg2(player, WaihzDisAction.action_mo, new ArrayList<>(Arrays.asList(card)), WaihzDisAction.action_type_mo,false);
							
							play(player, new ArrayList<Integer>(), WaihzDisAction.action_wei);
							return;
						}
						
//						 wei(player, new ArrayList<>(), card, WaihzDisAction.action_wei);
					}
				}
			}
			markMoSeat(player.getSeat(), WaihzDisAction.action_mo);
			sendActionMsg(player, WaihzDisAction.action_mo, new ArrayList<>(Arrays.asList(card)), WaihzDisAction.action_type_mo);
			if (this.leftCards != null && this.leftCards.size() == 0 && !isHasSpecialAction()) {
				calcOver();
				return;
			}
			checkAutoMo();
		}
		sendTingInfo(player);
	}

	/**
	 * ??????????????????????????????????????????
	 */
	private boolean isHasSpecialAction() {
		for (List<Integer> actionList : actionSeatMap.values()) {
			if (actionList.get(0) == 1 || actionList.get(1) == 1 || actionList.get(2) == 1 || actionList.get(3) == 1) {// ??????????????????????????????????????????
				return true;
			}
		}
		return false;
	}

	private void addAction(int seat, List<Integer> actionList) {
		actionSeatMap.put(seat, actionList);
		addPlayLog(seat, WaihzDisAction.action_hasaction + "", StringUtil.implode(actionList));
		saveActionSeatMap();
	}
	
	public void appendActionSeat(int seat, List<Integer> actionlist) {
		if (actionSeatMap.containsKey(seat)) {
			List<Integer> a = actionSeatMap.get(seat);
			DataMapUtil.appendList(a, actionlist);
			addPlayLog(seat, WaihzDisAction.action_hasaction + "", StringUtil.implode(a));
		} else {
			actionSeatMap.put(seat, actionlist);
			addPlayLog(seat, WaihzDisAction.action_hasaction + "", StringUtil.implode(actionlist));
		}
		saveActionSeatMap();
	}

	private List<Integer> removeAction(int seat) {
		List<Integer> list = actionSeatMap.remove(seat);
		saveActionSeatMap();
		return list;
	}

	/**
	 * ????????????????????????
	 */
	private void clearAction() {
		actionSeatMap.clear();
		saveActionSeatMap();
	}

	private void clearHuList() {
		huConfirmList.clear();
		changeExtend();
	}

	public void saveActionSeatMap() {
		dbParamMap.put("nowAction", JSON_TAG);
	}
	
	/**
	 * ????????????????????????msg
	 *
	 * @param player
	 * @param action
	 * @param cards
	 * @param actType
	 */
	public void sendMoMsg(WaihuziPlayer player, int action, List<GuihzCard> cards, int actType) {
		PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
		builder.setAction(action);
		builder.setUserId(player.getUserId() + "");
		builder.setSeat(player.getSeat());
		// builder.setNextSeat(nowDisCardSeat);
		setNextSeatMsg(builder);
		builder.setRemain(leftCards.size());
		builder.addAllPhzIds(GuihuziTool.toGhzCardIds(cards));
		builder.setActType(actType);
		sendMoMsgBySelfAction(builder, player.getSeat());
	}

	/**
	 * ?????????????????????msg
	 */
	private void sendPlayerActionMsg(WaihuziPlayer player, int action) {
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return;
		}
		PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
		builder.setAction(action);
		builder.setUserId(player.getUserId() + "");
		builder.setSeat(player.getSeat());
		setNextSeatMsg(builder);
		if (leftCards != null) {
			builder.setRemain(leftCards.size());
		}
		builder.setActType(0);
		List<Integer> actionList = actionSeatMap.get(player.getSeat());
		if (actionList != null) {
			builder.addAllSelfAct(actionList);
		}
		player.writeSocket(builder.build());
	}

	private void setNextSeatMsg(PlayPaohuziRes.Builder builder) {
		// if (!GameServerConfig.isDebug()) {
		// builder.setNextSeat(nowDisCardSeat);
		//
		// } else {
		builder.setTimeSeat(nowDisCardSeat);
		if (toPlayCardFlag == 1) {
			builder.setNextSeat(nowDisCardSeat);
		} else {
			builder.setNextSeat(0);
		}
		// }
	}

	/**
	 * ????????????msg
	 *
	 * @param player
	 * @param action
	 * @param cards
	 * @param actType
	 */
	private void sendActionMsg(WaihuziPlayer player, int action, List<GuihzCard> cards, int actType) {
		sendActionMsg2(player, action, cards, actType,true);
	}

	private void sendActionMsg2(WaihuziPlayer player, int action, List<GuihzCard> cards, int actType,boolean selfAct) {
		PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
		builder.setAction(action);
		builder.setUserId(player.getUserId() + "");
		builder.setSeat(player.getSeat());
		setNextSeatMsg(builder);
		
		  builder.setHuxi(player.getOutYingXiCount());
		if (leftCards != null) {
			builder.setRemain(leftCards.size());
		}
		builder.addAllPhzIds(GuihuziTool.toGhzCardIds(cards));
		builder.setActType(actType);
		sendMsgBySelfAction(builder,selfAct);
	}

	/**
	 * ??????????????????msg
	 * @param player
	 * @param action
	 * @param cards
	 * @param actType
	 */
//	private void sendTingActionMsg(WaihuziPlayer player, int action, List<Integer> cards, int actType) {
//		PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
//		builder.setAction(action);
//		builder.setUserId(player.getUserId() + "");
//		builder.setSeat(player.getSeat());
//		if (leftCards != null) {
//			builder.setRemain(leftCards.size());
//		}
//		builder.addAllPhzIds(cards);
//		builder.setActType(actType);
//		player.writeSocket(builder.build());
//	}

	/**
	 * ??????????????????????????????
	 *
	 * @param builder
	 */
	private void sendMoMsgBySelfAction(PlayPaohuziRes.Builder builder, int seat) {
		for (WaihuziPlayer player : seatMap.values()) {
			PlayPaohuziRes.Builder copy = builder.clone();
			if (player.getSeat() != seat) {
				// copy.clearPhzIds();
				// copy.addPhzIds(0);
			} else {
//				copy.setHuxi(player.getOutHuxi());
			}
			if (actionSeatMap.containsKey(player.getSeat())) {
				List<Integer> actionList = actionSeatMap.get(player.getSeat());
				if (actionList != null) {
					copy.addAllSelfAct(actionList);
				}
			}
			player.writeSocket(copy.build());
		}
	}

	/**
	 * ??????????????????????????????
	 *
	 * @param builder
	 */
	private void sendMsgBySelfAction(PlayPaohuziRes.Builder builder,boolean selfAct) {
		for (WaihuziPlayer player : seatMap.values()) {
			PlayPaohuziRes.Builder copy = builder.clone();
			if (actionSeatMap.containsKey(player.getSeat()) && !tempActionMap.containsKey(player.getSeat())) {
				List<Integer> actionList = actionSeatMap.get(player.getSeat());
				if (actionList != null &&selfAct) {
					copy.addAllSelfAct(actionList);
				}
			}
			player.writeSocket(copy.build());
		}
	}

	/**
	 * ??????????????????????????????
	 */
	private void checkSendActionMsg() {
		if (actionSeatMap.isEmpty()) {
			return;
		}
		PlayPaohuziRes.Builder disBuilder = PlayPaohuziRes.newBuilder();
		WaihuziPlayer player = seatMap.get(disCardSeat);
		GuihuziResTool.buildPlayRes(disBuilder, player, 0, null);
		disBuilder.setRemain(leftCards.size());
		setNextSeatMsg(disBuilder);
		for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
			PlayPaohuziRes.Builder copy = disBuilder.clone();
			List<Integer> actionList = entry.getValue();
			copy.addAllSelfAct(actionList);
			WaihuziPlayer seatPlayer = seatMap.get(entry.getKey());
			seatPlayer.writeSocket(copy.build());
		}
	}
	
	public boolean checkJiuDuiBanHu(WaihuziPlayer player) {
		List<GuihzCard> handCardsCopy = new ArrayList<>(player.getHandGhzs());// ??????
		GuihzCardIndexArr arr = GuihuziTool.getGuihzCardIndexArr(handCardsCopy);
		if((arr.getDuiziNum() >= 9 || arr.getDuiziNum()<=1)&& (handCardsCopy.size()==20 ||handCardsCopy.size()==19)) {// ?????????  ??????????????????9
			return true;
		}
		if(arr.getDuizis().size()==1 &&arr.getDuiziNum()==2) {
			return true;
		}
		return false;
	}

	/**
	 * ?????????????????? ????????????
	 */
	public void checkAction() {
		int nowSeat = getNowDisCardSeat();
		// ????????????????????????
		WaihuziPlayer nowPlayer = seatMap.get(nowSeat);
		if (nowPlayer == null) {
			return;
		}
		for(int seat : seatMap.keySet()) {// ??????????????????
			WaihuziPlayer seatPlayer = seatMap.get(seat);
			if(checkJiuDuiBanHu(seatPlayer)) {
				int[] actionArr = new int[7];
				actionArr[0] = 1;
				List<Integer> list = new ArrayList<>();
				for (int val : actionArr) {
					list.add(val);
				}
				addAction(seat, list);
			}
//			if(seatPlayer.getHandGhzs().size() == 19){
//				boolean isTing = seatPlayer.isTing(this, seatPlayer);
//				if(isTing){
//					List<Integer> cards = seatPlayer.getTingCards();
//					putTingMap(seatPlayer.getSeat(), cards);
//					sendTingActionMsg(seatPlayer, 8, seatPlayer.getTingCards(), WaihzDisAction.action_type_ting);
//				}
//			}
		}
		WaihuziCheckCardBean checkCard = nowPlayer.checkCard(null, true, true, false);// ??????????????????
		List<Integer> list = checkCard.getActionList();
		if (list != null && !list.isEmpty()) {
			appendActionSeat(nowSeat, list);
		}
		checkSendActionMsg();
	}

	private void sleep() {
		try {
			Thread.sleep(600);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void robotDealAction() {
		if (isTest()) {
			if (leftCards.size() == 0 && !isHasSpecialAction()) {
				calcOver();
				return;
			}
			if (actionSeatMap.isEmpty()) {
				int nextseat = getNowDisCardSeat();
				WaihuziPlayer player = seatMap.get(nextseat);
				if (player != null && player.isRobot()) {
					// ????????????
					int card = RobotAI.getInstance().outPaiHandle(0, GuihuziTool.toGhzCardIds(player.getHandGhzs()), new ArrayList<Integer>());
					if (card == 0) {
						return;
					}
					sleep();
					List<Integer> cardList = new ArrayList<>(Arrays.asList(card));
					play(player, cardList, 0);
				}
			} else {
				Iterator<Integer> iterator = actionSeatMap.keySet().iterator();
				while (iterator.hasNext()) {
					Integer key = iterator.next();
					List<Integer> value = actionSeatMap.get(key);
					WaihuziPlayer player = seatMap.get(key);
					if (player == null || !player.isRobot()) {
						continue;
					}
					List<Integer> actions = WaihzDisAction.parseToDisActionList(value);
					for (int action : actions) {
						if (!checkAction(player, null, nowDisCardIds.get(0), action)) {
							player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
//							player.writeComMessage(WebSocketMsgType.res_com_code_yj_guihz_skip);
							continue;
						}
						sleep();
						if (action == WaihzDisAction.action_hu) {
							broadMsg(player.getName() + "??????");
							play(player, null, action);
						} else if (action == WaihzDisAction.action_peng) {
							play(player, null, action);
						} else if (action == WaihzDisAction.action_chi) {
							play(player, null, action);
						}
						else if (action == WaihzDisAction.action_liu) {
							// play(player, null, action);
						} else if (action == WaihzDisAction.action_wei) {
							// play(player,
							// PaohuziTool.toPhzCardIds(nowDisCardIds), action);
						}
						break;

					}
				}
			}

		}
	}

	@Override
	public int getPlayerCount() {
		return seatMap.size();
	}


	@Override
	protected void initNext1() {
		setBeRemoveCard(null);
		clearMarkMoSeat();
		clearMoSeatPair();
		clearHuList();
		setLeftCards(null);
		setStartLeftCards(null);
		setMoFlag(0);
		setMoSeat(0);
		clearAction();
		clearTempAction();
		setNowDisCardSeat(0);
		setNowDisCardIds(null);
		setFirstCard(true);
		 timeNum = 0 ;
		 setOpreAc(false);
//		tingMap = new HashMap<>();
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
				tempMap.put("handPai1", seatMap.get(1).buildHandPaiStr());
			}
			if (tempMap.containsKey("handPai2")) {
				tempMap.put("handPai2", seatMap.get(2).buildHandPaiStr());
			}
			if (tempMap.containsKey("handPai3")) {
				tempMap.put("handPai3", seatMap.get(3).buildHandPaiStr());
			}
			if (tempMap.containsKey("handPai4")) {
				tempMap.put("handPai4", seatMap.get(4).buildHandPaiStr());
			}
			if (tempMap.containsKey("answerDiss")) {
				tempMap.put("answerDiss", buildDissInfo());
			}
			if (tempMap.containsKey("nowDisCardIds")) {
				tempMap.put("nowDisCardIds", StringUtil.implode(GuihuziTool.toGhzCardIds(nowDisCardIds), ","));
			}
			if (tempMap.containsKey("leftPais")) {
				tempMap.put("leftPais", StringUtil.implode(GuihuziTool.toGhzCardIds(leftCards), ","));
			}
			if (tempMap.containsKey("nowAction")) {
				tempMap.put("nowAction", buildNowAction());
			}
			if (tempMap.containsKey("extend")) {
				tempMap.put("extend", buildExtend());
			}
			//            TableDao.getInstance().save(tempMap);
		}
		return tempMap.size() > 0 ? tempMap : null;
	}

	@Override
	public JsonWrapper buildExtend0(JsonWrapper wrapper) {
		wrapper.putString(1, StringUtil.implode(huConfirmList, ","));
		wrapper.putInt(2, moFlag);
		wrapper.putInt(3, toPlayCardFlag);
		wrapper.putInt(4, moSeat);
		if (moSeatPair != null) {
			String moSeatPairVal = moSeatPair.getId() + "_" + moSeatPair.getValue();
			wrapper.putString(5, moSeatPairVal);
		}
		wrapper.putInt(6, firstCard ? 1 : 0);
		if (beRemoveCard != null) {
			wrapper.putInt(7, beRemoveCard.getId());
		}
		wrapper.putInt(8, maxPlayerCount);
		wrapper.putInt(9, maxhuxi);
		
		
		
		
		
		wrapper.putInt(10, kawai);
		wrapper.putInt(11, maiPai);
		wrapper.putInt(12, xianjiaDiHu);
		wrapper.putInt(13, HuDaWai);
		wrapper.putInt(14, zhuangjiaDiHu);
		wrapper.putInt(15, suijiZhuang);
		wrapper.putInt(16, haoFen);
		wrapper.putInt(17, mingtang);
		wrapper.putInt(18, piaoFen);
		wrapper.putInt(19, tuoguan);
		
		wrapper.putInt(20, autoPlayGlob);
		
		
		wrapper.putInt(21, jiaBei);
		wrapper.putInt(22, jiaBeiFen);
		wrapper.putInt(23, jiaBeiShu);
		wrapper.putInt(24, opreAc ? 1 : 0);
		

//		wrapper.putInt(10, kepaop ? 1 : 0);
//		wrapper.putInt(11, wuxiping ? 1 : 0);
//        wrapper.putString(12, Boolean.toString(isAAConsume));
//		wrapper.putInt(13, diaodiaoshou ? 1 : 0);
		JSONArray jsonArray = new JSONArray();
		for (int card : startLeftCards) {
			jsonArray.add(card);
		}
		wrapper.putString("startLeftCards", jsonArray.toString());
		JSONArray tempJsonArray = new JSONArray();
		for(int seat : tempActionMap.keySet()) {
			tempJsonArray.add(tempActionMap.get(seat).buildData());
		}
		wrapper.putString("tempActions", tempJsonArray.toString());
//		wrapper.putLong(14, groupPaylogId);
//		if(tingMap != null && tingMap.size() > 0){
//			String str = "";
//			int index = 0;
//			for(Entry<Integer, List<Integer>> ting : tingMap.entrySet()){
//				int key = ting.getKey();  //?????????
//				String value = StringUtil.implode(ting.getValue(), ",");  //????????????
//				index++;
//				if(index == tingMap.size()){
//					str += key + "_" + value;
//				}else{
//					str += key + "_" + value+";";
//				}
//			}
//			wrapper.putString(15,str);
//		}
		return wrapper;
	}

	@Override
	protected void deal() {
		if (lastWinSeat == 0) {
			if(suijiZhuang!=1) {
				int masterseat = playerMap.get(masterId).getSeat();
				setLastWinSeat(masterseat);
			}else {
				int masterseat = 1;
				setLastWinSeat(masterseat);
			}
		}
		setDisCardSeat(lastWinSeat);
		setNowDisCardSeat(lastWinSeat);
		setMoSeat(lastWinSeat);
		setToPlayCardFlag(1);
		markMoSeat(null, lastWinSeat);
		List<List<GuihzCard>> list = faPai();
		int fapaiSeat = lastWinSeat;
		for(int j = 0; j < getMaxPlayerCount(); j++) {
			WaihuziPlayer player = seatMap.get(fapaiSeat);
			player.changeState(player_state.play);
			player.getFirstPais().clear();
			player.dealHandPais(list.get(j));
			player.getFirstPais().addAll(GuihuziTool.toGhzCardIds(new ArrayList<GuihzCard>(list.get(j))));//?????????????????????????????????????????????
			fapaiSeat = calcNextSeat(fapaiSeat);
		}
		
		
		List<GuihzCard> left = new ArrayList<GuihzCard>();
		
		
		if(getMaxPlayerCount()<3){
			left.addAll(list.get(2));
		}
		left.addAll(list.get(3));
		
		int sub = 0;
		if(maiPai==2){
			sub=10;
		}else if(maiPai==3){
			sub=20;
		}
		
		left = left.subList(0, left.size()-sub);
		// ??????????????????
		setLeftCards(left);
		//?????????????????????
		setStartLeftCards(GuihuziTool.toGhzCardIds(left));
		
	}

	/**
	 * ??????
	 * @return
	 */
	private List<List<GuihzCard>> faPai() {
		List<Integer> copy = new ArrayList<>(WaihuziConstant.cardList);
		List<List<GuihzCard>> list = GuihuziTool.fapai(copy, zp);
		int checkTime = 0;
//		while(checkTime < 10) {// ????????????10???
//			boolean isForbidHu = false;
//			Iterator<List<GuihzCard>> iterator = list.iterator();
//			while(iterator.hasNext()) {
//				List<GuihzCard> next = iterator.next();
//				if(isLargeForbidHu(next)) {
//					ShutDownAction.testPai = null;
//					isForbidHu = true;
//					break;
//				}
//			}
//			if(isForbidHu == true) {// ????????????
//				copy = new ArrayList<>(WaihuziConstant.cardList);
//				list = GuihuziTool.fapai(copy, zp);
//			}
//			else
//				break;
//			checkTime ++;
//		}
		return list;
	}

	private boolean isLargeForbidHu(List<GuihzCard> handcards) {
		List<GuihzCard> copy = new ArrayList<>(handcards);
		if(copy.size() == 20) {// ?????????????????????????????????
			GuihuziHuLack lack = GuihuziTool.isHu(this, new ArrayList<>(), copy, null, true, 0, false, getNowDisCardIds().size()==0);
			if(lack.isHu()) {
//				System.out.println("???????????? ????????????:" + copy);
				return true;
			}
            GuihzCardIndexArr valArr = GuihuziTool.getMax(copy);
            GuihuziIndex index3 = valArr.getPaohzCardIndex(3);// ????????????
            if (index3 != null) {// ????????????????????? ??????????????????
//				System.out.println("??????????????????????????? ?????????????????? ????????????:" + copy);
				return true;
            }
//			GuihzCardIndexArr arr = GuihuziTool.getGuihzCardIndexArr(copy);
//			if(arr.getDuiziNum() >= 7) {// ?????????7??? ???????????????
////				System.out.println("??????????????????8??? ????????????:" + copy);
//				return true;
//			}
			List<GuihzCard> redCardList = GuihuziTool.findRedGhzs(copy);
			int redCardCount = redCardList.size();
			if(redCardCount < 3 || redCardCount > 11) {// ????????????3??? ??????11???
//				System.out.println("??????????????????3??? ??????11???:" + copy);
				return true;
			}
//			for(int index = 0; index < 14 ; index ++) {// ??????????????????14???
//				List<GuihzCard> tempMjs = new ArrayList<>(copy);
//				tempMjs.remove(index);
//				if(isBaotingHu(tempMjs))// ??????????????????????????????
//					return true;
//			}
		} else if(copy.size() == 19) {// ????????????????????????
            GuihzCardIndexArr valArr = GuihuziTool.getMax(copy);
            GuihuziIndex index3 = valArr.getPaohzCardIndex(3);// ????????????
            if (index3 != null) {// ????????????????????? ??????????????????
                return true;
            }
//			GuihzCardIndexArr arr = GuihuziTool.getGuihzCardIndexArr(copy);
//			if(arr.getDuiziNum() >= 7) {// ?????????7??? ???????????????
////				System.out.println("??????????????????8??? ????????????:" + copy);
//				return true;
//			}
			List<GuihzCard> redCardList = GuihuziTool.findRedGhzs(copy);
			int redCardCount = redCardList.size();
			if(redCardCount < 3 || redCardCount > 11) {// ????????????3??? ??????11???
//				System.out.println("??????????????????3??? ??????11???:" + copy);
				return true;
			}
//			boolean baotingHu = isBaotingHu(copy);
//			if(baotingHu)
//				return true;
		}
		return false;
	}


//	private boolean isBaotingHu(List<GuihzCard> handcards) {
//		if(handcards.size() != 19)
//			return false;
//		for(int value = 1; value <= 10; value ++) {
//			GuihzCard huCard = GuihzCard.getGuihzCard(handcards, value);
//			if(huCard == null)
//				continue;
//			GuihuziHuLack lack = GuihuziTool.isHu(this, new ArrayList<>(), handcards, huCard, true, 0, false, isWuxiping());
//			if(lack.isHu())
//				return true;
//		}
//		for(int value = 101; value <= 110; value ++) {
//			GuihzCard huCard = GuihzCard.getGuihzCard(handcards, value);
//			if(huCard == null)
//				continue;
//			GuihuziHuLack lack = GuihuziTool.isHu(this, new ArrayList<>(), handcards, huCard, true, 0, false, isWuxiping());
//			if(lack.isHu())
//				return true;
//		}
//		return false;
//	}

	@Override
	public int getNextDisCardSeat() {
		if (disCardSeat == 0) {
			return lastWinSeat;
		}
		return calcNextSeat(disCardSeat);
	}

	/**
	 * ??????seat???????????????
	 */
	public int calcNextSeat(int seat) {
		int nextSeat = seat + 1 > maxPlayerCount ? 1 : seat + 1;
		return nextSeat;
	}

	/**
	 * ??????seat???????????????
	 */
	public int calcFrontSeat(int seat) {
		int frontSeat = seat - 1 < 1 ? maxPlayerCount : seat - 1;
		return frontSeat;
	}

	/**
	 * ??????????????????
	 */
	public int calcNextNextSeat(int seat) {
		int nextSeat = seat + 1 > maxPlayerCount ? 1 : seat + 1;
		int nextNextSeat = nextSeat + 1 > maxPlayerCount ? 1 : nextSeat + 1;
		return nextNextSeat;
	}

	@Override
	public Player getPlayerBySeat(int seat) {
		return seatMap.get(seat);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<Integer, Player> getSeatMap() {
		Object o = seatMap;
		return (Map<Integer, Player>) o;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<Long, Player> getPlayerMap() {
		Object o = playerMap;
		return (Map<Long, Player>) o;
	}

	@Override
	public CreateTableRes buildCreateTableRes(long userId, boolean isrecover,boolean isLastReady) {
		CreateTableRes.Builder res = CreateTableRes.newBuilder();
        buildCreateTableRes0(res);
		res.setNowBurCount(getPlayBureau());
		res.setTotalBurCount(getTotalBureau());
		res.setGotyeRoomId(gotyeRoomId + "");
		res.setTableId(getId() + "");
		res.setWanfa(playType);
		res.setRenshu(maxPlayerCount);
		if (leftCards != null) {
			res.setRemain(leftCards.size());
		} else {
			res.setRemain(0);
		}
		List<PlayerInTableRes> players = new ArrayList<>();
		for (WaihuziPlayer player : playerMap.values()) {
			PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(userId, isrecover);
			playerRes.addRecover(player.getSeat() == lastWinSeat ? 1 : 0);
			if (player.getUserId() == userId) {
				playerRes.addAllHandCardIds(player.getHandPais());
				if (actionSeatMap.containsKey(player.getSeat())) {
					List<Integer> actionList = actionSeatMap.get(player.getSeat());
					if (actionList != null && !tempActionMap.containsKey(player.getSeat())) {
						playerRes.addAllRecover(actionList);
					}
				}
				List<GuihuziMenzi> passMenzis = player.getPassMenzi();
				List<Integer> cardVals = player.getPassChi();
				List<Integer> intExts = new ArrayList<>();
//				if(!passMenzis.isEmpty()) {
//					for(GuihuziMenzi passMenzi : passMenzis) {
//						List<Integer> menziIds = GuihuziTool.toGhzCardIds(GuihuziTool.findByVals(player.getHandGhzs(), passMenzi.getMenzi()));
//						if(menziIds.size() >= 2) {
//							intExts.addAll(menziIds);
//						}
//					}
//					playerRes.addAllIntExts(intExts);
//				}
				
				if(!cardVals.isEmpty()) {
//					for(GuihuziMenzi passMenzi : passMenzis) {
//						List<Integer> menziIds = GuihuziTool.toGhzCardIds(GuihuziTool.findByVals(player.getHandGhzs(), passMenzi.getMenzi()));
//						if(menziIds.size() >= 2) {
//							intExts.addAll(menziIds);
//						}
//					}
					playerRes.addAllIntExts(cardVals);
				}
				
//				if(tingMap != null && tingMap.containsKey(player.getSeat())){
//					List<Integer> tings = tingMap.get(player.getSeat());
//					String tingStr = "";
//					if(tings != null && tings.size() > 0){
//						tingStr = StringUtil.implode(tings, ",");
//					}
//					playerRes.addStrExts(tingStr);
//				}
			}
			if(state == null)
				state = table_state.ready;
			playerRes.addExt(state.getId());
			playerRes.addExt(player.getOutYingXiCount());
			players.add(playerRes.build());
		}
		res.addAllPlayers(players);
		if (actionSeatMap.isEmpty()) {
			// int nextSeat = getNextDisCardSeat();
			if (nowDisCardSeat != 0) {
				if (toPlayCardFlag == 1) {
					res.setNextSeat(nowDisCardSeat);
				} else {
					res.setNextSeat(0);
				}
			}
		}
		res.addExt(nowDisCardSeat);//0
		res.addExt(payType);//1
		res.addExt(piaoFen);//2
		
        res.addExt(maiPai);// 3
        
        res.addExt(kawai);//4
        res.addExt(xianjiaDiHu);//5
        res.addExt(HuDaWai);//6
        res.addExt(zhuangjiaDiHu);//7
        res.addExt(suijiZhuang);//8
        res.addExt(haoFen);//9
        res.addExt(mingtang);//10
        res.addExt(tuoguan);//11
        res.addExt(autoPlayGlob);//12
        
        res.addExt(creditMode);     // 13
        res.addExt(0);// 14
        res.addExt(creditCommissionMode1);// 15
        res.addExt(creditCommissionMode2);// 16
        res.addExt(autoPlay ? 1 : 0);// 17
        res.addExt(jiaBei);// 18
        res.addExt(jiaBeiFen);// 19
        res.addExt(jiaBeiShu);// 20
		
		
		
		
		return res.build();
	}

	@Override
	public void setConfig(int index, int val) {
	}
	
	public ClosingPhzInfoRes.Builder sendAccountsMsg(boolean over, boolean selfMo, List<Integer> winList, int maxFan, List<Integer> fanTypes, int totalTun, boolean isBreak) {
		List<ClosingPhzPlayerInfoRes> list = new ArrayList<>();
		 List<ClosingPhzPlayerInfoRes.Builder> builderList = new ArrayList<>();
        //????????????????????????
        if(over && jiaBei == 1){
            int jiaBeiPoint = 0;
            int loserCount = 0;
            for (WaihuziPlayer player : seatMap.values()) {
                if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                    jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
                    player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
                } else if (player.getTotalPoint() < 0) {
                    loserCount++;
                }
            }
            if (jiaBeiPoint > 0) {
                for (WaihuziPlayer player : seatMap.values()) {
                    if (player.getTotalPoint() < 0) {
                        player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                    }
                }
            }

        }
		
		WaihuziPlayer winPlayer = null;
		for (WaihuziPlayer player : seatMap.values()) {
			if (winList != null && winList.contains(player.getSeat())) {
				winPlayer = seatMap.get(player.getSeat());
			}
			ClosingPhzPlayerInfoRes.Builder build;
			if (over) {
				build = player.bulidTotalClosingPlayerInfoRes(totalTun);
			} else {
				build = player.bulidOneClosingPlayerInfoRes();
			}
			build.addAllFirstCards(player.getFirstPais());//?????????????????????????????????
			//list.add(build.build());
			  builderList.add(build);
		     //?????????
            if(isCreditTable()){
                    player.setWinLoseCredit(player.getTotalPoint() * creditDifen);
            }
			
		}
		
		
		
        //???????????????
        if (isCreditTable()) {
            //??????????????????
            calcNegativeCredit();

            long dyjCredit = 0;
            for (WaihuziPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
            	WaihuziPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);

                builder.addStrExt(player.getWinLoseCredit() + "");      //8
                builder.addStrExt(player.getCommissionCredit() + "");   //9

                // 2019-02-26??????
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------??????????????????---------------------------------
            for (WaihuziPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
                WaihuziPlayer player = seatMap.get(builder.getSeat());
                builder.addStrExt(player.getWinLoseCredit() + "");      //8
                builder.addStrExt(player.getCommissionCredit() + "");   //9
                builder.setWinLoseCredit(player.getWinGold());
            }
        } else {
            for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
                builder.addStrExt(0 + ""); //8
                builder.addStrExt(0 + ""); //9
            }
        }

        for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
            list.add(builder.build());
        }
		
		
		
		
		ClosingPhzInfoRes.Builder res = ClosingPhzInfoRes.newBuilder();
		res.addAllLeftCards(GuihuziTool.toGhzCardIds(leftCards));
		if (fanTypes != null) {
			res.addAllFanTypes(fanTypes);
		}
		if (winPlayer != null) {
			res.setFan(maxFan);
			res.setHuxi(winPlayer.getYingxi(winPlayer.getDahu()));
			res.setTotalTun(totalTun);
			res.setHuSeat(winPlayer.getSeat());
			if (winPlayer.getHu() != null && winPlayer.getHu().getCheckCard() != null) {
				res.setHuCard(winPlayer.getHu().getCheckCard().getId());
			}
			
	         res.addAllCards(winPlayer.buildGhzHuCards());
		} 
		res.addAllClosingPlayers(list);
		res.setIsBreak(isBreak ? 1 : 0);
		res.setWanfa(getWanFa());
		res.setGroupLogId((int)groupPaylogId);
		res.addAllExt(buildAccountsExt(over));
		res.addAllStartLeftCards(startLeftCards);
		for (WaihuziPlayer player : seatMap.values()) {
			player.writeSocket(res.build());
		}
		return res;
	}

	@Override
	public void sendAccountsMsg() {
		ClosingPhzInfoRes.Builder res = sendAccountsMsg(true, false, null, 0, null, 0, true);
		saveLog(true,0L, res.build());

	}

	public List<String> buildAccountsExt(boolean isOver) {
		List<String> ext = new ArrayList<>();
		ext.add(id + "");
		ext.add(masterId + "");
		ext.add(TimeUtil.formatTime(TimeUtil.now()));
		ext.add(playType + "");
		ext.add(getConifg(0) + "");
		ext.add(playBureau + "");
		ext.add(isOver ? 1 + "" : 0 + "");
		ext.add(maxPlayerCount + "");
		ext.add(maxhuxi + "");
		ext.add(lastWinSeat + "");
		ext.add(isGroupRoom()?loadGroupId():"0");
		
		
		 ext.add(suijiZhuang + ""); //11
		 ext.add(kawai + ""); //12
		 ext.add(piaoFen + ""); //13
		
		 ext.add(String.valueOf(maiPai));//14
        //?????????
        ext.add(creditMode + ""); //15
        ext.add(creditJoinLimit + "");//16
        ext.add(creditDissLimit + "");//17
        ext.add(creditDifen + "");//18
        ext.add(creditCommission + "");//19
        ext.add(creditCommissionMode1 + "");//20
        ext.add(creditCommissionMode2 + "");//21
        ext.add(autoPlay ? "1" : "0");//20
        ext.add(jiaBei + "");//22
        ext.add(jiaBeiFen + "");//23
        ext.add(jiaBeiShu + "");//24
		
		return ext;
	}

	@Override
	public int getMaxPlayerCount() {
		return maxPlayerCount;
	}

	public void createTable(Player player, int play, int bureauCount, List<Integer> params) throws Exception {
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
//		setMaxPlayerCount(3);
	//	setPayType(params.get(2).intValue());// AA?????? ????????????   
		int maxhuxi = params.get(3).intValue();// ????????????
		setMaxhuxi(maxhuxi);
		
		 payType = StringUtil.getIntValue(params, 2, 1);//????????????
		
		 HuDaWai = StringUtil.getIntValue(params, 3, 3);// ????????????
		kawai =  StringUtil.getIntValue(params, 4, 0); 
		
		maiPai = StringUtil.getIntValue(params, 5, 0); 
		
		xianjiaDiHu = StringUtil.getIntValue(params, 6, 0); 
		
		 maxPlayerCount= StringUtil.getIntValue(params, 7, 0); 
		zhuangjiaDiHu = StringUtil.getIntValue(params, 8, 0); 
		suijiZhuang = StringUtil.getIntValue(params, 9, 0); 
		haoFen = StringUtil.getIntValue(params, 10, 0); 
		mingtang = StringUtil.getIntValue(params, 11, 0); 
		piaoFen = StringUtil.getIntValue(params, 12, 0); 
		tuoguan = StringUtil.getIntValue(params, 13, 0); 
		
		autoPlay = tuoguan>0;
		
        //?????????0??????1???
        this.jiaBei = StringUtil.getIntValue(params, 14, 0);
        //?????????
        this.jiaBeiFen = StringUtil.getIntValue(params, 15, 0);
        //?????????
        this.jiaBeiShu = StringUtil.getIntValue(params, 16, 0);
		
        autoPlayGlob = StringUtil.getIntValue(params, 17, 0);
		
        if(tuoguan>0) {
        	autoTimeOut2 =autoTimeOut =tuoguan*1000 ;
        }
        
		
//		boolean kepiao = params.get(4).intValue() == 1 ? true : false;// ???
//		boolean wuxiping = params.get(5).intValue() == 1 ? true : false;// ?????????
//		boolean diaodiaoshou = params.get(6).intValue() == 1 ? true : false;// ?????????
//		setKepiao(kepiao);
//		setWuxiping(wuxiping);
//		setDiaodiaoshou(diaodiaoshou);
//		if (payType == 1) {
//        	setAAConsume(true);
//        }
	}


	
	@Override
	public boolean isTest() {
		return false;// ??????????????????
	}

	@Override
	public void checkReconnect(Player player) {
		checkMo();// ??????????????????????????????
		sendTingInfo((WaihuziPlayer)player);
	}

	@Override
	public void checkAutoPlay() {
		 synchronized (this){
	            if (getSendDissTime() > 0) { 
	                for (WaihuziPlayer player : seatMap.values()) {
	                    if (player.getLastCheckTime() > 0) {
	                        player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
	                    }
	                }
	                return;
	            }
	            if (autoPlay && state == table_state.ready && playedBureau > 0) {
	                ++timeNum;
	                for (WaihuziPlayer player : seatMap.values()) {
	                	if(player.getState()==player_state.ready) {
	                		continue;
	                	}
	                    // ????????????????????????5???????????????
	                    if (timeNum >= 5 && player.isAutoPlay()) {
	                        autoReady(player);
	                    } else if (timeNum >= 30) {
	                        autoReady(player);
	                    }
	                }
//	                return;
	            }

	            
	            if(!autoPlay){
	               return;
	            }
	            
	            int  timeout = autoTimeOut;;
	            if(state != table_state.play){
	            	if (getTableStatus() == WaihuziConstant.TABLE_STATUS_PIAO) {
	        			for (int seat : seatMap.keySet()) {
	        				WaihuziPlayer player = seatMap.get(seat);
	        				if (player.getLastCheckTime() > 0 && player.getPiaoPoint() >= 0) {
	        					player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
	        					continue;
	        				}
	        				
	        				boolean auto = player.isAutoPlay();
		                    if(!auto){
		                        auto = checkPlayerAuto(player,timeout);
		                    }
	        				if (!auto) {
	        					continue;
	        				}
	        				autoPiao(player);
	        			}
	        			boolean piao = true;
	        			for (int seat : seatMap.keySet()) {
	        				WaihuziPlayer player = seatMap.get(seat);
	        				if (player.getPiaoPoint() < 0) {
	        					piao = false;
	        				}

	        			}
	        			if (piao) {
	        				setTableStatus(WaihuziConstant.AUTO_PLAY_TIME);
	        			}

	        		} 
	            	
	                return;
	            }
	            
	            long autoPlayTime = ResourcesConfigsUtil.loadIntegerValue("ServerConfig","autoPlayTimePhz",2*1000);
	            long now = TimeUtil.currentTimeMillis();
	            if(!actionSeatMap.isEmpty()){
	                int action = 0,seat = 0;
	                for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()){
	                	
	                	
	                	 List<Integer> actinList =WaihzDisAction.parseToDisActionList(entry.getValue());
	                    int minAction = WaihzDisAction.getMaxPriorityAction(actinList);
	                    
	                    
	                   // int minAction = Collections.min(list);
	                    if(action == 0){
	                        action = minAction;
	                        seat = entry.getKey();
	                    }else if(WaihzDisAction.findPriorityAction2(minAction) < WaihzDisAction.findPriorityAction2(action)){
	                        action = minAction;
	                        seat = entry.getKey();
	                    }else if(WaihzDisAction.findPriorityAction2(minAction) == WaihzDisAction.findPriorityAction2(action)){
	                        int nearSeat = getNearSeat(disCardSeat, Arrays.asList(seat, entry.getKey()));
	                        seat = nearSeat;
	                    }
	                }
	                if(action > 0 && seat > 0){
	                    WaihuziPlayer player = seatMap.get(seat);
	                    if (player==null){
	                        LogUtil.errorLog.error("auto play error:tableId={},seat={} is null,seatMap={},playerMap={}",id,seat,seatMap.keySet(),playerMap.keySet());
	                        return;
	                    }

	                    boolean auto = player.isAutoPlay();
	                    if(!auto){
	                        auto = checkPlayerAuto(player,timeout);
	                    }
	                    if(auto){
	                        if (player.getAutoPlayTime() == 0L) {
	                            player.setAutoPlayTime(now);
	                        } else if (player.getAutoPlayTime() > 0L && now - player.getAutoPlayTime() >= autoPlayTime){
	                            player.setAutoPlayTime(0L);
	                            if(action == WaihzDisAction.action_chi|| action == WaihzDisAction.action_liu){
	                                action = WaihzDisAction.action_pass;
	                            }
	                            if(action == WaihzDisAction.action_pass || action == WaihzDisAction.action_peng || action == WaihzDisAction.action_hu||action == WaihzDisAction.action_wei){
	                                play(player, new ArrayList<Integer>(), action);
	                            }else{
	                                checkMo();
	                            }
	                        }
	                        return;
	                    }
	                    
	                }
	            }else{
	                WaihuziPlayer player = seatMap.get(nowDisCardSeat);
	                if (player == null) {
	                    return;
	                }
	                if(toPlayCardFlag==1){
	                    boolean auto = player.isAutoPlay();
	                    if(!auto){
	                        auto = checkPlayerAuto(player,timeout);
	                    }
	                    if(auto){
	                        if (player.getAutoPlayTime() == 0L) {
	                            player.setAutoPlayTime(now);
	                        } else if (player.getAutoPlayTime() > 0L && now - player.getAutoPlayTime() >= autoPlayTime){
	                            player.setAutoPlayTime(0L);
	                            GuihzCard paohzCard = GuihuziTool.autoDisCard(player.getHandGhzs(),player);
	                            if(paohzCard != null){
	                                play(player, Arrays.asList(paohzCard.getId()), 0);
	                            }
	                        }
	                    }
	                }else{
	                    checkMo();
	                }
	            }
	        }
	}
	
	
	
	public void autoPiao(WaihuziPlayer player) {
		int piaoPoint = 0;
		if (getTableStatus() != WaihuziConstant.TABLE_STATUS_PIAO) {
			return;
		}
		if (player.getPiaoPoint() < 0) {
			player.setPiaoPoint(piaoPoint);
		} else {
			return;
		}
		ComMsg.ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_piao_fen, player.getSeat(),
				piaoPoint);
		broadMsg(build.build());
		broadMsgRoomPlayer(build.build());
		checkDeal(player.getUserId());
	}
	
	

    public boolean checkPlayerAuto(WaihuziPlayer player ,int timeout){
        long now = TimeUtil.currentTimeMillis();
        boolean auto = false;
        if (player.isAutoPlayChecked() || (player.getAutoPlayCheckedTime() >= timeout && !player.isAutoPlayCheckedTimeAdded())) {
            player.setAutoPlayChecked(true);
            timeout = autoTimeOut2;
        }
        
        if (player.getLastCheckTime() > 0) {
            int checkedTime = (int) (now - player.getLastCheckTime());

            if (checkedTime >= timeout) {
                auto = true;
            }
            if(auto){
                player.setAutoPlay(true, this);
            }
            System.out.println("checkPlayerAuto----" + player.getSeat() + "|" + player.getUserId() + "|" + player.getAutoPlayCheckedTime() + "|" + checkedTime + "|" + auto);
        } else {
            player.setLastCheckTime(now);
            player.setCheckAuto(true);
            player.setAutoPlayCheckedTimeAdded(false);
        }

        return auto;
    }
    

	@Override
	public boolean isAllReady() {
		if (super.isAllReady()) {
			if (getPiaoFen() >= 1) {
				boolean bReturn = true;
				// ?????????????????????
				if (this.isTest()) {
					for (WaihuziPlayer robotPlayer : seatMap.values()) {
						if (robotPlayer.isRobot()) {
							robotPlayer.setPiaoPoint(1);
						}
					}
				}
				for (WaihuziPlayer player : seatMap.values()) {
					if (player.getPiaoPoint() < 0) {
						ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_table_status_piao,
								getTableStatus());
						player.writeSocket(com.build());
						if (getTableStatus() != WaihuziConstant.TABLE_STATUS_PIAO) {
							player.setLastCheckTime(System.currentTimeMillis());
						}
						bReturn = false;
						// if(getTableStatus()!=CsMjConstants.TABLE_STATUS_PIAO)
						// {
						//
						// }
					}
				}
				setTableStatus(WaihuziConstant.TABLE_STATUS_PIAO);

				return bReturn;
			} else {
				int point = 0;
//				if (getKePiao() == 2 || getKePiao() == 3 || getKePiao() == 4) {
//					point = getKePiao() - 1;
//				}

				for (WaihuziPlayer player : seatMap.values()) {
					player.setPiaoPoint(point);
				}
				return true;
			}
		}
		return false;
	}
	
	public void sendTingInfo(WaihuziPlayer player) {
		if (player.getHandGhzs().size() % 3 == 2) {
			// if (actionSeatMap.containsKey(player.getSeat())) {
			// return;
			// }
			DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
			List<GuihzCard> cards = new ArrayList<>(player.getHandGhzs());

			for (GuihzCard card : player.getHandGhzs()) {
				cards.remove(card);
				List<GuihzCard> huCards = GuihuziTool.getTingZps(cards,player);
				cards.add(card);
				if (huCards == null || huCards.size() == 0) {
					continue;
				}
				DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
				ting.setMajiangId(card.getId());
				for (GuihzCard mj : huCards) {
					ting.addTingMajiangIds(mj.getId());
				}
				tingInfo.addInfo(ting.build());
			}
			if (tingInfo.getInfoCount() > 0) {
				player.writeSocket(tingInfo.build());
			}
		} else {
			List<GuihzCard> cards = new ArrayList<>(player.getHandGhzs());
			List<GuihzCard> huCards = GuihuziTool.getTingZps(cards,player);

			if (huCards == null || huCards.size() == 0) {
				return;
			}
			TingPaiRes.Builder ting = TingPaiRes.newBuilder();
			for (GuihzCard zp : huCards) {
				ting.addMajiangIds(zp.getId());
			}
			player.writeSocket(ting.build());

		}
	}


	@Override
	public Class<? extends Player> getPlayerClass() {
		return WaihuziPlayer.class;
	}
	@Override
	public boolean canQuit(Player player) {
		if (super.canQuit(player)) {
			return getTableStatus() != WaihuziConstant.TABLE_STATUS_PIAO;
		}
		return false;
	}
	
	public void setTableStatus(int tableStatus) {
		this.tableStatus = tableStatus;
	}

	public int getTableStatus() {
		return tableStatus;
	}

	public GuihzCard getNextCard(int val) {
		if (this.leftCards.size() > 0) {
			Iterator<GuihzCard> iterator = this.leftCards.iterator();
			GuihzCard find = null;
			while (iterator.hasNext()) {
				GuihzCard paohzCard = iterator.next();
				if (paohzCard.getVal() == val) {
					find = paohzCard;
					iterator.remove();
					break;
				}
			}
			dbParamMap.put("leftPais", JSON_TAG);
			return find;
		}
		return null;
	}

    @Override
    public boolean isPlaying(){
        if(super.isPlaying()){
            return true;
        }
        return getTableStatus() == WaihuziConstant.TABLE_STATUS_PIAO;
    }
	public GuihzCard getNextCard() {
		if (this.leftCards.size() > 0) {
			GuihzCard card = this.leftCards.remove(0);
			dbParamMap.put("leftPais", JSON_TAG);
			return card;
		}
		return null;
	}

	public List<GuihzCard> getLeftCards() {
		return leftCards;
	}

	public void setLeftCards(List<GuihzCard> leftCards) {
		if (leftCards == null) {
			this.leftCards.clear();
		} else {
			this.leftCards = leftCards;
		}
		dbParamMap.put("leftPais", JSON_TAG);
	}

	public void setStartLeftCards(List<Integer> startLeftCards) {
		if (startLeftCards == null) {
			this.startLeftCards.clear();
		} else {
			this.startLeftCards = startLeftCards;
		}
		dbParamMap.put("leftPais", JSON_TAG);
	}

	public int getMoSeat() {
		return moSeat;
	}

	public void setMoSeat(int lastMoSeat) {
		this.moSeat = lastMoSeat;
		changeExtend();
	}

	public List<GuihzCard> getNowDisCardIds() {
		return nowDisCardIds;
	}

	public void setNowDisCardIds(List<GuihzCard> nowDisCardIds) {
		this.nowDisCardIds = nowDisCardIds;
		dbParamMap.put("nowDisCardIds", JSON_TAG);
	}

	/**
	 * ???????????????????????????
	 */
	public boolean isMoFlag() {
		return moFlag == 1;
	}

	public void setMoFlag(int moFlag) {
		if (this.moFlag != moFlag) {
			this.moFlag = moFlag;
			changeExtend();
		}
	}

	public void markMoSeat(int seat, int action) {
		checkMoMark = new KeyValuePair<>();
		checkMoMark.setId(seat);
		checkMoMark.setValue(action);
		changeExtend();
	}

	private void clearMarkMoSeat() {
		checkMoMark = null;
		changeExtend();
	}

	public void markMoSeat(GuihzCard card, int seat) {
		moSeatPair = new KeyValuePair<>();
		if (card != null) {
			moSeatPair.setId(card.getId());
		}
		moSeatPair.setValue(seat);
		changeExtend();
	}

	public void clearMoSeatPair() {
		moSeatPair = null;
	}

	public int getToPlayCardFlag() {
		return toPlayCardFlag;
	}

	public void setToPlayCardFlag(int toPlayCardFlag) {
		if (this.toPlayCardFlag != toPlayCardFlag) {
			this.toPlayCardFlag = toPlayCardFlag;
			changeExtend();
		}
	}

	@Override
	public boolean consumeCards() {
		return SharedConstants.consumecards;
	}

	public Map<Integer, List<Integer>> getActionSeatMap() {
		return actionSeatMap;
	}

	public boolean isFirstCard() {
		return firstCard;
	}

	public void setFirstCard(boolean firstCard) {
		this.firstCard = firstCard;
		changeExtend();
	}
	
	

	public boolean isOpreAc() {
		return opreAc;
	}

	public void setOpreAc(boolean opreAc) {
		this.opreAc = opreAc;
	}

	public int getMaxhuxi() {
		return maxhuxi;
	}

	public void setMaxhuxi(int maxhuxi) {
		this.maxhuxi = maxhuxi;
	}

	public boolean isKepiao() {
		return false;
	}

	public void setKepiao(boolean kepiao) {
		this.kepaop = kepiao;
	}

	public boolean isWuxiping() {
		return this.wuxiping;
	}

	public void setWuxiping(boolean wuxiping) {
		this.wuxiping = wuxiping;
	}

	public boolean isDiaodiaoshou() {
		return diaodiaoshou;
	}

	public void setDiaodiaoshou(boolean diaodiaoshou) {
		this.diaodiaoshou = diaodiaoshou;
	}

	/**
	 * ???????????????cardId-seat
	 */
	public KeyValuePair<Integer, Integer> getMoSeatPair() {
		return moSeatPair;
	}

	public GuihzCard getBeRemoveCard() {
		return beRemoveCard;
	}

	/**
	 * ?????????????????????
	 *
	 */
	public void setBeRemoveCard(GuihzCard beRemoveCard) {
		this.beRemoveCard = beRemoveCard;
		changeExtend();
	}

	/**
	 * ???????????????????????????
	 */
	public boolean isMoByPlayer(WaihuziPlayer player) {
		if (moSeatPair != null && moSeatPair.getValue() == player.getSeat()) {
			if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
				if (nowDisCardIds.get(0).getId() == moSeatPair.getId()) {
					return true;
				}
			}
		}
		return false;
	}

	public void setMaxPlayerCount(int maxPlayerCount) {
		this.maxPlayerCount = maxPlayerCount;
	}

	@Override
	public int getDissPlayerAgreeCount() {
		return super.getDissPlayerAgreeCount();
	}

	@Override
	public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams,
			Object... objects) throws Exception {
		createTable(player, play, bureauCount, params);
	}

	@Override
	public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {
	}

	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_YyWaiHZ);

	public static void loadWanfaTables(Class<? extends BaseTable> cls){
		for (Integer integer:wanfaList){
			TableManager.wanfaTableTypesPut(integer,cls);
		}
	}
	
    public int getAutoTimeOut() {
        return autoTimeOut;
    }
    


	public int getPiaoFen() {
		return piaoFen;
	}
	
	
	

	public int getMaiPai() {
		return maiPai;
	}

	public int getHaoFen() {
		return haoFen;
	}

	public int getMingtang() {
		return mingtang;
	}

	public static void main(String[] args) {
//		for(int time  = 1; time <= 1000; time ++) {
//			System.out.println("???" + time + "?????????---------");
//			List<List<GuihzCard>> cardds = faPai();
//			List<GuihzCard> hand1 = cardds.get(0);
//			GuihuziTool.sortMin(hand1);
//			System.out.println("??????1??????:" + hand1);
//			List<GuihzCard> hand2 = cardds.get(1);
//			GuihuziTool.sortMin(hand2);
//			System.out.println("??????2??????:" + hand2);
//			List<GuihzCard> hand3 = cardds.get(2);
//			GuihuziTool.sortMin(hand3);
//			System.out.println("??????3??????:" + hand3);
//		}
	}
}
