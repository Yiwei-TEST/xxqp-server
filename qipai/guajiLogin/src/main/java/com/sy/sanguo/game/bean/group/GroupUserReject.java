package com.sy.sanguo.game.bean.group;

import java.util.Date;

public class GroupUserReject {
    private static final long serialVersionUID = 1L;

    private Long keyId;
    private Long groupId;
    private Long userIdKey;
    private Long userId1;
    private Long userId2;
    private Date createdTime;
    private String userIdKeyStr;

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

    public Long getUserIdKey() {
        return userIdKey;
    }

    public void setUserIdKey(Long userIdKey) {
        this.userIdKey = userIdKey;
    }

    public Long getUserId1() {
        return userId1;
    }

    public void setUserId1(Long userId1) {
        this.userId1 = userId1;
    }

    public Long getUserId2() {
        return userId2;
    }

    public void setUserId2(Long userId2) {
        this.userId2 = userId2;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getUserIdKeyStr() {
        return userIdKeyStr;
    }

    public void setUserIdKeyStr(String userIdKeyStr) {
        this.userIdKeyStr = userIdKeyStr;
    }
}
