package com.sy599.game.qipai.yzphz.command.com;

import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.yzphz.bean.YzPhzPlayer;
import com.sy599.game.qipai.yzphz.bean.YzPhzTable;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class PaohuziComCommand extends BaseCommand<YzPhzPlayer> {
	@Override
	public void execute(YzPhzPlayer player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);

		YzPhzTable table = player.getPlayingTable(YzPhzTable.class);
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
