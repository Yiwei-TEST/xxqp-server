package com.sy.sanguo.game.bean;

import java.sql.Timestamp;

public class WeiXinAuthorization {

	private String unionId;
	
	private int agencyId;

	private Timestamp createTime;
	
	private long inviterId;
	
	private Timestamp inviterTime;
	
	public long getInviterId() {
		return inviterId;
	}

	public void setInviterId(long inviterId) {
		this.inviterId = inviterId;
	}

	public Timestamp getInviterTime() {
		return inviterTime;
	}

	public void setInviterTime(Timestamp inviterTime) {
		this.inviterTime = inviterTime;
	}

	public String getUnionId() {
		return unionId;
	}

	public void setUnionId(String unionId) {
		this.unionId = unionId;
	}

	public int getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(int agencyId) {
		this.agencyId = agencyId;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	
}
