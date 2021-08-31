package com.sy599.game.qipai.xx2710.bean;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import com.sy599.game.msg.serverPacket.TablePhzResMsg;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzInfoRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.xx2710.constant.PaohuziConstant;
import com.sy599.game.qipai.xx2710.constant.PaohzCard;
import com.sy599.game.qipai.xx2710.rule.PaohuziMingTangRule;
import com.sy599.game.qipai.xx2710.rule.RobotAI;
import com.sy599.game.qipai.xx2710.tool.PaohuziHuLack;
import com.sy599.game.qipai.xx2710.tool.PaohuziResTool;
import com.sy599.game.qipai.xx2710.tool.PaohuziTool;
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

public class Xx2710Table extends BaseTable {
    /*** 玩家map */
    private Map<Long, Xx2710Player> playerMap = new ConcurrentHashMap<>();
    /*** 座位对应的玩家 */
    private Map<Integer, Xx2710Player> seatMap = new ConcurrentHashMap<>();
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


    //是否已经完成第一次发牌
    private int finishiFapai=0;
    private volatile int timeNum = 0;

    //需要修改，用disNum+moNum代替
    private volatile int moNum=0;
    private volatile int disNum=0;
    //用该状态记录该回合是摸排还是出牌（吃碰后的出牌），1：出牌，2：摸牌，0：尚未开始或者正在吃碰中
    private volatile int disOrMo=0;
    //最后一张操作的牌，肯能是摸的，也可能是打的
    private int lastCardId=-1;
    //所有出去的牌，用于计算听牌
    private List<Integer> outCards=new ArrayList<>();
    /**
     * 玩家位置对应临时操作
     * 当同时存在多个可做的操作时
     * 1当优先级最高的玩家最先操作 则清理所有操作提示 并执行该操作
     * 2当操作优先级低的玩家先选择操作，则玩家先将所做操作存入临时操作中 当玩家优先级最高的玩家操作后 在判断临时操作是否可执行并执行
     */
    private Map<Integer, TempAction> tempActionMap = new ConcurrentHashMap<>();
    //是否加倍
    private int jiaBei;
    //加倍分数：低于xx分进行加倍
    private int jiaBeiFen;
    //加倍倍数：翻几倍
    private int jiaBeiShu;
    /**托管1：单局，2：全局*/
    private int autoPlayGlob;
    private int autoTableCount;
    //抽牌 0：不抽，10：抽10张，20：抽20张
    private int chouCardNum;
    //抽牌牌堆
    List<Integer> chouCards=new ArrayList<>();
    //庄分
    private int zhuangfen;
    //自摸加分
    private int zmjiafen;
    //自摸红字加1分
    private int zmHongJiafen;
    //充分
    private int chongfen;
    // 是否已经发送飘分信息
    private int isSendPiaoFenMsg=0;
    // 确认打鸟次数
    private volatile int confirmTime=0;
    private int lowfen =0;//低于多少分
    private int lowfenAdd = 0;//加多少分
    
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

        int playerCount = StringUtil.getIntValue(params, 7, 2);// 比赛人数
        if (playerCount<=1||playerCount>4){
            return false;
        }
        setPlayerCount(playerCount);
        payType = StringUtil.getIntValue(params, 9, 0);// 房费方式
        chouCardNum=StringUtil.getIntValue(params, 11, 0);// 抽牌
        zhuangfen=StringUtil.getIntValue(params, 12, 0);
        zmjiafen=StringUtil.getIntValue(params, 13, 0);
        zmHongJiafen=StringUtil.getIntValue(params, 14, 0);
        chongfen=StringUtil.getIntValue(params, 15, 0);
        lowfen=StringUtil.getIntValue(params, 16, 0);
        lowfenAdd=StringUtil.getIntValue(params, 17, 0);
        int time=StringUtil.getIntValue(params, 20, 0);//是否托管
        if(time ==1) {
            time =60;
        }
        if(time>0) {
            this.autoPlay =true;
        }
        this.jiaBei = StringUtil.getIntValue(params, 21, 0);
        if(this.getMaxPlayerCount() != 2){
            jiaBei = 0 ;
        }
        this.jiaBeiFen = StringUtil.getIntValue(params, 22, 100);
        this.jiaBeiShu = StringUtil.getIntValue(params, 23, 1);
        autoPlayGlob=StringUtil.getIntValue(params, 24, 0);
        
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
        wrapper.putString(1, StringUtil.implode(huConfirmList, ","));
        wrapper.putInt(2, toPlayCardFlag);
        wrapper.putInt(3, moSeat);
        if (moSeatPair != null) {
            String moSeatPairVal = moSeatPair.getId() + "_" + moSeatPair.getValue();
            wrapper.putString(4, moSeatPairVal);
        }
        wrapper.putString(5, "");
        if (zaiCard != null) {
            wrapper.putInt(6, zaiCard.getId());
        }
        wrapper.putInt(7, sendPaoSeat);
        if (beRemoveCard != null) {
            wrapper.putInt(8, beRemoveCard.getId());
        }
        wrapper.putString(10, StringUtil.implode(chouCards, ","));
        wrapper.putString("startLeftCards", startLeftCardsToJSON());
        wrapper.putInt(11, playerCount);
        wrapper.putInt(13, jiaBei);
        wrapper.putInt(14, jiaBeiFen);
        wrapper.putInt(15, jiaBeiShu);
        wrapper.putInt(17, disNum);
        wrapper.putInt(18, moNum);
        wrapper.putInt(19, disOrMo);
        wrapper.putInt(20, autoPlayGlob);
        wrapper.putInt(21, autoTimeOut);
        wrapper.putInt(22, chouCardNum);
        wrapper.putInt(23, zhuangfen);
        wrapper.putInt(24, zmjiafen);
        wrapper.putInt(25, zmHongJiafen);
        wrapper.putInt(26, chongfen);
        wrapper.putInt(27, lowfen);
        wrapper.putInt(28, lowfenAdd);
        JSONArray tempJsonArray = new JSONArray();
        for (int seat : tempActionMap.keySet()) {
            tempJsonArray.add(tempActionMap.get(seat).buildData());
        }
        wrapper.putString(29, tempJsonArray.toString());
        wrapper.putInt(30, confirmTime);
        wrapper.putInt(31, isSendPiaoFenMsg);
        wrapper.putInt(32, finishiFapai);
        return wrapper;
    }

    /**
     * 需要注意，如果保存了新字段，当服务器重启更新后，新字段为默认值（通常为0）
     * 若项目已上线，且该字段影响程序流程或最终结果，则会产生问题，需要做兼容处理。
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
        zaiCard = PaohzCard.getPaohzCard(wrapper.getInt(6, 0));
        sendPaoSeat = wrapper.getInt(7, 0);
        beRemoveCard = PaohzCard.getPaohzCard(wrapper.getInt(8, 0));
        String chouCardsStr = wrapper.getString(10);
        if (StringUtils.isNotBlank(chouCardsStr)) {
            chouCards = StringUtil.explodeToIntList(chouCardsStr);
        }
        playerCount = wrapper.getInt(11, 3);
        jiaBei = wrapper.getInt(13, 0);
        jiaBeiFen = wrapper.getInt(14, 0);
        jiaBeiShu = wrapper.getInt(15, 0);
        disNum=wrapper.getInt(17,0);
        moNum=wrapper.getInt(18,0);
        disOrMo=wrapper.getInt(19,0);
        autoPlayGlob = wrapper.getInt(20,0);
        autoTimeOut = wrapper.getInt(21,0);
        if(autoPlay&&autoTimeOut<=1) {
            autoTimeOut2=autoTimeOut = 60000;
        }
        chouCardNum=wrapper.getInt(22,0);
        zhuangfen=wrapper.getInt(23,0);
        zmjiafen=wrapper.getInt(24,0);
        zmHongJiafen=wrapper.getInt(25,0);
        chongfen=wrapper.getInt(26,0);
        lowfen=wrapper.getInt(27,0);
        lowfenAdd=wrapper.getInt(28,0);
        
        tempActionMap = loadTempActionMap(wrapper.getString(29));
        confirmTime=wrapper.getInt(30,0);
        isSendPiaoFenMsg=wrapper.getInt(31,0);
        finishiFapai = wrapper.getInt(32, 0);
      //TODO 由于版本更新，需对旧牌桌数据进行兼容,以下代码可在下一版本更新可以删除代码
        // -----------------start------------------------------
        if (finishiFapai == 0) {
            // 检查玩家身上是否有发牌
            for (Xx2710Player player : seatMap.values()) {
                if (player.getHandPais() != null && player.getHandPais().size() > 0) {
                	finishiFapai = 1;
                    break;
                }
            }
        }
//        passPengSeat=wrapper.getInt(41,0);
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

    /**
     * 胡牌结算
     */
    @Override
    public void calcOver() {
    	if (getState() != table_state.play) {
            return; 
		}
		if(getPlayBureau() >= getMaxPlayerCount()){
		  changeTableState(table_state.over);
		}
        boolean isHuangZhuang = false;
        List<Integer> winList=new ArrayList<>(huConfirmList);
        if ((winList.size() == 0 && leftCards.size() == 0)) {
            // 流局
            isHuangZhuang = true;
        }
        List<Integer> mt = null;
        int oldWinSeat = lastWinSeat;
        int winFen=0;
        int lostFen=0;
        boolean isOver = false;
        if(!isHuangZhuang){
            int winSeat=winList.get(0);
            Xx2710Player winner = seatMap.get(winSeat);
            PaohuziHuLack hu = winner.getHu();
//            mt=hu.getMingTang();
            Integer[] jiesuanData = new Integer[]{0,0,0,0,0};
            lostFen=hu.getFen();
            jiesuanData[4]= lostFen;
            this.lastWinSeat=winSeat;
            boolean isSelfMo = winSeat == moSeat;
            if(isSelfMo && zmjiafen == 1){
            	lostFen +=1;//自摸加1分
            	jiesuanData[0]= 1;
            }
            PaohzCard lastCard = lastCardId >0 ?PaohzCard.getPaohzCard(lastCardId):null;
            if(isSelfMo && zmHongJiafen== 1 && lastCard!= null && PaohzCard.isHongpai(lastCard.getVal())){
            	lostFen +=1;//自摸红牌加1分
            	jiesuanData[1]= 1;
            }
            int sumChongfen = 0;
    		int cumZhuangfen = 0;
            
            for (Map.Entry<Integer,Xx2710Player> entry: seatMap.entrySet()) {
                if(entry.getKey()!=winSeat){
                	int losePoint = lostFen;
                    losePoint += winner.getPiaoFen()>0?winner.getPiaoFen():0;
                    sumChongfen+=winner.getPiaoFen()>0?winner.getPiaoFen():0;
                    Xx2710Player player = seatMap.get(entry.getKey());
                    losePoint += player.getPiaoFen()>0?player.getPiaoFen():0;
                    sumChongfen+=player.getPiaoFen()>0?player.getPiaoFen():0;
                    if(winSeat == oldWinSeat || entry.getKey() == oldWinSeat){
                    	losePoint += zhuangfen;
                    	cumZhuangfen += zhuangfen;
                    }
                    
                    player.changePoint(-losePoint);
                    winFen += losePoint;
                }

            }
            jiesuanData[2]= cumZhuangfen;
            jiesuanData[3]= sumChongfen;
            mt = Arrays.asList(jiesuanData);
            winner.changePoint(winFen);
        }
        if(playBureau == totalBureau){
            isOver=true;
            changeTableState(table_state.over);
        }

        if(autoPlayGlob >0) {
            // //是否解散
            boolean diss = false;
            if(autoPlayGlob ==1) {
                for (Xx2710Player seat : seatMap.values()) {
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
        ClosingPhzInfoRes.Builder res = sendAccountsMsg(isOver, winList, winFen, mt, 0, false, null,null);
        saveLog(isOver,0L, res.build());
        if (isOver) {
            //这个方法应该是和红包相关的（不知道现在还有没有用）
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
        // if(autoPlayGlob==3) {
        boolean diss2 = false;
        for (Xx2710Player seat : seatMap.values()) {
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
            for (Xx2710Player player : playerMap.values()) {
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
        if(finishiFapai==0)
            return;
        // 天胡或者暗杠
//        int lastCardIndex = RandomUtils.nextInt(21);
        Xx2710Player winPlayer = seatMap.get(lastWinSeat);

        for (Xx2710Player tablePlayer : seatMap.values()) {
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
        if(finishiFapai==1)
            checkAction();
    }

    public void play(Xx2710Player player, List<Integer> cardIds, int action) {
        play(player, cardIds, action, false, false, false);
    }

    private void hu(Xx2710Player player, List<PaohzCard> cardList, int action, PaohzCard nowDisCard) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (huConfirmList.contains(player.getSeat())) {
            return;
        }
        if (!checkActionNew(player, action,cardList,nowDisCard)) {
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
            // player.writeErrMsg(LangMsgEnum.code_29);
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (actionList.get(0) != 1) {
            return;
        }
        if (!checkAction(player, action)) {
            return;
        }
        List<PaohuziHuLack> huList = player.checkHu(nowDisCard, this);
        PaohuziHuLack maxHu = getMaxHu(huList);
        if (maxHu!=null&&maxHu.isHu()) {
            player.setHu(maxHu);
            player.addHuNum(player.getSeat() == moSeat);
            huConfirmList.add(player.getSeat());
            addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
            sendActionMsg(player, action, null, PaohzDisAction.action_type_action);
            calcOver();
        } else {
            broadMsg(player.getName() + " 不能胡牌");
        }

    }

    public PaohuziHuLack getMaxHu(List<PaohuziHuLack> huList){
        PaohuziHuLack maxHu=null;
        int maxFen=0;
        for (PaohuziHuLack hu:huList) {
            int f = PaohuziMingTangRule.countFen(hu.getMingTang(), this);
            if(f>maxFen){
                maxFen=f;
                hu.setFen(maxFen);
                maxHu=hu;
            }else if(hu.getFen()>maxFen){
                maxFen=hu.getFen();
                maxHu=hu;
            }
        }
        huList.clear();
        return maxHu;
    }


    /**
     * 是否自摸
     *
     * @param player
     * @return
     */
    public boolean isSelfMo(Xx2710Player player) {
        if (moSeatPair != null) {
            return moSeatPair.getValue().intValue() == player.getSeat();
        }
        return false;
    }

    /**
     * 出牌
     */
    private void disCard(Xx2710Player player, List<PaohzCard> cardList, int action) {
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
        if(!player.getHandPais().contains(cardList.get(0).getId())){
            player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, cardList.get(0).getId());
            player.writeErrMsg("手中没有该牌:" + cardList);
            return;
        }
        if(player.getForbidDis().contains(cardList.get(0))) {
            player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, cardList.get(0).getId());
            player.writeErrMsg("锁（old吃过）的牌不能再出" + cardList);
            return;
        }
        if (cardList.get(0).getVal()==0){
            player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, cardList.get(0).getId());
            player.writeErrMsg("王牌不能出" + cardList);
            return;
        }
        if(cardList.size()>1){
            disOrMo=0;
        }else {
            disNum++;
            disOrMo=1;
            player.addDisNum();
        }
        addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
        player.disCard(action, cardList);
        player.addHandOut(cardList);
        if(action==0&&cardList.size()==1){
        	player.disPass(cardList.get(0).getVal());
        	player.pass(PaohzDisAction.action_chi, cardList.get(0).getVal(), true);
        }
        player.disTing(cardList.get(0).getId());
        markMoSeat(player.getSeat(), action);
        clearMoSeatPair();
        setToPlayCardFlag(0); // 应该要打牌的flag
        setDisCardSeat(player.getSeat());
        setNowDisCardIds(cardList);
        setNowDisCardSeat(getNextDisCardSeat());
        PaohuziCheckCardBean autoDisCard = checkDisAction(player, action, cardList.get(0), true);
        if(autoDisCard==null)
            return;
        sendActionMsg(player, action, cardList, PaohzDisAction.action_type_dis);
        checkAutoMo();
    }

    private void checkAutoMo() {
        if (isTest()) {
            checkMo();
        }
    }

    /**
     * 碰
     */
    private void peng(Xx2710Player player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (!checkActionNew(player, action, cardList, nowDisCard)) {
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
            return;
        }

        cardList = player.getPengList(nowDisCard, cardList);
        if (cardList == null) {
            player.writeErrMsg("不能碰");
            return;
        }
        if(!player.isCanPeng(nowDisCard)){
        	 player.writeErrMsg("不能碰");
             return;
        }
        if (!cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        }
        setBeRemoveCard(nowDisCard);
        getDisPlayer().removeOutPais(nowDisCard);
        addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
        player.disCard(action, cardList);
        sendForbidChi(player,nowDisCard.getVal());
        clearAction();

        boolean disCard = setDisPlayer(player, false);

        sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action);
        if (!disCard) {
            // checkMo();
        }

        // 碰的情况,把所有玩家的过牌去掉
        if (isMoFlag()) {
            for (Xx2710Player seatPlayer : seatMap.values()) {
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
    private void pass(Xx2710Player player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            // player.writeErrMsg("该玩家没有找到可以过的动作");
            return;
        }
        
//        if (!checkActionNew(player, action, cardList, nowDisCard)) {
//            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
//            return;
//        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        List<Integer> list = PaohzDisAction.parseToDisActionList(actionList);
        // 可以胡牌，然后点了过
        boolean isPassHu = actionList.get(0) == 1;
        if (isPassHu && player.getHandPhzs().isEmpty()) {
            player.writeErrMsg("手上已经没有牌了");
            return;
        }
//        if (isPassHu && bossOut==2&&nowDisCard.getVal()==0) {
//            player.writeErrMsg("该玩法模式下不能过王胡");
//            return;
//        }
        if(action==PaohzDisAction.action_pass){
            if(nowDisCard!=null)
                addPlayLog(player.getSeat(), PaohzDisAction.action_guo + "",nowDisCard.getId()+"");
            else
                addPlayLog(player.getSeat(), PaohzDisAction.action_guo + "");
        }
        removeAction(player.getSeat());
        markMoSeat(player.getSeat(), action);
        if(nowDisCard!=null){
            if(isPassHu&&nowDisCard.getVal()==0){
                setToPlayCardFlag(1);
                player.getAllDisTing(this);
                player.sendAllDisTing();
                sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action);
                return;
            }
            int val = 0;
            val = nowDisCard.getVal();
            // 将pass的吃碰值添加到passChi或passPeng中
            boolean addPassChi = false;
            if (player.getSeat() == moSeat) {
                addPassChi = true;
            }
            for (int i = list.size()-1; i >=0 ; i--) {
            	player.pass(list.get(i), val, addPassChi);
			}
//            for (int passAction : list) {
//                player.pass(passAction, val, addPassChi);
//            }
        }
        // 自动出牌
        PaohuziCheckCardBean checkCard = player.checkCard(nowDisCard, isSelfMo(player), isPassHu, false, false, true);
        checkPaohuziCheckCard(checkCard);
        //应该是在此处告诉前端，已执行pass操作
        sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action);
        if (PaohuziConstant.isAutoMo) {
            //在此处查询可以摸排的玩家并通知其摸排
            checkMo();
        } else {
            if (isTest()) {
                checkMo();
            }
        }

        refreshTempAction(player);

        if (this.leftCards.size() == 0 && !isHasSpecialAction()) {
            calcOver();
        }

    }

    /**
     * 吃
     */
    private void chi(Xx2710Player player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
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

        if(!player.isCanChi(nowDisCard,cardList)){
            player.writeErrMsg("不能吃");
            return;
        }
        if(!player.isCanChiOperateCards(nowDisCard,cardList)){
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

        if (!checkActionNew(player, action, cardList, nowDisCard)) {
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
            // 能吃能碰的情况下 如果吃下去了不算过碰，若是被别人抢先吃了，算过碰
//            List<Integer> list = actionSeatMap.get(player.getSeat());
//            if(list!= null && list.get(4)==1)
//                passPengSeat=player.getSeat();
            return;
        }else {
//            if(passPengSeat!=0&&player.getSeat()!=passPengSeat){
//                seatMap.get(passPengSeat).passPeng(cardList.get(0).getVal());
//                passPengSeat=0;
//            }
        }

        if (PaohuziTool.isPaohuziRepeat(cardList)) {
            player.writeErrMsg("不能吃");
            return;
        }
        List<PaohzCard> chilist = new ArrayList<>();
        if (!cardList.contains(nowDisCard)) {
        	chilist.addAll(cardList);
            cardList.add(0, nowDisCard);
        } else {
            cardList.remove(nowDisCard);
            chilist.addAll(cardList);
            cardList.add(0, nowDisCard);
        }
        setBeRemoveCard(nowDisCard);

        getDisPlayer().removeOutPais(nowDisCard);
        addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
        player.disCard(action, cardList);
        
        if(chilist.size() == 2){
        	Collections.sort(chilist, new Comparator<PaohzCard>() {
        		@Override             
        		public int compare(PaohzCard o1, PaohzCard o2) {
        			if(o1.getVal() > o2.getVal()){
        				return 1;
        			}              
        			return -1;      
        		}     
        	});  
        	int card1 = chilist.get(0).getVal();
        	int card2 = chilist.get(1).getVal();
        	
        	int chaVal = card1 - card2;
        	if(chaVal!= -1){
        		sendForbidChi(player,nowDisCard.getVal());
        	}else{
        		int a = card1 - 1;
        		int b = card2 + 1;
        		sendForbidChi(player,a,b);
        	}
        	
        	
        }
        
        clearAction();
        boolean disCard = setDisPlayer(player, false);
        sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action);
        if (!disCard) {
            if (PaohuziConstant.isAutoMo) {
                checkMo();
            }
        }
    }

    private void sendForbidChi(Xx2710Player player,int... cardVal){
        List<Integer> forbidDis = player.getForbidDis();
        if(cardVal.length > 0){
        	for (int i = 0; i < cardVal.length; i++) {
        		 List<PaohzCard> cards = PaohzCard.getPaohzCardsByVal(cardVal[i]);
                 forbidDis.addAll(PaohuziTool.toPhzCardIds(cards));
			}
        }
        player.writeComMessage(WebSocketMsgType.res_code_zhz_zhihui, forbidDis);
    }


    public synchronized void play(Xx2710Player player, List<Integer> cardIds, int action, boolean moPai, boolean isHu, boolean isPassHu) {
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
            if (cardIds != null && !cardIds.isEmpty()) {
                cardList = PaohuziTool.toPhzCards(cardIds);
            }
        }
        if (action != PaohzDisAction.action_mo) {
            StringBuilder sb = new StringBuilder("xx2710");
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
        if (action == PaohzDisAction.action_hu) {
            hu(player, cardList, action, nowDisCard);
        } else if (action == PaohzDisAction.action_peng) {
            peng(player, cardList, nowDisCard, action);
        } else if (action == PaohzDisAction.action_chi) {
            chi(player, cardList, nowDisCard, action);
        } else if (action == PaohzDisAction.action_pass) {
            pass(player, cardList, nowDisCard, action);
        } else if (action == PaohzDisAction.action_mo) {
            //当pass后会自动摸排，这个方法的作用只是客户端摸排后回来访问一次，做后续操作，实际牌已入手
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
        else {
            disCard(player, cardList, action);
        }
        if (!moPai && !isHu) {
            // 摸牌的时候提不需要做操作
            robotDealAction();
        }

    }

    /**
     * 设置要出牌的玩家
     */
    private boolean setDisPlayer(Xx2710Player player, boolean isHu) {
        if (this.leftCards.isEmpty()) {
            // 已经没有牌了
            if (!isHu) {
                calcOver();
            }
            return false;
        }
        if (player.getHandPhzs().isEmpty()||player.getOperateCards()==null||player.getOperateCards().size()==0) {
            player.setSiShou(true);
            setNowDisCardSeat(calcNextSeat(player.getSeat()));
            return false;
        }
        setNowDisCardSeat(player.getSeat());
        setToPlayCardFlag(1);
        return true;
    }

    /**
     * 检查优先度，胡杠补碰吃 如果同时出现一个事件，按出牌座位顺序优先
     */
    private boolean checkAction(Xx2710Player player, int action) {
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
    private boolean checkActionNew(Xx2710Player player, int action, List<PaohzCard> cardList, PaohzCard nowDisCard) {
        // 优先度为胡杠补碰吃
        boolean canPlay = true;
        List<Integer> stopActionList = PaohzDisAction.findPriorityAction(action);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (player.getSeat() != entry.getKey()) {
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
        if (canPlay) {
            clearTempAction();
            return true;
        }

        int seat = player.getSeat();
        tempActionMap.put(seat, new TempAction(seat, action, cardList, nowDisCard));

        // 玩家都已选择自己的临时操作后  选取优先级最高
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
            Xx2710Player tempPlayer = seatMap.get(maxSeat);
            List<PaohzCard> tempCardList = tempActionMap.get(maxSeat).getCardList();
            for (int removeSeat : prioritySeats.keySet()) {
                if (removeSeat != maxSeat) {
                    removeAction(removeSeat);
                }
            }
            clearTempAction();
            // 系统选取优先级最高操作
            play(tempPlayer, PaohuziTool.toPhzCardIds(tempCardList), maxAction);
        }
        return canPlay;
    }

    /**
     * 执行可做操作里面优先级最高的玩家操作
     *
     * @param player
     */
    private void refreshTempAction(Xx2710Player player) {
        tempActionMap.remove(player.getSeat());
        Map<Integer, Integer> prioritySeats = new HashMap<>();//各位置优先操作
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            int seat = entry.getKey();
            List<Integer> actionList = entry.getValue();
            List<Integer> list = PaohuziDisAction.parseToDisActionList(actionList);
            int priorityAction = PaohzDisAction.getMaxPriorityAction(list);
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
        Iterator<TempAction> iterator = tempActionMap.values().iterator();
        while (iterator.hasNext()) {
            TempAction tempAction = iterator.next();
            if (tempAction.getSeat() == maxPrioritySeat) {
                int action = tempAction.getAction();
                List<PaohzCard> tempCardList = tempAction.getCardList();
                Xx2710Player tempPlayer = seatMap.get(tempAction.getSeat());
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
    private Xx2710Player getDisPlayer() {
        return seatMap.get(disCardSeat);
    }

    @Override
    public int isCanPlay() {
        if (getPlayerCount() < getMaxPlayerCount()) {
            return 1;
        }
        return 0;
    }

    private synchronized void checkMo() {
        // 0胡 1碰 2栽 3提 4吃 5跑
        if (!actionSeatMap.isEmpty()) {
            return;
        }
        if (nowDisCardSeat == 0) {
            return;
        }
        // // 下一个要摸牌的人
        Xx2710Player player = seatMap.get(nowDisCardSeat);
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
        player.addMoNum();
        PaohzCard card= getNextCard();
        StringBuilder sb = new StringBuilder("xx2710");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append("moPai");
        sb.append("|").append(card);
        LogUtil.msgLog.info(sb.toString());
        if (card != null) {
            disOrMo=2;
            setMoSeat(player.getSeat());
            markMoSeat(card, player.getSeat());
            lastCardId=card.getId();
            player.moCard(card);
            setDisCardSeat(player.getSeat());
            setNowDisCardIds(new ArrayList<>(Arrays.asList(card)));
            boolean flag=false;
            if(card.getVal()==0){
                player.moBoss(card);
                if(player.isHu(null, this)){
                    PaohuziCheckCardBean check=new PaohuziCheckCardBean();
                    check.setHu(true);
                    check.setSeat(player.getSeat());
                    check.buildActionList();
                    checkPaohuziCheckCard(check);
                }else {
                    setToPlayCardFlag(1);
                }
            }else {
                int nowSeat=player.getSeat();
                int i=1;
                while (true){
                    Xx2710Player p = seatMap.get(nowSeat);
                    PaohuziCheckCardBean checkCard;
                    checkCard = p.checkCard(card, p.getSeat() == player.getSeat(), false);
                
                    checkPaohuziCheckCard(checkCard);
                    nowSeat = calcNextSeat(nowSeat);
                    if(i>=playerCount)
                        break;
                    i++;
                }
                setNowDisCardSeat(getNextDisCardSeat());
            }
            markMoSeat(player.getSeat(), PaohzDisAction.action_mo);
            boolean mo=false;
            if(card.getVal()==0){
                if(player.getOperateCards().size()==0){
                    mo=true;
                    setToPlayCardFlag(0);
                    setNowDisCardSeat(calcNextSeat(player.getSeat()));
                }
                sendActionMsg(player, PaohzDisAction.action_mowanghuishou, new ArrayList<>(Arrays.asList(card)), 0);
                addPlayLog(player.getSeat(), PaohzDisAction.action_mowanghuishou + "", (card == null ? 0 : card.getId()) + "");
            }else {
                sendActionMsg(player, PaohzDisAction.action_mo, new ArrayList<>(Arrays.asList(card)), PaohzDisAction.action_type_mo);
                addPlayLog(player.getSeat(), PaohzDisAction.action_mo + "", (card == null ? 0 : card.getId()) + "");
            }
            if (this.leftCards != null && this.leftCards.size() == 0 && !isHasSpecialAction()) {
                calcOver();
                return;
            }
            if(mo){
                checkMo();
            }
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
    private PaohuziCheckCardBean checkDisAction(Xx2710Player player, int action, PaohzCard disCard, boolean canBoom) {
        PaohuziCheckCardBean autoDisCheck = new PaohuziCheckCardBean();
        int nowSeat=disCardSeat;
        int i=1;
        while (true){
            nowSeat = calcNextSeat(nowSeat);
            i++;
            Xx2710Player p = seatMap.get(nowSeat);
            if (p.getUserId()==player.getUserId())
                continue;
            PaohuziCheckCardBean checkCard = p.checkCard(disCard,false,!canBoom,false,canBoom,false);
            checkPaohuziCheckCard(checkCard);
            if(nowSeat==disCardSeat||i>=playerCount)
                break;
        }
        return autoDisCheck;
    }

    private boolean isFangZhao(Xx2710Player player, PaohzCard disCard) {

        for (Entry<Integer, Xx2710Player> entry : seatMap.entrySet()) {
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

    public void checkPaohuziCheckCard(PaohuziCheckCardBean checkCard) {
        List<Integer> list = checkCard.getActionList();
        if (list == null || list.isEmpty()) {
            return;
        }
        addAction(checkCard.getSeat(), list);
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


    private void sendActionMsg(Xx2710Player player, int action, List<PaohzCard> cards, int actType) {
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
    private void sendMoMsg(Xx2710Player player, int action, List<PaohzCard> cards, int actType) {
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
    private void sendPlayerActionMsg(Xx2710Player player) {
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
        builder.setTimeSeat(nowDisCardSeat);
        if (toPlayCardFlag == 1) {
            builder.setNextSeat(nowDisCardSeat);
        } else {
            builder.setNextSeat(0);
        }
    }

    /**
     * 发送动作msg
     *
     * @param player
     * @param action
     * @param cards
     * @param actType
     */
    private void sendActionMsg(Xx2710Player player, int action, List<PaohzCard> cards, int actType, boolean isZaiPao, boolean isChongPao) {
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(action);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.setHuxi(0);
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
        if(getToPlayCardFlag()==1){
            player.getAllDisTing(this);
            player.sendAllDisTing();
        }
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
        Xx2710Player winPlayer = seatMap.get(lastWinSeat);
        for (Xx2710Player player : seatMap.values()) {
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
            }
        }

        Xx2710Player winPlayer = seatMap.get(lastWinSeat);

        for (Xx2710Player player : seatMap.values()) {
            PlayPaohuziRes.Builder copy = builder.clone();
            //摸到王牌不推送消息
//            if(copy.getSeat() == player.getSeat()&&copy.getAction()==PaohzDisAction.action_mo&&copy.getPhzIdsList().get(0)>80)
//                continue;
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
                StringBuilder sb = new StringBuilder("xx2710");
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
        Xx2710Player disCSMajiangPlayer = seatMap.get(disCardSeat);
        PaohuziResTool.buildPlayRes(disBuilder, disCSMajiangPlayer, 0, null);
        disBuilder.setRemain(leftCards.size());
        disBuilder.setHuxi(disCSMajiangPlayer.getOutHuxi() + disCSMajiangPlayer.getZaiHuxi());
        // disBuilder.setNextSeat(nowDisCardSeat);
        setNextSeatMsg(disBuilder);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            PlayPaohuziRes.Builder copy = disBuilder.clone();
            List<Integer> actionList = entry.getValue();
            copy.addAllSelfAct(actionList);
            Xx2710Player seatPlayer = seatMap.get(entry.getKey());
            seatPlayer.writeSocket(copy.build());
        }

    }

    public void checkAction() {
        int nowSeat = getNowDisCardSeat();
        int i=0;
        while (true){
            // 先判断拿牌的玩家
            Xx2710Player nowPlayer = seatMap.get(nowSeat);
            if (nowPlayer == null) {
                return;
            }
            if(nowSeat==lastWinSeat){
                //获取可以做的所有操作
                PaohuziCheckCardBean checkCard = nowPlayer.checkCard(null, true, true, false);
                checkPaohuziCheckCard(checkCard);
                checkSendActionMsg();
                if(!checkCard.isHu()){
                    nowPlayer.getAllDisTing(this);
                    nowPlayer.sendAllDisTing();
                }
            }else {
                nowPlayer.getQishouTing(this);
                nowPlayer.sendTing();
            }
            i++;
            nowSeat=calcNextSeat(nowSeat);
            if(i>=getPlayerCount())
                break;
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
                Xx2710Player player = seatMap.get(nextseat);
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
                    Xx2710Player player = seatMap.get(key);
                    if (player == null || !player.isRobot()) {
                        // player.writeErrMsg(player.getName() + " 有动作" +
                        // entry.getValue());
                        continue;
                    }
                    List<Integer> actions = PaohzDisAction.parseToDisActionList(value);
                    for (int action : actions) {
                        if (!checkActionNew(player, action,null,null)) {
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
        disOrMo=0;
        disNum=0;
        finishiFapai=0;
        timeNum = 0 ;
        outCards.clear();
        isSendPiaoFenMsg=0;
        confirmTime=0;
        clearTempAction();
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

            changeTableState(table_state.play);
            deal();
        }
    }

    @Override
    protected void deal() {

        if (playedBureau<=0){
            for (Xx2710Player player : playerMap.values()) {
//                player.setAutoPlay(false,this);
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
            	 setLastWinSeat(playerMap.get(masterId).getSeat());
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
        //洗牌之后发牌



        List<List<PaohzCard>> list = PaohuziTool.fapai(copy, zp ,16,playerCount);
        StringBuilder s = new StringBuilder("xx2710");
        s.append("|").append(getId());
        s.append("|").append(getPlayBureau());
        s.append("|").append("allCards");
        s.append("|").append(list);
        LogUtil.msgLog.info(s.toString());
        int seat=lastWinSeat;

        for (int i = 0; i < playerCount; i++) {
            Xx2710Player player = seatMap.get(seat);
            player.changeState(player_state.play);
            player.getFirstPais().clear();
            player.dealHandPais(list.get(i));
            player.getFirstPais().addAll(PaohuziTool.toPhzCardIds(new ArrayList(list.get(i))));
            seat=calcNextSeat(seat);

            StringBuilder sb = new StringBuilder("xx2710");
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
        //抽排
        if(chouCardNum==10||chouCardNum==20){
            List<PaohzCard> chuPaiList = cardList.subList(cardList.size() - chouCardNum, cardList.size());
            chouCards = PaohuziTool.toPhzCardIds(chuPaiList);
            cardList = cardList.subList(0, cardList.size() - chouCardNum);

            StringBuilder sb = new StringBuilder("xx2710");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append("chouPai");
            sb.append("|").append(chuPaiList);
            LogUtil.msgLog.info(sb.toString());
        }

        //桌上所有剩余牌
        setStartLeftCards(PaohuziTool.toPhzCardIds(cardList));
        setLeftCards(new ArrayList<>(cardList));
        finishiFapai=1;
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
        for (Xx2710Player player : playerMap.values()) {
            PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(userId, isrecover);
            if (playerRes==null){
                continue;
            }
            //是否为庄
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
        //下标二
        res.addExt(0);// 2
        res.addExt(0);// 3
        res.addExt(0);// 4
        res.addExt(0);// 5
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
//        res.addExt(creditJoinLimit);// 11
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

        res.addTimeOut((isGoldRoom() || autoPlay) ? autoTimeOut :0);
        res.addTimeOut(autoCheckTime);
        res.addTimeOut((isGoldRoom() || autoPlay) ? autoTimeOut2 :0);
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
        Xx2710Player winPlayer = null;

        //大结算计算加倍分
//        if(over && jiaBei == 1){
//            int jiaBeiPoint = 0;
//            int loserCount = 0;
//            for (Xx2710Player player : seatMap.values()) {
//                if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
//                    jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
//                    player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
//                } else if (player.getTotalPoint() < 0) {
//                    loserCount++;
//                }
//            }
//            if (jiaBeiPoint > 0) {
//                for (Xx2710Player player : seatMap.values()) {
//                    if (player.getTotalPoint() < 0) {
//                        player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
//                    }
//                }
//            }
//
//        }
      //大结算,找赢家,赢家可能会有多个
        int totalAddPoint = 0;
        List<Integer> bigWinList=new ArrayList<>();
        Iterator<Xx2710Player> iterator = seatMap.values().iterator();
        while (iterator.hasNext()) {
        	Xx2710Player p = iterator.next();
            if (p != null) {
                //展示分
//                p.setWinLossPoint(p.getTotalPoint());
                if (p.getTotalPoint() > 0) {
                    if (over) {
                        //大结算计算加倍分
                        int addScore = calcFinalRatio(p);
                        //低于X分进行加倍
                        totalAddPoint += addScore;
                        LogUtil.printDebug("加分:{}", addScore);
                        p.setTotalPoint(p.getTotalPoint() + addScore);
                    }

                    if (!bigWinList.contains(p.getSeat()))
                        bigWinList.add(p.getSeat());
                }
            }
        }

        //总翻倍和增加的分平均扣在每个输家身上
        int avg = totalAddPoint == 0 ? 0 : bigWinList.size() < seatMap.size() ? totalAddPoint / (seatMap.size() - bigWinList.size()) : totalAddPoint;

        //使用winLossPoint展示小结算总分,总结算分额外展示
        seatMap.values().stream().filter(v -> !bigWinList.contains(v.getSeat())).forEach(v -> {
            v.setTotalPoint(v.getTotalPoint() - avg);
        });
        List<TablePhzResMsg.PhzHuCardList> cardCombos = new ArrayList<>();

        for (Xx2710Player player : seatMap.values()) {
            if (winList != null && winList.contains(player.getSeat())) {
                winPlayer = seatMap.get(player.getSeat());
            }
            ClosingPhzPlayerInfoRes.Builder build;
            //总分大结算
//
            build = player.bulidTotalClosingPlayerInfoRes();
            build.addAllFirstCards(player.getFirstPais());//将初始手牌装入网络对象
//            build.addStrExt(player.getMaxPoint()+"");//0
//            build.addStrExt(player.getDaHuNum()+"");//1
//            build.addStrExt(player.getXiaoHuNum()+"");//2
//            build.addStrExt("0").addStrExt("0").addStrExt("0").addStrExt("0");//3.4,5,6,7
//            build.addStrExt(ticketMap==null?"0":String.valueOf(ticketMap.getOrDefault(player.getUserId(),0)));//7
            build.addStrExt(player.getXiaoHuNum()+"");//0胡牌次数
            build.addStrExt(player.getDaHuNum()+"");//1自摸次数
            build.addStrExt(player.getPiaoFen()+"");//2飘风数据
            build.addStrExt("0").addStrExt("0").addStrExt("0").addStrExt("0");;
            build.addStrExt("0");
            builderList.add(build);
            player.setWinLoseCredit(player.getTotalPoint() * creditDifen);
            
            TablePhzResMsg.PhzHuCardList.Builder builder = TablePhzResMsg.PhzHuCardList.newBuilder();
            builder.setSeat(player.getSeat());
            builder.addAllPhzCard(player.buildNormalPhzHuCards());
            cardCombos.add(builder.build());

        }

        //信用分计算
        if (isCreditTable()) {
            //计算信用负分
            calcNegativeCredit();

            long dyjCredit = 0;
            for (Xx2710Player player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
                Xx2710Player player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);
                builder.addStrExt(player.getWinLoseCredit() + "");      //8
                builder.addStrExt(player.getCommissionCredit() + "");   //9
                // 2019-02-26更新
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (Xx2710Player player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
                Xx2710Player player = seatMap.get(builder.getSeat());
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
            Xx2710Player player = seatMap.get(builder.getSeat());
            builder.addStrExt(-1+"");//10
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
            res.setHuCard(lastCardId);
            res.addAllCards(winPlayer.buildPhzHuCards());
        }
        res.addAllClosingPlayers(list);
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.addAllExt(buildAccountsExt(over));
        res.addAllStartLeftCards(startLeftCards);
        res.addAllAllCardsCombo(cardCombos);
        if (over && isGroupRoom() && !isCreditTable()) {
            res.setGroupLogId((int) saveUserGroupPlaylog());
        }
        for (Xx2710Player player : seatMap.values()) {
            player.writeSocket(res.build());
        }
        return res;
    }

    /**
     *@description 计算最终翻倍倍率
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2019/9/16
     */
    private int calcFinalRatio(Xx2710Player v) {
        //负分不做翻倍
        if (v.getTotalPoint() < 0) {
            return 0;
        }
        int ratio = doubleChipEffect(v.getTotalPoint());
        //低于X分进行加倍
        int doubleChip = (ratio * v.getTotalPoint()) - v.getTotalPoint();
        LogUtil.printDebug(v.getUserId() + "大结算加倍:倍率:{},winLoss:{},totalPoint:{}->point:{}", ratio, v.getTotalPoint(), v.getTotalPoint(), v.getPoint());

        int addScore = lowScoreEffect(v.getTotalPoint()+doubleChip);
        //低于X分进行加分
        int addScoreChip = (addScore +  v.getTotalPoint()) - v.getTotalPoint();
        LogUtil.printDebug(v.getUserId() + "大结算低于多少分+多少分:阈值:{},winLoss:{},totalPoint:{}->point:{}", addScore, v.getTotalPoint(), v.getTotalPoint(), v.getPoint());

        //把加的分都总计一下
        return doubleChip + addScoreChip;
    }
    public int doubleChipEffect(int currentScore) {
        return Math.max(isDoubleChip() && Math.abs(currentScore) < jiaBeiFen ? jiaBeiShu : 1, 1);
    }
    public boolean isDoubleChip() {
        return jiaBei == 1;
    }
    public int lowScoreEffect(int currentScore) {
        return lowfen > 0 && Math.abs(currentScore) < lowfen ? currentScore > 0 ? lowfenAdd : -lowfenAdd : 0;
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
        //金币场大于0
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


        //信用分
        ext.add(creditMode + ""); //15
        ext.add(creditJoinLimit + "");//16
        ext.add(creditDissLimit + "");//17
        ext.add(creditDifen + "");//18
        ext.add(creditCommission + "");//19
        ext.add(creditCommissionMode1 + "");//20
        ext.add(creditCommissionMode2 + "");//21
        ext.add(autoPlay ? "1" : "0");//22
        ext.add(jiaBei + "");//23
        ext.add(jiaBeiFen + "");//24
        ext.add(jiaBeiShu + "");//25

        ext.add(lastWinSeat + ""); // 26
        return ext;
    }
    private String dissInfo(){
        JSONObject jsonObject = new JSONObject();
        if(getSpecialDiss() == 1){
            jsonObject.put("dissState", "1");//群主解散
        }else{
            if(answerDissMap != null && !answerDissMap.isEmpty()){
                jsonObject.put("dissState", "2");//玩家申请解散
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
                jsonObject.put("dissState", "0");//正常打完
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

    public void
    createTable(Player player, int play, int bureauCount, List<Integer> params) throws Exception {
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
        Xx2710Player p=(Xx2710Player)player;
        player.writeComMessage(WebSocketMsgType.res_code_zhz_zhihui, p.getForbidDis());
        p.getAllDisTing(this);
        p.sendAllDisTing();
        p.sendTing();
    }


    @Override
    public void checkAutoPlay() {
        synchronized (this){
            if (getSendDissTime() > 0) {
                for (Xx2710Player player : seatMap.values()) {
                    if (player.getLastCheckTime() > 0) {
                        player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                    }
                }
                return;
            }

            if (isAutoPlayOff()) {
                // 托管关闭
                for (int seat : seatMap.keySet()) {
                    Xx2710Player player = seatMap.get(seat);
                    player.setAutoPlay(false, this);
                    player.setLastOperateTime(System.currentTimeMillis());
                }
                return;
            }

            if (autoPlay && state == table_state.ready && playedBureau > 0) {
                ++timeNum;
                for (Xx2710Player player : seatMap.values()) {
                    // 玩家进入托管后，5秒自动准备
                    if (timeNum >= 5 && player.isAutoPlay()) {
                        autoReady(player);
                    } else if (timeNum >= 30) {
                        autoReady(player);
                    }
                }
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
            
            
        	// 飘分模式，还没发牌
            if (isSendPiaoFenMsg == 1 && finishiFapai == 0) {
                for (Xx2710Player player : seatMap.values()) {
                    if(player.isAlreadyPiaoFen()){
                        continue;
                    }
                    boolean auto = player.isAutoPlay();
                    if (!auto) {
                        auto = checkPlayerAuto(player, timeout);
                        if (auto) {
                            if (!player.isAlreadyPiaoFen()) {
                                piaoFen(player, 0);
                            }
                        }
                    } else {
                        if (!player.isAlreadyPiaoFen()) {
                            piaoFen(player, 0);
                        }
                    }
                }
                return;
            }
            
            //timeout = 10*1000;
            long autoPlayTime = ResourcesConfigsUtil.loadIntegerValue("ServerConfig","autoPlayTimePhz",2*1000);
            long now = TimeUtil.currentTimeMillis();
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
                    Xx2710Player player = seatMap.get(seat);
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
                Xx2710Player player = seatMap.get(nowDisCardSeat);
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

    public boolean checkPlayerAuto(Xx2710Player player ,int timeout){
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
        return Xx2710Player.class;
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

    public List<Integer> getOutCards() {
        return outCards;
    }

    public void setOutCards(List<Integer> outCards) {
        this.outCards = outCards;
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

    public boolean isTianHu(){
        return disOrMo==0&&moNum==0&&disNum==0;
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

    /**
     * 是否是该玩家摸的牌
     */
    public boolean isMoByPlayer(Xx2710Player player) {
        if (moSeatPair != null && moSeatPair.getValue() == player.getSeat()) {
            if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
                return nowDisCardIds.get(0).getId() == moSeatPair.getId();
            }
        }
        return false;
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
        //俱乐部房间 单大局大赢家、单大局大负豪、总小局数、单大局赢最多、单大局输最多 数据统计
        if(isGroupRoom()){
            String groupId=loadGroupId();
            int maxPoint=0;
            int minPoint=0;
            Long dataDate=Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));

            calcDataStatistics3(groupId);

            for (Xx2710Player player:playerMap.values()){
                //总小局数
                DataStatistics dataStatistics1=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"xjsCount",playedBureau);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics1,3);
                int finalPoint;
                finalPoint = player.loadScore();


                //总大局数
                DataStatistics dataStatistics5=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"djsCount",1);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5,3);
                //总积分
                DataStatistics dataStatistics6=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"zjfCount",finalPoint);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics6,3);

                if (finalPoint >0){
                    if (finalPoint >maxPoint){
                        maxPoint= finalPoint;
                    }
                    //单大局赢最多
                    DataStatistics dataStatistics2=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"winMaxScore", finalPoint);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics2,4);
                }else if (finalPoint <0){
                    if (finalPoint <minPoint){
                        minPoint= finalPoint;
                    }
                    //单大局输最多
                    DataStatistics dataStatistics3=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"loseMaxScore", finalPoint);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics3,5);
                }
            }

            for (Xx2710Player player:playerMap.values()){
                int finalPoint= player.loadScore();
                if (maxPoint>0&&maxPoint== finalPoint){
                    //单大局大赢家
                    DataStatistics dataStatistics4=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"dyjCount",1);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4,1);
                }else if (minPoint<0&&minPoint== finalPoint){
                    //单大局大负豪
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
        for (Xx2710Player player : seatMap.values()) {
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
        return "湘西2710";
    }

    public int getAutoTimeOut() {
        return autoTimeOut;
    }

    public int getAutoTimeOut2() {
        return autoTimeOut2;
    }

    public int getMoNum() {
        return moNum;
    }

    public void setMoNum(int moNum) {
        this.moNum = moNum;
    }

    @Override
    public boolean isCreditTable(List<Integer> params){
        return params != null && params.size() > 15 && StringUtil.getIntValue(params, 15, 0) == 1;
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


    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_xiangxi2710_paohuzi);

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
        } else if(state == table_state.ready ){
            if(isSendPiaoFenMsg==1){
                return false;
            }
            return true;
        }else {
            return true;
        }
    }

    public String getTableMsg() {
        Map<String, Object> json = new HashMap<>();
        json.put("wanFa", "湘西2710");
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

    
    public int getChongfen() {
		return chongfen;
	}

    public int getIsSendPiaoFenMsg() {
        return isSendPiaoFenMsg;
    }

	@Override
    public boolean isAllReady() {
		
//		LogUtil.msgLog.info("xx2710|"+getId()+"|"+getPlayBureau()+"|判断是否准备begin");
		
        if (getPlayerCount() < getMaxPlayerCount()) {
            return false;
        }
        for (Player player : getSeatMap().values()) {
//        	LogUtil.msgLog.info("xx2710|"+getId()+"|"+getPlayBureau()+"|账号"+player.getFlatId()+"|状态："+player.getState());
        	if(!player.isRobot()){
                if(chongfen>0){
                    if (!(player.getState() == player_state.ready||player.getState() == player_state.play))
                        return false;
                }else{
                	if(player.getState() != player_state.ready)
                		return false;	
                }
                   
            }
        }
//        LogUtil.msgLog.info("xx2710|"+getId()+"|"+getPlayBureau()+"|finishiFapai状态："+finishiFapai);
        if(finishiFapai==1)
            return false;
        changeTableState(table_state.play);
        if (chongfen > 0 ) {
            boolean piaoFenOver = true;
            for (Xx2710Player player : playerMap.values()) {
                if(player.getPiaoFen()==-1){
                    piaoFenOver = false;
                    break;
                }
            }
            if(!piaoFenOver){
                if (isSendPiaoFenMsg==0 && finishiFapai==0) {
                    LogUtil.msgLog.info("xx2710|sendPiaoFen|" + getId() + "|" + getPlayBureau());
                    ComRes msg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_zzzp_piaofen, 0).build();
                    for (Xx2710Player player : playerMap.values()) {
                        if(!player.isAlreadyPiaoFen())
                            player.writeSocket(msg);
                    }
                    isSendPiaoFenMsg = 1;
                }
                return false;
            }
        }
        return true;
    }
    public synchronized void piaoFen(Xx2710Player player,int fen){
        if (chongfen==0||player.isAlreadyPiaoFen())
            return;
//        if(fen<=5&&fen>=0)
            player.setPiaoFen(fen);
        player.setAlreadyPiaoFen(true);
        confirmTime++;
        StringBuilder sb = new StringBuilder("XX2710");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append("piaoFen").append("|").append(fen);
        LogUtil.msgLog.info(sb.toString());
        for (Entry<Integer, Xx2710Player> entry : seatMap.entrySet()) {
			// 传code,座位号+已打鸟
            entry.getValue().writeComMessage(WebSocketMsgType.res_code_zzzp_broadcast_piaofen, (int) player.getUserId(),player.getPiaoFen());
        }
        if (confirmTime == playerCount) {
            checkDeal(player.getUserId());
			// 检查起手牌是否需要自动操作
            startNext();
        }
    }
}
