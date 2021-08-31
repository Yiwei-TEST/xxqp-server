package com.sy599.game.qipai.xx2710.rule;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sy599.game.qipai.xx2710.bean.CardTypeHuxi;
import com.sy599.game.qipai.xx2710.bean.PaohzDisAction;
import com.sy599.game.qipai.xx2710.bean.Xx2710Player;
import com.sy599.game.qipai.xx2710.bean.Xx2710Table;
import com.sy599.game.qipai.xx2710.constant.PaohzCard;
import com.sy599.game.qipai.xx2710.tool.PaohuziHuLack;
import com.sy599.game.qipai.xx2710.tool.PaohuziTool;

public class PaohuziMingTangRule {

	public static final int LOUDI_MINGTANG_HEIHU = 6;//黑胡 10分                全部黑字胡牌



	/**
	 * 计算普通牌型名堂
	 * @param player 胡牌玩家
	 * @return
	 */
	public static List<Integer> calcMingTang(Xx2710Player player, PaohuziHuLack hu,Xx2710Table table){
		List<Integer> mtList = new ArrayList<>();
		List<PaohzCard> allCards = new ArrayList<>();
		for (CardTypeHuxi type : player.getCardTypes()) {
			allCards.addAll(PaohuziTool.toPhzCards(type.getCardIds()));
		}
		if (hu != null && hu.getPhzHuCards() != null) {
			for (CardTypeHuxi type : hu.getPhzHuCards()) {
				allCards.addAll(PaohuziTool.toPhzCards(type.getCardIds()));
			}
		}
		int redCardCount = findRedPhzs(player,hu);
		if(redCardCount == 0){
			mtList.add(LOUDI_MINGTANG_HEIHU);
		}
		return mtList;
	}


	public static int countFen(List<Integer> mts,Xx2710Table table) {
		if(mts==null||mts.size()==0)
			return 0;

		int fen=0;

	    int mt=0;
		for (int i = 0; i < mts.size(); i++) {
			switch (mts.get(i)){
                case LOUDI_MINGTANG_HEIHU:
                    fen=10;
                    mt=mts.get(i);
                    break;
			}
		}
		mts.clear();
		mts.add(mt);
	
		return fen;
	}








	private static int findRedPhzs(Xx2710Player player,PaohuziHuLack hu){
		List<CardTypeHuxi> allCards=new ArrayList<>();
		if(player!=null)
			allCards.addAll(player.getCardTypes());
		allCards.addAll(hu.getPhzHuCards());
		int count=0;
		for (CardTypeHuxi type : allCards) {
			for (Integer id:type.getCardIds()) {
				if (PaohuziTool.c2710List.contains(PaohzCard.getPaohzCard(id).getVal()%100))
					count++;
			}
			Map<Integer, Integer> bossIdAndVals = type.getIdAndVals();
			if (bossIdAndVals==null||bossIdAndVals.size()==0)
				continue;
			for(Map.Entry<Integer,Integer> entry:type.getIdAndVals().entrySet()){
				if(entry.getKey()>80&&PaohuziTool.c2710List.contains(entry.getValue()%100))
					count++;
			}
		}
		return count;
	}




	public static boolean isHuDieFei(List<Integer> ids){
		int count=0;
		for (int i = 0; i < ids.size(); i++) {
			if(ids.get(i)>80)
				count++;
		}
        return count == 4;
    }

	private static boolean isXiaoYiSe(Xx2710Player player,PaohuziHuLack hu){
		List<CardTypeHuxi> allCards=new ArrayList<>();
		allCards.addAll(player.getCardTypes());
		allCards.addAll(hu.getPhzHuCards());
		for (CardTypeHuxi cardType:allCards) {
			Map<Integer, Integer> idAndVals = cardType.getIdAndVals();
			for (Integer id:cardType.getCardIds()) {
				if(id>40&&id<=80){
					return false;
				}else if(id>80){
					if(idAndVals.get(id)>100)
						return false;
				}
			}
		}
		return true;
	}



	private static boolean isDaYiSe(Xx2710Player player,PaohuziHuLack hu){
		List<CardTypeHuxi> allCards=new ArrayList<>();
		allCards.addAll(player.getCardTypes());
		allCards.addAll(hu.getPhzHuCards());
		for (CardTypeHuxi cardType:allCards) {
			Map<Integer, Integer> idAndVals = cardType.getIdAndVals();
			for (Integer id:cardType.getCardIds()) {
				if(id<=40){
					return false;
				}else if(id>80){
					if(idAndVals.get(id)<100)
						return false;
				}
			}
		}
		return true;
	}





	/**
	 * 该方法不会检测板板胡合法性，只检测当牌型确定后，是否可以胡板板胡
	 * 若牌型只能胡板板胡，不能胡其他任何牌型，则对应的idAndVals为空
	 * @param player
	 * @param hu
	 * @return
	 */
	public static boolean isBbh(Xx2710Player player,PaohuziHuLack hu){
		List<CardTypeHuxi> allCards=new ArrayList<>();
		allCards.addAll(player.getCardTypes());
		allCards.addAll(hu.getPhzHuCards());
		for (CardTypeHuxi cardType:allCards) {
			Map<Integer, Integer> idAndVals = cardType.getIdAndVals();
			if(idAndVals!=null&&idAndVals.size()>0){
				for (Map.Entry<Integer,Integer> entry:idAndVals.entrySet()) {
					if(PaohuziTool.c2710List.contains(entry.getValue()%100))
						return false;
				}
			}
		}
		return true;
	}

	/**
	 * 该方法只能查询是否可胡七对，若有王，还需找出王替代的牌是否符合七对
	 * @param allCards
	 * @return
	 */
	public static boolean isCanQiDui(List<Integer> allCards){
		if(allCards.size()!=14)
			return false;
		Map<Integer,List<Integer>> map=new HashMap<>();
		List<Integer> copy=new ArrayList<>(allCards);
		Iterator<Integer> it = copy.iterator();
		List<Integer> boss=new ArrayList<>();
		while(it.hasNext()){
			Integer id = it.next();
			PaohzCard card = PaohzCard.getPaohzCard(id);
			int val = card.getVal();
			if(val!=0){
				List<Integer> list = map.get(val);
				if(list==null||list.size()==0){
					list=new ArrayList<>();
					list.add(id);
					map.put(val,list);
				}else {
					list.add(id);
				}
				it.remove();
			}else {
				boss.add(id);
			}
		}
		if(map.size()>7)
			return false;
		int i=0;
		for (List<Integer> list : map.values()) {
			if (list.size()!=2&&list.size()!=4){
				if(i<boss.size()){
					list.add(boss.get(i));
					i++;
				}else {
					return false;
				}
			}
		}
		return true;
	}


	/**
	 * 当牌型确定后，是否可以胡七对
	 * @param player
	 * @param hu
	 * @return
	 */
	public static boolean isQiDui(Xx2710Player player,PaohuziHuLack hu){
		List<CardTypeHuxi> allCards=new ArrayList<>();
		allCards.addAll(player.getCardTypes());
		allCards.addAll(hu.getPhzHuCards());
		List<Integer> allVals=new ArrayList<>();
		for (CardTypeHuxi ct:allCards) {
			for (Integer id:ct.getCardIds()) {
				if(id>80){
					allVals.add(ct.getIdAndVals().get(id));
				}else {
					allVals.add(PaohzCard.getPaohzCard(id).getVal());
				}
			}
		}

		Map<Integer,List<Integer>> map=new HashMap<>();
		for (int val:allVals) {
			List<Integer> list = map.get(val);
			if(list==null||list.size()==0){
				list=new ArrayList<>();
				list.add(val);
				map.put(val,list);
			}else {
				list.add(val);
			}
		}
		if(map.size()>7)
			return false;
		for (List<Integer> list : map.values()) {
			if (list.size()!=2&&list.size()!=4)
				return false;
		}
		return true;
	}




	private static boolean isJuJuHong(Xx2710Player player, PaohuziHuLack hu, int juJuHong){
		List<CardTypeHuxi> allCards=new ArrayList<>();
		allCards.addAll(player.getCardTypes());
		allCards.addAll(hu.getPhzHuCards());
		if(allCards!=null&&!allCards.isEmpty()){
			for (CardTypeHuxi type : allCards) {
				int count=0;
				List<PaohzCard> disCards = PaohuziTool.toPhzCards(type.getCardIds());
				List<Integer> list2710=new ArrayList<>(PaohuziTool.c2710List);
				Map<Integer, Integer> bossIdAndVals = type.getIdAndVals();
				for (int i = 0; i < disCards.size(); i++) {
					PaohzCard card = disCards.get(i);
					int v=card.getPai();
					if(v==0)
						v=bossIdAndVals.get(card.getId())%100;
					if (list2710.contains(v)) {
						count++;
					}
				}
				if(juJuHong==1){
					if(type.getCardIds().size()>=3&&count==0){
						return false;
					}else if(type.getCardIds().size()==2&&count!=2)
						return false;
				}else if(juJuHong==2){
					if(type.getCardIds().size()==3&&count!=1){
						return false;
					}else if(type.getCardIds().size()==2&&count!=2)
						return false;
				}
			}
		}
		return true;
	}


	private static boolean isYiKuaiBian(Xx2710Player player,PaohuziHuLack hu){
		int count=0;
		List<CardTypeHuxi> allCards=new ArrayList<>();
		allCards.addAll(player.getCardTypes());
		allCards.addAll(hu.getPhzHuCards());
		if(allCards!=null&&!allCards.isEmpty()){
			for (CardTypeHuxi type : allCards) {
				if (type.getCardIds().size()==2)
					continue;
				List<PaohzCard> disCards = PaohuziTool.toPhzCards(type.getCardIds());
				List<Integer> list2710=new ArrayList<>(PaohuziTool.c2710List);
				Map<Integer, Integer> bossIdAndVals = type.getIdAndVals();
				for (int i = 0; i < disCards.size(); i++) {
					PaohzCard card = disCards.get(i);
					int v=card.getPai();
					if(v==0)
						v=bossIdAndVals.get(card.getId())%100;
					if (list2710.contains(v)) {
						count++;
						list2710.remove((Object)v);
					}
				}
				if(count!=0&&count!=3)
					return false;
				if (count==3)
					return true;
			}
		}
		return false;
	}



}
