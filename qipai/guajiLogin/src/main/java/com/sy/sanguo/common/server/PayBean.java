package com.sy.sanguo.common.server;

import java.util.Date;

import com.sy.sanguo.game.bean.PayItemMsg;
import com.sy.sanguo.game.msg.PayGItemMsg;
import com.sy599.sanguo.util.TimeUtil;

public class PayBean {
	public static final int MONTH_CARD = 1;
	public static final int YUANBAO = 2;

	private int id;
	private String desc;
	private int amount;
	private int type;
	private int yuanbao;
	private int specialGive;
	private int normalGive;
	private int giveCount;
	private String ps;
	private String name;
	private Date beginTime;
	private Date endTime;
	private int order;
	private String payType;

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getId() {
		return id;
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

	public int getType() {
		return type;
	}

	public int getYuanbao() {
		return yuanbao;
	}

	public int getSpecialGive() {
		return specialGive;
	}

	public int getNormalGive() {
		return normalGive;
	}

	public int getGiveCount() {
		return giveCount;
	}

	public String getPs() {
		return ps;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setYuanbao(int yuanbao) {
		this.yuanbao = yuanbao;
	}

	public void setSpecialGive(int specialGive) {
		this.specialGive = specialGive;
	}

	public void setNormalGive(int normalGive) {
		this.normalGive = normalGive;
	}

	public void setGiveCount(int giveCount) {
		this.giveCount = giveCount;
	}

	public void setPs(String ps) {
		this.ps = ps;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PayItemMsg buildMsg() {
		PayItemMsg msg = new PayItemMsg();
		msg.setV(yuanbao);
		msg.setCny(amount);
		msg.setN(name);
		msg.setSend(specialGive);
		return msg;
	}

	public PayGItemMsg buildItemMsg() {
		PayGItemMsg msg = new PayGItemMsg();
		msg.setAmount(amount);
		msg.setDesc(desc);
		msg.setId(id);
		msg.setName(name);
		msg.setRoomCards(yuanbao);
		msg.setSpecialGive(specialGive);
		msg.setType(type);
		return msg;
	}

	public boolean isDouble() {
		if (beginTime == null || endTime == null) {
			return false;
		}
		long now = TimeUtil.currentTimeMillis();
		if (now > beginTime.getTime() && now < endTime.getTime()) {
			return true;
		}
		return false;
		// retirm
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
}
