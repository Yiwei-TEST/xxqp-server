package com.sy599.game.db.bean;

import java.io.Serializable;

public class SevenSignConfig implements Serializable{
    private int id;
    private int dayNum;
    private int goldNum;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDayNum() {
        return dayNum;
    }

    public void setDayNum(int dayNum) {
        this.dayNum = dayNum;
    }

    public int getGoldNum() {
        return goldNum;
    }

    public void setGoldNum(int goldNum) {
        this.goldNum = goldNum;
    }
}
