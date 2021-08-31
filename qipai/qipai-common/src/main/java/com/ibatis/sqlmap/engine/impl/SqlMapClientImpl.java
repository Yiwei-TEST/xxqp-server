//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ibatis.sqlmap.engine.impl;

import com.ibatis.common.logging.Log;
import com.ibatis.common.logging.LogFactory;
import com.ibatis.common.util.PaginatedList;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.client.SqlMapSession;
import com.ibatis.sqlmap.client.event.RowHandler;
import com.ibatis.sqlmap.engine.execution.BatchException;
import com.ibatis.sqlmap.engine.execution.SqlExecutor;
import com.ibatis.sqlmap.engine.mapping.result.ResultObjectFactory;
import com.ibatis.sqlmap.engine.mapping.statement.MappedStatement;
import com.sy599.game.util.ResourcesConfigsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

public class SqlMapClientImpl implements SqlMapClient, ExtendedSqlMapClient {
    private static final Log log = LogFactory.getLog(SqlMapClientImpl.class);
    private static final Logger logger = LoggerFactory.getLogger("db");

    public SqlMapExecutorDelegate delegate;
    protected ThreadLocal localSqlMapSession = new ThreadLocal();

    public SqlMapClientImpl(SqlMapExecutorDelegate delegate) {
        this.delegate = delegate;
    }

    public Object insert(String id, Object param) throws SQLException {
        long t1 = System.currentTimeMillis();
        try {
            return this.getLocalSqlMapSession().insert(id, param);
        } catch (SQLException e) {
            throw e;
        } finally {
            dbXnLog("insert", id, param, t1);
        }
    }

    public Object insert(String id) throws SQLException {
        long t1 = System.currentTimeMillis();
        try {
            return this.getLocalSqlMapSession().insert(id);
        } catch (SQLException e) {
            throw e;
        } finally {
            dbXnLog("insert", id, null, t1);
        }
    }

    public int update(String id, Object param) throws SQLException {
        long t1 = System.currentTimeMillis();
        try {
            return this.getLocalSqlMapSession().update(id, param);
        } catch (SQLException e) {
            throw e;
        } finally {
            dbXnLog("update", id, param, t1);
        }
    }

    public int update(String id) throws SQLException {
        long t1 = System.currentTimeMillis();
        try {
            return this.getLocalSqlMapSession().update(id);
        } catch (SQLException e) {
            throw e;
        } finally {
            dbXnLog("update", id, null, t1);
        }
    }

    public int delete(String id, Object param) throws SQLException {
        long t1 = System.currentTimeMillis();
        try {
            return this.getLocalSqlMapSession().delete(id, param);
        } catch (SQLException e) {
            throw e;
        } finally {
            dbXnLog("delete", id, param, t1);
        }
    }

    public int delete(String id) throws SQLException {
        long t1 = System.currentTimeMillis();
        try {
            return this.getLocalSqlMapSession().delete(id);
        } catch (SQLException e) {
            throw e;
        } finally {
            dbXnLog("delete", id, null, t1);
        }
    }

    public Object queryForObject(String id, Object paramObject) throws SQLException {
        long t1 = System.currentTimeMillis();
        try {
            return this.getLocalSqlMapSession().queryForObject(id, paramObject);
        } catch (SQLException e) {
            throw e;
        } finally {
            dbXnLog("queryForObject", id, paramObject, t1);
        }
    }

    public Object queryForObject(String id) throws SQLException {
        long t1 = System.currentTimeMillis();
        try {
            return this.getLocalSqlMapSession().queryForObject(id);
        } catch (SQLException e) {
            throw e;
        } finally {
            dbXnLog("queryForObject", id, null, t1);
        }
    }

    public Object queryForObject(String id, Object paramObject, Object resultObject) throws SQLException {
        long t1 = System.currentTimeMillis();
        try {
            return this.getLocalSqlMapSession().queryForObject(id, paramObject, resultObject);
        } catch (SQLException e) {
            throw e;
        } finally {
            dbXnLog("queryForObject", id, paramObject, t1);
        }
    }

    public List queryForList(String id, Object paramObject) throws SQLException {
        long t1 = System.currentTimeMillis();
        try {
            return this.getLocalSqlMapSession().queryForList(id, paramObject);
        } catch (SQLException e) {
            throw e;
        } finally {
            dbXnLog("queryForList", id, paramObject, t1);
        }
    }

    public List queryForList(String id) throws SQLException {
        long t1 = System.currentTimeMillis();
        try {
            return this.getLocalSqlMapSession().queryForList(id);
        } catch (SQLException e) {
            throw e;
        } finally {
            dbXnLog("queryForList", id, null, t1);
        }
    }

    public List queryForList(String id, Object paramObject, int skip, int max) throws SQLException {
        long t1 = System.currentTimeMillis();
        try {
            return this.getLocalSqlMapSession().queryForList(id, paramObject, skip, max);
        } catch (SQLException e) {
            throw e;
        } finally {
            dbXnLog("queryForList", id, paramObject, t1);
        }
    }

    public List queryForList(String id, int skip, int max) throws SQLException {
        long t1 = System.currentTimeMillis();
        try {
            return this.getLocalSqlMapSession().queryForList(id, skip, max);
        } catch (SQLException e) {
            throw e;
        } finally {
            dbXnLog("queryForList", id, null, t1);
        }
    }

    /**
     * @deprecated
     */
    public PaginatedList queryForPaginatedList(String id, Object paramObject, int pageSize) throws SQLException {
        long t1 = System.currentTimeMillis();
        try {
            return this.getLocalSqlMapSession().queryForPaginatedList(id, paramObject, pageSize);
        } catch (SQLException e) {
            throw e;
        } finally {
            dbXnLog("queryForPaginatedList", id, paramObject, t1);
        }
    }

    /**
     * @deprecated
     */
    public PaginatedList queryForPaginatedList(String id, int pageSize) throws SQLException {
        long t1 = System.currentTimeMillis();
        try {
            return this.getLocalSqlMapSession().queryForPaginatedList(id, pageSize);
        } catch (SQLException e) {
            throw e;
        } finally {
            dbXnLog("queryForPaginatedList", id, null, t1);
        }
    }

    public Map queryForMap(String id, Object paramObject, String keyProp) throws SQLException {
        long t1 = System.currentTimeMillis();
        try {
            return this.getLocalSqlMapSession().queryForMap(id, paramObject, keyProp);
        } catch (SQLException e) {
            throw e;
        } finally {
            dbXnLog("queryForMap", id, paramObject, t1);
        }
    }

    public Map queryForMap(String id, Object paramObject, String keyProp, String valueProp) throws SQLException {
        long t1 = System.currentTimeMillis();
        try {
            return this.getLocalSqlMapSession().queryForMap(id, paramObject, keyProp, valueProp);
        } catch (SQLException e) {
            throw e;
        } finally {
            dbXnLog("queryForMap", id, paramObject, t1);
        }
    }

    public void queryWithRowHandler(String id, Object paramObject, RowHandler rowHandler) throws SQLException {
        this.getLocalSqlMapSession().queryWithRowHandler(id, paramObject, rowHandler);
    }

    public void queryWithRowHandler(String id, RowHandler rowHandler) throws SQLException {
        this.getLocalSqlMapSession().queryWithRowHandler(id, rowHandler);
    }

    public void startTransaction() throws SQLException {
        this.getLocalSqlMapSession().startTransaction();
    }

    public void startTransaction(int transactionIsolation) throws SQLException {
        this.getLocalSqlMapSession().startTransaction(transactionIsolation);
    }

    public void commitTransaction() throws SQLException {
        this.getLocalSqlMapSession().commitTransaction();
    }

    public void endTransaction() throws SQLException {
        try {
            this.getLocalSqlMapSession().endTransaction();
        } finally {
            this.getLocalSqlMapSession().close();
        }

    }

    public void startBatch() throws SQLException {
        this.getLocalSqlMapSession().startBatch();
    }

    public int executeBatch() throws SQLException {
        return this.getLocalSqlMapSession().executeBatch();
    }

    public List executeBatchDetailed() throws SQLException, BatchException {
        return this.getLocalSqlMapSession().executeBatchDetailed();
    }

    public void setUserConnection(Connection connection) throws SQLException {
        try {
            this.getLocalSqlMapSession().setUserConnection(connection);
        } finally {
            if (connection == null) {
                this.getLocalSqlMapSession().close();
            }

        }

    }

    /**
     * @deprecated
     */
    public Connection getUserConnection() throws SQLException {
        return this.getCurrentConnection();
    }

    public Connection getCurrentConnection() throws SQLException {
        return this.getLocalSqlMapSession().getCurrentConnection();
    }

    public DataSource getDataSource() {
        return this.delegate.getDataSource();
    }

    public MappedStatement getMappedStatement(String id) {
        return this.delegate.getMappedStatement(id);
    }

    public boolean isLazyLoadingEnabled() {
        return this.delegate.isLazyLoadingEnabled();
    }

    public boolean isEnhancementEnabled() {
        return this.delegate.isEnhancementEnabled();
    }

    public SqlExecutor getSqlExecutor() {
        return this.delegate.getSqlExecutor();
    }

    public SqlMapExecutorDelegate getDelegate() {
        return this.delegate;
    }

    public SqlMapSession openSession() {
        SqlMapSessionImpl sqlMapSession = new SqlMapSessionImpl(this);
        sqlMapSession.open();
        return sqlMapSession;
    }

    public SqlMapSession openSession(Connection conn) {
        try {
            SqlMapSessionImpl sqlMapSession = new SqlMapSessionImpl(this);
            sqlMapSession.open();
            sqlMapSession.setUserConnection(conn);
            return sqlMapSession;
        } catch (SQLException var3) {
            throw new SqlMapException("Error setting user provided connection.  Cause: " + var3, var3);
        }
    }

    /**
     * @deprecated
     */
    public SqlMapSession getSession() {
        log.warn("Use of a deprecated API detected.  SqlMapClient.getSession() is deprecated.  Use SqlMapClient.openSession() instead.");
        return this.openSession();
    }

    public void flushDataCache() {
        this.delegate.flushDataCache();
    }

    public void flushDataCache(String cacheId) {
        this.delegate.flushDataCache(cacheId);
    }

    protected SqlMapSessionImpl getLocalSqlMapSession() {
        SqlMapSessionImpl sqlMapSession = (SqlMapSessionImpl) this.localSqlMapSession.get();
        if (sqlMapSession == null || sqlMapSession.isClosed()) {
            sqlMapSession = new SqlMapSessionImpl(this);
            this.localSqlMapSession.set(sqlMapSession);
        }

        return sqlMapSession;
    }

    public void dbXnLog(String method, String id, Object params, long startTime) {
        Integer time = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "db_log_time");
        long timeLimit = time == null ? 50 : time.longValue();
        long timeUse = System.currentTimeMillis() - startTime;
        if (timeUse > timeLimit) {
            StringBuilder sb = new StringBuilder("xnlog|db");
            sb.append("|").append(timeUse);
            sb.append("|").append(startTime);
            sb.append("|").append(id);
            sb.append("|").append(params);
            sb.append("|").append(Thread.currentThread().toString());
            sb.append("|").append(method);
            logger.info(sb.toString());
        }
    }

    public ResultObjectFactory getResultObjectFactory() {
        return this.delegate.getResultObjectFactory();
    }
}
