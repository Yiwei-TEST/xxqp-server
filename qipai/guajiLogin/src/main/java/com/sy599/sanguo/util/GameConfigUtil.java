package com.sy599.sanguo.util;

import com.sy.sanguo.common.util.StringUtil;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏基本参数配置
 */
public final class GameConfigUtil {
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
                LogUtil.e("game config error:key=" + k + ",value=" + v);
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
                    LogUtil.e("game config error:key=" + k + ",value=" + v);
                }
            }
        }

        cacheIntsMap = cacheIntsMap0;
        cacheStringsMap = cacheStringsMap0;
        cacheStringLi = cacheStringLi0;
        cacheGoldRatioMap = cacheGoldRatioMap0;
        groupToQinYouQuan = groupToQinYouQuan0;
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
                    LogUtil.e("game config error:key=" + k + ",value=" + v);
                }
            }
        }
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
            LogUtil.i("game ints config is found:key=" + key + ",value=" + list);
            return list;
        } else {
            LogUtil.e("game ints config is not found or config error:key=" + key + ",value=[]");
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
            LogUtil.i("game strings config is found:key=" + key + ",value=" + list);
            return list;
        } else {
            LogUtil.e("game strings config is not found or config error:key=" + key + ",value=[]");
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
                list.add(Integer.parseInt(temp));
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
            LogUtil.i("gold_on_off_ints is found:key=" + key + ",value=" + list);
            return list;
        } else {
            LogUtil.e("gold_on_off_ints is not found or config error:key=" + key + ",value=[]");
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
            LogUtil.i("gold_border is found:key=" + key + ",value=" + list);
            return list;
        } else {
            LogUtil.e("gold_border is not found or config error:key=" + key + ",value=[]");
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
            LogUtil.i("gold_pay" + pay + " is found:key=" + key + ",value=" + list);
            return list;
        } else {
            LogUtil.e("gold_pay" + pay + " is not found or config error:key=" + key + ",value=[]");
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
            LogUtil.i("gold_auto is found:key=" + key + ",value=" + list);
            return list;
        } else {
            LogUtil.e("gold_auto is not found or config error:key=" + key + ",value=[]");
            return Collections.emptyList();
        }
    }

}
