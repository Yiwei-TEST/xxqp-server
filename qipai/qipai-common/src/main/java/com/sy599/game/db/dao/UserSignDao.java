package com.sy599.game.db.dao;

import com.sy599.game.db.bean.UserSign;
import com.sy599.game.util.LogUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserSignDao extends BaseDao {
	private static UserSignDao _inst = new UserSignDao();

	public static UserSignDao getInstance() {
		return _inst;
	}

	@SuppressWarnings("unchecked")
	public List<UserSign> getUserSign(long userId, String begin, String end) {
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("userId", userId);
			map.put("begin", begin);
			map.put("end", end);
			return (List<UserSign>) this.getSqlLoginClient().queryForList("userSign.getSigns", map);
		} catch (SQLException e) {
			LogUtil.e("getUseSign err", e);
		}
		return null;
	}

	public void sign(UserSign userSign) {
		try {
			this.getSqlLoginClient().insert("userSign.sign", userSign);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
