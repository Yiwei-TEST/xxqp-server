package com.sy599.game.db.dao;

import com.sy599.game.db.bean.UserBindGameBureau;
import com.sy599.game.util.LogUtil;

public class UserBindGameBureauDao extends BaseDao {
	private static UserBindGameBureauDao _inst = new UserBindGameBureauDao();

	public static UserBindGameBureauDao getInstance() {
		return _inst;
	}

	public void saveUserBindGameBureau(UserBindGameBureau userBindGameBureau) {
		try {
			getSqlLoginClient().insert("userBindGameBureau.saveUserBindGameBureau", userBindGameBureau);
		} catch (Exception e) {
			LogUtil.e("UserBindGameBureau.userBindGameBureau err", e);
		}
	}
}
