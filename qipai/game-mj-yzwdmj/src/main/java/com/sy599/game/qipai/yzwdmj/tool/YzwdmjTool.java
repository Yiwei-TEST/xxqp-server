package com.sy599.game.qipai.yzwdmj.tool;

import java.time.Clock;
import java.util.*;
import java.util.Map.Entry;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.yzwdmj.rule.Yzwdmj;
import com.sy599.game.qipai.yzwdmj.rule.YzwdmjIndex;
import com.sy599.game.qipai.yzwdmj.rule.YzwdmjIndexArr;
import com.sy599.game.qipai.yzwdmj.tool.hulib.util.HuUtil;
import com.sy599.game.util.JacksonUtil;

/**
 * @author lc
 */
public class YzwdmjTool {

    /**
     * 获取听牌列表
     * cards.size+hzCount = 3n+1
     *
     * @param cardArr 去掉红中的牌
     * @param hzCount 红中数
     * @param hu7dui  是否可胡七对
     * @return
     */
    public static List<Yzwdmj> getLackList(int[] cardArr, int hzCount, boolean hu7dui) {
        int cardNum = 0;
        for (int i = 0, length = cardArr.length; i < length; i++) {
            cardNum += cardArr[i];
        }
        if ((cardNum + hzCount) % 3 != 1) {
            return Collections.emptyList();
        }
        List<Yzwdmj> lackPaiList = new ArrayList<>();
        Set<Integer> have = new HashSet<>();
        for (Yzwdmj mj : Yzwdmj.fullMj) {
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


    public static synchronized List<List<Yzwdmj>> fapai(List<Integer> copy, int playerCount) {
        List<List<Yzwdmj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<Yzwdmj> allMjs = new ArrayList<>();
        for (int id : copy) {
            allMjs.add(Yzwdmj.getMajang(id));
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

    public static synchronized List<List<Yzwdmj>> fapai(List<Integer> copy, List<List<Integer>> t, int playerCount) {
        if(t.size()<playerCount)
            return null;
        List<List<Yzwdmj>> list = new ArrayList<>();
        for (int i = 0; i < t.size(); i++) {
            List<Integer> zp = t.get(i);
            //跳过调的王牌
//            if(i==0&&zp.size()==1)
//                continue;
            list.add(find(copy, zp));
        }
        Collections.shuffle(copy);
        boolean buShouPai=false;
        if(buShouPai){
            //补张
            for (int i = 0; i < playerCount; i++) {
                List<Yzwdmj> ahmjs = list.get(i);
                if(i==0){
                    for (int j = ahmjs.size(); j < 14; j++) {
                        ahmjs.add(Yzwdmj.getMajang(copy.get(j)));
                        copy.remove(j);
                    }
                }else {
                    for (int j = ahmjs.size(); j < 13; j++) {
                        ahmjs.add(Yzwdmj.getMajang(copy.get(j)));
                        copy.remove(j);
                    }
                }
            }
        }
        boolean buMoPai=false;
        if(list.size()>playerCount&&buMoPai){
            list.get(playerCount).addAll(YzwdmjHelper.toMajiang(copy));
        }else if(list.size()==playerCount){
            List<Yzwdmj> l=new ArrayList<>();
            for (int i = 0; i < copy.size(); i++) {
                l.add(Yzwdmj.getMajang(copy.get(i)));
            }
            list.add(l);
        }

        return list;
    }


    private static List<Yzwdmj> find(List<Integer> copy, List<Integer> valList) {
        List<Yzwdmj> pai = new ArrayList<>();
        if (!valList.isEmpty()) {
            for (int zpId : valList) {
                Iterator<Integer> iterator = copy.iterator();
                while (iterator.hasNext()) {
                    int card = iterator.next();
                    Yzwdmj mj = Yzwdmj.getMajang(card);
                    if (mj.getVal() == zpId) {
                        pai.add(mj);
                        iterator.remove();
                        break;
                    }
                }
            }
        }
        return pai;
    }



    public static boolean isPingHu(List<Yzwdmj> majiangIds) {
        return isPingHu(majiangIds, true);

    }

    public static boolean isPingHu(List<Yzwdmj> majiangIds, boolean needJiang258) {
        if (majiangIds == null || majiangIds.isEmpty()) {
            return false;
        }
        if (majiangIds.size() % 3 != 2) {
            System.out.println("%3！=2");
            return false;

        }

        // 先去掉红中
        List<Yzwdmj> copy = new ArrayList<>(majiangIds);
        List<Yzwdmj> hongzhongList = dropHongzhong(copy);

        YzwdmjIndexArr card_index = new YzwdmjIndexArr();
        YzwdmjQipaiTool.getMax(card_index, copy);
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
    public static boolean isHu(List<Yzwdmj> mjs, boolean hu7dui) {
        if (mjs == null || mjs.isEmpty()) {
            return false;
        }
        List<Yzwdmj> copy = new ArrayList<>(mjs);
//         先去掉红中
		List<Yzwdmj> hongzhongList = dropHongzhong(copy);
        if (mjs.size() % 3 != 2) {
            return false;
        }
        YzwdmjIndexArr card_index = new YzwdmjIndexArr();
        YzwdmjQipaiTool.getMax(card_index, copy);
        if (hu7dui && check7duizi(copy, card_index, hongzhongList.size())) {
            return true;
        }

        return HuUtil.isCanHu(new ArrayList<>(copy),hongzhongList.size());
        // 拆将
//        if (chaijiang(card_index, copy, 0, false)) {
//            return true;
//        } else {
//            return false;
//        }
    }

    public static boolean isTing(List<Yzwdmj> majiangIds, boolean hu7dui) {
        if (majiangIds == null || majiangIds.isEmpty()) {
            return false;
        }
        List<Yzwdmj> copy = new ArrayList<>(majiangIds);
        if (majiangIds.size() % 3 != 1) {
            return false;
        }
        YzwdmjIndexArr card_index = new YzwdmjIndexArr();
        YzwdmjQipaiTool.getMax(card_index, copy);
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
    public static boolean check7duizi(List<Yzwdmj> majiangIds, YzwdmjIndexArr card_index, int hongzhongNum) {
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

            YzwdmjIndex index0 = card_index.getMajiangIndex(0);
            YzwdmjIndex index2 = card_index.getMajiangIndex(2);
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
    public static boolean chaijiang(YzwdmjIndexArr card_index, List<Yzwdmj> hasPais, int hongzhongnum, boolean needJiang258) {
        Map<Integer, List<Yzwdmj>> jiangMap = card_index.getJiang(needJiang258);
        for (Entry<Integer, List<Yzwdmj>> valEntry : jiangMap.entrySet()) {
            List<Yzwdmj> copy = new ArrayList<>(hasPais);
            YzwdmjHuLack lack = new YzwdmjHuLack(hongzhongnum);
            List<Yzwdmj> list = valEntry.getValue();
            int i = 0;
            for (Yzwdmj majiang : list) {
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
            for (Yzwdmj majiang : hasPais) {
                List<Yzwdmj> copy = new ArrayList<>(hasPais);
                YzwdmjHuLack lack = new YzwdmjHuLack(hongzhongnum);
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
    public static boolean chaipai(YzwdmjHuLack lack, List<Yzwdmj> hasPais, boolean isNeedJiang258) {
        if (hasPais.isEmpty()) {
            return true;

        }
        boolean hu = chaishun(lack, hasPais, isNeedJiang258);
        if (hu)
            return true;
        return false;
    }

    public static void sortMin(List<Yzwdmj> hasPais) {
        Collections.sort(hasPais, new Comparator<Yzwdmj>() {

            @Override
            public int compare(Yzwdmj o1, Yzwdmj o2) {
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
    public static boolean chaishun(YzwdmjHuLack lack, List<Yzwdmj> hasPais, boolean needJiang258) {
        if (hasPais.isEmpty()) {
            return true;
        }
        sortMin(hasPais);
        Yzwdmj minMajiang = hasPais.get(0);
        int minVal = minMajiang.getVal();
        List<Yzwdmj> minList = YzwdmjQipaiTool.getVal(hasPais, minVal);
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
        List<Yzwdmj> num1 = YzwdmjQipaiTool.getVal(hasPais, pai1);
        List<Yzwdmj> num2 = YzwdmjQipaiTool.getVal(hasPais, pai2);
        List<Yzwdmj> num3 = YzwdmjQipaiTool.getVal(hasPais, pai3);

        // 找到一句话的麻将
        List<Yzwdmj> hasMajiangList = new ArrayList<>();
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
                List<Yzwdmj> count = YzwdmjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
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

                List<Yzwdmj> count = YzwdmjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
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
                List<Yzwdmj> count1 = YzwdmjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                List<Yzwdmj> count2 = YzwdmjQipaiTool.getVal(hasPais, hasMajiangList.get(1).getVal());
                List<Yzwdmj> count3 = YzwdmjQipaiTool.getVal(hasPais, hasMajiangList.get(2).getVal());
                if (count1.size() >= 2 && (count2.size() == 1 || count3.size() == 1)) {
                    List<Yzwdmj> copy = new ArrayList<>(hasPais);
                    removeAllPai(copy, count1);
                    //copy.removeAll(count1);
                    YzwdmjHuLack copyLack = lack.copy();
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

    public static void removeAllPai(List<Yzwdmj> hasPais, List<Yzwdmj> remPai) {
        for (Yzwdmj majiang : remPai) {
            hasPais.remove(majiang);
        }
    }

    public static boolean isCanAsJiang(Yzwdmj majiang, boolean isNeed258) {
        if (isNeed258) {
            if (majiang.getPai() == 2 || majiang.getPai() == 5 || majiang.getPai() == 8) {
                return true;
            }
            return false;
        } else {
            return true;
        }

    }

    public static List<Yzwdmj> checkChi(List<Yzwdmj> majiangs, Yzwdmj dismajiang) {
        return checkChi(majiangs, dismajiang, null);
    }

    /**
     * 是否能吃
     *
     * @param majiangs
     * @param dismajiang
     * @return
     */
    public static List<Yzwdmj> checkChi(List<Yzwdmj> majiangs, Yzwdmj dismajiang, List<Integer> wangValList) {
        int disMajiangVal = dismajiang.getVal();
        List<Integer> chi1 = new ArrayList<>(Arrays.asList(disMajiangVal - 2, disMajiangVal - 1));
        List<Integer> chi2 = new ArrayList<>(Arrays.asList(disMajiangVal - 1, disMajiangVal + 1));
        List<Integer> chi3 = new ArrayList<>(Arrays.asList(disMajiangVal + 1, disMajiangVal + 2));

        List<Integer> majiangIds = YzwdmjHelper.toMajiangVals(majiangs);
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
        return new ArrayList<Yzwdmj>();
    }

    public static List<Yzwdmj> findMajiangByVals(List<Yzwdmj> majiangs, List<Integer> vals) {
        List<Yzwdmj> result = new ArrayList<>();
        for (int val : vals) {
            for (Yzwdmj majiang : majiangs) {
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
    public static List<Yzwdmj> dropHongzhong(List<Yzwdmj> copy) {
        List<Yzwdmj> hongzhong = new ArrayList<>();
        Iterator<Yzwdmj> iterator = copy.iterator();
        while (iterator.hasNext()) {
            Yzwdmj majiang = iterator.next();
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
                if (majiang instanceof Yzwdmj) {
                    val = ((Yzwdmj) majiang).getVal();
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
    public static List<Yzwdmj> getSameMajiang(List<Yzwdmj> majiangs, Yzwdmj majiang, int num) {
        List<Yzwdmj> hongzhong = new ArrayList<>();
        int i = 0;
        for (Yzwdmj maji : majiangs) {
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
    public static List<Yzwdmj> dropMjId(List<Yzwdmj> copy, int id) {
        List<Yzwdmj> hongzhong = new ArrayList<>();
        Iterator<Yzwdmj> iterator = copy.iterator();
        while (iterator.hasNext()) {
            Yzwdmj majiang = iterator.next();
            if (majiang.getId() == id) {
                hongzhong.add(majiang);
                iterator.remove();
            }
        }
        return hongzhong;
    }

    public static List<Yzwdmj> getPais(String paisStr) {
        String[] pais = paisStr.split(",");
        List<Yzwdmj> handPais = new ArrayList<>();
        for (String pai : pais) {
            for (Yzwdmj mj : Yzwdmj.values()) {
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
        List<Yzwdmj> handPais = getPais(paisStr);
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

        List<Yzwdmj> lackPaiList = HuUtil.getLackPaiList(handPais, laiZiNum - 1);
        System.out.println(lackPaiList);
    }

}
