package com.sy599.game.qipai.lyzp.tool;

import com.alibaba.fastjson.JSON;
import com.sy599.game.GameServerConfig;

import com.sy599.game.qipai.lyzp.been.CardTypeHuxi;
import com.sy599.game.qipai.lyzp.been.PaohuziHandCard;
import com.sy599.game.qipai.lyzp.been.PaohuziHuBean;
import com.sy599.game.qipai.lyzp.been.PaohzDisAction;
import com.sy599.game.qipai.lyzp.constant.PaohuziConstant;
import com.sy599.game.qipai.lyzp.constant.PaohzCard;
import com.sy599.game.qipai.lyzp.rule.PaohuziIndex;
import com.sy599.game.qipai.lyzp.rule.PaohzCardIndexArr;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.Map.Entry;

/**
 * 跑胡子
 *
 * @author lc
 */
public class PaohuziTool {

    /**
     * 发牌
     *
     * @param copy
     * @param t
     * @return
     */
    public static List<Integer> c2710List = Arrays.asList(2, 7, 10);

    
    public static List<List<PaohzCard>> zuopai(List<Integer> copy, List<List<Integer>> t,int playerCount){
        if(t.size()<playerCount)
            return null;
        List<List<PaohzCard>> list = new ArrayList<>();
        for (List<Integer> zp : t) {
            list.add(find(copy, zp));
        }
        if(list.size()>playerCount){
            List<PaohzCard> l=list.get(playerCount);
            for (int i = 0; i < copy.size(); i++) {
                l.add(PaohzCard.getPaohzCard(copy.get(i)));
            }
            list.add(l);
        }else if(list.size()==playerCount){
            List<PaohzCard> l=new ArrayList<>();
            for (int i = 0; i < copy.size(); i++) {
                l.add(PaohzCard.getPaohzCard(copy.get(i)));
            }
            list.add(l);
        }
        return list;
    }

    
    public static synchronized List<List<PaohzCard>> fapai(List<Integer> copy, List<List<Integer>> t,int fifteenCards,int playerCount) {
        List<List<PaohzCard>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<PaohzCard> pai = new ArrayList<>();
        int j = 1;

        if (GameServerConfig.isDebug() && t != null && !t.isEmpty()) {
            list = zuopai(copy, t, playerCount);
        }else {
            int handCardNum=20;
            if(fifteenCards==1){
                handCardNum=14;
            }

            List<Integer> copy2 = new ArrayList<>(copy);
            int fapaiCount = handCardNum * playerCount + 1 ;
            if (pai.size() >= handCardNum+1) {
                list.add(pai);
                pai = new ArrayList<>();
            }

            boolean test = false;
            if (list.size() > 0) {
                test = true;
            }

            for (int i = 0; i < fapaiCount; i++) {
                // 发牌张数=21*4+1 正好第一个发牌的人14张其他人13张
                PaohzCard majiang = PaohzCard.getPaohzCard(copy.get(i));
                copy2.remove((Object) copy.get(i));
                if (test) {
                    if (i < j * handCardNum) {
                        pai.add(majiang);
                    } else {
                        list.add(pai);
                        pai = new ArrayList<>();
                        pai.add(majiang);
                        j++;
                    }
                } else {
                    if (i <= j * handCardNum) {
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
            List<PaohzCard> left = new ArrayList<>();
            for (int i = 0; i < copy2.size(); i++) {
                left.add(PaohzCard.getPaohzCard(copy2.get((i))));
            }
            list.add(left);
        }
        if(allReFaPai(list,playerCount)){
            return list;
        }else {
            return fapai(copy,null,fifteenCards,playerCount);
        }

    }

    public static boolean allReFaPai(List<List<PaohzCard>> list,int playerCount){
        if(!checkShuangLong(list,playerCount))
            return false;
        if(!checkTianHu(list))
            return false;
        return true;
    }

    public static synchronized boolean checkShuangLong(List<List<PaohzCard>> list,int playerCount){
        for (int i = 0; i < playerCount; i++) {
            List<PaohzCard> cards = list.get(i);
            int []nums=new int[20];
            for (PaohzCard card:cards) {
                int k;
                int val = card.getVal();
                if(val<100){
                    k=val-1;
                }else {
                    k=val-91;
                }
                nums[k]++;
            }
            int count=0;
            for (int j = 0; j < nums.length; j++) {
                if(nums[j]==4){
                    count++;
                }
            }
            if (count>=2)
                return false;
        }
        return true;
    }

    public static synchronized boolean checkTianHu(List<List<PaohzCard>> list){
        List<PaohzCard> banker = list.get(0);
        List<PaohuziHuLack> hulist = isHuNew1(PaohuziTool.getPaohuziHandCardBean(banker), null, false, false, false);
        for (PaohuziHuLack hu:hulist){
            //跑胡不可能为飘胡
            hu.setHuxi(hu.calcHuxi());
            if (hu.getHuxi() >= 15||hu.getHuxi()==0){
                return false;
            }
        }
        return true;
    }


    /**
     * 检查麻将是否有重复
     *
     * @param majiangs
     * @return
     */
    public static boolean isPaohuziRepeat(List<PaohzCard> majiangs) {
        if (majiangs == null) {
            return false;
        }

        Map<Integer, Integer> map = new HashMap<>();
        for (PaohzCard mj : majiangs) {
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
     * 找出红牌
     *
     * @param copy
     * @return
     */
    public static List<PaohzCard> findRedPhzs(List<PaohzCard> copy) {
        List<PaohzCard> find = new ArrayList<>();
        for (PaohzCard card : copy) {
            if (c2710List.contains(card.getPai())) {
                find.add(card);
            }
        }
        return find;
    }

    /**
     * 找出相同的值
     *
     * @param copy
     * @param val
     * @return
     */
    public static List<PaohzCard> findPhzByVal(List<PaohzCard> copy, int val) {
        List<PaohzCard> list = new ArrayList<>();
        for (PaohzCard phz : copy) {
            if (phz.getVal() == val) {
                list.add(phz);
            }
        }
        return list;
    }

    private static List<PaohzCard> find(List<Integer> copy, List<Integer> valList) {
        List<PaohzCard> pai = new ArrayList<>();
        if (!valList.isEmpty()) {
            for (int zpId : valList) {
                Iterator<Integer> iterator = copy.iterator();
                while (iterator.hasNext()) {
                    int card = iterator.next();
                    PaohzCard phz = PaohzCard.getPaohzCard(card);
                    if (phz.getVal() == zpId) {
                        pai.add(phz);
                        iterator.remove();
                        break;
                    }
                }
            }

        }
        return pai;
    }

    /**
     * 转化为Map<val,valNum>
     */
    public static Map<Integer, Integer> toPhzValMap(List<PaohzCard> phzs) {
        Map<Integer, Integer> ids = new HashMap<>();
        if (phzs == null) {
            return ids;
        }
        for (PaohzCard phz : phzs) {

            if (ids.containsKey(phz.getVal())) {
                ids.put(phz.getVal(), ids.get(phz.getVal()) + 1);
            } else {
                ids.put(phz.getVal(), 1);
            }
        }
        return ids;
    }

    /**
     * 去除重复转化为val
     */
    public static List<Integer> toPhzRepeatVals(List<PaohzCard> phzs) {
        List<Integer> ids = new ArrayList<>();
        if (phzs == null) {
            return ids;
        }
        for (PaohzCard phz : phzs) {
            if (!ids.contains(phz.getVal())) {
                ids.add(phz.getVal());
            }
        }
        return ids;
    }

    /**
     * Id转化为牌
     *
     * @param phzIds
     * @return
     */
    public static List<PaohzCard> toPhzCards(List<Integer> phzIds) {
        List<PaohzCard> cards = new ArrayList<>();
        for (int id : phzIds) {
            cards.add(PaohzCard.getPaohzCard(id));
        }
        return cards;
    }

    /**
     * 牌转化为Id
     *
     * @param phzs
     * @return
     */
    public static List<Integer> toPhzCardZeroIds(List<?> phzs) {
        List<Integer> ids = new ArrayList<>();
        if (phzs == null) {
            return ids;
        }
        for (int i = 0; i < phzs.size(); i++) {
            if (i == 0) {
                ids.add((Integer) phzs.get(i));
            } else {
                ids.add(0);
            }
        }
        return ids;
    }

    /**
     * 牌转化为Id
     *
     * @param phzs
     * @return
     */
    public static List<Integer> toPhzCardIds(List<PaohzCard> phzs) {
        if (phzs == null) {
            return Collections.emptyList();
        }

        List<Integer> ids = new ArrayList<>(phzs.size());
        for (PaohzCard phz : phzs) {
            ids.add(phz.getId());
        }
        return ids;
    }

    /**
     * 跑胡子转化为牌s
     */
    public static List<Integer> toPhzCardVals(List<PaohzCard> phzs, boolean matchCase) {
        List<Integer> majiangIds = new ArrayList<>();
        if (phzs == null) {
            return majiangIds;
        }
        for (PaohzCard card : phzs) {
            if (matchCase) {
                majiangIds.add(card.getVal());
            } else {
                majiangIds.add(card.getPai());
            }
        }

        return majiangIds;
    }

    /**
     * 得到最大相同数
     */
    public static PaohzCardIndexArr getMax(List<PaohzCard> list) {
        PaohzCardIndexArr card_index = new PaohzCardIndexArr();
        Map<Integer, List<PaohzCard>> phzMap = new HashMap<>();
        for (PaohzCard phzCard : list) {
            List<PaohzCard> count;
            if (phzMap.containsKey(phzCard.getVal())) {
                count = phzMap.get(phzCard.getVal());
            } else {
                count = new ArrayList<>();
                phzMap.put(phzCard.getVal(), count);
            }
            count.add(phzCard);
        }
        for (int phzVal : phzMap.keySet()) {
            List<PaohzCard> phzList = phzMap.get(phzVal);
            switch (phzList.size()) {
                case 1:
                    card_index.addPaohzCardIndex(0, phzList, phzVal);
                    break;
                case 2:
                    card_index.addPaohzCardIndex(1, phzList, phzVal);
                    break;
                case 3:
                    card_index.addPaohzCardIndex(2, phzList, phzVal);
                    break;
                case 4:
                    card_index.addPaohzCardIndex(3, phzList, phzVal);
                    break;
            }
        }
        return card_index;
    }

    /**
     * 是否能吃
     *
     * @param handCards
     * @param disCard
     * @return
     */
    public static List<PaohzCard> checkChi(List<PaohzCard> handCards, PaohzCard disCard) {
        int disVal = disCard.getVal();
        int otherVal = disCard.getOtherVal();

        List<Integer> chi0 = new ArrayList<>(Arrays.asList(disVal, otherVal));
        List<Integer> chi4 = new ArrayList<>(Arrays.asList(otherVal, otherVal));
        List<Integer> chi1 = new ArrayList<>(Arrays.asList(disVal - 2, disVal - 1));
        List<Integer> chi2 = new ArrayList<>(Arrays.asList(disVal - 1, disVal + 1));
        List<Integer> chi3 = new ArrayList<>(Arrays.asList(disVal + 1, disVal + 2));
        List<List<Integer>> chiList = new ArrayList<>();
        chiList.add(chi0);
        chiList.add(chi4);
        chiList.add(chi1);
        chiList.add(chi2);
        chiList.add(chi3);

        List<Integer> val2710 = Arrays.asList(2, 7, 10);
        if (val2710.contains(disCard.getPai())) {
            List<Integer> chi2710 = new ArrayList<>();
            for (int val : val2710) {
                if (disCard.getPai() == val) {
                    continue;
                }
                chi2710.add(disCard.getCase() + val);

            }

            chiList.add(chi2710);

        }

        List<PaohzCard> copy = new ArrayList<>(handCards);
        copy.remove(disCard);

        for (List<Integer> chi : chiList) {
            List<PaohzCard> findList = findPhzCards(copy, chi);
            if (!findList.isEmpty()) {
                return findList;
            }

        }

        return new ArrayList<PaohzCard>();
    }

    public static List<PaohzCard> findPhzCards(List<PaohzCard> cards, List<Integer> vals) {
        List<PaohzCard> findList = new ArrayList<>();
        for (int chiVal : vals) {
            boolean find = false;
            for (PaohzCard card : cards) {
                if (findList.contains(card)) {
                    continue;
                }
                if (card.getVal() == chiVal) {
                    findList.add(card);
                    find = true;
                    break;
                }
            }
            if (!find) {
                findList.clear();
                break;
            }
        }
        return findList;
    }

    /**
     * 检查出相同的牌
     */
    public static List<PaohzCard> getSameCards(List<PaohzCard> handCards, PaohzCard disCard) {
        List<PaohzCard> list = findCountByVal(handCards, disCard, true);
        if (list != null) {
            return list;
        }
        return null;

    }

    /**
     * 是否一样的牌
     */
    public static boolean isSameCard(List<PaohzCard> handCards) {
        int val = 0;
        for (PaohzCard card : handCards) {
            if (val == 0) {
                val = card.getVal();
                continue;
            }
            if (val != card.getVal()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 删除牌
     *
     * @param handCards
     * @param cardVal
     * @return
     */
    public static void removePhzByVal(List<PaohzCard> handCards, int cardVal) {
        Iterator<PaohzCard> iterator = handCards.iterator();
        while (iterator.hasNext()) {
            PaohzCard paohzCard = iterator.next();
            if (paohzCard.getVal() == cardVal) {
                iterator.remove();
            }

        }
    }

    /**
     * 是否有这张相同的牌
     */
    public static boolean isHasCardVal(List<PaohzCard> handCards, int cardVal) {
        for (PaohzCard card : handCards) {
            if (cardVal == card.getVal()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param phzList   牌List
     * @param o         值or牌
     * @param matchCase 是否为值（true取val，false取pai）
     * @return 返回与o的值相同的牌的集合
     */
    public static List<PaohzCard> findCountByVal(List<PaohzCard> phzList, Object o, boolean matchCase) {
        int val;
        if (o instanceof PaohzCard) {
            if (matchCase) {
                val = ((PaohzCard) o).getVal();

            } else {
                val = ((PaohzCard) o).getPai();

            }
        } else if (o instanceof Integer) {
            val = (int) o;
        } else {
            return null;
        }
        List<PaohzCard> result = new ArrayList<>();
        for (PaohzCard card : phzList) {
            int matchVal;
            if (matchCase) {
                matchVal = card.getVal();
            } else {
                matchVal = card.getPai();
            }
            if (matchVal == val) {
                result.add(card);
            }
        }
        return result;
    }



    /**
     * 得到某个值的麻将
     *
     * @param copy
     * @return
     */
    public static List<PaohzCard> getVals(List<PaohzCard> copy, int val, PaohzCard... exceptCards) {
        List<PaohzCard> list = new ArrayList<>();
        Iterator<PaohzCard> iterator = copy.iterator();
        while (iterator.hasNext()) {
            PaohzCard phz = iterator.next();
            if (exceptCards != null) {
                // 有某些除外的牌不能算在内
                boolean findExcept = false;
                for (PaohzCard except : exceptCards) {
                    if (phz == except) {
                        findExcept = true;
                        break;
                    }
                }
                if (findExcept) {
                    continue;
                }
            }
            if (phz.getVal() == val) {
                list.add(phz);
            }
        }
        return list;
    }

    /**
     * 得到某个值的跑胡子牌
     *
     * @param copy 手牌
     */
    public static PaohzCard getVal(List<PaohzCard> copy, int val, PaohzCard... exceptCards) {
        for (PaohzCard phz : copy) {
            if (exceptCards != null) {
                // 有某些除外的牌不能算在内
                boolean findExcept = false;
                for (PaohzCard except : exceptCards) {
                    if (phz == except) {
                        findExcept = true;
                        break;
                    }
                }
                if (findExcept) {
                    continue;
                }
            }
            if (phz.getVal() == val) {
                return phz;
            }
        }
        return null;
    }

    public static void sortMin(List<PaohzCard> hasPais) {
        Collections.sort(hasPais, new Comparator<PaohzCard>() {
            @Override
            public int compare(PaohzCard o1, PaohzCard o2) {
                if (o1.getPai() < o2.getPai()) {
                    return -1;
                } else if (o1.getPai() > o2.getPai()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }

    // 拆牌
    public static boolean chaipai(PaohuziHuLack lack, List<PaohzCard> hasPais) {
        if (hasPais.isEmpty()) {
            return true;

        }
        boolean hu = chaishun(lack, hasPais);
        if (hu)
            return true;
        return false;
    }

    public static boolean chaiSame(PaohuziHuLack lack, List<PaohzCard> hasPais, PaohzCard minCard, List<PaohzCard> sameList) {
        if (sameList.size() < 3) {
            // 小于3张牌没法拆
            return false;
        } else if (sameList.size() == 3) {
            // 大小加一起正好3张牌
            boolean chaisame = chaishun3(lack, hasPais, minCard, false, false, sameList.get(0).getVal(), sameList.get(1).getVal(), sameList.get(2).getVal());
            if (!chaisame) {
                return false;
            } else {
                return true;
            }
        } else if (sameList.size() > 3) {
            int minVal = minCard.getVal();
            List<List<Integer>> modelList = getSameModel(minVal);
            for (List<Integer> model : modelList) {
                PaohuziHuLack copyLack = lack.clone();
                List<PaohzCard> copyHasPais = new ArrayList<>(hasPais);
                boolean chaisame = chaishun3(copyLack, copyHasPais, minCard, false, false, model.get(0), model.get(1), minVal);
                if (chaisame) {
                    lack.copy(copyLack);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 拆坎子
     *
     * @param hasPais canoperateHandPais
     */
    private static boolean chaikan(PaohuziHuLack lack, List<PaohzCard> hasPais, PaohzCard minCard) {
        List<PaohzCard> sameValList = findCountByVal(hasPais, minCard, true);
        if (sameValList.size() == 3) {
            boolean chaiSame = chaishun3(lack, hasPais, minCard, false, false, sameValList.get(0).getVal(), sameValList.get(1).getVal(), sameValList.get(2).getVal());
            if (chaiSame) {
                return true;
            }
        }
        return false;
    }

    private static boolean chaiSame0(PaohuziHuLack lack, List<PaohzCard> hasPais, PaohzCard minCard) {
        List<PaohzCard> sameList = findCountByVal(hasPais, minCard, false);
        if (sameList.size() < 3) {
            return false;
        }

        // 拆相同
        PaohuziHuLack copyLack = lack.clone();
        List<PaohzCard> copyHasPais = new ArrayList<>(hasPais);
        if (chaikan(copyLack, copyHasPais, minCard)) {
            lack.copy(copyLack);
            return true;
        }

        // 拆相同2
        boolean chaiSame = chaiSame(copyLack, copyHasPais, minCard, sameList);
        if (chaiSame) {
            lack.copy(copyLack);
            return true;
        }
        return false;
    }

    /**
     * 拆顺
     */
    public static boolean chaishun(PaohuziHuLack lack, List<PaohzCard> hasPais) {
        if (hasPais.isEmpty()) {
            return true;
        }
        sortMin(hasPais);
        PaohzCard minCard = hasPais.get(0);
        int minVal = minCard.getVal();
        int minPai = minCard.getPai();
        boolean isTryChaiSame = false;
        PaohuziHuLack copyLack = null;
        List<PaohzCard> copyHasPais = null;
        if (minPai != 1 && minPai != 2) {
            // 如果不是1 和2 尝试拆相同
            isTryChaiSame = true;
            boolean isHu = chaiSame0(lack, hasPais, minCard);
            if (isHu) {
                return true;
            }
        } else {
            copyLack = lack.clone();
            copyHasPais = new ArrayList<>(hasPais);
        }

        // 拆顺子
        int pai1 = minVal;
        int pai2 = 0;
        int pai3 = 0;
        if (pai1 % 100 == 10) {
            pai1 = pai1 - 2;

        } else if (pai1 % 100 == 9) {
            pai1 = pai1 - 1;
        }
        pai2 = pai1 + 1;
        pai3 = pai2 + 1;
        boolean check2710 = false;
        boolean chaishun = false;
        if (!lack.isHasFail2710Val(minVal) && minCard.getPai() == 2) {
            // 2 7 10
            int pai7 = minCard.getCase() + 7;
            int pai10 = minCard.getCase() + 10;
            check2710 = true;
            PaohuziHuLack copyLack2 = lack.clone();
            List<PaohzCard> copyHasPais2 = new ArrayList<>(hasPais);
            chaishun = chaiShun(copyLack2, copyHasPais2, minCard, true, check2710, pai1, pai7, pai10);
            if (chaishun) {
                lack.copy(copyLack2);
            } else {
                // 拆2 7 10组合失败
                chaishun = chaiShun(lack, hasPais, minCard, true, false, pai1, pai2, pai3);
            }
        } else {
            // 拆顺
            chaishun = chaiShun(lack, hasPais, minCard, true, check2710, pai1, pai2, pai3);
        }

        if (!chaishun && !isTryChaiSame) {
            chaishun = chaiSame0(copyLack, copyHasPais, minCard);
            if (chaishun) {
                lack.copy(copyLack);

            }
        }
        return chaishun;

    }

    public static boolean chaiShun(PaohuziHuLack lack, List<PaohzCard> hasPais, PaohzCard minCard, boolean isShun, boolean check2710, int pai1, int pai2, int pai3) {
        // 拆顺
        boolean chaishun = chaishun3(lack, hasPais, minCard, true, check2710, pai1, pai2, pai3);
        if (!chaishun) {
            if (check2710) {
                return chaishun(lack, hasPais);
            }
            // 拆同牌
            return false;
        }
        return true;
    }

    /**
     * @return
     */
    private static List<List<Integer>> getSameModel(int val) {
        List<List<Integer>> result = new ArrayList<>();
        int smallpai = val % 100;
        int bigpai = 100 + smallpai;
        List<Integer> model1 = Arrays.asList(bigpai, smallpai);
        List<Integer> model2 = Arrays.asList(bigpai, bigpai);
        List<Integer> model3 = Arrays.asList(smallpai, smallpai);

        result.add(model2);
        result.add(model3);
        result.add(model1);
        return result;
    }

    private static boolean chaishun3(PaohuziHuLack lack, List<PaohzCard> hasPais, PaohzCard minCard, boolean isShun, boolean check2710, int pai1, int pai2, int pai3) {
        int minVal = minCard.getVal();

        List<Integer> lackList = new ArrayList<>();
        PaohzCard num1 = getVal(hasPais, pai1);
        PaohzCard num2 = getVal(hasPais, pai2, num1);
        PaohzCard num3 = getVal(hasPais, pai3, num1, num2);

        // 找到一句话的
        List<PaohzCard> hasMajiangList = new ArrayList<>();
        if (num1 != null) {
            hasMajiangList.add(num1);
        }
        if (num2 != null) {
            hasMajiangList.add(num2);
        }
        if (num3 != null) {
            hasMajiangList.add(num3);
        }

        // 一句话缺少的
        if (num1 == null) {
            lackList.add(pai1);
        }
        if (num2 == null) {
            lackList.add(pai2);
        }
        if (num3 == null) {
            lackList.add(pai3);
        }

        int lackNum = lackList.size();
        if (lackNum > 0) {
            // 看看三张牌是否相同
            if (lack.getHongzhongNum() <= 0) {
                if (check2710) {
                    lack.addFail2710Val(minVal);
                    return chaipai(lack, hasPais);
                }
                return false;
            }

            // 做成一句话缺少2张以上的，没有将优先做将
            if (lackNum >= 2) {
                // 补坎子
                List<PaohzCard> count = getVals(hasPais, hasMajiangList.get(0).getVal());
                if (count.size() == 2) {
                    if (lack.isNeedDui()) {
                        // 没有将做将
                        lack.setNeedDui(false);
                        hasPais.removeAll(count);
                        return chaipai(lack, hasPais);
                    }

                    // 拿一张红中补坎子
                    lack.changeHongzhong(-1);
                    lack.addLack(count.get(0).getVal());
                    hasPais.removeAll(count);
                    return chaipai(lack, hasPais);
                }

                // 做将
                if (lack.isNeedDui() && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.setNeedDui(false);
                    hasPais.removeAll(count);
                    lack.addLack(count.get(0).getVal());
                    return chaipai(lack, hasPais);
                }
            } else if (lackNum == 1) {
                // 做将
                if (lack.isNeedDui() && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.setNeedDui(false);
                    hasPais.remove(minCard);
                    lack.addLack(minCard.getVal());
                    return chaipai(lack, hasPais);
                }

                List<PaohzCard> count = getVals(hasPais, hasMajiangList.get(0).getVal());
                if (count.size() == 2 && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.addLack(count.get(0).getVal());
                    hasPais.removeAll(count);
                    return chaipai(lack, hasPais);
                }
            }

            // 如果有红中则补上
            if (lack.getHongzhongNum() >= lackNum) {
                lack.changeHongzhong(-lackNum);
                hasPais.removeAll(hasMajiangList);
                lack.addAllLack(lackList);

            } else {
                return false;
            }
        } else {
            // 可以一句话
            if (lack.getHongzhongNum() > 0) {
                List<PaohzCard> count1 = getVals(hasPais, hasMajiangList.get(0).getVal());
                List<PaohzCard> count2 = getVals(hasPais, hasMajiangList.get(1).getVal());
                List<PaohzCard> count3 = getVals(hasPais, hasMajiangList.get(2).getVal());
                if (count1.size() >= 2 && count2.size() == 1 && count3.size() == 1) {
                    hasPais.removeAll(count1);
                    lack.changeHongzhong(-1);
                    lack.addLack(hasMajiangList.get(0).getVal());
                    chaipai(lack, hasPais);
                }
            }
            int huxi = 0;
            int action = PaohzDisAction.action_chi;
            if (isShun) {
                int minPai = minCard.getPai();
                if (minPai == 1 || minPai == 2) {
                    // 123 和 2710 加胡息
                    huxi = getShunHuxi(hasMajiangList);
                }
            } else {
                if (isSameCard(hasMajiangList)) {
                    // 如果是三个一模一样的
                    action = PaohzDisAction.action_peng;
                    if (lack.isSelfMo()) {
                        action = PaohzDisAction.action_zai;
                    }
                    huxi = getOutCardHuxi(action, hasMajiangList);
                }
            }

            lack.addPhzHuCards(action, toPhzCardIds(hasMajiangList), huxi);
            lack.changeHuxi(huxi);
            hasPais.removeAll(hasMajiangList);
            return chaipai(lack, hasPais);
        }
        return chaipai(lack, hasPais);
    }

    /**
     * 去除重复后得到val的集合
     *
     * @param cards
     * @return
     */
    public static Map<Integer, Integer> getDistinctVal(List<PaohzCard> cards) {
        Map<Integer, Integer> valIds = new HashMap<>();
        if (cards == null) {
            return valIds;
        }
        for (PaohzCard phz : cards) {
            if (valIds.containsKey(phz.getVal())) {
                valIds.put(phz.getVal(), valIds.get(phz.getVal()) + 1);
            } else {
                valIds.put(phz.getVal(), 1);
            }
        }
        return valIds;
    }

    /**
     * 算出的牌的胡息
     *
     * @param action
     * @param cards
     * @return
     */
    public static int getOutCardHuxi(int action, List<PaohzCard> cards) {
        int huxi = 0;
        if (action == 0) {
            return huxi;
        }
        if (action == PaohzDisAction.action_ti) {
            // 大的12分 小的9分
            Map<Integer, Integer> valmap = getDistinctVal(cards);
            for (Entry<Integer, Integer> entry : valmap.entrySet()) {
                if (entry.getValue() != 4) {
                    continue;

                }
                if (entry.getKey() > 100) {
                    // 大的
                    huxi += 12;
                } else {
                    huxi += 9;
                }
            }

        } else if (action == PaohzDisAction.action_pao) {
            // 大的9分 小的6分
            Map<Integer, Integer> valmap = getDistinctVal(cards);
            for (Entry<Integer, Integer> entry : valmap.entrySet()) {
                if (entry.getValue() != 4) {
                    continue;
                }
                if (entry.getKey() > 100) {
                    // 大的
                    huxi += 9;
                } else {
                    huxi += 6;
                }

            }
        } else if (action == PaohzDisAction.action_chi) {
            // 吃 只有123 和2710 大的6分 小的3分
            List<PaohzCard> copy = new ArrayList<>(cards);
            sortMin(copy);
            huxi = getShunHuxi(copy);
        } else if (action == PaohzDisAction.action_peng) {
            // 大的3分 小的1分
            Map<Integer, Integer> valmap = getDistinctVal(cards);
            for (Entry<Integer, Integer> entry : valmap.entrySet()) {
                if (entry.getValue() != 3) {
                    continue;
                }
                if (entry.getKey() > 100) {
                    // 大的
                    huxi += 3;
                } else {
                    huxi += 1;
                }
            }

        } else if (action == PaohzDisAction.action_zai || action == PaohzDisAction.action_chouzai) {
            // 大的6分 小的3分
            Map<Integer, Integer> valmap = getDistinctVal(cards);
            for (Entry<Integer, Integer> entry : valmap.entrySet()) {
                if (entry.getValue() != 3) {
                    continue;
                }
                if (entry.getKey() > 100) {
                    // 大的
                    huxi += 6;
                } else {
                    huxi += 3;
                }
            }

        }
        return huxi;
    }

    /**
     * 算顺子的胡息 123 和2710 分大小 大6小3
     *
     * @param hasMajiangList
     * @return
     */
    private static int getShunHuxi(List<PaohzCard> hasMajiangList) {
        if (hasMajiangList.size() < 3) {
            return 0;
        }
        if (hasMajiangList.get(0).getPai() == hasMajiangList.get(1).getPai()) {
            //不是顺子
            return 0;
        }
        PaohzCard minCard = hasMajiangList.get(0);
        if (minCard.getPai() == 1) {
            if (minCard.isBig()) {
                return 6;
            } else {
                return 3;
            }
        } else if (minCard.getPai() == 2) {
            for (PaohzCard card : hasMajiangList) {
                if (!c2710List.contains(card.getPai())) {
                    return 0;
                }
            }
            if (minCard.isBig()) {
                return 6;
            } else {
                return 3;
            }
        }
        return 0;
    }

    /**
     * 得到可以操作的牌 4张和3张是不能操作的
     */
    public static PaohuziHandCard getPaohuziHandCardBean(List<PaohzCard> handPais) {
        PaohuziHandCard card = new PaohuziHandCard();
        List<PaohzCard> copy = new ArrayList<>(handPais);
        card.setHandCards(new ArrayList<>(copy));

        PaohzCardIndexArr valArr = PaohuziTool.getMax(copy);
        card.setIndexArr(valArr);
        // 去掉4张和3张
        PaohuziIndex index3 = valArr.getPaohzCardIndex(3);
        if (index3 != null) {
            copy.removeAll(index3.getPaohzList());
        }
        PaohuziIndex index2 = valArr.getPaohzCardIndex(2);
        if (index2 != null) {
            copy.removeAll(index2.getPaohzList());
        }
        card.setOperateCards(copy);
        return card;
    }

    /**
     * 将array组合成用delimiter分隔的字符串
     *
     * @return String
     */
    public static List<PaohzCard> explodePhz(String str, String delimiter) {
        List<PaohzCard> list = new ArrayList<>();
        if (StringUtils.isBlank(str) || str.equals("null") || str.equals("undefined"))
            return list;
        String strArray[] = str.split(delimiter);

        for (String val : strArray) {
            PaohzCard phz = null;
            if (val.startsWith("mj")) {
                phz = PaohzCard.valueOf(PaohzCard.class, val);
            } else {
                phz = PaohzCard.getPaohzCard((Integer.valueOf(val)));
            }
            list.add(phz);
        }
        return list;
    }

    /**
     * 将array组合成用delimiter分隔的字符串
     *
     * @param array
     * @param delimiter
     * @return String
     */
    public static String implodePhz(List<PaohzCard> array, String delimiter) {
        if (array == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder("");
        for (PaohzCard i : array) {
            sb.append(i.getId());
            sb.append(delimiter);
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
        return sb.toString();
    }

    /**
     * 自动出牌
     */
    public static PaohzCard autoDisCard(List<PaohzCard> handPais) {
        List<PaohzCard> copy = new ArrayList<>(handPais);
        PaohzCardIndexArr valArr = PaohuziTool.getMax(copy);
        PaohuziIndex index1 = valArr.getPaohzCardIndex(1);
        PaohuziIndex index0 = valArr.getPaohzCardIndex(0);
        //List<Integer> list2 = valArr.getPaohzCardIndex(1).getValList();
        //List<Integer> list1 = valArr.getPaohzCardIndex(0).getValList();
        int val = 0;
        if (index0 != null && !index0.getValList().isEmpty()) {
            //val = new Random().nextInt(index0.getValList().size());
            for (int i = 0; i < index0.getValList().size(); i++) {
                if (val == 0) {
                    val = index0.getValList().get(i);
                    continue;
                }
                if (val + 1 != index0.getValList().size()) {
                    break;
                }
                if (i + 1 % 3 == 0) {
                    val = 0;
                }
            }
            if (val == 0 && index1 != null && !index1.getValList().isEmpty()) {
                val = Collections.min(index1.getValList());
            }
        } else if (index1 != null && !index1.getValList().isEmpty()) {
            val = Collections.min(index1.getValList());
        }
        if (val == 0) {
            return null;
        } else {
            PaohzCard card = null;
            for (PaohzCard paohzCard : handPais) {
                if (paohzCard.getVal() == val) {
                    card = paohzCard;
                    break;
                }
            }
            return card;
        }
    }

    public static void main(String[] args) {
        List<Integer> list=new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        list.add(6);
        for (int i = 0; i < list.size() / 3; i++) {
            List<Integer> l = list.subList(0 + i * 3, (i + 1) * 3);
            System.out.println(l);
        }
    }

    public static List<PaohzCard> val2Card(List<Integer> valList) {
        List<PaohzCard> allCard = new ArrayList<>(toPhzCards(PaohuziConstant.cardList));
        List<PaohzCard> cardList = new ArrayList<>();
        for (Integer val : valList) {
            for (PaohzCard card : allCard) {
                if (card.getVal() == val && !cardList.contains(card)) {
                    cardList.add(card);
                    break;
                }
            }
        }
        return cardList;
    }

    public static List<PaohuziHuLack> isHuNew1(PaohuziHandCard handCardBean, PaohzCard disCard, boolean isSelfMo, boolean needDui, boolean isPaoHu) {

        PaohuziHuLack lack = new PaohuziHuLack(0);
        lack.setSelfMo(isSelfMo);
        lack.setCheckCard(disCard);
        lack.setNeedDui(needDui);

        PaohzCardIndexArr arr = handCardBean.getIndexArr();
        // 手上有3个的
        PaohuziIndex index2 = arr.getPaohzCardIndex(2);
        if (index2 != null) {
            List<Integer> list = index2.getValList();
            for (int val : list) {
                if (!isSelfMo&&disCard != null && val == disCard.getVal()) {
                    // 抓到手上的牌可以不用强制组出3个
                    handCardBean.getOperateCards().addAll(index2.getPaohzValMap().get(val));
                    continue;
                }
                int huxi = val > 100 ? 6 : 3;
                lack.changeHuxi(huxi);
                List<PaohzCard> cards = index2.getPaohzValMap().get(val);
                lack.addPhzHuCards(PaohzDisAction.action_kan, toPhzCardIds(cards), huxi);
            }
        }

        PaohuziIndex index3 = arr.getPaohzCardIndex(3);
        if (index3 != null) {
            List<Integer> list = index3.getValList();
            for (int val : list) {
                int huxi = 0;
                boolean paoHu = false;
                int action = PaohzDisAction.action_pao;
                if (disCard != null && val == disCard.getVal()) {
                    if (!isPaoHu) {
                        action = PaohzDisAction.action_kan;
                        huxi = val > 100 ? 6 : 3;
                        handCardBean.getOperateCards().add(disCard);
                    } else if (isSelfMo) {
                        huxi = val > 100 ? 12 : 9;
                        action = PaohzDisAction.action_ti;
                    } else {
                        action = PaohzDisAction.action_pao;
                        paoHu = true;
                        huxi = val > 100 ? 9 : 6;
                        List<PaohzCard> cards = index3.getPaohzValMap().get(val);
                        lack.changeHuxi(huxi);
                        lack.addPhzHuCards(action, toPhzCardIds(cards), huxi);
                    }
                } else {
                    //直接手上有4张
                    action = PaohzDisAction.action_ti;
                    huxi = val > 100 ? 12 : 9;
                    lack.setNeedDui(true);
                }
                if (!paoHu) {
                    lack.changeHuxi(huxi);
                    List<PaohzCard> cards = index3.getPaohzValMap().get(val);
                    if (!isPaoHu) {
                        cards.remove(disCard);
                    }
                    lack.addPhzHuCards(action, toPhzCardIds(cards), huxi);
                }
            }
        }
        List<PaohuziHuLack> huList = new ArrayList<>();
        List<PaohzCard> handPais = handCardBean.getOperateCards();
        if (handPais.size() == 0) {
            lack.setHu(true);
            huList.add(lack);
        } else {
            sortMin(handPais);
            chaiPaiNew(huList, lack, handPais, disCard);
            if (huList.size() > 0) {
                return huList;
            }
        }
        return huList;
    }


    public static void chaiPaiNew(List<PaohuziHuLack> huList, PaohuziHuLack lack, List<PaohzCard> handPais, PaohzCard disCard) {
        if (lack.isNeedDui()) {
            //拆对
            PaohuziHandCard bean = getPaohuziHandCardBean(handPais);
            PaohzCardIndexArr indexArr = bean.getIndexArr();
            PaohuziIndex duiZi = indexArr.getPaohzCardIndex(1);
            if (duiZi != null && duiZi.getPaohzValMap().size() > 0) {
                for (Integer val : duiZi.getPaohzValMap().keySet()) {
                    List<PaohzCard> handPaisCopy = new ArrayList<>(handPais);

                    List<PaohzCard> duiZiCardList = duiZi.getPaohzValMap().get(val);
                    handPaisCopy.removeAll(duiZiCardList);

                    List<Integer> duiZiIdList = new ArrayList<>();
                    for (PaohzCard card : duiZiCardList) {
                        duiZiIdList.add(card.getId());
                    }

                    PaohuziHuLack newLack = lack.clone();
                    newLack.setNeedDui(false);
                    newLack.addPhzHuCards(0, duiZiIdList, 0);
                    if (handPaisCopy.size() == 0) {
                        newLack.setHu(true);
                        huList.add(newLack);
                    } else {
                        if (disCard != null && disCard.getVal() == val) {
                            chaiPaiNew(huList, newLack, handPaisCopy, null);
                        } else {
                            chaiPaiNew(huList, newLack, handPaisCopy, disCard);
                        }
                    }
                }
            }
            PaohuziIndex sanZhang = indexArr.getPaohzCardIndex(2);
            if (sanZhang != null && sanZhang.getPaohzValMap().size() > 0) {
                for (Integer val : sanZhang.getPaohzValMap().keySet()) {
                    List<PaohzCard> handPaisCopy = new ArrayList<>(handPais);

                    List<PaohzCard> duiZiCardList = sanZhang.getPaohzValMap().get(val).subList(0, 2);
                    handPaisCopy.removeAll(duiZiCardList);

                    List<Integer> duiZiIdList = new ArrayList<>();
                    for (PaohzCard card : duiZiCardList) {
                        duiZiIdList.add(card.getId());
                    }

                    PaohuziHuLack newLack = lack.clone();
                    newLack.setNeedDui(false);
                    newLack.addPhzHuCards(0, duiZiIdList, 0);
                    if (handPaisCopy.size() == 0) {
                        newLack.setHu(true);
                        huList.add(newLack);
                    } else {
                        if (disCard != null && disCard.getVal() == val) {
                            chaiPaiNew(huList, newLack, handPaisCopy, null);
                        } else {
                            chaiPaiNew(huList, newLack, handPaisCopy, disCard);
                        }
                    }
                }
            }
        } else {
            if (handPais.size() < 3) {
                return;
            }
            //拆顺
            int val;
            if (disCard != null) {
                val = disCard.getVal();
            } else {
                val = handPais.get(0).getVal();
            }
            List<int[]> paiZus = PaohuziConstant.getPaiZu(val);
            List<PaohzCard> handPaisCopy;
            List<PaohzCard> rmList;
            PaohuziHuLack newLack;
            for (int[] paiZu : paiZus) {
                handPaisCopy = new ArrayList<>(handPais);
                rmList = removeVals(handPaisCopy, paiZu);
                if (rmList == null) {
                    continue;
                }

                newLack = lack.clone();
                if (disCard != null && disCard.getVal() == val && isSameCard(rmList)) {
                    // 三张一模一样的，碰牌分
                    newLack.addPhzHuCards(PaohzDisAction.action_peng, toPhzCardIds(rmList), disCard.isBig() ? 3 : 1);
                } else {
                    newLack.addPhzHuCards(0, toPhzCardIds(rmList), getShunHuxi(rmList));
                }
                if (handPaisCopy.size() == 0) {
                    newLack.setHu(true);
                    huList.add(newLack);
                } else {
                    chaiPaiNew(huList, newLack, handPaisCopy, null);
                }
            }
        }
    }

    public static List<PaohzCard> removeVals(List<PaohzCard> cards, int[] vals) {
        List<PaohzCard> rmList = new ArrayList<>(vals.length);
        for (Integer val : vals) {
            boolean hasVal = false;
            for (PaohzCard card : cards) {
                if (card.getVal() == val && !rmList.contains(card)) {
                    rmList.add(card);
                    hasVal = true;
                    break;
                }
            }
            if (!hasVal) {
                return null;
            }
        }
        if (rmList.size() == vals.length) {
            cards.removeAll(rmList);
            return rmList;
        }
        return null;
    }


}
