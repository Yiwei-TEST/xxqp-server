package com.sy599.game.db.dao;


import com.sy599.game.db.bean.RoomCard;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomCardDao extends BaseDao {
	private RoomCardDao(){

	}
	private static RoomCardDao instance = new RoomCardDao();

	public static RoomCardDao getInstance() {
		return instance;
	}

	public RoomCard queryAgencyInfo(int agencyId) throws SQLException {

		RoomCard agencyInfo = (RoomCard) this.getSqlLoginClient().queryForObject("roomcard.queryAgencyInfo", agencyId);
		
		return agencyInfo;
	}

	public HashMap<String,Object> queryAgencyByAgencyId(int agencyId) throws SQLException {
		return (HashMap<String,Object>) this.getSqlLoginClient().queryForObject("roomcard.queryAgencyByAgencyId", agencyId);
	}
	
	public RoomCard queryAgencyInfoByUserId(int userId) throws SQLException {

		RoomCard agencyInfo = (RoomCard) this.getSqlLoginClient().queryForObject("roomcard.queryAgencyInfoByUserId", userId);
		
		return agencyInfo;
	}

	public List<Map<String,String>> queryMyAgencyByMyUserId(int userId) throws SQLException {

		List<Map<String,String>> agencyInfo =this.getSqlLoginClient().queryForList("roomcard.queryMyAgencyByMyUserId", userId);

		return agencyInfo;
	}

	public List<Map<String,String>> queryMyAgencyByMyAgencyId(int agencyId) throws SQLException {

		List<Map<String,String>> agencyInfo =this.getSqlLoginClient().queryForList("roomcard.queryMyAgencyByMyAgencyId", agencyId);

		return agencyInfo;
	}
	
}
