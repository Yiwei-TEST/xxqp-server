package com.sy.sanguo.common.util.request;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;

/**
 * md5加密
 * 
 * @author Administrator
 *
 */
public final class MD5Util {

	private final static char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private static final String UTF_8 = "UTF-8";

	public final static String getFileMD5String(File file) {

//		FileInputStream in = null;
//		FileChannel ch = null;
//		try {
//			in = new FileInputStream(file);
//			ch = in.getChannel();
//			MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY,
//					0, file.length());
//			MessageDigest messagedigest = MessageDigest.getInstance("MD5");
//			messagedigest.update(byteBuffer);
//			return bufferToHex(messagedigest.digest());
//		} catch (Exception e) {
//			return null;
//		} finally {
//			try {
//				in.close();
//				ch.close();
//			} catch (Exception e) {
//			} finally {
//				in = null;
//				ch = null;
//			}
//		}
		
		try {
			return getMD5String(FileUtils.readFileToByteArray(file));
		} catch (IOException e) {
		}
		return null;
	}

	public final static String getMD5String(String s) {
		try {
			return getMD5String(s.getBytes(UTF_8));
		} catch (Exception e) {
			return getMD5String(s.getBytes());
		}
	}

	public final static String getMD5String(Object s) {
		return getMD5String(String.valueOf(s));
	}

	public final static String getMD5String(byte[] bytes) {
		try {
			MessageDigest messagedigest = MessageDigest.getInstance("MD5");
			messagedigest.update(bytes);
			return bufferToHex(messagedigest.digest());
		} catch (Exception e) {
			return null;
		}
	}

	private final static String bufferToHex(byte bytes[]) {
		return new String(encodeHex(bytes));
	}

	// >>>无符号，>>带符号

	private final static char[] encodeHex(byte[] data) {
		int l = data.length;
		char[] out = new char[l << 1];
		// two characters form the hex value.
		for (int i = 0, j = 0; i < l; i++) {
			out[j++] = HEX_DIGITS[(0xF0 & data[i]) >> 4];
			out[j++] = HEX_DIGITS[0x0F & data[i]];
		}
		return out;
	}
}
