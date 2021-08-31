package com.sy599.game.qipai.lyzp.command.com;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.lyzp.been.LyzpPlayer;
import com.sy599.game.qipai.lyzp.been.LyzpTable;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PaohuziComCommand extends BaseCommand<LyzpPlayer> {
	@Override
	public void execute(LyzpPlayer player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);

		LyzpTable table = player.getPlayingTable(LyzpTable.class);
		if (table == null) {
			return;
		}
		if (req.getCode()==131){
			player.setAutoPlay(false,table);
			player.setLastOperateTime(System.currentTimeMillis());
		}else if (req.getCode() == WebSocketMsgType.req_com_fangzhao) {
			int pai = req.getParams(0);
			player.setFangZhao(1);
            player.setAutoPlay(false,table);
            player.setLastOperateTime(System.currentTimeMillis());
			List<Integer> cards = new ArrayList<>(Arrays.asList(pai));
			table.play(player, cards, 0);
			for (Player playerTemp : table.getSeatMap().values()) {
				playerTemp.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao, player.getUserId() + "", 1 + "");
			}
			logMsg(table,player,pai,"确认放招");
		} else if (req.getCode() == WebSocketMsgType.req_code_lyzp_cbdb_hht) {
			int pai = req.getParams(0);
			List<Integer> cards = new ArrayList<>(Arrays.asList(pai));
			player.setOnlyHht(1);
            player.setAutoPlay(false,table);
            player.setLastOperateTime(System.currentTimeMillis());
			table.play(player, cards, 0);
			logMsg(table,player,pai,"确认吃边打边");
		} else if(req.getCode() == WebSocketMsgType.req_code_lyzp_cbdbfz){
			int pai = req.getParams(0);
			List<Integer> cards = new ArrayList<>(Arrays.asList(pai));
			player.setFangZhao(1);
			player.setOnlyHht(1);
            player.setAutoPlay(false,table);
            player.setLastOperateTime(System.currentTimeMillis());
			table.play(player, cards, 0);
			for (Player playerTemp : table.getSeatMap().values()) {
				playerTemp.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao, player.getUserId() + "", 1 + "");
			}
			logMsg(table,player,pai,"确认放招和吃边打边");
		}

	}

	private void logMsg(LyzpTable table,LyzpPlayer player,int cardId,String msg){
		StringBuilder sb = new StringBuilder("lyzp");
		sb.append("|").append(table.getId());
		sb.append("|").append(table.getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append(player.isAutoPlay() ? 1 : 0);
		sb.append("|").append(cardId);
		sb.append("|").append(msg);
		LogUtil.msgLog.info(sb.toString());
	}

	@Override
	public void setMsgTypeMap() {

	}

}
