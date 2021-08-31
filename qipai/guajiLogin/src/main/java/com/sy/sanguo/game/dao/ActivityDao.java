package com.sy.sanguo.game.dao;

import com.sy.sanguo.game.bean.Activity;
import com.sy.sanguo.game.pdkuai.db.dao.BaseDao;

import java.sql.SQLException;

public class ActivityDao extends BaseDao{
	private static ActivityDao _inst = new ActivityDao();

	public static ActivityDao getInstance() {
		return _inst;
	}

	public Activity getActivityById(int id) {
		try {
			return (Activity) getSql().queryForObject("activity.getActivityById", id);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
