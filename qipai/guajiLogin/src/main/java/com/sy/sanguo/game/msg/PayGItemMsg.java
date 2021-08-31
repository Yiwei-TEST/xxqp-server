package com.sy.sanguo.game.msg;

public class PayGItemMsg {
	private int id;
	private String desc;
	private int amount;
	private int type;
	private int roomCards;
	private int specialGive;
	private String name;
	private int isDouble;
	private int order;

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getSpecialGive() {
		return specialGive;
	}

	public void setSpecialGive(int specialGive) {
		this.specialGive = specialGive;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIsDouble() {
		return isDouble;
	}

	public void setIsDouble(int isDouble) {
		this.isDouble = isDouble;
	}

	public int getRoomCards() {
		return roomCards;
	}

	public void setRoomCards(int roomCards) {
		this.roomCards = roomCards;
	}

}
