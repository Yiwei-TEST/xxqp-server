package com.sy.sanguo.game.pdkuai.db.dao;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.init.InitData;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.Constants;
import com.sy.sanguo.common.util.StringUtil;
import com.sy.sanguo.game.pdkuai.db.bean.DaikaiTable;
import com.sy.sanguo.game.pdkuai.db.bean.UserPlaylog;

public class DaikaiTableDao extends BaseDao {
	
	private static DaikaiTableDao _inst = new DaikaiTableDao();

	public static DaikaiTableDao getInstance() {
		return _inst;
	}
	
	/**
	 * 获取代开的房间
	 * 
	 * @return
	 */
	public DaikaiTable getDaikaiTable(long tableId) {
		
		DaikaiTable daikaiTable = null;
		try {
			Object object = getSql().queryForObject("daikai.getDaikaiTable", tableId);
			if (object != null) {
				daikaiTable = (DaikaiTable) object;
			}
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("GameSiteDao.getDaikaiTable error:", e);
		}

		return daikaiTable;
	}
	
	
	/**
	 * 获取代开的房间
	 * 
	 * @param daikaiId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<DaikaiTable> getDaikaiList(long daikaiId) {
		
		List<DaikaiTable> daikaiList = null;
		try {
			Object object = getSql().queryForList("daikai.getDaikaiList", daikaiId);
			if (object != null) {
				daikaiList = (List<DaikaiTable>) object;
			}
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("GameSiteDao.getDaikaiList error:", e);
		}

		return daikaiList;
	}
	
	private SqlMapClient getUseSqlMapClient() {
		SqlMapClient sqlMapClient = getFuncSql();
		if (sqlMapClient == null) {
			sqlMapClient = getSql();
		}
		return sqlMapClient;
	}
	
	/**
	 * 根据ID取打牌日志
	 * 
	 * @param logId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserPlaylog> selectUserLogByLogId(List<Long> logId) {
		try {
			return (List<UserPlaylog>) getUseSqlMapClient().queryForList("daikai.selectUserLogByLogId", StringUtil.implode(logId));
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("#tablelog.selectUserLogByLogId:" + logId, e);
		}
		return null;
	}

	/**
	 * 根据tableId取打牌日志
	 * 
	 * @param tableIds
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserPlaylog> queryRecordIdByTableId(String tableIds, long userId) {
		Map<String, Object> map = new HashMap<>();
		map.put("id", tableIds);

		if ("1".equals(PropertiesCacheUtil.getValue("queryRecordIdWithUserId",Constants.GAME_FILE))){
			map.put("userId", userId);
		}

		try {
			return (List<UserPlaylog>) getUseSqlMapClient().queryForList("daikai.queryRecordIdByTableId", map);
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("#tablelog.queryRecordIdByTableId:" + map, e);
		}
		return null;
	}

	/**
	 * 取有打牌日志的代开房间号和createPara
	 * 
	 * @param userId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<DaikaiTable> getRecordTableId(long userId) {
		try {
			return (List<DaikaiTable>) getSql().queryForList("daikai.getRecordTableId", userId);
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("#tablelog.getRecordTableId:" + userId, e);
		}
		return null;
	}

	/**
	 * 获取需要清理的代开房间
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<DaikaiTable> getNeedClearDaikaiTable(Date overdueTime) {
		try {
			return (List<DaikaiTable>) getSql().queryForList("daikai.getNeedClearDaikaiTable", overdueTime);
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("#tablelog.getNeedClearDaikaiTable:", e);
		}
		
		return null;
	}
	
	/**
	 * 清理某个代开房间
	 * 
	 * @param tableId
	 * @param daikaiId
	 * @return
	 */
	public int clearDaikaiTable(long tableId, long daikaiId) {
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("tableId", tableId);
		paramMap.put("daikaiId", daikaiId);
		
		try {
			return (Integer) getSql().update("daikai.clearDaikaiTable", paramMap);
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("#tablelog.clearDaikaiTable:", e);
		}
		
		return 0;
	}
}
