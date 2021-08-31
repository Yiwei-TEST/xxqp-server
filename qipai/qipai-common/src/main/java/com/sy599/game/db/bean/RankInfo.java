package com.sy599.game.db.bean;

import com.sy599.game.msg.serverPacket.RankMsg;

/**
 * 排行榜信息
 */
public class RankInfo implements Comparable<RankInfo>{

    /**
     * 排行名次
     */
    private int rank;
    /**
     * 头像
     */
    private String icon;
    /**
     * 昵称
     */
    private String name;
    /**
     * 玩家ID
     */
    private int userId;
    /**
     * 具体分数
     */
    private int number;

    public RankInfo(int rank, String icon, String name, int userId, int number) {
        this.rank = rank;
        this.icon = icon;
        this.name = name;
        this.userId = userId;
        this.number = number;
    }

    public RankMsg.RankInfo.Builder getTaskInfoBuilder() {
        RankMsg.RankInfo.Builder info = RankMsg.RankInfo.newBuilder();
        info.setRank(rank);
        if(icon != null)
            info.setIcon(icon);
        else
            info.setIcon("");
        if(name != null)
            info.setName(name);
        else
            info.setName("");
        info.setUserId(userId);
        info.setNumber(number);
        return info;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public int compareTo(RankInfo o) {
        if(this.rank > o.rank)
            return 1;
        else {
            return -1;
        }
    }
}
