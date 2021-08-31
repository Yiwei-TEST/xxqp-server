package com.sy.sanguo.common.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.CollationKey;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

public class SdkSm {

	public static class KeySortor implements Comparator<Object> {
		// 关于Collator。
		private Collator collator = Collator.getInstance();// 点击查看中文api详解

		public KeySortor() {
		}

		/**
		 * compare 实现排序。
		 * 
		 * @param o1
		 *            Object
		 * @param o2
		 *            Object
		 */
		public int compare(Object o1, Object o2) {
			// 把字符串转换为一系列比特，它们可以以比特形式与 CollationKeys 相比较
			CollationKey key1 = collator.getCollationKey(o1.toString());// 要想不区分大小写进行比较用o1.toString().toLowerCase()
			CollationKey key2 = collator.getCollationKey(o2.toString());
			return key1.compareTo(key2);// 返回的分别为1,0,-1
			// 分别代表大于，等于，小于。要想按照字母降序排序的话加个“-”号
		}

	}

	/**
	 * 创建MD5的校验码
	 * 
	 * @param key
	 *            签名明文字符串末尾追加的签名Key
	 * @param params
	 *            参数Map
	 */
	public static String remark(String key, Map<String, String> params) {
		String[] payKeys = new String[params.keySet().size()];
		payKeys = params.keySet().toArray(payKeys);
		Arrays.sort(payKeys, new KeySortor());
		StringBuffer cStringBuffer = new StringBuffer();
		for (String s : payKeys) {
			cStringBuffer.append(s);
			cStringBuffer.append(params.get(s));
		}
		cStringBuffer.append(key);
		return getMD5Str(cStringBuffer.toString(), "UTF-8");
	}

	/*
	 * MD5 加密
	 */
	public static String getMD5Str(String str, String enc) {
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(str.getBytes(enc));
		} catch (NoSuchAlgorithmException e) {
			System.exit(-1);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		byte[] byteArray = messageDigest.digest();
		StringBuffer md5StrBuff = new StringBuffer();
		for (int i = 0; i < byteArray.length; i++) {
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
				md5StrBuff.append("0").append(
						Integer.toHexString(0xFF & byteArray[i]));
			else
				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
		}
		return md5StrBuff.toString();
	}
}
