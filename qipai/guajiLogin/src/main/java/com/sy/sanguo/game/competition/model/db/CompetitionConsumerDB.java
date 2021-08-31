package com.sy.sanguo.game.competition.model.db;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 赛事报名消耗
 * @author Guang.OuYang
 * @date 2020/5/20-11:27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionConsumerDB extends CompetitionBaseDBPojo {
	public static final String SPACE_NAME = SPACE_NAME_1 + ".consumer";

	private long id;

	private Long playingId;

	private Long userId;

	//消耗ID
	private Integer consumerId;
	//具体消耗
	private Integer consumerVal;

	private Date createTime;

	private Date updateTime;

	private Date deleteTime;
}
