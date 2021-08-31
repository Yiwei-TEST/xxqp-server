package com.sy.sanguo.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import com.sy.sanguo.common.util.https.MyX509TrustManager;

public class HttpsRequester {
	private static SSLContext sslContext;
	private static String defaultContentEncoding = "UTF-8";
	
	/**
	 * 发送POST请求
	 * @param urlString
	 * @param params
	 * @return 响应对象
	 * @throws IOException
	 */
	public static HttpRespons sendPost(String urlString, Map<String, String> params)
			throws Exception {
		return send(urlString, "POST", params, null);
	}
	
	public static HttpRespons sendGet(String urlString, Map<String, String> params)
			throws Exception {
		return send(urlString, "GET", params, null);
	}
 
	/**
	 * 发送HTTP请求
	 * @param urlString
	 * @return 响映对象
	 * @throws IOException
	 */
	private static HttpRespons send(String urlString, String method,
			Map<String, String> parameters, Map<String, String> propertys)
			throws Exception {
		HttpsURLConnection urlConnection = null;
 
		if (method.equalsIgnoreCase("GET") && parameters != null) {
			StringBuffer param = new StringBuffer();
			int i = 0;
			for (String key : parameters.keySet()) {
				if (i == 0)
					param.append("?");
				else
					param.append("&");
				param.append(key).append("=").append(parameters.get(key));
				i++;
			}
			urlString += param;
		}
		URL url = new URL(urlString);
		urlConnection = (HttpsURLConnection) url.openConnection();
 
		urlConnection.setRequestMethod(method);
		urlConnection.setDoOutput(true);
		urlConnection.setDoInput(true);
		urlConnection.setUseCaches(false);
		
		if(sslContext == null){
			TrustManager[] tm = { new MyX509TrustManager() };
			sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
		}
        // 从上述SSLContext对象中得到SSLSocketFactory对象
        SSLSocketFactory ssf = sslContext.getSocketFactory();
        urlConnection.setSSLSocketFactory(ssf);
 
		if (propertys != null)
			for (String key : propertys.keySet()) {
				urlConnection.addRequestProperty(key, propertys.get(key));
			}
 
		if (method.equalsIgnoreCase("POST") && parameters != null) {
			StringBuffer param = new StringBuffer();
			for (String key : parameters.keySet()) {
				param.append("&");
				param.append(key).append("=").append(parameters.get(key));
			}
			urlConnection.getOutputStream().write(param.toString().getBytes());
			urlConnection.getOutputStream().flush();
			urlConnection.getOutputStream().close();
		}
 
		return makeContent(urlString, urlConnection);
	}
 
	/**
	 * 得到响应对象
	 * @param urlConnection
	 * @return 响应对象
	 * @throws IOException
	 */
	private static HttpRespons makeContent(String urlString,
			HttpURLConnection urlConnection) throws IOException {
		HttpRespons httpResponser = new HttpRespons();
		try {
			InputStream in = urlConnection.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(in,"UTF-8"));
			httpResponser.contentCollection = new Vector<String>();
			StringBuffer temp = new StringBuffer();
			String line = bufferedReader.readLine();
			while (line != null) {
				httpResponser.contentCollection.add(line);
				temp.append(line).append("\r\n");
				line = bufferedReader.readLine();
			}
			bufferedReader.close();
 
			String ecod = urlConnection.getContentEncoding();
			if (ecod == null)
				ecod = defaultContentEncoding;
 
			httpResponser.urlString = urlString;
 
			httpResponser.defaultPort = urlConnection.getURL().getDefaultPort();
			httpResponser.file = urlConnection.getURL().getFile();
			httpResponser.host = urlConnection.getURL().getHost();
			httpResponser.path = urlConnection.getURL().getPath();
			httpResponser.port = urlConnection.getURL().getPort();
			httpResponser.protocol = urlConnection.getURL().getProtocol();
			httpResponser.query = urlConnection.getURL().getQuery();
			httpResponser.ref = urlConnection.getURL().getRef();
			httpResponser.userInfo = urlConnection.getURL().getUserInfo();
 
			httpResponser.content = temp.toString();//new String(temp.toString(), ecod);
			httpResponser.contentEncoding = ecod;
			httpResponser.code = urlConnection.getResponseCode();
			httpResponser.message = urlConnection.getResponseMessage();
			httpResponser.contentType = urlConnection.getContentType();
			httpResponser.method = urlConnection.getRequestMethod();
			httpResponser.connectTimeout = urlConnection.getConnectTimeout();
			httpResponser.readTimeout = urlConnection.getReadTimeout();
 
			return httpResponser;
		} catch (IOException e) {
			throw e;
		} finally {
			if (urlConnection != null)
				urlConnection.disconnect();
		}
	}
}
