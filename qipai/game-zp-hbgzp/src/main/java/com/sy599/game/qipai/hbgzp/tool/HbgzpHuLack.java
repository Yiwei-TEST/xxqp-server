package com.sy599.game.qipai.hbgzp.tool;


import java.util.ArrayList;
import java.util.List;

import com.sy599.game.qipai.hbgzp.bean.HbgzpCardDisType;
import com.sy599.game.qipai.hbgzp.rule.Hbgzp;
import com.sy599.game.util.LogUtil;

public class HbgzpHuLack implements Cloneable {
	private List<Integer> lackVal;
	private boolean isNeedDui;
	private boolean momentNeedDui = false;
	private int hongzhongNum;
	private boolean isHu;
	private int huxi;
	// 胡牌时的牌组
	private List<HbgzpCardDisType> phzHuCards;
	// 跑胡或者提胡
	private int paohuAction;
	private List<Hbgzp> paohuList;
	// 胡牌时检查的牌
	private Hbgzp checkCard;
	// 是否是自己摸的牌
	private boolean isSelfMo;
	// 娄底放炮罚特殊牌型：0,飘胡，20,20卡，30,30卡，-1，无特殊牌型
	private int specialHu=-1;
	//名堂list
	private List<Integer> mingTang=new ArrayList<>();
	//计算名堂后的分数
	private int finallyPoint=0;

	public Hbgzp getCheckCard() {
		return checkCard;
	}

	public void setCheckCard(Hbgzp checkCard) {
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

	public List<Hbgzp> getPaohuList() {
		return paohuList;
	}

	public void setPaohuList(List<Hbgzp> paohuList) {
		this.paohuList = paohuList;
	}

	public HbgzpHuLack(int hongzhongNum) {
		lackVal = new ArrayList<>();
		this.hongzhongNum = hongzhongNum;
	}
	public HbgzpHuLack() {
		lackVal = new ArrayList<>();
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

	public boolean isMomentNeedDui() {
		return momentNeedDui;
	}

	public void setMomentNeedDui(boolean momentNeedDui) {
		this.momentNeedDui = momentNeedDui;
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

	public List<HbgzpCardDisType> getPhzHuCards() {
		return phzHuCards;
	}

	public void setPhzHuCards(List<HbgzpCardDisType> phzHuCards) {
		this.phzHuCards = phzHuCards;
	}

	public void addPhzHuCards(int action, List<Integer> cards, int huxi,List<Integer> jianCards) {
		if (this.phzHuCards == null) {
			this.phzHuCards = new ArrayList<>();
		}
		//如果有一张是捡的 则不计算牌型分
//		if(cards != null && cards.size() > 0){
//			for (Integer cardId:cards) {
//				if(jianCards != null && jianCards.contains(cardId)){
//					huxi = 0;
//				}
//			}
//		}
		
		HbgzpCardDisType type = new HbgzpCardDisType();

		type.setCardIds(cards);
		type.setAction(action);
		type.setHuxi(huxi);
		this.phzHuCards.add(type);
	}
	public void addPhzPengHuCards(int action, List<Integer> cards, int huxi,List<Integer> jianCards) {
		if (this.phzHuCards == null) {
			this.phzHuCards = new ArrayList<>();
		}
		
		HbgzpCardDisType type = new HbgzpCardDisType();
		
		type.setCardIds(cards);
		type.setAction(action);
		type.setHuxi(huxi);
		this.phzHuCards.add(type);
	}

	public void copy(HbgzpHuLack copy) {
		setHuxi(copy.getHuxi());
		setPhzHuCards(copy.getPhzHuCards());
	}

	protected HbgzpHuLack clone() {
		HbgzpHuLack o = null;
		try {
			o = (HbgzpHuLack) super.clone();
			if (phzHuCards != null) {
				o.setPhzHuCards(new ArrayList<>(phzHuCards));

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
	        for(HbgzpCardDisType hx : phzHuCards){
	            res += hx.getHuxi();
            }
        }
	    return res;
    }

	public List<Integer> getMingTang() {
		return mingTang;
	}

	public void setMingTang(List<Integer> mingTang) {
		this.mingTang = mingTang;
	}

	public int getFinallyPoint() {
		return finallyPoint;
	}

	public void setFinallyPoint(int finallyPoint) {
		this.finallyPoint = finallyPoint;
	}

	public int getSpecialHu() {
		return specialHu;
	}

	public void setSpecialHu(int specialHu) {
		this.specialHu = specialHu;
	}

}
