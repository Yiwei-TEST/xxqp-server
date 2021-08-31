package com.sy599.game.qipai.qzmj.command.com;

import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.qzmj.bean.QzMjPlayer;
import com.sy599.game.qipai.qzmj.bean.QzMjTable;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class MjComCommand extends BaseCommand<QzMjPlayer> {

    @Override
    public void execute(QzMjPlayer player, MessageUnit message) throws Exception {
        QzMjTable table = player.getPlayingTable(QzMjTable.class);
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
            }
        }
    }

    @Override
    public void setMsgTypeMap() {
        // TODO Auto-generated method stub
    }

}
