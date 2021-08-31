package com.sy599.game.qipai.diantuo.command.play;

import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.diantuo.bean.DianTuoPlayer;
import com.sy599.game.qipai.diantuo.bean.DianTuoTable;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class DianTuoComCommand extends BaseCommand<DianTuoPlayer> {

    @Override
    public void execute(DianTuoPlayer player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        DianTuoTable table = player.getPlayingTable(DianTuoTable.class);
        if (table == null) {
            return;
        }
        synchronized (table) {
            switch (req.getCode()) {
                case 131:
                    player.setAutoPlay(false, table);
                    player.setLastOperateTime(System.currentTimeMillis());
                    break;
                case WebSocketMsgType.DUZHAN_DIANTUO:
                	 int action = req.getParams(0);
                	 table.playDuzhan(player, action);
                    break;
                    
                    
                case WebSocketMsgType.HUOFEN_DIANTUO:
               	 int type = req.getParams(0);
               	 table.playFenCards(player, type);
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
