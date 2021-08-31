package com.sy.sanguo.common.util.jinli;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.lang3.CharEncoding;

import javax.net.ssl.HttpsURLConnection;

public class HttpUtils {

	private static final int CONNECTION_TIMEOUT = 5000;
	private static final int READ_TIMEOUT = 5000;

	public static String post(String reqUrl, String body) throws Exception {
		String invokeUrl = reqUrl;
		URL serverUrl = new URL(invokeUrl);
		HttpURLConnection conn = (HttpURLConnection) serverUrl.openConnection();
		try{
//		HttpsURLConnection conn = (HttpsURLConnection) serverUrl.openConnection();
		conn.setConnectTimeout(CONNECTION_TIMEOUT);
		conn.setReadTimeout(READ_TIMEOUT);
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.connect();

		conn.getOutputStream().write(body.getBytes(CharEncoding.UTF_8));
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuffer buffer = new StringBuffer();
		String line = "";
		while ((line = in.readLine()) != null) {
			buffer.append(line);
		}
		in.close();
		String response = buffer.toString();
		return response;}finally {
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
