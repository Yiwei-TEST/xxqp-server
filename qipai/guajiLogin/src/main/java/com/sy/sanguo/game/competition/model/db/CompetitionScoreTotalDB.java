package com.sy.sanguo.game.competition.model.db;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 赛事积分汇总信息
 * @author Guang.OuYang
 * @date 2020/5/20-11:35
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionScoreTotalDB extends CompetitionBaseDBPojo {
	public static final String SPACE_NAME = SPACE_NAME_1 + ".score_total";
	@JSONField(serialize = false)
	private long id;

	private Integer rank;		//当前排名

	private Long userId;		//用户ID

	private String userName;	//玩家名字

	private Long playingId;		//赛事ID

	private Integer score;		//分数

	@JSONField(serialize = false)
	private Integer status;		//1正常,2淘汰

	//当前回合处于房间内
	@JSONField(serialize = false)
	private Boolean inRoom;

	@JSONField(serialize = false)
	private Date createTime;
	@JSONField(serialize = false)
	private Date updateTime;
	@JSONField(serialize = false)
	private Date deleteTime;

	//----------------------------------------------
	//总人数
	private Integer total;

	@JSONField(serialize = false)
	private int limit;

	//------------------------------
	private long randomVal;
}
