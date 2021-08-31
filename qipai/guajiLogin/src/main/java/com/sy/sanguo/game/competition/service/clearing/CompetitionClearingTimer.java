//package com.sy.sanguo.game.competition.service.clearing;
//
//import com.sy.sanguo.game.competition.model.db.*;
//import com.sy.sanguo.game.competition.model.enums.CompetitionPlayingStatusEnum;
//import com.sy.sanguo.game.competition.model.enums.CompetitionPlayingTypeEnum;
//import com.sy.sanguo.game.competition.model.enums.CompetitionScoreDetailStatusEnum;
//import com.sy.sanguo.game.competition.model.param.CompetitionClearingModelReq;
//import com.sy.sanguo.game.competition.model.param.CompetitionClearingModelRes;
//import com.sy.sanguo.game.competition.model.param.CompetitionClearingPlay;
//import com.sy.sanguo.game.competition.service.CompetitionPlayingService;
//import com.sy.sanguo.game.competition.service.CompetitionPushService.CompetitionPushModel;
//import com.sy.sanguo.game.pdkuai.util.LogUtil;
//import org.apache.commons.collections.CollectionUtils;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
///**
// * @author Guang.OuYang
// * @date 2020/7/21-14:14
// */
//@Component
//public class CompetitionClearingTimer extends CompetitionPlayingService {
//
//	public CompetitionClearingTimer() {
//		super.CHILDREN_CLEAING_ACTOR.put(CompetitionPlayingTypeEnum.TIMER_PLAYING, this);
//	}
//
//	/**
//	 *@description 赛场处理, 当前的玩家是进入下一个回合还是继续匹配, 还是大结算
//	 *@param build gameServer请求参数
//	 *@param playing 赛场
//	 *@param playingConfig 赛场配置
//	 *@param upRoundResolve 赛场配置
//	 *@param curStep 上一个回合\当前回合
//	 *@param curRound 上一个回合\当前回合
//	 *@param noClearingTableCount 当前未结算牌桌
//	 *@param curTotalHuman 上一个轮次的总人数
//	 *@param curRate 当前底分
//	 *@return
//	 *@author Guang.OuYang
//	 *@date 2020/6/11
//	 */
//	public void playingHandle(CompetitionClearingModelReq build,
//			CompetitionPlayingDB playing,
//			CompetitionPlayingConfigDB playingConfig,
//			CompetitionClearingRiseInRankMode upRoundResolve,
//			int curStep,
//			int curRound,
//			long noClearingTableCount,
//			int curTotalHuman,
//			int curRate,
//			CompetitionClearingModelRes competitionClearingModelRes) {
//		//主赛场常量
//		CompetitionPlayingDB playingf = setAddField(playing);
//
////		//这里不同用户id打到不同的索引, 期望中不会出现delockerror
////		//更新总榜状态
////		//排名调度更新
////		competitionScoreTotalDao.updatePlayStatus(playing.getId(), competitionClearingModelRes.getPlays().stream().filter(v -> v.getIsOver() == 2).map(v -> v.getUserId()).collect(Collectors.toList()));
////		//更改玩家进入房间状态
////		updateInRoom(playing.getId(), competitionClearingModelRes.getPlays().stream().map(v -> v.getUserId()).collect(Collectors.toList()), false);
////		//延迟更新榜单, 给失败者发送奖励
////		competitionPushService.pushRankRefresh(CompetitionPushService.CompetitionRefreshQueueModel.builder().playingId(playingf.getId()).build());
//
//		//分数淘汰局,实时更新总人数与排名
//		//固定局不推送总人数变动,仅依赖结算时更新当前排名
//		if (upRoundResolve.getRoundLowScore() > 0) {
//			synchronized (competitionPlayingDao) {
//				cgeTotalRankAndPush(playingf, competitionClearingModelRes.getPlays(), true, true, true);
//			}
//			//更新总榜状态
//			//排名调度更新
////			competitionScoreTotalDao.updatePlayStatus(playing.getId(), competitionClearingModelRes.getPlays().stream().filter(v -> v.getIsOver() == 2).map(v -> v.getUserId()).collect(Collectors.toList()));
//		}
//
//		//输家重排名
//		//赢家重排名
////		weedOutPlayReSortRank(playing, curStep, curRound, upRoundResolve);
////		LogUtil.i("competition|playingHandle|weedOutPlayReSortRank|end|" + playing.getId() + "|" + playing.getCurStep() + "|" + playing.getCurRound() + "|" + competitionClearingModelRes);
//
//		//当前回合的淘汰机制
//		CompetitionClearingRiseInRankMode curRoundResolve = upRoundResolve;
//
//		//当前晋级的总人数
//		int curUpLevelHuman = getCurUpLevelHuman(playing, curStep, curRound);
//
//		//等待其他牌桌完成结算
//		//人数满足晋级下一个轮次的人数
//		//仅预赛需要
//		CompetitionRoom competitionRoom = competitionRoomDao.queryNotStartRoomByCurCount(playing.getId());
//		//还有没有打完的牌桌
//		boolean noExistsTable = competitionRoom == null || competitionRoom.getCurrentCount() == competitionRoom.getMaxCount();
//		boolean theNextStage = noClearingTableCount <= 0 && competitionRoomDao.queryByPlayingCount(playing.getId(), 0l) == 0;
//
//		//小结,其他牌桌已经结算完毕
//		//这里的结算一定保证是其中某台服务接到结算,不会重复进入逻辑
//		//滚动轮次或者滚动回合数,晋级人数低于当前轮次配置的人数 或当前回合大于配置的总回合数结算晋级人数
//		//淘汰分0包含特殊含义:根据配置的回合数到达就进入下一个轮次,不为0则一直淘汰到需要晋级的人数
//		boolean nextStep = false;
//		//大结算标志
//		boolean bigStage = false;
//		//匹配时刷新需要匹配的数据
//		boolean matchRefreshData = upRoundResolve.getRoundLowScore() > 0 && (!isBreakWeedOutStep(curRoundResolve));
//		//现阶段所有的玩家数据
//		Optional<List<CompetitionScoreDetailDB>> stepAllScoreDetailOptional = null;
//		//这一轮剩余玩家
//		List<CompetitionClearingPlay> currentStepAllPlay = matchRefreshData ? competitionClearingModelRes.getPlays() : Collections.emptyList();
//
//		if(!CollectionUtils.isEmpty(competitionClearingModelRes.getPlays())){
//			//变更当前结算玩家->空闲
//			updateInRoom(playingf.getId(), competitionClearingModelRes.getPlays().stream().map(v -> v.getUserId()).collect(Collectors.toList()), false);
//		}
//
//		//当前所有玩家加入房间的状态
//		List<CompetitionScoreTotalDB> rankList = competitionScoreTotalDao.queryByInRoom(CompetitionScoreTotalDB.builder().playingId(playing.getId()).build()).orElse(Collections.emptyList());
//
//		if(theNextStage) { //牌桌现均结算完毕
////			nSynClearingTable(playingf);
//
//			//滚动轮次或者滚动回合数,晋级人数低于当前轮次配置的人数 或当前回合大于配置的总回合数结算晋级人数
//			//淘汰分0包含特殊含义:根据配置的回合数到达就进入下一个轮次,不为0则一直淘汰到需要晋级的人数
//			nextStep = curUpLevelHuman <= upRoundResolve.getRoundEndHuman() || (upRoundResolve.getRoundLowScore() == 0 && curRound >= upRoundResolve.getRoundLength());
//
//			//下个回合\下个轮次
//			playing = setAddField(rollStepOrRound(playing, nextStep));
//			//替换当前回合配置
//			curRoundResolve = CompetitionClearingRiseInRankMode.resolve(playing, playingConfig, build);
//
//			//从打立出局->淘汰赛可以晋级的TopN
//			//不能同时存在每桌前N名晋级名单
//			if (nextStep && upRoundResolve.getRoundUpLevelHuman() > 0) {
//				//赛制晋级人数前n名晋级
//				competitionScoreDetailDao.updateTopNByPlayingId(playingf.getId(), playingf.getCurStep(), playingf.getCurRound(), upRoundResolve.getRoundUpLevelHuman());
//				//总榜状态变更,根据排名
//				competitionScoreTotalDao.updateStatusByPlayingId(CompetitionScoreTotalDB.builder().playingId(playing.getId()).rank(upRoundResolve.getRoundUpLevelHuman()).build());
//
//				LogUtil.i("competition|playing|clearing|topN|" + playing.getId() + "|" + playing.getCurStep() + "|" + playing.getCurRound() + "|curRound|" + curRound + "|nextStep:" + nextStep + "|" + curUpLevelHuman + "|" + upRoundResolve);
//			}
//			//没有等待的桌子,这一轮的玩家自行匹配一桌
//			//输家重排名
//			//赢家重排名
//			weedOutPlayReSortRank(playing, curStep, curRound, upRoundResolve);
//
//			stepAllScoreDetailOptional = competitionScoreDetailDao.queryForList(
//					CompetitionScoreDetailDB.builder()
//							.playingId(playing.getId())
//							.lastStep(curStep)
//							.lastRound(curRound)
//							.build());
//
//			//大结算
//			bigStage = playing.getStatus() == CompetitionPlayingStatusEnum.PLAYING && (playing.getCurStep() > curRoundResolve.getStepLength() || (theNextStage && curUpLevelHuman <= playingConfig.getEndHuman()));
//
//			//当前底分
//			curRate = curRoundResolve.getBaseScore();
//
//			//下一个回合,重复匹配,展示结算界面 -------------------------------------
//			//现阶段所有玩家数据
//			if (stepAllScoreDetailOptional.isPresent()) {
//
//				//变更进入下一轮的倍率
//				stepAllScoreDetailOptional = refreshScoreBasicAndLastScore(playing, curRoundResolve, stepAllScoreDetailOptional);
//
//				//筛选->展示结算界面->这一轮最终排名
//				currentStepAllPlay = filterTheNextPlays(playing, playingConfig, upRoundResolve, curTotalHuman, curRate, curRoundResolve, rankList, theNextStage, nextStep, bigStage, stepAllScoreDetailOptional);
//
//				//发送淘汰的奖励, 同时更新房间状态
//				sendWeedOutAwardAndUpdateInRoom(playingConfig, playingf, currentStepAllPlay, false);
//
//				currentStepAllPlay = currentStepAllPlay.stream().filter(v -> v.getIsOver() == 1).collect(Collectors.toList());
//				//不需要刷新数据了
//				matchRefreshData = false;
//				//状态变更->空闲
//				updateInRoom(playingf.getId(), currentStepAllPlay.stream().map(v -> v.getUserId()).collect(Collectors.toList()), false);
//
//				try{
//					//失败者离开状态
//					competitionApplyDao.updatePlay(playingf.getId(), 0, currentStepAllPlay.stream().filter(v -> v.getIsOver() == 2).map(v -> v.getUserId()).collect(Collectors.toList()));
//				} catch (Exception e){
//					LogUtil.e("competition|playing|updatePlay", e);
//				}
//			}
//
//			LogUtil.i("Competition|playing|clearing|isOver|" + playing.getId() + "|" + playing.getCurStep() + "|" + playing.getCurRound() + "|curUpLevelHuman|" + curUpLevelHuman + "|isOver:" + bigStage + "|theNextStage:" + theNextStage + "|" + curRoundResolve.getStepLength() + "|" + curTotalHuman + "|" + playingConfig.getEndHuman());
//		}
//
//		try{
//			//失败者离开状态
//			competitionApplyDao.updatePlay(playingf.getId(), 0, currentStepAllPlay.stream().filter(v -> v.getIsOver() == 2).map(v -> v.getUserId()).collect(Collectors.toList()));
//		} catch (Exception e) {
//			LogUtil.e("competition|playing|updatePlay", e);
//		}
//
//		//大结算的奖励单独发放,失败方不需要重复发送,仅在其他阶段需要发送失败方的奖励
//		//大结算
//		if (bigStage) {
//			//大结算仅前几名的奖励发放
//			bigClearingPlayingClearing(playing, upRoundResolve);
//			//大结不重复匹配
//			currentStepAllPlay = Collections.emptyList();
//			//不需要刷新数据了
//			matchRefreshData = false;
//		}
//
//		//发送淘汰的奖励, 同时更新房间状态
//		sendWeedOutAwardAndUpdateInRoom(playingConfig, playingf, currentStepAllPlay, true);
//
//		LogUtil.i("Competition|playing|clearing|allEndRepeatedMatch" + playing.getId() + "|" + playing.getCurStep() + "|" + playing.getCurRound() + "|" + curUpLevelHuman + "|" + upRoundResolve + "|" + currentStepAllPlay.size() + "|" + currentStepAllPlay.stream().map(v -> v.getUserId()).collect(Collectors.toList()));
//
//		//重复匹配
//		waitAndRepeatedMatch(playing, playingConfig, upRoundResolve, curRoundResolve, currentStepAllPlay, curTotalHuman, theNextStage, curRate, bigStage, nextStep, curStep, curRound, 0, matchRefreshData, noExistsTable);
//
//		LogUtil.i("Competition|playing|clearing|match|end|" + competitionClearingModelRes);
//	}
//
//
//	/**
//	 *@description 进入下一轮时更新底分和转化倍率
//	 *@param
//	 *@return
//	 *@author Guang.OuYang
//	 *@date 2020/8/4
//	 */
//	private Optional<List<CompetitionScoreDetailDB>> refreshScoreBasicAndLastScore(CompetitionPlayingDB playingDB, CompetitionClearingRiseInRankMode curRoundResolve, Optional<List<CompetitionScoreDetailDB>> stepAllScoreDetailOptional) {
//		float convertRate = curRoundResolve.getConvertRate();
//
//		if(convertRate != 1){
//			//更新当前进入游戏底分
//			competitionScoreDetailDao.updateScoreBasicRatio(stepAllScoreDetailOptional.get().stream().map(v -> v.getId()).collect(Collectors.toList()), convertRate);
//			competitionScoreTotalDao.updateScoreBasicRatio(playingDB.getId(), convertRate);
//
//			//更新新的倍率
//			return Optional.ofNullable(new ArrayList<>(stepAllScoreDetailOptional.get()).stream().map(v -> {
//				v.setScoreBasicRatio(convertRate);
//				v.setLastScore((int) (v.getLastScore() * v.getScoreBasicRatio()));
//				return v;
//			}).collect(Collectors.toList()));
//		}
//
//		return stepAllScoreDetailOptional;
//	}
//
//	/**
//	 *@description  下个回合匹配的玩家
//	 *@param
//	 *@return
//	 *@author Guang.OuYang
//	 *@date 2020/7/24
//	 */
//	private List<CompetitionClearingPlay> filterTheNextPlays(CompetitionPlayingDB playing, CompetitionPlayingConfigDB playingConfig, CompetitionClearingRiseInRankMode upRoundResolve, int curTotalHuman, int curRate,
//			CompetitionClearingRiseInRankMode curRoundResolve, List<CompetitionScoreTotalDB> rankList, boolean theNextStage, boolean nextStep, boolean bigStage, Optional<List<CompetitionScoreDetailDB>> stepAllScoreDetailOptional) {
//		List<CompetitionClearingPlay> currentStepAllPlay;//				//所有数据转化
//		currentStepAllPlay = stepAllScoreDetailOptional.get().stream().filter(v ->
//				//淘汰赛赛制,不给失败的继续推送
//				//非淘汰赛仅赢家推送
//				((( v.getLastStatus() == 1) || (rankList.stream().anyMatch(v1 -> v1.getUserId().equals(v.getUserId()) && !v1.getInRoom()))))).map(v ->
//				dlsConvertToCPlay(v)).collect(Collectors.toList());
//
//		//当前赛事参与的所有人
//		List<Long> users = currentStepAllPlay.stream().map(v -> v.getUserId()).collect(Collectors.toList());
//
//		//全部牌桌完成后的结算数据
//		CompetitionClearingModelRes clearingModelRes = buildResModelClearing(
//				nextStep && !bigStage,
//				bigStage,
//				currentStepAllPlay,
//				playingConfig,
//				playing,
//				upRoundResolve,
//				curRoundResolve,
//				curTotalHuman,
//				theNextStage,
//				1,
//				curRate,
//				//各玩家排名 UserId->CompetitionScoreTotalDB
//				stepAllScoreDetailOptional.get().stream().map(v ->
//						CompetitionScoreTotalDB.builder()
//								.userId(v.getUserId())
//								.rank(v.getLastRank())
//								.build())
//						.collect(Collectors.groupingBy(CompetitionScoreTotalDB::getUserId)));
//
//        CompetitionClearingModelRes copy = clearingModelRes.copy();
//        //仅给失败的推送
//		copy.setPlays(clearingModelRes.getPlays().stream().filter(v -> bigStage || v.getIsOver() == 2).collect(Collectors.toList()));
//        competitionJobSchedulerThreadPool.nSynBus(()->{
//			//这里的积分比例存入赛事流水时就已经换算过
//			//批量推送最终晋级成功或失败结算界面
//			CompetitionPushModel arg;
//			competitionPushService.showClearingInfo(arg = CompetitionPushModel.builder()
//					.userIds(users)
//					//这里的积分比例存入赛事流水时就已经换算过
//					.clearModelRes(copy)
//					.build());
//			LogUtil.i("competition|playing|showClearing|" + playing.getId() + "|" + playing.getCurStep() + "|" + playing.getCurRound() + "|" + arg + "|" + users.size());
//        });
//		return currentStepAllPlay;
//	}
//
//	private int getCurUpLevelHuman(CompetitionPlayingDB playing, int curStep, int curRound) {
//		return competitionScoreDetailDao.queryByCount(
//				CompetitionScoreDetailDB.builder()
//						.playingId(playing.getId())
//						.lastStep(curStep)
//						.lastRound(curRound)
//						.lastStatus(CompetitionScoreDetailStatusEnum.RISE_IN_RANK)
//						.build());
//	}
//
//	private CompetitionClearingPlay dlsConvertToCPlay(CompetitionScoreDetailDB v) {
//		return CompetitionClearingPlay.builder()
//				.userId(v.getUserId())
//				.score(v.getLastScore())
//				.rank(v.getLastRank())
//				.isOver(v.getLastStatus())
//				.build();
//	}
//}
