package com.sy599.game.qipai.zzpdk.util;

import com.sy599.game.qipai.zzpdk.bean.ZZPdkTable;
import com.sy599.game.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.util.*;
import java.util.Map.Entry;


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
     * @return
     */
    public static Result calcCardValue(List<CardValue> cardValues, ZZPdkTable table,boolean isLast) {
        Map<Integer, Integer> valAndNum = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.intValue() - o2.intValue();
            }
        });
        int total = 0;
        int maxCount = 0;
        for (CardValue cv : cardValues) {
            int count = valAndNum.getOrDefault(cv.getValue(), 0);
            count++;
            valAndNum.put(cv.getValue(), count);
            total++;
            if (count > maxCount) {
                maxCount = count;
            }
        }

        int maxVal=0;
        int duiNum=0;
        TreeSet<Integer> set=new TreeSet<>();
        switch (maxCount){
            case 4://大概率为炸弹和四带,不勾选四带一为炸则当三带二
                for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                    if (kv.getValue().intValue() == 4) {
                        maxVal = kv.getKey().intValue();
                    }
                }
                switch (total){
                    case 4:
                        return new Result(Result.zhadan, 1, maxVal);
                    case 5:
                        // 4带1当3带2出
                        if(table.getSidai1()==0&&table.getSandai2()==1)
                            return new Result(Result.sandai, 1, maxVal);
                        else if(table.getSidai1()==1)
                            return new Result(Result.zhadan, 1, maxVal);
                        break;
                    case 6:
                        if(table.getSidai2()==1)
                            return new Result(Result.sidai, 1, maxVal,Result.sidai2);
                        break;
                    case 7:
                        if(table.getSidai3()==1)
                            return new Result(Result.sidai, 1, maxVal,Result.sidai3);
                        break;
                }
                break;
            case 3://三带和3A
                for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                    int count = kv.getValue().intValue();
                    int val=kv.getKey().intValue();
                    if ( count== 3){
                        set.add(val);
                        if(val>=maxVal)
                            maxVal = val;
                    }else if(count==2)
                        duiNum++;
                }
                if(total==3&&maxVal==14&&table.getAAAZha()==1)
                    return new Result(Result.zhadan, 1, 1000);
                if(isLast){
                    if((table.getSandai2()==1||table.getSandaidui()==1)&&total/5<set.size())
                        return new Result(Result.sandai, set.size(), maxVal);
                    if(table.getSandai1()==1&&total/4<set.size())
                        return new Result(Result.sandai, set.size(), maxVal);
                }
                if(total%set.size()!=0||set.last()-set.first()!=set.size()-1)
                    return new Result(Result.undefined, 0, 0);
                switch (total/set.size()){
                    case 3:
                        if(table.getSanzhang()==1)
                            return new Result(Result.sandai, set.size(), maxVal,Result.sandai0);
                        break;
                    case 4:
                        if(table.getSandai1()==1)
                            return new Result(Result.sandai, set.size(), maxVal,Result.sandai1);
                        break;
                    case 5:
                        if(table.sanDai2AndDui()){//可互接的情况，将3带对视为3带2
                            return new Result(Result.sandai, set.size(), maxVal,Result.sandai2);
                        }else {
                            if(duiNum==set.size()&&table.getSandaidui()==1)
                                return new Result(Result.sandai, set.size(), maxVal,Result.sandaidui);
                            else if(table.getSandai2()==1)
                                return new Result(Result.sandai, set.size(), maxVal,Result.sandai2);
                        }
                        break;
                    default:
                        return new Result(Result.undefined, 0, 0);
                }
                break;
            case 2:
                for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                    int val=kv.getKey().intValue();
                    if(kv.getValue()==2){
                        if(maxVal==0||val-maxVal==1){
                            set.add(val);
                            maxVal=val;
                        }else
                            return new Result(Result.undefined, 0, 0);
                    }else
                        return new Result(Result.undefined, 0, 0);
                }
                return new Result(Result.duizi, set.size(), maxVal);
            case 1:
                for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                    int val=kv.getKey().intValue();
                    if(maxVal==0||val-maxVal==1){
                        set.add(val);
                        maxVal=val;
                    } else
                        return new Result(Result.undefined, 0, 0);
                }
                if(set.size()>=5||set.size()==1)
                    return new Result(Result.danzhan, set.size(), maxVal);
        }
        return new Result(Result.undefined, 0, 0);
    }

    /**
     * 在src中查找比result大的最小值
     *
     * @param src
     * @param result
     * @return
     */
    public static List<CardValue> searchBiggerCardValues(List<CardValue> src, Result result,ZZPdkTable table,int... exclusives) {
        if (result.type > 0) {
            List<CardValue> AAA = new ArrayList<>();
            if(table.getAAAZha()==1) {
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


            Map<Integer, Integer> valAndNum = new TreeMap<>(new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o1.intValue() - o2.intValue();
                }
            });
            int maxCount = 0;
            int maxValue = 0;
            for (CardValue cv : src) {
                int count = valAndNum.getOrDefault(cv.getValue(), 0);
                count++;
                valAndNum.put(cv.getValue(), count);
                if (count > maxCount) {
                    maxCount = count;
                }
                if (cv.getValue() > maxValue) {
                    maxValue = cv.getValue();
                }
            }
            int val = 0;


            if (result.type == Result.zhadan) {
                //先找四炸
                for(Map.Entry<Integer, Integer> kv:valAndNum.entrySet()){
                    if(kv.getValue()==4&&kv.getKey()>result.max)
                        return searchCardValues(src, kv.getKey().intValue(), 4);
                }
            } else if (maxCount >= 4) {
                for (int i = 4; i <= maxCount; i++) {
                    for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                        if (kv.getValue().intValue() == i) {
                            return searchCardValues(src, kv.getKey().intValue(), i);
                        }
                    }
                }
            }

            if(table.getAAAZha()==1){
                if(AAA.size() == 3) {
                    // 有3条A炸，要放回去
                    src.addAll(AAA);
                    valAndNum.clear();
                    for (CardValue cv : src) {
                        int count = valAndNum.getOrDefault(cv.getValue(), 0);
                        count++;
                        valAndNum.put(cv.getValue(), count);
                    }
                }
                // 再找3条A
                for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                    if (kv.getValue().intValue() == 3 && kv.getKey().intValue() == 14) {
                        val = kv.getKey().intValue();
                        return searchCardValues(src, val, 3);
                    }
                }
            }


            switch (result.type) {
                case Result.danzhan:
                    return checkShunExist(src,result,valAndNum);
                case Result.duizi:
                    return checkShunExist(src,result,valAndNum);
                case Result.sandai:
                    if(src.size()<=5){
                        Result rest = calcCardValue(src, table, table.getCard3Eq()==1);
                        Integer i = compareTo(result, rest, table);
                        if(i!=null&&i>0)
                            return src;
                    }else {
                        return checkSanDaiExist(src, result, valAndNum, table);
                    }
            }
        }
        return Collections.emptyList();
    }


    private static List<CardValue> checkShunExist(List<CardValue> cards,Result cardType,Map<Integer,Integer> valAndNum){
        List<CardValue> result=new ArrayList<>();
        int min=0;
        for (Map.Entry<Integer,Integer> entry:valAndNum.entrySet()) {
            if(entry.getKey()>cardType.getMax()-cardType.getCount()+1&&entry.getValue()>=cardType.getType()){
                if(findNowVal(cardType,valAndNum,entry.getKey(),1)){
                    min=entry.getKey();
                    break;
                }
            }
        }
        if(min==0)
            return Collections.emptyList();

        for (int i = 0; i < cardType.count; i++) {
            result.addAll(searchCardValues(cards, min+i, cardType.getType()));
        }
        return result;
    }

    private static List<CardValue> checkSanDaiExist(List<CardValue> cards,Result cardType,Map<Integer,Integer> valAndNum,ZZPdkTable table){
        List<CardValue> retList=new ArrayList<>();
        int count=cardType.getCount();
        List<Integer> mins=new ArrayList<>();
        for (Map.Entry<Integer,Integer> entry:valAndNum.entrySet()) {
            if(entry.getKey()>cardType.getMax()-count&&entry.getValue()>=3){
                if(findNowVal(cardType,valAndNum,entry.getKey(),1)){
                    mins.add(entry.getKey());
                }
            }
        }
        if(mins.size()==0)
            return Collections.emptyList();
        for (Integer min:mins){
            List<CardValue> sanDai = findSanDai(cards, cardType, valAndNum,min,table);
            if(sanDai.size()>0){
                retList.addAll(sanDai);
                break;
            }
        }
        return retList;
    }

    private static List<CardValue> findSanDai(List<CardValue> cards, Result cardType, Map<Integer, Integer> valAndNum, int min,ZZPdkTable table){
        List<CardValue> result=new ArrayList<>();
        for (int i = 0; i < cardType.count; i++) {
            result.addAll(searchCardValues(cards, min+i, cardType.getType()));
        }
        //此处type为接上一家人的牌型，则默认勾选对应三带选项，不做确认
        if(cardType.getAddType()==Result.sandai0)
            return result;
        else if(cardType.getAddType()==Result.sandaidui&&table.getSandai2()==0){
            //确定勾选三带对的情况下，如果勾选三带二则可用三带二接，否则只能用三带对接。
            Map<Integer, Integer> copy = removeMapByMinVal(valAndNum, min, cardType.getCount(), cardType.getType());
            List<Integer> duiVals=new ArrayList<>();
            for (Map.Entry<Integer,Integer> entry:copy.entrySet()) {
                if(entry.getValue()>=2){
                    duiVals.add(entry.getKey());
                    if(duiVals.size()>=cardType.getCount())
                        break;
                }
            }
            if(duiVals.size()>=cardType.getCount()){
                for (Integer duiVal:duiVals) {
                    result.addAll(searchCardValues(cards, duiVal, cardType.getType()));
                }
                return result;
            }
        }else{
            int daiNum=0;
            switch (cardType.getAddType()){
                case Result.sandai1:
                    daiNum=cardType.getCount();
                    break;
                case Result.sandai2:
                case Result.sandaidui:
                    daiNum=2*cardType.getCount();
                    break;
            }
            List<Integer> danVals=new ArrayList<>();
            Map<Integer, Integer> copy = removeMapByMinVal(valAndNum, min, cardType.getCount(), cardType.getType());

            for (Map.Entry<Integer,Integer> entry:copy.entrySet()) {
                if(entry.getValue()>0){
                    for (int i = 1; i <= entry.getValue(); i++) {
                        danVals.add(entry.getKey());
                    }
                    if(danVals.size()>=daiNum)
                        break;
                }
            }
            if(danVals.size()>=daiNum){
                for (int i = 0; i < daiNum; i++) {
                    result.addAll(searchCardValues(cards, danVals.get(i), 1));
                }
                return result;
            }
        }
        return Collections.emptyList();
    }

    /**
     * 从传入的userCount开始遍历valAndNum，递归查询是否存在递增的nowVal的单双顺子或三张
     * @param result
     * @param valAndNum
     * @param nowVal
     * @param useCount
     * @return
     */
    private static boolean findNowVal(Result result,Map<Integer,Integer> valAndNum,int nowVal,int useCount){
        if(useCount>result.getCount())
            return true;
        if(result.getCount()>1&&nowVal>14)
            return false;
        if(valAndNum.containsKey(nowVal)&&valAndNum.get(nowVal)>=result.getType())
            return findNowVal(result,valAndNum,nowVal+1,useCount+1);
        return false;
    }

    private static Map<Integer,Integer> removeMapByMinVal(Map<Integer,Integer> valAndNum,int min,int count,int type){
        Map<Integer,Integer> copy=new TreeMap<>();
        for (Map.Entry<Integer,Integer> entry:valAndNum.entrySet()) {
            Integer val = entry.getKey();
            if(val>=min&&val<=min+count-1){
                if(entry.getValue()>type)
                    copy.put(val,entry.getValue()-type);
            }else
                copy.put(val,entry.getValue());
        }
        return copy;
    }

    /**
     *
     * @param src          手牌，用于找出需要的牌
     * @param valAndNum
     * @param searchType   1，单张，2，对子，3，三张
     * @param addSrcType   递归查询控制变量
     * @param searchCount  查询总数
     * @param findVal      查找牌大于findVal
     * @return
     */
    private static List<CardValue> searchValByType(List<CardValue> src,Map<Integer,Integer> valAndNum,int searchType,
                                              int addSrcType,int searchCount, int findVal){
        if(addSrcType>3)
            return Collections.emptyList();
        List<Integer> srcVal=new ArrayList<>();
        List<CardValue> result=new ArrayList<>();
        for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
            if (kv.getValue().intValue() == addSrcType && kv.getKey().intValue() > findVal) {
                srcVal.add(kv.getKey());
                if(srcVal.size()==searchCount)
                    break;
            }
        }
        if(srcVal.size()>=searchCount){
            for (Integer val:srcVal){
                result.addAll(searchCardValues(src, val, searchType));
            }
        }else {
            List<CardValue> searchsome = searchValByType(src, valAndNum, searchType,searchType + 1, searchCount - srcVal.size(), findVal);
            if(searchsome.size()+srcVal.size()<searchCount)
                return Collections.emptyList();
            result.addAll(searchsome);
        }
        return result;
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
    public static class Result {
        /**
         * 0无效，1单张，2对子，3三飘，4四带，11单顺子，22双顺子，33飞机，100炸弹，1000：3条A
         */
        private int type;
        private int count;
        private int max;

        public final static int undefined=0;
        public final static int danzhan=1;//单顺归到此类，用count数量标记单顺数量
        public final static int duizi=2;//连对归到此类，用count数量标记连对数量
        public final static int sandai=3;//飞机归到此类，用count数量标记飞机数量
        public final static int sidai=4;

        public final static int zhadan=100;

        //附加类型，只有3带和4带能用到，通常情况下相同type不同addType不能接，勾选少带可接完后，最后三代可以接
        private int addType=0;
        public final static int sandai0=5;
        public final static int sandai1=6;
        public final static int sandai2=7;
        public final static int sandaidui=8;

        public final static int sidai2=9;
        public final static int sidai3=10;

        //已废弃
        public final static int feiji=33;
        public final static int shuangshun=22;
        public final static int danshun=11;

        public Result(int type, int count, int max) {
            if(type==danzhan&&count<5){
                this.type = type;
                this.count = 1;
                this.max = max;
            }else {
                this.type = type;
                this.count = count;
                this.max = max;
            }
        }

        public Result(int type, int count, int max,int addType) {
            this.type = type;
            this.count = count;
            this.max = max;
            this.addType = addType;
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

        public int getAddType() {
            return addType;
        }

        public boolean isFeiJi(){
            return type==sandai&&count>1;
        }

        public boolean isLianDui(){
            return type==duizi&&count>1;
        }

        public boolean isShunZi(){
            return type==danzhan&&count>=51;
        }

        public boolean isSanDai(){
            return type==sandai&&count==1;
        }

        public boolean isDanZhan(){
            return type==danzhan&&count==1;
        }

        public boolean isDuiZi(){
            return type==duizi&&count==1;
        }


        @Override
        public String toString() {
            return new StringBuilder(32).append("{type=").append(type).append(",count=").append(count).append(",max=").append(max).append("}").toString();
        }

        public void init(String data) {
            if (!StringUtils.isBlank(data)) {
                String[] values = data.split("_");
                type = StringUtil.getIntValue(values, 0);
                count = StringUtil.getIntValue(values, 1);
                max = StringUtil.getIntValue(values, 2);
                addType = StringUtil.getIntValue(values, 3);
            }
        }

        public String toStr() {
            StringBuffer sb = new StringBuffer();
            if(type>0){
                sb.append(type).append("_");
                sb.append(count).append("_");
                sb.append(max).append("_");
                sb.append(addType).append("_");
            }
            return sb.toString();
        }

        public void clear(){
            type = 0;
            count = 0;
            max = 0;
            addType = 0;
        }
    }

    /**
     *
     * @param last 上一手牌
     * @param next 接牌
     * @param table
     * @return 0相等，null无法比较
     */
    public static Integer compareTo(Result last,Result next,ZZPdkTable table){
        if(next.getType()==Result.undefined)
            return null;
        if(last.getType()!=next.getType()){
            if(next.getType()==Result.zhadan){
                if(last.getType()==Result.zhadan)
                    return next.getMax()-last.getMax();
                else
                    return 1;
            }
        }else {
            //单张只能和单张比较，不能和顺子比较，同理对子和三代。
            if(next.getCount()==last.getCount()){
                switch (next.getType()){
                    case Result.danzhan:
                    case Result.duizi:
                    case Result.zhadan:
                        return next.getMax()-last.getMax();
                    case Result.sandai://三带和四代还得继续判断addType 看是否能接
                        if(table.getCard3Eq()==1){
                            if(next.getAddType()<=last.getAddType())
                                return next.getMax()-last.getMax();
                        }else {
                            if(next.getAddType()==last.getAddType())
                                return next.getMax()-last.getMax();
                        }
                        break;
                    case Result.sidai:
                        if(next.getAddType()==last.getAddType())
                            return next.getMax()-last.getMax();
                        break;
                }
            }
        }


        return null;
    }

    public static int cardResult2ReturnType(Result result) {
        int cardType;
        int type = result.getType();
        switch (type) {
            case Result.danzhan:
                cardType = result.isShunZi()?5:type;
                break;
            case Result.duizi:
                cardType = result.isLianDui()?5:type;
                break;
            case Result.sandai:
                cardType = result.isFeiJi()?6:type;
                break;
            case Result.sidai:
                cardType = 8;
                break;
            case Result.zhadan:
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
    public static List<Integer> loadSortCards(List<Integer> cards, Result cardResult,ZZPdkTable table) {
        if (cardResult == null) {
            cardResult = CardUtils.calcCardValue(loadCards(cards), table,false);
        }
        if (cardResult.isDanZhan() || cardResult.isDuiZi() || cardResult.getType() == Result.zhadan) {
            return cards;
        } else if (cardResult.isSanDai() || cardResult.getType() == Result.sidai) {//三飘 四带 牌排序显示
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
        else if (cardResult.isFeiJi()) {
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
        } else if (cardResult.isLianDui()||cardResult.isShunZi()) {
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

