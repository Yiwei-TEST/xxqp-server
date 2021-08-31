package com.sy599.game.util;

import com.sy599.game.common.UserResourceType;
import com.sy599.game.util.helper.ResourceHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏付费配置
 */
public final class PayConfigUtil extends ResourceHandler {

    /*** 前端支付类型：AA支付***/
    public static final int PayType_Client_AA = 1;
    /*** 前端支付类型：房主支付***/
    public static final int PayType_Client_TableMaster = 2;
    /*** 前端支付类型：群主支付***/
    public static final int PayType_Client_GroupMaster = 3;
    /*** 前端支付类型：金币AA支付***/
    public static final int PayType_Client_AA_Gold = 4;


    /*** 后端支付类型：AA支付***/
    public static final int PayType_Server_AA = 0;
    /*** 后端支付类型：房主支付***/
    public static final int PayType_Server_TableMaster = 1;
    /*** 后端支付类型：群主支付***/
    public static final int PayType_Server_GroupMaster = 3;
    /*** 后端支付类型：金币AA支付***/
    public static final int PayType_Server_AA_Gold = 4;



    private final static Map<String, String> cacheMap = new ConcurrentHashMap<>();

    public static void load(Map map) {
        cacheMap.putAll(map);
    }

    /**
     * 配置文件重新加载
     *
     * @param resourceFileName
     */
    @Override
    public void reload(String resourceFileName) {
        load(loadFromFile(resourceFileName));
    }

    /**
     * 获取游戏付费配置
     *
     * @param playType       游戏玩法
     * @param totalCount     总局数
     * @param maxPlayerCount 最大人数
     * @param payType        付费方式(0AA支付，1房主支付)
     * @return
     */
    public static int get(int playType, int totalCount, int maxPlayerCount, int payType) {
        return get(playType, totalCount, maxPlayerCount, payType, null);
    }

    /**
     * 获取游戏付费配置
     *
     * @param playType       游戏玩法
     * @param totalCount     总局数
     * @param maxPlayerCount 最大人数
     * @param payType        付费方式(0AA支付，1房主支付)
     * @param ext            附加信息
     * @return
     */
    public static int get(int playType, int totalCount, int maxPlayerCount, int payType, Object ext) {

        if (GameConfigUtil.freeGame(playType, -1)) {
            return 0;
        }

        StringBuilder sb = new StringBuilder(36);
        sb.append("pay_type").append(playType);
        sb.append("_count").append(totalCount);
        sb.append("_player").append(maxPlayerCount);
        sb.append("_pay").append(payType);
        if (ext != null && ext.toString().length() > 0) {
            sb.append("_").append(ext);
        }
        String key = sb.toString();
        Integer temp0 = ResourcesConfigsUtil.loadPayPropertyValue(key);
        if (temp0 != null) {
            LogUtil.msgLog.debug("pay config is found:key=" + key + ",value=" + temp0);
            return temp0.intValue();
        } else {
            String temp = cacheMap.get(key);
            if (NumberUtils.isDigits(temp)) {
                LogUtil.msgLog.debug("pay config is found:key=" + key + ",value=" + temp);
                return Integer.parseInt(temp);
            } else {
                LogUtil.errorLog.warn("pay config is not found or config error:key=" + key + ",value=" + temp);
                return -1;
            }
        }
    }

    /**
     * 好友房支付的资源类型<br/>
     * 默认为房卡/钻石
     *
     * @return
     */
    public static UserResourceType loadPayResourceType(int playType) {
        String type = ResourcesConfigsUtil.loadServerPropertyValue(new StringBuilder(22).append("pay_resource_type").append(playType).toString());
        if (StringUtils.isBlank(type)) {
            return UserResourceType.CARD;
        } else {
            return UserResourceType.valueOf(type);
        }
    }
}
