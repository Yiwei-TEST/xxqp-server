package com.sy599.game.qipai.yymj.command.play;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg.PlayCardReq;
import com.sy599.game.qipai.yymj.bean.YyMjPlayer;
import com.sy599.game.qipai.yymj.bean.YyMjTable;
import com.sy599.game.qipai.yymj.command.AbsCodeCommandExecutor;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class MjPlayCommand extends BaseCommand {


    @Override
    public void setMsgTypeMap() {

    }

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        YyMjTable table = player.getPlayingTable(YyMjTable.class);
        if (table == null || !(player instanceof YyMjPlayer)) {
            return;
        }
        YyMjPlayer csplayer = (YyMjPlayer) player;
        // 该牌局是否能打牌
        int canPlay = table.isCanPlay();
        if (canPlay != 0) {
            if (canPlay == 1) {
                csplayer.writeErrMsg(LangHelp.getMsg(LangMsg.code_6));
            } else if (canPlay == 2) {
                csplayer.writeErrMsg(LangHelp.getMsg(LangMsg.code_7));
            }
            return;
        }

        if (table.getState() != table_state.play) {
            return;
        }

        PlayCardReq playCard = (PlayCardReq) recognize(PlayCardReq.class, message);

        try {
            int action = playCard.getCardType();
            //默认缺省使用discard操作
            AbsCodeCommandExecutor.getGlobalActionCodeInstance(AbsCodeCommandExecutor.GlobalCommonIndex.PLAY_INDEX, action)
                    .orElse(AbsCodeCommandExecutor.getGlobalActionCodeInstance(AbsCodeCommandExecutor.GlobalCommonIndex.PLAY_INDEX, -1).get())
                    .execute0(player, message, this, playCard);
        } catch (Exception e) {
            LogUtil.e("CodeCommonErr: " + player.getUserId() + " " + AbsCodeCommandExecutor.GlobalCommonIndex.PLAY_INDEX + " " + (playCard != null ? playCard.getCardType() : "NULL") + " " + LogUtil.printlnLog(message.getMessage()), e);
            throw e;
        }

        YyMjPlayer player0 = (YyMjPlayer) player;
        player0.setAutoPlay(false, false);
        player0.setCheckAutoPlay(false);
    }

}
