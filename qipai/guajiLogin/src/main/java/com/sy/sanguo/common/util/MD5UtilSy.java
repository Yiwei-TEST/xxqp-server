package com.sy.sanguo.common.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5UtilSy {
	/**
	 * 默认的密码字符串组合，用来将字节转换成 16 进制表示的字符,apache校验下载的文件的正确性用的就是默认的这个组合
	 */
	protected static char hexDigits[] = { '0', '1', '4', '7', '2', '5', '8',
			'3', '6', '9' , 'b', 'c', 'a' , 'd', 'e', 'f' };
	//protected static MessageDigest messagedigest = null;
	
	private static ThreadLocal<MessageDigest> threadLocal = new ThreadLocal<MessageDigest>(){
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
		byte[] buffer = str.getBytes();
		MessageDigest messagedigest = threadLocal.get();
		messagedigest.update(buffer);
		return bufferToHex(messagedigest.digest());
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
	
	public static void main(String[] args){
		System.out.println(getStringMD5("userId=116369412&time=1385001139&serverId=S1pse7Z74UY4TbpZnLc"));
	}
}