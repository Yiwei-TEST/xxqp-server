package com.sy599.game.db.dao;

import com.sy599.game.db.bean.SevenGoldSign;
import com.sy599.game.util.LogUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SevenGoldSignDao extends BaseDao {
	private static SevenGoldSignDao _inst = new SevenGoldSignDao();

	public static SevenGoldSignDao getInstance() {
		return _inst;
	}

	@SuppressWarnings("unchecked")
	public List<SevenGoldSign> getGoldSign(long userId) {
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("userId", userId);
			return (List<SevenGoldSign>) this.getSqlLoginClient().queryForList("sevenGoldSign.getGoldSign", map);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#SevenGoldSignDao.getGoldSign:", e);
		}
		return null;
	}

	public void insert(SevenGoldSign sign) {
		try {
			this.getSqlLoginClient().insert("sevenGoldSign.insert", sign);
			LogUtil.dbLog.info("sevenGoldSign.insert"+sign.getUserId());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int updateGoldSign(SevenGoldSign sign) {
		Map<String, Object> map = new HashMap<>();
		map.put("userId", sign.getUserId());
		map.put("sevenSign", sign.getSevenSign());
		map.put("lastSignTime", sign.getLastSignTime());
		int update = 0;
		try {
			update = this.getSqlLoginClient().update("sevenGoldSign.updateGoldSign", map);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#SevenGoldSignDao.updateGoldSign:", e);
		}
		return update;
	}

}
