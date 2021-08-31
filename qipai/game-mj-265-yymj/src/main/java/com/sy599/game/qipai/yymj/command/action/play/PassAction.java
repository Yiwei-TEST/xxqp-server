package com.sy599.game.qipai.yymj.command.action.play;

import com.sy599.game.msg.serverPacket.PlayCardReqMsg;
import com.sy599.game.qipai.yymj.bean.MjDisAction;
import com.sy599.game.qipai.yymj.bean.YyMjPlayer;
import com.sy599.game.qipai.yymj.bean.YyMjTable;
import com.sy599.game.qipai.yymj.command.AbsCodeCommandExecutor;
import com.sy599.game.util.DataMapUtil;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 过orPass, 与服务器指令做比较
 *
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class PassAction extends AbsCodeCommandExecutor<YyMjTable, YyMjPlayer> {
    @Override
    public Integer actionCode() {
        return MjDisAction.action_pass;  //缺省操作
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return AbsCodeCommandExecutor.GlobalCommonIndex.PLAY_INDEX;
    }

    @Override
    public void execute(YyMjTable table, YyMjPlayer player, CarryMessage carryMessage) {
        PlayCardReqMsg.PlayCardReq playCard = carryMessage.parseFrom(PlayCardReqMsg.PlayCardReq.class);

        boolean cardsIsNull = CollectionUtils.isEmpty(playCard.getCardIdsList());

        if (cardsIsNull) {
            return;
        }

        List<Integer> majiangIds = new ArrayList<>(playCard.getCardIdsList());

        Map<Integer, List<Integer>> actionMap = table.getActionSeatMap();
        if (actionMap != null && actionMap.containsKey(player.getSeat())) {
            List<Integer> list = actionMap.get(player.getSeat());
            int playIsMo = majiangIds.remove((int) 0);
            int isAlreadyMo = player.noNeedMoCard() ? 1 : 0;
            if (playIsMo != isAlreadyMo) {
                return;
            }

            // 比较动作
            if (!DataMapUtil.compareVal(list, majiangIds)) {
                return;
            }

            majiangIds.clear();
        }
//
//        List<Mj> majiangs = MjHelper.toMajiang(majiangIds);
//        majiangs.addAll(MjHelper.toMajiang(playCard.getExtCardIdsList()));
//        table.playCommand(player, majiangs, playCard.getCardType());
        table.playCommand(player, new ArrayList<>(), playCard.getCardType());
    }

}
