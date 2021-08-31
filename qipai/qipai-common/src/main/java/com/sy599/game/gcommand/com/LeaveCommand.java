package com.sy599.game.gcommand.com;

import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.ArrayList;
import java.util.List;

public class LeaveCommand extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		BaseTable table = player.getPlayingTable();
		if (table == null) {
			return;
		}

		if (player.getState() != player_state.entry) {
			return;
		}
		player.changeIsLeave(1);

		ComRes.Builder com = ComRes.newBuilder();
		com.setCode(WebSocketMsgType.res_code_state);
		List<Integer> list = new ArrayList<Integer>();
		list.add(player.getSeat());
		list.add(SharedConstants.state_player_offline);
		com.addAllParams(list);
		for (Player tableplayer : table.getSeatMap().values()) {
			tableplayer.writeSocket(com.build());
		}
		// 检查所有人是否都准备完毕,如果准备完毕,改变牌桌状态并开始发牌
//		table.checkDeal();

	}

	@Override
	public void setMsgTypeMap() {

	}

}
