package com.sy599.game.qipai.cdtlj.command.play;

import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.cdtlj.bean.CdtljPlayer;
import com.sy599.game.qipai.cdtlj.bean.CdtljTable;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class CdtljComCommand extends BaseCommand<CdtljPlayer> {

    @Override
    public void execute(CdtljPlayer player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        CdtljTable table = player.getPlayingTable(CdtljTable.class);
        if (table == null) {
            return;
        }
        synchronized (table) {
            switch (req.getCode()) {
                case 131:
                    player.setAutoPlay(false, table);
                    player.setLastOperateTime(System.currentTimeMillis());
                    break;
                case 3100:// WebSocketMsgType.REQ_CDTLJ_JIAOZHU:
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
                case 3101: // WebSocketMsgType.REQ_CDTLJ_XUANZHU:
                    int zhu = req.getParams(0);
                    table.playXuanzhu(player, zhu);
                    break;
                case 3106:// WebSocketMsgType.REQ_CDTLJ_XUANDUI:
                	int dui = req.getParams(0);// 1=1v3模式  ;2 =队友模式
                	table.playerXuanDui(player, dui);
                    break;
                case 3103: //WebSocketMsgType.RES_CHUPAI_RECORD://查出牌
                	table.playChuPaiRecord(player);
                    break;
                case 3107://WebSocketMsgType.REQ_CDTLJ_XUANDIPAI://
                    int index = req.getParams(0);
                    table.playXuanDiPai(player,index);
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
