package com.sy599.game.qipai.dtz.tool;

import com.sy599.game.qipai.dtz.bean.*;
import com.sy599.game.qipai.dtz.compare.AutoPlay;
import com.sy599.game.qipai.dtz.compare.AutoPlayFactory;
import com.sy599.game.qipai.dtz.compare.CanPlayCompare;
import com.sy599.game.qipai.dtz.compare.CanPlayCompareFactory;
import com.sy599.game.qipai.dtz.compare.CardTypeFactory;
import com.sy599.game.qipai.dtz.constant.DtzConstant;
import com.sy599.game.qipai.dtz.constant.DtzzConstants;
import com.sy599.game.qipai.dtz.rule.CardTypeDtz;
import com.sy599.game.qipai.dtz.rule.ScoreType;
import org.javatuples.Pair;

import java.util.*;
import java.util.Map.Entry;

/**
 * 牌型工具
 * 判断牌型，等
 * @author lc
 *
 */
public class CardTypeToolDtz implements Comparator<List<Integer>>{
	
	/**
	 * 比较器
	 */
	public static Comparator<Integer> comparator = new Comparator<Integer>() {
        public int compare(Integer o1, Integer o2) {
        	int card1 = CardToolDtz.toVal(o1), card2 = CardToolDtz.toVal(o2);
        	if (card1 > card2) {
        		return 1;
        	}
        	else if (card1 == card2) {
        		return 0;
        	}
        	else {
        		return -1;
        	}
        }
    };
	/**
	 * 以自己为基准比较对手
	 */
	@Override
	public int compare(List<Integer> from, List<Integer> op) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	/**
	 * 判断单牌
	 * @param pokers 牌列表（带花色）
	 * @return
	 */
	private static boolean  isSingle(List<Integer> pokers) {
		return pokers.size() == 1;
	}
	
	/**
	 * 判断对子
	 * @param pokers 牌列表（带花色）
	 * @return
	 */
	private static boolean isPair(List<Integer> pokers) {
		return (pokers != null && pokers.size() == 2 && CardToolDtz.toVal(pokers.get(0).intValue()) == CardToolDtz.toVal(pokers.get(1).intValue()));
	}
	
	/**
	 * 判断三张（包括筒子）
	 * @param pokers 牌列表（带花色）
	 * @return
	 */
	public static boolean isThree(List<Integer> pokers) {
		return (pokers != null && pokers.size() == 3 && CardToolDtz.toVal(pokers.get(0).intValue()) == CardToolDtz.toVal(pokers.get(1).intValue()) && CardToolDtz.toVal(pokers.get(0).intValue()) == CardToolDtz.toVal(pokers.get(2).intValue()));
	}
	
	/**
	 * 判断炸弹 四张以上就是炸弹
	 * @param pokers 牌列表（不带花色，只有数字）
	 * @return
	 */
	public static boolean isBomb(List<Integer> pokers) {
		if (pokers == null || pokers.size() < 4) return false;
		int value = pokers.get(0);
		for(int poker:pokers){
			if(value != poker)return false;
		}
		return true;
	}
	
	/**
	 * 判断筒子，数字和花色都相同的3个牌
	 * @param pokers 牌列表（带花色）
	 */
	public static boolean isTongZi(List<Integer> pokers) {
		return (pokers != null && pokers.size() == 3 && pokers.get(0).intValue() == pokers.get(1).intValue() && pokers.get(0).intValue() == pokers.get(2).intValue());
	}
	
	/**
	 * 判断喜
	 * @param pokers 牌列表（带花色）
	 */
	public static boolean isXi(List<Integer> pokers) {
		return (pokers != null && pokers.size() == 4 && pokers.get(0).intValue() == pokers.get(1).intValue() && pokers.get(0).intValue() == pokers.get(2).intValue() && pokers.get(0).intValue() == pokers.get(3).intValue());
	}
	
	
	
	
	/**
	 * 判断地炸（3副牌）， 8个同样数字的牌，黑红梅方各两张
	 * @param pokers 牌列表（带花色）
	 * @return
	 */
	public static boolean isBobmDi(List<Integer> pokers) {
		if (pokers == null || pokers.size() != 8) return false;
		//判断牌数字都一样&四种花色各2个
		int value = CardToolDtz.toVal(pokers.get(0));
		int hei = 0,hong = 0,mei = 0,fang = 0;
		for(int poker:pokers){
			if(value != CardToolDtz.toVal(poker))return false;
			if(CardToolDtz.toSuit(poker)==400)hei++;
			if(CardToolDtz.toSuit(poker)==300)hong++;
			if(CardToolDtz.toSuit(poker)==200)mei++;
			if(CardToolDtz.toSuit(poker)==100)fang++;
		}
		if(hei==hong&&mei==fang&&hei==mei&&hei==2)return true;
		return false;
	}
	

    /**
     * 单顺：五张或更多数值连续的单牌(如： 45678 或 78910JQK )。不包括 2 和双王。
	 * @param pokers 牌列表（带花色）
	 * @return
	 */
    private static boolean isStraight(List<Integer> pokers){
        if(pokers == null || pokers.size() < 5)return false;
        int first = pokers.get(0);
        for(int i = 1; i < pokers.size(); i++){
            if(first + i != pokers.get(i))return false;
        }
        return true;
    }
    
    
    
    /**
     * 双顺：二对或更多数值连续的对牌  如：3344,7788991010JJ 注意，3副牌当中2不能连对
     * @param pokers 牌列表（不带花色，只有数字）
     * @return
     */
    private static boolean isDoubleStraight(List<Integer> pokers, DtzTable table) {
    	//双数
        if(pokers == null || pokers.size() < 4 || pokers.size()%2 != 0)return false;
        //先排序
        Collections.sort(pokers, comparator);
        //循环判断
        int start = pokers.get(0);
        int play_type = table.getPlayType();
        for(int i=1;i<=pokers.size();i++){
        	if(pokers.get(i-1) != start)return false;
        	//双数 值加1
        	if(i%2==0)start++;
        	//判断是否是三副牌带2了
        	if(table.isThreePai() && pokers.get(i-1) >= 15 && table.getWangTongZi()==0)return false;
        }
        return true;
    }
    
    /**
     * 三顺：二个或更多数值连续的三张牌(如：333444 、555666777888) 注意 三副牌不包括2
     * @param pokers 牌列表（不带花色，只有数字）
     * @return
     */
    private static boolean isThreeStraight(List<Integer> pokers, DtzTable table) {
    	//3的倍数
        if(pokers == null || pokers.size() < 6 || pokers.size()%3 != 0)return false;
        //先排序
        Collections.sort(pokers, comparator);
        //循环判断
        int start = pokers.get(0);
        int play_type = table.getPlayType();
        for(int i=1;i<=pokers.size();i++){
        	if(pokers.get(i-1) != start)return false;
        	//3的倍数 值加1
        	if(i%3==0)start++;
        	//判断是否是三副牌带2了
//        	if((play_type == DtzzConstants.play_type_3POK 
//        			|| play_type == DtzzConstants.play_type_3PERSON_3POK 
//        			|| play_type == DtzzConstants.play_type_2PERSON_3POK) && pokers.get(i-1) >= 15 && table.getWangTongZi()==0)return false;
//        	if(play_type == DtzzConstants.play_type_3POK || play_type == DtzzConstants.play_type_3PERSON_3POK || play_type == DtzzConstants.play_type_2PERSON_3POK){
//        		if(pokers.get(i-1) >= 15 || (table.getWangTongZi()==0 && pokers.get(i-1) > 15)){
//        			return false;
//        		}
//        	}else{
//        		if(pokers.get(i-1) > 15){
//        			return false;
//        		}
//        	}
        	if(table.isFourPai() || table.getWangTongZi()==1){
        		if(pokers.get(i-1) > 15){
        			return false;
        		}
        	}else{
        		if(pokers.get(i-1) > 14){
        			return false;
        		}
        	}
        }
        return true;
    }
    
    /**
     * 三顺：二个或更多数值连续的三张牌(如：333444 、555666777888) 注意 三副牌不包括2
     * @param pokers 牌列表（不带花色，只有数字）
     * @return
     */
    private static boolean isThreeStraight(List<Integer> pokers) {
    	//3的倍数
        if(pokers == null || pokers.size() < 6 || pokers.size()%3 != 0)return false;
        //先排序
        Collections.sort(pokers, comparator);
        //循环判断
        int start = pokers.get(0);
        for(int i=1;i<=pokers.size();i++){
        	if(pokers.get(i-1) != start)return false;
        	//3的倍数 值加1
        	if(i%3==0)start++;
        }
        return true;
    }
    
    /**
     * 飞机带翅膀，3副牌才有：三顺+同数量的任意单牌(或同数量的任意对牌)。(如：44455579，3334445557799JJ，3334445555)
     * @param pokers 牌列表（不带花色，只有数字）
     * @return
     */
    public static boolean isPlan(List<Integer> pokers, DtzTable table) {
    	//数量判断
        if(pokers == null || pokers.size() < 7)return false;
        //先得到所有出现次数为3以上的牌
        HashMap<Integer, Integer> map = new HashMap<>();
        for(int card:pokers){
    		if (!map.containsKey(card)) map.put(card,1);
    		else map.put(card, map.get(card)+1);
    	}
        List<Integer> vList = new ArrayList<Integer>();
        for (Entry<Integer, Integer> entry : map.entrySet()) {
        	//三副牌飞机不能有2为三个的
    		if (entry.getValue() >= 3 && (entry.getKey() <= 14 || ((table.getWangTongZi()==1 || table.isFourPai()) && entry.getKey() <= 15))) {
    			vList.add(entry.getKey());
    		}
    	}
        //不够2连，返回
        if(vList.size() < 2)return false;
        //排序后判断得到最大的连续飞机
        Collections.sort(vList, comparator);
        //得到次数列表
        List<Integer> countList = new ArrayList<Integer>();
        int stV = vList.get(0);
        int count = 1;
        for(int i=1;i<vList.size();i++){
        	if(vList.get(i) == stV+1){
        		count++;
        		if(i==vList.size()-1){
        			//如果是最后一个满足条件，需要加入列表
        			countList.add(count);
        		}
        		stV = vList.get(i);
        	}else{
        		countList.add(count);
        		count = 1;
        		stV = vList.get(i);
        	}
        }
        //得到最大的飞机连续数量
        int maxcount = 0;
        for(int c:countList){
        	if(c>maxcount)maxcount=c;
        }
        //判断牌的数量是否符合规则
        if(pokers.size()>(maxcount*5))return false;
        return true;
    }
    
    /**
     * 我的判断飞机带翅膀的方法
     * @return
     */
    public static boolean isPlan2(List<Integer> pokers) {
    	ArrayList<Entry<Integer, Integer>>  cardl = new ArrayList<>();
    	for (int val : pokers) {
    		if (!constainKey(val, cardl)) { //看他是不是包含
    			Entry<Integer, Integer> entry = new AbstractMap.SimpleEntry<Integer, Integer>(val, 1);
    			cardl.add(entry); //加进去
    		}
    		else {
    			Entry<Integer, Integer> entry = getValue(val, cardl);
    			if (entry != null) {
    				if (entry.getValue() >= 3) {
            			cardl.add(new AbstractMap.SimpleEntry<Integer, Integer>(val, 1)); //加进去
        			}
        			else {
        				entry.setValue(entry.getValue() + 1);
        			}
    			}
    			else {
    				cardl.add(new AbstractMap.SimpleEntry<Integer, Integer>(val, 1)); //加进去
    			}
    		}
    	}
    	//
    	ArrayList<Integer> keys = toKeys(cardl), cardTemp = new ArrayList<>();
    	for (int i = 0; i < keys.size(); i ++) {
    		int firstVal = keys.get(i);
    		int j = i + 1;
    		for (;j < keys.size(); j ++) {
    			if (firstVal + 1 == keys.get(j)) {
    				if (!cardTemp.contains(firstVal)) {
        				cardTemp.add(firstVal);
    				}
    				cardTemp.add(firstVal + 1);
    				firstVal = keys.get(j);
    			}
    			else firstVal = keys.get(j);
    		}
    		if (cardTemp.size() >= 2) {
    			break;
    		}
    		else {
    			cardTemp.clear();
    		}
    	}
    	int max = cardTemp.size() * 2;
    	ArrayList<Entry<Integer, Integer>> cards = new ArrayList<>(cardl);
    	for (Entry<Integer, Integer> entry : cardl) {
    		if (cardTemp.contains(entry.getKey())) {
    			cards.remove(entry);
    			cardTemp.remove(entry.getKey());
    		}
    	}
    	int cardNum = 0;
    	for (Entry<Integer, Integer> entry : cards) {
    		cardNum += entry.getValue();
    	}
    	
    	return cardNum >= 1 && cardNum <= max;
    }
    
    /**
     * 得到飞机带翅膀里面里面最大的一张牌
     * @param pokers
     * @return
     */
    public static int getPlanMaxCard(List<Integer> pokers) {
    	if (!isPlan2(pokers)) {
    		return 0;
    	}
    	ArrayList<Entry<Integer, Integer>>  cardl = new ArrayList<>();
    	for (int val : pokers) {
    		if (!constainKey(val, cardl)) { //看他是不是包含
    			Entry<Integer, Integer> entry = new AbstractMap.SimpleEntry<Integer, Integer>(val, 1);
    			cardl.add(entry); //加进去
    		}
    		else {
    			Entry<Integer, Integer> entry = getValue(val, cardl);
    			if (entry != null) {
    				if (entry.getValue() >= 3) {
            			cardl.add(new AbstractMap.SimpleEntry<Integer, Integer>(val, 1)); //加进去
        			}
        			else {
        				entry.setValue(entry.getValue() + 1);
        			}
    			}
    			else {
    				cardl.add(new AbstractMap.SimpleEntry<Integer, Integer>(val, 1)); //加进去
    			}
    		}
    	}
    	//
    	ArrayList<Integer> keys = toKeys(cardl), cardTemp = new ArrayList<>();
    	for (int i = 0; i < keys.size(); i ++) {
    		int firstVal = keys.get(i);
    		int j = i + 1;
    		for (;j < keys.size(); j ++) {
    			if (firstVal + 1 == keys.get(j)) {
    				if (!cardTemp.contains(firstVal)) {
        				cardTemp.add(firstVal);
    				}
    				cardTemp.add(firstVal + 1);
    				firstVal = keys.get(j);
    			}
    			else firstVal = keys.get(j);
    		}
    		if (cardTemp.size() >= 2) {
    			break;
    		}
    		else {
    			cardTemp.clear();
    		}
    	}
    	return Collections.max(cardTemp);
    }
    
    public static int getMaxCard(List<Integer> pokers) {
    	if (isPlan2(pokers)) {
    		return getPlanMaxCard(pokers);
    	}
    	else if (isThreeStraight(pokers)) {
    		LinkedHashMap<Integer, Integer> linkedHashMap = new LinkedHashMap<Integer, Integer>();
            for (int _val : pokers) {
                if (!linkedHashMap.containsKey(_val)) {
                    linkedHashMap.put(_val, 1);
                } else {
                    linkedHashMap.put(_val, linkedHashMap.get(_val) + 1);
                }
            }
            ArrayList<Integer> list = new ArrayList<Integer>(linkedHashMap.keySet());
            return Collections.max(list);
    	}
    	else return 0;
    }
    
    private static ArrayList<Integer> toKeys(List<Entry<Integer, Integer>> list) {
    	ArrayList<Integer> cards = new ArrayList<>();
    	HashSet<Integer> tp = new HashSet<>();
    	for (Entry<Integer, Integer> entry : list) {
    		if (entry.getValue() == 3) {
    			tp.add(entry.getKey());
    		}
    	}
    	cards.addAll(tp);
    	Collections.sort(cards, comparator); //必须从小到大排序
    	return cards;
    }
    
    private static boolean constainKey(int key, List<Entry<Integer, Integer>> cardl) {
    	for (Entry<Integer, Integer> entry : cardl) {
    		if (entry.getKey() == key) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private static Entry<Integer, Integer> getValue(int key, List<Entry<Integer, Integer>> cardl) {
    	for (Entry<Integer, Integer> entry : cardl) {
    		if (entry.getKey() == key && entry.getValue() < 3) {
    			return entry;
    		}
    	}
    	return null;
    }
    
   
    /**
     * 获得tolal 
     * @param keys
     * @param index
     * @param map
     * @return
     */
    private static int getPlanValues(List<Integer> keys, int index, Map<Integer, Integer> map) {
    	int values = 0;
    	for (;index < keys.size(); index ++) {
    		values += map.get(keys.get(index));
    	}
    	return values;
    }
    
    /**
     * 判断三带一或者三带二（可以是两个不一样的带牌）,3副牌的牌型，如：3331,44412,55566,66667,注意（4444是不能做为三个4带一个4的）
     * @param pokers 牌列表（不带花色，只有数字）
     * @return
     */
    private static boolean isThreeWithone(List<Integer> pokers) {
    	if(pokers == null || (pokers.size() != 4 && pokers.size() != 5))return false;
    	HashMap<Integer, Integer> map = new HashMap<>();
    	for(int poker:pokers){
    		if (!map.containsKey(poker)){
    			map.put(poker, 1);
    		}else{
    			map.put(poker, map.get(poker)+1);
    		}
    	}
//    	for (int count : map.values()){
//    		if (count >= 3)return true;
//    	}
    	for(Entry<Integer, Integer> entry : map.entrySet()){
    		if(entry.getKey() <= 15 && entry.getValue()>=3){
    			return true;
    		}
    	}
    	return false;
    }
    
	/**
	 * 添加到map
	 * @param val
	 * @param map
	 */
	public static void addToMap(int val,  Map<Integer, Integer> map) {
		if (!map.containsKey(val)) {
			map.put(val, 1);
		}
		else {
			map.put(val, map.get(val) + 1);
		}
	}
	
	/**
	 * 判断王炸（大王+小王）
	 * @param pokers 牌列表（不带花色，只有数字）
	 * @return
	 */
	private static boolean isJockerboom(List<Integer> pokers) {
        if (pokers == null || pokers.size() != 2) return false;
        if ((pokers.get(0) == 16 && pokers.get(1) == 17) || (pokers.get(1) == 16 || pokers.get(0) == 17))return true;
        return false;
    }
	

	
	/**
	 * 得到xi的类型， 得到的是具体的牌的数字
	 * @param pokers
	 * @return
	 */
	public static int xiTypeOf(List<Integer> pokers) {
		if (!isXi(pokers)) return 0;
		return CardToolDtz.toVal(pokers.get(0));
	}
	
	/**
	 * 看xi的类型是不是我指定这种类型
	 * @param pokers
	 * @param card
	 * @return
	 */
	public static boolean xiTypeOf_(List<Integer> pokers, int card) {
		if (!isXi(pokers)) return false;
		return xiTypeOf(pokers) == card;
	}

    /**
     * 看xi的类型是不是我指定这种类型
     * @param pokers
     * @return
     */
    public static boolean xiTypeOf_5Q(List<Integer> pokers) {
        if (!isXi(pokers)) return false;
        return xiTypeOf(pokers) >=5 && xiTypeOf(pokers) <= 12;
    }
	
	
	/**
	 * 得到出牌的牌型
	 * @param pokers 牌列表
	 */
	public static CardTypeDtz toPokerType(List<Integer> pokers, DtzTable aTable) {
		//判断参数
		if(aTable == null || pokers == null || pokers.size() == 0)return null;
		
		//判断单牌
		if(isSingle(pokers))return CardTypeDtz.CARD_1;
		//判断对子
		if(isPair(pokers))return CardTypeDtz.CARD_2;
		
		//先判断筒子和喜，后判断炸弹和三张，这样就排除炸弹（三张）里面会有喜（筒子）
		//判断筒子
		if(isTongZi(pokers))return CardTypeDtz.CARD_10;
		//判断喜，4副牌牌型
		if(aTable.isFourPai() && isXi(pokers))return CardTypeDtz.CARD_13;
		
		//判断三张
		if(isThree(pokers))return CardTypeDtz.CARD_3;
		
		//先判断地炸，这样就排除炸弹里面有地炸
		//判断地炸,3副牌才有地炸
		if(aTable.isThreePai() && isBobmDi(pokers))return CardTypeDtz.CARD_11;
		//判断炸弹
		if(isBomb(CardToolDtz.toValueList(pokers)))return CardTypeDtz.CARD_9;
		//判断三带一或者三带二,3副牌的牌型
//		if((aTable.getPlayType() == DtzzConstants.play_type_3POK 
//				|| aTable.getPlayType() == DtzzConstants.play_type_3PERSON_3POK 
//				|| aTable.getPlayType() == DtzzConstants.play_type_2PERSON_3POK) && isThreeWithone(CardToolDtz.toValueList(pokers)))return CardTypeDtz.CARD_4;
		if(aTable.getIsDaiPai() == 1 && isThreeWithone(CardToolDtz.toValueList(pokers)))return CardTypeDtz.CARD_4;
		//判断双顺（连对），注意3副牌当中2不能连对
		if(isDoubleStraight(CardToolDtz.toValueList(pokers),aTable))return CardTypeDtz.CARD_6;
		//判断三顺（连续三个，不带任何牌），注意3副牌当中2不能三顺
		if(isThreeStraight(CardToolDtz.toValueList(pokers),aTable))return CardTypeDtz.CARD_7;
		//判断飞机带翅膀,3副牌才有飞机带翅膀,而且不能有2
		if(aTable.getIsDaiPai() == 1 && isPlan(CardToolDtz.toValueList(pokers), aTable))return CardTypeDtz.CARD_8;
		//判断大王+小王的炸弹，打筒子暂时用不到
		//if(isJockerboom(CardToolDtz.toValueList(pokers)))return CardTypeDtz.CARD_12;
		//判断单顺，打筒子暂时用不到
		//if(isStraight(CardToolDtz.toValueList(pokers)))return CardTypeDtz.CARD_5;
		//如果都不是，牌型错误，不能出牌
		return CardTypeDtz.CARD_0;
	}
	
	/**
	 * 比较两副牌大小
	 * @param from 别人出的原牌（带花色）
	 * @param own 我出的原牌（带花色）
	 * @param table
	 * @return 
	 */
	public static int comparePoker(List<Integer> from, List<Integer> own, DtzTable table) {
		//判断参数
		if(from == null || from.size() == 0 || own == null || own.size() == 0)return DtzConstant.CARD_ERROR;
		if(table == null)return DtzConstant.CARD_ERROR;
		//得到双方的牌型
		CardTypeDtz fromType = toPokerType(from, table);
		CardTypeDtz	ownType = toPokerType(own, table);
		if (fromType == CardTypeDtz.CARD_0) return DtzConstant.FROM_CARD_ERROR;
		if (ownType == CardTypeDtz.CARD_0) return DtzConstant.OWN_CARD_ERROR;
		//判断是否能出牌
		CardPair fCardPair = new CardPair(CardToolDtz.toValueList(from), fromType);
		CardPair tCardPair = new CardPair(CardToolDtz.toValueList(own), ownType);
		int code;
		switch (fromType){
			case CARD_1 : //单牌
				code = CardTypeFactory.factory.getCompare(fromType).compareTo(fCardPair, tCardPair);
				break;
			case CARD_2 : //对子
				code = CardTypeFactory.factory.getCompare(fromType).compareTo(fCardPair, tCardPair);
				break;
			case CARD_3 : //三张
				code = CardTypeFactory.factory.getCompare(fromType).compareTo(fCardPair, tCardPair);
				break;
			case CARD_4 : //三带一或者三带二
				code = CardTypeFactory.factory.getCompare(fromType).compareTo(fCardPair, tCardPair);
				break;
//			case CARD_5 : //单顺，暂时不用
//				code = CardTypeFactory.factory.getCompare(fromType).compareTo(fCardPair, tCardPair);
//				break;
			case CARD_6 : //双顺（连对）
				code = CardTypeFactory.factory.getCompare(fromType).compareTo(fCardPair, tCardPair);
				break;
			case CARD_7 : //三顺
				code = CardTypeFactory.factory.getCompare(fromType).compareTo(fCardPair, tCardPair,table.getFirstCardType().getValue1());
				break;
			case CARD_8 : //飞机带翅膀，需要传入上家牌的飞机连续数
				code = CardTypeFactory.factory.getCompare(fromType).compareTo(fCardPair, tCardPair,table.getFirstCardType().getValue1());
				break;
			case CARD_9 : //炸弹
				code = CardTypeFactory.factory.getCompare(fromType).compareTo(fCardPair, tCardPair);
				break;
			case CARD_10 : //筒子（注意三副牌和四副牌有差异，三副牌判断花色，四副牌不判断）
				fCardPair = new CardPair(from, fromType);
				tCardPair = new CardPair(own, ownType);
				code = CardTypeFactory.factory.getCompare(fromType).compareTo(fCardPair, tCardPair, table.getPlayType());
				break;
			case CARD_11 : //地炸
				code = CardTypeFactory.factory.getCompare(fromType).compareTo(fCardPair, tCardPair);
				break;
//			case CARD_12 : //大小王炸（大王+小王） ，暂时不用
//				fCardPair = new CardPair(from, fromType);
//				tCardPair = new CardPair(own, ownType);
//				code = CardTypeFactory.factory.getCompare(fromType).compareTo(fCardPair, tCardPair);
//				break;
			case CARD_13 : //囍
				fCardPair = new CardPair(from, fromType);
				tCardPair = new CardPair(own, ownType);
                code = CardTypeFactory.factory.getCompare(fromType).compareTo(fCardPair, tCardPair,table.getPlayType());
				break;
			default:
				code = DtzConstant.CARD_ERROR;
		}
		return code;
	}
	
	static class A {
		private int val = 1;
		
		public A() {}
		public A(int val) {this.val = val;}
		// equals(Object obj)
		public boolean equals(Object o) {
			System.out.println("被调用..");
			return ((A)o).val == val;
		}
	}
	
	/**
	 * 发出的牌的估值(得到普通分，筒子分)
	 */
	public static Pair<Integer, Integer> toScore(List<Integer>  pokers,DtzTable table) {
		int score = 0, tz_score = 0;
		for (Integer poker : pokers) {
			int val = CardToolDtz.toVal(poker);
			if (val == 5) { // 5, 10, k
				score += 5;
			} else if (val == 10 || val == 13) {
				score += 10;
			}
		}
		BombScore bombScore = table.getBombScore();
		switch(table.getPlayType()){
			case DtzzConstants.play_type_3POK:
			case DtzzConstants.play_type_3PERSON_3POK:
			case DtzzConstants.play_type_2PERSON_3POK:
				if (isTongZi(pokers)) { //看是不是筒子
					int value = CardToolDtz.toVal(pokers.get(0));
					if (value == 16 || value == 17){
						tz_score += bombScore.getTongZi_Wang();
					}else if (value == 15) {
						tz_score += bombScore.getTongZi_2();
					}else if (value == 14) {
						tz_score += bombScore.getTongZi_A();
					}else if (value == 13) {
						tz_score += bombScore.getTongZi_K();
					}
				}else if (isBobmDi(pokers)) { //给我看看是不是地炸
					tz_score += bombScore.getDiBomb();
				}
				break;
			case DtzzConstants.play_type_4POK:
			case DtzzConstants.play_type_3PERSON_4POK:
			case DtzzConstants.play_type_2PERSON_4POK:	
				if (isXi(pokers)) { //看是不是喜
					int value = CardToolDtz.toVal(pokers.get(0));
					if (value == 16 || value == 17) {
						tz_score += bombScore.getWang_Xi();
					}else {
						tz_score += bombScore.getXi();
					}
				}
				break;
            case DtzzConstants.play_type_2PERSON_4Xi:
            case DtzzConstants.play_type_3PERSON_4Xi:
            case DtzzConstants.play_type_4PERSON_4Xi:
                if (isTongZi(pokers)) { //看是不是筒子
                    int value = CardToolDtz.toVal(pokers.get(0));
                    if (value == 15) {
                        tz_score += bombScore.getTongZi_2();
                    }else if (value == 14) {
                        tz_score += bombScore.getTongZi_A();
                    }else if (value == 13) {
                        tz_score += bombScore.getTongZi_K();
                    }
                }
                if (isXi(pokers)) { //看是不是喜
                    int value = CardToolDtz.toVal(pokers.get(0));
                    if (value == 13 ) {
                        tz_score += bombScore.getXi_K();
                    }else if(value == 14){
                        tz_score += bombScore.getXi_A();
                    }else if(value == 15){
                        tz_score += bombScore.getXi_2();
                    }else{
                        tz_score += bombScore.getXi_5Q();
                    }
                }
                break;
        }
		return Pair.with(score, tz_score);
	}
	
	/**
	 * 得到具体筒子的数值
	 */
	public static int getTZType(List<Integer> pokers) {
		if (isTongZi(pokers)) {
			return CardToolDtz.toVal(pokers.get(0));
		}
		else {
			return 0;
		}
	}
	
	/**
	 * 得到花色
	 * @param pokers
	 * @return
	 */
	public static int getTZSuit(List<Integer> pokers) {
		if (isTongZi(pokers)) {
			return CardToolDtz.toSuit(pokers.get(0));
		}
		else {
			return 0;
		}
	}
	
	/**
	 * 获得炸弹牌其中一张
	 * @param pokers
	 * @return
	 */
	public static int getCardOfBomb(List<Integer> pokers) {
		if (isBomb(CardToolDtz.toValueList(pokers))) {
			return CardToolDtz.toVal(pokers.get(0));
		}
		else {
			return 0;
		}
	}
	
	/**
	 * 判断筒子的类型
	 * 3 4 5 6 7 8 9 10 j q k a 2 <br />
	 * 
	 * @param pokers  
	 * @return
	 */
	public static boolean  tongZi_(List<Integer> pokers, int cardVal) {
		int card = getTZType(pokers);
		if (cardVal < 3 || cardVal > 17) return false;
		if (card != 0) {
			return card == cardVal;
		}
		else {
			return false;
		}
	}
	
	
	/**
	 * 只是得到三带 中的 三张
	 * @param poker
	 * @return
	 */
	public static List<Integer> getThree(List<Integer> poker) {
		if (!isThreeWithone(CardToolDtz.toValueList(poker))) return Collections.emptyList();
		List<Integer> vals = CardToolDtz.toValueList(poker);
		int  mVal = 0;
		for (int val : vals) {
			if (Collections.frequency(vals, val) >= 3) {
				mVal = val;
			}
		}
		ArrayList<Card> cardss = new ArrayList<>(CardToolDtz.toCardList(poker));
		ArrayList<Integer> temp = new ArrayList<>();
		for (Card cardd : cardss) {
			if (cardd.getValue() == mVal) {
				temp.add(cardd.getValue());
			}
		}
		for (Card cardd : cardss) {
			if (cardd.getValue() != mVal) {
				temp.add(cardd.getValue());
			}
		}
		return temp;
	}
	
	/**
	 * 只是得到飞机带翅膀中的 三顺
	 * @param poker
	 * @return
	 */
	private static List<Integer> getPlan(List<Integer> poker, DtzTable table) {
		if (!isPlan(CardToolDtz.toValueList(poker), table)) return Collections.emptyList();
		ArrayList<Entry<Integer, Integer>>  cardl = new ArrayList<>();
    	for (int val : poker) {
    		if (!constainKey(val, cardl)) { //看他是不是包含
    			Entry<Integer, Integer> entry = new AbstractMap.SimpleEntry<Integer, Integer>(val, 1);
    			cardl.add(entry); //加进去
    		}
    		else {
    			Entry<Integer, Integer> entry = getValue(val, cardl);
    			if (entry != null) {
    				if (entry.getValue() >= 3) {
            			cardl.add(new AbstractMap.SimpleEntry<Integer, Integer>(val, 1)); //加进去
        			}
        			else {
        				entry.setValue(entry.getValue() + 1);
        			}
    			}
    			else {
    				cardl.add(new AbstractMap.SimpleEntry<Integer, Integer>(val, 1)); //加进去
    			}
    		}
    	}
    	//
    	ArrayList<Integer> keys = toKeys(cardl), cardTemp = new ArrayList<>();
    	for (int i = 0; i < keys.size(); i ++) {
    		int firstVal = keys.get(i);
    		int j = i + 1;
    		for (;j < keys.size(); j ++) {
    			if (firstVal + 1 == keys.get(j)) {
    				if (!cardTemp.contains(firstVal)) {
        				cardTemp.add(firstVal);
    				}
    				cardTemp.add(firstVal + 1);
    				firstVal = keys.get(j);
    			}
    			else firstVal = keys.get(j);
    		}
    		if (cardTemp.size() >= 2) {
    			break;
    		}
    		else {
    			cardTemp.clear();
    		}
    	}
    	Collections.reverse(cardTemp);
    	ArrayList<Integer> source = new ArrayList<>();
		for (int val : cardTemp) {
			source.add(val);
			source.add(val);
			source.add(val);
		}
		source.addAll(CardToolDtz.removeAll(source, poker));
    	return source;
	}
	
	/**
	 * 对牌进行排序返回给客户端
	 * @return
	 */
	public static List<Integer> sortPokers(List<Integer> poker, DtzTable table) {
		//先将数据按牌数字进行排序
		Collections.sort(poker, CardTypeToolDtz.comparator);
		//得到当前牌型
		CardTypeDtz type = toPokerType(poker, table);
		if(type == CardTypeDtz.CARD_4){
			//三带一或者三带二的处理
			//得到牌对象的列表
			List<Card> cards = CardToolDtz.toCardList(poker);
			HashMap<Integer, Integer> map = new HashMap<>();
	    	for(Card pk:cards){
	    		if (!map.containsKey(pk.getValue())){
	    			map.put(pk.getValue(), 1);
	    		}else{
	    			map.put(pk.getValue(), map.get(pk.getValue())+1);
	    		}
	    	}
	    	int value = 0;
	    	for (int key : map.keySet()){
	    		if (map.get(key) >= 3)value = key;
	    	}
	    	if(value == 0)return null;
	    	List<Integer> ret = new ArrayList<Integer>();
	    	//先将三张加入列表
	    	for(int p:poker){
	    		if(CardToolDtz.toVal(p) == value){
	    			ret.add(p);
	    		}
	    	}
	    	//再将带的部分加入列表
	    	List<Integer> rd = new ArrayList<Integer>();
	    	for(int p:poker){
	    		if(CardToolDtz.toVal(p) != value){
	    			rd.add(p);
	    		}
	    	}
	    	ret.addAll(rd);
			return ret;
		}else if(type == CardTypeDtz.CARD_7){
			//三顺的排序
			//得到三顺的连续数
			if(poker.size()%3!=0)return null;
			int minValue = 100;
			int maxValue = 0;
			for(int p:poker){
				if((table.getPlayType() == DtzzConstants.play_type_3POK 
						|| table.getPlayType() == DtzzConstants.play_type_3PERSON_3POK
						|| table.getPlayType() == DtzzConstants.play_type_2PERSON_3POK) && table.getWangTongZi()==0){
					//三副牌不能带2
					if(CardToolDtz.toVal(p) > maxValue && CardToolDtz.toVal(p) < 15){
						maxValue = CardToolDtz.toVal(p);
					}
				}else{
					if(CardToolDtz.toVal(p) > maxValue){
						maxValue = CardToolDtz.toVal(p);
					}
				}
				if(CardToolDtz.toVal(p) < minValue){
					minValue = CardToolDtz.toVal(p);
				}
			}
			if(table.getFirstCardType().getValue0() != CardTypeDtz.CARD_0 && (maxValue-minValue+1) >= table.getFirstCardType().getValue1()){
				int minP = (maxValue-table.getFirstCardType().getValue1()+1);
				List<Integer> qian = new ArrayList<Integer>();
				List<Integer> hou = new ArrayList<Integer>();
				for(int v=minValue;v<=maxValue;v++){
					for(int p:poker){
						if(CardToolDtz.toVal(p) == v){
							if(v >= minP)qian.add(p);
							else hou.add(p);
						}
					}
				}
				qian.addAll(hou);
				return qian;
			}
			return poker;
		}else if(type == CardTypeDtz.CARD_8){
			//三副牌才会有飞机
			List<Integer> partPoker = CardToolDtz.toValueList(poker);			
			//得到上家牌的对应连续数和最大牌
			int fromCount = 0;
			int fromMaxCard = 0;
			
			if(table.getFirstCardType().getValue0() != CardTypeDtz.CARD_0)fromCount = table.getFirstCardType().getValue1();
			
	        //先得到所有出现次数为3以上的牌
	        HashMap<Integer, Integer> map = new HashMap<>();
	        for(int card:partPoker){
	    		if (!map.containsKey(card)) map.put(card,1);
	    		else map.put(card, map.get(card)+1);
	    	}
	        List<Integer> vList = new ArrayList<Integer>();
	        for (Entry<Integer, Integer> entry : map.entrySet()) {
	        	//注意，三副牌2不能作为飞机，三副牌才会有飞机
	    		if (entry.getValue() >= 3) {
	    			if((table.getPlayType() == DtzzConstants.play_type_3POK 
	    					|| table.getPlayType() == DtzzConstants.play_type_3PERSON_3POK
	    					|| table.getPlayType() == DtzzConstants.play_type_2PERSON_3POK) && table.getWangTongZi()==0){
	    				if(entry.getKey() < 15)vList.add(entry.getKey());
	    			}else{
	    				vList.add(entry.getKey());
	    			}
	    			
	    		}
	    	}
	        //排序后判断得到最大的连续飞机
	        Collections.sort(vList, CardTypeToolDtz.comparator);
	        //得到连续最大牌、连续次数列表
	        Map<Integer,Integer> countList = new HashMap<Integer,Integer>();
	        int stV = vList.get(0);
	        int count = 1;
	        for(int i=1;i<vList.size();i++){
	        	if(vList.get(i) == (stV+1)){
	        		count++;
	        		if(i==vList.size()-1){
	        			//如果是最后一个满足条件，需要加入列表
	        			countList.put(vList.get(i), count);
	        		}
					stV = vList.get(i);
	        	}else{
	        		countList.put(vList.get(i-1), count);
	        		count = 1;
	        		stV = vList.get(i);
	        	}
	        }
	        //找到对应连续数的最大牌
	        for(Entry<Integer, Integer> entry : countList.entrySet()){
	        	if(fromCount != 0){
	        		//不是第一个出牌
		        	if(entry.getValue() >= fromCount && entry.getKey() > fromMaxCard){
		        		fromMaxCard = entry.getKey();
		        	}
	        	}else{
	        		//该轮首次出牌者
	        		if(entry.getValue() > fromCount || (entry.getValue()  == fromCount && entry.getKey() > fromMaxCard)){
	        			fromCount = entry.getValue();
		        		fromMaxCard = entry.getKey();
		        	}
	        	}
	        }
	        
	        List<Integer> pokers = new ArrayList<Integer>(poker);
	        List<Integer> qian = new ArrayList<Integer>();
			for(int v=fromMaxCard-fromCount+1;v<=fromMaxCard;v++){
				//只加3次
				int c = 0;
				for(int p=0;p<pokers.size();p++){
					if(CardToolDtz.toVal(pokers.get(p)) == v){
						qian.add(pokers.get(p));
						pokers.remove(p);
						p--;
						c++;
						if(c==3)break;
					}
				}
			}
			qian.addAll(pokers);
			return qian;
		}
		return poker;
	}
	

	/**
	 * 得到三顺的最大牌
	 */
	public static int getThreeStraightMaxCard(CardPair cardPair) {
		int maxcard = 0;
		for(int val:cardPair.getPokers()){
			if(maxcard<val)maxcard=val;
		}
		return maxcard;
	}
	
	
	/**
	 * 检查出牌是否在手牌范围
	 * @return true 通过  false 不通过
	 */
	public static boolean cheakOutcard(List<Integer> handsCards,List<Integer> outCards){
		//得到手牌各种牌的数量
		HashMap<Integer, Integer> handCountMap = new HashMap<>();
		for(int card:handsCards){
			if (!handCountMap.containsKey(card)) handCountMap.put(card,1);
			else handCountMap.put(card, handCountMap.get(card)+1);
		}
		//得到出牌各种牌的数量
		HashMap<Integer, Integer> outCountMap = new HashMap<>();
		  for(int card:outCards){
			if (!outCountMap.containsKey(card)) outCountMap.put(card,1);
			else outCountMap.put(card, outCountMap.get(card)+1);
		}
		//比较两副牌的吻合度
		for(Entry<Integer, Integer> v:outCountMap.entrySet()){
			if(!handCountMap.containsKey(v.getKey()) || handCountMap.get(v.getKey()) < v.getValue())return false;
		}
		return true;
	}
	/**
	 * 统计牌
	 * @param cards
	 * @return
	 */
	public static DtzModel getBestModel(List<Integer> cards){
		DtzModel model = new DtzModel();
		if(cards != null && !cards.isEmpty()){
			model.setCards(new ArrayList<Integer>(cards));
			for(Integer card : cards){
				int val = CardToolDtz.toVal(card);
				if(model.getPai().containsKey(val)){
					model.getPai().put(val, model.getPai().get(val) + 1);
				}else{
					model.getPai().put(val, 1);
				}
				if(model.getPaiCode().containsKey(card)){
					model.getPaiCode().put(card, model.getPaiCode().get(card) + 1);
				}else{
					model.getPaiCode().put(card, 1);
				}
			}
		}
		return model;
	}
	/**
	 * 
	 * @param handPais 手上的牌
	 * @param outPais 对手出的牌
	 * @return 0要不起 1要的起
	 */
	public static int isCanPlay(List<Integer> handPais, List<Integer> outPais, DtzTable table){
		DtzModel paiModel = getBestModel(handPais);
		CardTypeDtz outType = toPokerType(outPais, table);
		CardPair cardPair = new CardPair(outPais, outType);
		int code = 1;
		CanPlayCompare compare = CanPlayCompareFactory.factory.getCompare(cardPair.getType());
		if(compare != null){
			code = compare.compareTo(paiModel, cardPair, table);
		}
		return code;
	}
	/**
	 * 统计手牌特殊牌型数
	 * @param cards
	 * @return
	 */
	public static int statisticsCardTypeNum(List<Integer> cards, DtzTable table){
		int num = 0;
		DtzModel model = getBestModel(cards);
		int playType = table.getPlayType();
		if(playType == DtzzConstants.play_type_4POK || playType == DtzzConstants.play_type_3PERSON_4POK || playType == DtzzConstants.play_type_2PERSON_4POK){
			for(Entry<Integer, Integer> entry : model.getPaiCode().entrySet()){
				if(entry.getValue() == 4){
					int val = CardToolDtz.toVal(entry.getKey());
					if(val == 16 || val == 17){
						num++;
					}
				}
			}
		}else{
			for(Entry<Integer, Integer> entry : model.getPaiCode().entrySet()){
				if(entry.getValue() == 3){
					int val = CardToolDtz.toVal(entry.getKey());
					if(val==15){
						num++;
					}
				}
			}
			for(Entry<Integer, Integer> entry : model.getPai().entrySet()){
				if(entry.getValue() >= 8){
					Map<Integer, Integer> paiCode = model.getPaiCode();
					if(paiCode.containsKey(entry.getKey()+100) && paiCode.get(entry.getKey()+100) >= 2 
							&& paiCode.containsKey(entry.getKey()+200) && paiCode.get(entry.getKey()+200) >= 2 
							&& paiCode.containsKey(entry.getKey()+300) && paiCode.get(entry.getKey()+300) >= 2 
							&& paiCode.containsKey(entry.getKey()+400) && paiCode.get(entry.getKey()+400) >= 2){
						num++;
					}
				}
			}
		}
		return num;
	}
	/**
	 * 计算解散后未结束这局筒子喜分
	 * @param table
	 * @param player
	 * @return
	 */
//	public static int calcTongziScore(DtzTable table, DtzPlayer player){
//		int score = 0;
//		List<Integer> cards = new ArrayList<>();
//		cards.addAll(player.getHandPais());
//		if(!player.getOutPais().isEmpty()){
//			for(List<Integer> list : player.getOutPais()){
//				if(list.size()==1 && list.get(0)==0){
//					continue;
//				}
//				cards.addAll(list);
//			}
//		}
//		DtzModel model = getBestModel(cards);
//		int playType = table.getPlayType();
//		BombScore bombScore = table.getBombScore();
//		if(playType == DtzzConstants.play_type_4POK || playType == DtzzConstants.play_type_3PERSON_4POK || playType == DtzzConstants.play_type_2PERSON_4POK){
//			for(Entry<Integer, Integer> entry : model.getPaiCode().entrySet()){
//				if(entry.getValue() == 4){
//					int val = CardToolDtz.toVal(entry.getKey());
//					switch(val){
//						case 16:
//							score+=bombScore.getWang_Xi();
//							table.tempAddToScoreList(player, ScoreType.POINT_JACKER_S);
//							break;
//						case 17:	
//							score+=bombScore.getWang_Xi();
//							table.tempAddToScoreList(player, ScoreType.POINT_JACKER_B);
//							break;
//						default:
//							score+=bombScore.getXi();
//							table.tempAddToScoreList(player, ScoreType.POINT_XI);
//							break;
//					}
//				}
//			}
//		}else{
//			List<Integer> diBombPai = new ArrayList<>();//记录K A 2地炸
//			for(Entry<Integer, Integer> entry : model.getPai().entrySet()){
//				if(entry.getValue() >= 8){
//					Map<Integer, Integer> paiCode = model.getPaiCode();
//					if(paiCode.containsKey(entry.getKey()+100) && paiCode.get(entry.getKey()+100) >= 2 
//							&& paiCode.containsKey(entry.getKey()+200) && paiCode.get(entry.getKey()+200) >= 2 
//							&& paiCode.containsKey(entry.getKey()+300) && paiCode.get(entry.getKey()+300) >= 2 
//							&& paiCode.containsKey(entry.getKey()+400) && paiCode.get(entry.getKey()+400) >= 2){
//						if(entry.getKey() == 13 || entry.getKey() == 14 || entry.getKey() == 15){
//							diBombPai.add(entry.getKey());
//						}
//						score+=bombScore.getDiBomb();
//						table.tempAddToScoreList(player, ScoreType.POINT_BD);
//					}
//				}
//			}
//			for(Entry<Integer, Integer> entry : model.getPaiCode().entrySet()){
//				if(entry.getValue() == 3){
//					int val = CardToolDtz.toVal(entry.getKey());
//					if(diBombPai.contains(val)){
//						continue;
//					}
//					switch(val){
//						case 13:
//							score+=bombScore.getTongZi_K();
//							table.tempAddToScoreList(player, ScoreType.POINT_TZ_K);
//							break;
//						case 14:
//							table.tempAddToScoreList(player, ScoreType.POINT_TZ_A);
//							score+=bombScore.getTongZi_A();
//							break;
//						case 15:
//							table.tempAddToScoreList(player, ScoreType.POINT_TZ_2);
//							score+=bombScore.getTongZi_2();
//							break;
//						case 16:
//							table.tempAddToScoreList(player, ScoreType.POINT_TZ_S);
//							score+=bombScore.getTongZi_Wang();
//							break;
//						case 17:	
//							table.tempAddToScoreList(player, ScoreType.POINT_TZ_B);
//							score+=bombScore.getTongZi_Wang();
//							break;	
//					}
//				}
//			}
//		}
//		return score;
//	}
	public static int calcTongziScore(DtzTable table, DtzPlayer player){
		int score = 0;
		List<Integer> cards = new ArrayList<>();
		cards.addAll(player.getHandPais());
		DtzModel model = getBestModel(cards);
		BombScore bombScore = table.getBombScore();
		CardTypeDtz cardType = CardTypeToolDtz.toPokerType(table.getNowDisCardIds(), table);
		if(table.isFourPai() && !table.isKlSX()){
			for(Entry<Integer, Integer> entry : model.getPaiCode().entrySet()){
				if(entry.getValue() == 4){
					int val = CardToolDtz.toVal(entry.getKey());
					switch(val){
						case 16:
							score+=bombScore.getWang_Xi();
							table.addToScoreList(player, ScoreType.POINT_JACKER_S);
							break;
						case 17:	
							score+=bombScore.getWang_Xi();
							table.addToScoreList(player, ScoreType.POINT_JACKER_B);
							break;
						default:
							score+=bombScore.getXi();
							table.addToScoreList(player, ScoreType.POINT_XI);
							break;
					}
				}
			}
			if(cardType != null && table.getDisCardSeat() == player.getSeat() && cardType == CardTypeDtz.CARD_13){
				int val = CardToolDtz.toVal(table.getNowDisCardIds().get(0));
				switch(val){
					case 16:
						score+=bombScore.getWang_Xi();
						table.addToScoreList(player, ScoreType.POINT_JACKER_S);
						break;
					case 17:	
						score+=bombScore.getWang_Xi();
						table.addToScoreList(player, ScoreType.POINT_JACKER_B);
						break;
					default:
						score+=bombScore.getXi();
						table.addToScoreList(player, ScoreType.POINT_XI);
						break;
				}
			}
		}else{
			List<Integer> diBombPai = new ArrayList<>();//记录K A 2地炸
            if(!table.isKlSX()) {
                for (Entry<Integer, Integer> entry : model.getPai().entrySet()) {
                    if (entry.getValue() >= 8) {
                        Map<Integer, Integer> paiCode = model.getPaiCode();
                        if (paiCode.containsKey(entry.getKey() + 100) && paiCode.get(entry.getKey() + 100) >= 2
                                && paiCode.containsKey(entry.getKey() + 200) && paiCode.get(entry.getKey() + 200) >= 2
                                && paiCode.containsKey(entry.getKey() + 300) && paiCode.get(entry.getKey() + 300) >= 2
                                && paiCode.containsKey(entry.getKey() + 400) && paiCode.get(entry.getKey() + 400) >= 2) {
                            if (entry.getKey() == 13 || entry.getKey() == 14 || entry.getKey() == 15) {
                                diBombPai.add(entry.getKey());
                            }
                            score += bombScore.getDiBomb();
                            table.addToScoreList(player, ScoreType.POINT_BD);
                        }
                    }
                }
            }
			for(Entry<Integer, Integer> entry : model.getPaiCode().entrySet()){
				if(entry.getValue() == 3){
					int val = CardToolDtz.toVal(entry.getKey());
					if(diBombPai.contains(val)){
						continue;
					}
					switch(val){
						case 13:
							score+=bombScore.getTongZi_K();
							table.addToScoreList(player, ScoreType.POINT_TZ_K);
							break;
						case 14:
							table.addToScoreList(player, ScoreType.POINT_TZ_A);
							score+=bombScore.getTongZi_A();
							break;
						case 15:
							table.addToScoreList(player, ScoreType.POINT_TZ_2);
							score+=bombScore.getTongZi_2();
							break;
						case 16:
							table.addToScoreList(player, ScoreType.POINT_TZ_S);
							score+=bombScore.getTongZi_Wang();
							break;
						case 17:	
							table.addToScoreList(player, ScoreType.POINT_TZ_B);
							score+=bombScore.getTongZi_Wang();
							break;
					}
				}
                if(entry.getValue() == 4){
                    int val = CardToolDtz.toVal(entry.getKey());
                    switch(val){
                        case 16:
                            score+=bombScore.getWang_Xi();
                            table.addToScoreList(player, ScoreType.POINT_JACKER_S);
                            break;
                        case 17:
                            score+=bombScore.getWang_Xi();
                            table.addToScoreList(player, ScoreType.POINT_JACKER_B);
                            break;
                        case 13:
                            score+=bombScore.getXi_K();
                            table.addToScoreList(player, ScoreType.POINT_Xi_K);
                            break;
                        case 14:
                            score+=bombScore.getXi_A();
                            table.addToScoreList(player, ScoreType.POINT_Xi_A);
                            break;
                        case 15:
                            score+=bombScore.getXi_2();
                            table.addToScoreList(player, ScoreType.POINT_Xi_2);
                            break;
                        default:
                            score+=bombScore.getXi_5Q();
                            table.addToScoreList(player, ScoreType.POINT_Xi_5Q);
                            break;
                    }
                }
			}
			if(cardType != null && table.getDisCardSeat() == player.getSeat()){
				if(cardType == CardTypeDtz.CARD_11){
					score+=bombScore.getDiBomb();
					table.addToScoreList(player, ScoreType.POINT_BD);
				}else if(cardType == CardTypeDtz.CARD_10){
					int val = CardToolDtz.toVal(table.getNowDisCardIds().get(0));
					switch(val){
						case 13:
							score+=bombScore.getTongZi_K();
							table.addToScoreList(player, ScoreType.POINT_TZ_K);
							break;
						case 14:
							table.addToScoreList(player, ScoreType.POINT_TZ_A);
							score+=bombScore.getTongZi_A();
							break;
						case 15:
							table.addToScoreList(player, ScoreType.POINT_TZ_2);
							score+=bombScore.getTongZi_2();
							break;
						case 16:
							table.addToScoreList(player, ScoreType.POINT_TZ_S);
							score+=bombScore.getTongZi_Wang();
							break;
						case 17:	
							table.addToScoreList(player, ScoreType.POINT_TZ_B);
							score+=bombScore.getTongZi_Wang();
							break;	
					}
				}else if(cardType == CardTypeDtz.CARD_13){
                    int val = CardToolDtz.toVal(table.getNowDisCardIds().get(0));
                    switch(val){
                        case 16:
                            score+=bombScore.getWang_Xi();
                            table.addToScoreList(player, ScoreType.POINT_JACKER_S);
                            break;
                        case 17:
                            score+=bombScore.getWang_Xi();
                            table.addToScoreList(player, ScoreType.POINT_JACKER_B);
                            break;
                        case 13:
                            score+=bombScore.getXi_K();
                            table.addToScoreList(player, ScoreType.POINT_Xi_K);
                            break;
                        case 14:
                            score+=bombScore.getXi_A();
                            table.addToScoreList(player, ScoreType.POINT_Xi_A);
                            break;
                        case 15:
                            score+=bombScore.getXi_2();
                            table.addToScoreList(player, ScoreType.POINT_Xi_2);
                            break;
                        default:
                            score+=bombScore.getXi_5Q();
                            table.addToScoreList(player, ScoreType.POINT_Xi_5Q);
                            break;
                    }
                }
			}
		}
		return score;
	}
	/**
	 * 托管手牌整理
	 * @param cards
	 * @param table
	 * @return
	 */
	public static AutoHandPais getAutoHandPais(List<Integer> cards, DtzTable table){
		AutoHandPais autoHandPais = new AutoHandPais();
		if(cards != null && !cards.isEmpty()){
			List<Integer> temp = new ArrayList<>(cards);
			Map<Integer, List<Integer>> pai = new HashMap<>();
			for(Integer card : temp){
				int val = CardToolDtz.toVal(card);
				if(pai.containsKey(val)){
					pai.get(val).add(card);
				}else{
					List<Integer> list = new ArrayList<>();
					list.add(card);
					pai.put(val, list);
				}
			}
			for(Entry<Integer, List<Integer>> entry : pai.entrySet()){ 
				List<Integer> list = entry.getValue();
				switch(list.size()){
					case 1:
						autoHandPais.getSingle().put(entry.getKey(), entry.getValue().get(0));
						break;
					case 2:
						autoHandPais.getPair().put(entry.getKey(), entry.getValue());
						break;
					case 3:
						if(list.get(0).intValue() == list.get(1).intValue() && list.get(0).intValue() == list.get(2).intValue()){
							autoHandPais.getTongzi().put(list.get(0), entry.getValue());
						}else{
							autoHandPais.getThree().put(entry.getKey(), entry.getValue());
						}
						break;
					case 4:
						if(list.get(0).intValue() == list.get(1).intValue() && list.get(0).intValue() == list.get(2).intValue() && list.get(0).intValue() == list.get(3).intValue()){
							autoHandPais.getXi().put(list.get(0), entry.getValue());
						}else{
							autoHandPais(entry.getValue(), autoHandPais, table);
						}
						break;
					default:
						autoHandPais(entry.getValue(), autoHandPais, table);
						break;
				}
			}
			//autoHandPais.order();不保存了,每次用到排序
		}
		return autoHandPais;
	}
	public static void autoHandPais(List<Integer> cards, AutoHandPais autoHandPais, DtzTable table){
		Map<Integer, Integer> map = new HashMap<>();//ID,数量
		for(Integer card : cards){
			if(map.containsKey(card)){
				map.put(card, map.get(card)+1);
			}else{
				map.put(card, 1);
			}
		}
		int val = CardToolDtz.toVal(cards.get(0));
		if(cards.size() >= 8 && table.isThreePai()){//地炸
			if(map.containsKey(val+100) && map.get(val+100) >= 2 
					&& map.containsKey(val+200) && map.get(val+200) >= 2 
					&& map.containsKey(val+300) && map.get(val+300) >= 2 
					&& map.containsKey(val+400) && map.get(val+400) >= 2){
				autoHandPais.getBombdi().put(val, Arrays.asList(val+100,val+100,val+200,val+200,val+300,val+300,val+400,val+400));
				if(cards.size() > 8){
					List<Integer> temp = new ArrayList<>();
					for(Entry<Integer, Integer> entry : map.entrySet()){
						if(entry.getValue()>2){
							temp.add(entry.getKey());//三副牌 地炸后同花色只可能多一张
						}
					}
					switch(temp.size()){
						case 1:
							autoHandPais.getSingle().put(val, temp.get(0));
							break;
						case 2:
							autoHandPais.getPair().put(val, temp);
							break;	
						case 3:
							autoHandPais.getThree().put(val, temp);
							break;	
					}
				}
				return;
			}
		}
		List<Integer> temp = new ArrayList<>();
		for(Entry<Integer, Integer> entry : map.entrySet()){
			switch(entry.getValue()){
				case 1:
					temp.add(entry.getKey());
					break;
				case 2:
					temp.add(entry.getKey());
					temp.add(entry.getKey());
					break;	
				case 3:
					autoHandPais.getTongzi().put(entry.getKey(), Arrays.asList(entry.getKey(),entry.getKey(),entry.getKey()));//筒子喜的key为牌ID
					break;
				case 4:
					autoHandPais.getXi().put(entry.getKey(), Arrays.asList(entry.getKey(),entry.getKey(),entry.getKey(),entry.getKey()));//筒子喜的key为牌ID
					break;
			}
		}
		if(!temp.isEmpty()){
			switch(temp.size()){
				case 1:
					autoHandPais.getSingle().put(val, temp.get(0));
					break;
				case 2:
					autoHandPais.getPair().put(val, temp);
					break;	
				case 3:
					autoHandPais.getThree().put(val, temp);
					break;	
				default:
					autoHandPais.getBomb().put(val, temp);
					break;
			}
		}
	}
	public static CardPair autoPlay(List<Integer> handPais, List<Integer> outPais, DtzTable table){
		AutoHandPais autoHandPais = getAutoHandPais(handPais, table);
		AutoPlay autoPlay = null;
		if(outPais==null || outPais.isEmpty()){
			autoPlay = AutoPlayFactory.factory.getFirstAutoPlay();
		}else{
			CardTypeDtz outType = toPokerType(outPais, table);
			autoPlay = AutoPlayFactory.factory.getAutoPlay(outType);
		}
		CardPair cardPair = null;
		if(autoPlay != null){
			cardPair = autoPlay.autoPlay(autoHandPais, outPais, table);
		}
		return cardPair;
	}
	
	public static boolean isRedeal(DtzTable table, List<List<Integer>> list){
		if(list == null || list.isEmpty()){
			return false;
		}
		int[] count = new int[list.size()];
		for(int i=0,len=list.size();i<len;i++){
			DtzModel model = getBestModel(list.get(i));
			if(table.isFourPai()){
				for(Entry<Integer, Integer> entry : model.getPaiCode().entrySet()){
					if(entry.getValue() == 4){
						count[i] += 1;
					}
				}
			}else{
				for(Entry<Integer, Integer> entry : model.getPai().entrySet()){
					if(entry.getValue() >= 8){
						Map<Integer, Integer> paiCode = model.getPaiCode();
						if(paiCode.containsKey(entry.getKey()+100) && paiCode.get(entry.getKey()+100) >= 2 
								&& paiCode.containsKey(entry.getKey()+200) && paiCode.get(entry.getKey()+200) >= 2 
								&& paiCode.containsKey(entry.getKey()+300) && paiCode.get(entry.getKey()+300) >= 2 
								&& paiCode.containsKey(entry.getKey()+400) && paiCode.get(entry.getKey()+400) >= 2){
							count[i] += 1;
						}
					}
				}
				for(Entry<Integer, Integer> entry : model.getPaiCode().entrySet()){
					if(entry.getValue() == 3){
						int val = CardToolDtz.toVal(entry.getKey());
						switch(val){
							case 13:
							case 14:
							case 15:
							case 16:
							case 17:	
								count[i] += 1;
								break;	
						}
					}
				}
			}
		}
		int max=0,min=0;
		for(int val : count){
			if(val>=max){
				max = val;
			}
			if(val<=min){
				min = val;
			}
		}
		if(max-min>=2){
			return true;
		}
		return false;
	}
	public static void main(String args[]) {
		long t = System.currentTimeMillis();
		int i = 0;
		DtzTable table = new DtzTable();
		table.setPlayType(115);
		int fapai = 10000;
		for(int j=0;j<fapai;j++){
			List<List<Integer>> list = CardTool.fapaiDtz(table, 3, 115, 0, null);
			if(isRedeal(table, list)){
//				List<List<Integer>> list1 = CardTool.fapaiDtz(table, 3, 115, 0, null);
//				if(isRedeal(table, list1)){
//					i++;
//				}
				i++;
			}
		}
		System.out.println("发牌:"+fapai+"  出现次数"+i+" 耗时"+(System.currentTimeMillis()-t));
	}
}
