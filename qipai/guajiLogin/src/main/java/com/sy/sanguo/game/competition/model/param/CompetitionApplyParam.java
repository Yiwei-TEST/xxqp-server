package com.sy.sanguo.game.competition.model.param;

import lombok.Builder;
import lombok.Data;

/**
 * @author Guang.OuYang
 * @date 2020/5/20-16:45
 */
@Data
@Builder
public class CompetitionApplyParam {
	private Long userId;	//用户ID

	private Integer titleType;//赛事类型

	private Integer playingType;// 比赛类型

	private Integer category;// 场次类型

	private Integer entrance;// 赛事入口,不同入口不同ID   type+category+entrance联合主键

	private Long playingId;

	private Integer shareFreeSign; //分享报名
}
