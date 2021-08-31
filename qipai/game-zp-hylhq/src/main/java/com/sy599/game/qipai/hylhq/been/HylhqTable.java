package com.sy599.game.qipai.hylhq.been;

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

import com.sy599.game.common.bean.CreateTableInfo;
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
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayPaohuziRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzInfoRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.hylhq.constant.PaohuziConstant;
import com.sy599.game.qipai.hylhq.constant.PaohzCard;
import com.sy599.game.qipai.hylhq.rule.PaohuziIndex;
import com.sy599.game.qipai.hylhq.rule.PaohuziMingTangRule;
import com.sy599.game.qipai.hylhq.rule.PaohzCardIndexArr;
import com.sy599.game.qipai.hylhq.rule.RobotAI;
import com.sy599.game.qipai.hylhq.tool.PaohuziHuLack;
import com.sy599.game.qipai.hylhq.tool.PaohuziResTool;
import com.sy599.game.qipai.hylhq.tool.PaohuziTool;
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
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class HylhqTable extends BaseTable {
    /*** 玩家map */
    private Map<Long, HylhqPlayer> playerMap = new ConcurrentHashMap<>();
    /*** 座位对应的玩家 */
    private Map<Integer, HylhqPlayer> seatMap = new ConcurrentHashMap<>();
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

    //最后操作的一张牌
    private int finalCard=0;
    //是否已经完成第一次发牌
    private int finishiFapai=0;
    //是否已经发送飘分信息
    private int isSendPiaoFenMsg=0;
    private volatile int timeNum = 0;
    //起手提龙标记
    private boolean qiShouTi=false;


//    private volatile int lockHandNum=0;


    //需要修改，用disNum+moNum代替
    private volatile int moNum=0;
    private volatile int disNum=0;
    //用来记录必胡的人的座位
    private int biHuSeat=0;
    //用该状态记录该回合是摸排还是出牌（吃碰后的出牌），1：出牌，2：摸牌，0：尚未开始 3吃碰出牌
    private volatile int disOrMo=0;



    //起胡息数量
    private int floorValue=6;
    //是否勾选首局庄家随机
    private int bankerRand=0;
    //是否加倍：0否，1是
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
    //醒：0 不带醒 ， 1跟醒 ， 2翻醒
    private int xingType=0;
    //一息一屯
    private int yiXiYiTun=0;
    //1,5,10一句话
    private int val1_5_10=0;
    //明偎
    private int mingWei=0;
    //红黑点
    private int hongHei=0;
    //天湖
    private int tianHu=0;
    //地湖
    private int diHu=0;
    //21张(4人时无效)
    private int card21=0;
    //自摸翻倍
    private int ziMo=0;
    //放炮包赔
    private int fangPaoBaoPei=0;
    //放炮必胡
    private int fangPaoBiHu=0;
    //有胡必胡
    private int youHuBiHu=0;

    //醒牌
    private int xingCard=0;
    //底分2分
    private int diFen2=0;
    //低于below加分
    private int belowAdd=0;
    private int below=0;
    //跑胡
    private int paoHu=0;
    //红黑2番
    private int hongHei2Fan=0;



    /**
     * 玩家位置对应临时操作
     * 当同时存在多个可做的操作时
     * 1当优先级最高的玩家最先操作 则清理所有操作提示 并执行该操作
     * 2当操作优先级低的玩家先选择操作，则玩家先将所做操作存入临时操作中 当玩家优先级最高的玩家操作后 在判断临时操作是否可执行并执行
     */
    private Map<Integer, TempAction> tempActionMap = new ConcurrentHashMap<>();


    public boolean isAllReady() {
        log(2);
        if (state == table_state.play) {
            return false;
        }
        log(3);
        if (getPlayerCount() < getMaxPlayerCount()) {
            return false;
        }
        log(4);
        for (Player player : getSeatMap().values()) {
            if (!player.isRobot() && player.getState() != player_state.ready) {
                return false;
            }
        }
        log(5);
        return true;
    }

    public void log(int num){
        if(true){
            // 2020-04-30关闭日志
            return;
        }
        StringBuilder sb=new StringBuilder("noHandCards");
        sb.append("|").append(getId());
        sb.append("|").append(playBureau);
        sb.append("|").append(num);
        LogUtil.msgLog.info(sb.toString());
    }

    @Override
    public boolean createTable(CreateTableInfo createTableInfo) throws Exception {
        Player player = createTableInfo.getPlayer();
        int play = createTableInfo.getPlayType();
        int bureauCount = createTableInfo.getBureauCount();
        int tableType = createTableInfo.getTableType();
        List<Integer> params = createTableInfo.getIntParams();
        List<String> strParams = createTableInfo.getStrParams();
        boolean saveDb = createTableInfo.isSaveDb();
        long id = getCreateTableId(player.getUserId(), play);
        if (id <= 0) {
            return false;
        }
        if(saveDb){
            TableInf info = new TableInf();
            info.setTableType(tableType);
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

        playerCount = StringUtil.getIntValue(params, 7, 3);// 比赛人数
        payType = StringUtil.getIntValue(params, 9, 0);// 房费方式
        xingType=StringUtil.getIntValue(params, 10, 0);//醒
        yiXiYiTun=StringUtil.getIntValue(params, 11, 0);//一息一屯
        if(yiXiYiTun==1)
            hongHei2Fan = StringUtil.getIntValue(params, 2, 0);// 红黑2番
        val1_5_10=StringUtil.getIntValue(params, 12, 0);//1,5,10
        mingWei=StringUtil.getIntValue(params, 13, 0);//明偎
        hongHei=StringUtil.getIntValue(params, 14, 0);//红黑点
        tianHu=StringUtil.getIntValue(params, 15, 0);//天胡
        diHu=StringUtil.getIntValue(params, 16, 0);//地胡
        ziMo=StringUtil.getIntValue(params, 17, 0);//自摸翻倍
        fangPaoBaoPei=StringUtil.getIntValue(params, 18, 0);  //放炮包赔
        fangPaoBiHu=StringUtil.getIntValue(params, 19, 0);//放炮必胡
        card21=StringUtil.getIntValue(params, 20, 0);//21张
        floorValue=StringUtil.getIntValue(params, 21, 0);//起胡胡息
        bankerRand=StringUtil.getIntValue(params, 22, 0);//随机坐庄
        if(playerCount==2){
            chouCardNum=StringUtil.getIntValue(params, 23, 0);
        }else {
            chouCardNum=0;
        }
        int time=StringUtil.getIntValue(params, 24, 0);
        if(time ==1) {
            time =60;
        }
        if(time>0) {
            this.autoPlay =true;
        }
        autoPlayGlob=StringUtil.getIntValue(params, 25, 0);
        jiaBei = StringUtil.getIntValue(params, 26, 0);
        jiaBeiFen = StringUtil.getIntValue(params, 27, 0);
        jiaBeiShu = StringUtil.getIntValue(params, 28, 0);
        youHuBiHu = StringUtil.getIntValue(params, 29, 0);
        diFen2 = StringUtil.getIntValue(params, 30, 0);
        if(playerCount==2){
            int belowAdd = StringUtil.getIntValue(params, 31, 0);
            if(belowAdd<=100&&belowAdd>=0)
                this.belowAdd=belowAdd;
            int below = StringUtil.getIntValue(params, 32, 0);
            if(below<=100&&below>=0){
                this.below=below;
                if(belowAdd>0&&below==0)
                    this.below=10;
            }
        }
        if (playerCount<=1||playerCount>4){
            return false;
        }
        setPlayerCount(playerCount);
        if(this.getMaxPlayerCount() != 2){
            jiaBei = 0 ;
        }
        setPayType(payType);
        if(autoPlay){
            autoTimeOut2=autoTimeOut = time*1000;
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
        wrapper.putInt(13, jiaBei);
        wrapper.putInt(14, jiaBeiFen);
        wrapper.putInt(15, jiaBeiShu);
        wrapper.putInt(16, bankerRand);
        wrapper.putInt(17, floorValue);
        wrapper.putInt(18, disNum);
        wrapper.putInt(19, moNum);
        wrapper.putInt(20, disOrMo);
        wrapper.putInt(21, autoPlayGlob);
        wrapper.putInt(22, autoTimeOut);
        wrapper.putInt(23, biHuSeat);
        wrapper.putInt(24, finalCard);
        wrapper.putInt(25, isSendPiaoFenMsg);
        wrapper.putInt(26, finishiFapai);
        JSONArray tempJsonArray = new JSONArray();
        for (int seat : tempActionMap.keySet()) {
            tempJsonArray.add(tempActionMap.get(seat).buildData());
        }
        wrapper.putString(27, tempJsonArray.toString());
        wrapper.putInt(28, chouCardNum);
        wrapper.putInt(29, xingType);
        wrapper.putInt(30, yiXiYiTun);
        wrapper.putInt(31, val1_5_10);
        wrapper.putInt(32, mingWei);
        wrapper.putInt(33, hongHei);
        wrapper.putInt(34, tianHu);
        wrapper.putInt(35, diHu);
        wrapper.putInt(36, card21);
        wrapper.putInt(37, ziMo);
        wrapper.putInt(38, fangPaoBaoPei);
        wrapper.putInt(39, fangPaoBiHu);
        wrapper.putInt(40, xingCard);
        wrapper.putInt(41, youHuBiHu);
        wrapper.putInt(42, diFen2);
        wrapper.putInt(43, belowAdd);
        wrapper.putInt(44, below);
        wrapper.putInt(45, paoHu);
        wrapper.putInt(46, hongHei2Fan);


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
        jiaBei = wrapper.getInt(13, 0);
        jiaBeiFen = wrapper.getInt(14, 0);
        jiaBeiShu = wrapper.getInt(15, 0);
        bankerRand=wrapper.getInt(16, 0);
        floorValue = wrapper.getInt(17, 0);
        disNum=wrapper.getInt(18,0);
        moNum=wrapper.getInt(19,0);
        disOrMo=wrapper.getInt(20,0);
        autoPlayGlob = wrapper.getInt(21,0);
        autoTimeOut = wrapper.getInt(22,0);
        biHuSeat = wrapper.getInt(23,0);
        finalCard=wrapper.getInt(24,0);
        isSendPiaoFenMsg=wrapper.getInt(25,0);
        finishiFapai=wrapper.getInt(26,0);
        if(autoPlay&&autoTimeOut<=1) {
            autoTimeOut2=autoTimeOut = 60000;
        }
        tempActionMap = loadTempActionMap(wrapper.getString(27));
        chouCardNum=wrapper.getInt(28,0);
        xingType=wrapper.getInt(29,0);
        yiXiYiTun=wrapper.getInt(30,0);
        val1_5_10=wrapper.getInt(31,0);
        mingWei=wrapper.getInt(32,0);
        hongHei=wrapper.getInt(33,0);
        tianHu=wrapper.getInt(34,0);
        diHu=wrapper.getInt(35,0);
        card21=wrapper.getInt(36,0);
        ziMo=wrapper.getInt(37,0);
        fangPaoBaoPei=wrapper.getInt(38,0);
        fangPaoBiHu=wrapper.getInt(39,0);
        xingCard=wrapper.getInt(40,0);
        youHuBiHu=wrapper.getInt(41,0);
        diFen2=wrapper.getInt(42,0);
        belowAdd=wrapper.getInt(43,0);
        below=wrapper.getInt(44,0);
        if(belowAdd>0&&below==0)
            below=10;
        paoHu=wrapper.getInt(45,0);
        hongHei2Fan=wrapper.getInt(46,0);
    }

    public int getHongHei2Fan() {
        return hongHei2Fan;
    }

    public int getDisNum() {
        return disNum;
    }

    public int getMoNum() {
        return moNum;
    }


    public int getYiXiYiTun() {
        return yiXiYiTun;
    }

    public int getVal1_5_10() {
        return val1_5_10;
    }

    public int getMingWei() {
        return mingWei;
    }

    public int getHongHei() {
        return hongHei;
    }

    public int getTianHu() {
        return tianHu;
    }

    public int getDiHu() {
        return diHu;
    }

    public int getPaoHu() {
        return paoHu;
    }

    public void setPaoHu(int paoHu) {
        this.paoHu = paoHu;
    }

    public int getCard21() {
        return card21;
    }

    public int getZiMo() {
        return ziMo;
    }

    public int getFangPaoBaoPei() {
        return fangPaoBaoPei;
    }

    public int getFangPaoBiHu() {
        return fangPaoBiHu;
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
        int tun = 0;
        int winTun=0;
        int lostTun=0;
        int xingTun=0;
        boolean isOver = false;
        if(!isHuangZhuang){
            for (int winSeat : winList) {
                // 赢的玩家
                HylhqPlayer winPlayer = seatMap.get(winSeat);
                winPlayer.changeAction(PaohuziConstant.ACTION_COUNT_INDEX_HU, 1);
                mt = winPlayer.getHu().getMingTang();
                int huxi=winPlayer.getTotalHu();
                tun=countXiTun(huxi);
                xingTun=xingTun(winPlayer,xingType);
                tun+=xingTun;
                lostTun=PaohuziMingTangRule.countXiTun(tun,mt,hongHei2Fan==1);
                if(disOrMo==1){
                    //放炮
                    if(fangPaoBaoPei==1){
                        winTun=lostTun=(playerCount-1)*lostTun;
                    }else {
                        winTun=lostTun;
                    }
                    HylhqPlayer player = seatMap.get(disCardSeat);
                    player.calcResult(this, 1, -lostTun, isHuangZhuang);
                }else if(isMoFlag()||(disOrMo==0&&moNum==0)){
                    //摸胡
                    if(moSeat==winSeat){
                        winPlayer.changeAction(PaohuziConstant.ACTION_COUNT_INDEX_ZIMO, 1);
                    }
                    for (int seat : seatMap.keySet()){
                        if (winSeat!=seat) {
                            HylhqPlayer player = seatMap.get(seat);
                            winTun+=lostTun;
                            player.calcResult(this, 1, -lostTun, isHuangZhuang);

                        }
                    }
                }
                winPlayer.calcResult(this, 1, winTun, isHuangZhuang);
                setLastWinSeat(winSeat);
            }
        }
        if(playBureau == totalBureau){
            isOver=true;
            changeTableState(table_state.over);
        }
        if(autoPlayGlob >0) {
//          //是否解散
            boolean diss = false;
            if(autoPlayGlob ==1) {
                for (HylhqPlayer seat : seatMap.values()) {
                    if(seat.isAutoPlay()) {
                        diss = true;
                        break;
                    }
                }
            }else  if(autoPlayGlob ==3) {
           	 diss = checkAuto3();
            }
            if(diss) {
            	 autoPlayDiss= true;
            	 isOver =true;
            }
        }
        
        if(isHuangZhuang)
            setLastWinSeat(calcNextSeat(lastWinSeat));

        // -----------金币场---------------------------------
        if (isGoldRoom()) {
            for (HylhqPlayer player : seatMap.values()) {
                player.setPoint(player.getTotalPoint());
                player.setWinGold(player.getTotalPoint());
            }
            calcGoldRoom();
        }
        calcAfter();
        ClosingPhzInfoRes.Builder res = sendAccountsMsg(isOver, winList, 0, mt, tun, false, null,null,xingTun);
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
//		if(autoPlayGlob==3) {
			boolean diss2 = false;
			 for (HylhqPlayer seat : seatMap.values()) {
		      	if(seat.isAutoPlay()) {
		      		diss2 = true;
		          	break;
		          }
		      }
			 if(diss2) {
				 autoTableCount +=1;
			 }else{
				 autoTableCount = 0;
			 }
			if(autoTableCount==3) {
				diss = true;
			}
//		}
		return diss;
	}
    

    private int countXiTun(int huxi){
        if(huxi<0)
            return 0;
        if(yiXiYiTun==1)
            return huxi;
        huxi=huxi-floorValue;
        int tun=huxi/3+1;
        if(diFen2==1)
            tun++;
        return tun;
    }

    /**
     * 醒牌加屯
     * @param winPlayer
     * @return
     */
    private int xingTun(HylhqPlayer winPlayer, int xingType){
        Set<Integer> sameVal=new HashSet<>();
        PaohzCard xingCard=null;
        switch (xingType){
            case 0:
                return 0;
            case 1:
                xingCard = PaohzCard.getPaohzCard(finalCard);
                break;
            case 2:
                if(leftCards.size()==0){
                    //海底捞
                    xingCard = PaohzCard.getPaohzCard(finalCard);
                }else {
                    xingCard = getNextCard();
                }
                break;
        }
        if(xingCard==null)
            return 0;
        this.xingCard=xingCard.getId();
        for (CardTypeHuxi type:winPlayer.getCardTypes()){
            for(Integer id:type.getCardIds()){
                if(PaohzCard.getPaohzCard(id).getVal()==xingCard.getVal())
                    sameVal.add(id);
            }
        }
        List<PaohzCard> handCards = PaohuziTool.toPhzCards(winPlayer.getHandPais());
        for (PaohzCard card:handCards) {
            if(card.getVal()==xingCard.getVal())
                sameVal.add(card.getId());
        }
        if(PaohzCard.getPaohzCard(finalCard).getVal()==xingCard.getVal())
            sameVal.add(finalCard);
        if(xingType==1||(xingType==2&&leftCards.size()==0))
            sameVal.add(xingCard.getId());
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
            for (HylhqPlayer player : playerMap.values()) {
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
        HylhqPlayer winPlayer = seatMap.get(lastWinSeat);

        for (HylhqPlayer tablePlayer : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            res.addAllHandCardIds(tablePlayer.getHandPais());
            res.setNextSeat(lastWinSeat);
            res.setGameType(getWanFa());// 1跑得快 2麻将
            res.setRemain(leftCards.size());
            res.setBanker(lastWinSeat);
            res.addXiaohu(winPlayer.getHandPais().get(0));
            tablePlayer.writeSocket(res.build());
        }
        finalCard=winPlayer.getHandPais().get(0);
    }

    @Override
    public synchronized void startNext() {
        if(finishiFapai==1)
            checkAction();
    }

    public void play(HylhqPlayer player, List<Integer> cardIds, int action) {
        play(player, cardIds, action, false, false, false);
    }

    private void hu(HylhqPlayer player, List<PaohzCard> cardList, int action, PaohzCard nowDisCard,boolean system) {
        if(player==null)
            return;
        if (!system){//系统调用，跳过合法检测
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

        List<PaohuziHuLack> pao = player.checkPaoHu1(nowDisCard, isSelfMo(player), true);
        if(player.isZaiTiHu()){
            nowDisCard=null;
        }
        List<PaohuziHuLack> ping = player.checkHu1(nowDisCard, isSelfMo(player));
        PaohuziHuLack hu = filtrateHu(player, pao, ping);
        if(hu!=null&&!player.isSiShou()){
//            if (hu.getPaohuAction()==PaohzDisAction.action_ti){
//                ti(player, hu.getPaohuList(),nowDisCardIds.get(0),PaohzDisAction.action_ti,false);
//                List<PaohuziHuLack> list = player.checkHu1(null, isSelfMo(player));
//                hu = filtrateHu(player, null, list);
//            }else
            if(hu.getPaohuAction()==PaohzDisAction.action_pao){
                pao(player,hu.getPaohuList(),nowDisCardIds.get(0),PaohzDisAction.action_pao,true,false,true);
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

    public PaohuziHuLack filtrateHu(HylhqPlayer player,List<PaohuziHuLack> pao,List<PaohuziHuLack> ping){
        List<PaohuziHuLack> zaiTi=new ArrayList<>();
        List<PaohuziHuLack> hu=new ArrayList<>();
        if (pao!=null&&!pao.isEmpty()){
            for (PaohuziHuLack lack:pao){
                //跑胡不可能胡息为0（飘胡）
                int allHuxi=lack.getHuxi() + player.getOutHuxi() +player.getZaiHuxi();
                if (allHuxi < getFloorValue())
                    continue;
                //栽胡和提胡优先级高于其他胡行
                if(lack.getPaohuAction()==PaohzDisAction.action_ti&&player.isZaiTiHu()){
                    zaiTi.add(lack);
                    continue;
                }
                hu.add(lack);
            }
            //多个栽提胡牌型取最大胡息
            if(zaiTi.size()>=1){
                return getMaxHu(player,zaiTi);
            }
        }
        if (ping!=null&&!ping.isEmpty()){
            for (PaohuziHuLack lack:ping){
                int allHuxi=lack.getHuxi() + player.getOutHuxi() +player.getZaiHuxi();
                if (allHuxi >= getFloorValue())
                    hu.add(lack);
            }
        }
        if(hu.size()>=1){
            return getMaxHu(player,hu);
        }
        return null;
    }

    public PaohuziHuLack getMaxHu(HylhqPlayer player,List<PaohuziHuLack> hu){
        int maxpoint=-1;
        PaohuziHuLack lack1=null;
        for (PaohuziHuLack lack:hu){
            List<Integer> arr = PaohuziMingTangRule.calcMingTang(player, lack,this);
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
    public boolean isSelfMo(HylhqPlayer player) {
        if (moSeatPair != null) {
            return moSeatPair.getValue().intValue() == player.getSeat();
        }
        return false;
    }

    /**
     * 提
     */
    private void ti(HylhqPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action, boolean moPai) {
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
        if(checkCard.isHu()&&youHuBiHu==1){
            hu(player,null,PaohzDisAction.action_hu,null,true);
        }
    }

    /**
     * 栽(臭栽)
     *
     * @param cardList 要栽的牌
     */
    private void zai(HylhqPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
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
        if(checkCard.isHu()&&youHuBiHu==1){
            hu(player,null,PaohzDisAction.action_hu,null,true);
        }
        if (!disCard) {
            // checkMo();
        }

    }

    /**
     * 跑
     */
    private void pao(HylhqPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action, boolean isHu, boolean isPassHu ,boolean system) {

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
    private void disCard(HylhqPlayer player, List<PaohzCard> cardList, int action) {
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
            setFinalCard(cardList.get(0).getId());
        }
        player.setZaiTiHu(false);
        // 判断是否为放招
        boolean paoFlag = isFangZhao(player, cardList.get(0));
        if (mingWei==1&&paoFlag) {
            if(player.isAutoPlay()){//托管自动放招
                player.setFangZhao(1);
                for (Player playerTemp : getSeatMap().values()) {
                    playerTemp.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao, player.getUserId() + "", 1 + "");
                }
            }else if (!player.isFangZhao() && !player.isRobot()) {
                player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, cardList.get(0).getId());
                LogUtil.msgLog.info("----tableId:" + getId() + "---userName:" + player.getName() + "------是否确定放招:--->>>>>>" + cardList.get(0));
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
            HylhqPlayer p = seatMap.get(biHuSeat);
            if(p==null){
                StringBuilder sb=new StringBuilder("hylhq.bihu");
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

    private void tiLong(HylhqPlayer player) {
        boolean isTiLong = false;
        List<PaohzCard> cardList = new ArrayList<>();
        while (player.getOweCardCount() < -1) {
            if (!isTiLong) {
                isTiLong = true;
                removeAction(player.getSeat());
            }
            PaohzCard card = null;
            if (card == null) {
                card = getNextCard();
            }
            player.tiLong(card);
            cardList.add(card);

            addPlayLog(player.getSeat(), PaohzDisAction.action_buPai + "", (card == null ? 0 : card.getId()) + "");
            StringBuilder sb = new StringBuilder("hylhq.tiLong");
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

    public void checkFreePlayerTi(HylhqPlayer player, int action) {
        if (player.getSeat() == lastWinSeat && player.isFristDisCard() && action != PaohzDisAction.action_ti) {
            for (int seat : getSeatMap().keySet()) {
                if (lastWinSeat == seat) {
                    continue;
                }
                HylhqPlayer nowPlayer = seatMap.get(seat);
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
    private void peng(HylhqPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
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
            for (HylhqPlayer seatPlayer : seatMap.values()) {
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
    private void pass(HylhqPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
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
        if (actionList.get(0) == 1 && (player.getHandPhzs().isEmpty()||player.getOperateCards().size()==0)) {
            player.writeErrMsg("手上已经没有牌可出了,不能过胡");
            return;
        }
        // 可以胡牌，然后点了过
        boolean isPassHu = actionList.get(0) == 1;
        if (youHuBiHu==1&&isPassHu) {
            player.writeErrMsg("有胡必胡,不能过胡");
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
        player.setZaiTiHu(false);
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
            //应该是在此处告诉前端，已执行pass操作
            sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action);
            if (check) {
                playAutoDisCard(checkCard, true);
            } else {
                if (PaohuziConstant.isAutoMo) {
                    //在此处查询可以摸排的玩家并通知其摸排
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
    private void chi(HylhqPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
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

        cardList = player.getChiList(nowDisCard, cardList,getVal1_5_10()==1);
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
        //如果选择吃，则下家不算过吃
        if(disOrMo==2&&getMoSeat()==player.getSeat()){
            HylhqPlayer nextP = seatMap.get(calcNextSeat(player.getSeat()));
            nextP.removeLastPassChi(nowDisCard.getVal());
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

    public synchronized void play(HylhqPlayer player, List<Integer> cardIds, int action, boolean moPai, boolean isHu, boolean isPassHu) {
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
            StringBuilder sb = new StringBuilder("hylhq");
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
    public synchronized void lockHand(HylhqPlayer player) {
    }

//    public synchronized void piaoFen(HylhqPlayer player,int fen){
//        if (piaoFen==1||player.isAlreadyPiaoFen())
//            return;
//        if(fen<=5&&fen>=0)
//            player.setPiaoFen(fen);
//        player.setAlreadyPiaoFen(true);
//        confirmTime++;
//        StringBuilder sb = new StringBuilder("hylhq");
//        sb.append("|").append(getId());
//        sb.append("|").append(getPlayBureau());
//        sb.append("|").append(player.getUserId());
//        sb.append("|").append(player.getSeat());
//        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
//        sb.append("|").append("piaoFen").append("|").append(fen);
//        LogUtil.msgLog.info(sb.toString());
//        for (Entry<Integer, HylhqPlayer> entry : seatMap.entrySet()) {
//            //传code,座位号+已打鸟
//            entry.getValue().writeComMessage(WebSocketMsgType.res_code_hylhq_broadcast_piaofen, (int) player.getUserId(),player.getPiaoFen());
//        }
//        if (confirmTime == playerCount) {
//            checkDeal(player.getUserId());
//            //检查起手牌是否需要自动操作
//            startNext();
//        }
//    }











    private boolean setDisPlayer(HylhqPlayer player, int action, boolean isHu) {
        return setDisPlayer(player, action, false, isHu);
    }

    /**
     * 设置要出牌的玩家
     */
    private boolean setDisPlayer(HylhqPlayer player, int action, boolean isFirstDis, boolean isHu) {
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
            if(!isHu)
                player.setSiShou(true);
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
    private boolean checkActionOld(HylhqPlayer player, int action) {
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
    private boolean checkAction(HylhqPlayer player, int action, List<PaohzCard> cardList, PaohzCard nowDisCard) {
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
            HylhqPlayer tempPlayer = seatMap.get(maxSeat);
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
    private void refreshTempAction(HylhqPlayer player) {
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
                HylhqPlayer tempPlayer = seatMap.get(tempAction.getSeat());
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
    private HylhqPlayer getDisPlayer() {
        return seatMap.get(disCardSeat);
    }

    private void record(HylhqPlayer player, int action, List<PaohzCard> cardList) {
    }

    @Override
    public int isCanPlay() {
        if (getPlayerCount() < getMaxPlayerCount()) {
            return 1;
        }
        // for (HylhqPlayer player : seatMap.values()) {
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
        HylhqPlayer player = seatMap.get(nowDisCardSeat);

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
        PaohzCard card;
        if (player.getFlatId().startsWith("vkscz2855914")) {
            card = getNextCard(102);
            if (card == null) {
                card = getNextCard();
            }
        } else {
            card = getNextCard();
        }

        addPlayLog(player.getSeat(), PaohzDisAction.action_mo + "", (card == null ? 0 : card.getId()) + "");
        StringBuilder sb = new StringBuilder("hylhq");
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
            setFinalCard(card.getId());
            player.moCard(card);
            setDisCardSeat(player.getSeat());
            setNowDisCardIds(new ArrayList<>(Arrays.asList(card)));
            setNowDisCardSeat(getNextDisCardSeat());
            PaohuziCheckCardBean autoDisCard = null;
            int nowSeat=player.getSeat();
            int i=1;
            while (true){
                HylhqPlayer p = seatMap.get(nowSeat);
                PaohuziCheckCardBean checkCard = p.checkCard(card, p.getSeat() == player.getSeat(), false);
                if(!checkCard.isZai()&&!checkCard.isTi()&!checkCard.isChouZai())
                    p.setZaiTiHu(false);
                if (checkPaohuziCheckCard(checkCard)) {
                    autoDisCard = checkCard;
                }
                if(checkCard.isTi()||checkCard.isZai()||checkCard.isChouZai()){
                    break;
                }
                //判断是否强制胡牌
                if (checkCard.isHu()&&youHuBiHu==1&&biHuSeat==0){
                    biHuSeat=nowSeat;
                    break;
                }
                nowSeat = calcNextSeat(nowSeat);
                if(i>=playerCount)
                    break;
                i++;
            }
            markMoSeat(player.getSeat(), PaohzDisAction.action_mo);
            if (autoDisCard != null && (autoDisCard.getAutoAction() == PaohzDisAction.action_zai||autoDisCard.getAutoAction() == PaohzDisAction.action_ti)) {
                sendMoMsg(player, PaohzDisAction.action_mo, new ArrayList<>(Arrays.asList(card)), PaohzDisAction.action_type_mo);
            } else {
                sendActionMsg(player, PaohzDisAction.action_mo, new ArrayList<>(Arrays.asList(card)), PaohzDisAction.action_type_mo);
            }

            if (autoDisBean != null) {
                playAutoDisCard(autoDisBean);
            }

            if(biHuSeat!=0){
                sb=new StringBuilder("hylhq.bihu");
                sb.append("|").append(getId());
                sb.append("|").append(getPlayBureau());
                sb.append("|").append(biHuSeat);
                sb.append("|").append(player.isAutoPlay() ? 1 : 0);
                sb.append("|").append(seatMap==null?null:seatMap);
                LogUtil.msgLog.info(sb.toString());
                hu(seatMap.get(biHuSeat),null,PaohzDisAction.action_hu,card,true);
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
    private PaohuziCheckCardBean checkDisAction(HylhqPlayer player, int action, PaohzCard disCard, boolean canBoom) {
        PaohuziCheckCardBean autoDisCheck = new PaohuziCheckCardBean();
        int nowSeat=disCardSeat;
        int i=1;
        while (true){
            nowSeat = calcNextSeat(nowSeat);
            i++;
            if(i>getMaxPlayerCount())
                return autoDisCheck;
            HylhqPlayer p = seatMap.get(nowSeat);
            if (p.getUserId()==player.getUserId())
                continue;
            PaohuziCheckCardBean checkCard = p.checkCard(disCard,false,!canBoom,false,canBoom,false);
            boolean check = checkPaohuziCheckCard(checkCard);
            if (check) {
                autoDisCheck = checkCard;
            }
            //判断是否强制胡牌
            if (checkCard.isHu()&&(fangPaoBiHu==1||youHuBiHu==1)){
                biHuSeat=nowSeat;
                break;
            }
            if(nowSeat==disCardSeat||i>=playerCount)
                break;
        }
        return autoDisCheck;
    }

    private boolean isFangZhao(HylhqPlayer player, PaohzCard disCard) {

        for (Entry<Integer, HylhqPlayer> entry : seatMap.entrySet()) {
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
    private PaohuziCheckCardBean checkAutoDis(HylhqPlayer player, boolean isMoPaiIng) {
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


    private void sendActionMsg(HylhqPlayer player, int action, List<PaohzCard> cards, int actType) {
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
    private void sendMoMsg(HylhqPlayer player, int action, List<PaohzCard> cards, int actType) {
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(action);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
        setNextSeatMsg(builder);
        builder.setRemain(leftCards.size());
        builder.setActType(actType);
        KeyValuePair<Boolean, Integer> zaiKeyValue = getZaiOrTiKeyValue();
        for (HylhqPlayer p : seatMap.values()) {
            PlayPaohuziRes.Builder copy = builder.clone();
            if (p.getSeat() != player.getSeat()) {
            } else {
                copy.setHuxi(p.getOutHuxi() + p.getZaiHuxi());
            }
            if(mingWei==0&&p.getSeat()!=player.getSeat())
                copy.addPhzIds(0);
            else {
                copy.addAllPhzIds(PaohuziTool.toPhzCardIds(cards));
            }
            if (actionSeatMap.containsKey(p.getSeat())) {
                List<Integer> actionList = getSendSelfAction(zaiKeyValue, p.getSeat(), actionSeatMap.get(p.getSeat()));
                if (actionList != null) {
                    copy.addAllSelfAct(actionList);
                }
            }
            p.writeSocket(copy.build());
        }
    }

    /**
     * 发送该玩家动作msg
     */
    private void sendPlayerActionMsg(HylhqPlayer player) {
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
        if (toPlayCardFlag == 1) {
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
    private void sendActionMsg(HylhqPlayer player, int action, List<PaohzCard> cards, int actType, boolean isZaiPao, boolean isChongPao) {
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
    private void sendMsgBySelfAction(PlayPaohuziRes.Builder builder) {
        KeyValuePair<Boolean, Integer> zaiKeyValue = getZaiOrTiKeyValue();

        int actType = builder.getActType();
        boolean noShow = false;
        int paoSeat = 0;
        if (PaohzDisAction.action_type_dis == actType || PaohzDisAction.action_type_mo == actType) {
            for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                if (1 == entry.getValue().get(5)) {
                    noShow = true;
                    paoSeat = entry.getKey();
                }
            }
        }

        HylhqPlayer winPlayer = seatMap.get(lastWinSeat);

        for (HylhqPlayer player : seatMap.values()) {
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
                StringBuilder sb = new StringBuilder("hylhq");
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
        HylhqPlayer disPlayer = seatMap.get(disCardSeat);
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
            HylhqPlayer seatPlayer = seatMap.get(entry.getKey());
            seatPlayer.writeSocket(copy.build());
        }

    }

    public synchronized void checkAction() {
        int nowSeat = getNowDisCardSeat();
        // 先判断拿牌的玩家
        HylhqPlayer nowPlayer = seatMap.get(nowSeat);
        if (nowPlayer == null) {
            return;
        }
        //获取可以做的所有操作
        PaohuziCheckCardBean checkCard = nowPlayer.checkCard(null, true, true, false);
        if (checkPaohuziCheckCard(checkCard)) {
            playAutoDisCard(checkCard);
            tiLong(nowPlayer);
        }
        checkSendActionMsg();
        if(checkCard.isHu()&&youHuBiHu==1){
            hu(seatMap.get(nowSeat),null,PaohzDisAction.action_hu,null,true);
        }
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
     * @param moPai 是否是摸牌 如果是摸牌，需要
     */
    private void playAutoDisCard(PaohuziCheckCardBean checkCard, boolean moPai) {
        if (checkCard.getActionList() != null) {
            int seat = checkCard.getSeat();
            HylhqPlayer player = seatMap.get(seat);
            if (player.isRobot()) {
                sleep();
            }
            //System.out.println(player.getName() + "自动出牌------------check:" + checkCard.getAutoAction() + " " + checkCard.getAutoDisList());
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
                HylhqPlayer player = seatMap.get(nextseat);
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
                    HylhqPlayer player = seatMap.get(key);
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
        finishiFapai=0;
        isSendPiaoFenMsg=0;
        finalCard=0;
        timeNum=0;
        clearTempAction();
        xingCard=0;
        setPaoHu(0);
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
            for (HylhqPlayer player : playerMap.values()) {
                player.setLastOperateTime(System.currentTimeMillis());
            }
        }
        if(getPlayBureau()==1 && isGroupRoom()){
            if(bankerRand==1){
                setLastWinSeat(new Random().nextInt(getMaxPlayerCount())+1);
            }else {
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

        List<List<PaohzCard>> list=null;
        if(card21==1){
            list = PaohuziTool.fapai(copy, zp ,20,playerCount,val1_5_10==1);
        }else {
            list = PaohuziTool.fapai(copy, zp ,14,playerCount,val1_5_10==1);
        }


        int seat=lastWinSeat;

        for (int i = 0; i < playerCount; i++) {
            HylhqPlayer player = seatMap.get(seat);
            player.changeState(player_state.play);
            player.getFirstPais().clear();
            player.dealHandPais(list.get(i));
            player.getFirstPais().addAll(PaohuziTool.toPhzCardIds(new ArrayList(list.get(i))));
            seat=calcNextSeat(seat);
            if (!player.isAutoPlay()) {
                player.setAutoPlay(false, this);
                player.setLastOperateTime(System.currentTimeMillis());
            }
            StringBuilder sb = new StringBuilder("hylhq");
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

            StringBuilder sb = new StringBuilder("hylhq");
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
        for (HylhqPlayer player : playerMap.values()) {
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
        res.addExt(floorValue);// 5
        res.addExt(modeId.length()>0?Integer.parseInt(modeId):0);//6
        int ratio;
        int pay;
        if (isGoldRoom()){
            ratio = 0;
            pay = 0;
        }else{
            ratio = 1;
            pay = consumeCards()?loadPayConfig(payType):0;
        }
        res.addExt(ratio);// 7
        res.addExt(pay);// 8
        res.addExt(0);// 9
        res.addExt(creditMode);     // 10
//        res.addExt(creditJoinLimit);// 11门清
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



    public ClosingPhzInfoRes.Builder sendAccountsMsg(boolean over, List<Integer> winList, int winFen, List<Integer> mt, int totalTun, boolean isBreak,Map<Long,Integer> outScoreMap,Map<Long,Integer> ticketMap,int xingTun) {

        List<ClosingPhzPlayerInfoRes> list = new ArrayList<>();
        List<ClosingPhzPlayerInfoRes.Builder> builderList = new ArrayList<>();
        HylhqPlayer winPlayer = null;

        //大结算计算加倍分
        if(over && jiaBei == 1){
            int jiaBeiPoint = 0;
            int loserCount = 0;
            for (HylhqPlayer player : seatMap.values()) {
                if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                    jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
                    player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
                } else if (player.getTotalPoint() < 0) {
                    loserCount++;
                }
            }
            if (jiaBeiPoint > 0) {
                for (HylhqPlayer player : seatMap.values()) {
                    if (player.getTotalPoint() < 0) {
                        player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                    }
                }
            }
        }

        //大结算低于below分+belowAdd分
        if(over&&belowAdd>0&&playerMap.size()==2){
            for (HylhqPlayer player : seatMap.values()) {
                int totalPoint = player.getTotalPoint();
                if (totalPoint >-below&&totalPoint<0) {
                    player.setTotalPoint(player.getTotalPoint()-belowAdd);
                }else if(totalPoint < below&&totalPoint>0){
                    player.setTotalPoint(player.getTotalPoint()+belowAdd);
                }
            }
        }

        for (HylhqPlayer player : seatMap.values()) {
            if (winList != null && winList.contains(player.getSeat())) {
                winPlayer = seatMap.get(player.getSeat());
            }
            ClosingPhzPlayerInfoRes.Builder build;
            //总分大结算
//
            build = player.bulidTotalClosingPlayerInfoRes();
            build.addAllFirstCards(player.getFirstPais());//将初始手牌装入网络对象
            for(int action : player.getActionTotalArr()){
                build.addStrExt(action+"");
            }
            if (isGoldRoom()){
                build.setTotalPoint((int)player.getWinGold());
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

        //信用分计算
        if (isCreditTable()) {
            //计算信用负分
            calcNegativeCredit();

            long dyjCredit = 0;
            for (HylhqPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
                HylhqPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);
                builder.addStrExt(player.getWinLoseCredit() + "");      //8
                builder.addStrExt(player.getCommissionCredit() + "");   //9
                // 2019-02-26更新
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (HylhqPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
                HylhqPlayer player = seatMap.get(builder.getSeat());
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
            res.setHuCard(finalCard);
            res.addAllCards(winPlayer.buildPhzHuCards());
        }
        res.addAllClosingPlayers(list);
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.addAllExt(buildAccountsExt(over,xingTun));
        res.addAllStartLeftCards(startLeftCards);
        res.addAllIntParams(getIntParams());
        if (over && isGroupRoom() && !isCreditTable()) {
            res.setGroupLogId((int) saveUserGroupPlaylog());
        }
        for (HylhqPlayer player : seatMap.values()) {
            player.writeSocket(res.build());
        }
        return res;
    }



    @Override
    public void sendAccountsMsg() {
        ClosingPhzInfoRes.Builder res = sendAccountsMsg(true, null, 0, null, 0, true, null,null,0);
        saveLog(true,0L, res.build());
    }

    public List<String> buildAccountsExt(boolean isOver,int xingTun) {
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
            ratio = 0;
            pay = 0;
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
        ext.add(autoPlay ? "1" : "0");//20
        ext.add(jiaBei + "");//22
        ext.add(jiaBeiFen + "");//23
        ext.add(jiaBeiShu + "");//24
        ext.add(xingCard+"");//25
        ext.add(xingTun+"");//26
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
        return createTable(new CreateTableInfo(player, TABLE_TYPE_NORMAL, play, bureauCount, params, strParams, saveDb));
    }

    public void createTable(Player player, int play, int bureauCount, List<Integer> params) throws Exception {
        createTable(new CreateTableInfo(player, TABLE_TYPE_NORMAL, play, bureauCount, params, true));
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
        for(Map.Entry<Integer,HylhqPlayer> entry:seatMap.entrySet()){
            if(entry.getValue().getState()==player_state.ready);
        }
    }


    @Override
    public void checkAutoPlay() {
        synchronized (this){
            if(!autoPlay)
                return;
            if (getSendDissTime() > 0) {
                for (HylhqPlayer player : seatMap.values()) {
                    if (player.getLastCheckTime() > 0) {
                        player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                    }
                }
                return;
            }

            if (isAutoPlayOff()) {
                // 托管关闭
                for (int seat : seatMap.keySet()) {
                    HylhqPlayer player = seatMap.get(seat);
                    player.setAutoPlay(false, this);
                    player.setLastOperateTime(System.currentTimeMillis());
                }
                return;
            }

            if (state == table_state.ready&& playedBureau > 0) {
                ++timeNum;
                for (HylhqPlayer player : seatMap.values()) {
                    if ((timeNum >= 5 && player.isAutoPlay())||timeNum >= 30) {
                        autoReady(player);
                    }
                }
                return;
            }

            int timeout;
            if(state != table_state.play){
                return;
            }else if(autoPlay){
                timeout = autoTimeOut;
            }else{
                return;
            }
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
                    HylhqPlayer player = seatMap.get(seat);
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
                HylhqPlayer player = seatMap.get(nowDisCardSeat);
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



    public boolean checkPlayerAuto(HylhqPlayer player ,int timeout){
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
        return HylhqPlayer.class;
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

    public void setFinalCard(int finalCard) {
        this.finalCard = finalCard;
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

    /**
     * 是否是该玩家摸的牌
     */
    public boolean isMoByPlayer(HylhqPlayer player) {
        if (moSeatPair != null && moSeatPair.getValue() == player.getSeat()) {
            if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
                if (nowDisCardIds.get(0).getId() == moSeatPair.getId()) {
                    return true;
                }
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

            for (HylhqPlayer player:playerMap.values()){
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

            for (HylhqPlayer player:playerMap.values()){
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
        for (HylhqPlayer player : seatMap.values()) {
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
    public boolean  isNeedFromOverPop(){
        if(youHuBiHu==1)
            return false;
        return true;
    }

    @Override
    public String getGameName() {
        return "衡阳六胡抢";
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

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_hylhq);

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
        json.put("wanFa", "衡阳六胡抢");
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
