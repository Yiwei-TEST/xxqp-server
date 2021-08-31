package com.sy599.game.qipai.yzwdmj.command.play;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg.PlayCardReq;
import com.sy599.game.qipai.yzwdmj.bean.YzwdmjDisAction;
import com.sy599.game.qipai.yzwdmj.bean.YzwdmjPlayer;
import com.sy599.game.qipai.yzwdmj.bean.YzwdmjTable;
import com.sy599.game.qipai.yzwdmj.rule.Yzwdmj;
import com.sy599.game.qipai.yzwdmj.tool.YzwdmjHelper;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class YzwdmjPlayCommand extends BaseCommand {
    @Override
    public void execute(Player player0, MessageUnit message) throws Exception {
        YzwdmjPlayer player = (YzwdmjPlayer) player0;
        YzwdmjTable table = player.getPlayingTable(YzwdmjTable.class);
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

        if (action == YzwdmjDisAction.action_pass) {
            Map<Integer, List<Integer>> actionMap = table.getActionSeatMap();
            if ((actionMap != null && actionMap.containsKey(player.getSeat()))||player.getWangAct().contains(1)) {
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
                    if(!player.getWangAct().contains(1)){//非王钓王闯的情况下
                        // 比较动作
                        if (!DataMapUtil.compareVal(list, majiangIds)) {
                            //player.writeErrMsg("过---动作错误!!");
                            return;
                        }
                    }
                    majiangIds.clear();
                }
            }
        }
        List<Yzwdmj> majiangs = YzwdmjHelper.toMajiang(majiangIds);
        table.playCommand(player, majiangs, action);
        player.setAutoPlay(false, false);
        player.setCheckAutoPlay(false);
    }

    @Override
    public void setMsgTypeMap() {

    }

}
