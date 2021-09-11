package com.sy.sanguo.game.dao.group;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy.sanguo.common.parent.impl.CommonDaoImpl;
import com.sy.sanguo.game.bean.group.*;
import com.sy.sanguo.game.constants.GroupConstants;
import com.sy599.sanguo.util.SysPartitionUtil;
import com.sy599.sanguo.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupDaoNew extends CommonDaoImpl {

    public long createGroup(GroupInfo groupInfo) throws Exception {
        Long ret = (Long) this.getSqlMapClient().insert("groupNew.createGroup", groupInfo);
        return ret == null ? -1 : ret.longValue();
    }

    public GroupInfo loadGroupInfo(long groupId) throws Exception {
        return loadGroupInfo(groupId, 0);
    }

    public GroupInfo loadGroupInfo(long groupId, long parentGroup) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("parentGroup", parentGroup);
        return (GroupInfo) this.getSqlMapClient().queryForObject("groupNew.loadGroupInfo", map);
    }

    public GroupInfo loadGroupForceMaster(long groupId) throws Exception {
        return loadGroupForceMaster(groupId, 0);
    }

    public GroupInfo loadGroupForceMaster(long groupId, long parentGroup) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("parentGroup", parentGroup);
        return (GroupInfo) this.getSqlMapClient().queryForObject("groupNew.loadGroupForceMaster", map);
    }

    public GroupInfo loadGroupByKeyId(long keyId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("keyId", keyId);
        return (GroupInfo) this.getSqlMapClient().queryForObject("groupNew.loadGroupByKeyId", map);
    }

    public int updateGroupByKeyId(HashMap<String, Object> map) throws Exception {
        return this.getSqlMapClient().update("groupNew.updateGroupByKeyId", map);
    }

    public int updateGroupUserCount(long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        map.put("parentGroup", "0");
        return this.getSqlMapClient().update("groupNew.updateGroupUserCount", map);
    }

    public boolean existsGroupInfo(long groupId, long parentGroup) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        map.put("parentGroup", String.valueOf(parentGroup));
        Integer ret = (Integer) this.getSqlMapClient().queryForObject("groupNew.existsGroupInfo", map);
        return ret == null || ret.intValue() > 0;
    }

    public int countGroupByCreator(long userId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("userId", String.valueOf(userId));
        return (Integer) this.getSqlMapClient().queryForObject("groupNew.countGroupByCreator", map);
    }

    public int deleteGroupInfoByGroupId(Integer groupId, Integer parentGroup) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId.toString());
        map.put("parentGroup", parentGroup.toString());
        return this.getSqlMapClient().delete("groupNew.deleteGroupInfoByGroupId", map);
    }

    public long createGroupUser(GroupUser groupUser) throws Exception {
        Long ret = (Long) this.getSqlMapClient().insert("groupNew.createGroupUser", groupUser);
        return ret == null ? -1 : ret.longValue();
    }

    public int createGroupUserBatch(List<GroupUser> list) throws Exception {
        SqlMapClient sqlMapClient = this.getSqlMapClient();
        sqlMapClient.startBatch();
        for (GroupUser gu : list) {
            sqlMapClient.update("groupNew.createGroupUser", gu);
        }
        sqlMapClient.executeBatch();
        return 0;
    }

    public int updateGroupUserByKeyId(HashMap<String, Object> map) throws Exception {
        return this.getSqlMapClient().update("groupNew.updateGroupUserByKeyId", map);
    }

    public int updateGroupUserCredit(HashMap<String, Object> map) throws Exception {
        return this.getSqlMapClient().update("groupNew.updateGroupUserCredit", map);
    }

    public List<GroupUser> loadGroupUsersByUser(long userId, int pageNo, int pageSize) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("userId", userId);
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<GroupUser>) this.getSqlMapClient().queryForList("groupNew.loadGroupUsersByUser", map);
    }

    public GroupUser loadGroupUser(long groupId, long userId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        return (GroupUser) this.getSqlMapClient().queryForObject("groupNew.loadGroupUser", map);
    }

    public GroupUser loadGroupUserForceMaster(long groupId, long userId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        return (GroupUser) this.getSqlMapClient().queryForObject("groupNew.loadGroupUserForceMaster", map);
    }

    public List<GroupUser> loadGroupUserByUserIds(long groupId, String userIds) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userIds", userIds);
        return (List<GroupUser>) this.getSqlMapClient().queryForList("groupNew.loadGroupUserByUserIds", map);
    }

    public List<GroupUser> loadAllGroupUser(long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return (List<GroupUser>) this.getSqlMapClient().queryForList("groupNew.loadAllGroupUser", map);
    }

    public GroupUser loadGroupMaster(long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return (GroupUser) this.getSqlMapClient().queryForObject("groupNew.loadGroupMaster", map);
    }

    public List<HashMap<String, Object>> userListAdmin(long groupId, int promoterLevel, long promoterId, int optType, String keyWord) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        String andSql = " AND promoterId" + promoterLevel + " = " + promoterId;
        if (optType == 2) {
            andSql += " AND ( promoterLevel = " + promoterLevel + " or promoterLevel = " + (promoterLevel + 1) + " ) ";
        } else {
            map.put("limitSql", " limit 100");
        }
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord", keyWord);
        }
        map.put("andSql", andSql);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.userListAdmin", map);
    }

    public List<HashMap<String, Object>> userListAll(long groupId, int orderByField, int orderByType, int pageNo, int pageSize, String keyWord) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord", keyWord);
        }
        String orderBySql = " ORDER BY ";
        if (orderByField == 2) {
            orderBySql += " d1.logTime ";
            if (orderByType == 1) {
                orderBySql += " DESC ";
            } else {
                orderBySql += " ASC ";
            }
        } else {
            orderBySql += " d1.userRole asc ,d1.promoterLevel asc ";
        }
        map.put("orderBySql", orderBySql);
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.userListAll", map);
    }

    public Integer countOnlineUserListAll(long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        Integer res = (Integer) this.getSqlMapClient().queryForObject("groupNew.countOnlineUserListAll", map);
        return res == null ? 0 : res;
    }

    public Integer countUserListNextLevel(long groupId, int promoterLevel, long promoterId, String keyWord) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("promoterId", promoterId);
        String andSql = " AND promoterId" + promoterLevel + " = " + promoterId;
        andSql += " AND ( promoterLevel = " + promoterLevel + " or promoterLevel = " + (promoterLevel + 1) + " ) ";
        map.put("andSql", andSql);
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord", keyWord);
        }
        Integer res = (Integer) this.getSqlMapClient().queryForObject("groupNew.countUserListNextLevel", map);
        return res == null ? 0 : res;
    }

    public Integer countOnlineUserListNextLevel(long groupId, int promoterLevel, long promoterId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("promoterId", promoterId);
        String andSql = " AND gu.promoterId" + promoterLevel + " = " + promoterId;
        andSql += " AND ( gu.promoterLevel = " + promoterLevel + " or gu.promoterLevel = " + (promoterLevel + 1) + " ) ";
        map.put("andSql", andSql);
        Integer res = (Integer) this.getSqlMapClient().queryForObject("groupNew.countOnlineUserListNextLevel", map);
        return res == null ? 0 : res;
    }

    public List<HashMap<String, Object>> userListNextLevel(long groupId, int promoterLevel, long promoterId, int orderByField, int orderByType, int pageNo, int pageSize, String keyWord) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("promoterId", promoterId);
        String andSql = " AND promoterId" + promoterLevel + " = " + promoterId;
        andSql += " AND ( promoterLevel = " + promoterLevel + " or promoterLevel = " + (promoterLevel + 1) + " ) ";
        map.put("andSql", andSql);
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord", keyWord);
        }

        String orderBySql = " ORDER BY ";
        if (orderByField == 2) {
            orderBySql += " d1.logTime ";
            if (orderByType == 1) {
                orderBySql += " DESC ";
            } else {
                orderBySql += " ASC ";
            }
        } else {
            orderBySql += " d1.userRole asc ,d1.promoterLevel asc ";
        }
        map.put("orderBySql", orderBySql);
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.userListNextLevel", map);
    }

    public List<HashMap<String, Object>> teamUserList(long groupId, int promoterLevel, long promoterId, int orderByField, int orderByType, int pageNo, int pageSize, String keyWord) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("promoterId", promoterId);
        String andSql = " AND promoterId" + promoterLevel + " = " + promoterId;
        andSql += " AND ( promoterLevel = " + promoterLevel + " or promoterLevel = " + (promoterLevel + 1) + " ) ";
        map.put("andSql", andSql);
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord", keyWord);
        }

        String orderBySql = " ORDER BY ";
        if (orderByField == 1) {
            orderBySql += " d1.userRole ";
        } else {
            orderBySql += " d1.credit ";
        }
        if (orderByField == 1 && orderByType != 3) {
            orderByType = orderByType == 2 ? 1 : 2;
        }
        if (orderByType == 1) {
            orderBySql += " ASC ";
        } else if (orderByType == 2) {
            orderBySql += " DESC ";
        } else {
            orderBySql = " ORDER BY d1.userRole asc , d1.promoterLevel asc , d1.credit desc ";
        }
        map.put("orderBySql", orderBySql);
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.teamUserList", map);
    }

    public Integer countUserListForSearch(long groupId, int promoterLevel, long promoterId, String keyWord) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        String andSql = "AND promoterId" + promoterLevel + " = " + promoterId;
        map.put("andSql", andSql);
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord", keyWord);
        }
        Integer res = (Integer) this.getSqlMapClient().queryForObject("groupNew.countUserListForSearch", map);
        return res == null ? 0 : res;
    }

    public List<HashMap<String, Object>> userListForSearch(long groupId, int promoterLevel, long promoterId, int pageNo, int pageSize, String keyWord) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        String andSql = " AND promoterId" + promoterLevel + " = " + promoterId;
        map.put("andSql", andSql);
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord", keyWord);
        }
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.userListForSearch", map);
    }

    public List<Map<String, Object>> loadGroupManagers(long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return (List<Map<String, Object>>) this.getSqlMapClient().queryForList("groupNew.loadGroupManagers", map);
    }

    public GroupReview loadTeamInvite(long groupId, long userId, long inviterId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("inviterId", inviterId);
        return (GroupReview) this.getSqlMapClient().queryForObject("groupNew.loadTeamInvite", map);
    }

    public int rejectTeamInvite(long groupId, long userId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        return this.getSqlMapClient().update("groupNew.rejectTeamInvite", map);
    }


    public Integer countTeamList(long groupId, int promoterLevel, long promoterId, String keyWord) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        String andSql = " AND gu.promoterLevel = " + (promoterLevel + 1);
        andSql += " AND gu.promoterId = " + promoterId;
        andSql += " AND gu.userRole != 90000 AND userRole != 2 ";
        map.put("andSql", andSql);
        if (StringUtils.isNotEmpty(keyWord)) {
            map.put("keyWord", keyWord);
        }
        return (Integer) this.getSqlMapClient().queryForObject("groupNew.countTeamList", map);
    }

    public List<Map<String, Object>> teamList(long groupId, int promoterLevel, long promoterId, String keyWord, int pageNo, int pageSize) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("promoterId", promoterId);
        map.put("andSql", " AND promoterId" + promoterLevel + " = " + promoterId);
        map.put("groupByKey", " promoterId" + (promoterLevel + 1));
        if (StringUtils.isNotEmpty(keyWord)) {
            map.put("keyWord", keyWord);
        }
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<Map<String, Object>>) this.getSqlMapClient().queryForList("groupNew.teamList", map);
    }

    public long sumCredit(long groupId, int promoterLevel, long promoterId) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        if (promoterId > 0) {
            map.put("andSql", " AND promoterId" + promoterLevel + " = " + promoterId);
        }
        return (Long) this.getSqlMapClient().queryForObject("groupNew.sumCredit", map);
    }

    public long sumCommissionCredit(long groupId, long userId) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());
        long dataDate = Long.valueOf(dateStr);
        return sumCommissionCredit(groupId, userId, dataDate);
    }

    public long sumCommissionCredit(long groupId, long userId, long dataDate) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("dataDate", dataDate);
        Long res = (Long) this.getSqlMapClient().queryForObject("groupNew.sumCommissionCredit", map);
        return res != null ? res : 0l;
    }

    public int getUserGroupTableIdCount(long groupId, long promoterId, int promoterLevel, long queryUserId, long queryTableId, String currentState, int playType, String startDate, String endDate) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("currentState", currentState);
        if (promoterId > 0) {
            // 普通成员查自己
            String andSql = " AND promoterId" + promoterLevel + " = " + promoterId;
            map.put("andSql", andSql);
        }

        if (queryUserId > 0) {
            map.put("queryUserId", queryUserId);
        }
        if (queryTableId > 0) {
            map.put("queryTableId", queryTableId);
        }
        if (playType > 0) {
            map.put("playType", playType);
        }
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(groupId)));
        return (Integer) this.getSqlMapClient().queryForObject("groupNew.getUserGroupTableIdCount", map);
    }

    public List<GroupTable> getUserPlayLogGroupTable(long groupId, long promoterId, int promoterLevel, long queryUserId, long queryTableId, String currentState, int playType, String startDate, String endDate, int pageNo, int pageSize) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("currentState", currentState);
        if (promoterId > 0) {
            // 普通成员查自己
            String andSql = " AND promoterId" + promoterLevel + " = " + promoterId;
            map.put("andSql", andSql);
        }

        if (queryUserId > 0) {
            map.put("queryUserId", queryUserId);
        }
        if (queryTableId > 0) {
            map.put("queryTableId", queryTableId);
        }
        if (playType > 0) {
            map.put("playType", playType);
        }

        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(groupId)));
        return (List<GroupTable>) this.getSqlMapClient().queryForList("groupNew.getUserPlayLogGroupTable", map);
    }

    public boolean isGroupTableOver(long groupId, long tableId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("tableId", tableId);
        Integer count = (Integer) this.getSqlMapClient().queryForObject("groupNew.isGroupTableOver", map);
        return count == null || count == 0;
    }

    public List<HashMap<String, Object>> loadGroupCreditLog(Map<String, Object> map) throws Exception {
        int selectType = (int) map.get("selectType");
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(String.valueOf(map.get("groupId")))));
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.loadGroupCreditLog_" + selectType, map);
    }

    public int countGroupCreditLog(Map<String, Object> map) throws Exception {
        int selectType = (int) map.get("selectType");
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(String.valueOf(map.get("groupId")))));
        return (Integer) this.getSqlMapClient().queryForObject("groupNew.countGroupCreditLog_" + selectType, map);
    }

    public List<GroupCommissionConfig> loadCommissionConfig(long groupId, long userId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        return (List<GroupCommissionConfig>) this.getSqlMapClient().queryForList("groupNew.loadCommissionConfig", map);
    }

    public long insertCommissionConfig(GroupCommissionConfig config) throws Exception {
        return (long) this.getSqlMapClient().insert("groupNew.insertCommissionConfig", config);
    }

    public int updateCommissionConfig(long groupId, long userId, int seq, long credit, long leftCredit, long maxCreditLog) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("seq", seq);
        map.put("credit", credit);
        map.put("leftCredit", leftCredit);
        map.put("maxCreditLog", maxCreditLog);
        map.put("lastUpTime", new Date());
        return this.getSqlMapClient().update("groupNew.updateCommissionConfig", map);
    }

    public List<HashMap<String, Object>> userListUnder(long groupId, int promoterLevel, long promoterId, int pageNo, int pageSize, String keyWord, int creditOrder) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("promoterId", promoterId);
        String andSql = " AND promoterId" + promoterLevel + " = " + promoterId;
        map.put("andSql", andSql);
        map.put("creditOrder", creditOrder);
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord", keyWord);
        }
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.userListUnder", map);
    }

    public Integer countUserListUnder(long groupId, int promoterLevel, long promoterId, String keyWord, int creditOrder) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("promoterId", promoterId);
        String andSql = " AND promoterId" + promoterLevel + " = " + promoterId;
        map.put("andSql", andSql);
        map.put("creditOrder", creditOrder);
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord", keyWord);
        }
        return (Integer) this.getSqlMapClient().queryForObject("groupNew.countUserListUnder", map);
    }

    public Long sumCreditLogList(long groupId, long userId, int type, String startDate, String endDate, int upOrDown,boolean isLookXipai, boolean fullQLType) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        if (type > 0) {
            map.put("type", type);
            if(type == 2 || (type == 6 && fullQLType)) {
                map.put("multiType", type);
            }
        }
        if (upOrDown != 0) {
            map.put("upOrDown", upOrDown);
        }
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(groupId));
        if(!isLookXipai){
            map.put("isLookXipai", 4);
        }
        return (Long) this.getSqlMapClient().queryForObject("groupNew.sumCreditLogList", map);
    }

    public int countCreditLogList(long groupId, long userId, int type, String startDate, String endDate, int upOrDown,boolean isLookXipai, boolean fullQLType) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        if (type > 0) {
            map.put("type", type);
            if(type == 2 || (type == 6 && fullQLType)) {
                map.put("multiType", type);
            }
        }
        if (upOrDown != 0) {
            map.put("upOrDown", upOrDown);
        }
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(groupId));
        if(!isLookXipai){
            map.put("isLookXipai", 4);
        }
        return (Integer) this.getSqlMapClient().queryForObject("groupNew.countCreditLogList", map);
    }

    public List<Map<String, Object>> creditLogList(long groupId, long userId, int type, String startDate, String endDate, int upOrDown, int pageNo, int pageSize,boolean isLookXipai, boolean fullQLType) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        if (type > 0) {
            map.put("type", type);
            if(type == 2 || (type == 6 && fullQLType)) {
                map.put("multiType", type);
            }
        }
        if (upOrDown != 0) {
            map.put("upOrDown", upOrDown);
        }
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(groupId));
        if(!isLookXipai){
            map.put("isLookXipai", 4);
        }
        return (List<Map<String, Object>>) this.getSqlMapClient().queryForList("groupNew.creditLogList", map);
    }

    public List<HashMap<String, Object>> creditStatistics(long groupId, long userId, int promoterLevel, int curLevel) throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        String andSql = " and promoterId" + promoterLevel + " = " + userId;
        map.put("andSql", andSql);
        String groupBySql = " group by promoterId" + curLevel;
        map.put("groupBySql", groupBySql);
        String selSql = " promoterId" + curLevel + " as promoterIdKey ";
        map.put("selSql", selSql);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.creditStatistics", map);
    }

    public Integer creditStatisticsMaxLevel(long groupId, long userId, int promoterLevel) throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        String andSql = " and promoterId" + promoterLevel + " = " + userId;
        map.put("andSql", andSql);
        return (Integer) this.getSqlMapClient().queryForObject("groupNew.creditStatisticsMaxLevel", map);
    }

    public Map<String, String> creditStatisticsMap(long groupId, long userId, int promoterLevel) throws Exception {
        Map<String, String> res = new HashMap<>();
        List<HashMap<String, Object>> list = new ArrayList<>();
        Integer maxLevel = creditStatisticsMaxLevel(groupId, userId, promoterLevel);
        for (int curLevel = promoterLevel; curLevel <= maxLevel; curLevel++) {
            List<HashMap<String, Object>> temp = creditStatistics(groupId, userId, promoterLevel, curLevel);
            if (temp != null && temp.size() > 0) {
                list.addAll(temp);
            }
        }
        if (list != null && list.size() > 0) {
            for (HashMap<String, Object> data : list) {
                String key = data.get("promoterIdKey").toString();
                if (!"0".equals(key)) {
                    res.put(data.get("promoterIdKey").toString(), data.get("sumCredit").toString());
                    res.put("-" + data.get("promoterIdKey").toString(), data.get("sumNegativeCredit").toString());
                }
            }
        }
        return res;
    }

    public List<HashMap<String, Object>> commissionLog(long groupId, long userId, int promoterLevel, long dataDate) throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        String andSql = " and promoterId" + promoterLevel + " = " + userId;
        map.put("andSql", andSql);
        map.put("dataDate", dataDate);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.commissionLog", map);
    }

    public Integer countCommissionLogNextLevel(long groupId, long userId, int promoterLevel, long queryUserId) throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        String andSql = " and promoterId" + promoterLevel + " = " + userId;
        andSql += " and promoterLevel in (" + promoterLevel + "," + (promoterLevel + 1) + ")";
        andSql += " and ( userRole not in (10000,20000,30000) or userId = " + userId + " ) ";
        map.put("andSql", andSql);
        if (queryUserId > 0) {
            map.put("keyWord", queryUserId);
        }
        return (Integer) this.getSqlMapClient().queryForObject("groupNew.countCommissionLogNextLevel", map);
    }

    public List<HashMap<String, Object>> commissionLogNextLevel(long groupId, long userId, int promoterLevel, long dataDate, long queryUserId, int pageNo, int pageSize) throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        String andSql = " and gu.promoterId" + promoterLevel + " = " + userId;
        andSql += " and gu.promoterLevel in (" + promoterLevel + "," + (promoterLevel + 1) + ")";
        andSql += " and ( gu.userRole not in (10000,20000,30000) or gu.userId = " + userId + " ) ";
        map.put("andSql", andSql);
        map.put("dataDate", dataDate);
        if (queryUserId > 0) {
            map.put("keyWord", queryUserId);
        }
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.commissionLogNextLevel", map);
    }

    public Integer countRankList(long groupId, long userId, int promoterLevel, long startDate, long endDate, long queryUserId, int optType) throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        String andSql = " and gu.promoterId" + promoterLevel + " = " + userId;
        if (optType == 2) {
            andSql += " and gu.promoterLevel in (" + promoterLevel + "," + (promoterLevel + 1) + ")";
        }
        map.put("andSql", andSql);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        if (queryUserId > 0) {
            map.put("keyWord", queryUserId);
        }
        return (Integer) this.getSqlMapClient().queryForObject("groupNew.countRankList", map);
    }

    public List<HashMap<String, Object>> rankList(long groupId, long userId, int promoterLevel, long startDate, long endDate, int rankField, int rankType, long queryUserId, int optType, int pageNo, int pageSize) throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        String andSql = " and gu.promoterId" + promoterLevel + " = " + userId;
        if (optType == 2) {
            andSql += " and gu.promoterLevel in (" + promoterLevel + "," + (promoterLevel + 1) + ")";
        }
        map.put("andSql", andSql);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        if (queryUserId > 0) {
            map.put("keyWord", queryUserId);
        }

        String orderBySql = " ORDER BY ";
        if (rankField == 1) {
            orderBySql += " selfWinCredit ";
        } else if (rankField == 2) {
            orderBySql += " selfCommissionCredit ";
        } else if (rankField == 3) {
            orderBySql += " selfCommissionCount ";
        } else if (rankField == 4) {
            orderBySql += " selfZjsCount ";
        } else if (rankField == 5) {
            orderBySql += " selfDyjCount ";
        } else if (rankField == 6) {
            orderBySql += " selfTotalPay ";
        } else {
            // 默认总局数
            orderBySql += " selfZjsCount ";
        }
        if (rankType == 1) {
            orderBySql += " ASC ";
        } else {
            orderBySql += " DESC ";
        }
        map.put("orderBySql", orderBySql);
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.rankList", map);
    }

    public HashMap<String, Object> rankSum(long groupId, long userId, int promoterLevel, long startDate, long endDate, int optType) throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        String andSql = " and gu.promoterId" + promoterLevel + " = " + userId;
        if (optType == 2) {
            andSql += " and gu.promoterLevel in (" + promoterLevel + "," + (promoterLevel + 1) + ")";
        }
        map.put("andSql", andSql);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        List<HashMap<String, Object>> list = (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.rankSum", map);
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

    public long insertGroupUserAlert(LogGroupUserAlert log) throws Exception {
        Long ret = (Long) this.getSqlMapClient().insert("groupNew.insertGroupUserAlert", log);
        return ret == null ? -1 : ret.longValue();
    }

    public Integer deleteUserAndLower(long groupId, int promoterLevel, long promoterId) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("andSql", " AND promoterId" + promoterLevel + " = " + promoterId);
        return this.getSqlMapClient().delete("groupNew.deleteUserAndLower", map);
    }

    public long insertGroupUserLog(GroupUserLog log) throws Exception {
        Long ret = (Long) this.getSqlMapClient().insert("groupNew.insertGroupUserLog", log);
        return ret == null ? -1 : ret.longValue();
    }

    public GroupUserLog loadGroupUserLog(long groupId, long userId) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        return (GroupUserLog) this.getSqlMapClient().queryForObject("groupNew.loadGroupUserLog", map);
    }

    public Integer deleteGroupUserLog(long keyId) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("keyId", keyId);
        return this.getSqlMapClient().delete("groupNew.deleteGroupUserLog", map);
    }

    public List<GroupUser> loadGroupUsers(long groupId, long promoterId, int promoterLevel) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("andSql", " AND promoterId" + promoterLevel + " = " + promoterId);
        return (List<GroupUser>) this.getSqlMapClient().queryForList("groupNew.loadGroupUsers", map);
    }

    public boolean isAllUserTableOver(long groupId, long promoterId, int promoterLevel) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("andSql", " AND gu.promoterId" + promoterLevel + " = " + promoterId);
        Integer count = (Integer) this.getSqlMapClient().queryForObject("groupNew.isGroupTableOver", map);
        return count == null || count == 0;
    }

    public Integer countSearchGroupUser(long groupId, long promoterId, int promoterLevel, String keyWord) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        String andSql = " AND gu.promoterId" + promoterLevel + " = " + promoterId;
        map.put("andSql", andSql);
        if (StringUtils.isNotEmpty(keyWord)) {
            map.put("keyWord", keyWord);
        }
        return (Integer) this.getSqlMapClient().queryForObject("groupNew.countSearchGroupUser", map);
    }

    public List<HashMap<String, Object>> searchGroupUser(long groupId, long promoterId, int promoterLevel, String keyWord, int pageNo, int pageSize) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        String andSql = " AND gu.promoterId" + promoterLevel + " = " + promoterId;
        map.put("andSql", andSql);
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord", keyWord);
        }
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.searchGroupUser", map);
    }

    public int updateGroupUser(HashMap<String, Object> map) throws Exception {
        return this.getSqlMapClient().update("groupNew.updateGroupUser", map);
    }

    public int updateGroupUserByKeyIdBatch(List<HashMap<String, Object>> list) throws Exception {
        SqlMapClient sqlMapClient = this.getSqlMapClient();
        sqlMapClient.startBatch();
        for (HashMap<String, Object> map : list) {
            sqlMapClient.update("groupNew.updateGroupUserByKeyId", map);
        }
        sqlMapClient.executeBatch();
        return 0;
    }

    public int getMaxPromoterLevel(long groupId, long userId, long promoterLevel) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        String andSql = " and promoterId" + promoterLevel + " = " + userId;
        map.put("andSql", andSql);
        return (Integer) this.getSqlMapClient().queryForObject("groupNew.getMaxPromoterLevel", map);
    }

    public Integer countGroupUserAlert(long groupId, int selectType, long queryUserId, int type) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        if (selectType == 1) {
            map.put("userId", queryUserId);
        } else if (selectType == 2) {
            map.put("optUserId", queryUserId);
        }
        if (type != 0) {
            map.put("type", type);
        }
        Integer res = (Integer) this.getSqlMapClient().queryForObject("groupNew.countGroupUserAlert", map);
        return res == null ? 0 : res;
    }

    public List<HashMap<String, Object>> loadGroupUserAlert(long groupId, int selectType, long queryUserId, int type, int pageNo, int pageSize) throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        if (selectType == 1) {
            map.put("userId", queryUserId);
        } else if (selectType == 2) {
            map.put("optUserId", queryUserId);
        }
        if (type != 0) {
            map.put("type", type);
        }
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.loadGroupUserAlert", map);
    }

    /**
     * 查看玩家最近一条被踢出群的日志
     * @param groupId
     * @param optUserId
     * @param type
     * @param userId
     * @return
     * @throws Exception
     */
    public List<HashMap<String, Object>> loadGroupUserAlertKickOutLog(long groupId,   long optUserId, int type ,long userId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupid", groupId);
        map.put("userid", userId);
        map.put("optUserid", optUserId);
        map.put("type", type);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.loadGroupUserAlertKickOutLog", map);
    }


    public List<HashMap<String, Object>> loadTableCount(String groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("dataCode", "group" + groupId);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.loadTableCount", map);
    }

    public List<GroupInfo> loadAllGroup() throws Exception {
        return (List<GroupInfo>) this.getSqlMapClient().queryForList("groupNew.loadAllGroup");
    }


    public Integer countCreditCommissionLog(long groupId, long userId, int promoterLevel, String startDate, String endDate) throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("selPromoterId", "gu.promoterId" + (promoterLevel + 1));
        map.put("andSql", "AND gu.promoterId" + promoterLevel + "=" + userId);
        map.put("groupBySql", "promoterId" + (promoterLevel + 1));
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(groupId));
        Integer res = (Integer) this.getSqlMapClient().queryForObject("groupNew.countCreditCommissionLog", map);
        return res != null ? res : 0;
    }

    public List<HashMap<String, Object>> creditCommissionLog(long groupId, long userId, int promoterLevel, String startDate, String endDate, int orderByField, int orderByType, int pageNo, int pageSize) throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        map.put("selPromoterId", "gu.promoterId" + (promoterLevel + 1));
        map.put("andSql", "AND gu.promoterId" + promoterLevel + "=" + userId);
        map.put("groupBySql", "gu.promoterId" + (promoterLevel + 1));

        String orderBySql;
        if (orderByField == 1) {
            orderBySql = " commissionCount ";
        } else {
            orderBySql = " commissionCredit ";
        }
        if (orderByType == 2) {
            orderBySql += " desc ";
        } else if (orderByType == 1) {
            orderBySql += " asc ";
        } else {
            orderBySql = " ( CASE WHEN gu.keyId is null THEN 0 WHEN gu.promoterLevel <= " + (promoterLevel + 1) + " THEN gu.promoterLevel ELSE 100 END ) asc ";
            orderBySql += " , d1.commissionCount desc ";
        }

        map.put("orderBySql", orderBySql);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(groupId));
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.creditCommissionLog", map);
    }

    public List<HashMap<String, Object>> creditZjs(long groupId, long userId, int promoterLevel, String startDate, String endDate) throws Exception {

        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("selPromoterId", "gu.promoterId" + (promoterLevel + 1));
        map.put("andSql", "AND gu.promoterId" + promoterLevel + "=" + userId);
        map.put("groupBySql", "gu.promoterId" + (promoterLevel + 1));
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(groupId)));
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.creditZjs", map);
    }

    public Long sumCommissionCreditLog(long groupId, long userId, String startDate, String endDate) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("type", 2);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(groupId));
        Long res = (Long) this.getSqlMapClient().queryForObject("groupNew.sumCommissionCreditLog", map);
        return res != null ? res : 0;
    }

    public Integer countCreditCommissionLogByUser(long groupId, long selfId, long targetId, int targetLevel, String startDate, String endDate) throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", selfId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("type", 2);
        String andSql = " AND gu.promoterId" + targetLevel + " = " + targetId;
        if (targetId == selfId) {
            andSql += " AND ( ";
            andSql += " (gu.promoterLevel = " + (targetLevel + 1) + " AND gu.userRole in ( " + GroupConstants.USER_ROLE_ChengYuan + " , " + GroupConstants.USER_ROLE_FuHuiZhang + " ) " + " ) ";
            andSql += " OR ";
            andSql += " (gu.userId = " + selfId + " ) ";
            andSql += ")";
        }
        map.put("andSql", andSql);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(groupId));
        Integer res = (Integer) this.getSqlMapClient().queryForObject("groupNew.countCreditCommissionLogByUser", map);
        return res != null ? res : 0;
    }

    public List<HashMap<String, Object>> creditCommissionLogByUser(long groupId, long selfId, long targetId, int targetLevel, String startDate, String endDate, int pageNo, int pageSize) throws Exception {

        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", selfId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("type", 2);
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        String andSql = " AND gu.promoterId" + targetLevel + "=" + targetId;
        if (targetId == selfId) {
            andSql += " AND ( ";
            andSql += " (gu.promoterLevel = " + (targetLevel + 1) + " AND gu.userRole in ( " + GroupConstants.USER_ROLE_ChengYuan + " , " + GroupConstants.USER_ROLE_FuHuiZhang + " ) " + " ) ";
            andSql += " OR ";
            andSql += " (gu.userId = " + selfId + " ) ";
            andSql += ")";
        }
        map.put("andSql", andSql);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(groupId));
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.creditCommissionLogByUser", map);
    }

    public List<HashMap<String, Object>> creditZjsByUser(long groupId, long selfId, long targetId, int targetLevel, String startDate, String endDate) throws Exception {

        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", selfId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("andSql", " AND gu.promoterId" + targetLevel + "=" + targetId);
        map.put("dataDate", TimeUtil.getDataDate(startDate));
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(groupId)));
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.creditZjsByUserNew", map);
    }

    public Map<String, Object> winLoseCreditByUserIdsToMap(long groupId, String userIds, String startDate, String endDate) throws Exception {
        Map<String, Object> res = new HashMap<>();
        List<HashMap<String, Object>> dataList = winLoseCreditByUserIds(groupId, userIds, startDate, endDate);
        for (HashMap<String, Object> data : dataList) {
            res.put(data.get("userId").toString(), data.get("winLoseCredit"));
        }
        return res;
    }

    public List<HashMap<String, Object>> winLoseCreditByUserIds(long groupId, String userIds, String startDate, String endDate) throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userIds", userIds);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.winLoseCreditByUserIds", map);
    }

    public HashMap<String, Object> searchCommissionLog(long groupId, long userId, long targetUserId, String startDate, String endDate) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("optUserId", targetUserId);
        map.put("type", 2);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(groupId));
        return (HashMap<String, Object>) this.getSqlMapClient().queryForObject("groupNew.searchCommissionLog", map);
    }

    public HashMap<String, Object> logGroupCommissionBuUser(long groupId, long userId, String startDate, String endDate) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        return (HashMap<String, Object>) this.getSqlMapClient().queryForObject("groupNew.logGroupCommissionBuUser", map);
    }

    public int updateCreditRate(long groupId, int creditRate) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("creditRate", creditRate);
        return this.getSqlMapClient().update("groupNew.updateCreditRate", map);
    }

    public int resetCommissionConfig(long groupId, int seq, long minCredit, long maxCredit) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("seq", seq);
        map.put("minCredit", minCredit);
        map.put("maxCredit", maxCredit);
        return this.getSqlMapClient().update("groupNew.resetCommissionConfig", map);
    }

    public int updateCreditAllotMode(long groupId, int mode) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("mode", mode);
        return this.getSqlMapClient().update("groupNew.updateCreditAllotMode", map);
    }

    public int insertGroupCreditLog(HashMap<String, Object> map) throws Exception {
        int res = 0;
        if(SysPartitionUtil.isWriteMaster()) {
            map.put("gpSeq", SysPartitionUtil.getGroupSeqForMaster(Long.valueOf(String.valueOf(map.get("groupId")))));
            res = this.getSqlMapClient().update("groupNew.insertGroupCreditLog", map);
        }

        if (SysPartitionUtil.isWritePartition()) {
            map.put("gpSeq", SysPartitionUtil.getGroupSeqForPartition(Long.valueOf(String.valueOf(map.get("groupId")))));
            this.getSqlMapClient().update("groupNew.insertGroupCreditLog", map);
        }
        return res;
    }

    public int updateGroupUserCreditPurse(HashMap<String,Object> map) throws Exception{
        return this.getSqlMapClient().update("groupNew.update_group_user_creditPurse",map);
    }

    public int insertGroupCreditLogMaster(HashMap<String, Object> map) throws Exception {
        return this.getSqlMapClient().update("groupNew.insertGroupCreditLogMaster", map);
    }

    /**
     * 转移信用分接口
     *
     * @param fromUserId
     * @param destUserId
     * @param credit     必须是正数
     * @return
     * @throws Exception
     */
    public int transferGroupUserCredit(long fromUserId, long destUserId, long groupId, int credit) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("fromUserId", fromUserId);
        map.put("destUserId", destUserId);
        map.put("groupId", groupId);
        map.put("credit", Math.abs(credit));
        return this.getSqlMapClient().update("groupNew.transferGroupUserCredit", map);
    }

    /**
     * 会长给其他人下分接口
     *
     * @param destUserId 成员id
     * @param credit     必须是正数
     * @return
     * @throws Exception
     */
    public int reduceGroupUserCreditForMaster(long destUserId, long groupId, int credit) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("destUserId", destUserId);
        map.put("groupId", groupId);
        map.put("credit", Math.abs(credit));
        return this.getSqlMapClient().update("groupNew.reduceGroupUserCreditForMaster", map);
    }

    public int addGroupExp(long groupKeyId, long exp, long creditExpToday) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("keyId", groupKeyId);
        map.put("exp", exp);
        if (creditExpToday > 0) {
            map.put("creditExpToday", creditExpToday);
        }
        return this.getSqlMapClient().update("groupNew.addGroupExp", map);
    }

    public int calcGroupLevel(long groupKeyId) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("keyId", groupKeyId);
        return this.getSqlMapClient().update("groupNew.calcGroupLevel", map);
    }

    public int addGroupUserExp(long groupUserKeyId, long exp, long creditExpToday) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("keyId", groupUserKeyId);
        map.put("exp", exp);
        if (creditExpToday > 0) {
            map.put("creditExpToday", creditExpToday);
        }
        return this.getSqlMapClient().update("groupNew.addGroupUserExp", map);
    }

    public int calcGroupUserLevel(long groupUserKeyId) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("keyId", groupUserKeyId);
        return this.getSqlMapClient().update("groupNew.calcGroupUserLevel", map);
    }

    public int insertLogGroupExp(HashMap<String, Object> map) throws Exception {
        return this.getSqlMapClient().update("groupNew.insertLogGroupExp", map);
    }

    public int insertLogGroupUserExp(HashMap<String, Object> map) throws Exception {
        return this.getSqlMapClient().update("groupNew.insertLogGroupUserExp", map);
    }

    public Integer countLogGroupExp(long groupId, long userId, String startDate, String endDate) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        if (userId > 0) {
            map.put("userId", userId);
        }
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        return (Integer) this.getSqlMapClient().queryForObject("groupNew.countLogGroupExp", map);
    }

    public List<HashMap<String, Object>> loadLogGroupExp(long groupId, long userId, String startDate, String endDate, int pageNo, int pageSize) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        if (userId > 0) {
            map.put("userId", userId);
        }
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.loadLogGroupExp", map);
    }

    public long sumLogGroupExpCredit(long groupId, String startDate, String endDate) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        return (Long) this.getSqlMapClient().queryForObject("groupNew.sumLogGroupExpCredit", map);
    }

    public Long loadLogGroupUserLevelKeyId(long groupId, long userId, int level) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("level", level);
        map.put("stat", 1);
        return (Long) this.getSqlMapClient().queryForObject("groupNew.loadLogGroupUserLevelKeyId", map);
    }

    public int updateLogGroupUserLevel(long keyId) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("keyId", keyId);
        map.put("stat", 2);
        return this.getSqlMapClient().update("groupNew.updateLogGroupUserLevel", map);
    }

    public List<HashMap<String, Object>> loadLogGroupUserLevel(long groupId, long userId) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("stat", 1);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.loadLogGroupUserLevel", map);
    }

    public int insertLogGroupUserLevel(HashMap<String, Object> map) throws Exception {
        return this.getSqlMapClient().update("groupNew.insertLogGroupUserLevel", map);
    }

    public int calcGroupExp(long groupKeyId) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("keyId", groupKeyId);
        return this.getSqlMapClient().update("groupNew.calcGroupExp", map);
    }

    public int calcGroupUserExp(long groupUserKeyId) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("keyId", groupUserKeyId);
        return this.getSqlMapClient().update("groupNew.calcGroupUserExp", map);
    }

    public List<Long> loadGroupUsersByDataStatistics(long groupId, long promoterId, int promoterLevel, int activeDate) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("andSql", " AND gu.promoterId" + promoterLevel + " = " + promoterId);
        map.put("dataDate", activeDate);
        map.put("dataCode", "group" + groupId);
        map.put("dataType", "xjsCount");
        return (List<Long>) this.getSqlMapClient().queryForList("groupNew.loadGroupUsersByDataStatistics", map);
    }

    public int forbidGroupUser(GroupUser target, int userLevel, int stateType) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("groupId", target.getGroupId());
        map.put("userLevel", userLevel);
        String andSql = "";
        if (stateType == 0) {
            andSql = " AND userId = " + target.getUserId();
        } else {
            andSql = " AND promoterId" + target.getPromoterLevel() + " = " + target.getUserId();
        }
        map.put("andSql", andSql);
        return this.getSqlMapClient().update("groupNew.forbidGroupUser", map);
    }

    public int loadFakeTableCount(long configId, int hiding) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("configId", configId);
        if (hiding >= 0){
            map.put("hiding", hiding);
        }
        return (Integer) this.getSqlMapClient().queryForObject("groupNew.loadFakeTableCount", map);
    }

    public List<Long> loadFakeTableKeyIds(long configId) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("configId", configId);
        return (List<Long>) this.getSqlMapClient().queryForList("groupNew.loadFakeTableKeyIds", map);
    }

    public GroupTable loadGroupTableById(long groupTableId, long groupId) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("keyId", groupTableId);
        List<GroupTable> groupTables = this.getSqlMapClient().queryForList("groupNew.loadGroupTableById", map);
        if (groupTables != null || groupTables.size() > 0) {
            return groupTables.get(0);
        }
        return null;
    }

    public List<HashMap<String, Object>> loadCommissionDetailForTable(long groupTableId, long groupId) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("groupTableId", groupTableId);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(groupId));
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.loadCommissionDetailForTable", map);
    }

    public List<HashMap<String, Object>> loadGroupUserRejectList(long groupId, long targetUserId, int pageNo, int pageSize) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        if (targetUserId > 0) {
            map.put("targetUserId", targetUserId);
            return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.loadGroupUserRejectList", map);
        } else {
            return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.loadGroupUserRejectAll", map);
        }
    }

    public long createGroupUserReject(GroupUserReject reject) throws Exception {
        Long ret = (Long) this.getSqlMapClient().insert("groupNew.createGroupUserReject", reject);
        return ret == null ? -1 : ret.longValue();
    }

    public Integer deleteGroupUserReject(long keyId, long groupId) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("keyId", keyId);
        map.put("groupId", groupId);
        return this.getSqlMapClient().delete("groupNew.deleteGroupUserReject", map);
    }

    public GroupUserReject loadGroupUserReject(long groupId, String userIdKeyStr) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userIdKeyStr", userIdKeyStr);
        return (GroupUserReject) this.getSqlMapClient().queryForObject("groupNew.loadGroupUserReject", map);
    }

    public GroupTableConfig loadGroupTableConfigByKeyId(long keyId) throws Exception {
        return (GroupTableConfig) this.getSqlMapClient().queryForObject("groupNew.loadGroupTableConfigByKeyId", String.valueOf(keyId));
    }

    public int updateGroupTableConfigByKeyId(HashMap<String, Object> map) throws Exception {
        return this.getSqlMapClient().update("groupNew.updateGroupTableConfigByKeyId", map);
    }

    public List<HashMap<String, Object>> loadGroupUserList(long groupId, int pageNo, int pageSize, String keyWord, int optType) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        map.put("optType", optType);
        map.put("keyWord", keyWord);
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.loadGroupUserList", map);
    }

    public Integer countGroupUserList(long groupId, String keyWord, int optType) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        map.put("optType", optType);
        map.put("keyWord", keyWord);
        return (Integer) this.getSqlMapClient().queryForObject("groupNew.countGroupUserList", map);
    }

    public List<HashMap<String, Object>> creditZjsNew(long groupId, long userId, int promoterLevel, String startDate, String endDate) throws Exception {

        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("dataDate", TimeUtil.getDataDate(startDate));
        map.put("selPromoterId", "gu.promoterId" + (promoterLevel + 1));

        // AND (gu.promoterId2 = #userId# AND gu.promoterLevel = #nextPromoterLevel# AND gu.userRole != 900000)
        map.put("andSql1", " AND gu.promoterId" + promoterLevel + "=" + userId + " AND gu.promoterLevel=" + (promoterLevel + 1) + " AND gu.userRole not in (2,90000)");

        //	AND gu.promoterId2 = #userId# AND (gu.promoterLevel = #promoterLevel# or (promoterLevel = #nextPromoterLevel# and userRole=90000))
        map.put("andSql2", " AND gu.promoterId" + promoterLevel + "=" + userId + " AND ( gu.promoterLevel = " + promoterLevel + " OR ( gu.promoterLevel = " + (promoterLevel + 1) + " AND gu.userRole in (2,90000) ) ) ");
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.creditZjsNew", map);
    }

    public int insertGroupCreditLogTransfer(HashMap<String, Object> map) throws Exception {
        return this.getSqlMapClient().update("groupNew.insertGroupCreditLogTransfer", map);
    }

    public Integer countInactiveUser(int groupId, String dateType) throws Exception {
        int dayOffset = -30;
        if ("2".equals(dateType)) {
            dayOffset = -60;
        } else if ("3".equals(dateType)) {
            dayOffset = -90;
        }
        String endDate = TimeUtil.getStartOfDay(dayOffset);
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("endDate", endDate);
        Integer res = (Integer) this.getSqlMapClient().queryForObject("groupNew.countInactiveUser", map);
        return res != null ? res : 0;
    }

    public List<HashMap<String, Object>> loadInactiveUser(int groupId, String dateType, String creditLimit, int pageNo, int pageSize) throws Exception {
        int dayOffset = -30;
        if ("2".equals(dateType)) {
            dayOffset = -60;
        } else if ("3".equals(dateType)) {
            dayOffset = -90;
        } else if ("4".equals(dateType)) {
            dayOffset = -7;
        } else if ("5".equals(dateType)) {
            dayOffset = -15;
        }

        long endDate = TimeUtil.getDateyyyyMMdd(dayOffset);
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("endDate", endDate);
        if (StringUtils.isBlank(creditLimit) || "0".equals(creditLimit)) {
            map.put("creditLimit", 0);
        } else {
            map.put("creditLimit", 1);
        }
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.loadInactiveUser", map);
    }

    public int fireInactiveUser(String userIds, long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userIds", userIds);
        return this.getSqlMapClient().delete("groupNew.fireInactiveUser", map);
    }

    public int updateGroupGameIds(String groupIds, String gameIds) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupIds", groupIds);
        map.put("gameIds", gameIds);
        return this.getSqlMapClient().update("groupNew.updateGroupGameIds", map);
    }

    public List<HashMap<String, Object>> groupGoldWinLog(long groupId, long userId, int promoterLevel, long dataDate, int pageNo, int pageSize, long queryUserId, int tag) throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        String andSql = " and ( ( gu.promoterId" + promoterLevel + " = " + userId;
        andSql += " and gu.promoterLevel = " + (promoterLevel + 1);
        andSql += " and gu.userRole in ( 10000, 20000, 30000 ) )";
        andSql += " or gu.userId = " + userId + " ) ";
        map.put("andSql", andSql);
        map.put("dataDate", dataDate);
        if (queryUserId > 0) {
            map.put("queryUserId", queryUserId);
        }
        if (tag != -1) {
            map.put("tag", tag);
        }

        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.group_gold_win_log", map);
    }

    public List<HashMap<String, Object>> groupGoldWinLogNextLevel(long groupId, long userId, int promoterLevel, long dataDate, int pageNo, int pageSize, long queryUserId, int tag) throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        String andSql = " and gu.promoterId" + promoterLevel + " = " + userId;
        andSql += " and gu.promoterLevel in (" + promoterLevel + "," + (promoterLevel + 1) + ")";
        andSql += " and ( gu.userRole not in (10000,20000,30000) or gu.userId = " + userId + " ) ";
        map.put("andSql", andSql);
        map.put("dataDate", dataDate);
        if (queryUserId > 0) {
            map.put("queryUserId", queryUserId);
        }
        if (tag != -1) {
            map.put("tag", tag);
        }
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.group_gold_win_log_next_level", map);
    }

    public Integer countValidGroupGoldWin(long groupId, String keyIds, long userId, int promoterLevel) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("keyIds", keyIds);
        map.put("groupId", groupId);
        String andSql = " and gu.promoterId" + promoterLevel + " = " + userId + " and gu.promoterLevel = " + (promoterLevel + 1);
        map.put("andSql", andSql);
        return (Integer) this.getSqlMapClient().queryForObject("groupNew.count_valid_group_gold_win", map);
    }

    public int updateGroupGoldWinTag(long groupId, String keyIds, int tag) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("keyIds", keyIds);
        map.put("groupId", groupId);
        map.put("tag", tag);
        return this.getSqlMapClient().update("groupNew.update_group_gold_win_tag", map);
    }

    public List<HashMap<String, Object>> groupGoldWinLogNextAll(long groupId, long userId, int promoterLevel, long dataDate, int rankField, int rankType, long queryUserId, int pageNo, int pageSize) throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        String andSql = " and gu.promoterId" + promoterLevel + " = " + userId;
        map.put("andSql", andSql);
        map.put("dataDate", dataDate);
        if (queryUserId > 0) {
            map.put("keyWord", queryUserId);
        }
        String orderBySql = " ORDER BY ";
        if (rankField == 1) {
            orderBySql += " log.selfJsCount ";
        } else if (rankField == 2) {
            orderBySql += " log.selfWin ";
        } else {
            // 默认总局数
            orderBySql += " log.selfJsCount ";
        }
        if (rankType == 1) {
            orderBySql += " ASC ";
        } else {
            orderBySql += " DESC ";
        }
        map.put("orderBySql", orderBySql);
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.group_gold_win_log_next_all", map);
    }

    public HashMap<String, Object> groupGoldWinLogNextAllSum(long groupId, long userId, int promoterLevel, long dataDate) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        String andSql = " and gu.promoterId" + promoterLevel + " = " + userId;
        map.put("andSql", andSql);
        map.put("dataDate", dataDate);
        List<HashMap<String, Object>> list = (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupNew.group_gold_win_log_next_all_sum", map);
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

    public Integer sumCreditByType(long groupId, long targetUserId, int type, String startDate, String endDate) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", targetUserId);
        map.put("type", type);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        Integer res = (Integer) this.getSqlMapClient().queryForObject("groupNew.sumCreditByType", map);
        return res == null ? 0 : res;
    }

    public HashMap<String, Object> jsFromGroupGoldWinLog(long groupId, long userId, long startDate, long endDate) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        return (HashMap<String, Object>) this.getSqlMapClient().queryForObject("groupNew.js_from_group_gold_win_log", map);
    }

    public long loadGroupCreditCommission(Map<String,Object> params) throws Exception {
        return (Long) this.getSqlMapClient().queryForObject("groupNew.load_group_commission_creditPurse", params);
    }

    public HashMap<String, Object> loadGroupUserwheel(long guId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("guId", String.valueOf(guId));
        return (HashMap<String, Object>) this.getSqlMapClient().queryForObject("groupNew.load_group_user_wheel", map);
    }

    public GroupCreditWheel loadGroupCreditWheel(long groupId) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return (GroupCreditWheel) this.getSqlMapClient().queryForObject("groupNew.load_group_credit_wheel", map);
    }

    public int updateCreditWheel(Map<String,Object> map) throws Exception {
        return this.getSqlMapClient().update("groupNew.update_credit_wheel", map);
    }

    public int updateUserWheelCount(int wheelCount, long groupUserId) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("guId", groupUserId);
        map.put("wheelCount", wheelCount);
        return this.getSqlMapClient().update("groupNew.update_user_wheel_count", map);
    }
    public long loadCreditWheelPool(long groupId) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        Long ret = (Long)this.getSqlMapClient().queryForObject("groupNew.load_credit_wheel_pool", map);
        return ret == null ? 0 : ret;
    }
}
