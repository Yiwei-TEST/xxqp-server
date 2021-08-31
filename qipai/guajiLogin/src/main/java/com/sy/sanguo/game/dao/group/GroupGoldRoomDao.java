package com.sy.sanguo.game.dao.group;

import com.sy.sanguo.common.parent.impl.CommonDaoImpl;
import com.sy.sanguo.game.bean.gold.GoldRoom;
import com.sy.sanguo.game.bean.group.GroupGoldCommissionConfig;
import com.sy599.sanguo.util.SysPartitionUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupGoldRoomDao extends CommonDaoImpl {

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
        return (Integer) this.getSqlMapClient().queryForObject("groupGoldRoom.count_team_list", map);
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
        return (List<Map<String, Object>>) this.getSqlMapClient().queryForList("groupGoldRoom.team_list", map);
    }


    public List<GroupGoldCommissionConfig> loadGoldCommissionConfig(long groupId, long userId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        return (List<GroupGoldCommissionConfig>) this.getSqlMapClient().queryForList("groupGoldRoom.load_gold_commission_config", map);
    }

    public long insertGoldCommissionConfig(GroupGoldCommissionConfig config) throws Exception {
        return (long) this.getSqlMapClient().insert("groupGoldRoom.insert_gold_commission_config", config);
    }

    public int updateGoldCommissionConfig(long groupId, long userId, int seq, long value, long leftValue, long maxLog) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("seq", seq);
        map.put("value", value);
        map.put("leftValue", leftValue);
        map.put("maxLog", maxLog);
        map.put("lastUpTime", new Date());
        return this.getSqlMapClient().update("groupGoldRoom.update_gold_commission_config", map);
    }

    public int getUserGoldTableIdCount(long groupId, long promoterId, int promoterLevel, long queryUserId, long queryTableId, String currentState, int playType, String startDate, String endDate) throws Exception {
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
        return (Integer) this.getSqlMapClient().queryForObject("groupGoldRoom.get_user_gold_room_count", map);
    }

    public List<GoldRoom> getUserPlayLogGroupTable(long groupId, long promoterId, int promoterLevel, long queryUserId, long queryTableId, String currentState, int playType, String startDate, String endDate, int pageNo, int pageSize) throws Exception {
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
        return (List<GoldRoom>) this.getSqlMapClient().queryForList("groupGoldRoom.get_user_gold_room_play_log", map);
    }

    public List<HashMap<String, Object>> goldCommissionLog(long groupId, long userId, int promoterLevel, long dataDate) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        String andSql = " and promoterId" + promoterLevel + " = " + userId;
        map.put("andSql", andSql);
        map.put("dataDate", dataDate);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupGoldRoom.gold_commission_log", map);
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
        return (Integer) this.getSqlMapClient().queryForObject("groupGoldRoom.count_rank_list", map);
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
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupGoldRoom.gold_rank_list", map);
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
        List<HashMap<String, Object>> list = (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupGoldRoom.rank_sum", map);
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

    public List<HashMap<String,Object>> loadGoldRoomTableRecord(long goldRoomId) throws Exception{
        return (List<HashMap<String,Object>>)this.getSqlMapClient().queryForList("groupGoldRoom.load_gold_room_table_record",goldRoomId);
    }

    public List<HashMap<String, Object>> loadGoldRoomUserByTableId(String roomIds,long groupId) throws Exception{
        Map<String, Object> map = new HashMap<>(8);
        map.put("roomIds", roomIds);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(groupId)));
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupGoldRoom.load_gold_room_user_by_tableId",map);
    }


    public Integer countGoldCommissionLogNextLevel(long groupId, long userId, int promoterLevel, long queryUserId) throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        String andSql = " and promoterId" + promoterLevel + " = " + userId;
        andSql += " and promoterLevel in (" + promoterLevel + "," + (promoterLevel + 1) + ")";
        andSql += " and ( userRole not in (10000,20000,30000) or userId = " + userId + " ) ";
        map.put("andSql", andSql);
        if (queryUserId > 0) {
            map.put("keyWord", queryUserId);
        }
        return (Integer) this.getSqlMapClient().queryForObject("groupGoldRoom.count_gold_commission_log_next_level", map);
    }

    public List<HashMap<String, Object>> goldCommissionLogNextLevel(long groupId, long userId, int promoterLevel, long dataDate, long queryUserId, int pageNo, int pageSize) throws Exception {

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
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("groupGoldRoom.gold_commission_log_next_level", map);
    }


    public int countSoloRoomTableRecord(long userId, long queryUserId, String startDate, String endDate) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("userId", userId);
        if (queryUserId > 0) {
            map.put("queryUserId", queryUserId);
        }
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        return (Integer) this.getSqlMapClient().queryForObject("groupGoldRoom.count_solo_room_table_record", map);
    }

    public List<Map<String, Object>> getSoloRoomTableRecord(long userId, long queryUserId, String startDate, String endDate, int pageNo, int pageSize) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("userId", userId);
        if (queryUserId > 0) {
            map.put("queryUserId", queryUserId);
        }
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<Map<String, Object>>) this.getSqlMapClient().queryForList("groupGoldRoom.get_solo_room_table_record", map);
    }

}
