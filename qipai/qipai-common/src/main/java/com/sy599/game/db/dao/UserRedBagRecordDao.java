package com.sy599.game.db.dao;

import com.sy599.game.db.bean.activityRecord.UserRedBagRecord;
import com.sy599.game.util.LogUtil;

import java.util.List;

public class UserRedBagRecordDao extends BaseDao {
	
	private static UserRedBagRecordDao _inst = new UserRedBagRecordDao();

	public static UserRedBagRecordDao getInstance() {
		return _inst;
	}

	public List<UserRedBagRecord> getUserRedBagRecord(long userId) {
		try {
			return (List<UserRedBagRecord>)getSqlLoginClient().queryForList("userRedBag.getUserRedBagRecord", userId);
		}catch (Exception e) {
			LogUtil.errorLog.error("userRedBag.getUserRedBagRecord Exception:" + e.getMessage(), e);
		}
		return null;
	}

	public void saveUserRedBagRecord(UserRedBagRecord userRedBagRecord) {
		try {
			getSqlLoginClient().update("userRedBag.saveUserRedBagRecord", userRedBagRecord);
		} catch (Exception e) {
			LogUtil.e("userRedBag.saveUserRedBagRecord err", e);
		}
	}
}
