package com.sy599.game.qipai.ahpaohuzi.command.action;

import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.qipai.ahpaohuzi.command.AbsCodeCommandExecutor;
import com.sy599.game.qipai.ahpaohuzi.bean.AhPaohuziPlayer;
import com.sy599.game.qipai.ahpaohuzi.bean.AhPaohuziTable;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 放技能, 组合牌型
 *
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class SkillAction extends AbsCodeCommandExecutor<AhPaohuziTable, AhPaohuziPlayer> {

    @Override
    public Integer actionCode() {
        return WebSocketMsgType.req_com_fangzhao;
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return GlobalCommonIndex.COMMAND_INDEX;
    }

    @Override
    public void execute(AhPaohuziTable table, AhPaohuziPlayer player, CarryMessage carryMessage) {
        ComMsg.ComReq req = carryMessage.parseFrom(ComMsg.ComReq.class);

        int pai = req.getParams(0);

        LogUtil.msgLog.info("XtPhz_fangzhao|" + table.getId() + "|" + table.getPlayBureau() + "|" + player.getUserId() + "|" + player.getSeat() + "|fangZhao|2|" + pai);

        player.setFangZhao(1);

        table.play(player, new ArrayList<>(Arrays.asList(pai)), 0);

        table.broadcast(WebSocketMsgType.res_code_phz_fangzhao, player.getUserId() + "", 1 + "");
    }
}
