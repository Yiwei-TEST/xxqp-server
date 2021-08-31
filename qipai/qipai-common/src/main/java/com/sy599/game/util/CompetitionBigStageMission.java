package com.sy599.game.util;

import java.util.List;

import com.sy599.game.character.Player;
import com.sy599.game.db.bean.competition.CompetitionRoom;
import com.sy599.game.db.bean.competition.CompetitionRoomUser;
import com.sy599.game.manager.PlayerManager;
import lombok.Builder;

/**
 * 比赛场大结算任务处理模块
 * @author Guang.OuYang
 * @date 2020/7/15-14:30
 */
@Builder
public class CompetitionBigStageMission implements CompetitionBigStageTask {

	@Override
	public void invoke(List<Long> winnerUserIds, List<CompetitionRoomUser> players, CompetitionRoom room) {
		for (CompetitionRoomUser roomUser:players) {
			Player player = PlayerManager.getInstance().getPlayer(roomUser.getUserId());
			if(player!=null)
				player.getMission().addChallengeRound();
			else
				LogUtil.msgLog.info("addChallengeRound|"+roomUser.getUserId()+"|user missing");
		}
	}

}
