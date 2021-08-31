package com.sy599.game.qipai.qianfen.command.com;

import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.qianfen.bean.QianfenPlayer;
import com.sy599.game.qipai.qianfen.bean.QianfenTable;

public class CardMarkerCommand extends BaseCommand<QianfenPlayer> {

	@Override
	public void execute(QianfenPlayer player, MessageUnit message) throws Exception {
		if(player==null)return;
		QianfenTable table = player.getPlayingTable();
		if(table==null)return;
		table.sendCardMarker(player);
	}

	@Override
	public void setMsgTypeMap() {
		// TODO Auto-generated method stub

	}

}
