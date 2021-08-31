package com.sy599.game.qipai.hsth.command.play;

import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.hsth.bean.HsthPlayer;
import com.sy599.game.qipai.hsth.bean.HsthTable;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class HsthComCommand extends BaseCommand<HsthPlayer> {

    @Override
    public void execute(HsthPlayer player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        HsthTable table = player.getPlayingTable(HsthTable.class);
        if (table == null) {
            return;
        }
        synchronized (table) {
            switch (req.getCode()) {
                case 131:
                    player.setAutoPlay(false, table);
                    player.setLastOperateTime(System.currentTimeMillis());
                    break;
//                case WebSocketMsgType.DUZHAN_DIANTUO:
//                	 int action = req.getParams(0);
//                	 table.playDuzhan(player, action);
//                    break;
                case 4102://获取分牌
               	     int type = req.getParams(0);
               	     table.playFenCards(player, type);
                    break;
                case WebSocketMsgType.req_code_nsb_mingpai://明牌
                    table.playShowCards(player);
                    break;
            }
            if (table.isAutoPlay()&&!player.isAutoPlay()) {
                player.setAutoPlay(false, table);
                player.setLastOperateTime(System.currentTimeMillis());
            }
        }
    }

    @Override
    public void setMsgTypeMap() {

    }



}
