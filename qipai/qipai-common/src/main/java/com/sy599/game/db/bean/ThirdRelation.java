package com.sy599.game.db.bean;

import java.io.Serializable;
import java.util.Date;

public class ThirdRelation implements Serializable {
    private Long keyId;
    private Long userId;
    private String thirdPf;
    private String thirdId;
    private String currentState;
    private Date createdTime;
    private Date checkedTime;

    public ThirdRelation() {

    }

    public ThirdRelation(Long userId, String thirdPf, String thirdId) {
        this.userId = userId;
        this.thirdPf = thirdPf;
        this.thirdId = thirdId;
        this.currentState = "1";
        createdTime = new Date();
        checkedTime = new Date();
    }


    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getThirdPf() {
        return thirdPf;
    }

    public void setThirdPf(String thirdPf) {
        this.thirdPf = thirdPf;
    }

    public String getThirdId() {
        return thirdId;
    }

    public void setThirdId(String thirdId) {
        this.thirdId = thirdId;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getCheckedTime() {
        return checkedTime;
    }

    public void setCheckedTime(Date checkedTime) {
        this.checkedTime = checkedTime;
    }
}
