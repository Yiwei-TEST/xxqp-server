package com.sy.sanguo.game.dao.group;

import com.sy.sanguo.common.parent.impl.CommonDaoImpl;
import com.sy.sanguo.game.bean.group.GroupCommissionConfig;
import com.sy.sanguo.game.bean.group.GroupCreditConfig;
import com.sy.sanguo.game.bean.group.GroupReview;
import com.sy.sanguo.game.bean.group.GroupTableConfig;
import com.sy.sanguo.game.bean.group.GroupUser;
import com.sy.sanguo.game.bean.group.LogGroupUserAlert;
import com.sy599.sanguo.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupCreditDao extends CommonDaoImpl {

    //-----------群主管理员的小组列表
    public Integer countTeamListForMaster(int groupId, String keyWord) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        if (StringUtils.isNotEmpty(keyWord)) {
            map.put("keyWord", keyWord);
        }
        return (Integer) this.getSqlMapClient().queryForObject("groupCredit.countTeamListForMaster", map);
    }

    public List<Map<String, Object>> teamListForMaster(int groupId, String keyWord, int pageNo, int pageSize) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        if (StringUtils.isNotEmpty(keyWord)) {
            map.put("keyWord", keyWord);
        }
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<Map<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.teamListForMaster", map);
    }

    // ----------小组长的下一级拉手列表
    public Integer countTeamListForTeamLeader(int groupId, String userGroup, String keyWord) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        map.put("userGroup", userGroup);
        if (StringUtils.isNotEmpty(keyWord)) {
            map.put("keyWord", keyWord);
        }
        return (Integer) this.getSqlMapClient().queryForObject("groupCredit.countTeamListForTeamLeader", map);
    }

    public List<Map<String, Object>> teamListForTeamLeader(int groupId, String userGroup, long userId, String keyWord, int pageNo, int pageSize) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        map.put("userGroup", userGroup);
        map.put("userId", userId);
        if (StringUtils.isNotEmpty(keyWord)) {
            map.put("keyWord", keyWord);
        }
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<Map<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.teamListForTeamLeader", map);
    }

    //-----------拉手的下一级拉手列表
    public Integer countTeamListForPromoter(int groupId, String userGroup, String promoterId, int promoterLevel, String keyWord) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        map.put("userGroup", userGroup);
        map.put("promoterId" + promoterLevel, promoterId);
        map.put("promoterLevel", promoterLevel + 1);
        if (StringUtils.isNotEmpty(keyWord)) {
            map.put("keyWord", keyWord);
        }
        return (Integer) this.getSqlMapClient().queryForObject("groupCredit.countTeamListForPromoter", map);
    }
    public List<Map<String, Object>> teamListForPromoter(int groupId, String userGroup, String userId, int promoterLevel, String keyWord, int pageNo, int pageSize) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        map.put("userGroup", userGroup);
        map.put("userId", userId);
        if (StringUtils.isNotEmpty(keyWord)) {
            map.put("keyWord", keyWord);
        }
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<Map<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.teamListForPromoter" + promoterLevel, map);
    }

    public Map<String, Map<String, Object>> countTeamUserForPromoter4(int groupId, String userGroup, String promoterId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userGroup", userGroup);
        map.put("promoterId", promoterId);
        List<Map<String, Object>> list = (List<Map<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.countTeamUserForPromoter4", map);
        Map<String, Map<String, Object>> res = new HashMap<>();
        if (list != null && list.size() > 0) {
            for (Map<String, Object> data : list) {
                res.put(data.get("uid").toString(), data);
            }
        }
        return res;
    }

    public Integer countUserListForMaster(int groupId, String keyWord) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        if (StringUtils.isNotEmpty(keyWord)) {
            map.put("keyWord", keyWord);
        }
        return (Integer) this.getSqlMapClient().queryForObject("groupCredit.countUserListForMaster", map);
    }

    public List<Map<String, Object>> userListForMaster(int groupId, String keyWord, int pageNo, int pageSize) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        if (StringUtils.isNotEmpty(keyWord)) {
            map.put("keyWord", keyWord);
        }
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<Map<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.userListForMaster", map);
    }

    public Integer countUserListForTeamLeader(int groupId, String userGroup, int promoterLevel, String keyWord) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        map.put("userGroup", userGroup);
        map.put("promoterLevel", String.valueOf(promoterLevel));
        if (StringUtils.isNotEmpty(keyWord)) {
            map.put("keyWord", keyWord);
        }
        return (Integer) this.getSqlMapClient().queryForObject("groupCredit.countUserListForTeamLeader", map);
    }

    public List<Map<String, Object>> userListForTeamLeader(int groupId, String userGroup, int promoterLevel, String keyWord, int pageNo, int pageSize) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        map.put("userGroup", userGroup);
        map.put("promoterLevel", String.valueOf(promoterLevel));
        if (StringUtils.isNotEmpty(keyWord)) {
            map.put("keyWord", keyWord);
        }
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<Map<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.userListForTeamLeader", map);
    }

    public Integer countUserListForPromoter(int groupId, String userGroup, long promoterId, int promoterLevel, String keyWord) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        map.put("userGroup", userGroup);
        map.put("promoterLevel", promoterLevel);
        map.put("nextPromoterLevel", promoterLevel + 1);
        map.put("promoterId" + promoterLevel, promoterId);
        if (StringUtils.isNotEmpty(keyWord)) {
            map.put("keyWord", keyWord);
        }
        return (Integer) this.getSqlMapClient().queryForObject("groupCredit.countUserListForPromoter", map);
    }

    public List<Map<String, Object>> userListForPromoter(int groupId, String userGroup, long promoterId, int promoterLevel, String keyWord, int pageNo, int pageSize) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userGroup", userGroup);
        map.put("promoterLevel", promoterLevel);
        map.put("nextPromoterLevel", promoterLevel + 1);
        map.put("promoterId" + promoterLevel, promoterId);
        if (StringUtils.isNotEmpty(keyWord)) {
            map.put("keyWord", keyWord);
        }
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<Map<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.userListForPromoter", map);
    }

    public List<GroupUser> allUserForPromoter(long groupId, String userGroup, long promoterId, int promoterLevel) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userGroup", userGroup);
        map.put("promoterId" + promoterLevel, promoterId);
        map.put("promoterLevel", promoterLevel);
        return (List<GroupUser>) this.getSqlMapClient().queryForList("groupCredit.allUserForPromoter", map);
    }

    public List<GroupUser> allUserForTeamLeader(long groupId, String userGroup) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userGroup", userGroup);
        return (List<GroupUser>) this.getSqlMapClient().queryForList("groupCredit.allUserForTeamLeader", map);
    }

    public int countUserHaveCreditForPromoter(long groupId, String userGroup, long promoterId, int promoterLevel) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userGroup", userGroup);
        map.put("promoterId" + promoterLevel, promoterId);
        map.put("promoterLevel", promoterLevel);
        Integer count = (Integer) this.getSqlMapClient().queryForObject("groupCredit.countUserHaveCreditForPromoter", map);
        return count != null ? count : 0;
    }

    public int deleteUserForPromoter(long groupId, String userGroup, long promoterId, int promoterLevel) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userGroup", userGroup);
        map.put("promoterId" + promoterLevel, promoterId);
        map.put("promoterLevel", promoterLevel);
        return this.getSqlMapClient().delete("groupCredit.deleteUserForPromoter", map);
    }

    public List<GroupCreditConfig> loadCreditConfig(long groupId, long preUserId, long userId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("preUserId", preUserId);
        map.put("userId", userId);
        return (List<GroupCreditConfig>) this.getSqlMapClient().queryForList("groupCredit.loadCreditConfig", map);
    }

    public int countCreditConfig(long groupId, long preUserId, long userId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("preUserId", preUserId);
        map.put("userId", userId);
        Integer res = (Integer) this.getSqlMapClient().queryForObject("groupCredit.countCreditConfig", map);
        return res == null ? 0 : res;
    }

    public GroupCreditConfig loadCreditConfigByConfigId(long groupId, long preUserId, long userId, long configId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("preUserId", preUserId);
        map.put("userId", userId);
        map.put("configId", configId);
        List<GroupCreditConfig> list = this.getSqlMapClient().queryForList("groupCredit.loadCreditConfigByConfigId", map);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public long insertGroupCreditConfig(GroupCreditConfig config) throws Exception {
        return (long) this.getSqlMapClient().insert("groupCredit.insertGroupCreditConfig", config);
    }

    public int updateGroupCreditConfig(long keyId, int credit, int maxCreditLog) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("keyId", keyId);
        map.put("credit", credit);
        map.put("maxCreditLog", maxCreditLog);
        return this.getSqlMapClient().update("groupCredit.updateGroupCreditConfig", map);
    }

    public int deleteGroupCreditConfig(long groupId, long userId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        return this.getSqlMapClient().delete("groupCredit.deleteGroupCreditConfig", map);
    }

    public int deleteGroupCreditConfigForPromoter(long groupId, long userId, int promoterLevel) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("promoterId" + promoterLevel, userId);
        return this.getSqlMapClient().delete("groupCredit.deleteGroupCreditConfigForPromoter", map);
    }

    public int updateCreditAllotMode(long groupId, int mode) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("mode", mode);
        return this.getSqlMapClient().update("groupCredit.updateCreditAllotMode", map);
    }

    public int deleteGroupCreditConfigForTeamLeader(long groupId, String userGroup) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userGroup", userGroup);
        return this.getSqlMapClient().delete("groupCredit.deleteGroupCreditConfigForTeamLeader", map);
    }

    public int deleteUserForTeamLeader(long groupId, String userGroup) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userGroup", userGroup);
        return this.getSqlMapClient().delete("groupCredit.deleteUserForTeamLeader", map);
    }

    public long sumCreditForMaster(long groupId) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return (Long) this.getSqlMapClient().queryForObject("groupCredit.sumCreditForMaster", map);
    }

    public long sumCreditForTeamLeader(long groupId, String userGroup) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userGroup", userGroup);
        return (Long) this.getSqlMapClient().queryForObject("groupCredit.sumCreditForTeamLeader", map);
    }

    public long sumCreditForPromoter(long groupId, String userGroup, long promoterId, int promoterLevel) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userGroup", userGroup);
        map.put("promoterId" + promoterLevel, promoterId);
        return (Long) this.getSqlMapClient().queryForObject("groupCredit.sumCreditForPromoter", map);
    }


    public GroupReview loadTeamInvite(long groupId, long userId, long inviterId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("inviterId", inviterId);
        return (GroupReview) this.getSqlMapClient().queryForObject("groupCredit.loadTeamInvite", map);
    }

    public int rejectTeamInvite(long groupId, long userId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        return this.getSqlMapClient().update("groupCredit.rejectTeamInvite", map);
    }

    public List<Map<String, Object>> loadGroupTableConfigWithRoomName(long parentGroup) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("parentGroup", parentGroup);
        return (List<Map<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.loadGroupTableConfigWithRoomName", map);
    }

    public List<GroupTableConfig> loadAllGroupTableConfig(long parentGroup) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("parentGroup", parentGroup);
        return (List<GroupTableConfig>) this.getSqlMapClient().queryForList("groupCredit.loadAllGroupTableConfig", map);
    }

    public List<GroupTableConfig> loadAllGroupTableConfigByIds(long parentGroup, String keyIds) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("parentGroup", parentGroup);
        map.put("keyIds", keyIds);
        return (List<GroupTableConfig>) this.getSqlMapClient().queryForList("groupCredit.loadAllGroupTableConfigByIds", map);
    }

    public Integer countCreditCommissionLogForMaster(long groupId, long userId, String keyWord, String dateType) throws Exception {

        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("type", 2);
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord", keyWord);
        }
        Integer res = (Integer) this.getSqlMapClient().queryForObject("groupCredit.countCreditCommissionLogForMaster", map);
        return res != null ? res : 0;
    }

    public List<HashMap<String, Object>> creditCommissionLogForMaster(long groupId, long userId, String dateType, String keyWord, int pageNo, int pageSize) throws Exception {

        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("type", 2);
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord", keyWord);
        }
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.creditCommissionLogForMaster", map);
    }

    public Long sumCommissionCreditLog(long groupId, long userId, String dateType) throws Exception {
        return sumCommissionCreditLog(groupId, userId, null, dateType);
    }

    public Long sumCommissionCreditLog(long groupId, long userId, String userGroup, String dateType) throws Exception {

        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        if (StringUtils.isNotBlank(userGroup)) {
            map.put("userGroup", userGroup);
        }
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("type", 2);
        Long res = (Long) this.getSqlMapClient().queryForObject("groupCredit.sumCommissionCreditLog", map);
        return res != null ? res : 0;
    }


    public Integer countCreditCommissionLogForTeamLeader(long groupId, long userId, int promoterLevel, String keyWord, String dateType) throws Exception {

        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("groupByField", "promoterId" + (promoterLevel + 1));
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("type", 2);
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord", keyWord);
        }
        Integer res = (Integer) this.getSqlMapClient().queryForObject("groupCredit.countCreditCommissionLogForTeamLeader", map);
        return res != null ? res : 0;
    }

    public List<HashMap<String, Object>> creditCommissionLogForTeamLeader(long groupId, long userId, int promoterLevel, String dateType, String keyWord, int pageNo, int pageSize) throws Exception {

        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("type", 2);
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord", keyWord);
        }
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.creditCommissionLogForTeamLeader", map);
    }

    public Integer countCreditCommissionLogForPromoter(long groupId, long userId, int promoterLevel, String keyWord, String dateType) throws Exception {

        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("groupByField", "promoterId" + (promoterLevel + 1));
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("type", 2);
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord", keyWord);
        }
        Integer res = (Integer) this.getSqlMapClient().queryForObject("groupCredit.countCreditCommissionLogForPromoter", map);
        return res != null ? res : 0;
    }

    public List<HashMap<String, Object>> creditCommissionLogForPromoter(long groupId, long userId, int promoterLevel, String dateType, String keyWord, int pageNo, int pageSize) throws Exception {

        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("groupByField", "promoterId" + (promoterLevel + 1));
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("type", 2);
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord", keyWord);
        }
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.creditCommissionLogForPromoter", map);
    }

    public List<HashMap<String, Object>> creditCommissionLogForPromoter4(long groupId, long userId, String dateType) throws Exception {

        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("type", 2);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.creditCommissionLogForPromoter4", map);
    }

    public List<HashMap<String, Object>> loadPromoterMsgForTeamLeader(long groupId, String userGroup) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userGroup", userGroup);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.loadPromoterMsgForTeamLeader", map);
    }

    public List<HashMap<String, Object>> loadPromoterMsgForPromoter(long groupId, long userId, int promoterLevel) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("promoterId" + promoterLevel, userId);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.loadPromoterMsgForPromoter", map);
    }


    public HashMap<String, Object> searchCommissionLog(long groupId, long userId, long targetUserId, String dateType) throws Exception {
        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("optUserId", targetUserId);
        map.put("type", 2);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        return (HashMap<String, Object>) this.getSqlMapClient().queryForObject("groupCredit.searchCommissionLog", map);
    }

    public HashMap<String, Object> logGroupCommissionBuUser(long groupId, long userId, String dateType) throws Exception {
        int dayOffset = TimeUtil.getDayOffset(dateType);
        long dataDate = TimeUtil.getDateyyyyMMdd(dayOffset);
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("dataDate", dataDate);
        return (HashMap<String, Object>) this.getSqlMapClient().queryForObject("groupCredit.logGroupCommissionBuUser", map);
    }

    public List<HashMap<String, Object>> creditZjsForMaster(long groupId, String dateType) throws Exception {

        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);

        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.creditZjsForMaster", map);
    }

    public List<HashMap<String, Object>> creditZjsForTeamLeader(long groupId, String userGroup, int promoterLevel, String dateType) throws Exception {

        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);

        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userGroup", userGroup);
        map.put("groupByField", "promoterId" + (promoterLevel + 1));
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.creditZjsForTeamLeader", map);
    }

    public List<HashMap<String, Object>> creditZjsForPromoter(long groupId, String userGroup, long promoterId, int promoterLevel, String dateType) throws Exception {

        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);

        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userGroup", userGroup);
        map.put("promoterId" + promoterLevel, promoterId);
        map.put("groupByField", "promoterId" + (promoterLevel + 1));
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.creditZjsForPromoter", map);
    }

    public List<HashMap<String, Object>> creditZjsForPromoter4(long groupId, String userGroup, long promoterId, int promoterLevel, String dateType) throws Exception {

        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);

        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userGroup", userGroup);
        map.put("promoterId" + promoterLevel, promoterId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.creditZjsForPromoter4", map);
    }

    public List<HashMap<String, Object>> searchGroupUserForMaster(long groupId, String keyWord, int pageNo, int pageSize) throws Exception {
        return searchGroupUser(groupId, keyWord, null, -1, -1, pageNo, pageSize);
    }

    public List<HashMap<String, Object>> searchGroupUserForTeamLeader(long groupId, String keyWord, String userGroup, int pageNo, int pageSize) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord", keyWord);
        }
        if (StringUtils.isNotBlank(userGroup)) {
            map.put("userGroup", userGroup);
        }
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.searchGroupUserForTeamLeader", map);
    }

    public List<HashMap<String, Object>> searchGroupUserForPromoter(long groupId, String keyWord, GroupUser groupUser, int pageNo, int pageSize) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        map.put("userGroup", groupUser.getUserGroup());
        map.put("promoterId" + groupUser.getPromoterLevel(), groupUser.getUserId());
        if (groupUser.getPromoterLevel() == 1) {
            map.put("level1", 1);
        } else if (groupUser.getPromoterLevel() == 2) {
            map.put("prePromoterId1", groupUser.getPromoterId1());
        } else if (groupUser.getPromoterLevel() == 3) {
            map.put("prePromoterId2", groupUser.getPromoterId2());
        } else if (groupUser.getPromoterLevel() == 4) {
            map.put("prePromoterId3", groupUser.getPromoterId3());
        }
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord", keyWord);
        }
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.searchGroupUserForPromoter", map);
    }

    public List<HashMap<String, Object>> searchGroupUser(long groupId, String keyWord, String userGroup, long promoterId, int promoterLevel, int pageNo, int pageSize) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord", keyWord);
        }
        if (StringUtils.isNotBlank(userGroup)) {
            map.put("userGroup", userGroup);
        }
        if (promoterId > 0) {
            map.put("promoterId" + promoterLevel, promoterId);
        }
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.searchGroupUser", map);
    }

    public Integer countSearchGroupUserForMaster(long groupId, String keyWord) throws Exception {
        return countSearchGroupUser(groupId, keyWord, null, -1, -1);
    }

    public Integer countSearchGroupUserForTeamLeader(long groupId, String keyWord, String userGroup) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        if (StringUtils.isNotEmpty(keyWord)) {
            map.put("keyWord", keyWord);
        }
        if (StringUtils.isNotBlank(userGroup)) {
            map.put("userGroup", userGroup);
        }
        return (Integer) this.getSqlMapClient().queryForObject("groupCredit.countSearchGroupUserForTeamLeader", map);
    }

    public Integer countSearchGroupUserForPromoter(long groupId, String keyWord, GroupUser groupUser) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        map.put("userGroup", groupUser.getUserGroup());
        map.put("promoterId" + groupUser.getPromoterLevel(), groupUser.getUserId());
        if (groupUser.getPromoterLevel() == 1) {
            map.put("level1", 1);
        } else if (groupUser.getPromoterLevel() == 2) {
            map.put("prePromoterId1", groupUser.getPromoterId1());
        } else if (groupUser.getPromoterLevel() == 3) {
            map.put("prePromoterId2", groupUser.getPromoterId2());
        } else if (groupUser.getPromoterLevel() == 4) {
            map.put("prePromoterId3", groupUser.getPromoterId3());
        }
        if (StringUtils.isNotEmpty(keyWord)) {
            map.put("keyWord", keyWord);
        }
        return (Integer) this.getSqlMapClient().queryForObject("groupCredit.countSearchGroupUserForPromoter", map);
    }

    public Integer countSearchGroupUser(long groupId, String keyWord, String userGroup, long promoterId, int promoterLevel) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));

        if (StringUtils.isNotEmpty(keyWord)) {
            map.put("keyWord", keyWord);
        }
        if (StringUtils.isNotBlank(userGroup)) {
            map.put("userGroup", userGroup);
        }
        if (promoterId > 0) {
            map.put("promoterId" + promoterLevel, promoterId);
        }
        return (Integer) this.getSqlMapClient().queryForObject("groupCredit.countSearchGroupUser", map);
    }

    public List<HashMap<String, Object>> searchGroupUserForUser(long groupId, long userId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.searchGroupUserForUser", map);
    }

    public GroupUser loadGroupTeamMaster(long groupId, String userGroup) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userGroup", userGroup);
        return (GroupUser) this.getSqlMapClient().queryForObject("groupCredit.loadGroupTeamMaster", map);
    }

    public Integer countCreditCommissionLogByUserForMaster(long groupId, String userGroup, long userId, String dateType) throws Exception {
        return countCreditCommissionLogByUser(groupId, userGroup, userId, -1, -1, dateType);
    }

    public Integer countCreditCommissionLogByUser(long groupId, String userGroup, long userId, long promoterId, int promoterLevel, String dateType) throws Exception {

        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("type", 2);
        map.put("userGroup", userGroup);
        if (promoterLevel >= 0 && promoterLevel < 4) {
            if (promoterId >= 0) {
                map.put("promoterId" + (promoterLevel + 1), promoterId);
            }
        } else if (promoterLevel == 4) { //四级拉手查看自己的下属成员
            map.put("promoterId" + promoterLevel, userId);
        }
        Integer res = (Integer) this.getSqlMapClient().queryForObject("groupCredit.countCreditCommissionLogByUser", map);
        return res != null ? res : 0;
    }

    public List<HashMap<String, Object>> creditCommissionLogByUserForMaster(long groupId, String userGroup, long userId, String dateType, int pageNo, int pageSize) throws Exception {
        return creditCommissionLogByUser(groupId, userGroup, userId, -1, -1, dateType, pageNo, pageSize);
    }

    public List<HashMap<String, Object>> creditCommissionLogByUser(long groupId, String userGroup, long userId, long promoterId, int promoterLevel, String dateType, int pageNo, int pageSize) throws Exception {

        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("type", 2);
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        map.put("userGroup", userGroup);
        if (promoterLevel >= 0 && promoterLevel < 4) {
            if (promoterId >= 0) {
                map.put("promoterId" + (promoterLevel + 1), promoterId);
            }
        } else if (promoterLevel == 4) { //四级拉手查看自己的下属成员
            map.put("promoterId" + promoterLevel, userId);
        }
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.creditCommissionLogByUser", map);
    }

    public List<HashMap<String, Object>> creditZjsByUser(long groupId, String userGroup, int promoterLevel, String dateType) throws Exception {

        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);

        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userGroup", userGroup);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.creditZjsByUser", map);
    }

    public Integer countUserForMaster(long groupId, String keyWord) throws Exception {
        return countUser(groupId, null, 0, 0, keyWord);
    }

    public Integer countUserForTeamLeader(long groupId, String userGroup, String keyWord) throws Exception {
        return countUser(groupId, userGroup, 0, 0, keyWord);
    }

    public Integer countUserForPromoter(long groupId, String userGroup, long promoterId, int promoterLevel, String keyWord) throws Exception {
        return countUser(groupId, userGroup, promoterId, promoterLevel, keyWord);
    }

    public Integer countUser(long groupId, String userGroup, long promoterId, int promoterLevel, String keyWord) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord", keyWord);
        }
        if (StringUtils.isNotBlank(userGroup)) {
            map.put("userGroup", userGroup);
        }
        if (promoterId > 0) {
            map.put("promoterId" + promoterLevel, promoterId);
        }
        return (Integer) this.getSqlMapClient().queryForObject("groupCredit.countUser", map);
    }

    public List<Map<String, Object>> userListForMaster(long groupId, String keyWord,int creditOrder, int pageNo,  int pageSize) throws Exception {
        return userList(groupId, null, 0, 0, keyWord,creditOrder, pageNo, pageSize);
    }

    public List<Map<String, Object>> userListForTeamLeader(long groupId, String userGroup, String keyWord,int creditOrder,  int pageNo, int pageSize) throws Exception {
        return userList(groupId, userGroup, 0, 0, keyWord,creditOrder, pageNo, pageSize);
    }

    public List<Map<String, Object>> userListForPromoter(long groupId, String userGroup, long promoterId, int promoterLevel, String keyWord,int creditOrder,  int pageNo, int pageSize) throws Exception {
        return userList(groupId, userGroup, promoterId, promoterLevel, keyWord,creditOrder, pageNo, pageSize);
    }

    public List<Map<String, Object>> userList(long groupId, String userGroup, long promoterId, int promoterLevel, String keyWord,int creditOrder, int pageNo, int pageSize) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        if (StringUtils.isNotEmpty(keyWord)) {
            map.put("keyWord", keyWord);
        }
        if (StringUtils.isNotBlank(userGroup)) {
            map.put("userGroup", userGroup);
        }
        if (promoterId > 0) {
            map.put("promoterId" + promoterLevel, promoterId);
        }
        map.put("creditOrder",creditOrder);
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<Map<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.userList", map);
    }

    public int countCreditLogList(long groupId, long userId, int type, String dateType, int upOrDown) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        if(type > 0) {
            map.put("type", type);
        }
        if (upOrDown != 0) {
            map.put("upOrDown", upOrDown);
        }
        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        return (Integer) this.getSqlMapClient().queryForObject("groupCredit.countCreditLogList", map);
    }

    public List<Map<String, Object>> creditLogList(long groupId, long userId, int type, String dateType, int upOrDown, int pageNo, int pageSize) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        if(type > 0) {
            map.put("type", type);
        }
        if (upOrDown != 0) {
            map.put("upOrDown", upOrDown);
        }
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        return (List<Map<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.creditLogList", map);
    }

    public Long sumCreditLogList(long groupId, long userId, int type, String dateType, int upOrDown) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        if (type > 0) {
            map.put("type", type);
        }
        if (upOrDown != 0) {
            map.put("upOrDown", upOrDown);
        }
        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        return (Long) this.getSqlMapClient().queryForObject("groupCredit.sumCreditLogList", map);
    }

    public List<GroupCommissionConfig> loadCommissionConfig(long groupId, long userId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        return (List<GroupCommissionConfig>) this.getSqlMapClient().queryForList("groupCredit.loadCommissionConfig", map);
    }

    public long insertCommissionConfig(GroupCommissionConfig config) throws Exception {
        return (long) this.getSqlMapClient().insert("groupCredit.insertCommissionConfig", config);
    }

    public int updateCommissionConfig(long groupId, long userId, int seq, long credit,long leftCredit ,long maxCreditLog) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("seq", seq);
        map.put("credit", credit);
        map.put("leftCredit", leftCredit);
        map.put("maxCreditLog", maxCreditLog);
        return this.getSqlMapClient().update("groupCredit.updateCommissionConfig", map);
    }

    public int updateCreditRate(long groupId, int creditRate) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("creditRate", creditRate);
        return this.getSqlMapClient().update("groupCredit.updateCreditRate", map);
    }

    public int resetCommissionConfig(long groupId, int seq, long minCredit, long maxCredit) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("seq", seq);
        map.put("minCredit", minCredit);
        map.put("maxCredit", maxCredit);
        return this.getSqlMapClient().update("groupCredit.resetCommissionConfig", map);
    }

    public int countUserHaveCreditForTeamLeader(long groupId, String userGroup) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userGroup", userGroup);
        Integer count = (Integer) this.getSqlMapClient().queryForObject("groupCredit.countUserHaveCreditForTeamLeader", map);
        return count != null ? count : 0;
    }

    public int countUserHaveCreditForMaster(long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        Integer count = (Integer) this.getSqlMapClient().queryForObject("groupCredit.countUserHaveCreditForMaster", map);
        return count != null ? count : 0;
    }

    public List<GroupUser> loadMasterAndTeamLeader(long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return (List<GroupUser>)this.getSqlMapClient().queryForList("groupCredit.loadMasterAndTeamLeader",map);
    }

    public int deleteCommissionConfig(long groupId, long userId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        return this.getSqlMapClient().delete("groupCredit.deleteCommissionConfig", map);
    }

    public boolean isGroupTableOver(long groupId, long tableId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("tableId", tableId);
        Integer count = (Integer) this.getSqlMapClient().queryForObject("groupCredit.isGroupTableOver", map);
        return count == null || count == 0;
    }

    public List<HashMap<String, Object>> creditLogByUserIds(long groupId,String userIds, String dateType) throws Exception {

        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userIds", userIds);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.creditLogByUserIds", map);
    }

    public Map<String, Object> creditLogByUserIdsToMap(long groupId, String userIds, String dateType) throws Exception {
        Map<String, Object> res = new HashMap<>();
        List<HashMap<String, Object>> dataList = creditLogByUserIds(groupId, userIds, dateType);
        for (HashMap<String, Object> data : dataList) {
            res.put(data.get("userId").toString(), data.get("winLoseCredit"));
        }
        return res;
    }

    public List<HashMap<String, Object>> winLoseCreditByUserIds(long groupId,String userIds, String dateType) throws Exception {

        int dayOffset = TimeUtil.getDayOffset(dateType);
        long dataDate = TimeUtil.getDateyyyyMMdd(dayOffset);
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userIds", userIds);
        map.put("dataDate", dataDate);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.winLoseCreditByUserIds", map);
    }

    public Map<String, Object> winLoseCreditByUserIdsToMap(long groupId, String userIds, String dateType) throws Exception {
        Map<String, Object> res = new HashMap<>();
        if(userIds != null && userIds.length() > 0) {
            List<HashMap<String, Object>> dataList = winLoseCreditByUserIds(groupId, userIds, dateType);
            for (HashMap<String, Object> data : dataList) {
                res.put(data.get("userId").toString(), data.get("winLoseCredit"));
            }
        }
        return res;
    }

    public long insertGroupUserAlert(LogGroupUserAlert log) throws Exception {
        Long ret = (Long) this.getSqlMapClient().insert("groupCredit.insertGroupUserAlert", log);
        return ret == null ? -1 : ret.longValue();
    }

    public Integer countGroupUserAlert(long groupId, int selectType, long queryUserId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        if (selectType == 1) {
            map.put("userId", queryUserId);
        } else if (selectType == 2) {
            map.put("optUserId", queryUserId);
        }
        Integer res = (Integer) this.getSqlMapClient().queryForObject("groupCredit.countGroupUserAlert", map);
        return res == null ? 0 : res;
    }

    public List<HashMap<String, Object>> loadGroupUserAlert(long groupId, int selectType, long queryUserId, int pageNo, int pageSize) throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        if (selectType == 1) {
            map.put("userId", queryUserId);
        } else if (selectType == 2) {
            map.put("optUserId", queryUserId);
        }
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupCredit.loadGroupUserAlert", map);
    }

}
