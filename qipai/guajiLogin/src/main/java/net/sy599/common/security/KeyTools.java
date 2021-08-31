package net.sy599.common.security;

import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

/**
 * @author kalman
 */
public final class KeyTools {

	/**
	 * 从指定字符串生成密钥,密钥所需的字节数组长度为8位,不足8位时后面补0,超出8位只取前8位
	 * @param array 指定字符串对应的字节数组
	 * @return 生成的密钥
	 * @throws Exception
	 */
	Key getKey(byte[] array) throws Exception{
		//要返回的密钥对象
		Key key = null;
		//一个默认值为0的8位数组
		byte[] b = new byte[8];
		//不足8位时后面默认为0,超出8位只取前8位
		for(int i=0;i<b.length && i<array.length;i++){
			b[i] = array[i];
		}
		//生成密钥
		key = new SecretKeySpec(b,"DES");
		return key;
	}
}
