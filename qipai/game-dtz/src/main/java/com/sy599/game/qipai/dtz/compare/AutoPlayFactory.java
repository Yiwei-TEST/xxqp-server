package com.sy599.game.qipai.dtz.compare;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sy599.game.qipai.dtz.bean.AutoHandPais;
import com.sy599.game.qipai.dtz.bean.CardPair;
import com.sy599.game.qipai.dtz.bean.DtzTable;
import com.sy599.game.qipai.dtz.rule.CardTypeDtz;
import com.sy599.game.qipai.dtz.tool.CardToolDtz;
import com.sy599.game.util.LogUtil;

public class AutoPlayFactory {

	public static final AutoPlayFactory factory = new AutoPlayFactory();
	
	/**
	 * 单牌
	 */
	private AutoPlay single = new AutoPlay(){
		@Override
		public CardPair autoPlay(AutoHandPais autoHandPais, List<Integer> outCards, DtzTable table) {
			int outMaxVal = CardToolDtz.toVal(outCards.get(0));
			if(!autoHandPais.getSingle().isEmpty()){
				for(Entry<Integer, Integer> entry : autoHandPais.getSingle().entrySet()){
					if(entry.getKey()>outMaxVal){
						return new CardPair(Arrays.asList(entry.getValue()), CardTypeDtz.CARD_1);
					}
				}
			}
			if(!autoHandPais.getPair().isEmpty()){
				for(Entry<Integer, List<Integer>> entry : autoHandPais.getPair().entrySet()){
					if(entry.getKey()>outMaxVal){
						return new CardPair(Arrays.asList(entry.getValue().get(0)), CardTypeDtz.CARD_1);
					}
				}
			}
			if(!autoHandPais.getThree().isEmpty()){
				for(Entry<Integer, List<Integer>> entry : autoHandPais.getThree().entrySet()){
					if(entry.getKey()>outMaxVal){
						return new CardPair(Arrays.asList(entry.getValue().get(0)), CardTypeDtz.CARD_1);
					}
				}
			}
			return disBomb(autoHandPais, table);
		}
	};
	/**
	 * 对子
	 */
	private AutoPlay pair = new AutoPlay(){
		@Override
		public CardPair autoPlay(AutoHandPais autoHandPais, List<Integer> outCards, DtzTable table) {
			int outMaxVal = CardToolDtz.toVal(outCards.get(0));
			if(!autoHandPais.getPair().isEmpty()){
				for(Entry<Integer, List<Integer>> entry : autoHandPais.getPair().entrySet()){
					if(entry.getKey()>outMaxVal){
						return new CardPair(entry.getValue(), CardTypeDtz.CARD_2);
					}
				}
			}
			if(!autoHandPais.getThree().isEmpty()){
				for(Entry<Integer, List<Integer>> entry : autoHandPais.getThree().entrySet()){
					if(entry.getKey()>outMaxVal){
						return new CardPair(entry.getValue().subList(0, 2), CardTypeDtz.CARD_2);
					}
				}
			}
			return disBomb(autoHandPais, table);
		}
	};
	/**
	 * 三张  三带一二
	 */
	private AutoPlay three = new AutoPlay(){
		@Override
		public CardPair autoPlay(AutoHandPais autoHandPais, List<Integer> outCards, DtzTable table) {
			int outMaxVal = CardToolDtz.toVal(outCards.get(0));
			if(outCards.size()>3){
				Map<Integer,Integer> map = new HashMap<>();
				for(int val : outCards){
					val = CardToolDtz.toVal(val);
					if(!map.containsKey(val)){
						map.put(val,1);
					}else{
						if(map.get(val)==2){
							outMaxVal = val;
							break;
						}
						map.put(val,map.get(val)+1);
					}
				}
			}
			if(!autoHandPais.getThree().isEmpty()){
				List<Integer> temp = null;
				for(Entry<Integer, List<Integer>> entry : autoHandPais.getThree().entrySet()){
					if(entry.getKey()>outMaxVal){
						temp = entry.getValue();
						break;
					}
				}
				if(temp != null){
					if(table.getIsDaiPai() == 1 && !autoHandPais.getSingle().isEmpty()){
						int i = 0;
						for(Integer pai : autoHandPais.getSingle().values()){
							if(i>=2) break;
							temp.add(pai);
							i++;
						}
						return new CardPair(temp, CardTypeDtz.CARD_4);
					}else{
						return new CardPair(temp, CardTypeDtz.CARD_3);
					}
				}
			}
			return disBomb(autoHandPais, table);
		}
	};
	/**
	 * 双顺
	 */
	private AutoPlay doubleStraight = new AutoPlay(){
		@Override
		public CardPair autoPlay(AutoHandPais autoHandPais, List<Integer> outCards, DtzTable table) {
			int outMaxVal = Collections.max(CardToolDtz.toValueList(outCards)),len = outCards.size()/2;
			int maxCard = 0;
			if( (table.isFourPai() && !table.isKlSX()) || table.getWangTongZi()==1){
				maxCard = 17;
			}else{
				maxCard = 14;
			}
			List<Integer> temp = new ArrayList<>();
			for(int i=outMaxVal-len+2;i<=maxCard;i++){
				if(temp.size() >= len*2){
					break;
				}
				if(!autoHandPais.getPair().isEmpty() && autoHandPais.getPair().containsKey(i)){
					temp.addAll(autoHandPais.getPair().get(i));
					continue;
				}
				if(!autoHandPais.getThree().isEmpty() && autoHandPais.getThree().containsKey(i)){
					temp.addAll(autoHandPais.getThree().get(i).subList(0, 2));
					continue;
				}
				temp.clear();
			}
			if(!temp.isEmpty() && temp.size()==outCards.size()){
				return new CardPair(temp, CardTypeDtz.CARD_6);
			}
			return disBomb(autoHandPais, table);
		}
	};
	/**
	 * 三顺  飞机
	 */
	private AutoPlay threeStraight = new AutoPlay(){
		@Override
		public CardPair autoPlay(AutoHandPais autoHandPais, List<Integer> outCards, DtzTable table) {
			int len = table.getFirstCardType().getValue1();
			if(len!=0){
				Map<Integer,Integer> map = new HashMap<>();
				for(int val : outCards){
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
				int outMaxVal = Collections.max(list);
				if(list.size() != len){
					Collections.sort(list,Collections.reverseOrder());
					for(int i=0;i<list.size();i++){
						if(list.get(i)-len+1 == list.get(i+len-1)){
							outMaxVal = list.get(i);
							break;
						}
					}
				}
				int maxCard = 0;
				if( (table.isFourPai() && !table.isKlSX() ) || table.getWangTongZi()==1){
					maxCard = 15;
				}else{
					maxCard = 14;
				}
				List<Integer> temp = new ArrayList<>();
				for(int i=outMaxVal-len+2;i<=maxCard;i++){
					if(temp.size() >= len*3){
						break;
					}
					if(!autoHandPais.getThree().isEmpty() && autoHandPais.getThree().containsKey(i)){
						temp.addAll(autoHandPais.getThree().get(i));
						continue;
					}
					temp.clear();
				}
				if(!temp.isEmpty() && temp.size()==len*3){
					if(table.getIsDaiPai() == 1 && !autoHandPais.getSingle().isEmpty()){
						int i = 0;
						for(Integer pai : autoHandPais.getSingle().values()){
							if(i>=len*2) break;
							temp.add(pai);
							i++;
						}
						return new CardPair(temp, CardTypeDtz.CARD_8);
					}else{
						return new CardPair(temp, CardTypeDtz.CARD_7);
					}
				}
			}else{
				LogUtil.errorLog.warn("threeStraight length error:tableId="+table.getId()+",len="+len);
			}

			return disBomb(autoHandPais, table);
		}
	};
	/**
	 * 炸弹
	 */
	private AutoPlay bomb = new AutoPlay(){
		@Override
		public CardPair autoPlay(AutoHandPais autoHandPais, List<Integer> outCards, DtzTable table) {
			int outMaxVal = CardToolDtz.toVal(outCards.get(0)),len = outCards.size();
			if(!autoHandPais.getBomb().isEmpty()){
				autoHandPais.setBombOrder(autoHandPais.getBomb());
				for(Entry<Integer, List<Integer>> entry : autoHandPais.getBomb().entrySet()){
					if(entry.getValue().size() > len || (entry.getValue().size() == len && entry.getKey() > outMaxVal)){
						return new CardPair(entry.getValue(), CardTypeDtz.CARD_9);
					}
				}
			}
			if(!autoHandPais.getTongzi().isEmpty()){
				autoHandPais.setOrder(autoHandPais.getTongzi());
				for(Entry<Integer, List<Integer>> entry : autoHandPais.getTongzi().entrySet()){
					return new CardPair(entry.getValue(), CardTypeDtz.CARD_10);
				}
			}
			if(table.isThreePai()){
				if(!autoHandPais.getBombdi().isEmpty()){
					for(Entry<Integer, List<Integer>> entry : autoHandPais.getBombdi().entrySet()){
						return new CardPair(entry.getValue(), CardTypeDtz.CARD_11);
					}
				}
			}else{
				if(!autoHandPais.getXi().isEmpty()){
					autoHandPais.setOrder(autoHandPais.getXi());
					for(Entry<Integer, List<Integer>> entry : autoHandPais.getXi().entrySet()){
						return new CardPair(entry.getValue(), CardTypeDtz.CARD_13);
					}
				}
			}
			return null;
		}
	};
	/**
	 * 筒子
	 */
	private AutoPlay tongzi = new AutoPlay(){
		@Override
		public CardPair autoPlay(AutoHandPais autoHandPais, List<Integer> outCards, DtzTable table) {
			int outMaxVal = CardToolDtz.toVal(outCards.get(0));
			if(!autoHandPais.getTongzi().isEmpty()){
				autoHandPais.setOrder(autoHandPais.getTongzi());
				for(Entry<Integer, List<Integer>> entry : autoHandPais.getTongzi().entrySet()){
					int val = CardToolDtz.toVal(entry.getKey());
					if(val > outMaxVal){
						return new CardPair(entry.getValue(), CardTypeDtz.CARD_10);
					}
					if(val == outMaxVal && entry.getKey() > outCards.get(0) && (table.isThreePai() || table.isKlSX())){
						return new CardPair(entry.getValue(), CardTypeDtz.CARD_10);
					}
				}
			}
			if(table.isThreePai()){
				if(!autoHandPais.getBombdi().isEmpty()){
					for(Entry<Integer, List<Integer>> entry : autoHandPais.getBombdi().entrySet()){
						return new CardPair(entry.getValue(), CardTypeDtz.CARD_11);
					}
				}
			}else{
				if(!autoHandPais.getXi().isEmpty()){
					autoHandPais.setOrder(autoHandPais.getXi());
					for(Entry<Integer, List<Integer>> entry : autoHandPais.getXi().entrySet()){
						return new CardPair(entry.getValue(), CardTypeDtz.CARD_13);
					}
				}
			}
			return null;
		}
	};
	/**
	 * 地炸
	 */
	private AutoPlay bombdi = new AutoPlay(){
		@Override
		public CardPair autoPlay(AutoHandPais autoHandPais, List<Integer> outCards, DtzTable table) {
			int outMaxVal = CardToolDtz.toVal(outCards.get(0));
			if(table.isThreePai()){
				if(!autoHandPais.getBombdi().isEmpty()){
					for(Entry<Integer, List<Integer>> entry : autoHandPais.getBombdi().entrySet()){
						if(entry.getKey() > outMaxVal){
							return new CardPair(entry.getValue(), CardTypeDtz.CARD_11);
						}
					}
				}
			}
			return null;
		}
	};
	/**
	 * 囍
	 */
	private AutoPlay xi = new AutoPlay(){
		@Override
		public CardPair autoPlay(AutoHandPais autoHandPais, List<Integer> outCards, DtzTable table) {
			int outMaxVal = CardToolDtz.toVal(outCards.get(0));
			if(table.isFourPai()){
				if(!autoHandPais.getXi().isEmpty()){
					autoHandPais.setOrder(autoHandPais.getXi());
					for(Entry<Integer, List<Integer>> entry : autoHandPais.getXi().entrySet()){
                        int val = CardToolDtz.toVal(entry.getKey());
						if(val > outMaxVal){
							return new CardPair(entry.getValue(), CardTypeDtz.CARD_13);
						}else if(val == outMaxVal && entry.getKey() > outCards.get(0)){
                            return new CardPair(entry.getValue(), CardTypeDtz.CARD_13);
                        }
					}
				}
			}
			return null;
		}
	};
	/**
	 * 首出
	 */
	private AutoPlay first = new AutoPlay(){
		@Override
		public CardPair autoPlay(AutoHandPais autoHandPais, List<Integer> outCards, DtzTable table) {
			if(!autoHandPais.getSingle().isEmpty()){
				for(Entry<Integer, Integer> entry : autoHandPais.getSingle().entrySet()){
					return new CardPair(Arrays.asList(entry.getValue()), CardTypeDtz.CARD_1);
				}
			}
			if(!autoHandPais.getPair().isEmpty()){
				for(Entry<Integer, List<Integer>> entry : autoHandPais.getPair().entrySet()){
					return new CardPair(entry.getValue(), CardTypeDtz.CARD_2);
				}
			}
			if(!autoHandPais.getThree().isEmpty()){
				for(Entry<Integer, List<Integer>> entry : autoHandPais.getThree().entrySet()){
					return new CardPair(entry.getValue(), CardTypeDtz.CARD_3);
				}
			}
			if(!autoHandPais.getBomb().isEmpty()){
				autoHandPais.setBombOrder(autoHandPais.getBomb());
				for(Entry<Integer, List<Integer>> entry : autoHandPais.getBomb().entrySet()){
					return new CardPair(entry.getValue(), CardTypeDtz.CARD_9);
				}
			}
			if(!autoHandPais.getTongzi().isEmpty()){
				autoHandPais.setOrder(autoHandPais.getTongzi());
				for(Entry<Integer, List<Integer>> entry : autoHandPais.getTongzi().entrySet()){
					return new CardPair(entry.getValue(), CardTypeDtz.CARD_10);
				}
			}
			if(table.isThreePai()){
				if(!autoHandPais.getBombdi().isEmpty()){
					for(Entry<Integer, List<Integer>> entry : autoHandPais.getBombdi().entrySet()){
						return new CardPair(entry.getValue(), CardTypeDtz.CARD_11);
					}
				}
			}else{
				if(!autoHandPais.getXi().isEmpty()){
					autoHandPais.setOrder(autoHandPais.getXi());
					for(Entry<Integer, List<Integer>> entry : autoHandPais.getXi().entrySet()){
						return new CardPair(entry.getValue(), CardTypeDtz.CARD_13);
					}
				}
			}
			return null;
		}
	};
	public AutoPlay getFirstAutoPlay(){
		return first;
	}
	public AutoPlay getAutoPlay(CardTypeDtz cardType){
		if(cardType  == null){
			return null;
		}
		switch(cardType){
			case CARD_1: return single;
			case CARD_2: return pair;
			case CARD_3: 
			case CARD_4: return three;
			case CARD_6: return doubleStraight;
			case CARD_7: 
			case CARD_8: return threeStraight;
			case CARD_9: return bomb;
			case CARD_10 : return tongzi;
			case CARD_11 : return bombdi;
			case CARD_13 : return xi;
			default : return first;
		}
	}
	
	private CardPair disBomb(AutoHandPais autoHandPais, DtzTable table){
		if(!autoHandPais.getBomb().isEmpty()){
			autoHandPais.setBombOrder(autoHandPais.getBomb());
			for(Entry<Integer, List<Integer>> entry : autoHandPais.getBomb().entrySet()){
				return new CardPair(entry.getValue(), CardTypeDtz.CARD_9);
			}
		}
		if(!autoHandPais.getTongzi().isEmpty()){
			autoHandPais.setOrder(autoHandPais.getTongzi());
			for(Entry<Integer, List<Integer>> entry : autoHandPais.getTongzi().entrySet()){
				return new CardPair(entry.getValue(), CardTypeDtz.CARD_10);
			}
		}
		if(table.isThreePai()){
			if(!autoHandPais.getBombdi().isEmpty()){
				for(Entry<Integer, List<Integer>> entry : autoHandPais.getBombdi().entrySet()){
					return new CardPair(entry.getValue(), CardTypeDtz.CARD_11);
				}
			}
		}else{
			if(!autoHandPais.getXi().isEmpty()){
				autoHandPais.setOrder(autoHandPais.getXi());
				for(Entry<Integer, List<Integer>> entry : autoHandPais.getXi().entrySet()){
					return new CardPair(entry.getValue(), CardTypeDtz.CARD_13);
				}
			}
		}
		return null;
	}
}
