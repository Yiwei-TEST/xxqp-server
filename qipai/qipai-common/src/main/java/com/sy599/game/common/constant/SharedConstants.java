package com.sy599.game.common.constant;

import com.sy599.game.util.GameUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.util.StringUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * 管理所有公共属性
 * 
 * @author taohuiliang
 * @date 2013-3-1
 * @version v1.0
 */
public class SharedConstants {
	/*** 超时自动操作时间*/
	public static int timeOut = 10 * 1000;
    /*** 托管时间*/
    public static int timeOut_bet = 5000;
    public static int timeOut_robBanker = 5000;
    public static int timeOut_ready = 5000;
    public static int timeOut_wantCard = 5000;
    public static int timeOut_wantFirstCard = 5000;
	public static int timeOut_showCard = 5000;
	public static int timeOut_shuffling = 5000;

	public static int timeOut_sp_bet = 8000;
	public static int timeOut_sp_robBanker = 8000;
	public static int timeOut_sp_ready = 8000;

	/*** 称号*/
	public static int caoming = 0;
	public static int xianling = 1;
	public static int zhifu = 2;
	public static int shangshu = 3;
	public static int zaixiang = 4;
	public static int huangdi = 5;
	
	
	/*** 代码版本号 */
	public static int version = 9;
	/** 数据库存储的心跳间隔(分钟为单位)，5分钟统一执行一次数据库操作 **/
	public static int DB_SAVE_INTERVAL = 1;

	public static int SENCOND_IN_MINILLS = 1000;
	public static int MIN_IN_MINILLS = 60 * 1000;
	public static int HOUR_IN_MINILLS = 3600 * 1000;
	public static long DAY_IN_MINILLS = 24 * 3600 * 1000;

	public static long diss_timeout = 3 * MIN_IN_MINILLS;

	public static final int err_code_cheart = -3;
	/***/
	public static final int err_code_no_player = -2;
	/** 找不到用户 */
	public static final int err_code_data_exception = -1;
	/** 后台数据异常 */
	public static int code_err_msg = 999;
	public static int code_err_item = 998;
	public static int code_1 = 1;
	public static int code_2 = 2;

	public static final int game_flag_start = 1;
	public static final int game_flag_shut = 0;

	/** 消费提示 **/
	public static final int FUNC_NO_TIPS = 0;

	/**** 游戏内刷新时间 对应user_inf--refreshTime *****/
	/** 刷新时间1--05:00 **/
	public static final int REFRESH_TIME_FIVE_AM = 0;
	/** 刷新时间2--12:00 **/
	public static final int REFRESH_TIME_TWELVE_AM = 1;
	/** 刷新时间3--14:00 **/
	public static final int REFRESH_TIME_TWO_PM = 2;
	/** 刷新时间3--20:00 **/
	public static final int REFRESH_TIME_EIGHT_PM = 3;
	/** 刷新时间4--21:00 **/
	public static final int REFRESH_TIME_NINE_PM = 4;
	/** 刷新时间5--22:00 **/
	public static final int REFRESH_TIME_TEN_PM = 5;

	public static final int game_type_pdk = 1;
	public static final int game_type_majiang = 2;
	public static final int game_type_dn = 3;
	public static final int game_type_paohuzi = 4;
	public static final int game_type_sg = 5;
	public static final int game_type_sdb = 6;
	public static final int game_type_ddz = 7;
	/**  打筒子   **/
	public static final int game_type_dtz = 8;
	/**  三皮   **/
	public static final int game_type_sp = 9;
	/**  半边天炸   **/
	public static final int game_type_bbtz = 10;
	/** 升级  */
	public static final int game_type_shengji = 11;
	
	/** 房主支付是AA支付的倍数**/
	public static int payRatio = 4;

    public static final String SWITCH_DEFAULT_ON = "1";
    public static final String SWITCH_DEFAULT_OFF = "0";
    public static final String SWITCH_AUTO_QUIT = "switch_auto_quit";
	public static final String TIME_OUT_AUTO_QUIT = "time_out_auto_quit";
	public static final String TIME_OUT_AUTO_QUIT_DEFAULT = "120000";

	public static int getType(int playType) {
		if (playType == 15 || playType == 16) {
			return game_type_pdk;
		}
		if(GameUtil.isPlayDtz(playType)){
			return game_type_dtz;
		}
		if(GameUtil.isPlayBbtz(playType)){
			return game_type_bbtz;
		}
		if (playType < 10) {
			return game_type_majiang;
		}
		return 0;
	}

	/** 牌局离线 **/
	public static final int table_offline = 1;
	/** 牌局在线 **/
	public static final int table_online = 2;
	/** 牌局暂离 **/
	public static final int table_afk = 3;
	/** 牌局暂离回来 **/
	public static final int table_afkback = 4;
	
	public static final int state_player_ready = 1;
	public static final int state_player_offline = 2;
	public static final int state_player_diss = 3;

	public enum table_state {
		ready(1), play(2), over(3);
		private int id;

		private table_state(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}
	}

	public enum
	player_state {
		entry(1), ready(2), play(3), over(4);
		private int id;

		private player_state(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}
	}

	public static player_state getPlayerState(int id) {
		for (player_state state : player_state.values()) {
			if (state.getId() == id) {
				return state;
			}
		}
		return null;
	}

	public static table_state getTableState(int id) {
		for (table_state state : table_state.values()) {
			if (state.getId() == id) {
				return state;
			}
		}
		return null;
	}

	public static volatile boolean consumecards = true;
	public static volatile boolean consumegold = true;
	
	public static boolean isCalcxgnn() {
		return "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("calcxgnn"));
	}

	public static boolean isCalcsdb() {
		return "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("calcsdb"));
	}
	
	public static boolean isCalcddz() {
		return "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("calcddz"));
	}

	public static boolean isKingOfBull(){
		return "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("isKingOfBull"));
	}

	public static void setTimeOut(int time) {
		timeOut = time;
	}


    public static void loadTimeOut() {
        setTimeOut(Integer.parseInt(ResourcesConfigsUtil.loadServerPropertyValue("timeOut", "10000")));
        String phaseTimeOut = ResourcesConfigsUtil.loadServerPropertyValue("phaseTimeOut", "");
        String phaseTimeOutSp = ResourcesConfigsUtil.loadServerPropertyValue("phaseTimeOutSp","");
        if (!StringUtil.isBlank(phaseTimeOut)) {
            String[] strs = phaseTimeOut.split(",");
            timeOut_ready = StringUtil.getIntValue(strs, 0) * 1000;
            timeOut_robBanker = StringUtil.getIntValue(strs, 1) * 1000;
            timeOut_bet = StringUtil.getIntValue(strs, 2) * 1000;
            timeOut_wantCard = StringUtil.getIntValue(strs, 3) * 1000 ;
            timeOut_wantFirstCard = StringUtil.getIntValue(strs, 4) * 1000;
            timeOut_shuffling = StringUtil.getIntValue(strs, 5) * 1000;
			timeOut_showCard = StringUtil.getIntValue(strs, 6) * 1000;
        }
        if (!StringUtil.isBlank(phaseTimeOutSp)) {
			String[] strs = phaseTimeOutSp.split(",");
			timeOut_sp_ready = StringUtil.getIntValue(strs, 0) * 1000;
			timeOut_sp_robBanker = StringUtil.getIntValue(strs, 1) * 1000;
			timeOut_sp_bet = StringUtil.getIntValue(strs, 2) * 1000;
		}

    }

    public static boolean isAssisOpen() {
		return  "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("assis_on_off"));
	}

	public static boolean isRestrictOpen() {
		return "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("assis_restrict_on_off"));
	}

    public static boolean isAutoQuit() {
        return "1".equals(ResourcesConfigsUtil.loadServerPropertyValue(SWITCH_AUTO_QUIT, SWITCH_DEFAULT_OFF));
    }

    public static int getAutoQuitTimeOut() {
        return Integer.parseInt(ResourcesConfigsUtil.loadServerPropertyValue(TIME_OUT_AUTO_QUIT, TIME_OUT_AUTO_QUIT_DEFAULT));
    }

    /**
     * 测试服：测试服不统计白金岛有效局数
     */
    public static final List<Integer> testPlayTypes = new ArrayList<>();

    static {
        List<Integer> list = StringUtil.explodeToIntList(ResourcesConfigsUtil.loadServerPropertyValue("test_playType"));
        if (list != null) {
            testPlayTypes.addAll(list);
        }

    }

}
