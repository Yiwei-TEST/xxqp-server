package com.sy.sanguo.game.service.pfs;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.sy.sanguo.game.bean.RegInfo;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import com.alibaba.fastjson.TypeReference;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.server.GameServerManager;
import com.sy.sanguo.common.server.PayBean;
import com.sy.sanguo.common.util.Http;
import com.sy.sanguo.common.util.JacksonUtil;
import com.sy.sanguo.common.util.JsonWrapper;
import com.sy.sanguo.common.util.MD5Util;
import com.sy.sanguo.game.bean.OrderValidate;
import com.sy.sanguo.game.bean.OvaliMsg;
import com.sy.sanguo.game.service.BaseSdk;

public class Tonglian extends BaseSdk {
	private static String appId = "00008534";
	private static String cusid = "271551048163156";
	private static String sub_mchid = "20036136";
	private static String sub_appid = "wx8fc5c2549fb186b1";
	private static String key = "xunyou84756912365478951wacdxw564v";

	@Override
	public String payExecute() {
		GameBackLogger.SYS_LOG.info(pf + "payExecute:" + JacksonUtil.writeValueAsString(getRequest().getParameterMap()));
		/*
		 * {"trxid":["303160574"],"trxdate":["20170216"],"termtraceno":["0"],
		 * "trxstatus"
		 * :["3045"],"appid":["00008534"],"trxcode":["VSP501"],"cusid"
		 * :["271551048163156"],
		 * "paytime":["20170216102413"],"sign":["E8DF297AE2630C6472B1CA3D18FEED1D"
		 * ],"acct":["000000"],"cusorderid":["7c5d10a5c6ef357f58a50ee4"],
		 * "outtrxid":["7c5d10a5c6ef357f58a50ee4"],"trxamt":["1500"]}
		 */
		Map<String, String> params = new HashMap<>();
		for (Object o : getRequest().getParameterMap().keySet()) {
			params.put(o.toString(), this.getString(o.toString()));
		}

		// {"sign":"8D4BB3E0644813AC5B5DC4147C56CBD5","trxdate":"20170216","trxid":"303212692","acct":"000000","cusorderid":"e8b3293c74ec5b1b58a51a99","termtraceno":"0","trxstatus":"3045","appid":"00008534","trxcode":"VSP501","cusid":"271551048163156","outtrxid":"e8b3293c74ec5b1b58a51a99","paytime":"20170216111904","trxamt":"100"}
		String result = "fail";
		String sign = this.getString("sign");
		String signData = sign(params);
		if (!sign.equals(signData)) {
			GameBackLogger.SYS_LOG.error(pf + " payExecute sign is err:" + sign + "  " + JacksonUtil.writeValueAsString(params));
			return "fail";
		}

		String trxstatus = this.getString("trxstatus");
		if (!trxstatus.equals("0000")) {
			GameBackLogger.SYS_LOG.error(pf + " payExecute trxstatus is err:" + trxstatus);
			return "fail";
		}
		String cusorderid = this.getString("cusorderid");
		String trxid = this.getString("trxid");
		result = pay(cusorderid, trxid);
		return result;
	}

	private String pay(String cusorderid, String trxid) {
		String result = "fail";
		try {
			OrderValidate ov = orderValiDao.getOne(cusorderid);
			String pf = ov.getPf();
			PayBean bean = GameServerManager.getInstance().getPayBean(ov.getItem_id());
			RegInfo userInfo;
			int code;
			if (ov.getAgencyUserId() > 0) {
				RegInfo user = userDao.getUser(ov.getAgencyUserId());
				RegInfo user0 ;
				if (ov.getUserId()!=null&&ov.getUserId().longValue()>0L){
					user0 = userDao.getUser(ov.getUserId());
				}else{
					user0 = userDao.getUser(ov.getFlat_id(), ov.getPf());
				}
				code = payCards(user,ov, trxid, bean, String.valueOf(user0.getUserId()));
				userInfo = user;
			} else {
				if (ov.getUserId()!=null&&ov.getUserId().longValue()>0L){
					userInfo = userDao.getUser(ov.getUserId());
				}else{
					userInfo = userDao.getUser(ov.getFlat_id(), ov.getPf());
				}
				code = payCards(userInfo,ov, trxid, bean);
			}
			switch (code) {
			case 1:
				result = "success";
				break;
			case 2:
				result = "fail";
				break;
			case 3:
				result = "fail";
				break;
			case 0:// 充值成功
				result = "success";
				break;
			case -1:
				result = "fail";
				break;
			}
			if (code == 0) {
				insertRoomCard(ov, bean, trxid, userInfo);
			}
			if (code == 0 || code == 1) {
				orderValiDao.validate(cusorderid);
			}
			GameBackLogger.SYS_LOG.info(pf + " pay orderid:" + trxid + ",code:" + code);
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error(pf + " pay err:", e);
			return "fail";
		}
		return result;
	}

	@Override
	public String ovali() {
		Map<String, Object> result = new HashMap<String, Object>();
		OvaliMsg msg = ovaliCom();
		if (msg.getCode() == 0) {
			result.put("code", 0);
			result.put("url", msg.getMsg());

		} else {
			result.put("code", msg.getCode());
			result.put("msg", msg.getMsg());
		}
		return JacksonUtil.writeValueAsString(result);
	}

	@Override
	protected void ovaliCom1(OvaliMsg msg) throws Exception {
		// Map<String, Object> result = new HashMap<String, Object>();
		// PfSdkConfig config = getPfSdkConfig();
		// JSAPI--公众号支付、NATIVE--原生扫码支付、APP--app支付，统一下单接口trade_type的传参可参考这里
		String url = "https://vsp.allinpay.com/apiweb/weixin/pay";
		String nonce_str = RandomStringUtils.randomAlphabetic(10);
		String requesturl = getRequest().getRequestURL().toString();
		String notify_url = requesturl.replace("support!ovali_com", "pay!tonglian");
		Map<String, String> params = new HashMap<>();
		params.put("cusid", cusid);
		params.put("appid", appId);
		params.put("trxamt", msg.getPayItem().getAmount() * 100 + "");
		params.put("reqsn", msg.getOv().getOrder_id());
		params.put("paytype", "2");
		params.put("randomstr", nonce_str);
		params.put("sub_appid", sub_appid);
		params.put("sub_mchid", sub_mchid);
		params.put("notify_url", notify_url);
		params.put("limit_pay", "no_credit");
		params.put("sign", sign(params));
		Http http = new Http(url, false);
		String retMsg = http.post(params);

		if (!StringUtils.isBlank(retMsg)) {
			JsonWrapper wrapper = new JsonWrapper(retMsg);
			String retcode = wrapper.getString("retcode");
			if (!retcode.equals("SUCCESS")) {
				String retmsg = wrapper.getString("retmsg");
				msg.setCode(-1);
				msg.setMsg(retmsg);
				GameBackLogger.SYS_LOG.error(pf + "ovali err:" + retMsg);
				return;
			}

			String trxstatus = wrapper.getString("trxstatus");
			if (!trxstatus.equals("0000")) {
				String errmsg = wrapper.getString("errmsg");
				msg.setCode(-2);
				msg.setMsg(errmsg);
				GameBackLogger.SYS_LOG.error(pf + "ovali err:" + retMsg);
				return;
			}

			String trxid = wrapper.getString("trxid");
			msg.getOv().setSdk_order_id(trxid);

			// /////////////////////////////////////////////////////
			msg.setCode(0);
			msg.setMsg(wrapper.getString("weixinstr"));
			// /////////////////////////////////////////////////////
		} else {
			msg.setCode(-3);
			msg.setMsg("请求失败");
			GameBackLogger.SYS_LOG.error(pf + "ovali err:" + retMsg);

		}

	}

	private static String sign(Map<String, String> params) {
		Map<String, String> copy = new HashMap<>(params);
		copy.put("key", key);
		Object[] keys = copy.keySet().toArray();
		Arrays.sort(keys);

		StringBuffer sb = new StringBuffer();
		for (Object o : keys) {
			if (o.toString().equals("sign")) {
				continue;
			}
			Object value = copy.get(o);
			sb.append(o).append("=").append(value).append("&");
		}

		// sb.append(key);
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return MD5Util.getStringMD5(sb.toString()).toUpperCase();
	}

	private String query(String cpOrderId) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		OrderValidate ov = orderValiDao.getOne(cpOrderId);
		if (ov == null) {
			result.put("code", -3);
			result.put("msg", "ov is null");
			GameBackLogger.SYS_LOG.error(pf + "query err ov is null:" + cpOrderId);
			return JacksonUtil.writeValueAsString(result);
		}
		if (ov.getStatus() == 1) {
			result.put("code", -4);
			result.put("msg", "ov status is 1");
			GameBackLogger.SYS_LOG.error(pf + "query err ov status is 1:" + cpOrderId);
			return JacksonUtil.writeValueAsString(result);
		}

		String url = "https://vsp.allinpay.com/apiweb/weixin/query";
		String nonce_str = RandomStringUtils.randomAlphabetic(10);
		String trxid = ov.getSdk_order_id();
		Map<String, String> params = new HashMap<>();
		params.put("cusid", cusid);
		params.put("appid", appId);
		params.put("trxid", trxid);
		params.put("randomstr", nonce_str);
		params.put("sign", sign(params));
		Http http = new Http(url, false);
		String retMsg = http.post(params);

		if (!StringUtils.isBlank(retMsg)) {
			JsonWrapper wrapper = new JsonWrapper(retMsg);
			String retcode = wrapper.getString("retcode");
			if (!retcode.equals("SUCCESS")) {
				String retmsg = wrapper.getString("retmsg");
				result.put("code", -1);
				result.put("msg", retmsg);
				GameBackLogger.SYS_LOG.error(pf + "query err:" + retMsg);
				return JacksonUtil.writeValueAsString(result);
			}

			String trxstatus = wrapper.getString("trxstatus");
			if (!trxstatus.equals("0000")) {
				String errmsg = wrapper.getString("errmsg");
				result.put("code", -2);
				result.put("msg", errmsg);
				GameBackLogger.SYS_LOG.error(pf + "query err:" + retMsg);
				return JacksonUtil.writeValueAsString(result);
			}
		}
		// 补发
		String resultMsg = pay(cpOrderId, trxid);
		result.put("code", 0);
		result.put("msg", resultMsg);
		return JacksonUtil.writeValueAsString(result);

	}

	@Override
	public String common() {
		String opt = this.getOpt();
		if (opt.equals("query")) {
			String cpOrderId = this.getString("cpOrderId");
			try {
				return query(cpOrderId);
			} catch (Exception e) {
				GameBackLogger.SYS_LOG.error("common query err:", e);
				return e.getMessage();
			}
		}
		return null;
	}

	@Override
	public String loginExecute() {
		return null;
	}

	public static void main(String[] args) {
		String data = "{\"sign\":\"8D4BB3E0644813AC5B5DC4147C56CBD5\",\"trxdate\":\"20170216\",\"trxid\":\"303212692\",\"acct\":\"000000\",\"cusorderid\":\"e8b3293c74ec5b1b58a51a99\",\"termtraceno\":\"0\",\"trxstatus\":\"3045\",\"appid\":\"00008534\",\"trxcode\":\"VSP501\",\"cusid\":\"271551048163156\",\"outtrxid\":\"e8b3293c74ec5b1b58a51a99\",\"paytime\":\"20170216111904\",\"trxamt\":\"100\"}";
		Map<String, String> params = JacksonUtil.readValue(data, new TypeReference<Map<String, String>>() {
		});
		// 8D4BB3E0644813AC5B5DC4147C56CBD5
		System.out.println(sign(params));
	}
}
