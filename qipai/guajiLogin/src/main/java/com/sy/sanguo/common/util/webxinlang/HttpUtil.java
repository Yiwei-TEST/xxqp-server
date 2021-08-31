package com.sy.sanguo.common.util.webxinlang;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
@SuppressWarnings("deprecation")
public class HttpUtil {

    private static final String UTF_8 = HTTP.UTF_8;
    
    public static String post(String url , Map<String , String> params) throws Exception{
        DefaultHttpClient client = HttpFactory.createHttpClient();
        HttpPost post = new HttpPost(url);
        if(params != null ){
            List<BasicNameValuePair> lparams = new LinkedList<BasicNameValuePair>();
            for (ConcurrentHashMap.Entry<String, String> entry : params.entrySet()) {
                if (entry.getValue() != null) {
                    lparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }else{
                    lparams.add(new BasicNameValuePair(entry.getKey(), ""));
                }
            }
            HttpEntity entiry = new UrlEncodedFormEntity(lparams, UTF_8);
            post.setEntity(entiry);
        }
        try {
            HttpResponse resonse = client.execute(post);
            return entityToString(resonse);
        } catch (Exception exception) {
            throw exception;
        } finally {
            post.abort();
            client.getConnectionManager().shutdown();
        }
    }

    public static String get(String url) throws Exception{
        DefaultHttpClient client = HttpFactory.createHttpClient();
        
        HttpGet get = new HttpGet(url);
        try {
            HttpResponse resonse = client.execute(get);
            return entityToString(resonse);
        } catch (Exception exception) {
            throw exception;
        } finally {
            get.abort();
            client.getConnectionManager().shutdown();
        }
    }

    public static String entityToString(HttpResponse resonse) throws Exception{
        HttpEntity entity = resonse.getEntity();
        if (entity != null) {
            String msg = null;
            try {
                msg = EntityUtils.toString(entity, UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int code = resonse.getStatusLine().getStatusCode();
            if (code == 200) {
                return msg;
            } else {
                String errerMsg = (msg == null ? null : msg);
                throw new Exception("http code:" + code +",error:"+ errerMsg);
            }
        }
        throw new Exception("http entity is null");
    }

    public static byte[] entityTobyte(HttpResponse resonse) throws Exception {
        HttpEntity entity = resonse.getEntity();
        if (entity != null) {
            byte[] buffer = null;
            try {
                buffer = EntityUtils.toByteArray(entity);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int code = resonse.getStatusLine().getStatusCode();
            if (code == 200) {
                return buffer;
            } else {
                String errerMsg = (buffer == null ? null : new String(buffer, UTF_8));
                throw new Exception("http code:" + code +",error:"+ errerMsg);
            }
        }
        throw new Exception("http entity is null");
    }
}