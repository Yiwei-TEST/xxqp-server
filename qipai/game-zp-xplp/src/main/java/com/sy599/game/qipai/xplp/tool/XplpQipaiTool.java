package com.sy599.game.qipai.xplp.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sy599.game.qipai.xplp.rule.XpLp;
import org.apache.commons.lang3.StringUtils;

import com.sy599.game.qipai.xplp.rule.XplpIndexArr;

public class XplpQipaiTool {
    /**
     * 麻将转化为majiangIds
     *
     * @param majiangs
     * @return
     */
    public static List<Integer> toRepeatMajiangVals(List<XpLp> majiangs) {
        List<Integer> majiangVals = new ArrayList<>();
        if (majiangs == null) {
            return majiangVals;
        }
        for (XpLp majiang : majiangs) {
            if (!majiangVals.contains(majiang.getVal())) {
                majiangVals.add(majiang.getVal());

            }
        }
        return majiangVals;
    }

    /**
     * 检查麻将是否有重复
     *
     * @param majiangs
     * @return
     */
    public static boolean isMajiangRepeat(List<XpLp> majiangs) {
        if (majiangs == null) {
            return false;
        }

        Map<Integer, Integer> map = new HashMap<>();
        for (XpLp mj : majiangs) {
            int count = 0;
            if (map.containsKey(mj.getId())) {
                count = map.get(mj.getId());
            }
            map.put(mj.getId(), count + 1);
        }
        for (int count : map.values()) {
            if (count > 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 麻将转化为majiangIds
     *
     * @param majiangs
     * @return
     */
    public static String toMajiangStrs(List<XpLp> majiangs) {
        StringBuffer sb = new StringBuffer();
        if (majiangs == null) {
            return sb.toString();
        }
        for (XpLp majiang : majiangs) {
            sb.append(majiang.getId()).append(",");

        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * 将array组合成用delimiter分隔的字符串
     *
     * @param array
     * @param delimiter
     * @return String
     */
    public static String implodeMajiang(List<XpLp> array, String delimiter) {
        StringBuilder sb = new StringBuilder("");
        for (XpLp i : array) {
            sb.append(i.getId());
            sb.append(delimiter);
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
        return sb.toString();
    }

    /**
     * 麻将val的个数
     *
     * @param majiangs
     * @param majiangVal
     * @return
     */
    public static int getMajiangCount(List<XpLp> majiangs, int majiangVal) {
        int count = 0;
        for (XpLp majiang : majiangs) {
            if (majiang.getVal() == majiangVal) {
                count++;
            }
        }
        return count;
    }

    /**
     * 麻将val的List
     *
     * @param majiangs
     * @param majiangVal
     * @return
     */
    public static List<XpLp> getMajiangList(List<XpLp> majiangs, int majiangVal) {
        List<XpLp> list = new ArrayList<>();
        for (XpLp majiang : majiangs) {
            if (majiang.getVal() == majiangVal) {
                list.add(majiang);
            }
        }
        return list;
    }

    public static int findIndexByVal(List<XpLp> copy, int val) {
        int index = -1;
        int count = 0;
        for (XpLp majiang : copy) {
            if (majiang.getVal() == val) {
                index = count;
                break;
            }
            count++;
        }
        return index;
    }

    public static Integer findMajiangIdByVal(List<Integer> copy, int val) {
        for (int majiangId : copy) {
            XpLp majiang = XpLp.getMajang(majiangId);
            if (majiang.getVal() == val) {
                return majiangId;
            }
        }
        return 0;
    }

    public static XpLp findMajiangByVal(List<XpLp> copy, int val) {
        for (XpLp majiang : copy) {
            if (majiang.getVal() == val) {
                return majiang;
            }
        }
        return null;
    }

    /**
     * 麻将转化为Map<val,valNum>
     *
     * @param majiangs
     * @return
     */
    public static Map<Integer, Integer> toMajiangValMap(List<XpLp> majiangs) {
        Map<Integer, Integer> majiangIds = new HashMap<>();
        if (majiangs == null) {
            return majiangIds;
        }
        for (XpLp majiang : majiangs) {

            if (majiangIds.containsKey(majiang.getVal())) {
                majiangIds.put(majiang.getVal(), majiangIds.get(majiang.getVal()) + 1);
            } else {
                majiangIds.put(majiang.getVal(), 1);
            }
        }
        return majiangIds;
    }

    public static List<XpLp> findMajiangByVals(List<XpLp> majiangs, List<Integer> vals) {
        List<XpLp> result = new ArrayList<>();
        for (int val : vals) {
            for (XpLp majiang : majiangs) {
                if (majiang.getVal() == val) {
                    result.add(majiang);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 将array组合成用delimiter分隔的字符串
     *
     * @param array
     * @param delimiter
     * @return String
     */
    public static List<XpLp> explodeMajiang(String str, String delimiter) {
        List<XpLp> list = new ArrayList<>();
        if (StringUtils.isBlank(str) || str.equals("null") || str.equals("undefined"))
            return list;
        String strArray[] = str.split(delimiter);

        for (String val : strArray) {
            XpLp majiang = null;
            if (val.startsWith("mj")) {
                majiang = XpLp.valueOf(XpLp.class, val);
            } else {
                Integer intVal = (Integer.valueOf(val));
                if (intVal == 0) {
                    continue;
                }
                majiang = XpLp.getMajang(intVal);
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
    public static List<Integer> toMajiangIds(List<XpLp> majiangs) {
        List<Integer> majiangIds = new ArrayList<>();
        if (majiangs == null) {
            return majiangIds;
        }
        for (XpLp majiang : majiangs) {
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
    public static List<Integer> toMajiangVals(List<XpLp> majiangs) {
        List<Integer> majiangIds = new ArrayList<>();
        if (majiangs == null) {
            return majiangIds;
        }
        for (XpLp majiang : majiangs) {
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
    public static List<XpLp> toMajiang(List<Integer> majiangIds) {
        if (majiangIds == null) {
            return new ArrayList<>();
        }
        List<XpLp> majiangs = new ArrayList<>();
        for (int majiangId : majiangIds) {
            if (majiangId == 0) {
                continue;
            }
            majiangs.add(XpLp.getMajang(majiangId));
        }
        return majiangs;
    }

    /**
     * 得到最大相同数
     *
     * @param card_index
     * @param list
     */
    public static void getMax(XplpIndexArr card_index, List<XpLp> list) {
        Map<Integer, List<XpLp>> majiangMap = new HashMap<Integer, List<XpLp>>();
        for (int i = 0; i < list.size(); i++) {
            XpLp majiang = list.get(i);
            List<XpLp> count = null;
            if (majiangMap.containsKey(majiang.getVal())) {
                count = majiangMap.get(majiang.getVal());
            } else {
                count = new ArrayList<>();
                majiangMap.put(majiang.getVal(), count);
            }
            count.add(majiang);
        }
        for (int majiangVal : majiangMap.keySet()) {
            List<XpLp> majiangList = majiangMap.get(majiangVal);
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

    /**
     * 去掉麻将中指定的val
     *
     * @param copy
     * @return
     */
    public static List<XpLp> dropMajiang(List<XpLp> copy, List<Integer> valList) {
        List<XpLp> hongzhong = new ArrayList<>();
        Iterator<XpLp> iterator = copy.iterator();
        while (iterator.hasNext()) {
            XpLp majiang = iterator.next();
            if (valList.contains(majiang.getVal())) {
                hongzhong.add(majiang);
                iterator.remove();
            }
        }
        return hongzhong;
    }

    /**
     * 去掉麻将中指定的val
     *
     * @param copy
     * @return
     */
    public static List<XpLp> dropMajiangId(List<Integer> copy, List<Integer> valList) {
        List<XpLp> hongzhong = new ArrayList<>();
        Iterator<Integer> iterator = copy.iterator();
        while (iterator.hasNext()) {
            Integer majiangId = iterator.next();
            XpLp majiang = XpLp.getMajang(majiangId);
            if (valList.contains(majiang.getVal())) {
                hongzhong.add(majiang);
                iterator.remove();
            }
        }
        return hongzhong;
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
     * 删掉某个值
     *
     * @param copy
     * @return
     */
    public static List<XpLp> dropVal(List<XpLp> copy, int val, int count) {
        List<XpLp> hongzhong = new ArrayList<>();
        Iterator<XpLp> iterator = copy.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            XpLp majiang = iterator.next();
            if (majiang.getVal() == val) {
                i++;
                hongzhong.add(majiang);
                iterator.remove();
                if (count == i) {
                    break;
                }
            }
        }
        return hongzhong;
    }

    /**
     * 删掉某个值
     *
     * @param copy
     * @return
     */
    public static List<XpLp> dropVal(List<XpLp> copy, int val) {
        List<XpLp> hongzhong = new ArrayList<>();
        Iterator<XpLp> iterator = copy.iterator();
        while (iterator.hasNext()) {
            XpLp majiang = iterator.next();
            if (majiang.getVal() == val) {
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
    public static List<XpLp> getVal(List<XpLp> copy, int val) {
        List<XpLp> hongzhong = new ArrayList<>();
        Iterator<XpLp> iterator = copy.iterator();
        while (iterator.hasNext()) {
            XpLp majiang = iterator.next();
            if (majiang.getVal() == val) {
                hongzhong.add(majiang);
            }
        }
        return hongzhong;
    }

    /**
     * 相同的麻将
     *
     * @param majiangs 麻将牌
     * @param majiang  麻将
     * @param num      想要的数量
     * @return
     */
    public static List<XpLp> getSameMajiang(List<XpLp> majiangs, XpLp majiang, int num) {
        List<XpLp> hongzhong = new ArrayList<>();
        int i = 0;
        for (XpLp maji : majiangs) {
            if (maji.getVal() == majiang.getVal()) {
                hongzhong.add(maji);
                i++;
            }
            if (i >= num) {
                break;
            }
        }
        return hongzhong;

    }

    /**
     * 先去某个值
     *
     * @param copy
     * @return
     */
    public static List<XpLp> dropMjId(List<XpLp> copy, int id) {
        List<XpLp> hongzhong = new ArrayList<>();
        Iterator<XpLp> iterator = copy.iterator();
        while (iterator.hasNext()) {
            XpLp majiang = iterator.next();
            if (majiang.getId() == id) {
                hongzhong.add(majiang);
                iterator.remove();
            }
        }
        return hongzhong;
    }
}
