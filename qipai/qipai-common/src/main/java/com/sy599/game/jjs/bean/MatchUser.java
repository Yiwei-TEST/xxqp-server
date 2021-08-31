package com.sy599.game.jjs.bean;

import java.util.Date;

public class MatchUser {

    private Long keyId;
    private String userId;
    private Long matchId;
    private String matchType;
    private String currentState;
    private Integer currentNo;
    private Integer currentScore;
    private Date createdTime;
    private Date modifiedTime;
    private Integer userRank;
    private String userAward;
    private String awardState;
    private Integer reliveCount;

    public MatchUser() {
        this.reliveCount = 0;
    }

    public MatchUser(String userId, Long matchId,String matchType, int currentNo) {
        this.matchId = matchId;
        this.matchType = matchType;
        this.userId = userId;
        this.currentNo = currentNo;
        this.currentState = "0";
        this.currentScore = 0;
        this.createdTime = new Date();
        this.modifiedTime = new Date();
        this.userRank = 0;
        this.userAward = "";
        this.awardState = "0";
        this.reliveCount = 0;
    }

    public Integer getReliveCount() {
        return reliveCount;
    }

    public void setReliveCount(Integer reliveCount) {
        this.reliveCount = reliveCount;
    }

    public String getUserAward() {
        return userAward;
    }

    public void setUserAward(String userAward) {
        this.userAward = userAward;
    }

    public String getAwardState() {
        return awardState;
    }

    public void setAwardState(String awardState) {
        this.awardState = awardState;
    }

    public Integer getUserRank() {
        return userRank;
    }

    public void setUserRank(Integer userRank) {
        this.userRank = userRank;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public Integer getCurrentNo() {
        return currentNo;
    }

    public void setCurrentNo(Integer currentNo) {
        this.currentNo = currentNo;
    }

    public Integer getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(Integer currentScore) {
        this.currentScore = currentScore;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
}
