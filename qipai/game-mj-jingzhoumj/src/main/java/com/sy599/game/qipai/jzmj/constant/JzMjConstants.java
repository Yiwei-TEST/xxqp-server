package com.sy599.game.qipai.jzmj.constant;

import com.sy599.game.GameServerConfig;

import java.util.ArrayList;
import java.util.List;

public class JzMjConstants {
	public static boolean isTest = false;
	public static boolean isTestAh = false;

	public static final int xjsHuType_XiaoHuZiMo=1;
	public static final int xjsHuType_XiaoHuJiePao=2;
	public static final int xjsHuType_DaHuZiMo=3;
	public static final int xjsHuType_DaHuJiePao=4;

	/** 桌状态飘分 */
	public static final int TABLE_STATUS_PIAO = 1;
	
	
    /*** 托管后xx秒自动出牌**/
    public static final int AUTO_PLAY_TIME = 0;
    /*** 托管后xx秒自动准备**/
    public static int AUTO_READY_TIME = 10;
    /*** 托管后xx秒自动胡**/
    public static final int AUTO_HU_TIME = 0;
    
    /**托管**/
    public static final int action_tuoguan = 100;

	public static final String def_icon = "http://testxsg.sy599.com/qiji_egret_sdk/src/resource/dynamic/item/1.png";
	// public static final int state_player_online=3;
	// public static List<Integer> cardList_16 = new ArrayList<>();
	public static List<Integer> zhuanzhuan_mjList = new ArrayList<>();
	public static List<Integer> hongzhong_mjList = new ArrayList<>();
	public static List<Integer> fullMj = new ArrayList<>();
	static {
		if (GameServerConfig.isDeveloper()) {
			isTest = false;
//			isTestAh = true;
		}

		// ///////////////////////
		// 筒万条 108张
		for (int i = 1; i <= 108; i++) {
			zhuanzhuan_mjList.add(i);
//			hongzhong_mjList.add(i);
			if(i<= 27){
				fullMj.add(i);
			}
		}
//		for (int j = 201; j <= 204; j++) {
//			hongzhong_mjList.add(j);
//		}
	}

	public static void main(String[] args) {
		zhuanzhuan_mjList = zhuanzhuan_mjList.subList(0,zhuanzhuan_mjList.size()-18);
		System.out.println(zhuanzhuan_mjList.size());
	}
}
