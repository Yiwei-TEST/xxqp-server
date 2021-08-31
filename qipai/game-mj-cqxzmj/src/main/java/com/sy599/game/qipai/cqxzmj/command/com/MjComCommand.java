package com.sy599.game.qipai.cqxzmj.command.com;

import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.cqxzmj.bean.CqxzMjPlayer;
import com.sy599.game.qipai.cqxzmj.bean.CqxzMjTable;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class MjComCommand extends BaseCommand<CqxzMjPlayer> {

    @Override
    public void execute(CqxzMjPlayer player, MessageUnit message) throws Exception {
        CqxzMjTable table = player.getPlayingTable(CqxzMjTable.class);
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
                case WebSocketMsgType.req_code_cqxzmj_hz:
                    player.setAutoPlay(false, true);
                    table.huanZhang(player, req.getParamsList());
                    break;
                case WebSocketMsgType.req_code_cqxzmj_dq:
                    player.setAutoPlay(false, true);
                    table.dingQue(player, req.getParams(0));
                    break;
            }
        }
    }

    @Override
    public void setMsgTypeMap() {
        // TODO Auto-generated method stub
    }

}
