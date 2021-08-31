package com.sy599.game.qipai.zzmj.tool.hulib.util;


import com.sy599.game.qipai.zzmj.rule.ZzMj;
import com.sy599.game.util.mj.core.handle.Hulib;

import java.util.*;


public class HuUtil {
    public static final String source_dir = "zzmjtbl/";
    public static final String dump_file_dir = "D:\\develop\\workspace\\2d\\qipai_all_2\\game-zzmj\\src\\main\\resources\\zzmjtbl\\";
    public static final Map<Integer,Integer> val2Index = new HashMap<>();
    public static void init() {
//        TableMgr.getInstance().load();

        for (ZzMj mj : ZzMj.fullMj) {
            //1 条 2 筒 3万  301东风 311南风  321西风 331北风 201红中  211发财  221白板
            int val = mj.getVal();
            int type = 0;
            int number = 0;
            int color = mj.getHuase();
            if (color <= 3) {
                type = color;
                number = val % 10;
            } else if (color >= 30) {
                type = 4;
                number = color % 10 + 1;
            } else {
                type = 4;
                number = color % 10 + 5;
            }
            int index = (type - 1) * 9 + number - 1;
            val2Index.put(val, index);
        }
    }

    public static boolean isCanHu(int[] cardsArr, int gui_num) {
        int cardNum = 0;
        for (int i = 0; i < cardsArr.length; i++) {
            cardNum += cardsArr[i];
        }
        if ((cardNum + gui_num) % 3 != 2) {
            return false;
        }
        return Hulib.getInstance().get_hu_info(cardsArr, 34, gui_num, false);
    }
    public static boolean isCanHu(List<ZzMj> cards, int gui_num) {
        if ((cards.size() + gui_num) % 3 != 2) {
            return false;
        }
        int[] cardsArr = toCardArray(cards);
        return Hulib.getInstance().get_hu_info(cardsArr, 34, gui_num, false);
    }

    public static List<ZzMj> getLackPaiList(List<ZzMj> cards, int gui_num) {
        int[] cardsArr = toCardArray(cards);
        boolean any = false;
        if (gui_num > 0) {
            any = Hulib.getInstance().get_hu_info(cardsArr, 34, gui_num - 1, true);
        }
        List<Integer> tingList;
        if (any) {
            tingList = new ArrayList<>();
            tingList.add(-1);
        } else {
            tingList = Hulib.getInstance().get_ting_info(cardsArr, 34, gui_num);
        }
        return toRawCards(tingList);
    }



    public static int[] toCardArray(List<ZzMj> cards) {
        int[] cardsArr = new int[34];
        Arrays.fill(cardsArr, 0);

        for (int i = 0; i < cards.size(); i++) {
            ZzMj card = cards.get(i);
            //1 条 2 筒 3万  301东风 311南风  321西风 331北风 201红中  211发财  221白板
            int val = card.getVal();
            int type = 0;
            int number = 0;
            int color = card.getHuase();
            if (color <= 3) {
                type = color;
                number = val % 10;
            } else if (color >= 30) {
                type = 4;
                number = color % 10 + 1;
            } else {
                type = 4;
                number = color % 10 + 5;
            }


            int index = (type - 1) * 9 + number - 1;
            cardsArr[index] += 1;
        }

        return cardsArr;
    }

    private static ZzMj index2Majiang(int index) {
        int type = index / 9 + 1;
        int number = index % 9 + 1;
        int val = 0;
        if (type <= 3) {
            val = type * 10 + number;
        } else {
            if (number <= 4) {
                val = 300 + (number - 1) * 10 + 1;
            } else {
                val = 200 + (number - 5) * 10 + 1;
            }
        }
        return ZzMj.getMajiangByVal(val);
    }

    private static List<ZzMj> toRawCards(List<Integer> list) {
        List<ZzMj> retList = new ArrayList<>();
        for (int index : list) {
            retList.add(index2Majiang(index));
        }
        return retList;
    }

    public static boolean isCanHu7Dui(int[] cardsArr, int gui_num) {
        int needGui = 0;
        int cardNum = 0;
        for (int i = 0; i < cardsArr.length; i++) {
            needGui += cardsArr[i] % 2;
            cardNum += cardsArr[i];
        }
        return cardNum + gui_num == 14 && needGui <= gui_num;
    }

    public static boolean isCanHu7Dui(int[] cardsArr) {
        int cardNum = 0;
        for (int i = 0; i < cardsArr.length; i++) {
            cardNum += cardsArr[i];
        }
        return cardNum == 14;
    }

    public static int getMjIndex(ZzMj card) {
        return val2Index.get(card.getVal());
    }
}
