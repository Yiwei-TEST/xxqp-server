package com.sy599.game.db.dao;

import com.sy599.game.db.bean.SystemMarquee;
import com.sy599.game.util.LogUtil;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class MarqueeDao extends BaseDao {
	private static MarqueeDao _inst = new MarqueeDao();

	public static MarqueeDao getInstance() {
		return _inst;
	}

	@SuppressWarnings("unchecked")
	public List<SystemMarquee> loadMarquee() {
		try {
			return (List<SystemMarquee>) this.getSqlLoginClient().queryForList("systemMarquee.getUseMarquee");
		} catch (SQLException e) {
			LogUtil.e("getUseMarquee err", e);
		}
		return null;
	}

	public Date selectNewMarqueeTime() {
		try {
			return (Date) this.getSqlLoginClient().queryForObject("systemMarquee.selectNewMarqueeTime");
		} catch (SQLException e) {
			LogUtil.e("selectNewMarqueeTime err", e);
		}
		return null;
	}
}
