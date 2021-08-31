package com.sy.sanguo.game.msg;

public class UserPlayMsg {
	private long userId;
	private String name;
	private int point;
	private int totalPoint;
	private int sex;
	private int bopiPoint;
	
	private int group; //他的分组
	private int mingci; //他的名次
	private int jiangli;
	private int winGroup; //
	private int dtzOrXiScore; //
	private int winLossPoint;
	private int allHuxi;
	private String ext;
	private String icon;

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public int getMingci() {
		return mingci;
	}

	public void setMingci(int mingci) {
		this.mingci = mingci;
	}

	public int getJiangli() {
		return jiangli;
	}

	public void setJiangli(int jiangli) {
		this.jiangli = jiangli;
	}

	public int getWinGroup() {
		return winGroup;
	}

	public void setWinGroup(int winGroup) {
		this.winGroup = winGroup;
	}

	public int getDtzOrXiScore() {
		return dtzOrXiScore;
	}

	public void setDtzOrXiScore(int dtzOrXiScore) {
		this.dtzOrXiScore = dtzOrXiScore;
	}

	public int getWinLossPoint() {
		return winLossPoint;
	}

	public void setWinLossPoint(int winLossPoint) {
		this.winLossPoint = winLossPoint;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public int getTotalPoint() {
		return totalPoint;
	}

	public void setTotalPoint(int totalPoint) {
		this.totalPoint = totalPoint;
	}

	public int getBopiPoint() {
		return bopiPoint;
	}

	public void setBopiPoint(int bopiPoint) {
		this.bopiPoint = bopiPoint;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}


	public int getAllHuxi() {
		return allHuxi;
	}

	public void setAllHuxi(int allHuxi) {
		this.allHuxi = allHuxi;
	}
}
