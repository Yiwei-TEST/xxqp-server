package com.sy599.game.qipai.jhphz281.util.zphu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZpConstant {
    public static final int laiZiVal = 200;

    /**
     * 牌组
     **/
    public static final Map<Integer, int[][]> paiZuMap = new HashMap<>();

    public static final String[] xiao_zi = {"", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
    public static final String[] da_zi = {"", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖", "拾"};

    static {
        init();
    }

    public static void init() {
        //初始化牌组
        for (int i = 1; i <= 10; i++) {
            paiZuMap.put(i, initPaiZu(i));
        }
        for (int i = 11; i <= 20; i++) {
            paiZuMap.put(i, initPaiZu(i));
        }
    }

    public static int[][] initPaiZu(int val) {
        List<int[]> paiZus = new ArrayList<>();

        // 四张一样的
        int[] paiZu = new int[]{val, val, val, val, 0};
        if (isBig(val)) {
            paiZu[paiZu.length - 1] = 12;
        } else {
            paiZu[paiZu.length - 1] = 9;
        }
        paiZus.add(paiZu);

        // 三张一样的
        paiZu = new int[]{val, val, val, 0};
        if (isBig(val)) {
            paiZu[paiZu.length - 1] = 6;
        } else {
            paiZu[paiZu.length - 1] = 3;
        }
        paiZus.add(paiZu);

        // 二七十
        if (val % 10 == 2) {
            paiZu = new int[]{val, val + 5, val + 8, 0};
            if (isBig(val)) {
                paiZu[paiZu.length - 1] = 6;
            } else {
                paiZu[paiZu.length - 1] = 3;
            }
            paiZus.add(paiZu);
        } else if (val % 10 == 7) {
            paiZu = new int[]{val - 5, val, val + 3, 0};
            if (isBig(val)) {
                paiZu[paiZu.length - 1] = 6;
            } else {
                paiZu[paiZu.length - 1] = 3;
            }
            paiZus.add(paiZu);
        } else if (val % 10 == 0) {
            paiZu = new int[]{val - 8, val - 3, val, 0};
            if (isBig(val)) {
                paiZu[paiZu.length - 1] = 6;
            } else {
                paiZu[paiZu.length - 1] = 3;
            }
            paiZus.add(paiZu);
        }

        // 做顺
        if (val % 10 == 0) {
            // 十九八
            paiZus.add(new int[]{val - 2, val - 1, val, 0});
        } else if (val % 10 == 9) {
            // 七八九
            paiZus.add(new int[]{val - 2, val - 1, val, 0});
            // 八九十
            paiZus.add(new int[]{val - 1, val, val + 1, 0});
        } else if (val % 10 == 1) {
            // 一二三
            paiZu = new int[]{val, val + 1, val + 2, 0};
            if (isBig(val)) {
                paiZu[paiZu.length - 1] = 6;
            } else {
                paiZu[paiZu.length - 1] = 3;
            }
            paiZus.add(paiZu);
        } else if (val % 10 == 2) {
            // 一二三
            paiZu = new int[]{val - 1, val, val + 1, 0};
            if (isBig(val)) {
                paiZu[paiZu.length - 1] = 6;
            } else {
                paiZu[paiZu.length - 1] = 3;
            }
            paiZus.add(paiZu);
            // 二三四
            paiZus.add(new int[]{val, val + 1, val + 2, 0});
        } else {
            if (val % 10 == 3) {
                paiZu = new int[]{val - 2, val - 1, val, 0};
                if (isBig(val)) {
                    paiZu[paiZu.length - 1] = 6;
                } else {
                    paiZu[paiZu.length - 1] = 3;
                }
                paiZus.add(paiZu);
            }
            paiZus.add(new int[]{val - 1, val, val + 1, 0});
            paiZus.add(new int[]{val, val + 1, val + 2, 0});
        }

        // 大小牌组合
        if (val > 10) {
            paiZus.add(new int[]{val - 10, val - 10, val, 0});
            paiZus.add(new int[]{val - 10, val, val, 0});
        } else {
            paiZus.add(new int[]{val, val + 10, val + 10, 0});
            paiZus.add(new int[]{val, val, val + 10, 0});
        }
        int[][] arrRes = new int[paiZus.size()][];
        for (int i = 0; i < paiZus.size(); i++) {
            arrRes[i] = paiZus.get(i);
        }
        return arrRes;
    }

    public static int[][] getPaiZu(int val) {
        return paiZuMap.get(val);
    }

    /**
     * 复制数组
     *
     * @param src
     * @return
     */
    public static int[] copyArr(int[] src) {
        int[] dest = new int[src.length];
        System.arraycopy(src, 0, dest, 0, src.length);
        return dest;
    }

    /**
     * 少复制最后一位
     *
     * @param src
     * @return
     */
    public static int[] subArr1(int[] src) {
        int[] dest = new int[src.length - 1];
        System.arraycopy(src, 0, dest, 0, src.length - 1);
        return dest;
    }

    /**
     * 是否是大牌
     *
     * @param val
     * @return
     */
    public static boolean isBig(int val) {
        return val > 10;
    }

    public static String getName(int val) {
        if (val == laiZiVal) {
            return "王";
        } else if (val <= 10) {
            return xiao_zi[val];
        } else {
            return da_zi[val == 20 ? 10 : val % 10];
        }
    }


}
