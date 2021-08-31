package com.sy.sanguo.game.bean.gold;

import java.util.Date;

public class GoldPlayer {
    private static final long serialVersionUID = 1L;
    private long userId;
    private String userName;
    /*** 昵称*/
    private String userNickName;
    private int sex;
    /*** 头像*/
    private String headimgurl;
    private String headimgraw;
    /*** 总计小局数*/
    private int playCount;
    /*** 总计胜局数*/
    private int playCountWin;
    /*** 总计败局数*/
    private int playCountLose;
    /*** 总计平局数*/
    private int playCountEven;
    private volatile long freeGold;
    private volatile long gold;
    /*** 消耗过的金币*/
    private volatile long usedGold;
    /*** 经验*/
    private long exp;
    /*** vip经验*/
    private long vipexp;
    /*** 签名*/
    private volatile String signature;
    /*** 扩展*/
    private String extend;
    /*** 补救金领取次数*/
    private int drawRemedyCount;
    /*** 注册时间*/
    private Date regTime;
    /*** 最后登录时间*/
    private Date lastLoginTime;

    /*** 芒果跑得快段位*/
    private int grade;
    /*** 芒果跑得快段位经验值*/
    private int gradeExp;

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public int getGradeExp() {
        return gradeExp;
    }

    public void setGradeExp(int gradeExp) {
        this.gradeExp = gradeExp;
    }

    public Date getRegTime() {
        return regTime;
    }

    public void setRegTime(Date regTime) {
        this.regTime = regTime;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public int getDrawRemedyCount() {
        return drawRemedyCount;
    }

    public void setDrawRemedyCount(int drawRemedyCount) {
        this.drawRemedyCount = drawRemedyCount;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserNickName() {
        return userNickName;
    }

    public void setUserNickName(String userNickName) {
        this.userNickName = userNickName;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getHeadimgurl() {
        return headimgurl;
    }

    public void setHeadimgurl(String headimgurl) {
        this.headimgurl = headimgurl;
    }

    public String getHeadimgraw() {
        return headimgraw;
    }

    public void setHeadimgraw(String headimgraw) {
        this.headimgraw = headimgraw;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public int getPlayCountWin() {
        return playCountWin;
    }

    public void setPlayCountWin(int playCountWin) {
        this.playCountWin = playCountWin;
    }

    public int getPlayCountLose() {
        return playCountLose;
    }

    public void setPlayCountLose(int playCountLose) {
        this.playCountLose = playCountLose;
    }

    public int getPlayCountEven() {
        return playCountEven;
    }

    public void setPlayCountEven(int playCountEven) {
        this.playCountEven = playCountEven;
    }

    public long getFreeGold() {
        return freeGold;
    }

    public void setFreeGold(long freeGold) {
        this.freeGold = freeGold;
    }

    public long getGold() {
        return gold;
    }

    public void setGold(long gold) {
        this.gold = gold;
    }

    public long getUsedGold() {
        return usedGold;
    }

    public void setUsedGold(long usedGold) {
        this.usedGold = usedGold;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public long getVipexp() {
        return vipexp;
    }

    public void setVipexp(long vipexp) {
        this.vipexp = vipexp;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    public void changeUsedGold(long usedGold) {
        this.usedGold += usedGold;
    }

    public long getAllGold() {
        return freeGold + gold;
    }

    public String getShowGold() {
        return String.valueOf(getAllGold());
    }

    /**
     * 加金币
     */
    public void changeGold(int gold) {
        this.gold += gold;
    }

    public void changeWinCount() {
        playCountWin++;
    }

    public void changeLoseCount() {
        playCountLose++;
    }

    public void changePlayCountEven() {
        playCountEven++;
    }

    public void changePlayCount() {
        playCount++;
    }

    public void changeGrade() {
        grade++;
    }

}
