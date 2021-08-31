package com.sy599.game.qipai.yiyangwhz.command.play;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.yiyangwhz.bean.YyWhzCheckCardBean;
import com.sy599.game.qipai.yiyangwhz.bean.YyWhzDisAction;
import com.sy599.game.qipai.yiyangwhz.bean.YyWhzPlayer;
import com.sy599.game.qipai.yiyangwhz.bean.YyWhzTable;
import com.sy599.game.qipai.yiyangwhz.constant.YyWhzCard;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg.PlayCardReq;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.LangHelp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class YyWhzPlayCommand extends BaseCommand {
    @Override
    public void execute(Player player0, MessageUnit message) throws Exception {
        YyWhzPlayer player = (YyWhzPlayer) player0;
        YyWhzTable table = player.getPlayingTable(YyWhzTable.class);
        if (table == null)
            return;
        int canPlay = table.isCanPlay();
        if (canPlay != 0) {// 该牌局是否能打牌
            if (canPlay == 1) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_6));
                return;
            }
        }
        if (table.getState() != table_state.play) {// 检查桌子的状态
            return;
        }
        PlayCardReq playCard = (PlayCardReq) recognize(PlayCardReq.class, message);
        int action = playCard.getCardType();
        List<Integer> cards = playCard.getCardIdsList();// 牌参数
        if (action == YyWhzDisAction.action_pass) {// 客户端过的操作
            List<Integer> passCards = new ArrayList<>();
            if (!cards.isEmpty()) {// 摸的牌
                cards = new ArrayList<>(cards);
            }
            // 获得所有的操作集合
            Map<Integer, List<Integer>> actionMap = table.getActionSeatMap();
            if (actionMap != null && actionMap.containsKey(player.getSeat())) {
                List<Integer> list = actionMap.get(player.getSeat());
                if (!cards.isEmpty()) {
                    // 得到第一优先顺序的操作
                    int nowId = cards.remove(0);
                    if (nowId > 0) {
                        passCards.add(nowId);
                    }
                    List<YyWhzCard> nowDisCards = table.getNowDisCardIds();
                    if (nowDisCards != null && !nowDisCards.isEmpty() && !YyWhzCheckCardBean.hasLiu(list) && !YyWhzCheckCardBean.hasHu(list)) {
                        if (nowDisCards.get(0).getId() > 0 && nowDisCards.get(0).getId() != nowId) {
                            return;
                        }
                    }
                    if (!DataMapUtil.compareVal(list, cards)) {// 比较动作
                        return;
                    }
                }
            }
            if (!cards.isEmpty()) {// 是否已经摸牌
                cards.clear();
            }
            player.setAutoPlay(false, table);
            player.setLastOperateTime(System.currentTimeMillis());
            table.play(player, passCards, action);
            return;
        }
        if (action != YyWhzDisAction.action_mo) {
            player.setAutoPlay(false, table);
            player.setLastOperateTime(System.currentTimeMillis());
        }
        table.play(player, cards, action);
    }

    @Override
    public void setMsgTypeMap() {
    }
}
