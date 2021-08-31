package com.sy599.game.db.bean.serverSign;

import com.sy599.game.util.TimeUtil;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 玩家七日签到信息(芒果)
 */
public class UserSevenSignInfo {
    /**
     * 上次签到时间
     */
    private long lastSignTime;

    /**
     * 已签到档次
     */
    private List<Integer> signGrades;

    public UserSevenSignInfo() {

    }

    public UserSevenSignInfo(long lastSignTime, List<Integer> signGrades) {
        this.lastSignTime = lastSignTime;
        this.signGrades = signGrades;
    }

    public long getLastSignTime() {
        return lastSignTime;
    }

    public void setLastSignTime(long lastSignTime) {
        this.lastSignTime = lastSignTime;
    }

    /**
     * 是否可领取 0不能领取 1领取下一个档次 2领取档次1
     * @return
     */
    public int canReceive() {
        int canReceiveNext = 0;// 0不能领取 1领取下一个档次 2领取档次1
        if(lastSignTime == 0 ) {
            canReceiveNext = 1;
        } else {
            if(lastSignTime > 0) {
                int apartDay = TimeUtil.apartDays(new Date(lastSignTime), new Date());
                if(apartDay == 1) {
                    canReceiveNext = 1;
                } else if(apartDay > 1)
                    canReceiveNext = 2;
            }
        }
        return canReceiveNext;
    }

    public List<Integer> getSignGrades() {
        return signGrades;
    }

    public void setSignGrades(List<Integer> signGrades) {
        this.signGrades = signGrades;
    }
}
