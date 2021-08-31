package com.sy.sanguo.common.util.request;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class CoderUtil {
	public static final String DEFAULT_CHARSET = "UTF-8";

	/**
	 * 解码
	 * 
	 * @param str
	 * @param charset
	 * @return
	 * @throws UnsupportedEncodingException
	 */

	public static final String decode(String str, String charset)
			throws UnsupportedEncodingException {
		try {
			return URLDecoder.decode(str, charset);
		} catch (UnsupportedEncodingException e) {
			throw e;
		} catch (Exception e) {
			return str;
		}
	}

	/**
	 * 编码
	 * 
	 * @param str
	 * @param charset
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static final String encode(String str, String charset)
			throws UnsupportedEncodingException {
		return URLEncoder.encode(str, charset);
	}

	/**
	 * UTF-8解码
	 * 
	 * @param str
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static final String decode(String str) throws UnsupportedEncodingException {
		return decode(str, DEFAULT_CHARSET);
	}

	/**
	 * UTF-8编码
	 * 
	 * @param str
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static final String encode(String str) throws UnsupportedEncodingException {
		return encode(str, DEFAULT_CHARSET);
	}

	/**
	 * 解码
	 * 
	 * @param str
	 * @param charset
	 * @return
	 * @throws UnsupportedEncodingException
	 */

	public static final String decode(String str, String charset, int count)
			throws UnsupportedEncodingException {
		for (int i = 0; i < count; i++) {
			str = URLDecoder.decode(str, charset);
		}
		return str;
	}

	/**
	 * 编码
	 * 
	 * @param str
	 * @param charset
	 * @param count
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static final String encode(String str, String charset, int count)
			throws UnsupportedEncodingException {
		for (int i = 0; i < count; i++) {
			str = URLEncoder.encode(str, charset);
		}
		return str;
	}

	/**
	 * UTF-8解码
	 * 
	 * @param str
	 * @param count
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static final String decode(String str, int count)
			throws UnsupportedEncodingException {
		for (int i = 0; i < count; i++) {
			str = decode(str, DEFAULT_CHARSET);
		}
		return str;
	}

	/**
	 * UTF-8编码
	 * 
	 * @param str
	 * @param count
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static final String encode(String str, int count)
			throws UnsupportedEncodingException {
		for (int i = 0; i < count; i++) {
			str = encode(str, DEFAULT_CHARSET);
		}
		return str;
	}
}
