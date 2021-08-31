package com.sy599.game.qipai.dtz.bean;

import com.sy599.game.qipai.dtz.rule.CardTypeDtz;

import java.util.List;

/**
 * 出牌的牌型信息
 * @author zhouhj
 *
 */
public class CardPair {
	/** 牌列表（可能不包括花色也可能包括花色，具体看使用场景）*/
	private List<Integer> pokers;
	/** 该牌的牌型 */
	private CardTypeDtz type;
	
	public CardPair(List<Integer> pokers, CardTypeDtz type) {
		this.pokers = pokers;
		this.type = type;
	}

	public List<Integer> getPokers() {
		return pokers;
	}

	public void setPokers(List<Integer> pokers) {
		this.pokers = pokers;
	}

	public CardTypeDtz getType() {
		return type;
	}

	public void setType(CardTypeDtz type) {
		this.type = type;
	}
	
}
