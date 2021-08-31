package com.sy599.game.db.bean.competition.param;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetitionClearingModelRes {
	private boolean firstMatch;	//首次匹配

	private Long playingId;

	private Long roomConfigId;

	private Integer result;    //1等待其他玩家完成比赛, 2大结算

	private Integer curStep;	//赛制 1:打立出局,2:淘汰赛

	private Integer curRound;	//回合

	private String playingTitle;	//标题

	private Integer baseScore;    //当前基础底分

	private Integer weedOutScore;    //淘汰分数

	private Integer curStepTotalHuman;    //当前回合剩余人数

	private Integer totalHuman;    //当前参与总人数

	private long playingRoomId;    //房间ID

	private List<Round> stepUpgradeDetails;	//回合晋级

	//登录服回调
	private String loginCallBackUrl;

	//登陆服回调结算地址
	private String loginCallBackClearing;

	//登陆服回调退赛地址
	private String loginCallBackCancel;

	//登陆服小结更新排名
	private String loginCallBackOnlyRefreshRank;

	private boolean offlinePlayCancelSign;

	private List<CompetitionClearingPlay> plays;

	//同一个时间基点,当前的loginServer时间戳
	private long currentMills;

	//额外配置,每秒增加的配置  倍率_淘汰分 : 60,1,2_60,2,500
	private String secondWeedOut;

	//比赛开始时间
	private long playingOpenTime;

	//切割用户顺序
	private boolean noSplitUserOrder;

	//不创建房间
	private boolean noCreateTable;

	public CompetitionClearingModelRes copyBasicOption(){
		return CompetitionClearingModelRes.builder()
				.playingId(playingId)
				.roomConfigId(roomConfigId)
				.result(result)
				.curStep(curStep)
				.curRound(curRound)
				.baseScore(baseScore)
				.weedOutScore(weedOutScore)
				.curStepTotalHuman(curStepTotalHuman)
//				.loginCallBackUrl(loginCallBackUrl)
//				.loginCallBackClearing(loginCallBackClearing)
//				.loginCallBackCancel(loginCallBackCancel)
				.currentMills(currentMills)
				.secondWeedOut(secondWeedOut)
				.playingOpenTime(playingOpenTime)
				.totalHuman(totalHuman)
				.build();
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
		private boolean isNextStep;	//晋级到下一个轮次
	}
}