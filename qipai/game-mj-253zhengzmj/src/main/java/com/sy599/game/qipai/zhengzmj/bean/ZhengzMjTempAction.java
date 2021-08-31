package com.sy599.game.qipai.zhengzmj.bean;

import java.util.ArrayList;
import java.util.List;

import com.sy599.game.qipai.zhengzmj.rule.ZhengzMj;
import com.sy599.game.qipai.zhengzmj.tool.ZhengzMjQipaiTool;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.StringUtil;

public class ZhengzMjTempAction {

	private int seat;// 玩家位置
	private int action;// 玩家所做的操作
	private List<ZhengzMj> cardList;// 操作对应的牌
    private List<Integer> hucards;// 报听胡的牌
    
    public ZhengzMjTempAction(){
    }
    
    public ZhengzMjTempAction(int seat, int action, List<ZhengzMj> cardList, List<Integer> hucards){
    	this.seat = seat;
		this.action = action;
		if(cardList == null)
			this.cardList = new ArrayList<>();
		else 
			this.cardList = cardList;
		this.hucards = hucards;
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

	public List<ZhengzMj> getCardList() {
		return cardList;
	}

	public void setCardList(List<ZhengzMj> cardList) {
		this.cardList = cardList;
	}

	public List<Integer> getHucards() {
		return hucards;
	}

	public void setHucards(List<Integer> hucards) {
		this.hucards = hucards;
	}
	
	public void initData(String data) {
		JsonWrapper wrapper = new JsonWrapper(data);
		seat = wrapper.getInt("1", 0);
		action = wrapper.getInt("2", 0);
		String cardStr = wrapper.getString("3");
		cardList = ZhengzMjQipaiTool.explodeMajiang(cardStr, ",");//GuihuziTool.explodeGhz(cardStr, ",");
        String hucardStr = wrapper.getString("4");
        hucards = StringUtil.explodeToIntList(hucardStr, ",");
	}

	public String buildData() {
		JsonWrapper wrapper = new JsonWrapper("");
		wrapper.putInt(1, seat);
		wrapper.putInt(2, action);
		wrapper.putString(3, StringUtil.implode(ZhengzMjQipaiTool.toMajiangIds(cardList), ","));
		wrapper.putString(4, StringUtil.implode(hucards, ","));
		return wrapper.toString();
	}
	
}
