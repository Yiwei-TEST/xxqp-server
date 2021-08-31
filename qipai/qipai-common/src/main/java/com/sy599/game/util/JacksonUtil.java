
package com.sy599.game.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

public final class JacksonUtil {

    private JacksonUtil() {
    }

    public static String writeValueAsString(Object value) {
        String result = null;

        try {
            result = JSON.toJSONString(value);
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
