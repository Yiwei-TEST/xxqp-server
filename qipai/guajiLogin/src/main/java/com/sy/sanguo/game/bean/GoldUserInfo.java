package com.sy.sanguo.game.bean;

public class GoldUserInfo {
    private static final long serialVersionUID = 1L;
    private long userId;
    private String userName;
    private String userNickName;
    private int sex;
    private String headimgurl;
    private String headimgraw;
    private int playCount;
    private int playCountWin;
    private int playCountLose;
    private int playCountEven;
    private long freeGold;
    private long gold;
    private long usedGold;
    private long exp;
    private long vipexp;
    private String signature;
    private String extend;

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
}
