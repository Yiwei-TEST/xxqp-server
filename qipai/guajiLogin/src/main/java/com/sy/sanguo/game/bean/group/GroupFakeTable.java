package com.sy.sanguo.game.bean.group;


import java.io.Serializable;

/**
 * 俱乐部房间配置
 */
public class GroupFakeTable extends GroupTableConfig implements Serializable  {

    private static final long serialVersionUID = 1L;

    /**
     * 绑定的玩法配置Id
     */
    private Long configId;

    /**
     * 打到该局数就切新大局
     */
    private Integer overCount;

    /**
     * 当前打到的局数
     */
    private Integer playedBureau;

    /**
     * 下次局数刷新时间戳
     */
    private Long roundRefrshTime;


    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public Integer getOverCount() {
        return overCount;
    }

    public void setOverCount(Integer overCount) {
        this.overCount = overCount;
    }

    public Integer getPlayedBureau() {
        return playedBureau;
    }

    public void setPlayedBureau(Integer playedBureau) {
        this.playedBureau = playedBureau;
    }

    public Long getRoundRefrshTime() {
        return roundRefrshTime;
    }

    public void setRoundRefrshTime(Long roundRefrshTime) {
        this.roundRefrshTime = roundRefrshTime;
    }
}
