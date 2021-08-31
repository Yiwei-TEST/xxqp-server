package com.sy599.game.qipai.yjghz.constant;

import com.sy599.game.util.ResourcesConfigsUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class YjGhzConstant {

    public static boolean isTest = false;

    public static final int state_player_ready = 1;
    public static final int state_player_offline = 2;
    public static final int state_player_diss = 3;

    /**
     * 牌局离线
     **/
    public static final int table_offline = 1;
    /**
     * 牌局在线
     **/
    public static final int table_online = 2;
    /**
     * 牌局暂离
     **/
    public static final int table_afk = 3;
    /**
     * 牌局暂离回来
     **/
    public static final int table_afkback = 4;

    public static boolean isAutoMo = true;

    // 跑胡子
    public static List<Integer> cardList = new ArrayList<>();

    // 检查听
    public static List<YjGhzCard> checkTingList = new ArrayList<>();

    static {
        isTest = "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("test"));

        Set<Integer> added = new HashSet<>();
        for (int i = 1; i <= 80; i++) {

            cardList.add(i);

            YjGhzCard card = YjGhzCard.getPaohzCard(i);
            if (card != null) {
//                if (!added.contains(card.getVal())) {
                    checkTingList.add(card);
//                }
                added.add(card.getVal());
            }
        }
    }

    /**托管**/
    public static final int action_tuoguan = 100;
}
