package com.sy599.game.qipai.nymj.command.action.play;

import com.sy599.game.qipai.nymj.bean.MjDisAction;
import com.sy599.game.qipai.nymj.bean.NyMjPlayer;
import com.sy599.game.qipai.nymj.bean.NyMjTable;
import com.sy599.game.qipai.nymj.command.AbsCodeCommandExecutor;

/**
 * 过orPass, 与服务器指令做比较
 *
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class MinHuAction extends AbsCodeCommandExecutor<NyMjTable, NyMjPlayer> {
    @Override
    public Integer actionCode() {
        return MjDisAction.action_xiaohu;  //缺省操作
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return GlobalCommonIndex.PLAY_INDEX;
    }

    @Override
    public void execute(NyMjTable table, NyMjPlayer player, CarryMessage carryMessage) {
//        PlayCardReqMsg.PlayCardReq playCard = carryMessage.parseFrom(PlayCardReqMsg.PlayCardReq.class);
//
//        boolean cardsIsNull = CollectionUtils.isEmpty(playCard.getCardIdsList());
//
//        if (cardsIsNull) {
//            return;
//        }
//
//        List<Integer> majiangIds = new ArrayList<>(playCard.getCardIdsList());
//        int xiaoHuType = majiangIds.remove((int) 0);
//        table.huXiaoHu(player, new ArrayList<Mj>(player.showXiaoHuMajiangs(xiaoHuType, false)), xiaoHuType, actionCode());
    }

}
