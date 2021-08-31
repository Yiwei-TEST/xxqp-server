package net.sy599.common.security;

/**
 * @author kalman
 */
public final class DataConvert {

	/**
	 * 将字符数组转换为16进制的字符串
	 * 
	 * @param b 要转换的字符数组
	 * @return 16进制的字符串
	 * @throws Exception
	 */
	public static String byteArr2HexStr(byte[] b) throws Exception {
		//获得要转换的字符数组的长度
		int bLength = b.length;
		// 1个byte是2字符，所以字符串的长度是数组长度的两倍
		StringBuffer sb = new StringBuffer(bLength * 2);
		for (int i = 0; i < bLength; i++) {
			int intTmp = b[i];
			// 把负数转换为正数
			while (intTmp < 0) {
				intTmp = intTmp + 256;
			}
			// 小于0F的数需要在前面补0
			if (intTmp < 16) {
				sb.append("0");
			}
			sb.append(Integer.toString(intTmp, 16));
		}
		return sb.toString();
	}
	
	
	/**
	 * 将16进制字符串转换成字节数组
	 * @param str 要转换的16进制字符串
	 * @return 字节数组
	 * @throws Exception
	 */
	public static byte[] HexStr2byteArr(String str) throws Exception {
		byte[] arrB = str.getBytes();
		int iLen = arrB.length;
		// 1个byte是2字符，所以字符串的长度是数组长度的两倍
		byte[] arrOut = new byte[iLen / 2];
		for (int i = 0; i < iLen; i = i + 2) {
			String strTmp = new String(arrB, i, 2);
			arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 16);
		}
		return arrOut;
	}
}
