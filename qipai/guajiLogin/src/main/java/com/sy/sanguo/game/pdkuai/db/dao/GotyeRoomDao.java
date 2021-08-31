package com.sy.sanguo.game.pdkuai.db.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sy.sanguo.game.pdkuai.db.bean.GotyeRoomInfo;
import com.sy.sanguo.game.pdkuai.util.LogUtil;



public class GotyeRoomDao extends BaseDao {
	private static GotyeRoomDao _inst = new GotyeRoomDao();

	public static GotyeRoomDao getInstance() {
		return _inst;
	}

	public void save(GotyeRoomInfo info) {
		try {
			getSql().insert("gotyeroom.addgotyeroom", info);
		} catch (SQLException e) {
			LogUtil.e("#gotyeroom.addgotyeroom:" + info.getRoomId(), e);
		}
	}

	public int del(long roomId) {
		try {
			return getSql().delete("gotyeroom.del", roomId);
		} catch (SQLException e) {
			LogUtil.e("del e:" + roomId, e);
		}
		return 0;
	}

	public int updateUse(long roomId, long tableId) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("roomId", roomId);
			map.put("tableId", tableId);
			return getSql().update("gotyeroom.updateUse", map);
		} catch (SQLException e) {
			LogUtil.e("updUse e:" + roomId, e);
		}
		return 0;
	}

	public int updateDel(long roomId) {
		try {
			return getSql().update("gotyeroom.updateDel", roomId);
		} catch (SQLException e) {
			LogUtil.e("updUse e:" + roomId, e);
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
			list = (List<GotyeRoomInfo>) getSql().queryForList("gotyeroom.getAll", map);
		} catch (SQLException e) {
			LogUtil.e("#gotyeroom.getAll:", e);
		}
		return list;
	}

	public int selectCount() {
		int playerCount = 0;
		try {
			Object result = getSql().queryForObject("gotyeroom.selectCount");
			if (result != null) {
				playerCount = (Integer) result;
			}

		} catch (SQLException e) {
			LogUtil.e("#gotyeroom.selectCount:", e);
		}

		return playerCount;
	}

	// 没有使用的语音房间数量
	public int getNotUseCnt() {
		int notUseCnt = 0;
		
		Object result;
		try {
			result = getSql().queryForObject("gotyeroom.getNotUseCnt");			
			if (result != null) {
				notUseCnt = (Integer) result;
			}
		} catch (SQLException e) {
			LogUtil.e("#gotyeroom.getNotUseCnt err:", e);
		}
		
		return notUseCnt;
	}
	
	// 获取一批可删掉的语音房间
	@SuppressWarnings("unchecked")
	public List<GotyeRoomInfo> canDelGotyeRoom(int amount) {
		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("start", 1);
		map.put("end", amount);
		List<GotyeRoomInfo> list = null;
		try {
			list = (List<GotyeRoomInfo>) getSql().queryForList("gotyeroom.canDelGotyeRoom", map);
		} catch (SQLException e) {
			LogUtil.e("#gotyeroom.canDelGotyeRoom err:", e);
		}
		return list;
	}
	
}
