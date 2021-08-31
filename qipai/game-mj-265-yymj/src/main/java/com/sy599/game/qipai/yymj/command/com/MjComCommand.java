package com.sy599.game.qipai.yymj.command.com;

import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.yymj.bean.YyMjPlayer;
import com.sy599.game.qipai.yymj.command.AbsCodeCommandExecutor;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class MjComCommand extends BaseCommand<YyMjPlayer> {

    @Override
    public void setMsgTypeMap() {

    }

    @Override
    public void execute(YyMjPlayer player, MessageUnit message) throws Exception {
        ComReq req = null;
        try {
            req = (ComReq) this.recognize(ComReq.class, message);
            AbsCodeCommandExecutor.getGlobalActionCodeInstance(AbsCodeCommandExecutor.GlobalCommonIndex.COMMAND_INDEX, req.getCode())
                    .orElse(AbsCodeCommandExecutor.getGlobalActionCodeInstance(AbsCodeCommandExecutor.GlobalCommonIndex.COMMAND_INDEX, -1).get())
                    .execute0(player, message, this, req);
        } catch (Exception e) {
            LogUtil.e("CodeCommonErr: " + player.getUserId() + " " + AbsCodeCommandExecutor.GlobalCommonIndex.COMMAND_INDEX + " " + (req != null ? req.getCode() : "Null") + " " + LogUtil.printlnLog(message.getMessage()), e);
            throw e;
        }
    }

}
