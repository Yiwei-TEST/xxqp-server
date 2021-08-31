package com.sy.sanguo.game.service.pfs.apple;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONObject;
import com.sy.sanguo.game.bean.RegInfo;

import org.apache.commons.lang3.StringUtils;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.server.GameServerManager;
import com.sy.sanguo.common.server.PayBean;
import com.sy.sanguo.common.util.Base64Util;
import com.sy.sanguo.common.util.JacksonUtil;
import com.sy.sanguo.common.util.JsonWrapper;
import com.sy.sanguo.common.util.StringUtil;
import com.sy.sanguo.game.bean.OrderValidate;
import com.sy.sanguo.game.bean.OvaliMsg;
import com.sy.sanguo.game.service.BaseSdk;

public class Apple extends BaseSdk {
	private String productId;
	private static Map<String, String> productIds = new HashMap<String, String>();
	private static Map<String, Integer> productIdMap = new HashMap<String, Integer>();
	private static Map<String, String> productIds1 = new HashMap<String, String>();
	private static Map<String, Integer> productId1Map = new HashMap<String, Integer>();
	private static Map<String, String> productIds2 = new HashMap<String, String>();
	private static Map<String, Integer> productId2Map = new HashMap<String, Integer>();
	static {
		productIds.put("1200", "com.sy599.qp.paodekuai.m12");
		productIds.put("3000", "com.sy599.qp.paodekuai.m30");
		productIds.put("6000", "com.sy599.qp.paodekuai.m60");
		productIds.put("9800", "com.sy599.qp.paodekuai.m98");
		productIds.put("16800", "com.sy599.qp.paodekuai.m168");
		productIds.put("25800", "com.sy599.qp.paodekuai.m258");

		productIds1.put("1200", "com.xy.xunyouqipai.pay.m12");
		productIds1.put("3000", "com.xy.xunyouqipai.pay.m30");
		productIds1.put("6000", "com.xy.xunyouqipai.pay.m60");
		productIds1.put("9800", "com.xy.xunyouqipai.pay.m98");
		productIds1.put("16800", "com.xy.xunyouqipai.pay.m168");
		productIds1.put("25800", "com.xy.xunyouqipai.pay.m258");

		productIds2.put("1200", "com.qp.anhua.pay.m12");
		productIds2.put("3000", "com.qp.anhua.pay.m30");
		productIds2.put("6000", "com.qp.anhua.pay.m60");
		productIds2.put("9800", "com.qp.anhua.pay.m98");
		productIds2.put("16800", "com.qp.anhua.pay.m168");
		productIds2.put("25800", "com.qp.anhua.pay.m258");

		for (Entry<String, String> entry : productIds.entrySet()) {
			productIdMap.put(entry.getValue(), Integer.parseInt(entry.getKey()));
		}

		for (Entry<String, String> entry : productIds1.entrySet()) {
			productId1Map.put(entry.getValue(), Integer.parseInt(entry.getKey()));
		}

		for (Entry<String, String> entry : productIds2.entrySet()) {
			productId2Map.put(entry.getValue(), Integer.parseInt(entry.getKey()));
		}
	}

	@Override
	public String payExecute() {
		Map<String, Object> result = new HashMap<String, Object>();
		String msg = "";
		String receipt_data = this.getString("receipt_data");
		String order_id = this.getString("order_id");
		GameBackLogger.SYS_LOG.info(pf + " payExecute:orderId-->" + order_id);

		if (StringUtils.isBlank(receipt_data)) {
			result.put("code", -1);
			result.put("msg", "receipt-data is empty");
			GameBackLogger.SYS_LOG.error(pf + " payExecute receipt-data is empty");
			return JacksonUtil.writeValueAsString(result);
		}

		try {
			OrderValidate ov = orderValiDao.getOne(order_id);
			if (ov == null || ov.getStatus() != 0) {
				result.put("code", -2);
				result.put("msg", "ov is null");
				GameBackLogger.SYS_LOG.info(pf + " payExecute orderVali is null");
				return JacksonUtil.writeValueAsString(result);
			}
			Map<String, String> map = decode(receipt_data);
			String resp = null;
			int isSandbox = 0;
			if (map.containsKey("environment")) {
				String environment = map.get("environment");
				if (environment.equals("Sandbox")) {
					// 沙箱环境
					isSandbox = 1;
					resp = IapController.setIapCertificate(receipt_data, false);
					GameBackLogger.SYS_LOG.info(pf + " payExecute back Sandbox-->" + order_id + " resp:" + resp);
				} else {
					// 正式环境
					resp = IapController.setIapCertificate(receipt_data, true);
					GameBackLogger.SYS_LOG.info(pf + " payExecute back -->" + order_id + " resp:" + resp);
				}
			} else {
				resp = IapController.setIapCertificate(receipt_data, true);
				GameBackLogger.SYS_LOG.info(pf + " payExecute back -->" + order_id + " resp:" + resp);
			}
			if (!StringUtils.isBlank(resp)) {
				JsonWrapper wrapper = new JsonWrapper(resp);
				String status = wrapper.getString("status");
				if ("0".equals(status)) {// 验证通过
					JSONObject receipt = wrapper.getJson("receipt");
					String transaction_id = receipt.getString("transaction_id");// 订单ID从苹果返回信息中获取
					String product_id = receipt.getString("product_id");

					int money = 0;
					if (productIdMap.containsKey(product_id)) {
						money = productIdMap.get(product_id);
					} else if (productId1Map.containsKey(product_id)) {
						money = productId1Map.get(product_id);
					} else if (productId2Map.containsKey(product_id)) {
						money = productId2Map.get(product_id);
					}

					PayBean bean = null;
					if (money == 0) {
						bean = GameServerManager.getInstance().getIosPayBeanByProductId(product_id);
					} else {
						bean = GameServerManager.getInstance().getIosPayBeanByAmount(money / 100);

					}

					if (bean == null) {
						// bean = new PayBean();
						// bean.setAmount(money / 100);
						// bean.setId(1);
						// bean.setYuanbao(money / 300);
						// bean.setName(product_id);
						result.put("code", -2);
						result.put("msg", "money is err:" + money);
						GameBackLogger.SYS_LOG.info(pf + " money is err:" + money);
						return JacksonUtil.writeValueAsString(result);
					}

//					int code = payCards(ov.getPf(), transaction_id, ov.getFlat_id(), bean, "apple," + isSandbox);
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
						code = payCards(user,ov, transaction_id, bean, String.valueOf(user0.getUserId()));
						userInfo = user;
					} else {
						if (ov.getUserId()!=null&&ov.getUserId().longValue()>0L){
							userInfo = userDao.getUser(ov.getUserId());
						}else{
							userInfo = userDao.getUser(ov.getFlat_id(), ov.getPf());
						}
						code = payCards(userInfo,ov, transaction_id, bean,"apple," + isSandbox);
					}
					switch (code) {
					case 1:
						msg = "OK";
						break;
					case 2:
						msg = "fail";
						break;
					case 3:
						msg = "fail";
						break;
					case 0:// 充值成功
						msg = "OK";
						break;
					case -1:
						msg = "fail";
						break;
					}

					if ("OK".equals(msg)) {
						result.put("code", 0);
					} else {
						result.put("code", code);
					}

					GameBackLogger.SYS_LOG.info(pf + " pay orderid:" + order_id + ",code:" + code);
				} else {// 验证失败
					result.put("code", status);
					GameBackLogger.SYS_LOG.info(pf + " pay orderid:" + order_id + ",code:" + status);
				}
			}

		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error(pf + " payExecute get order error, orderid:" + order_id, e);
		}

		return JacksonUtil.writeValueAsString(result);
	}

	// base64
	public static String getBASE64(String s) {
		if (s == null)
			return null;
		return (new sun.misc.BASE64Encoder()).encode(s.getBytes());
	}

	public static Map<String, String> decode(String base64) {
		String msg = Base64Util.getFromBASE64(base64);
		if (msg.startsWith("{") && msg.endsWith("}")) {
			msg = msg.replace("{", "").replace("}", "").replace("\n\t", "");
		}
		String values[] = msg.split(";");
		Map<String, String> result = new HashMap<String, String>();
		for (String value : values) {
			if (value.contains("=")) {
				String[] valArr = value.split("=");
				String key = StringUtil.getValue(valArr, 0);
				if (!StringUtils.isBlank(key) && key.contains("\"")) {
					key = key.replace("\"", "").trim();
				}
				String val = StringUtil.getValue(valArr, 1);
				if (!StringUtils.isBlank(val) && val.contains("\"")) {
					val = val.replace("\"", "").trim();
				}
				result.put(key, val);
			}

		}
		return result;
	}

	@Override
	public String ovali() {
		Map<String, Object> result = new HashMap<String, Object>();
		String total_fee = this.getString("total_fee");
		String pname = this.getString("pname");
		if (StringUtils.isBlank(total_fee)) {
			result.put("code", -1);
			result.put("msg", "total_fee is empty");
			GameBackLogger.SYS_LOG.error(pf + " payExecute total_fee is empty");
			return JacksonUtil.writeValueAsString(result);
		}
		PayBean bean = GameServerManager.getInstance().getIosPayBean(pname,Integer.parseInt(total_fee) / 100);
		if (bean == null || !bean.getDesc().contains(".")) {
			Map<String, String> proMap = productIds;
			if (!StringUtils.isBlank(pname) && pname.equals("com.xy.xunyouqipai")) {
				proMap = productIds1;
			} else if (!StringUtils.isBlank(pname) && pname.equals("com.qp.anhua")) {
				proMap = productIds2;
			}

			if (proMap.get(total_fee) == null) {
				result.put("code", -2);
				result.put("msg", total_fee + "对应的productId不存在");
				GameBackLogger.SYS_LOG.error(pf + " payExecute " + total_fee + "对应的productId不存在");
				return JacksonUtil.writeValueAsString(result);
			}
			productId = proMap.get(total_fee);
		} else {
			productId = bean.getDesc();
		}

		OvaliMsg msg = ovaliComMsg();
		if (msg.getCode() == 0) {
			Map<String, String> urlPara = new HashMap<String, String>();
			result.put("code", msg.getCode());
			urlPara.put("productId", productId);
			urlPara.put("order_id", msg.getOv().getOrder_id());
			result.put("url", urlPara);
			GameBackLogger.SYS_LOG.info(pf + " result  : " + JacksonUtil.writeValueAsString(result));
		} else {
			result.put("code", msg.getCode());
			result.put("msg", msg.getMsg());
			GameBackLogger.SYS_LOG.error(pf + " payExecute error : " + msg.getMsg());
		}

		return JacksonUtil.writeValueAsString(result);
	}

	@Override
	protected void buildOrderVali(OrderValidate ov) {
		if (!StringUtils.isBlank(productId)) {
			String payChannel = ov.getPay_channel();
			if (StringUtils.isBlank(payChannel)) {
				payChannel = ",," + productId;
			} else {
				payChannel += "," + productId;
			}
			ov.setPay_channel(payChannel);
		}
	}

	@Override
	public String loginExecute() {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) {
		String base64 = "ewoJInNpZ25hdHVyZSIgPSAiQTB3b0hUVCtPUk8xQy9vR3JNYnBuaDhHZlhLdS9FRUh3WElXR0RuK3pKVEtnampGSlN1MU5nUFk0S0RnMDRieU9waDU0R3E2NlZtT0JRUllvWHZrMEJ3UThEQXYzL0UzOUNwamdpR3NBbGZLcVExS1loeUhjL2pYK1p5ZjBvb3huS29HakFuZHhudkcwcU5wSlM1TzlhbVBYV1ZjaXpnWEcveW5nSjFxZ2pyd3RkeTlpNys2QU41dVNncVlVMFZvRFN3S1lTOFJQK3VPMWN1U1Vacitmb0ZlcFJKVFJ6cmdmMDhNOC90L2ZjbURaNFZyNWM1S3EydDBPK1Z5TUNpczE3WDhaU1dBbEZnMWF2dzBoU005dEhuSU1rOWZudCsvZzVGTXU2LzRIYVk5QUlLOTVpcHZhOEhYWkU2ZnVTQ3JlTDVjM0diZk45aERtZXlTblhmNjVwUUFBQVdBTUlJRmZEQ0NCR1NnQXdJQkFnSUlEdXRYaCtlZUNZMHdEUVlKS29aSWh2Y05BUUVGQlFBd2daWXhDekFKQmdOVkJBWVRBbFZUTVJNd0VRWURWUVFLREFwQmNIQnNaU0JKYm1NdU1Td3dLZ1lEVlFRTERDTkJjSEJzWlNCWGIzSnNaSGRwWkdVZ1JHVjJaV3h2Y0dWeUlGSmxiR0YwYVc5dWN6RkVNRUlHQTFVRUF3dzdRWEJ3YkdVZ1YyOXliR1IzYVdSbElFUmxkbVZzYjNCbGNpQlNaV3hoZEdsdmJuTWdRMlZ5ZEdsbWFXTmhkR2x2YmlCQmRYUm9iM0pwZEhrd0hoY05NVFV4TVRFek1ESXhOVEE1V2hjTk1qTXdNakEzTWpFME9EUTNXakNCaVRFM01EVUdBMVVFQXd3dVRXRmpJRUZ3Y0NCVGRHOXlaU0JoYm1RZ2FWUjFibVZ6SUZOMGIzSmxJRkpsWTJWcGNIUWdVMmxuYm1sdVp6RXNNQ29HQTFVRUN3d2pRWEJ3YkdVZ1YyOXliR1IzYVdSbElFUmxkbVZzYjNCbGNpQlNaV3hoZEdsdmJuTXhFekFSQmdOVkJBb01Da0Z3Y0d4bElFbHVZeTR4Q3pBSkJnTlZCQVlUQWxWVE1JSUJJakFOQmdrcWhraUc5dzBCQVFFRkFBT0NBUThBTUlJQkNnS0NBUUVBcGMrQi9TV2lnVnZXaCswajJqTWNqdUlqd0tYRUpzczl4cC9zU2cxVmh2K2tBdGVYeWpsVWJYMS9zbFFZbmNRc1VuR09aSHVDem9tNlNkWUk1YlNJY2M4L1cwWXV4c1FkdUFPcFdLSUVQaUY0MWR1MzBJNFNqWU5NV3lwb041UEM4cjBleE5LaERFcFlVcXNTNCszZEg1Z1ZrRFV0d3N3U3lvMUlnZmRZZUZScjZJd3hOaDlLQmd4SFZQTTNrTGl5a29sOVg2U0ZTdUhBbk9DNnBMdUNsMlAwSzVQQi9UNXZ5c0gxUEttUFVockFKUXAyRHQ3K21mNy93bXYxVzE2c2MxRkpDRmFKekVPUXpJNkJBdENnbDdaY3NhRnBhWWVRRUdnbUpqbTRIUkJ6c0FwZHhYUFEzM1k3MkMzWmlCN2o3QWZQNG83UTAvb21WWUh2NGdOSkl3SURBUUFCbzRJQjF6Q0NBZE13UHdZSUt3WUJCUVVIQVFFRU16QXhNQzhHQ0NzR0FRVUZCekFCaGlOb2RIUndPaTh2YjJOemNDNWhjSEJzWlM1amIyMHZiMk56Y0RBekxYZDNaSEl3TkRBZEJnTlZIUTRFRmdRVWthU2MvTVIydDUrZ2l2Uk45WTgyWGUwckJJVXdEQVlEVlIwVEFRSC9CQUl3QURBZkJnTlZIU01FR0RBV2dCU0lKeGNKcWJZWVlJdnM2N3IyUjFuRlVsU2p0ekNDQVI0R0ExVWRJQVNDQVJVd2dnRVJNSUlCRFFZS0tvWklodmRqWkFVR0FUQ0IvakNCd3dZSUt3WUJCUVVIQWdJd2diWU1nYk5TWld4cFlXNWpaU0J2YmlCMGFHbHpJR05sY25ScFptbGpZWFJsSUdKNUlHRnVlU0J3WVhKMGVTQmhjM04xYldWeklHRmpZMlZ3ZEdGdVkyVWdiMllnZEdobElIUm9aVzRnWVhCd2JHbGpZV0pzWlNCemRHRnVaR0Z5WkNCMFpYSnRjeUJoYm1RZ1kyOXVaR2wwYVc5dWN5QnZaaUIxYzJVc0lHTmxjblJwWm1sallYUmxJSEJ2YkdsamVTQmhibVFnWTJWeWRHbG1hV05oZEdsdmJpQndjbUZqZEdsalpTQnpkR0YwWlcxbGJuUnpMakEyQmdnckJnRUZCUWNDQVJZcWFIUjBjRG92TDNkM2R5NWhjSEJzWlM1amIyMHZZMlZ5ZEdsbWFXTmhkR1ZoZFhSb2IzSnBkSGt2TUE0R0ExVWREd0VCL3dRRUF3SUhnREFRQmdvcWhraUc5Mk5rQmdzQkJBSUZBREFOQmdrcWhraUc5dzBCQVFVRkFBT0NBUUVBRGFZYjB5NDk0MXNyQjI1Q2xtelQ2SXhETUlKZjRGelJqYjY5RDcwYS9DV1MyNHlGdzRCWjMrUGkxeTRGRkt3TjI3YTQvdncxTG56THJSZHJqbjhmNUhlNXNXZVZ0Qk5lcGhtR2R2aGFJSlhuWTR3UGMvem83Y1lmcnBuNFpVaGNvT0FvT3NBUU55MjVvQVE1SDNPNXlBWDk4dDUvR2lvcWJpc0IvS0FnWE5ucmZTZW1NL2oxbU9DK1JOdXhUR2Y4YmdwUHllSUdxTktYODZlT2ExR2lXb1IxWmRFV0JHTGp3Vi8xQ0tuUGFObVNBTW5CakxQNGpRQmt1bGhnd0h5dmozWEthYmxiS3RZZGFHNllRdlZNcHpjWm04dzdISG9aUS9PamJiOUlZQVlNTnBJcjdONFl0UkhhTFNQUWp2eWdhWndYRzU2QWV6bEhSVEJoTDhjVHFBPT0iOwoJInB1cmNoYXNlLWluZm8iID0gImV3b0pJbTl5YVdkcGJtRnNMWEIxY21Ob1lYTmxMV1JoZEdVdGNITjBJaUE5SUNJeU1ERTJMVEE0TFRFNElESXdPalU1T2pNNElFRnRaWEpwWTJFdlRHOXpYMEZ1WjJWc1pYTWlPd29KSW5WdWFYRjFaUzFwWkdWdWRHbG1hV1Z5SWlBOUlDSmtOV1F5TTJJMlpUUXhOek15TWpJMlpEQXpZMlJoWkRrMk1UZzBZV0kzTVRBNFptTm1PVEZtSWpzS0NTSnZjbWxuYVc1aGJDMTBjbUZ1YzJGamRHbHZiaTFwWkNJZ1BTQWlNVEF3TURBd01ESXpNRFUyTmprMU15STdDZ2tpWW5aeWN5SWdQU0FpTVM0eExqSWlPd29KSW5SeVlXNXpZV04wYVc5dUxXbGtJaUE5SUNJeE1EQXdNREF3TWpNd05UWTJPVFV6SWpzS0NTSnhkV0Z1ZEdsMGVTSWdQU0FpTVNJN0Nna2liM0pwWjJsdVlXd3RjSFZ5WTJoaGMyVXRaR0YwWlMxdGN5SWdQU0FpTVRRM01UVTNPVEUzT0RZMk55STdDZ2tpZFc1cGNYVmxMWFpsYm1SdmNpMXBaR1Z1ZEdsbWFXVnlJaUE5SUNKQ1F6TTNRVFZDUXkxRVFqTkZMVFE0TTBNdFFqWkZOUzFET1VGRU1EUTVOak14TXpjaU93b0pJbkJ5YjJSMVkzUXRhV1FpSUQwZ0ltTnZiUzV6ZVRVNU9TNXhjQzV3WVc5a1pXdDFZV2t1YlRNd0lqc0tDU0pwZEdWdExXbGtJaUE5SUNJeE1UUTFNVGN5T0RrNElqc0tDU0ppYVdRaUlEMGdJbU52YlM1emVUVTVPUzV4Y0M1d1lXOWtaV3QxWVdraU93b0pJbkIxY21Ob1lYTmxMV1JoZEdVdGJYTWlJRDBnSWpFME56RTFOemt4TnpnMk5qY2lPd29KSW5CMWNtTm9ZWE5sTFdSaGRHVWlJRDBnSWpJd01UWXRNRGd0TVRrZ01ETTZOVGs2TXpnZ1JYUmpMMGROVkNJN0Nna2ljSFZ5WTJoaGMyVXRaR0YwWlMxd2MzUWlJRDBnSWpJd01UWXRNRGd0TVRnZ01qQTZOVGs2TXpnZ1FXMWxjbWxqWVM5TWIzTmZRVzVuWld4bGN5STdDZ2tpYjNKcFoybHVZV3d0Y0hWeVkyaGhjMlV0WkdGMFpTSWdQU0FpTWpBeE5pMHdPQzB4T1NBd016bzFPVG96T0NCRmRHTXZSMDFVSWpzS2ZRPT0iOwoJImVudmlyb25tZW50IiA9ICJTYW5kYm94IjsKCSJwb2QiID0gIjEwMCI7Cgkic2lnbmluZy1zdGF0dXMiID0gIjAiOwp9";
		Map<String, String> map = decode(base64);

		// {"receipt":{"original_purchase_date_pst":"2016-08-18 20:59:38 America/Los_Angeles",
		// "purchase_date_ms":"1471579178667",
		// "unique_identifier":"d5d23b6e41732226d03cdad96184ab7108fcf91f",
		// "original_transaction_id":"1000000230566953", "bvrs":"1.1.2",
		// "transaction_id":"1000000230566953", "quantity":"1",
		// "unique_vendor_identifier":"BC37A5BC-DB3E-483C-B6E5-C9AD04963137",
		// "item_id":"1145172898", "product_id":"com.sy599.qp.paodekuai.m30",
		// "purchase_date":"2016-08-19 03:59:38 Etc/GMT",
		// "original_purchase_date":"2016-08-19 03:59:38 Etc/GMT",
		// "purchase_date_pst":"2016-08-18 20:59:38 America/Los_Angeles",
		// "bid":"com.sy599.qp.paodekuai",
		// "original_purchase_date_ms":"1471579178667"}, "status":0}

		if (map.containsKey("environment")) {
			String environment = map.get("environment");
			String res;
			if (environment.equals("Sandbox")) {
				// 沙箱环境
				res = IapController.setIapCertificate(base64, false);
			} else {
				// 正式环境
				res = IapController.setIapCertificate(base64, true);
			}
			System.out.println(res);
		}
		System.out.println(JacksonUtil.writeValueAsString(decode(base64)));
	}
}
