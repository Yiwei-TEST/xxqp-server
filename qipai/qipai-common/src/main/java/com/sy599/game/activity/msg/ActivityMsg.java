package com.sy599.game.activity.msg;

import java.util.Map;

public class ActivityMsg {
	private int tarVal;
	private Map<Integer, Integer> award;
	private int canGet;

	

	public Map<Integer, Integer> getAward() {
		return award;
	}

	public void setAward(Map<Integer, Integer> award) {
		this.award = award;
	}

	public int getCanGet() {
		return canGet;
	}

	public void setCanGet(int canGet) {
		this.canGet = canGet;
	}

	public int getTarVal() {
		return tarVal;
	}

	public void setTarVal(int tarVal) {
		this.tarVal = tarVal;
	}

}
