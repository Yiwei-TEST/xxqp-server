package com.sy599.game.qipai.dtz.tool;

import com.sy599.game.qipai.dtz.bean.Card;
import com.sy599.game.qipai.dtz.constant.DtzConstant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * 卡牌工具
 *  比如发牌等动作
 * @author lc
 *	
 */
public class CardToolDtz {
	
	/**
	 * 三副牌  没有 3， 4
	 */
	private static final ArrayList<Integer> POKER_3 = new ArrayList<>();
	
	/**
	 * 四副牌  没有3， 4 但是有小王大王
	 */
	private static final ArrayList<Integer> POKER_4 = new ArrayList<Integer>();
	
	static {
		for (int count = 0; count < DtzConstant.POKER_3; count ++) {
			for (int val = 3; val <= DtzConstant.MAX_COUNT; val ++) {
				if (val != 3 && val != 4) { //去掉 3, 4
					for (int suit = 1; suit <= 4; suit ++) { //生成花色
						POKER_3.add(val + suit * 100);
					}
				}
			}
		}
		
		/*
		 * 四副牌没有筒子但是有xi. 还有王炸
		 * 分数是， 普通的xi 100分， 大小王的xi的 是 200分 
		 */
		for (int count = 0; count < DtzConstant.POKER_4; count ++) {
			for (int val = 3; val <= DtzConstant.MAX_COUNT; val ++) {
				if (val != 3 && val != 4) { //去掉 3, 4
					for (int suit = 1; suit <= 4; suit ++) { //生成花色
						POKER_4.add(val + suit * 100);
					}
				}
			}
			POKER_4.add(16);
			POKER_4.add(17);
		}
		//四副牌是有大小鬼的
	}
	
	/**
	 * 得到牌的花色（黑桃400>红桃300>梅花200>方块100
	 * @param poker
	 * @return
	 */
	public static int toSuit(int poker) {
		return poker/100*100;
	}
	
	/**
	 * 得到牌的数字，如果已经是牌的数字，也不影响
	 * @param poker
	 * @return
	 */
	public static int toVal(int poker) {
		return poker - toSuit(poker);
	}
	
	/**
	 * 得到一副牌的花色列表 这副牌必须是排好序的 <br />
	 * key是传进来的花色和数字之和，value是这个对象 <br />
	 * @param pokers
	 * @return LinkedHashMap
	 */
	public static ArrayList<Card> toCardList(List<Integer> pokers) {
		ArrayList<Card> cards = new ArrayList<>();
		for (int card : pokers) {
			cards.add(new Card(card));
		}
		return cards;
	}
	
	/**
	 * 得到一副牌的数字列表
	 * @param pokers
	 * @return
	 */
	public static List<Integer> toValueList(List<Integer> pokers) {
		ArrayList<Integer> values = new ArrayList<>();
		for (int card : pokers) {
			values.add(toVal(card));
		}
		return values;
	}
	
	/**
	 * 得到一副牌的花色列表
	 * @param pokers
	 * @return
	 */
	public static List<Integer> toSuitList(List<Integer> pokers) {
		ArrayList<Integer> suites = new ArrayList<>();
		for (int card : pokers) {
			suites.add(toSuit(card));
		}
		return suites;
	}
	
	/**
	 * 把一张牌转化成card对象
	 * @param card
	 * @return
	 */
	public static Card toCard(int card) {
		return new Card(card);
	}
	
	
	/**
	 * 得到一副牌
	 * @return
	 * @param type 牌的类型
	 */
	public static List<Integer> createPokers(int type) {
		switch (type) {
		case DtzConstant.POKER_3:
			return POKER_3;
		case DtzConstant.POKER_4:
			return POKER_4;
		default:
			return Collections.<Integer>emptyList();
		}
	}
	
	/**
	 * 给每个玩家一副牌
	 * @param type 牌的类型
	 * @param maxPlayerCount  玩家数量
	 * @return
	 */
	public static List<ArrayList<Integer>> getPokers(int type, int maxPlayerCount) {
		List<Integer> list;
		if (type == DtzConstant.POKER_3) {
			list = new ArrayList<Integer> (POKER_3);
		}
		else if (type == DtzConstant.POKER_4) {
			list = new ArrayList<Integer> (POKER_4);
		}
		else return Collections.<ArrayList<Integer>>emptyList();
		Collections.shuffle(list); //先把牌洗乱
		ArrayList<ArrayList<Integer>> pokersList = new ArrayList<>();
		int maxSize = list.size() / maxPlayerCount; //每个人平均的大小
		int index;
		for (index = 0; index < list.size();) { //开始发牌
			ArrayList<Integer> playerPokers = new ArrayList<>();
			for (int poker_i = 1; poker_i <= maxSize; poker_i ++) {
				playerPokers.add(list.get(index)); //得到牌然后放进去
				index ++;
			}
			pokersList.add(playerPokers);
			if (pokersList.size() == maxPlayerCount) break;
		}
		if (list.size() % maxPlayerCount != 0) { //有多的  默认全给最后一个
			pokersList.get(pokersList.size() - 1).addAll(list.subList(index, list.size()));
		}
		return pokersList;
	}
	
	
	/**
	 * 从已知牌中删掉  要出的
	 * @param cards
	 * @param handPais
	 */
	public static List<Integer> removeAll(List<Integer> cards, List<Integer> handPais) {
		for (int card : cards) {
			handPais = removeNode(card, handPais);
		}
		return handPais;
	}
	
	public static List<Card> removeAllByCard(List<Card> cards, List<Card> handPais) {
		for (Card card : cards) {
			handPais = removeNode(card, handPais);
		}
		return handPais;
	}
	
	
	/**
	 * 删掉这个节点 得到一个新节点
	 * @param node
	 * @param handPais
	 */
	private static ArrayList<Integer> removeNode(int node, List<Integer> handPais) {
		ArrayList<Integer> newPas = new ArrayList<>();
		int times = 0;
		for (int i = 0; i < handPais.size(); i ++) {
			if (handPais.get(i) != node) {
				newPas.add(handPais.get(i));
			}
			else times ++;
		}
		times --;
		for (;times > 0; times --) {
			newPas.add(node);
		}
		return newPas;
	}
	
	private static ArrayList<Card> removeNode(Card node, List<Card> handPais) {
		ArrayList<Card> newPas = new ArrayList<>();
		int times = 0;
		for (int i = 0; i < handPais.size(); i ++) {
			Card cc = handPais.get(i);
			if (cc.getValue() != node.getValue()) {
				newPas.add(handPais.get(i));
			}
			else {
				if (cc.getSuit() == node.getSuit()) {
					times ++;
				}
				else newPas.add(handPais.get(i));
			}
		}
		times --;
		for (;times > 0; times --) {
			newPas.add(node);
		}
		return newPas;
	}
	
	public static void main(String args[]) {
//		ArrayList<Integer> handPais = new ArrayList<>(Arrays.asList(1, 1, 1, 2, 2, 2, 2, 3, 3, 4, 4, 5, 5)), cards = new ArrayList<>(Arrays.asList(1, 1, 2, 2));
//		System.out.println(com.sy599.game.qipai.datongzi.tool.CardTool.removeAll(cards, handPais));
//		List<ArrayList<Integer>> pokers = getPokers(DtzConstant.POKER_3, 4);
//		for (ArrayList<Integer> pc : pokers) {
//			System.out.println(pc.size());
//		}
		
		List<Integer> v = new ArrayList<Integer>();
		v.add(1);
		v.add(2);
		v.add(3);
		v.add(1);
		v.add(4);
		for(int i=0;i<v.size();i++){
			System.out.println("i="+i+",v="+v.get(i));
		}
		System.out.println("========");
		
		v.remove(Integer.valueOf(1));
		for(int i=0;i<v.size();i++){
			System.out.println("i="+i+",v="+v.get(i));
		}
		
		
	}
}
