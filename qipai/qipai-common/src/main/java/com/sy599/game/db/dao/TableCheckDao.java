package com.sy599.game.db.dao;

import com.sy599.game.common.datasource.DataSourceManager;
import com.sy599.game.db.enums.DbEnum;
import com.sy599.game.util.LogUtil;

import java.util.HashMap;

public class TableCheckDao extends BaseDao {
    private final static TableCheckDao _inst = new TableCheckDao();

    public static TableCheckDao getInstance() {
        return _inst;
    }

    public Integer checkTableCount(DbEnum dbEnum, String tableName) throws Exception {
        Integer number;
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("tableName", tableName);
        try {
            switch (dbEnum) {
                case LOGIN:
                    map.put("dbName", DataSourceManager.getLoginDbName());
                    number = (Integer) getSqlLoginClient().queryForObject("check_table.check_table_exists", map);
                    break;
                case SERVER:
                    map.put("dbName", DataSourceManager.getServerDbName());
                    number = (Integer) getSqlClient().queryForObject("check_table.check_table_exists", map);
                    break;
                case LOG:
                    map.put("dbName", DataSourceManager.getLogDbName());
                    number = (Integer) getSqlLogClient().queryForObject("check_table.check_table_exists", map);
                    break;
                default:
                    number = null;
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("checkTableExists Exception:" + e.getMessage(), e);
            throw e;
        }
        return number;
    }

    public boolean checkTableExists(DbEnum dbEnum, String tableName) throws Exception {
        Integer number = checkTableCount(dbEnum, tableName);
        return number != null ? number.intValue() > 0 : false;
    }

    public boolean checkTableExists0(DbEnum dbEnum, String tableName){
        try {
            Integer number = checkTableCount(dbEnum, tableName);
            return number != null ? number.intValue() > 0 : false;
        }catch (Exception e){
            return false;
        }
    }
}
