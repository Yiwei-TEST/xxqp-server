package com.sy599.game.util.mj.util;

import com.sy599.game.util.mj.core.handle.Hulib;
import com.sy599.game.util.mj.core.handle.TableMgr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MjHuUtil {
    public static final String source_dir = "mjtbl/";
    public static final String dump_file_dir = "D:\\develop\\workspace\\bjd\\qipai-all-2\\qipai-common\\src\\main\\resources\\mjtbl\\";

    public static final Set<Integer> index258 = new HashSet<>(Arrays.asList(1, 4, 7, 10, 13, 16, 19, 22, 25));
    public static final Map<Integer, Integer> val2Index = new HashMap<>();
    public static final Map<Integer, Integer> index2Val = new HashMap<>();

    static {

        // 1-9条 val：11-19 index：0-8
        // 1-9筒 val：21-29 index：9-17
        // 1-9万 val：31-39 index：18-26
        // 东风 val：301 index：27
        // 南风 val：311 index：28
        // 西风 val：321 index：29
        // 北风 val：331 index：30
        // 红中 val：201 index：31
        // 发财 val：211 index：32
        // 白板 val：221 index：33

        for (int index = 0; index < 34; index++) {
            index2Val.put(index, index2MjValForInit(index));
        }

        for (Integer index : index2Val.keySet()) {
            val2Index.put(index2Val.get(index), index);
        }
    }

    public static void init() {
        TableMgr.getInstance().load();
    }

    public static int index2MjValForInit(int index) {
        int type = index / 9 + 1;
        int number = index % 9 + 1;
        int val;
        if (type <= 3) {
            val = type * 10 + number;
        } else {
            if (number <= 4) {
                val = 300 + (number - 1) * 10 + 1;
            } else {
                val = 200 + (number - 5) * 10 + 1;
            }
        }
        return val;
    }


    public static void dumpFile() {
        com.sy599.game.util.mj.core.gen_feng_table.Program.gen();
        com.sy599.game.util.mj.core.gen_table.Program.gen();
        com.sy599.game.util.mj.core.gen_ting_table.Program.gen();
    }


    /**
     * 麻将val列表转成cardArr
     *
     * @param vals 麻将的val列表
     * @return
     */
    private static int[] toCardArr(List<Integer> vals) {
        int[] cardsArr = new int[34];
        Arrays.fill(cardsArr, 0);
        for (Integer val : vals) {
            cardsArr[getIndex(val)] += 1;
        }
        return cardsArr;
    }

    private static int dropGui(int[] cardArr, int guiVal) {
        int guiNum = 0;
        for (int index = 0; index < cardArr.length; index++) {
            if (index2Val.get(index).intValue() == guiVal) {
                guiNum += cardArr[index];
                cardArr[index] = 0;
            }
        }
        return guiNum;
    }

    private static int getVal(int index) {
        return index2Val.get(index);
    }

    private static int getIndex(int val) {
        return val2Index.get(val);
    }

    private static boolean isCanHu(int[] cardsArr, int guiNum, boolean need258, boolean hu7Dui) {
        if (hu7Dui) {
            if (isHu7Dui(cardsArr, guiNum)) {
                return true;
            }
        }
        if (need258) {
            for (int index = 0; index < cardsArr.length; index++) {
                if (cardsArr[index] <= 0) {
                    continue;
                }
                if (!index258.contains(index)) {
                    continue;
                }
            }
            return false;
        } else {
            return Hulib.getInstance().get_hu_info(cardsArr, 34, guiNum, false);
        }
    }

    /**
     *  是否可胡牌
     * @param vals
     * @param guiVal
     * @param hu7Dui 是否可胡7小对
     * @return
     */
    public static boolean isCanHu(List<Integer> vals, int guiVal, boolean hu7Dui) {
        if (vals.size() % 3 != 2) {
            return false;
        }
        int[] cardsArr = toCardArr(vals);
        int guiNum = dropGui(cardsArr, guiVal);
        return isCanHu(cardsArr, guiNum, false, hu7Dui);

    }

    private static boolean isHu7Dui(int[] cardsArr, int guiNum) {
        int needGui = 0;
        int cardNum = 0;
        for (int i = 0; i < cardsArr.length; i++) {
            needGui += cardsArr[i] % 2;
            cardNum += cardsArr[i];
        }
        return cardNum + guiNum == 14 && needGui <= guiNum;
    }

    private static List<Integer> calcTingList(int[] cardsArr, int guiVal, int guiNum, boolean need258, boolean hu7Dui) {
        List<Integer> tingList = new ArrayList<>();
        for (Integer index : index2Val.keySet()) {
            int val = getVal(index);
            if (val == guiVal) {
                continue;
            }
            cardsArr[index]++;
            if (isCanHu(cardsArr, guiNum, need258, hu7Dui)) {
                tingList.add(val);
            }
            cardsArr[index]--;
        }
        return tingList;
    }

    /**
     * 计算听牌列表
     * @param vals
     * @param guiVal
     * @param need258 是否258作将，暂不支持
     * @param hu7Dui 是否可胡7小对
     * @return
     */
    public static List<Integer> calcTingList(List<Integer> vals, int guiVal, boolean need258, boolean hu7Dui) {
        if (vals.size() % 3 != 1) {
            return null;
        }
        int[] cardsArr = toCardArr(vals);
        int guiNum = dropGui(cardsArr, guiVal);
        return calcTingList(cardsArr, guiVal, guiNum, need258, hu7Dui);
    }

    /**
     * 打牌听牌列表
     * @param vals
     * @param guiVal
     * @param need258 是否258作将，暂不支持
     * @param hu7Dui 是否可胡7小对
     * @return
     */
    public static Map<Integer, List<Integer>> calcDaTingMap(List<Integer> vals, int guiVal, boolean need258, boolean hu7Dui) {
        if (vals.size() % 3 != 2) {
            return null;
        }
        int[] cardsArr = toCardArr(vals);
        int guiNum = dropGui(cardsArr, guiVal);
        Map<Integer, List<Integer>> daTingMap = new HashMap<>();
        for (int index = 0; index < cardsArr.length; index++) {
            if (cardsArr[index] <= 0) {
                continue;
            }
            if (getVal(index) == guiVal) {
                continue;
            }
            cardsArr[index] -= 1;
            List<Integer> tingList = calcTingList(cardsArr, guiVal, guiNum, need258, hu7Dui);
            if (tingList != null && tingList.size() > 0) {
                daTingMap.put(getVal(index), tingList);
            }
            cardsArr[index] += 1;
        }
        return daTingMap;
    }

    public static void main(String[] args) {
//        dumpFile();

        init();


//        testHu();
        testTing();
//        testDaTing();

    }

    public static void testHu() {
        List<Integer> vals = Arrays.asList(11, 12, 13, 23, 24, 25, 34, 35, 36, 211, 211, 201, 21, 201);
//        vals = Arrays.asList(211, 201, 33, 33, 33);
        int guiVal = 201;
        boolean need258 = false;
        boolean hu7Dui = true;
        boolean isHu = false;

        int count = 1000000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            isHu = isCanHu(new ArrayList<>(vals), guiVal, hu7Dui);
        }
        long timeUse = System.currentTimeMillis() - start;
        System.out.println("count=" + count + "次,timeUse=" + timeUse + "ms" + ",avg=" + (timeUse * 1d / count) + "ms");
        System.out.println("isH=" + isHu);
    }


    public static void testTing() {
        List<Integer> vals = Arrays.asList(11, 12, 13, 23, 24, 25, 34, 35, 36, 211, 211, 201, 21);
//        vals = Arrays.asList(201, 33, 33, 33);

        int guiVal = 201;
        boolean need258 = false;
        boolean hu7Dui = true;
        int count = 100000;
        List<Integer> tingList = new ArrayList<>();

        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            tingList = calcTingList(new ArrayList<>(vals), guiVal, need258, hu7Dui);
        }
        long timeUse = System.currentTimeMillis() - start;
        System.out.println("count=" + count + "次,timeUse=" + timeUse + "ms" + ",avg=" + (timeUse * 1d / count) + "ms");
        System.out.println("tingList=" + tingList);
    }

    public static void testDaTing() {
        List<Integer> vals = Arrays.asList(11, 12, 13, 23, 24, 25, 34, 35, 36, 211, 211, 201, 21, 201);
//        vals = Arrays.asList(211, 201, 33, 33, 33);

        int guiVal = 201;
        boolean need258 = false;
        boolean hu7Dui = true;
        int count = 10000;
        Map<Integer, List<Integer>> daTingList = new HashMap<>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            daTingList = calcDaTingMap(new ArrayList<>(vals), guiVal, need258, hu7Dui);
        }
        long timeUse = System.currentTimeMillis() - start;
        System.out.println("count=" + count + "次,timeUse=" + timeUse + "ms" + ",avg=" + (timeUse * 1d / count) + "ms");
        System.out.println("daTingList=" + daTingList);
    }

}
