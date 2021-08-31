package com.sy599.game.qipai.dzbp.constant;

import com.sy599.game.util.ResourcesConfigsUtil;

import java.util.*;

public class PaohuziConstant {

    public static boolean isTest = false;

    public static boolean isAutoMo = true;

    public static final int state_player_ready = 1;
    /**
     * 胡牌次数
     */
    public static final int ACTION_COUNT_INDEX_HU = 0;
    /**
     * 自摸次数
     */
    public static final int ACTION_COUNT_INDEX_ZIMO = 1;
    /**
     * 提次数
     */
    public static final int ACTION_COUNT_INDEX_TI = 2;
    /**
     * 跑次数
     */
    public static final int ACTION_COUNT_INDEX_PAO = 3;

    // 跑胡子
    public static List<Integer> cardList = new ArrayList<>();
    /**
     * 牌组
     **/
    public static final Map<Integer, List<int[]>> paiZuMap = new HashMap<>();

    static {
        isTest = "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("test"));
        for (int i = 1; i <= 40; i++) {
            cardList.add(i);
        }
        //初始化牌组
//        for (int i = 1; i <= 10; i++) {
//            paiZuMap.put(i, initPaiZu(i));
//        }
        for (int i = 101; i <= 110; i++) {
            paiZuMap.put(i, initPaiZu(i));
        }

    }
    public static List<int[]> getPaiZu(int val) {
        return paiZuMap.get(val);
    }

    public static List<int[]> initPaiZu(int val) {
        List<int[]> res = new ArrayList<>();

        //三张一样
        res.add(new int[]{val, val, val});

        //二七十
        if (val % 100 == 2) {
            res.add(new int[]{val, val + 5, val + 8});
        } else if (val % 100 == 7) {
            res.add(new int[]{val - 5, val, val + 3});
        } else if (val % 100 == 10) {
            res.add(new int[]{val - 8, val - 3, val});
        }

        //做顺
        if (val % 100 == 10) {
            res.add(new int[]{val - 2, val - 1, val});
        } else if (val % 100 == 9) {
            res.add(new int[]{val - 2, val - 1, val});
            res.add(new int[]{val - 1, val, val + 1});
        } else if (val % 100 == 1) {
            res.add(new int[]{val, val + 1, val + 2});
        } else if (val % 100 == 2) {
            res.add(new int[]{val - 1, val, val + 1});
            res.add(new int[]{val, val + 1, val + 2});
        } else {
            res.add(new int[]{val - 2, val - 1, val});
            res.add(new int[]{val - 1, val, val + 1});
            res.add(new int[]{val, val + 1, val + 2});
        }

        return res;
    }
}
