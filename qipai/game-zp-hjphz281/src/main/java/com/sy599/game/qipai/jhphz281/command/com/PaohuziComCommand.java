package com.sy599.game.qipai.jhphz281.command.com;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.jhphz281.bean.JhPhzPlayer;
import com.sy599.game.qipai.jhphz281.bean.JhPhzTable;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class PaohuziComCommand extends BaseCommand<JhPhzPlayer> {
	@Override
	public void execute(JhPhzPlayer player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);

		JhPhzTable table = player.getPlayingTable(JhPhzTable.class);
		if (table == null) {
			return;
		}
		if (req.getCode()==131){
			player.setAutoPlay(false,table);
			player.setLastOperateTime(System.currentTimeMillis());
		}
		
//		else if (req.getCode() == WebSocketMsgType.req_com_fangzhao) {
//			int pai = req.getParams(0);
//            LogUtil.msgLog.info("SyPhz|" + table.getId() + "|" + table.getPlayBureau() + "|" + player.getUserId() + "|" + player.getSeat() + "|fangZhao|2|" + pai);
//			player.setFangZhao(1);
//			List<Integer> cards = new ArrayList<>(Arrays.asList(pai));
//			table.play(player, cards, 0);
//			
//			// player.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao, player.getUserId() + "", 1 + "");
//			for (Player playerTemp : table.getSeatMap().values()) {
//				playerTemp.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao, player.getUserId() + "", 1 + "");
//			}
//		}

	}

	@Override
	public void setMsgTypeMap() {

	}

}
