package com.sy599.game.gcommand.com;

import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

/**
 * Created by lc on 2017/3/24.
 */
public class LatitudeLongitudeCommand extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        int code = 1;
        if (req.getParamsCount() > 0) {
            code = req.getParams(0);
        }
        BaseTable table = player.getPlayingTable();
        if (code == 1) {
            //前台定位经纬度
            if (req.getStrParamsCount() > 0) {
                String latitudeLongitude = req.getStrParams(0);
                player.getMyExtend().setLatitudeLongitude(latitudeLongitude);
                ComMsg.ComRes res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_latitudeandlongitude, player.getUserId() + "", latitudeLongitude).build();
                if (table != null) {
                    table.broadMsg(res);
                } else {
                    player.writeSocket(res);
                }

            } else {
                player.getMyExtend().setLatitudeLongitude("");
            }
        } else if (code == 2) {
            //获取同一个桌子上的一个seat玩家经纬度
            if (table == null) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_32));
                return;
            }
            int seat = req.getParams(1);
            Player seatPlayer = table.getSeatMap().get(seat);
            if (seatPlayer == null) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_33));
                return;
            }
            ComMsg.ComRes res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_latitudeandlongitude, player.getUserId() + "", seatPlayer.getMyExtend().getLatitudeLongitude()).build();
            player.writeSocket(res);
        }


    }

    @Override
    public void setMsgTypeMap() {

    }

}
