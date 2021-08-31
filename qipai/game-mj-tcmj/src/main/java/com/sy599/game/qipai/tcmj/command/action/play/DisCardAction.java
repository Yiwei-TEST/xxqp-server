package com.sy599.game.qipai.tcmj.command.action.play;

import com.sy599.game.msg.serverPacket.PlayCardReqMsg;
import com.sy599.game.qipai.tcmj.bean.TcMjPlayer;
import com.sy599.game.qipai.tcmj.bean.TcMjTable;
import com.sy599.game.qipai.tcmj.rule.Mj;
import com.sy599.game.qipai.tcmj.rule.MjHelper;
import com.sy599.game.qipai.tcmj.command.AbsCodeCommandExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * 出牌操作, 与服务器指令做比较
 *
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class DisCardAction extends AbsCodeCommandExecutor<TcMjTable, TcMjPlayer> {
    @Override
    public Integer actionCode() {
        return -1;  //缺省操作
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return AbsCodeCommandExecutor.GlobalCommonIndex.PLAY_INDEX;
    }

    @Override
    public void execute(TcMjTable table, TcMjPlayer player, CarryMessage carryMessage) {
        PlayCardReqMsg.PlayCardReq playCard = carryMessage.parseFrom(PlayCardReqMsg.PlayCardReq.class);

//        boolean cardsIsNull = CollectionUtils.isEmpty(playCard.getCardIdsList());

//        if (cardsIsNull) {
//            return;
//        }

        List<Integer> majiangIds = new ArrayList<>(playCard.getCardIdsList());
        List<Mj> majiangs = MjHelper.toMajiang(majiangIds);
        majiangs.addAll(MjHelper.toMajiang(playCard.getExtCardIdsList()));
        table.playCommand(player, majiangs, playCard.getCardType());
    }

}
