package com.sy.sanguo.game.action;

import com.sy.sanguo.common.struts.StringResultType;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.pdkuai.db.dao.GameUserDao;
import com.sy.sanguo.game.service.pfs.weixin.Weixin;
import org.apache.commons.lang3.StringUtils;

import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.game.dao.OrderDaoImpl;
import com.sy.sanguo.game.dao.OrderValiDaoImpl;
import com.sy.sanguo.game.dao.UserDaoImpl;
import com.sy.sanguo.game.service.IMobileSdk;
import com.sy.sanguo.game.service.SdkFactory;

public class CommonAction extends GameStrutsAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OrderDaoImpl orderDao;
	private UserDaoImpl userDao;
	private OrderValiDaoImpl orderValiDao;
	private String result = "";

	@Override
	public String execute() throws Exception {
		String pf = this.getString("pf");
		String opt = this.getString("opt");
		IMobileSdk sdk = SdkFactory.getInst(pf, getRequest(), orderDao, orderValiDao, userDao);
		if (sdk == null) {
			return StringResultType.RETURN_ATTRIBUTE_NAME;
		}
		sdk.setOpt(opt);
		this.result = sdk.common();
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	/**
	 * 越南vega
	 * 
	 * @return String
	 * @throws
	 */
	public String vega() {
		this.result = SdkFactory.getInst("vega", getRequest(), orderDao, orderValiDao).common();
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	/**
	 * 微信获取token
	 * 
	 * @return
	 */
	public String getWxAccessToken() {
		String pf = this.getString("pf");
		if (StringUtils.isBlank(pf)) {
			pf = "weixin";
		}
		IMobileSdk sdk = SdkFactory.getInst(pf, getRequest(), orderDao, orderValiDao);
		sdk.setOpt("getAccessToken");
		this.result = sdk.common();
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	public String getInfo() throws Exception{
		String pf = this.getString("pf");
		String flatId = this.getString("flatId");
		String key = this.getString("key");
		IMobileSdk sdk = SdkFactory.getInst(pf, getRequest(), orderDao, orderValiDao, userDao, GameUserDao.getInstance(),null);
		if (sdk != null) {
			RegInfo user = null;
			String info = sdk.loginExecute();

			String uid = sdk.getSdkId();
			// 验证成功
			if (!org.apache.commons.lang.StringUtils.isBlank(uid)) {
				if (sdk instanceof Weixin){
//					com.alibaba.fastjson.JSONObject jsonObject=com.alibaba.fastjson.JSONObject.parseObject(info);
//					String unionId=jsonObject.getString("unionid");
//					if (org.apache.commons.lang3.StringUtils.isNotBlank(unionId)){
//						user = this.userDao.getUser(unionId,"weixin",uid,pf);
//					}else{
						user = this.userDao.getUser(uid, pf);
//					}
				}
			}
			if (user == null){
				user = this.userDao.getUser(flatId, pf);
			}
			if (user != null){
				this.result = sdk.getInfo(user.getUserId(), key);
			}
		}
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	public String upInfo() throws Exception{
		String pf = this.getString("pf");
		String flatId = this.getString("flatId");
		String key = this.getString("key");
		String value = this.getString("value");
		IMobileSdk sdk = SdkFactory.getInst(pf, getRequest(), orderDao, orderValiDao, userDao, GameUserDao.getInstance(),null);
		if (sdk != null) {
			RegInfo user = null;
			String info = sdk.loginExecute();

			String uid = sdk.getSdkId();
			// 验证成功
			if (!org.apache.commons.lang.StringUtils.isBlank(uid)) {
				if (sdk instanceof Weixin){
//					com.alibaba.fastjson.JSONObject jsonObject=com.alibaba.fastjson.JSONObject.parseObject(info);
//					String unionId=jsonObject.getString("unionid");
//					if (org.apache.commons.lang3.StringUtils.isNotBlank(unionId)){
//						user = this.userDao.getUser(unionId,"weixin",uid,pf);
//					}else{
						user = this.userDao.getUser(uid, pf);
//					}
				}
			}
			if (user == null){
				user = this.userDao.getUser(flatId, pf);
			}
			if (user != null){
				this.result = sdk.upInfo(user.getUserId(), key, value);
			}
		}
		return StringResultType.RETURN_ATTRIBUTE_NAME;
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

	public OrderValiDaoImpl getOrderValiDao() {
		return orderValiDao;
	}

	public void setOrderValiDao(OrderValiDaoImpl orderValiDao) {
		this.orderValiDao = orderValiDao;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

}
