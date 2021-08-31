package com.sy.sanguo.common.util.request;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.BufferedInputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * https请求
 *
 * @author Administrator
 */
public class HttpsUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpsUtil.class);
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final int DEFAULT_TIMEOUT = 60;
    public static final String POST = "POST";
    public static final String GET = "GET";

    static {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }
            }};
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String urlHostName, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.error("https error:" + e.getMessage(), e);
        }
    }

    /**
     * GET获取访问地址的返回值
     *
     * @param urlAddress URL地址 <br/>
     *                   编码格式 默认UTF-8
     * @return URL的返回内容
     */
    public static final String getUrlReturnValue(String urlAddress) {
        return getUrlReturnValue(urlAddress, DEFAULT_TIMEOUT);
    }

    /**
     * GET获取访问地址的返回值
     *
     * @param urlAddress URL地址 <br/>
     *                   编码格式 默认UTF-8
     * @param seconds    超时时间（秒）
     * @return URL的返回内容
     */
    public static final String getUrlReturnValue(String urlAddress, int seconds) {
        return getUrlReturnValue(urlAddress, DEFAULT_CHARSET, seconds);
    }

    /**
     * 获取访问地址的返回值
     *
     * @param urlAddress URL地址
     * @param charset    编码格式 </br/>
     *                   请求方式:默认GET
     * @return URL的返回内容
     */
    public static final String getUrlReturnValue(String urlAddress, String charset) {
        return getUrlReturnValue(urlAddress, charset, DEFAULT_TIMEOUT);
    }

    /**
     * 获取访问地址的返回值
     *
     * @param urlAddress URL地址
     * @param charset    编码格式 </br/>
     *                   请求方式:默认GET
     * @param seconds    超时时间（秒）
     * @return URL的返回内容
     */
    public static final String getUrlReturnValue(String urlAddress, String charset, int seconds) {
        return getUrlReturnValue(urlAddress, charset, GET, seconds);
    }

    /**
     * 获取访问地址的返回值
     *
     * @param urlAddress URL地址
     * @param charset    编码格式
     * @param method     GET,POST
     * @return URL的返回内容
     */
    public static final String getUrlReturnValue(String urlAddress, String charset, String method) {
        return getUrlReturnValue(urlAddress, charset, method, DEFAULT_TIMEOUT);
    }

    /**
     * 获取访问地址的返回值
     *
     * @param urlAddress URL地址
     * @param charset    编码格式
     * @param method     GET,POST
     * @param seconds    超时时间（秒）
     * @return URL的返回内容
     */
    public static final String getUrlReturnValue(String urlAddress, String charset, String method, int seconds) {
        return getUrlReturnValue(urlAddress, charset, method, null, seconds);
    }

    /**
     * 获取访问地址的返回值
     *
     * @param urlAddress URL地址
     * @param charset    编码格式
     * @param method     GET,POST
     * @param paramsMap  参数值(参数个数为1且key值为$号时，实际请求消息仅为value值)
     * @return URL的返回内容
     */
    public static final String getUrlReturnValue(String urlAddress, String charset, String method,
                                                 Map<String, String> paramsMap) {
        return getUrlReturnValue(urlAddress, charset, method, paramsMap, DEFAULT_TIMEOUT);
    }

    /**
     * 获取访问地址的返回值
     *
     * @param urlAddress URL地址
     * @param charset    编码格式
     * @param method     GET,POST
     * @param paramsMap  参数值(参数个数为1且key值为$号时，实际请求消息仅为value值)
     * @param seconds    超时时间（秒）
     * @return URL的返回内容
     */
    public static final String getUrlReturnValue(String urlAddress, String charset, String method,
                                                 Map<String, String> paramsMap, int seconds) {
        return getUrlReturnValue(urlAddress, charset, method, paramsMap, null, seconds);
    }

    /**
     * 获取访问地址的返回值
     *
     * @param urlAddress           URL地址
     * @param charset              编码格式
     * @param method               GET,POST
     * @param paramsMap            参数值(参数个数为1且key值为$号时，实际请求消息仅为value值)
     * @param requestPropertiesMap requestProperty值
     * @return URL的返回内容
     */
    public static final String getUrlReturnValue(String urlAddress, String charset, String method,
                                                 Map<String, String> paramsMap, Map<String, String> requestPropertiesMap) {
        return getUrlReturnValue(urlAddress, charset, method, paramsMap, requestPropertiesMap, DEFAULT_TIMEOUT);
    }

    /**
     * 获取访问地址的返回值
     *
     * @param urlAddress           URL地址
     * @param charset              编码格式
     * @param method               GET,POST
     * @param paramsMap            参数值(参数个数为1且key值为$号时，实际请求消息仅为value值)
     * @param requestPropertiesMap requestProperty值
     * @param seconds              超时时间（秒）
     * @return URL的返回内容
     */
    public static final String getUrlReturnValue(String urlAddress, String charset, String method,
                                                 Map<String, String> paramsMap, Map<String, String> requestPropertiesMap, int seconds) {
        return getUrlReturnValue(urlAddress, charset, method, paramsMap, null, requestPropertiesMap, seconds);
    }

    /**
     * 获取访问地址的返回值
     *
     * @param urlAddress           URL地址
     * @param charset              编码格式
     * @param method               GET,POST
     * @param paramsMap            参数值(参数个数为1且key值为$号时，实际请求消息仅为value值)
     * @param paramsKeyList        参数名称（有序排列）
     * @param requestPropertiesMap requestProperty值
     * @return URL的返回内容
     */
    public static final String getUrlReturnValue(String urlAddress, String charset, String method,
                                                 Map<String, String> paramsMap, List<String> paramsKeyList, Map<String, String> requestPropertiesMap) {
        return getUrlReturnValue(urlAddress, charset, method, paramsMap, paramsKeyList, requestPropertiesMap, DEFAULT_TIMEOUT);
    }

    /**
     * 获取访问地址的返回值
     *
     * @param urlAddress           URL地址
     * @param charset              编码格式
     * @param method               GET,POST
     * @param paramsMap            参数值(参数个数为1且key值为$号时，实际请求消息仅为value值)
     * @param paramsKeyList        参数名称（有序排列）
     * @param requestPropertiesMap requestProperty值
     * @param seconds              超时时间（秒）
     * @return URL的返回内容
     */
    public static final String getUrlReturnValue(String urlAddress, String charset, String method,
                                                 Map<String, String> paramsMap, List<String> paramsKeyList, Map<String, String> requestPropertiesMap, int seconds) {
        if (urlAddress == null) {
            throw new IllegalArgumentException("param 'urlAddress' is required");
        }
        if (!urlAddress.startsWith("https:")) {
            return HttpUtil.getUrlReturnValue(urlAddress, charset, method, paramsMap, paramsKeyList,
                    requestPropertiesMap);
        }
        StringBuilder paramBuilder = null;

        boolean isSource = false;
        if (paramsMap != null && paramsMap.size() == 1 && paramsMap.get("$") != null) {
            isSource = true;
            paramBuilder = new StringBuilder(paramsMap.get("$"));
        }

        HttpsURLConnection conn = null;
        int tempCode = -1;
        try {
            URL url;

            if (!isSource) {
                int index = urlAddress.indexOf("?");
                if (index != -1 && urlAddress.length() > index + 1) {
                    paramBuilder = new StringBuilder("&");
                    if (urlAddress.indexOf("=") == -1) {
                        String paramStr = CoderUtil.decode(urlAddress.substring(index + 1), charset);
                        String[] params = StringUtils.isNotEmpty(paramStr)
                                ? paramStr.split("&(?=[a-zA-Z_]{1}[\\w]*\\=[\\s\\S]*(?=&|$))") : new String[0];
                        for (String param : params) {
                            int idx = param.indexOf("=");
                            if (idx != -1)
                                paramBuilder.append("&").append(param.substring(0, idx)).append("=")
                                        .append(CoderUtil.encode(param.substring(idx + 1), charset));
                        }
                        if (paramBuilder.length() > 1) {
                            paramBuilder.deleteCharAt(0);
                        }
                    } else {
                        paramBuilder.append(urlAddress.substring(index + 1));
                    }
                    urlAddress = urlAddress.substring(0, index);
                }
                if (paramsMap != null && paramsMap.size() > 0) {
                    if (paramBuilder == null) {
                        paramBuilder = new StringBuilder();
                    }
                    if (paramsKeyList != null && paramsKeyList.size() == paramsMap.size()) {
                        for (String paramKey : paramsKeyList) {
                            String tempValue = paramsMap.get(paramKey);
                            if (tempValue == null) {
                                tempValue = "";
                            }
                            paramBuilder.append("&").append(paramKey).append("=")
                                    .append(CoderUtil.encode(tempValue, charset));
                        }
                    } else {
                        for (Entry<String, String> keyValue : paramsMap.entrySet()) {
                            String tempValue = keyValue.getValue();
                            if (tempValue == null) {
                                tempValue = "";
                            }
                            paramBuilder.append("&").append(keyValue.getKey()).append("=")
                                    .append(CoderUtil.encode(tempValue, charset));
                        }
                    }
                }
            }
            final int millis = seconds * 1000;
            if (POST.equalsIgnoreCase(method)) {
                url = new URL(urlAddress);
                conn = (HttpsURLConnection) url.openConnection();
                if (requestPropertiesMap != null) {
                    for (Entry<String, String> keyValue : requestPropertiesMap.entrySet()) {
                        String tempValue = keyValue.getValue();
                        if (tempValue == null) {
                            tempValue = "";
                        }
                        conn.addRequestProperty(keyValue.getKey(), tempValue);
                    }
                    logger.debug("requestProperties:" + requestPropertiesMap);
                }
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setConnectTimeout(millis);// 设置连接超时
                // 如果在建立连接之前超时期满，则会引发一个
                // java.net.SocketTimeoutException。超时时间为零表示无穷大超时。
                conn.setReadTimeout(millis);// 设置读取超时
                conn.setRequestMethod(POST);
                if (paramBuilder != null) {
                    if (!isSource)
                        while (paramBuilder.indexOf("&") == 0) {
                            paramBuilder.deleteCharAt(0);
                        }
                    String temp = paramBuilder.toString();
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
                    conn.connect();
                    OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream(), charset);
                    osw.write(temp);
                    osw.flush();
                    osw.close();
                    logger.debug(temp);
                } else {
                    conn.connect();
                }
            } else {
                if (paramBuilder != null) {
                    while (paramBuilder.indexOf("&") == 0) {
                        paramBuilder.deleteCharAt(0);
                    }
                    paramBuilder.insert(0, "?");
                    paramBuilder.insert(0, urlAddress);
                    urlAddress = paramBuilder.toString();
                }

                url = new URL(urlAddress);
                conn = (HttpsURLConnection) url.openConnection();
                conn.setConnectTimeout(millis);// 设置连接超时
                // 如果在建立连接之前超时期满，则会引发一个
                // java.net.SocketTimeoutException。超时时间为零表示无穷大超时。
                conn.setReadTimeout(millis);// 设置读取超时
                conn.setRequestMethod(GET);

                if (requestPropertiesMap != null) {
                    for (Entry<String, String> keyValue : requestPropertiesMap.entrySet()) {
                        String tempValue = keyValue.getValue();
                        if (tempValue == null) {
                            tempValue = "";
                        }
                        conn.addRequestProperty(keyValue.getKey(), tempValue);
                    }
                    logger.debug("requestProperties:" + requestPropertiesMap);
                }
                conn.connect();
            }
            logger.debug(urlAddress);

            tempCode = conn.getResponseCode();
            BufferedInputStream bis;
            if (tempCode == 200) {
                bis = new BufferedInputStream(conn.getInputStream(), 1024);
            } else {
                bis = new BufferedInputStream(conn.getErrorStream(), 1024);
            }

            int length = -1;
            StringBuilder result = new StringBuilder();
            byte[] buf = new byte[1024];
            while ((length = bis.read(buf)) != -1) {
                result.append(new String(buf, 0, length, charset));
            }

            bis.close();
            bis = null;
            buf = null;

            if (tempCode == 200) {
                return result.toString();
            } else {
                logger.error(new StringBuilder().append(urlAddress).append(" Code:").append(tempCode)
                        .append(" getUrlReturnValue Error>>>").append(result.toString()).toString());
                return null;
            }
        } catch (Exception e) {
            logger.error(new StringBuilder().append(urlAddress).append(" Code:").append(tempCode)
                    .append(" getUrlReturnValue Exception>>>").append(e.getMessage()).toString(), e);
            return null;
        } finally {
            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Exception e) {
                }
                conn = null;
            }
        }
    }

    /**
     * GET获取访问地址的返回值
     *
     * @param urlAddress URL地址 <br/>
     *                   编码格式 默认UTF-8
     * @return status code from an HTTP response message
     */
    public static final int getUrlAccessCode(String urlAddress) {
        return getUrlAccessCode(urlAddress, DEFAULT_TIMEOUT);
    }

    /**
     * GET获取访问地址的返回值
     *
     * @param urlAddress URL地址 <br/>
     *                   编码格式 默认UTF-8
     * @param seconds    超时时间（秒）
     * @return status code from an HTTP response message
     */
    public static final int getUrlAccessCode(String urlAddress, int seconds) {
        return getUrlAccessCode(urlAddress, DEFAULT_CHARSET, seconds);
    }

    /**
     * 获取访问地址的返回值
     *
     * @param urlAddress URL地址
     * @param charset    编码格式 </br/>
     *                   请求方式:默认GET
     * @return status code from an HTTP response message
     */
    public static final int getUrlAccessCode(String urlAddress, String charset) {
        return getUrlAccessCode(urlAddress, charset, DEFAULT_TIMEOUT);
    }

    /**
     * 获取访问地址的返回值
     *
     * @param urlAddress URL地址
     * @param charset    编码格式 </br/>
     *                   请求方式:默认GET
     * @param seconds    超时时间（秒）
     * @return status code from an HTTP response message
     */
    public static final int getUrlAccessCode(String urlAddress, String charset, int seconds) {
        return getUrlAccessCode(urlAddress, charset, GET, seconds);
    }

    /**
     * 获取访问地址的返回值
     *
     * @param urlAddress URL地址
     * @param charset    编码格式
     * @param method     GET,POST
     * @return status code from an HTTP response message
     */
    public static final int getUrlAccessCode(String urlAddress, String charset, String method) {
        return getUrlAccessCode(urlAddress, charset, method, DEFAULT_TIMEOUT);
    }

    /**
     * 获取访问地址的返回值
     *
     * @param urlAddress URL地址
     * @param charset    编码格式
     * @param method     GET,POST
     * @param seconds    超时时间（秒）
     * @return status code from an HTTP response message
     */
    public static final int getUrlAccessCode(String urlAddress, String charset, String method, int seconds) {
        return getUrlAccessCode(urlAddress, charset, method, null, seconds);
    }

    /**
     * 获取访问地址的返回值
     *
     * @param urlAddress URL地址
     * @param charset    编码格式
     * @param method     GET,POST
     * @param paramsMap  参数值(参数个数为1且key值为$号时，实际请求消息仅为value值)
     * @return status code from an HTTP response message
     */
    public static final int getUrlAccessCode(String urlAddress, String charset, String method,
                                             Map<String, String> paramsMap) {
        return getUrlAccessCode(urlAddress, charset, method, paramsMap, DEFAULT_TIMEOUT);
    }

    /**
     * 获取访问地址的返回值
     *
     * @param urlAddress URL地址
     * @param charset    编码格式
     * @param method     GET,POST
     * @param paramsMap  参数值(参数个数为1且key值为$号时，实际请求消息仅为value值)
     * @param seconds    超时时间（秒）
     * @return status code from an HTTP response message
     */
    public static final int getUrlAccessCode(String urlAddress, String charset, String method,
                                             Map<String, String> paramsMap, int seconds) {
        return getUrlAccessCode(urlAddress, charset, method, paramsMap, null, seconds);
    }

    /**
     * 获取访问地址的返回值
     *
     * @param urlAddress           URL地址
     * @param charset              编码格式
     * @param method               GET,POST
     * @param paramsMap            参数值(参数个数为1且key值为$号时，实际请求消息仅为value值)
     * @param requestPropertiesMap requestProperty值
     * @return status code from an HTTP response message
     */
    public static final int getUrlAccessCode(String urlAddress, String charset, String method,
                                             Map<String, String> paramsMap, Map<String, String> requestPropertiesMap) {
        return getUrlAccessCode(urlAddress, charset, method, paramsMap, requestPropertiesMap, DEFAULT_TIMEOUT);
    }

    /**
     * 获取访问地址的返回值
     *
     * @param urlAddress           URL地址
     * @param charset              编码格式
     * @param method               GET,POST
     * @param paramsMap            参数值(参数个数为1且key值为$号时，实际请求消息仅为value值)
     * @param requestPropertiesMap requestProperty值
     * @param seconds              超时时间（秒）
     * @return status code from an HTTP response message
     */
    public static final int getUrlAccessCode(String urlAddress, String charset, String method,
                                             Map<String, String> paramsMap, Map<String, String> requestPropertiesMap, int seconds) {
        return getUrlAccessCode(urlAddress, charset, method, paramsMap, null, requestPropertiesMap, seconds);
    }

    /**
     * 获取访问地址的返回值
     *
     * @param urlAddress           URL地址
     * @param charset              编码格式
     * @param method               GET,POST
     * @param paramsMap            参数值(参数个数为1且key值为$号时，实际请求消息仅为value值)
     * @param paramsKeyList        参数名称（有序排列）
     * @param requestPropertiesMap requestProperty值
     * @return status code from an HTTP response message
     */
    public static final int getUrlAccessCode(String urlAddress, String charset, String method,
                                             Map<String, String> paramsMap, List<String> paramsKeyList, Map<String, String> requestPropertiesMap) {
        return getUrlAccessCode(urlAddress, charset, method, paramsMap, paramsKeyList, requestPropertiesMap, DEFAULT_TIMEOUT);
    }

    /**
     * 获取访问地址的返回值
     *
     * @param urlAddress           URL地址
     * @param charset              编码格式
     * @param method               GET,POST
     * @param paramsMap            参数值(参数个数为1且key值为$号时，实际请求消息仅为value值)
     * @param paramsKeyList        参数名称（有序排列）
     * @param requestPropertiesMap requestProperty值
     * @param seconds              超时时间（秒）
     * @return status code from an HTTP response message
     */
    public static final int getUrlAccessCode(String urlAddress, String charset, String method,
                                             Map<String, String> paramsMap, List<String> paramsKeyList, Map<String, String> requestPropertiesMap, int seconds) {
        if (urlAddress == null) {
            throw new IllegalArgumentException("param 'urlAddress' is required");
        }
        if (!urlAddress.startsWith("https:")) {
            return HttpUtil.getUrlAccessCode(urlAddress, charset, method, paramsMap, paramsKeyList,
                    requestPropertiesMap);
        }
        StringBuilder paramBuilder = null;

        boolean isSource = false;
        if (paramsMap != null && paramsMap.size() == 1 && paramsMap.get("$") != null) {
            isSource = true;
            paramBuilder = new StringBuilder(paramsMap.get("$"));
        }

        HttpsURLConnection conn = null;
        int tempCode = -1;
        try {
            URL url;

            if (!isSource) {
                int index = urlAddress.indexOf("?");
                if (index != -1 && urlAddress.length() > index + 1) {
                    paramBuilder = new StringBuilder("&");
                    if (urlAddress.indexOf("=") == -1) {
                        String paramStr = CoderUtil.decode(urlAddress.substring(index + 1), charset);
                        String[] params = StringUtils.isNotEmpty(paramStr)
                                ? paramStr.split("&(?=[a-zA-Z_]{1}[\\w]*\\=[\\s\\S]*(?=&|$))") : new String[0];
                        for (String param : params) {
                            int idx = param.indexOf("=");
                            if (idx != -1)
                                paramBuilder.append("&").append(param.substring(0, idx)).append("=")
                                        .append(CoderUtil.encode(param.substring(idx + 1), charset));
                        }
                        if (paramBuilder.length() > 1) {
                            paramBuilder.deleteCharAt(0);
                        }
                    } else {
                        paramBuilder.append(urlAddress.substring(index + 1));
                    }
                    urlAddress = urlAddress.substring(0, index);
                }
                if (paramsMap != null && paramsMap.size() > 0) {
                    if (paramBuilder == null) {
                        paramBuilder = new StringBuilder();
                    }
                    if (paramsKeyList != null && paramsKeyList.size() == paramsMap.size()) {
                        for (String paramKey : paramsKeyList) {
                            String tempValue = paramsMap.get(paramKey);
                            if (tempValue == null) {
                                tempValue = "";
                            }
                            paramBuilder.append("&").append(paramKey).append("=")
                                    .append(CoderUtil.encode(tempValue, charset));
                        }
                    } else {
                        for (Entry<String, String> keyValue : paramsMap.entrySet()) {
                            String tempValue = keyValue.getValue();
                            if (tempValue == null) {
                                tempValue = "";
                            }
                            paramBuilder.append("&").append(keyValue.getKey()).append("=")
                                    .append(CoderUtil.encode(tempValue, charset));
                        }
                    }
                }
            }
            final int millis = seconds * 1000;
            if (POST.equalsIgnoreCase(method)) {
                url = new URL(urlAddress);
                conn = (HttpsURLConnection) url.openConnection();

                if (requestPropertiesMap != null) {
                    for (Entry<String, String> keyValue : requestPropertiesMap.entrySet()) {
                        String tempValue = keyValue.getValue();
                        if (tempValue == null) {
                            tempValue = "";
                        }
                        conn.addRequestProperty(keyValue.getKey(), tempValue);
                    }
                    logger.debug("requestProperties:" + requestPropertiesMap);
                }
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setConnectTimeout(millis);// 设置连接超时
                // 如果在建立连接之前超时期满，则会引发一个
                // java.net.SocketTimeoutException。超时时间为零表示无穷大超时。
                conn.setReadTimeout(millis);// 设置读取超时
                conn.setRequestMethod(POST);
                if (paramBuilder != null) {
                    if (!isSource)
                        while (paramBuilder.indexOf("&") == 0) {
                            paramBuilder.deleteCharAt(0);
                        }
                    String temp = paramBuilder.toString();
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
                    conn.connect();
                    OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream(), charset);
                    osw.write(temp);
                    osw.flush();
                    osw.close();
                    logger.debug(temp);
                } else {
                    conn.connect();
                }
            } else {
                if (paramBuilder != null) {
                    while (paramBuilder.indexOf("&") == 0) {
                        paramBuilder.deleteCharAt(0);
                    }
                    paramBuilder.insert(0, "?");
                    paramBuilder.insert(0, urlAddress);
                    urlAddress = paramBuilder.toString();
                }

                url = new URL(urlAddress);
                conn = (HttpsURLConnection) url.openConnection();
                conn.setConnectTimeout(millis);// 设置连接超时
                // 如果在建立连接之前超时期满，则会引发一个
                // java.net.SocketTimeoutException。超时时间为零表示无穷大超时。
                conn.setReadTimeout(millis);// 设置读取超时
                conn.setRequestMethod(GET);

                if (requestPropertiesMap != null) {
                    for (Entry<String, String> keyValue : requestPropertiesMap.entrySet()) {
                        String tempValue = keyValue.getValue();
                        if (tempValue == null) {
                            tempValue = "";
                        }
                        conn.addRequestProperty(keyValue.getKey(), tempValue);
                    }
                    logger.debug("requestProperties:" + requestPropertiesMap);
                }
                conn.connect();
            }

            logger.debug(urlAddress);
            tempCode = conn.getResponseCode();
            return tempCode;
        } catch (Exception e) {
            logger.error(new StringBuilder().append(urlAddress).append(" Code:").append(tempCode)
                    .append(" getUrlAccessCode Exception>>>").append(e.getMessage()).toString(), e);
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Exception e) {
                }
                conn = null;
            }
        }
    }
}
