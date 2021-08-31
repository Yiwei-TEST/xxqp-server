package com.sy599.game.gcommand.com;

import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.util.LangHelp;
import com.sy599.game.websocket.netty.coder.MessageUnit;

/**
 * 前端请求洗牌
 */
public class XipaiCommand extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {

		BaseTable table = player.getPlayingTable();
		if (table == null) {
			player.writeErrMsg(LangHelp.getMsg(LangMsg.code_1));
			return;
		}

		if(!table.isXipai()){
			player.writeErrMsg(LangMsg.code_265);
			return;
		}
		if(!table.checkXipaiCreditOnStartNext(player)){
			player.writeErrMsg(LangMsg.code_266);
			return;
		}
//		player.setXipaiStatus(1);
		//扣除洗牌分
		table.calcCreditXipai(player);
		table.addXipaiName(player.getName());
//		ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_xipai,player.getName());
//		for (Player tableplayer : table.getSeatMap().values()) {
//			tableplayer.writeSocket(com.build());
//		}



//		player.setXipaiStatus(0);
//		player.setXipaiCount(player.getXipaiCount()+1);
//		player.setXipaiScore(player.getXipaiScore()+table.getXipaiScoure());
	}

	@Override
	public void setMsgTypeMap() {
	}
}
