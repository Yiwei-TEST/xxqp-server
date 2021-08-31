package com.sy599.game.qipai.pdkuai.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


public final class CardUtils {

    public static final CardValue EMPTY_CARD_VALUE = new CardValue(0, 0);
    public static final List<CardValue> EMPTY_CARD_VALUE_LIST = Arrays.asList(EMPTY_CARD_VALUE);

    /**
     * 发牌
     *
     * @param pairs      几副牌
     * @param exclusives 踢除的牌值
     * @return
     */
    public static List<Integer> loadCards(int pairs, int... exclusives) {
        List<Integer> list = new ArrayList<>(52 * pairs);
        for (int i = 0; i < pairs; i++) {
            for (int n = 3; n < 16; n++) {
                if (!contains(n, exclusives)) {
                    for (int m = 1; m < 5; m++) {
                        list.add((100 * m) + n);
                    }
                }
            }
        }
        Collections.shuffle(list, new SecureRandom());
        return list;
    }

    /**
     * 计算所有牌的得分值
     *
     * @param cards
     * @return
     */
    public static int loadCardScore(int... cards) {
        int total = 0;
        for (int card : cards) {
            int val = loadCardValue(card);
            if (val == 5 || val == 10) {
                total += val;
            } else if (val == 13) {
                total += 10;
            }
        }
        return total;
    }

    /**
     * 计算所有牌的得分值
     *
     * @param cards
     * @return
     */
    public static int loadCardValueScore(CardValue... cards) {
        int total = 0;
        for (CardValue card : cards) {
            int val = card.getValue();
            if (val == 5 || val == 10) {
                total += val;
            } else if (val == 13) {
                total += 10;
            }
        }
        return total;
    }

    /**
     * 计算所有牌的得分值
     *
     * @param cards
     * @return
     */
    public static int loadCardScore(List<Integer> cards) {
        int total = 0;
        for (int card : cards) {
            int val = loadCardValue(card);
            if (val == 5 || val == 10) {
                total += val;
            } else if (val == 13) {
                total += 10;
            }
        }
        return total;
    }

    /**
     * 计算所有牌的得分值
     *
     * @param cards
     * @return
     */
    public static int loadCardValueScore(List<CardValue> cards) {
        int total = 0;
        for (CardValue card : cards) {
            int val = card.getValue();
            if (val == 5 || val == 10) {
                total += val;
            } else if (val == 13) {
                total += 10;
            }
        }
        return total;
    }

    /**
     * 添加分牌
     *
     * @param cards
     * @return
     */
    public static int filterCardValueScore(List<CardValue> cards, List<Integer> scoreCards) {
        int total = 0;
        for (CardValue card : cards) {
            int val = card.getValue();
            if (val == 5 || val == 10) {
                total += val;
                scoreCards.add(card.getCard());
            } else if (val == 13) {
                total += 10;
                scoreCards.add(card.getCard());
            }
        }
        return total;
    }

    /**
     * 计算牌型的得分值
     *
     * @param result
     * @param min    牌的个数
     * @param base   最低分
     * @param rule   记分规则
     * @return
     */
    public static int loadResultScore(Result result, int min, int base, int rule) {
        if (result.type == 100) {
            int temp = result.count - min;
            if (temp == 0) {
                return base;
            } else if (temp > 0) {
                if (rule == 1) {
                    return base + (result.count - min) * base;
                } else if (rule == 2) {
                    int ratio = 1;
                    for (int i = 0; i < temp; i++) {
                        ratio *= 2;
                    }
                    return base * ratio;
                } else {
                    return base;
                }
            }
        }
        return 0;
    }

    /**
     * 检查vals中是否包含val
     *
     * @param val
     * @param vals
     * @return
     */
    public static boolean contains(int val, int... vals) {
        for (int v : vals) {
            if (v == val) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据牌id获取CardValue
     *
     * @param card
     * @return
     */
    public static CardValue initCardValue(int card) {
        return new CardValue(card, loadCardValue(card));
    }

    /**
     * 根据牌id获取CardValue
     *
     * @param cards
     * @return
     */
    public static List<CardValue> loadCards(List<Integer> cards) {
        List<CardValue> cardValues = new ArrayList<>(cards.size());
        for (Integer card : cards) {
            cardValues.add(initCardValue(card.intValue()));
        }
        return cardValues;
    }

    /**
     * 根据牌id获取CardValue
     *
     * @param cards
     * @return
     */
    public static List<CardValue> loadCardValues(int... cards) {
        List<CardValue> cardValues = new ArrayList<>(cards.length);
        for (int card : cards) {
            cardValues.add(initCardValue(card));
        }
        return cardValues;
    }

    /**
     * 根据牌CardValue获取id
     *
     * @param cards
     * @return
     */
    public static List<Integer> loadCardIds(List<CardValue> cards) {
        List<Integer> ids = new ArrayList<>(cards.size());
        for (CardValue card : cards) {
            ids.add(card.getCard());
        }
        return ids;
    }

    /**
     * 计算牌值(A_14,2_15,3_3...,K_13)
     *
     * @param card
     * @return
     */
    public static int loadCardValue(int card) {
        return card % 100;
    }

    /**
     * 计算相同牌值的数量
     *
     * @param cardValues
     * @return
     */
    public static int countSameCards(List<CardValue> cardValues, int val) {
        int count = 0;
        for (CardValue cardValue : cardValues) {
            if (cardValue.getValue() == val) {
                count++;
            }
        }
        return count;
    }

    /**
     * 根据牌值查找牌
     *
     * @param cardValues
     * @param val        牌值
     * @param count      数量
     * @return
     */
    public static List<CardValue> searchCardValues(List<CardValue> cardValues, int val, int count) {
        List<CardValue> list = new ArrayList<>(count);
        for (CardValue cardValue : cardValues) {
            if (cardValue.getValue() == val) {
                list.add(cardValue);
                if (list.size() >= count) {
                    break;
                }
            }
        }
        return list;
    }


    /**
     * 牌比较
     *
     * @param cardValues1
     * @param cardValues2
     * @return -100不能比较，大：正数，小：负数，相等：零
     */
    public static int compare(List<CardValue> cardValues1, List<CardValue> cardValues2, int siDai) {
        return calcCardValue(cardValues1, siDai, false,false).compareTo(calcCardValue(cardValues2, siDai, false,false));
    }


    public static Map<Integer,Integer> findBoom(List<CardValue> cardValues, boolean AAAZha) {
        Map<Integer, Integer> map = new HashMap<>();
        int maxCount = 0;
        for (CardValue cv : cardValues) {
            int count = map.getOrDefault(cv.getValue(), 0);
            count++;
            map.put(cv.getValue(), count);
            if (count > maxCount) {
                maxCount = count;
            }
        }
        Map<Integer,Integer> result=new HashMap<>();
        for (Entry<Integer, Integer> entry :map.entrySet()) {
            if(entry.getValue()==4)
                result.put(entry.getKey(),entry.getValue());
        }
        Integer A3 = map.get(14);
        if(AAAZha&&A3!=null&&A3==3)
            result.put(14,map.get(14));
        return result;
    }


        /**
         * 计算牌组合结果
         *
         * @param cardValues
         * @param siDai             4带
         * @param isFirstCardType32 本轮起始牌型是滞是3带2
         * @return
         */
    public static Result calcCardValue(List<CardValue> cardValues, int siDai, boolean isFirstCardType32,boolean AAAZha) {
        Map<Integer, Integer> map = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.intValue() - o2.intValue();
            }
        });

        int total = 0;
        int maxCount = 0;
        for (CardValue cv : cardValues) {
            int count = map.getOrDefault(cv.getValue(), 0);
            count++;
            map.put(cv.getValue(), count);
            total++;
            if (count > maxCount) {
                maxCount = count;
            }
        }
        int valCount = map.size();
        if (valCount == 1) {
            //单张、对子、炸弹
            Map.Entry<Integer, Integer> kv = map.entrySet().iterator().next();
            int count = kv.getValue();
            if (count == 1) {
                return new Result(1, count, kv.getKey());
            } else if (count == 2) {
                return new Result(2, count, kv.getKey());
            } else if (count == 3) {
                if(cardValues.get(0).getValue() ==  14&&AAAZha){
                    // 3条A炸
                    return new Result(1000, 1, kv.getKey());
                }else {
                    return new Result(3, 1, kv.getKey());
                }
            } else if (count >= 4) {
                return new Result(100, count, kv.getKey());
            }
        } else {
            if (maxCount >= 3) {
                if (maxCount == 4) {
                    if((siDai==0||isFirstCardType32) && total == 5){
                        // 4带当3带2出
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            if (kv.getValue().intValue() == 4) {
                                int max = kv.getKey().intValue();
                                return new Result(3, 1, max);
                            }
                        }
                    }else if(total <= siDai + 4 ) {
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            if (kv.getValue().intValue() == 4) {
                                int max = kv.getKey().intValue();
                                return new Result(4, 1, max);
                            }
                        }
                    }
                }

                if (total <= 5) {
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        if (kv.getValue().intValue() == 3) {
                            int max = kv.getKey().intValue();
                            return new Result(3, 1, max);
                        }
                    }
                } else {
                    int pre = 0;
                    int count = 0;
                    List<Map.Entry<Integer, Integer>> list = new ArrayList<>(valCount);
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        list.add(0, kv);
                    }
                    for (Map.Entry<Integer, Integer> kv : list) {
                        if (kv.getValue().intValue() >= 3) {
                            if (pre == 0) {
                                pre = kv.getKey().intValue();
                                count = 1;
                            } else {
                                pre -= 1;
//                                    while (contains(pre, exclusives)) {
//                                        pre -= 1;
//                                    }
                                if (kv.getKey().intValue() == pre) {
                                    count++;
                                    if ((count * 5) >= total) {
                                        break;
                                    }
                                } else {
                                    if ((count * 5) >= total) {
                                        break;
                                    } else {
                                        pre = kv.getKey().intValue();
                                        count = 1;
                                    }
                                }
                            }
                        }
                    }
                    if ((count * 5) >= total) {
                        int max = pre;
                        int i = 1;
                        while (i < count) {
                            max++;
//                                while (contains(max, exclusives)) {
//                                    max++;
//                                }
                            i++;
                        }
                        return new Result(33, count, max);
                    }
                }
            } else if (maxCount == 2 && total >= 4 && (total % 2 == 0)) {
                boolean canPop = true;
                int pre = 0;
                for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                    if (kv.getValue().intValue() != 2) {
                        canPop = false;
                        break;
                    } else {
                        if (pre == 0) {
                            pre = kv.getKey().intValue();
                        } else {
                            pre += 1;
//                            while (contains(pre, exclusives)) {
//                                pre += 1;
//                            }
                            if (kv.getKey().intValue() != pre) {
                                canPop = false;
                                break;
                            }
                        }
                    }
                }

                if (canPop) {
                    return new Result(22, valCount, pre);
                }
            } else if (maxCount == 1 && total >= 5) {
                boolean canPop = true;
                int pre = 0;
                for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                    if (kv.getValue().intValue() != 1) {
                        canPop = false;
                        break;
                    } else {
                        if (pre == 0) {
                            pre = kv.getKey().intValue();
                        } else {
                            pre += 1;
//                            while (contains(pre, exclusives)) {
//                                pre += 1;
//                            }
                            if (kv.getKey().intValue() != pre) {
                                canPop = false;
                                break;
                            }
                        }
                    }
                }

                if (canPop) {
                    return new Result(11, valCount, pre);
                }
            }
        }
        return new Result(0, 0, 0);
    }

    /**
     * 在src中查找比search大的最小值
     *
     * @param src
     * @param search
     * @return
     */
    public static List<CardValue> searchBiggerCardValues(List<CardValue> src, List<CardValue> search, int siDai) {
        return searchBiggerCardValues(src, calcCardValue(search, siDai, false,false),false, siDai);
    }

    /**
     * 在src中查找比result大的最小值
     *
     * @param src
     * @param result
     * @return
     */
    public static List<CardValue> searchBiggerCardValues(List<CardValue> src, Result result,boolean AAAZha ,int... exclusives) {
        if (result.type > 0) {
            List<CardValue> AAA = new ArrayList<>();
            if(AAAZha) {
                Iterator<CardValue> iterator = src.iterator();
                while (iterator.hasNext()) {
                    CardValue next = iterator.next();
                    if(next.getValue() == 14){
                        iterator.remove();
                        AAA.add(next);
                    }
                }
                if(AAA.size() != 3){
                    // 不够3个，又放回去
                    src.addAll(AAA);
                }
            }


            Map<Integer, Integer> map = new TreeMap<>(new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o1.intValue() - o2.intValue();
                }
            });
            int maxCount = 0;
            int maxValue = 0;
            for (CardValue cv : src) {
                int count = map.getOrDefault(cv.getValue(), 0);
                count++;
                map.put(cv.getValue(), count);
                if (count > maxCount) {
                    maxCount = count;
                }
                if (cv.getValue() > maxValue) {
                    maxValue = cv.getValue();
                }
            }
            int val = 0;


            if (result.type == 100) {
                //先找四炸
                for (int i = result.count; i <= maxCount; i++) {
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        if (kv.getValue().intValue() == i) {
                            if (i == result.count) {
                                if (kv.getKey().intValue() > result.max) {
                                    return searchCardValues(src, kv.getKey().intValue(), i);
                                }
                            } else {
                                return searchCardValues(src, kv.getKey().intValue(), i);
                            }
                        }
                    }
                }
            } else if (maxCount >= 4) {
                for (int i = 4; i <= maxCount; i++) {
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        if (kv.getValue().intValue() == i) {
                            return searchCardValues(src, kv.getKey().intValue(), i);
                        }
                    }
                }
            }

            if(AAAZha){
                if(AAA.size() == 3) {
                    // 有3条A炸，要放回去
                    src.addAll(AAA);
                    map.clear();
                    for (CardValue cv : src) {
                        int count = map.getOrDefault(cv.getValue(), 0);
                        count++;
                        map.put(cv.getValue(), count);
                    }
                }
                // 再找3条A
                for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                    if (kv.getValue().intValue() == 3 && kv.getKey().intValue() == 14) {
                        val = kv.getKey().intValue();
                        return searchCardValues(src, val, 3);
                    }
                }
            }


            switch (result.type) {
                case 1:
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        if (kv.getValue().intValue() == 1 && kv.getKey().intValue() > result.max) {
                            val = kv.getKey().intValue();
                            return searchCardValues(src, val, 1);
                        }
                    }
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        if (kv.getValue().intValue() == 2 && kv.getKey().intValue() > result.max) {
                            val = kv.getKey().intValue();
                            return searchCardValues(src, val, 1);
                        }
                    }
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        if (kv.getValue().intValue() == 3 && kv.getKey().intValue() > result.max) {
                            val = kv.getKey().intValue();
                            return searchCardValues(src, val, 1);
                        }
                    }
                    break;
                case 2:
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        if (kv.getValue().intValue() == 2 && kv.getKey().intValue() > result.max) {
                            val = kv.getKey().intValue();
                            return searchCardValues(src, val, 2);
                        }
                    }
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        if (kv.getValue().intValue() == 3 && kv.getKey().intValue() > result.max) {
                            val = kv.getKey().intValue();
                            return searchCardValues(src, val, 2);
                        }
                    }
                    break;
                case 3:
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        if (kv.getValue().intValue() == 3 && kv.getKey().intValue() > result.max) {
                            val = kv.getKey().intValue();
                            break;
                        }
                    }
                    if (val > 0) {
                        int val1 = 0, val2 = 0;
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            if (kv.getValue().intValue() == 1) {
                                if (val1 == 0) {
                                    val1 = kv.getKey().intValue();
                                } else {
                                    val2 = kv.getKey().intValue();
                                    break;
                                }
                            }
                        }
                        if (val2 == 0) {
                            for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                if (kv.getValue().intValue() == 2) {
                                    val2 = kv.getKey().intValue();
                                    if (val1 == 0 || val1 > val2) {
                                        val1 = val2;
                                    }
                                    break;
                                }
                            }

                            if (val2 == 0) {
                                for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                    if (kv.getValue().intValue() == 3 && kv.getKey().intValue() != val) {
                                        val2 = kv.getKey().intValue();
                                        if (val1 == 0) {
                                            val1 = val2;
                                        }
                                        break;
                                    }
                                }
                            }

                            if (val2 == 0) {
                                for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                    if (kv.getValue().intValue() == 4 && kv.getKey().intValue() != val) {
                                        val2 = kv.getKey().intValue();
                                        if (val1 == 0) {
                                            val1 = val2;
                                        }
                                        break;
                                    }
                                }
                            }
                        }

//                        if (val1 > 0 && val2 > 0) {
                        List<CardValue> retList = searchCardValues(src, val, 3);
                        if (val2 > 0 && val1 == val2) {
                            retList.addAll(searchCardValues(src, val1, 2));
                        } else {
                            if (val1 > 0) {
                                retList.addAll(searchCardValues(src, val1, 1));
                            }
                            if (val2 > 0) {
                                retList.addAll(searchCardValues(src, val2, 1));
                            }
                        }
                        return retList;
//                        }
                    }
                    break;
                case 11:
                    int min = result.max;
                    for (int i = 0; i < result.count; i++) {
                        min--;
//                        while (contains(min, exclusives)) {
//                            min--;
//                        }
                    }
                    List<Integer> tempList = new ArrayList<>(result.count);
                    int min1 = maxValue;
                    for (int i = 0; i < result.count; i++) {
                        min1--;
//                        while (contains(min1, exclusives)) {
//                            min1--;
//                        }
                    }
                    if (min1 > min) {
                        for (int i = min + 1; i <= min1; i++) {
                            if (tempList.size() == result.count && tempList.get(result.count - 1).intValue() > result.max) {
                                break;
                            } else {
                                tempList.clear();
                                for (int m = 0; m < result.count; m++) {
                                    int tempVal = i + m + 1;
//                                    while (contains(tempVal, exclusives)) {
//                                        tempVal++;
//                                    }
                                    if (tempVal > 14) {
                                        break;
                                    }
                                    int len = countSameCards(src, tempVal);
                                    if (len >= 1 && len <= 3) {
                                        if (!tempList.contains(tempVal))
                                            tempList.add(tempVal);
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }
                        if (tempList.size() == result.count && tempList.get(result.count - 1).intValue() > result.max) {
                            List<CardValue> retList = new ArrayList<>(result.count);
                            for (int i = 0; i < result.count; i++) {
                                retList.addAll(searchCardValues(src, tempList.get(i).intValue(), 1));
                            }
                            return retList;
                        }
                    }
                    break;
                case 22:
                    min = result.max;
                    for (int i = 0; i < result.count; i++) {
                        min--;
//                        while (contains(min, exclusives)) {
//                            min--;
//                        }
                    }
                    tempList = new ArrayList<>(result.count);
                    min1 = maxValue;
                    for (int i = 0; i < result.count; i++) {
                        min1--;
//                        while (contains(min1, exclusives)) {
//                            min1--;
//                        }
                    }
                    if (min1 > min) {
                        for (int i = min + 1; i <= min1; i++) {
                            if (tempList.size() == result.count && tempList.get(result.count - 1).intValue() > result.max) {
                                break;
                            } else {
                                tempList.clear();
                                for (int m = 0; m < result.count; m++) {
                                    int tempVal = i + m + 1;
//                                    while (contains(tempVal, exclusives)) {
//                                        tempVal++;
//                                    }
                                    if (tempVal > 14) {
                                        break;
                                    }
                                    int len = countSameCards(src, tempVal);
                                    if (len == 2 || len == 3) {
                                        if (!tempList.contains(tempVal))
                                            tempList.add(tempVal);
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }
                        if (tempList.size() == result.count && tempList.get(result.count - 1).intValue() > result.max) {
                            List<CardValue> retList = new ArrayList<>(result.count * 2);
                            for (int i = 0; i < result.count; i++) {
                                retList.addAll(searchCardValues(src, tempList.get(i).intValue(), 2));
                            }
                            return retList;
                        }
                    }
                    break;
                case 33:
                    min = result.max;
                    for (int i = 0; i < result.count; i++) {
                        min--;
//                        while (contains(min, exclusives)) {
//                            min--;
//                        }
                    }
                    tempList = new ArrayList<>(result.count);
                    min1 = maxValue;
                    for (int i = 0; i < result.count; i++) {
                        min1--;
//                        while (contains(min1, exclusives)) {
//                            min1--;
//                        }
                    }
                    if (min1 > min) {
                        for (int i = min + 1; i <= min1; i++) {
                            if (tempList.size() == result.count && tempList.get(result.count - 1).intValue() > result.max) {
                                break;
                            } else {
                                tempList.clear();
                                for (int m = 0; m < result.count; m++) {
                                    int tempVal = i + m + 1;
//                                    while (contains(tempVal, exclusives)) {
//                                        tempVal++;
//                                    }
                                    if (tempVal > 14) {
                                        break;
                                    }
                                    int len = countSameCards(src, tempVal);
                                    if (len >= 3) {
                                        if (!tempList.contains(tempVal))
                                            tempList.add(tempVal);
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }
                        if (tempList.size() == result.count && tempList.get(result.count - 1).intValue() > result.max) {
                            int daiCount = 0;
                            int daiMax = result.count * 2;
                            List<CardValue> retList = new ArrayList<>(result.count * 5);
                            for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                if (kv.getValue().intValue() == 1) {
                                    daiCount++;
                                    retList.addAll(searchCardValues(src, kv.getKey().intValue(), 1));
                                    if (daiCount >= daiMax) {
                                        break;
                                    }
                                }
                            }
                            if (daiCount < daiMax) {
                                for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                    if (kv.getValue().intValue() == 2) {
                                        if (daiMax - daiCount > 1) {
                                            daiCount += 2;
                                            retList.addAll(searchCardValues(src, kv.getKey().intValue(), 2));
                                        } else if (daiMax - daiCount == 1) {
                                            daiCount++;
                                            retList.addAll(searchCardValues(src, kv.getKey().intValue(), 1));
                                        } else {
                                            break;
                                        }
                                        if (daiCount >= daiMax) {
                                            break;
                                        }
                                    }
                                }
                                if (daiCount < daiMax) {
                                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                        if (kv.getValue().intValue() == 3 && !tempList.contains(kv.getKey())) {
                                            if (daiMax - daiCount > 2) {
                                                daiCount += 3;
                                                retList.addAll(searchCardValues(src, kv.getKey().intValue(), 3));
                                            } else if (daiMax - daiCount == 2) {
                                                daiCount += 2;
                                                retList.addAll(searchCardValues(src, kv.getKey().intValue(), 2));
                                            } else if (daiMax - daiCount == 1) {
                                                daiCount++;
                                                retList.addAll(searchCardValues(src, kv.getKey().intValue(), 1));
                                            } else {
                                                break;
                                            }
                                            if (daiCount >= daiMax) {
                                                break;
                                            }
                                        }
                                    }

                                    if (daiCount < daiMax) {
                                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                                            if (kv.getValue().intValue() == 4 && tempList.contains(kv.getKey())) {
                                                daiCount++;
                                                CardValue cv = searchCardValues(src, kv.getKey().intValue(), 1).get(0);
                                                retList.add(cv);
                                                src.remove(cv);
                                                if (daiCount >= daiMax) {
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (daiCount <= daiMax) {
                                for (int i = 0; i < result.count; i++) {
                                    retList.addAll(0, searchCardValues(src, tempList.get(i).intValue(), 3));
                                }
                                return retList;
                            }
                        }
                    }
                    break;
            }


        }
        return Collections.emptyList();
    }

    /**
     * 统计相同牌值的个数
     *
     * @param cardValues
     * @return
     */
    public static final Map<Integer, Integer> countValue(List<CardValue> cardValues) {
        Map<Integer, Integer> map = new HashMap<>();
        for (CardValue cv : cardValues) {
            Integer val = map.getOrDefault(cv.getValue(), 0);
            map.put(cv.getValue(), val + 1);
        }
        return map;
    }
    
    
    
	
	/***
	 * 飞机
	 * @param list
	 * @return
	 */
	public static int isFeiJi(List<Integer> list){
		
		
//		if(isContainWang(list)){
//			return null;
//		}
		HashMap<Integer,Integer> map = new HashMap<Integer,Integer>();
		
		for(Integer id: list){
			int value = CardUtils.loadCardValue(id);
			Integer count = map.get(value);
			if(count==null){
				map.put(value, 1);
			}else {
				map.put(value, count+1);
			}
		}
		
		
		List<Integer> keys = new ArrayList<Integer>();
		for(Entry<Integer, Integer> entry: map.entrySet()){
			int key = entry.getKey();
			int val = entry.getValue();
			if(key==15){
				continue;
			}
			if(val>=3){
				keys.add(key);
			}
		}
		
		int feijiKey = 0;
		Collections.sort(keys);
		boolean feiji = false;
		for(int i=0;i<keys.size()-1;i++){
			if(keys.get(i+1)-keys.get(i)==1){
				feiji = true;
				feijiKey = keys.get(i);
				break;
			}
		}
		
		if(feiji){
			int val= feijiKey%100;
			return val;
		
		}
		return 0;
	}
	
	
	public static int isShunzi(List<Integer> list){
		List<Integer> valus = new ArrayList<Integer>();
		
		for(Integer id: list){
			int value = CardUtils.loadCardValue(id);
			valus.add(value);
		}
	
		Collections.sort(valus);
		
		
		int count = 0;
		int val= 0;
		for(int i=0;i<valus.size()-1;i++) {
			if(Math.abs(valus.get(i) -valus.get(1+i))==1){
				count ++;
				if(count==5){
					val = valus.get(i);
				}
			}
		}
		
		if(count>=8){
			return val;
		}
		return 0;
		
	}
	

    public static void sortCards(List<Integer> cards) {
        Collections.sort(cards, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                int val1 = loadCardValue(o1);
                int val2 = loadCardValue(o2);
                return val1 - val2;
            }
        });
    }

    public static void sortCardValues(List<CardValue> cards) {
        Collections.sort(cards, new Comparator<CardValue>() {
            @Override
            public int compare(CardValue o1, CardValue o2) {
                return o1.getValue() - o2.getValue();
            }
        });
    }

    /**
     * 组合牌结果
     */
    public static class Result implements Comparable<Result> {
        /**
         * 0无效，1单张，2对子，3三飘，4四带，11单顺子，22双顺子，33飞机，100炸弹，1000：3条A
         */
        private final int type;
        private final int count;
        private final int max;

        public Result(int type, int count, int max) {
            this.type = type;
            this.count = count;
            this.max = max;
        }

        public int getType() {
            return type;
        }

        public int getCount() {
            return count;
        }

        public int getMax() {
            return max;
        }

        @Override
        public String toString() {
            return new StringBuilder(32).append("{type=").append(type).append(",count=").append(count).append(",max=").append(max).append("}").toString();
        }

        /**
         * -100不能比较，大：正数，小：负数，相等：零
         *
         * @param o
         * @return
         */
        @Override
        public int compareTo(Result o) {
            if (this.type <= 0 || o.type <= 0) {
                return -100;
            } else if (this.type == 1000) {
                return 1;
            } else if (o.type == 1000) {
                return -1;
            } else if (this.type == o.type) {
                switch (this.type) {
                    case 100:
                        if (this.count > o.count) {
                            return 1;
                        } else if (this.count < o.count) {
                            return -1;
                        } else {
                            return this.max - o.max;
                        }
                    case 1:
                        return this.max - o.max;
                    case 2:
                        return this.max - o.max;
                    case 3:
                        return this.max - o.max;
                    case 4:
                        return this.max - o.max;
                    case 22:
                        if (this.count == o.count) {
                            return this.max - o.max;
                        } else {
                            return -100;
                        }
                    case 33:
                        if (this.count == o.count) {
                            return this.max - o.max;
                        } else {
                            return -100;
                        }
                    case 11:
                        if (this.count == o.count) {
                            return this.max - o.max;
                        } else {
                            return -100;
                        }
                    default:
                        return -100;
                }
            } else if (this.type == 100) {
                return 1;
            } else if (o.type == 100) {
                return -1;
            } else {
                return -100;
            }
        }
    }

    public static int cardResult2ReturnType(Result result) {
        int cardType;
        switch (result.getType()) {
            case 1:
                cardType = 1;
                break;
            case 2:
                cardType = 2;
                break;
            case 3:
                cardType = 3;
                break;
            case 4:
                cardType = 8;
                break;
            case 11:
                cardType = 5;
                break;
            case 22:
                cardType = 5;
                break;
            case 33:
                cardType = 6;
                break;
            case 100:
                cardType = 4;
                break;
            default:
                cardType = 0;
        }
        return cardType;
    }


    /**
     * 牌按牌型顺序显示
     *
     * @param cards
     * @param cardResult
     * @return
     */
    public static List<Integer> loadSortCards(List<Integer> cards, Result cardResult, int siDai,boolean AAAAha) {
        if (cardResult == null) {
            cardResult = CardUtils.calcCardValue(loadCards(cards), siDai, false,false);
        }
        if (cardResult.getType() == 1 || cardResult.getType() == 2 || cardResult.getType() == 100) {
            return cards;
        } else if (cardResult.getType() == 3 || cardResult.getType() == 4) {//三飘 四带 牌排序显示
            List<Integer> tempList = new ArrayList<>(cards.size());
            for (Integer cv : cards) {
                if (CardUtils.loadCardValue(cv) == cardResult.getMax()) {
                    tempList.add(0, cv);
                } else {
                    tempList.add(cv);
                }
            }
            return tempList;
        }
        //飞机 牌排序显示
        else if (cardResult.getType() == 33) {
            int len = cards.size();
            List<CardValue> tempList = new ArrayList<>(len);
            List<CardValue> copyList = new ArrayList<>(CardUtils.loadCards(cards));

            int count = cardResult.getCount();
            for (int i = 0; i < len; i++) {
                int val = cardResult.getMax() - i;
                if (val <= 0) {
                    break;
                }
                int[] idxs = new int[]{-1, -1, -1};
                int j = 0;
                int m = copyList.size();
                for (int k = 0; k < m; k++) {
                    CardValue cv = copyList.get(k);
                    if (cv.getValue() == val) {
                        idxs[j] = k;
                        j++;
                        if (j >= idxs.length) {
                            break;
                        }
                    }
                }
                if (j > 0) {
                    tempList.add(copyList.remove(idxs[0]));
                    tempList.add(copyList.remove(idxs[1] - 1));
                    tempList.add(copyList.remove(idxs[2] - 2));
                    count--;
                    if (count <= 0) {
                        Collections.sort(copyList, new Comparator<CardValue>() {
                            @Override
                            public int compare(CardValue o1, CardValue o2) {
                                return o1.getValue() - o2.getValue();
                            }
                        });
                        tempList.addAll(copyList);
                        return CardUtils.loadCardIds(tempList);
                    }
                }

            }
        } else if (cardResult.getType() == 11 || cardResult.getType() == 22) {
            Collections.sort(cards, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return loadCardValue(o2) - loadCardValue(o1);
                }
            });
        }

        return cards;
    }

}

