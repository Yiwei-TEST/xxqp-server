package com.sy.sanguo.game.competition.model.db;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 赛事申请流水表
 * @author Guang.OuYang
 * @date 2020/5/20-11:27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionClearingAwardDB extends CompetitionBaseDBPojo {
	public static final String SPACE_NAME = SPACE_NAME_1 + ".clearing_award";

	private long id;

	//赛场ID
	private Long playingId;
	//排名
	private Integer rank;
	//用户
	private Long userId;
	//奖励
	private Integer awardId;
	//奖励值
	private Integer awardVal;
	//当前状态 1到账
	private Integer status;
	//
	private Date createTime;

	private Date updateTime;

	private Date deleteTime;
}
