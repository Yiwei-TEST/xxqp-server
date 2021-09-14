package com.sy599.game.db.dao.group;

import com.sy.mainland.util.CommonUtil;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.db.bean.group.*;
import com.sy599.game.db.dao.BaseDao;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.SysPartitionUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.util.constants.GroupConstants;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupDao extends BaseDao {

    private static GroupDao groupDao = new GroupDao();

    public static GroupDao getInstance() {
        return groupDao;
    }

    public GroupInfo loadGroupInfo(long groupId) throws Exception {
        return loadGroupInfo(groupId, 0);
    }

    public GroupInfo loadGroupInfo(long groupId, long parentGroup) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        map.put("parentGroup", String.valueOf(parentGroup));
        return (GroupInfo) this.getSqlLoginClient().queryForObject("group.group_info_id", map);
    }

    public List<HashMap<String, Object>> loadSubGroups(int parentGroup, String groupState) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("parentGroup", String.valueOf(parentGroup));
        if (StringUtils.isNotBlank(groupState)) {
            map.put("groupState", groupState);
        }
        return (List<HashMap<String, Object>>) this.getSqlLoginSlaveClient().queryForList("group.all_group_info_parentGroup", map);
    }

    public List<HashMap<String, Object>> loadSubGroupBaseMsgs(int parentGroup, int groupRoom) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("parentGroup", String.valueOf(parentGroup));
        map.put("groupRoom", groupRoom);
        return (List<HashMap<String, Object>>) this.getSqlLoginClient().queryForList("group.all_group_room_base", map);
    }

    public GroupInfo loadGroupInfo(String groupId) throws Exception {
        return loadGroupInfo(groupId, "0");
    }

    public GroupInfo loadGroupInfo(String groupId, String parentGroup) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("parentGroup", parentGroup);
        return (GroupInfo) this.getSqlLoginClient().queryForObject("group.group_info_id", map);
    }

    public GroupTable loadGroupTable(long userId, long tableId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", String.valueOf(userId));
        map.put("tableId", String.valueOf(tableId));
        return (GroupTable) this.getSqlLoginClient().queryForObject("group.one_group_table_current", map);
    }

    public List<GroupTable> loadGroupTables(long groupId, int groupRoom, int pageNo, int pageSize) throws Exception {
        return loadGroupTables(groupId, groupRoom, 1, 0, 2, pageNo, pageSize);

    }

    public List<GroupTable> loadGroupTables(long groupId, int groupRoom, int orStarted, int andNotFull, int tableOrder, int pageNo, int pageSize) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        if (orStarted == 1) {
            map.put("orStarted", 1);
        }
        if (andNotFull == 1) {
            map.put("andNotFull", 1);
        }
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        map.put("tableOrder", tableOrder);
        if (groupRoom > 0) {
            map.put("groupRoom", String.valueOf(groupRoom));
            return (List<GroupTable>) this.getSqlLoginSlaveClient().queryForList("group.group_tables_groupId_room", map);
        } else {
            map.put("group", "1");
            return (List<GroupTable>) this.getSqlLoginSlaveClient().queryForList("group.group_tables_groupId_all", map);
        }
    }

    public List<HashMap<String, Object>> loadTableUserInfo(String tableNos, long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("tableNos", tableNos);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(groupId));
        return (List<HashMap<String, Object>>) this.getSqlLoginClient().queryForList("group.load_table_user_info", map);
    }

    public Integer loadGroupTableCount(Object groupId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        return (Integer) this.getSqlLoginClient().queryForObject("group.count_group_table_current_group", map);
    }

    public Integer loadGroupTableCount(Object groupId, String currentState) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("currentState", currentState);
        return (Integer) this.getSqlLoginClient().queryForObject("group.count_group_table_current_group1", map);
    }

    public Integer loadMyGroupTableCount(Object groupId, Object userId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("userId", String.valueOf(userId));
        return (Integer) this.getSqlLoginClient().queryForObject("group.count_group_table_current_user", map);
    }

    public GroupTable loadRandomGroupTable(long modeId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("configId", String.valueOf(modeId));
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        map.put("myDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
        return (GroupTable) this.getSqlLoginClient().queryForObject("group.one_group_table_random", map);
    }

    public int loadGroupCount(long userId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", String.valueOf(userId));
        Integer integer = (Integer) this.getSqlLoginClient().queryForObject("group.user_all_group_count", map);
        return integer == null ? 0 : integer.intValue();
    }

    public GroupTable loadRandomGroupTable(long modeId, String serverId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("configId", String.valueOf(modeId));
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        map.put("myDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
        map.put("serverId", serverId);
        return (GroupTable) this.getSqlLoginClient().queryForObject("group.one_group_table_random_server", map);
    }

    public GroupTable loadRandomSameModelTable(long modeId, int groupId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("configId", modeId > 0 ? String.valueOf(modeId) : "0");
        return (GroupTable) this.getSqlLoginClient().queryForObject("group.one_group_table_same_model", map);
    }

    public GroupTable loadRandomSameModelTableforSpy(long modeId, int groupId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("configId", modeId > 0 ? String.valueOf(modeId) : "0");
        return (GroupTable) this.getSqlLoginClient().queryForObject("group.one_group_table_same_model_spy", map);
    }

    public GroupTable loadGroupTableByKeyId(String keyId) throws Exception {
        return (GroupTable) this.getSqlLoginClient().queryForObject("group.one_group_table_keyId", keyId);
    }

    public Long createGroupTable(GroupTable groupTable) throws Exception {
        return (Long) this.getSqlLoginClient().insert("group.create_group_table", groupTable);
    }

    public Object createTableUser(TableUser tableUser) throws Exception {
        Object res = null;
        if(SysPartitionUtil.isWriteMaster()) {
            tableUser.setGpSeq(SysPartitionUtil.getGroupSeqForMaster(tableUser.getGroupId()));
            res = this.getSqlLoginClient().insert("group.create_table_user", tableUser);
        }

        if (SysPartitionUtil.isWritePartition()) {
            tableUser.setGpSeq(SysPartitionUtil.getGroupSeqForPartition(tableUser.getGroupId()));
            this.getSqlLoginClient().insert("group.create_table_user", tableUser);
        }
        return res;
    }

    public Object createTableRecord(TableRecord tableRecord) throws Exception {
        return this.getSqlLoginClient().insert("group.insert_table_record", tableRecord);
    }

    public GroupUser loadGroupUser(long userId, long groupId) {
        return loadGroupUser(userId,String.valueOf(groupId));
    }

    public GroupUser loadGroupUser(long userId, String groupId) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", String.valueOf(userId));
        if (StringUtils.isNotBlank(groupId) && !"0".equals(groupId)) {
            map.put("groupId", groupId);
        }
        Object o = null;
        try {
            o = this.getSqlLoginClient().queryForObject("group.group_user_userId", map);
        } catch (SQLException e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
        if (o == null) {
            return null;
        } else {
            return (GroupUser) o;
        }
    }

    public GroupUser loadGroupMaster(String groupId) throws Exception {
        return (GroupUser) this.getSqlLoginClient().queryForObject("group.loadGroupMaster", groupId);
    }

    public GroupUser loadPromoter(String groupId, long userId, String promoterId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("promoterId", promoterId);
        return (GroupUser) this.getSqlLoginClient().queryForObject("group.loadPromoter", map);
    }

    public GroupUser loadGroupMasterOld(String groupId) throws Exception {
        return (GroupUser) this.getSqlLoginClient().queryForObject("group.group_user_master_userId", groupId);
    }

    public List<HashMap<String, Object>> loadAllGroupUser(Object groupId) throws Exception {
        return (List<HashMap<String, Object>>) this.getSqlLoginClient().queryForList("group.group_user_all", String.valueOf(groupId));
    }

    public List<HashMap<String, Object>> loadAllLastGroupTableConfig(String groupIds, String parentGroup) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("parentGroup", parentGroup);
        if (StringUtils.isNotBlank(groupIds)) {
            map.put("groupIds", groupIds);
        }
        return (List<HashMap<String, Object>>) this.getSqlLoginClient().queryForList("group.all_last_group_table_config", map);
    }

    public GroupTableConfig loadGroupTableConfig(long keyId) throws Exception {
        return (GroupTableConfig) this.getSqlLoginSlaveClient().queryForObject("group.one_group_table_config_keyId", String.valueOf(keyId));
    }

    public List<GroupTableConfig> loadGroupTableConfig(int groupId, int parentGroup) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        if (groupId > 0) {
            map.put("groupId", String.valueOf(groupId));
        }
        map.put("parentGroup", String.valueOf(parentGroup));
        return (List<GroupTableConfig>) this.getSqlLoginClient().queryForList("group.all_group_table_config", map);
    }

    public List<GroupTableConfig> loadGroupTableConfig2(int groupId, int parentGroup) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        if (groupId > 0) {
            map.put("groupId", String.valueOf(groupId));
        }
        map.put("parentGroup", String.valueOf(parentGroup));
        return (List<GroupTableConfig>) this.getSqlLoginClient().queryForList("group.all_group_table_config2", map);
    }

    public List<GroupTableConfig> loadGroupTableLastConfig(int groupId, int parentGroup) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        map.put("parentGroup", String.valueOf(parentGroup));
        return (List<GroupTableConfig>) this.getSqlLoginClient().queryForList("group.last_group_table_config", map);
    }

    public GroupTableConfig loadRandomGroupTableConfig(String parentGroup, String groupId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("parentGroup", parentGroup);
        map.put("groupId", groupId);
        return (GroupTableConfig) this.getSqlLoginClient().queryForObject("group.one_group_table_config_groupId", map);
    }

    public int updateGroupTableConfigByKeyId(HashMap<String, Object> map) throws Exception {
        return this.getSqlLoginClient().update("group.update_group_table_config_keyId", map);
    }

    public int updateTableUserScore(int score, long userId, long tableNo, int isWinner, long winLoseCredit, long commissionCredit, int userGroup, long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("playResult", String.valueOf(score));
        map.put("userId", String.valueOf(userId));
        map.put("tableNo", String.valueOf(tableNo));
        map.put("isWinner", String.valueOf(isWinner));
        map.put("winLoseCredit", winLoseCredit);
        map.put("commissionCredit", commissionCredit);
        map.put("createdTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        map.put("userGroup", userGroup);
        int res = 0;
        if(SysPartitionUtil.isWriteMaster()) {
            map.put("gpSeq", SysPartitionUtil.getGroupSeqForMaster(groupId));
            res = this.getSqlLoginClient().update("group.update_table_user_score", map);
        }
        if (SysPartitionUtil.isWritePartition()) {
            map.put("gpSeq", SysPartitionUtil.getGroupSeqForPartition(groupId));
            res = this.getSqlLoginClient().update("group.update_table_user_score", map);
        }
        return res;
    }

    public int updateGroupTableByKeyId(HashMap<String, Object> map) throws Exception {
        return this.getSqlLoginClient().update("group.update_group_table", map);
    }

    public int updateGroupUser(HashMap<String, Object> map) throws Exception {
        return this.getSqlLoginClient().update("group.update_group_user", map);
    }

    public int deleteTableUser(String tableNo, long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("tableNo", tableNo);
        int res = 0;
        if(SysPartitionUtil.isWriteMaster()) {
            map.put("gpSeq", SysPartitionUtil.getGroupSeqForMaster(groupId));
            res = this.getSqlLoginClient().delete("group.delete_table_user_tableNo", map);
        }
        if (SysPartitionUtil.isWritePartition()) {
            map.put("gpSeq", SysPartitionUtil.getGroupSeqForPartition(groupId));
            res = this.getSqlLoginClient().delete("group.delete_table_user_tableNo", map);
        }
        return res;
    }

    public int deleteGroupTableByKeyId(Long keyId) throws Exception {
        return this.getSqlLoginClient().delete("group.delete_group_table_keyId", keyId.toString());
    }

    public int deleteTableUser(String tableNo, String userId, long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("tableNo", tableNo);
        map.put("userId", userId);
        int res = 0;
        if(SysPartitionUtil.isWriteMaster()) {
            map.put("gpSeq", SysPartitionUtil.getGroupSeqForMaster(groupId));
            res = this.getSqlLoginClient().delete("group.delete_table_user_unique", map);
        }
        if (SysPartitionUtil.isWritePartition()) {
            map.put("gpSeq", SysPartitionUtil.getGroupSeqForPartition(groupId));
            res = this.getSqlLoginClient().delete("group.delete_table_user_unique", map);
        }
        return res;
    }

    public GroupTableConfig loadLastGroupTableConfig(long groupId, int parentGroup) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("parentGroup", String.valueOf(parentGroup));
        return (GroupTableConfig) this.getSqlLoginClient().queryForObject("group.last_one_group_table_config", map);
    }

    public Long saveGroupMatch(long userId, String groupId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", String.valueOf(userId));
        map.put("groupCode", groupId);
        map.put("createdTime", CommonUtil.dateTimeToString());
        return (Long) this.getSqlLoginClient().insert("group.save_group_match_user", map);
    }

    public HashMap<String, Object> loadGroupMatch(long userId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", String.valueOf(userId));
        return (HashMap<String, Object>) this.getSqlLoginClient().queryForObject("group.load_group_match_user", map);
    }

    public int quitGroupMatch(long userId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", String.valueOf(userId));
        return this.getSqlLoginClient().delete("group.quit_group_match_user", map);
    }

    public int quitGroupMatch(List<String> userIds) throws Exception {
        Map<String, Object> map = new HashMap<>();
        StringBuilder strBuilder = new StringBuilder();
        for (String userId : userIds) {
            strBuilder.append(",'").append(userId).append("'");
        }
        map.put("userIds", strBuilder.substring(1));
        return this.getSqlLoginClient().delete("group.quit_group_match_users", map);
    }

    public List<HashMap<String, Object>> loadGroupMatchUsers(String groupId, int size) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupCode", groupId);
        map.put("lockCode", "lock" + groupId);
        map.put("size", size);
        return (List<HashMap<String, Object>>) this.getSqlLoginClient().queryForList("group.load_group_match_users", map);
    }

    public int countGroupMatchUsers(String groupId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupCode", groupId);
        map.put("lockCode", "lock" + groupId);
        return (Integer) this.getSqlLoginClient().queryForObject("group.count_group_match_users", map);
    }

    public boolean lockGroupMatch(String groupId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", "lock" + groupId);
        map.put("createdTime", CommonUtil.dateTimeToString());
        return this.getSqlLoginClient().update("group.lock_group_match", map) == 1;
    }

    public int unlockGroupMatch(String groupId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", "lock" + groupId);
        return this.getSqlLoginClient().delete("group.quit_group_match_user", map);
    }

    public int countGroupTables(Number groupId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId.toString());
        Integer ret = (Integer) this.getSqlLoginClient().queryForObject("group.count_tables_groupId", map);
        return ret == null ? 0 : ret.intValue();
    }

    /**
     * 获取俱乐部在线成员，随机或limit条
     *
     * @param groupId
     * @param limit
     * @return
     * @throws Exception
     */
    public List<HashMap<String, Object>> loadRandomGroupUsers(String groupId, int limit) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("limit", limit);
        return (List<HashMap<String, Object>>) this.getSqlLoginClient().queryForList("group.random_group_user_limit", map);
    }

    /**
     * 获取俱乐部在线人员信息（没有房间）
     *
     * @param groupId
     * @param userIds
     * @return
     * @throws Exception
     */
    public List<HashMap<String, Object>> loadOnlineGroupUsers(String groupId, String userIds) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        if (StringUtils.isNotBlank(userIds)) {
            map.put("userIds", userIds);
        }
        return (List<HashMap<String, Object>>) this.getSqlLoginClient().queryForList("group.group_user_online", map);
    }

    public List<GroupInfo> loadAllGroupRoom(String parentGroup) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("parentGroup", String.valueOf(parentGroup));
        return (List<GroupInfo>) this.getSqlLoginClient().queryForList("group.all_group_room", map);
    }

    public Integer loadGroupRoomTableCount(Object groupId, String currentState, String groupRoom) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("currentState", currentState);
        map.put("tableMsg", "'%\"room\":\"" + groupRoom + "\"%'");
        return (Integer) this.getSqlLoginClient().queryForObject("group.count_group_room_table", map);
    }


    public int updateGroupUserCredit(Map<String, Object> map) throws Exception {
        return this.getSqlLoginClient().update("group.update_group_user_credit", map);
    }


    public int updateGroupCredit(String groupId, long userId, long credit) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("credit", credit);
        return GroupDao.getInstance().updateGroupUserCredit(map);
    }

    public void insertGroupCreditLog(Map<String, Object> map) {
        try {
            TaskExecutor.EXECUTOR_SERVICE_STATISTICS.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(SysPartitionUtil.isWriteMaster()) {
                            map.put("gpSeq", SysPartitionUtil.getGroupSeqForMaster(Long.valueOf(String.valueOf(map.get("groupId")))));
                            getSqlLoginClient().insert("group.insert_group_credit_log", map);
                        }
                        if (SysPartitionUtil.isWritePartition()) {
                            map.put("gpSeq", SysPartitionUtil.getGroupSeqForPartition(Long.valueOf(String.valueOf(map.get("groupId")))));
                            getSqlLoginClient().insert("group.insert_group_credit_log", map);
                        }
                    } catch (Exception e) {
                        LogUtil.e("insertGroupCreditLog|error|" + map, e);
                    }
                }
            });
        } catch (Exception e) {
            LogUtil.errorLog.error("insertGroupCreditLog|error|" + map, e);
        }
    }

    public void insertGroupCreditLogSync(Map<String, Object> map) {
        try {
            if (SysPartitionUtil.isWriteMaster()) {
                map.put("gpSeq", SysPartitionUtil.getGroupSeqForMaster(Long.valueOf(String.valueOf(map.get("groupId")))));
                getSqlLoginClient().insert("group.insert_group_credit_log", map);
            }
            if (SysPartitionUtil.isWritePartition()) {
                map.put("gpSeq", SysPartitionUtil.getGroupSeqForPartition(Long.valueOf(String.valueOf(map.get("groupId")))));
                getSqlLoginClient().insert("group.insert_group_credit_log", map);
            }
        } catch (Exception e) {
            LogUtil.e("insertGroupCreditLogSync|error|" + map, e);
        }

    }

    public long createGroup(GroupInfo groupInfo) throws Exception {
        Long ret = (Long) this.getSqlLoginClient().insert("group.create_group_info", groupInfo);
        return ret == null ? -1 : ret.longValue();
    }

    public long createGroupTableConfig(GroupTableConfig groupTableConfig) throws Exception {
        Long ret = (Long) this.getSqlLoginClient().insert("group.insert_group_table_config", groupTableConfig);
        return ret == null ? -1 : ret.longValue();
    }

    public int deleteGroupInfoByKeyId(String keyId) throws Exception {
        return this.getSqlLoginClient().delete("group.delete_group_info_keyId", keyId);
    }

    public int countGroupStartedTables(String groupId, String configId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("configId", configId);
        Integer ret = (Integer) this.getSqlLoginClient().queryForObject("group.count_group_started_table", map);
        return ret == null ? 0 : ret.intValue();
    }

    public GroupTable selectUserGroupTable(String userId, int groupId, int configId) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("groupId", groupId);
        map.put("configId", configId);
        try {
            return (GroupTable) this.getSqlLoginClient().queryForObject("group.select_user_group_table", map);
        } catch (SQLException e) {
            LogUtil.e("select_user_group_table|error|" + userId + "|" + groupId + "|" + "|" + configId + "|", e);
        }
        return null;
    }

    public GroupRelation getGroupRelation(String keyId) {
        try {
            return (GroupRelation) this.getSqlLoginClient().queryForObject("group.load_group_relation", keyId);
        } catch (SQLException e) {
            LogUtil.e("getGroupRelation|error|" + keyId + "|", e);
        }
        return null;
    }

    public GroupUser loadGroupTeamLeader(String groupId, String teamId) {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("teamId", teamId);
        try {
            return (GroupUser) this.getSqlLoginClient().queryForObject("group.load_group_team_leader", map);
        } catch (SQLException e) {
            LogUtil.e("loadGroupTeamLeader|error|" + groupId + "|" + teamId + "|", e);
        }
        return null;
    }

    public GroupTable selectGroupTable(int keyId) {
        Map<String, Object> map = new HashMap<>();
        map.put("keyId", keyId);
        try {
            return (GroupTable) this.getSqlLoginClient().queryForObject("group.select_group_table", map);
        } catch (SQLException e) {
            LogUtil.e("selectGroupTable|error|" + keyId + "|", e);
        }
        return null;
    }

    public void updateGroupTableDealCount(int keyId, int dealCount) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("keyId", keyId);
            map.put("dealCount", dealCount);
            getSqlLoginClient().update("group.update_group_table_dealCount", map);
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
    }

    public int selectGroupTableDealCount(long keyId) {
        Integer dealCount;
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("keyId", keyId);
            dealCount = (Integer) getSqlLoginClient().queryForObject("group.select_group_table_dealCount", map);
            return dealCount == null ? 0 : dealCount;
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
        return 0;
    }

    public String selectCurrentStateById(long keyId) {
        String currentState;
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("keyId", keyId);
            currentState = (String) getSqlLoginClient().queryForObject("group.select_currentState_byId", map);
            return currentState == null ? "" : currentState;
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
        return "";
    }

    public int selectCurrentCountById(long keyId) {
        Integer currentCount;
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("keyId", keyId);
            currentCount = (Integer) getSqlLoginClient().queryForObject("group.select_currentCount_byId", map);
            return currentCount == null ? 0 : currentCount;
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
        return 0;
    }

    public List<GroupTable> loadGroupTablesGroupId(long groupId, int groupRoom, int pageNo, int pageSize) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        map.put("orStarted", 1);
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        if (groupRoom > 0) {
            map.put("groupRoom", String.valueOf(groupRoom));
            return (List<GroupTable>) this.getSqlLoginSlaveClient().queryForList("group.group_tables_groupId_room", map);
        } else {
            map.put("group", "1");
            return (List<GroupTable>) this.getSqlLoginSlaveClient().queryForList("group.group_tables_groupId", map);
        }
    }

    public List<GroupTable> loadGroupTablesGroupId(long groupId, String currentState) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("currentState", currentState);
        return (List<GroupTable>) this.getSqlLoginClient().queryForList("group.all_group_table_current_group", map);
    }


    public void addGroupTableDealCount(int keyId) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("keyId", keyId);
            getSqlLoginClient().update("group.add_group_table_dealCount", map);
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
    }

    public int bindIsNewBjd(String groupId, long userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        try {
            return this.getSqlLoginClient().update("group.bindIsNewBjd", map);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long loadIsNewBjdBindGroup(long userId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        Long res = (Long) this.getSqlLoginClient().queryForObject("group.loadIsNewBjdBind", map);
        return res == null ? 0 : res;
    }

    public Integer countGroupConfigTable(Object groupId, String currentState, long configId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("currentState", currentState);
        map.put("configId", configId);
        return (Integer) this.getSqlLoginClient().queryForObject("group.countGroupConfigTable", map);
    }

    public GroupCommissionConfig loadCommissionConfigBySeq(long groupId, long userId, int seq) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("seq", seq);
        return (GroupCommissionConfig) this.getSqlLoginClient().queryForObject("group.loadCommissionConfigBySeq", map);
    }

    public GroupCommissionConfig loadCommissionConfigBySeq(String groupId, long userId, int seq) throws Exception {
        return loadCommissionConfigBySeq(Long.valueOf(groupId), userId, seq);
    }

    public Integer loadGroupRoomEmptyTableCount(long groupId, String currentState, long configId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("currentState", currentState);
        map.put("configId", configId);
        return (Integer) this.getSqlLoginClient().queryForObject("group.loadGroupRoomEmptyTableCount", map);
    }

    public int updateTableUserIsNewPlayer(long userId, long tableNo, int isNewPlayer, long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("tableNo", tableNo);
        map.put("isNewPlayer", isNewPlayer);
        int res = 0;
        if(SysPartitionUtil.isWriteMaster()) {
            map.put("gpSeq", SysPartitionUtil.getGroupSeqForMaster(groupId));
            res = this.getSqlLoginClient().update("group.updateTableUserIsNewPlayer", map);
        }
        if(SysPartitionUtil.isWritePartition()) {
            map.put("gpSeq", SysPartitionUtil.getGroupSeqForPartition(groupId));
            res = this.getSqlLoginClient().update("group.updateTableUserIsNewPlayer", map);
        }
        return res;
    }

    public GroupTable loadGroupTableByKeyIdMaster(String keyId) throws Exception {
        return (GroupTable) this.getSqlLoginClient().queryForObject("group.one_group_table_keyId_master", keyId);
    }

    public GroupUser loadGroupUserForceMaster(long userId, String groupId) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", String.valueOf(userId));
        map.put("groupId", groupId);
        try {
            return (GroupUser) this.getSqlLoginClient().queryForObject("group.loadGroupUserForceMaster", map);
        } catch (SQLException e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
        return null;
    }

    public int addGroupExp(long groupKeyId, long exp) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("keyId", groupKeyId);
        map.put("exp", exp);
        return this.getSqlLoginClient().update("group.addGroupExp", map);
    }

    public int calcGroupLevel(long groupKeyId) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("keyId", groupKeyId);
        return this.getSqlLoginClient().update("group.calcGroupLevel", map);
    }

    public int addGroupUserExp(long groupUserKeyId, long exp) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("keyId", groupUserKeyId);
        map.put("exp", exp);
        return this.getSqlLoginClient().update("group.addGroupUserExp", map);
    }

    public int calcGroupUserLevel(long groupUserKeyId) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("keyId", groupUserKeyId);
        return this.getSqlLoginClient().update("group.calcGroupUserLevel", map);
    }

    public int insertLogGroupExp(HashMap<String, Object> map) throws Exception {
        return this.getSqlLoginClient().update("group.insertLogGroupExp", map);
    }

    public int insertLogGroupUserExp(HashMap<String, Object> map) throws Exception {
        return this.getSqlLoginClient().update("group.insertLogGroupUserExp", map);
    }

    public int insertLogGroupUserLevel(HashMap<String, Object> map) throws Exception {
        try {
            return this.getSqlLoginClient().update("group.insertLogGroupUserLevel", map);
        } catch (Exception e) {
            LogUtil.errorLog.error("insertLogGroupUserLevel|Exception:" + e.getMessage(), e);
        }
        return 0;
    }

    public int updateGroupInfo(HashMap<String, Object> map) throws Exception {
        return this.getSqlLoginClient().update("group.updateGroupInfo", map);
    }

    public int calcGroupExp(long groupKeyId) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("keyId", groupKeyId);
        return this.getSqlLoginClient().update("group.calcGroupExp", map);
    }

    public int calcGroupUserExp(long groupUserKeyId) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("keyId", groupUserKeyId);
        return this.getSqlLoginClient().update("group.calcGroupUserExp", map);
    }

    public int countGroupUserReject(long groupId, long userId1, long userId2) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userIdKeyStr", GroupConstants.genGroupUserRejectKeyStr(userId1, userId2));
        return (Integer) this.getSqlLoginClient().queryForObject("group.countGroupUserReject", map);
    }

    public Map<String, Long> countGroupStartedTablesAll(String groupId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        List<HashMap<String, Long>> list = (List<HashMap<String, Long>>) this.getSqlLoginClient().queryForList("group.count_group_started_table_all", map);
        Map<String, Long> res = new HashMap<>();
        if (list != null && list.size() > 0) {
            for (HashMap<String, Long> data : list) {
                res.put(data.get("configId").toString(), data.get("ct"));
            }
        }
        return res;
    }

    public Map<String, Long> countFakeTablesAll() throws Exception {
        List<HashMap<String, Long>> list = (List<HashMap<String, Long>>) this.getSqlLoginClient().queryForList("group.count_fake_table_all");
        Map<String, Long> res = new HashMap<>();
        if (list != null && list.size() > 0) {
            for (HashMap<String, Long> data : list) {
                res.put(data.get("configId").toString(), data.get("ct"));
            }
        }
        return res;
    }

    public List<HashMap<String, Object>> loadTableUserInfoForceMaster(String tableNos, long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("tableNos", tableNos);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(groupId));
        return (List<HashMap<String, Object>>) this.getSqlLoginClient().queryForList("group.load_table_user_info_force_master", map);
    }

    public List<HashMap<String, Object>> searchGroupUserListForInvite(String groupId, long userId) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        return (List<HashMap<String, Object>>) this.getSqlLoginClient().queryForList("group.searchGroupUserListForInvite", map);
    }

    public List<HashMap<String, Object>> loadGroupUserFriendList(String groupId, long userId, int pageNo, int pageSize) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<HashMap<String, Object>>) this.getSqlLoginClient().queryForList("group.loadGroupUserFriendList", map);
    }

    public long createGroupUserFriend(Map<String, Object> friend) throws Exception {
        Long ret = (Long) this.getSqlLoginClient().insert("group.createGroupUserFriend", friend);
        return ret == null ? -1 : ret.longValue();
    }

    public int countGroupUserFriend(long groupId, long userId1, long userId2) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userIdKeyStr", GroupConstants.genGroupUserFriendKeyStr(userId1, userId2));
        return (Integer) this.getSqlLoginClient().queryForObject("group.countGroupUserFriend", map);
    }

    public Integer deleteGroupUserFriend(long keyId, long groupId) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("keyId", keyId);
        map.put("groupId", groupId);
        return this.getSqlLoginClient().delete("group.deleteGroupUserFriend", map);
    }

    public List<HashMap<String, Object>> loadGroupUserByUserIdsOrUserRoles(long groupId, String userIds, String userRoles) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        String andSql = " AND ( gu.userId in ( " + userIds + ")";
        if (StringUtils.isNotBlank(userRoles)) {
            andSql += " OR gu.userRole in ( " + userRoles + " ) ";
        }
        andSql += " ) ";
        map.put("andSql", andSql);
        return (List<HashMap<String, Object>>) this.getSqlLoginClient().queryForList("group.load_group_user_by_userIds_or_userRoles", map);
    }

    public int updateGroupScore(String groupId, long userId, long score) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("score", score);
        return this.getSqlLoginClient().update("group.update_group_user_score", map);
    }

    public int resetGroupUserScore(String groupId, long userId, long score) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("score", score);
        return this.getSqlLoginClient().update("group.reset_group_user_score", map);
    }

    public List<HashMap<String, Object>> loadGroupNameByUser(long userId) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("userId", userId);
        return (List<HashMap<String, Object>>) this.getSqlLoginClient().queryForList("group.load_group_name_by_user", map);
    }

    public GroupGoldCommissionConfig loadGoldCommissionConfigBySeq(long groupId, long userId, int seq) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("seq", seq);
        return (GroupGoldCommissionConfig) this.getSqlLoginClient().queryForObject("group.load_gold_commission_config_bySeq", map);
    }

    public void insertGroupGoldLog(Map<String, Object> map) {
        try {
            TaskExecutor.EXECUTOR_SERVICE_STATISTICS.execute(new Runnable() {
                @Override
                public void run() {
                    try {
//                        if(SysPartitionUtil.isWriteMaster()) {
//                            map.put("gpSeq", SysPartitionUtil.getGroupSeqForMaster(Long.valueOf(String.valueOf(map.get("groupId")))));
//                            getSqlLoginClient().insert("group.insert_group_gold_log", map);
//                        }
                        if (SysPartitionUtil.isWritePartition()) {
                            map.put("gpSeq", SysPartitionUtil.getGroupSeqForPartition(Long.valueOf(String.valueOf(map.get("groupId")))));
                            getSqlLoginClient().insert("group.insert_group_gold_log", map);
                        }
                    } catch (Exception e) {
                        LogUtil.e("insertGoldLog|error|" + map, e);
                    }
                }
            });
        } catch (Exception e) {
            LogUtil.errorLog.error("insertGoldLog|error|" + map, e);
        }
    }


    public List<GroupInfo> getGroupAllCredit(long groupId,int indexBegin) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("indexBegin",indexBegin);
            if(groupId > 0){
                map.put("groupId", groupId);
                return (List<GroupInfo>) this.getSqlLoginClient().queryForList("group.getGroupAllCreditId", map);
            }else{
                return (List<GroupInfo>) this.getSqlLoginClient().queryForList("group.getGroupAllCredit", map);
            }
        } catch (SQLException e) {
            LogUtil.e("getGroupAllCredit err", e);
        }
        return null;
    }

    public int updateGroupIsCredit(Long groupId, int credit) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("credit", credit);
        map.put("isCreditUpTime", TimeUtil.currentTimeMillis());
        return this.getSqlLoginClient().update("group.updateGroupIsCredit", map);
    }

    public List<GroupUser> loadGroupUserListForceMaster(List<Long> userIdList, Long groupId) {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userIdList", StringUtil.implode(userIdList, ","));
        try {
            return (List<GroupUser>) this.getSqlLoginClient().queryForList("group.loadGroupUserListForceMaster", map);
        } catch (SQLException e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
        return null;
    }

    public List<GroupFakeTable> loadGroupFakeTable(long groupId, String gameTypes, Long configId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        if (StringUtils.isNotBlank(gameTypes)) {
            map.put("gameTypes", gameTypes);
        }
        if (configId != null && configId >= 0) {
            map.put("configId", configId);
        }
        return this.getSqlLoginClient().queryForList("group.load_group_fake_table", map);
    }

    public List<Map<String,Object>> loadFakeImgByTableId(String fakeTableIds) throws Exception{
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(fakeTableIds)) {
            map.put("fakeTableIds", fakeTableIds);
        }
        return getSqlLoginClient().queryForList("group.select_fake_headimage", map);
    }

    public List<Map<String,Object>> loadTableConfigOrder(long groupId) throws Exception{
        Map<String, Object> map = new HashMap<>();
        map.put("parentGroup", groupId);
        return getSqlLoginClient().queryForList("group.select_table_config_order", map);
    }

    public int settleTableTempCredit(long userIds) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userIds);
        return this.getSqlLoginClient().update("group.settle_table_tempCredit_uid", map);
    }


    /**
     * 更新玩家零钱包(返佣信用分先加到零钱包)
     * @param groupId
     * @param userId
     * @param credit
     * @return
     * @throws Exception
     */
    public int updateGroupCreditPurse(String groupId, long userId, long credit) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("credit", credit);
        return this.getSqlLoginClient().update("group.update_group_user_credit_purse", map);
    }

    public HashMap<String, Object> loadGroupUserwheel(long guId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("guId", String.valueOf(guId));
        return (HashMap<String, Object>) this.getSqlLoginClient().queryForObject("group.loadGroupUserwheel", map);
    }

    public int saveGroupUserwheel(long guId, long groupId, int lastCount, int wheelCount) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        map.put("guId", guId);
        map.put("groupId", groupId);
        map.put("lastCount", lastCount);
        map.put("wheelCount", wheelCount);
        return this.getSqlLoginClient().update("group.saveGroupUserwheel", map);
    }

    public int savePlayWithSpy(long guId, long groupId, long userId, int playCount) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        map.put("guId", guId);
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("playCount", playCount);
        return this.getSqlLoginClient().update("group.savePlayWithSpy", map);
    }

}
