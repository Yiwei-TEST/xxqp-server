package com.sy599.game.qipai.dtz.constant;
/**
 * 需要的常量
 * @author lc
 *
 */
public class DtzConstant {
	/**
	 * 打筒子玩法的玩家
	 */
	public static final int DTZ_PLAYER = 111; 
	
	/**
	 * 需要的牌的数量
	 */
	public static final int POKER_3 = 3, POKER_4 = 4;
	
	
	/**
	 * 一副牌的最大
	 */
	public static final int MAX_COUNT = 15;
	
	
	/**
	 * 别人出的牌比较大
	 */
	public static final int FROM_WIN = 1;

	/**
	 * 别人出的牌识别不了
	 */
	public static final int FROM_CARD_ERROR = -1;
	
	/**
	 * 我出的牌比较大
	 */
	public static final int OWN_WIN = 2;
	
	/**
	 * 我出的牌识别不了
	 */
	public static final int OWN_CARD_ERROR = -2;
	
	/**
	 * 别人的牌和我的牌都有问题
	 */
	public static final int CARD_ERROR = -3;
	
	/**
	 * 这个牌型判断还没完成
	 */
	public static final int CARD_UN_DONE = -4;
	
	/** 默认的结算分值 */
	public static final int MAX_SCORE = 600;
}
