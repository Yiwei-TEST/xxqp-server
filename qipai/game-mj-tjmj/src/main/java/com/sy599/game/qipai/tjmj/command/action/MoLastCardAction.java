package com.sy599.game.qipai.tjmj.command.action;

import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.qipai.tjmj.bean.TjMjPlayer;
import com.sy599.game.qipai.tjmj.bean.TjMjTable;
import com.sy599.game.qipai.tjmj.command.AbsCodeCommandExecutor;
import com.sy599.game.websocket.constant.WebSocketMsgType;

/**
 * 最后一张牌海底捞月, 与服务器指令做比较
 *
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class MoLastCardAction extends AbsCodeCommandExecutor<TjMjTable, TjMjPlayer> {
    @Override
    public Integer actionCode() {
        return WebSocketMsgType.req_com_lastmajiang;  //缺省操作
    }

    @Override
    public AbsCodeCommandExecutor.GlobalCommonIndex globalCommonIndex() {
        return GlobalCommonIndex.COMMAND_INDEX;
    }

    @Override
    public void execute(TjMjTable table, TjMjPlayer player, CarryMessage carryMessage) {
//        ComMsg.ComReq req = carryMessage.parseFrom(ComMsg.ComReq.class);
//        int action = req.getParams(0);
//        table.moLastMajiang(player, action);
    }

}
