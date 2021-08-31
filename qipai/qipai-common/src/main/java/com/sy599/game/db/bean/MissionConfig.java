package com.sy599.game.db.bean;

import java.io.Serializable;

public class MissionConfig implements Serializable{
    private int id;
    private int tag;
    private String missionExplain;
    private String awardExplain;
    private int type;
    private int finishNum;
    private int awardId;
    private int awardIcon;
    private int awardNum;
    private String ext;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public String getMissionExplain() {
        return missionExplain;
    }

    public void setMissionExplain(String missionExplain) {
        this.missionExplain = missionExplain;
    }

    public String getAwardExplain() {
        return awardExplain;
    }

    public void setAwardExplain(String awardExplain) {
        this.awardExplain = awardExplain;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getFinishNum() {
        return finishNum;
    }

    public void setFinishNum(int finishNum) {
        this.finishNum = finishNum;
    }

    public int getAwardId() {
        return awardId;
    }

    public void setAwardId(int awardId) {
        this.awardId = awardId;
    }

    public int getAwardIcon() {
        return awardIcon;
    }

    public void setAwardIcon(int awardIcon) {
        this.awardIcon = awardIcon;
    }

    public int getAwardNum() {
        return awardNum;
    }

    public void setAwardNum(int awardNum) {
        this.awardNum = awardNum;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }
}
