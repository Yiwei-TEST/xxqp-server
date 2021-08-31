package com.sy599.game.qipai.ddz.command.play;

import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.ddz.bean.DdzPlayer;
import com.sy599.game.qipai.ddz.bean.DdzTable;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class DdzComCommand extends BaseCommand<DdzPlayer> {

    @Override
    public void execute(DdzPlayer player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        DdzTable table = player.getPlayingTable(DdzTable.class);
        if (table == null) {
            return;
        }
        synchronized (table) {
            switch (req.getCode()) {
                case 131:
                    player.setAutoPlay(false, table);
                    player.setLastOperateTime(System.currentTimeMillis());
                    break;
                case WebSocketMsgType.REQ_2RenDDZ_START_CallLandlord://叫地主 4140
                    int params = req.getParams(0);
                    table.playJiaoDiZhu(player, params);
                    break;
                case WebSocketMsgType.REQ_2RenDDZ_START_RobLandlord://抢地主 4141
                    int params1 = req.getParams(0);
                    table.playQiangDiZhu(player, params1);
                    break;
                case WebSocketMsgType.REQ_2RenDDZ_RangPai://让牌 4143
                    int params3= req.getParams(0);
                    table.bankerRangPai(player, params3);
                    break;
                case WebSocketMsgType.REQ_2RenDDZ_SelJiaBei://加倍地主4144
                    int params2 = req.getParams(0);
                    table.playSelectJiaBei(player, params2);
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
