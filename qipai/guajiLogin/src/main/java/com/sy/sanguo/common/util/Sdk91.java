package com.sy.sanguo.common.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;


import javax.net.ssl.HttpsURLConnection;

public class Sdk91 {
	//这里设置你的APPID
	private static String appid = "112210";  
	//这里设置你的APPKEY
	private static String appkey = "9aa20313c412cdcdf6246349a403b3a234cf8fe22f3a210b";
	//91的服务器地址
	private static String goUrl ="http://service.sj.91.com/usercenter/ap.aspx?";
	

	/**
	 * 查询支付购买结果的API调用
	 * @param cooOrderSerial 商户订单号
	 * @return ERRORCODE的值
	 * @throws Exception API调用失败
	 */
	public static int queryPayResult(String cooOrderSerial)  throws Exception{
		String act = "1";
		StringBuilder strSign = new StringBuilder();
		strSign.append(appid);
		strSign.append(act);
		strSign.append(cooOrderSerial);
		strSign.append(appkey);
		String sign = md5(strSign.toString());
		StringBuilder getUrl = new StringBuilder();
		getUrl.append("Appid=");
		getUrl.append(appid);
		getUrl.append("&Act=");
		getUrl.append(act);
		getUrl.append("&CooOrderSerial=");
		getUrl.append(cooOrderSerial);
		getUrl.append("&Sign=");
		getUrl.append(sign);
		return GetResult(HttpGetGo(getUrl.toString()));
	}
	
	/**
	 * 检查用户登陆SESSIONID是否有效
	 * @param uin 91账号ID
	 * @param sessionID
	 * @return
	 * @throws Exception
	 */
	public static int checkUserLogin(String uin,String sessionID) throws Exception{
		String act = "4";
		StringBuilder strSign = new StringBuilder();
		strSign.append(appid);
		strSign.append(act);
		strSign.append(uin);
		strSign.append(sessionID);
		strSign.append(appkey);
		String sign = md5(strSign.toString());
		StringBuilder getUrl = new StringBuilder();
		getUrl.append("Appid=");
		getUrl.append(appid);
		getUrl.append("&Act=");
		getUrl.append(act);
		getUrl.append("&Uin=");
		getUrl.append(uin);
		getUrl.append("&SessionId=");
		getUrl.append(sessionID);
		getUrl.append("&Sign=");
		getUrl.append(sign);
		return GetResult(HttpGetGo(getUrl.toString()));
	}
	
	/**
	 * 接收支付购买结果
	 * @param appid
	 * @param act
	 * @param productName
	 * @param consumeStreamId
	 * @param cooOrderSerial
	 * @param uin
	 * @param goodsId
	 * @param goodsInfo
	 * @param goodsCount
	 * @param originalMoney
	 * @param orderMoney
	 * @param note
	 * @param payStatus
	 * @param createTime
	 * @param fromSign
	 * @return 支付结果
	 * @throws UnsupportedEncodingException 
	 */
	public static int checkPay(String appid,String act, String productName,String consumeStreamId,
			String cooOrderSerial,String uin,String goodsId,String goodsInfo,String goodsCount,
			String originalMoney,String orderMoney,String note,
			String payStatus,String createTime,String fromSign) throws UnsupportedEncodingException{
		
		StringBuilder strSign = new StringBuilder();
		strSign.append(appid);
		strSign.append(act);
		strSign.append(productName);
		strSign.append(consumeStreamId);
		strSign.append(cooOrderSerial);
		strSign.append(uin);
		strSign.append(goodsId);
		strSign.append(goodsInfo);
		strSign.append(goodsCount);
		strSign.append(originalMoney);
		strSign.append(orderMoney);
		strSign.append(note);
		strSign.append(payStatus);
		strSign.append(createTime);
		strSign.append(appkey);
		String sign = md5(strSign.toString());
		
		if(!appid.equals(appid)){
			return 2; //appid无效
		}
		if(!"1".equals(act)){
			return 3; //Act无效
		}
		if(!sign.toLowerCase().equals(fromSign.toLowerCase())){
			return 5; //sign无效
		}
		if("1".equals(payStatus)){
			return 0;
		}
		return 1;//错误
	}
	
	
	/**
	 * 获取91服务器返回的结果
	 * @param jsonStr
	 * @return
	 * @throws Exception
	 */
	private static int GetResult(String jsonStr) throws Exception{
//		Pattern p = Pattern.compile("(?<=\"ErrorCode\":\")\\d{1,3}(?=\")");
//		Matcher m = p.matcher(jsonStr);
//		m.find();
//		return Integer.parseInt(m.group());
		
		//这里需要引入JSON-LIB包内的JAR
		JSONObject jo = JSONObject.parseObject(jsonStr);
		return Integer.parseInt(jo.getString("ErrorCode"));
	}
	
	

	/**
	 * 对字符串进行MD5并返回结果
	 * @param sourceStr
	 * @return
	 */
	private static String md5(String sourceStr){
		String signStr = "";
		try {
			byte[] bytes = sourceStr.getBytes("utf-8");
			MessageDigest md5 = MessageDigest.getInstance("MD5"); md5.update(bytes);
			byte[] md5Byte = md5.digest();
			if(md5Byte != null){
			signStr = HexBin.encode(md5Byte); }
			} catch (NoSuchAlgorithmException e) { e.printStackTrace();
			} catch (UnsupportedEncodingException e) { e.printStackTrace();
			}
			return signStr;
	}
	
	/**
	 * 发送GET请求并获取结果
	 * @param getUrl
	 * @return
	 * @throws Exception
	 */
	private static String HttpGetGo(String getUrl) throws Exception{   
	    StringBuffer readOneLineBuff = new StringBuffer();   
	    String content ="";   
        URL url = new URL( goUrl + getUrl);
		HttpURLConnection conn =(HttpURLConnection) url.openConnection();
        try{
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
        String line = "";   
        while ((line = reader.readLine()) != null) {   
            readOneLineBuff.append(line);   
        }   
        content = readOneLineBuff.toString();   
        reader.close();   
	    return content;   }finally {
        	close(conn);
		}
	}

	static void close(HttpURLConnection conn) {
		if (conn != null) {
			conn.disconnect();
			conn = null;
		}
	}

	static void close(HttpsURLConnection conn) {
		if (conn != null) {
			conn.disconnect();
			conn = null;
		}
	}
}
