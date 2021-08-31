package com.sy599.game.util.mj.util;


import com.sy599.game.util.TingResouce;

import java.util.*;

public class MjHuUtilNew {

    private static final Set<Integer> val258 = new HashSet<>(Arrays.asList(2, 5, 8));
    public static final Set<Integer> tiaoValSet = new HashSet<>(Arrays.asList(11, 12, 13, 14, 15, 16, 17, 18, 19));
    public static final Set<Integer> tongValSet = new HashSet<>(Arrays.asList(21, 22, 23, 24, 25, 26, 27, 28, 29));
    public static final Set<Integer> wanValSet = new HashSet<>(Arrays.asList(31, 32, 33, 34, 35, 36, 37, 38, 39));
    public static final Set<Integer> fengValSet = new HashSet<>(Arrays.asList(301, 311, 321, 331, 201, 211, 221));
    public static final List<Integer> allValList = new ArrayList<>();
    public static final List<Integer> allValNoFengList = new ArrayList<>();
    public static final Map<Integer, Integer> val2Index = new HashMap<>();
    public static final Map<Integer, Integer> index2Val = new HashMap<>();

    static {

        allValList.addAll(tiaoValSet);
        allValList.addAll(tongValSet);
        allValList.addAll(wanValSet);
        allValList.addAll(fengValSet);

        allValNoFengList.addAll(tiaoValSet);
        allValNoFengList.addAll(tongValSet);
        allValNoFengList.addAll(wanValSet);

        for (Integer val : allValList) {
            val2Index.put(val, getIndexForInit(val));
        }
        for (Integer index : val2Index.keySet()) {
            index2Val.put(index, val2Index.get(index));
        }
    }

    private static int getIndexForInit(int val) {
        int yu = val % 10;
        int chu = val / 10;
        return (chu - 1) * 9 + yu;
    }

    /**
     * 去除给定的王牌
     *
     * @param cardVals
     * @param bossVals
     * @return
     */
    private static int dropBoss(List<Integer> cardVals, Set<Integer> bossVals) {
        int bossNum = 0;
        if (bossVals != null && bossVals.size() > 0) {
            Iterator<Integer> it = cardVals.iterator();
            while (it.hasNext()) {
                Integer val = it.next();
                if (bossVals.contains(val)) {
                    it.remove();
                    bossNum++;
                }
            }
        }
        return bossNum;
    }


    public static int getVal(int index) {
        return index2Val.get(index);
    }

    public static int getIndex(int val) {
        return val2Index.get(val);
    }

    private static int[] val2IndexArr(List<Integer> mjVals, int bossNum) {
        int[] yus = new int[28];
        for (Integer val : mjVals) {
            yus[getIndex(val)]++;
        }
        yus[0] = bossNum;
        return yus;
    }


    private static boolean isHu(int[] yus) {
        for (int i = 1; i < 28; i += 9) {
            if (isZu(yus, i) == -1) {
                return false;
            }
        }
        return true;
    }


    /**
     * 将已经按条筒万分类的数组转化为code查询是否组成牌组
     * 牌数不够则用王补，王不够则返回-1,表示不能组成牌组
     *
     * @param yus
     * @param start
     * @return
     */
    private static int isZu(int[] yus, int start) {
        int size = 0;
        int bossNum = yus[0];
        for (int i = start, end = start + 8; i <= end; i++) {
            size += yus[i];
        }
        if (size == 0) {
            return bossNum;
        }
        int yu = size % 3;
        if (yu != 0 && bossNum < 3 - yu) {
            return -1;
        }
        int useBoss = 0;
        if (yu != 0) {
            useBoss = 3 - yu;
        }
        for (int i = 0; i < bossNum / 3; i++) {
            if (bossNum >= useBoss + 3) {
                useBoss += 3;
            } else {
                break;
            }
        }

        long code = useBoss;
        for (int i = start, end = start + 8; i <= end; i++) {
            code = code * 10 + yus[i];
        }

        int remainBossNum = TingResouce.getRemainBossNum(useBoss, code);
        if (remainBossNum == -1) {
            return -1;
        }
        return bossNum - useBoss + remainBossNum;
    }


    /*--------------------------------------------------------------------------------------*/


    public static boolean isHuAll(List<Integer> mjVals, Set<Integer> bossVals, Set<Integer> yingJiangVals) {
        return isHuAll(mjVals, bossVals, true, yingJiangVals, true);
    }

    /**
     * 包括风牌是否可胡牌
     *
     * @param mjVals        检查胡牌的牌值列表
     * @param bossVals      癞子牌的值列表，支持多个癞子牌，null或empty表示没有癞子牌
     * @param daiFeng       是否带风牌
     * @param yingJiangVals 硬将列表：如：Set<>{2,5,8}表示必须使用258做硬将,null或空Set表示不强制将类
     * @param hu7Dui        是否可胡七小对，是：会检查七小对牌型，否不会检查七小对牌型
     * @return
     */
    public static boolean isHuAll(List<Integer> mjVals, Set<Integer> bossVals, boolean daiFeng, Set<Integer> yingJiangVals, boolean hu7Dui) {
        if (mjVals.size() % 3 != 2)
            return false;
        List<Integer> copy = new ArrayList<>(mjVals);
        int bossNum = dropBoss(copy, bossVals);
        int needWang = 0;
        if (daiFeng) {
            Map<Integer, Integer> valAndNum = findFengPai(copy);
            for (Map.Entry<Integer, Integer> entry : valAndNum.entrySet()) {
                switch (entry.getValue()) {
                    case 1:
                    case 4:
                        needWang += 2;
                        break;
                    case 2:
                        needWang++;
                        break;
                }
            }
        }
        if (hu7Dui && isHu7dui(copy, bossNum)) {
            return true;
        }
        if (yingJiangVals != null && yingJiangVals.size() > 0) {
            return isHuNeedYingJiang(copy, bossNum - needWang, yingJiangVals);
        } else {
            if (needWang == 0 && isHu(copy, bossNum, true)) {
                return true;
            } else if (needWang > 0) {
                if (bossNum >= needWang && isHu(copy, bossNum - needWang, true)) {
                    return true;
                }
                if (bossNum + 1 >= needWang && isHu(copy, bossNum - needWang + 1, false)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 找到风牌的同时从ids中移除
     *
     * @return
     */
    private static Map<Integer, Integer> findFengPai(List<Integer> mjVals) {
        Map<Integer, Integer> valAndNum = new HashMap<>();
        Iterator<Integer> it = mjVals.iterator();
        while (it.hasNext()) {
            Integer val = it.next();
            if (fengValSet.contains(val)) {
                it.remove();
                if (valAndNum.containsKey(val)) {
                    valAndNum.put(val, valAndNum.get(val) + 1);
                } else {
                    valAndNum.put(val, 1);
                }
            }
        }
        return valAndNum;
    }

    public static boolean isHu(List<Integer> mjVals, int bossNum, boolean needJiang) {
        if (needJiang && (mjVals.size() + bossNum) % 3 != 2) {
            return false;
        } else if (!needJiang && (mjVals.size() + bossNum) % 3 != 0) {
            return false;
        }
        if (bossNum < 0) {
            return false;
        }
        int[] yus = val2IndexArr(mjVals, bossNum);
        if (needJiang) {
            for (int i = 0; i < yus.length; i++) {
                //找到将牌
                if (yus[0] < (2 - yus[i])) {
                    continue;
                }
                int rmNum = 0;
                int wNum = 0;

                if (yus[i] >= 2) {
                    yus[i] -= 2;
                    rmNum = 2;
                } else {
                    //不够则用王补
                    rmNum = yus[i];
                    yus[i] = 0;
                    yus[0] -= (2 - rmNum);
                    wNum = 2 - rmNum;
                }
                if (isHu(yus)) {
                    return true;
                }
                yus[0] += wNum;
                yus[i] += rmNum;
            }
        } else {
            if (isHu(yus)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否需要硬将
     *
     * @param mjVals
     * @param bossNum
     * @return
     */
    public static boolean isHuNeedYingJiang(List<Integer> mjVals, int bossNum, Set<Integer> yingJiangVals) {
        if ((mjVals.size() + bossNum) % 3 != 2 || bossNum < 0) {
            return false;
        }
        int[] yus = val2IndexArr(mjVals, bossNum);
        for (int i = 0; i < yus.length; i++) {
            //找到将牌
            if ((yingJiangVals.contains(i % 9))) {
                if (yus[0] < (2 - yus[i])) {
                    continue;
                }
                int rmNum = 0;
                int wNum = 0;

                if (yus[i] >= 2) {
                    yus[i] -= 2;
                    rmNum = 2;
                } else {
                    //不够则用王补
                    rmNum = yus[i];
                    yus[i] = 0;
                    yus[0] -= (2 - rmNum);
                    wNum = 2 - rmNum;
                }
                if (isHu(yus)) {
                    return true;
                }
                yus[0] += wNum;
                yus[i] += rmNum;
            }
        }
        return false;
    }

    private static boolean isHu7dui(List<Integer> mjVals, int bossNum) {
        if (mjVals.size() + bossNum != 14) {
            return false;
        }
        List<Integer> copy = new ArrayList<>(mjVals);
        Map<Integer, Integer> valAndNum = new HashMap<>();
        for (Integer val : copy) {
            if (valAndNum.containsKey(val)) {
                valAndNum.put(val, valAndNum.get(val) + 1);
            } else {
                valAndNum.put(val, 1);
            }
        }
        for (Integer num : valAndNum.values()) {
            if (num % 2 != 0)
                bossNum--;
        }
        if (bossNum >= 0)
            return true;
        return false;
    }

    /**
     * 听牌列表
     *
     * @param mjVals        检查听牌的牌值列表
     * @param bossVals      癞子牌的值列表，支持多个癞子牌，null或empty表示没有癞子牌
     * @param daiFeng       是否带风牌
     * @param yingJiangVals 硬将列表：如：Set<>{2,5,8}表示必须使用258做硬将,null或空Set表示不强制将类
     * @param hu7Dui        是否可胡七小对，是：会检查七小对牌型，否不会检查七小对牌型
     * @return <所有听的牌值列表>
     */
    public static List<Integer> calcTingList(List<Integer> mjVals, Set<Integer> bossVals, boolean daiFeng, Set<Integer> yingJiangVals, boolean hu7Dui) {
        if (mjVals.size() % 3 != 1) {
            return null;
        }
        List<Integer> tingList = new ArrayList<>();
        List<Integer> copy = new ArrayList<>(mjVals);
        int size = copy.size();
        List<Integer> allVals = daiFeng ? allValList : allValNoFengList;

        for (Integer val : allVals) {
            if (bossVals.contains(val)) {
                continue;
            }
            copy.add(val);
            if (isHuAll(copy, bossVals, daiFeng, yingJiangVals, hu7Dui)) {
                tingList.add(val);
            }
            copy.remove(size);
        }
        return tingList;
    }

    /**
     * 打听列表
     *
     * @param mjVals        检查听牌的牌值列表
     * @param bossVals      癞子牌的值列表，支持多个癞子牌，null或empty表示没有癞子牌
     * @param daiFeng       是否带风牌
     * @param yingJiangVals 硬将列表：如：Set<>{2,5,8}表示必须使用258做硬将,null或空Set表示不强制将类
     * @param hu7Dui        是否可胡七小对，是：会检查七小对牌型，否不会检查七小对牌型
     * @return <打的牌值,听的牌值列表>
     */
    public static Map<Integer, List<Integer>> calcDaTingMap(List<Integer> mjVals, Set<Integer> bossVals, boolean daiFeng, Set<Integer> yingJiangVals, boolean hu7Dui) {
        if (mjVals.size() % 3 != 2) {
            return null;
        }
        List<Integer> copy = new ArrayList<>(mjVals);
        Map<Integer, List<Integer>> daTingList = new HashMap<>();
        for (int i = 0, size = copy.size(); i < size; i++) {
            Integer val = copy.get(i);
            if (bossVals.contains(val)) {
                continue;
            }
            if (daTingList.containsKey(val)) {
                continue;
            }
            copy.remove(i);
            List<Integer> tingList = calcTingList(copy, bossVals, daiFeng, yingJiangVals, hu7Dui);
            if (tingList != null && tingList.size() > 0) {
                daTingList.put(val, tingList);
            }
            copy.add(i, val);
        }
        return daTingList;
    }

    public static void main(String[] args) {
        TingResouce.init();

//        testHu();
//        testTing();
        testDaTing();

    }

    public static void testHu() {
        List<Integer> vals = Arrays.asList(11, 12, 13, 23, 24, 25, 34, 35, 36, 211, 211, 201, 21, 201);
//        vals = Arrays.asList(211, 201, 33, 33, 33);
        List<Integer> bossVals = Arrays.asList(201);
        Set<Integer> yingJiangVals = new HashSet<>();
        boolean hu7Dui = true;
        boolean daiFeng = true;

        int count = 1000000;
        boolean isHu = false;
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            isHu = isHuAll(new ArrayList<>(vals), new HashSet<>(bossVals), daiFeng, yingJiangVals, hu7Dui);
        }
        long timeUse = System.currentTimeMillis() - start;
        System.out.println("count=" + count + "次,timeUse=" + timeUse + "ms" + ",avg=" + (timeUse * 1d / count) + "ms");
        System.out.println("isH=" + isHu);
    }


    public static void testTing() {
        List<Integer> vals = Arrays.asList(11, 12, 13, 23, 24, 25, 34, 35, 36, 211, 211, 201, 21);
//        vals = Arrays.asList(201, 33, 33, 33);

        List<Integer> bossVals = Arrays.asList(201);
        Set<Integer> yingJiangVals = new HashSet<>();
        boolean hu7Dui = true;
        boolean daiFeng = true;

        int count = 100000;
        List<Integer> tingList = new ArrayList<>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            tingList = calcTingList(new ArrayList<>(vals), new HashSet<>(bossVals), daiFeng, yingJiangVals, hu7Dui);
        }
        long timeUse = System.currentTimeMillis() - start;
        System.out.println("count=" + count + "次,timeUse=" + timeUse + "ms" + ",avg=" + (timeUse * 1d / count) + "ms");
        System.out.println("tingList=" + tingList);
    }

    public static void testDaTing() {
        List<Integer> vals = Arrays.asList(11, 12, 13, 23, 24, 25, 34, 35, 36, 211, 211, 201, 21, 201);
//        vals = Arrays.asList(211, 201, 33, 33, 33);

        List<Integer> bossVals = Arrays.asList(201);
        Set<Integer> yingJiangVals = new HashSet<>();
        boolean hu7Dui = true;
        boolean daiFeng = true;

        int count = 10000;
        Map<Integer, List<Integer>> daTingList = new HashMap<>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            daTingList = calcDaTingMap(new ArrayList<>(vals), new HashSet<>(bossVals), daiFeng, yingJiangVals, hu7Dui);
        }
        long timeUse = System.currentTimeMillis() - start;
        System.out.println("count=" + count + "次,timeUse=" + timeUse + "ms" + ",avg=" + (timeUse * 1d / count) + "ms");
        System.out.println("daTingList=" + daTingList);
    }

}
