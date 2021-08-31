package com.sy.sanguo.game.bean;

import java.util.Date;

/**
 * Created by pc on 2017/4/20.
 */
public class UserSign {
    private long userId;
    private Date signTime;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Date getSignTime() {
        return signTime;
    }

    public void setSignTime(Date signTime) {
        this.signTime = signTime;
    }
}
