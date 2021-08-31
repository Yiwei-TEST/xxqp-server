package com.sy599.game.qipai.symj.tool;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.symj.bean.SyMjHu;
import com.sy599.game.qipai.symj.bean.SyMjPlayer;
import com.sy599.game.qipai.symj.bean.SyMjTable;
import com.sy599.game.qipai.symj.constant.SyMjConstants;
import com.sy599.game.qipai.symj.rule.SyMj;
import com.sy599.game.qipai.symj.rule.SyMjIndex;
import com.sy599.game.qipai.symj.rule.SyMjIndexArr;
import com.sy599.game.qipai.symj.tool.hulib.util.HuUtil;
import com.sy599.game.util.JacksonUtil;

/**
 *
 *
 */
public class SyMjTool {


    public static synchronized List<List<SyMj>> fapai(List<Integer> copy, List<List<Integer>> t) {
        List<List<SyMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<SyMj> pai = new ArrayList<>();
        int j = 1;

        int testcount = 0;
        if (GameServerConfig.isDebug() && t != null && !t.isEmpty()) {
            for (List<Integer> zp : t) {
                list.add(SyMjHelper.find(copy, zp));
                testcount += zp.size();
            }

            if (list.size() == 4) {
                list.add(SyMjHelper.toMajiang(copy));
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
            SyMj majiang = SyMj.getMajang(copy.get(i));
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
        List<SyMj> left = new ArrayList<>();
        for (int i = 0; i < copy2.size(); i++) {
            left.add(SyMj.getMajang(copy2.get((i))));
        }
        list.add(left);
        return list;
    }

    public static synchronized List<List<SyMj>> fapai(List<Integer> copy, int playerCount) {
        List<List<SyMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<SyMj> allMjs = new ArrayList<>();
        for (int id : copy) {
            allMjs.add(SyMj.getMajang(id));
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

    public static synchronized List<List<SyMj>> fapai(List<Integer> copy, int playerCount, List<List<Integer>> t) {
        List<List<SyMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<List<SyMj>> zpList = new ArrayList<>();
        if (GameServerConfig.isDebug() && t != null && !t.isEmpty()) {
            for (List<Integer> zp : t) {
                zpList.add(SyMjHelper.find(copy, zp));
            }
        }
        List<SyMj> allMjs = new ArrayList<>();
        for (int id : copy) {
            allMjs.add(SyMj.getMajang(id));
        }
        int count = 0;
        for (int i = 0; i < playerCount; i++) {
            if (i == 0) {
                if (zpList.size() > 0) {
                    List<SyMj> pai = zpList.get(0);
                    int len = 14 - pai.size();
                    pai.addAll(allMjs.subList(count, len));
                    count += len;
                    list.add(new ArrayList<>(pai));
                } else {
                    list.add(new ArrayList<>(allMjs.subList(0, 14)));
                }
            } else {
                if (zpList.size() > i) {
                    List<SyMj> pai = zpList.get(i);
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
                    List<SyMj> pai = zpList.get(i + 1);
                    pai.addAll(allMjs.subList(count, allMjs.size()));
                    list.add(new ArrayList<>(pai));
                } else {
                    list.add(new ArrayList<>(allMjs.subList(count, allMjs.size())));
                }
            }

        }
        return list;
    }

    public static synchronized List<List<SyMj>> fapai(List<Integer> copy) {
        List<List<SyMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<SyMj> pai = new ArrayList<>();
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
            SyMj majiang = SyMj.getMajang(copy.get(i));
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
        List<SyMj> left = new ArrayList<>();
        for (int i = 0; i < copy2.size(); i++) {
            left.add(SyMj.getMajang(copy2.get((i))));
        }
        list.add(left);
        return list;
    }

    /**
     * 麻将胡牌
     *
     * @return
     */
//	public static boolean isHu(List<SyMj> majiangIds) {
//		if (majiangIds == null || majiangIds.isEmpty()) {
//			return false;
//		}
//		List<SyMj> copy = new ArrayList<>(majiangIds);
//		if (majiangIds.size() % 3 != 2) {
//			return false;
//		}
//		SyMjIndexArr card_index = new SyMjIndexArr();
//		SyMjQipaiTool.getMax(card_index, copy);
//		if (check7duizi(copy, card_index, 0)) {
//			return true;
//		}
//		// 拆将
//		if (chaijiang(card_index, copy, 0, false)) {
//			return true;
//		} else {
//			return false;
//		}
//	}
    public static boolean isTing(SyMjPlayer player) {
        List<SyMj> majiangIds = new ArrayList<>(player.getHandMajiang());
        if (majiangIds.size() % 3 != 1) {
            return false;
        }
        SyMjTable table = player.getPlayingTable(SyMjTable.class);
        if (table != null && table.getKeChi() == 2 && !player.getChi().isEmpty()) {
            List<SyMj> allMajiangs = new ArrayList<>();
            allMajiangs.addAll(majiangIds);
            allMajiangs.addAll(player.getaGang());
            allMajiangs.addAll(player.getmGang());
            allMajiangs.addAll(player.getPeng());
            allMajiangs.addAll(player.getChi());
            if (!isQingYiSe(allMajiangs)) {
                return false;
            }
        }
        SyMjIndexArr card_index = new SyMjIndexArr();
        SyMjQipaiTool.getMax(card_index, majiangIds);
        if (card_index.getDuiziNum() == 6) {
            return true;
        }
        if (majiangIds.size() == 13) {
            List<Integer> shiSanYao = Arrays.asList(11, 19, 21, 29, 31, 39, 301, 311, 321, 331, 201, 211, 221);
            List<Integer> valList = new ArrayList<>(shiSanYao);
            List<SyMj> copy = new ArrayList<>(majiangIds);
            Iterator<SyMj> iterator = copy.iterator();
            while (iterator.hasNext()) {
                SyMj majiang = iterator.next();
                if (valList.contains(majiang.getVal())) {
                    iterator.remove();
                    valList.remove(new Integer(majiang.getVal()));
                }
            }
            if (copy.size() == 0 || (copy.size() == 1 && shiSanYao.contains(copy.get(0).getVal()))) {
                return true;
            }
        }
        // 拆将
        if (chaijiang(card_index, majiangIds, 1, false)) {
            return true;
        }
        return false;
    }

    public static SyMjHu isHu(SyMjPlayer player) {
        return isHu(player, null);
    }

    public static SyMjHu isHu(SyMjPlayer player, SyMj disMajiang) {
        SyMjHu hu = new SyMjHu();
        hu.setNeedJiang258(false);
        List<SyMj> copy = new ArrayList<>(player.getHandMajiang());
        if (disMajiang != null) {
            copy.add(disMajiang);
        } else {
            hu.setZiMo(true);
        }
        if (isPingHu(copy, hu.isNeedJiang258())) {
            hu.setHu(true);
            hu.setPingHu(true);
        }
        daHu(hu, player, copy);
        if (!hu.getDaHuList().isEmpty()) {
            hu.setHu(true);
            hu.setPingHu(false);
        }
        if (hu.isHu()) {
            SyMjTable table = player.getPlayingTable(SyMjTable.class);
            if (table != null && table.getKeChi() == 2 && !player.getChi().isEmpty() && !hu.isDaHu(SyMjConstants.HU_QINGYISE)) {
                hu.setHu(false);
            }
        }
        return hu;
    }

    public static boolean isPingHu(List<SyMj> majiangIds) {
        return isPingHu(majiangIds, true);
    }

    public static boolean isPingHu(List<SyMj> majiangIds, boolean needJiang258) {
        if (majiangIds == null || majiangIds.isEmpty()) {
            return false;
        }
        if (majiangIds.size() % 3 != 2) {
            return false;
        }
        List<SyMj> copy = new ArrayList<>(majiangIds);

        SyMjIndexArr card_index = new SyMjIndexArr();
        SyMjQipaiTool.getMax(card_index, copy);
        return HuUtil.isCanHu(new ArrayList<>(copy), 0);
    }


    public static void daHu(SyMjHu hu, SyMjPlayer player, List<SyMj> majiangIds) {
        if (majiangIds == null || majiangIds.isEmpty()) {
            return;
        }
        if (majiangIds.size() % 3 != 2) {
            return;
        }
        List<SyMj> copy = new ArrayList<>(majiangIds);
        SyMjIndexArr card_index = new SyMjIndexArr();
        SyMjQipaiTool.getMax(card_index, copy);

        List<SyMj> allMajiangs = new ArrayList<>();
        allMajiangs.addAll(copy);
        allMajiangs.addAll(player.getaGang());
        allMajiangs.addAll(player.getmGang());
        allMajiangs.addAll(player.getPeng());
        allMajiangs.addAll(player.getChi());
        SyMjIndexArr all_card_index = new SyMjIndexArr();
        SyMjQipaiTool.getMax(all_card_index, allMajiangs);

        List<Integer> daHuList = new ArrayList<>();
        if (isMenQing(hu, copy, player.getaGang())) {
            daHuList.add(SyMjConstants.HU_MENQING);
        }
        int duiziNum = card_index.getDuiziNum();
        if (duiziNum == 7) {
            SyMjIndex index = card_index.getMajiangIndex(3);
            if (index != null) {// 有4个一样的牌
                daHuList.add(SyMjConstants.HU_LONGQIDUI);
            } else {// 普通7小对
                daHuList.add(SyMjConstants.HU_QIXIAODUI);
            }
        }
        if (player.getChi().isEmpty() && isPengPengHu(all_card_index)) {
            daHuList.add(SyMjConstants.HU_DADUIPENG);
        }
        if (isQingYiSeHu(hu, daHuList, allMajiangs, copy)) {
            daHuList.add(SyMjConstants.HU_QINGYISE);
        }
        if (isFengYiSeHu(daHuList, allMajiangs)) {
            daHuList.add(SyMjConstants.HU_FENGYISE);
        }
        if (isShiSanYaoHu(majiangIds)) {
            daHuList.add(SyMjConstants.HU_SHISANYAO);
        }
        hu.setDaHuList(daHuList);
    }

    /**
     * 红中麻将没有7小对，所以不用红中补
     *
     * @param majiangIds
     * @param card_index
     */
    public static boolean check7duizi(List<SyMj> majiangIds, SyMjIndexArr card_index, int hongzhongNum) {
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

            SyMjIndex index0 = card_index.getMajiangIndex(0);
            SyMjIndex index2 = card_index.getMajiangIndex(2);
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
    public static boolean chaijiang(SyMjIndexArr card_index, List<SyMj> hasPais, int hongzhongnum, boolean needJiang258) {
        Map<Integer, List<SyMj>> jiangMap = card_index.getJiang(needJiang258);
        for (Entry<Integer, List<SyMj>> valEntry : jiangMap.entrySet()) {
            List<SyMj> copy = new ArrayList<>(hasPais);
            SyMjHuLack lack = new SyMjHuLack(hongzhongnum);
            List<SyMj> list = valEntry.getValue();
            int i = 0;
            for (SyMj majiang : list) {
                i++;
                copy.remove(majiang);
                if (i >= 2) {
                    break;
                }
            }
            lack.setHasJiang(true);
            boolean hu = chaipai(lack, copy, needJiang258);
            if (hu) {
                System.out.println(JacksonUtil.writeValueAsString(lack));
                return hu;
            }
        }

        if (hongzhongnum > 0) {
            // 只剩下红中
            if (hasPais.isEmpty()) {
                return true;
            }
            // 没有将
            for (SyMj majiang : hasPais) {
                List<SyMj> copy = new ArrayList<>(hasPais);
                SyMjHuLack lack = new SyMjHuLack(hongzhongnum);
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
                    System.out.println(JacksonUtil.writeValueAsString(lack));
                    return true;
                }
                if (!lack.isHasJiang() && hu) {
                    if (lack.getHongzhongNum() == 2) {
                        // 红中做将
                        System.out.println(JacksonUtil.writeValueAsString(lack));
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // 拆牌
    public static boolean chaipai(SyMjHuLack lack, List<SyMj> hasPais, boolean isNeedJiang258) {
        if (hasPais.isEmpty()) {
            return true;

        }
        boolean hu = chaishun(lack, hasPais, isNeedJiang258);
        if (hu)
            return true;
        return false;
    }

    public static void sortMin(List<SyMj> hasPais) {
        Collections.sort(hasPais, new Comparator<SyMj>() {

            @Override
            public int compare(SyMj o1, SyMj o2) {
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
    public static boolean chaishun(SyMjHuLack lack, List<SyMj> hasPais, boolean needJiang258) {
        if (hasPais.isEmpty()) {
            return true;
        }
        sortMin(hasPais);
        SyMj minMajiang = hasPais.get(0);
        int minVal = minMajiang.getVal();
        List<SyMj> minList = SyMjQipaiTool.getVal(hasPais, minVal);
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
        List<SyMj> num1 = SyMjQipaiTool.getVal(hasPais, pai1);
        List<SyMj> num2 = SyMjQipaiTool.getVal(hasPais, pai2);
        List<SyMj> num3 = SyMjQipaiTool.getVal(hasPais, pai3);

        // 找到一句话的麻将
        List<SyMj> hasMajiangList = new ArrayList<>();
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
                List<SyMj> count = SyMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
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

                List<SyMj> count = SyMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
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
                List<SyMj> count1 = SyMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                List<SyMj> count2 = SyMjQipaiTool.getVal(hasPais, hasMajiangList.get(1).getVal());
                List<SyMj> count3 = SyMjQipaiTool.getVal(hasPais, hasMajiangList.get(2).getVal());
                if (count1.size() >= 2 && (count2.size() == 1 || count3.size() == 1)) {
                    List<SyMj> copy = new ArrayList<>(hasPais);
                    removeAllPai(copy, count1);
                    //copy.removeAll(count1);
                    SyMjHuLack copyLack = lack.copy();
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

    public static void removeAllPai(List<SyMj> hasPais, List<SyMj> remPai) {
        for (SyMj majiang : remPai) {
            hasPais.remove(majiang);
        }
    }

    public static boolean isCanAsJiang(SyMj majiang, boolean isNeed258) {
        if (isNeed258) {
            if (majiang.getPai() == 2 || majiang.getPai() == 5 || majiang.getPai() == 8) {
                return true;
            }
            return false;
        } else {
            return true;
        }

    }

    public static List<SyMj> checkChi(List<SyMj> majiangs, SyMj dismajiang) {
        return checkChi(majiangs, dismajiang, null);
    }

    /**
     * 是否能吃
     *
     * @param majiangs
     * @param dismajiang
     * @return
     */
    public static List<SyMj> checkChi(List<SyMj> majiangs, SyMj dismajiang, List<Integer> wangValList) {
        int disMajiangVal = dismajiang.getVal();
        List<Integer> chi1 = new ArrayList<>(Arrays.asList(disMajiangVal - 2, disMajiangVal - 1));
        List<Integer> chi2 = new ArrayList<>(Arrays.asList(disMajiangVal - 1, disMajiangVal + 1));
        List<Integer> chi3 = new ArrayList<>(Arrays.asList(disMajiangVal + 1, disMajiangVal + 2));

        List<Integer> majiangIds = SyMjHelper.toMajiangVals(majiangs);
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
        return new ArrayList<SyMj>();
    }

    public static List<SyMj> findMajiangByVals(List<SyMj> majiangs, List<Integer> vals) {
        List<SyMj> result = new ArrayList<>();
        for (int val : vals) {
            for (SyMj majiang : majiangs) {
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
    public static List<SyMj> dropHongzhong(List<SyMj> copy) {
        List<SyMj> hongzhong = new ArrayList<>();
//        Iterator<SyMj> iterator = copy.iterator();
//        while (iterator.hasNext()) {
//            SyMj majiang = iterator.next();
//            if (majiang.getVal() == 201) {
//                hongzhong.add(majiang);
//                iterator.remove();
//            }
//        }
        return hongzhong;
    }

    public static boolean checkWang(Object majiangs, List<Integer> wangValList) {
        if (majiangs instanceof List) {
            List list = (List) majiangs;
            for (Object majiang : list) {
                int val = 0;
                if (majiang instanceof SyMj) {
                    val = ((SyMj) majiang).getVal();
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
    public static List<SyMj> getSameMajiang(List<SyMj> majiangs, SyMj majiang, int num) {
        List<SyMj> hongzhong = new ArrayList<>();
        int i = 0;
        for (SyMj maji : majiangs) {
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
    public static List<SyMj> dropMjId(List<SyMj> copy, int id) {
        List<SyMj> hongzhong = new ArrayList<>();
        Iterator<SyMj> iterator = copy.iterator();
        while (iterator.hasNext()) {
            SyMj majiang = iterator.next();
            if (majiang.getId() == id) {
                hongzhong.add(majiang);
                iterator.remove();
            }
        }
        return hongzhong;
    }

    private static boolean isMenQing(SyMjHu hu, List<SyMj> majiangIds, List<SyMj> aGangs) {
        int allSize = majiangIds.size() + (aGangs.size()) / 4 * 3;
        if (allSize == 14 && hu.isPingHu()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否是清一色
     *
     * @param allMajiangs
     * @return
     */
    private static boolean isQingYiSe(List<SyMj> allMajiangs) {
        boolean qingyise = false;
        int se = 0;
        for (SyMj mjiang : allMajiangs) {
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

    private static boolean isQingYiSeHu(SyMjHu hu, List<Integer> daHuList, List<SyMj> allMajiangs, List<SyMj> majiangIds) {
        if (isQingYiSe(allMajiangs)) {
            if (daHuList.contains(SyMjConstants.HU_LONGQIDUI)
                    || daHuList.contains(SyMjConstants.HU_QIXIAODUI)
                    || daHuList.contains(SyMjConstants.HU_DADUIPENG)) {
                return true;
            }
            if (hu.isNeedJiang258()) {
                return isPingHu(majiangIds, false);
            } else {
                return hu.isPingHu();
            }
        }
        return false;
    }

    private static boolean isPengPengHu(SyMjIndexArr all_card_index) {
        SyMjIndex index4 = all_card_index.getMajiangIndex(3);
        SyMjIndex index3 = all_card_index.getMajiangIndex(2);
        SyMjIndex index2 = all_card_index.getMajiangIndex(1);
        int sameCount = 0;
        if (index4 != null) {
            sameCount += index4.getLength();
        }
        if (index3 != null) {
            sameCount += index3.getLength();
        }
        if (sameCount == 4 && index2 != null && index2.getLength() == 1) {
            return true;
        }
        return false;
    }

    private static boolean isFengYiSeHu(List<Integer> daHuList, List<SyMj> allMajiangs) {
        boolean fengyise = false;
        for (SyMj mjiang : allMajiangs) {
            if (mjiang.isZhongFaBai() || mjiang.isFeng()) {
                fengyise = true;
            } else {
                fengyise = false;
                break;
            }
        }
        return fengyise && daHuList.contains(SyMjConstants.HU_DADUIPENG);
    }

    public static boolean isShiSanYaoHu(List<SyMj> majiangIds) {
        if (majiangIds.size() != 14) {
            return false;
        }
        List<Integer> shiSanYao = Arrays.asList(11, 19, 21, 29, 31, 39, 301, 311, 321, 331, 201, 211, 221);
        List<Integer> valList = new ArrayList<>(shiSanYao);

        List<SyMj> copy = new ArrayList<>(majiangIds);
        Iterator<SyMj> iterator = copy.iterator();
        while (iterator.hasNext()) {
            SyMj majiang = iterator.next();
            if (valList.contains(majiang.getVal())) {
                iterator.remove();
                valList.remove(new Integer(majiang.getVal()));
            }
        }
        if (copy.size() == 1 && shiSanYao.contains(copy.get(0).getVal())) {
            return true;
        }
        return false;
    }

    public static int calcDaHuPoint(List<Integer> daHuList) {
        if (daHuList != null && !daHuList.isEmpty()) {
            if (daHuList.contains(SyMjConstants.HU_QINGYISE) && daHuList.contains(SyMjConstants.HU_LONGQIDUI)) {
                return 32;
            }
            if (daHuList.contains(SyMjConstants.HU_FENGYISE) || daHuList.contains(SyMjConstants.HU_SHISANYAO)) {
                return 24;
            }
            if (daHuList.contains(SyMjConstants.HU_QINGYISE)
                    && (daHuList.contains(SyMjConstants.HU_QIXIAODUI)
                    || daHuList.contains(SyMjConstants.HU_DADUIPENG)
                    || daHuList.contains(SyMjConstants.HU_MENQING))) {
                return 16;
            }
            if (daHuList.contains(SyMjConstants.HU_LONGQIDUI)) {
                return 16;
            }
            if (daHuList.contains(SyMjConstants.HU_QINGYISE)
                    || daHuList.contains(SyMjConstants.HU_QIXIAODUI)
                    || daHuList.contains(SyMjConstants.HU_DADUIPENG)) {
                return 8;
            }
            if (daHuList.contains(SyMjConstants.HU_MENQING)) {
                return 4;
            }
        }
        return 0;
    }

    public static List<SyMj> getPais(String paisStr) {
        String[] pais = paisStr.split(",");
        List<SyMj> handPais = new ArrayList<>();
        for (String pai : pais) {
            for (SyMj mj : SyMj.values()) {
                if (mj.getVal() == Integer.valueOf(pai) && !handPais.contains(mj)) {
                    handPais.add(mj);
                    break;
                }
            }
        }
        return handPais;
    }

    /**
     * 获取听牌列表
     * cards.size+hzCount = 3n+1
     *
     * @param cards   去掉红中的牌
     * @param hzCount 红中数
     * @param hu7dui  是否可胡七对
     * @return
     */
    public static List<SyMj> getLackList(List<SyMj> cards, int hzCount, boolean hu7dui) {
        if ((cards.size() + hzCount) % 3 != 1) {
            return Collections.emptyList();
        }
        List<SyMj> lackPaiList = HuUtil.getLackPaiList(cards, hzCount);
        if (lackPaiList.size() == 1 && null == lackPaiList.get(0)) {
            // 听所有
            return lackPaiList;
        }
        if (hu7dui && cards.size() + hzCount + 1 == 14) {
            //检查七小对
            if (SyMjTool.isHu7Dui(cards, hzCount + 1)) {
                int rmIndex = cards.size();
                for (SyMj mj : SyMj.fullMj) {
                    if (mj.isHongzhong()) {
                        continue;
                    }
                    cards.add(mj);
                    if (SyMjTool.isHu7Dui(cards, hzCount)) {
                        lackPaiList.add(mj);
                    }
                    cards.remove(rmIndex);
                }
            }
        }

        if (lackPaiList.size() > 0) {
            Iterator<SyMj> iterator = lackPaiList.iterator();
            Set<Integer> have = new HashSet<>();
            int lastIndex = cards.size();
            SyMj mj;
            while (iterator.hasNext()) {
                mj = iterator.next();
                // 去重
                if (have.contains(mj.getVal())) {
                    iterator.remove();
                } else {
                    have.add(mj.getVal());

                    //检验
                    cards.add(mj);
                    if (!HuUtil.isCanHu(cards, hzCount)) {
                        iterator.remove();
                    }
                    cards.remove(lastIndex);
                }
            }
            if (lackPaiList.size() == 27) {
                lackPaiList.clear();
                lackPaiList.add(null);
            }
        }
        return lackPaiList;
    }

    /**
     * 获取听牌列表
     * cards.size+hzCount = 3n+1
     *
     * @param cardArr 去掉红中的牌
     * @param hzCount 红中数
     * @param hu7dui  是否可胡七对
     * @return
     */
    public static List<SyMj> getLackList(int[] cardArr, int hzCount, boolean hu7dui,SyMjPlayer player, SyMjTable table,List<SyMj> allMajiangs) {
        int cardNum = 0;
        for (int i = 0, length = cardArr.length; i < length; i++) {
            cardNum += cardArr[i];
        }
        if ((cardNum + hzCount) % 3 != 1) {
            return Collections.emptyList();
        }
        List<SyMj> lackPaiList = new ArrayList<>();
        Set<Integer> have = new HashSet<>();
        for (SyMj mj : SyMj.fullMj) {
            if (mj.isHongzhong() || have.contains(mj.getVal())) {
                continue;
            }
        	if(table.getKeChi()==2&&player.getChi().size()>0 &&  player.getChi().get(0).getColourVal()!= mj.getColourVal()) {
				continue;
			}
            int cardIndex = HuUtil.getMjIndex(mj);
            cardArr[cardIndex] = cardArr[cardIndex] + 1;
            if (hu7dui && HuUtil.isCanHu7Dui(cardArr, hzCount)) {
                lackPaiList.add(mj);
                have.add(mj.getVal());
            }
            if (HuUtil.isCanHu(cardArr, hzCount)) {
            	if(!keChicanHu(player, table,allMajiangs)){
            		continue;
            	}
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
    
    
    public static boolean keChicanHu(SyMjPlayer player, SyMjTable table,List<SyMj> allMajiangs) {
    	
    	if(table.getKeChi()==2&&player.getChi().size()>0 && (!isQingYiSe(allMajiangs)|| !isQingYiSe(player.getChi()))) {
			return false;
		}
    	return true;
    	
    }
    
    
    
    /**
     * 是否胡七对
     *
     * @param mjs     去掉红中的麻将
     * @param hzCount
     * @return
     */
    public static boolean isHu7Dui(List<SyMj> mjs, int hzCount) {
        SyMjIndexArr card_index = new SyMjIndexArr();
        SyMjQipaiTool.getMax(card_index, mjs);
        return check7duizi(mjs, card_index, hzCount);
    }


    public static void main(String[] args) {
        HuUtil.init();
        int laiZiVal = 331;
        int laiZiNum = 0;
        String paisStr = "331,331,331,11,12,13,21,22,23,31,32,33,39,39";
        paisStr = "33,33,33,36,36,36,38,38,39,39";
        paisStr = "37,37,15,16,17,24,24,25,25";
        paisStr = "37,38,39,11,11,11,15,16,17,17,18,19,26";
        List<SyMj> handPais = getPais(paisStr);
        System.out.println(handPais);
        int count = 1;
        long start = Clock.systemDefaultZone().millis();
        SyMjPlayer player = new SyMjPlayer();
        player.getHandMajiang().addAll(handPais);
        SyMjHu hu = null;
        for (int i = 0; i < count; i++) {
            hu = isHu(player, null);
        }
        long timeUse = Clock.systemDefaultZone().millis() - start;
        System.out.println("canHu = " + JSON.toJSONString(hu) + " , count = " + count + " , timeUse = " + timeUse + " ms");

        boolean canHu = false;
        canHu = false;
        start = Clock.systemDefaultZone().millis();
        for (int i = 0; i < count; i++) {
            canHu = HuUtil.isCanHu(handPais, laiZiNum);
        }
        timeUse = Clock.systemDefaultZone().millis() - start;
        System.out.println("canHu = " + canHu + " , count = " + count + " , timeUse = " + timeUse + " ms");

        List<SyMj> lackPaiList = HuUtil.getLackPaiList(handPais, laiZiNum);
        Iterator<SyMj> iterator = lackPaiList.iterator();
        while (iterator.hasNext()) {
            SyMj mj = iterator.next();
            handPais.add(mj);
            if (!HuUtil.isCanHu(handPais, laiZiNum)) {
                iterator.remove();
            }
            handPais.remove(handPais.size() - 1);
        }
        System.out.println("tingList : " + lackPaiList);

    }

}
