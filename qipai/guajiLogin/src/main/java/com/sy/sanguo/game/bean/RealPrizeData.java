package com.sy.sanguo.game.bean;

import java.sql.Timestamp;

/**
 * Created by 35829 on 2017/6/6.
 */
public class RealPrizeData {
    private long userId;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getPrize() {
        return prize;
    }

    public void setPrize(String prize) {
        this.prize = prize;
    }

    public String getName() {
        return name;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public void setName(String name) {

        this.name = name;
    }

    private String create_time;
    private String prize;
    private String name;
}
