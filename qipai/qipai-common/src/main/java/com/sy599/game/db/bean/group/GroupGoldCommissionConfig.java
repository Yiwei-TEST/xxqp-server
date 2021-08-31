package com.sy599.game.db.bean.group;

import java.io.Serializable;
import java.util.Date;

public class GroupGoldCommissionConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long keyId;

    /**
     * 军团id
     */
    private Long groupId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 档位序号
     */
    private Integer seq;

    /**
     * 区间最小值
     */
    private Long minValue;

    /**
     * 区间最大值
     */
    private Long maxValue;

    /**
     * 配置的数值
     */
    private Long value;

    /**
     * 剩余的数值
     */
    private Long leftValue;

    /**
     * 记录修改时允许最大分值
     */
    private Long maxLog;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 最后更新时间
     */
    private Date lastUpTime;

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public Long getMinValue() {
        return minValue;
    }

    public void setMinValue(Long minValue) {
        this.minValue = minValue;
    }

    public Long getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Long maxValue) {
        this.maxValue = maxValue;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public Long getLeftValue() {
        return leftValue;
    }

    public void setLeftValue(Long leftValue) {
        this.leftValue = leftValue;
    }

    public Long getMaxLog() {
        return maxLog;
    }

    public void setMaxLog(Long maxLog) {
        this.maxLog = maxLog;
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
