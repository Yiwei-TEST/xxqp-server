package com.sy.sanguo.game.competition.model.param;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetitionClearingModelRes {
	private boolean firstMatch;	//首次匹配

	private long playingId;

	private long roomConfigId;

	private int result;    //1等待其他玩家完成比赛, 2大结算

	private int curStep;	//赛制 1:打立出局,2:淘汰赛

	private int curRound;	//回合

	private String playingTitle;	//标题

	private int baseScore;    //当前基础底分

	private int weedOutScore;    //淘汰分数

	private int curStepTotalHuman;    //当前回合剩余人数
	private int totalHuman;    //当前参与总人数

	private long playingRoomId;

	private List<Round> stepUpgradeDetails;	//回合晋级
	//登录服回调
	@JSONField(serialize = false)
	private String loginCallBackUrl;

	//登陆服回调结算地址
	@JSONField(serialize = false)
	private String loginCallBackClearing;

	//登陆服回调退赛地址
	@JSONField(serialize = false)
	private String loginCallBackCancel;

	//登陆服小结更新排名
	@JSONField(serialize = false)
	private String loginCallBackOnlyRefreshRank;

	private List<CompetitionClearingPlay> plays;

	//同一个时间基点,当前的loginServer时间戳
	private long currentMills;

	//额外配置,每秒增加的配置  倍率_淘汰分 : 60,1,2_60,2,500
	private String secondWeedOut;

	//比赛开始时间
	private long playingOpenTime;

	//不切割用户顺序
	private boolean noSplitUserOrder;

	//不创建房间
	private boolean noCreateTable;

	/**
	 *@description
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/6
	 */
	public CompetitionClearingModelRes copy(CompetitionClearingModelRes arg) {
		return CompetitionClearingModelRes.builder()
				.playingId(arg.getPlayingId())
				.roomConfigId(arg.getRoomConfigId())
				.result(arg.getResult())
				.curStep(arg.getCurStep())
				.curRound(arg.getCurRound())
				.playingTitle(arg.getPlayingTitle())
				.baseScore(arg.getBaseScore())
				.weedOutScore(arg.getWeedOutScore())
				.curStepTotalHuman(arg.getCurStepTotalHuman())
				.playingRoomId(arg.getPlayingRoomId())
				.stepUpgradeDetails(arg.getStepUpgradeDetails())
				.loginCallBackClearing(arg.getLoginCallBackClearing())
				.loginCallBackUrl(arg.getLoginCallBackUrl())
				.loginCallBackCancel(arg.getLoginCallBackCancel())
				.loginCallBackOnlyRefreshRank(arg.getLoginCallBackOnlyRefreshRank())
				.currentMills(arg.getCurrentMills())
				.secondWeedOut(arg.getSecondWeedOut())
				.totalHuman(arg.getTotalHuman())
				.playingOpenTime(arg.getPlayingOpenTime())
				.build();
	}

	/**
	 *@description
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/6
	 */
	public CompetitionClearingModelRes copy() {
		return copy(this);
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Round {
		//赛制: -1.n进m, 其他.每桌前n晋级
		private int stepCategory;
		//轮次
		private int step;
		//当前总人数
		private int total;
		//晋级人数
		private int upgrade;
	}
}