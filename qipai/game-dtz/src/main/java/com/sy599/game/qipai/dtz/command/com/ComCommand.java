package com.sy599.game.qipai.dtz.command.com;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.dtz.command.play.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class ComCommand extends BaseCommand {

	@Override
	public void execute(Player player0, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);

		BaseCommand action;
		switch (req.getCode()){
			case WebSocketMsgType.REQ_COM_GIVEUP:
				action=new GiveupCommand();
				break;
			case WebSocketMsgType.COM_SELECT_SEAT:
				action=new PlayerSelectSeatCommand();
				break;
			case WebSocketMsgType.REQ_GOTYE:
				action=new GotyeCommand();
				break;
			case WebSocketMsgType.REQ_RECORD:
				action=new RecordCommand();
				break;
			case WebSocketMsgType.REQ_CARD_MARKER:
				action=new CardMarkerCommand();
				break;
			case WebSocketMsgType.REQ_DTZ_AUTOPLAY:
				action=new AutoPlayCommand();
				break;
			default:
				action=new CutCardCommand();
		}

		action.setPlayer(player);
		action.execute(player0, message);
	}

	@Override
	public void setMsgTypeMap() {
	}

}
