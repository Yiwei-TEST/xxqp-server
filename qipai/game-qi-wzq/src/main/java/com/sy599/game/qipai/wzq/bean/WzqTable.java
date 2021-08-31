package com.sy599.game.qipai.wzq.bean;

import com.alibaba.fastjson.JSON;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.PlayCardResMsg;
import com.sy599.game.msg.serverPacket.TableRes;
import com.sy599.game.msg.serverPacket.TableRes.ClosingInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.ClosingPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.wzq.tool.WzqUtil;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.Constants;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.PayConfigUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.util.constants.GroupConstants;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;


public class WzqTable extends BaseTable {

    public static final int COST_TYPE_CREDIT = 1;
    public static final int COST_TYPE_SCORE = 2;


    /*** 人数***/
    private int maxPlayerCount = 2;

    /*** 玩家map */
    private Map<Long, WzqPlayer> playerMap = new ConcurrentHashMap<>();

    /*** 座位对应的玩家 */
    private Map<Integer, WzqPlayer> seatMap = new ConcurrentHashMap<>();

    /*** 使用分值***/
    private long costValue = 0;

    /*** 牌桌上的棋，0：未放棋，-1：黑棋，1：棋***/
    private int[][] qiPan = new int[15][15];

    /*** 当前走棋的人***/
    private int nowSeat;

    /*** 下一个走棋的人***/
    private int nextSeat;

    /*** 赢家***/
    private WzqPlayer winner;

    /*** 输家***/
    private WzqPlayer loser;

    public long getCostValue() {
        return costValue;
    }

    public void setCostValue(long costValue) {
        this.costValue = costValue;
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, Object... objects) throws Exception {

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
        // 0局数，1玩法Id
        payType = StringUtil.getIntValue(params, 2, 1);   // 支付方式
        maxPlayerCount = StringUtil.getIntValue(params, 7, 2);  // 人数

        costValue = StringUtil.getIntValue(params, 8, 10); // 分值
        if (costValue > GroupConstants.DEFAULT_SCORE) {
            costValue = GroupConstants.DEFAULT_SCORE;
        } else if (costValue < 1) {
            costValue = 1;
        }

        // 信用分是乘以100的模式
        credit100 = 1;
        changeExtend();
    }

    @Override
    public JsonWrapper buildExtend0(JsonWrapper wrapper) {
        for (WzqPlayer player : seatMap.values()) {
            wrapper.putString(player.getSeat(), player.toExtendStr());
        }
        wrapper.putInt(1, maxPlayerCount);
        wrapper.putLong(2, costValue);
        wrapper.putInt(3, nowSeat);
        return wrapper;
    }

    @Override
    public void initExtend0(JsonWrapper wrapper) {
        for (WzqPlayer player : seatMap.values()) {
            player.initExtend(wrapper.getString(player.getSeat()));
        }
        maxPlayerCount = wrapper.getInt(1, 2);
        costValue = wrapper.getLong(2, 10);
        nowSeat = wrapper.getInt(3, 0);

    }


    @Override
    protected boolean quitPlayer1(Player player) {
        return false;
    }

    @Override
    protected boolean joinPlayer1(Player player) {
        ready(player);
        return false;
    }

    @Override
    public int isCanPlay() {
        return 0;
    }

    public void saveLog(boolean over, long winId, Object resObject) {
        ClosingInfoRes res = (ClosingInfoRes) resObject;
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
        userLog.setExtend(logOtherRes);
        userLog.setType(creditMode == 1 ? 2 : 1);
        userLog.setMaxPlayerCount(maxPlayerCount);
        userLog.setGeneralExt(buildGeneralExtForPlaylog().toString());
        long logId = TableLogDao.getInstance().save(userLog);
        saveTableRecord(logId, over, playBureau);
        for (WzqPlayer player : playerMap.values()) {
            player.addRecord(logId, playBureau);
        }
        UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
    }

    public String getMasterName() {
        Player master = PlayerManager.getInstance().getPlayer(creatorId);
        String masterName = "";
        if (master == null) {
            masterName = UserDao.getInstance().selectNameByUserId(creatorId);
        } else {
            masterName = master.getName();
        }
        return masterName;
    }


    public void qiPanFromDb(String qiPanStr) {
        String[] splits = qiPanStr.split(",");
        for (int i = 0; i < splits.length; i++) {
            int x = i / 15;
            int y = i % 15;
            qiPan[x][y] = Integer.valueOf(splits[i]);
        }
    }

    public String qiPanToString() {
        StringJoiner sj = new StringJoiner(",");
        for (int x = 0; x < qiPan.length; x++) {
            for (int y = 0; y < qiPan[x].length; y++) {
                sj.add(String.valueOf(qiPan[x][y]));
            }
        }
        return sj.toString();
    }

    @Override
    protected void loadFromDB1(TableInf info) {
        if (!StringUtils.isBlank(info.getNowDisCardIds())) {
            qiPanFromDb(info.getNowDisCardIds());
        }
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
            if (tempMap.containsKey("nowDisCardIds")) {
                tempMap.put("nowDisCardIds", qiPanToString());
            }
            if (tempMap.containsKey("answerDiss")) {
                tempMap.put("answerDiss", buildDissInfo());
            }
            if (tempMap.containsKey("extend")) {
                tempMap.put("extend", buildExtend());
            }
        }
        return tempMap.size() > 0 ? tempMap : null;
    }


    @Override
    public int getPlayerCount() {
        return playerMap.size();
    }

    @Override
    public boolean isAllReady() {
        if (!super.isAllReady()) {
            return false;
        }
        boolean res = true;
        for (WzqPlayer player : seatMap.values()) {
            if (player.getCostType() == 0) {
                ComMsg.ComRes msg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_wzq_cost_type).build();
                player.writeSocket(msg);
                res = false;
            }
        }
        if (res) {
            changeTableState(table_state.play);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void sendDealMsg() {
        sendDealMsg(0);
    }

    @Override
    protected void deal() {
        lastWinSeat = new Random().nextInt(maxPlayerCount) + 1;
        setDisCardSeat(lastWinSeat);
        setNowDisCardSeat(lastWinSeat);
    }

    @Override
    protected void sendDealMsg(long userId) {
        qiPan = new int[15][15];
        for (WzqPlayer player : seatMap.values()) {
            if (player.getSeat() == lastWinSeat) {
                player.setColor(-1);
            } else {
                player.setColor(1);
            }
            StringBuilder sb = new StringBuilder("Wzq");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append("sendDealMsg");
            sb.append("|").append(player.getCostType());
            sb.append("|").append(player.getColor());
            LogUtil.msgLog.info(sb.toString());
        }
        DealInfoRes.Builder dealMsg = DealInfoRes.newBuilder();
        dealMsg.setNextSeat(getNextDisCardSeat());
        dealMsg.setGameType(playType);
        dealMsg.setBanker(lastWinSeat);
        broadMsgToAll(dealMsg.build());
        changeExtend();
    }

    /**
     * 走棋
     *
     * @param player
     */
    public synchronized void play(WzqPlayer player, List<Integer> xy) {
        if (state != table_state.play) {
            return;
        }
        if (winner != null || loser != null) {
            return;
        }
        if (player.getSeat() != getNextDisCardSeat()) {
            player.writeErrMsg("没轮到你下棋");
            return;
        }
        int qiVal = player.getColor();
        int x = xy.get(0);
        int y = xy.get(1);

        if (!WzqUtil.isValid(x, y)) {
            return;
        }
        if (!WzqUtil.canPlay(qiPan, x, y)) {
            return;
        }

        // 走棋
        qiPan[x][y] = qiVal;
        changeNowDisCardIds();
        changeExtend();
        changeDisCardRound(1);
        setNowSeat(player.getSeat());


        PlayCardResMsg.PlayCardRes.Builder playMsg = PlayCardResMsg.PlayCardRes.newBuilder();
        playMsg.addCardIds(x);
        playMsg.addCardIds(y);
        playMsg.addCardIds(player.getColor());
        playMsg.setUserId(player.getUserId() + "");
        playMsg.setSeat(player.getSeat());
        playMsg.setIsPlay(1);
        broadMsgToAll(playMsg.build());

        StringBuilder sb = new StringBuilder("Wzq");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("play");
        sb.append("|").append(player.getCostType());
        sb.append("|").append(player.getColor());
        sb.append("|").append(xy);
        LogUtil.msgLog.info(sb.toString());

        boolean isOver = false;
        if (WzqUtil.isWin(qiPan, x, y)) {
            winner = player;
            isOver = true;

            StringBuilder winMsg = new StringBuilder("Wzq");
            winMsg.append("|").append(getId());
            winMsg.append("|").append(getPlayBureau());
            winMsg.append("|").append(player.getUserId());
            winMsg.append("|").append(player.getSeat());
            winMsg.append("|").append("winMsg");
            winMsg.append("|").append(qiPanToString());
            LogUtil.msgLog.info(winMsg.toString());
        }

        if (!isOver) {
            isOver = WzqUtil.isFull(qiPan);
        }

        if (isOver) {
            state = table_state.over;
            calcOver();
            return;
        }
    }

    public void choseCostType(WzqPlayer player, int costType) throws Exception {
        if (costType != COST_TYPE_CREDIT && costType != COST_TYPE_SCORE) {
            player.writeErrMsg(LangMsg.code_3);
            return;
        }

        GroupUser gu = GroupDao.getInstance().loadGroupUser(player.getUserId(), loadGroupId());
        if (costType == COST_TYPE_CREDIT) {
            if (gu == null || gu.getCredit() < costValue) {
                player.writeErrMsg("比赛分不足" + (costValue / 100) + "，请选择棋分！");
                return;
            }
        } else {
            if (gu.getScore() < GroupConstants.DEFAULT_SCORE) {
                GroupDao.getInstance().resetGroupUserScore(loadGroupId(), player.getUserId(), GroupConstants.DEFAULT_SCORE);
                return;
            }
        }
        player.setCostType(costType);
        ComMsg.ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_wzq_cost_type_value, player.getSeat(), costType);
        broadMsgToAll(build.build());

        StringBuilder sb = new StringBuilder("Wzq");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("choseCostType");
        sb.append("|").append(player.getCostType());
        sb.append("|").append(player.getColor());
        LogUtil.msgLog.info(sb.toString());

        int count = 0;
        for (WzqPlayer wzqP : seatMap.values()) {
            if (wzqP.getCostType() != 0) {
                count++;
            }
        }
        if (count >= getMaxPlayerCount()) {
            checkDeal();
        }
    }

    public void giveUp(WzqPlayer player) {
        ComMsg.ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_wzq_giveUp, player.getSeat());
        broadMsgToAll(build.build());


        StringBuilder sb = new StringBuilder("Wzq");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("giveUp");
        sb.append("|").append(player.getCostType());
        sb.append("|").append(player.getColor());
        LogUtil.msgLog.info(sb.toString());

        int winnerSeat = player.getSeat() == 1 ? 2 : 1;
        winner = seatMap.get(winnerSeat);
        loser = player;
        calcOver();
    }


    @Override
    public void calcOver() {
        boolean isOver = true;
        if (winner != null) {
            for (WzqPlayer player : seatMap.values()) {
                if (player.getUserId() == winner.getUserId()) {
                    continue;
                }
                loser = player;
            }

            int winPoint = Integer.valueOf(String.valueOf(costValue));

            winner.changePoint(winPoint);
            winner.setWinCount(winner.getWinCount() + 1);

            loser.changePoint(-winPoint);
            loser.setLostCount(loser.getLostCount() + 1);

            if (loser.getCostType() == COST_TYPE_CREDIT) {
                winner.setWinLoseCredit(costValue);
                winner.setCommissionCredit(0);

                loser.setWinLoseCredit(-costValue);
                loser.setCommissionCredit(0);
            }
        }
        calcAfter();
        ClosingInfoRes.Builder res = sendAccountsMsg(isOver, false);
        settleAccount();
        saveLog(isOver, 0l, res.build());
        calcOver1();
        calcOver2();
        diss();

        for (Player player : seatMap.values()) {
            player.saveBaseInfo();
        }
    }

    /**
     * 结算
     */
    public void settleAccount() {
        try {
            if (winner == null || loser == null || winner.getWinLoseCredit() <= 0 || loser.getWinLoseCredit() >= 0) {
                return;
            }
            String groupId = loadGroupId();
            Date now = new Date();

            if (loser.getCostType() == COST_TYPE_CREDIT) {

                // 信用分
                for (WzqPlayer player : seatMap.values()) {

                    player.changePoint(-player.getTotalPoint()); // 为了防止t_table_user上存playResult

                    GroupUser gu = getGroupUser(player.getUserId());
                    if (gu == null) {
                        continue;
                    }
                    int updateResult = 1;
                    if (player.getWinLoseCredit() != 0) {
                        updateResult = updateGroupCredit(groupId, player.getUserId(), player.getSeat(), player.getWinLoseCredit());
                    }
                    HashMap<String, Object> log = new HashMap<>();
                    log.put("groupId", groupId);
                    log.put("userId", player.getUserId());
                    log.put("optUserId", player.getUserId());
                    log.put("tableId", getId());
                    log.put("credit", player.getWinLoseCredit());
                    log.put("type", Constants.CREDIT_LOG_TYPE_TABLE);
                    log.put("flag", updateResult);
                    log.put("promoterId1", gu.getPromoterId1());
                    log.put("promoterId2", gu.getPromoterId2());
                    log.put("promoterId3", gu.getPromoterId3());
                    log.put("promoterId4", gu.getPromoterId4());
                    log.put("promoterId5", gu.getPromoterId5());
                    log.put("promoterId6", gu.getPromoterId6());
                    log.put("promoterId7", gu.getPromoterId7());
                    log.put("promoterId8", gu.getPromoterId8());
                    log.put("promoterId9", gu.getPromoterId9());
                    log.put("promoterId10", gu.getPromoterId10());
                    log.put("roomName", StringUtils.isNotBlank(roomName) ? roomName : "");
                    log.put("createdTime", now);
                    log.put("groupTableId", groupTable != null ? groupTable.getKeyId() : 0);
                    GroupDao.getInstance().insertGroupCreditLog(log);
                }
            } else {
                for (Player player : getSeatMap().values()) {
                    GroupUser gu = getGroupUser(player.getUserId());
                    if (gu == null) {
                        continue;
                    }
                    int updateResult = 1;
                    if (player.getTotalPoint() != 0) {
                        updateResult = GroupDao.getInstance().updateGroupScore(groupId, player.getUserId(), player.getTotalPoint());
                    }
                }

            }
        } catch (Exception e) {
            LogUtil.errorLog.error("Wzq|settleAccount|error|" + getId() + "|" + getPlayBureau() + "|" + groupId, e);
        }
    }

    @Override
    protected void robotDealAction() {
    }


    @Override
    public void startNext() {
    }

    @Override
    public int getNextDisCardSeat() {
        if (state != table_state.play) {
            return 0;
        }
        if (disCardRound == 0) {
            return lastWinSeat;
        } else {
            return nowSeat == 1 ? 2 : 1;
        }
    }

    /**
     * 计算seat右边的座位
     *
     * @param seat
     * @return
     */
    public int calcNextSeat(int seat) {
        return seat + 1 > maxPlayerCount ? 1 : seat + 1;
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
    public CreateTableRes buildCreateTableRes(long userId, boolean isrecover, boolean isLastReady) {
        CreateTableRes.Builder tableMsg = CreateTableRes.newBuilder();
        buildCreateTableRes0(tableMsg);
        tableMsg.setNowBurCount(getPlayBureau());
        tableMsg.setTotalBurCount(getTotalBureau());
        tableMsg.setGotyeRoomId(gotyeRoomId + "");
        tableMsg.setTableId(getId() + "");
        tableMsg.setWanfa(playType);
        for (WzqPlayer player : playerMap.values()) {
            PlayerInTableRes.Builder playerMsg = player.buildPlayInTableInfo(isrecover);
            playerMsg.addRecover(player.getIsEntryTable());
            playerMsg.addRecover(player.getSeat() == lastWinSeat ? 1 : 0);
            tableMsg.addPlayers(playerMsg);
        }
        tableMsg.setRenshu(getMaxPlayerCount());
        tableMsg.setLastWinSeat(getLastWinSeat());
        tableMsg.setNextSeat(getNextDisCardSeat());
        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 15; y++) {
                int val = qiPan[x][y];
                if (val != 0) {
                    TableRes.QiZi.Builder qiZi = TableRes.QiZi.newBuilder();
                    qiZi.setX(x);
                    qiZi.setY(y);
                    qiZi.setVal(val);
                    tableMsg.addQiPan(qiZi);
                }
            }
        }

        tableMsg.addExt(payType);  //0
        tableMsg.addExt(state.getId());

        tableMsg.addExtStr(costValue + ""); // 0

        return tableMsg.build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getPlayer(long id, Class<T> cl) {
        return (T) playerMap.get(id);
    }

    @Override
    public int getMaxPlayerCount() {
        return maxPlayerCount;
    }

    public void setMaxPlayerCount(int maxPlayerCount) {
        this.maxPlayerCount = maxPlayerCount;
    }

    @Override
    public int loadAgreeCount() {
        // 有人申请就解散
        return 1;
    }

    @Override
    public Map<Long, Player> getPlayerMap() {
        Object o = playerMap;
        return (Map<Long, Player>) o;
    }

    @Override
    protected void initNext1() {
        qiPan = new int[15][15];
        winner = null;
        loser = null;
    }

    public void saveActionSeatMap() {
        dbParamMap.put("nowAction", JSON_TAG);
    }

    @Override
    protected void initNowAction(String nowAction) {
        JsonWrapper wrapper = new JsonWrapper(nowAction);
    }

    public void setNowSeat(int nowSeat) {
        this.nowSeat = nowSeat;

        changeExtend();
    }

    public void changeNowDisCardIds() {
        dbParamMap.put("nowDisCardIds", JSON_TAG);
    }

    public int getNowSeat() {
        return nowSeat;
    }

    @Override
    protected String buildNowAction() {
        return "";
    }

    @Override
    public void setConfig(int index, int val) {

    }

    /**
     * @param over
     * @return
     */
    public ClosingInfoRes.Builder sendAccountsMsg(boolean over, boolean isBreak) {

        List<ClosingPlayerInfoRes> list = new ArrayList<>();
        List<ClosingPlayerInfoRes.Builder> builderList = new ArrayList<>();
        for (WzqPlayer player : seatMap.values()) {
            ClosingPlayerInfoRes.Builder build = null;
            if (over) {
                build = player.buildTotalClosingPlayerInfoRes();
            } else {
                build = player.buildOneClosingPlayerInfoRes();
            }
            if (winner != null && winner.getUserId() == player.getUserId()) {
                builderList.add(0, build);
            } else {
                builderList.add(build);
            }
        }
        if (winner != null && loser != null) {
            GroupUser loserGu = getGroupUser(loser.getUserId());
            if (loserGu != null) {
                // 检查分
                if (loser.getCostType() == COST_TYPE_CREDIT) {
                    if (loserGu.getCredit() > 0) {
                        if (loserGu.getCredit() < costValue) {
                            winner.setWinLoseCredit(loserGu.getCredit());
                            loser.setWinLoseCredit(-loserGu.getCredit());
                        }
                    } else {
                        winner.setWinLoseCredit(0);
                        loser.setWinLoseCredit(0);
                    }
                } else {
                    if (loserGu.getScore() > 0) {
                        if (loserGu.getScore() < costValue) {
                            winner.setWinLoseCredit(loserGu.getScore());
                            loser.setWinLoseCredit(-loserGu.getScore());
                        }
                    } else {
                        winner.setWinLoseCredit(0);
                        loser.setWinLoseCredit(0);
                    }
                }
            } else {
                winner.setWinLoseCredit(0);
                loser.setWinLoseCredit(0);
            }
        }

        for (ClosingPlayerInfoRes.Builder builder : builderList) {
            WzqPlayer player = seatMap.get(builder.getSeat());
            builder.setWinLoseCredit(player.getWinLoseCredit());
            builder.setCommissionCredit(player.getCommissionCredit());
            list.add(builder.build());
        }

        ClosingInfoRes.Builder res = ClosingInfoRes.newBuilder();
        res.addAllClosingPlayers(list);
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.addAllExt(buildAccountsExt(over ? 1 : 0));
        broadMsgToAll(res.build());
        return res;

    }


    public List<String> buildAccountsExt(int over) {
        List<String> ext = new ArrayList<>();
        if (isGroupRoom()) {
            ext.add(loadGroupId());
        } else {
            ext.add("0");
        }
        ext.add(id + "");  //1
        ext.add(masterId + ""); //2
        ext.add(TimeUtil.formatTime(TimeUtil.now()));    //3
        ext.add(playType + "");  //4
        ext.add(lastWinSeat + ""); //5
        ext.add(over + ""); //6
        ext.add((loser != null ? loser.getUserId() : 0) + ""); // 7
        ext.add((loser != null ? loser.getCostType() : 0) + ""); // 8
        return ext;
    }

    @Override
    public void sendAccountsMsg() {
        ClosingInfoRes.Builder builder = sendAccountsMsg(true, true);
        saveLog(true, 0l, builder.build());
    }

    @Override
    public Class<? extends Player> getPlayerClass() {
        return WzqPlayer.class;
    }

    @Override
    public int getWanFa() {
        return getPlayType();
    }

    @Override
    public void checkReconnect(Player player) {
        if (state == table_state.ready) {
            WzqPlayer wzqPlayer = (WzqPlayer) player;
            if (wzqPlayer.getCostType() == 0) {
                ComMsg.ComRes msg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_wzq_cost_type).build();
                player.writeSocket(msg);
            }
        }
    }

    @Override
    public boolean consumeCards() {
        return SharedConstants.consumecards;
    }

    @Override
    public void checkAutoPlay() {

    }


    /**
     * 自动出牌
     */
    public synchronized void autoPlay() {
        if (state != table_state.play) {
            return;
        }
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {
    }


    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_wzq);

    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
        for (Integer integer : wanfaList) {
            TableManager.wanfaTableTypesPut(integer, cls);
        }
    }

    public String getTableMsg() {
        Map<String, Object> json = new HashMap<>();
        json.put("wanFa", "转转麻将");
        if (isGroupRoom()) {
            json.put("roomName", getRoomName());
        }
        json.put("playerCount", getPlayerCount());
        json.put("count", getTotalBureau());
        return JSON.toJSONString(json);
    }

    @Override
    public String getGameName() {
        return "转转麻将";
    }
}
