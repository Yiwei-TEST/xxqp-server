package com.sy599.game.util.mj.serialize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MjConstant {

    /**
     * 牌组
     **/
    public static final Map<Integer, List<int[]>> paiZuMap = new HashMap<>();

    static {
        //初始化牌组
        for (int i = 1; i <= 9; i++) {
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

        //做顺
        if (val == 9) {
            res.add(new int[]{val - 2, val - 1, val});
        } else if (val == 8) {
            res.add(new int[]{val - 2, val - 1, val});
            res.add(new int[]{val - 1, val, val + 1});
        } else if (val == 1) {
            res.add(new int[]{val, val + 1, val + 2});
        } else if (val == 2) {
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
