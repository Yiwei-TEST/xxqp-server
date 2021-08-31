package com.sy599.game.qipai.diantuo.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardType {

	/** 单 */
	public static final int DAN = 1;
	/** 对 */
	public static final int DUI = 2;
	/** 连对 */
	public static final int LIAN_DUI = 3;
	/** 三张 */
	public static final int SAN_ZHANG = 4;
	/** 飞机 */
	public static final int FEI_JI = 5;
	
	/** 五十K */
	public static final int WU_SHI_K = 6;
	/**炸弹*/
	public static final int BOOM = 7;
	
	/**天炸*/
	public static final int TIAN_BOOM = 8;
	
	/**三带一对*/
	public static final int SAN_ZHANG_DUI = 9;
	
	
	/**天炸*/
	public static final int SHUNZI = 10;
	
	
	private List<Integer> cardIds;
	private int type;
	
	private int val;//牌型值
	
	private int val2;//牌型附加  比如三带1 三带2 炸弹
	
	
	public int getVal2() {
		return val2;
	}

	public void setVal2(int val2) {
		this.val2 = val2;
	}

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

	public int getVal() {
		return val;
	}

	public void setVal(int val) {
		this.val = val;
	}
	
	

	
	
}
