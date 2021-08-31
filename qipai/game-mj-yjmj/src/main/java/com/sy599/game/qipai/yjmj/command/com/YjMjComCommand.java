package com.sy599.game.qipai.yjmj.command.com;

import com.sy599.game.qipai.yjmj.bean.YjMjPlayer;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.yjmj.bean.YjMjTable;
import com.sy599.game.websocket.constant.WebSocketMsgType;

/**
 *
 */
public class YjMjComCommand extends BaseCommand<YjMjPlayer> {


    @Override
    public void setMsgTypeMap() {

    }

    @Override
    public void execute(YjMjPlayer player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        switch (req.getCode()){
            case WebSocketMsgType.req_code_tuoguan:
                boolean autoPlay = req.getParamsCount() > 0 && req.getParams(0) == 1;
                player.setAutoPlay(autoPlay, true);
                LogUtil.msg("YjMjComCommand|setAutoPlay|" + player.getPlayingTableId() + "|" + player.getSeat() + "|" + player.getUserId() + "|" + (autoPlay ? 1 : 0) + "|" + 1);
                break;
            case WebSocketMsgType.res_code_ask_dismajiang:
                YjMjTable table = player.getPlayingTable(YjMjTable.class);
                if (table.getActionSeatMap() != null && !table.getActionSeatMap().isEmpty()) {
                    player.writeComMessage(WebSocketMsgType.res_com_code_yjmj_ask_dis, 0);
                } else {
                    player.writeComMessage(WebSocketMsgType.res_com_code_yjmj_ask_dis, 1);
                }
                break;
        }
    }
}
