package com.sy599.game.qipai.nymj.constant;

import java.util.ArrayList;
import java.util.List;

import com.sy599.game.GameServerConfig;

public class MjConstants {
	public static boolean isTest = false;
	public static boolean isTestAh = false;

	/** 桌状态飘分 */
	public static final int TABLE_STATUS_PIAO = 1;
	
	
    /*** 托管后xx秒自动出牌**/
    public static final int AUTO_PLAY_TIME = 0;
    /*** 托管后xx秒自动准备**/
    public static int AUTO_READY_TIME = 10;
    /*** 托管后xx秒自动胡**/
    public static final int AUTO_HU_TIME = 0;

	public static final String def_icon = "http://testxsg.sy599.com/qiji_egret_sdk/src/resource/dynamic/item/1.png";
	// public static final int state_player_online=3;
	// public static List<Integer> cardList_16 = new ArrayList<>();
	//全牌136
	public static List<Integer> fullMj = new ArrayList<>();
	//无风牌
//	public static List<Integer> noneWind = new ArrayList<>();
	//无字牌,东南西北中发白
	public static List<Integer> noneChar = new ArrayList<>();
	static {
		if (GameServerConfig.isDeveloper()) {
			isTest = false;
//			isTestAh = true;
		}

		// ///////////////////////
		// 筒万条 108张+4红中
		for (int i = 1; i <= 108; i++) {
			fullMj.add(i);
//			noneWind.add(i);
			noneChar.add(i);
		}

		for (int i = 201; i <= 204 ;i++){
			fullMj.add(i);
		}

//		//风
//		for (int j = 109; j <= 124; j++) {
//			fullMj.add(j);
//		}
//
//		//箭,字
//		for (int j = 201; j <= 212; j++) {
//			fullMj.add(j);
//			//风
//			noneWind.add(j);
//		}
	}

}
