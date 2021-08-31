package com.sy599.game.qipai.dtz.command.play;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.dtz.bean.DtzPlayer;
import com.sy599.game.qipai.dtz.bean.DtzTable;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class CutCardCommand extends BaseCommand {

	@Override
	public void execute(Player player0, MessageUnit message) throws Exception {
		DtzPlayer player=(DtzPlayer)player0;
		ComReq req = (ComReq) this.recognize(ComReq.class, message);
		DtzTable table = player.getPlayingTable(DtzTable.class);
		if (table == null) {
			return;
		}

		synchronized (table) {
			if (req.getCode() == WebSocketMsgType.req_com_enter_cutcard) {
				enterCutCard(table, player);

			} else if (req.getCode() == WebSocketMsgType.req_com_cutcard) {
				cutCard(table, player, req);

			}
		}

	}

	private void enterCutCard(DtzTable table, DtzPlayer player) {
		boolean allReady = true;
		for (Player tableplayer : table.getSeatMap().values()) {
			if (tableplayer.getUserId() == player.getUserId()) {
				continue;
			}
			if (tableplayer.getState() != player_state.ready) {
				allReady = false;
				break;
			}
		}

		if (allReady) {
			
			ComRes.Builder builder = null;
			
			for (Player tableplayer : table.getSeatMap().values()) {
				if (tableplayer.getUserId() == player.getUserId()) {
					builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_com_code_cutcard, -1, 0);
				} else {
					builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_com_code_cutcard, -1, 1);
				}
				tableplayer.writeSocket(builder.build());
			}
			
		} else {
			player.writeErrMsg(LangMsg.code_210);
		}
	}

	private void cutCard(DtzTable table, DtzPlayer player, ComReq req) {
		int progress = req.getParams(0);
		ComRes.Builder builder = null;
		
		for (Player tableplayer : table.getSeatMap().values()) {
			if (tableplayer.getUserId() == player.getUserId()) {
				builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_com_code_cutcard, progress, 0);
			} else {
				builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_com_code_cutcard, progress, 1);
			}
			tableplayer.writeSocket(builder.build());
		}
	}

	@Override
	public void setMsgTypeMap() {

	}

}
