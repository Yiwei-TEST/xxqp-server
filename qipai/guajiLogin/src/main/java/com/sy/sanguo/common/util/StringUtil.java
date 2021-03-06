package com.sy.sanguo.common.util;

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;

public class StringUtil {
    private static final Pattern SQL_INJECTION = Pattern.compile("\\b(and|exec|insert|select|drop|grant|alter|delete|update|count|chr|mid|master|truncate|char|declare|or)\\b|(\\*|;|\\+|'|%)");
    private static final String CHINESE = "[\u0391-\uFFE5]";

	public static String filterEmoji(String source) {
		if (StringUtils.isNotBlank(source)) {
			return source.replaceAll("[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]", "");
		} else {
			return source;
		}
	}

	// public static String filterEmoji(String source) {
	// if (source != null) {
	// Pattern emoji =
	// Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
	// Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
	// Matcher emojiMatcher = emoji.matcher(source);
	// if (emojiMatcher.find()) {
	// source = emojiMatcher.replaceAll("");
	// return source;
	// }
	// return source;
	// }
	// return source;
	// }

	public static void loadFromFile(Properties properties, String dir) throws Exception {
		try {
			FileInputStream fis = new FileInputStream(dir);
			properties.load(fis);
			fis.close();
		} catch (Exception e) {
			throw e;
		}
	}

	public static boolean isFindEmoji(String source) {
		if (source != null) {
			Pattern emoji = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]", Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
			Matcher emojiMatcher = emoji.matcher(source);
			if (emojiMatcher.find()) {
				// source = emojiMatcher.replaceAll("*");
				return true;
			}
			return false;
		}
		return false;
	}

	public static String emojiConvert1(String str) throws UnsupportedEncodingException {
		String patternString = "([\\x{10000}-\\x{10ffff}\ud800-\udfff])";

		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(str);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			try {
				matcher.appendReplacement(sb, "[[" + URLEncoder.encode(matcher.group(1), "UTF-8") + "]]");
			} catch (UnsupportedEncodingException e) {
				throw e;
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * @Description ??????utf8?????????????????????????????????emoji??????????????????
	 * @param str
	 *            ?????????????????????
	 * @return ?????????????????????
	 * @throws UnsupportedEncodingException
	 *             exception
	 */
	public static String emojiRecovery2(String str) throws UnsupportedEncodingException {
		String patternString = "\\[\\[(.*?)\\]\\]";

		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(str);

		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			try {
				matcher.appendReplacement(sb, URLDecoder.decode(matcher.group(1), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw e;
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	public static String contentToEmoji(String content) {
		return StringEscapeUtils.unescapeJava(content.replace("\\\\u", "\\u"));
	}

	public static String emojiToContent(String emoji) {
		return StringEscapeUtils.escapeJava(emoji);
	}

	/**
	 * ???????????????
	 * 
	 * @param length
	 * @return
	 */
	public static String getRandomLowerString(int length) {
		String str = "abcdefghijklmnopqrstuvwxyz";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < length; ++i) {
			int number = random.nextInt(str.length());// [0,62)
			sb.append(str.charAt(number));
		}
		return sb.toString().toLowerCase();
	}

	/**
	 * ???????????????
	 * 
	 * @param length
	 * @return
	 */
	public static String getRandomString(int length) {
		Random random = new Random();

		StringBuffer sb = new StringBuffer();

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
	 * ??????????????????UUID????????????????????????
	 * 
	 * @return String
	 * @throws
	 */
	public static String randomUUID() {
		return UUID.randomUUID().toString().replace("_", "");
	}

	/**
	 * ?????????????????????????????????double[]
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

	/**
	 * ?????????????????????????????????double[]
	 * 
	 * @param str
	 * @return double[]
	 */
	public static double[] explodeToDoubleArray(String str) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(",");
		double[] result = new double[strArray.length];
		int i = 0;
		for (String val : strArray) {
			result[i] = Double.valueOf(val);
			i++;
		}
		return result;
	}

	/**
	 * ?????????????????????????????????long[]
	 * 
	 * @param str
	 * @return long[]
	 */
	public static long[] explodeToLongArray(String str) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(",");
		long[] result = new long[strArray.length];
		int i = 0;
		for (String val : strArray) {
			result[i] = Long.valueOf(val);
			i++;
		}
		return result;
	}

	/**
	 * ?????????????????????????????????int[]
	 * 
	 * @param str
	 * @return int[]
	 */
	public static int[] explodeToIntArray(String str) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(",");
		int[] result = new int[strArray.length];
		int i = 0;
		for (String val : strArray) {
			result[i] = Integer.valueOf(val);
			i++;
		}
		return result;
	}

	/**
	 * ?????????????????????????????????int[]
	 * 
	 * @param str
	 * @return int[]
	 */
	public static String[] explodeToStringArray(String str) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(",");
		String[] result = new String[strArray.length];
		int i = 0;
		for (String val : strArray) {
			result[i] = val;
			i++;
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

	/**
	 * ?????????????????????????????????int[]
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

	public static List<Integer> explodeToIntList(String str) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(",");
		List<Integer> result = new ArrayList<Integer>(strArray.length);

		for (String val : strArray) {
			result.add(Integer.valueOf(val));
		}
		return result;
	}

	public static List<List<Long>> explodeToLongLists(String str) {
		if (StringUtils.isBlank(str))
			return null;
		List<List<Long>> lists = new ArrayList<List<Long>>();
		String strArray[] = str.split(";");
		for (String listValues : strArray) {
			List<Long> list = explodeToLongList(listValues);
			if (list != null) {
				lists.add(list);
			}

		}
		return lists;
	}

	public static List<Long> explodeToLongList(String str) {
		if (StringUtils.isBlank(str))
			return null;
		String strArray[] = str.split(",");
		List<Long> result = new ArrayList<Long>(strArray.length);

		for (String val : strArray) {
			result.add(Long.valueOf(val));
		}
		return result;
	}

	/**
	 * ???array????????????delimiter??????????????????
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
	 * ???array????????????,??????????????????
	 * 
	 * @param array
	 * @return String
	 */
	public static String implode(int[] array) {
		StringBuilder sb = new StringBuilder("");
		for (int i : array) {
			sb.append(i);
			sb.append(",");
		}
		if (sb.length() > 0) {
			sb.delete(sb.length() - 1, sb.length());
		}
		return sb.toString();
	}

	public static String implode(List<?> array){
		return implode(array,",");
	}
	/**
	 * ???array????????????,??????????????????
	 * 
	 * @param array
	 * @param delimiter
	 * @return String
	 */
	public static String implode(List<?> array,String delimiter) {
		StringBuilder sb = new StringBuilder("");
		for (Object i : array) {
			sb.append(i);
			sb.append(delimiter);
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
			sb.append(",");
		}
		if (sb.length() > 0) {
			sb.delete(sb.length() - 1, sb.length());
		}
		return sb.toString();
	}

	public static String implode(String[] array) {
		return StringUtils.join(array, ",");
	}

	public static String implode(String[] array, String delimiter) {
		return StringUtils.join(array, delimiter);
	}

	/**
	 * getValue??????csv
	 * 
	 * @param values
	 * @param index
	 * @return
	 */
	public static String getValue(String[] values, int index) {
		if (index >= values.length) {
			return "";
		}
		return values[index];
	}

	/**
	 * getValue??????csv
	 * 
	 * @param values
	 * @param index
	 * @return
	 */
	public static String getValue(String[] values, int index, String def) {
		if (index >= values.length) {
			return def;
		}
		return values[index];
	}

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
	public static float getFloatValue(String[] values, int index) {
		String value = getValue(values, index);
		if (StringUtils.isBlank(value)) {
			return 0;
		}
		return Float.parseFloat(value);
	}

    /**
     * ????????????sql???????????????true????????????
     *
     * @param src
     * @return
     */
    public static boolean hasSqlInjection(String src) {
        Matcher matcher = SQL_INJECTION.matcher(src);
        return matcher.find();
    }

    /**
     * ????????????
     * ?????? A-Z  a-z  0-9
     * ???????????????
     * ??????????????? ???????????????
     * ?????????6-8 ???
     *
     * @return
     */
    public static String checkPassword(String password) {
        if(StringUtils.isBlank(password)){
            return "??????????????????";
        }
        if (password.length() < 6 || password.length() > 30) {
            return "??????????????????6-30?????????";
        }
        if (!password.matches("^[A-Za-z0-9]+$")) {
            return "??????????????????????????????";
        }
        boolean flag1 = false, flag2 = false;
        for (int i = 0; i < password.length(); i++) {
            if (password.charAt(i) >= 65) {
                flag1 = true;
            } else if (password.charAt(i) <= 57) {
                flag2 = true;
            }
        }
        if (flag1 && flag2) {
            return null;
        } else {
            return "???????????????????????????????????????";
        }
    }

    /**
     * ????????????????????????
     *
     * @param username
     * @return
     */
    public static boolean checkUserNameForSelfRegister(String username) {
        String reg = "^[a-zA-Z0-9]\\w{5,20}$";
        Pattern p1 = Pattern.compile(reg);
        Matcher mat = p1.matcher(username);
        return mat.find();
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    public static int lengthOfNickName(String nickName) {
        return length1(nickName);
    }

    /**
     * ????????????????????????????????????????????????????????????????????????2???????????????1???
     *
     * @return int ????????????????????????
     */
    public static int length1(String s) {
        if (s == null)
            return 0;
        char[] c = s.toCharArray();
        int len = 0;
        for (int i = 0; i < c.length; i++) {
            len++;
            if (!isLetter(c[i])) {
                len++;
            }
        }
        return len;
    }

    public static boolean isLetter(char c) {
        int k = 0x80;
        return c / k == 0 ? true : false;
    }


    /**
     * ????????????????????????????????????????????????????????????????????????2???????????????1???
     *
     * @param s ??????????????????
     * @return ??????????????????
     */
    public static int length2(String s) {
        int valueLength = 0;
        for (int i = 0; i < s.length(); i++) {
            String temp = s.substring(i, i + 1);
            if (temp.matches(CHINESE)) {
                valueLength += 2;
            } else {
                valueLength += 1;
            }
        }
        return valueLength;
    }

    public static void main(String[] arg) {
		String s = "??????##??????~";
        System.out.println(length1(s));
        System.out.println(length2(s));
	}

}
