package com.sy599.game.db.dao;

import com.sy599.game.db.bean.UserFirstmyth;
import com.sy599.game.util.LogUtil;

public class UserFirstmythDao extends BaseDao {
	private static UserFirstmythDao _inst = new UserFirstmythDao();

	public static UserFirstmythDao getInstance() {
		return _inst;
	}

	public void saveUserFirstmyth(UserFirstmyth bean) {
		try {
			getSqlLoginClient().update("userFirstmyth.saveUserFirstmyth", bean);
		} catch (Exception e) {
			LogUtil.e("userFirstmyth.saveUserFirstmyth err", e);
		}
	}
}
