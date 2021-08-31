package com.sy599.game.qipai.sypaohuzi.command.com;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.sypaohuzi.bean.SyPaohuziPlayer;
import com.sy599.game.qipai.sypaohuzi.bean.SyPaohuziTable;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PaohuziComCommand extends BaseCommand<SyPaohuziPlayer> {
	@Override
	public void execute(SyPaohuziPlayer player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);

		SyPaohuziTable table = player.getPlayingTable(SyPaohuziTable.class);
		if (table == null) {
			return;
		}
		if (req.getCode()==131){
			player.setAutoPlay(false,table);
			player.setLastOperateTime(System.currentTimeMillis());
		}else if (req.getCode() == WebSocketMsgType.req_com_fangzhao) {
			int pai = req.getParams(0);
            LogUtil.msgLog.info("SyPhz|" + table.getId() + "|" + table.getPlayBureau() + "|" + player.getUserId() + "|" + player.getSeat() + "|fangZhao|2|" + pai);
			player.setFangZhao(1);
            player.setAutoPlay(false,table);
            player.setLastOperateTime(System.currentTimeMillis());
			List<Integer> cards = new ArrayList<>(Arrays.asList(pai));
			table.play(player, cards, 0);
			
			// player.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao, player.getUserId() + "", 1 + "");
			for (Player playerTemp : table.getSeatMap().values()) {
				playerTemp.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao, player.getUserId() + "", 1 + "");
			}
		}else if(req.getCode() ==WebSocketMsgType.req_code_sybp_chui){
			player.setAutoPlay(false, table);
			table.chui(player,req.getParams(0));
		}

	}

	@Override
	public void setMsgTypeMap() {

	}

}
