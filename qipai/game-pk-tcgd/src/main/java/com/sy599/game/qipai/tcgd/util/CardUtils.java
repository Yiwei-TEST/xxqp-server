package com.sy599.game.qipai.tcgd.util;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import com.sy599.game.qipai.tcgd.bean.TcgdPlayer;

import java.util.*;


public final class CardUtils {

    public static final CardValue EMPTY_CARD_VALUE = new CardValue(0, 0,0);
    public static final List<CardValue> EMPTY_CARD_VALUE_LIST = Arrays.asList(EMPTY_CARD_VALUE);
    public static List<Integer>  cardList = new ArrayList<>();
    static {
        for (int i = 1; i <= 4; i++) {
            for (int j = 3; j <= 15; j++) {
                //
                int card = i * 100 + j;
                cardList.add(card);
            }
        }
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
    
    
    
    public static List<Integer> getScoreCards(List<Integer> cardIds) {
        List<Integer> scoreCards = new ArrayList<Integer>();
        
        for (Integer id : cardIds) {
        	int val = loadCardValue(id);
            if (val == 5 || val == 10) {
                scoreCards.add(id);
            } else if (val == 13) {
            	  scoreCards.add(id);
            }
        }
        return scoreCards;
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
        return new CardValue(card, loadCardValue(card),loadCardColor(card));
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
    
    public static boolean isYingZhu(int card){
    	int value = loadCardValue(card);
    	
    	if(value==10||value==15||value==1||value==2) {
    		return true;
    	}
    	return false;
    }
    
    
    public static boolean isZhu(int card,int zhuColor){
    	int value = loadCardValue(card);
    	int color =loadCardColor(card);
    	if(value==10||value==15||value==1||value==2) {
    		return true;
    	}
    	
    	if(color==zhuColor){
    		return true;
    	}
    	return false;
    }
    
    
    
    public static boolean comCardValue(int card1,int card2,int zhuColor){
    	int color1 = loadCardColor(card1);
    	int color2 = loadCardColor(card2);
    	
    	int value1 = loadCardValue(card1);
    	int value2 = loadCardValue(card2);
    	
    	
    	value1 = changeCardValue(value1);
    	value2 = changeCardValue(value2);
    	
    	//同花色
    	if(color1==color2){
    		if(value1>=value2){
        		return true;
    		}
    	}else {
    		if(zhuColor>0){
    			//第一个是主牌,第二个如果不是硬主第一个大，如果第二个是硬主那比较大小即可
    			if(isZhu(card1, zhuColor)){
    				if(!isYingZhu(card2)||value1>value2){
    					return true;
    				}else if(value1==value2){//两个都是硬主谁是正的谁大,都是副的前面的大
    					if(color1 == zhuColor||color2!=zhuColor) {
    						return true;
    					}
    				}
        		}else{
        			//如果两个都不是主，那前面的大
        			if(!isZhu(card2, zhuColor)){
        				return true;
        			}
        		}
    		}else {
    			//无主 副牌 不同花色前面的大
    			if(value1<15&&value2<15){
    				return true;
    			}else if(value1>=value2){//一旦有硬主比较大小即可
    				return true;
    			}
    			
    		}
    	}
    	return false;
    }
    
    
    
    private static int changeCardValue(int value){
    	if(value==10){
    		value+=10;
    	}
    	if(value==1||value==2){
    		value +=20;
    	}
    	return value;
    	
    }

    /**
     * 计算牌花色方片 1 梅花2 洪涛3 黑桃4  王5
     *
     * @param card
     * @return
     */
    public static int loadCardColor(int card) {
        return card / 100;
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

    public static void sortCards(List<Integer> cards) {
        Collections.sort(cards, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                int val1 = loadCardValue(o1);
                val1 = changeCardValue(val1);
                int val2 = loadCardValue(o2);
                val2 = changeCardValue(val2);
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
    
    
    
    /***
     * 拖拉机
     * @param cards
     */
    public static CardType  isTuoLaji(List<Integer> cards,int zhuColor) {
        CardType ct = new CardType(0, cards);
        if(cards.size()%2!=0) {
            ct.setType(CardType.SHUAIPAI);
            return ct;
        }
        CardUtils.sortCards(cards);
        List<Integer> newCardIds = new LinkedList<Integer>();
        newCardIds.addAll(cards);

        //是否全是对子len / 2 - 1
        if(hasDuiCount(newCardIds)==100&&(newCardIds.get(newCardIds.size()-1) -newCardIds.get(0)==newCardIds.size()/2-1)&&(isSameColor(cards)&&!isContainsYZ(cards))){//连对 拖拉机
            ct.setType(CardType.TUOLAJI);
            ct.setCardIds(newCardIds);
        }else {
            int dui = hasDuiCount(newCardIds);
            if(dui == 100) {//全是对子
                List<Integer> proList = new ArrayList<Integer>();
                for(int i=0;i<newCardIds.size()-1;i++) {
                    int a = newCardIds.get(i);
                    int b = newCardIds.get(i+1);
                    int val1 = getCardPro(a, zhuColor);
                    int val2 = getCardPro(b, zhuColor);
                    //相邻的两对不是连着的 不是拖拉机
                    if(a!=b){//&&(Math.abs(val2-val1)!=1)
                        if(!proList.contains(val1)){
                            proList.add(val1);
                        }
                        if(!proList.contains(val2)){
                            proList.add(val2);
                        }
//
//						break;
                    }
                }
                Collections.sort(proList);
                if(proList.size()<=1){
                    ct.setType(CardType.SHUAI_LIAN_DUI);
                }else {
                    for(int i=0;i<proList.size()-1;i++){
                        if(Math.abs(proList.get(i)-proList.get(i+1))!=1){
                            ct.setType(CardType.SHUAI_LIAN_DUI);
                            break;
                        }
                    }
                }



            }else {
                ct.setType(CardType.SHUAIPAI);
            }
        }

        //拖拉机
        if(ct.getType()==0){
            ct.setCardIds(newCardIds);
            ct.setType(CardType.TUOLAJI);
            System.out.println("tuolaji...................................... cards = " + newCardIds);
        }

        return ct;

    }
    public static Boolean  isTuoLaji2(List<Integer> cards,int zhuColor,boolean ischou6) {
        CardType ct = new CardType(0, cards);
        if(cards.size()%2!=0) {
            ct.setType(CardType.SHUAIPAI);
            return false;
        }
        if(!ischou6){
            return false;
        }
        List<Integer> cpcards = new ArrayList<>();
        for (int num:cards){
            if(loadCardValue(num)==5){
                cpcards.add(num+1);
            }else{
                cpcards.add(num);
            }
        }

        CardUtils.sortCards(cpcards);
        List<Integer> newCardIds = new LinkedList<Integer>();
        newCardIds.addAll(cpcards);

        //是否全是对子len / 2 - 1
        if(hasDuiCount(newCardIds)==100&&(newCardIds.get(newCardIds.size()-1) -newCardIds.get(0)==newCardIds.size()/2-1)&&(isSameColor(cards)&&!isContainsYZ(cards))){//连对 拖拉机
            ct.setType(CardType.TUOLAJI);
            ct.setCardIds(newCardIds);
        }else {
            int dui = hasDuiCount(newCardIds);
            if(dui == 100) {//全是对子
                List<Integer> proList = new ArrayList<Integer>();
                for(int i=0;i<newCardIds.size()-1;i++) {
                    int a = newCardIds.get(i);
                    int b = newCardIds.get(i+1);
                    int val1 = getCardPro2(a, zhuColor);
                    int val2 = getCardPro2(b, zhuColor);
                    //相邻的两对不是连着的 不是拖拉机
                    if(a!=b){//&&(Math.abs(val2-val1)!=1)
                        if(!proList.contains(val1)){
                            proList.add(val1);
                        }
                        if(!proList.contains(val2)){
                            proList.add(val2);
                        }
//						break;
                    }
                }
                Collections.sort(proList);
                if(proList.size()<=1){
                    ct.setType(CardType.SHUAI_LIAN_DUI);
                }else {
                    for(int i=0;i<proList.size()-1;i++){
                        if(Math.abs(proList.get(i)-proList.get(i+1))!=1){
                            ct.setType(CardType.SHUAI_LIAN_DUI);
                            break;
                        }
                    }
                }
//                for(int i=0;i<proList.size()-1;i++){
//                    if(Math.abs(proList.get(i)-proList.get(i+1))!=1){
//                        ct.setType(CardType.SHUAI_LIAN_DUI);
//                        break;
//                    }
//                }




            }else {
                ct.setType(CardType.SHUAIPAI);
            }
        }

        //拖拉机
        if(ct.getType()==0){
            ct.setCardIds(newCardIds);
            ct.setType(CardType.TUOLAJI);
            System.out.println("tuolaji...................................... cards = " + newCardIds);
        }
        if(ct.getType()==3){
            return true;
        }else{
            return false;
        }
    }
    public static boolean isContainsYZ(List<Integer> cards){
        for(Integer card: cards){
            boolean yz = isYingZhu(card);
            if(yz){
                return true;
            }

        }
        return false;
    }
    private static int getCardPro(int card,int zhuColor){
    	
    	int val = loadCardValue(card);
    	int color = loadCardColor(card);
    	
    	int priority = 0;
    	if(val==2){
    		priority = 1;
    	}else if(val==1){
    		priority = 2;
    	}else if(val==10&&color==zhuColor){
    		priority=3;
    	}else if(val==10){
    		priority=4;
    	}else if(val==15&&color==zhuColor){
    		priority = 5;
    	}else if(val==15){
    		priority = 6;
    	}else {//if(color==zhuColor)
    		priority = 21-val;//21-5=16 //21-7=14
    		if(val==6||val==5 ||val==7||val==8 ||val==9){
    			priority-=1;
    		}
//            if(val==6||val==5 ){
//    			priority-=1;
//    		}
    	}
    	return priority;
    	
    }
    private static int getCardPro2(int card,int zhuColor){

        int val = loadCardValue(card);
        int color = loadCardColor(card);

        int priority = 0;
        if(val==2){
            priority = 1;
        }else if(val==1){
            priority = 2;
        }
//        else if(val==7&&color==zhuColor){
//            priority=3;
//        }else if(val==7){
//            priority=4;
//        }
        else if(val==15&&color==zhuColor){
            priority = 5;
        }else if(val==15){
            priority = 6;
        }else {//if(color==zhuColor)
            priority = 21-val;
            if(val==6||val==5){
                priority-=1;
            }

        }
        return priority;

    }


    /**
	 * 获取主牌
	 * @param hands
	 * @param zhuColor
	 * @return
	 */
    public static List<Integer> getZhu(List<Integer> hands,int zhuColor){
		List<Integer> res = new ArrayList<Integer>();
		
		for(Integer card: hands){
			if(CardUtils.isZhu(card, zhuColor)){
				res.add(card);
			}
		}
		return res;
	}
	
	
	/**
	 * 获取花色牌
	 * @param hands
	 * @param color
	 * @return
	 */
	public static List<Integer> getColorCards(List<Integer> hands,int color){
		List<Integer> res = new ArrayList<Integer>();
		for(Integer card: hands){
			int cardColor = CardUtils.loadCardColor(card);
			if(!CardUtils.isYingZhu(card)&&cardColor==color){
				res.add(card);
			}
		}
		return res;
	}
	
	
	public static List<Integer> getDuiCards(List<Integer> hands,int count){
		HashSet<Integer> set = new HashSet<Integer>();
		List<Integer> duis = new ArrayList<Integer>();
		int dui=0;
		for(Integer card: hands){
			if(!set.contains(card)){
				set.add(card);
			}else{
				dui++;
				duis.add(card);
				duis.add(card);
				if(dui==count){
					break;
				}
			}
		}
		return duis;
	}
	
	
	
	/**
	 * 是否同花色
	 * @param
	 * @param
	 * @return
	 */
	public static boolean isSameColor(List<Integer> cards){
		int color =-1;
		for(Integer card: cards){
			int cardColor = CardUtils.loadCardColor(card);
			if(color==-1){
				color =cardColor;
			}else if(cardColor!=color){
				return false;
			}
		}
		return true;
	}
    public static boolean isSameColorRed(List<Integer> cards){
	    boolean result = true;
	    //42 13
        for (int card: cards) {
           if( loadCardColor(card)==1 || loadCardColor(card)==3){
               continue;
           }else{
              result = false;
              break;
           }
        }
        return result;
    }
    public static boolean isSameColorBlack(List<Integer> cards){
        boolean result = true;
        //42 13
        for (int card: cards) {
            if( loadCardColor(card)==2 || loadCardColor(card)==4){
                continue;
            }else{
                result = false;
                break;
            }
        }
        return result;
    }

	public static int hasDuiCount(List<Integer> hands){
		HashSet<Integer> set = new HashSet<Integer>();
		int dui=0;
		for(Integer card: hands){
			if(!set.contains(card)){
				set.add(card);
			}else{
				dui++;
			}
		}
		int size = set.size();
		if(size>1&&size*2==hands.size()) {
			//全是对子
			dui = 100;
		}
		
		return dui;
	}


    /**
     * 比较反主
     */
    public static boolean compareFanzhuPai(String jiaozhuCardStr , String fzpai, TcgdPlayer player){
        if("".equals(jiaozhuCardStr)){
            return true;
        }else{
            HashMap<String,String>  map = getMinFanZhuPai(player.getHandPais());
            int jz = toIntResult(jiaozhuCardStr);//
            if(jz==0){
                jz=1;
            }
            int minLV= 0;
            for (int i=jz+1;i<=11;i++){
                 if( null==map.get(String.valueOf(i)) || "".equals(map.get(String.valueOf(i)))){
                     continue;
                 }else{
                     minLV = i;
                     break;
                 }
            }
            int p = toIntResult(fzpai);
            if(p == minLV){
                return true;
            }else{
                player.writeErrMsg("反主只能从小到大逐步反主！");
                return false;
            }
//            if(p>jz){
//                return true;
//            }else{
//                return false;
//            }
        }
    }

    public static int selColor(String pai){
        int r = 0;
        for(String str :pai.split(",")) {
            if (loadCardValue(Integer.valueOf(str)) == 10) {
                r =  loadCardColor(Integer.valueOf(str));
                break;
            }
        }
        return r;
    }


    /**
     * 获取选主牌层级
     * @param pai 牌
     * @return
     */
    public  static int toIntResult(String pai) {
        List<Integer> list = new ArrayList<>();
        int pai10=0;
        int pai2=0;
        int paiwang=0;
        for(String str :pai.split(",")){
            list.add(Integer.valueOf(str));
            if(loadCardValue(Integer.valueOf(str))==10){
                pai10++;
            }
            if(loadCardValue(Integer.valueOf(str))==15){
                pai2++;
            }
            if(loadCardValue(Integer.valueOf(str))==1 ||loadCardValue(Integer.valueOf(str))==2){
                paiwang++;
            }
        }
        boolean issamecolor = isSameColorBlack(list) || isSameColorRed(list);
        int lv =0;
        if(list.size()==1 && pai10==1){
            lv =0;
        }
        if((pai.contains("315")|| pai.contains("115")) && (pai.contains("110")|| pai.contains("210")|| pai.contains("310")|| pai.contains("410")) && list.size() ==2){
            lv =1;// 1抢主
        }else if(issamecolor && pai10==2 && list.size() ==2 && (list.get(0).equals(list.get(1))) && loadCardColor(list.get(0))==1){
            lv =2;//反主一对10 方块
        }else if(issamecolor && pai10==2 && list.size() ==2 && (list.get(0).equals(list.get(1))) && loadCardColor(list.get(0))==2){
            lv =3;//反主一对10 梅花
        }else if(issamecolor && pai10==2 && list.size() ==2 && (list.get(0).equals(list.get(1))) && loadCardColor(list.get(0))==3){
            lv =4;//反主一对10 红桃
        }else if(issamecolor && pai10==2 && list.size() ==2 && (list.get(0).equals(list.get(1))) && loadCardColor(list.get(0))==4){
            lv =5;//反主一对10 黑桃
        }else if( pai2 ==3 && list.size() ==3 && isSameColorRed(list)){
            lv =6;//三个同色红2反
        }else if( pai2 ==3 && list.size() ==3 && isSameColorBlack(list)){
            lv =7;//三个同色黑2反
        }else if(issamecolor && pai2 ==4 && list.size() ==4 && isSameColorRed(list)){
            lv =8;//4个同色红2反
        }else if(issamecolor && pai2 ==4 && list.size() ==4 && isSameColorBlack(list)){
            lv =9;//4个同色黑2反
        }else if(paiwang == 3 && list.size() ==3){
            lv =10;//3王反
        }else if(paiwang == 4 && list.size() ==4){
            lv =11;// 4王反
        }
        return lv;
    }

    public static  HashMap<String,String> getMinFanZhuPai(List<Integer> handcard){
        List<Integer> heitao10 = new ArrayList<>();
        List<Integer> hongtao10 = new ArrayList<>();
        List<Integer> meihua10 = new ArrayList<>();
        List<Integer> fangkuai10 = new ArrayList<>();
        List<Integer> b2card = new ArrayList<>();
        List<Integer> r2card = new ArrayList<>();
        List<Integer> wcard = new ArrayList<>();
        for (int pai : handcard) {
            if(loadCardValue(pai)==10 && loadCardColor(pai)==4){
                heitao10.add(pai);
            }
            if(loadCardValue(pai)==10 && loadCardColor(pai)==3){
                hongtao10.add(pai);
            }
            if(loadCardValue(pai)==10 && loadCardColor(pai)==2){
                meihua10.add(pai);
            }
            if(loadCardValue(pai)==10 && loadCardColor(pai)==1){
                fangkuai10.add(pai);
            }
            if(loadCardValue(pai)==15 && (loadCardColor(pai)==1|| loadCardColor(pai)==3)){
                r2card.add(pai);
            }
            if(loadCardValue(pai)==15 && (loadCardColor(pai)==2|| loadCardColor(pai)==4)){
                b2card.add(pai);
            }
            if(loadCardValue(pai)==1 ||loadCardValue(pai)==2){
                wcard.add(pai);
            }
        }
        HashMap<String,String> tip = new HashMap<>();

        if(fangkuai10.size()==2){
            tip.put("2",fangkuai10.toString());
        }
        if(meihua10.size()==2){
            tip.put("3",meihua10.toString());
        }
        if(hongtao10.size()==2){
            tip.put("4",hongtao10.toString());
        }
        if(heitao10.size()==2){
            tip.put("5",heitao10.toString());
        }
        if(r2card.size()>=3){
            tip.put("6",r2card.subList(0,3).toString());
        }
        if(b2card.size()>=3){
            tip.put("7",b2card.subList(0,3).toString());
        }
        if(r2card.size()==4){
            tip.put("8",r2card.toString());
        }
        if(b2card.size()==4){
            tip.put("9",b2card.toString());
        }
        if(wcard.size()>=3){
            tip.put("10",wcard.subList(0,3).toString());
        }
        if(wcard.size()==4){
            tip.put("11",wcard.toString());
        }
        return tip;
    }
//    public static int getFanzhuTip(List<Integer> cards){
//        int pai10=0;
//        int pai2=0;
//        int paiwang=0;
//        boolean iscontainDui10 = false;
//        boolean iscontain3Red2= false;
//        boolean iscontain3Blanc2 = false;
//        boolean iscontain4red2 = false;
//        boolean iscontain4b2 = false;
//        boolean iscontain3w = false;
//        boolean iscontain4w = false;
//        List<Integer> list = new ArrayList<>();
//        for(int str :cards){
//            list.add(Integer.valueOf(str));
//            if(loadCardValue(Integer.valueOf(str))==10){
//                pai10++;
//            }
//            if(loadCardValue(Integer.valueOf(str))==15){
//                pai2++;
//            }
//            if(loadCardValue(Integer.valueOf(str))==1 ||loadCardValue(Integer.valueOf(str))==2){
//                paiwang++;
//            }
//        }
//    }
    public static int getSelTeamCardNum(List<Integer>  ls,int card){
        int num=0;
        for (int pai: ls ) {
            if(pai==card){
                num++;
            }
        }
        return num;
    }

    /**
     *
     * @param sjp 当前桌牌
     * @param px 当前桌牌牌型
     * @param zjp  下家手牌
     * @return
     */
    public static boolean getTs(List<Integer> sjp, int px, List<Integer> zjp) {
        boolean result = true;
        if(null==sjp || sjp.isEmpty()){
            return false;
        }
        if(null==zjp || zjp.isEmpty()){
            return false;
        }
        List<List<String>> re =new ArrayList<>();
        //  paixAction 11单 12=对子 13=顺 14=连对 15=三张 16= 三代一 17=飞机 20=炸 23同花顺 30=天炸
        if(px==11){
             String cptype ="danz";
             String zjpa = TcgdSfNew.listToString(TcgdSfNew.intCardToStringCard(zjp));
             re = TcgdUtil.getAllTs(zjpa,re,cptype,TcgdSfNew.intCardToStringCard(sjp));
             if(re.isEmpty()){
                 return false;
             }
        } else if(12==px){//12=对子
            String cptype ="duiz";
            String zjpa = TcgdSfNew.listToString(TcgdSfNew.intCardToStringCard(zjp));
            re = TcgdUtil.getAllTs(zjpa,re,cptype,TcgdSfNew.intCardToStringCard(sjp));
            if(re.isEmpty()){
                return false;
            }
        }
        else if(13==px){
            String cptype ="shunzi";// 13=顺
             String zjpa = TcgdSfNew.listToString(TcgdSfNew.intCardToStringCard(zjp));
            re = TcgdUtil.getAllTs(zjpa,re,cptype,TcgdSfNew.intCardToStringCard(sjp));
            if(re.isEmpty()){
                return false;
            }
        }else if(14==px){
            String cptype ="ld";// 14=连对
              String zjpa = TcgdSfNew.listToString(TcgdSfNew.intCardToStringCard(zjp));
            re = TcgdUtil.getAllTs(zjpa,re,cptype,TcgdSfNew.intCardToStringCard(sjp));
            if(re.isEmpty()){
                return false;
            }
        }
        else if(15==px){//15=三张
            String cptype ="3z";
               String zjpa = TcgdSfNew.listToString(TcgdSfNew.intCardToStringCard(zjp));
            re = TcgdUtil.getAllTs(zjpa,re,cptype,TcgdSfNew.intCardToStringCard(sjp));
            if(re.isEmpty()){
                return false;
            }
        }else if(16==px){
            String cptype ="sandaidui";// 16= 三代对
            String zjpa = TcgdSfNew.listToString(TcgdSfNew.intCardToStringCard(zjp));
            re = TcgdUtil.getAllTs(zjpa,re,cptype,TcgdSfNew.intCardToStringCard(sjp));
            if(re.isEmpty()){
                return false;
            }
        }else if(17==px){
            String cptype ="feiji";//  17=飞机
             String zjpa = TcgdSfNew.listToString(TcgdSfNew.intCardToStringCard(zjp));
            re = TcgdUtil.getAllTs(zjpa,re,cptype,TcgdSfNew.intCardToStringCard(sjp));
            if(re.isEmpty()){
                return false;
            }
        }else if(20==px){
            String cptype =sjp.size()+"zha";//20=炸
              String zjpa = TcgdSfNew.listToString(TcgdSfNew.intCardToStringCard(zjp));
            re = TcgdUtil.getAllTs(zjpa,re,cptype,TcgdSfNew.intCardToStringCard(sjp));
            if(re.isEmpty()){
                return false;
            }
        }else if(23==px){
            String cptype ="ths";//  23同花顺
             String zjpa = TcgdSfNew.listToString(TcgdSfNew.intCardToStringCard(zjp));
            re = TcgdUtil.getAllTs(zjpa,re,cptype,TcgdSfNew.intCardToStringCard(sjp));
            if(re.isEmpty()){
                return false;
            }
        }else if(16==px){
             //  30=天炸
            return false;

        }
        System.out.println("====提示："+re);
        if(re.isEmpty()){
            return false;
        }
        return result;
    }

    /**
     * 包含癞子牌的提示。
     * @param sjp
     * @param px
     * @param zjp
     * @return
     */
    public static boolean getTs1(List<Integer> sjp, int px, List<Integer> zjp){
        List<Integer> zjp2 = new ArrayList<>(zjp);
        boolean result = getTs(sjp,px,zjp2);
        if(result){
            return true;
        }
        if(zjp2.contains(315)){
           int index =zjp2.lastIndexOf(315);
            for (int p:cardList) {
                zjp2.set(index,p);
                  result = getTs(sjp,px,zjp2);
                if(result){
                    return true;
                }
            }
            if(zjp2.contains(315)){
                int index2 =zjp2.lastIndexOf(315);
                for (int p:cardList) {
                    zjp2.set(index2,p);
                    result = getTs(sjp,px,zjp2);
                    if(result){
                        return true;
                    }
                }
            }
        }
        return  false;
    }
//     11单 12=对子 13=顺 14=连对 15=三张 16=三带对 17=飞机 20=炸 23同花顺 30=天炸
    public static void main(String[] args) {
        long A = System.currentTimeMillis();
        testLaizi();
        long AB= System.currentTimeMillis();
        System.out.println("AB-A :"+(AB-A));
	}
    public static void testLaizi(){
        List<Integer> newCardIds = new LinkedList<Integer>();
        newCardIds.add(113);
        newCardIds.add(112);
        newCardIds.add(111);
        newCardIds.add(110);
        newCardIds.add(109);

        String zjp ="213,313,413,213,313,413,211,404,404,304,304,403,403,502";
        List<Integer> zjhp = new ArrayList<>();
        List<String> stringList = new ArrayList<>();
        for (String p :  zjp.split(",")) {
            zjhp.add(Integer.valueOf(p));
        }

        System.out.println( getTs(newCardIds, 23,zjhp));
    }
    public static void testfeiji(){
        String[] pai = new String[]{"R15","R15","M15","R14","R14","M14"};
       TcgdSfNew.isFeiJi(pai);
    }
    public static void testdan(){
        List<Integer> newCardIds = new LinkedList<Integer>();
        newCardIds.add(415);
        String zjp ="501,502,206,207,407,307,204,403,303";
        List<Integer> zjhp = new ArrayList<>();
        List<String> stringList = new ArrayList<>();
        for (String p :  zjp.split(",")) {
            zjhp.add(Integer.valueOf(p));
        }
        getTs(newCardIds,11,zjhp);
    }
    public static void testShunzi(){
        List<Integer> newCardIds = new LinkedList<Integer>();
        newCardIds.add(414);
        newCardIds.add(215);
        newCardIds.add(205);
        newCardIds.add(304);
        newCardIds.add(303);
        // paixAction 11单 12=对子 13=顺 14=连对 15=三张 16= 三代一dui 17=飞机 20=炸 23同花顺 30=天炸
        String zjp ="501,502,206,207,407,307,204,403,303";
        List<Integer> zjhp = new ArrayList<>();
        List<String> stringList = new ArrayList<>();
        for (String p :  zjp.split(",")) {
            zjhp.add(Integer.valueOf(p));
        }
        getTs(newCardIds,13,zjhp);
    }

    /**
     * 组合牌结果
     */
    public static class Result implements Comparable<Result> {
        /**
         * 0无效，1单张，2对子，3三飘，4四带，11单顺子，22双顺子，33飞机，100炸弹，1000：3条A
         */
        private final int type;
        private final int count;
        private final int max;

        public Result(int type, int count, int max) {
            this.type = type;
            this.count = count;
            this.max = max;
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

        @Override
        public String toString() {
            return new StringBuilder(32).append("{type=").append(type).append(",count=").append(count).append(",max=").append(max).append("}").toString();
        }

        /**
         * -100不能比较，大：正数，小：负数，相等：零
         *
         * @param o
         * @return
         */
        @Override
        public int compareTo(Result o) {
            if (this.type <= 0 || o.type <= 0) {
                return -100;
            } else if (this.type == 1000) {
                return 1;
            } else if (o.type == 1000) {
                return -1;
            } else if (this.type == o.type) {
                switch (this.type) {
                    case 100:
                        if (this.count > o.count) {
                            return 1;
                        } else if (this.count < o.count) {
                            return -1;
                        } else {
                            return this.max - o.max;
                        }
                    case 1:
                        return this.max - o.max;
                    case 2:
                        return this.max - o.max;
                    case 3:
                        return this.max - o.max;
                    case 4:
                        return this.max - o.max;
                    case 22:
                        if (this.count == o.count) {
                            return this.max - o.max;
                        } else {
                            return -100;
                        }
                    case 33:
                        if (this.count == o.count) {
                            return this.max - o.max;
                        } else {
                            return -100;
                        }
                    case 11:
                        if (this.count == o.count) {
                            return this.max - o.max;
                        } else {
                            return -100;
                        }
                    default:
                        return -100;
                }
            } else if (this.type == 100) {
                return 1;
            } else if (o.type == 100) {
                return -1;
            } else {
                return -100;
            }
        }
    }
}

