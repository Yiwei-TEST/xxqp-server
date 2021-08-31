package com.sy599.game.qipai.ldfpf.command.com;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.ldfpf.been.LdfpfPlayer;
import com.sy599.game.qipai.ldfpf.been.LdfpfTable;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PaohuziComCommand extends BaseCommand<LdfpfPlayer> {
	@Override
	public void execute(LdfpfPlayer player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);

		LdfpfTable table = player.getPlayingTable(LdfpfTable.class);
		if (table == null) {
			return;
		}
		if (req.getCode()==131){
			player.setAutoPlay(false,table);
			player.setLastOperateTime(System.currentTimeMillis());
		}else if (req.getCode() == WebSocketMsgType.req_com_fangzhao) {
			int pai = req.getParams(0);
			LogUtil.msgLog.info("----tableId:" + table.getId() + "---userName:" + player.getName() + "------->已确认放招:" + pai);
			player.setFangZhao(1);
			List<Integer> cards = new ArrayList<>(Arrays.asList(pai));
            player.setAutoPlay(false,table);
            player.setLastOperateTime(System.currentTimeMillis());
			table.play(player, cards, 0);

			// player.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao, player.getUserId() + "", 1 + "");
			for (Player playerTemp : table.getSeatMap().values()) {
				playerTemp.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao, player.getUserId() + "", 1 + "");
			}
		}else if(req.getCode() == WebSocketMsgType.req_code_ldfpf_lockhand){
			table.lockHand(player);
		} else if (req.getCode() == WebSocketMsgType.req_code_ldfpf_daniao) {
			player.setAutoPlay(false, table);
			player.setLastOperateTime(System.currentTimeMillis());
			table.daNiao(player, req.getParams(0));
		} else if (req.getCode() == WebSocketMsgType.req_code_first_xipai) {
			player.setAutoPlay(false, table);
			player.setLastOperateTime(System.currentTimeMillis());
			if(!table.isFirstXipai()){
				player.writeErrMsg(LangMsg.code_265);
				return;
			}
			if(!table.checkXipaiCreditOnStartNext(player)){
				player.writeErrMsg(LangMsg.code_266);
				return;
			}
			table.handleFirstXipai(player, req.getParams(0));
		}

	}

	@Override
	public void setMsgTypeMap() {

	}

}
