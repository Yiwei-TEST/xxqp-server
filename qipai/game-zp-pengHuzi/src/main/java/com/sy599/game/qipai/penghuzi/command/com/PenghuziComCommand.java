package com.sy599.game.qipai.penghuzi.command.com;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.penghuzi.bean.PenghuziPlayer;
import com.sy599.game.qipai.penghuzi.bean.PenghuziTable;
import com.sy599.game.qipai.penghuzi.bean.PenghzDisAction;
import com.sy599.game.qipai.penghuzi.tool.PenghuziTool;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PenghuziComCommand extends BaseCommand<PenghuziPlayer> {
	@Override
	public void execute(PenghuziPlayer player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);

		PenghuziTable table = player.getPlayingTable(PenghuziTable.class);
		if (table == null) {
			return;
		}
		if (req.getCode()==131){
			player.setAutoPlay(false,table);
			player.setLastOperateTime(System.currentTimeMillis());
		}else if (req.getCode() == WebSocketMsgType.RES_WUFU_XUANZ) {
			int flag = req.getParams(0);
			if(player.getWufuBaoj()>0){
				return;
			}
			
			int pwp =  player.getPWPTCount(null);
	        if(player.getHandPais().size()!=2||pwp!=4){
	        	return;
	        }
			
            LogUtil.msgLog.info("Penghz|" + table.getId() + "|" + table.getPlayBureau() + "|" + player.getUserId() + "|" + player.getSeat() + "|baojing|" + flag);
            if(flag==1){
            	table.addPlayLog(player.getSeat(), PenghzDisAction.action_WUFU_DENG + "", 1 + "");
            }
			// player.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao, player.getUserId() + "", 1 + "");
			for (Player playerTemp : table.getSeatMap().values()) {
				playerTemp.writeComMessage(WebSocketMsgType.RES_WUFU_XUANZ, player.getSeat(), flag);
			}
			table.wufuXz(player);
			 player.setWufuBaoj(flag);
			
		}else if(req.getCode() == WebSocketMsgType.req_code_piao_PENGHU_DANAIO && table.getDaNiaoWF() >= 1) {
			int paoFen = req.getParams(0);
			table.playDaNiao(player,  paoFen);
		}

	}

//	private void playDaNiao(PenghuziPlayer player, PenghuziTable table, int paoFen) {
//		player.setDaNiaoPoint(paoFen <= 0 ? 0 : paoFen);
//		ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_piao_PENGHU_DANAIO, player.getSeat(), player.getDaNiaoPoint());
//		
//		for (Player tableplayer : table.getSeatMap().values()) {// 推送客户端玩家抛分情况
//			tableplayer.writeSocket(com.build());
//		}
//		table.checkDeal();
//		table.startNext();
//	}

	@Override
	public void setMsgTypeMap() {

	}

}
