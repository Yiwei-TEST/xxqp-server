package com.sy599.game.qipai.tcgd.util;

import org.apache.commons.lang.ArrayUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TcgdSfNew {
    /**
     * 获取初始化牌局的所有牌
     *
     * @param
     * @return
     */
    public static List<String> getInitArr() {
        List<String> initList = new ArrayList<String>();
        // 14表示A 15表示2 16表示王
        for (int j = 0; j < 2; j++) {
            for (int i = 3; i <= 15; i++) {
                initList.add("B" + i);
                initList.add("R" + i);
                initList.add("M" + i);
                initList.add("F" + i);
            }
            initList.add("W16");
            initList.add("w16");
        }
        List<String> ranList = new ArrayList<String>();
        while (initList.size() > 0) {
            Random random = new Random();
            int ranNum = random.nextInt(initList.size());
            ranList.add(initList.get(ranNum));
            initList.remove(ranNum);
        }
        return ranList;
    }

    public static List<String> getInitFen() {
        List<String> initList = new ArrayList<String>();
        //B R M F
        initList.add("B13");
        initList.add("B13");
        initList.add("B10");
        initList.add("B10");
        initList.add("B5");
        initList.add("B5");

        initList.add("R13");
        initList.add("R13");
        initList.add("R10");
        initList.add("R10");
        initList.add("R5");
        initList.add("R5");

        initList.add("M13");
        initList.add("M13");
        initList.add("M10");
        initList.add("M10");
        initList.add("M5");
        initList.add("M5");

        initList.add("F13");
        initList.add("F13");
        initList.add("F10");
        initList.add("F10");
        initList.add("F5");
        initList.add("F5");

        return initList;
    }

    /**
     * 初始化手牌
     *
     * @param list
     * @param num  起手发num张牌
     * @return
     */
    public static List<String> initShouPai(List<String> list, int num) {
        List<String> ranList = new ArrayList<String>();
        while (list.size() > 0 && ranList.size() < num) {
            Random random = new Random();
            int ranNum = random.nextInt(list.size());
            ranList.add(list.get(ranNum));
            list.remove(ranNum);
        }
        return ranList;
    }

    /**
     * 牌转短int数组
     *
     * @param pai
     * @return
     */
    public static int[] paiToShortAry(List<String> pai) {
        int[] ary = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (int i = 0; i < pai.size(); i++) {
            int p = getNumbers(pai.get(i));
            ary[p - 3]++;
        }
        return ary;
    }

    /**
     * 截取数值
     *
     * @param content
     * @return
     */
    public static Integer getNumbers(String content) {
        Integer i = 0;
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            i = Integer.parseInt(matcher.group(0));
        }
        return i;
    }

    /**
     * 截取花色 返回对应花色的值，方便排序
     *
     * @param content
     * @return 大王6 小王5  黑红梅方  4321
     */
    public static Integer getColor(String content) {
        String color = String.valueOf(content.charAt(0));
        if (color.equals("W")) {
            return 6;
        } else if (color.equals("w")) {
            return 5;
        } else if (color.equals("B")) {
            return 4;
        } else if (color.equals("R")) {
            return 3;
        } else if (color.equals("M")) {
            return 2;
        } else {
            return 1;
        }
    }

    /**
     * @param pai
     * @param type asc desc
     * @return
     */
    public static List<String> sortList(List<String> pai, String type) {
        for (int i = 0; i < pai.size() - 1; i++) {
            for (int j = 0; j < pai.size() - i - 1; j++) {
                if ("asc".equals(type)) {
                    if (comparePai(pai.get(j), pai.get(j + 1)) > 0) { // 把小的值交换到后面
                        String temp = pai.get(j);
                        pai.set(j, pai.get(j + 1));
                        pai.set(j + 1, temp);
                    }
                } else if ("desc".equals(type)) {
                    if (comparePai(pai.get(j), pai.get(j + 1)) < 0) { // 把小的值交换到后面
                        String temp = pai.get(j);
                        pai.set(j, pai.get(j + 1));
                        pai.set(j + 1, temp);
                    }
                }
            }
        }
        return pai;
    }

    /**
     * 自定义牌比较大小
     *
     * @param arg0
     * @param arg1
     * @return
     */
    public static int comparePai(String arg0, String arg1) {
        if (arg0.equals(arg1)) {
            return 0;
        } else {
            if (getNumbers(arg0) > getNumbers(arg1)) {
                return 1;
            } else if (getNumbers(arg0) < getNumbers(arg1)) {
                return -1;
            } else {// 两者数值相等，判断花色
                if (getColor(arg0) > getColor(arg1)) {
                    return 1;
                } else if (getColor(arg0) < getColor(arg1)) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }

    public static boolean is8zha(List<String> pai) {
        boolean bl = false;
        if (pai.size() == 8) {
            int temp = 0;
            int flag = 0;
            for (String p : pai) {
                int ip = getNumbers(p);
                if (temp != ip) {
                    temp = ip;
                    flag++;
                }
            }
            if (flag == 1) {
                bl = true;
            }
        }
        return bl;
    }

    public static boolean is7zha(List<String> pai) {
        boolean bl = false;
        if (pai.size() == 7) {
            int temp = 0;
            int flag = 0;
            for (String p : pai) {
                int ip = getNumbers(p);
                if (temp != ip) {
                    temp = ip;
                    flag++;
                }
            }
            if (flag == 1) {
                bl = true;
            }
        }
        return bl;
    }

    public static boolean is6zha(List<String> pai) {
        boolean bl = false;
        if (pai.size() == 6) {
            int temp = 0;
            int flag = 0;
            for (String p : pai) {
                int ip = getNumbers(p);
                if (temp != ip) {
                    temp = ip;
                    flag++;
                }
            }
            if (flag == 1) {
                bl = true;
            }
        }
        return bl;
    }

    public static boolean is4zha(List<String> pai) {
        boolean bl = false;
        if (pai.size() == 4) {
            int temp = 0;
            int flag = 0;
            for (String p : pai) {
                int ip = getNumbers(p);
                if (temp != ip) {
                    temp = ip;
                    flag++;
                }
            }
            if (flag == 1) {
                bl = true;
            }
        }
        return bl;
    }

    public static boolean is5zha(List<String> pai) {
        boolean bl = false;
        if (pai.size() == 5) {
            int temp = 0;
            int flag = 0;
            for (String p : pai) {
                int ip = getNumbers(p);
                if (temp != ip) {
                    temp = ip;
                    flag++;
                }
            }
            if (flag == 1) {
                bl = true;
            }
        }
        return bl;
    }

    public static boolean is4zhang(List<String> pai) {
        boolean bl = false;
        if (pai.size() == 4) {
            if (getNumbers(pai.get(0)) == getNumbers(pai.get(1)) && getNumbers(pai.get(0)) == getNumbers(pai.get(2)) && getNumbers(pai.get(0)) == getNumbers(pai.get(3))) {
                bl = true;
            }
            for (String p : pai) {
                int ip = getNumbers(p);
                if (ip == 16) {
                    bl = false;
                    break;
                }
            }
        }
        return bl;
    }

    public static boolean is3zhang(List<String> pai) {
        boolean bl = false;
        if (pai.size() == 3) {
            if (getNumbers(pai.get(0)) == getNumbers(pai.get(1)) && getNumbers(pai.get(0)) == getNumbers(pai.get(2))) {
                bl = true;
            }
            for (String p : pai) {
                int ip = getNumbers(p);
                if (ip == 16) {
                    bl = false;
                    break;
                }
            }
        }
        return bl;
    }

    public static boolean is510k(List<String> pai) {
        boolean bl = false;
        if (pai.size() == 3) {
            pai = sortList(pai, "asc");
            if (getNumbers(pai.get(0)) == 5 && getNumbers(pai.get(1)) == 10 && getNumbers(pai.get(2)) == 13) {
                bl = true;
            }
        }
        return bl;
    }

    public static boolean isHt510k(List<String> pai) {
        boolean bl = false;
        if (pai.size() == 3) {
            if (getColor(pai.get(0)) == 4 && getColor(pai.get(1)) == 4 && getColor(pai.get(2)) == 4 && is510k(pai)) {
                bl = true;
            }
        }
        return bl;
    }

    public static boolean isHx510k(List<String> pai) {
        boolean bl = false;//B5 B10 B13
        if (pai.size() == 3) {
            if (getColor(pai.get(0)) == 3 && getColor(pai.get(1)) == 3 && getColor(pai.get(2)) == 3 && is510k(pai)) {
                bl = true;
            }
        }
        return bl;
    }

    public static boolean isMh510k(List<String> pai) {
        boolean bl = false;
        if (pai.size() == 3) {
            if (getColor(pai.get(0)) == 2 && getColor(pai.get(1)) == 2 && getColor(pai.get(2)) == 2 && is510k(pai)) {
                bl = true;
            }
        }
        return bl;
    }

    public static boolean isFk510k(List<String> pai) {
        boolean bl = false;
        if (pai.size() == 3) {
            if (getColor(pai.get(0)) == 1 && getColor(pai.get(1)) == 1 && getColor(pai.get(2)) == 1 && is510k(pai)) {
                bl = true;
            }
        }
        return bl;
    }

    public static boolean isMin510k(List<String> pai) {
        boolean bl = false;
        if (pai.size() == 3) {
            if (getColor(pai.get(0)) != getColor(pai.get(1)) || getColor(pai.get(0)) != getColor(pai.get(2)) || getColor(pai.get(1)) != getColor(pai.get(2))) {
                if (is510k(pai)) {
                    bl = true;
                }
            }
        }
        return bl;
    }

    public static boolean is4wang(List<String> pai) {
        boolean bl = true;
        if (pai.size() == 4) {
            for (String p : pai) {
                if (getNumbers(p) != 16) {
                    bl = false;
                    break;
                }
            }
        } else {
            bl = false;
        }
        return bl;
    }

    public static boolean isTongHuaShun(List<String> pai) {
        boolean bl = true;
        if (pai.size() >= 5) {
            int col = getColor(pai.get(0));// b h m f = 4321
            for (String p : pai) {
                if (getColor(p) != col) {
                    bl = false;
                    break;
                }
            }
        } else {
            bl = false;
        }
        return bl;
    }

    public static String isShunzi(List<String> pai) {
        if (pai.size() == 0) {
            return "false";
        }

        pai = sortList(pai, "asc");
        String bool = "false";
        ;
        if (pai.size() < 5) {
            bool = "false";
            ;
            return "false";
        }
        //A 2 也能组成顺子 既能 12345  也能910JQKA2
        List<String> ls1 = new ArrayList<>();
        List<String> ls2 = new ArrayList<>();
        boolean turnflag = true;
        int[] ary = paiToShortAry(pai);
        for (int i = 0; i < pai.size(); i++) {
            int num = getNumbers(pai.get(i));

            if (num == 14 || num == 15) {
                ls1.add(pai.get(i).substring(0, 1) + String.valueOf(num - 13));
            } else {
                ls1.add(pai.get(i));
            }
            ls2.add(pai.get(i));
        }
        sortList(ls1, "asc");
        //是否为顺子  是否为同花顺 同花顺等价且大于炸弹
        boolean flag = true;
        for (int i = 1; i < ls1.size(); i++) {
            if ((getNumbers(ls1.get(i - 1)) + 1) != getNumbers(ls1.get(i))) {
                flag = false;
            }
        }
        if (flag) {
            bool = "shunzi1";
        }
        boolean flag2 = true;
        for (int i = 1; i < ls2.size(); i++) {
            if ((getNumbers(ls2.get(i - 1)) + 1) != getNumbers(ls2.get(i))) {
                flag2 = false;
                break;
            }
        }
        if (flag2) {
            bool = "shunzi2";
        }
        if ("shunzi1".equals(bool) || "shunzi2".equals(bool)) {
            if (isTongHuaShun(ls1) || isTongHuaShun(ls2)) {
                return "ths";
            } else {
                return "shunzi";
            }
        }
        return bool;
    }

    public static boolean is3wang(List<String> pai) {
        boolean bl = true;
        if (pai.size() == 3) {
            for (String p : pai) {
                if (getNumbers(p) != 16) {
                    bl = false;
                    break;
                }
            }
        } else {
            bl = false;
        }
        return bl;
    }

    public static boolean is2dawang(List<String> pai) {
        boolean bl = false;
        if (pai.size() == 2) {
            if (getColor(pai.get(0)) == 6 && getColor(pai.get(1)) == 6) {
                bl = true;
            }
        } else {
            bl = false;
        }
        return bl;
    }

    public static boolean is2xiaowang(List<String> pai) {
        boolean bl = false;
        if (pai.size() == 2) {
            if (getColor(pai.get(0)) == 5 && getColor(pai.get(1)) == 5) {
                bl = true;
            }
        } else {
            bl = false;
        }
        return bl;
    }

    public static boolean is1dw1xw(List<String> pai) {
        boolean bl = false;
        if (pai.size() == 2) {
            if ((getColor(pai.get(0)) == 6 && getColor(pai.get(1)) == 5) || (getColor(pai.get(0)) == 5 && getColor(pai.get(1)) == 6)) {
                bl = true;
            }
        } else {
            bl = false;
        }
        return bl;
    }

    public static boolean isDanzhang(List<String> pai) {
        if (pai.size() == 1) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isDuizi(List<String> pai) {
        if (pai.size() == 2) {
            if (getNumbers(pai.get(0)) == getNumbers(pai.get(1))) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 是否连对
     *
     * @param pai
     * @return
     */
    public static boolean isLiandui(List<String> pai) {
        boolean bl = true;
        if (pai.size() % 2 == 0 && pai.size() >= 4) {
            List<Integer> xblist = new ArrayList<Integer>();
            int[] ary = paiToShortAry(pai);
            if (ary[13] != 0) {//若连对里包含王
                bl = false;
            } else {
                for (int i = 0; i < ary.length; i++) {
                    if (ary[i] != 2 && ary[i] != 0) {//若牌型里有不等于2张的
                        bl = false;
                        break;
                    }
                    if (ary[i] == 2) {
                        xblist.add(i);
                    }
                }

                for (int i = 0; i < xblist.size() - 1; i++) {
                    if (xblist.get(i + 1) - xblist.get(i) != 1) {//若连对的下标不连续
                        bl = false;
                        break;
                    }
                }

            }
        } else {
            bl = false;
        }
        return bl;
    }

    public static boolean isLiandui2(List<String> pai) {
        boolean bl = true;
        if (pai.size() % 2 == 0 && pai.size() >= 4) {
            List<Integer> xblist = new ArrayList<Integer>();
            int[] ary = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            for (int i = 0; i < pai.size(); i++) {
                int p = getNumbers(pai.get(i));
                ary[p]++;
            }
            if (ary[16] != 0) {//若连对里包含王
                bl = false;
            } else {
                for (int i = 0; i < ary.length; i++) {
                    if (ary[i] != 2 && ary[i] != 0) {//若牌型里有不等于2张的
                        bl = false;
                        break;
                    }
                    if (ary[i] == 2) {
                        xblist.add(i);
                    }
                }

                for (int i = 0; i < xblist.size() - 1; i++) {
                    if (xblist.get(i + 1) - xblist.get(i) != 1) {//若连对的下标不连续
                        bl = false;
                        break;
                    }
                }

            }
        } else {
            bl = false;
        }
        return bl;
    }

    public static boolean isLiandui3(List<String> pai) {
        boolean bool = false;
        List<String> ls1 = new ArrayList<>();
        List<String> ls2 = new ArrayList<>();
        for (int i = 0; i < pai.size(); i++) {
            int num = getNumbers(pai.get(i));
            if (num == 14 || num == 15) {//A 2
                ls1.add(pai.get(i).substring(0, 1) + String.valueOf(num - 13));
            } else {
                ls1.add(pai.get(i));
            }
            ls2.add(pai.get(i));
        }
        if (isLiandui2(ls1) || isLiandui2(ls2)) {
            bool = true;
        } else {
            bool = false;
        }
        return bool;
    }

    /**
     * 是否连三张
     *
     * @param pai
     * @return
     */
    public static boolean isLian3zhang(List<String> pai) {
        boolean bl = true;
        if (pai.size() % 3 == 0 && pai.size() >= 6) {
            List<Integer> xblist = new ArrayList<Integer>();
            int[] ary = paiToShortAry(pai);
            if (ary[13] != 0) {//若连对里包含王
                bl = false;
            } else {
                for (int i = 0; i < ary.length; i++) {
                    if (ary[i] != 3 && ary[i] != 0) {//若牌型里有不等于2张的
                        bl = false;
                        break;
                    }
                    if (ary[i] == 3) {
                        xblist.add(i);
                    }
                }

                for (int i = 0; i < xblist.size() - 1; i++) {
                    if (xblist.get(i + 1) - xblist.get(i) != 1) {//若连对的下标不连续
                        bl = false;
                        break;
                    }
                }

            }
        } else {
            bl = false;
        }
        return bl;
    }

    /**
     * 是否连四张
     *
     * @param pai
     * @return
     */
    public static boolean isLian4zhang(List<String> pai) {
        boolean bl = true;
        if (pai.size() % 4 == 0 && pai.size() >= 8) {
            List<Integer> xblist = new ArrayList<Integer>();
            int[] ary = paiToShortAry(pai);
            if (ary[13] != 0) {//若连对里包含王
                bl = false;
            } else {
                for (int i = 0; i < ary.length; i++) {
                    if (ary[i] != 4 && ary[i] != 0) {//若牌型里有不等于2张的
                        bl = false;
                        break;
                    }
                    if (ary[i] == 4) {
                        xblist.add(i);
                    }
                }

                for (int i = 0; i < xblist.size() - 1; i++) {
                    if (xblist.get(i + 1) - xblist.get(i) != 1) {//若连对的下标不连续
                        bl = false;
                        break;
                    }
                }
            }
        } else {
            bl = false;
        }
        return bl;
    }

    /**
     * 获取单张提示
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTsDanzhang(List<String> sjp, List<String> zjp) {

        List<List<String>> all = new ArrayList<List<String>>();
        for (String p : zjp) {
            if (getNumbers(p) > getNumbers(sjp.get(0))) {
                List<String> ts = new ArrayList<String>();
                ts.add(p);
                all.add(ts);
            }
            //单张小王要被大王打起
            if (getNumbers(sjp.get(0)) == 16 && getColor(sjp.get(0)) == 5) {
                if (getNumbers(p) == 16 && getColor(p) == 6) {
                    List<String> ts = new ArrayList<String>();
                    ts.add(p);
                    all.add(ts);
                }
            }
        }

        return all;
    }

    /**
     * 获取单张提示,去花色
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTsDanzhang1(List<String> sjp, List<String> zjp) {

        List<List<String>> all = new ArrayList<List<String>>();
        for (String p : zjp) {
            if (getNumbers(p) > getNumbers(sjp.get(0))) {
                List<String> ts = new ArrayList<String>();
                ts.add(p);
                if (!isHave2(ts, all)) {
                    all.add(ts);
                }
            }
            //单张小王要被大王打起
            if (getNumbers(sjp.get(0)) == 16 && getColor(sjp.get(0)) == 5) {
                if (getNumbers(p) == 16 && getColor(p) == 6) {
                    List<String> ts = new ArrayList<String>();
                    ts.add(p);
                    if (!isHave2(ts, all)) {
                        all.add(ts);
                    }
                }
            }
        }

        return all;
    }

    /**
     * 获取对子提示
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTsDuizi(List<String> sjp, List<String> zjp) {
        List<List<String>> all = new ArrayList<List<String>>();
        List<List<String>> list = new ArrayList<List<String>>();
        String[] zjpary = new String[zjp.size()];
        zjp.toArray(zjpary);
        combinationDz(zjpary, 2, list, 2);
        int snum = getNumbers(sjp.get(0));
        for (int i = 0; i < list.size(); i++) {
            List<String> l = list.get(i);
            if (getNumbers(l.get(0)) > snum && !isHave1(l, all)) {
                all.add(l);
            }
        }
        return all;
    }

    public static List<List<String>> getTsDuizi1(List<String> sjp, List<String> zjp) {
        List<List<String>> all = new ArrayList<List<String>>();
        List<List<String>> list = new ArrayList<List<String>>();
        String[] zjpary = new String[zjp.size()];
        zjp.toArray(zjpary);
        combinationDz(zjpary, 2, list, 2);
        int snum = getNumbers(sjp.get(0));
        for (int i = 0; i < list.size(); i++) {
            List<String> l = list.get(i);
            if (getNumbers(l.get(0)) > snum && !isHave2(l, all) && getNumbers(l.get(0)) != 16) {
                all.add(l);
            }
        }
        //补 一对大王
        List<String> ls = copyList(zjp);
        if (ls.contains("W16")) {
            ls.remove("W16");
            if (ls.contains("W16")) {
                ls.remove("W16");
                List<String> w1 = new ArrayList<String>();
                w1.add("W16");
                w1.add("W16");
                all.add(w1);
            }
        }
        if (ls.contains("w16")) {
            ls.remove("w16");
            if (ls.contains("w16")) {
                ls.remove("w16");
                List<String> w1 = new ArrayList<String>();
                w1.add("w16");
                w1.add("w16");
                all.add(w1);
            }
        }
        return all;
    }

    /**
     * 获取三张提示
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTs3zhang(List<String> sjp, List<String> zjp) {
        List<List<String>> all = new ArrayList<List<String>>();
        List<List<String>> list = new ArrayList<List<String>>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> zjp2 = new ArrayList<String>();
        zjp2.addAll(zjp);

        List<Integer> xblist = new ArrayList<Integer>();
        int[] ary = paiToShortAry(zjp);
        for (int i = 0; i < ary.length; i++) {
            if (ary[i] >= 3) {
                xblist.add(i);
            }
        }
        for (int i = 0; i < zjp1.size(); i++) {
            int num = getNumbers(zjp1.get(i));
            if (!xblist.contains(num - 3)) {
                zjp2.remove(zjp1.get(i));
            }
        }

        String[] zjpary = new String[zjp2.size()];
        zjp2.toArray(zjpary);
        combination3zhang(zjpary, 3, list, 3);
        int snum = getNumbers(sjp.get(0));
        for (int i = 0; i < list.size(); i++) {
            List<String> l = list.get(i);
            if (getNumbers(l.get(0)) > snum && !isHave1(l, all)) {
                all.add(l);
            }
        }
        return all;
    }

    public static List<List<String>> getTs3zhang1(List<String> sjp, List<String> zjp) {
        List<List<String>> all = new ArrayList<List<String>>();
        List<List<String>> list = new ArrayList<List<String>>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> zjp2 = new ArrayList<String>();
        zjp2.addAll(zjp);

        List<Integer> xblist = new ArrayList<Integer>();
        int[] ary = paiToShortAry(zjp);
        for (int i = 0; i < ary.length; i++) {
            if (ary[i] >= 3) {
                xblist.add(i);
            }
        }
        for (int i = 0; i < zjp1.size(); i++) {
            int num = getNumbers(zjp1.get(i));
            if (!xblist.contains(num - 3)) {
                zjp2.remove(zjp1.get(i));
            }
        }

        String[] zjpary = new String[zjp2.size()];
        zjp2.toArray(zjpary);
        combination3zhang(zjpary, 3, list, 3);
        int snum = getNumbers(sjp.get(0));
        for (int i = 0; i < list.size(); i++) {
            List<String> l = list.get(i);
            if (getNumbers(l.get(0)) > snum && !isHave2(l, all)) {
                all.add(l);
            }
        }
        return all;
    }

    /**
     * 获取4张提示
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTs4zhang(List<String> sjp, List<String> zjp) {
        List<List<String>> all = new ArrayList<List<String>>();
        List<List<String>> list = new ArrayList<List<String>>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> zjp2 = new ArrayList<String>();
        zjp2.addAll(zjp);

        List<Integer> xblist = new ArrayList<Integer>();
        int[] ary = paiToShortAry(zjp);
        for (int i = 0; i < ary.length; i++) {
            if (ary[i] >= 4) {
                xblist.add(i);
            }
        }
        for (int i = 0; i < zjp1.size(); i++) {
            int num = getNumbers(zjp1.get(i));
            if (!xblist.contains(num - 3)) {
                zjp2.remove(zjp1.get(i));
            }
        }

        String[] zjpary = new String[zjp2.size()];
        zjp2.toArray(zjpary);
        combination4zhang(zjpary, 4, list, 4);
        int snum = getNumbers(sjp.get(0));
        for (int i = 0; i < list.size(); i++) {
            List<String> l = list.get(i);
            if (getNumbers(l.get(0)) > snum && !isHave1(l, all)) {
                all.add(l);
            }
        }
        return all;
    }

    public static List<List<String>> getTs4zhang1(List<String> sjp, List<String> zjp) {
        List<List<String>> all = new ArrayList<List<String>>();
        List<List<String>> list = new ArrayList<List<String>>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> zjp2 = new ArrayList<String>();
        zjp2.addAll(zjp);

        List<Integer> xblist = new ArrayList<Integer>();
        int[] ary = paiToShortAry(zjp);
        for (int i = 0; i < ary.length; i++) {
            if (ary[i] >= 4) {
                xblist.add(i);
            }
        }
        for (int i = 0; i < zjp1.size(); i++) {
            int num = getNumbers(zjp1.get(i));
            if (!xblist.contains(num - 3)) {
                zjp2.remove(zjp1.get(i));
            }
        }

        String[] zjpary = new String[zjp2.size()];
        zjp2.toArray(zjpary);
        combination4zhang(zjpary, 4, list, 4);
        int snum = getNumbers(sjp.get(0));
        for (int i = 0; i < list.size(); i++) {
            List<String> l = list.get(i);
            if (getNumbers(l.get(0)) > snum && !isHave2(l, all)) {
                all.add(l);
            }
        }
        return all;
    }

    /**
     * 获取连对提示
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTsLiandui(List<String> sjp, List<String> zjp) {

        List<List<String>> all = new ArrayList<List<String>>();
        List<List<String>> list = new ArrayList<List<String>>();
        List<Integer> xblist = new ArrayList<Integer>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> zjp2 = new ArrayList<String>();
        zjp2.addAll(zjp);
        int[] ary = paiToShortAry(zjp);
        for (int i = 0; i < ary.length; i++) {
            if (ary[i] >= 2) {
                xblist.add(i);
            }
        }
        for (int i = 0; i < zjp1.size(); i++) {
            int num = getNumbers(zjp1.get(i));
            if (!xblist.contains(num - 3)) {
                zjp2.remove(zjp1.get(i));
            }
        }
        String[] zjpary = new String[zjp2.size()];
        zjp2.toArray(zjpary);
        combinationLiandui(zjpary, sjp.size(), list, sjp.size());
        int snum = getNumbers(sjp.get(0));
        for (int i = 0; i < list.size(); i++) {
            List<String> l = list.get(i);
            if (getNumbers(l.get(0)) > snum && !isHave1(l, all)) {
                all.add(l);
            }
        }
        return all;
    }

    public static List<List<String>> getTsLiandui1(List<String> sjp, List<String> zjp) {
        List<List<String>> all = new ArrayList<List<String>>();
        List<List<String>> list = new ArrayList<List<String>>();
        List<List<String>> ldlist = new ArrayList<List<String>>();
        List<Integer> xblist = new ArrayList<Integer>();
        sjp = sortList(sjp, "asc");
        List<String> zjp3 = new ArrayList<String>();
        zjp3.addAll(zjp);
        for (String p : zjp3) {
            if (getNumbers(p) < getNumbers(sjp.get(0))) {
                zjp.remove(p);
            }
        }

        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> zjp2 = new ArrayList<String>();
        zjp2.addAll(zjp);
        int[] ary = paiToShortAry(zjp);
        for (int i = 0; i < ary.length; i++) {
            if (ary[i] >= 2) {
                xblist.add(i);
            }
        }
        for (int i = 0; i < zjp1.size(); i++) {
            int num = getNumbers(zjp1.get(i));
            if (!xblist.contains(num - 3)) {
                zjp2.remove(zjp1.get(i));
            }
        }
        String[] zjpary = new String[zjp2.size()];
        zjp2.toArray(zjpary);
        zjpary = sortAry(zjpary, "asc");
        if (isLiandui(sjp)) {
            combinationDz1(zjpary, 2, list, 2);
            int slen = sjp.size();
            int num = slen / 2;
            if (slen <= zjp.size()) {
                combinationLd(list, num, ldlist, num);
            }
        }

        for (int i = 0; i < ldlist.size(); i++) {
            List<String> pl = ldlist.get(i);
            String[] pary = new String[pl.size()];
            for (int j = 0; j < pl.size(); j++) {
                pary[j] = pl.get(j);
            }
            pary = sortAry(pary, "asc");
            if (getNumbers(pary[0]) > getNumbers(sjp.get(0)) && !isHave2(pl, all)) {
                if(pl.contains("w16") || pl.contains("W16")){
                }else{
                    all.add(pl);
                }
            }
        }
        return all;
//		List<List<String>> all = new ArrayList<List<String>>();
//		List<List<String>> list = new ArrayList<List<String>>();
//		List<Integer> xblist = new ArrayList<Integer>();
//		List<String> zjp1 = new ArrayList<String>();
//		zjp1.addAll(zjp);
//		List<String> zjp2 = new ArrayList<String>();
//		zjp2.addAll(zjp);
//		int[] ary = paiToShortAry(zjp);
//		for (int i = 0; i < ary.length; i++) {
//			if(ary[i] >= 2) {
//				xblist.add(i);
//			}
//		}
//		for (int i = 0; i < zjp1.size(); i++) {
//			int num = getNumbers(zjp1.get(i));
//			if(!xblist.contains(num - 3)) {
//				zjp2.remove(zjp1.get(i));
//			}
//		}
//		System.out.println(zjp2.size());
//		String[] zjpary = new String[zjp2.size()];
//		zjp2.toArray(zjpary);
//		combinationLiandui(zjpary, sjp.size(), list, sjp.size());
//		int snum = getNumbers(sjp.get(0));
//		for (int i = 0; i < list.size(); i++) {
//			List<String> l = list.get(i);
//			if(getNumbers(l.get(0)) > snum && !isHave2(l, all)) {
//				all.add(l);
//			}
//		}
//		return all;
    }

    /**
     * 获取连3张提示
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTsLian3zhang(List<String> sjp, List<String> zjp) {
//		List<List<String>> all = new ArrayList<List<String>>();
//		List<List<String>> list = new ArrayList<List<String>>();
//		String[] zjpary = new String[zjp.size()];
//		zjp.toArray(zjpary);
//		combinationLian3zhang(zjpary, sjp.size(), list, sjp.size());
//		int snum = getNumbers(sjp.get(0));
//		for (int i = 0; i < list.size(); i++) {
//			List<String> l = list.get(i);
//			if(getNumbers(l.get(0)) > snum) {
//				all.add(l);
//			}
//		}
//		return all;

        List<List<String>> all = new ArrayList<List<String>>();
        List<List<String>> list = new ArrayList<List<String>>();
        List<List<String>> l3zlist = new ArrayList<List<String>>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> zjp2 = new ArrayList<String>();
        zjp2.addAll(zjp);

        List<Integer> xblist = new ArrayList<Integer>();
        int[] ary = paiToShortAry(zjp);
        for (int i = 0; i < ary.length; i++) {
            if (ary[i] >= 3) {
                xblist.add(i);
            }
        }
        for (int i = 0; i < zjp1.size(); i++) {
            int num = getNumbers(zjp1.get(i));
            if (!xblist.contains(num - 3)) {
                zjp2.remove(zjp1.get(i));
            }
        }

        String[] zjpary = new String[zjp2.size()];
        zjp2.toArray(zjpary);
        int num = sjp.size() / 3;
        combination3zhang(zjpary, 3, list, 3);
        combinationl3z(list, num, l3zlist, num);

        int snum = getNumbers(sjp.get(0));
        for (int i = 0; i < l3zlist.size(); i++) {
            List<String> l = l3zlist.get(i);
            if (getNumbers(l.get(0)) > snum && !isHave1(l, all)) {
                all.add(l);
            }
        }
        return all;
    }

    public static List<List<String>> getTsLian3zhang1(List<String> sjp, List<String> zjp) {
        List<List<String>> all = new ArrayList<List<String>>();
        List<List<String>> list = new ArrayList<List<String>>();
        List<List<String>> l3zlist = new ArrayList<List<String>>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> zjp2 = new ArrayList<String>();
        zjp2.addAll(zjp);

        List<Integer> xblist = new ArrayList<Integer>();
        int[] ary = paiToShortAry(zjp);
        for (int i = 0; i < ary.length; i++) {
            if (ary[i] >= 3) {
                xblist.add(i);
            }
        }
        for (int i = 0; i < zjp1.size(); i++) {
            int num = getNumbers(zjp1.get(i));
            if (!xblist.contains(num - 3)) {
                zjp2.remove(zjp1.get(i));
            }
        }

        String[] zjpary = new String[zjp2.size()];
        zjp2.toArray(zjpary);
        int num = sjp.size() / 3;
        combination3zhang(zjpary, 3, list, 3);
        combinationl3z(list, num, l3zlist, num);

        int snum = getNumbers(sjp.get(0));
        for (int i = 0; i < l3zlist.size(); i++) {
            List<String> l = l3zlist.get(i);
            if (getNumbers(l.get(0)) > snum && !isHave2(l, all)) {
                all.add(l);
            }
        }
        return all;
    }

    /**
     * 获取连4张提示
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTsLian4zhang(List<String> sjp, List<String> zjp) {
//		List<List<String>> all = new ArrayList<List<String>>();
//		List<List<String>> list = new ArrayList<List<String>>();
//		String[] zjpary = new String[zjp.size()];
//		zjp.toArray(zjpary);
//		combinationLian4zhang(zjpary, sjp.size(), list, sjp.size());
//		int snum = getNumbers(sjp.get(0));
//		for (int i = 0; i < list.size(); i++) {
//			List<String> l = list.get(i);
//			if(getNumbers(l.get(0)) > snum) {
//				all.add(l);
//			}
//		}
//		return all;

        List<List<String>> all = new ArrayList<List<String>>();
        List<List<String>> list = new ArrayList<List<String>>();
        List<List<String>> l4zlist = new ArrayList<List<String>>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> zjp2 = new ArrayList<String>();
        zjp2.addAll(zjp);

        List<Integer> xblist = new ArrayList<Integer>();
        int[] ary = paiToShortAry(zjp);
        for (int i = 0; i < ary.length; i++) {
            if (ary[i] >= 4) {
                xblist.add(i);
            }
        }
        for (int i = 0; i < zjp1.size(); i++) {
            int num = getNumbers(zjp1.get(i));
            if (!xblist.contains(num - 3)) {
                zjp2.remove(zjp1.get(i));
            }
        }

        String[] zjpary = new String[zjp2.size()];
        zjp2.toArray(zjpary);
        int num = sjp.size() / 4;
        combination4zhang(zjpary, 4, list, 4);
        combinationl4z(list, num, l4zlist, num);

        int snum = getNumbers(sjp.get(0));
        for (int i = 0; i < l4zlist.size(); i++) {
            List<String> l = l4zlist.get(i);
            if (getNumbers(l.get(0)) > snum && !isHave1(l, all)) {
                all.add(l);
            }
        }
        return all;
    }

    public static List<List<String>> getTsLian4zhang1(List<String> sjp, List<String> zjp) {
        List<List<String>> all = new ArrayList<List<String>>();
        List<List<String>> list = new ArrayList<List<String>>();
        List<List<String>> l4zlist = new ArrayList<List<String>>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> zjp2 = new ArrayList<String>();
        zjp2.addAll(zjp);

        List<Integer> xblist = new ArrayList<Integer>();
        int[] ary = paiToShortAry(zjp);
        for (int i = 0; i < ary.length; i++) {
            if (ary[i] >= 4) {
                xblist.add(i);
            }
        }
        for (int i = 0; i < zjp1.size(); i++) {
            int num = getNumbers(zjp1.get(i));
            if (!xblist.contains(num - 3)) {
                zjp2.remove(zjp1.get(i));
            }
        }

        String[] zjpary = new String[zjp2.size()];
        zjp2.toArray(zjpary);
        int num = sjp.size() / 4;
        combination4zhang(zjpary, 4, list, 4);
        combinationl4z(list, num, l4zlist, num);

        int snum = getNumbers(sjp.get(0));
        for (int i = 0; i < l4zlist.size(); i++) {
            List<String> l = l4zlist.get(i);
            if (getNumbers(l.get(0)) > snum && !isHave2(l, all)) {
                all.add(l);
            }
        }
        return all;
    }

    /**
     * 获取副50K提示
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTsMin50k(List<String> sjp, List<String> zjp) {

        List<List<String>> list = new ArrayList<List<String>>();
        if(true){
            return list;
        }
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> zjp2 = new ArrayList<String>();
        zjp2.addAll(zjp);

        List<Integer> xblist = new ArrayList<Integer>();
        xblist.add(2);
        xblist.add(7);
        xblist.add(10);
//		int[] ary = paiToShortAry(zjp);
//		for (int i = 0; i < ary.length; i++) {
//			if(ary[i] != 2 && ary[i] != 7 && ary[i] != 10) {
//				xblist.add(i);
//			}
//		}
        for (int i = 0; i < zjp1.size(); i++) {
            int num = getNumbers(zjp1.get(i));
            if (!xblist.contains(num - 3)) {
                zjp2.remove(zjp1.get(i));
            }
        }

        String[] zjpary = new String[zjp2.size()];
        zjp2.toArray(zjpary);
        combinationMin50k(zjpary, 3, list, 3);
        return list;
    }

    /**
     * 获取副50K提示
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTs50k(List<String> sjp, List<String> zjp) {
        List<List<String>> list = new ArrayList<List<String>>();
        if(true){
            return list;
        }
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> zjp2 = new ArrayList<String>();
        zjp2.addAll(zjp);

        List<Integer> xblist = new ArrayList<Integer>();
        xblist.add(2);
        xblist.add(7);
        xblist.add(10);
        for (int i = 0; i < zjp1.size(); i++) {
            int num = getNumbers(zjp1.get(i));
            if (!xblist.contains(num - 3)) {
                zjp2.remove(zjp1.get(i));
            }
        }

        String[] zjpary = new String[zjp2.size()];
        zjp2.toArray(zjpary);
        combination50k(zjpary, 3, list, 3);
        return list;
    }

    /**
     * 获取方块50K提示
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTsFk50k(List<String> sjp, List<String> zjp) {

        List<List<String>> list = new ArrayList<List<String>>();
        if(true){
            return list;
        }
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> zjp2 = new ArrayList<String>();
        zjp2.addAll(zjp);

        List<Integer> xblist = new ArrayList<Integer>();
        xblist.add(2);
        xblist.add(7);
        xblist.add(10);
        for (int i = 0; i < zjp1.size(); i++) {
            int num = getNumbers(zjp1.get(i));
            if (!xblist.contains(num - 3)) {
                zjp2.remove(zjp1.get(i));
            }
        }

        String[] zjpary = new String[zjp2.size()];
        zjp2.toArray(zjpary);
        combinationFk50k(zjpary, 3, list, 3);
        return list;
    }

    /**
     * 获取梅花50K提示
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTsMh50k(List<String> sjp, List<String> zjp) {

        List<List<String>> list = new ArrayList<List<String>>();
        if(true){
            return list;
        }
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> zjp2 = new ArrayList<String>();
        zjp2.addAll(zjp);

        List<Integer> xblist = new ArrayList<Integer>();
        xblist.add(2);
        xblist.add(7);
        xblist.add(10);
        for (int i = 0; i < zjp1.size(); i++) {
            int num = getNumbers(zjp1.get(i));
            if (!xblist.contains(num - 3)) {
                zjp2.remove(zjp1.get(i));
            }
        }

        String[] zjpary = new String[zjp2.size()];
        zjp2.toArray(zjpary);
        combinationMh50k(zjpary, 3, list, 3);
        return list;
    }

    /**
     * 获取红心50K提示
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTsHx50k(List<String> sjp, List<String> zjp) {

        List<List<String>> list = new ArrayList<List<String>>();
        if(true){
            return list;
        }
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> zjp2 = new ArrayList<String>();
        zjp2.addAll(zjp);

        List<Integer> xblist = new ArrayList<Integer>();
        xblist.add(2);
        xblist.add(7);
        xblist.add(10);
        for (int i = 0; i < zjp1.size(); i++) {
            int num = getNumbers(zjp1.get(i));
            if (!xblist.contains(num - 3)) {
                zjp2.remove(zjp1.get(i));
            }
        }

        String[] zjpary = new String[zjp2.size()];
        zjp2.toArray(zjpary);
        combinationHx50k(zjpary, 3, list, 3);
        return list;
    }

    /**
     * 获取黑桃50K提示
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTsHt50k(List<String> sjp, List<String> zjp) {

        List<List<String>> list = new ArrayList<List<String>>();
        if(true){
            return list;
        }
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> zjp2 = new ArrayList<String>();
        zjp2.addAll(zjp);

        List<Integer> xblist = new ArrayList<Integer>();
        xblist.add(2);
        xblist.add(7);
        xblist.add(10);
        for (int i = 0; i < zjp1.size(); i++) {
            int num = getNumbers(zjp1.get(i));
            if (!xblist.contains(num - 3)) {
                zjp2.remove(zjp1.get(i));
            }
        }

        String[] zjpary = new String[zjp2.size()];
        zjp2.toArray(zjpary);
        combinationHt50k(zjpary, 3, list, 3);
        return list;
    }

    /**
     * 获取2w提示
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTs2XiaoWang(List<String> sjp, List<String> zjp) {

        List<List<String>> list = new ArrayList<List<String>>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> ts = new ArrayList<String>();
        if (zjp1.contains("w16")) {
            ts.add("w16");
            zjp1.remove("w16");
        }
        if (zjp1.contains("w16")) {
            ts.add("w16");
            zjp1.remove("w16");
        }
        if (ts.size() == 2) {
            list.add(ts);
        }
        return list;
    }

    /**
     * 获取1dw1xw提示
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTs1dw1xw(List<String> sjp, List<String> zjp) {

        List<List<String>> list = new ArrayList<List<String>>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> ts = new ArrayList<String>();
        if (zjp1.contains("W16")) {
            ts.add("W16");
            zjp1.remove("W16");
        }
        if (zjp1.contains("w16")) {
            ts.add("w16");
            zjp1.remove("w16");
        }
        if (ts.size() == 2) {
            list.add(ts);
        }
        return list;
    }

    /**
     * 获取2W提示
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTs2DaWang(List<String> sjp, List<String> zjp) {

        List<List<String>> list = new ArrayList<List<String>>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> ts = new ArrayList<String>();
        if (zjp1.contains("W16")) {
            ts.add("W16");
            zjp1.remove("W16");
        }
        if (zjp1.contains("W16")) {
            ts.add("W16");
            zjp1.remove("W16");
        }
        if (ts.size() == 2) {
            list.add(ts);
        }
        return list;
    }

    /**
     * 获取3W提示
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTs3Wang(List<String> sjp, List<String> zjp) {

        List<List<String>> list = new ArrayList<List<String>>();
        if(true){
            return list;
        }
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> zjp2 = new ArrayList<String>();
        zjp2.addAll(zjp);

        List<Integer> xblist = new ArrayList<Integer>();
        xblist.add(13);
        for (int i = 0; i < zjp1.size(); i++) {
            int num = getNumbers(zjp1.get(i));
            if (!xblist.contains(num - 3)) {
                zjp2.remove(zjp1.get(i));
            }
        }

        String[] zjpary = new String[zjp2.size()];
        zjp2.toArray(zjpary);
        combination3Wang(zjpary, 3, list, 3);
        return list;
    }

    /**
     * 获取4W提示
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTs4Wang(List<String> sjp, List<String> zjp) {

        List<List<String>> list = new ArrayList<List<String>>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> ts = new ArrayList<String>();
        if (zjp1.contains("W16")) {
            ts.add("W16");
            zjp1.remove("W16");
        }
        if (zjp1.contains("W16")) {
            ts.add("W16");
            zjp1.remove("W16");
        }
        if (zjp1.contains("w16")) {
            ts.add("w16");
            zjp1.remove("w16");
        }
        if (zjp1.contains("w16")) {
            ts.add("w16");
            zjp1.remove("w16");
        }
        if (ts.size() == 4) {
            list.add(ts);
        }
        return list;
    }

    /**
     * 获取5炸提示
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTs5zha(List<String> sjp, List<String> zjp) {

        List<List<String>> all = new ArrayList<List<String>>();
        List<List<String>> list = new ArrayList<List<String>>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> zjp2 = new ArrayList<String>();
        zjp2.addAll(zjp);

        List<Integer> xblist = new ArrayList<Integer>();
        int[] ary = paiToShortAry(zjp);
        for (int i = 0; i < ary.length; i++) {
            if (ary[i] >= 5) {
                xblist.add(i);
            }
        }
        for (int i = 0; i < zjp1.size(); i++) {
            int num = getNumbers(zjp1.get(i));
            if (!xblist.contains(num - 3)) {
                zjp2.remove(zjp1.get(i));
            }
        }

        String[] zjpary = new String[zjp2.size()];
        zjp2.toArray(zjpary);
        combination5zha(zjpary, 5, list, 5);
        if (is5zha(sjp)) {
            int snum = getNumbers(sjp.get(0));
            for (int i = 0; i < list.size(); i++) {
                List<String> l = list.get(i);
                if (getNumbers(l.get(0)) > snum && !isHave1(l, all)) {
                    all.add(l);
                }
            }
            return all;
        } else {
            for (int i = 0; i < list.size(); i++) {
                List<String> l = list.get(i);
                if (!isHave1(l, all)) {
                    all.add(l);
                }
            }
            return all;
            //return list;
        }
    }

    /**
     * 获取6炸提示
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTs6zha(List<String> sjp, List<String> zjp) {

        List<List<String>> all = new ArrayList<List<String>>();
        List<List<String>> list = new ArrayList<List<String>>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> zjp2 = new ArrayList<String>();
        zjp2.addAll(zjp);

        List<Integer> xblist = new ArrayList<Integer>();
        int[] ary = paiToShortAry(zjp);
        for (int i = 0; i < ary.length; i++) {
            if (ary[i] >= 6) {
                xblist.add(i);
            }
        }
        for (int i = 0; i < zjp1.size(); i++) {
            int num = getNumbers(zjp1.get(i));
            if (!xblist.contains(num - 3)) {
                zjp2.remove(zjp1.get(i));
            }
        }

        String[] zjpary = new String[zjp2.size()];
        zjp2.toArray(zjpary);
        combination6zha(zjpary, 6, list, 6);

        if (is6zha(sjp)) {
            int snum = getNumbers(sjp.get(0));
            for (int i = 0; i < list.size(); i++) {
                List<String> l = list.get(i);
                if (getNumbers(l.get(0)) > snum && !isHave1(l, all)) {
                    all.add(l);
                }
            }
            return all;
        } else {
            for (int i = 0; i < list.size(); i++) {
                List<String> l = list.get(i);
                if (!isHave1(l, all)) {
                    all.add(l);
                }
            }
            return all;
        }
    }

    /**
     * 获取7炸提示
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTs7zha(List<String> sjp, List<String> zjp) {

        List<List<String>> all = new ArrayList<List<String>>();
        List<List<String>> list = new ArrayList<List<String>>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> zjp2 = new ArrayList<String>();
        zjp2.addAll(zjp);

        List<Integer> xblist = new ArrayList<Integer>();
        int[] ary = paiToShortAry(zjp);
        for (int i = 0; i < ary.length; i++) {
            if (ary[i] >= 7) {
                xblist.add(i);
            }
        }
        for (int i = 0; i < zjp1.size(); i++) {
            int num = getNumbers(zjp1.get(i));
            if (!xblist.contains(num - 3)) {
                zjp2.remove(zjp1.get(i));
            }
        }

        String[] zjpary = new String[zjp2.size()];
        zjp2.toArray(zjpary);
        combination7zha(zjpary, 7, list, 7);
        if (is7zha(sjp)) {
            int snum = getNumbers(sjp.get(0));
            for (int i = 0; i < list.size(); i++) {
                List<String> l = list.get(i);
                if (getNumbers(l.get(0)) > snum && !isHave1(l, all)) {
                    all.add(l);
                }
            }
            return all;
        } else {
            for (int i = 0; i < list.size(); i++) {
                List<String> l = list.get(i);
                if (!isHave1(l, all)) {
                    all.add(l);
                }
            }
            return all;
        }
    }

    /**
     * 获取8炸提示
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTs8zha(List<String> sjp, List<String> zjp) {

        List<List<String>> all = new ArrayList<List<String>>();
        List<List<String>> list = new ArrayList<List<String>>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        List<String> zjp2 = new ArrayList<String>();
        zjp2.addAll(zjp);

        List<Integer> xblist = new ArrayList<Integer>();
        int[] ary = paiToShortAry(zjp);
        for (int i = 0; i < ary.length; i++) {
            if (ary[i] >= 8) {
                xblist.add(i);
            }
        }
        for (int i = 0; i < zjp1.size(); i++) {
            int num = getNumbers(zjp1.get(i));
            if (!xblist.contains(num - 3)) {
                zjp2.remove(zjp1.get(i));
            }
        }

        String[] zjpary = new String[zjp2.size()];
        zjp2.toArray(zjpary);
        combination8zha(zjpary, 8, list, 8);
        if (is8zha(sjp)) {
            int snum = getNumbers(sjp.get(0));
            for (int i = 0; i < list.size(); i++) {
                List<String> l = list.get(i);
                if (getNumbers(l.get(0)) > snum) {
                    all.add(l);
                }
            }
            return all;
        } else {
            return list;
        }
    }

    /**
     * 获取顺子提示
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTsShunzi(List<String> sjp, List<String> zjp, String type) {
        List<List<String>> all = new ArrayList<List<String>>();
        List<Integer> specialA2345 = new ArrayList<>();
        specialA2345.add(14);
        specialA2345.add(15);
        specialA2345.add(3);
        specialA2345.add(4);
        specialA2345.add(5);
        List<Integer> special23456 = new ArrayList<>();
        special23456.add(6);
        special23456.add(15);
        special23456.add(3);
        special23456.add(4);
        special23456.add(5);
        List<Integer> special10JQKA = new ArrayList<>();
        special10JQKA.add(10);
        special10JQKA.add(11);
        special10JQKA.add(12);
        special10JQKA.add(13);
        special10JQKA.add(14);
        if (type.equals("shunzi")) {
            int slen = sjp.size();
            //从小到大依次是：
            // A2345--23456--34567--45678--略-- 10JQKA
            List<Integer> valls =loadValList(sjp);
            int m = getNumbers(sjp.get(0));
            if(valls.containsAll(specialA2345)){
                m=1;
            }else if(valls.containsAll(special23456)){
                m=2;
            }else if(valls.containsAll(special10JQKA)){
                // m=15;
                return all;
            }
            if (slen <= zjp.size()) {
                for (int j = 0; j < zjp.size(); j++) {
                    int n = getNumbers(zjp.get(j));
                    int tmp = 0;
                    if (n > m) {
                        List<String> l = new ArrayList<String>();
                        String pai = zjp.get(j);
                        String p = pai;
                        l.add(pai);
                        do {
                            p = findNumBigOne(p, zjp);
                            l.add(p);
                            tmp++;

                        } while (!findNumBigOne(p, zjp).equals("") && tmp < slen - 1);
                        if (l.size() == slen) {
                            if (!isHave(l, all)) {
                                if(l.contains("w16")||l.contains("W16")){

                                }else{
                                    all.add(l);
                                }
                            }
                        }
                    }
                }
            }
        }
        //[B4, B5, B6, B7, B8, B9, M10] B10 //同牌替换
//		R H M F = 4 3 2 1
        List<List<String>> allts = new ArrayList<List<String>>();
        for (List<String> ts : all) {
            for (String paii : ts) {
                int col = getColor(paii);
                int um = getNumbers(paii);
                String replace_B = "B" + String.valueOf(um);
                String replace_R = "R" + String.valueOf(um);
                String replace_M = "M" + String.valueOf(um);
                String replace_F = "F" + String.valueOf(um);
                if (zjp.contains(replace_B)) {
                    List<String> l = copyList(ts);
                    l.remove(paii);
                    l.add(replace_B);
                    sortList(l, "asc");
                    if (!isHave(l, allts)) {
                        allts.add(l);
                    }
                }
                if (zjp.contains(replace_R)) {
                    List<String> l = copyList(ts);
                    l.remove(paii);
                    l.add(replace_B);
                    sortList(l, "asc");
                    if (!isHave(l, allts)) {
                        allts.add(l);
                    }
                }
                if (zjp.contains(replace_M)) {
                    List<String> l = copyList(ts);
                    l.remove(paii);
                    l.add(replace_B);
                    sortList(l, "asc");
                    if (!isHave(l, allts)) {
                        allts.add(l);
                    }
                }
                if (zjp.contains(replace_F)) {
                    List<String> l = copyList(ts);
                    l.remove(paii);
                    l.add(replace_B);
                    sortList(l, "asc");
                    if (!isHave(l, allts)) {
                        if(l.contains("w16")||l.contains("W16")){

                        }else{
                            allts.add(l);
                        }
                    }
                }
            }
        }
        return allts;
    }

    /**
     *
     * @param sjp
     * @return 从sjp总回去牌值数据
     */
    public static List<Integer> loadValList(List<String> sjp){
        List<Integer> ls = new ArrayList<>();
        for (String str:sjp ) {
            ls.add(getNumbers(str)%100);
        }
        return ls;
    }
    /**
     * 获取同花顺提示
     *
     * @param sjp
     * @param zjp
     * @param type
     * @return
     */
    public static List<List<String>> getTsTongHuaShun(List<String> sjp, List<String> zjp, String type) {
//        同花顺限定为5张牌。
        List<List<String>> all = new ArrayList<List<String>>();
        if (type.equals("ths")) {
//			zjp = removeBoom(zjp);
            int slen =5;
            int m = getNumbers(sjp.get(0));
            int col = getColor(sjp.get(0));
            if (slen <= zjp.size()) {
                for (int j = 0; j < zjp.size(); j++) {
                    int n = getNumbers(zjp.get(j));
                    int tmp = 0;
                    if (n > m) {
                        List<String> l = new ArrayList<String>();
                        String pai = zjp.get(j);
                        int col2 = getColor(pai);
                        String p = pai;
                        l.add(pai);
                        do {
                            p = findNumBigOne(p, zjp);
                            l.add(p);
                            tmp++;

                        } while (!findNumBigOne(p, zjp).equals("") && tmp < slen - 1);
                        if (l.size() == slen) {
                            if (!isHave(l, all)) {
                                all.add(l);
                            }
                        }
                    }
                }
            }
        }else {
            //上家非同花顺。 自家同花顺提示
            int slen =5;
            int m = 0;//getNumbers(sjp.get(0));
            if (slen <= zjp.size()) {
                for (int j = 0; j < zjp.size(); j++) {
                    int n = getNumbers(zjp.get(j));
                    int tmp = 0;
                    if (n > m) {
                        List<String> l = new ArrayList<String>();
                        String pai = zjp.get(j);
                        int col2 = getColor(pai);
                        String p = pai;
                        l.add(pai);
                        do {
                            p = findNumBigOne(p, zjp);
                            l.add(p);
                            tmp++;

                        } while (!findNumBigOne(p, zjp).equals("") && tmp < slen - 1);
                        if (l.size() == slen) {
                            if (!isHave(l, all)) {
                                all.add(l);
                            }
                        }
                    }
                }
            }
        }
        //[B4, B5, B6, B7, B8, B9, M10] B10 //同牌替换
//		R H M F = 4 3 2 1
        List<List<String>> allts = new ArrayList<List<String>>();
        for (List<String> ts : all) {
            if(isTongHuaShun(ts)){
                allts.add(ts);
                continue;
            }else{
                for (String paii : ts) {
                    int um = getNumbers(paii);
                    String replace_B = "B" + String.valueOf(um);
                    String replace_R = "R" + String.valueOf(um);
                    String replace_M = "M" + String.valueOf(um);
                    String replace_F = "F" + String.valueOf(um);
                    if (zjp.contains(replace_B)) {
                        List<String> l = copyList(ts);
                        l.remove(paii);
                        l.add(replace_B);
                        sortList(l, "asc");
                        if (!isHave(l, allts) && isTongHuaShun(l)) {
                            allts.add(l);
                        }
                    }
                    if (zjp.contains(replace_R)) {
                        List<String> l = copyList(ts);
                        l.remove(paii);
                        l.add(replace_B);
                        sortList(l, "asc");
                        if (!isHave(l, allts) && isTongHuaShun(l)) {
                            allts.add(l);
                        }
                    }
                    if (zjp.contains(replace_M)) {
                        List<String> l = copyList(ts);
                        l.remove(paii);
                        l.add(replace_B);
                        sortList(l, "asc");
                        if (!isHave(l, allts) && isTongHuaShun(l)) {
                            allts.add(l);
                        }
                    }
                    if (zjp.contains(replace_F)) {
                        List<String> l = copyList(ts);
                        l.remove(paii);
                        l.add(replace_B);
                        sortList(l, "asc");
                        if (!isHave(l, allts) && isTongHuaShun(l)) {
                            allts.add(l);
                        }
                    }
                }
            }

        }
        return allts;
    }

    public static List<List<String>> getTsTongHuaShun2(List<String> sjp, List<String> zjp, String type){
        zjp = add12(zjp);
        List<List<String>> all = new ArrayList<List<String>>();
        if (type.equals("ths")) {
            //上家同花顺。打得起的同花顺
//			zjp = removeBoom(zjp);
            int slen = 5;
            List<Integer> specialA2345 = new ArrayList<>();
            specialA2345.add(14);
            specialA2345.add(15);
            specialA2345.add(3);
            specialA2345.add(4);
            specialA2345.add(5);
            List<Integer> special23456 = new ArrayList<>();
            special23456.add(6);
            special23456.add(15);
            special23456.add(3);
            special23456.add(4);
            special23456.add(5);
            List<Integer> special10JQKA = new ArrayList<>();
            special10JQKA.add(10);
            special10JQKA.add(11);
            special10JQKA.add(12);
            special10JQKA.add(13);
            special10JQKA.add(14);
            List<Integer> valls =loadValList(sjp);
            int m = getNumbers(sjp.get(0));
            if(valls.containsAll(specialA2345)){
                m=1;
            }else if(valls.containsAll(special23456)){
                m=2;
            }else if(valls.containsAll(special10JQKA)){
                // m=15;
                return all;
            }
            String[] col = new String[]{"R","B","M","F"};
           for(int i=m;i+5<15;i++){
               for (String p : col) {
                   List<String> t = new ArrayList<>();
                   t.add(p+(m+1));
                   t.add(p+(m+2));
                   t.add(p+(m+3));
                   t.add(p+(m+4));
                   t.add(p+(m+5));
                   if(zjp.containsAll(t)){
                       all.add(t);
                       return all;
                   }
               }
               m++;
           }
        }else {
            int m = 0;
            String[] col = new String[]{"B","R", "M", "F"};
            for(int i=m;i+5<15;i++){
                for (String p : col) {
                    List<String> t = new ArrayList<>();
                    t.add(p+(m+1));
                    t.add(p+(m+2));
                    t.add(p+(m+3));
                    t.add(p+(m+4));
                    t.add(p+(m+5));
                    if(zjp.containsAll(t)){
                        all.add(t);
                        return all;
                    }
                }
                m++;
            }

        }
        return all;
    }
    public static List<String> add12( List<String> zjp){
        List<String> zjp2 = new ArrayList<>(zjp);

        for(int i=0;i<zjp2.size();i++) {
            int col = getColor(zjp2.get(i));
            String strcol ="";
            if(col==4){
                strcol="B";
            }else if(col==3){
                strcol="R";
            }else if(col==2){
                strcol="M";
            }else if(col==1){
                strcol="F";
            }
            if(14==getNumbers(zjp2.get(i))){
                zjp2.add(strcol+"1");
            }
            if(15==getNumbers(zjp2.get(i))){
                zjp2.add(strcol+"2");
            }
        }
        return zjp2;
    }
    public static List<String> copyList(List<String> ts) {
        List<String> l = new ArrayList<String>();
        for (String pai : ts) {
            l.add(pai);
        }
        return l;
    }

    /**
     * 找到牌里比参数牌大1的牌，没有则返回空字符串
     *
     * @param pai
     * @param
     * @return
     */
    public static String findNumBigOne(String pai, List<String> zjp) {
        String p = "";
        int m = getNumbers(pai);
        for (int i = 0; i < zjp.size(); i++) {
            int n = getNumbers(zjp.get(i));
            if (n - m == 1) {
                p = zjp.get(i);
                break;
            }
        }
        return p;
    }

    /**
     * 获取所有提示2
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getAllTs2(List<String> sjp, List<String> zjp) {
        List<List<String>> all = new ArrayList<List<String>>();
        sjp = sortList(sjp, "asc");
        zjp = sortList(zjp, "asc");


        return all;
    }

    /**
     * 获取所有提示1
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getAllTs1(List<String> sjp, List<String> zjp) {
        List<List<String>> all = new ArrayList<List<String>>();
        sjp = sortList(sjp, "asc");
        zjp = sortList(zjp, "asc");
        if (getCpType(sjp).equals("danz")) {
            List<List<String>> list = getTsDanzhang(sjp, zjp);
            List<List<String>> zdlist = getTsZhaDan1(sjp, zjp, "danz");
            all.addAll(list);
            all.addAll(zdlist);//额外获取50k，各种炸弹
        } else if (getCpType(sjp).equals("duiz")) {
            List<List<String>> list = getTsDuizi(sjp, zjp);
            List<List<String>> zdlist = getTsZhaDan1(sjp, zjp, "duiz");
            all.addAll(list);
            all.addAll(zdlist);//额外获取50k，各种炸弹
        } else if (getCpType(sjp).equals("3z")) {
            List<List<String>> list = getTs3zhang(sjp, zjp);
            List<List<String>> zdlist = getTsZhaDan1(sjp, zjp, "3z");
            all.addAll(list);
            all.addAll(zdlist);//额外获取50k，各种炸弹
        } else if (getCpType(sjp).equals("4z")) {
            List<List<String>> list = getTs4zhang(sjp, zjp);
            List<List<String>> zdlist = getTsZhaDan1(sjp, zjp, "4z");
            all.addAll(list);
            all.addAll(zdlist);//额外获取50k，各种炸弹
        } else if (getCpType(sjp).equals("ld")) {
            List<List<String>> list = getTsLiandui(sjp, zjp);
            List<List<String>> zdlist = getTsZhaDan1(sjp, zjp, "ld");
            all.addAll(list);
            all.addAll(zdlist);//额外获取50k，各种炸弹
        } else if (getCpType(sjp).equals("l3z")) {
            List<List<String>> list = getTsLian3zhang(sjp, zjp);
            List<List<String>> zdlist = getTsZhaDan1(sjp, zjp, "l3z");
            all.addAll(list);
            all.addAll(zdlist);//额外获取50k，各种炸弹
        } else if (getCpType(sjp).equals("l4z")) {
            List<List<String>> list = getTsLian4zhang(sjp, zjp);
            List<List<String>> zdlist = getTsZhaDan1(sjp, zjp, "l4z");
            all.addAll(list);
            all.addAll(zdlist);//额外获取50k，各种炸弹
        } else if (getCpType(sjp).equals("min50k")) {
            all.addAll(getTsZhaDan1(sjp, zjp, "min50k"));
        } else if (getCpType(sjp).equals("f50k")) {
            all.addAll(getTsZhaDan1(sjp, zjp, "f50k"));
        } else if (getCpType(sjp).equals("m50k")) {
            all.addAll(getTsZhaDan1(sjp, zjp, "m50k"));
        } else if (getCpType(sjp).equals("r50k")) {
            all.addAll(getTsZhaDan1(sjp, zjp, "r50k"));
        } else if (getCpType(sjp).equals("b50k")) {
            all.addAll(getTsZhaDan1(sjp, zjp, "b50k"));
        } else if (getCpType(sjp).equals("ww")) {
            all.addAll(getTsZhaDan1(sjp, zjp, "ww"));
        } else if (getCpType(sjp).equals("Ww")) {
            all.addAll(getTsZhaDan1(sjp, zjp, "Ww"));
        } else if (getCpType(sjp).equals("WW")) {
            all.addAll(getTsZhaDan1(sjp, zjp, "WW"));
        } else if (getCpType(sjp).equals("5zha")) {
            all.addAll(getTsZhaDan1(sjp, zjp, "5zha"));
        } else if (getCpType(sjp).equals("3w")) {
            all.addAll(getTsZhaDan1(sjp, zjp, "3w"));
        } else if (getCpType(sjp).equals("6zha")) {
            all.addAll(getTsZhaDan1(sjp, zjp, "6zha"));
        } else if (getCpType(sjp).equals("7zha")) {
            all.addAll(getTsZhaDan1(sjp, zjp, "7zha"));
        } else if (getCpType(sjp).equals("8zha")) {
            all.addAll(getTsZhaDan1(sjp, zjp, "8zha"));
        }

        return all;
    }

    /**
     * 获取所有提示
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getAllTsNew(List<String> sjp, List<String> zjp) {
        List<List<String>> all = new ArrayList<List<String>>();
        sjp = sortList(sjp, "asc");
        zjp = sortList(zjp, "asc");
        List<String> sjp1 = copyList(sjp);
        String cptype = getCpType(sjp1);
        if (cptype.equals("danz")) {
            List<List<String>> list = getTsDanZhangNew(sjp, zjp);
            all.addAll(getTs4zha(sjp, zjp,"danz"));
            all.addAll(list);
        } else if (cptype.equals("duiz")) {
            List<List<String>> list = getTsDuiZiNew(sjp, zjp);
            all.addAll(getTs4zha(sjp, zjp,"duiz"));
            all.addAll(list);
        } else if (cptype.equals("3z")) {
            List<List<String>> list = getTs3zhangNew(sjp, zjp);
            all.addAll(getTs4zha(sjp, zjp,"3z"));
            all.addAll(list);
        } else if (cptype.equals("4z")) {
            List<List<String>> list = getTs4zhangNew(sjp, zjp);
            all.addAll(list);
        } else if (cptype.equals("feiji")) {
            List<List<String>> list = getTsFeiJiNew(sjp, zjp);
            all.addAll(getTs4zha(sjp, zjp,"feiji"));
            all.addAll(list);
        } else if (cptype.equals("ld")) {
            List<List<String>> list = getTsLianDuiNew(sjp, zjp);
            all.addAll(getTs4zha(sjp, zjp,"ld"));
            all.addAll(list);
        } else if (cptype.equals("l3z")) {
            List<List<String>> list = getTsLian3zhangNew(sjp, zjp);
            all.addAll(getTs4zha(sjp, zjp,"l3z"));
            all.addAll(list);
        }
//		else if(cptype.equals("l4z")) {
//			List<List<String>> list = getTsLian4zhangNew(sjp, zjp);
//			all.addAll(list);
//		}
//		else if(cptype.equals("min50k")) {
//			all.addAll(getTsZhaDan(sjp, zjp, "min50k"));
//		} else if(cptype.equals("f50k")) {
//			all.addAll(getTsZhaDan(sjp, zjp, "f50k"));
//		} else if(cptype.equals("m50k")) {
//			all.addAll(getTsZhaDan(sjp, zjp, "m50k"));
//		} else if(cptype.equals("r50k")) {
//			all.addAll(getTsZhaDan(sjp, zjp, "r50k"));
//		} else if(cptype.equals("b50k")) {
//			all.addAll(getTsZhaDan(sjp, zjp, "b50k"));
//		}
//		else if(cptype.equals("ww")) {
//			all.addAll(getTsZhaDan(sjp, zjp, "ww"));
//		} else if(cptype.equals("Ww")) {
//			all.addAll(getTsZhaDan(sjp, zjp, "Ww"));
//		} else if(cptype.equals("WW")) {
//			all.addAll(getTsZhaDan(sjp, zjp, "WW"));
//		}
        else if (cptype.equals("4zha")) {
            all.addAll(getTsZhaDan(sjp, zjp, "4zha"));
        } else if (cptype.equals("5zha")) {
            all.addAll(getTsZhaDan(sjp, zjp, "5zha"));
        }
//		else if(cptype.equals("3w")) {
//			all.addAll(getTsZhaDan(sjp, zjp, "3w"));
//		}
        else if (cptype.equals("6zha")) {
            all.addAll(getTsZhaDan(sjp, zjp, "6zha"));
        } else if (cptype.equals("7zha")) {
            all.addAll(getTsZhaDan(sjp, zjp, "7zha"));
        } else if (cptype.equals("8zha")) {
            all.addAll(getTsZhaDan(sjp, zjp, "8zha"));
        } else if (cptype.equals("4wzha")) {
            all.addAll(getTsZhaDan(sjp, zjp, "4wzha"));
        } else if (cptype.equals("shunzi")) {
            all.addAll(getTsShunzi(sjp, zjp, "shunzi"));
            all.addAll(getTs4zha(sjp, zjp,"shunzi"));
        } else if (cptype.equals("ths")) {
            all.addAll(getTsTongHuaShun(sjp, zjp, "ths"));
        }
        List<List<String>> all1 = new ArrayList<List<String>>();
        all1.addAll(all);
        List<List<String>> all2 = new ArrayList<List<String>>();
        all2.addAll(all);
        List<List<String>> all3 = new ArrayList<List<String>>();
        for (List<String> l : all) {
            if (!TcgdSfNew.isZhadan(l)) {
                for (String p : l) {
                    for (List<String> l1 : all1) {
                        if (l1.contains(p) && l1.size() != l.size()) {
                            if (!all3.contains(l)) {
                                all3.add(0, l);
                            }
                            all2.remove(l);
                        }
                    }
                }
            }
        }
        all2.addAll(all3);

        return all2;
    }

	public static List<List<String>> getAllTsNew1(List<String> sjp, List<String> zjp,String cptype) {
		List<List<String>> all = new ArrayList<List<String>>();
		sjp = sortList(sjp, "asc");
		zjp = sortList(zjp, "asc");
		List<String> sjp1 = copyList(sjp);
		if (cptype.equals("danz")) {
            List<List<String>> ls = getTs4zha(sjp, zjp,"danz");
            if(!ls.isEmpty()){
                all.addAll(ls);
            }else{
                List<List<String>> list = getTsDanZhangNew(sjp, zjp);
                all.addAll(list);
            }
            all.addAll(getTsTongHuaShun2(sjp, zjp,"danz"));
		} else if (cptype.equals("duiz")) {
            List<List<String>> ls = getTs4zha(sjp, zjp,"duiz");
            if(!ls.isEmpty()){
                all.addAll(ls);
            }else{
                List<List<String>> list = getTsDuiZiNew(sjp, zjp);
                all.addAll(list);
            }
            all.addAll(getTsTongHuaShun2(sjp, zjp,"duiz"));
		} else if (cptype.equals("3z")) {
            List<List<String>> ls = getTs4zha(sjp, zjp,"3z");
            if(!ls.isEmpty()){
                all.addAll(ls);
            }else{
                List<List<String>> list = getTs3zhangNew(sjp, zjp);
                all.addAll(list);
            }
            all.addAll(getTsTongHuaShun2(sjp, zjp,"3z"));
		} else if (cptype.equals("4z")) {
			List<List<String>> list = getTs4zhangNew(sjp, zjp);
			all.addAll(list);
            all.addAll(getTsTongHuaShun2(sjp, zjp,"4z"));
		} else if (cptype.equals("feiji")) {
            List<List<String>> ls = getTs4zha(sjp, zjp,"feiji");
            if(!ls.isEmpty()){
                all.addAll(ls);
            }else{
                List<List<String>> list = getTsFeiJiNew(sjp, zjp);
                all.addAll(list);
            }
            all.addAll(getTsTongHuaShun2(sjp, zjp,"feiji"));
		} else if (cptype.equals("ld")) {
            List<List<String>> ls = getTs4zha(sjp, zjp,"ld");
            if(!ls.isEmpty()){
                all.addAll(ls);
            }else{
                List<List<String>> list = getTsLianDuiNew(sjp, zjp);
                all.addAll(list);
            }
            all.addAll(getTsTongHuaShun2(sjp, zjp,"ld"));
		} else if (cptype.equals("sandaidui")) {
            List<List<String>> ls = getTs4zha(sjp, zjp,"sandaidui");
            if(!ls.isEmpty()){
                all.addAll(ls);
            }else{
                List<List<String>> list = getTsSanDaiEr(paiToStringArr(sjp), paiToStringArr(zjp));
                all.addAll(list);
            }
            all.addAll(getTsTongHuaShun2(sjp, zjp,"sandaidui"));
        } else if (cptype.equals("4zha")) {
			all.addAll(getTsZhaDan(sjp, zjp, "4zha"));
            all.addAll(getTsTongHuaShun2(sjp, zjp,"4zha"));
		} else if (cptype.equals("5zha")) {
			all.addAll(getTsZhaDan(sjp, zjp, "5zha"));
            all.addAll(getTsTongHuaShun2(sjp, zjp,"5zha"));
		}
		else if (cptype.equals("6zha")) {
			all.addAll(getTsZhaDan(sjp, zjp, "6zha"));
		} else if (cptype.equals("7zha")) {
			all.addAll(getTsZhaDan(sjp, zjp, "7zha"));
		} else if (cptype.equals("8zha")) {
			all.addAll(getTsZhaDan(sjp, zjp, "8zha"));
		}  else if (cptype.equals("shunzi")) {
            List<List<String>> ls = getTs4zha(sjp, zjp,"shunzi");
             if(!ls.isEmpty()){
                 all.addAll(ls);
             }else{
                 all.addAll(getTsShunzi(sjp, zjp, "shunzi"));
             }
            all.addAll(getTsTongHuaShun2(sjp, zjp,"shunzi"));
		} else if (cptype.equals("ths")) {
//			all.addAll(getTsTongHuaShun(sjp, zjp, "ths"));
            all.addAll(getTsTongHuaShun2(sjp, zjp, "ths"));
            List<List<String>> zha6 = getTs6zha(sjp,zjp);
            if(!zha6.isEmpty()){
                all.addAll(zha6);
            }else{
                all.addAll(getTs7zha(sjp,zjp));
                all.addAll(getTs8zha(sjp,zjp));
            }
		}
        all.addAll(getTs4Wang(sjp,zjp));
		List<List<String>> all1 = new ArrayList<List<String>>();
		all1.addAll(all);
		List<List<String>> all2 = new ArrayList<List<String>>();
		all2.addAll(all);
		List<List<String>> all3 = new ArrayList<List<String>>();
		for (List<String> l : all) {
			if (!TcgdSfNew.isZhadan(l)) {
				for (String p : l) {
					for (List<String> l1 : all1) {
						if (l1.contains(p) && l1.size() != l.size()) {
							if (!all3.contains(l)) {
								all3.add(0, l);
							}
							all2.remove(l);
						}
					}
				}
			}
		}
		all2.addAll(all3);

		return all2;
	}
    /**
     * 判断一组牌是否为三带二
     * @param pai
     * @return
     */
    public static int[] paiToShortAry2(String[] pai){
        int[] ary = {0,0,0,0,0,0,0,0, 0, 0, 0, 0, 0,0,0,0,0};
        for (int i = 0; i < pai.length; i++) {
            int p = getNumbers(pai[i]);
            ary[p-3]++;
        }
        return ary;
    }
    public static boolean isSanDaiEr(String[] pai, boolean isyp) {
        if(pai.length==0) {
            return false;
        }
        pai = sortAry(pai, "asc");
        boolean bool = false;

        if(!isyp) {
            if(pai.length != 5) {
                bool = false;
                return bool;
            }
        } else {
            if(pai.length < 3 || pai.length > 5) {
                bool = false;
                return bool;
            }
        }

        int[] ar =	paiToShortAry2(pai);
        String patStr = paiToString(ar);
        //正则表达式
        String pa1 ="^[^3]*3[^3]*$";
        Pattern p = Pattern.compile(pa1);
        Matcher matcher = p.matcher(patStr);
        boolean m3 = matcher.matches();
        String pa2 ="^[^2]*2[^2]*$";
        Pattern p2 = Pattern.compile(pa2);
        Matcher matcher2 = p2.matcher(patStr);
        boolean m2 = matcher2.matches();
        if(m2 && m3){
            return true;
        }else{
            return false;
        }
    }
    /**
     * 把牌转成带花色数组
     * @param pai
     * @return
     */
    public static int[] paiToIntColorAry(String[] pai) {
        int[] iary = {0,0,0,0,0,0,0,0,0,0,0,0,0//"B3","B4","B5","B6","B7","B8","B9","B10","B11","B12","B13","B14","B15",0-12
                ,0,0,0,0,0,0,0,0,0,0,0,0,0//"R3","R4","R5","R6","R7","R8","R9","R10","R11","R12","R13","R14","R15",13-25
                ,0,0,0,0,0,0,0,0,0,0,0,0,0//"F3","F4","F5","F6","F7","F8","F9","F10","F11","F12","F13","F14","F15",26-38
                ,0,0,0,0,0,0,0,0,0,0,0,0,0//"M3","M4","M5","M6","M7","M8","M9","M10","M11","M12","M13","M14","M15"39-51
                ,0,0};//bt add 大小王52-53
        //大王W(6)小王w(5)黑B(4)红R(3)梅M(2)方F(1)
        for (int i = 0; i < pai.length; i++) {
            if(getColor(pai[i])==4) {
                iary[getNumbers(pai[i])-3] = 1;
            }
            if(getColor(pai[i])==3) {
                iary[getNumbers(pai[i])-3+13] = 1;
            }
            if(getColor(pai[i])==1) {
                iary[getNumbers(pai[i])-3+26] = 1;
            }
            if(getColor(pai[i])==2) {
                iary[getNumbers(pai[i])-3+39] = 1;
            }
            //bt add 大小王
            if(pai[i].equals("w16")){
                iary[52] =1;
            }
            if(pai[i].equals("W17")){
                iary[53] =1;
            }
        }
        return iary;
    }
    /**
     * 根据下标转成牌
     * @param i
     * @return
     */
    public static String indexToPai(int i){
        String color = "";
        if(i/13==0) {
            color = "B";
        } else if(i/13==1) {
            color = "R";
        } else if(i/13==2) {
            color = "F";
        } else if(i/13==3) {
            color = "M";
        } else if(i/13==4) {
            color = "W";
        } else if(i/13==5) {
            color = "w";
        }

        return color+(i%13+3);
    }
    public static String[] delWings(String[] pai) {
        List<String> stlist = new ArrayList<String>();
        int[] ary = paiToIntColorAry(pai);
        if(pai.length == 5) {
            for (int i = 0; i < 13; i++) {
                int total = ary[i] + ary[i+13] + ary[i+26] + ary[i+39];
                if(total >= 3) {
                    int flag = 0;
                    for (int j = i; j < i+50; j+=13) {
                        if(ary[j] == 1 && flag < 3) {
                            ary[j] = 0;
                            String pstr = indexToPai(j);
                            stlist.add(pstr);
                            flag++;
                        }
                    }
                }
            }
        } else if(pai.length == 10) {
            for (int i = 0; i < 12-1; i++) {
                int total = ary[i] + ary[i+13] + ary[i+26] + ary[i+39];
                int total1 = ary[i+1] + ary[i+13+1] + ary[i+26+1] + ary[i+39+1];
                if(total >= 3 && total1 >= 3) {
                    int flag = 0;
                    for (int j = i; j < i+50; j+=13) {
                        if(ary[j] == 1 && flag < 3) {
                            ary[j] = 0;
                            String pstr = indexToPai(j);
                            stlist.add(pstr);
                            flag++;
                        }
                    }

                    flag = 0;
                    for (int j = i+1; j < i+50; j+=13) {
                        if(ary[j] == 1 && flag < 3) {
                            ary[j] = 0;
                            String pstr = indexToPai(j);
                            stlist.add(pstr);
                            flag++;
                        }
                    }
                }
            }
        } else if(pai.length == 15) {
            for (int i = 0; i < 12-2; i++) {
                int total = ary[i] + ary[i+13] + ary[i+26] + ary[i+39];
                int total1 = ary[i+1] + ary[i+13+1] + ary[i+26+1] + ary[i+39+1];
                int total2 = ary[i+2] + ary[i+13+2] + ary[i+26+2] + ary[i+39+2];
                if(total >= 3 && total1 >= 3 && total2 >= 3) {
                    int flag = 0;
                    for (int j = i; j < i+50; j+=13) {
                        if(ary[j] == 1 && flag < 3) {
                            ary[j] = 0;
                            String pstr = indexToPai(j);
                            stlist.add(pstr);
                            flag++;
                        }
                    }

                    flag = 0;
                    for (int j = i+1; j < i+50; j+=13) {
                        if(ary[j] == 1 && flag < 3) {
                            ary[j] = 0;
                            String pstr = indexToPai(j);
                            stlist.add(pstr);
                            flag++;
                        }
                    }

                    flag = 0;
                    for (int j = i+2; j < i+50; j+=13) {
                        if(ary[j] == 1 && flag < 3) {
                            ary[j] = 0;
                            String pstr = indexToPai(j);
                            stlist.add(pstr);
                            flag++;
                        }
                    }
                }
            }
        }
        String[] arys = new String[stlist.size()];
        for (int i = 0; i < stlist.size(); i++) {
            arys[i] = stlist.get(i);
        }
        return arys;
    }
    /**
     * 获取带的牌
     * @param tip
     * @param zjp
     * @param num 带几张牌
     * @return
     */
//	public static List<String> getDaiPai(List<String> tip, String[] zjp, int num) {
    public static List<List<String>> getDaiPai(List<String> tip, String[] zjp, int num) {
        List<String> daipai = new ArrayList<String>();
        List<List<String>> list = new ArrayList<List<String>>();
        for (int i = 0; i < zjp.length; i++) {
            for (int j = 0; j < tip.size(); j++) {
                if(zjp[i].equals(tip.get(j))) {
                    zjp = (String[]) ArrayUtils.remove(zjp, i);
                }
            }
        }
        //剩余的牌数小于要带的牌数则直接返回剩余牌
        if(zjp.length <= num) {
            for (int i = 0; i < zjp.length; i++) {
                daipai.add(zjp[i]);
            }
            list.add(daipai);
            return list;
        }

        combination(zjp, 2, list, 2);
        return list;
    }


    public static List<List<String>> getTsSanDaiEr(String[] sjp, String[] zjp) {
        List<List<String>> all = new ArrayList<List<String>>();
        List<String> aa = new ArrayList<>();
        //获取三同牌
        int index =0;
        int m_index =0;
        for (String p1:sjp){
            m_index=  getNumbers(p1);
            for (String p:sjp){
                if(p1.equals(p)){
                    index++;
                }
            }
            if(index==3){
                break;
            }
        }
        if(isSanDaiEr(sjp, true)) {
//            sjp = delWings(sjp);
            int slen = sjp.length;
            int m = m_index;
            if(slen <= zjp.length) {
                for (int j = 0; j < zjp.length-2; j++) {
                    int n = getNumbers(zjp[j]);
                    int k = getNumbers(zjp[j+1]);
                    int q = getNumbers(zjp[j+2]);
                    if(n > m && n==k && n==q && n!=16) {
                        List<String> l = new ArrayList<String>();
                        l.add(zjp[j]);
                        l.add(zjp[j+1]);
                        l.add(zjp[j+2]);
                        List<List<String>> dplists = getDaiPai(l, zjp, 2);
                        if(dplists.size() > 0) {
                            for (int i = 0; i < dplists.size(); i++) {
                                List<String> l1 = new ArrayList<String>();
                                {
                                    if(dplists.get(i).size()==2) {//三带二/飞机    只剩下三带一  是不能出的
                                        //判断带的牌是不是一对
                                        List<String> isDui = dplists.get(i);
                                        int  dp1 = getNumbers(isDui.get(0));
                                        int  dp2 = getNumbers(isDui.get(1));
                                        if(dp1 == dp2){
                                            l1.addAll(l);
                                            l1.addAll(dplists.get(i));
                                            all.add(l1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //去重一下
        List<List<String>> allNew = new ArrayList<List<String>>();
        allNew.addAll(all);
        for (int i = 0; i < all.size()-1; i++) {
            List<String> ts = all.get(i);
            List<String> ts1 = all.get(i+1);
            List<String> tss = sorList(ts, "asc");
            List<String> tss1 = sorList(ts1, "asc");
            if(tss.containsAll(tss1)) {
                allNew.remove(ts);
            }
        }
        return allNew;
    }
    /**
     * 将牌倒序排列  花色按 大王W小王w黑B红R梅M方F
     * @param list
     * @return
     */
    public static List<String> sorList(List<String> list, String type) {

        for (int i = 0; i < list.size()-1; i++) {
            for (int j = 0; j < list.size()-i-1; j++) {
                if("asc".equals(type)) {
                    if(comparePai(list.get(j), list.get(j+1)) > 0){    //把小的值交换到后面
                        String temp = list.get(j);
                        list.set(j, list.get(j+1));
                        list.set(j+1, temp);
                    }
                } else if("desc".equals(type)){
                    if(comparePai(list.get(j), list.get(j+1)) < 0){    //把小的值交换到后面
                        String temp = list.get(j);
                        list.set(j, list.get(j+1));
                        list.set(j+1, temp);
                    }
                }

            }
        }

        return list;
    }
    private static List<List<String>> getTsFeiJiNew(List<String> sjp, List<String> zjp) {
        long d1 = System.currentTimeMillis();
        List<List<String>> all = new ArrayList<List<String>>();
        sjp = sortList(sjp, "asc");
        zjp = sortList(zjp, "asc");
        //获取所有的8炸
        List<String> sjp1 = new ArrayList<String>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        List<List<String>> _8zhaList = getTs8zha(sjp1, zjp1);
        //将8炸里出现的牌remove掉
        for (List<String> list : _8zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _7zhaList = getTs7zha(sjp1, zjp1);
        for (List<String> list : _7zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _4wList = getTs4Wang(sjp1, zjp1);
        for (List<String> list : _4wList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _6zhaList = getTs6zha(sjp1, zjp1);
        for (List<String> list : _6zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
//		List<List<String>> _3wList = getTs3Wang(sjp1, zjp1);
//		for(List<String> list : _3wList) {
//			for(String pai : list) {
//				zjp1.remove(pai);
//			}
//		}
        List<List<String>> _5zhaList = getTs5zha(sjp1, zjp1);
        for (List<String> list : _5zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
//		List<List<String>> _2dwList = getTs2DaWang(sjp1, zjp1);
//		for(List<String> list : _2dwList) {
//			for(String pai : list) {
//				zjp1.remove(pai);
//			}
//		}
//		List<List<String>> _1dw1xwList = getTs1dw1xw(sjp1, zjp1);
//		for(List<String> list : _1dw1xwList) {
//			for(String pai : list) {
//				zjp1.remove(pai);
//			}
//		}
//		List<List<String>> _2xwList = getTs2XiaoWang(sjp1, zjp1);
//		for(List<String> list : _2xwList) {
//			for(String pai : list) {
//				zjp1.remove(pai);
//			}
//		}
        List<List<String>> _ht50kList = getTsHt50k(sjp1, zjp1);
        for (List<String> list : _ht50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _hx50kList = getTsHx50k(sjp1, zjp1);
        for (List<String> list : _hx50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _mh50kList = getTsMh50k(sjp1, zjp1);
        for (List<String> list : _mh50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _fk50kList = getTsFk50k(sjp1, zjp1);
        for (List<String> list : _fk50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _min50kList = getTsMin50k(sjp1, zjp1);
        if (_min50kList.size() > 1) {
            List<String> no1 = _min50kList.get(0);
            _min50kList = new ArrayList<List<String>>();
            _min50kList.add(no1);
        }
        for (List<String> list : _min50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }

        List<List<String>> _feijiTsList = getTsFeiJi(listToStringAry(sjp), listToStringAry(zjp));

        sjp1 = new ArrayList<String>();
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        List<List<String>> _4zhangList = getTs4zhang(sjp1, zjp1);
        for (List<String> list : _4zhangList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
//		sjp1 = new ArrayList<String>();
//		sjp1.add("F3");sjp1.add("F3");sjp1.add("F3");
//		List<List<String>> _3zhangList = getTs3zhang1(sjp, zjp1);
//		for(List<String> list : _3zhangList) {
//			for(String pai : list) {
//				zjp1.remove(pai);
//			}
//		}

        all.addAll(_feijiTsList);
//		all.addAll(_3zhangList);
        all.addAll(_min50kList);
        all.addAll(_fk50kList);
        all.addAll(_mh50kList);
        all.addAll(_hx50kList);
        all.addAll(_ht50kList);
//		all.addAll(_1dw1xwList);
//		all.addAll(_2xwList);
//		all.addAll(_2dwList);
        all.addAll(_5zhaList);
//		all.addAll(_3wList);
        all.addAll(_6zhaList);
        all.addAll(_4wList);
        all.addAll(_7zhaList);
        all.addAll(_8zhaList);

//		List<List<String>> __3zhangList = getTs3zhang1(sjp, zjp);
//		List<List<String>> __3zhangList1 = new ArrayList<List<String>>();
//		__3zhangList1.addAll(__3zhangList);
//		for(List<String> list : __3zhangList) {
//			for(String pai : list) {
//				for(List<String> _list : _3zhangList) {
//					for(String _pai : _list) {
//						if(getNumbers(_pai) == getNumbers(pai)) {
//							__3zhangList1.remove(list);
//						}
//					}
//				}
//			}
//		}
//		all.addAll(__3zhangList1);
        long d2 = System.currentTimeMillis();
//        //System.out.println("总耗时：" + (d2 - d1));
        return all;
    }

    public static List<List<String>> getTsZhaDan1(List<String> sjp, List<String> zjp, String type) {
        List<List<String>> all = new ArrayList<List<String>>();
        if (type.equals("danz") || type.equals("duiz") || type.equals("3z") || type.equals("4z")
                || type.equals("ld") || type.equals("l3z") || type.equals("l4z") || type.equals("noBoom")) {
            all.addAll(getTsMin50k(sjp, zjp));
            all.addAll(getTsFk50k(sjp, zjp));
            all.addAll(getTsMh50k(sjp, zjp));
            all.addAll(getTsHx50k(sjp, zjp));
            all.addAll(getTsHt50k(sjp, zjp));
//			all.addAll(getTs1dw1xw(sjp, zjp));
//			all.addAll(getTs2XiaoWang(sjp, zjp));
//			all.addAll(getTs2DaWang(sjp, zjp));
            all.addAll(getTs5zha(sjp, zjp));
//			all.addAll(getTs3Wang(sjp, zjp));
            all.addAll(getTs6zha(sjp, zjp));
            all.addAll(getTs4Wang(sjp, zjp));
            all.addAll(getTs7zha(sjp, zjp));
            all.addAll(getTs8zha(sjp, zjp));
        } else if (type.equals("min50k")) {
            all.addAll(getTsFk50k(sjp, zjp));
            all.addAll(getTsMh50k(sjp, zjp));
            all.addAll(getTsHx50k(sjp, zjp));
            all.addAll(getTsHt50k(sjp, zjp));
//			all.addAll(getTs1dw1xw(sjp, zjp));
//			all.addAll(getTs2XiaoWang(sjp, zjp));
//			all.addAll(getTs2DaWang(sjp, zjp));
            all.addAll(getTs5zha(sjp, zjp));
//			all.addAll(getTs3Wang(sjp, zjp));
            all.addAll(getTs6zha(sjp, zjp));
            all.addAll(getTs4Wang(sjp, zjp));
            all.addAll(getTs7zha(sjp, zjp));
            all.addAll(getTs8zha(sjp, zjp));
        } else if (type.equals("f50k")) {
            all.addAll(getTsMh50k(sjp, zjp));
            all.addAll(getTsHx50k(sjp, zjp));
            all.addAll(getTsHt50k(sjp, zjp));
//			all.addAll(getTs1dw1xw(sjp, zjp));
//			all.addAll(getTs2XiaoWang(sjp, zjp));
//			all.addAll(getTs2DaWang(sjp, zjp));
            all.addAll(getTs5zha(sjp, zjp));
//			all.addAll(getTs3Wang(sjp, zjp));
            all.addAll(getTs6zha(sjp, zjp));
            all.addAll(getTs4Wang(sjp, zjp));
            all.addAll(getTs7zha(sjp, zjp));
            all.addAll(getTs8zha(sjp, zjp));
        } else if (type.equals("m50k")) {
            all.addAll(getTsHx50k(sjp, zjp));
            all.addAll(getTsHt50k(sjp, zjp));
//			all.addAll(getTs1dw1xw(sjp, zjp));
//			all.addAll(getTs2XiaoWang(sjp, zjp));
//			all.addAll(getTs2DaWang(sjp, zjp));
            all.addAll(getTs5zha(sjp, zjp));
//			all.addAll(getTs3Wang(sjp, zjp));
            all.addAll(getTs6zha(sjp, zjp));
            all.addAll(getTs4Wang(sjp, zjp));
            all.addAll(getTs7zha(sjp, zjp));
            all.addAll(getTs8zha(sjp, zjp));
        } else if (type.equals("r50k")) {
            all.addAll(getTsHt50k(sjp, zjp));
//			all.addAll(getTs1dw1xw(sjp, zjp));
//			all.addAll(getTs2XiaoWang(sjp, zjp));
//			all.addAll(getTs2DaWang(sjp, zjp));
            all.addAll(getTs5zha(sjp, zjp));
//			all.addAll(getTs3Wang(sjp, zjp));
            all.addAll(getTs6zha(sjp, zjp));
            all.addAll(getTs4Wang(sjp, zjp));
            all.addAll(getTs7zha(sjp, zjp));
            all.addAll(getTs8zha(sjp, zjp));
        }
//		else if(type.equals("ww")) {
//			all.addAll(getTs1dw1xw(sjp, zjp));
//			all.addAll(getTs2DaWang(sjp, zjp));
//			all.addAll(getTs5zha(sjp, zjp));
//			all.addAll(getTs3Wang(sjp, zjp));
//			all.addAll(getTs6zha(sjp, zjp));
//			all.addAll(getTs4Wang(sjp, zjp));
//			all.addAll(getTs7zha(sjp, zjp));
//			all.addAll(getTs8zha(sjp, zjp));
//		} else if(type.equals("Ww")) {
//			all.addAll(getTs2DaWang(sjp, zjp));
//			all.addAll(getTs5zha(sjp, zjp));
//			all.addAll(getTs3Wang(sjp, zjp));
//			all.addAll(getTs6zha(sjp, zjp));
//			all.addAll(getTs4Wang(sjp, zjp));
//			all.addAll(getTs7zha(sjp, zjp));
//			all.addAll(getTs8zha(sjp, zjp));
//		} else if(type.equals("WW")) {
//			all.addAll(getTs5zha(sjp, zjp));
//			all.addAll(getTs3Wang(sjp, zjp));
//			all.addAll(getTs6zha(sjp, zjp));
//			all.addAll(getTs4Wang(sjp, zjp));
//			all.addAll(getTs7zha(sjp, zjp));
//			all.addAll(getTs8zha(sjp, zjp));
//		}
        else if (type.equals("5zha")) {
            all.addAll(getTs5zha(sjp, zjp));
            all.addAll(getTs3Wang(sjp, zjp));
            all.addAll(getTs6zha(sjp, zjp));
            all.addAll(getTs4Wang(sjp, zjp));
            all.addAll(getTs7zha(sjp, zjp));
            all.addAll(getTs8zha(sjp, zjp));
        } else if (type.equals("3w")) {
            all.addAll(getTs6zha(sjp, zjp));
            all.addAll(getTs4Wang(sjp, zjp));
            all.addAll(getTs7zha(sjp, zjp));
            all.addAll(getTs8zha(sjp, zjp));
        } else if (type.equals("6zha")) {
            all.addAll(getTs6zha(sjp, zjp));
            all.addAll(getTs4Wang(sjp, zjp));
            all.addAll(getTs7zha(sjp, zjp));
            all.addAll(getTs8zha(sjp, zjp));
        } else if (type.equals("4w")) {
            all.addAll(getTs7zha(sjp, zjp));
            all.addAll(getTs8zha(sjp, zjp));
        } else if (type.equals("7zha")) {
            all.addAll(getTs7zha(sjp, zjp));
            all.addAll(getTs8zha(sjp, zjp));
        } else if (type.equals("8zha")) {
            all.addAll(getTs8zha(sjp, zjp));
        }

        return all;
    }

    public static List<List<String>> getTsZhaDan(List<String> sjp, List<String> zjp, String type) {
        List<List<String>> all = new ArrayList<List<String>>();
        if (type.equals("4zha")) {
            List<String> zjp1 = new ArrayList<String>(zjp);
            List<String> zjp2 = new ArrayList<String>(zjp);
            int[] a = paiToIntAry(paiToStringArr(zjp2));
            int sjpnum = getNumbers(sjp.get(0));
            for (int i = 0; i < a.length; i++) {
                int a1 = a[i];
                if (a1 >= 4) {
                    int t = i + 3;
                    if (t > sjpnum) {
                        List<String> ts1 = new ArrayList<>();
                        for (String p2 : zjp2) {
                            if (getNumbers(p2) == t) {
                                ts1.add(p2);
                            }
                            if (ts1.size() == 4) {
                                all.add(ts1);
                                break;
                            }
                        }
                    }
                }
            }
            sjp = sortList(sjp, "asc");
            List<String> sjp1 = new ArrayList<String>();

            sjp1.add("F3");
            sjp1.add("F3");
            sjp1.add("F3");
            sjp1.add("F3");
            sjp1.add("F4");
            sjp1.add("F4");
            sjp1.add("F4");
            sjp1.add("F4");
            List<List<String>> _8zhaList = getTs8zha(sjp1, zjp1);
            //将8炸里出现的牌remove掉
            for (List<String> list : _8zhaList) {
                for (String pai : list) {
                    zjp1.remove(pai);
                }
            }
            List<List<String>> _7zhaList = getTs7zha(sjp1, zjp1);
            for (List<String> list : _7zhaList) {
                for (String pai : list) {
                    zjp1.remove(pai);
                }
            }
            List<List<String>> _4wList = getTs4Wang(sjp1, zjp1);
            for (List<String> list : _4wList) {
                for (String pai : list) {
                    zjp1.remove(pai);
                }
            }
            List<List<String>> _6zhaList = getTs6zha(sjp1, zjp1);
            for (List<String> list : _6zhaList) {
                for (String pai : list) {
                    zjp1.remove(pai);
                }
            }
            List<List<String>> _3wList = getTs3Wang(sjp1, zjp1);
            for (List<String> list : _3wList) {
                for (String pai : list) {
                    zjp1.remove(pai);
                }
            }
            List<List<String>> _5zhaList = getTs5zha(sjp, zjp1);
            for (List<String> list : _5zhaList) {
                for (String pai : list) {
                    zjp1.remove(pai);
                }
            }
            all.addAll(_5zhaList);
//			all.addAll(_3wList);
            all.addAll(_6zhaList);
            all.addAll(_4wList);
            all.addAll(_7zhaList);
            all.addAll(_8zhaList);
        } else if (type.equals("5zha")) {
            sjp = sortList(sjp, "asc");
            zjp = sortList(zjp, "asc");
            //获取所有的8炸
            List<String> sjp1 = new ArrayList<String>();
            List<String> zjp1 = new ArrayList<String>();
            zjp1.addAll(zjp);
            sjp1.add("F3");
            sjp1.add("F3");
            sjp1.add("F3");
            sjp1.add("F3");
            sjp1.add("F4");
            sjp1.add("F4");
            sjp1.add("F4");
            sjp1.add("F4");
            List<List<String>> _8zhaList = getTs8zha(sjp1, zjp1);
            //将8炸里出现的牌remove掉
            for (List<String> list : _8zhaList) {
                for (String pai : list) {
                    zjp1.remove(pai);
                }
            }
            List<List<String>> _7zhaList = getTs7zha(sjp1, zjp1);
            for (List<String> list : _7zhaList) {
                for (String pai : list) {
                    zjp1.remove(pai);
                }
            }
            List<List<String>> _4wList = getTs4Wang(sjp1, zjp1);
            for (List<String> list : _4wList) {
                for (String pai : list) {
                    zjp1.remove(pai);
                }
            }
            List<List<String>> _6zhaList = getTs6zha(sjp1, zjp1);
            for (List<String> list : _6zhaList) {
                for (String pai : list) {
                    zjp1.remove(pai);
                }
            }
            List<List<String>> _3wList = getTs3Wang(sjp1, zjp1);
            for (List<String> list : _3wList) {
                for (String pai : list) {
                    zjp1.remove(pai);
                }
            }
            List<List<String>> _5zhaList = getTs5zha(sjp, zjp1);
            for (List<String> list : _5zhaList) {
                for (String pai : list) {
                    zjp1.remove(pai);
                }
            }
            all.addAll(_5zhaList);
//			all.addAll(_3wList);
            all.addAll(_6zhaList);
            all.addAll(_4wList);
            all.addAll(_7zhaList);
            all.addAll(_8zhaList);
        } else if (type.equals("6zha")) {
            sjp = sortList(sjp, "asc");
            zjp = sortList(zjp, "asc");
            //获取所有的8炸
            List<String> sjp1 = new ArrayList<String>();
            List<String> zjp1 = new ArrayList<String>();
            zjp1.addAll(zjp);
            sjp1.add("F3");
            sjp1.add("F3");
            sjp1.add("F3");
            sjp1.add("F3");
            sjp1.add("F4");
            sjp1.add("F4");
            sjp1.add("F4");
            sjp1.add("F4");
            List<List<String>> _8zhaList = getTs8zha(sjp1, zjp1);
            //将8炸里出现的牌remove掉
            for (List<String> list : _8zhaList) {
                for (String pai : list) {
                    zjp1.remove(pai);
                }
            }
            List<List<String>> _7zhaList = getTs7zha(sjp1, zjp1);
            for (List<String> list : _7zhaList) {
                for (String pai : list) {
                    zjp1.remove(pai);
                }
            }
            List<List<String>> _4wList = getTs4Wang(sjp1, zjp1);
            for (List<String> list : _4wList) {
                for (String pai : list) {
                    zjp1.remove(pai);
                }
            }
            List<List<String>> _6zhaList = getTs6zha(sjp, zjp1);
            for (List<String> list : _6zhaList) {
                for (String pai : list) {
                    zjp1.remove(pai);
                }
            }
            all.addAll(_6zhaList);
            all.addAll(_4wList);
            all.addAll(_7zhaList);
            all.addAll(_8zhaList);
        } else if (type.equals("4w")) {
            sjp = sortList(sjp, "asc");
            zjp = sortList(zjp, "asc");
            //获取所有的8炸
            List<String> sjp1 = new ArrayList<String>();
            List<String> zjp1 = new ArrayList<String>();
            zjp1.addAll(zjp);
            sjp1.add("F3");
            sjp1.add("F3");
            sjp1.add("F3");
            sjp1.add("F3");
            sjp1.add("F4");
            sjp1.add("F4");
            sjp1.add("F4");
            sjp1.add("F4");
            List<List<String>> _8zhaList = getTs8zha(sjp1, zjp1);
            //将8炸里出现的牌remove掉
            for (List<String> list : _8zhaList) {
                for (String pai : list) {
                    zjp1.remove(pai);
                }
            }
            List<List<String>> _7zhaList = getTs7zha(sjp1, zjp1);
            for (List<String> list : _7zhaList) {
                for (String pai : list) {
                    zjp1.remove(pai);
                }
            }
            all.addAll(_7zhaList);
            all.addAll(_8zhaList);
        } else if (type.equals("7zha")) {
            sjp = sortList(sjp, "asc");
            zjp = sortList(zjp, "asc");
            //获取所有的8炸
            List<String> sjp1 = new ArrayList<String>();
            List<String> zjp1 = new ArrayList<String>();
            zjp1.addAll(zjp);
            sjp1.add("F3");
            sjp1.add("F3");
            sjp1.add("F3");
            sjp1.add("F3");
            sjp1.add("F4");
            sjp1.add("F4");
            sjp1.add("F4");
            sjp1.add("F4");
            List<List<String>> _8zhaList = getTs8zha(sjp1, zjp1);
            //将8炸里出现的牌remove掉
            for (List<String> list : _8zhaList) {
                for (String pai : list) {
                    zjp1.remove(pai);
                }
            }
            List<List<String>> _7zhaList = getTs7zha(sjp, zjp1);
            for (List<String> list : _7zhaList) {
                for (String pai : list) {
                    zjp1.remove(pai);
                }
            }
            all.addAll(_7zhaList);
            all.addAll(_8zhaList);
        } else if (type.equals("8zha")) {
            all.addAll(getTs8zha(sjp, zjp));
        }

        return all;
    }

    /**
     * 获取出牌类型
     *
     * @param pai
     * @return
     */
    public static String getCpType(List<String> pai) {
        String type = "";
        if (isDanzhang(pai)) {
            type = "danz";
        } else if (isDuizi(pai)) {
            type = "duiz";
        } else if (is3zhang(pai)) {
            type = "3z";
        } else if (is4zhang(pai)) {
            type = "4z";
        } else if (isLiandui(pai)) {
            type = "ld";
        } else if (isLian3zhang(pai)) {
            type = "l3z";
        } else if (is3dai1dui(pai)) {
            type = "3d2";
        } else if (isLian4zhang(pai)) {
            type = "l4z";
        } else if (isMin510k(pai)) {
            type = "min50k";
        } else if (isFk510k(pai)) {
            type = "f50k";
        } else if (isMh510k(pai)) {
            type = "m50k";
        } else if (isHx510k(pai)) {
            type = "r50k";
        } else if (isHt510k(pai)) {
            type = "b50k";
        }
//		else if(is2xiaowang(pai)) {
//			type = "ww";
//		}  else if(is1dw1xw(pai)) {
//			type = "Ww";
//		} else if(is2dawang(pai)) {
//			type = "WW";
//		}
        else if (is4zha(pai)) {
            type = "4zha";
        } else if (is5zha(pai)) {
            type = "5zha";
        } else if (is3wang(pai)) {
            type = "3w";
        } else if (is6zha(pai)) {
            type = "6zha";
        } else if (is7zha(pai)) {
            type = "7zha";
        } else if (is8zha(pai)) {
            type = "8zha";
        } else if (is4wang(pai)) {
            type = "4w";
        }

        String shunzitype = isShunzi(pai);
        if (!shunzitype.equals("false")) {
            type = shunzitype;// thx  or  shunzi
        }
        String fjtype = isFeiJi2(pai);
        if (!fjtype.equals("false")) {
//			 if("3n".equals(fjtype)){
//			 	type = "fjd0";//飞机不带 连三张
//			 }else if("3n2n".equals(fjtype)){
//			 	type = "fjdd";//飞机带对
//			 }
            type = "feiji";
        }
        return type;
    }

    /**
     * 获取出牌类型
     *
     * @param pai
     * @return
     */
    public static String getCpType2(List<String> pai, int issadaidui, int isfeijiDld) {
        String type = "";
        if (isDanzhang(pai)) {
            type = "danz";
        } else if (isDuizi(pai)) {
            type = "duiz";
        } else if (isLiandui3(pai)) {
            type = "ld";
        } else if (is3zhang(pai)) {
            type = "3z";
        } else if (is4zhang(pai)) {
            type = "4z";
        } else if (isLian3zhang(pai)) {
            type = "l3z";
        } else if (is3dai1dui(pai) && issadaidui == 1) {
            type = "3d2";
        }
//        else if (isLian4zhang(pai)) {
//            type = "l4z";
//        } else if (isMin510k(pai)) {
//            type = "min50k";
//        } else if (isFk510k(pai)) {
//            type = "f50k";
//        } else if (isMh510k(pai)) {
//            type = "m50k";
//        } else if (isHx510k(pai)) {
//            type = "r50k";
//        } else if (isHt510k(pai)) {
//            type = "b50k";
//        }
//		else if(is2xiaowang(pai)) {
//			type = "ww";
//		}  else if(is1dw1xw(pai)) {
//			type = "Ww";
//		} else if(is2dawang(pai)) {
//			type = "WW";
//		}
        else if (is5zha(pai)) {
            type = "5zha";
        }
//        else if (is3wang(pai)) {
//            type = "3w";
//        }
        else if (is6zha(pai)) {
            type = "6zha";
        } else if (is7zha(pai)) {
            type = "7zha";
        } else if (is8zha(pai)) {
            type = "8zha";
        } else if (is4wang(pai)) {
            type = "4w";
        }

        String shunzitype = isShunzi(pai);
        if (!shunzitype.equals("false")) {
            type = shunzitype;// thx  or  shunzi
        }
        String fjtype = isFeiJi2(pai);
        if ("3n".equals(fjtype)) {
            type = "fjd0";//飞机不带 连三张
        } else if ("3n2n".equals(fjtype) && isfeijiDld == 1) {
            type = "fjdd";//飞机带对
        }


        return type;
    }

    public static boolean isFeiJi(String[] pai) {
        if (pai.length == 0) {
            return false;
        }
        boolean bool = false;

        pai = sortAry(pai, "asc");
        //找出里面所有3个相同的
        //判断三个相同的是否连续
        //没连续则false
        //有几个连续则需要几个翅膀，若翅膀数小于剩余牌数，则false
        //若翅膀数大于剩余牌数，一、若没牌了true，二、若有牌则false
        List<List<String>> planeList = new ArrayList<List<String>>();
        for (int i = 0; i < pai.length - 2; i++) {
            List<String> plane = new ArrayList<String>();
            int m = getNumbers(pai[i]);
            int n = getNumbers(pai[i + 1]);
            int k = getNumbers(pai[i + 2]);
//            if (m == n && m == k && m != 15) {
             if (m == n && m == k ) {//可以连到2
                plane.add(pai[i]);
                plane.add(pai[i + 1]);
                plane.add(pai[i + 2]);
                planeList.add(plane);
            }
        }

        List<List<String>> planeList1 = new ArrayList<List<String>>();
        List<List<String>> planeList2 = new ArrayList<List<String>>();
        planeList1.addAll(planeList);
        for (List<String> l : planeList1) {
            if (!isHave(l, planeList2)) {
                planeList2.add(l);
            }
        }
        planeList = new ArrayList<List<String>>();
        planeList.addAll(planeList2);
        int psize = planeList.size();
        int pailength = pai.length;
        if (psize > 1) {//至少有2个三张相同的
            //判断连续的有几个，并把连续的保存下来
            int lxgs = 1;
            int lxgs1 = 1;
            // 取出改牌组里三同连续个数最大的num
            for (int i = 0; i < planeList.size() - 1; i++) {
                int m = getNumbers(planeList.get(i).get(0));
                int n = getNumbers(planeList.get(i + 1).get(0));
                if (n - m == 1) {
                    lxgs++;
                }
            }
            //飞机不带翅膀 3*n
            if (pailength == 3 * lxgs) {
//				 System.out.println("3n");
                bool = true;
            }
            //飞机带单翅膀3*n +n

            if (pailength > 3 * lxgs && (pailength - 3 * lxgs == lxgs)) {
                //n单张
                bool = false;
            }
            if (pailength > 3 * lxgs && (pailength - 3 * lxgs == 2 * lxgs)) {
                // n对子
                String str = planeList.toString();
                List<String> syplist = new ArrayList<String>();
                for (int i = 0; i < pai.length; i++) {
                    if (!str.contains(pai[i])) {
                        syplist.add(pai[i]);
                    }
                }
                if (syplist.size() == 2 * lxgs) {
                    if (isLiandui(syplist)) {
                        bool = true;
                    }
                }
            }
            //飞机带双翅膀3*n+2*n
        }
        return bool;
    }

    public static String isFeiJi2(List<String> pai2) {
        List<String> pai = new ArrayList(pai2);
        if (pai.size() == 0) {
            return "";
        }
        List<Integer> feiji222333 =  new ArrayList<>();
        feiji222333.add(15); feiji222333.add(15); feiji222333.add(15);
        feiji222333.add(3);  feiji222333.add(3);  feiji222333.add(3);
        List<Integer>  a =loadValList(pai2);
        if(a.containsAll(feiji222333)){
            return "3n";
        }
        String bool = "";
        pai = sortPai(pai, "asc");
        //找出里面所有3个相同的
        //判断三个相同的是否连续
        //没连续则false
        //有几个连续则需要几个翅膀，若翅膀数小于剩余牌数，则false
        //若翅膀数大于剩余牌数，一、若没牌了true，二、若有牌则false
        List<List<String>> planeList = new ArrayList<List<String>>();
        for (int i = 0; i < pai.size() - 2; i++) {
            List<String> plane = new ArrayList<String>();
            int m = getNumbers(pai.get(i));
            int n = getNumbers(pai.get(i + 1));
            int k = getNumbers(pai.get(i + 2));
            if (m == n && m == k && m != 16) {
                plane.add(pai.get(i));
                plane.add(pai.get(i + 1));
                plane.add(pai.get(i + 2));
                planeList.add(plane);
            }
        }
        List<List<String>> planeList1 = new ArrayList<List<String>>();
        List<List<String>> planeList2 = new ArrayList<List<String>>();
        planeList1.addAll(planeList);
        for (List<String> l : planeList1) {
            if (!isHave(l, planeList2)) {
                planeList2.add(l);
            }
        }
        planeList = new ArrayList<List<String>>();
        planeList.addAll(planeList2);
        int psize = planeList.size();
        int pailength = pai.size();
        if (psize > 1) {//至少有2个三张相同的
            //判断连续的有几个，并把连续的保存下来
            int lxgs = 1;
            int lxgs1 = 1;
            // 取出改牌组里三同连续个数最大的num
            for (int i = 0; i < planeList.size() - 1; i++) {
                int m = getNumbers(planeList.get(i).get(0));
                int n = getNumbers(planeList.get(i + 1).get(0));
                if (m - n == 1) {
                    lxgs++;
                }
            }
            //飞机不带翅膀 3*n
            if (pailength == 3 * lxgs) {
//				 System.out.println("3n");
                bool = "3n";
            }
            //飞机带单翅膀3*n +n

            if (pailength > 3 * lxgs && (pailength - 3 * lxgs == lxgs)) {
                //n单张
//				bool = "3n1";
                bool = "false";
            }
            if (pailength > 3 * lxgs && (pailength - 3 * lxgs == 2 * lxgs)) {
                // n对子
                String str = planeList.toString();
                List<String> syplist = new ArrayList<String>();
                for (int i = 0; i < pai.size(); i++) {
                    if (!str.contains(pai.get(i))) {
                        syplist.add(pai.get(i));
                    }
                }
                sortList(syplist, "asc");
                if (syplist.size() == 2 * lxgs) {
                    if (isLiandui(syplist)) {
                        return "3n2n";
                    }
                }
            }
            //飞机带双翅膀3*n+2*n
        }
        return bool;
    }

    /**
     * 是否为三带一对子
     *
     * @param pai
     * @return
     */
    public static boolean is3dai1dui(List<String> pai) {
        if (pai.size() == 0) {
            return false;
        }

        boolean bool = false;
        if (is5zha(pai)) {
            return false;
        }

        if (pai.size() < 3 || pai.size() > 5) {
            bool = false;
            return bool;
        }
        int[] ar = paiToShortAry(pai);
        String patStr = paiToString(ar);
        //正则表达式
        String pa1 = "^[^3]*3[^3]*$";
        Pattern p = Pattern.compile(pa1);
        Matcher matcher = p.matcher(patStr);
        boolean m3 = matcher.matches();
        String pa2 = "^[^2]*2[^2]*$";
        Pattern p2 = Pattern.compile(pa2);
        Matcher matcher2 = p2.matcher(patStr);
        boolean m2 = matcher2.matches();
        if (m2 && m3) {
            return true;
        } else {
            return false;
        }
    }

    public static String paiToString(int[] pai) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < pai.length; i++) {
            sb.append(String.valueOf(pai[i]));
        }
        return sb.toString();
    }


    /////////////
    public static boolean isZhadan(List<String> pai) {
        boolean bl = false;
        if (isMin510k(pai) || isFk510k(pai) || isMh510k(pai) || isHx510k(pai) || isHt510k(pai)
                || is2dawang(pai) || is2xiaowang(pai) || is1dw1xw(pai) || is5zha(pai) || is3wang(pai)
                || is4wang(pai) || is6zha(pai) || is7zha(pai) || is8zha(pai)) {
            bl = true;
        }
        return bl;
    }

    /**
     * 判断该组合在所有组合中是否已经存在，存在返回true，不存在返回false
     *
     * @param list
     * @param all
     * @return
     */
    public static boolean isHave(List<String> list, List<List<String>> all) {
        for (int i = 0; i < all.size(); i++) {
            List<String> a1 = all.get(i);
            if (a1.size() == list.size() && getListSum(a1) == getListSum(list)) {
                return true;
            }
        }
        return false;
    }

    public static int getListSum(List<String> list) {
        int sum = 0;
        for (int i = 0; i < list.size(); i++) {
            sum += getNumbers(list.get(i)) + getColor(list.get(i)) * 100;
        }
        return sum;
    }

    public static boolean isHave2(List<String> list, List<List<String>> all) {
        for (int i = 0; i < all.size(); i++) {
            List<String> a1 = all.get(i);
            if (a1.size() == list.size() && getListSum2(a1) == getListSum2(list)) {
                return true;
            }
        }
        return false;
    }

    public static int getListSum2(List<String> list) {
        int sum = 0;
        for (int i = 0; i < list.size(); i++) {
            sum += getNumbers(list.get(i));
        }
        return sum;
    }

    public static boolean isHave1(List<String> list, List<List<String>> all) {
//		for (int i = 0; i < all.size(); i++) {
//			List<String> a1 = all.get(i);
//			if(a1.size()==list.size() && getListSum1(a1) == getListSum1(list)) {
//				return true;
//			}
//		}
//		return false;
        return false;
    }

    public static int getListSum1(List<String> list) {
        int sum = 0;
        for (int i = 0; i < list.size(); i++) {
            sum += getNumbers(list.get(i));
        }
        return sum;
    }

    public static void combinationDz(String[] ia, int n, List<List<String>> list, int dpnum) {
        combinationDz("", ia, n, list, dpnum);
    }

    public static void combinationDz(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if (isDuizi(pai)) {
                    List<String> list1 = new ArrayList<String>();
                    for (int j = 0; j < dpnum; j++) {
                        list1.add(iary[j]);
                    }
                    if (!isHave(list1, list)) {
                        list.add(list1);
                    }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationDz(ss, ii, n - 1, list, dpnum);
            }
        }
    }

    public static void combination3zhang(String[] ia, int n, List<List<String>> list, int dpnum) {
        combination3zhang("", ia, n, list, dpnum);
    }

    public static void combination3zhang(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if (is3zhang(pai)) {
                    List<String> list1 = new ArrayList<String>();
                    for (int j = 0; j < dpnum; j++) {
                        list1.add(iary[j]);
                    }
                    if (!isHave(list1, list)) {
                        list.add(list1);
                    }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination3zhang(ss, ii, n - 1, list, dpnum);
            }
        }
    }

    public static void combination4zhang(String[] ia, int n, List<List<String>> list, int dpnum) {
        combination4zhang("", ia, n, list, dpnum);
    }

    public static void combination4zhang(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if (is4zhang(pai)) {
                    List<String> list1 = new ArrayList<String>();
                    for (int j = 0; j < dpnum; j++) {
                        list1.add(iary[j]);
                    }
                    if (!isHave(list1, list)) {
                        list.add(list1);
                    }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination4zhang(ss, ii, n - 1, list, dpnum);
            }
        }
    }

    public static void combinationLiandui(String[] ia, int n, List<List<String>> list, int dpnum) {
        combinationLiandui("", ia, n, list, dpnum);
    }

    public static void combinationLiandui(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if (isLiandui(pai)) {
                    List<String> list1 = new ArrayList<String>();
                    for (int j = 0; j < dpnum; j++) {
                        list1.add(iary[j]);
                    }
                    if (!isHave(list1, list)) {
                        list.add(list1);
                    }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationLiandui(ss, ii, n - 1, list, dpnum);
            }
        }
    }

    public static void combinationLian3zhang(String[] ia, int n, List<List<String>> list, int dpnum) {
        combinationLian3zhang("", ia, n, list, dpnum);
    }

    public static void combinationLian3zhang(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if (isLian3zhang(pai)) {
                    List<String> list1 = new ArrayList<String>();
                    for (int j = 0; j < dpnum; j++) {
                        list1.add(iary[j]);
                    }
                    if (!isHave(list1, list)) {
                        list.add(list1);
                    }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationLian3zhang(ss, ii, n - 1, list, dpnum);
            }
        }
    }

    public static void combinationLian4zhang(String[] ia, int n, List<List<String>> list, int dpnum) {
        combinationLian4zhang("", ia, n, list, dpnum);
    }

    public static void combinationLian4zhang(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if (isLian4zhang(pai)) {
                    List<String> list1 = new ArrayList<String>();
                    for (int j = 0; j < dpnum; j++) {
                        list1.add(iary[j]);
                    }
                    if (!isHave(list1, list)) {
                        list.add(list1);
                    }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationLian4zhang(ss, ii, n - 1, list, dpnum);
            }
        }
    }

    public static void combinationMin50k(String[] ia, int n, List<List<String>> list, int dpnum) {
        combinationMin50k("", ia, n, list, dpnum);
    }

    public static void combinationMin50k(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if (isMin510k(pai)) {
                    List<String> list1 = new ArrayList<String>();
                    for (int j = 0; j < dpnum; j++) {
                        list1.add(iary[j]);
                    }
                    if (!isHave(list1, list)) {
                        list.add(list1);
                    }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationMin50k(ss, ii, n - 1, list, dpnum);
            }
        }
    }

    public static void combination50k(String[] ia, int n, List<List<String>> list, int dpnum) {
        combination50k("", ia, n, list, dpnum);
    }

    public static void combination50k(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if (is510k(pai)) {
                    List<String> list1 = new ArrayList<String>();
                    for (int j = 0; j < dpnum; j++) {
                        list1.add(iary[j]);
                    }
                    if (!isHave(list1, list)) {
                        list.add(list1);
                    }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination50k(ss, ii, n - 1, list, dpnum);
            }
        }
    }

    public static void combinationFk50k(String[] ia, int n, List<List<String>> list, int dpnum) {
        combinationFk50k("", ia, n, list, dpnum);
    }

    public static void combinationFk50k(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if (isFk510k(pai)) {
                    List<String> list1 = new ArrayList<String>();
                    for (int j = 0; j < dpnum; j++) {
                        list1.add(iary[j]);
                    }
                    if (!isHave(list1, list)) {
                        list.add(list1);
                    }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationFk50k(ss, ii, n - 1, list, dpnum);
            }
        }
    }

    public static void combinationMh50k(String[] ia, int n, List<List<String>> list, int dpnum) {
        combinationMh50k("", ia, n, list, dpnum);
    }

    public static void combinationMh50k(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if (isMh510k(pai)) {
                    List<String> list1 = new ArrayList<String>();
                    for (int j = 0; j < dpnum; j++) {
                        list1.add(iary[j]);
                    }
                    if (!isHave(list1, list)) {
                        list.add(list1);
                    }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationMh50k(ss, ii, n - 1, list, dpnum);
            }
        }
    }

    public static void combinationHx50k(String[] ia, int n, List<List<String>> list, int dpnum) {
        combinationHx50k("", ia, n, list, dpnum);
    }

    public static void combinationHx50k(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if (isHx510k(pai)) {
                    List<String> list1 = new ArrayList<String>();
                    for (int j = 0; j < dpnum; j++) {
                        list1.add(iary[j]);
                    }
                    if (!isHave(list1, list)) {
                        list.add(list1);
                    }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationHx50k(ss, ii, n - 1, list, dpnum);
            }
        }
    }

    public static void combinationHt50k(String[] ia, int n, List<List<String>> list, int dpnum) {
        combinationHt50k("", ia, n, list, dpnum);
    }

    public static void combinationHt50k(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if (isHt510k(pai)) {
                    List<String> list1 = new ArrayList<String>();
                    for (int j = 0; j < dpnum; j++) {
                        list1.add(iary[j]);
                    }
                    if (!isHave(list1, list)) {
                        list.add(list1);
                    }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationHt50k(ss, ii, n - 1, list, dpnum);
            }
        }
    }

    public static void combination3Wang(String[] ia, int n, List<List<String>> list, int dpnum) {
        combination3Wang("", ia, n, list, dpnum);
    }

    public static void combination3Wang(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if (is3wang(pai)) {
                    List<String> list1 = new ArrayList<String>();
                    for (int j = 0; j < dpnum; j++) {
                        list1.add(iary[j]);
                    }
                    if (!isHave(list1, list)) {
                        list.add(list1);
                    }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination3Wang(ss, ii, n - 1, list, dpnum);
            }
        }
    }


    public static void combination6zha(String[] ia, int n, List<List<String>> list, int dpnum) {
        combination6zha("", ia, n, list, dpnum);
    }

    public static void combination6zha(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if (is6zha(pai)) {
                    List<String> list1 = new ArrayList<String>();
                    for (int j = 0; j < dpnum; j++) {
                        list1.add(iary[j]);
                    }
                    if (!isHave(list1, list)) {
                        list.add(list1);
                    }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination6zha(ss, ii, n - 1, list, dpnum);
            }
        }
    }

    public static void combination7zha(String[] ia, int n, List<List<String>> list, int dpnum) {
        combination7zha("", ia, n, list, dpnum);
    }

    public static void combination7zha(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if (is7zha(pai)) {
                    List<String> list1 = new ArrayList<String>();
                    for (int j = 0; j < dpnum; j++) {
                        list1.add(iary[j]);
                    }
                    if (!isHave(list1, list)) {
                        list.add(list1);
                    }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination7zha(ss, ii, n - 1, list, dpnum);
            }
        }
    }

    public static void combination8zha(String[] ia, int n, List<List<String>> list, int dpnum) {
        combination8zha("", ia, n, list, dpnum);
    }

    public static void combination8zha(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if (is8zha(pai)) {
                    List<String> list1 = new ArrayList<String>();
                    for (int j = 0; j < dpnum; j++) {
                        list1.add(iary[j]);
                    }
                    if (!isHave(list1, list)) {
                        list.add(list1);
                    }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination8zha(ss, ii, n - 1, list, dpnum);
            }
        }
    }

    public static void combination5zha(String[] ia, int n, List<List<String>> list, int dpnum) {
        combination5zha("", ia, n, list, dpnum);
    }

    public static void combination5zha(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if (is5zha(pai)) {
                    List<String> list1 = new ArrayList<String>();
                    for (int j = 0; j < dpnum; j++) {
                        list1.add(iary[j]);
                    }
                    if (!isHave(list1, list)) {
                        list.add(list1);
                    }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination5zha(ss, ii, n - 1, list, dpnum);
            }
        }
    }


    public static void combinationl3z(List<List<String>> ia, int n, List<List<String>> list, int dpnum) {
        List<String> l = new ArrayList<String>();
        combinationl3z(l, ia, n, list, dpnum);
    }

    public static void combinationl3z(List<String> s, List<List<String>> ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.size(); i++) {
                List<String> totalStr = new ArrayList<String>();
                totalStr.addAll(s);
                totalStr.addAll(ia.get(i));
                String[] iary = new String[totalStr.size()];
                for (int j = 0; j < iary.length; j++) {
                    iary[j] = totalStr.get(j);
                }
                List<String> pai = Arrays.asList(iary);
                if (isLian3zhang(pai) && iary.length == dpnum * 3) {
                    List<String> list1 = new ArrayList<String>();
                    for (int j = 0; j < dpnum * 3; j++) {
                        list1.add(iary[j]);
                    }
                    list.add(list1);
                }
            }
        } else {
            for (int i = 0; i < ia.size() - (n - 1); i++) {
                List<String> ss = new ArrayList<String>();
                ss.addAll(s);
                ss.addAll(ia.get(i));
                List<List<String>> ii = new ArrayList<List<String>>();
                for (int j = 0; j < ia.size() - i - 1; j++) {
                    ii.add(ia.get(i + j + 1));
                }
                combinationl3z(ss, ii, n - 1, list, dpnum);
            }
        }
    }

    public static void combinationl4z(List<List<String>> ia, int n, List<List<String>> list, int dpnum) {
        List<String> l = new ArrayList<String>();
        combinationl4z(l, ia, n, list, dpnum);
    }

    public static void combinationl4z(List<String> s, List<List<String>> ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.size(); i++) {
                List<String> totalStr = new ArrayList<String>();
                totalStr.addAll(s);
                totalStr.addAll(ia.get(i));
                String[] iary = new String[totalStr.size()];
                for (int j = 0; j < iary.length; j++) {
                    iary[j] = totalStr.get(j);
                }
                List<String> pai = Arrays.asList(iary);
                if (isLian4zhang(pai) && iary.length == dpnum * 4) {
                    List<String> list1 = new ArrayList<String>();
                    for (int j = 0; j < dpnum * 4; j++) {
                        list1.add(iary[j]);
                    }
                    list.add(list1);
                }
            }
        } else {
            for (int i = 0; i < ia.size() - (n - 1); i++) {
                List<String> ss = new ArrayList<String>();
                ss.addAll(s);
                ss.addAll(ia.get(i));
                List<List<String>> ii = new ArrayList<List<String>>();
                for (int j = 0; j < ia.size() - i - 1; j++) {
                    ii.add(ia.get(i + j + 1));
                }
                combinationl4z(ss, ii, n - 1, list, dpnum);
            }
        }
    }

    public static Map<String, Object> isHaveXiqian(List<String> pai) {
        Map<String, Object> map = new HashMap<String, Object>();
        List<String> sjp = new ArrayList<String>();
        List<List<String>> _8list = getTs8zha(sjp, pai);
        List<List<String>> _7list = getTs7zha(sjp, pai);
        List<List<String>> _6list = getTs6zha(sjp, pai);
        List<List<String>> _4wlist = getTs4Wang(sjp, pai);
        List<List<String>> all = new ArrayList<List<String>>();
        all.addAll(_8list);
        all.addAll(_4wlist);
        for (List<String> list : _7list) {
            int pnum = getNumbers(list.get(0));
            if (!isHaveInLists(pnum, _8list) && !isHaveInLists(pnum, all)) {
                all.add(list);
            }
        }
        for (List<String> list : _6list) {
            int pnum = getNumbers(list.get(0));
            if (!isHaveInLists(pnum, _7list) && !isHaveInLists(pnum, all)) {
                all.add(list);
            }
        }

        if (_8list.size() + _7list.size() + _6list.size() + _4wlist.size() > 0) {
            map.put("have", "1");
            map.put("pai", all);
        } else {
            map.put("have", "0");
            map.put("pai", all);
        }

        return map;
    }

    public static boolean isHaveInLists(int pnum, List<List<String>> pai) {
        boolean bl = false;
        for (List<String> list : pai) {
            int plnum = getNumbers(list.get(0));
            if (pnum == plnum) {
                bl = true;
                break;
            }
        }
        return bl;
    }

    /**
     * 排序
     *
     * @param pai2
     * @return
     */
    public static List<String> sortPai(List<String> pai2, String type) {
        List<String> pai = new ArrayList<>(pai2);
        List<String> slist = new ArrayList<String>();
        List<String> sjp = new ArrayList<String>();
        sjp.add("B0");
        //pai = sortList(pai, "desc");
        //获取所有炸弹，往左边排，从大到小获取一个remove一个
        List<List<String>> _8zhalist = getTs8zha(sjp, pai);
        for (int i = 0; i < _8zhalist.size(); i++) {
            List<String> l = _8zhalist.get(i);
            //pai.removeAll(l);
            pai = myRemoveAll(pai, l);
            l = sortList(l, "desc");
            slist.addAll(l);
        }
        List<List<String>> _7zhalist = getTs7zha(sjp, pai);
        for (int i = 0; i < _7zhalist.size(); i++) {
            List<String> l = _7zhalist.get(i);
            //pai.removeAll(l);
            pai = myRemoveAll(pai, l);
            l = sortList(l, "desc");
            slist.addAll(l);
        }
        List<List<String>> _4wlist = getTs4Wang(sjp, pai);
        for (int i = 0; i < _4wlist.size(); i++) {
            List<String> l = _4wlist.get(i);
            //pai.removeAll(l);
            pai = myRemoveAll(pai, l);
            l = sortList(l, "desc");
            slist.addAll(l);
        }
        List<List<String>> _6zhalist = getTs6zha(sjp, pai);
        for (int i = 0; i < _6zhalist.size(); i++) {
            List<String> l = _6zhalist.get(i);
            //pai.removeAll(l);
            pai = myRemoveAll(pai, l);
            l = sortList(l, "desc");
            slist.addAll(l);
        }
        List<List<String>> _3wlist = getTs3Wang(sjp, pai);
        for (int i = 0; i < _3wlist.size(); i++) {
            List<String> l = _3wlist.get(i);
            //pai.removeAll(l);
            pai = myRemoveAll(pai, l);
            l = sortList(l, "desc");
            slist.addAll(l);
        }
        List<List<String>> _5zhalist = getTs5zha(sjp, pai);
        for (int i = 0; i < _5zhalist.size(); i++) {
            List<String> l = _5zhalist.get(i);
            //pai.removeAll(l);
            pai = myRemoveAll(pai, l);
            l = sortList(l, "desc");
            slist.addAll(l);
        }
        List<List<String>> _2Wlist = getTs2DaWang(sjp, pai);
        for (int i = 0; i < _2Wlist.size(); i++) {
            List<String> l = _2Wlist.get(i);
            //pai.removeAll(l);
            pai = myRemoveAll(pai, l);
            slist.addAll(l);
        }
        List<List<String>> _1dw1xwlist = getTs1dw1xw(sjp, pai);
        for (int i = 0; i < _1dw1xwlist.size(); i++) {
            List<String> l = _1dw1xwlist.get(i);
            //pai.removeAll(l);
            pai = myRemoveAll(pai, l);
            l = sortList(l, "desc");
            slist.addAll(l);
        }
        List<List<String>> _2wlist = getTs2XiaoWang(sjp, pai);
        for (int i = 0; i < _2wlist.size(); i++) {
            List<String> l = _2wlist.get(i);
            //pai.removeAll(l);
            pai = myRemoveAll(pai, l);
            slist.addAll(l);
        }
//    	List<List<String>> _ht50k = getTsHt50k(sjp, pai);
//    	for (int i = 0; i < _ht50k.size(); i++) {
//    		List<String> l = _ht50k.get(i);
//    		//pai.removeAll(l);
//    		pai = myRemoveAll(pai, l);
//    		l = sortList(l, "desc");
//			slist.addAll(l);
//			_ht50k = getTsHt50k(sjp, pai);
//		}
//    	List<List<String>> _hx50k = getTsHx50k(sjp, pai);
//    	for (int i = 0; i < _hx50k.size(); i++) {
//    		List<String> l = _hx50k.get(i);
//    		//pai.removeAll(l);
//    		pai = myRemoveAll(pai, l);
//    		l = sortList(l, "desc");
//			slist.addAll(l);
//			_hx50k = getTsHx50k(sjp, pai);
//		}
//    	List<List<String>> _mh50k = getTsMh50k(sjp, pai);
//    	for (int i = 0; i < _mh50k.size(); i++) {
//    		List<String> l = _mh50k.get(i);
//    		//pai.removeAll(l);
//    		pai = myRemoveAll(pai, l);
//    		l = sortList(l, "desc");
//			slist.addAll(l);
//			_ht50k = getTsHt50k(sjp, pai);
//		}
        List<List<String>> _ht50k = getTsHt50k(sjp, pai);
        List<String> _ht50kslist = new ArrayList<String>();
        while (_ht50k.size() > 0) {
            List<String> l = _ht50k.get(0);
            pai = myRemoveAll(pai, l);
            l = sortList(l, "desc");
            _ht50kslist.addAll(l);
            _ht50k = getTsHt50k(sjp, pai);
        }
        slist.addAll(_ht50kslist);

        List<List<String>> _hx50k = getTsHx50k(sjp, pai);
        List<String> _hx50kslist = new ArrayList<String>();
        while (_hx50k.size() > 0) {
            List<String> l = _hx50k.get(0);
            pai = myRemoveAll(pai, l);
            l = sortList(l, "desc");
            _hx50kslist.addAll(l);
            _hx50k = getTsHx50k(sjp, pai);
        }
        slist.addAll(_hx50kslist);

        List<List<String>> _mh50k = getTsMh50k(sjp, pai);
        List<String> _mh50kslist = new ArrayList<String>();
        while (_mh50k.size() > 0) {
            List<String> l = _mh50k.get(0);
            pai = myRemoveAll(pai, l);
            l = sortList(l, "desc");
            _mh50kslist.addAll(l);
            _mh50k = getTsMh50k(sjp, pai);
        }
        slist.addAll(_mh50kslist);

//    	List<List<String>> _fk50k = getTsFk50k(sjp, pai);
//    	for (int i = 0; i < _fk50k.size(); i++) {
//    		List<String> l = _fk50k.get(i);
//    		//pai.removeAll(l);
//    		pai = myRemoveAll(pai, l);
//    		l = sortList(l, "desc");
//			slist.addAll(l);
//			_fk50k = getTsFk50k(sjp, pai);
//		}

        List<List<String>> _fk50k = getTsFk50k(sjp, pai);
        List<String> _fk50kslist = new ArrayList<String>();
        while (_fk50k.size() > 0) {
            List<String> l = _fk50k.get(0);
            pai = myRemoveAll(pai, l);
            l = sortList(l, "desc");
            _fk50kslist.addAll(l);
            _fk50k = getTsFk50k(sjp, pai);
        }
        slist.addAll(_fk50kslist);
//    	List<List<String>> _min50k = getTsMin50k(sjp, pai);
//    	List<String> _min50kslist = new ArrayList<String>();
//    	for (int i = 0; i < _min50k.size(); i++) {
//    		List<String> l = _min50k.get(i);
//    		//pai.removeAll(l);
//    		pai = myRemoveAll(pai, l);
//    		l = sortList(l, "desc");
//    		_min50kslist.addAll(l);
//			_min50k = getTsMin50k(sjp, pai);
//		}
//    	List<String> _min50kslist1 = new ArrayList<String>();
//    	_min50kslist1.addAll(_min50kslist);
//    	//如果该副50K已经在正套里了，则删除
//    	for(int i=0; i<_min50kslist1.size(); i++) {
//    		String p = _min50kslist1.get(i);
//    		if(_ht50k.contains(p) || _hx50k.contains(p) || _mh50k.contains(p) || _fk50k.contains(p)) {
//    			_min50kslist.remove(i);
//    		}
//    	}
//    	slist.addAll(_min50kslist);


//    	List<List<String>> _min50k = getTsMin50k(sjp, pai);
//    	List<String> _min50kslist = new ArrayList<String>();
//    	for (int i = 0; i < _min50k.size(); i++) {
//    		List<String> l = _min50k.get(i);
//    		pai = myRemoveAll(pai, l);
//    		l = sortList(l, "desc");
//    		_min50kslist.addAll(l);
//			_min50k = getTsMin50k(sjp, pai);
//			i=0;
//		}
//    	slist.addAll(_min50kslist);

        List<List<String>> _min50k = getTsMin50k(sjp, pai);
        List<String> _min50kslist = new ArrayList<String>();
        while (_min50k.size() > 0) {
            List<String> l = _min50k.get(0);
            pai = myRemoveAll(pai, l);
            l = sortList(l, "desc");
            _min50kslist.addAll(l);
            _min50k = getTsMin50k(sjp, pai);
        }
        slist.addAll(_min50kslist);


        if (type.equals("1")) {
            pai = sortList(pai, "desc");
            slist.addAll(pai);
        } else {
            List<List<String>> _4zhang = getTs4zhang(sjp, pai);
            List<String> _4slist = new ArrayList<String>();
            while (_4zhang.size() > 0) {
                List<String> l = _4zhang.get(0);
                pai = myRemoveAll(pai, l);
                l = sortList(l, "desc");
                _4slist.addAll(l);
                _4zhang = getTs4zhang(sjp, pai);
            }
            _4slist = sortList(_4slist, "desc");
            slist.addAll(_4slist);

            List<List<String>> _3zhang = getTs3zhang(sjp, pai);
            List<String> _3slist = new ArrayList<String>();
            while (_3zhang.size() > 0) {
                List<String> l = _3zhang.get(0);
                pai = myRemoveAll(pai, l);
                l = sortList(l, "desc");
                _3slist.addAll(l);
                _3zhang = getTs3zhang(sjp, pai);
            }
            _3slist = sortList(_3slist, "desc");
            slist.addAll(_3slist);

            List<List<String>> _duizi = getTsDuizi(sjp, pai);
            List<String> _dzslist = new ArrayList<String>();
            while (_duizi.size() > 0) {
                List<String> l = _duizi.get(0);
                pai = myRemoveAll(pai, l);
                l = sortList(l, "desc");
                _dzslist.addAll(l);
                _duizi = getTsDuizi(sjp, pai);
            }
            _dzslist = sortList(_dzslist, "desc");
            slist.addAll(_dzslist);

            pai = sortList(pai, "desc");
            slist.addAll(pai);
        }
        return slist;
    }

    public static List<String> myRemoveAll(List<String> pai, List<String> l) {
        for (String p : l) {
            if (pai.contains(p)) {
                pai.remove(p);
            }
        }
        return pai;
    }

    public static boolean isXiaochaiDa(List<String> zha, List<List<String>> zhalist) {
        boolean bl = false;
        int num = getNumbers(zha.get(0));
        for (List<String> list : zhalist) {
            int num1 = getNumbers(list.get(0));
            if (num == num1) {
                bl = true;
            }
        }
        return bl;
    }

    public static boolean is50kFuchaiZheng(List<String> fu, List<List<String>> zheng, List<String> pai) {
        boolean bl = false;
        for (String p : fu) {
            for (List<String> list : zheng) {
                if (getStringInListCount(p, pai) == 1 && list.contains(p)) {
                    bl = true;
                    break;
                }
            }
        }
        return bl;
    }

    public static int getStringInListCount(String p, List<String> pai) {
        int num = 0;
        for (String pp : pai) {
            if (p.equals(pp)) {
                num++;
            }
        }
        return num;
    }

    //////new///////
    public static List<List<String>> getAllPaiXing(List<String> sjp, List<String> zjp) {
        List<List<String>> all = new ArrayList<List<String>>();
        List<List<String>> houlist = new ArrayList<List<String>>();
        sjp = sortList(sjp, "asc");
        String sjtype = getCpType(sjp);
        zjp = sortList(zjp, "asc");
        //获取所有单张
        List<List<String>> _danzhanglist = new ArrayList<List<String>>();
        _danzhanglist = getTsDanzhang(sjp, zjp);
        List<List<String>> boomList = getTsZhaDan(sjp, zjp, sjtype);
        int[] ary = paiToShortAry(zjp);
        for (List<String> pai : _danzhanglist) {
            for (String p : pai) {
                int index = getNumbers(p) - 3;
                if (ary[index] > 1) {
                    //houlist.add(0, pai);
                    houlist.add(pai);
                } else {
                    all.add(pai);
                }
            }
        }
        all.addAll(houlist);
        all.addAll(boomList);
        //
        return all;
    }

    public static List<List<String>> getTsDanZhangNew(List<String> sjp, List<String> zjp) {
        long d1 = System.currentTimeMillis();
        List<List<String>> all = new ArrayList<List<String>>();
        sjp = sortList(sjp, "asc");
        zjp = sortList(zjp, "asc");
        //获取所有的8炸
        List<String> sjp1 = new ArrayList<String>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        sjp1.add("F3");
        List<List<String>> _8zhaList = getTs8zha(sjp1, zjp1);
        //将8炸里出现的牌remove掉
        for (List<String> list : _8zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _7zhaList = getTs7zha(sjp1, zjp1);
        for (List<String> list : _7zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _4wList = getTs4Wang(sjp1, zjp1);
        for (List<String> list : _4wList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _6zhaList = getTs6zha(sjp1, zjp1);
        for (List<String> list : _6zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _3wList = getTs3Wang(sjp1, zjp1);
        for (List<String> list : _3wList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _5zhaList = getTs5zha(sjp1, zjp1);
        for (List<String> list : _5zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
//		List<List<String>> _2dwList = getTs2DaWang(sjp1, zjp1);
//		for(List<String> list : _2dwList) {
//			for(String pai : list) {
//				zjp1.remove(pai);
//			}
//		}
//		List<List<String>> _1dw1xwList = getTs1dw1xw(sjp1, zjp1);
//		for(List<String> list : _1dw1xwList) {
//			for(String pai : list) {
//				zjp1.remove(pai);
//			}
//		}
//		List<List<String>> _2xwList = getTs2XiaoWang(sjp1, zjp1);
//		for(List<String> list : _2xwList) {
//			for(String pai : list) {
//				zjp1.remove(pai);
//			}
//		}
        List<List<String>> _ht50kList = getTsHt50k(sjp1, zjp1);
        for (List<String> list : _ht50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _hx50kList = getTsHx50k(sjp1, zjp1);
        for (List<String> list : _hx50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _mh50kList = getTsMh50k(sjp1, zjp1);
        for (List<String> list : _mh50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _fk50kList = getTsFk50k(sjp1, zjp1);
        for (List<String> list : _fk50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _min50kList = getTsMin50k(sjp1, zjp1);
        if (_min50kList.size() > 1) {
            List<String> no1 = _min50kList.get(0);
            _min50kList = new ArrayList<List<String>>();
            _min50kList.add(no1);
        }
        for (List<String> list : _min50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        sjp1 = new ArrayList<String>();
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        List<List<String>> _4zhangList = getTs4zhang(sjp1, zjp1);
        for (List<String> list : _4zhangList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        sjp1 = new ArrayList<String>();
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        List<List<String>> _3zhangList = getTs3zhang(sjp1, zjp1);
        for (List<String> list : _3zhangList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        sjp1 = new ArrayList<String>();
        sjp1.add("F3");
        sjp1.add("F3");
        List<List<String>> _duiziList = getTsDuizi(sjp1, zjp1);
        for (List<String> list : _duiziList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        sjp1 = new ArrayList<String>();
        sjp1.add("F3");
        List<List<String>> _danzhangList = getTsDanzhang1(sjp, zjp1);
        for (List<String> list : _danzhangList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        all.addAll(_danzhangList);
        all.addAll(_min50kList);
        all.addAll(_fk50kList);
        all.addAll(_mh50kList);
        all.addAll(_hx50kList);
        all.addAll(_ht50kList);
//		all.addAll(_1dw1xwList);
//		all.addAll(_2xwList);
//		all.addAll(_2dwList);
        all.addAll(_5zhaList);
//		all.addAll(_3wList);
        all.addAll(_6zhaList);
        all.addAll(_4wList);
        all.addAll(_7zhaList);
        all.addAll(_8zhaList);

        //每个组合里获取一个单张
        List<List<String>> danzhanglist = getTsDanzhang1(sjp, zjp);
        List<List<String>> danzhanglist1 = new ArrayList<List<String>>();
        danzhanglist1.addAll(danzhanglist);
        for (List<String> list : danzhanglist) {
            for (String pai : list) {
                for (List<String> _list : _danzhangList) {
                    for (String _pai : _list) {
                        if (getNumbers(_pai) == getNumbers(pai)) {
                            danzhanglist1.remove(list);
                        }
                    }
                }
            }
        }
        all.addAll(danzhanglist1);
        long d2 = System.currentTimeMillis();
//        //System.out.println("总耗时：" + (d2 - d1));
        return all;
    }

    public static List<List<String>> getTsDuiZiNew(List<String> sjp, List<String> zjp) {
        long d1 = System.currentTimeMillis();
        List<List<String>> all = new ArrayList<List<String>>();
        sjp = sortList(sjp, "asc");
        zjp = sortList(zjp, "asc");
        //获取所有的8炸
        List<String> sjp1 = new ArrayList<String>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        sjp1.add("F3");
        sjp1.add("F3");
        List<List<String>> _8zhaList = getTs8zha(sjp1, zjp1);
        //将8炸里出现的牌remove掉
        for (List<String> list : _8zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _7zhaList = getTs7zha(sjp1, zjp1);
        for (List<String> list : _7zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _4wList = getTs4Wang(sjp1, zjp1);
        for (List<String> list : _4wList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _6zhaList = getTs6zha(sjp1, zjp1);
        for (List<String> list : _6zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _3wList = getTs3Wang(sjp1, zjp1);
        for (List<String> list : _3wList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _5zhaList = getTs5zha(sjp1, zjp1);
        for (List<String> list : _5zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _2dwList = getTs2DaWang(sjp1, zjp1);
        for (List<String> list : _2dwList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
//		List<List<String>> _1dw1xwList = getTs1dw1xw(sjp1, zjp1);
//		for(List<String> list : _1dw1xwList) {
//			for(String pai : list) {
//				zjp1.remove(pai);
//			}
//		}
        List<List<String>> _2xwList = getTs2XiaoWang(sjp1, zjp1);
        for (List<String> list : _2xwList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _ht50kList = getTsHt50k(sjp1, zjp1);
        for (List<String> list : _ht50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _hx50kList = getTsHx50k(sjp1, zjp1);
        for (List<String> list : _hx50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _mh50kList = getTsMh50k(sjp1, zjp1);
        for (List<String> list : _mh50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _fk50kList = getTsFk50k(sjp1, zjp1);
        for (List<String> list : _fk50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _min50kList = getTsMin50k(sjp1, zjp1);
        if (_min50kList.size() > 1) {
            List<String> no1 = _min50kList.get(0);
            _min50kList = new ArrayList<List<String>>();
            _min50kList.add(no1);
        }
        for (List<String> list : _min50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }

        sjp1 = new ArrayList<String>();
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        List<List<String>> _4zhangList = getTs4zhang(sjp1, zjp1);
        for (List<String> list : _4zhangList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        sjp1 = new ArrayList<String>();
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        List<List<String>> _3zhangList = getTs3zhang(sjp1, zjp1);
        for (List<String> list : _3zhangList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        sjp1 = new ArrayList<String>();
        sjp1.add("F3");
        sjp1.add("F3");
        List<List<String>> _duiziList = getTsDuizi1(sjp, zjp1);
        for (List<String> list : _duiziList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        all.addAll(_duiziList);
        all.addAll(_min50kList);
        all.addAll(_fk50kList);
        all.addAll(_mh50kList);
        all.addAll(_hx50kList);
        all.addAll(_ht50kList);
//		all.addAll(_1dw1xwList);
//		all.addAll(_2xwList);
//		all.addAll(_2dwList);
        all.addAll(_5zhaList);
//		all.addAll(_3wList);
        all.addAll(_6zhaList);
        all.addAll(_4wList);
        all.addAll(_7zhaList);
        all.addAll(_8zhaList);

        List<List<String>> duiziList = getTsDuizi1(sjp, zjp);
        List<List<String>> duiziList1 = new ArrayList<List<String>>();
        duiziList1.addAll(duiziList);
        for (List<String> list : duiziList) {
            for (String pai : list) {
                for (List<String> _list : _duiziList) {
                    for (String _pai : _list) {
                        if (getNumbers(_pai) == getNumbers(pai)) {
                            duiziList1.remove(list);
                        }
                    }
                }
            }
        }
        all.addAll(duiziList1);
        long d2 = System.currentTimeMillis();
//        //System.out.println("总耗时：" + (d2 - d1));
        return all;
    }

    public static List<List<String>> getTs3zhangNew(List<String> sjp, List<String> zjp) {
        long d1 = System.currentTimeMillis();
        List<List<String>> all = new ArrayList<List<String>>();
        sjp = sortList(sjp, "asc");
        zjp = sortList(zjp, "asc");
        //获取所有的8炸
        List<String> sjp1 = new ArrayList<String>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        List<List<String>> _8zhaList = getTs8zha(sjp1, zjp1);
        //将8炸里出现的牌remove掉
        for (List<String> list : _8zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _7zhaList = getTs7zha(sjp1, zjp1);
        for (List<String> list : _7zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _4wList = getTs4Wang(sjp1, zjp1);
        for (List<String> list : _4wList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _6zhaList = getTs6zha(sjp1, zjp1);
        for (List<String> list : _6zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _3wList = getTs3Wang(sjp1, zjp1);
        for (List<String> list : _3wList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _5zhaList = getTs5zha(sjp1, zjp1);
        for (List<String> list : _5zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
//		List<List<String>> _2dwList = getTs2DaWang(sjp1, zjp1);
//		for(List<String> list : _2dwList) {
//			for(String pai : list) {
//				zjp1.remove(pai);
//			}
//		}
//		List<List<String>> _1dw1xwList = getTs1dw1xw(sjp1, zjp1);
//		for(List<String> list : _1dw1xwList) {
//			for(String pai : list) {
//				zjp1.remove(pai);
//			}
//		}
//		List<List<String>> _2xwList = getTs2XiaoWang(sjp1, zjp1);
//		for(List<String> list : _2xwList) {
//			for(String pai : list) {
//				zjp1.remove(pai);
//			}
//		}
        List<List<String>> _ht50kList = getTsHt50k(sjp1, zjp1);
        for (List<String> list : _ht50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _hx50kList = getTsHx50k(sjp1, zjp1);
        for (List<String> list : _hx50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _mh50kList = getTsMh50k(sjp1, zjp1);
        for (List<String> list : _mh50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _fk50kList = getTsFk50k(sjp1, zjp1);
        for (List<String> list : _fk50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _min50kList = getTsMin50k(sjp1, zjp1);
        if (_min50kList.size() > 1) {
            List<String> no1 = _min50kList.get(0);
            _min50kList = new ArrayList<List<String>>();
            _min50kList.add(no1);
        }
        for (List<String> list : _min50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }

        sjp1 = new ArrayList<String>();
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        List<List<String>> _4zhangList = getTs4zhang(sjp1, zjp1);
        for (List<String> list : _4zhangList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        sjp1 = new ArrayList<String>();
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        List<List<String>> _3zhangList = getTs3zhang1(sjp, zjp1);
        for (List<String> list : _3zhangList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        all.addAll(_3zhangList);
        all.addAll(_min50kList);
        all.addAll(_fk50kList);
        all.addAll(_mh50kList);
        all.addAll(_hx50kList);
        all.addAll(_ht50kList);
//		all.addAll(_1dw1xwList);
//		all.addAll(_2xwList);
//		all.addAll(_2dwList);
        all.addAll(_5zhaList);
//		all.addAll(_3wList);
        all.addAll(_6zhaList);
        all.addAll(_4wList);
        all.addAll(_7zhaList);
        all.addAll(_8zhaList);

        List<List<String>> __3zhangList = getTs3zhang1(sjp, zjp);
        List<List<String>> __3zhangList1 = new ArrayList<List<String>>();
        __3zhangList1.addAll(__3zhangList);
        for (List<String> list : __3zhangList) {
            for (String pai : list) {
                for (List<String> _list : _3zhangList) {
                    for (String _pai : _list) {
                        if (getNumbers(_pai) == getNumbers(pai)) {
                            __3zhangList1.remove(list);
                        }
                    }
                }
            }
        }
        all.addAll(__3zhangList1);
        long d2 = System.currentTimeMillis();
//        //System.out.println("总耗时：" + (d2 - d1));
        return all;
    }

    public static List<List<String>> getTs4zha(List<String> sjp, List<String> zjp,String cp_type) {

        List<List<String>> all = new ArrayList<List<String>>();
        List<String> zjp1 = new ArrayList<String>(zjp);
        List<String> zjp2 = new ArrayList<String>(zjp);
        String type =cp_type;
        if ("danz".equals(type) || "duiz".equals(type) || "ld".equals(type) || "sandaidui".equals(type)
                || "3z".equals(type) || "l3z".equals(type) || "feiji".equals(type) || "shunzi".equals(type)) {
            int[] a = paiToIntAry(paiToStringArr(zjp1));
            for (int i = 0; i < a.length; i++) {
                int a1 = a[i];
                if (a1 >= 4) {
                    int t = i + 3;
                    List<String> ts1 = new ArrayList<>();
					 for (String p2 : zjp2) {
							if (getNumbers(p2) == t) {
								ts1.add(p2);
							}
							if (ts1.size() == 4) {
								all.add(ts1);
								break;
							}
						}
                    all.add(ts1);
                }
            }
        } else if (is4zha(sjp)) {
            int[] a = paiToIntAry(paiToStringArr(zjp2));
            int sjpnum = getNumbers(sjp.get(0));
            for (int i = 0; i < a.length; i++) {
                int a1 = a[i];
                if (a1 >= 4) {
                    int t = i + 3;
                    if (t > sjpnum) {
                        List<String> ts1 = new ArrayList<>();
                        for (String p2 : zjp2) {
                            if (getNumbers(p2) == t) {
                                ts1.add(p2);
                            }
                            if (ts1.size() == 4) {
                                all.add(ts1);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return all;
        //return list;

    }

    public static List<List<String>> getTs4zhangNew(List<String> sjp, List<String> zjp) {
        long d1 = System.currentTimeMillis();
        List<List<String>> all = new ArrayList<List<String>>();
        sjp = sortList(sjp, "asc");
        zjp = sortList(zjp, "asc");
        //获取所有的8炸
        List<String> sjp1 = new ArrayList<String>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        List<List<String>> _8zhaList = getTs8zha(sjp1, zjp1);
        //将8炸里出现的牌remove掉
        for (List<String> list : _8zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _7zhaList = getTs7zha(sjp1, zjp1);
        for (List<String> list : _7zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _4wList = getTs4Wang(sjp1, zjp1);
        for (List<String> list : _4wList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _6zhaList = getTs6zha(sjp1, zjp1);
        for (List<String> list : _6zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _3wList = getTs3Wang(sjp1, zjp1);
        for (List<String> list : _3wList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _5zhaList = getTs5zha(sjp1, zjp1);
        for (List<String> list : _5zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
//		List<List<String>> _2dwList = getTs2DaWang(sjp1, zjp1);
//		for(List<String> list : _2dwList) {
//			for(String pai : list) {
//				zjp1.remove(pai);
//			}
//		}
//		List<List<String>> _1dw1xwList = getTs1dw1xw(sjp1, zjp1);
//		for(List<String> list : _1dw1xwList) {
//			for(String pai : list) {
//				zjp1.remove(pai);
//			}
//		}
//		List<List<String>> _2xwList = getTs2XiaoWang(sjp1, zjp1);
//		for(List<String> list : _2xwList) {
//			for(String pai : list) {
//				zjp1.remove(pai);
//			}
//		}
        List<List<String>> _ht50kList = getTsHt50k(sjp1, zjp1);
        for (List<String> list : _ht50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _hx50kList = getTsHx50k(sjp1, zjp1);
        for (List<String> list : _hx50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _mh50kList = getTsMh50k(sjp1, zjp1);
        for (List<String> list : _mh50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _fk50kList = getTsFk50k(sjp1, zjp1);
        for (List<String> list : _fk50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _min50kList = getTsMin50k(sjp1, zjp1);
        if (_min50kList.size() > 1) {
            List<String> no1 = _min50kList.get(0);
            _min50kList = new ArrayList<List<String>>();
            _min50kList.add(no1);
        }
        for (List<String> list : _min50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        sjp1 = new ArrayList<String>();
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        List<List<String>> _4zhangList = getTs4zhang1(sjp, zjp1);
        for (List<String> list : _4zhangList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        all.addAll(_4zhangList);
        all.addAll(_min50kList);
        all.addAll(_fk50kList);
        all.addAll(_mh50kList);
        all.addAll(_hx50kList);
        all.addAll(_ht50kList);
//		all.addAll(_1dw1xwList);
//		all.addAll(_2xwList);
//		all.addAll(_2dwList);
        all.addAll(_5zhaList);
//		all.addAll(_3wList);
        all.addAll(_6zhaList);
        all.addAll(_4wList);
        all.addAll(_7zhaList);
        all.addAll(_8zhaList);

        List<List<String>> __4zhangList = getTs4zhang1(sjp, zjp);
        List<List<String>> __4zhangList1 = new ArrayList<List<String>>();
        __4zhangList1.addAll(__4zhangList);
        for (List<String> list : __4zhangList) {
            for (String pai : list) {
                for (List<String> _list : _4zhangList) {
                    for (String _pai : _list) {
                        if (getNumbers(_pai) == getNumbers(pai)) {
                            __4zhangList1.remove(list);
                        }
                    }
                }
            }
        }
        all.addAll(__4zhangList1);
        long d2 = System.currentTimeMillis();
//        //System.out.println("总耗时：" + (d2 - d1));
        return all;
    }

    public static List<List<String>> getTsLianDuiNew(List<String> sjp, List<String> zjp) {
        long d1 = System.currentTimeMillis();
        List<List<String>> all = new ArrayList<List<String>>();
        sjp = sortList(sjp, "asc");
        zjp = sortList(zjp, "asc");
        //获取所有的8炸
        List<String> sjp1 = new ArrayList<String>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F4");
        sjp1.add("F4");
        List<List<String>> _8zhaList = getTs8zha(sjp1, zjp1);
        //将8炸里出现的牌remove掉
        for (List<String> list : _8zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _7zhaList = getTs7zha(sjp1, zjp1);
        for (List<String> list : _7zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _4wList = getTs4Wang(sjp1, zjp1);
        for (List<String> list : _4wList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _6zhaList = getTs6zha(sjp1, zjp1);
        for (List<String> list : _6zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _3wList = getTs3Wang(sjp1, zjp1);
        for (List<String> list : _3wList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _5zhaList = getTs5zha(sjp1, zjp1);
        for (List<String> list : _5zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
//		List<List<String>> _2dwList = getTs2DaWang(sjp1, zjp1);
//		for(List<String> list : _2dwList) {
//			for(String pai : list) {
//				zjp1.remove(pai);
//			}
//		}
//		List<List<String>> _1dw1xwList = getTs1dw1xw(sjp1, zjp1);
//		for(List<String> list : _1dw1xwList) {
//			for(String pai : list) {
//				zjp1.remove(pai);
//			}
//		}
//		List<List<String>> _2xwList = getTs2XiaoWang(sjp1, zjp1);
//		for(List<String> list : _2xwList) {
//			for(String pai : list) {
//				zjp1.remove(pai);
//			}
//		}
        List<List<String>> _ht50kList = getTsHt50k(sjp1, zjp1);
        for (List<String> list : _ht50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _hx50kList = getTsHx50k(sjp1, zjp1);
        for (List<String> list : _hx50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _mh50kList = getTsMh50k(sjp1, zjp1);
        for (List<String> list : _mh50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _fk50kList = getTsFk50k(sjp1, zjp1);
        for (List<String> list : _fk50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _min50kList = getTsMin50k(sjp1, zjp1);
        if (_min50kList.size() > 1) {
            List<String> no1 = _min50kList.get(0);
            _min50kList = new ArrayList<List<String>>();
            _min50kList.add(no1);
        }
        for (List<String> list : _min50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        sjp1 = new ArrayList<String>();
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        List<List<String>> _4zhangList = getTs4zhang(sjp1, zjp1);
        for (List<String> list : _4zhangList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        sjp1 = new ArrayList<String>();
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        List<List<String>> _3zhangList = getTs3zhang(sjp1, zjp1);
        for (List<String> list : _3zhangList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        sjp1 = new ArrayList<String>();
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F4");
        sjp1.add("F4");
        List<List<String>> _lianduiList = getTsLiandui1(sjp, zjp1);
        for (List<String> list : _lianduiList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        all.addAll(_lianduiList);
        all.addAll(_min50kList);
        all.addAll(_fk50kList);
        all.addAll(_mh50kList);
        all.addAll(_hx50kList);
        all.addAll(_ht50kList);
//		all.addAll(_1dw1xwList);
//		all.addAll(_2xwList);
//		all.addAll(_2dwList);
        all.addAll(_5zhaList);
//		all.addAll(_3wList);
        all.addAll(_6zhaList);
        all.addAll(_4wList);
        all.addAll(_7zhaList);
        all.addAll(_8zhaList);

        List<List<String>> lianduiList = getTsLiandui1(sjp, zjp);
        List<List<String>> lianduiList1 = new ArrayList<List<String>>();
        lianduiList1.addAll(lianduiList);
        for (List<String> list : lianduiList) {
            if (isHave2(list, _lianduiList)) {
                lianduiList1.remove(list);
            }
        }
        all.addAll(lianduiList1);
        long d2 = System.currentTimeMillis();
//        //System.out.println("总耗时：" + (d2 - d1));
        return all;
    }

    public static List<List<String>> getTsLian3zhangNew(List<String> sjp, List<String> zjp) {
        long d1 = System.currentTimeMillis();
        List<List<String>> all = new ArrayList<List<String>>();
        sjp = sortList(sjp, "asc");
        zjp = sortList(zjp, "asc");
        //获取所有的8炸
        List<String> sjp1 = new ArrayList<String>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F4");
        sjp1.add("F4");
        sjp1.add("F4");
        List<List<String>> _8zhaList = getTs8zha(sjp1, zjp1);
        //将8炸里出现的牌remove掉
        for (List<String> list : _8zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _7zhaList = getTs7zha(sjp1, zjp1);
        for (List<String> list : _7zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _4wList = getTs4Wang(sjp1, zjp1);
        for (List<String> list : _4wList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _6zhaList = getTs6zha(sjp1, zjp1);
        for (List<String> list : _6zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _3wList = getTs3Wang(sjp1, zjp1);
        for (List<String> list : _3wList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _5zhaList = getTs5zha(sjp1, zjp1);
        for (List<String> list : _5zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
//		List<List<String>> _2dwList = getTs2DaWang(sjp1, zjp1);
//		for(List<String> list : _2dwList) {
//			for(String pai : list) {
//				zjp1.remove(pai);
//			}
//		}
//		List<List<String>> _1dw1xwList = getTs1dw1xw(sjp1, zjp1);
//		for(List<String> list : _1dw1xwList) {
//			for(String pai : list) {
//				zjp1.remove(pai);
//			}
//		}
//		List<List<String>> _2xwList = getTs2XiaoWang(sjp1, zjp1);
//		for(List<String> list : _2xwList) {
//			for(String pai : list) {
//				zjp1.remove(pai);
//			}
//		}
        List<List<String>> _ht50kList = getTsHt50k(sjp1, zjp1);
        for (List<String> list : _ht50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _hx50kList = getTsHx50k(sjp1, zjp1);
        for (List<String> list : _hx50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _mh50kList = getTsMh50k(sjp1, zjp1);
        for (List<String> list : _mh50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _fk50kList = getTsFk50k(sjp1, zjp1);
        for (List<String> list : _fk50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _min50kList = getTsMin50k(sjp1, zjp1);
        if (_min50kList.size() > 1) {
            List<String> no1 = _min50kList.get(0);
            _min50kList = new ArrayList<List<String>>();
            _min50kList.add(no1);
        }
        for (List<String> list : _min50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        sjp1 = new ArrayList<String>();
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        List<List<String>> _4zhangList = getTs4zhang(sjp1, zjp1);
        for (List<String> list : _4zhangList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        sjp1 = new ArrayList<String>();
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F4");
        sjp1.add("F4");
        sjp1.add("F4");
        List<List<String>> _lian3zhangList = getTsLian3zhang1(sjp, zjp1);
        for (List<String> list : _lian3zhangList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        all.addAll(_lian3zhangList);
        all.addAll(_min50kList);
        all.addAll(_fk50kList);
        all.addAll(_mh50kList);
        all.addAll(_hx50kList);
        all.addAll(_ht50kList);
//		all.addAll(_1dw1xwList);
//		all.addAll(_2xwList);
//		all.addAll(_2dwList);
        all.addAll(_5zhaList);
//		all.addAll(_3wList);
        all.addAll(_6zhaList);
        all.addAll(_4wList);
        all.addAll(_7zhaList);
        all.addAll(_8zhaList);

        List<List<String>> lian3zhangList = getTsLian3zhang1(sjp, zjp);
        List<List<String>> lian3zhangList1 = new ArrayList<List<String>>();
        lian3zhangList1.addAll(lian3zhangList);
        for (List<String> list : lian3zhangList) {
            if (isHave2(list, _lian3zhangList)) {
                lian3zhangList1.remove(list);
            }
        }
        all.addAll(lian3zhangList1);
        long d2 = System.currentTimeMillis();
//        //System.out.println("总耗时：" + (d2 - d1));
        return all;
    }

    public static List<List<String>> getTsLian4zhangNew(List<String> sjp, List<String> zjp) {
        long d1 = System.currentTimeMillis();
        List<List<String>> all = new ArrayList<List<String>>();
        sjp = sortList(sjp, "asc");
        zjp = sortList(zjp, "asc");
        //获取所有的8炸
        List<String> sjp1 = new ArrayList<String>();
        List<String> zjp1 = new ArrayList<String>();
        zjp1.addAll(zjp);
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F4");
        sjp1.add("F4");
        sjp1.add("F4");
        sjp1.add("F4");
        List<List<String>> _8zhaList = getTs8zha(sjp1, zjp1);
        //将8炸里出现的牌remove掉
        for (List<String> list : _8zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _7zhaList = getTs7zha(sjp1, zjp1);
        for (List<String> list : _7zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _4wList = getTs4Wang(sjp1, zjp1);
        for (List<String> list : _4wList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _6zhaList = getTs6zha(sjp1, zjp1);
        for (List<String> list : _6zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _3wList = getTs3Wang(sjp1, zjp1);
        for (List<String> list : _3wList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _5zhaList = getTs5zha(sjp1, zjp1);
        for (List<String> list : _5zhaList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
//		List<List<String>> _2dwList = getTs2DaWang(sjp1, zjp1);
//		for(List<String> list : _2dwList) {
//			for(String pai : list) {
//				zjp1.remove(pai);
//			}
//		}
//		List<List<String>> _1dw1xwList = getTs1dw1xw(sjp1, zjp1);
//		for(List<String> list : _1dw1xwList) {
//			for(String pai : list) {
//				zjp1.remove(pai);
//			}
//		}
//		List<List<String>> _2xwList = getTs2XiaoWang(sjp1, zjp1);
//		for(List<String> list : _2xwList) {
//			for(String pai : list) {
//				zjp1.remove(pai);
//			}
//		}
        List<List<String>> _ht50kList = getTsHt50k(sjp1, zjp1);
        for (List<String> list : _ht50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _hx50kList = getTsHx50k(sjp1, zjp1);
        for (List<String> list : _hx50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _mh50kList = getTsMh50k(sjp1, zjp1);
        for (List<String> list : _mh50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _fk50kList = getTsFk50k(sjp1, zjp1);
        for (List<String> list : _fk50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        List<List<String>> _min50kList = getTsMin50k(sjp1, zjp1);
        if (_min50kList.size() > 1) {
            List<String> no1 = _min50kList.get(0);
            _min50kList = new ArrayList<List<String>>();
            _min50kList.add(no1);
        }
        for (List<String> list : _min50kList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        sjp1 = new ArrayList<String>();
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F3");
        sjp1.add("F4");
        sjp1.add("F4");
        sjp1.add("F4");
        sjp1.add("F4");
        List<List<String>> _lian4zhangList = getTsLian4zhang1(sjp, zjp1);
        for (List<String> list : _lian4zhangList) {
            for (String pai : list) {
                zjp1.remove(pai);
            }
        }
        all.addAll(_lian4zhangList);
        all.addAll(_min50kList);
        all.addAll(_fk50kList);
        all.addAll(_mh50kList);
        all.addAll(_hx50kList);
        all.addAll(_ht50kList);
//		all.addAll(_1dw1xwList);
//		all.addAll(_2xwList);
//		all.addAll(_2dwList);
        all.addAll(_5zhaList);
//		all.addAll(_3wList);
        all.addAll(_6zhaList);
        all.addAll(_4wList);
        all.addAll(_7zhaList);
        all.addAll(_8zhaList);

        List<List<String>> lian4zhangList = getTsLian4zhang1(sjp, zjp);
        List<List<String>> lian4zhangList1 = new ArrayList<List<String>>();
        lian4zhangList1.addAll(lian4zhangList);
        for (List<String> list : lian4zhangList) {
            if (isHave2(list, _lian4zhangList)) {
                lian4zhangList1.remove(list);
            }
        }
        all.addAll(lian4zhangList1);
        long d2 = System.currentTimeMillis();
        //System.out.println("总耗时：" + (d2 - d1));
        return all;
    }


    /**
     * 返回打得起上家的飞机
     *
     * @param sjp
     * @param zjp
     * @return
     */
    public static List<List<String>> getTsFeiJi(String[] sjp, String[] zjp) {
        List<String> sjp_list = paiToList(sjp);
        List<String> zjp_list = paiToList(zjp);
        sjp = sortFeiji(sjp, "asc");
        String planeType = isFeiJi2(sjp_list);
        List<List<String>> all = new ArrayList<List<String>>();
        List<List<String>> list = new ArrayList<List<String>>();
        List<List<String>> fjlist = new ArrayList<List<String>>();
        zjp_list = sortList(zjp_list, "asc");
        // 从小到大依次是
        //AAA222--222333--333444--略—KKKAAA
        if ("3n".equals(planeType)) {
            if (isFeiJi(sjp)) {
                combinationSt(zjp, 3, list, 3);
                int slen = sjp.length;
                int num = slen / 3;
                if (slen <= zjp.length) {
                    combinationFj(list, num, fjlist, num);
                }
            }
            int sjppai1 = getNumbers(sjp[0]);
            //特殊处理 sjppai1=14=A 转1
            //  sjppai1=15=2 转2
            // sjppai1=13=2 转15
            if(sjppai1==14){
                sjppai1=1;
            }else if(sjppai1==15){
                sjppai1=2;
            }else if(sjppai1==13){
                sjppai1=15;
            }
            for (int i = 0; i < fjlist.size(); i++) {
                List<String> fj = fjlist.get(i);
                String[] fjary = paiToStringArr(fj);
                fjary = sortFeiji(fjary, "asc");
                int fjnum = getNumbers(fjary[0]);
                if (fjnum > sjppai1) {

                    all.add(fj);
                }
            }
        }
//		else if("3n2n".equals(planeType)) {
//			if(isFeiJi(sjp) ) {
//				combinationSt(zjp, 3, list, 3);
//				int slen = sjp.length;
//				int num = slen/4;
//				if(slen <= zjp.length) {
//					combinationFj(list, num, fjlist, num);
//				}
//			}
//			int sjppai1 = getNumbers(sjp[0]);
//			//带牌list
//			for (int i = 0; i <fjlist.size(); i++) {
//				List<String> fj = fjlist.get(i);
//				int size = fj.size()/3;
//				List<List<String>> dplists = getDaiPaiFeiji(fj, zjp, size, size);
//				String[] fjary =paiToStringArr(fj);
//				fjary=  sortFeiji(fjary, "asc");
//				int fjnum = getNumbers(fjary[0]);
//				if(fjnum>sjppai1){
//					for (int j = 0; j < dplists.size(); j++) {
//						List<String>  tsfj = new ArrayList<String>();
//						tsfj.addAll(fj);
//						tsfj.addAll(dplists.get(j));
//						all.add(tsfj);
//					}
//				}
//			}
//			}
//		}
//        else if ("3n2n".equals(planeType)) {
//            if (isFeiJi(sjp)) {
//                combinationSt(zjp, 3, list, 3);
//                int slen = sjp.length;
//                int num = slen / 5;
//                if (slen <= zjp.length) {
//                    combinationFj(list, num, fjlist, num);
//                }
//            }
//            int sjppai1 = getNumbers(sjp[0]);
//            //带牌list
//            for (int i = 0; i < fjlist.size(); i++) {
//                List<String> fj = fjlist.get(i);
//                int size = fj.size() / 3;
//                List<List<String>> dplists = getDaiPaiFeiji(fj, zjp, size * 2, size * 2);
//                //从带牌中删选出2个对子的带牌
//                dplists = getAllDuiziInDplists(dplists);
//                String[] fjary = paiToStringArr(fj);
//                fjary = sortFeiji(fjary, "asc");
//                int fjnum = getNumbers(fjary[0]);
//                if (fjnum > sjppai1) {
//                    for (int j = 0; j < dplists.size(); j++) {
//                        List<String> tsfj = new ArrayList<String>();
//                        tsfj.addAll(fj);
//                        tsfj.addAll(dplists.get(j));
//                        all.add(tsfj);
//                    }
//                }
//            }
//        }
        //System.out.println(all);
        return all;
    }

    public static List<List<String>> getAllDuiziInDplists(List<List<String>> dplists) {
        List<List<String>> all = new ArrayList<List<String>>();
        for (int i = 0; i < dplists.size(); i++) {
            List<String> dpitem = sortList(dplists.get(i), "asc");
            dpitem = sortList(dpitem, "asc");
            int pai1 = getNumbers(dpitem.get(0));
            int pai2 = getNumbers(dpitem.get(1));
            int pai3 = getNumbers(dpitem.get(2));
            int pai4 = getNumbers(dpitem.get(3));
            if (pai1 == pai2 && pai3 == pai4) {
                List<String> ts = new ArrayList<String>();
                ts.add(dpitem.get(0));
                ts.add(dpitem.get(1));
                ts.add(dpitem.get(2));
                ts.add(dpitem.get(3));
                if (isLiandui(ts)) {
                    all.add(ts);
                }
            }
        }
        return all;
    }

    public static List<List<String>> getDaiPaiFeiji(List<String> tip, String[] zjp, int num, int dpnum) {
        List<String> daipai = new ArrayList<String>();
        List<List<String>> list = new ArrayList<List<String>>();
        for (int i = 0; i < zjp.length; i++) {
            for (int j = 0; j < tip.size(); j++) {
                if (zjp[i].equals(tip.get(j))) {
                    zjp = (String[]) ArrayUtils.remove(zjp, i);
                }
            }
        }
        //剩余的牌数小于要带的牌数则直接返回剩余牌
        if (zjp.length <= num) {
            for (int i = 0; i < zjp.length; i++) {
                daipai.add(zjp[i]);
            }
            list.add(daipai);
            return list;
        }

        combination(zjp, num, list, dpnum);
        return list;
    }

    public static void combination(String[] ia, int n, List<List<String>> list, int dpnum) {
        combination("", ia, n, list, dpnum);
    }

    public static void combination(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> list1 = new ArrayList<String>();
                for (int j = 0; j < dpnum; j++) {
                    list1.add(iary[j]);
                }
                list.add(list1);
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination(ss, ii, n - 1, list, dpnum);
            }
        }
    }

    public static void combinationFj(List<List<String>> ia, int n, List<List<String>> list, int dpnum) {
        List<String> l = new ArrayList<String>();
        combinationFj(l, ia, n, list, dpnum);
    }

    public static void combinationFj(List<String> s, List<List<String>> ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.size(); i++) {
                List<String> totalStr = new ArrayList<String>();
                totalStr.addAll(s);
                totalStr.addAll(ia.get(i));
                String[] iary = new String[totalStr.size()];
                for (int j = 0; j < iary.length; j++) {
                    iary[j] = totalStr.get(j);
                }
                //if(isLiandui(iary) && iary.length==dpnum*2) {
                if (isFeiJi(iary) && iary.length == dpnum * 3) {
                    List<String> list1 = new ArrayList<String>();
                    for (int j = 0; j < dpnum * 3; j++) {
                        list1.add(iary[j]);
                    }
                    list.add(list1);
                }
            }
        } else {
            for (int i = 0; i < ia.size() - (n - 1); i++) {
                List<String> ss = new ArrayList<String>();
                ss.addAll(s);
                ss.addAll(ia.get(i));
                List<List<String>> ii = new ArrayList<List<String>>();
                for (int j = 0; j < ia.size() - i - 1; j++) {
                    ii.add(ia.get(i + j + 1));
                }
                combinationFj(ss, ii, n - 1, list, dpnum);
            }
        }
    }

    public static String[] sortFeiji(String[] pai, String type) {
        String[] paiary = new String[pai.length];
        //获取里面所有的三同
        //找出里面所有3个相同的
        //判断三个相同的是否连续
        List<List<String>> planeList = new ArrayList<List<String>>();
        List<String> stlist = new ArrayList<String>();
        for (int i = 0; i < pai.length - 2; i++) {
            List<String> plane = new ArrayList<String>();
            int m = getNumbers(pai[i]);
            int n = getNumbers(pai[i + 1]);
            int k = getNumbers(pai[i + 2]);
            if (m == n && m == k) {
                plane.add(pai[i]);
                plane.add(pai[i + 1]);
                plane.add(pai[i + 2]);
                planeList.add(plane);
            }
        }
        int psize = planeList.size();
        if (psize > 1) {
            for (int i = 0; i < planeList.size() - 1; i++) {
                int m = getNumbers(planeList.get(i).get(0));
                int n = getNumbers(planeList.get(i + 1).get(0));
                if (n - m == 1) {
                    stlist.addAll(planeList.get(i));
                    if (i + 1 == planeList.size() - 1) {
                        stlist.addAll(planeList.get(i + 1));
                    }
                }
            }
        }
        //获取带牌
        stlist = sortList(stlist, type);
        String[] stary = new String[stlist.size()];
        for (int i = 0; i < stlist.size(); i++) {
            stary[i] = stlist.get(i);
        }
        List<String> dplist = new ArrayList<String>();
        for (int i = 0; i < pai.length; i++) {
            if (!ArrayUtils.contains(stary, pai[i])) {
                dplist.add(pai[i]);
            }
        }
        sortList(dplist, "asc");
        //重新组装
        for (int i = 0; i < stlist.size(); i++) {
            paiary[i] = stlist.get(i);
        }
        for (int i = 0; i < dplist.size(); i++) {
            paiary[i + stlist.size()] = dplist.get(i);
        }
        return paiary;

    }


    public static List<String> paiToList(String[] pai) {
        List<String> p = new ArrayList<String>();
        for (int i = 0; i < pai.length; i++) {
            p.add(pai[i]);
        }
        return p;
    }

    public static String[] paiToStringArr(List<String> pai) {
        String[] a = new String[pai.size()];
        for (int i = 0; i < pai.size(); i++) {
            a[i] = pai.get(i);
        }
        return a;
    }

    public static boolean isSt(String[] pai) {
        if (pai.length == 0) {
            return false;
        }
        pai = sortAry(pai, "asc");
        boolean bool = false;
        if (pai.length == 3) {
            int m = getNumbers(pai[0]);
            int n = getNumbers(pai[1]);
            int k = getNumbers(pai[2]);
            if (m == n && m == k && m != 15) {
                bool = true;
            }
        }
        return bool;
    }

    public static void combinationSt(String[] ia, int n, List<List<String>> list, int dpnum) {
        combinationSt("", ia, n, list, dpnum);
    }

    public static void combinationSt(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                if (isSt(iary)) {
                    List<String> list1 = new ArrayList<String>();
                    for (int j = 0; j < dpnum; j++) {
                        list1.add(iary[j]);
                    }
                    if (!isHave(list1, list)) {
                        list.add(list1);
                    }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationSt(ss, ii, n - 1, list, dpnum);
            }
        }
    }

    public static void combinationDz1(String[] ia, int n, List<List<String>> list, int dpnum) {
        combinationDz1("", ia, n, list, dpnum);
    }

    public static void combinationDz1(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                if (isDuizi(iary)) {
                    List<String> list1 = new ArrayList<String>();
                    for (int j = 0; j < dpnum; j++) {
                        list1.add(iary[j]);
                    }
                    if (!isHave(list1, list)) {
                        list.add(list1);
                    }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationDz(ss, ii, n - 1, list, dpnum);
            }
        }
    }


    public static void combinationLd(List<List<String>> ia, int n, List<List<String>> list, int dpnum) {
        List<String> l = new ArrayList<String>();
        combinationLd(l, ia, n, list, dpnum);
    }

    public static void combinationLd(List<String> s, List<List<String>> ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.size(); i++) {
                List<String> totalStr = new ArrayList<String>();
                totalStr.addAll(s);
                totalStr.addAll(ia.get(i));
                String[] iary = new String[totalStr.size()];
                for (int j = 0; j < iary.length; j++) {
                    iary[j] = totalStr.get(j);
                }
                //if(isLiandui(iary) && iary.length==dpnum*2) {
                if (isLiandui(iary) && iary.length == dpnum * 2) {
                    List<String> list1 = new ArrayList<String>();
                    for (int j = 0; j < dpnum * 2; j++) {
                        list1.add(iary[j]);
                    }
                    list.add(list1);
                }
            }
        } else {
            for (int i = 0; i < ia.size() - (n - 1); i++) {
                List<String> ss = new ArrayList<String>();
                ss.addAll(s);
                ss.addAll(ia.get(i));
                List<List<String>> ii = new ArrayList<List<String>>();
                for (int j = 0; j < ia.size() - i - 1; j++) {
                    ii.add(ia.get(i + j + 1));
                }
                combinationLd(ss, ii, n - 1, list, dpnum);
            }
        }
    }

    /**
     * 把牌转成带花色数组
     *
     * @param pai
     * @return
     */
    public static int[] paiToIntAry(String[] pai) {
//       int[] iary = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};// "M5","M6","M7","M8","M9","M10","M11","M12","M13","M14","M15"33-43
        int[] iary = {0, 0,0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};//  "M3","M4","M5","M6","M7","M8","M9","M10","M11","M12","M13","M14","M15"33-43
        // 大王W(6)小王w(5)黑B(4)红R(3)梅M(2)方F(1)
        for (int i = 0; i < pai.length; i++) {
            if(getNumbers(pai[i])==16){
                continue;
            }else{
                iary[getNumbers(pai[i]) - 3] += 1;
            }

        }
        return iary;
    }

    /**
     * 判断一组牌是否为连对
     *
     * @param pai
     * @return
     */
    public static boolean isLiandui(String[] pai) {
        if (pai.length == 0) {
            return false;
        }
        pai = sortAry(pai, "asc");
        boolean bool = true;
        if (pai.length % 2 != 0 || pai.length < 4) {
            bool = false;
            return bool;
        }
        for (int i = 0; i < pai.length - 2; i++) {
//			if(getNumbers(pai[i+2])==15 || getNumbers(pai[i+1])==15 || getNumbers(pai[i]) == 15) {
//				bool = false;
//				return bool;
//			}
            if (getNumbers(pai[i + 2]) - getNumbers(pai[i]) != 1 || (getNumbers(pai[i]) - getNumbers(pai[i + 1]) != 0 && i % 2 == 0)) {
                bool = false;
                return bool;
            }
        }

        return bool;
    }

    public static String[] sortAry(String[] pai, String type) {
        for (int i = 0; i < pai.length - 1; i++) {
            for (int j = 0; j < pai.length - i - 1; j++) {
                if ("asc".equals(type)) {
                    if (comparePai(pai[j], pai[j + 1]) > 0) {    //把小的值交换到后面
                        String temp = pai[j];
                        pai[j] = pai[j + 1];
                        pai[j + 1] = temp;
                    }
                } else if ("desc".equals(type)) {
                    if (comparePai(pai[j], pai[j + 1]) < 0) {    //把小的值交换到后面
                        String temp = pai[j];
                        pai[j] = pai[j + 1];
                        pai[j + 1] = temp;
                    }
                }

            }
        }
        return pai;
    }

    /**
     * 判断一组牌是否对子
     *
     * @param pai
     * @return
     */
    public static boolean isDuizi(String[] pai) {
        if (pai.length == 0) {
            return false;
        }
        pai = sortAry(pai, "asc");
        boolean bool = false;
        if (pai.length == 2) {
            int m = getNumbers(pai[0]);
            int n = getNumbers(pai[1]);
            if (m == n && m != 16) {
                bool = true;
            }
        }
        return bool;
    }

    public static void main(String[] args) {
        long s = System.currentTimeMillis();
        List<String> pai = new ArrayList<>();
		pai.add("B3");
		pai.add("R3");
		pai.add("R3");
		pai.add("B15");
		pai.add("B15");
		pai.add("M15");


        List<String> zjp = new ArrayList<>();
        zjp.add("R14");
        zjp.add("R15");
        zjp.add("B14");
        zjp.add("B14");
        zjp.add("B15");
        zjp.add("B15");
//		zjp.add("B9");
//		zjp.add("B9");
//		zjp.add("M10");
//		zjp.add("B10");
        System.out.println("---");
        System.out.println("type::" + getCpType2(pai, 1, 1));
//		System.out.println("ts:" +getAllTsNew(pai,zjp));
//		String[] a =listToStringAry(pai);
//		String[] b = listToStringAry(zjp);
//		System.out.println("ts:" +getTsFeiJi(a, b));
        long e = System.currentTimeMillis();
        System.out.println("runtime:" + (e - s));
    }

    public static String[] listToStringAry(List<String> pai) {
        String[] a = new String[pai.size()];
        int i = 0;
        for (String pa : pai) {
            a[i] = pa;
            i++;
        }
        return a;
    }

    public static String listToString(List<String> pai) {
        StringBuffer s = new StringBuffer();
        for (String pa : pai) {
            s.append(pa).append(",");
        }
        return s.substring(0, s.length() - 1);
    }

    public static List<String> intCardToStringCard(List<Integer> cardList) {
        List<String> ls = new ArrayList<>();
        for (Integer in : cardList) {
            //			方片 1 梅花2 洪涛3 黑桃4 5王
            //		B  R  M F
            if (in > 100 && in < 200) {
                int p = in - 100;
                ls.add("F" + p);
            }
            if (in > 200 && in < 300) {
                int p = in - 200;
                ls.add("M" + p);
            }
            if (in > 300 && in < 400) {
                int p = in - 300;
                ls.add("R" + p);
            }
            if (in > 400 && in < 500) {
                int p = in - 400;
                ls.add("B" + p);
            }
            if (501 == in) {
                ls.add("w16");
            }
            if (502 == in) {
                ls.add("W16");
            }
        }
        return ls;
    }

    public static List<Integer> stringCardToIntCard(List<String> cardList) {
        List<Integer> ls = new ArrayList<>();
        for (int i = 0; i < cardList.size(); i++) {
            String s = cardList.get(i);
            if (s.startsWith("F")) {
                ls.add(getNumbers(s) + 100);
            }
            if (s.startsWith("M")) {
                ls.add(getNumbers(s) + 200);
            }
            if (s.startsWith("R")) {
                ls.add(getNumbers(s) + 300);
            }
            if (s.startsWith("B")) {
                ls.add(getNumbers(s) + 400);
            }
            if (s.startsWith("w")) {
                ls.add(501);
            }
            if (s.startsWith("W")) {
                ls.add(502);
            }
        }
        return ls;
    }

    public static int stringCardToIntCard1(String cardList) {
        List<String> ls = new ArrayList<>();
        ls.add(cardList);
        int carnum = stringCardToIntCard(ls).get(0);
        return carnum;
    }

    //
    public static int getRandomCardNum(List<String> ls, String card) {
        int num = 0;
        for (String pai : ls) {
            if (pai.equals(card)) {
                num++;
            }
        }
        return num;
    }
}
