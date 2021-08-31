package com.sy.sanguo.game.bean;

import java.io.Serializable;
import java.util.Date;

public class UserLottery implements Serializable {

    private static final long serialVersionUID = 4840434212131406568L;
    private long id;

    private long userId;

    private int prizeIndex;

    private String prize;

    private Date createTime;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getPrizeIndex() {
        return prizeIndex;
    }

    public void setPrizeIndex(int prizeIndex) {
        this.prizeIndex = prizeIndex;
    }

    public String getPrize() {
        return prize;
    }

    public void setPrize(String prize) {
        this.prize = prize;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }


}
