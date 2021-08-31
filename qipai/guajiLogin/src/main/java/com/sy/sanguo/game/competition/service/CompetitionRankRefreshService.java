package com.sy.sanguo.game.competition.service;

import com.sy.sanguo.game.competition.dao.*;
import com.sy.sanguo.game.competition.job.CompetitionJobSchedulerThreadPool;
import com.sy.sanguo.game.competition.model.db.*;
import com.sy.sanguo.game.competition.model.enums.CompetitionScoreDetailStatusEnum;
import com.sy.sanguo.game.competition.model.param.CompetitionClearingModelRes;
import com.sy.sanguo.game.competition.model.param.CompetitionClearingPlay;
import com.sy.sanguo.game.competition.model.param.CompetitionRefreshQueueModel;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 消息推送
 *
 * @author Guang.OuYang
 * @date 2020/5/20-16:29
 */
@Service
public class CompetitionRankRefreshService {
    private final static Byte Z_BYTE_CHAR = Byte.valueOf("0");
    private final static ConcurrentHashMap<CompetitionRefreshQueueModel, Byte> RANK_PUSH_MSG = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<CompetitionRefreshQueueModel, Byte> RANK_TOTAL_CACHE_REFRESH_PUSH_MSG = new ConcurrentHashMap<>();

    private final static ConcurrentHashMap<Long, CopyOnWriteArrayList<CompetitionClearingPlay>> PLAYING_RANKS=new ConcurrentHashMap<>();

    @Autowired
    private CompetitionScoreDetailDao<CompetitionScoreDetailDB> competitionScoreDetailDao;
    @Autowired
    private CompetitionScoreTotalDao competitionScoreTotalDao;
    @Autowired
    private CompetitionRoomDao competitionRoomDao;
    @Autowired
    private CompetitionPlayingService competitionPlayingService;
    @Autowired
    private CompetitionGeneratePlayingService competitionGeneratePlayingService;
    @Autowired
    private CompetitionRankService competitionRankService;
    @Autowired
    private CompetitionPushService competitionPushService;
    @Autowired
    private CompetitionPlayingDao competitionPlayingDao;
    @Autowired
    private CompetitionMatchService competitionMatchService;
    @Autowired
    private CompetitionJobSchedulerThreadPool competitionJobSchedulerThreadPool;
    @Autowired
    private CompetitionApplyDao competitionApplyDao;

    public void resetRankAndSort(Long playingId, CopyOnWriteArrayList<CompetitionClearingPlay> ranks) {
        PLAYING_RANKS.put(playingId, ranks);
        sortRank(playingId);
    }

    public void updateUserRankData(Long playingId, CompetitionClearingPlay arg){
        if(!PLAYING_RANKS.isEmpty()){
            CopyOnWriteArrayList<CompetitionClearingPlay> competitionScoreTotalDBS = PLAYING_RANKS.get(playingId);
            competitionScoreTotalDBS.removeIf(v -> Long.valueOf(v.getUserId()).equals(arg.getUserId()));
            competitionScoreTotalDBS.add(arg);
        }
//        sortRank(playingId);
    }

    private void checkResetTotalRankCache(long playingId, CompetitionPlayingDB playingFinal) {
        if(PLAYING_RANKS.isEmpty()) {
            List<CompetitionScoreTotalDB> loseListInRoom = competitionRankService.getRank(CompetitionScoreTotalDB.builder().inRoom(false).playingId(playingId).limit(playingFinal.getCurHuman()).build());
            resetRankAndSort(playingFinal.getId(), loseListInRoom.stream().map(arg -> CompetitionClearingPlay.builder()
                    .userId(arg.getUserId())
                    .score(arg.getScore())
                    .rank(arg.getRank())
                    .tableRank(0)
                    .isOver(arg.getStatus())
                    .build()).collect(Collectors.toCollection(CopyOnWriteArrayList::new)));
        }
    }

    public void removeWeedOutAndSort(CompetitionPlayingDB playing, CompetitionPlayingService.CompetitionClearingRiseInRankMode curRoundResolve) {
        CopyOnWriteArrayList<CompetitionClearingPlay> competitionScoreTotalDBS = PLAYING_RANKS.get(playing.getId());
        Iterator<CompetitionClearingPlay> iterator = competitionScoreTotalDBS.iterator();
        while (iterator.hasNext()) {
            CompetitionClearingPlay arg = iterator.next();
            if (competitionPlayingService.roundWeedOutCheck(playing, curRoundResolve.getRoundLowScore(), curRoundResolve.getNextStepNotInTopNOut(), arg) != 1) {
                iterator.remove();//淘汰
            }
        }

        sortRank(playing.getId());
    }

    public void sortRank(Long playingId) {
        Collections.sort(PLAYING_RANKS.get(playingId), Comparator.comparing(CompetitionClearingPlay::getScore));
        int i = 1;
        Iterator<CompetitionClearingPlay> iterator = PLAYING_RANKS.get(playingId).iterator();
        while (iterator.hasNext()) {
            CompetitionClearingPlay next = iterator.next();
            next.setRank(i);
            i++;
        }
    }

    public void addRankRefreshQueue(CompetitionRefreshQueueModel queueModel) {
        if (RANK_PUSH_MSG.containsKey(queueModel)) {
            RANK_PUSH_MSG.remove(queueModel);
        }
        RANK_PUSH_MSG.put(queueModel, Z_BYTE_CHAR);
    }

    public CompetitionRefreshQueueModel pollRankMsg() {
        Iterator<Map.Entry<CompetitionRefreshQueueModel, Byte>> iterator = RANK_PUSH_MSG.entrySet().iterator();
        if (iterator.hasNext()) {
            Map.Entry<CompetitionRefreshQueueModel, Byte> next = iterator.next();
            iterator.remove();
            return next.getKey();
        }
        return null;
    }

    public void addRankTotalCacheRefreshQueue(CompetitionRefreshQueueModel queueModel) {
        RANK_TOTAL_CACHE_REFRESH_PUSH_MSG.put(queueModel, Z_BYTE_CHAR);
    }

    public CompetitionRefreshQueueModel pollTotalRefreshRankMsg() {
        Iterator<Map.Entry<CompetitionRefreshQueueModel, Byte>> iterator = RANK_TOTAL_CACHE_REFRESH_PUSH_MSG.entrySet().iterator();
        if (iterator.hasNext()) {
            Map.Entry<CompetitionRefreshQueueModel, Byte> next = iterator.next();
            iterator.remove();
            return next.getKey();
        }
        return null;
    }

//    public void refreshTotalRank() {
//        if (RANK_TOTAL_CACHE_REFRESH_PUSH_MSG.isEmpty()) {
//            return;
//        }
//
//        CompetitionRefreshQueueModel nextQueue = pollTotalRefreshRankMsg();
//        CompetitionPlayingDB playingFinal = nextQueue.getPlaying();
//
//        //该回合的榜单
//        Optional<List<CompetitionScoreDetailDB>> curScoreDetails = competitionScoreDetailDao.queryForList(
//                CompetitionScoreDetailDB.builder()
//                        .playingId(playingFinal.getId())
//                        .lastStep(playingFinal.getCurStep())
//                        .lastRound(playingFinal.getCurRound())
//                        .build());
//
//        if (curScoreDetails.isPresent() && !CollectionUtils.isEmpty(curScoreDetails.get())) {
//            competitionPlayingService.lockAndRetry(competitionPlayingDao, playingFinal.getId(), () -> {
//                try {
//                    //更新总榜状态
//                    competitionScoreTotalDao.insertBatch(true, true, true, curScoreDetails.get()
//                            .stream()
//                            .map(v -> CompetitionScoreTotalDB
//                                    .builder()
//                                    .userId(v.getUserId())
//                                    .playingId(v.getPlayingId())
//                                    .rank(v.getLastRank())
//                                    .score(v.getLastScore())
//                                    .status(v.getLastStatus())    //状态由结算管理
//                                    .build()).collect(Collectors.toList()));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
//
//            //更新总榜,仅更新存活玩家
//            CompetitionScoreTotalDB build = CompetitionScoreTotalDB.builder().randomVal(competitionPlayingService.getRandomVal()).status(1).playingId(playingFinal.getId()).build();
//
//            //淘汰的排名是全部排名
//            competitionPlayingService.updateTotalRank(build);
//        }
//    }

    public void pushRankRefresh() {
        try {
            if (RANK_PUSH_MSG.isEmpty()) return;

            long t = System.currentTimeMillis();
            CompetitionRefreshQueueModel nextQueue = pollRankMsg();

            long playingId = nextQueue.getPlayingId();


            CompetitionPlayingDB playingFinal = competitionPlayingService.getPlaying(playingId).get();

            LogUtil.i("competition|pushRankRefresh|start|" + playingId + "|" + playingFinal.getCurStep() + "|" + playingFinal.getCurRound() + "|" + nextQueue);

            CompetitionPlayingConfigDB playingConfig = competitionGeneratePlayingService.getPlayingConfig(playingFinal.getPlayingConfigId()).get();

            CompetitionPlayingService.CompetitionClearingRiseInRankMode upRoundResolve
                    = CompetitionPlayingService.CompetitionClearingRiseInRankMode.resolve1(playingFinal, playingConfig);


            //重置总榜缓存
//            checkResetTotalRankCache(playingId, playingFinal);

            //下个回合的校验
            CompetitionPlayingDB playing = Optional.ofNullable(nextStepCge(nextQueue)).orElse(playingFinal);;

            Long playCount = competitionRoomDao.queryPlayCountByRoomConfigId(competitionPlayingService.resolveRoomConfigId(playingFinal.getCurStep(), playingFinal.getCurRound(), playingConfig));

            //跨级,大结
            boolean nextRoundOrStep = !playing.getCurStep().equals(playingFinal.getCurStep()) || !playing.getCurRound().equals(playingFinal.getCurRound());
            //小结
            boolean theNextStage = nextQueue.isTheNextStage();
            //
            CompetitionPlayingService.CompetitionClearingRiseInRankMode curRoundResolve
                    = CompetitionPlayingService.CompetitionClearingRiseInRankMode.resolve1(playing, playingConfig);

            int curTotalHuman = competitionPlayingDao.queryAliveByPlayingId(playing.getId());

            //终结
            boolean bigStage = curTotalHuman < playCount || (playing.getCurStep() > curRoundResolve.getStepLength() || (theNextStage && curTotalHuman <= playingConfig.getEndHuman()));

            CompetitionRoom competitionRoom = competitionRoomDao.queryNotStartRoomByCurCount(playing.getId());
            //还有没开始等待的牌桌
            boolean noExistsTable = competitionRoom == null || competitionRoom.getCurrentCount() == competitionRoom.getMaxCount();

            //需要匹配,淘汰赛阶段,非淘汰赛阶段当前人数等于晋级人数时继续匹配
            boolean needMatch = !bigStage &&
                    (!competitionPlayingService.isBreakWeedOutStep(curRoundResolve, curTotalHuman)
                            || (nextRoundOrStep && curTotalHuman <= upRoundResolve.getRoundEndHuman())
                            || !noExistsTable
                            //人满开赛,当前全部结束牌局,开启下一轮
                            || (upRoundResolve.getRoundLowScore() == 0 && upRoundResolve.getNextStepNotInTopNOut() == 0 && theNextStage)
                    );

            LogUtil.i(String.format("competition|needMatch|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s", playing.getId(), playing.getCurStep(), playing.getCurRound(),
                    nextQueue.getNoClearingTableCount(), noExistsTable, competitionPlayingService.isBreakWeedOutStep(curRoundResolve, curTotalHuman), nextRoundOrStep,curTotalHuman,upRoundResolve.getRoundEndHuman()
                    ,upRoundResolve.getRoundLowScore(),upRoundResolve.getNextStepNotInTopNOut(), needMatch));

            //输家重排名
            //赢家重排名
            competitionPlayingService.weedOutPlayReSortRank(playingFinal, playingFinal.getCurStep(), playingFinal.getCurRound(), upRoundResolve);

            //该回合的榜单
            Optional<List<CompetitionScoreDetailDB>> curScoreDetails = competitionScoreDetailDao.queryForList(
                    CompetitionScoreDetailDB.builder()
                            .playingId(playingId)
                            .lastStep(playingFinal.getCurStep())
                            .lastRound(playingFinal.getCurRound())
                            .build());
            List<CompetitionScoreTotalDB> loseListInRoom = Collections.emptyList();
            if (curScoreDetails.isPresent() && !CollectionUtils.isEmpty(curScoreDetails.get())) {
                competitionPlayingService.lockAndRetry(competitionPlayingDao, playing.getId(), () -> {
                    try {
                        //更新总榜状态
                        competitionScoreTotalDao.insertBatch(false, true, false, curScoreDetails.get()
                                .stream()
                                .map(v -> CompetitionScoreTotalDB
                                        .builder()
                                        .userId(v.getUserId())
                                        .playingId(v.getPlayingId())
                                        .rank(v.getLastRank())
                                        .score(v.getLastScore())
                                        .status(v.getLastStatus())    //状态由结算管理
                                        .build()).collect(Collectors.toList()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                //更新总榜,仅更新存活玩家
                //淘汰的排名是全部排名
                competitionPlayingService.updateTotalRank(CompetitionScoreTotalDB.builder().randomVal(competitionPlayingService.getRandomVal()).status(1).playingId(playingId).build());

                loseListInRoom = competitionRankService.getRank(CompetitionScoreTotalDB.builder().inRoom(false).playingId(playingId).limit(playing.getCurHuman()).build());

                //这里仅更新状态, 如果是被淘汰的(需要空出来排名),那么名次必定为最后, 这后面无需重排名
                competitionPlayingService.lockAndRetry(competitionPlayingDao, playing.getId(), () -> {
                    try {
                        List<CompetitionScoreTotalDB> collect = curScoreDetails.get()
                                .stream()
                                .map(v -> CompetitionScoreTotalDB
                                        .builder()
                                        .userId(v.getUserId())
                                        .playingId(v.getPlayingId())
                                        .rank(v.getLastRank())
                                        .score(v.getLastScore())
                                        .status(v.getLastStatus())    //状态由结算管理
                                        .build()).collect(Collectors.toList());

                        if(!CollectionUtils.isEmpty(collect)){
                            LogUtil.i("competition|rankRefresh|loseExtTotalRank|" + playingId + "|" + playingFinal.getCurStep() + "|" + playingFinal.getCurRound() + "|" + collect.size() + "|" + curScoreDetails.get());
                            //更新总榜状态
                            competitionScoreTotalDao.insertBatch(true, false, false, collect);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                //淘汰的排名是全部排名
                competitionPlayingService.updateTotalRank(CompetitionScoreTotalDB.builder().randomVal(competitionPlayingService.getRandomVal()).status(1).playingId(playingId).build());
            }

            //玩家离场
            loseExit(loseListInRoom, playingId, playingFinal, playingConfig, upRoundResolve, playing, curRoundResolve, bigStage);

            //刷新推送当前存活玩家的排名
            refreshAndPushAlivePlayRank(playing);

            //匹配消息
            competitionMatchService.addMatchQueue(CompetitionRefreshQueueModel.builder()
                    .playingId(playingId)
                    .bigStage(bigStage)
                    .theNextStage(nextQueue.isTheNextStage())
                    .nextStep(nextRoundOrStep)
                    .upRoundResolve(upRoundResolve)
                    .curRoundResolve(curRoundResolve)
                    .noExistsTable(noExistsTable)
                    .needMatch(needMatch).build());

            if (bigStage) {
                //大结算清理赛场
                competitionPlayingService.bigClearingPlayingClearing(playing, upRoundResolve);
            }

            LogUtil.i("competition|rankRefresh|end|" + (System.currentTimeMillis() - t) + "ms|" + bigStage);

        } catch (Exception e) {
            LogUtil.e("competition|push|rank|error:", e);
        }
    }


    private void refreshAndPushAlivePlayRank(CompetitionPlayingDB playing) {
        //当前存活所有玩家列表
        List<CompetitionScoreTotalDB> rank = competitionRankService.getRank(CompetitionScoreTotalDB.builder().playingId(playing.getId()).status(CompetitionScoreDetailStatusEnum.RISE_IN_RANK).limit(playing.getCurHuman()).build());
        int total = rank.size();
        List<Long> userIds = new ArrayList<>();
        Map<Integer, String> arg = new HashMap<>();

        for (CompetitionScoreTotalDB d : rank) {
            userIds.add(d.getUserId());
            arg.put(d.getUserId().intValue(), d.getRank() + ":" + total);
        }

        //给其他玩家推送实时榜单
        try {
            competitionPushService.playingRankPush(rank.stream().map(v -> v.getUserId()).collect(Collectors.toList()), arg);
        } catch (Exception e) {
            LogUtil.e("competition|rankRefresh|push|error",e);
        }
    }

    /**
     * @param
     * @return
     * @description 进入下个回合/轮次
     * @author Guang.OuYang
     * @date 2020/8/13
     */
    public CompetitionPlayingDB nextStepCge(CompetitionRefreshQueueModel arg) {
        //当前结算牌桌剩余数量
        long noClearingTableCount = arg.getNoClearingTableCount();

        LogUtil.i("competition|nextStepCge|" + arg.getPlayingId() + "|" + noClearingTableCount);
        if (noClearingTableCount > 0) {
            return null;
        }

        boolean theNextStage = noClearingTableCount <= 0 && competitionRoomDao.queryByPlayingCount(arg.getPlayingId(), 0l) == 0;

        LogUtil.i("competition|nextStepCge|" + arg.getPlayingId() + "|" + noClearingTableCount + "|" + competitionRoomDao.queryByPlayingCount(arg.getPlayingId(), 0l));

        arg.setTheNextStage(theNextStage);

        if (theNextStage) { //牌桌现均结算完毕
            CompetitionPlayingDB playing = competitionPlayingService.getPlaying(arg.getPlayingId()).get();
            CompetitionPlayingDB playingf = playing;
            CompetitionPlayingConfigDB playingConfig = competitionGeneratePlayingService.getPlayingConfig(playing.getPlayingConfigId()).get();

            int curUpLevelHuman = competitionPlayingDao.queryAliveByPlayingId(playing.getId());

            CompetitionPlayingService.CompetitionClearingRiseInRankMode upRoundResolve
                    = CompetitionPlayingService.CompetitionClearingRiseInRankMode.resolve1(playingf, playingConfig);

            //滚动轮次或者滚动回合数,晋级人数低于当前轮次配置的人数 或当前回合大于配置的总回合数结算晋级人数
            //淘汰分0包含特殊含义:根据配置的回合数到达就进入下一个轮次,不为0则一直淘汰到需要晋级的人数
            boolean nextStep = curUpLevelHuman <= upRoundResolve.getRoundEndHuman() || (upRoundResolve.getRoundLowScore() == 0 && playingf.getCurRound() >= upRoundResolve.getRoundLength());

            //下个回合\下个轮次
            playing = competitionPlayingService.setAddField(competitionPlayingService.rollStepOrRound(playing, nextStep));

            CompetitionPlayingService.CompetitionClearingRiseInRankMode curRoundResolve
                    = CompetitionPlayingService.CompetitionClearingRiseInRankMode.resolve1(playing, playingConfig);

            //没有等待的桌子,这一轮的玩家自行匹配一桌
            //输家重排名up
            //赢家重排名
            competitionPlayingService.weedOutPlayReSortRank(playing, playingf.getCurStep(), playingf.getCurRound(), upRoundResolve);

            //从打立出局->淘汰赛可以晋级的TopN
            //不能同时存在每桌前N名晋级名单
            if (upRoundResolve.getNextStepNotInTopNOut() > 0) {    //每桌前1晋级
                //赛制晋级人数前n名晋级
                competitionScoreDetailDao.updateTableTopNByPlayingId(playingf.getId(), playingf.getCurStep(), playingf.getCurRound(), upRoundResolve.getNextStepNotInTopNOut());
                //重置人数
                competitionPlayingDao.setAliveByPlayingId(playing.getId(), upRoundResolve.getRoundUpLevelHuman(), upRoundResolve.getRoundUpLevelHuman());

                LogUtil.i("competition|playing|clearing|topTableN|" + playing.getId() + "|" + playing.getCurStep() + "|" + playing.getCurRound() + "|curRound|" + playingf.getCurRound() + "|nextStep:" + nextStep + "|" + curUpLevelHuman + "|" + upRoundResolve);
            } else if (nextStep && (upRoundResolve.getRoundUpLevelHuman() > 0
                    || (curRoundResolve.getNextStepNotInTopNOut() == 0 && curRoundResolve.getRoundLowScore() == 0 && curRoundResolve.isFinalConfig()))) {    //topN晋级
                //赛制晋级人数前n名晋级
                competitionScoreDetailDao.updateTopNByPlayingId(playingf.getId(), playingf.getCurStep(), playingf.getCurRound(), upRoundResolve.getRoundUpLevelHuman());
                //总榜状态变更,根据排名
//                competitionScoreTotalDao.updateStatusByPlayingId(CompetitionScoreTotalDB.builder().playingId(playing.getId()).rank(upRoundResolve.getRoundUpLevelHuman()).build());
                //重置人数
                competitionPlayingDao.setAliveByPlayingId(playing.getId(), upRoundResolve.getRoundUpLevelHuman(), upRoundResolve.getRoundUpLevelHuman());

                LogUtil.i("competition|playing|clearing|topN|" + playing.getId() + "|" + playing.getCurStep() + "|" + playing.getCurRound() + "|curRound|" + playingf.getCurRound() + "|nextStep:" + nextStep + "|" + curUpLevelHuman + "|" + upRoundResolve);
            }

            try {
                if (curRoundResolve.getConvertRate() != 1.0 && CompetitionGeneratePlayingService.isTimerPlaying(playing::getType)) {
                    Optional<List<CompetitionScoreDetailDB>> competitionScoreDetailDBS = competitionScoreDetailDao.queryForList(
                            CompetitionScoreDetailDB.builder()
                                    .playingId(upRoundResolve.getPlayingId())
                                    .lastStep(upRoundResolve.getCurStep())
                                    .lastRound(upRoundResolve.getCurRound())
                                    .build());
                    if(competitionScoreDetailDBS.isPresent() && !CollectionUtils.isEmpty(competitionScoreDetailDBS.get())){
                        //刷新这个回合进入的积分
                        refreshScoreBasicAndLastScore(playing, curRoundResolve, competitionScoreDetailDBS);
                    }
                }
            } catch (Exception e) {
                LogUtil.e("competition|refreshScoreBasicAndLastScore|error",e);
            }


            return playing;
        }
        return null;
    }

    /**
	 *@description 进入下一轮时更新底分和转化倍率
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/8/4
	 */
	private Optional<List<CompetitionScoreDetailDB>> refreshScoreBasicAndLastScore(CompetitionPlayingDB playingDB,
                                                                                   CompetitionPlayingService.CompetitionClearingRiseInRankMode curRoundResolve,
                                                                                   Optional<List<CompetitionScoreDetailDB>> stepAllScoreDetailOptional) {
		float convertRate = curRoundResolve.getConvertRate();

		if(convertRate != 1){
			//更新当前进入游戏底分
			competitionScoreDetailDao.updateScoreBasicRatio(stepAllScoreDetailOptional.get().stream().map(v -> v.getId()).collect(Collectors.toList()), convertRate);
//			competitionScoreTotalDao.updateScoreBasicRatio(playingDB.getId(), convertRate);

			//更新新的倍率
			return Optional.ofNullable(stepAllScoreDetailOptional.get().stream().map(v -> {
				v.setScoreBasicRatio(convertRate);
				v.setLastScore((int) (v.getLastScore() * v.getScoreBasicRatio()));
				return v;
			}).collect(Collectors.toList()));
		}

		return stepAllScoreDetailOptional;
	}

    private void loseExit(List<CompetitionScoreTotalDB> loseListInRoom , long playingId, CompetitionPlayingDB playingFinal, CompetitionPlayingConfigDB playingConfig, CompetitionPlayingService.CompetitionClearingRiseInRankMode upRoundResolve, CompetitionPlayingDB playing, CompetitionPlayingService.CompetitionClearingRiseInRankMode curRoundResolve, boolean bigStage) {
        CompetitionScoreDetailDB build = CompetitionScoreDetailDB.builder()
                .playingId(playingId)
                .lastStep(playingFinal.getCurStep())
                .lastRound(playingFinal.getCurRound())
                .lastStatus(CompetitionScoreDetailStatusEnum.WEED_OUT)
                .build();

        //大结算都推送
        if(bigStage){
            build.setLastStatus(null);
        }

        //失败需要结算的玩家
        List<CompetitionScoreDetailDB> loseList = competitionScoreDetailDao.queryForList(build).get();

        //失败需要结算的玩家
        if (!CollectionUtils.isEmpty(loseList)) {
            LogUtil.i("competition|rankRefresh|loseExt|" + playingId + "|" + playingFinal.getCurStep() + "|" + playingFinal.getCurRound() + "|" + loseList.size());

            Map<Long, Integer> inRooms = new HashMap<>();
            if (!CollectionUtils.isEmpty(loseListInRoom)) {
                loseListInRoom.forEach(v -> inRooms.put(v.getUserId(), v.getRank()));
            }

            //已经结算的不做重复结算
            List<CompetitionClearingPlay> losePlayList = loseList.stream()
                    .filter(v -> v.getLastRank() != null)
                    .filter(v -> inRooms.containsKey(v.getUserId()))
                    .map(v -> CompetitionClearingPlay.builder()
                            .userId(v.getUserId())
                            .score(v.getLastScore())
                            .rank(upRoundResolve.getRoundLowScore() > 0 ? inRooms.get(v.getUserId()) : v.getLastRank())
                            .isOver(2)
                            .build()).collect(Collectors.toList());

            //
            List<Long> loseUserIdList = losePlayList.stream().map(v -> v.getUserId()).collect(Collectors.toList());

            if(!CollectionUtils.isEmpty(loseUserIdList)){
                try {
                    //淘汰的玩家推送
                    competitionPlayingService.sendWeedOutAwardAndUpdateInRoom(playingConfig, playingFinal, losePlayList, true);
                } catch (Exception e) {
                    LogUtil.e("competition|clearing|sendWeedOutAwardAndUpdateInRoom|error|", e);
                }

                //失败在等待的推送
                CompetitionClearingModelRes clearingModelRes = competitionPlayingService.buildResModelClearing(
                        false,
                        true,
                        losePlayList,
                        playingConfig,
                        playingFinal,
                        upRoundResolve,
                        curRoundResolve,
                        0,
                        false,
                        1,
                        1,
                        //各玩家排名 UserId->CompetitionScoreTotalDB
                        losePlayList.stream().map(v ->
                                CompetitionScoreTotalDB.builder()
                                        .userId(v.getUserId())
                                        .rank(v.getRank())
                                        .build())
                                .collect(Collectors.groupingBy(CompetitionScoreTotalDB::getUserId)));

                //这里的积分比例存入赛事流水时就已经换算过
                //批量推送最终晋级成功或失败结算界面
                CompetitionPushService.CompetitionPushModel arg;
                competitionPushService.showClearingInfo(arg = CompetitionPushService.CompetitionPushModel.builder()
                        .userIds(loseUserIdList)
                        //这里的积分比例存入赛事流水时就已经换算过
                        .clearModelRes(clearingModelRes)
                        .build());

                try {
                    List<Long> collect = bigStage ? null : loseList.stream().filter(v -> v.getLastRank() != null && v.getLastStatus() == 2).map(v->v.getUserId()).collect(Collectors.toList());
                    if(!CollectionUtils.isEmpty(collect) || bigStage){
                        competitionApplyDao.updatePlay(playing.getId(), 0, collect);
                    }
                } catch (Exception e) {
                    LogUtil.e("competition|clearing|updatePlay|error|", e);
                }
            }

        }
    }
}


