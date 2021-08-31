package com.sy599.game.db.dao;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy599.game.db.bean.UserCardRecordInfo;
import com.sy599.game.util.LogUtil;

import java.util.List;
import java.util.Map;

public class UserCoinRecordDao extends BaseDao {
	private static UserCoinRecordDao _inst = new UserCoinRecordDao();

	public static UserCoinRecordDao getInstance() {
		return _inst;
	}

    /**
     * 使用日志库
     * @return
     */
    protected SqlMapClient getUseSqlClient() {
        SqlMapClient sqlMapClient = getSqlLogClient();
        if (sqlMapClient == null) {
            sqlMapClient = getSqlLoginClient();
        }
        return sqlMapClient;
    }


    public void batchInsert(final List<Map<String,Object>> list){
		try {
			if (list != null && list.size()>0) {
				SqlMapClient sqlMapClient = getUseSqlClient();
				sqlMapClient.startBatch();
				for ( int i = 0, n = list.size(); i < n; i++) {
					sqlMapClient.insert("userCoinRecord.saveUserCoinRecord", list.get(i));
				}
				sqlMapClient.executeBatch();
				LogUtil.msgLog.info("batchInsert saveUserCoinRecord success:count={}",list.size());
			}
		} catch (Exception e) {
			LogUtil.dbLog.error("batchInsert saveUserCoinRecord Exception:"+e.getMessage(),e);
			for ( int i = 0, n = list.size(); i < n; i++) {
				try {
                    getUseSqlClient().update("userCoinRecord.saveUserCoinRecord", list.get(i));
				} catch (Exception e0) {
					LogUtil.dbLog.error("singleInsert saveUserCoinRecord Exception:" + e0.getMessage(), e0);
				}
			}
		}
	}
}
