package com.sy599.game.db.bean;

import java.util.Date;

public class Activity {
    private int id;
    private Date beginTime;
    private Date endTime;
    private String them;
    private String showContent;
    private String extend;
    private Date showBeginTime;
    private Date showEndTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getThem() {
        return them;
    }

    public void setThem(String them) {
        this.them = them;
    }

    public String getShowContent() {
        return showContent;
    }

    public void setShowContent(String showContent) {
        this.showContent = showContent;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    public Date getShowBeginTime() {
        return showBeginTime;
    }

    public void setShowBeginTime(Date showBeginTime) {
        this.showBeginTime = showBeginTime;
    }

    public Date getShowEndTime() {
        return showEndTime;
    }

    public void setShowEndTime(Date showEndTime) {
        this.showEndTime = showEndTime;
    }

    public boolean sysUse(){
        Date date=new Date();
        if(showEndTime!=null&&date.after(showEndTime))
            return false;
        if(showEndTime==null&&date.after(endTime))
            return false;
        return true;
    }

    public boolean show(){
        Date date=new Date();
        if(showBeginTime!=null&&date.before(showBeginTime))
            return false;
        if(showBeginTime==null&&date.before(beginTime))
            return false;
        if(showEndTime!=null&&date.after(showEndTime))
            return false;
        if(showEndTime==null&&date.after(endTime))
            return false;
        return true;
    }

    public boolean isUsing(){
        Date date=new Date();
        if(date.after(beginTime)&&date.before(endTime))
            return true;
        return false;
    }
}
