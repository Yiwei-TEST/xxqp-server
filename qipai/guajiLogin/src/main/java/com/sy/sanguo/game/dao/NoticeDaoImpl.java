package com.sy.sanguo.game.dao;
 
import java.sql.SQLException;
import java.util.List;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.parent.impl.CommonDaoImpl;
import com.sy.sanguo.game.bean.DBNotice;
import com.sy.sanguo.game.bean.SystemMessage;

public class NoticeDaoImpl extends CommonDaoImpl {

	@SuppressWarnings("unchecked")
	public List<DBNotice> loadNotice() throws Exception {
		return (List<DBNotice>)this.getSqlMapClient().queryForList("notice.selectSystemNotice");
	}
	
	@SuppressWarnings("unchecked")
	public List<SystemMessage> getAllSystemMessage() {
		
		List<SystemMessage> systemMessages = null;
		try {
			Object object = this.getSqlMapClient().queryForList("notice.getAllSystemMessage");
			if (object != null) {
				systemMessages = (List<SystemMessage>) object;
			}
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("notice.getAllSystemMessage err", e);
		}
		
		return systemMessages;
	}
}
