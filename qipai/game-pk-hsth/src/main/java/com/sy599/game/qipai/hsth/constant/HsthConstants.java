package com.sy599.game.qipai.hsth.constant;

import java.util.ArrayList;
import java.util.List;

public class HsthConstants {



    /*** 打牌 */
    public static final int TABLE_STATUS_PLAY = 2;

    /**托管**/
    public static final int action_tuoguan = 100;


    /**分组*/
    public static final int FENZU=4;
    /**明牌*/
    public static final int MINGPAI=6;


    // public static List<Integer> cardList_16 = new ArrayList<>(52);
    public static List<Integer> cardList_8 = new ArrayList<>();
    static {
        // 方片 1 梅花2 洪涛3 黑桃4 5王
        // 牌数 八，九，十，十二副牌，去掉2、3、4、6、7、大、小王。
        for (int n = 1; n <=8; n++) {
            for (int i = 1; i <= 4; i++) {
                for (int j = 5; j < 15; j++) {
                     if(j==6 || j==7){
                         continue;
                     }else{
                         int card = i * 100 + j;
                         cardList_8.add(card);
                     }
                }
            }
        }
    }
    public static List<Integer> cardList_9 = new ArrayList<>();
    static {
        // 方片 1 梅花2 洪涛3 黑桃4 5王
        // 牌数 八，九，十，十二副牌，去掉2、3、4、6、7、大、小王。
        for (int n = 1; n <=9; n++) {
            for (int i = 1; i <= 4; i++) {
                for (int j = 5; j < 15; j++) {
                    if(j==6 || j==7){
                        continue;
                    }else{
                        int card = i * 100 + j;
                        cardList_9.add(card);
                    }
                }
            }
        }
    }
    public static List<Integer> cardList_10= new ArrayList<>();
    static {
        // 方片 1 梅花2 洪涛3 黑桃4 5王
        // 牌数 八，九，十，十二副牌，去掉2、3、4、6、7、大、小王。
        for (int n = 1; n <=10; n++) {
            for (int i = 1; i <= 4; i++) {
                for (int j = 5; j < 15; j++) {
                    if(j==6 || j==7){
                        continue;
                    }else{
                        int card = i * 100 + j;
                        cardList_10.add(card);
                    }
                }
            }
        }
    }
    public static List<Integer> cardList_12= new ArrayList<>();
    static {
        // 方片 1 梅花2 洪涛3 黑桃4 5王
        // 牌数 八，九，十，十二副牌，去掉2、3、4、6、7、大、小王。
        for (int n = 1; n <=12; n++) {
            for (int i = 1; i <= 4; i++) {
                for (int j = 5; j < 15; j++) {
                    if(j==6 || j==7){
                        continue;
                    }else{
                        int card = i * 100 + j;
                        cardList_12.add(card);
                    }
                }
            }
        }
    }

    public static List<Integer> cardList_13= new ArrayList<>();
    static {
        // 方片 1 梅花2 洪涛3 黑桃4 5王
        // 牌数 八，九，十，十二副牌，去掉2、3、4、6、7、大、小王。
        for (int n = 1; n <=13; n++) {
            for (int i = 1; i <= 4; i++) {
                for (int j = 5; j < 15; j++) {
                    if(j==6 || j==7){
                        continue;
                    }else{
                        int card = i * 100 + j;
                        cardList_13.add(card);
                    }
                }
            }
        }
    }

    public static List<Integer> cardList_14= new ArrayList<>();
    static {
        // 方片 1 梅花2 洪涛3 黑桃4 5王
        // 牌数 八，九，十，十二副牌，去掉2、3、4、6、7、大、小王。
        for (int n = 1; n <=14; n++) {
            for (int i = 1; i <= 4; i++) {
                for (int j = 5; j < 15; j++) {
                    if(j==6 || j==7){
                        continue;
                    }else{
                        int card = i * 100 + j;
                        cardList_14.add(card);
                    }
                }
            }
        }
    }
    public static void main(String[] args) {
        List<Integer>	copy = cardList_8.subList(8,cardList_8.size());
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
//        list.add(cardList.subList(0, 8));
        System.out.println(list);

    }
}
