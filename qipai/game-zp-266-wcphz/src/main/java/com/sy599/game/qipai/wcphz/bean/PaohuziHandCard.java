package com.sy599.game.qipai.wcphz.bean;

import com.sy599.game.qipai.wcphz.constant.PaohzCard;
import com.sy599.game.qipai.wcphz.rule.PaohuziIndex;
import com.sy599.game.qipai.wcphz.rule.PaohzCardIndexArr;

import java.util.List;

public class PaohuziHandCard {
	//可操作的卡牌,去掉了起手牌中的3张与4张
	private List<PaohzCard> operateCards;
	private List<PaohzCard> inoperableCards;
	//各个相同数量的牌型,[0]一张,[1]两张,[2]三张,[3]四张
	private PaohzCardIndexArr groupByNumberToArray;
	//当前手牌
	private List<PaohzCard> handCards;

	public List<PaohzCard> getOperateCards() {
		return operateCards;
	}

	public void setOperateCards(List<PaohzCard> operateCards) {
		this.operateCards = operateCards;
	}

	public List<PaohzCard> getInoperableCards() {
		return inoperableCards;
	}

	public void setInoperableCards(List<PaohzCard> inoperableCards) {
		this.inoperableCards = inoperableCards;
	}

	public PaohzCardIndexArr getGroupByNumberToArray() {
		return groupByNumberToArray;
	}

	public void setGroupByNumberToArray(PaohzCardIndexArr groupByNumberToArray) {
		this.groupByNumberToArray = groupByNumberToArray;
	}

	public List<PaohzCard> getHandCards() {
		return handCards;
	}

	public void setHandCards(List<PaohzCard> handCards) {
		this.handCards = handCards;
	}

	public boolean isCanoperateCard(PaohzCard card) {
		PaohuziIndex index2 = groupByNumberToArray.getPaohzCardIndex(2);
		PaohuziIndex index3 = groupByNumberToArray.getPaohzCardIndex(3);
		if (index2 != null && index2.getPaohzValMap().containsKey(card.getVal())) {
			return false;
		}
		if (index3 != null && index3.getPaohzValMap().containsKey(card.getVal())) {
			return false;
		}
		return true;
	}
}
