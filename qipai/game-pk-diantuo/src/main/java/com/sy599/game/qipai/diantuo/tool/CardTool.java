package com.sy599.game.qipai.diantuo.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang.math.RandomUtils;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.diantuo.constant.DianTuoConstants;
import com.sy599.game.qipai.diantuo.util.CardType;
import com.sy599.game.qipai.diantuo.util.CardUtils;

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
	public static List<List<Integer>> fapai(int playerCount, List<List<Integer>> zps, boolean chou34) {
		List<List<Integer>> list = new ArrayList<>();
		List<Integer> copy;
		copy = new ArrayList<>(DianTuoConstants.cardList);
		Collections.shuffle(copy);
		
		if (chou34) {
			int size = copy.size();
			List<Integer> liu = new ArrayList<Integer>();
			for (int i = 0; i < size; i++) {
				int val = CardUtils.loadCardValue(copy.get(i));
				if (val == 3||val==4) {
					liu.add(copy.get(i));
				}
			}
			copy.removeAll(liu);
		}

		int maxCount = copy.size() / playerCount;
		List<Integer> pai = new ArrayList<>();
		if (GameServerConfig.isDebug()) {
			if (zps != null && !zps.isEmpty()) {
//				List<Integer> pai2 = new ArrayList<>();
//				for (List<Integer> zp : zps) {
//					pai2.addAll(zp);
//				//	list.add(findCardIIds(copy, zp, 0));
//				}
//				copy = new ArrayList<>(DianTuoConstants.cardList);
//				for(Integer id: pai2){
//					copy.remove(id);
//				}
				//copy.removeAll(pai2);
				list.addAll(zps);
				
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
		
		
//		if(i==1){
//			pai.add(501);
//			pai.add(501);
//			pai.add(502);
//			pai.add(502);
//		}
		
		list.add(pai);
//		for(List<Integer>  cards : list){
//			System.out.println(" catds ============"+cards);
//		}
//		
		return list;
	}


	public static List<List<Integer>> fapai(int playerCount, boolean chouliu, List<List<Integer>> zps) {
		return fapai(playerCount,  zps, chouliu);
	}
	
	
//	public static int checkFirstCard(List<Integer> hands,List<Integer> list ,int zhuColor,int disColor,boolean isFirst) {
//		
//		
//		
//	}
	




	/**
	 * 是否可以出
	 * @param list 前一个人出的
	 * @param type
	 * @param handCards
	 * @return
	 */
	public static boolean canChuPai(List<Integer> list, CardType ct,boolean noWang,boolean zhengWSK,boolean shaodai) {

		CardType proCT = getCardType(list,true,true,noWang);
		
		if(ct.getType()==CardType.TIAN_BOOM){
			return true;
		}
		if(proCT.getType()==ct.getType()){
			if(ct.getType()==CardType.BOOM){
				if(proCT.getVal2()<ct.getVal2()){//个数压制
					//四个
					if(ct.getVal2()>4){
						return true;
					}
				}else if(proCT.getVal2()==ct.getVal2()){//炸弹个数
					if(proCT.getVal()<ct.getVal()){
						return true;
					}
				}
			}else if(ct.getType()==CardType.WU_SHI_K){
				if(zhengWSK){
					if(proCT.getVal()<ct.getVal()){//正510K
						return true;
					}
				}else {
					if(proCT.getVal()==0&&proCT.getVal()<ct.getVal()){//正510K
						return true;
					}
				}
				
			}else {
				if(!shaodai){
					if(list.size()!=ct.getCardIds().size()){
						return false;
					}
				}
				if(proCT.getVal()<ct.getVal()){
					return true;
				}
			}
			
		}else  {
			
			if(ct.getType()==CardType.BOOM||ct.getType()==CardType.WU_SHI_K){
				return true;
			}
			
		}
			
		return false;
	}
	

	public static CardType getCardType(List<Integer> list,boolean empty,boolean dashun,boolean noWang) {
		CardType ct = new CardType(0, list);
		if(list.size()==1) {
			ct.setType(CardType.DAN);
			int value = CardUtils.loadCardValue(list.get(0));
			if(value==1||value==2){
				value+=20;
			}
			ct.setVal(value);
			return ct;
		}
		
 		int duiCount = CardUtils.hasDuiCount(list);
		if(duiCount==1){
//			if(list.contains(501)||list.contains(502)){
//				ct.setType(CardType.BOOM);
//			}else{
//				
//			}
			ct.setType(CardType.DUI);
			int value = CardUtils.loadCardValue(list.get(0));
			if(value==1||value==2){
				value+=20;
			}
			ct.setVal(value);
			return ct;
		}
		
		
		
		CardType liandui = isLianDui(list,duiCount);
		if(liandui!=null){
			ct = liandui;
			return ct;
		}
		
		CardType feiji = isFeiJi(list,empty);
		if(feiji!=null){
			ct = feiji;
			return ct;
		}
		
		CardType boom = isBoom(list,noWang);
		if(boom!=null){
			ct = boom;
			return ct;
		}
		CardType san = isSanzhang(list);
		if(san!=null){
			ct = san;
//			ct.setType(san.getType());
//			ct.setVal(san.getVal());
			return ct;
		}
		
		CardType wsk = isWSK(list);
		if(wsk!=null){
			ct = wsk;
			return ct;
		}
		
		CardType shunzi = isShunzi(list);
		if(shunzi!=null){
			ct = shunzi;
			return ct;
		}
		
		return ct;
	}


	private static CardType isLianDui(List<Integer> list, int duiCount) {
		List<Integer> valus = new ArrayList<Integer>();
		for(Integer id: list){
			int value = CardUtils.loadCardValue(id);
			valus.add(value);
		}
		Collections.sort(valus);
		int dui1 = CardUtils.loadCardValue(valus.get(valus.size()-1));
		int dui2 =CardUtils.loadCardValue(valus.get(0));
		
		if(duiCount>1&&(Math.abs(dui1 -dui2)==list.size()/2-1) &&!isContainWang(list)){//连对
			CardType ct = new CardType(CardType.SAN_ZHANG, list);
			ct.setType(CardType.LIAN_DUI);
			ct.setVal(dui1);
			return ct;
		}
		return null;
	}
	
	
	/***
	 * 返回三张里三张的值
	 * @param list
	 * @return
	 */
	public static CardType isSanzhang(List<Integer> list){
		if(list.size()>5){
			return null;
		}
		
		HashMap<Integer, Integer> map = getCardMaps(list);
		if(list.size()==3&&map.size()==1){
			CardType ct = new CardType(CardType.SAN_ZHANG, list);
			int value = CardUtils.loadCardValue(list.get(0));
			ct.setVal(value);
			return ct;
		}
		
		
		if(list.size()==4&&map.size()==2){
			CardType ct = new CardType(CardType.SAN_ZHANG, list);
			for(Entry<Integer, Integer> entry: map.entrySet()){
				int key = entry.getKey();
				int val = entry.getValue();
				if(val>=3){
//					int value = CardUtils.loadCardValue(key);
					ct.setVal(key);
					return ct;
				}
			}
		}
		if(list.size()==5&&map.size()<=3){
			CardType ct = new CardType(CardType.SAN_ZHANG, list);
			for(Entry<Integer, Integer> entry: map.entrySet()){
				int key = entry.getKey();
				int val = entry.getValue();
				if(val>=3){
					//int value = CardUtils.loadCardValue(key);
					ct.setVal(key);
				}
//				else if(val ==2){
//					ct.setType(CardType.SAN_ZHANG_DUI);
//				}
			}
			if(ct.getVal()>0){
				return ct;
			}
		}
		
		return null;
	}


	public static HashMap<Integer, Integer> getCardMaps(List<Integer> list) {
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
		return map;
	}
	
	

	public static List<CardType> getWsk(List<Integer> list,HashMap<Integer, Integer> map) {
		
		List<CardType> ctRes= null;
		int  wCount = map.get(5);
		int sCount =  map.get(10);
		int kCount =  map.get(13);
		if(wCount==0||sCount==0||kCount==0){
			return null;
		}
		
		List<Integer> wids = new ArrayList<Integer>();
		List<Integer> sids = new ArrayList<Integer>();
		List<Integer> kids = new ArrayList<Integer>();
		for(Integer id : list){
			int val = CardUtils.loadCardValue(id);
			if(val ==5){
				wids.add(id);
			}else if(val==10){
				sids.add(id);
			}else if(val==13){
				kids.add(id);
			}
		}
		Collections.sort(wids);
		Collections.sort(sids);
		Collections.sort(kids);
		ctRes = new ArrayList<>();
		for(Integer wid: wids){
			if(sids.isEmpty()||kids.isEmpty()){
				return ctRes;
			}
			List<Integer> clist= new ArrayList<>();
			clist.add(wid);
			int sid = sids.remove(0);
			int kid = kids.remove(0);
			int wcolor = CardUtils.loadCardColor(wid);
			int scolor = CardUtils.loadCardColor(sid);
			int kcolor = CardUtils.loadCardColor(kid);
			CardType ct = new CardType(CardType.WU_SHI_K, list);
			if(kcolor==scolor&& scolor== wcolor){
				ct.setVal(kcolor);//正510k
			}
		}
		
		
		return ctRes;
	
	}
	
	
	
	public static CardType getBoom(List<Integer> list,int val,int count ,HashMap<Integer, Integer> map) {
		CardType ct = null;
		for(Entry<Integer, Integer> entry: map.entrySet()){
			int cVal = entry.getKey();
			int cCount = entry.getValue();
			if(cCount<4){
				continue;
			}
			if(val==0||(count<cCount||(count==cCount&&cVal>val))){
				 ct = new CardType(CardType.BOOM, getCardListByVal(list, cVal));
			}
			if(ct!=null){
				break;
			}
		}
		return ct;
	}
	
	
	private static  List<Integer> getCardListByVal(List<Integer> list,int val){
		List<Integer> cards = new ArrayList<>();
		for(Integer id : list){
			int val2 = CardUtils.loadCardValue(id);
			if(val2==val){
				cards.add(id);
			}
		}
		
		return cards;
		
	}
	
	public static List<Integer> getsanDaiY(List<Integer> list,int val,HashMap<Integer, Integer> map,int dai) {
		List<Integer> cards = new ArrayList<>();
		for(Entry<Integer, Integer> entry: map.entrySet()){
			int cVal = entry.getKey();
			int cCount = entry.getValue();
			if(cCount!=3){
				continue;
			}
			cards.addAll(getCardListByVal(list, cVal));
			break;
		}
		
		List<Integer> cardDais = new ArrayList<>();
		for(Entry<Integer, Integer> entry: map.entrySet()){
			int cVal = entry.getKey();
			int cCount = entry.getValue();
			if(cCount<=2){
				cardDais.addAll(getCardListByVal(list, cVal));
				if(cardDais.size()>dai){
					cardDais.remove(0);
				}
			}
			if(dai<=cardDais.size()){
				break;
			}
			
		}
		cards.addAll(cardDais);
		return cards;
	}
	
	public static List<Integer>  getFeiJi(List<Integer> list,HashMap<Integer, Integer> map,int value,int feijiC,int dai) {
		
		List<Integer> keys = new ArrayList<Integer>();
		for(Entry<Integer, Integer> entry: map.entrySet()){
			int key = entry.getKey();
			int val = entry.getValue();
			if(key==15){
				continue;
			}
			if(val==3){
				keys.add(key);
			}
		}
		if(keys.size()<2){
			return null;
		}
		Collections.sort(keys);
		
		List<Integer> feiJivs = new ArrayList<Integer>();
		for(int i=0;i<keys.size()-1;i++){
			if(keys.get(i+1)-keys.get(i)==1){
				if(!feiJivs.contains(keys.get(i))&&keys.get(i)>value){
					feiJivs.add(keys.get(i));
				}
				if(!feiJivs.contains(keys.get(i+1))&&keys.get(i+1)>value){
					feiJivs.add(keys.get(i+1));
				}
			}
		}
		
		if(feiJivs.size()<feijiC){
			return null;
		}
		
		List<Integer> cards = new ArrayList<>();
		for(int i=0;i<feiJivs.size()-1;i++){
			if(i==feijiC){
				break;
			}
			cards.addAll(getCardListByVal(list, feiJivs.get(i)));
		}
		
		
		List<Integer> cardDais = new ArrayList<>();
		for(Entry<Integer, Integer> entry: map.entrySet()){
			int cVal = entry.getKey();
			int cCount = entry.getValue();
			if(cCount<=2){
				cardDais.addAll(getCardListByVal(list, cVal));
				if(cardDais.size()>dai){
					cardDais.remove(0);
				}
			}
			if(dai<=cardDais.size()){
				break;
			}
			
		}
		
		cards.addAll(cardDais);
		return cards;
	}
	
	
	/***
	 * 飞机
	 * @param list
	 * @return
	 */
	public static CardType isFeiJi(List<Integer> list,boolean shao){
		if(list.size()<6){
			return null;
		}
		
		HashMap<Integer, Integer> map = getCardMaps(list);
		
		
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
		
		if(keys.size()<2||keys.contains(15)){
			return null;
		}
		
		Collections.sort(keys);
		List<Integer> feiJivs = new ArrayList<Integer>();
		boolean feiji = false;
		for(int i=0;i<keys.size()-1;i++){
			if(keys.get(i+1)-keys.get(i)==1){
				if(!feiJivs.contains(keys.get(i))){
					feiJivs.add(keys.get(i));
				}
				if(!feiJivs.contains(keys.get(i+1))){
					feiJivs.add(keys.get(i+1));
				}
				feiji = true;
				break;
			}
		}
		
		if(feiji){
//			int feijiCount = list.size() - keys.size()*3;
//			if(feijiCount>0&&feijiCount!=keys.size()&&feijiCount!=keys.size()*2){
//				if(!shao){
//					return null;
//				}
//			}
			
			CardType ct = new CardType(CardType.FEI_JI, list);
			int value = CardUtils.loadCardValue(feiJivs.get(feiJivs.size()-1));
			ct.setVal(value);
			ct.setVal2(feiJivs.size());//几飞
			return ct;
		}
		return null;
	}
	
	
	/***
	 * 炸弹
	 * @param list
	 * @return
	 */
	public static CardType isBoom(List<Integer> list,boolean noWang){
		if(list.size()<4){
			return null;
		}
		
		HashSet<Integer> set = new HashSet<Integer>();
		int wangCount =0; 
		for(Integer id: list){
			int value = CardUtils.loadCardValue(id);
			if(value==1||value==2){
				wangCount+=1;
				continue;
			}
				set.add(value);
		}
		
	
		
		if(set.size()==0&&list.size()==4){
			CardType ct = new CardType(CardType.TIAN_BOOM, list);
			return ct;
		}
		if(noWang&&wangCount>0){
			return null;
		}
		if(set.size()==1&&list.size()-wangCount>=4){
			CardType ct = new CardType(CardType.BOOM, list);
			int value = CardUtils.loadCardValue(list.get(0));
			ct.setVal(value);
			ct.setVal2(list.size());
			return ct;
		}
		
		return null;
	}
	public static CardType isWSK(List<Integer> list){
		if(list.size()!=3){
			return null;
		}
		HashSet<Integer> set = new HashSet<Integer>();
		HashSet<Integer> colorSet = new HashSet<Integer>();
		for(Integer id: list){
			int value = CardUtils.loadCardValue(id);
			int color = CardUtils.loadCardColor(id);
			if(value==13||value==5||value==10) {
				set.add(value);
	    	}
			colorSet.add(color);
		}
	if(set.size()==3){
		CardType ct = new CardType(CardType.WU_SHI_K, list);
		if(colorSet.size()==1){
			int color = CardUtils.loadCardColor(list.get(0));
			ct.setVal(color);//正510k
		}
		return ct;
	}	
		return null;
	}
	
	
	
	
	
	

	public static CardType isShunzi(List<Integer> list){
		if(list.size()<5){
			return null;
		}
		
		if(isContainWang(list)){
			return null;
		}
		List<Integer> valus = new ArrayList<Integer>();
		
		for(Integer id: list){
			int value = CardUtils.loadCardValue(id);
			valus.add(value);
		}
	
		Collections.sort(valus);
		
		
		
		for(int i=0;i<valus.size()-1;i++) {
			if(Math.abs(valus.get(i) -valus.get(1+i))!=1){
				return null;
			}
		}
		
		CardType ct = new CardType(CardType.SHUNZI, list);
		ct.setVal(valus.get(valus.size()-1));
		
		return ct;
	}
	
	
	
	
	
	
	
	
	
	
	
	private static boolean isContainWang(List<Integer> list){
		
		for(Integer id: list){
			int value = CardUtils.loadCardValue(id);
			if(value==15||value==1||value==2) {
	    		return true;
	    	}
		}
		return false;
		
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
