package com.sy599.game.util.mj.serialize;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

class Test1 {
    private static long num1 = 0;
    private static long num2 = 0;
    private static BufferedWriter bw = null;

    /**
     * 字牌
     *
     * @param obj
     * @param surplus
     * @param cur
     */
    private static void createCombo2(int[] obj, int surplus, int maxsurplus, int cur) {
        if (cur >= obj.length)
            return;
        if (surplus == 0) {
            num1++;
            printYus(obj, maxsurplus);
            return;
        }

        //游标是否还可以增长
        boolean curAdd = true;
        if (cur + 1 == obj.length) {
            curAdd = false;
            if (surplus > 4)
                return;
        }
        //游标对应的数是否还可以增长
        boolean numAdd = true;
        if ((cur == 0 && obj[0] >= 8) || (cur > 0 && obj[cur] >= 4)) {
            numAdd = false;
        }

        if (curAdd) {
            createCombo2(obj.clone(), surplus, maxsurplus, cur + 1);
        }
        if (numAdd) {
            int[] clone = obj.clone();
            clone[cur]++;
            createCombo2(clone, surplus - 1, maxsurplus, cur);
        }
    }

    public static void printYus(int[] obj, int maxsurplus) {
        List<Integer> yus = new ArrayList<>();
        for (int i = 1; i < obj.length; i++) {
            for (int j = 0; j < obj[i]; j++) {
                yus.add(i);
            }
        }
        int bossNum = obj[0];
        CardType ct = new CardType(bossNum);
        List<CardType> cts = new ArrayList<>();
        MjTool.splitCards(yus, ct, cts);
        ct = MjTool.findUseLess(cts);
        if (ct != null) {
            long code = 0;
            for (int i = 0; i < obj.length; i++) {
                code = code * 10 + obj[i];
            }
            num2++;
            try {
                bw.write(obj[0] + "_" + code + "_" + ct.getRemainBoss());
                bw.newLine();
                if (num2 % 100 == 0)
                    bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {

        genCode();

    }

    /**
     * 生成code文件
     *
     * @throws Exception
     */
    public static void genCode() throws Exception {
        long l1 = System.currentTimeMillis();
        bw = new BufferedWriter(new FileWriter("E:\\pp\\huCode.txt"));
        int[] obj = new int[10];
        createCombo2(obj, 3, 3, 0);
        createCombo2(obj, 6, 6, 0);
        createCombo2(obj, 9, 9, 0);
        createCombo2(obj, 12, 12, 0);
        long l2 = System.currentTimeMillis();
        System.out.println("用时" + (l2 - l1) + "ms,共计" + num1 + "组合");
        System.out.println("有效组合:" + num2);
        bw.close();
    }
}

