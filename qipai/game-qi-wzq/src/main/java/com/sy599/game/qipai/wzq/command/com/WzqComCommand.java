package com.sy599.game.qipai.wzq.command.com;

import com.sy599.game.character.Player;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.wzq.bean.WzqPlayer;
import com.sy599.game.qipai.wzq.bean.WzqTable;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class WzqComCommand extends BaseCommand<WzqPlayer> {


    @Override
    public void setMsgTypeMap() {

    }

    @Override
    public void execute(WzqPlayer player, MessageUnit message) throws Exception {
        WzqTable table = player.getPlayingTable(WzqTable.class);
        if (table == null) {
            return;
        }
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        synchronized (table) {
            switch (req.getCode()) {
                case WebSocketMsgType.res_code_wzq_cost_type:
                    // 选择使用分类型
                    int costType = req.getParamsCount() > 0 ? req.getParams(0) : 1;
                    table.choseCostType(player, costType);
                    break;
                case WebSocketMsgType.res_code_wzq_giveUp:
                    table.giveUp(player);
                    break;
            }
        }
    }


}
