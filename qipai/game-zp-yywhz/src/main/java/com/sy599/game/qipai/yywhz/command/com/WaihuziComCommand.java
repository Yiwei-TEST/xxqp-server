package com.sy599.game.qipai.yywhz.command.com;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.yywhz.bean.WaihuziPlayer;
import com.sy599.game.qipai.yywhz.bean.WaihuziTable;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;

public class WaihuziComCommand extends BaseCommand<WaihuziPlayer> {
	@Override
	public void execute(WaihuziPlayer player0, MessageUnit message) throws Exception {
		WaihuziPlayer player = (WaihuziPlayer) player0;
		ComReq req = (ComReq) this.recognize(ComReq.class, message);

		WaihuziTable table = player.getPlayingTable(WaihuziTable.class);
		if (table == null) {
			return;
		}
		
		
		if(req.getCode() ==  WebSocketMsgType.REQ_DTZ_AUTOPLAY){
			
			boolean autoPlay = req.getParamsCount() > 0 && req.getParams(0) == 1;
			player.setAutoPlay(false,table);
			player.setLastOperateTime(System.currentTimeMillis());
			
	         LogUtil.msg("HzMjComCommand|setAutoPlay|" + player.getPlayingTableId() + "|" + player.getSeat() + "|" + player.getUserId() + "|" + (autoPlay ? 1 : 0) + "|" + 1);
		}
		else if(req.getCode() == WebSocketMsgType.req_code_piao_fen && table.getPiaoFen() >= 1) {
			int paoFen = req.getParams(0);
			player.setPiaoPoint(paoFen <= 0 ? 0 : paoFen);
			ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_piao_fen, player.getSeat(), player.getPiaoPoint());
			for (Player tableplayer : table.getSeatMap().values()) {// 推送客户端玩家抛分情况
				tableplayer.writeSocket(com.build());
			}
			table.checkDeal();
			player.setAutoPlay(false,table);
			player.setLastOperateTime(System.currentTimeMillis());
		}
		
		
		
		
		
//		if (req.getCode() == WebSocketMsgType.req_com_fangzhao) {
//			int pai = req.getParams(0);
//			LogUtil.msgLog.info("----tableId:" + table.getId() + "---userName:" + player.getName() + "------->已确认放招:" + pai);
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
