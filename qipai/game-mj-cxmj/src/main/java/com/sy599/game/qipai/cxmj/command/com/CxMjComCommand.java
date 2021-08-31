package com.sy599.game.qipai.cxmj.command.com;

import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.cxmj.bean.CxMjPlayer;
import com.sy599.game.qipai.cxmj.bean.CxMjTable;
import com.sy599.game.qipai.cxmj.constant.CxMjConstants;
import com.sy599.game.qipai.cxmj.constant.CxMj;
import com.sy599.game.qipai.cxmj.tool.CxMjTool;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.ArrayList;
import java.util.List;

public class CxMjComCommand extends BaseCommand<CxMjPlayer> {

    @Override
    public void execute(CxMjPlayer player, MessageUnit message) throws Exception {
        CxMjTable table = player.getPlayingTable(CxMjTable.class);
        if (table == null) {
            return;
        }
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        synchronized (table) {
            switch (req.getCode()) {
                case WebSocketMsgType.req_code_tuoguan:
                    boolean autoPlay = req.getParamsCount() > 0 && req.getParams(0) == 1;
                    player.setAutoPlay(autoPlay, true);
                    LogUtil.msg("CxMjComCommand|setAutoPlay|" + player.getPlayingTableId() + "|" + player.getSeat() + "|" + player.getUserId() + "|" + (autoPlay ? 1 : 0) + "|" + 1);
                    break;
                case WebSocketMsgType.req_code_cxmj_gangBu:
                    player.setAutoPlay(false, true);
                    int id = req.getParams(0);
                    table.gangBuPai(id,player);
                    break;
            }
        }
    }

    @Override
    public void setMsgTypeMap() {
        // TODO Auto-generated method stub

    }

}
