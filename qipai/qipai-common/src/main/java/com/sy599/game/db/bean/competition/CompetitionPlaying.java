package com.sy599.game.db.bean.competition;


import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 赛事
 * @author Guang.OuYang
 * @date 2020/5/20-11:27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionPlaying  {

	@JSONField(name = "playingId")
	private long id;// 比赛场

	private Long bindServerId;//自动激活绑定的ID,要求同一个赛事的所有配置处于一个服

	private Integer titleType;	//标题分类, 1话费赛, 2赢豆赛

	private Long playingConfigId;	//比赛配置id外键

	private Integer type;// 比赛类型

	private Integer category;// 场次类型

	private Integer entrance;// 赛事入口,不同入口不同ID   type+category+entrance联合主键

	private Integer consumerId;// 消耗类型

	private Integer consumerVal;// 消耗具体

	private Boolean shareFreeSign;// 分享免费报名

	private Integer initScore;// 初始积分

	private Integer curHuman;// 报名参赛人数

	private Integer beginHuman;// 开赛人数阈值

	private Integer maxHuman;// 最大人数阈值

	private Integer endHuman;// 结算人数阈值

	private String stepOutDesc;// 单局淘汰策略 轮次:每桌晋级人数

	private String titleCode;// 赛事名称 (国际化)

	private String desc;// 比赛描述

	private Integer status;// 状态1报名中,2比赛中,3结算中,4结算完毕

	private Integer beginPlayingPushStatus;	//赛前推送

	private Integer applyBeforeMin ;	//赛前n分钟可以开放报名

	private Date applyBefore;// 报名开始时间

	private Date applyAfter;//   报名结束时间

	private Date matchBefore;// 赛事开启时间段开始

	private Date matchAfter;//   赛事开启时间段结束

	@JSONField(serialize = false)
	private Integer curStep;// 当前进行到轮次

	@JSONField(serialize = false)
	private Integer curRound;// 当前进行到回合数

	private Boolean iteration;// 迭代0关闭,最后一场比赛后不再生成新的比赛场次

	@JSONField(serialize = false)
	private String shardingTakeOver;	//分片接管, 当前执行该任务的组

	private Integer curPlayingTableCount;	//当前进行中的牌桌

	private String logo;	//今天logo

	private String disableStartTime;// 禁赛时间

	private String disableEndTime;// 禁赛时间

	private Date openTime;//  比赛实际开始时间

	@JSONField(serialize = false)
	private String ext;	//额外字段

	private Date createTime;//

	private Date updateTime;//

	private Date deleteTime;//

	@JSONField(serialize = false)
	private Integer orderField;		//排序


	//------------------临时变量
	//开放报名
	private boolean openSign;
	@JSONField(serialize = false)
	private Long userId;
	//报名状态 1已经报名,
	private boolean signStatus;
	//结算奖励
	private String awards;	//结算奖励  rank,awardId,awardVal;
	//分配的房间号
	private long playingRoomId;
	//房间配置ID
	private long roomConfigId;
	//迭代类型, 1分钟赛,2日赛,3周赛
	private Integer iterationType;
	//迭代分钟
	private Integer iterationMin;
}
