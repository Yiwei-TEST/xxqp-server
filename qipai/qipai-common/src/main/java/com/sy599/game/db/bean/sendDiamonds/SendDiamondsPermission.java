package com.sy599.game.db.bean.sendDiamonds;

import java.util.Date;

/**
 * 送钻权限实体
 * 2020年10月24日 15:09:49
 */
public class SendDiamondsPermission {
    private int id;
    private long userid;
    private int permissionState;
    private int isAdmin;
    private Date createdTime;
    private Date  modifiedTime;

    public long getUserid() {
        return userid;
    }

    public void setUserid(long userid) {
        this.userid = userid;
    }

    public int getPermissionState() {
        return permissionState;
    }

    public void setPermissionState(int permissionState) {
        this.permissionState = permissionState;
    }

    public boolean getIsAdmin() {
        return isAdmin==1;
    }

    public void setIsAdmin(int isAdmin) {
        this.isAdmin = isAdmin;
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
