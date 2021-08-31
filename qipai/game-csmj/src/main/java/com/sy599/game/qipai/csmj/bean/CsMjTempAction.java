package com.sy599.game.qipai.csmj.bean;

import com.sy599.game.qipai.csmj.rule.CsMj;
import com.sy599.game.qipai.csmj.tool.CsMjQipaiTool;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuping 金昌划水麻将临时操作
 */
public class CsMjTempAction {

	private int seat;// 玩家位置
	private int action;// 玩家所做的操作
	private List<CsMj> cardList;// 操作对应的牌

	public CsMjTempAction() {
	}
	
	public CsMjTempAction(int seat, int action, List<CsMj> cardList, List<Integer> hucards) {
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

	public List<CsMj> getCardList() {
		return cardList;
	}

	public void setCardList(List<CsMj> cardList) {
		this.cardList = cardList;
	}

    public void initData(String data) {
		JsonWrapper wrapper = new JsonWrapper(data);
		seat = wrapper.getInt("1", 0);
		action = wrapper.getInt("2", 0);
		String cardStr = wrapper.getString("3");
		cardList = CsMjQipaiTool.explodeMajiang(cardStr, ",");//GuihuziTool.explodeGhz(cardStr, ",");
	}

	public String buildData() {
		JsonWrapper wrapper = new JsonWrapper("");
		wrapper.putInt(1, seat);
		wrapper.putInt(2, action);
		wrapper.putString(3, StringUtil.implode(CsMjQipaiTool.toMajiangIds(cardList), ","));
		return wrapper.toString();
	}
}
