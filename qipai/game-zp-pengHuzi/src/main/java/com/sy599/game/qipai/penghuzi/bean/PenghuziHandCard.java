package com.sy599.game.qipai.penghuzi.bean;

import java.util.List;

import com.sy599.game.qipai.penghuzi.constant.PenghzCard;
import com.sy599.game.qipai.penghuzi.rule.PenghuziIndex;
import com.sy599.game.qipai.penghuzi.rule.PenghzCardIndexArr;

public class PenghuziHandCard {
	private List<PenghzCard> operateCards;
	private List<PenghzCard> inoperableCards;
	private PenghzCardIndexArr indexArr;
	private List<PenghzCard> handCards;

	public List<PenghzCard> getOperateCards() {
		return operateCards;
	}

	public void setOperateCards(List<PenghzCard> operateCards) {
		this.operateCards = operateCards;
	}

	public List<PenghzCard> getInoperableCards() {
		return inoperableCards;
	}

	public void setInoperableCards(List<PenghzCard> inoperableCards) {
		this.inoperableCards = inoperableCards;
	}

	public PenghzCardIndexArr getIndexArr() {
		return indexArr;
	}

	public void setIndexArr(PenghzCardIndexArr indexArr) {
		this.indexArr = indexArr;
	}

	public List<PenghzCard> getHandCards() {
		return handCards;
	}

	public void setHandCards(List<PenghzCard> handCards) {
		this.handCards = handCards;
	}

	public boolean isCanoperateCard(PenghzCard card) {
		PenghuziIndex index2 = indexArr.getPaohzCardIndex(2);
		PenghuziIndex index3 = indexArr.getPaohzCardIndex(3);
		if (index2 != null && index2.getPaohzValMap().containsKey(card.getVal())) {
			return false;
		}
		if (index3 != null && index3.getPaohzValMap().containsKey(card.getVal())) {
			return false;
		}
		return true;
	}
}
