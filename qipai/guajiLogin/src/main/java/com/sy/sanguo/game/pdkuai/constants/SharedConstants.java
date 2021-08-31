package com.sy.sanguo.game.pdkuai.constants;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.util.Constants;
import org.apache.commons.lang3.math.NumberUtils;

public class SharedConstants {
	public static SqlMapClient sqlClient;
	public static SqlMapClient sqlFuncClient;

	/*** 拓展字段 滴滴打车 唯一奖励 */
	public final static int extend_getaward_dididache = 101;

	public static int SENCOND_IN_MINILLS = 1000;

	public static boolean consumecards_type_csmajiang = false;

	public static int giveRoomCards = 5;
	public static int bindGiveRoomCards = 5;
	public static long blockIconTime = 0;

	public static final int game_type_pdk = 1;
	public static final int game_type_majiang = 2;
	public static final int game_type_dn = 3;
	public static final int game_type_paohuzi = 4;
	public static final int game_type_sg = 5;
	public static final int game_type_sdb = 6;
	public static final int game_type_ddz = 7;
	public static final int game_type_dtz = 8;
	public static final int game_type_sp = 9;
	public static final int game_type_bbtz = 10;

	public static int getType(int playType) {
		int logType = NumberUtils.toInt(PropertiesCacheUtil.getValueOrDefault("log_type_play"+playType,"-1", Constants.GAME_FILE),-1);
		if (logType >= 0){
			return logType;
		}else if (playType == 15 || playType == 16 || playType == 17 || playType == 18) {
			return game_type_pdk;
		}else if (isPlayMj(playType)) {
			return game_type_majiang;
		} else if (isPlayDn(playType)) {
			return game_type_dn;
		} else if (isPlayPhz(playType)) {
			return game_type_paohuzi;
		}else if (isPlaySg(playType)) {
			return game_type_sg;
		}else if (isPlaySdb(playType)) {
			return game_type_sdb;
		} else if (isPlayDdz(playType)) {
			return game_type_ddz;
		} else if (isPlayDtz(playType)) {
			return game_type_dtz;
		} else if (isPlaySp(playType)) {
			return game_type_sp;
		} else if (isPlayBbtz(playType)) {
			return game_type_bbtz;
		}
		return 0;
	}
	
	/**
	 * 录入所有麻将玩法ID
	 * @param playType
	 * @return
	 */
	private static boolean isPlayMj(int playType) {
		return playType < 10 || (playType >= 101 && playType <= 112) || playType >= 500;
	}
	
	private static boolean isPlayDdz(int playType) {
		return playType >= 91 && playType <100;
	}

	private static boolean isPlayDn(int playType) {
		return playType >= 20 && playType < 30;
	}

	private static boolean isPlayPhz(int playType) {
		return playType >= 30 && playType < 40;
	}

	private static boolean isPlaySg(int playType) {
		return playType >= 41 && playType < 50;
	}

	private static boolean isPlaySdb(int playType) {
		return playType >= 61 && playType < 70;
	}

    private static boolean isPlayDtz(int playType) {
        return (playType >= 111 && playType < 120)
                || (playType >= 210 && playType <= 212);
    }

	private static boolean isPlaySp(int playType) {return playType >= 121 && playType < 130; }
	
	private static boolean isPlayBbtz(int playType) {return playType == 131; }
}
