package com.sy599.game.qipai.zhz.rule;


import com.sy599.game.qipai.zhz.been.CardTypeHuxi;
import com.sy599.game.qipai.zhz.been.PaohzDisAction;
import com.sy599.game.qipai.zhz.been.ZhzPlayer;
import com.sy599.game.qipai.zhz.been.ZhzTable;
import com.sy599.game.qipai.zhz.constant.PaohzCard;
import com.sy599.game.qipai.zhz.tool.PaohuziHuLack;
import com.sy599.game.qipai.zhz.tool.PaohuziTool;

import java.util.*;

public class PaohuziMingTangRule {

	public static final int LOUDI_MINGTANG_DIANHU = 1;//点胡 10分               只有一张红字
	public static final int LOUDI_MINGTANG_PENGPENGHU = 2;//碰碰胡 20分         牌型同麻将的碰碰胡
	public static final int LOUDI_MINGTANG_XIAOYISE = 3;//小一色 20分           全部是小字胡牌
	public static final int LOUDI_MINGTANG_DAYISE = 4;//大一色 20分             全部是大字胡牌
	public static final int LOUDI_MINGTANG_SHIHONG = 5;//十红 20分              刚好10个红字胡牌
	public static final int LOUDI_MINGTANG_HEIHU = 6;//黑胡 20分                全部黑字胡牌
	public static final int LOUDI_MINGTANG_QIDUI = 7;//七对 20分                全部是对子胡牌
	public static final int LOUDI_MINGTANG_BANBANHU = 8;//板板胡 10分
	public static final int LOUDI_MINGTANG_YIKUANBIAN = 9;//一块匾 10分         手里有且只有一组“二七十”或者“贰柒拾”，没有别的红字
	public static final int LOUDI_MINGTANG_JUJUHONG = 10;//句句红 10分          手里每句话都有一个红字，且将也是一对红字
	public static final int LOUDI_MINGTANG_MANTANGHONG = 11;//满堂红 40分       胡牌的时候都是红字
	public static final int LOUDI_MINGTANG_HUDIEFEI = 12;//蝴蝶飞 20分          手里有4个蝴蝶（王牌），直接胡牌
	public static final int LOUDI_MINGTANG_SIPENDANDIAO = 13;//四碰单吊 40分    4碰后，手里剩一张牌胡牌
	public static final int LOUDI_MINGTANG_SHIERHONG = 14;//十二红 20分         成牌后有12个红字，20分
	public static final int LOUDI_MINGTANG_SHIYIHONG = 15;//十一红 20分         成牌后有11个红字，20分

	/**
	 * 计算普通牌型名堂
	 * @param player 胡牌玩家
	 * @return
	 */
	public static List<Integer> calcMingTang(ZhzPlayer player, PaohuziHuLack hu,ZhzTable table){
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
		}else if(redCardCount==10){
			mtList.add(LOUDI_MINGTANG_SHIHONG);
		}else if (table.getHong11()==1&&redCardCount==11){
			mtList.add(LOUDI_MINGTANG_SHIYIHONG);
		}else if (table.getHong12()==1&&redCardCount==12){
			mtList.add(LOUDI_MINGTANG_SHIERHONG);
		}else if(table.getManTangHong()==1&&redCardCount==14){
			mtList.add(LOUDI_MINGTANG_MANTANGHONG);
		}else if (redCardCount==1){
			mtList.add(LOUDI_MINGTANG_DIANHU);
		}else if (table.getYiKuaiBian()==1&&redCardCount==3&&isYiKuaiBian(player,hu)){
			mtList.add(LOUDI_MINGTANG_YIKUANBIAN);
		}
		if(table.getJuJuHong()>=1&&isJuJuHong(player,hu,table.getJuJuHong())){
			mtList.add(LOUDI_MINGTANG_JUJUHONG);
		}

		if (isXiaoYiSe(player,hu)){
			mtList.add(LOUDI_MINGTANG_XIAOYISE);
		} else if (isDaYiSe(player,hu)){
			mtList.add(LOUDI_MINGTANG_DAYISE);
		}
		checkPenPenHu(player,hu,mtList,table.getSiPengDandiao()==1,table.getPengPengHu()==1);

		if(player.getMt().contains(LOUDI_MINGTANG_QIDUI)&&mtList.size()!=0&&isQiDui(player,hu))
			mtList.add(LOUDI_MINGTANG_QIDUI);
		if(player.getMt().contains(LOUDI_MINGTANG_BANBANHU)&&isBbh(player,hu))
			mtList.add(LOUDI_MINGTANG_BANBANHU);
		if(player.getMt().contains(LOUDI_MINGTANG_HUDIEFEI))
			mtList.add(LOUDI_MINGTANG_HUDIEFEI);
		return mtList;
	}

	/**
	 * 计算七对牌型名堂
	 * @param player 胡牌玩家
	 * @return
	 */
	public static List<Integer> calcMingTangQiDui(ZhzPlayer player, PaohuziHuLack hu,ZhzTable table){
		List<Integer> mtList = hu.getMingTang();
		List<PaohzCard> allCards = new ArrayList<>();
		for (CardTypeHuxi type : hu.getPhzHuCards()) {
			allCards.addAll(PaohuziTool.toPhzCards(type.getCardIds()));
		}

		int redCardCount = findRedPhzs(null,hu);
		if(redCardCount == 0){
			mtList.add(LOUDI_MINGTANG_HEIHU);
		}else if(redCardCount==10){
			mtList.add(LOUDI_MINGTANG_SHIHONG);
		}else if (table.getHong12()==1&&redCardCount==12){
			mtList.add(LOUDI_MINGTANG_SHIERHONG);
		}else if(table.getManTangHong()==1&&redCardCount==14){
			mtList.add(LOUDI_MINGTANG_MANTANGHONG);
		}

		if (isXiaoYiSe(player,hu)){
			mtList.add(LOUDI_MINGTANG_XIAOYISE);
		} else if (isDaYiSe(player,hu)){
			mtList.add(LOUDI_MINGTANG_DAYISE);
		}

		if(player.getMt().contains(LOUDI_MINGTANG_BANBANHU)&&mtList.contains(LOUDI_MINGTANG_HEIHU))
			mtList.add(LOUDI_MINGTANG_BANBANHU);
		return mtList;
	}




	public static int countFen(List<Integer> mts,ZhzTable table) {
		if(mts==null||mts.size()==0)
			return 0;

		int fen=0;
		if(table.getShuangHeFanBei()==1){
			for (int i = 0; i < mts.size(); i++) {
				switch (mts.get(i)){
					case LOUDI_MINGTANG_PENGPENGHU:
					case LOUDI_MINGTANG_XIAOYISE:
					case LOUDI_MINGTANG_DAYISE:
					case LOUDI_MINGTANG_SHIHONG:
					case LOUDI_MINGTANG_HEIHU:
					case LOUDI_MINGTANG_QIDUI:
					case LOUDI_MINGTANG_HUDIEFEI:
					    if(table.getDaHu10Fen()==1){
					        fen+=10;
                        }else {
                            fen+=20;
                        }
						break;
                    case LOUDI_MINGTANG_SIPENDANDIAO:
                        if(table.getDaHu10Fen()==1){
                            fen+=10;
                        }else {
                            fen+=40;
                        }
                        break;
                    case LOUDI_MINGTANG_DIANHU:
                    case LOUDI_MINGTANG_BANBANHU:
                    case LOUDI_MINGTANG_YIKUANBIAN:
                    case LOUDI_MINGTANG_JUJUHONG:
                        fen+=10;
                        break;
                    case LOUDI_MINGTANG_SHIERHONG:
                    case LOUDI_MINGTANG_SHIYIHONG:
                        fen+=20;
                        break;
                    case LOUDI_MINGTANG_MANTANGHONG:
                        fen+=40;
                        break;
				}
			}
		}else {
		    int mt=0;
			for (int i = 0; i < mts.size(); i++) {
				switch (mts.get(i)){
                    case LOUDI_MINGTANG_PENGPENGHU:
                    case LOUDI_MINGTANG_XIAOYISE:
                    case LOUDI_MINGTANG_DAYISE:
                    case LOUDI_MINGTANG_SHIHONG:
                    case LOUDI_MINGTANG_HEIHU:
                    case LOUDI_MINGTANG_QIDUI:
                    case LOUDI_MINGTANG_HUDIEFEI:
                        if(table.getDaHu10Fen()==1){
                            if(fen<10){
                                fen=10;
                                mt=mts.get(i);
                            }
                        }else {
                            if(fen<20){
                                fen=20;
                                mt=mts.get(i);
                            }
                        }
                        break;
                    case LOUDI_MINGTANG_SIPENDANDIAO:
                        if(table.getDaHu10Fen()==1){
                            if(fen<10){
                                fen=10;
                                mt=mts.get(i);
                            }
                        }else{
                            if(fen<40){
                                fen=40;
                                mt=mts.get(i);
                            }
                        }
                        break;
                    case LOUDI_MINGTANG_DIANHU:
                    case LOUDI_MINGTANG_BANBANHU:
                    case LOUDI_MINGTANG_YIKUANBIAN:
                    case LOUDI_MINGTANG_JUJUHONG:
                        if(fen<10){
                            fen=10;
                            mt=mts.get(i);
                        }
                        break;
                    case LOUDI_MINGTANG_SHIERHONG:
                    case LOUDI_MINGTANG_SHIYIHONG:
                        if(fen<20){
                            fen=20;
                            mt=mts.get(i);
                        }
                        break;
                    case LOUDI_MINGTANG_MANTANGHONG:
                        if(fen<40){
                            fen=40;
                            mt=mts.get(i);
                        }
                        break;
				}
			}
			mts.clear();
			mts.add(mt);
		}
		return fen;
	}








	private static int findRedPhzs(ZhzPlayer player,PaohuziHuLack hu){
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

	private static boolean isXiaoYiSe(ZhzPlayer player,PaohuziHuLack hu){
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



	private static boolean isDaYiSe(ZhzPlayer player,PaohuziHuLack hu){
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
	 * 该方法只能查询是否可胡板板胡，若有王，还需找出王替代的牌是否符合板板胡
	 * @param player
	 * @param table
	 * @param allCards
	 * @return
	 */
	public static boolean isCanBbh(ZhzPlayer player,ZhzTable table,List<Integer> allCards){
		if(allCards.size()!=14||table.getBanBanHu()!=1)
			return false;
		if(player.getSeat()==table.getLastWinSeat()){
			if(player.getDisNum()!=0)
				return false;
		}else {
			if(table.getBbhType()==1){
				if(table.getMoNum()!=1)
					return false;
			}else if(table.getBbhType()==2)
				if(table.getMoSeat()!=player.getSeat()||player.getDisNum()!=0||player.getMoNum()>1)
					return false;
		}
		int count=0;
		for (int i = 0; i < allCards.size(); i++) {
			if(allCards.get(i)>80)
				continue;
			List<Integer> list2710=new ArrayList<>(PaohuziTool.c2710List);
			int v=allCards.get(i)%10;
			if(v==0)
				v=10;
			if(list2710.contains(v))
				count++;
		}
        return count == 0;
    }

	/**
	 * 该方法不会检测板板胡合法性，只检测当牌型确定后，是否可以胡板板胡
	 * 若牌型只能胡板板胡，不能胡其他任何牌型，则对应的idAndVals为空
	 * @param player
	 * @param hu
	 * @return
	 */
	public static boolean isBbh(ZhzPlayer player,PaohuziHuLack hu){
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
	public static boolean isQiDui(ZhzPlayer player,PaohuziHuLack hu){
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




	private static boolean isJuJuHong(ZhzPlayer player, PaohuziHuLack hu, int juJuHong){
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

	private static void checkPenPenHu(ZhzPlayer player,PaohuziHuLack hu,List<Integer> mt,boolean siPengDanDiao,boolean pengPengHu){
		int count=0;
		List<CardTypeHuxi> pCards = player.getCardTypes();
		List<CardTypeHuxi> huCards = hu.getPhzHuCards();
		if(pCards!=null&&!pCards.isEmpty()){
			for (CardTypeHuxi pType : pCards) {
				if(pType.getAction()==PaohzDisAction.action_peng)
					count++;
			}
			if(siPengDanDiao&&count==4)
				mt.add(LOUDI_MINGTANG_SIPENDANDIAO);
		}
		for (CardTypeHuxi huType : huCards) {
			if(huType.getAction()==PaohzDisAction.action_peng)
				count++;
		}
		if(pengPengHu&&count==4)
			mt.add(LOUDI_MINGTANG_PENGPENGHU);
	}

	private static boolean isYiKuaiBian(ZhzPlayer player,PaohuziHuLack hu){
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
