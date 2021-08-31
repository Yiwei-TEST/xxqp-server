package com.sy599.game.db.dao;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy599.game.db.bean.UserGameBureau;
import com.sy599.game.db.bean.UserGoldRecord;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SysPartitionUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserGoldRecordDao extends BaseDao {
    private static UserGoldRecordDao _inst = new UserGoldRecordDao();

    public static UserGoldRecordDao getInstance() {
        return _inst;
    }

    /**
     * 使用日志库
     *
     * @return
     */
    protected SqlMapClient getUseSqlClient() {
        SqlMapClient sqlMapClient = getSqlLogClient();
        if (sqlMapClient == null) {
            sqlMapClient = getSqlLoginClient();
        }
        return sqlMapClient;
    }


    public void batchInsert(final List<UserGoldRecord> list) {
        try {
            if (list != null && list.size() > 0) {
                SqlMapClient sqlMapClient = getUseSqlClient();
                sqlMapClient.startBatch();
                for (int i = 0, n = list.size(); i < n; i++) {
                    UserGoldRecord log = list.get(i);
                    log.setUserSeq(SysPartitionUtil.getUserGoldRecordSeq(log.getUserId()));
                    sqlMapClient.insert("userGoldRecord.save_user_gold_record", log);
                }
                sqlMapClient.executeBatch();
                LogUtil.msgLog.info("batchInsert saveUserGoldRecord success:count={}", list.size());
            }
        } catch (Exception e) {
            LogUtil.dbLog.error("batchInsert saveUserCardRecord Exception:" + e.getMessage(), e);
            for (int i = 0, n = list.size(); i < n; i++) {
                try {
                    UserGoldRecord log = list.get(i);
                    log.setUserSeq(SysPartitionUtil.getUserGoldRecordSeq(log.getUserId()));
                    getUseSqlClient().update("userGoldRecord.save_user_gold_record", log);
                } catch (Exception e0) {
                    LogUtil.dbLog.error("singleInsert saveUserGoldRecord Exception:" + e0.getMessage(), e0);
                }
            }
        }
     
        
    }
    
    
    public List<UserGoldRecord> getUserGoldRecords(long  userId) {
    	List<UserGoldRecord> list2 = null;
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
    	   try {
    		   SqlMapClient sqlMapClient = getUseSqlClient();
               list2 = sqlMapClient.queryForList("userGoldRecord.get_user_gold_record", params);
               System.out.println("res list = "+list2);
           } catch (SQLException e) {
               LogUtil.e("userGoldRecord.get_user_gold_record err", e);
           }
    	   return list2;
        
    }
}
