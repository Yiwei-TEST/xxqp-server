package com.sy.sanguo.common.util;

import com.sy.sanguo.common.server.GameServerManager;
import com.sy.sanguo.game.pdkuai.util.LogUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息提示
 */
public final class LangMsg {
    public static final String code_0 = "code_0";//操作成功
    public static final String code_1 = "code_1";//操作失败：签名验证失败
    public static final String code_2 = "code_2";//操作失败：身份信息验证失败
    public static final String code_3 = "code_3";//操作失败：参数错误
    public static final String code_4 = "code_4";//操作失败：请联系系统管理员
    public static final String code_5 = "code_5";//操作失败：亲友圈不存在
    public static final String code_6 = "code_6";//操作失败：未开启此功能
    public static final String code_7 = "code_7";//操作失败：无权限操作
    public static final String code_8 = "code_8";//操作失败：权限不足
    public static final String code_9 = "code_9";//操作失败：您不是该军团成员
    public static final String code_10 = "code_10";//操作失败：权限不够，仅限群主和管理员
    public static final String code_11 = "code_11";//操作失败：尚未加入亲友圈
    public static final String code_12 = "code_12";//操作失败：系统繁忙，请稍后再试
    public static final String code_13 = "code_13";//操作失败：该玩家正在牌局中，不能进行此操作
    public static final String code_14 = "code_14";//操作失败：成员不存在
    public static final String code_15 = "code_15";//操作失败：你的比赛分不够本次转移
    public static final String code_16 = "code_16";//操作失败：成员比赛分不够本次转移
    public static final String code_17 = "code_17";//操作失败：该玩家比赛分已上锁，不可操作
    public static final String code_18 = "code_18";//操作失败：该玩家已下线，不可操作
    public static final String code_19 = "code_19";//操作失败：您在牌局中，不能进行此操作
    public static final String code_20 = "code_20";//操作失败：亲友圈已暂停
    public static final String code_21 = "code_21";//操作失败：该成员或成员下级的比赛分不为0
    public static final String code_22 = "code_22";//操作失败：分配错误{0}
    public static final String code_23 = "code_23";//操作失败：模式不存在
    public static final String code_24 = "code_24";//操作失败：没有找到该房间
    public static final String code_25 = "code_25";//操作失败：没有找到该玩家
    public static final String code_26 = "code_26";//操作失败：已绑定，不可重复绑定
    public static final String code_27 = "code_27";//操作失败：目标玩家零钱包不为零
    /**
     * 提示消息缓存
     */
    private static Map<String, String> langMap = new ConcurrentHashMap<>();

    public static final String getMsg(String msgKey, Object... o) {
        String msg = langMap.get(msgKey);
        if (msg == null) {
            LogUtil.e("Lang msg not exist:" + msgKey);
            return "未知错误：" + msgKey;
        }
        String result = msg;
        if (o == null || o.length == 0) {
            return result;
        }
        for (int i = 0; i < o.length; i++) {
            result = result.replace("{" + i + "}", String.valueOf(o[i]));
        }
        return result;
    }

    public static final void loadLangMsg() {
        String path = LangMsg.class.getClassLoader().getResource("").getPath() + "csv/lang.csv";
        List<String[]> list = GameServerManager.readCSV(path, false);
        parseLangMsg(list);
    }

    private static final void parseLangMsg(List<String[]> csvList) {
        if (csvList.isEmpty()) {
            return;
        }

        for (String[] values : csvList) {
            int i = 0;
            String langKey = getStrValue(values, i++);
            String langMsg = getStrValue(values, i++);
            langMap.put(langKey, langMsg);
        }
        LogUtil.i("load lang msg finished");
    }

    /**
     * getValue读取csv
     *
     * @param values
     * @param index
     * @return
     */
    protected static String getStrValue(String[] values, int index) {
        return StringUtil.getValue(values, index);
    }

}
