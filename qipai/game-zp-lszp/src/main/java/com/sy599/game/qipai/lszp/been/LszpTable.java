package com.sy599.game.qipai.lszp.been;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
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
import org.apache.commons.lang3.math.NumberUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.DataStatistics;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserGroupPlaylog;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.dao.DataStatisticsDao;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayPaohuziRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzInfoRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.lszp.constant.PaohuziConstant;
import com.sy599.game.qipai.lszp.constant.PaohzCard;
import com.sy599.game.qipai.lszp.rule.PaohuziIndex;
import com.sy599.game.qipai.lszp.rule.PaohuziMingTangRule;
import com.sy599.game.qipai.lszp.rule.PaohzCardIndexArr;
import com.sy599.game.qipai.lszp.rule.RobotAI;
import com.sy599.game.qipai.lszp.tool.PaohuziHuLack;
import com.sy599.game.qipai.lszp.tool.PaohuziResTool;
import com.sy599.game.qipai.lszp.tool.PaohuziTool;
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

public class LszpTable extends BaseTable {
	/*** 玩家map */
    private Map<Long, LszpPlayer> playerMap = new ConcurrentHashMap<>();
	/*** 座位对应的玩家 */
    private Map<Integer, LszpPlayer> seatMap = new ConcurrentHashMap<>();
    /**
	 * 开局所有底牌
	 **/
    private volatile List<Integer> startLeftCards = new ArrayList<>();
    /**
	 * 当前桌面底牌
	 **/
    private volatile List<PaohzCard> leftCards = new ArrayList<>();
	/*** 应该要打牌的flag */
    private volatile int toPlayCardFlag;
    private volatile PaohuziCheckCardBean autoDisBean;
    private volatile int moSeat;
    private volatile PaohzCard zaiCard;
    private volatile PaohzCard beRemoveCard;
    private volatile int playerCount = 3;
    private volatile List<Integer> huConfirmList = new ArrayList<>();
	/*** 摸牌时对应的座位 */
    private volatile KeyValuePair<Integer, Integer> moSeatPair;
	/*** 摸牌时对应的座位 */
    private volatile KeyValuePair<Integer, Integer> checkMoMark;
    private volatile int sendPaoSeat;

    /**
	 * 0胡 1碰 2栽 3提 4吃 5跑 6臭栽
	 */
    private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
    private volatile List<PaohzCard> nowDisCardIds = new ArrayList<>();
    /**
	 * 托管时间
	 */
    private volatile int autoTimeOut = Integer.MAX_VALUE;
    private volatile int autoTimeOut2 = Integer.MAX_VALUE;
	// 是否已经完成发牌
    private int finishFapai=0;
    private volatile int timeNum = 0;
	// 起手提龙标记
    private boolean qiShouTi=false;

//    private volatile int lockHandNum=0;


	// 需要修改，用disNum+moNum代替
    private volatile int moNum=0;
    private volatile int disNum=0;
	// 用来记录必胡的人的座位
    private int biHuSeat=0;
	// 用该状态记录该回合是摸排还是出牌（吃碰后的出牌），1：出牌，2：摸牌，0：尚未开始或者正在吃碰中
    private volatile int disOrMo=0;



	// 起胡息数量
    private int floorValue=6;
	// 是否毛胡
    private int maoHu=0;
	// 15张玩法
    private int fifteenCards=1;
	// 飘分 1不飘 2飘 1/2/3 3飘2/3/5
    private int piaoFen=0;
	// 是否勾选首局庄家随机
    private int bankerRand=0;
	// 明龙 1出牌后明龙 2发牌后明龙
    private int mingLong=1;
	// 是否加倍：0否，1是
    private int jiaBei;
	// 加倍分数：低于xx分进行加倍
    private int jiaBeiFen;
	// 加倍倍数：翻几倍
    private int jiaBeiShu;
	/** 托管1：单局，2：全局 */
    private int autoPlayGlob;

	private int autoTableCount;
	// 抽牌 0：不抽，10：抽10张，20：抽20张
    private int chouCardNum;
	// 低于below加分
    private int belowAdd=0;
    private int below=0;
	// 抽牌牌堆
    List<Integer> chouCards=new ArrayList<>();
    //醒牌
    private int xingCard=0;


    /**
	 * 玩家位置对应临时操作 当同时存在多个可做的操作时 1当优先级最高的玩家最先操作 则清理所有操作提示 并执行该操作
	 * 2当操作优先级低的玩家先选择操作，则玩家先将所做操作存入临时操作中 当玩家优先级最高的玩家操作后 在判断临时操作是否可执行并执行
	 */
    private Map<Integer, TempAction> tempActionMap = new ConcurrentHashMap<>();
    //跑胡
    private int paoHu=0;


    public boolean createTable(Player player, int play, int bureauCount, List<Integer> params, boolean saveDb) throws Exception {
        long id = getCreateTableId(player.getUserId(), play);
        if (id<=0){
            return false;
        }
        if(saveDb){
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
        }else{
            setPlayType(play);
            setDaikaiTableId(daikaiTableId);
            this.id=id;
            this.totalBureau=bureauCount;
            this.playBureau=1;
        }
		int playerCount = StringUtil.getIntValue(params, 7, 3);// 比赛人数
        setPlayerCount(playerCount);
		int payType = StringUtil.getIntValue(params, 9, 0);// 房费方式
        piaoFen = StringUtil.getIntValue(params, 10, 0);// 飘分
        if(playerCount==2) {
            chouCardNum = StringUtil.getIntValue(params, 11, 0);
        }
        int time=StringUtil.getIntValue(params, 12, 0);
        if(time ==1) {
            time =60;
        }
        if(time>0) {
            this.autoPlay =true;
        }
        this.jiaBei = StringUtil.getIntValue(params, 13, 0);
        this.jiaBeiFen = StringUtil.getIntValue(params, 14, 100);
        this.jiaBeiShu = StringUtil.getIntValue(params, 15, 1);
        autoPlayGlob=StringUtil.getIntValue(params, 16, 0);
        if(playerCount==2){
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
        if (playerCount<=1||playerCount>4){
            return false;
        }

        if(this.getMaxPlayerCount() != 2){
            jiaBei = 0 ;
        }
        setPayType(payType);
        String[] split = ResourcesConfigsUtil.loadStringValue("ServerConfig", "autoTimeOutLdfpfNormal","60,30,20").split(",");
        if (isGoldRoom()){
            try{
                GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(id);
                if (goldRoom!=null){
                    modeId = goldRoom.getModeId();
                }
            }catch(Exception e){
            }
            autoTimeOut = Integer.parseInt(split[0])*1000;
        }else{
            if(autoPlay){
                autoTimeOut2=autoTimeOut = time*1000;
//                autoTimeOut2=autoTimeOut = 10*1000;
            }
        }
        changeExtend();
        LogUtil.msgLog.info("createTable tid:"+getId()+" "+player.getName() + " params"+params.toString());
        return true;
    }



    @Override
    public JsonWrapper buildExtend0(JsonWrapper wrapper) {
//		JsonWrapper wrapper = new JsonWrapper("");
        wrapper.putString(1, StringUtil.implode(huConfirmList, ","));
        wrapper.putInt(2, toPlayCardFlag);
        wrapper.putInt(3, moSeat);
        if (moSeatPair != null) {
            String moSeatPairVal = moSeatPair.getId() + "_" + moSeatPair.getValue();
            wrapper.putString(4, moSeatPairVal);
        }
        if (autoDisBean != null) {
            wrapper.putString(5, autoDisBean.buildAutoDisStr());

        } else {
            wrapper.putString(5, "");
        }
        if (zaiCard != null) {
            wrapper.putInt(6, zaiCard.getId());
        }
        wrapper.putInt(7, sendPaoSeat);
        if (beRemoveCard != null) {
            wrapper.putInt(8, beRemoveCard.getId());
        }
        wrapper.putString("startLeftCards", startLeftCardsToJSON());
        wrapper.putString(10, StringUtil.implode(chouCards, ","));
        wrapper.putInt(11, playerCount);
        wrapper.putInt(12, jiaBei);
        wrapper.putInt(13, jiaBeiFen);
        wrapper.putInt(14, jiaBeiShu);
        wrapper.putInt(15, disNum);
        wrapper.putInt(16, moNum);
        wrapper.putInt(17, disOrMo);
        wrapper.putInt(18, autoPlayGlob);
        wrapper.putInt(19, autoTimeOut);
        wrapper.putInt(20, piaoFen);
        wrapper.putInt(21, finishFapai);
        JSONArray tempJsonArray = new JSONArray();
        for (int seat : tempActionMap.keySet()) {
            tempJsonArray.add(tempActionMap.get(seat).buildData());
        }
        wrapper.putString(22, tempJsonArray.toString());
        wrapper.putInt(23, chouCardNum);
        wrapper.putInt(24, belowAdd);
        wrapper.putInt(25, below);
        wrapper.putInt(26, paoHu);
        wrapper.putInt(27, xingCard);
        return wrapper;
    }

    /**
	 * 需要注意，如果保存了新字段，当服务器重启更新后，新字段为默认值（通常为0）
	 * 若项目已上线，且该字段影响程序流程或最终结果，则会产生问题，需要做兼容处理。
	 * 
	 * @param wrapper
	 */
    @Override
    public void initExtend0(JsonWrapper wrapper) {
        startLeftCards = loadStartLeftCards(wrapper.getString("startLeftCards"));
        if (payType== -1) {
            String isAAStr =  wrapper.getString("isAAConsume");
            if (!StringUtils.isBlank(isAAStr)) {
                this.payType = Boolean.parseBoolean(wrapper.getString("isAAConsume"))?1:2;
            } else {
                payType=1;
            }
        }
        String hu = wrapper.getString(1);
        if (!StringUtils.isBlank(hu)) {
            huConfirmList = StringUtil.explodeToIntList(hu);
        }
        toPlayCardFlag = wrapper.getInt(2, 0);
        moSeat = wrapper.getInt(3, 0);
        String moSeatVal = wrapper.getString(4);
        if (!StringUtils.isBlank(moSeatVal)) {
            moSeatPair = new KeyValuePair<>();
            String[] values = moSeatVal.split("_");
            String idStr = StringUtil.getValue(values, 0);
            if (!StringUtil.isBlank(idStr)) {
                moSeatPair.setId(Integer.parseInt(idStr));
            }

            moSeatPair.setValue(StringUtil.getIntValue(values, 1));
        }
        String autoDisPhz = wrapper.getString(5);
        if (!StringUtils.isBlank(autoDisPhz)) {
            autoDisBean = new PaohuziCheckCardBean();
            autoDisBean.initAutoDisData(autoDisPhz);
        }
        zaiCard = PaohzCard.getPaohzCard(wrapper.getInt(6, 0));
        sendPaoSeat = wrapper.getInt(7, 0);
        beRemoveCard = PaohzCard.getPaohzCard(wrapper.getInt(8, 0));
        String chouCardsStr = wrapper.getString(10);
        if (StringUtils.isNotBlank(chouCardsStr)) {
            chouCards = StringUtil.explodeToIntList(chouCardsStr);
        }
        playerCount = wrapper.getInt(11, 3);
        jiaBei = wrapper.getInt(12, 0);
        jiaBeiFen = wrapper.getInt(13, 0);
        jiaBeiShu = wrapper.getInt(14, 0);
        disNum=wrapper.getInt(15,0);
        moNum=wrapper.getInt(16,0);
        disOrMo=wrapper.getInt(17,0);
        autoPlayGlob = wrapper.getInt(18,0);
        autoTimeOut = wrapper.getInt(19,0);
        piaoFen=wrapper.getInt(20,0);
        finishFapai=wrapper.getInt(21,0);
        if(autoPlay&&autoTimeOut<=1) {
            autoTimeOut2=autoTimeOut = 60000;
        }
        tempActionMap = loadTempActionMap(wrapper.getString(22));
        chouCardNum=wrapper.getInt(23,0);
        belowAdd=wrapper.getInt(24,0);
        below=wrapper.getInt(25,0);
        paoHu=wrapper.getInt(26,0);
        xingCard=wrapper.getInt(27,0);
    }

    public int getMaoHu() {
        return maoHu;
    }

    /**
	 * 获取所有底牌内容
	 */
    public List<Integer> getStartLeftCards() {
        return startLeftCards;
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
    public int getIsShuffling() {
        return super.getIsShuffling();
    }


    public void setWinLostPiaoFen(LszpPlayer win, LszpPlayer lost) {
        if(piaoFen>0){
            lost.setWinLostPiaoFen(-win.getPiaoFen()-lost.getPiaoFen());
            win.setWinLostPiaoFen(win.getWinLostPiaoFen()+win.getPiaoFen()+lost.getPiaoFen());
        }
    }

    /**
	 * 娄底放炮罚胡牌结算
	 */
    @Override
    public void calcOver() {
        if (state == table_state.ready) {
            return;
        }
        boolean isHuangZhuang = false;
        List<Integer> winList=new ArrayList<>(huConfirmList);
        if ((winList.size() == 0 && leftCards.size() == 0)) {
			// 流局
            isHuangZhuang = true;
        }
        List<Integer> mt = null;
        int tun = 0;
        int winTun=0;
        int lostTun=0;
        boolean isOver = false;
        if(!isHuangZhuang){
            for (int winSeat : winList) {
				// 赢的玩家
                LszpPlayer winPlayer = seatMap.get(winSeat);
                winPlayer.changeAction(PaohuziConstant.ACTION_COUNT_INDEX_HU, 1);
                mt = winPlayer.getHu().getMingTang();
                int huxi=winPlayer.getTotalHu();
                tun=countXiTun(huxi);
//                tun+=xingTun(winPlayer);
                lostTun=PaohuziMingTangRule.countXiTun(tun,mt);
                if(disOrMo==1){
					// 放炮
                    winTun=lostTun;
                    LszpPlayer player = seatMap.get(disCardSeat);
                    player.calcResult(this, 1, -lostTun, isHuangZhuang);
                    setWinLostPiaoFen(winPlayer,player);
                }else if(isMoFlag()||(disOrMo==0&&moNum==0)){
					// 摸胡
                    if(moSeat==winSeat){
                        winPlayer.changeAction(PaohuziConstant.ACTION_COUNT_INDEX_ZIMO, 1);
                    }
                    for (int seat : seatMap.keySet()){
                        if (winSeat!=seat) {
                            LszpPlayer player = seatMap.get(seat);
                            winTun+=lostTun;
                            player.calcResult(this, 1, -lostTun, isHuangZhuang);
                            setWinLostPiaoFen(winPlayer,player);
                        }
                    }
                }
                winPlayer.calcResult(this, 1, winTun, isHuangZhuang);
                setLastWinSeat(winSeat);
            }
            for(int seat:seatMap.keySet()){
                LszpPlayer player = seatMap.get(seat);
                player.setPoint(player.getPoint()+player.getWinLostPiaoFen());
                player.setTotalPoint(player.getTotalPoint()+player.getWinLostPiaoFen());
            }
        }
        if(playBureau == totalBureau){
            isOver=true;
            changeTableState(table_state.over);
        }
        if(autoPlayGlob >0) {
			// //是否解散
            boolean diss = false;
            if(autoPlayGlob ==1) {
            	 for (LszpPlayer seat : seatMap.values()) {
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
            	 isOver =true;
            }
        }
        

        calcAfter();
        ClosingPhzInfoRes.Builder res = sendAccountsMsg(isOver, winList, 0, mt, lostTun, false, null,null);
        saveLog(isOver,0L, res.build());
        if (isOver) {
			// 这个方法应该是和红包相关的（不知道现在还有没有用）
            calcOver1();
            calcOver2();
            calcOver3();
            diss();
        } else {
            calcOver1();
            initNext(isOver);
        }

        for (Player player : seatMap.values()) {
            player.saveBaseInfo();
        }
    }

	private boolean checkAuto3() {
		boolean diss = false;
		boolean diss2 = false;
		for (LszpPlayer seat : seatMap.values()) {
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

    private int countXiTun(int huxi){
        return huxi/3;
    }

    /**
     * 醒牌加屯
     * @param winPlayer
     * @return
     */
    private int xingTun(LszpPlayer winPlayer){
        Set<Integer> sameVal=new HashSet<>();
        PaohzCard xingCard=null;
        List<PaohzCard> handCards = PaohuziTool.toPhzCards(winPlayer.getHandPais());
        if(leftCards.size()==0){
            //海底捞
            xingCard = handCards.get(handCards.size()-1);
        }else {
            xingCard = getNextCard();
        }
        this.xingCard=xingCard.getId();

        for (PaohzCard card:handCards) {
            if(card.getVal()==xingCard.getVal())
                sameVal.add(card.getId());
        }
        return sameVal.size();
    }


    @Override
    public void saveLog(boolean over,long winId, Object resObject) {
        ClosingPhzInfoRes res = (ClosingPhzInfoRes) resObject;
        LogUtil.d_msg("tableId:" + id + " play:" + playBureau + " over:" + res);
        String logRes = JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResLog(res));
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
        userLog.setType(creditMode == 1 ? 2 : 1 );
        userLog.setMaxPlayerCount(playerCount);
        userLog.setGeneralExt(buildGeneralExtForPlaylog().toString());
        long logId = TableLogDao.getInstance().save(userLog);
        saveTableRecord(logId, over, playBureau);
        UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
        if (!isGoldRoom()){
            for (LszpPlayer player : playerMap.values()) {
                player.addRecord(logId, playBureau);
            }
        }
    }

    @Override
    protected void loadFromDB1(TableInf info) {
        if (!StringUtils.isBlank(info.getNowDisCardIds())) {
            this.nowDisCardIds = PaohuziTool.explodePhz(info.getNowDisCardIds(), ",");
        }
        if (!StringUtils.isBlank(info.getLeftPais())) {
            this.leftCards = PaohuziTool.explodePhz(info.getLeftPais(), ",");
        }
        String[] split = ResourcesConfigsUtil.loadStringValue("ServerConfig", "autoTimeOutLdfpfNormal","60,30,20").split(",");
        if (isGoldRoom()){
            autoTimeOut = Integer.parseInt(split[0])*1000;
        }else{
//            autoTimeOut = Integer.parseInt(split[0])*1000;
//            autoTimeOut2 = Integer.parseInt(split[1])*1000;
//            autoTimeOut3 = Integer.parseInt(split[2]);
        }
    }

    @Override
    protected void sendDealMsg() {
        sendDealMsg(0);
    }

    @Override
    protected void sendDealMsg(long userId) {
        if(finishFapai==0)
            return;
		// 天胡或者暗杠
//        int lastCardIndex = RandomUtils.nextInt(21);
        LszpPlayer winPlayer = seatMap.get(lastWinSeat);

        for (LszpPlayer tablePlayer : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            res.addAllHandCardIds(tablePlayer.getHandPais());
            res.setNextSeat(lastWinSeat);
			res.setGameType(getWanFa());// 1跑得快 2麻将
            res.setRemain(leftCards.size());
            res.setBanker(lastWinSeat);
            res.addXiaohu(winPlayer.getHandPais().get(0));
            tablePlayer.writeSocket(res.build());
            
        	if(tablePlayer.isAutoPlay()) {
       		 addPlayLog(tablePlayer.getSeat(), PaohzDisAction.action_tuoguan + "",1 + "");
        	}
        }
    }

    @Override
    public synchronized void startNext() {
        if(finishFapai==1)
            checkAction();
    }

    public void play(LszpPlayer player, List<Integer> cardIds, int action) {
        play(player, cardIds, action, false, false, false);
    }

    private void hu(LszpPlayer player, List<PaohzCard> cardList, int action, PaohzCard nowDisCard, boolean system) {
		if (!system) {// 系统调用，跳过合法检测
            if (!actionSeatMap.containsKey(player.getSeat())) {
                return;
            }
            if (huConfirmList.contains(player.getSeat())) {
                return;
            }
            if (!checkAction(player, action,cardList,nowDisCard)) {
                player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
                // player.writeErrMsg(LangMsgEnum.code_29);
                return;
            }
            List<Integer> actionList = actionSeatMap.get(player.getSeat());
            if (actionList.get(0) != 1) {
                return;
            }
        }
        if (!checkAction(player, action,cardList,nowDisCard)) {
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
            // player.writeErrMsg(LangMsgEnum.code_29);
            return;
        }
        PaohuziHuLack hu=null;

        List<PaohuziHuLack> pao = player.checkPaoHu1(nowDisCard, isSelfMo(player), true);
        if(player.isZaiTiHu()){
            nowDisCard=null;
        }
        List<PaohuziHuLack> ping = player.checkHu1(nowDisCard, isSelfMo(player));
        logDiHu(player,ping.size());
        hu = filtrateHu(player, pao, ping);
        if(hu!=null&&!player.isSiShou()){
            if (hu.getPaohuAction()==PaohzDisAction.action_ti){
                ti(player, hu.getPaohuList(),nowDisCardIds.get(0),PaohzDisAction.action_ti,false);
                List<PaohuziHuLack> list = player.checkHu1(null, isSelfMo(player));
                hu = filtrateHu(player, null, list);
            }else if(hu.getPaohuAction()==PaohzDisAction.action_pao){
                pao(player,hu.getPaohuList(),nowDisCardIds.get(0),PaohzDisAction.action_pao,true,false,true);
//                play(player, PaohuziTool.toPhzCardIds(hu.getPaohuList()), hu.getPaohuAction(), false, true, false);
                List<PaohuziHuLack> list = player.checkHu1(null, isSelfMo(player));
                hu = filtrateHu(player, null, list);
            }

        }


        if (hu!=null&&hu.isHu()) {
            player.setHuxi(hu.getHuxi());
            player.setHu(hu);
            huConfirmList.add(player.getSeat());
            addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
            sendActionMsg(player, action, null, PaohzDisAction.action_type_action);
            calcOver();
        } else {
			broadMsg(player.getName() + " 不能胡牌");
        }

    }

    private void logDiHu(LszpPlayer player,int size){
        StringBuilder sb = new StringBuilder("hyshk.dihu");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(size);
        LogUtil.msgLog.info(sb.toString());
    }


    public PaohuziHuLack filtrateHu(LszpPlayer player, List<PaohuziHuLack> pao, List<PaohuziHuLack> ping){
        List<PaohuziHuLack> zaiTi=new ArrayList<>();
        List<PaohuziHuLack> hu=new ArrayList<>();
        if (pao!=null&&!pao.isEmpty()){
            for (PaohuziHuLack lack:pao){
				// 跑胡和平湖不可能胡息为0（飘胡）
                int allHuxi=lack.getHuxi() + player.getOutHuxi() +player.getZaiHuxi();
                if (allHuxi < getFloorValue())
                    continue;
				// 栽胡和提胡优先级高于其他胡行
                if(lack.getPaohuAction()==PaohzDisAction.action_ti&&player.isZaiTiHu()){
                    zaiTi.add(lack);
                    continue;
                }
                hu.add(lack);
            }
			// 多个栽提胡牌型取最大胡息
            if(zaiTi.size()>=1){
                return getMaxHu(player,zaiTi);
            }
        }
        if (ping!=null&&!ping.isEmpty()){
            for (PaohuziHuLack lack:ping){
                int allHuxi=lack.getHuxi() + player.getOutHuxi() +player.getZaiHuxi();
                if (allHuxi >= getFloorValue()||((getMaoHu()==1&&allHuxi==0)))
                    hu.add(lack);
            }
        }
        if(hu.size()>=1){
            return getMaxHu(player,hu);
        }
        return null;
    }

    public PaohuziHuLack getMaxHu(LszpPlayer player, List<PaohuziHuLack> hu){
        int maxpoint=-1;
        PaohuziHuLack lack1=null;
        for (PaohuziHuLack lack:hu){
            List<Integer> arr = PaohuziMingTangRule.calcMingTang(player, disOrMo==1);
            int point = lack.getHuxi()+player.getOutHuxi() +player.getZaiHuxi();
            if(maxpoint<point){
                maxpoint=point;
                lack1=lack;
                lack1.setMingTang(arr);
                lack1.setFinallyPoint(point);
            }
        }
        player.setHuxi(lack1.getHuxi());
        return lack1;
    }


    /**
	 * 是否自摸
	 *
	 * @param player
	 * @return
	 */
    public boolean isSelfMo(LszpPlayer player) {
        if (moSeatPair != null) {
            return moSeatPair.getValue().intValue() == player.getSeat();
        }
        return false;
    }

    /**
	 * 提
	 */
    private void ti(LszpPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action, boolean moPai) {
		// cards肯定是4个相同的
        if (cardList == null) {
			System.out.println("提不合法:" + cardList);
			player.writeErrMsg("提不合法:" + cardList);
            return;
        }

        if (cardList.size() == 1) {
            List<PaohzCard> tiCards = player.getTiCard(cardList.get(0));
            if (tiCards == null || tiCards.size() != 3) {
				System.out.println("提不合法:" + tiCards);
				player.writeErrMsg("提不合法:" + tiCards);
                return;
            }
            cardList.addAll(tiCards);
        } else {
            if (!player.getHandPhzs().contains(cardList.get(0))) {
                return;
            }
        }
		// 是否栽跑
        boolean isZaiPao = player.isZaiPao(cardList.get(0).getVal());

        if (cardList.size() != 4 && !cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        }

        if (cardList.size() != 4) {
            return;
        }

        if (!PaohuziTool.isSameCard(cardList)) {
			System.out.println("提不合法:" + cardList);
			player.writeErrMsg("提不合法:" + cardList);
            return;
        }
        player.changeAction(PaohuziConstant.ACTION_COUNT_INDEX_TI, 1);
        addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
        if (nowDisCard != null) {
            getDisPlayer().removeOutPais(nowDisCard);
        }
        player.disCard(action, cardList);
        clearAction();
        setAutoDisBean(null);

        PaohuziCheckCardBean checkAutoDis = checkAutoDis(player, false);
        if (checkAutoDis != null) {
            playAutoDisCard(checkAutoDis, true);
        }

		// 检查是否能胡牌
        PaohuziCheckCardBean checkCard = player.checkPaoHu(action);
        checkPaohuziCheckCard(checkCard);

		// 是否能出牌
        if (!moPai) {
			// 不是轮到自己摸牌的时候提的牌
            boolean disCard = setDisPlayer(player, action, checkCard.isHu());
            if (!disCard) {
//                checkMo();
            }

        }
        sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action, isZaiPao, false);
        if(checkCard.isHu()){
            hu(player,null,PaohzDisAction.action_hu,null,true);
        }
    }

    /**
	 * 栽(臭栽)
	 *
	 * @param cardList
	 *            要栽的牌
	 */
    private void zai(LszpPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }

        boolean isFristDisCard = player.isFristDisCard();
        PaohuziCheckCardBean checkAutoDis = checkAutoDis(player, false);
        if (checkAutoDis != null) {
            playAutoDisCard(checkAutoDis, true);
        }
        getDisPlayer().removeOutPais(nowDisCard);
        if (!cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        }
        setBeRemoveCard(nowDisCard);
        if (action == PaohzDisAction.action_zai) {
            setZaiCard(nowDisCard);

        }
        addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
        player.disCard(action, cardList);
        clearAction();
        setAutoDisBean(null);
		// 检查是否能胡牌
        PaohuziCheckCardBean checkCard = player.checkPaoHu(action);
        checkPaohuziCheckCard(checkCard);
		// 是否能出牌
        boolean disCard = setDisPlayer(player, action, isFristDisCard, checkCard.isHu());
        sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action);
        if(checkCard.isHu()){
            hu(player,null,PaohzDisAction.action_hu,null,true);
        }

    }

    /**
	 * 跑
	 */
    private void pao(LszpPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action, boolean isHu, boolean isPassHu , boolean system) {

        if (cardList.size() != 3 && cardList.size() != 1) {
			broadMsg("跑的张数不对:" + cardList);
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if(!system){
            if (actionList == null) {
                return;
            }
            if (!isHu && actionList.get(5) != 1) {
                return;
            }
        }
		// 能跑胡的情况下不 胡挡不住跑
        if (!isHu && !checkAction(player, action, cardList, nowDisCard)) {
			// 发现别人能胡
			// 能跑能胡的情况下
            if (actionList.get(0) == 1) {
                actionList.set(0, 0);
                addAction(player.getSeat(), actionList);
				// 更新前台数据
                setSendPaoSeat(player.getSeat());
                sendPlayerActionMsg(player);
            }
            // player.writeErrMsg(LangMsgEnum.code_29);
            return;
        }
        boolean isZaiPao = player.isZaiPao(cardList.get(0).getVal());
        getDisPlayer().removeOutPais(nowDisCard);
        if (!cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        }
        setBeRemoveCard(nowDisCard);

        if (cardList.size() == 1) {
			// 如果是一张牌说明已经在出的牌里面了
            List<PaohzCard> list = player.getSameCards(nowDisCard);
            cardList.addAll(list);
        }

        if (cardList.size() != 4) {
            return;
        }

		// 检测是否能提
        boolean isFristDisCard = player.isFristDisCard();
        PaohuziCheckCardBean checkAutoDis = checkAutoDis(player, false);
        if (checkAutoDis != null) {
            playAutoDisCard(checkAutoDis, true);
        }
        player.changeAction(PaohuziConstant.ACTION_COUNT_INDEX_PAO, 1);
        addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
        player.disCard(action, cardList);
        clearAction();
        setAutoDisBean(null);

        if (!isHu && !isPassHu && isMoFlag()) {
            PaohuziCheckCardBean checkCard = player.checkPaoHu(action);
            checkPaohuziCheckCard(checkCard);
        }

		// 是否能出牌
        if (!isHu) {
            boolean disCard = setDisPlayer(player, action, isFristDisCard, false);
            sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action, isZaiPao, !disCard);
            if (!disCard) {
                if (PaohuziConstant.isAutoMo) {
                    checkMo();
                }
            }
        } else {
            sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action, isZaiPao, false);
        }

    }

    /**
	 * 出牌
	 */
    private void disCard(LszpPlayer player, List<PaohzCard> cardList, int action) {
        if (!actionSeatMap.isEmpty()) {
            player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, cardList.get(0).getId());
			LogUtil.e("动作:" + JacksonUtil.writeValueAsString(actionSeatMap));
            return;
        }

        if (toPlayCardFlag != 1) {
            player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, cardList.get(0).getId());
			LogUtil.e(player.getName() + "错误 toPlayCardFlag:" + toPlayCardFlag + "出牌");
            checkMo();
            return;
        }

        if (player.getSeat() != nowDisCardSeat) {
            player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, cardList.get(0).getId());
			player.writeErrMsg("轮到:" + nowDisCardSeat + "出牌");
            return;
        }
        if (cardList.size() != 1) {
            player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, cardList.get(0).getId());
			player.writeErrMsg("出牌数量不对:" + cardList);
            return;
        }

        PaohuziHandCard cardBean = player.getPaohuziHandCard();
        if (!cardBean.isCanoperateCard(cardList.get(0))) {
            player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, cardList.get(0).getId());
			player.writeErrMsg("该牌不能单出:" + cardList);
			LogUtil.e("该牌不能单出:" + cardList);
            return;
        }
        if(cardList.size()>1){
            disOrMo=3;
        }else {
            disNum++;
            disOrMo=1;
            player.addDisNum();
        }
        player.setZaiTiHu(false);
		// 判断是否为放招
        boolean paoFlag = isFangZhao(player, cardList.get(0));
        if (paoFlag) {
			if (player.isAutoPlay()) {// 托管自动放招
                player.setFangZhao(1);
                for (Player playerTemp : getSeatMap().values()) {
                    playerTemp.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao, player.getUserId() + "", 1 + "");
                }
            }else if (!player.isFangZhao() && !player.isRobot()) {
                player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, cardList.get(0).getId());
				LogUtil.msgLog.info("----tableId:" + getId() + "---userName:" + player.getName()
						+ "------是否确定放招:--->>>>>>" + cardList.get(0));
                player.writeComMessage(WebSocketMsgType.res_com_code_fangzhao, cardList.get(0).getId());
                return;
            }
        }


        addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
		checkFreePlayerTi(player, action);// 检查闲家提
        player.disCard(action, cardList);
        markMoSeat(player.getSeat(), action);
        clearMoSeatPair();
		setToPlayCardFlag(0); // 应该要打牌的flag
        setDisCardSeat(player.getSeat());
        setNowDisCardIds(cardList);
        setNowDisCardSeat(getNextDisCardSeat());
        PaohuziCheckCardBean autoDisCard = checkDisAction(player, action, cardList.get(0), true);
        if(autoDisCard==null)
            return;
        if(biHuSeat!=0){
            if (actionSeatMap.containsKey(biHuSeat))
                actionSeatMap.remove(biHuSeat);
        }
        sendActionMsg(player, action, cardList, PaohzDisAction.action_type_dis);
        if(biHuSeat!=0){
            LszpPlayer p = seatMap.get(biHuSeat);
            if(p==null){
                StringBuilder sb=new StringBuilder("lszp.bihu");
                sb.append("|").append(getId());
                sb.append("|").append(getPlayBureau());
                sb.append("|").append(biHuSeat);
                sb.append("|").append(player.isAutoPlay() ? 1 : 0);
                sb.append("|").append(seatMap==null?null:seatMap);
                LogUtil.msgLog.info(sb.toString());
                return;
            }
            hu(p,null,PaohzDisAction.action_hu,cardList.get(0),true);
        }
        checkAutoMo();
    }

    private void checkAutoMo() {
        if (isTest()) {
            checkMo();
        }
    }

    private void tiLong(LszpPlayer player) {
        boolean isTiLong = false;
        List<PaohzCard> cardList = new ArrayList<>();
        while (player.getOweCardCount() < -1) {
            if (!isTiLong) {
                isTiLong = true;
                removeAction(player.getSeat());
            }
            PaohzCard card = null;
            if (GameServerConfig.isDeveloper()) {
                if (card == null) {
                    card = getNextCard(106);
                }
                if (card == null) {
                    card = getNextCard(4);
                }
            }

            if (card == null) {
                card = getNextCard();
            }
            player.tiLong(card);
            cardList.add(card);

            addPlayLog(player.getSeat(), PaohzDisAction.action_buPai + "", (card == null ? 0 : card.getId()) + "");
            StringBuilder sb = new StringBuilder("lszp.tiLong");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.isAutoPlay() ? 1 : 0);
            sb.append("|").append("tiLong");
            sb.append("|").append(card);
            LogUtil.msgLog.info(sb.toString());
        }

        if (isTiLong) {

            sendActionMsg(player, PaohzDisAction.action_tilong, cardList, PaohzDisAction.action_type_action, false, false);


            PaohuziCheckCardBean checkCard = player.checkCard(null, true, true, false);
            if (checkPaohuziCheckCard(checkCard)) {
                playAutoDisCard(checkCard);
                if (player.getSeat() != lastWinSeat && checkCard.isTi()) {
					// 闲家提龙补牌后再提龙，需要补牌
                    player.setOweCardCount(player.getOweCardCount() - 1);
                }
                tiLong(player);
            }
        }
    }

    public void checkFreePlayerTi(LszpPlayer player, int action) {
        if (player.getSeat() == lastWinSeat && player.isFristDisCard() && action != PaohzDisAction.action_ti) {
            for (int seat : getSeatMap().keySet()) {
                if (lastWinSeat == seat) {
                    continue;
                }
                LszpPlayer nowPlayer = seatMap.get(seat);
                PaohuziCheckCardBean checkCard = nowPlayer.checkCard(null, true, true, false);
                if (checkPaohuziCheckCard(checkCard)) {
                    playAutoDisCard(checkCard);
                    if (nowPlayer.isFristDisCard()) {
                        nowPlayer.setFristDisCard(false);
                    }
                    tiLong(nowPlayer);
                }
                checkSendActionMsg();
            }
        }
    }

    /**
	 * 碰
	 */
    private void peng(LszpPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (!checkAction(player, action, cardList, nowDisCard)) {
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
            return;
        }

        cardList = player.getPengList(nowDisCard, cardList);
        if (cardList == null) {
			player.writeErrMsg("不能碰");
            return;
        }
        if (!cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        }
        setBeRemoveCard(nowDisCard);

        boolean isFristDisCard = player.isFristDisCard();
        PaohuziCheckCardBean checkAutoDis = checkAutoDis(player, false);
        if (checkAutoDis != null) {
            playAutoDisCard(checkAutoDis, true);

        }
        getDisPlayer().removeOutPais(nowDisCard);
        addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
        player.disCard(action, cardList);
        clearAction();

        boolean disCard = setDisPlayer(player, action, isFristDisCard, false);
        sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action);
        if (!disCard) {
            // checkMo();
        }

		// 碰的情况,把所有玩家的过牌去掉
        if (isMoFlag()) {
            for (LszpPlayer seatPlayer : seatMap.values()) {
                if (seatPlayer.getSeat() == player.getSeat()) {
                    continue;
                }
                seatPlayer.removePassChi(nowDisCard.getVal());
            }
        }
    }



    /**
	 * 过
	 */
    private void pass(LszpPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
			// player.writeErrMsg("该玩家没有找到可以过的动作");
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        List<Integer> list = PaohzDisAction.parseToDisActionList(actionList);
		// 栽，提，跑是不可以过的
        if (list.contains(PaohzDisAction.action_zai) || list.contains(PaohzDisAction.action_ti) || list.contains(PaohzDisAction.action_pao) || list.contains(PaohzDisAction.action_chouzai)) {
            return;
        }
		// 如果没有吃，碰，胡也是不可以过的
        if (!list.contains(PaohzDisAction.action_chi) && !list.contains(PaohzDisAction.action_peng) && !list.contains(PaohzDisAction.action_hu)) {
            return;
        }

		// 可以胡牌，然后点了过
        boolean isPassHu = actionList.get(0) == 1;
        player.setZaiTiHu(false);
        if (actionList.get(0) == 1 && player.getHandPhzs().isEmpty()) {
			player.writeErrMsg("手上已经没有牌了");
            return;
        }

        if(action==PaohzDisAction.action_pass){
            int logId;
            if(paoHu==1){
                logId=0;
            }else {
                logId = nowDisCard.getId();
            }
            addPlayLog(player.getSeat(), PaohzDisAction.action_guo + "",logId+"");
            setPaoHu(0);
        }
        int val = 0;
        if (nowDisCard != null) {
            val = nowDisCard.getVal();
        }

        boolean addPassChi = false;
        if (player.getSeat() == moSeat) {
            addPassChi = true;
        }

		// 将pass的吃碰值添加到passChi或passPeng中
        for (int passAction : list) {
            player.pass(passAction, val, addPassChi);

        }
        removeAction(player.getSeat());
		// 自动出牌
        if (autoDisBean != null) {
            refreshTempAction(player);
            playAutoDisCard(autoDisBean);
        } else {
            PaohuziCheckCardBean checkCard = player.checkCard(nowDisCard, isSelfMo(player), isPassHu, false, false, true);
            checkCard.setPassHu(isPassHu);
            boolean check = checkPaohuziCheckCard(checkCard);
            markMoSeat(player.getSeat(), action);
			// 应该是在此处告诉前端，已执行pass操作
            sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action);
            if (check) {
                playAutoDisCard(checkCard, true);
            } else {
                if (PaohuziConstant.isAutoMo) {
					// 在此处查询可以摸排的玩家并通知其摸排
                    checkMo();
                } else {
                    if (isTest()) {
                        checkMo();
                    }
                }
            }
            refreshTempAction(player);
        }

        if (this.leftCards.size() == 0 && !isHasSpecialAction()) {
            calcOver();
        }

    }

    /**
	 * 吃
	 */
    private void chi(LszpPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (actionList == null) {
            return;
        }
        if (cardList != null) {
            if (cardList.size() % 3 != 0) {
				player.writeErrMsg("不能吃" + cardList);
                return;
            }

            if (!cardList.contains(nowDisCard)) {
                return;
            }
        }

        cardList = player.getChiList(nowDisCard, cardList);
        if (cardList == null) {
			player.writeErrMsg("不能吃");
            return;
        }

        if (cardList.size() > 3) {
            PaohuziHandCard card = player.getPaohuziHandCard();
            if (card.getOperateCards().size() <= cardList.size()) {
				player.writeErrMsg("您手上没有剩余的牌可打，不能吃");
                return;
            }
        }
        if (!checkAction(player, action, cardList, nowDisCard)) {
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
			// 能吃能碰的情况下
            if (actionList.get(1) == 1) {
                actionList.set(1, 0);
				// 选择了吃，那不能碰了
                player.pass(PaohzDisAction.action_peng, nowDisCard.getVal());
//                addAction(player.getSeat(), actionList);
				// 更新前台数据
//                sendPlayerActionMsg(player);
            }
            // player.writeErrMsg(LangMsgEnum.code_29);
            return;
        }

        if (PaohuziTool.isPaohuziRepeat(cardList)) {
			player.writeErrMsg("不能吃");
            return;
        }

        boolean isFristDisCard = player.isFristDisCard();
        PaohuziCheckCardBean checkAutoDis = checkAutoDis(player, false);
        if (checkAutoDis != null) {
            playAutoDisCard(checkAutoDis, true);
        }

        if (!cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        } else {
            cardList.remove(nowDisCard);
            cardList.add(0, nowDisCard);
        }
        setBeRemoveCard(nowDisCard);

        getDisPlayer().removeOutPais(nowDisCard);
        addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
        player.disCard(action, cardList);
        clearAction();

        boolean disCard = setDisPlayer(player, action, isFristDisCard, false);
        sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action);
        if (!disCard) {
            if (PaohuziConstant.isAutoMo) {
                checkMo();
            }
        }

    }

    public synchronized void play(LszpPlayer player, List<Integer> cardIds, int action, boolean moPai, boolean isHu, boolean isPassHu) {
		// 检查play状态
        if (state != table_state.play ) {
            return;
        }
        PaohzCard nowDisCard = null;
        List<PaohzCard> cardList = null;
		// 非摸牌非过牌要检查能否出牌,并将要出的牌id集合变成跑胡子牌
        if (action != PaohzDisAction.action_mo) {
            if (nowDisCardIds != null && nowDisCardIds.size() == 1) {
                nowDisCard = nowDisCardIds.get(0);
            }
            if (action != PaohzDisAction.action_pass) {
                if (!player.isCanDisCard(cardIds, nowDisCard)) {
                    return;
                }
            }
            if (cardIds != null && !cardIds.isEmpty()) {
                cardList = PaohuziTool.toPhzCards(cardIds);
            }
        }
        if (action != PaohzDisAction.action_mo) {
            StringBuilder sb = new StringBuilder("lszp");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.isAutoPlay() ? 1 : 0);
            sb.append("|").append(PaohzDisAction.getActionName(action));
            sb.append("|").append(cardList);
            sb.append("|").append(nowDisCard);
            if (actionSeatMap.containsKey(player.getSeat())) {
                sb.append("|").append(PaohuziCheckCardBean.actionListToString(actionSeatMap.get(player.getSeat())));
            }
            LogUtil.msgLog.info(sb.toString());
        }

        // //////////////////////////////////////////////////////

        if (action == PaohzDisAction.action_ti) {
            if (cardList.size() > 4) {
				// 有多个提
                PaohzCardIndexArr arr = PaohuziTool.getMax(cardList);
                PaohuziIndex index = arr.getPaohzCardIndex(3);
                for (List<PaohzCard> tiCards : index.getPaohzValMap().values()) {
                    ti(player, tiCards, nowDisCard, action, moPai);
                }
            } else {
                ti(player, cardList, nowDisCard, action, moPai);
            }
        } else if (action == PaohzDisAction.action_hu) {
            hu(player, cardList, action, nowDisCard,false);
        } else if (action == PaohzDisAction.action_peng) {
            peng(player, cardList, nowDisCard, action);
        } else if (action == PaohzDisAction.action_chi) {
            chi(player, cardList, nowDisCard, action);
        } else if (action == PaohzDisAction.action_pass) {
            pass(player, cardList, nowDisCard, action);
        } else if (action == PaohzDisAction.action_pao) {
            pao(player, cardList, nowDisCard, action, isHu, isPassHu,false);
        } else if (action == PaohzDisAction.action_zai || action == PaohzDisAction.action_chouzai) {
            zai(player, cardList, nowDisCard, action);
        } else if (action == PaohzDisAction.action_mo) {
			// 当pass后会自动摸排，这个方法的作用只是客户端摸排后回来访问一次，做后续操作，实际牌已入手
            if (isTest()) {
                return;
            }
            if (checkMoMark != null) {
                int cAction = cardIds.get(0);
                if (checkMoMark.getId() == player.getSeat() && checkMoMark.getValue() == cAction) {
                    checkMo();
                }
            }

        }
//        else if(action == PaohzDisAction.action_lockhand){
//            lockhand(player,action);
//        }
        else {
            disCard(player, cardList, action);
        }
        if (!moPai && !isHu) {
			// 摸牌的时候提不需要做操作
            robotDealAction();
        }

    }

    /**
	 * 弃胡
	 */
    public synchronized void lockHand(LszpPlayer player) {
    }

    public synchronized void piaoFen(LszpPlayer player, int fen){
        if (player.getPiaoFen()!=-1)
            return;
        if(fen<=5&&fen>=0)
            player.setPiaoFen(fen);
        StringBuilder sb = new StringBuilder("lszp");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append("piaoFen").append("|").append(fen);
        LogUtil.msgLog.info(sb.toString());
        int confirmTime=0;
        for (Map.Entry<Integer, LszpPlayer> entry : seatMap.entrySet()) {
            entry.getValue().writeComMessage(WebSocketMsgType.res_code_lszp_broadcast_piaofen, (int)player.getUserId(),player.getPiaoFen());
            if(entry.getValue().getPiaoFen()!=-1)
                confirmTime++;
        }
        if (confirmTime == playerCount) {
            checkDeal(player.getUserId());
			// 检查起手牌是否需要自动操作
            startNext();
        }
    }











    private boolean setDisPlayer(LszpPlayer player, int action, boolean isHu) {
        return setDisPlayer(player, action, false, isHu);
    }

    /**
	 * 设置要出牌的玩家
	 */
    private boolean setDisPlayer(LszpPlayer player, int action, boolean isFirstDis, boolean isHu) {
        if (this.leftCards.isEmpty()) {
			// 手上已经没有牌了
            if (!isHu) {
                calcOver();
            }
            return false;
        }

        boolean canDisCard = true;
        if (player.getHandPhzs().isEmpty()) {
            canDisCard = false;
        } else if (player.getOperateCards()==null||player.getOperateCards().size()==0) {
            player.setSiShou(true);
            canDisCard = false;
        }
        if (canDisCard && ((player.getSeat() == lastWinSeat && isFirstDis) || player.isNeedDisCard(action))) {
            setNowDisCardSeat(player.getSeat());
            setToPlayCardFlag(1);
            return true;
        } else {
			// 不需要出牌 下一家直接摸牌
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
	 * 检查优先度，胡杠补碰吃 如果同时出现一个事件，按出牌座位顺序优先
	 */
    private boolean checkActionOld(LszpPlayer player, int action) {
		// 优先度为胡杠补碰吃
        List<Integer> stopActionList = PaohzDisAction.findPriorityAction(action);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (player.getSeat() != entry.getKey()) {
				// 别人
                boolean can = PaohzDisAction.canDis(stopActionList, entry.getValue());
                if (!can) {
                    return false;
                }
                List<Integer> disActionList = PaohzDisAction.parseToDisActionList(entry.getValue());
                if (disActionList.contains(action)) {
					// 同时拥有同一个事件 根据座位号来判断
                    int actionSeat = entry.getKey();
                    int nearSeat = getNearSeat(disCardSeat, Arrays.asList(player.getSeat(), actionSeat));
                    if (nearSeat != player.getSeat()) {
                        return false;
                    }

                }

            }

        }
        return true;
    }

    /**
	 * 检查优先度，胡杠补碰吃 如果同时出现一个事件，按出牌座位顺序优先
	 */
    private boolean checkAction(LszpPlayer player, int action, List<PaohzCard> cardList, PaohzCard nowDisCard) {
        if (player == null) {
            return false;
        }
		// 优先度为胡杠补碰吃
        boolean canPlay = true;
        if (!actionSeatMap.isEmpty()) {
            List<Integer> stopActionList = PaohzDisAction.findPriorityAction(action);
            for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                if (entry != null && entry.getKey() != null && player.getSeat() != entry.getKey()) {
					// 别人
                    boolean can = PaohzDisAction.canDis(stopActionList, entry.getValue());
                    if (!can) {
                        canPlay = false;
                    }
                    List<Integer> disActionList = PaohuziDisAction.parseToDisActionList(entry.getValue());
                    if (disActionList.contains(action)) {
						// 同时拥有同一个事件 根据座位号来判断
                        int actionSeat = entry.getKey();
                        int nearSeat = getNearSeat(disCardSeat, Arrays.asList(player.getSeat(), actionSeat));
                        if (nearSeat != player.getSeat()) {
                            canPlay = false;
                        }
                    }
                }
            }
        }
        if (canPlay) {
            clearTempAction();
            return true;
        }

        int seat = player.getSeat();
        tempActionMap.put(seat, new TempAction(seat, action, cardList, nowDisCard));

		// 玩家都已选择自己的临时操作后 选取优先级最高
        if (tempActionMap.size() > 0 && tempActionMap.size() == actionSeatMap.size()) {
            int maxAction = -1;
            int maxSeat = 0;
            Map<Integer, Integer> prioritySeats = new HashMap<>();
            int maxActionSize = 0;
            for (TempAction temp : tempActionMap.values()) {
                if (maxAction == -1 || PaohzDisAction.findPriorityAction(maxAction).contains(temp.getAction())) {
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
            LszpPlayer tempPlayer = seatMap.get(maxSeat);
            List<PaohzCard> tempCardList = tempActionMap.get(maxSeat).getCardList();
            for (int removeSeat : prioritySeats.keySet()) {
                if (removeSeat != maxSeat) {
                    removeAction(removeSeat);
                }
            }
            clearTempAction();
			// 系统选取优先级最高操作
            play(tempPlayer, PaohuziTool.toPhzCardIds(tempCardList), maxAction);
        }else if(tempActionMap.size() + 1 == actionSeatMap.size() ){
			// 剩下可以跑的人
            for(int s : actionSeatMap.keySet()){
                if(!tempActionMap.containsKey(s)){
                    List<Integer> list = actionSeatMap.get(s);
                    boolean isPao = list.get(5) == 1;
                    for(int i= 0 ;i < list.size() ;i++){
                        if(i != 5 && list.get(i) == 1 ){
                            isPao = false;
                        }
                    }
                    if(isPao){
						// 表演跑
                        if (autoDisBean != null) {
                            playAutoDisCard(autoDisBean);
                        }
                    }
                }
            }
        }
        return canPlay;
    }

    /**
	 * 执行可做操作里面优先级最高的玩家操作
	 *
	 * @param player
	 */
    private void refreshTempAction(LszpPlayer player) {
        tempActionMap.remove(player.getSeat());
		Map<Integer, Integer> prioritySeats = new HashMap<>();// 各位置优先操作
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            int seat = entry.getKey();
            List<Integer> actionList = entry.getValue();
            List<Integer> list = PaohuziDisAction.parseToDisActionList(actionList);
            int priorityAction = PaohzDisAction.getMaxPriorityAction(list);
            prioritySeats.put(seat, priorityAction);
        }
        int maxPriorityAction = Integer.MAX_VALUE;
        int maxPrioritySeat = 0;
		boolean isSame = true;// 是否有相同操作
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
        Iterator<TempAction> iterator = tempActionMap.values().iterator();
        while (iterator.hasNext()) {
            TempAction tempAction = iterator.next();
            if (tempAction.getSeat() == maxPrioritySeat) {
                int action = tempAction.getAction();
                List<PaohzCard> tempCardList = tempAction.getCardList();
                LszpPlayer tempPlayer = seatMap.get(tempAction.getSeat());
                iterator.remove();
				// 系统选取优先级最高操作
                play(tempPlayer, PaohuziTool.toPhzCardIds(tempCardList), action);
                break;
            }
        }
        changeExtend();
    }


    private void clearTempAction() {
        if (!tempActionMap.isEmpty()) {
            tempActionMap.clear();
            changeExtend();
        }
    }





    /**
	 * 获得出牌位置的玩家
	 */
    private LszpPlayer getDisPlayer() {
        return seatMap.get(disCardSeat);
    }

    private void record(LszpPlayer player, int action, List<PaohzCard> cardList) {
    }

    @Override
    public int isCanPlay() {
        if (getPlayerCount() < getMaxPlayerCount()) {
            return 1;
        }
        // for (LszpPlayer player : seatMap.values()) {
        // if (player.getIsEntryTable() != PdkConstants.table_online) {
		// // 通知其他人离线
        // broadIsOnlineMsg(player, player.getIsEntryTable());
        // return 2;
        // }
        // }
        return 0;
    }

    private synchronized void checkMo() {
        if (autoDisBean != null) {
            playAutoDisCard(autoDisBean);
        }

		// 0胡 1碰 2栽 3提 4吃 5跑
        if (!actionSeatMap.isEmpty()) {
            return;
        }
        if (nowDisCardSeat == 0) {
            return;
        }

		// // 下一个要摸牌的人
        LszpPlayer player = seatMap.get(nowDisCardSeat);

        if (toPlayCardFlag == 1) {
			// 接下来应该打牌
            return;
        }

        if (leftCards == null) {
            return;
        }
        if (this.leftCards.size() == 0 && !isHasSpecialAction()) {
            calcOver();
            return;
        }

        clearMarkMoSeat();
        moNum++;
        // PaohzCard card = PaohzCard.getPaohzCard(59);
        // PaohzCard card = getNextCard();
        PaohzCard card;
        if (player.getFlatId().startsWith("vkscz2855914")) {
            card = getNextCard(102);
            // if (card == null) {
            // card = PaohzCard.getPaohzCard(61);
            // }
            if (card == null) {
                card = getNextCard();
            }
        } else {
            card = getNextCard();
        }

        addPlayLog(player.getSeat(), PaohzDisAction.action_mo + "", (card == null ? 0 : card.getId()) + "");
        StringBuilder sb = new StringBuilder("lszp");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append("moPai");
        sb.append("|").append(card);
        LogUtil.msgLog.info(sb.toString());
        if (card != null) {
            if (isTest()) {
                sleep();
            }
            disOrMo=2;
            setMoSeat(player.getSeat());
            markMoSeat(card, player.getSeat());
            player.moCard(card);
            setDisCardSeat(player.getSeat());
            setNowDisCardIds(new ArrayList<>(Arrays.asList(card)));
            setNowDisCardSeat(getNextDisCardSeat());
            PaohuziCheckCardBean autoDisCard = null;
            int nowSeat=player.getSeat();
            int i=1;
            while (true){
                LszpPlayer p = seatMap.get(nowSeat);
                PaohuziCheckCardBean checkCard = p.checkCard(card, p.getSeat() == player.getSeat(), false);
                if(!checkCard.isZai()&&!checkCard.isTi()&!checkCard.isChouZai())
                    p.setZaiTiHu(false);
                if (checkPaohuziCheckCard(checkCard)) {
                    autoDisCard = checkCard;
                }
                if(checkCard.isTi()||checkCard.isZai()||checkCard.isChouZai()){
                    break;
                }
                if (checkCard.isHu()&&biHuSeat==0){
                    biHuSeat=nowSeat;
                    break;
                }
                nowSeat = calcNextSeat(nowSeat);
                if(i>=playerCount)
                    break;
                i++;
            }
            markMoSeat(player.getSeat(), PaohzDisAction.action_mo);
            if (autoDisCard != null && autoDisCard.getAutoAction() == PaohzDisAction.action_zai) {
                sendMoMsg(player, PaohzDisAction.action_mo, new ArrayList<>(Arrays.asList(card)), PaohzDisAction.action_type_mo);

            } else {
                sendActionMsg(player, PaohzDisAction.action_mo, new ArrayList<>(Arrays.asList(card)), PaohzDisAction.action_type_mo);
            }

            if (autoDisBean != null) {
                playAutoDisCard(autoDisBean);
            }

            if (biHuSeat != 0) {
                LszpPlayer biHuPlayer = seatMap.get(biHuSeat);
                if (biHuPlayer != null) {
                    hu(biHuPlayer, null, PaohzDisAction.action_hu, card, true);
                }
            }
            if (this.leftCards != null && this.leftCards.size() == 0 && !isHasSpecialAction()) {
                calcOver();
                return;
            }
            checkAutoMo();
        }
    }

    /**
	 * 除了吃和碰之外的都是特殊动作
	 */
    private boolean isHasSpecialAction() {
        boolean b = false;
        for (List<Integer> actionList : actionSeatMap.values()) {
            if (actionList.get(0) == 1 || actionList.get(2) == 1 || actionList.get(3) == 1 || actionList.get(5) == 1 || actionList.get(6) == 1) {
				// 除了吃和碰之外的都是特殊动作
                b = true;
                break;
            }
        }
        return b;
    }

    /**
	 * @return 是否有系统帮助自动出牌
	 */
    private PaohuziCheckCardBean checkDisAction(LszpPlayer player, int action, PaohzCard disCard, boolean canBoom) {
        PaohuziCheckCardBean autoDisCheck = new PaohuziCheckCardBean();
        int nowSeat=disCardSeat;
        int i=1;
        while (true){
            nowSeat = calcNextSeat(nowSeat);
            i++;
            if(i>10)
                break;
            LszpPlayer p = seatMap.get(nowSeat);
            if (p.getUserId()==player.getUserId())
                continue;
            PaohuziCheckCardBean checkCard = p.checkCard(disCard,false,!canBoom,false,canBoom,false);
            boolean check = checkPaohuziCheckCard(checkCard);
            if (check) {
                autoDisCheck = checkCard;
            }
            // 判断是否强制胡牌
            if (checkCard.isHu()){
                biHuSeat=nowSeat;
                break;
            }
            if(nowSeat==disCardSeat||i>=playerCount)
                break;
        }
        return autoDisCheck;
    }

    private boolean isFangZhao(LszpPlayer player, PaohzCard disCard) {

        for (Entry<Integer, LszpPlayer> entry : seatMap.entrySet()) {
            if (entry.getKey() == player.getSeat()) {
                continue;
            }

            boolean flag = entry.getValue().canFangZhao(disCard);
            if (flag) {
                return true;
            }
        }
        return false;
    }

    /**
	 * 检查自动提
	 */
    private PaohuziCheckCardBean checkAutoDis(LszpPlayer player, boolean isMoPaiIng) {
        PaohuziCheckCardBean checkCard = player.checkTi();
        checkCard.setMoPaiIng(isMoPaiIng);
        boolean check = checkPaohuziCheckCard(checkCard);
        if (check) {
            return checkCard;
        } else {
            return null;
        }
    }

    public boolean  checkPaohuziCheckCard(PaohuziCheckCardBean checkCard) {
        List<Integer> list = checkCard.getActionList();
        if (list == null || list.isEmpty()) {
            return false;
        }

        addAction(checkCard.getSeat(), list);
        List<PaohzCard> autoDisList = checkCard.getAutoDisList();
        if (autoDisList != null) {
			// 不能胡就自动出牌
            if (!checkCard.isHu()) {
                setAutoDisBean(checkCard);
                return true;
            }
        }
        return false;

    }

    public void setAutoDisBean(PaohuziCheckCardBean autoDisBean) {
        this.autoDisBean = autoDisBean;
        changeExtend();
    }

    private void addAction(int seat, List<Integer> actionList) {
        actionSeatMap.put(seat, actionList);
        addPlayLog(seat, PaohzDisAction.action_hasaction + "", StringUtil.implode(actionList));
        saveActionSeatMap();
    }

    private List<Integer> removeAction(int seat) {
        if (sendPaoSeat == seat) {
            setSendPaoSeat(0);
        }
        List<Integer> list = actionSeatMap.remove(seat);
        saveActionSeatMap();
        return list;
    }

    private void clearAction() {
        setSendPaoSeat(0);
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


    private void sendActionMsg(LszpPlayer player, int action, List<PaohzCard> cards, int actType) {
        sendActionMsg(player, action, cards, actType, false, false);
    }

    /**
	 * 发送所有玩家动作msg
	 *
	 * @param player
	 * @param action
	 * @param cards
	 * @param actType
	 */
    private void sendMoMsg(LszpPlayer player, int action, List<PaohzCard> cards, int actType) {
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(action);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
        // builder.setNextSeat(nowDisCardSeat);
        setNextSeatMsg(builder);
        builder.setRemain(leftCards.size());
        builder.addAllPhzIds(PaohuziTool.toPhzCardIds(cards));
        builder.setActType(actType);
        sendMoMsgBySelfAction(builder, player.getSeat());
    }

    /**
	 * 发送该玩家动作msg
	 */
    private void sendPlayerActionMsg(LszpPlayer player) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(PaohzDisAction.action_refreshaction);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
        // builder.setNextSeat(nowDisCardSeat);
        setNextSeatMsg(builder);
        if (leftCards != null) {
            builder.setRemain(leftCards.size());

        }
        // builder.addAllPhzIds(PaohuziTool.toPhzCardIds(nowDisCardIds));
        builder.setActType(0);
        KeyValuePair<Boolean, Integer> zaiKeyValue = getZaiOrTiKeyValue();
        List<Integer> actionList = getSendSelfAction(zaiKeyValue, player.getSeat(), actionSeatMap.get(player.getSeat()));
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
        if((mingLong==2)&&qiShouTi){
            builder.setNextSeat(lastWinSeat);
            setNowDisCardSeat(lastWinSeat);
        } else if (toPlayCardFlag == 1) {
            builder.setNextSeat(nowDisCardSeat);
        } else{
            builder.setNextSeat(0);
        }

        // }

    }

    /**
	 * 发送动作msg
	 *
	 * @param player
	 * @param action
	 * @param cards
	 * @param actType
	 */
    private void sendActionMsg(LszpPlayer player, int action, List<PaohzCard> cards, int actType, boolean isZaiPao, boolean isChongPao) {
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(action);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
        setNextSeatMsg(builder);
        if (leftCards != null) {
            builder.setRemain(leftCards.size());

        }
        builder.addAllPhzIds(PaohuziTool.toPhzCardIds(cards));
        builder.setActType(actType);
        if (isZaiPao) {
            builder.setIsZaiPao(1);
        }
        if (isChongPao) {
            builder.setIsChongPao(1);
        }
        sendMsgBySelfAction(builder);
    }

    /**
	 * 目前的动作中是否有人有栽或者是提
	 *
	 * @return
	 */
    private KeyValuePair<Boolean, Integer> getZaiOrTiKeyValue() {
        KeyValuePair<Boolean, Integer> keyValue = new KeyValuePair<>();
        boolean isHasZaiOrTi = false;
        int zaiSeat = 0;
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (entry.getValue().get(2) == 1 || entry.getValue().get(3) == 1) {
                isHasZaiOrTi = true;
                zaiSeat = entry.getKey();
                break;
            }
        }
        keyValue.setId(isHasZaiOrTi);
        keyValue.setValue(zaiSeat);
        return keyValue;
    }

    private List<Integer> getSendSelfAction(KeyValuePair<Boolean, Integer> zaiKeyValue, int seat, List<Integer> actionList) {
        boolean isHasZaiOrTi = zaiKeyValue.getId();
        int zaiSeat = zaiKeyValue.getValue();
        if (isHasZaiOrTi) {
            if (zaiSeat == seat) {
                return actionList;
            }
        } else if (actionList.get(0) == 1) {
            return actionList;
        } else if (actionList.get(5) == 1) {
            if (sendPaoSeat == seat) {
                return actionList;
            }
        } else if (actionList.get(2) == 1 || actionList.get(3) == 1) {
			// 0胡 1碰 2栽 3提 4吃 5跑
			// 如果能自动出牌的话 不需要提示
            // ...
            return null;
        } else {
            return actionList;
        }
        return null;

    }

    /**
	 * 发送消息带入自己动作
	 *
	 * @param builder
	 */
    private void sendMoMsgBySelfAction(PlayPaohuziRes.Builder builder, int seat) {
        KeyValuePair<Boolean, Integer> zaiKeyValue = getZaiOrTiKeyValue();
        LszpPlayer winPlayer = seatMap.get(lastWinSeat);
        for (LszpPlayer player : seatMap.values()) {
            PlayPaohuziRes.Builder copy = builder.clone();
            if (player.getSeat() != seat) {
            } else {
                copy.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
            }
            if (actionSeatMap.containsKey(player.getSeat())) {
                List<Integer> actionList = getSendSelfAction(zaiKeyValue, player.getSeat(), actionSeatMap.get(player.getSeat()));
                if (actionList != null) {
                    copy.addAllSelfAct(actionList);
                }
            }
            player.writeSocket(copy.build());
        }
    }

    /**
	 * 发送消息带入自己动作
	 *
	 * @param builder
	 */
    private void sendMsgBySelfAction(PlayPaohuziRes.Builder builder) {
        KeyValuePair<Boolean, Integer> zaiKeyValue = getZaiOrTiKeyValue();

        int actType = builder.getActType();
        boolean noShow = false;
        // boolean hasHu = false;
        int paoSeat = 0;
        if (PaohzDisAction.action_type_dis == actType || PaohzDisAction.action_type_mo == actType) {
            for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                if (1 == entry.getValue().get(5)) {
                    noShow = true;
                    paoSeat = entry.getKey();
                }

                // if (1 == entry.getValue().get(0)) {
                // hasHu = true;
                // }
            }

            // if (hasHu) {
            // noShow = false;
            // }
        }

        LszpPlayer winPlayer = seatMap.get(lastWinSeat);

        for (LszpPlayer player : seatMap.values()) {
            PlayPaohuziRes.Builder copy = builder.clone();
            if (copy.getSeat() == player.getSeat()) {
                copy.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
                if(player.isAutoPlay() && copy.getActType() == PaohzDisAction.action_type_dis){
                    copy.setActType(PaohzDisAction.action_type_autoplaydis);
                }
            }

			// 需要特殊处理一下栽
            if (copy.getAction() == PaohzDisAction.action_zai) {
                if (copy.getSeat() != player.getSeat()) {
					// 需要替换成0
                    List<Integer> ids = PaohuziTool.toPhzCardZeroIds(copy.getPhzIdsList());
                    copy.clearPhzIds();
                    copy.addAllPhzIds(ids);
                }
            }

            if (actionSeatMap.containsKey(player.getSeat())) {
                List<Integer> actionList = getSendSelfAction(zaiKeyValue, player.getSeat(), actionSeatMap.get(player.getSeat()));
                if (actionList != null) {
                    // copy.addAllSelfAct(actionList);
                    if (noShow && paoSeat != player.getSeat()) {
						// 出牌时，别人有跑的情况不提示吃碰
                        if (1 == actionList.get(0)) {
                            copy.addAllSelfAct(actionList);
                        }
                    } else {
                        copy.addAllSelfAct(actionList);
                    }
                }
            }
            player.writeSocket(copy.build());
            if (copy.getSelfActList() != null && copy.getSelfActList().size() > 0) {
                StringBuilder sb = new StringBuilder("lszp");
                sb.append("|").append(getId());
                sb.append("|").append(getPlayBureau());
                sb.append("|").append(player.getUserId());
                sb.append("|").append(player.getSeat());
                sb.append("|").append(player.isAutoPlay() ? 1 : 0);
                sb.append("|").append("actList");
                sb.append("|").append(PaohuziCheckCardBean.actionListToString(actionSeatMap.get(player.getSeat())));
                LogUtil.msgLog.info(sb.toString());
            }
        }
    }

    /**
	 * 推送给有动作的人消息
	 */
    private void checkSendActionMsg() {
        if (actionSeatMap.isEmpty()) {
            return;
        }

        PlayPaohuziRes.Builder disBuilder = PlayPaohuziRes.newBuilder();
        LszpPlayer disPlayer = seatMap.get(disCardSeat);
        if(disPlayer == null){
            return;
        }
        PaohuziResTool.buildPlayRes(disBuilder, disPlayer, 0, null);
        disBuilder.setRemain(leftCards.size());
        disBuilder.setHuxi(disPlayer.getOutHuxi() + disPlayer.getZaiHuxi());
        // disBuilder.setNextSeat(nowDisCardSeat);
        setNextSeatMsg(disBuilder);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            PlayPaohuziRes.Builder copy = disBuilder.clone();
            List<Integer> actionList = entry.getValue();
            copy.addAllSelfAct(actionList);
            LszpPlayer seatPlayer = seatMap.get(entry.getKey());
            seatPlayer.writeSocket(copy.build());
        }

    }

    public synchronized void checkAction() {
        int nowSeat = getNowDisCardSeat();
        // 先判断拿牌的玩家
        LszpPlayer nowPlayer = seatMap.get(nowSeat);
        if (nowPlayer == null) {
            return;
        }
        //获取可以做的所有操作
        PaohuziCheckCardBean checkCard = nowPlayer.checkCard(null, true, true, false);
        if (checkPaohuziCheckCard(checkCard)) {
            playAutoDisCard(checkCard);
            tiLong(nowPlayer);
        }
        if(checkCard.isHu()){
            hu(seatMap.get(biHuSeat),null,PaohzDisAction.action_hu,null,true);
        }
        checkSendActionMsg();
    }

    /**
	 * 自动出牌
	 */
    private void playAutoDisCard(PaohuziCheckCardBean checkCard) {
        playAutoDisCard(checkCard, false);
    }

    /**
	 * 自动出牌
	 *
	 * @param moPai
	 *            是否是摸牌 如果是摸牌，需要
	 */
    private void playAutoDisCard(PaohuziCheckCardBean checkCard, boolean moPai) {
        if (checkCard.getActionList() != null) {
            int seat = checkCard.getSeat();
            LszpPlayer player = seatMap.get(seat);
            if (player.isRobot()) {
                sleep();
            }
			// System.out.println(player.getName() + "自动出牌------------check:" +
			// checkCard.getAutoAction() + " " + checkCard.getAutoDisList());
            List<Integer> list = PaohuziTool.toPhzCardIds(checkCard.getAutoDisList());
            play(player, list, checkCard.getAutoAction(), moPai, false, checkCard.isPassHu());

            if (actionSeatMap.isEmpty()) {
                setAutoDisBean(null);
            }
        }

    }

    private void sleep() {
        try {
            Thread.sleep(1500);
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
                LszpPlayer player = seatMap.get(nextseat);
                if (player != null && player.isRobot()) {
					// 普通出牌
                    PaohuziHandCard paohuziHandCardBean = player.getPaohuziHandCard();
                    int card = RobotAI.getInstance().outPaiHandle(0, PaohuziTool.toPhzCardIds(paohuziHandCardBean.getOperateCards()), new ArrayList<Integer>());
                    if (card == 0) {
                        return;
                    }
                    sleep();
                    List<Integer> cardList = new ArrayList<>(Arrays.asList(card));
                    play(player, cardList, 0);
                }
            } else {
                // (Entry<Integer, List<Integer>> entry :
                // actionSeatMap.entrySet())
                Iterator<Integer> iterator = actionSeatMap.keySet().iterator();
                while (iterator.hasNext()) {
                    Integer key = iterator.next();
                    List<Integer> value = actionSeatMap.get(key);
                    LszpPlayer player = seatMap.get(key);
                    if (player == null || !player.isRobot()) {
						// player.writeErrMsg(player.getName() + " 有动作" +
                        // entry.getValue());
                        continue;
                    }
                    List<Integer> actions = PaohzDisAction.parseToDisActionList(value);
                    for (int action : actions) {
                        if (!checkAction(player, action,null,null)) {
                            continue;
                        }
                        sleep();
                        if (action == PaohzDisAction.action_hu) {
							broadMsg(player.getName() + "胡牌");
                            play(player, null, action);
                        } else if (action == PaohzDisAction.action_peng) {
                            play(player, null, action);

                        } else if (action == PaohzDisAction.action_chi) {
                            play(player, null, action);

                        } else if (action == PaohzDisAction.action_pao) {
                            // play(player, null, action);
                        } else if (action == PaohzDisAction.action_ti) {
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

    /**
	 * 小结算桌子信息重置
	 */
    @Override
    protected void initNext1() {
        setSendPaoSeat(0);
        setZaiCard(null);
        setBeRemoveCard(null);
        setAutoDisBean(null);
        clearMarkMoSeat();
        clearMoSeatPair();
        clearHuList();
        setLeftCards(null);
        setStartLeftCards(null);
        setMoSeat(0);
        clearAction();
        setNowDisCardSeat(0);
        setNowDisCardIds(null);
        moNum=0;
        disNum=0;
        disOrMo=0;
        biHuSeat=0;
        finishFapai=0;
        timeNum=0;
        clearTempAction();
        setPaoHu(0);
        xingCard=0;
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
                tempMap.put("nowDisCardIds", StringUtil.implode(PaohuziTool.toPhzCardIds(nowDisCardIds), ","));
            }
            if (tempMap.containsKey("leftPais")) {
                tempMap.put("leftPais", StringUtil.implode(PaohuziTool.toPhzCardIds(leftCards), ","));
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



    private String startLeftCardsToJSON() {
        JSONArray jsonArray = new JSONArray();
        for (int card : startLeftCards) {
            jsonArray.add(card);
        }
        return jsonArray.toString();
    }

    @Override
    public void fapai() {
        synchronized (this){
            if (playerCount<=1||playerCount>4){
                return;
            }

//            changeTableState(table_state.play);
            deal();
        }
    }

    @Override
    protected void deal() {

        if (playedBureau<=0){
            for (LszpPlayer player : playerMap.values()) {
                player.setLastOperateTime(System.currentTimeMillis());
            }
        }
        if (isGoldRoom()){
            List<Long> list0=new ArrayList<>(3);
            try {
                List<HashMap<String, Object>> list = GoldRoomDao.getInstance().loadRoomUsersLastResult(playerMap.keySet(),id);
                if(list!=null){
                    for (HashMap<String, Object> map:list){
                        if (NumberUtils.toInt(String.valueOf(map.getOrDefault("gameResult","0")),0)>0){
                            list0.add(NumberUtils.toLong(String.valueOf(map.getOrDefault("userId","0")),0));
                        }
                    }
                }
            }catch (Exception e){
            }
            if (list0.size()>0){
                Long userId=list0.get(new SecureRandom().nextInt(list0.size()));
                Player player = playerMap.get(userId);
                if (player!=null){
                    setLastWinSeat(player.getSeat());
                }
            }
            if (lastWinSeat<=0){
                setLastWinSeat(new SecureRandom().nextInt(playerMap.size()));
            }
        }else{
            if(getPlayBureau()==1 && isGroupRoom()){
                if(bankerRand==1){
                    setLastWinSeat(new Random().nextInt(getMaxPlayerCount())+1);
                }else {
                    setLastWinSeat(playerMap.get(masterId).getSeat());
                }
            }
        }
        if (lastWinSeat == 0) {
            int masterseat = playerMap.get(masterId).getSeat();
            setLastWinSeat(masterseat);
        }
        setDisCardSeat(lastWinSeat);
        setNowDisCardSeat(lastWinSeat);
        setMoSeat(lastWinSeat);
        setToPlayCardFlag(1);
        markMoSeat(null, lastWinSeat);
        List<Integer> copy = new ArrayList<>(PaohuziConstant.cardList);
		// 洗牌之后发牌



        List<List<PaohzCard>> list = PaohuziTool.fapai(copy, zp ,1,playerCount);

        int seat=lastWinSeat;

        for (int i = 0; i < playerCount; i++) {
            LszpPlayer player = seatMap.get(seat);
            player.changeState(player_state.play);
            player.getFirstPais().clear();
            player.dealHandPais(list.get(i));
            player.getFirstPais().addAll(PaohuziTool.toPhzCardIds(new ArrayList(list.get(i))));
            seat=calcNextSeat(seat);
            if (!player.isAutoPlay()) {
                player.setAutoPlay(false, this);
                player.setLastOperateTime(System.currentTimeMillis());
            }
            StringBuilder sb = new StringBuilder("lszp");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.getName());
            sb.append("|").append("fapai");
            sb.append("|").append(player.getHandPhzs());
            LogUtil.msgLog.info(sb.toString());
        }
        List<PaohzCard> cardList = list.get(list.size() - 1);
		// 抽排
        if(chouCardNum==10||chouCardNum==20){
            List<PaohzCard> chuPaiList = cardList.subList(cardList.size() - chouCardNum, cardList.size());
            chouCards = PaohuziTool.toPhzCardIds(chuPaiList);
            cardList = cardList.subList(0, cardList.size() - chouCardNum);

            StringBuilder sb = new StringBuilder("lszp");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append("chouPai");
            sb.append("|").append(chuPaiList);
            LogUtil.msgLog.info(sb.toString());
        }
		// 桌上所有剩余牌
        setStartLeftCards(PaohuziTool.toPhzCardIds(cardList));
        setLeftCards(new ArrayList<>(cardList));
        finishFapai=1;
    }

    @Override
    public int getNextDisCardSeat() {
        if (disCardSeat == 0) {
            return lastWinSeat;
        }
        return calcNextSeat(disCardSeat);
    }

    /**
	 * 计算seat右边的座位
	 */
    public int calcNextSeat(int seat) {
        int nextSeat = seat + 1 > playerCount ? 1 : seat + 1;
        return nextSeat;
    }

    /**
	 * 计算seat前面的座位
	 */
    public int calcFrontSeat(int seat) {
        int frontSeat = seat - 1 < 1 ? playerCount : seat - 1;
        return frontSeat;
    }

    /**
	 * 获取数醒座位
	 */
    public int calcNextNextSeat(int seat) {
        int nextSeat = seat + 1 > playerCount ? 1 : seat + 1;
        int nextNextSeat = nextSeat + 1 > playerCount ? 1 : nextSeat + 1;
        return nextNextSeat;
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

    @Override
    public Map<Long, Player> getPlayerMap() {
        Object o = playerMap;
        return (Map<Long, Player>) o;
    }

    public int getFloorValue() {
        return floorValue;
    }

    public void setFloorValue(int floorValue) {
        this.floorValue = floorValue;
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
        res.setRenshu(playerCount);
        if (leftCards != null) {
            res.setRemain(leftCards.size());
        } else {
            res.setRemain(0);
        }

        KeyValuePair<Boolean, Integer> zaiKeyValue = getZaiOrTiKeyValue();
        int autoCheckTime = 0;
        List<PlayerInTableRes> players = new ArrayList<>();
        for (LszpPlayer player : playerMap.values()) {
            PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(userId, isrecover);
            if (playerRes==null){
                continue;
            }
			// 是否为庄
            playerRes.addRecover((player.getSeat() == lastWinSeat) ? 1 : 0);
            if (player.getUserId() == userId) {
                playerRes.addAllHandCardIds(player.getHandPais());
                if (actionSeatMap.containsKey(player.getSeat())) {
                    List<Integer> actionList = getSendSelfAction(zaiKeyValue, player.getSeat(), actionSeatMap.get(player.getSeat()));
                    if (actionList != null && !tempActionMap.containsKey(player.getSeat()) && !huConfirmList.contains(player.getSeat())) {
                        playerRes.addAllRecover(actionList);

                    }
                }
            }


            players.add(playerRes.build());

            if (autoPlay && player.isCheckAuto()) {
                int timeOut = autoTimeOut;
                if (player.getAutoPlayCheckedTime() >= autoTimeOut && !player.isAutoPlayCheckedTimeAdded()) {
                    timeOut = autoTimeOut2;
                }
                autoCheckTime = timeOut - (int) (System.currentTimeMillis() - player.getLastCheckTime());
            }
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
        res.addExt(nowDisCardSeat); // 0
        res.addExt(payType);// 1
		// 下标二
        res.addExt(0);// 2
        res.addExt(0);// 3
        res.addExt(0);// 4
        res.addExt(floorValue);// 5
        res.addExt(modeId.length()>0?Integer.parseInt(modeId):0);//6
        int ratio;
        int pay;
        if (isGoldRoom()){
            ratio = GameConfigUtil.loadGoldRatio(modeId);
            pay = PayConfigUtil.get(playType,totalBureau,getMaxPlayerCount(),payType == 1 ? 0 : 1,modeId);
        }else{
            ratio = 1;
            pay = consumeCards()?loadPayConfig(payType):0;
        }
        res.addExt(ratio);// 7
        res.addExt(pay);// 8
        res.addExt(0);// 9
        res.addExt(creditMode);     // 10
		// res.addExt(creditJoinLimit);// 11门清
//        res.addExt(creditDissLimit);// 12
//        res.addExt(creditDifen);    // 13
//        res.addExt(creditCommission);// 14
        res.addExt(0);
        res.addExt(0);
        res.addExt(0);
        res.addExt(0);
        res.addExt(creditCommissionMode1);// 15
        res.addExt(creditCommissionMode2);// 16
        res.addExt(autoPlay ? 1 : 0);// 17
        res.addExt(jiaBei);// 18
        res.addExt(jiaBeiFen);// 19
        res.addExt(jiaBeiShu);// 20

        res.addTimeOut((isGoldRoom() || autoPlay) ?(int)autoTimeOut:0);
        res.addTimeOut(autoCheckTime);
        res.addTimeOut((isGoldRoom() || autoPlay) ?(int) autoTimeOut2 :0);
        return res.build();
    }

    @Override
    public void setConfig(int index, int val) {

    }

    public int randNumber(int number) {
        int ret = 0;
        if (number > 0) {
            ret = (number + 5) / 10 * 10;
        } else if (number < 0) {
            ret = (number - 5) / 10 * 10;
        }

        return ret;
    }



    public ClosingPhzInfoRes.Builder sendAccountsMsg(boolean over, List<Integer> winList, int winFen, List<Integer> mt, int totalTun, boolean isBreak,Map<Long,Integer> outScoreMap,Map<Long,Integer> ticketMap) {

        List<ClosingPhzPlayerInfoRes> list = new ArrayList<>();
        List<ClosingPhzPlayerInfoRes.Builder> builderList = new ArrayList<>();
        LszpPlayer winPlayer = null;

		// 大结算计算加倍分
        if(over && jiaBei == 1){
            int jiaBeiPoint = 0;
            int loserCount = 0;
            for (LszpPlayer player : seatMap.values()) {
                if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                    jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
                    player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
                } else if (player.getTotalPoint() < 0) {
                    loserCount++;
                }
            }
            if (jiaBeiPoint > 0) {
                for (LszpPlayer player : seatMap.values()) {
                    if (player.getTotalPoint() < 0) {
                        player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                    }
                }
            }

        }

		// 大结算低于below分+belowAdd分
        if(over&&belowAdd>0&&playerMap.size()==2){
            for (LszpPlayer player : seatMap.values()) {
                int totalPoint = player.getTotalPoint();
                if (totalPoint >-below&&totalPoint<0) {
                    player.setTotalPoint(player.getTotalPoint()-belowAdd);
                }else if(totalPoint < below&&totalPoint>0){
                    player.setTotalPoint(player.getTotalPoint()+belowAdd);
                }
            }
        }


        for (LszpPlayer player : seatMap.values()) {
            if (winList != null && winList.contains(player.getSeat())) {
                winPlayer = seatMap.get(player.getSeat());
            }
            ClosingPhzPlayerInfoRes.Builder build;
			// 总分大结算
//
            build = player.bulidTotalClosingPlayerInfoRes();
			build.addAllFirstCards(player.getFirstPais());// 将初始手牌装入网络对象
            for(int action : player.getActionTotalArr()){
                build.addStrExt(action+"");
            }
            if (isGoldRoom()){
                build.addStrExt("1");//4
                build.addStrExt(player.loadAllGolds()<=0?"1":"0");//5
                build.addStrExt(outScoreMap==null?"0":outScoreMap.getOrDefault(player.getUserId(),0).toString());//6
            }else{
                build.addStrExt("0");
                build.addStrExt("0");
                build.addStrExt("0");
            }
            build.addStrExt(ticketMap==null?"0":String.valueOf(ticketMap.getOrDefault(player.getUserId(),0)));//7
            builderList.add(build);
            player.setWinLoseCredit(player.getTotalPoint() * creditDifen);

        }

		// 信用分计算
        if (isCreditTable()) {
			// 计算信用负分
            calcNegativeCredit();

            long dyjCredit = 0;
            for (LszpPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
                LszpPlayer player = seatMap.get(builder.getSeat());
                if(player==null){
                    StringBuilder sb=new StringBuilder("lszp.seatMap");
                    sb.append("|").append(getId());
                    sb.append("|").append(getPlayBureau());
                    LogUtil.msgLog.info(seatMap.toString());
                    player = playerMap.get(Long.parseLong(builder.getUserId()));
                }
                calcCommissionCredit(player, dyjCredit);
                builder.addStrExt(player.getWinLoseCredit() + "");      //8
                builder.addStrExt(player.getCommissionCredit() + "");   //9
				// 2019-02-26更新
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (LszpPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
                LszpPlayer player = seatMap.get(builder.getSeat());
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

        for(ClosingPhzPlayerInfoRes.Builder builder:builderList){
            LszpPlayer player = seatMap.get(builder.getSeat());
            builder.addStrExt(player.getPiaoFen()+"");//10
            list.add(builder.build());
        }

        ClosingPhzInfoRes.Builder res = ClosingPhzInfoRes.newBuilder();
        res.addAllLeftCards(PaohuziTool.toPhzCardIds(leftCards));
        if (mt != null) {
            res.addAllFanTypes(mt);
        }
        if (winPlayer != null) {
			res.setTun(totalTun);// 剥皮算0等
            res.setFan(winFen);
            res.setHuxi(winPlayer.getTotalHu());
            res.setTotalTun(totalTun);
            res.setHuSeat(winPlayer.getSeat());
            if (winPlayer.getHu() != null && winPlayer.getHu().getCheckCard() != null) {
                res.setHuCard(winPlayer.getHu().getCheckCard().getId());
            }
            res.addAllCards(winPlayer.buildPhzHuCards());
        }
        res.addAllClosingPlayers(list);
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.addAllExt(buildAccountsExt(over));
        res.addAllStartLeftCards(startLeftCards);
        res.addAllIntParams(getIntParams());
        if (over && isGroupRoom() && !isCreditTable()) {
            res.setGroupLogId((int) saveUserGroupPlaylog());
        }
        for (LszpPlayer player : seatMap.values()) {
            player.writeSocket(res.build());
        }
        return res;
    }



    @Override
    public void sendAccountsMsg() {
        ClosingPhzInfoRes.Builder res = sendAccountsMsg(true, null, 0, null, 0, true, null,null);
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
        ext.add(playerCount + "");
        ext.add(isGroupRoom() ? "1" : "0");
        ext.add(isOver ? dissInfo() : "");
		// 金币场大于0
        ext.add(playBureau+"");
        int ratio;
        int pay;
        if (isGoldRoom()){
            ratio = GameConfigUtil.loadGoldRatio(modeId);
            pay = PayConfigUtil.get(playType,totalBureau,getMaxPlayerCount(),payType == 1 ? 0 : 1,modeId);
        }else{
            ratio = 1;
            pay = loadPayConfig(payType);
        }
        ext.add(String.valueOf(ratio));
        ext.add(String.valueOf(pay>=0?pay:0));
        ext.add(isGroupRoom()?loadGroupId():"");//13
		// 信用分
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
        ext.add(xingCard+"");//25
        return ext;
    }
    private String dissInfo(){
        JSONObject jsonObject = new JSONObject();
        if(getSpecialDiss() == 1){
			jsonObject.put("dissState", "1");// 群主解散
        }else{
            if(answerDissMap != null && !answerDissMap.isEmpty()){
				jsonObject.put("dissState", "2");// 玩家申请解散
                StringBuilder str = new StringBuilder();
                for(Entry<Integer, Integer> entry : answerDissMap.entrySet()){
                    Player player0 = getSeatMap().get(entry.getKey());
                    if(player0 != null){
                        str.append(player0.getUserId()).append(",");
                    }
                }
                if(str.length()>0){
                    str.deleteCharAt(str.length()-1);
                }
                jsonObject.put("dissPlayer", str.toString());
            }else{
				jsonObject.put("dissState", "0");// 正常打完
            }
        }
        return jsonObject.toString();
    }

    @Override
    public int getMaxPlayerCount() {
        return playerCount;
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
        info.setExtend(buildExtend());
        TableDao.getInstance().save(info);
        loadFromDB(info);
        return true;
    }

    public boolean createSimpleTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, boolean saveDb) throws Exception {
        return createTable(player,play,bureauCount,params,saveDb);
    }

    public void createTable(Player player, int play, int bureauCount, List<Integer> params) throws Exception {
        createTable(player,play,bureauCount,params,true);
    }




    @Override
    public int getWanFa() {
        return SharedConstants.game_type_paohuzi;
    }

    @Override
    public boolean isTest() {
        return PaohuziConstant.isTest;
    }

    @Override
    public void checkReconnect(Player player) {
        checkMo();
        for(Map.Entry<Integer, LszpPlayer> entry:seatMap.entrySet()){
            if(entry.getValue().getState()==player_state.ready);
        }
        sendPiaoReconnect(player);
    }

    private void sendPiaoReconnect(Player player){
        if(piaoFen==0)
            return;
        int count=0;
        for(Map.Entry<Integer,LszpPlayer> entry:seatMap.entrySet()){
            player_state state = entry.getValue().getState();
            if(state==player_state.play||state==player_state.ready)
                count++;
        }
        if(count!=getPlayerCount())
            return;

        for(Map.Entry<Integer,LszpPlayer> entry:seatMap.entrySet()){
            LszpPlayer p = entry.getValue();
            if(p.getUserId()==player.getUserId()){
                if(p.getPiaoFen()==-1){
                    player.writeComMessage(WebSocketMsgType.res_code_lszp_piaofen);
                    continue;
                }
            }else {
                List<Integer> l=new ArrayList<>();
                l.add((int)p.getUserId());
                l.add(p.getPiaoFen());
                player.writeComMessage(WebSocketMsgType.res_code_lszp_broadcast_piaofen, l);
            }
        }
    }

    @Override
    public void checkAutoPlay() {
        synchronized (this){
            if(!autoPlay)
                return;
            if (getSendDissTime() > 0) {
                for (LszpPlayer player : seatMap.values()) {
                    if (player.getLastCheckTime() > 0) {
                        player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                    }
                }
                return;
            }

            if (isAutoPlayOff()) {
                // 托管关闭
                for (int seat : seatMap.keySet()) {
                    LszpPlayer player = seatMap.get(seat);
                    player.setAutoPlay(false, this);
                    player.setLastOperateTime(System.currentTimeMillis());
                }
                return;
            }

            if (state == table_state.ready&& playedBureau > 0) {
                ++timeNum;
                int i=0;
                for (LszpPlayer player : seatMap.values()) {
					// 玩家进入托管后，5秒自动准备
                    if(player.getState()==player_state.ready){
                        i++;
                    }else if ((timeNum >= 5 && player.isAutoPlay())||timeNum >= 30) {
                        autoReady(player);
                        i++;
                    }
                }
                if(i>=playerCount)
                    changeTableState(table_state.play);
                return;
            }

            int timeout;
            if(state != table_state.play){
                return;
            }else if (isGoldRoom()){
                timeout = autoTimeOut;
            }else if(autoPlay){
                timeout = autoTimeOut;
            }else{
                return;
            }
            //timeout = 10*1000;
            long autoPlayTime = ResourcesConfigsUtil.loadIntegerValue("ServerConfig","autoPlayTimePhz",2*1000);
            long now = TimeUtil.currentTimeMillis();
			// 飘分模式，还没发牌
            if (finishFapai == 0) {
                for (LszpPlayer player : seatMap.values()) {
                    if(player.getPiaoFen()>0){
                        continue;
                    }
                    boolean auto = player.isAutoPlay();
                    if (!auto) {
                        auto = checkPlayerAuto(player, timeout);
                        if (auto) {
                            if (player.getPiaoFen()==-1) {
                                piaoFen(player, 0);
                            }
                        }
                    } else {
                        if (player.getPiaoFen()==-1) {
                            piaoFen(player, 0);
                        }
                    }
                }
                return;
            }


            if(!actionSeatMap.isEmpty()){
                int action = 0,seat = 0;
                for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()){
                    List<Integer> list = PaohzDisAction.parseToDisActionList(entry.getValue());
                    int minAction = Collections.min(list);
                    if(action == 0){
                        action = minAction;
                        seat = entry.getKey();
                    }else if(minAction < action){
                        action = minAction;
                        seat = entry.getKey();
                    }else if(minAction == action){
                        int nearSeat = getNearSeat(disCardSeat, Arrays.asList(seat, entry.getKey()));
                        seat = nearSeat;
                    }
                }
                if(action > 0 && seat > 0){
                    LszpPlayer player = seatMap.get(seat);
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
                            if(action == PaohzDisAction.action_chi){
                                action = PaohzDisAction.action_pass;
                            }
                            if(action == PaohzDisAction.action_pass || action == PaohzDisAction.action_peng || action == PaohzDisAction.action_hu){
                                play(player, new ArrayList<Integer>(), action);
                            }else{
                                checkMo();
                            }
                        }
                        return;
                    }
                    if(action == PaohzDisAction.action_pao && player.getLastCheckTime()>0){
                        checkMo();
                    }
                }
            }else{
                LszpPlayer player = seatMap.get(nowDisCardSeat);
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
                            PaohzCard paohzCard = PaohuziTool.autoDisCard(player.getHandPhzs());
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



    public boolean checkPlayerAuto(LszpPlayer player , int timeout){
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
        } else {
            player.setLastCheckTime(now);
            player.setCheckAuto(true);
            player.setAutoPlayCheckedTimeAdded(false);
        }

        return auto;
    }

    @Override
    public Class<? extends Player> getPlayerClass() {
        return LszpPlayer.class;
    }

    public PaohzCard getNextCard(int val) {
        if (this.leftCards.size() > 0) {
            Iterator<PaohzCard> iterator = this.leftCards.iterator();
            PaohzCard find = null;
            while (iterator.hasNext()) {
                PaohzCard paohzCard = iterator.next();
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

    public PaohzCard getNextCard() {
        if (this.leftCards.size() > 0) {
            PaohzCard card = this.leftCards.remove(0);
            dbParamMap.put("leftPais", JSON_TAG);
            return card;
        }
        return null;
    }

    public List<PaohzCard> getLeftCards() {
        return leftCards;
    }

    public void setLeftCards(List<PaohzCard> leftCards) {
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
        changeExtend();
    }

    public int getMoSeat() {
        return moSeat;
    }

    public void setMoSeat(int lastMoSeat) {
        this.moSeat = lastMoSeat;
        changeExtend();
    }

    public List<PaohzCard> getNowDisCardIds() {
        return nowDisCardIds;
    }

    public void setNowDisCardIds(List<PaohzCard> nowDisCardIds) {
        this.nowDisCardIds = nowDisCardIds;
        dbParamMap.put("nowDisCardIds", JSON_TAG);
    }

    /**
	 * 打出的牌是刚刚摸的
	 */
    public boolean isMoFlag() {
        return disOrMo == 2;
    }

    public int getPaoHu() {
        return paoHu;
    }

    public void setPaoHu(int paoHu) {
        this.paoHu = paoHu;
    }

    public int getDisOrMo() {
        return disOrMo;
    }

    public void setDisOrMo(int disOrMo) {
        if (this.disOrMo != disOrMo) {
            this.disOrMo = disOrMo;
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

    public void markMoSeat(PaohzCard card, int seat) {
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

    // public boolean checkMo

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

    public PaohzCard getZaiCard() {
        return zaiCard;
    }

    public void setZaiCard(PaohzCard zaiCard) {
        this.zaiCard = zaiCard;
        changeExtend();
    }

    public int getSendPaoSeat() {
        return sendPaoSeat;
    }

    public void setSendPaoSeat(int sendPaoSeat) {
        if (this.sendPaoSeat != sendPaoSeat) {
            this.sendPaoSeat = sendPaoSeat;
            changeExtend();
        }

    }


    public Map<Integer, List<Integer>> getActionSeatMap() {
        return actionSeatMap;
    }

    /**
	 * 对应的座位cardId-seat
	 */
    public KeyValuePair<Integer, Integer> getMoSeatPair() {
        return moSeatPair;
    }

    public PaohzCard getBeRemoveCard() {
        return beRemoveCard;
    }

    /**
	 * 桌子上移除的牌
	 */
    public void setBeRemoveCard(PaohzCard beRemoveCard) {
        this.beRemoveCard = beRemoveCard;
        changeExtend();
    }


    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
        changeExtend();
    }


    @Override
    public int getDissPlayerAgreeCount() {
        return getPlayerCount();
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
    public void calcDataStatistics2() {
		// 俱乐部房间 单大局大赢家、单大局大负豪、总小局数、单大局赢最多、单大局输最多 数据统计
        if(isGroupRoom()){
            String groupId=loadGroupId();
            int maxPoint=0;
            int minPoint=0;
            Long dataDate=Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));

            calcDataStatistics3(groupId);

            for (LszpPlayer player:playerMap.values()){
				// 总小局数
                DataStatistics dataStatistics1=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"xjsCount",playedBureau);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics1,3);
                int finalPoint;
                finalPoint = player.loadScore();


				// 总大局数
                DataStatistics dataStatistics5=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"djsCount",1);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5,3);
				// 总积分
                DataStatistics dataStatistics6=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"zjfCount",finalPoint);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics6,3);

                if (finalPoint >0){
                    if (finalPoint >maxPoint){
                        maxPoint= finalPoint;
                    }
					// 单大局赢最多
                    DataStatistics dataStatistics2=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"winMaxScore", finalPoint);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics2,4);
                }else if (finalPoint <0){
                    if (finalPoint <minPoint){
                        minPoint= finalPoint;
                    }
					// 单大局输最多
                    DataStatistics dataStatistics3=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"loseMaxScore", finalPoint);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics3,5);
                }
            }

            for (LszpPlayer player:playerMap.values()){
                int finalPoint= player.loadScore();
                if (maxPoint>0&&maxPoint== finalPoint){
					// 单大局大赢家
                    DataStatistics dataStatistics4=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"dyjCount",1);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4,1);
                }else if (minPoint<0&&minPoint== finalPoint){
					// 单大局大负豪
                    DataStatistics dataStatistics5=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"dfhCount",1);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5,2);
                }
            }
        }
    }

    public long saveUserGroupPlaylog() {
        if(!needSaveUserGroupPlayLog()){
            return 0;
        }
        UserGroupPlaylog userGroupLog = new UserGroupPlaylog();
        userGroupLog.setTableid(id);
        userGroupLog.setUserid(creatorId);
        userGroupLog.setCount(playBureau);
        String players = "";
        String score = "";
        String diFenScore = "";
        for (LszpPlayer player : seatMap.values()) {
            players += player.getUserId() + ",";
            score += player.getTotalPoint() + ",";
            diFenScore += player.getTotalPoint() + ",";

        }
        userGroupLog.setPlayers(players.length() > 0 ? players.substring(0, players.length() - 1) : "");
        userGroupLog.setScore(score.length() > 0 ? score.substring(0, score.length() - 1) : "");
        userGroupLog.setDiFenScore(diFenScore.length() > 0 ? diFenScore.substring(0, diFenScore.length() - 1) : "");
        userGroupLog.setDiFen("");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        userGroupLog.setCreattime(sdf.format(createTime));
        userGroupLog.setOvertime(sdf.format(new Date()));
        userGroupLog.setPlayercount(playerCount);
        userGroupLog.setGroupid(Long.parseLong(loadGroupId()));
        userGroupLog.setGamename(getGameName());
        userGroupLog.setTotalCount(totalBureau);
        return TableLogDao.getInstance().saveGroupPlayLog(userGroupLog);
    }

    @Override
    public String getGameName() {
		return "蓝山字牌";
    }

    public int getPiaoFen() {
        return piaoFen;
    }

    public void setPiaoFen(int piaoFen) {
        this.piaoFen = piaoFen;
    }

    public int getAutoTimeOut() {
        return autoTimeOut;
    }

    public int getAutoTimeOut2() {
        return autoTimeOut2;
    }

    @Override
    public boolean isCreditTable(List<Integer> params){
        return params != null && params.size() > 15 && StringUtil.getIntValue(params, 15, 0) == 1;
    }


    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_lszp);

    public static void loadWanfaTables(Class<? extends BaseTable> cls){
        for (Integer integer:wanfaList){
            TableManager.wanfaTableTypesPut(integer,cls);
        }
    }

    /**
	 * 是否可以退出
	 *
	 * @param player
	 * @return
	 */
    @Override
    public boolean canQuit(Player player) {
        if (state == table_state.play || playedBureau > 0 || isMatchRoom() || isGoldRoom()) {
            return false;
        } else {
            return true;
        }
    }


    @Override
    public boolean isAllReady() {
        if (getPlayerCount() < getMaxPlayerCount()) {
            return false;
        }
        for (Player player : getSeatMap().values()) {
            if(!player.isRobot()){
                if(piaoFen>0){
                    if (!(player.getState() == player_state.ready||player.getState() == player_state.play))
                        return false;
                }else {
                    if(player.getState() != player_state.ready)
                        return false;
                }
            }
        }
        if(finishFapai==1)
            return false;
        changeTableState(table_state.play);
        if (piaoFen > 0 ) {
            boolean piaoFenOver = true;
            for (LszpPlayer player : playerMap.values()) {
                if(player.getPiaoFen()==-1){
                    piaoFenOver = false;
                    break;
                }
            }
            if(!piaoFenOver){
                if (finishFapai==0) {
                    LogUtil.msgLog.info("pdk|sendPiaoFen|" + getId() + "|" + getPlayBureau());
                    ComRes msg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_lszp_piaofen).build();
                    for (LszpPlayer player : playerMap.values()) {
                        if(player.getPiaoFen()==-1)
                            player.writeSocket(msg);
                    }
                }
                return false;
            }
        }
        return true;
    }

    private Map<Integer, TempAction> loadTempActionMap(String json) {
        Map<Integer, TempAction> map = new ConcurrentHashMap<>();
        if (json == null || json.isEmpty())
            return map;
        JSONArray jsonArray = JSONArray.parseArray(json);
        for (Object val : jsonArray) {
            String str = val.toString();
            TempAction tempAction = new TempAction();
            tempAction.initData(str);
            map.put(tempAction.getSeat(), tempAction);
        }
        return map;
    }

    public String getTableMsg() {
        Map<String, Object> json = new HashMap<>();
		json.put("wanFa", "蓝山字牌");
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

}
