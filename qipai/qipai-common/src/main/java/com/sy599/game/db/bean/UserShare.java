package com.sy599.game.db.bean;

import java.util.Date;

public class UserShare {
    public static final int type_normal = 1;
    public static final int type_gold_room = 2;

    private long userId;
    private Date shareDate;
    private int diamond;
    private String extend;
    private int type = 1;

    public UserShare() {
    }

    /**
     * @param userId
     * @param shareDate
     * @param diamond
     * @param extend
     */
    public UserShare(long userId, Date shareDate, int diamond, String extend) {
        super();
        this.userId = userId;
        this.shareDate = shareDate;
        this.diamond = diamond;
        this.extend = extend;
        this.type = type_normal;
    }

    public UserShare(long userId, int type, Date shareDate, int diamond, String extend) {
        this.userId = userId;
        this.shareDate = shareDate;
        this.diamond = diamond;
        this.extend = extend;
        this.type = type;
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

    public int getDiamond() {
        return diamond;
    }

    public void setDiamond(int diamond) {
        this.diamond = diamond;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
