package com.sy.sanguo.game.dao;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.game.bean.UserCardRecordInfo;
import com.sy.sanguo.game.pdkuai.db.dao.BaseDao;

public class UserCardRecordDao extends BaseDao {
	private static UserCardRecordDao _inst = new UserCardRecordDao();

	public static UserCardRecordDao getInstance() {
		return _inst;
	}

    private SqlMapClient getUseSqlMapClient() {
        SqlMapClient sqlMapClient = getFuncSql();
        if (sqlMapClient == null) {
            sqlMapClient = getSql();
        }
        return sqlMapClient;
    }

	public void insert(UserCardRecordInfo info){
		try {
            getUseSqlMapClient().update("userCardRecord.saveUserCardRecord", info);
		} catch (Exception e0) {
			GameBackLogger.SYS_LOG.error("insert saveUserCardRecord Exception:" + e0.getMessage(), e0);
		}
	}

	public void clearExpireUserCardRecords() {
		try {
            getUseSqlMapClient().delete("userCardRecord.clearExpireUserCardRecords");
		} catch (Exception e0) {
			GameBackLogger.SYS_LOG.error("clearExpireUserCardRecords Exception:" + e0.getMessage(), e0);
		}
	}
}
