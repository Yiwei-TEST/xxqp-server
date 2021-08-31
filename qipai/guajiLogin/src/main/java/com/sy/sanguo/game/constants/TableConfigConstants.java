package com.sy.sanguo.game.constants;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class TableConfigConstants {

    private static final List<Integer> dtz_dtz = Arrays.asList(113, 114, 115, 116, 117, 118, 210, 211, 212);
    private static final List<Integer> dtz_pdk = Arrays.asList(15, 16);
    private static final List<Integer> dtz_phz = Arrays.asList(32, 33);
    private static final List<Integer> dtz_bbtz = Arrays.asList(131);
    private static final List<Integer> klphz_phz = Arrays.asList(30, 31);

    /**
     * 设置分成值：为牌桌配置里的抽水分
     *
     * @param modeMsg
     * @return
     */
    public static boolean isCredit(String modeMsg) {
        if (StringUtils.isBlank(modeMsg)) {
            return false;
        }
        String[] splits = modeMsg.split(",");
        if (splits.length < 2) {
            return false;
        }
        int playType = Integer.valueOf(splits[1]);
        int creditModeIndex = -1;

        if (dtz_dtz.contains(playType)) {
            creditModeIndex = 14;
        } else if (dtz_pdk.contains(playType)) {
            creditModeIndex = 13;
        } else if (dtz_phz.contains(playType)) {
            creditModeIndex = 15;
        } else if (dtz_bbtz.contains(playType)) {
            creditModeIndex = 10;
        } else if (klphz_phz.contains(playType)) {
            creditModeIndex = 15;
        }
        int creditMode = (creditModeIndex != -1 && splits.length > (creditModeIndex + 1)) ? Integer.valueOf(splits[(creditModeIndex)]) : 0;
        return creditMode == 1;
    }


    /**
     * 设置分成值：为牌桌配置里的抽水分
     *
     * @param modeMsg
     * @return
     */
    public static int getCreditCommission(String modeMsg) {
        if (StringUtils.isBlank(modeMsg)) {
            return 0;
        }
        String[] splits = modeMsg.split(",");
        if (splits.length < 2) {
            return 0;
        }
        int playType = Integer.valueOf(splits[1]);
        int creditModeIndex = -1;
        int creditCommissionIndex = 0;

        if (dtz_dtz.contains(playType)) {
            creditModeIndex = 14;
            creditCommissionIndex = 18;
        } else if (dtz_pdk.contains(playType)) {
            creditModeIndex = 13;
            creditCommissionIndex = 17;
        } else if (dtz_phz.contains(playType)) {
            creditModeIndex = 15;
            creditCommissionIndex = 19;
        } else if (dtz_bbtz.contains(playType)) {
            creditModeIndex = 10;
            creditCommissionIndex = 14;
        } else if (klphz_phz.contains(playType)) {
            creditModeIndex = 15;
            creditCommissionIndex = 19;
        }
        int creditMode = (creditModeIndex != -1 && splits.length > (creditModeIndex + 1)) ? Integer.valueOf(splits[(creditModeIndex)]) : 0;
        int creditCommission = (creditCommissionIndex != -1 && splits.length > (creditCommissionIndex + 1)) ? Integer.valueOf(splits[(creditCommissionIndex)]) : 0;
        return creditMode == 1 ? creditCommission : 0;
    }

    /**
     * 群主设置小组长的分成值时需要判断：
     * 分成模式为参与模式时，限制值需除以牌桌人数
     *
     * @param modeMsg
     * @param creditAllotMode
     * @return
     */
    public static int getCreditCommissionLimit(String modeMsg, int creditAllotMode) {
        int value = getCreditCommission(modeMsg);
        if (value == 0) {
            return 0;
        }
        if (creditAllotMode == 1) { // 大赢家分成模式
            return value;
        }
        String[] splits = modeMsg.split(",");
        if (splits.length > 8) {
            //参与分成
            value = value / Integer.valueOf(splits[7]);
        }
        return value;
    }

}
