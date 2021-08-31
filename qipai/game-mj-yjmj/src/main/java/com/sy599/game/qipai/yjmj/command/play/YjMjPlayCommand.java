package com.sy599.game.qipai.yjmj.command.play;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.qipai.yjmj.bean.YjMjDisAction;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg.PlayCardReq;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.yjmj.bean.YjMjPlayer;
import com.sy599.game.qipai.yjmj.bean.YjMjTable;
import com.sy599.game.qipai.yjmj.rule.MajiangHelper;
import com.sy599.game.qipai.yjmj.rule.YjMj;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class YjMjPlayCommand extends BaseCommand {

    @Override
    public void setMsgTypeMap() {
    }

    @Override
    public void execute(Player player0, MessageUnit message) throws Exception {
        YjMjTable table = player0.getPlayingTable(YjMjTable.class);
        if (table == null || !(player0 instanceof YjMjPlayer)) {
            return;
        }
        YjMjPlayer player = (YjMjPlayer) player0;
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
        int action = playCard.getCardType();
        List<Integer> majiangIds = playCard.getCardIdsList();

        if (action == YjMjDisAction.action_pass || action == YjMjDisAction.action_haodilaoPass) {
            Map<Integer, List<Integer>> actionMap = table.getActionSeatMap();
            if (actionMap != null && actionMap.containsKey(player.getSeat())) {
                List<Integer> list = actionMap.get(player.getSeat());
                // 检查过
                if (!majiangIds.isEmpty()) {
                    // 是否已经摸牌
                    majiangIds = new ArrayList<>(majiangIds);
                    int playIsMo = majiangIds.remove((int) 0);
                    int isAlreadyMo = player.isAlreadyMoMajiang() ? 1 : 0;
                    if (playIsMo != isAlreadyMo) {
                        return;
                    }
                    // 比较动作
                    if (!DataMapUtil.compareVal(list, majiangIds)) {
                        return;
                    }
                    majiangIds.clear();
                }
//                if (list != null && !list.isEmpty() && list.get(0) == 1) {
//                    LogUtil.monitor_i("------pass日志过掉可以胡的牌麻将:tableId:" + table.getId() + " playType:" + table.getPlayType() + " playBureau:" + table.getPlayBureau() + " userId:" + player.getUserId()
//                            + " cards:" + player.getHandPais() + " action:" + list);
//                }
            }
        }
        if (majiangIds != null && majiangIds.contains(0)) {
            LogUtil.monitor_i("麻将出牌-->" + table.getId() + " playType:" + table.getPlayType() + " playBureau:" + table.getPlayBureau() + " userId:" + player.getUserId() + " cards:" + majiangIds + " action:" + action);
        }
        List<YjMj> majiangs = MajiangHelper.toMajiang(majiangIds);
        table.playCommand(player, majiangs, action);
        if(action != YjMjDisAction.action_haodilaoPass) {
            player.setAutoPlay(false, false);
            player.setCheckAutoPlay(false);
        }
    }

}
