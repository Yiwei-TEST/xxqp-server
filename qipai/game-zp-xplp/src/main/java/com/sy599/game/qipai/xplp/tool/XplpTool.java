package com.sy599.game.qipai.xplp.tool;

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

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.xplp.rule.XpLp;
import com.sy599.game.qipai.xplp.rule.XplpIndexArr;
import com.sy599.game.util.JacksonUtil;

/**
 * @author lc
 */
public class XplpTool {

    /**
     * 获取听牌列表
     * cards.size+hzCount = 3n+1
     *
     * @param cardArr 去掉红中的牌
     * @param hzCount 红中数
     * @param hu7dui  是否可胡七对
     * @return
     */
    public static List<XpLp> getLackList(List<XpLp> handPais) {

        if (handPais == null || handPais.isEmpty()) {
            return null;
        }
        if (handPais.size() % 3 != 1) {
            return null;
        }
        
        if(!isTing(handPais)){
        	return null;
        }
        
        List<XpLp> lackPaiList = new ArrayList<>();
        Set<Integer> have = new HashSet<>();
    	for (XpLp mj : XpLp.fullMj) {
    		if ( have.contains(mj.getVal())) {
                continue;
            }
    		List<XpLp> copy = new ArrayList<>(handPais);
    		copy.add(mj);
    		if(isPingHu(copy)){
    			lackPaiList.add(mj);
                have.add(mj.getVal());
    		}
    	}
    	if (lackPaiList.size() == 30) {
            lackPaiList.clear();
            lackPaiList.add(null);
        }
        return lackPaiList;
    }


   
    public static synchronized List<List<XpLp>> fapai(List<Integer> copy, int playerCount) {
        List<List<XpLp>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<XpLp> allMjs = new ArrayList<>();
        for (int id : copy) {
            allMjs.add(XpLp.getMajang(id));
        }
        for (int i = 0; i < playerCount; i++) {
            if (i == 0) {
                list.add(new ArrayList<>(allMjs.subList(0, 17)));
            } else {
                list.add(new ArrayList<>(allMjs.subList(17 + (i - 1) * 16, 17 + (i - 1) * 16 + 16)));
            }
            if (i == playerCount - 1) {
                list.add(new ArrayList<>(allMjs.subList(17 + (i) * 16, allMjs.size())));
            }

        }
        return list;
    }

    public static synchronized List<List<XpLp>> fapai(List<Integer> copy, int playerCount, List<List<Integer>> t) {
        List<List<XpLp>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<List<XpLp>> zpList = new ArrayList<>();
        if (GameServerConfig.isDebug() && t != null && !t.isEmpty()) {
            for (List<Integer> zp : t) {
                zpList.add(XplpHelper.find(copy, zp));
            }
        }
        List<XpLp> allMjs = new ArrayList<>();
        for (int id : copy) {
            allMjs.add(XpLp.getMajang(id));
        }
        int count = 0;
        for (int i = 0; i < playerCount; i++) {
            if (i == 0) {
                if (zpList.size() > 0) {
                    List<XpLp> pai = zpList.get(0);
                    int len = 17 - pai.size();
                    pai.addAll(allMjs.subList(count, len));
                    count += len;
                    list.add(new ArrayList<>(pai));
                } else {
                    list.add(new ArrayList<>(allMjs.subList(0, 17)));
                    count += 17;
                }
            } else {
                if (zpList.size() > i) {
                    List<XpLp> pai = zpList.get(i);
                    int len = 16 - pai.size();
                    pai.addAll(allMjs.subList(count, count + len));
                    count += len;
                    list.add(new ArrayList<>(pai));
                } else {
                    list.add(new ArrayList<>(allMjs.subList(count, count + 16)));
                    count += 16;
                }
            }
            if (i == playerCount - 1) {
                if (zpList.size() > i + 1) {
                    List<XpLp> pai = zpList.get(i + 1);
                    pai.addAll(allMjs.subList(count, allMjs.size()));
                    list.add(new ArrayList<>(pai));
                } else {
                    list.add(new ArrayList<>(allMjs.subList(count, allMjs.size())));
                }
            }

        }
//        t.clear();
        return list;
    }

    public static synchronized List<List<XpLp>> fapai(List<Integer> copy) {
        List<List<XpLp>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<XpLp> pai = new ArrayList<>();
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
            XpLp majiang = XpLp.getMajang(copy.get(i));
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
        List<XpLp> left = new ArrayList<>();
        for (int i = 0; i < copy2.size(); i++) {
            left.add(XpLp.getMajang(copy2.get((i))));
        }
        list.add(left);
        return list;
    }

    public static boolean isPingHu(List<XpLp> majiangIds) {
        return isPingHu(majiangIds, false);

    }

    public static boolean isPingHu(List<XpLp> majiangIds, boolean needJiang258) {
        if (majiangIds == null || majiangIds.isEmpty()) {
            return false;
        }
        if (majiangIds.size() % 3 != 2) {
            System.out.println("%3！=2");
            return false;

        }

        // 先去掉红中
        List<XpLp> copy = new ArrayList<>(majiangIds);
//        List<XpLp> hongzhongList = dropHongzhong(copy);

        XplpIndexArr card_index = new XplpIndexArr();
        XplpQipaiTool.getMax(card_index, copy);
        // 拆将
        if (chaijiang(card_index, copy, 0, needJiang258)) {
            return true;
        } else {
            return false;
        }
    }


    
    public static boolean isTing(List<XpLp> majiangIds) {
        if (majiangIds == null || majiangIds.isEmpty()) {
            return false;
        }
        List<XpLp> copy = new ArrayList<>(majiangIds);
        if (majiangIds.size() % 3 != 1) {
            return false;
        }

        XplpIndexArr card_index = new XplpIndexArr();
        XplpQipaiTool.getMax(card_index, copy);
        // 拆将
        if (chaijiang(card_index, copy, 1, false)) {
            return true;
        } else {
            return false;
        }
    
    }

    // 拆将
    public static boolean chaijiang(XplpIndexArr card_index, List<XpLp> hasPais, int hongzhongnum, boolean needJiang258) {
        Map<Integer, List<XpLp>> jiangMap = card_index.getJiang(needJiang258);
        for (Entry<Integer, List<XpLp>> valEntry : jiangMap.entrySet()) {
            List<XpLp> copy = new ArrayList<>(hasPais);
            XplpHuLack lack = new XplpHuLack(hongzhongnum);
            List<XpLp> list = valEntry.getValue();
            int i = 0;
            for (XpLp majiang : list) {
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
            for (XpLp majiang : hasPais) {
                List<XpLp> copy = new ArrayList<>(hasPais);
                XplpHuLack lack = new XplpHuLack(hongzhongnum);
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
    public static boolean chaipai(XplpHuLack lack, List<XpLp> hasPais, boolean isNeedJiang258) {
        if (hasPais.isEmpty()) {
            return true;

        }
        boolean hu = chaishun(lack, hasPais, isNeedJiang258);
        if (hu)
            return true;
        return false;
    }

    public static void sortMin(List<XpLp> hasPais) {
        Collections.sort(hasPais, new Comparator<XpLp>() {

            @Override
            public int compare(XpLp o1, XpLp o2) {
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
    public static boolean chaishun(XplpHuLack lack, List<XpLp> hasPais, boolean needJiang258) {
        if (hasPais.isEmpty()) {
            return true;
        }
        sortMin(hasPais);
        XpLp minMajiang = hasPais.get(0);
        int minVal = minMajiang.getVal();
        List<XpLp> minList = XplpQipaiTool.getVal(hasPais, minVal);
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
        List<XpLp> num1 = XplpQipaiTool.getVal(hasPais, pai1);
        List<XpLp> num2 = XplpQipaiTool.getVal(hasPais, pai2);
        List<XpLp> num3 = XplpQipaiTool.getVal(hasPais, pai3);

        // 找到一句话的麻将
        List<XpLp> hasMajiangList = new ArrayList<>();
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
                List<XpLp> count = XplpQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
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

                List<XpLp> count = XplpQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
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
                List<XpLp> count1 = XplpQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                List<XpLp> count2 = XplpQipaiTool.getVal(hasPais, hasMajiangList.get(1).getVal());
                List<XpLp> count3 = XplpQipaiTool.getVal(hasPais, hasMajiangList.get(2).getVal());
                if (count1.size() >= 2 && (count2.size() == 1 || count3.size() == 1)) {
                    List<XpLp> copy = new ArrayList<>(hasPais);
                    removeAllPai(copy, count1);
                    //copy.removeAll(count1);
                    XplpHuLack copyLack = lack.copy();
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

    public static void removeAllPai(List<XpLp> hasPais, List<XpLp> remPai) {
        for (XpLp majiang : remPai) {
            hasPais.remove(majiang);
        }
    }

    public static boolean isCanAsJiang(XpLp majiang, boolean isNeed258) {
        if (isNeed258) {
            if (majiang.getPai() == 2 || majiang.getPai() == 5 || majiang.getPai() == 8) {
                return true;
            }
            return false;
        } else {
            return true;
        }

    }

    public static List<XpLp> checkChi(List<XpLp> majiangs, XpLp dismajiang) {
        return checkChi(majiangs, dismajiang, null);
    }

    /**
     * 是否能吃
     *
     * @param majiangs
     * @param dismajiang
     * @return
     */
    public static List<XpLp> checkChi(List<XpLp> majiangs, XpLp dismajiang, List<Integer> wangValList) {
        int disMajiangVal = dismajiang.getVal();
        List<Integer> chi1 = new ArrayList<>(Arrays.asList(disMajiangVal - 2, disMajiangVal - 1));
        List<Integer> chi2 = new ArrayList<>(Arrays.asList(disMajiangVal - 1, disMajiangVal + 1));
        List<Integer> chi3 = new ArrayList<>(Arrays.asList(disMajiangVal + 1, disMajiangVal + 2));

        List<Integer> majiangIds = XplpHelper.toMajiangVals(majiangs);
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
        return new ArrayList<XpLp>();
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
     * 去掉红中
     *
     * @param copy
     * @return
     */
    public static List<XpLp> dropHongzhong(List<XpLp> copy) {
        List<XpLp> hongzhong = new ArrayList<>();
        Iterator<XpLp> iterator = copy.iterator();
        while (iterator.hasNext()) {
            XpLp majiang = iterator.next();
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
                if (majiang instanceof XpLp) {
                    val = ((XpLp) majiang).getVal();
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

    public static List<XpLp> getPais(String paisStr) {
        String[] pais = paisStr.split(",");
        List<XpLp> handPais = new ArrayList<>();
        for (String pai : pais) {
            for (XpLp mj : XpLp.values()) {
                if (mj.getVal() == Integer.valueOf(pai) && !handPais.contains(mj)) {
                    handPais.add(mj);
                    break;
                }
            }
        }
        return handPais;
    }

    public static void main(String[] args) {
        int laiZiVal = 331;
        int laiZiNum = 0;
        String paisStr = "331,331,331,11,12,13,21,22,23,31,32,33,39,39";
        paisStr = "33,33,33,36,36,36,38,38,39,39,38,39,11,11,11,202";
        List<XpLp> handPais = getPais(paisStr);
        System.out.println(handPais);
        int count = 1;
        boolean canHu = false;
        long start = Clock.systemDefaultZone().millis();

        for (int i = 0; i < count; i++) {
            canHu = isTing(handPais);
        }
        long timeUse = Clock.systemDefaultZone().millis() - start;
        System.out.println("canHu = " + canHu + " , count = " + count + " , timeUse = " + timeUse + " ms");
    }

}
