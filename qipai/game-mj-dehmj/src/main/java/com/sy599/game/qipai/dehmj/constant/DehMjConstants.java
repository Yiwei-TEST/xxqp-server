package com.sy599.game.qipai.dehmj.constant;

import com.sy599.game.GameServerConfig;

import java.util.ArrayList;
import java.util.List;

public class DehMjConstants {
	public static boolean isTest = false;
	public static boolean isTestAh = false;
	
	
	
	
	
	
	
	
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
	
	

	/** 桌状态飘分 */
	public static final int TABLE_STATUS_PIAO = 1;

	public static final String def_icon = "http://testxsg.sy599.com/qiji_egret_sdk/src/resource/dynamic/item/1.png";
	// public static final int state_player_online=3;
	// public static List<Integer> cardList_16 = new ArrayList<>();
	public static List<Integer> baoshan_mjList = new ArrayList<>();
	public static List<Integer> feng_mjList = new ArrayList<>();
	static {
		if (GameServerConfig.isDeveloper()) {
			isTest = false;
//			isTestAh = true;
		}

		// ///////////////////////
		// 筒万条 108张+4红中
		for (int i = 1; i <= 108; i++) {
			baoshan_mjList.add(i);
			//hongzhong_mjList.add(i);
		}
		for (int j = 201; j <= 212; j++) {
			feng_mjList.add(j);
		}
		for (int j = 109; j <= 124; j++) {
			feng_mjList.add(j);
		}
	}

}
