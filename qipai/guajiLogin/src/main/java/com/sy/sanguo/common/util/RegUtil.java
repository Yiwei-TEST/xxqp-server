package com.sy.sanguo.common.util;

public class RegUtil {
	/**
	 * 加密密码
	 * 
	 * @param source
	 * @return
	 */
	public static String genPw(String source) {
		return MD5Util.getStringMD5(source + "sanguo_shangyou_2013");
	}

	/**
	 * 生成session code
	 * 
	 * @param username
	 * @return
	 */
	public static String genSessCode(String username) {
		StringBuilder sb = new StringBuilder();
		sb.append(username);
		sb.append(MathUtil.mt_rand(10000, 99999));
		sb.append(System.currentTimeMillis());
		return MD5Util.getStringMD5(sb.toString());
	}
}
