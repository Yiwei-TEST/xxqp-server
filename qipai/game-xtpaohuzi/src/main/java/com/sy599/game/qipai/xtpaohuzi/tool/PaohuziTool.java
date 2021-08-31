package com.sy599.game.qipai.xtpaohuzi.tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.xtpaohuzi.bean.GameModel;
import com.sy599.game.qipai.xtpaohuzi.bean.PaohuziHandCard;
import com.sy599.game.qipai.xtpaohuzi.bean.PaohuziHuBean;
import com.sy599.game.qipai.xtpaohuzi.bean.PaohzDisAction;
import com.sy599.game.qipai.xtpaohuzi.constant.PaohuziConstant;
import com.sy599.game.qipai.xtpaohuzi.constant.PaohzCard;
import com.sy599.game.qipai.xtpaohuzi.rule.PaohuziIndex;
import com.sy599.game.qipai.xtpaohuzi.rule.PaohuziMingTangRule;
import com.sy599.game.qipai.xtpaohuzi.rule.PaohzCardIndexArr;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.LogUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

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

    /**
     * @param initAllCardsCombo 初始卡组
     * @param gmOptCards        后台设定初始发牌
     * @return
     * @description 新的发牌逻辑, 屏蔽起手三提五坎的输出
     * @author Guang.OuYang
     * @date 2019/9/28
     */
    public static List<List<PaohzCard>> getAllSeatAllCardNew(List<Integer> initAllCardsCombo, List<List<Integer>> gmOptCards, GameModel gameModel) {
        long t = System.currentTimeMillis();

        //0庄家的牌,1闲家,2闲家,3剩余牌
        List<List<PaohzCard>> allSeatAllCards = new ArrayList<>();
        Collections.shuffle(initAllCardsCombo);
        List<PaohzCard> seatCards = new ArrayList<>();
        int j = 1;

        if (GameServerConfig.isDebug() && gmOptCards != null && !gmOptCards.isEmpty()) {
            for (List<Integer> zp : gmOptCards) {
                if (!CollectionUtils.isEmpty(zp)) {
                    allSeatAllCards.add(find(initAllCardsCombo, zp));
                }
                j += 1;
            }

            if (allSeatAllCards.size() == 3) {
                allSeatAllCards.add(toPhzCards(initAllCardsCombo));
                System.out.println(JacksonUtil.writeValueAsString(allSeatAllCards));
                return allSeatAllCards;
            } else if (allSeatAllCards.size() == 4) {
                return allSeatAllCards;
            }
        }

        //剩余底牌
        List<Integer> leftCards = new ArrayList<>(initAllCardsCombo);

        //剩余卡组
        LinkedList<Integer> leftCardsCombo = new LinkedList(initAllCardsCombo);

        //总发牌数
        int totalSeat = 3;
        //庄家比闲家多的牌
        int mainAddCardCount = 1;
        //闲家牌数量
        int optSeatCardCount = 20;
        //庄家牌数量
        int mainSeatCardCount = optSeatCardCount + mainAddCardCount;
        //总次数等于 20*3+1, 如测试牌数存在时,首位认为是庄家
        int totalCardCount = optSeatCardCount * totalSeat + mainAddCardCount - (j > 1 ? (j - 1) * optSeatCardCount + mainAddCardCount : 0);
        //统计全部卡组其中出现的次数
        Map<Integer, Integer> allCardStatics = new HashMap<>();

        for (int i = 0; i < totalCardCount; i++) {
//            System.out.println(i);
            //全部发放完毕
            if (j > totalSeat) {
                break;
            }

            boolean onlyAdd = j == 1 && mainSeatCardCount > seatCards.size() || (j > 1 && optSeatCardCount > seatCards.size());

            Integer cardId = leftCardsCombo.removeFirst();//copyOnWriteArrayList.get(i);
            //不能出现2提5坎
            //统计每个卡组出现的次数
            allCardStatics.merge(PaohzCard.getPaohzCard(cardId).getVal(), 1, (oV, nV) -> oV + nV);

            //3提5坎的检测
            //不能超出当前玩家的检测范围,规避在极端情况下3个人刚换好的牌数可能
            if (onlyAdd) {
                //可以组成的最大坎数量
                int threeCount = 5;
                //可以组成的最大提数量
                int fourCount = 1;
                Iterator<Entry<Integer, Integer>> iterator = allCardStatics.entrySet().iterator();
                while (iterator.hasNext()) {
                    Entry<Integer, Integer> next = iterator.next();
                    if (next.getValue() > 4) {
                        fourCount = -1;
                    } else if (next.getValue() == 4) {
                        --fourCount;
                    } else if (next.getValue() == 3) {
                        --threeCount;
                    }
                }

                //移除这张牌,同时给出新的一张牌
                if (fourCount < 0 || threeCount < 0) {
                    allCardStatics.merge(PaohzCard.getPaohzCard(cardId).getVal(), -1, (oV, nV) -> oV + nV);
                    totalCardCount += 1;
                    //这里把位置调换到最后去,增加最大索引
                    leftCardsCombo.addLast(cardId);
                    continue;
                }
            }

            // 发牌张数=21*3+1 正好第一个发牌的人21张其他人20张
            leftCards.remove((Object) cardId);

            PaohzCard card = PaohzCard.getPaohzCard(cardId);
            if (onlyAdd) {
                //第一轮多1张这里都属于庄家的牌
                seatCards.add(card);
            } else {
                allSeatAllCards.add(seatCards);
                seatCards = new ArrayList<>();
                seatCards.add(card);
                j++;
//                System.out.println("玩家总牌数:"+j+" "+allCardStatics);
                allCardStatics.clear();
                //不能出现2提5坎
                //统计每个卡组出现的次数
                allCardStatics.merge(PaohzCard.getPaohzCard(cardId).getVal(), 1, (oV, nV) -> oV + nV);
            }
        }
//        System.out.println("玩家总牌数:"+j+" "+allCardStatics);

        allSeatAllCards.add(seatCards);

//        int gmSeatCards = CollectionUtils.isEmpty(gmOptCards) ? -99 : gmOptCards.size();
//        Iterator<List<PaohzCard>> iterator = allSeatAllCards.iterator();
//        while (iterator.hasNext()) {
//            List<PaohzCard> seatCardCombo =  iterator.next();
//            if (--gmSeatCards >= 0) {
//                continue;
//            }
//            //仅系统发出的牌不允许出现听牌
//            leftCards = listenCardBarrier(seatCardCombo, new LinkedList<>(leftCards), gameModel);
//        }

        List<PaohzCard> finalLeft = new ArrayList<>();
        for (int i = 0; i < leftCards.size(); i++) {
            finalLeft.add(PaohzCard.getPaohzCard(leftCards.get((i))));
        }
//
        allSeatAllCards.add(finalLeft);
//
//        LogUtil.msg("SkyFloorHuBarrier totalConsumer:" +  (System.currentTimeMillis() - t)+"ms");

        return allSeatAllCards;
    }


    /**
     * 听牌屏障
     * @param cards     手牌
     * @param leftCards 剩余牌组
     * @return
     * @description
     * @author Guang.OuYang
     * @date 2019/10/9
     */
    public static List<Integer> listenCardBarrier(List<PaohzCard> cards, LinkedList<Integer> leftCards, GameModel gameModel) {
        long t = System.currentTimeMillis();

        //检测手牌中是否有顺子
        List<PaohzCard> handCards = new ArrayList<>();
        handCards.addAll(cards);

        //找到所有牌组中3个以上的组合值val
        PaohzCardIndexArr max = PaohuziTool.getMax(handCards);

        List<PaohzCard> needRve = new ArrayList<>();

        PaohuziIndex[] valCombos = new PaohuziIndex[3];
        valCombos[1] = max.getPaohzCardIndex(2);
        valCombos[2] = max.getPaohzCardIndex(3);

        for (int i = 0; i < valCombos.length; i++) {
            if (valCombos[i] == null) continue;
            needRve.addAll(valCombos[i].getPaohzList());
        }

        //除掉手牌中的3~4个
        handCards.removeAll(needRve);

        //匹配所有顺子组合
        List<PaohuziMingTangRule.FindAnyCombo.FindAny> findCombos = new ArrayList<>();

        //处理所有顺子组合
        if (true) {
//        if (true) {
            //匹配找到所有36种组合
            Integer[][] allCombo = PaohuziMingTangRule.comboAllSerial(gameModel);

            PaohuziMingTangRule.FindAnyComboResult findAnyComboResult = new PaohuziMingTangRule.FindAnyCombo(allCombo).allComboMath(toPhzCardVals(handCards, true).stream());

            findCombos.addAll(findAnyComboResult.getFindCombos());


//            int repeated=findAnyComboResult.getFindCombos().size();
//            Iterator<PaohuziMingTangRule.FindAnyCombo.FindAny> iterator2 = findAnyComboResult.getFindCombos().iterator();
//            ArrayList<PaohzCard> paohzCards = null;
//            do {
//                paohzCards = new ArrayList<>(handCards);
//                //这里用全部组合排除法去除现有组合,优点是相对速度较快,缺点是,有时候不能胡牌,也需要拆牌,总是会保留3张或以上的单牌
//
//                while (iterator2.hasNext()) {
//                    PaohuziMingTangRule.FindAnyCombo.FindAny next = iterator2.next();
//                    next.clearMark();
//                    //一个完整的组合
//                    //以完整组合形式移除牌中剩余牌位12333,能够组合出来的牌型123,333此时先移除一种完整的组合,不完整组合不移除
//                    //这里会存在组合优先级的问题,暂时不用
//                    if (paohzCards.stream().anyMatch(v -> next.in(v.getVal()) && next.checkAllIn())) {
//                        next.clearMark();
//                        Iterator<PaohzCard> iterator4 = paohzCards.iterator();
//                        while (iterator4.hasNext()) {
//                            PaohzCard v = iterator4.next();
//                            if (next.getSrcCount().containsKey(v.getVal()) && next.getSrcCount().get(v.getVal()) > 0 && next.in(v.getVal())) {
//                                iterator4.remove();
//                                if (next.checkAllIn()) {
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                }
//                PaohuziMingTangRule.FindAnyCombo.FindAny remove = findAnyComboResult.getFindCombos().remove(0);
//                ArrayList<PaohuziMingTangRule.FindAnyCombo.FindAny> findAnies = new ArrayList<>(findAnyComboResult.getFindCombos());
//                findAnies.add(remove);
//
//                findAnyComboResult.getFindCombos().clear();
//                findAnyComboResult.getFindCombos().addAll(findAnies);
//                //所有单排组合排列完毕后, 手牌存在提,那么把对子也挪出去
//                if (valCombos[2] != null && max.getPaohzCardIndex(1) != null) {
//                    paohzCards.removeAll(max.getPaohzCardIndex(1).getPaohzList());
//                }
//
//            } while (--repeated <= 0 || paohzCards.size() <= 2);
//
//            handCards=paohzCards;

            //这里用全部组合排除法去除现有组合,优点是相对速度较快,缺点是,有时候不能胡牌,也需要拆牌,总是会保留3张或以上的单牌
            Iterator<PaohuziMingTangRule.FindAnyCombo.FindAny> iterator2 = findAnyComboResult.getFindCombos().iterator();
            while (iterator2.hasNext()) {
                PaohuziMingTangRule.FindAnyCombo.FindAny next = iterator2.next();
                //一个完整的组合
                //以完整组合形式移除牌中剩余牌位12333,能够组合出来的牌型123,333此时先移除一种完整的组合,不完整组合不移除
                //这里会存在组合优先级的问题,暂时不用
                Iterator<PaohzCard> iterator4 = handCards.iterator();
                while (iterator4.hasNext()) {
                    PaohzCard v = iterator4.next();
                    if (next.getSrcCount().containsKey(v.getVal())) {
                        iterator4.remove();
                    }
                }
            }
        }

        //所有单排组合排列完毕后, 手牌存在提,那么把对子也挪出去
        if (valCombos[2] != null && max.getPaohzCardIndex(1) != null) {
            handCards.removeAll(max.getPaohzCardIndex(1).getPaohzList());
        }

        //这里移除了对子坎子对子和提, 还剩余有牌大于2则此牌型不能听
        if (handCards.size() <= 2) {

            //所有牌刚好能组成听牌或天胡牌
            //最后剩余的牌不能做处理
            //尝试随便找到个组合替换其中一张牌
            if (!CollectionUtils.isEmpty(findCombos)) {
                PaohuziMingTangRule.FindAnyCombo.FindAny findAny = findCombos.get(0);
                //牌型中存在的顺子
                List<Integer> oldCombo = new ArrayList<>(findAny.getRepeatedSrcKey());
                //干掉当前组合的顺子或对子
                clearSerialOrDoubleCard(cards, leftCards, oldCombo, gameModel);
            } else if (max.getPaohzCardIndex(3) != null || max.getPaohzCardIndex(2) != null) {
                //如果牌型中没有顺子存在,拆散其中一对坎, 同时不能组成顺子
                Iterator<Integer> iterator = (max.getPaohzCardIndex(2) == null ? max.getPaohzCardIndex(3) : max.getPaohzCardIndex(2)).getPaohzValMap().keySet().iterator();
                Integer threeCombo = iterator.hasNext() ? iterator.next() : null;
                //干掉当前组合的顺子或对子
                clearSerialOrDoubleCard(cards, leftCards, new ArrayList<>(Arrays.asList(threeCombo, threeCombo, threeCombo)), gameModel);
            }
        }

//        System.out.println("检测通过时的卡组:"+cards);
//        System.out.println("检测通过时的手牌:"+handCards);

        LogUtil.printDebug("天地胡屏蔽:{}ms", (System.currentTimeMillis() - t));
//        System.out.println("天地胡屏蔽:{}ms" + (System.currentTimeMillis() - t));
        return new ArrayList<>(leftCards);
    }

    /**
     * @param
     * @return
     * @description 不能有对子和顺子组合
     * @author Guang.OuYang
     * @date 2019/10/9
     */
    private static void clearSerialOrDoubleCard(List<PaohzCard> cards, LinkedList<Integer> leftCards, List<Integer> oldCombo, GameModel gameModel) {
        //容差,牌型极限,已经不能匹配出非顺子组合了
        int rx = 80 - (2 * 20) - 1;
        int rxi = 1;

        long t = System.currentTimeMillis();
        while (rx > rxi) {
            List<Integer> newCombo = new ArrayList<>(oldCombo);

            //获取新的牌组
            int firstCardId = leftCards.removeFirst();
            int lastCardId = leftCards.removeFirst();

            PaohzCard firstCard = PaohzCard.getPaohzCard(firstCardId);
            PaohzCard lastCard = PaohzCard.getPaohzCard(lastCardId);


//            System.out.println("牌型1:" + cards);

//            System.out.println("需更换的牌组:" + newCombo.get(0) + "," + newCombo.get(1));
            //尝试更换其中的两张牌
            //把当前旧的牌组重新加入牌堆
            PaohzCard[] cardAndRemove = findCardAndRemove(cards, newCombo.get(0), newCombo.get(1));
            leftCards.addLast(cardAndRemove[0].getId());
            leftCards.addLast(cardAndRemove[1].getId());

            //更换手牌
            newCombo.set(0, firstCard.getVal());
            newCombo.set(1, lastCard.getVal());

            //获取新的牌组加入手牌
            cards.add(firstCard);
            cards.add(lastCard);

//            System.out.println("牌型2:" + cards);
//            System.out.println("牌堆:" + leftCards);
//            System.out.println("换出来的组合:"+cardAndRemove[0].getId()+","+cardAndRemove[1].getId());
//            System.out.println("老的组合:" + oldCombo + " ," + (System.currentTimeMillis() - t) + "ms");
//            System.out.println("新的组合:" + newCombo + " ," + (System.currentTimeMillis() - t) + "ms");

//            HashSet<Integer> integers = new HashSet<>(newCombo);
//            Collections.sort(newCombo);
//            newCombo.sort((v1, v2) -> v1.compareTo(v2));

//            System.out.println("排序:"+integers+" "  + (System.currentTimeMillis()-t)+"ms");

            //检测顺子
            boolean c = PaohuziMingTangRule.isSerialNumber(new int[]{newCombo.get(0), newCombo.get(1), newCombo.get(2)}, false);
            //3条
            boolean d = newCombo.get(2) != newCombo.get(1) && newCombo.get(0) != newCombo.get(1) && newCombo.get(2) != newCombo.get(0);
            //大小3条,吃牌
            boolean e = newCombo.get(2) % 100 == newCombo.get(1) % 100 && newCombo.get(0) % 100 == newCombo.get(1) % 100 && newCombo.get(2) % 100 == newCombo.get(0) % 100;

//            System.out.println("检测顺子:或对子:" + newCombo + " 是顺子:" + c + "  是3条" + !d + " 是大小3条: " + e + " " + (System.currentTimeMillis() - t) + "ms");
//            //检测顺子or对子
            if (!c && d && !e) {
                //防止撞牌, 重新和其他组合检测
                listenCardBarrier(cards, leftCards, gameModel);
                break;
            }
            ++rxi;
            oldCombo = newCombo;
        }
    }


    /**
     * @param
     * @return
     * @description 找到牌组中所有大于3张的牌组
     * @author Guang.OuYang
     * @date 2019/9/26
     */
    public static Map<Integer, Integer> findCountGeThree(List<PaohzCard> cards) {
        Map<Integer, Integer> res = new HashMap<Integer, Integer>();
        cards.sort((v1, v2) -> Integer.valueOf(v1.getPai()).compareTo(Integer.valueOf(v2.getPai())));

        //最低匹配数量
        int minMathCount = 3;
        int prePai = 0;   //上一个牌
        int repeated = 0; //当前计算的数量

        Iterator<PaohzCard> iterator = cards.iterator();
        while (iterator.hasNext()) {
            PaohzCard next = iterator.next();
            if (prePai > 0 && prePai > 0 && prePai != next.getPai()) {
                repeated = 0;
                prePai = 0;
            }
            if (prePai == 0 || prePai == next.getPai()) {
                prePai = next.getPai();
                repeated += 1;
            }
            if (repeated == minMathCount) {
                res.merge(next.getPai(), repeated, (oV, nV) -> nV);
            }
        }
        return res;
    }

    public static PaohzCard[] findCardAndRemove(List<PaohzCard> leftCards, int... vals) {
        int i = vals.length;
        PaohzCard[] res = new PaohzCard[i];
        PaohzCard paohzCard = null;
        Iterator<PaohzCard> iterator = leftCards.iterator();
        while (iterator.hasNext()) {
            paohzCard = iterator.next();
            if (findArrayEquals(vals, paohzCard.getVal())) {
                iterator.remove();

                res[res.length - i] = paohzCard;

                if (--i <= 0) {
                    break;
                }
            }

        }
        return res;
    }

    public static boolean findArrayEquals(int[] srcs, int v) {
        for (int i = 0; i < srcs.length; i++) {
            if (srcs[i] != -1 && srcs[i] == v) {
                srcs[i] = -1; //该牌已经被找到
                return true;
            }
        }
        return false;
    }

    public static synchronized List<List<PaohzCard>> fapai(List<Integer> copy, List<List<Integer>> t) {
        List<List<PaohzCard>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<PaohzCard> pai = new ArrayList<>();
        int j = 1;

        int testcount = 0;
        if (GameServerConfig.isDebug() && t != null && !t.isEmpty()) {
            for (List<Integer> zp : t) {
                list.add(find(copy, zp));
                testcount += zp.size();
            }

            if (list.size() == 3) {
                list.add(toPhzCards(copy));
                System.out.println(JacksonUtil.writeValueAsString(list));
                return list;
            } else if (list.size() == 4) {
                return list;
            }
        }

        List<Integer> copy2 = new ArrayList<>(copy);
        int fapaiCount = 20 * 3 + 1 - testcount;
        if (pai.size() >= 21) {
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
                if (i < j * 20) {
                    pai.add(majiang);
                } else {
                    list.add(pai);
                    pai = new ArrayList<>();
                    pai.add(majiang);
                    j++;
                }
            } else {
                if (i <= j * 20) {
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
        return list;
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
     * 找出小牌
     *
     * @param copy
     * @return
     */
    public static List<PaohzCard> findSmallPhzs(List<PaohzCard> copy) {
        List<PaohzCard> find = new ArrayList<>();
        for (PaohzCard card : copy) {
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
    public static List<PaohzCard> checkChi(List<PaohzCard> handCards, PaohzCard disCard, GameModel gameModel) {
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

        //1510
        if (gameModel != null && gameModel.getSpecialPlay().isOneFiveTen()) {
            List<Integer> val1510 = Arrays.asList(1, 5, 10);
            if (val1510.contains(disCard.getPai())) {
                List<Integer> chi1510 = new ArrayList<>();
                for (int val : val1510) {
                    if (disCard.getPai() == val) {
                        continue;
                    }
                    chi1510.add(disCard.getCase() + val);
                }

                chiList.add(chi1510);
            }
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

    public static void main(String[] args) {
        long totalTime = 0;
        for (int i = 0; i < 10000000; i++) {
            //        long t=System.currentT0..imeMillis();
//        System.out.println(checkChi(Arrays.asList(PaohzCard.phz1, PaohzCard.phz2, PaohzCard.phz3), PaohzCard.phz1, null));
//        System.out.println(System.currentTimeMillis()-t+"ms");

            List<PaohzCard> pcards = new ArrayList<>();
            ArrayList<Integer> lfs = new ArrayList<>(PaohuziConstant.cardList);
            Collections.shuffle(lfs);

//            ArrayList<Integer> cards = new ArrayList<>(Arrays.asList(108, 108, 108, 106, 106, 6, 3, 3, 3, 3, 103, 104, 102, 7, 2, 109, 109, 109, 2, 2, 2));
//            ArrayList<Integer> cards = new ArrayList<>(Arrays.asList(108, 108, 8, 106, 106, 6, 3, 3, 3, 3, 110, 110, 10, 2, 109, 109, 2, 2, 2));
            //检测之前:true  [捌, 捌, 八, 陆, 陆, 六, 三, 三, 三, 三, 拾, 拾, 十, 二, 玖, 玖, 二, 二, 二]   0ms
//            ArrayList<Integer> cards = new ArrayList<>(Arrays.asList(8, 3, 3, 3, 3, 110, 110, 10, 2, 109, 109, 2, 2, 2, 7, 9, 10, 5, 6));
            //捌, 陆, 三, 三, 三, 三, 拾, 二, 玖, 玖, 二, 二, 二, 九, 肆, 柒, 肆, 柒, 贰
//            ArrayList<Integer> cards = new ArrayList<>(Arrays.asList(108, 106, 3, 3, 3, 3, 110, 2, 109, 109, 2, 2, 2, 9, 104, 107, 104, 107, 102));
            //八, 柒, 肆, 叁, 六, 壹, 伍, 四, 十, 柒, 叁, 四, 十, 拾, 伍, 肆, 七, 四, 一, 七, 壹
//            ArrayList<Integer> cards = new ArrayList<>(Arrays.asList(8, 107, 104, 103, 6, 101, 105, 4, 110, 107, 103, 4, 10, 110, 105, 104, 7, 4, 1, 7, 101));
            //玖, 一, 柒, 叁, 六, 壹, 柒, 贰, 九, 一, 捌, 肆, 二, 四, 拾, 伍, 五, 三, 八, 七, 四
//            ArrayList<Integer> cards = new ArrayList<Integer>(Arrays.asList(109,1,107,103,6,101,107,102,9,1,108,104,2,4,110,105,5,3,8,7,4));

            //七, 十, 六, 十, 贰, 二, 肆, 叁, 玖, 五, 肆, 五, 玖, 七, 六, 玖, 四, 贰, 叁, 二, 四
//            ArrayList<Integer> cards = new ArrayList<Integer>(Arrays.asList(7,10,6,10,102,2,104,103,109,5,104,5,109,7,6,109,4,102,103,2,4));
//////
//            Iterator<Integer> iterator = cards.iterator();
//            while (iterator.hasNext()) {
//                Integer next = iterator.next();
//
//                Iterator<Integer> iterator1 = lfs.iterator();
//                while (iterator1.hasNext()) {
//                    Integer integer = iterator1.next();
//                    PaohzCard paohzCard = toPhzCards(Arrays.asList(integer)).get(0);
//                    int val = paohzCard.getVal();
//                    if (val == next) {
//                        pcards.add(paohzCard);
//                        iterator1.remove();
//                        break;
//                    }
//                }
//            }
//
//            lfs = new ArrayList<>(Arrays.asList(80, 67, 51, 9, 11, 76, 15, 20, 77, 43, 46, 22, 7, 47, 78, 25, 60, 28, 70, 42, 56, 64, 38, 10, 45, 18, 57, 69, 55, 66, 44, 3, 58, 29, 72, 19, 2, 41, 33, 23, 8, 48, 61, 4, 75, 16, 31, 6, 68, 50, 39, 1, 17, 73, 13, 71, 21, 65, 34));

            int j = 0 ;
            Iterator<Integer> iterator = lfs.iterator();
            while (iterator.hasNext()) {
                Integer next = iterator.next();
                pcards.add(PaohzCard.getPaohzCard(next));
                iterator.remove();
                if (++j >= 21) {
                    break;
                }
            }

            long t = System.currentTimeMillis();
            PaohuziHuLack huNew = PaohuziTool.isHuNew(PaohuziTool.getPaohuziHandCardBean(pcards), null, true, 15, false, true, GameModel.builder().specialPlay(new GameModel.SpecialPlay()).build());
            if(huNew.isHu())
                System.out.println(i+"检测之前:" + huNew.isHu() + "  " + pcards + "   " + (System.currentTimeMillis() - t) + "ms");
            long t1=0;
            try {
                t1 = System.currentTimeMillis();
                listenCardBarrier(pcards, new LinkedList<>(lfs.subList(0, 39)), null);
                totalTime += System.currentTimeMillis() - t1;
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }

            t = System.currentTimeMillis();
            PaohuziHuLack huNew1 = PaohuziTool.isHuNew(PaohuziTool.getPaohuziHandCardBean(pcards), null, true, 15, false, true, GameModel.builder().specialPlay(new GameModel.SpecialPlay()).build());
            if(huNew.isHu())
                System.out.println("检测之后:" + pcards + " " + (System.currentTimeMillis() - t) + "ms"+"   "+(System.currentTimeMillis() - t1)+"ms");
            if (huNew1.isHu()) {
                System.out.println("鱼排:"+lfs);
                System.out.println("检测失败" + pcards);
                System.exit(-1);
            }
        }

        System.out.println("总耗时:" + totalTime + "ms");
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

//    /**
//     * @param phzList   牌List
//     * @param o         值or牌
//     * @param matchCase 是否为值（true取val，false取pai）
//     * @return 返回与o的值相同的牌的集合
//     */
//    public static List<PaohzCard> findCountByVal(List<PaohzCard> phzList, Integer o, boolean matchCase) {
//        int val = (int) o;
//        return phzList.stream().filter(v -> (matchCase ? v.getVal() : v.getPai()) == val).collect(Collectors.toList());
//    }
//
//    /**
//     * @param phzList   牌List
//     * @param o         值or牌
//     * @param matchCase 是否为值（true取val，false取pai）
//     * @return 返回与o的值相同的牌的集合
//     */
//    public static List<PaohzCard> findCountByVal(List<PaohzCard> phzList, PaohzCard o, boolean matchCase) {
//        int val = matchCase ? o.getVal() : o.getPai();
//        return findCountByVal(phzList, val, matchCase);
//    }

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

    public static List<PaohzCard> findByVals(List<PaohzCard> majiangs, List<Integer> vals) {
        List<PaohzCard> result = new ArrayList<>();
        for (int val : vals) {
            for (PaohzCard majiang : majiangs) {
                if (majiang.getVal() == val) {
                    result.add(majiang);
                    break;
                }
            }
        }

        return result;
    }

    public static PaohuziHuBean checkHu(List<PaohzCard> handCard) {
        if (handCard == null || handCard.isEmpty()) {
            return null;
        }
        PaohuziHuBean bean = new PaohuziHuBean();

        List<PaohzCard> copy = new ArrayList<>(handCard);
        bean.setHandCards(new ArrayList<>(copy));

        PaohzCardIndexArr valArr = PaohuziTool.getMax(copy);
        bean.setValArr(valArr);

        // 去掉3张和4张一样的牌
        PaohuziIndex index3 = valArr.getPaohzCardIndex(3);
        if (index3 != null) {
            copy.removeAll(index3.getPaohzList());
        }
        PaohuziIndex index2 = valArr.getPaohzCardIndex(2);
        if (index2 != null) {
            copy.removeAll(index2.getPaohzList());
        }
        bean.setOperateCards(copy);

        return bean;
    }

    // public static void chaiPai(PaohuziHuBean bean) {
    // 不管大2小2先找出来
    // List<PaohzCard> find2 = findCountByVal(bean.getOperateCards(), 2,
    // false);
    // for (PaohzCard card2 : find2) {
    // 是否有123 或者2710可以配对

    // }

    // }

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
    public static boolean chaipai(PaohuziHuLack lack, List<PaohzCard> hasPais, GameModel gameModel) {
        if (hasPais.isEmpty()) {
            return true;

        }
        boolean hu = chaishun(lack, hasPais, gameModel);
        if (hu)
            return true;
        return false;
    }

    public static boolean chaiSame(PaohuziHuLack lack, List<PaohzCard> hasPais, PaohzCard minCard, List<PaohzCard> sameList, GameModel gameModel) {
        if (sameList.size() < 3) {
            // 小于3张牌没法拆
            return false;
        } else if (sameList.size() == 3) {
            // 大小加一起正好3张牌
            boolean chaisame = chaishun3(lack, hasPais, minCard, false, false, sameList.get(0).getVal(), sameList.get(1).getVal(), sameList.get(2).getVal(), gameModel);
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
                boolean chaisame = chaishun3(copyLack, copyHasPais, minCard, false, false, model.get(0), model.get(1), minVal, gameModel);
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
    private static boolean chaikan(PaohuziHuLack lack, List<PaohzCard> hasPais, PaohzCard minCard, GameModel gameModel) {
        List<PaohzCard> sameValList = findCountByVal(hasPais, minCard, true);
        if (sameValList.size() == 3) {
            boolean chaiSame = chaishun3(lack, hasPais, minCard, false, false, sameValList.get(0).getVal(), sameValList.get(1).getVal(), sameValList.get(2).getVal(), gameModel);
            if (chaiSame) {
                return true;
            }
        }
        return false;
    }

    private static boolean chaiSame0(PaohuziHuLack lack, List<PaohzCard> hasPais, PaohzCard minCard, GameModel gameModel) {
        List<PaohzCard> sameList = findCountByVal(hasPais, minCard, false);
        if (sameList.size() < 3) {
            return false;
        }

        // 拆相同
        PaohuziHuLack copyLack = lack.clone();
        List<PaohzCard> copyHasPais = new ArrayList<>(hasPais);
        if (chaikan(copyLack, copyHasPais, minCard, gameModel)) {
            lack.copy(copyLack);
            return true;
        }

        // 拆相同2
        boolean chaiSame = chaiSame(copyLack, copyHasPais, minCard, sameList, gameModel);
        if (chaiSame) {
            lack.copy(copyLack);
            return true;
        }
        return false;
    }

    /**
     * 拆顺
     */
    public static boolean chaishun(PaohuziHuLack lack, List<PaohzCard> hasPais, GameModel gameModel) {
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
            boolean isHu = chaiSame0(lack, hasPais, minCard, gameModel);
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
            chaishun = chaiShun(copyLack2, copyHasPais2, minCard, true, check2710, pai1, pai7, pai10, gameModel);
            if (chaishun) {
                lack.copy(copyLack2);
            } else {
                // 拆2 7 10组合失败
                chaishun = chaiShun(lack, hasPais, minCard, true, false, pai1, pai2, pai3, gameModel);
            }
        } else {
            // 拆顺
            chaishun = chaiShun(lack, hasPais, minCard, true, check2710, pai1, pai2, pai3, gameModel);
        }

        if (!chaishun && !isTryChaiSame) {
            chaishun = chaiSame0(copyLack, copyHasPais, minCard, gameModel);
            if (chaishun) {
                lack.copy(copyLack);

            }
        }
        return chaishun;

    }

    public static boolean chaiShun(PaohuziHuLack lack, List<PaohzCard> hasPais, PaohzCard minCard, boolean isShun, boolean check2710, int pai1, int pai2, int pai3, GameModel gameModel) {
        // 拆顺
        boolean chaishun = chaishun3(lack, hasPais, minCard, true, check2710, pai1, pai2, pai3, gameModel);
        if (!chaishun) {
            if (check2710) {
                return chaishun(lack, hasPais, gameModel);
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

    private static boolean chaishun3(PaohuziHuLack lack, List<PaohzCard> hasPais, PaohzCard minCard, boolean isShun, boolean check2710, int pai1, int pai2, int pai3, GameModel gameModel) {
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
                    return chaipai(lack, hasPais, gameModel);
                }

                // 检查是不是对子
                // if (lack.isNeedDui()) {
                // List<PaohzCard> count = getVal(hasPais,
                // hasMajiangList.get(0).getVal());
                // if (count.size() == 2) {
                // lack.setNeedDui(false);
                // hasPais.removeAll(count);
                // return chaipai(lack, hasPais);
                // }
                // }

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
                        return chaipai(lack, hasPais, gameModel);
                    }

                    // 拿一张红中补坎子
                    lack.changeHongzhong(-1);
                    lack.addLack(count.get(0).getVal());
                    hasPais.removeAll(count);
                    return chaipai(lack, hasPais, gameModel);
                }

                // 做将
                if (lack.isNeedDui() && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.setNeedDui(false);
                    hasPais.removeAll(count);
                    lack.addLack(count.get(0).getVal());
                    return chaipai(lack, hasPais, gameModel);
                }
            } else if (lackNum == 1) {
                // 做将
                if (lack.isNeedDui() && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.setNeedDui(false);
                    hasPais.remove(minCard);
                    lack.addLack(minCard.getVal());
                    return chaipai(lack, hasPais, gameModel);
                }

                List<PaohzCard> count = getVals(hasPais, hasMajiangList.get(0).getVal());
                if (count.size() == 2 && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.addLack(count.get(0).getVal());
                    hasPais.removeAll(count);
                    return chaipai(lack, hasPais, gameModel);
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
                    chaipai(lack, hasPais, gameModel);
                }
            }
            int huxi = 0;
            int action = PaohzDisAction.action_chi;
            if (isShun) {
                int minPai = minCard.getPai();
                if (minPai == 1 || minPai == 2) {
                    // 123 和 2710 加胡息
                    huxi = getShunHuxi(hasMajiangList, gameModel);
                }
            } else {
                if (isSameCard(hasMajiangList)) {
                    // 如果是三个一模一样的
                    action = PaohzDisAction.action_peng;
                    if (lack.isSelfMo()) {
                        action = PaohzDisAction.action_zai;
                    }
                    huxi = getOutCardHuxi(action, hasMajiangList, gameModel);
                }
            }

            lack.addPhzHuCards(action, toPhzCardIds(hasMajiangList), huxi);
            lack.changeHuxi(huxi);
            hasPais.removeAll(hasMajiangList);
            return chaipai(lack, hasPais, gameModel);
        }
        return chaipai(lack, hasPais, gameModel);
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
    public static int getOutCardHuxi(int action, List<PaohzCard> cards, GameModel gameModel) {
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
            huxi = getShunHuxi(copy, gameModel);
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
    private static int getShunHuxi(List<PaohzCard> hasMajiangList, GameModel gameModel) {
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
            //校验2710&1510
            //必须强匹配为小写或大写
            if (gameModel != null && gameModel.getSpecialPlay().isOneFiveTen()) {
                if (!new PaohuziMingTangRule.FindAnyCombo(
                        new Integer[]{1, 5, 10},
                        new Integer[]{101, 105, 110},
                        new Integer[]{2, 7, 10},
                        new Integer[]{102, 107, 110})
                        .anyComboMath(hasMajiangList.stream().map(v -> v.getVal())).isFind()) {
                    return 0;
                }
            } else {
                if (!new PaohuziMingTangRule.FindAnyCombo(
                        new Integer[]{2, 7, 10},
                        new Integer[]{102, 107, 110})
                        .anyComboMath(hasMajiangList.stream().map(v -> v.getVal())).isFind()) {
                    return 0;
                }
            }


//            for (PaohzCard card : hasMajiangList) {
//                if (!c2710List.contains(card.getPai())) {
//                    return 0;
//                }
//            }

            if (minCard.isBig()) {
                return 6;
            } else {
                return 3;
            }
        }
        return 0;
    }

    /**
     * 是否胡牌
     */
    public static PaohuziHuLack isHu(PaohuziHandCard handCardBean, PaohzCard disCard, boolean isSelfMo, int outCardHuxi, boolean needDui, boolean isPaoHu, GameModel gameModel) {
        PaohuziHuLack lack = new PaohuziHuLack(0);
        // lack.changeHuxi(outCardHuxi);
        lack.setSelfMo(isSelfMo);
        lack.setCheckCard(disCard);
        // int count = handCardBean.getHandCards().size();
        // if (count % 3 != 0) {
        lack.setNeedDui(needDui);
        // }

        PaohzCardIndexArr arr = handCardBean.getGroupByNumberToArray();
        // 手上有3个的
        PaohuziIndex index2 = arr.getPaohzCardIndex(2);
        if (index2 != null) {
            List<Integer> list = index2.getValList();
            for (int val : list) {
                if (disCard != null && val == disCard.getVal()) {
                    // 抓到手上的牌可以不用强制组出3个
                    handCardBean.getOperateCards().addAll(index2.getPaohzValMap().get(val));
                    continue;
                }
                int huxi = 0;
                if (val > 100) {
                    // 大的6分
                    huxi = 6;
                } else {
                    // 小的3分
                    huxi = 3;
                }
                lack.changeHuxi(huxi);
                List<PaohzCard> cards = index2.getPaohzValMap().get(val);
//				lack.addPhzHuCards(PaohzDisAction.action_zai, toPhzCardIds(cards), huxi);
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
                        if (val > 100) {
                            // 坎大的6分
                            huxi = 6;
                        } else {
                            // 坎小的3分
                            huxi = 3;
                        }
                        handCardBean.getOperateCards().add(disCard);
                    } else if (isSelfMo) {
                        if (val > 100) {
                            // 大的12分
                            huxi = 12;
                        } else {
                            // 小的3分
                            huxi = 9;
                        }
                        action = PaohzDisAction.action_ti;
                    } else {
                        action = PaohzDisAction.action_pao;
                        paoHu = true;
                        if (val > 100) {
                            // 碰大的6分
                            huxi = 9;
                        } else {
                            // 小的3分
                            huxi = 6;
                        }
                        List<PaohzCard> cards = index3.getPaohzValMap().get(val);
                        // cards.remove(disCard);

                        lack.changeHuxi(huxi);
                        lack.addPhzHuCards(action, toPhzCardIds(cards), huxi);
                        // handCardBean.getOperateCards().add(disCard);
                    }
                } else {
                    //直接手上有4张
                    action = PaohzDisAction.action_ti;
                    if (val > 100) {
                        // 大的6分
                        huxi = 12;
                    } else {
                        // 小的3分
                        huxi = 9;
                    }
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
        // 需要对子才能胡牌
        if (lack.isNeedDui()) {
            List<List<PaohzCard>> duiziList = arr.getDuizis();
            List<PaohuziHuLack> lackList = new ArrayList<>();

            for (List<PaohzCard> list : duiziList) {
                List<PaohzCard> copy = new ArrayList<>(handCardBean.getOperateCards());
                // List<PaohzCard> list = valEntry.getValue();
                if (!copy.containsAll(list)) {
                    continue;
                }
                int i = 0;
                List<Integer> duizi = new ArrayList<>();
                for (PaohzCard phz : list) {
                    i++;
                    duizi.add(phz.getId());
                    copy.remove(phz);
                    if (i >= 2) {
                        break;
                    }
                }
                PaohuziHuLack lackCopy = new PaohuziHuLack(0);
                lackCopy.setSelfMo(isSelfMo);
                lackCopy.setCheckCard(disCard);
                lackCopy.changeHuxi(lack.getHuxi());
                if (lack.getPhzHuCards() != null) {
                    lackCopy.setPhzHuCards(new ArrayList<>(lack.getPhzHuCards()));

                }
                lackCopy.addPhzHuCards(0, duizi, 0);
                boolean hu = chaipai(lackCopy, copy, gameModel);
                if (hu) {
                    lackCopy.setHu(hu);
                    lackList.add(lackCopy);
                }

            }
            if (!lackList.isEmpty()) {
                int maxHuxi = 0;
                PaohuziHuLack maxHuxiLack = null;
                for (PaohuziHuLack copy : lackList) {
                    if (maxHuxi == 0 || copy.getHuxi() > maxHuxi) {
                        maxHuxi = copy.getHuxi();
                        maxHuxiLack = copy;
                    }
                }
                return maxHuxiLack;

            }

        } else {
            boolean hu = chaipai(lack, handCardBean.getOperateCards(), gameModel);
            lack.setHu(hu);
        }
        return lack;
    }

    /**
     * 得到可以操作的牌 4张和3张是不能操作的
     */
    public static PaohuziHandCard getPaohuziHandCardBean(List<PaohzCard> handPais) {
        PaohuziHandCard card = new PaohuziHandCard();
        List<PaohzCard> copy = new ArrayList<>(handPais);
        card.setHandCards(new ArrayList<>(copy));

        PaohzCardIndexArr valArr = PaohuziTool.getMax(copy);
        card.setGroupByNumberToArray(valArr);
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

//    public static void main(String[] args) {
//        boolean needDui = false;
//        boolean isPaoHu = true;
//        boolean isSelfMo = true;
//        int outCardHuXi = 0;
//        List<Integer> valList;
//        valList = new ArrayList<>(Arrays.asList(101, 102, 103, 4, 5, 6, 7, 8, 9, 107, 107, 108, 109, 110, 110, 10, 10, 107));
//        valList = new ArrayList<>(Arrays.asList(101, 1, 1, 2, 3, 4, 101, 102, 103, 105, 5, 5, 6, 7, 8, 102, 107, 110, 104, 104, 104));
//        valList = new ArrayList<>(Arrays.asList(101, 102, 103, 104, 104));
//        needDui = true;
//        valList = new ArrayList<>(Arrays.asList(1, 1, 1, 4, 4, 4, 7, 7, 7, 10, 10, 101, 102, 103, 106, 107, 108, 109, 110, 110, 105));
//        needDui = false;
//        valList = new ArrayList<>(Arrays.asList(
//                PaohzCard.phz1.getVal(), PaohzCard.phz11.getVal(), PaohzCard.phz21.getVal(),
////                PaohzCard.phz2,PaohzCard.phz22,PaohzCard.phz32,
//                PaohzCard.phz3.getVal(), PaohzCard.phz3.getVal()));
//        needDui = true;
//
//
//        List<PaohzCard> cardList = val2Card(valList);
//        PaohzCard card = cardList.get(cardList.size() - 1);
//        PaohuziHandCard bean = getPaohuziHandCardBean(cardList);
//
//        int count = 1000000;
//        PaohuziHuLack hu1 = null;
//        long start = System.currentTimeMillis();
//        for (int i = 0; i < count; i++) {
//            hu1 = isHu(bean, card, isSelfMo, outCardHuXi, needDui, isPaoHu);
//        }
//        System.out.println("count = " + count + " timeUse = " + (System.currentTimeMillis() - start) + " ms");
//        System.out.println((hu1.isHu() ? "胡啦" : "不胡") + "|" + JSON.toJSONString(hu1));
//
//        bean = getPaohuziHandCardBean(cardList);
//        card = null;
//        PaohuziHuLack hu2 = null;
//        start = System.currentTimeMillis();
//        for (int i = 0; i < count; i++) {
//            hu2 = isHuNew(bean, card, isSelfMo, outCardHuXi, needDui, isPaoHu);
//        }
//        System.out.println("count = " + count + " timeUse = " + (System.currentTimeMillis() - start) + " ms");
//        System.out.println((hu2.isHu() ? "胡啦" : "不胡") + "|" + JSON.toJSONString(hu2));
//    }


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

    /**
     * @param handCardBean 当前手牌组成
     * @param disCard      打出来的牌
     * @param isSelfMo     是否自己摸出
     * @param outCardHuxi
     * @param needDui
     * @param isPaoHu
     * @return
     * @description
     * @author Guang.OuYang
     * @date 2019/9/4
     */
    public static PaohuziHuLack isHuNew(PaohuziHandCard handCardBean, PaohzCard disCard, boolean isSelfMo, int outCardHuxi, boolean needDui, boolean isPaoHu, GameModel gameModel) {

        PaohuziHuLack lack = new PaohuziHuLack(0);
        lack.setSelfMo(isSelfMo);
        lack.setCheckCard(disCard);
        lack.setNeedDui(needDui);

        PaohzCardIndexArr arr = handCardBean.getGroupByNumberToArray();
        // 手上有3个的
        PaohuziIndex index2 = arr.getPaohzCardIndex(2);
        if (index2 != null) {
            List<Integer> list = index2.getValList();
            for (int val : list) {
                if (disCard != null && val == disCard.getVal()) {
                    // 抓到手上的牌可以不用强制组出3个
                    handCardBean.getOperateCards().addAll(index2.getPaohzValMap().get(val));
                    continue;
                }
                //大字6分小字3分
                int huxi = val > 100 ? 6 : 3;
                lack.changeHuxi(huxi);
                List<PaohzCard> cards = index2.getPaohzValMap().get(val);
                lack.addPhzHuCards(PaohzDisAction.action_kan, toPhzCardIds(cards), huxi);
            }
        }

        //四张牌型包含的数量
        PaohuziIndex fourNumberIndex = arr.getPaohzCardIndex(3);
        if (fourNumberIndex != null) {
            List<Integer> list = fourNumberIndex.getValList();
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
                        List<PaohzCard> cards = fourNumberIndex.getPaohzValMap().get(val);
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
                    List<PaohzCard> cards = fourNumberIndex.getPaohzValMap().get(val);
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
            lack.setHu(!needDui);
        } else {
            sortMin(handPais);
            chaiPaiNew(huList, lack, handPais, disCard, gameModel);
            if (huList.size() > 0) {
                return getMaxHuxi(huList);
            }
        }
        return lack;
    }

    public static PaohuziHuLack getMaxHuxi(List<PaohuziHuLack> huList) {
        if (huList != null && huList.size() > 0) {
            PaohuziHuLack maxHu = null;
            int maxHuxi = -99999;
            for (PaohuziHuLack hu : huList) {
                int huxi = hu.calcHuxi();
                hu.setHuxi(huxi);
                if (hu.calcHuxi() > maxHuxi) {
                    maxHuxi = huxi;
                    maxHu = hu;
                }
            }
            return maxHu;
        }
        return null;
    }

    public static void chaiPaiNew(List<PaohuziHuLack> huList, PaohuziHuLack lack, List<PaohzCard> handPais, PaohzCard disCard, GameModel gameModel) {
        if (lack.isNeedDui()) {
            //拆对
            PaohuziHandCard bean = getPaohuziHandCardBean(handPais);
            PaohzCardIndexArr indexArr = bean.getGroupByNumberToArray();
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
                            chaiPaiNew(huList, newLack, handPaisCopy, null, gameModel);
                        } else {
                            chaiPaiNew(huList, newLack, handPaisCopy, disCard, gameModel);
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
                            chaiPaiNew(huList, newLack, handPaisCopy, null, gameModel);
                        } else {
                            chaiPaiNew(huList, newLack, handPaisCopy, disCard, gameModel);
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
            PaohuziMingTangRule.FindAnyCombo findAnyCombo = new PaohuziMingTangRule.FindAnyCombo(
                    new Integer[]{1, 5, 10},
                    new Integer[]{101, 105, 110});
            for (int[] paiZu : paiZus) {
                if (!gameModel.getSpecialPlay().isOneFiveTen() &&
                        findAnyCombo.anyComboMath(IntStream.of(paiZu).mapToObj(v -> Integer.valueOf(v))).isFind()) {
                    continue;
                }
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
                    newLack.addPhzHuCards(0, toPhzCardIds(rmList), getShunHuxi(rmList, gameModel));
                }
                if (handPaisCopy.size() == 0) {
                    newLack.setHu(true);
                    huList.add(newLack);
                } else {
                    chaiPaiNew(huList, newLack, handPaisCopy, null, gameModel);
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
