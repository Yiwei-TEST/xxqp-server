package com.sy599.game.qipai.xx2710.bean;

import com.sy599.game.qipai.xx2710.constant.PaohzCard;
import com.sy599.game.qipai.xx2710.tool.PaohuziTool;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class TempAction {

	private int seat;// 玩家位置
	private int action;// 玩家所做的操作
	private List<PaohzCard> cardList;// 操作对应的牌
	private PaohzCard nowDisCard;// 当前打出的牌
	
	public TempAction() {
	}
	
	public TempAction(int seat, int action, List<PaohzCard> cardList, PaohzCard nowDisCard) {
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
	public List<PaohzCard> getCardList() {
		return cardList;
	}
	public void setCardList(List<PaohzCard> cardList) {
		this.cardList = cardList;
	}
	public PaohzCard getNowDisCard() {
		return nowDisCard;
	}
	public void setNowDisCard(PaohzCard nowDisCard) {
		this.nowDisCard = nowDisCard;
	}
	
	public void initData(String data) {
		JsonWrapper wrapper = new JsonWrapper(data);
		seat = wrapper.getInt("1", 0);
		action = wrapper.getInt("2", 0);
		String cardStr = wrapper.getString("3");
		cardList = PaohuziTool.explodePhz(cardStr, ",");
		int nowDisCardId = wrapper.getInt("4", 0);
		nowDisCard = PaohzCard.getPaohzCard(nowDisCardId);
	}

	public String buildData() {
		JsonWrapper wrapper = new JsonWrapper("");
		wrapper.putInt(1, seat);
		wrapper.putInt(2, action);
		wrapper.putString(3, StringUtil.implode(PaohuziTool.toPhzCardIds(cardList), ","));
		wrapper.putInt(4, (nowDisCard != null) ? nowDisCard.getId() : 0);
		return wrapper.toString();
	}
}
