package com.sy599.game.qipai.yzlc.command.action.play;

import com.sy599.game.msg.serverPacket.PlayCardReqMsg;
import com.sy599.game.qipai.yzlc.bean.PaohuziCheckCardBean;
import com.sy599.game.qipai.yzlc.command.AbsCodeCommandExecutor;
import com.sy599.game.qipai.yzlc.constant.PaohzCard;
import com.sy599.game.qipai.yzlc.bean.PaohzDisAction;
import com.sy599.game.qipai.yzlc.bean.YzLcPaohuziPlayer;
import com.sy599.game.qipai.yzlc.bean.YzLcPaohuziTable;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.LogUtil;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * 过orPass, 与服务器指令做比较
 *
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class PassAction extends AbsCodeCommandExecutor<YzLcPaohuziTable, YzLcPaohuziPlayer> {
    @Override
    public Integer actionCode() {
        return PaohzDisAction.action_pass;  //缺省操作
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return GlobalCommonIndex.PLAY_INDEX;
    }

    @Override
    public void execute(YzLcPaohuziTable table, YzLcPaohuziPlayer player, CarryMessage carryMessage) {
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
                    //当前打出的牌
                    List<PaohzCard> nowDisCards = table.getNowDisCardIds();
                    if (!CollectionUtils.isEmpty(nowDisCards)) {
						int size = Optional.of(nowDisCards).orElse(Collections.emptyList()).size();
						List<Integer> nowId = cards.subList(0, size);
						if (nowId.size() != nowDisCards.size() || IntStream.range(0, nowId.size()).anyMatch(i -> nowDisCards.get(i).getId() != nowId.get(i))) {
							StringBuilder sb = new StringBuilder("YzLc");
							sb.append("|").append(table.getId());
							sb.append("|").append(table.getPlayBureau());
							sb.append("|").append(player.getUserId());
							sb.append("|").append(player.getSeat());
							sb.append("|").append("PassLog");
							sb.append("|").append(nowId);
							sb.append("|").append(nowDisCards);
							LogUtil.msgLog.info(sb.toString());
                            return;
                        }
						cards = cards.subList(size, cards.size());
                    }

                    // 客户端指令动作与服务器动作比较
                    if (!DataMapUtil.compareVal(list, cards)) {
						StringBuilder sb = new StringBuilder("YzLc");
						sb.append("|").append(table.getId());
						sb.append("|").append(table.getPlayBureau());
						sb.append("|").append(player.getUserId());
						sb.append("|").append(player.getSeat());
						sb.append("|").append("PassLog");
						sb.append("|").append(nowDisCards);
						sb.append("|").append(cards);
						sb.append("|").append(list);
						LogUtil.msgLog.info(sb.toString());
                        return;
                    }

                    if (!CollectionUtils.isEmpty(list) && list.get(0) == 1) {
                        LogUtil.monitor_i("------pass日志过掉可以胡的牌跑胡子:tableId:" + table.getId() + " playType:" + table.getPlayType() + " playBureau:" + table.getPlayBureau() + " userId:" + player.getUserId()
                                + " cards:" + player.getHandPais() + " action:" + list);

                    }
                }

				//指令
				table.play(player, new ArrayList<>(), playCard.getCardType());
            } catch(Exception e){
				LogUtil.errorLog.error("YzLcError:{},e:{}",e.getMessage(),e);
				StringBuilder sb = new StringBuilder("YzLc");
				sb.append("|").append(table.getId());
				sb.append("|").append(table.getPlayBureau());
				sb.append("|").append(player.getUserId());
				sb.append("|").append(player.getSeat());
				sb.append("|").append("PassLogException");
				sb.append("|").append(playCard);
				sb.append("|").append(cards);
				sb.append("|").append(e.getMessage());
				LogUtil.msgLog.info(sb.toString());
			} finally {
                cards.clear();
            }
        }else{
			StringBuilder sb = new StringBuilder("YzLc");
			sb.append("|").append(table.getId());
			sb.append("|").append(table.getPlayBureau());
			sb.append("|").append(player.getUserId());
			sb.append("|").append(player.getSeat());
			sb.append("|").append("PassLog");
			sb.append("|").append(cardNotNull);
			LogUtil.msgLog.info(sb.toString());
		}
    }

}
