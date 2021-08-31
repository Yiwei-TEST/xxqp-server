package com.sy.sanguo.game.action;

import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.struts.StringResultType;
import com.sy.sanguo.common.util.OutputUtil;
import com.sy.sanguo.game.dao.OrderDaoImpl;
import com.sy.sanguo.game.dao.OrderValiDaoImpl;
import com.sy.sanguo.game.dao.RoomCardDaoImpl;
import com.sy.sanguo.game.dao.UserDaoImpl;
import com.sy.sanguo.game.service.IMobileSdk;
import com.sy.sanguo.game.service.SdkFactory;
import com.sy.sanguo.game.service.pfs.Upstream;
import org.apache.commons.lang3.StringUtils;

public class PayAction extends GameStrutsAction {

	private static final long serialVersionUID = -4293687737035405944L;
	private UserDaoImpl userDao;
	private OrderDaoImpl orderDao;
	private OrderValiDaoImpl orderValiDao;
	private RoomCardDaoImpl roomCardDao;
	private String result = "";

	public void setRoomCardDao(RoomCardDaoImpl roomCardDao) {
		this.roomCardDao = roomCardDao;
	}

	/**
	 * 微信u2
	 * 
	 * @return
	 */
	public String u2() {
		this.result = upstreamPay("u2");
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	/**
	 * 自有平台u1
	 * 
	 * @return
	 */
	public String u1() {
		this.result = upstreamPay("u1");
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	/**
	 * 运营平台发送房卡
	 * 
	 * @return
	 */
	public String yypt() {
		this.result = SdkFactory.getInst("yypt", getRequest(), orderDao, orderValiDao, userDao).buildRoomCardDao(roomCardDao).payExecute();
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	/**
	 * weixin支付
	 * 
	 * @return
	 */
	public String weixin() {
		this.result = SdkFactory.getInst("weixin", getRequest(), orderDao, orderValiDao, userDao).buildRoomCardDao(roomCardDao).payExecute();
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	/**
	 * tonglian支付
	 * 
	 * @return
	 */
	public String tonglian() {
		this.result = SdkFactory.getInst("tonglian", getRequest(), orderDao, orderValiDao, userDao).buildRoomCardDao(roomCardDao).payExecute();
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	/**
	 * 微富通支付
	 * 
	 * @return
	 */
	public String weifutong() {
		this.result = SdkFactory.getInst("futong", getRequest(), orderDao, orderValiDao, userDao).buildRoomCardDao(roomCardDao).payExecute();
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	/**
	 * 查单并补单
	 */
	public void queryOrder(){
		String pf = getString("pf");
		IMobileSdk sdk;
		if (StringUtils.isBlank(pf) || (sdk=SdkFactory.getInst(pf, getRequest(), orderDao, orderValiDao, userDao).buildRoomCardDao(roomCardDao))==null){
			OutputUtil.output(-1,"pf is error:"+pf,getRequest(),getResponse(),false);
		}else{
			sdk.setResponse(getResponse());
			sdk.queryOrder();
		}
	}

	/**
	 * h5微富通支付
	 * 
	 * @return
	 */
	public String webweifutong() {
		this.result = SdkFactory.getInst("webfutong", getRequest(), orderDao, orderValiDao, userDao).buildRoomCardDao(roomCardDao).payExecute();
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	/**
	 * h5禅游支付
	 *
	 * @return
	 */
	public String webchanyou() {
		IMobileSdk inst = SdkFactory.getInst("webchanyou", getRequest(), orderDao, orderValiDao, userDao).buildRoomCardDao(roomCardDao);
		inst.setResponse(getResponse());
		this.result = inst.payExecute();
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	/**
	 * 星富通支付
	 *
	 * @return
	 */
	public String xftpay() {
		IMobileSdk inst = SdkFactory.getInst("xftpay", getRequest(), orderDao, orderValiDao, userDao).buildRoomCardDao(roomCardDao);
		inst.setResponse(getResponse());
		this.result = inst.payExecute();
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	/**
	 * h5掌宜付支付
	 *
	 * @return
	 */
	public String webzyf() {
		IMobileSdk inst = SdkFactory.getInst("webzyf", getRequest(), orderDao, orderValiDao, userDao).buildRoomCardDao(roomCardDao);
		inst.setResponse(getResponse());
		this.result = inst.payExecute();
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}
	
	/**
	 * h5掌宜付支付
	 *
	 * @return
	 */
	public String h5zyf() {
		IMobileSdk inst = SdkFactory.getInst("h5zyf", getRequest(), orderDao, orderValiDao, userDao).buildRoomCardDao(roomCardDao);
		inst.setResponse(getResponse());
		this.result = inst.payExecute();
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	/**
	 * h5 unpay
	 *
	 * @return
	 */
	public String webunpay() {
		IMobileSdk inst = SdkFactory.getInst("webunpay", getRequest(), orderDao, orderValiDao, userDao).buildRoomCardDao(roomCardDao);
		inst.setResponse(getResponse());
		this.result = inst.payExecute();
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	/**
	 * h5 wmpay
	 *
	 * @return
	 */
	public String webwmpay() {
		IMobileSdk inst = SdkFactory.getInst("webwmpay", getRequest(), orderDao, orderValiDao, userDao).buildRoomCardDao(roomCardDao);
		inst.setResponse(getResponse());
		this.result = inst.payExecute();
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	/**
	 * apple支付
	 * 
	 * @return
	 */
	public String apple() {
		this.result = SdkFactory.getInst("apple", getRequest(), orderDao, orderValiDao, userDao).buildRoomCardDao(roomCardDao).payExecute();
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	private String upstreamPay(String channel) {
		return ((Upstream) SdkFactory.getInst("upstream", getRequest(), orderDao, orderValiDao).buildRoomCardDao(roomCardDao)).payExecute(channel);
	}

	/**
	 * 益玩奇迹大天使异步回调充值
	 * 
	 * @return String
	 * @throws
	 */
	public String aibei() {
		this.result = SdkFactory.getInst("aibei", getRequest(), orderDao, orderValiDao, userDao).buildRoomCardDao(roomCardDao).payExecute();
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	@Override
	public String execute() throws Exception {
		// 多酷,华为的回调地址不能包含!分隔符,通过回调参数来区分是否为多酷
		return super.execute();

	}

	// ///////////////////////////////////////////////////////

	public OrderValiDaoImpl getOrderValiDao() {
		return orderValiDao;
	}

	public void setOrderValiDao(OrderValiDaoImpl orderValiDao) {
		this.orderValiDao = orderValiDao;
	}

	public OrderDaoImpl getOrderDao() {
		return orderDao;
	}

	public void setOrderDao(OrderDaoImpl orderDao) {
		this.orderDao = orderDao;
	}

	public void setUserDao(UserDaoImpl userDao) {
		this.userDao = userDao;
	}

	public String getResult() {
		return result;
	}
}
