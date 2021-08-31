package com.sy.sanguo.game.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.HttpUtil;
import com.sy.sanguo.common.util.MD5Util;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy599.sanguo.util.ResourcesConfigsUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BjdUtil {

    public static final String plat = "mjqz";
    public static final String sign_key = "0NUs3u0qpsfrB4k9";
    public static final String sign_key_new = "qZzWngop3t8OswG0";
    public static long sign_key_time_out = 0L;

    public static void init() throws Exception {
        sign_key_time_out = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2021-01-25 11:00:00").getTime();
    }

    public static boolean useNewSignKey() {
        return System.currentTimeMillis() > sign_key_time_out;
    }
    /**
     * 签名验证
     *
     * @param params
     * @return
     */
    public static boolean checkSign(Map<String, String> params) {
        String sign = params.remove("sign");
        String time = params.get("time");
        if (StringUtils.isBlank(sign) || !NumberUtils.isDigits(time)) {
            return false;
        }
        String[] keys = params.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            sb.append("&").append(key).append("=").append(params.get(key));
        }
        String s1 = sb.toString() + "&key=" + sign_key;
        String s2 = sb.toString() + "&key=" + sign_key_new;
        if (useNewSignKey()) {
            return sign.equalsIgnoreCase(MD5Util.getStringMD5(s2, "utf-8"));
        } else {
            return sign.equalsIgnoreCase(MD5Util.getStringMD5(s1, "utf-8"))
                    || sign.equalsIgnoreCase(MD5Util.getStringMD5(s2, "utf-8"));
        }
    }

    /**
     * 获取用户绑定的代理邀请码
     *
     * @return
     */
    public static int getBindAgency(RegInfo user) {
        Map<String, String> map = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        String account;
        String reg_type;
        if (StringUtils.isNotBlank(user.getIdentity())) {
            reg_type = "0";
            account = user.getIdentity();
        } else {
            reg_type = "1";
            account = user.getUserId() + "";
        }
        String sign = MD5Util.getStringMD5(account + time, "utf-8");
        try {
            account = URLEncoder.encode(account, "utf-8");
            map.put("account", account);
            map.put("time", time);
            map.put("wx_plat", plat);
            map.put("reg_type", reg_type);
            map.put("sign", sign);
            String url_checkBind = ResourcesConfigsUtil.loadServerPropertyValue("bjdAdmin_checkBind", "http://bjdqp.firstmjq.club/agent/player/checkBind");
            HttpUtil httpUtil = new HttpUtil(url_checkBind);
            String postRes = httpUtil.post(map);
            GameBackLogger.SYS_LOG.info("getBindAgency|" + account + "|" + time + "|" + sign + "|" + postRes);
            if (StringUtils.isBlank(postRes)) {
                return 0;
            }
            JSONObject obj = JSON.parseObject(postRes);
            if (obj == null) {
                return 0;
            }
            if (obj.getIntValue("code") != 1) {
                // 未绑定
                return 0;
            }
            JSONObject data = obj.getJSONObject("data");
            if (data == null) {
                return 0;
            }
            String parentId = data.getString("parent_id");
            if (StringUtils.isBlank(parentId) || "null".equals(parentId)) {
                return 0;
            }
            return Integer.valueOf(parentId);
        } catch (Exception e) {
            GameBackLogger.SYS_LOG.error("getBindAgency|error|" + account + "|" + time + "|" + sign, e);
        }
        return 0;
    }

    /**
     * 为用户绑定代理邀请码
     *
     * @param user
     * @param agencyId
     * @return
     */
    public static String bindAgencyId(RegInfo user, int agencyId) {
        Map<String, String> map = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        String account;
        String reg_type;
        if (StringUtils.isNotBlank(user.getIdentity())) {
            reg_type = "0";
            account = user.getIdentity();
        } else {
            reg_type = "1";
            account = user.getUserId() + "";
        }
        String sign = MD5Util.getStringMD5(account + "" + agencyId + "" + time, "utf-8");
        String defaltRes = "绑定" + agencyId + "的代理商失败，请联系管理员!";
        try {
            account = URLEncoder.encode(account, "utf-8");
            map.put("account", account);
            map.put("invite_code", String.valueOf(agencyId));
            map.put("wx_plat", plat);
            map.put("time", time);
            map.put("reg_type", reg_type);
            map.put("sign", sign);
            String url_bind = ResourcesConfigsUtil.loadServerPropertyValue("bjdAdmin_bind", "http://bjdqp.firstmjq.club/agent/player/bind");
            HttpUtil httpUtil = new HttpUtil(url_bind);
            String postRes = httpUtil.post(map);
            GameBackLogger.SYS_LOG.info("bindAgencyId|" + account + "|" + time + "|" + sign + "|" + postRes);
            if (StringUtils.isBlank(postRes)) {
                return defaltRes;
            }
            JSONObject obj = JSON.parseObject(postRes);
            if (obj == null) {
                return defaltRes;
            }
            if (obj.getIntValue("code") != 0) {
                // 绑定失败
                String msg = obj.getString("msg");
                if (StringUtils.isNotBlank(msg)) {
                    return msg;
                } else {
                    return defaltRes;
                }
            }
            return "";
        } catch (Exception e) {
            GameBackLogger.SYS_LOG.error("bindAgencyId|error|" + account + "|" + time + "|" + sign, e);
        }
        return defaltRes;
    }

    /**
     * 获取用户预绑定的代理邀请码
     *
     * @return
     */
    public static int getPreBindAgency(RegInfo user) {
        Map<String, String> map = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        String account;
        String reg_type;
        if (StringUtils.isNotBlank(user.getIdentity())) {
            reg_type = "0";
            account = user.getIdentity();
        } else {
            reg_type = "1";
            account = user.getUserId() + "";
        }
        String sign = MD5Util.getStringMD5(account + time, "utf-8");
        try {
            account = URLEncoder.encode(account, "utf-8");
            map.put("account", account);
            map.put("time", time);
            map.put("wx_plat", plat);
            map.put("reg_type", reg_type);
            map.put("sign", sign);
            String url_checkBind = ResourcesConfigsUtil.loadServerPropertyValue("bjdAdmin_checkBind", "http://bjdqp.firstmjq.club/agent/player/checkBind");
            HttpUtil httpUtil = new HttpUtil(url_checkBind);
            String postRes = httpUtil.post(map);
            GameBackLogger.SYS_LOG.info("getPreBindAgency|" + account + "|" + time + "|" + sign + "|" + postRes);
            if (StringUtils.isBlank(postRes)) {
                return 0;
            }
            JSONObject obj = JSON.parseObject(postRes);
            if (obj == null) {
                return 0;
            }
            if (obj.getIntValue("code") != 2) {
                // 未绑定
                return 0;
            }
            JSONObject data = obj.getJSONObject("data");
            if (data == null) {
                return 0;
            }
            String parentId = data.getString("parent_id");
            if (StringUtils.isBlank(parentId) || "null".equals(parentId)) {
                return 0;
            }
            return Integer.valueOf(parentId);
        } catch (Exception e) {
            GameBackLogger.SYS_LOG.error("getPreBindAgency|error|" + account + "|" + time + "|" + sign, e);
        }
        return 0;
    }


    /**
     * 转移俱乐部
     *
     * @return
     */
    public static String transferGroup(long fromUserId, long toUserId, long groupId) {
        Map<String, String> map = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        String sign = MD5Util.getStringMD5(fromUserId + "" + groupId + "" + toUserId + "" + time, "utf-8");
        String defaltRes = "转移俱乐部" + groupId + "给用户" + toUserId + "失败，请联系管理员!";
        try {
            map.put("from_char_id", String.valueOf(fromUserId));
            map.put("to_char_id", String.valueOf(toUserId));
            map.put("group_id", String.valueOf(groupId));
            map.put("time", time);
            map.put("sign", sign);
            String url_transfer_group = ResourcesConfigsUtil.loadServerPropertyValue("bjdAdmin_transferGroup", "http://bjdqp.firstmjq.club/agent/player/changeGroupMaster/wx_plat/mjqz");
            HttpUtil httpUtil = new HttpUtil(url_transfer_group);
            String postRes = httpUtil.post(map);
            GameBackLogger.SYS_LOG.info("transferGroup|" + fromUserId + "|" + toUserId + "|" + groupId + "|" + time + "|" + sign + "|" + postRes);
            if (StringUtils.isBlank(postRes)) {
                return defaltRes;
            }
            JSONObject obj = JSON.parseObject(postRes);
            if (obj == null) {
                return defaltRes;
            }
            if (obj.getIntValue("code") != 0) {
                // 转移失败
                String msg = obj.getString("msg");
                if (StringUtils.isNotBlank(msg)) {
                    return msg;
                } else {
                    return defaltRes;
                }
            }
            return "";
        } catch (Exception e) {
            GameBackLogger.SYS_LOG.error("transferGroup|error|" + fromUserId + "|" + toUserId + "|" + groupId + "|" + time + "|" + sign, e);
        }
        return defaltRes;
    }

    /**
     * 创建俱乐部后，通知代理后台
     *
     * @return
     */
    public static String notifyCreateGroup(long userId, long groupId) {
        Map<String, String> map = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        String sign = MD5Util.getStringMD5(userId + "" + groupId + "" + time, "utf-8");
        String defaltRes = "创建俱乐部成功后，通知代理后台成功!";
        try {
            map.put("char_id", String.valueOf(userId));
            map.put("club_id", String.valueOf(groupId));
            map.put("time", time);
            map.put("sign", sign);
            String url_notify_create_group = ResourcesConfigsUtil.loadServerPropertyValue("bjdAdmin_notifyCreateGroup", "http://bjdqp.firstmjq.club/agentZp/player/openClub/wx_plat/zpq");
            HttpUtil httpUtil = new HttpUtil(url_notify_create_group);
            String postRes = httpUtil.post(map);
            GameBackLogger.SYS_LOG.info("notifyCreateGroup|" + userId + "|" + groupId + "|" + time + "|" + sign + "|" + postRes);
            if (StringUtils.isBlank(postRes)) {
                return defaltRes;
            }
            JSONObject obj = JSON.parseObject(postRes);
            if (obj == null) {
                return defaltRes;
            }
            if (obj.getIntValue("code") != 0) {
                // 转移失败
                String msg = obj.getString("msg");
                if (StringUtils.isNotBlank(msg)) {
                    return msg;
                } else {
                    return defaltRes;
                }
            }
            return "";
        } catch (Exception e) {
            GameBackLogger.SYS_LOG.error("notifyCreateGroup|error|" + userId + "|" + groupId + "|" + time + "|" + sign, e);
        }
        return defaltRes;
    }

    /**
     * 用户举报
     *
     * @return
     */
    public static String userReport(long userId, String email, String content) {
        String defaltRes = "举报失败，请联系系统管理员！";
        try {

            Map<String, String> map = new HashMap<>();
            map.put("char_id", String.valueOf(userId));
            map.put("rand", rand6());
            map.put("t", String.valueOf(System.currentTimeMillis()).substring(0,10));
            map.put("sign", genSign(map));
            map.put("email", email);
            map.put("content", content);
            String url_user_report = ResourcesConfigsUtil.loadServerPropertyValue("bjdAdmin_userReport", "http://bjdqp.firstmjq.club/Agent/player/userReport/wx_plat/mjqz");
            HttpUtil httpUtil = new HttpUtil(url_user_report);
            String postRes = httpUtil.post(map);
            GameBackLogger.SYS_LOG.info("userReport|" + userId + "|" + email + "|" + content + "|" + postRes + "|" + map);
            if (StringUtils.isBlank(postRes)) {
                return defaltRes;
            }
            JSONObject obj = JSON.parseObject(postRes);
            if (obj == null) {
                return defaltRes;
            }
            if (obj.getIntValue("code") != 0) {
                String msg = obj.getString("msg");
                if (StringUtils.isNotBlank(msg)) {
                    return msg;
                } else {
                    return defaltRes;
                }
            }
            return "";
        } catch (Exception e) {
            GameBackLogger.SYS_LOG.error("userReport|error|" + userId + "|" + email + "|" + content, e);
        }
        return defaltRes;
    }

    /**
     * 6位随机数
     *
     * @return
     */
    public static String rand6() {
        return String.valueOf(100000 + new Random().nextInt(100000));
    }

    /**
     * 签名
     *
     * @param map
     * @return
     */
    protected static String genSign(Map<String, String> map) {
        String[] keySet = map.keySet().toArray(new String[0]);
        Arrays.sort(keySet);
        StringBuilder sb = new StringBuilder();
        for (String key : keySet) {
            sb.append("&").append(key).append("=").append(map.get(key));
        }
//        sb.append("&key=").append("dfc2c2d62dde2c104203cf71c6e15580");
        sb.append("dfc2c2d62dde2c104203cf71c6e15580");
        return StringUtils.upperCase(MD5Util.getStringMD5(sb.toString().substring(1), "utf-8"));
    }


}
