package com.sy.sanguo.game.competition.model.param;

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


	public CompetitionClearingPlay copy() {
		return copy(this);
	}
	public CompetitionClearingPlay copy(CompetitionClearingPlay arg) {
		return CompetitionClearingPlay.builder()
				.userId(arg.getUserId())
				.score(arg.getScore())
				.rank(arg.getRank())
				.tableRank(arg.getTableRank())
				.isOver(arg.getIsOver())
				.awardId(arg.getAwardId())
				.awardVal(arg.getAwardVal())
				.nextStep(arg.isNextStep())
				.build();
	}

}