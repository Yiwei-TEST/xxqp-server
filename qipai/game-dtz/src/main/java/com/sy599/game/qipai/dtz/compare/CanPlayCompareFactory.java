package com.sy599.game.qipai.dtz.compare;

import com.sy599.game.qipai.dtz.bean.CardPair;
import com.sy599.game.qipai.dtz.bean.DtzModel;
import com.sy599.game.qipai.dtz.bean.DtzTable;
import com.sy599.game.qipai.dtz.constant.DtzzConstants;
import com.sy599.game.qipai.dtz.rule.CardTypeDtz;
import com.sy599.game.qipai.dtz.tool.CardToolDtz;

import java.util.*;

public class CanPlayCompareFactory {

	public static final CanPlayCompareFactory factory = new CanPlayCompareFactory();
	public static final int PASS_CARD = 0;//过
	public static final int PLAY_CARD = 1;
	/**
	 * 单牌
	 */
	private CanPlayCompare single = new CanPlayCompare() {
		@Override
		public int compareTo(DtzModel model, CardPair cardPair, Object... param) {
			Map<Integer, Integer> pai = model.getPai();
			int outMaxCard = CardToolDtz.toVal(cardPair.getPokers().get(0));
			for(Map.Entry<Integer, Integer> entry : pai.entrySet()){
				if(entry.getKey() > outMaxCard){
					return PLAY_CARD;
				}
				if(entry.getValue() >= 4){
					return PLAY_CARD;
				}
				if(entry.getValue() >= 3){
					if(entry.getKey() == 16 || entry.getKey() == 17){
						return PLAY_CARD;
					}else{
						for(int i=1;i<=4;i++){
							int code = i*100+entry.getKey();
							if(model.getPaiCode().containsKey(code) && model.getPaiCode().get(code) >= 3){
								return PLAY_CARD;
							}
						}
					}
				}
			}
			return PASS_CARD;
		}
	};
	/**
	 * 对子
	 */
	private CanPlayCompare pair = new CanPlayCompare() {
		@Override
		public int compareTo(DtzModel model, CardPair cardPair, Object... param) {
			Map<Integer, Integer> pai = model.getPai();
			int outMaxCard = CardToolDtz.toVal(cardPair.getPokers().get(0));
			for(Map.Entry<Integer, Integer> entry : pai.entrySet()){
				if(entry.getValue() >= 4){
					return PLAY_CARD;
				}
				if(entry.getKey() > outMaxCard && entry.getValue() >= 2){
					return PLAY_CARD;
				}
				if(entry.getValue() >= 3){
					if(entry.getKey() == 16 || entry.getKey() == 17){
						return PLAY_CARD;
					}else{
						for(int i=1;i<=4;i++){
							int code = i*100+entry.getKey();
							if(model.getPaiCode().containsKey(code) && model.getPaiCode().get(code) >= 3){
								return PLAY_CARD;
							}
						}
					}
				}
			}
			return PASS_CARD;
		}
		
	};
	/**
	 * 三张
	 */
	private CanPlayCompare three = new CanPlayCompare() {
		@Override
		public int compareTo(DtzModel model, CardPair cardPair, Object... param) {
			Map<Integer, Integer> pai = model.getPai();
			int outMaxCard = CardToolDtz.toVal(cardPair.getPokers().get(0));
			for(Map.Entry<Integer, Integer> entry : pai.entrySet()){
				if(entry.getValue() >= 4){
					return PLAY_CARD;
				}
				if(entry.getValue() >= 3){
					if(entry.getKey() > outMaxCard){
						return PLAY_CARD;
					}
					if(entry.getKey() == 16 || entry.getKey() == 17){
						return PLAY_CARD;
					}else{
						for(int i=1;i<=4;i++){
							int code = i*100+entry.getKey();
							if(model.getPaiCode().containsKey(code) && model.getPaiCode().get(code) >= 3){
								return PLAY_CARD;
							}
						}
					}
				}
			}
			return PASS_CARD;
		}
		
	};
	/**
	 * 三带一或者三带二
	 */
	private CanPlayCompare threeWithone = new CanPlayCompare() {
		@Override
		public int compareTo(DtzModel model, CardPair cardPair, Object... param) {
			Map<Integer, Integer> pai = model.getPai();
			Map<Integer,Integer> map = new HashMap<>();
			int outMaxCard = 0;
			for(int val : cardPair.getPokers()){
				val = CardToolDtz.toVal(val);
				if(!map.containsKey(val)){
					map.put(val,1);
				}else{
					if(map.get(val)==2){
						outMaxCard = val;
						break;
					}
					map.put(val,map.get(val)+1);
				}
			}
			for(Map.Entry<Integer, Integer> entry : pai.entrySet()){
				if(entry.getValue() >= 4){
					return PLAY_CARD;
				}
				if(entry.getValue() >= 3){
					if(entry.getKey() > outMaxCard){
						return PLAY_CARD;
					}
					if(entry.getKey() == 16 || entry.getKey() == 17){
						return PLAY_CARD;
					}else{
						for(int i=1;i<=4;i++){
							int code = i*100+entry.getKey();
							if(model.getPaiCode().containsKey(code) && model.getPaiCode().get(code) >= 3){
								return PLAY_CARD;
							}
						}
					}
				}
			}
			return PASS_CARD;
		}
	};
	/**
	 * 单顺(暂时没用)
	 */
	private CanPlayCompare straight = new CanPlayCompare() {
		@Override
		public int compareTo(DtzModel model, CardPair cardPair, Object... param) {
			return PASS_CARD;
		}
	};
	/**
	 * 双顺
	 */
	private CanPlayCompare doubleStraight = new CanPlayCompare() {
		@Override
		public int compareTo(DtzModel model, CardPair cardPair, Object... param) {
			//int playType = Integer.parseInt(param[0]+"");
			DtzTable table = (DtzTable)param[0];
			List<Integer> list =  CardToolDtz.toValueList(cardPair.getPokers());
			return Straight(model, Collections.max(list), table, 2, (cardPair.getPokers().size()/2));
		}
	};
	/**
	 * 三顺
	 */
	private CanPlayCompare threeStraight = new CanPlayCompare() {
		@Override
		public int compareTo(DtzModel model, CardPair cardPair, Object... param) {
			//int playType = Integer.parseInt(param[0]+"");
			DtzTable table = (DtzTable)param[0];
			int len = cardPair.getPokers().size()/3;
			if(table.getFirstCardType().getValue1() != 0){
				len = table.getFirstCardType().getValue1();
			}
			List<Integer> list =  CardToolDtz.toValueList(cardPair.getPokers());
			return Straight(model, Collections.max(list), table, 3, len);
		}
	};
	/**
	 * 飞机带翅膀
	 */
	private CanPlayCompare plan = new CanPlayCompare() {
		@Override
		public int compareTo(DtzModel model, CardPair cardPair, Object... param) {
			//int playType = Integer.parseInt(param[0]+"");
			DtzTable table = (DtzTable)param[0];
			int len = table.getFirstCardType().getValue1();
			Map<Integer,Integer> map = new HashMap<>();
			for(int val : cardPair.getPokers()){
				val = CardToolDtz.toVal(val);
				if(!map.containsKey(val)){
					map.put(val,1);
				}else{
					map.put(val,map.get(val)+1);
				}
			}
			List<Integer> list = new ArrayList<Integer>();
			for(Map.Entry<Integer,Integer> entry : map.entrySet()){
				if(entry.getValue() >= 3){
					list.add(entry.getKey());
				}
			}
			int outMaxCard = Collections.max(list);
			if(list.size() != len){
				Collections.sort(list,Collections.reverseOrder());
				for(int i=0;i<list.size();i++){
					if(list.get(i)-len+1 == list.get(i+len-1)){
						outMaxCard = list.get(i);
						break;
					}
				}
			}
			return Straight(model, outMaxCard, table, 3, len);
		}
	};
	/**
	 * 炸弹
	 */
	private CanPlayCompare bomb = new CanPlayCompare() {
		@Override
		public int compareTo(DtzModel model, CardPair cardPair, Object... param) {
			DtzTable table = (DtzTable)param[0];
			int playType = table.getPlayType();
			Map<Integer, Integer> pai = model.getPai();
			int outMaxCard = CardToolDtz.toVal(cardPair.getPokers().get(0)),len = cardPair.getPokers().size();
			for(Map.Entry<Integer, Integer> entry : pai.entrySet()){
				if(entry.getValue() > len || (entry.getValue() == len && entry.getKey() > outMaxCard)){
					return PLAY_CARD;
				}
				if(entry.getValue() >= 3){
					if(entry.getKey() == 16 || entry.getKey() == 17){
						return PLAY_CARD;
					}
					for(int i=1;i<=4;i++){
						int code = i*100+entry.getKey();
						if(model.getPaiCode().containsKey(code) && model.getPaiCode().get(code) >= 3){
							return PLAY_CARD;
						}
					}
					if(playType == DtzzConstants.play_type_3POK || playType == DtzzConstants.play_type_3PERSON_3POK || playType == DtzzConstants.play_type_2PERSON_3POK){
						if(entry.getValue() >= 8){
							Map<Integer, Integer> paiCode = model.getPaiCode();
							if(paiCode.containsKey(entry.getKey()+100) && paiCode.get(entry.getKey()+100) >= 2 
									&& paiCode.containsKey(entry.getKey()+200) && paiCode.get(entry.getKey()+200) >= 2 
									&& paiCode.containsKey(entry.getKey()+300) && paiCode.get(entry.getKey()+300) >= 2 
									&& paiCode.containsKey(entry.getKey()+400) && paiCode.get(entry.getKey()+400) >= 2){
								return PLAY_CARD;
							}
						}
					}
				}
			}
			return PASS_CARD;
		}
	};
	/**
	 * 筒子
	 */
	private CanPlayCompare tongzi = new CanPlayCompare() {
		@Override
		public int compareTo(DtzModel model, CardPair cardPair, Object... param) {
			DtzTable table = (DtzTable)param[0];
			int playType = table.getPlayType();
			int outMaxCard = CardToolDtz.toVal(cardPair.getPokers().get(0));
			for(Map.Entry<Integer, Integer> entry : model.getPaiCode().entrySet()){
				if(entry.getValue() >= 3){
					int cardval = CardToolDtz.toVal(entry.getKey());
					if(cardval > outMaxCard || entry.getValue() > 3){
						return PLAY_CARD;
					}
					if(cardval == outMaxCard &&
                            (playType == DtzzConstants.play_type_3POK
                                    || playType == DtzzConstants.play_type_3PERSON_3POK
                                    || playType == DtzzConstants.play_type_2PERSON_3POK
                                    || DtzzConstants.isKlSiXi(playType))
							&& entry.getKey() > cardPair.getPokers().get(0)){
						return PLAY_CARD;
					}
				}
			}
			if(playType == DtzzConstants.play_type_3POK
                    || playType == DtzzConstants.play_type_3PERSON_3POK
                    || playType == DtzzConstants.play_type_2PERSON_3POK){
				for(Map.Entry<Integer, Integer> entry : model.getPai().entrySet()){
					if(entry.getValue() >= 8){
						Map<Integer, Integer> paiCode = model.getPaiCode();
						if(paiCode.containsKey(entry.getKey()+100) && paiCode.get(entry.getKey()+100) >= 2 
								&& paiCode.containsKey(entry.getKey()+200) && paiCode.get(entry.getKey()+200) >= 2 
								&& paiCode.containsKey(entry.getKey()+300) && paiCode.get(entry.getKey()+300) >= 2 
								&& paiCode.containsKey(entry.getKey()+400) && paiCode.get(entry.getKey()+400) >= 2){
							return PLAY_CARD;
						}
					}
				}
			}
			return PASS_CARD;
		}
	};
	/**
	 * 地炸
	 */
	private CanPlayCompare bombdi = new CanPlayCompare() {
		@Override
		public int compareTo(DtzModel model, CardPair cardPair, Object... param) {
			int outMaxCard = CardToolDtz.toVal(cardPair.getPokers().get(0));
			for(Map.Entry<Integer, Integer> entry : model.getPai().entrySet()){
				if(entry.getValue() >= 8 && entry.getKey() > outMaxCard){
					Map<Integer, Integer> paiCode = model.getPaiCode();
					if(paiCode.containsKey(entry.getKey()+100) && paiCode.get(entry.getKey()+100) >= 2 
							&& paiCode.containsKey(entry.getKey()+200) && paiCode.get(entry.getKey()+200) >= 2 
							&& paiCode.containsKey(entry.getKey()+300) && paiCode.get(entry.getKey()+300) >= 2 
							&& paiCode.containsKey(entry.getKey()+400) && paiCode.get(entry.getKey()+400) >= 2){
						return PLAY_CARD;
					}
				}
			}
			return PASS_CARD;
		}
	};
	/**
	 * 王炸的大小(暂时没用)
	 */
	private CanPlayCompare jacker = new CanPlayCompare() {
		@Override
		public int compareTo(DtzModel model, CardPair cardPair, Object... param) {
			return PASS_CARD;
		}
	};
	/**
	 * 囍
	 */
	private CanPlayCompare xi = new CanPlayCompare() {
		@Override
		public int compareTo(DtzModel model, CardPair cardPair, Object... param) {
            DtzTable table = (DtzTable)param[0];
            int playType = table.getPlayType();
            int outMaxCard = CardToolDtz.toVal(cardPair.getPokers().get(0));
			for(Map.Entry<Integer, Integer> entry : model.getPaiCode().entrySet()){
                if(entry.getValue() == 4){
                    int cardval = CardToolDtz.toVal(entry.getKey());
                    if(cardval > outMaxCard || entry.getValue() > 4){
                        return PLAY_CARD;
                    }
                    if(cardval == outMaxCard &&
                            (playType == DtzzConstants.play_type_3POK
                                    || playType == DtzzConstants.play_type_3PERSON_3POK
                                    || playType == DtzzConstants.play_type_2PERSON_3POK
                                    || DtzzConstants.isKlSiXi(playType))
                            && entry.getKey() > cardPair.getPokers().get(0)){
                        return PLAY_CARD;
                    }
                }

			}
			return PASS_CARD;
		}
	};
	/**
	 * 得到比较器
	 * @param cardType
	 * @return
	 */
	public CanPlayCompare getCompare(CardTypeDtz cardType){
		if(cardType  == null){
			return null;
		}
		switch(cardType){
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
	 * 顺子
	 * @param model
	 * @param outMaxCard 最大顺牌
	 * @param table table
	 * @param num 2为双顺 3为三顺
	 * @param len 顺长度
	 * @return
	 */
	private int Straight(DtzModel model, int outMaxCard, DtzTable table, int num, int len){
		Map<Integer, Integer> pai = model.getPai();
		int maxCard = 0;
		if( (table.isFourPai() && !table.isKlSX()) || table.getWangTongZi()==1){
			if(num == 3){
				maxCard = 15;
			}else{
				maxCard = 17;
			}
		}else{
			maxCard = 14;
		}
		List<Integer> list = new ArrayList<Integer>();
		for(Map.Entry<Integer, Integer> entry : model.getPai().entrySet()){
			if(entry.getValue() >= num){
				if(entry.getValue() >= 4){  
					return PLAY_CARD;
				}
				if(entry.getValue() == 3){
					if(entry.getKey() == 16 || entry.getKey() == 17){
						return PLAY_CARD;
					}else{
						for(int i=1;i<=4;i++){
							int code = i*100+entry.getKey();
							if(model.getPaiCode().containsKey(code) && model.getPaiCode().get(code) >= 3){
								return PLAY_CARD;
							}
						}
					}
				}
				if(entry.getKey() <= maxCard){
					list.add(entry.getKey());
				}
			}
		}
		ok:
		for(int val : list){
			if(val > outMaxCard){
				for(int i=1;i<len;i++){
					if(!pai.containsKey(val-i) || pai.get(val-i)<num){
						continue ok;
					}
				}
				return PLAY_CARD;
			}
		}
		return PASS_CARD;
	}
}
