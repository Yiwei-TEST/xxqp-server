package com.sy.sanguo.common.util;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import com.sy.sanguo.game.pdkuai.user.Manager;
import org.apache.commons.lang3.StringUtils;

import com.sy.sanguo.game.dao.OrderDaoImpl;
import com.sy.sanguo.game.dao.SupportDaoImpl;
import com.sy.sanguo.game.dao.UserDaoImpl;
import com.sy.sanguo.game.service.SysInfManager;
import com.sy.sanguo.common.server.GameServerManager;
import com.sy.sanguo.common.util.JacksonUtil;
import org.apache.commons.lang3.math.NumberUtils;

/** 缓存全局的数据 */
public class LoginCacheContext {
	private UserDaoImpl userDao;
	private OrderDaoImpl orderDao;
	private SupportDaoImpl supportDao;
	private static ConcurrentHashMap<Long, Long> supportTimeMap = new ConcurrentHashMap<Long, Long>();
	public static String gameloginkey;
	public static boolean isDebug;
	public static boolean isMemcache;
	public static String memcacheServer;
	public static String pf;
	public static String callbackUrl;
	public static String gameName;
	public static int userMaxCount;
	public static String headImgUrl;

	public static String minPlayerId;
	public static String withFilterUserId;
	public static String tipWithCard;

    public static String getTipWithCard() {
		return tipWithCard;
	}

	public void setTipWithCard(String tipWithCard) {
		LoginCacheContext.tipWithCard = tipWithCard;
	}

	public static String getMinPlayerId() {
		return minPlayerId;
	}

	public void setMinPlayerId(String minPlayerId) {
		LoginCacheContext.minPlayerId = minPlayerId;
		if (NumberUtils.isDigits(minPlayerId)){
			Manager.min_player_id = Long.parseLong(minPlayerId);
		}
	}

	public static String getWithFilterUserId() {
		return withFilterUserId;
	}

	public void setWithFilterUserId(String withFilterUserId) {
		LoginCacheContext.withFilterUserId = withFilterUserId;
		if ("1".equals(withFilterUserId)||"true".equalsIgnoreCase(withFilterUserId)){
			Manager.withFilterUserId = true;
		}
	}

	public static void setSupportTime(long uid, long time) {
		supportTimeMap.put(uid, time);
	}

	public static long getSupportTime(long uid) {
		return supportTimeMap.containsKey(uid) ? supportTimeMap.get(uid) : 0;
	}

	public String getPf() {
		return pf;
	}

	public void setPf(String pf) {
		LoginCacheContext.pf = pf;
	}

	public String getMemcacheServer() {
		return memcacheServer;
	}

	public void setMemcacheServer(String memcacheServer) {
		LoginCacheContext.memcacheServer = memcacheServer;
	}

	public boolean isMemcache() {
		return isMemcache;
	}

	public void setIsMemcache(String isMemcache) {
		LoginCacheContext.isMemcache = Boolean.parseBoolean(isMemcache);
	}

	public void initData() throws SQLException {
		SysInfManager.getInstance();
		GameServerManager.getInstance();
		// 初始化JacksonUtil
		JacksonUtil.writeValueAsString("init");
	}

	public SupportDaoImpl getSupportDao() {
		return supportDao;
	}

	public void setSupportDao(SupportDaoImpl supportDao) {
		this.supportDao = supportDao;
	}

	public OrderDaoImpl getOrderDao() {
		return orderDao;
	}

	public void setOrderDao(OrderDaoImpl orderDao) {
		this.orderDao = orderDao;
	}

	public UserDaoImpl getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDaoImpl userDao) {
		this.userDao = userDao;
	}

	public boolean isDebug() {
		return isDebug;
	}

	public void setIsDebug(String isDebug) {
		LoginCacheContext.isDebug = Boolean.parseBoolean(isDebug);
	}

	public String getGameloginkey() {
		return gameloginkey;
	}

	public void setGameloginkey(String gameloginkey) {
		LoginCacheContext.gameloginkey = gameloginkey;
	}

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public void setCallbackUrl(String callbackUrl) {
		LoginCacheContext.callbackUrl = callbackUrl;
	}

	public void setGameName(String gameName) {
		LoginCacheContext.gameName = gameName;
	}

	public String getGameName() {
		return gameName;
	}

	public int getUserMaxCount() {
		return userMaxCount;
	}

	public void setUserMaxCount(int userMaxCount) {
		LoginCacheContext.userMaxCount = userMaxCount;
	}

	public static boolean isTestServer() {
		return !StringUtils.isBlank(callbackUrl) && callbackUrl.indexOf("testxsg.sy599.com") > -1;
	}

	public String getHeadImgUrl() {
		return headImgUrl;
	}

	public void setHeadImgUrl(String headImgUrl) {
		LoginCacheContext.headImgUrl = headImgUrl;
	}

}
