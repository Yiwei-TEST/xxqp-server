package com.sy.sanguo.common.util.yidongjidi;

import java.util.HashMap;
import java.util.Map;

public class SdkUtil {
	public static Map<String,String> codeMap=new HashMap<String,String>();
	public static Map<String,String> pkCodeMap=new HashMap<String,String>();
	
	static{
		codeMap.put("006058040010", "21");
		codeMap.put("006058040004", "22");
		codeMap.put("006058040006", "23");
		codeMap.put("006058040007", "24");
		codeMap.put("006058040008", "25");
		codeMap.put("006058040009", "26");
		codeMap.put("006058040011", "27");
		codeMap.put("006058040012", "28");
		codeMap.put("006058040013", "29");
		codeMap.put("006058040001", "10");
		

		pkCodeMap.put("006078935010", "21");
		pkCodeMap.put("006078935004", "22");
		pkCodeMap.put("006078935006", "23");
		pkCodeMap.put("006078935007", "24");
		pkCodeMap.put("006078935008", "25");
		pkCodeMap.put("006078935009", "26");
		pkCodeMap.put("006078935011", "27");
		pkCodeMap.put("006078935012", "28");
		pkCodeMap.put("006078935013", "29");
		pkCodeMap.put("006078935001", "10");
	}
	
	public static String  makeOrderId(String orderTime,String pf){
		return orderTime +"_"+ pf;
	}
	
	@SuppressWarnings("unchecked")
	public static boolean isYidongjidiParam(Map requestMap){
		if(requestMap.containsKey("userId")&&
				requestMap.containsKey("key")&&
				requestMap.containsKey("cpId")&&
				requestMap.containsKey("cpServiceId")&&
				requestMap.containsKey("channelId")&&
				requestMap.containsKey("p")&&
				requestMap.containsKey("region")&&
				requestMap.containsKey("Ua")){
    		return true;
    	}
		return false;
	}
} 
