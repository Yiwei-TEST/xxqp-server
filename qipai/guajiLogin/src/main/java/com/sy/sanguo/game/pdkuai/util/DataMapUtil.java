package com.sy.sanguo.game.pdkuai.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import com.sy.sanguo.common.util.StringUtil;

public class DataMapUtil {
	

	private static Object parse(Class<?> key, String s) {
		if (key.equals(Integer.class)) {
			return Integer.parseInt(s);
		} else if (key.equals(Long.class)) {
			return Long.parseLong(s);
		} else if (key.equals(Float.class)) {
			return Float.parseFloat(s);
		} else if (key.equals(String.class)) {
			return s;
		}
		return null;

	}
	public static <T> T implodeT(String str, Class<?> key, Class<?> value, String separator) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		Map<Object, Object> map = new LinkedHashMap<>();
		String[] valArr = str.split(separator);
		Object oKey = parse(key, valArr[0]);
		Object oValue = parse(value, valArr[1]);
		if (oKey == null || oValue == null) {
			return null;
		}
		map.put(oKey, oValue);
		return (T) map;
	}

	/**
	 * map转为 1,1;2,2
	 * 
	 * @param map
	 * @param separator
	 *            ;
	 * @return
	 */
	public static String explodeIntToArrayString(Map<Integer, Integer> map, String separator) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		for (int key : map.keySet()) {
			int val = map.get(key);
			sb.append(key).append(",").append(val).append(";");
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * map转为 1,1;2,2
	 * 
	 * @param map
	 * @param separator
	 *            ;
	 * @return
	 */
	public static String explode(Map<?, ?> map) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		for (Object key : map.keySet()) {
			Object val = map.get(key);
			sb.append(key).append(",").append(val).append(";");
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * string转为 1:1,2,3;2:1,2,3
	 * 
	 * @param map
	 * @return
	 */
	public static Map<Integer, List<Integer>> toListMap(String str) {
		Map<Integer, List<Integer>> list = new HashMap<>();
		if (StringUtils.isBlank(str)) {
			return list;
		}
		if (str.equals("null")) {
			return list;
		}
		Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
		String[] values = str.split(";");
		for (String value : values) {
			String[] arr = value.split(":");
			int key = Integer.parseInt(arr[0]);
			String val = arr[1];
			map.put(key, StringUtil.explodeToIntList(val));
		}
		return map;
	}

	/**
	 * string转为 1:{1:123};2:{1:123}
	 * 
	 * @param map
	 * @return
	 */
	public static Map<Integer, Map<Integer, List<Integer>>> toListMapMap(String str) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		Map<Integer, Map<Integer, List<Integer>>> map = new HashMap<>();
		String[] values = str.split("!");
		for (String value : values) {
			String[] arr = value.split("_");
			int key = Integer.parseInt(arr[0]);
			String val = arr[1];
			map.put(key, toListMap(val));
		}
		return map;
	}

	public static String explodeListMapMap(Map<Integer, Map<Integer, List<Integer>>> map) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		for (int key : map.keySet()) {
			Map<Integer, List<Integer>> val = map.get(key);
			sb.append(key).append("_").append(explodeListMap(val)).append("!");
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();

	}

	public static String explodeListMap(Map<Integer, List<Integer>> map) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		for (int key : map.keySet()) {
			List<Integer> val = map.get(key);
			sb.append(key).append(":").append(StringUtil.implode(val)).append(";");
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();

	}

	/**
	 * map转为 1:2,3,4;2:2,3,4
	 * 
	 * @param map
	 * @param separator
	 *            ;
	 * @return
	 */
	public static String explodeArrMap(Map<Integer, int[]> map) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		for (int key : map.keySet()) {
			int[] val = map.get(key);
			sb.append(key).append(":").append(StringUtil.implode(val)).append(";");
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * map转为 1,1;2,2
	 * 
	 * @param map
	 * @param separator
	 *            ;
	 * @return
	 */
	public static Map<Integer, int[]> toArrMap(String str) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		Map<Integer, int[]> map = new TreeMap<Integer, int[]>();
		String[] values = str.split(";");
		for (String value : values) {
			String[] arr = value.split(":");
			int key = Integer.parseInt(arr[0]);
			String val = arr[1];
			map.put(key, StringUtil.explodeToIntArray(val));
		}
		return map;
	}

	/**
	 * map转为 1,1;2,2
	 * 
	 * @param map
	 * @param separator
	 *            ;
	 * @return
	 */
	public static String explodeToArrayString(Map<Integer, String> map, String separator) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		String[] arr = new String[map.size()];
		for (int key : map.keySet()) {
			arr[key] = map.get(key);
		}
		return StringUtils.join(arr, separator);
	}

	/**
	 * @param str
	 *            1,100;3,300
	 * @return Map<Integer,Integer>
	 */
	public static Map<Integer, Integer> implode(String str) {
		if (str == null) {
			return new HashMap<Integer, Integer>();
		}
		if (StringUtils.isBlank(str)) {
			return null;
		}
		Map<Integer, Integer> map = new LinkedHashMap<Integer, Integer>();
		for (String val : StringUtil.explodeToStringArray(str, ";")) {
			String[] valArr = val.split(",");
			if (valArr.length < 2 || StringUtils.isBlank(valArr[0]) || valArr[0] == null || valArr[1] == null || StringUtils.isBlank(valArr[1])) {
				continue;
			}
			map.put(Integer.parseInt(valArr[0]), Integer.parseInt(valArr[1]));
		}
		return map;
	}

	/**
	 * @param str
	 *            1,0.3;3,0.7
	 * @return Map<Integer,Integer>
	 */
	public static Map<Integer, Float> implodeStrToFloat(String str) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		Map<Integer, Float> map = new LinkedHashMap<Integer, Float>();
		for (String val : StringUtil.explodeToStringArray(str, ";")) {
			String[] valArr = val.split(",");
			map.put(Integer.parseInt(valArr[0]), Float.parseFloat(valArr[1]));
		}
		return map;
	}

	/**
	 * @param str
	 *            1,1000;3,2000
	 * @return Map<Integer,Integer>
	 */
	public static Map<Integer, Long> implodeStrToLong(String str) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		Map<Integer, Long> map = new LinkedHashMap<Integer, Long>();
		for (String val : StringUtil.explodeToStringArray(str, ";")) {
			String[] valArr = val.split(",");
			map.put(Integer.parseInt(valArr[0]), Long.parseLong(valArr[1]));
		}
		return map;
	}

	/**
	 * @param str
	 *            1000,1;2000,2
	 * @return Map<Integer,Integer>
	 */
	public static Map<Long, Integer> implodeStrToLongInt(String str) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		Map<Long, Integer> map = new LinkedHashMap<Long, Integer>();
		for (String val : StringUtil.explodeToStringArray(str, ";")) {
			String[] valArr = val.split(",");
			map.put(Long.parseLong(valArr[0]), Integer.parseInt(valArr[1]));
		}
		return map;
	}

	/**
	 * 两个map累加
	 * 
	 * @param target
	 * @param plus
	 * @return
	 */
	public static Map<Integer, Integer> addTo(Map<Integer, Integer> target, Map<Integer, Integer> plus) {
		Object[] os = plus.keySet().toArray();
		Integer key;
		for (int i = 0; i < os.length; i++) {
			key = (Integer) os[i];
			if (target.containsKey(key))
				target.put(key, target.get(key) + plus.get(key));
			else
				target.put(key, plus.get(key));
		}
		return target;
	}

	/**
	 * 两个map累加
	 * 
	 * @param target
	 * @param plus
	 * @return
	 */
	public static Map<Integer, Float> addToFloat(Map<Integer, Float> target, Map<Integer, Float> plus) {
		Object[] os = plus.keySet().toArray();
		Integer key;
		for (int i = 0; i < os.length; i++) {
			key = (Integer) os[i];
			if (target.containsKey(key))
				target.put(key, target.get(key) + plus.get(key));
			else
				target.put(key, plus.get(key));
		}
		return target;
	}

	public static void addToObj(Map<Integer, Object> target, Map<Integer, ?> plus) {
		Object[] os = plus.keySet().toArray();
		Integer key;
		for (int i = 0; i < os.length; i++) {
			key = (Integer) os[i];
			if (target.containsKey(key)) {
				Object value = null;
				Object plus_val = plus.get(key);
				Object target_val = target.get(key);

				if (plus_val instanceof Integer && target_val instanceof Integer) {
					// int类型
					value = Integer.parseInt(plus_val.toString()) + Integer.parseInt(target_val.toString());

				} else if (plus_val instanceof Float && target_val instanceof Float) {
					// float类型
					value = Float.parseFloat(plus_val.toString()) + Float.parseFloat(target_val.toString());

				} else if (plus_val instanceof Long && target_val instanceof Long) {
					// long类型
					value = Long.parseLong(plus_val.toString()) + Long.parseLong(target_val.toString());

				}
				target.put(key, value);

			} else {
				target.put(key, plus.get(key));
			}

		}
	}

	public static List<Integer> toList(int[] arr) {
		List<Integer> list = new ArrayList<>();
		for (int a : arr) {
			list.add(a);
		}
		return list;
	}

	public static List<Integer> indexToValList(List<Integer> arr) {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < arr.size(); i++) {
			if (arr.get(i) != 0) {
				list.add(i);
			}
		}
		return list;
	}

	public static List<Integer> indexToValList(int[] arr) {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] != 0) {
				list.add(i);
			}
		}
		return list;
	}

	/**
	 * arr[2,3] -->[0,0,2,2,2] index 是值 val是数量
	 * 
	 * @param data
	 * @return
	 */
	public static List<Integer> indexToValCountList(List<Integer> data) {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < data.size(); i++) {
			int val = data.get(i);
			if (val > 0) {
				for (int j = 0; j < val; j++) {
					list.add(i);
				}
			}
		}
		return list;
	}

	public static void appendToList(List<Integer> list, int[] arr) {
		for (int a : arr) {
			list.add(a);
		}
	}

	public static void addToArr(int[] target, int[] plus) {
		for (int i = 0; i < plus.length; i++) {
			target[i] = target[i] + plus[i];
			if (i >= target.length) {
				break;
			}
		}
	}

	/**
	 * 两个List合并，同一个index,当targetList中的值为0 并且plusList不为0的时候
	 * 把plusList的值设置为targetList的值
	 * 
	 * @param targetList
	 * @param plusList
	 */
	public static void appendList(List<Integer> targetList, List<Integer> plusList) {
		for (int i = 0; i < plusList.size(); i++) {
			int plusVal = plusList.get(i);
			int targetVal = targetList.get(i);
			if (plusVal != 0 && targetVal == 0) {
				targetList.set(i, plusVal);
			}
		}

	}

	public static void main(String[] args) {
	}
}
