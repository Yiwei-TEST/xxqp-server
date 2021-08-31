package com.sy599.game.qipai.hgwpaohuzi.tool;

import com.sy599.game.qipai.hgwpaohuzi.bean.CardTypeHuxi;
import com.sy599.game.qipai.hgwpaohuzi.constant.PaohzCard;
import com.sy599.game.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class PaohuziHuLack implements Cloneable {
	private List<Integer> lackVal;
	private boolean isNeedDui;
	private int hongzhongNum;
	private boolean isHu;
	private int huxi;
	private List<Integer> fail2710Val;
	// 胡牌时的牌组
	private List<CardTypeHuxi> phzHuCards;
	// 跑胡或者提胡
	private int paohuAction;
	private List<PaohzCard> paohuList;
	// 胡牌时检查的牌
	private PaohzCard checkCard;
	// 是否是自己摸的牌
	private boolean isSelfMo;

	public PaohzCard getCheckCard() {
		return checkCard;
	}

	public void setCheckCard(PaohzCard checkCard) {
		this.checkCard = checkCard;
	}

	public boolean isSelfMo() {
		return isSelfMo;
	}

	public void setSelfMo(boolean isSelfMo) {
		this.isSelfMo = isSelfMo;
	}

	public int getPaohuAction() {
		return paohuAction;
	}

	public void setPaohuAction(int action) {
		this.paohuAction = action;
	}

	public List<PaohzCard> getPaohuList() {
		return paohuList;
	}

	public void setPaohuList(List<PaohzCard> paohuList) {
		this.paohuList = paohuList;
	}

	public PaohuziHuLack(int hongzhongNum) {
		lackVal = new ArrayList<>();
		this.hongzhongNum = hongzhongNum;
	}

	public void addLack(int val) {
		lackVal.add(val);
	}

	public void addAllLack(List<Integer> vallist) {
		lackVal.addAll(vallist);
	}

	public List<Integer> getLackVal() {
		return lackVal;
	}

	public void setLackVal(List<Integer> lackVal) {
		this.lackVal = lackVal;
	}

	public int getHongzhongNum() {
		return hongzhongNum;
	}

	public void changeHongzhong(int count) {
		hongzhongNum += count;
	}

	public void setHongzhongNum(int hongzhongNum) {
		this.hongzhongNum = hongzhongNum;
	}

	public boolean isNeedDui() {
		return isNeedDui;
	}

	public void setNeedDui(boolean isNeedDui) {
		this.isNeedDui = isNeedDui;
	}

	public boolean isHu() {
		return isHu;
	}

	public void setHu(boolean isHu) {
		this.isHu = isHu;
	}

	public int getHuxi() {
		return huxi;
	}

	public void changeHuxi(int huxi) {
		this.huxi += huxi;
	}

	public void setHuxi(int huxi) {
		this.huxi = huxi;
	}

	public List<Integer> getFail2710Val() {
		return fail2710Val;
	}

	public boolean isHasFail2710Val(int val) {
		if (val % 100 != 2) {
			return true;
		}
		if (fail2710Val == null) {
			return false;
		}
		return fail2710Val.contains(val);
	}

	public void addFail2710Val(int fail2710Val) {
		if (this.fail2710Val == null) {
			this.fail2710Val = new ArrayList<>();
		}
		this.fail2710Val.add(fail2710Val);
	}

	public List<CardTypeHuxi> getPhzHuCards() {
		return phzHuCards;
	}

	public void setPhzHuCards(List<CardTypeHuxi> phzHuCards) {
		this.phzHuCards = phzHuCards;
	}

	public void addPhzHuCards(int action, List<Integer> cards, int huxi) {
		if (this.phzHuCards == null) {
			this.phzHuCards = new ArrayList<>();
		}
		
		CardTypeHuxi type = new CardTypeHuxi();

		type.setCardIds(cards);
		type.setAction(action);
		type.setHux(huxi);
		this.phzHuCards.add(type);
	}

	public void setFail2710Val(List<Integer> fail2710Val) {
		this.fail2710Val = fail2710Val;
	}

	public void copy(PaohuziHuLack copy) {
		setHuxi(copy.getHuxi());
		setPhzHuCards(copy.getPhzHuCards());
	}

	protected PaohuziHuLack clone() {
		PaohuziHuLack o = null;
		try {
			o = (PaohuziHuLack) super.clone();
			if (phzHuCards != null) {
				o.setPhzHuCards(new ArrayList<>(phzHuCards));

			}
			if (fail2710Val != null) {
				o.setFail2710Val(new ArrayList<>(fail2710Val));

			}
			if (paohuList != null) {
				o.setPaohuList(new ArrayList<>(paohuList));

			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.e("PaohuziHuLack clone err", e);
		}

		return o;
	}

	public int calcHuxi(){
	    int res = 0 ;
	    if(phzHuCards != null && phzHuCards.size() > 0){
	        for(CardTypeHuxi hx : phzHuCards){
	            res += hx.getHux();
            }
        }
	    return res;
    }

}
