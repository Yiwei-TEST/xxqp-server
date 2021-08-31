package com.sy599.game.db.bean.gold;

import java.util.Date;

/**
 * 金币场活动配置bean
 * 2020年6月20日 15:30:45
 * butao
 */
public class GoldAcitivityRankResult {
    private long rowno ;
    private long userid;
    private long activityItemNum ;
    private String name;
    private String headimgurl;
    private Date modifiedTime ;

    public long getRowno() {
        return rowno;
    }

    public void setRowno(long rowno) {
        this.rowno = rowno;
    }

    public long getUserid() {
        return userid;
    }

    public void setUserid(long userid) {
        this.userid = userid;
    }

    public long getActivityItemNum() {
        return activityItemNum;
    }

    public void setActivityItemNum(long activityItemNum) {
        this.activityItemNum = activityItemNum;
    }

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

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    @Override
    public String toString() {
            StringBuffer sb = new StringBuffer("");
            sb.append(" rowno=").append(getRowno())
              .append(" userid=").append(getUserid())
           .append(" getActivityItemnum=").append(getActivityItemNum())
            .append(" name=").append(getName())
            .append(" headurl=").append(getHeadimgurl());
            return sb.toString();
    }
}
