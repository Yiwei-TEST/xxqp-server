package com.sy599.game.db.dao;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy599.game.db.bean.UserCardRecordInfo;
import com.sy599.game.util.LogUtil;

import java.util.List;

public class UserCardRecordDao extends BaseDao {
	private static UserCardRecordDao _inst = new UserCardRecordDao();

	public static UserCardRecordDao getInstance() {
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


    public void batchInsert(final List<UserCardRecordInfo> list){
		try {
			if (list != null && list.size()>0) {
				SqlMapClient sqlMapClient = getUseSqlClient();
				sqlMapClient.startBatch();
				for ( int i = 0, n = list.size(); i < n; i++) {
					sqlMapClient.insert("userCardRecord.saveUserCardRecord", list.get(i));
				}
				sqlMapClient.executeBatch();
				LogUtil.msgLog.info("batchInsert saveUserCardRecord success:count={}",list.size());
			}
		} catch (Exception e) {
			LogUtil.dbLog.error("batchInsert saveUserCardRecord Exception:"+e.getMessage(),e);
			for ( int i = 0, n = list.size(); i < n; i++) {
				try {
                    getUseSqlClient().update("userCardRecord.saveUserCardRecord", list.get(i));
				} catch (Exception e0) {
					LogUtil.dbLog.error("singleInsert saveUserCardRecord Exception:" + e0.getMessage(), e0);
				}
			}
		}
	}
}
