package com.sy.sanguo.game.dao.group;

import com.sy.sanguo.common.parent.impl.CommonDaoImpl;
import com.sy599.sanguo.util.SysPartitionUtil;

import java.util.HashMap;
import java.util.Map;

public class GroupRecycleDao extends CommonDaoImpl {

    public int countGroupTable(long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return (Integer) this.getSqlMapClient().queryForObject("groupRecycle.countGroupTable", map);
    }

    public int deleteGroup(Long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return this.getSqlMapClient().delete("groupRecycle.deleteGroup", map);
    }

    public int deleteGroupUser(Long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return this.getSqlMapClient().delete("groupRecycle.deleteGroupUser", map);
    }

    public int deleteGroupTableConfig(Long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return this.getSqlMapClient().delete("groupRecycle.deleteGroupTableConfig", map);
    }

    public int deleteGroupCommissionConfig(Long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return this.getSqlMapClient().delete("groupRecycle.deleteGroupCommissionConfig", map);
    }

    public int deleteGroupCreditConfig(Long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return this.getSqlMapClient().delete("groupRecycle.deleteGroupCreditConfig", map);
    }

    public int deleteGroupTable(Long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForPartition(Long.valueOf(groupId)));
        return this.getSqlMapClient().delete("groupRecycle.deleteGroupTable", map);
    }

    public int deleteTableUserPartition(Long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForPartition(Long.valueOf(groupId)));
        return this.getSqlMapClient().delete("groupRecycle.deleteTableUser", map);
    }

    public int deleteTableUserMaster(Long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForMaster(Long.valueOf(groupId)));
        return this.getSqlMapClient().delete("groupRecycle.deleteTableUser", map);
    }

    public int deleteTableRecord(Long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return this.getSqlMapClient().delete("groupRecycle.deleteTableRecord", map);
    }

    public int deleteGroupReview(Long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return this.getSqlMapClient().delete("groupRecycle.deleteGroupReview", map);
    }

    public int deleteGroupCreditLogPartition(Long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForPartition(Long.valueOf(groupId)));
        return this.getSqlMapClient().delete("groupRecycle.deleteGroupCreditLog", map);
    }

    public int deleteGroupCreditLogMaster(Long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForMaster(Long.valueOf(groupId)));
        return this.getSqlMapClient().delete("groupRecycle.deleteGroupCreditLogMaster", map);
    }

    public int deleteLogGroupTable(Long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return this.getSqlMapClient().delete("groupRecycle.deleteLogGroupTable", map);
    }

    public int deleteLogGroupCommission(Long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return this.getSqlMapClient().delete("groupRecycle.deleteLogGroupCommission", map);
    }

    public int deleteGroupUserLog(Long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return this.getSqlMapClient().delete("groupRecycle.deleteGroupUserLog", map);
    }

    public int deleteBjdDataStatistics(Long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return this.getSqlMapClient().delete("groupRecycle.deleteBjdDataStatistics", map);
    }

    public int deleteDataStatistics(Long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("dataCode", "group" + groupId);
        return this.getSqlMapClient().delete("groupRecycle.deleteDataStatistics", map);
    }

    public int deleteBjdGroupNewerBind(Long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return this.getSqlMapClient().delete("groupRecycle.deleteBjdGroupNewerBind", map);
    }

    public int deleteLogGroupExp(Long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return this.getSqlMapClient().delete("groupRecycle.deleteLogGroupExp", map);
    }

    public int deleteLogGroupUserExp(Long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return this.getSqlMapClient().delete("groupRecycle.deleteLogGroupUserExp", map);
    }

    public int deleteLogGroupUserAlert(Long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return this.getSqlMapClient().delete("groupRecycle.deleteLogGroupUserAlert", map);
    }

    public int deleteLogGroupUserLevel(Long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return this.getSqlMapClient().delete("groupRecycle.deleteLogGroupUserLevel", map);
    }

    public int deleteGroupUserReject(Long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return this.getSqlMapClient().delete("groupRecycle.deleteGroupUserReject", map);
    }


}
