package com.sy599.game.qipai.qianfen.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseGame;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
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
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayCardRes;
import com.sy599.game.msg.serverPacket.TableRes.*;
import com.sy599.game.qipai.qianfen.util.CardUtils;
import com.sy599.game.qipai.qianfen.util.CardValue;
import com.sy599.game.qipai.qianfen.util.QianFUtil;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QianfenTable extends BaseTable implements BaseGame {
    private static final int JSON_TAG = 1;
    private Object lock = this;

    /**
     * 托管时间
     */
    private volatile int autoTimeOut = Integer.MAX_VALUE;
    private volatile int autoTimeOut2 = Integer.MAX_VALUE;

    private volatile int overGame = 0;//房间正常结束
    /*** 当前牌桌上出的牌 */
    private volatile List<CardValue> nowDisCardIds = new ArrayList<>();
    /*** 玩家map */
    private Map<Long, QianfenPlayer> playerMap = new ConcurrentHashMap<Long, QianfenPlayer>();
    /*** 座位对应的玩家 */
    private Map<Integer, QianfenPlayer> seatMap = new ConcurrentHashMap<Integer, QianfenPlayer>();
    /*** 最大玩家数量 */
    private volatile int max_player_count = 3;

    private volatile List<Integer> cutCardList = new ArrayList<>();// 切掉的牌

    private volatile List<Integer> leftCardList = new ArrayList<>();// 剩下的牌

    private volatile List<Integer> scoreCardList = new ArrayList<>();// 当前轮的分牌

    private volatile int showCardNumber = 1; // 是否显示剩余牌数量

    private volatile int random_start = 0;//游戏开始随机出牌

    private long groupPaylogId = 0;  //俱乐部战绩记录id
    /**
     * 牌局结束分
     */
    private volatile int over_score = 1000;

    /**
     * 桌面上的牌分值
     */
    private volatile int current_score = 0;

    /**
     * 牌局结束奖励分
     */
    private volatile int over_reward = 100;

    /**
     * 桌面上的炸弹记分规则，1加法2乘法
     */
    private volatile int boom_score_rule = 1;

    /**
     * 几副牌
     */
    private volatile int pairs = 3;

    /**
     * 切掉的牌值
     */
    private volatile int[] cutCardVals = new int[]{3, 4};

    private volatile int cutCardMark = 0;

    private volatile long startXjTime = 0;

    /**
     * 翻出的牌
     */
    private volatile int fanCard = 0;

    /**
     * 二游三游赔给一游的分
     */
    private volatile int[] peiScore = new int[]{0, 0};

    private volatile int peiMark = 0;
    //是否加倍：0否，1是
    private int jiaBei;
    //加倍分数：低于xx分进行加倍
    private int jiaBeiFen;
    //加倍倍数：翻几倍
    private int jiaBeiShu;
    //低于below加分
    private int belowAdd=0;
    private int below=0;
    private volatile int timeNum = 0;
    /**托管1：单局，2：全局*/
    private int autoPlayGlob;
    private int autoTableCount;
    private int finishFapai=0;
    private int newRound=1;
    private int second=0;

    public int[] getCutCardVals() {
        return cutCardVals;
    }

    public List<Integer> getLeftCardList() {
        return leftCardList;
    }

    public int getPairs() {
        return pairs;
    }

    public void setFanCard(int fanCard) {
        this.fanCard = fanCard;
        changeExtend();
    }

    public int getFanCard() {
        return fanCard;
    }

    public Object getLock() {
        return lock;
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
        setLastActionTime(TimeUtil.currentTimeMillis());

        max_player_count = StringUtil.getIntValue(params, 7, max_player_count);
        payType = StringUtil.getIntValue(params, 2, 1);
        int temp = StringUtil.getIntValue(params, 3, 0);
        if (temp == 2) {
            cutCardVals = new int[]{3, 4, 6, 7};
        } else if (temp == 1) {
            cutCardVals = new int[]{3, 4};
        } else {
            cutCardVals = new int[0];
        }
        boom_score_rule = StringUtil.getIntValue(params, 4, 1);



        cutCardMark = temp;
        temp = StringUtil.getIntValue(params, 5, 0);
        if (max_player_count == 3) {
            if (temp == 1) {
                peiScore = new int[]{40, 60};
            } else if (temp == 2) {
                peiScore = new int[]{30, 70};
            } else if (temp == 3) {
                peiScore = new int[]{0, 40};
            }
        }
        if (max_player_count == 2) {
            if (temp == 4) {
                peiScore = new int[]{60, 0};
            } else if (temp == 5) {
                peiScore = new int[]{40, 0};
            }
        }
        temp = StringUtil.getIntValue(params, 6, 1);
        switch (temp){
            case 1:
                over_reward=100;
                break;
            case 2:
                over_reward=200;
                break;
        }
        peiMark = temp;
        int time = StringUtil.getIntValue(params, 8, 0);
        if(time>0) {
            this.autoPlay =true;
        }
        autoTimeOut =autoTimeOut2 =time*1000;
        autoPlayGlob = StringUtil.getIntValue(params, 9, 0);

        fanCard = 0;

        changeExtend();

    }


    @Override
    protected void loadFromDB1(TableInf info) {
        if (!StringUtils.isBlank(info.getNowDisCardIds())) {
            this.nowDisCardIds = CardUtils.loadCards(StringUtil.explodeToIntList(info.getNowDisCardIds()));
        }
    }

    public long getId() {
        return id;
    }

    public QianfenPlayer getPlayer(long id) {
        return playerMap.get(id);
    }

    /**
     * 一局结束
     */
    public void calcOver() {
        QianfenPlayer winPlayer = null, winPlayer0 = null;
        int maxPoint = 0;
        for (QianfenPlayer player : seatMap.values()) {
            player.changeState(player_state.over);
            int rank = player.getResults().get(playedBureau).get(4).intValue();
            if (rank == 1) {
                winPlayer = player;
            }
            int totalPoint = player.calcCardAndRankScore();
            LogUtil.msgLog.info("calcOver msg:tableId=" + id + ",player=" + player.getUserId() + ",calcCardAndRankScore=" + totalPoint + ",boomScore=" + player.calcBoomScore() + ",rank=" + rank);
            if (totalPoint > maxPoint || winPlayer0 == null) {
                winPlayer0 = player;
                maxPoint = totalPoint;
            } else if (totalPoint == maxPoint && rank <
                    winPlayer0.getResults().get(playedBureau).get(4).intValue()) {
                winPlayer0 = player;
            }
        }
        if (winPlayer == null) {
            return;
        }

        boolean isOver = maxPoint >= over_score;
//        boolean isOver = maxPoint >= 10;
        if (isOver) {
            overGame = 1;
            changeExtend();
            LogUtil.msgLog.info("calcOver msg win:tableId=" + id + ",player=" + winPlayer0.getUserId() + ",calcCardAndRankScore=" + maxPoint + ",boomScore=" + winPlayer0.calcBoomScore() + ",rank=" + winPlayer0.getResults().get(playedBureau).get(4).intValue() + ",reward=" + over_reward);
            if (over_reward > 0) {
                winPlayer0.getResults().get(playedBureau).set(5, over_reward);
                changePlayers();
            }
        }
        if(autoPlayGlob >0) {
            boolean diss = false;
            if(autoPlayGlob ==1) {
                for (QianfenPlayer seat : seatMap.values()) {
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
        ClosingInfoRes.Builder res = sendAccountsMsg(isOver, winPlayer0, false);
        saveLog(isOver, winPlayer.getUserId(), res.build());
        setLastWinSeat(winPlayer.getSeat());

        calcAfter();

        if (isOver) {
            calcOver1();
            calcOver2();
            calcOver3();
            diss();
        } else {
            initNext();
            calcOver1();

            fanCard = 0;
        }
        for (Player player : seatMap.values()) {
            player.saveBaseInfo();
        }
    }

    private boolean checkAuto3() {
        boolean diss = false;
        // if(autoPlayGlob==3) {
        boolean diss2 = false;
        for (QianfenPlayer seat : seatMap.values()) {
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


    public void saveLog(boolean over, long winId, Object resObject) {
        ClosingInfoRes res = (ClosingInfoRes) resObject;
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
        userLog.setMaxPlayerCount(max_player_count);
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
            userGroupLog.setDiFen(over_reward+"");
            userGroupLog.setOvertime(sdf.format(now));
            userGroupLog.setPlayercount(max_player_count);
            String groupId = isGroupRoom()?loadGroupId() : 0+"";
            userGroupLog.setGroupid(Long.parseLong(groupId));
            userGroupLog.setGamename("益阳千分");
            userGroupLog.setTotalCount(totalBureau);
            if(playBureau == 1){
                groupPaylogId = TableLogDao.getInstance().saveGroupPlayLog(userGroupLog);
            }else if(playBureau > 1 && groupPaylogId != 0){
                userGroupLog.setId(groupPaylogId);
                TableLogDao.getInstance().updateGroupPlayLog(userGroupLog);
            }
        }
        saveTableRecord(logId, over, playBureau);
        UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
        for (QianfenPlayer player : playerMap.values()) {
            player.addRecord(logId, playBureau);
        }

    }

    public Map<String, Object> saveDB(boolean asyn) {
        if (id < 0) {
            return null;
        }
        Map<String, Object> tempMap;
        synchronized (lock) {
            tempMap = loadCurrentDbMap();
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
                    tempMap.put("nowDisCardIds", StringUtil.implode(CardUtils.loadCardIds(nowDisCardIds), ","));
                }
                if (tempMap.containsKey("extend")) {
                    tempMap.put("extend", buildExtend());
                }
            }
        }
//	if (tempMap.size()>0)
//		TableDao.getInstance().save(tempMap);
        return tempMap.size() > 0 ? tempMap : null;
    }

    String convert2String(int... ints) {
        StringBuilder sb = new StringBuilder();
        for (int i : ints) {
            sb.append(",").append(i);
        }
        return sb.length() > 0 ? sb.substring(1) : "";
    }

    String convert2String(List<Integer> ints) {
        StringBuilder sb = new StringBuilder();
        for (Integer i : ints) {
            sb.append(",").append(i);
        }
        return sb.length() > 0 ? sb.substring(1) : "";
    }

    int[] convert2Ints(String str) {
        if (StringUtils.isNotBlank(str)) {
            String[] strs = str.split(",");
            int[] ints = new int[strs.length];
            for (int i = 0; i < strs.length; i++) {
                ints[i] = Integer.parseInt(strs[i]);
            }
            return ints;
        } else {
            return new int[0];
        }
    }

    public boolean isAllReady() {
        if (state == table_state.play) {
            return false;
        }
        if (getPlayerCount() < getMaxPlayerCount()) {
            return false;
        }
        for (Player player : getSeatMap().values()) {
            if (!player.isRobot() && (player.getState() != player_state.ready&& player.getState() != player_state.play)) {
                return false;
            }
        }
        return true;
    }

    List<Integer> convert2IntLists(String str) {
        if (StringUtils.isNotBlank(str)) {
            String[] strs = str.split(",");
            List<Integer> ints = new ArrayList<>(strs.length);
            for (int i = 0; i < strs.length; i++) {
                ints.add(Integer.valueOf(strs[i]));
            }
            return ints;
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public boolean isDissSendAccountsMsg(){
        return (playBureau > 1 || (playBureau==1 && (getState() == table_state.play || getState() == table_state.over)));
    }

    @Override
    public JsonWrapper buildExtend0(JsonWrapper wrapper) {
        wrapper.putInt(1, pairs);
        wrapper.putInt(2, max_player_count);
        wrapper.putInt(3, showCardNumber);
        wrapper.putInt(4, over_score);
        wrapper.putString(5, convert2String(cutCardList));
        wrapper.putString(6, convert2String(leftCardList));
        wrapper.putString(7, convert2String(peiScore));
        wrapper.putString(8, convert2String(cutCardVals));
        wrapper.putInt(9, random_start);
        wrapper.putInt(10, current_score);
        wrapper.putInt(11, over_reward);
        wrapper.putInt(12, boom_score_rule);
        wrapper.putInt(13, overGame);
        wrapper.putString(14, convert2String(scoreCardList));
        wrapper.putInt(15, cutCardMark);
        wrapper.putInt(16, peiMark);
        wrapper.putInt(17, fanCard);
        wrapper.putLong(18, groupPaylogId);
        wrapper.putInt(19, autoTimeOut);
        wrapper.putInt(20, autoPlayGlob);
        wrapper.putInt(21, finishFapai);
        wrapper.putInt(22, newRound);
        return wrapper;
    }

    protected String buildPlayersInfo() {
        StringBuilder sb = new StringBuilder();
        for (QianfenPlayer pdkPlayer : playerMap.values()) {
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

    private Map<Integer, Integer> fapai(List<List<Integer>> paiList, List<Integer> leftList, List<Integer> cutList) {
        int count = 0;
        int playerCount = max_player_count;
        List<Integer> list = CardUtils.loadCards(pairs, cutCardVals);
        leftList.clear();
        for (int i = 0; i < 6; i++) {
            leftList.add(list.get(i));
            count++;
        }
        int pais = (list.size() - count) / 3;

        paiList.clear();
        Map<Integer, Integer> map = new HashMap<>();
        for (int m = 0; m < playerCount; m++) {
            List<Integer> temp = new ArrayList<>();
            for (int i = 0; i < pais; i++) {
                temp.add(list.get(count));
                count++;
            }
            paiList.add(temp);
            Map<Integer, Integer> countMap = CardUtils.countValue(CardUtils.loadCards(temp));
            for (Map.Entry<Integer, Integer> kv : countMap.entrySet()) {
                if (kv.getValue().intValue() >= 7) {
                    map.put(kv.getValue(), map.getOrDefault(kv.getValue(), 0) + 1);
                }
            }
        }

        cutList.clear();
        for (; count < list.size(); count++) {
            cutList.add(list.get(count));
        }
        return map;
    }

    public void fapai(){
        fapai0();
    }

    /**
     * 开始发牌
     */
    public boolean fapai0() {
        synchronized (lock) {
            timeNum=0;
            Integer currentFanCard;
            if (fanCard > 0) {
                changeTableState(table_state.play);
                currentFanCard = fanCard;
                fanCard = 0;
                startXjTime = 0;
            } else {
                if (startXjTime == 0) {
                    startXjTime = System.currentTimeMillis();
                }

                return false;
            }

            List<List<Integer>> paiList = new ArrayList<>();
            if(zp!=null&&zp.size()!=0){
                paiList = CardUtils.zuoPai(zp, getMaxPlayerCount(),cutCardVals);
            }else {
                paiList= QianFUtil.fapaiControl(leftCardList,CardUtils.loadCards(pairs, cutCardVals),cutCardList,max_player_count,0);
            }

            int playerNo = 0;
            Map<Integer, List<Integer>> paisMap = new HashMap<>();
            for (QianfenPlayer player : playerMap.values()) {
                if (playerNo >= paiList.size()) {
                    break;
                }
                player.changeState(player_state.play);
                List<Integer> temp = paiList.get(playerNo);
                playerNo++;
                paisMap.put(player.getSeat(), temp);

                if (player.getResults().size() == playedBureau + 1) {
                    player.getResults().set(playedBureau, player.initResult(playBureau, 0, 0, 0, 0, 0));
                } else {
                    while (player.getResults().size() < playedBureau + 1) {
                        player.getResults().add(player.initResult(player.getResults().size() + 1, 0, 0, 0, 0, 0));
                    }
                }
            }

            if (lastWinSeat == 0) {
                for (QianfenPlayer player : playerMap.values()) {
                    List<Integer> paiList0 = paisMap.get(player.getSeat());
                    if (paiList0 != null && paiList0.contains(currentFanCard)) {
                        setLastWinSeat(player.getSeat());
                        break;
                    }
                }

                if (lastWinSeat == 0) {
                    int idx;
                    int seat = new SecureRandom().nextInt(max_player_count) + 1;
                    if ((idx = leftCardList.indexOf(currentFanCard)) != -1) {
                        List<Integer> paiList0 = paisMap.get(seat);
                        if (paiList0 != null) {
                            Integer pre = paiList0.set(0, currentFanCard);
                            leftCardList.set(idx, pre);
                            setLastWinSeat(seat);
                        }
                    } else if ((idx = cutCardList.indexOf(currentFanCard)) != -1) {
                        List<Integer> paiList0 = paisMap.get(seat);
                        if (paiList0 != null) {
                            Integer pre = paiList0.set(0, currentFanCard);
                            cutCardList.set(idx, pre);
                            setLastWinSeat(seat);
                        }
                    }
                }
            }

            if (lastWinSeat == 0) {
                if (random_start == 0 && masterId > 0) {
                    lastWinSeat = playerMap.get(masterId).getSeat();
                } else {
                    int[][] idVals = new int[max_player_count][2];
                    for (int i = 0; i < max_player_count; i++) {
                        List<Integer> ids = seatMap.get(i + 1).getHandPais();
                        int sum0 = 0;
                        int sum1 = 0;
                        for (int id : ids) {
                            sum0 += id;
                            sum1 += CardUtils.loadCardValue(id);
                        }
                        idVals[i][0] = sum0;
                        idVals[i][1] = sum1;
                    }
                    for (int i = 0; i < max_player_count; i++) {
                        int count1 = 1;
                        for (int j = 0; j < max_player_count; j++) {
                            if (i != j && idVals[i][0] > idVals[j][0]) {
                                count1++;
                            }
                        }
                        if (count1 == max_player_count) {
                            lastWinSeat = i + 1;
                            break;
                        }
                    }
                    if (lastWinSeat == 0) {
                        for (int i = 0; i < max_player_count; i++) {
                            int count1 = 1;
                            for (int j = 0; j < max_player_count; j++) {
                                if (i != j && idVals[i][0] < idVals[j][0]) {
                                    count1++;
                                }
                            }
                            if (count1 == max_player_count) {
                                lastWinSeat = i + 1;
                                break;
                            }
                        }
                        if (lastWinSeat == 0) {
                            for (int i = 0; i < max_player_count; i++) {
                                int count1 = 1;
                                for (int j = 0; j < max_player_count; j++) {
                                    if (i != j && idVals[i][1] > idVals[j][1]) {
                                        count1++;
                                    }
                                }
                                if (count1 == max_player_count) {
                                    lastWinSeat = i + 1;
                                    break;
                                }
                            }
                            if (lastWinSeat == 0) {
                                for (int i = 0; i < max_player_count; i++) {
                                    int count1 = 1;
                                    for (int j = 0; j < max_player_count; j++) {
                                        if (i != j && idVals[i][1] < idVals[j][1]) {
                                            count1++;
                                        }
                                    }
                                    if (count1 == max_player_count) {
                                        lastWinSeat = i + 1;
                                        break;
                                    }
                                }
                                if (lastWinSeat == 0) {
                                    for (int i = 0; i < max_player_count; i++) {
                                        int count1 = 1;
                                        for (int j = 0; j < max_player_count; j++) {
                                            if (i != j && idVals[i][1] + idVals[i][0] > idVals[j][1] + idVals[j][0]) {
                                                count1++;
                                            }
                                        }
                                        if (count1 == max_player_count) {
                                            lastWinSeat = i + 1;
                                            break;
                                        }
                                    }
                                    if (lastWinSeat == 0) {
                                        for (int i = 0; i < max_player_count; i++) {
                                            int count1 = 1;
                                            for (int j = 0; j < max_player_count; j++) {
                                                if (i != j && idVals[i][1] + idVals[i][0] < idVals[j][1] + idVals[j][0]) {
                                                    count1++;
                                                }
                                            }
                                            if (count1 == max_player_count) {
                                                lastWinSeat = i + 1;
                                                break;
                                            }
                                        }
                                        if (lastWinSeat == 0) {
                                            lastWinSeat = 1;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                setLastWinSeat(lastWinSeat);
            }
            finishFapai=1;
            if (playedBureau<=0){
                for (QianfenPlayer player : playerMap.values()) {
                    player.setAutoPlay(false,this);
                    player.setLastOperateTime(System.currentTimeMillis());
                }
            }
            for (QianfenPlayer player : playerMap.values()) {
                List<Integer> paiList0 = paisMap.get(player.getSeat());
                player.dealHandPais(paiList0 != null ? paiList0 : new ArrayList<>());

                LogUtil.msgLog.info("qianfen fapai:tableId={},playNum={},userId={},seat={},cards={}"
                ,id,playBureau,player.getUserId(),player.getSeat(),paiList0);
            }

            changePlayers();
            changeExtend();
        }

        return true;
    }

    /**
     * 下一次出牌的seat
     *
     * @return
     */
    public int getNextDisCardSeat() {
        int seat = 0;
        if (disCardRound == 0) {
            return lastWinSeat;
        } else {
            if (nowDisCardSeat != 0) {
                seat = nowDisCardSeat;
            }
        }
        return seat;
    }

    public QianfenPlayer getPlayerBySeat(int seat) {
        int next = seat >= max_player_count ? 1 : seat + 1;
        return seatMap.get(next);

    }

    public Map<Integer, Player> getSeatMap() {
        Object o = seatMap;
        return (Map<Integer, Player>) o;
    }

    @Override
    public CreateTableRes buildCreateTableRes(long userId, boolean isrecover, boolean isLastReady) {
        CreateTableRes.Builder res = CreateTableRes.newBuilder();
        synchronized (lock) {
            buildCreateTableRes0(res);
            res.setNowBurCount(getPlayBureau());
            res.setTotalBurCount(getTotalBureau());
            res.setGotyeRoomId(gotyeRoomId + "");
            res.setTableId(getId() + "");
            res.setWanfa(playType);
            List<PlayerInTableRes> players = new ArrayList<>();
            for (QianfenPlayer player : playerMap.values()) {
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
                if (player.getSeat() == disCardSeat && nowDisCardIds != null) {
                    playerRes.addAllOutCardIds(CardUtils.loadCardIds(nowDisCardIds));
                }
                players.add(playerRes.build());
            }
            res.addAllPlayers(players);
            int nextSeat = getNextDisCardSeat();
            if (nextSeat != 0) {
                res.setNextSeat(nextSeat);
            }
            res.setRenshu(this.max_player_count);
            res.addExt(this.showCardNumber);
            res.addExt(this.pairs);
            res.addExt(this.payType);
            res.addExt(this.random_start);
            res.addExt(this.over_score);
            res.addExt(this.current_score);
            res.addExt(this.cutCardMark);
            res.addExt(this.peiMark);
            res.addExt(this.over_reward);
            res.addExt(this.boom_score_rule);
            res.addExt(loadCutCardsSeat());//切牌人的位置 10
            res.addAllScoreCard(scoreCardList);
        }

        return buildCreateTableRes1(res, isLastReady);
    }

    public int loadCutCardsSeat() {
        if (state == SharedConstants.table_state.ready && fanCard <= 0 && isAllReady()) {
            if (playedBureau > 0) {
                for (Map.Entry<Integer, QianfenPlayer> kv : seatMap.entrySet()) {
                    if (kv.getValue().getResults().get(playedBureau - 1).get(4).intValue() == max_player_count) {
                        return kv.getKey().intValue();
                    }
                }
            } else if (masterId > 0) {
                return playerMap.get(masterId).getSeat();
            }
        }
        return 0;
    }

    public int getOnTablePlayerNum() {
        int num = 0;
        for (QianfenPlayer player : seatMap.values()) {
            if (player.getIsLeave() == 0) {
                num++;
            }
        }
        return num;
    }

    QianfenPlayer loadNextPlayer(QianfenPlayer player) {
        QianfenPlayer player1;
        int seat = player.getSeat() >= getMaxPlayerCount() ? 1 : player.getSeat() + 1;
        player1 = seatMap.get(seat);
        while (player1.isOver() && seat != player.getSeat()) {
            seat = seat >= getMaxPlayerCount() ? 1 : seat + 1;
            player1 = seatMap.get(seat);
        }
        return player1;
    }

    /**
     * 出牌
     *
     * @param player
     * @param cards
     */
    public boolean disCards(QianfenPlayer player, List<CardValue> cards) {
        if (cards.size() == 0) {
            return true;
        }

        CardUtils.Result cardResult = CardUtils.calcCardValue(cards, cutCardVals);
        if (cardResult.getType() <= 0) {
            LogUtil.errorLog.error("chupai error1:tableId=" + id + ",userId=" + player.getUserId() + ",playType=" + playType + ",playNum=" + playBureau + ",cards=" + cards);
            return false;
        }

        if (disCardRound > 0 && disCardSeat != player.getSeat() && cards.size() > 0) {
            boolean check = true;
            if (seatMap.get(disCardSeat).isOver() && CardUtils.searchBiggerCardValues(player.getHandPais0(), nowDisCardIds, cutCardVals).size() == 0) {
                check = false;
            }
            if (check) {
                CardUtils.Result cr = CardUtils.calcCardValue(nowDisCardIds, cutCardVals);
                if (cardResult.compareTo(cr) <= 0) {
                    player.writeErrMsg("要不起");
                    return false;
                }
            }
        }

        Map<Integer, Integer> map = CardUtils.countValue(cards);
        Map<Integer, Integer> map0 = CardUtils.countValue(player.getHandPais0());
        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
            Integer integer = map0.get(kv.getKey());
            if (integer == null || integer.intValue() < kv.getValue().intValue()) {
                player.writeErrMsg("出牌错误");
                return false;
            } else if ((integer.intValue() >= 4 && integer.intValue() != kv.getValue().intValue())) {
                player.writeErrMsg("炸弹不能拆");
                return false;
            }
        }

        if (!player.addOutPais(cards)) {
            LogUtil.errorLog.error("chupai error2:tableId=" + id + ",userId=" + player.getUserId() + ",playType=" + playType + ",playNum=" + playBureau + ",cards=" + cards + ",handPais=" + player.getHandPais());
            return false;
        }

        if (disCardRound == 0) {
            changeDisCardRound(1);
        }

        current_score += CardUtils.filterCardValueScore(cards, scoreCardList);

        //三飘 牌排序显示
        if (cardResult.getType() == 3) {
            List<CardValue> tempList = new ArrayList<>(cards.size());
            for (CardValue cv : cards) {
                if (cv.getValue() == cardResult.getMax()) {
                    tempList.add(0, cv);
                } else {
                    tempList.add(cv);
                }
            }
            cards = tempList;
        }
        //飞机 牌排序显示
        else if (cardResult.getType() == 33) {
            int len = cards.size();
            List<CardValue> tempList = new ArrayList<>(len);
            List<CardValue> copyList = new ArrayList<>(cards);

            int count = cardResult.getCount();
            for (int i = 0; i < len; i++) {
                int val = cardResult.getMax() - i;
                if (val <= 0) {
                    break;
                }
                int[] idxs = new int[]{-1, -1, -1};
                int j = 0;
                int m = copyList.size();
                for (int k = 0; k < m; k++) {
                    CardValue cv = copyList.get(k);
                    if (cv.getValue() == val) {
                        idxs[j] = k;
                        j++;
                        if (j >= idxs.length) {
                            break;
                        }
                    }
                }
                if (j > 0) {
                    tempList.add(copyList.remove(idxs[0]));
                    tempList.add(copyList.remove(idxs[1] - 1));
                    tempList.add(copyList.remove(idxs[2] - 2));
                    count--;
                    if (count <= 0) {
                        Collections.sort(copyList, new Comparator<CardValue>() {
                            @Override
                            public int compare(CardValue o1, CardValue o2) {
                                return o1.getValue() - o2.getValue();
                            }
                        });
                        tempList.addAll(copyList);
                        cards = tempList;
                        break;
                    }
                }

            }
        }

        setDisCardSeat(player.getSeat());
        setNowDisCardIds(cards);
        setNowDisCardSeat(player.getSeat());

        // 构建出牌消息
        PlayCardRes.Builder res = PlayCardRes.newBuilder();
        res.addAllCardIds(CardUtils.loadCardIds(cards));
        res.setCardType(cardResult.getType());
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setIsPlay(2);
        res.setCurScore(current_score);
        res.addAllScoreCard(scoreCardList);
        res.setIsBt(player.getHandPais0().size());
        res.setIsFirstOut(0);

        boolean resultOver = true;
        int myCount = 0;
        int myCount1 = 0;


        Map<Integer,List<CardValue>> biggerCards = new HashMap<>(8);
        Map<Long, PlayCardRes.Builder> resMap = new HashMap<>(8);
        List<Integer> seatList = new ArrayList<>(getMaxPlayerCount());
        for (QianfenPlayer pdkPlayer : seatMap.values()) {
            PlayCardRes.Builder copy = res.clone();
            if (pdkPlayer.getUserId() == player.getUserId()) {
                resMap.put(pdkPlayer.getUserId(), copy);
                continue;
            }

            if (pdkPlayer.isOver()) {
                // 如果玩家出完了最后一张牌，不需要提示要不起
                copy.setIsLet(1);
            } else {
                List<CardValue> canPlayList = CardUtils.searchBiggerCardValues(pdkPlayer.getHandPais0(), cardResult, cutCardVals);
                if (canPlayList.size()>0) {
                    seatList.add(pdkPlayer.getSeat());
                    myCount++;
                    copy.setIsLet(1);
                    resultOver = false;
                    biggerCards.put(pdkPlayer.getSeat(),canPlayList);
                } else {
                    myCount1++;
                    copy.setIsLet(0);
                }
            }
            resMap.put(pdkPlayer.getUserId(), copy);
        }

        int nextSeat;
        if (myCount == 0) {
            int i = 1;
            nextSeat = player.getSeat();
            while (true) {
                if (seatMap.get(nextSeat).isOver()) {
                    i++;
                    if (i == getMaxPlayerCount()) {
                        break;
                    }
                } else {
                    setNowDisCardSeat(nextSeat);
                    break;
                }
                nextSeat = nextSeat >= getMaxPlayerCount() ? 1 : nextSeat + 1;
            }
            newRound=1;
        } else {
            nextSeat = player.getSeat() >= getMaxPlayerCount() ? 1 : player.getSeat() + 1;
            while (!seatList.contains(nextSeat)) {
                nextSeat = nextSeat >= getMaxPlayerCount() ? 1 : nextSeat + 1;
            }
            setNowDisCardSeat(nextSeat);
            newRound=0;
        }

        changeExtend();

        QianfenPlayer nextPlayer = loadNextPlayer(player);
        int ns = nextPlayer.getSeat();
        for (Map.Entry<Long, PlayCardRes.Builder> kv : resMap.entrySet()) {
            kv.getValue().setIsPlay(1);
            kv.getValue().setNextSeat(ns);
            QianfenPlayer player1 = playerMap.get(kv.getKey());

            if (ns == nowDisCardSeat && ns == player1.getSeat() && !seatList.contains(ns)) {
                kv.getValue().setIsFirstOut(1);
            }

            player1.writeSocket(kv.getValue().build());
        }
        while (myCount1 > 0) {
            if (ns == player.getSeat()) {
                break;
            }
            QianfenPlayer player1 = seatMap.get(ns);
            if (seatList.contains(player1.getSeat())) {
                break;
            }
            if (!player1.isOver()) {
                myCount1--;
                PlayCardRes.Builder res0 = PlayCardRes.newBuilder();
                res0.addAllCardIds(Collections.emptyList());
                res0.setCardType(0);
                res0.setUserId(player1.getUserId() + "");
                res0.setSeat(player1.getSeat());
                QianfenPlayer player2 = loadNextPlayer(player1);
                res0.setNextSeat(player2.getSeat());
                res0.setIsPlay(1);
                res0.setCurScore(current_score);
                res0.addAllScoreCard(scoreCardList);
                res0.setIsBt(player1.getHandPais0().size());
                res0.setIsFirstOut(0);

                for (QianfenPlayer player3 : seatMap.values()) {
                    if (player2.getSeat() == nowDisCardSeat && nowDisCardSeat == player3.getSeat()) {
                        if (!seatList.contains(nowDisCardSeat)) {
                            player3.writeSocket(res0.clone().setIsLet(1).setIsFirstOut(1).build());
                        } else {
                            player3.writeSocket(res0.clone().setIsLet(1).build());
                        }
                    } else {
                        player3.writeSocket(res0.clone().setIsLet(seatList.contains(player3.getSeat()) ? 1 : 0).build());
                    }
                }
            }
            ns = ns >= getMaxPlayerCount() ? 1 : ns + 1;
        }

        int num = 1;
        try {
            //处理牌型分
            int resultScore = CardUtils.loadResultScore(cardResult, 7, 100, boom_score_rule);
            if (resultScore > 0) {
                int score = 0;
                for (QianfenPlayer player1 : seatMap.values()) {
                    if (player1.getUserId() != player.getUserId()) {
                        int score0 = player1.getResults().get(playedBureau).get(2);
                        player1.getResults().get(playedBureau).set(2, score0 - resultScore);
                        score += resultScore;
                    }
                }
                int score0 = player.getResults().get(playedBureau).get(2);
                score0 += score;
                player.getResults().get(playedBureau).set(2, score0);

                changePlayers();

                for (QianfenPlayer player1 : seatMap.values()) {
                    int s1 = player1.getResults().get(playedBureau).get(1);
                    score0 = player1.getResults().get(playedBureau).get(2);
                    int s = score0 + s1;
                    int totalBoomScore = player1.calcBoomScore();
                    for (QianfenPlayer player2 : seatMap.values()) {
                        player2.writeComMessage(WebSocketMsgType.res_code_qf_refreshF, (int) player1.getUserId(), s, s1, score0, current_score, totalBoomScore);
                    }
                }
            }

            //处理桌面上的牌分
            if (resultOver) {
                //第几局，牌面分，炸弹分，奖惩分，名次
                if (current_score > 0) {
                    int score0 = player.getResults().get(playedBureau).get(1);
                    score0 += current_score;
                    player.getResults().get(playedBureau).set(1, score0);
                    current_score = 0;
                    scoreCardList = new ArrayList<>();
                    changePlayers();
                    changeExtend();

                    int s1 = player.getResults().get(playedBureau).get(2);
                    int s = score0 + s1;
                    int totalBoomScore = player.calcBoomScore();
                    for (QianfenPlayer player1 : seatMap.values()) {
                        //2本局得分，3喜分，4牌面分（没实际意义），
                        player1.writeComMessage(WebSocketMsgType.res_code_qf_refreshF, (int) player.getUserId(), s, score0, s1, current_score, totalBoomScore);
                    }
                }
            }

            for (QianfenPlayer player1 : seatMap.values()) {
                if (player1.getUserId() != player.getUserId() && player1.isOver()) {
                    num++;
                }
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }

        boolean isOver = num == max_player_count;

        try {
            if (player.isOver() || isOver) {
                if (resultOver && (num + 1 == max_player_count)) {
                    isOver = true;
                    for (QianfenPlayer player1 : seatMap.values()) {
                        if (player1.getHandPais().size() > 0) {
                            player1.getResults().get(playedBureau).set(4, max_player_count);
                            if (player1.getUserId() == player.getUserId()) {
                                num = max_player_count;
                            }
                            break;
                        }
                    }
                }
                player.getResults().get(playedBureau).set(4, num);
                changePlayers();

                for (QianfenPlayer player1 : seatMap.values()) {
                    player1.writeComMessage(WebSocketMsgType.res_code_qf_refreshM, (int) player.getUserId(), num);
                }
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }

        if (isOver) {
            changeTableState(table_state.over);
            try {
                for (QianfenPlayer player1 : seatMap.values()) {
                    if (player1.getResults().get(playedBureau).get(4).intValue() == 1) {
                        int score = player1.getResults().get(playedBureau).get(1);
                        score += CardUtils.loadCardScore(leftCardList);
                        player1.getResults().get(playedBureau).set(1, score);
                        player1.getResults().get(playedBureau).set(3, peiScore[0] + peiScore[1]);
                    } else if (player1.getResults().get(playedBureau).get(4).intValue() == 2) {
                        player1.getResults().get(playedBureau).set(3, -peiScore[0]);
                    } else if (player1.getResults().get(playedBureau).get(4).intValue() == 3) {
                        player1.getResults().get(playedBureau).set(3, -peiScore[1]);
                    }
                }
                changePlayers();

                changeExtend();
            } catch (Exception e) {
                LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
            }
        }

        QianfenPlayer nextQianfenPlayer = seatMap.get(nextSeat);

        LogUtil.msgLog.info("qianfen tableId={},playNum={},playType={},userId={},seat={},nextUser={},nextSeat={}:{},resultOver={},over={},biggerCards={}"
                ,id,playBureau,playType,player.getUserId(),player.getSeat(),nextQianfenPlayer.getUserId(),nextQianfenPlayer.getSeat(),nowDisCardSeat,resultOver,isOver,biggerCards.get(nextQianfenPlayer.getSeat()));

        return true;
    }

    /**
     * 打牌
     *
     * @param player
     * @param cards
     */
    public void playCommand(QianfenPlayer player, List<Integer> cards) {

        synchronized (lock) {
            if (player.getSeat() != getNextDisCardSeat() || getState() != table_state.play) {
                return;
            }

            StringBuilder sb = new StringBuilder("QianFen");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.isAutoPlay() ? 1 : 0);
            sb.append("|").append("chuPai");
            sb.append("|").append(cards);
            LogUtil.msgLog.info(sb.toString());
            if (disCardRound > 0) {
                changeDisCardRound(1);
            }
            if (cards != null && !cards.isEmpty()) {
                // 出牌了
                if(disCards(player, cards.contains(0) ? Collections.emptyList() : CardUtils.loadCards(cards))){
                    addPlayLog(player.getSeat() + "_" + StringUtil.implode(cards, ","));
                    // /////////////////////////////////////////////////////
                    setLastActionTime(TimeUtil.currentTimeMillis());

                    if (isOver()) {
                        calcOver();
                    }
                }
            }
        }

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
        for (QianfenPlayer player : seatMap.values()) {
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
        setNowDisCardIds(new ArrayList<>());
        nowDisCardSeat = lastWinSeat;
        current_score = 0;
        scoreCardList = new ArrayList<>();
        timeNum=0;
        changeExtend();
        newRound=1;
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
        List<Integer> leftList = new ArrayList<>();
        for (int i = 1; i <= getMaxPlayerCount(); i++) {
            leftList.add(seatMap.get(i).getHandPais0().size());
        }
        for (Player tablePlayer : getSeatMap().values()) {
            if (userId == tablePlayer.getUserId()) {
                continue;
            }
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            res.addAllHandCardIds(tablePlayer.getHandPais());
            res.setNextSeat(nextSeat);
            res.setGameType(getWanFa());
            res.addAllXiaohu(leftList);
            tablePlayer.writeSocket(res.build());
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

    public List<CardValue> getNowDisCardIds() {
        return nowDisCardIds;
    }

    public void setNowDisCardIds(List<CardValue> nowDisCardIds) {
        if (nowDisCardIds == null) {
            this.nowDisCardIds.clear();
        } else {
            this.nowDisCardIds = nowDisCardIds;

        }
        dbParamMap.put("nowDisCardIds", JSON_TAG);
    }

    @Override
    protected void initNowAction(String nowAction) {

    }

    @Override
    public void initExtend0(JsonWrapper wrapper) {

        pairs = wrapper.getInt(1, 3);

        max_player_count = wrapper.getInt(2, 3);
        if (max_player_count == 0) {
            max_player_count = 3;
        }
        showCardNumber = wrapper.getInt(3, 0);
        over_score = wrapper.getInt(4, over_score);
        String temp = wrapper.getString(5);
        if (temp != null) {
            cutCardList = convert2IntLists(temp);
        }
        temp = wrapper.getString(6);
        if (temp != null) {
            leftCardList = convert2IntLists(temp);
        }
        temp = wrapper.getString(7);
        if (temp != null) {
            peiScore = convert2Ints(temp);
        }
        temp = wrapper.getString(8);
        if (temp != null) {
            cutCardVals = convert2Ints(temp);
        }
        random_start = wrapper.getInt(9, random_start);
        current_score = wrapper.getInt(10, current_score);
        over_reward = wrapper.getInt(11, over_reward);
        boom_score_rule = wrapper.getInt(12, boom_score_rule);

        overGame = wrapper.getInt(13, 0);
        temp = wrapper.getString(14);
        if (temp != null) {
            scoreCardList = convert2IntLists(temp);
        }
        cutCardMark = wrapper.getInt(15, cutCardMark);
        peiMark = wrapper.getInt(16, peiMark);
        fanCard = wrapper.getInt(17, fanCard);
        if (payType == -1) {
            String isAAStr = wrapper.getString("isAAConsume");
            if (!StringUtils.isBlank(isAAStr)) {
                this.payType = Boolean.parseBoolean(wrapper.getString("isAAConsume")) ? 1 : 2;
            } else {
                payType = 1;
            }
        }
        groupPaylogId = wrapper.getLong(18, 0);
        autoTimeOut2=autoTimeOut = wrapper.getInt(19, autoTimeOut);
        autoPlayGlob = wrapper.getInt(20, autoPlayGlob);
        finishFapai = wrapper.getInt(21, finishFapai);
        for (QianfenPlayer player:seatMap.values()) {
            if(player.getHandPais().size()!=0)
                finishFapai=1;
        }
        newRound = wrapper.getInt(22, newRound);
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
     * @param over      是否已经结束
     * @param winPlayer 赢的玩家
     * @return
     */
    public ClosingInfoRes.Builder sendAccountsMsg(boolean over, Player winPlayer, boolean isBreak) {
        List<ClosingPlayerInfoRes.Builder> builderList = new ArrayList<>();
        //大结算计算加倍分
        if(over && jiaBei == 1){
            int jiaBeiPoint = 0;
            int loserCount = 0;
            for (QianfenPlayer player : seatMap.values()) {
                if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                    jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
                    player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
                } else if (player.getTotalPoint() < 0) {
                    loserCount++;
                }
            }
            if (jiaBeiPoint > 0) {
                for (QianfenPlayer player : seatMap.values()) {
                    if (player.getTotalPoint() < 0) {
                        player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                    }
                }
            }
        }
        //大结算低于below分+belowAdd分
        if(over&&belowAdd>0&&playerMap.size()==2){
            for (QianfenPlayer player : seatMap.values()) {
                int totalPoint = player.getTotalPoint();
                if (totalPoint >-below&&totalPoint<0) {
                    player.setTotalPoint(player.getTotalPoint()-belowAdd);
                }else if(totalPoint < below&&totalPoint>0){
                    player.setTotalPoint(player.getTotalPoint()+belowAdd);
                }
            }
        }

        List<ClosingPlayerInfoRes> list = new ArrayList<>();
        if (over) {
            Map<Long, Integer> scoreMap = new HashMap<>();
            Map<Long, Integer> score0Map = new HashMap<>();
            for (QianfenPlayer player : seatMap.values()) {
                int score = player.calcCardAndRankAndGiveScore();
                score = ((score / 100) + (Math.abs((score % 100)) >= 50 ? (score>0?1:-1) : 0)) * 100;
                scoreMap.put(player.getUserId(), score);
            }
            int winPlayerScore = scoreMap.get(winPlayer.getUserId());
            int winScore = 0;
            for (Map.Entry<Long, Integer> kv : scoreMap.entrySet()) {
                if (kv.getKey().longValue() != winPlayer.getUserId()) {
                    QianfenPlayer player = playerMap.get(kv.getKey());
                    int score = kv.getValue().intValue() - winPlayerScore + player.calcBoomScore();
                    score0Map.put(kv.getKey(), score);
                    player.setTotalPoint(score);
                    winScore += score;
                }
            }
            score0Map.put(winPlayer.getUserId(), -winScore);
            winPlayer.setTotalPoint(-winScore);

            LogUtil.msgLog.info("qianfen result tableId=" + id + ",socres1=" + scoreMap + ",scores2=" + score0Map + ",winId=" + winPlayer.getUserId());
        }
        for (QianfenPlayer player : seatMap.values()) {
            ClosingPlayerInfoRes.Builder build;
            if (over) {
                build = player.bulidTotalClosingPlayerInfoRes();
            } else {
                build = player.bulidOneClosingPlayerInfoRes();
            }

            if (winPlayer != null && player.getUserId() == winPlayer.getUserId()) {
                // 手上没有剩余的牌放第一位为赢家
                builderList.add(0,build);
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
            for (QianfenPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingPlayerInfoRes.Builder builder : builderList) {
                QianfenPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (QianfenPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingPlayerInfoRes.Builder builder : builderList) {
                QianfenPlayer player = seatMap.get(builder.getSeat());
                builder.setWinLoseCredit(player.getWinGold());
            }
        }

        for (ClosingPlayerInfoRes.Builder builder : builderList) {
            list.add(builder.build());
        }


        ClosingInfoRes.Builder res = ClosingInfoRes.newBuilder();
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.addAllClosingPlayers(list);
        res.addAllExt(buildAccountsExt(over));
        res.setGroupLogId((int)groupPaylogId);
        res.addAllCutCard(this.cutCardList);
        res.addAllBird(leftCardList);
        res.addAllIntParams(getIntParams());
        for (QianfenPlayer player : seatMap.values()) {
            player.writeSocket(res.build());
        }
        return res;
    }

    public List<String> buildAccountsExt(boolean over) {
        List<String> ext = new ArrayList<>();
        ext.add(id + "");
        ext.add(masterId + "");
        ext.add(TimeUtil.formatTime(TimeUtil.now()));
        ext.add(playType + "");
        //设置当前第几局
        ext.add(playBureau + "");
        ext.add(playedBureau + "");
        ext.add(over ? "1" : "0");//6
        ext.add(isGroupRoom()?loadGroupId():"0");//7
        return ext;
    }

    @Override
    public void sendAccountsMsg() {
        QianfenPlayer winPlayer0 = null;
        int maxPoint = 0;
        for (QianfenPlayer player : seatMap.values()) {
            int totalPoint = player.calcCardAndRankScore();
            if (totalPoint > maxPoint || winPlayer0 == null) {
                winPlayer0 = player;
                maxPoint = totalPoint;
            } else if (totalPoint == maxPoint) {
                if (winPlayer0.calcBoomScore() < player.calcBoomScore()) {
                    winPlayer0 = player;
                }
            }
        }
        if (winPlayer0 == null) {
            return;
        }
        ClosingInfoRes.Builder builder = sendAccountsMsg(true, winPlayer0, true);
        saveLog(true, winPlayer0.getUserId(), builder.build());
    }

    @Override
    public Class<? extends Player> getPlayerClass() {
        return QianfenPlayer.class;
    }

    @Override
    public int getWanFa() {
        return playType;
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
        if (startXjTime > 0) {
            Integer timeout = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "qfQp_timeout");
            if (System.currentTimeMillis() - startXjTime >= (timeout == null ? 10 * 1000L : timeout.longValue()) && loadCutCardsSeat() > 0) {
                boolean fapai = false;
                synchronized (lock) {
                    int seat = loadCutCardsSeat();
                    if (seat > 0) {
                        List<Integer> cardsList = CardUtils.loadCards(pairs, cutCardVals);
                        int card = cardsList.get(new SecureRandom().nextInt(cardsList.size()));
                        setFanCard(card);
                        fapai = true;
                        Player player = seatMap.get(seat);
                        for (Player player1 : getPlayerMap().values()) {
                            player1.writeComMessage(1121, (int) player.getUserId(), player.getSeat(), new SecureRandom().nextInt(100)+1, 1, card);
                        }
                    }
                }

                if (fapai) {
                    this.ready();
                    this.checkDeal();
                    this.startNext();
                }
            }
        }
        checkAutoPlay();
    }

    public synchronized void checkDeal(long userId) {
        if (isAllReady()) {

            // ------ 开局前检查信用分是否足够----------------
            if(!checkCreditOnTableStart()){
                return;
            }
            // 发牌
            if(fapai0()){
                setLastActionTime(TimeUtil.currentTimeMillis());
                for (int i = 1; i <= getMaxPlayerCount(); i++) {
                    Player player = getSeatMap().get(i);
                    addPlayLog(StringUtil.implode(player.getHandPais(), ","));
                }
                // 发牌msg
                sendDealMsg(userId);
                robotDealAction();

                updateGroupTableDealCount();
                calcCoinOnStart();

                // 私密房，开局后，创建好友关系
                genGroupUserFriend();
            }
        } else {
            robotDealAction();
        }
    }

    @Override
    public void checkAutoPlay() {
        synchronized (lock) {
            second++;
            if (!autoPlay) {
                return;
            }
            // 发起解散，停止倒计时
            if (getSendDissTime() > 0) {
                for (QianfenPlayer player : seatMap.values()) {
                    if (player.getLastCheckTime() > 0) {
                        player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                    }
                }
                return;
            }

            if (isAutoPlayOff()) {
                // 托管关闭
                for (int seat : seatMap.keySet()) {
                    QianfenPlayer player = seatMap.get(seat);
                    player.setAutoPlay(false, this);
                    player.setLastOperateTime(System.currentTimeMillis());
                }
                return;
            }

            // 准备托管
            if (state == table_state.ready && playedBureau > 0) {
                ++timeNum;
                for (QianfenPlayer player : seatMap.values()) {
                    // 玩家进入托管后，5秒自动准备
                    if (timeNum >= 5 && player.isAutoPlay()) {
                        autoReady(player);
                    } else if (timeNum >= 30) {
                        autoReady(player);
                    }
                }
                return;
            }

            if(finishFapai==0||second%2==0)
                return;
            QianfenPlayer player = seatMap.get(getNextDisCardSeat());
            if (player == null) {
                return;
            }
            boolean auto=player.isAutoPlay();
            if(!player.isAutoPlay()){
                auto=checkPlayerAuto(player,autoTimeOut);
            }
            if(!auto)
                return;
            List<CardValue> outCards=new ArrayList<>();
            if(newRound==1){
                Map<Integer, Integer> map = CardUtils.countValue(player.getHandPais0());
                for (Map.Entry<Integer, Integer> entry:map.entrySet()) {
                    if(entry.getValue()<4){
                        outCards=CardUtils.searchCardValues(player.getHandPais0(),entry.getKey(),1);
                        break;
                    }
                }
                if(outCards.size()==0){
                    for (Map.Entry<Integer, Integer> entry:map.entrySet()) {
                        outCards=CardUtils.searchCardValues(player.getHandPais0(),entry.getKey(),entry.getValue());
                    }
                }
            }else {
                if(nowDisCardIds.size()==0)
                    return;
                CardUtils.Result cardResult = CardUtils.calcCardValue(nowDisCardIds, cutCardVals);
                outCards = CardUtils.searchBiggerCardValues(player.getHandPais0(), cardResult, cutCardVals);
            }
            playCommand(player,CardUtils.loadCardIds(outCards));
        }
    }

    public boolean checkPlayerAuto(QianfenPlayer player ,int timeout){
        if(player.isAutoPlay())
            return true;
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

    public int getShowCardNumber() {
        return showCardNumber;
    }

    public void setShowCardNumber(int showCardNumber) {
        this.showCardNumber = showCardNumber;
        changeExtend();
    }


    @Override
    public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams,
                            Object... objects) throws Exception {
        createTable(player, play, bureauCount, params);
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {

    }

    /**
     * 发送记牌器消息
     *
     * @param player
     */
    public void sendCardMarker(QianfenPlayer player) {
        ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_qf_refreshJ, cardMarkerToJSON());
        player.writeSocket(com.build());
    }

    private String cardMarkerToJSON() {
        JSONObject jsonObject = new JSONObject();
        synchronized (lock) {
            for (Map.Entry<Integer, QianfenPlayer> entry : seatMap.entrySet()) {
                jsonObject.put("" + entry.getKey(), entry.getValue().getOutPais());
            }
        }
        return jsonObject.toString();
    }

    @Override
    public int loadOverValue() {
        return over_score;
    }

    @Override
    public int loadOverCurrentValue() {
        int temp = 0;
        for (Map.Entry<Long,QianfenPlayer> kv : playerMap.entrySet()){
            int tmp = kv.getValue().calcCardAndRankScore();
            if (tmp > temp){
                temp = tmp;
            }
        }
        return temp;
    }

    public String getTableMsg() {

        Map<String, Object> json = new HashMap<>();
        json.put("wanFa", "千分");
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

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_qf);

    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
        for (Integer integer : wanfaList) {
            TableManager.wanfaTableTypesPut(integer, cls);
        }
    }
}
