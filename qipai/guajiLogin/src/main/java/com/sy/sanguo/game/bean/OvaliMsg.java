package com.sy.sanguo.game.bean;

import com.sy.sanguo.common.server.PayBean;

public class OvaliMsg {
	private int code;
	private PayBean payItem;
	private OrderValidate ov;
	private String msg;
	private Object url;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public OrderValidate getOv() {
		return ov;
	}

	public void setOv(OrderValidate ov) {
		this.ov = ov;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getUrl() {
		return url;
	}

	public void setUrl(Object url) {
		this.url = url;
	}

	public void setPayItem(PayBean payItem) {
		this.payItem = payItem;
	}

	public PayBean getPayItem() {
		return payItem;
	}

}
