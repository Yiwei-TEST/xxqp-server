package com.sy599.game.qipai.dtz.compare;

import com.sy599.game.qipai.dtz.bean.CardPair;
import com.sy599.game.qipai.dtz.constant.DtzConstant;
import com.sy599.game.qipai.dtz.constant.DtzzConstants;
import com.sy599.game.qipai.dtz.rule.CardTypeDtz;
import com.sy599.game.qipai.dtz.tool.CardToolDtz;
import com.sy599.game.qipai.dtz.tool.CardTypeToolDtz;

import java.util.*;

/**
 * 牌大小比较器
 * @author zhouhj
 *
 */
public class CardTypeFactory {
	
	public static final CardTypeFactory factory = new CardTypeFactory();
	/**
	 * 上家单牌的大小比较
	 */
	private IPokerCompare single = new IPokerCompare() {
		@Override
		public int compareTo(CardPair from, CardPair own, int... param) {
			int from_card = from.getPokers().get(0), own_card = own.getPokers().get(0);
			//如果我出的牌的牌型是个炸，筒子，地炸，囍
			CardTypeDtz ownType = own.getType();
			if(ownType == CardTypeDtz.CARD_9 || 
				ownType == CardTypeDtz.CARD_10 ||
				ownType == CardTypeDtz.CARD_11 ||
				ownType == CardTypeDtz.CARD_13) return DtzConstant.OWN_WIN;
			if(from.getType() != ownType)return DtzConstant.CARD_ERROR;
			if(from_card < own_card)return DtzConstant.OWN_WIN;
			if(from_card > own_card)return DtzConstant.FROM_WIN;
			return DtzConstant.CARD_ERROR;
		}
	};
	
	/**
	 * 上家对子的大小比较
	 */
	private IPokerCompare pair = new IPokerCompare() {
		@Override
		public int compareTo(CardPair from, CardPair own, int... param) {
			int from_card = from.getPokers().get(0), own_card = own.getPokers().get(0);
			//如果我出的牌的牌型是个炸，筒子，地炸，囍
			CardTypeDtz ownType = own.getType();
			if (ownType == CardTypeDtz.CARD_9 || 
				ownType == CardTypeDtz.CARD_10 ||
				ownType == CardTypeDtz.CARD_11 ||
				ownType == CardTypeDtz.CARD_13)return DtzConstant.OWN_WIN;
			if(from.getType() != ownType)return DtzConstant.CARD_ERROR;
			if(from_card < own_card)return DtzConstant.OWN_WIN;
			if(from_card > own_card)return DtzConstant.FROM_WIN;
			return DtzConstant.CARD_ERROR;
		}
	};
	
	/**
	 * 上家三张的大小比较
	 */
	private IPokerCompare three = new IPokerCompare() {
		@Override
		public int compareTo(CardPair from, CardPair own, int... param) {
			//如果我出的牌的牌型是个炸，筒子，地炸，囍
			CardTypeDtz ownType = own.getType();
			if (ownType == CardTypeDtz.CARD_9 
				|| ownType == CardTypeDtz.CARD_10
				|| ownType == CardTypeDtz.CARD_11
				|| ownType == CardTypeDtz.CARD_13)return DtzConstant.OWN_WIN;
			if (own.getType() == CardTypeDtz.CARD_3 || own.getType() == CardTypeDtz.CARD_4){
				int fromCard = from.getPokers().get(0), ownCard = getThreeCardOfOne(own);
				if(fromCard < ownCard)return DtzConstant.OWN_WIN;
				if(fromCard > ownCard)return DtzConstant.FROM_WIN;
			}
			return DtzConstant.CARD_ERROR;
		}
		
	};
	
	/**
	 * 上家炸弹的大小比较
	 */
	private IPokerCompare bomb = new IPokerCompare() {
		@Override
		public int compareTo(CardPair from, CardPair own, int... param) {
			int from_card = from.getPokers().get(0), own_card = own.getPokers().get(0);
			//如果我出的牌的牌型是个筒子，地炸，囍
			CardTypeDtz ownType = own.getType();
			if(ownType == CardTypeDtz.CARD_10 ||
				ownType == CardTypeDtz.CARD_11 ||
				ownType == CardTypeDtz.CARD_13)return DtzConstant.OWN_WIN;
			if(from.getType() != ownType)return DtzConstant.CARD_ERROR;
			//先判断一下炸弹的牌数
			if(from.getPokers().size() < own.getPokers().size())return DtzConstant.OWN_WIN;
			if(from.getPokers().size() > own.getPokers().size())return DtzConstant.FROM_WIN;
			//在牌数一样的情况下 比较牌的大小
			if(from_card < own_card)return DtzConstant.OWN_WIN;
			if(from_card > own_card)return DtzConstant.FROM_WIN;
			return DtzConstant.CARD_ERROR;
		}
	};
	
	/**
	 * 上家筒子的大小比较（三副牌筒子还要比较花色）
	 * @param param[0] 三副牌或者四副牌
	 */
	private IPokerCompare tongzi = new IPokerCompare() {
		@Override
		public int compareTo(CardPair from, CardPair own, int... param) {
			int from_card = from.getPokers().get(0), own_card = own.getPokers().get(0);
			CardTypeDtz ownType = own.getType();
			if(ownType == CardTypeDtz.CARD_11 ||
				ownType == CardTypeDtz.CARD_13)return DtzConstant.OWN_WIN;
			if(from.getType() != ownType)return DtzConstant.CARD_ERROR;
			//先比较筒子的大小
			if(CardToolDtz.toVal(from_card) < CardToolDtz.toVal(own_card))return DtzConstant.OWN_WIN;
			if(CardToolDtz.toVal(from_card) > CardToolDtz.toVal(own_card))return DtzConstant.FROM_WIN;
			//如果是三副牌筒子大小相同就比较花色
			if(param != null && param.length == 1 &&
                    (param[0] == DtzzConstants.play_type_3POK
                        || param[0] == DtzzConstants.play_type_3PERSON_3POK
                        || param[0] == DtzzConstants.play_type_2PERSON_3POK
                        || DtzzConstants.isKlSiXi(param[0])  //快乐四喜
                    )){
				if(CardToolDtz.toSuit(from_card) < CardToolDtz.toSuit(own_card))return DtzConstant.OWN_WIN;
				if(CardToolDtz.toSuit(from_card) > CardToolDtz.toSuit(own_card))return DtzConstant.FROM_WIN;
			}
			return DtzConstant.CARD_ERROR;
		}
	};
	
	/**
	 * 上家地炸的大小比较（只在三副牌出现）
	 */
	private IPokerCompare bombdi = new IPokerCompare() {
		@Override
		public int compareTo(CardPair from, CardPair own, int... param) {
			int from_card = from.getPokers().get(0), own_card = own.getPokers().get(0);
			if(from.getType() != own.getType())return DtzConstant.CARD_ERROR;
			if(from_card < own_card)return DtzConstant.OWN_WIN;
			if(from_card > own_card)return DtzConstant.FROM_WIN;
			return DtzConstant.CARD_ERROR;
		}
	};
	
	/**
	 * 上家单顺的大小比较（打筒子暂时用不到）
	 */
	private IPokerCompare straight = new IPokerCompare() {
		@Override
		public int compareTo(CardPair from, CardPair own, int... param) {
			int fromMaxCard = Collections.max(from.getPokers()), ownMaxCard = Collections.max(own.getPokers());
			//如果我出的牌的牌型是个炸，筒子，地炸，囍
			CardTypeDtz ownType = own.getType();
			if(ownType == CardTypeDtz.CARD_9 || 
				ownType == CardTypeDtz.CARD_10 ||
				ownType == CardTypeDtz.CARD_11 || 
				ownType == CardTypeDtz.CARD_13)return DtzConstant.OWN_WIN;
			if(from.getType() != ownType)return DtzConstant.CARD_ERROR;
			if(fromMaxCard < ownMaxCard)return DtzConstant.OWN_WIN;
			if(fromMaxCard > ownMaxCard)return DtzConstant.FROM_WIN;
			return DtzConstant.CARD_ERROR;
		}
	};
	
	/**
	 * 上家双顺的大小比较
	 */
	private IPokerCompare doubleStraight = new IPokerCompare() {
		@Override
		public int compareTo(CardPair from, CardPair own, int... param) {
			int fromMaxCard = Collections.max(from.getPokers());
			int ownMaxCard = Collections.max(own.getPokers());
			//如果我出的牌的牌型是个炸，筒子，地炸，囍
			CardTypeDtz ownType = own.getType();
			if(ownType == CardTypeDtz.CARD_9 || 
				ownType == CardTypeDtz.CARD_10 ||
				ownType == CardTypeDtz.CARD_11 ||
				ownType == CardTypeDtz.CARD_13)return DtzConstant.OWN_WIN;
			if(from.getType() != ownType)return DtzConstant.CARD_ERROR;
			if(from.getPokers().size() != own.getPokers().size())return DtzConstant.CARD_ERROR;
			if(fromMaxCard < ownMaxCard)return DtzConstant.OWN_WIN;
			if(fromMaxCard > ownMaxCard)return DtzConstant.FROM_WIN;
			return DtzConstant.CARD_ERROR;
		}
	};
	
	
	/**
	 * 上家三顺的大小比较
	 */
	private IPokerCompare threeStraight = new IPokerCompare() {
		@Override
		public int compareTo(CardPair from, CardPair own, int... param) {
			if(param == null || param.length != 1)return DtzConstant.CARD_ERROR;
			//如果我出的牌的牌型是个炸，筒子，地炸，囍
			CardTypeDtz ownType = own.getType();
			if(ownType == CardTypeDtz.CARD_9 || 
				ownType == CardTypeDtz.CARD_10 ||
				ownType == CardTypeDtz.CARD_11 ||
				ownType == CardTypeDtz.CARD_13)return DtzConstant.OWN_WIN;
			
			//同是三顺牌型
			if(own.getType() == from.getType()){
				//先判断数量是否相等
				if(from.getPokers().size() != own.getPokers().size()){
					//如果不等，而且都是三顺，说明上家或者自己出牌是把某3牌当做带牌，只需要依据本轮初始的连续数判定我出的牌即可
					int fromMaxCard = CardTypeToolDtz.getThreeStraightMaxCard(from);
					int ownMaxCard = CardTypeToolDtz.getThreeStraightMaxCard(own);
					if(fromMaxCard < ownMaxCard && (param[0]*3) <= own.getPokers().size() &&  own.getPokers().size() <= (param[0]*5))return DtzConstant.OWN_WIN;
				}else{
					//再判断最大值
					int fromMaxCard = CardTypeToolDtz.getThreeStraightMaxCard(from);
					int ownMaxCard = CardTypeToolDtz.getThreeStraightMaxCard(own);
					if(fromMaxCard < ownMaxCard)return DtzConstant.OWN_WIN;
					if(fromMaxCard > ownMaxCard)return DtzConstant.FROM_WIN;
				}
				return DtzConstant.CARD_ERROR;
			}
			//出牌的牌型为飞机也可以
			if(ownType == CardTypeDtz.CARD_8){
				//得到上家三顺的最大牌和连续的数
				int fromMaxCard = CardTypeToolDtz.getThreeStraightMaxCard(from);
				int fromCount = from.getPokers().size()/3;
				//考虑上家三顺也可以作为飞机来打，所有须与本轮初始出牌的玩家的飞机连续数匹配
				if(param[0] != fromCount){
					//如果不同，则以初始玩家的连续数为准
					fromCount = param[0];
				}
				//得到出牌的飞机的最大牌和连续的数
		        //先得到所有出现次数为3以上的牌
		        HashMap<Integer, Integer> map = new HashMap<>();
		        for(int card:own.getPokers()){
		    		if (!map.containsKey(card)) map.put(card,1);
		    		else map.put(card, map.get(card)+1);
		    	}
		        List<Integer> vList = new ArrayList<Integer>();
		        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
		    		if (entry.getValue() >= 3) {
		    			vList.add(entry.getKey());
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
		        //循环是否有  连续数>=上家 并且 最大牌 > 上家
		        for(Map.Entry<Integer, Integer> entry : countList.entrySet()){
		        	if(entry.getValue() >= fromCount && entry.getKey() > fromMaxCard){
		        		//如果有，再判断牌的数量是否符合
		        		if((fromCount*3) < own.getPokers().size() && own.getPokers().size() <= (fromCount*5))return DtzConstant.OWN_WIN;
		        	}
		        }
			}
			return DtzConstant.CARD_ERROR;
		}
	};
	
	
	/**
	 * 上家飞机带翅膀的大小比较
	 * param[0] 上家出牌飞机的连续数
	 */
	private IPokerCompare plan = new IPokerCompare() {
		@Override
		public int compareTo(CardPair from, CardPair own, int... param) {
			if(param == null || param.length != 1)return DtzConstant.CARD_ERROR;
			//如果我出的牌的牌型是个炸，筒子，地炸，囍
			CardTypeDtz ownType = own.getType();
			if(ownType == CardTypeDtz.CARD_9 || 
				ownType == CardTypeDtz.CARD_10 ||
				ownType == CardTypeDtz.CARD_11 ||
				ownType == CardTypeDtz.CARD_13)return DtzConstant.OWN_WIN;
		
			if(own.getType() == CardTypeDtz.CARD_7 ||  own.getType() == CardTypeDtz.CARD_8){
				//得到上家牌的对应连续数和最大牌
				int fromCount = param[0];
				
		        //先得到上家所有出现次数为3以上的牌
		        HashMap<Integer, Integer> map = new HashMap<>();
		        for(int card:from.getPokers()){
		    		if (!map.containsKey(card)) map.put(card,1);
		    		else map.put(card, map.get(card)+1);
		    	}
		        List<Integer> vList = new ArrayList<Integer>();
		        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
		    		if (entry.getValue() >= 3) {
		    			vList.add(entry.getKey());
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
		        //找到上家对应连续数的最大牌
		        int fromMaxCard = 0;
		        for(Map.Entry<Integer, Integer> entry : countList.entrySet()){
		        	if(entry.getValue() >= fromCount && entry.getKey() > fromMaxCard){
		        		fromMaxCard = entry.getKey();
		        	}
		        }
		        //判断上家牌牌数是否正确
		        if((fromCount*3) < from.getPokers().size() && from.getPokers().size() <= (fromCount*5)){
		        	//同样判断自己出的牌的最大连续数和最大值
		        	//如果自己出牌是三顺牌型
					if(own.getType() == CardTypeDtz.CARD_7){
						//三顺当做飞机打，判断连续数和最大牌
						int ownMaxCard = CardTypeToolDtz.getThreeStraightMaxCard(own);
						int ownCount = own.getPokers().size()/3;
						if((fromCount*3) <= own.getPokers().size() && own.getPokers().size() <= (fromCount*5) && fromMaxCard < ownMaxCard && fromCount <= ownCount)return DtzConstant.OWN_WIN;
						return DtzConstant.CARD_ERROR;
					}
					
					//出牌的牌型为飞机也可以
					if(ownType == CardTypeDtz.CARD_8){
						//得到出牌的飞机的最大牌和连续的数
				        //先得到所有出现次数为3以上的牌
				        HashMap<Integer, Integer> ownmap = new HashMap<>();
				        for(int card:own.getPokers()){
				    		if (!ownmap.containsKey(card)) ownmap.put(card,1);
				    		else ownmap.put(card, ownmap.get(card)+1);
				    	}
				        List<Integer> vOwnList = new ArrayList<Integer>();
				        for (Map.Entry<Integer, Integer> entry : ownmap.entrySet()) {
				    		if (entry.getValue() >= 3) {
				    			vOwnList.add(entry.getKey());
				    		}
				    	}
				        //排序后判断得到最大的连续飞机
				        Collections.sort(vOwnList, CardTypeToolDtz.comparator);
				        //得到连续最大牌、连续次数列表
				        Map<Integer,Integer> countOwnList = new HashMap<Integer,Integer>();
				        int stVOwn = vOwnList.get(0);
				        int countOwn = 1;
				        for(int i=1;i<vOwnList.size();i++){
				        	if(vOwnList.get(i) == (stVOwn+1)){
				        		countOwn++;
				        		if(i==vOwnList.size()-1){
				        			//如果是最后一个满足条件，需要加入列表
				        			countOwnList.put(vOwnList.get(i), countOwn);
				        		}
								stVOwn = vOwnList.get(i);
				        	}else{
				        		countOwnList.put(vOwnList.get(i-1), countOwn);
				        		countOwn = 1;
				        		stVOwn = vOwnList.get(i);
				        	}
				        }
				        //循环是否有  连续数>=上家 并且 最大牌 > 上家
				        for(Map.Entry<Integer, Integer> entry : countOwnList.entrySet()){
				        	if(entry.getValue() >= fromCount && entry.getKey() > fromMaxCard){
				        		//如果有，再判断牌的数量是否符合
				        		if((fromCount*3) < own.getPokers().size() && own.getPokers().size() <= (fromCount*5))return DtzConstant.OWN_WIN;
				        	}
				        }
					}
		        }
			}
			return DtzConstant.CARD_ERROR;
		}
	};
	
	
	/**
	 * 上家三带一或者三带二的大小比较
	 */
	private IPokerCompare threeWithone = new IPokerCompare() {
		@Override
		public int compareTo(CardPair from, CardPair own, int... param) {
			//如果我出的牌是个炸弹
			CardTypeDtz ownType = own.getType();
			if(ownType == CardTypeDtz.CARD_9 || 
				ownType == CardTypeDtz.CARD_10 ||
				ownType == CardTypeDtz.CARD_11 ||
				ownType == CardTypeDtz.CARD_13)return DtzConstant.OWN_WIN;
			if(own.getType() == CardTypeDtz.CARD_3 || own.getType() == CardTypeDtz.CARD_4){
				int fromCard = getThreeCardOfOne(from), ownCard = getThreeCardOfOne(own);
				if(fromCard < ownCard)return DtzConstant.OWN_WIN;
				if(fromCard > ownCard)return DtzConstant.FROM_WIN;
			}
			return DtzConstant.CARD_ERROR;
		}
	};
	
	/**
	 * 上家王炸的大小比较，暂时未实现
	 */
	private IPokerCompare jacker = new IPokerCompare() {
		@Override
		public int compareTo(CardPair from, CardPair own, int... param) {
			return DtzConstant.CARD_ERROR;
		}
	};
	
	/**
	 * 上家为囍的大小判断 （囍不判断花色，只比较大小）
	 */
	private IPokerCompare xi = new IPokerCompare() {
		@Override
		public int compareTo(CardPair from, CardPair own, int... param) {
			int from_card = from.getPokers().get(0), own_card = own.getPokers().get(0);
			if(from.getType() != own.getType())return DtzConstant.CARD_ERROR;
			//先判断囍的大小
			if(CardToolDtz.toVal(from_card) < CardToolDtz.toVal(own_card))return DtzConstant.OWN_WIN;
			if(CardToolDtz.toVal(from_card) > CardToolDtz.toVal(own_card))return DtzConstant.FROM_WIN;
			//判断囍的花色
            if(param != null && param.length == 1 && DtzzConstants.isKlSiXi(param[0]) ){
                if(CardToolDtz.toSuit(from_card) < CardToolDtz.toSuit(own_card))return DtzConstant.OWN_WIN;
                if(CardToolDtz.toSuit(from_card) > CardToolDtz.toSuit(own_card))return DtzConstant.FROM_WIN;
            }

			return DtzConstant.CARD_ERROR;
		}
	};
	
	/**
	 * 得到比较器
	 * @param cardType
	 * @return
	 */
	public IPokerCompare getCompare(CardTypeDtz cardType) {
		switch (cardType) {
		case CARD_1: return single;
		case CARD_2: return pair;
		case CARD_3: return three;
		case CARD_4: return threeWithone;
		case CARD_5: return straight;
		case CARD_6: return doubleStraight;
		case CARD_7: return threeStraight;
		case CARD_8: return plan;
		case CARD_9: return bomb;
		case CARD_10 : return tongzi;
		case CARD_11 : return bombdi;
		case CARD_12 : return jacker;
		case CARD_13 : return xi;
		 default : return null;
		}
	}
	
	/**
	 * 得到三张或者三带一、三带二的三张牌的大小
	 * @param cardPair
	 * @return
	 */
	private int getThreeCardOfOne(CardPair cardPair) {
		Map<Integer,Integer> map = new HashMap<>();
		for(int val:cardPair.getPokers()){
			if(!map.containsKey(val)){
				map.put(val,1);
			}else{
				//包含的话，只要前面的已经有两个就能得到结果了
				if(map.get(val)==2)return val;
				map.put(val,map.get(val)+1);
			}
		}
		return 0;
	}
}
