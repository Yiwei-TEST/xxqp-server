package com.sy599.game.qipai.tcdpmj.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sy599.game.qipai.tcdpmj.constant.TcdpMj;
import org.apache.commons.lang3.StringUtils;

import com.sy599.game.qipai.tcdpmj.rule.MjIndexArr;

public class MjQipaiTool {


    /**
     * 将array组合成用delimiter分隔的字符串
     *
     * @param delimiter
     * @return String
     */
    public static List<TcdpMj> explodeMajiang(String str, String delimiter) {
        List<TcdpMj> list = new ArrayList<>();
        if (StringUtils.isBlank(str) || str.equals("null") || str.equals("undefined"))
            return list;
        String strArray[] = str.split(delimiter);

        for (String val : strArray) {
            TcdpMj majiang = null;
            if (val.startsWith("mj")) {
                majiang = TcdpMj.valueOf(TcdpMj.class, val);
            } else {
                Integer intVal = (Integer.valueOf(val));
                if (intVal == 0) {
                    continue;
                }
                majiang = TcdpMj.getMajang(intVal);
            }
            list.add(majiang);
        }
        return list;
    }

    /**
     * 麻将转化为majiangIds
     *
     * @param majiangs
     * @return
     */
    public static List<Integer> toMajiangIds(List<TcdpMj> majiangs) {
        List<Integer> majiangIds = new ArrayList<>();
        if (majiangs == null) {
            return majiangIds;
        }
        for (TcdpMj majiang : majiangs) {
            majiangIds.add(majiang.getId());
        }
        return majiangIds;
    }

    /**
     * 麻将转化为majiangIds
     *
     * @param majiangs
     * @return
     */
    public static List<Integer> toMajiangVals(List<TcdpMj> majiangs) {
        List<Integer> majiangIds = new ArrayList<>();
        if (majiangs == null) {
            return majiangIds;
        }
        for (TcdpMj majiang : majiangs) {
            majiangIds.add(majiang.getVal());
        }
        return majiangIds;
    }

    /**
     * 麻将Id转化为麻将
     *
     * @param majiangIds
     * @return
     */
    public static List<TcdpMj> toMajiang(List<Integer> majiangIds) {
        if (majiangIds == null) {
            return new ArrayList<>();
        }
        List<TcdpMj> majiangs = new ArrayList<>();
        for (int majiangId : majiangIds) {
            if (majiangId == 0) {
                continue;
            }
            majiangs.add(TcdpMj.getMajang(majiangId));
        }
        return majiangs;
    }

    /**
     * 得到最大相同数
     *
     * @param card_index
     * @param list
     */
    public static void getMax(MjIndexArr card_index, List<TcdpMj> list) {
        Map<Integer, List<TcdpMj>> majiangMap = new HashMap<Integer, List<TcdpMj>>();
        for (int i = 0; i < list.size(); i++) {
            TcdpMj majiang = list.get(i);
            List<TcdpMj> count = null;
            if (majiangMap.containsKey(majiang.getVal())) {
                count = majiangMap.get(majiang.getVal());
            } else {
                count = new ArrayList<>();
                majiangMap.put(majiang.getVal(), count);
            }
            count.add(majiang);
        }
        for (int majiangVal : majiangMap.keySet()) {
            List<TcdpMj> majiangList = majiangMap.get(majiangVal);
            switch (majiangList.size()) {
                case 1:
                    card_index.addMajiangIndex(0, majiangList, majiangVal);
                    break;
                case 2:
                    card_index.addMajiangIndex(1, majiangList, majiangVal);
                    break;
                case 3:
                    card_index.addMajiangIndex(2, majiangList, majiangVal);
                    break;
                case 4:
                    card_index.addMajiangIndex(3, majiangList, majiangVal);
                    break;
                case 5://听牌时加入一张导致5张牌
                    card_index.addMajiangIndex(4, majiangList, majiangVal);
                    break;
            }
        }
    }



    public static List<Integer> dropHongzhongVal(List<Integer> copy) {
        List<Integer> hongzhong = new ArrayList<>();
        Iterator<Integer> iterator = copy.iterator();
        while (iterator.hasNext()) {
            Integer majiang = iterator.next();
            if (majiang > 200) {
                hongzhong.add(majiang);
                iterator.remove();
            }
        }
        return hongzhong;
    }

    /**
     * 得到某个值的麻将
     *
     * @param copy
     * @return
     */
    public static List<TcdpMj> getVal(List<TcdpMj> copy, int val) {
        List<TcdpMj> hongzhong = new ArrayList<>();
        Iterator<TcdpMj> iterator = copy.iterator();
        while (iterator.hasNext()) {
            TcdpMj majiang = iterator.next();
            if (majiang.getVal() == val) {
                hongzhong.add(majiang);
            }
        }
        return hongzhong;
    }


}
