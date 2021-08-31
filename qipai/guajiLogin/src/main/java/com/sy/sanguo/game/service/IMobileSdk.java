package com.sy.sanguo.game.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.dao.OrderDaoImpl;
import com.sy.sanguo.game.dao.OrderValiDaoImpl;
import com.sy.sanguo.game.dao.RoomCardDaoImpl;

public interface IMobileSdk {

	IMobileSdk buildRoomCardDao(RoomCardDaoImpl roomCardDao);

	public String payExecute();

	public String loginExecute();

	/**
	 * 查单并补单
	 */
	void queryOrder();

	boolean checkOrderAgencyId();

	/**
	 * 玩家Info
	 * 
	 * @param userId
	 *            玩家id
	 * @param key
	 *            key
	 * @return
	 */
	public String getInfo(long userId, String key);

	/**
	 * 修改玩家Info
	 * 
	 * @param userId
	 *            玩家id
	 * @param key
	 *            key
	 */
	public String upInfo(long userId, String key);

	/**
	 * 修改玩家info
	 * 
	 * @param userId
	 *            玩家Id
	 * @param key
	 *            KEY
	 * @param value
	 *            value
	 */
	public String upInfo(long userId, String key, String value);

	public String getLoginInfo();

	public String ovali();

	public String common();

	public String callback(HttpServletResponse response);

	public void setRequest(HttpServletRequest request);

	void setResponse(HttpServletResponse response);

	public void setOrderDao(OrderDaoImpl orderDao);

	public void setOrderValiDao(OrderValiDaoImpl orderValiDao);

	public void setPf(String pf);

	/**
	 * common方法
	 * 
	 * @param opt
	 */
	public void setOpt(String opt);

	/**
	 * 创建角色
	 * 
	 * @param regInfo
	 */
	public void createRole(RegInfo regInfo, String info) throws Exception;

	/**
	 * 刷新角色
	 * 
	 * @param regInfo
	 */
	public Map<String,Object> refreshRole(RegInfo regInfo, String info) throws Exception;

	/**
	 * 角色id
	 *
	 */
	public String getSdkId();

	void setExt(String ext);
}
