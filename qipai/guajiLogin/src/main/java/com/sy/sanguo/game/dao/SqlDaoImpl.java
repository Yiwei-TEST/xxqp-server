package com.sy.sanguo.game.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.parent.impl.CommonDaoImpl;

public class SqlDaoImpl extends CommonDaoImpl {

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> showcolumns(String tableName) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("tablename", tableName);
			return (List<Map<String, Object>>) this.getSqlMapClient().queryForList("sql.showcolumns", map);
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("sql.showcolumns", e);
		}
		return null;
	}
	
	public Object update(String sql){
		try {
			return this.getSqlMapClient().update("sql.updateSql", sql);
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("sql.updateSql", e);
		}
		return null;
	}
	
	public Object createTable(String sql){
		try {
			return this.getSqlMapClient().update("sql.createTable", sql);
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("sql.createTable", e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> showIndex(String tableName) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tablename", tableName);
		try {
			return this.getSqlMapClient().queryForList("sql.showIndex", map);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
