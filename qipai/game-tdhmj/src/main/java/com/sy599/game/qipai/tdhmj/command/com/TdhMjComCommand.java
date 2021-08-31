package com.sy599.game.qipai.tdhmj.command.com;

import java.util.ArrayList;
import java.util.List;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiInfo;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.TingPaiRes;
import com.sy599.game.qipai.tdhmj.bean.TdhMjPlayer;
import com.sy599.game.qipai.tdhmj.bean.TdhMjTable;
import com.sy599.game.qipai.tdhmj.rule.TdhMj;
import com.sy599.game.qipai.tdhmj.tool.TdhMjTool;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class TdhMjComCommand extends BaseCommand<TdhMjPlayer> {

    @Override
	public void setMsgTypeMap() {

	}

	@Override
	public void execute(TdhMjPlayer player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);
		TdhMjTable table = player.getPlayingTable(TdhMjTable.class);
		
		
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
		}else if(req.getCode() == WebSocketMsgType.req_code_TDH_AUTO_MD) {
			int autoMD = req.getParams(0);
			if(autoMD==1){
					List<TdhMj> cards = new ArrayList<>(player.getHandMajiang());
					List<TdhMj> huCards = TdhMjTool.getTingMjs(cards, table, player, true);
					if (huCards == null || huCards.size() == 0) {
						return;
					}
			}
			player.setAutoMD(autoMD);
			ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_code_TDH_AUTO_MD, player.getSeat(), player.getAutoMD());
			player.writeSocket(com.build());
			LogUtil.msg("TdhMjComCommand|setAutoMD|" +table.getId()+"|"+player.getSeat()+"|"+player.getName()+"|"+player.getAutoMD());
		}
		
		
		
		
	}

}
