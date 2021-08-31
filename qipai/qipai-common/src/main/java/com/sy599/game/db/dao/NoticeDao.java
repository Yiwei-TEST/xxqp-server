package com.sy599.game.db.dao;

import com.sy599.game.db.bean.DBNotice;
import com.sy599.game.util.LogUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class NoticeDao extends BaseDao {
	private static NoticeDao _inst = new NoticeDao();

	public static NoticeDao getInstance() {
		return _inst;
	}

	@SuppressWarnings("unchecked")
	public List<DBNotice> loadNotice() {
		try {
			return (List<DBNotice>) this.getSqlLoginClient().queryForList("notice.selectSystemNotice");
		} catch (SQLException e) {
			LogUtil.e("loadNotice err", e);
		}
		return null;
	}

	public String loadSystemNotice() {
		try {
			String notice = (String)this.getSqlLoginClient().queryForObject("notice.selectSystem1Notice");
			return StringUtils.isNotBlank(notice)?notice:null;
		} catch (SQLException e) {
			LogUtil.e("selectSystem1Notice err:"+e.getMessage(), e);
		}
		return null;
	}

	public Date selectNewNoticeTime() {
		try {
			return (Date) this.getSqlLoginClient().queryForObject("notice.selectNewNoticeTime");
		} catch (SQLException e) {
			LogUtil.e("selectNewNoticeTime err", e);
		}
		return null;
	}
}
