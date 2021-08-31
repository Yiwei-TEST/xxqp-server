package com.sy.sanguo.common.util;

import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Vector;

public class HttpRequester {
	private static String defaultContentEncoding = "UTF-8";

	/**
	 * 发送GET请求
	 * 
	 * @param urlString
	 * @param params
	 * @return 响应对象
	 * @throws IOException
	 */
	public static HttpRespons sendGet(String urlString, Map<String, String> params,Map<String, String> propertys) throws IOException {
		return send(urlString, "GET", params, propertys);
	}
	/**
	 * 发送GET请求
	 * 
	 * @param urlString
	 * @param params
	 * @return 响应对象
	 * @throws IOException
	 */
	public static HttpRespons sendGet(String urlString, Map<String, String> params) throws IOException {
		return send(urlString, "GET", params, null);
	}

	/**
	 * 发送POST请求
	 * 
	 * @param urlString
	 * @param params
	 * @return 响应对象
	 * @throws IOException
	 */
	public static HttpRespons sendPost(String urlString, Map<String, String> params) throws IOException {
		return send(urlString, "POST", params, null);
	}
	/**
	 * 发送POST请求
	 * 
	 * @param urlString
	 * @param params
	 * @return 响应对象
	 * @throws IOException
	 */
	public static HttpRespons sendPost(String urlString, Map<String, String> params,Map<String, String> propertys) throws IOException {
		return send(urlString, "POST", params, propertys);
	}
	

	/**
	 * 发送HTTP请求
	 * 
	 * @param urlString
	 * @return 响映对象
	 * @throws IOException
	 */
	private static HttpRespons send(String urlString, String method, Map<String, String> parameters, Map<String, String> propertys)
			throws IOException {
		HttpURLConnection urlConnection = null;

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
		urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setRequestMethod(method);
		urlConnection.setDoOutput(true);
		urlConnection.setDoInput(true);
		urlConnection.setUseCaches(false);

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
	 * 
	 * @param urlConnection
	 * @return 响应对象
	 * @throws IOException
	 */
	private static HttpRespons makeContent(String urlString, HttpURLConnection urlConnection) throws IOException {
		HttpRespons httpResponser = new HttpRespons();
		try {
			InputStream in = urlConnection.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
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

			httpResponser.content = new String(temp.toString().getBytes(), ecod);
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

	/**
	 * 发送json post请求
	 * 
	 * @param addUrl
	 * @param json
	 * @return
	 * @throws IOException
	 */
	public static HttpRespons sendPostByJson(String addUrl, JSONObject json) throws IOException {

		HttpURLConnection connection = null;
		HttpRespons httpResponser = new HttpRespons();
		try {
			// 创建连接
			URL url = new URL(addUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			connection.connect();

			// POST请求
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());

			out.writeBytes(json.toString());
			out.flush();
			out.close();

			// 读取响应
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			httpResponser.contentCollection = new Vector<String>();
			StringBuffer temp = new StringBuffer();
			String line = null;
			while ((line = reader.readLine()) != null) {
				line = new String(line.getBytes(), "utf-8");
				temp.append(line);
			}
			reader.close();

			String ecod = connection.getContentEncoding();
			if (ecod == null)
				ecod = defaultContentEncoding;

			httpResponser.urlString = addUrl;

			httpResponser.defaultPort = connection.getURL().getDefaultPort();
			httpResponser.file = connection.getURL().getFile();
			httpResponser.host = connection.getURL().getHost();
			httpResponser.path = connection.getURL().getPath();
			httpResponser.port = connection.getURL().getPort();
			httpResponser.protocol = connection.getURL().getProtocol();
			httpResponser.query = connection.getURL().getQuery();
			httpResponser.ref = connection.getURL().getRef();
			httpResponser.userInfo = connection.getURL().getUserInfo();

			httpResponser.content = temp.toString();
			httpResponser.contentEncoding = ecod;
			httpResponser.code = connection.getResponseCode();
			httpResponser.message = connection.getResponseMessage();
			httpResponser.contentType = connection.getContentType();
			httpResponser.method = connection.getRequestMethod();
			httpResponser.connectTimeout = connection.getConnectTimeout();
			httpResponser.readTimeout = connection.getReadTimeout();

			reader.close();
			// 断开连接
			connection.disconnect();
		} catch (IOException e) {
			throw e;
		} finally {
			if (connection != null)
				connection.disconnect();
		}
		return httpResponser;

	}
}
