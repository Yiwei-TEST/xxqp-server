package com.sy599.game.qipai.yywhz.constant;

import com.sy599.game.GameServerConfig;
import com.sy599.game.util.ResourcesConfigsUtil;

import java.util.ArrayList;
import java.util.List;

public class WaihuziConstant {
	
	public static boolean isTest = false;

	public static final int state_player_ready = 1;
	public static final int state_player_offline = 2;
	public static final int state_player_diss = 3;
	
	
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

	/** 牌局离线 **/
	public static final int table_offline = 1;
	/** 牌局在线 **/
	public static final int table_online = 2;
	/** 牌局暂离 **/
	public static final int table_afk = 3;
	/** 牌局暂离回来 **/
	public static final int table_afkback = 4;

	public static boolean isAutoMo = true;

	// 跑胡子
	public static List<Integer> cardList = new ArrayList<>();
	
	static {
		isTest="1".equals(ResourcesConfigsUtil.loadServerPropertyValue("test"));
		for (int i = 1; i <= 80; i++) {
			cardList.add(i);
		}

	}
}
