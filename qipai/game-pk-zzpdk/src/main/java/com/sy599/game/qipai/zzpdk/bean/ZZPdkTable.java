package com.sy599.game.qipai.zzpdk.bean;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.sy599.game.qipai.zzpdk.util.CardValue;
import com.sy599.game.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.CommonUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.FirstmythConstants;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.DataStatistics;
import com.sy599.game.db.bean.PdkRateConfig;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.dao.DataStatisticsDao;
import com.sy599.game.db.dao.PdkRateConfigDao;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.db.dao.UserDatasDao;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.jjs.bean.MatchBean;
import com.sy599.game.jjs.util.JjsUtil;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayCardRes;
import com.sy599.game.msg.serverPacket.TableRes.ClosingInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.ClosingPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.zzpdk.constant.PdkConstants;
import com.sy599.game.qipai.zzpdk.tool.CardTool;
import com.sy599.game.qipai.zzpdk.tool.CardTypeTool;
import com.sy599.game.qipai.zzpdk.tool.Scheme;
import com.sy599.game.qipai.zzpdk.util.CardUtils;
import com.sy599.game.qipai.zzpdk.util.CardUtils.Result;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class ZZPdkTable extends BaseTable {
    public static final String GAME_CODE = "zzpdk";
    private static final int JSON_TAG = 1;
    /*** 当前牌桌上出的牌 */
    private volatile List<Integer> nowDisCardIds = new ArrayList<>();
    /*** 玩家map */
    private Map<Long, ZZPdkPlayer> playerMap = new ConcurrentHashMap<Long, ZZPdkPlayer>();
    /*** 座位对应的玩家 */
    private Map<Integer, ZZPdkPlayer> seatMap = new ConcurrentHashMap<Integer, ZZPdkPlayer>();
    /*** 最大玩家数量 */
    private volatile int max_player_count = 3;

    private volatile int isFirstRoundDisThree;// 首局是否出黑挑三

    public static final int FAPAI_PLAYER_COUNT = 3;// 发牌人数

    private volatile List<Integer> cutCardList = new ArrayList<>();// 切掉的牌

    private volatile int showCardNumber = 0; // 是否显示剩余牌数量

    private volatile int redTen;//是否红10  1:5分  2:10分 3:翻倍
    private volatile int isFirstCardType32;//是否本轮第一个出牌3带2
    private volatile int card3Eq = 1;//三张/飞机可少带接完(玩家最后一手牌可少带牌接三张或飞机)0是1否（牌个数必须相等）

    private volatile int timeNum = 0;

    //新的一轮，3人为2人pass之后为新的一轮出牌
    private boolean newRound = true;
    //pass累计
    private volatile int passNum=0;
    /**
     * 托管时间
     */
    private volatile int autoTimeOut = 5 * 24 * 60 * 60 * 1000;
    private volatile int autoTimeOut2 = 5 * 24 * 60 * 60 * 1000;

    //是否已经发牌
    private int finishFapai=0;

    //是否加倍：0否，1是
    private int jiaBei;
    //加倍分数：低于xx分进行加倍
    private int jiaBeiFen;
    //加倍倍数：翻几倍
    private int jiaBeiShu;
    //是否勾选无炸弹
    private int isNoBoom;

    /**托管1：单局，2：全局*/
    private int autoPlayGlob;
    private int autoTableCount;
    //是否勾选回放
    private int isPlayBack;
    //直到为出现要不起，出的牌都会临时存在这里
    private volatile List<PlayerCard> noPassDisCard = new ArrayList<>();
    //回放手牌
    private volatile String replayDisCard = "";

    /** 打鸟选项：0不打鸟 ，可选分：1-100**/
    private int daNiaoFen;

    /** 是否可拆炸蛋：0否，1是**/
    private int chai4Zha;
    /** 3张A算炸蛋：0否，1是**/
    private int AAAZha;

    /** 特殊状态 1打鸟**/
    private int tableStatus = 0;

    //飘分 0:不飘 1:每局飘1 2:每局飘2 3:飘123  4:飘235  5:飘258
    private int piaoFenType=0;
    //是否已经发送飘分信息
    private int isSendPiaoFenMsg=0;
    //低于below加分
    private int belowAdd=0;
    private int below=0;
    //0 16张 1 15张
    private int cardNum=0;
    //允许出四代x
    private int sidai2=0;
    private int sidai3=0;
    //四代1算炸弹
    private int sidai1=0;
    //三张
    private int sanzhang=0;
    //三带一
    private int sandai1=0;
    //三带二
    private int sandai2=0;
    //三带一对
    private int sandaidui=0;
    //三带少出完
    private int sdscw=0;
    //炸弹算分 0 10分 1翻倍 2不算分
    private int boomPointType=0;
    //炸弹归属 0 出牌人 1赢家
    private int boomBelong=0;
    //炸弹封顶 0 1倍 1 2倍 2 3倍
    private int boomCelling=0;
    //上一手出的牌，要不起清零
    private Result cardClas=new Result(Result.undefined,0,0);
    //有效炸弹数
    private int boomNum=0;
    //0报单顶大，1放走包赔
    private int danType=0;

    public boolean createTable(Player player, int play, int bureauCount, List<Integer> params, boolean saveDb) throws Exception {
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

        setLastActionTime(TimeUtil.currentTimeMillis());
        int i = StringUtil.getIntValue(params, 2, 15);
        cardNum = i==1?16:15;
        sidai2 = StringUtil.getIntValue(params, 3, 0);
        sidai3 = StringUtil.getIntValue(params, 4, 0);
        sidai1 = StringUtil.getIntValue(params, 5, 0);
        isFirstRoundDisThree = StringUtil.getIntValue(params, 6, 0);
        max_player_count = StringUtil.getIntValue(params, 7, 0);
        showCardNumber = StringUtil.getIntValue(params, 8, 0);
        payType = StringUtil.getIntValue(params, 9, 0);
        redTen = StringUtil.getIntValue(params, 10, 0);
        danType=max_player_count==2?0:StringUtil.getIntValue(params, 11, 0);
        card3Eq = StringUtil.getIntValue(params, 12, 1);
        boomPointType = StringUtil.getIntValue(params, 13, 0);
        boomBelong = StringUtil.getIntValue(params, 14, 0);
        boomCelling = StringUtil.getIntValue(params, 15, 0);
        sanzhang = StringUtil.getIntValue(params, 16, 0);
        sandai1 = StringUtil.getIntValue(params, 17, 0);
        sandai2 = StringUtil.getIntValue(params, 18, 0);
        sandaidui = StringUtil.getIntValue(params, 19, 0);
        sdscw = StringUtil.getIntValue(params, 20, 0);
        this.autoPlay = StringUtil.getIntValue(params, 21, 0) >= 1;
        int time =StringUtil.getIntValue(params, 21, 0);
        if(time==1) {
            time =60;
        }

        this.jiaBei = StringUtil.getIntValue(params, 22, 0);
        this.jiaBeiFen = StringUtil.getIntValue(params, 23, 100);
        this.jiaBeiShu = StringUtil.getIntValue(params, 24, 1);
        if(this.getMaxPlayerCount() != 2){
            jiaBei = 0 ;
        }

        this.isPlayBack=StringUtil.getIntValue(params, 26, 0);
        autoPlayGlob = StringUtil.getIntValue(params, 27, 0);
        this.daNiaoFen = StringUtil.getIntValue(params, 28, 0);
        this.chai4Zha = StringUtil.getIntValue(params, 29, 1);
        this.AAAZha = StringUtil.getIntValue(params, 30, 0);
        if(daNiaoFen==0)
            this.piaoFenType = StringUtil.getIntValue(params, 31, 0);
        if(max_player_count==2){
            int belowAdd = StringUtil.getIntValue(params, 32, 0);
            if(belowAdd<=100&&belowAdd>=0)
                this.belowAdd=belowAdd;

            int below = StringUtil.getIntValue(params, 33, 0);
            if(below<=100&&below>=0){
                this.below=below;
                if(belowAdd>0&&below==0)
                    this.below=10;
            }
        }


        if (isGoldRoom()) {
            try {
                GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(id);
                if (goldRoom != null) {
                    modeId = goldRoom.getModeId();
                }
            } catch (Exception e) {
            }

            if (isMatchRoom()) {
                autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "matchAutoTimeOutPdk", 15 * 1000);
            } else if (isGoldRoom()) {
                autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoTimeOutPdk", 30 * 1000);
            }
            autoTimeOut2=autoTimeOut;
        }else{
            if(autoPlay) {
                autoTimeOut =autoTimeOut2 =time*1000;
            }
        }
        changeExtend();
        return true;
    }

    @Override
    public void initExtend0(JsonWrapper wrapper) {
        isFirstRoundDisThree = wrapper.getInt(1, 0);
        max_player_count = wrapper.getInt(2, 3);
        if (max_player_count == 0) {
            max_player_count = 3;
        }
        showCardNumber = wrapper.getInt(3, 0);
        redTen = wrapper.getInt(4, 0);
        danType = wrapper.getInt(5, 0);
        isFirstCardType32 = wrapper.getInt(6, 0);
        if (payType == -1) {
            String isAAStr = wrapper.getString("isAAConsume");
            if (!StringUtils.isBlank(isAAStr)) {
                this.payType = Boolean.parseBoolean(wrapper.getString("isAAConsume")) ? 1 : 2;
            } else {
                payType = 1;
            }
        }

        card3Eq = wrapper.getInt("card3Eq", card3Eq);
        jiaBei = wrapper.getInt(7, 0);
        jiaBeiFen = wrapper.getInt(8, 0);
        jiaBeiShu = wrapper.getInt(9, 0);
        isNoBoom = wrapper.getInt(10,0);
        isPlayBack = wrapper.getInt(11,0);
        replayDisCard = wrapper.getString(12);
        autoTimeOut = wrapper.getInt(13,0);
        autoPlayGlob = wrapper.getInt(14,0);
        daNiaoFen = wrapper.getInt(15,0);
        chai4Zha = wrapper.getInt(16,1);
        newRound = wrapper.getInt(17, 1) == 1;
        AAAZha = wrapper.getInt(18, 1);
        piaoFenType=wrapper.getInt(19, 0);
        isSendPiaoFenMsg=wrapper.getInt(20, 0);
        finishFapai=wrapper.getInt(21, 0);
        belowAdd=wrapper.getInt(22, 0);
        below=wrapper.getInt(23, 0);
        if(belowAdd>0&&below==0)
            below=10;
        autoTimeOut2 = autoTimeOut;
        //设置默认值
        if(autoPlay && autoTimeOut<=1) {
            autoTimeOut2 = autoTimeOut=60000;
        }
        sidai2=wrapper.getInt(24, 0);
        sidai3=wrapper.getInt(25, 0);
        sidai1=wrapper.getInt(26, 0);
        boomPointType=wrapper.getInt(27, 0);
        boomBelong=wrapper.getInt(28, 0);
        boomCelling=wrapper.getInt(29, 0);
        cardNum=wrapper.getInt(30, 0);
        cardClas.init(wrapper.getString(31));
        sanzhang=wrapper.getInt(32, 0);
        sandai1=wrapper.getInt(33, 0);
        sandai2=wrapper.getInt(34, 0);
        sandaidui=wrapper.getInt(35, 0);
        sdscw=wrapper.getInt(36, 0);
        boomNum=wrapper.getInt(37, 0);
    }

    @Override
    public JsonWrapper buildExtend0(JsonWrapper wrapper) {
        wrapper.putInt(1, isFirstRoundDisThree);
        wrapper.putInt(2, max_player_count);
        wrapper.putInt(3, showCardNumber);
        wrapper.putInt(4, redTen);
        wrapper.putInt(5, danType);
        wrapper.putInt(6, isFirstCardType32);
        wrapper.putString("card3Eq", String.valueOf(card3Eq));
        wrapper.putInt(7, jiaBei);
        wrapper.putInt(8, jiaBeiFen);
        wrapper.putInt(9, jiaBeiShu);
        wrapper.putInt(10,isNoBoom);

        wrapper.putInt(11,isPlayBack);
        wrapper.putString(12,replayDisCard);
        wrapper.putInt(13,autoTimeOut);
        wrapper.putInt(14,autoPlayGlob);
        wrapper.putInt(15,daNiaoFen);
        wrapper.putInt(16,chai4Zha);
        wrapper.putInt(17, newRound ? 1 : 0);
        wrapper.putInt(18, AAAZha);
        wrapper.putInt(19, piaoFenType);
        wrapper.putInt(20, isSendPiaoFenMsg);
        wrapper.putInt(21, finishFapai);
        wrapper.putInt(22, belowAdd);
        wrapper.putInt(23, below);
        wrapper.putInt(24, sidai2);
        wrapper.putInt(25, sidai3);
        wrapper.putInt(26, sidai1);
        wrapper.putInt(27, boomPointType);
        wrapper.putInt(28, boomBelong);
        wrapper.putInt(29, boomCelling);
        wrapper.putInt(30, cardNum);
        wrapper.putString(31, cardClas.toStr());
        wrapper.putInt(32, sanzhang);
        wrapper.putInt(33, sandai1);
        wrapper.putInt(34, sandai2);
        wrapper.putInt(35, sandaidui);
        wrapper.putInt(36, sdscw);
        wrapper.putInt(37, boomNum);
        return wrapper;
    }

    public Result getCardClas() {
        return cardClas;
    }

    public int getSanzhang() {
        return sanzhang;
    }

    public int getSandai1() {
        return sandai1;
    }

    public int getSandai2() {
        return sandai2;
    }

    public int getSandaidui() {
        return sandaidui;
    }

    public boolean haveSanDai(){
        return sanzhang==1||sandai1==1||sandai2==1||sandaidui==1;
    }

    public boolean sanDai2AndDui(){
        return sandai2==1&&sandaidui==1;
    }

    public int getSdscw() {
        return sdscw;
    }

    public int getSidai2() {
        return sidai2;
    }

    public int getSidai3() {
        return sidai3;
    }

    public int getSidai1() {
        return sidai1;
    }

    public int getCard3Eq() {
        return card3Eq;
    }

    public void setCard3Eq(int card3Eq) {
        this.card3Eq = card3Eq;
        changeExtend();
    }

    public int getIsFirstCardType32() {
        return isFirstCardType32;
    }

    public void setIsFirstCardType32(int isFirstCardType32) {
        this.isFirstCardType32 = isFirstCardType32;
        changeExtend();
    }

    public String getReplayDisCard() {
        return replayDisCard;
    }

    public void setReplayDisCard(String replayDisCard) {
        this.replayDisCard = replayDisCard;
    }

    public int getRedTen() {
        return redTen;
    }

    public void setRedTen(int redTen) {
        this.redTen = redTen;
        changeExtend();
    }

    public int getPiaoFenType() {
        return piaoFenType;
    }

    public void setPiaoFenType(int piaoFenType) {
        this.piaoFenType = piaoFenType;
    }

    @Override
    protected void loadFromDB1(TableInf info) {
        if (!StringUtils.isBlank(info.getNowDisCardIds())) {
            this.nowDisCardIds = StringUtil.explodeToIntList(info.getNowDisCardIds());
        }
        if (isMatchRoom()) {
            autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "matchAutoTimeOutPdk", 15 * 1000);
        } else if (isGoldRoom()) {
            autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoTimeOutPdk", 30 * 1000);
            autoTimeOut2=autoTimeOut;
        }else{
//            autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoTimeOutPdkNormal", 30 * 1000);
//            autoTimeOut2 = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoTimeOutPdkNormal2", 20 * 1000);
        }
    }

    public long getId() {
        return id;
    }

    public ZZPdkPlayer getPlayer(long id) {
        return playerMap.get(id);
    }

    /**
     * 一局结束
     */
    public void calcOver() {
        ZZPdkPlayer winPlayer = null;
        int winPoint = 0;
        Map<Integer, Integer> lossPoint = new HashMap<Integer, Integer>();
        int closeNum = 0;

        for (ZZPdkPlayer player : seatMap.values()) {
            player.changeState(player_state.over);
            int left = player.getHandPais().size();

            int currentLs = player.getCurrentLs();
            int maxLs = player.getMaxLs();
            if (left == 0) {
                winPlayer = player;
                currentLs++;
                player.setCurrentLs(currentLs);
                UserDatasDao.getInstance().updateUserDatas(String.valueOf(player.getUserId()), GAME_CODE, "all", "currentLs", String.valueOf(currentLs));
                if (currentLs > maxLs) {
                    maxLs = currentLs;
                    player.setMaxLs(maxLs);
                    UserDatasDao.getInstance().updateUserDatas(String.valueOf(player.getUserId()), GAME_CODE, "all", "maxLs", String.valueOf(maxLs));
                }
                player.setCurrentLshu(0);
            } else {
                if (currentLs > 0) {
                    currentLs = 0;
                    player.setCurrentLs(currentLs);
                    UserDatasDao.getInstance().updateUserDatas(String.valueOf(player.getUserId()), GAME_CODE, "all", "currentLs", String.valueOf(currentLs));
                }
                
                
                player.setCurrentLshu(player.getCurrentLshu()+1);

                // 需要大于1爆单不扣分
                int point = left <= 1 ? 0 : left;
                if (!player.isOutCards()) {
                    // 一张牌都没出算双倍
                    point = point * 2;
                    closeNum++;
                    player.getMyExtend().setPdkFengshen(FirstmythConstants.firstmyth_index6, 1);
                }

                lossPoint.put(player.getSeat(), point);
            }
        }
        if (winPlayer == null) {
            return;
        }
        if (closeNum > 0) {
            winPlayer.getMyExtend().setPdkFengshen(FirstmythConstants.firstmyth_index1, closeNum);
        }

        if(boomNum>0){
            winPlayer.changeBoomCount(boomNum);
            if(boomPointType==0&&boomBelong==1){
                for (Map.Entry<Integer, Integer> entry : lossPoint.entrySet()) {
                    int point=entry.getValue();
                    entry.setValue(point+10*boomNum);
                }
            }else if(boomPointType==1){
                for (Map.Entry<Integer, Integer> entry : lossPoint.entrySet()) {
                    int point=entry.getValue();
                    point = (int)(point*Math.pow(2,boomNum>boomCelling+1?boomCelling+1:boomNum));
                    entry.setValue(point);
                }
            }
        }

        for (Map.Entry<Integer, Integer> entry : lossPoint.entrySet()) {
            ZZPdkPlayer player = seatMap.get(entry.getKey());
            int point0 = entry.getValue();
            int point = calcRedTen(winPlayer, player, point0);
            if (point != point0) {
                entry.setValue(point);
            }
            winPoint += point;
        }
        int baoPeiSeat=0;
        //放走包赔
        if(!isTwoPlayer()&&danType==1){
            if(nowDisCardIds.size()==1&&noPassDisCard!=null&&noPassDisCard.size()>1) {
                PlayerCard playerCard = noPassDisCard.get(noPassDisCard.size() - 2);
                int lastSeat = calcLastSeat(winPlayer.getSeat());
                ZZPdkPlayer lastPlayer = seatMap.get(lastSeat);
                if (lastPlayer.getName() == playerCard.getName()) {
                    int yu=playerCard.getCards().get(0)%100;
                    for (Integer handId : seatMap.get(lastSeat).getHandPais()) {
                        if (yu < handId % 100) {
                            baoPeiSeat = lastSeat;
                            break;
                        }
                    }
                }
            }
        }


        Map<Long, Integer> ticketMap = new HashMap<>();
        Map<Long, Integer> outScoreMap = new HashMap<>();
        if (isMatchRoom()) {
            Map<Long, Integer> map = new HashMap<>();
            int tempScore = 0;
            int tempScore0 = 0;
            for (Map.Entry<Integer, Integer> entry : lossPoint.entrySet()) {
                ZZPdkPlayer player = seatMap.get(entry.getKey());
                int tempSc = -entry.getValue();
                tempScore += tempSc;
                map.put(player.getUserId(), tempSc);
                int tempSc0 = (int) (tempSc * matchRatio);
                tempScore0 += tempSc0;
                if(baoPeiSeat==0)
                    player.calcLost(this, 1, tempSc0);
            }
            if(baoPeiSeat!=0){
                seatMap.get(baoPeiSeat).calcLost(this, 1, tempScore0);
            }
            map.put(winPlayer.getUserId(), -tempScore);
            winPlayer.calcWin(this, 1, -tempScore0);
            changeMatchData(map);
        } else {
            if(baoPeiSeat!=0){
                seatMap.get(baoPeiSeat).calcLost(this, 1, -winPoint);
            }else {
                for (Map.Entry<Integer, Integer> entry : lossPoint.entrySet()) {
                    seatMap.get(entry.getKey()).calcLost(this, 1, -entry.getValue());
                }
            }
            winPlayer.calcWin(this, 1, winPoint);
        }


        //算飘分
        if(piaoFenType>0){
            for (ZZPdkPlayer p : seatMap.values()) {
                if(winPlayer.getUserId()!=p.getUserId()){
                    setWinLostPiaoFen(winPlayer,p);
                }
            }
            for (ZZPdkPlayer p : seatMap.values()) {
                p.setPoint(p.getPoint()+p.getWinLostPiaoFen());
                p.changeTotalPoint(p.getWinLostPiaoFen());
            }
        }

        boolean isOver = playBureau >= totalBureau;
        if(autoPlayGlob >0) {
//          //是否解散
            boolean diss = false;
            if(autoPlayGlob ==1) {
                for (ZZPdkPlayer seat : seatMap.values()) {
                    if(seat.isAutoPlay()) {
                        diss = true;
                        break;
                    }

                }
            }else if (autoPlayGlob == 3) {
				diss = checkAuto3();
			}
            if(diss) {
                autoPlayDiss= true;
                isOver =true;
            }
        }
        calcAfter();
        ClosingInfoRes.Builder res = sendAccountsMsg(isOver, winPlayer, false, outScoreMap,ticketMap);
        saveLog(isOver, winPlayer.getUserId(), res.build());
        setLastWinSeat(winPlayer.getSeat());
        if (isOver) {
            calcOver1();
            calcOver2();
            calcCreditNew();
            diss();

            for (Player player : seatMap.values()) {
                player.saveBaseInfo();
            }
        } else {
            initNext();
            calcOver1();
            for (Map.Entry<Integer, Player> kv : getSeatMap().entrySet()) {
                int seat = kv.getKey().intValue();
                if (seat != kv.getValue().getSeat()) {
                    LogUtil.errorLog.warn("table user seat error3:tableId={},userId={},seat={},auto change seat={}", id, kv.getValue().getUserId(), kv.getValue().getSeat(), seat);

                    kv.getValue().setSeat(seat);
                    kv.getValue().setPlayingTableId(id);
                    changePlayers();
                }
                kv.getValue().saveBaseInfo();
            }
        }

    }
    
	private boolean checkAuto3() {
		boolean diss = false;
			boolean diss2 = false;
			 for (ZZPdkPlayer seat : seatMap.values()) {
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
		return diss;
	}

    @Override
    public void calcDataStatistics2() {
        //俱乐部房间 单大局大赢家、单大局大负豪、总小局数、单大局赢最多、单大局输最多 数据统计
        if (isGroupRoom()) {
            String groupId = loadGroupId();
            int maxPoint = 0;
            int minPoint = 0;
            Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            //俱乐部活动总大局数
            calcDataStatistics3(groupId);

            //Long dataDate, String dataCode, String userId, String gameType, String dataType, int dataValue
            for (ZZPdkPlayer player : playerMap.values()) {
                //总小局数
                DataStatistics dataStatistics1 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "xjsCount", playedBureau);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics1, 3);
                //总大局数
                DataStatistics dataStatistics5 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "djsCount", 1);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5, 3);
                //总积分
                DataStatistics dataStatistics6 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "zjfCount", player.loadScore());
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics6, 3);
                if (player.loadScore() > 0) {
                    if (player.loadScore() > maxPoint) {
                        maxPoint = player.loadScore();
                    }
                    //单大局赢最多
                    DataStatistics dataStatistics2 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "winMaxScore", player.loadScore());
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics2, 4);
                } else if (player.loadScore() < 0) {
                    if (player.loadScore() < minPoint) {
                        minPoint = player.loadScore();
                    }
                    //单大局输最多
                    DataStatistics dataStatistics3 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "loseMaxScore", player.loadScore());
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics3, 5);
                }
            }

            for (ZZPdkPlayer player : playerMap.values()) {
                if (maxPoint > 0 && maxPoint == player.loadScore()) {
                    //单大局大赢家
                    DataStatistics dataStatistics4 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "dyjCount", 1);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4, 1);
                } else if (minPoint < 0 && minPoint == player.loadScore()) {
                    //单大局大负豪
                    DataStatistics dataStatistics5 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "dfhCount", 1);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5, 2);
                }
            }
        }
    }

    public int calcRedTen(ZZPdkPlayer winPlayer, ZZPdkPlayer player, int point) {
        if (this.getRedTen() > 0 && (winPlayer.getRedTenPai() == 1 || player.getRedTenPai() == 1)) {
            switch (this.getRedTen()) {
                case 1:
                    point += 5;
                    break;
                case 2:
                    point += 10;
                    break;
                case 3:
                    point *= 2;
                    break;
            }
        }
        return point;
    }

    public void saveLog(boolean over, long winId, Object resObject) {
        ClosingInfoRes res = (ClosingInfoRes) resObject;
        LogUtil.d_msg("tableId:" + id + " play:" + playBureau + " over:" + res);
        String logRes = JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResLog(res));
        String logOtherRes = JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResOtherLog(res));
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
        userLog.setMaxPlayerCount(max_player_count);
        userLog.setType(creditMode == 1 ? 2 : 1 );
        userLog.setGeneralExt(buildGeneralExtForPlaylog().toString());
        long logId = TableLogDao.getInstance().save(userLog);
        saveTableRecord(logId, over, playBureau);

        if (!isGoldRoom()) {
            for (ZZPdkPlayer player : playerMap.values()) {
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
            if (tempMap.containsKey("handPai1")) {
                tempMap.put("handPai1", StringUtil.implode(seatMap.get(1).getHandPais(), ","));
            }
            if (tempMap.containsKey("handPai2")) {
                tempMap.put("handPai2", StringUtil.implode(seatMap.get(2).getHandPais(), ","));
            }
            if (tempMap.containsKey("handPai3")) {
                tempMap.put("handPai3", StringUtil.implode(seatMap.get(3).getHandPais(), ","));
            }
            if (tempMap.containsKey("answerDiss")) {
                tempMap.put("answerDiss", buildDissInfo());
            }
            if (tempMap.containsKey("nowDisCardIds")) {
                tempMap.put("nowDisCardIds", StringUtil.implode(nowDisCardIds, ","));
            }
            if (tempMap.containsKey("extend")) {
                tempMap.put("extend", buildExtend());
            }
//			TableDao.getInstance().save(tempMap);
        }
        return tempMap.size() > 0 ? tempMap : null;
    }



    protected String buildPlayersInfo() {
        StringBuilder sb = new StringBuilder();
        for (ZZPdkPlayer pdkPlayer : playerMap.values()) {
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

    public int calcLastSeat(int seat) {
        return seat-1 ==0 ? getMaxPlayerCount() : seat-1;
    }

    /**
     * 开始发牌
     */
    public void fapai() {
        synchronized (this) {
            changeTableState(table_state.play);
            timeNum = 0;
            if (isGoldRoom()) {
                if (playedBureau == 0 || lastWinSeat <= 0) {
                    List<Long> list0 = new ArrayList<>(3);
                    try {
                        List<HashMap<String, Object>> list = GoldRoomDao.getInstance().loadRoomUsersLastResult(playerMap.keySet(), id);
                        if (list != null) {
                            for (HashMap<String, Object> map : list) {
                                if (NumberUtils.toInt(String.valueOf(map.getOrDefault("gameResult", "0")), 0) > 0) {
                                    list0.add(NumberUtils.toLong(String.valueOf(map.getOrDefault("userId", "0")), 0));
                                }
                            }
                        }
                    } catch (Exception e) {
                    }

                    if (list0.size() > 0) {
                        Long userId = list0.get(new SecureRandom().nextInt(list0.size()));
                        Player player = playerMap.get(userId);
                        if (player != null) {
                            setLastWinSeat(player.getSeat());
                        }
                    }
                    if (lastWinSeat <= 0) {
                        setLastWinSeat(new SecureRandom().nextInt(playerMap.size()));
                    }
                }
            }

        	ZZPdkPlayer winPlayer = null;
        	ZZPdkPlayer losePlayer = null;
        	List<PdkRateConfig> configs = new ArrayList<>();
			for (ZZPdkPlayer player : playerMap.values()) {
				PdkRateConfig config = getPdkRateConfig(player);
				if (config == null) {
					continue;
				}
				if (config.getType() <= 3) {
					if (winPlayer == null)
						winPlayer = player;
				} else {
					if (losePlayer == null)
						losePlayer = player;
				}
				configs.add(config);
			}

			if(GameServerConfig.isDebug()&&zp!=null){
                zuopai();
            }else if(!configs.isEmpty()){
            	 sysPeiPai(winPlayer, losePlayer, configs);
            }else{
            	 commonFaPai();
            }
        	setTableStatus(0);
        }
        finishFapai=1;
    }

	private void sysPeiPai(ZZPdkPlayer winPlayer, ZZPdkPlayer losePlayer, List<PdkRateConfig> configs) {
		List<List<Integer>> list =  CardTool.fapaiPingHeng(max_player_count, playType, configs, isNoBoom==1,zp);
		if(losePlayer!=null){
			losePlayer.dealHandPais(list.get(0), this);
			list.remove(0);
			PdkRateConfig config = CardTool.getXuanZCongfig(configs, 4);
			if(config!=null){
				faPaiGamLog(losePlayer,config.getType());
			}
		}
		if(winPlayer!=null){
			winPlayer.dealHandPais(list.get(0), this);
			list.remove(0);
			PdkRateConfig config = CardTool.getXuanZCongfig(configs, 1);
			if(config!=null){
				faPaiGamLog(winPlayer,config.getType());
			}
		}
		
		int i = 0;
		for (ZZPdkPlayer player : playerMap.values()) {
		    player.changeState(player_state.play);
		    if(player.getHandPais().isEmpty()){
		    	player.dealHandPais(list.get(i), this);
		    	i++;
		    	 faPaiGamLog(player,0);
		    }
		    if (getRedTen() > 0 && player.getHandPais().contains(310)) {//是否有红心10
		        player.setRedTenPai(1);
		    }
		    player.setIsNoLet(0);

		    if (!player.isAutoPlay()) {
		        player.setAutoPlay(false, this);
		        player.setLastOperateTime(System.currentTimeMillis());
		    }
		   
		}
		//三人
		if (isTwoPlayer()) {
		    this.cutCardList.clear();
		    this.cutCardList.addAll(list.get(i));
		}
	}

	private void faPaiGamLog(ZZPdkPlayer player, int peiType) {
		StringBuilder sb = new StringBuilder("zzpdk");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append(player.getName());
		sb.append("|").append(player.isAutoPlay() ? 1 : 0);
		if(peiType==0){
			sb.append("|").append("faPai");
		}else{
			sb.append("|").append("peiPai");
		}
		sb.append("|").append(player.getHandPais());
		sb.append("|").append(peiType);
		LogUtil.msgLog.info(sb.toString());
	}
    
    private PdkRateConfig getPdkRateConfig(ZZPdkPlayer player){
    	PdkRateConfig res = null;
    	List<PdkRateConfig> configs =   PdkRateConfigDao.getInstance().queryAllPdkConfig();
    	int bur = getPeiPaiBureau();
    	for(PdkRateConfig  config : configs){
    		if(config.getType()==Scheme.WIN_A&&config.getVal()<=player.getTotalPoint()){
    			res = config;
    		}else if(config.getType()==Scheme.WIN_B&&player.getCurrentLs()>=config.getVal()){
    			res = config;
    		}else if(config.getType()==Scheme.WIN_C&&player.getWinCount()>=bur){
    			res = config;
    		}else if(config.getType()==Scheme.LOSE_A&&config.getVal()<=-player.getTotalPoint()){
    			res = config;
    		}else if(config.getType()==Scheme.LOSE_B&&player.getCurrentLshu()>=config.getVal()){
    			res = config;
    		}else if(config.getType()==Scheme.LOSE_C&&player.getLostCount()>=bur){
    			res = config;
    		}
    		
    	}
    	return res;
    	
    }
    
    private int getPeiPaiBureau(){
    	int rate = totalBureau/10;
    	if(rate==0){
    		rate=1;
    	}
    	int bur = totalBureau/max_player_count+rate;
    	
    	return bur;
    	
    }

	private void commonFaPai() {
		List<List<Integer>> list;
		if(isNoBoom==1){
		    list = CardTool.fapaiNoBoom(max_player_count, playType, zp);
		}else {
		    list = CardTool.fapai(max_player_count, zp,cardNum);
		}
		
		int i = 0;
		for (ZZPdkPlayer player : playerMap.values()) {
		    player.changeState(player_state.play);
		    player.dealHandPais(list.get(i), this);
		    if (getRedTen() > 0 && list.get(i).contains(310)) {//是否有红心10
		        player.setRedTenPai(1);
		    }
		    player.setIsNoLet(0);
		    i++;

		    if (!player.isAutoPlay()) {
		        player.setAutoPlay(false, this);
		        player.setLastOperateTime(System.currentTimeMillis());
		    }
		    faPaiGamLog(player,0);
		}
		if (isTwoPlayer()) {
		    this.cutCardList.clear();
		    this.cutCardList.addAll(list.get(i));
		}
	}

    private void zuopai() {
        List<List<Integer>> list=CardTool.zuopai(max_player_count,cardNum,zp);
        int vurSeat=getNextDisCardSeat();
        for (int i = 0; i < max_player_count; i++) {
            ZZPdkPlayer player=seatMap.get(vurSeat);
            player.changeState(player_state.play);
            player.dealHandPais(list.get(i), this);
            player.setIsNoLet(0);
            if (!player.isAutoPlay()) {
                player.setAutoPlay(false, this);
                player.setLastOperateTime(System.currentTimeMillis());
            }
            faPaiGamLog(player,0);
            vurSeat=calcNextSeat(vurSeat);
        }
    }




    /**
     * 下一次出牌的seat
     *
     * @return
     */
    public int getNextDisCardSeat() {
        int seat = 0;
        if (state != table_state.play) {
            return seat;
        }
        if (disCardRound == 0) {
            if (lastWinSeat == 0) {
                // 还没有出过牌 看黑桃3在谁手里
                for (ZZPdkPlayer player : playerMap.values()) {
                	
                	
                	if(getPlayType() == GameUtil.play_type_11){
                		 if (player.getHandPais().contains(406)) {
                             seat = player.getSeat();
                             return seat;
                         }
                	}else{
                		 if (player.getHandPais().contains(403)) {
                             seat = player.getSeat();
                             return seat;
                         }
                	}
                   
                }
                // 当黑桃3在切牌里面时
                if (0 == seat) {
                    for (int temp : seatMap.keySet()) {
                        if (seat == 0) {
                            seat = temp;
                        } else {
                            if (seat > temp) {
                                seat = temp;
                            }
                        }
                    }
//					seat = RandomUtils.nextInt(2) + 1;
                }
            } else {
                return lastWinSeat;
            }

        } else {
            if (nowDisCardSeat != 0) {
                seat = nowDisCardSeat >= max_player_count ? 1 : nowDisCardSeat + 1;
            }
        }
        return seat;
    }

    public ZZPdkPlayer getPlayerBySeat(int seat) {
        int next = seat >= max_player_count ? 1 : seat + 1;
        return seatMap.get(next);

    }

    public Map<Integer, Player> getSeatMap() {
        Object o = seatMap;
        return (Map<Integer, Player>) o;
    }

    public int getChai4Zha() {
        return chai4Zha;
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
            for (ZZPdkPlayer player : playerMap.values()) {
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

                if (player.getSeat() == disCardSeat && nowDisCardIds != null && nowDisCardIds.size()>0) {
                    playerRes.addAllOutCardIds(nowDisCardIds);
                    int cardType = CardUtils.cardResult2ReturnType(cardClas);
                    playerRes.addRecover(cardType);
                }
                players.add(playerRes.build());
            }
            res.addAllPlayers(players);
            int nextSeat = getNextDisCardSeat();
            if (nextSeat != 0) {
                res.setNextSeat(nextSeat);
            }
            res.setRenshu(this.max_player_count);
            res.addExt(this.showCardNumber);//0
            res.addExt(this.isFirstRoundDisThree);//1
            res.addExt(this.payType);//2
            res.addExt(this.redTen);//3

            res.addExt(CommonUtil.isPureNumber(modeId) ? Integer.parseInt(modeId) : 0);//5
            int ratio;
            int pay;
            MatchBean matchBean = isMatchRoom() ? JjsUtil.loadMatch(matchId) : null;
            if (matchBean != null) {
                ratio = (int) matchRatio;
                pay = 0;
            } else if (isGoldRoom()) {
                ratio = GameConfigUtil.loadGoldRatio(modeId);
                pay = PayConfigUtil.get(playType, totalBureau, max_player_count, payType == 1 ? 0 : 1, modeId);
            } else {
                ratio = 1;
                pay = consumeCards() ? loadPayConfig(payType) : 0;
            }

            res.addExt(ratio);//6
            res.addExt(pay);//7
            res.addExt(lastWinSeat);//8
            if (matchBean != null) {
                int num = JjsUtil.loadMatchCurrentGameNo(matchBean);
                res.addExt(num);//9
                res.addExt(num == 0 ? JjsUtil.loadMinScore(matchBean, getMatchRatio()) : 0);//10
            } else {
                res.addExt(0);
                res.addExt(0);
            }
            res.addExtStr(String.valueOf(matchId));//0
            res.addExtStr(cardMarkerToJSON());//1
            res.addTimeOut((isGoldRoom() || autoPlay) ? autoTimeOut : 0);
            if (matchBean != null) {
                if (disCardRound == 0) {
                    res.addTimeOut((autoTimeOut + 5000));
                } else {
                    res.addTimeOut(autoTimeOut);
                }
            } else if(autoPlay){
                if (disCardRound == 0) {
                    res.addTimeOut((autoTimeOut + 5000));
                } else {
                    res.addTimeOut(autoTimeOut);
                }
            }else{
                res.addTimeOut(0);
            }

            res.addExt(playedBureau);//11
            res.addExt(disCardRound);//12
            res.addExt(card3Eq);//13
            res.addExt(creditMode); //14
            res.addExt(0);
            res.addExt(0);
            res.addExt(0);
            res.addExt(0);
            res.addExt(creditCommissionMode1);//19
            res.addExt(creditCommissionMode2);//20
            res.addExt(autoPlay ? 1 : 0);//21
            res.addExt(jiaBei); //22
            res.addExt(jiaBeiFen);//23
            res.addExt(jiaBeiShu);//24
            res.addExt(tableStatus);//25
        }

        return res.build();
    }

    public int getOnTablePlayerNum() {
        int num = 0;
        for (ZZPdkPlayer player : seatMap.values()) {
            if (player.getIsLeave() == 0) {
                num++;
            }
        }
        return num;
    }

    public void notLet(ZZPdkPlayer player) {
        // 要不起
        setNowDisCardSeat(player.getSeat());
        player.setIsNoLet(1);
        List<Integer> cards = new ArrayList<>();
        cards.add(0);
        player.addOutPais(cards, this);
        PlayCardRes.Builder res = PlayCardRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setCardType(0);
        res.setIsPlay(1);
        if (player.getHandPais().size() == 1) {
            // 报单
            res.setIsBt(1);
        }

        for (ZZPdkPlayer pdkPlayer : seatMap.values()) {
            PlayCardRes.Builder copy = res.clone();
            List<Integer> canPlayList = CardTypeTool.canPlay(pdkPlayer.getHandPais(), nowDisCardIds, false, pdkPlayer, this, cardClas);
            if (disCardSeat == pdkPlayer.getSeat() || !canPlayList.isEmpty()) {
                copy.setIsLet(1);

            }
            pdkPlayer.writeSocket(copy.build());
        }
    }


    public void disCards(ZZPdkPlayer player, List<Integer> cards,Result outType) {
        if (disCardSeat == player.getSeat()) {
            // 新一轮开始
            clearIsNotLet();
        } else {
            player.setIsNoLet(0);
        }
        int cardType = cardClas.getType();
        if (cardClas.isFeiJi() ||outType.isFeiJi()) {
            // 飞机
            player.getMyExtend().setPdkFengshen(FirstmythConstants.firstmyth_index5, 1);
        }
        cards = CardUtils.loadSortCards(cards, outType,this);

        setDisCardSeat(player.getSeat());
        player.addOutPais(cards, this);
        setCardClas(outType);
        setNowDisCardIds(cards);
        setNowDisCardSeat(player.getSeat());
        if (cards != null) {
            noPassDisCard.add(new PlayerCard(player.getName(), cards));
        }

        // 构建出牌消息
        PlayCardRes.Builder res = PlayCardRes.newBuilder();
        res.addAllCardIds(getNowDisCardIds());
        if (isNewRound()) {
            //推送消息清桌子
//            res.setIsClearDesk(1);
            if (outType.isSanDai()) {
                setIsFirstCardType32(1);
            } else {
                setIsFirstCardType32(0);
            }
        }
        res.setIsClearDesk(0);
        cardType = CardUtils.cardResult2ReturnType(outType);
        res.setCardType(cardType);
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setIsPlay(2);
        if (player.getHandPais().size() == 1) {
            res.setIsBt(1);
        }
        boolean let = false;
        boolean isOver = player.getHandPais().size() == 0;

        Map<Long, Integer> stateMap = new HashMap<>();
        int passNum = 0;
        for (ZZPdkPlayer pdkPlayer : seatMap.values()) {
            PlayCardRes.Builder copy = res.clone();
            if (pdkPlayer.getUserId() == player.getUserId()) {
                pdkPlayer.writeSocket(copy.build());
                continue;
            }
            if (isOver) {
                // 如果玩家出完了最后一张牌，不需要提示要不起
                copy.setIsLet(0);

            }else {
                List<Integer> canPlayList = CardTypeTool.canPlay(pdkPlayer.getHandPais(), nowDisCardIds, false, pdkPlayer, this,cardClas);
                if (canPlayList.size() > 0) {
                    if(canPlayList.size()==pdkPlayer.getHandPais().size()&&card3Eq==1){
                        pdkPlayer.setAutoFinalCards(canPlayList);
                    }
                    copy.setIsLet(1);
                    let = true;
                    stateMap.put(pdkPlayer.getUserId(), 1);
                } else {
                    stateMap.put(pdkPlayer.getUserId(), 0);
                    passNum++;
                    pdkPlayer.getAutoFinalCards().clear();
                }

                if (passNum >= max_player_count - 1) {
                    setReplayDisCard();
                    for (ZZPdkPlayer p : seatMap.values()) {
                        p.writeSocket(SendMsgUtil.buildComRes(WebSocketMsgType.req_code_pdk_playBack, getReplayDisCard()).build());
                    }
                }

            }
            pdkPlayer.writeSocket(copy.build());
        }

        if (outType.getType() == Result.zhadan) {
            if (!let) {
                // 别人打不起 算炸弹积分
                if(boomPointType==0){
                    if(boomBelong==0){
                        for (ZZPdkPlayer pdkPlayer : seatMap.values()) {
                            if (pdkPlayer.getUserId() == player.getUserId()) {
                                if (isTwoPlayer()) {
                                    pdkPlayer.changePlayBoomPoint(10);
                                } else {
                                    pdkPlayer.changePlayBoomPoint(20);
                                }
                            } else {
                                pdkPlayer.changePlayBoomPoint(-10);
                            }
                        }
                    }else {
                        changeBoomNum();
                    }
                }else if(boomPointType==1){
                    changeBoomNum();
                }
            }
        }
        if(!let){
            setNewRound(true);
            setIsFirstCardType32(0);
            //当其他人要不起后，检查手牌是否可以一次出完
            CardUtils.Result result = CardUtils.calcCardValue(CardUtils.loadCards(player.getHandPais()), this,sdscw==1);
            Map<Integer, Integer> boom = CardUtils.findBoom(CardUtils.loadCards(player.getHandPais()), AAAZha == 1);
            boolean lastCards=true;
            if(result.getType()==0)
                lastCards=false;
            if(boom.size()>1)
                lastCards=false;
            if(boom.size()==1){
                for (Integer count:boom.values()) {
                    if(count!=player.getHandPais().size())
                        lastCards=false;
                }
            }
            if(lastCards)
                player.setAutoFinalCards(new ArrayList<>(player.getHandPais()));
        }else{
            setNewRound(false);
        }
        if (isOver) {
            state = table_state.over;
        } else {
            int nextSeat = calcNextSeat(player.getSeat());
            ZZPdkPlayer nextPlayer = seatMap.get(nextSeat);
            while (nextSeat != player.getSeat() && nextPlayer.getHandPais().size() == 0) {
                nextSeat = calcNextSeat(nextPlayer.getSeat());
                nextPlayer = seatMap.get(nextSeat);
            }

            Integer state = stateMap.remove(nextPlayer.getUserId());
            while (state != null && state.intValue() == 0) {

                if (nextPlayer.getUserId() != player.getUserId()) {
                    playCommand(nextPlayer, null,null);
                }

                nextSeat = calcNextSeat(nextPlayer.getSeat());
                nextPlayer = seatMap.get(nextSeat);
                if (nextPlayer == null) {
                    break;
                }
                while (nextSeat != player.getSeat() && nextPlayer.getHandPais().size() == 0) {
                    nextSeat = calcNextSeat(nextPlayer.getSeat());
                    nextPlayer = seatMap.get(nextSeat);
                }

                state = stateMap.remove(nextPlayer.getUserId());
            }
        }

    }

    public void setReplayDisCard(){
        List<PlayerCard> cards = new ArrayList<>();
        int size = noPassDisCard.size();
        for (int i = 0; i < 3&&i<size; i++) {
            cards.add(noPassDisCard.get(size-1-i));
        }
        setReplayDisCard(cards.toString());
        noPassDisCard.clear();
    }

    /**
     * 清理要不起的状态
     */
    public void clearIsNotLet() {
        for (ZZPdkPlayer player : seatMap.values()) {
            player.setIsNoLet(0);
        }
    }


    /**
     * 打牌
     *
     * @param player
     * @param cards
     */
    public void playCommand(ZZPdkPlayer player, List<Integer> cards,Result outType) {
        synchronized (this) {
            if (state != table_state.play) {
                return;
            }
            addPlayLog(player.getSeat(), cards, ",");
            StringBuilder sb = new StringBuilder("zzpdk");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.isAutoPlay() ? 1 : 0);
            sb.append("|").append("chuPai");
            sb.append("|").append(cards);
            LogUtil.msgLog.info(sb.toString());
            if (cards != null && cards.size() > 0) {
                changeDisCardRound(1);
                // 出牌了
                disCards(player, cards,outType);
            } else {
                if (disCardRound > 0) {
                    changeDisCardRound(1);
                }
                notLet(player);
            }

            setLastActionTime(TimeUtil.currentTimeMillis());

            if (isOver()) {
                calcOver();
            } else {
                int nextSeat = calcNextSeat(player.getSeat());
                ZZPdkPlayer nextPlayer = seatMap.get(nextSeat);
                if (!nextPlayer.isRobot()) {
                    nextPlayer.setNextAutoDisCardTime(TimeUtil.currentTimeMillis() + autoTimeOut);
                }
            }
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
        for (ZZPdkPlayer player : seatMap.values()) {
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

    @Override
    protected void initNext1() {
        setNowDisCardIds(null);
        setIsFirstCardType32(0);
        replayDisCard="";
        timeNum = 0;
        newRound = true;
        finishFapai=0;
        isSendPiaoFenMsg=0;
        boomNum=0;
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
        int nextSeat = getNextDisCardSeat();
        ZZPdkPlayer nextPlayer = seatMap.get(nextSeat);
        int timeout;
        if (!nextPlayer.isRobot()) {
            if (matchId > 0L && disCardRound == 0) {
                timeout = autoTimeOut + 5000;
            } else if(autoPlay && disCardRound == 0){
                timeout = autoTimeOut + 5000;
            }else{
                timeout = autoTimeOut;
            }
            nextPlayer.setNextAutoDisCardTime(TimeUtil.currentTimeMillis() + timeout);
        } else {
            timeout = autoTimeOut;
        }

        for (ZZPdkPlayer tablePlayer : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            res.addAllHandCardIds(tablePlayer.getHandPais());
            res.setNextSeat(nextSeat);
            res.setGameType(getWanFa());// 1跑得快 2麻将
            res.setBanker(lastWinSeat);

            res.addXiaohu(nextPlayer.isAutoPlay() ? 1 : 0);
            res.addXiaohu(timeout);

            tablePlayer.writeSocket(res.build());
            
            if(tablePlayer.isAutoPlay()) {
       		 addPlayLog(tablePlayer.getSeat(), PdkConstants.action_tuoguan + "",1 + "");
            }
            if(tablePlayer.getSeat()==nextSeat&&playType == GameUtil.play_type_15){
                CardUtils.Result result = CardUtils.calcCardValue(CardUtils.loadCards(tablePlayer.getHandPais()), this,false);
                Map<Integer, Integer> boom = CardUtils.findBoom(CardUtils.loadCards(tablePlayer.getHandPais()), AAAZha == 1);
                boolean lastCards=true;
                if(result.getType()==0)
                    lastCards=false;
                if(boom.size()>1)
                    lastCards=false;
                if(boom.size()==1){
                    for (Integer count:boom.values()) {
                        if(count!=tablePlayer.getHandPais().size())
                            lastCards=false;
                    }
                }
                if(lastCards)
                    tablePlayer.setAutoFinalCards(new ArrayList<>(tablePlayer.getHandPais()));
            }
        }
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
        return max_player_count;
    }


    public void setMaxPlayerCount(int maxPlayerCount) {
        this.max_player_count = maxPlayerCount;
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

    public void setCardClas(Result cardClas) {
        this.cardClas = cardClas;
        changeExtend();
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

    public boolean createSimpleTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, boolean saveDb) throws Exception {
        return createTable(player, play, bureauCount, params, saveDb);
    }

    public void createTable(Player player, int play, int bureauCount, List<Integer> params) throws Exception {
        createTable(player, play, bureauCount, params, true);
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
        return sendAccountsMsg(over, winPlayer, isBreak, null,null);
    }

    /**
     * 发送结算msg
     *
     * @param over      是否已经结束
     * @param winPlayer 赢的玩家
     * @return
     */
    public ClosingInfoRes.Builder sendAccountsMsg(boolean over, Player winPlayer, boolean isBreak, Map<Long, Integer> outScoreMap,Map<Long, Integer> ticketMap) {
        List<ClosingPlayerInfoRes> list = new ArrayList<>();
        List<ClosingPlayerInfoRes.Builder> builderList = new ArrayList<>();

        int minPointSeat = 0;
        int minPoint = 0;
        if (winPlayer != null) {
            for (ZZPdkPlayer player : seatMap.values()) {
                if (player.getUserId() == winPlayer.getUserId()) {
                    continue;
                }
                if (minPoint == 0 || player.getPoint() < minPoint) {
                    minPoint = player.getPlayPoint();
                    minPointSeat = player.getSeat();
                }
            }
        }

        // 算打鸟分
        if (over && daNiaoFen > 0) {
            for (ZZPdkPlayer winner : seatMap.values()) {
                for (ZZPdkPlayer loser : seatMap.values()) {
                    if (loser.getSeat() != winner.getSeat() && winner.getTotalPoint() > loser.getTotalPoint()) {
                        int niaoFen = (winner.getNiaoFen() + loser.getNiaoFen());
                        winner.setTotalPoint(winner.getTotalPoint() + niaoFen);
                        loser.setTotalPoint(loser.getTotalPoint() - niaoFen);
                    }
                }
            }
        }

        //大结算计算加倍分
        if(over && jiaBei == 1){
            int jiaBeiPoint = 0;
            int loserCount = 0;
            for (ZZPdkPlayer player : seatMap.values()) {
                if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                    jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
                    player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
                } else if (player.getTotalPoint() < 0) {
                    loserCount++;
                }
            }
            if (jiaBeiPoint > 0) {
                for (ZZPdkPlayer player : seatMap.values()) {
                    if (player.getTotalPoint() < 0) {
                        player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                    }
                }
            }
        }
        //大结算低于below分+belowAdd分
        if(over&&belowAdd>0&&playerMap.size()==2){
            for (ZZPdkPlayer player : seatMap.values()) {
                int totalPoint = player.getTotalPoint();
                if (totalPoint >-below&&totalPoint<0) {
                    player.setTotalPoint(player.getTotalPoint()-belowAdd);
                }else if(totalPoint < below&&totalPoint>0){
                    player.setTotalPoint(player.getTotalPoint()+belowAdd);
                }
            }
        }


        for (ZZPdkPlayer player : seatMap.values()) {
            ClosingPlayerInfoRes.Builder build = null;
            if (over) {
                build = player.bulidTotalClosingPlayerInfoRes(this);
            } else {
                build = player.bulidOneClosingPlayerInfoRes(this);

            }
            //添加本局所有牌和炸弹分
            //所有牌
            //添加手牌
            List<Integer> allCard = new ArrayList<Integer>();
            for (Integer v : player.getHandPais()) {
                if (!allCard.contains(v)) {
                    allCard.add(v);
                }
            }
            //添加已出的牌
            for (List<Integer> c : player.getOutPais()) {
                for (Integer v : c) {
                    if (!allCard.contains(v)) {
                        allCard.add(v);
                    }
                }
            }

            JSONArray jsonArray = new JSONArray();
            for (int card : allCard) {
                if (card != 0) {
                    jsonArray.add(card);
                }
            }
            build.addExt(jsonArray.toString()); //0
            //炸弹分
            build.addExt(player.getPlayBoomPoint() + "");//1
            build.addExt(player.getRedTenPai() + "");//2

            if (isGoldRoom()) {
                build.addExt("1");//3
                build.addExt(player.loadAllGolds() <= 0 ? "1" : "0");//4
                build.addExt(outScoreMap == null ? "0" : outScoreMap.getOrDefault(player.getUserId(), 0).toString());//5
            } else {
                build.addExt("0");//3
                build.addExt("0");//4
                build.addExt("0");//5
            }

            build.addExt(String.valueOf(player.getCurrentLs()));//6
            build.addExt(String.valueOf(player.getMaxLs()));//7
            build.addExt(String.valueOf(matchId));//8
            build.addExt(ticketMap==null?"0":String.valueOf(ticketMap.getOrDefault(player.getUserId(),0)));//9

            if (winPlayer != null) {
                if (player.getSeat() == minPointSeat) {
                    build.setIsHu(1);
                    player.changeCutCard(1);
                } else {
                    build.setIsHu(0);
                    player.changeCutCard(0);
                }
            }

            if (winPlayer != null && player.getUserId() == winPlayer.getUserId()) {
                // 手上没有剩余的牌放第一位为赢家
                builderList.add(0, build);
            } else {
                builderList.add(build);
            }

            //信用分
            if(isCreditTable()){
                player.setWinLoseCredit(player.getTotalPoint() * creditDifen);
            }

        }

        //信用分计算
        if (isCreditTable()) {
            //计算信用负分
            calcNegativeCredit();

            long dyjCredit = 0;
            for (ZZPdkPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingPlayerInfoRes.Builder builder : builderList) {
                ZZPdkPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);

                builder.addExt(player.getWinLoseCredit() + "");      //10
                builder.addExt(player.getCommissionCredit() + "");   //11
                builder.addExt(player.getNiaoFen()+"");// 12

                // 2019-02-26更新
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else {
            for (ClosingPlayerInfoRes.Builder builder : builderList) {
                ZZPdkPlayer player = seatMap.get(builder.getSeat());
                builder.addExt(0 + ""); //10
                builder.addExt(0 + ""); //11
                builder.addExt(player.getNiaoFen()+"");// 12
            }
        }
        for (ClosingPlayerInfoRes.Builder builder : builderList) {
            ZZPdkPlayer player = seatMap.get(builder.getSeat());
            builder.addExt(player.getPiaoFen() + ""); //13
            list.add(builder.build());
        }

        ClosingInfoRes.Builder res = ClosingInfoRes.newBuilder();
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.addAllClosingPlayers(list);
        res.addAllExt(buildAccountsExt(over?1:0));
        if (isTwoPlayer()) {
            res.addAllCutCard(this.cutCardList);
        }
        if (over && isGroupRoom() && !isCreditTable()) {
            res.setGroupLogId((int) saveUserGroupPlaylog());
        }
        for (ZZPdkPlayer player : seatMap.values()) {
            player.writeSocket(res.build());
        }
        return res;
    }

    public List<String> buildAccountsExt(int over) {
        List<String> ext = new ArrayList<>();
        ext.add(id + "");//0
        ext.add(masterId + "");//1
        ext.add(TimeUtil.formatTime(TimeUtil.now()));//2
        ext.add(playType + "");//3
        //设置当前第几局
        ext.add(playBureau + "");//4
        ext.add(isGroupRoom() ? "1" : "0");//5
        //金币场大于0
        ext.add(CommonUtil.isPureNumber(modeId) ? modeId : "0");//6
        int ratio;
        int pay;
        if (isMatchRoom()) {
            ratio = (int) matchRatio;
            pay = 0;
        } else if (isGoldRoom()) {
            ratio = GameConfigUtil.loadGoldRatio(modeId);
            pay = PayConfigUtil.get(playType, totalBureau, max_player_count, payType == 1 ? 0 : 1, modeId);
        } else {
            ratio = 1;
            pay = loadPayConfig(payType);
        }
        ext.add(String.valueOf(ratio));//7
        ext.add(String.valueOf(pay >= 0 ? pay : 0));//8
        ext.add(String.valueOf(payType));//9
        ext.add(String.valueOf(playedBureau));//10

        ext.add(String.valueOf(matchId));//11
        ext.add(isGroupRoom() ? loadGroupId() : "");//12

        ext.add(creditMode + ""); //13
        ext.add(creditJoinLimit + "");//14
        ext.add(creditDissLimit + "");//15
        ext.add(creditDifen + "");//16
        ext.add(creditCommission + "");//17
        ext.add(creditCommissionMode1 + "");//18
        ext.add(creditCommissionMode2 + "");//19
        ext.add((autoPlay ? 1 : 0) + "");//20
        ext.add(jiaBei + "");//21
        ext.add(jiaBeiFen + "");//22
        ext.add(jiaBeiShu + "");//23
        ext.add(over+""); // 24
        ext.add(daNiaoFen+""); // 25
        ext.add(chai4Zha+""); // 25
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
        return ZZPdkPlayer.class;
    }

    @Override
    public int getWanFa() {
        return GameUtil.game_type_zzpdk;
    }

    @Override
    public void checkReconnect(Player player) {
        ZZPdkTable table = player.getPlayingTable(ZZPdkTable.class);
        player.writeSocket(SendMsgUtil.buildComRes(WebSocketMsgType.req_code_pdk_playBack, table.getReplayDisCard()).build());
        sendPiaoReconnect(player);
    }

    private void sendPiaoReconnect(Player player){
        if(piaoFenType==0||max_player_count!=getPlayerCount())
            return;
        int count=0;
        for(Map.Entry<Integer, ZZPdkPlayer> entry:seatMap.entrySet()){
            player_state state = entry.getValue().getState();
            if(state==player_state.play||state==player_state.ready)
                count++;
        }
        if(count!=max_player_count)
            return;

        for(Map.Entry<Integer, ZZPdkPlayer> entry:seatMap.entrySet()){
            ZZPdkPlayer p = entry.getValue();
            if(p.getUserId()==player.getUserId()){
                if(!p.isAlreadyPiaoFen()){
                    player.writeComMessage(WebSocketMsgType.res_code_pdk_piaofen,piaoFenType);
                    continue;
                }
            }
        }
        for(Map.Entry<Integer, ZZPdkPlayer> entry:seatMap.entrySet()){
            ZZPdkPlayer p = entry.getValue();
            if(p.getUserId()!=player.getUserId()){
                List<Integer> l=new ArrayList<>();
                l.add((int)p.getUserId());
                l.add(p.getPiaoFen());
                player.writeComMessage(WebSocketMsgType.res_code_pdk_broadcast_piaofen, l);
            }
        }

    }

    public void setWinLostPiaoFen(ZZPdkPlayer win, ZZPdkPlayer lost) {
        lost.setWinLostPiaoFen(-win.getPiaoFen()-lost.getPiaoFen());
        win.setWinLostPiaoFen(win.getWinLostPiaoFen()+win.getPiaoFen()+lost.getPiaoFen());
    }

    // 是否二人跑得快
    public boolean isTwoPlayer() {
        return max_player_count == 2;
    }

    // 是否显示剩余牌的数量
    public boolean isShowCardNumber() {
        return 1 == getShowCardNumber();
    }

    public void checkCompetitionPlay() {
        synchronized (this) {
            checkAutoPlay();

            checkCanFinalDis();
        }
    }

    public void checkCanFinalDis() {
        ZZPdkPlayer player = seatMap.get(getNextDisCardSeat());
        if(player!=null&&player.getAutoFinalCards().size()>0)
            playCommand(player,player.getAutoFinalCards(),player.getFinalClas());
    }

    @Override
    public void checkAutoPlay() {
        synchronized (this) {
            if(!autoPlay){
                return;
            }
            // 发起解散，停止倒计时
            if (getSendDissTime() > 0) {
                for (ZZPdkPlayer player : seatMap.values()) {
                    if (player.getLastCheckTime() > 0) {
                        player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                    }
                }
                return;
            }

            if (isAutoPlayOff()) {
                // 托管关闭
                for (int seat : seatMap.keySet()) {
                    ZZPdkPlayer player = seatMap.get(seat);
                    player.setAutoPlay(false, this);
                    player.setLastOperateTime(System.currentTimeMillis());
                }
                return;
            }

            // 准备托管
            if (state == table_state.ready && playedBureau > 0 ) {
                ++timeNum;
                for (ZZPdkPlayer player : seatMap.values()) {
                    // 玩家进入托管后，5秒自动准备
                    if (timeNum >= 5 && player.isAutoPlay()) {
                        autoReady(player);
                    } else if (timeNum >= 30) {
                        autoReady(player);
                    }
                }
                return;
            }
            // 打鸟托管
            if (tableStatus == PdkConstants.TABLE_STATUS_DANIAO) {
                for (ZZPdkPlayer player : seatMap.values()) {
                    if(player.getNiaoFen() != -1){
                        continue;
                    }
                    boolean auto = checkPlayerAuto(player, autoTimeOut);
                    if (auto) {
                        daNiao(player, 0);
                    }
                }
                return;
            }else if (daNiaoFen==0&&piaoFenType>0&&isSendPiaoFenMsg == 1 && finishFapai == 0) {
                //飘分模式，还没发牌
                for (ZZPdkPlayer player : seatMap.values()) {
                    if(player.isAlreadyPiaoFen()){
                        continue;
                    }
                    boolean auto = checkPlayerAuto(player, autoTimeOut);
                    if (auto) {
                        piaoFen(player, piaoFenType>=3?0:piaoFenType);
                    }
                }
                return;
            }

            if(state != table_state.play){
                return;
            }

            int timeout;
            ZZPdkPlayer player = seatMap.get(getNextDisCardSeat());
            if (player == null) {
                return;
            } else if (isGoldRoom()) {
                if (isMatchRoom()) {
                    if ("self".equals(player.getPf()) && player.getName().equals("test" + player.getUserId())) {
                        timeout = 2000;
                    } else if (disCardRound == 0) {
                        timeout = autoTimeOut + 5000;
                    } else {
                        timeout = autoTimeOut;
                    }
                } else {
                    timeout = autoTimeOut;
                }
            }else if(autoPlay){
                timeout = autoTimeOut;
                if (disCardRound == 0) {
                    timeout = autoTimeOut ;
                }
            } else if (player.isRobot()) {
                timeout = 3 * SharedConstants.SENCOND_IN_MINILLS;
            }else{
                return;
            }
            long autoPlayTime = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoPlayTimePdk", 2 * 1000);
            long now = TimeUtil.currentTimeMillis();
            boolean auto = player.isAutoPlay();
            if (!auto) {
                if (GameServerConfig.isAbroad()) {
                    if (!player.isRobot() && now >= player.getNextAutoDisCardTime()) {
                        auto = true;
                        player.setAutoPlay(true, this);
                    }
                } else {
                    auto = checkPlayerAuto(player,timeout);
                }
            }

            if (auto || player.isRobot()) {
                boolean autoPlay = false;
                if (GameServerConfig.isAbroad()) {
                    if (player.isRobot()) {
                        autoPlayTime = MathUtil.mt_rand(2, 6) * 1000;
                    } else {
                        autoPlay = true;
                    }
                }
                if (player.getAutoPlayTime() == 0L && !autoPlay) {
                    player.setAutoPlayTime(now);
                } else if (autoPlay || (player.getAutoPlayTime() > 0L && now - player.getAutoPlayTime() >= autoPlayTime)) {
                    player.setAutoPlayTime(0L);

                    if (state == table_state.play) {
                        // 检查黑桃3出头，有黑桃3必出黑桃3，没有的话交给下面的逻辑处理
                        if(checkHeiTao3ChuTou(player)){
                            return;
                        }
                        List<Integer> curList = new ArrayList<>(player.getHandPais());
                        if (curList.isEmpty()) {
                            return;
                        }
                        List<Integer> oppo;
                        if (disCardSeat != player.getSeat()) {
                            oppo = getNowDisCardIds();
                        } else {
                            oppo = null;
                        }

                        boolean nextDan = seatMap.get(calcNextSeat(player.getSeat())).getHandPais().size() == 1;
                        oppo = oppo == null ? null : new ArrayList<>(oppo);
                        List<Integer> list = CardTypeTool.getBestAI2(curList, oppo, nextDan, this,cardClas);
                        Result result=null;
                        if(list.size()==player.getHandPais().size()){
                            if((oppo==null||oppo.size()==0)&&sdscw==1){
                                result=CardUtils.calcCardValue(CardUtils.loadCards(list), this,true);
                            }else if(oppo!=null&&oppo.size()>0&&card3Eq==1){
                                result=CardUtils.calcCardValue(CardUtils.loadCards(list), this,true);
                            }
                        }
                        if(result==null)
                            result=CardUtils.calcCardValue(CardUtils.loadCards(list), this,false);
                        playCommand(player,list,result);
                    }
                }
            }
        }
    }

    /**
     * 托管时，检查黑桃3出头
     *
     * @param player
     * @return
     */
    public boolean checkHeiTao3ChuTou(ZZPdkPlayer player) {
        if (getIsFirstRoundDisThree() != 1) {
            return false;
        }
        if (getPlayBureau() != 1 || getDisCardRound() != 0) {
            return false;
        }
        List<Integer> curList = new ArrayList<>(player.getHandPais());
        List<Integer> card3List = new ArrayList<>();
        boolean hasHeiTao3 = false;
        Iterator<Integer> iterator = curList.iterator();
        while (iterator.hasNext()) {
            int card = iterator.next();
            if (CardTool.loadCardValue(card) == 3||(getPlayType()== GameUtil.play_type_11&&CardTool.loadCardValue(card) == 6)) {
                if (card == 403||(getPlayType()== GameUtil.play_type_11&&card == 406)) {
                    hasHeiTao3 = true;
                    if (card3List.size() > 0) {
                        card3List.add(0, card);
                    } else {
                        card3List.add(card);
                    }
                } else {
                    card3List.add(card);
                }
                iterator.remove();
            }
        }
        if (!hasHeiTao3) {
            return false;
        }
        if (card3List.size() == 1 || card3List.size() == 2 || card3List.size() == 4) {
            // 1张，2张，4张直接出
            playCommand(player, card3List,CardUtils.calcCardValue(CardUtils.loadCards(card3List),this,false));
        } else {
            // 3张，取两张带牌
            Map<Integer, Integer> map = CardTool.loadCards(curList);
            List<Integer> retList = new ArrayList<>();
            // 找带的牌,依次从1张，2张，3张的牌中找一对带的牌
            for (int i = 1; i <= 2; i++) {
                for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                    if (kv.getValue().intValue() == i) {
                        retList.addAll(CardTool.loadCards(curList, kv.getKey().intValue()));
                    }
                    if (retList.size() >= 2) {
                        break;
                    }
                }
                if (retList.size() >= 2) {
                    break;
                }
            }
            if (retList.size() >= 2) {
                card3List.addAll(retList.subList(0, 2));
                playCommand(player, card3List,CardUtils.calcCardValue(CardUtils.loadCards(card3List),this,false));
            } else {
                // 再没找到，出一对3
                List<Integer> list = card3List.subList(0, 2);
                playCommand(player, card3List,CardUtils.calcCardValue(CardUtils.loadCards(list),this,false));
            }
        }
        return true;
    }

    public boolean checkPlayerAuto(ZZPdkPlayer player , int timeout){
        if(player.isAutoPlay()){
            return true;
        }
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
            player.setAutoPlayCheckedTimeAdded(false);
        }

        return auto;
    }

    private String cardMarkerToJSON() {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<Integer, ZZPdkPlayer> entry : seatMap.entrySet()) {
            jsonObject.put("" + entry.getKey(), entry.getValue().getOutPais());
        }
        return jsonObject.toString();
    }

    public int getIsFirstRoundDisThree() {
        return isFirstRoundDisThree;
    }

    public void setIsFirstRoundDisThree(int isFirstRoundDisThree) {
        this.isFirstRoundDisThree = isFirstRoundDisThree;
        changeExtend();
    }

    public int getShowCardNumber() {
        return showCardNumber;
    }

    public void setShowCardNumber(int showCardNumber) {
        this.showCardNumber = showCardNumber;
        changeExtend();
    }


    @Override
    public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams,Object... objects) throws Exception {
        createTable(player, play, bureauCount, params);
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {

    }

    @Override
    public boolean isCreditTable(List<Integer> params){
        return params != null && params.size() > 13 && StringUtil.getIntValue(params, 13, 0) == 1;
    }

    public String getGameName(){
        return "郑州跑得快";
    }

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_zzpdk);

    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
        for (Integer integer : wanfaList) {
            TableManager.wanfaTableTypesPut(integer, cls);
        }
    }

    @Override
    public boolean allowRobotJoin() {
        return StringUtils.contains(ResourcesConfigsUtil.loadServerPropertyValue("robot_modes", ""), new StringBuilder().append("|").append(modeId).append("|").toString());
    }

    public void setTableStatus(int tableStatus) {
        this.tableStatus = tableStatus;
    }

    public int getTableStatus() {
        return tableStatus;
    }

    public int getDaNiaoFen() {
        return daNiaoFen;
    }

    public void setDaNiaoFen(int daNiaoFen) {
        this.daNiaoFen = daNiaoFen;
    }

    @Override
    public boolean isAllReady() {
        if(daNiaoFen==0&&piaoFenType>0){
            return isAllReady2();
        }else if(piaoFenType==0){
            return isAllReady1();
        }else {
            return super.isAllReady();
        }
    }

    public boolean isAllReady1() {
        if (super.isAllReady()) {
            if (playBureau != 1) {
                return true;
            }
            // 只有第一局需要推送打鸟消息
            if (daNiaoFen > 0) {
                setTableStatus(PdkConstants.TABLE_STATUS_DANIAO);
                boolean isAllDaNiao = true;
                if (this.isTest()) {
                    // 机器人默认处理
                    for (ZZPdkPlayer robotPlayer : seatMap.values()) {
                        if (robotPlayer.isRobot()) {
                            robotPlayer.setNiaoFen(0);
                        }
                    }
                }
                ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_pdk_daniao, 1);
                for (ZZPdkPlayer player : seatMap.values()) {
                    if (player.getNiaoFen() < 0) {
                        // 有人未打鸟
                        player.writeSocket(com.build());
                        isAllDaNiao = false;
                    }
                }
                if (!isAllDaNiao) {
                    broadMsgRoomPlayer(com.build());
                }
                return isAllDaNiao;
            } else {
                for (ZZPdkPlayer player : seatMap.values()) {
                    player.setNiaoFen(0);
                }
                return true;
            }
        }
        return false;
    }

    public void daNiao(ZZPdkPlayer player, int niaoFen) {
        if (this.getDaNiaoFen() > 0) {
            player.setNiaoFen(niaoFen);
            StringBuilder sb = new StringBuilder("zzpdk");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.isAutoPlay() ? 1 : 0);
            sb.append("|").append("daNiao");
            sb.append("|").append(niaoFen);
            LogUtil.msgLog.info(sb.toString());
            ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_pdk_daniao, 2, player.getSeat(), player.getNiaoFen());
            this.broadMsgToAll(com.build());
            this.checkDeal();
        }
    }


    public boolean isAllReady2() {
        if (getPlayerCount() < getMaxPlayerCount()) {
            return false;
        }
        for (Player player : getSeatMap().values()) {
            if(!player.isRobot()){
                if(piaoFenType>1){
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
        if (piaoFenType >=3 ) {
            boolean piaoFenOver = true;
            for (ZZPdkPlayer player : playerMap.values()) {
                if(!player.isAlreadyPiaoFen()){
                    piaoFenOver = false;
                    break;
                }
            }
            if(!piaoFenOver){
                if (isSendPiaoFenMsg==0 && finishFapai==0) {
                    LogUtil.msgLog.info("zzpdk|sendPiaoFen|" + getId() + "|" + getPlayBureau());
                    ComRes msg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_pdk_piaofen, piaoFenType).build();
                    for (ZZPdkPlayer player : playerMap.values()) {
                        if(!player.isAlreadyPiaoFen())
                            player.writeSocket(msg);
                    }
                    isSendPiaoFenMsg = 1;
                }
                return false;
            }
        }else if(piaoFenType<=2){
            for (ZZPdkPlayer player : playerMap.values()) {
                player.setAlreadyPiaoFen(true);
                player.setPiaoFen(piaoFenType);
                for (ZZPdkPlayer p : playerMap.values()) {
                    p.writeComMessage(WebSocketMsgType.res_code_pdk_broadcast_piaofen, (int)player.getUserId(),player.getPiaoFen());
                }
            }
        }
        return true;
    }

    public synchronized void piaoFen(ZZPdkPlayer player, int fen){
        if (piaoFenType<3||player.isAlreadyPiaoFen())
            return;
        if(fen<=8&&fen>=0)
            player.setPiaoFen(fen);
        player.setAlreadyPiaoFen(true);
        StringBuilder sb = new StringBuilder("zzpdk");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append("piaoFen").append("|").append(fen);
        LogUtil.msgLog.info(sb.toString());
        int confirmTime=0;
        for (Map.Entry<Integer, ZZPdkPlayer> entry : seatMap.entrySet()) {
            entry.getValue().writeComMessage(WebSocketMsgType.res_code_pdk_broadcast_piaofen, (int)player.getUserId(),player.getPiaoFen());
            if(entry.getValue().isAlreadyPiaoFen())
                confirmTime++;
        }
        if (confirmTime == max_player_count) {
            checkDeal(player.getUserId());
        }
    }

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


    /**
     * 检查是否:拆4炸了
     *
     * @param player
     * @param cards
     * @return true拆4炸了，false没有拆
     */
    public boolean checkIsChai4Zha(ZZPdkPlayer player, List<Integer> cards) {
        if (chai4Zha == 1) {
            // 牌局设置，可拆
            return false;
        }
        List<Integer> handPais = new ArrayList<>(player.getHandPais());
        Map<Integer, Integer> map = CardTool.loadCards(handPais);
        List<Integer> valListOf4Zha = new ArrayList<>();
        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
            if (kv.getValue() >= 4) {
                valListOf4Zha.add(kv.getKey());
            }else if(AAAZha==1 && kv.getKey() == 14 && kv.getValue() == 3){
                // 3条A算炸蛋
                valListOf4Zha.add(kv.getKey());
            }
        }
        if (valListOf4Zha.size() == 0) {
            return false;
        }
        boolean isChai4Zha = false;
        for (Integer card : cards) {
            int val = CardTool.loadCardValue(card);
            if (valListOf4Zha.contains(val)) {
                isChai4Zha = true;
            }
        }
        if (!isChai4Zha) {
            return false;
        }
        CardUtils.Result cardResult = CardUtils.calcCardValue(CardUtils.loadCards(cards), this,false);
        if (cardResult.getType() == Result.sidai) {
            // 4带的牌型，不算
            return false;
        }else if(cardResult.getType() == Result.zhadan){
            // 炸蛋的牌型，不算
            return false;
        }
        return true;
    }

    public boolean isNewRound() {
        return newRound;
    }

    public void setNewRound(boolean newRound) {
        this.newRound = newRound;
        changeExtend();
    }

    @Override
    public int getDissPlayerAgreeCount() {
        return getPlayerCount();
    }

    public int getAAAZha() {
        return AAAZha;
    }

    public void setAAAZha(int AAAZha) {
        this.AAAZha = AAAZha;
    }

    public int getCardNum() {
        return cardNum;
    }

    public void changeBoomNum() {
        this.boomNum++;
        changeExtend();
    }

    public String getTableMsg() {

        Map<String, Object> json = new HashMap<>();
        json.put("wanFa", "跑得快");
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

    /**
     * 报单检查
     *
     * @param player
     * @param cards
     * @return true:下家报单，这张单牌不是手牌最大，不能出，false:可以出单牌，
     */
    public boolean checkBaoDan(ZZPdkPlayer player, List<Integer> cards) {
        if(danType==1)
            return false;
        if (cards == null || cards.size() > 1) {
            return false;
        }
        int nextSeat = getNextSeat(player.getSeat());
        ZZPdkPlayer nextPlayer = seatMap.get(nextSeat);
        if (nextPlayer == null || nextPlayer.getHandPais().size() != 1) {
            return false;
        }
        int card = cards.get(0);
        for (Integer handPai : player.getHandPais()) {
            if (handPai % 100 > card % 100) {
                return true;
            }
        }
        return false;
    }
}
