package com.sy.sanguo.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * 工具类。
 */
@SuppressWarnings("deprecation")
public class UCUtil {
	
	/** 
     * 执行一个HTTP POST请求，返回请求响应的内容
     * @param url        请求的URL地址 
     * @param params 请求的查询参数,可以为null 
     * @return 返回请求响应的内容 
     */ 
     public static String doPost(String url, String body) { 
    	 StringBuffer stringBuffer = new StringBuffer();
			HttpEntity entity = null;
			BufferedReader in = null;
			HttpResponse response = null;
			try {
				DefaultHttpClient httpclient = new DefaultHttpClient();
				HttpParams params = httpclient.getParams();
				HttpConnectionParams.setConnectionTimeout(params, 20000);
				HttpConnectionParams.setSoTimeout(params, 20000);
				HttpPost httppost = new HttpPost(url);
				httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");

				httppost.setEntity(new ByteArrayEntity(body.getBytes("UTF-8")));
				response = httpclient.execute(httppost);
				entity = response.getEntity();
				in = new BufferedReader(new InputStreamReader(entity.getContent()));
				String ln;
				while ((ln = in.readLine()) != null) {
					stringBuffer.append(ln);
					stringBuffer.append("\r\n");
				}
				httpclient.getConnectionManager().shutdown();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (IllegalStateException e2) {
				e2.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (null != in) {
					try {
						in.close();
						in = null;
					} catch (IOException e3) {
						e3.printStackTrace();
					}
				}
			}
			return stringBuffer.toString();
    } 

	/** 
     * MD5 加密 
     */  
    public static String getMD5Str(String str) {  
        MessageDigest messageDigest = null;  
  
        try {  
            messageDigest = MessageDigest.getInstance("MD5");  
  
            messageDigest.reset();  
  
            messageDigest.update(str.getBytes("UTF-8"));  
        } catch (NoSuchAlgorithmException e) {  
            System.out.println("NoSuchAlgorithmException caught!");  
            System.exit(-1);  
        } catch (UnsupportedEncodingException e) {  
            e.printStackTrace();  
        }  
  
        byte[] byteArray = messageDigest.digest();  
  
        StringBuffer md5StrBuff = new StringBuffer();  
  
        for (int i = 0; i < byteArray.length; i++) {              
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)  
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));  
            else  
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));  
        }  
  
        return md5StrBuff.toString();  
    }  
    /**
     * 读取配置信息。
     * @param key Key值
     * @return
     * @throws Exception
     */
    public static String getConfig(String key) throws Exception{
    	try{
	    	InputStream in = UCUtil.class.getResourceAsStream("/conf.Properties");
	        Properties p = new Properties();
	        p.load(in);
	        return p.get(key).toString().trim();
    	}catch(Exception e){
    		System.out.println("配置文件不存在"+e.toString());
    	}
    	return "";
    }
    
}
