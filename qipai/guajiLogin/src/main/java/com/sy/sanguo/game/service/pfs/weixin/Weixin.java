package com.sy.sanguo.game.service.pfs.weixin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.Base64Util;
import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.util.*;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.server.GameServerManager;
import com.sy.sanguo.common.server.PayBean;
import com.sy.sanguo.game.bean.OrderValidate;
import com.sy.sanguo.game.bean.OvaliMsg;
import com.sy.sanguo.game.bean.PfSdkConfig;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.RoomcardOrder;
import com.sy.sanguo.game.pdkuai.user.Manager;
import com.sy.sanguo.game.service.BaseSdk;
import com.sy.sanguo.game.service.pfs.weixin.util.WeixinUtil;
import com.sy599.sanguo.util.TimeUtil;

public class Weixin extends BaseSdk {
	// private static String appId = "wx8fc5c2549fb186b1";
	// private static String secret = "46ddeebd9b2778f63b995d3c545fe71b";
	// private static String key = "192a250b4c09247ec02edce69f6a2dba";

	private String code;
	private JsonWrapper sessionJson;
	private JsonWrapper userInfo;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public JsonWrapper getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(JsonWrapper userInfo) {
		this.userInfo = userInfo;
	}

	public JsonWrapper getSessionJson() {
		return sessionJson;
	}

	public void setSessionJson(JsonWrapper sessionJson) {
		this.sessionJson = sessionJson;
	}
	
	@Override
	public String payExecute() {

		String result = "FAIL";
		String resultFail = "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[payFail]]></return_msg></xml>";
		String resultSuccess = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";

		try {
			String retStr = NetTool.receivePost(getRequest());
			GameBackLogger.SYS_LOG.info(pf + "payExecute:" + retStr);
			if (StringUtils.isBlank(retStr)) {
				return resultFail;
			}
			Map<String, Object> params = XmlUtil.toMap(retStr);
			Document doc = DocumentHelper.parseText(retStr);
			Element root = doc.getRootElement();
			Element eName = root.element("return_code");

			if ("SUCCESS".equals(eName.getTextTrim())) {
				eName = root.element("out_trade_no");
				String cporderid = eName.getTextTrim();
				this.payType = root.element("attach").getTextTrim();

				OrderValidate ov = orderValiDao.getOne(cporderid);
				if (ov == null || ov.getStatus() != 0) {
					GameBackLogger.SYS_LOG.error(pf + " payExecute orderVali is null");
					return resultFail;
				}

				if (!root.element("sign").getTextTrim().equals(sign(params))) {
					GameBackLogger.SYS_LOG.error(pf + " payExecute sign is err");
					return resultFail;
				}

				String pf = ov.getPf();
				String[] payChannel = ov.getPay_channel().split(",");
				String channel = StringUtil.getValue(payChannel, 0);

				if (!StringUtils.isBlank(channel) && !channel.equals("null")) {
					pf = pf + channel;
				}

				eName = root.element("total_fee");
				int money = Integer.parseInt(eName.getTextTrim());

				PayBean bean = GameServerManager.getInstance().getPayBean(ov.getItem_id());
//				PayBean bean = GameServerManager.getInstance().getPayBeanByAmount(money / 100);
				// PayBean bean = new PayBean();
				// bean.setAmount(money);
				// bean.setId(1);
				// bean.setYuanbao(money);

				// 支付钱是否对应上
				// if (bean.getAmount() != money) {
				// GameBackLogger.SYS_LOG.error(pf +
				// " payExecute amount is err");
				// return resultFail;
				// }

				eName = root.element("transaction_id");
				String transid = eName.getTextTrim();

				// bean.setAmount(money / 10);
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
					code = payCards(user,ov, transid, bean, String.valueOf(user0.getUserId()));
					userInfo = user;
				} else {
					if (ov.getUserId()!=null&&ov.getUserId().longValue()>0L){
						userInfo = userDao.getUser(ov.getUserId());
					}else{
						userInfo = userDao.getUser(ov.getFlat_id(), ov.getPf());
					}
					code = payCards(userInfo,ov, transid, bean);
				}
				switch (code) {
				case 1:
					result = "SUCCESS";
					break;
				case 2:
					result = "FAIL";
					break;
				case 3:
					result = "FAIL";
					break;
				case 0:// 充值成功
					result = "SUCCESS";
					break;
				case -1:
					result = "FAIL";
					break;
				}
				GameBackLogger.SYS_LOG.info(pf + " pay orderid:" + transid + ",code:" + code);

				if (code == 0) {
					// 记录直充记录
					RoomcardOrder order = new RoomcardOrder();
					order.setRoleId(userInfo.getUserId());
					order.setOrderId(transid);
					order.setRegisterBindAgencyId(userInfo.getRegBindId());
					order.setRechargeBindAgencyId(userInfo.getPayBindId());
					if (orderDao.isFirstRecharge(userInfo.getUserId()) <= 0) {
						order.setIsFirstPayAmount(money / 10);
						order.setIsFirstPayBindId(1);
					}
					order.setCommonCards(bean.getYuanbao());
					order.setFreeCards(0);
					order.setIsDirectRecharge(1);
					order.setRechargeWay("weixin");
					order.setRechargeAgencyId(0);
					order.setOrderStatus(1);
					order.setCreateTime(TimeUtil.now());// 时间
					orderDao.insertRoomCards(order);
				}

			} else {
				return resultFail;
			}

		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error(pf + " pay err:", e);
			return resultFail;
		}

		if ("SUCCESS".equals(result)) {
			return resultSuccess;
		} else {
			return resultFail;
		}
	}

	private String sign(Map<String, Object> map) {
		PfSdkConfig config = getPfSdkConfig(this.payType);
		Object[] oArr = map.keySet().toArray();
		Arrays.sort(oArr);
		StringBuilder sb = new StringBuilder();
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
		return com.sy.mainland.util.MD5Util.getMD5String(sb.toString());
	}

	@Override
	public String loginExecute() {
		String result = "";
		String access_token;
		String openid;
		if (sessionJson == null){
			String refresh_token = getString("refresh_token");
			if (getOpt()!=null&&getOpt().startsWith("auth:")&&getExt()!=null&&getExt().startsWith("openid:")){
				access_token = getOpt().substring(5);
				openid = getExt().substring(7);
			} else if("refresh".equals(getOpt())&&StringUtils.isNotBlank(refresh_token)&&!"undefined".equalsIgnoreCase(refresh_token)){
				PfSdkConfig pfSdkConfig = getPfSdkConfig();
				if (pfSdkConfig == null){
					return "";
				}else{
					access_token = null;
					openid = null;

					Map<String,String> map = new HashMap<>();
					map.put("appid",pfSdkConfig.getAppId());
					map.put("grant_type","refresh_token");
					map.put("refresh_token",refresh_token);

					String ret = com.sy.mainland.util.HttpUtil.getUrlReturnValue("https://api.weixin.qq.com/sns/oauth2/refresh_token","UTF-8","GET",map,2);
					if (StringUtils.isNotBlank(ret)){
						JSONObject jsonObject = JSONObject.parseObject(ret);
						openid = jsonObject.getString("openid");
						access_token = jsonObject.getString("access_token");
						if (StringUtils.isBlank(openid)|| StringUtils.isBlank(access_token)){
							return "";
						}
					}
				}
			} else{
				access_token = this.getString("access_token");
				openid = this.getString("openid");
			}

			long time1 = System.currentTimeMillis();
			JsonWrapper json = WeixinUtil.getUserinfo(access_token, openid);

			if ("1".equals(PropertiesCacheUtil.getValueOrDefault("wx_login_time","1",Constants.GAME_FILE))){
				LogUtil.i("weixin login time(ms):"+(System.currentTimeMillis()-time1));
			}

			if (json != null) {
				if (json.hasKey("openid")) {
					setSdkId(json.getString("openid"));
					json.putString("access_token",access_token);
					result = json.toString();
				} else {
					GameBackLogger.SYS_LOG.error(pf + " loginExecute err:" + json.toString());
				}

			}
		}else{
			//微信小程序、小游戏登陆
			String openid0 = sessionJson.getString("openid");
			String unionId = sessionJson.getString("unionid");
			String session_key = sessionJson.getString("session_key");

			String miniProgram = this.getString("miniProgram");
			JSONObject json = JSONObject.parseObject(miniProgram);

			String encryptedData = json.getString("encryptedData");
//			String rawData = json.getString("rawData");
//			String signature = json.getString("signature");
			String iv = json.getString("iv");
//			String userInfo = json.getString("userInfo");

			try {
				SecretKeySpec skeySpec = new SecretKeySpec(Base64Util.decode(session_key.getBytes("utf-8")), "AES");
				Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
				IvParameterSpec ips = new IvParameterSpec(Base64Util.decode(iv.getBytes("utf-8")));
				cipher.init(Cipher.DECRYPT_MODE, skeySpec, ips);
				byte[] original = cipher.doFinal(Base64Util.decode(encryptedData.getBytes("utf-8")));
				encryptedData = new String(original, "UTF-8");

				JSONObject jsonObject = JSONObject.parseObject(encryptedData);

				if (openid0.equals(jsonObject.getString("openId"))&&getPfSdkConfig().getAppId().equals(jsonObject.getJSONObject("watermark").getString("appid"))){
					JSONObject json0 = new JSONObject();
					if (StringUtils.isNotBlank(unionId)){
						json0.put("unionid",unionId);
					}else if (jsonObject.containsKey("unionId")){
						json0.put("unionid",jsonObject.getString("unionId"));
					}
					json0.put("openid",openid0);
					json0.put("nickname",jsonObject.getString("nickName"));
					String image = jsonObject.getString("avatarUrl");
					json0.put("headimgurl",image!=null&&image.startsWith("https://")? image.replace("https://","http://") : image);
					json0.put("sex",jsonObject.getString("gender"));

					setSdkId(openid0);
					result = json0.toString();
					userInfo = new JsonWrapper(json0);
				}
			}catch (Exception e){
				GameBackLogger.SYS_LOG.error("Exception:"+e.getMessage(),e);
			}
		}
		return result;
	}

	@Override
	public String ovali() {
		Map<String, Object> result = new HashMap<String, Object>();
		OvaliMsg msg = ovaliComMsg();
		if (msg.getCode() == 0) {
			// RegInfo userInfo = null;
			try {
				PfSdkConfig config = getPfSdkConfig(payType);
				// JSAPI--公众号支付、NATIVE--扫码支付、APP--app支付，MWEB -- h5支付，统一下单接口trade_type的传参可参考这里
				String openId = this.getString("openid");
				String trade_type = this.getString("trade_type", "APP");
				// String trade_type = "APP";
				String total_fee = this.getString("total_fee");
				String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";

				if (payType.startsWith("weixinmweb")){
					trade_type = "MWEB";
				}else if (payType.startsWith("weixinjsapi")){
					trade_type = "JSAPI";
				}else if (payType.startsWith("weixinnative")){
					trade_type = "NATIVE";
				}else if (payType.startsWith("weixinapp")){
					trade_type = "APP";
				}

				String appid = config.getAppId();
				String mch_id = config.getMch_id();
				String paykey = config.getPayKey();

				String nonce_str = RandomStringUtils.randomAlphabetic(10);// =
																			// "IfeMJketEf";
				String body = "游戏充值";
				// System.out.println("nonce_str=" + nonce_str);
				String out_trade_no = msg.getOv().getOrder_id();// =
																// "1fd9de175f156b9557957d21";
				// String total_fee = "1";
				String spbill_create_ip = getIpAddr(request);// =
																// "113.240.222.140";
				// String notify_url =
				// "http://wxpay.weixin.qq.com/pub_v2/pay/notify.v2.php";
				String notify_url = loadPayUrl(getRequest(),"support!ovali_com", "pay!weixin");//getRequest().getRequestURL().toString();

				Map<String, String> map = new HashMap<String, String>();
				map.put("attach", payType);
				map.put("appid", appid);
				map.put("mch_id", mch_id);
				map.put("nonce_str", nonce_str);
				map.put("body", body);
				map.put("out_trade_no", out_trade_no);
				map.put("total_fee", total_fee + "");
				map.put("spbill_create_ip", spbill_create_ip);
				map.put("notify_url", notify_url);
				map.put("trade_type", trade_type);
				map.put("sign_type", "MD5");

				String tempPage = loadRootUrl(getRequest());
				if ("JSAPI".equals(trade_type)) {
					map.put("openid", openId);
					map.put("device_info", "WEB");
				}else if("MWEB".equals(trade_type)){
					map.put("device_info", "WEB");
					String str = "{\"h5_info\": {\"type\":\"Wap\",\"wap_url\": \""+tempPage+"\",\"wap_name\": \"游戏充值\"}}";
					map.put("scene_info", str);
				}

				Object[] oArr = map.keySet().toArray();
				Arrays.sort(oArr);
				StringBuilder sb = new StringBuilder();
				StringBuilder xmlStr = new StringBuilder();
				xmlStr.append("<xml>");
				for (Object o : oArr) {
					String keyTemp = o.toString();
					if (keyTemp.equals("sign")) {
						continue;
					}
					sb.append(keyTemp);
					sb.append("=");
					String v = map.get(keyTemp);
					sb.append(v);
					sb.append("&");

					xmlStr.append("<").append(keyTemp).append(">").append(v).append("</").append(keyTemp).append(">");
				}

				if (sb.length() > 0) {
					sb.append("key=");
					sb.append(paykey);
				}

				String sign = com.sy.mainland.util.MD5Util.getMD5String(sb.toString());
				map.put("sign", sign);

				xmlStr.append("<sign>").append(sign).append("</sign>");
				xmlStr.append("</xml>");

//				Http http = new Http(url, false);
				String str = xmlStr.toString();
				Map<String,String> params = new HashMap<>(4);
				params.put("$",str);

				String retMsg = com.sy.mainland.util.HttpUtil.getUrlReturnValue(url,"UTF-8","POST",params,5);
				LogUtil.i("weixin create order:url="+url+",body="+str+",ret="+retMsg);

				Document doc = DocumentHelper.parseText(retMsg);

				Element root = doc.getRootElement();
				Element eName = root.element("return_code");
				if ("SUCCESS".equals(eName.getTextTrim())&&"SUCCESS".equals(root.element("result_code").getTextTrim())) {
					eName = root.element("prepay_id");
					Map<String, String> urlPara = new HashMap<String, String>();
					String trade_type0 = root.element("trade_type").getTextTrim();
					urlPara.put("trade_type",trade_type0);
					urlPara.put("prepay_id",eName.getTextTrim());

					if ("MWEB".equalsIgnoreCase(trade_type0)){
						String tmpPage = tempPage;
						if (tmpPage.contains("?")){
							tmpPage = tmpPage+"&forward="+ com.sy.mainland.util.CoderUtil.encode(root.element("mweb_url").getTextTrim());
						}else{
							tmpPage = tmpPage+"?forward="+ com.sy.mainland.util.CoderUtil.encode(root.element("mweb_url").getTextTrim());
						}
						urlPara.put("pay_info",tmpPage);
					}else if ("NATIVE".equals(trade_type)){
						String tmpPage = tempPage;
						if (tmpPage.contains("?")){
							tmpPage = tmpPage+"&qrcode="+ com.sy.mainland.util.CoderUtil.encode(root.element("code_url").getTextTrim());
						}else{
							tmpPage = tmpPage+"?qrcode="+ com.sy.mainland.util.CoderUtil.encode(root.element("code_url").getTextTrim());
						}
						urlPara.put("pay_info",tmpPage);
					} else if ("JSAPI".equals(trade_type)){
						urlPara.clear();
						urlPara.put("appId", appid);
						urlPara.put("timeStamp", TimeUtil.currentTimeMillis() / 1000 + "");
						urlPara.put("nonceStr", nonce_str);
						urlPara.put("package", "prepay_id=" + eName.getTextTrim());
						urlPara.put("signType", "MD5");
					} else {
						urlPara.put("appid", appid);
						urlPara.put("partnerid", mch_id);
						urlPara.put("noncestr", nonce_str);
						urlPara.put("timestamp", TimeUtil.currentTimeMillis() / 1000 + "");
						urlPara.put("prepayid", eName.getTextTrim());
						urlPara.put("package", "Sign=WXPay");
					}

					oArr = urlPara.keySet().toArray();
					Arrays.sort(oArr);
					sb = new StringBuilder();

					for (Object o : oArr) {
						String keyTemp = o.toString();
						if (keyTemp.equals("sign") || keyTemp.equals("paySign")) {
							continue;
						}
						sb.append(keyTemp);
						sb.append("=");
						sb.append(urlPara.get(keyTemp));
						sb.append("&");
					}

					if (sb.length() > 0) {
						sb.append("key=");
						sb.append(paykey);

					}

					sign = com.sy.mainland.util.MD5Util.getMD5String(sb.toString());

					if ("JSAPI".equals(trade_type)){
						urlPara.put("paySign", sign);
					} else {
						urlPara.put("sign", sign);
					}

					result.put("code", msg.getCode());
					result.put("url", urlPara);

				} else {
					eName = root.element("return_msg");
					result.put("code", -2);
					result.put("msg", eName.getTextTrim());
				}
			} catch (Exception e) {
				GameBackLogger.SYS_LOG.error(pf + "ovali err:", e);
			}

		} else {
			result.put("code", msg.getCode());
			result.put("msg", msg.getMsg());
		}
		return JacksonUtil.writeValueAsString(result);
	}

	// private void post() {
	// HttpsRequest httpRequest =
	// HttpsRequest.post("https://api.mch.weixin.qq.com/pay/unifiedorder");
	// String x = xstream.toXML(data);
	// httpRequest.body( StringUtil.convertCharset(x, "UTF-8", "ISO-8859-1"));
	// HttpResponse response = httpRequest.send();
	// String body = response.body();
	// body = StringUtil.convertCharset(body, "ISO-8859-1", "UTF-8");
	//
	//
	// }

	@Override
	public String common() {
		String opt = this.getOpt();
		if (opt.equals("getAccessToken")) {
			String code = getRequest().getParameter("code");
			PfSdkConfig config = getPfSdkConfig();
			JsonWrapper wrapper = WeixinUtil.getAccessToken(config.getAppId(), config.getAppKey(), code);
			if (wrapper != null) {
				if (!wrapper.isHas("access_token")) {
					GameBackLogger.SYS_LOG.error("common getAccessToken err: code-->" + code + " wx-->" + wrapper.toString());
				}

				return wrapper.toString();
			} else {
				Map<String, Object> err = new HashMap<String, Object>();
				err.put("errcode", -1);
				return JacksonUtil.writeValueAsString(err);
			}
		} else if (opt.equals("share")) {
			return share();
		}
		return null;
	}

	/**
	 * 分享
	 * 
	 * @return
	 */
	private String share() {
		String appid = "wx4f538df309ec657b";
		String secret = "bcea7ecebb0c3cf562895b71e8f7520c";
		String code = this.getString("code");
		JsonWrapper tokenJson = WeixinUtil.getAccessToken(appid, secret, code);
		if (tokenJson == null) {
			return "";
		}
		String access_token = tokenJson.getString("access_token");
		String openid = tokenJson.getString("openid");
		JsonWrapper userJson = WeixinUtil.getUserinfo(access_token, openid);

		// 注册用户
		RegInfo regInfo = new RegInfo();
		long maxId = 0;
		try {
			maxId = Manager.getInstance().generatePlayerId(userDao);
			Manager.getInstance().buildBaseUser(regInfo, "weixin", maxId);
			WeixinUtil.createRole(userJson, regInfo);
			this.userDao.addUser(regInfo);
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("WebWeixinAuth share err:", e);
		}

		return "";
	}

	@Override
	public void createRole(RegInfo regInfo, String info) throws Exception {
		JsonWrapper wrapper = new JsonWrapper(info);
		WeixinUtil.createRole(wrapper, regInfo);

	}

	@Override
	public Map<String, Object> refreshRole(RegInfo regInfo, String info) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		JsonWrapper wrapper = new JsonWrapper(info);
		String nickname = wrapper.getString("nickname");
		String headimgurl = wrapper.getString("headimgurl");
		String unionid = wrapper.getString("unionid");

		if (headimgurl==null){
			headimgurl="";
		}

		int sex = wrapper.getInt("sex", 1);
		if (sex == 0) {
			sex = 1;
		}

		if (StringUtils.isBlank(regInfo.getIdentity())) {
			map.put("identity", unionid);
			regInfo.setIdentity(unionid);
		}

		// String text = StringUtil.emojiConvert1(nickname);
		// System.out.println(nickname + "-" + text + "-" +
		// StringUtil.emojiRecovery2(text) + "-" + text.length());

		nickname = StringUtil.filterEmoji(nickname);
		if (!regInfo.getName().equals(nickname)) {
			map.put("name", nickname);
			regInfo.setName(nickname);
		}

        JsonWrapper jsonWrapper = new JsonWrapper(regInfo.getLoginExtend());
        if (!org.apache.commons.lang3.StringUtils.equals(nickname,jsonWrapper.getString("wx"))){
            jsonWrapper.putString("wx",nickname);
            regInfo.setLoginExtend(jsonWrapper.toString());
            map.put("loginExtend", regInfo.getLoginExtend());
        }

		if (regInfo.getSex() != sex) {
			map.put("sex", sex);
			regInfo.setSex(sex);
		}

		// if (StringUtils.isBlank(regInfo.getHeadimgurl()) ||
		// !regInfo.getHeadimgurl().equals(headimgurl)) {
		// map.put("headimgurl", headimgurl);
		// regInfo.setHeadimgurl(headimgurl);
		// }
//		boolean check = WeixinUtil.checkSaveHeadImg(regInfo, headimgurl);

		if (!headimgurl.equals(regInfo.getHeadimgurl())){
			map.put("headimgurl", headimgurl);
			map.put("headimgraw", MD5Util.getStringMD5(headimgurl));
			regInfo.setHeadimgurl(headimgurl);
		}

//		if (check) {
//			map.put("headimgurl", regInfo.getHeadimgurl());
//			map.put("headimgraw", MD5Util.getStringMD5(headimgurl));
//		}

		WeixinUtil.authorization(regInfo, unionid, false);
		return map;
	}

}
