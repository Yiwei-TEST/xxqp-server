package com.sy599.game.qipai.dtz.rule;

/**
 * dtz的牌型
 * @author lc
 *
 */
public enum CardTypeDtz {
	
	/** 不能出牌*/
	CARD_0(0),
	
	/** 单牌*/
	CARD_1(1),
	
	/** 对子*/
	CARD_2(2),
	
	/** 三张*/
	CARD_3(3),
	
	/** 三带一或者三带二*/
	CARD_4(4),
	
	/** 单顺*/
	CARD_5(5),
	
	/** 双顺*/
	CARD_6(6),
	
	/**  三顺*/
	CARD_7(7),
	
	/** 飞机带翅膀*/
	CARD_8(8),
	
	/** 炸弹*/
	CARD_9(9),
	
	/** 筒子*/
	CARD_10(10),
	
	/** 地炸*/
	CARD_11(11),
	
	/** 大小王 王炸*/
	CARD_12(12),
	
	/** 囍 */
	CARD_13(13);
	
	/**
	 * 牌的类型
	 */
	private int type;
	
	private CardTypeDtz(int type) {
		this.type = type;
	}
	
	
	/**
	 * 获取到牌型
	 * @return
	 */
	public int getType() {
		return type;
	}
	
}
