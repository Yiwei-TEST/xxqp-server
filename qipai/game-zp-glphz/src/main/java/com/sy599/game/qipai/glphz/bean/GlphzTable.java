package com.sy599.game.qipai.glphz.bean;

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
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiInfo;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayPaohuziRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzInfoRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCardList;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.glphz.constant.PaohuziConstant;
import com.sy599.game.qipai.glphz.constant.PaohzCard;
import com.sy599.game.qipai.glphz.rule.PaohuziIndex;
import com.sy599.game.qipai.glphz.rule.PaohzCardIndexArr;
import com.sy599.game.qipai.glphz.rule.RobotAI;
import com.sy599.game.qipai.glphz.tool.PaohuziHuLack;
import com.sy599.game.qipai.glphz.tool.PaohuziResTool;
import com.sy599.game.qipai.glphz.tool.PaohuziTool;
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

public class GlphzTable extends BaseTable {
    /*** 玩家map */
    private Map<Long, GlphzPlayer> playerMap = new ConcurrentHashMap<>();
    /*** 座位对应的玩家 */
    private Map<Integer, GlphzPlayer> seatMap = new ConcurrentHashMap<>();
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
    private volatile int shuXingSeat = 0;
    private volatile PaohzCard huCard;
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
    private int finishFapai=0;
    //是否已经发送飘分信息
    private int isSendPiaoFenMsg=0;
    private volatile int timeNum = 0;

    //需要修改，用disNum+moNum代替
    private volatile int moNum=0;
    private volatile int disNum=0;
    //用来记录必胡的人的座位
    private int biHuSeat=0;
    //用该状态记录该回合是摸排还是出牌（吃碰后的出牌），1：出牌，2：摸牌，0：尚未开始或者正在吃碰中
    private volatile int disOrMo=0;

    private volatile int zimoOrFangpao=0;//自摸还是放炮

    //起胡息数量
    private int floorValue=10;
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
    private int chouCardNum=0;
    //抽牌牌堆
    List<Integer> chouCards=new ArrayList<>();
    //醒：0 不带醒 ， 1跟醒 ， 2翻醒
    private int xingType=0;
    
    private int isZhongXing=0;
    private int isShangXing=0;
    private int isXiaXing=0;
    private int isChongxing=0;
    private Boolean[] xingszxTypes;//是不是[上。中。下]
    private int suanzi;//算子  3分1子，5分1子
//    //21张
//    private int card21=0;
    //自摸翻倍
    private int ziMo=0;

    //可胡示众牌
    private int keHuShiZhong=0;
    //示众牌
    private int shiZhongCard=0;
    //飘胡
    private int piaoHu=0;
    //1有胡必胡，2 放炮必胡
    private int biHuType=0;
    //放炮倍数
    private int fangPaoBeiShu=1;
//    //海底胡
//    private int haiDiHu=0;
    //一点红三倍
//    private int yiDianHong3Bei=0;
    //锤
    private int chui=0; 
    //1十红三倍/十三红五倍，2十红三倍，多一红+3胡息
//    private int shiHong3Bei=0;
    //醒牌
    private int xingCard=0;
    //重新翻出来的牌
    private String cxCards="";
    //醒囤
    private String cardXingTuns="";
    //底分 0:1分 1:2分 2:2分
    private int diFen=0;
    //1出牌后明龙，2发牌后明龙
    private int mingLongType=0;
    //低于below加分
    private int belowAdd=0;
    private int below=0;
    //起手提
    private boolean qiShouTi=false;
    //三个座位对应的人是否胡示众牌
    private int[] shiZhongHu=new int[3];
    //起手胡牌座位
    private int huSeat=0;
    //明偎
    private int mingWei=0;
    //跑胡
    private int paoHu=0;
    private int wangxingCard=0;
	private int guipai =0;
    /**
     * 玩家位置对应临时操作
     * 当同时存在多个可做的操作时
     * 1当优先级最高的玩家最先操作 则清理所有操作提示 并执行该操作
     * 2当操作优先级低的玩家先选择操作，则玩家先将所做操作存入临时操作中 当玩家优先级最高的玩家操作后 在判断临时操作是否可执行并执行
     */
    private Map<Integer, TempAction> tempActionMap = new ConcurrentHashMap<>();



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
        playerCount = StringUtil.getIntValue(params, 7, 3);// 比赛人数
        payType = StringUtil.getIntValue(params, 9, 0);// 房费方式
        xingType=StringUtil.getIntValue(params, 10, 0);//醒
        biHuType=StringUtil.getIntValue(params, 11, 0);//必胡选项
        int fangPaoBeiShu=StringUtil.getIntValue(params, 12, 1);//放炮倍数
        //if(fangPaoBeiShu>0&&fangPaoBeiShu<=3)
            this.fangPaoBeiShu=fangPaoBeiShu;
//        haiDiHu=StringUtil.getIntValue(params, 13, 0);//海底胡
//        val1_5_10=StringUtil.getIntValue(params, 14, 0);//1,5,10
        piaoHu=StringUtil.getIntValue(params, 15, 0);//飘胡
//        hongHei=StringUtil.getIntValue(params, 16, 0);//红黑堂
//        tianHu=StringUtil.getIntValue(params, 17, 0);//天胡
//        diHu=StringUtil.getIntValue(params, 18, 0);//地胡
        ziMo=StringUtil.getIntValue(params, 19, 1);//自摸翻倍
//        yiDianHong3Bei=StringUtil.getIntValue(params, 20, 0);//一点红三倍
        keHuShiZhong=StringUtil.getIntValue(params, 21, 0);//可胡示众牌
        chui=0;//加锤
//        int card21=StringUtil.getIntValue(params, 23, 0);//21张
        if(playerCount==2){
        	chouCardNum=StringUtil.getIntValue(params, 23, 0);//可胡示众牌
        }
//        if(playerCount!=4){
//            this.card21=card21;
//        }else {
//            this.card21=14;
//        }
//        shiHong3Bei=StringUtil.getIntValue(params, 24, 0);//1十红三倍/十三红五倍，2十红三倍，多一红+3胡息
        bankerRand=StringUtil.getIntValue(params, 25, 0);//坐庄
        guipai = StringUtil.getIntValue(params, 26, 0);//鬼牌
        int time=StringUtil.getIntValue(params, 27, 0);
        if(time ==1) {
            time =60;
        }
        if(time>0) {
            this.autoPlay =true;
        }
        autoPlayGlob=StringUtil.getIntValue(params, 28, 0);
        jiaBei = StringUtil.getIntValue(params, 29, 0);
        jiaBeiFen = StringUtil.getIntValue(params, 30, 0);
        jiaBeiShu = StringUtil.getIntValue(params, 31, 0);
        diFen = StringUtil.getIntValue(params, 32, 0);
        if(playerCount==2){
            int belowAdd = StringUtil.getIntValue(params, 33, 0);
            if(belowAdd<=100&&belowAdd>=0)
                this.belowAdd=belowAdd;
            int below = StringUtil.getIntValue(params, 35, 0);
            if(below<=100&&below>=0){
                this.below=below;
                if(belowAdd>0&&below==0)
                    this.below=10;
            }
        }
//        mingLongType = StringUtil.getIntValue(params, 34, 0);
        mingWei = StringUtil.getIntValue(params, 36, 0);
        isChongxing = StringUtil.getIntValue(params, 37, 0);
        isShangXing = StringUtil.getIntValue(params, 38, 0);
        isZhongXing = StringUtil.getIntValue(params, 39, 0);
        isXiaXing = StringUtil.getIntValue(params, 40, 0);
        xingszxTypes = new Boolean[]{isShangXing==1,isZhongXing==1,isXiaXing==1};
        floorValue = StringUtil.getIntValue(params, 41, 0);
        suanzi = StringUtil.getIntValue(params, 42, 3);
        if (playerCount<=1||playerCount>4){
            return false;
        }
        setPlayerCount(playerCount);
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
        wrapper.putInt(26, finishFapai);
        JSONArray tempJsonArray = new JSONArray();
        for (int seat : tempActionMap.keySet()) {
            tempJsonArray.add(tempActionMap.get(seat).buildData());
        }
        wrapper.putString(27, tempJsonArray.toString());
        wrapper.putInt(28, chouCardNum);
        wrapper.putInt(29, xingType);
//        wrapper.putInt(31, val1_5_10);
//        wrapper.putInt(32, hongHei);
//        wrapper.putInt(33, tianHu);
//        wrapper.putInt(34, diHu);
//        wrapper.putInt(35, card21);
        wrapper.putInt(36, ziMo);
        wrapper.putInt(37, biHuType);
        wrapper.putInt(38, keHuShiZhong);
        wrapper.putInt(39, shiZhongCard);
        wrapper.putInt(40, piaoHu);
        wrapper.putInt(41, fangPaoBeiShu);
//        wrapper.putInt(42, haiDiHu);
//        wrapper.putInt(43, yiDianHong3Bei);
        wrapper.putInt(44, chui);
//        wrapper.putInt(45, shiHong3Bei);
        wrapper.putInt(46, xingCard);
        wrapper.putInt(47, diFen);
        wrapper.putInt(48, belowAdd);
//        wrapper.putInt(49, mingLongType);
        wrapper.putInt(50, below);
        if(belowAdd>0&&below==0)
            below=10;
        wrapper.putInt(51, mingWei);
        wrapper.putInt(52, isChongxing);
        wrapper.putInt(53, isShangXing);
        wrapper.putInt(54, isZhongXing);
        wrapper.putInt(55, isXiaXing);
        wrapper.putInt(57, suanzi);
        wrapper.putString("cxCards", cxCards);
        wrapper.putInt(58, shuXingSeat);
        wrapper.putInt(59, paoHu);
        wrapper.putInt(60, guipai);
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
        finishFapai=wrapper.getInt(26,0);
        if(autoPlay&&autoTimeOut<=1) {
            autoTimeOut2=autoTimeOut = 60000;
        }
        tempActionMap = loadTempActionMap(wrapper.getString(27));
        
        chouCardNum=wrapper.getInt(28,0);
        xingType=wrapper.getInt(29,0);
//        val1_5_10=wrapper.getInt(31,0);
//        hongHei=wrapper.getInt(32,0);
//        tianHu=wrapper.getInt(33,0);
//        diHu=wrapper.getInt(34,0);
//        card21=wrapper.getInt(35,0);
        ziMo=wrapper.getInt(36,0);
        biHuType=wrapper.getInt(37,0);
        keHuShiZhong=wrapper.getInt(38,0);
        shiZhongCard=wrapper.getInt(39,0);
        piaoHu=wrapper.getInt(40,0);
        fangPaoBeiShu=wrapper.getInt(41,1);
//        haiDiHu=wrapper.getInt(42,0);
//        yiDianHong3Bei=wrapper.getInt(43,0);
        chui=wrapper.getInt(44,0);
//        shiHong3Bei=wrapper.getInt(45,0);
        xingCard=wrapper.getInt(46,0);
        diFen=wrapper.getInt(47,0);
        belowAdd=wrapper.getInt(48,0);
//        mingLongType=wrapper.getInt(49,0);
//        //修复svn合并代码造成的mingLong值异常，默认改为出牌后明龙
//        if(mingLongType>2)
//            mingLongType=1;
        below=wrapper.getInt(50,0);
        mingWei=wrapper.getInt(51,0);
        isChongxing=wrapper.getInt(52,0);
        isShangXing=wrapper.getInt(53,0);
        isZhongXing=wrapper.getInt(54,0);
        isXiaXing=wrapper.getInt(55,0);
        suanzi=wrapper.getInt(57,3);
        cxCards=wrapper.getString("cxCards");
        shuXingSeat = wrapper.getInt(58, 0);
        paoHu = wrapper.getInt(59, 0);
        guipai = wrapper.getInt(60, 0);
    }

    public int getDisNum() {
        return disNum;
    }

    public int getMoNum() {
        return moNum;
    }

//    public int getVal1_5_10() {
//        return val1_5_10;
//    }

//    public int getHongHei() {
//        return hongHei;
//    }
//
//    public int getTianHu() {
//        return tianHu;
//    }

//    public int getHaiDiHu() {
//        return haiDiHu;
//    }
//
//    public int getDiHu() {
//        return diHu;
//    }

    public int getPaoHu() {
        return paoHu;
    }

    public void setPaoHu(int paoHu) {
        this.paoHu = paoHu;
    }
//    public int getCard21() {
//        return card21;
//    }

    public int getZiMo() {
        return ziMo;
    }
//
//    public int getShiHong3Bei() {
//        return shiHong3Bei;
//    }

    public int getShiZhongCard() {
        return shiZhongCard;
    }

    public int getPiaoHu() {
        return piaoHu;
    }

    public int getFangPaoBeiShu() {
        return fangPaoBeiShu;
    }
//
//    public int getYiDianHong3Bei() {
//        return yiDianHong3Bei;
//    }

    public int getMingWei() {
        return mingWei;
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
        int shuWinTun=0;
        int shuxingTun=0;//数醒的人
        boolean isOver = false;
        int nextBankerSeat = lastWinSeat;
        if(!isHuangZhuang){
            Integer winSeat = winList.get(0);
            // 赢的玩家
            GlphzPlayer winPlayer = seatMap.get(winSeat);
            //数醒的玩家
            GlphzPlayer xingPlayer = seatMap.get(shuXingSeat);
           
            winPlayer.changeAction(PaohuziConstant.ACTION_COUNT_INDEX_HU, 1);
            mt = winPlayer.getHu().getMingTang();
            int huxi=winPlayer.getTotalHu();
//            if(mt.contains(PaohuziMingTangRule.LOUDI_MINGTANG_PIAOHU))
//                huxi=15;
            tun=countXiTun(huxi,mt,winPlayer);
            Integer[] xingTunAll = xingTun(winPlayer);
            if(xingTunAll != null && xingTunAll.length >= 2){
            	xingTun+= xingTunAll[0];
            	if(lastWinSeat==winPlayer.getSeat() && xingPlayer!= null){
            		shuxingTun+=xingTunAll[1];
            	}
            }
            tun+=xingTun;
//            lostTun=PaohuziMingTangRule.countXiTun(tun,mt,this);
            lostTun=tun;
            if(disOrMo==1){
                //放炮
            	zimoOrFangpao=1;
            	winTun=lostTun=lostTun*fangPaoBeiShu;
                if(lastWinSeat==winPlayer.getSeat() && xingPlayer!= null){
//                	shuWinTun = shuxingTun*fangPaoBeiShu;
                	shuWinTun = shuxingTun;
                }
                GlphzPlayer player = seatMap.get(disCardSeat);
                int lose = 0-(lostTun+shuWinTun);
                player.calcResult(this, 1, lose, isHuangZhuang);
                countWinLostPoint(winPlayer,player,xingPlayer,lostTun,shuWinTun);
            }else if(isMoFlag()||(disOrMo==0&&moNum==0)){
                //摸胡
                if(moSeat==winSeat){
                    winPlayer.changeAction(PaohuziConstant.ACTION_COUNT_INDEX_ZIMO, 1);
                    if(ziMo == 1){
                    	lostTun+=ziMo;
                    }else{
                    	lostTun*=ziMo;
                    }
//                    if(lastWinSeat==winPlayer.getSeat() && xingPlayer!= null){
//                    	shuxingTun*=ziMo;
//                    }
                    zimoOrFangpao=2;
                }
                for (int seat : seatMap.keySet()){
                	if (xingPlayer!= null && seat == xingPlayer.getSeat()) {
                		 // 数醒不为输 -- 数醒得到下醒得分数
                         continue;
                    }
                	if(lastWinSeat == winPlayer.getSeat() && seat!=winPlayer.getSeat()){
                		shuWinTun +=shuxingTun;
                 	}
                    if (winSeat!=seat) {
                        GlphzPlayer player = seatMap.get(seat);
                        winTun+=lostTun;
                        int lose = 0-(lostTun+shuxingTun);
                        player.calcResult(this, 1, lose, isHuangZhuang);
                        countWinLostPoint(winPlayer,player,xingPlayer,lostTun,shuxingTun);
                    }
                }
            }
            if(shuWinTun > 0){
            	xingPlayer.calcResult(this, 0, shuWinTun, isHuangZhuang);
            }
            winPlayer.calcResult(this, 1, winTun, isHuangZhuang);
            nextBankerSeat = winSeat;
        }
        if(playBureau == totalBureau){
            isOver=true;
            changeTableState(table_state.over);
        }
        if(autoPlayGlob >0) {
//          //是否解散
            boolean diss = false;
			if (autoPlayGlob == 1) {
				for (GlphzPlayer seat : seatMap.values()) {
					if (seat.isAutoPlay()) {
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

        if(isHuangZhuang) {
            nextBankerSeat = calcNextSeat(lastWinSeat);
        }
        calcAfter();
        ClosingPhzInfoRes.Builder res = sendAccountsMsg(isOver, winList, 0, mt, tun, false, null,null,xingTun);
        saveLog(isOver,0L, res.build());
        setLastWinSeat(nextBankerSeat);
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
			 for (GlphzPlayer seat : seatMap.values()) {
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
    
    private void countWinLostPoint(GlphzPlayer win,GlphzPlayer lost,GlphzPlayer shuxing,int point,int xing){
        if(chui<=0)
            return ;
        point*=Math.pow(2,win.getChui()+lost.getChui());
        win.changeWinLostPoint(point);
        lost.changeWinLostPoint(-point);
        if(shuxing != null){
        	shuxing.changeWinLostPoint(xing);
        }
        return ;
    }

    private int countXiTun(int huxi,List<Integer> mt,GlphzPlayer player){
//        if(mt.contains(PaohuziMingTangRule.LOUDI_MINGTANG_SHIHONG)){
//            List<PaohzCard> allCards = new ArrayList<>();
//            for (CardTypeHuxi type : player.getCardTypes()) {
//                allCards.addAll(PaohuziTool.toPhzCards(type.getCardIds()));
//            }
//            if (player.getHu() != null && player.getHu().getPhzHuCards() != null) {
//                for (CardTypeHuxi type : player.getHu().getPhzHuCards()) {
//                    allCards.addAll(PaohuziTool.toPhzCards(type.getCardIds()));
//                }
//            }
//            List<PaohzCard> redCardList = PaohuziTool.findRedPhzs(allCards);
//            if(redCardList.size()>10){
//                huxi+=(redCardList.size()-10)*3;
//            }
//        }
        int tun=0;
//        if(huxi<10){
//            return 0;
//        }else if(huxi==10){
//            tun=3;
//        }else if(huxi==11) {
//            tun=1;
//        }else if(huxi>=12&&huxi<15){
//            tun=2;
//        }else if(huxi>=15){
//            tun=3+(huxi-15)/3;
//        }
        tun = (huxi - floorValue) /suanzi + 1;
        
        if(diFen>0&&diFen<3)
            tun+=diFen;
        return tun;
    }

    
    /**
     * 醒牌加屯
     * @param winPlayer
     * @return
     */
    private Integer[] xingTun(GlphzPlayer winPlayer){
        List<Integer[]> xingCardVals = new ArrayList<>();
        PaohzCard card = null;
        
        switch (xingType){
            case 0:
                return null;
            case 1:
            	card =  PaohzCard.getPaohzCard(finalCard);
                break;
            case 2:
            	//先翻一张醒牌
            	if(leftCards.size()==0){
            		//海底捞
            		card = PaohzCard.getPaohzCard(finalCard);
	            }else {
	            	card = getNextCard();
	            }
                break;
        }
        this.xingCard=card.getId();
        Set<Integer> cxSet=new HashSet<>();
        if(card.isGuiPai()){
        	if(xingType== 1){//跟醒跟到鬼牌
        		int newGuiHuanVal = winPlayer.getHu().getGuihuanCard();
        		wangxingCard = PaohzCard.getIdByHucardVal(newGuiHuanVal);
        		Integer[] cardVals = getXingCard(newGuiHuanVal,cxSet,winPlayer);//得到X张醒牌
        		int zhuangtun = 0;
        		int xiantun = 0;
        		for (int i = 0; i < cardVals.length; i++) {
    				if(cardVals[i] == null || cardVals[i] ==0){
    					continue;
    				}
    				if(playerCount >= 4 && shuXingSeat > 0 && i == 2){
    					xiantun += getCardNumByCardVal(winPlayer, cardVals[i]);
    				}else{
    					int newTun = getCardNumByCardVal(winPlayer, cardVals[i]);
    					zhuangtun+=newTun;
    				}
    			}
    			if("".equals(cardXingTuns)){
    				cardXingTuns = zhuangtun+"";
    			}else{
    				cardXingTuns =cardXingTuns+","+ zhuangtun;
    			}
    			return new Integer[]{zhuangtun,xiantun};
        	}else{
        		Integer[] xinfen = getXingMaxTun(winPlayer);
    			if(xinfen.length > 2){}
            	cardXingTuns = xinfen[0]+"";
            	return xinfen;
        	}
        	
        }
        
        Integer[] xingCardValAll = getXingCard(card.getVal(),cxSet,winPlayer);//得到X张醒牌
        if(xingCardValAll.length <= 0)
        	return null;
        xingCardVals.add(xingCardValAll);
        boolean iscfGui=false;
        if(isChongxing == 1 && leftCards.size()>0 && cxSet.size() > 0 && !card.isGuiPai()){
    		Set<Integer> jxset = new HashSet<>();
    		while (cxSet.size() > 0) {
				//先翻一张醒牌
            	if(leftCards.size()==0){
            		break;
	            }else {
	            	PaohzCard cxcard = getNextCard();
	            	if(cxcard != null){
	            		if(cxcard.isGuiPai()){
	            			iscfGui = true;
	            			break;
	            		}
	            		if(cxCards != null && !"".equals(cxCards)){
	            			cxCards += ","+cxcard.getId();
	            		}else{
	            			cxCards = cxcard.getId()+"";
	            		}
	            		Integer[] cxCardAll = getXingCard(cxcard.getVal(),jxset,winPlayer);//得到X张醒牌
	            		xingCardVals.add(cxCardAll);
	            	}
	            }
    			cxSet.clear();
    			cxSet.addAll(jxset);
    			jxset.clear();
    		}
        }
        
        if(xingCardVals.size() <= 0)
            return null;
        
        int dunshu = 0;
        int xingdunshu = 0;
        for (Integer[] cardVals:xingCardVals) {
			if(cardVals == null || cardVals.length <= 0){
				cardXingTuns = cardXingTuns+",";
				continue;
			}
			int zhuangtun = 0;
			for (int i = 0; i < cardVals.length; i++) {
				if(cardVals[i] == null || cardVals[i] ==0){
					continue;
				}
				if(playerCount >= 4 && shuXingSeat > 0 && i == 2){
					xingdunshu += getCardNumByCardVal(winPlayer, cardVals[i]);
				}else{
					int newTun = getCardNumByCardVal(winPlayer, cardVals[i]);
					dunshu += newTun;
					zhuangtun+=newTun;
				}
			}
			if("".equals(cardXingTuns)){
				cardXingTuns = zhuangtun+"";
			}else{
				cardXingTuns =cardXingTuns+","+ zhuangtun;
			}
		}
        if(iscfGui){
        	Integer[] guiTun = getXingMaxTun(winPlayer);
        	if(cxCards != null && !"".equals(cxCards)){
    			cxCards += ","+81;
    		}else{
    			cxCards = 81+"";
    		}
        	if(guiTun!= null && guiTun.length>=2){
        		dunshu +=guiTun[0];
        		xingdunshu+=guiTun[1];
        		if("".equals(cardXingTuns)){
    				cardXingTuns = dunshu+"";
    			}else{
    				cardXingTuns =cardXingTuns+","+ dunshu;
    			}
        	}
        }
        return new Integer[]{dunshu,xingdunshu};
    }

    /**获取牌数*/
    private int getCardNumByCardVal(GlphzPlayer winPlayer,int cardVal){
    	if(cardVal <= 0 || winPlayer == null){
    		return 0;
    	}
    	int num = 0;
    	for (CardTypeHuxi type:winPlayer.getCardTypes()){
            for(Integer id:type.getCardIds()){
                if(PaohzCard.getPaohzCard(id).getVal()==cardVal)
                	num++;
            }
        }
        List<PaohzCard> handCards = PaohuziTool.toPhzCards(winPlayer.getHandPais());
        for (PaohzCard paohzCard:handCards) {
            if(paohzCard.getVal()==cardVal)
            	num++;
        }
//        if(PaohzCard.getPaohzCard(finalCard).getVal()==cardVal)
//        	num++;
//        if(xingType==1||(xingType==2&&leftCards.size()==0))
//        	num++;
        if(huCard != null && huCard.getVal() == cardVal && num<4){
        	num++;
        }
        if(winPlayer.getHu().getGuihuanCard() == cardVal && num < 4){
        	num++;
        }
        return num;
    }
    
//    private  List<PaohzCard> getXingCard(PaohzCard card,Set<Integer> csSet){
//    	List<PaohzCard> list = new ArrayList<>();
//    	if(isZhongXing == 1){
//    		list.add(card);
//    	}
//    	if(isXiaXing == 1){
//    		PaohzCard xiacard = PaohzCard.getNextCardVal(card.getId());
//    		if(xiacard != null){
//    			list.add(xiacard);
//    		}
//    		//判断是否有4张这个
//    	}
//    	if(isShangXing == 1){
//    		PaohzCard shangcard = PaohzCard.getLastCardVal(card.getId());
//    		if(shangcard != null){
//    			list.add(shangcard);
//    		}
//    	}
//    	return list;
//    }
    private  Integer[] getXingCard(Integer credVal,Set<Integer> csSet,GlphzPlayer winPlayer){
    	Integer[] xingVals = new Integer[3];
    	if(isZhongXing == 1){
    		xingVals[0] = credVal;
    		if(isYiLong(credVal, winPlayer)){
    			csSet.add(credVal);
    		}
    	}
    	if(isXiaXing == 1){
    		Integer xiacardVal = PaohzCard.getNextCardVal(credVal);
    		if(xiacardVal != null){
    			xingVals[2] = xiacardVal;
    		}
    		if(isYiLong(xiacardVal, winPlayer)){
    			csSet.add(xiacardVal);  
    		}
    	}
    	if(isShangXing == 1){
    		Integer shangcardVal = PaohzCard.getLastCardVal(credVal);
    		if(shangcardVal != null){
    			xingVals[1] = shangcardVal;
    		}
    		if(isYiLong(shangcardVal, winPlayer)){
    			csSet.add(shangcardVal);
    		}
    	}
    	return xingVals;
    }
    
    
    /**
     * 找到最多醒的那张牌
     * @param credVal
     * @param csSet
     * @param winPlayer
     * @return
     */
    private  Integer[] getXingMaxTun(GlphzPlayer winPlayer){
    	Set<Integer> cardVals = new HashSet<>();
    	for (CardTypeHuxi type:winPlayer.getCardTypes()){
            for(Integer id:type.getCardIds()){
                if(!cardVals.contains(PaohzCard.getPaohzCard(id).getVal())){
                	cardVals.add(PaohzCard.getPaohzCard(id).getVal());
                }
            }
        }
        List<PaohzCard> handCards = PaohuziTool.toPhzCards(winPlayer.getHandPais());
        for (PaohzCard paohzCard:handCards) {
        	if(!cardVals.contains(paohzCard.getVal())){
            	cardVals.add(paohzCard.getVal());
            }
        }
        if(huCard!= null && !cardVals.contains(huCard.getVal())){
        	cardVals.add(huCard.getVal());
        }
        if(cardVals.size() <= 0){
        	return null;
        }
        
    	//找到赢家醒囤最多的那张牌
        int zhuangtun = 0;
        int cardVal =0;
        int xianTun = 0;//闲家的囤
        for (Integer val:cardVals) {
        	int tun = 0;
        	int xian = 0;
        	if(isZhongXing == 1){
        		tun +=getCardNumByCardVal(winPlayer, val);
        	}
        	if(isShangXing == 1){
        		tun +=getCardNumByCardVal(winPlayer, PaohzCard.getLastCardVal(val));
        	}
        	if(isXiaXing == 1 && playerCount < 4){
        		if(playerCount == 4){
        			xian =getCardNumByCardVal(winPlayer, PaohzCard.getNextCardVal(val));
        		}else{
        			tun +=getCardNumByCardVal(winPlayer, PaohzCard.getNextCardVal(val));
        		}
        	}
        	if(tun > zhuangtun){
        		zhuangtun = tun;
        		cardVal = val;
        		xianTun = xian;
        	}else if(tun == zhuangtun && xian > xianTun){
        		cardVal = val;
        		xianTun = xian;
        	}
		}
//        winPlayer.getHu().setGuihuanCard(cardVal);
        wangxingCard = PaohzCard.getIdByHucardVal(cardVal);
    	return new Integer[]{zhuangtun,xianTun};
    }
    
    
    
    /**
     * 是否有一拢  同val有4张
     * @return
     */
    public boolean isYiLong(int cardVal,GlphzPlayer winPlayer){
    	int i = 0;
    	for (CardTypeHuxi type:winPlayer.getCardTypes()){
            for(Integer id:type.getCardIds()){
                if(PaohzCard.getPaohzCard(id).getVal()==cardVal)
                	i++;
            }
        }
        List<PaohzCard> handCards = PaohuziTool.toPhzCards(winPlayer.getHandPais());
        for (PaohzCard paohzCard:handCards) {
            if(paohzCard.getVal()==cardVal)
            	i++;
        }
//        if(PaohzCard.getPaohzCard(finalCard).getVal()==cardVal)
//        	i++;
        if(huCard != null && huCard.getVal() == cardVal){
        	i++;
        }

        if(i>=4){
        	return true;
        }
        
    	return false;
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
            for (GlphzPlayer player : playerMap.values()) {
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
        GlphzPlayer winPlayer = seatMap.get(lastWinSeat);
        finalCard=winPlayer.getHandPais().get(0);
        if(keHuShiZhong==1)
            shiZhongCard=finalCard;
        for (GlphzPlayer tablePlayer : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
//            res.addAllHandCardIds(tablePlayer.getHandPais());
            res.addAllHandCardIds(tablePlayer.getSeat() == shuXingSeat?winPlayer.getHandPais():tablePlayer.getHandPais());
            res.setNextSeat(lastWinSeat);
            res.setGameType(getWanFa());// 1跑得快 2麻将
            res.setRemain(leftCards.size());
            res.setBanker(lastWinSeat);
            res.addXiaohu(winPlayer.getHandPais().get(0));
            //res.addXiaohu(winPlayer.getSeat());
            res.addXiaohu(shuXingSeat);
            tablePlayer.writeSocket(res.build());
            if(tablePlayer.isAutoPlay()) {
          		 addPlayLog(tablePlayer.getSeat(), PaohzDisAction.action_tuoguan + "",1 + "");
           	}
            sendTingInfo(tablePlayer);
        }
        
    }

    
    public void sendTingInfo(GlphzPlayer player) {
        if(player.isAutoPlay()){
            return;
        }
        long start = System.currentTimeMillis();
        DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
        List<PaohzCard> huCardList = new ArrayList<>(PaohzCard.huCardList);
        List<PaohzCard> handCardList = player.getHandPhzs();
        List<Integer> handCards = player.getOperateCardVals();
        Set<Integer> nohuCards = getNoHuCards(player);
        if(handCards == null || handCards.size() <= 0 || huCardList == null || huCardList.size() <= 0){
        	return;
        }
        for (Integer handCardVal : handCards) {
        	List<PaohzCard> copyPais = new ArrayList<>(handCardList);
        	for (PaohzCard card:copyPais) {
				if(card.getVal() == handCardVal){
					copyPais.remove(card);
					break;
				}
			}
        	Set<Integer> canhuVals = new HashSet<>();
        	for (PaohzCard nowDisCard : huCardList) {
        		if(nohuCards.contains(nowDisCard.getVal())){
        			continue;
        		}
        		List<PaohuziHuLack> pao = player.checkPaoHuTingpai(copyPais,nowDisCard, false, true);
                if(pao!=null && pao.size() >0){
                    canhuVals.add(nowDisCard.getVal());
                    nowDisCard=null;
                    continue;
                }
        		PaohuziHuLack lack = player.checkHuNewTingpai(copyPais,nowDisCard, true);
                if (lack.isHu()) {
                	canhuVals.add(nowDisCard.getVal());
                }
        		
//        		if(isCanHu(player, copyPais, nowDisCard)){
//        			canhuVals.add(nowDisCard.getVal());
//        		}
			}
        	if(canhuVals.size() <= 0){
        		continue;
        	}
			 DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
			 ting.setMajiangId(handCardVal);
			 ting.addAllTingMajiangIds(canhuVals);
//			 ting.addTingMajiangIds(81);
			 tingInfo.addInfo(ting.build());
		}
        if (tingInfo.getInfoCount() > 0) {
            player.writeSocket(tingInfo.build());
        }
        long timeUse = System.currentTimeMillis() - start;
        if(timeUse > 50){
            StringBuilder sb = new StringBuilder("Glphz|sendTingInfo");
            sb.append("|").append(timeUse);
            sb.append("|").append(start);
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.getHandPais());
            LogUtil.monitorLog.info("------------------------------"+sb.toString());
        }
        
    }
    private void removeHands(Set<Integer> list,Set<Integer> huCardList) {
		if (list != null && list.size() > 0) {
			huCardList.removeAll(list);
		}
	}
    public Set<Integer> getNoHuCards(GlphzPlayer player){
    	Set<Integer> huCardList = new HashSet<>();
    	huCardList.addAll(player.getTiVals());
    	huCardList.addAll(player.getPaoVals());
    	huCardList.addAll(player.getgetChouZaiVals());
    	PaohuziHandCard bean =PaohuziTool.getPaohuziHandCardBean(new ArrayList<>(player.getHandPhzs()));
        PaohzCardIndexArr indexArr = bean.getIndexArr();
        PaohuziIndex index3 = indexArr.getPaohzCardIndex(3);
        if (index3 != null) {
            List<Integer> list = index3.getValList();
            for (int val : list) {
            	huCardList.add(val);
            }
        }

        return huCardList;
    }
    public Set<Integer> getHandCards(GlphzPlayer player){
    	
       Set<Integer> huCardList = player.getHandVals();
      
		removeHands(player.getTiVals(), huCardList);
		removeHands(player.getPaoVals(), huCardList);
		removeHands(player.getZaiVals(), huCardList);
		removeHands(player.getgetChouZaiVals(), huCardList);

      return huCardList;
  }
    
    @Override
    public void startNext() {
        if(finishFapai==1)
            checkAction();
    }

    public void play(GlphzPlayer player, List<Integer> cardIds, int action) {
        play(player, cardIds, action, false, false, false);
    }

    private void hu(GlphzPlayer player, List<PaohzCard> cardList, int action, PaohzCard nowDisCard,boolean system) {
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
        if(nowDisCard==null&&shiZhongCard!=0&&player.getSeat()!=lastWinSeat){
            nowDisCard=PaohzCard.getPaohzCard(shiZhongCard);
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
                pao(player,hu.getPaohuList(),nowDisCard,PaohzDisAction.action_pao,true,false,true);
                List<PaohuziHuLack> list = player.checkHu1(null, isSelfMo(player));
                hu = filtrateHu(player, null, list);
            }

        }


//        if (hu!=null&&hu.isHu()&&hu.getHuxi()>=qihu) {
        if (hu!=null&&hu.isHu()) {
        	huCard = nowDisCard;
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


    private void huShiZhong(GlphzPlayer player,PaohzCard nowDisCard) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (huConfirmList.contains(player.getSeat())) {
            return;
        }
        if (!checkAction(player, PaohzDisAction.action_hu,null,nowDisCard)) {
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
            // player.writeErrMsg(LangMsgEnum.code_29);
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (actionList.get(0) != 1) {
            return;
        }

        if (!checkAction(player, PaohzDisAction.action_hu,null,nowDisCard)) {
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
            // player.writeErrMsg(LangMsgEnum.code_29);
            return;
        }

        //是否为起手胡示众牌
        if(shiZhongCard!=0){
            int seat=lastWinSeat;
            while (true){
                GlphzPlayer p=seatMap.get(seat);
                PaohuziCheckCardBean checkCard=p.checkCard(null, true, true, false);;
                if(checkPaohuziCheckCard(checkCard)){
                    qiShouTi=true;
                    playAutoDisCard(checkCard);
                    tiLong(p);
                    qiShouTi=false;
                }
                seat=calcNextSeat(seat);
                if(seat==lastWinSeat)
                    break;
            }
        }

        List<PaohuziHuLack> pao = player.checkPaoHuShiZhong(nowDisCard,true);
        List<PaohuziHuLack> ping = player.checkHu1(nowDisCard, isSelfMo(player));
        PaohuziHuLack hu = filtrateHu(player, pao, ping);
        if(hu!=null&&!player.isSiShou()){
            if (hu.getPaohuAction()==PaohzDisAction.action_ti){
                List<PaohzCard> paohuList = hu.getPaohuList();
                ti(player, paohuList!=null?paohuList:null,nowDisCard,PaohzDisAction.action_ti,false,true);
                if(biHuType==1)
                    return;
                List<PaohuziHuLack> list = player.checkHu1(null, isSelfMo(player));
                hu = filtrateHu(player, null, list);
            }
        }
        if (hu!=null&&hu.isHu()) {
            player.setHuxi(hu.getHuxi());
            player.setHu(hu);
            huConfirmList.add(player.getSeat());
            addPlayLog(player.getSeat(), PaohzDisAction.action_hu + "", PaohuziTool.implodePhz(null, ","));
            sendActionMsg(player, PaohzDisAction.action_hu, null, PaohzDisAction.action_type_action);
            calcOver();
        } else {
            broadMsg(player.getName() + " 不能胡牌");
        }

    }




    public PaohuziHuLack filtrateHu(GlphzPlayer player,List<PaohuziHuLack> pao,List<PaohuziHuLack> ping){
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
                if (allHuxi >= getFloorValue()||(piaoHu==1&&allHuxi==0))
                    hu.add(lack);
            }
        }
        if(hu.size()>=1){
            return getMaxHu(player,hu);
        }
        return null;
    }

    public PaohuziHuLack getMaxHu(GlphzPlayer player,List<PaohuziHuLack> hu){
        int maxtun=-1;
        int maxXing = -1;
        PaohuziHuLack lack1=null;
        for (PaohuziHuLack lack:hu){
//            List<Integer> arr = PaohuziMingTangRule.calcMingTang(player, lack,this);
            List<Integer> arr = new ArrayList<>();
            int point = lack.getHuxi()+player.getOutHuxi() +player.getZaiHuxi();
            if(piaoHu==1&&point==0){
                point=15;
            }
            int tun=countXiTun(point,arr,player);
            int xing = lack.getGuixing();
            if(maxtun<tun){
                maxtun=tun;
                lack1=lack;
                lack1.setMingTang(arr);
                lack1.setFinallyPoint(maxtun);
                maxXing = xing;
            }else if(maxtun==tun && maxXing < xing){
            	lack1=lack;
                lack1.setMingTang(arr);
                lack1.setFinallyPoint(maxtun);
                maxXing = xing;
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
    public boolean isSelfMo(GlphzPlayer player) {
        if(disNum==0&&moNum==0)
            return true;
        if (moSeatPair != null) {
            return moSeatPair.getValue().intValue() == player.getSeat();
        }
        return false;
    }
    /**
     * 提
     */
    private void ti(GlphzPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action, boolean moPai,boolean tiHu) {
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
        if(!tiHu)
            sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action, isZaiPao, false);
        if(checkCard.isHu()&&biHuType==1){
            hu(player,null,PaohzDisAction.action_hu,null,true);
        }
    }

    /**
     * 栽(臭栽)
     *
     * @param cardList 要栽的牌
     */
    private void zai(GlphzPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }

        boolean isFristDisCard = player.isFristDisCard();
//        PaohuziCheckCardBean checkAutoDis = checkAutoDis(player, false);
//        if (checkAutoDis != null) {
//            playAutoDisCard(checkAutoDis, true);
//        }
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
        if(checkCard.isHu()&&biHuType==1){
            hu(player,null,PaohzDisAction.action_hu,null,true);
        }
        if (!disCard) {
            // checkMo();
        }

    }

    /**
     * 跑
     */
    private void pao(GlphzPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action, boolean isHu, boolean isPassHu ,boolean system) {

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
    private void disCard(GlphzPlayer player, List<PaohzCard> cardList, int action) {
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
        if(shiZhongCard!=0)
            shiZhongCard=0;
        //第一次出牌检测明龙
        if(disNum==1&&mingLongType==1){
            int seat=lastWinSeat;
            while (true){
                GlphzPlayer p=seatMap.get(seat);
                PaohuziCheckCardBean checkCard=p.checkCard(null, true, true, false);;
                if(checkPaohuziCheckCard(checkCard)){
                    qiShouTi=true;
                    playAutoDisCard(checkCard);
                    tiLong(p);
                    qiShouTi=false;
                }
                seat=calcNextSeat(seat);
                if(seat==lastWinSeat)
                    break;
            }
        }

        player.setZaiTiHu(false);
        // 判断是否为放招
//        boolean paoFlag = isFangZhao(player, cardList.get(0));
//        if (paoFlag) {
//            if(player.isAutoPlay()){//托管自动放招
//                player.setFangZhao(1);
//                for (Player playerTemp : getSeatMap().values()) {
//                    playerTemp.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao, player.getUserId() + "", 1 + "");
//                }
//            }else if (!player.isFangZhao() && !player.isRobot()) {
//                player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, cardList.get(0).getId());
//                LogUtil.msgLog.info("----tableId:" + getId() + "---userName:" + player.getName() + "------是否确定放招:--->>>>>>" + cardList.get(0));
//                player.writeComMessage(WebSocketMsgType.res_com_code_fangzhao, cardList.get(0).getId());
//                return;
//            }
//        }


        addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
//        checkFreePlayerTi(player, action);// 检查闲家提
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
            GlphzPlayer p = seatMap.get(biHuSeat);
            hu(p,null,PaohzDisAction.action_hu,cardList.get(0),true);
        }
        checkAutoMo();
    }

    private void checkAutoMo() {
        if (isTest()) {
            checkMo();
        }
    }

    private void tiLong(GlphzPlayer player) {
        boolean isTiLong = false;
        List<PaohzCard> cardList = new ArrayList<>();
        while (player.getOweCardCount() < -1) {
            if (!isTiLong) {
                isTiLong = true;
                removeAction(player.getSeat());
            }
            PaohzCard card = getNextCard();
            player.tiLong(card);
            cardList.add(card);

            addPlayLog(player.getSeat(), PaohzDisAction.action_buPai + "", (card == null ? 0 : card.getId()) + "");
            StringBuilder sb = new StringBuilder("glphz.tiLong");
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

    public void checkFreePlayerTi(GlphzPlayer player, int action) {
        if (player.getSeat() == lastWinSeat && player.isFristDisCard() && action != PaohzDisAction.action_ti) {
            for (int seat : getSeatMap().keySet()) {
                if (lastWinSeat == seat) {
                    continue;
                }
                GlphzPlayer nowPlayer = seatMap.get(seat);
                PaohuziCheckCardBean checkCard = nowPlayer.checkCard(null, true, true, false);
                if (checkPaohuziCheckCard(checkCard)) {
                    playAutoDisCard(checkCard);
                    if (nowPlayer.isFristDisCard()) {
                        nowPlayer.setFristDisCard(false);
                    }
                    tiLong(nowPlayer);
                }
                checkSendActionMsg(false);
            }
        }
    }

    /**
     * 碰
     */
    private void peng(GlphzPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
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
//        PaohuziCheckCardBean checkAutoDis = checkAutoDis(player, false);
//        if (checkAutoDis != null) {
//            playAutoDisCard(checkAutoDis, true);
//
//        }
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
            for (GlphzPlayer seatPlayer : seatMap.values()) {
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
    private void pass(GlphzPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
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
        if (actionList.get(0) == 1 && player.getHandPhzs().isEmpty()) {
            player.writeErrMsg("手上已经没有牌了,不能过胡");
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
        // 可以胡牌，然后点了过
        boolean isPassHu = actionList.get(0) == 1;
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
     * 过示众牌
     */
    private void passShiZhong(GlphzPlayer player, List<PaohzCard> cardList,  int action) {
        if((cardList==null||cardList.size()==0)&&shiZhongCard!=0){
            cardList=new ArrayList<>();
            cardList.add(PaohzCard.getPaohzCard(shiZhongCard));
        }
        if (!actionSeatMap.containsKey(player.getSeat())) {
            // player.writeErrMsg("该玩家没有找到可以过的动作");
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        List<Integer> list = PaohzDisAction.parseToDisActionList(actionList);
        // 如果胡也是不可以过的
        if (!list.contains(PaohzDisAction.action_hu)) {
            return;
        }
        removeAction(player.getSeat());
        boolean flag1=false;
        for(List<Integer> acts:actionSeatMap.values()){
            if(PaohzDisAction.parseToDisActionList(acts).contains(PaohzDisAction.action_hu))
                flag1=true;
        }
        boolean flag2=false;
        if(!flag1){
            int seat=calcNextSeat(lastWinSeat);
            while (true){
                GlphzPlayer p=seatMap.get(seat);
                PaohuziCheckCardBean checkCard=p.checkCard(null, true, true, false);;
                if(checkPaohuziCheckCard(checkCard)){
                    playAutoDisCard(checkCard);
                    tiLong(p);
                    flag2=true;
                }
                seat=calcNextSeat(seat);
                if(seat==calcNextSeat(lastWinSeat))
                    break;
            }
        }
        if(flag1||!flag2){
            sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action);
        }
    }


    /**
     * 吃
     */
    private void chi(GlphzPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
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

        cardList = player.getChiList(nowDisCard, cardList,false);
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
//        PaohuziCheckCardBean checkAutoDis = checkAutoDis(player, false);
//        if (checkAutoDis != null) {
//            playAutoDisCard(checkAutoDis, true);
//        }

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

    public synchronized void play(GlphzPlayer player, List<Integer> cardIds, int action, boolean moPai, boolean isHu, boolean isPassHu) {
        // 检查play状态
        if (state != table_state.play || player.getSeat() == shuXingSeat) {
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
            StringBuilder sb = new StringBuilder("glphz");
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
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        List<Integer> list = PaohzDisAction.parseToDisActionList(actionList);
        // 如果胡也是不可以过的
        if (list.contains(PaohzDisAction.action_hu) && action != PaohzDisAction.action_hu) {
            return;
        }
        
        if (action == PaohzDisAction.action_ti) {
            if (cardList.size() > 4) {
                // 有多个提
                PaohzCardIndexArr arr = PaohuziTool.getMax(cardList);
                PaohuziIndex index = arr.getPaohzCardIndex(3);
                for (List<PaohzCard> tiCards : index.getPaohzValMap().values()) {
                    ti(player, tiCards, nowDisCard, action, moPai,false);
                }
            } else {
                ti(player, cardList, nowDisCard, action, moPai,false);
            }
        } else if (action == PaohzDisAction.action_hu) {
            if(shiZhongCard!=0&&player.getSeat()!=lastWinSeat){
                huShiZhong(player,PaohzCard.getPaohzCard(shiZhongCard));
            }else {
                hu(player, cardList, action, nowDisCard,false);
            }

        } else if (action == PaohzDisAction.action_peng) {
            peng(player, cardList, nowDisCard, action);
        } else if (action == PaohzDisAction.action_chi) {
            chi(player, cardList, nowDisCard, action);
        } else if (action == PaohzDisAction.action_pass) {
            if(shiZhongCard!=0){
                passShiZhong(player, cardList, action);
            }else {
                pass(player, cardList, nowDisCard, action);
            }
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
        if (action >= PaohzDisAction.action_peng && action != PaohzDisAction.action_pass
				&& action != PaohzDisAction.action_mo) {
			sendTingInfo(player);
		}

    }

    public synchronized void chui(GlphzPlayer player,int isChui){
        if (chui!=1||player.getChui()!=-1)
            return;
        player.setChui(isChui);
        StringBuilder sb = new StringBuilder("bopi");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append("chui");
        LogUtil.msgLog.info(sb.toString());
        int confirmTime=0;
        for (Map.Entry<Integer, GlphzPlayer> entry : seatMap.entrySet()) {
            entry.getValue().writeComMessage(WebSocketMsgType.res_code_hyshk_broadcast_chui, (int)player.getUserId(),player.getChui());
            if(entry.getValue().getChui()>=0)
                confirmTime++;
        }
        if (confirmTime == getMaxPlayerCount()) {
            checkDeal(player.getUserId());
            //检查起手牌是否需要自动操作
            startNext();
        }
    }











    private boolean setDisPlayer(GlphzPlayer player, int action, boolean isHu) {
        return setDisPlayer(player, action, false, isHu);
    }

    /**
     * 设置要出牌的玩家
     */
    private boolean setDisPlayer(GlphzPlayer player, int action, boolean isFirstDis, boolean isHu) {
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
    private boolean checkActionOld(GlphzPlayer player, int action) {
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
    private boolean checkAction(GlphzPlayer player, int action, List<PaohzCard> cardList, PaohzCard nowDisCard) {
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
            GlphzPlayer tempPlayer = seatMap.get(maxSeat);
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
    private void refreshTempAction(GlphzPlayer player) {
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
                GlphzPlayer tempPlayer = seatMap.get(tempAction.getSeat());
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
    private GlphzPlayer getDisPlayer() {
        return seatMap.get(disCardSeat);
    }

    private void record(GlphzPlayer player, int action, List<PaohzCard> cardList) {
    }

    @Override
    public int isCanPlay() {
        if (getPlayerCount() < getMaxPlayerCount()) {
            return 1;
        }
        // for (HyshkPlayer player : seatMap.values()) {
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
        GlphzPlayer player = seatMap.get(nowDisCardSeat);

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
        StringBuilder sb = new StringBuilder("glphz");
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
            if(shiZhongCard!=0)
                shiZhongCard=0;
            markMoSeat(card, player.getSeat());
            
            setDisCardSeat(player.getSeat());
        	setFinalCard(card.getId());
        	setNowDisCardIds(new ArrayList<>(Arrays.asList(card)));
            setNowDisCardSeat(getNextDisCardSeat());
            
            player.moCard(card);
            PaohuziCheckCardBean autoDisCard = null;
            int nowSeat=player.getSeat();
            
            if(card.isGuiPai()){
                PaohuziCheckCardBean checkCard = player.checkCard(card, true, false);
                if(!checkCard.isZai()&&!checkCard.isTi()&!checkCard.isChouZai())
                	player.setZaiTiHu(false);
                //判断是否强制胡牌
                if (checkCard.isHu()&&biHuType==1&&biHuSeat==0){
                    biHuSeat=nowSeat;
                }
                if(checkCard.isHu()){
                	addAction(checkCard.getSeat(), checkCard.getActionList());
                }
                //不能胡就放到手上
                if (!checkCard.isHu()) {
                	player.getHandPhzs().add(card);
//                	setBeRemoveCard(card);
                	getDisPlayer().removeOutPais(card);
//                	player.removeOutPais(card);
                	player.dealHandPais(player.getHandPhzs());
//                	player.getPlayingTable().changeCards(player.getSeat());
//                	player.changeSeat(player.getSeat());
//                	player.changeTableInfo();
                	setDisPlayer(player, PaohzDisAction.action_mogui, player.isFristDisCard(), false);
                }
                markMoSeat(player.getSeat(), PaohzDisAction.action_mogui);
        		sendActionMsg(player, PaohzDisAction.action_mogui, new ArrayList<>(Arrays.asList(card)), PaohzDisAction.action_type_action);

                if (autoDisBean != null) {
                    playAutoDisCard(autoDisBean);
                }
                checkAutoMo();
                sendTingInfo(player);
                return;
            }else{
            	int i=1;
                while (true){
                    GlphzPlayer p = seatMap.get(nowSeat);
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
                    if (checkCard.isHu()&&biHuType==1&&biHuSeat==0){
                        biHuSeat=nowSeat;
                        break;
                    }
                    nowSeat = calcNextSeat(nowSeat);
                    if(i>=playerCount)
                        break;
                    i++;
                }
            }
            markMoSeat(player.getSeat(), PaohzDisAction.action_mo);
            if (autoDisCard != null && (autoDisCard.getAutoAction() == PaohzDisAction.action_zai||autoDisCard.getAutoAction() == PaohzDisAction.action_ti)) {
                sendMoMsg(player, PaohzDisAction.action_mo, new ArrayList<>(Arrays.asList(card)), PaohzDisAction.action_type_mo,isMoFlag());
            } else {
            	if(card.isGuiPai()){
            		sendActionMsg(player, PaohzDisAction.action_mogui, new ArrayList<>(Arrays.asList(card)), PaohzDisAction.action_type_mo);
            	}else{
            		sendActionMsg(player, PaohzDisAction.action_mo, new ArrayList<>(Arrays.asList(card)), PaohzDisAction.action_type_mo);
            	}
            }

            if (autoDisBean != null) {
                playAutoDisCard(autoDisBean);
            }

            if(biHuSeat!=0){
                GlphzPlayer p = seatMap.get(biHuSeat);
                if(p==null){
                    sb = new StringBuilder("hylhq.mobihu");
                    sb.append("|").append(getId());
                    sb.append("|").append(getPlayBureau());
                    sb.append("|").append(biHuSeat);
                    LogUtil.msgLog.info(sb.toString());
                }else {
                    hu(p,null,PaohzDisAction.action_hu,card,true);
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
    private PaohuziCheckCardBean checkDisAction(GlphzPlayer player, int action, PaohzCard disCard, boolean canBoom) {
        PaohuziCheckCardBean autoDisCheck = new PaohuziCheckCardBean();
        int nowSeat=disCardSeat;
        int i=1;
        while (true){
            nowSeat = calcNextSeat(nowSeat);
            i++;
            GlphzPlayer p = seatMap.get(nowSeat);
            if (p.getUserId()==player.getUserId())
                continue;
            PaohuziCheckCardBean checkCard = p.checkCard(disCard,false,!canBoom,false,canBoom,false);
            boolean check = checkPaohuziCheckCard(checkCard);
            if (check) {
                autoDisCheck = checkCard;
            }
            //判断是否强制胡牌
            if (checkCard.isHu()&&biHuType>=1){
                biHuSeat=nowSeat;
                break;
            }
            if(nowSeat==disCardSeat||i>=playerCount)
                break;
        }
        return autoDisCheck;
    }

    private boolean isFangZhao(GlphzPlayer player, PaohzCard disCard) {

        for (Entry<Integer, GlphzPlayer> entry : seatMap.entrySet()) {
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
    private PaohuziCheckCardBean checkAutoDis(GlphzPlayer player, boolean isMoPaiIng) {
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


    private void sendActionMsg(GlphzPlayer player, int action, List<PaohzCard> cards, int actType) {
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
    private void sendMoMsg(GlphzPlayer player, int action, List<PaohzCard> cards, int actType,boolean ismo) {
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(action);
        if(action == PaohzDisAction.action_hu && ismo){
        	builder.setAction(PaohzDisAction.action_zimo);
        }
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
        // builder.setNextSeat(nowDisCardSeat);
        setNextSeatMsg(builder,false);
        builder.setRemain(leftCards.size());
        builder.addAllPhzIds(PaohuziTool.toPhzCardIds(cards));
        builder.setActType(actType);
        sendMoMsgBySelfAction(builder, player.getSeat());
    }

    /**
     * 发送该玩家动作msg
     */
    private void sendPlayerActionMsg(GlphzPlayer player) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(PaohzDisAction.action_refreshaction);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
        // builder.setNextSeat(nowDisCardSeat);
        setNextSeatMsg(builder,false);
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
        if (player.getSeat()==lastWinSeat&&shuXingSeat>0){
            GlphzPlayer paohuziPlayer = seatMap.get(shuXingSeat);
            paohuziPlayer.writeSocket(builder.build());
        }
    }

    private void setNextSeatMsg(PlayPaohuziRes.Builder builder,boolean shiZhongHu) {
        builder.setTimeSeat(nowDisCardSeat);
        if(qiShouTi){
            if(mingLongType==2){
                builder.setNextSeat(lastWinSeat);
                setNowDisCardSeat(lastWinSeat);
            }else {
                builder.setNextSeat(0);
            }
        } else if (toPlayCardFlag == 1&&!shiZhongHu) {
            builder.setNextSeat(nowDisCardSeat);
        } else{
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
    private void sendActionMsg(GlphzPlayer player, int action, List<PaohzCard> cards, int actType, boolean isZaiPao, boolean isChongPao) {
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(action);
        if(action == PaohzDisAction.action_hu && isSelfMo(player)){
        	builder.setAction(PaohzDisAction.action_zimo);
        }
        if(action == PaohzDisAction.action_hu && disOrMo==1){
        	builder.setAction(PaohzDisAction.action_fangpao);
        }
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
        setNextSeatMsg(builder,false);
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
        GlphzPlayer winPlayer = seatMap.get(lastWinSeat);
        for (GlphzPlayer player : seatMap.values()) {
            PlayPaohuziRes.Builder copy = builder.clone();
            if (player.getSeat() != seat) {
                if(mingWei==0&&(autoDisBean.isTi()||autoDisBean.isZai()||autoDisBean.isChouZai())){
                    copy.clearPhzIds();
                    copy.addPhzIds(0);
                }
            } else {
                copy.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
            }
            if (actionSeatMap.containsKey(player.getSeat())) {
                List<Integer> actionList = getSendSelfAction(zaiKeyValue, player.getSeat(), actionSeatMap.get(player.getSeat()));
                if (actionList != null) {
                    copy.addAllSelfAct(actionList);
                }
            }else if (seat==lastWinSeat&&shuXingSeat==player.getSeat()&&actionSeatMap.containsKey(winPlayer.getSeat())) {
                List<Integer> actionList = getSendSelfAction(zaiKeyValue, winPlayer.getSeat(), actionSeatMap.get(winPlayer.getSeat()));
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
        int paoSeat = 0;
        if (PaohzDisAction.action_type_dis == actType || PaohzDisAction.action_type_mo == actType) {
            for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                if (1 == entry.getValue().get(5)) {
                    noShow = true;
                    paoSeat = entry.getKey();
                }
            }
        }

        GlphzPlayer winPlayer = seatMap.get(lastWinSeat);

        for (GlphzPlayer player : seatMap.values()) {
            PlayPaohuziRes.Builder copy = builder.clone();
            if (copy.getSeat() == player.getSeat()) {
                copy.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
                if(player.isAutoPlay() && copy.getActType() == PaohzDisAction.action_type_dis){
                    copy.setActType(PaohzDisAction.action_type_autoplaydis);
                }
            }
            else if(copy.getSeat()==lastWinSeat&&player.getSeat()==shuXingSeat){
                copy.setHuxi(winPlayer.getOutHuxi() + winPlayer.getZaiHuxi());
                if(winPlayer.isAutoPlay() && copy.getActType() == PaohzDisAction.action_type_dis){
                    copy.setActType(PaohzDisAction.action_type_autoplaydis);
                }
            }

            // 需要特殊处理一下栽
            if (mingWei==0&&(copy.getAction() == PaohzDisAction.action_zai||copy.getAction() == PaohzDisAction.action_ti)) {
                if (copy.getSeat() != player.getSeat()) {
                	if (copy.getSeat()!=lastWinSeat||player.getSeat()!=shuXingSeat){
                        copy.clearPhzIds();
                        List<Integer> ids = new ArrayList<>();
                        ids.add(0);
                        ids.add(0);
                        ids.add(0);
                        if(copy.getAction() == PaohzDisAction.action_ti)
                            ids.add(0);
                        copy.addAllPhzIds(ids);
                    }
                   
                }
            }

            if (actionSeatMap.containsKey(player.getSeat()) && player.getSeat()!=shuXingSeat) {
                List<Integer> actionList = getSendSelfAction(zaiKeyValue, player.getSeat(), actionSeatMap.get(player.getSeat()));
                if (actionList != null) {
                    if (noShow && paoSeat != player.getSeat()) {
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
                StringBuilder sb = new StringBuilder("glphz");
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
    private void checkSendActionMsg(boolean shiZhongHu) {
        if (actionSeatMap.isEmpty()) {
            return;
        }

        PlayPaohuziRes.Builder disBuilder = PlayPaohuziRes.newBuilder();
        GlphzPlayer disPlayer = seatMap.get(disCardSeat);
        if(disPlayer == null){
            return;
        }
        PaohuziResTool.buildPlayRes(disBuilder, disPlayer, 0, null);
        disBuilder.setRemain(leftCards.size());
        disBuilder.setHuxi(disPlayer.getOutHuxi() + disPlayer.getZaiHuxi());
        // disBuilder.setNextSeat(nowDisCardSeat);
        setNextSeatMsg(disBuilder,shiZhongHu);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            PlayPaohuziRes.Builder copy = disBuilder.clone();
            List<Integer> actionList = entry.getValue();
            copy.addAllSelfAct(actionList);
            GlphzPlayer seatPlayer = seatMap.get(entry.getKey());
            seatPlayer.writeSocket(copy.build());
            if (shuXingSeat>0&&seatPlayer.getSeat()==lastWinSeat){
                seatMap.get(shuXingSeat).writeSocket(copy.build());
            }
        }

    }

    /**
     *
     */
    public synchronized void checkAction() {
        int nowSeat=disCardSeat;
        int i=0;
        //三个座位对应的人是否有起手明龙
        PaohuziCheckCardBean[] qiShouTiLong=new PaohuziCheckCardBean[3];

        while (true){
            nowSeat = calcNextSeat(nowSeat);
            i++;
            if(keHuShiZhong==0&&mingLongType!=2&&nowSeat!=lastWinSeat)
                continue;
            GlphzPlayer player = seatMap.get(nowSeat);
            if (player == null) {
                if(i>playerCount)
                    break;
                continue;
            }
            PaohuziCheckCardBean checkCard= player.checkCard(null, true, true, false);
            PaohuziCheckCardBean checkCard1=null;
            if(player.getSeat()!=lastWinSeat&&keHuShiZhong==1){
                checkCard1 = player.checkShiZhongCard(PaohzCard.getPaohzCard(shiZhongCard));
            }
            else {
                checkCard1 = player.checkShiZhongCard(null);
            }
            if(checkCard.isHu()||(checkCard1!=null&&checkCard1.isHu())){
                if(player.getSeat()!=lastWinSeat)
                    shiZhongHu[player.getSeat()-1]=1;
                if(biHuType==1)
                    biHuSeat=player.getSeat();
                huSeat=player.getSeat();
                addAction(player.getSeat(),checkCard.buildHuList());
            }
            //起手提先预存，若无闲家胡示众，再提
            if(checkCard.isTi())
                qiShouTiLong[player.getSeat()-1]=checkCard;
            if(i>=playerCount)
                break;
        }

        if(biHuSeat!=0){
            if(biHuSeat==lastWinSeat){
                hu(seatMap.get(biHuSeat),null,PaohzDisAction.action_hu,null,true);
            }else {
                huShiZhong(seatMap.get(biHuSeat),PaohzCard.getPaohzCard(shiZhongCard));
            }
            return;
        }

        boolean flag=false;
        for (int j = 0; j < shiZhongHu.length; j++) {
            if(shiZhongHu[j]!=0){
                flag=true;
                break;
            }
        }
        //如果不存在闲家胡示众牌，则起手明龙
        if(!flag&&mingLongType==2){
            int seat=calcNextSeat(lastWinSeat);
            while (true){
                PaohuziCheckCardBean checkCard=qiShouTiLong[seat-1];
                if (checkCard!=null&&checkPaohuziCheckCard(checkCard)) {
                    qiShouTi=true;
                    playAutoDisCard(checkCard);
                    tiLong(seatMap.get(seat));
                    qiShouTi=false;
                }
                seat=calcNextSeat(seat);
                if(seat==calcNextSeat(lastWinSeat))
                    break;
            }
        }

        checkSendActionMsg(flag);
        if(flag){
            for(Integer seat:seatMap.keySet()){
                GlphzPlayer player = seatMap.get(seat);
                ComMsg.ComRes msg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_hyshk_shizhongcard,lastWinSeat,shiZhongCard).build();
                player.writeSocket(msg);
            }
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
            GlphzPlayer player = seatMap.get(seat);
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
                GlphzPlayer player = seatMap.get(nextseat);
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
                    GlphzPlayer player = seatMap.get(key);
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
        setNowDisCardIds(new ArrayList<>());
        if(playerCount == 4){
        	setShuXingSeat(calcNextNextSeat(getLastWinSeat()));
        }
        moNum=0;
        disNum=0;
        disOrMo=0;
        biHuSeat=0;
        finishFapai=0;
        isSendPiaoFenMsg=0;
        finalCard=0;
        zimoOrFangpao=0;
        timeNum=0;
        clearTempAction();
        shiZhongCard=0;
        xingCard=0;
        cxCards ="";
        cardXingTuns="";
        shiZhongHu=new int[3];
        huSeat=0;
        wangxingCard=0;
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
            for (GlphzPlayer player : playerMap.values()) {
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
        
        if(playerCount == 4){
        	setShuXingSeat(calcNextNextSeat(lastWinSeat));// 设置数醒的座位号
        }
        
        setDisCardSeat(lastWinSeat);
        setNowDisCardSeat(lastWinSeat);
        setMoSeat(lastWinSeat);
        setToPlayCardFlag(1);
        markMoSeat(null, lastWinSeat);
        List<Integer> copy = new ArrayList<>(PaohuziConstant.cardList);
        if(guipai == 1){
        	copy = new ArrayList<>(PaohuziConstant.guiCardList);
        }
        //洗牌之后发牌

        List<List<PaohzCard>> list;
        
        if(playerCount == 4){//4个人的只发3方牌
        	list = PaohuziTool.fapaiControl(copy, zp ,20,playerCount-1,0);
        }else{
        	list = PaohuziTool.fapaiControl(copy, zp ,20,playerCount,0);
        }
        
        //if(card21==1){
//            list = PaohuziTool.fapaiControl(copy, zp ,20,playerCount,0);
//        }else {
//            list = PaohuziTool.fapai(copy, zp ,14,playerCount);
//        }

//
//        int seat=lastWinSeat;
//        for (int i = 0; i < playerCount; i++) {
//            GlphzPlayer player = seatMap.get(seat);
//            player.changeState(player_state.play);
//            player.getFirstPais().clear();
//            // 数醒不发牌,设置为空List
//            if (player.getSeat() == shuXingSeat) {
//                player.dealHandPais(new ArrayList<PaohzCard>());
//                continue;
//            }
//            player.dealHandPais(list.get(i));
//            player.getFirstPais().addAll(PaohuziTool.toPhzCardIds(new ArrayList(list.get(i))));
//            seat=calcNextSeat(seat);
//            if (!player.isAutoPlay()) {
//                player.setAutoPlay(false, this);
//                player.setLastOperateTime(System.currentTimeMillis());
//            }
//            StringBuilder sb = new StringBuilder("glphz");
//            sb.append("|").append(getId());
//            sb.append("|").append(getPlayBureau());
//            sb.append("|").append(player.getUserId());
//            sb.append("|").append(player.getSeat());
//            sb.append("|").append(player.getName());
//            sb.append("|").append("fapai");
//            sb.append("|").append(player.getHandPhzs());
//            LogUtil.msgLog.info(sb.toString());
//        }
        
        int i = 1;
        for (GlphzPlayer player : playerMap.values()) {
            player.changeState(player_state.play);
            player.getFirstPais().clear();
            if (player.getSeat() == lastWinSeat) {
                player.dealHandPais(list.get(0));
				player.getFirstPais().addAll(PaohuziTool.toPhzCardIds(new ArrayList(list.get(0))));// 将初始手牌保存结算时发给客户端
                continue;
            }
			// 数醒不发牌,设置为空List
            if (player.getSeat() == shuXingSeat) {
                player.dealHandPais(new ArrayList<PaohzCard>());
                continue;
            }
            player.dealHandPais(list.get(i));
			player.getFirstPais().addAll(PaohuziTool.toPhzCardIds(new ArrayList(list.get(i))));// 将初始手牌保存结算时发给客户端
            i++;

            StringBuilder sb = new StringBuilder("glphz");
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

            StringBuilder sb = new StringBuilder("glphz");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append("chouPai");
            sb.append("|").append(chuPaiList);
            LogUtil.msgLog.info(sb.toString());
        }
        //桌上所有剩余牌
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
        if (nextSeat == shuXingSeat) {
            nextSeat = nextSeat + 1 > playerCount ? 1 : nextSeat + 1;
        }
        return nextSeat;
    }

    /**
     * 计算seat前面的座位
     */
    public int calcFrontSeat(int seat) {
    	 int frontSeat = seat - 1 < 1 ? playerCount : seat - 1;
         if (frontSeat == shuXingSeat) {
             frontSeat = frontSeat - 1 < 1 ? playerCount : frontSeat - 1;
         }
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
        for (GlphzPlayer player : playerMap.values()) {
            PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(userId, isrecover);
            if (playerRes==null){
                continue;
            }
            //是否为庄
            playerRes.addRecover((player.getSeat() == lastWinSeat) ? 1 : 0);
            if (player.getUserId() == userId) {

                if (player.getSeat()==shuXingSeat){
                	GlphzPlayer winPlayer = seatMap.get(lastWinSeat);

                    playerRes.addAllHandCardIds(winPlayer.getHandPais());
                    if (actionSeatMap.containsKey(winPlayer.getSeat())) {
                        List<Integer> actionList = getSendSelfAction(zaiKeyValue, winPlayer.getSeat(), actionSeatMap.get(winPlayer.getSeat()));
                        if (actionList != null) {
                            playerRes.addAllRecover(actionList);
                        }
                    }
                }else{
                	  playerRes.addAllHandCardIds(player.getHandPais());
                      if (actionSeatMap.containsKey(player.getSeat())) {
                          List<Integer> actionList = getSendSelfAction(zaiKeyValue, player.getSeat(), actionSeatMap.get(player.getSeat()));
                          if (actionList != null && !tempActionMap.containsKey(player.getSeat()) && !huConfirmList.contains(player.getSeat())) {
                              playerRes.addAllRecover(actionList);
                          }
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
        GlphzPlayer winPlayer = null;

        //大结算锤分
        if(over&&chui==1){
            for (GlphzPlayer player : seatMap.values()) {
                player.setTotalPoint(player.getWinLostPoint());
            }
        }
        //大结算计算加倍分
        if(over && jiaBei == 1){
            int jiaBeiPoint = 0;
            int loserCount = 0;
            for (GlphzPlayer player : seatMap.values()) {
                if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                    jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
                    player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
                } else if (player.getTotalPoint() < 0) {
                    loserCount++;
                }
            }
            if (jiaBeiPoint > 0) {
                for (GlphzPlayer player : seatMap.values()) {
                    if (player.getTotalPoint() < 0) {
                        player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                    }
                }
            }

        }

        //大结算低于below分+belowAdd分
        if(over&&belowAdd>0&&playerMap.size()==2){
            for (GlphzPlayer player : seatMap.values()) {
                int totalPoint = player.getTotalPoint();
                if (totalPoint >-below&&totalPoint<0) {
                    player.setTotalPoint(player.getTotalPoint()-belowAdd);
                }else if(totalPoint < below&&totalPoint>0){
                    player.setTotalPoint(player.getTotalPoint()+belowAdd);
                }
            }
        }

        List<PhzHuCardList> phzHuCardListBui = new ArrayList<>();
        for (GlphzPlayer player : seatMap.values()) {
            if (winList != null && winList.contains(player.getSeat())) {
                winPlayer = seatMap.get(player.getSeat());
            }
            ClosingPhzPlayerInfoRes.Builder build;
            //总分大结算
//
            build = player.bulidTotalClosingPlayerInfoRes();
            if(playerCount == 4){
            	 build.setIsShuXing(shuXingSeat);
            }
            
            build.addAllFirstCards(player.getFirstPais());//将初始手牌装入网络对象
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
            
            PhzHuCardList.Builder phzHuCardListBuider = PhzHuCardList.newBuilder();
        	phzHuCardListBuider.setSeat(player.getSeat());
        	phzHuCardListBuider.addAllPhzCard(player.buildNormalPhzHuCards());
        	phzHuCardListBui.add(phzHuCardListBuider.build());
        }

        //信用分计算
        if (isCreditTable()) {
            //计算信用负分
            calcNegativeCredit();

            long dyjCredit = 0;
            for (GlphzPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
                GlphzPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);
                builder.addStrExt(player.getWinLoseCredit() + "");      //8
                builder.addStrExt(player.getCommissionCredit() + "");   //9
                // 2019-02-26更新
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (GlphzPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
                GlphzPlayer player = seatMap.get(builder.getSeat());
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
            GlphzPlayer player = seatMap.get(builder.getSeat());
            builder.addStrExt(player.getChui()+"");//10
            list.add(builder.build());
        }

        ClosingPhzInfoRes.Builder res = ClosingPhzInfoRes.newBuilder();
        res.addAllLeftCards(PaohuziTool.toPhzCardIds(leftCards));
//        if (mt != null) {
//            res.addAllFanTypes(mt);
//        }
        res.addAllAllCardsCombo(phzHuCardListBui);
        res.addFanTypes(zimoOrFangpao);
        if (winPlayer != null) {
            res.setTun(totalTun);// 剥皮算0等
            res.setFan(winFen);
//            if(mt.contains(PaohuziMingTangRule.LOUDI_MINGTANG_PIAOHU)){
//                res.setHuxi(15);
//            }else {
                res.setHuxi(winPlayer.getTotalHu());
//            }
            res.setTotalTun(totalTun);
            res.setHuSeat(winPlayer.getSeat());
//            if (winPlayer.getHu() != null && winPlayer.getHu().getCheckCard() != null) {
            res.setHuCard(finalCard);
//            }
            res.addAllCards(winPlayer.buildPhzHuCards());
        }
        res.addAllClosingPlayers(list);
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.addAllExt(buildAccountsExt(over,xingTun));
        res.addAllStartLeftCards(startLeftCards);
        res.addAllIntParams(getIntParams());
        
        for (GlphzPlayer player : seatMap.values()) {
        	player.writeSocket(res.build());
        }
        if (over && isGroupRoom() && !isCreditTable()) {
            res.setGroupLogId((int) saveUserGroupPlaylog());
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
        ext.add(creditMode + ""); //14
        ext.add(creditJoinLimit + "");//15
        ext.add(creditDissLimit + "");//16
        ext.add(creditDifen + "");//17
        ext.add(creditCommission + "");//18
        ext.add(creditCommissionMode1 + "");//19
        ext.add(creditCommissionMode2 + "");//20
        ext.add(autoPlay ? "1" : "0");//21
        ext.add(jiaBei + "");//22
        ext.add(jiaBeiFen + "");//23
        ext.add(jiaBeiShu + "");//24
        ext.add(xingCard+"");//25
        ext.add(xingTun+"");//26
        ext.add(cxCards);// 27
        ext.add(cardXingTuns);// 28
        ext.add(wangxingCard+""); // 29

        ext.add(lastWinSeat + ""); // 30
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
        sendTingInfo((GlphzPlayer) player);
    }

    @Override
    public void checkAutoPlay() {
        synchronized (this){
            if(!autoPlay)
                return;
            if (getSendDissTime() > 0) {
                for (GlphzPlayer player : seatMap.values()) {
                    if (player.getLastCheckTime() > 0) {
                        player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                    }
                }
                return;
            }
            if (isAutoPlayOff()) {
				// 托管关闭
				for (int seat : seatMap.keySet()) {
					GlphzPlayer player = seatMap.get(seat);
					player.setAutoPlay(false, this);
					player.setLastOperateTime(System.currentTimeMillis());
				}
				return;
			}
            if (state == table_state.ready&& playedBureau > 0) {
                ++timeNum;
                int i=0;
                for (GlphzPlayer player : seatMap.values()) {
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
            //选锤，还没发牌
            if (finishFapai == 0&&chui==1&&playedBureau==0) {
                for (GlphzPlayer player : seatMap.values()) {
                    if(player.getChui()>=0){
                        continue;
                    }
                    boolean auto = checkPlayerAuto(player, timeout);
                    if (auto)
                        chui(player, 0);
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
                    GlphzPlayer player = seatMap.get(seat);
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
                GlphzPlayer player = seatMap.get(nowDisCardSeat);
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



    public boolean checkPlayerAuto(GlphzPlayer player ,int timeout){
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
        return GlphzPlayer.class;
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
    public int getNextCardValNorm() {
    	if (this.leftCards.size() > 0) {
    		PaohzCard card =  leftCards.get(0);
    		if(card != null){
    			return card.getVal();
    		}
    	}else{
    		return PaohzCard.getPaohzCard(finalCard).getVal();
    	}
    	return 0;
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
    public int getFinalCard() {
    	return finalCard;
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
    public boolean isMoByPlayer(GlphzPlayer player) {
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
//        // 添加剥皮上限
//        if (PaohuziConstant.isPlayBopi(play)) {
//            if (params.size()>= 11) {
//                setCeiling(params.get(10));
//            }
////            setCeiling(150);
//        }
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

            for (GlphzPlayer player:playerMap.values()){
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

            for (GlphzPlayer player:playerMap.values()){
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
        for (GlphzPlayer player : seatMap.values()) {
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
        return "桂林跑胡子";
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

    @Override
    public synchronized void setReplenishParams(Player player, List<Integer> intParams, List<String> strParams) {
    }

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_guilin_paohuzi);

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

    @Override
    public boolean  isNeedFromOverPop(){
        if(biHuType==1)
            return false;
        return true;
    }

    @Override
    public boolean isAllReady() {
        if (getPlayerCount() < getMaxPlayerCount()) {
            return false;
        }
        for (Player player : getSeatMap().values()) {
            if(!player.isRobot()){
                if(chui>0){
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
        if (chui==1&&playedBureau==0) {
            boolean chuiOver = true;
            for (GlphzPlayer player : playerMap.values()) {
                if(player.getChui()<0){
                    chuiOver = false;
                    break;
                }
            }
            if(!chuiOver){
                if (finishFapai==0) {
                    LogUtil.msgLog.info("Glphz|sendChui|" + getId() + "|" + getPlayBureau());
                    ComMsg.ComRes msg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_sybp_chui).build();
                    for (GlphzPlayer player : playerMap.values()) {
                        if(player.getChui()<0){
                            player.writeSocket(msg);
                        }
                    }
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean ready(Player player) {
        boolean flag=super.ready(player);
        if(chui!=1||playedBureau>0)
            return flag;
        int count=0;
        for(GlphzPlayer p:seatMap.values()){
            if (p.getState() == player_state.ready||p.getState() == player_state.play)
                count++;
        }
        if(count==getMaxPlayerCount()){
            for(GlphzPlayer p:seatMap.values()){
                p.setChui(-1);
            }
        }
        return flag;
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
        json.put("wanFa", "桂林跑胡子");
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

    public int getShuXingSeat() {
        return shuXingSeat;
    }

    public void setShuXingSeat(int shuXingSeat) {
        this.shuXingSeat = shuXingSeat;
    }
    
    public Boolean[] getXingTypes(){
    	return xingszxTypes;
    }

	public int getXingType() {
		return xingType;
	}
    
}
