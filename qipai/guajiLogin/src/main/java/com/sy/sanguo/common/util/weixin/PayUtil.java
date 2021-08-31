package com.sy.sanguo.common.util.weixin;

import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.util.Constants;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import com.sy.sanguo.game.service.SysInfManager;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import javax.net.ssl.SSLContext;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PayUtil {

    public static final String PAY_URL = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers";
    public static final String QUERY_URL = "https://api.mch.weixin.qq.com/mmpaymkttransfers/gettransferinfo";

    public static String post(String url,String postContent) {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            FileInputStream instream = new FileInputStream(new File(SysInfManager.baseDir + "/WEB-INF/config/certs/weixin/apiclient_cert.p12"));
            char[] pw = PropertiesCacheUtil.getValue("mchid",Constants.GAME_FILE).toCharArray();
            try {
                keyStore.load(instream, pw);
            } finally {
                instream.close();
            }

            // Trust own CA and all self-signed certs
            SSLContext sslcontext = SSLContexts.custom()
                    .loadKeyMaterial(keyStore, pw)
                    .build();
            // Allow TLSv1 protocol only
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    sslcontext,
                    new String[]{"TLSv1"},
                    null,
                    SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setSSLSocketFactory(sslsf)
                    .build();
            try {

                HttpPost http = new HttpPost(url);
                http.setEntity(EntityBuilder.create().setContentType(ContentType.create("application/x-www-form-urlencoded", Charset.forName("UTF-8"))).setContentEncoding("UTF-8").setText(postContent).build());

                CloseableHttpResponse response = httpclient.execute(http);
                try {
                    HttpEntity entity = response.getEntity();

                    StringBuilder resultBuilder = new StringBuilder();
                    if (entity != null) {
                        BufferedInputStream bis = new BufferedInputStream(entity.getContent(), 1024);

                        byte[] buf = new byte[1024];

                        int length;
                        while ((length = bis.read(buf)) != -1) {
                            resultBuilder.append(new String(buf, 0, length, "UTF-8"));
                        }

                        bis.close();
                    }
                    String result = resultBuilder.toString();

                    LogUtil.i("url:"+url+",content:"+postContent+",status:"+response.getStatusLine()+",length="+(entity == null ? 0 : entity.getContentLength())+",result="+result);
                    EntityUtils.consume(entity);

                    return result;
                } catch (Exception e) {
                    LogUtil.e("Exception:" + e.getMessage(), e);
                } finally {
                    response.close();
                }
            } catch (Exception e) {
                LogUtil.e("Exception:" + e.getMessage(), e);
            } finally {
                httpclient.close();
            }
        } catch (Exception e) {
            LogUtil.e("Exception:" + e.getMessage(), e);
        }

        return null;
    }

    /**
     * 转XMLmap
     * @author
     * @param xmlBytes
     * @param charset
     * @return
     * @throws Exception
     */
    public static Map<String, String> toMap(byte[] xmlBytes,String charset) throws Exception{
        SAXReader reader = new SAXReader(false);
        InputSource source = new InputSource(new ByteArrayInputStream(xmlBytes));
        source.setEncoding(charset);
        Document doc = reader.read(source);
        Map<String, String> params = toMap(doc.getRootElement());
        return params;
    }

    /**
     * 转MAP
     * @author
     * @param element
     * @return
     */
    public static Map<String, String> toMap(Element element){
        Map<String, String> rest = new HashMap<String, String>();
        List<Element> els = element.elements();
        for(Element el : els){
            rest.put(el.getName().toLowerCase(), el.getTextTrim());
        }
        return rest;
    }

}
