package com.sy.sanguo.game.bean.group;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
    /**公告内容*/
    private String content;
    /** 信用分兑换比例*/
    private int creditRate;
    private int switchInvite;
    private int switchCoin; // 亲友圈金币系统：开关：1打开，0不打开
    private long exp;       // 亲友圈金币系统：经验
    private long totalExp;  // 亲友圈金币系统：总经验
    private int level;      // 亲友圈金币系统：等级
    private long creditExpToday;  // 亲友圈金币系统：当天通过下分获得经验值

    private String gameIds;
    Set<Integer> gameIdSet = new HashSet<>();

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

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public Integer getParentGroup() {
        return parentGroup;
    }

    public void setParentGroup(Integer parentGroup) {
        this.parentGroup = parentGroup;
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
    
    public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
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

    public int getCreditRate() {
        return creditRate;
    }

    public void setCreditRate(int creditRate) {
        this.creditRate = creditRate;
    }

    public int getSwitchInvite() {
        return switchInvite;
    }

    public void setSwitchInvite(int switchInvite) {
        this.switchInvite = switchInvite;
    }

    public int getSwitchCoin() {
        return switchCoin;
    }

    public void setSwitchCoin(int switchCoin) {
        this.switchCoin = switchCoin;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public long getTotalExp() {
        return totalExp;
    }

    public void setTotalExp(long totalExp) {
        this.totalExp = totalExp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getCreditExpToday() {
        return creditExpToday;
    }

    public void setCreditExpToday(long creditExpToday) {
        this.creditExpToday = creditExpToday;
    }

    public String getGameIds() {
        return gameIds;
    }

    public void setGameIds(String gameIds) {
        this.gameIds = gameIds;
        if (StringUtils.isNotBlank(gameIds)) {
            String[] splits = gameIds.split(",");
            for (String split : splits) {
                gameIdSet.add(Integer.valueOf(split));
            }
        }
    }

    public Set<Integer> getGameIdSet() {
        return gameIdSet;
    }
}
