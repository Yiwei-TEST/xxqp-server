package com.sy599.game.qipai.penghuzi.bean;

import java.util.List;

import com.sy599.game.qipai.penghuzi.constant.PenghzCard;
import com.sy599.game.qipai.penghuzi.rule.PenghzCardIndexArr;

public class PenghuziHuBean {
	private int huxi;
	private boolean ishu;
	private List<PenghzCard> handCards;
	private PenghzCardIndexArr valArr;
	private List<PenghzCard> operateCards;

	public List<PenghzCard> getHandCards() {
		return handCards;
	}

	public void setHandCards(List<PenghzCard> handCards) {
		this.handCards = handCards;
	}

	public PenghzCardIndexArr getValArr() {
		return valArr;
	}

	public void setValArr(PenghzCardIndexArr valArr) {
		this.valArr = valArr;
	}

	public List<PenghzCard> getOperateCards() {
		return operateCards;
	}

	public void setOperateCards(List<PenghzCard> operateCards) {
		this.operateCards = operateCards;
	}

	// private
	public int getHuxi() {
		return huxi;
	}

	public void setHuxi(int huxi) {
		this.huxi = huxi;
	}

	public boolean isIshu() {
		return ishu;
	}

	public void setIshu(boolean ishu) {
		this.ishu = ishu;
	}

}
