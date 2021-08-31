package com.sy599.game.db.bean;
/**
 * 跑得快调牌概率表
 * @author ly
 *
 */
public class PdkRateConfig {

	private int id;
	
	private int type;
	
	private int val;
	
	private String rate;
	
	private String descriptMsg;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getVal() {
		return val;
	}

	public void setVal(int val) {
		this.val = val;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public String getDescriptMsg() {
		return descriptMsg;
	}

	public void setDescriptMsg(String descriptMsg) {
		this.descriptMsg = descriptMsg;
	}
	
	
	
}
