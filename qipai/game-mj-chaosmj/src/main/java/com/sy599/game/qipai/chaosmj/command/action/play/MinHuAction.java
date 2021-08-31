package com.sy599.game.qipai.chaosmj.command.action.play;

import com.sy599.game.qipai.chaosmj.bean.MjDisAction;
import com.sy599.game.qipai.chaosmj.bean.ChaosMjPlayer;
import com.sy599.game.qipai.chaosmj.bean.ChaosMjTable;
import com.sy599.game.qipai.chaosmj.command.AbsCodeCommandExecutor;

/**
 * 过orPass, 与服务器指令做比较
 *
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class MinHuAction extends AbsCodeCommandExecutor<ChaosMjTable, ChaosMjPlayer> {
    @Override
    public Integer actionCode() {
        return MjDisAction.action_xiaohu;  //缺省操作
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return GlobalCommonIndex.PLAY_INDEX;
    }

    @Override
    public void execute(ChaosMjTable table, ChaosMjPlayer player, CarryMessage carryMessage) {
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
