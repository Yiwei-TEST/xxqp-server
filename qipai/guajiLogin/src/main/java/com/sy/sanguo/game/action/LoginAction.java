package com.sy.sanguo.game.action;

import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.game.dao.ServerDaoImpl;
import com.sy.sanguo.game.dao.UserDaoImpl;

public class LoginAction extends GameStrutsAction {
	private static final long serialVersionUID = 9102807327813284888L;
	private UserDaoImpl userDao;
	private ServerDaoImpl serverDao;
	private String result = "";

	public UserDaoImpl getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDaoImpl userDao) {
		this.userDao = userDao;
	}

	public String getResult() {
		return result;
	}

	public void setServerDao(ServerDaoImpl serverDao) {
		this.serverDao = serverDao;
	}

	public ServerDaoImpl getServerDao() {
		return serverDao;
	}
}
