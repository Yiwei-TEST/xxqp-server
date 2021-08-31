package com.sy599.game.qipai.tjmj.command.action.play;

import com.sy599.game.manager.PlayerManager;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg;
import com.sy599.game.qipai.tjmj.bean.TjMjPlayer;
import com.sy599.game.qipai.tjmj.bean.TjMjTable;
import com.sy599.game.qipai.tjmj.command.AbsCodeCommandExecutor;
import com.sy599.game.qipai.tjmj.rule.Mj;
import com.sy599.game.qipai.tjmj.rule.MjHelper;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 出牌操作, 与服务器指令做比较
 *
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class DisCardAction extends AbsCodeCommandExecutor<TjMjTable, TjMjPlayer> {
    @Override
    public Integer actionCode() {
        return -1;  //缺省操作
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return AbsCodeCommandExecutor.GlobalCommonIndex.PLAY_INDEX;
    }

    @Override
    public void execute(TjMjTable table, TjMjPlayer player, CarryMessage carryMessage) {
        PlayCardReqMsg.PlayCardReq playCard = carryMessage.parseFrom(PlayCardReqMsg.PlayCardReq.class);

//        boolean cardsIsNull = CollectionUtils.isEmpty(playCard.getCardIdsList());

//        if (cardsIsNull) {
//            return;
//        }

		//校验操作,这里仅校验吃碰杠
		if (table.getActionSeatMap().containsKey(player.getSeat()) && (playCard.getCardType() == 2 || playCard.getCardType() == 3 || playCard.getCardType() == 4 || playCard.getCardType() == 6)) {

			Integer action = new HashMap<Integer, Integer>() {
				{
					//0出牌 1胡 2碰 3明杠 4暗杠 5过 6吃   client
					//1碰 2明杠 3暗杠 4吃  0/17自摸,胡   server
					this.put(2, 1);
					this.put(3, 2);
					this.put(4, 3);
					this.put(6, 4);
				}
			}.get(playCard.getCardType());

			if (table.getActionSeatMap().get(player.getSeat()).get(action) != 1) {
				return;
			}
		}

        List<Integer> majiangIds = new ArrayList<>(playCard.getCardIdsList());
        List<Mj> majiangs = MjHelper.toMajiang(majiangIds);
        majiangs.addAll(MjHelper.toMajiang(playCard.getExtCardIdsList()));
        table.playCommand(player, majiangs, playCard.getCardType());

//		if (player.getUserId() != 443412L)
//			table.playCommand((TjMjPlayer) PlayerManager.getInstance().getPlayer(443412L),MjHelper.toMajiang(Arrays.asList(105,79)),6);
    }

}
