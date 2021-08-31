package com.sy599.game.qipai.xplp.command.play;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg.PlayCardReq;
import com.sy599.game.qipai.xplp.bean.XplpDisAction;
import com.sy599.game.qipai.xplp.bean.XplpPlayer;
import com.sy599.game.qipai.xplp.bean.XplpTable;
import com.sy599.game.qipai.xplp.rule.XpLp;
import com.sy599.game.qipai.xplp.tool.XplpHelper;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class XplpPlayCommand extends BaseCommand {
    @Override
    public void execute(Player player0, MessageUnit message) throws Exception {
        XplpPlayer player = (XplpPlayer) player0;
        XplpTable table = player.getPlayingTable(XplpTable.class);
        if (table == null) {
            return;
        }

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

        if (action == XplpDisAction.action_pass) {
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
                        //player.writeErrMsg("过---手牌数量错误!!");
                        return;
                    }
                    // 比较动作
                    if (!DataMapUtil.compareVal(list, majiangIds)) {
                        //player.writeErrMsg("过---动作错误!!");
                        return;
                    }
                    majiangIds.clear();
                }
            }
        }
        List<XpLp> majiangs = XplpHelper.toMajiang(majiangIds);
        table.playCommand(player, majiangs, action);
        player.setAutoPlay(false, false);
        player.setCheckAutoPlay(false);
    }

    @Override
    public void setMsgTypeMap() {

    }

}
