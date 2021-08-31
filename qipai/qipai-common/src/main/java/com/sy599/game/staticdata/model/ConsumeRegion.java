package com.sy599.game.staticdata.model;

/**
 * 钻石消费区间表的模型
 * @author zhoufan
 * @date 2013-4-27
 * @version v1.0
 */
public class ConsumeRegion {
	private int id;
	private int region;
	private int amount;
	
	public int getRegion(){
		return this.region;
	}
	
	public void setRegion(int value){
		this.region = value;
	}
	public int getAmount(){
		return this.amount;
	}
	
	public void setAmount(int value){
		this.amount = value;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
