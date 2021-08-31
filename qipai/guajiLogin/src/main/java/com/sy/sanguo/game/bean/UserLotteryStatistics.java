package com.sy.sanguo.game.bean;

public class UserLotteryStatistics {

	private int recordDate;
	private long userId;
	private String userName;
	private int record1;
	private int record2;
	private int record3;
	
	public UserLotteryStatistics(){}
	
	public UserLotteryStatistics(int recordDate, long userId, String userName, int type, int record) {
		super();
		this.recordDate = recordDate;
		this.userName = userName;
		this.userId = userId;
		switch(type){
			case 1:
				this.record1 = record;
				break;
			case 2:
				this.record2 = record;
				break;	
			case 3:
				this.record3 = record;
				break;	
		}
	}
	
	public int getRecordDate() {
		return recordDate;
	}
	public void setRecordDate(int recordDate) {
		this.recordDate = recordDate;
	}
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getRecord1() {
		return record1;
	}
	public void setRecord1(int record1) {
		this.record1 = record1;
	}
	public int getRecord2() {
		return record2;
	}
	public void setRecord2(int record2) {
		this.record2 = record2;
	}
	public int getRecord3() {
		return record3;
	}
	public void setRecord3(int record3) {
		this.record3 = record3;
	}
	
}
