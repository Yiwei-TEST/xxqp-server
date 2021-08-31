package com.sy599.game.qipai.smpaohuzi.command.action.play;

import com.sy599.game.msg.serverPacket.PlayCardReqMsg;
import com.sy599.game.qipai.smpaohuzi.command.AbsCodeCommandExecutor;
import com.sy599.game.qipai.smpaohuzi.bean.SmPaohuziPlayer;
import com.sy599.game.qipai.smpaohuzi.bean.SmPaohuziTable;

import java.util.*;

/**
 * 出牌操作, 与服务器指令做比较
 *
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class DisCardAction extends AbsCodeCommandExecutor<SmPaohuziTable, SmPaohuziPlayer> {
    @Override
    public Integer actionCode() {
        return -1;  //缺省操作
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return GlobalCommonIndex.PLAY_INDEX;
    }

    @Override
    public void execute(SmPaohuziTable table, SmPaohuziPlayer player, CarryMessage carryMessage) {
        PlayCardReqMsg.PlayCardReq playCard = carryMessage.parseFrom(PlayCardReqMsg.PlayCardReq.class);
        //指令
        table.play(player, new ArrayList<>(playCard.getCardIdsList()), playCard.getCardType());
    }

}
