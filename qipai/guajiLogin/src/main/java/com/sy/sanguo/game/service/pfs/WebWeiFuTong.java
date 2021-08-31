package com.sy.sanguo.game.service.pfs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.util.*;
import com.sy.sanguo.game.bean.PfSdkConfig;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import com.sy.sanguo.game.staticdata.PfCommonStaticData;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.server.GameServerManager;
import com.sy.sanguo.common.server.PayBean;
import com.sy.sanguo.game.bean.OrderValidate;
import com.sy.sanguo.game.bean.OvaliMsg;
import com.sy.sanguo.game.service.BaseSdk;
import org.apache.commons.lang3.math.NumberUtils;

public class WebWeiFuTong extends BaseSdk {
//	private static String appId = "7552900037";
//	private static String appKey = "11f4aca52cf400263fdd8faf7a69e007";
//	private static String mch_id = "101560003280";
//	private static String appId = "wx75f3966a99dec79e";
//	private static String appKey = "78c61fb712d951544fa290df0b354dbb";

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
		GameBackLogger.SYS_LOG.info("payType="+getString("payType"));
		if (!sign.equals(sign(params,PfCommonStaticData.getConfig(getString("payType"))))) {
			GameBackLogger.SYS_LOG.error(pf + " payExecute sign is err");
			return "fail";
		}

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

			if (ov.getAmount()*100 != Integer.parseInt(total_fee)) {
				GameBackLogger.SYS_LOG.error(pf + " payExecute total_fee is err" + total_fee);
				return "fail";
			}

			String pf = ov.getPf();
			String[] payChannel = ov.getPay_channel().split(",");
			String channel = StringUtil.getValue(payChannel, 0);

			if (!StringUtils.isBlank(channel) && !channel.equals("null")) {
				pf = pf + channel;
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
				String total_fee = this.getString("total_fee");
				String url = "https://pay.swiftpass.cn/pay/gateway";
				String nonce_str = RandomStringUtils.randomAlphabetic(10);
//				String requesturl = getRequest().getRequestURL().toString();
				String notify_url =loadPayUrl(getRequest(),"support!ovali_com", "pay!webweifutong");
				String mch_create_ip = getIpAddr(request);

				// /////////////////////////////////////////////////////

				PayBean bean = msg.getPayItem();
				if (bean == null) {
					result.put("code", -4);
					return JacksonUtil.writeValueAsString(result);
				}
				PfSdkConfig config=PfCommonStaticData.getConfig(payType);
				if (!notify_url.contains("payType="+config.getPf())){
					notify_url=notify_url.contains("?")?(notify_url+"&payType="+config.getPf()):(notify_url+"?payType="+config.getPf());
				}

				Map<String, Object> payMap = new HashMap<>();
				payMap.put("service", "pay.weixin.wappay");
				payMap.put("mch_id", config.getMch_id());
				payMap.put("out_trade_no", msg.getOv().getOrder_id());
				payMap.put("body", bean.getName());
				payMap.put("total_fee", total_fee);
				payMap.put("mch_create_ip", mch_create_ip);
				payMap.put("notify_url", notify_url);
				payMap.put("nonce_str", nonce_str);

				payMap.put("attach","附加信息");
				payMap.put("device_info","AND_WAP");
				payMap.put("mch_app_name","上游戏");
				payMap.put("mch_app_id","http://www.139up.com");
				payMap.put("version","2.0");
				payMap.put("sign_type","MD5");

				payMap.put("is_raw","1");

				payMap.put("sign", sign(payMap,PfCommonStaticData.getConfig(payType)));
				String xmlStr = XmlUtil.fromMap(payMap, "xml");
				GameBackLogger.SYS_LOG.info(JacksonUtil.writeValueAsString(payMap));
				// /////////////////////////////////////////////////////////////
				Http http = new Http(url, false);
				String postXml = http.post(xmlStr);
				Map<String, Object> resMap = XmlUtil.toMap(postXml);

				LogUtil.i("create order url:"+url+",xmlStr="+xmlStr+",result="+postXml);

				String status = resMap.get("status").toString();
//				GameBackLogger.SYS_LOG.info(JacksonUtil.writeValueAsString(resMap));
				if (status.equals("0")) {
					String result_code = resMap.get("result_code").toString();
					if (result_code.equals("0")) {
						String resMd5 = sign(resMap,PfCommonStaticData.getConfig(payType));
						if (resMd5.equals(resMap.get("sign").toString())) {
							String pay_info = resMap.get("pay_info").toString();

							Map<String, Object> urlMap = new HashMap<>();
							urlMap.put("pay_info", pay_info);
							result.put("url", urlMap);
							result.put("code", 0);

						} else {
							result.put("code", -1);
						}
					} else {
						result.put("code", -3);
					}

				} else {
					result.put("code", -2);
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

	@Override
	protected void buildOrderVali(OrderValidate ov) {
//		String total_fee = this.getString("total_fee");
//		PayBean bean = GameServerManager.getInstance().getPayBeanByAmount(Integer.parseInt(total_fee) / 100);
//		if (bean == null) {
//			return;
//		}
//		ov.setItem_id(bean.getId());
//		ov.setAmount(bean.getAmount());
	}

	private String sign(Map<String, Object> map,PfSdkConfig config) {
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
		sb.append("key=").append(config.getPayKey());
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

	@Override
	public void queryOrder() {
		try {
			Map<String, String> params = UrlParamUtil.getParameters(getRequest());
			String mOrderId=params.get("mOrderId");
			String tOrderId=params.get("tOrderId");
			Map<String,Object> map=new HashMap<>();

			if (StringUtils.isNotBlank(mOrderId)){
				map.put("out_trade_no",mOrderId);
			}else{
				OutputUtil.output(3,"error:mOrderId is empty!",getRequest(),getResponse(),false);
				return;
			}

			map.put("service","unified.trade.query");
			map.put("version","2.0");
			map.put("sign_type","MD5");
			map.put("charset","UTF-8");
			map.put("nonce_str", UUID.randomUUID().toString().replace("-",""));

			if (StringUtils.isNotBlank(tOrderId)){
				map.put("transaction_id",tOrderId);
			}

			OrderValidate ov = orderValiDao.getOne(mOrderId);
			if (ov == null || ov.getStatus() != 0) {
				OutputUtil.output(3,"error:mOrderId "+(ov == null?"is null" : "status != 0"),getRequest(),getResponse(),false);
				return;
			}

			String[] payChannel = ov.getPay_channel().split(",");

			String payPf=payChannel.length>=2?payChannel[1]:payChannel[0];

			PfSdkConfig config=PfCommonStaticData.getConfig(payPf);

			if (config==null){
				OutputUtil.output(3,"error:config is null,payType="+payPf,getRequest(),getResponse(),false);
				return;
			}else{
				map.put("mch_id",config.getMch_id());
				map.put("sign", sign(map,config));
			}

			String url = "https://pay.swiftpass.cn/pay/gateway";

			String xmlStr = XmlUtil.fromMap(map, "xml");

			Http http = new Http(url, false);
			String postXml = http.post(xmlStr);
			Map<String, Object> resMap = XmlUtil.toMap(postXml);

			LogUtil.i("query order url:"+url+",xmlStr="+xmlStr+",result="+postXml);

			String status = String.valueOf(resMap.get("status"));
//				GameBackLogger.SYS_LOG.info(JacksonUtil.writeValueAsString(resMap));
			if ("0".equals(status)) {
				String result_code = String.valueOf(resMap.get("result_code"));
				if ("0".equals(result_code)) {
					String resMd5 = sign(resMap,config);
					if (resMd5.equals(String.valueOf(resMap.get("sign")))) {

						String trade_state=String.valueOf(resMap.get("trade_state"));
						if ("SUCCESS".equalsIgnoreCase(trade_state)){
							int total_fee= NumberUtils.toInt(String.valueOf(resMap.get("total_fee")),-1);
							if (total_fee!=ov.getAmount()*100){
								OutputUtil.output(3,"error:total_fee "+ov.getAmount()+"*100!="+total_fee,getRequest(),getResponse(),false);
								return;
							}else{
								String transaction_id=String.valueOf(resMap.get("transaction_id"));
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
										OutputUtil.output(0,"ok,1",getRequest(),getResponse(),false);
										break;
									case 2:
										OutputUtil.output(3,"code:"+code,getRequest(),getResponse(),false);
										break;
									case 3:
										OutputUtil.output(3,"code:"+code,getRequest(),getResponse(),false);
										break;
									case 0:// 充值成功
										insertRoomCard(ov, bean, transaction_id, userInfo);
										OutputUtil.output(0,"success",getRequest(),getResponse(),false);
										LogUtil.e("resend success:"+JacksonUtil.writeValueAsString(ov));
										break;
									case -1:
										OutputUtil.output(3,"code:"+code,getRequest(),getResponse(),false);
										break;
								}
								return;
							}
						}

					}else{
						OutputUtil.output(3,"sign error:"+resMd5+" != "+resMap.get("sign"),getRequest(),getResponse(),false);
						return;
					}
				}else{
					OutputUtil.output(3,"result_code error:result_code="+result_code+",err_code="+resMap.get("err_code")+",err_msg="+resMap.get("err_msg"),getRequest(),getResponse(),false);
					return;
				}
			}else{
				OutputUtil.output(3,"status error:status="+status+",message="+resMap.get("message"),getRequest(),getResponse(),false);
				return;
			}
		}catch (Exception e){
			OutputUtil.output(2,"Exception:"+e.getMessage(),getRequest(),getResponse(),false);
		}

	}
}
