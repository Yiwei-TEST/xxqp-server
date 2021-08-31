package com.sy.sanguo.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.sy.sanguo.game.pdkuai.util.LogUtil;

/**
 * JacksonJSON序列化、反序列化Util类
 * @author zhoufan
 * @date 2013-6-8
 * @version v1.0
 */
public class JacksonUtil {
	
	/**
	 * Object to JSON
	 * @param value
	 * @return String
	 */
	public static String writeValueAsString(Object value){
		String result = null;
		try{
			result = JSON.toJSONString(value);

			if (result!=null){
				String temp = LoginCacheContext.getTipWithCard();
				if ("1".equals(temp)){
					if (result.contains("钻石")){
						result = result.replace("钻石","房卡");
					}
					if (result.contains("俱乐部")){
						result = result.replace("俱乐部","军团");
					}
				}else if ("2".equals(temp)){
					if (result.contains("房卡")){
						result = result.replace("房卡","钻石");
					}
					if (result.contains("军团")){
						result = result.replace("军团","俱乐部");
					}
				}
			}
		} catch (Exception var4) {
			LogUtil.e("Unable to serialize to json: " + value, var4);
		}
		return result;
	}


	public static <T> T readValue(String content, Class<T> clazz) {
		try {
			return JSON.parseObject(content,clazz);
		} catch (Exception var4) {
			LogUtil.e("Unable to unserialize to json: " + content, var4);
			return null;
		}
	}

	public static <T> T readValue(String content, TypeReference<T> valueTypeRef) {
		try {
			return JSON.parseObject(content, valueTypeRef);
		}catch (Exception var4) {
			LogUtil.e("Unable to unserialize to json: " + content, var4);
			return null;
		}
	}
}
