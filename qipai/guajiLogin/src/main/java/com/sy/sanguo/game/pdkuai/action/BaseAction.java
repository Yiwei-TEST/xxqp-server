package com.sy.sanguo.game.pdkuai.action;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.sy.sanguo.common.util.JacksonUtil;
import com.sy.sanguo.game.dao.UserDaoImpl;
import com.sy.sanguo.game.pdkuai.db.dao.GameUserDao;
import org.apache.commons.lang3.math.NumberUtils;

public abstract class BaseAction {
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected String result;
	protected GameUserDao gameUserDao;
	protected UserDaoImpl userDao;

	public void setUserDao(UserDaoImpl userDao) {
		this.userDao = userDao;
	}
	
	public abstract String execute() throws Exception;

	public void setGameUserDao(GameUserDao gameUserDao) {
		this.gameUserDao = gameUserDao;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	/**
	 * 是否有这个参数
	 * 
	 * @param name
	 * @return
	 */
	public boolean hasParam(String name) {
		return !StringUtils.isBlank(getString(name));
	}

	public int getInt(String name) {
		return NumberUtils.toInt(getString(name),0);
	}

	public int getInt(String name, int def) {
		if (this.hasParam(name)) {
			return getInt(name);
		}
		return def;
	}
	
	public Double getDouble(String name) {
		return Double.parseDouble(getString(name));
	}

	public Double getDouble(String name, Double def) {
		if (this.hasParam(name)) {
			return getDouble(name);
		}
		return def;
	}

	public long getLong(String name, long def) {
		if (this.hasParam(name)) {
			return getLong(name);

		}
		return def;
	}

	public long getLong(String name) {
		return NumberUtils.toLong(getString(name),0);
	}

	public String getString(String name) {
		String value = request.getParameter(name);
		return value;

	}

	/**
	 * 返回错误msg
	 * 
	 * @param code
	 * @param extra
	 * @throws Exception
	 */
	public void writeMsg(int code, Object extra) {
		Map<String, Object> temp = new HashMap<String, Object>(4);
		temp.put("code", code);
		if (extra != null) {
			temp.put("extra", extra);
		}
		this.result = JacksonUtil.writeValueAsString(temp);
	}

	/**
	 * 返回msg
	 * 
	 * @param code
	 * @param map
	 * @throws Exception
	 */
	public void writeMsg(int code, Map<String, Object> map) {
		if (map == null) {
			map = new HashMap<String, Object>();
		}
		map.put("code", code);
		this.result = JacksonUtil.writeValueAsString(map);
	}
}
