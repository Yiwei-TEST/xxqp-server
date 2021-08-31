package com.sy599.game.websocket.netty;

import com.sy.mainland.util.SecretUtil2;
import com.sy599.game.util.ResourcesConfigsUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.SslHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public final class SslUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger("msg");

    public static SslHandler createSslHandler() {
        String path = ResourcesConfigsUtil.loadServerPropertyValue("ssl_file_path");
        String password = ResourcesConfigsUtil.loadServerPropertyValue("ssl_password");

        if (StringUtils.isBlank(path)||StringUtils.isBlank(password)){
            return null;
        }

        try {
            SSLContext sslContext = SslUtil.createSSLContext(ResourcesConfigsUtil.loadServerPropertyValue("ssl_type", "JKS"),path , SecretUtil2.decrypt(password, null));
            //SSLEngine 此类允许使用ssl安全套接层协议进行安全通信
            SSLEngine engine = sslContext.createSSLEngine();
            engine.setUseClientMode(false);
            engine.setNeedClientAuth(false);
            return new SslHandler(engine);
        } catch (Exception e) {
            LOGGER.error("SSLContext Exception:" + e.getMessage(), e);
        }
        return null;
    }

    public static boolean hasSslHandler(ChannelHandlerContext ctx) {
        return ctx.pipeline().get("SslHandler") != null;
    }

    public static SSLContext createSSLContext(String type, String path, String password) throws Exception {
        KeyStore ks = KeyStore.getInstance(type); /// "JKS"
        InputStream ksInputStream = new FileInputStream(path); /// 证书存放地址
        ks.load(ksInputStream, password.toCharArray());
        //KeyManagerFactory充当基于密钥内容源的密钥管理器的工厂。
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());//getDefaultAlgorithm:获取默认的 KeyManagerFactory 算法名称。
        kmf.init(ks, password.toCharArray());
        //SSLContext的实例表示安全套接字协议的实现，它充当用于安全套接字工厂或 SSLEngine 的工厂。
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }}, new SecureRandom());
        return sslContext;
    }

}
