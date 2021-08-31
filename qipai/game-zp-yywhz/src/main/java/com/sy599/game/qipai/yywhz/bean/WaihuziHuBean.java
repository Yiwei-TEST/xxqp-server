package com.sy599.game.qipai.yywhz.bean;

import java.util.List;

import com.sy599.game.qipai.yywhz.constant.GuihzCard;
import com.sy599.game.qipai.yywhz.rule.GuihzCardIndexArr;

public class WaihuziHuBean {
	private int huxi;
	private boolean ishu;
	private List<GuihzCard> handCards;
	private GuihzCardIndexArr valArr;
	private List<GuihzCard> operateCards;

	public List<GuihzCard> getHandCards() {
		return handCards;
	}

	public void setHandCards(List<GuihzCard> handCards) {
		this.handCards = handCards;
	}

	public GuihzCardIndexArr getValArr() {
		return valArr;
	}

	public void setValArr(GuihzCardIndexArr valArr) {
		this.valArr = valArr;
	}

	public List<GuihzCard> getOperateCards() {
		return operateCards;
	}

	public void setOperateCards(List<GuihzCard> operateCards) {
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
