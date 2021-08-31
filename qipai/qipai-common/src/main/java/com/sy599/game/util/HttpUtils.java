package com.sy599.game.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 * HTTP请求
 *
 * @author wqc
 * @version v1.0
 * @date 2012-7-24
 */
public class HttpUtils {

    public static final String text = "text/plain;charset=UTF-8";
    public static final String post = "application/x-www-form-urlencoded;charset=UTF-8";

    public enum ContentType {
        asf("video/x-ms-asf"),
        avi("video/avi"),
        mpg("ivideo/mpeg"),
        gif("image/gif"),
        jpg("image/jpeg"),
        bmp("image/bmp"),
        png("image/png"),
        wav("audio/wav"),
        mp3("audio/mpeg3"),
        html("text/html"),
        txt("text/plain"),
        zip("application/zip"),
        doc("application/msword"),
        xls("application/vnd.ms-excel"),
        rtf("application/rtf"),
        all("application/octet-stream");


        private String type;

        private ContentType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public String toString() {
            return type;
        }
    }

    private String httpURL;
    private URL url;
    /**
     * ����ͷ����
     **/
    private Map<String, String> requestProperty;
    /**
     * l�ӳ�ʱ
     **/
    private int connectTimeout = 5000;
    /**
     * ��Ӧ��ʱ
     **/
    private int readTimeout = 3000;

    /**
     * @param httpURL ��http��ַ����Http����
     * @throws Exception
     */
    public HttpUtils(String httpURL) throws Exception {
        this.httpURL = httpURL;
        init();
    }


    /**
     * POSTЭ�鴫�ݲ���
     *
     * @param params post����
     * @return ��Ӧ
     * @throws Exception
     */
    public String post(String params) throws Exception {
        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
        try {
            huc.setRequestMethod("POST");
            huc.setDoOutput(true);
            huc.setDoInput(true);
            huc.setConnectTimeout(connectTimeout);
            huc.setReadTimeout(readTimeout);
            for (String property : requestProperty.keySet()) {
                huc.setRequestProperty(property, requestProperty.get(property));
            }
            huc.connect();

            OutputStream out = huc.getOutputStream();
            out.write(params.getBytes("UTF-8"));
            out.flush();
            out.close();

            String err = checkError(huc);
            if (err != null) return err;

            BufferedReader in = new BufferedReader(new InputStreamReader(huc.getInputStream(), "utf-8"));
            StringBuffer resp = new StringBuffer();

            String s = in.readLine();
            while (s != null) {
                resp.append(s);
                s = in.readLine();
            }
            in.close();

            return resp.toString();
        } finally {
            close(huc);
        }
    }

    public String post(String params, int connTimeOut, int readTimeOut) throws Exception {
        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
        try {
            huc.setRequestMethod("POST");
            huc.setDoOutput(true);
            huc.setDoInput(true);
            huc.setConnectTimeout(connTimeOut);
            huc.setReadTimeout(readTimeOut);
            for (String property : requestProperty.keySet()) {
                huc.setRequestProperty(property, requestProperty.get(property));
            }
            huc.connect();

            OutputStream out = huc.getOutputStream();
            out.write(params.getBytes("UTF-8"));
            out.flush();
            out.close();

            String err = checkError(huc);
            if (err != null) return err;

            BufferedReader in = new BufferedReader(new InputStreamReader(huc.getInputStream(), "utf-8"));
            StringBuffer resp = new StringBuffer();

            String s = in.readLine();
            while (s != null) {
                resp.append(s);
                s = in.readLine();
            }
            in.close();

            return resp.toString();
        } finally {
            close(huc);
        }
    }


    /**
     * POST MAP
     *
     * @param param post����
     * @return ��Ӧ
     * @throws Exception
     */
    public String post(Map<String, String> param) throws Exception {
        StringBuffer s = new StringBuffer();
        //k=v&k=v
        for (String k : param.keySet()) {
            s.append("&").append(k).append("=").append(param.get(k));
        }
        s.deleteCharAt(0);
        return post(s.toString());
    }

    /**
     * POST JSON
     *
     * @param param post����
     * @return ��Ӧ
     * @throws Exception
     */
    public String post(JSONObject param) throws Exception {
        StringBuffer s = new StringBuffer();
        //k=v&k=v
        for (Object k : param.keySet()) {
            s.append("&").append(k).append("=").append(param.get(k));
        }
        s.deleteCharAt(0);
        return post(s.toString());
    }


    /**
     * �����ļ�
     *
     * @param fileParamName ������յĲ�����
     * @param fileName      �ļ���
     * @param file          �ļ�
     * @return ��Ӧ
     * @throws Exception
     */
    public String post(String fileParamName, String fileName, byte[] file)
            throws Exception {
        return post(fileParamName, fileName, file, null);
    }


    /**
     * �����ļ�
     *
     * @param fileParamName ������յĲ�����
     * @param fileName      �ļ���
     * @param file          �ļ�
     * @param params        ����
     * @return ��Ӧ
     * @throws Exception
     */
    public String post(String fileParamName, String fileName, byte[] file, Map<String, String> params)
            throws Exception {
        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
        try {
            huc.setRequestMethod("POST");
            huc.setDoOutput(true);
            huc.setDoInput(true);
            huc.setConnectTimeout(connectTimeout);
            huc.setReadTimeout(readTimeout);

            //�ָ�
            String boundary = "-----------------------------114975832116442893661388290519";
            huc.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            boundary = "--" + boundary;

            StringBuffer sb = new StringBuffer();
            //����
            if (params != null) {
                for (Iterator<String> it = params.keySet().iterator(); it.hasNext(); ) {
                    String k = it.next();
                    String v = params.get(k);
                    sb.append(boundary).append("\r\n");
                    sb.append("Content-Disposition: form-data; name=\"" + k + "\"\r\n\r\n");
                    sb.append(v).append("\r\n");
                }
            }

            //�ļ�
            sb.append(boundary).append("\r\n");
            sb.append("Content-Disposition: form-data; name=\"" + fileParamName
                    + "\"; filename=\"" + fileName + "\"\r\n");
            sb.append("Content-Type: " + getContentType(fileName) + " \r\n\r\n");

            huc.connect();

            OutputStream out = huc.getOutputStream();
            out.write(sb.toString().getBytes("UTF-8"));
            out.write(file);
            out.flush();
            out.close();

            String err = checkError(huc);
            if (err != null) return err;

            BufferedReader in = new BufferedReader(new InputStreamReader(huc.getInputStream(), "utf-8"));
            StringBuffer resp = new StringBuffer();

            String s = in.readLine();
            while (s != null) {
                resp.append(s);
                s = in.readLine();
            }
            in.close();

            return resp.toString();
        } finally {
            close(huc);
        }
    }


    /**
     * ��Ӧ�ļ�
     *
     * @param resp
     * @param fileName �ļ���
     * @param file     �ļ�
     * @return boolean �Ƿ���Ӧ�ɹ�
     * @throws Exception
     */
    public static boolean response(HttpServletResponse resp, String fileName, byte[] file) {
        if (resp == null || fileName == null || fileName.trim().equals("")
                || file == null || file.length == 0) {
            throw new NullPointerException("param is null");
        }
        try {//�ļ������ı���
            fileName = new String(fileName.getBytes("GBK"), "ISO8859-1");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        resp.reset();
        resp.setCharacterEncoding("UTF-8");
        resp.addHeader("Content-Disposition", "attachment;filename=" + fileName + ";");
        resp.setContentType(getContentType(fileName));
        try {
            OutputStream out = resp.getOutputStream();
            out.write(file);
            out.flush();
            out.close();
        } catch (Throwable e) {
            return false;
        }
        return true;
    }


    /**
     * GETЭ�鷢��Http����
     *
     * @return String ��Ӧ
     * @throws Exception
     */
    public static String get(String httpUrl) throws Exception {
        return get(httpUrl, 5000, 5000);
    }

    /**
     * GETЭ�鷢��Http����
     *
     * @return String ��Ӧ
     * @throws Exception
     */
    public static String get(String httpUrl, int connectTimeout, int readTimeout) throws Exception {
        URL url = new URL(httpUrl);
        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
        try {
            huc.setRequestMethod("GET");
            huc.setDoOutput(false);
            huc.setDoInput(true);
            huc.setConnectTimeout(connectTimeout);
            huc.setReadTimeout(readTimeout);
            huc.connect();

            String err = checkError(huc);
            if (err != null) return err;

            BufferedReader in = new BufferedReader(new InputStreamReader(huc.getInputStream(), "utf-8"));
            StringBuffer resp = new StringBuffer();

            String s = in.readLine();
            while (s != null) {
                resp.append(s);
                s = in.readLine();
            }
            in.close();

            return resp.toString();
        } finally {
            close(huc);
        }
    }


    /**
     * ��ȡ�ļ�����
     *
     * @param fileName
     * @return String
     */
    public static String getContentType(String fileName) {
        //�ļ���Сд
        String filename = fileName.toLowerCase();
        if (filename.endsWith(".asf")) {
            return ContentType.asf.toString();
        } else if (filename.endsWith(".avi")) {
            return ContentType.avi.toString();
        } else if (filename.endsWith(".mpg") || filename.endsWith(".mpeg")) {
            return ContentType.mpg.toString();
        } else if (filename.endsWith(".gif")) {
            return ContentType.gif.toString();
        } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return ContentType.jpg.toString();
        } else if (filename.endsWith(".bmp")) {
            return ContentType.bmp.toString();
        } else if (filename.endsWith(".png")) {
            return ContentType.png.toString();
        } else if (filename.endsWith(".wav")) {
            return ContentType.wav.toString();
        } else if (filename.endsWith(".mp3")) {
            return ContentType.mp3.toString();
        } else if (filename.endsWith(".htm") || filename.endsWith(".html")) {
            return ContentType.html.toString();
        } else if (filename.endsWith(".txt")) {
            return ContentType.txt.toString();
        } else if (filename.endsWith(".zip")) {
            return ContentType.zip.toString();
        } else if (filename.endsWith(".doc")) {
            return ContentType.doc.toString();
        } else if (filename.endsWith(".xls")) {
            return ContentType.xls.toString();
        } else if (filename.endsWith(".rtf")) {
            return ContentType.rtf.toString();
        }

        return ContentType.all.toString();
    }

    /**
     * ����쳣
     *
     * @param huc l��
     * @return String �쳣��Ӧ
     * @throws Exception String
     */
    private static String checkError(HttpURLConnection huc) throws Exception {

        if (huc.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR//500����
                ) {
            BufferedReader in = new BufferedReader(new InputStreamReader(huc.getErrorStream(), "ISO8859-1"));
            StringBuffer resp = new StringBuffer();
            String s = in.readLine();
            while (s != null) {
                resp.append(s).append("\n");
                s = in.readLine();
            }
            in.close();
            return resp.toString();
        }


        return null;
    }


    //��ʼ��
    private void init() throws Exception {
        if (httpURL == null || (!httpURL.startsWith("http://") && !httpURL.startsWith("https://"))) {
            throw new NullPointerException("param is't url-" + httpURL);
        }
        url = new URL(httpURL);
        if (requestProperty == null) {
            requestProperty = new HashMap<String, String>();
        }
        if (requestProperty.isEmpty()) {
//			requestProperty.put("Content-type", text);
            requestProperty.put("Content-type", post);
        }
    }


    public String getHttpURL() {
        return httpURL;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        if (connectTimeout < 1) return;
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        if (readTimeout < 1) return;
        this.readTimeout = readTimeout;
    }

    public void setRequestProperty(Map<String, String> requestProperty) {
        this.requestProperty = requestProperty;
    }

    public String getAddress(String ip) {
        String address = "";
        try {
            String temp = post("ip=" + ip);
            JSONObject json = JSONObject.parseObject(temp);
            if (json.getIntValue("code") == 0) {
                json = json.getJSONObject("data");
                String country = json.getString("country");
                String countryId = json.getString("country_id");
                if (!countryId.equals("CN")) {
                    address = country;
                } else {
                    String area = json.getString("region");
                    area = area.substring(0, 2);
                    if (area.equals("黑龙")) {
                        area = "黑龙江";
                    } else if (area.equals("内蒙")) {
                        area = "内蒙古";
                    }

                    String region = json.getString("city");
                    String lastVal = region.substring(region.length() - 1, region.length());
                    if (lastVal.equals("市")) {
                        region = region.substring(0, region.length() - 1);
                    }
                    ;

                    if (area.equals(region)) {
                        address = area;
                    } else {
                        address = area + region;
                    }

                }

            }
        } catch (Exception e) {
        }

        return address;
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

    /**
     *@description 字符串转原始编码
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2020/6/15
     */
	public static String forSrcDecoder(String val) {
		if(StringUtils.isBlank(val)){
			return val;
		}

		try {
			char[] chars = val.toCharArray();//获取原始编码
			byte[] bytes = new byte[chars.length];
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = (byte) chars[i];
			}
			return new String(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return val;
	}

}
