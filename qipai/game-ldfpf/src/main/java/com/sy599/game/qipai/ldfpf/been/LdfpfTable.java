package com.sy599.game.qipai.ldfpf.been;

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
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

import com.sy599.game.common.bean.CreateTableInfo;
import com.sy599.game.msg.serverPacket.TablePhzResMsg;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;

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
import com.sy599.game.db.bean.PlayLogTable;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserGroupPlaylog;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.dao.DataStatisticsDao;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayPaohuziRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzInfoRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.ldfpf.constant.PaohuziConstant;
import com.sy599.game.qipai.ldfpf.constant.PaohzCard;
import com.sy599.game.qipai.ldfpf.rule.PaohuziIndex;
import com.sy599.game.qipai.ldfpf.rule.PaohuziMingTangRule;
import com.sy599.game.qipai.ldfpf.rule.PaohzCardIndexArr;
import com.sy599.game.qipai.ldfpf.rule.RobotAI;
import com.sy599.game.qipai.ldfpf.tool.PaohuziHuLack;
import com.sy599.game.qipai.ldfpf.tool.PaohuziResTool;
import com.sy599.game.qipai.ldfpf.tool.PaohuziTool;
import com.sy599.game.staticdata.KeyValuePair;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class LdfpfTable extends BaseTable {
	/*** 玩家map */
    private Map<Long, LdfpfPlayer> playerMap = new ConcurrentHashMap<>();
	/*** 座位对应的玩家 */
    private Map<Integer, LdfpfPlayer> seatMap = new ConcurrentHashMap<>();
    /**
	 * 开局所有底牌
	 **/
    private volatile List<Integer> startLeftCards = new ArrayList<>();
    /**
	 * 当前桌面底牌
	 **/
    private volatile List<PaohzCard> leftCards = new ArrayList<>();
	/*** 摸牌flag */
    private volatile int moFlag;
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
    private volatile boolean firstCard;
    private volatile int disNum=0;
    private volatile int shuXingSeat = 0;
    private volatile int ceiling =0;
	// 是否可放炮标志位，当pass操作后置为false,出牌操作需要，置为true
    private volatile boolean boomFlag=false;
	// 封顶分数 0或者null为不封顶 200或400
    private Integer capping=200;
	// 是否勾选首局庄家随机
    private boolean bankerRand=false;
	// 起胡数量
    private int floorValue=15;
	// 是否飘胡
    private boolean isPiaoHu=false;
	// 是否抽排
    private int chouPai=0;
	// 抽牌牌堆
    List<Integer> chouCards=new ArrayList<>();
	// 是否翻倍
    private boolean isDouble=false;
	// 翻倍上线
    private int dScore=0;
	// 翻倍倍数
    private int doubleNum=1;
	// 放炮必胡
    private boolean boomMustHu=true;
    //低于below加分
    private int belowAdd=0;
    private int below=0;

    /**
	 * 0胡 1碰 2栽 3提 4吃 5跑 6臭栽
	 */
    private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
    private volatile List<PaohzCard> nowDisCardIds = new ArrayList<>();
	// 红黑点
    private volatile int isRedBlack;
	// 可连庄
    private volatile int isLianBanker;

	private volatile int catCardCount = 0;// 抽掉的牌数量


    /**
	 * 托管时间
	 */
    private volatile int autoTimeOut = Integer.MAX_VALUE;
    private volatile int autoTimeOut2 = Integer.MAX_VALUE;
    private volatile int autoTimeOut3 = Integer.MAX_VALUE;

	// 是否加倍：0否，1是
    private int jiaBei;
	// 加倍分数：低于xx分进行加倍
    private int jiaBeiFen;
	// 加倍倍数：翻几倍
    private int jiaBeiShu;
	// 打鸟类型：0，不打鸟，1胡息打鸟，2分数打鸟，3局内打鸟
    private int daNiaoType=0;
	// 打鸟分
    private volatile int daNiaoFen=0;
	// 确认打鸟次数
    private volatile int confirmTime=0;
    private volatile int timeNum = 0;
    // 首局洗开关;
    private int firstXiPai=0;
    // 黄庄次数
    private int huangZhuangTimes=0;
    
    
	/** 托管1：单局，2：全局 */
    private int autoPlayGlob;
	private int autoTableCount;
    private volatile int lockHandNum=0;
    private int boomHuSeat=0;
    /**
     * 玩家位置对应临时操作 当同时存在多个可做的操作时 1当优先级最高的玩家最先操作 则清理所有操作提示 并执行该操作
     * 2当操作优先级低的玩家先选择操作，则玩家先将所做操作存入临时操作中 当玩家优先级最高的玩家操作后 在判断临时操作是否可执行并执行
     */
    private Map<Integer, TempAction> tempActionMap = new ConcurrentHashMap<>();
    //当可以跑胡时为1，用于日志标记处理。
    private int paoHu=0;
    //
    private int finishFapai=0;
    private int finishDaNiao =0;



    @Override
    public boolean createTable(CreateTableInfo createTableInfo) throws Exception {
        Player player = createTableInfo.getPlayer();
        int play = createTableInfo.getPlayType();
        int bureauCount =createTableInfo.getBureauCount();
        int tableType = createTableInfo.getTableType();
        List<Integer> params = createTableInfo.getIntParams();
        List<String> strParams = createTableInfo.getStrParams();
        boolean saveDb = createTableInfo.isSaveDb();
        long id = getCreateTableId(player.getUserId(), play);
        if (id<=0){
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
        int playerCount = StringUtil.getIntValue(params, 7, 3);// 比赛人数
        int payType = StringUtil.getIntValue(params, 9, 0);// 房费方式
        int ceiling = StringUtil.getIntValue(params, 10, 0);// 单局胡息封顶
        int isRedBlack = 1;// 娄底放炮罚默认开启名堂
        int isLianBanker = 1;// 默认可连庄
        // int xiTotun=StringUtil.getIntValue(params, 13, 0);//息屯算法
        int floorValue = StringUtil.getIntValue(params, 13, 15);// 起胡下限
        int catCardCount = StringUtil.getIntValue(params, 14, 0);// 除掉的牌数量
        int time=(StringUtil.getIntValue(params, 23, 0));
        if(time ==1) {
            time =60;
        }
        if(time>0) {
            this.autoPlay =true;
        }

        this.jiaBei = StringUtil.getIntValue(params, 24, 0);
        this.jiaBeiFen = StringUtil.getIntValue(params, 25, 100);
        this.jiaBeiShu = StringUtil.getIntValue(params, 26, 1);
        // 是否首局庄家随机
        int bankerRand=StringUtil.getIntValue(params, 27, 0);
        if(bankerRand==1){
            this.bankerRand=true;
            setLastWinSeat(new Random().nextInt(playerCount) + 1);
        }
        // 是否飘胡
        int isPiaoHu=StringUtil.getIntValue(params, 28, 0);
        if(isPiaoHu==1){
            this.isPiaoHu=true;
        }
        if(playerCount==2){
            this.chouPai=StringUtil.getIntValue(params, 29, 0);
            this.isDouble=StringUtil.getIntValue(params, 30, 0)==1?true:false;
            this.dScore=StringUtil.getIntValue(params, 31, 0);
            this.doubleNum=StringUtil.getIntValue(params, 32, 0);
        }
        int boomMustHu=StringUtil.getIntValue(params, 33, 0);
        this.boomMustHu=boomMustHu==1?true:false;
//        if(playerCount==2){
        this.daNiaoType=StringUtil.getIntValue(params, 34, 0);
//        }
        if (daNiaoType==2){
            this.daNiaoFen=StringUtil.getIntValue(params, 35, 0);
        }

        autoPlayGlob=StringUtil.getIntValue(params, 36, 0);
        setPlayerCount(playerCount);
        if(getMaxPlayerCount()==2){
            int belowAdd = StringUtil.getIntValue(params, 37, 0);
            if(belowAdd<=100&&belowAdd>=0)
                this.belowAdd=belowAdd;
            int below = StringUtil.getIntValue(params, 38, 0);
            if(below<=100&&below>=0){
                this.below=below;
                if(belowAdd>0&&below==0)
                    this.below=10;
            }
        }

        firstXiPai = StringUtil.getIntValue(params, 39, 0);


        if (playerCount<=1||playerCount>4){
            return false;
        }
        if(playerCount == 3 || playerCount == 4){
            catCardCount = 0 ;
        }
        this.catCardCount = catCardCount;
        if(this.getMaxPlayerCount() != 2){
            jiaBei = 0 ;
        }
        setPayType(payType);
        this.capping=ceiling;
        if (PaohuziConstant.isPlayBopi(play)){
            setCeiling(ceiling);
        }
        setIsRedBlack(isRedBlack);
        setIsLianBanker(isLianBanker);
        if (floorValue!=0)
            setFloorValue(floorValue);
        if(autoPlay)
            autoTimeOut3 = autoTimeOut2=autoTimeOut = time*1000;

        firstCard=true;
        changeExtend();
        LogUtil.msgLog.info("createTable tid:"+getId()+" "+player.getName() + " params"+params.toString());
        return true;
    }


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
        String autoDisPhz = wrapper.getString(6);
        if (!StringUtils.isBlank(autoDisPhz)) {
            autoDisBean = new PaohuziCheckCardBean();
            autoDisBean.initAutoDisData(autoDisPhz);
        }
        zaiCard = PaohzCard.getPaohzCard(wrapper.getInt(7, 0));
        sendPaoSeat = wrapper.getInt(8, 0);
        firstCard = wrapper.getInt(9, 0) == 1 ? true : false;
        beRemoveCard = PaohzCard.getPaohzCard(wrapper.getInt(10, 0));
        shuXingSeat = wrapper.getInt(11, 0);
        playerCount = wrapper.getInt(12, 3);
        startLeftCards = loadStartLeftCards(wrapper.getString("startLeftCards"));
        ceiling = wrapper.getInt(13, 0);
        isRedBlack = wrapper.getInt(14, 0);
        isLianBanker = wrapper.getInt(15, 0);
//        xiTotun = wrapper.getInt(16, 3);
        if (payType== -1) {
            String isAAStr =  wrapper.getString("isAAConsume");
            if (!StringUtils.isBlank(isAAStr)) {
                this.payType = Boolean.parseBoolean(wrapper.getString("isAAConsume"))?1:2;
            } else {
                payType=1;
            }
        }

        catCardCount = wrapper.getInt("catCardCount", catCardCount);

        jiaBei = wrapper.getInt(17, 0);
        jiaBeiFen = wrapper.getInt(18, 0);
        jiaBeiShu = wrapper.getInt(19, 0);
        daNiaoType = wrapper.getInt(20, 0);
        daNiaoFen = wrapper.getInt(21, 0);
        confirmTime = wrapper.getInt(22, 0);
        boomFlag=wrapper.getInt(25, 0)==1;
        capping=wrapper.getInt(26, 0);
        bankerRand=wrapper.getInt(27, 0)==1;
        int i28 = wrapper.getInt(28, 0);
        if(i28!=0)
            floorValue=i28;
        isPiaoHu=wrapper.getInt(29, 0)==1;
        chouPai=wrapper.getInt(30, 0);
        isDouble=wrapper.getInt(31, 0)==1;
        dScore=wrapper.getInt(32, 0);
        doubleNum=wrapper.getInt(33, 0);
        boomMustHu=wrapper.getInt(34, 0)==1;
        disNum=wrapper.getInt(35,0);

        String chouCardsStr = wrapper.getString(36);
        if (StringUtils.isNotBlank(chouCardsStr)) {
            chouCards = StringUtil.explodeToIntList(chouCardsStr);
        }
        lockHandNum=wrapper.getInt(37,0);

        autoPlayGlob = wrapper.getInt(38,0);
        autoTimeOut = wrapper.getInt(39,0);
        if(autoPlay&&autoTimeOut<=1) {
            autoTimeOut = 60000;
        }
        tempActionMap = loadTempActionMap(wrapper.getString(40));
        below = wrapper.getInt(41,0);
        belowAdd = wrapper.getInt(42,0);
        paoHu = wrapper.getInt(43,0);
        finishFapai = wrapper.getInt(44,0);
        firstXiPai = wrapper.getInt(45,0);
        finishDaNiao = wrapper.getInt(46,0);
        huangZhuangTimes = wrapper.getInt(47,0);
    }

    @Override
    public JsonWrapper buildExtend0(JsonWrapper wrapper) {
//		JsonWrapper wrapper = new JsonWrapper("");
        wrapper.putString(1, StringUtil.implode(huConfirmList, ","));
        wrapper.putInt(2, moFlag);
        wrapper.putInt(3, toPlayCardFlag);
        wrapper.putInt(4, moSeat);
        if (moSeatPair != null) {
            String moSeatPairVal = moSeatPair.getId() + "_" + moSeatPair.getValue();
            wrapper.putString(5, moSeatPairVal);
        }
        if (autoDisBean != null) {
            wrapper.putString(6, autoDisBean.buildAutoDisStr());

        } else {
            wrapper.putString(6, "");
        }
        if (zaiCard != null) {
            wrapper.putInt(7, zaiCard.getId());
        }
        wrapper.putInt(8, sendPaoSeat);
        wrapper.putInt(9, firstCard ? 1 : 0);
        if (beRemoveCard != null) {
            wrapper.putInt(10, beRemoveCard.getId());
        }
        wrapper.putInt(11, shuXingSeat);
        wrapper.putInt(12, playerCount);
        wrapper.putString("startLeftCards", startLeftCardsToJSON());
        wrapper.putInt(13, ceiling);
        wrapper.putInt(14, isRedBlack);
        wrapper.putInt(15, isLianBanker);
//        wrapper.putInt(16, xiTotun);
        wrapper.putInt("catCardCount", catCardCount);
        wrapper.putInt(17, jiaBei);
        wrapper.putInt(18, jiaBeiFen);
        wrapper.putInt(19, jiaBeiShu);
        wrapper.putInt(20, daNiaoType);
        wrapper.putInt(21, daNiaoFen);
        wrapper.putInt(22, confirmTime);
        wrapper.putInt(25, boomFlag?1:0);
        wrapper.putInt(26, capping);
        wrapper.putInt(27, bankerRand?1:0);
        wrapper.putInt(28, floorValue);
        wrapper.putInt(29, isPiaoHu?1:0);
        wrapper.putInt(30, chouPai);
        wrapper.putInt(31, isDouble?1:0);
        wrapper.putInt(32, dScore);
        wrapper.putInt(33, doubleNum);
        wrapper.putInt(34, boomMustHu?1:0);
        wrapper.putInt(35, disNum);
        wrapper.putString(36, StringUtil.implode(chouCards, ","));
        wrapper.putInt(37, lockHandNum);
        wrapper.putInt(38, autoPlayGlob);
        wrapper.putInt(39, autoTimeOut);

        JSONArray tempJsonArray = new JSONArray();
        for (int seat : tempActionMap.keySet()) {
            tempJsonArray.add(tempActionMap.get(seat).buildData());
        }
        wrapper.putString(40, tempJsonArray.toString());
        wrapper.putInt(41, below);
        wrapper.putInt(42, belowAdd);
        wrapper.putInt(43, paoHu);
        wrapper.putInt(44, finishFapai);
        wrapper.putInt(45, firstXiPai);
        wrapper.putInt(46, finishDaNiao);
        wrapper.putInt(47,huangZhuangTimes);
        return wrapper;
    }



    public boolean getFirstCard(){
        return firstCard;
    }

    public int getIsRedBlack() {
        return isRedBlack;
    }

    public void setIsRedBlack(int isRedBlack) {
        this.isRedBlack = isRedBlack;
    }

    public int getIsLianBanker() {
        return isLianBanker;
    }

    public void setIsLianBanker(int isLianBanker) {
        this.isLianBanker = isLianBanker;
    }

    public int getCeiling() {
        return ceiling;
    }

    public void setCeiling(int ceiling) {
        this.ceiling = ceiling;
        changeExtend();
    }

    public boolean isPiaoHu() {
        return isPiaoHu;
    }

    public void setPiaoHu(boolean piaoHu) {
        isPiaoHu = piaoHu;
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

    public List<Integer> getChouCards() {
        return chouCards;
    }

    public void setChouCards(List<Integer> chouCards) {
        this.chouCards = chouCards;
    }

    @Override
    public int getIsShuffling() {
        return super.getIsShuffling();
    }

    /**
	 * 娄底放炮罚胡牌结算
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
        List<Integer> winList = new ArrayList<>(huConfirmList);
        if ((winList.size() == 0 && leftCards.size() == 0)||lockHandNum==getMaxPlayerCount()) {
			// 流局
            isHuangZhuang = true;
        }
        List<Integer> mt = null;
        int winFen = 0;
        boolean isOver = playBureau == totalBureau;
        Map<Long,Integer> outScoreMap = new HashMap<>();
        Map<Long,Integer> ticketMap = new HashMap<>();
        int nextBankerSeat = lastWinSeat;
        for (int winSeat : winList) {
            // 赢的玩家
            boolean isSelfMo = winSeat == moSeat;
            LdfpfPlayer winPlayer = seatMap.get(winSeat);
            winPlayer.changeAction(PaohuziConstant.ACTION_COUNT_INDEX_HU, 1);
            if(isSelfMo){
                winPlayer.changeAction(PaohuziConstant.ACTION_COUNT_INDEX_ZIMO, 1);
            }
            mt = winPlayer.getHu().getMingTang();
            winFen = PaohuziMingTangRule.calcMingTangFen(winPlayer.getTotalHu(), mt,capping,boomFlag);
            if (capping>0&&winFen>=capping){
                winFen=capping;
            }
            for (int seat : seatMap.keySet()) {
                LdfpfPlayer player = seatMap.get(seat);
                if (!winList.contains(seat)) {
                    // 如果放炮，则单独扣分
                    if(boomFlag&&disCardSeat==seat){
                        if(boomFlag&&winFen>=100){
                            // 放炮最多胡100
                            winFen=100;
                        }
                        player.calcResult(this, 1, -winFen, isHuangZhuang);
                    }
                    player.calcResult(this, 1, 0, isHuangZhuang);
                }else {
                    winPlayer.calcResult(this, 1, winFen, isHuangZhuang);
                }
                if(player.getTotalPoint()>=100){
                    isOver=true;
                    changeTableState(table_state.over);
                }
            }
            nextBankerSeat = winSeat;
        }
        if(isHuangZhuang){
            // 荒庄，庄家-10息
            for (int seat : seatMap.keySet()) {
                if (seat==lastWinSeat) {
                    seatMap.get(seat).calcResult(this, 0, -10, isHuangZhuang);
                    StringBuilder sb=new StringBuilder();
                    sb.append("|").append("ldfpf.huangzhuang");
                    sb.append("|").append(getId());
                    sb.append("|").append(getPlayBureau());
                    sb.append("|").append(lastWinSeat);
                    sb.append("|").append(seatMap.get(seat).getUserId());
                    sb.append("|").append(lastWinSeat);
                }
            }
            huangZhuangTimes++;
            if (huangZhuangTimes >= 6){ //黄庄6次就结束
                isOver = true;
            }
        }

        for (int i = 1; i < playerCount+1; i++) {
            for (int j = i+1; j < playerCount+1; j++) {
                if (i!=j){
                    countBetweenTwoPoint(seatMap.get(i),seatMap.get(j));
                }
            }
        }

        if(autoPlayGlob >0) {
			// //是否解散
            boolean diss = false;
            if(autoPlayGlob ==1) {
            	 for (LdfpfPlayer seat : seatMap.values()) {
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

        if (isOver) {
            calcPointBeforeOver();
        } else {
            for (LdfpfPlayer player : seatMap.values()) {
                int winLosePoint = getLdfpfPoint(player, isOver);
                player.setWinLossPoint(winLosePoint);
            }
        }

        // -----------金币场---------------------------------
        if (isGoldRoom()) {
            for (LdfpfPlayer player : seatMap.values()) {
                player.setPoint(player.getWinLossPoint());
                player.setWinGold(player.getWinLossPoint());
            }
            calcGoldRoom();
        }
        
        
        calcAfter();
        ClosingPhzInfoRes.Builder res = sendAccountsMsg(isOver, winList, winFen, mt, winFen, false, outScoreMap,ticketMap);
        saveLog(isOver,0L, res.build());

        setLastWinSeat(nextBankerSeat);
        if (isOver) {
            initstatus();
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

    public void calcPointBeforeOver() {
        // 大结算计算加倍分
        if (jiaBei == 1) {
            int jiaBeiPoint = 0;
            int loserCount = 0;
            for (LdfpfPlayer player : seatMap.values()) {
                if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                    jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
                    player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
                } else if (player.getTotalPoint() < 0) {
                    loserCount++;
                }
            }
            if (jiaBeiPoint > 0) {
                for (LdfpfPlayer player : seatMap.values()) {
                    if (player.getTotalPoint() < 0) {
                        player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                    }
                }
            }

        }

        for (LdfpfPlayer player : seatMap.values()) {
            int winLosePoint = getLdfpfPoint(player, true);
            player.setWinLossPoint(winLosePoint);
        }

        //大结算低于below分+belowAdd分
        if (belowAdd > 0 && playerMap.size() == 2) {
            for (LdfpfPlayer player : seatMap.values()) {
                int totalPoint = player.getWinLossPoint();
                if (totalPoint > -below && totalPoint < 0) {
                    player.setWinLossPoint(player.getWinLossPoint() - belowAdd);
                } else if (totalPoint < below && totalPoint > 0) {
                    player.setWinLossPoint(player.getWinLossPoint() + belowAdd);
                }
            }
        }
    }

    private boolean checkAuto3() {
		boolean diss = false;
		// if(autoPlayGlob==3) {
		boolean diss2 = false;
		for (LdfpfPlayer seat : seatMap.values()) {
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
            for (LdfpfPlayer player : playerMap.values()) {
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
        if(finishFapai==0)
            return;
		// 天胡或者暗杠
//        int lastCardIndex = RandomUtils.nextInt(21);
        LdfpfPlayer winPlayer = seatMap.get(lastWinSeat);

        for (LdfpfPlayer tablePlayer : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            res.addAllHandCardIds(tablePlayer.getSeat() == shuXingSeat?winPlayer.getHandPais():tablePlayer.getHandPais());
            res.setNextSeat(lastWinSeat);
			res.setGameType(getWanFa());// 1跑得快 2麻将
            res.setRemain(leftCards.size());
            res.setBanker(lastWinSeat);
            res.addXiaohu(winPlayer.getHandPais().get(0));
            if(tablePlayer.isAutoPlay()) {
         		 addPlayLog(tablePlayer.getSeat(), PaohzDisAction.action_tuoguan + "",1 + "");
          	}
            addPlayLog(tablePlayer.getSeat(), PaohzDisAction.action_daniao + "", tablePlayer.getNiaoFenNonnegative()+"");
            tablePlayer.writeSocket(res.build());
            tablePlayer.writeComMessage(WebSocketMsgType.req_code_daniao_return);
        }
        sendZpLeftCards(-1);
    }

    @Override
    public synchronized void startNext() {
        for(LdfpfPlayer player : seatMap.values()){     //重置进入托管的计时
            if (!player.isAutoPlay()) {
                player.setAutoPlay(false, this);
                player.setLastOperateTime(System.currentTimeMillis());
            }
        }
        checkAction();
    }

    public void play(LdfpfPlayer player, List<Integer> cardIds, int action) {
        play(player, cardIds, action, false, false, false);
    }

    private void hu(LdfpfPlayer player, List<PaohzCard> cardList, int action, PaohzCard nowDisCard,boolean system) {
        PaohzCard disCard = nowDisCard == null ? null : PaohzCard.getPaohzCard(nowDisCard.getId());
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
            if(hu.getCheckCard() == null && disCard != null){
                hu.setCheckCard(disCard);
            }
            player.setHu(hu);
            huConfirmList.add(player.getSeat());
            addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
            sendActionMsg(player, action, null, PaohzDisAction.action_type_action);
            calcOver();
        } else {
			broadMsg(player.getName() + " 不能胡牌");
        }

    }


    public PaohuziHuLack filtrateHu(LdfpfPlayer player,List<PaohuziHuLack> pao,List<PaohuziHuLack> ping){
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
                if (allHuxi >= getFloorValue()||((isPiaoHu()&&allHuxi==0)))
                    hu.add(lack);
            }
        }
        if(hu.size()>=1){
            return getMaxHu(player,hu);
        }
        return null;
    }

    public PaohuziHuLack getMaxHu(LdfpfPlayer player,List<PaohuziHuLack> hu){
        int maxpoint=-1;
        PaohuziHuLack lack1=null;
        for (PaohuziHuLack lack:hu){
            List<Integer> arr = PaohuziMingTangRule.calcMingTang(player, lack);
            int point = PaohuziMingTangRule.calcMingTangFen(lack.getHuxi()+player.getOutHuxi() +player.getZaiHuxi(), arr, capping, boomFlag);
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
    public boolean isSelfMo(LdfpfPlayer player) {
        if (moSeatPair != null) {
            return moSeatPair.getValue().intValue() == player.getSeat() || (player.getSeat()==shuXingSeat&&moSeatPair.getValue().intValue()==lastWinSeat);
        }
        return false;
    }

    /**
	 * 提
	 */
    private void ti(LdfpfPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action, boolean moPai) {
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



    }

    /**
	 * 栽(臭栽)
	 *
	 * @param cardList
	 *            要栽的牌
	 */
    private void zai(LdfpfPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
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


        if (!disCard) {
            // checkMo();
        }

    }

    /**
	 * 跑
	 */
    private void pao(LdfpfPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action, boolean isHu, boolean isPassHu ,boolean system) {

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

    private void relieveFangZhao(long userId) {
        for (Player player : seatMap.values()) {
            player.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao, userId + "", 0 + "");
        }
    }

    /**
	 * 出牌
	 */
    private void disCard(LdfpfPlayer player, List<PaohzCard> cardList, int action) {
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
        setBoomFlag(true);
        disNum++;
        if (disNum>1)
            setFirstCard(false);
        if(nowDisCardSeat!=lastWinSeat)
            setFirstCard(false);
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
        setMoFlag(0);
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
        if(boomHuSeat!=0){
            hu(seatMap.get(boomHuSeat),null,PaohzDisAction.action_hu,cardList.get(0),true);
        }
        checkAutoMo();
    }

    private void checkAutoMo() {
        if (isTest()) {
            checkMo();
        }
    }

    private void tiLong(LdfpfPlayer player) {
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
            StringBuilder sb = new StringBuilder("Ldfpf");
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
            sendZpLeftCards(-1);
        }
    }

    public void checkFreePlayerTi(LdfpfPlayer player, int action) {
        if (player.getSeat() == lastWinSeat && player.isFristDisCard() && action != PaohzDisAction.action_ti) {
            for (int seat : getSeatMap().keySet()) {
                if (lastWinSeat == seat) {
                    continue;
                }
                LdfpfPlayer nowPlayer = seatMap.get(seat);
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
    private void peng(LdfpfPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
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
            for (LdfpfPlayer seatPlayer : seatMap.values()) {
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
    private void pass(LdfpfPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
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
			player.writeErrMsg("手上已经没有牌了");
            return;
        }
        // 可以胡牌，然后点了过
        boolean isPassHu = actionList.get(0) == 1;
        player.setZaiTiHu(false);
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
    private void chi(LdfpfPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
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
        //如果选择吃，则下家不算过吃
        if(moFlag==1&&getMoSeat()==player.getSeat()){
            LdfpfPlayer nextP = seatMap.get(calcNextSeat(player.getSeat()));
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

    public synchronized void play(LdfpfPlayer player, List<Integer> cardIds, int action, boolean moPai, boolean isHu, boolean isPassHu) {
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
            StringBuilder sb = new StringBuilder("Ldfpf");
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
    public synchronized void lockHand(LdfpfPlayer player) {
        if (player.getLockHand() == 1) {
            return;
        }
        if(player.getHandPais().size() == 0 ){
            player.writeErrMsg(LangMsg.code_69);
            return;
        }
        StringBuilder sb = new StringBuilder("Ldfpf.lockHand");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        LogUtil.msgLog.info(sb.toString());
        player.setSiShou(true);
        player.setLockHand(1);
        lockHandNum++;
        for (Map.Entry<Integer, LdfpfPlayer> entry : seatMap.entrySet()) {
            LdfpfPlayer p = entry.getValue();
            ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.req_code_ldfpf_lockhand, (int) player.getUserId());
            p.writeSocket(builder.build());
        }
		// 点击弃牌，当前回合为自己出牌回合，直接跳过出牌

        int action = PaohzDisAction.action_lockhand;
        addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(null, ","));
		// 所有人都弃胡
        if (lockHandNum == seatMap.size()) {
            calcOver();
            return;
        }
        if (autoDisBean != null) {
            removeAction(player.getSeat());
            playAutoDisCard(autoDisBean);
        } else {
            if (actionSeatMap.containsKey(player.getSeat())) {
                play(player, new ArrayList<>(), PaohzDisAction.action_pass);
            }
			// 当前玩家需要出牌时，检查
            if (nowDisCardSeat == player.getSeat()) {
				checkFreePlayerTi(player, action);// 检查闲家提
//                setMoFlag(1);
                markMoSeat(player.getSeat(), action);
                clearMoSeatPair();
				setToPlayCardFlag(0); // 应该要打牌的flag
                setDisCardSeat(player.getSeat());
                setNowDisCardSeat(getNextDisCardSeat());
                checkMo();
            }
        }
    }

    private boolean setDisPlayer(LdfpfPlayer player, int action, boolean isHu) {
        return setDisPlayer(player, action, false, isHu);
    }

    /**
	 * 设置要出牌的玩家
	 */
    private boolean setDisPlayer(LdfpfPlayer player, int action, boolean isFirstDis, boolean isHu) {
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
        if (player.getLockHand()==0&&canDisCard && ((player.getSeat() == lastWinSeat && isFirstDis) || player.isNeedDisCard(action))) {
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
    private boolean checkActionOld(LdfpfPlayer player, int action) {
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
    private boolean checkAction(LdfpfPlayer player, int action, List<PaohzCard> cardList, PaohzCard nowDisCard) {
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
            LdfpfPlayer tempPlayer = seatMap.get(maxSeat);
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
    private void refreshTempAction(LdfpfPlayer player) {
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
                LdfpfPlayer tempPlayer = seatMap.get(tempAction.getSeat());
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
    private LdfpfPlayer getDisPlayer() {
        return seatMap.get(disCardSeat);
    }

    private void record(LdfpfPlayer player, int action, List<PaohzCard> cardList) {
    }

    @Override
    public int isCanPlay() {
        if (getPlayerCount() < getMaxPlayerCount()) {
            return 1;
        }
        // for (LdfpfPlayer player : seatMap.values()) {
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
        LdfpfPlayer player = seatMap.get(nowDisCardSeat);

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
            if(zpMap.containsKey(player.getUserId())){
                card = getNextCardById(zpMap.remove(player.getUserId()));
            } else {
                card = getNextCard();
            }
        }

        addPlayLog(player.getSeat(), PaohzDisAction.action_mo + "", (card == null ? 0 : card.getId()) + "");
        StringBuilder sb = new StringBuilder("Ldfpf");
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

            setMoFlag(1);
            setMoSeat(player.getSeat());
            markMoSeat(card, player.getSeat());
            player.moCard(card);
            setDisCardSeat(player.getSeat());
            setNowDisCardIds(new ArrayList<>(Arrays.asList(card)));
            setNowDisCardSeat(getNextDisCardSeat());
            boomFlag=false;
            setFirstCard(false);
            PaohuziCheckCardBean autoDisCard = null;
            for (Entry<Integer, LdfpfPlayer> entry : seatMap.entrySet()) {
				// 检查摸的牌可以做哪些操作，封装好，发给前端
                PaohuziCheckCardBean checkCard = entry.getValue().checkCard(card, entry.getKey() == player.getSeat(), false);
                if(!checkCard.isZai()&&!checkCard.isTi()&!checkCard.isChouZai())
                    entry.getValue().setZaiTiHu(false);
                if (checkPaohuziCheckCard(checkCard)) {
                    autoDisCard = checkCard;
                }

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

            if (this.leftCards != null && this.leftCards.size() == 0 && !isHasSpecialAction()) {
                calcOver();
                return;
            }

//			if (PaohuziConstant.isAutoMo) {
            // if (actionSeatMap.isEmpty()) {
            // markMoSeat(player.getSeat(), PaohzDisAction.action_mo);
            // }
            // checkMo();
//			}

            if (actionSeatMap.isEmpty()) {
                LdfpfPlayer nextPlayer = seatMap.get(calcNextSeat(player.getSeat()));
                if (nextPlayer.getLockHand() == 1 && nextPlayer.getIsOnline() != 0) {
                    checkMo();
                }
            }
            checkAutoMo();
            sendZpLeftCards(-1);
        }
    }

    public void sendZpLeftCards(long userId) {
        if(!isGroupRoom())
            return;

        ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_set_mo);
        List<PaohzCard> canChooseCards = new ArrayList<>(leftCards);
        Iterator<PaohzCard> iterator = canChooseCards.iterator();
        while (iterator.hasNext()) {
            PaohzCard card = iterator.next();
            if(zpMap.containsValue(card.getId())){
                iterator.remove();
            }
        }
        builder.addAllParams((PaohuziTool.toPhzCardIds(canChooseCards)));
        for(Player player : seatMap.values()){
            if(player.getGroupUser() != null && player.getGroupUser().getIsSpy() == 2 && (userId <= 0 ? true : player.getUserId() == userId) ){
                if(zpMap.containsKey(player.getUserId())) {
                    builder.addStrParams(zpMap.get(player.getUserId())+"");
                }
                player.writeSocket(builder.build());
                builder.clearStrParams();
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

    public boolean setZpMap(long zpUser, int zpValue) {
        if(leftCards.size() < seatMap.size()){      //牌跺牌数量小于玩家数时不能做牌
            return false;
        }
        synchronized (this) {
            for (PaohzCard card : leftCards) {
                if (card.getId() == zpValue) {
                    if (!zpMap.values().contains(zpValue)) {
                        zpMap.put(zpUser, zpValue);
                        sendZpLeftCards(zpUser);
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
	 * @return 是否有系统帮助自动出牌
	 */
    private PaohuziCheckCardBean checkDisAction(LdfpfPlayer player, int action, PaohzCard disCard, boolean canBoom) {
        PaohuziCheckCardBean autoDisCheck = new PaohuziCheckCardBean();
        int nowSeat=disCardSeat;
        int i=1;
        while (true){
            nowSeat = calcNextSeat(nowSeat);
            i++;
            if(i >= 10){
                break;
            }
            LdfpfPlayer p = seatMap.get(nowSeat);
            if (p.getUserId()==player.getUserId())
                continue;
            PaohuziCheckCardBean checkCard = p.checkCard(disCard,false,!canBoom,false,canBoom,false);
            boolean check = checkPaohuziCheckCard(checkCard);
            if (check) {
                autoDisCheck = checkCard;
            }

			// 判断是否强制胡牌
            if (checkCard.isHu()&&boomFlag&&boomMustHu){
                boomHuSeat=nowSeat;
                break;
//                hu(seatMap.get(nowSeat),null,PaohzDisAction.action_hu,disCard,true);
            }
            if(nowSeat==disCardSeat||i>=playerCount)
                break;
        }
        return autoDisCheck;
    }

    private boolean isFangZhao(LdfpfPlayer player, PaohzCard disCard) {

        for (Entry<Integer, LdfpfPlayer> entry : seatMap.entrySet()) {
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
    private PaohuziCheckCardBean checkAutoDis(LdfpfPlayer player, boolean isMoPaiIng) {
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

    private void sendActionMsg(LdfpfPlayer player, int action, List<PaohzCard> cards, int actType) {
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
    private void sendMoMsg(LdfpfPlayer player, int action, List<PaohzCard> cards, int actType) {
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(action);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
        // builder.setNextSeat(nowDisCardSeat);
        setNextSeatMsg(builder, player.getSeat());
        builder.setRemain(leftCards.size());
        builder.addAllPhzIds(PaohuziTool.toPhzCardIds(cards));
//        builder.addAllPhzIds(player.toShowOutPais(cards));
        builder.setActType(actType);
        sendMoMsgBySelfAction(builder, player.getSeat());
    }

    /**
	 * 发送该玩家动作msg
	 */
    private void sendPlayerActionMsg(LdfpfPlayer player) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(PaohzDisAction.action_refreshaction);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
        // builder.setNextSeat(nowDisCardSeat);
        setNextSeatMsg(builder, -1);
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
            LdfpfPlayer paohuziPlayer = seatMap.get(shuXingSeat);
            paohuziPlayer.writeSocket(builder.build());
        }
    }

    private void setNextSeatMsg(PlayPaohuziRes.Builder builder, Integer nowMoSeat) {

        // if (!GameServerConfig.isDebug()) {
        // builder.setNextSeat(nowDisCardSeat);
        //
        // } else {
        if(nowMoSeat > 0){      //优先现在摸的人
            builder.setTimeSeat(nowMoSeat);
        } else if(!actionSeatMap.isEmpty()) {      //优先操作的人
            int seat = getTimeSeat();
            builder.setTimeSeat(seat);
        } else {        //下个出牌人
            builder.setTimeSeat(nowDisCardSeat);
        }
        if (toPlayCardFlag == 1) {
            builder.setNextSeat(nowDisCardSeat);
        } else {
            builder.setNextSeat(0);

        }

        // }

    }

    private int getTimeSeat() {
        int action = 0, seat = 0;
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            List<Integer> list = PaohzDisAction.parseToDisActionList(entry.getValue());
            int minAction = Collections.min(list);
            if (action == 0) {
                action = minAction;
                seat = entry.getKey();
            } else if (minAction < action) {
                action = minAction;
                seat = entry.getKey();
            } else if (minAction == action) {
                int nearSeat = getNearSeat(disCardSeat, Arrays.asList(seat, entry.getKey()));
                seat = nearSeat;
            }
        }
        return seat;
    }

    /**
	 * 发送动作msg
	 *
	 * @param player
	 * @param action
	 * @param cards
	 * @param actType
	 */
    private void sendActionMsg(LdfpfPlayer player, int action, List<PaohzCard> cards, int actType, boolean isZaiPao, boolean isChongPao) {
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(action);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
        setNextSeatMsg(builder, actType == PaohzDisAction.action_type_mo ? player.getSeat() : -1);
        if (leftCards != null) {
            builder.setRemain(leftCards.size());

        }
        builder.addAllPhzIds(PaohuziTool.toPhzCardIds(cards));
//        builder.addAllPhzIds(player.toShowOutPais(cards));
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
        LdfpfPlayer winPlayer = seatMap.get(lastWinSeat);
        for (LdfpfPlayer player : seatMap.values()) {
            PlayPaohuziRes.Builder copy = builder.clone();
            if (player.getSeat() != seat) {
                // copy.clearPhzIds();
                // copy.addPhzIds(0);
                if (seat==lastWinSeat&&player.getSeat()==shuXingSeat){
                    copy.setHuxi(winPlayer.getOutHuxi() + winPlayer.getZaiHuxi());
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

        LdfpfPlayer winPlayer = seatMap.get(lastWinSeat);

        for (LdfpfPlayer player : seatMap.values()) {
            PlayPaohuziRes.Builder copy = builder.clone();
            if (copy.getSeat() == player.getSeat()) {
                copy.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
                if(player.isAutoPlay() && copy.getActType() == PaohzDisAction.action_type_dis){
                    copy.setActType(PaohzDisAction.action_type_autoplaydis);
                }
            }else if(copy.getSeat()==lastWinSeat&&player.getSeat()==shuXingSeat){
                copy.setHuxi(winPlayer.getOutHuxi() + winPlayer.getZaiHuxi());
                if(winPlayer.isAutoPlay() && copy.getActType() == PaohzDisAction.action_type_dis){
                    copy.setActType(PaohzDisAction.action_type_autoplaydis);
                }
            }

			// 需要特殊处理一下栽
            if (copy.getAction() == PaohzDisAction.action_zai) {
                if (copy.getSeat() != player.getSeat()) {
                    if (copy.getSeat()==lastWinSeat&&player.getSeat()==shuXingSeat){
                    }else{
						// 需要替换成0
                        List<Integer> ids = PaohuziTool.toPhzCardZeroIds(copy.getPhzIdsList());
                        copy.clearPhzIds();
                        copy.addAllPhzIds(ids);
                    }
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

            }else if (player.getSeat()==shuXingSeat&&actionSeatMap.containsKey(winPlayer.getSeat())) {
                List<Integer> actionList = getSendSelfAction(zaiKeyValue, winPlayer.getSeat(), actionSeatMap.get(winPlayer.getSeat()));
                if (actionList != null) {
                    // copy.addAllSelfAct(actionList);
                    if (noShow && paoSeat != winPlayer.getSeat()) {
						// 出牌时，别人有跑的情况不提示吃碰
                        if (1 == actionList.get(0)) {
                            copy.addAllSelfAct(actionList);
                        }
                    } else {
                        copy.addAllSelfAct(actionList);
                    }
                }

            }

            //放炮自动胡时 不需要SelfAct
            if(boomMustHu && boomHuSeat != 0){
                copy.clearSelfAct();
            }
            player.writeSocket(copy.build());
            if (copy.getSelfActList() != null && copy.getSelfActList().size() > 0) {
                StringBuilder sb = new StringBuilder("Ldfpf");
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
        LdfpfPlayer disCSMajiangPlayer = seatMap.get(disCardSeat);
        PaohuziResTool.buildPlayRes(disBuilder, disCSMajiangPlayer, 0, null);
        disBuilder.setRemain(leftCards.size());
        disBuilder.setHuxi(disCSMajiangPlayer.getOutHuxi() + disCSMajiangPlayer.getZaiHuxi());
        // disBuilder.setNextSeat(nowDisCardSeat);
        setNextSeatMsg(disBuilder, -1);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            PlayPaohuziRes.Builder copy = disBuilder.clone();
            List<Integer> actionList = entry.getValue();
            copy.addAllSelfAct(actionList);
            LdfpfPlayer seatPlayer = seatMap.get(entry.getKey());
            seatPlayer.writeSocket(copy.build());
            if (shuXingSeat>0&&seatPlayer.getSeat()==lastWinSeat){
                seatMap.get(shuXingSeat).writeSocket(copy.build());
            }
        }

    }

    public void checkAction() {
        int nowSeat = getNowDisCardSeat();
		// 先判断拿牌的玩家
        LdfpfPlayer nowPlayer = seatMap.get(nowSeat);
        if (nowPlayer == null) {
            return;
        }
		// 获取可以做的所有操作
        PaohuziCheckCardBean checkCard = nowPlayer.checkCard(null, true, true, false);
        if (checkPaohuziCheckCard(checkCard)) {
            playAutoDisCard(checkCard);
            tiLong(nowPlayer);
			/*-- 暂时屏蔽两条龙的处理
			boolean needBuPai = false;
			if (checkCard.isTi()) {
				PaohuziHandCard cardBean = nowPlayer.getPaohuziHandCard();
				PaohzCardIndexArr valArr = cardBean.getIndexArr();
				PaohuziIndex index3 = valArr.getPaohzCardIndex(3);
				if (index3 != null && index3.getLength() >= 2) {
					needBuPai = true;
				}
			}
			playAutoDisCard(checkCard);
			
			if (needBuPai) {
				PaohzCard buPai = leftCards.remove(0);
				for (LdfpfPlayer player : seatMap.values()) {
					if (nowSeat == player.getSeat()) {
						player.getHandPhzs().add(buPai);
						System.out.println("----------------------------------谁补:" + player.getName() + "  什么牌:" + buPai.getId() + "  醒子数量:" + leftCards.size());
					}
					player.writeComMessage(WebSocketMsgType.res_com_code_phzbupai, nowSeat, buPai.getId(), leftCards.size());
				}
			}
			 */
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
        if (checkCard != null && checkCard.getActionList() != null) {
            int seat = checkCard.getSeat();
            LdfpfPlayer player = seatMap.get(seat);
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
                LdfpfPlayer player = seatMap.get(nextseat);
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
                    LdfpfPlayer player = seatMap.get(key);
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
        setMoFlag(0);
        setMoSeat(0);
        clearAction();
        setNowDisCardSeat(0);
        setNowDisCardIds(null);
        setFirstCard(true);
        disNum=0;
        lockHandNum=0;
        boomHuSeat=0;
        timeNum = 0 ;
        clearTempAction();
        setPaoHu(0);
        finishFapai=0;
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
        if(getPlayBureau()==1 && isGroupRoom()){
            if(bankerRand){
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
		// 洗牌之后发牌

        List<List<PaohzCard>> list = new ArrayList<>();
        boolean again = false;
        int fapaiTimes = 0;
        int redPaiCountMin = ResourcesConfigsUtil.loadServerConfigIntegerValue("ldfpf_redeal_redPaiCount_min", 3);
        int redPaiCountMax = ResourcesConfigsUtil.loadServerConfigIntegerValue("ldfpf_redeal_redPaiCount_max", 8);
        int redealRate = ResourcesConfigsUtil.loadServerConfigIntegerValue("ldfpf_redeal_rate", 80);
        do {
            again = false;
            list = PaohuziTool.fapai(copy, zp, 0, getPlayerCount());
            fapaiTimes++;
            //玩家红牌数不在范围内 有几率重新发
            for (int i = 0; i < playerCount; i++) {
                int redPaiCont = 0;
                for (PaohzCard card : list.get(i)) {
                    if (card.isRed()) {
                        redPaiCont++;
                    }
                }
                if ((redPaiCont <= redPaiCountMin || redPaiCont >= redPaiCountMax)) {
                    again = true;
                    break;
                }
            }
            if (again) {
                int rate = RandomUtils.nextInt(100);
                if (rate > redealRate) {
                    again = false;
                } else {
                    LogUtil.i("Ldfpf|redeal|"+ getId() + "|" + playBureau);
                }
            }
        } while (again && (zp == null || zp.isEmpty()) && fapaiTimes < 10);



        int seat=lastWinSeat;

        for (int i = 0; i < playerCount; i++) {
            LdfpfPlayer player = seatMap.get(seat);
            player.changeState(player_state.play);
            player.getFirstPais().clear();
            player.dealHandPais(list.get(i));
            player.dealOriginalHandPaiIds(list.get(i));
            player.getFirstPais().addAll(PaohuziTool.toPhzCardIds(new ArrayList(list.get(i))));
            seat=calcNextSeat(seat);

            StringBuilder sb = new StringBuilder("Ldfpf");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.getName());
            sb.append("|").append("fapai");
            sb.append("|").append(player.getHandPhzs());
            LogUtil.msgLog.info(sb.toString());
        }

        List<PaohzCard> cardList;
		// 玩家数量为2，将第三人的手牌加入剩余卡牌
        if (playerCount==2){
            cardList=list.get(2);
            if(list.size()!=3)
                cardList.addAll(list.get(3));
        }else {
            cardList=list.get(3);
        }
        int size = cardList.size();
		// 抽排
        if(chouPai==10||chouPai==20){
            List<PaohzCard> chuPaiList = cardList.subList(size - chouPai, size);
            chouCards = PaohuziTool.toPhzCardIds(chuPaiList);
            cardList = cardList.subList(0, cardList.size() - chouPai);

            StringBuilder sb = new StringBuilder("Ldfpf");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append("chouPai");
            sb.append("|").append(chuPaiList);
            LogUtil.msgLog.info(sb.toString());
        }

		// 桌上所有剩余牌
        setStartLeftCards(PaohuziTool.toPhzCardIds(cardList));
        setLeftCards(new ArrayList<>(cardList));
        setFinishFapai(1);
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
        for (LdfpfPlayer player : playerMap.values()) {
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
        int seat = getTimeSeat();
        if (seat > 0){
            res.addExt(seat);
        } else {
            res.addExt(nowDisCardSeat); // 0
        }
        res.addExt(payType);// 1
		// 下标二
        res.addExt(ceiling);// 2
        res.addExt(isRedBlack);// 3
        res.addExt(isLianBanker);// 4
//        res.addExt(xiTotun);// 5
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
        res.addExt(catCardCount);// 9

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
        res.addExt(isGroupRoom() ? Integer.valueOf(loadGroupId()) : 0);// 21

        res.addTimeOut((isGoldRoom() || autoPlay) ?(int)autoTimeOut:0);
        res.addTimeOut(autoCheckTime);
        res.addTimeOut((isGoldRoom() || autoPlay) ?(int) autoTimeOut2 :0);
        return res.build();
    }

    @Override
    public void setConfig(int index, int val) {

    }

    public int randNumber(int number) {
        if(isGoldRoom()){
            return number;
        }else {
            int ret = 0;
            if (number > 0) {
                ret = (number + 5) / 10 * 10;
            } else if (number < 0) {
                ret = (number - 5) / 10 * 10;
            }

            return ret;
        }
    }

    public int getLdfpfPoint(LdfpfPlayer player,boolean isOver){
        int selfPoint = 0;
        int selfPointReal = 0;
        int otherPoint = 0;
        int otherPointReal = 0;
        int retPoint = 0;
        int retPointReal = 0;
        int daniaoFen=0;
        for (LdfpfPlayer temp : seatMap.values()) {
            if (player.getUserId() == temp.getUserId()) {
                selfPoint = isOver?randNumber(temp.getTotalPoint()):temp.getTotalPoint();
                selfPointReal = temp.getTotalPoint();
            } else {
                otherPoint += isOver?randNumber(temp.getTotalPoint()):temp.getTotalPoint();
                otherPointReal += temp.getTotalPoint();
            }
            daniaoFen+=temp.getNiaoFenNonnegative();
        }

        retPoint = selfPoint * (seatMap.size() - 1) - otherPoint;
        retPointReal = selfPointReal * (seatMap.size() - 1) - otherPointReal;

        if(isOver&&playerCount==2&&retPointReal!=0){
			// 分数可能为负，便于计算，转为正
            boolean flag=false;
            if(retPointReal<0){
                retPoint=-retPoint;
                flag=true;
            }
			// 打鸟计算
            switch (daNiaoType){
			case 1:// 胡息打鸟
                    retPoint*=Math.pow(2,daniaoFen);
                    break;
			case 2:// 分数打鸟
                    retPoint+=daniaoFen;
                    break;
			case 3:// 局内打鸟
                    retPoint+=daniaoFen;
                    break;
            }
            if(flag)
                retPoint=-retPoint;
        }
		// 判断输赢分是否需要加倍
        if(isOver&&isDouble){
            int i=retPoint>0?retPoint:-retPoint;
            if(i<dScore)
                retPoint=retPoint*doubleNum;
        }

        return retPoint;
    }

    public ClosingPhzInfoRes.Builder sendAccountsMsg(boolean over, List<Integer> winList, int winFen, List<Integer> mt, int totalTun, boolean isBreak,Map<Long,Integer> outScoreMap,Map<Long,Integer> ticketMap) {

        List<ClosingPhzPlayerInfoRes> list = new ArrayList<>();
        List<ClosingPhzPlayerInfoRes.Builder> builderList = new ArrayList<>();
        LdfpfPlayer winPlayer=null ;
        if (winList != null &&winList.size()>0) {
            winPlayer = seatMap.get(winList.get(0));
        }

        List<TablePhzResMsg.PhzHuCardList> cardCombos = new ArrayList<>();
        for (LdfpfPlayer player : seatMap.values()) {
            ClosingPhzPlayerInfoRes.Builder build;
            build = player.bulidTotalClosingPlayerInfoRes( over, player.getWinLossPoint(),player.getTotalPoint());
			build.addAllFirstCards(player.getFirstPais());// 将初始手牌装入网络对象
            for(int action : player.getActionTotalArr()){
                build.addStrExt(action+""); // 0 , 1 ,2 ,3
            }
            if (isGoldRoom()){
                build.setTotalPoint((int)player.getWinGold());
                build.setPoint((int)player.getWinGold());
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
            player.setWinLoseCredit(player.getWinLossPoint() * creditDifen);
            TablePhzResMsg.PhzHuCardList.Builder builder = TablePhzResMsg.PhzHuCardList.newBuilder();
            builder.setSeat(player.getSeat());
            builder.addAllPhzCard(player.buildNormalPhzHuCards());
            cardCombos.add(builder.build());
        }



		// 信用分计算
        if (isCreditTable()) {
			// 计算信用负分
            calcNegativeCredit();

            long dyjCredit = 0;
            for (LdfpfPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
                LdfpfPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (LdfpfPlayer player : seatMap.values()) {
                player.setWinGold(player.getWinLossPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
                LdfpfPlayer player = seatMap.get(builder.getSeat());
                builder.setWinLoseCredit(player.getWinGold());
            }
        }

        for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
            LdfpfPlayer player = seatMap.get(builder.getSeat());
            builder.addStrExt(player.getWinLoseCredit() + "");      //8
            builder.addStrExt(player.getCommissionCredit() + "");   //9
            builder.addStrExt(player.getIsDaniao()+""); //10
            builder.addStrExt(player.getLockHand()+""); //11
            builder.addStrExt(player.getNiaoFen()+"");  //12
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
        res.addAllChouCards(chouCards);
        res.addAllIntParams(getIntParams());
        res.addAllAllCardsCombo(cardCombos);
        if (over && isGroupRoom() && !isCreditTable()) {
            res.setGroupLogId((int) saveUserGroupPlaylog());
        }
        for (LdfpfPlayer player : seatMap.values()) {
            player.writeSocket(res.build());
        }
        return res;
    }


    private void countBetweenTwoPoint(LdfpfPlayer p1,LdfpfPlayer p2){
        if(p1 == null || p2 == null){
            return;
        }
        int self = randNumber(p1.getTotalPoint());
        int other = randNumber(p2.getTotalPoint());
        int point =self-other;
        if(p1.getTotalPoint()==p2.getTotalPoint())
            return;
        boolean minus=false;
        if(p1.getTotalPoint()<p2.getTotalPoint()){
            minus=true;
            point=-point;
        }
        switch (daNiaoType){
		case 1:// 胡息打鸟
                point*=Math.pow(2,p1.getNiaoFen()+p2.getNiaoFen());
                break;
		case 2:// 分数打鸟
		case 3:// 局内打鸟
                point=p1.getNiaoFen()+p2.getNiaoFen()+point;
                break;
        }
        if (minus){
            point=-point;
        }
        p1.changeLastPoint(point);
        p2.changeLastPoint(-point);
    }

    @Override
    public void sendAccountsMsg() {
        calcPointBeforeOver();
        ClosingPhzInfoRes.Builder res = sendAccountsMsg(true, null, 0, null, 0, true, null,null);
        saveLog(true,0L, res.build());
        initstatus();
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
            ratio = 0;
            pay = 0;
        }else{
            ratio = 1;
            pay = loadPayConfig(payType);
        }
        ext.add(String.valueOf(ratio));
        ext.add(String.valueOf(pay>=0?pay:0));
        ext.add(isGroupRoom()?loadGroupId():"");//13
        ext.add(String.valueOf(catCardCount));//14


		// 信用分
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
        return createTable(new CreateTableInfo(player, TABLE_TYPE_NORMAL, play, bureauCount, params, strParams, saveDb));
    }

    public void createTable(Player player, int play, int bureauCount, List<Integer> params) throws Exception {
        createTable(new CreateTableInfo(player, TABLE_TYPE_NORMAL, play, bureauCount, params, true));
    }

    @Override
    public void calcOver0() {
        super.calcOver0();
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
        if(!sendPiaoReconnect(player)) {
            sendFirstXipaiReconnect(player);
        }
        LdfpfPlayer p=(LdfpfPlayer)player;
    }

    private boolean sendPiaoReconnect(Player player){
        boolean send = false;
        if(daNiaoType==0)
            return send;
        int count=0;
        for(Map.Entry<Integer,LdfpfPlayer> entry:seatMap.entrySet()){
            player_state state = entry.getValue().getState();
            if(state==player_state.play||state==player_state.ready)
                count++;
        }
        if(count!=getMaxPlayerCount())
            return send;

        for(Map.Entry<Integer,LdfpfPlayer> entry:seatMap.entrySet()){
            LdfpfPlayer p = entry.getValue();
            if(p.getUserId()==player.getUserId()){
                if(p.getNiaoFen()==-1){
                    player.writeComMessage(WebSocketMsgType.res_code_ldfpf_daniao,daNiaoType);
                    send = true;
                }
            }
        }
        return send;
    }

    private void sendFirstXipaiReconnect(Player player){
        if(!isFirstXipai())
            return;
        if(finishDaNiao == 0 && daNiaoType >0)  //还没完成打鸟阶段
            return;
        int count=0;
        for(Map.Entry<Integer,LdfpfPlayer> entry:seatMap.entrySet()){
            player_state state = entry.getValue().getState();
            if(state==player_state.play||state==player_state.ready)
                count++;
        }
        if(count!=getMaxPlayerCount())
            return;

        for(Map.Entry<Integer,LdfpfPlayer> entry:seatMap.entrySet()){
            LdfpfPlayer p = entry.getValue();
            if(p.getUserId()==player.getUserId()){
                if(p.getFirstXipai()==-1){
                    player.writeComMessage(WebSocketMsgType.res_code_first_xipai_inform,firstXiPai);
                }
            }
        }
    }

    @Override
    public void checkAutoPlay() {
        synchronized (this){
            if (getSendDissTime() > 0) {
                for (LdfpfPlayer player : seatMap.values()) {
                    if (player.getLastCheckTime() > 0) {
                        player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                    }
                }
                return;
            }

            if (isAutoPlayOff()) {
                // 托管关闭
                for (int seat : seatMap.keySet()) {
                    LdfpfPlayer player = seatMap.get(seat);
                    player.setAutoPlay(false, this);
                    player.setLastOperateTime(System.currentTimeMillis());
                }
                return;
            }

            if (state == table_state.ready&& playedBureau > 0) {
                ++timeNum;
                for (LdfpfPlayer player : seatMap.values()) {
                    if ((timeNum >= 15 && player.isAutoPlay())||timeNum >= 30) {
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
//                timeout = autoTimeOut = 1000000000;
            }else{
                return;
            }
            long autoPlayTime = ResourcesConfigsUtil.loadIntegerValue("ServerConfig","autoPlayTimePhz",2*1000);
            long now = TimeUtil.currentTimeMillis();

            if (daNiaoType>0&&(isFirstXipai()? finishDaNiao == 0 : finishFapai == 0) &&playedBureau==0) {
                for (LdfpfPlayer player : seatMap.values()) {
                    if(player.getNiaoFen()==-1){
                        boolean auto = checkPlayerAuto(player ,timeout);
                        if (auto) {
                            daNiao(player, 0);
                        }
                    }else {
                        player.setLastCheckTime(0);
                    }
                }
                return;
            }

            if (isFirstXipai()&& (finishFapai == 0 && (daNiaoType > 0 ? finishDaNiao == 1 : finishDaNiao ==0)) && playedBureau==0) {
                for (LdfpfPlayer player : seatMap.values()) {
                    if(player.getFirstXipai()==-1){
                        boolean auto = checkPlayerAuto(player ,timeout);
                        if (auto) {
                            handleFirstXipai(player, 0);
                        }
                    }else {
                        player.setLastCheckTime(0);
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
                    LdfpfPlayer player = seatMap.get(seat);
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
                            if(action == PaohzDisAction.action_chi || action == PaohzDisAction.action_peng || action == PaohzDisAction.action_hu){  //20210703修改： 要手动的操作都过掉
                                action = PaohzDisAction.action_pass;
                            }
                            if(action == PaohzDisAction.action_pass){
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
                LdfpfPlayer player = seatMap.get(nowDisCardSeat);
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

    public boolean checkPlayerAuto(LdfpfPlayer player ,int timeout){
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
        return LdfpfPlayer.class;
    }

    public PaohzCard getNextCardById(int id) {
        synchronized (this) {
            if (this.leftCards.size() > 0) {
                Iterator<PaohzCard> iterator = this.leftCards.iterator();
                PaohzCard find = null;
                while (iterator.hasNext()) {
                    PaohzCard paohzCard = iterator.next();
                    if (paohzCard.getId() == id) {
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
    }

    public PaohzCard getNextCard(int val) {
        synchronized (this) {
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
    }

    public PaohzCard getNextCard() {
        if (this.leftCards.size() > 0) {
            PaohzCard card = null;
            //过滤zpMap里的
            Iterator<PaohzCard> iterator = this.leftCards.iterator();
            while (iterator.hasNext()) {
                PaohzCard paohzCard = iterator.next();
                if (!this.zpMap.values().contains(paohzCard.getId())) {
                    card = paohzCard;
                    iterator.remove();
                    break;
                }
            }
            dbParamMap.put("leftPais", JSON_TAG);
            if(card == null){       //没有非zpMap里的就取后面一个
                card = leftCards.remove(0);
            }
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

    public void setFinishFapai(int finishFapai) {
        this.finishFapai = finishFapai;
        changeExtend();
    }

    public int getFinishDaNiao() {
        return finishDaNiao;
    }

    public void setFinishDaNiao(int finishDaNiao) {
        this.finishDaNiao = finishDaNiao;
        changeExtend();
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


//	public int calcNeedRoomCards(int needCard, int playerCount) {
//		long endTime = 1482940800000L;// 2016-12-29 00:00:00
//		long nowTime = TimeUtil.currentTimeMillis();
//		if (nowTime < endTime) {
//			return 0;
//		}
//		if (isSiRenBoPi()) {
//			needCard = 2;
//		} else if (isBoPi()) {
//			needCard = 1;
//		}
//		super.calcNeedRoomCards(needCard, playerCount);
//		return needCard;
//	}

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
    public boolean isMoByPlayer(LdfpfPlayer player) {
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

	// 大结算重置打鸟信息
    public void initstatus(){
        for(int seat:seatMap.keySet()){
            LdfpfPlayer player = seatMap.get(seat);
            player.setTotalPoint(player.getWinLossPoint());
        }
        firstCard=true;
        confirmTime=0;
    }


    public boolean isBoomFlag() {
        return boomFlag;
    }

    public void setBoomFlag(boolean boomFlag) {
        this.boomFlag = boomFlag;
    }



    public int getShuXingSeat() {
        return shuXingSeat;
    }


    public void setShuXingSeat(int shuXingSeat) {
        this.shuXingSeat = shuXingSeat;
    }

    @Override
    public int getDissPlayerAgreeCount() {
        return getPlayerCount();
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams,
                            Object... objects) throws Exception {
		// // 添加剥皮上限
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
		// 俱乐部房间 单大局大赢家、单大局大负豪、总小局数、单大局赢最多、单大局输最多 数据统计
        if(isGroupRoom()){
            String groupId=loadGroupId();
            int maxPoint=0;
            int minPoint=0;
            Long dataDate=Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            //Long dataDate, String dataCode, String userId, String gameType, String dataType, int dataValue

            calcDataStatistics3(groupId);

            for (LdfpfPlayer player:playerMap.values()){
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

            for (LdfpfPlayer player:playerMap.values()){
                int finalPoint;
                finalPoint = player.loadScore();

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
        for (LdfpfPlayer player : seatMap.values()) {
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
		return "娄底放炮罚";
    }


    public int getDaNiaoType() {
        return daNiaoType;
    }

    public void setDaNiaoType(int daNiaoType) {
        this.daNiaoType = daNiaoType;
    }

    public int getFirstXiPai() {
        return firstXiPai;
    }

    public boolean isFirstXipai() {
        return firstXiPai > 0 && isXipai();
    }

    public void setFirstXiPai(int firstXiPai) {
        this.firstXiPai = firstXiPai;
    }

    public int getPaoHu() {
        return paoHu;
    }

    public void setPaoHu(int paoHu) {
        this.paoHu = paoHu;
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

    //-----------------------兼容代码，前后端同步后，可删除-----------------------//
//    @Override
//    public synchronized void setReplenishParams(Player player, List<Integer> intParams, List<String> strParams) {
//
//        LdfpfPlayer p;
//        if (!(player instanceof LdfpfPlayer) || daNiaoType == 0) {
//            return;
//        }
//        p = (LdfpfPlayer) player;
//        if (p.isAlreadyDaniao()) {
//            return;
//        }
//        switch (daNiaoType) {
//            case 1:// 胡息打鸟
//            case 2:// 分数打鸟
//                daNiao(p,intParams.get(0));
//                break;
//            case 3:// 局内打鸟
//                daNiao(p,intParams.get(1));
//                break;
//        }
//        for (Entry<Integer, LdfpfPlayer> entry : seatMap.entrySet()) {
//            // 传code,座位号+已打鸟
//            entry.getValue().writeComMessage(WebSocketMsgType.req_code_daniao_seat, (int) p.getUserId(), 1, p.getNiaoFenNonnegative());
//        }
//    }

    public synchronized void daNiao(LdfpfPlayer player, int fen){
        if (daNiaoType == 0||playedBureau!=0) {
            return;
        }
        if (player.getNiaoFen()!=-1) {
            return;
        }
        switch (daNiaoType) {
            case 1:// 胡息打鸟
                player.setNiaoFen(fen);
                break;
            case 2:// 分数打鸟
                if (fen == 1) {
                    fen=daNiaoFen;
                }else {
                    fen=0;
                }
                player.setNiaoFen(fen);
                break;
            case 3:// 局内打鸟
                player.setNiaoFen(fen);
                break;
        }
        //-------------兼容代码----------------------//
//        player.setAlreadyDaniao(true);
//        player.setIsDaniao(fen);

        StringBuilder sb = new StringBuilder("ldfpf");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append("daNiao").append("|").append(fen);
        LogUtil.msgLog.info(sb.toString());
        int confirmTime=0;
        for (Map.Entry<Integer, LdfpfPlayer> entry : seatMap.entrySet()) {
            entry.getValue().writeComMessage(WebSocketMsgType.res_code_ldfpf_broadcast_daniao, (int)player.getUserId(),player.getNiaoFen());
            if(entry.getValue().getNiaoFen()!=-1)
                confirmTime++;
            //-------------兼容------------
//            player.writeComMessage(WebSocketMsgType.req_code_daniao_return);
        }
        if (confirmTime == playerCount) {
            setFinishDaNiao(1);
            if (!isFirstXipai()) {
                checkDeal(player.getUserId());
                // 检查起手牌是否需要自动操作
                startNext();
            } else {
                LogUtil.msgLog.info("ldfpf|sendFirstXipai|" + getId() + "|" + getPlayBureau());
                ComRes msg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_first_xipai_inform,firstXiPai).build();
                for (LdfpfPlayer p : playerMap.values()) {
                    if(p.getFirstXipai()==-1)
                        p.writeSocket(msg);
                }
            }
        }
    }

    /**
     * 首局洗牌
     * @param player
     * @param xipai
     */
    public synchronized void handleFirstXipai(LdfpfPlayer player, int xipai) {
        if (!isFirstXipai()) {
            return;
        }
        if (daNiaoType > 0 && finishDaNiao == 0 ) {   //未选完打鸟
            return;
        }
        if (!checkXipaiCreditOnStartNext(player)) {
            return;
        }
        if (player.getFirstXipai() >= 0) {
            return;
        }
        player.setFirstXipai(xipai);
        //扣除洗牌分
        if(xipai >0) {
            calcCreditXipai(player);
            addXipaiName(player.getName());
        }
        ComRes msg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_first_xipai, player.getSeat(), xipai).build();
        for (LdfpfPlayer p : playerMap.values()) {
                p.writeSocket(msg);
        }

        int confirmTime = 0;
        for (Map.Entry<Integer, LdfpfPlayer> entry : seatMap.entrySet()) {
            if (entry.getValue().getFirstXipai() != -1)
                confirmTime++;
        }
        if (confirmTime == playerCount) {
            checkDeal(player.getUserId());
            // 检查起手牌是否需要自动操作
            startNext();
        }
    }


    public static final List<Integer> wanfaList = Arrays.asList(199);

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
            return true;
        } else {
            return true;
        }
    }

    @Override
    public boolean isAllReady() {
        if(playedBureau==0){
            if (getPlayerCount() < getMaxPlayerCount())
                return false;
            for (Player player : getSeatMap().values()) {
                if(!player.isRobot()){
                    if (!(player.getState() == player_state.ready||player.getState() == player_state.play))
                        return false;
                }
            }
            if(finishFapai==1)
                return false;
            changeTableState(table_state.play);
            if (daNiaoType > 0 ) {
                boolean daniaoOver = true;
                for (LdfpfPlayer player : playerMap.values()) {
                    if(player.getNiaoFen()==-1){
                        daniaoOver = false;
                        break;
                    }
                }
                if(!daniaoOver){
                    if (finishFapai==0 && finishDaNiao == 0) {
                        LogUtil.msgLog.info("ldfpf|sendDniao|" + getId() + "|" + getPlayBureau());
                        ComRes msg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_ldfpf_daniao,daNiaoType).build();
                        for (LdfpfPlayer player : playerMap.values()) {
                            if(player.getNiaoFen()==-1)
                                player.writeSocket(msg);
                        }
                        //兼容旧版
//                        ComRes msg1 = SendMsgUtil.buildComRes(WebSocketMsgType.req_code_table_replenish, daNiaoType).build();
//                        for (LdfpfPlayer player : playerMap.values()) {
//                            player.writeSocket(msg1);
//                        }
                    }
                    return false;
                }
            }
            if(isFirstXipai()){
                boolean firstXipaiOver = true;
                for (LdfpfPlayer player : playerMap.values()) {
                    if(player.getFirstXipai()==-1){
                        firstXipaiOver = false;
                        break;
                    }
                }
                if(!firstXipaiOver){
                    if (finishFapai==0 && finishDaNiao == 0) {
                        LogUtil.msgLog.info("ldfpf|sendfirstXiPaiInform|" + getId() + "|" + getPlayBureau());
                        ComRes msg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_first_xipai_inform,firstXiPai).build();
                        for (LdfpfPlayer player : playerMap.values()) {
                            if(player.getFirstXipai()==-1)
                                player.writeSocket(msg);
                        }
                    }
                    return false;
                }
            }
            return true;
        }else {
            return super.isAllReady();
        }
    }

    public boolean isAllReadyOld(){
        if(daNiaoType!=0&&playedBureau==0){
            if (getPlayerCount() < getMaxPlayerCount())
                return false;
            for (Player player : getSeatMap().values()) {
                if(!player.isRobot()){
                    if (!(player.getState() == player_state.ready||player.getState() == player_state.play))
                        return false;
                }
            }
            if(finishFapai==1)
                return false;
            changeTableState(table_state.play);
            if (daNiaoType > 0 ) {
                boolean daniaoOver = true;
                for (LdfpfPlayer player : playerMap.values()) {
                    if(player.getNiaoFen()==-1){
                        daniaoOver = false;
                        break;
                    }
                }
                if(!daniaoOver){
                    if (finishFapai==0 && finishDaNiao == 0) {
                        LogUtil.msgLog.info("ldfpf|sendDniao|" + getId() + "|" + getPlayBureau());
                        ComRes msg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_ldfpf_daniao,daNiaoType).build();
                        for (LdfpfPlayer player : playerMap.values()) {
                            if(player.getNiaoFen()==-1)
                                player.writeSocket(msg);
                        }
                        //兼容旧版
//                        ComRes msg1 = SendMsgUtil.buildComRes(WebSocketMsgType.req_code_table_replenish, daNiaoType).build();
//                        for (LdfpfPlayer player : playerMap.values()) {
//                            player.writeSocket(msg1);
//                        }
                    }
                    return false;
                }
            }
            return true;
        }else {
            return super.isAllReady();
        }
    }

    public int getFinishFapai() {
        return finishFapai;
    }


    @Override
    public int getLogGroupTableBureau() {
        return super.getLogGroupTableBureau();
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

    public void setDataForPlayLogTable(PlayLogTable logTable) {
        StringJoiner players = new StringJoiner(",");
        StringJoiner scores = new StringJoiner(",");
        for (int seat = 1, length = getSeatMap().size(); seat <= length; seat++) {
            LdfpfPlayer player = seatMap.get(seat);
            players.add(String.valueOf(player.getUserId()));
            scores.add(String.valueOf(player.getTotalPoint()));
        }
        logTable.setPlayers(players.toString());
        logTable.setScores(scores.toString());
    }

    public String getTableMsg() {

        Map<String, Object> json = new HashMap<>();
		json.put("wanFa", "类底放炮罚");
        if (isGroupRoom()) {
            json.put("roomName", getRoomName());
        }
        json.put("playerCount", getPlayerCount());
        json.put("count", getTotalBureau());
        if (autoPlay) {
            json.put("autoTime", autoTimeOut3 / 1000);
            if (autoPlayGlob == 1) {
				json.put("autoName", "单局");
            } else {
				json.put("autoName", "整局");
            }
        }
        return JSON.toJSONString(json);
    }

}
