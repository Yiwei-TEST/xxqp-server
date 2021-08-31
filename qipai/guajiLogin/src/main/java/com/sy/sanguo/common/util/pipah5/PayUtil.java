package com.sy.sanguo.common.util.pipah5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.MD5Util;

public class PayUtil {
	public static String CALLBACK_KEY="c518b9616b73ceab8a0cdfb2fc22174d";
	
	public static boolean verifyCallback(Map<String,String> params){
		try{
			List<String> keys = new ArrayList<String>(params.keySet());
	        Collections.sort(keys);
	        StringBuffer prestr = new StringBuffer("");
	        
	        for (int i = 0; i < keys.size(); i++) {
	            String key = keys.get(i);
	            String value = params.get(key);
	            if("sign".equals(key)){
	            	continue;
	            }
	            
	            prestr.append(key).append("=").append(value).append("&");
	            
	        }
	        prestr.append(CALLBACK_KEY);
	        if(MD5Util.getStringMD5(prestr.toString()).equals(params.get("sign"))){
	        	return true;
	        }
		}catch(Exception e){
			GameBackLogger.SYS_LOG.error("pipah5 verifyCallback error",e);
		}
		return false;
	}
}
