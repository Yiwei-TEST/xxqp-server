package com.sy599.game.qipai.nymj.command.action;

import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.qipai.nymj.bean.NyMjPlayer;
import com.sy599.game.qipai.nymj.bean.NyMjTable;
import com.sy599.game.qipai.nymj.command.AbsCodeCommandExecutor;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

/**
 * 托管
 *
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class AutoPlayAction extends AbsCodeCommandExecutor<NyMjTable, NyMjPlayer> {

    @Override
    public Integer actionCode() {
        return WebSocketMsgType.req_code_tuoguan;
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return GlobalCommonIndex.COMMAND_INDEX;
    }

    @Override
    public void execute(NyMjTable table, NyMjPlayer player, CarryMessage carryMessage) {
        ComMsg.ComReq req = carryMessage.parseFrom(ComMsg.ComReq.class);
        boolean autoPlay = req.getParamsCount() > 0 && req.getParams(0) == 1;
        player.setAutoPlay(autoPlay, true);
        LogUtil.msg("HzMjComCommand|setAutoPlay|" + player.getPlayingTableId() + "|" + player.getSeat() + "|" + player.getUserId() + "|" + (autoPlay ? 1 : 0) + "|" + 1);
    }
}

