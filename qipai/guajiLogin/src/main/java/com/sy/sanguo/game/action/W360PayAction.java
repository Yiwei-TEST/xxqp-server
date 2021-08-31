package com.sy.sanguo.game.action;

import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.struts.StringResultType;
import com.sy.sanguo.game.dao.OrderDaoImpl;
import com.sy.sanguo.game.dao.OrderValiDaoImpl;
import com.sy.sanguo.game.service.SdkFactory;

public class W360PayAction extends GameStrutsAction{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OrderDaoImpl orderDao;
	private OrderValiDaoImpl orderValiDao;
	private String result = "";
	
	@Override
	public String execute() throws Exception {
		this.result = SdkFactory.getInst("web360", getRequest(), orderDao, orderValiDao,null,null,null).payExecute();
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	public String getResult() {
		return result;
	}

	public OrderDaoImpl getOrderDao() {
		return orderDao;
	}

	public void setOrderDao(OrderDaoImpl orderDao) {
		this.orderDao = orderDao;
	}

	public OrderValiDaoImpl getOrderValiDao() {
		return orderValiDao;
	}

	public void setOrderValiDao(OrderValiDaoImpl orderValiDao) {
		this.orderValiDao = orderValiDao;
	}
}
