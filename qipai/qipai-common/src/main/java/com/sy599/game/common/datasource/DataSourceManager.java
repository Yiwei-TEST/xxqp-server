package com.sy599.game.common.datasource;

import com.ibatis.sqlmap.client.SqlMapClient;

public final class DataSourceManager {

    private SqlMapClient loginSqlMapClient;
    private SqlMapClient serverSqlMapClient;
    private SqlMapClient logSqlMapClient;
    private String loginDbName;
    private String serverDbName;
    private String logDbName;

    private SqlMapClient loginSlaveSqlMapClient;
    private String loginSlaveDbName;

    private static final DataSourceManager dataSourceManager = new DataSourceManager();

    private DataSourceManager() {

    }

    public static String getLogDbName() {
        return dataSourceManager.logDbName;
    }

    public static String getServerDbName() {
        return dataSourceManager.serverDbName;
    }

    public static String getLoginDbName() {
        return dataSourceManager.loginDbName;
    }

    public static SqlMapClient getLoginSqlMapClient() {
        return dataSourceManager.loginSqlMapClient;
    }

    public static void setLoginSqlMapClient(SqlMapClient loginSqlMapClient,String dbName) {
        synchronized (DataSourceManager.class) {
            if (dataSourceManager.loginSqlMapClient == null) {
                dataSourceManager.loginSqlMapClient = loginSqlMapClient;
                dataSourceManager.loginDbName = dbName;
            }
        }
    }

    public static SqlMapClient getServerSqlMapClient() {
        return dataSourceManager.serverSqlMapClient;
    }

    public static void setServerSqlMapClient(SqlMapClient serverSqlMapClient,String dbName) {
        synchronized (DataSourceManager.class) {
            if (dataSourceManager.serverSqlMapClient == null) {
                dataSourceManager.serverSqlMapClient = serverSqlMapClient;
                dataSourceManager.serverDbName = dbName;
            }
        }
    }

    public static SqlMapClient getLogSqlMapClient() {
        return dataSourceManager.logSqlMapClient;
    }

    public static void setLogSqlMapClient(SqlMapClient logSqlMapClient,String dbName) {
        synchronized (DataSourceManager.class) {
            if (dataSourceManager.logSqlMapClient == null) {
                dataSourceManager.logSqlMapClient = logSqlMapClient;
                dataSourceManager.logDbName = dbName;
            }
        }
    }


    public static void setLoginSlaveSqlMapClient(SqlMapClient loginSqlMapClient, String dbName) {
        synchronized (DataSourceManager.class) {
            if (dataSourceManager.loginSlaveSqlMapClient == null) {
                dataSourceManager.loginSlaveSqlMapClient = loginSqlMapClient;
                dataSourceManager.loginSlaveDbName = dbName;
            }
        }
    }

    public static SqlMapClient getLoginSlaveSqlMapClient() {
        if (dataSourceManager.loginSlaveSqlMapClient != null) {
            return dataSourceManager.loginSlaveSqlMapClient;
        } else {
            return dataSourceManager.loginSqlMapClient;
        }
    }
}
