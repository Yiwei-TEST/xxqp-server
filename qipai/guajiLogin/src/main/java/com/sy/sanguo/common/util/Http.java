package com.sy.sanguo.common.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletResponse;

/**
 * HTTP协议工具类
 * @date 2012-7-24
 * @version v1.0
 * Http响应中文编码-new String("".getBytes("GBK"),"ISO8859-1")
 */
public class Http {

	public static boolean isxml = true;
	public static String context = "";
	public static final String text	= "text/plain;charset=UTF-8";
	public static final String post	= "application/x-www-form-urlencoded;charset=UTF-8";

	public enum ContentType{
		asf("video/x-ms-asf"),
		avi("video/avi"),
		mpg("ivideo/mpeg"),
		gif("image/gif"),
		jpg("image/jpeg"),
		bmp("image/bmp"),
		png("image/png"),
		wav("audio/wav"),
		mp3("audio/mpeg3"),
		html("text/html"),
		txt("text/plain"),
		zip("application/zip"),
		doc("application/msword"),
		xls("application/vnd.ms-excel"),
		rtf("application/rtf"),
		all("application/octet-stream");


		private String type;
		private ContentType(String type){
			this.type = type;
		}
		public String getType() {
			return type;
		}
		public String toString() {
			return type;
		}
	}

	private String httpURL;
	private URL url;
	/** 请求头参数 **/
	private Map<String, String> requestProperty;
	/** 连接超时 **/
	private int connectTimeout = 5000;
	/** 响应超时 **/
	private int readTimeout = 5000;

	/**
	 * @param httpURL 用http地址构建Http对象
	 * @throws Exception
	 */
	public Http(String httpURL,boolean isxml) throws Exception{
		this.httpURL = httpURL;
		this.isxml=isxml;
		init();

	}


	/**
	 * POST协议传递参数
	 * @param params post参数
	 * @return 响应
	 * @throws Exception
	 */
	public String post(String params) throws Exception{

		HttpURLConnection huc = (HttpURLConnection) url.openConnection();
		try{
		if(httpURL.startsWith("https")){
			HttpsURLConnection hucs = (HttpsURLConnection) huc;
			// 创建SSLContext对象，并使用我们指定的信任管理器初始化
			TrustManager[] tm = {new MyX509TrustManager()};
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
			// 从上述SSLContext对象中得到SSLSocketFactory对象
			SSLSocketFactory ssf = sslContext.getSocketFactory();
			hucs.setSSLSocketFactory(ssf);
			HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
		}
		huc.setRequestMethod("POST");
		huc.setDoOutput(true);
		huc.setDoInput(true);
		huc.setConnectTimeout(connectTimeout);
		huc.setReadTimeout(readTimeout);
		for (String property : requestProperty.keySet()) {
			huc.setRequestProperty(property, requestProperty.get(property));
		}
		huc.connect();

		OutputStream out = huc.getOutputStream();
		out.write(params.toString().getBytes("UTF-8"));
		out.flush();
		out.close();

		String err = checkError(huc);
		if(err != null) return err;

//		BufferedReader in = new BufferedReader(new InputStreamReader(huc.getInputStream(),"ISO8859-1"));
		BufferedReader in = new BufferedReader(new InputStreamReader(huc.getInputStream(),"UTF-8"));
		StringBuffer resp = new StringBuffer();

		String s = in.readLine();
		while (s != null) {
			resp.append(s);
			s = in.readLine();
		}
		in.close();

		return resp.toString();}finally {
			close(huc);
		}
	}


	/**
	 * POST协议传递参数
	 * @param params post参数
	 * @return 响应
	 * @throws Exception
	 */
	public String post(Map<String, String> params) throws Exception{
		StringBuffer s = new StringBuffer();
		//k=v&k=v
		for (String k : params.keySet()) {
			s.append("&").append(k).append("=").append(params.get(k));
		}
		s.deleteCharAt(0);
//		System.out.println(s.toString());
		return post(s.toString());
	}


	/**
	 * 发送文件
	 * @param fileParamName 请求接收的参数名
	 * @param fileName 文件名
	 * @param file 文件
	 * @return 响应
	 * @throws Exception
	 */
	public String post(String fileParamName,String fileName,byte[] file)
			throws Exception{
		return post(fileParamName, fileName, file, null);
	}


	/**
	 * 发送文件
	 * @param fileParamName 请求接收的参数名
	 * @param fileName 文件名
	 * @param file 文件
	 * @param params 参数
	 * @return 响应
	 * @throws Exception
	 */
	public String post(String fileParamName,String fileName,byte[] file,Map<String, String> params)
			throws Exception{
		HttpURLConnection huc = (HttpURLConnection) url.openConnection();
		try{
		if(httpURL.startsWith("https")){
			HttpsURLConnection hucs = (HttpsURLConnection) huc;
			// 创建SSLContext对象，并使用我们指定的信任管理器初始化
			TrustManager[] tm = {new MyX509TrustManager()};
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
			// 从上述SSLContext对象中得到SSLSocketFactory对象
			SSLSocketFactory ssf = sslContext.getSocketFactory();
			hucs.setSSLSocketFactory(ssf);
			HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
		}
		huc.setRequestMethod("POST");
		huc.setDoOutput(true);
		huc.setDoInput(true);
		huc.setConnectTimeout(connectTimeout);
		huc.setReadTimeout(readTimeout);

		//分隔
		String boundary = "-----------------------------114975832116442893661388290519";
		huc.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

		boundary = "--" + boundary;

		StringBuffer sb = new StringBuffer();
		//参数
		if(params != null){
			for (Iterator<String> it = params.keySet().iterator();it.hasNext();) {
				String k = it.next();
				String v = params.get(k);
				sb.append(boundary).append("\r\n");
				sb.append("Content-Disposition: form-data; name=\"" + k + "\"\r\n\r\n");
				sb.append(v).append("\r\n");
			}
		}

		//文件
		sb.append(boundary).append("\r\n");
		sb.append("Content-Disposition: form-data; name=\"" + fileParamName
				+ "\"; filename=\"" + fileName + "\"\r\n");
		sb.append("Content-Type: " + getContentType(fileName) + " \r\n\r\n");

		huc.connect();

		OutputStream out = huc.getOutputStream();
		out.write(sb.toString().getBytes("UTF-8"));
		out.write(file);
		out.flush();
		out.close();

		String err = checkError(huc);
		if(err != null) return err;

		BufferedReader in = new BufferedReader(new InputStreamReader(huc.getInputStream(),"ISO8859-1"));
		StringBuffer resp = new StringBuffer();

		String s = in.readLine();
		while (s != null) {
			resp.append(s);
			s = in.readLine();
		}
		in.close();

		return resp.toString();}finally {
			close(huc);
		}
	}


	/**
	 * 响应文件
	 * @param resp
	 * @param fileName 文件名
	 * @param file 文件
	 * @return boolean 是否相应成功
	 * @throws Exception
	 */
	public static boolean response(HttpServletResponse resp,String fileName,byte[] file){
		if(resp == null || fileName == null || fileName.trim().equals("")
				|| file == null || file.length == 0){
			throw new NullPointerException("param is null");
		}
		try {//文件名中文编码
			fileName = new String(fileName.getBytes("GBK"),"ISO8859-1");
		} catch (Throwable e) {
			e.printStackTrace();
		}
		resp.reset();
		resp.setCharacterEncoding("UTF-8");
		resp.addHeader("Content-Disposition", "attachment;filename="+ fileName +";");
		resp.setContentType(getContentType(fileName));
		try {
			OutputStream out = resp.getOutputStream();
			out.write(file);
			out.flush();
			out.close();
		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	/**
	 * GET协议发送Http请求
	 * httpUrl url
	 * params 参数
	 * @return String 响应
	 * @throws Exception
	 */
	public static String get(String httpUrl,Map<String, String> params)throws Exception{
		StringBuffer s = new StringBuffer();
		//k=v&k=v
		for (String k : params.keySet()) {
			s.append("&").append(k).append("=").append(params.get(k));
		}
		s.deleteCharAt(0);
		httpUrl = httpUrl + "?" + s.toString();
		return get(httpUrl);
	}

	/**
	 * GET协议发送Http请求
	 * httpUrl url
	 * @return String 响应
	 * @throws Exception
	 */
	public static String get(String httpUrl)throws Exception{
		URL url = new URL(httpUrl);
		HttpURLConnection huc = (HttpURLConnection) url.openConnection();
		try{
		huc.setRequestMethod("GET");
		huc.setDoOutput(false);
		huc.setDoInput(true);
		huc.setConnectTimeout(5000);
		huc.setReadTimeout(5000);
		huc.setRequestProperty("Content-type", "text/plain;charset=UTF-8");
		huc.connect();

		String err = checkError(huc);
		if(err != null) return err;

		BufferedReader in = new BufferedReader(new InputStreamReader(huc.getInputStream(),"ISO8859-1"));
		StringBuffer resp = new StringBuffer();

		String s = in.readLine();
		while (s != null) {
			resp.append(s);
			s = in.readLine();
		}
		in.close();

		return resp.toString();}finally {
			close(huc);
		}
	}


	/**
	 * 获取文件类型
	 * @param fileName
	 * @return String
	 */
	public static String getContentType(String fileName){
		//文件名小写
		String filename = fileName.toLowerCase();
		if(filename.endsWith(".asf")){
			return ContentType.asf.toString();
		}else if(filename.endsWith(".avi")){
			return ContentType.avi.toString();
		}else if(filename.endsWith(".mpg") || filename.endsWith(".mpeg")){
			return ContentType.mpg.toString();
		}else if(filename.endsWith(".gif")){
			return ContentType.gif.toString();
		}else if(filename.endsWith(".jpg") || filename.endsWith(".jpeg")){
			return ContentType.jpg.toString();
		}else if(filename.endsWith(".bmp")){
			return ContentType.bmp.toString();
		}else if(filename.endsWith(".png")){
			return ContentType.png.toString();
		}else if(filename.endsWith(".wav")){
			return ContentType.wav.toString();
		}else if(filename.endsWith(".mp3")){
			return ContentType.mp3.toString();
		}else if(filename.endsWith(".htm") || filename.endsWith(".html")){
			return ContentType.html.toString();
		}else if(filename.endsWith(".txt")){
			return ContentType.txt.toString();
		}else if(filename.endsWith(".zip")){
			return ContentType.zip.toString();
		}else if(filename.endsWith(".doc")){
			return ContentType.doc.toString();
		}else if(filename.endsWith(".xls")){
			return ContentType.xls.toString();
		}else if(filename.endsWith(".rtf")){
			return ContentType.rtf.toString();
		}

		return ContentType.all.toString();
	}

	/**
	 * 检查异常
	 * @param huc 连接
	 * @return String 异常响应
	 * @throws Exception String
	 */
	private static String checkError(HttpURLConnection huc) throws Exception{

		if(huc.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR //500错误
				|| huc.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST //400
				|| huc.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED //401
				){
			BufferedReader in = new BufferedReader(new InputStreamReader(huc.getErrorStream(),"ISO8859-1"));
			StringBuffer resp = new StringBuffer();
			String s = in.readLine();
			while (s != null) {
				resp.append(s).append("\n");
				s = in.readLine();
			}
			in.close();
			return resp.toString();
		}




		return null;
	}



	//初始化
	private void init() throws Exception{
		if(httpURL == null || (!httpURL.startsWith("http://") && !httpURL.startsWith("https://"))){
			throw new NullPointerException("param is't url-"+httpURL);
		}
		url = new URL(httpURL);
		if(requestProperty == null){
			requestProperty = new HashMap<String, String>();
		}
		if(requestProperty.isEmpty()) {
//			requestProperty.put("Content-type", text);
//			requestProperty.put("Content-type", post);
			if (isxml)
				context = "text/plain;charset=UTF-8";
			else
				context = "application/x-www-form-urlencoded;charset=UTF-8";
			requestProperty.put("Content-type", context);
		}
	}

	public class MyX509TrustManager implements javax.net.ssl.TrustManager,
			javax.net.ssl.X509TrustManager {X509TrustManager sunJSSEX509TrustManager;
		MyX509TrustManager() throws Exception {
			// create a "default" JSSE X509TrustManager.

			System.setProperty("javax.net.ssl.trustStore", "*.keystore");
			System.setProperty("java.protocol.handler.pkgs","com.sun.net.ssl.internal.www.protocol");
			Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
			System.setProperty("java.protocol.handler.pkgs", "javax.net.ssl");

			KeyStore ks = KeyStore.getInstance("JKS");
			//ks.load(new FileInputStream("trustedCerts"),"passphrase".toCharArray());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
			tmf.init(ks);
			TrustManager tms [] = tmf.getTrustManagers();
        /*
         * Iterate over the returned trustmanagers, look
         * for an instance of X509TrustManager.  If found,
         * use that as our "default" trust manager.
         */
			for (int i = 0; i < tms.length; i++) {
				if (tms[i] instanceof X509TrustManager) {
					sunJSSEX509TrustManager = (X509TrustManager) tms[i];
					return;
				}
			}
        /*
         * Find some other way to initialize, or else we have to fail the
         * constructor.
         */
			throw new Exception("Couldn't initialize");
		}
		/*
         * Delegate to the default trust manager.
         */
		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			try {
				sunJSSEX509TrustManager.checkClientTrusted(chain, authType);
			} catch (CertificateException excep) {
				// do any special handling here, or rethrow exception.
			}
		}
		/*
         * Delegate to the default trust manager.
         */
		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			try {
				sunJSSEX509TrustManager.checkServerTrusted(chain, authType);
			} catch (CertificateException excep) {
            /*
             * Possibly pop up a dialog box asking whether to trust the
             * cert chain.
             */
			}
		}
		/*
         * Merely pass this through.
         */
		public X509Certificate[] getAcceptedIssuers() {
			return sunJSSEX509TrustManager.getAcceptedIssuers();
		}
	}


	public String getHttpURL() {
		return httpURL;
	}
	public int getConnectTimeout() {
		return connectTimeout;
	}
	public void setConnectTimeout(int connectTimeout) {
		if(connectTimeout < 1) return;
		this.connectTimeout = connectTimeout;
	}
	public int getReadTimeout() {
		return readTimeout;
	}
	public void setReadTimeout(int readTimeout) {
		if(readTimeout < 1) return;
		this.readTimeout = readTimeout;
	}
	public void setRequestProperty(Map<String, String> requestProperty) {
		this.requestProperty = requestProperty;
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
