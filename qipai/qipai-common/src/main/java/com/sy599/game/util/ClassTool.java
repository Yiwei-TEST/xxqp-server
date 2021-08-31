
package com.sy599.game.util;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public final class ClassTool {
    private ClassTool() {
    }

    public static void setValue(Object object, Map<String, Object> valueMap) {
        try {
            Iterator var3 = valueMap.entrySet().iterator();

            while(var3.hasNext()) {
                Entry<String, Object> entry = (Entry)var3.next();
                if(entry.getKey()==null||entry.getValue()==null){
                    continue;
                }
                String setName = "set" + captureName(entry.getKey());
                Method method = getMenthod(object, setName, getValueClass(entry.getValue()));
                if (method != null) {
                    method.invoke(object, entry.getValue());
                }
            }
        } catch (Exception var6) {
            LogUtil.e("ClassTool.setValue e:", var6);
        }

    }

    private static Method getMenthod(Object object, String methodname, Class... parameterTypes) {
        try {
            Method method = object.getClass().getMethod(methodname, parameterTypes);
            return method;
        } catch (SecurityException var4) {
            var4.printStackTrace();
            LogUtil.e("getMenthod err", var4);
            return null;
        } catch (NoSuchMethodException var5) {
            return null;
        }
    }

    private static Class<?> getValueClass(Object object) {
        if (object instanceof Long) {
            return Long.TYPE;
        } else if (object instanceof Integer) {
            return Integer.TYPE;
        } else if (object instanceof String) {
            return String.class;
        } else {
            return object instanceof Boolean ? Boolean.TYPE : object.getClass();
        }
    }

    public static String captureName(String name) {
        char[] cs = name.toCharArray();
        cs[0] = (char)(cs[0] - 32);
        return String.valueOf(cs);
    }
}
