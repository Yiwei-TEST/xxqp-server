package com.sy599.game.qipai.hbgzp.bean;

import java.util.ArrayList;
import java.util.List;

import com.sy599.game.qipai.hbgzp.rule.Hbgzp;
import com.sy599.game.qipai.hbgzp.rule.HbgzpIndex;
import com.sy599.game.qipai.hbgzp.rule.HbgzpCardIndexArr;
import com.sy599.game.util.LogUtil;

public class HbgzpHandCard implements Cloneable{
	private List<Hbgzp> operateCards;
	private List<Hbgzp> inoperableCards;
	private HbgzpCardIndexArr indexArr;
	private List<Hbgzp> handCards;
	private List<Integer> jianCards;//捡的牌

	public List<Hbgzp> getOperateCards() {
		return operateCards;
	}

	public void setOperateCards(List<Hbgzp> operateCards) {
		this.operateCards = operateCards;
	}

	public List<Hbgzp> getInoperableCards() {
		return inoperableCards;
	}

	public void setInoperableCards(List<Hbgzp> inoperableCards) {
		this.inoperableCards = inoperableCards;
	}

	public HbgzpCardIndexArr getIndexArr() {
		return indexArr;
	}

	public void setIndexArr(HbgzpCardIndexArr indexArr) {
		this.indexArr = indexArr;
	}

	public List<Hbgzp> getHandCards() {
		return handCards;
	}

	public void setHandCards(List<Hbgzp> handCards) {
		this.handCards = handCards;
	}

	public List<Integer> getJianCards() {
		return jianCards;
	}

	public void setJianCards(List<Integer> jianCards) {
		this.jianCards = jianCards;
	}

	public boolean isCanoperateCard(Hbgzp card) {
		HbgzpIndex index2 = indexArr.getPaohzCardIndex(2);
		HbgzpIndex index3 = indexArr.getPaohzCardIndex(3);
		if (index2 != null && index2.getPaohzValMap().containsKey(card.getVal())) {
			return false;
		}
		if (index3 != null && index3.getPaohzValMap().containsKey(card.getVal())) {
			return false;
		}
		return true;
	}
	
	public HbgzpHandCard clone() {
		HbgzpHandCard o = null;
		try {
			o = (HbgzpHandCard) super.clone();
			if (operateCards != null) {
				o.setOperateCards(new ArrayList<>(operateCards));

			}
			if (inoperableCards != null) {
				o.setInoperableCards(new ArrayList<>(inoperableCards));

			}
			if (handCards != null) {
				o.setHandCards(new ArrayList<>(handCards));

			}
			if (jianCards != null) {
				o.setJianCards(new ArrayList<>(jianCards));
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.e("PaohuziHuLack clone err", e);
		}

		return o;
	}

}
