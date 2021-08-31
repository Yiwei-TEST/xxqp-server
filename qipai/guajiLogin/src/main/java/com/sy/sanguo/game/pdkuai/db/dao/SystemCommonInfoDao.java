package com.sy.sanguo.game.pdkuai.db.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy.sanguo.common.util.TaskExecutor;
import com.sy.sanguo.game.pdkuai.db.bean.SystemCommonInfo;
import com.sy.sanguo.game.pdkuai.util.LogUtil;


/**
 * 系统公用Dao处理
 * 
 * @author lc
 * @date 2016-6-24
 * @version v1.0
 */
public class SystemCommonInfoDao extends BaseDao {

	private static SystemCommonInfoDao _inst = new SystemCommonInfoDao();

	public static SystemCommonInfoDao getInstance() {
		return _inst;
	}

	public void update(final SystemCommonInfo bat) {
		try {
			getSql().update("systemCommonInfo.update", bat);
		} catch (SQLException e) {
			LogUtil.e("#SystemCommonInfoDao.update error:" + bat.getType(), e);
		}
	}

	public void asynchUpdate(final SystemCommonInfo info) {
		TaskExecutor.getInstance().submitTask(new Runnable() {
			@Override
			public void run() {
				update(info);
			}
		});
	}

	public void save(final SystemCommonInfo bat) {
		try {
			getSql().insert("systemCommonInfo.save", bat);
		} catch (SQLException e) {
			LogUtil.e("#SystemCommonInfoDao.save error", e);
		}
	}

	public void asynchSave(final SystemCommonInfo info) {
		TaskExecutor.getInstance().submitTask(new Runnable() {
			@Override
			public void run() {
				save(info);
			}
		});
	}

	@SuppressWarnings("unchecked")
	public List<SystemCommonInfo> selectAll() {
		try {
			return (List<SystemCommonInfo>) getSql().queryForList("systemCommonInfo.selectAll");
		} catch (SQLException e) {
			LogUtil.e("#SystemCommonInfoDao.selectAll error", e);
		}
		return null;
	}
	
	public int selectOne() {
		try {
			return (Integer) getSql().queryForObject("systemCommonInfo.selectOne");
		} catch (Exception e) {
			LogUtil.e("#SystemCommonInfoDao.selectOne error", e);
		}
		return 0;
	}

	public int selectFuncOne() {
		try {
			return (Integer) getFuncSql().queryForObject("systemCommonInfo.selectOne");
		} catch (Exception e) {
			LogUtil.e("#SystemCommonInfoDao.selectOne error", e);
		}
		return 0;
	}

	public int selectLoginOne() {
		try {
			return (Integer) getSql().queryForObject("systemCommonInfo.selectOne");
		} catch (SQLException e) {
			LogUtil.e("#SystemCommonInfoDao.selectOne error", e);
		}
		return 0;
	}

	public SystemCommonInfo select(String type) {
		try {
			return (SystemCommonInfo) getSql().queryForObject("systemCommonInfo.select", type);
		} catch (SQLException e) {
			LogUtil.e("#SystemCommonInfoDao.select error:"+e.getMessage(), e);
		}
		return null;
	}

	public List<HashMap<String,Object>> select(String... types) {
		if (types==null||types.length==0){
			return  null;
		}
		try {
			StringBuilder strBuilder = new StringBuilder();
			for (String type : types){
				strBuilder.append(",'").append(type).append("'");
			}
			HashMap<String,Object> map = new HashMap<>(4);
			map.put("types",strBuilder.substring(1));
			return (List<HashMap<String,Object>>) getSql().queryForList("systemCommonInfo.selectConfig", map);
		} catch (SQLException e) {
			LogUtil.e("#SystemCommonInfoDao.select error:"+e.getMessage(), e);
		}
		return null;
	}

	public Object update(String sql) throws Exception {
		return getSql().update("systemCommonInfo.updateSql", sql);
	}

	public List<Map<String, Object>> showLoginColumns(String tableName) {
		return showSql("SHOW COLUMNS from " + tableName + ";");
	}

	public List<Map<String, Object>> showLoginTable() {
		return showSql("SHOW TABLES;");
	}

	public boolean isHasTableName(String tableName) {
		List<Map<String, Object>> list = showLoginTable();
		if (list == null) {
			return false;

		}
		boolean isHas = false;
		for (Map<String, Object> map : list) {
			if (map.containsValue(tableName)) {
				isHas = true;
				break;
			}
		}
		return isHas;
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> showSql(String sql) {
		try {
			SqlMapClient sqlMapClient = getSql();
			return (List<Map<String, Object>>) sqlMapClient.queryForList("systemCommonInfo.showsql", sql);
		} catch (Exception e) {
			LogUtil.e("systemCommonInfo.showLoginSql", e);
		}
		return null;
	}
}
