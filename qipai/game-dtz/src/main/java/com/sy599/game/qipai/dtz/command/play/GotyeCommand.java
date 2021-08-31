package com.sy599.game.qipai.dtz.command.play;

import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.StringUtils;

/**
 * 语音，只推送给同组的玩家
 * @author zhouhj
 *
 */
public class GotyeCommand extends BaseCommand{
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
        BaseTable basetable = player.getPlayingTable();
        if(basetable == null){
            LogUtil.e("userId:"+player.getUserId()+",seat:"+player.getSeat()+",playingTable is empty!");
            return;
        }
        /*if(basetable.getPlayType() == DtzzConstants.play_type_3POK || basetable.getPlayType() == DtzzConstants.play_type_4POK){
	        DtzTable dtzTable = (DtzTable)basetable;
	        int group = dtzTable.findGroupByPlayer(player);
	        List<DtzPlayer> players = dtzTable.getGroupMap().get(group);
	        for (DtzPlayer pl : players) {
	            if (!pl.equals(player)) {
	                ComMsg.ComRes res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_gotye, player.getSeat(), String.valueOf(player.getUserId()), gotyeId).build();
	                pl.writeSocket(res);
	            }
	        }
        }else{*/
        	for (Player player1:basetable.getSeatMap().values()){
                if (player1.getUserId()!=player.getUserId()){
					ComMsg.ComRes res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_gotye, player.getSeat(), String.valueOf(player.getUserId()), gotyeId).build();
                    player1.writeSocket(res);
                }
            }
        //}
    }

    @Override
    public void setMsgTypeMap() {

    }

}
