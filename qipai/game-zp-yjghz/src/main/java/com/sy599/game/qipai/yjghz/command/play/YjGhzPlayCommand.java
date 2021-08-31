package com.sy599.game.qipai.yjghz.command.play;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.yjghz.bean.YjGhzCheckCardBean;
import com.sy599.game.qipai.yjghz.bean.YjGhzDisAction;
import com.sy599.game.qipai.yjghz.bean.YjGhzPlayer;
import com.sy599.game.qipai.yjghz.bean.YjGhzTable;
import com.sy599.game.qipai.yjghz.constant.YjGhzCard;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg.PlayCardReq;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.LangHelp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class YjGhzPlayCommand extends BaseCommand {
    @Override
    public void execute(Player player0, MessageUnit message) throws Exception {
        YjGhzPlayer player = (YjGhzPlayer) player0;
        YjGhzTable table = player.getPlayingTable(YjGhzTable.class);
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
        if (action == YjGhzDisAction.action_pass) {// 客户端过的操作
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
                    List<YjGhzCard> nowDisCards = table.getNowDisCardIds();
                    if (nowDisCards != null && !nowDisCards.isEmpty() && !YjGhzCheckCardBean.hasLiu(list) && !YjGhzCheckCardBean.hasHu(list)) {
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
        if (action != YjGhzDisAction.action_mo) {
            player.setAutoPlay(false, table);
            player.setLastOperateTime(System.currentTimeMillis());
        }
        table.play(player, cards, action);
    }

    @Override
    public void setMsgTypeMap() {
    }
}
