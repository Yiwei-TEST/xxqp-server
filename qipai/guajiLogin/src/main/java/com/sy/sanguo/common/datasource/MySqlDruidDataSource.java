package com.sy.sanguo.common.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySqlDruidDataSource extends DruidDataSource {
    private final static Logger LOGGER = LoggerFactory.getLogger(MySqlDruidDataSource.class);

    public static final boolean NEW_DRIVER;
    public static volatile String dbName;

    static {
        boolean isNew = true;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            isNew = false;
        }
        NEW_DRIVER = isNew;
    }

    @Override
    public void setUrl(String jdbcUrl) {
        if (NEW_DRIVER) {
            if (!jdbcUrl.contains("useSSL=")) {
                if (jdbcUrl.contains("?")) {
                    jdbcUrl = jdbcUrl + "&useSSL=false";
                } else {
                    jdbcUrl = jdbcUrl + "?useSSL=false";
                }
            }

            if (!jdbcUrl.contains("serverTimezone=")) {
                if (jdbcUrl.contains("?")) {
                    jdbcUrl = jdbcUrl + "&serverTimezone=GMT%2B8";
                } else {
                    jdbcUrl = jdbcUrl + "?serverTimezone=GMT%2B8";
                }
            }

            if (jdbcUrl.contains("zeroDateTimeBehavior=exception")) {
                jdbcUrl = jdbcUrl.replace("zeroDateTimeBehavior=exception", "zeroDateTimeBehavior=EXCEPTION");
            } else if (jdbcUrl.contains("zeroDateTimeBehavior=round")) {
                jdbcUrl = jdbcUrl.replace("zeroDateTimeBehavior=round", "zeroDateTimeBehavior=ROUND");
            } else if (jdbcUrl.contains("zeroDateTimeBehavior=convertToNull")) {
                jdbcUrl = jdbcUrl.replace("zeroDateTimeBehavior=convertToNull", "zeroDateTimeBehavior=CONVERT_TO_NULL");
            }
        }

        if (!jdbcUrl.contains("rewriteBatchedStatements=")){
            jdbcUrl = jdbcUrl + (jdbcUrl.contains("?") ? "&rewriteBatchedStatements=true" : "?rewriteBatchedStatements=true");
        }

        if (!jdbcUrl.contains("useAffectedRows=")){
            jdbcUrl = jdbcUrl + "&useAffectedRows=true";
        }

        String tempDbName = loadDbNameFromUrl(jdbcUrl);
        if (StringUtils.isBlank(dbName)){
            dbName = tempDbName;
        }else if ((!dbName.contains("login"))&&tempDbName.contains("login")){
            dbName = tempDbName;
        }

        LOGGER.info("init DruidDataSource success:newDriver={},url={},dbName={}", NEW_DRIVER, jdbcUrl,tempDbName);
        super.setUrl(jdbcUrl);
    }

    public static String loadDbName(){
        return dbName;
    }

    public static String loadDbNameFromUrl(String jdbcUrl) {
        String dbName = null;
        if (jdbcUrl != null) {
            int idx = jdbcUrl.indexOf("?");
            if (idx >= 0) {
                String temp = jdbcUrl.substring(0, idx);
                idx = temp.lastIndexOf("/");
                if (idx >= 0) {
                    dbName = temp.substring(idx + 1);
                }
            }
        }

        return dbName;
    }
}
