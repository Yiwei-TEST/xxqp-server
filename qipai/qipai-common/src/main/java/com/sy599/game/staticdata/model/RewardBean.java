package com.sy599.game.staticdata.model;

public class RewardBean {

    private int data;
    private int type;  //奖励类型 1钻石 2现金红包
    private float value; //奖励额
    private int status; //状态：0未达成，1：已达成未领取，2：已经达成已经领取
    public RewardBean(){

    }
    public RewardBean(int data, int type, float value) {
        this.data = data;
        this.type = type;
        this.value = value;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
