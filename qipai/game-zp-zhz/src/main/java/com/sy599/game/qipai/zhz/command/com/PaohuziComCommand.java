package com.sy599.game.qipai.zhz.command.com;


import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.zhz.been.ZhzPlayer;
import com.sy599.game.qipai.zhz.been.ZhzTable;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;


public class PaohuziComCommand extends BaseCommand<ZhzPlayer> {
	@Override
	public void execute(ZhzPlayer player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);

		ZhzTable table = player.getPlayingTable(ZhzTable.class);
		if (table == null) {
			return;
		}
		if (req.getCode()==131){
			player.setAutoPlay(false,table);
			player.setLastOperateTime(System.currentTimeMillis());
		}
	}

	@Override
	public void setMsgTypeMap() {

	}

}
