package com.sy599.game.qipai.xtbp.util;

import java.util.List;

public class CardType {

	/** 单 */
	public static final int DAN = 1;
	/** 对 */
	public static final int DUI = 2;
	/** 拖拉机 */
	public static final int TUOLAJI = 3;
	/** 甩主 */
	public static final int SHUAIPAI = 4;
	
	
	/** 甩牌 有可能没主 */
	public static final int SHUAIPAI2 = 5;
	/***
	 * 甩对子
	 */
	public static final int SHUAI_LIAN_DUI = 6;
	
	private List<Integer> cardIds;
	private int type;
	
	
	public CardType(int type,List<Integer> cardIds) {
		this.type=type;
		this.cardIds =cardIds;
	}
	
	public List<Integer> getCardIds() {
		return cardIds;
	}
	public void setCardIds(List<Integer> cardIds) {
		this.cardIds = cardIds;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
	
}
