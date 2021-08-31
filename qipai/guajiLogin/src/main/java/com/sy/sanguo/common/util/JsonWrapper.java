package com.sy.sanguo.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 * {@link JSONObject}的包装类，提供一些辅助功�?
 * 
 * @author taohuiliang
 * @date Jul 19, 2012
 * @version v1.0
 */
public class JsonWrapper {
	private JSONObject json;

	public JsonWrapper(JSONObject json) {
		this.json = json;
	}

	public JsonWrapper(String jsonStr) {
		this.json = org.apache.commons.lang3.StringUtils.isBlank(jsonStr)?new JSONObject():JSONObject.parseObject(jsonStr);
	}

	public long getLong(int key, long defaultValue) {
		String strValue = getJsonString(key);
		if (StringUtils.isBlank(strValue)) {
			return defaultValue;
		} else {
			return Long.parseLong(strValue);
		}
	}
	
	public long getLong(String key, long defaultValue) {
		String strValue = getJsonString(key);
		if (StringUtils.isBlank(strValue)) {
			return defaultValue;
		} else {
			return Long.parseLong(strValue);
		}
	}

	private String getJsonString(int key) {
		return getJsonString(Integer.toString(key));
	}

	public JSONObject getJson(String key){
		return json.getJSONObject(key);
	}
	
	private String getJsonString(String key) {
		String value = "";
		try {
			value = json.getString(key);
		} catch (Exception e) {

		}
		return value;
	}

	public void putLong(int key, long value) {
		json.put(Integer.toString(key), Long.toString(value));
	}
	
	public void putLong(String key, long value) {
		json.put(key, Long.toString(value));
	}
	
	public JSONObject getJosn(String key) {
		return json.getJSONObject(key);
	}

	public int getInt(int key, int defaultValue) {
		return getInt(Integer.toString(key), defaultValue);
	}

	public int getInt(String key, int defaultValue) {
		String strValue = getJsonString(key);
		if (StringUtils.isBlank(strValue)) {
			return defaultValue;
		} else {
			return Integer.valueOf(strValue);
		}
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		if (hasKey(key)) {
			return json.getBoolean(key);

		}
		return defaultValue;
	}

	public boolean hasKey(String key) {
		return json.containsKey(key);
	}

	public void putInt(int key, int value) {
		json.put(Integer.toString(key), Integer.toString(value));
	}

	public void putInt(String key, int value) {
		json.put(key, Integer.toString(value));
	}

	public void addInt(int key, int value) {
		this.putInt(key, getInt(key, 0) + value);
	}

	public float getFloat(int key, float defaultValue) {
		String strValue = getJsonString(key);
		if (StringUtils.isBlank(strValue)) {
			return defaultValue;
		} else {
			return Float.parseFloat(strValue);
		}
	}

	public void putFloat(int key, float value) {
		json.put(Integer.toString(key), Float.toString(value));
	}

	public String getString(int key) {
		return getJsonString(key);
	}

	public String getString(String key) {
		return getJsonString(key);
	}

	public void putString(int key, String value) {
		json.put(Integer.toString(key), value);
	}

	public void putString(String key, String value) {
		json.put(key, value);
	}

	/**
	 * @param key
	 * @param map
	 *            建议只放置Map<String,Integer>、List<String,String>
	 *            、List<String,JavaBean> 其它基本类型也可以放置如short、float等
	 */
	public void putMap(int key, Map<String, ?> map) {
		json.put(Integer.toString(key), map);
	}

	/*
	 * public JSONObject getJSONObject(int key){
	 * 
	 * JSONObject value = null; try { value = (JSONObject)
	 * json.get(Integer.toString(key)); } catch (Exception e) {
	 * 
	 * }
	 * 
	 * return value; }
	 * 
	 * public void putJSONObject(int key,JSONObject jsonObj){
	 * json.put(Integer.toString(key), jsonObj); }
	 */
	/**
	 * @param key
	 * @return List<?> ?只能是基本数据类型,返回值不为空</br> <b>建议是Integer和String两种</b>
	 */
	@SuppressWarnings("unchecked")
	public List<?> getList(int key) {
		List<?> value = null;
		try {
			value = (List<?>) json.get(Integer.toString(key));
		} catch (Exception e) {

		}

		if (value == null) {
			value = new ArrayList();
		}

		return value;
	}

	/**
	 * @param key
	 * @param clazz
	 *            必须为JavaBean
	 * @return List 返回值不会为null
	 */
	@SuppressWarnings("unchecked")
	public List<?> getList(int key, Class clazz) {
		List result = new ArrayList();
		try {
			JSONArray arr = (JSONArray) json.get(Integer.toString(key));
			for (int index = 0; index < arr.size(); index++) {
				JSONObject jsonObj = arr.getJSONObject(index);
				result.add(JSONObject.toJavaObject(jsonObj, clazz));
			}

		} catch (Exception e) {

		}

		return result;
	}

	/**
	 * @param key
	 * @param list
	 *            建议只放置List<Integer>、List<String> 、List<JavaBean>
	 *            其它基本类型也可以放置如short、float等
	 * @throws
	 */
	public void putList(int key, List<?> list) {

		json.put(Integer.toString(key), list);
	}

	/**
	 * @param key
	 * @return Map<String,?>返回值一定不为空,?为只能为基本数据类型，Integer，String等等
	 * @throws
	 */
	@SuppressWarnings("unchecked")
	public Map<String, ?> getMap(int key) {
		Map<String, ?> value = null;
		try {
			value = (Map<String, ?>) json.get(Integer.toString(key));
		} catch (Exception e) {

		}

		if (value == null) {
			value = new HashMap();
		}

		return value;
	}

	/**
	 * @param key
	 * @return Map<String,？> 返回值一定不为空 ,?为clazz对应的JavaBean
	 * @throws
	 */
	@SuppressWarnings("unchecked")
	public Map<String, ?> getMap(int key, Class clazz) {
		Map result = new HashMap();
		try {
			JSONObject value = (JSONObject) json.get(Integer.toString(key));
			Iterator iterator = value.keySet().iterator();
			while (iterator.hasNext()) {
				String strkey = (String) iterator.next();
				result.put(strkey, JSONObject.toJavaObject((JSONObject) value.get(strkey), clazz));
			}
		} catch (Exception e) {

		}

		return result;
	}

	/**
	 * 返回json 中Key数量
	 * 
	 * @return int
	 * @throws
	 */
	public int getKeyCount() {
		return json.keySet().size();
	}

	/**
	 * 返回json 中所有KEY
	 * 
	 * @return Set
	 * @throws
	 */
	@SuppressWarnings("unchecked")
	public Set getKeySet() {
		return json.keySet();
	}

	public void removeKey(String key) {
		json.remove(key);
	}

	public void removeKey(int key) {
		json.remove(String.valueOf(key));
	}

	public String toString() {
		return json.toString();
	}

	public boolean isHas(Object key) {
		return json.containsKey(key);

	}

	public static void main(String[] args) {
		// JsonWrapper obj = new JsonWrapper("{}");

		// Map<Integer,HeroTrainInfo> map = new
		// HashMap<Integer,HeroTrainInfo>();
		// map.put(1, new HeroTrainInfo(1,1,1));
		// map.put(2, new HeroTrainInfo(2,2,2));
		// obj.put("test", map);

		// Map<Integer,String> vals = new HashMap<Integer,String>();
		// vals.put(1, "1");
		// vals.put(2, "2");
		// obj.put("vals", vals);
		//
		//
		// List<String> stringList= new ArrayList<String>();
		// stringList.add("str1");
		// stringList.add("str2");
		// obj.put("list", stringList);
		//
		// List<Float> integerList = new ArrayList<Float>();
		// integerList.add(1.0f);
		// integerList.add(2.0f);
		// integerList.add(3.0f);
		// obj.put("integerList", integerList);
		// List<Float> result = (List<Float>) obj.get("integerList");
		// System.out.print(result.contains(1.0f));

		/*
		 * JsonWrapper wrap = new JsonWrapper(""); List<String> list = new
		 * ArrayList<String>(); list.add("adb"); list.add("abc");
		 * wrap.putList(123,list );
		 */
		JsonWrapper wrap = new JsonWrapper("");
		List<String> list = new ArrayList<String>();
		list.add("adb");
		list.add("abc");
		wrap.putList(123, list);

	}
}
