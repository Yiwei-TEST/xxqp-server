package com.sy599.game.db.bean.gold;

import com.sy599.game.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GoldRoomHall {

    private Long keyId;
    private String name;
    private Integer type;
    private String extMsg;
    private String playTypes;
    private String description;
    private Date createdTime;
    private Date lastUpTime;

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getExtMsg() {
        return extMsg;
    }

    public void setExtMsg(String extMsg) {
        this.extMsg = extMsg;
    }

    public String getPlayTypes() {
        return playTypes;
    }

    public void setPlayTypes(String playTypes) {
        this.playTypes = playTypes;

        if (StringUtils.isNotBlank(playTypes)) {
            playTypeList = StringUtil.explodeToIntList(playTypes);
            if (playTypeList == null) {
                playTypeList = new ArrayList<>();
            }
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getLastUpTime() {
        return lastUpTime;
    }

    public void setLastUpTime(Date lastUpTime) {
        this.lastUpTime = lastUpTime;
    }

    private List<Integer> playTypeList;

    public List<Integer> getPlayTypeList() {
        return playTypeList;
    }
}
