package com.sy599.game.qipai.niushibie.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NsbUtil {
	/**
	 * 获取下一个说话玩家的位置
	 * @param ups
	 * @return
	 */
//	public static String getNextUps(String ups) {
//		if(ups.equals("4")) {
//			return "1";
//		} else {
//			return String.valueOf(Integer.parseInt(ups)+1);
//		}
//	}
	public static List<Integer> getScoreCardsList(List<Integer> cardIds) {
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
	public static String getDuiJiaUps(String ups) {
		String djups = "";
		if(ups.equals("1")) {
			djups = "3";
		} else if(ups.equals("2")) {
			djups = "4";
		} else if(ups.equals("3")) {
			djups = "1";
		} else if(ups.equals("4")) {
			djups = "2";
		}
		return djups;
	}
	
	/**
	 * 获取一组牌里的分
	 * @param pai
	 * @return
	 */
	public static int getCpfen(List<String> pai) {
		if(null==pai || pai.size()==0){
			return 0;
		}
		int fen = 0;
		int[] ary = NsbSf.paiToShortAry(pai);
		fen = ary[2]*5 + ary[7]*10 + ary[10]*10;
		return fen;
	}
	/**
	 * 获取一组牌里的分
	 * @param pai2
	 * @return
	 */
	public static int getScoreCards(List<Integer> pai2) {
		List<String> pai = NsbSfNew.intCardToStringCard(pai2);
		if(null==pai || pai.size()==0){
			return 0;
		}
		int fen = 0;
		int[] ary = NsbSf.paiToShortAry(pai);
		fen = ary[2]*5 + ary[7]*10 + ary[10]*10;
		return fen;
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
	public static List<Integer> getScoreCards2(List<Integer> cardIds) {
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
	 * 获取喜钱分数
	 * @param pai
	 * @return
	 */
	public static int getXiqian(List<String> pai) {
		if(NsbSf.is8zha(pai)) {
			return 4*4;
		} else if(NsbSf.is7zha(pai)) {
			return 2*4;
		} else if(NsbSf.is6zha(pai)) {
			return 1*4;
		} else if(NsbSf.is4wang(pai)) {
			return 1*4;
		} else {
			return 0;
		}
	}
	
	public static void main(String[] args) {

//		String no_1 = "3";
//		String no_2 = "4";
//		String no_3 = "1";
//		String no_4 = null;
//		String ups = "2";
//		String nextups = "";
//		List<String> polt = new ArrayList<String>();
//		List<String> playerList = new ArrayList<String>();
//		playerList.add("2");playerList.add("3");playerList.add("4");playerList.add("1");
//		for(String pId : playerList) {
//			String uups = pId;
//			if(!uups.equals(no_1) && !uups.equals(no_2) && !uups.equals(no_3) && !uups.equals(no_4)) {
//				polt.add(uups);
//			}
//		}
//		Collections.sort(polt);
//		for (int i = 0; i < polt.size(); i++) {
//			String p = polt.get(i);
//			if(p.equals(ups)) {
//				if(i != polt.size()-1) {
//					nextups = polt.get(i+1);
//				} else {
//					nextups = polt.get(0);
//				}
//			}
//		}
//		System.out.println(nextups);
	}
}
