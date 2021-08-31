package com.sy599.game.qipai.bsmj.tool.hulib.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sy599.game.qipai.bsmj.rule.BsMj;
import com.sy599.game.qipai.bsmj.tool.hulib.core.handle.Hulib;
import com.sy599.game.qipai.bsmj.tool.hulib.core.handle.TableMgr;


public class HuUtil {
    public static final String source_dir = "bsmjtbl/";
    public static final String dump_file_dir = "E:\\work\\server\\code\\bjd2\\game-bsmj\\src\\main\\resources\\bsmjtbl\\";

    public static final Map<Integer,Integer> val2Index = new HashMap<>();
    public static void init() {
        TableMgr.getInstance().load();

        for (BsMj mj : BsMj.fullMj) {
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

    public static boolean isCanHu(List<BsMj> cards, int gui_num) {
        if ((cards.size() + gui_num) % 3 != 2) {
            return false;
        }
        int[] cardsArr = toCardArray(cards);
        return Hulib.getInstance().get_hu_info(cardsArr, 34, gui_num, false);
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

    public static boolean isCanHu7Dui(int[] cardsArr, int gui_num) {
        int needGui = 0;
        int cardNum = 0;
        for (int i = 0; i < cardsArr.length; i++) {
            needGui += cardsArr[i] % 2;
            cardNum += cardsArr[i];
        }
        return cardNum + gui_num == 14 && needGui <= gui_num;
    }

    public static List<BsMj> getLackPaiList(List<BsMj> cards, int gui_num) {
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

    public static int[] toCardArray(List<BsMj> cards) {
        int[] cardsArr = new int[34];
        Arrays.fill(cardsArr, 0);

        for (int i = 0; i < cards.size(); i++) {
            BsMj card = cards.get(i);
            cardsArr[getMjIndex(card)] += 1;
        }

        return cardsArr;
    }

    private static BsMj index2Majiang(int index) {
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
        return BsMj.getMajiangByVal(val);
    }

    private static List<BsMj> toRawCards(List<Integer> list) {
        List<BsMj> retList = new ArrayList<>();
        for (int index : list) {
            retList.add(index2Majiang(index));
        }
        return retList;
    }

    public static int getMjIndex(BsMj card) {
        return val2Index.get(card.getVal());
    }
    
    public static void main(String args[]) {
//    	com.sy599.game.qipai.bsmj.tool.hulib.core.gen_feng_table.Program.gen();
//    	com.sy599.game.qipai.bsmj.tool.hulib.core.gen_table.Program.gen();
//    	com.sy599.game.qipai.bsmj.tool.hulib.core.gen_ting_table.Program.gen();
    }


}
