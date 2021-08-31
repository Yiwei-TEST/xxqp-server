package com.sy.sanguo.game.action;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import com.sy.sanguo.common.struts.StringResultType;

import com.sy.sanguo.game.dao.UserDaoImpl;
import org.apache.commons.lang.StringUtils;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.server.GameServerManager;
import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.util.JacksonUtil;
import com.sy.sanguo.common.util.LoginCacheContext;
import com.sy.sanguo.common.util.MD5Util;
import com.sy.sanguo.common.util.MathUtil;
import com.sy.sanguo.common.util.huawei.RSA;
import com.sy.sanguo.game.bean.OrderValidate;
import com.sy.sanguo.game.bean.UserSupport;
import com.sy.sanguo.game.dao.OrderDaoImpl;
import com.sy.sanguo.game.dao.OrderValiDaoImpl;
import com.sy.sanguo.game.dao.SupportDaoImpl;
import com.sy.sanguo.game.service.SdkFactory;
import com.sy599.sanguo.util.TimeUtil;

public class SupportAction extends GameStrutsAction {

	private static final long serialVersionUID = 1724284725591850534L;
	private SupportDaoImpl supportDao;
	private OrderValiDaoImpl orderValiDao;
	private OrderDaoImpl orderDao;
	private UserDaoImpl userDao;
	private String result = "";

	public String commit() {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			String name = getRequest().getParameter("name");
			long uid = Long.valueOf(getRequest().getParameter("uid"));
			int sid = Integer.valueOf(getRequest().getParameter("sid"));
			int type = Integer.valueOf(getRequest().getParameter("type"));
			String content = getRequest().getParameter("content");
			long time = Long.valueOf(getRequest().getParameter("t"));
			String pf = getRequest().getParameter("pf");
			String sign = getRequest().getParameter("k");
			String secret = "mwFLeKLzNoL46dDn0vE2";

			// 校验参数

			StringBuilder md5 = new StringBuilder();
			md5.append(URLEncoder.encode(name, "UTF-8"));
			md5.append(uid);
			md5.append(sid);
			md5.append(type);
			md5.append(URLEncoder.encode(content, "UTF-8"));
			md5.append(time);
			md5.append(pf);
			md5.append(secret);
			String md5str = MD5Util.getStringMD5(md5.toString());
			if (md5str.equals(sign)) {
				long last_time = LoginCacheContext.getSupportTime(uid);
				long now = TimeUtil.currentTimeMillis();
				long diff = 15 * 1000;
				if (now - last_time < diff) {
					result.put("code", 1);
					result.put("msg", "time error");
				} else {
					UserSupport support = new UserSupport();
					support.setRoleName(name);
					support.setRoleUid(uid);
					support.setPf(pf);
					support.setServerId(sid);
					support.setStatus(0);
					support.setType(type);
					support.setContent(content);
					support.setCreateTime(now);
					support.setReply("");
					supportDao.insert(support);
					result.put("code", 0);
					result.put("next_time", now + diff);
					LoginCacheContext.setSupportTime(uid, now);
				}
			} else {
				result.put("code", 2);
				result.put("msg", "sig error");
			}
			this.result = JacksonUtil.writeValueAsString(result);
		} catch (Exception e) {
			result.put("code", 999);
			result.put("msg", e.getMessage());
			GameBackLogger.SYS_LOG.error("support.exception", e);
		}
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	/**
	 * 生成联通支付所需要的订单号
	 * 
	 * @return
	 */
	public String ovali() {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			String flat_id = getRequest().getParameter("flat_id");
			String server_id = getRequest().getParameter("server_id");
			String pf = getRequest().getParameter("pf");
			String pay_channel = getRequest().getParameter("pay_channel");
			String channel_id = getRequest().getParameter("channel_id");
			String sign = getRequest().getParameter("k");
			String secret = "mwFLeKLzNoL46dDn0vE2";

			StringBuilder md5 = new StringBuilder();
			md5.append(flat_id);
			md5.append(server_id);
			md5.append(pf);
			md5.append(pay_channel);
			md5.append(channel_id);
			md5.append(secret);
			if (!StringUtils.isBlank(flat_id) && GameServerManager.getInstance().isCorrectServerId(Integer.valueOf(server_id)) && !StringUtils.isBlank(pf) && !StringUtils.isBlank(pay_channel)
					&& !StringUtils.isBlank(sign) && !StringUtils.isBlank(channel_id) && MD5Util.getStringMD5(md5.toString()).equals(sign)) {
				long now = TimeUtil.currentTimeMillis();
				long time = now / 1000 + MathUtil.mt_rand(100, 999);
				String order_id = now + flat_id + server_id + pf + MathUtil.mt_rand(1000, 9999);
				order_id = MD5Util.getStringMD5(order_id).substring(8, 24);
				order_id += Long.toHexString(time);
				OrderValidate ov = new OrderValidate();
				ov.setFlat_id(flat_id);
				ov.setOrder_id(order_id);
				ov.setServer_id(server_id);
				if (pf.equals("soumi"))
					pf += channel_id;
				ov.setPf(pf);
				ov.setPay_channel(pay_channel);
				ov.setAmount(0);
				orderValiDao.insert(ov);
				result.put("code", 0);
				result.put("order_id", order_id);
			} else {
				result.put("code", 1);
				result.put("msg", "param error or md5 error");
			}
		} catch (Exception e) {
			result.put("code", 999);
			result.put("msg", e.getMessage());
			GameBackLogger.SYS_LOG.error("ovali.exception", e);
		}
		this.result = JacksonUtil.writeValueAsString(result);
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}



	public String pipaKey() {
		StringBuilder sb = new StringBuilder();
		sb.append(getRequest().getParameter("merchantId")).append(getRequest().getParameter("merchantAppId")).append(getRequest().getParameter("appId")).append(getRequest().getParameter("payerId"))
				.append(getRequest().getParameter("exOrderNo")).append(getRequest().getParameter("subject")).append(getRequest().getParameter("price")).append(getRequest().getParameter("extraParam"))
				.append("78c872dfcb3d612e15e4cd9e5c2ece74");
		this.result = MD5Util.getStringMD5(sb.toString());
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	public String huaweiSign() {
		this.result = "";
		try {
			String noSign = getRequest().getParameter("noSign");
			noSign = URLDecoder.decode(noSign, "utf-8");
			// 华为私钥
			String privateKey = "MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAlhM0pP4LnhHr32+oj//+66DMjZzOt0KDD0vOZCYzJsvnk/QWcRfGA2NJ0XE0gplgUVKY+HPz93dnEx5ebE6hvwIDAQABAkBkAOJIu7zUFU8fMdGaO1UMgLct+nrJJXod/D7I/6eARsPsoVCEbS+Ga6/+1Xz4iQxQ4FvpzuSiQe0pZ2WGCUzRAiEA0hBjjizk4lQgUanctXgxBu3/NBp30suUpC3z5fcYj8cCIQC25JFaI8WZKcPEX1Sx+z4SvUw/2/gwDd/2cZxRL4lOSQIgG6YQlHwQPEH6ZwVGiZQiT9kvh/ob32DwVZO+0Hvvfa0CIQCtcY+ioz/2I5RjO2DvtOtGDD0uZmY09EOLzGxI1cLp+QIgM3ZIPL8gOsFCfixlnVoOdoQN3AKTE48G8K26KvmCsrg=";

			if (!StringUtils.isBlank(noSign)) {
				this.result = RSA.sign(noSign, privateKey);
			}
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("support.huaweiSign.exception", e);
		}
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	public void webwayxOauth() {
		try {
			String appid = "8716935";
			String secret = "b9a4e84b73a3386c85ee77bf90e86f0f";
			String state = MD5Util.getStringMD5(appid + "wayx" + secret);
			StringBuilder url = new StringBuilder();
			url.append("http://browser-api.ilovegame.net/oauth2/authorize");
			url.append("?client_id=" + appid);
			url.append("&redirect_uri=" + URLEncoder.encode("http://login.gjweb.kx7p.com/guajih5/wayx.html", "UTF-8"));
			url.append("&response_type=code");
			url.append("&state=" + state);
			this.getResponse().sendRedirect(url.toString());
		} catch (Exception e) {
		}
	}

	/**
	 * 下验证订单(通用)
	 * 
	 * @return
	 */
	public String ovali_com() {
		String pf = getRequest().getParameter("p");
		String payType = getRequest().getParameter("payType");
		if (!StringUtils.isBlank(payType) && !pf.equals(payType)) {
			// 跟pf接的不同的支付方式
			pf = "upstream";
		}
		this.result = SdkFactory.getInst(pf, getRequest(), orderDao, orderValiDao,userDao).ovali();
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	/********************************** QQ浏览器start ***************************************/

	public String testlog() {
		GameBackLogger.SYS_LOG.info("testlog::" + JacksonUtil.writeValueAsString(getRequest().getParameterMap()));
		this.result = "";
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	/********************************** QQ浏览器end ***************************************/

	public OrderValiDaoImpl getOrderValiDao() {
		return orderValiDao;
	}

	public void setOrderValiDao(OrderValiDaoImpl orderValiDao) {
		this.orderValiDao = orderValiDao;
	}

	public SupportDaoImpl getSupportDao() {
		return supportDao;
	}

	public void setSupportDao(SupportDaoImpl supportDao) {
		this.supportDao = supportDao;
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

	public void setUserDao(UserDaoImpl userDao) {
		this.userDao = userDao;
	}

	public static void main(String[] args) {
		String secret = "mwFLeKLzNoL46dDn0vE2";

		StringBuilder md5 = new StringBuilder();
		md5.append(1020310203);
		md5.append(998);
		md5.append("yiwan");
		md5.append(2);
		md5.append(secret);

		System.out.println(MD5Util.getStringMD5(md5.toString()));
	}
}
