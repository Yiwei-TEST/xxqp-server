package com.sy599.game.gcommand.com;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class ConfigCommand extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);
		String config = StringUtil.implode(req.getParamsList(), ",");
		player.setConfig(config);
		player.saveBaseInfo();
	}

	@Override
	public void setMsgTypeMap() {

	}

}
