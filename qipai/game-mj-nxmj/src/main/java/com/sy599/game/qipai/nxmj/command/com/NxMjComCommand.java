package com.sy599.game.qipai.nxmj.command.com;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.nxmj.bean.NxMjPlayer;
import com.sy599.game.qipai.nxmj.bean.NxMjTable;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class NxMjComCommand extends BaseCommand<NxMjPlayer> {

    @Override
	public void setMsgTypeMap() {

	}

	@Override
	public void execute(NxMjPlayer player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);
		NxMjTable table = player.getPlayingTable(NxMjTable.class);
		
		
		if (req.getCode() == WebSocketMsgType.req_com_lastmajiang) {
			int action = req.getParams(0);
			table.moLastMajiang(player, action);
		}else if(req.getCode() ==  WebSocketMsgType.req_code_tuoguan){
			 boolean autoPlay = req.getParamsCount() > 0 && req.getParams(0) == 1;
	         player.setAutoPlay(autoPlay, true);
	         LogUtil.msg("HzMjComCommand|setAutoPlay|" + player.getPlayingTableId() + "|" + player.getSeat() + "|" + player.getUserId() + "|" + (autoPlay ? 1 : 0) + "|" + 1);
		}
		else if(req.getCode() == WebSocketMsgType.req_code_piao_fen && table.getKePiao() == 1) {
			int paoFen = req.getParams(0);
			player.setPiaoPoint(paoFen <= 0 ? 0 : paoFen);
			ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_piao_fen, player.getSeat(), player.getPiaoPoint());
			for (Player tableplayer : table.getSeatMap().values()) {// 推送客户端玩家抛分情况
				tableplayer.writeSocket(com.build());
			}
			table.checkDeal();
		}
	}

}
