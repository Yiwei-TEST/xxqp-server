package com.sy599.game.db.dao;

import com.sy599.game.db.bean.UserGameSite;
import com.sy599.game.util.LogUtil;

import java.sql.SQLException;

public class GameSiteDao extends BaseDao {
	private static GameSiteDao _inst = new GameSiteDao();

	public static GameSiteDao getInstance() {
		return _inst;
	}

	/**
	 * 根据userId取玩家比赛场数据
	 * 
	 * @param userId
	 * @return
	 */
	public UserGameSite queryUserGameSite(long userId) {
		UserGameSite userGameSite = null;
		try {
			Object result = getSqlLoginClient().queryForObject("gamesite.queryUserGameSite", userId);
			if (result != null) {
				userGameSite = (UserGameSite) result;
			}
		} catch (SQLException e) {
			LogUtil.dbLog.error("#gameSite.queryUserGameSite error:", e);
		}
		return userGameSite;
	}

}
