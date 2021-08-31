package com.sy.sanguo.game.bean.group;

public class LogGroupUserAlert {

    private static final long serialVersionUID = 1L;

    private long keyId;
    private long groupId;
    private long userId;
    private long optUserId;
    private int type;

    public long getKeyId() {
        return keyId;
    }

    public void setKeyId(long keyId) {
        this.keyId = keyId;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getOptUserId() {
        return optUserId;
    }

    public void setOptUserId(long optUserId) {
        this.optUserId = optUserId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
