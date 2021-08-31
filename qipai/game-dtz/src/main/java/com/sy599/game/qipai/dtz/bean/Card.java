package com.sy599.game.qipai.dtz.bean;

import com.sy599.game.qipai.dtz.tool.CardToolDtz;

/**
 * 
 * @author lc
 *
 */
public class Card {
	/**
	 * 数值
	 */
	private int value;
	
	/**
	 * 花色
	 */
	private int suit;
	
	/**
	 * 传进来的是一张牌
	 * 就是花色和数值之和
	 * @param card
	 */
	public Card(int card) {
		value = CardToolDtz.toVal(card);
		suit = CardToolDtz.toSuit(card);
	}
	
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public int getSuit() {
		return suit;
	}
	public void setSuit(int suit) {
		this.suit = suit;
	}
	
	public int toCard() {
		return suit + value;
	}
}
