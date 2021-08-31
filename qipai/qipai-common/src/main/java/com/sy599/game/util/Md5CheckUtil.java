
package com.sy599.game.util;

import com.sy599.game.common.constant.KeyConstants;

import javax.servlet.http.HttpServletRequest;

public class Md5CheckUtil {
    private Md5CheckUtil() {
    }

    public static boolean checkLoginMd5(String userId, String t, String s) {
        return MD5Util.getMD5String(userId + t + KeyConstants.md5_login).equals(s);
    }

    public static boolean checkPayMd5(String time, String flatId, String signal) {
        return MD5Util.getMD5String(time + KeyConstants.md5_pay + flatId).equals(signal);
    }

    public static boolean checkConsumeMd5(String time, String flatId, String signal) {
        return MD5Util.getMD5String(time + KeyConstants.md5_consume + flatId).equals(signal);
    }

    public static boolean checkHttpMd5(HttpServletRequest request) {
        String sytime = request.getParameter("sytime");
        String sign = request.getParameter("sysign");
        String md5 = MD5Util.getMD5String(sytime + KeyConstants.md5_http);
        return md5.equals(sign);
    }

    public static boolean checkHttpMd5(String str, String sign) {
        String md5 = MD5Util.getMD5String(str + KeyConstants.md5_loginserver);
        return md5.equals(sign);
    }
}
