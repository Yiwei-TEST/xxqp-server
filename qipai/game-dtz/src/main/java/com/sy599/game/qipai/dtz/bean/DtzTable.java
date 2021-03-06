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
     * ??????????????????
     **/
    public final BombScore bombScore = new BombScore();
    /*** ?????????8??????    ?????? ?????????9?????????52 ?????? ?????????66?????????96 */
    private List<Integer> kou8CardList = new ArrayList<>();
    /*** ????????????????????????????????? */
    private volatile List<Integer> nowDisCardIds = new ArrayList<>();
    /**
     * ??????????????????????????????????????????
     **/
    private Pair<CardTypeDtz, Integer> firstCardTypePair = Pair.with(CardTypeDtz.CARD_0, 0);

    /*** ??????map */
    private Map<Long, DtzPlayer> playerMap = new ConcurrentHashMap<>();
    /*** ????????????????????? */
    private Map<Integer, DtzPlayer> seatMap = new ConcurrentHashMap<>();
    /*** ?????????????????? */
    private int max_player_count = 4; //4

    private int isFirstRoundDisThree;//????????????????????????  !!  ?????? ?????????????????????

    private List<Integer> cutCardList = new ArrayList<>();// ????????????

    private int showCardNumber = 0; // ???????????????????????????

    private int disCardRandom;//2???3?????????????????????

    private int ifMustPlay;//??????????????????

    private int wangTongZi;//3?????????????????????

    private int isAutoPlay;//????????????????????????

    private String modeId = "0";//?????????

    private int isDaiPai;//??????????????????

    private int dtz_auto_timeout = 30000;//??????????????????????????????
    private int dtz_auto_timeout2 = 30000;//??????????????????????????????
    private int dtz_auto_play_time = 3000;
    private int dtz_auto_startnext = 10000;//???????????????

    private volatile int discardCount = 0;//??????????????????
    /**
     * ??????1????????????2?????????
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
     * ???????????????
     **/
    public int dtzRound = 1;

    /**
     * ??????
     */
    private Map<Integer, List<DtzPlayer>> groupMap = new ConcurrentHashMap<>();

    public Map<Integer, Integer> getGroupScore() {
        return groupScore;
    }

    /**
     * ????????????
     */
    private Map<Integer, Integer> groupScore = new ConcurrentHashMap<>();

    public boolean isNew = true;
    /**
     * ?????????????????????????????????????????? ?????????????????????????????????????????????true????????????false?????????????????? ????????????true??????????????????
     **/
    public boolean masterFP = false;
    /**
     * ???????????????????????????????????????????????????????????????????????????????????????0
     */
    public int settSeat = 0;

    {
//        groupScore.put(1, 0);
//        groupScore.put(2, 0);
        totalBureau = 30;
    }

    /**
     * ??????????????????????????????
     */
    @Override
    protected void loadFromDB1(TableInf info) {
        if (!StringUtils.isBlank(info.getNowDisCardIds())) {
            this.nowDisCardIds = StringUtil.explodeToIntList(info.getNowDisCardIds());
            LogUtil.msg("load nowDisCardIds:" + nowDisCardIds + ",tableId:" + info.getTableId());
        }
    }


    /**
     * ?????????8?????????
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
     * ????????????
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
            //????????????
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
        //???????????????
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
            if (isBreak) {//????????????
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
                    if (winSeat1 < 10) {//???????????????????????????
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

        // ?????????
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
        // ?????????????????????
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
            // ???????????????
            long winGold = player.getWinGold()* getGoldRoom().getRate();
            long havingGold = player.loadAllGolds();
            if (winGold > 0) {
            	if (havingGold*loseGoldCount <= winGold) {
            		winGold = havingGold*loseGoldCount;
//            		fengDing = true;
            		player.setGoldResult(2);// ??????
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
        //????????????????????????????????????,???????????? , ?????????????????????????????????????????????????????????????????????????????????????????????
        if(maxTotalWin<-maxTotalLose){
        	loseFengDing =maxTotalWin/loseGoldCount;
        }
        for (Player player : getSeatMap().values()) {
            // ???????????????
            player.setWinGold(player.getWinGold() * getGoldRoom().getRate());

            long winGold = player.getWinGold();
            if (winGold < 0) {
            	//???????????????????????????????????????????????????
            	if(loseFengDing>0&&loseFengDing<=-winGold){
            		winGold =-loseFengDing;
            	}
                long havingGold = player.loadAllGolds();
                if (winGold + havingGold < 0) {
                    winGold = -havingGold;
                    player.setWinGold(winGold);
                    player.setGoldResult(1);//??????
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

        // ?????????
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
     * ??????????????????????????????
     **/
    public boolean jiesuan = false;


    private void checkSettlement() {
        if (hasWinGroup() != 0) { //????????????
            StringBuilder sb = new StringBuilder();
            sb.append("table:" + this.id + " ????????????????????? ???????????? group1:" + groupScore.get(1) + " group2:" + groupScore.get(2));
            if (isThreePlayer()) {
                sb.append(" group3:" + groupScore.get(3));
            }
            LogUtil.msg(sb.toString());
            calcOver1();
            calcOver2();
            calcOver3();
            //?????????
            diss();
        } else {
            LogUtil.msg("table:" + this.id + " ???????????????..");
            initNext();
            calcOver1(); //?????????
        }
    }

    /**
     * ??????????????? ?????? ??????????????????
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
     * ????????? ?????????????????? ???????????????
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
        /* ????????? ?????? ???------------- */
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
     * ????????? ??????????????? ?????? ???string
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
     * ????????????
     */
    public void fapai() {
        synchronized (this) {
            changeTableState(table_state.play);

            discardCount = 0;

            //???????????????????????????????????????????????????
            List<List<Integer>> list;
            if (GameServerConfig.isDebug() && zp != null) {
                //?????????????????????????????????
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
                //??????
                List<Integer> pdAll = new ArrayList<>(copyAll);
                Collections.shuffle(copyAll);
                //??????????????????
                List<List<Integer>> zplist = new ArrayList<>(zp);
                List<Integer> allZp = new ArrayList<>();
                for (List<Integer> a : zplist) {
                    allZp.addAll(a);
                }
                //?????????67
                if (out67Type == 1) {
                    for (int i = 0; i < copyAll.size(); i++) {
                        if ((copyAll.get(i) % 100) == 6 || (copyAll.get(i) % 100) == 7) {
                            copyAll.remove(i);
                            i--;
                        }
                    }
                }

                //?????????????????????????????????
                if (!CardTypeToolDtz.cheakOutcard(copyAll, allZp)) {
                    //?????????????????????????????????
                    list = CardTool.fapaiDtz(this, getPlayerCount(), playType, isFirstRoundDisThree, zp);
                } else {
                    //??????????????????
                    for (int x = 1; x <= getPlayerCount(); x++) {
                        if (zplist.size() < x) {
                            //???????????????
                            zplist.add(new ArrayList<Integer>());
                        }
                        List<Integer> ren = zplist.get(x - 1);
                        for (int y = 1; y <= count; y++) {
                            if (ren.size() >= y && ren.get(y - 1) != null) {
                                //?????????????????????????????????????????????
                                copyAll.remove(ren.get(y - 1));
                            }
                        }
                    }

                    copyAll = DtzzConstants.getPokCardList(this, copyAll, kouType, out67Type, getPlayType());
                    //??????
                    for (int x = 1; x <= getPlayerCount(); x++) {
                        List<Integer> ren = zplist.get(x - 1);
                        for (int y = 1; y <= count; y++) {
                            if (!(ren.size() >= y && ren.get(y - 1) != null)) {
                                //????????????
                                ren.add(copyAll.get(0));
                                copyAll.remove(0);
                            }
                        }
                    }
                    //??????????????????
                    List<Integer> pdZp = new ArrayList<>();
                    for (List<Integer> a : zplist) {
                        pdZp.addAll(a);
                    }
                    //????????????
                    if (CardTypeToolDtz.cheakOutcard(pdAll, pdZp) && (kouType == 1 || CardTypeToolDtz.cheakOutcard(pdZp, pdAll))) {
                        list = zplist;
                        //?????????????????????????????????
                        int i = 0;
                        for (int s = 1; s <= getPlayerCount(); s++) {
                            DtzPlayer player = seatMap.get(Integer.valueOf(s));
                            player.changeState(player_state.play);
                            List<Integer> lls = list.get(i);
                            LogUtil.msg(" ???????????????:" + lls.size());
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
     * ??????????????????seat
     */
    public int getNextDisCardSeat() {
        if (lastWin != 0) {
            return lastWin;
        }
        if (useNowDis) return nowDisCardSeat;
        return nowDisCardSeat;
    }

    /**
     * ??????????????????????????????
     */
    private int getNextDisCardSeatByDtz() {
        if (state != table_state.play) {
            return 0;
        }
        if (disCardRound == 0) { //????????????????????????
            if (lastWinSeat == 0) {  //???????????????????????????????????????
                return 1; //???????????????
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
                //???1????????????
                res.addExt((groupScore.containsKey(1)) ? groupScore.get(1) - scoreJu.get(1) : 0);//Ext2
                //???2????????????
                res.addExt((groupScore.containsKey(2)) ? groupScore.get(2) - scoreJu.get(2) : 0);//Ext3
                if (isThreePlayer()) {
                    res.addExt((groupScore.containsKey(3)) ? groupScore.get(3) - scoreJu.get(3) : 0);
                }
            } else {
                //???1????????????
                res.addExt(0);//Ext2
                //???2????????????
                res.addExt(0);//Ext3
                if (isThreePlayer()) {
                    res.addExt(0);
                }
            }
            //????????????dtz?????????????????????
            res.addExt(this.score);//Ext4
            //????????????5,10,k?????????
            Triplet<Integer, Integer, Integer> triplet = getFragmentCard();
            //?????????5???10???K
            res.addExt(triplet.getValue0());//Ext5
            res.addExt(triplet.getValue1());//Ext6
            res.addExt(triplet.getValue2());//Ext7
            //?????????????????????
            List<Integer> groupCount = getAllTzOrXiGroupScore();
            //?????????A??????B???
            for (Integer cout : groupCount) {
                res.addExt(cout);
            }
//        res.addExt(groupScore1.getValue0()); //Ext7
//        res.addExt(groupScore1.getValue1());//Ext8
            //??????????????????????????????
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

            //??????????????????????????????
            res.addExt(this.score_max); //Ext10
            //???????????????????????????
            res.addExt(jiangli);//Ext11
            //?????????8???????????????0???????????????1??????
            res.addExt(kouType);//Ext12
            //????????????6,7????????????0??????????????????1?????????
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
                    // ????????????????????????????????????
                    playerRes.addAllHandCardIds(player.getHandPais());
                } else {
                    // ?????????????????????????????????????????????????????????????????????
                }
//                LogUtil.msg(" ?????????????????????????????? : " + player.getPoint() + " group : " + findGroupByPlayer(player) + " ????????? : " + findPlayerMingCi(player) + " ????????? : " + player.getSeat() + " ???????????????????????? :" + player.getDtzTotalPoint());
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
            int nextSeat = getNextDisCardSeat(); //?????????????????????
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
     * ????????????  ?????? ??????
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
     * ?????????????????????
     */
    public void notLet(DtzPlayer player) {
        //????????????????????????????????????
        setNowDisCardSeat(player.getSeat());
        //?????????????????????????????????
        player.setIsNoLet(1);
        //??????????????????
        List<Integer> cards = new ArrayList<>();
        cards.add(0);
        player.addOutPais(cards);
        //???????????????????????????
        PlayCardRes.Builder res = PlayCardRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setCardType(0);
        res.setIsPlay(1);
        res.setCurScore(score);
        if (player.getHandPais().size() == 1) {
            // ??????
            res.setIsBt(1);
        }
        this.useNowDis = true;
        Pair<Boolean, DtzPlayer> pair = isDoneRound1();
        if (pair.getValue0()) {
            //DtzSendLog.sendCardLog(id, player.getUserId(), player.getName(), "????????????===??????===???????????????????????????????????????????????????notLet", playTimes.getValue0() + "???" + playTimes.getValue1() == null ? "" : playTimes.getValue1().getName() + "[" + playTimes.getValue1().getName() + "]");
            //???????????????????????????????????????????????????
            if (groupPlayer != null && groupPlayer.equals(pair.getValue1())) {
                res.setNextSeat(groupPlayer.getSeat());
                nowDisCardSeat = groupPlayer.getSeat();
                groupPlayer = null;
            } else {
                nowDisCardSeat = next(player, true);
                res.setNextSeat(nowDisCardSeat);
            }
        } else {
            //??????????????????????????????????????????
            res.setNextSeat(nowDisCardSeat = next(player, true));
        }

        res.setIsFirstOut(isDoneRound1().getValue0() ? 1 : 0);
        addPlayLog(player.getSeat() + "_");
//        LogUtil.msg("?????????="+getId()+",??????="+player.getUserId()+"["+player.getName()+"]??????="+player.getSeat()+"-??????-?????????????????????="+res.getNextSeat());
        logNotLet(player);
        DtzPlayer canPlayPlayer = null;
        for (DtzPlayer pdkPlayer : seatMap.values()) {
            PlayCardRes.Builder copy = res.clone();
//            List<Integer> canPlayList = CardTypeTool.canPlay(pdkPlayer.getHandPais(), nowDisCardIds);
//            if (disCardSeat == pdkPlayer.getSeat() || !canPlayList.isEmpty()) {
//                copy.setIsLet(1);
//
//            }
            if ((isThreePlayer() || getIfMustPlay() == 1) && pdkPlayer.getSeat() == this.nowDisCardSeat && pdkPlayer.getSeat() != this.disCardSeat && !this.isDoneRound1().getValue0()) { //???????????????????????????
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
     * ??????????????????????????????????????????
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
     * ??????
     */
    public void disCards(DtzPlayer player, List<Integer> cards, CardTypeDtz cardType) {
        if (disCardSeat == player.getSeat()) {
            clearIsNotLet();
        } else {
            player.setIsNoLet(0);
        }
        //??????????????????????????????
        setDisCardSeat(player.getSeat());
        //??????????????????????????????????????????????????????????????????
        player.addOutPais(cards);
        //??????????????????????????????
        setNowDisCardIds(cards);
        //CardTypeDtz cardType = CardTypeToolDtz.toPokerType(cards, this);
        // ??????????????????
        PlayCardRes.Builder res = PlayCardRes.newBuilder();

        List<Integer> cardss = getNowDisCardIds();

        if (nowDisCardIds != null && nowDisCardIds.size() != 0) {
            cardss = CardTypeToolDtz.sortPokers(cardss, this);
//        	LogUtil.msg("?????????????????????:" + nowDisCardIds + "  ?????????????????????:" + cardss);
        }
        res.addAllCardIds(cardss);
        res.setCardType(cardType.getType());
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setIsPlay(1);
        res.setIsFirstOut(0);
//        LogUtil.msg("table:" + id + " ??????:" + (isDoneRound1().getValue1() == null ? "" : isDoneRound1().getValue1().getName()) + " ???????????????????????????:" + ((res.getIsFirstOut() == 1) ? "?????????" : "??????"));
//        if (player.getHandPais().size() == 1) {
//            res.setIsBt(1);
//        }
        res.setIsBt(player.getHandPais().size());
        Pair<Integer, Integer> score;
        if (player.getIsNoLet() == 0) { //????????????
            if (scorePlayerTemp != null) scorePlayerTemp = null;
            changePlayTimes(true, player); //??????????????????
            score = CardTypeToolDtz.toScore(cards, this); //?????????????????????
            addScore(score.getValue0());  //?????????????????????
            addTzScore(score.getValue1()); //?????????????????????
            offerdCard(cards, player);
//            LogUtil.msg(player + " : " + score.getValue1());
        }
        res.setCurScore(this.score);
        addPlayLog(player.getSeat() + "_" + StringUtil.implode(cards));
        boolean isOver = player.getHandPais().size() == 0;
        if (isOver) { //????????????????????????
            this.scorePlayerTemp = player; //????????????????????????
            addFinishedPlaying(player); //????????????????????????
            for (DtzPlayer pdkPlayer : this.playerMap.values()) {
                pdkPlayer.writeComMessage(WebSocketMsgType.RES_MINGCI, (int) player.getUserId(), serialIndex);
            }

            int seat = getSeatInGroup(player);
            DtzPlayer next = seatMap.get(seat); //???????????????????????????
            this.groupPlayer = next; //????????????????????????
            changePlayTimes(true, next); //??????????????????
            int cc = next(player, true);
            this.nowDisCardSeat = cc;
            res.setNextSeat(cc);
        } else {
            int seat = next(player, true); //????????????????????????
            DtzPlayer next = seatMap.get(seat); //???????????????????????????
            this.nowDisCardSeat = next.getSeat();
            res.setNextSeat(seat);
        }
        logChuPai(player, cards, cardType);
//        LogUtil.msg("?????????="+getId()+",??????="+player.getUserId()+"["+player.getName()+"]??????="+player.getSeat()+"-discard-" + JacksonUtil.writeValueAsString(cards) + ":" + cardType.name()+"??????????????????="+res.getNextSeat());
        DtzPlayer canPlayPlaye = null;
        for (DtzPlayer pdkPlayer : seatMap.values()) {
            PlayCardRes.Builder copy = res.clone();
            if ((isThreePlayer() || getIfMustPlay() == 1) && pdkPlayer.getSeat() == this.nowDisCardSeat) { //???????????????????????????
                int canPlay = CardTypeToolDtz.isCanPlay(pdkPlayer.getHandPais(), cards, this);
                copy.setIsLet(canPlay);
                if (canPlay == 0) {
                    canPlayPlaye = pdkPlayer;
                }
            }

            pdkPlayer.writeSocket(copy.build());
//            LogUtil.msg(copy.toString());
        }
        if (checkFinished()) { // ??????????????????
//            LogUtil.msg("table:" + id + " ?????? ?????????!!!   ---->groupSerial????????????: " + this.groupSerial.size());
            changeTableState(table_state.over); // ????????????????????????
        }
        if (canPlayPlaye != null) {
            this.giveup(canPlayPlaye, 10);
        }
    }

    /**
     * ????????????????????????
     */
    public void clearIsNotLet() {
        for (DtzPlayer player : seatMap.values()) {
            player.setIsNoLet(0);
        }
    }

    /**
     * value1????????????????????????????????????0????????????????????????????????????3???????????????????????????value2??????????????????????????????
     **/
    private Pair<Integer, DtzPlayer> playTimes = Pair.with(0, null);

    /**
     * ?????????????????????????????????????????????
     *
     * @param islet  ???????????????
     * @param player ???????????????
     */
    public void changePlayTimes(boolean islet, DtzPlayer player) {
        if (islet) {
            //?????????
            playTimes = playTimes.setAt0(0);
            playTimes = playTimes.setAt1(player);
        } else {
            //?????????
            playTimes = playTimes.setAt0(playTimes.getValue0() + 1);
        }
    }


    /**
     * ????????????????????????????????????
     *
     * @return Pair ture ?????????  false ?????????
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
     * ?????????????????????????????????
     */
    private int score = 0;
    /**
     * ????????????????????????????????????
     **/
    private int tz_score = 0;

    /**
     * ?????????????????????
     */
    public void addScore(int score) {
        if (score != 0) {
            this.score += score;
            changeExtend();
        }
    }

    /**
     * ???????????????
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
     * ???????????????
     */
    public int getScore() {
        return this.score;
    }

    /**
     * ?????????????????????
     */
    public void clearRoundScore() {
        this.score = 0;
        this.tz_score = 0;
    }

    public void addScoreBuGroup(int group, int score) {
        this.groupScore.put(group, (this.groupScore.containsKey(group) ? this.groupScore.get(group) : 0) + score);
    }

    /**
     * ????????????
     */
    public void playCommandDtz(DtzPlayer player, List<Integer> cards, CardTypeDtz cardType) {
        if (lastWin != 0) lastWin = 0;
        setLastActionTime(TimeUtil.currentTimeMillis());

        if (cards != null && cards.size() > 0 && cards.get(0).intValue() > 0) {
            discardCount++;
        }

        //??????????????????????????????
        setNowDisCardSeat(player.getSeat());
        disCards(player, cards, cardType);
        if (isTest() && !isOver()) { //????????????????????????????????????
            while (true) {
                DtzPlayer next = seatMap.get((player.getSeat() + 1) > max_player_count ? 1 : player.getSeat() + 1); //?????????????????????
                if (next.getUserId() < 0) { // ?????????robot
                    List<Integer> oppo = null;
                    if (disCardSeat != next.getSeat()) {
                        oppo = getNowDisCardIds();
                    }
                    List<Integer> curList = next.getHandPais();
                    if (curList.isEmpty()) { //???????????????
                        break;
                    }
                    List<Integer> list = CardTypeTool.getBestAI(curList, oppo);
                    if (!checkCanPlayCard()) {
                        break;
                    }
                    changeDisCardRound(1);
                    if (list != null && !list.isEmpty()) {
                        /* ???????????????????????? */
                        setNowDisCardSeat(next.getSeat());
                        disCards(next, list, cardType); // ?????? nowDisCardSeat
                    } else {
                        // notLet(player);
                        if (next.getUserId() < 0) { // ???robot??????
                            notLet(next);
//							LogUtil.msg("??????:" + next.getSeat() + "????????????????????? ");
                        } else {
//							LogUtil.msg("????????????????????????");
                        }
                    }
                    player = next;
                    setLastActionTime(TimeUtil.currentTimeMillis());
                } else { //?????????
                    break;
                }
            }
        }
    }

    private boolean checkCanPlayCard() {
        //??????????????????
        if (seatMap.size() <= 0) {
            return false;
        }

        boolean isOver = isOver();
        boolean zeroHandPais = false;
        boolean allZeroPoint = true;
        for (DtzPlayer player : seatMap.values()) {
            // ?????????
            if (player.getHandPais().size() <= 0) {
                zeroHandPais = true;
            }
            // ??????????????????????????????0
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
            //??????????????????????????????
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
            StringBuilder str = new StringBuilder("table:" + getId() + " ??????:" + player.getName() + " ??????, ????????????seatMap ???????????????:");
            for (Entry<Integer, DtzPlayer> entry : getSeatMap().entrySet()) {
                str.append(entry.getKey()).append(":").append(entry.getValue().getName()).append(" ");
            }
            LogUtil.msg(str.toString());
            str = new StringBuilder("table:" + getId() + "?????????????????????????????????: \n");
            for (Entry<Integer, List<DtzPlayer>> entry : getGroupMap().entrySet()) {
                str.append("???:group -> ").append(entry.getKey());
                for (DtzPlayer player1 : entry.getValue()) {
                    str.append("  ??????:").append(player1.getName()).append(" ???????????? ").append(entry.getKey()).append(" ?????? ").append(player1.getSeat());
                }
                str.append("\n");
            }
            LogUtil.msg(str.toString());
            return true;
        }
    }

    @Override
    protected boolean quitPlayer1(Player player) {
        //?????????????????????????????????
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
        } else {//?????????????????????
            groupPlayer(player.getSeat(), (DtzPlayer) player);
        }
//        StringBuilder str = new StringBuilder("table:" + getId() + "????????????seatMap ???????????????:");
//        for (Map.Entry<Integer, Player> entry : getSeatMap().entrySet()) {
//            str.append(entry.getKey()).append(":").append(entry.getValue().getName()).append(" ");
//        }
//        str.append(" ????????????:").append(player.getName());
//        LogUtil.msg(str.toString());
        return false;
    }

    @Override
    protected void initNext1() {
        setNowDisCardIds(null);
        this.jiesuan = false;
        //???????????????????????????
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
            res.setGameType(getWanFa());// 1????????? 2??????

            res.setBanker(lastWinSeat);

            res.addXiaohu(nextPlayer.getAutoPlay());
            res.addXiaohu(timeout);

            tablePlayer.writeSocket(res.build());
            if (tablePlayer.getAutoPlay() == 1) {
                addPlayLog(tablePlayer.getSeat(), DtzzConstants.action_tuoguan + "", 1 + "");
            }
            logFaPai(tablePlayer);
        }
//        LogUtil.msg(" table: " + id + " ????????????????????? ????????????  " + nextSeat);
    }

    private int firstPai() {
        if (isFourPlayer() || getDisCardRandom() == 0) {
            return getMasterSeat();
        } else {
            return new Random().nextInt(getMaxPlayerCount()) + 1;
        }
    }

    /**
     * ???????????????
     */
    @Override
    protected void robotDealAction() {
        if (isTest()) {
            DtzPlayer next = seatMap.get(getNextDisCardSeatByDtz());
            if (next != null && next.getUserId() < 0) { //????????????
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
        // ??????????????????
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
        //????????????private int fangfei = 20, score_max = 600, jiangli = 0;
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
        if (isDaiPai == 2) {//??????????????????
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
     * ????????????msg
     *
     * @param over      ??????????????????
     * @param winPlayer ????????????
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

            HashMap<ScoreType, Integer> scoreMap = bureau.get(player); // ???????????????scorelist ?????????bug ??????????????????
            if (scoreMap != null) {
                build.addExt(((scoreMap.get(ScoreType.POINT_5) == null) ? 0 : scoreMap.get(ScoreType.POINT_5)) + ""); // ???????????????5?????????  1
                build.addExt(((scoreMap.get(ScoreType.POITN_10) == null) ? 0 : scoreMap.get(ScoreType.POITN_10)) + ""); // ???????????????10?????????  2
                build.addExt(((scoreMap.get(ScoreType.POINT_K) == null) ? 0 : scoreMap.get(ScoreType.POINT_K)) + ""); // ???????????????k????????? 3
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
            build.setTotalPoint(player.getDtzTotalPoint()); //??????????????? ????????????  !!!!!!!!!!
            build.setPoint(player.getPoint()); //????????????????????????????????? ????????????

            build.addExt((xiNumByType(ScoreType.POINT_XI, player)) + "");// ??????????????? "???"?????????  5
            build.addExt((xiNumByType(ScoreType.POINT_JACKER_S, player)) + "");// 6
            build.addExt((xiNumByType(ScoreType.POINT_JACKER_B, player)) + "");// 7

            build.addExt((getBomdByPlayer(player)) + "");//??????????????????????????????  3????????? // 8

            build.addExt(((player.getHandPais().size() != 0) ? 1 : 0) + "");//?????????????????? bk  9

            build.addExt((tongziNumByType(ScoreType.POINT_TZ_A, player)) + "");//A????????????  10
            build.addExt((tongziNumByType(ScoreType.POINT_TZ_K, player)) + "");//k????????????  11
            build.addExt((tongziNumByType(ScoreType.POINT_TZ_2, player)) + "");//2????????????  12

            build.addExt(findGroupByPlayer(player) + "");// 13

            build.addExt(findPlayerMingCi(player) + "");// 14
            // ???????????????
            build.addExt(winGroup + ""); // 15
            build.addExt((player.getRoundScore() + (build.getPoint())) + ""); //16
            build.addExt((tongziNumByType(ScoreType.POINT_TZ_S, player)) + "");//?????????????????? //17
            build.addExt((tongziNumByType(ScoreType.POINT_TZ_B, player)) + "");//?????????????????? //18
            //19
            build.addExt(String.valueOf((isGoldRoom() ? player.loadAllGolds() : 0)));
            build.addExt(String.valueOf(player.getCurrentLs()));
            build.addExt(String.valueOf(player.getMaxLs()));
            build.addExt(String.valueOf(matchId));

            build.addExt(String.valueOf(ticketCount));//23

            build.addExt((tongziNumByType(ScoreType.POINT_Xi_K, player)) + "");//K??? 24
            build.addExt((tongziNumByType(ScoreType.POINT_Xi_A, player)) + "");//A??? 25
            build.addExt((tongziNumByType(ScoreType.POINT_Xi_2, player)) + "");//2??? 26
            build.addExt((tongziNumByType(ScoreType.POINT_Xi_5Q, player)) + "");//5Q??? 27

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
                // ?????????????????????????????????????????????
                builderList.add(0, build);
            } else {
                builderList.add(build);
            }


            if (isCreditTable()) {
                player.setWinLoseCredit(player.getWinLossPoint() * creditDifen);
            }

        }

        //???????????????
        if (isCreditTable()) {
            //??????????????????
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
                // 2019-02-26??????
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------??????????????????---------------------------------

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
        if(AAScoure == 100 ){//AA?????????=100
            // 2020???11???19??? 10:01:07  AA?????????   ?????????????????????
            long _AACreditSum = 0;
            for (DtzPlayer p:getSeatMap().values()) {
                if(p.getDtzTotalPoint()>0){
                    _AACreditSum +=p.getDtzTotalPoint();
                }
            }

            _AACreditSum = _AACreditSum * creditDifen;
            long commissionCredit = 0;
            // ????????? ??????????????????
            if(_AACreditSum>creditCommissionLimit){
                //???????????? creditCommission
                commissionCredit =creditCommission;
            }else{
                //?????????
                long baoDiCredit = calcBaoDi(_AACreditSum);
                if (baoDiCredit <= 0) {
                    // ??????
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
            // ????????????
            long baoDiCredit = calcBaoDi(credit);
            if (baoDiCredit <= 0) {
                // ??????
                return;
            }
            commissionCredit = credit > baoDiCredit ? baoDiCredit : credit;
            isBaoDiCommission = true;
        }else {
            //??????
            if (creditCommissionMode1 == 1) {
                //??????????????????
                if (creditCommissionMode2 == 1) {
                    //?????????
                    if (credit >= dyjCredit && dyjCredit > 0) {
                        if (credit >= creditCommission) {
                            commissionCredit = creditCommission;
                        } else {
                            commissionCredit = credit;
                        }
                    }
                } else {
                    //????????????
                    if (credit > 0) {
                        if (credit >= creditCommission) {
                            commissionCredit = creditCommission;
                        } else {
                            commissionCredit = credit;
                        }
                    }
                }
            } else {
                //??????????????????
                if (creditCommissionMode2 == 1) {
                    //?????????
                    if (credit >= dyjCredit && dyjCredit > 0) {
                        long commission = credit * creditCommission / 100;
                        if (credit >= commission) {
                            commissionCredit = commission;
                        } else {
                            commissionCredit = credit;
                        }
                    }
                } else {
                    //????????????
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

        //???????????????0
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

    // ??????????????????????????????
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
                    // ????????????
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

                            // ????????????
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
//                            LogUtil.msg("?????????=" + getId() + ",??????=" + player.getUserId() + "[" + player.getName() + "]??????=" + player.getSeat() + "????????????");
                            player.setAutoPlay(1);
                            //????????????
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
                } else if (getState() == table_state.ready && playBureau > 1) {//???????????????
                    for (DtzPlayer player : seatMap.values()) {
                        if (player.getState() != player_state.entry) {
                            continue;
                        }
                        // ????????????????????????5???????????????
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


    //?????????8???????????????0???????????????1??????  ?????????????????????
    private int kouType = 0;
    //????????????6,7????????????0??????????????????1?????????
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
        //???????????????
        int score_max = StringUtil.getIntValue(params, 3, 600);
        //????????????
        int jiangli = StringUtil.getIntValue(params, 4, 0);
        //?????????8???????????????0???????????????1??????
        int kouType = StringUtil.getIntValue(params, 5, 0);
        //????????????6,7????????????0??????????????????1?????????
        int out67Type = StringUtil.getIntValue(params, 6, 0);
        int playerCount = StringUtil.getIntValue(params, 7, 0);// ????????????
        int showCardNumber = StringUtil.getIntValue(params, 8, 0);// ???????????????????????????
        int haveJacker = StringUtil.getIntValue(params, 6, 0);
        int disCardRandom = StringUtil.getIntValue(params, 9, 0);//2???3?????????????????????
        int ifMustPlay = StringUtil.getIntValue(params, 10, 0);//??????????????????
        int wangTongZi = StringUtil.getIntValue(params, 11, 0);//???????????????
        int isAutoPlay = StringUtil.getIntValue(params, 12, 0);//????????????

        setIsDaiPai(StringUtil.getIntValue(params, 13, 1));//????????????

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
         * 600??? AA 8 ?????? 30

		1000??? AA 15 ?????? 50
         */
        this.default_Sore_Dtz = (play == DtzzConstants.play_type_4POK) ? 80 : 60;
        this.fangfei = fangfei;//?????????
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

        //??????????????????????????????
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
        LogUtil.msg("table:" + id + " ?????????????????????  ???????????????:" + default_Sore_Dtz);


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
        //???????????????
        int score_max = StringUtil.getIntValue(params, 3, 600);
        //????????????
        int jiangli = StringUtil.getIntValue(params, 4, 0);
        //?????????8???????????????0???????????????1??????
        int kouType = StringUtil.getIntValue(params, 5, 0);
        //????????????6,7????????????0??????????????????1?????????
        int out67Type = StringUtil.getIntValue(params, 6, 0);
        int playerCount = StringUtil.getIntValue(params, 7, 0);// ????????????
        int showCardNumber = StringUtil.getIntValue(params, 8, 0);// ???????????????????????????
        int haveJacker = StringUtil.getIntValue(params, 6, 0);
        int disCardRandom = StringUtil.getIntValue(params, 9, 0);//2???3?????????????????????
        int ifMustPlay = StringUtil.getIntValue(params, 10, 0);//??????????????????
        int wangTongZi = StringUtil.getIntValue(params, 11, 0);//???????????????
        int time = StringUtil.getIntValue(params, 12, 0);//????????????
        int isDaiPai = StringUtil.getIntValue(params, 13, 2);//????????????
        int isOpenGps = StringUtil.getIntValue(params, 22, 0);//???????????????gps??????


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
        // ??????????????????
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
         * 600??? AA 8 ?????? 30

		1000??? AA 15 ?????? 50
         */
        this.default_Sore_Dtz = (play == DtzzConstants.play_type_4POK) ? 80 : 60;
        this.fangfei = fangfei;//?????????
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
        //??????????????????????????????
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
        LogUtil.msg("table:" + id + " ?????????????????????  ???????????????:" + default_Sore_Dtz);
        return true;
    }

    /**
     * ?????????????????????table ????????????  ????????????
     */
    private boolean issend = true;

    /**
     * ????????????????????????????????????
     */
    public synchronized void ready() {
        if (isAllReady() && issend) { //??????????????????????????????
            initRandomChoose();
            for (Entry<Integer, DtzPlayer> entry : seatMap.entrySet()) {
                entry.getValue().writeComMessage(WebSocketMsgType.COM_SELECT_SEAT, getRandomChoose().get(this.getRoomId()));
            }
            issend = false;
        }
    }

    /**
     * ??????????????????????????????
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
     * ?????????????????????
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
     * ??????????????????????????????????????????
     */
    public boolean checkChooseDone() {
//    	if(isThreePlayer()){
//    		return playerMap.size() == getMaxPlayerCount() && groupMap.size() == 3;
//    	}else if(isTwoPlayer()){
//    		return playerMap.size() == getMaxPlayerCount() && groupMap.size() == 2;
//    	}else{
        if (isFourPlayer()) {
            //?????????????????? ????????????????????? ???????????????
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
     * ?????????????????????????????????????????????
     */
    public synchronized void checkDeal(long userId) {
        if (!isNew || isThreePlayer() || isTwoPlayer()) {
            if (!isAllReady()) {
                return;
            }

            // ------ ????????????????????????????????????----------------
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
            // ??????
            fapai();
            setLastActionTime(TimeUtil.currentTimeMillis());
//            LogUtil.msg("table:" + id + "  ????????????????????? > seatMap.size:" + getSeatMap().size() + " playerMap.size:" + getPlayerMap().size());
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
                    // 1 ???????????????  2 ??????seat?????? ?????? ?????????null
                    StringBuilder str = new StringBuilder("table:" + id + " ??? " + i + " ?????????????????????. " + "groupMap?????????:");
                    for (Entry<Integer, List<DtzPlayer>> entry : getGroupMap().entrySet()) {
                        str.append(" ???:").append(entry.getKey()).append(" [");
                        for (DtzPlayer pt : entry.getValue()) {
                            str.append("{name:").append(pt.getName()).append(", seat:").append(pt.getSeat()).append("} ");
                        }
                        str.append("]  ");
                    }
                    LogUtil.msg(str.toString());
                }
            }
            //tempScoreList = cloneScoreList();//????????????????????????????????????
            // ??????msg
            sendDealMsg(0);
            robotDealAction();
            consume();//?????????????????????????????????
            calcAfter();//?????????????????????????????????
            updateGroupTableDealCount();

            calcCoinOnStart();
            calcCreditAAOnStart();
            // ??????????????????????????????????????????
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
     * ????????????
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
     * ?????????????????? ????????????????????????
     */
    private PriorityQueue<DtzPlayer> queue = new PriorityQueue<>();

    /**
     * ???????????????????????????????????????????????????
     */
    private synchronized void addFinishedPlaying(DtzPlayer player) {
        queue.offer(player);
    }

    /**
     * ??????????????????????????????  <??????, ArrayList<Pair<??????, DtzPlayer>????????????1???A??????2???B?????????????????????????????????
     **/
    private HashMap<Integer, ArrayList<Pair<Integer, DtzPlayer>>> groupSerial = new HashMap<>();

    /**
     * ??????????????? ????????????  ?????????????????????????????????!
     */
    private HashSet<DtzPlayer> removeTemp = new HashSet<>();
    private int serialIndex = 1; //???????????????

    /**
     * ????????????????????????
     */
    private synchronized boolean checkFinished() {
        while (!queue.isEmpty()) {
            DtzPlayer player = queue.poll();
            int group = getGroup(player.getSeat());
            ArrayList<Pair<Integer, DtzPlayer>> list;
            if (!groupSerial.containsKey(group)) {
                list = new ArrayList<>(); //????????????
                removeTemp.add(player);
                list.add(Pair.with(serialIndex, player)); //?????????
                groupSerial.put(group, list); //??????map?????????
            } else {
                list = groupSerial.get(group);
                //??????????????????bug
                for (Pair<Integer, DtzPlayer> entry : list) {
                    if (entry.getValue1().equals(player)) {
                        return false;
                    }
                }
                removeTemp.add(player);
                list.add(Pair.with(serialIndex, player)); //?????????
            }
            saveWinSeat(player);
            serialIndex++;
        }
        if (isThreePlayer() || isTwoPlayer()) {//???????????????????????????????????????
            return groupSerial.size() + 1 >= getMaxPlayerCount();
        } else {//????????????
            if (groupSerial.size() == 1) {
                if (groupSerial.get(1) != null) { //?????????????????????
                    return groupSerial.get(1).size() == 2;
                } else//???????????????????????????
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
     * ?????????????????????????????????
     *
     * @return 1A??????2B??????3?????????0??????
     */
    public int getWinGroup() {
        int ret = 0;
        if (groupSerial.size() == 1) {
            //?????????????????????????????????
            if (groupSerial.get(1) != null) {
                //?????????A????????????
                if (groupSerial.get(1).size() == 2) {
                    ret = 1;
                }
            } else if (groupSerial.get(2) != null) {
                //A?????????????????????B???
                if (groupSerial.get(2).size() == 2) {
                    ret = 2;
                }
            }
        } else if (groupSerial.size() == 2) {
            //????????????????????????
            if (groupSerial.get(1).size() == 2 && groupSerial.get(2).size() == 1) {
                ret = 1;
            } else if (groupSerial.get(1).size() == 1 && groupSerial.get(2).size() == 2) {
                ret = 2;
            } else if (groupSerial.get(1).size() == 2 && groupSerial.get(2).size() == 2) {
                ret = 3;
            }
        } else {
            //????????????????????????,????????????
            return 0;
        }
        //????????????????????????????????????????????????????????????????????????1??????2???
        if (ret == 0 || ret == 3) {
            StringBuilder error = new StringBuilder();
            error.append("??????===");
            for (Integer group : groupSerial.keySet()) {
                error.append("?????????").append(group).append(",");
                for (Pair<Integer, DtzPlayer> p : groupSerial.get(group)) {
                    error.append("???????????????").append(p.getValue0().toString()).append(",");
                    error.append("???????????????").append(p.getValue1().getUserId()).append("???").append(p.getValue1().getName()).append("???,");
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
     * ???????????????
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
     * ?????? 3, 4 ?????? 4???
     */
    private void completionGroupSerial() {
        List<DtzPlayer> pls = new ArrayList<>(playerMap.values()); //????????????
        pls.removeAll(removeTemp); //??????????????????????????????
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
                list = new ArrayList<>(); //????????????
                removeTemp.add(player);
                list.add(Pair.with(serialIndex, player)); //?????????
                groupSerial.put(group, list); //??????map?????????
            } else {
                list = groupSerial.get(group);
                removeTemp.add(player);
                list.add(Pair.with(serialIndex, player)); //?????????
            }
            serialIndex++;
        }
    }

    /**
     * ???????????????????????????????????? ????????????zero
     * <p>
     * autoPlayDiss:??????????????????
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
            if ((isMatch && playedBureau >= totalBureau) || ((!isMatch) && isGold && playedBureau >= 1) || ((!isMatch) && (!isGold) && entry.getValue() >= score_max)) {    //?????????  ???????????????
                if (isThreePlayer()) {
                    if (groupScore.get(1) > groupScore.get(2) && groupScore.get(1) > groupScore.get(3)) {
//                        LogUtil.msg("table:" + id + " ?????? ????????????:  1 -> " + groupScore.get(1) + " 2 ->" + groupScore.get(2) + " 3 ->" + groupScore.get(3) + " A?????????");
                        logGroupScore(groupScore, 1, "A?????????");
                        return 1;
                    } else if (groupScore.get(2) > groupScore.get(1) && groupScore.get(2) > groupScore.get(3)) {
//                        LogUtil.msg("table:" + id + " ?????? ????????????:  1 -> " + groupScore.get(1) + " 2 ->" + groupScore.get(2) + " 3 ->" + groupScore.get(3) + " B?????????");
                        logGroupScore(groupScore, 2, "B?????????");
                        return 2;
                    } else if (groupScore.get(3) > groupScore.get(1) && groupScore.get(3) > groupScore.get(2)) {
//                        LogUtil.msg("table:" + id + " ?????? ????????????:  1 -> " + groupScore.get(1) + " 2 ->" + groupScore.get(2) + " 3 ->" + groupScore.get(3) + " C?????????");
                        logGroupScore(groupScore, 3, "C?????????");
                        return 3;
                    } else if (groupScore.get(1).intValue() == groupScore.get(2).intValue() && groupScore.get(1) > groupScore.get(3)) {
//                        LogUtil.msg("table:" + id + " ?????? ????????????:  1 -> " + groupScore.get(1) + " 2 ->" + groupScore.get(2) + " 3 ->" + groupScore.get(3) + " AB?????????");
                        logGroupScore(groupScore, 12, "AB?????????");
                        return 12;
                    } else if (groupScore.get(1).intValue() == groupScore.get(3).intValue() && groupScore.get(1) > groupScore.get(2)) {
//                        LogUtil.msg("table:" + id + " ?????? ????????????:  1 -> " + groupScore.get(1) + " 2 ->" + groupScore.get(2) + " 3 ->" + groupScore.get(3) + " AC?????????");
                        logGroupScore(groupScore, 13, "AC?????????");
                        return 13;
                    } else if (groupScore.get(2).intValue() == groupScore.get(3).intValue() && groupScore.get(2) > groupScore.get(1)) {
//                        LogUtil.msg("table:" + id + " ?????? ????????????:  1 -> " + groupScore.get(1) + " 2 ->" + groupScore.get(2) + " 3 ->" + groupScore.get(3) + " BC?????????");
                        logGroupScore(groupScore, 23, "BC?????????");
                        return 23;
                    } else if (groupScore.get(1).intValue() == groupScore.get(2).intValue() && groupScore.get(2) == groupScore.get(3)) {
//                        LogUtil.msg("table:" + id + " ?????? ????????????:  1 -> " + groupScore.get(1) + " 2 ->" + groupScore.get(2) + " 3 ->" + groupScore.get(3) + " ABC?????????");
                        logGroupScore(groupScore, 123, "ABC?????????");
                        return 123;
                    }
                } else {
                    if (groupScore.get(1) > groupScore.get(2)) {
//                        LogUtil.msg("table:" + id + " ?????? ????????????:  1 -> " + groupScore.get(1) + " 2 ->" + groupScore.get(2) + " A?????????");
                        logGroupScore(groupScore, 1001, "A?????????");
                        return 1;
                    } else if (groupScore.get(1) < groupScore.get(2)) {
//                        LogUtil.msg("table:" + id + " ?????? ????????????:  1 -> " + groupScore.get(1) + " 2 ->" + groupScore.get(2) + " B?????????");
                        logGroupScore(groupScore, 1002, "B?????????");
                        return 2;
                    } else if (groupScore.get(1).intValue() == groupScore.get(2).intValue()) {
//                        LogUtil.msg("table:" + id + " ?????? ????????????:  1 -> " + groupScore.get(1) + " 2 ->" + groupScore.get(2) + " A,B ??????????????????");
                        logGroupScore(groupScore, 1012, "A,B ??????????????????");
                        return 12; // 3
                    }
                }
                return entry.getKey();
            }
        }
//        LogUtil.msg("table:" + id + " ?????? ????????????:  1 -> " + groupScore.get(1) + " 2 ->" + groupScore.get(2) + " ????????????????????????????????????.");
        logGroupScore(groupScore, 0, "????????????????????????????????????");
        //???????????????????????????
        return -1;
    }

    /**
     * ???????????????????????? ?????? ?????? ?????? ?????????  ???????????????????????? ????????????
     */
    private HashMap<DtzPlayer, HashMap<ScoreType, Integer>> scoreList = new HashMap<>();
    //??????????????????????????????????????????  ???????????????
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
     * ????????????????????? (??????????????????????????????)
     */
    private void recordCard(List<Integer> pokers, DtzPlayer player) {
        switch (playType) {
            case DtzzConstants.play_type_3POK:
            case DtzzConstants.play_type_3PERSON_3POK:
            case DtzzConstants.play_type_2PERSON_3POK:
                if (CardTypeToolDtz.isTongZi(pokers)) {
                    if (CardTypeToolDtz.tongZi_(pokers, 13)) { //K??????
                        addToScoreList(player, ScoreType.POINT_TZ_K); //?????????
                    } else if (CardTypeToolDtz.tongZi_(pokers, 14)) { //a ??????
                        addToScoreList(player, ScoreType.POINT_TZ_A); //?????????
                    } else if (CardTypeToolDtz.tongZi_(pokers, 15)) { //2??????
                        addToScoreList(player, ScoreType.POINT_TZ_2); //?????????
                    } else if (CardTypeToolDtz.tongZi_(pokers, 16)) { //????????????
                        addToScoreList(player, ScoreType.POINT_TZ_S); //?????????
                    } else if (CardTypeToolDtz.tongZi_(pokers, 17)) { //????????????
                        addToScoreList(player, ScoreType.POINT_TZ_B); //?????????
                    }
                } else if (CardTypeToolDtz.isBobmDi(pokers)) { //???????????????
                    addToScoreList(player, ScoreType.POINT_BD); //?????????
                }
                break;
            case DtzzConstants.play_type_4POK:
            case DtzzConstants.play_type_3PERSON_4POK:
            case DtzzConstants.play_type_2PERSON_4POK:
                if (CardTypeToolDtz.isXi(pokers)) {
                    if (CardTypeToolDtz.xiTypeOf_(pokers, 17)) { //?????????
                        addToScoreList(player, ScoreType.POINT_JACKER_B); //?????????
                    } else if (CardTypeToolDtz.xiTypeOf_(pokers, 16)) { //?????????
                        addToScoreList(player, ScoreType.POINT_JACKER_S); //?????????
                    } else { //???sss???
                        addToScoreList(player, ScoreType.POINT_XI);
                    }
                }
                break;
            case DtzzConstants.play_type_2PERSON_4Xi:
            case DtzzConstants.play_type_3PERSON_4Xi:
            case DtzzConstants.play_type_4PERSON_4Xi:
                if (CardTypeToolDtz.isTongZi(pokers)) {
                    if (CardTypeToolDtz.tongZi_(pokers, 13)) { //K??????
                        addToScoreList(player, ScoreType.POINT_TZ_K); //?????????
                    } else if (CardTypeToolDtz.tongZi_(pokers, 14)) { //a ??????
                        addToScoreList(player, ScoreType.POINT_TZ_A); //?????????
                    } else if (CardTypeToolDtz.tongZi_(pokers, 15)) { //2??????
                        addToScoreList(player, ScoreType.POINT_TZ_2); //?????????
                    }
                } else if (CardTypeToolDtz.isBobmDi(pokers)) { //???????????????
                    addToScoreList(player, ScoreType.POINT_BD); //?????????
                }
                if (CardTypeToolDtz.isXi(pokers)) {
                    if (CardTypeToolDtz.xiTypeOf_(pokers, 13)) { //K???
                        addToScoreList(player, ScoreType.POINT_Xi_K);
                    } else if (CardTypeToolDtz.xiTypeOf_(pokers, 14)) { //A???
                        addToScoreList(player, ScoreType.POINT_Xi_A);
                    } else if (CardTypeToolDtz.xiTypeOf_(pokers, 15)) { //2???
                        addToScoreList(player, ScoreType.POINT_Xi_2);
                    } else { //5Q???
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
            logScoreHistory(pdkplayer, poker); //?????????bug??????????????????
        }
    }

    /**
     * ????????????????????????????????????????????? ???????????????
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
     * ????????????????????????????????????????????? ??????????????? <br />
     * ?????? ??????????????????5 10 k ?????????
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
     * ?????????????????????????????????
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
     * ???????????????????????????
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
     * ???????????????????????????
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
     * ??????????????????????????????
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
     * ????????????
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
     * ??????????????????????????? ?????????
     */
    private void saveWinSeat(DtzPlayer player) {
        if (serialIndex == 1) {
            this.lastWinTemp = player.getSeat();
        }
    }

    /**
     * ????????????
     */
    public synchronized void delayedSettlement(int seta) {
        if (this.getState() == table_state.over) { //?????????
            this.jiesuan = true;
            completionGroupSerial();
            calcOver();
//		   addSerialScoreToGroup(groupScore());
            checkSettlement();
            clearBureauScoreLog(); //???????????????????????????
            this.score = 0; //??????????????????
            if (isGoldRoom()) {
//                for (DtzPlayer player : playerMap.values()) {
//                    player.setPoint(0); //????????????
//                    player.setDtzTotalPoint(0);
//                }
            } else {
                for (DtzPlayer player : playerMap.values()) {
                    player.setPoint(0); //????????????
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

            tempScoreList = cloneScoreList();//????????????????????????????????????

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
     * ??????????????????
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
     * ????????????(cardTemp????????????????????????????????????bureauTemp???????????????)
     */
    private List<List<Integer>> cardTemp = new ArrayList<>(), bureauTemp = new ArrayList<>();

    /**
     * ??????????????? ????????????5, 10, k??? ??????	HashMap<PdkPlayer, HashMap<ScoreType, Integer>>
     */
    private LinkedHashMap<DtzPlayer, HashMap<ScoreType, Integer>> roundCardScore = new LinkedHashMap<>();

    /***
     * ???????????????,????????????
     */
    public void offerdCard(List<Integer> cards, DtzPlayer player) {
        bureauTemp.add(new ArrayList<>(cards));
        switch (playType) {
            case DtzzConstants.play_type_3POK:
            case DtzzConstants.play_type_3PERSON_3POK:
            case DtzzConstants.play_type_2PERSON_3POK:
                if (CardTypeToolDtz.isTongZi(cards)) {
                    int value = CardToolDtz.toVal(cards.get(0));
                    if (value == 15 || value == 14 || value == 13 || value == 16 || value == 17) { // 2 a k  ??????????????? +?????????
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
                    if (value == 15 || value == 14 || value == 13 || value == 16 || value == 17) { // 2 a k  ??????????????? +?????????
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
     * ??????????????????
     */
    public List<List<Integer>> getCardTemp() {
        return cardTemp;
    }

    /**
     * ?????????
     */
    public void clearCardTemp() {
        cardTemp.clear();
        roundCardScore.clear();
        bureauTemp.clear();
    }

    /**
     * ???????????????5, 10, k?????????  ?????? ???????????? ????????? ?????? ??????
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
     * ???????????????
     */
    public int getRoundFragmentCardSocre() {
        Triplet<Integer, Integer, Integer> triplet = getFragmentCard();
        return triplet.getValue0() * 5 + triplet.getValue1() * 10 + triplet.getValue2() * 10;
    }

    /**
     * ??????????????????????????? A?????? B???
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
     * ????????????????????????????????????????????? ?????????????????????????????????
     */
    private int getSeatInGroup(DtzPlayer player) {
        // ??????????????????????????????
        int next = getPartnerSeat(player);
        // ??????????????????????????????????????????
        if (seatMap.containsKey(next) && !seatMap.get(next).getHandPais().isEmpty()) return next;
        // ???????????????????????????????????????
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
     * ??????????????????????????????
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
     * ???????????????,  ????????????????????????
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
     * ???????????????????????????????????????????????????
     */
    private LinkedHashMap<DtzPlayer, HashMap<ScoreType, Integer>> bureau = new LinkedHashMap<>();

    /**
     * ????????????????????????
     */
    private void logScoreHistory(DtzPlayer player, List<Integer> pokers) {
        switch (playType) {
            case DtzzConstants.play_type_3POK:
            case DtzzConstants.play_type_3PERSON_3POK:
            case DtzzConstants.play_type_2PERSON_3POK:
                if (CardTypeToolDtz.isTongZi(pokers)) {
                    if (CardTypeToolDtz.tongZi_(pokers, 13)) { //K??????
                        logScoreToBureauMap(player, ScoreType.POINT_TZ_K); //?????????
                    } else if (CardTypeToolDtz.tongZi_(pokers, 14)) { //a ??????
                        logScoreToBureauMap(player, ScoreType.POINT_TZ_A); //?????????
                    } else if (CardTypeToolDtz.tongZi_(pokers, 15)) { //2??????
                        logScoreToBureauMap(player, ScoreType.POINT_TZ_2); //?????????
                    } else if (CardTypeToolDtz.tongZi_(pokers, 16)) { //????????????
                        logScoreToBureauMap(player, ScoreType.POINT_TZ_S); //?????????
                    } else if (CardTypeToolDtz.tongZi_(pokers, 17)) { //????????????
                        logScoreToBureauMap(player, ScoreType.POINT_TZ_B); //?????????
                    }
                } else if (CardTypeToolDtz.isBobmDi(pokers)) { //???????????????
                    logScoreToBureauMap(player, ScoreType.POINT_BD); //?????????
                }
                break;
            case DtzzConstants.play_type_4POK:
            case DtzzConstants.play_type_3PERSON_4POK:
            case DtzzConstants.play_type_2PERSON_4POK:
                if (CardTypeToolDtz.isXi(pokers)) {
                    if (CardTypeToolDtz.xiTypeOf_(pokers, 17)) { //?????????
                        logScoreToBureauMap(player, ScoreType.POINT_JACKER_B); //?????????
                    } else if (CardTypeToolDtz.xiTypeOf_(pokers, 16)) { //?????????
                        logScoreToBureauMap(player, ScoreType.POINT_JACKER_S); //?????????
                    } else { //???sss???
                        logScoreToBureauMap(player, ScoreType.POINT_XI);
                    }
                }
                break;
            case DtzzConstants.play_type_2PERSON_4Xi:
            case DtzzConstants.play_type_3PERSON_4Xi:
            case DtzzConstants.play_type_4PERSON_4Xi:
                if (CardTypeToolDtz.isTongZi(pokers)) {
                    if (CardTypeToolDtz.tongZi_(pokers, 13)) { //K??????
                        logScoreToBureauMap(player, ScoreType.POINT_TZ_K); //?????????
                    } else if (CardTypeToolDtz.tongZi_(pokers, 14)) { //a ??????
                        logScoreToBureauMap(player, ScoreType.POINT_TZ_A); //?????????
                    } else if (CardTypeToolDtz.tongZi_(pokers, 15)) { //2??????
                        logScoreToBureauMap(player, ScoreType.POINT_TZ_2); //?????????
                    }
                } else if (CardTypeToolDtz.isBobmDi(pokers)) { //???????????????
                    logScoreToBureauMap(player, ScoreType.POINT_BD); //?????????
                }
                if (CardTypeToolDtz.isXi(pokers)) {
                    if (CardTypeToolDtz.xiTypeOf_(pokers, 13)) { //K???
                        logScoreToBureauMap(player, ScoreType.POINT_Xi_K);
                    } else if (CardTypeToolDtz.xiTypeOf_(pokers, 14)) { //A???
                        logScoreToBureauMap(player, ScoreType.POINT_Xi_A);
                    } else if (CardTypeToolDtz.xiTypeOf_(pokers, 15)) { //2???
                        logScoreToBureauMap(player, ScoreType.POINT_Xi_2);
                    } else { //5Q???
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
     * ???????????????????????????
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
     * ??????????????????????????????
     */
    private void clearBureauScoreLog() {
        bureauTemp.clear();
        bureau.clear();
        changeExtend();
    }

    //???????????????????????????
    public DtzPlayer scorePlayerTemp;
    //?????????????????????
    public DtzPlayer groupPlayer;
    //??????????????????????????????????????????true???????????????????????????????????????????????????
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
     * ?????????????????????????????????????????????????????????????????????????????????
     */
    private void setFirstCardType(CardTypeDtz fromType, int fromCount) {
        firstCardTypePair = Pair.with(fromType, fromCount);
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????
     */
    public Pair<CardTypeDtz, Integer> getFirstCardType() {
        return firstCardTypePair;
    }


    /**
     * ?????????????????????
     */
    public void initFirstCardType(List<Integer> cards, DtzTable table) {
        // ?????????????????????
        //???????????????????????????
        setFirstCardType(CardTypeDtz.CARD_0, 0);
        //????????????????????????????????????????????????
        CardTypeDtz fromType = CardTypeToolDtz.toPokerType(cards, table);
        if (fromType == CardTypeDtz.CARD_7 || fromType == CardTypeDtz.CARD_8) {
            //??????????????????????????????3????????????
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
            //??????????????????????????????????????????
            Collections.sort(vList, CardTypeToolDtz.comparator);
            //??????????????????????????????????????????
            Map<Integer, Integer> countList = new HashMap<>();
            int stV = vList.get(0);
            int count = 1;
            for (int i = 1; i < vList.size(); i++) {
                if (vList.get(i) == (stV + 1)) {
                    count++;
                    if (i == vList.size() - 1) {
                        //??????????????????????????????????????????????????????
                        countList.put(vList.get(i), count);
                    }
                    stV = vList.get(i);
                } else {
                    countList.put(vList.get(i - 1), count);
                    count = 1;
                    stV = vList.get(i);
                }
            }
            //????????????????????????
            int fromCount = 0;
            for (Entry<Integer, Integer> entry : countList.entrySet()) {
                if (entry.getValue() > fromCount) {
                    //?????????????????????????????????????????????
                    fromCount = entry.getValue();
                }
            }
            //????????????
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
     * ????????????????????????
     */
    @Override
    public void sendPlayerStatusMsg() {
        List<String> readyList = new ArrayList<>();
        //????????????????????????????????????????????????????????????????????????
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
            //????????????
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
     * ???????????????????????????
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

    // ?????????????????????
    public boolean isFourPlayer() {
        return getMaxPlayerCount() == 4;
    }

    // ?????????????????????
    public boolean isThreePlayer() {
        return getMaxPlayerCount() == 3;
    }

    // ?????????????????????
    public boolean isTwoPlayer() {
        return getMaxPlayerCount() == 2;
    }

    // ???????????????
    public boolean isThreePai() {
        int play = getPlayType();
        return (play == DtzzConstants.play_type_3POK || play == DtzzConstants.play_type_3PERSON_3POK || play == DtzzConstants.play_type_2PERSON_3POK);
    }

    // ???????????????
    public boolean isFourPai() {
        int play = getPlayType();
        return (play == DtzzConstants.play_type_4POK
                || play == DtzzConstants.play_type_3PERSON_4POK
                || play == DtzzConstants.play_type_2PERSON_4POK
                || DtzzConstants.isKlSiXi(play)
        );
    }

    //?????????????????????
    public boolean isKlSX() {
        return DtzzConstants.isKlSiXi(getPlayType());
    }

    /**
     * ?????????????????????
     *
     * @param player
     */
    public void sendCardMarker(DtzPlayer player) {
        ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.RES_CARD_MARKER, cardMarkerToJSON());
        player.writeSocket(com.build());
    }

    /**
     * ????????????
     *
     * @param player
     */
    public void giveup(DtzPlayer player, int len) {
        StringBuffer params = new StringBuffer();
        int param[] = new int[len];
        //????????????????????????
        if (this.isOver()) {
            //??????????????????
            if (this.isFourPlayer()) {
                //?????????????????????????????????,??????????????????
                int wingroup = this.getWinGroup();
                //?????????????????????????????????
                if (this.settSeat == 0) {
                    if (wingroup == 1) {
                        //A??????????????????B??????????????????????????????
                        if (player.getSeat() == 2) this.settSeat = 4;
                        else if (player.getSeat() == 4) this.settSeat = 2;
                        else return;//????????????,?????????????????????????????????????????????????????????
                    } else if (wingroup == 2) {
                        //B??????????????????A??????????????????????????????
                        if (player.getSeat() == 1) this.settSeat = 3;
                        else if (player.getSeat() == 3) this.settSeat = 1;
                        else return;//????????????,?????????????????????????????????????????????????????????
                    }
                }
                //??????????????????????????????????????????????????????????????????????????????????????????
                if (this.getSeatMap().get(this.settSeat).getHandPais().size() == 0) {
                    if (wingroup == 1) {
                        if (this.settSeat == 2) this.settSeat = 4;
                        else if (this.settSeat == 4) this.settSeat = 2;
                        else return;//????????????,?????????????????????????????????????????????????????????
                    } else if (wingroup == 2) {
                        if (this.settSeat == 1) this.settSeat = 3;
                        else if (this.settSeat == 3) this.settSeat = 1;
                        else return;//????????????,?????????????????????????????????????????????????????????
                    }
                    DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "????????????===??????===???????????????????????????", "???????????????" + this.settSeat);
                }
            }
            params.append("??????????????????[");
            //for(int i:param) params.append(i).append(",");
            params.append("]");
            params.append(",???????????????").append(player.getSeat());
            params.append(",?????????????????????").append(this.jiesuan);
            params.append(",???????????? : ").append(this.dtzRound);
            params.append(",???????????????????????????????????? : ").append(this.masterFP);
            params.append(",?????????????????????").append(this.isOver());
            //params.append(",????????????????????????????????????").append(this.getWinGroup());
            params.append(",?????????????????????").append(this.settSeat);
//			DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "????????????????????????????????????",params.toString());
            //???????????????????????????????????????
            if (this.settSeat != 0) {
                //??????????????????????????????????????????????????????
                if (player.getSeat() == this.settSeat) {
                    //?????????????????????????????????????????????
                    this.sendNotletInfo(player);
                    addPlayLog(player.getSeat() + "_");
                    //??????????????????
                    DtzPlayer winPlayer = player;
                    if (this.scorePlayerTemp != null) {
                        //??????scorePlayerTemp???????????????????????????????????????????????????
                        winPlayer = this.scorePlayerTemp;
                        this.scorePlayerTemp = null;
//						DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "????????????????????????????????????????????????","???????????????"+winPlayer.getUserId()+"["+winPlayer.getName()+"]");
                    } else {
                        //scorePlayerTemp????????????????????????
                        DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "??????????????????????????????????????????????????????", "???????????????" + winPlayer.getUserId() + "[" + winPlayer.getName() + "]");
                    }
                    //???????????????????????????
                    winPlayer.setPoint(winPlayer.getPoint() + this.getScore());
                    //????????????????????????????????????
                    winPlayer.setRoundScore(winPlayer.getRoundScore() + this.getTzScore());
                    LogUtil.msg("???????????????:" + winPlayer + " score : " + winPlayer.getPoint() + " - " + winPlayer.getRoundScore());
                    this.changeDisCardRound(1); //????????????
                    int group = this.findGroupByPlayer(winPlayer);//
                    this.addScoreBuGroup(group, this.getScore()); //??????????????????
                    this.clearRoundScore(); //????????????????????????
                    this.recordCardAll(this.getCardTemp(), winPlayer);  //???????????????
                    List<Integer> groupCount = this.getAllTzOrXiGroupScore();
                    int score_c = this.getRoundFragmentCardSocre();
                    for (Player pp : this.getPlayerMap().values()) {
                        pp.writeComMessage(WebSocketMsgType.RES_DISCARD_RUNS, (int) winPlayer.getUserId(), groupCount, score_c, 0);
                    }
                    this.clearCardTemp(); //??????????????????
                    LogUtil.msg("table" + this.getId() + " ???????????? ????????????????????? ??????  ???????????? ????????? ??????.");
                    this.delayedSettlement(player.getSeat());
                    return;
                }
            } else {
                this.sendNotletInfo(player);
                addPlayLog(player.getSeat() + "_");
                //?????????????????????????????????
                DtzPlayer winPlayer = player;
                if (this.scorePlayerTemp != null) {
//					DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "?????????????????????????????????","?????????????????? "+winPlayer.getUserId()+"["+winPlayer.getName()+"],?????????????????????"+this.scorePlayerTemp.getUserId()+"["+this.scorePlayerTemp.getName()+"]");
                    winPlayer = this.scorePlayerTemp;
                    this.scorePlayerTemp = null;
                }
                //???????????????????????????????????????????????????????????????????????????
                winPlayer.setPoint(winPlayer.getPoint() + this.getScore());
                //??????????????????????????????????????????????????????
                winPlayer.setRoundScore(winPlayer.getRoundScore() + this.getTzScore());
//				DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "??????????????????????????????????????????????????????????????????","???????????????:" + winPlayer.getUserId()+"["+winPlayer.getName()+"]"+",????????????"+winPlayer.getPoint()+",????????????"+winPlayer.getRoundScore());
                //????????????
                this.changeDisCardRound(1);
                //??????????????????
                int group = this.findGroupByPlayer(winPlayer);//
                //???????????????????????????
                this.addScoreBuGroup(group, this.getScore());
                //???????????????????????????
                this.clearRoundScore();
                //??????????????????
                this.recordCardAll(this.getCardTemp(), winPlayer);
                //??????????????????????????? A?????? B???
                List<Integer> groupCount = this.getAllTzOrXiGroupScore();
                //????????????5,10???k??????????????????
                int score_c = this.getRoundFragmentCardSocre();
                //???????????????
                for (Player pp : this.getPlayerMap().values()) {
                    pp.writeComMessage(WebSocketMsgType.RES_DISCARD_RUNS, (int) winPlayer.getUserId(), groupCount, score_c, 0);
                }
                //??????????????????
                this.clearCardTemp();
                //???????????????????????????????????????
//				DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "?????????????????????????????????","???????????????:" + winPlayer.getUserId()+"["+winPlayer.getName()+"]"+",????????????"+winPlayer.getPoint()+",????????????"+winPlayer.getRoundScore());
                //??????????????????
                this.delayedSettlement(player.getSeat());
                return;
            }
        }
        //??????????????????????????????

        //????????????????????????????????????
        Pair<Boolean, DtzPlayer> pair = this.isDoneRound1();
        //???????????????????????????????????????????????????????????????
        if (pair.getValue0()) {
            //DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "????????????===??????===???????????????????????????????????????????????????",params.toString());
            //????????????????????????????????????
            this.getNowDisCardIds().clear();
            if (pair.getValue1().equals(player)) {
                //????????????????????????????????????????????????????????????????????????????????????
                DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "????????????===????????????===???????????????????????????????????????????????????", params.toString());
                return;
            }
        }
        //????????????????????????????????????????????????????????????
        player.writeComMessage(WebSocketMsgType.REQ_COM_GIVEUP, param);
        //??????????????????????????????
        synchronized (this) {
            //???????????????????????????????????????????????????
            this.changePlayTimes(false, player);
            //?????????????????????
            this.notLet(player);
        }
//        DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "?????????????????????????????????????????????","??????????????? ??????????????????????????? " + this.getNowDisCardSeat());
//    				LogUtil.msg("table:" + this.getId() + "??????:" + player.getSeat() + "?????????????????? ??????????????????????????? " + this.getNowDisCardSeat());
        //????????????,???????????????????????????
        pair = this.isDoneRound1();
        if (pair.getValue0()) {
            //????????????????????????????????????????????????????????????
            //????????????????????????
            this.getNowDisCardIds().clear();
            //??????????????????
            DtzPlayer winPlayer = pair.getValue1();
            if (this.scorePlayerTemp != null) {
//                DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "?????????????????????????????????","?????????????????? "+winPlayer.getUserId()+"["+winPlayer.getName()+"],?????????????????????"+this.scorePlayerTemp.getUserId()+"["+this.scorePlayerTemp.getName()+"]");
                winPlayer = this.scorePlayerTemp;
                this.scorePlayerTemp = null;
            }
            //???????????????
            //???????????????????????????????????????????????????????????????????????????
            winPlayer.setPoint(winPlayer.getPoint() + this.getScore()); //?????????
            //??????????????????????????????????????????????????????
            winPlayer.setRoundScore(winPlayer.getRoundScore() + this.getTzScore());
//            DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "??????????????????????????????????????????","???????????????:" + winPlayer.getUserId()+"["+winPlayer.getName()+"]"+",????????????"+winPlayer.getPoint()+",????????????"+winPlayer.getRoundScore());
//    					LogUtil.msg("??????:" + winPlayer + " score : " + winPlayer.getPoint() + " - " + winPlayer.getRoundScore());
            //??????????????????????????????
            int group = this.findGroupByPlayer(winPlayer);
            //????????????????????????
            this.addScoreBuGroup(group, this.getScore());
            //?????????????????????????????????????????????
            this.clearRoundScore();
            //???????????????????????????
            this.recordCardAll(this.getCardTemp(), winPlayer);  //???????????????
            //????????????????????????????????????
            List<Integer> groupCount = this.getAllTzOrXiGroupScore();
            //????????????5,10???k??????????????????
            int score_c = this.getRoundFragmentCardSocre();
            //???????????????
            for (Player pp : this.getPlayerMap().values()) {
                pp.writeComMessage(WebSocketMsgType.RES_DISCARD_RUNS, (int) winPlayer.getUserId(), groupCount, score_c);
            }
            //????????????????????????
            this.clearCardTemp(); //??????????????????
        }
    }

    public void playCommand(DtzPlayer player, List<Integer> cards, CardTypeDtz cardType) {
        synchronized (this) {
            //?????????????????????????????????
            if (this.getNowDisCardSeat() != 0 && this.getNowDisCardSeat() != player.getSeat()) return;
            //?????????????????????????????????????????????
            if (this.jiesuan) return;
            //????????????????????????
            if (player.getHandPais().size() == 0) return;
            //?????????????????????
            if (this.getState() == table_state.ready) return;

            if (!CardTypeToolDtz.cheakOutcard(player.getHandPais(), cards)) return;

            //???????????????????????????????????????
            List<Integer> disCardIds = this.getNowDisCardIds();

            //????????????
            StringBuilder params = new StringBuilder();
            params.append("??????????????????");
            params.append(JacksonUtil.writeValueAsString(cards));
            params.append(",???????????????").append(player.getSeat());
            params.append(",?????????????????????").append(this.jiesuan);
            params.append(",???????????? : ").append(this.dtzRound);
            params.append(",???????????????????????????????????? : ").append(this.masterFP);
            params.append(",?????????????????????").append(this.isOver());
            //params.append(",????????????????????????????????????").append(table.getWinGroup());
            params.append(",?????????????????????").append(this.settSeat);
            params.append(",???????????????????????????").append(this.getNowDisCardSeat());
            params.append(",??????????????????");
            params.append(JacksonUtil.writeValueAsString(disCardIds));
            params.append(",?????????????????????[").append(this.getFirstCardType().getValue0()).append(",").append(this.getFirstCardType().getValue1()).append("]");
            //DtzSendLog.sendCardLog(table.getId(), player.getUserId(), player.getName(), "????????????",params.toString());

            //??????????????????
            this.groupPlayer = null;
            //???????????????,??????false??????????????????????????????????????????????????????
            this.useNowDis = false;
            //???????????????????????????
            if (this.isOver()) {
                //???????????????????????????????????????
                if (CardTypeToolDtz.comparePoker(disCardIds, cards, this) != DtzConstant.OWN_WIN) {
                    DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "????????????===??????===?????????????????????????????????", params.toString());
                    return;
                }
                //????????????
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
                //??????????????????????????????????????????????????????????????????
                player.addOutPais(cards);//???????????????????????????

                Pair<Integer, Integer> score = CardTypeToolDtz.toScore(cards, this); //?????????????????????
                this.addScore(score.getValue0());  //?????????????????????
                this.addTzScore(score.getValue1()); //?????????????????????
                this.offerdCard(cards, player);

                //???????????????????????????
                player.setPoint(player.getPoint() + this.getScore());
                //?????????????????????
                player.setRoundScore(player.getRoundScore() + this.getTzScore());
                //????????????
                this.changeDisCardRound(1);
                //????????????
                int group = this.findGroupByPlayer(player);
                //??????????????????
                this.addScoreBuGroup(group, this.getScore());
                //????????????????????????
                this.clearRoundScore();
                //??????????????????
                this.recordCardAll(this.getCardTemp(), player);
                List<Integer> groupCount = this.getAllTzOrXiGroupScore();
                //???5,10???K???????????????
                int score_c = this.getRoundFragmentCardSocre();
                //???????????????????????????
                for (Player pp : this.getPlayerMap().values()) {
                    pp.writeComMessage(WebSocketMsgType.RES_DISCARD_RUNS, (int) player.getUserId(), groupCount, score_c, 0);
                }
                //??????????????????
                this.clearCardTemp();
                //????????????????????????
                this.delayedSettlement(player.getSeat());
                return;
            }

            //???????????????????????????0
            if (cardType.getType() != 0) {
                //????????????????????????
                if (this.getSeatMap().size() < this.getMaxPlayerCount()) {
                    DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "????????????===??????===????????????", "Seat:" + this.getSeatMap().size());
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_6));
                    return;
                }
                // ????????????
                if (this.getDisCardSeat() != player.getSeat()) { //???????????????
                    // ?????????????????????????????????,??????????????????????????????
                    if (disCardIds != null && !disCardIds.isEmpty()) {
                        // ????????????????????????????????????????????????????????????????????????
                        if (CardTypeToolDtz.comparePoker(disCardIds, cards, this) != DtzConstant.OWN_WIN) {
                            DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "????????????===??????===?????????????????????????????????", params.toString());
                            return;
                        }
                    } else {
                        //??????????????????????????????????????????
                        this.initFirstCardType(cards, this);
                    }
                    if (this.getMasterId() == player.getUserId() || (this.getSeatMap().get(this.lastWin) != null && this.getSeatMap().get(this.lastWin).getUserId() == player.getUserId())) {
                        //??????????????????????????????????????????????????????????????????????????????
                    }
                    this.setMasterFP(true);//???????????????????????????
                } else {
                    //??????????????????????????????????????????
                    this.initFirstCardType(cards, this);
                    this.clearIsNotLet();
                }
            } else {
                String param = "Empty:" + disCardIds.isEmpty() + ",NowDisCardSeat:" + this.getNowDisCardSeat() + ",player.getSeat:" + player.getSeat() + ",table.lastWin:" + this.lastWin;
                DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "????????????===??????===CardType???0 ", param);
                // ?????????????????????
                // ??????????????????
                if (disCardIds.isEmpty()) return;
                // ??????????????????????????????
                if (this.getNowDisCardSeat() == player.getSeat()) return;
                if (CardTypeToolDtz.comparePoker(disCardIds, cards, this) != DtzConstant.OWN_WIN) {
                    DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "????????????===??????===CardType???0?????????????????????", params.toString());
                    return;
                }
                if (this.getMasterId() == player.getUserId() || (this.getSeatMap().get(this.lastWin) != null && this.getSeatMap().get(this.lastWin).getUserId() == player.getUserId())) {
                    DtzSendLog.sendCardLog(this.getId(), player.getUserId(), player.getName(), "????????????===??????===?????????????????????", "win:" + this.lastWin);
                    this.setMasterFP(true);
                }
            }
            this.playCommandDtz(player, cards, cardType);
        }
    }

    @Override
    public void calcDataStatistics2() {
        //??????????????? ???????????????????????????????????????????????????????????????????????????????????????????????? ????????????
        if (isGroupRoom()) {
            String groupId = loadGroupId();
            int maxPoint = 0;
            int minPoint = 0;
            Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            //Long dataDate, String dataCode, String userId, String gameType, String dataType, int dataValue
            calcDataStatistics3(groupId);

            for (DtzPlayer player : playerMap.values()) {
                //????????????
                DataStatistics dataStatistics1 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "xjsCount", playedBureau);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics1, 3);
                //????????????
                DataStatistics dataStatistics5 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "djsCount", 1);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5, 3);
                //?????????
                DataStatistics dataStatistics6 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "zjfCount", player.getWinLossPoint());
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics6, 3);
                if (player.getWinLossPoint() > 0) {
                    if (player.getWinLossPoint() > maxPoint) {
                        maxPoint = player.getWinLossPoint();
                    }
                    //??????????????????
                    DataStatistics dataStatistics2 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "winMaxScore", player.getWinLossPoint());
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics2, 4);
                } else if (player.getWinLossPoint() < 0) {
                    if (player.getWinLossPoint() < minPoint) {
                        minPoint = player.getWinLossPoint();
                    }
                    //??????????????????
                    DataStatistics dataStatistics3 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "loseMaxScore", player.getWinLossPoint());
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics3, 5);
                }
            }

            for (DtzPlayer player : playerMap.values()) {
                if (maxPoint > 0 && maxPoint == player.getWinLossPoint()) {
                    //??????????????????
                    DataStatistics dataStatistics4 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "dyjCount", 1);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4, 1);
                } else if (minPoint < 0 && minPoint == player.getWinLossPoint()) {
                    //??????????????????
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
     * ?????????????????????
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

        // ??????????????????????????????
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
                                // ?????????AA?????????????????????
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
        return "?????????";
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
        json.put("wanFa", "?????????");
        if (isGroupRoom()) {
            json.put("roomName", getRoomName());
        }
        json.put("playerCount", getPlayerCount());
        json.put("count", 0);
        if (isAutoPlay > 0) {
            json.put("autoTime", isAutoPlay / 1000);
            if (autoPlayGlob == 1) {
                json.put("autoName", "??????");
            } else {
                json.put("autoName", "??????");
            }
        }
        return JSON.toJSONString(json);
    }

    @Override
    public String getTableMsgForXianLiao() {
        StringBuilder sb = new StringBuilder();
        sb.append("???").append(getId()).append("???").append(finishBureau).append("/").append(totalBureau).append("???").append("\n");
        sb.append("????????????????????????????????????????????????").append("\n");
        sb.append("???").append(getRoomName()).append("???").append("\n");
        sb.append("???").append(getGameName()).append("???").append("\n");
        sb.append("???").append(TimeUtil.formatTime(new Date())).append("???").append("\n");
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
            sb.append("????????????????????????????????????????????????").append("\n");
            int point = player.getWinLossPoint();
            sb.append(StringUtil.cutHanZi(player.getName(), 5)).append("???").append(player.getUserId()).append("???").append(point == maxPoint ? "????????????" : "").append("\n");
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
