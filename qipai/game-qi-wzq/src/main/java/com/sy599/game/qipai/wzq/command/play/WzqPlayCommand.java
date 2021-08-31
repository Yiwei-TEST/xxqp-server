package com.sy599.game.qipai.wzq.command.play;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg.PlayCardReq;
import com.sy599.game.qipai.wzq.bean.WzqPlayer;
import com.sy599.game.qipai.wzq.bean.WzqTable;
import com.sy599.game.util.LangHelp;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.List;

public class WzqPlayCommand extends BaseCommand {
    @Override
    public void execute(Player player0, MessageUnit message) throws Exception {
        WzqPlayer player = (WzqPlayer) player0;
        WzqTable table = player.getPlayingTable(WzqTable.class);
        if (table == null) {
            return;
        }

        // 该牌局是否能打牌
        int canPlay = table.isCanPlay();
        if (canPlay != 0) {
            if (canPlay == 1) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_6));
            } else if (canPlay == 2) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_7));
            }
            return;
        }

        if (table.getState() != table_state.play) {
            return;
        }
        PlayCardReq playCard = (PlayCardReq) recognize(PlayCardReq.class, message);
        List<Integer> xy = playCard.getCardIdsList();
        table.play(player, xy);
    }

    @Override
    public void setMsgTypeMap() {

    }

}
