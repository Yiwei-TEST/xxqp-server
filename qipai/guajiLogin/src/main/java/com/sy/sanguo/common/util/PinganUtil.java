package com.sy.sanguo.common.util;

import java.util.TreeMap;

import org.apache.commons.codec.digest.DigestUtils;

public class PinganUtil {

	/**
	 * 用SHA1生成sign
	 * 
	 * @param map
	 * @return
	 */
	public static String sign(String secret, String inter,
			TreeMap<String, String> map) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(secret);
		sb.append(inter.replaceAll("/", "").replaceAll("\\?", "").replaceAll(
				"\\.", ""));
		for (String key : map.keySet()) {
			sb.append(key.replaceAll("\\s+", "")).append(
					map.get(key).replaceAll("\\s+", ""));
		}
//		GameBackLogger.SYS_LOG.info("mingwen: " + sb.toString());
		//String sha1 = DigestUtils.shaHex(sb.toString());
		return DigestUtils.shaHex(sb.toString());
	}
}
