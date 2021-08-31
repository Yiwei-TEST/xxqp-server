package com.sy.sanguo.game.utils;

import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES工具类
 */
public class AESUtil {
    private static final String charset = "utf-8";
    // 偏移量
    private static final int offset = 16;
    private static final String transformation = "AES/CBC/PKCS5Padding";
    private static final String algorithm = "AES";

    /**
     * 加密
     *
     * @param content 需要加密的内容
     * @param key     密钥
     * @return
     */
    public static String encrypt(String content, String key) {
        try {
            if (StringUtils.isBlank(content) || StringUtils.isBlank(key)) {
                LogUtil.e("AESUtil|encrypt|error|" + content + "|" + key);
                return null;
            }
            if (key.length() != offset) {
                LogUtil.e("AESUtil|encrypt|error|" + content + "|" + key);
                return null;
            }
            SecretKeySpec skey = new SecretKeySpec(key.getBytes(), algorithm);
            IvParameterSpec iv = new IvParameterSpec(key.getBytes(), 0, offset);
            Cipher cipher = Cipher.getInstance(transformation);
            byte[] byteContent = content.getBytes(charset);
            cipher.init(Cipher.ENCRYPT_MODE, skey, iv);
            byte[] result = cipher.doFinal(byteContent);
            return new Base64().encodeToString(result);
        } catch (Exception e) {
            LogUtil.e("AESUtil|encrypt|error|" + content + "|" + key, e);
        }
        return null;
    }

    /**
     * AES（256）解密
     *
     * @param content 待解密内容
     * @param key     密钥
     * @return 解密之后
     * @throws Exception
     */
    public static String decrypt(String content, String key) {
        try {
            if (StringUtils.isBlank(content) || StringUtils.isBlank(key)) {
                LogUtil.e("AESUtil|decrypt|error|" + content + "|" + key);
                return null;
            }
            if (key.length() != offset) {
                LogUtil.e("AESUtil|decrypt|error|" + content + "|" + key);
                return null;
            }
            SecretKeySpec skey = new SecretKeySpec(key.getBytes(), algorithm);
            IvParameterSpec iv = new IvParameterSpec(key.getBytes(), 0, offset);
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.DECRYPT_MODE, skey, iv);
            byte[] result = cipher.doFinal(new Base64().decode(content));
            return new String(result);
        } catch (Exception e) {
            LogUtil.e("AESUtil|decrypt|error|" + content + "|" + key, e);
        }
        return null;
    }

}
