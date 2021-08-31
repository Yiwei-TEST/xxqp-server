package com.sy.sanguo.game.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sy.sanguo.game.bean.UserLucky;
import com.sy.sanguo.game.pdkuai.db.dao.BaseDao;
import com.sy.sanguo.game.pdkuai.util.LogUtil;

public class LuckyDaoImpl extends BaseDao {
	private static LuckyDaoImpl _inst =new LuckyDaoImpl();
	public static LuckyDaoImpl getInstance(){
		return _inst;
	}

	public UserLucky getUserLucky(Long userId){
		try {
			return (UserLucky) this.getSql().queryForObject("lucky.getUserLucky", userId);
		} catch (SQLException e) {
			LogUtil.e("getUserLucky err:", e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<UserLucky> getInviteeInfo(Long userId){		
		try {
			return (List<UserLucky>)this.getSql().queryForList("lucky.getInviteeInfo", userId);
		} catch (SQLException e) {
			LogUtil.e("getInviteeInfo err:", e);
		}
		return null;
		
	}

	public void insertUserLucky(UserLucky userLucky) {
		try {
			this.getSql().insert("lucky.insertUserLucky", userLucky);
		} catch (SQLException e) {
			LogUtil.e("insertUserLucky err:", e);
		}
		
		
	}

	public int updateUserLucky(long invitorId, Map<String, Object> paraMap) {
		paraMap.put("userId", invitorId);
		try {
			return this.getSql().update("lucky.updateUserLucky", paraMap);
		} catch (SQLException e) {
			LogUtil.e("updateUserLucky err:", e);
		}	
		return 0;
	}

	@SuppressWarnings({ "unchecked"})
	public List<HashMap<String, Object>> getRankingList(int number){		
		try {
			return this.getSql().queryForList("lucky.getRankingList", number);
		} catch (SQLException e) {
			LogUtil.e("getRankingList err:", e);
		}
		return null;
	}
	
}
