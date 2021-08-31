package com.sy.sanguo.game.service.pfs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.util.*;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.server.GameServerManager;
import com.sy.sanguo.common.server.PayBean;
import com.sy.sanguo.game.bean.OrderValidate;
import com.sy.sanguo.game.bean.OvaliMsg;
import com.sy.sanguo.game.bean.PfSdkConfig;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.service.BaseSdk;

/**
 * @author lc 微富通
 * 
 */
public class WeiFuTong extends BaseSdk {
	// 商户号：101560003280
	// AppID: wx75f3966a99dec79e
	// AppSecret: 78c61fb712d951544fa290df0b354dbb

	// private static String mch_id="101560003280";
	// private static String appId = "wx75f3966a99dec79e";
	// private static String appKey = "78c61fb712d951544fa290df0b354dbb";

	@Override
	public String payExecute() {
		String retStr = NetTool.receivePost(getRequest());
		GameBackLogger.SYS_LOG.info(pf + "payExecute:" + retStr);
		if (StringUtils.isBlank(retStr)) {
			return "fail";
		}
		Map<String, Object> params = XmlUtil.toMap(retStr);
		String status = params.get("status").toString();
		if (!status.equals("0")) {
			return "fail";
		}

		String result_code = params.get("result_code").toString();
		if (!result_code.equals("0")) {
			return "fail";
		}

		String sign = params.get("sign").toString();

		String transaction_id = params.get("transaction_id").toString();
		String out_trade_no = params.get("out_trade_no").toString();
		String total_fee = params.get("total_fee").toString();
		String result = "fail";
		try {
			OrderValidate ov = orderValiDao.getOne(out_trade_no);
			if (ov == null || ov.getStatus() != 0) {
				GameBackLogger.SYS_LOG.error(pf + " payExecute orderVali is null");
				return "fail";
			}

			String pf = ov.getPf();
			String[] payChannel = ov.getPay_channel().split(",");
			String channel = StringUtil.getValue(payChannel, 0);
			String payType = StringUtil.getValue(payChannel, 1);
			long agencyUserId = ov.getAgencyUserId();
			
			if (!StringUtils.isBlank(channel) && !channel.equals("null")) {
				pf = pf + channel;
			}

			if (StringUtils.isBlank(payType)) {
				payType = ov.getPf();
			}

			PfSdkConfig config = getPfSdkConfig(payType);
			if (!sign.equals(sign(params, config.getPayKey()))) {
				GameBackLogger.SYS_LOG.error(pf + " payExecute sign is err");
				return "fail";
			}
			if (ov.getAmount() * 100 != Integer.parseInt(total_fee)) {
				GameBackLogger.SYS_LOG.error(pf + " payExecute total_fee is err" + total_fee);
				return "fail";
			}

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

				code = payCards(user, ov, transaction_id, bean, String.valueOf(user0.getUserId()));
				userInfo = user;
			} else {
				if (ov.getUserId()!=null&&ov.getUserId().longValue()>0L){
					userInfo = userDao.getUser(ov.getUserId());
				}else{
				userInfo = userDao.getUser(ov.getFlat_id(), ov.getPf());
				}
				code = payCards(userInfo, ov, transaction_id, bean);
			}
			
			switch (code) {
			case 1:
				result = "success";
				break;
			case 2:
				result = "FAIL";
				break;
			case 3:
				result = "FAIL";
				break;
			case 0:// 充值成功
				result = "success";
				break;
			case -1:
				result = "FAIL";
				break;
			}
			if (code == 0) {
				insertRoomCard(ov, bean, transaction_id, userInfo);
			}

			GameBackLogger.SYS_LOG.info(pf + " pay orderid:" + transaction_id + ",code:" + code);
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error(pf + " pay err:", e);
			return "fail";
		}
		return result;
	}

	@Override
	public String ovali() {
		Map<String, Object> result = new HashMap<String, Object>();

		String payForbidPfs = PropertiesCacheUtil.getValue("pay_forbid_pfs",Constants.GAME_FILE);
		if (StringUtils.contains(payForbidPfs,"|"+payType+"|")){
			result.put("code", "111");
			result.put("msg", "发起订单失败");
			GameBackLogger.SYS_LOG.warn("pay_forbid_pfs:"+payForbidPfs+",payType="+payType);
			return JacksonUtil.writeValueAsString(result);
		}

		OvaliMsg msg = ovaliComMsg();
		if (msg.getCode() == 0) {
			try {
				if (StringUtils.isBlank(payType)) {
					payType = msg.getOv().getPf();
				}
                result.put("payType",payType);
				PfSdkConfig config = getPfSdkConfig(payType);
				boolean isWap = false;
				String url = "https://pay.swiftpass.cn/pay/gateway";
				String nonce_str = RandomStringUtils.randomAlphabetic(10);
//				String requesturl = getRequest().getRequestURL().toString();
//				String notify_url = requesturl.replace("support!ovali_com", "pay!weifutong");
				String notify_url =loadPayUrl(getRequest(),"support!ovali_com", "pay!weifutong");
				String mch_create_ip = getIpAddr(request);

				// /////////////////////////////////////////////////////

				Map<String, Object> payMap = new HashMap<>();
				if (isWap) {
					payMap.put("service", "pay.weixin.wappay");
				} else {
					payMap.put("service", "unified.trade.pay");
				}
				payMap.put("mch_id", config.getMch_id());
				payMap.put("out_trade_no", msg.getOv().getOrder_id());
				payMap.put("body", msg.getPayItem().getName());
				payMap.put("total_fee", msg.getPayItem().getAmount() * 100);
				payMap.put("mch_create_ip", mch_create_ip);
				payMap.put("notify_url", notify_url);
				payMap.put("nonce_str", nonce_str);
				payMap.put("sign", sign(payMap, config.getPayKey()));
				String xmlStr = XmlUtil.fromMap(payMap, "xml");

				// /////////////////////////////////////////////////////////////
				Http http = new Http(url, false);
				String postXml = http.post(xmlStr);
				Map<String, Object> resMap = XmlUtil.toMap(postXml);
				String status = resMap.get("status").toString();
				if (status.equals("0")) {
					String resMd5 = sign(resMap, config.getPayKey());
					if (resMd5.equals(resMap.get("sign").toString())) {
						if (isWap) {
							String pay_info = resMap.get("pay_info").toString();
							// Map<String, Object> urlMap = new HashMap<>();
							// urlMap.put("pay_info", pay_info);
							result.put("url", pay_info);
						} else {
							String services = resMap.get("services").toString();
							String token_id = resMap.get("token_id").toString();

							Map<String, Object> urlMap = new HashMap<>();
							urlMap.put("token_id", token_id);
							urlMap.put("services", services);
                            if (payType.startsWith("futongMp")) {
                                JSONObject extraObj = JSONObject.parseObject(config.getExtStr());
                                urlMap.put("miniProgramId",extraObj.getString("miniProgramId"));
                                urlMap.put("appId",config.getAppId());
                            }
							result.put("url", urlMap);
						}
						result.put("code", 0);
					} else {
						result.put("code", -1);
					}
				} else {
					result.put("code", -2);
					GameBackLogger.SYS_LOG.info(payType + config.getAppId() + " postXml-->" + JacksonUtil.writeValueAsString(resMap));
				}

				// /////////////////////////////////////////
			} catch (Exception e) {
				GameBackLogger.SYS_LOG.error(pf + "ovali err:", e);
			}

		} else {
			result.put("code", msg.getCode());
			result.put("msg", msg.getMsg());
		}
		return JacksonUtil.writeValueAsString(result);
	}

	private static String sign(Map<String, Object> map, String appKey) {
		Object[] oArr = map.keySet().toArray();
		Arrays.sort(oArr);
		StringBuffer sb = new StringBuffer();
		for (Object o : oArr) {
			String key = o.toString();
			if (key.equals("sign")) {
				continue;
			}
			Object value = map.get(key);
			if (value == null || StringUtils.isBlank(value.toString())) {
				continue;
			}
			sb.append(key).append("=").append(value).append("&");
		}
		sb.append("key=").append(appKey);
		return MD5Util.getStringMD5(sb.toString()).toUpperCase();
	}

	public static void main(String[] args) {
		String post = "<xml><charset><![CDATA[UTF-8]]></charset><services><![CDATA[wft.rns.tft|pay.weixin.scancode|pay.weixin.native|pay.weixin.micropay|pay.weixin.jspay|pay.alipay.micropay|pay.weixin.app|pay.alipay.app|pay.qq.micropay|pay.jdpay.micropay|pay.qq.jspay|pay.jdpay.native|pay.alipay.native|pay.alipay.wappay|trade.urovo.pos|wft.rns.smzy|pay.alipay.jspay]]></services><sign><![CDATA[932CAEB46523853EF4B3562B79622468]]></sign><sign_type><![CDATA[MD5]]></sign_type><status><![CDATA[0]]></status><token_id><![CDATA[e221eea7ab9fcd1cde5ffceac403fe6b]]></token_id><version><![CDATA[2.0]]></version></xml>";
		Map<String, Object> map = XmlUtil.toMap(post);
		// map.put("body", "2张房卡");
		// map.put("total_fee", 1000);
		// map.put("sign", sign(map));
		System.out.println(JacksonUtil.writeValueAsString(map));
		// // Map<String, Object> payMap = new HashMap<>();
		// // payMap.put("service", "unified.trade.pay");
		// String xmlStr = XmlUtil.fromMap(map, "xml");
		// String url = "https://pay.swiftpass.cn/pay/gateway";
		// Http http;
		//
		// try {
		// http = new Http(url, false);
		// String postXml = http.post(xmlStr);
		// System.out.println(postXml);
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	@Override
	public String loginExecute() {
		return null;
	}

}
