package com.sy.sanguo.game.action;

import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.struts.StringResultType;
import com.sy.sanguo.game.dao.ServerDaoImpl;
import com.sy.sanguo.game.dao.UserDaoImpl;
import com.sy.sanguo.game.service.BaseSdk;
import com.sy.sanguo.game.service.SdkFactory;

public class W360LoginAction extends GameStrutsAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UserDaoImpl userDao;
	private ServerDaoImpl serverDao;
	private String result = "";
	
	
	@Override
	public String execute() throws Exception {
		BaseSdk sdk = (BaseSdk) SdkFactory.getLoginInst("web360", getRequest());
		sdk.setResponse(getResponse());
		sdk.loginH5Redirect();
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}
	

	public String w360login() throws Exception {
		BaseSdk sdk = (BaseSdk) SdkFactory.getLoginInst("web360", getRequest());
		sdk.loginH5Redirect();
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}
	
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
