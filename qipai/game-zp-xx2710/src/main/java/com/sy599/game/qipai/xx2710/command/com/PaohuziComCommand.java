package com.sy599.game.qipai.xx2710.command.com;


import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.xx2710.bean.Xx2710Player;
import com.sy599.game.qipai.xx2710.bean.Xx2710Table;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;


public class PaohuziComCommand extends BaseCommand<Xx2710Player> {
	@Override
	public void execute(Xx2710Player player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);

		Xx2710Table table = player.getPlayingTable(Xx2710Table.class);
		if (table == null) {
			return;
		}
		synchronized (table) {
			if (req.getCode()==131){
				player.setAutoPlay(false,table);
				player.setLastOperateTime(System.currentTimeMillis());
			}else if (req.getCode() == WebSocketMsgType.req_code_zzzp_piaofen) {
	            player.setAutoPlay(false, table);
	            player.setLastOperateTime(System.currentTimeMillis());
	            table.piaoFen(player, req.getParams(0));
	        }
		}
		
	}

	@Override
	public void setMsgTypeMap() {

	}

}
