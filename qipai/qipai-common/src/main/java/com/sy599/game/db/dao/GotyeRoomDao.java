package com.sy599.game.db.dao;

import com.sy599.game.db.bean.GotyeRoomInfo;
import com.sy599.game.util.LogUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GotyeRoomDao extends BaseDao {
	private static GotyeRoomDao _inst = new GotyeRoomDao();

	public static GotyeRoomDao getInstance() {
		return _inst;
	}

	public void save(GotyeRoomInfo info) {
		try {
			getSqlClient().insert("gotyeroom.addgotyeroom", info);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#gotyeroom.addgotyeroom:" + info.getRoomId(), e);
		}
	}

	public int del(long roomId) {
		try {
			return getSqlClient().delete("gotyeroom.del", roomId);
		} catch (SQLException e) {
			LogUtil.dbLog.error("del e:" + roomId, e);
		}
		return 0;
	}

	public int updateUse(long roomId, long tableId) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("roomId", roomId);
			map.put("tableId", tableId);
			return getSqlClient().update("gotyeroom.updateUse", map);
		} catch (SQLException e) {
			LogUtil.dbLog.error("updUse e:" + roomId, e);
		}
		return 0;
	}

	public int updateDel(long roomId) {
		try {
			return getSqlClient().update("gotyeroom.updateDel", roomId);
		} catch (SQLException e) {
			LogUtil.dbLog.error("updUse e:" + roomId, e);
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
	public List<GotyeRoomInfo> getAll(int start, int end) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("start", start);
		map.put("end", end);
		List<GotyeRoomInfo> list = null;
		try {
			list = (List<GotyeRoomInfo>) getSqlClient().queryForList("gotyeroom.getAll", map);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#gotyeroom.getAll:", e);
		}
		return list;
	}

	public int selectCount() {
		int playerCount = 0;
		try {
			Object result = getSqlClient().queryForObject("gotyeroom.selectCount");
			if (result != null) {
				playerCount = (Integer) result;
			}

		} catch (SQLException e) {
			LogUtil.dbLog.error("#gotyeroom.selectCount:", e);
		}

		return playerCount;
	}
}
