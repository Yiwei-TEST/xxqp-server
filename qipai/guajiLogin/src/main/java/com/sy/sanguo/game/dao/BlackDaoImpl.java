package com.sy.sanguo.game.dao;

import java.util.List;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.parent.impl.CommonDaoImpl;
import com.sy.sanguo.game.bean.SystemBlack;

public class BlackDaoImpl extends CommonDaoImpl {

	@SuppressWarnings("unchecked")
	public List<SystemBlack> selectSystemBlack() {
		try {
			return (List<SystemBlack>) this.getSqlMapClient().queryForList("black.selectSystemBlack");
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("selectSystemBlack err", e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<SystemBlack> selectNoLoadSystemBlack() {
		try {
			return (List<SystemBlack>) this.getSqlMapClient().queryForList("black.selectNoLoadSystemBlack");
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("selectNoLoadSystemBlack err", e);
		}
		return null;
	}

	public void updateLoad() {
		try {
			this.getSqlMapClient().update("black.updateLoad");
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("updateLoadupdateLoad err", e);
		}
	}
}
