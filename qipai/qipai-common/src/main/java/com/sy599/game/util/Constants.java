package com.sy599.game.util;

public final class Constants {
    /**
     * 无房号金币场房间最小id
     **/
    public static final long MIN_GOLD_ID = 10000000L;

    /**
     * 无房号金币场---锁
     **/
    public static final Object GOLD_LOCK = new Object();

    /**
     * 俱乐部自动创房---锁
     **/
    public static final Object LOCK_AUTO_CREATE_GROUP_TABLE = new Object();

    /**
     * 信用分记录类型：管理者加减分
     */
    public static final int CREDIT_LOG_TYPE_ADMIN = 1;
    /**
     * 信用分记录类型：牌桌佣金分
     */
    public static final int CREDIT_LOG_TYPE_COMMSION = 2;
    /**
     * 信用分记录类型：牌桌输赢分
     */
    public static final int CREDIT_LOG_TYPE_TABLE = 3;
    /**
     * 信用分记录类型：洗牌分
     */
    public static final int CREDIT_LOG_TYPE_XIPAI = 4;

    /**
     * 信用分记录类型：AA佣金分
     */
    public static final int CREDIT_LOG_TYPE_AA = 5;


    /**
     * 金币记录类型：抽水
     */
    public static final int GOLD_LOG_TYPE_COMMISSION = 1;


}
