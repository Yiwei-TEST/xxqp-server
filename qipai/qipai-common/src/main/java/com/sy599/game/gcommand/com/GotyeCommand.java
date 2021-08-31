package com.sy599.game.gcommand.com;

import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by lc on 2017/3/24.
 */
public class GotyeCommand extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        String gotyeId = null;
        if (req.getStrParamsCount()>0) {
            gotyeId=req.getStrParams(0);
        }
        if (StringUtils.isBlank(gotyeId)){
            LogUtil.e("userId:"+player.getUserId()+",seat:"+player.getSeat()+",gotyeId is blank!");
            return;
        }
        BaseTable table = player.getPlayingTable();
        if (table!=null){
        	 for (Player player1:table.getSeatMap().values()){
                 if (player1.getUserId()!=player.getUserId()){
 					ComMsg.ComRes res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_gotye, player.getSeat(), String.valueOf(player.getUserId()), gotyeId).build();
                     player1.writeSocket(res);
                 }
             }
        }else{
            LogUtil.e("userId:"+player.getUserId()+",seat:"+player.getSeat()+",playingTable is empty!");
            return;
        }
    }

    @Override
    public void setMsgTypeMap() {

    }

}
