package com.sy599.game.util;

import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.db.bean.MissionConfig;
import com.sy599.game.db.bean.ResourcesConfigs;
import com.sy599.game.db.bean.SevenSignConfig;
import com.sy599.game.db.dao.ResourcesConfigsDao;
import com.sy599.game.db.dao.TableCheckDao;
import com.sy599.game.db.enums.DbEnum;
import com.sy599.game.msg.serverPacket.ConfigMsg.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 资源配置工具类
 */
public final class ResourcesConfigsUtil {
    public static final String TYPE_SERVER_CONFIG = "ServerConfig";
    private static final Map<String, Map<String, Integer>> INT_VALUE_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, String>> STRING_VALUE_MAP = new ConcurrentHashMap<>();

    /** 是否保存userGroupPlayLog表**/
    public static final String KEY_SWITCH_SAVE_USER_GROUP_PLAYLOG = "switch_save_user_group_play_log";
    /** 是否分享到闲聊群助手**/
    public static final String KEY_SWITCH_SHARE_TO_XIANLIAO_GROUP= "switch_share_to_xianliao_group";

    /** 服务器是否停止托管：-1：关闭，0：全服，1,2,3：1服2服3服**/
    private static final String KEY_AUTO_PLAY_OFF_SERVERS = "auto_play_off_servers";

    /** 亲友圈房间限制：未开局、正在进行**/
    private static final String KEY_GROUP_TABLE_COUNT_LIMIT = "group_table_count_limit";
    /** 亲友圈牌桌列表发送最大牌桌数**/
    private static final String KEY_GROUP_TABLE_LIST_COUNT_LIMIT = "group_table_list_count_limit";
    /** 亲友圈牌桌列表缓存数据更新频率，单位:毫秒**/
    private static final String KEY_GROUP_TABLE_LIST_REFRESH_TIME = "group_table_list_refresh_time";

    /** 是否全服关闭托管功能**/
    private static boolean isAutoPlayOff = false;
    /**
     * 亲友圈预警分开关
     */
    public static final String KEY_GROUP_WARN_SWITCH = "group_warn_switch";

    /**
     * 开关：打开
     */
    public static final String SWITCH_ON = "1";
    /**
     * 开关：关闭
     */
    public static final String SWITCH_OFF = "0";

    /**
     * 初始化资源配置
     */
    public final static void initResourcesConfigs() {
        try {
            List<ResourcesConfigs> list = TableCheckDao.getInstance().checkTableExists(DbEnum.LOGIN,"t_resources_configs") ? ResourcesConfigsDao.getInstance().loadAllConfigs() : null;
            if (list != null) {
                boolean reloadGameConfig = false;
                for (ResourcesConfigs rc : list) {
                    if (NumberUtils.isDigits(rc.getMsgValue())) {
                        Map<String, Integer> map = INT_VALUE_MAP.get(rc.getMsgType());
                        try {
                            if (map == null) {
                                map = new ConcurrentHashMap<>();
                                map.put(rc.getMsgKey(), Integer.valueOf(rc.getMsgValue()));
                                INT_VALUE_MAP.put(rc.getMsgType(), map);
                            } else {
                                map.put(rc.getMsgKey(), Integer.valueOf(rc.getMsgValue()));
                            }
                        }catch (Exception e){
                            LogUtil.msgLog.warn("msgType={},msgValue={},convert integer fail : Exception:{}",rc.getMsgType(),rc.getMsgValue(),e.getMessage());
                        }
                    }

                    String pre;
                    Map<String, String> map = STRING_VALUE_MAP.get(rc.getMsgType());
                    if (map == null) {
                        map = new ConcurrentHashMap<>();
                        pre = map.put(rc.getMsgKey(), rc.getMsgValue());
                        STRING_VALUE_MAP.put(rc.getMsgType(), map);
                    } else {
                        pre = map.put(rc.getMsgKey(), rc.getMsgValue());
                    }

                    if (pre == null) {
                        LogUtil.msgLog.info("initResourcesConfigs:type={},key={},value={}", rc.getMsgType(), rc.getMsgKey(), rc.getMsgValue());
                    } else if (!pre.equals(rc.getMsgValue())) {
                        LogUtil.msgLog.info("reloadResourcesConfigs:type={},key={},value={}", rc.getMsgType(), rc.getMsgKey(), rc.getMsgValue());
                    }

                    if ("GameOrGoldConfig".equals(rc.getMsgType())){
                        reloadGameConfig = true;
                    }
                }

                if (reloadGameConfig){
                    GameConfigUtil.loadGameConfigsFromDb();
                }
            }
            ActivityUtil.init();
            MissionConfigUtil.init();
            TWinRewardUtil.init();
            initAutoPlayOff();
            if (list == null || list.size() == 0) {
                LogUtil.msgLog.info("initResourcesConfigs:size=0");
            } else {
                LogUtil.msgLog.info("initResourcesConfigs:size=" + list.size());
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 支付配置
     *
     * @param key
     * @return
     */
    public final static Integer loadPayPropertyValue(String key) {
        return loadIntegerValue("PayConfig", key);
    }

    /**
     * server配置
     *
     * @param key
     * @return
     */
    public final static String loadServerPropertyValue(String key) {
        return loadServerPropertyValue(key, null);
    }

    /**
     * server配置
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public final static String loadServerPropertyValue(String key, String defaultValue) {
        String val = loadStringValue("ServerConfig", key);
        if (val == null) {
            val = PropertiesCacheUtil.getValue(key, "config" + File.separator + "server.properties");
            return val.length() > 0 ? val : defaultValue;
        } else {
            return val;
        }
    }

    /**
     * 获取整型资源配置
     *
     * @param type
     * @param key
     * @return
     */
    public final static Integer loadIntegerValue(String type, String key) {
        Map<String, Integer> map = INT_VALUE_MAP.get(type);
        return map == null ? null : map.get(key);
    }

    /**
     * 获取整型资源配置
     *
     * @param type
     * @param key
     * @param defaultValue
     * @return
     */
    public final static Integer loadIntegerValue(String type, String key,int defaultValue) {
        Map<String, Integer> map = INT_VALUE_MAP.get(type);
        Integer value = map == null ? null : map.get(key);
        return value == null ? defaultValue : value;
    }

    /**
     * 获取整型资源配置
     *
     * @param type
     * @return
     */
    public final static Map<String, Integer> loadIntegerValues(String type) {
        return INT_VALUE_MAP.get(type);
    }

    /**
     * 获取字符串资源配置（包括整型资源）
     *
     * @param type
     * @param key
     * @return
     */
    public final static String loadStringValue(String type, String key) {
        Map<String, String> map = STRING_VALUE_MAP.get(type);
        return map == null ? null : map.get(key);
    }

    /**
     * 获取整型资源配置
     *
     * @param type
     * @param key
     * @param defaultValue
     * @return
     */
    public final static String loadStringValue(String type, String key,String defaultValue) {
        Map<String, String> map = STRING_VALUE_MAP.get(type);
        String value = map == null ? null : map.get(key);
        return value == null ? defaultValue : value;
    }

    /**
     * 获取字符串资源配置（包括整型资源）
     *
     * @param type
     * @return
     */
    public final static Map<String, String> loadStringValues(String type) {
        return STRING_VALUE_MAP.get(type);
    }


    public final static Integer loadServerConfigIntegerValue(String key, int defaultValue) {
        Map<String, Integer> map = INT_VALUE_MAP.get(TYPE_SERVER_CONFIG);
        Integer value = map == null ? null : map.get(key);
        return value == null ? defaultValue : value;
    }

    /**
     * ServerConfig 配置
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public final static String loadServerConfigValue(String key, String defaultValue) {
        String val = loadStringValue(TYPE_SERVER_CONFIG, key);
        if (val != null) {
            return val;
        }
        return defaultValue;
    }

    /**
     * ServerConfig 配置
     *
     * @param key
     * @return
     */
    public final static String loadServerConfigValue(String key) {
        return loadServerConfigValue(key,null);
    }

    /**
     * 开关类配置，是否打开
     * 读取type=ServerConfig的配置
     * 配置值：0关闭，1打开
     * @param key
     * @return
     */
    public final static boolean isSwitchOn(String key){
        return SWITCH_ON.equals(loadServerConfigValue(key));
    }


    /**
     * 开关类配置，是否打开
     * 读取type=ServerConfig的配置
     * 配置值：0关闭，1打开
     *
     * @param key
     * @param def :未读到配置数据时，返回def
     * @return
     */
    public final static boolean isSwitchOn(String key, Boolean def) {
        String data = loadServerConfigValue(key);
        if (StringUtils.isBlank(data)) {
            return def;
        }
        return SWITCH_ON.equals(loadServerConfigValue(key));
    }

    /**
     * 刷新缓存的数据
     *
     * @param msgType
     * @param msgKey
     */
    public static void refreshIntegerData(String msgType, String msgKey) {
        if (StringUtils.isBlank(msgType) || StringUtils.isBlank(msgKey)) {
            return;
        }
        try {
            ResourcesConfigs data = ResourcesConfigsDao.getInstance().loadOneConfig(msgType, msgKey);
            if (data == null) {
                return;
            }
            if (NumberUtils.isDigits(data.getMsgValue())) {
                Map<String, Integer> intMap = INT_VALUE_MAP.get(data.getMsgType());
                if (intMap == null) {
                    intMap = new HashMap<>();
                    INT_VALUE_MAP.put(data.getMsgType(), intMap);
                }
                intMap.put(data.getMsgKey(), Integer.valueOf(data.getMsgValue()));
            }

            Map<String, String> strMap = STRING_VALUE_MAP.get(data.getMsgType());
            if (strMap == null) {
                strMap = new HashMap<>();
                STRING_VALUE_MAP.put(data.getMsgType(), strMap);
            }
            strMap.put(data.getMsgKey(), data.getMsgValue());

        } catch (Exception e) {
            LogUtil.errorLog.error("refreshIntegerData|error|" + msgType + "|" + msgKey, e);
        }
    }

    /**
     * 10秒种刷新一次
     */
    public static void refreshDataTenSecond() {
        refreshIntegerData(TYPE_SERVER_CONFIG, KEY_AUTO_PLAY_OFF_SERVERS);
        initAutoPlayOff();
    }

    /**
     * 初始化关闭托管功能的服务器
     */
    public static void initAutoPlayOff() {
        try {
            String servers = loadServerConfigValue(KEY_AUTO_PLAY_OFF_SERVERS, "-1");
            boolean tmp;
            if ("-1".equals(servers)) {
                tmp = false;
            } else if ("0".equals(servers)) {
                tmp = true;
            } else {
                String[] splits = servers.split(",");
                Set<Integer> set = new HashSet<>();
                for (String s : splits) {
                    set.add(Integer.valueOf(s));
                }
                tmp = set.contains(GameServerConfig.SERVER_ID);
            }
            if (isAutoPlayOff != tmp) {
                LogUtil.msgLog.info("ResourcesConfigsUtil|isAutoPlayOff|" + GameServerConfig.SERVER_ID + "|" + tmp + "|" + servers);
                isAutoPlayOff = tmp;
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("ResourcesConfigsUtil|refreshAutoPlayOffServers|error|" + e.getMessage(), e);
        }
    }

    /**
     * 是否关闭托管功能
     *
     * @return
     */
    public static boolean isAutoPlayOff() {
        return isAutoPlayOff;
    }

    /**
     * 亲友圈房间限制：未开局、正在进行
     * 不配置时默认300
     * @return
     */
    public static int getGroupTableCountLimit() {
        return ResourcesConfigsUtil.loadServerConfigIntegerValue(KEY_GROUP_TABLE_COUNT_LIMIT, 300);
    }

    /**
     * 亲友圈牌桌列表：发送给客户关最大数量限制
     * 不配置时默认100
     * @return
     */
    public static int getGroupTableListCountLimit() {
        return ResourcesConfigsUtil.loadServerConfigIntegerValue(KEY_GROUP_TABLE_LIST_COUNT_LIMIT, 100);
    }

    /**
     * 亲友圈牌桌列表缓存数据更新频率，单位:毫秒
     * 不配置时，默认3000
     * @return
     */
    public static int getGroupTableListRefreshTime() {
        return ResourcesConfigsUtil.loadServerConfigIntegerValue(KEY_GROUP_TABLE_LIST_REFRESH_TIME, 3000);
    }


    public static Map<String, Map<String, String>> getStringValueMap() {
        return STRING_VALUE_MAP;
    }

    public static Map<String,Object> getGoldRoomActivityConfig(String cur_configId){
        Map<String,Object> returnMap = new HashMap<>();
        try {
            Map<String, String> config = ResourcesConfigsUtil.getStringValueMap().get("GoldRoomGiftCertActivityConfig");
            int bean = 0;
            int rule = 0;
            int limitnum =0;
            String roomlv ="";
            returnMap.put("bean",bean);
            returnMap.put("rule",rule);
            returnMap.put("limitnum",limitnum);
            returnMap.put("roomlv",roomlv);
            if(null == config){
                return returnMap;
            }
            for(Map.Entry<String, String> entry:config.entrySet()){
            System.out.println(entry.getKey()+"--->"+entry.getValue());
            String configDetail = entry.getValue().split(";")[0];
            String gameKeyId =  entry.getValue().split(";")[1];

            String configDetail_activityIsOpen = configDetail.split(",")[0];//活动是否开启了
            if(!configDetail_activityIsOpen.equals("1")){
                continue;
            }
            String configDetail_activityRewardBean = configDetail.split(",")[1];//活动奖励豆
            String configDetail_activityRule = configDetail.split(",")[2];//活动规则
            String configDetail_activityItemLimit = configDetail.split(",")[3];//活动 每日数量获取限制
            String[] keyIds = gameKeyId.split(",");
            for (String key:keyIds){
                if(key.equals(cur_configId)){
                    bean = Integer.valueOf(configDetail_activityRewardBean);
                    rule = Integer.valueOf(configDetail_activityRule);
                    limitnum = Integer.valueOf(configDetail_activityItemLimit);
                    roomlv = String.valueOf(entry.getKey());
                    break;
                }
            }
            if(bean!=0 && rule!=0){
                returnMap.put("bean",bean);
                returnMap.put("rule",rule);
                returnMap.put("limitnum",limitnum);
                returnMap.put("roomlv",roomlv);
                break;
            }
        }
        }catch (Exception e){
            LogUtil.errorLog.error("initGoldRoomGiftCertActivityConfig|error|" , e);
        }
        return returnMap;
    }

    /**
     * 解析七夕活动配置
     * @param cur_configId 当前金币场玩法ID
     * @return
     */
    public static Map<String,Object> getGoldRoom7xiActivityConfig(String cur_configId){
        Map<String,Object> returnMap = new HashMap<>();
        try {
            Map<String, String> config = ResourcesConfigsUtil.getStringValueMap().get("GoldRoom7xiActivityConfig");
            int rate = 0;
            String roomlv ="";
            returnMap.put("rate",rate);
            returnMap.put("roomlv",roomlv);
            if(null == config){
                return returnMap;
            }
            for(Map.Entry<String, String> entry:config.entrySet()){
                System.out.println(entry.getKey()+"--->"+entry.getValue());
                String configDetail = entry.getValue().split(";")[0];
                String gameKeyId =  entry.getValue().split(";")[1];
                String configDetail_activityIsOpen = configDetail.split(",")[0];//活动是否开启了
                if(!configDetail_activityIsOpen.equals("1")){
                    continue;
                }
                String configDetail_activityRewardBean = configDetail.split(",")[1];//活动奖励倍率
                String[] keyIds = gameKeyId.split(",");
                for (String key:keyIds){
                    if(key.equals(cur_configId)){
                        rate = Integer.valueOf(configDetail_activityRewardBean);
                        roomlv = String.valueOf(entry.getKey());
                        break;
                    }
                }
                if(rate!=0  ){
                    returnMap.put("rate",rate);
                    returnMap.put("roomlv",roomlv);
                    break;
                }
            }
        }catch (Exception e){
            LogUtil.errorLog.error("getGoldRoom7xiActivityConfig|error|" , e);
        }
        return returnMap;
    }
}
