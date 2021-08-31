package com.sy.sanguo.game.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.sy.sanguo.common.parent.impl.CommonDaoImpl;
import com.sy.sanguo.game.bean.Room;
import com.sy.sanguo.game.pdkuai.db.dao.BaseDao;
import com.sy.sanguo.game.pdkuai.util.LogUtil;

/**
 * @author libin
 * @date 2016年8月31日 下午3:46:06
 */
public class RoomDaoImpl extends BaseDao {

	private static RoomDaoImpl inst = new RoomDaoImpl();

	public static RoomDaoImpl getInstance() {
		return inst;
	}

	public void addRoom(Room room) throws SQLException {
		this.getSql().insert("room.addRoom", room);
	}

	public long queryRoomUsed() throws SQLException {
		try {
			long roomNo = (long) this.getSql().queryForObject("room.queryRoomUsed");
			return roomNo;
		} catch (SQLException e) {
			throw e;
		}
	}

	public Room queryRoom(long roomId) throws SQLException {
		try {
			Room room = (Room) this.getSql().queryForObject("room.queryRoom", roomId);
			return room;
		} catch (SQLException e) {
			throw e;
		}
	}

	public Room queryRoomByRoomId(long roomId) throws SQLException {
		try {
			Room room = (Room) this.getSql().queryForObject("room.queryRoomByRoomId", roomId);
			return room;
		} catch (SQLException e) {
			throw e;
		}
	}


	public void updateRoomCreateTime() throws SQLException {
		try {
			this.getSql().update("room.updateRoomCreateTime");
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 回收房间
	 * @param date
	 * @throws SQLException
	 */
	public int recoverRoom(String date) throws SQLException {
		try {
			Map<String,Object> map=new HashMap<>();
			map.put("createdTime",date);
			return this.getSql().update("room.recoverRoom",map);
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 清理使用过的房间
	 *
	 * @param roomId
	 * @return
	 */
	public int clearRoom(long roomId) {
		Map<String, Object> params = new HashMap<>();
		params.put("used", 0);
		params.put("serverId", 0);
		params.put("roomId", roomId);
		try {
			return getSql().update("room.updateRoom", params);
		} catch (Exception e) {
			LogUtil.e("updateRoom:"+e.getMessage(), e);
		}
		return 0;
	}

}
