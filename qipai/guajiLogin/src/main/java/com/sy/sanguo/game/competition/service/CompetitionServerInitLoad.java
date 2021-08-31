package com.sy.sanguo.game.competition.service;

import com.sy.sanguo.game.competition.dao.CompetitionPlayingConfigDao;
import com.sy.sanguo.game.competition.dao.CompetitionPlayingDao;
import com.sy.sanguo.game.competition.dao.CompetitionScoreTotalDao;
import com.sy.sanguo.game.competition.job.CompetitionJobSchedulerThreadPool;
import com.sy.sanguo.game.competition.util.LockSharing;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * 比赛场初始化数据
 *
 * @author Guang.OuYang
 * @date 2020/5/25-11:40
 */
@Service
public class CompetitionServerInitLoad {

	@Autowired
	private CompetitionPushService competitionPushService;

	@Autowired
	private CompetitionGeneratePlayingService competitionGeneratePlayingService;

	@Autowired
	private CompetitionPlayingService competitionPlayingService;

	@Autowired
	private CompetitionJobSchedulerThreadPool competitionJobScheduler;

	@Autowired
	private CompetitionPlayingConfigDao competitionPlayingConfigDao;

	@Autowired
	private CompetitionPlayingDao competitionPlayingDao;

	@Autowired
	private CompetitionScoreTotalDao competitionScoreTotalDao;

	@EventListener(ContextRefreshedEvent.class)
	public void init() {
		try {

			LogUtil.i("competition|init|start");
			// 重启服后,分片格式化
			LockSharing.releaseLock(competitionPlayingConfigDao);
			LockSharing.releaseLock(competitionPlayingDao);

			//初始化赛场匹配调度器
			competitionJobScheduler.initStart();

			//赛场人数推送
			competitionPushService.initPushMsg();

			//下一场生成检测
			competitionGeneratePlayingService.findAndGeneratePlaying();

			//检查正在匹配中的赛事,上一次重启时赛事被中断
			competitionPlayingService.matchStartCheckMatch();

			LogUtil.i("competition|init|end");
		} catch (Exception e) {
			LogUtil.e("Competition|server|init|error",e);
		}
	}
}
