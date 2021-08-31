package com.sy599.game.qipai.zzpdk.constant;

import java.util.ArrayList;
import java.util.List;

public class PdkConstants {

    /*** 桌状态飘分 */
    public static final int TABLE_STATUS_DANIAO = 1;
    /**托管**/
    public static final int action_tuoguan = 100;
	public static List<Integer> cardList_16 = new ArrayList<>(52);
	public static List<Integer> cardList_15 = new ArrayList<>(52);
	
	public static List<Integer> cardList_11 = new ArrayList<>(33);
	static {
		// 方片 1 梅花2 洪涛3 黑桃4

		// ///////////////////////
		// 16张玩法 3-A 1-2
		for (int i = 1; i <= 3; i++) {
			for (int j = 3; j <= 14; j++) {
				int card = i * 100 + j;
				cardList_16.add(card);
			}
		}

		// 留了黑桃2 没留黑桃1
		for (int k = 3; k <= 13; k++) {
			int card = 4 * 100 + k;
			cardList_16.add(card);
		}
		cardList_16.add(415);

		// //////////////////////////////
		// 15张玩法 3-k 1-A 1-2
		for (int i = 1; i <= 4; i++) {
			for (int j = 3; j <= 13; j++) {
				int card = i * 100 + j;
				cardList_15.add(card);
			}
		}
		cardList_15.remove(Integer.valueOf(413));
		cardList_15.add(214);
		cardList_15.add(415);
		
		
		
		
		
		
		for (int i = 1; i <= 4; i++) {
			for (int j = 6; j <= 13; j++) {
				int card = i * 100 + j;
				cardList_11.add(card);
			}
		}
		cardList_11.remove(Integer.valueOf(413));
		cardList_11.add(314);
		cardList_11.add(415);
		
		
		
		
		
	}
}
