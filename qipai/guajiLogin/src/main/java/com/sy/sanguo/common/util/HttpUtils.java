package com.sy.sanguo.common.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.io.IOUtils;

import com.sy.sanguo.common.util.https.MySecureProtocolSocketFactory;

public class HttpUtils {
	
	// 编码方式
    private static final String CONTENT_CHARSET = "UTF-8";

    // 连接超时时间
    private static final int CONNECTION_TIMEOUT = 3000;

    // 读数据超时时间
    private static final int READ_DATA_TIMEOUT = 3000;
	
    public static void main(String[] args){
    	try{
	    	HashMap<String,String> params = new HashMap<String,String>();
	    	String url = "http:///sanguologinmbsll/user!login.xsgmobile?t=765723731fe3ae13cccd57f0f0db8375e2e662c1c4bd82350&u=11123&p=m360";
//			String url = "http://localhost:8080/sanguoLogin/user!login.xsgmobile?t=765723731fe3ae13cccd57f0f0db8375e2e662c1c4bd82350&u=11123&p=m360";
//			for(int i=0;i<2;i++){
				String resp = HttpUtils.postRequest(url, params,null);
				System.out.println(resp);
//			}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    @SuppressWarnings("unchecked")
	public static String postRequest(String url,  HashMap<String, String> params, HashMap<String, String> cookies, String protocol) throws Exception {
    	if (protocol.equalsIgnoreCase("https"))
        {
            Protocol httpsProtocol = new Protocol("https",
                new MySecureProtocolSocketFactory(), 443);
            Protocol.registerProtocol("https", httpsProtocol);
        }
    	HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(url);

        // 设置请求参数
        if (params != null && !params.isEmpty())
        {
            NameValuePair[] data = new NameValuePair[params.size()];

            Iterator iter = params.entrySet().iterator(); 
            int i=0;
            while (iter.hasNext()) 
            { 
                Map.Entry entry = (Map.Entry) iter.next(); 
                data[i] = new NameValuePair((String)entry.getKey(), (String)entry.getValue());
                ++i;
            } 
			
            postMethod.setRequestBody(data);
        }

        // 设置cookie
        if (cookies !=null && !cookies.isEmpty())
        {
            Iterator iter = cookies.entrySet().iterator();
            StringBuilder buffer = new StringBuilder(128);
            while (iter.hasNext())
            {
                Map.Entry entry = (Map.Entry) iter.next(); 
                buffer.append((String)entry.getKey()).append("=").append((String)entry.getValue()).append("; ");
            }
            // 设置cookie策略
            postMethod.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
            // 设置cookie内容
            postMethod.setRequestHeader("Cookie", buffer.toString());
        }

        // 设置User-Agent
//        postMethod.setRequestHeader("User-Agent", "Java OpenApiV3 SDK Client");

        // 设置建立连接超时时间
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(CONNECTION_TIMEOUT);

        // 设置读数据超时时间
        httpClient.getHttpConnectionManager().getParams().setSoTimeout(READ_DATA_TIMEOUT);

        // 设置编码
        postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                CONTENT_CHARSET); 

        //使用系统提供的默认的恢复策略
        postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler());

        try 
        {
            try
            {
                int statusCode = httpClient.executeMethod(postMethod);
    
                if (statusCode != HttpStatus.SC_OK) 
                {
                    throw new Exception("network error statusCode::"+statusCode+" url::"+url);
                }
    
                //读取内容 
                byte[] responseBody = postMethod.getResponseBody();
    
                return new String(responseBody, CONTENT_CHARSET);
            }
            finally
            {
                //释放链接
                postMethod.releaseConnection();
            }
        } 
        catch (HttpException e) 
        {
            //发生致命的异常，可能是协议不对或者返回的内容有问题
            throw e;
        } 
        catch (IOException e) 
        {
            //发生网络异常
            throw e;
        } 
    }
    
	public static String postRequest(String url,  HashMap<String, String> params, HashMap<String, String> cookies) throws Exception {
        return postRequest(url, params, cookies, "http");
    }
	
	
	public static String getRequest(String url,  Map<String, String> params,HashMap<String, String> cookies) throws Exception {
		HttpClient httpClient = new HttpClient();
        

        // 设置请求参数
        if (params != null && !params.isEmpty())
        {
        	StringBuffer param = new StringBuffer();
			int i = 0;
			for (String key : params.keySet()) {
				if (i == 0)
					param.append("?");
				else
					param.append("&");
				param.append(key).append("=").append(params.get(key));
				i++;
			}
			url += param;
        }
        System.out.println("url:"+url);
        GetMethod getMethod = new GetMethod(url);

        // 设置cookie
        if (cookies !=null && !cookies.isEmpty())
        {
            Iterator iter = cookies.entrySet().iterator();
            StringBuilder buffer = new StringBuilder();
            while (iter.hasNext())
            {
                Map.Entry entry = (Map.Entry) iter.next(); 
                buffer.append((String)entry.getKey()).append("=").append((String)entry.getValue()).append("; ");
            }
            // 设置cookie策略
            getMethod.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
            // 设置cookie内容
            getMethod.setRequestHeader("Cookie", buffer.toString());
        }
        
        // 设置User-Agent
//        postMethod.setRequestHeader("User-Agent", "Java OpenApiV3 SDK Client");

        // 设置建立连接超时时间
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(CONNECTION_TIMEOUT);

        // 设置读数据超时时间
        httpClient.getHttpConnectionManager().getParams().setSoTimeout(READ_DATA_TIMEOUT);

        // 设置编码
        getMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
                CONTENT_CHARSET); 

        //使用系统提供的默认的恢复策略
        getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler());

        try 
        {
            try
            {
                int statusCode = httpClient.executeMethod(getMethod);
    System.out.print(statusCode+".....");
                if (statusCode != HttpStatus.SC_OK) 
                {
                    throw new Exception("network error");
                }
    
                //读取内容 
                byte[] responseBody = getMethod.getResponseBody();
    
                return new String(responseBody, CONTENT_CHARSET);
            }
            finally
            {
                //释放链接
                getMethod.releaseConnection();
            }
        } 
        catch (HttpException e) 
        {
            //发生致命的异常，可能是协议不对或者返回的内容有问题
            throw e;
        } 
        catch (IOException e) 
        {
            //发生网络异常
            throw e;
        } 
	}
	
	public static String postJsonRequest(String json, String URL) {
		//System.out.println("发起的数据:" + json);
		byte[] xmlData = json.getBytes();
		InputStream instr = null;
		//java.io.ByteArrayOutputStream out = null;
        HttpURLConnection urlCon=null;
		try {
			URL url = new URL(URL);
            urlCon =(HttpURLConnection) url.openConnection();
			urlCon.setDoOutput(true);
			urlCon.setDoInput(true);
			urlCon.setUseCaches(false);
			urlCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			urlCon.setRequestProperty("Content-length", String.valueOf(xmlData.length));
			//System.out.println(String.valueOf(xmlData.length));
			DataOutputStream printout = new DataOutputStream(urlCon.getOutputStream());
			printout.write(xmlData);
			printout.flush();
			printout.close();
			instr = urlCon.getInputStream();
			byte[] bis = IOUtils.toByteArray(instr);
			String ResponseString = new String(bis, "UTF-8");
			/*if ((ResponseString == null) || ("".equals(ResponseString.trim()))) {
				System.out.println("返回空");
			}
			System.out.println("返回数据为:" + ResponseString);*/
			return ResponseString;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
		    if (urlCon!=null){
                urlCon.disconnect();
            }
			try {
				//out.close();
				instr.close();

			} catch (Exception ex) {
				return null;
			}
		}
	}
    
}
