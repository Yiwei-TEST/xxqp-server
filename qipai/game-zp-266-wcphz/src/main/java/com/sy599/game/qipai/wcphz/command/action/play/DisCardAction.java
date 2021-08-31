package com.sy599.game.qipai.wcphz.command.action.play;

import java.util.ArrayList;

import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg;
import com.sy599.game.qipai.wcphz.bean.WcPaohuziPlayer;
import com.sy599.game.qipai.wcphz.bean.WcPaohuziTable;
import com.sy599.game.qipai.wcphz.command.AbsCodeCommandExecutor;

/**
 * 出牌操作, 与服务器指令做比较
 *
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class DisCardAction extends AbsCodeCommandExecutor<WcPaohuziTable, WcPaohuziPlayer> {
    @Override
    public Integer actionCode() {
        return -1;  //缺省操作
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return GlobalCommonIndex.PLAY_INDEX;
    }

    @Override
    public void execute(WcPaohuziTable table, WcPaohuziPlayer player, CarryMessage carryMessage) {
		WcPaohuziTable table1 = player.getPlayingTable(WcPaohuziTable.class);
		if (table1 == null || table1.getState() != table_state.play) return;

        PlayCardReqMsg.PlayCardReq playCard = carryMessage.parseFrom(PlayCardReqMsg.PlayCardReq.class);
        //指令
        table.play(player, new ArrayList<>(playCard.getCardIdsList()), playCard.getCardType());
    }

}
