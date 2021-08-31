package com.sy599.game.qipai.yjmj.rule;

import com.sy599.game.qipai.yjmj.bean.YjMjHu;
import com.sy599.game.qipai.yjmj.tool.YjMjTool;
import com.sy599.game.qipai.yjmj.tool.YjMjQipaiTool;
import com.sy599.game.util.DataMapUtil;

import java.util.*;

/**
 * 沅江麻将规则
 */
public class YjMjRule {

    /**
     * 0 碰碰胡  1 将将胡 2 清一色 3 七小队 4 豪华七小队 5 双豪华七小队 6三豪华七小队 7 杠爆 8 抢杠胡 9 海底捞 10 一条龙 11 门清 12 天胡 13 一字翘
     *
     * @param mjs
     * @param gang
     * @param peng
     * @param canMengQing 是否有门清
     * @param canMaMaHu   是否可胡码码胡
     * @return
     */
    public static int[] checkDahu(YjMjHu hu, List<YjMj> mjs, List<YjMj> gang, List<YjMj> aGangs, List<YjMj> peng, boolean canMengQing, boolean canMaMaHu) {
        int arr[] = new int[YjMjHu.daHuCount];
        if (mjs.size() % 3 != 2) {
            return arr;
        }

        List<YjMj> allMjs = new ArrayList<>();
        allMjs.addAll(mjs);
        allMjs.addAll(gang);
        allMjs.addAll(peng);

        YjMjIndexArr all_card_index = new YjMjIndexArr();
        YjMjQipaiTool.getMax(all_card_index, allMjs);

        YjMjIndexArr card_index = new YjMjIndexArr();
        YjMjQipaiTool.getMax(card_index, mjs);

        // 碰碰胡
        if (isPengPengHu(mjs, all_card_index)) {
            arr[YjMjHu.daHu_pengPengHu] = 1;
            hu.setPengpengHu(true);
        }
        // 将将胡
        if (isJiangJiangHu(allMjs)) {
            arr[YjMjHu.daHu_jiangJiangHu] = 1;
            hu.setJiangjiangHu(true);
        }
        // 7小对
        int duiziNum = card_index.getDuiziNum();
        duiziNum += YjMj.getHongzhongNum(mjs);// 报听的红中也算一对
        if (duiziNum == 7) {
            // 是否有豪华7小对
            YjMjIndex index = card_index.getMajiangIndex(3);
            if (index != null) {// 有4个一样的牌
                if (index.getLength() == 3) {// 三豪华7小对
                    arr[YjMjHu.daHu_sanHaoXiao7Dui] = 1;
                    hu.setSan7xiaodui(true);
                } else if (index.getLength() == 2) {// 双豪华7小对
                    arr[YjMjHu.daHu_shuangHaoXiao7Dui] = 1;
                    hu.setShuang7xiaodui(true);
                } else if (index.getLength() == 1) {// 豪华7小对
                    arr[YjMjHu.daHu_haoXiao7Dui] = 1;
                    hu.setHao7xiaodui(true);
                }
            } else {// 普通7小对
                arr[YjMjHu.daHu_xiao7Dui] = 1;
                hu.setXiao7dui(true);
            }
        }
        // 杠爆
        if (hu.isGangBao()) {
            arr[YjMjHu.daHu_gangBao] = 1;
        }

        // 清一色
        if (isQingYiSeHu(hu, allMjs, mjs)) {
            arr[YjMjHu.daHu_qingYiSe] = 1;
            hu.setQingyiseHu(true);
        }

        // 一条龙
        if (isYiTiaoLong(mjs)) {
            arr[YjMjHu.daHu_yiTiaoLong] = 1;
            hu.setYiTiaoLong(true);
        }

        // 码码胡
        if (canMaMaHu && isMaMaHu(allMjs)) {
            arr[YjMjHu.daHu_maMaHu] = 1;
            hu.setMaMaHu(true);
        }

        // 门清 最后计算：因为只有七小对胡时不算门清
        if (canMengQing == true && isMenQing(hu, mjs, aGangs)) {
            arr[YjMjHu.daHu_mengQing] = 1;
            hu.setMengQing(true);
        }

        // 天胡
        if (hu.isTianHu()) {
            arr[YjMjHu.daHu_tianHu] = 1;
        }
        // 一字撬
        if (mjs.size() == 2) {// 两张牌只要有一个红中算可以胡了
            if (mjs.get(0).getVal() == mjs.get(1).getVal()) {
                arr[YjMjHu.daHu_yiZiQiao] = 1;
                hu.setYiZiQiao(true);
            }
        }

        List<Integer> list = DataMapUtil.toList(arr);
        if (list.contains(1)) {
            hu.setHu(true);
            hu.setDahu(true);
            hu.setDahuList(list);
        } else {
            list.clear();
            hu.setDahuList(list);
        }
        return arr;
    }

    /**
     * 是否是清一色
     *
     * @param allMajiangs
     * @param majiangIds
     * @return
     */
    private static boolean isqingyise(List<YjMj> allMajiangs, List<YjMj> majiangIds) {
        boolean qingyise = false;
        int se = 0;
        for (YjMj mjiang : allMajiangs) {
            if (mjiang.isHongzhong()) {
                continue;
            }
            if (se == 0) {
                qingyise = true;
                se = mjiang.getHuase();
                continue;
            }

            if (mjiang.getHuase() != se) {
                qingyise = false;
                break;
            }
        }
        return qingyise;
    }

    /**
     * 是否清一色 平胡
     *
     * @param allMajiangs
     * @param majiangIds
     * @return
     */
    private static boolean isQingYiSeHu(YjMjHu hu, List<YjMj> allMajiangs, List<YjMj> majiangIds) {
        if (hu.isPingHu() || hu.isXiao7duiHu()) {
            return isqingyise(allMajiangs, majiangIds);
        }
        return false;
    }

    /**
     * 是否一条龙（手上的牌有同花色的1-9  并且除去1-9这9个牌后 还能胡牌即为一条龙）
     *
     * @param majiangIds
     * @return
     */
    private static boolean isYiTiaoLong(List<YjMj> majiangIds) {
        Map<Integer, List<YjMj>> longMaps = new HashMap<>();
        for (YjMj mjiang : majiangIds) {
            int huase = mjiang.getHuase();
            if (longMaps.containsKey(huase)) {
                longMaps.get(huase).add(mjiang);
            } else {
                List<YjMj> list = new ArrayList<>();
                list.add(mjiang);
                longMaps.put(huase, list);
            }
        }
        for (int huase : longMaps.keySet()) {
            if (longMaps.get(huase).size() >= 9) {// 同花色的牌超出9张
                Set<Integer> longSet = new HashSet<>();
                Set<YjMj> longMjSet = new HashSet<>();
                for (YjMj mjiang : longMaps.get(huase)) {
                    if (!longSet.contains(mjiang.getPai())) {
                        longSet.add(mjiang.getPai());// 有一条龙
                        longMjSet.add(mjiang);
                    }
                }
                if (longSet.size() >= 9) {
                    List<YjMj> copy = new ArrayList<>(majiangIds);
                    for (YjMj maJiang : longMjSet) {// 除去1-9这9个牌
                        copy.remove(maJiang);
                    }
                    boolean hu = YjMjTool.isPingHu(copy, false);
                    return hu;
                }
            }
        }
        return false;
    }

    /**
     * 是否门清 （门清必须是大门子不下牌  包括清一色门清和一条龙门清和门清将降胡） 7小队不算门清
     *
     * @param hu
     * @param majiangIds
     * @return
     */
    private static boolean isMenQing(YjMjHu hu, List<YjMj> majiangIds, List<YjMj> aGangs) {
        int allSize = majiangIds.size() + (aGangs.size()) / 4 * 3;
        if (allSize != 14) {
            return false;
        }

        if (hu.isQingyiseHu() || hu.isYiTiaoLong() || hu.isPengpengHu() || hu.isJiangjiangHu() || hu.isMaMaHu() && !(hu.isXiao7duiHu())) {// 包括门清清一色 门清一条龙 门清将降胡 门清碰碰胡
            return true;
        } else if (hu.isXiao7duiHu() && hu.isQingyiseHu()) {// 清一色的7小队算门清
            return true;
        } else if (hu.isXiao7duiHu() && hu.isJiangjiangHu()) {// 清一色的7小队算门清
            return true;
        } else if (hu.isXiao7duiHu() && hu.isMaMaHu()) {// 清一色的7小队算门清
            return true;
        }

        return false;
    }

    /**
     * 是否将将胡
     *
     * @param majiangIds
     * @return
     */
    private static boolean isJiangJiangHu(List<YjMj> majiangIds) {
        boolean jiangjianghu = true;
        for (YjMj mjiang : majiangIds) {
            if (!mjiang.isJiang() && !mjiang.isHongzhong()) {
                jiangjianghu = false;
                break;
            }
        }
        return jiangjianghu;
    }

    /**
     * 是否碰碰胡
     *
     * @param majiangIds
     * @param card_index
     * @return
     */
    private static boolean isPengPengHu(List<YjMj> majiangIds, YjMjIndexArr card_index) {
        YjMjIndex index4 = card_index.getMajiangIndex(3);
        YjMjIndex index3 = card_index.getMajiangIndex(2);
        YjMjIndex index2 = card_index.getMajiangIndex(1);
        YjMjIndex index1 = card_index.getMajiangIndex(0);
        int sameCount = 0;
        if (index4 != null) {
            sameCount += index4.getLength();
        }
        if (index3 != null) {
            sameCount += index3.getLength();
        }
        // 3个相同或者4个相同有4个
        if (sameCount == 4 && index2 != null && index2.getLength() == 1) {
            return true;
        } else if (majiangIds.contains(YjMj.getMajang(201)) && index4 != null && index4.getLength() == 2 && index1 != null && index1.getLength() == 1 && index2 == null) {
            return true;
        } else if (majiangIds.contains(YjMj.getMajang(201)) && sameCount >= 3 && index2 == null && index1 != null && index1.getLength() == 2) {
            return true;
        } else if (majiangIds.contains(YjMj.getMajang(201)) && sameCount >= 2 && index2 != null && index2.getLength() == 2 && index1 != null && index1.getLength() == 1) {
            return true;
        }
        return false;
    }

    /**
     * 是否码码胡
     *
     * @param mjs
     * @return
     */
    private static boolean isMaMaHu(List<YjMj> mjs) {
        boolean res = true;
        for (YjMj mj : mjs) {
            if (!mj.isMa() && !mj.isHongzhong()) {
                res = false;
                break;
            }
        }
        return res;
    }
}
