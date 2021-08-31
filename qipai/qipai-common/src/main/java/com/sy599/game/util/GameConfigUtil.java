package com.sy599.game.util;

import com.sy599.game.db.dao.UserShareDao;
import com.sy599.game.util.gold.constants.GoldConstans;
import com.sy599.game.util.helper.ResourceHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏基本参数配置
 */
public final class GameConfigUtil extends ResourceHandler {
    private static Map<String, List<Integer>> cacheIntsMap = new ConcurrentHashMap<>();
    private static Map<String, List<String>> cacheStringsMap = new ConcurrentHashMap<>();
    private static Map<String, String> cacheStringLi = new ConcurrentHashMap<>();
    private static Map<String, Integer> cacheGoldRatioMap = new ConcurrentHashMap<>();
    private static Map<Integer, String> GAME_MAP = new ConcurrentHashMap<>();
    private static Map<Integer, String> SHARE_FREE_GAME_MAP = new ConcurrentHashMap<>();
    public static int groupToQinYouQuan = 0;

    static {
        loadOpenGameTypes();
        loadShareFreeGames();
    }

    /**
     * 加载分享免费的游戏
     */
    public static void loadShareFreeGames() {
        Map<Integer, String> map = new ConcurrentHashMap<>();
        String gameStrs = ResourcesConfigsUtil.loadServerPropertyValue("share_free_games");
        if (StringUtils.isNotBlank(gameStrs)) {
            String[] games = gameStrs.split(",");
            for (String str : games) {
                if (StringUtils.isNotBlank(str)) {
                    int idx = str.indexOf("_");
                    if (idx != -1) {
                        int start = Integer.parseInt(str.substring(0, idx));
                        int end = Integer.parseInt(str.substring(idx + 1));
                        for (int i = start; i <= end; i++) {
                            map.put(i, String.valueOf(i));
                        }
                    } else {
                        map.put(Integer.valueOf(str), str);
                    }
                }
            }
        }
        SHARE_FREE_GAME_MAP = map;
    }

    /**
     * 加载支持的游戏玩法
     */
    public static void loadOpenGameTypes() {
        Map<Integer, String> map = new ConcurrentHashMap<>();
        String gameStrs = ResourcesConfigsUtil.loadServerPropertyValue("openGameTypes");
        if (StringUtils.isNotBlank(gameStrs)) {
            String[] games = gameStrs.split(",");
            for (String str : games) {
                if (StringUtils.isNotBlank(str)) {
                    int idx = str.indexOf("_");
                    if (idx != -1) {
                        int start = Integer.parseInt(str.substring(0, idx));
                        int end = Integer.parseInt(str.substring(idx + 1));
                        for (int i = start; i <= end; i++) {
                            map.put(i, String.valueOf(i));
                        }
                    } else {
                        map.put(Integer.valueOf(str), str);
                    }
                }
            }
        }

        GAME_MAP = map;
    }

    /**
     * 检查是否开启免费玩游戏<br/>
     * 如果server.properties中没配置share_free_games则返回false，否则有就返回true没有就返回false，详见配置share_free_date <br/>
     * <p>
     * 时间段内免费游戏 （"game"+gameType+"_free_date"）
     *
     * @param gameType
     * @param userId
     * @return
     */
    public static boolean freeGame(Integer gameType, long userId) {
        boolean ret = false;
        if (userId > 0 && SHARE_FREE_GAME_MAP.containsKey(gameType)) {
            try {
                String activityRange = ResourcesConfigsUtil.loadServerPropertyValue("share_free_date");
                if (StringUtils.isNotBlank(activityRange)) {
                    Date date = new Date();
                    String[] rangeStrs = activityRange.split("\\;");
                    for (String rangeStr : rangeStrs) {
                        if (StringUtils.isNotBlank(rangeStr)) {
                            String[] strs = rangeStr.split("_");
                            if (strs.length == 2) {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                if (sdf.parse(strs[0]).before(date) && sdf.parse(strs[1]).after(date)) {
                                    String ymd = new SimpleDateFormat("yyyy-MM-dd").format(date);
                                    ret = UserShareDao.getInstance().countUserShare(userId, ymd + " 00:00:00", ymd + " 23:59:59") > 0;

                                    if (ret) {
                                        LogUtil.msgLog.info("shareFreeGame:userId=" + userId + ",gameType=" + gameType);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
            }
        }

        if (!ret) {
            try {
                String[] dateStrs = ResourcesConfigsUtil.loadServerPropertyValue("game" + gameType + "_free_date", "").split("_");
                if (dateStrs.length == 2) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date();
                    if (sdf.parse(dateStrs[0]).before(date) && sdf.parse(dateStrs[1]).after(date)) {
                        ret = true;
                        LogUtil.msgLog.info("freeGame:gameType=" + gameType);
                    }
                }
            } catch (Exception e) {
                LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
            }
        }

        return ret;
    }

    /**
     * 俱乐部免费玩游戏
     *
     * @param gameType
     * @param groupId
     * @return
     */
    public static boolean freeGameOfGroup(int gameType, String groupId) {
        boolean free = false;
        try {
            String[] dateStrs = ResourcesConfigsUtil.loadServerPropertyValue("game" + gameType + "_group_free_date", "").split("_");
            if (dateStrs.length == 2) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();
                if (sdf.parse(dateStrs[0]).before(date) && sdf.parse(dateStrs[1]).after(date)) {
                    String groupIds = ResourcesConfigsUtil.loadServerPropertyValue("game" + gameType + "_group_ids", "ALL");
                    if ("ALL".equals(groupIds) || groupId != null && groupIds.contains(new StringBuilder().append("|").append(groupId).append("|").toString())) {
                        free = true;
                        LogUtil.msgLog.info("freeGameOfGroup:gameType=" + gameType + ",groupId=" + groupId);
                    }
                }
            }
            if(free == false) {// 俱乐部所有玩法在某个时段免费
                String[] groupFreeStrs = ResourcesConfigsUtil.loadServerPropertyValue("group_" + groupId + "_free_date", "").split("_");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();
                if(groupFreeStrs != null && groupFreeStrs.length > 1)
                {
                    if (sdf.parse(groupFreeStrs[0]).before(date) && sdf.parse(groupFreeStrs[1]).after(date)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
        return free;
    }

    /**
     * 检查是否包含该玩法<br/>
     * 如果server.properties中没配置openGameTypes则返回true，否则有就返回true没有就返回false
     *
     * @param gameType
     * @return
     */
    public static boolean hasGame(Integer gameType) {
        return GAME_MAP.isEmpty() || GAME_MAP.containsKey(gameType);
    }

    public static void loadGameConfigs(Map map) {
        Map<String, List<Integer>> cacheIntsMap0 = new ConcurrentHashMap<>();
        Map<String, List<String>> cacheStringsMap0 = new ConcurrentHashMap<>();
        Map<String, String> cacheStringLi0 = new ConcurrentHashMap<>();
        Map<String, Integer> cacheGoldRatioMap0 = new ConcurrentHashMap<>();
        int groupToQinYouQuan0 = 0;
        Set<Map.Entry> entrySet = map.entrySet();
        for (Map.Entry kv : entrySet) {
            String k = kv.getKey().toString();
            String v = kv.getValue().toString();
            if (k.endsWith("_ints")) {
                cacheIntsMap0.put(k, string2IntList(v));
            } else if (k.endsWith("_strings")) {
                cacheStringsMap0.put(k, string2List(v));
            } else if (k.endsWith("_li")) {
                cacheStringLi0.put(k, v);
            } else if (k.endsWith("_ratio")) {
                cacheGoldRatioMap0.put(k, NumberUtils.toInt(v, 1));
            } else if (k.contains("groupToQinYouQuan")) {
                groupToQinYouQuan0 = Integer.parseInt(v);
            } else {
                LogUtil.errorLog.error("game config error:key=" + k + ",value=" + v);
            }
        }

        Map<String, String> map1 = ResourcesConfigsUtil.loadStringValues("GameOrGoldConfig");
        if (map1 != null) {
            for (Map.Entry<String, String> kv : map1.entrySet()) {
                String k = kv.getKey();
                String v = kv.getValue();
                if (k.endsWith("_ints")) {
                    cacheIntsMap0.put(k, string2IntList(v));
                } else if (k.endsWith("_strings")) {
                    cacheStringsMap0.put(k, string2List(v));
                } else if (k.endsWith("_li")) {
                    cacheStringLi0.put(k, v);
                } else if (k.endsWith("_ratio")) {
                    cacheGoldRatioMap0.put(k, NumberUtils.toInt(v, 1));
                } else if (k.contains("groupToQinYouQuan")) {
                    groupToQinYouQuan0 = Integer.parseInt(v);
                } else {
                    LogUtil.errorLog.error("game config error:key=" + k + ",value=" + v);
                }
            }
        }

        cacheIntsMap = cacheIntsMap0;
        cacheStringsMap = cacheStringsMap0;
        cacheStringLi = cacheStringLi0;
        cacheGoldRatioMap = cacheGoldRatioMap0;
        groupToQinYouQuan = groupToQinYouQuan0;

        GoldConstans.loadGoldInfo();
    }

    public static void loadGameConfigsFromDb() {
        Map<String, String> map1 = ResourcesConfigsUtil.loadStringValues("GameOrGoldConfig");
        if (map1 != null) {
            for (Map.Entry<String, String> kv : map1.entrySet()) {
                String k = kv.getKey();
                String v = kv.getValue();
                if (k.endsWith("_ints")) {
                    cacheIntsMap.put(k, string2IntList(v));
                } else if (k.endsWith("_strings")) {
                    cacheStringsMap.put(k, string2List(v));
                } else if (k.endsWith("_li")) {
                    cacheStringLi.put(k, v);
                } else if (k.endsWith("_ratio")) {
                    cacheGoldRatioMap.put(k, NumberUtils.toInt(v, 1));
                } else if (k.contains("groupToQinYouQuan")) {
                    groupToQinYouQuan = Integer.parseInt(v);
                } else {
                    LogUtil.errorLog.error("game config error:key=" + k + ",value=" + v);
                }
            }
        }

        GoldConstans.loadGoldInfo();
    }

    /**
     * 配置文件重新加载
     *
     * @param realPath
     */
    @Override
    public void reload(String realPath) {
        load(loadFromFile(realPath));
    }

    public static void load(Map map) {
        loadGameConfigs(map);
    }

    /**
     * 获取金币场倍率参数配置
     *
     * @param modeId 编号
     */
    public static int loadGoldRatio(String modeId) {
        String key = new StringBuilder(16).append("gold_")
                .append(modeId).append("_ratio").toString();

        Integer ret = cacheGoldRatioMap.get(key);

        return ret == null ? 1 : ret.intValue();
    }

    /**
     * 获取游戏基本参数配置
     *
     * @param playNo     编号
     * @param serverType 场类型
     */
    public static List<Integer> getIntsList(int serverType, String playNo) {
        String key;
        if (serverType == 0) {
            key = new StringBuilder(32).append("game_driving_range_")
                    .append(playNo).append("_ints").toString();
        } else {
            key = new StringBuilder(32).append("gold_")
                    .append(playNo).append("_ints").toString();
        }
        List<Integer> list = cacheIntsMap.get(key);
        if (list != null) {
            LogUtil.msgLog.info("game ints config is found:key=" + key + ",value=" + list);
            return list;
        } else {
            LogUtil.errorLog.warn("game ints config is not found or config error:key=" + key + ",value=[]");
            return Collections.emptyList();
        }
    }

    /**
     * 获取游戏基本参数配置
     *
     * @param playNo     编号
     * @param serverType 场类型
     */
    public static List<String> getStringsList(int serverType, String playNo) {
        String key;
        if (serverType == 0) {
            key = new StringBuilder(32).append("game_driving_range_")
                    .append(playNo).append("_strings").toString();
        } else {
            key = new StringBuilder(32).append("gold_")
                    .append(playNo).append("_strings").toString();
        }
        List<String> list = cacheStringsMap.get(key);
        if (list != null) {
            LogUtil.msgLog.info("game strings config is found:key=" + key + ",value=" + list);
            return list;
        } else {
            LogUtil.errorLog.warn("game strings config is not found or config error:key=" + key + ",value=[]");
            return Collections.emptyList();
        }
    }

    public static List<Integer> string2IntList(String intStr) {
        return string2IntList(intStr, ",");
    }

    public static List<Integer> string2IntList(String intStr, String regex) {
        List<Integer> list;
        if (StringUtils.isBlank(intStr)) {
            list = Collections.emptyList();
        } else {
            list = new ArrayList<>();
            String[] strs = intStr.split(regex);
            for (String temp : strs) {
                list.add(Integer.valueOf(temp));
            }
        }
        return list;
    }

    public static List<String> string2List(String str) {
        return string2List(str, ",");
    }

    public static List<String> string2List(String str, String regex) {
        if (StringUtils.isBlank(str)) {
            return Collections.emptyList();
        } else {
            String[] strs = str.split(regex);
            return Arrays.asList(strs);
        }
    }

    public static String list2String(List<Integer> list, String regex) {
        if (list == null || list.isEmpty()) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            for (Integer value : list) {
                sb.append(value).append(regex);
            }
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
    }

    /**
     * 加载金币场的开关
     */
    public static List<Integer> loadGoldOnOff() {
        String key = "gold_on_off_ints";
        List<Integer> list = cacheIntsMap.get(key);
        if (list != null) {
            LogUtil.msgLog.info("gold_on_off_ints is found:key=" + key + ",value=" + list);
            return list;
        } else {
            LogUtil.errorLog.warn("gold_on_off_ints is not found or config error:key=" + key + ",value=[]");
            return list;
        }
    }

    /**
     * 读取金币场的边界配置
     */
    public static List<Integer> getGoldBorder(int playNo) {
        String key;
        key = new StringBuilder(32).append("gold_border_")
                .append(playNo).append("_ints").toString();
        List<Integer> list = cacheIntsMap.get(key);
        if (list != null) {
            LogUtil.msgLog.info("gold_border is found:key=" + key + ",value=" + list);
            return list;
        } else {
            LogUtil.errorLog.warn("gold_border is not found or config error:key=" + key + ",value=[]");
            return list;
        }
    }

    /**
     * 金币支付的方式
     */
    public static List<Integer> getGoldPay(int pay) {
        String key;
        key = new StringBuilder(32).append("gold_pay_")
                .append(pay).append("_ints").toString();
        List<Integer> list = cacheIntsMap.get(key);
        if (list != null) {
            LogUtil.msgLog.info("gold_pay" + pay + " is found:key=" + key + ",value=" + list);
            return list;
        } else {
            LogUtil.errorLog.warn("gold_pay" + pay + " is not found or config error:key=" + key + ",value=[]");
            return Collections.emptyList();
        }
    }

    /**
     * 金币场自动托管配置
     */
    public static List<Integer> getGoldAuto() {
        String key;
        key = "gold_auto_ints";
        List<Integer> list = cacheIntsMap.get(key);
        if (list != null) {
            LogUtil.msgLog.info("gold_auto is found:key=" + key + ",value=" + list);
            return list;
        } else {
            LogUtil.errorLog.warn("gold_auto is not found or config error:key=" + key + ",value=[]");
            return Collections.emptyList();
        }
    }

    /**
     * 金币场补救金配置
     */
    public static String getRemedy() {
        String key;
        key = "gold_remedy_li";
        String result = cacheStringLi.get(key);
        if (!StringUtil.isBlank(result)) {
            LogUtil.msgLog.info("gold_remedy is found:key=" + key + ",value=" + result);
            return result;
        } else {
            LogUtil.errorLog.warn("gold_remedy is not found or config error:key=" + key + ",value=[]");
            return null;
        }
    }

    /**
     * 金币场聊天室配置
     */
    public static List<Integer> getChatRoom() {
        String key;
        key = "gold_chatRoom_ints";
        List<Integer> list = cacheIntsMap.get(key);
        if (list != null) {
            LogUtil.msgLog.info("gold_chatRoom is found:key=" + key + ",value=" + list);
            return list;
        } else {
            LogUtil.errorLog.warn("gold_chatRoom is not found or config error:key=" + key + ",value=[]");
            return Collections.emptyList();
        }
    }

    public static List<String> getGoldPayStr(int pay) {
        String key;
        key = new StringBuilder(32).append("gold_pay_")
                .append(pay).append("_strings").toString();
        List<String> list = cacheStringsMap.get(key);
        if (list != null) {
            LogUtil.msgLog.info("gold_pay" + pay + " str is found:key=" + key + ",value=" + list);
            return list;
        } else {
            LogUtil.errorLog.warn("gold_pay" + pay + " str is not found or config error:key=" + key + ",value=[]");
            return Collections.emptyList();
        }
    }

    public static List<Integer> getSignGold() {
        String key = "gold_sign_ints";
        List<Integer> list = cacheIntsMap.get(key);
        if (list != null) {
            LogUtil.msgLog.info("gold_sign is found:key=" + key + ",value=" + list);
            return list;
        } else {
            LogUtil.errorLog.warn("gold_sign is not found or config error:key=" + key + ",value=[]");
            return Collections.emptyList();
        }
    }


}
