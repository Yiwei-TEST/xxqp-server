package com.sy599.game.credit;

import com.sy599.game.db.bean.group.GroupUser;

public class CreditCommission {

    private GroupUser groupUser;
    private long destUserId;
    private long credit;
    private boolean addCount = true;

    public CreditCommission(GroupUser groupUser, long destUserId, long credit) {
        this.groupUser = groupUser;
        this.destUserId = destUserId;
        this.credit = credit;
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

    public long getCredit() {
        return credit;
    }

    public void setCredit(long credit) {
        this.credit = credit;
    }

    public boolean isAddCount() {
        return addCount;
    }

    public void setAddCount(boolean addCount) {
        this.addCount = addCount;
    }
}
