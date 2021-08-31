package com.sy599.game.common.constant;

public enum LogConstants {
    /**
     * 消耗房卡
     */
    reason_consumecards(100),
    /**
     * 创建房间
     */
    reason_createtable(101),
    /**
     * 充值房卡
     */
    reason_pay(102),
    /**
     * 解散房间
     */
    reason_diss(103),
    /**
     * 登陆
     */
    reason_login(1),
    /**
     * 登出
     */
    reason_logout(2);

    private int val;

    LogConstants(int val) {
        this.val = val;
    }

    public int val() {
        return this.val;
    }

}
