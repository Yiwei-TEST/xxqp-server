package com.sy599.game.qipai.dtz.command.play;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.qipai.dtz.bean.DtzPlayer;
import com.sy599.game.qipai.dtz.bean.DtzTable;

public class CardMarkerCommand extends BaseCommand {

	@Override
	public void execute(Player player0, MessageUnit message) throws Exception {
		DtzPlayer player=(DtzPlayer)player0;
		if(player==null)return;
		DtzTable table = player.getPlayingTable(DtzTable.class);
		if(table==null)return;
		//检查牌桌的状态
//		if(table.getState() != table_state.play)return;
		synchronized (player){
			table.sendCardMarker(player);
		}
	}

	@Override
	public void setMsgTypeMap() {
		// TODO Auto-generated method stub

	}

}
