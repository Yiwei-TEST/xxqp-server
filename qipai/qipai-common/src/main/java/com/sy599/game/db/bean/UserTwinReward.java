package com.sy599.game.db.bean;

import java.util.ArrayList;
import java.util.List;

import com.sy599.game.util.StringUtil;

public class UserTwinReward {

	private long userId;
	private int tWinCount;
	private String tWinIds;
	private List<Integer> winRewardList =new ArrayList<>();
	private int tempVal;
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int gettWinCount() {
		return tWinCount;
	}
	public void settWinCount(int tWinCount) {
		this.tWinCount = tWinCount;
	}
	public String gettWinIds() {
		return tWinIds;
	}
	public void settWinIds(String tWinIds) {
		this.tWinIds = tWinIds;
		if(tWinIds!=null &&!StringUtil.isBlank(tWinIds)){
			List<Integer> result= StringUtil.explodeToIntList(tWinIds,"_");
			if(result!=null){
				winRewardList =result;
			}
		}
	}
	public List<Integer> getWinRewardList() {
		return winRewardList;
	}
	public void setWinRewardList(List<Integer> winRewardList) {
		String res = StringUtil.implode(winRewardList, "_");
		tWinIds = res;
	}
	public int getTempVal() {
		return tempVal;
	}
	public void setTempVal(int tempVal) {
		this.tempVal = tempVal;
	}
	
	
	
	
	
}
