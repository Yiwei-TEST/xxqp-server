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
public class CompetitionApplyDB extends CompetitionBaseDBPojo {
	public static final String SPACE_NAME = SPACE_NAME_1 + ".apply";

	private long id;

	private Long userId;

	//当前积分
	private Integer consumerId;

	//具体消耗
	private Integer consumerVal;

	//分享免费报名
	private Boolean shareFreeSign;

	//赛场ID
	private Long playingId;

	//当前状态 1正常参赛,2退赛中,3退赛退费
	private Integer status;

	//1在赛场中
	private Integer play;

	//在报名界面
	private Integer signShow;

	//开启1赛前推送
	private Boolean push;

	private Date createTime;

	private Date updateTime;

	private Date deleteTime;


	//---------------------------------------
	private Long playingConfigId;
}
