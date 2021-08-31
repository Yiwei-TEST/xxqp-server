package com.sy.sanguo.common.util;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;

import com.sy.sanguo.common.log.GameBackLogger;

public class SoumiUtil {
	
	public static String remark(String key, Map<String, String> params){
		String[] payKeys = new String[params.keySet().size()];
		payKeys = params.keySet().toArray(payKeys);
		Arrays.sort(payKeys, new KeySortor());
		StringBuffer sb = new StringBuffer();
		for(String s:payKeys){
			sb.append(s);
			sb.append(params.get(s));
		}
		sb.append(key);
		GameBackLogger.SYS_LOG.info("sb:"+sb.toString());
		return getMD5Str(sb.toString(), "UTF-8");
	}
	
	public static String getMD5Str(String str, String enc){
		GameBackLogger.SYS_LOG.info("yuanchuan:"+str);
		MessageDigest md = null;
		try{
			md = MessageDigest.getInstance("MD5");
			md.reset();
			md.update(str.getBytes(enc));
		}catch(Exception e){
			e.printStackTrace();
		}
		byte[] byteArray = md.digest();
		StringBuffer sb = new StringBuffer();
		for(int i=0;i< byteArray.length;i++){
			if(Integer.toHexString(0xFF & byteArray[i]).length() == 1){
				sb.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
			}else{
				sb.append(Integer.toHexString(0xFF & byteArray[i]));
			}
		}
		return sb.toString();
	}
}
