package com.sy.sanguo.game.utils;

import com.alibaba.fastjson.TypeReference;
import com.sy.sanguo.common.util.JacksonUtil;
import com.sy.sanguo.common.util.MathUtil;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.Server;
import com.sy.sanguo.game.bean.User;
import com.sy599.game.util.MD5Util;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LoginUtil {

    public static final String pf_phoneNum = "phoneLogin";

    private static String md5_key_phoneNum = "sanguo_shangyou_2013";

    private static String aes_key_phoneNum = "bjdlimsam2019%@)";


    public static void setMd5KeyPhoneNum(String md5Key) {
        if (StringUtils.isNotBlank(md5Key)) {
            md5_key_phoneNum = md5Key;
        }
    }

    public static void setAESKeyPhoneNum(String aesKey) {
        if (StringUtils.isNotBlank(aesKey) && aesKey.length() == 16) {
            aes_key_phoneNum = aesKey;
        }
    }

    /**
     * 加密手机号
     *
     * @param phoneNum
     * @return
     */
    public static String encryptPhoneNumMD5(String phoneNum) {
        return MD5Util.getStringMD5(phoneNum + md5_key_phoneNum, "utf-8");
    }

    /**
     * AES加密手机号
     *
     * @param phoneNum
     * @return
     */
    public static String encryptPhoneNumAES(String phoneNum) {
        return AESUtil.encrypt(phoneNum, aes_key_phoneNum);
    }


    /**
     * AES 解密手机号
     *
     * @param phoneNum
     * @return
     */
    public static String decryptPhoneNumAES(String phoneNum) {
        return AESUtil.decrypt(phoneNum, aes_key_phoneNum);
    }

    /**
     * AES 解密手机号
     *
     * @param phoneNum
     * @return
     */
    public static String decryptPhoneNumAES(String phoneNum, String key) {
        return AESUtil.decrypt(phoneNum, key);
    }


    /**
     * 加密密码
     *
     * @param source
     * @return
     */
    public static String genPw(String source) {
        return com.sy.sanguo.common.util.MD5Util.getStringMD5(source + "sanguo_shangyou_2013");
    }

    /**
     * 生成session code
     *
     * @param username
     * @return
     */
    public static String genSessCode(String username) {
        StringBuilder sb = new StringBuilder();
        sb.append(username);
        sb.append(MathUtil.mt_rand(10000, 99999));
        sb.append(System.currentTimeMillis());
        return com.sy.sanguo.common.util.MD5Util.getStringMD5(sb.toString());
    }

    /**
     * 构建User对象
     *
     * @param userInfo
     * @return
     */
    public static User buildUser(RegInfo userInfo, List<Server> serverList) {
        User user = new User();
        user.setUsername(userInfo.getFlatId());
        user.setPf(userInfo.getPf());
        if (!StringUtils.isBlank(userInfo.getPlayedSid())) {
            if (serverList != null&&serverList.size()>0) {
                // 服务器列表
                List<Integer> servers = JacksonUtil.readValue(userInfo.getPlayedSid(), new TypeReference<List<Integer>>() {
                });
                // 服务器ID列表
                List<Integer> serverIdList = new ArrayList<Integer>();
                for (Server server : serverList) {
                    serverIdList.add(server.getId());
                }
                // 不在服务器列表内
                Iterator<Integer> iterator = servers.iterator();
                while (iterator.hasNext()) {
                    int serverId = iterator.next();
                    if (!serverIdList.contains(serverId)) {
                        iterator.remove();
                    }
                }
                user.setPlayedSid(JacksonUtil.writeValueAsString(servers));
            } else {
                user.setPlayedSid(userInfo.getPlayedSid());
            }

        } else {
            user.setPlayedSid("[]");
        }
        // servers = JacksonUtil.readValue(userInfo.getPlayedSid(), new
        // TypeReference<List<Integer>>() {
        // });
        user.setSessCode(userInfo.getSessCode());
        return user;
    }


    public static void main(String[] args) {
        String phoneNum = "15575222762";
        System.out.println(encryptPhoneNumMD5(phoneNum));


        String encrypt = encryptPhoneNumAES(phoneNum);
        System.out.println("AES|encrypt|" + phoneNum + "|" + encrypt + "|");

        encrypt = "j74yW+xes783u03vNSGCJQ==";
        String decrypt = decryptPhoneNumAES(encrypt);
        System.out.println("AES|decrypt|" + encrypt + "|" + decrypt + "|");

    }


}
