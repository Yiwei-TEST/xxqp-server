package com.sy599.game.qipai.nanxmj.command.com;

import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.nanxmj.bean.NanxMjPlayer;
import com.sy599.game.qipai.nanxmj.bean.NanxMjTable;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class MjComCommand extends BaseCommand<NanxMjPlayer> {

    @Override
    public void execute(NanxMjPlayer player, MessageUnit message) throws Exception {
        NanxMjTable table = player.getPlayingTable(NanxMjTable.class);
        if (table == null) {
            return;
        }
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        synchronized (table) {
            switch (req.getCode()) {
                case WebSocketMsgType.req_code_tuoguan:
                    boolean autoPlay = req.getParamsCount() > 0 && req.getParams(0) == 1;
                    player.setAutoPlay(autoPlay, true);
                    LogUtil.msg("NxkwMjComCommand|setAutoPlay|" + player.getPlayingTableId() + "|" + player.getSeat() + "|" + player.getUserId() + "|" + (autoPlay ? 1 : 0) + "|" + 1);
                    break;
                case WebSocketMsgType.req_code_piaoFen:
                    player.setAutoPlay(false, true);
                    table.piaoFen(player, req.getParams(0));
                    break;
            }
        }
    }

    @Override
    public void setMsgTypeMap() {
        // TODO Auto-generated method stub
    }

}
