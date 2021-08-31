package com.sy.sanguo.game.pdkuai.db.dao;


import com.sy.sanguo.game.bean.redbag.UserRedBagRecord;
import com.sy.sanguo.game.pdkuai.util.LogUtil;

import java.util.Date;
import java.util.List;

public class UserRedBagRecordDao extends BaseDao {
	
	private static UserRedBagRecordDao _inst = new UserRedBagRecordDao();

	public static UserRedBagRecordDao getInstance() {
		return _inst;
	}

	public List<UserRedBagRecord> getUserRedBagRecord(long userId) {
		try {
			return (List<UserRedBagRecord>)getSql().queryForList("userRedBag.getUserRedBagRecord", userId);
		}catch (Exception e) {
			LogUtil.e("userRedBag.getUserRedBagRecord Exception:" + e.getMessage(), e);
		}
		return null;
	}

	public List<UserRedBagRecord> getUserRedBagRecordByBigRedBag(String receiveDate) {
		try {
			return (List<UserRedBagRecord>)getSql().queryForList("userRedBag.getUserRedBagRecordByBigRedBag", receiveDate);
		}catch (Exception e) {
			LogUtil.e("userRedBag.getUserRedBagRecord Exception:" + e.getMessage(), e);
		}
		return null;
	}

	public void saveUserRedBagRecord(UserRedBagRecord userRedBagRecord) {
		try {
			getSql().update("userRedBag.saveUserRedBagRecord", userRedBagRecord);
		} catch (Exception e) {
			LogUtil.e("userRedBag.saveUserRedBagRecord err", e);
		}
	}
}
