package com.sy599.game.db.bean.gold;

import java.util.Date;

/**
 * 金币场活动用户物品Bean
 * 2020年6月20日 15:26:46
 * butao
 */
public class GoldRoomActivityUserItem {
    private long keyId;
    private long userid;
    private String activityBureau;
    private long activityItemNum;
    private String daterecord;
    private String activityDesc;
    private Date createdTime;
    private Date modifiedTime;
    private int isReward;
    private int everydayLimit;
    private String name;
    private String headimgurl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeadimgurl() {
        return headimgurl;
    }

    public void setHeadimgurl(String headimgurl) {
        this.headimgurl = headimgurl;
    }

    public int getIsReward() {
        return isReward;
    }

    public void setIsReward(int isReward) {
        this.isReward = isReward;
    }

    public long getKeyId() {
        return keyId;
    }

    public void setKeyId(long keyId) {
        this.keyId = keyId;
    }

    public long getUserid() {
        return userid;
    }

    public void setUserid(long userid) {
        this.userid = userid;
    }

    public String getActivityBureau() {
        return activityBureau;
    }

    public void setActivityBureau(String activityBureau) {
        this.activityBureau = activityBureau;
    }

    public long getActivityItemNum() {
        return activityItemNum;
    }

    public void setActivityItemNum(long activityItemNum) {
        this.activityItemNum = activityItemNum;
    }

    public String getDaterecord() {
        return daterecord;
    }

    public void setDaterecord(String daterecord) {
        this.daterecord = daterecord;
    }

    public String getActivityDesc() {
        return activityDesc;
    }

    public void setActivityDesc(String activityDesc) {
        this.activityDesc = activityDesc;
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

    public int getEverydayLimit() {
        return everydayLimit;
    }

    public void setEverydayLimit(int everydayLimit) {
        this.everydayLimit = everydayLimit;
    }

    @Override
    public String toString() {
        StringBuffer  sb = new StringBuffer();
        sb.append(" keyId="+keyId);
        sb.append(" userid="+userid );
        sb.append(" activityBureau="+activityBureau );
        sb.append(" activityItemNum="+activityItemNum );
        sb.append(" daterecord="+daterecord );
        sb.append(" activityDesc="+activityDesc );
        sb.append(" createdTime="+createdTime );
        sb.append(" modifiedTime="+modifiedTime );
        sb.append(" everydayLimit="+everydayLimit );
        return sb.toString();
    }
}
