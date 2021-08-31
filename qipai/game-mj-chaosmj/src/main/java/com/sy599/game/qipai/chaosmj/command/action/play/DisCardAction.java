package com.sy599.game.qipai.chaosmj.command.action.play;

import com.sy599.game.msg.serverPacket.PlayCardReqMsg;
import com.sy599.game.qipai.chaosmj.bean.ChaosMjPlayer;
import com.sy599.game.qipai.chaosmj.bean.ChaosMjTable;
import com.sy599.game.qipai.chaosmj.rule.Mj;
import com.sy599.game.qipai.chaosmj.rule.MjHelper;
import com.sy599.game.qipai.chaosmj.command.AbsCodeCommandExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * 出牌操作, 与服务器指令做比较
 *
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class DisCardAction extends AbsCodeCommandExecutor<ChaosMjTable, ChaosMjPlayer> {
    @Override
    public Integer actionCode() {
        return -1;  //缺省操作
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return AbsCodeCommandExecutor.GlobalCommonIndex.PLAY_INDEX;
    }

    @Override
    public void execute(ChaosMjTable table, ChaosMjPlayer player, CarryMessage carryMessage) {
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
