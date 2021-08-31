package com.sy.sanguo.common.util.qqbrowser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import com.sy.sanguo.game.service.SysInfManager;

public class X5Service {

	// 请CP配置以下5个参数
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// private static final String X5_APPID = "2466856218";
	// private static final String X5_APP_KEY = "PoltShYwh0uunguq";
	// private static final String X5_TRANSFER_KEY = "vS99f24SlVeWmmrg";

	public static String X5_APPID = "8254853885";
	public static String X5_APP_KEY = "OSmb6MqIUoxt92sq";
	private static String X5_TRANSFER_KEY = "a1MRjc6DbRxyslP0";
	private static String login = "gjqqbLogin";
	static {
		if (SysInfManager.isMyz()) {
			X5_APPID = "8335658517";
			X5_APP_KEY = "ApqPRuuSWrkiNwBt";
			X5_TRANSFER_KEY = "ptsbsQRjyhvzKfWa";
			login = "dqmyzlogin";
			System.out.println("myz " + X5_APPID);
		}else if(SysInfManager.isQiji()){
			X5_APPID = "9162226655";
			X5_APP_KEY = "whWvSEBDedCxGpuW";
			X5_TRANSFER_KEY = "AEFVTjarRDWETdRQ";
			login = "qjh5login";
			System.out.println("qiji " + X5_APPID);
			
		}
	}

	// GET + 批价回调URL去掉前缀 + 链接本身参数（比如，http://xxx.com/x5/price?action=inquiry,
	// 其中?后的action=inquiry表示此参数，此时第三个参数为"action=inquiry"，否则第三个参数为""）
	private static final String[] PRICE_URL = { "GET", "/" + login + "/qbprice.guajilogin", "" };

	// GET + 发货回调URL去掉前缀 + 链接本身参数（比如，http://xxx.com/x5/pay?action=inquiry,
	// 其中?后的action=inquiry表示此参数，此时第三个参数为"action=inquiry"，否则第三个参数为""）
	private static final String[] PAY_URL = { "GET", "/" + login + "/qbpay.guajilogin", "" };

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static final String ALGORITHM_KEY = "AES";
	private static final String ALGORITHM = "AES/ECB/NoPadding";
	private static final int BLOCK_SIZE = 16;
	private static final byte PADDING = 0x00;

	public static void log(String str) {
		// System.out.println(str);
	}

	public static String getRandomString(int length) {
		String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < length; ++i) {
			int number = random.nextInt(str.length());

			sb.append(str.charAt(number));
		}
		return sb.toString();
	}

	private static byte[] pad(byte[] s) {
		byte[] ret = new byte[s.length + (BLOCK_SIZE - s.length % BLOCK_SIZE)];
		int i = 0;
		for (; i < s.length; ++i) {
			ret[i] = s[i];
		}

		for (; i < ret.length; ++i) {
			ret[i] = PADDING;
		}
		return ret;
	}

	private static byte[] AESEncrypt(byte[] data) {
		byte[] ret = null;
		try {
			Key k = toKey(X5_TRANSFER_KEY);
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, k);
			ret = cipher.doFinal(pad(data));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/** 创建密钥 **/
	private static Key toKey(String password) {
		return new SecretKeySpec(password.getBytes(), ALGORITHM_KEY);
	}

	private static String parseByte2HexStr(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1) {
				hs = hs + "0" + stmp;
			} else {
				hs = hs + stmp;
			}
		}
		return hs.toLowerCase();
	}

	private static byte[] parseHexStr2Byte(String strhex) {
		if (strhex == null) {
			return null;
		}
		int l = strhex.length();
		if (l % 2 == 1) {
			return null;
		}
		byte[] b = new byte[l / 2];
		for (int i = 0; i != l / 2; i++) {
			b[i] = (byte) Integer.parseInt(strhex.substring(i * 2, i * 2 + 2), 16);
		}
		return b;
	}

	private static byte[] AESDecrypt(byte[] data) {
		byte[] ret = null;
		try {
			Key k = toKey(X5_TRANSFER_KEY);
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, k);
			ret = cipher.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/*
	 * Java doc:
	 * https://docs.oracle.com/javase/7/docs/api/java/net/URLEncoder.html
	 * #encode(java.lang.String,%20java.lang.String)
	 * 
	 * Utility class for HTML form encoding. This class contains static methods
	 * for converting a String to the application/x-www-form-urlencoded MIME
	 * format. For more information about HTML form encoding, consult the HTML
	 * specification. When encoding a String, the following rules apply:
	 * 
	 * The alphanumeric characters "a" through "z", "A" through "Z" and "0"
	 * through "9" remain the same. The special characters ".", "-", "*", and
	 * "_" remain the same. The space character " " is converted into a plus
	 * sign "+". All other characters are unsafe and are first converted into
	 * one or more bytes using some encoding scheme. Then each byte is
	 * represented by the 3-character string "%xy", where xy is the two-digit
	 * hexadecimal representation of the byte. The recommended encoding scheme
	 * to use is UTF-8. However, for compatibility reasons, if an encoding is
	 * not specified, then the default encoding of the platform is used. For
	 * example using UTF-8 as the encoding scheme the string
	 * "The string ü@foo-bar" would get converted to
	 * "The+string+%C3%BC%40foo-bar" because in UTF-8 the character ü is encoded
	 * as two bytes C3 (hex) and BC (hex), and the character @ is encoded as one
	 * byte 40 (hex).
	 */
	// 腾讯使用的是《RFC 3986 section 2.3》编码规范，除了以下几种字符不编码，其他字符都需要被编码为%开头的串
	// unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~"
	public static String x5_quote(String url) {
		String ret = null;
		try {
			ret = URLEncoder.encode(url, "UTF-8");
			ret = ret.replace("%7E", "~"); // 不编码"~"
			ret = ret.replace("+", "%20"); // 编码空格为%20
			ret = ret.replace("*", "%2A"); // 编码"*"为%2A
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			ret = null;
		}
		log("URL Encode后:" + ret);
		return ret;
	}

	public static String x5_unquote(String url) {
		String ret = null;
		try {
			ret = URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		log("URL Decode后:" + ret);
		return ret;
	}

	public static byte[] base64Decode(String base64Str) {
		byte[] ret = null;
		try {
			ret = Base64.decode(base64Str, Base64.DEFAULT);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public static String base64Encode(byte[] bytes) {
		return new String(Base64.encode(bytes, Base64.NO_WRAP));
	}

	private static String x5_encrypt(String data) {
		log("AES加密前:" + data);
		byte[] encrypted = AESEncrypt(data.getBytes());
		log("AES加密后16进制:" + parseByte2HexStr(encrypted));
		String base64 = base64Encode(encrypted);
		log("base64编码后:" + base64);
		String encoded = x5_quote(base64);
		return encoded;
	}

	private static String x5_decrypt(String data) {
		String decoded = x5_unquote(data);
		log("AES解密前:" + data);
		byte[] binary = base64Decode(decoded);
		log("base64解码后的16进制:" + parseByte2HexStr(binary));
		String originData = new String(AESDecrypt(binary));
		log("AES解密后:" + originData);
		return originData;
	}

	private static String x5_hmac_base64(byte[] bytes) {
		SecretKeySpec keySpec = new SecretKeySpec((X5_APP_KEY + "&").getBytes(), "HmacSHA1");
		String ret = null;
		try {
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(keySpec);
			byte[] result = mac.doFinal(bytes);
			ret = base64Encode(result);
			System.out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	// 添加腾讯要求的前缀字段后，做URL编码
	private static String x5_encode_url(String[] prefix, String data) {
		String ret = null;
		try {
			String request_mode = prefix[0];
			String request_path = prefix[1];
			String request_arg = null;
			if (!prefix[2].isEmpty()) {
				request_arg = prefix[2] + "&data=" + data;
			} else {
				request_arg = "data=" + data;
			}
			ret = x5_quote(request_mode) + "&" + x5_quote(request_path) + "&" + x5_quote(request_arg);
		} catch (Exception e) {
			e.printStackTrace();
			ret = null;
		}

		return ret;
	}

	// 添加"批价"前缀字段后，做URL编码
	private static String x5_encode_url_price(String url) {
		String r = x5_encode_url(PRICE_URL, url);
		log("加GET&/x5/price/&data=后:" + r);
		return r;
	}

	// 添加"发货"前缀字段后，做URL编码
	private static String x5_encode_url_pay(String url) {
		String r = x5_encode_url(PAY_URL, url);
		log("加GET&/x5/pay/&data=后:" + r);
		return r;
	}

	// 批价回调里用的签名函数
	public static String x5_sign_price_data(String data) {
		log("原数据:" + data);
		String r = x5_encode_url_price(data);
		r = x5_hmac_base64(r.getBytes());
		r = x5_quote(r);
		return r;
	}

	// 发货回调里用的签名函数
	public static String x5_sign_pay_data(String data) {
		log("原数据:" + data);
		String r = x5_encode_url_pay(data);
		r = x5_hmac_base64(r.getBytes());
		r = x5_quote(r);
		return r;
	}

	// 检测批价的签名是否正确
	public static boolean x5_is_correct_of_price_sign(String data, String sign) {
		boolean ret = false;
		String calc_sign = x5_sign_price_data(data);
		log("正确的:" + sign);
		log("计算的:" + calc_sign);
		ret = sign.equals(calc_sign) ? true : false;
		if (ret) {
			log("签名验证成功!");
		} else {
			log("签名验证失败!");
		}
		return ret;
	}

	// 检测发货的签名是否正确
	public static boolean x5_is_correct_of_pay_sign(String data, String sign) {
		boolean ret = false;
		String calc_sign = x5_sign_pay_data(data);
		log("正确的:" + sign);
		log("计算的:" + calc_sign);
		ret = sign.equals(calc_sign) ? true : false;
		if (ret) {
			log("签名验证成功!");
		} else {
			log("签名验证失败!");
		}
		return ret;
	}

	// 生成批价响应数据，返回json格式
	public static String x5_price_response(JSONObject obj) {
		String data = obj.toString();
		// 测试数据:
		// data =
		// "{\"msg\": \"success\", \"nonce\": \"NphsHyYDljzEVLn\", \"time\": 1430404481, \"ret\": 0, \"payamount\": 10}";
		// 用此测试数据返回的结果为: 批价响应数据:{"rspsig": "eZR%2BhSmVn07Ysswa6GyMM%2FWjrI8%3D",
		// "data":
		// "pwSvgzN0vJieacYw7z8SBLP8INLmWY%2Bq2o74E0%2BBEAYi5OhHMq9Ndo5c1EEa9RBHpzjxGKRsbfIt10acMiT%2Bv7k1VoEukIaJPa1T49R0KN54oRW436bHJl%2BSsngqvSuC"}
		System.out.println("==> obj: " + data);
		String encrytedData = x5_encrypt(data);
		String sign = x5_sign_price_data(encrytedData);

		JSONObject ret = new JSONObject();
		try {
			ret.put("data", encrytedData);
			ret.put("rspsig", sign);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		log("批价响应数据:" + ret.toString());
		return ret.toString();
	}

	// 生成发货响应数据，返回json格式
	public static String x5_pay_response(JSONObject obj) {
		String data = obj.toString();
		// 测试数据:
		// data =
		// "{\"nonce\": \"xnqScIyYzuDgvGj\", \"ret\": 0, \"time\": 1430404489}";
		// 用此测试数据返回的结果为: 发货响应数据:{"rspsig": "ZxGojeCwrOpJH2eIbmUObTzHHIo%3D",
		// "data":
		// "GVXnSvyysWVKS0c5QaHNDbeYMHRgl99qoRv4hAOXzp8gLtR%2F5XMg7CBK5%2FcmuhDrL3t%2BxEJrouNRs7eNK1p%2FwA%3D%3D"}

		System.out.println("==> obj: " + data);
		String encrytedData = x5_encrypt(data);
		String sign = x5_sign_pay_data(encrytedData);

		JSONObject ret = new JSONObject();
		try {
			ret.put("data", encrytedData);
			ret.put("rspsig", sign);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		log("发货响应数据:" + ret.toString());
		return ret.toString();
	}

	// 获取回调函数的参数，返回JSONObject
	public static JSONObject x5_get_decoded_args(String data) {
		log("正在参数解码...");
		log("原数据:" + data);

		JSONObject argObj = new JSONObject();
		String decrypted_data = x5_decrypt(data);
		String[] arg_list = decrypted_data.split("&");
		for (String arg : arg_list) {
			String[] pair = arg.split("=", 2);
			if (pair.length != 2) {
				log("ERROR: pair size is wrong, it needs to be 2 while now is " + pair.length + "!");
				log("ERROR: arg:" + arg + ",pair:" + pair);
				return null;
			}
			try {
				argObj.put(pair[0].trim(), pair[1].trim());
			} catch (JSONException e) {
				e.printStackTrace();
				argObj = null;
			}
		}
		return argObj;
	}

	public static String x5_sign_appsigdata(String data) {
		log("x5_sign_appsigdata, 原数据:" + data);
		data = x5_hmac_base64(data.getBytes());
		data = x5_quote(data);
		return data;
	}

	// 获取游戏登陆信息
	public static Map<String, String> x5_getLoginInfo() {
		Map<String, String> ret = new HashMap<String, String>();
		
		long time = System.currentTimeMillis() / 1000;
		String nonce = getRandomString(15);
		
		String encrypted_data = x5_encrypt(X5_APPID + "_" + time + "_" + nonce);
		ret.put("appid", X5_APPID);
		ret.put("appsigdata", encrypted_data);
		ret.put("appsig", x5_sign_appsigdata(encrypted_data));
		
		return ret;
	}
	
	// 获取游戏登陆信息
	public static Map<String, String> x5_getLoginInfo(String X5_APPID) {
		Map<String, String> ret = new HashMap<String, String>();

		long time = System.currentTimeMillis() / 1000;
		String nonce = getRandomString(15);

		String encrypted_data = x5_encrypt(X5_APPID + "_" + time + "_" + nonce);
		ret.put("appid", X5_APPID);
		ret.put("appsigdata", encrypted_data);
		ret.put("appsig", x5_sign_appsigdata(encrypted_data));

		return ret;
	}

	public static void assert_if_failed(boolean conf, String msg) {
		if (!conf) {
			log(msg);
		}
	}
}
