package com.sy.sanguo.game.dao;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.game.bean.UserGoldBeanRecord;
import com.sy.sanguo.game.bean.UserGoldRecord;
import com.sy.sanguo.game.pdkuai.db.dao.BaseDao;
import com.sy599.sanguo.util.SysPartitionUtil;

public class UserGoldBeanRecordDao extends BaseDao {
	private static UserGoldBeanRecordDao _inst = new UserGoldBeanRecordDao();

	public static UserGoldBeanRecordDao getInstance() {
		return _inst;
	}

    private SqlMapClient getUseSqlMapClient() {
        SqlMapClient sqlMapClient = getFuncSql();
        if (sqlMapClient == null) {
            sqlMapClient = getSql();
        }
        return sqlMapClient;
    }

    public void saveUserGoldBeanRecord(UserGoldBeanRecord log){
        try {
            getUseSqlMapClient().update("userGoldBeanRecord.save_user_gold_bean_record", log);
        } catch (Exception e0) {
            GameBackLogger.SYS_LOG.error("insert saveUserGoldBeanRecord Exception:" + e0.getMessage(), e0);
        }
    }


}
