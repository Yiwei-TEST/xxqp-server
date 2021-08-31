package com.sy599.game.qipai.zzmj.constant;

import java.util.ArrayList;
import java.util.List;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.zzmj.rule.ZzMj;

public class ZzMjConstants {
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
    /*** 自摸  */
    public static final int ACTION_INDEX_ZIMO = 5;
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

    public static List<Integer> zhuanzhuan_2Player = new ArrayList<>();
    public static List<Integer> zhuanzhuan_mjList = new ArrayList<>();
    public static List<Integer> hongzhong_mjList = new ArrayList<>();

    static {
        if (GameServerConfig.isDeveloper()) {
            isTest = true;
            isTestAh = true;
        }

        // ///////////////////////
        // 筒万条 108张+4红中
        for (int i = 1; i <= 108; i++) {
            zhuanzhuan_mjList.add(i);
            hongzhong_mjList.add(i);
            if ((i >= 19 && i <= 27) || (i >= 46 && i <= 54) || (i >= 73 && i <= 81) || (i >= 100 && i <= 108)) {
                zhuanzhuan_2Player.add(i);
            }
        }
        //东南西北
        for (int i = 109; i <= 124; i++) {
            zhuanzhuan_2Player.add(i);
        }
        //红中
        for (int i = 201; i <= 204; i++) {
            hongzhong_mjList.add(i);
            zhuanzhuan_2Player.add(i);
        }
        //发白
        for (int i = 205; i <= 212; i++) {
            zhuanzhuan_2Player.add(i);
        }
    }

    /**
     * 根据玩法获取牌
     *
     * @return
     */
    public static List<Integer> getMajiangList(int playerCount) {
        switch (playerCount) {
            case 4:
            case 3:
                return new ArrayList<>(zhuanzhuan_mjList);
            case 2:
                return new ArrayList<>(zhuanzhuan_2Player);
            default:
                return new ArrayList<>(zhuanzhuan_mjList);
        }
    }

    public static List<ZzMj> getMajiangPai(int playerCount) {
        List<ZzMj> majiangs = new ArrayList<>();
        if (playerCount == 2) {
            for (int i = 19; i <= 27; i++) {
                majiangs.add(ZzMj.getMajang(i));
            }
            majiangs.add(ZzMj.getMajang(109));
            majiangs.add(ZzMj.getMajang(113));
            majiangs.add(ZzMj.getMajang(117));
            majiangs.add(ZzMj.getMajang(121));
            majiangs.add(ZzMj.getMajang(201));
            majiangs.add(ZzMj.getMajang(205));
            majiangs.add(ZzMj.getMajang(209));
        } else {
            for (int i = 1; i <= 27; i++) {
                majiangs.add(ZzMj.getMajang(i));
            }
        }
        return majiangs;
    }
}
