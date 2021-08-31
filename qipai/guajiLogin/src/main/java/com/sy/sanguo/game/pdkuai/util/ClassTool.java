package com.sy.sanguo.game.pdkuai.util;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

public class ClassTool {
	public static void setValue(Object object, Map<String, Object> valueMap) {
		try {
			for (Entry<String, Object> entry : valueMap.entrySet()) {
				String setName = "set" + captureName(entry.getKey());
				Method method = getMenthod(object, setName, getValueClass(entry.getValue()));
				if (method != null)
					method.invoke(object, entry.getValue());
			}
		} catch (Exception e) {
			LogUtil.e("ClassTool.setValue e:", e);
		}

	}

	private static Method getMenthod(Object object, String methodname, Class<?>... parameterTypes) {
		try {
			Method method = object.getClass().getMethod(methodname, parameterTypes);
			return method;
		} catch (SecurityException e) {
			e.printStackTrace();
			LogUtil.e("getMenthod err", e);
		} catch (NoSuchMethodException e) {
			return null;
		}
		return null;
	}

	private static Class<?> getValueClass(Object object) {
		// Class<?> cl = object.getClass();
		if (object instanceof java.lang.Long) {
			return long.class;
		}else if (object instanceof Integer) {
			return int.class;
		}else if (object instanceof String) {
			return String.class;
		}else if(object instanceof Boolean){
			return boolean.class;
		}else{
			return object.getClass();
		}

	}

	/**
	 * 首字母大写
	 * 
	 * @param name
	 * @return
	 */
	public static String captureName(String name) {
		// name = name.substring(0, 1).toUpperCase() + name.substring(1);
		// return name;
		char[] cs = name.toCharArray();
		cs[0] -= 32;
		return String.valueOf(cs);

	}
}
