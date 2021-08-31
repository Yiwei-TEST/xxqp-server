package com.sy.sanguo.game.competition.service.clearing;

import com.sy.sanguo.game.competition.model.db.CompetitionPlayingConfigDB;
import com.sy.sanguo.game.competition.model.db.CompetitionPlayingDB;
import com.sy.sanguo.game.competition.model.db.CompetitionScoreDetailDB;
import com.sy.sanguo.game.competition.model.db.CompetitionScoreTotalDB;
import com.sy.sanguo.game.competition.model.enums.CompetitionPlayingStatusEnum;
import com.sy.sanguo.game.competition.model.enums.CompetitionPlayingTypeEnum;
import com.sy.sanguo.game.competition.model.enums.CompetitionScoreDetailStatusEnum;
import com.sy.sanguo.game.competition.model.param.CompetitionClearingModelReq;
import com.sy.sanguo.game.competition.model.param.CompetitionClearingModelRes;
import com.sy.sanguo.game.competition.model.param.CompetitionClearingPlay;
import com.sy.sanguo.game.competition.service.CompetitionPlayingService;
import com.sy.sanguo.game.competition.service.CompetitionPushService.CompetitionPushModel;
import com.sy.sanguo.game.competition.util.AttributeKV;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Guang.OuYang
 * @date 2020/7/21-14:14
 */
@Component
public class CompetitionClearingApply extends CompetitionPlayingService {

	public CompetitionClearingApply() {
		super.CHILDREN_CLEAING_ACTOR.put(CompetitionPlayingTypeEnum.APPLY_PLAYING, this);
	}

	/**
	 *@description 赛场处理, 当前的玩家是进入下一个回合还是继续匹配, 还是大结算
	 *@param build gameServer请求参数
	 *@param playing 赛场
	 *@param playingConfig 赛场配置
	 *@param upRoundResolve 赛场配置
	 *@param curStep 上一个回合\当前回合
	 *@param curRound 上一个回合\当前回合
	 *@param noClearingTableCount 当前未结算牌桌
	 *@param curTotalHuman 上一个轮次的总人数
	 *@param curRate 当前底分
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/11
	 */
	public void playingHandle(CompetitionClearingModelReq build,
			CompetitionPlayingDB playing,
			CompetitionPlayingConfigDB playingConfig,
			CompetitionClearingRiseInRankMode upRoundResolve,
			int curStep,
			int curRound,
			long noClearingTableCount,
			int curTotalHuman,
			int curRate,
			CompetitionClearingModelRes competitionClearingModelRes) {
		//主赛场常量
		CompetitionPlayingDB playingf = playing;

		//等待其他牌桌完成结算
		boolean theNextStage = noClearingTableCount == 0;

		//当前回合的淘汰机制
		CompetitionClearingRiseInRankMode curRoundResolve = upRoundResolve;

		//小结,其他牌桌已经结算完毕
		//这里的结算一定保证是其中某台服务接到结算,不会重复进入逻辑
		boolean nextStep = false;
		//大结算标志
		boolean bigStage = false;

		//这一轮剩余玩家
		List<CompetitionClearingPlay> currentStepAllPlay = null;

		//当前晋级的总人数
		int curUpLevelHuman = 0;

		//牌桌现均结算完毕
		if(theNextStage) {
			//清理所有空余房间
//			nSynClearingTable(playingf);

			//输家重排名
			//赢家重排名
			//当前晋级的总人数
			AttributeKV<Long> longAttributeKV = Optional.ofNullable(weedOutPlayReSortRank(playing, curStep, curRound, upRoundResolve)).orElse(new AttributeKV<>(0l,0l));
			curUpLevelHuman = longAttributeKV.getKey().intValue();

			//滚动轮次或者滚动回合数,晋级人数低于当前轮次配置的人数 或当前回合大于配置的总回合数结算晋级人数
			//淘汰分0包含特殊含义:根据配置的回合数到达就进入下一个轮次,不为0则一直淘汰到需要晋级的人数
			nextStep = curUpLevelHuman <= upRoundResolve.getRoundEndHuman() || (upRoundResolve.getRoundLowScore() == 0 && curRound >= upRoundResolve.getRoundLength());
			//下个回合\下个轮次
			playing = rollStepOrRound(playing, nextStep);
			//替换当前回合配置
			curRoundResolve = CompetitionClearingRiseInRankMode.resolve(playing, playingConfig, build);

			//从打立出局->淘汰赛可以晋级的TopN
			//不能同时存在每桌前N名晋级名单
			if (nextStep && upRoundResolve.getRoundUpLevelHuman() > 0 && upRoundResolve.getNextStepNotInTopNOut() <= 0) {
				//赛制晋级人数前n名晋级
				competitionScoreDetailDao.updateTopNByPlayingId(playingf.getId(), playingf.getCurStep(), playingf.getCurRound(), upRoundResolve.getRoundUpLevelHuman());
				//总榜状态变更,根据排名
				competitionScoreTotalDao.updateStatusByPlayingId(CompetitionScoreTotalDB.builder().playingId(playing.getId()).rank(upRoundResolve.getRoundUpLevelHuman()).build());

				LogUtil.i("competition|playing|clearing|topN|" + playing.getId() + "|" + playing.getCurStep() + "|" + playing.getCurRound() + "|curRound|" + curRound + "|nextStep:" + nextStep + "|" + curUpLevelHuman + "|" + upRoundResolve);
			}

			//大结算
			bigStage = playing.getStatus() == CompetitionPlayingStatusEnum.PLAYING && (playing.getCurStep() > curRoundResolve.getStepLength() || (theNextStage && curUpLevelHuman <= playingConfig.getEndHuman()));

			//当前底分
			curRate = curRoundResolve.getBaseScore();

			LogUtil.i("Competition|playing|clearing|isOver|" + playing.getId() + "|" + playing.getCurStep() + "|" + playing.getCurRound() + "|curUpLevelHuman|" + curUpLevelHuman + "|isOver:" + bigStage + "|theNextStage:" + theNextStage + "|" + curRoundResolve.getStepLength() + "|" + curTotalHuman + "|" + playingConfig.getEndHuman());

			//大结算一定按分数晋级, 这里不做输家重排名
			if(!bigStage){
				//输家重排名
				//赢家重排名
				weedOutPlayReSortRank(playing, curStep, curRound, upRoundResolve);
			}

			//下一个回合,重复匹配, 展示结算界面 -------------------------------------

			//现阶段所有的玩家数据
			Optional<List<CompetitionScoreDetailDB>> stepAllScoreDetailOptional = competitionScoreDetailDao.queryForList(
					CompetitionScoreDetailDB.builder()
							.playingId(playing.getId())
							.lastStep(curStep)
							.lastRound(curRound)
							.build());

			//现阶段所有玩家数据
			if (stepAllScoreDetailOptional.isPresent()) {
				//所有数据转化
				currentStepAllPlay = stepAllScoreDetailOptional.get().stream().map(v -> CompetitionClearingPlay.builder()
						.userId(v.getUserId())
						.score(v.getLastScore())
						.rank(v.getLastRank())
						.isOver(v.getLastStatus())
						.build()).collect(Collectors.toList());

				//变更总榜状态并推送
				//这里更新最终榜单,并且替换掉原有积分
				//最终榜单推送只会给还存活的玩家推送,这里不做推送,返回结算数据时携带数据推送给玩家
				cgeTotalRankAndPush(playing, currentStepAllPlay, true, true, false);

				//当前赛事参与的所有人
				List<Long> users = currentStepAllPlay.stream().map(v -> v.getUserId()).collect(Collectors.toList());

				//全部牌桌完成后的结算数据
				CompetitionClearingModelRes clearingModelRes = buildResModelClearing(
						nextStep && !bigStage,
						bigStage,
						currentStepAllPlay,
						playingConfig,
						playing,
						upRoundResolve,
						curRoundResolve,
						curTotalHuman,
						theNextStage,
						1,
						curRate,
						//各玩家排名 UserId->CompetitionScoreTotalDB
						stepAllScoreDetailOptional.get().stream().map(v ->
								CompetitionScoreTotalDB.builder()
										.userId(v.getUserId())
										.rank(v.getLastRank())
										.build())
								.collect(Collectors.groupingBy(CompetitionScoreTotalDB::getUserId)));

				//这里的积分比例存入赛事流水时就已经换算过
				//批量推送最终晋级成功或失败结算界面
				CompetitionPushModel arg;
				competitionPushService.showClearingInfo(arg = CompetitionPushModel.builder()
						.userIds(users)
						//这里的积分比例存入赛事流水时就已经换算过
						.clearModelRes(clearingModelRes)
						.build());
				LogUtil.i("competition|playing|showClearing|" + playing.getId() + "|" + playing.getCurStep() + "|" + playing.getCurRound() + "|" + arg + "|" + users.size());
			}

			try{
				//失败者离开状态
				competitionApplyDao.updatePlay(playingf.getId(), 0, currentStepAllPlay.stream().filter(v -> v.getIsOver() == 2).map(v -> v.getUserId()).collect(Collectors.toList()));
			}catch (Exception e){
				LogUtil.e("competition|playing|updatePlay", e);
			}

			//大结算的奖励单独发放,失败方不需要重复发送,仅在其他阶段需要发送失败方的奖励
			//大结算
			if (bigStage) {
				//大结算仅前几名的奖励发放
				bigClearingPlayingClearing(playing, upRoundResolve);
				//大结不重复匹配
				currentStepAllPlay = null;
			} else {
				List<CompetitionClearingPlay> currentStepAllPlay1 = currentStepAllPlay;
				competitionJobSchedulerThreadPool.nSynBus(() -> {
					//失败的发送奖励
					currentStepAllPlay1.stream().filter(v -> v.getIsOver() == CompetitionScoreDetailStatusEnum.WEED_OUT).forEach(v -> {
						try {
							//失败的直接发送奖励
							singleSendAward(playingf, playingConfig.getAwards().split("_"), v.getUserId(), v.getRank());
						} catch (Exception e) {
							LogUtil.e("Competition|playing|AwardInfo|error|" + playingf.getId() + "|" + playingConfig.getAwards() + "|" + v.getUserId() + "|" + v.getRank(), e);
						}
					});
				});
			}
		}

		//需要重复匹配
		if(!CollectionUtils.isEmpty(currentStepAllPlay)) {
			//过滤出晋级玩家
			List<Long> rankUpUserId = filterRankUpUser(currentStepAllPlay);

			LogUtil.i("Competition|playing|clearing|allEndRepeatedMatch|" + curUpLevelHuman + "|" + upRoundResolve + "|" + currentStepAllPlay.size());

			//通知这一批客户端切服
			notifyPlayCgeGameServer(playing, playingConfig, rankUpUserId);

			//重复匹配
			waitAndRepeatedMatch(playing, playingConfig, upRoundResolve, curRoundResolve, curUpLevelHuman, curTotalHuman, theNextStage, curRate, bigStage, nextStep, currentStepAllPlay, rankUpUserId, null,0);

			LogUtil.i("Competition|playing|clearing|match|end|" + competitionClearingModelRes);
		}
	}
}
