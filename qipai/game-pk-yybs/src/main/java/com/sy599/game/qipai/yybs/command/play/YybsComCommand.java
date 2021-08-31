package com.sy599.game.qipai.yybs.command.play;

import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.yybs.bean.YybsPlayer;
import com.sy599.game.qipai.yybs.bean.YybsTable;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class YybsComCommand extends BaseCommand<YybsPlayer> {

    @Override
    public void execute(YybsPlayer player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        YybsTable table = player.getPlayingTable(YybsTable.class);
        if (table == null) {
            return;
        }
        synchronized (table) {
            switch (req.getCode()) {
                case 131:
                    player.setAutoPlay(false, table);
                    player.setLastOperateTime(System.currentTimeMillis());
                    break;
                case WebSocketMsgType.REQ_YYBS_JIAOZHU:
                    int type = req.getParams(0);// 0 弃权   1 抢主 。 2 反主
                    String pai="";
                    if(type==0){
                        pai="";
                    }else{
                        pai = req.getStrParams(0);// 牌
                    }
                	 table.playJiaoZhu(player, type, pai);
                    break;
//               case WebSocketMsgType.REQ_XUANZHU:
               case  WebSocketMsgType.REQ_YYBS_XUANZHU:
                    int zhu = req.getParams(0);
                    table.playXuanzhu(player, zhu);
                    break;
                case  WebSocketMsgType.REQ_YYBS_XUANDUI:
                	int dui = req.getParams(0);// 1=1v3模式  ;2 =队友模式
                	table.playerXuanDui(player, dui);
                    break;
                case WebSocketMsgType.RES_CHUPAI_RECORD://查出牌
                	table.playChuPaiRecord(player);
                    break;
//                case WebSocketMsgType.RES_Liushou:
//                	int color = req.getParams(0);
//                	table.playLiushou(player,color);
//                    break;
//                case WebSocketMsgType.RES_TOUX:
//                	int type = req.getParams(0);
//                	int txtype = type;
//                	//1 询问投降。2直接投降
//                	table.playTouxiang(player,type,txtype);
//                    break;
//                case 3116://WebSocketMsgType.RES_XTBP_LaiMi: //喊来米
////                    int type = req.getParams(0);
//                    table.playLaiMi(player);
//                    break;
//                case 3117://WebSocketMsgType.RES_XTBP_PIAOFEN: //飘分
//                    int piaofen = req.getParams(0);
//                    table.playPiaoFen(player,piaofen);
//                    break;
//                case 4102://WebSocketMsgType.RES_XTBP_CHAFEN://查出分牌
////                    int type = req.getParams(0);
//                    table.playFenPaiRecord(player);
//                    break;
//                case 3119://WebSocketMsgType.RES__XTBP_CHADI://庄查底牌
////                    int type = req.getParams(0);
//                    table.playChaDi(player);
//                    break;
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
