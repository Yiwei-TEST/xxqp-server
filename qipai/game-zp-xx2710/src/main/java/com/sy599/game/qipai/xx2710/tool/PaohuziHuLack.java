package com.sy599.game.qipai.xx2710.tool;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sy599.game.qipai.xx2710.bean.CardTypeHuxi;
import com.sy599.game.qipai.xx2710.constant.PaohzCard;
import com.sy599.game.util.LogUtil;

public class PaohuziHuLack implements Cloneable {
	private boolean isHu;
	// 胡牌时的牌组
	private List<CardTypeHuxi> phzHuCards;
	//名堂list
	private List<Integer> mingTang=new ArrayList<>();
	//分数，平胡时为红字数量
	private int fen=0;

	public PaohuziHuLack() {
	}


	public boolean isHu() {
		return isHu;
	}

	public void setHu(boolean isHu) {
		this.isHu = isHu;
	}


	public List<CardTypeHuxi> getPhzHuCards() {
		return phzHuCards;
	}

	public void setPhzHuCards(List<CardTypeHuxi> phzHuCards) {
		this.phzHuCards = phzHuCards;
	}

	public void addPhzHuCards(int action, List<PaohzCard> cards, Map<Integer,Integer> idAndVal) {
		if (this.phzHuCards == null) {
			this.phzHuCards = new ArrayList<>();
		}

		CardTypeHuxi type = new CardTypeHuxi();
		type.setCardIds(PaohuziTool.toPhzCardIds(cards));
		type.setAction(action);
		type.setIdAndVals(idAndVal);
		this.phzHuCards.add(type);
	}


	public PaohuziHuLack clone() {
		PaohuziHuLack o = null;
		try {
			o = (PaohuziHuLack) super.clone();
			if (phzHuCards != null) {
				List<CardTypeHuxi> l=new ArrayList<>(phzHuCards.size());
				for (CardTypeHuxi type:phzHuCards) {
					l.add(type.clone());
				}
				o.setPhzHuCards(l);
			}
			if(o.getMingTang()!=null){
				o.setMingTang(new ArrayList<>(mingTang));
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.e("PaohuziHuLack clone err", e);
		}

		return o;
	}

	public List<Integer> getMingTang() {
		return mingTang;
	}

	public void setMingTang(List<Integer> mingTang) {
		this.mingTang = mingTang;
	}

	public int getFen() {
		return fen;
	}

	public void setFen(int fen) {
		this.fen = fen;
	}
}
