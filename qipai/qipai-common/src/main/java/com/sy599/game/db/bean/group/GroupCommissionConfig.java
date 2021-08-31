package com.sy599.game.db.bean.group;

import java.io.Serializable;
import java.util.Date;

public class GroupCommissionConfig implements Serializable {

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
     * 下级用户id
     */
    private Long nextUserId;

    /**
     * 档位序号
     */
    private Integer seq;

    /**
     * 区间最小值
     */
    private Long minCredit;

    /**
     * 区间最大值
     */
    private Long maxCredit;

    /**
     * 配置的数值
     */
    private Long credit;

    /**
     * 记录修改时允许最大分值
     */
    private Long maxCreditLog;

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

    public Long getNextUserId() {
        return nextUserId;
    }

    public void setNextUserId(Long nextUserId) {
        this.nextUserId = nextUserId;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public Long getMinCredit() {
        return minCredit;
    }

    public void setMinCredit(Long minCredit) {
        this.minCredit = minCredit;
    }

    public Long getMaxCredit() {
        return maxCredit;
    }

    public void setMaxCredit(Long maxCredit) {
        this.maxCredit = maxCredit;
    }

    public Long getCredit() {
        return credit;
    }

    public void setCredit(Long credit) {
        this.credit = credit;
    }

    public Long getMaxCreditLog() {
        return maxCreditLog;
    }

    public void setMaxCreditLog(Long maxCreditLog) {
        this.maxCreditLog = maxCreditLog;
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
