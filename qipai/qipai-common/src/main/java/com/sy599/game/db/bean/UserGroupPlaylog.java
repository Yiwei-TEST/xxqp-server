package com.sy599.game.db.bean;

import java.io.Serializable;

public class UserGroupPlaylog implements Serializable {
    private long id;
    private long tableid;
    private long userid;
    private int count;
    private String creattime;
    private String players;
    private String score;
    private String overtime;
    private Integer playercount;
    private long groupid;
    private String gamename;
    private int totalCount;
    private String diFenScore;
    private String diFen;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTableid() {
        return tableid;
    }

    public void setTableid(long tableid) {
        this.tableid = tableid;
    }

    public long getUserid() {
        return userid;
    }

    public void setUserid(long userid) {
        this.userid = userid;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getCreattime() {
        return creattime;
    }

    public void setCreattime(String creattime) {
        this.creattime = creattime;
    }

    public String getPlayers() {
        return players;
    }

    public void setPlayers(String players) {
        this.players = players;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getOvertime() {
        return overtime;
    }

    public void setOvertime(String overtime) {
        this.overtime = overtime;
    }

    public Integer getPlayercount() {
        return playercount;
    }

    public void setPlayercount(Integer playercount) {
        this.playercount = playercount;
    }

    public long getGroupid() {
        return groupid;
    }

    public void setGroupid(long groupid) {
        this.groupid = groupid;
    }

    public String getGamename() {
        return gamename;
    }

    public void setGamename(String gamename) {
        this.gamename = gamename;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public String getDiFenScore() {
        return diFenScore;
    }

    public void setDiFenScore(String diFenScore) {
        this.diFenScore = diFenScore;
    }

    public String getDiFen() {
        return diFen;
    }

    public void setDiFen(String diFen) {
        this.diFen = diFen;
    }
}
