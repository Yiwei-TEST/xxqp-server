package com.sy.sanguo.game.utils;

import org.apache.commons.lang3.StringUtils;

public class HttpDataUtil {

    private static String http_data_aes_key = "";
    private static String http_data_aes_id = "";
    private static String http_data_aes_switch = "0";

    public static String getHttpDataAESKey() {
        return http_data_aes_key;
    }

    public static void setHttpDataAESKey(String aesKey) {
        if (StringUtils.isNotBlank(aesKey) && aesKey.length() == 16) {
            http_data_aes_key = aesKey;
        }
    }

    public static String getHttpDataAESId() {
        return http_data_aes_id;
    }

    public static void setHttpDataAESId(String dataId) {
        if (StringUtils.isNotBlank(dataId) && dataId.length() == 16) {
            http_data_aes_id = dataId;
        }
    }

    public static void setHttpDataAESSwitch(String funcSwitch) {
        http_data_aes_switch = funcSwitch;
    }

    public static boolean isSwitchOn(){
        return "1".equals(http_data_aes_switch);
    }


    /**
     * AES加密手机号
     *
     * @param data
     * @return
     */
    public static String aesEncryptHttpData(String data) {
        return AESUtil.encrypt(data, http_data_aes_key);
    }


    /**
     * AES 解密手机号
     *
     * @param msg
     * @return
     */
    public static String aesDecryptHttpData(String msg) {
        return AESUtil.decrypt(msg, http_data_aes_key);
    }


    public static void main(String[] args) throws Exception {

        test();
//        test1();
    }

    public static void test() throws Exception {
        String s = "很不错的";
        String encrypt = aesEncryptHttpData(s);
        System.out.println(encrypt);
        encrypt = "/xZH2LvPRdc0245RA2uThg==";
        String decrypt = aesDecryptHttpData(encrypt);
        System.out.println(decrypt);

        decrypt = aesDecryptHttpData(encrypt);
        System.out.println(decrypt);
    }

    public static void test1() {
        String msg = "clPH+5o8S4MGaiqPGgXMFeB67w4as0+XyGrO5qRsDTFr/KnXn5I8ptg9MlJBvb0oKGdiuikaiA4us0xjK3wA5w==";
        int count = 10000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            String encrypt = aesEncryptHttpData(msg);
//            System.out.println("AES|encrypt|" + msg + "|" + encrypt + "|");

            String decrypt = aesDecryptHttpData(encrypt);
//            System.out.println("AES|decrypt|" + encrypt + "|" + decrypt + "|");
        }
        System.out.println("--------count=" + count + ",use=" + (System.currentTimeMillis() - start) + "ms");
    }


}
