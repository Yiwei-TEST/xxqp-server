package com.sy.sanguo.common.util.dangle;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;



public class HttpsUtil {
	
	private static HashMap<String, String> requestProperty;
	
	static{
		requestProperty = new HashMap<String, String>();
		requestProperty.put("Content-type", "application/x-www-form-urlencoded;charset=UTF-8");
	}


	public static String toParam(Map<String,String> params){
		List<String> list = new ArrayList<String>();
		
		for (String s : params.keySet()) {
			list.add(s);
		}
		
		Collections.sort(list);
		StringBuffer encodeparam = new StringBuffer();
		for (String s : list) {
			String v = params.get(s);
			if (v == null) v = "";
			encodeparam.append("&").append(s).append("=").append(v);//&key=value
		}
		encodeparam.deleteCharAt(0);
		list.clear();
		return encodeparam.toString();
	}
	
	public static void main(String args[]){
		//HttpsUtil.send(paramMap, false, urlstr, "POST");
		//http://connect.d.cn/open/member/info/?token=1B2BF037D2E04AEBB225DEE9CDCB4B5D&app_id=2855&mid=11410804&sig=3c4fdee7557bfc09b9413e641d61d317
		Map<String,String> hashMap=new HashMap<String,String>();
		hashMap.put("token", "1B2BF037D2E04AEBB225DEE9CDCB4B5D");
		hashMap.put("app_id", "2855");
		hashMap.put("sig", "3c4fdee7557bfc09b9413e641d61d317");
		try {
			String a=send(hashMap,false,"http://connect.d.cn/open/member/info/","GET");
			System.out.println(a);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 回调发货中对于单个value的特殊处理
	 * @param params
	 * @return
	 */
	public static String singleEncode(String params){
		String s = "";
		for (int i = 0; i < params.length(); i++) {
			String sub = params.substring(i,i+1);
			if(!sub.matches("[0-9]|[a-z]|[A-Z]|\\*|\\!|\\(|\\)")){
				s += "%"+Integer.toHexString(sub.codePointAt(0)).toUpperCase();
			}else{
				s += sub;
			}
		}
		
		return s;
	}
	
	/**
	 * open.qq.com/tools的签名工具中使用，和工程无关
	 * @param params
	 * @return
	 */
	public static String myEncode(Map<String,String> params){
		List<String> list = new ArrayList<String>();
		
		for (String s : params.keySet()) {
			list.add(s);
		}
		
		Collections.sort(list);
		StringBuffer encodeparam = new StringBuffer();
		for (String s : list) {
			String v = params.get(s);
			if (v == null) v = "";
			String res = "";
			for (int i = 0; i < v.length(); i++) {
				String sub = v.substring(i,i+1);
				if(!sub.matches("[0-9]|[a-z]|[A-Z]|!|\\*|\\(|\\)|\\.|\\:|\\/|\\-|\\_|\\%")){
					res += "%"+Integer.toHexString(sub.codePointAt(0)).toUpperCase();
				}else{
					res += sub;
				}
			}
			encodeparam.append("&").append(s).append("=").append(res);//&key=value
		}
		encodeparam.deleteCharAt(0);
		return encodeparam.toString();
	}
	
	/**
	 * 对源串进行编码，为了签名
	 * @param src
	 * @return
	 */
	public static String simpleEncode(String src){
		String res = "";
		for (int i = 0; i < src.length(); i++) {
			String sub = src.substring(i,i+1);
			if(!sub.matches("[0-9]|[a-z]|[A-Z]|\\-|\\_|\\.")){
				res += "%"+Integer.toHexString(sub.codePointAt(0)).toUpperCase();
			}else{
				res += sub;
			}
		}
		return res;
	}
	
	/**
	 * 对URL进行编码，为了签名
	 * @param src
	 * @return
	 */
	public static String urlEncode(String src){
		String res = "";
		for (int i = 0; i < src.length(); i++) {
			String sub = src.substring(i,i+1);
			if(!sub.matches("[0-9]|[a-z]|[A-Z]")){
				res += "%"+Integer.toHexString(sub.codePointAt(0)).toUpperCase();
			}else{
				res += sub;
			}
		}
		return res;
	}
	
	private static HostnameVerifier hv = new HostnameVerifier() {
        public boolean verify(String urlHostName, SSLSession session) {
            System.out.println("Warning: URL Host: " + urlHostName + " vs. "
                               + session.getPeerHost());
            return true;
        }
    };
	
	private static void trustAllHttpsCertificates() throws Exception {
		javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
		javax.net.ssl.TrustManager tm = new miTM();
		trustAllCerts[0] = tm;
		javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext
				.getInstance("SSL");
		sc.init(null, trustAllCerts, null);
		javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc
				.getSocketFactory());
	}

	static class miTM implements javax.net.ssl.TrustManager,
			javax.net.ssl.X509TrustManager {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public boolean isServerTrusted(
				java.security.cert.X509Certificate[] certs) {
			return true;
		}

		public boolean isClientTrusted(
				java.security.cert.X509Certificate[] certs) {
			return true;
		}

		public void checkServerTrusted(
				java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}

		public void checkClientTrusted(
				java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}
	}
	

	
	public static String send(Map<String,String> paramMap,boolean trust,String urlstr,String method) throws Exception{
		StringBuffer resp = null;
		HttpURLConnection huc=null;
		try{
			if(trust){
				trustAllHttpsCertificates();  
				HttpsURLConnection.setDefaultHostnameVerifier(hv);
			}
			if("POST".equals(method)){
				huc = (HttpURLConnection) new URL(urlstr).openConnection();
				huc.setRequestMethod(method);
				huc.setDoOutput(true);
				huc.setDoInput(true);
				huc.setConnectTimeout(5000);
				huc.setReadTimeout(3000);
				for (String property : requestProperty.keySet()) {
					huc.setRequestProperty(property, requestProperty.get(property));
				}
				huc.connect();
				
				OutputStream out = huc.getOutputStream();
				out.write(toParam(paramMap).toString().getBytes("UTF-8"));
				out.flush();
				out.close();
				
				BufferedReader in = new BufferedReader(new InputStreamReader(huc.getInputStream(),"utf-8"));
				resp = new StringBuffer();
				
				String s = in.readLine();
				while (s != null) {
					resp.append(s);
					s = in.readLine();
				}
				in.close();
			}else{
				urlstr += "?"+toParam(paramMap);
				huc = (HttpURLConnection) new URL(urlstr).openConnection();
				huc.setRequestMethod(method);
				huc.setDoOutput(true);
				huc.setDoInput(true);
				huc.setConnectTimeout(5000);
				huc.setReadTimeout(3000);
				for (String property : requestProperty.keySet()) {
					huc.setRequestProperty(property, requestProperty.get(property));
				}
				huc.connect();
				
				/*OutputStream out = huc.getOutputStream();
				out.write(toParam(paramMap).toString().getBytes("UTF-8"));
				out.flush();
				out.close();*/
				
				//BufferedReader in = new BufferedReader(new InputStreamReader(huc.getInputStream(),"ISO8859-1"));
				BufferedReader in = new BufferedReader(new InputStreamReader(huc.getInputStream(),"utf-8"));
				resp = new StringBuffer();
				
				String s = in.readLine();
				while (s != null) {
					resp.append(s);
					s = in.readLine();
				}
				in.close();
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			close(huc);
		}
		
		return resp.toString();
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
