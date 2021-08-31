package com.sy599.game.qipai.lyzp.been;


import com.sy599.game.qipai.lyzp.constant.PaohzCard;
import com.sy599.game.qipai.lyzp.rule.PaohzCardIndexArr;

import java.util.List;

public class PaohuziHuBean {
	private int huxi;
	private boolean ishu;
	private List<PaohzCard> handCards;
	private PaohzCardIndexArr valArr;
	private List<PaohzCard> operateCards;

	public List<PaohzCard> getHandCards() {
		return handCards;
	}

	public void setHandCards(List<PaohzCard> handCards) {
		this.handCards = handCards;
	}

	public PaohzCardIndexArr getValArr() {
		return valArr;
	}

	public void setValArr(PaohzCardIndexArr valArr) {
		this.valArr = valArr;
	}

	public List<PaohzCard> getOperateCards() {
		return operateCards;
	}

	public void setOperateCards(List<PaohzCard> operateCards) {
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
