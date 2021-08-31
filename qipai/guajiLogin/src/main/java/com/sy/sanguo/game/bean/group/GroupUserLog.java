package com.sy.sanguo.game.bean.group;

import java.io.Serializable;

public class GroupUserLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long keyId;
    private long groupId;
    private Long userId;
    private Long credit = 0L;

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCredit() {
        return credit;
    }

    public void setCredit(Long credit) {
        this.credit = credit;
    }
}
