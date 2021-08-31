package com.sy599.game.qipai.xpepaohuzi.command.action;

import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.qipai.xpepaohuzi.bean.XPPaohuziPlayer;
import com.sy599.game.qipai.xpepaohuzi.bean.XPPaohuziTable;
import com.sy599.game.qipai.xpepaohuzi.command.AbsCodeCommandExecutor;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 放技能, 组合牌型
 *
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class SkillAction extends AbsCodeCommandExecutor<XPPaohuziTable, XPPaohuziPlayer> {

    @Override
    public Integer actionCode() {
        return WebSocketMsgType.req_code_GU_CHOU;
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return GlobalCommonIndex.COMMAND_INDEX;
    }

    @Override
    public void execute(XPPaohuziTable table, XPPaohuziPlayer player, CarryMessage carryMessage) {
        ComMsg.ComReq req = carryMessage.parseFrom(ComMsg.ComReq.class);

//        int guchou = req.getParams(0);
        if(table.getGameModel().getGuchou()!=1){
        	return;
        }
        if(player.getGuChou()>0){
        	return;
        }
        LogUtil.msgLog.info("xpPhz_guchou|" + table.getId() + "|" + table.getPlayBureau() + "|" + player.getUserId() + "|" + player.getSeat() + "|guchou|" + 1);

        player.setGuChou(1);

        ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_GU_CHOU, player.getSeat());
		player.writeSocket(com.build());

//        table.broadcast(WebSocketMsgType.res_code_GU_CHOU, player.getUserId() + "", 1 + "");
    }
}
