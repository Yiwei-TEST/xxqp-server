package com.sy.sanguo.game.msg;

import java.util.List;
import java.util.Map;

public class FirstMythMsg {
	private List<Map<String, Object>> list;
	private Map<String, Object> self;
	private int func;
	private int rankCount;

	public List<Map<String, Object>> getList() {
		return list;
	}

	public void setList(List<Map<String, Object>> list) {
		this.list = list;
	}

	public Map<String, Object> getSelf() {
		return self;
	}

	public void setSelf(Map<String, Object> self) {
		this.self = self;
	}

	public int getFunc() {
		return func;
	}

	public void setFunc(int func) {
		this.func = func;
	}

	public int getRankCount() {
		return rankCount;
	}

	public void setRankCount(int rankCount) {
		this.rankCount = rankCount;
	}

}
