package com.sy.sanguo.game.action;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONObject;
import com.sy.sanguo.common.struts.StringResultType;
import org.apache.commons.lang.StringUtils;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.server.GameServerManager;
import com.sy.sanguo.common.server.PayBean;
import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.util.qqbrowser.X5Service;
import com.sy.sanguo.game.bean.OrderValidate;
import com.sy.sanguo.game.dao.OrderValiDaoImpl;
import com.sy599.sanguo.util.TimeUtil;

public class QBPriceAction extends GameStrutsAction {

	private static final long serialVersionUID = 3394669673223627455L;

	private OrderValiDaoImpl orderValiDao;
	private String result = "";

	private boolean checkCustommeta(String string) {
		String reg = "^[a-zA-Z0-9_]*$";
		Pattern p1 = Pattern.compile(reg);
		Matcher mat = p1.matcher(string);
		return mat.find();
	}

	public String execute() throws Exception {

		GameBackLogger.SYS_LOG.info("批价回调接口被调用了...");

		String data = getRequest().getParameter("data");
		if (data != null) {
			data = X5Service.x5_quote(data);
		}
		// data =
		// "K0hNxW046polkGzuQ4j7JLOyH%2B2SkMQvuEZ28ny11SFgTjQkmWhjX9AHN0nbPD0WxB8Zkj14%2B2yIXEkcp4%2FdGKT5gaImDUUIXDkF7WVVQragsQALRTrx0z0wdkQiqkVFdokPtBpJcotIeAZBl1r1SEHxAPLvLbGeKnJxjJawK98tFMxhtaCspaPfTlLXbd3Sfi436fr%2BupHmNtiHw2NfE%2FKiKtj2zkz8k1pV4oLf%2Fs1m5dEJa2d2b2m%2FQ4scYPGFyM4eMPeOHHqtpUDS2zSYoA%3D%3D";
		// GameBackLogger.SYS_LOG.info("args: data=" + data);

		String reqsig = getRequest().getParameter("reqsig");
		if (reqsig != null) {
			reqsig = X5Service.x5_quote(reqsig);
		}
		// reqsig = "GSOnCNbncMK9ySNTx5cfVkNAwoU%3D";
		// GameBackLogger.SYS_LOG.info("args: reqsig=" + reqsig);

		JSONObject ret = new JSONObject();
		try {
			ret.put("ret", 0);
			ret.put("msg", "success");
			ret.put("time", System.currentTimeMillis() / 1000);
			ret.put("nonce", X5Service.getRandomString(15));

			boolean is_sign_correct = X5Service.x5_is_correct_of_price_sign(data, reqsig);
			if (is_sign_correct) {
				JSONObject args = X5Service.x5_get_decoded_args(data);

				if (args == null || !this.checkCustommeta(args.getString("custommeta"))) {
					ret.put("ret", -2);
					ret.put("msg", "failure: Parse arguments failed!");
				} else {
					String qbopenid = args.getString("qbopenid");
					String payitem = args.getString("payitem");
					String custommeta = args.getString("custommeta");
					String[] customArray = custommeta.split("_");
					String server_id = customArray[2];
					String channelId = customArray[3];
					int itemid = Integer.parseInt(payitem.split("\\*")[0]);
					PayBean bean = GameServerManager.getInstance().getPayBean(itemid);
					if (bean == null || StringUtils.isBlank(server_id) || StringUtils.isBlank(channelId)) {
						GameBackLogger.SYS_LOG.info("qqbrowser price::" + custommeta + " item is not exist..." + server_id + " " + channelId);
						this.result = "item not exist";
						return StringResultType.RETURN_ATTRIBUTE_NAME;
					}

					OrderValidate exsit = orderValiDao.getOne(custommeta);
					if (exsit != null) {
						GameBackLogger.SYS_LOG.info("qqbrowser price::" + custommeta + " has been insert..." + server_id + " " + channelId);
						if (exsit.getCreate_time() != null) {
							long now = TimeUtil.currentTimeMillis();
							long apart = now - exsit.getCreate_time().getTime();
							// 订单30分钟过期
							if (apart / TimeUtil.MIN_IN_MINILLS > 30) {
								this.result = "order has expired";
							} else {
								ret.put("payamount", bean.getAmount() * 10);
								this.result = X5Service.x5_price_response(ret);
							}

						} else {
							GameBackLogger.SYS_LOG.info("qqbrowser price createtime is null");

						}

						// this.result = "orderid is exist";
						return StringResultType.RETURN_ATTRIBUTE_NAME;
					}

					String pf = "qqbrowser";
					if (channelId.equals("qqbcom")) {
						pf = "qqbcom";
						channelId = "";
					}

					OrderValidate ov = new OrderValidate();
					ov.setFlat_id(qbopenid);
					ov.setOrder_id(custommeta);
					ov.setServer_id(server_id);
					ov.setPf(pf);
					ov.setItem_id(itemid);
					ov.setAmount(bean.getAmount());
					ov.setPay_channel(channelId);
					orderValiDao.insert(ov);
					ret.put("payamount", bean.getAmount() * 10);
				}
			} else {
				GameBackLogger.SYS_LOG.info("ERROR(Price): The sign wasn't correct when checking signing!");
				ret.put("ret", -1);
				ret.put("msg", "failure: sign wasn't correct!");
			}
			GameBackLogger.SYS_LOG.info("QBPriceAction resp::" + ret.toString());
			this.result = X5Service.x5_price_response(ret);
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("qqbrowser price exception::", e);
		}
		return StringResultType.RETURN_ATTRIBUTE_NAME;
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

}
