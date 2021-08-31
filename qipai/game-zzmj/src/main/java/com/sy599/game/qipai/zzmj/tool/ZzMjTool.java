package com.sy599.game.qipai.zzmj.tool;

import java.time.Clock;
import java.util.*;
import java.util.Map.Entry;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.zzmj.rule.ZzMj;
import com.sy599.game.qipai.zzmj.rule.ZzMjIndex;
import com.sy599.game.qipai.zzmj.rule.ZzMjIndexArr;
import com.sy599.game.qipai.zzmj.tool.hulib.util.HuUtil;
import com.sy599.game.util.JacksonUtil;

/**
 * @author lc
 */
public class ZzMjTool {

    /**
     * 获取听牌列表
     * cards.size+hzCount = 3n+1
     *
     * @param cardArr 去掉红中的牌
     * @param hzCount 红中数
     * @param hu7dui  是否可胡七对
     * @return
     */
    public static List<ZzMj> getLackList(int[] cardArr, int hzCount, boolean hu7dui) {
        int cardNum = 0;
        for (int i = 0, length = cardArr.length; i < length; i++) {
            cardNum += cardArr[i];
        }
        if ((cardNum + hzCount) % 3 != 1) {
            return Collections.emptyList();
        }
        List<ZzMj> lackPaiList = new ArrayList<>();
        Set<Integer> have = new HashSet<>();
        for (ZzMj mj : ZzMj.fullMj) {
            if (mj.isHongzhong() || have.contains(mj.getVal())) {
                continue;
            }
            int cardIndex = HuUtil.getMjIndex(mj);
            cardArr[cardIndex] = cardArr[cardIndex] + 1;
            if (hu7dui && HuUtil.isCanHu7Dui(cardArr, hzCount)) {
                lackPaiList.add(mj);
                have.add(mj.getVal());
            }
            if (HuUtil.isCanHu(cardArr, hzCount)) {
                lackPaiList.add(mj);
                have.add(mj.getVal());
            }
            cardArr[cardIndex] = cardArr[cardIndex] - 1;
        }
        if (lackPaiList.size() == 27) {
            lackPaiList.clear();
            lackPaiList.add(null);
        }
        return lackPaiList;
    }


    public static synchronized List<List<ZzMj>> fapai(List<Integer> copy, List<List<Integer>> t) {
        List<List<ZzMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<ZzMj> pai = new ArrayList<>();
        int j = 1;

        int testcount = 0;
        if (GameServerConfig.isDebug() && t != null && !t.isEmpty()) {
            for (List<Integer> zp : t) {
                list.add(ZzMjHelper.find(copy, zp));
                testcount += zp.size();
            }

            if (list.size() == 4) {
                list.add(ZzMjHelper.toMajiang(copy));
                System.out.println(JacksonUtil.writeValueAsString(list));
                return list;
            } else if (list.size() == 5) {
                return list;
            }
        }

        List<Integer> copy2 = new ArrayList<>(copy);
        int fapaiCount = 13 * 4 + 1 - testcount;
        if (pai.size() >= 14) {
            list.add(pai);
            pai = new ArrayList<>();
        }

        boolean test = false;
        if (list.size() > 0) {
            test = true;
        }

        for (int i = 0; i < fapaiCount; i++) {
            // 发牌张数=13*4+1 正好第一个发牌的人14张其他人13张
            ZzMj majiang = ZzMj.getMajang(copy.get(i));
            copy2.remove((Object) copy.get(i));
            if (test) {
                if (i < j * 13) {
                    pai.add(majiang);
                } else {
                    list.add(pai);
                    pai = new ArrayList<>();
                    pai.add(majiang);
                    j++;
                }
            } else {
                if (i <= j * 13) {
                    pai.add(majiang);
                } else {
                    list.add(pai);
                    pai = new ArrayList<>();
                    pai.add(majiang);
                    j++;
                }
            }

        }
        list.add(pai);
        List<ZzMj> left = new ArrayList<>();
        for (int i = 0; i < copy2.size(); i++) {
            left.add(ZzMj.getMajang(copy2.get((i))));
        }
        list.add(left);
        return list;
    }

    public static synchronized List<List<ZzMj>> fapai(List<Integer> copy, int playerCount) {
        List<List<ZzMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<ZzMj> allMjs = new ArrayList<>();
        for (int id : copy) {
            allMjs.add(ZzMj.getMajang(id));
        }
        for (int i = 0; i < playerCount; i++) {
            if (i == 0) {
                list.add(new ArrayList<>(allMjs.subList(0, 14)));
            } else {
                list.add(new ArrayList<>(allMjs.subList(14 + (i - 1) * 13, 14 + (i - 1) * 13 + 13)));
            }
            if (i == playerCount - 1) {
                list.add(new ArrayList<>(allMjs.subList(14 + (i) * 13, allMjs.size())));
            }

        }
        return list;
    }

    public static synchronized List<List<ZzMj>> fapai(List<Integer> copy, int playerCount, List<List<Integer>> t) {
        List<List<ZzMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<List<ZzMj>> zpList = new ArrayList<>();
        if (GameServerConfig.isDebug() && t != null && !t.isEmpty()) {
            for (List<Integer> zp : t) {
                zpList.add(ZzMjHelper.find(copy, zp));
            }
        }
        List<ZzMj> allMjs = new ArrayList<>();
        for (int id : copy) {
            allMjs.add(ZzMj.getMajang(id));
        }
        int count = 0;
        for (int i = 0; i < playerCount; i++) {
            if (i == 0) {
                if (zpList.size() > 0) {
                    List<ZzMj> pai = zpList.get(0);
                    int len = 14 - pai.size();
                    pai.addAll(allMjs.subList(count, len));
                    count += len;
                    list.add(new ArrayList<>(pai));
                } else {
                    list.add(new ArrayList<>(allMjs.subList(0, 14)));
                }
            } else {
                if (zpList.size() > i) {
                    List<ZzMj> pai = zpList.get(i);
                    int len = 13 - pai.size();
                    pai.addAll(allMjs.subList(count, count + len));
                    count += len;
                    list.add(new ArrayList<>(pai));
                } else {
                    list.add(new ArrayList<>(allMjs.subList(count, count + 13)));
                    count += 13;
                }
            }
            if (i == playerCount - 1) {
                if (zpList.size() > i + 1) {
                    List<ZzMj> pai = zpList.get(i + 1);
                    pai.addAll(allMjs.subList(count, allMjs.size()));
                    list.add(new ArrayList<>(pai));
                } else {
                    list.add(new ArrayList<>(allMjs.subList(count, allMjs.size())));
                }
            }

        }
        t.clear();
        return list;
    }

    public static synchronized List<List<ZzMj>> fapai(List<Integer> copy) {
        List<List<ZzMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<ZzMj> pai = new ArrayList<>();
        int j = 1;

        int testcount = 0;
        if (GameServerConfig.isDeveloper()) {
            if (list.size() == 5) {
                System.out.println(JacksonUtil.writeValueAsString(list));
                return list;
            }
        }

        List<Integer> copy2 = new ArrayList<>(copy);

        int fapaiCount = 13 * 4 + 1 - testcount;
        if (pai.size() >= 14) {
            list.add(pai);
            pai = new ArrayList<>();
        }

        boolean test = false;
        if (list.size() > 0) {
            test = true;
        }

        for (int i = 0; i < fapaiCount; i++) {
            // 发牌张数=13*4+1 正好第一个发牌的人14张其他人13张
            ZzMj majiang = ZzMj.getMajang(copy.get(i));
            copy2.remove((Object) copy.get(i));
            if (test) {
                if (i < j * 13) {
                    pai.add(majiang);
                } else {
                    list.add(pai);
                    pai = new ArrayList<>();
                    pai.add(majiang);
                    j++;
                }
            } else {
                if (i <= j * 13) {
                    pai.add(majiang);
                } else {
                    list.add(pai);
                    pai = new ArrayList<>();
                    pai.add(majiang);
                    j++;
                }
            }

        }
        list.add(pai);
        List<ZzMj> left = new ArrayList<>();
        for (int i = 0; i < copy2.size(); i++) {
            left.add(ZzMj.getMajang(copy2.get((i))));
        }
        list.add(left);
        return list;
    }

    public static boolean isPingHu(List<ZzMj> majiangIds) {
        return isPingHu(majiangIds, true);

    }

    public static boolean isPingHu(List<ZzMj> majiangIds, boolean needJiang258) {
        if (majiangIds == null || majiangIds.isEmpty()) {
            return false;
        }
        if (majiangIds.size() % 3 != 2) {
            System.out.println("%3！=2");
            return false;

        }

        // 先去掉红中
        List<ZzMj> copy = new ArrayList<>(majiangIds);
        List<ZzMj> hongzhongList = dropHongzhong(copy);

        ZzMjIndexArr card_index = new ZzMjIndexArr();
        ZzMjQipaiTool.getMax(card_index, copy);
        // 拆将
        if (chaijiang(card_index, copy, hongzhongList.size(), needJiang258)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 麻将胡牌
     *
     * @param mjs
     * @return
     */
    public static boolean isHu(List<ZzMj> mjs, boolean hu7dui) {
        if (mjs == null || mjs.isEmpty()) {
            return false;
        }
        List<ZzMj> copy = new ArrayList<>(mjs);
        // 先去掉红中
//		List<ZzMj> hongzhongList = dropHongzhong(copy);
//		if (hongzhongList.size() == 4) {
//			// 4张红中直接胡
//			return true;
//		}
        if (mjs.size() % 3 != 2) {
            return false;
        }
        ZzMjIndexArr card_index = new ZzMjIndexArr();
        ZzMjQipaiTool.getMax(card_index, copy);
        if (hu7dui && check7duizi(copy, card_index, 0)) {
            return true;
        }

        return HuUtil.isCanHu(new ArrayList<>(mjs),0);
        // 拆将
//        if (chaijiang(card_index, copy, 0, false)) {
//            return true;
//        } else {
//            return false;
//        }
    }

    public static boolean isTing(List<ZzMj> majiangIds, boolean hu7dui) {
        if (majiangIds == null || majiangIds.isEmpty()) {
            return false;
        }
        List<ZzMj> copy = new ArrayList<>(majiangIds);
        if (majiangIds.size() % 3 != 1) {
            return false;
        }
        ZzMjIndexArr card_index = new ZzMjIndexArr();
        ZzMjQipaiTool.getMax(card_index, copy);
        if (hu7dui && check7duizi(copy, card_index, 1)) {
            return true;
        }
        return HuUtil.isCanHu(copy,1);
        // 拆将
//        if (chaijiang(card_index, copy, 1, false)) {
//            return true;
//        } else {
//            return false;
//        }
    }

    /**
     * 红中麻将没有7小对，所以不用红中补
     *
     * @param majiangIds
     * @param card_index
     */
    public static boolean check7duizi(List<ZzMj> majiangIds, ZzMjIndexArr card_index, int hongzhongNum) {
        if (majiangIds.size() == 14) {
            // 7小对
            int duizi = card_index.getDuiziNum();
            if (duizi == 7) {
                return true;
            }

        } else if (majiangIds.size() + hongzhongNum == 14) {
            if (hongzhongNum == 0) {
                return false;
            }

            ZzMjIndex index0 = card_index.getMajiangIndex(0);
            ZzMjIndex index2 = card_index.getMajiangIndex(2);
            int lackNum = index0 != null ? index0.getLength() : 0;
            lackNum += index2 != null ? index2.getLength() : 0;

            if (lackNum <= hongzhongNum) {
                return true;
            }

            if (lackNum == 0) {
                lackNum = 14 - majiangIds.size();
                if (lackNum == hongzhongNum) {
                    return true;
                }
            }

        }
        return false;
    }

    // 拆将
    public static boolean chaijiang(ZzMjIndexArr card_index, List<ZzMj> hasPais, int hongzhongnum, boolean needJiang258) {
        Map<Integer, List<ZzMj>> jiangMap = card_index.getJiang(needJiang258);
        for (Entry<Integer, List<ZzMj>> valEntry : jiangMap.entrySet()) {
            List<ZzMj> copy = new ArrayList<>(hasPais);
            ZzMjHuLack lack = new ZzMjHuLack(hongzhongnum);
            List<ZzMj> list = valEntry.getValue();
            int i = 0;
            for (ZzMj majiang : list) {
                i++;
                copy.remove(majiang);
                if (i >= 2) {
                    break;
                }
            }
            lack.setHasJiang(true);
            boolean hu = chaipai(lack, copy, needJiang258);
            if (hu) {
                return hu;
            }
        }

        if (hongzhongnum > 0) {
            // 只剩下红中
            if (hasPais.isEmpty()) {
                return true;
            }
            // 没有将
            for (ZzMj majiang : hasPais) {
                List<ZzMj> copy = new ArrayList<>(hasPais);
                ZzMjHuLack lack = new ZzMjHuLack(hongzhongnum);
                boolean isJiang = false;
                if (!needJiang258) {
                    // 不需要将
                    isJiang = true;

                } else {
                    // 需要258做将
                    if (majiang.getPai() == 2 || majiang.getPai() == 5 || majiang.getPai() == 8) {
                        isJiang = true;
                    }

                }
                if (isJiang) {
                    lack.setHasJiang(true);
                    lack.changeHongzhong(-1);
                    lack.addLack(majiang.getVal());
                    copy.remove(majiang);
                }

                boolean hu = chaipai(lack, copy, needJiang258);
                if (lack.isHasJiang() && hu) {
                    return true;
                }
                if (!lack.isHasJiang() && hu) {
                    if (lack.getHongzhongNum() == 2) {
                        // 红中做将
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // 拆牌
    public static boolean chaipai(ZzMjHuLack lack, List<ZzMj> hasPais, boolean isNeedJiang258) {
        if (hasPais.isEmpty()) {
            return true;

        }
        boolean hu = chaishun(lack, hasPais, isNeedJiang258);
        if (hu)
            return true;
        return false;
    }

    public static void sortMin(List<ZzMj> hasPais) {
        Collections.sort(hasPais, new Comparator<ZzMj>() {

            @Override
            public int compare(ZzMj o1, ZzMj o2) {
                if (o1.getPai() < o2.getPai()) {
                    return -1;
                }
                if (o1.getPai() > o2.getPai()) {
                    return 1;
                }
                return 0;
            }

        });
    }

    /**
     * 拆顺
     *
     * @param hasPais
     * @return
     */
    public static boolean chaishun(ZzMjHuLack lack, List<ZzMj> hasPais, boolean needJiang258) {
        if (hasPais.isEmpty()) {
            return true;
        }
        sortMin(hasPais);
        ZzMj minMajiang = hasPais.get(0);
        int minVal = minMajiang.getVal();
        List<ZzMj> minList = ZzMjQipaiTool.getVal(hasPais, minVal);
        if (minList.size() >= 3) {
            // 先拆坎子
            removeAllPai(hasPais, minList.subList(0, 3));
            //hasPais.removeAll(minList.subList(0, 3));
            return chaipai(lack, hasPais, needJiang258);
        }

        // 做顺子
        int pai1 = minVal;
        int pai2 = 0;
        int pai3 = 0;
        if (pai1 % 10 == 9) {
            pai1 = pai1 - 2;

        } else if (pai1 % 10 == 8) {
            pai1 = pai1 - 1;
        }
        pai2 = pai1 + 1;
        pai3 = pai2 + 1;

        List<Integer> lackList = new ArrayList<>();
        List<ZzMj> num1 = ZzMjQipaiTool.getVal(hasPais, pai1);
        List<ZzMj> num2 = ZzMjQipaiTool.getVal(hasPais, pai2);
        List<ZzMj> num3 = ZzMjQipaiTool.getVal(hasPais, pai3);

        // 找到一句话的麻将
        List<ZzMj> hasMajiangList = new ArrayList<>();
        if (!num1.isEmpty()) {
            hasMajiangList.add(num1.get(0));
        }
        if (!num2.isEmpty()) {
            hasMajiangList.add(num2.get(0));
        }
        if (!num3.isEmpty()) {
            hasMajiangList.add(num3.get(0));
        }

        // 一句话缺少的麻将
        if (num1.isEmpty()) {
            lackList.add(pai1);
        }
        if (num2.isEmpty()) {
            lackList.add(pai2);
        }
        if (num3.isEmpty()) {
            lackList.add(pai3);
        }

        int lackNum = lackList.size();
        if (lackNum > 0) {
            if (lack.getHongzhongNum() <= 0) {
                return false;
            }

            // 做成一句话缺少2张以上的，没有将优先做将
            if (lackNum >= 2) {
                // 补坎子
                List<ZzMj> count = ZzMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                if (count.size() >= 3) {
                    removeAllPai(hasPais, count);
                    //hasPais.removeAll(count);
                    return chaipai(lack, hasPais, needJiang258);

                } else if (count.size() == 2) {
                    if (!lack.isHasJiang() && isCanAsJiang(count.get(0), needJiang258)) {
                        // 没有将做将
                        lack.setHasJiang(true);
                        removeAllPai(hasPais, count);
                        //hasPais.removeAll(count);
                        return chaipai(lack, hasPais, needJiang258);
                    }

                    // 拿一张红中补坎子
                    lack.changeHongzhong(-1);
                    lack.addLack(count.get(0).getVal());
                    removeAllPai(hasPais, count);
                    //hasPais.removeAll(count);
                    return chaipai(lack, hasPais, needJiang258);
                }

                // 做将
                if (!lack.isHasJiang() && isCanAsJiang(count.get(0), needJiang258) && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.setHasJiang(true);
                    removeAllPai(hasPais, count);
                    //hasPais.removeAll(count);
                    lack.addLack(count.get(0).getVal());
                    return chaipai(lack, hasPais, needJiang258);
                }
            } else if (lackNum == 1) {
                // 做将
                if (!lack.isHasJiang() && isCanAsJiang(minMajiang, needJiang258) && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.setHasJiang(true);
                    hasPais.remove(minMajiang);
                    lack.addLack(minMajiang.getVal());
                    return chaipai(lack, hasPais, needJiang258);
                }

                List<ZzMj> count = ZzMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                if (count.size() == 2 && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.addLack(count.get(0).getVal());
                    removeAllPai(hasPais, count);
                    //hasPais.removeAll(count);
                    return chaipai(lack, hasPais, needJiang258);
                }
            }

            // 如果有红中则补上
            if (lack.getHongzhongNum() >= lackNum) {
                lack.changeHongzhong(-lackNum);
                removeAllPai(hasPais, hasMajiangList);
                //hasPais.removeAll(hasMajiangList);
                lack.addAllLack(lackList);

            } else {
                return false;
            }
        } else {
            // 可以一句话
            if (lack.getHongzhongNum() > 0) {
                List<ZzMj> count1 = ZzMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                List<ZzMj> count2 = ZzMjQipaiTool.getVal(hasPais, hasMajiangList.get(1).getVal());
                List<ZzMj> count3 = ZzMjQipaiTool.getVal(hasPais, hasMajiangList.get(2).getVal());
                if (count1.size() >= 2 && (count2.size() == 1 || count3.size() == 1)) {
                    List<ZzMj> copy = new ArrayList<>(hasPais);
                    removeAllPai(copy, count1);
                    //copy.removeAll(count1);
                    ZzMjHuLack copyLack = lack.copy();
                    copyLack.changeHongzhong(-1);

                    copyLack.addLack(hasMajiangList.get(0).getVal());
                    if (chaipai(copyLack, copy, needJiang258)) {
                        return true;
                    }
                }
            }
            removeAllPai(hasPais, hasMajiangList);
            //hasPais.removeAll(hasMajiangList);
        }
        return chaipai(lack, hasPais, needJiang258);
    }

    public static void removeAllPai(List<ZzMj> hasPais, List<ZzMj> remPai) {
        for (ZzMj majiang : remPai) {
            hasPais.remove(majiang);
        }
    }

    public static boolean isCanAsJiang(ZzMj majiang, boolean isNeed258) {
        if (isNeed258) {
            if (majiang.getPai() == 2 || majiang.getPai() == 5 || majiang.getPai() == 8) {
                return true;
            }
            return false;
        } else {
            return true;
        }

    }

    public static List<ZzMj> checkChi(List<ZzMj> majiangs, ZzMj dismajiang) {
        return checkChi(majiangs, dismajiang, null);
    }

    /**
     * 是否能吃
     *
     * @param majiangs
     * @param dismajiang
     * @return
     */
    public static List<ZzMj> checkChi(List<ZzMj> majiangs, ZzMj dismajiang, List<Integer> wangValList) {
        int disMajiangVal = dismajiang.getVal();
        List<Integer> chi1 = new ArrayList<>(Arrays.asList(disMajiangVal - 2, disMajiangVal - 1));
        List<Integer> chi2 = new ArrayList<>(Arrays.asList(disMajiangVal - 1, disMajiangVal + 1));
        List<Integer> chi3 = new ArrayList<>(Arrays.asList(disMajiangVal + 1, disMajiangVal + 2));

        List<Integer> majiangIds = ZzMjHelper.toMajiangVals(majiangs);
        if (wangValList == null || !checkWang(chi1, wangValList)) {
            if (majiangIds.containsAll(chi1)) {
                return findMajiangByVals(majiangs, chi1);
            }
        }
        if (wangValList == null || !checkWang(chi2, wangValList)) {
            if (majiangIds.containsAll(chi2)) {
                return findMajiangByVals(majiangs, chi2);
            }
        }
        if (wangValList == null || !checkWang(chi3, wangValList)) {
            if (majiangIds.containsAll(chi3)) {
                return findMajiangByVals(majiangs, chi3);
            }
        }
        return new ArrayList<ZzMj>();
    }

    public static List<ZzMj> findMajiangByVals(List<ZzMj> majiangs, List<Integer> vals) {
        List<ZzMj> result = new ArrayList<>();
        for (int val : vals) {
            for (ZzMj majiang : majiangs) {
                if (majiang.getVal() == val) {
                    result.add(majiang);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 去掉红中
     *
     * @param copy
     * @return
     */
    public static List<ZzMj> dropHongzhong(List<ZzMj> copy) {
        List<ZzMj> hongzhong = new ArrayList<>();
        Iterator<ZzMj> iterator = copy.iterator();
        while (iterator.hasNext()) {
            ZzMj majiang = iterator.next();
            if (majiang.getVal() > 200) {
                hongzhong.add(majiang);
                iterator.remove();
            }
        }
        return hongzhong;
    }

    public static boolean checkWang(Object majiangs, List<Integer> wangValList) {
        if (majiangs instanceof List) {
            List list = (List) majiangs;
            for (Object majiang : list) {
                int val = 0;
                if (majiang instanceof ZzMj) {
                    val = ((ZzMj) majiang).getVal();
                } else {
                    val = (int) majiang;
                }
                if (wangValList.contains(val)) {
                    return true;
                }
            }
        }

        return false;

    }

    /**
     * 相同的麻将
     *
     * @param majiangs 麻将牌
     * @param majiang  麻将
     * @param num      想要的数量
     * @return
     */
    public static List<ZzMj> getSameMajiang(List<ZzMj> majiangs, ZzMj majiang, int num) {
        List<ZzMj> hongzhong = new ArrayList<>();
        int i = 0;
        for (ZzMj maji : majiangs) {
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
    public static List<ZzMj> dropMjId(List<ZzMj> copy, int id) {
        List<ZzMj> hongzhong = new ArrayList<>();
        Iterator<ZzMj> iterator = copy.iterator();
        while (iterator.hasNext()) {
            ZzMj majiang = iterator.next();
            if (majiang.getId() == id) {
                hongzhong.add(majiang);
                iterator.remove();
            }
        }
        return hongzhong;
    }

    public static List<ZzMj> getPais(String paisStr) {
        String[] pais = paisStr.split(",");
        List<ZzMj> handPais = new ArrayList<>();
        for (String pai : pais) {
            for (ZzMj mj : ZzMj.values()) {
                if (mj.getVal() == Integer.valueOf(pai) && !handPais.contains(mj)) {
                    handPais.add(mj);
                    break;
                }
            }
        }
        return handPais;
    }

    public static void main(String[] args) {
        HuUtil.init();
        int laiZiVal = 331;
        int laiZiNum = 0;
        String paisStr = "331,331,331,11,12,13,21,22,23,31,32,33,39,39";
        paisStr = "33,33,33,36,36,36,38,38,39,39,38";
        List<ZzMj> handPais = getPais(paisStr);
        System.out.println(handPais);
        int count = 1;
        boolean canHu = false;
        long start = Clock.systemDefaultZone().millis();

        for (int i = 0; i < count; i++) {
            canHu = isHu(handPais, true);
        }
        long timeUse = Clock.systemDefaultZone().millis() - start;
        System.out.println("canHu = " + canHu + " , count = " + count + " , timeUse = " + timeUse + " ms");


        canHu = false;
        start = Clock.systemDefaultZone().millis();
        for (int i = 0; i < count; i++) {
            canHu = HuUtil.isCanHu(handPais, laiZiNum);
        }
        timeUse = Clock.systemDefaultZone().millis() - start;
        System.out.println("canHu = " + canHu + " , count = " + count + " , timeUse = " + timeUse + " ms");

        List<ZzMj> lackPaiList = HuUtil.getLackPaiList(handPais, laiZiNum - 1);
        System.out.println(lackPaiList);
    }

}
