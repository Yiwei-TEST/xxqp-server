package com.sy599.game.gcommand.com;

import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class AfkCommand extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);
		BaseTable table = player.getPlayingTable();
		if (table == null) {
			return;
		}
		
//		MajiangTable majiangTable=player.getPlayingTable(MajiangTable.class);
//		majiangTable.getActionSeatMap();
		// 1是进入后台 2是从后台回来
		int state = req.getParams(0);
		if (state == 1) {
			player.setIsEntryTable(SharedConstants.table_afk);
			table.broadIsOnlineMsg(player, SharedConstants.table_afk);
		} else {
			player.setIsEntryTable(SharedConstants.table_online);
			table.broadIsOnlineMsg(player, SharedConstants.table_online);
		}

	}

	@Override
	public void setMsgTypeMap() {

	}

}
