package com.sy.sanguo.game.service.channel.aibei;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.sy.sanguo.game.bean.RegInfo;

import org.apache.commons.lang.StringUtils;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.server.GameServerManager;
import com.sy.sanguo.common.server.PayBean;
import com.sy.sanguo.common.util.JacksonUtil;
import com.sy.sanguo.common.util.StringUtil;
import com.sy.sanguo.game.bean.OrderValidate;
import com.sy.sanguo.game.bean.OvaliMsg;
import com.sy.sanguo.game.service.BaseSdk;
import com.sy.sanguo.game.service.channel.aibei.sign.BareBonesBrowserLaunch;
import com.sy.sanguo.game.service.channel.aibei.sign.HttpUtils;
import com.sy.sanguo.game.service.channel.aibei.sign.SignHelper;
import com.sy.sanguo.game.service.channel.aibei.sign.SignUtils;

public class Aibei extends BaseSdk {

	// 应用名称：大天使挂机
	// 应用编号（APP_ID）：3005638180
	// 应用私钥（APPV_KEY）：MIICWwIBAAKBgQCBuhhIeSP8CBiGq1t2URrtkJ6z3IEOMuzpHlKpyCwGNPew3QruDrCMcLnqJK0TwRfCeaSTrNi5gxSJugBA/3EL0POS+icDAbgbZMMAQCqPSTopa4V1thjH0De0ptjIp/R6CJe1mfuR+rSZcnD7f4k03ADVZhTNAaHzI07bwJJPpwIDAQABAoGALlrK9oqi+xoBeY2dnweYMa9tpiNy0hoMYbh+HUDzyjv/DenOUAZbu7NEG5CDb/2dedZxonsu7gsSuawHDzK3t9jtSdyZKTD6vfGLIIikqhUORj/oFRzDIurM7PSL4ixAzwvvwJ6mENrncHr57rLZAdBvP9UhVTTTcndxsk95FBECQQC50S1bP3R1C+5D65AAirZ5wfEOtZa06/Mt5qlgXrux5ABL/Yczq4VMQz5jefqpjX6I4JkYl678G+rPPzsyRCDbAkEAsrmCTpEWwfoAFhhI6ycLAxt7kayjSTwNFhuGWdEacxG6upUD77IRsw2C/6JOuiHg8VzLasuEJPTHbXyNzlmwJQJAdSKAphHVNuejdltrdnv61bxFWlFsRHas4FWUljSBu30QFtcmQJMyP0XwcUL2maWhi2WR/Oe5niF3HtgsV96MmwJAMHJS3UM9HQXMojChC4VA2e3IODvI42Aw1+5MI+qntct3h34/W+k0HQormWoA1zm5xRN3XJKgVng32vjpY6cyNQJAGeIhthe8Fu/1ixGeGuotDIeWtj4uot5Yz4UplaGN/hi7DoMnb5vtaHcLPmwCJiII5sgAKOa+gxIy9zVdkLLXcg==
	// 平台公钥（PLATP_KEY）：MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCdjRBVcF+Ha0h68e8vkom74UVOYHLSNRDVL3LCkmw6ORvn3jnmXHhYUF4I2JtrkQXbkvg6pdwH40wyDdMUVWvSitrl40HAnxaUOKNGXSoLlE6mLCBfdguQo+LR8vhS11EkOHiwt2nklqlDuIbi+eLBNv/f0FlYkmOfmjp3MtNG8QIDAQAB
	private static String appId = "3005638180";
	// private static String APPV_KEY =
	// "MIICWwIBAAKBgQCBuhhIeSP8CBiGq1t2URrtkJ6z3IEOMuzpHlKpyCwGNPew3QruDrCMcLnqJK0TwRfCeaSTrNi5gxSJugBA/3EL0POS+icDAbgbZMMAQCqPSTopa4V1thjH0De0ptjIp/R6CJe1mfuR+rSZcnD7f4k03ADVZhTNAaHzI07bwJJPpwIDAQABAoGALlrK9oqi+xoBeY2dnweYMa9tpiNy0hoMYbh+HUDzyjv/DenOUAZbu7NEG5CDb/2dedZxonsu7gsSuawHDzK3t9jtSdyZKTD6vfGLIIikqhUORj/oFRzDIurM7PSL4ixAzwvvwJ6mENrncHr57rLZAdBvP9UhVTTTcndxsk95FBECQQC50S1bP3R1C+5D65AAirZ5wfEOtZa06/Mt5qlgXrux5ABL/Yczq4VMQz5jefqpjX6I4JkYl678G+rPPzsyRCDbAkEAsrmCTpEWwfoAFhhI6ycLAxt7kayjSTwNFhuGWdEacxG6upUD77IRsw2C/6JOuiHg8VzLasuEJPTHbXyNzlmwJQJAdSKAphHVNuejdltrdnv61bxFWlFsRHas4FWUljSBu30QFtcmQJMyP0XwcUL2maWhi2WR/Oe5niF3HtgsV96MmwJAMHJS3UM9HQXMojChC4VA2e3IODvI42Aw1+5MI+qntct3h34/W+k0HQormWoA1zm5xRN3XJKgVng32vjpY6cyNQJAGeIhthe8Fu/1ixGeGuotDIeWtj4uot5Yz4UplaGN/hi7DoMnb5vtaHcLPmwCJiII5sgAKOa+gxIy9zVdkLLXcg==";
	private static String APPV_KEY = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIG6GEh5I/wIGIarW3ZRGu2QnrPcgQ4y7OkeUqnILAY097DdCu4OsIxwueokrRPBF8J5pJOs2LmDFIm6AED/cQvQ85L6JwMBuBtkwwBAKo9JOilrhXW2GMfQN7Sm2Min9HoIl7WZ+5H6tJlycPt/iTTcANVmFM0BofMjTtvAkk+nAgMBAAECgYAuWsr2iqL7GgF5jZ2fB5gxr22mI3LSGgxhuH4dQPPKO/8N6c5QBlu7s0QbkINv/Z151nGiey7uCxK5rAcPMre32O1J3JkpMPq98YsgiKSqFQ5GP+gVHMMi6szs9IviLEDPC+/AnqYQ2udwevnustkB0G8/1SFVNNNyd3GyT3kUEQJBALnRLVs/dHUL7kPrkACKtnnB8Q61lrTr8y3mqWBeu7HkAEv9hzOrhUxDPmN5+qmNfojgmRiXrvwb6s8/OzJEINsCQQCyuYJOkRbB+gAWGEjrJwsDG3uRrKNJPA0WG4ZZ0RpzEbq6lQPvshGzDYL/ok66IeDxXMtqy4Qk9MdtfI3OWbAlAkB1IoCmEdU256N2W2t2e/rVvEVaUWxEdqzgVZSWNIG7fRAW1yZAkzI/RfBxQvaZpaGLZZH857meIXce2CxX3oybAkAwclLdQz0dBcyiMKELhUDZ7cg4O8jjYDDX7kwj6qe1y3eHfj9b6TQdCiuZagDXObnFE3dckqBWeDfa+OljpzI1AkAZ4iG2F7wW7/WLEZ4a6i0Mh5a2Pi6i3ljPhSmVoY3+GLsOgydvm+1odws+bAImIgjmyAAo5r6DEjL3NV2Qstdy";
	private static String PLATP_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCdjRBVcF+Ha0h68e8vkom74UVOYHLSNRDVL3LCkmw6ORvn3jnmXHhYUF4I2JtrkQXbkvg6pdwH40wyDdMUVWvSitrl40HAnxaUOKNGXSoLlE6mLCBfdguQo+LR8vhS11EkOHiwt2nklqlDuIbi+eLBNv/f0FlYkmOfmjp3MtNG8QIDAQAB";

	@Override
	public String loginExecute() {
		return null;
	}

	@Override
	public String ovali() {
		String redirecturl = this.getString("redirecturl");
		Map<String, Object> result = new HashMap<String, Object>();
		OvaliMsg msg = ovaliComMsg();
		if (msg.getCode() == 0) {
			OrderValidate ov = msg.getOv();
			String requesturl = getRequest().getRequestURL().toString();
			String[] values = ov.getPay_channel().split(",");
			// String payChannel = StringUtil.getValue(values, 0);
			String notify = requesturl.replace("support!ovali_com", "pay!aibei");
			String transid = CheckSign(appId, ov.getItem_id(), msg.getPayItem().getName(), ov.getOrder_id(), msg.getPayItem().getAmount(), ov.getFlat_id(),
					ov.getPay_channel(), notify);

			if (StringUtils.isBlank(transid)) {
				result.put("code", -2);
				result.put("msg", "transid is null");
			} else {
				Map<String, String> url = new HashMap<String, String>();

				// String sign = SignHelper.sign(transid + redirecturl,
				// APPV_KEY);iframe签名方式
				url.put("transId", transid);
				url.put("redirecturl", redirecturl);
				url.put("cpurl", "");
				String param = "{transid:\"" + transid + "\",redirecturl:\"" + redirecturl + "\",cpurl:\"\"}";
				String sign = SignHelper.sign(param, APPV_KEY);// 挑战签名方式
				url.put("sign", sign);
				result.put("code", msg.getCode());
				result.put("url", url);
			}

		} else {
			result.put("code", msg.getCode());
			result.put("msg", msg.getMsg());
		}
		return JacksonUtil.writeValueAsString(result);
	}

	private static String ReqData(String appid, int waresid, String waresname, String cporderid, float price, String appuserid, String cpprivateinfo, String notifyurl) {
		String json;
		json = "appid:";
		json += appid;
		json += " userid:";
		json += appuserid;
		json += " waresid:";
		json += waresid;
		json += "cporderid:";
		json += cporderid;

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("appid", appid);
		jsonObject.put("waresid", waresid);
		jsonObject.put("cporderid", cporderid);
		jsonObject.put("currency", "RMB");
		jsonObject.put("appuserid", appuserid);
		// 以下是参数列表中的可选参数
		if (!waresname.isEmpty()) {
			jsonObject.put("waresname", waresname);
		}
		/*
		 * 当使用的是 开放价格策略的时候 price的值是 程序自己 设定的价格，使用其他的计费策略的时候 price 不用传值
		 */
		jsonObject.put("price", price);
		if (!cpprivateinfo.isEmpty()) {
			jsonObject.put("cpprivateinfo", cpprivateinfo);
		}
		if (!notifyurl.isEmpty()) {
			/*
			 * 如果此处不传同步地址，则是以后台传的为准。
			 */
			jsonObject.put("notifyurl", notifyurl);
		}
		String content = jsonObject.toString();// 组装成 json格式数据
		// 调用签名函数 重点注意： 请一定要阅读 sdk
		// 包中的爱贝AndroidSDK3.4.4\03-接入必看-服务端接口说明及范例\爱贝服务端接入指南及示例0311\IApppayCpSyncForJava
		// \接入必看.txt
		String sign = SignHelper.sign(content, APPV_KEY);
		String data = "transdata=" + content + "&sign=" + sign + "&signtype=RSA";// 组装请求参数
		return data;
	}

	// 数据验签
	private static String CheckSign(String appid, int waresid, String waresname, String cporderid, float price, String appuserid, String cpprivateinfo, String notifyurl) {
		String reqData = ReqData(appid, waresid, waresname, cporderid, price, appuserid, cpprivateinfo, notifyurl);
		String respData = HttpUtils.sentPost("http://ipay.iapppay.com:9999/payapi/order", reqData, "UTF-8"); // 请求验证服务端

		/*---------------------------------------------如果得到成功响应的结果-----------------------------------------------------------*/
		// 解析结果 得到的 数据为一个以&分割的字符串，需要分成三个部分transdata，sign，signtype。
		// 成功示例：respData ==
		// "transdata={"transid":"32011501141440430237"}&sign=NJ1qphncrBZX8nLjonKk2tDIKRKc7vHNej3e/jZaXV7Gn/m1IfJv4lNDmDzy88Vd5Ui1PGMGvfXzbv8zpuc1m1i7lMvelWLGsaGghoXi0Rk7eqCe6tpZmciqj1dCojZoi0/PnuL2Cpcb/aMmgpt8LVIuebYcaFVEmvngLIQXwvE=&signtype=RSA"

		Map<String, String> reslutMap = SignUtils.getParmters(respData);
		// String transdata = null;
		String transid = "";
		String signtype = reslutMap.get("signtype"); // "RSA";

		/*
		 * 调用验签接口
		 * 
		 * 主要 目的 确定 收到的数据是我们 发的数据，是没有被非法改动的
		 */
		if (SignHelper.verify(reslutMap.get("transdata"), reslutMap.get("sign"), PLATP_KEY)) {
			System.out.println(reslutMap.get("transdata"));
			System.out.println(reslutMap.get("sign"));
			JSONObject json = JSONObject.parseObject(reslutMap.get("transdata"));
			transid = json.getString("transid");
			GameBackLogger.SYS_LOG.info("verify ok");
		} else {
			GameBackLogger.SYS_LOG.info("aibei verify fail" + JacksonUtil.writeValueAsString(reslutMap));
		}

		return transid;
	}

	// 当客户端上使用H5 的时候下面的示例代码可以有所帮助。

	public static void H5orPCpay(String transid) {
		String pcurl = "https://web.iapppay.com/pc/exbegpay?";
		String h5url = "https://web.iapppay.com/h5/exbegpay?";
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("transid", transid);
		jsonObject.put("redirecturl", "http://58.250.160.241:8888/IapppayCpSyncForPHPDemo/Test.php");
		jsonObject.put("cpurl", "http://58.250.160.241:8888/IapppayCpSyncForPHPDemo/Test.php");
		String content = jsonObject.toString();
		String sign = SignHelper.sign(content, APPV_KEY);
		String data = "transdata=" + URLEncoder.encode(content) + "&sign=" + URLEncoder.encode(sign) + "&signtype=RSA";
		System.out.println("可以直接在浏览器中访问该链接:" + h5url + data);// 我们的常连接版本 有PC 版本
		// 和移动版本。
		// 根据使用的环境不同请更换相应的URL:h5url,pcurl.
		String url = pcurl + data; // String url=pcurl+data; 可以直接更换
		// url=pcurl+data中的pcurl
		// 为h5url，即可在手机浏览器中调出移动版本的收银台。
		BareBonesBrowserLaunch.openURL(url);
	}

	@Override
	public String payExecute() {
		String result = "FAIL";
		try {
			request.setCharacterEncoding("utf-8");
			GameBackLogger.SYS_LOG.info(pf + " payExecute :" + JacksonUtil.writeValueAsString(request.getParameterMap()));
			String transdata = this.getString("transdata");
			String sign = this.getString("sign");
			String signtype = this.getString("signtype");
			if (signtype == null) {
				GameBackLogger.SYS_LOG.error(pf + " payExecute signtype null");
				return "FAIL";
			}
			/*
			 * 调用验签接口 主要 目的 确定 收到的数据是我们 发的数据，是没有被非法改动的
			 */
			if (!SignHelper.verify(transdata, sign, PLATP_KEY)) {
				GameBackLogger.SYS_LOG.error(pf + " payExecute verify err");
				return "FAIL";
			}

			// 支付成功
			transdata = URLDecoder.decode(transdata, "utf-8");

			JSONObject data = JSONObject.parseObject(transdata);
			String cporderid = data.getString("cporderid");
			String money = data.getString("money");
			String transid = data.getString("transid");

			OrderValidate ov = orderValiDao.getOne(cporderid);
			if (ov == null || ov.getStatus() != 0) {
				GameBackLogger.SYS_LOG.info(pf + " payExecute orderVali is null");
				return "FAIL";
			}

			String pf = ov.getPf();
			String[] payChannel = ov.getPay_channel().split(",");
			String channel = StringUtil.getValue(payChannel, 0);

			if (!StringUtils.isBlank(channel)&&!channel.equals("null")) {
				pf = pf + channel;
			}
			PayBean bean = GameServerManager.getInstance().getPayBean(ov.getItem_id());
			// 支付钱是否对应上
			if (bean.getAmount() != Float.parseFloat(money)) {
				GameBackLogger.SYS_LOG.info(pf + " payExecute amount is err");
				return "FAIL";
			}

//			int code = payCards(pf, transid, ov.getFlat_id(), bean);
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
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error(pf + " pay err:", e);
		}

		return result;
	}

	public static void main(String[] args) {
		Map<String, String> url = new HashMap<String, String>();

		// String sign = SignHelper.sign(transid + redirecturl,
		// APPV_KEY);iframe签名方式
		url.put("transid", "32281606271015442825");
		url.put("redirecturl", "http://testxsg.sy599.com/qiji_egret_sdk/src/sdk/webAibeiPayCallBack.html");
		url.put("cpurl", "");
		String transId = "32281606271015442825";
		String redirecturl = "http://testxsg.sy599.com/qiji_egret_sdk/src/sdk/webAibeiPayCallBack.html";

		String param = "{transid:\"" + transId + "\",redirecturl:\"" + redirecturl + "\",cpurl:\"\"}";

		System.out.println(param);
		String sign = SignHelper.sign(param, APPV_KEY);// 挑战签名方式
		// url.put("sign", sign);
		System.out.println(sign);

	}
}
