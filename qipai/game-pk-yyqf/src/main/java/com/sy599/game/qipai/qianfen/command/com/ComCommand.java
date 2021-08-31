package com.sy599.game.qipai.qianfen.command.com;

import com.sy599.game.qipai.qianfen.bean.QianfenPlayer;
import com.sy599.game.qipai.qianfen.bean.QianfenTable;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class ComCommand extends  BaseCommand<QianfenPlayer> {

	@Override
	public void execute(QianfenPlayer player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);
		QianfenTable table = player.getPlayingTable(QianfenTable.class);
		BaseCommand action = null;
		switch (req.getCode()){
			case WebSocketMsgType.REQ_CARD_MARKER:
				action=new CardMarkerCommand();
				break;
			case CutCardsCommand.REQ_CUT_CARDS_COMMAND:
				action=new CutCardsCommand();
				break;
			case WebSocketMsgType.REQ_COM_GIVEUP:
				break;
			case WebSocketMsgType.COM_SELECT_SEAT:
				break;
			case WebSocketMsgType.REQ_GOTYE:
				break;
			case WebSocketMsgType.REQ_RECORD:
				break;
			case WebSocketMsgType.REQ_DTZ_AUTOPLAY:
				player.setAutoPlay(false,table);
				player.setLastOperateTime(System.currentTimeMillis());
				break;
			default:
		}

		if (action!=null){
			action.setPlayer(player);
			action.execute(player, message);
		}else{
			LogUtil.errorLog.error("qianfen command not exists:msgType={},code={}"+message.getMsgType(),req.getCode());
		}

	}

	@Override
	public void setMsgTypeMap() {
	}

}
