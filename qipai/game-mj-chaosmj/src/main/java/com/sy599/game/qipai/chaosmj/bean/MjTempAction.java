package com.sy599.game.qipai.chaosmj.bean;

import com.sy599.game.qipai.chaosmj.rule.Mj;
import com.sy599.game.qipai.chaosmj.tool.MjQipaiTool;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuping 麻将临时操作
 */
public class MjTempAction {

	private int seat;// 玩家位置
	private int action;// 玩家所做的操作
	private List<Mj> cardList;// 操作对应的牌

	public MjTempAction() {
	}
	
	public MjTempAction(int seat, int action, List<Mj> cardList, List<Integer> hucards) {
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

	public List<Mj> getCardList() {
		return cardList;
	}

	public void setCardList(List<Mj> cardList) {
		this.cardList = cardList;
	}

    public void initData(String data) {
		JsonWrapper wrapper = new JsonWrapper(data);
		seat = wrapper.getInt("1", 0);
		action = wrapper.getInt("2", 0);
		String cardStr = wrapper.getString("3");
		cardList = MjQipaiTool.explodeMajiang(cardStr, ",");//GuihuziTool.explodeGhz(cardStr, ",");
	}

	public String buildData() {
		JsonWrapper wrapper = new JsonWrapper("");
		wrapper.putInt(1, seat);
		wrapper.putInt(2, action);
		wrapper.putString(3, StringUtil.implode(MjQipaiTool.toMajiangIds(cardList), ","));
		return wrapper.toString();
	}
}
