package com.sy.sanguo.game.bean;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单信息
 * @author taohuiliang
 * @date 2013-8-28
 * @version v1.0
 */
public class OrderInfo {
	private long id;
	/**玩家平台UID  **/
	private String flat_id;
	/** 平台生成的订单ID **/
	private String order_id;
	/** 服务器ID **/
	private String server_id;
	/**充值金额  **/
	private int order_amount;
	/** 道具ID **/
	private int item_id;
	/** 道具数量 **/
	private int item_num;
	/**平台  **/
	private String platform;
	private Date create_time;
	private String extend;
	private long userId;
	private BigDecimal payMoney;
	private String payPf;

	private String payType;//支付平台：微信、支付宝

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	public String getPayPf() {
		return payPf;
	}

	public void setPayPf(String payPf) {
		this.payPf = payPf;
	}

	public BigDecimal getPayMoney() {
		return payMoney;
	}

	public void setPayMoney(BigDecimal payMoney) {
		this.payMoney = payMoney;
	}

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
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
	public int getOrder_amount() {
		return order_amount;
	}
	public void setOrder_amount(int orderAmount) {
		order_amount = orderAmount;
	}
	public int getItem_id() {
		return item_id;
	}
	public void setItem_id(int itemId) {
		item_id = itemId;
	}
	public int getItem_num() {
		return item_num;
	}
	public void setItem_num(int itemNum) {
		item_num = itemNum;
	}
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	public Date getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Date createTime) {
		create_time = createTime;
	}
	public void setExtend(String extend) {
		this.extend = extend;
	}
	public String getExtend() {
		return extend;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
}
