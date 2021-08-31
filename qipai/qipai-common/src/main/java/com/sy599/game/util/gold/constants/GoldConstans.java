package com.sy599.game.util.gold.constants;

import com.sy599.game.util.GameConfigUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;

public final class GoldConstans {
    /*** 金币场总开关*/
    public static int gold_on_off = 0;

    /**
     * 读取金币场配置信息
     * #金币场总开关
     * #每人支付金币开关
     * #赢家抽水开关
     * #金币场托管开关
     * #金币场补救金开关
     * #金币场聊天室开关
     * #金币场签到开关
     */
    public static void loadGoldInfo() {
        List<Integer> list = GameConfigUtil.loadGoldOnOff();
        if (list == null) {
            gold_on_off = 0;
            return;
        }
        if (!list.isEmpty()) {
            int i = 0;
            gold_on_off = getValue(list, i++);
        }
    }

    private static int getValue(List<Integer> list, int i) {
        if (list == null || i >= list.size()) {
            return 0;
        }
        return list.get(i);
    }

    /**
     * 金币场是否开放
     */
    public static boolean isGoldSiteOpen() {
//        return gold_on_off == 1;
        return true;
    }

    /**
     * 金币场机器人自动加入时间
     */
    public static long loadRobotJoinTime() {
        return NumberUtils.toLong(ResourcesConfigsUtil.loadServerPropertyValue("gold_robot_join_time"),30*1000L);
    }
}
