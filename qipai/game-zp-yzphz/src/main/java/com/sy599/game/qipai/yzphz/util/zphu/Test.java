package com.sy599.game.qipai.yzphz.util.zphu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Test {

    public static void main(String[] args) {
        ZpConstant.init();

//        testHu();
        testTing();
    }

    public static void testHu() {
        boolean isSelfMo = true;
        List<Integer> valList = new ArrayList<>();
        int disCardVal = 0;

//        valList = new ArrayList<>(Arrays.asList(11, 12, 13, 4, 5, 6, 7, 8, 9, 17, 17, 18, 19, 20, 20, 10, 10));
//        isSelfMo = true;
//        disCardVal = 17;

//        valList = new ArrayList<>(Arrays.asList(11, 1, 1, 2, 3, 4, 11, 12, 13, 15, 5, 5, 6, 7, 8, 12, 17, 20, 14, 14));
//        isSelfMo = true;
//        disCardVal = 14;

//        valList = new ArrayList<>(Arrays.asList(11, 12, 13, 14));
//        isSelfMo = true;
//        disCardVal = 14;

//        valList = new ArrayList<>(Arrays.asList(1, 1, 1, 4, 4, 4, 7, 7, 7, 10, 10, 11, 12, 13, 16, 17, 200, 200, 200, 200));
//        isSelfMo = true;
//        disCardVal = 18;

        valList = new ArrayList<>(Arrays.asList(16, 16, 18, 18, 8, 9, 10, 7, 200));
        isSelfMo = true;
        disCardVal = 0;

//        valList = new ArrayList<>(Arrays.asList(2, 3, 6, 7, 9, 12, 15, 16, 16, 17, 17, 200, 200, 200));
//        isSelfMo = true;
//        disCardVal = 20;


        Collections.sort(valList);

        int[] cardArr = new int[21];
        int laiZiNum = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Integer val : valList) {
            sb.append(ZpConstant.getName(val)).append(",");
            if (val == ZpConstant.laiZiVal) {
                laiZiNum++;
            } else {
                cardArr[val] += 1;
            }
        }
        sb.append("]");
        sb.append(" , isSelfMo = ").append(isSelfMo);
        if (disCardVal > 0) {
            sb.append(" ,  disCard = ").append(ZpConstant.getName(disCardVal));
        }

        System.out.println();
        System.out.println();
        System.out.println(sb.toString());
        int count = 1000;
        ZpHuBean huBean = null;
        long start = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            huBean = new ZpHuBean(cardArr, disCardVal, laiZiNum, isSelfMo);
            huBean.calcHu();
        }
        if (huBean != null) {
            System.out.println();
            System.out.println();
            System.out.println("----------------------------------------------result-------------------------------------------------------------------");
            System.out.print("hu = " + (huBean.getHuList().size() > 0) + " , huCount = " + huBean.getHuList().size());
            System.out.println(" , calcCount = " + count + " , timeUse = " + (System.currentTimeMillis() - start) + " ms");

            System.out.println();
            System.out.println();
            System.out.println("----------------------------------------------huList-------------------------------------------------------------------");
            for (ZpHuLack hu : huBean.getHuList()) {
                System.out.println("------------" + hu);
            }

            System.out.println();
            System.out.println();
            System.out.println("----------------------------------------------maxHuList-----------------------------------------------------------------");
            System.out.println("hu = " + (huBean.getMaxHuList().size() > 0) + " , huCount = " + huBean.getMaxHuList().size());
            for (ZpHuLack hu : huBean.getMaxHuList()) {
                System.out.println("------" + hu);
            }
        }
    }

    public static void testTing() {
        List<Integer> valList = new ArrayList<>();
        int disCardVal = 0;

//        valList = new ArrayList<>(Arrays.asList(11, 12, 13, 4, 5, 6, 7, 8, 9, 17, 17, 18, 19, 20, 20, 10, 10));

//        valList = new ArrayList<>(Arrays.asList(11, 1, 1, 2, 3, 4, 11, 12, 13, 15, 5, 5, 6, 7, 8, 12, 17, 20, 14, 14));

//        valList = new ArrayList<>(Arrays.asList(11, 12, 13, 14));

        valList = new ArrayList<>(Arrays.asList(1, 1, 1, 4, 4, 4, 7, 7, 7, 10, 10, 11, 12, 13, 16, 17, 18, 200, 200, 200, 200));

        valList = new ArrayList<>(Arrays.asList(2, 3, 6, 7, 9, 12, 15, 16, 16, 17, 17, 20, 200, 200, 200));

        valList = new ArrayList<>(Arrays.asList(16, 16, 18, 18, 8, 9, 10, 7, 200));

        Collections.sort(valList);

        int[] cardArr = new int[21];
        int laiZiNum = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Integer val : valList) {
            sb.append(ZpConstant.getName(val)).append(",");
            if (val == ZpConstant.laiZiVal) {
                laiZiNum++;
            } else {
                cardArr[val] += 1;
            }
        }
        sb.append("]");
        if (disCardVal > 0) {
            sb.append(" ,  disCard = ").append(ZpConstant.getName(disCardVal));
        }

        System.out.println();
        System.out.println();
        System.out.println(sb.toString());
        int count = 1;
        ZpHuBean huBean = null;
        long start = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            huBean = new ZpHuBean(cardArr, laiZiNum);
            huBean.calcDaTingMap();
        }
        if (huBean != null) {
            System.out.println();
            System.out.println();
            System.out.println("----------------------------------------------result-------------------------------------------------------------------");
            System.out.println(" calcCount = " + count + " , timeUse = " + (System.currentTimeMillis() - start) + " ms");

            System.out.println();
            System.out.println();
            System.out.println("----------------------------------------------tingInfo------------------------------------------------------------------");
            Map<Integer, List<Integer>> tingInfo = huBean.getDaTingMap();
            if (tingInfo.size() > 0) {
                for (Integer key : tingInfo.keySet()) {
                    sb = new StringBuilder();
                    sb.append(" dis = ").append(ZpConstant.getName(key));
                    sb.append(" , ting = ").append("[");
                    for (Integer t : tingInfo.get(key)) {
                        sb.append(ZpConstant.getName(t)).append(",");
                    }
                    sb.append("]");
                    System.out.println(sb.toString());
                }

            }
        }
    }

}
