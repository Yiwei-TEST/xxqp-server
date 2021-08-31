package com.sy599.game.gcommand.com;

import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class UpdateLatitudeLongitudeCommand extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);
		BaseTable table = player.getPlayingTable();
		if(table != null){
			int paramsCount=req.getParamsCount();
			if(paramsCount > 0){
				for(int i=0;i<paramsCount;i++){
					long userId = req.getParams(i);
					for (Player player1:table.getSeatMap().values()){
						if (player1.getUserId()==userId){
							if(player1.getMyExtend().getLatitudeLongitude() != null && !"".equals(player1.getMyExtend().getLatitudeLongitude())){
								ComMsg.ComRes res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_latitudeandlongitude, player1.getUserId() + "", player1.getMyExtend().getLatitudeLongitude()).build();
								table.broadMsg(res);
							}else{//没有数据发给传过来的ID 251协议
								ComMsg.ComRes res = SendMsgUtil.buildComRes(WebSocketMsgType.res_com_update_latitudeandlongitude).build();
								player1.writeSocket(res);
							}
						}
					}
				}
			}else{
				ComMsg.ComRes res = SendMsgUtil.buildComRes(WebSocketMsgType.res_com_update_latitudeandlongitude).build();
				table.broadMsg(res);
			}
		}
	}

	@Override
	public void setMsgTypeMap() {
	}

}
