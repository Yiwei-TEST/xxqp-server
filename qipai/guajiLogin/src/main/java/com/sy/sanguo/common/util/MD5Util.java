package com.sy.sanguo.common.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
	/**
	 * 默认的密码字符串组合，用来将字节转换成 16 进制表示的字符,apache校验下载的文件的正确性用的就是默认的这个组合
	 */
	protected static char hexDigits[] = { '0', '1', '4', '7', '2', '5', '8', '3', '6', '9', 'b', 'c', 'a', 'd', 'e', 'f' };
	// protected static MessageDigest messagedigest = null;
	private final static String[] hexDigit1s = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

	/**
	 * 转换字节数组为16进制字串
	 * 
	 * @param b
	 *            字节数组
	 * @return 16进制字串
	 */
	public static String byteArrayToHexString(byte[] b) {
		StringBuilder resultSb = new StringBuilder();
		for (byte aB : b) {
			resultSb.append(byteToHexString(aB));
		}
		return resultSb.toString();
	}

	/**
	 * 转换byte到16进制
	 * 
	 * @param b
	 *            要转换的byte
	 * @return 16进制格式
	 */
	private static String byteToHexString(byte b) {
		int n = b;
		if (n < 0) {
			n = 256 + n;
		}
		int d1 = n / 16;
		int d2 = n % 16;
		return hexDigit1s[d1] + hexDigit1s[d2];
	}

	/**
	 * MD5编码
	 * 
	 * @param origin
	 *            原始字符串
	 * @return 经过MD5加密之后的结果
	 */
	public static String MD5Encode(String origin) {
		String resultString = null;
		try {
			resultString = origin;
			MessageDigest md = MessageDigest.getInstance("MD5");
//			resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
			resultString = byte2HexStr(md.digest(resultString.getBytes("utf-8")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultString;
	}

	private static ThreadLocal<MessageDigest> threadLocal = new ThreadLocal<MessageDigest>() {
		@Override
		protected MessageDigest initialValue() {
			MessageDigest messagedigest = null;
			try {
				messagedigest = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			return messagedigest;
		}
	};

	public static String getFileMD5String(java.io.File file) throws IOException {
		InputStream fis;
		fis = new FileInputStream(file);
		byte[] buffer = new byte[1024];
		int numRead = 0;
		MessageDigest messagedigest = threadLocal.get();
		while ((numRead = fis.read(buffer)) > 0) {
			messagedigest.update(buffer, 0, numRead);
		}
		fis.close();
		return bufferToHex(messagedigest.digest());
	}

	public static String getStringMD5(String str) {
		byte[] buffer;
		try {
			buffer = str.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
		MessageDigest messagedigest = threadLocal.get();
		messagedigest.update(buffer);

		return byte2HexStr(messagedigest.digest());

	}

	public static String getStringMD5(String str, String charset) {
		byte[] buffer;
		try {
			buffer = str.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
		MessageDigest messagedigest = threadLocal.get();
		messagedigest.update(buffer);

		return byte2HexStr(messagedigest.digest());

	}

	public static String md5Digest(String src) throws Exception {
		// 定义数字签名方法, 可用：MD5, SHA-1
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] b = md.digest(src.getBytes("utf-8"));

		return byte2HexStr(b);
	}

	private static String byte2HexStr(byte[] b) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < b.length; i++) {
			String s = Integer.toHexString(b[i] & 0xFF);
			if (s.length() == 1) {
				sb.append("0");
			}

			sb.append(s.toLowerCase());
		}

		return sb.toString();
	}

	public static String bufferToHex(byte bytes[]) {
		return bufferToHex(bytes, 0, bytes.length);
	}

	private static String bufferToHex(byte bytes[], int m, int n) {
		StringBuffer stringbuffer = new StringBuffer(2 * n);
		int k = m + n;
		for (int l = m; l < k; l++) {
			appendHexPair(bytes[l], stringbuffer);
		}
		return stringbuffer.toString();
	}

	private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
		char c0 = hexDigits[(bt & 0xf0) >> 4];// 取字节中高 4 位的数字转换
		// 为逻辑右移，将符号位一起右移,此处未发现两种符号有何不同
		char c1 = hexDigits[bt & 0xf];// 取字节中低 4 位的数字转换
		stringbuffer.append(c0);
		stringbuffer.append(c1);
	}

	public static String SHA1(String decript) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte messageDigest[] = digest.digest(decript.getBytes("utf-8"));
			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			// 字节数组转换为 十六进制 数
			for (int i = 0; i < messageDigest.length; i++) {
				String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
				if (shaHex.length() == 1) {
					hexString.append(0);
				}
				hexString.append(shaHex);
			}
			return hexString.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static void main(String[] args) {
		// e87f25a96baa80d6a66828fc56d2b4a49f74d60f
		String sha1 = "client_id=8716935&notify_url=http://192.168.1.160:8080/guajiLogin/pay!webwayx.guajilogin&out_trade_no=9456b9d9c83442e615bce9&subject=测试商品&time=1423808107&total_fee=1&0a075225be8bd76edb43771cd210daa7";
		System.out.println(SHA1(sha1));
		
		System.out.println(MD5Util.MD5Encode("WeiFuTong.class"));

        String pw = "a12345";
        System.out.println("pw|"+pw+"|"+getStringMD5(pw + "sanguo_shangyou_2013"));
	}
}