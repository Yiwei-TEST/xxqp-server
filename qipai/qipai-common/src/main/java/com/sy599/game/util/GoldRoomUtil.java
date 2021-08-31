package com.sy599.game.util;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.GeneratedMessage;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.bean.gold.GoldRoomArea;
import com.sy599.game.db.bean.gold.GoldRoomConfig;
import com.sy599.game.db.bean.gold.GoldRoomGroupLimit;
import com.sy599.game.db.bean.gold.GoldRoomHall;
import com.sy599.game.db.bean.gold.GoldRoomUser;
import com.sy599.game.db.dao.DataStatisticsDao;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.db.enums.SourceType;
import com.sy599.game.jjs.bean.MatchBean;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.TableRes;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public final class GoldRoomUtil {

    public static final Map<Long, GoldRoomConfig> CONFIG_MAP = new HashMap<>();
    public static final Map<Long, List<GoldRoomConfig>> AREA_CONFIG_MAP = new HashMap<>();
    public static final Map<Long, List<GoldRoomConfig>> HALL_CONFIG_MAP = new HashMap<>();
    public static final Map<Integer, List<GoldRoomConfig>> PLAY_TYPE_CONFIG_MAP = new HashMap<>();
    public static final List<GoldRoomArea> AREA_LIST = new CopyOnWriteArrayList<>();
    public static final List<GoldRoomHall> HALL_LIST = new CopyOnWriteArrayList<>();
    public static final Map<Long, String> GROUP_ID_LIMIT_MAP = new ConcurrentHashMap<>();

    /*** 金币场机器人单日总输阀值，到达此值后，当天不再投放机器人***/
    private static long goldRoomRobotTotalLose = 0;

    /*** 金币场机器人每日单个玩法总输阀值，单个玩法到达此值后，该玩法当天不再投放机器人 ***/
    private static final Map<Integer, Long> goldRoomRobotPlayTypeLoseMap = new ConcurrentHashMap<>();

    public static long giveGoldOnNew = 3000;

    public static void init() {
        initGoldRoomConfig();
        initGoldRoomArea();
        initGoldRoomGroupLimit();
        giveGoldOnNew = ResourcesConfigsUtil.loadServerConfigIntegerValue("give_gold_on_new", 3000);
        initGoldRoomHall();
        initGoldRoomRobotLose();
    }


    public static void initGoldRoomConfig() {
        try {
            List<GoldRoomConfig> all = GoldRoomDao.getInstance().loadAllGoldRoomConfig();
            if (all == null || all.size() == 0) {
                return;
            }
            Set<Long> areaIdSet = new HashSet<>();
            Set<Integer> playTypeSet = new HashSet<>();
            for (GoldRoomConfig config : all) {
                initGiftCertificate(config);
                CONFIG_MAP.put(config.getKeyId(), config);
                if (config.getState() == GoldRoomConfig.STATE_VALID) {
                    List<GoldRoomConfig> areaConfigList = AREA_CONFIG_MAP.get(config.getAreaId());
                    if (areaConfigList == null) {
                        areaConfigList = new CopyOnWriteArrayList<>();
                        AREA_CONFIG_MAP.put(config.getAreaId(), areaConfigList);
                    } else {
                        if (!areaIdSet.contains(config.getAreaId())) {
                            areaConfigList.clear();
                            areaIdSet.add(config.getAreaId());
                        }
                    }
                    areaConfigList.add(config);

                    List<GoldRoomConfig> playTypeConfigList = PLAY_TYPE_CONFIG_MAP.get(config.getPlayType());
                    if (playTypeConfigList == null) {
                        playTypeConfigList = new CopyOnWriteArrayList<>();
                        PLAY_TYPE_CONFIG_MAP.put(config.getPlayType(), playTypeConfigList);
                    } else {
                        if (!playTypeSet.contains(config.getPlayType())) {
                            playTypeConfigList.clear();
                            playTypeSet.add(config.getPlayType());
                        }
                    }
                    playTypeConfigList.add(config);
                }
            }

            for (List<GoldRoomConfig> list : PLAY_TYPE_CONFIG_MAP.values()) {
                Collections.sort(list, new Comparator<GoldRoomConfig>() {
                    @Override
                    public int compare(GoldRoomConfig o1, GoldRoomConfig o2) {
                        if (o1.getPlayerCount() > o2.getPlayerCount()) {
                            return 1;
                        } else if (o1.getPlayerCount() < o2.getPlayerCount()) {
                            return -1;
                        } else {
                            if (o1.getRate() < o2.getRate()) {
                                return 1;
                            } else if (o1.getRate() > o2.getRate()) {
                                return -1;
                            } else {
                                return 0;
                            }
                        }
                    }
                });
            }

        } catch (Exception e) {
            LogUtil.errorLog.error("GoldRoomUtil|initGoldRoomConfig|error" + e.getMessage(), e);
        }
    }

    public static void initGoldRoomArea() {
        try {
            List<GoldRoomArea> all = GoldRoomDao.getInstance().loadAllGoldRoomArea();
            if (all == null || all.size() == 0) {
                return;
            }
            AREA_LIST.clear();
            AREA_LIST.addAll(all);
        } catch (Exception e) {
            LogUtil.errorLog.error("GoldRoomUtil|initGoldRoomArea|error" + e.getMessage(), e);
        }
    }

    public static void initGoldRoomHall() {
        try {
            List<GoldRoomHall> all = GoldRoomDao.getInstance().loadAllGoldRoomHall();
            if (all == null || all.size() == 0) {
                return;
            }
            HALL_LIST.clear();
            HALL_LIST.addAll(all);
            Set<Long> cleared = new HashSet<>();
            for (GoldRoomHall hall : HALL_LIST) {
                List<GoldRoomConfig> hallConfigList = HALL_CONFIG_MAP.get(hall.getKeyId());
                if (hallConfigList == null) {
                    hallConfigList = new ArrayList<>();
                    HALL_CONFIG_MAP.put(hall.getKeyId(), hallConfigList);
                } else {
                    if (!cleared.contains(hall.getKeyId())) {
                        hallConfigList.clear();
                        cleared.add(hall.getKeyId());
                    }
                }
                for (GoldRoomConfig config : CONFIG_MAP.values()) {
                    if (hall.getPlayTypeList().contains(config.getPlayType())) {
                        hallConfigList.add(config);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("GoldRoomUtil|initGoldRoomHall|error" + e.getMessage(), e);
        }
    }


    public static void initGoldRoomGroupLimit() {
        try {
            List<GoldRoomGroupLimit> all = GoldRoomDao.getInstance().loadAllGoldRoomGroupLimit();
            if (all == null || all.size() == 0) {
                return;
            }
            for (GoldRoomGroupLimit limit : all) {
                String groupIds = limit.getGroupIds();
                if (StringUtils.isNotBlank(groupIds)) {
                    String[] splits = groupIds.split(",");
                    if (splits.length > 0) {
                        for (String split : splits) {
                            Long groupId = Long.valueOf(split);
                            if (!GROUP_ID_LIMIT_MAP.containsKey(groupId)) {
                                GROUP_ID_LIMIT_MAP.put(groupId, groupIds);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("GoldRoomUtil|initGoldRoomGroupLimit|error" + e.getMessage(), e);
        }
    }

    public static GoldRoomConfig getGoldRoomConfig(long keyId) {
        return getGoldRoomConfig(keyId, true);
    }

    public static String getGroupIdLimit(long groupId) {
        return GROUP_ID_LIMIT_MAP.get(groupId);
    }

    /**
     * 金币场配置
     *
     * @param keyId   t_gold_room_config.keyId
     * @param isValid 是否必须是有效的
     * @return
     */
    public static GoldRoomConfig getGoldRoomConfig(long keyId, boolean isValid) {
        GoldRoomConfig config = CONFIG_MAP.get(keyId);
        if (config == null) {
            return null;
        }
        if (isValid && !config.isValid()) {
            return null;
        }
        return config;
    }

    public static List<GoldRoomArea> getAllArea() {
        return AREA_LIST;
    }

    public static List<GoldRoomConfig> getAreaConfigList(long areaId) {
        return AREA_CONFIG_MAP.get(areaId);
    }

    public static List<GoldRoomHall> getAllHall() {
        return HALL_LIST;
    }

    public static List<GoldRoomConfig> getHallConfigList(long hallId) {
        return HALL_CONFIG_MAP.get(hallId);
    }

    public static List<GoldRoomConfig> getPlayTypeConfigList(int playType) {
        return PLAY_TYPE_CONFIG_MAP.get(playType);
    }

    public final static boolean isGoldRoom(Player player) {
        return isGoldRoom(player.getPlayingTableId());
    }

    public final static boolean isGoldRoom(long id) {
        BaseTable table = TableManager.getInstance().getTable(id);
        return table != null && table.isGoldRoom();
    }

    public final static boolean isNotGoldRoom(Player player) {
        return isNotGoldRoom(player.getPlayingTableId());
    }

    public final static boolean isNotGoldRoom(long id) {
        return !isGoldRoom(id);
    }

    @Deprecated
    public final static GoldRoom joinGoldRoom(Player player, int gameType, int serverType, String modeId, String matchType, MatchBean matchBean, boolean newRoom, GoldRoom goldRoom) throws Exception {
        return null;
    }

    public static GoldRoom joinGoldRoom(Player player, GoldRoomConfig config, GoldRoom goldRoom, Long groupId) throws Exception {
        GoldRoomUser goldRoomUser;
        BaseTable table;
        synchronized (config) {
            if (goldRoom == null) {
                goldRoom = newGoldRoom(config);
                if (groupId > 0) {
                    String groupIdLimit = GoldRoomUtil.getGroupIdLimit(groupId);
                    if (StringUtils.isNotBlank(groupIdLimit)) {
                        goldRoom.setGroupIdLimit(1);
                    }
                }
                Long roomId = GoldRoomDao.getInstance().saveGoldRoom(goldRoom);
                if (roomId == null || roomId.longValue() <= 0) {
                    player.writeComMessage(WebSocketMsgType.GOLD_JOIN_FAIL);
                    return null;
                }
                goldRoom.setKeyId(roomId);
                table = TableManager.getInstance().createGoldRoomTable(player, config, goldRoom);
                if (table == null || !table.isGoldRoom()) {
                    GoldRoomDao.getInstance().deleteGoldRoomByKeyId(goldRoom.getKeyId());
                    player.writeComMessage(WebSocketMsgType.GOLD_JOIN_FAIL);
                    return null;
                }

                StringBuilder sb = new StringBuilder("GoldRoom|create");
                sb.append("|").append(goldRoom.getKeyId());
                sb.append("|").append(goldRoom.getModeId());
                sb.append("|").append(player.getUserId());
                LogUtil.msgLog.info(sb.toString());
            } else {
                table = TableManager.getInstance().getTable(goldRoom.getTableId());
                if (table == null) {
                    GoldRoomDao.getInstance().deleteGoldRoomByKeyId(goldRoom.getKeyId());
                    GoldRoomDao.getInstance().deleteGoldRoomUserByRoomId(goldRoom.getKeyId());
                    player.writeComMessage(WebSocketMsgType.GOLD_JOIN_FAIL);
                    return null;
                }
                goldRoom = table.getGoldRoom();
                goldRoomUser = table.getGoldRoomUser(player.getUserId());
                if (goldRoomUser != null) {
                    player.writeComMessage(WebSocketMsgType.GOLD_JOIN_WAIT);
                    player.writeSocket(table.buildCreateTableRes(player.getUserId()));
                    return goldRoom;
                }
            }
        }

        synchronized (goldRoom) {
            if (goldRoom.isFull()) {
                // 重新走创建流程
                return joinGoldRoom(player, config, null, groupId);
            }
            goldRoomUser = newGoldRoomUser(player, goldRoom, groupId);
            Long keyId = GoldRoomDao.getInstance().saveGoldRoomUser(goldRoomUser);
            if (keyId == null || keyId.longValue() <= 0) {
                player.writeComMessage(WebSocketMsgType.GOLD_JOIN_FAIL);
                return null;
            }
            goldRoomUser.setKeyId(keyId);
            int ret = GoldRoomDao.getInstance().addGoldRoomPlayerCount(goldRoom.getKeyId(), 1);
            if (ret <= 0) {
                GoldRoomDao.getInstance().deleteGoldRoomUser(goldRoom.getKeyId(), player.getUserId());
                player.writeComMessage(WebSocketMsgType.GOLD_JOIN_FAIL);
                return null;
            }
            goldRoom.setCurrentCount(goldRoom.getCurrentCount() + 1);
            table.addGoldRoomUser(goldRoomUser);
            player.setPlayingTableId(table.getId());
            player.setEnterServer(goldRoom.getServerId());
            player.saveBaseInfo();

            StringBuilder sb = new StringBuilder("GoldRoom|join");
            sb.append("|").append(goldRoom.getKeyId());
            sb.append("|").append(goldRoom.getModeId());
            sb.append("|").append(player.getUserId());
            LogUtil.msgLog.info(sb.toString());

            if (!goldRoom.canStart()) {
                // 等待开始游戏
                player.writeComMessage(WebSocketMsgType.GOLD_JOIN_WAIT);
                player.writeSocket(table.buildCreateTableRes(player.getUserId()));
                return goldRoom;
            } else {
                player.writeSocket(table.buildCreateTableRes(player.getUserId()));
                startGoldRoom(goldRoom);
            }
        }
        return goldRoom;
    }

    public final static GoldRoomUser newGoldRoomUser(Player player, GoldRoom goldRoom, long groupId) {
        GoldRoomUser goldRoomUser = new GoldRoomUser();
        goldRoomUser.setCreatedTime(new Date());
        goldRoomUser.setGameResult(0L);
        goldRoomUser.setLogIds("");
        goldRoomUser.setRoomId(goldRoom.getKeyId());
        goldRoomUser.setUserId(player.getUserId());
        goldRoomUser.setGroupId(groupId);
        return goldRoomUser;
    }

    public final static GoldRoom newGoldRoom(GoldRoomConfig config) {
        GoldRoom goldRoom;
        goldRoom = new GoldRoom();
        goldRoom.setConfigId(config.getKeyId());
        goldRoom.setGroupIdLimit(0);
        goldRoom.setModeId(config.getKeyId().toString());
        goldRoom.setServerId(GameServerConfig.SERVER_ID);
        goldRoom.setCurrentCount(0);
        goldRoom.setCurrentState(GoldRoom.STATE_NEW);
        goldRoom.setGameCount(config.getTotalBureau());
        goldRoom.setMaxCount(config.getPlayerCount());
        goldRoom.setTableMsg(config.getTableMsg());
        goldRoom.setGoldMsg(config.getGoldMsg());
        goldRoom.setTableName(config.getName());
        goldRoom.setCreatedTime(new Date());
        goldRoom.setModifiedTime(goldRoom.getCreatedTime());
        return goldRoom;
    }

    public static void startGoldRoom(GoldRoom goldRoom) throws Exception {
        startGoldRoom(goldRoom, 500, false);
    }

    /**
     * 金币场开局
     *
     * @param goldRoom
     * @param delay    检查发牌延迟
     * @param forMatch 是否匹配场
     * @throws Exception
     */
    public static void startGoldRoom(GoldRoom goldRoom, int delay, boolean forMatch) throws Exception {
        synchronized (goldRoom) {
            if (goldRoom == null || !goldRoom.canStart()) {
                LogUtil.msgLog.info("GoldRoom|start|error|configIsNull|" + (goldRoom != null ? JSON.toJSONString(goldRoom) : ""));
                return;
            }
            GoldRoomConfig config = getGoldRoomConfig(goldRoom.getConfigId());
            if (config == null) {
                LogUtil.msgLog.info("GoldRoom|start|error|configIsNull|" + goldRoom.getKeyId());
                return;
            }
            BaseTable table = TableManager.getInstance().getTable(goldRoom.getTableId());
            if (table == null) {
                LogUtil.msgLog.info("GoldRoom|start|error|tableIsNull|" + goldRoom.getKeyId());
                return;
            }

            //开始游戏
            GoldRoomDao.getInstance().updateGoldRoomCurrentState(goldRoom.getKeyId(), GoldRoom.STATE_PLAYING);
            goldRoom.setCurrentState(GoldRoom.STATE_PLAYING);
            Map<Long, GoldRoomUser> goldRoomUserMap = table.getGoldRoomUserMap();
            List<Player> playerList = new ArrayList<>(goldRoomUserMap.size());
            for (GoldRoomUser gu : goldRoomUserMap.values()) {
                Player tempPlayer = PlayerManager.getInstance().getPlayer(gu.getUserId());
                if (tempPlayer == null) {
                    tempPlayer = PlayerManager.getInstance().loadPlayer(gu.getUserId(), config.getPlayType());
                    tempPlayer.setIsOnline(0);
                    tempPlayer.setIsEntryTable(SharedConstants.table_offline);
                    LogUtil.msgLog.info("GoldRoom|userOnline|0|" + gu.getUserId());
                }
                playerList.add(tempPlayer);
            }

            // -----------随机房主-------------------
            Player master = playerList.remove(new SecureRandom().nextInt(playerList.size()));
            playerList.add(0, master);

            // -----------牌桌进入准备阶段-------------------
            table.changeTableState(SharedConstants.table_state.ready);
            if (TableManager.getInstance().addTable(table) == 1) {
                table.saveSimpleTable();
            }

            table.setMasterId(master.getUserId());
            table.changeExtend();
            StringBuilder sb = new StringBuilder();
            List<Player> tablePlayerList = new ArrayList<>(playerList.size());
            for (Player player1 : playerList) {

                sb = new StringBuilder("GoldRoom|Start");
                sb.append("|").append(table.getId());
                sb.append("|").append(goldRoom.getKeyId());
                sb.append("|").append(goldRoom.getModeId());
                sb.append("|").append(player1.getUserId());
                LogUtil.msgLog.info(sb.toString());

                Player player2 = PlayerManager.getInstance().changePlayer(player1, table.getPlayerClass());
                // 加入牌桌
                if (!table.joinPlayer(player2)) {
                    sb = new StringBuilder("GoldRoom|error|join");
                    sb.append("|").append(table.getId());
                    sb.append("|").append(goldRoom.getKeyId());
                    sb.append("|").append(goldRoom.getModeId());
                    sb.append("|").append(player1.getUserId());
                    LogUtil.msgLog.info(sb.toString());
                    continue;
                } else {
                    tablePlayerList.add(player2);
                }
                table.ready(player2);
                if (player2.isOnline()) {
                    player2.writeComMessage(WebSocketMsgType.GOLD_JOIN_SUCCESS);
                }
            }
            if (forMatch) {
                for (Player player2 : tablePlayerList) {
                    if (player2.isOnline()) {
                        TableRes.CreateTableRes createTableRes = table.buildCreateTableRes(player2.getUserId());
                        player2.writeSocket(createTableRes);
                    }
                }
            } else {
                Map<Long, GeneratedMessage> msgMap = new HashMap<>();
                // 给玩家推送加入房间消息时，先发自己加入房间的消息
                for (Player player2 : tablePlayerList) {
                    if (player2.isOnline()) {
                        TableRes.JoinTableRes.Builder joinRes = TableRes.JoinTableRes.newBuilder();
                        joinRes.setPlayer(player2.buildPlayInTableInfo());
                        joinRes.setWanfa(table.getPlayType());

                        GeneratedMessage msg = joinRes.build();
                        msgMap.put(player2.getUserId(), msg);
                        player2.writeSocket(msg);
                    }
                }

                // 给玩家推送加入房间消息时，发别人的加入房间的消息
                for (Player player2 : tablePlayerList) {
                    if (player2.isOnline()) {
                        for (Map.Entry<Long, GeneratedMessage> kv : msgMap.entrySet()) {
                            if (player2.getUserId() != kv.getKey().longValue()) {
                                player2.writeSocket(kv.getValue());
                            }
                        }
                    }
                }
            }

            table.broadOnlineStateMsg();

            if (delay > 0) {
                final BaseTable goldTable = table;
                TaskExecutor.delayExecutor.schedule(new Runnable() {
                    @Override
                    public void run() {
                        goldTable.checkDeal();
                        goldTable.startNext();
                    }
                }, delay, TimeUnit.MILLISECONDS);
            } else {
                table.checkDeal();
                table.startNext();
            }
        }
    }

    public static BaseTable matchSucc(List<Player> playerList, GoldRoomConfig config, int delay) {
        Long goldRoomId = 0L;
        BaseTable table = null;
        try {
            Server server = ServerManager.loadServer(GameServerConfig.SERVER_ID);
            GoldRoom goldRoom = GoldRoomUtil.newGoldRoom(config);
            goldRoom.setServerId(server.getId());
            goldRoom.setCurrentCount(goldRoom.getMaxCount());
            goldRoomId = GoldRoomDao.getInstance().saveGoldRoom(goldRoom);
            if (goldRoomId == null || goldRoomId.longValue() <= 0) {
                return null;
            }
            goldRoom.setKeyId(goldRoomId);

            Player player1 = null;
            List<GoldRoomUser> gruList = new ArrayList<>();
            for (Player player : playerList) {
                GoldRoomUser goldRoomUser = GoldRoomUtil.newGoldRoomUser(player, goldRoom, 0);
                Long keyId = GoldRoomDao.getInstance().saveGoldRoomUser(goldRoomUser);
                if (keyId == null || keyId.longValue() <= 0) {
                    GoldRoomDao.getInstance().deleteGoldRoomByKeyId(goldRoom.getKeyId());
                    GoldRoomDao.getInstance().deleteGoldRoomUserByRoomId(goldRoom.getKeyId());
                    return null;
                }
                goldRoomUser.setKeyId(keyId);
                player1 = player;
                gruList.add(goldRoomUser);
            }
            // 在本服，创建好牌桌
            table = TableManager.getInstance().createGoldRoomTableForMatch(player1, config, goldRoom);
            if (table == null || !table.isGoldRoom()) {
                GoldRoomDao.getInstance().deleteGoldRoomByKeyId(goldRoom.getKeyId());
                GoldRoomDao.getInstance().deleteGoldRoomUserByRoomId(goldRoom.getKeyId());
                return null;
            }

            Map<String, Object> modify = new HashMap<>(8);
            modify.put("keyId", goldRoom.getKeyId());
            modify.put("tableId", table.getId());
            modify.put("currentState", GoldRoom.STATE_READY);
            GoldRoomDao.getInstance().updateGoldRoomByKeyId(modify);

            goldRoom.setCurrentCount(goldRoom.getMaxCount());
            for (GoldRoomUser goldRoomUser : gruList) {
                table.addGoldRoomUser(goldRoomUser);
            }
            for (Player player : playerList) {
                player.setPlayingTableId(table.getId());
                player.setEnterServer(goldRoom.getServerId());
                player.saveBaseInfo();
            }
            // 牌桌开始
            GoldRoomUtil.startGoldRoom(goldRoom, delay, true);
        } catch (Exception e) {
            LogUtil.errorLog.error("matchSucc|error|", e);
            if (table != null) {
                table.diss();
                if (goldRoomId > 0) {
                    try {
                        GoldRoomDao.getInstance().deleteGoldRoomByKeyId(goldRoomId);
                        GoldRoomDao.getInstance().deleteGoldRoomUserByRoomId(goldRoomId);
                    } catch (Exception e1) {
                        LogUtil.errorLog.error("deleteGoldRoomByKeyId|error|" + goldRoomId, e);
                    }
                }
            }
        }
        return table;
    }

    public static void initGiftCertificate(GoldRoomConfig config) {
        // 在名字里面初始化上赠送的礼券信息
        try {
            Map<String, String> giftConfig = ResourcesConfigsUtil.getStringValueMap().get("GoldRoomGiftCertActivityConfig");
            if (null == giftConfig) {
                //t_resouce_config未配置 GoldRoomGiftCertActivityConfig
                return;
            }
            for (Map.Entry<String, String> entry : giftConfig.entrySet()) {
                String configDetail = entry.getValue().split(";")[0];
                String gameKeyId = entry.getValue().split(";")[1];
                String[] ganmeKeyAry = gameKeyId.split(",");
                String configDetail_activityIsOpen = configDetail.split(",")[0];//活动是否开启了
                if (!configDetail_activityIsOpen.equals("1")) {
                    continue;
                }
                String configDetail_activityRewardBean = configDetail.split(",")[1];//活动奖励
                String configDetail_activityRule = configDetail.split(",")[2];//活动规则
                //String configDetail_activityItemLimit = configDetail.split(",")[3];//活动 每日数量获取限制
                for (int i = 0; i < ganmeKeyAry.length; i++) {
                    long keyid = Long.valueOf(ganmeKeyAry[i]);
                    if (keyid == config.getKeyId()) {
                        config.setName(config.getName() + "|" + configDetail_activityRule + "局送" + configDetail_activityRewardBean + "礼券");
                        return;
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("initGoldRoomGiftCertificate|error|configError", e);
        }
    }

    public static void initGoldRoomRobotLose() {
        try {
            Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));

            Long totalLose = DataStatisticsDao.getInstance().loadGoldDataStatisticsDataValue(dataDate, SourceType.goldRoom_robot_total_lose.type(), 0l);
            Long totalWin = DataStatisticsDao.getInstance().loadGoldDataStatisticsDataValue(dataDate, SourceType.goldRoom_robot_total_win.type(), 0l);

            goldRoomRobotTotalLose = totalWin + totalLose > 0 ? 0 : totalWin + totalLose;

            String dataTypes = SourceType.goldRoom_robot_playType_lose.type() + "," + SourceType.goldRoom_robot_playType_win.type();
            List<Map<String, Object>> playTypeLoseMap = DataStatisticsDao.getInstance().loadGoldRoomRobotPlayTypeLose(dataDate, dataTypes);
            if (playTypeLoseMap != null && playTypeLoseMap.size() > 0) {
                for (Map<String, Object> lose : playTypeLoseMap) {
                    Integer playType = Integer.valueOf(lose.get("userId").toString());
                    Long sumValue = Long.valueOf(lose.get("sumValue").toString());
                    if (sumValue != null && sumValue < 0) {
                        goldRoomRobotPlayTypeLoseMap.put(playType, sumValue);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("GoldRoomUtil|initGoldRoomRobotLose|error" + e.getMessage(), e);
        }
    }

    public static boolean canUseRobot(int playType) {
        if (playType <= 0) {
            return false;
        }
        long limit = Long.valueOf(ResourcesConfigsUtil.loadServerConfigValue("goldRoom_robot_total_lose_limit", "99999999"));
        if (goldRoomRobotTotalLose < 0 && Math.abs(goldRoomRobotTotalLose) >= limit) {
            return false;
        }
        Long playTypeLose = goldRoomRobotPlayTypeLoseMap.get(playType);
        if (playTypeLose != null && playTypeLose < 0) {
            long playTypeLimit = Long.valueOf(ResourcesConfigsUtil.loadServerConfigValue("goldRoom_robot_playType_lose_limit", "9999999"));
            if (Math.abs(playTypeLose) >= playTypeLimit) {
                return false;
            }
        }
        return true;
    }
}
