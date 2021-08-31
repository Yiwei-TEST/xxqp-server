package com.sy599.game.robot;

import com.sy599.game.util.StringUtil;

import java.util.Date;
import java.util.List;

public class Robot {

    public static final int type_goldRoom = 1;

    private Long userId;
    private Integer type;
    private Integer used;
    private Integer usedCount;
    private String generalExt;
    private String playTypes;
    private String hours;
    private Date createTime;
    private Date lastUseTime;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getUsed() {
        return used;
    }

    public void setUsed(Integer used) {
        this.used = used;
    }

    public Integer getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(Integer usedCount) {
        this.usedCount = usedCount;
    }

    public String getGeneralExt() {
        return generalExt;
    }

    public void setGeneralExt(String generalExt) {
        this.generalExt = generalExt;
    }

    public String getPlayTypes() {
        return playTypes;
    }

    public void setPlayTypes(String playTypes) {
        this.playTypes = playTypes;
        initPlayTypeList();
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
        initHourList();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastUseTime() {
        return lastUseTime;
    }

    public void setLastUseTime(Date lastUseTime) {
        this.lastUseTime = lastUseTime;
    }


    // --------------以下不是数据库字段---------------
    private List<Integer> playTypeList;
    private List<Integer> hourList;

    private void initPlayTypeList() {
        playTypeList = StringUtil.explodeToIntList(playTypes);
    }

    private void initHourList() {
        hourList = StringUtil.explodeToIntList(hours);
    }

    public List<Integer> getPlayTypeList() {
        return playTypeList;
    }

    public List<Integer> getHourList() {
        return hourList;
    }
}
