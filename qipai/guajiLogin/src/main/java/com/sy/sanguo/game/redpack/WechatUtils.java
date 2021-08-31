package com.sy.sanguo.game.redpack;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONObject;
import com.sy.sanguo.game.service.pfs.webchanyou.HttpUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import com.thoughtworks.xstream.io.xml.XppDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WechatUtils {

    /**
     * 签名算法
     * @return 签名
     * @throws IllegalAccessException
     */
    public static String getSign(WechatRedPackRequest request) throws IllegalAccessException {
        ArrayList<String> list = new ArrayList<String>();
        Field[] fields = WechatRedPackRequest.class.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            if (f.get(request) != null && f.get(request) != "") {
                if(f.getName().equals("packageStr")){
                    list.add("package=" + f.get(request) + "&");
                }else{
                    list.add(f.getName() + "=" + f.get(request) + "&");
                }
            }
        }
        int size = list.size();
        String [] arrayToSort = list.toArray(new String[size]);
        Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < size; i ++) {
            sb.append(arrayToSort[i]);
        }
        String result = sb.toString();
        result += "key=" + WeixinRedbagConfig.getKey();
        System.out.println("Sign 生成前 MD5:" + result);
        result = MD5.MD5Encode(result).toUpperCase();
        System.out.println("Sign 结果:" + result);
        return result;
    }

    /**
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串，排除空值
     *
     * @param params 需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    public static String createLinkString(Map<String, String> params) {
        System.out.println(params.toString());
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        String prestr = "";

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }
        return prestr;
    }

    /*
     * 清除Map里的空值，以及由 BeanUtils.describe 产生的class键
     */
    public static void cleanMap(Map<String, String> map) {
        List<String> keys = new ArrayList<>();
        for (Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue() == null || entry.getValue().equals("")) {
                keys.add(entry.getKey());
            }
        }

        for (String key : keys) {
            map.remove(key);
        }

        if (map.keySet().contains("class")) {
            map.remove("class");
        }
    }

    /*
     * xml和对象的互相转换
     */
    public static <T> String convertObjectToXml(Object obj, Class<T> type) {
        XStream xstream = new XStream(new XppDriver(new XmlFriendlyNameCoder("__", "_")));
        xstream.alias("xml", type);
        String xml = xstream.toXML(obj);
        return xml;
    }

    public static WechatRedPackResponse convertXmlToResponse(String xml) {
        XStream xstream = new XStream();
        xstream.alias("xml", WechatRedPackResponse.class);
        WechatRedPackResponse response = (WechatRedPackResponse) xstream.fromXML(xml);
        return response;
    }

    /**
     * 获取access_token的请求地址（GET）
     */
    public final static String token_url = "https://api.weixin.qq.com/cgi-bin/token";

    /**
     * 获取access_token
     * @return accessToken（java对象）
     */
    public static String getNewAccessToken(){//String appid, String appsecret
        String requestUrl = token_url;
        HashMap<String, Object> params = new HashMap<>();
        params.put("grant_type", "client_credential");
        params.put("appid", WeixinRedbagConfig.getAppId());
        params.put("secret", WeixinRedbagConfig.getAppsecret());
        String jsonString = HttpUtil.doGet(requestUrl, params, null);
        String[] strTemp = jsonString.split("\":\"");
        String json = strTemp[1];
        strTemp = json.split("\",\"");
        return strTemp[0];
    }

    private static final Logger monitor = LoggerFactory.getLogger("MONITOR");

    /**
     * 获取用户基本信息
     * @param accessToken 借口访问凭证
     * @param openid 用户的OPENID
     * @return
     */
    public static WeixinUser getUserInfo(String accessToken, String openid) {
        WeixinUser weixinUser = null;
        String requestUrl = "https://api.weixin.qq.com/cgi-bin/user/info";
        HashMap<String, Object> params = new HashMap<>();
        params.put("access_token", accessToken);
        params.put("openid", openid);
        params.put("secret", WeixinRedbagConfig.getAppsecret());
        params.put("lang", "zh_CN");
        String respJSON = HttpUtil.doGet(requestUrl, params, null);
        JSONObject jsonObject = JSONObject.parseObject(respJSON);
        if (null != jsonObject) {
            try {
                weixinUser = new WeixinUser();
                weixinUser.setSubscribe(jsonObject.getIntValue("subscribe"));
                weixinUser.setOpenid(jsonObject.getString("openid"));
                weixinUser.setNickname(jsonObject.getString("nickname"));
                weixinUser.setSex(jsonObject.getIntValue("sex"));
                weixinUser.setLanguage(jsonObject.getString("language"));
                weixinUser.setCity(jsonObject.getString("city"));
                weixinUser.setProvince(jsonObject.getString("province"));
                weixinUser.setCountry(jsonObject.getString("country"));
                weixinUser.setHeadimgurl(jsonObject.getString("headimgurl"));
                weixinUser.setSubscribe_time(jsonObject.getIntValue("subscribe_time"));
				weixinUser.setUnionid(jsonObject.getString("unionid"));
                weixinUser.setRemark(jsonObject.getString("remark"));
                weixinUser.setGroupid(jsonObject.getIntValue("groupid"));
            } catch (Exception e) {
                if(0 == weixinUser.getSubscribe()){
                    monitor.error("用户" + weixinUser.getOpenid() + "已经取消关注了微信公众号");
                }else{
                    monitor.error("用户信息获取失败errcode:" + respJSON);
                }
            }
        } else {
            monitor.error("用户信息获取失败errcode:" + respJSON);
        }
        return weixinUser;
    }
}