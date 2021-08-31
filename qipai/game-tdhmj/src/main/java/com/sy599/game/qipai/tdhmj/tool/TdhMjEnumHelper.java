package com.sy599.game.qipai.tdhmj.tool;

import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;

public class TdhMjEnumHelper {
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
}
