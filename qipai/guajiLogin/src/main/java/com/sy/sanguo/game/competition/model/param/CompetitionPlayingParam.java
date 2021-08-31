package com.sy.sanguo.game.competition.model.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Guang.OuYang
 * @date 2020/5/22-9:23
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionPlayingParam {
	private Long playingId;
	private Integer titleType;
	private Integer playingType;
	private Integer category;
	private Integer entrance;

	//报名时间
	private String applyBefore;
	private String applyAfter;

	//匹配时间
	private String matchBefore;
	private String matchAfter;

//	CompetitionPlayingStatusEnum
	private Integer status;

	private Long userId;
}
