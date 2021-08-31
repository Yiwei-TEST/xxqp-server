package com.sy.sanguo.game.bean;

import java.util.Date;

public class OrderValidate {
	private String flat_id;
	private String order_id;
	private String server_id;
	private String pf;
	private int item_id;
	private String pay_channel;
	
	/**
	 * 单位元
	 */
	private int amount;
	private int status;
	private Date create_time;
	private String sdk_order_id;

	private Long userId;//玩家id

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/**
	 * 被代充人id
	 */
	private long agencyUserId;
	
	public long getAgencyUserId() {
		return agencyUserId;
	}

	public void setAgencyUserId(long agencyUserId) {
		this.agencyUserId = agencyUserId;
	}

	public String getFlat_id() {
		return flat_id;
	}

	public void setFlat_id(String flatId) {
		flat_id = flatId;
	}

	public String getOrder_id() {
		return order_id;
	}

	public void setOrder_id(String orderId) {
		order_id = orderId;
	}

	public String getServer_id() {
		return server_id;
	}

	public void setServer_id(String serverId) {
		server_id = serverId;
	}

	public int getItem_id() {
		return item_id;
	}

	public void setItem_id(int itemId) {
		item_id = itemId;
	}

	public String getPf() {
		return pf;
	}

	public void setPf(String pf) {
		this.pf = pf;
	}

	public String getPay_channel() {
		return pay_channel;
	}

	public void setPay_channel(String payChannel) {
		pay_channel = payChannel;
	}

	/**
	 * 单位元
	 * 
	 * @return
	 */
	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Date createTime) {
		create_time = createTime;
	}

	public String getSdk_order_id() {
		return sdk_order_id;
	}

	public void setSdk_order_id(String sdk_order_id) {
		this.sdk_order_id = sdk_order_id;
	}
}
