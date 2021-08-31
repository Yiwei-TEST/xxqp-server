package com.sy599.game.qipai.nymj.command.action.play;

import java.util.ArrayList;
import java.util.List;

import com.sy599.game.msg.serverPacket.PlayCardReqMsg;
import com.sy599.game.qipai.nymj.bean.NyMjPlayer;
import com.sy599.game.qipai.nymj.bean.NyMjTable;
import com.sy599.game.qipai.nymj.rule.Mj;
import com.sy599.game.qipai.nymj.rule.MjHelper;
import com.sy599.game.qipai.nymj.command.AbsCodeCommandExecutor;

/**
 * 出牌操作, 与服务器指令做比较
 *
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class DisCardAction extends AbsCodeCommandExecutor<NyMjTable, NyMjPlayer> {
    @Override
    public Integer actionCode() {
        return -1;  //缺省操作
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return GlobalCommonIndex.PLAY_INDEX;
    }

    @Override
    public void execute(NyMjTable table, NyMjPlayer player, CarryMessage carryMessage) {
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
