package com.sy.sanguo.game.dao;

import com.sy.sanguo.common.datasource.MySqlDruidDataSource;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.game.pdkuai.constants.SharedConstants;
import com.sy.sanguo.game.pdkuai.db.dao.BaseDao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlDao extends BaseDao {
    private static SqlDao _inst = new SqlDao();

    public static SqlDao getInstance() {
        return _inst;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> showcolumns(String tableName) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("tablename", tableName);
            return (List<Map<String, Object>>) getSql().queryForList("sql.showcolumns", map);
        } catch (Exception e) {
            GameBackLogger.SYS_LOG.error("sql.showcolumns", e);
        }
        return null;
    }

    public int update(String sql) {
        try {
            return getSql().update("sql.updateSql", sql);
        } catch (SQLException e) {
            GameBackLogger.SYS_LOG.error("sql.updateSql", e);
        }
        return -1;
    }

    public List<HashMap<String, Object>> query(String sql) {
        try {
            return (List<HashMap<String, Object>>) (getSql().queryForList("sql.querySql", sql));
        } catch (SQLException e) {
            GameBackLogger.SYS_LOG.error("sql.querySql", e);
        }
        return null;
    }

    public int createTable(String sql) {
        try {
            return getSql().update("sql.createTable", sql);
        } catch (SQLException e) {
            GameBackLogger.SYS_LOG.error("sql.createTable", e);
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> showIndex(String tableName) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("tablename", tableName);
        try {
            return getSql().queryForList("sql.showIndex", map);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public int checkTableCount(String tableName) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("tableName", tableName);
        map.put("dbName", MySqlDruidDataSource.loadDbName());
        Integer ret = (Integer) getSql().queryForObject("sql.check_table_exists", map);
        return ret == null ? 0 : ret.intValue();
    }

    public int checkExistsGroupTable(int gpSeq) throws Exception {
        return checkTableCount("t_group_table" + gpSeq);
    }

    public int checkExistsDataStatisticsTable() throws Exception {
        return checkTableCount("t_data_statistics");
    }

    public int checkExistsGoldRoomTable() throws Exception {
        return checkTableCount("t_gold_room");
    }

    public boolean checkTableExists(String tableName) throws Exception {
        return checkTableCount(tableName) > 0;
    }

    public int delete(String sql) {
        try {
            return getSql().delete("sql.deleteSql", sql);
        } catch (SQLException e) {
            GameBackLogger.SYS_LOG.error("sql.deleteSql", e);
        }
        return -1;
    }

    public int transfer(String sql) {
        try {
            return getSql().update("sql.transferSql", sql);
        } catch (SQLException e) {
            GameBackLogger.SYS_LOG.error("sql.transferSql", e);
        }
        return -1;
    }

    public long loadMaxKeyId(String sql) {
        try {
            return (Long) getSql().queryForObject("sql.loadMaxKeyId", sql);
        } catch (SQLException e) {
            GameBackLogger.SYS_LOG.error("sql.loadMaxKeyId", e);
        }
        return -1;
    }

}
