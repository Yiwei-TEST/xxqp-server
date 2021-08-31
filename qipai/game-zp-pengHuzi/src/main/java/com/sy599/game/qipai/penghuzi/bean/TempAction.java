package com.sy599.game.qipai.penghuzi.bean;

import java.util.ArrayList;
import java.util.List;

import com.sy599.game.qipai.penghuzi.constant.PenghzCard;
import com.sy599.game.qipai.penghuzi.tool.PenghuziTool;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.StringUtil;

public class TempAction {

	private int seat;// 玩家位置
	private int action;// 玩家所做的操作
	private List<PenghzCard> cardList;// 操作对应的牌
	private PenghzCard nowDisCard;// 当前打出的牌
	
	public TempAction() {
	}
	
	public TempAction(int seat, int action, List<PenghzCard> cardList, PenghzCard nowDisCard) {
		this.seat = seat;
		this.action = action;
		if(cardList == null)
			this.cardList = new ArrayList<>();
		else 
			this.cardList = cardList;
		this.nowDisCard = nowDisCard;
	}
	
	public int getSeat() {
		return seat;
	}
	public void setSeat(int seat) {
		this.seat = seat;
	}
	public int getAction() {
		return action;
	}
	public void setAction(int action) {
		this.action = action;
	}
	public List<PenghzCard> getCardList() {
		return cardList;
	}
	public void setCardList(List<PenghzCard> cardList) {
		this.cardList = cardList;
	}
	public PenghzCard getNowDisCard() {
		return nowDisCard;
	}
	public void setNowDisCard(PenghzCard nowDisCard) {
		this.nowDisCard = nowDisCard;
	}
	
	public void initData(String data) {
		JsonWrapper wrapper = new JsonWrapper(data);
		seat = wrapper.getInt("1", 0);
		action = wrapper.getInt("2", 0);
		String cardStr = wrapper.getString("3");
		cardList = PenghuziTool.explodePhz(cardStr, ",");
		int nowDisCardId = wrapper.getInt("4", 0);
		nowDisCard = PenghzCard.getPaohzCard(nowDisCardId);
	}

	public String buildData() {
		JsonWrapper wrapper = new JsonWrapper("");
		wrapper.putInt(1, seat);
		wrapper.putInt(2, action);
		wrapper.putString(3, StringUtil.implode(PenghuziTool.toPhzCardIds(cardList), ","));
		wrapper.putInt(4, (nowDisCard != null) ? nowDisCard.getId() : 0);
		return wrapper.toString();
	}
}
