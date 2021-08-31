package com.sy.sanguo.game.bean.group;

import java.io.Serializable;
import java.util.Date;

public class GroupWarn implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer groupId;
    private Long userId;
    private Integer warnScore;
    private Integer warnSwitch;
    private Long createTime;
    private Date updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getWarnScore() {
        return warnScore;
    }

    public void setWarnScore(Integer warnScore) {
        this.warnScore = warnScore;
    }

    public Integer getWarnSwitch() {
        return warnSwitch;
    }

    public void setWarnSwitch(Integer warnSwitch) {
        this.warnSwitch = warnSwitch;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
