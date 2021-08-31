package com.sy599.game.qipai.yjghz.rule;

import com.sy599.game.qipai.yjghz.constant.YjGhzCard;
import com.sy599.game.qipai.yjghz.tool.YjGhzTool;
import com.sy599.game.util.JacksonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 判断牌型
 *
 * @author lc
 */
public class YjGhzCardIndexArr {
    YjGhzIndex[] a = new YjGhzIndex[4];

    public void addPaohzCardIndex(int count, List<YjGhzCard> majiangList, int val) {
        if (a[count] == null) {
            a[count] = new YjGhzIndex();
        }
        a[count].addPaohz(val, majiangList);
        a[count].addVal(val);
    }

    /**
     * 根据牌的张数得到牌
     *
     * @param size 张数
     * @return
     */
    public Map<Integer, List<YjGhzCard>> getPaohzCardMap(int size) {
        Map<Integer, List<YjGhzCard>> map = new HashMap<>();
        for (int i = 0; i < a.length; i++) {
            if (size <= i + 1) {
                YjGhzIndex majiangIndex = a[i];
                if (majiangIndex != null) {
                    map.putAll(majiangIndex.getPaohzValMap());
                }
            }

        }
        return map;
    }

    /**
     * 获得所有门子（包括对子）
     *
     * @return
     */
    public List<YjGhzMenzi> getMenzis(boolean containDuizi) {
        List<YjGhzMenzi> menzis = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < a.length; i++) {
            if (i >= 0) {
                YjGhzIndex majiangIndex = a[i];
                if (majiangIndex != null) {
                    if (containDuizi && i >= 1) {
                        for (int value : majiangIndex.getValList()) {
                            List<Integer> duizi = new ArrayList<>();
                            duizi.add(value);
                            duizi.add(value);
                            YjGhzMenzi menzi = new YjGhzMenzi(duizi, 1);
                            menzis.add(menzi);
                        }
                    }
                    values.addAll(majiangIndex.getValList());
                }
            }
        }
        for (int i = 0; i < values.size(); i++) {
            for (int j = i + 1; j < values.size(); j++) {
                if (YjGhzTool.c2710List.contains(values.get(i)) && YjGhzTool.c2710List.contains(values.get(j)) || (values.get(i) >= 100 && values.get(j) >= 100 && YjGhzTool.c2710List.contains(values.get(i) % 100) && YjGhzTool.c2710List.contains(values.get(j) % 100))) {
                    List<Integer> menzi2710 = new ArrayList<>();
                    menzi2710.add(values.get(i));
                    menzi2710.add(values.get(j));
                    YjGhzMenzi menzi = new YjGhzMenzi(menzi2710, 2);
                    menzis.add(0, menzi);
                } else if (Math.abs(values.get(i) - values.get(j)) <= 2) {
                    List<Integer> menziOrd = new ArrayList<>();
                    menziOrd.add(values.get(i));
                    menziOrd.add(values.get(j));
                    YjGhzMenzi menzi = new YjGhzMenzi(menziOrd, 0);
                    menzis.add(0, menzi);
                }
            }
        }
        return menzis;
    }

    /**
     * 获得所有对子
     *
     * @return
     */
    public List<List<YjGhzCard>> getDuizis() {
        List<List<YjGhzCard>> list = new ArrayList<>();
        for (int i = 0; i < a.length; i++) {
            if (2 <= i + 1) {
                YjGhzIndex index = a[i];
                if (index != null) {
                    for (Entry<Integer, List<YjGhzCard>> entry : index.getPaohzValMap().entrySet()) {
                        // if(PaohuziTool.c2710Listentry.getKey())
                        int val = entry.getValue().get(0).getVal();
                        if (!YjGhzTool.c2710List.contains(val)) {
                            list.add(0, entry.getValue());
                        } else {
                            list.add(entry.getValue());
                        }
                    }
                }
            }

        }
        return list;
    }

    /**
     * 牌的张数大于2的 (对子数)
     *
     * @return
     */
    public int getDuiziNum() {
        int num = 0;
        for (int i = 1; i < a.length; i++) {
            YjGhzIndex majiangIndex = a[i];
            if (majiangIndex == null) {
                continue;
            }
            if (i == 3) {
                num += majiangIndex.getLength() * 2;
            } else {
                num += majiangIndex.getLength();

            }
        }
        return num;
    }

    /**
     * 牌的张数大于3的 (刻字数)
     *
     * @return
     */
    public int getKeziNum() {
        int num = 0;
        for (int i = 2; i < a.length; i++) {
            YjGhzIndex majiangIndex = a[i];
            if (majiangIndex == null) {
                continue;
            }
            num += majiangIndex.getLength();

        }
        return num;
    }

    public List<YjGhzCard> getKeziList() {
        List<YjGhzCard> list = new ArrayList<>();
        for (int i = 2; i < a.length; i++) {
            YjGhzIndex majiangIndex = a[i];
            if (majiangIndex == null) {
                continue;
            }
            list.addAll(majiangIndex.getPaohzList());

        }
        return list;
    }

    /**
     * 得到牌
     *
     * @param index 0一张 , 1 二张 , 2 三张 , 3 四张
     * @return
     */
    public YjGhzIndex getPaohzCardIndex(int index) {
        YjGhzIndex majiangIndex = a[index];
        if (majiangIndex == null) {
            // return new PaohzCardIndex();
        }
        return majiangIndex;
    }

    public YjGhzIndex[] getA() {
        return a;
    }

    public String tostr() {
        int i = 0;
        String str = "";
        for (YjGhzIndex majiang : a) {
            if (majiang == null) {
                continue;
            }
            str += i + "  " + JacksonUtil.writeValueAsString(majiang.getValList()) + " -->" + JacksonUtil.writeValueAsString(majiang.getPaohzValMap()) + "\n";
            i++;
        }
        return str;

    }
}
