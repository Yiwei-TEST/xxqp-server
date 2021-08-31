
package com.sy599.game.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class JsonWrapper {
    private JSONObject json;

    public JsonWrapper(JSONObject json) {
        this.json = json;
    }

    public JsonWrapper(String jsonStr) {
        this.json = StringUtils.isBlank(jsonStr)?new JSONObject():JSONObject.parseObject(jsonStr);
    }

    public long getLong(int key, long defaultValue) {
        String strValue = this.getJsonString(key);
        return StringUtils.isBlank(strValue) ? defaultValue : Long.parseLong(strValue);
    }

    public long getLong(String key, long defaultValue) {
        String strValue = this.getJsonString(key);
        return StringUtils.isBlank(strValue) ? defaultValue : Long.parseLong(strValue);
    }

    private String getJsonString(int key) {
        return this.getJsonString(Integer.toString(key));
    }

    private String getJsonString(String key) {
        String value = "";

        try {
            value = this.json.getString(key);
        } catch (Exception var4) {
            ;
        }

        return value;
    }

    public JSONObject getJosn(String key) {
        return this.json.getJSONObject(key);
    }

    public void putLong(int key, long value) {
        this.json.put(Integer.toString(key), Long.toString(value));
    }

    public void putLong(String key, long value) {
        this.json.put(key, Long.toString(value));
    }

    public int getInt(int key, int defaultValue) {
        return this.getInt(Integer.toString(key), defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        String strValue = this.getJsonString(key);
        return StringUtils.isBlank(strValue) ? defaultValue : Integer.parseInt(strValue);
    }

    public boolean isHas(Object key) {
        return this.json.containsKey(key);
    }

    public void putInt(int key, int value) {
        this.json.put(Integer.toString(key), Integer.toString(value));
    }

    public void putInt(String key, int value) {
        this.json.put(key, Integer.toString(value));
    }

    public void addInt(int key, int value) {
        this.putInt(key, this.getInt(key, 0) + value);
    }

    public float getFloat(int key, float defaultValue) {
        String strValue = this.getJsonString(key);
        return StringUtils.isBlank(strValue) ? defaultValue : Float.parseFloat(strValue);
    }

    public void putFloat(int key, float value) {
        this.json.put(Integer.toString(key), Float.toString(value));
    }

    public String getString(int key) {
        return this.getJsonString(key);
    }

    public String getString(String key) {
        return this.getJsonString(key);
    }

    public void putString(int key, String value) {
        this.json.put(Integer.toString(key), value);
    }

    public void putString(String key, String value) {
        this.json.put(key, value);
    }

    public void putMap(int key, Map<String, ?> map) {
        this.json.put(Integer.toString(key), map);
    }

    public List<?> getList(int key) {
        Object value = null;

        try {
            value = (List)this.json.get(Integer.toString(key));
        } catch (Exception var4) {
            ;
        }

        if (value == null) {
            value = new ArrayList();
        }

        return (List)value;
    }

    public List<?> getList(int key, Class clazz) {
        ArrayList result = new ArrayList();

        try {
            JSONArray arr = (JSONArray)this.json.get(Integer.toString(key));

            for(int index = 0; index < arr.size(); ++index) {
                JSONObject jsonObj = arr.getJSONObject(index);
                result.add(JSONObject.toJavaObject(jsonObj, clazz));
            }
        } catch (Exception var7) {
        }

        return result;
    }

    public void putList(int key, List<?> list) {
        this.json.put(Integer.toString(key), list);
    }

    public Map<String, ?> getMap(int key) {
        Object value = null;

        try {
            value = (Map)this.json.get(Integer.toString(key));
        } catch (Exception var4) {
            ;
        }

        if (value == null) {
            value = new HashMap();
        }

        return (Map)value;
    }

    public Map<String, ?> getMap(int key, Class clazz) {
        HashMap result = new HashMap();

        try {
            JSONObject value = (JSONObject)this.json.get(Integer.toString(key));
            Iterator iterator = value.keySet().iterator();

            while(iterator.hasNext()) {
                String strkey = (String)iterator.next();
                result.put(strkey, JSONObject.toJavaObject((JSONObject)value.get(strkey), clazz));
            }
        } catch (Exception var7) {
        }

        return result;
    }

    public int getKeyCount() {
        return this.json.keySet().size();
    }

    public Set getKeySet() {
        return this.json.keySet();
    }

    public void removeKey(int key) {
        this.json.remove(String.valueOf(key));
    }

    public String toString() {
        return this.json.toString();
    }
}
