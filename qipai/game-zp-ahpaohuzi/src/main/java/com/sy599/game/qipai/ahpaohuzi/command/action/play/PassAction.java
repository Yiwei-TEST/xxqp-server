package com.sy599.game.qipai.ahpaohuzi.command.action.play;

import com.sy599.game.msg.serverPacket.PlayCardReqMsg;
import com.sy599.game.qipai.ahpaohuzi.command.AbsCodeCommandExecutor;
import com.sy599.game.qipai.ahpaohuzi.constant.PaohzCard;
import com.sy599.game.qipai.ahpaohuzi.bean.PaohzDisAction;
import com.sy599.game.qipai.ahpaohuzi.bean.AhPaohuziPlayer;
import com.sy599.game.qipai.ahpaohuzi.bean.AhPaohuziTable;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.LogUtil;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 过orPass, 与服务器指令做比较
 *
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class PassAction extends AbsCodeCommandExecutor<AhPaohuziTable, AhPaohuziPlayer> {
    @Override
    public Integer actionCode() {
        return PaohzDisAction.action_pass;  //缺省操作
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return GlobalCommonIndex.PLAY_INDEX;
    }

    @Override
    public void execute(AhPaohuziTable table, AhPaohuziPlayer player, CarryMessage carryMessage) {
        PlayCardReqMsg.PlayCardReq playCard = carryMessage.parseFrom(PlayCardReqMsg.PlayCardReq.class);

        boolean cardNotNull = !CollectionUtils.isEmpty(playCard.getCardIdsList());

        // 检查过
        if (cardNotNull) {
            //摸的牌
            List<Integer> cards = new ArrayList<>(playCard.getCardIdsList());

            try {
                // 从所有的操作集合
                // 获取牌桌当前玩家座位的动作
                List<Integer> list;
                if (!CollectionUtils.isEmpty((list = Optional.ofNullable(table.getActionSeatMap()).orElse(Collections.emptyMap()).get(player.getSeat())))) {

                    // 得到第一优先顺序的操作
                    int nowId = cards.remove((int) 0);
                    //当前打出的牌
                    List<PaohzCard> nowDisCards = table.getNowDisCardIds();
                    if (!CollectionUtils.isEmpty(nowDisCards)) {
                        if (nowDisCards.get(0).getId() != nowId && nowDisCards.get(0).getId() > 0) {
                            return;
                        }
                    }

                    // 客户端指令动作与服务器动作比较
                    if (!DataMapUtil.compareVal(list, cards)) {
                        return;
                    }

                    if (!CollectionUtils.isEmpty(list) && list.get(0) == 1) {
                        LogUtil.monitor_i("------pass日志过掉可以胡的牌跑胡子:tableId:" + table.getId() + " playType:" + table.getPlayType() + " playBureau:" + table.getPlayBureau() + " userId:" + player.getUserId()
                                + " cards:" + player.getHandPais() + " action:" + list);

                    }
                }
            } finally {
                cards.clear();
            }

            //指令
            table.play(player, cards, playCard.getCardType());
        }
    }

}
