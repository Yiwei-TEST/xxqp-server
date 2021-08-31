package com.sy599.game.qipai.dehmj.command.com;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.dehmj.bean.DehMjPlayer;
import com.sy599.game.qipai.dehmj.bean.DehMjTable;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class DehMjComCommand extends BaseCommand<DehMjPlayer> {

    @Override
	public void setMsgTypeMap() {

	}

	@Override
	public void execute(DehMjPlayer player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);
		DehMjTable table = player.getPlayingTable(DehMjTable.class);
		if (req.getCode() == WebSocketMsgType.req_com_lastmajiang) {
			int action = req.getParams(0);
			table.moLastMajiang(player, action);
		}else if(req.getCode() == WebSocketMsgType.req_code_piao_buy_point && table.getBuyPoint() > 0) {
			int paoFen = req.getParams(0);
			if(table.getBuyPoint() ==1|| table.getBuyPoint() ==3) {
//				if(paoFen>1) {
//					paoFen = 1;
//				}
			}
			if(paoFen<0 ||paoFen>4){
				paoFen = 1;
			}
			player.setPiaoPoint(paoFen <= 0 ? 0 : paoFen);
			ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_buy_point, player.getSeat(), player.getPiaoPoint());
			for (Player tableplayer : table.getSeatMap().values()) {// 推送客户端玩家抛分情况
				tableplayer.writeSocket(com.build());
			}
			table.checkDeal();
		}else if(req.getCode() == WebSocketMsgType.req_code_tuoguan) {
			 boolean autoPlay = req.getParamsCount() > 0 && req.getParams(0) == 1;
		        player.setAutoPlay(autoPlay, true);
		        LogUtil.msg("HzMjComCommand|setAutoPlay|" + player.getPlayingTableId() + "|" + player.getSeat() + "|" + player.getUserId() + "|" + (autoPlay ? 1 : 0) + "|" + 1);
		}else if(req.getCode() == WebSocketMsgType.res_code_bs_Baoting) {
			int baoting = req.getParams(0);
			player.setAutoPlay(false,false);
			if(table.getState()!= table_state.play){
				table.playBaoting(player,-1);
				return;
			}
			table.playBaoting(player,baoting);
		   LogUtil.msg("DehMjComCommand|baoting|" + player.getPlayingTableId() + "|" + player.getSeat() + "|" + player.getUserId() + "|" + baoting+ "|" );
		}else if(req.getCode() == 6104){//带根 杠牌补充刷新接口：防止前端没刷新
			if(table.getDaiGen()!=1){
				return;
			}
        	ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_deh_TMJ, table.getLastTwoMj());
        	player.writeSocket(com.build());
        	table.logdaiGenMsg(player, "refreshDaiGen|" +table.getLastTwoMj());
		}
		
	}



}
