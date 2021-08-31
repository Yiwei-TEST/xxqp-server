package com.sy.sanguo.game.pdkuai.db.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy.sanguo.common.datasource.MySqlDruidDataSource;
import com.sy.sanguo.common.init.InitData;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.StringUtil;
import com.sy.sanguo.game.pdkuai.db.bean.PdkTableLog;
import com.sy.sanguo.game.pdkuai.db.bean.UserPlaylog;
import com.sy.sanguo.game.pdkuai.util.LogUtil;

public class TableLogDao extends BaseDao {
    private static TableLogDao _inst = new TableLogDao();

    public static TableLogDao getInstance() {
        return _inst;
    }

    private SqlMapClient getUseSqlMapClient() {
        SqlMapClient sqlMapClient = getFuncSql();
        if (sqlMapClient == null) {
            sqlMapClient = getSql();
        }
        return sqlMapClient;
    }

    public long save(PdkTableLog tableInfo) {
        try {
            return (Long) getUseSqlMapClient().insert("tablelog.savetablelogInfo", tableInfo);
        } catch (SQLException e) {
            LogUtil.e("#tablelog.savetablelogInfo:" + tableInfo.getTableId(), e);
        }

        return 0;
    }

    public void delete(long userId) {
        try {
            getUseSqlMapClient().delete("tablelog.deleteLog", userId);
        } catch (SQLException e) {
            LogUtil.e("#tablelog.deleteLog:" + userId, e);
        }
    }

    public int clearUserPlayLog(String clearDate) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("clearDate", clearDate);
            return getUseSqlMapClient().delete("tablelog.clearUserPlayLog", map);
        } catch (Exception e) {
            LogUtil.e("#tablelog.delLogByHour:", e);
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    public List<UserPlaylog> selectUserLogByLogId(List<Long> logId) {
        try {
            return (List<UserPlaylog>) getUseSqlMapClient().queryForList("tablelog.selectUserLogByLogId", StringUtil.implode(logId));
        } catch (SQLException e) {
            LogUtil.e("#tablelog.selectUserLogById:" + logId, e);
        }
        return null;
    }

    public List<UserPlaylog> selectUserLogs(String ids) {
        try {
            return (List<UserPlaylog>) getUseSqlMapClient().queryForList("tablelog.selectUserLogs", ids);
        } catch (SQLException e) {
            LogUtil.e("#tablelog.selectUserLogs:" + ids, e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<UserPlaylog> selectUserLogById(long userId) {
        try {
            return (List<UserPlaylog>) getUseSqlMapClient().queryForList("tablelog.selectUserLogById", userId);
        } catch (SQLException e) {
            LogUtil.e("#tablelog.selectUserLogById:" + userId, e);
        }
        return null;
    }

    public long save(UserPlaylog info) {
        try {
            return (Long) getUseSqlMapClient().insert("tablelog.saveuserlogInfo", info);
        } catch (SQLException e) {
            LogUtil.e("#tablelog.saveuserlogInfo:" + info.getTableId(), e);
        }

        return 0;
    }

    public int deleteSql(String sql) {
        try {
            return getUseSqlMapClient().delete("tablelog.deleteSql", sql);
        } catch (SQLException e) {
            LogUtil.e("tablelog.deleteSql", e);
        }
        return -1;
    }

    public int transfer(String sql) {
        try {
            return getUseSqlMapClient().update("tablelog.transferSql", sql);
        } catch (SQLException e) {
            GameBackLogger.SYS_LOG.error("tablelog.transferSql", e);
        }
        return -1;
    }

    public long loadMaxKeyId(String sql) {
        try {
            return (Long) getUseSqlMapClient().queryForObject("tablelog.loadMaxKeyId", sql);
        } catch (SQLException e) {
            GameBackLogger.SYS_LOG.error("tablelog.loadMaxKeyId", e);
        }
        return -1;
    }

    public int checkTableCount(String tableName) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("tableName", tableName);
        map.put("dbName", InitData.db_name_1kz);
        Integer ret = (Integer) getUseSqlMapClient().queryForObject("tablelog.check_table_exists", map);
        return ret == null ? 0 : ret.intValue();
    }
}
