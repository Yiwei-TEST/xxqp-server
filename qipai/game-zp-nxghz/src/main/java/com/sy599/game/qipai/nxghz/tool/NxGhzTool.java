package com.sy599.game.qipai.nxghz.tool;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage;
import com.sy599.game.GameServerConfig;
import com.sy599.game.msg.serverPacket.TableGhzResMsg.ClosingGhzInfoRes;
import com.sy599.game.msg.serverPacket.TableGhzResMsg.ClosingGhzPlayerInfoRes;
import com.sy599.game.qipai.nxghz.bean.NxGhzCardTypeHuxi;
import com.sy599.game.qipai.nxghz.bean.NxGhzDisAction;
import com.sy599.game.qipai.nxghz.bean.NxGhzHuBean;
import com.sy599.game.qipai.nxghz.bean.NxGhzPlayer;
import com.sy599.game.qipai.nxghz.bean.NxGhzTable;
import com.sy599.game.qipai.nxghz.constant.NxGhzCard;
import com.sy599.game.qipai.nxghz.constant.NxGhzConstant;
import com.sy599.game.qipai.nxghz.rule.NxGhzCardIndexArr;
import com.sy599.game.qipai.nxghz.rule.NxGhzIndex;
import com.sy599.game.qipai.nxghz.rule.NxGhzMenzi;
import com.sy599.game.qipai.nxghz.rule.NxGhzMingTangRule;
import com.sy599.game.util.JacksonUtil;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.Map.Entry;

/**
 * 鬼胡子
 *
 * @author lc
 */
public class NxGhzTool {
    /**
     * 发牌
     *
     * @param copy
     * @param t
     * @return
     */
    public static List<Integer> c2710List = Arrays.asList(2, 7, 10);

    /**
     * 发牌
     *
     * @param copy
     * @param tiaoPai 做的牌
     * @return
     */
    public static synchronized List<List<NxGhzCard>> fapai(List<Integer> copy, List<List<Integer>> tiaoPai, int playerCount) {
        List<List<NxGhzCard>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<NxGhzCard> pai = new ArrayList<>();
        int j = 1;
        int testcount = 0;
        if (GameServerConfig.isDebug() && tiaoPai != null && !tiaoPai.isEmpty()) {
            for (List<Integer> zp : tiaoPai) {
                list.add(find(copy, zp));
                testcount += zp.size();
            }
            if (list.size() == 3) {
                list.add(toGhzCards(copy));
                return list;
            } else if (list.size() == playerCount + 1) {
                return list;
            }
        }
        List<Integer> copy2 = new ArrayList<>(copy);
        int fapaiCount = 19 * playerCount + 1 - testcount;// 庄家20张 闲家19张
        if (pai.size() >= 20) {
            list.add(pai);
            pai = new ArrayList<>();
        }
        boolean test = false;
        if (list.size() > 0) {
            test = true;
        }
        for (int i = 0; i < fapaiCount; i++) {
            NxGhzCard majiang = NxGhzCard.getPaohzCard(copy.get(i));
            copy2.remove((Object) copy.get(i));
            if (test) {
                if (i < j * 19) {
                    pai.add(majiang);
                } else {
                    list.add(pai);
                    pai = new ArrayList<>();
                    pai.add(majiang);
                    j++;
                }
            } else {
                if (i <= j * 19) {
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
        List<NxGhzCard> left = new ArrayList<>();
        for (int i = 0; i < copy2.size(); i++) {
            left.add(NxGhzCard.getPaohzCard(copy2.get((i))));
        }
        list.add(left);// 剩下的牌
        return list;
    }

    /**
     * 检查牌是否有重复
     *
     * @param majiangs
     * @return
     */
    public static boolean isGuihuziRepeat(List<NxGhzCard> majiangs) {
        if (majiangs == null) {
            return false;
        }
        Map<Integer, Integer> map = new HashMap<>();
        for (NxGhzCard mj : majiangs) {
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
    public static List<NxGhzCard> findRedGhzs(List<NxGhzCard> copy) {
        List<NxGhzCard> find = new ArrayList<>();
        for (NxGhzCard card : copy) {
            if (c2710List.contains(card.getPai())) {
                find.add(card);
            }
        }
        return find;
    }

    /**
     * 找出小牌
     *
     * @param copy
     * @return
     */
    public static List<NxGhzCard> findSmallGhzs(List<NxGhzCard> copy) {
        List<NxGhzCard> find = new ArrayList<>();
        for (NxGhzCard card : copy) {
            if (!card.isBig()) {
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
    public static List<NxGhzCard> findGhzByVal(List<NxGhzCard> copy, int val) {
        List<NxGhzCard> list = new ArrayList<>();
        for (NxGhzCard phz : copy) {
            if (phz.getVal() == val) {
                list.add(phz);
            }
        }
        return list;
    }

    private static List<NxGhzCard> find(List<Integer> copy, List<Integer> valList) {
        List<NxGhzCard> pai = new ArrayList<>();
        if (!valList.isEmpty()) {
            for (int zpId : valList) {
                Iterator<Integer> iterator = copy.iterator();
                while (iterator.hasNext()) {
                    int card = iterator.next();
                    NxGhzCard phz = NxGhzCard.getPaohzCard(card);
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
    public static Map<Integer, Integer> toGhzValMap(List<NxGhzCard> phzs) {
        Map<Integer, Integer> ids = new HashMap<>();
        if (phzs == null) {
            return ids;
        }
        for (NxGhzCard phz : phzs) {

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
    public static List<Integer> toGhzRepeatVals(List<NxGhzCard> phzs) {
        List<Integer> ids = new ArrayList<>();
        if (phzs == null) {
            return ids;
        }
        for (NxGhzCard phz : phzs) {
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
    public static List<NxGhzCard> toGhzCards(List<Integer> phzIds) {
        List<NxGhzCard> cards = new ArrayList<>();
        for (int id : phzIds) {
            cards.add(NxGhzCard.getPaohzCard(id));
        }
        return cards;
    }

    /**
     * 牌转化为Id
     *
     * @param phzs
     * @return
     */
    public static List<Integer> toGhzCardZeroIds(List<?> phzs) {
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
    public static List<Integer> toGhzCardIds(List<NxGhzCard> phzs) {
        List<Integer> ids = new ArrayList<>();
        if (phzs == null) {
            return ids;
        }
        for (NxGhzCard phz : phzs) {
            ids.add(phz.getId());
        }
        return ids;
    }

    /**
     * 牌转化为门子Id
     *
     * @return
     */
    public static List<String> toGhzMenziIds(List<NxGhzMenzi> ghzs, String delimiter) {
        List<String> ids = new ArrayList<>();
        if (ghzs == null) {
            return ids;
        }
        for (NxGhzMenzi ghz : ghzs) {
            ids.add(ghz.getMenzi().get(0) + delimiter + ghz.getMenzi().get(1));
        }
        return ids;
    }

    /**
     * 胡子转化为牌
     */
    public static List<Integer> toGhzCardVals(List<NxGhzCard> phzs, boolean matchCase) {
        List<Integer> majiangIds = new ArrayList<>();
        if (phzs == null) {
            return majiangIds;
        }
        for (NxGhzCard card : phzs) {
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
    public static NxGhzCardIndexArr getMax(List<NxGhzCard> list) {
        NxGhzCardIndexArr card_index = new NxGhzCardIndexArr();
        Map<Integer, List<NxGhzCard>> phzMap = new HashMap<>();
        for (NxGhzCard phzCard : list) {
            List<NxGhzCard> count;
            if (phzMap.containsKey(phzCard.getVal())) {
                count = phzMap.get(phzCard.getVal());
            } else {
                count = new ArrayList<>();
                phzMap.put(phzCard.getVal(), count);
            }
            count.add(phzCard);
        }
        for (int phzVal : phzMap.keySet()) {
            List<NxGhzCard> phzList = phzMap.get(phzVal);
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
    public static List<NxGhzCard> checkChi(List<NxGhzCard> handCards, NxGhzCard disCard) {
        int disVal = disCard.getVal();
//		int otherVal = disCard.getOtherVal();

        //List<Integer> chi0 = new ArrayList<>(Arrays.asList(disVal, otherVal));
        //List<Integer> chi4 = new ArrayList<>(Arrays.asList(otherVal, otherVal));
        List<Integer> chi1 = new ArrayList<>(Arrays.asList(disVal - 2, disVal - 1));
        List<Integer> chi2 = new ArrayList<>(Arrays.asList(disVal - 1, disVal + 1));
        List<Integer> chi3 = new ArrayList<>(Arrays.asList(disVal + 1, disVal + 2));
        List<List<Integer>> chiList = new ArrayList<>();
        //chiList.add(chi0);
        //chiList.add(chi4);
        chiList.add(chi1);
        chiList.add(chi2);
        chiList.add(chi3);

        // // 不区分大小写 找到值
        // List<PaohzCard> vals = findCountByVal(handCards, disCard, false);
        // if (vals != null && vals.size() >= 2) {
        // int needCards = 3;
        // if (!vals.contains(disCard)) {
        // // 只能是3个牌
        // vals.add(disCard);
        // }
        //
        // if (vals.size() == needCards) {
        // if (isSameCard(vals)) {
        // // 不能是一样的牌
        // return new ArrayList<PaohzCard>();
        // }
        // return vals;
        //
        // } else if (vals.size() > needCards) {
        // List<PaohzCard> list = new ArrayList<>();
        // if (needCards == 3) {
        // list.add(disCard);
        // needCards = 2;
        // }
        // int k = 0;
        // int s = 1;
        // for (int i = 0; i < vals.size(); i++) {
        // PaohzCard card = vals.get(i);
        // if (!list.contains(card)) {
        // if (card.getId() == disCard.getId()) {
        // if (s >= 2) {
        // continue;
        // } else {
        // s++;
        //
        // }
        // }
        //
        // k++;
        // list.add(card);
        //
        // }
        //
        // if (k >= needCards) {
        // break;
        // }
        // }
        // if (isSameCard(list)) {
        // // 不能是一样的牌
        // return new ArrayList<PaohzCard>();
        // }
        // return list;
        // }
        // return new ArrayList<PaohzCard>();
        // }
        // 检查2 7 10

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

        List<NxGhzCard> copy = new ArrayList<>(handCards);
        copy.remove(disCard);

        for (List<Integer> chi : chiList) {
            List<NxGhzCard> findList = findGhzCards(copy, chi);
            if (!findList.isEmpty()) {
                return findList;
            }

        }

        // List<Integer> ids = toPhzCardVals(copy, true);
        // for (List<Integer> chi : chiList) {
        // for (int chiVal : chi) {
        // for (int id : ids) {
        // if (chiVal == id) {
        //
        // }
        // }
        // }
        // if (ids.containsAll(chi)) {
        // return findByVals(handCards, chi);
        // }
        // }

        return new ArrayList<NxGhzCard>();
    }

    public static List<NxGhzCard> findGhzCards(List<NxGhzCard> cards, List<Integer> vals) {
        List<NxGhzCard> findList = new ArrayList<>();
        for (int chiVal : vals) {
            boolean find = false;
            for (NxGhzCard card : cards) {
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
    public static List<NxGhzCard> getSameCards(List<NxGhzCard> handCards, NxGhzCard disCard) {
        List<NxGhzCard> list = findCountByVal(handCards, disCard, true);
        if (list != null) {
            return list;
        }
        return null;

    }

    /**
     * 是否一样的牌
     */
    public static boolean isSameCard(List<NxGhzCard> handCards) {
        int val = 0;
        for (NxGhzCard card : handCards) {
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
    public static void removePhzByVal(List<NxGhzCard> handCards, int cardVal) {
        Iterator<NxGhzCard> iterator = handCards.iterator();
        while (iterator.hasNext()) {
            NxGhzCard paohzCard = iterator.next();
            if (paohzCard.getVal() == cardVal) {
                iterator.remove();
            }

        }
    }

    /**
     * 是否有这张相同的牌
     */
    public static boolean isHasCardVal(List<NxGhzCard> handCards, int cardVal) {
        for (NxGhzCard card : handCards) {
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
    public static List<NxGhzCard> findCountByVal(List<NxGhzCard> phzList, Object o, boolean matchCase) {
        int val;
        if (o instanceof NxGhzCard) {
            if (matchCase) {
                val = ((NxGhzCard) o).getVal();
            } else {
                val = ((NxGhzCard) o).getPai();
            }
        } else if (o instanceof Integer) {
            val = (int) o;
        } else {
            return null;
        }
        List<NxGhzCard> result = new ArrayList<>();
        for (NxGhzCard card : phzList) {
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

    public static List<NxGhzCard> findByVals(List<NxGhzCard> majiangs, List<Integer> vals) {
        List<NxGhzCard> result = new ArrayList<>();
        for (int val : vals) {
            for (NxGhzCard majiang : majiangs) {
                if (majiang.getVal() == val) {
                    result.add(majiang);
                    break;
                }
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
    public static List<NxGhzCard> getVals(List<NxGhzCard> copy, int val, NxGhzCard... exceptCards) {
        List<NxGhzCard> list = new ArrayList<>();
        Iterator<NxGhzCard> iterator = copy.iterator();
        while (iterator.hasNext()) {
            NxGhzCard phz = iterator.next();
            if (exceptCards != null) {
                // 有某些除外的牌不能算在内
                boolean findExcept = false;
                for (NxGhzCard except : exceptCards) {
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
     * 得到某个值的鬼胡子牌
     *
     * @param copy 手牌
     */
    public static NxGhzCard getVal(List<NxGhzCard> copy, int val, NxGhzCard... exceptCards) {
        for (NxGhzCard phz : copy) {
            if (exceptCards != null) {
                // 有某些除外的牌不能算在内
                boolean findExcept = false;
                for (NxGhzCard except : exceptCards) {
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

    public static void sortMin(List<NxGhzCard> hasPais) {
        Collections.sort(hasPais, new Comparator<NxGhzCard>() {

            @Override
            public int compare(NxGhzCard o1, NxGhzCard o2) {
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

    // 拆牌
    public static boolean chaipai(NxGhzHuLack lack, List<NxGhzCard> hasPais) {
        if (hasPais.isEmpty()) {
            return true;
        }
        boolean hu = chaishun(lack, hasPais);
        if (hu)
            return true;
        return false;
    }

    public static boolean chaiSame(NxGhzHuLack lack, List<NxGhzCard> hasPais, NxGhzCard minCard, List<NxGhzCard> sameList) {
        if (sameList.size() < 3) {// 小于3张牌没法拆
            return false;
        } else if (sameList.size() == 3) {// 大小加一起正好3张牌
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
                NxGhzHuLack copyLack = lack.clone();
                List<NxGhzCard> copyHasPais = new ArrayList<>(hasPais);
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
    private static boolean chaikan(NxGhzHuLack lack, List<NxGhzCard> hasPais, NxGhzCard minCard) {
        List<NxGhzCard> sameValList = findCountByVal(hasPais, minCard, true);
        if (sameValList.size() == 3) {
            boolean chaiSame = chaishun3(lack, hasPais, minCard, false, false, sameValList.get(0).getVal(), sameValList.get(1).getVal(), sameValList.get(2).getVal());
            if (chaiSame) {
                return true;
            }
        }
        return false;
    }

    private static boolean chaiSame0(NxGhzHuLack lack, List<NxGhzCard> hasPais, NxGhzCard minCard) {
        List<NxGhzCard> sameList = findCountByVal(hasPais, minCard, true);
        if (sameList.size() < 3) {
            return false;
        }
        // 拆相同
        NxGhzHuLack copyLack = lack.clone();
        List<NxGhzCard> copyHasPais = new ArrayList<>(hasPais);
        if (chaikan(copyLack, copyHasPais, minCard)) {
            lack.copy(copyLack);
            return true;
        }
// 		拆相同2
//		boolean chaiSame = chaiSame(copyLack, copyHasPais, minCard, sameList);
//		if (chaiSame) {
//			lack.copy(copyLack);
//			return true;
//		}
        return false;
    }

    /**
     * 拆顺
     */
    public static boolean chaishun(NxGhzHuLack lack, List<NxGhzCard> hasPais) {
        if (hasPais.isEmpty()) {
            return true;
        }
        sortMin(hasPais);
        NxGhzCard minCard = hasPais.get(0);
        int minVal = minCard.getVal();
        int minPai = minCard.getPai();
        boolean isTryChaiSame = false;
        NxGhzHuLack copyLack = null;
        List<NxGhzCard> copyHasPais = null;
        // 优先拆坎子
        if (minPai != 2) {// 如果不是2 尝试拆坎子
            isTryChaiSame = true;
            boolean isHu = chaiSame0(lack, hasPais, minCard);
            if (isHu) {
                return true;
            }
        } else {// 为 2 7 10 则拆顺
            isTryChaiSame = true;
            boolean isHu = chaiSame0(lack, hasPais, minCard);
            if (isHu) {
                return true;
            } else {
                copyLack = lack.clone();
                copyHasPais = new ArrayList<>(hasPais);
            }
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
            NxGhzHuLack copyLack2 = lack.clone();
            List<NxGhzCard> copyHasPais2 = new ArrayList<>(hasPais);
            chaishun = chaiShun(copyLack2, copyHasPais2, minCard, true, check2710, pai1, pai7, pai10);
            if (chaishun) {
                lack.copy(copyLack2);
            } else {// 拆2 7 10组合失败
                chaishun = chaiShun(lack, hasPais, minCard, true, false, pai1, pai2, pai3);
            }
        } else {// 拆顺
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

    public static boolean chaiShun(NxGhzHuLack lack, List<NxGhzCard> hasPais, NxGhzCard minCard, boolean isShun, boolean check2710, int pai1, int pai2, int pai3) {
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

    private static boolean chaiWuXiPingShun(NxGhzHuLack lack, List<NxGhzCard> hasPais, NxGhzCard minCard, int pai1, int pai2, int pai3) {
        List<Integer> lackList = new ArrayList<>();
        NxGhzCard num1 = getVal(hasPais, pai1);
        NxGhzCard num2 = getVal(hasPais, pai2, num1);
        NxGhzCard num3 = getVal(hasPais, pai3, num1, num2);
        // 找到一句话的
        List<NxGhzCard> hasMajiangList = new ArrayList<>();
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
        if (lackNum > 0) {// 一句话缺牌个数
            return false;
        } else {// 可以一句话
            int huxi = 0;
            int action = NxGhzDisAction.action_shun;
            lack.addPhzHuCards(action, toGhzCardIds(hasMajiangList), huxi);
            lack.changeHuxi(huxi);
            hasPais.removeAll(hasMajiangList);
            return wuPingXiChaiShun(lack, hasPais);
        }
    }

    private static boolean chaishun3(NxGhzHuLack lack, List<NxGhzCard> hasPais, NxGhzCard minCard, boolean isShun, boolean check2710, int pai1, int pai2, int pai3) {
        int minVal = minCard.getVal();
        List<Integer> lackList = new ArrayList<>();
        NxGhzCard num1 = getVal(hasPais, pai1);
        NxGhzCard num2 = getVal(hasPais, pai2, num1);
        NxGhzCard num3 = getVal(hasPais, pai3, num1, num2);
        // 找到一句话的
        List<NxGhzCard> hasMajiangList = new ArrayList<>();
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
        if (lackNum > 0) {// 一句话缺牌个数
            if (lackNum >= 2) {
                List<NxGhzCard> count = getVals(hasPais, hasMajiangList.get(0).getVal());
                if (count.size() == 3) {// 直接做坎子
                    lack.addLack(count.get(0).getVal());
                    hasPais.removeAll(count);
                    lack.addPhzHuCards(NxGhzDisAction.action_kang, toGhzCardIds(count), 1);
                    lack.changeHuxi(1);
                    return chaipai(lack, hasPais);
                }
            }
//			else if (lackNum == 1) {
//				if (check2710) {
//					lack.addFail2710Val(minVal);
//					return chaipai(lack, hasPais);
//				}
//			}
            if (check2710) {
                lack.addFail2710Val(minVal);
                return chaipai(lack, hasPais);
            }
            return false;
        } else {// 可以一句话
            int huxi = 0;
            int action = NxGhzDisAction.action_shun;
            if (isShun) {
                int minPai = minCard.getPai();
                if (minPai == 2) {// 2710 加胡息
                    huxi = getShunHuxi(hasMajiangList);
                }
            } else {
                if (isSameCard(hasMajiangList)) {
                    // 如果是三个一模一样的
                    action = NxGhzDisAction.action_kang;
//					if (lack.isSelfMo()) {
//						action = YjGhzDisAction.action_kang;
//					}
                    huxi = getYingHuxi(action, hasMajiangList);
                }
            }
            lack.addPhzHuCards(action, toGhzCardIds(hasMajiangList), huxi);
            lack.changeHuxi(huxi);
            hasPais.removeAll(hasMajiangList);
            return chaipai(lack, hasPais);
        }
    }

    /**
     * 去除重复后得到val的集合
     *
     * @param cards
     * @return
     */
    public static Map<Integer, Integer> getDistinctVal(List<NxGhzCard> cards) {
        Map<Integer, Integer> valIds = new HashMap<>();
        if (cards == null) {
            return valIds;
        }
        for (NxGhzCard phz : cards) {
            if (valIds.containsKey(phz.getVal())) {
                valIds.put(phz.getVal(), valIds.get(phz.getVal()) + 1);
            } else {
                valIds.put(phz.getVal(), 1);
            }
        }
        return valIds;
    }

//	/**
//	 * 获取牌型息数
//	 * @param action
//	 * @param cards
//	 * @return
//	 */
//	public static int getHuxi(int action, List<YjGhzCard> cards) {
//		int huxi = 0;
//		if (action == 0) {
//			return huxi;
//		}
//		if (action == YjGhzDisAction.action_liu) {
//			Map<Integer, Integer> valmap = getDistinctVal(cards);
//			for (Entry<Integer, Integer> entry : valmap.entrySet()) {
//				if (entry.getValue() != 4) {
//					continue;
//				}
//				huxi += 1;
//			}
//		} else if (action == YjGhzDisAction.action_chi) {// 吃 只有2710 算1息
//			List<YjGhzCard> copy = new ArrayList<>(cards);
//			sortMin(copy);
//			huxi = getShunHuxi(copy);
//		} else if (action == YjGhzDisAction.action_peng) {
//			Map<Integer, Integer> valmap = getDistinctVal(cards);
//			for (Entry<Integer, Integer> entry : valmap.entrySet()) {
//				if (entry.getValue() != 3) {
//					continue;
//				}
//				huxi += 1;
//			}
//		} else if (action == YjGhzDisAction.action_wei) {
//			Map<Integer, Integer> valmap = getDistinctVal(cards);
//			for (Entry<Integer, Integer> entry : valmap.entrySet()) {
//				if (entry.getValue() != 3) {
//					continue;
//				}
//				huxi += 1;
//			}
//
//		}
//		return huxi;
//	}

    /**
     * 算硬息
     *
     * @param action 溜（四张）：1息
     *               飘（四张）：1息
     *               偎（三张）：1息
     *               坎（三张）：1息
     *               碰（三张）：1息
     * @param cards
     * @return
     */
    public static int getYingHuxi(int action, List<NxGhzCard> cards) {
        int huxi = 0;
        if (action == 0) {
            return huxi;
        }
        if (action == NxGhzDisAction.action_liu || action == NxGhzDisAction.action_piao || action == NxGhzDisAction.action_weiHouLiu) {
            Map<Integer, Integer> valmap = getDistinctVal(cards);
            for (Entry<Integer, Integer> entry : valmap.entrySet()) {
                if (entry.getValue() != 4) {
                    continue;
                }
                huxi += 1;
            }
        } else if (action == NxGhzDisAction.action_chi || action == NxGhzDisAction.action_shun) {// 吃 只有2710 算1息
            List<NxGhzCard> copy = new ArrayList<>(cards);
            sortMin(copy);
            huxi = getShunHuxi(copy);
        } else if (action == NxGhzDisAction.action_peng || action == NxGhzDisAction.action_wei || action == NxGhzDisAction.action_kang) {
            Map<Integer, Integer> valmap = getDistinctVal(cards);
            for (Entry<Integer, Integer> entry : valmap.entrySet()) {
                if (entry.getValue() != 3) {
                    continue;
                }
                huxi += 1;
            }
        }
        return huxi;
    }

    /**
     * 算顺子的胡息 2710  1息
     *
     * @param hasMajiangList
     * @return
     */
    private static int getShunHuxi(List<NxGhzCard> hasMajiangList) {
        boolean isSamePai = true;
        int minPai = 0;
        for (NxGhzCard card : hasMajiangList) {
            if (minPai == 0) {
                minPai = card.getPai();
                continue;
            }
            if (minPai != card.getPai()) {
                isSamePai = false;
                break;
            }

        }
        if (isSamePai) {
            return 0;
        }
        NxGhzCard minCard = hasMajiangList.get(0);
        if (minCard.getPai() == 2) {
            for (NxGhzCard card : hasMajiangList) {
                if (!c2710List.contains(card.getPai())) {
                    return 0;
                }
            }
            return 1;
        }
        return 0;
    }

    public static void addHuLackMenzi(NxGhzMenzi menzi, List<Integer> menziIds, NxGhzHuLack lack) {
        if (menzi.getType() == 1) {
            lack.addPhzHuCards(NxGhzDisAction.action_kang, menziIds, 1);
            lack.changeHuxi(1);
        } else if (menzi.getType() == 2) {
            lack.addPhzHuCards(NxGhzDisAction.action_shun, menziIds, 1);
            lack.changeHuxi(1);
        } else {
            lack.addPhzHuCards(NxGhzDisAction.action_shun, menziIds, 0);
        }
    }

    /**
     * 5硬胡息（5砍 5偎） 无息平(起手全是顺子 加伴张) 九对半 项项息(七息)
     * 是否胡牌  判断胡牌时只算硬息
     *
     * @param handCards      当前手上的牌
     * @param disCard        胡的牌
     * @param isSelfMo       是否自己摸
     * @param outYingXiCount 当前出的牌硬息数
     * @param hasWei         是否偎过
     * @return
     */
    public static NxGhzHuLack isHu(List<NxGhzCardTypeHuxi> cardTypes, List<NxGhzCard> handCards, NxGhzCard disCard, boolean isSelfMo, int outYingXiCount, boolean hasWei, boolean wupingxi, boolean isHaiDi, boolean canDiaoDiaoShou) {
        NxGhzHuLack lack = new NxGhzHuLack(0);
        lack.setSelfMo(isSelfMo);
        lack.setCheckCard(disCard);
        lack.setHu(false);
        lack.setHuxi(outYingXiCount);
        List<NxGhzCard> handCardsCopy = new ArrayList<>(handCards);// 手牌
        if (disCard != null) {// 胡的牌
            handCardsCopy.add(0, disCard);
        }
        if (handCardsCopy.size() % 3 != 2) {// 手牌不是模3余2 表示手牌数量有误
            return lack;
        }
        
        NxGhzCardIndexArr shiduiArr = NxGhzTool.getGuihzCardIndexArr(handCardsCopy);
        if (shiduiArr.getDuiziNum() == 10) {// 十对
        	 lack.setHu(true);
        	 return lack;
        }
        
        NxGhzCardIndexArr arr = getGuihzCardIndexArr(handCardsCopy);
        // 项项息(7息)  或者  硬5息(有砍或者有偎)
        // 计算手牌是否能胡 并且是否满足息数
        List<NxGhzHuLack> lackList = new ArrayList<>();
        List<NxGhzMenzi> menzis = arr.getMenzis(true);
        for (NxGhzMenzi menzi : menzis) {// 拆门子
            List<NxGhzCard> copy = new ArrayList<>(handCardsCopy);
            int i = 0;
            List<Integer> menziIds = new ArrayList<>();
            Iterator<NxGhzCard> iterator = copy.iterator();
            while (iterator.hasNext()) {
                NxGhzCard card = iterator.next();
                if (menzi.getMenzi().contains(card.getVal())) {
                    i++;
                    menziIds.add(card.getId());
                    menzi.getMenzi().remove(new Integer(card.getVal()));
                    iterator.remove();
                }
                if (i >= 2) {
                    break;
                }
            }
            NxGhzHuLack lackCopy = new NxGhzHuLack(0);
            lackCopy.setSelfMo(isSelfMo);
            lackCopy.setCheckCard(disCard);
            lackCopy.changeHuxi(lack.getHuxi());
            if (lack.getGhzHuCards() != null) {
                lackCopy.setPhzHuCards(new ArrayList<>());
            }
            NxGhzHuLack lackCopy1 = new NxGhzHuLack(0);
            lackCopy1.setSelfMo(isSelfMo);
            lackCopy1.setCheckCard(disCard);
            lackCopy1.changeHuxi(lack.getHuxi());
            if (lack.getGhzHuCards() != null) {
                lackCopy1.setPhzHuCards(new ArrayList<>());
            }
            List<NxGhzCard> copy1 = new ArrayList<>(copy);
            boolean hu = chaipai(lackCopy, copy);
            if (disCard != null && menziIds.contains(disCard.getId())) {
                menziIds.remove((Integer) disCard.getId());
                menziIds.add(0, disCard.getId());
            }
            addHuLackMenzi(menzi, menziIds, lackCopy);
            addHuLackMenzi(menzi, menziIds, lackCopy1);
            boolean haswupingxi = wuPingXiChaiShun(lackCopy1, copy1);
            if (hu) {
                lackCopy.setHu(hu);
                lackList.add(lackCopy);
            }
            if (haswupingxi && lackCopy1.getHuxi() == 0) {// 如果有无息平胡
                lackCopy1.setHu(haswupingxi);
                lackCopy1.setHuxi(0);
                lackList.add(lackCopy1);
            }
        }
        boolean hasWupingXi = false;
        NxGhzHuLack wupingxiHuLack = null;
        if (!lackList.isEmpty()) {
            int maxHuxi = 0;
            NxGhzHuLack maxHuxiLack = null;
            for (NxGhzHuLack copy : lackList) {
                if (copy.getHuxi() == 0) {
                    hasWupingXi = true;
                    wupingxiHuLack = copy;
                }
                if (maxHuxi == 0 || copy.getHuxi() > maxHuxi || (maxHuxiLack != null && copy.getHuxi() == maxHuxi && copy.getKangNum() > maxHuxiLack.getKangNum())) {
                    maxHuxi = copy.getHuxi();
                    maxHuxiLack = copy;
                }
            }
            maxHuxiLack.refreshWeiHuCard();
            boolean hasWaiYuan = hasWaiyuanOrDahu(cardTypes, maxHuxiLack, handCardsCopy, isHaiDi, canDiaoDiaoShou);
            if (maxHuxiLack.getHuxi() == 7) {// 项项息 其中包含二七十或贰柒拾的息
                if (maxHuxiLack.contains2710() || (maxHuxiLack.isAllDuizi())) {// 对子胡
                    maxHuxiLack.setHu(true);
                    if (hasWupingXi == true && wupingxi) {
                        maxHuxiLack.setHasWupingXi(true);// 包含无平息
                    }
                    maxHuxiLack.refreshSelfMoCard(isSelfMo, disCard);
                    return maxHuxiLack;
                }
            } else if (maxHuxiLack.getHuxi() >= 5 && maxHuxiLack.getHuxi() < 7) {//  硬5息
                if (maxHuxiLack.getKangNum() > 0 || hasWei || hasWaiYuan) {
                    maxHuxiLack.setHu(true);
                    if (hasWupingXi == true && wupingxi) {// 包含无平息
                        maxHuxiLack.setHasWupingXi(true);
                    }
                    maxHuxiLack.refreshSelfMoCard(isSelfMo, disCard);
                    return maxHuxiLack;
                }
            }
            if (hasWupingXi == true && wupingxi && handCardsCopy.size() == 20) {// 只胡无息平
                wupingxiHuLack.setHasWupingXi(true);
                wupingxiHuLack.setHu(true);
                return wupingxiHuLack;
            }
        }
        return lack;
    }

    public static boolean wuPingXiChaiShun(NxGhzHuLack huLack, List<NxGhzCard> hasPais) {
        if (hasPais.isEmpty()) {
            return true;
        }
        sortMin(hasPais);
        NxGhzCard minCard = hasPais.get(0);
        int minVal = minCard.getVal();
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
        boolean chaishun = false;
        chaishun = chaiWuXiPingShun(huLack, hasPais, minCard, pai1, pai2, pai3);
        return chaishun;
    }

    /**
     * 是否有外圆 或者 大胡
     *
     * @param cardTypes
     * @param hu
     * @param handCards
     * @return
     */
    private static boolean hasWaiyuanOrDahu(List<NxGhzCardTypeHuxi> cardTypes, NxGhzHuLack hu, List<NxGhzCard> handCards, boolean isHaiDi, boolean canDiaoDiaoShou) {
        List<NxGhzCard> allCards = new ArrayList<>();
        List<NxGhzCardTypeHuxi> allHuxiCards = new ArrayList<>();
        for (NxGhzCardTypeHuxi type : cardTypes) {
            allCards.addAll(NxGhzTool.toGhzCards(type.getCardIds()));
            allHuxiCards.add(type);
        }
        if (hu != null && hu.getGhzHuCards() != null) {
            allHuxiCards.addAll(hu.getGhzHuCards());
            for (NxGhzCardTypeHuxi type : hu.getGhzHuCards()) {
                allCards.addAll(NxGhzTool.toGhzCards(type.getCardIds()));
            }
        }
        if (NxGhzMingTangRule.hasWaiYuan(allCards, allHuxiCards, handCards, hu.getCheckCard())) {
            return true;
        }
        if (NxGhzMingTangRule.hasDahu(handCards, allCards, isHaiDi, canDiaoDiaoShou)) {
            return true;
        }
        return false;
    }

    /**
     * 获取手牌里面各个牌的个数
     *
     * @param handPais
     * @return
     */
    public static NxGhzCardIndexArr getGuihzCardIndexArr(List<NxGhzCard> handPais) {
        List<NxGhzCard> copy = new ArrayList<>(handPais);
        NxGhzCardIndexArr valArr = NxGhzTool.getMax(copy);
        return valArr;
    }

    /**
     * 将array组合成用delimiter分隔的字符串
     *
     * @return String
     */
    public static List<NxGhzCard> explodeGhz(String str, String delimiter) {
        List<NxGhzCard> list = new ArrayList<>();
        if (StringUtils.isBlank(str) || str.equals("null") || str.equals("undefined"))
            return list;
        String strArray[] = str.split(delimiter);

        for (String val : strArray) {
            NxGhzCard phz = null;
            if (val.startsWith("mj")) {
                phz = NxGhzCard.valueOf(NxGhzCard.class, val);
            } else {
                phz = NxGhzCard.getPaohzCard((Integer.valueOf(val)));
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
    public static String implodeGhz(List<NxGhzCard> array, String delimiter) {
        if (array == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder("");
        for (NxGhzCard i : array) {
            sb.append(i.getId());
            sb.append(delimiter);
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public static List<String> buildClosingInfoResLog(ClosingGhzInfoRes res) {
        List<String> list = new ArrayList<>();
        for (ClosingGhzPlayerInfoRes info : res.getClosingPlayersList()) {
            Map<String, Object> map = new HashMap<>();
            for (Entry<FieldDescriptor, Object> entry : info.getAllFields().entrySet()) {
                if (entry.getValue() instanceof List) {
                    List<Object> l = new ArrayList<>();
                    for (Object o : (List<?>) entry.getValue()) {
                        if (o instanceof String) {
                            l = (List<Object>) entry.getValue();
                            break;
                        } else if (o instanceof Integer) {
                            l = (List<Object>) entry.getValue();
                            break;
                        } else if (o instanceof Long) {
                            l = (List<Object>) entry.getValue();
                            break;
                        } else if (o instanceof GeneratedMessage) {
                            l.add(buildGhzClosingInfoResOtherLog((GeneratedMessage) o));
                        }
                    }
                    map.put(entry.getKey().getName(), l);
                } else {
                    map.put(entry.getKey().getName(), entry.getValue());
                }
            }
            list.add(JacksonUtil.writeValueAsString(map));
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> buildGhzClosingInfoResOtherLog(GeneratedMessage res) {
        Map<String, Object> map = new HashMap<>();
        for (Entry<FieldDescriptor, Object> entry : res.getAllFields().entrySet()) {
            String name = entry.getKey().getName();
            if (!name.equals("closingPlayers")) {
                if (entry.getValue() instanceof List) {
                    List<Object> l = new ArrayList<>();
                    for (Object o : (List<?>) entry.getValue()) {
                        if (o instanceof String) {
                            l = (List<Object>) entry.getValue();
                            break;
                        } else if (o instanceof Integer) {
                            l = (List<Object>) entry.getValue();
                            break;
                        } else if (o instanceof Long) {
                            l = (List<Object>) entry.getValue();
                            break;
                        } else if (o instanceof GeneratedMessage) {
                            l.add(buildClosingInfoResOtherLog((GeneratedMessage) o));
                        }
                    }
                    map.put(entry.getKey().getName(), l);

                } else {
                    map.put(entry.getKey().getName(), entry.getValue());
                }
            }
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> buildClosingInfoResOtherLog(GeneratedMessage res) {
        Map<String, Object> map = new HashMap<>();
        for (Entry<FieldDescriptor, Object> entry : res.getAllFields().entrySet()) {
            String name = entry.getKey().getName();
            if (!name.equals("closingPlayers")) {
                if (entry.getValue() instanceof List<?>) {
                    List<Object> l = new ArrayList<>();
                    for (Object o : (List<?>) entry.getValue()) {
                        if (o instanceof String) {
                            l = (List<Object>) entry.getValue();
                            break;
                        } else if (o instanceof Integer) {
                            l = (List<Object>) entry.getValue();
                            break;
                        } else if (o instanceof Long) {
                            l = (List<Object>) entry.getValue();
                            break;
                        } else if (o instanceof GeneratedMessage) {
                            l.add(buildClosingInfoResOtherLog((GeneratedMessage) o));
                        }
                    }
                    map.put(entry.getKey().getName(), l);
                } else {
                    map.put(entry.getKey().getName(), entry.getValue());
                }
            }
        }
        return map;
    }

    public static boolean contains2710(List<NxGhzCardTypeHuxi> ghzHuCards) {
        for (NxGhzCardTypeHuxi cardType : ghzHuCards) {
            List<NxGhzCard> cards = NxGhzTool.toGhzCards(cardType.getCardIds());
            List<Integer> cardPais = NxGhzTool.toGhzCardVals(cards, false);
            if (cardPais.contains(2) && cardPais.contains(7) && cardPais.contains(10)) {
                return true;
            } else {
                if (!NxGhzTool.isSameCard(cards) && NxGhzTool.c2710List.containsAll(cardPais)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 自动出牌
     */
    public static NxGhzCard autoDisCard(List<NxGhzCard> handPais, NxGhzPlayer player) {
        List<NxGhzCard> copy = new ArrayList<>(handPais);
        NxGhzCardIndexArr valArr = getMax(copy);
        NxGhzIndex index1 = valArr.getPaohzCardIndex(1);
        NxGhzIndex index0 = valArr.getPaohzCardIndex(0);

        NxGhzIndex index2 = valArr.getPaohzCardIndex(2);
        List<Integer> values = new ArrayList<>();

        if (index0 != null) {
            values.addAll(index0.getValList());
        }
        if (index1 != null) {
            values.addAll(index1.getValList());
        }
        if (index2 != null) {
            values.addAll(index2.getValList());
        }
        NxGhzCard disC = null;

        for (Integer value : values) {

            NxGhzCard card = getdiscards(handPais, value);
            if (card == null) {
                continue;
            }
            if (player.getHasPengOrWeiPais(null).contains(card.getVal())) {
                continue;
            }
            if (player.getHasChiMenzi(null, card)) {
                continue;
            }
            disC = card;
            break;

        }
        return disC;
    }

    public static NxGhzCard getdiscards(List<NxGhzCard> handPais, int val) {
        NxGhzCard card = null;
        for (NxGhzCard paohzCard : handPais) {
            if (paohzCard.getVal() == val) {
                card = paohzCard;
                break;
            }
        }
        return card;
    }

    public static List<NxGhzCard> getTingZps(List<NxGhzCard> handPais, NxGhzPlayer player) {

//        boolean hasWei = player.getWei() != null && player.getWei().size() > 0;
        boolean hasWei = true;
        int outYingxiCount = player.getOutYingXiCount();
        NxGhzTable table = player.getPlayingTable(NxGhzTable.class);
        List<NxGhzCard> huCards = new ArrayList<>();
        if (table == null) {
            return huCards;
        }
        boolean isHaiDi = true;
        boolean canDiaoDiaoShou = table.isDiaodiaoshou();
        Set<Integer> checkedHuVal = new HashSet<>();
        for (NxGhzCard card : NxGhzConstant.checkTingList) {
            if (checkedHuVal.contains(card.getVal())) {
                continue;
            }
            NxGhzHuLack lack = NxGhzTool.isHu(player.getCardTypes(), handPais, card, true, outYingxiCount, hasWei, table.isWuxiping(), isHaiDi, canDiaoDiaoShou);
            if (lack.isHu()) {
                huCards.add(card);
                checkedHuVal.add(card.getVal());
            }
        }
        return huCards;
    }

    private static boolean checkHuCard(List<NxGhzCard> huCards, NxGhzCard card) {
        if (huCards.size() == 0) {
            return true;
        }
        for (NxGhzCard huC : huCards) {
            if (huC.getVal() == card.getVal()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 找出相同的值
     *
     * @param copy
     * @param val
     * @return
     */
    public static List<NxGhzCard> findPhzByVal(List<NxGhzCard> copy, int val) {
        List<NxGhzCard> list = new ArrayList<>();
        for (NxGhzCard phz : copy) {
            if (phz.getVal() == val) {
                list.add(phz);
            }
        }
        return list;
    }


    public static void main(String[] args) {
        //1,1,1,2,2,2,3,3,3,4,5,6,7,8,8,8,9,9,9,101;
//        List<Integer> vals = Arrays.asList(1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 107, 102, 110);//102,103,

//		List<YjGhzCard> test_chi1 = toGhzCards(new ArrayList<>(Arrays.asList(101,102,102,103,103,107,108,109,2,3,4,7,7,7,8,8,8,9,9,9)));//61, 51, 71, 1, 11, 21, 21, 2, 12, 52, 62, 72, 56, 66, 76
//        List<YjGhzCard> cards = new ArrayList<>();
//        for (int val : vals) {
//            for (YjGhzCard card : YjGhzCard.values()) {
//                if (card.getVal() == val && !cards.contains(card)) {
//                    cards.add(card);
//                    break;
//                }
//            }
//        }
        //		System.out.println(test_chi1);
//        YjGhzCard disCard = YjGhzCard.getPaohzCard(16);
//        List<YjGhzCardTypeHuxi> cardhuxis = new ArrayList<>();
//		YjGhzHuLack lack = isHu(cardhuxis, test_chi1, disCard, true, 0, false, true);
//		System.out.println("是否胡牌-->" + lack.isHu() + " 胡息:" + lack.getHuxi() + " " + lack.getGhzHuCards().size() + " " + JacksonUtil.writeValueAsString(lack.getGhzHuCards()));
//		List<YjGhzCard> test_chi2 = toGhzCards(new ArrayList<>(Arrays.asList(5,6)));
//		List<YjGhzCard> chi = checkChi(test_chi2, YjGhzCard.getPaohzCard(4));
//		System.out.println(chi);
//        YjGhzHuLack huLack = new YjGhzHuLack(0);
//        boolean wupingxi = wuPingXiChaiShun(huLack, cards);
//        System.out.println("wupingxi:" + wupingxi + "--huxi:" + huLack.getHuxi());

        testFaPai();
    }

    public static void testFaPai() {
        List<Integer> copy = new ArrayList<>(NxGhzConstant.cardList);
        List<List<NxGhzCard>> list = NxGhzTool.fapai(copy, null, 2);
        System.out.println(list);
    }
}
