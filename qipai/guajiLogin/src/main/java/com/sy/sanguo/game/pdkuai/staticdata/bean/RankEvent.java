package com.sy.sanguo.game.pdkuai.staticdata.bean;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RankEvent {
	private int day;
	private List<Integer> events;
    // 奖励
	private Map<Integer, Integer> awardMap;
	// 统计前多少名
	private int rankCount;

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public List<Integer> getEvents() {
		return events;
	}

	public void setEvents(List<Integer> events) {
		this.events = events;
	}

	public Map<Integer, Integer> getAwardMap() {
		return awardMap;
	}

	public void setAwardMap(Map<Integer, Integer> awardMap) {
		this.awardMap = awardMap;
	}

	public int getRankCount() {
		return rankCount;
	}

	public void setRankCount(int rankCount) {
		this.rankCount = rankCount;
	}

	public int getAward(int rank) {
		for (Entry<Integer, Integer> entry : awardMap.entrySet()) {
			if (rank <= entry.getKey()) {
				return entry.getValue();
			}
		}
		return 0;
	}
}
