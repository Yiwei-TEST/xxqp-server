package com.sy.sanguo.game.service.pfs.webchanyou;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;

/**
 * @author relax
 * @since jdk1.7,Created on 2017-5-12 下午4:34:08
 * @version 1.0
 **/
public class Util {

	/**
	 * 对JSON key进行排序，然后组合成p=v&p1=v1&p2=v2&p3=v3形式
	 * 
	 * @param data
	 * @return
	 */
	public static String getUrlData(Map data) {
		Set<String> keySets = data.keySet();
		Object[] keys = keySets.toArray();
		Arrays.sort(keys);

		StringBuffer str = new StringBuffer();
		int i = 0;
		for (Object key : keys) {
			String value = data.get(key.toString()).toString();
			if (i > 0)
				str.append("&");
			str.append(key).append("=").append(value);
			i++;
		}
		return str.toString();
	}

	/** 时间格式第二种 yyyyMMddHHmmss */
	public static final String FORMAT_FOUR = "yyyyMMddHHmmss";

	public static String dateToString(Date time, String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		String date = dateFormat.format(time);
		return date;
	}
}
