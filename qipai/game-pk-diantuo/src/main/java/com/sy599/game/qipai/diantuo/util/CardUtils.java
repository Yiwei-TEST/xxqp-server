package com.sy599.game.qipai.diantuo.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.math.RandomUtils;


public final class CardUtils {

    public static final CardValue EMPTY_CARD_VALUE = new CardValue(0, 0,0);
    public static final List<CardValue> EMPTY_CARD_VALUE_LIST = Arrays.asList(EMPTY_CARD_VALUE);


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
    	if(cards==null||cards.isEmpty()){
    		return 0;
    	}
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
    
    
    
    public static int loadWangCount(List<Integer> handPais) {
    	int count =0;
    	for(Integer id: handPais){
    		int value = loadCardValue(id);
    		if(value==1||value==2){
    			count++;
    		}
    	}
        return count;
    }
    
    

    /**
     * 计算牌值(A_14,2_15,3_3...,K_13)
     *
     * @param card
     * @return
     */
    public static int loadCardValue(int card) {
    	int value = card % 100;
        return value;
    }
    
    public static boolean isYingZhu(int card){
    	int value = loadCardValue(card);
    	
    	if(value==7||value==15||value==1||value==2) {
    		return true;
    	}
    	return false;
    }
    
    
    public static boolean isZhu(int card,int zhuColor){
    	int value = loadCardValue(card);
    	int color =loadCardColor(card);
    	if(value==7||value==15||value==1||value==2) {
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
    	if(value==7){
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
    
    
    
    
    private static int getCardPro(int card,int zhuColor){
    	
    	int val = loadCardValue(card);
    	int color = loadCardColor(card);
    	
    	int priority = 0;
    	if(val==2){
    		priority = 1;
    	}else if(val==1){
    		priority = 2;
    	}else if(val==7&&color==zhuColor){
    		priority=3;
    	}else if(val==7){
    		priority=4;
    	}else if(val==15&&color==zhuColor){
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
	 * @param hands
	 * @param color
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
    
	public static int hasDuiCount(List<Integer> hands){
	HashMap<Integer,Integer> map = new HashMap<Integer,Integer>();
		for(Integer id: hands){
			int value = CardUtils.loadCardValue(id);
			Integer count = map.get(value);
			if(count==null){
				map.put(value, 1);
			}else {
				map.put(value, count+1);
			}
		}
		
		List<Integer> list = new ArrayList<Integer>(map.values());
		for(Integer c: list){
			if(c!=2){
				return 0;
			}
		}
		return map.size();
	}
	
    
    
    
    
    public static void main(String[] args) {
		
    	List<Integer> newCardIds = new LinkedList<Integer>();
    	newCardIds.add(501);
    	newCardIds.add(501);
    	newCardIds.add(315);
    	newCardIds.add(315);
    	List<Integer> proList = new ArrayList<Integer>();
		for(int i=0;i<newCardIds.size()-1;i++) {
			int a = newCardIds.get(i);
			int b = newCardIds.get(i+1);
			int val1 = getCardPro(a, 3);
			int val2 = getCardPro(b, 3);
			//相邻的两对不是连着的 不是拖拉机
			if(a!=b){//&&(Math.abs(val2-val1)!=1)
				if(!proList.contains(val1)){
					proList.add(val1);
				}
				if(!proList.contains(val2)){
					proList.add(val2);
				}
//				
//				break;
			}
		}
    	
    	
    	
    	Collections.sort(proList);
		for(int i=0;i<proList.size()-1;i++){
			if(Math.abs(proList.get(i)-proList.get(i+1))!=1){
				System.out.println("shuai...............");
				break;
			}
		}
    	
    	
    	System.out.println("pro = +" + proList);
    	
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

