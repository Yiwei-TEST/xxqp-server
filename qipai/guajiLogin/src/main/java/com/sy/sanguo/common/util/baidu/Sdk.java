/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sy.sanguo.common.util.baidu;

/**
 *
 * @author Administrator
 */
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;


public class Sdk {
	
	//开发者应用APPID
	private String appid = "";  
        public String getAppID(){
            return appid;
        }
	//开发者应用APPKEY
	private String appkey = "";
        public String getAppKey(){
            return appkey;
        }        
       
	/**
	 * 对字符串进行MD5并返回结果
	 * @param sourceStr
	 * @return
	 */
	public static String md5(String sourceStr){
		String signStr = "";
		try {
			byte[] bytes = sourceStr.getBytes("utf-8");
			MessageDigest md5 = MessageDigest.getInstance("MD5"); md5.update(bytes);
			byte[] md5Byte = md5.digest();
			if(md5Byte != null){
			signStr = HexBin.encode(md5Byte); }
			} catch (NoSuchAlgorithmException e) { e.printStackTrace();
			} catch (UnsupportedEncodingException e) { e.printStackTrace();
			}
			return signStr.toLowerCase();
	}
	
        /**
        * 向指定 URL 发送POST方法的请求
        * @param goUrl
        *            请求地址
        * @param param
        *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式
        * @return 所代表远程资源的响应结果
        */
        public static String sendPost(String goUrl,Map<String, String> params) {
            PrintWriter out = null;
            BufferedReader in = null;
            String result = "";
    		StringBuffer s = new StringBuffer();
    		for (String k : params.keySet()) {
    			s.append("&").append(k).append("=").append(params.get(k));
    		}
    		s.deleteCharAt(0);
    		String	param=s.toString();
            HttpURLConnection conn=null;
            try {
                URL realUrl = new URL(goUrl);
                // 打开和URL之间的连接
                conn =(HttpURLConnection) realUrl.openConnection();
                // 设置通用的请求属性
                conn.setRequestProperty("accept", "*/*");
                conn.setRequestProperty("connection", "Keep-Alive");
                conn.setRequestProperty("user-agent",
                        "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
                // 发送POST请求必须设置如下两行
                conn.setDoOutput(true);
                conn.setDoInput(true);
                // 获取URLConnection对象对应的输出流
                out = new PrintWriter(conn.getOutputStream());
                // 发送请求参数
                out.print(param);
                // flush输出流的缓冲
                out.flush();
                // 定义BufferedReader输入流来读取URL的响应
                in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(),"utf-8"));      
                String line;
                while ((line = in.readLine()) != null) {
                    result += line;
                }
            } catch (Exception e) {
                System.out.println("发送 POST 请求出现异常！"+e);
                e.printStackTrace();
            }
            //使用finally块来关闭输出流、输入流
            finally{
                close(conn);
                try{
                    if(out!=null){
                        out.close();
                    }
                    if(in!=null){
                        in.close();
                    }
                }
                catch(IOException ex){
                    ex.printStackTrace();
                }
            }
            return result;
        }

    static void close(HttpURLConnection conn) {
        if (conn != null) {
            conn.disconnect();
            conn = null;
        }
    }

    static void close(HttpsURLConnection conn) {
        if (conn != null) {
            conn.disconnect();
            conn = null;
        }
    }
}
