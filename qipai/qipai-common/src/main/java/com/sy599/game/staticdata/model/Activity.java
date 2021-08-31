package com.sy599.game.staticdata.model;

import java.util.List;

public class Activity {
	private int id;
	private String name;
	private String conditions;
	private String awardStr;
	private List<Award> awardList;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getConditions() {
		return conditions;
	}

	public void setConditions(String conditions) {
		this.conditions = conditions;
	}

	public String getAwardStr() {
		return awardStr;
	}

	public void setAwardStr(String awardStr) {
		this.awardStr = awardStr;
	}

	public List<Award> getAwardList() {
		return awardList;
	}

	public void setAwardList(List<Award> awardList) {
		this.awardList = awardList;
	}

}
