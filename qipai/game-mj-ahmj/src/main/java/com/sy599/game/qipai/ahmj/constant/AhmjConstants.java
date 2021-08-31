package com.sy599.game.qipai.ahmj.constant;

import com.sy599.game.GameServerConfig;
import com.sy599.game.util.ResourcesConfigsUtil;

import java.util.ArrayList;
import java.util.List;

public class AhmjConstants {
	/**
	 * 托管后xx秒自动出牌
	 **/
	public static final int AUTO_PLAY_TIME = 2;
	/**
	 * 托管后xx秒自动胡
	 **/
	public static final int AUTO_HU_TIME = 2;
	/**
	 * xx秒后开始托管倒计时
	 */
	public static final int AUTO_CHECK_TIMEOUT = 10;

	public static boolean isTest = false;
	public static boolean isTestAh = false;

    
    /**托管**/
    public static final int action_tuoguan = 100;
	/**
	 * xx秒后进入托管
	 **/
	public static final int AUTO_TIMEOUT = GameServerConfig.isDeveloper() ? 20 : 180;

	public static List<Integer> zhuanzhuan_mjList = new ArrayList<>();
	public static Ahmj[] fullMj = new Ahmj[27];

	static {
//		if (GameServerConfig.isDeveloper()) {
//			isTest = true;
//			isTestAh = true;
//		}
	}

	static {
		for (int i = 1; i <= 108; i++) {
			zhuanzhuan_mjList.add(i);
		}
		for(Ahmj mj : Ahmj.values()) {
			if(mj.getId() <= 27){
				fullMj[mj.getId()-1] = mj;
			}
		}
	}
	public static List<Integer> getMajiangList() {
		return new ArrayList<>(zhuanzhuan_mjList);
	}
}
