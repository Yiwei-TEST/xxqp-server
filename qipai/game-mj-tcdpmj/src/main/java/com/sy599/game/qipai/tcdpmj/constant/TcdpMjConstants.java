package com.sy599.game.qipai.tcdpmj.constant;

import java.util.ArrayList;
import java.util.List;

import com.sy599.game.GameServerConfig;

public class TcdpMjConstants {
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
    public static final int ACTION_INDEX_CHI = 4;//改吃
    /*** 报听  */
    public static final int ACTION_INDEX_BAOTING = 5;
//    /*** 杠爆（摸杠胡）*/
//    public static final int ACTION_INDEX_GANGBAO = 5;
//    /*** 报听*/
//    public static final int ACTION_INDEX_BAOTING = 6;
    
    /**托管**/
    public static final int action_tuoguan = 100;
    /**
     * 自摸次数
     */
    public static final int ACTION_COUNT_INDEX_ZIMO = 0;
    /**
     * 接炮次数
     */
    public static final int ACTION_COUNT_INDEX_JIEPAO = 1;
    /**
     * 点炮次数
     */
    public static final int ACTION_COUNT_INDEX_DIANPAO = 2;
    /**
     * 暗杠次数
     */
    public static final int ACTION_COUNT_INDEX_ANGANG = 3;
    /**
     * 明杠次数
     */
    public static final int ACTION_COUNT_INDEX_MINGGANG = 4;

    /**
     * 抢杠胡
     */
    public static final int HU_QIANGGANGHU = 101;
    /**
     * 自摸胡
     */
    public static final int HU_ZIMO = 102;
    /**
     * 杠开胡
     */
    public static final int HU_GANGKAI = 103;
    /**
     * 接炮胡
     */
    public static final int HU_JIPAO = 104;

    /**
     * 放炮
     */
    public static final int HU_FANGPAO = 201;

    /**
     * xx秒后进入托管
     **/
    public static final int AUTO_TIMEOUT = GameServerConfig.isDeveloper() ? 20 : 180;
    /**
     * xx秒后开始托管倒计时
     */
    public static final int AUTO_CHECK_TIMEOUT = 10;
    /**
     * 托管后xx秒自动出牌
     **/
    public static final int AUTO_PLAY_TIME = 2;
    /**
     * 托管后xx秒自动准备
     **/
    public static final int AUTO_READY_TIME = 4;
    /**
     * 托管后xx秒自动胡
     **/
    public static final int AUTO_HU_TIME = 2;

    public static boolean isTest = false;
    public static boolean isTestAh = false;

    public static List<Integer> cx_mjList = new ArrayList<>();

    static {
        if (GameServerConfig.isDeveloper()) {
            isTest = true;
            isTestAh = true;
        }

        // ///////////////////////
        for (int i = 1; i <= 124; i++) {
            cx_mjList.add(i);
        }

        for (int i = 201; i <= 212; i++) {
            cx_mjList.add(i);
        }
    }

    /**
     * 根据玩法获取牌
     *
     * @return
     */
    public static List<Integer> getMajiangList(int playerCount) {
        return new ArrayList<>(cx_mjList);
    }

}
