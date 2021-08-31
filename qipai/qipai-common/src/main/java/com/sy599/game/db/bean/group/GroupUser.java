package com.sy599.game.db.bean.group;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;

public class GroupUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long keyId;
    private Integer groupId;
    private String groupName;
    private Long userId;
    private String userName;
    private String userNickname;
    private Integer userLevel;
    private Integer playCount1;
    private Integer playCount2;
    private Date createdTime;
    private Long inviterId;
    private Integer userRole;
    private Integer userGroup;
    private long credit;
    private Long creditPurse = 0L;
    private long tempCredit;
    private Long promoterId = 0L;
    private int promoterLevel;
    private long promoterId1;
    private long promoterId2;
    private long promoterId3;
    private long promoterId4;
    private Long promoterId5 = 0L;
    private Long promoterId6 = 0L;
    private Long promoterId7 = 0L;
    private Long promoterId8 = 0L;
    private Long promoterId9 = 0L;
    private Long promoterId10 = 0L;
    private int creditCommissionRate;
    private Integer refuseInvite;
    private Integer creditLock;
    private String ext;
    private int isBjdNewer;
    private Long exp;
    private Long totalExp;
    private Integer level;
    private Long creditExpToday;
    private Integer frameId;
    private Long score;
    private JSONObject extJson;
    /**
     * 为1时为内陪玩  2为可调摸牌
     */
    private int isSpy;

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public Integer getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(Integer userLevel) {
        this.userLevel = userLevel;
    }

    public Integer getPlayCount1() {
        return playCount1;
    }

    public void setPlayCount1(Integer playCount1) {
        this.playCount1 = playCount1;
    }

    public Integer getPlayCount2() {
        return playCount2;
    }

    public void setPlayCount2(Integer playCount2) {
        this.playCount2 = playCount2;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Long getInviterId() {
        return inviterId;
    }

    public void setInviterId(Long inviterId) {
        this.inviterId = inviterId;
    }

    public Integer getUserRole() {
        return userRole;
    }

    public void setUserRole(Integer userRole) {
        this.userRole = userRole;
    }

    public long getCredit() {
        return credit;
    }

    public void setCredit(long credit) {
        this.credit = credit;
    }

    public Long getCreditPurse() {
        return creditPurse;
    }

    public void setCreditPurse(Long creditPurse) {
        this.creditPurse = creditPurse;
    }

    public long getTempCredit() {
        return tempCredit;
    }

    public void setTempCredit(long tempCredit) {
        this.tempCredit = tempCredit;
    }

    public Integer getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(Integer userGroup) {
        this.userGroup = userGroup;
    }

    public Long getPromoterId() {
        return promoterId;
    }

    public void setPromoterId(Long promoterId) {
        this.promoterId = promoterId;
    }

    public int getPromoterLevel() {
        return promoterLevel;
    }

    public void setPromoterLevel(int promoterLevel) {
        this.promoterLevel = promoterLevel;
    }

    public long getPromoterId1() {
        return promoterId1;
    }

    public void setPromoterId1(long promoterId1) {
        this.promoterId1 = promoterId1;
    }

    public long getPromoterId2() {
        return promoterId2;
    }

    public void setPromoterId2(long promoterId2) {
        this.promoterId2 = promoterId2;
    }

    public long getPromoterId3() {
        return promoterId3;
    }

    public void setPromoterId3(long promoterId3) {
        this.promoterId3 = promoterId3;
    }

    public long getPromoterId4() {
        return promoterId4;
    }

    public void setPromoterId4(long promoterId4) {
        this.promoterId4 = promoterId4;
    }

    public Long getPromoterId5() {
        return promoterId5;
    }

    public void setPromoterId5(Long promoterId5) {
        this.promoterId5 = promoterId5;
    }

    public Long getPromoterId6() {
        return promoterId6;
    }

    public void setPromoterId6(Long promoterId6) {
        this.promoterId6 = promoterId6;
    }

    public Long getPromoterId7() {
        return promoterId7;
    }

    public void setPromoterId7(Long promoterId7) {
        this.promoterId7 = promoterId7;
    }

    public Long getPromoterId8() {
        return promoterId8;
    }

    public void setPromoterId8(Long promoterId8) {
        this.promoterId8 = promoterId8;
    }

    public Long getPromoterId9() {
        return promoterId9;
    }

    public void setPromoterId9(Long promoterId9) {
        this.promoterId9 = promoterId9;
    }

    public Long getPromoterId10() {
        return promoterId10;
    }

    public void setPromoterId10(Long promoterId10) {
        this.promoterId10 = promoterId10;
    }

    public int getCreditCommissionRate() {
        return creditCommissionRate;
    }

    public void setCreditCommissionRate(int creditCommissionRate) {
        this.creditCommissionRate = creditCommissionRate;
    }

	public Integer getRefuseInvite() {
		return refuseInvite;
	}

	public void setRefuseInvite(Integer refuseInvite) {
		this.refuseInvite = refuseInvite;
	}

    public Integer getCreditLock() {
        return creditLock;
    }

    public void setCreditLock(Integer creditLock) {
        this.creditLock = creditLock;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
        if (StringUtils.isNotBlank(ext)&&ext.startsWith("{")&&ext.endsWith("}")){
            this.extJson = JSON.parseObject(ext);
        }
    }

    public int getIsBjdNewer() {
        return isBjdNewer;
    }

    public void setIsBjdNewer(int isBjdNewer) {
        this.isBjdNewer = isBjdNewer;
    }

    public Long getExp() {
        return exp;
    }

    public void setExp(Long exp) {
        this.exp = exp;
    }

    public Long getTotalExp() {
        return totalExp;
    }

    public void setTotalExp(Long totalExp) {
        this.totalExp = totalExp;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Long getCreditExpToday() {
        return creditExpToday;
    }

    public void setCreditExpToday(Long creditExpToday) {
        this.creditExpToday = creditExpToday;
    }

    public Integer getFrameId() {
        return frameId;
    }

    public void setFrameId(Integer frameId) {
        this.frameId = frameId;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public int getIsSpy() {
        return isSpy;
    }

    public void setIsSpy(int isSpy) {
        this.isSpy = isSpy;
    }

    public JSONObject getExtJson() {
        return extJson;
    }
}
