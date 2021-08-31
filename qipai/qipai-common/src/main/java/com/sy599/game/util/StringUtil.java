package com.sy599.game.util;

import com.sy599.game.staticdata.StaticDataManager;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class StringUtil {
	private static final String split_mark = ";";

	public static String filterEmoji(String source) {
		if (StringUtils.isNotBlank(source)) {
			return source.replaceAll("[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]", "");
		} else {
			return source;
		}
	}

	public static boolean isBlank(String str) {
		if (StringUtils.isBlank(str) || str.equals("null")) {
			return true;
		}
		return false;
	}

	/**
	 * 小写字符串
	 * 
	 * @param length
	 * @return
	 */
	public static String getRandomLowerString(int length) {
		String str = "abcdefghijklmnopqrstuvwxyz";
		Random random = new Random();
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < length; ++i) {
			int number = random.nextInt(str.length());// [0,62)
			sb.append(str.charAt(number));
		}
		return sb.toString().toLowerCase();
	}

	/**
	 * 随机字符串
	 * 
	 * @param length
	 * @return
	 */
	public static String getRandomString(int length) {
		Random random = new Random();

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < length; ++i) {
			int number = random.nextInt(3);
			long result = 0;

			switch (number) {
			case 0:
				result = Math.round(Math.random() * 25 + 65);
				sb.append(String.valueOf((char) result));
				break;
			case 1:
				result = Math.round(Math.random() * 25 + 97);
				sb.append(String.valueOf((char) result));
				break;
			case 2:
				sb.append(String.valueOf(new Random().nextInt(10)));
				break;
			}
		}
		return sb.toString();
	}

	/**
	 * 随机获得一个UUID，这个值是唯一的
	 * 
	 * @return String
	 * @throws
	 */
	public static String randomUUID() {
		return UUID.randomUUID().toString().replace("_", "");
	}

	/**
	 * 将字符串分隔后，转换成double[]
	 * 
	 * @param str
	 * @param delimiter
	 * @return double[]
	 */
	public static double[] explodeToDoubleArray(String str, String delimiter) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(delimiter);
		double[] result = new double[strArray.length];
		int i = 0;
		for (String val : strArray) {
			result[i] = Double.valueOf(val);
			i++;
		}
		return result;
	}

	public static String implodeLongLists(List<List<Long>> lists) {
		StringBuilder sb = new StringBuilder();
		for (List<Long> list : lists) {
			sb.append(implode(list, ",")).append(";");
		}
		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public static List<List<Long>> explodeToLongLists(String str) {
		List<List<Long>> lists = new ArrayList<>();
		if (StringUtils.isBlank(str)){
			return lists;
		}

		String strArray[] = str.split(";");
		for (String listValues : strArray) {
			List<Long> list = explodeToLongList(listValues);
			if (list != null) {
				lists.add(list);
			}
		}
		if (str.endsWith(";")&&(lists.size()==0||(lists.get(lists.size()-1).size()>0))){
			lists.add(new ArrayList<Long>());
		}
		return lists;
	}

	public static String implodeLists(List<List<Integer>> lists) {
		StringBuilder sb = new StringBuilder();
		for (List<Integer> list : lists) {
			sb.append(implode(list, ",")).append(";");
		}
		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public static List<List<Integer>> explodeToLists(String str) {
		if (StringUtils.isBlank(str))
			return null;
		List<List<Integer>> lists = new ArrayList<>();
		String strArray[] = str.split(";");
		for (String listValues : strArray) {
			List<Integer> list = explodeToIntList(listValues);
			if (list != null) {
				lists.add(list);
			}

		}
		return lists;
	}

	/**
	 * 将字符串分隔后，转换成double[]
	 * 
	 * @param str
	 * @return double[]
	 */
	public static double[] explodeToDoubleArray(String str) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(StaticDataManager.DELIMITER);
		double[] result = new double[strArray.length];
		int i = 0;
		for (String val : strArray) {
			result[i] = Double.valueOf(val);
			i++;
		}
		return result;
	}

	/**
	 * 将字符串分隔后，转换成double[]
	 * 
	 * @param str
	 * @return double[]
	 */
	public static float[] explodeToFloatArray(String str) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(StaticDataManager.DELIMITER);
		float[] result = new float[strArray.length];
		int i = 0;
		for (String val : strArray) {
			result[i] = Float.valueOf(val);
			i++;
		}
		return result;
	}

	/**
	 * 将字符串分隔后，转换成long[]
	 * 
	 * @param str
	 * @return long[]
	 */
	public static long[] explodeToLongArray(String str) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(StaticDataManager.DELIMITER);
		long[] result = new long[strArray.length];
		int i = 0;
		for (String val : strArray) {
			result[i] = Long.valueOf(val);
			i++;
		}
		return result;
	}

	/**
	 * 将字符串分隔后，转换成int[]
	 * 
	 * @param str
	 * @return int[]
	 */
	public static int[] explodeToIntArray(String str) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(StaticDataManager.DELIMITER);
		int[] result = new int[strArray.length];
		int i = 0;
		for (String val : strArray) {
			if (!StringUtils.isBlank(str)) {
				result[i] = Integer.valueOf(val);
				i++;
			}
		}
		return result;
	}

	/**
	 * 将字符串分隔后，转换成int[]
	 * 
	 * @param str
	 * @return int[]
	 */
	public static String[] explodeToStringArray(String str) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(StaticDataManager.DELIMITER);
		String[] result = new String[strArray.length];
		int i = 0;
		for (String val : strArray) {
			result[i] = val;
			i++;
		}
		return result;
	}

	public static List<String> explodeToStringList(String str, String delimiter) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(delimiter);
		List<String> result = new ArrayList<>(strArray.length);
		for (String val : strArray) {
			result.add(val);
		}
		return result;
	}

	public static String[] explodeToStringArray(String str, String delimiter) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(delimiter);
		String[] result = new String[strArray.length];
		int i = 0;
		for (String val : strArray) {
			result[i] = val;
			i++;
		}
		return result;
	}

	public static List<Integer> explodeToIntList(String str) {
		return explodeToIntList(str, StaticDataManager.DELIMITER);
	}

	public static List<Integer> explodeToIntList(String str, String split) {
		if (StringUtils.isBlank(str) || str.equals("null") || str.equals("undefined"))
			return null;
		String strArray[] = str.split(split);
		List<Integer> result = new ArrayList<Integer>(strArray.length);

		for (String val : strArray) {
			result.add(Integer.valueOf(val));
		}
		return result;
	}

	public static List<Long> explodeToLongList(String str) {
		if (str==null)
			return null;
		String strArray[] = str.split(StaticDataManager.DELIMITER);
		List<Long> result = new ArrayList<Long>(strArray.length);

		for (String val : strArray) {
			if (val.length()>0)
			result.add(Long.valueOf(val));
		}
		return result;
	}

	public static Set<Integer> explodeToIntSet(String str) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(StaticDataManager.DELIMITER);
		Set<Integer> result = new HashSet<Integer>(strArray.length);

		for (String val : strArray) {
			result.add(Integer.valueOf(val));
		}
		return result;
	}

	/**
	 * 将字符串分隔后，转换成int[]
	 * 
	 * @param str
	 * @param delimiter
	 * @return int[]
	 */
	public static int[] explodeToIntArray(String str, String delimiter) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(delimiter);
		int[] result = new int[strArray.length];
		int i = 0;
		for (String val : strArray) {
			result[i] = Integer.valueOf(val);
			i++;
		}
		return result;
	}

	/**
	 * 将array组合成用delimiter分隔的字符串
	 * 
	 * @param array
	 * @param delimiter
	 * @return String
	 */
	public static String implode(int[] array, String delimiter) {
		StringBuilder sb = new StringBuilder("");
		for (int i : array) {
			sb.append(i);
			sb.append(delimiter);
		}
		if (sb.length() > 0) {
			sb.delete(sb.length() - 1, sb.length());
		}
		return sb.toString();
	}

	/**
	 * 将array组合成用delimiter分隔的字符串
	 * 
	 * @param array
	 * @param delimiter
	 * @return String
	 */
	public static String implodeLongToStr(List<Long> array, String delimiter) {
		StringBuilder sb = new StringBuilder("");
		for (long i : array) {
			sb.append(i);
			sb.append(delimiter);
		}
		if (sb.length() > 0) {
			sb.delete(sb.length() - 1, sb.length());
		}
		return sb.toString();
	}

	/**
	 * 将array组合成用delimiter分隔的字符串
	 * 
	 * @param array
	 * @param delimiter
	 * @return String
	 */
	public static String implode(List<?> array, String delimiter) {
		if (array == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (Object i : array) {
			sb.append(i);
			sb.append(delimiter);
		}
		if (sb.length() > 0) {
			sb.delete(sb.length() - 1, sb.length());
		}
		return sb.toString();
	}

	/**
	 * 将array组合成用delimiter分隔的字符串
	 * 
	 * @param array
	 * @return String
	 */
	public static String listToString(List<String> array) {
		StringBuilder sb = new StringBuilder("");
		for (String i : array) {
			sb.append(i);
			sb.append(split_mark);
		}
		if (sb.length() > 0) {
			sb.delete(sb.length() - 1, sb.length());
		}
		return sb.toString();
	}

	/**
	 * String转List
	 * 
	 * @param str
	 * @return
	 */
	public static List<String> stringToList(String str) {
		if (StringUtils.isBlank(str)) {
			return null;

		}
		String[] arr = str.split(split_mark);

		List<String> list = new ArrayList<String>();
		for (String s : arr) {
			if (!StringUtils.isBlank(s)) {
				list.add(s);

			}

		}
		return list;
	}

	/**
	 * 将array组合成用,分隔的字符串
	 * 
	 * @param array
	 * @return String
	 */
	public static String implode(int[] array) {
		StringBuilder sb = new StringBuilder("");
		for (int i : array) {
			sb.append(i);
			sb.append(StaticDataManager.DELIMITER);
		}
		if (sb.length() > 0) {
			sb.delete(sb.length() - 1, sb.length());
		}
		return sb.toString();
	}

	/**
	 * 将array组合成用,分隔的字符串
	 * 
	 * @param array
	 * @return String
	 */
	public static String implode(List<?> array) {
		if (array == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder("");
		for (Object i : array) {
			sb.append(i);
			sb.append(",");
		}
		if (sb.length() > 0) {
			sb.delete(sb.length() - 1, sb.length());
		}
		return sb.toString();
	}

	public static String implode(long[] array) {
		StringBuilder sb = new StringBuilder("");
		for (long i : array) {
			sb.append(i);
			sb.append(StaticDataManager.DELIMITER);
		}
		if (sb.length() > 0) {
			sb.delete(sb.length() - 1, sb.length());
		}
		return sb.toString();
	}

	public static String implode(String[] array) {
		return StringUtils.join(array, StaticDataManager.DELIMITER);
	}

	public static String implode(String[] array, String delimiter) {
		return StringUtils.join(array, delimiter);
	}

	public static Integer getInt(String val, int def) {
		if (!StringUtils.isBlank(val)) {
			return Integer.valueOf(val);
		}
		return def;
	}

	public static boolean getBoolean(String val, boolean def) {
		if (!StringUtils.isBlank(val)) {
			return Boolean.valueOf(val);
		}
		return def;
	}

	/**
	 * getValue读取csv
	 * 
	 * @param values
	 * @param index
	 * @return
	 */
	public static String getValue(String[] values, int index) {
		if (index >= values.length) {
//			LogUtil.msgLog.error("getValue index > length-->" + index + ":" + JacksonUtil.writeValueAsString(values));
			return "";
		}
		return values[index];
	}

	//
	// /**
	// * getValue读取csv
	// *
	// * @param values
	// * @param index
	// * @return
	// */
	// public static <T> List<T> getListValue(String[] values, int index,
	// Class<T> clazz) {
	// if (index >= values.length) {
	// LogUtil.msgLog.error("getValue index > length-->" + index + ":" +
	// JacksonUtil.writeValueAsString(values));
	// return new ArrayList<T>();
	// }
	// if (clazz.getName().equals(PaohzCard.class.getName())) {
	// return (List<T>) explodePhz(values[index], ",");
	// }
	// if (clazz.getName().equals(M.class.getName())) {
	// return (List<T>) explodePhz(values[index], ",");
	// }
	// return null;
	// }

	public static long getLongValue(String[] values, int index) {
		String value = getValue(values, index);
		if (StringUtils.isBlank(value)) {
			return 0;
		}
		return Long.parseLong(value);
	}

	public static int getIntValue(String[] values, int index) {
		String value = getValue(values, index);
		if (StringUtils.isBlank(value)) {
			return 0;
		}
		return Integer.parseInt(value);
	}

    public static int getIntValue(String[] values, int index, int def) {
        String value = getValue(values, index);
        if (StringUtils.isBlank(value)) {
            return def;
        }
        return Integer.parseInt(value);
    }

	public static int getIntValue(List<Integer> values, int index, int def) {
		if (index >= values.size()) {
			return def;
		}
		return values.get(index);
	}

	public static float getFloatValue(String[] values, int index) {
		String value = getValue(values, index);
		if (StringUtils.isBlank(value)) {
			return 0;
		}
		return Float.parseFloat(value);
	}

	/**
	 * 解析概率
	 * 
	 * @param source
	 * @return List<List<Integer>> <br>
	 *         0号元素为region 1号元素为values
	 */
	public static List<List<Integer>> parseRate(String source) {
		List<List<Integer>> result = new ArrayList<List<Integer>>();
		String[] strs = source.split(",");
		List<Integer> region = new ArrayList<Integer>();
		List<Integer> values = new ArrayList<Integer>();
		for (String str : strs) {
			String[] m = str.split(":");
			region.add(Integer.valueOf(m[1]));
			values.add(Integer.valueOf(m[0]));
		}
		result.add(region);
		result.add(values);
		return result;
	}

	public static int parseVersions(String versions) {
		if (StringUtils.isBlank(versions)) {
			return 0;
		}

        versions = versions.replace(".", "").replace("v", "").replace("g", "").replace("c", "").trim();
		try {
			int v = Integer.parseInt(versions);
			return v;
		} catch (Exception e) {
			LogUtil.e("parseVersions err ", e);
		}
		return 0;
	}

	public static String filterEmoji1(String source) {
		if (StringUtils.isNotBlank(source)) {
			return source.replaceAll("[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]", "*");
		} else {
			return source;
		}
	}

    /**
     * 截取前几个汉字
     *
     * @param src
     * @param length
     * @return
     */
    public static String cutHanZi(String src, int length) {
        String res = "";
        if (isBlank(src)) {
            return "";
        }
        try {
            if (src.length() <= length) {
                res = src;
            } else {
                res = src.substring(0, length);
            }
        } catch (Exception e) {
            LogUtil.e("cutHanZi|error|" + src, e);
        }
        return res;
    }

	public static void main(String[] arg) {
		try {
			System.out.println(parseVersions("2.0.1"));
			;
			// String text =
			// "This is a smiley \uD83C\uDFA6 face\uD860\uDD5D \uD860\uDE07 \uD860\uDEE2 \uD863\uDCCA \uD863\uDCCD \uD863\uDCD2 \uD867\uDD98 ";
			// System.out.println(text);
			// System.out.println(text.length());
			// System.out.println(text.replaceAll("[\\ud83c\\udc00-\\ud83c\\udfff]|[\\ud83d\\udc00-\\ud83d\\udfff]|[\\u2600-\\u27ff]",
			// "*"));
			// System.out.println(filterEmoji1(text));
            String s = "34测试";
            System.out.println(cutHanZi(s,5));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
