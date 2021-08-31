package com.sy599.game.util;

import java.util.List;

import com.sy599.game.db.bean.competition.CompetitionRoom;
import com.sy599.game.db.bean.competition.CompetitionRoomUser;

/**
 * 比赛场大结算后执行业务逻辑
 * @author Guang.OuYang
 * @date 2020/7/15-14:29
 */
public interface CompetitionBigStageTask {
	/**
	 *@description 业务模块
	 *@param winnerUserIds 最终赢家
	 *@param players 比赛场当前房间所有玩家,可能包含机器人
	 *@param room 比赛场房间
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/15
	 */
	public void invoke(List<Long> winnerUserIds, List<CompetitionRoomUser> players, CompetitionRoom room);
}
