package com.sy599.game.db.bean.competition.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetitionClearingPlay {
	private long userId;    //用户ID

	private int score;        //积分

	private int rank;        //当前排名

	private int tableRank;    //牌桌内排名

	private int isOver;        //结束当前回合了1晋级,2淘汰

	private int awardId;    	//有无奖励

	private int awardVal;    	//

	private boolean nextStep;	//晋级到下一个轮次
}