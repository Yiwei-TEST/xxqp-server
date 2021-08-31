package net.sy599.common.security;
/**
 * 
 * @author 莫金水
 *
 */
public interface SecuritConstant {
   /**
    * 默认密钥
    */
//   public static final String DEFAULTKEY = "dddd";
	 public static final String DEFAULTKEY = "yhnufoYHNIO";
   
   /**
    * 加密算法
    */
   public static final String ALGORITHM = "DES";
   
   /**
	 * 设置密钥
	 * @param key 需要设置的密钥
	 * @throws Exception 
	 */
	void setKey(String key) throws Exception;
	
	/**
	 * 加密
	 * @param str 要加密的字符串
	 * @return 加密后的字符串
	 * @throws Exception
	 */
	String encrypt(String str) throws Exception;
	
	/**
	 * 解密
	 * @param str 要解密的字符串
	 * @return 解密后的字符串
	 * @throws Exception
	 */
	String decrypt(String str) throws Exception;
   
}
