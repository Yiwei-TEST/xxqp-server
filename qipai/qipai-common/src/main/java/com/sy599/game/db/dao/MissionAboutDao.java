package com.sy599.game.db.dao;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy599.game.db.bean.MissionAbout;
import com.sy599.game.util.LogUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MissionAboutDao extends BaseDao {
	private static MissionAboutDao _inst = new MissionAboutDao();

	public static MissionAboutDao getInstance() {
		return _inst;
	}

	protected SqlMapClient getMissionSqlClient() {
		SqlMapClient sqlMapClient = getSqlLoginClient();
		return sqlMapClient;
	}

	@SuppressWarnings("unchecked")
	public  List<MissionAbout> selectMissionStateByUserId(long userId) {
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("userId", userId);
			return (List<MissionAbout>)this.getSqlLoginClient().queryForList("missionAbout.selectMissionStateByUserId", map);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#MissionAboutDao.getGoldSign:", e);
		}
		return null;
	}

	public  MissionAbout selectMSByUserId(long userId) {
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("userId", userId);
			return (MissionAbout)this.getSqlLoginClient().queryForObject("missionAbout.selectMissionStateByUserId", map);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#MissionAboutDao.getGoldSign:", e);
		}
		return null;
	}

	public void insert(MissionAbout ma) {
		try {
			this.getSqlLoginClient().insert("missionAbout.insert", ma);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int update(MissionAbout ma) {
		int update = 0;
		try {
			update = this.getSqlLoginClient().update("missionAbout.update", ma);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#MissionAboutDao.update:", e);
		}
		return update;
	}

	public void batchUpdate(final Map<Long,MissionAbout> map){
		try {
			if (map != null && map.size()>0) {
				SqlMapClient sqlMapClient = getMissionSqlClient();
				sqlMapClient.startBatch();
				for (MissionAbout ma:map.values()) {
					sqlMapClient.update("missionAbout.update", ma);
				}
				sqlMapClient.executeBatch();
				LogUtil.msgLog.info("batchInsert missionAbout success:count={}",map.size());
			}
		} catch (Exception e) {
			LogUtil.dbLog.error("batchInsert missionAbout Exception:"+e.getMessage(),e);
		}
	}

	public void batchInsert(final List<MissionAbout> list){
		try {
			if (list != null && list.size()>0) {
				SqlMapClient sqlMapClient = getMissionSqlClient();
				sqlMapClient.startBatch();
				for (MissionAbout ma:list) {
					sqlMapClient.update("missionAbout.insert", ma);
				}
				sqlMapClient.executeBatch();
				LogUtil.msgLog.info("batchInsert missionAbout success:count={}",list.size());
			}
		} catch (Exception e) {
			LogUtil.dbLog.error("batchInsert missionAbout Exception:"+e.getMessage(),e);
		}
	}

}
