package com.sy599.game.qipai.tcgd.tool;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.tcgd.constant.TcgdConstants;
import com.sy599.game.qipai.tcgd.util.CardType;
import com.sy599.game.qipai.tcgd.util.CardUtils;
import org.apache.commons.lang.math.RandomUtils;

import java.util.*;

/**
 * @author lc
 * 
 */
public final class CardTool {
	/**
	 * @param playerCount
	 *            人数
	 * @param
	 *            
	 * @param zps
	 * @param chouliu
	 * @return
	 */
	public static List<List<Integer>> fapai(int playerCount, List<List<Integer>> zps, boolean chouliu,boolean chou2) {
		List<List<Integer>> list = new ArrayList<>();
		List<Integer> copy;
	    copy = new ArrayList<>(TcgdConstants.cardList);
		Collections.shuffle(copy);
		//19
		//八张底牌
		List<Integer> dipai = new ArrayList<Integer>();
		int dip = 0;
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
				copy = new ArrayList<>(TcgdConstants.cardList);
				for(Integer id: pai2){
					copy.remove(id);
				}
				//copy.removeAll(pai2);
				list.addAll(zps);
				list.add(copy);
				return list;
			}
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
//		list.add(dipai);

		for(List<Integer>  cards : list){
			System.out.println(" catds ============"+cards);
		}
		
		return list;
	}


	public static List<List<Integer>> fapai(int playerCount, boolean chouliu, List<List<Integer>> zps,boolean chou2) {
		return fapai(playerCount,  zps, chouliu,chou2);
	}
	
	
//	public static int checkFirstCard(List<Integer> hands,List<Integer> list ,int zhuColor,int disColor,boolean isFirst) {
//		
//		
//		
//	}
	

	/***
	 * 检查出的牌花色和类型 两位 10位是花色，个位类型
	 * @param  
	 * @return
	 */
	public static int checkCardValue(List<Integer> hands,List<Integer> list ,int zhuColor,int disColor,boolean isFirst,boolean isChou6) {
		
		//一轮首次出牌
		if(isFirst) {
			CardType ct = getCardType(list,zhuColor,isChou6);
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
	

	public static CardType getCardType(List<Integer> list, int zhuColor,boolean ischou6) {
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
				ct = CardUtils.isTuoLaji(list,zhuColor);
				boolean isShunzi =  CardUtils.isTuoLaji2(list,zhuColor,ischou6);
				if(isShunzi){
					ct.setType(CardType.TUOLAJI);
				}
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

//		int playerCount, List<List<Integer>> zps, boolean chouliu
		List<List<Integer>> zps =new ArrayList<>();
    	fapai(4,zps,false,false);
    	//
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
