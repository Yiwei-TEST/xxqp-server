package com.sy599.game.db.dao;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy599.game.common.datasource.DataSourceManager;

/**
 * DAO基类
 * @author taohuiliang
 * @date 2013-3-7
 * @version v1.0
 */
public class BaseDao {
    
	/**
	 * 获得sqlMapClient
	 * @return SqlMapClient
	 */
	protected SqlMapClient getSqlClient(){
//		return SharedConstants.sqlClient;
		return DataSourceManager.getServerSqlMapClient();
	}
	/**
	 * 获得登录工程sqlMapClient
	 * @return SqlMapClient
	 */
	protected SqlMapClient getSqlLoginClient(){
//		return SharedConstants.sqlLoginClient;
		return DataSourceManager.getLoginSqlMapClient();
	}
	/**
	 * 获得日志SqlMapClient
	 * @return SqlMapClient
	 */
	protected SqlMapClient getSqlLogClient(){
//    	return SharedConstants.sqlLogClient;
		return DataSourceManager.getLogSqlMapClient();
    }

    protected SqlMapClient getSqlLoginSlaveClient(){
//		return SharedConstants.sqlLoginClient;
        return DataSourceManager.getLoginSlaveSqlMapClient();
    }
}
