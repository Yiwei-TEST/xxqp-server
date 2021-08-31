package com.sy599.game.qipai.tjmj.command.action.play;

import com.sy599.game.msg.serverPacket.PlayCardReqMsg;
import com.sy599.game.qipai.tjmj.bean.MjDisAction;
import com.sy599.game.qipai.tjmj.bean.TjMjPlayer;
import com.sy599.game.qipai.tjmj.bean.TjMjTable;
import com.sy599.game.qipai.tjmj.command.AbsCodeCommandExecutor;
import com.sy599.game.qipai.tjmj.rule.Mj;
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
public class MinHuAction extends AbsCodeCommandExecutor<TjMjTable, TjMjPlayer> {
    @Override
    public Integer actionCode() {
        return MjDisAction.action_xiaohu;  //缺省操作
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return GlobalCommonIndex.PLAY_INDEX;
    }

    @Override
    public void execute(TjMjTable table, TjMjPlayer player, CarryMessage carryMessage) {
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
