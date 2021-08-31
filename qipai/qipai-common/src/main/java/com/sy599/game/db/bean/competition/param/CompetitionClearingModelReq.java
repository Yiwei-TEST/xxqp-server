package com.sy599.game.db.bean.competition.param;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 *@description 清算模型
 *@param
 *@return
 *@author Guang.OuYang
 *@date 2020/6/4
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetitionClearingModelReq {
	//当前轮次
	private int curStep;

	//当前回合
	private int curRound;

	//赛事ID
	private long playingId;

	//结算房号
	private long competitionRoomKeyId;

	//现有房间数
	private long curTableCount;

	//同一个时间基点,当前的loginServer时间戳
	private long currentMills;

	//同一个时间基点,当前的loginServer时间戳
	private long currentClearingMills;

	//同一桌玩家
	private List<CompetitionClearingPlay> users;
}
