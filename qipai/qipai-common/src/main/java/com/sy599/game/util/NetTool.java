package com.sy599.game.util;

import com.alibaba.fastjson.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


/**
 * NetTool:封装一个类搞定90%安卓客户端与服务器端交互
 */
public class NetTool {
    private int TIMEOUT = 10000;// 10秒
    private Map<String, String> reqProperty;

    public void addReqProperty(String key, String value) {
        if (reqProperty == null) {
            reqProperty = new HashMap<String, String>();
        }
        reqProperty.put(key, value);
    }

    public void setTIMEOUT(int tIMEOUT) {
        TIMEOUT = tIMEOUT;
    }

    /**
     * 通过post方式提交参数给服务器
     */
    public String sendPost(String urlPath, Map<String, String> params, String encoding) throws Exception {

        // 使用StringBuilder对象
        StringBuilder sb = new StringBuilder(urlPath);
        sb.append('?');

        // 迭代Map
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(entry.getKey()).append('=').append(URLEncoder.encode(entry.getValue(), encoding)).append('&');
        }
        sb.deleteCharAt(sb.length() - 1);
        // 打开链接
        URL url = new URL(sb.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type", "text/xml");
            conn.setRequestProperty("Charset", encoding);
            conn.setRequestProperty("cache-control", "no-cache");
            if (reqProperty != null) {
                for (Map.Entry<String, String> entry : reqProperty.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());

                }
            }
            conn.setConnectTimeout(TIMEOUT);
            // 如果请求响应码是200，则表示成功
            if (conn.getResponseCode() == 200) {
                // 获得服务器响应的数据
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), encoding));
                // 数据
                String retData = null;
                String responseData = "";
                while ((retData = in.readLine()) != null) {
                    responseData += retData;
                }
                in.close();
                return responseData;
            }
            return "sendGetRequest error!";
        } finally {
            close(conn);
        }
    }

    /**
     * 传送文本,例如Json,xml等
     */
    public String sendTxt(String urlPath, String txt, String encoding)
            throws Exception {
        byte[] sendData = txt.getBytes();
        URL url = new URL(urlPath);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(TIMEOUT);
            conn.setDoOutput(true);// 如果通过post提交数据，必须设置允许对外输出数据
            conn.setRequestProperty("Content-Type", "text/xml");
            conn.setRequestProperty("Charset", encoding);
            conn.setRequestProperty("Content-Length", String.valueOf(sendData.length));
            OutputStream outStream = conn.getOutputStream();
            outStream.write(sendData);
            outStream.flush();
            outStream.close();
            if (conn.getResponseCode() == 200) {
                // 获得服务器响应的数据
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(), encoding));
                // 数据
                String retData = null;
                String responseData = "";
                while ((retData = in.readLine()) != null) {
                    responseData += retData;
                }
                in.close();
                return responseData;
            }
            return "sendText error!";
        } finally {
            close(conn);
        }
    }

    /**
     * 上传文件
     */
    public String sendFile(String urlPath, String filePath,
                           String newName) throws Exception {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        URL url = new URL(urlPath);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        try {
        /* 允许Input、Output，不使用Cache */
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
        /* 设置传送的method=POST */
            con.setRequestMethod("POST");
        /* setRequestProperty */

            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Charset", "UTF-8");
            con.setRequestProperty("Content-Type", "multipart/form-data;boundary="
                    + boundary);
        /* 设置DataOutputStream */
            DataOutputStream ds = new DataOutputStream(con.getOutputStream());
            ds.writeBytes(twoHyphens + boundary + end);
            ds.writeBytes("Content-Disposition: form-data; "
                    + "name=\"file1\";filename=\"" + newName + "\"" + end);
            ds.writeBytes(end);

        /* 取得文件的FileInputStream */
            FileInputStream fStream = new FileInputStream(filePath);
        /* 设置每次写入1024bytes */
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            int length = -1;
        /* 从文件读取数据至缓冲区 */
            while ((length = fStream.read(buffer)) != -1) {
            /* 将资料写入DataOutputStream中 */
                ds.write(buffer, 0, length);
            }
            ds.writeBytes(end);
            ds.writeBytes(twoHyphens + boundary + twoHyphens + end);

        /* close streams */
            fStream.close();
            ds.flush();

        /* 取得Response内容 */
            InputStream is = con.getInputStream();
            int ch;
            StringBuffer b = new StringBuffer();
            while ((ch = is.read()) != -1) {
                b.append((char) ch);
            }
        /* 关闭DataOutputStream */
            ds.close();
            return b.toString();
        } finally {
            close(con);
        }
    }

    /**
     * 通过get方式提交参数给服务器
     */
    public String sendGetRequest(String urlPath,
                                 Map<String, String> params, String encoding) throws Exception {

        // 使用StringBuilder对象
        StringBuilder sb = new StringBuilder(urlPath);
        sb.append('?');

        // 迭代Map
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(entry.getKey()).append('=')
                    .append(URLEncoder.encode(entry.getValue(), encoding))
                    .append('&');
        }
        sb.deleteCharAt(sb.length() - 1);
        // 打开链接
        URL url = new URL(sb.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type", "text/xml");
            conn.setRequestProperty("Charset", encoding);
            conn.setRequestProperty("cache-control", "no-cache");
            conn.setConnectTimeout(TIMEOUT);
            // 如果请求响应码是200，则表示成功
            if (conn.getResponseCode() == 200) {
                // 获得服务器响应的数据
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(), encoding));
                // 数据
                String retData = null;
                String responseData = "";
                while ((retData = in.readLine()) != null) {
                    responseData += retData;
                }
                in.close();
                return responseData;
            }
            return "sendGetRequest error!";
        } finally {
            close(conn);
        }
    }

    /**
     * 通过Post方式提交参数给服务器
     */
    public String sendPostRequest(String urlPath, Map<String, String> params, String encoding) throws Exception {
        StringBuilder sb = new StringBuilder();
        // 如果参数不为空
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                // Post方式提交参数的话，不能省略内容类型与长度
                sb.append(entry.getKey()).append('=')
                        .append(URLEncoder.encode(entry.getValue(), encoding))
                        .append('&');
            }
            sb.deleteCharAt(sb.length() - 1);
        }

        byte[] entitydata = sb.toString().getBytes();// 得到实体的二进制数据
        URL url = new URL(urlPath);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(TIMEOUT);
            conn.setDoOutput(true);// 如果通过post提交数据，必须设置允许对外输出数据
            // 这里只设置内容类型与内容长度的头字段
            conn.setRequestProperty("Content-Type", "text/xml ");//text/xml   application/x-www-form-urlencoded
            conn.setRequestProperty("Charset", encoding);
            conn.setRequestProperty("cache-control", "no-cache");
            conn.setRequestProperty("Content-Length", String.valueOf(entitydata.length));
            OutputStream outStream = conn.getOutputStream();
            // 把实体数据写入是输出流
            outStream.write(entitydata);
            // 内存中的数据刷入
            outStream.flush();
            outStream.close();
            // 如果请求响应码是200，则表示成功
            if (conn.getResponseCode() == 200) {
                // 获得服务器响应的数据
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(), encoding));
                // 数据
                String retData = null;
                String responseData = "";
                while ((retData = in.readLine()) != null) {
                    responseData += retData;
                }
                in.close();
                return responseData;
            }
            return "error";
        } finally {
            close(conn);
        }
    }

    /**
     * 通过Post方式提交参数给服务器,也可以用来传送json或xml文件
     */
    public String sendPostJsonRequest(String urlPath, JSONObject json, String encoding) throws Exception {

//    	Log.d("sy599",">>>>>>"+urlPath);
//    	Log.d("sy599",">>>>>>"+json.toString());
//        StringBuilder sb = new StringBuilder();
        byte[] entitydata = json.toString().getBytes();// 得到实体的二进制数据
        URL url = new URL(urlPath);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(TIMEOUT);
            conn.setDoOutput(true);// 如果通过post提交数据，必须设置允许对外输出数据
            //=============== 这里只设置内容类型与内容长度的头字段======================//
            conn.setRequestProperty("Content-Type", "application/json");//text/xml   application/x-www-form-urlencoded
            conn.setRequestProperty("Charset", encoding);
            conn.setRequestProperty("cache-control", "no-cache");
            conn.setRequestProperty("Content-Length", String.valueOf(entitydata.length));
            conn.setUseCaches(false);
            OutputStream outStream = conn.getOutputStream();
            outStream.write(entitydata);// 把实体数据写入是输出流
            outStream.flush();// 内存中的数据刷入
            outStream.close();
            // 如果请求响应码是200，则表示成功
            if (conn.getResponseCode() == 200) {
                // 获得服务器响应的数据
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(), encoding));
                // 数据
                String retData = null;
                String responseData = "";
                while ((retData = in.readLine()) != null) {
                    responseData += retData;
                }
                in.close();
//            Log.d("sy599","<<<<<"+responseData);
                return responseData;
            }

            return "error";
        } finally {
            close(conn);
        }
    }

    /**
     * 根据URL直接读文件内容，前提是这个文件当中的内容是文本，函数的返回值就是文件当中的内容
     */
    public String readTxtFile(String urlStr, String encoding)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        String line = null;
        BufferedReader buffer = null;
        HttpURLConnection urlConn = null;
        try {
            // 创建一个URL对象
            URL url = new URL(urlStr);
            // 创建一个Http连接
            urlConn = (HttpURLConnection) url
                    .openConnection();
            // 使用IO流读取数据
            buffer = new BufferedReader(new InputStreamReader(
                    urlConn.getInputStream(), encoding));
            while ((line = buffer.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                buffer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            close(urlConn);
        }
        return sb.toString();
    }

    /**
     * 根据URL得到输入流
     *
     * @param urlStr
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    public InputStream getInputStreamFromUrl(String urlStr)
            throws MalformedURLException, IOException {
        URL url = new URL(urlStr);
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        try {
            InputStream inputStream = urlConn.getInputStream();
            return inputStream;
        } finally {
            close(urlConn);
        }
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
