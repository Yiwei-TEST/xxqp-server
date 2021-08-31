package com.sy.sanguo.common.util;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

/**
 * JSONObject的建造者模式
 * 
 * @author Administrator
 *
 */
public class MessageBuilder implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final JSONObject jsonObject;

	private MessageBuilder() {
		jsonObject = new JSONObject();
	}

	private MessageBuilder(boolean ordered) {
		jsonObject = new JSONObject(ordered);
	}

	private MessageBuilder(JSONObject json) {
		if (json == null) {
			jsonObject = new JSONObject();
		} else {
			jsonObject = (JSONObject)json.clone();
		}
	}
	
	private MessageBuilder(Map<String, Object> map) {
		if (map == null) {
			jsonObject = new JSONObject();
		} else {
			jsonObject = new JSONObject(map);
		}
	}

	public final static MessageBuilder newInstance() {
		return new MessageBuilder();
	}

	public final static MessageBuilder newInstance(boolean ordered) {
		return new MessageBuilder(ordered);
	}

	public final static MessageBuilder newInstance(Map<String, Object> map) {
		return new MessageBuilder(map);
	}

	public final static MessageBuilder newInstance(Map<String, Object> map, boolean ordered) {
		return new MessageBuilder(ordered).builder(map);
	}

	public final static MessageBuilder newInstance(JSONObject json) {
		return new MessageBuilder(json);
	}

	public MessageBuilder builder(String key, Object value) {
		jsonObject.put(key, value);
		return this;
	}

	public MessageBuilder builder(Map<String, Object> map) {
		jsonObject.putAll(map);
		return this;
	}

	public MessageBuilder builder(JSONObject json) {
		for (Entry<String, Object> keyValue : json.entrySet()) {
			jsonObject.put(keyValue.getKey(), keyValue.getValue());
		}
		return this;
	}

	public MessageBuilder builderCodeMessage(Object code, Object message) {
		jsonObject.put("code", code);
		jsonObject.put("message", message);
		return this;
	}

	public Object get(String key) {
		return jsonObject.get(key);
	}

	public Object remove(String key) {
		return jsonObject.remove(key);
	}

	public void clear() {
		jsonObject.clear();
	}
	
	public JSONObject loadJSONObject(){
		return jsonObject;
	}

	public String toString() {
		return jsonObject.toString();
	}

	public MessageBuilder copy() {
		return MessageBuilder.newInstance(jsonObject);
	}
}
