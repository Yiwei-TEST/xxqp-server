package com.sy599.game.db.dao;

import com.alibaba.fastjson.JSONObject;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy599.game.common.asyn.AsynExecutor;
import com.sy599.game.db.bean.PdkTableLog;
import com.sy599.game.db.bean.PlayLogTable;
import com.sy599.game.db.bean.PlayLogUser;
import com.sy599.game.db.bean.UserGroupPlaylog;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.StringUtil;

import java.sql.SQLException;
import java.util.List;

public class TableLogDao extends BaseDao {
	private static TableLogDao _inst = new TableLogDao();

	public static TableLogDao getInstance() {
		return _inst;
	}

	/**
	 * 使用日志库
	 * @return
	 */
	protected SqlMapClient getUseSqlClient() {
		SqlMapClient sqlMapClient = getSqlLogClient();
		if (sqlMapClient == null) {
			sqlMapClient = getSqlLoginClient();
		}
		return sqlMapClient;
	}

	public long save(PdkTableLog tableInfo) {
		try {
			return (Long) getUseSqlClient().insert("tablelog.savetablelogInfo", tableInfo);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#tablelog.savetablelogInfo:" + tableInfo.getTableId(), e);
		}

		return 0;
	}

	@SuppressWarnings("unchecked")
	public List<UserPlaylog> selectUserLogByLogId(List<Long> logId) {
		try {
			return (List<UserPlaylog>) getUseSqlClient().queryForList("tablelog.selectUserLogByLogId", StringUtil.implode(logId));
		} catch (SQLException e) {
			LogUtil.dbLog.error("#tablelog.selectUserLogById:" + logId, e);
		}
		return null;
	}

	public List<UserPlaylog> selectBaiRenRecent20TableLog(int logId) {
		try {
			return (List<UserPlaylog>) getUseSqlClient().queryForList("tablelog.selectBaiRenRecent20TableLog", logId);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#tablelog.selectLHDRecent20TableLog:", e);
		}
		return null;
	}

	public long save(UserPlaylog info) {
		try {
			if (AsynExecutor.canAsyn()){
				Long result = AsynExecutor.loadUserPlayLogId(info);
				if (result==null){
					LogUtil.monitorLog.info("asyn UserPlaylog:"+JSONObject.toJSONString(info));
				}else{
					return result.longValue();
				}
			}else{
				return (Long) getUseSqlClient().insert("tablelog.saveuserlogInfo", info);
			}
		} catch (Exception e) {
			LogUtil.dbLog.error("#tablelog.saveuserlogInfo:" + info.getTableId(), e);
		}

		return 0;
	}

	public long saveDb(UserPlaylog info,boolean saveDb) {
		try {
			if (!saveDb && AsynExecutor.canAsyn()){
				Long result = AsynExecutor.loadUserPlayLogId(info);
				if (result==null){
					LogUtil.monitorLog.info("asyn UserPlaylog:"+JSONObject.toJSONString(info));
				}else{
					return result.longValue();
				}
			}else{
				getUseSqlClient().insert("tablelog.saveuserlogInfo0", info);
				return info.getId();
			}
		} catch (Exception e) {
			LogUtil.dbLog.error("#tablelog.saveuserlogInfo0:" + info.getTableId(), e);
		}

		return 0;
	}

	public long saveGroupPlayLog(UserGroupPlaylog info) {
		try {
			if (AsynExecutor.canAsynGroup()){
				Long result = AsynExecutor.loadGroupPlayLogId(info);
				if (result==null){
					LogUtil.monitorLog.info("asyn UserGroupPlaylog:"+JSONObject.toJSONString(info));
				}else{
					return result.longValue();
				}
			}else{
				return (Long) getSqlLoginClient().insert("tablelog.saveGroupPlayLog", info);
			}
		} catch (Exception e) {
			LogUtil.dbLog.error("#tablelog.saveGroupPlayLog:" + info.getTableid(), e);
		}
		return 0;
	}

	public long saveGroupPlayLogDb(UserGroupPlaylog info,boolean saveDb) {
		try {
			if (!saveDb && AsynExecutor.canAsynGroup()){
				Long result = AsynExecutor.loadGroupPlayLogId(info);
				if (result==null){
					LogUtil.monitorLog.info("asyn UserGroupPlaylog:"+JSONObject.toJSONString(info));
				}else{
					return result.longValue();
				}
			}else{
				getSqlLoginClient().insert("tablelog.saveGroupPlayLog0", info);
				return info.getId();
			}
		} catch (Exception e) {
			LogUtil.dbLog.error("#tablelog.saveGroupPlayLog0:" + info.getTableid(), e);
		}

		return 0;
	}

	public void updateGroupPlayLog(UserGroupPlaylog info){
		try {
			if (AsynExecutor.updateAaynGroup()){
				Long result = AsynExecutor.loadUpdateGroupPlayLogId(info);
				if (result==null){
					LogUtil.monitorLog.info("asyn UserGroupPlaylog:"+JSONObject.toJSONString(info));
				}
			}else{
				getSqlLoginClient().update("tablelog.updateGroupPlayLog", info);
			}
		} catch (Exception e) {
			LogUtil.dbLog.error("#tablelog.updateGroupPlayLog:" + info.getTableid(), e);
		}
	}

	public void updateGroupPlayLogDb(UserGroupPlaylog info,boolean saveDb) {
		try {
			if (!saveDb && AsynExecutor.updateAaynGroup()){
				Long result = AsynExecutor.loadUpdateGroupPlayLogId(info);
				if (result==null){
					LogUtil.monitorLog.info("asyn UserGroupPlaylog:"+JSONObject.toJSONString(info));
				}
			}else{
				getSqlLoginClient().update("tablelog.updateGroupPlayLog0", info);
			}
		} catch (Exception e) {
			LogUtil.dbLog.error("#tablelog.updateGroupPlayLog0:" + info.getTableid(), e);
		}
	}

	public long saveGroupPlayLogAh(UserGroupPlaylog info) {
		try {
			if (AsynExecutor.canAsynGroup()){
				Long result = AsynExecutor.loadGroupPlayLogId(info);
				if (result==null){
					LogUtil.monitorLog.info("asyn UserGroupPlaylog:"+JSONObject.toJSONString(info));
				}else{
					return result.longValue();
				}
			}else{
				return (Long) getUseSqlClient().insert("tablelog.saveGroupPlayLog", info);
			}
		} catch (Exception e) {
			LogUtil.dbLog.error("#tablelog.saveGroupPlayLog:" + info.getTableid(), e);
		}
		return 0;
	}

	public long saveGroupPlayLogDbAh(UserGroupPlaylog info,boolean saveDb) {
		try {
			if (!saveDb && AsynExecutor.canAsynGroup()){
				Long result = AsynExecutor.loadGroupPlayLogId(info);
				if (result==null){
					LogUtil.monitorLog.info("asyn UserGroupPlaylog:"+JSONObject.toJSONString(info));
				}else{
					return result.longValue();
				}
			}else{
				getUseSqlClient().insert("tablelog.saveGroupPlayLog0", info);
				return info.getId();
			}
		} catch (Exception e) {
			LogUtil.dbLog.error("#tablelog.saveGroupPlayLog0:" + info.getTableid(), e);
		}

		return 0;
	}

	public void updateGroupPlayLogAh(UserGroupPlaylog info){
		try {
			if (AsynExecutor.updateAaynGroup()){
				Long result = AsynExecutor.loadUpdateGroupPlayLogId(info);
				if (result==null){
					LogUtil.monitorLog.info("asyn UserGroupPlaylog:"+JSONObject.toJSONString(info));
				}
			}else{
				getUseSqlClient().update("tablelog.updateGroupPlayLog", info);
			}
		} catch (Exception e) {
			LogUtil.dbLog.error("#tablelog.updateGroupPlayLog:" + info.getTableid(), e);
		}
	}

	public void updateGroupPlayLogDbAh(UserGroupPlaylog info,boolean saveDb) {
		try {
			if (!saveDb && AsynExecutor.updateAaynGroup()){
				Long result = AsynExecutor.loadUpdateGroupPlayLogId(info);
				if (result==null){
					LogUtil.monitorLog.info("asyn UserGroupPlaylog:"+JSONObject.toJSONString(info));
				}
			}else{
				getUseSqlClient().update("tablelog.updateGroupPlayLog0", info);
			}
		} catch (Exception e) {
			LogUtil.dbLog.error("#tablelog.updateGroupPlayLog0:" + info.getTableid(), e);
		}
	}

    public long savePlayLogTable(PlayLogTable info) {
        try {
//            if (AsynExecutor.canAsynGroup()){
//                Long result = AsynExecutor.loadPlayLogTableId(info);
//                if (result==null){
//                    LogUtil.monitorLog.info("asyn savePlayLogTable:"+JSONObject.toJSONString(info));
//                }else{
//                    return result.longValue();
//                }
//            }else{
                return (Long) getSqlLoginClient().insert("tablelog.savePlayLogTable", info);
//            }
        } catch (Exception e) {
            LogUtil.dbLog.error("#tablelog.savePlayLogTable:" + info.getTableId(), e);
        }
        return 0;
    }

    public long savePlayLogUser(PlayLogUser info) {
        try {
            return (Long) getSqlLoginClient().insert("tablelog.savePlayLogUser", info);
        } catch (Exception e) {
            LogUtil.dbLog.error("#tablelog.savePlayLogUser:" + info.getKeyId(), e);
        }
        return 0;
    }
}
