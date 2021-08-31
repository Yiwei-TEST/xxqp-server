package com.sy.sanguo.game.pdkuai.db.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sy.sanguo.common.parent.impl.CommonDaoImpl;
import com.sy.sanguo.common.util.TaskExecutor;
import com.sy.sanguo.game.pdkuai.db.bean.UserInf;
import com.sy.sanguo.game.pdkuai.util.LogUtil;

public class GameUserDao extends CommonDaoImpl {
	private static GameUserDao _inst = new GameUserDao();

	public static GameUserDao getInstance() {
		return _inst;
	}

	public UserInf selectUserByUserId(long userId) throws SQLException {
		UserInf result = null;
		try {
			result = (UserInf) getSqlMapClient().queryForObject("usergame.selectUserByUserId", userId);
		} catch (SQLException e) {
			LogUtil.e("#UserDao.selectUserByUserId:" + userId, e);
			throw e;
		}
		return result;
	}

	public UserInf selectPlayerByFlatId(String flatId) throws SQLException {
		UserInf result = null;
		try {
			result = (UserInf) getSqlMapClient().queryForObject("usergame.selectUserByFlatId", flatId);
		} catch (SQLException e) {
			LogUtil.e("#UserDao.selectPlayerByFlatId:" + flatId, e);
			throw e;
		}
		return result;
	}

	public void save(UserInf userInfo) throws Exception {
		try {
			getSqlMapClient().insert("usergame.saveUserInfo", userInfo);

		} catch (SQLException e) {
			LogUtil.e("#PlayerDao.save:" + userInfo.getUserId(), e);
			throw e;
		}

		return;
	}

	public void syncSave(Map<String, Object> paramMap) throws Exception {
		LogUtil.i(String.format("updUser,uId:%d", paramMap.get("userId")));
		getSqlMapClient().update("usergame.updateUserInfo", paramMap);
	}

	public int save(Map<String, Object> paramMap) {
		int update=0;
		try {
			LogUtil.i(String.format("updUser,uId:%d", paramMap.get("userId")));
			update=getSqlMapClient().update("usergame.updateUserInfo", paramMap);

		} catch (SQLException e) {
			LogUtil.e("#PlayerDao.dbSave.Sql", e);
		}
		return update;

	}

	public void asynchSave(long userId, final Map<String, Object> paramMap) {
		TaskExecutor.getInstance().submitOrderTask(userId, new Runnable() {

			public void run() {
				save(paramMap);

			}
		});
	}

	public int selectPlayerCount() {
		int playerCount = 0;
		try {
			Object result = getSqlMapClient().queryForObject("usergame.selectUserCount");
			if (result != null) {
				playerCount = (Integer) result;
			}

		} catch (SQLException e) {
			LogUtil.e("#selectUserCount:", e);
		}

		return playerCount;
	}

	public long selectPlayerMaxId() {
		long maxPlayerId = 0;
		try {
			Object result = getSqlMapClient().queryForObject("usergame.selectMaxUserId");
			if (result != null) {
				maxPlayerId = (Long) result;
			}

		} catch (SQLException e) {
			LogUtil.e("#UserDao.selectPlayerMaxId:", e);
		}

		return maxPlayerId;
	}

	@SuppressWarnings("unchecked")
	public List<UserInf> selectAll(int start, int end) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("start", start);
		map.put("end", end);
		List<UserInf> result = new ArrayList<UserInf>();
		try {
			result = (List<UserInf>) getSqlMapClient().queryForList("usergame.selectActiveUser", map);
		} catch (SQLException e) {
			LogUtil.e("#PlayerDao.selectActiveUser:", e);
		}
		return result;
	}

}
