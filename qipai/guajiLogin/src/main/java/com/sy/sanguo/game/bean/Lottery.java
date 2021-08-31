package com.sy.sanguo.game.bean;

/**
 * Created by 35829 on 2017/6/3.
 */
public class Lottery {

    private int index;
    private String name;
    private double chance;
    private int roomCard;
    private int state = 1;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getRoomCard() {
        return roomCard;
    }

    public void setRoomCard(int roomCard) {
        this.roomCard = roomCard;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
