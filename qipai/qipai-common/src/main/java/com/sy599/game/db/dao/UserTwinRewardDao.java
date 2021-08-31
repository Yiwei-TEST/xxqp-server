package com.sy599.game.db.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy599.game.db.bean.UserTwinReward;
import com.sy599.game.util.LogUtil;

public class UserTwinRewardDao extends BaseDao {

	private static UserTwinRewardDao _inst = new UserTwinRewardDao();

	public static UserTwinRewardDao getInstance() {
		return _inst;
	}

	protected SqlMapClient getMissionSqlClient() {
		SqlMapClient sqlMapClient = getSqlLoginClient();
		return sqlMapClient;
	}

	@SuppressWarnings("unchecked")
	public UserTwinReward getUserTwinReward(long userId) {
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("userId", userId);
			return (UserTwinReward) this.getSqlLoginClient().queryForObject("UserTwinReward.getUserTwinReward", map);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#UserTwinRewardDao.getUserTwinReward:", e);
		}
		return null;
	}

	// public void insert(MissionAbout ma) {
	// try {
	// this.getSqlLoginClient().insert("missionAbout.insert", ma);
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }
	// }

	public int update(UserTwinReward ma) {
		int update = 0;
		try {
			update = this.getSqlLoginClient().update("UserTwinReward.updateUserTwinReward", ma);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#UserTwinRewardDao.updateUserTwinReward:", e);
		}
		return update;
	}

	public void batchUpdate(final Map<Long, UserTwinReward> map) {
		try {
			if (map != null && map.size() > 0) {
				SqlMapClient sqlMapClient = getMissionSqlClient();
				sqlMapClient.startBatch();
				for (UserTwinReward ma : map.values()) {
					sqlMapClient.update("UserTwinReward.updateUserTwinReward", ma);
				}
				sqlMapClient.executeBatch();
				LogUtil.msgLog.info("batchInsert userTwinReward success:count={}", map.size());
			}
		} catch (Exception e) {
			LogUtil.dbLog.error("batchInsert userTwinReward Exception:" + e.getMessage(), e);
		}
	}

	public void deleteUserTwinReward() {
		try {
			this.getSqlLoginClient().update("UserTwinReward.clearUserTwinReward");
		} catch (SQLException e) {
			LogUtil.e("clearUserTwinReward err-->", e);
		}
	}

}
