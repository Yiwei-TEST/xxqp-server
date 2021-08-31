package com.sy.sanguo.game.service.pfs.webchanyou;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpUtil {
	private static final CloseableHttpClient httpClient;
	private static final CloseableHttpClient longHttpClient;
	public static final String CHARSET = "UTF-8";
	private static final String USER_AGENT = "Mozilla/5.0 (X11; U; Linux i686; zh-CN; rv:1.9.1.2) Gecko/20090803";
	private static final int POOL_SIZE = 500;

	static {
		//1设置连接超时时间，单位毫秒
		//2请求数据超时时间
		RequestConfig config = RequestConfig.custom().setConnectTimeout(30000).setSocketTimeout(10000).build();
		httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).setUserAgent(USER_AGENT).setMaxConnPerRoute(POOL_SIZE).build();
		
		
		RequestConfig longConfig = RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(60000).build();
		longHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(longConfig).setUserAgent(USER_AGENT).setMaxConnPerRoute(POOL_SIZE).build();
	}

	public static String doGet(String url, Map<String, Object> params) {
		return doGet(url, null, params, CHARSET, false);
	}
	
	public static String doGet(String url, Map<String, Object> params,Map<String, String> heads) {
		return doGet(url, heads, params, CHARSET, false);
	}

	public static String doPost(String url, Map<String, Object> params) {
		return doPost(url, null, params, CHARSET, false);
	}
	
	public static String doPost(String url, Map<String, Object> params,Map<String, String> heads) {
		return doPost(url, heads, params, CHARSET, false);
	}

	public static String doPost(String url,String data,Map<String, String> heads) {
		return doPost(url, heads, data, CHARSET, true);
	}

	/**
	 * * HTTP Get 获取内容 * 
	 * @param url 请求的url地址 ?之前的地址 *
	 * @param heads http头参数
	 * @param params 请求的参数 * 
	 * @param charset 编码格式 *
	 *  
	 * @return 页面内容
	 */
	@SuppressWarnings("resource")
	public static String doGet(String url, Map<String, String> heads,Map<String, Object> params, String charset, boolean isLong) {
		CloseableHttpClient curHttpClient = isLong ? longHttpClient: httpClient;
		CloseableHttpResponse response = null;
		try {
			if (params != null && !params.isEmpty()) {
				List<NameValuePair> pairs = new ArrayList<NameValuePair>(params.size());
				for (Map.Entry<String, Object> entry : params.entrySet()) {
					String value = String.valueOf(entry.getValue());
					if (value != null) {
						pairs.add(new BasicNameValuePair(entry.getKey(), value));
					}
				}
				if (url.indexOf("?") == -1)
					url += "?";
				
				url += EntityUtils.toString(new UrlEncodedFormEntity(pairs,charset));
			}
			HttpGet httpGet = new HttpGet(url);

			// 请求头添加
			if (heads != null && heads.size() > 0) {
				for (String hKey : heads.keySet()) {
					httpGet.addHeader(hKey, heads.get(hKey));
				}
			}

			response = curHttpClient.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				httpGet.abort();
			}
			HttpEntity entity = response.getEntity();
			String result = null;
			if (entity != null) {
				result = EntityUtils.toString(entity, charset);
			}
			EntityUtils.consume(entity);
			response.close();
			return result;
		}catch (Exception e) { //其他异常捕获
			e.printStackTrace();
		}finally{
			if(response!=null)
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return null;
	}

	/**
	 * HTTP Post 获取内容
	 * 
	 * @param url 请求的url地址 ?之前的地址
	 * @param params 请求的参数
	 * @param charset  编码格式
	 * @return 页面内容
	 */
	@SuppressWarnings("resource")
	public static String doPost(String url, Map<String, String> heads,Map<String, Object> params, String charset, boolean isLong) {
		CloseableHttpClient curHttpClient = isLong ? longHttpClient: httpClient;
		CloseableHttpResponse response = null;
		try {
			List<NameValuePair> pairs = null;
			if (params != null && !params.isEmpty()) {
				pairs = new ArrayList<NameValuePair>(params.size());
				for (Map.Entry<String, Object> entry : params.entrySet()) {
					String value = String.valueOf(entry.getValue());
					if (value != null) {
						pairs.add(new BasicNameValuePair(entry.getKey(), value));
					}
				}
			}
			HttpPost httpPost = new HttpPost(url);

			// 请求头添加
			if (heads != null && heads.size() > 0) {
				for (String hKey : heads.keySet()) {
					httpPost.addHeader(hKey, heads.get(hKey));
				}
			}

			if (pairs != null && pairs.size() > 0) {
				httpPost.setEntity(new UrlEncodedFormEntity(pairs, CHARSET));
			}

			response = curHttpClient.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				httpPost.abort();
			}
			HttpEntity entity = response.getEntity();
			String result = null;
			if (entity != null) {
				result = EntityUtils.toString(entity, charset);
			}
			EntityUtils.consume(entity);
			response.close();
			return result;
		}catch (Exception e) { //其他异常捕获
			e.printStackTrace();
		}finally{
			if(response!=null)
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return null;
	}

	@SuppressWarnings("resource")
	public static String doPost(String url, Map<String, String> heads,String data, String charset, boolean isLong) {
		CloseableHttpClient curHttpClient = isLong ? longHttpClient: httpClient;
		CloseableHttpResponse response = null;
		try {
			HttpPost httpPost = new HttpPost(url);

			// 请求头添加
			if (heads != null && heads.size() > 0) {
				for (String hKey : heads.keySet()) {
					httpPost.addHeader(hKey, heads.get(hKey));
				}
			}
			
			
			//head.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
			//该方式post的值默认类型为Content-Type="multipart/form-data"的数据类型提交"
			//这个时候getParamter()是无法获取参数的。
			EntityBuilder eb = EntityBuilder.create();
			eb.setBinary(data.getBytes());
			eb.setContentEncoding(CHARSET);
			httpPost.setEntity(eb.build());

			response = curHttpClient.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				httpPost.abort();
			}
			HttpEntity entity = response.getEntity();
			String result = null;
			if (entity != null) {
				result = EntityUtils.toString(entity, charset);
			}
			EntityUtils.consume(entity);
			response.close();
			return result;
		} catch (SocketTimeoutException e) { //超时异常捕获
			e.printStackTrace();
			
		}catch (Exception e) { //其他异常捕获
			e.printStackTrace();
		}finally{
			if(response!=null)
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return null;
	}

	public static void main(String[] args) {
	}
}
