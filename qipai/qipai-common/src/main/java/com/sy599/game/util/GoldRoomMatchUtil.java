package com.sy599.game.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.gold.GoldRoomConfig;
import com.sy599.game.db.bean.gold.GoldRoomMatchPlayer;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.db.enums.SourceType;
import com.sy599.game.gold.GoldRoomMatch;
import com.sy599.game.gold.GoldRoomMatchSucc;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.robot.RobotManager;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

public class GoldRoomMatchUtil {

    public static final Map<Integer, Object> playTypeLock = new ConcurrentHashMap<>();
    public static final Map<Long, GoldRoomMatch> map = new ConcurrentHashMap<>();

    public static void shutdown() {
        for (GoldRoomMatch match : map.values()) {
            Player player = PlayerManager.getInstance().getPlayer(match.getPlayer().getUserId());
            GoldRoomDao.getInstance().deleteGoldRoomMatchPlayerByUserId(match.getPlayer().getUserId());
            if (player != null) {
                if (match.isMatching()) {
                    player.writeComMessage(WebSocketMsgType.res_code_quitGoldRoomMatch);
                    delLoginExtend(player);
                }
            }
        }
    }

    /**
     * 服务器启动时初始化数据
     */
    public static void initFromDBOnServerStart() {
        try {
            if (!ServerManager.isGoldRoomServer()) {
                return;
            }
            Server server = ServerManager.loadServer(GameServerConfig.SERVER_ID);
            List<Integer> matchTypes = server.getMatchType();
            for (Integer type : matchTypes) {
                playTypeLock.put(type, new Object());
            }
            List<GoldRoomMatchPlayer> matchPlayerList = GoldRoomDao.getInstance().loadAllGoldRoomMatchPlayer(GameServerConfig.SERVER_ID);
            if (matchPlayerList == null || matchPlayerList.size() == 0) {
                return;
            }
            for (GoldRoomMatchPlayer matchPlayer : matchPlayerList) {
                try {
                    Player player = PlayerManager.getInstance().loadPlayer0(matchPlayer.getUserId(), matchPlayer.getPlayType());
                    delLoginExtend(player);
                    GoldRoomDao.getInstance().deleteGoldRoomMatchPlayerByUserId(matchPlayer.getUserId());
                } catch (Exception e) {
                    LogUtil.errorLog.error("initFromDbOnServerStart|error|" + JSON.toJSONString(matchPlayer), e);
                    GoldRoomDao.getInstance().deleteGoldRoomMatchPlayerByUserId(matchPlayer.getUserId());
                }
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("initFromDbOnServerStart|error|" + e.getMessage(), e);
        }
    }

    public static List<GoldRoomConfig> initConfigList(Player player, int playType, int matchType, long configId) {
        List<GoldRoomConfig> list = new ArrayList<>();
        if (matchType == GoldRoomMatch.MATCH_TYPE_FAST && configId > 0) {
            // 指定某个配置加入
            GoldRoomConfig config = GoldRoomUtil.getGoldRoomConfig(configId);
            if (config == null) {
                player.writeErrMsg("匹配失败：配置错误");
                return list;
            }
            if (player.loadAllGolds() < config.getJoinLimit()) {
                player.sendBrokeAward(config.getJoinLimit());
            } else {
                list.add(config);
            }
            return list;
        }
        // 找最接近的
        List<GoldRoomConfig> configList = GoldRoomUtil.getPlayTypeConfigList(playType);
        Set<Integer> playerCountSet = new HashSet<>();
        long minJoinLimit = Long.MAX_VALUE;
        for (GoldRoomConfig config : configList) {
            if (config.getJoinLimit() < minJoinLimit) {
                minJoinLimit = config.getJoinLimit();
            }
            if (playerCountSet.contains(config.getPlayerCount())) {
                continue;
            }
            if (player.loadAllGolds() >= config.getJoinLimit()) {
                list.add(config);
                playerCountSet.add(config.getPlayerCount());
            }
        }
        if (list.size() == 0) {
            LogUtil.msgLog.error("joinMatch|error|noConfig|" + player.getUserId() + "|" + playType + "|" + matchType + "|" + configId);
            if (configList.size() > 0) {
                player.sendBrokeAward(minJoinLimit);
            } else {
                player.writeErrMsg("匹配失败：配置错误");
            }
        }
        return list;
    }

    /**
     * 加入匹配队列
     *
     * @param player
     * @param playType
     * @param matchType
     * @param goldRoomConfigId
     * @return
     */
    public static boolean joinMatch(Player player, int playType, int matchType, long goldRoomConfigId) {
        Object lock = playTypeLock.get(playType);
        synchronized (lock) {
            if (GameServerConfig.isClosed()) {
                return false;
            }
            GoldRoomMatch match = map.get(player.getUserId());
            if (match != null && match.isMatching()) {
                return false;
            }
            match = new GoldRoomMatch(player, playType);
            List<GoldRoomConfig> configList = initConfigList(player, playType, matchType, goldRoomConfigId);
            if (configList.size() == 0) {
                return false;
            }

            match.addAllGoldRoomConfig(configList);
            match.setMatchType(matchType);

            GoldRoomMatchPlayer matchPlayer = new GoldRoomMatchPlayer();
            matchPlayer.setUserId(player.getUserId());
            matchPlayer.setStatus(GoldRoomMatchPlayer.status_match);
            matchPlayer.setPlayType(playType);
            matchPlayer.setMatchType(matchType);
            matchPlayer.setConfigId(goldRoomConfigId);
            matchPlayer.setCreatedTime(new Date());
            matchPlayer.setGroupId(0L);
            matchPlayer.setServerId(GameServerConfig.SERVER_ID);
            GoldRoomDao.getInstance().saveGoldRoomMatch(matchPlayer);

            saveLoginExtend(player);
            match.setStatus(GoldRoomMatch.status_matching);
            addMatchPlayer(match);
            LogUtil.msgLog.info("GoldRoomMatch|joinMatch|" + player.getUserId() + "|" + playType + "|" + matchType + "|" + goldRoomConfigId + "|" + match.getConfigIdSet());
            return true;
        }
    }

    public static void addMatchPlayer(GoldRoomMatch matchPlayer) {
        Player player = matchPlayer.getPlayer();
        map.put(player.getUserId(), matchPlayer);
    }

    public static boolean saveLoginExtend(Player player) {
        try {
            JSONObject json;
            if (StringUtils.isBlank(player.getLoginExtend())) {
                json = new JSONObject();
            } else {
                json = JSON.parseObject(player.getLoginExtend());
            }
            json.put("grmId", 1);
            String loginExtend = json.toString();
            Map<String, Object> map = new HashMap<>();
            map.put("loginExtend", loginExtend);
            UserDao.getInstance().updateUser(String.valueOf(player.getUserId()), map);
            player.setLoginExtend(loginExtend);
            return true;
        } catch (Exception e) {
            LogUtil.errorLog.error("saveLoginExtend|error|" + player.getUserId(), e);
        }
        return false;
    }

    public static boolean delLoginExtend(Player player) {
        try {
            if (StringUtils.isBlank(player.getLoginExtend())) {
                return true;
            }
            JSONObject json = JSON.parseObject(player.getLoginExtend());
            if (json.containsKey("grmId")) {
                json.remove("grmId");
                String loginExtend = json.toString();
                Map<String, Object> map = new HashMap<>();
                map.put("loginExtend", loginExtend);
                UserDao.getInstance().updateUser(String.valueOf(player.getUserId()), map);
                player.setLoginExtend(loginExtend);
                return true;
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("saveLoginExtend|error|" + player.getUserId(), e);
        }
        return false;
    }

    /**
     * 离开匹配队列
     *
     * @param player
     * @return
     */
    public synchronized static boolean quitMatch(Player player) {
        try {
            GoldRoomMatch match = map.get(player.getUserId());
            if (match == null) {
                delLoginExtend(player);
                return true;
            }
            Object lock = playTypeLock.get(match.getPlayType());
            synchronized (lock) {
                if (!match.isMatching()) {
                    return false;
                }
                delLoginExtend(player);
                match.setStatus(GoldRoomMatch.status_quit);
                GoldRoomDao.getInstance().deleteGoldRoomMatchPlayerByUserId(player.getUserId());
                map.remove(player.getUserId());
            }
            LogUtil.msgLog.info("GoldRoomMatch|quitMatch|" + player.getUserId() + "|" + match.getPlayType() + "|" + match.getMatchType() + "|" + match.getInitConfigId());
        } catch (Exception e) {
            LogUtil.errorLog.error("quitMatch|error|" + player.getUserId(), e);
        }
        return true;
    }

    /**
     * 匹配
     */
    public static void doMatch() {
        if (GameServerConfig.isClosed()) {
            return;
        }

        List<GoldRoomMatch> all = new ArrayList<>(map.values());
        Map<Integer, List<GoldRoomMatch>> playTypeMap = new HashMap<>();
        for (GoldRoomMatch match : all) {
            List<GoldRoomMatch> list = playTypeMap.get(match.getPlayType());
            if (list == null) {
                list = new ArrayList<>();
                playTypeMap.put(match.getPlayType(), list);
            }
            list.add(match);
        }

        List<GoldRoomMatchSucc> matchSuccList = new ArrayList<>();
        for (Integer playType : playTypeLock.keySet()) {
            List<GoldRoomMatch> matchList = playTypeMap.get(playType);
            if (matchList == null || matchList.size() == 0) {
                continue;
            }
            Object lock = playTypeLock.get(playType);
            synchronized (lock) {

                // ---------------匹配----------------------
                processMatching(playType, matchList, matchSuccList);

                // --------------判断超时，是否要增加范围或退出匹配队列-----------------
                processMatchingTimeOut(matchList);

            }
        }

        // -----------------匹配成功----------------------
        processMatchSucc(matchSuccList);
    }

    public static void processMatching(int playType, List<GoldRoomMatch> matchList, List<GoldRoomMatchSucc> matchSuccList) {
        long now = System.currentTimeMillis();
        // -------------排序------------------------------------------
        for (GoldRoomMatch match : matchList) {
            long priority = 1000 * (now - match.getStartTime()) / 1000 + 10 * match.getMaxRate() + match.getMatchType() == GoldRoomMatch.MATCH_TYPE_FAST ? 2 : 1;
            match.setMatchPriority(priority);
        }

        Collections.sort(matchList, new Comparator<GoldRoomMatch>() {
            @Override
            public int compare(GoldRoomMatch o1, GoldRoomMatch o2) {
                return (int) (o1.getMatchPriority() - o2.getMatchPriority());
            }
        });

        // ------------------匹配------------------------------------
        for (GoldRoomMatch match : matchList) {
            matching(match, matchList, matchSuccList);
        }
    }

    public static void matching(GoldRoomMatch match, List<GoldRoomMatch> matchList, List<GoldRoomMatchSucc> matchSuccList) {
        if (!match.isMatching()) {
            return;
        }
        List<GoldRoomMatch> matchingList = new ArrayList<>();
        Set<Long> matchingUserIdSet = new HashSet<>();
        boolean matchSucc = false;
        long now = System.currentTimeMillis();
        match.sortConfigList();
        GoldRoomConfig robotConfig = null;
        List<GoldRoomMatch> robotMatchingList = null;
        int robotLack = 100;
        for (GoldRoomConfig config : match.getConfigList()) {
            if (matchSucc) {
                break;
            }
            boolean canUseRobot = GoldRoomUtil.canUseRobot(config.getPlayType()) && config.canUseRobot();
            matchingList.clear();
            matchingList.add(match);
            matchingUserIdSet.clear();
            matchingUserIdSet.add(match.getPlayer().getUserId());

            if (canUseRobot && robotConfig == null) {
                robotConfig = config;
                robotLack = config.getPlayerCount() - matchingList.size();
                robotMatchingList = new ArrayList<>(matchingList);
            }

            for (GoldRoomMatch other : matchList) {
                if (!other.isMatching()) {
                    continue;
                }
                if (matchingUserIdSet.contains(other.getPlayer().getUserId())) {
                    continue;
                }
                if (!other.getConfigIdSet().contains(config.getKeyId())) {
                    continue;
                }
                matchingList.add(other);
                matchingUserIdSet.add(other.getPlayer().getUserId());
                if (matchingList.size() == config.getPlayerCount()) {
                    // isOK
                    for (GoldRoomMatch matching : matchingList) {
                        matching.setStatus(GoldRoomMatch.status_matched);
                        matching.setMatched(config);
                    }
                    matchSuccList.add(new GoldRoomMatchSucc(config, new ArrayList<>(matchingList)));
                    matchSucc = true;
                    break;
                }

                if (canUseRobot && config.getPlayerCount() - matchingList.size() < robotLack) {
                    robotConfig = config;
                    robotLack = config.getPlayerCount() - matchingList.size();
                    robotMatchingList = new ArrayList<>(matchingList);
                }
            }
        }

        // 机器人投放
        if (!matchSucc && robotConfig != null) {
            int timeOutSecond = ResourcesConfigsUtil.loadServerConfigIntegerValue("goldRoom_robot_timeOut_second",50);
            if (now - match.getStartTime() > timeOutSecond * 1000) {
                int needRobotCount = robotConfig.getPlayerCount() - robotMatchingList.size();
                for (int i = 0; i < needRobotCount; i++) {
                    Player robot = RobotManager.genGoldRoomRobot(robotConfig.getPlayType());
                    if (robot != null) {
                        if(robot.loadAllGolds() < robotConfig.getJoinLimit()){
                            robot.changeGold(robotConfig.getJoinLimit(), 0, 0, SourceType.goldRoom_robot_replenish);
                        }

                        GoldRoomMatch robotMatch = new GoldRoomMatch(robot, robotConfig.getPlayType());
                        robotMatch.addAllGoldRoomConfig(match.getConfigList());
                        robotMatch.setMatchType(match.getMatchType());
                        robotMatch.setStatus(GoldRoomMatch.status_matching);
                        robotMatchingList.add(robotMatch);
                        matchingUserIdSet.add(robotMatch.getPlayer().getUserId());
                    }
                }
                if (robotMatchingList.size() == robotConfig.getPlayerCount()) {
                    // isOK
                    for (GoldRoomMatch matching : robotMatchingList) {
                        matching.setStatus(GoldRoomMatch.status_matched);
                        matching.setMatched(robotConfig);
                    }
                    matchSuccList.add(new GoldRoomMatchSucc(robotConfig, new ArrayList<>(robotMatchingList)));
                }
            }
        }
    }


    public static void processMatchingTimeOut(List<GoldRoomMatch> matchList) {
        long now = System.currentTimeMillis();
        for (GoldRoomMatch match : matchList) {
            if (!match.isMatching()) {
                continue;
            }
            if (now - match.getLastTimeout() < 10 * 1000) {
                continue;
            }
            if (match.getMatchType() == GoldRoomMatch.MATCH_TYPE_FAST) {
                // 匹配失败
//                match.setStatus(GoldRoomMatch.status_fail);
                continue;
            }
            match.setLastTimeout(now);
            List<GoldRoomConfig> list = GoldRoomUtil.getPlayTypeConfigList(match.getPlayType());
            if (list.size() == match.getConfigList().size()) {
                continue;
            }
            long nowMin = Integer.MAX_VALUE;
            for (GoldRoomConfig config : match.getConfigList()) {
                if (config.getJoinLimit() < nowMin) {
                    nowMin = config.getJoinLimit();
                }
            }
            long nextMin = 0;
            for (GoldRoomConfig config : list) {
                // 匹配范围增大
                if (match.getConfigIdSet().contains(config.getKeyId())) {
                    continue;
                }
                if (nextMin == 0 && config.getJoinLimit() < nowMin) {
                    nextMin = config.getJoinLimit();
                }
                if (nextMin != 0 && nextMin == config.getJoinLimit()) {
                    match.addGoldRoomConfig(config);
                }
            }
            LogUtil.msgLog.info("GoldRoomMatch|addConfig|" + match.getPlayer().getUserId() + "|" + match.getConfigIdSet());
        }

        // ----------离开匹配-------------------
        for (GoldRoomMatch match : matchList) {
            if (!match.isMatching() && !match.isMatched()) {
                Player player = match.getPlayer();
                if (match.isFail()) {
                    if (player.isOnline()) {
                        player.writeComMessage(WebSocketMsgType.res_code_goldRoomMatchTimeOut, match.getPlayType(), match.getMatchType(), String.valueOf(match.getInitConfigId()));
                    }
                }
                GoldRoomDao.getInstance().deleteGoldRoomMatchPlayerByUserId(match.getPlayer().getUserId());
                GoldRoomMatch goldRoomMatch = map.get(player.getUserId());
                if (goldRoomMatch == match) {
                    map.remove(player.getUserId());
                    delLoginExtend(player);
                }
            }
        }
    }


    public static void processMatchSucc(List<GoldRoomMatchSucc> matchSuccList) {
        if (matchSuccList == null || matchSuccList.size() == 0) {
            return;
        }
        for (GoldRoomMatchSucc matchSucc : matchSuccList) {
            TaskExecutor.EXECUTOR_SERVICE_GOLD_ROOM_MATCH.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        boolean res = matchSucc(matchSucc);
                        synchronized (playTypeLock.get(matchSucc.getConfig().getPlayType())) {
                            for (GoldRoomMatch match : matchSucc.getMatchedList()) {
                                map.remove(match.getPlayer().getUserId());
                                delLoginExtend(match.getPlayer());
                                if (!res) {
                                    Player tempPlayer = PlayerManager.getInstance().getPlayer(match.getPlayer().getUserId());
                                    if (tempPlayer != null && tempPlayer.isOnline()) {
                                        tempPlayer.writeComMessage(WebSocketMsgType.res_code_quitGoldRoomMatch);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        LogUtil.errorLog.error("processMatchSucc|error|" + JSON.toJSONString(matchSucc), e);
                    }
                }
            });
        }
    }

    /**
     * 匹配成功后续操作
     *
     * @param matchSucc
     */
    public static boolean matchSucc(GoldRoomMatchSucc matchSucc) {
        try {
            GoldRoomConfig config = matchSucc.getConfig();
            StringBuilder sb = new StringBuilder();
            sb.append("|").append(config.getKeyId());
            sb.append("|").append(config.getPlayType());
            sb.append("|").append(config.getPlayerCount());
            sb.append("|[");
            for (GoldRoomMatch matchPlayer : matchSucc.getMatchedList()) {
                sb.append(matchPlayer.getPlayer().getUserId());
                sb.append(",");
            }
            sb.append("]");
            LogUtil.msgLog.info("GoldRoomMatch|processMatchSucc|start" + sb.toString());

            Server server = ServerManager.loadServer(config.getPlayType(), Server.SERVER_TYPE_GOLD_ROOM);
            if (server == null) {
                LogUtil.msgLog.info("GoldRoomMatch|processMatchSucc|error|serverIsNull" + sb.toString());
                return false;
            }
            if (server.getId() == GameServerConfig.SERVER_ID) {
                List<Player> playerList = new ArrayList<>();
                for (GoldRoomMatch match : matchSucc.getMatchedList()) {
                    playerList.add(match.getPlayer());
                }
                BaseTable table = GoldRoomUtil.matchSucc(playerList, config, 500);
                LogUtil.msgLog.info("GoldRoomMatch|processMatchSucc|end" + sb.toString());
                return table != null;
            } else {
                if (!acrossServer(matchSucc, server)) {
                    return false;
                }
            }
            LogUtil.msgLog.info("GoldRoomMatch|processMatchSucc|end" + sb.toString());
        } catch (Exception e) {
            LogUtil.errorLog.error("processMatchSucc|error|" + e.getMessage() + "|" + JSON.toJSONString(matchSucc), e);
        }
        return true;
    }


    public static boolean acrossServer(GoldRoomMatchSucc matchSucc, Server server) {
        try {
            StringJoiner sj = new StringJoiner(",");
            for (GoldRoomMatch match : matchSucc.getMatchedList()) {
                sj.add(String.valueOf(match.getPlayer().getUserId()));
            }
            String ret = ServerUtil.genGoldRoomTable(server.getId(), sj.toString(), matchSucc.getConfig().getKeyId());
            if (StringUtil.isBlank(ret)) {
                return false;
            }
            JSONObject json = JSON.parseObject(ret);
            if (json == null) {
                return false;
            }
            int code = json.getIntValue("code");
            if (code != 0) {
                return false;
            }
            long tableId = json.getLongValue("tableId");
            if (tableId == 0) {
                return false;
            }

            for (GoldRoomMatch match : matchSucc.getMatchedList()) {
                long userId = match.getPlayer().getUserId();
                Player player = PlayerManager.getInstance().getPlayer(userId);
                if (player != null) {
                    player.setPlayingTableId(tableId);
                    player.setEnterServer(server.getId());
                    player.saveBaseInfo();

                    // 推送给用户切服
                    Map<String, Object> result = new HashMap<>();
                    Map<String, Object> serverMap = new HashMap<>();
                    serverMap.put("serverId", server.getId());
                    serverMap.put("connectHost", server.getChathost());
                    result.put("server", serverMap);
                    result.put("goldRoomId", tableId);
                    result.put("goldRoomMatch", 1);
                    result.put("tId", tableId);
                    result.put("code", 0);
                    if (player.isOnline()) {
                        player.writeComMessage(WebSocketMsgType.res_code_goldRoomMatchCrossServer, JacksonUtil.writeValueAsString(result));
                    }
                }
            }

            return true;
        } catch (Exception e) {
            LogUtil.errorLog.error("GoldRoomMatch|acrossServer|error|", e);
        }
        return false;
    }

}
