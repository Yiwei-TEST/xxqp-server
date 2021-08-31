package com.sy599.game.db.bean.group;

import java.io.Serializable;
import java.util.Date;

public class GroupConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer keyId;
    private Integer groupLevel;
    private Integer maxCount;
    private Long groupCoin;
    private Date createdTime;

    public Integer getKeyId() {
        return keyId;
    }

    public void setKeyId(Integer keyId) {
        this.keyId = keyId;
    }

    public Integer getGroupLevel() {
        return groupLevel;
    }

    public void setGroupLevel(Integer groupLevel) {
        this.groupLevel = groupLevel;
    }

    public Integer getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }

    public Long getGroupCoin() {
        return groupCoin;
    }

    public void setGroupCoin(Long groupCoin) {
        this.groupCoin = groupCoin;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
}
