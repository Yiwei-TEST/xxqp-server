package com.sy.sanguo.common.util;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;

import org.apache.http.protocol.HTTP;

import com.sy.sanguo.common.log.GameBackLogger;

/**
 * NetTool:封装一个类搞定90%安卓客户端与服务器端交互
 */
public class NetTool {
	private static final int TIMEOUT = 10000;// 10秒
	private Map<String, String> reqProperty;

	public void addReqProperty(String key, String value) {
		if (reqProperty == null) {
			reqProperty = new HashMap<String, String>();
		}
		reqProperty.put(key, value);
	}

	public static boolean getImage(String urlPath, int height, int width, String imagePath, String imageName) throws Exception {
		// new一个URL对象
		URL url = new URL(urlPath);
		// 打开链接
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		try {
			// 设置请求方式为"GET"
			conn.setRequestMethod("GET");
			// 超时响应时间为5秒
			conn.setConnectTimeout(5 * 1000);
			// 通过输入流获取图片数据
			InputStream inStream = conn.getInputStream();
			File dir = new File(imagePath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			if (height == 0 || width == 0) {
				// 得到图片的二进制数据，以二进制封装得到数据，具有通用性
				byte[] data = readInputStream(inStream);
				// new一个文件对象用来保存图片，默认保存当前工程根目录
				File imageFile = new File(imagePath + imageName + ".jpg");
				// 创建输出流
				FileOutputStream outStream = new FileOutputStream(imageFile);
				// 写入数据
				outStream.write(data);
				// 关闭输出流
				outStream.close();
				return true;

			} else {
				// BufferedImage bimg = new BufferedImage(115, 115,
				// BufferedImage.TYPE_INT_BGR);
				BufferedImage bufferedImage = ImageIO.read(inStream);
				int calcHeight = height > 0 ? height : (width * bufferedImage.getHeight() / bufferedImage.getWidth());
				// ImageIO.write(createResizedCopy(bufferedImage, width,
				// calcHeight), "D:/" + imageName + ".jpg",
				// response.getOutputStream());
				return ImageIO.write(createResizedCopy(bufferedImage, width, calcHeight), "jpg", new File(imagePath + imageName + ".jpg"));
				// ImageIO.write(bufferedImage, "jpg", new File("D:/" +
				// imageName +
				// ".jpg"));

			}
		}finally {
			close(conn);
		}
	}

	static BufferedImage createResizedCopy(Image originalImage, int scaledWidth, int scaledHeight) {
		BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = scaledBI.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
		g.dispose();
		return scaledBI;
	}

	public static void main(String[] args) {
//		try {
//			getImage("http://wx.qlogo.cn/mmopen/SOkBQWIHibUYwibboHZ0WsrTZJOQ5kQXzvOznpM0NyLg9qhTXFz85XhX5agUaD14JVdUHs0R8k7icUJOHILU462IpKDYeBytzqC/0", 115, 115, "D:/111/", "1");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		
		
	}

	public static byte[] readInputStream(InputStream inStream) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		// 创建一个Buffer字符串
		byte[] buffer = new byte[1024];
		// 每次读取的字符串长度，如果为-1，代表全部读取完毕
		int len = 0;
		// 使用一个输入流从buffer里把数据读取出来
		while ((len = inStream.read(buffer)) != -1) {
			// 用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
			outStream.write(buffer, 0, len);
		}
		// 关闭输入流
		inStream.close();
		// 把outStream里的数据写入内存
		return outStream.toByteArray();
	}

	/**
	 * 传送文本,例如Json,xml等
	 */
	public String sendTxt(String urlPath, String txt, String encoding) throws Exception {
		byte[] sendData = txt.getBytes();
		URL url = new URL(urlPath);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		try{
		conn.setRequestMethod("POST");
		conn.setConnectTimeout(TIMEOUT);
		conn.setDoOutput(true);// 如果通过post提交数据，必须设置允许对外输出数据
		conn.setRequestProperty("Content-Type", "text/xml");
		conn.setRequestProperty("Charset", encoding);
		conn.setRequestProperty("Content-Length", String.valueOf(sendData.length));
		OutputStream outStream = conn.getOutputStream();
		outStream.write(sendData);
		outStream.flush();
		outStream.close();
		if (conn.getResponseCode() == 200) {
			// 获得服务器响应的数据
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), encoding));
			// 数据
			String retData = null;
			String responseData = "";
			while ((retData = in.readLine()) != null) {
				responseData += retData;
			}
			in.close();
			return responseData;
		}
		return "sendText error!";}finally {
			close(conn);
		}
	}

	/**
	 * 通过post方式提交参数给服务器
	 */
	public String sendPost(String urlPath, Map<String, String> params, String encoding) throws Exception {

		// 使用StringBuilder对象
		StringBuilder sb = new StringBuilder(urlPath);
		sb.append('?');

		// 迭代Map
		for (Map.Entry<String, String> entry : params.entrySet()) {
			sb.append(entry.getKey()).append('=').append(URLEncoder.encode(entry.getValue(), encoding)).append('&');
		}
		sb.deleteCharAt(sb.length() - 1);
		// 打开链接
		URL url = new URL(sb.toString());
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		try{
		conn.setRequestMethod("POST");
		conn.setUseCaches(false);
		conn.setRequestProperty("Content-Type", "text/xml");
		conn.setRequestProperty("Charset", encoding);
		conn.setRequestProperty("cache-control", "no-cache");
		if (reqProperty != null) {
			for (java.util.Map.Entry<String, String> entry : reqProperty.entrySet()) {
				conn.setRequestProperty(entry.getKey(), entry.getValue());

			}
		}
		conn.setConnectTimeout(TIMEOUT);
		// 如果请求响应码是200，则表示成功
		if (conn.getResponseCode() == 200) {
			// 获得服务器响应的数据
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), encoding));
			// 数据
			String retData = null;
			String responseData = "";
			while ((retData = in.readLine()) != null) {
				responseData += retData;
			}
			in.close();
			return responseData;
		}
		return "sendGetRequest error!";
		}finally {
			close(conn);
		}
	}

	/**
	 * 上传文件
	 */
	public String sendFile(String urlPath, String filePath, String newName) throws Exception {
		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";

		URL url = new URL(urlPath);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		try{
		/* 允许Input、Output，不使用Cache */
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setUseCaches(false);
		/* 设置传送的method=POST */
		con.setRequestMethod("POST");
		/* setRequestProperty */

		con.setRequestProperty("Connection", "Keep-Alive");
		con.setRequestProperty("Charset", "UTF-8");
		con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
		/* 设置DataOutputStream */
		DataOutputStream ds = new DataOutputStream(con.getOutputStream());
		ds.writeBytes(twoHyphens + boundary + end);
		ds.writeBytes("Content-Disposition: form-data; " + "name=\"file1\";filename=\"" + newName + "\"" + end);
		ds.writeBytes(end);

		/* 取得文件的FileInputStream */
		FileInputStream fStream = new FileInputStream(filePath);
		/* 设置每次写入1024bytes */
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];

		int length = -1;
		/* 从文件读取数据至缓冲区 */
		while ((length = fStream.read(buffer)) != -1) {
			/* 将资料写入DataOutputStream中 */
			ds.write(buffer, 0, length);
		}
		ds.writeBytes(end);
		ds.writeBytes(twoHyphens + boundary + twoHyphens + end);

		/* close streams */
		fStream.close();
		ds.flush();

		/* 取得Response内容 */
		InputStream is = con.getInputStream();
		int ch;
		StringBuffer b = new StringBuffer();
		while ((ch = is.read()) != -1) {
			b.append((char) ch);
		}
		/* 关闭DataOutputStream */
		ds.close();
		return b.toString();}finally {
			close(con);
		}
	}

	/**
	 * 通过get方式提交参数给服务器
	 */
	public String sendGetRequest(String urlPath, Map<String, String> params, String encoding) throws Exception {

		// 使用StringBuilder对象
		StringBuilder sb = new StringBuilder(urlPath);
		sb.append('?');

		// 迭代Map
		for (Map.Entry<String, String> entry : params.entrySet()) {
			sb.append(entry.getKey()).append('=').append(URLEncoder.encode(entry.getValue(), encoding)).append('&');
		}
		sb.deleteCharAt(sb.length() - 1);
		// 打开链接
		URL url = new URL(sb.toString());
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		try{
		conn.setRequestMethod("POST");
		conn.setUseCaches(false);
		conn.setRequestProperty("Content-Type", "text/xml");
		conn.setRequestProperty("Charset", encoding);
		conn.setRequestProperty("cache-control", "no-cache");
		conn.setConnectTimeout(TIMEOUT);
		// 如果请求响应码是200，则表示成功
		if (conn.getResponseCode() == 200) {
			// 获得服务器响应的数据
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), encoding));
			// 数据
			String retData = null;
			String responseData = "";
			while ((retData = in.readLine()) != null) {
				responseData += retData;
			}
			in.close();
			return responseData;
		}
		return "sendGetRequest error!";
		}finally {
			close(conn);
		}
	}

	/**
	 * 通过Post方式提交参数给服务器
	 */
	public String sendPostRequest(String urlPath, Map<String, String> params, String encoding) throws Exception {
		StringBuilder sb = new StringBuilder();
		// 如果参数不为空
		if (params != null && !params.isEmpty()) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				// Post方式提交参数的话，不能省略内容类型与长度
				sb.append(entry.getKey()).append('=').append(URLEncoder.encode(entry.getValue(), encoding)).append('&');
			}
			sb.deleteCharAt(sb.length() - 1);
		}

		byte[] entitydata = sb.toString().getBytes();// 得到实体的二进制数据
		URL url = new URL(urlPath);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		try{
		conn.setRequestMethod("POST");
		conn.setConnectTimeout(TIMEOUT);
		conn.setDoOutput(true);// 如果通过post提交数据，必须设置允许对外输出数据
		// 这里只设置内容类型与内容长度的头字段
		conn.setRequestProperty("Content-Type", "text/xml ");// text/xml
																// application/x-www-form-urlencoded
		conn.setRequestProperty("Charset", encoding);
		conn.setRequestProperty("cache-control", "no-cache");
		conn.setRequestProperty("Content-Length", String.valueOf(entitydata.length));
		OutputStream outStream = conn.getOutputStream();
		// 把实体数据写入是输出流
		outStream.write(entitydata);
		// 内存中的数据刷入
		outStream.flush();
		outStream.close();
		// 如果请求响应码是200，则表示成功
		if (conn.getResponseCode() == 200) {
			// 获得服务器响应的数据
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), encoding));
			// 数据
			String retData = null;
			String responseData = "";
			while ((retData = in.readLine()) != null) {
				responseData += retData;
			}
			in.close();
			return responseData;
		}
		return "error";}finally {
			close(conn);
		}
	}

	/**
	 * 通过Post方式提交参数给服务器,也可以用来传送json或xml文件
	 */
	public String sendPostJsonRequest(String urlPath, JSONObject json, String encoding) throws Exception {

		// Log.d("sy599",">>>>>>"+urlPath);
		// Log.d("sy599",">>>>>>"+json.toString());
		// StringBuilder sb = new StringBuilder();
		byte[] entitydata = json.toString().getBytes();// 得到实体的二进制数据
		URL url = new URL(urlPath);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		try{
		conn.setRequestMethod("POST");
		conn.setConnectTimeout(TIMEOUT);
		conn.setDoOutput(true);// 如果通过post提交数据，必须设置允许对外输出数据
		// =============== 这里只设置内容类型与内容长度的头字段======================//
		conn.setRequestProperty("Content-Type", "application/json");// text/xml
																	// application/x-www-form-urlencoded
		conn.setRequestProperty("Charset", encoding);
		conn.setRequestProperty("cache-control", "no-cache");
		conn.setRequestProperty("Content-Length", String.valueOf(entitydata.length));
		conn.setUseCaches(false);
		OutputStream outStream = conn.getOutputStream();
		outStream.write(entitydata);// 把实体数据写入是输出流
		outStream.flush();// 内存中的数据刷入
		outStream.close();
		// 如果请求响应码是200，则表示成功
		if (conn.getResponseCode() == 200) {
			// 获得服务器响应的数据
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), encoding));
			// 数据
			String retData = null;
			String responseData = "";
			while ((retData = in.readLine()) != null) {
				responseData += retData;
			}
			in.close();
			// Log.d("sy599","<<<<<"+responseData);
			return responseData;
		}

		return "error";}finally {
			close(conn);
		}
	}

	/**
	 * 根据URL直接读文件内容，前提是这个文件当中的内容是文本，函数的返回值就是文件当中的内容
	 */
	public String readTxtFile(String urlStr, String encoding) throws Exception {
		StringBuffer sb = new StringBuffer();
		String line = null;
		BufferedReader buffer = null;
		HttpURLConnection urlConn=null;
		try {
			// 创建一个URL对象
			URL url = new URL(urlStr);
			// 创建一个Http连接
			urlConn = (HttpURLConnection) url.openConnection();
			// 使用IO流读取数据
			buffer = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), encoding));
			while ((line = buffer.readLine()) != null) {
				sb.append(line);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			close(urlConn);
			try {
				buffer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * 根据URL得到输入流
	 * 
	 * @param urlStr
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public InputStream getInputStreamFromUrl(String urlStr) throws MalformedURLException, IOException {
		URL url = new URL(urlStr);
		HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
		try{
		InputStream inputStream = urlConn.getInputStream();
		return inputStream;}finally {
			close(urlConn);
		}
	}

	public static String receivePost(HttpServletRequest request) {
		// 读取请求内容
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			String line = null;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			// 将资料解码
			String reqBody = sb.toString();
			return URLDecoder.decode(reqBody, HTTP.UTF_8);
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("receivePost err", e);
		}
		return null;

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
