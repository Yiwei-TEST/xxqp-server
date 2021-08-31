package com.sy.sanguo.game.competition.service;

import com.sy.sanguo.game.competition.dao.CompetitionPlayingDao;
import com.sy.sanguo.game.competition.model.db.CompetitionPlayingConfigDB;
import com.sy.sanguo.game.competition.model.db.CompetitionPlayingDB;
import com.sy.sanguo.game.competition.model.param.CompetitionRefreshQueueModel;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息推送
 *
 * @author Guang.OuYang
 * @date 2020/5/20-16:29
 */
@Service
public class CompetitionMatchService {

    private final static Byte Z_BYTE_CHAR = Byte.valueOf("0");
    private final static ConcurrentHashMap<CompetitionRefreshQueueModel, Byte> MATCH_SCHEDULE_MSG = new ConcurrentHashMap<>();

    @Value("${competition.push.delay:1000}")
    private int pushDelay;

    @Autowired
    private CompetitionPlayingService competitionPlayingService;

    @Autowired
    private CompetitionGeneratePlayingService competitionGeneratePlayingService;

    @Autowired
    private CompetitionPlayingDao competitionPlayingDao;

    /**
     * @param
     * @return
     * @description 推送, 当前人数
     * @author Guang.OuYang
     * @date 2020/5/25
     */
    public void addMatchQueue(CompetitionRefreshQueueModel queueModel) {
        MATCH_SCHEDULE_MSG.put(queueModel, Z_BYTE_CHAR);
    }

    public CompetitionRefreshQueueModel pollMatchMsg() {
        Iterator<Entry<CompetitionRefreshQueueModel, Byte>> iterator = MATCH_SCHEDULE_MSG.entrySet().iterator();
        if (iterator.hasNext()) {
            Entry<CompetitionRefreshQueueModel, Byte> next = iterator.next();
            iterator.remove();
            return next.getKey();
        }
        return null;
    }

    public void repeatedMatch() {
        if (MATCH_SCHEDULE_MSG.isEmpty()) {
            return;
        }

        try {
            long t = System.currentTimeMillis();
            CompetitionRefreshQueueModel arg = pollMatchMsg();

            LogUtil.i("competition|repeatedMatchQueue|start|" + arg.getPlayingId() + "|" + arg.isNeedMatch() + "|" + arg.getUpRoundResolve().getCurStep() + "|" + arg.getUpRoundResolve().getCurRound() + "|" + arg);

            if (!arg.isNeedMatch()) {
                return;
            }

            CompetitionPlayingDB playing = competitionPlayingService.getPlaying(arg.getPlayingId()).get();

            CompetitionPlayingConfigDB playingConfig = competitionGeneratePlayingService.getPlayingConfig(playing.getPlayingConfigId()).get();

            CompetitionPlayingService.CompetitionClearingRiseInRankMode upRoundResolve = arg.getUpRoundResolve();
            CompetitionPlayingService.CompetitionClearingRiseInRankMode curRoundResolve = arg.getCurRoundResolve();


            int curTotalHuman = competitionPlayingDao.queryAliveByPlayingId(playing.getId());

            int curStep = upRoundResolve.getCurStep();
            int curRound = upRoundResolve.getCurRound();

            int curRate = 1;

            boolean noExistsTable = arg.isNoExistsTable();

            //使用这一轮所有空闲的玩家进行匹配
            boolean matchRefreshData = upRoundResolve.getRoundLowScore() > 0 && (!competitionPlayingService.isBreakWeedOutStep(curRoundResolve));

            boolean theNextStage = arg.isTheNextStage();
            boolean bigStage = arg.isBigStage();
            boolean nextStep = arg.isNextStep();

            //重复匹配
            competitionPlayingService.waitAndRepeatedMatch(playing,
                    playingConfig,
                    upRoundResolve,
                    curRoundResolve,
                    null,
                    curTotalHuman,
                    theNextStage,
                    curRate,
                    bigStage,
                    nextStep,
                    curStep,
                    curRound,
                    0, matchRefreshData, noExistsTable);

            LogUtil.i("competition|repeatedMatchQueue|end|" + arg.getPlayingId() + "|" + arg.isNeedMatch() + "|" + arg.getUpRoundResolve().getCurStep() + "|" + arg.getUpRoundResolve().getCurRound() + "|" + (System.currentTimeMillis() - t) + "ms");

        } catch (Exception e) {
           LogUtil.e("competition|repeatedMatchQueue|error",e);
        }
    }

}


