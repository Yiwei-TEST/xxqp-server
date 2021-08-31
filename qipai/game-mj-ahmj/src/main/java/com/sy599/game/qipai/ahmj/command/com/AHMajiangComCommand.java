package com.sy599.game.qipai.ahmj.command.com;

import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.*;
import com.sy599.game.qipai.ahmj.bean.AhmjPlayer;
import com.sy599.game.qipai.ahmj.bean.AhmjTable;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class AHMajiangComCommand extends BaseCommand<AhmjPlayer> {
	@Override
	public void execute(AhmjPlayer player, MessageUnit message) throws Exception {
		AhmjTable table = player.getPlayingTable(AhmjTable.class);
		if (table == null) {
			return;
		}
		ComReq req = (ComReq) this.recognize(ComReq.class, message);
		synchronized (table) {
			switch (req.getCode()) {
				case WebSocketMsgType.req_code_tuoguan:
					boolean autoPlay = req.getParamsCount() > 0 && req.getParams(0) == 1;
					player.setAutoPlay(autoPlay, true);
					LogUtil.msg("ZzMjComCommand|setAutoPlay|" + player.getPlayingTableId() + "|" + player.getSeat() + "|" + player.getUserId() + "|" + (autoPlay ? 1 : 0) + "|" + 1);
					break;
			}
		}
	}

	@Override
	public void setMsgTypeMap() {

	}

}
