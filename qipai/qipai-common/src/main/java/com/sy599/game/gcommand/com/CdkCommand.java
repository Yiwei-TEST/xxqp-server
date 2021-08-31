package com.sy599.game.gcommand.com;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class CdkCommand extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);
		String cdk = req.getStrParams(0);
		
	}

	@Override
	public void setMsgTypeMap() {
		// TODO Auto-generated method stub

	}

}
