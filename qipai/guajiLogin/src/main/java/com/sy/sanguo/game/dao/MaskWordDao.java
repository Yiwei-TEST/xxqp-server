package com.sy.sanguo.game.dao;

import java.sql.SQLException;
import java.util.List;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.parent.impl.CommonDaoImpl;
import com.sy.sanguo.game.bean.SystemChatMaskWord;

public class MaskWordDao extends CommonDaoImpl {
	private static MaskWordDao _inst = new MaskWordDao();

	public static MaskWordDao getInstance() {
		return _inst;
	}

	public void add(SystemChatMaskWord bean) throws Exception {
		this.getSqlMapClient().insert("maskword.add", bean);
	}

	@SuppressWarnings("unchecked")
	public List<SystemChatMaskWord> getList() {
		try {
			return (List<SystemChatMaskWord>) this.getSqlMapClient().queryForList("maskword.getList");
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("getList err", e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<SystemChatMaskWord> getNoLoadList() {
		try {
			return (List<SystemChatMaskWord>) this.getSqlMapClient().queryForList("maskword.getNoLoadList");
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("getList err", e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<SystemChatMaskWord> getDelList() {
		try {
			return (List<SystemChatMaskWord>) this.getSqlMapClient().queryForList("maskword.getDelList");
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("getList err", e);
		}
		return null;
	}

	public void delete() {
		try {
			this.getSqlMapClient().delete("maskword.delete");
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("delete", e);
		}
	}

	public void updateLoad() {
		try {
			this.getSqlMapClient().update("maskword.updateLoad");
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("getList err", e);
		}
	}
}
