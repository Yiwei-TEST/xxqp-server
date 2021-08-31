package com.sy599.game.qipai.nxphz.command.com;

import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.nxphz.bean.NxphzPlayer;
import com.sy599.game.qipai.nxphz.command.AbsCodeCommandExecutor;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.util.LogUtil;

/**
 * @author Guang.OuYang
 * @description 通用消息
 * @return
 * @date 2019/9/2
 */
public class ComCommand extends BaseCommand<NxphzPlayer> {
    @Override
    public void execute(NxphzPlayer player, MessageUnit message) throws Exception {
        ComReq req = null;
        try {
            req = (ComReq) this.recognize(ComReq.class, message);
            AbsCodeCommandExecutor.getGlobalActionCodeInstance(AbsCodeCommandExecutor.GlobalCommonIndex.COMMAND_INDEX, req.getCode())
                    .orElse(AbsCodeCommandExecutor.getGlobalActionCodeInstance(AbsCodeCommandExecutor.GlobalCommonIndex.COMMAND_INDEX, -1).get())
                    .execute0(player, message, this, req);
        } catch (Exception e) {
            LogUtil.e("CodeCommonErr: " + player.getUserId() + " "+ AbsCodeCommandExecutor.GlobalCommonIndex.COMMAND_INDEX+" " + (req != null ? req.getCode() : "Null") + " " + LogUtil.printlnLog(message.getMessage()), e);
            throw e;
        }
    }

    @Override
    public void setMsgTypeMap() {

    }

}
