package com.sy599.game.db.bean.group;

import java.io.Serializable;
import java.util.Date;

public class GroupInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /*** 类型：普通***/
    public static final int isCredit_normal = 0;
    /*** 类型：信用模式***/
    public static final int isCredit_credit = 1;
    /*** 类型：金币模式***/
    public static final int isCredit_gold = 2;


    private Long keyId;
    private Integer parentGroup;
    private Integer groupId;
    private String groupName;
    private Integer maxCount;
    private Integer currentCount;
    private Integer groupLevel;
    private Integer groupMode;
    private String extMsg;
    private Date createdTime;
    private Long createdUser;
    private String descMsg;
    private String groupState;
    private Date modifiedTime;
    private Integer isCredit;
    private Integer creditAllotMode;
    private Integer creditRate;
    private Integer switchCoin;
    private Long exp;
    private Long totalExp;
    private Integer level;
    private Long creditExpToday;
    private Integer goldRoomSwitch;
    private Long goldRoomRate;
    private Long isCreditUpTime;

    public Integer getParentGroup() {
        return parentGroup;
    }

    public void setParentGroup(Integer parentGroup) {
        this.parentGroup = parentGroup;
    }

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

    public Integer getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }

    public Integer getCurrentCount() {
        return currentCount;
    }

    public void setCurrentCount(Integer currentCount) {
        this.currentCount = currentCount;
    }

    public Integer getGroupLevel() {
        return groupLevel;
    }

    public void setGroupLevel(Integer groupLevel) {
        this.groupLevel = groupLevel;
    }

    public Integer getGroupMode() {
        return groupMode;
    }

    public void setGroupMode(Integer groupMode) {
        this.groupMode = groupMode;
    }

    public String getExtMsg() {
        return extMsg;
    }

    public void setExtMsg(String extMsg) {
        this.extMsg = extMsg;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Long getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(Long createdUser) {
        this.createdUser = createdUser;
    }

    public String getDescMsg() {
        return descMsg;
    }

    public void setDescMsg(String descMsg) {
        this.descMsg = descMsg;
    }

    public String getGroupState() {
        return groupState;
    }

    public void setGroupState(String groupState) {
        this.groupState = groupState;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Integer getIsCredit() {
        return isCredit;
    }

    public void setIsCredit(Integer isCredit) {
        this.isCredit = isCredit;
    }

    public Integer getCreditAllotMode() {
        return creditAllotMode;
    }

    public void setCreditAllotMode(Integer creditAllotMode) {
        this.creditAllotMode = creditAllotMode;
    }

    public Integer getCreditRate() {
        return creditRate;
    }

    public void setCreditRate(Integer creditRate) {
        this.creditRate = creditRate;
    }

    public Integer getSwitchCoin() {
        return switchCoin;
    }

    public void setSwitchCoin(Integer switchCoin) {
        this.switchCoin = switchCoin;
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

    public Integer getGoldRoomSwitch() {
        return goldRoomSwitch;
    }

    public void setGoldRoomSwitch(Integer goldRoomSwitch) {
        this.goldRoomSwitch = goldRoomSwitch;
    }

    public Long getGoldRoomRate() {
        return goldRoomRate;
    }

    public void setGoldRoomRate(Long goldRoomRate) {
        this.goldRoomRate = goldRoomRate;
    }

    public Long getIsCreditUpTime() {
        return isCreditUpTime;
    }

    public void setIsCreditUpTime(Long isCreditUpTime) {
        this.isCreditUpTime = isCreditUpTime;
    }
}
