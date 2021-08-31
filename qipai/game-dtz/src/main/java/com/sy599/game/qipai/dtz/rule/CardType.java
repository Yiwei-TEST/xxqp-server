package com.sy599.game.qipai.dtz.rule;

public enum CardType {
	/*** 单牌 */
	c1(1),
	/*** 对子 */
	c2(2),
	/*** 3不带 */
	c3(3),
	/*** 炸弹 */
	c4(4),
	/*** 3带1 */
	c31(3),
	/*** 3带2 */
	c32(3),
	/*** 4带2个单，或者一对 */
	c411(4),
	/*** 4带2对 */
	c422(4),
	/*** 连子 */
	c123(5),
	/*** 连队。 */
	c1122(5),
	/*** 飞机。 */
	c111222(6),
	/*** 飞机带单排. */
	c11122234(6),
	/*** 飞机带对子. */
	c1112223344(6),
	/*** 不能出牌 */
	c0(0),
	/** 筒子*/
	cTZ(7);
	
	private int type;
	
	private CardType(int type) {
		this.type=type;
	}
	
	public int getType() {
		return type;
	}
	
}
