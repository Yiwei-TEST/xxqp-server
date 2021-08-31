package com.sy.sanguo.game.dao;

import com.sy.sanguo.game.bean.group.GroupUser;
import com.sy.sanguo.game.pdkuai.db.dao.BaseDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsDaoImpl extends BaseDao {

    private static StatisticsDaoImpl inst = new StatisticsDaoImpl();

    public static StatisticsDaoImpl getInstance() {
        return inst;
    }


    public List<Long> allGroupIdGold() throws Exception {
        return (List<Long>) this.getSql().queryForList("statistics.all_group_id_gold");
    }

    public List<GroupUser> allGroupUserAdmin(long groupId) throws Exception {
        return (List<GroupUser>) this.getSql().queryForList("statistics.all_group_user_admin", groupId);
    }

    public Integer groupMaxPromoterLevel(long groupId) throws Exception {
        return (Integer) this.getSql().queryForObject("statistics.group_max_promoterLevel", groupId);
    }

    public List<HashMap<String, Object>> groupGoldWinStatisticsMaster(long dataDate, long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("dataDate", dataDate);
        return (List<HashMap<String, Object>>) this.getSql().queryForList("statistics.group_gold_win_statistics_master", map);
    }

    public List<HashMap<String, Object>> groupGoldWinStatisticsNextLevel(long dataDate, long groupId, long userId, int promoterLevel) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("dataDate", dataDate);
        map.put("andSql", "and gu.promoterId" + promoterLevel + " = " + userId);
        map.put("groupBySql", "gu.promoterId" + (promoterLevel + 1));
        return (List<HashMap<String, Object>>) this.getSql().queryForList("statistics.group_gold_win_statistics_next_level", map);
    }

    public List<HashMap<String, Object>> groupGoldWinStatisticsSelf(long dataDate, long groupId, long userId, int promoterLevel) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("dataDate", dataDate);
        StringBuilder andSql = new StringBuilder();
        andSql.append(" and gu.promoterId").append(promoterLevel).append(" = ").append(userId);
        andSql.append(" and gu.promoterLevel in ( ").append(promoterLevel).append(" , ").append(promoterLevel + 1).append(" ) ");
        andSql.append(" and gu.userRole not in ( 10000 , 20000 , 30000 ) ");
        map.put("andSql", andSql.toString());
        return (List<HashMap<String, Object>>) this.getSql().queryForList("statistics.group_gold_win_statistics_self", map);
    }

    public void saveLogGroupGoldWin(List<HashMap<String, Object>> logList) throws Exception {
        this.getSql().startBatch();
        for (HashMap<String, Object> log : logList) {
            this.getSql().update("statistics.save_log_group_gold_win", log);
        }
        this.getSql().executeBatch();
    }

}
