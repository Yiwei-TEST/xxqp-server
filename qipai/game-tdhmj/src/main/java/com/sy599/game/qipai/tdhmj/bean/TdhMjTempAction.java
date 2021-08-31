package com.sy599.game.qipai.tdhmj.bean;

import com.sy599.game.qipai.tdhmj.rule.TdhMj;
import com.sy599.game.qipai.tdhmj.tool.TdhMjQipaiTool;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuping 金昌划水麻将临时操作
 */
public class TdhMjTempAction {

	private int seat;// 玩家位置
	private int action;// 玩家所做的操作
	private List<TdhMj> cardList;// 操作对应的牌

	public TdhMjTempAction() {
	}
	
	public TdhMjTempAction(int seat, int action, List<TdhMj> cardList, List<Integer> hucards) {
		this.seat = seat;
		this.action = action;
		if(cardList == null)
			this.cardList = new ArrayList<>();
		else 
			this.cardList = cardList;
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

	public List<TdhMj> getCardList() {
		return cardList;
	}

	public void setCardList(List<TdhMj> cardList) {
		this.cardList = cardList;
	}

    public void initData(String data) {
		JsonWrapper wrapper = new JsonWrapper(data);
		seat = wrapper.getInt("1", 0);
		action = wrapper.getInt("2", 0);
		String cardStr = wrapper.getString("3");
		cardList = TdhMjQipaiTool.explodeMajiang(cardStr, ",");//GuihuziTool.explodeGhz(cardStr, ",");
	}

	public String buildData() {
		JsonWrapper wrapper = new JsonWrapper("");
		wrapper.putInt(1, seat);
		wrapper.putInt(2, action);
		wrapper.putString(3, StringUtil.implode(TdhMjQipaiTool.toMajiangIds(cardList), ","));
		return wrapper.toString();
	}
}
