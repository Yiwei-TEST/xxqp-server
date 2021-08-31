package com.sy599.game.db.bean.group;

import java.util.Date;

public class SysGroupUserLevelConfig {

    private static final long serialVersionUID = 1L;

    Long keyId;
    Integer level;
    String name;
    Long exp;
    Long totalExp;
    Long creditExpLimit;
    Long playExp;
    String headimgList;
    Integer goldRate;
    Date createdTime;
    Date lastUpTime;

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Long getCreditExpLimit() {
        return creditExpLimit;
    }

    public void setCreditExpLimit(Long creditExpLimit) {
        this.creditExpLimit = creditExpLimit;
    }

    public Long getPlayExp() {
        return playExp;
    }

    public void setPlayExp(Long playExp) {
        this.playExp = playExp;
    }

    public String getHeadimgList() {
        return headimgList;
    }

    public void setHeadimgList(String headimgList) {
        this.headimgList = headimgList;
    }

    public Integer getGoldRate() {
        return goldRate;
    }

    public void setGoldRate(Integer goldRate) {
        this.goldRate = goldRate;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getLastUpTime() {
        return lastUpTime;
    }

    public void setLastUpTime(Date lastUpTime) {
        this.lastUpTime = lastUpTime;
    }
}
