package com.sy599.game.qipai.dtz.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DtzModel {
	/* 牌集合 */
	private List<Integer> cards;
	/* 统计 <牌数字，牌个数> */
	private Map<Integer, Integer> pai = new HashMap<Integer, Integer>();
	/* <牌号，牌个数> */
	private Map<Integer, Integer> paiCode = new HashMap<Integer, Integer>();
	
	public List<Integer> getCards() {
		return cards;
	}
	public void setCards(List<Integer> cards) {
		this.cards = cards;
	}
	public Map<Integer, Integer> getPai() {
		return pai;
	}
	public void setPai(Map<Integer, Integer> pai) {
		this.pai = pai;
	}
	public Map<Integer, Integer> getPaiCode() {
		return paiCode;
	}
	public void setPaiCode(Map<Integer, Integer> paiCode) {
		this.paiCode = paiCode;
	}
}
