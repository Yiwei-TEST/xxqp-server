package com.sy.sanguo.game.competition.model.db;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 赛事积分流水表, 每个赛事最后一步的排名为玩家最终排名
 * @author Guang.OuYang
 * @date 2020/5/20-11:27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionScoreDetailDB extends CompetitionBaseDBPojo {
	public static final String SPACE_NAME = SPACE_NAME_1 + ".score_detail";

	@JSONField(serialize = true)
	private long id;

	private Long userId;

	//玩家名字
	private String userName;

	//赛场ID
	@JSONField(serialize = false)
	private Long playingId;

	//当前积分
	@JSONField(name = "score")
	private Integer lastScore;

	//当前名次变化
	@JSONField(name = "rank")
	private Integer lastRank;

	//桌子内结算排名
	@JSONField(name = "tableRank", serialize = false)
	private Integer lastTableRank;

	//当前轮次
	@JSONField(serialize = false, name = "step")
	private Integer lastStep;

	//当前轮次
	@JSONField(serialize = false, name = "round")
	private Integer lastRound;

	//当前阶段状态, 1:晋级,2:淘汰
	@JSONField(serialize = false)
	private Integer lastStatus;

	//积分换算比例
	@JSONField(serialize = false)
	private Float scoreBasicRatio;

	//结算房号
	@JSONField(serialize = false)
	private Long roomId;

	//结算备注
	@JSONField(serialize = false)
	private String remark;

	//淘汰策略,前几名
	@JSONField(serialize = false)
	private Integer weedTopNumber;

	//淘汰策略,同桌前几名
	@JSONField(serialize = false)
	private Integer weedTopScore;

	@JSONField(serialize = false)
	private Date createTime;

	@JSONField(serialize = false)
	private Date updateTime;

	@JSONField(serialize = false)
	private Date deleteTime;

	@JSONField(serialize = false)
	private int limit;//仅作为查询


	//-------------------------------------------随机值
	private long randomVal;
}
