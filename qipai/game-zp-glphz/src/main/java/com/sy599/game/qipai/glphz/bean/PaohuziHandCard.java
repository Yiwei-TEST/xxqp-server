package com.sy599.game.qipai.glphz.bean;

import java.util.List;

import com.sy599.game.qipai.glphz.constant.PaohzCard;
import com.sy599.game.qipai.glphz.rule.PaohuziIndex;
import com.sy599.game.qipai.glphz.rule.PaohzCardIndexArr;

public class PaohuziHandCard {
	private List<PaohzCard> operateCards;
	private List<PaohzCard> inoperableCards;
	private PaohzCardIndexArr indexArr;
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

	public PaohzCardIndexArr getIndexArr() {
		return indexArr;
	}

	public void setIndexArr(PaohzCardIndexArr indexArr) {
		this.indexArr = indexArr;
	}

	public List<PaohzCard> getHandCards() {
		return handCards;
	}

	public void setHandCards(List<PaohzCard> handCards) {
		this.handCards = handCards;
	}

	public boolean isCanoperateCard(PaohzCard card) {
		PaohuziIndex index2 = indexArr.getPaohzCardIndex(2);
		PaohuziIndex index3 = indexArr.getPaohzCardIndex(3);
		if (index2 != null && index2.getPaohzValMap().containsKey(card.getVal())) {
			return false;
		}
		if (index3 != null && index3.getPaohzValMap().containsKey(card.getVal())) {
			return false;
		}
		return true;
	}
}
