package com.sy.sanguo.game.bean;

import java.io.Serializable;
import java.util.Date;

public class UserRelation implements Serializable {
    private Long keyId;
    private String gameCode;
    private String userId;
    private String regPf;
    private String loginPf;
    private Date regTime;
    private Date loginTime;

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public String getGameCode() {
        return gameCode;
    }

    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRegPf() {
        return regPf;
    }

    public void setRegPf(String regPf) {
        this.regPf = regPf;
    }

    public String getLoginPf() {
        return loginPf;
    }

    public void setLoginPf(String loginPf) {
        this.loginPf = loginPf;
    }

    public Date getRegTime() {
        return regTime;
    }

    public void setRegTime(Date regTime) {
        this.regTime = regTime;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }
}
