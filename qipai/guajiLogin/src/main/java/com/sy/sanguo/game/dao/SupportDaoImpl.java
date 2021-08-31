package com.sy.sanguo.game.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sy.sanguo.common.parent.impl.CommonDaoImpl;
import com.sy.sanguo.game.bean.UserSupport;

public class SupportDaoImpl extends CommonDaoImpl {
	
	public void insert(UserSupport info) throws SQLException{
    	this.getSqlMapClient().insert("userSupport.insert",info);
    }
    
	public UserSupport getOne(Map<String, Object> param) throws SQLException{
		UserSupport supportInfo = null;
		try{
			supportInfo = (UserSupport) this.getSqlMapClient().queryForObject("userSupport.getOne",param);
		}catch(SQLException e){
			throw e;
		}
		return supportInfo;
    }
	
	@SuppressWarnings("unchecked")
	public List<UserSupport> getAll(int status) throws SQLException{
		List<UserSupport> list = null;
		try{
			list = (List<UserSupport>) this.getSqlMapClient().queryForObject("userSupport.getAll",status);
		}catch(SQLException e){
			throw e;
		}
		return list;
	}
	
	public void del(Map<String, Object> param) throws SQLException{
		try{
			this.getSqlMapClient().queryForObject("userSupport.del",param);
		}catch(SQLException e){
			throw e;
		}
	}
	
	public void update(UserSupport info) throws SQLException{
		try{
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("status", info.getStatus());
			param.put("roleUid", info.getRoleUid());
			param.put("serverId", info.getServerId());
			param.put("createTime", info.getCreateTime());
			this.getSqlMapClient().queryForObject("userSupport.update",param);
		}catch(SQLException e){
			throw e;
		}
	}
}
