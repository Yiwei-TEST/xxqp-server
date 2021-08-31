package com.sy.sanguo.game.redpack;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HttpUtil1 {

    public static String wechatPost(String url,String params, InputStream keyStream ) throws Exception{
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try {
            keyStore.load(keyStream, WeixinRedbagConfig.getMchId().toCharArray());
        } finally {
            keyStream.close();
        }
        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom()
                .loadKeyMaterial(keyStore, WeixinRedbagConfig.getMchId().toCharArray())
                .build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[] { "TLSv1" },
                null,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build();
        try {
            String resp = "";
            HttpPost httpPost = new HttpPost(url);
            StringEntity ent = new StringEntity(params,"utf-8");
            ent.setContentType("application/x-www-form-urlencoded");
            httpPost.setEntity(ent);
            CloseableHttpResponse response = httpclient.execute(httpPost);
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    System.out.println("Response content length: " + entity.getContentLength());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity.getContent()));
                    String text;
                    while ((text = bufferedReader.readLine()) != null) {
                        resp += text;
                    }
                }
                EntityUtils.consume(entity);
                return resp;
            }catch(Exception e){
            }
            finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
        return null;
    }
    public static String getHttpsUrlHost(String url) {
        Pattern p = Pattern.compile("^https://[^/]*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(url);
        if (matcher.find()) {
            String baseUri = matcher.group();
            return baseUri;
        }
        return null;
    }
    public static void main(String[] args) {
        String url = "https://api.mch.weixin.qq.com/mmpaymkttransfers/sendredpack";
        System.out.println(getHttpsUrlHost(url));
    }
}