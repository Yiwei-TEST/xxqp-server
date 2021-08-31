package com.sy599.game.character;

import com.alibaba.fastjson.JSON;
import com.sy599.game.common.bean.Consume;
import com.sy599.game.db.bean.GoldDataStatistics;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.bean.UserGoldRecord;
import com.sy599.game.db.dao.DataStatisticsDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.gold.GoldDao;
import com.sy599.game.db.enums.SourceType;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ServerUtil;

import java.text.SimpleDateFormat;
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

    public boolean changeGold(Consume consume) {
        return changeGold(consume, 1);
    }


    /**
     * 钻石变动
     */
    private synchronized boolean changeGold(Consume consume, int tryCount) {
        try {
            long freeGold = consume.getFreeValue();
            long gold = consume.getValue();
            boolean isWrite = consume.isWrite();
            int playType = consume.getPlayType();
            boolean isRecord = consume.isRecord();
            SourceType sourceType = consume.getSourceType();
            if (sourceType == null) {
                consume.setSourceType(SourceType.unknown);
                sourceType = consume.getSourceType();
            }
            long minusFreeGold = 0; // 减少的免费钻 freeCards
            long minusGold = 0; // 减少的充值钻 cards
            long oldFreeGold = this.freeGold;
            long oldGold = this.gold;
            synchronized (this) {
                if (gold < 0) { //扣钻，只允许会用该值
                    // temp等于绑定房卡 + cards
                    long temp = this.freeGold + gold;
                    if (temp >= 0) {
                        // 房卡足够
                        this.freeGold = temp;
                        minusGold = 0;
                        minusFreeGold = -gold;
                    } else {
                        // 房卡不足，先用完绑定房卡，再用普通房卡
                        this.freeGold = 0;
                        minusGold = -temp;
                        minusFreeGold = (-gold) - minusGold;
                    }
                    minusFreeGold += -freeGold;

                    this.gold -= minusGold;
                    this.freeGold += freeGold;
                } else {
                    minusFreeGold = -freeGold;
                    minusGold = -gold;

                    this.gold += gold;
                    this.freeGold += freeGold;
                }

                if (minusFreeGold > 0 && isRecord) {
                    this.usedGold += -minusFreeGold;
                }
                if (minusGold > 0 && isRecord) {
                    this.usedGold += -minusGold;
                }

                if (minusGold != 0 || minusFreeGold != 0) {
                    if (GoldDao.getInstance().changeUserGold(userId, oldGold, oldFreeGold, -minusGold, -minusFreeGold) > 0) {
                        consume.setOK(true);
                        consume.setFreeValue1(-minusFreeGold);
                        consume.setValue1(-minusGold);

                        long goldChange = -minusGold - minusFreeGold;
                        if (isWrite) {
                            // 推送金币变化消息
                            Player player = PlayerManager.getInstance().getPlayer(userId);
                            if (player != null) {
                                player.writeGoldMessage((oldFreeGold + oldGold), goldChange, getAllGold());
                            } else {
                                try {
                                    RegInfo info = UserDao.getInstance().getUser(userId);
                                    if (info != null && info.getEnterServer() > 0 && info.getIsOnLine() == 1) {
                                        ServerUtil.notifyChangeGolds(info.getEnterServer(), userId, (oldFreeGold + oldGold), goldChange, getAllGold());
                                    }
                                } catch (Exception e) {
                                    LogUtil.errorLog.error("", e);
                                }
                            }
                        }

                        StringBuilder sb = new StringBuilder("updateUserGold|succ");
                        sb.append("|").append(userId);
                        sb.append("|").append(sourceType.type());
                        sb.append("|").append(gold);
                        sb.append("|").append(freeGold);
                        sb.append("|").append(oldGold);
                        sb.append("|").append(oldFreeGold);
                        sb.append("|").append(-minusGold);
                        sb.append("|").append(-minusFreeGold);
                        sb.append("|").append(tryCount);
                        LogUtil.monitorLog.info(sb.toString());

                        PlayerManager.getInstance().addUserGoldRecord(new UserGoldRecord(userId, this.freeGold, this.gold, (int) -minusFreeGold, (int) -minusGold, playType, sourceType));

                        // 统计数据
                        if (sourceType != SourceType.table_win
                                && sourceType != SourceType.solo_room
                                && sourceType != SourceType.group_commission
                                && sourceType != SourceType.groupTableGoldRoom) {
                            Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
                            GoldDataStatistics data = new GoldDataStatistics(dataDate, sourceType, userId, 1, goldChange);
                            DataStatisticsDao.getInstance().saveOrUpdateGoldDataStatistics(data);

                            GoldDataStatistics dataSys = new GoldDataStatistics(dataDate, sourceType, 0l, 1, goldChange);
                            DataStatisticsDao.getInstance().saveOrUpdateGoldDataStatistics(dataSys);
                        }
                    } else {
                        refreshGoldFromDb();
                        tryCount++;
                        if (tryCount > 20) {
                            StringBuilder sb = new StringBuilder("updateUserGold|fail");
                            sb.append("|").append(userId);
                            sb.append("|").append(gold);
                            sb.append("|").append(freeGold);
                            sb.append("|").append(oldGold);
                            sb.append("|").append(oldFreeGold);
                            sb.append("|").append(-minusGold);
                            sb.append("|").append(-minusFreeGold);
                            sb.append("|").append(tryCount);
                            LogUtil.monitorLog.info(sb.toString());
                            return false;
                        }
                        return changeGold(consume, tryCount);
                    }
                }

            }
            return true;
        } catch (Exception e) {
            LogUtil.errorLog.error("changeGold|error|" + userId + "|" + tryCount + "|" + JSON.toJSONString(consume));
        }
        return false;
    }

    public void refreshGoldFromDb(){
        long[] cs = GoldDao.getInstance().loadUserGold(userId);
        this.gold = cs[0];
        this.freeGold = cs[1];
    }
}
