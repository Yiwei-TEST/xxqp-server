package com.sy599.game.db.dao;

import com.sy599.game.db.bean.UserGameRebate;
import com.sy599.game.util.LogUtil;

public class UserGameRebateDao extends BaseDao {
	private static UserGameRebateDao _inst = new UserGameRebateDao();

	public static UserGameRebateDao getInstance() {
		return _inst;
	}

	public void saveUserGameRebate(UserGameRebate userGameRebate) {
		try {
			getSqlLoginClient().update("userGameRebate.saveUserGameRebate", userGameRebate);
		} catch (Exception e) {
			LogUtil.e("UserGameRebate.saveUserGameRebate err", e);
		}
	}
}
