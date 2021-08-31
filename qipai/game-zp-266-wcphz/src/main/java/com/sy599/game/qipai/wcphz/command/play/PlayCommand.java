package com.sy599.game.qipai.wcphz.command.play;

import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg.PlayCardReq;
import com.sy599.game.qipai.wcphz.command.AbsCodeCommandExecutor;
import com.sy599.game.qipai.wcphz.bean.WcPaohuziPlayer;
import com.sy599.game.qipai.wcphz.bean.WcPaohuziTable;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;

/**
 * @author Guang.OuYang
 * @description 个性化消息
 * @return
 * @date 2019/9/2
 */
public class PlayCommand extends BaseCommand<WcPaohuziPlayer> {

    @Override
    public void execute(WcPaohuziPlayer player, MessageUnit message) throws Exception {
        WcPaohuziTable table = player.getPlayingTable(WcPaohuziTable.class);
        if (table == null) return;

        // 该牌局是否能打牌

        //
        if (table.isCanPlay() == 1) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_6));
            return;
        }

        // 检查桌子的状态
        if (table.getState() != table_state.play || player.getSeat() == table.getShuXingSeat()) {
            return;
        }

        PlayCardReq playCard = (PlayCardReq) recognize(PlayCardReq.class, message);

		int action = playCard.getCardType();
        try {
            //重置托管时间
            if(action != 9){
                player.setAutoPlay(false,table);
                player.setLastOperateTime(System.currentTimeMillis());
            }
            //默认缺省使用discard操作
            AbsCodeCommandExecutor.getGlobalActionCodeInstance(AbsCodeCommandExecutor.GlobalCommonIndex.PLAY_INDEX, action)
                    .orElse(AbsCodeCommandExecutor.getGlobalActionCodeInstance(AbsCodeCommandExecutor.GlobalCommonIndex.PLAY_INDEX, -1).get())
                    .execute0(player, message, this, playCard);
        } catch (Exception e) {
			LogUtil.e("CodeCommonErr: " + player.getUserId() + " " + AbsCodeCommandExecutor.GlobalCommonIndex.PLAY_INDEX + " action: " + action + " " + (playCard != null ? playCard.getCardType() : "NULL") + " " + LogUtil.printlnLog(message.getMessage()), e);
            throw e;
        }
    }

    @Override
    public void setMsgTypeMap() {

    }

}
