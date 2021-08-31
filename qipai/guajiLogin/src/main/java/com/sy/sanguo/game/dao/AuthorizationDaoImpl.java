package com.sy.sanguo.game.dao;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.game.bean.WeiXinAuthorization;
import com.sy.sanguo.game.pdkuai.db.dao.BaseDao;

public class AuthorizationDaoImpl extends BaseDao {
	private static AuthorizationDaoImpl _inst = new AuthorizationDaoImpl();
	public static AuthorizationDaoImpl getInstance() {
		return _inst;
	}

	/**
	 * 创建微信授权记录
	 * 
	 * @param unionId
	 * @param agencyId
	 * @throws SQLException
	 */
	public void addWeiXinAuthorization(String unionId, int agencyId) throws SQLException {

		Timestamp createTime = new Timestamp(System.currentTimeMillis());
		WeiXinAuthorization wx = new WeiXinAuthorization();
		wx.setUnionId(unionId);
		wx.setAgencyId(agencyId);
		wx.setCreateTime(createTime);
		wx.setInviterId(0);
		wx.setInviterTime(null);

		this.getSql().insert("authorization.addWeiXinAuthorization", wx);
	}

	/**
	 * 创建微信邀请记录
	 * 
	 * @param unionId
	 * @param userId
	 * @throws SQLException
	 */
	public void addWeiXinInviter(String unionId, long userId) throws SQLException {
		Timestamp createTime = new Timestamp(System.currentTimeMillis());
		WeiXinAuthorization wx = new WeiXinAuthorization();
		wx.setUnionId(unionId);
		wx.setAgencyId(0);
		wx.setCreateTime(null);
		wx.setInviterId(userId);
		wx.setInviterTime(createTime);

		this.getSql().insert("authorization.addWeiXinAuthorization", wx);
	}
	
	/**
	 * 查询微信授权记录
	 * 
	 * @param unionId
	 * @return
	 * @throws SQLException
	 */
	public WeiXinAuthorization queryWeiXinAuthorization(String unionId) throws SQLException {

		Map<String, String> sqlMap = new HashMap<String, String>();
		sqlMap.put("unionId", unionId);
		WeiXinAuthorization wx = (WeiXinAuthorization) this.getSql().queryForObject("authorization.queryWeiXinAuthorization", sqlMap);

		return wx;
	}
	
	public int updateAuthorization(Map<String, Object> sqlMap) {
		
		try {
			return (Integer) this.getSql().update("authorization.updateAuthorization", sqlMap);
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("authorization.updateAuthorization err:" + sqlMap, e);
		}
		
		return 0;
	}

}
