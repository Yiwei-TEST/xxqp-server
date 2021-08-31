package com.sy599.game.gold;

import com.sy599.game.db.bean.group.GroupUser;

public class GroupGoldLog {
    private long fromUserId;
    private GroupUser groupUser;
    private long destUserId;
    private long value;
    private int type;

    public GroupGoldLog(int type, long fromUserId, GroupUser groupUser, long destUserId, long value) {
        this.type = type;
        this.fromUserId = fromUserId;
        this.groupUser = groupUser;
        this.destUserId = destUserId;
        this.value = value;
    }

    public long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public GroupUser getGroupUser() {
        return groupUser;
    }

    public void setGroupUser(GroupUser groupUser) {
        this.groupUser = groupUser;
    }

    public long getDestUserId() {
        return destUserId;
    }

    public void setDestUserId(long destUserId) {
        this.destUserId = destUserId;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
