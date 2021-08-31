package com.sy599.game.db.bean;

import java.util.Date;

public class RoomCard {
	
	private Integer userId;
	
	private String userName;
	
	private Integer parentId;
	
	private Integer agencyId;
	
	private Integer commonCard;
	
	private Integer freeCard;
	
	private Date createTime;
	
	private String remark;
	
	private String agencyPhone;
	
	private String agencyWechat;
	
	private String bankName;
	
	private String bankCard;
	
	private String agencyComment;
	
	private Integer partAdmin;
	
	private Date updateTime;

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public Integer getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(Integer agencyId) {
		this.agencyId = agencyId;
	}

	public Integer getCommonCard() {
		return commonCard==null?0:commonCard;
	}

	public void setCommonCard(Integer commonCard) {
		this.commonCard = commonCard;
	}

	public Integer getFreeCard() {
		return freeCard==null?0:freeCard;
	}

	public void setFreeCard(Integer freeCard) {
		this.freeCard = freeCard;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getAgencyPhone() {
		return agencyPhone;
	}

	public void setAgencyPhone(String agencyPhone) {
		this.agencyPhone = agencyPhone;
	}

	public String getAgencyWechat() {
		return agencyWechat;
	}

	public void setAgencyWechat(String agencyWechat) {
		this.agencyWechat = agencyWechat;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getBankCard() {
		return bankCard;
	}

	public void setBankCard(String bankCard) {
		this.bankCard = bankCard;
	}

	public String getAgencyComment() {
		return agencyComment;
	}

	public void setAgencyComment(String agencyComment) {
		this.agencyComment = agencyComment;
	}

	public Integer getPartAdmin() {
		return partAdmin==null?0:partAdmin;
	}

	public void setPartAdmin(Integer partAdmin) {
		this.partAdmin = partAdmin;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
}
