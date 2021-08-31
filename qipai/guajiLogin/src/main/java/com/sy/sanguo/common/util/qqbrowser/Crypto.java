package com.sy.sanguo.common.util.qqbrowser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * 应用签名及请求响应数据签名类
 *
 */
public class Crypto {

    private static final String[] ignoredSigName = {"appsig", "reqsig", "paysig"};
    private static final String encoding = "UTF-8";

    /**
     * 生成请求或响应数据的签名
     *
     * @param uri 请求的Url
     * @param method 请求的方式，如GET,POST等
     * @param queryMap 查询参数对象
     * @param appKey 应用密钥
     * @return 返回请求或响应签名
     */
    public static String GetDataSig(String uri, String method, TreeMap<String, String> queryMap, String appKey) {
        String dataSig = "";
        try {
            if (uri.indexOf("http://") == -1 && uri.indexOf("https://") == -1) {
                if (uri.indexOf("/") < 0) {
                    uri += "/";
                }
                uri = "http://" + uri.substring(uri.lastIndexOf("/"));
                //System.out.println(uri);
            }

            URL url = new URL(uri);
            String path = url.getPath();
            path = UrlEncode(path);

            for (String sig : Crypto.ignoredSigName) {
                if (queryMap.containsKey(sig)) {
                    queryMap.remove(sig);
                    //System.out.println(sig);
                }
            }

            Iterator it = queryMap.entrySet().iterator();
            StringBuilder paraString = new StringBuilder();
            Map.Entry entry;
            while (it.hasNext()) {
                entry = (Map.Entry<String, String>) it.next();
                paraString.append("&").append(entry.getKey()).append("=").append(entry.getValue());
            }
            paraString = paraString.deleteCharAt(0);
            String paras2 = paraString.toString();
            paras2 = UrlEncode(paras2);

            String srcUrl = method + "&" + path + "&" + paras2;
            String srcSigKey = appKey + "&";
            //System.out.println(srcUrl);
            dataSig = Crypto.GetSig(srcUrl, srcSigKey);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return dataSig;
    }

    /**
     * 获取应用签名
     *
     * @param appId 应用Id
     * @param time 时间戳
     * @param nonce 随机串
     * @param appKey 应用密钥
     * @param dataKey 数据加密密钥
     * @return 返回应用签名
     */
    public static String GetAppSig(String appId, String time, String nonce, String appKey, String dataKey) {
        String src = appId + "_" + time + "_" + nonce;
        String cipher = Crypto.GetCipherData(src, dataKey);
        appKey += "&";
        String appsig = Crypto.GetSig(cipher, appKey);
        return appsig;
    }

    /**
     * 获取签名
     *
     * @param rawData 需签名的数据
     * @param appKey 密钥
     * @return 返回数据的签名
     */
    public static String GetSig(String rawData, String appKey) {
        String sig = "";
        try {
            byte[] rawBytes = rawData.getBytes(encoding);

            String macName = "HmacSHA1";
            byte[] keyBytes = appKey.getBytes(encoding);

            SecretKey secretKey = new SecretKeySpec(keyBytes, macName);
            Mac mac = Mac.getInstance(macName);
            mac.init(secretKey);
            byte[] sigBytes = mac.doFinal(rawBytes);

            sig = new BASE64Encoder().encode(sigBytes);
            //System.out.println(sig);
            sig = UrlEncode(sig);

        } catch (UnsupportedEncodingException e) {

        } catch (NoSuchAlgorithmException e) {

        } catch (InvalidKeyException e) {
 
        } catch (IllegalStateException e) {

        }

        return sig;
    }

    /**
     * 获取加密密文
     *
     * @param rawData 需加密的明文数据
     * @param dataKey 密钥
     * @return 返回加密密文的Base64及UrlEncode后的串
     */
    public static String GetCipherData(String rawData, String dataKey) {
        String cipherData = "";
        try {
            //在末尾补结束符\0
            int blockSize = 16;
            int padding = blockSize - rawData.length() % blockSize;
            StringBuilder sb = new StringBuilder();
            while (padding-- > 0) {
                sb.append('\0');
            }
            rawData += sb.toString();

            //创建密钥
            byte[] keyBytes = dataKey.getBytes(encoding);
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

            //加密
            byte[] rawBytes = rawData.getBytes(encoding);
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key);//加密模式
            byte[] cipherBytes = cipher.doFinal(rawBytes); //完成加密操作

            cipherData = new BASE64Encoder().encode(cipherBytes);//Base64
            cipherData = UrlEncode(cipherData);

        } catch (NoSuchAlgorithmException e) {
            //e.printStackTrace();
        } catch (NoSuchPaddingException e) {

        } catch (InvalidKeyException e) {

        } catch (UnsupportedEncodingException e) {

        } catch (IllegalBlockSizeException e) {

        } catch (BadPaddingException e) {

        }
        return cipherData;
    }

    /**
     * 获取明文
     *
     * @param cipherData 需解密的明文
     * @param dataKey 密钥
     * @return 返回解密后的明文数据（utf8格式）
     */
    public static String GetPlainData(String cipherData, String dataKey) {
        String plainData = "";
        try {
            //urldecode及base64解密还原加密数据
            cipherData = UrlDecode(cipherData);
            byte[] cipherBytes = new BASE64Decoder().decodeBuffer(cipherData);//Base64 

            //创建密钥
            byte[] keyBytes = dataKey.getBytes(encoding);
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

            //解密
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");//PKCS5Padding
            cipher.init(Cipher.DECRYPT_MODE, key);//解密模式 
            byte[] plainBytes = cipher.doFinal(cipherBytes);
            plainData = new String(plainBytes, encoding);

            //去掉结束符
            int index = plainData.indexOf('\0');
            if (index > -1) {
                plainData = plainData.substring(0, index);
            }

        } catch (NoSuchAlgorithmException e) {
            //e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            //e.printStackTrace();
        } catch (InvalidKeyException e) {
            //e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            //e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            //e.printStackTrace();
        } catch (BadPaddingException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return plainData;
    }

    public static String UrlEncode(String rawData) {
        String result = rawData;

        try {
            result = URLEncoder.encode(rawData, "utf-8").replaceAll("\\*", "%2A");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String UrlDecode(String encodedData) {
        String result = encodedData;
        try {
            result = URLDecoder.decode(encodedData, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return result;
    }
}
