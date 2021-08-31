package com.sy.sanguo.game.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sy.sanguo.common.parent.impl.CommonDaoImpl;
import com.sy.sanguo.game.bean.RoomCard;

public class RoomCardDaoImpl extends CommonDaoImpl {

	public RoomCard queryAgencyInfo(int agencyId) throws SQLException {

		RoomCard agencyInfo = (RoomCard) this.getSqlMapClient().queryForObject("roomcard.queryAgencyInfo", agencyId);
		
		return agencyInfo;
	}

	public HashMap<String,Object> queryAgencyByAgencyId(int agencyId) throws SQLException {
		return (HashMap<String,Object>) this.getSqlMapClient().queryForObject("roomcard.queryAgencyByAgencyId", agencyId);
	}
	
	public RoomCard queryAgencyInfoByUserId(int userId) throws SQLException {

		RoomCard agencyInfo = (RoomCard) this.getSqlMapClient().queryForObject("roomcard.queryAgencyInfoByUserId", userId);
		
		return agencyInfo;
	}

	public List<Map<String,String>> queryMyAgencyByMyUserId(int userId) throws SQLException {

		List<Map<String,String>> agencyInfo =this.getSqlMapClient().queryForList("roomcard.queryMyAgencyByMyUserId", userId);

		return agencyInfo;
	}

	public List<Map<String,String>> queryMyAgencyByMyAgencyId(int agencyId) throws SQLException {

		List<Map<String,String>> agencyInfo =this.getSqlMapClient().queryForList("roomcard.queryMyAgencyByMyAgencyId", agencyId);

		return agencyInfo;
	}
	
}
