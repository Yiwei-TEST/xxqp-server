package com.sy599.game.db.bean.group;

import java.io.Serializable;
import java.util.Date;

public class GroupReview implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long keyId;
    private Long userId;
    private String userName;
    private Long groupId;
    private String groupName;
    private Integer reviewMode;
    private Integer currentState;
    private Date createdTime;
    private String contentMsg;
    private Long currentOperator;
    private Date operateTime;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getCurrentOperator() {
        return currentOperator;
    }

    public void setCurrentOperator(Long currentOperator) {
        this.currentOperator = currentOperator;
    }

    public Date getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime;
    }

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Integer getReviewMode() {
        return reviewMode;
    }

    public void setReviewMode(Integer reviewMode) {
        this.reviewMode = reviewMode;
    }

    public Integer getCurrentState() {
        return currentState;
    }

    public void setCurrentState(Integer currentState) {
        this.currentState = currentState;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getContentMsg() {
        return contentMsg;
    }

    public void setContentMsg(String contentMsg) {
        this.contentMsg = contentMsg;
    }
}
