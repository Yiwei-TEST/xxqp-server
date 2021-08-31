package com.sy599.game.db.dao;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.db.bean.SystemCommonInfo;
import com.sy599.game.util.LogUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public int update(final SystemCommonInfo bat) {
		int ret = -1;
		try {
			ret = getSqlClient().update("systemCommonInfo.update", bat);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#SystemCommonInfoDao.update error:" + bat.getType(), e);
		} finally {
			LogUtil.msgLog.info("update SystemCommonInfo:type="+bat.getType()+",content="+bat.getContent()+",ret="+ret);
		}
		return ret;
	}

	public int updateServerSwitch(String type,String oldContent,String newContent) {
		int ret = -1;
		try {
			HashMap<String,Object> map=new HashMap<>();
			map.put("type",type);
			map.put("content0",oldContent);
			map.put("content",newContent);
			ret = getSqlClient().update("systemCommonInfo.updateServerSwitch", map);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#SystemCommonInfoDao.updateServerSwitch error:" + type, e);
		} finally {
			LogUtil.msgLog.info("update SystemCommonInfo:type="+type+",oldContent="+oldContent+",newContent="+newContent+",ret="+ret);
		}
		return ret;
	}

	public void updateLogin(String type,String content) {
		try {
			Map<String,Object> map=new HashMap<String, Object>();
			map.put("type", type);
			map.put("content", content);
			getSqlLoginClient().update("systemCommonInfo.updateContent", map);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#SystemCommonInfoDao.updateLogin error:" +type, e);
		}
	}

	public void sumLogin(String type,String content) {
		try {
			Map<String,Object> map=new HashMap<String, Object>();
			map.put("type", type);
			map.put("content", content);
			getSqlLoginClient().update("systemCommonInfo.sumContent", map);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#SystemCommonInfoDao.sumLogin error:" +type, e);
		}
	}
	
	
	public Object updateLoginSql(String sql){
		try {
			return this.getSqlLoginClient().update("systemCommonInfo.updateSql", sql);
		} catch (SQLException e) {
			LogUtil.dbLog.error("sql.updateSql", e);
		}
		return null;
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
			getSqlClient().insert("systemCommonInfo.save", bat);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#SystemCommonInfoDao.save error", e);
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
			return (List<SystemCommonInfo>) getSqlClient().queryForList("systemCommonInfo.selectAll");
		} catch (SQLException e) {
			LogUtil.dbLog.error("#SystemCommonInfoDao.selectAll error", e);
		}
		return null;
	}

	public int selectOne() {
		try {
			return (Integer) getSqlClient().queryForObject("systemCommonInfo.selectOne");
		} catch (SQLException e) {
			LogUtil.dbLog.error("#SystemCommonInfoDao.selectOne error:"+e.getMessage(), e);
		}
		return 0;
	}

	public int selectLoginOne() {
		try {
			return (Integer) getSqlLoginClient().queryForObject("loginSelectOne");
		} catch (SQLException e) {
			LogUtil.dbLog.error("#loginSelectOne error:"+e.getMessage(), e);
		}
		return 0;
	}

	public SystemCommonInfo selectLogin(String type) {
		try {
			return (SystemCommonInfo) getSqlLoginClient().queryForObject("systemCommonInfo.select", type);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#SystemCommonInfoDao.select error", e);
		}
		return null;
	}

	public SystemCommonInfo select(String type) {
		try {
			return (SystemCommonInfo) getSqlClient().queryForObject("systemCommonInfo.select", type);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#SystemCommonInfoDao.select error", e);
		}
		return null;
	}

	public Object update(String sql) throws Exception {
		return getSqlClient().update("systemCommonInfo.updateSql", sql);
	}

	public List<Map<String, Object>> showLoginColumns(String tableName) {
		return showSql("SHOW COLUMNS from " + tableName + ";", true);
	}

	public List<Map<String, Object>> showLoginTable(boolean loginDataBase) {
		return showSql("SHOW TABLES;", loginDataBase);
	}

	public boolean isHasTableName(boolean loginDataBase, String tableName) {
		List<Map<String, Object>> list = showLoginTable(loginDataBase);
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
	public List<Map<String, Object>> showSql(String sql, boolean isLoginDataBase) {
		try {
//			Map<String, Object> map = new HashMap<String, Object>();
//			map.put("sql", sql);
			// SHOW COLUMNS from $tablename$;
			SqlMapClient sqlMapClient = null;
			if (isLoginDataBase) {
				sqlMapClient = getSqlLoginClient();
			} else {
				sqlMapClient = getSqlClient();
			}
			return (List<Map<String, Object>>) sqlMapClient.queryForList("systemCommonInfo.showsql", sql);
		} catch (Exception e) {
			LogUtil.e("sql.showLoginSql", e);
		}
		return null;
	}
}
