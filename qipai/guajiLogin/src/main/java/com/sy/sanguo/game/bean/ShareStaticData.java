package com.sy.sanguo.game.bean;

import java.util.Date;

/**
 * 分享统计数据
 */
public class ShareStaticData {

    /**
     * 玩家ID
     */
    private long userId;

    /**
     * 分享时间
     */
    private Date shareDate;

    /**
     * 分享类型（0好友/群  1朋友圈）
     */
    private int shareType;

    /**
     * 分享来源 0未知 1表示瓜分红包活动
     */
    private int sourceType;

    public ShareStaticData() {
    }

    public ShareStaticData(long userId, Date shareDate, int shareType, int sourceType) {
        this.userId = userId;
        this.shareDate = shareDate;
        this.shareType = shareType;
        this.sourceType = sourceType;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Date getShareDate() {
        return shareDate;
    }

    public void setShareDate(Date shareDate) {
        this.shareDate = shareDate;
    }

    public int getShareType() {
        return shareType;
    }

    public void setShareType(int shareType) {
        this.shareType = shareType;
    }

    public int getSourceType() {
        return sourceType;
    }

    public void setSourceType(int sourceType) {
        this.sourceType = sourceType;
    }
}
