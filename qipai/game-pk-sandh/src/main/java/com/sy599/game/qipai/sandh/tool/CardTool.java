package com.sy599.game.qipai.sandh.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.math.RandomUtils;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.sandh.constant.SandhConstants;
import com.sy599.game.qipai.sandh.util.CardType;
import com.sy599.game.qipai.sandh.util.CardUtils;

/**
 * @author lc
 * 
 */
public final class CardTool {
	/**
	 * @param playerCount
	 *            人数
	 * @param playType
	 *            
	 * @param zps
	 * @param chouliu
	 * @return
	 */
	public static List<List<Integer>> fapai(int playerCount, List<List<Integer>> zps, boolean chouliu,boolean quGui) {
		List<List<Integer>> list = new ArrayList<>();
		List<Integer> copy;
		copy = new ArrayList<>(SandhConstants.cardList);
		if(quGui){
			copy = new ArrayList<>(SandhConstants.WuGui_cardList);
		}
		Collections.shuffle(copy);
		if(playerCount == 2) {
			/// 2人欢乐豆模式按照4人发牌
			playerCount = 4;
		}
		//八张底牌
		List<Integer> dipai = new ArrayList<Integer>();
		int dip = 8;
		if (chouliu) {
			int size = copy.size();
			List<Integer> liu = new ArrayList<Integer>();
			for (int i = 0; i < size; i++) {
				int val = CardUtils.loadCardValue(copy.get(i));
				if (val == 6) {
					liu.add(copy.get(i));
				}
			}
			copy.removeAll(liu);
			
			if(playerCount<4){
				dip = 9;
			}
			
		}
		
		
		
		dipai.addAll(copy.subList(0, dip));
		copy = copy.subList(dip,copy.size());
		int maxCount = copy.size() / playerCount;
		List<Integer> pai = new ArrayList<>();
		if (GameServerConfig.isDebug()) {
			if (zps != null && !zps.isEmpty()) {
				List<Integer> pai2 = new ArrayList<>();
				for (List<Integer> zp : zps) {
					pai2.addAll(zp);
				//	list.add(findCardIIds(copy, zp, 0));
				}
				copy = new ArrayList<>(SandhConstants.cardList);
				for(Integer id: pai2){
					copy.remove(id);
				}
				//copy.removeAll(pai2);
				list.addAll(zps);
				list.add(copy);
				
				return list;
				
			}

//			if (list.size() == 3) {
//				return list;
//			}
		}

		for (int i = 0; i < copy.size(); i++) {
			int card = copy.get(i);
			if (pai.size() < maxCount) {
				pai.add(card);
			} else {
				list.add(pai);
				pai = new ArrayList<>();
				pai.add(card);
			}
			
		}
		list.add(pai);
		list.add(dipai);


		
		return list;
	}


	public static List<List<Integer>> fapai(int playerCount, boolean chouliu, List<List<Integer>> zps,boolean quGui) {
		return fapai(playerCount,  zps, chouliu,quGui);
	}
	
	
//	public static int checkFirstCard(List<Integer> hands,List<Integer> list ,int zhuColor,int disColor,boolean isFirst) {
//		
//		
//		
//	}
	

	/***
	 * 检查出的牌花色和类型 两位 10位是花色，个位类型
	 * @param lists
	 * @return
	 */
	public static int checkCardValue(List<Integer> hands,List<Integer> list ,int zhuColor,int disColor,boolean isFirst,boolean chouLiu) {
		
		//一轮首次出牌
		if(isFirst) {
			CardType ct = getCardType(list,zhuColor,chouLiu);
			//甩牌
			if(ct.getType() ==CardType.SHUAIPAI2) {
				//检查其他玩家是否报副
				return -1;
			}
			//副牌不能甩
			if(!allZhu(list, zhuColor)&&ct.getType() >=CardType.SHUAIPAI){
				return -1;
			}
			int card = list.get(0);
			int cardColor =  CardUtils.loadCardColor(card);
			int resColor = 0;
			if(CardUtils.isZhu(card, zhuColor)){
				resColor = zhuColor;//调主
			}else {
				resColor = cardColor;
			}
			
			int res = resColor*10+ct.getType();
			return res;
			
		}
		int color = disColor/10;
		int type = disColor%10;
		
		//调主
		if(color==zhuColor){
			List<Integer> zhuCards = CardUtils.getZhu(hands, zhuColor);
			//如果有主就有限制，无主了随便出
			if(zhuCards.size()>=list.size()){
				if(!allZhu(list, zhuColor)){
					return -1;
				}
				return canChuPai(list, type, zhuCards);
			}else {
				List<Integer> chuZhus = CardUtils.getZhu(list, zhuColor);
				//还有主没出完
				if(chuZhus.size()<zhuCards.size()){
					return -1;
				}
			}
			
		}else {
			//如果有这个花色就有限制，没了随便出
			List<Integer> cards =CardUtils.getColorCards(hands, color);
			if(cards.size()>=list.size()){
				if(!CardUtils.isSameColor(list)||CardUtils.loadCardColor(list.get(0))!=CardUtils.loadCardColor(cards.get(0))){
					return -1;
				}
			}else{
				List<Integer> chuColors =CardUtils.getColorCards(list, color);
				if(chuColors.size()<cards.size()){//出完
					return -1;
				}
			}
			
			return canChuPai(list, type, cards);
			
		}
		
		return 0;

	}


	/**
	 * 是否可以出
	 * @param list
	 * @param type
	 * @param handCards
	 * @return
	 */
	private static int canChuPai(List<Integer> list, int type, List<Integer> handCards) {
		int hDui = CardUtils.hasDuiCount(handCards);
		int cDui = CardUtils.hasDuiCount(list);
		//有对不出对
		if(type==CardType.DUI){
			if(hDui>0&&cDui==0){
				return -1;
			}
		}else if(type ==CardType.TUOLAJI){
			int duiCount = list.size()/2;
			int sDui = hDui>duiCount?duiCount:hDui;
			if(sDui>cDui&&hDui!=100){
				return -1;
			}
		}
		return 0;
	}
	

	public static CardType getCardType(List<Integer> list,int zhuColor,boolean chouLiu) {
		CardType ct;
		if(list.size()==1) {
			ct = new CardType(CardType.DAN, list);
		}else {
			if(list.size()==2){
				ct = new CardType(CardType.DUI, list);
				int color1 = CardUtils.loadCardColor(list.get(0));
				int color2 = CardUtils.loadCardColor(list.get(1));
				if(color1!=color2||!list.get(0).equals(list.get(1))){
					if (allZhu(list, zhuColor)) {
						ct.setType( CardType.SHUAIPAI);
					}else{
						ct.setType( CardType.SHUAIPAI2);
					}
				}
			}else {
				//拖拉机或者甩主 
				 ct = 	CardUtils.isTuoLaji(list,zhuColor,chouLiu);
				 if(ct.getType()==CardType.SHUAIPAI){
					 if (!allZhu(list, zhuColor)) {
						 ct.setType(CardType.SHUAIPAI2);
					 }
				 }
			}
			
		}
		return ct;
	}
	
	
	public static boolean allZhu(List<Integer> list,int zhuColor){
		for(Integer id: list){
			if(!CardUtils.isZhu(id, zhuColor)){
				return false;
			}
		}
		return true;
	}
	
	
	public static boolean allFuPai(List<Integer> list,int zhuColor){
		for(Integer id: list){
			if(CardUtils.isZhu(id, zhuColor)){
				return false;
			}
		}
		return true;
	}
	
	
	
    public  static int getBaofuValue(int seat){
    	int res=0;
    	switch (seat) {
		case 1:
			res =1;
			break;
		case 2:
			res =10;
			break;
		case 3:
			res =100;
			break;
		case 4:
			res =1000;
			break;
		default:
			break;
		}
    	return res;
    }
	
    

    /**
     * 获取叫分是几档
     * @param jiaofen
     * @return
     */
    public static int getDang(int jiaofen){
    	if(jiaofen>50){
    		return 1;
    	}
    	if(jiaofen>30&&jiaofen<=50){
    		return 2;
    	}
    	if(jiaofen<=30){
    		return 3;
    	}
    	return 1;
    }
    
    
    public static CardType getTunWin(HashMap<Integer,CardType> map,int turnFirst,int zhuColor){
    	
    	int winSeat = turnFirst;
    	CardType winType = map.get(winSeat);
    	List<Integer> fenCards = new ArrayList<Integer>();
    	
    	addScore(winType, fenCards);
    	
    	int size = map.size();
    	int nextS = winSeat;
    	for(int i=0;i<size-1;i++) {
    		nextS+=1;
    		if(nextS > size){
    			nextS = 1;
    		}
    		
    		CardType ct = map.get(nextS);
    		addScore(ct, fenCards);
    		List<Integer> cards = ct.getCardIds();
    		int color = CardUtils.loadCardColor(cards.get(0));
    		int card = cards.get(cards.size()-1);
    		int card2 = cards.get(0);
    		
    		if(CardUtils.comCardValue(card2, card, zhuColor)){
    			card= card2;
    		}
    		
    		int winCard = winType.getCardIds().get(winType.getCardIds().size()-1);
    		int winColor =CardUtils.loadCardColor(winType.getCardIds().get(0));
    		//同牌型
    		if(winType.getType()==ct.getType()&&!CardUtils.comCardValue(winCard, card, zhuColor)){
				 winSeat = nextS;
		    	 winType = ct;
			}
    	}
    	CardType result = new CardType(winSeat, fenCards);
    	return result;
    }


	private static void addScore(CardType winType, List<Integer> fenCards) {
		List<Integer> scoreCards = CardUtils.getScoreCards(winType.getCardIds());
		if(scoreCards.size()>0) {
			fenCards.addAll(scoreCards);
		}
	}
    
    

    

    public static List<Integer> loadCards(List<Integer> list,int val){
	    List<Integer> ret = new ArrayList<>(4);
	    for (Integer integer:list){
            if (val==CardUtils.loadCardValue(integer.intValue())){
                ret.add(integer);
            }
        }
        return ret;
    }
    
    

    
    

    public static Map<Integer,Integer> loadCards(List<Integer> list){
        Map<Integer,Integer> map = new TreeMap<>();
        for (Integer integer:list){
            int val = CardUtils.loadCardValue(integer.intValue());
            int count = map.getOrDefault(val,0);
            count++;
            map.put(val,count);
        }
        return map;
    }

	public static List<Integer> findCardIIds(List<Integer> copy, List<Integer> vals, int cardNum) {
		List<Integer> pai = new ArrayList<>();
		if (!vals.isEmpty()) {
			int i = 1;
			for (int zpId : vals) {
				Iterator<Integer> iterator = copy.iterator();
				while (iterator.hasNext()) {
					int card = iterator.next();
					int paiVal=card % 100;
					if (paiVal == zpId) {
						pai.add(card);
						iterator.remove();
						break;
					}
				}
				if (cardNum != 0) {
					if (i >= cardNum) {
						break;
					}
					i++;
				}
			}
		}
		return pai;
	}

    public static void main(String args[]) {
    	int rand = RandomUtils.nextInt(1);
    	for(int i=0;i<30;i++){
    		System.out.println(rand);
    	}
    }

    public static boolean check3(List<List<Integer>> list){

		List<Integer> l=list.get(2);
		for (int a:l){
			if(a==403)
				return true;
		}
		return false;
	}

}
