package com.sy.sanguo.game.bean.group;

import java.io.Serializable;
import java.util.Date;

public class GroupCreditConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long keyId;

    /**
     * 军团id
     */
    private Long groupId;

    /**
     * 上级用户id
     */
    private Long preUserId;
    /**
     * 用户id
     */
    private Long userId;

    /**
     * t_group_table_config.keyId
     */
    private Long configId;

    /**
     * 配置的数值
     */
    private Integer credit;

    /**
     * 记录修改时允许最大分值
     */
    private Integer maxCreditLog;

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

    public Long getPreUserId() {
        return preUserId;
    }

    public void setPreUserId(Long preUserId) {
        this.preUserId = preUserId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public Integer getCredit() {
        return credit;
    }

    public void setCredit(Integer credit) {
        this.credit = credit;
    }

    public Integer getMaxCreditLog() {
        return maxCreditLog;
    }

    public void setMaxCreditLog(Integer maxCreditLog) {
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
