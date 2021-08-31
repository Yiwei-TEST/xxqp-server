package com.sy599.game.qipai.xtbp.command.play;

import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.xtbp.bean.XtbpPlayer;
import com.sy599.game.qipai.xtbp.bean.XtbpTable;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class XtbpComCommand extends BaseCommand<XtbpPlayer> {

    @Override
    public void execute(XtbpPlayer player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        XtbpTable table = player.getPlayingTable(XtbpTable.class);
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
                case WebSocketMsgType.RES_CHUPAI_RECORD://查出牌
                	table.playChuPaiRecord(player);
                    break;
                case WebSocketMsgType.RES_Liushou:
                	int color = req.getParams(0);
                	table.playLiushou(player,color);
                    break;
                case WebSocketMsgType.RES_TOUX:
                	int type = req.getParams(0);
                	int txtype = type;
                	//1 询问投降。2直接投降
                	table.playTouxiang(player,type,txtype);
                    break;
                case 3116://WebSocketMsgType.RES_XTBP_LaiMi: //喊来米
//                    int type = req.getParams(0);
                    table.playLaiMi(player);
                    break;
                case 3117://WebSocketMsgType.RES_XTBP_PIAOFEN: //飘分
                    int piaofen = req.getParams(0);
                    table.playPiaoFen(player,piaofen);
                    break;
                case 4102://WebSocketMsgType.RES_XTBP_CHAFEN://查出分牌
//                    int type = req.getParams(0);
                    table.playFenPaiRecord(player);
                    break;
                case 3119://WebSocketMsgType.RES__XTBP_CHADI://庄查底牌
//                    int type = req.getParams(0);
                    table.playChaDi(player);
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
