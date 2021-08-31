package com.sy599.game.db.dao.gold;

import com.alibaba.fastjson.JSON;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.bean.gold.GoldRoomArea;
import com.sy599.game.db.bean.gold.GoldRoomConfig;
import com.sy599.game.db.bean.gold.GoldRoomGroupLimit;
import com.sy599.game.db.bean.gold.GoldRoomHall;
import com.sy599.game.db.bean.gold.GoldRoomMatchPlayer;
import com.sy599.game.db.bean.gold.GoldRoomUser;
import com.sy599.game.db.dao.BaseDao;
import com.sy599.game.gold.GoldRoomTableRecord;
import com.sy599.game.gold.SoloRoomTableRecord;
import com.sy599.game.util.LogUtil;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoldRoomDao extends BaseDao {

    private static GoldRoomDao groupDao = new GoldRoomDao();

    public static GoldRoomDao getInstance() {
        return groupDao;
    }

    public int updateGoldRoomByKeyId(HashMap<String, Object> map) throws Exception {
        return this.getSqlLoginClient().update("goldRoom.update_gold_room", map);
    }

    public GoldRoomUser loadGoldRoomUser(long roomId, long userId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("roomId", roomId);
        map.put("userId", userId);
        return (GoldRoomUser) this.getSqlLoginClient().queryForObject("goldRoom.select_gold_room_user", map);
    }

    public List<GoldRoomUser> loadAllGoldRoomUser(long roomId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("roomId", roomId);
        return (List<GoldRoomUser>) this.getSqlLoginClient().queryForList("goldRoom.load_all_gold_room_user", map);
    }

    public GoldRoom loadGoldRoom(long keyId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("keyId", String.valueOf(keyId));
        return (GoldRoom) this.getSqlLoginClient().queryForObject("goldRoom.select_gold_room", map);
    }
    public GoldRoom loadGoldRoomForceMaster(long keyId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("keyId", String.valueOf(keyId));
        return (GoldRoom) this.getSqlLoginClient().queryForObject("goldRoom.select_gold_room_force_master", map);
    }

    public List<HashMap<String, Object>> loadGoldRoomUserIds(Long roomId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("roomId", roomId.toString());
        return this.getSqlLoginClient().queryForList("goldRoom.load_gold_room_userIds", map);
    }

    /**
     * 随机获取一个未开局未满人的房间
     */
    public GoldRoom loadCanJoinGoldRoom(String modeId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("modeId", modeId);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        map.put("myDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
        return (GoldRoom) this.getSqlLoginClient().queryForObject("goldRoom.one_gold_room_random", map);
    }

    /**
     * 随机获取一个未开局未满人的房间
     */
    public GoldRoom loadCanJoinGoldRoom(String modeId, int serverId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("modeId", modeId);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        map.put("myDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
        map.put("serverId", String.valueOf(serverId));
        return (GoldRoom) this.getSqlLoginClient().queryForObject("goldRoom.one_gold_room_random_server", map);
    }

    /**
     * 随机获取一个指定状态未满人的房间
     */
    public GoldRoom loadCanJoinGoldRoom(String modeId, int serverId, String currentState) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("modeId", modeId);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        map.put("myDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
        map.put("serverId", String.valueOf(serverId));
        map.put("currentState", currentState);
        return (GoldRoom) this.getSqlLoginClient().queryForObject("goldRoom.one_gold_room_server_state", map);
    }

    /**
     * 随机获取一个指定状态未满人的房间
     */
    public GoldRoom loadCanJoinGoldRoom(String modeId, String currentState) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("modeId", modeId);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        map.put("myDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
        map.put("currentState", currentState);
        return (GoldRoom) this.getSqlLoginClient().queryForObject("goldRoom.one_gold_room_state", map);
    }


    public Long saveGoldRoom(GoldRoom goldRoom) {
        try {
            return (Long) this.getSqlLoginClient().insert("goldRoom.insert_gold_room", goldRoom);
        } catch (Exception e) {
            LogUtil.errorLog.error("saveGoldRoom|error|" + JSON.toJSONString(goldRoom), e);
        }
        return 0L;
    }

    public Long saveGoldRoomUser(GoldRoomUser goldRoomUser) throws Exception {
        try{
            return (Long) this.getSqlLoginClient().insert("goldRoom.insert_gold_room_user", goldRoomUser);
        } catch (Exception e) {
            LogUtil.errorLog.error("saveGoldRoomUser|error|" + JSON.toJSONString(goldRoomUser), e);
        }
        return 0L;
    }

    public int deleteGoldRoomUser(long roomId, long userId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("roomId", String.valueOf(roomId));
        if (userId != 0L) {
            map.put("userId", String.valueOf(userId));
        }
        return this.getSqlLoginClient().delete("goldRoom.delete_gold_room_user", map);
    }

    public int updateGoldRoomUser(long roomId, long userId, long gameResult, String logIds) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("roomId", String.valueOf(roomId));
        map.put("userId", String.valueOf(userId));
        map.put("gameResult", String.valueOf(gameResult));
        if (StringUtils.isNotBlank(logIds)) {
            map.put("logIds", logIds);
        }
        return this.getSqlLoginClient().update("goldRoom.update_gold_room_user", map);
    }

    public int updateGoldRoom(long keyId, int addCount, String currentState) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("keyId", String.valueOf(keyId));
        if (StringUtils.isNotBlank(currentState)) {
            map.put("currentState", currentState);
        }

        if (addCount == 1) {
            return this.getSqlLoginClient().update("goldRoom.update_gold_room_jia", map);
        } else if (addCount == -1) {
            return this.getSqlLoginClient().update("goldRoom.update_gold_room_jian", map);
        } else {
            if (StringUtils.isNotBlank(currentState)) {
                return this.getSqlLoginClient().update("goldRoom.update_gold_room_currentState", map);
            }
        }

        return -1;
    }

    public int updateGoldRoom0(long keyId, int count, String currentState) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("keyId", String.valueOf(keyId));
        map.put("currentCount", count);
        if (StringUtils.isNotBlank(currentState)) {
            map.put("currentState", currentState);
        }
        return this.getSqlLoginClient().update("goldRoom.update_gold_room_count", map);
    }

    public List<HashMap<String, Object>> loadGoldRoomMsgs(Collection<String> userIds) throws Exception {
        StringBuilder sqlBuilder = new StringBuilder(100 * userIds.size());
        for (String userId : userIds) {
            sqlBuilder.append("UNION (SELECT * FROM t_gold_room_user WHERE userId='").append(userId).append("' ORDER BY keyId DESC LIMIT 1)");
        }
        String sql = sqlBuilder.substring(6);
        return this.getSqlLoginClient().queryForList("goldRoom.select_gold_room_msgs", sql);
    }

    public List<HashMap<String, Object>> loadRoomUsersLastResult(Collection<Long> users, long exclusiveId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        int i = 1;
        for (Long userId : users) {
            map.put("userId" + i, userId.toString());
            i++;
        }

        if (map.size() == 3) {
            map.put("exclusiveId", String.valueOf(exclusiveId));
            return this.getSqlLoginClient().queryForList("goldRoom.select_gold_room_user3_result_last", map);
        } else {
            return null;
        }
    }

    public List<HashMap<String, Object>> loadRooms(String modeId, String... states) throws Exception {
        Map<String, Object> map = new HashMap<>();

        map.put("modeId", modeId);
        if (states != null && states.length > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String state : states) {
                stringBuilder.append(",'").append(state).append("'");
            }
            map.put("states", stringBuilder.substring(1));
        }

        return this.getSqlLoginClient().queryForList("goldRoom.select_gold_room_by_mode", map);
    }

    public List<HashMap<String, Object>> loadRoomUsers(List<String> roomIds) throws Exception {
        if (roomIds != null && roomIds.size() > 0) {
            Map<String, Object> map = new HashMap<>();
            StringBuilder stringBuilder = new StringBuilder();
            for (String roomId : roomIds) {
                stringBuilder.append(",'").append(roomId).append("'");
            }
            map.put("roomIds", stringBuilder.substring(1));
            return this.getSqlLoginClient().queryForList("goldRoom.select_gold_room_users_by_rooms", map);
        }
        return null;
    }

    public int loadUserRoomCount(String modeId, long userId) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("modeId", modeId);
            map.put("userId", String.valueOf(userId));
            return ((Number) this.getSqlLoginClient().queryForObject("goldRoom.select_user_room_count", map)).intValue();
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
        return 0;
    }

    public int countOnline(String modeId) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("modeId", modeId);
            return ((Number) this.getSqlLoginClient().queryForObject("goldRoom.count_online", map)).intValue();
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
        return 0;
    }

    public List<GoldRoomConfig> loadAllGoldRoomConfig() throws Exception {
        return (List<GoldRoomConfig>) this.getSqlLoginClient().queryForList("goldRoom.load_all_gold_room_config");
    }

    public List<GoldRoomArea> loadAllGoldRoomArea() throws Exception {
        return (List<GoldRoomArea>) this.getSqlLoginClient().queryForList("goldRoom.load_all_gold_room_area");
    }

    public List<GoldRoomHall> loadAllGoldRoomHall() throws Exception {
        return (List<GoldRoomHall>) this.getSqlLoginClient().queryForList("goldRoom.load_all_gold_room_hall");
    }

    public List<GoldRoomGroupLimit> loadAllGoldRoomGroupLimit() throws Exception {
        return (List<GoldRoomGroupLimit>) this.getSqlLoginClient().queryForList("goldRoom.load_all_gold_room_group_limit");
    }

    public GoldRoom randomGoldRoom(Long configId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("configId", configId);
        return (GoldRoom) this.getSqlLoginClient().queryForObject("goldRoom.random_gold_room", map);
    }

    public GoldRoom randomGoldRoomByGroupLimit(Long configId, String groupIdLimit) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("configId", configId);
        map.put("groupIdLimit", groupIdLimit);
        return (GoldRoom) this.getSqlLoginClient().queryForObject("goldRoom.random_gold_room_by_group_limit", map);
    }

    public int addGoldRoomPlayerCount(long keyId, int addCount) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("keyId", keyId);
        map.put("addCount", addCount);
        try {
            return this.getSqlLoginClient().update("goldRoom.add_gold_room_player_count", map);
        } catch (Exception e) {
            LogUtil.errorLog.error("addGoldRoomPlayerCount|error|" + keyId + "|" + addCount, e);
        }
        return 0;
    }

    public int freezeGoldRoom(long roomKeyId) {
        Map<String, Object> map = new HashMap<>();
        map.put("keyId", roomKeyId);
        try {
            return this.getSqlLoginClient().update("goldRoom.freeze_gold_room", map);
        } catch (Exception e) {
            LogUtil.errorLog.error("freezeGoldRoom|error|" + roomKeyId, e);
        }
        return 0;
    }

    public int freezeGoldRoomUser(long roomKeyId) {
        Map<String, Object> map = new HashMap<>();
        map.put("roomId", roomKeyId);
        try {
            return this.getSqlLoginClient().update("goldRoom.freeze_gold_room_user", map);
        } catch (Exception e) {
            LogUtil.errorLog.error("freezeGoldRoomUser|error|" + roomKeyId, e);
        }
        return 0;
    }

    public int deleteGoldRoomByKeyId(long roomKeyId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("keyId", roomKeyId);
        return this.getSqlLoginClient().update("goldRoom.delete_gold_room_by_keyId", map);
    }

    public int deleteGoldRoomUserByRoomId(long roomKeyId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("roomId", roomKeyId);
        return this.getSqlLoginClient().update("goldRoom.delete_gold_room_user", map);
    }

    public void insertGoldRoomTableRecord(GoldRoomTableRecord log) {
        try {
            TaskExecutor.EXECUTOR_SERVICE_LOG.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        getSqlLoginClient().insert("goldRoom.insert_gold_room_table_record", log);
                    } catch (Exception e) {
                        LogUtil.e("insertGoldLog|error|" + JSON.toJSONString(log), e);
                    }
                }
            });
        } catch (Exception e) {
            LogUtil.errorLog.error("insertGoldLog|error|" + JSON.toJSONString(log), e);
        }
    }

    public int updateGoldRoomByKeyId(Map<String, Object> map) throws Exception {
        return this.getSqlLoginClient().update("goldRoom.update_gold_room_by_keyId", map);
    }

    public int updateGoldRoomCurrentState(Long keyId, int currentState) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("keyId", keyId);
        map.put("currentState", currentState);
        return updateGoldRoomByKeyId(map);
    }

    public void insertSoloRoomTableRecord(SoloRoomTableRecord log) {
        try {
            TaskExecutor.EXECUTOR_SERVICE_LOG.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        getSqlLoginClient().insert("goldRoom.insert_solo_room_table_record", log);
                    } catch (Exception e) {
                        LogUtil.e("insertGoldLog|error|" + JSON.toJSONString(log), e);
                    }
                }
            });
        } catch (Exception e) {
            LogUtil.errorLog.error("insertGoldLog|error|" + JSON.toJSONString(log), e);
        }
    }

    public long saveGoldRoomMatch(GoldRoomMatchPlayer data){
        try{
            return (Long) this.getSqlLoginClient().insert("goldRoom.save_gold_room_match_player", data);
        } catch (Exception e) {
            LogUtil.errorLog.error("saveGoldRoomMatch|error|" + JSON.toJSONString(data), e);
        }
        return 0L;
    }

    public List<GoldRoomMatchPlayer> loadAllGoldRoomMatchPlayer(int serverId) throws Exception{
        return (List<GoldRoomMatchPlayer>) this.getSqlLoginClient().queryForList("goldRoom.load_all_gold_room_match_player", serverId);
    }

    public int deleteGoldRoomMatchPlayerByUserId(long userId) {
        try {
            return this.getSqlLoginClient().update("goldRoom.delete_gold_room_match_player_by_userId", userId);
        } catch (Exception e) {
            LogUtil.errorLog.error("deleteGoldRoomMatchByUserId|error|" + userId, e);
        }
        return 0;
    }

    public GoldRoomMatchPlayer loadGoldRoomMatchPlayer(long userId) throws Exception{
        return (GoldRoomMatchPlayer) this.getSqlLoginClient().queryForObject("goldRoom.load_gold_room_match_player", userId);
    }

}
