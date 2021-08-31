package com.sy599.game.db.bean.gold;

import com.sy599.game.util.LogUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Set;

public class GoldRoomConfig {

    public static final int STATE_UNVALID = 0;
    public static final int STATE_VALID = 1;

    /*** 新手***/
    public static final int LEVEL_NOVICE = 1;
    /*** 初级***/
    public static final int LEVEL_PRIMARY = 2;
    /*** 中级***/
    public static final int LEVEL_MEDIATE = 3;
    /*** 高级***/
    public static final int LEVEL_SENIOR = 4;

    private Long keyId;
    private Integer state;
    private Integer playType;
    private String name;
    private Integer playerCount;
    private Integer totalBureau;
    private String tableMsg;
    private String goldMsg;
    private Long areaId;
    private Integer order;
    private Date createdTime;
    private Date lastUpTime;
    private Integer robotState;
    private String robotHours;
    private Integer level;

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getPlayType() {
        return playType;
    }

    public void setPlayType(Integer playType) {
        this.playType = playType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(Integer playerCount) {
        this.playerCount = playerCount;
    }

    public Integer getTotalBureau() {
        return totalBureau;
    }

    public void setTotalBureau(Integer totalBureau) {
        this.totalBureau = totalBureau;
    }

    public String getTableMsg() {
        return tableMsg;
    }

    public void setTableMsg(String tableMsg) {
        this.tableMsg = tableMsg;
    }

    public String getGoldMsg() {
        return goldMsg;
    }

    public void setGoldMsg(String goldMsg) {
        this.goldMsg = goldMsg;
        initGoldMsg();
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
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

    public Integer getRobotState() {
        return robotState;
    }

    public void setRobotState(Integer robotState) {
        this.robotState = robotState;
    }

    public String getRobotHours() {
        return robotHours;
    }

    public void setRobotHours(String robotHours) {
        this.robotHours = robotHours;
        initRobotHourSet();
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }



    // --------------以下与数据库无关---------------
    /*** 金币场：门票 ***/
    private long ticket = 0;
    /*** 金币场：加入分数限制 ***/
    private long joinLimit = 0;
    /*** 金币场：倍率 ***/
    private long rate = 1;

    public long getJoinLimit() {
        return joinLimit;
    }

    public boolean isValid() {
        return state == STATE_VALID;
    }

    public void initGoldMsg() {
        try {
            if (StringUtils.isNotBlank(goldMsg)) {
                String[] splits = goldMsg.split(",");
                ticket = Long.valueOf(splits[0]);
                joinLimit = Long.valueOf(splits[1]);
                rate = Long.valueOf(splits[2]);
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("" + keyId, e);
        }
    }

    public long getTicket() {
        return ticket;
    }

    public long getRate() {
        return rate;
    }

    public boolean canUseRobot() {
        if (robotState != STATE_VALID) {
            return false;
        }
        if (robotHourSet == null || robotHourSet.size() == 0) {
            return false;
        }
        if (!robotHourSet.contains(TimeUtil.curHour())) {
            return false;
        }
        return true;
    }

    private Set<Integer> robotHourSet;

    private void initRobotHourSet() {
        try {
            if (StringUtils.isNotBlank(robotHours)) {
                robotHourSet = StringUtil.explodeToIntSet(robotHours);
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("" + keyId, e);
        }
    }


    public boolean isNovice() {
        return level != null && level == LEVEL_NOVICE;
    }

    public boolean isPrimary() {
        return level != null && level == LEVEL_PRIMARY;
    }

    public boolean isMediate() {
        return level != null && level == LEVEL_MEDIATE;
    }

    public boolean isSenior() {
        return level != null && level == LEVEL_SENIOR;
    }
}
