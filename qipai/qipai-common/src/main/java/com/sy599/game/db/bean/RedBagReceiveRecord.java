package com.sy599.game.db.bean;

public class RedBagReceiveRecord implements Comparable<RedBagReceiveRecord> {

    private String userName;

    private float receiveNum;

    public RedBagReceiveRecord() {
    }

    public RedBagReceiveRecord(String userName, float receiveNum) {
        this.userName = userName;
        this.receiveNum = receiveNum;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public float getReceiveNum() {
        return receiveNum;
    }

    public void setReceiveNum(float receiveNum) {
        this.receiveNum = receiveNum;
    }

    @Override
    public int compareTo(RedBagReceiveRecord o) {
        if(this.receiveNum < o.receiveNum)
            return 1;
        else if(this.receiveNum == o.receiveNum)
            return 0;
        else
            return -1;
    }
}
