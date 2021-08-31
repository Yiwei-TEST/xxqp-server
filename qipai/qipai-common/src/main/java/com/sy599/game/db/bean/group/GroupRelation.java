package com.sy599.game.db.bean.group;

/**
 * 俱乐部小组
 */
public class GroupRelation {
    Long keyId;
    String groupKey;
    String teamName;
    Integer teamGroup;
    Integer creditCommissionRate;

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Integer getTeamGroup() {
        return teamGroup;
    }

    public void setTeamGroup(Integer teamGroup) {
        this.teamGroup = teamGroup;
    }

    public Integer getCreditCommissionRate() {
        return creditCommissionRate;
    }

    public void setCreditCommissionRate(Integer creditCommissionRate) {
        this.creditCommissionRate = creditCommissionRate;
    }
}
