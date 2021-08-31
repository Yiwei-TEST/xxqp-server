package com.sy.sanguo.game.redpack;

import com.sy.sanguo.common.init.InitData;

/**
 * 微信红包相关参数配置
 */
public class WeixinRedbagConfig {

    /*
     * 微信支付分配的商户号
     */
    public static String getMchId() {
        return (String) InitData.redbag_properties.get("MCH_ID");
    }

    /*
     * 商户appid
     */
    public static String getAppId() {
        return (String) InitData.redbag_properties.get("APP_ID");
    }

    /*
     * API密钥 用于签名
     */
    public static String getKey() {
        return (String) InitData.redbag_properties.get("KEY");
    }

    /**
     * 商户密钥  用于调用微信接口
     */
    public static String getAppsecret() {
        return (String) InitData.redbag_properties.get("appsecret");
    }

    /*
     * 微信红包的API地址
     */
    public static String getApiUrl() {
        return (String) InitData.redbag_properties.get("API_URL");
    }

    /*
     * 微信p12文件存放路径
     */
    public static String getP12KeyPath() {
        return (String) InitData.redbag_properties.get("p12KeyPath");
    }

    /**
     * 参与游戏名
     */
    public static String getGameName() {return (String) InitData.redbag_properties.get("gameName"); }

    /**
     * 红包祝福语
     */
    public static String getWishContent() {return (String) InitData.redbag_properties.get("wishContent"); }

    /**
     * 活动名
     */
    public static String getActivityName() {return (String) InitData.redbag_properties.get("activityName"); }

    /**
     * 公众号名
     */
    public static String getAppName() {return (String) InitData.redbag_properties.get("appName"); }

    /**
     * 现金红包领取开始时间
     */
    public static String getStartTime() {return (String) InitData.redbag_properties.get("startTime"); }

    /**
     * 现金红包领取结束时间
     */
    public static String getEndTime() {return (String) InitData.redbag_properties.get("endTime"); }

    /**
     * 现金红包领取口令
     */
    public static String getToken() {return (String) InitData.redbag_properties.get("token"); }

    /**
     * 现金红包每日系统最高领取额
     */
    public static int getRedbagMaxNum() {return Integer.parseInt((String)InitData.redbag_properties.get("redbagMaxNum")); }
}