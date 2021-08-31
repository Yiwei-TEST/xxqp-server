package com.sy599.game.qipai.sandh.command.play;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.qipai.sandh.bean.SandhPlayer;
import com.sy599.game.qipai.sandh.bean.SandhTable;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class SandhComCommand extends BaseCommand<SandhPlayer> {

    @Override
    public void execute(SandhPlayer player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        SandhTable table = player.getPlayingTable(SandhTable.class);
        if (table == null) {
            return;
        }
        synchronized (table) {
            switch (req.getCode()) {
                case 131:
                    player.setAutoPlay(false, table);
                    player.setLastOperateTime(System.currentTimeMillis());
                    break;
                case WebSocketMsgType.REQ_JIAOFEN:
                	 int fen = req.getParams(0);
                	 int pai  = req.getParams(1);
                	 table.playJiaoFen(player, fen, pai);
                    break;
                case WebSocketMsgType.REQ_XUANZHU:
                	int zhu = req.getParams(0);
                	table.playXuanzhu(player, zhu);
                    break;
                case WebSocketMsgType.RES_CHUPAI_RECORD:
                	table.playChuPaiRecord(player);
                    break;
                case WebSocketMsgType.RES_Liushou:
                	int color = req.getParams(0);
                	table.playLiushou(player,color);
                    break;
                case WebSocketMsgType.RES_TOUX:
                	int type = req.getParams(0);
                	table.playTouxiang(player,type);
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
