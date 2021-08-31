package com.sy599.game.qipai.yjmj.constant;

import com.sy599.game.GameServerConfig;

import java.util.ArrayList;
import java.util.List;

public class YjMjConstants {
    public static boolean isTest = false;

    public static final String def_icon = "http://testxsg.sy599.com/qiji_egret_sdk/src/resource/dynamic/item/1.png";

    public static List<Integer> yuanjiang_mjList = new ArrayList<>();
    /**
     * 沅江麻将番数上限
     */
    public static int fanshuLimit = 24;

    static {
        if (GameServerConfig.isDeveloper()) {
            isTest = false;
        }
        // 筒万条 108张+4红中
        for (int i = 1; i <= 108; i++) {
            yuanjiang_mjList.add(i);
        }
    }

    /*** xx秒后进入托管**/
    public static int AUTO_TIMEOUT = 20;
    /*** 防恶意托管时间**/
    public static int AUTO_TIMEOUT2 = 10;
    /*** 托管后xx秒自动出牌**/
    public static final int AUTO_PLAY_TIME = 0;
    /*** 托管后xx秒自动准备**/
    public static int AUTO_READY_TIME = 10;
    /*** 托管后xx秒自动胡**/
    public static final int AUTO_HU_TIME = 0;

    /*** 桌状态飘分 */
    public static final int TABLE_STATUS_PIAO = 1;


    /*** arr大小，一共有多少个操作类型**/
    public static final int ACTION_INDEX_SIZE = 7;

    // 0胡 1碰 2明杠 3暗杠 4接杠 5杠爆(摸杠胡) 报听6
    /*** 胡*/
    public static final int ACTION_INDEX_HU = 0;
    /*** 碰*/
    public static final int ACTION_INDEX_PENG = 1;
    /*** 明杠*/
    public static final int ACTION_INDEX_MINGGANG = 2;
    /*** 暗杠*/
    public static final int ACTION_INDEX_ANGANG = 3;
    /*** 接杠*/
    public static final int ACTION_INDEX_JIEGANG = 4;
    /*** 杠暴  */
    public static final int ACTION_INDEX_GANGBAO = 5;
    /*** 听  */
    public static final int ACTION_INDEX_BAOTING = 6;
}
