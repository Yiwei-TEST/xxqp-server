package com.sy599.game.qipai.dtz.bean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

import com.sy599.game.activity.ActivityConstant;
import com.sy599.game.common.UserResourceType;
import com.sy599.game.db.bean.*;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.msg.serverPacket.ComMsg;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.GeneratedMessage;
import com.sy.mainland.util.CommonUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseGame;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.bean.CreateTableInfo;
import com.sy599.game.common.constant.FirstmythConstants;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.DataStatisticsDao;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.db.enums.SourceType;
import com.sy599.game.jjs.bean.MatchBean;
import com.sy599.game.jjs.util.JjsUtil;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayCardRes;
import com.sy599.game.msg.serverPacket.TableRes.ClosingInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.ClosingPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.dtz.constant.DtzConstant;
import com.sy599.game.qipai.dtz.constant.DtzzConstants;
import com.sy599.game.qipai.dtz.rule.CardTypeDtz;
import com.sy599.game.qipai.dtz.rule.ScoreType;
import com.sy599.game.qipai.dtz.tool.CardTool;
import com.sy599.game.qipai.dtz.tool.CardToolDtz;
import com.sy599.game.qipai.dtz.tool.CardTypeTool;
import com.sy599.game.qipai.dtz.tool.CardTypeToolDtz;
import com.sy599.game.qipai.dtz.tool.DtzSendLog;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.GameConfigUtil;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ObjectUtil;
import com.sy599.game.util.PayConfigUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class DtzTable extends BaseTable implements BaseGame {
    public static final String GAME_CODE = "dtz";
    private static final int JSON_TAG = 1;
    /**
     * 筒子喜的分值
     **/
    public final BombScore bombScore = new BombScore();
    /*** 四人扣8张牌    三人 三副铺9四副铺52 两人 三副铺66四副铺96 */
    private List<Integer> kou8CardList = new ArrayList<>();
    /*** 当前牌桌上一玩家出的牌 */
    private volatile List<Integer> nowDisCardIds = new ArrayList<>();
    /**
     * 当前牌桌上一玩家出的牌的牌型
     **/
    private Pair<CardTypeDtz, Integer> firstCardTypePair = Pair.with(CardTypeDtz.CARD_0, 0);

    /*** 玩家map */
    private Map<Long, DtzPlayer> playerMap = new ConcurrentHashMap<>();
    /*** 座位对应的玩家 */
    private Map<Integer, DtzPlayer> seatMap = new ConcurrentHashMap<>();
    /*** 最大玩家数量 */
    private int max_player_count = 4; //4

    private int isFirstRoundDisThree;//首局是否出黑挑三  !!  测试 改成是不是要王

    private List<Integer> cutCardList = new ArrayList<>();// 切掉的牌

    private int showCardNumber = 0; // 是否显示剩余牌数量

    private int disCardRandom;//2人3人是否随机出牌

    private int ifMustPlay;//是否有牌必打

    private int wangTongZi;//3副是否有王筒子

    private int isAutoPlay;//是否开启自动托管

    private String modeId = "0";//金币场

    private int isDaiPai;//是否可以带牌

    private int dtz_auto_timeout = 30000;//进入托管最大等待时间
    private int dtz_auto_timeout2 = 30000;//进入托管最大等待时间
    private int dtz_auto_play_time = 3000;
    private int dtz_auto_startnext = 10000;//自动下一局

    private volatile int discardCount = 0;//每局出牌轮数
    /**
     * 托管1：单局，2：全局
     */
    private int autoPlayGlob;

    public int getIsDaiPai() {
        return isDaiPai;
    }

    public void setIsDaiPai(int isDaiPai) {
        this.isDaiPai = isDaiPai;
        changeExtend();
    }

    public int getIsAutoPlay() {
        return isAutoPlay;
    }

    public void setIsAutoPlay(int isAutoPlay) {
        this.isAutoPlay = isAutoPlay;
        changeExtend();
    }

    public BombScore getBombScore() {
        return bombScore;
    }

    public int getWangTongZi() {
        return wangTongZi;
    }

    public void setWangTongZi(int wangTongZi) {
        this.wangTongZi = wangTongZi;
        changeExtend();
    }

    public int getIfMustPlay() {
        return ifMustPlay;
    }

    public void setIfMustPlay(int ifMustPlay) {
        this.ifMustPlay = ifMustPlay;
        changeExtend();
    }

    public int getDisCardRandom() {
        return disCardRandom;
    }

    public void setDisCardRandom(int disCardRandom) {
        this.disCardRandom = disCardRandom;
        changeExtend();
    }

    /**
     * 当前小局数
     **/
    public int dtzRound = 1;

    /**
     * 分组
     */
    private Map<Integer, List<DtzPlayer>> groupMap = new ConcurrentHashMap<>();

    public Map<Integer, Integer> getGroupScore() {
        return groupScore;
    }

    /**
     * 分组分数
     */
    private Map<Integer, Integer> groupScore = new ConcurrentHashMap<>();

    public boolean isNew = true;
    /**
     * 控制每局开始的第一个出牌的人 不能出不要，在第一次出牌后置为true，当值为false是不能出不要 ，当值为true时可以出不要
     **/
    public boolean masterFP = false;
    /**
     * 当一组玩家全都出完牌时，需要进行结算的玩家座位位置，默认为0
     */
    public int settSeat = 0;

    {
//        groupScore.put(1, 0);
//        groupScore.put(2, 0);
        totalBureau = 30;
    }

    /**
     * 这里是加载数据的地方
     */
    @Override
    protected void loadFromDB1(TableInf info) {
        if (!StringUtils.isBlank(info.getNowDisCardIds())) {
            this.nowDisCardIds = StringUtil.explodeToIntList(info.getNowDisCardIds());
            LogUtil.msg("load nowDisCardIds:" + nowDisCardIds + ",tableId:" + info.getTableId());
        }
    }


    /**
     * 获取扣8张内容
     */
    public List<Integer> getKou8CardList() {
        return kou8CardList;
    }


    public Map<Integer, List<DtzPlayer>> getGroupMap() {
        return groupMap;
    }

    public long getId() {
        return id;
    }

    public DtzPlayer getPlayer(long id) {
        return playerMap.get(id);
    }

    /**
     * 一局结束
     */
    public void calcOver() {
        DtzPlayer winPlayer = seatMap.get(lastWinTemp);
        if (winPlayer == null) {
            LogUtil.e("winPlayer is null-->tableId:" + id + ",lastWinTemp:" + lastWinTemp);
            return;
        }
        for (DtzPlayer player : seatMap.values()) {
            player.changeState(player_state.over);
        }


        boolean isOver = calhasWinGroup() > 0;

        if (autoPlayGlob > 0) {
            //是否解散
            boolean diss = false;
            if (autoPlayGlob == 1) {
                for (DtzPlayer seat : seatMap.values()) {
                    if (seat.getAutoPlay() == 1) {
                        diss = true;
                        break;
                    }
                }
            }
            if (diss) {
                autoPlayDiss = true;
                isOver = true;
            }
        }
        boolean isBreak = false;
        //组对应分数
        HashMap<Integer, Integer> addScore = groupScore();
        Map<Integer, Integer> seatFen = new HashMap<>();
        int winGroup = isBreak ? -1 : calhasWinGroup();
        for (DtzPlayer player : seatMap.values()) {
            int addPoint = 0, group = findGroupByPlayer(player);
            if (addScore != null && !addScore.isEmpty() && addScore.containsKey(group)) {
                if (isFourPlayer()) {
                    addPoint = addScore.get(group) / 2;
                } else {
                    addPoint = addScore.get(group);
                }
            }
            if (isBreak) {//提前解散
                int roundScore = CardTypeToolDtz.calcTongziScore(this, player);
                player.setRoundScore(player.getRoundScore() + roundScore);
            }
            int totalPoint = player.getDtzTotalPoint() + player.getPoint() + addPoint + player.getRoundScore();
            if (isFourPlayer()) {
                seatFen.put(group, seatFen.containsKey(group) ? seatFen.get(group) + totalPoint : totalPoint);
            } else {
                seatFen.put(player.getSeat(), totalPoint);
            }
        }
        int totalA = 0, totalB = 0, totalC = 0;
        if (isThreePlayer()) {
            if (this.jiangli > 0 && winGroup > 0) {
                if (seatFen.containsKey(winGroup)) {
                    seatFen.put(winGroup, seatFen.get(winGroup) + this.jiangli);
                } else {
                    int winSeat1 = winGroup / 10;
                    int winSeat2 = winGroup % 10;
                    if (winSeat1 < 10) {//只有两组牌面分相等
                        Map<Integer, Integer> tongziXiFen = this.getTongziXiFen();
                        int tongzi1 = tongziXiFen.containsKey(winSeat1) ? tongziXiFen.get(winSeat1) : 0;
                        int tongzi2 = tongziXiFen.containsKey(winSeat2) ? tongziXiFen.get(winSeat2) : 0;
                        if (tongzi1 > tongzi2) {
                            seatFen.put(winSeat1, seatFen.get(winSeat1) + this.jiangli);
                            winGroup = winSeat1;
                        } else if (tongzi1 < tongzi2) {
                            seatFen.put(winSeat2, seatFen.get(winSeat2) + this.jiangli);
                            winGroup = winSeat2;
                        } else {
                            switch (winGroup) {
                                case 12:
                                    seatFen.put(1, seatFen.get(1) + (this.jiangli / 2));
                                    seatFen.put(2, seatFen.get(2) + (this.jiangli / 2));
                                    break;
                                case 13:
                                    seatFen.put(1, seatFen.get(1) + (this.jiangli / 2));
                                    seatFen.put(3, seatFen.get(3) + (this.jiangli / 2));
                                    break;
                                case 23:
                                    seatFen.put(2, seatFen.get(2) + (this.jiangli / 2));
                                    seatFen.put(3, seatFen.get(3) + (this.jiangli / 2));
                                    break;
                            }
                        }
                    }
                }
            }
            totalA = seatFen.containsKey(1) ? seatFen.get(1) : 0;
            totalB = seatFen.containsKey(2) ? seatFen.get(2) : 0;
            totalC = seatFen.containsKey(3) ? seatFen.get(3) : 0;
            totalA = CardTool.calcScore(totalA, isGoldRoom());
            totalB = CardTool.calcScore(totalB, isGoldRoom());
            totalC = CardTool.calcScore(totalC, isGoldRoom());
        } else {
            if (this.jiangli > 0 && winGroup > 0) {
                if (seatFen.containsKey(winGroup)) {
                    seatFen.put(winGroup, seatFen.get(winGroup) + this.jiangli);
                } else {
                    int winSeat1 = winGroup / 10;
                    int winSeat2 = winGroup % 10;
                    Map<Integer, Integer> tongziXiFen = this.getTongziXiFen();
                    int tongzi1 = 0, tongzi2 = 0;
                    if (isFourPlayer()) {
                        tongzi1 = (tongziXiFen.containsKey(1) ? tongziXiFen.get(1) : 0) + (tongziXiFen.containsKey(3) ? tongziXiFen.get(3) : 0);
                        tongzi2 = (tongziXiFen.containsKey(2) ? tongziXiFen.get(2) : 0) + (tongziXiFen.containsKey(4) ? tongziXiFen.get(4) : 0);
                    } else {
                        tongzi1 = tongziXiFen.containsKey(winSeat1) ? tongziXiFen.get(winSeat1) : 0;
                        tongzi2 = tongziXiFen.containsKey(winSeat2) ? tongziXiFen.get(winSeat2) : 0;
                    }
                    if (tongzi1 > tongzi2) {
                        seatFen.put(winSeat1, seatFen.get(winSeat1) + this.jiangli);
                        winGroup = winSeat1;
                    } else if (tongzi1 < tongzi2) {
                        seatFen.put(winSeat2, seatFen.get(winSeat2) + this.jiangli);
                        winGroup = winSeat2;
                    }
                }
            }
            totalA = seatFen.containsKey(1) ? seatFen.get(1) : 0;
            totalB = seatFen.containsKey(2) ? seatFen.get(2) : 0;
            totalA = CardTool.calcScore(totalA, isGoldRoom());
            totalB = CardTool.calcScore(totalB, isGoldRoom());
        }
        for (DtzPlayer player : seatMap.values()) {
            int addPoint = 0;
            if (addScore.size() != 0) {
                if (isFourPlayer()) {
                    addPoint = addScore.get(findGroupByPlayer(player)) / 2;
                } else {
                    addPoint = addScore.get(findGroupByPlayer(player));
                }
            }
            int totalPoint = player.getDtzTotalPoint() + player.getPoint() + addPoint + player.getRoundScore();
            player.setDtzTotalPoint(totalPoint);
            player.setPoint(player.getPoint() + addPoint);
            int winLossPoint = 0;
            if (isThreePlayer()) {
                switch (player.getSeat()) {
                    case 1:
                        winLossPoint = totalA * 2 - totalB - totalC;
                        break;
                    case 2:
                        winLossPoint = totalB * 2 - totalA - totalC;
                        break;
                    case 3:
                        winLossPoint = totalC * 2 - totalA - totalB;
                        break;
                }
                player.calcResult(this, winLossPoint);
            } else if (isTwoPlayer()) {
                switch (player.getSeat()) {
                    case 1:
                        winLossPoint = totalA - totalB;
                        break;
                    case 2:
                        winLossPoint = totalB - totalA;
                        break;
                }
                player.calcResult(this, winLossPoint);
            } else if (isFourPlayer()) {
                if (groupMap.containsKey(winGroup) && groupMap.get(winGroup).contains(player)) {
                    player.calcResult(this, 1);
                } else {
                    player.calcResult(this, -1);
                }
                switch (findGroupByPlayer(player)) {
                    case 1:
                        winLossPoint = totalA - totalB;
                        break;
                    case 2:
                        winLossPoint = totalB - totalA;
                        break;
                }
            }
            player.setWinLossPoint(winLossPoint);
            player.setRoundScore(0);
        }

        // 金币场
        if (isGoldRoom()) {
            for (DtzPlayer player : seatMap.values()) {
                player.setWinGold(player.getWinLossPoint());
            }
            calcGoldRoom();
        }

        calcAfter();
        ClosingInfoRes.Builder res = sendAccountsMsg(isOver, winPlayer, false ,winGroup);
        HashMap<Integer, Integer> groupScoreMap = groupScore();
        addSerialScoreToGroup(groupScoreMap);

        for (DtzPlayer player : seatMap.values()) {
            logPoint(player, groupScoreMap);
        }

        saveLog(isOver, winPlayer.getUserId(), res.build());
        setLastWinSeat(winPlayer.getSeat());


        for (DtzPlayer player : seatMap.values()) {
            player.saveBaseInfo();
        }
    }

    @Override
    protected void calcGoldRoom() {
        if (!isGoldRoom()) {
            return;
        }
        long totalWin = 0;
        // 算最多赢多少分
        List<Player> winList = new ArrayList<>();
        for (Player player : getSeatMap().values()) {
            if (player.getWinGold() > 0) {
                winList.add(player);
                player.updateTwinRewardCount();
            }
        }
        
        int loseGoldCount = getSeatMap().size()-winList.size();
        long maxTotalWin = 0;
        long maxTotalLose = 0;
        for (Player player : getSeatMap().values()) {
            // 金币场倍率
            long winGold = player.getWinGold()* getGoldRoom().getRate();
            long havingGold = player.loadAllGolds();
            if (winGold > 0) {
            	if (havingGold*loseGoldCount <= winGold) {
            		winGold = havingGold*loseGoldCount;
//            		fengDing = true;
            		player.setGoldResult(2);// 封顶
            	}
            	maxTotalWin+=winGold;
            }else{
            	if (havingGold < -winGold) {
            		winGold = -havingGold;
            	}
            	maxTotalLose +=winGold;
            }
        }
        
        long loseFengDing = 0;
        //输的分比赢的多，那就少输,赢的封顶 , 如果输的分比赢的分少，下面会优先算输的总共多少分，再分给赢的人
        if(maxTotalWin<-maxTotalLose){
        	loseFengDing =maxTotalWin/loseGoldCount;
        }
        for (Player player : getSeatMap().values()) {
            // 金币场倍率
            player.setWinGold(player.getWinGold() * getGoldRoom().getRate());

            long winGold = player.getWinGold();
            if (winGold < 0) {
            	//实际输的最大，应该是赢最多的平均数
            	if(loseFengDing>0&&loseFengDing<=-winGold){
            		winGold =-loseFengDing;
            	}
                long havingGold = player.loadAllGolds();
                if (winGold + havingGold < 0) {
                    winGold = -havingGold;
                    player.setWinGold(winGold);
                    player.setGoldResult(1);//破产
                }else{
                	 player.setWinGold(winGold);
                }
                totalWin += -winGold;
                player.changeGold(winGold, playType, SourceType.table_win);
                player.calcGoldResult(winGold);
            } else {
                winList.add(player);
            }
        }
        Collections.sort(winList, new Comparator<Player>() {
            @Override
            public int compare(Player o1, Player o2) {
                return Long.valueOf(o2.getWinGold() - o1.getWinGold()).intValue();
            }
        });

        // 算赢家
        for (Player player : winList) {
            long winGold = player.getWinGold();
            if (winGold > 0 && totalWin > 0) {
                if (totalWin > winGold) {
                    totalWin -= winGold;
                } else {
                    winGold = totalWin;
                    totalWin = 0;
                }
                player.setWinGold(winGold);
                player.changeGold(winGold, playType, SourceType.table_win);
                player.calcGoldResult(winGold);
            }
        }
        if(isGoldRoom()){
            addGoldRoomBureau(getGoldRoom().getRate());
        }
    }

    /**
     * 是否已结算，默认为否
     **/
    public boolean jiesuan = false;


    private void checkSettlement() {
        if (hasWinGroup() != 0) { //局数判定
            StringBuilder sb = new StringBuilder();
            sb.append("table:" + this.id + " 达到总结算分数 即将解散 group1:" + groupScore.get(1) + " group2:" + groupScore.get(2));
            if (isThreePlayer()) {
                sb.append(" group3:" + groupScore.get(3));
            }
            LogUtil.msg(sb.toString());
            calcOver1();
            calcOver2();
            calcOver3();
            //大结算
            diss();
        } else {
            LogUtil.msg("table:" + this.id + " 进行小结算..");
            initNext();
            calcOver1(); //小结算
        }
    }

    /**
     * 保存战绩的 保存 每一次小结算
     */
    public void saveLog(boolean over, long winId, Object resObject) {
        ClosingInfoRes res = (ClosingInfoRes) resObject;
        LogUtil.d_msg("tableId:" + id + " play:" + playBureau + " over:" + res);
        String logRes = JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResLog(res));
        UserPlaylog userLog = new UserPlaylog();
        userLog.setLogId(playType);
        userLog.setUserId(creatorId);
        userLog.setTableId(id);
        userLog.setRes(extendLogDeal(logRes));
        userLog.setTime(new Date(TimeUtil.now().getTime()));
        userLog.setTotalCount(totalBureau);
        userLog.setCount(playBureau);
        userLog.setStartseat(lastWinSeat);
        userLog.setOutCards(playLog);
        userLog.setExtend(buildUserLogExtend());
        userLog.setType(creditMode == 1 ? 2 : 1);
        userLog.setGeneralExt(buildGeneralExtForPlaylog().toString());
        long logId = TableLogDao.getInstance().save(userLog);
        saveTableRecord(logId, over, playBureau);
        UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
        if (!isGoldRoom()) {
            for (DtzPlayer player : playerMap.values()) {
                player.addRecord(logId, playBureau);
            }
        }

    }

    private String buildUserLogExtend() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("roomid", id);
        jsonObject.put("jiangli", this.jiangli);
        jsonObject.put("aScore", (this.groupScore.containsKey(1)) ? groupScore.get(1) : 0);
        jsonObject.put("bScore", (this.groupScore.containsKey(2)) ? groupScore.get(2) : 0);
        if (isThreePlayer()) {
            jsonObject.put("cScore", (this.groupScore.containsKey(3)) ? groupScore.get(3) : 0);
        }
        jsonObject.put("cutCardList", kou8CardListToJSON());
        jsonObject.put("isGroupRoom", isGroupRoom() ? 1 : 0);
        return jsonObject.toString();
    }

    /**
     * 这里是 保存修改了的 数据的地方
     */
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
//                LogUtil.msg("save nowDisCardIds:" + nowDisCardIds + "," + id);
            }
            //nowDisCardSeat
            tempMap.put("nowDisCardSeat", nowDisCardSeat);
            tempMap.put("extend", buildExtend());
            //            TableDao.getInstance().save(tempMap);
        }
        return tempMap.size() > 0 ? tempMap : null;
    }

    public JsonWrapper buildExtend0(JsonWrapper wrapper) {
        wrapper.putInt(1, isFirstRoundDisThree);
        wrapper.putInt(2, max_player_count);
        wrapper.putInt(3, showCardNumber);
        /* 下面是 新增 的------------- */
        // public int lastWin = 0, lastWinTemp = 0;
        //private int fangfei = 20, score_max = 600, jiangli = 0;
        wrapper.putString("settSeat", String.valueOf(this.settSeat));
        wrapper.putString("jiesuan", Boolean.toString(this.jiesuan));
//        wrapper.putString("isAAConsume", Boolean.toString(this.isAAConsume));
        wrapper.putInt("fangfei", fangfei);
        wrapper.putInt("score_max", score_max);
        wrapper.putInt("jiangli", jiangli);
        wrapper.putInt("lastWin", lastWin);
        wrapper.putInt("lastWinTemp", lastWinTemp);
        wrapper.putInt("dtzRound", dtzRound);
        wrapper.putString("isNew", Boolean.toString(isNew));
        wrapper.putInt("score", score);
        wrapper.putInt("tz_score", tz_score);
        wrapper.putInt("serialIndex", serialIndex);
        wrapper.putInt("default_Sore_Dtz", default_Sore_Dtz);
        wrapper.putString("masterFP", Boolean.toString(masterFP));
        wrapper.putString("useNowDis", Boolean.toString(useNowDis));
        wrapper.putLong("scorePlayerTemp", scorePlayerTemp == null ? 0 : scorePlayerTemp.getUserId());
        wrapper.putLong("groupPlayer", groupPlayer == null ? 0 : groupPlayer.getUserId());
        wrapper.putString("groupMap", groupMapToJSON());
        wrapper.putString("groupScore", groupScoreToJSON());
        wrapper.putString("playTimes", playTimesToJSON());
        wrapper.putString("queue", queueToJSON());
        wrapper.putString("groupSerial", groupSerialToJSON());
        wrapper.putString("removeTemp", removeTempToJSON());
        wrapper.putString("scoreList", scoreListToJSON(scoreList));
        wrapper.putString("cardTemp", cardTempToJSON());
        wrapper.putString("bureauTemp", bureauTempToJSON());
        wrapper.putString("roundCardScore", roundCardScoreToJSON());
        wrapper.putString("bureau", bureauToJSON());
        wrapper.putString("firstCardTypePair", firstCardTypePairToJSON());
        wrapper.putInt("kouType", kouType);
        wrapper.putInt("out67Type", out67Type);
        wrapper.putString("kou8CardList", kou8CardListToJSON());
        wrapper.putInt("disCardRandom", disCardRandom);
        wrapper.putString("tempScoreList", scoreListToJSON(tempScoreList));
        wrapper.putInt("ifMustPlay", ifMustPlay);
        wrapper.putInt("wangTongZi", wangTongZi);
        wrapper.putInt("isAutoPlay", isAutoPlay);
        wrapper.putString("gModeId", modeId);
        wrapper.putInt("isDaiPai", isDaiPai);

        wrapper.putInt("autoPlayGlob", autoPlayGlob);


        return wrapper;
    }

    private DtzPlayer toPlayer(String userid) {
        if (userid == null || userid.isEmpty()) return null;
        return playerMap.get(Long.parseLong(userid));
    }

    private String groupMapToJSON() {
        JSONObject groupMap = new JSONObject();
        for (Map.Entry<Integer, List<DtzPlayer>> entry : this.groupMap.entrySet()) {
            JSONArray jsonArray = new JSONArray();
            for (DtzPlayer player : entry.getValue()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userId", player.getUserId());
                jsonObject.put("seat", player.getSeat());
                jsonArray.add(jsonObject);
            }
            groupMap.put(entry.getKey().toString(), jsonArray);
        }
        return groupMap.toString();
    }

    private String groupScoreToJSON() {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<Integer, Integer> entry : groupScore.entrySet()) {
            jsonObject.put(entry.getKey().toString(), entry.getValue());
        }
        return jsonObject.toString();
    }

    private String playTimesToJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("value0", playTimes.getValue0());
        jsonObject.put("value1", (playTimes.getValue1() == null) ? "" : playTimes.getValue1().getUserId());
        return jsonObject.toString();
    }

    private String firstCardTypePairToJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("value0", firstCardTypePair.getValue0().getType());
        jsonObject.put("value1", firstCardTypePair.getValue1());
        return jsonObject.toString();
    }

    private String queueToJSON() {
        JSONArray jsonArray = new JSONArray();
        List<DtzPlayer> list = new ArrayList<>(queue);
        for (DtzPlayer player : list) {
            jsonArray.add(player.getUserId());
        }
        return jsonArray.toString();
    }

    private String groupSerialToJSON() {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<Integer, ArrayList<Pair<Integer, DtzPlayer>>> entry : groupSerial.entrySet()) {
            JSONArray jsonArray = new JSONArray();
            for (Pair<Integer, DtzPlayer> pairEntry : entry.getValue()) {
                JSONObject arrayObj = new JSONObject();
                arrayObj.put("value0", pairEntry.getValue0());
                arrayObj.put("value1", (pairEntry.getValue1() == null) ? "" : pairEntry.getValue1().getUserId());
                jsonArray.add(arrayObj);
            }
            jsonObject.put(entry.getKey().toString(), jsonArray);
        }
        return jsonObject.toString();
    }

    private String removeTempToJSON() {
        JSONArray jsonArray = new JSONArray();
        for (DtzPlayer player : removeTemp) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId", player.getUserId());
            jsonArray.add(jsonObject);
        }
        return jsonArray.toString();
    }

    private String scoreListToJSON(HashMap<DtzPlayer, HashMap<ScoreType, Integer>> map) {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<DtzPlayer, HashMap<ScoreType, Integer>> entry : map.entrySet()) {
            JSONObject jsonObject2 = new JSONObject();
            for (Map.Entry<ScoreType, Integer> scoreEntry : entry.getValue().entrySet()) {
                jsonObject2.put(scoreEntry.getKey().name(), scoreEntry.getValue());
            }
            jsonObject.put("" + entry.getKey().getUserId(), jsonObject2);
        }
        return jsonObject.toString();
    }

    private String cardTempToJSON() {
        JSONArray jsonArray = new JSONArray();
        for (List<Integer> list : this.cardTemp) {
            JSONArray jsonArray_list = new JSONArray();
            for (int card : list) {
                jsonArray_list.add(card);
            }
            jsonArray.add(jsonArray_list);
        }
        return jsonArray.toString();
    }


    private String kou8CardListToJSON() {
        JSONArray jsonArray = new JSONArray();
        for (int card : kou8CardList) {
            jsonArray.add(card);
        }
        return jsonArray.toString();
    }


    private String bureauTempToJSON() {
        JSONArray jsonArray = new JSONArray();
        for (List<Integer> list : this.bureauTemp) {
            JSONArray jsonArray_list = new JSONArray();
            for (int card : list) {
                jsonArray_list.add(card);
            }
            jsonArray.add(jsonArray_list);
        }
        return jsonArray.toString();
    }

    private String roundCardScoreToJSON() {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<DtzPlayer, HashMap<ScoreType, Integer>> entry : roundCardScore.entrySet()) {
            JSONObject jsonObject2 = new JSONObject();
            for (Map.Entry<ScoreType, Integer> scoreEntry : entry.getValue().entrySet()) {
                jsonObject2.put(scoreEntry.getKey().name(), scoreEntry.getValue());
            }
            jsonObject.put("" + entry.getKey().getUserId(), jsonObject2);
        }
        return jsonObject.toString();
    }

    private String bureauToJSON() {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<DtzPlayer, HashMap<ScoreType, Integer>> entry : bureau.entrySet()) {
            JSONObject jsonObject2 = new JSONObject();
            for (Map.Entry<ScoreType, Integer> scoreEntry : entry.getValue().entrySet()) {
                jsonObject2.put(scoreEntry.getKey().name(), scoreEntry.getValue());
            }
            jsonObject.put("" + entry.getKey().getUserId(), jsonObject2);
        }
        return jsonObject.toString();
    }

    private String cardMarkerToJSON() {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<Integer, DtzPlayer> entry : seatMap.entrySet()) {
            jsonObject.put("" + entry.getKey(), entry.getValue().getOutPais());
        }
        return jsonObject.toString();
    }

    /**
     * 从这里 把多的字段 转换 成string
     */
    protected String buildPlayersInfo() {
        StringBuilder sb = new StringBuilder();
        for (DtzPlayer pdkPlayer : playerMap.values()) {
            sb.append(pdkPlayer.toInfoStr()).append(pdkPlayer.toXipaiDatatoInfoStr()).append(";");
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
        synchronized (this) {
            changeTableState(table_state.play);

            discardCount = 0;

            //如果有设置牌型，直接按设置牌型处理
            List<List<Integer>> list;
            if (GameServerConfig.isDebug() && zp != null) {
                //得到牌张数和玩法所有牌
                int count;
                List<Integer> copyAll;
                switch (getPlayType()) {
                    case DtzzConstants.play_type_3POK:
                        count = 33;
                        copyAll = new ArrayList<>(DtzzConstants.cardList_Dtz);
                        break;
                    case DtzzConstants.play_type_4POK:
                        count = 46;
                        copyAll = new ArrayList<>(DtzzConstants.cardList_Dtz_4);
                        break;
                    case DtzzConstants.play_type_3PERSON_3POK:
                        count = 41;
                        if (getWangTongZi() == 1) {
                            copyAll = new ArrayList<>(DtzzConstants.cardList_wang_Dtz);
                        } else {
                            copyAll = new ArrayList<>(DtzzConstants.cardList_Dtz);
                        }
                        break;
                    case DtzzConstants.play_type_3PERSON_4POK:
                        count = 44;
                        copyAll = new ArrayList<>(DtzzConstants.cardList_Dtz_4);
                        break;
                    case DtzzConstants.play_type_2PERSON_3POK:
                        count = 33;
                        if (getWangTongZi() == 1) {
                            copyAll = new ArrayList<>(DtzzConstants.cardList_wang_Dtz);
                        } else {
                            copyAll = new ArrayList<>(DtzzConstants.cardList_Dtz);
                        }
                        break;
                    case DtzzConstants.play_type_2PERSON_4POK:
                        count = 44;
                        copyAll = new ArrayList<>(DtzzConstants.cardList_Dtz_4);
                        break;
                    case DtzzConstants.play_type_2PERSON_4Xi:
                    case DtzzConstants.play_type_3PERSON_4Xi:
                    case DtzzConstants.play_type_4PERSON_4Xi:
                        count = 44;
                        copyAll = new ArrayList<>(DtzzConstants.cardList_Dtz_4_buDaiWang);
                        break;
                    default:
                        return;
                }
                //乱序
                List<Integer> pdAll = new ArrayList<>(copyAll);
                Collections.shuffle(copyAll);
                //得到输入的牌
                List<List<Integer>> zplist = new ArrayList<>(zp);
                List<Integer> allZp = new ArrayList<>();
                for (List<Integer> a : zplist) {
                    allZp.addAll(a);
                }
                //先去掉67
                if (out67Type == 1) {
                    for (int i = 0; i < copyAll.size(); i++) {
                        if ((copyAll.get(i) % 100) == 6 || (copyAll.get(i) % 100) == 7) {
                            copyAll.remove(i);
                            i--;
                        }
                    }
                }

                //检查输入牌是否符合规则
                if (!CardTypeToolDtz.cheakOutcard(copyAll, allZp)) {
                    //不满足条件还是自由发牌
                    list = CardTool.fapaiDtz(this, getPlayerCount(), playType, isFirstRoundDisThree, zp);
                } else {
                    //去掉已有的牌
                    for (int x = 1; x <= getPlayerCount(); x++) {
                        if (zplist.size() < x) {
                            //不够，补上
                            zplist.add(new ArrayList<Integer>());
                        }
                        List<Integer> ren = zplist.get(x - 1);
                        for (int y = 1; y <= count; y++) {
                            if (ren.size() >= y && ren.get(y - 1) != null) {
                                //判断有这张牌，从所有牌里面删除
                                copyAll.remove(ren.get(y - 1));
                            }
                        }
                    }

                    copyAll = DtzzConstants.getPokCardList(this, copyAll, kouType, out67Type, getPlayType());
                    //补牌
                    for (int x = 1; x <= getPlayerCount(); x++) {
                        List<Integer> ren = zplist.get(x - 1);
                        for (int y = 1; y <= count; y++) {
                            if (!(ren.size() >= y && ren.get(y - 1) != null)) {
                                //开始补牌
                                ren.add(copyAll.get(0));
                                copyAll.remove(0);
                            }
                        }
                    }
                    //得到最终数据
                    List<Integer> pdZp = new ArrayList<>();
                    for (List<Integer> a : zplist) {
                        pdZp.addAll(a);
                    }
                    //最后验证
                    if (CardTypeToolDtz.cheakOutcard(pdAll, pdZp) && (kouType == 1 || CardTypeToolDtz.cheakOutcard(pdZp, pdAll))) {
                        list = zplist;
                        //限定玩家位置为对应的牌
                        int i = 0;
                        for (int s = 1; s <= getPlayerCount(); s++) {
                            DtzPlayer player = seatMap.get(Integer.valueOf(s));
                            player.changeState(player_state.play);
                            List<Integer> lls = list.get(i);
                            LogUtil.msg(" 牌的数量是:" + lls.size());
                            player.dealHandPais(lls);
                            player.setIsNoLet(0);
                            i++;
                        }
//                    if (isTwoPlayer()) {
//                        this.cutCardList.clear();
//                        this.cutCardList.addAll(list.get(i));
//                    }
                        return;
                    } else {
                        list = CardTool.fapaiDtz(this, getPlayerCount(), playType, isFirstRoundDisThree, zp);
                    }

                }
            } else {
                list = CardTool.fapaiDtz(this, getPlayerCount(), playType, isFirstRoundDisThree, zp);
                if (CardTypeToolDtz.isRedeal(this, list)) {
                    list = CardTool.fapaiDtz(this, getPlayerCount(), playType, isFirstRoundDisThree, zp);
                }
            }
            int i = 0;
            for (DtzPlayer player : playerMap.values()) {
                player.changeState(player_state.play);
                List<Integer> lls = list.get(i);
                player.dealHandPais(lls);
                player.setIsNoLet(0);
                i++;
            }
        }

    }

    /**
     * 下一次出牌的seat
     */
    public int getNextDisCardSeat() {
        if (lastWin != 0) {
            return lastWin;
        }
        if (useNowDis) return nowDisCardSeat;
        return nowDisCardSeat;
    }

    /**
     * 获取到打筒子出牌位置
     */
    private int getNextDisCardSeatByDtz() {
        if (state != table_state.play) {
            return 0;
        }
        if (disCardRound == 0) { //一开始打牌的时候
            if (lastWinSeat == 0) {  //加入上一把胜利者的位置是空
                return 1; //默认第一个
            } else {
                return lastWinSeat;
            }
        } else {
            return nowDisCardSeat > max_player_count ? 1 : nowDisCardSeat + 1;
        }
    }

    public DtzPlayer getPlayerBySeat(int seat) {
        int next = seat >= max_player_count ? 1 : seat + 1;
        return seatMap.get(next);

    }

    @SuppressWarnings("unchecked")
    public Map<Integer, DtzPlayer> getSeatMap() {
        return seatMap;
    }

    public CreateTableRes buildCreateTableRes(long userId, boolean isrecover) {
        CreateTableRes.Builder res = CreateTableRes.newBuilder();
        buildCreateTableRes0(res);
        synchronized (this) {
            res.setNowBurCount(getPlayBureau());
            res.setTotalBurCount(getTotalBureau());
            res.setGotyeRoomId(gotyeRoomId + "");
            res.setTableId(getId() + "");
            res.setWanfa(playType);
            res.addExt(this.showCardNumber);//Ext1
            Map<Integer, Integer> scoreJu = getScoreJu();
            if (dtzRound != 1) {
                //第1组的分数
                res.addExt((groupScore.containsKey(1)) ? groupScore.get(1) - scoreJu.get(1) : 0);//Ext2
                //第2组的分数
                res.addExt((groupScore.containsKey(2)) ? groupScore.get(2) - scoreJu.get(2) : 0);//Ext3
                if (isThreePlayer()) {
                    res.addExt((groupScore.containsKey(3)) ? groupScore.get(3) - scoreJu.get(3) : 0);
                }
            } else {
                //第1组的分数
                res.addExt(0);//Ext2
                //第2组的分数
                res.addExt(0);//Ext3
                if (isThreePlayer()) {
                    res.addExt(0);
                }
            }
            //设置一下dtz玩法的牌面分数
            res.addExt(this.score);//Ext4
            //设置牌面5,10,k的总数
            Triplet<Integer, Integer, Integer> triplet = getFragmentCard();
            //分别是5、10、K
            res.addExt(triplet.getValue0());//Ext5
            res.addExt(triplet.getValue1());//Ext6
            res.addExt(triplet.getValue2());//Ext7
            //设置全局筒子分
            List<Integer> groupCount = getAllTzOrXiGroupScore();
            //分别是A组，B组
            for (Integer cout : groupCount) {
                res.addExt(cout);
            }
//        res.addExt(groupScore1.getValue0()); //Ext7
//        res.addExt(groupScore1.getValue1());//Ext8
            //设置是否付费付费情况
            int ratio;
            int pay;
            MatchBean matchBean = isMatchRoom() ? JjsUtil.loadMatch(matchId) : null;
            if (matchBean != null) {
                ratio = (int) matchRatio;
                pay = 0;
            } else if (isGoldRoom()) {
                ratio = 0;
                pay = 0;
            } else {
                ratio = 1;
                pay = consumeCards() ? loadPayConfig(payType) : 0;
            }

            res.addExt(pay);

            //设置房间结算分数标准
            res.addExt(this.score_max); //Ext10
            //设置房间终局奖励分
            res.addExt(jiangli);//Ext11
            //是否扣8张牌，默认0表示不扣，1为扣
            res.addExt(kouType);//Ext12
            //是否去掉6,7牌，默认0表示不去掉，1为去掉
            res.addExt(out67Type);//Ext13
            res.addExt(payType);//Ext14
            res.addExt(ifMustPlay);//Ext15
            res.addExt(wangTongZi);
            res.addExt(isAutoPlay);
            res.addExt(disCardRandom);

            res.addExt(CommonUtil.isPureNumber(modeId) ? Integer.parseInt(modeId) : 0);
            res.addExt(ratio);
            res.addExt(dtz_auto_timeout);
            res.addExt(dtz_auto_play_time);
            res.addExt(dtz_auto_startnext);
            long time = (dtz_auto_timeout + (matchBean != null && discardCount == 0 ? 5000 : 0) + lastActionTime - TimeUtil.currentTimeMillis()) / 1000;
            res.addExt(Integer.parseInt(String.valueOf(time)));
            res.addExt(isDaiPai);//3 27

            res.addExt(lastWinSeat);//28
            if (matchBean != null) {
                int num = JjsUtil.loadMatchCurrentGameNo(matchBean);
                res.addExt(num);//29
                res.addExt(num == 0 ? JjsUtil.loadMinScore(matchBean, getMatchRatio()) : 0);//30
            } else {
                res.addExt(0);
                res.addExt(0);
            }
            res.addExt(creditMode); //31
//            res.addExt(creditJoinLimit);//32
//            res.addExt(creditDissLimit);//33
//            res.addExt(creditDifen);//34
//            res.addExt(creditCommission);//35
            res.addExt(0);
            res.addExt(0);
            res.addExt(0);
            res.addExt(0);
            res.addExt(creditCommissionMode1);//36
            res.addExt(creditCommissionMode2);//37
            res.addExtStr(String.valueOf(matchId));//0
            res.addTimeOut(dtz_auto_timeout);
            if (matchBean != null) {
                if (discardCount == 0) {
                    res.addTimeOut((dtz_auto_timeout + 5000));
                } else {
                    res.addTimeOut(dtz_auto_timeout);
                }
            } else {
                res.addTimeOut(0);
            }
            List<PlayerInTableRes> players = new ArrayList<>();
            for (DtzPlayer player : playerMap.values()) {
                PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(this, userId, isrecover);
                if (playerRes == null) {
                    continue;
                }
                if (player.getUserId() == userId) {
                    // 如果是自己重连能看到手牌
                    playerRes.addAllHandCardIds(player.getHandPais());
                } else {
                    // 如果是别人重连，轮到出牌人出牌时要不起可以去掉
                }
//                LogUtil.msg(" 这个玩家的这局的分数 : " + player.getPoint() + " group : " + findGroupByPlayer(player) + " 名次是 : " + findPlayerMingCi(player) + " 座位是 : " + player.getSeat() + " 这个玩家的总积分 :" + player.getDtzTotalPoint());
//            playerRes.setPoint(player.getPoint());

                if (player.getSeat() == disCardSeat && nowDisCardIds != null) {
                    List<Integer> cardss = getNowDisCardIds();
                    if (cardss != null && cardss.size() != 0) {
                        cardss = com.sy599.game.qipai.dtz.tool.CardTypeToolDtz.sortPokers(cardss, this);
                        playerRes.addAllOutCardIds(cardss);
                    } else {
                        playerRes.addAllOutCardIds(nowDisCardIds);
                    }
                }
                if (isGroupRoom()) {
                    GroupUser gu = player.getGroupUser();
                    String groupId = loadGroupId();
                    if (gu == null || !groupId.equals(gu.getGroupId() + "")) {
                        gu = GroupDao.getInstance().loadGroupUser(player.getUserId(), groupId);
                    }
                    playerRes.setCredit(gu != null ? gu.getCredit() : 0);
                }
                players.add(playerRes.build());
            }
            res.addAllPlayers(players);
            int nextSeat = getNextDisCardSeat(); //重连时的玩家位
            if (nextSeat == 0) {
                nextSeat = getMasterSeat();
            }
            res.setNextSeat(nextSeat);
            res.setRenshu(this.max_player_count);
        }
        return buildCreateTableRes1(res);
    }


    public int getOnTablePlayerNum() {
        int num = 0;
        for (DtzPlayer player : seatMap.values()) {
            if (player.getIsLeave() == 0) {
                num++;
            }
        }
        return num;
    }

    /**
     * 得到每组  一局 分数
     */
    private HashMap<Integer, Integer> getScoreJu() {
        HashMap<Integer, Integer> map = new HashMap<>();
        for (Map.Entry<Integer, List<DtzPlayer>> entry : this.groupMap.entrySet()) {
            int score = 0;
            for (DtzPlayer player : entry.getValue()) {
                score += player.getPoint();
            }
            map.put(entry.getKey(), score);
        }
        return map;
    }


    /**
     * 要不起逻辑处理
     */
    public void notLet(DtzPlayer player) {
        //设置牌桌当前要出牌的座位
        setNowDisCardSeat(player.getSeat());
        //设置玩家自己当前要不起
        player.setIsNoLet(1);
        //设置出牌为空
        List<Integer> cards = new ArrayList<>();
        cards.add(0);
        player.addOutPais(cards);
        //组织返回客户端消息
        PlayCardRes.Builder res = PlayCardRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setCardType(0);
        res.setIsPlay(1);
        res.setCurScore(score);
        if (player.getHandPais().size() == 1) {
            // 报单
            res.setIsBt(1);
        }
        this.useNowDis = true;
        Pair<Boolean, DtzPlayer> pair = isDoneRound1();
        if (pair.getValue0()) {
            //DtzSendLog.sendCardLog(id, player.getUserId(), player.getName(), "玩家不要===异常===其他三家都不要，最后出牌人是自己的notLet", playTimes.getValue0() + "：" + playTimes.getValue1() == null ? "" : playTimes.getValue1().getName() + "[" + playTimes.getValue1().getName() + "]");
            //设置同组的另外一玩家为下一个出牌人
            if (groupPlayer != null && groupPlayer.equals(pair.getValue1())) {
                res.setNextSeat(groupPlayer.getSeat());
                nowDisCardSeat = groupPlayer.getSeat();
                groupPlayer = null;
            } else {
                nowDisCardSeat = next(player, true);
                res.setNextSeat(nowDisCardSeat);
            }
        } else {
            //设置下一个要出牌的人，并计入
            res.setNextSeat(nowDisCardSeat = next(player, true));
        }

        res.setIsFirstOut(isDoneRound1().getValue0() ? 1 : 0);
        addPlayLog(player.getSeat() + "_");
//        LogUtil.msg("房间号="+getId()+",玩家="+player.getUserId()+"["+player.getName()+"]座位="+player.getSeat()+"-不要-下一个出牌位置="+res.getNextSeat());
        logNotLet(player);
        DtzPlayer canPlayPlayer = null;
        for (DtzPlayer pdkPlayer : seatMap.values()) {
            PlayCardRes.Builder copy = res.clone();
//            List<Integer> canPlayList = CardTypeTool.canPlay(pdkPlayer.getHandPais(), nowDisCardIds);
//            if (disCardSeat == pdkPlayer.getSeat() || !canPlayList.isEmpty()) {
//                copy.setIsLet(1);
//
//            }
            if ((isThreePlayer() || getIfMustPlay() == 1) && pdkPlayer.getSeat() == this.nowDisCardSeat && pdkPlayer.getSeat() != this.disCardSeat && !this.isDoneRound1().getValue0()) { //三人玩法添加牌必压
                int canPlay = CardTypeToolDtz.isCanPlay(pdkPlayer.getHandPais(), this.nowDisCardIds, this);
                copy.setIsLet(canPlay);
                if (canPlay == 0) {
                    canPlayPlayer = pdkPlayer;
                }
            }
            pdkPlayer.writeSocket(copy.build());
        }
        if (canPlayPlayer != null) {
            this.giveup(canPlayPlayer, 10);
        }
    }

    /**
     * 发送不要的消息到客户端各玩家
     */
    public void sendNotletInfo(DtzPlayer player) {
        PlayCardRes.Builder res = PlayCardRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setCardType(0);
        res.setIsPlay(1);
        res.setCurScore(score);
        res.setIsLet(1);
        broadMsg(res.build());
    }

    /**
     * 出牌
     */
    public void disCards(DtzPlayer player, List<Integer> cards, CardTypeDtz cardType) {
        if (disCardSeat == player.getSeat()) {
            clearIsNotLet();
        } else {
            player.setIsNoLet(0);
        }
        //设置当前出牌人的位置
        setDisCardSeat(player.getSeat());
        //增加出牌到出牌列表，删除对应玩家手牌中出的牌
        player.addOutPais(cards);
        //设置当前上家出牌内容
        setNowDisCardIds(cards);
        //CardTypeDtz cardType = CardTypeToolDtz.toPokerType(cards, this);
        // 构建出牌消息
        PlayCardRes.Builder res = PlayCardRes.newBuilder();

        List<Integer> cardss = getNowDisCardIds();

        if (nowDisCardIds != null && nowDisCardIds.size() != 0) {
            cardss = CardTypeToolDtz.sortPokers(cardss, this);
//        	LogUtil.msg("这副牌排序之前:" + nowDisCardIds + "  这副牌排序之后:" + cardss);
        }
        res.addAllCardIds(cardss);
        res.setCardType(cardType.getType());
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setIsPlay(1);
        res.setIsFirstOut(0);
//        LogUtil.msg("table:" + id + " 玩家:" + (isDoneRound1().getValue1() == null ? "" : isDoneRound1().getValue1().getName()) + " 是不是该显示要不起:" + ((res.getIsFirstOut() == 1) ? "不需要" : "需要"));
//        if (player.getHandPais().size() == 1) {
//            res.setIsBt(1);
//        }
        res.setIsBt(player.getHandPais().size());
        Pair<Integer, Integer> score;
        if (player.getIsNoLet() == 0) { //要的起啦
            if (scorePlayerTemp != null) scorePlayerTemp = null;
            changePlayTimes(true, player); //现在他最大啦
            score = CardTypeToolDtz.toScore(cards, this); //对他出的牌估值
            addScore(score.getValue0());  //这是加普通分数
            addTzScore(score.getValue1()); //这是加筒子分数
            offerdCard(cards, player);
//            LogUtil.msg(player + " : " + score.getValue1());
        }
        res.setCurScore(this.score);
        addPlayLog(player.getSeat() + "_" + StringUtil.implode(cards));
        boolean isOver = player.getHandPais().size() == 0;
        if (isOver) { //我已经把牌打完了
            this.scorePlayerTemp = player; //别忘了给自己加分
            addFinishedPlaying(player); //把自己添加到队列
            for (DtzPlayer pdkPlayer : this.playerMap.values()) {
                pdkPlayer.writeComMessage(WebSocketMsgType.RES_MINGCI, (int) player.getUserId(), serialIndex);
            }

            int seat = getSeatInGroup(player);
            DtzPlayer next = seatMap.get(seat); //先把这个玩家存起来
            this.groupPlayer = next; //把这个玩家存起来
            changePlayTimes(true, next); //现在他最大啦
            int cc = next(player, true);
            this.nowDisCardSeat = cc;
            res.setNextSeat(cc);
        } else {
            int seat = next(player, true); //默认取下一个玩家
            DtzPlayer next = seatMap.get(seat); //先把这个玩家存起来
            this.nowDisCardSeat = next.getSeat();
            res.setNextSeat(seat);
        }
        logChuPai(player, cards, cardType);
//        LogUtil.msg("房间号="+getId()+",玩家="+player.getUserId()+"["+player.getName()+"]座位="+player.getSeat()+"-discard-" + JacksonUtil.writeValueAsString(cards) + ":" + cardType.name()+"下一个出牌位="+res.getNextSeat());
        DtzPlayer canPlayPlaye = null;
        for (DtzPlayer pdkPlayer : seatMap.values()) {
            PlayCardRes.Builder copy = res.clone();
            if ((isThreePlayer() || getIfMustPlay() == 1) && pdkPlayer.getSeat() == this.nowDisCardSeat) { //三人玩法添加牌必压
                int canPlay = CardTypeToolDtz.isCanPlay(pdkPlayer.getHandPais(), cards, this);
                copy.setIsLet(canPlay);
                if (canPlay == 0) {
                    canPlayPlaye = pdkPlayer;
                }
            }

            pdkPlayer.writeSocket(copy.build());
//            LogUtil.msg(copy.toString());
        }
        if (checkFinished()) { // 是不是结束了
//            LogUtil.msg("table:" + id + " 这局 完毕了!!!   ---->groupSerial的大小是: " + this.groupSerial.size());
            changeTableState(table_state.over); // 改变一下桌子状态
        }
        if (canPlayPlaye != null) {
            this.giveup(canPlayPlaye, 10);
        }
    }

    /**
     * 清理要不起的状态
     */
    public void clearIsNotLet() {
        for (DtzPlayer player : seatMap.values()) {
            player.setIsNoLet(0);
        }
    }

    /**
     * value1：当某位置出牌后（此时为0），后续出不要的人数（为3时，表示都不要），value2是最后出牌的玩家对象
     **/
    private Pair<Integer, DtzPlayer> playTimes = Pair.with(0, null);

    /**
     * 修改当前存储玩家要不起玩家对象
     *
     * @param islet  是否要的起
     * @param player 对应的玩家
     */
    public void changePlayTimes(boolean islet, DtzPlayer player) {
        if (islet) {
            //要的起
            playTimes = playTimes.setAt0(0);
            playTimes = playTimes.setAt1(player);
        } else {
            //要不起
            playTimes = playTimes.setAt0(playTimes.getValue0() + 1);
        }
    }


    /**
     * 判断是否此轮其他人都不要
     *
     * @return Pair ture 都不要  false 有人要
     */
    public Pair<Boolean, DtzPlayer> isDoneRound1() {
        if (playTimes.getValue0() >= (seatMap.size() - 1)) {
            return Pair.with(true, playTimes.getValue1());
        }
        return Pair.with(false, null);
    }


    public void clearPlayTimes() {
        this.playTimes = Pair.with(0, null);
    }

    /**
     * 打筒子当前轮的牌面分数
     */
    private int score = 0;
    /**
     * 打筒子当前轮的地炸筒子分
     **/
    private int tz_score = 0;

    /**
     * 增加一轮的分数
     */
    public void addScore(int score) {
        if (score != 0) {
            this.score += score;
            changeExtend();
        }
    }

    /**
     * 加筒子分数
     */
    public void addTzScore(int tz_score) {
        if (tz_score != 0) {
            switch (this.playType) {
                case DtzzConstants.play_type_3POK:
                case DtzzConstants.play_type_3PERSON_3POK:
                case DtzzConstants.play_type_2PERSON_3POK:
                    this.tz_score = tz_score;
                    break;
                case DtzzConstants.play_type_2PERSON_4Xi:
                case DtzzConstants.play_type_3PERSON_4Xi:
                case DtzzConstants.play_type_4PERSON_4Xi:
                    this.tz_score = tz_score;
                    break;
                default:
                    this.tz_score += tz_score;
                    break;
            }
            changeExtend();
        }
    }


    public int getTzScore() {
        return this.tz_score;
    }

    /**
     * 返回该分数
     */
    public int getScore() {
        return this.score;
    }

    /**
     * 重置一轮的分数
     */
    public void clearRoundScore() {
        this.score = 0;
        this.tz_score = 0;
    }

    public void addScoreBuGroup(int group, int score) {
        this.groupScore.put(group, (this.groupScore.containsKey(group) ? this.groupScore.get(group) : 0) + score);
    }

    /**
     * 玩家出牌
     */
    public void playCommandDtz(DtzPlayer player, List<Integer> cards, CardTypeDtz cardType) {
        if (lastWin != 0) lastWin = 0;
        setLastActionTime(TimeUtil.currentTimeMillis());

        if (cards != null && cards.size() > 0 && cards.get(0).intValue() > 0) {
            discardCount++;
        }

        //设置当前要出牌的座位
        setNowDisCardSeat(player.getSeat());
        disCards(player, cards, cardType);
        if (isTest() && !isOver()) { //在测试模式下而且没有结束
            while (true) {
                DtzPlayer next = seatMap.get((player.getSeat() + 1) > max_player_count ? 1 : player.getSeat() + 1); //得到下一个玩家
                if (next.getUserId() < 0) { // 是不是robot
                    List<Integer> oppo = null;
                    if (disCardSeat != next.getSeat()) {
                        oppo = getNowDisCardIds();
                    }
                    List<Integer> curList = next.getHandPais();
                    if (curList.isEmpty()) { //牌已经出完
                        break;
                    }
                    List<Integer> list = CardTypeTool.getBestAI(curList, oppo);
                    if (!checkCanPlayCard()) {
                        break;
                    }
                    changeDisCardRound(1);
                    if (list != null && !list.isEmpty()) {
                        /* 当前要出牌的座位 */
                        setNowDisCardSeat(next.getSeat());
                        disCards(next, list, cardType); // 发牌 nowDisCardSeat
                    } else {
                        // notLet(player);
                        if (next.getUserId() < 0) { // 对robot来说
                            notLet(next);
//							LogUtil.msg("位置:" + next.getSeat() + "的机器人要不起 ");
                        } else {
//							LogUtil.msg("不是机器人？！！");
                        }
                    }
                    player = next;
                    setLastActionTime(TimeUtil.currentTimeMillis());
                } else { //是玩家
                    break;
                }
            }
        }
    }

    private boolean checkCanPlayCard() {
        //检查是否有人
        if (seatMap.size() <= 0) {
            return false;
        }

        boolean isOver = isOver();
        boolean zeroHandPais = false;
        boolean allZeroPoint = true;
        for (DtzPlayer player : seatMap.values()) {
            // 零手牌
            if (player.getHandPais().size() <= 0) {
                zeroHandPais = true;
            }
            // 判断玩家的积分是否为0
            if (player.getPoint() != 0) {
                allZeroPoint = false;
            }
        }

        if (isOver) {
            LogUtil.e("checkCanPlayCard isOver error : tableId-->" + this.getId() + ", zeroHandPais-->" + zeroHandPais + ", allZeroPoint-->" + allZeroPoint);
            if (zeroHandPais && allZeroPoint) {
                return false;
            } else if (zeroHandPais) {
                LogUtil.e("checkCanPlayCard error : tableId-->" + this.getId() + ", zeroHandPais-->" + zeroHandPais + ", allZeroPoint-->" + allZeroPoint);
                return false;
            }
        } else if (zeroHandPais && allZeroPoint) {
            LogUtil.e("checkCanPlayCard error : tableId-->" + this.getId() + ", zeroHandPais-->" + zeroHandPais + ", allZeroPoint-->" + allZeroPoint);
            changeTableState(table_state.over);
            return false;
        } else if (zeroHandPais) {
            LogUtil.e("checkCanPlayCard error : tableId-->" + this.getId() + ", zeroHandPais-->" + zeroHandPais + ", allZeroPoint-->" + allZeroPoint);
            changeTableState(table_state.over);
            return false;
        }

        return true;
    }


    @Override
    public <T> T getPlayer(long id, Class<T> cl) {
        return (T) playerMap.get(id);
    }

    public boolean quitPlayer(Player player) {
        synchronized (this) {
            //已准备的玩家不能退出
            if (checkChooseDone()) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_10));
                return false;
            }

            if (!canQuit(player)) {
                return false;
            }

            if (getMasterId() == player.getUserId()) {
                if (isGoldRoom() || isDaikaiTable() || StringUtils.isNotBlank(serverKey) || isGroupRoom()) {
                } else {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_11));
                    return false;
                }
            }

            Iterator<Map.Entry<Integer, DtzPlayer>> it = getSeatMap().entrySet().iterator();
            while (it.hasNext()) {
                if (it.next().getValue().getUserId() == player.getUserId()) {
                    it.remove();
                }
            }

            getPlayerMap().remove(player.getUserId());

            int group = findGroupByPlayer(player);
            if (groupMap.containsKey(group)) {
                groupMap.get(group).remove(player);
            }

            changePlayers();

            player.clearTableInfo();
            player.cleanXipaiData();
            sendPlayerStatusMsg();
            StringBuilder str = new StringBuilder("table:" + getId() + " 玩家:" + player.getName() + " 退出, 现在所有seatMap 里面的玩家:");
            for (Entry<Integer, DtzPlayer> entry : getSeatMap().entrySet()) {
                str.append(entry.getKey()).append(":").append(entry.getValue().getName()).append(" ");
            }
            LogUtil.msg(str.toString());
            str = new StringBuilder("table:" + getId() + "现在已经选择座位的玩家: \n");
            for (Entry<Integer, List<DtzPlayer>> entry : getGroupMap().entrySet()) {
                str.append("组:group -> ").append(entry.getKey());
                for (DtzPlayer player1 : entry.getValue()) {
                    str.append("  玩家:").append(player1.getName()).append(" 属于分组 ").append(entry.getKey()).append(" 座位 ").append(player1.getSeat());
                }
                str.append("\n");
            }
            LogUtil.msg(str.toString());
            return true;
        }
    }

    @Override
    protected boolean quitPlayer1(Player player) {
        //给其他玩家发送退出信息
        return false;
    }

    @Override
    protected boolean joinPlayer1(Player player) {
        DtzPlayer pdkPlayer = (DtzPlayer) player;
        pdkPlayer.setRoundScore(0);
        pdkPlayer.setPoint(0);
        pdkPlayer.setDtzTotalPoint(0);
        if (isFourPlayer() && player.getUserId() == getMasterId()) {
            groupPlayer(1, (DtzPlayer) player);
        } else if (isFourPlayer()) {
            player.setSeat(0);
        } else {//三人二人添加组
            groupPlayer(player.getSeat(), (DtzPlayer) player);
        }
//        StringBuilder str = new StringBuilder("table:" + getId() + "现在所有seatMap 里面的玩家:");
//        for (Map.Entry<Integer, Player> entry : getSeatMap().entrySet()) {
//            str.append(entry.getKey()).append(":").append(entry.getValue().getName()).append(" ");
//        }
//        str.append(" 这个玩家:").append(player.getName());
//        LogUtil.msg(str.toString());
        return false;
    }

    @Override
    protected void initNext1() {
        setNowDisCardIds(null);
        this.jiesuan = false;
        //下面还要做清空操作
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
        int nextSeat = (dtzRound == 1) ? firstPai() : getNextDisCardSeat(); //
        setNowDisCardSeat(nextSeat);

        DtzPlayer nextPlayer = seatMap.get(nextSeat);
        int timeout;
        if (!nextPlayer.isRobot()) {
            if (matchId > 0L && discardCount == 0) {
                timeout = dtz_auto_timeout + 5000;
            } else {
                timeout = dtz_auto_timeout;
            }
        } else {
            timeout = dtz_auto_timeout;
        }

        for (DtzPlayer tablePlayer : getSeatMap().values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            res.addAllHandCardIds(tablePlayer.getHandPais());
            res.setNextSeat(nextSeat);
            res.setGameType(getWanFa());// 1跑得快 2麻将

            res.setBanker(lastWinSeat);

            res.addXiaohu(nextPlayer.getAutoPlay());
            res.addXiaohu(timeout);

            tablePlayer.writeSocket(res.build());
            if (tablePlayer.getAutoPlay() == 1) {
                addPlayLog(tablePlayer.getSeat(), DtzzConstants.action_tuoguan + "", 1 + "");
            }
            logFaPai(tablePlayer);
        }
//        LogUtil.msg(" table: " + id + " 连接房间的时候 出牌信息  " + nextSeat);
    }

    private int firstPai() {
        if (isFourPlayer() || getDisCardRandom() == 0) {
            return getMasterSeat();
        } else {
            return new Random().nextInt(getMaxPlayerCount()) + 1;
        }
    }

    /**
     * 机器人出牌
     */
    @Override
    protected void robotDealAction() {
        if (isTest()) {
            DtzPlayer next = seatMap.get(getNextDisCardSeatByDtz());
            if (next != null && next.getUserId() < 0) { //是机器人
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    LogUtil.e("robotDealAction err", e);
                }
                List<Integer> oppo = getNowDisCardIds();
                List<Integer> curList = next.getHandPais();
                List<Integer> list = CardTypeTool.getBestAI(curList, oppo);
                if (list != null) {
                    CardTypeDtz cardType = CardTypeToolDtz.toPokerType(list, this);
                    playCommandDtz(next, list, cardType);
                } else {
                    setNowDisCardSeat(next.getSeat());
                }
            }
        }
    }

    @Override
    protected void deal() {

    }

    @Override
    public Map<Long, DtzPlayer> getPlayerMap() {
        return playerMap;
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
//            LogUtil.msg("clear nowDisCardIds success" + "," + id);
        } else {
            this.nowDisCardIds = nowDisCardIds;
//            LogUtil.msg("set nowDisCardIds: "+ nowDisCardIds+","+id);
        }
        dbParamMap.put("nowDisCardIds", JSON_TAG);
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {
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
        setLastActionTime(TimeUtil.currentTimeMillis());
        setIsFirstRoundDisThree((int) objects[0]);
        // 是否二人比赛
        if (objects.length >= 2) {
            int isTwoPlayer = (int) objects[1];
            setMaxPlayerCount(isTwoPlayer);
            groupScore.put(1, 0);
            groupScore.put(2, 0);
            if (isTwoPlayer == 3) {
                groupScore.put(3, 0);
            }
        }
        if (objects.length >= 3) {
            setShowCardNumber((int) objects[2]);
        }

        if (isAutoPlay > 0) {
            dtz_auto_play_time = 1000;
            dtz_auto_timeout = isAutoPlay;
            dtz_auto_startnext = isAutoPlay;
            dtz_auto_timeout2 = dtz_auto_timeout;
        }

    }


    @Override
    protected void initNowAction(String nowAction) {

    }

    @Override
    public void initExtend0(JsonWrapper wrapper) {
        isFirstRoundDisThree = wrapper.getInt(1, 0);
        max_player_count = wrapper.getInt(2, 3);
        if (max_player_count == 0) {
            max_player_count = 3;
        }
        showCardNumber = wrapper.getInt(3, 0);
        //开始还原private int fangfei = 20, score_max = 600, jiangli = 0;
        //public int lastWin = 0, lastWinTemp = 0;
        this.settSeat = wrapper.getInt("settSeat", 0);
        this.jiesuan = Boolean.parseBoolean(wrapper.getString("jiesuan"));
        lastWin = wrapper.getInt("lastWin", 0);
        lastWinTemp = wrapper.getInt("lastWinTemp", 0);
        dtzRound = wrapper.getInt("dtzRound", 1);
        isNew = Boolean.parseBoolean((wrapper.getString("isNew") == null || wrapper.getString("isNew").isEmpty()) ? "true" : wrapper.getString("isNew"));
        score = wrapper.getInt("score", 0);
        tz_score = wrapper.getInt("tz_score", 0);
        serialIndex = wrapper.getInt("serialIndex", 1);
        default_Sore_Dtz = wrapper.getInt("default_Sore_Dtz", 60);
        fangfei = wrapper.getInt("fangfei", 20);
        score_max = wrapper.getInt("score_max", 600);
        jiangli = wrapper.getInt("jiangli", 0);
//        this.isAAConsume = Boolean.parseBoolean(wrapper.getString("isAAConsume"));
        if (payType == -1) {
            String isAAStr = wrapper.getString("AAConsume");
            if (!StringUtils.isBlank(isAAStr)) {
                this.payType = Boolean.parseBoolean(wrapper.getString("isAAConsume")) ? 1 : 2;
            } else {
                payType = 1;
            }
        }
        masterFP = Boolean.parseBoolean(wrapper.getString("masterFP"));
        useNowDis = Boolean.parseBoolean(wrapper.getString("useNowDis"));
        scorePlayerTemp = toPlayer(wrapper.getString("scorePlayerTemp"));
        groupPlayer = toPlayer(wrapper.getString("groupPlayer"));
        groupMap = loadGroupMap(wrapper.getString("groupMap"));
        groupScore = loadGroupScore(wrapper.getString("groupScore"));
        playTimes = loadPlayTimes(wrapper.getString("playTimes"));
        queue = loadQueue(wrapper.getString("queue"));
        groupSerial = loadGroupSerial(wrapper.getString("groupSerial"));
        removeTemp = loadRemoveTemp(wrapper.getString("removeTemp"));
        firstCardTypePair = loadFirstCardTypePair(wrapper.getString("firstCardTypePair"));
        kouType = wrapper.getInt("kouType", 0);
        out67Type = wrapper.getInt("out67Type", 0);
        kou8CardList = loadKou8CardList(wrapper.getString("kou8CardList"));
        disCardRandom = wrapper.getInt("disCardRandom", 0);
        ifMustPlay = wrapper.getInt("ifMustPlay", 0);
        wangTongZi = wrapper.getInt("wangTongZi", 0);
        isAutoPlay = wrapper.getInt("isAutoPlay", 0);
        modeId = wrapper.getString("gModeId");
        isDaiPai = wrapper.getInt("isDaiPai", 2);

        autoPlayGlob = wrapper.getInt("autoPlayGlob", 0);
        if (StringUtils.isBlank(modeId)) {
            modeId = "0";
        }
        if (isDaiPai == 2) {//下次更新去掉
            if (isThreePai()) {
                this.isDaiPai = 1;
            } else {
                this.isDaiPai = 0;
            }
        }
        if (isAutoPlay == 1) {
            isAutoPlay = 60000;
        }
        if (isAutoPlay > 0) {
            dtz_auto_timeout = isAutoPlay;
            dtz_auto_timeout2 = dtz_auto_timeout;
            dtz_auto_play_time = 1000;
            dtz_auto_startnext = dtz_auto_timeout;
        }


        bombScore.load(getWangTongZi());
        try {
            scoreList = (HashMap<DtzPlayer, HashMap<ScoreType, Integer>>) loadScore(wrapper.getString("scoreList"), HashMap.class);
            cardTemp = loadCards(wrapper.getString("cardTemp"));
            bureauTemp = loadCards(wrapper.getString("bureauTemp"));
            roundCardScore = (LinkedHashMap<DtzPlayer, HashMap<ScoreType, Integer>>) loadScore(wrapper.getString("roundCardScore"), LinkedHashMap.class);
            bureau = (LinkedHashMap<DtzPlayer, HashMap<ScoreType, Integer>>) loadScore(wrapper.getString("bureau"), LinkedHashMap.class);
//        	totalBureau = Integer.parseInt(wrapper.getString("totalBureau"));
//        	canSett = loadCanSett(wrapper.getString("canSett"));
            tempScoreList = (HashMap<DtzPlayer, HashMap<ScoreType, Integer>>) tempLoadScore(wrapper.getString("tempScoreList"), HashMap.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private Map<Integer, List<DtzPlayer>> loadGroupMap(String json) {
        Map<Integer, List<DtzPlayer>> map = new ConcurrentHashMap<>();
        if (json == null || json.isEmpty()) return map;
        JSONObject jsonObject = JSONObject.parseObject(json);
        for (Object group : jsonObject.keySet()) {
            int ggroup = Integer.valueOf(group.toString());
            JSONArray jsonArray = jsonObject.getJSONArray(group.toString());
            List<DtzPlayer> players = new ArrayList<>();
            for (Object jo : jsonArray) {
                JSONObject pdkob = (JSONObject) jo;
                DtzPlayer player = playerMap.get(pdkob.getLong("userId"));
                if (player != null) {
                    player.setSeat(pdkob.getIntValue("seat"));
                    players.add(player);
                }
            }
            map.put(ggroup, players);
        }
        return map;
    }

    private Map<Integer, Integer> loadGroupScore(String json) {
        Map<Integer, Integer> map = new ConcurrentHashMap<>();
        if (json == null || json.isEmpty()) {
            map.put(1, 0);
            map.put(2, 0);
            return map;
        }
        JSONObject jsonObject = JSONObject.parseObject(json);
        for (Object obj : jsonObject.keySet()) {
            map.put(Integer.valueOf(obj.toString()), jsonObject.getIntValue(obj.toString()));
        }
        return map;
    }

    private Pair<Integer, DtzPlayer> loadPlayTimes(String json) {
        if (json == null || json.isEmpty()) return Pair.with(0, null);
        JSONObject jsonObject = JSONObject.parseObject(json);
        int value0 = jsonObject.getIntValue("value0");
        DtzPlayer player = toPlayer(jsonObject.getString("value1"));
        return Pair.with(value0, player);
    }

    private Pair<CardTypeDtz, Integer> loadFirstCardTypePair(String json) {
        if (json == null || json.isEmpty()) return Pair.with(CardTypeDtz.CARD_0, 0);
        JSONObject jsonObject = JSONObject.parseObject(json);
        CardTypeDtz value0 = CardTypeDtz.CARD_0;
        int v0 = jsonObject.getIntValue("value0");
        CardTypeDtz[] list = CardTypeDtz.values();
        for (CardTypeDtz v : list) {
            if (v.getType() == v0) {
                value0 = v;
                break;
            }
        }
        int value1 = jsonObject.getIntValue("value1");
        return Pair.with(value0, value1);
    }

    private PriorityQueue<DtzPlayer> loadQueue(String json) {
        PriorityQueue<DtzPlayer> queue = new PriorityQueue<>();
        if (json == null || json.isEmpty()) return queue;
        JSONArray jsonArray = JSONArray.parseArray(json);
        for (Object obj : jsonArray) {
            DtzPlayer player = toPlayer(obj.toString());
            queue.offer(player);
        }
        return queue;
    }

    private HashMap<Integer, ArrayList<Pair<Integer, DtzPlayer>>> loadGroupSerial(String json) {
        HashMap<Integer, ArrayList<Pair<Integer, DtzPlayer>>> map = new HashMap<>();
        if (json == null || json.isEmpty()) return map;
        JSONObject jsonObject = JSONObject.parseObject(json);
        for (Object obj : jsonObject.keySet()) {
            JSONArray jsonArray = jsonObject.getJSONArray(obj.toString());
            ArrayList<Pair<Integer, DtzPlayer>> pairs = new ArrayList<>();
            for (Object jo : jsonArray) {
                JSONObject pairobj = (JSONObject) jo;
                pairs.add(Pair.with(pairobj.getIntValue("value0"), toPlayer(pairobj.getLong("value1") + "")));
            }
            map.put(Integer.valueOf(obj.toString()), pairs);
        }
        return map;
    }

    private HashSet<DtzPlayer> loadRemoveTemp(String json) {
        HashSet<DtzPlayer> set = new HashSet<>();
        if (json == null || json.isEmpty()) return set;
        JSONArray jsonArray = JSONArray.parseArray(json);
        for (Object obj : jsonArray) {
            JSONObject jsonObject = (JSONObject) obj;
            set.add(toPlayer(jsonObject.getString("userId")));
        }
        return set;
    }

    private Map<DtzPlayer, HashMap<ScoreType, Integer>> loadScore(String json, Class<? extends Map> mapclazz) throws Exception {
        Map<DtzPlayer, HashMap<ScoreType, Integer>> map = ObjectUtil.newInstance(mapclazz);
        if (json == null || json.isEmpty()) return map;
        JSONObject jsonObject = JSONObject.parseObject(json);
        for (Object obj : jsonObject.keySet()) {
            DtzPlayer player = toPlayer(obj.toString());
            JSONObject jsonObject_ob = jsonObject.getJSONObject(obj.toString());
            HashMap<ScoreType, Integer> score = new HashMap<>();
            for (Object oc : jsonObject_ob.keySet()) {
                ScoreType scoreType = ScoreType.valueOf(oc.toString());
                int sc = jsonObject_ob.getIntValue(oc.toString());
                score.put(scoreType, sc);
            }
            map.put(player, score);
        }
        return map;
    }

    private Map<DtzPlayer, HashMap<ScoreType, Integer>> tempLoadScore(String json, Class<? extends Map> mapclazz) throws Exception {
        Map<DtzPlayer, HashMap<ScoreType, Integer>> map = ObjectUtil.newInstance(mapclazz);
        if (json == null || json.isEmpty()) {
            return map;
        }
        JSONObject jsonObject = JSONObject.parseObject(json);
        for (Object obj : jsonObject.keySet()) {
            DtzPlayer player = toPlayer(obj.toString());
            JSONObject jsonObject_ob = jsonObject.getJSONObject(obj.toString());
            HashMap<ScoreType, Integer> score = new HashMap<>();
            for (Object oc : jsonObject_ob.keySet()) {
                ScoreType scoreType = ScoreType.valueOf(oc.toString());
                int sc = jsonObject_ob.getIntValue(oc.toString());
                score.put(scoreType, sc);
            }
            map.put(player, score);
        }
        return map;
    }

    private List<List<Integer>> loadCards(String json) {
        List<List<Integer>> list = new ArrayList<>();
        if (json == null || json.isEmpty()) return list;
        JSONArray jsonArray = JSONArray.parseArray(json);
        for (Object obj : jsonArray) {
            List<Integer> llist = new ArrayList<>();
            JSONArray jsonArray_2 = (JSONArray) obj;
            for (Object val : jsonArray_2) {
                llist.add(Integer.valueOf(val.toString()));
            }
            list.add(llist);
        }
        return list;
    }


    private List<Integer> loadKou8CardList(String json) {
        List<Integer> list = new ArrayList<>();
        if (json == null || json.isEmpty()) return list;
        JSONArray jsonArray = JSONArray.parseArray(json);
        for (Object val : jsonArray) {
            list.add(Integer.valueOf(val.toString()));
        }
        return list;
    }

    @Override
    protected String buildNowAction() {
        return null;
    }

    @Override
    public void setConfig(int index, int val) {

    }

    private boolean isFirstDissScore(boolean isBreak) {
        return (isBreak && playBureau == 1);
    }

    /**
     * 发送结算msg
     *
     * @param over      是否已经结束
     * @param winPlayer 赢的玩家
     */
    public ClosingInfoRes.Builder sendAccountsMsg(boolean over, Player winPlayer, boolean isBreak , int winGroup) {
        List<ClosingPlayerInfoRes> list = new ArrayList<>();
        List<ClosingPlayerInfoRes.Builder> builderList = new ArrayList<>();

        int minPointSeat = 0;
        int minPoint = 0;
        if (winPlayer != null) {
            for (DtzPlayer player : seatMap.values()) {
                if (player.getUserId() == winPlayer.getUserId()) {
                    continue;
                }
                if (minPoint == 0 || player.getPoint() < minPoint) {
                    minPoint = player.getPlayPoint();
                    minPointSeat = player.getSeat();
                }
            }
        }

        for (DtzPlayer player : seatMap.values()) {
            ClosingPlayerInfoRes.Builder build;
            build = player.bulidOneClosingPlayerInfoRes(this);

            HashMap<ScoreType, Integer> scoreMap = bureau.get(player); // 这里原来是scorelist 如果有bug 就改成以前的
            if (scoreMap != null) {
                build.addExt(((scoreMap.get(ScoreType.POINT_5) == null) ? 0 : scoreMap.get(ScoreType.POINT_5)) + ""); // 设置一下总5的个数  1
                build.addExt(((scoreMap.get(ScoreType.POITN_10) == null) ? 0 : scoreMap.get(ScoreType.POITN_10)) + ""); // 设置一下总10的个数  2
                build.addExt(((scoreMap.get(ScoreType.POINT_K) == null) ? 0 : scoreMap.get(ScoreType.POINT_K)) + ""); // 设置一下总k的数个 3
                build.addExt((tongziNumByPlayer(player)) + "");
            } else {
                build.addExt(0 + "");
                build.addExt(0 + "");
                build.addExt(0 + "");
                build.addExt(0 + "");
            }
            int ticketCount = 0;
            if(isGoldRoom()){
                build.setBoom((int)player.getWinGold());
            }else {
                build.setBoom(player.getWinLossPoint());
            }
            build.setTotalPoint(player.getDtzTotalPoint()); //设置总积分 大结算的  !!!!!!!!!!
            build.setPoint(player.getPoint()); //设置一下这个玩家的总分 小结算的

            build.addExt((xiNumByType(ScoreType.POINT_XI, player)) + "");// 设置一下总 "喜"的个数  5
            build.addExt((xiNumByType(ScoreType.POINT_JACKER_S, player)) + "");// 6
            build.addExt((xiNumByType(ScoreType.POINT_JACKER_B, player)) + "");// 7

            build.addExt((getBomdByPlayer(player)) + "");//设置一下总地炸的个数  3副牌的 // 8

            build.addExt(((player.getHandPais().size() != 0) ? 1 : 0) + "");//设置一下是否 bk  9

            build.addExt((tongziNumByType(ScoreType.POINT_TZ_A, player)) + "");//A筒子个数  10
            build.addExt((tongziNumByType(ScoreType.POINT_TZ_K, player)) + "");//k筒子个数  11
            build.addExt((tongziNumByType(ScoreType.POINT_TZ_2, player)) + "");//2筒子个数  12

            build.addExt(findGroupByPlayer(player) + "");// 13

            build.addExt(findPlayerMingCi(player) + "");// 14
            // 赢的哪一组
            build.addExt(winGroup + ""); // 15
            build.addExt((player.getRoundScore() + (build.getPoint())) + ""); //16
            build.addExt((tongziNumByType(ScoreType.POINT_TZ_S, player)) + "");//小王筒子个数 //17
            build.addExt((tongziNumByType(ScoreType.POINT_TZ_B, player)) + "");//大王筒子个数 //18
            //19
            build.addExt(String.valueOf((isGoldRoom() ? player.loadAllGolds() : 0)));
            build.addExt(String.valueOf(player.getCurrentLs()));
            build.addExt(String.valueOf(player.getMaxLs()));
            build.addExt(String.valueOf(matchId));

            build.addExt(String.valueOf(ticketCount));//23

            build.addExt((tongziNumByType(ScoreType.POINT_Xi_K, player)) + "");//K喜 24
            build.addExt((tongziNumByType(ScoreType.POINT_Xi_A, player)) + "");//A喜 25
            build.addExt((tongziNumByType(ScoreType.POINT_Xi_2, player)) + "");//2喜 26
            build.addExt((tongziNumByType(ScoreType.POINT_Xi_5Q, player)) + "");//5Q喜 27

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


            if (isCreditTable()) {
                player.setWinLoseCredit(player.getWinLossPoint() * creditDifen);
            }

        }

        //信用分计算
        if (isCreditTable()) {
            //计算信用负分
            calcNegativeCredit();

            long dyjCredit = 0;
            for (DtzPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingPlayerInfoRes.Builder builder : builderList) {
                DtzPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);
                //calcCommissionCreditDtz(player, dyjCredit);
                builder.addExt(player.getWinLoseCredit() + ""); //28
                builder.addExt(player.getCommissionCredit() + "");//29
                // 2019-02-26更新
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------

            for (DtzPlayer player : seatMap.values()) {
                player.setWinGold(player.getWinLossPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingPlayerInfoRes.Builder builder : builderList) {
                DtzPlayer player = seatMap.get(builder.getSeat());
                builder.addExt(player.getWinLoseCredit() + ""); //28
                builder.addExt(player.getCommissionCredit() + "");//29
                builder.setWinLoseCredit(player.getWinGold());
            }
        } else {
            for (ClosingPlayerInfoRes.Builder builder : builderList) {
                builder.addExt(0 + ""); //28
                builder.addExt(0 + ""); //29
            }
        }

        for (ClosingPlayerInfoRes.Builder builder : builderList) {
            list.add(builder.build());
        }

        ClosingInfoRes.Builder res = ClosingInfoRes.newBuilder();
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.addAllClosingPlayers(list);
        res.addAllExt(buildAccountsExt(winGroup, over ? 1 : 0));
        res.addAllCutDtzCard(this.kou8CardList);

        if (over && isGroupRoom() && !isCreditTable()) {
            res.setGroupLogId((int) saveUserGroupPlaylog());
        }
        GeneratedMessage msg = res.build();
        for (DtzPlayer player : seatMap.values()) {
            player.writeSocket(msg);
        }

        return res;
    }
    public void calcCommissionCreditDtz(DtzPlayer player, long dyjCredit) {
        player.setCommissionCredit(0);
        if(AAScoure == 100 ){//AA赠送分=100
            // 2020年11月19日 10:01:07  AA赠送分   从每个玩家抽成
            long _AACreditSum = 0;
            for (DtzPlayer p:getSeatMap().values()) {
                if(p.getDtzTotalPoint()>0){
                    _AACreditSum +=p.getDtzTotalPoint();
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

    public List<String> buildAccountsExt(int winGroup, int over) {
        List<String> ext = new ArrayList<>();
        ext.add(id + "");
        ext.add(masterId + "");
        ext.add(TimeUtil.formatTime(TimeUtil.now()));
        ext.add(playType + "");
        ext.add(dtzRound + ""); // 1
        ext.add(((this.groupScore.get(1) == null) ? 0 : this.groupScore.get(1)) + "");// 2
        ext.add(((this.groupScore.get(2) == null) ? 0 : this.groupScore.get(2)) + "");// 3
        if (isThreePlayer()) {
            ext.add(((this.groupScore.get(3) == null) ? 0 : this.groupScore.get(3)) + "");
        }
        ext.add(winGroup + ""); // 4
        ext.add(isGroupRoom() ? "1" : "0");

        //金币场大于0
        ext.add(CommonUtil.isPureNumber(modeId) ? modeId : "0");
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
        ext.add(String.valueOf(ratio));
        ext.add(String.valueOf(pay >= 0 ? pay : 0));
        ext.add(isGroupRoom() ? loadGroupId() : "");//13(12)

        ext.add(String.valueOf(matchId));//14

        ext.add(creditMode + ""); //15
        ext.add(creditJoinLimit + "");//16
        ext.add(creditDissLimit + "");//17
        ext.add(creditDifen + "");//18
        ext.add(creditCommission + "");//19
        ext.add(creditCommissionMode1 + "");//20
        ext.add(creditCommissionMode2 + "");//21
        ext.add(creditCommissionLimit + "");//22
        ext.add(over + "");//23

        return ext;
    }

    private int comeOtherTzOrXiScore(DtzPlayer player) {
        int sc = 0;
        BombScore bombScore = getBombScore();
        for (Entry<DtzPlayer, HashMap<ScoreType, Integer>> entry : bureau.entrySet()) {
            if (entry.getKey().equals(player)) {
                HashMap<ScoreType, Integer> score = entry.getValue();
                for (Entry<ScoreType, Integer> scentry : score.entrySet()) {
                    switch (getPlayType()) {
                        case DtzzConstants.play_type_3POK:
                        case DtzzConstants.play_type_3PERSON_3POK:
                        case DtzzConstants.play_type_2PERSON_3POK:
                            if (scentry.getKey() == ScoreType.POINT_TZ_K) {
                                sc += bombScore.getTongZi_K() * scentry.getValue();
                            } else if (scentry.getKey() == ScoreType.POINT_TZ_A) {
                                sc += bombScore.getTongZi_A() * scentry.getValue();
                            } else if (scentry.getKey() == ScoreType.POINT_TZ_2) {
                                sc += bombScore.getTongZi_2() * scentry.getValue();
                            } else if (scentry.getKey() == ScoreType.POINT_TZ_S || scentry.getKey() == ScoreType.POINT_TZ_B) {
                                sc += bombScore.getTongZi_Wang() * scentry.getValue();
                            } else if (scentry.getKey() == ScoreType.POINT_BD) {
                                sc += bombScore.getDiBomb() * scentry.getValue();
                            }
                            break;
                        case DtzzConstants.play_type_4POK:
                        case DtzzConstants.play_type_3PERSON_4POK:
                        case DtzzConstants.play_type_2PERSON_4POK:
                            if (scentry.getKey() == ScoreType.POINT_XI) {
                                sc += bombScore.getXi() * scentry.getValue();
                            } else if (scentry.getKey() == ScoreType.POINT_JACKER_S || scentry.getKey() == ScoreType.POINT_JACKER_B) {
                                sc += bombScore.getWang_Xi() * scentry.getValue();
                            }
                            break;
                    }
                }
            }
        }
        return sc;
    }

    @Override
    public void sendAccountsMsg() {
        ClosingInfoRes.Builder builder = sendAccountsMsg(true, null, true,0);
        saveLog(true, 0L, builder.build());
    }

    @Override
    public Class<? extends Player> getPlayerClass() {
        return DtzPlayer.class;
    }

    @Override
    public int getWanFa() {
        return SharedConstants.game_type_dtz;
    }

    @Override
    public boolean isTest() {
        return DtzzConstants.isTest;
    }

    @Override
    public void checkReconnect(Player player) {
    }

    // 是否显示剩余牌的数量
    public boolean isShowCardNumber() {
        return 1 == getShowCardNumber();
    }

    @Override
    public void checkCompetitionPlay() {
        checkAutoPlay();
    }

    @Override
    public void checkAutoPlay() {
        if (getSendDissTime() > 0) {
            lastActionTime += 1 * 1000;
            return;
        }

        if (getIsAutoPlay() >= 1) {
            synchronized (this) {

                if (isAutoPlayOff()) {
                    // 托管关闭
                    for (int seat : seatMap.keySet()) {
                        DtzPlayer player = seatMap.get(seat);
                        player.setAutoPlay(0);
                        lastActionTime += 1 * 1000;
                    }
                    return;
                }

                long time = TimeUtil.currentTimeMillis();
                if (getState() == table_state.play || (getState() == table_state.over && !jiesuan)) {
                    DtzPlayer player = seatMap.get(nowDisCardSeat);
                    if (player != null) {
                        long dtz_auto_timeout0;
                        long dtz_auto_play0;
                        if (player.isRobot()) {
                            dtz_auto_timeout0 = 2000;
                            dtz_auto_play0 = 2000;
                        } else {
                            dtz_auto_timeout0 = dtz_auto_timeout;
                            dtz_auto_play0 = dtz_auto_play_time;
                        }

                        if (player.getAutoPlay() == 1 && time - lastActionTime >= dtz_auto_play0) {
                            CardPair cardPair = CardTypeToolDtz.autoPlay(player.getHandPais(), getNowDisCardIds(), this);
                            if (cardPair != null) {
                                playCommand(player, new ArrayList<>(cardPair.getPokers()), cardPair.getType());
                                return;
                            }

                            // 自动不要
                            int canPlay = CardTypeToolDtz.isCanPlay(player.getHandPais(), getNowDisCardIds(), this);
                            if (canPlay == 0) {
                                this.giveup(player, 10);
                            }
                        }
                        int checkTime = (int) (time - lastActionTime);
                        if (checkTime > 10 * 1000) {
                            player.addAutoPlayCheckedTime(1 * 1000);
                            if (!player.isAutoPlayCheckedTimeAdded()) {
                                player.setAutoPlayCheckedTimeAdded(true);
                                player.addAutoPlayCheckedTime(10 * 1000);
                            }
                        }
                        if (player.getAutoPlayCheckedTime() > dtz_auto_timeout0) {
                            dtz_auto_timeout0 = dtz_auto_timeout2;
                        }
                        if (player.getAutoPlay() == 0 && checkTime >= dtz_auto_timeout0) {
//                            LogUtil.msg("房间号=" + getId() + ",玩家=" + player.getUserId() + "[" + player.getName() + "]座位=" + player.getSeat() + "自动托管");
                            player.setAutoPlay(1);
                            //发送托管
                            addPlayLog(player.getSeat(), DtzzConstants.action_tuoguan + "", 1 + "");
                            ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.RES_DTZ_AUTOPLAY, player.getSeat(), player.getAutoPlay());
                            broadMsg(build.build());

                            StringBuilder sb = new StringBuilder("Dtz");
                            sb.append("|").append(getId());
                            sb.append("|").append(getPlayBureau());
                            sb.append("|").append(player.getUserId());
                            sb.append("|").append(player.getSeat());
                            sb.append("|").append(player.getAutoPlay());
                            sb.append("|").append("autoPlay|checkAutoPlay");
                            LogUtil.msgLog.info(sb.toString());
                        }
                    }
                } else if (getState() == table_state.ready && playBureau > 1) {//自动下一局
                    for (DtzPlayer player : seatMap.values()) {
                        if (player.getState() != player_state.entry) {
                            continue;
                        }
                        // 玩家进入托管后，5秒自动准备
                        if (time - lastActionTime > 5 * 1000 && player.getAutoPlay() == 1) {
                            LogUtil.msg("Dtz|" + getId() + "|" + getPlayBureau() + "|" + player.getUserId() + "|" + player.getSeat() + "|autoReady");
                            autoReady(player);
                        } else if (time - lastActionTime > 30 * 1000) {
                            autoReady(player);
                        }
                    }
                }
            }
        }
    }

    private void setIsFirstRoundDisThree(int isFirstRoundDisThree) {
        this.isFirstRoundDisThree = isFirstRoundDisThree;
        changeExtend();
    }

    public int getShowCardNumber() {
        return showCardNumber;
    }

    private void setShowCardNumber(int showCardNumber) {
        this.showCardNumber = showCardNumber;
        changeExtend();
    }

    private int fangfei = 20, score_max = 600, jiangli = 0;


    //是否扣8张牌，默认0表示不扣，1为扣  修改为是否铺牌
    private int kouType = 0;
    //是否去掉6,7牌，默认0表示不去掉，1为去掉
    private int out67Type = 0;

    public int getKouType() {
        return kouType;
    }

    public int getOut67Type() {
        return out67Type;
    }

    @Override
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

    @Override
    public boolean createSimpleTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, boolean saveDb) throws Exception {
        int payType = StringUtil.getIntValue(params, 2, 1);
        int fangfei = payType == 1 ? 20 : 60;
        //结算总积分
        int score_max = StringUtil.getIntValue(params, 3, 600);
        //终局奖励
        int jiangli = StringUtil.getIntValue(params, 4, 0);
        //是否扣8张牌，默认0表示不扣，1为扣
        int kouType = StringUtil.getIntValue(params, 5, 0);
        //是否去掉6,7牌，默认0表示不去掉，1为去掉
        int out67Type = StringUtil.getIntValue(params, 6, 0);
        int playerCount = StringUtil.getIntValue(params, 7, 0);// 比赛人数
        int showCardNumber = StringUtil.getIntValue(params, 8, 0);// 是否显示剩余牌数量
        int haveJacker = StringUtil.getIntValue(params, 6, 0);
        int disCardRandom = StringUtil.getIntValue(params, 9, 0);//2人3人是否随机出牌
        int ifMustPlay = StringUtil.getIntValue(params, 10, 0);//是否有牌必打
        int wangTongZi = StringUtil.getIntValue(params, 11, 0);//是否王筒子
        int isAutoPlay = StringUtil.getIntValue(params, 12, 0);//是否托管

        setIsDaiPai(StringUtil.getIntValue(params, 13, 1));//是否带牌

        if (playerCount != 3) {
            return false;
        }

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

            setLastActionTime(TimeUtil.currentTimeMillis());
            setIsFirstRoundDisThree(haveJacker);
            setMaxPlayerCount(playerCount);
            groupScore.put(1, 0);
            groupScore.put(2, 0);
            if (playerCount == 3) {
                groupScore.put(3, 0);
            }
            setShowCardNumber(showCardNumber);

            TableDao.getInstance().save(info);
            loadFromDB(info);
        } else {
            setPlayType(play);
            setDaikaiTableId(daikaiTableId);
            this.id = id;
            this.totalBureau = bureauCount;
            this.playBureau = 1;
            setLastActionTime(TimeUtil.currentTimeMillis());
            setIsFirstRoundDisThree(haveJacker);
            setMaxPlayerCount(playerCount);
            groupScore.put(1, 0);
            groupScore.put(2, 0);
            if (playerCount == 3) {
                groupScore.put(3, 0);
            }
            setShowCardNumber(showCardNumber);
        }

        /*
         * 600分 AA 8 房主 30

		1000分 AA 15 房主 50
         */
        this.default_Sore_Dtz = (play == DtzzConstants.play_type_4POK) ? 80 : 60;
        this.fangfei = fangfei;//已废弃
        this.kouType = (play == DtzzConstants.play_type_3PERSON_3POK || play == DtzzConstants.play_type_3PERSON_4POK
                || play == DtzzConstants.play_type_2PERSON_3POK || play == DtzzConstants.play_type_2PERSON_4POK) ? 1 : kouType;
        this.out67Type = out67Type;
        this.score_max = score_max;
        this.jiangli = jiangli;
        this.payType = payType;
        this.disCardRandom = disCardRandom;
        this.ifMustPlay = ifMustPlay;
        this.wangTongZi = wangTongZi;

        if (isAutoPlay == 1 || isAutoPlay < 0) {
            isAutoPlay = 60;
        }
        this.isAutoPlay = isAutoPlay * 1000;

        //以下服务器防止乱传值
        if (!isTwoPlayer()) {
            this.ifMustPlay = 0;
        }
        if (isFourPlayer() || isFourPai()) {
            this.wangTongZi = 0;
        }
        if (!isThreePlayer() && getIfMustPlay() == 0) {
            this.isAutoPlay = 0;
        }

        bombScore.load(getWangTongZi());
        changeExtend();
        LogUtil.msg("table:" + id + " 创建桌子的时候  加的组分数:" + default_Sore_Dtz);


        if (isAutoPlay > 0) {
            dtz_auto_play_time = 1000;
            dtz_auto_timeout = isAutoPlay;
            dtz_auto_startnext = isAutoPlay;
            dtz_auto_timeout2 = dtz_auto_timeout;
        }


        return true;
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, Object... objects) throws Exception {
        createTable(new CreateTableInfo(player, TABLE_TYPE_NORMAL, play, bureauCount, params, strParams, true));
    }


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

        int payType = StringUtil.getIntValue(params, 2, 1);
        int fangfei = payType == 1 ? 20 : 60;
        //结算总积分
        int score_max = StringUtil.getIntValue(params, 3, 600);
        //终局奖励
        int jiangli = StringUtil.getIntValue(params, 4, 0);
        //是否扣8张牌，默认0表示不扣，1为扣
        int kouType = StringUtil.getIntValue(params, 5, 0);
        //是否去掉6,7牌，默认0表示不去掉，1为去掉
        int out67Type = StringUtil.getIntValue(params, 6, 0);
        int playerCount = StringUtil.getIntValue(params, 7, 0);// 比赛人数
        int showCardNumber = StringUtil.getIntValue(params, 8, 0);// 是否显示剩余牌数量
        int haveJacker = StringUtil.getIntValue(params, 6, 0);
        int disCardRandom = StringUtil.getIntValue(params, 9, 0);//2人3人是否随机出牌
        int ifMustPlay = StringUtil.getIntValue(params, 10, 0);//是否有牌必打
        int wangTongZi = StringUtil.getIntValue(params, 11, 0);//是否王筒子
        int time = StringUtil.getIntValue(params, 12, 0);//是否托管
        int isDaiPai = StringUtil.getIntValue(params, 13, 2);//是否带牌
        int isOpenGps = StringUtil.getIntValue(params, 22, 0);//是否开启了gps验证


        autoPlayGlob = StringUtil.getIntValue(params, 15, 0);


        if (playerCount == 0) {
            playerCount = 4;
        } else if (playerCount != 4 && playerCount != 3 && playerCount != 2) {
            LogUtil.e("create table playerCount err-->" + playerCount);
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            return false;
        }

        if (time == 1) {
            time = 60;
        }


        this.isAutoPlay = time * 1000;


        setLastActionTime(TimeUtil.currentTimeMillis());
        setIsFirstRoundDisThree(haveJacker);
        // 是否二人比赛
        int isTwoPlayer = playerCount;
        setMaxPlayerCount(isTwoPlayer);
        groupScore.put(1, 0);
        groupScore.put(2, 0);
        if (isTwoPlayer == 3) {
            groupScore.put(3, 0);
        }
        setShowCardNumber(showCardNumber);

        if (isAutoPlay > 0) {
            dtz_auto_play_time = 1000;
            dtz_auto_timeout = isAutoPlay;
            dtz_auto_startnext = isAutoPlay;
            dtz_auto_timeout2 = dtz_auto_timeout;
        }


        /*
         * 600分 AA 8 房主 30

		1000分 AA 15 房主 50
         */
        this.default_Sore_Dtz = (play == DtzzConstants.play_type_4POK) ? 80 : 60;
        this.fangfei = fangfei;//已废弃
        this.kouType = (play == DtzzConstants.play_type_3PERSON_3POK || play == DtzzConstants.play_type_3PERSON_4POK
                || play == DtzzConstants.play_type_2PERSON_3POK || play == DtzzConstants.play_type_2PERSON_4POK) ? 1 : kouType;
        this.out67Type = out67Type;
        this.score_max = score_max;
        this.jiangli = jiangli;
        this.payType = payType;
        this.disCardRandom = disCardRandom;
        this.ifMustPlay = ifMustPlay;
        this.wangTongZi = wangTongZi;

        this.isDaiPai = isDaiPai;
        //以下服务器防止乱传值
        if (!isTwoPlayer()) {
            this.ifMustPlay = 0;
        }
        if (isFourPlayer() || isFourPai()) {
            this.wangTongZi = 0;
        }
        if (!isThreePlayer() && getIfMustPlay() == 0) {
//            this.isAutoPlay = 0;
        }
        if (isDaiPai == 2) {
            if (isThreePai()) {
                this.isDaiPai = 1;
            } else {
                this.isDaiPai = 0;
            }
        }

        bombScore.load(getWangTongZi());
        if (creditMode == 1) {
            jiangli = 0;
        }

        changeExtend();
        LogUtil.msg("table:" + id + " 创建桌子的时候  加的组分数:" + default_Sore_Dtz);
        return true;
    }

    /**
     * 特别注意的是当table 牌打完了  必须重置
     */
    private boolean issend = true;

    /**
     * 检查所有玩家都是否准好了
     */
    public synchronized void ready() {
        if (isAllReady() && issend) { //检查是否全都准备好了
            initRandomChoose();
            for (Entry<Integer, DtzPlayer> entry : seatMap.entrySet()) {
                entry.getValue().writeComMessage(WebSocketMsgType.COM_SELECT_SEAT, getRandomChoose().get(this.getRoomId()));
            }
            issend = false;
        }
    }

    /**
     * 将玩家放到分组集合中
     */
    public void groupPlayer(int group, DtzPlayer player) {
        synchronized (this) {
            for (Entry<Integer, List<DtzPlayer>> entry : groupMap.entrySet()) {
                DtzPlayer temp = null;
                for (DtzPlayer pl : entry.getValue()) {
                    if (pl.getSeat() == player.getSeat()) {
                        temp = pl;
                    }
                }
                if (temp != null) entry.getValue().remove(temp);
                entry.getValue().remove(player);
            }
            if (!groupMap.containsKey(group)) {
                ArrayList<DtzPlayer> pp = new ArrayList<>();
                pp.add(player);
                groupMap.put(group, pp);
            } else {
                groupMap.get(group).add(player);
            }
        }
    }

    /**
     * 通过玩家获得组
     */
    public int findGroupByPlayer(Player player) {
        for (Entry<Integer, List<DtzPlayer>> entry : this.groupMap.entrySet()) {
            if (entry.getValue().contains(player)) {
                return entry.getKey();
            }
        }
        return 1;
    }

    int findGroupInit(Player player) {
        for (Entry<Integer, List<DtzPlayer>> entry : this.groupMap.entrySet()) {
            if (entry.getValue().contains(player)) {
                return entry.getKey();
            }
        }
        return 0;
    }

    /**
     * 检查是不是可以结束掉分组过程
     */
    public boolean checkChooseDone() {
//    	if(isThreePlayer()){
//    		return playerMap.size() == getMaxPlayerCount() && groupMap.size() == 3;
//    	}else if(isTwoPlayer()){
//    		return playerMap.size() == getMaxPlayerCount() && groupMap.size() == 2;
//    	}else{
        if (isFourPlayer()) {
            //首先要看这个 是不是有四个人 在房间里面
            return playerMap.size() == getMaxPlayerCount() && groupMap.size() == 2 && groupMap.get(1).size() == 2 && groupMap.get(2).size() == 2;
        } else {
            return false;
        }

//    	}
    }

    private ArrayList<DtzPlayer> getOther(List<DtzPlayer> list) {
        ArrayList<DtzPlayer> dd = new ArrayList<>(this.playerMap.values());
        dd.removeAll(list);
        return dd;
    }

    public static void main(String args[]) {
        Pair<String, String> p = new Pair<>("a", "b");
        System.out.println(p.getValue0());
        System.out.println(p.getValue1());
//    	PriorityQueue<Integer> queue = new PriorityQueue<>();
//    	for (int i = 0; i < 10; i ++) {
//    		queue.offer(i);
//    	}
//    	while (!queue.isEmpty()) {
//    		System.out.println(queue);
//    		queue.poll();
//    	}
        try {
//        	BeanInfo info = Introspector.getBeanInfo(String.class);
//        	java.beans.PropertyDescriptor[] ar = info.getPropertyDescriptors();
//        	for (java.beans.PropertyDescriptor property : ar) {
//        		System.out.println(property.getName() + ":" + property.getPropertyType());
//        	}
//    		List<Integer> list = new ArrayList<Integer>();
//    		Collections.copy(list, Arrays.asList(1, 2, 3));
//    		System.out.println(list);
//    		ArrayList<Integer> handPais = new ArrayList<>(Arrays.asList(1, 1, 2, 2, 1, 2, 3, 4, 5)), cards = new ArrayList<>(Arrays.asList(1, 2));
//    		System.out.println(com.sy599.game.qipai.datongzi.tool.CardTool.removeAll(cards, handPais));
//    		Pattern mat = Pattern.compile("(\\d{17}[0-9a-zA-Z]|\\d{14}[0-9a-zA-Z])");
//    		Pattern pt = Pattern.compile("\\d{6}(\\d{8}).*"), p2 = Pattern.compile("(\\d{4})(\\d{2})(\\d{2})");
//    		String card = "430781199411224015";
//    		if (mat.matcher(card).matches()) {
//    			Matcher ch = pt.matcher(card);
//    			if (ch.find()) {
//    				String brith = ch.group(1);
//        			ch = p2.matcher(brith);
//        			if (ch.find()) {
//        				System.out.println(ch.group(1) + " " + ch.group(2) + " " + ch.group(3));
//        			}
//    			}
////    		}
//    		HashMap<Integer, String> map = new HashMap<>();
//    		map.put(1, "2");
//    		map.put(2, "2");
//    		map.values().remove("2");
//    		System.out.println(map);
//    		System.out.println(Boolean.toString(false));
//    		JSONObject jsObject = JSONObject.parseObject("{\"a\":1}");
//    		System.out.println(jsObject);
//            jsObject.element("cc", 22);
//            System.out.println(jsObject);
//            ArrayList<Integer> list = new ArrayList<>();
//            list.add(1);
//            list.add(1);
//            list.add(2);
//            Iterator<Integer> it = list.iterator();
//            while (it.hasNext()) {
//                int val = it.next();
//                if (val == 1)
//                    it.remove();
//            }
//            System.out.println(list);
//            JSONArray json = new JSONArray();
//              System.out.println(StringUtils.isEmpty(""));
            HashMap<Integer, Integer> ma = new HashMap<>();
            ma.put(1, 1);
            ma.put(2, 1);
            System.out.println(ma.get(1) == ma.get(2));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 重写发牌等待时分组完成之后开始
     */
    public synchronized void checkDeal(long userId) {
        if (!isNew || isThreePlayer() || isTwoPlayer()) {
            if (!isAllReady()) {
                return;
            }

            // ------ 开局前检查信用分是否足够----------------
            if (!checkCreditOnTableStart()) {
                return;
            }

            if(isGoldRoom()){
                if(!payGoldRoomTicket()){
                    return;
                }
            }
            if(isCreditTable()){
                for (int i = 1; i <= getMaxPlayerCount(); i++) {
                    Player player = getSeatMap().get(i);
                    if(player != null && player.getGroupUser()!=null){
                        creditMap.put(player.getSeat(),player.getGroupUser().getCredit());
                    }else{
                        creditMap.put(player.getSeat(),0l);
                    }
                }
            }
            if(xipaiName != null && xipaiName.size() > 0){
                ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_xipai,xipaiName);
                for (Player tableplayer : getSeatMap().values()) {
                    tableplayer.writeSocket(com.build());
                }
                cleanXipaiName();
            }
            // 发牌
            fapai();
            setLastActionTime(TimeUtil.currentTimeMillis());
//            LogUtil.msg("table:" + id + "  检查发牌的时候 > seatMap.size:" + getSeatMap().size() + " playerMap.size:" + getPlayerMap().size());
            for (int i = 1; i <= getMaxPlayerCount(); i++) {
                Player player = getSeatMap().get(i);
                if (player != null) {
                    int num = CardTypeToolDtz.statisticsCardTypeNum(player.getHandPais(), this);
                    if (num > 0) {
                        if (getPlayType() == DtzzConstants.play_type_4POK || getPlayType() == DtzzConstants.play_type_3PERSON_4POK || getPlayType() == DtzzConstants.play_type_2PERSON_4POK) {
                            player.getMyExtend().setFengshen(FirstmythConstants.firstmyth_index2, num);
                        } else {
                            player.getMyExtend().setFengshen(FirstmythConstants.firstmyth_index1, num);
                        }
                    }
                    addPlayLog(StringUtil.implode(player.getHandPais(), ","));
                } else {
                    // 1 玩家不存在  2 玩家seat有值 但是 对象是null
                    StringBuilder str = new StringBuilder("table:" + id + " 在 " + i + " 位置的玩家为空. " + "groupMap的内容:");
                    for (Entry<Integer, List<DtzPlayer>> entry : getGroupMap().entrySet()) {
                        str.append(" 组:").append(entry.getKey()).append(" [");
                        for (DtzPlayer pt : entry.getValue()) {
                            str.append("{name:").append(pt.getName()).append(", seat:").append(pt.getSeat()).append("} ");
                        }
                        str.append("]  ");
                    }
                    LogUtil.msg(str.toString());
                }
            }
            //tempScoreList = cloneScoreList();//复制前面的所有筒子喜记录
            // 发牌msg
            sendDealMsg(0);
            robotDealAction();
            consume();//打筒子扣钻消耗移到发牌
            calcAfter();//打筒子发牌后就算作一局
            updateGroupTableDealCount();

            calcCoinOnStart();
            calcCreditAAOnStart();
            // 私密房，开局后，创建好友关系
            genGroupUserFriend();
        } else {
            robotDealAction();
        }
    }

    private HashMap<DtzPlayer, HashMap<ScoreType, Integer>> cloneScoreList() {
        HashMap<DtzPlayer, HashMap<ScoreType, Integer>> tempScoreMap = new HashMap<>();
        if ((!scoreList.isEmpty()) && (!isGoldRoom())) {
            for (Map.Entry<DtzPlayer, HashMap<ScoreType, Integer>> entry : scoreList.entrySet()) {
                HashMap<ScoreType, Integer> temp = new HashMap<>();
                for (Map.Entry<ScoreType, Integer> ent : entry.getValue().entrySet()) {
                    temp.put(ent.getKey(), ent.getValue());
                }
                tempScoreMap.put(entry.getKey(), temp);
            }
        }
        return tempScoreMap;
    }

    public void sendDissRoomMsg(Player sendplayer, boolean sendAll) {
        super.sendDissRoomMsg(sendplayer, sendAll);
    }

    /**
     * 座位选择
     */
    private HashMap<Integer, List<Integer>> randomChoose = new HashMap<>();

    private void initRandomChoose() {
        if (!randomChoose.containsKey(this.getRoomId())) {
            List<Integer> list = Arrays.asList(1, 1, 2, 2);
            Collections.shuffle(list);
            randomChoose.put(this.getRoomId(), list);
        }
    }

    public int randomSeat() {
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= getMaxPlayerCount(); i++) {
            list.add(i);
        }

        List<Integer> seatlist = new ArrayList<>(getSeatMap().keySet());
        list.removeAll(seatlist);
        if (list.isEmpty()) {
            return 0;
        }

        return list.get(0);
    }

    private HashMap<Integer, List<Integer>> getRandomChoose() {
        return randomChoose;
    }

    /**
     * 出完牌的玩家 这里面有先后顺序
     */
    private PriorityQueue<DtzPlayer> queue = new PriorityQueue<>();

    /**
     * 为该队列增加一个率先把牌出完的玩家
     */
    private synchronized void addFinishedPlaying(DtzPlayer player) {
        queue.offer(player);
    }

    /**
     * 用来判断是不是打完了  <组号, ArrayList<Pair<几游, DtzPlayer>，组号（1：A组，2：B组），几游（当局名次）
     **/
    private HashMap<Integer, ArrayList<Pair<Integer, DtzPlayer>>> groupSerial = new HashMap<>();

    /**
     * 这个是用来 方便排除  已经获得排名的不要乱用!
     */
    private HashSet<DtzPlayer> removeTemp = new HashSet<>();
    private int serialIndex = 1; //最后用到的

    /**
     * 检查是不是打完了
     */
    private synchronized boolean checkFinished() {
        while (!queue.isEmpty()) {
            DtzPlayer player = queue.poll();
            int group = getGroup(player.getSeat());
            ArrayList<Pair<Integer, DtzPlayer>> list;
            if (!groupSerial.containsKey(group)) {
                list = new ArrayList<>(); //新建对象
                removeTemp.add(player);
                list.add(Pair.with(serialIndex, player)); //放进去
                groupSerial.put(group, list); //放到map里面去
            } else {
                list = groupSerial.get(group);
                //这里有可能的bug
                for (Pair<Integer, DtzPlayer> entry : list) {
                    if (entry.getValue1().equals(player)) {
                        return false;
                    }
                }
                removeTemp.add(player);
                list.add(Pair.with(serialIndex, player)); //放进去
            }
            saveWinSeat(player);
            serialIndex++;
        }
        if (isThreePlayer() || isTwoPlayer()) {//两人三人玩法每组只有一个人
            return groupSerial.size() + 1 >= getMaxPlayerCount();
        } else {//四人玩法
            if (groupSerial.size() == 1) {
                if (groupSerial.get(1) != null) { //先得到第一组的
                    return groupSerial.get(1).size() == 2;
                } else//没有的话看看第二组
                    return groupSerial.get(2) != null && groupSerial.get(2).size() == 2;
            } else if (groupSerial.size() == 2) {
                if (groupSerial.get(1).size() == 2 && groupSerial.get(2).size() == 1) {
                    return true;
                } else if (groupSerial.get(1).size() == 1 && groupSerial.get(2).size() == 2) {
                    return true;
                } else return groupSerial.get(1).size() == 2 && groupSerial.get(2).size() == 2;
            } else {
                return false;
            }
        }
    }


    /**
     * 获取已经完成出牌的一组
     *
     * @return 1A组，2B组，3两组，0异常
     */
    public int getWinGroup() {
        int ret = 0;
        if (groupSerial.size() == 1) {
            //只有一组完全结束的情况
            if (groupSerial.get(1) != null) {
                //先得到A组的赢家
                if (groupSerial.get(1).size() == 2) {
                    ret = 1;
                }
            } else if (groupSerial.get(2) != null) {
                //A组没有赢家看看B组
                if (groupSerial.get(2).size() == 2) {
                    ret = 2;
                }
            }
        } else if (groupSerial.size() == 2) {
            //两组都完成的情况
            if (groupSerial.get(1).size() == 2 && groupSerial.get(2).size() == 1) {
                ret = 1;
            } else if (groupSerial.get(1).size() == 1 && groupSerial.get(2).size() == 2) {
                ret = 2;
            } else if (groupSerial.get(1).size() == 2 && groupSerial.get(2).size() == 2) {
                ret = 3;
            }
        } else {
            //没有完成结束的组,直接返回
            return 0;
        }
        //判断调用此方法时是否发生异常（正常调用只可能返回1或者2）
        if (ret == 0 || ret == 3) {
            StringBuilder error = new StringBuilder();
            error.append("异常===");
            for (Integer group : groupSerial.keySet()) {
                error.append("组号：").append(group).append(",");
                for (Pair<Integer, DtzPlayer> p : groupSerial.get(group)) {
                    error.append("成员位置：").append(p.getValue0().toString()).append(",");
                    error.append("成员信息：").append(p.getValue1().getUserId()).append("（").append(p.getValue1().getName()).append("）,");
                }
            }
            LogUtil.msgLog.info(error.toString());
        }
        return ret;
    }

    private HashMap<Integer, Integer> groupScore() {
        HashMap<Integer, Integer> map = new HashMap<>();
        if (isFourPlayer() && this.groupSerial.size() == 2) {
            if (groupSerial.containsKey(1) && groupSerial.get(1).size() == 2 && groupSerial.containsKey(2) && groupSerial.get(2).size() == 2) {
                ArrayList<Pair<Integer, DtzPlayer>> group_1 = groupSerial.get(1), group_2 = groupSerial.get(2);
                int score_1 = getGroupScore(group_1.get(0).getValue0(), group_1.get(1).getValue0());
                int score_2 = getGroupScore(group_2.get(0).getValue0(), group_2.get(1).getValue0());
                for (Entry<Integer, ArrayList<Pair<Integer, DtzPlayer>>> entry : groupSerial.entrySet()) {
                    if (entry.getKey() == 1) {
                        map.put(1, score_1);
                    }
                    if (entry.getKey() == 2) {
                        map.put(2, score_2);
                    }
                }
            }
        } else if (isThreePlayer() && this.groupSerial.size() == 3) {
            for (Entry<Integer, ArrayList<Pair<Integer, DtzPlayer>>> entry : groupSerial.entrySet()) {
                switch (entry.getValue().get(0).getValue0()) {
                    case 1:
                        map.put(entry.getKey(), 100);
                        break;
                    case 2:
                        map.put(entry.getKey(), -40);
                        break;
                    case 3:
                        map.put(entry.getKey(), -60);
                        break;
                }
            }
        } else if (isTwoPlayer() && this.groupSerial.size() == 2) {
            if (groupSerial.containsKey(1) && groupSerial.get(1).size() == 1 && groupSerial.containsKey(2) && groupSerial.get(2).size() == 1) {
                for (Entry<Integer, ArrayList<Pair<Integer, DtzPlayer>>> entry : groupSerial.entrySet()) {
                    switch (entry.getValue().get(0).getValue0()) {
                        case 1:
                            map.put(entry.getKey(), 60);
                            break;
                        case 2:
                            map.put(entry.getKey(), -60);
                            break;
                    }
                }
            }
        }
        return map;
    }

    /**
     * 加分组分数
     */
    private void addSerialScoreToGroup(HashMap<Integer, Integer> map) {
//        addScoreBuGroup(1, (map.containsKey(1) ? map.get(1) : 0));
//        addScoreBuGroup(2, (map.containsKey(2) ? map.get(2) : 0));
        if (map != null && !map.isEmpty()) {
            for (Entry<Integer, Integer> entry : map.entrySet()) {
                addScoreBuGroup(entry.getKey(), entry.getValue());
            }
        }
    }

    private int getGroupScore(int fv, int lv) {
        if ((fv == 1 && lv == 2) || (fv == 2 && lv == 1)) {
            return default_Sore_Dtz * 2;
        } else if ((fv == 1 && lv == 3) || (fv == 3 && lv == 1)) {
            return default_Sore_Dtz;
        } else if ((fv == 2 && lv == 4) || (fv == 4 && lv == 2)) {
            return 0 - default_Sore_Dtz;
        } else if ((fv == 3 && lv == 4) || (fv == 4 && lv == 3)) {
            return 0 - default_Sore_Dtz * 2;
        } else {
            return 0;
        }
    }

    private int default_Sore_Dtz = 60;

    /**
     * 补全 3, 4 或者 4名
     */
    private void completionGroupSerial() {
        List<DtzPlayer> pls = new ArrayList<>(playerMap.values()); //所有玩家
        pls.removeAll(removeTemp); //移除掉获得名次的玩家
        if (pls.size() == 2) {
            if (pls.get(0).getHandPais().size() < pls.get(1).getHandPais().size()) {
                addFinishedPlaying(pls.get(0));
                addFinishedPlaying(pls.get(1));
            } else {
                addFinishedPlaying(pls.get(1));
                addFinishedPlaying(pls.get(0));
            }
        } else if (pls.size() == 1) {
            addFinishedPlaying(pls.get(0));
        } else {
            return;
        }
        while (!queue.isEmpty()) {
            DtzPlayer player = queue.poll();
            int group = findGroupByPlayer(player);
            ArrayList<Pair<Integer, DtzPlayer>> list;
            if (!groupSerial.containsKey(group)) {
                list = new ArrayList<>(); //新建对象
                removeTemp.add(player);
                list.add(Pair.with(serialIndex, player)); //放进去
                groupSerial.put(group, list); //放到map里面去
            } else {
                list = groupSerial.get(group);
                removeTemp.add(player);
                list.add(Pair.with(serialIndex, player)); //放进去
            }
            serialIndex++;
        }
    }

    /**
     * 判断是不是有胜利的一组， 默认返回zero
     * <p>
     * autoPlayDiss:托管提前解散
     */
    private int hasWinGroup() {
        boolean isGold = isGoldRoom();
        boolean isMatch = isMatchRoom();
        for (Entry<Integer, Integer> entry : this.groupScore.entrySet()) {
            if ((isMatch && playedBureau >= totalBureau) || ((!isMatch) && isGold && playedBureau >= 1) || ((!isMatch) && (!isGold) && entry.getValue() >= score_max) || autoPlayDiss) {
                return entry.getKey();
            }
        }
        return 0;
    }

    private int calhasWinGroup() {
        HashMap<Integer, Integer> groupScore = new HashMap<>(this.groupScore);
        HashMap<Integer, Integer> addScore = groupScore();
        if (groupScore.containsKey(1)) {
            groupScore.put(1, groupScore.get(1) + (addScore.get(1) == null ? 0 : addScore.get(1)));
        }
        if (groupScore.containsKey(2)) {
            groupScore.put(2, groupScore.get(2) + (addScore.get(2) == null ? 0 : addScore.get(2)));
        }
        if (groupScore.containsKey(3)) {
            groupScore.put(3, groupScore.get(3) + (addScore.get(3) == null ? 0 : addScore.get(3)));
        }
        boolean isGold = isGoldRoom();
        boolean isMatch = isMatchRoom();
        for (Entry<Integer, Integer> entry : groupScore.entrySet()) {
            if ((isMatch && playedBureau >= totalBureau) || ((!isMatch) && isGold && playedBureau >= 1) || ((!isMatch) && (!isGold) && entry.getValue() >= score_max)) {    //有达到  结算分数的
                if (isThreePlayer()) {
                    if (groupScore.get(1) > groupScore.get(2) && groupScore.get(1) > groupScore.get(3)) {
//                        LogUtil.msg("table:" + id + " 结算 时的分数:  1 -> " + groupScore.get(1) + " 2 ->" + groupScore.get(2) + " 3 ->" + groupScore.get(3) + " A组胜利");
                        logGroupScore(groupScore, 1, "A组胜利");
                        return 1;
                    } else if (groupScore.get(2) > groupScore.get(1) && groupScore.get(2) > groupScore.get(3)) {
//                        LogUtil.msg("table:" + id + " 结算 时的分数:  1 -> " + groupScore.get(1) + " 2 ->" + groupScore.get(2) + " 3 ->" + groupScore.get(3) + " B组胜利");
                        logGroupScore(groupScore, 2, "B组胜利");
                        return 2;
                    } else if (groupScore.get(3) > groupScore.get(1) && groupScore.get(3) > groupScore.get(2)) {
//                        LogUtil.msg("table:" + id + " 结算 时的分数:  1 -> " + groupScore.get(1) + " 2 ->" + groupScore.get(2) + " 3 ->" + groupScore.get(3) + " C组胜利");
                        logGroupScore(groupScore, 3, "C组胜利");
                        return 3;
                    } else if (groupScore.get(1).intValue() == groupScore.get(2).intValue() && groupScore.get(1) > groupScore.get(3)) {
//                        LogUtil.msg("table:" + id + " 结算 时的分数:  1 -> " + groupScore.get(1) + " 2 ->" + groupScore.get(2) + " 3 ->" + groupScore.get(3) + " AB组胜利");
                        logGroupScore(groupScore, 12, "AB组胜利");
                        return 12;
                    } else if (groupScore.get(1).intValue() == groupScore.get(3).intValue() && groupScore.get(1) > groupScore.get(2)) {
//                        LogUtil.msg("table:" + id + " 结算 时的分数:  1 -> " + groupScore.get(1) + " 2 ->" + groupScore.get(2) + " 3 ->" + groupScore.get(3) + " AC组胜利");
                        logGroupScore(groupScore, 13, "AC组胜利");
                        return 13;
                    } else if (groupScore.get(2).intValue() == groupScore.get(3).intValue() && groupScore.get(2) > groupScore.get(1)) {
//                        LogUtil.msg("table:" + id + " 结算 时的分数:  1 -> " + groupScore.get(1) + " 2 ->" + groupScore.get(2) + " 3 ->" + groupScore.get(3) + " BC组胜利");
                        logGroupScore(groupScore, 23, "BC组胜利");
                        return 23;
                    } else if (groupScore.get(1).intValue() == groupScore.get(2).intValue() && groupScore.get(2) == groupScore.get(3)) {
//                        LogUtil.msg("table:" + id + " 结算 时的分数:  1 -> " + groupScore.get(1) + " 2 ->" + groupScore.get(2) + " 3 ->" + groupScore.get(3) + " ABC组胜利");
                        logGroupScore(groupScore, 123, "ABC组胜利");
                        return 123;
                    }
                } else {
                    if (groupScore.get(1) > groupScore.get(2)) {
//                        LogUtil.msg("table:" + id + " 结算 时的分数:  1 -> " + groupScore.get(1) + " 2 ->" + groupScore.get(2) + " A组胜利");
                        logGroupScore(groupScore, 1001, "A组胜利");
                        return 1;
                    } else if (groupScore.get(1) < groupScore.get(2)) {
//                        LogUtil.msg("table:" + id + " 结算 时的分数:  1 -> " + groupScore.get(1) + " 2 ->" + groupScore.get(2) + " B组胜利");
                        logGroupScore(groupScore, 1002, "B组胜利");
                        return 2;
                    } else if (groupScore.get(1).intValue() == groupScore.get(2).intValue()) {
//                        LogUtil.msg("table:" + id + " 结算 时的分数:  1 -> " + groupScore.get(1) + " 2 ->" + groupScore.get(2) + " A,B 组总分数相同");
                        logGroupScore(groupScore, 1012, "A,B 组总分数相同");
                        return 12; // 3
                    }
                }
                return entry.getKey();
            }
        }
//        LogUtil.msg("table:" + id + " 结算 时的分数:  1 -> " + groupScore.get(1) + " 2 ->" + groupScore.get(2) + " 没有哪一组达到大结算分数.");
        logGroupScore(groupScore, 0, "没有哪一组达到大结算分数");
        //如果没有正常打完。
        return -1;
    }

    /**
     * 用来记录详细分数 这是 记录 所有 筒子的  大结算的时候需要 不能清除
     */
    private HashMap<DtzPlayer, HashMap<ScoreType, Integer>> scoreList = new HashMap<>();
    //临时在发牌时存所有筒子喜记录  提前解散用
    private HashMap<DtzPlayer, HashMap<ScoreType, Integer>> tempScoreList = new HashMap<>();

    public void setTempScoreList(HashMap<DtzPlayer, HashMap<ScoreType, Integer>> tempScoreList) {
        this.tempScoreList = tempScoreList;
    }

    public HashMap<DtzPlayer, HashMap<ScoreType, Integer>> getTempScoreList() {
        return tempScoreList;
    }

    public HashMap<DtzPlayer, HashMap<ScoreType, Integer>> getScoreList() {
        return scoreList;
    }

    public void startNext() {

    }

    /**
     * 记录一下牌面值 (累不累加是在这里判断)
     */
    private void recordCard(List<Integer> pokers, DtzPlayer player) {
        switch (playType) {
            case DtzzConstants.play_type_3POK:
            case DtzzConstants.play_type_3PERSON_3POK:
            case DtzzConstants.play_type_2PERSON_3POK:
                if (CardTypeToolDtz.isTongZi(pokers)) {
                    if (CardTypeToolDtz.tongZi_(pokers, 13)) { //K筒子
                        addToScoreList(player, ScoreType.POINT_TZ_K); //全局的
                    } else if (CardTypeToolDtz.tongZi_(pokers, 14)) { //a 筒子
                        addToScoreList(player, ScoreType.POINT_TZ_A); //全局的
                    } else if (CardTypeToolDtz.tongZi_(pokers, 15)) { //2筒子
                        addToScoreList(player, ScoreType.POINT_TZ_2); //全局的
                    } else if (CardTypeToolDtz.tongZi_(pokers, 16)) { //小王筒子
                        addToScoreList(player, ScoreType.POINT_TZ_S); //全局的
                    } else if (CardTypeToolDtz.tongZi_(pokers, 17)) { //大王筒子
                        addToScoreList(player, ScoreType.POINT_TZ_B); //全局的
                    }
                } else if (CardTypeToolDtz.isBobmDi(pokers)) { //是不是地炸
                    addToScoreList(player, ScoreType.POINT_BD); //全局的
                }
                break;
            case DtzzConstants.play_type_4POK:
            case DtzzConstants.play_type_3PERSON_4POK:
            case DtzzConstants.play_type_2PERSON_4POK:
                if (CardTypeToolDtz.isXi(pokers)) {
                    if (CardTypeToolDtz.xiTypeOf_(pokers, 17)) { //大王息
                        addToScoreList(player, ScoreType.POINT_JACKER_B); //全局的
                    } else if (CardTypeToolDtz.xiTypeOf_(pokers, 16)) { //小王息
                        addToScoreList(player, ScoreType.POINT_JACKER_S); //全局的
                    } else { //小sss子
                        addToScoreList(player, ScoreType.POINT_XI);
                    }
                }
                break;
            case DtzzConstants.play_type_2PERSON_4Xi:
            case DtzzConstants.play_type_3PERSON_4Xi:
            case DtzzConstants.play_type_4PERSON_4Xi:
                if (CardTypeToolDtz.isTongZi(pokers)) {
                    if (CardTypeToolDtz.tongZi_(pokers, 13)) { //K筒子
                        addToScoreList(player, ScoreType.POINT_TZ_K); //全局的
                    } else if (CardTypeToolDtz.tongZi_(pokers, 14)) { //a 筒子
                        addToScoreList(player, ScoreType.POINT_TZ_A); //全局的
                    } else if (CardTypeToolDtz.tongZi_(pokers, 15)) { //2筒子
                        addToScoreList(player, ScoreType.POINT_TZ_2); //全局的
                    }
                } else if (CardTypeToolDtz.isBobmDi(pokers)) { //是不是地炸
                    addToScoreList(player, ScoreType.POINT_BD); //全局的
                }
                if (CardTypeToolDtz.isXi(pokers)) {
                    if (CardTypeToolDtz.xiTypeOf_(pokers, 13)) { //K喜
                        addToScoreList(player, ScoreType.POINT_Xi_K);
                    } else if (CardTypeToolDtz.xiTypeOf_(pokers, 14)) { //A喜
                        addToScoreList(player, ScoreType.POINT_Xi_A);
                    } else if (CardTypeToolDtz.xiTypeOf_(pokers, 15)) { //2喜
                        addToScoreList(player, ScoreType.POINT_Xi_2);
                    } else { //5Q喜
                        addToScoreList(player, ScoreType.POINT_Xi_5Q);
                    }
                }
                break;
        }
        pokers = CardToolDtz.toValueList(pokers);
        for (int val : pokers) {
            if (val == 5) {
                addToScoreList(player, ScoreType.POINT_5);
            } else if (val == 10) {
                addToScoreList(player, ScoreType.POITN_10);
            } else if (val == 13) {
                addToScoreList(player, ScoreType.POINT_K);
            }
        }
    }

    public void recordCardAll(List<List<Integer>> cards, DtzPlayer pdkplayer) {
        for (List<Integer> poker : cards) {
            recordCard(poker, pdkplayer);
        }
        for (List<Integer> poker : this.bureauTemp) {
            logScoreHistory(pdkplayer, poker); //如果有bug的话就注释掉
        }
    }

    /**
     * 这个方法不能给其他方法拿去用。 只能自己用
     */
    public void addToScoreList(DtzPlayer player, ScoreType type) {
        if (!scoreList.containsKey(player)) {
            HashMap<ScoreType, Integer> score = new HashMap<>();
            score.put(type, 1);
            scoreList.put(player, score);
        } else {
            HashMap<ScoreType, Integer> score = scoreList.get(player);
            if (score != null) {
                if (score.get(type) == null) {
                    score.put(type, 1);
                } else {
                    score.put(type, score.get(type) + 1);
                }
            } else {
                score = new HashMap<>();
                score.put(type, 1);
                scoreList.put(player, score);
            }
        }
    }

    public void tempAddToScoreList(DtzPlayer player, ScoreType type) {
        if (!tempScoreList.containsKey(player)) {
            HashMap<ScoreType, Integer> score = new HashMap<>();
            score.put(type, 1);
            tempScoreList.put(player, score);
        } else {
            HashMap<ScoreType, Integer> score = tempScoreList.get(player);
            if (score != null) {
                if (score.get(type) == null) {
                    score.put(type, 1);
                } else {
                    score.put(type, score.get(type) + 1);
                }
            } else {
                score = new HashMap<>();
                score.put(type, 1);
                tempScoreList.put(player, score);
            }
        }
    }

    /**
     * 这个方法不能给其他方法拿去用。 只能自己用 <br />
     * 这是 记录一轮中的5 10 k 数量的
     */
    private void addToRoundScoreList(DtzPlayer player, ScoreType type) {
        if (!roundCardScore.containsKey(player)) {
            HashMap<ScoreType, Integer> score = new HashMap<>();
            score.put(type, 1);
            roundCardScore.put(player, score);
        } else {
            HashMap<ScoreType, Integer> score = roundCardScore.get(player);
            if (score != null) {
                if (score.get(type) == null) {
                    score.put(type, 1);
                } else {
                    score.put(type, score.get(type) + 1);
                }
            } else {
                score = new HashMap<>();
                score.put(type, 1);
                roundCardScore.put(player, score);
            }
        }
    }

    /**
     * 得到某个玩家的地炸数量
     */
    private int getBomdByPlayer(DtzPlayer player) {
        int num = 0;
        for (Entry<DtzPlayer, HashMap<ScoreType, Integer>> entry : scoreList.entrySet()) {
            if (entry.getKey().equals(player)) {
                HashMap<ScoreType, Integer> score = entry.getValue();
                for (Entry<ScoreType, Integer> scentry : score.entrySet()) {
                    if (scentry.getKey() == ScoreType.POINT_BD) {
                        num += scentry.getValue();
                    }
                }
            }
        }
        return num;
    }

    /**
     * 得到某种筒子的数量
     */
    private int tongziNumByType(ScoreType type, DtzPlayer player) {
        int num = 0;
        for (Entry<DtzPlayer, HashMap<ScoreType, Integer>> entry : scoreList.entrySet()) {
            if (entry.getKey().equals(player)) {
                HashMap<ScoreType, Integer> score = entry.getValue();
                for (Entry<ScoreType, Integer> scentry : score.entrySet()) {
                    if (scentry.getKey() == type) {
                        num += scentry.getValue();
                    }
                }
            }
        }
        return num;
    }

    private int xiNumByType(ScoreType type, DtzPlayer player) {
        int num = 0;
        for (Entry<DtzPlayer, HashMap<ScoreType, Integer>> entry : scoreList.entrySet()) {
            if (entry.getKey().equals(player)) {
                HashMap<ScoreType, Integer> score = entry.getValue();
                for (Entry<ScoreType, Integer> scentry : score.entrySet()) {
                    if (scentry.getKey() == type) {
                        num += scentry.getValue();
                    }
                }
            }
        }
        return num;
    }

    /**
     * 某个玩家的筒子数量
     */
    private int tongziNumByPlayer(DtzPlayer player) {
        int num = 0;
        for (Entry<DtzPlayer, HashMap<ScoreType, Integer>> entry : scoreList.entrySet()) {
            if (entry.getKey() == player) {
                HashMap<ScoreType, Integer> score = entry.getValue();
                for (Entry<ScoreType, Integer> scentry : score.entrySet()) {
                    if (scentry.getKey() == ScoreType.POINT_TZ_B || scentry.getKey() == ScoreType.POINT_TZ_S
                            || scentry.getKey() == ScoreType.POINT_TZ_2 || scentry.getKey() == ScoreType.POINT_TZ_K
                            || scentry.getKey() == ScoreType.POINT_TZ_A || scentry.getKey() == ScoreType.POINT_TZ) {
                        num += scentry.getValue();
                    }
                }
            }
        }
        return num;
    }

    /**
     * 获得所有筒子喜的分数
     *
     * @return
     */
    private Map<Integer, Integer> getTongziXiFen() {
        Map<Integer, Integer> map = new HashMap<>();
        int addFen = 0;
        BombScore bombScore = getBombScore();
        for (Entry<DtzPlayer, HashMap<ScoreType, Integer>> entry : scoreList.entrySet()) {
            addFen = 0;
            Map<ScoreType, Integer> score = entry.getValue();
            for (Entry<ScoreType, Integer> scentry : score.entrySet()) {
                if (scentry.getKey() == ScoreType.POINT_TZ_K) {
                    addFen += bombScore.getTongZi_K() * scentry.getValue();
                } else if (scentry.getKey() == ScoreType.POINT_TZ_A) {
                    addFen += bombScore.getTongZi_A() * scentry.getValue();
                } else if (scentry.getKey() == ScoreType.POINT_TZ_2) {
                    addFen += bombScore.getTongZi_2() * scentry.getValue();
                } else if (scentry.getKey() == ScoreType.POINT_TZ_S || scentry.getKey() == ScoreType.POINT_TZ_B) {
                    addFen += bombScore.getTongZi_Wang() * scentry.getValue();
                } else if (scentry.getKey() == ScoreType.POINT_BD) {
                    addFen += bombScore.getDiBomb() * scentry.getValue();
                } else if (scentry.getKey() == ScoreType.POINT_XI) {
                    addFen += bombScore.getXi() * scentry.getValue();
                } else if (scentry.getKey() == ScoreType.POINT_JACKER_S || scentry.getKey() == ScoreType.POINT_JACKER_B) {
                    addFen += bombScore.getWang_Xi() * scentry.getValue();
                } else if (scentry.getKey() == ScoreType.POINT_Xi_5Q) {
                    addFen += bombScore.getXi_5Q() * scentry.getValue();
                } else if (scentry.getKey() == ScoreType.POINT_Xi_K) {
                    addFen += bombScore.getXi_K() * scentry.getValue();
                } else if (scentry.getKey() == ScoreType.POINT_Xi_A) {
                    addFen += bombScore.getXi_A() * scentry.getValue();
                } else if (scentry.getKey() == ScoreType.POINT_Xi_2) {
                    addFen += bombScore.getXi_2() * scentry.getValue();
                }
            }
            map.put(entry.getKey().getSeat(), addFen);
        }
        return map;
    }

    /**
     * 房主位置
     */
    private int getMasterSeat() {
        for (DtzPlayer player : seatMap.values()) {
            if (player.getUserId() == this.masterId) {
                return player.getSeat();
            }
        }
        return 1;
    }

    public int lastWin = 0, lastWinTemp = 0;


    /**
     * 存下来上一次第一个 出完的
     */
    private void saveWinSeat(DtzPlayer player) {
        if (serialIndex == 1) {
            this.lastWinTemp = player.getSeat();
        }
    }

    /**
     * 延迟结算
     */
    public synchronized void delayedSettlement(int seta) {
        if (this.getState() == table_state.over) { //打完了
            this.jiesuan = true;
            completionGroupSerial();
            calcOver();
//		   addSerialScoreToGroup(groupScore());
            checkSettlement();
            clearBureauScoreLog(); //清理一局的牌和分数
            this.score = 0; //清除牌面分数
            if (isGoldRoom()) {
//                for (DtzPlayer player : playerMap.values()) {
//                    player.setPoint(0); //清除这局
//                    player.setDtzTotalPoint(0);
//                }
            } else {
                for (DtzPlayer player : playerMap.values()) {
                    player.setPoint(0); //清除这局
                }
            }
            getNowDisCardIds().clear();
            groupSerial.clear();
            removeTemp.clear();
            serialIndex = 1;
            dtzRound++;
            setMasterFP(false);
            useNowDis = false;
            this.nowDisCardSeat = 0;
            this.settSeat = 0;
            this.lastWin = this.lastWinTemp;

            tempScoreList = cloneScoreList();//复制前面的所有筒子喜记录

            if (isGoldRoom()) {
//                tempScoreList.clear();
//                scoreList.clear();
//                for (Map.Entry<Integer, Integer> kv : groupScore.entrySet()) {
//                    kv.setValue(0);
//                }
            }
        }
    }

    /**
     * 查询玩家名次
     */
    int findPlayerMingCi(DtzPlayer player) {
        for (Entry<Integer, ArrayList<Pair<Integer, DtzPlayer>>> entry : this.groupSerial.entrySet()) {
            for (Pair<Integer, DtzPlayer> pair : entry.getValue()) {
                if (pair.getValue1().equals(player)) {
                    return pair.getValue0();
                }
            }
        }
        return 0;
    }

    /**
     * 牌的暂存(cardTemp存放筒子，地炸等特殊牌、bureauTemp存放出的牌)
     */
    private List<List<Integer>> cardTemp = new ArrayList<>(), bureauTemp = new ArrayList<>();

    /**
     * 这是一轮的 用于记录5, 10, k的 数量	HashMap<PdkPlayer, HashMap<ScoreType, Integer>>
     */
    private LinkedHashMap<DtzPlayer, HashMap<ScoreType, Integer>> roundCardScore = new LinkedHashMap<>();

    /***
     * 把牌加进去,处理吃分
     */
    public void offerdCard(List<Integer> cards, DtzPlayer player) {
        bureauTemp.add(new ArrayList<>(cards));
        switch (playType) {
            case DtzzConstants.play_type_3POK:
            case DtzzConstants.play_type_3PERSON_3POK:
            case DtzzConstants.play_type_2PERSON_3POK:
                if (CardTypeToolDtz.isTongZi(cards)) {
                    int value = CardToolDtz.toVal(cards.get(0));
                    if (value == 15 || value == 14 || value == 13 || value == 16 || value == 17) { // 2 a k  筒子加进去 +王筒子
                        cardTemp.clear();
                        cardTemp.add(new ArrayList<>(cards));
                    }
                }
                if (CardTypeToolDtz.isBobmDi(cards)) {
                    cardTemp.clear();
                    cardTemp.add(new ArrayList<>(cards));
                }
                break;
            case DtzzConstants.play_type_4POK:
            case DtzzConstants.play_type_3PERSON_4POK:
            case DtzzConstants.play_type_2PERSON_4POK:
                if (CardTypeToolDtz.isXi(cards)) {
                    cardTemp.add(new ArrayList<>(cards));
                }
                break;
            case DtzzConstants.play_type_2PERSON_4Xi:
            case DtzzConstants.play_type_3PERSON_4Xi:
            case DtzzConstants.play_type_4PERSON_4Xi:
                if (CardTypeToolDtz.isTongZi(cards)) {
                    int value = CardToolDtz.toVal(cards.get(0));
                    if (value == 15 || value == 14 || value == 13 || value == 16 || value == 17) { // 2 a k  筒子加进去 +王筒子
                        cardTemp.clear();
                        cardTemp.add(new ArrayList<>(cards));
                    }
                }
                if (CardTypeToolDtz.isXi(cards)) {
                    cardTemp.clear();
                    cardTemp.add(new ArrayList<>(cards));
                }
                break;
        }
        cards = CardToolDtz.toValueList(cards);
        for (int val : cards) {
            if (val == 5) {
                addToRoundScoreList(player, ScoreType.POINT_5);
            } else if (val == 10) {
                addToRoundScoreList(player, ScoreType.POITN_10);
            } else if (val == 13) {
                addToRoundScoreList(player, ScoreType.POINT_K);
            }
        }
    }

    /**
     * 得到牌的缓存
     */
    public List<List<Integer>> getCardTemp() {
        return cardTemp;
    }

    /**
     * 清掉牌
     */
    public void clearCardTemp() {
        cardTemp.clear();
        roundCardScore.clear();
        bureauTemp.clear();
    }

    /**
     * 得到一轮的5, 10, k的总数  就是 桌子上面 这一轮 所有 这些
     */
    private Triplet<Integer, Integer, Integer> getFragmentCard() {
        int num5 = 0, num10 = 0, numk = 0;
        for (Entry<DtzPlayer, HashMap<ScoreType, Integer>> entry : roundCardScore.entrySet()) {
            for (Entry<ScoreType, Integer> sc_entry : entry.getValue().entrySet()) {
                if (sc_entry.getKey() == ScoreType.POINT_5) {
                    num5 += sc_entry.getValue();
                } else if (sc_entry.getKey() == ScoreType.POITN_10) {
                    num10 += sc_entry.getValue();
                } else if (sc_entry.getKey() == ScoreType.POINT_K) {
                    numk += sc_entry.getValue();
                }
            }
        }
        return Triplet.with(num5, num10, numk);
    }

    /**
     * 转化为分数
     */
    public int getRoundFragmentCardSocre() {
        Triplet<Integer, Integer, Integer> triplet = getFragmentCard();
        return triplet.getValue0() * 5 + triplet.getValue1() * 10 + triplet.getValue2() * 10;
    }

    /**
     * 得到全局筒子的总分 A组， B组
     */
    public List<Integer> getAllTzOrXiGroupScore() {
        List<Integer> groupCount = new ArrayList<Integer>();
        int agroup = 0, bgroup = 0, cgroup = 0, count = 0;
        BombScore bombScore = getBombScore();
        for (Entry<DtzPlayer, HashMap<ScoreType, Integer>> entry : scoreList.entrySet()) {
            for (Entry<ScoreType, Integer> sc_entry : entry.getValue().entrySet()) {
                count = 0;
                switch (playType) {
                    case DtzzConstants.play_type_3POK:
                    case DtzzConstants.play_type_3PERSON_3POK:
                    case DtzzConstants.play_type_2PERSON_3POK:
                        if (sc_entry.getKey() == ScoreType.POINT_TZ_K) {
                            count = bombScore.getTongZi_K();
                        } else if (sc_entry.getKey() == ScoreType.POINT_TZ_A) {
                            count = bombScore.getTongZi_A();
                        } else if (sc_entry.getKey() == ScoreType.POINT_TZ_2) {
                            count = bombScore.getTongZi_2();
                        } else if (sc_entry.getKey() == ScoreType.POINT_BD) {
                            count = bombScore.getDiBomb();
                        } else if (sc_entry.getKey() == ScoreType.POINT_TZ_S || sc_entry.getKey() == ScoreType.POINT_TZ_B) {
                            count = bombScore.getTongZi_Wang();
                        }
                        break;
                    case DtzzConstants.play_type_4POK:
                    case DtzzConstants.play_type_3PERSON_4POK:
                    case DtzzConstants.play_type_2PERSON_4POK:
                        if (sc_entry.getKey() == ScoreType.POINT_JACKER_B) {
                            count = bombScore.getWang_Xi();
                        } else if (sc_entry.getKey() == ScoreType.POINT_JACKER_S) {
                            count = bombScore.getWang_Xi();
                        } else if (sc_entry.getKey() == ScoreType.POINT_XI) {
                            count = bombScore.getXi();
                        }
                        break;
                    case DtzzConstants.play_type_2PERSON_4Xi:
                    case DtzzConstants.play_type_3PERSON_4Xi:
                    case DtzzConstants.play_type_4PERSON_4Xi:
                        if (sc_entry.getKey() == ScoreType.POINT_TZ_K) {
                            count = bombScore.getTongZi_K();
                        } else if (sc_entry.getKey() == ScoreType.POINT_TZ_A) {
                            count = bombScore.getTongZi_A();
                        } else if (sc_entry.getKey() == ScoreType.POINT_TZ_2) {
                            count = bombScore.getTongZi_2();
                        } else if (sc_entry.getKey() == ScoreType.POINT_BD) {
                            count = bombScore.getDiBomb();
                        } else if (sc_entry.getKey() == ScoreType.POINT_Xi_5Q) {
                            count = bombScore.getXi_5Q();
                        } else if (sc_entry.getKey() == ScoreType.POINT_Xi_K) {
                            count = bombScore.getXi_K();
                        } else if (sc_entry.getKey() == ScoreType.POINT_Xi_A) {
                            count = bombScore.getXi_A();
                        } else if (sc_entry.getKey() == ScoreType.POINT_Xi_2) {
                            count = bombScore.getXi_2();
                        }
                        break;
                }
                switch (findGroupByPlayer(entry.getKey())) {
                    case 1:
                        agroup += count * sc_entry.getValue();
                        break;
                    case 2:
                        bgroup += count * sc_entry.getValue();
                        break;
                    case 3:
                        cgroup += count * sc_entry.getValue();
                        break;
                }
            }
        }
        groupCount.add(agroup);
        groupCount.add(bgroup);
        if (isThreePlayer()) {
            groupCount.add(cgroup);
        }
        return groupCount;
    }

    /**
     * 得到这个玩家一组的另外一个玩家 得不到就选择下一个玩家
     */
    private int getSeatInGroup(DtzPlayer player) {
        // 得到同组玩家的座位号
        int next = getPartnerSeat(player);
        // 如果同组玩家出完了就找下一个
        if (seatMap.containsKey(next) && !seatMap.get(next).getHandPais().isEmpty()) return next;
        // 得到顺序下一个玩家的座位号
        int nextSeat = getNextSeat(player.getSeat());
        int i = 0;
        while (i < 10) {
            if (seatMap.get(nextSeat).getHandPais().size() == 0) {
                nextSeat = (nextSeat + 1) > seatMap.size() ? 1 : nextSeat + 1;
            } else break;
            i++;
        }
        return nextSeat;
    }

    /**
     * 获得同组玩家的座位号
     */
    private int getPartnerSeat(DtzPlayer player) {
        List<DtzPlayer> groupPlayer = groupMap.get(getGroup(player.getSeat()));
        groupPlayer = new ArrayList<>(groupPlayer);
        groupPlayer.remove(player);
        if (groupPlayer.size() != 0) {
            DtzPlayer next = groupPlayer.get(0);
            return next.getSeat();
        }
        return -1;
    }

    /**
     * 下一个玩家,  计不计数是可选的
     */
    public int next(DtzPlayer player, boolean istrue) {
        int i = 0, seat = (player.getSeat() + 1) > seatMap.size() ? 1 : player.getSeat() + 1;
        while (i < 10) {
            if (seatMap.get(seat).getHandPais().size() == 0) {
                if (istrue) changePlayTimes(false, player);
                seat = (seat + 1) > seatMap.size() ? 1 : seat + 1;
            } else break;
            i++;
        }
        Pair<Boolean, DtzPlayer> pair = isDoneRound1();
        if (pair.getValue0()) {
            if (groupPlayer != null && pair.getValue1().equals(groupPlayer)) return groupPlayer.getSeat();
        }
        return seat;
    }

    /**
     * 这是一局的只用于记录各种分数的数量
     */
    private LinkedHashMap<DtzPlayer, HashMap<ScoreType, Integer>> bureau = new LinkedHashMap<>();

    /**
     * 记录每一局的分数
     */
    private void logScoreHistory(DtzPlayer player, List<Integer> pokers) {
        switch (playType) {
            case DtzzConstants.play_type_3POK:
            case DtzzConstants.play_type_3PERSON_3POK:
            case DtzzConstants.play_type_2PERSON_3POK:
                if (CardTypeToolDtz.isTongZi(pokers)) {
                    if (CardTypeToolDtz.tongZi_(pokers, 13)) { //K筒子
                        logScoreToBureauMap(player, ScoreType.POINT_TZ_K); //全局的
                    } else if (CardTypeToolDtz.tongZi_(pokers, 14)) { //a 筒子
                        logScoreToBureauMap(player, ScoreType.POINT_TZ_A); //全局的
                    } else if (CardTypeToolDtz.tongZi_(pokers, 15)) { //2筒子
                        logScoreToBureauMap(player, ScoreType.POINT_TZ_2); //全局的
                    } else if (CardTypeToolDtz.tongZi_(pokers, 16)) { //小王筒子
                        logScoreToBureauMap(player, ScoreType.POINT_TZ_S); //全局的
                    } else if (CardTypeToolDtz.tongZi_(pokers, 17)) { //大王筒子
                        logScoreToBureauMap(player, ScoreType.POINT_TZ_B); //全局的
                    }
                } else if (CardTypeToolDtz.isBobmDi(pokers)) { //是不是地炸
                    logScoreToBureauMap(player, ScoreType.POINT_BD); //全局的
                }
                break;
            case DtzzConstants.play_type_4POK:
            case DtzzConstants.play_type_3PERSON_4POK:
            case DtzzConstants.play_type_2PERSON_4POK:
                if (CardTypeToolDtz.isXi(pokers)) {
                    if (CardTypeToolDtz.xiTypeOf_(pokers, 17)) { //大王息
                        logScoreToBureauMap(player, ScoreType.POINT_JACKER_B); //全局的
                    } else if (CardTypeToolDtz.xiTypeOf_(pokers, 16)) { //小王息
                        logScoreToBureauMap(player, ScoreType.POINT_JACKER_S); //全局的
                    } else { //小sss子
                        logScoreToBureauMap(player, ScoreType.POINT_XI);
                    }
                }
                break;
            case DtzzConstants.play_type_2PERSON_4Xi:
            case DtzzConstants.play_type_3PERSON_4Xi:
            case DtzzConstants.play_type_4PERSON_4Xi:
                if (CardTypeToolDtz.isTongZi(pokers)) {
                    if (CardTypeToolDtz.tongZi_(pokers, 13)) { //K筒子
                        logScoreToBureauMap(player, ScoreType.POINT_TZ_K); //全局的
                    } else if (CardTypeToolDtz.tongZi_(pokers, 14)) { //a 筒子
                        logScoreToBureauMap(player, ScoreType.POINT_TZ_A); //全局的
                    } else if (CardTypeToolDtz.tongZi_(pokers, 15)) { //2筒子
                        logScoreToBureauMap(player, ScoreType.POINT_TZ_2); //全局的
                    }
                } else if (CardTypeToolDtz.isBobmDi(pokers)) { //是不是地炸
                    logScoreToBureauMap(player, ScoreType.POINT_BD); //全局的
                }
                if (CardTypeToolDtz.isXi(pokers)) {
                    if (CardTypeToolDtz.xiTypeOf_(pokers, 13)) { //K喜
                        logScoreToBureauMap(player, ScoreType.POINT_Xi_K);
                    } else if (CardTypeToolDtz.xiTypeOf_(pokers, 14)) { //A喜
                        logScoreToBureauMap(player, ScoreType.POINT_Xi_A);
                    } else if (CardTypeToolDtz.xiTypeOf_(pokers, 15)) { //2喜
                        logScoreToBureauMap(player, ScoreType.POINT_Xi_2);
                    } else { //5Q喜
                        logScoreToBureauMap(player, ScoreType.POINT_Xi_5Q);
                    }
                }
                break;
        }
        pokers = CardToolDtz.toValueList(pokers);
        for (int val : pokers) {
            if (val == 5) {
                logScoreToBureauMap(player, ScoreType.POINT_5);
            } else if (val == 10) {
                logScoreToBureauMap(player, ScoreType.POITN_10);
            } else if (val == 13) {
                logScoreToBureauMap(player, ScoreType.POINT_K);
            }
        }
    }

    /**
     * 用于记录一局的分数
     */
    private void logScoreToBureauMap(DtzPlayer player, ScoreType type) {
        if (!bureau.containsKey(player)) {
            HashMap<ScoreType, Integer> score = new HashMap<>();
            score.put(type, 1);
            bureau.put(player, score);
        } else {
            HashMap<ScoreType, Integer> score = bureau.get(player);
            if (score != null) {
                if (score.get(type) == null) {
                    score.put(type, 1);
                } else {
                    score.put(type, score.get(type) + 1);
                }
            } else {
                score = new HashMap<>();
                score.put(type, 1);
                bureau.put(player, score);
            }
        }
    }

    /**
     * 清理掉一局的分数记录
     */
    private void clearBureauScoreLog() {
        bureauTemp.clear();
        bureau.clear();
        changeExtend();
    }

    //当前轮要加分的玩家
    public DtzPlayer scorePlayerTemp;
    //同组对面的玩家
    public DtzPlayer groupPlayer;
    //当一方已经出完牌后，此属性为true，表示将出牌权移交给同组的对面玩家
    public boolean useNowDis = false;

    @Override
    public CreateTableRes buildCreateTableRes(long userId, boolean isrecover,
                                              boolean isLastReady) {
        return this.buildCreateTableRes(userId, isrecover);
    }

    @Override
    public int calcPlayerCount(int playerCount) {
        return playerCount > 0 ? playerCount : getMaxPlayerCount();
    }

    /**
     * 设置本轮第一次出牌的牌型及连续数（主要针对三顺的判断）
     */
    private void setFirstCardType(CardTypeDtz fromType, int fromCount) {
        firstCardTypePair = Pair.with(fromType, fromCount);
    }

    /**
     * 获取设置本轮第一次出牌的牌型及连续数（主要针对三顺的判断）
     */
    public Pair<CardTypeDtz, Integer> getFirstCardType() {
        return firstCardTypePair;
    }


    /**
     * 初始化首牌牌型
     */
    public void initFirstCardType(List<Integer> cards, DtzTable table) {
        // 开启新一轮出牌
        //先清理存储连续数量
        setFirstCardType(CardTypeDtz.CARD_0, 0);
        //如果是飞机或者三顺，存储连续数量
        CardTypeDtz fromType = CardTypeToolDtz.toPokerType(cards, table);
        if (fromType == CardTypeDtz.CARD_7 || fromType == CardTypeDtz.CARD_8) {
            //先得到所有出现次数为3以上的牌
            HashMap<Integer, Integer> map = new HashMap<>();
            for (int card : CardToolDtz.toValueList(cards)) {
                if (!map.containsKey(card)) map.put(card, 1);
                else map.put(card, map.get(card) + 1);
            }
            List<Integer> vList = new ArrayList<Integer>();
            for (Entry<Integer, Integer> entry : map.entrySet()) {
                if (entry.getValue() >= 3) {
                    vList.add(entry.getKey());
                }
            }
            //排序后判断得到最大的连续飞机
            Collections.sort(vList, CardTypeToolDtz.comparator);
            //得到连续最大牌、连续次数列表
            Map<Integer, Integer> countList = new HashMap<>();
            int stV = vList.get(0);
            int count = 1;
            for (int i = 1; i < vList.size(); i++) {
                if (vList.get(i) == (stV + 1)) {
                    count++;
                    if (i == vList.size() - 1) {
                        //如果是最后一个满足条件，需要加入列表
                        countList.put(vList.get(i), count);
                    }
                    stV = vList.get(i);
                } else {
                    countList.put(vList.get(i - 1), count);
                    count = 1;
                    stV = vList.get(i);
                }
            }
            //得到连续的最大值
            int fromCount = 0;
            for (Entry<Integer, Integer> entry : countList.entrySet()) {
                if (entry.getValue() > fromCount) {
                    //如果有，再判断牌的数量是否符合
                    fromCount = entry.getValue();
                }
            }
            //存入牌桌
            table.setFirstCardType(fromType, fromCount);
        }
    }


    public int getScore_max() {
        return score_max;
    }

    @Override
    public int loadOverValue() {
        return score_max;
    }

    @Override
    public int loadOverCurrentValue() {
        int temp = 0;
        for (Map.Entry<Integer, Integer> kv : groupScore.entrySet()) {
            int tmp = kv.getValue();
            if (tmp > temp) {
                temp = tmp;
            }
        }
        return temp;
    }

    /**
     * 发送玩家状态信息
     */
    @Override
    public void sendPlayerStatusMsg() {
        List<String> readyList = new ArrayList<>();
        //牌局的第一局开始前发送各玩家的选座情况和准备情况
        if (dtzRound == 1 && !checkChooseDone()) {
            DtzPlayer play;
            for (Entry<Long, DtzPlayer> p : playerMap.entrySet()) {
                play = p.getValue();
                if (play == null) continue;
                ReadyPlayer rp = new ReadyPlayer();
                rp.setUserId(p.getKey());
                rp.setSeat(play.getSeat());
                if (play.getState() == player_state.ready) {
                    rp.setReady(1);
                }
                readyList.add(JSONObject.toJSONString(rp));
            }
            ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.RES_READYMSG, readyList);
            //发送消息
            if (readyList.size() > 0) {
                for (Entry<Long, DtzPlayer> p : playerMap.entrySet()) {
                    play = p.getValue();
                    if (play == null || play.getIsOnline() == 0) continue;
                    play.writeSocket(com.build());
                }
            }
        }
    }

    public void setMasterFP(boolean b) {
        this.masterFP = b;
        changeExtend();
    }

    /**
     * 根据座位号得到分组
     */
    public int getGroup(int seat) {
        int group = -1;
        if (isThreePlayer() || isTwoPlayer()) {
            group = seat;
        } else {
            if (seat == 1 || seat == 3) {
                group = 1;
            } else if (seat == 2 || seat == 4) {
                group = 2;
            }
        }
        return group;
    }

    // 是否四人打筒子
    public boolean isFourPlayer() {
        return getMaxPlayerCount() == 4;
    }

    // 是否三人打筒子
    public boolean isThreePlayer() {
        return getMaxPlayerCount() == 3;
    }

    // 是否两人打筒子
    public boolean isTwoPlayer() {
        return getMaxPlayerCount() == 2;
    }

    // 是否三副牌
    public boolean isThreePai() {
        int play = getPlayType();
        return (play == DtzzConstants.play_type_3POK || play == DtzzConstants.play_type_3PERSON_3POK || play == DtzzConstants.play_type_2PERSON_3POK);
    }

    // 是否四副牌
    public boolean isFourPai() {
        int play = getPlayType();
        return (play == DtzzConstants.play_type_4POK
                || play == DtzzConstants.play_type_3PERSON_4POK
                || play == DtzzConstants.play_type_2PERSON_4POK
                || DtzzConstants.isKlSiXi(play)
        );
    }

    //是否是快乐四喜
    public boolean isKlSX() {
        return DtzzConstants.isKlSiXi(getPlayType());
    }

    /**
     * 发送记牌器消息
     *
     * @param player
     */
    public void sendCardMarker(DtzPlayer player) {
        ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.RES_CARD_MARKER, cardMarkerToJSON());
        player.writeSocket(com.build());
    }

    /**
     * 自动过牌
     *
     * @param player
     */
    public void giveup(DtzPlayer player, int len) {
        StringBuffer params = new StringBuffer();
        int param[] = new int[len];
        //判断是否该局结束
        if (this.isOver()) {
            //四人玩法才有
            if (this.isFourPlayer()) {
                //得到两人都出完牌的一组,需要进行结算
                int wingroup = this.getWinGroup();
                //设置最终结算的玩家位置
                if (this.settSeat == 0) {
                    if (wingroup == 1) {
                        //A组结束，设置B组某位玩家为结算位置
                        if (player.getSeat() == 2) this.settSeat = 4;
                        else if (player.getSeat() == 4) this.settSeat = 2;
                        else return;//发生错误,角色的位置不正确，前面有打印，直接返回
                    } else if (wingroup == 2) {
                        //B组结束，设置A组某位玩家为结算位置
                        if (player.getSeat() == 1) this.settSeat = 3;
                        else if (player.getSeat() == 3) this.settSeat = 1;
                        else return;//发生错误,角色的位置不正确，前面有打印，直接返回
                    }
                }
                //疑问代码，当牌桌设置的结算位置没有手牌时，切到同组的对面玩家
                if (this.getSeatMap().get(this.settSeat).getHandPais().size() == 0) {
                    if (wingroup == 1) {
                        if (this.settSeat == 2) this.settSeat = 4;
                        else if (this.settSeat == 4) this.settSeat = 2;
                        else return;//发生错误,角色的位置不正确，前面有打印，直接返回
                    } else if (wingroup == 2) {
                        if (this.settSeat == 1) this.settSeat = 3;
                        else if (this.settSeat == 3) this.settSeat = 1;
                        else return;//发生错误,角色的位置不正确，前面有打印，直接返回
                    }
                    DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "玩家不要===异常===切到同组的对面玩家", "计算位置：" + this.settSeat);
                }
            }
            params.append("客户端参数：[");
            //for(int i:param) params.append(i).append(",");
            params.append("]");
            params.append(",玩家位置：").append(player.getSeat());
            params.append(",牌局结算状态：").append(this.jiesuan);
            params.append(",当前小局 : ").append(this.dtzRound);
            params.append(",是否不是当局第一人出不要 : ").append(this.masterFP);
            params.append(",当局结束状态：").append(this.isOver());
            //params.append(",双方已经都出完牌的组号：").append(this.getWinGroup());
            params.append(",结算玩家位置：").append(this.settSeat);
//			DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "玩家不要，定位结算位置后",params.toString());
            //发送玩家不要的消息到客户端
            if (this.settSeat != 0) {
                //如果结算位置是玩家所在位置，进行结算
                if (player.getSeat() == this.settSeat) {
                    //给客户端各个玩家发送不要的消息
                    this.sendNotletInfo(player);
                    addPlayLog(player.getSeat() + "_");
                    //设置本轮赢家
                    DtzPlayer winPlayer = player;
                    if (this.scorePlayerTemp != null) {
                        //此处scorePlayerTemp肯定不会为空，结束时一定有积分赢家
                        winPlayer = this.scorePlayerTemp;
                        this.scorePlayerTemp = null;
//						DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "玩家不要，双关后设置本轮积分赢家","积分赢家："+winPlayer.getUserId()+"["+winPlayer.getName()+"]");
                    } else {
                        //scorePlayerTemp为空，发生了异常
                        DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "玩家不要，双关后设置本轮积分赢家异常", "积分赢家：" + winPlayer.getUserId() + "[" + winPlayer.getName() + "]");
                    }
                    //增加分数到本轮赢者
                    winPlayer.setPoint(winPlayer.getPoint() + this.getScore());
                    //增加地炸筒子分到本轮赢者
                    winPlayer.setRoundScore(winPlayer.getRoundScore() + this.getTzScore());
                    LogUtil.msg("双关后玩家:" + winPlayer + " score : " + winPlayer.getPoint() + " - " + winPlayer.getRoundScore());
                    this.changeDisCardRound(1); //增加轮数
                    int group = this.findGroupByPlayer(winPlayer);//
                    this.addScoreBuGroup(group, this.getScore()); //为这一组加分
                    this.clearRoundScore(); //清理掉一局的分数
                    this.recordCardAll(this.getCardTemp(), winPlayer);  //筒子放起来
                    List<Integer> groupCount = this.getAllTzOrXiGroupScore();
                    int score_c = this.getRoundFragmentCardSocre();
                    for (Player pp : this.getPlayerMap().values()) {
                        pp.writeComMessage(WebSocketMsgType.RES_DISCARD_RUNS, (int) winPlayer.getUserId(), groupCount, score_c, 0);
                    }
                    this.clearCardTemp(); //清掉牌的缓存
                    LogUtil.msg("table" + this.getId() + " 双关后在 要不起的状态， 而且  打完之后 将进入 结算.");
                    this.delayedSettlement(player.getSeat());
                    return;
                }
            } else {
                this.sendNotletInfo(player);
                addPlayLog(player.getSeat() + "_");
                //得到要结算加积分的玩家
                DtzPlayer winPlayer = player;
                if (this.scorePlayerTemp != null) {
//					DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "玩家不要，计算加分玩家","原加分玩家是 "+winPlayer.getUserId()+"["+winPlayer.getName()+"],现加分玩家是："+this.scorePlayerTemp.getUserId()+"["+this.scorePlayerTemp.getName()+"]");
                    winPlayer = this.scorePlayerTemp;
                    this.scorePlayerTemp = null;
                }
                //设置玩家当前轮加的牌面分，不包含筒子、地炸、囍的分
                winPlayer.setPoint(winPlayer.getPoint() + this.getScore());
                //设置玩家当前轮加的筒子、地炸、囍的分
                winPlayer.setRoundScore(winPlayer.getRoundScore() + this.getTzScore());
//				DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "玩家不要，进入本局结算中的本轮结算胜出的玩家","胜出玩家是:" + winPlayer.getUserId()+"["+winPlayer.getName()+"]"+",牌面分："+winPlayer.getPoint()+",筒子分："+winPlayer.getRoundScore());
                //增加轮数
                this.changeDisCardRound(1);
                //得到赢的那组
                int group = this.findGroupByPlayer(winPlayer);//
                //为这一组加该轮积分
                this.addScoreBuGroup(group, this.getScore());
                //清理牌桌该轮的分数
                this.clearRoundScore();
                //筒子牌放起来
                this.recordCardAll(this.getCardTemp(), winPlayer);
                //得到全局筒子的总分 A组， B组
                List<Integer> groupCount = this.getAllTzOrXiGroupScore();
                //得到该轮5,10，k的所有积分和
                int score_c = this.getRoundFragmentCardSocre();
                //推送给玩家
                for (Player pp : this.getPlayerMap().values()) {
                    pp.writeComMessage(WebSocketMsgType.RES_DISCARD_RUNS, (int) winPlayer.getUserId(), groupCount, score_c, 0);
                }
                //清掉牌的缓存
                this.clearCardTemp();
                //本轮结算结束，进入本局结算
//				DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "玩家不要，进入本局结算","胜出玩家是:" + winPlayer.getUserId()+"["+winPlayer.getName()+"]"+",牌面分："+winPlayer.getPoint()+",筒子分："+winPlayer.getRoundScore());
                //开始本局结算
                this.delayedSettlement(player.getSeat());
                return;
            }
        }
        //牌局未结束，继续运行

        //得到此轮其他人是否都不要
        Pair<Boolean, DtzPlayer> pair = this.isDoneRound1();
        //其他人都不要，这段有问题，不会出现这种情况
        if (pair.getValue0()) {
            //DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "玩家不要===异常===其他三家都不要，最后出牌人是自己的",params.toString());
            //清空当前牌桌上此轮出的牌
            this.getNowDisCardIds().clear();
            if (pair.getValue1().equals(player)) {
                //如果此时最后出牌的人是自己，这是一个异常情况，打印后返回
                DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "玩家不要===异常二次===其他三家都不要，最后出牌人是自己的", params.toString());
                return;
            }
        }
        //给自己客户端推送不要的消息及传过来的参数
        player.writeComMessage(WebSocketMsgType.REQ_COM_GIVEUP, param);
        //计算下一个出牌的玩家
        synchronized (this) {
            //设置当前存储玩家要不起玩家数量对象
            this.changePlayTimes(false, player);
            //要不起逻辑处理
            this.notLet(player);
        }
//        DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "玩家不要，计算下一个出牌的玩家","玩家要不起 下一个玩家的位置是 " + this.getNowDisCardSeat());
//    				LogUtil.msg("table:" + this.getId() + "位置:" + player.getSeat() + "的玩家要不起 下一个玩家的位置是 " + this.getNowDisCardSeat());
        //判定逻辑,看是否本轮已经结束
        pair = this.isDoneRound1();
        if (pair.getValue0()) {
            //没有人要的起了，本轮结束，开始给玩家加分
            //清空本轮牌面的牌
            this.getNowDisCardIds().clear();
            //得到加分玩家
            DtzPlayer winPlayer = pair.getValue1();
            if (this.scorePlayerTemp != null) {
//                DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "玩家不要，计算加分玩家","原加分玩家是 "+winPlayer.getUserId()+"["+winPlayer.getName()+"],现加分玩家是："+this.scorePlayerTemp.getUserId()+"["+this.scorePlayerTemp.getName()+"]");
                winPlayer = this.scorePlayerTemp;
                this.scorePlayerTemp = null;
            }
            //给玩家加分
            //设置玩家当前轮加的牌面分，不包含筒子、地炸、囍的分
            winPlayer.setPoint(winPlayer.getPoint() + this.getScore()); //加分数
            //设置玩家当前轮加的筒子、地炸、囍的分
            winPlayer.setRoundScore(winPlayer.getRoundScore() + this.getTzScore());
//            DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "玩家不要，计算本轮胜出的玩家","胜出玩家是:" + winPlayer.getUserId()+"["+winPlayer.getName()+"]"+",牌面分："+winPlayer.getPoint()+",筒子分："+winPlayer.getRoundScore());
//    					LogUtil.msg("玩家:" + winPlayer + " score : " + winPlayer.getPoint() + " - " + winPlayer.getRoundScore());
            //给本轮胜出组进行加分
            int group = this.findGroupByPlayer(winPlayer);
            //为这一组加牌面分
            this.addScoreBuGroup(group, this.getScore());
            //清理掉该轮牌桌的牌面分和筒子分
            this.clearRoundScore();
            //记录本轮出牌的情况
            this.recordCardAll(this.getCardTemp(), winPlayer);  //筒子放起来
            //得到全局各组的筒子分总和
            List<Integer> groupCount = this.getAllTzOrXiGroupScore();
            //得到该轮5,10，k的所有积分和
            int score_c = this.getRoundFragmentCardSocre();
            //推送给玩家
            for (Player pp : this.getPlayerMap().values()) {
                pp.writeComMessage(WebSocketMsgType.RES_DISCARD_RUNS, (int) winPlayer.getUserId(), groupCount, score_c);
            }
            //清理本轮牌的缓存
            this.clearCardTemp(); //清掉牌的缓存
        }
    }

    public void playCommand(DtzPlayer player, List<Integer> cards, CardTypeDtz cardType) {
        synchronized (this) {
            //判断当前是不是自己出牌
            if (this.getNowDisCardSeat() != 0 && this.getNowDisCardSeat() != player.getSeat()) return;
            //判断牌局是否结算，防止多次点击
            if (this.jiesuan) return;
            //检查出牌人的手牌
            if (player.getHandPais().size() == 0) return;
            //检查牌桌的状态
            if (this.getState() == table_state.ready) return;

            if (!CardTypeToolDtz.cheakOutcard(player.getHandPais(), cards)) return;

            //得到当前牌桌上一玩家出的牌
            List<Integer> disCardIds = this.getNowDisCardIds();

            //打印参数
            StringBuilder params = new StringBuilder();
            params.append("玩家出牌为：");
            params.append(JacksonUtil.writeValueAsString(cards));
            params.append(",玩家位置：").append(player.getSeat());
            params.append(",牌局结算状态：").append(this.jiesuan);
            params.append(",当前小局 : ").append(this.dtzRound);
            params.append(",是否不是当局第一人出不要 : ").append(this.masterFP);
            params.append(",当局结束状态：").append(this.isOver());
            //params.append(",双方已经都出完牌的组号：").append(table.getWinGroup());
            params.append(",结算玩家位置：").append(this.settSeat);
            params.append(",牌面应该出牌位置：").append(this.getNowDisCardSeat());
            params.append(",上家出牌为：");
            params.append(JacksonUtil.writeValueAsString(disCardIds));
            params.append(",该轮首牌牌型：[").append(this.getFirstCardType().getValue0()).append(",").append(this.getFirstCardType().getValue1()).append("]");
            //DtzSendLog.sendCardLog(table.getId(), player.getUserId(), player.getName(), "玩家出牌",params.toString());

            //清掉对面玩家
            this.groupPlayer = null;
            //自己出牌了,设置false，表示不将出牌权移交给同组的对面玩家
            this.useNowDis = false;
            //判断要出的牌的牌型
            if (this.isOver()) {
                //进入本局结算，最后一次出牌
                if (CardTypeToolDtz.comparePoker(disCardIds, cards, this) != DtzConstant.OWN_WIN) {
                    DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "玩家出牌===异常===进入结算出牌未大于上家", params.toString());
                    return;
                }
                //玩家出牌
                PlayCardRes.Builder res = PlayCardRes.newBuilder();
                res.addAllCardIds(cards);
                res.setCardType(0);
                res.setUserId(player.getUserId() + "");
                res.setSeat(player.getSeat());
                res.setIsPlay(1);
                res.setIsFirstOut(0);
                res.setNextSeat(0);
                this.broadMsg(res.build());
                this.addPlayLog(player.getSeat() + "_" + StringUtil.implode(cards));
                //增加出牌到出牌列表，删除对应玩家手牌中出的牌
                player.addOutPais(cards);//可以清除最后出的牌

                Pair<Integer, Integer> score = CardTypeToolDtz.toScore(cards, this); //对他出的牌估值
                this.addScore(score.getValue0());  //这是加普通分数
                this.addTzScore(score.getValue1()); //这是加筒子分数
                this.offerdCard(cards, player);

                //给玩家加牌面基础分
                player.setPoint(player.getPoint() + this.getScore());
                //给玩家加筒子分
                player.setRoundScore(player.getRoundScore() + this.getTzScore());
                //增加轮数
                this.changeDisCardRound(1);
                //得到组号
                int group = this.findGroupByPlayer(player);
                //为这一组加分
                this.addScoreBuGroup(group, this.getScore());
                //清理掉一局的分数
                this.clearRoundScore();
                //把筒子放起来
                this.recordCardAll(this.getCardTemp(), player);
                List<Integer> groupCount = this.getAllTzOrXiGroupScore();
                //将5,10，K转化为分数
                int score_c = this.getRoundFragmentCardSocre();
                //给所有玩家返回信息
                for (Player pp : this.getPlayerMap().values()) {
                    pp.writeComMessage(WebSocketMsgType.RES_DISCARD_RUNS, (int) player.getUserId(), groupCount, score_c, 0);
                }
                //清掉牌的缓存
                this.clearCardTemp();
                //开始进行大局结算
                this.delayedSettlement(player.getSeat());
                return;
            }

            //如果请求的牌型不为0
            if (cardType.getType() != 0) {
                //检测人数是否满了
                if (this.getSeatMap().size() < this.getMaxPlayerCount()) {
                    DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "玩家出牌===异常===人数未满", "Seat:" + this.getSeatMap().size());
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_6));
                    return;
                }
                // 自己要牌
                if (this.getDisCardSeat() != player.getSeat()) { //轮到了自己
                    // 上一张的牌不是自己出的,如果前面出牌人也是空
                    if (disCardIds != null && !disCardIds.isEmpty()) {
                        // 如果出的是单张下一家出牌只剩下一张了只能出最大的
                        if (CardTypeToolDtz.comparePoker(disCardIds, cards, this) != DtzConstant.OWN_WIN) {
                            DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "玩家出牌===异常===进入结算出牌未大于上家", params.toString());
                            return;
                        }
                    } else {
                        //上轮出牌结束，开启新一轮出牌
                        this.initFirstCardType(cards, this);
                    }
                    if (this.getMasterId() == player.getUserId() || (this.getSeatMap().get(this.lastWin) != null && this.getSeatMap().get(this.lastWin).getUserId() == player.getUserId())) {
                        //如果当前玩家为房主并且为上轮赢家，设置下面可以出不要
                    }
                    this.setMasterFP(true);//随机出牌需要拿出来
                } else {
                    //出牌人为自己，开启新一轮出牌
                    this.initFirstCardType(cards, this);
                    this.clearIsNotLet();
                }
            } else {
                String param = "Empty:" + disCardIds.isEmpty() + ",NowDisCardSeat:" + this.getNowDisCardSeat() + ",player.getSeat:" + player.getSeat() + ",table.lastWin:" + this.lastWin;
                DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "玩家出牌===异常===CardType为0 ", param);
                // 是否真的要不起
                // 桌子的没有牌
                if (disCardIds.isEmpty()) return;
                // 桌子上的牌是自己出的
                if (this.getNowDisCardSeat() == player.getSeat()) return;
                if (CardTypeToolDtz.comparePoker(disCardIds, cards, this) != DtzConstant.OWN_WIN) {
                    DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "玩家出牌===异常===CardType为0出牌未大于上家", params.toString());
                    return;
                }
                if (this.getMasterId() == player.getUserId() || (this.getSeatMap().get(this.lastWin) != null && this.getSeatMap().get(this.lastWin).getUserId() == player.getUserId())) {
                    DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "玩家出牌===异常===房主第一次出牌", "win:" + this.lastWin);
                    this.setMasterFP(true);
                }
            }
            this.playCommandDtz(player, cards, cardType);
        }
    }

    @Override
    public void calcDataStatistics2() {
        //俱乐部房间 单大局大赢家、单大局大负豪、总小局数、单大局赢最多、单大局输最多 数据统计
        if (isGroupRoom()) {
            String groupId = loadGroupId();
            int maxPoint = 0;
            int minPoint = 0;
            Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            //Long dataDate, String dataCode, String userId, String gameType, String dataType, int dataValue
            calcDataStatistics3(groupId);

            for (DtzPlayer player : playerMap.values()) {
                //总小局数
                DataStatistics dataStatistics1 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "xjsCount", playedBureau);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics1, 3);
                //总大局数
                DataStatistics dataStatistics5 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "djsCount", 1);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5, 3);
                //总积分
                DataStatistics dataStatistics6 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "zjfCount", player.getWinLossPoint());
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics6, 3);
                if (player.getWinLossPoint() > 0) {
                    if (player.getWinLossPoint() > maxPoint) {
                        maxPoint = player.getWinLossPoint();
                    }
                    //单大局赢最多
                    DataStatistics dataStatistics2 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "winMaxScore", player.getWinLossPoint());
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics2, 4);
                } else if (player.getWinLossPoint() < 0) {
                    if (player.getWinLossPoint() < minPoint) {
                        minPoint = player.getWinLossPoint();
                    }
                    //单大局输最多
                    DataStatistics dataStatistics3 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "loseMaxScore", player.getWinLossPoint());
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics3, 5);
                }
            }

            for (DtzPlayer player : playerMap.values()) {
                if (maxPoint > 0 && maxPoint == player.getWinLossPoint()) {
                    //单大局大赢家
                    DataStatistics dataStatistics4 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "dyjCount", 1);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4, 1);
                } else if (minPoint < 0 && minPoint == player.getWinLossPoint()) {
                    //单大局大负豪
                    DataStatistics dataStatistics5 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "dfhCount", 1);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5, 2);
                }
            }
        }
    }

    @Override
    public boolean isDissSendAccountsMsg() {
        return (playBureau > 1 || (playBureau == 1 && (getState() == table_state.play || getState() == table_state.over && !jiesuan)));
    }

    @Override
    public boolean isFirstBureauOverConsume() {
        return false;
    }

    @Override
    public boolean isGroupRoomReturnConsume() {
        return playBureau <= 1 && getState() == table_state.ready;
    }

    @Override
    public boolean isDaikaiRoomReturnConsume() {
        return super.isDaikaiRoomReturnConsume() && getState() == table_state.ready;
    }

    @Override
    public long getDissTimeout() {
        if (playBureau == 1) {
            return 5 * 60 * 1000;
        } else {
            return super.getDissTimeout();
        }
    }

    @Override
    public int loadAgreeCount() {
        return getMaxPlayerCount();
    }

    @Override
    public String loadGameCode() {
        return GAME_CODE;
    }

    @Override
    public int loadPayConfig() {
        return loadPayConfig(payType);
    }

    @Override
    public int loadPayConfig(int payType) {
        int serverPayType = -1;
        switch (payType) {
            case PayConfigUtil.PayType_Client_AA:
                serverPayType = PayConfigUtil.PayType_Server_AA;
                break;
            case PayConfigUtil.PayType_Client_TableMaster:
                serverPayType = PayConfigUtil.PayType_Server_TableMaster;
                break;
            case PayConfigUtil.PayType_Client_GroupMaster:
                serverPayType = PayConfigUtil.PayType_Server_GroupMaster;
                break;
            case PayConfigUtil.PayType_Client_AA_Gold:
                serverPayType = PayConfigUtil.PayType_Server_AA_Gold;
                break;
        }
        if (serverPayType == -1) {
            return -1;
        }
        return PayConfigUtil.get(playType, totalBureau, getMaxPlayerCount(), serverPayType, getScore_max());
    }

    @Override
    public boolean allowChooseSeat() {
        return max_player_count == 4;
    }

    public static final List<Integer> wanfaList = Arrays.asList(
            GameUtil.play_type_3POK,
            GameUtil.play_type_4POK,
            GameUtil.play_type_3PERSON_3POK,
            GameUtil.play_type_3PERSON_4POK,
            GameUtil.play_type_2PERSON_3POK,
            GameUtil.play_type_2PERSON_4POK,
            GameUtil.play_type_2PERSON_4Xi,
            GameUtil.play_type_3PERSON_4Xi,
            GameUtil.play_type_4PERSON_4Xi);

    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
        for (Integer integer : wanfaList) {
            TableManager.wanfaTableTypesPut(integer, cls);
        }
        TableManager.msgPlayerTypes.put(WebSocketMsgType.REQ_RECORD, DtzPlayer.class);
    }

    @Override
    public boolean isCreditTable(List<Integer> params) {
        return params != null && params.size() > 14 && StringUtil.getIntValue(params, 14, 0) == 1;
    }

    /**
     * 计算分是否够除
     */
    @Override
    public void calcNegativeCredit() {
        if (!isCreditTable()) {
            return;
        }
        if (!isFourPlayer()) {
            super.calcNegativeCredit();
            return;
        }
        if (canNegativeCredit()) {
            return;
        }
        initGroupUser();
        calcWinCreditLimit();

        String groupId = loadGroupId();
        int totalHave = 0;
        for (Player player : getSeatMap().values()) {
            if (player.getWinLoseCredit() < 0) {
                GroupUser gu = getGroupUser(player.getUserId());
                long haveCredit = gu.getCredit();
                if (haveCredit <= 0) {
                    totalHave += 0;
                    player.setWinLoseCredit(0);
                } else if (haveCredit + player.getWinLoseCredit() < 0) {
                    totalHave += haveCredit;
                    player.setWinLoseCredit(-haveCredit);
                } else {
                    totalHave += -player.getWinLoseCredit();
                }
            }
        }
        int credit = totalHave / 2;
        int leftCredit = totalHave % 2;
        for (Player player : getSeatMap().values()) {
            if (player.getWinLoseCredit() > 0) {
                player.setWinLoseCredit(credit);
                if (leftCredit > 0) {
                    player.setWinLoseCredit(player.getWinLoseCredit() + leftCredit);
                    leftCredit = 0;
                }
            }
        }
    }

    protected boolean consume() {
        boolean requriedCard = false;

        // 如果是第一句扣掉房卡
        if (playBureau == 1 && consumeCards() && !isGoldRoom() && !isCompetitionRoom()) {
            if(payType == PayConfigUtil.PayType_Client_AA
                    || payType == PayConfigUtil.PayType_Client_TableMaster
                    || payType == PayConfigUtil.PayType_Client_GroupMaster
            ) {
                int needCards = 0;
                if(isGroupRoom()){
                    needCards = loadGroupRoomPay();
                }else{
                    needCards = loadPayConfig();
                }
                if (needCards > 0) {
                    CardSourceType sourceType = getCardSourceType(payType);
                    if (isAAConsume() || isAAConsume0()) {
                        if (needCards < 0) {
                            needCards = loadPayConfig();
                        }
                        if (needCards <= 0) {
                            return requriedCard;
                        }

                        for (Player player : getPlayerMap().values()) {
                            if (!GameConfigUtil.freeGame(playType, player.getUserId())) {
                                // 如果是AA开房每人扣一张
                                if (PayConfigUtil.loadPayResourceType(playType) == UserResourceType.TILI) {
                                    player.changeTili(-needCards, true);
                                } else {
                                    player.changeCards(0, -needCards, true, playType, sourceType);
                                    player.saveBaseInfo();
                                    calcActivity(ActivityConstant.activity_fudai, player, needCards);
                                }

                                requriedCard = true;
                            }
                        }
                    } else {
                        if (NumberUtils.isDigits(getServerKey()) || isGroupRoom()) {
                            LogUtil.msgLog.info("group master pay:group table keyId=" + serverKey + ",tableId=" + getId());
                            changeConsume(needCards);
                        } else {
                            if (needCards < 0) {
                                needCards = loadPayConfig();
                            }
                            if (needCards <= 0) {
                                return requriedCard;
                            }
                            Player player = getPlayerMap().get(masterId);
                            if (player != null && !GameConfigUtil.freeGame(playType, player.getUserId())) {
                                if (PayConfigUtil.loadPayResourceType(playType) == UserResourceType.TILI) {
                                    player.changeTili(-needCards, true);
                                } else {
                                    player.changeCards(0, -needCards, true, playType, sourceType);
                                    player.saveBaseInfo();
                                    calcActivity(ActivityConstant.activity_fudai, player, needCards);
                                }
                            } else {
                                if (player == null) {
                                    RegInfo user = UserDao.getInstance().selectUserByUserId(masterId);
                                    if (user != null && !GameConfigUtil.freeGame(playType, user.getUserId())) {
                                        try {
                                            player = ObjectUtil.newInstance(getPlayerClass());
                                            player.loadFromDB(user);
                                            if (PayConfigUtil.loadPayResourceType(playType) == UserResourceType.TILI) {
                                                player.changeTili(-needCards, true);
                                            } else {
                                                player.changeCards(0, -needCards, true, playType, sourceType);
                                                player.saveBaseInfo();
                                                calcActivity(ActivityConstant.activity_fudai, player, needCards);
                                            }
                                        } catch (Exception e) {
                                            LogUtil.errorLog.error("consume err-->Exception:" + e.getMessage(), e);
                                        }
                                    } else {
                                        LogUtil.e("consume err-->tableId:" + id + ",masterId:" + masterId);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (playBureau == 1 && (NumberUtils.isDigits(getServerKey()) || isGroupRoom() || isDaikaiTable())) {
            int needCards = loadGroupRoomPay();
            if (needCards == 0) {
            } else {
                changeConsume(needCards);
            }
        }
        return requriedCard;
    }

    public long saveUserGroupPlaylog() {
        if (!needSaveUserGroupPlayLog()) {
            return 0;
        }
        UserGroupPlaylog userGroupLog = new UserGroupPlaylog();
        userGroupLog.setTableid(id);
        userGroupLog.setUserid(creatorId);
        userGroupLog.setCount(playBureau);
        String players = "";
        String score = "";
        String diFenScore = "";
        for (DtzPlayer player : getSeatMap().values()) {
            players += player.getUserId() + ",";
            score += player.getWinLossPoint() + ",";
            diFenScore += player.getWinLossPoint() + ",";
        }
        userGroupLog.setPlayers(players.length() > 0 ? players.substring(0, players.length() - 1) : "");
        userGroupLog.setScore(score.length() > 0 ? score.substring(0, score.length() - 1) : "");
        userGroupLog.setDiFenScore(diFenScore.length() > 0 ? diFenScore.substring(0, diFenScore.length() - 1) : "");
        userGroupLog.setDiFen("");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        userGroupLog.setCreattime(sdf.format(createTime));
        userGroupLog.setOvertime(sdf.format(new Date()));
        userGroupLog.setPlayercount(getMaxPlayerCount());
        userGroupLog.setGroupid(Long.parseLong(loadGroupId()));
        userGroupLog.setGamename(getGameName());
        userGroupLog.setTotalCount(totalBureau);
        return TableLogDao.getInstance().saveGroupPlayLog(userGroupLog);
    }

    @Override
    public String getGameName() {
        return "打筒子";
    }

    @Override
    public int getLogGroupTableBureau() {
        return score_max;
    }


    public void setDataForPlayLogTable(PlayLogTable logTable) {
        StringJoiner players = new StringJoiner(",");
        StringJoiner scores = new StringJoiner(",");
        for (int seat = 1, length = getSeatMap().size(); seat <= length; seat++) {
            DtzPlayer player = getSeatMap().get(seat);
            players.add(String.valueOf(player.getUserId()));
            scores.add(String.valueOf(player.getWinLossPoint()));
        }
        logTable.setPlayers(players.toString());
        logTable.setScores(scores.toString());
    }

    @Override
    public String getTableMsg() {
        Map<String, Object> json = new HashMap<>();
        json.put("wanFa", "打筒子");
        if (isGroupRoom()) {
            json.put("roomName", getRoomName());
        }
        json.put("playerCount", getPlayerCount());
        json.put("count", 0);
        if (isAutoPlay > 0) {
            json.put("autoTime", isAutoPlay / 1000);
            if (autoPlayGlob == 1) {
                json.put("autoName", "单局");
            } else {
                json.put("autoName", "整局");
            }
        }
        return JSON.toJSONString(json);
    }

    @Override
    public String getTableMsgForXianLiao() {
        StringBuilder sb = new StringBuilder();
        sb.append("【").append(getId()).append("】").append(finishBureau).append("/").append(totalBureau).append("局").append("\n");
        sb.append("————————————————").append("\n");
        sb.append("【").append(getRoomName()).append("】").append("\n");
        sb.append("【").append(getGameName()).append("】").append("\n");
        sb.append("【").append(TimeUtil.formatTime(new Date())).append("】").append("\n");
        int maxPoint = -999999999;
        List<DtzPlayer> players = new ArrayList<>();
        for (DtzPlayer player : seatMap.values()) {
            if (player.getWinLossPoint() > maxPoint) {
                maxPoint = player.getWinLossPoint();
            }
            players.add(player);
        }
        Collections.sort(players, new Comparator<DtzPlayer>() {
            @Override
            public int compare(DtzPlayer o1, DtzPlayer o2) {
                return o2.getWinLossPoint() - o1.getWinLossPoint();
            }
        });
        for (DtzPlayer player : players) {
            sb.append("————————————————").append("\n");
            int point = player.getWinLossPoint();
            sb.append(StringUtil.cutHanZi(player.getName(), 5)).append("【").append(player.getUserId()).append("】").append(point == maxPoint ? "，大赢家" : "").append("\n");
            sb.append(point > 0 ? "+" : point == 0 ? "" : "-").append(Math.abs(point)).append("\n");
        }
        return sb.toString();
    }

    public void logFaPai(DtzPlayer player) {
        StringBuilder sb = new StringBuilder("Dtz");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getName());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.getAutoPlay());
        sb.append("|").append("faPai");
        sb.append("|").append(player.getHandPais());
        LogUtil.msg(sb.toString());
    }

    public void logChuPai(DtzPlayer player, List<Integer> cards, CardTypeDtz cardType) {
        StringBuilder sb = new StringBuilder("Dtz");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.getAutoPlay());
        sb.append("|").append("chuPai");
        sb.append("|").append(cards);
        sb.append("|").append(cardType.name());
        LogUtil.msg(sb.toString());
    }

    public void logNotLet(DtzPlayer player) {
        StringBuilder sb = new StringBuilder("Dtz");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.getAutoPlay());
        sb.append("|").append("notLet");
        sb.append("|").append(player.getHandPais());
        LogUtil.msg(sb.toString());
    }

    public void logPoint(DtzPlayer player, Map<Integer, Integer> groupScore) {
        StringBuilder sb = new StringBuilder("Dtz");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.getAutoPlay());
        sb.append("|").append("point");
        sb.append("|").append(player.getWinLossPoint());
        sb.append("|").append(player.getWinLoseCredit());
        sb.append("|").append(player.getPoint());
        sb.append("|").append(findGroupByPlayer(player));
        sb.append("|").append(findPlayerMingCi(player));
        sb.append("|").append(player.getDtzTotalPoint());
        sb.append("|").append(groupScore);
        LogUtil.msg(sb.toString());
    }

    public void logGroupScore(Map<Integer, Integer> groupScore, int seq, String msg) {
        StringBuilder sb = new StringBuilder("Dtz");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append("groupScore");
        sb.append("|").append(seq);
        sb.append("|").append(groupScore);
        sb.append("|").append(msg);
        LogUtil.msg(sb.toString());
    }

}
