package com.sy599.game.gcommand.com;

import com.sy599.game.activity.ActivityConstant;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.dao.LogDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.db.enums.UserMessageEnum;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.manager.MarqueeManager;
import com.sy599.game.message.MessageUtil;
import com.sy599.game.msg.AwardMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.staticdata.StaticDataManager;
import com.sy599.game.staticdata.model.Activity;
import com.sy599.game.staticdata.model.Award;
import com.sy599.game.staticdata.model.DrawLottery;
import com.sy599.game.util.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class DrawLotteryCommand extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		long usedCards = -player.getUsedCards();
		// long usedCards = 1000;
		// 15次房卡消耗换一个抽奖
		int drawCount = player.getCanDrawCount();

		ComReq req = (ComReq) this.recognize(ComReq.class, message);

		int command = req.getParams(0);
		if (command == 1) {
			// 进入
			Activity activity = StaticDataManager.activityMap.get(ActivityConstant.activity_logindays);
			String activityMsg = JacksonUtil.writeValueAsString(player.getMyActivity().buildMessage(activity, player.getLoginDays()));
			player.writeComMessage(WebSocketMsgType.res_code_drawlotteryenter, usedCards + "", drawCount, player.getLoginDays(), activityMsg, StaticDataManager.drawType,
					StaticDataManager.drawIdNameListStr);

		} else if (command == 2) {
			// 抽奖
			if (drawCount <= 0) {
				return;
			}
			int draw = MathUtil.draw(StaticDataManager.drawLottery);
			DrawLottery drawLottery = StaticDataManager.drawLotteryMap.get(draw);
			if (drawLottery == null) {
				return;
			}
			int itemNum = 0;
			switch (drawLottery.getItemId()) {
			case -1:
				itemNum = drawLottery.getItemNum();
				if (TimeUtil.currentTimeMillis() < 1480867203000l) {
					itemNum = itemNum * 2;
				}
				player.changeCards(itemNum, 0, true, CardSourceType.activity_drawLottery);
				MessageUtil.sendMessage(UserMessageEnum.TYPE1,player, "您在抽奖中获得:房卡x" + itemNum,null);
				break;

			default:
				MessageUtil.sendMessage(UserMessageEnum.TYPE1,player, "您在抽奖中获得:" + drawLottery.getName(),null);
				break;
			}
			// if (drawLottery.getItemId() != 0) {
			LogDao.getInstance().insetDrawLog(player.getUserId(), TimeUtil.now(), drawLottery.getItemId(), drawLottery.getName(), itemNum);
			// }

			if (drawLottery.getItemId() > 0) {
				MarqueeManager.getInstance().sendMarquee(LangHelp.getMsg(LangMsg.code_30, player.getName(), drawLottery.getName()), 1);
			}
			player.changeDrawLottery(1);
			player.saveBaseInfo();
			player.writeComMessage(WebSocketMsgType.res_code_drawlottery, drawLottery.getId(), drawCount - 1);

		} else if (command == 3) {
			// 领取
			int index = req.getParams(1);
			boolean canGet = player.getMyActivity().isCanGetAward(ActivityConstant.activity_logindays, index, player.getLoginDays());

			if (canGet) {
				Activity activity = StaticDataManager.activityMap.get(ActivityConstant.activity_logindays);
				Award award = player.getMyActivity().getAward(activity, index);
				AwardMsg msg = AwardUtil.changeAward(player, award);
				player.getMyActivity().updateAwardState(activity, index, TimeUtil.currentTimeMillis());

				String activityMsg = JacksonUtil.writeValueAsString(player.getMyActivity().buildMessage(activity, player.getLoginDays()));
				player.writeComMessage(WebSocketMsgType.res_code_getActivityAward, ActivityConstant.activity_logindays, activityMsg);
				MessageUtil.sendMessage(UserMessageEnum.TYPE1,player, "您领取了:房卡x" + msg.getCards(),null);
				LogDao.getInstance().insetDrawLog(player.getUserId(), TimeUtil.now(), 100, "您领取了:房卡x" + msg.getCards(), msg.getCards());
			}

		}

	}

	@Override
	public void setMsgTypeMap() {

	}

}
