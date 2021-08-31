package com.sy599.game.qipai.qianfen.util;

import java.security.SecureRandom;
import java.util.*;

public final class CardUtils {

    public static final CardValue EMPTY_CARD_VALUE = new CardValue(0,0);
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
     * @param cards
     * @return
     */
    public static int loadCardScore(List<Integer> cards){
        int total = 0;
        for (int card:cards){
            int val=loadCardValue(card);
            if (val==5||val==10){
                total+=val;
            }else if (val==13){
                total+=10;
            }
        }
        return total;
    }


    /**
     * 添加分牌
     * @param cards
     * @return
     */
    public static int filterCardValueScore(List<CardValue> cards,List<Integer> scoreCards){
        int total = 0;
        for (CardValue card:cards){
            int val=card.getValue();
            if (val==5||val==10){
                total+=val;
                scoreCards.add(card.getCard());
            }else if (val==13){
                total+=10;
                scoreCards.add(card.getCard());
            }
        }
        return total;
    }

    /**
     * 计算牌型的得分值
     *
     * @param result
     * @param min 牌的个数
     * @param base 最低分
     * @param rule 记分规则
     * @return
     */
    public static int loadResultScore(Result result,int min,int base,int rule){
        if (result.type==100){
            int temp = result.count-min;
            if(temp==0){
                return base;
            }else if (temp>0){
                if (rule==1){
                    return base+(result.count-min)*base;
                }else if (rule==2){
                    int ratio = 1;
                    for (int i=0;i<temp;i++){
                        ratio *= 2;
                    }
                    return base * ratio;
                }else{
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
     * 计算牌组合结果
     *
     * @param cardValues
     * @param exclusives
     * @return
     */
    public static Result calcCardValue(List<CardValue> cardValues, int... exclusives) {
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
            } else if (count >= 4) {
                return new Result(100, count, kv.getKey());
            }
        } else {
            if (maxCount == 3 && (total % 5 == 0)) {
                if (total == 5) {
                    int max = 0;
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        if (kv.getValue().intValue() == 3) {
                            max = kv.getKey().intValue();
                            break;
                        }
                    }
                    return new Result(3, 1, max);
                } else {
                    int pre = 0;
                    int count = 0;
                    List<Map.Entry<Integer, Integer>> list = new ArrayList<>(valCount);
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        list.add(0, kv);
                    }
                    for (Map.Entry<Integer, Integer> kv : list) {
                        if (kv.getValue().intValue() == 3) {
                            if (pre == 0) {
                                pre = kv.getKey().intValue();
                                count = 1;
                            } else {
                                pre -= 1;
                                while (contains(pre, exclusives)) {
                                    pre -= 1;
                                }
                                if (kv.getKey().intValue() == pre) {
                                    count++;
                                    if ((count * 5) == total) {
                                        break;
                                    }
                                } else {
                                    if ((count * 5) == total) {
                                        break;
                                    } else {
                                        pre = kv.getKey().intValue();
                                        count = 1;
                                    }
                                }
                            }
                        }
                    }
                    if ((count * 5) == total) {
                        int max = pre;
                        int i = 1;
                        while (i < count) {
                            max++;
                            while (contains(max, exclusives)) {
                                max++;
                            }
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
                            while (contains(pre, exclusives)) {
                                pre += 1;
                            }
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
                            while (contains(pre, exclusives)) {
                                pre += 1;
                            }
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
    public static List<CardValue> searchBiggerCardValues(List<CardValue> src, List<CardValue> search, int... exclusives) {
        return searchBiggerCardValues(src, calcCardValue(search, exclusives), exclusives);
    }

    /**
     * 在src中查找比result大的最小值
     *
     * @param src
     * @param result
     * @return
     */
    public static List<CardValue> searchBiggerCardValues(List<CardValue> src, Result result, int... exclusives) {
        if (result.type > 0) {
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
                        }

                        if (val1 > 0 && val2 > 0) {
                            List<CardValue> retList = searchCardValues(src, val, 3);
                            if (val1 == val2) {
                                retList.addAll(searchCardValues(src, val1, 2));
                            } else {
                                retList.addAll(searchCardValues(src, val1, 1));
                                retList.addAll(searchCardValues(src, val2, 1));
                            }
                            return retList;
                        }
                    }
                    break;
                case 11:
                    int min = result.max;
                    for (int i = 0; i < result.count; i++) {
                        min--;
                        while (contains(min, exclusives)) {
                            min--;
                        }
                    }
                    List<Integer> tempList = new ArrayList<>(result.count);
                    int min1 = maxValue;
                    for (int i = 0; i < result.count; i++) {
                        min1--;
                        while (contains(min1, exclusives)) {
                            min1--;
                        }
                    }
                    if (min1 > min) {
                        for (int i = min + 1; i <= min1; i++) {
                            if (tempList.size() == result.count && tempList.get(result.count-1).intValue() > result.max) {
                                break;
                            } else {
                                tempList.clear();
                                for (int m = 0; m < result.count; m++) {
                                    int tempVal = i + m + 1;
                                    while (contains(tempVal, exclusives)) {
                                        tempVal++;
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
                        if (tempList.size() == result.count && tempList.get(result.count-1).intValue() > result.max) {
                            List<CardValue> retList = new ArrayList<>(result.count);
                            for (int i = 0; i < result.count; i++) {
                                retList.addAll(searchCardValues(src, tempList.get(i).intValue(), 1));
                            }
                            return retList;
                        }
                    }
                    break;
                case 22:
                    if(maxValue<result.max)
                        break;
                    min = result.max;
                    for (int i = 0; i < result.count-1; i++) {
                        min--;
                        while (contains(min, exclusives)) {
                            min--;
                        }
                    }
                    //标记连对当前值。
                    int disMin=0;
                    tempList=new ArrayList<>();
                    for (Map.Entry<Integer,Integer> entry:map.entrySet()) {
                        Integer num = entry.getValue();
                        Integer val1 = entry.getKey();
                        if(num<2||num>=4||val1<=min)
                            continue;
                        if(disMin==0){
                            disMin=entry.getKey();
                            tempList.add(disMin);
                            continue;
                        }
                        val1--;
                        while (contains(val1, exclusives)) {
                            val1--;
                        }

                        if(val1==disMin){
                            disMin=entry.getKey();
                            tempList.add(disMin);
                        } else{
                            disMin=entry.getKey();
                            tempList.clear();
                            tempList.add(disMin);
                        }
                        if(tempList.size()==result.count){
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
                        while (contains(min, exclusives)) {
                            min--;
                        }
                    }
                    tempList = new ArrayList<>(result.count);
                    min1 = maxValue;
                    for (int i = 0; i < result.count; i++) {
                        min1--;
                        while (contains(min1, exclusives)) {
                            min1--;
                        }
                    }
                    if (min1 > min) {
                        for (int i = min + 1; i <= min1; i++) {
                            if (tempList.size() == result.count && tempList.get(result.count-1).intValue() > result.max) {
                                break;
                            } else {
                                tempList.clear();
                                for (int m = 0; m < result.count; m++) {
                                    int tempVal = i + m + 1;
                                    while (contains(tempVal, exclusives)) {
                                        tempVal++;
                                    }
                                    int len = countSameCards(src, tempVal);
                                    if (len == 3) {
                                        if (!tempList.contains(tempVal))
                                            tempList.add(tempVal);
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }
                        if (tempList.size() == result.count && tempList.get(result.count-1).intValue() > result.max) {
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
                                }
                            }

                            if (daiCount == daiMax) {
                                for (int i = 0; i < result.count; i++) {
                                    retList.addAll(0, searchCardValues(src, tempList.get(i).intValue(), 3));
                                }
                                return retList;
                            }
                        }
                    }
                    break;
            }

            if (result.type == 100) {
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
        }
        return Collections.emptyList();
    }

    /**
     * 统计相同牌值的个数
     *
     * @param cardValues
     * @return
     */
    public static final Map<Integer,Integer> countValue(List<CardValue> cardValues){
        Map<Integer,Integer> map = new HashMap<>();
        for (CardValue cv : cardValues){
            Integer val = map.getOrDefault(cv.getValue(),0);
            map.put(cv.getValue(),val+1);
        }
        return map;
    }

    /**
     * 组合牌结果
     */
    public static class Result implements Comparable<Result> {
        /**
         * 0无效，1单张，2对子，3三飘，11单顺子，22双顺子，33飞机，100炸弹
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


    public static List<Integer> loadCardsNoRand(int pairs, int... exclusives) {
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
        return list;
    }

    public static List<List<Integer>> zuoPai(List<List<Integer>> zp,int playerCount, int []cutCardVals){
        for (int i = 0; i < playerCount ; i++) {
            if(i>=zp.size()){
                List<Integer> l=new ArrayList<>();
                zp.add(l);
            }
        }
        List<Integer> list = CardUtils.loadCards(3, cutCardVals);
        List<List<Integer>> result=new ArrayList<>();
        for (int i = 0; i < zp.size(); i++) {
            List<Integer> l = new ArrayList<>();
            for (Integer val:zp.get(i)) {
                findCardAndRemove(val,list,l);
            }
            result.add(l);
        }

//        int count=6;
//        int pais = (list.size() - count) / 3;
//        for (int m = 0; m < playerCount; m++) {
//            List<Integer> l = result.get(m);
//            for (int i = l.size(); i < pais; i++) {
//                l.add(list.get(count));
//                count++;
//            }
//        }
        return result;
    }

    public static boolean findCardAndRemove(Integer val, List<Integer> cards, List<Integer> ids){
        Iterator<Integer> it = cards.iterator();
        int i=0;
        while (it.hasNext()){
            i++;
            Integer next = it.next();
            int yu = next % 100;
            if(yu==val){
                it.remove();
                ids.add(next);
                return true;
            }
            if(i>=cards.size())
                break;
        }
        return false;
    }

}
