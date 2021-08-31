package com.sy599.game.db.dao;

import com.sy599.game.db.bean.JiFenRecordLog;
import com.sy599.game.util.LogUtil;

public class JiFenRecordLogDao extends BaseDao {
	private static JiFenRecordLogDao _inst = new JiFenRecordLogDao();

	public static JiFenRecordLogDao getInstance() {
		return _inst;
	}

	public void saveJiFenRecordLog(JiFenRecordLog recordLog) {
		try {
			getSqlLoginClient().insert("jifenRecordLog.saveJiFenRecordLog", recordLog);
		} catch (Exception e) {
			LogUtil.e("jifenRecordLog.saveJiFenRecordLog err", e);
		}
	}
}
