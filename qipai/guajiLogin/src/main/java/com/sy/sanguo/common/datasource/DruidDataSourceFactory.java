package com.sy.sanguo.common.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.ibatis.sqlmap.engine.datasource.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.*;

public class DruidDataSourceFactory implements DataSourceFactory {

    private final static Logger LOGGER = LoggerFactory.getLogger(DruidDataSourceFactory.class);

    private static final List<DruidDataSource> DRUID_DATA_SOURCE_LIST = new ArrayList<>();

    private static final boolean NEW_DRIVER;

    private DataSource dataSource;

    static {
        boolean isNew = true;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            isNew = false;
        }
        NEW_DRIVER = isNew;
    }

    public DruidDataSourceFactory() {
    }

    public void initialize(Map map) {
        dataSource = new DruidDataSource();

        Properties properties = new Properties();

        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> kv = (Map.Entry<String, String>) iterator.next();
            properties.put(kv.getKey(), kv.getValue());
        }

        String url = String.valueOf(properties.get("druid.url"));

        if (NEW_DRIVER && "com.mysql.jdbc.Driver".equals(properties.get("druid.driverClassName"))) {
            properties.put("druid.driverClassName", "com.mysql.cj.jdbc.Driver");
            if (!url.contains("useSSL=")) {
                if (url.contains("?")) {
                    url = url + "&useSSL=false";
                } else {
                    url = url + "?useSSL=false";
                }
            }

            if (!url.contains("serverTimezone=")) {
                if (url.contains("?")) {
                    url = url + "&serverTimezone=GMT%2B8";
                } else {
                    url = url + "?serverTimezone=GMT%2B8";
                }
            }

            if (url.contains("zeroDateTimeBehavior=exception")) {
                url = url.replace("zeroDateTimeBehavior=exception", "zeroDateTimeBehavior=EXCEPTION");
            } else if (url.contains("zeroDateTimeBehavior=round")) {
                url = url.replace("zeroDateTimeBehavior=round", "zeroDateTimeBehavior=ROUND");
            } else if (url.contains("zeroDateTimeBehavior=convertToNull")) {
                url = url.replace("zeroDateTimeBehavior=convertToNull", "zeroDateTimeBehavior=CONVERT_TO_NULL");
            }
        }

        if (!url.contains("rewriteBatchedStatements=")){
            url = url + (url.contains("?") ? "&rewriteBatchedStatements=true" : "?rewriteBatchedStatements=true");
        }

        if (!url.contains("useAffectedRows=")){
            url = url + "&useAffectedRows=true";
        }

        properties.put("druid.url", url);

        ((DruidDataSource) dataSource).configFromPropety(properties);

        try {
            ((DruidDataSource) dataSource).init();

            synchronized (DruidDataSourceFactory.class) {
                DRUID_DATA_SOURCE_LIST.add((DruidDataSource) dataSource);
            }

            LOGGER.info("init DruidDataSource success:newDriver={},url={}", NEW_DRIVER, url);
        } catch (Throwable t) {
            LOGGER.error("init DruidDataSource fail:url=" + url + ",Exception:" + t.getMessage(), t);
        }
    }

    public static final int closeAll() {
        int count = 0;
        synchronized (DruidDataSourceFactory.class) {
            for (DruidDataSource dataSource : DRUID_DATA_SOURCE_LIST) {
                dataSource.close();
                LOGGER.info("close DruidDataSource success:url={}", dataSource.getUrl());
                count++;
            }
        }

        LOGGER.info("close all dataSource success:{}", count);

        return count;
    }

    public static final void msg() {
        for (DruidDataSource dataSource : DRUID_DATA_SOURCE_LIST) {
            LOGGER.info("database connections:url={},count={},active={},open={},close={}"
                    , dataSource.getUrl(), dataSource.getPoolingCount()
                    , dataSource.getActiveCount(), dataSource.getConnectCount(), dataSource.getCloseCount());
        }
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }
}
