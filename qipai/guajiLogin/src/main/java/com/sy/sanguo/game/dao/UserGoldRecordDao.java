package com.sy.sanguo.game.dao;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.game.bean.UserCardRecordInfo;
import com.sy.sanguo.game.bean.UserGoldRecord;
import com.sy.sanguo.game.pdkuai.db.dao.BaseDao;
import com.sy599.sanguo.util.SysPartitionUtil;

import java.util.List;

public class UserGoldRecordDao extends BaseDao {
	private static UserGoldRecordDao _inst = new UserGoldRecordDao();

	public static UserGoldRecordDao getInstance() {
		return _inst;
	}

    private SqlMapClient getUseSqlMapClient() {
        SqlMapClient sqlMapClient = getFuncSql();
        if (sqlMapClient == null) {
            sqlMapClient = getSql();
        }
        return sqlMapClient;
    }

    public void saveUserGoldRecord(UserGoldRecord log){
        try {
            log.setUserSeq(SysPartitionUtil.getUserGoldRecordSeq(log.getUserId()));
            getUseSqlMapClient().update("userGoldRecord.save_user_gold_record", log);
        } catch (Exception e0) {
            GameBackLogger.SYS_LOG.error("insert saveUserGoldRecord Exception:" + e0.getMessage(), e0);
        }
    }


}
