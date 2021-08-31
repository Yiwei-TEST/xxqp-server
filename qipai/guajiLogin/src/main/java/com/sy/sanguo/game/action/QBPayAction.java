package com.sy.sanguo.game.action;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.sy.sanguo.common.struts.StringResultType;
import org.apache.commons.lang.StringUtils;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.server.GameServerManager;
import com.sy.sanguo.common.server.PayBean;
import com.sy.sanguo.common.server.Server;
import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.util.HttpRequester;
import com.sy.sanguo.common.util.HttpRespons;
import com.sy.sanguo.common.util.MD5Util;
import com.sy.sanguo.common.util.qqbrowser.X5Service;
import com.sy.sanguo.game.bean.OrderInfo;
import com.sy.sanguo.game.bean.OrderValidate;
import com.sy.sanguo.game.dao.OrderDaoImpl;
import com.sy.sanguo.game.dao.OrderValiDaoImpl;

public class QBPayAction extends GameStrutsAction {

	private static String signstr = "1M4C0GIZRLYADQEWBNJF762HPTXUO95S8VK3";

	private static final long serialVersionUID = -3788678829039402523L;
	private OrderDaoImpl orderDao;
	private OrderValiDaoImpl orderValiDao;
	private String result = "";

	public String execute() throws Exception {

		GameBackLogger.SYS_LOG.info("发货回调接口被调用了...");

		String data = getRequest().getParameter("data");
		if (data != null) {
			data = X5Service.x5_quote(data);
		}
		// data =
		// "kPTGY1X55h8aZfgw%2BxUuKS%2B5UvHpy7v9KwBzco%2B6W8o3BBcFCfsX1oMsm2nhh9Nbb%2BEf5kRA1NG%2FtAgv7hQdKFjZ7kBlfHOs5G7PsFo6qWitctaogTYW8PRdFM64ce6MrWvsoyZZk1tBVVco%2BJz%2BdK99KWgTSbqR%2BozLHSuO5EkotLA9k0STGBUdj5OP0SX8Dv1fEYtBJnuO1V0P9AeGkGET9nmB%2BfqLjciMRgmHwVIG1k6AK5d1ox4jJGUbvEZ%2Fzw7eNZNRKJU8XbfYPLxD4DWk%2FHprtLECR%2FmhzaDuduRYlz9cjdE2c04p%2Fyzc5iozYK9QWY7p7Lfgsv%2F2WWwDkg%3D%3D";
		GameBackLogger.SYS_LOG.info("args: data=" + data);

		String reqsig = getRequest().getParameter("reqsig");
		if (reqsig != null) {
			reqsig = X5Service.x5_quote(reqsig);
		}
		// reqsig = "NjUFC%2B9rm6OV0oywNCYrTpom1Lg%3D";
		GameBackLogger.SYS_LOG.info("args: reqsig=" + reqsig);

		JSONObject ret = new JSONObject();
		try {
			ret.put("ret", 0); // 0表示成功
			ret.put("time", System.currentTimeMillis() / 1000);
			ret.put("nonce", X5Service.getRandomString(15));

			boolean is_sign_correct = X5Service.x5_is_correct_of_pay_sign(data, reqsig);
			if (is_sign_correct) {
				JSONObject args = X5Service.x5_get_decoded_args(data);
				if (args == null) {
					// 返回-2， 表示参数格式不对
					ret.put("ret", -2);
				} else {
					String custommeta = args.getString("custommeta");
					int payamount = args.getIntValue("payamount") / 10;
					String orderno = args.getString("orderno");

					OrderValidate ov = orderValiDao.getOne(custommeta);
					if (ov == null || ov.getStatus() != 0) {
						GameBackLogger.SYS_LOG.info("qqbrowser pay ov is null");
						ret.put("ret", -2);
						this.result = ret.toString();
						return StringResultType.RETURN_ATTRIBUTE_NAME;
					}

					PayBean bean = GameServerManager.getInstance().getPayBean(ov.getItem_id());
					if (bean.getAmount() != payamount) {
						GameBackLogger.SYS_LOG.info("qqbrowser pay price error::" + payamount);
						ret.put("ret", -2);
						this.result = ret.toString();
						return StringResultType.RETURN_ATTRIBUTE_NAME;
					}
					String pf = ov.getPf();
					pf += ov.getPay_channel();
//					if (ov.getPay_channel().equals("qqbcom")) {
//						pf = "qqbcom";
//					} else {
//						pf += ov.getPay_channel();
//					}

					int code = newpay(pf, orderno, ov.getFlat_id(), Integer.parseInt(ov.getServer_id()), bean);
					switch (code) {
					case 1:// successed
						break;
					case 2:
						ret.put("ret", -3);
						break;
					case 3:
						ret.put("ret", -4);
						break;
					case 0:// 充值成功
						ret.put("ret", 0);
						break;
					case -1:
						ret.put("ret", -6);
						break;
					}
				}
			} else {
				// 返回-1，表示签名不对
				GameBackLogger.SYS_LOG.info("ERROR(Pay): The sign wasn't correct when checking signing!");
				ret.put("ret", -1);
			}

			this.result = X5Service.x5_pay_response(ret);
			GameBackLogger.SYS_LOG.info("qqbrowser pay result::" + ret.toString());

		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("qqbrowser pay exception::", e);
		}
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	protected int newpay(String platform, String orderid, String uid, int serverId, PayBean bean) {
		int order_amount = bean.getAmount() * 10;
		// 处理uid
		uid += "_" + serverId;
		uid += "_" + platform;
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("order_id", orderid);
			map.put("flat_id", uid);
			OrderInfo info = orderDao.getOne(map);
			// 已经充值
			if (info != null) {
				return 1;
			}
			// 验证游戏服务器
			if (!GameServerManager.getInstance().isCorrectServerId(serverId)) {
				GameBackLogger.SYS_LOG.info("#pay error--serverId " + serverId + " is not correctly");
				return 2;
			}
			// 开始充值流程
			try {
				Server server = GameServerManager.getInstance().getServer(serverId);
				Map<String, String> payParams = new HashMap<String, String>();
				payParams.put("type", "15");
				payParams.put("funcType", "2");
				payParams.put("flatId", uid);
				payParams.put("orderId", orderid);
				payParams.put("itemId", String.valueOf(bean.getId()));
				payParams.put("flatId", uid);
				payParams.put("time", String.valueOf(System.currentTimeMillis()));
				String verifystr = payParams.get("time") + signstr + payParams.get("flatId");
				payParams.put("sign", MD5Util.getStringMD5(verifystr));
				HttpRespons res = HttpRequester.sendPost(server.getHost(), payParams);
				String resp = res.getContent();
				if (!StringUtils.isBlank(resp)) {
					JSONObject json = JSONObject.parseObject(resp);
					int code = json.getIntValue("code");
					if (code == 0) {
						// 写入数据库
						info = new OrderInfo();
						info.setFlat_id(String.valueOf(uid));
						info.setItem_id(bean.getId());
						info.setItem_num(1);
						info.setOrder_amount(order_amount);
						info.setOrder_id(orderid);
						info.setPlatform(platform);
						info.setServer_id(String.valueOf(serverId));
						orderDao.insert(info);
					} else {
						GameBackLogger.SYS_LOG.info("BaseSdk pay code not 0");
						errorlog(platform, orderid, order_amount, uid, "res code:" + code);
						return 3;
					}
				}
			} catch (Exception e) {
				GameBackLogger.SYS_LOG.error("BaseSdk pay error", e);
				exception(platform, orderid, order_amount, uid, e);
				return 3;
			}
			return 0;
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("BaseSdk pay error", e);
			exception(platform, orderid, order_amount, uid, e);
			return 3;
		}
	}

	protected void exception(String platform, String orderid, int amount, String uid, Exception e) {
		StringBuilder sb = new StringBuilder();
		sb.append("#pay error--pf::").append(platform);
		sb.append(" orderId::").append(orderid);
		sb.append(" amount::").append(amount);
		sb.append(" userId::").append(uid);
		GameBackLogger.MONITOR_LOG.error(sb.toString(), e);
	}

	protected void errorlog(String platform, String orderid, int amount, String uid, String msg) {
		StringBuilder sb = new StringBuilder();
		sb.append("#pay error--pf::").append(platform);
		sb.append(" orderId::").append(orderid);
		sb.append(" amount::").append(amount);
		sb.append(" userId::").append(uid);
		sb.append(" msg::").append(msg);
		GameBackLogger.SYS_LOG.info(sb.toString());
	}

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

	public String getResult() {
		return result;
	}

}
