package net.sy599.common.security;

import java.security.Key;
import java.security.Security;

import javax.crypto.Cipher;

import com.sun.crypto.provider.SunJCE;



public class SecuritConstantImpl implements SecuritConstant {
    
	/**
	 * 默认密钥
	 */
	private  String defaultKey = null;
	public String getDefaultKey() {
		return defaultKey;
	}

	public void setDefaultKey(String defaultKey) {
		this.defaultKey = defaultKey;
	}

	public String getAlgorithm() {
		return Algorithm;
	}

	public void setAlgorithm(String algorithm) {
		Algorithm = algorithm;
	}

	/**
	 * 加密算法
	 */
	private  String Algorithm = null;
	/**
	 * 加密对象
	 */
	private Cipher encryptCipher = null;
	/**
	 * 解密对象
	 */
	private Cipher decryptCipher = null;
	
	/**
	 * 构造方法,从DesConf.properties文件中获得默认密钥和加密算法
	 * @throws Exception
	 */
	public SecuritConstantImpl() throws Exception{
		
		setAlgorithm(SecuritConstant.ALGORITHM);
		setDefaultKey(SecuritConstant.DEFAULTKEY);
		setKey(getDefaultKey());
	}
	
	/**
	 * 解密字节数组
	 * @param arrB 要解密的字节数组
	 * @return 解密后的字节数组
	 * @throws Exception
	 */
	private byte[] decrypt(byte[] arrB) throws Exception {
		return decryptCipher.doFinal(arrB);
	}
	
	/**
	 * 解密字符串
	 */
	public String decrypt(String str) throws Exception {
		return new String(decrypt(DataConvert.HexStr2byteArr(str)));
	}
	
	/**
	 * 加密字节数组
	 * @param arrB 要加密的字节数组
	 * @return 加密后的字节数组
	 * @throws Exception
	 */
	private byte[] encrypt(byte[] arrB) throws Exception {
		return encryptCipher.doFinal(arrB);
	 }
	
	/**
	 * 加密字符串
	 */

	public String encrypt(String str) throws Exception {
		return DataConvert.byteArr2HexStr(encrypt(str.getBytes()));
	}
    
	
	/**
	 * 设置密钥
	 */
	public void setKey(String key) throws Exception {
		Security.addProvider(new SunJCE());
		//获得密钥
		Key generateKey = new KeyTools().getKey(key.getBytes());
		//初始化加密对象		
		encryptCipher = Cipher.getInstance(getAlgorithm());
		encryptCipher.init(Cipher.ENCRYPT_MODE,generateKey);
		//初始化解密对象
		decryptCipher = Cipher.getInstance(getAlgorithm());
		decryptCipher.init(Cipher.DECRYPT_MODE, generateKey);
	}

}
