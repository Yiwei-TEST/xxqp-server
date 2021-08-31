package com.sy599.game.qipai.ddz.constant;

import java.util.ArrayList;
import java.util.List;

public class DdzConstants {



    /*** 打牌 */
    public static final int TABLE_STATUS_PLAY = 2;

    /**托管**/
    public static final int action_tuoguan = 100;

    /**叫地主**/
    public static final int TABLE_STATUS_JIAODIZHU = 3;

    /**抢地主**/
    public static final int TABLE_STATUS_QIANGDIZHU = 4;

    /**加倍**/
    public static final int TABLE_STATUS_SELECT_JIABEI = 5;

    /**让牌**/
    public static final int TABLE_STATUS_SELECT_RANGPAI=6;

    /**底牌录像码**/
    public static final int TABLE_REPLAY_DIPAI_CODE=7;


    // public static List<Integer> cardList_16 = new ArrayList<>(52);
    public static List<Integer> cardList = new ArrayList<>(54);
    static {
        // 方片 1 梅花2 洪涛3 黑桃4 5王
        //  3-A 2
            for (int i = 1; i <= 4; i++) {
                for (int j = 3; j <= 15; j++) {
                    int card = i * 100 + j;
                    cardList.add(card);
                }
            }
            cardList.add(501);//小王
            cardList.add(502);//大王
    }
    public static void main(String[] args) {
        List<Integer>	copy = cardList.subList(8,cardList.size());
        int maxCount = copy.size() / 4;
        List<Integer> pai = new ArrayList<>();
        List<List<Integer>> list = new ArrayList<>();

        int j=1;
        for (int i = 0; i < copy.size(); i++) {
            int card = copy.get(i);
            if (i < j*maxCount) {
                pai.add(card);
            } else {
                list.add(pai);
                pai = new ArrayList<>();
                pai.add(card);
                j++;
            }

        }
        list.add(pai);
        list.add(cardList.subList(0, 8));
        System.out.println(list);

    }
}
