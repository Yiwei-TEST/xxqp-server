package com.sy.sanguo.game.competition.job;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.game.competition.service.CompetitionGeneratePlayingService;
import com.sy.sanguo.game.competition.service.CompetitionPlayingService;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class CompetitionJobSchedulerThreadPool {
	//匹配调度休眠时间点
	public long matchStartSleepTime = 0;
	//匹配调度需要休眠时间
	public long matchStartSleepWaitTime = 0;

	//比赛配置刷新时间
	public long configClearTime = 5 * 60 * 1000;
	public long configClearTimeCurrentTime = System.currentTimeMillis();

	//赛场主逻辑调度器
	private final ScheduledExecutorService playing_scheduler;

	//人数推送调度器
	public final ScheduledExecutorService push_scheduler;

	//主需要异步处理的线程池
	private final ScheduledExecutorService function_thread_pool;

	private final ScheduledExecutorService clearing_main_pool;
	private final ScheduledExecutorService match_main_pool;
//	private final ScheduledExecutorService total_rank_refresh_pool;

	@Autowired
	public CompetitionJobSchedulerThreadPool(
			//主流程线程池
			@Value("${competitionPlayingThreadPool:2}") int competitionPlayingThreadPool,
			//推送线程池
			@Value("${competitionPushThreadPool:1}") int competitionPushThreadPool,
			//异步处理事件线程池
			@Value("${competitionNSyncThreadPool:16}") int competitionNSyncThreadPool,
			//清算主业务队列线程池
			@Value("${clearingMainNSyncThreadPool:8}") int clearingMainNSyncThreadPool,
			//匹配业务队列线程池
			@Value("${matchNSyncThreadPool:6}") int matchNSyncThreadPool
			//更新榜单线程池
//			@Value("${totalRankNSyncThreadPool:1}") int totalRankNSyncThreadPool
	) {
		playing_scheduler = Executors.newScheduledThreadPool(competitionPlayingThreadPool, new ThreadFactoryReWrite("CompetitionJobScheduler_playing_main_thread"));
		push_scheduler = Executors.newScheduledThreadPool(competitionPushThreadPool, new ThreadFactoryReWrite("CompetitionJobScheduler_push_main_thread"));
		function_thread_pool = Executors.newScheduledThreadPool(competitionNSyncThreadPool, new ThreadFactoryReWrite("Competition_n_sync_thread"));
		clearing_main_pool = Executors.newScheduledThreadPool(clearingMainNSyncThreadPool, new ThreadFactoryReWrite("Competition_clearing_main_pool_thread"));
		match_main_pool = Executors.newScheduledThreadPool(matchNSyncThreadPool, new ThreadFactoryReWrite("Competition_match_main_pool_thread"));
//		total_rank_refresh_pool = Executors.newScheduledThreadPool(totalRankNSyncThreadPool, new ThreadFactoryReWrite("Competition_total_rank_refresh_pool_thread"));
	}

	public ScheduledExecutorService getClearingMainPool(){return clearing_main_pool;}
//	public ScheduledExecutorService	getTotalRankRefreshPool(){return total_rank_refresh_pool;}

	public ScheduledExecutorService getMatchMainPool(){return match_main_pool;}

	@Value("${competitionSchedulerTimerDelay:1000}")
	private long competitionSchedulerTimerDelay;

	@Autowired
	private CompetitionPlayingService competitionPlayingService;

	@Autowired
	private CompetitionGeneratePlayingService competitionGeneratePlayingService;

	public void initStart() {
		playing_scheduler.scheduleWithFixedDelay(this::flow, 0, competitionSchedulerTimerDelay, TimeUnit.MILLISECONDS);
	}


	/**
	 *@description 调度器内容
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/26
	 */
	public void flow() {
		try {
			//入口开放检测
			//淘汰赛事检测,到开赛时间人数不满足开赛人数
//			competitionPlayingService.openApplyEnter();

			//开始匹配检测
			//报名赛可以使用报名驱动
			//定时赛需要定时器协助开赛
			//匹配检测到空列表时会大跨度休眠调度器以节省数据库性能
			if (matchStartSleepTime == 0 || (System.currentTimeMillis() - matchStartSleepTime) >= matchStartSleepWaitTime) {
				//检查开赛
				//检查无效赛事
				matchStartSleepWaitTime = competitionPlayingService.matchStartCheck();
				matchStartSleepTime = matchStartSleepWaitTime > 0 ? System.currentTimeMillis() : 0;

				if (matchStartSleepWaitTime > 0){
					LogUtil.i("Competition|flow|findNullTimerCompetition|wait|" + matchStartSleepWaitTime);
				}
			}

			//比赛配置的刷新时间
			if ((System.currentTimeMillis() - configClearTimeCurrentTime) >= configClearTime) {
				competitionGeneratePlayingService.clearConfigCache();
				configClearTimeCurrentTime = System.currentTimeMillis();
			}

		}catch (Exception e) {
			GameBackLogger.SYS_LOG.error(e);

		}
	}

	/**
	 *@description 异步延迟总线
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/23
	 */
	public void nSynDelayBus(Runnable r, long mills) {
		function_thread_pool.schedule(r, mills, TimeUnit.MILLISECONDS);
	}

	/**
	 *@description 异步处理总线
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/23
	 */
	public void nSynBus(Runnable r) {
		function_thread_pool.submit(r);
	}

	/**
	 *@description 异步处理总线
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/23
	 */
	public void nSynClearingBus(Runnable r) {
		if (r != null)
			clearing_main_pool.submit(r);
	}

	/**
	 *@description 异步延迟总线
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/23
	 */
	public void nSynClearingDelayBus(Runnable r, long mills) {
		clearing_main_pool.schedule(r, mills, TimeUnit.MILLISECONDS);
	}


//	/**
//	 *@description 同步总线处理总线
//	 *@param
//	 *@return
//	 *@author Guang.OuYang
//	 *@date 2020/6/24
//	 */
//	public <T> T synBus(Runnable r) throws Exception {
//		return (T) function_thread_pool.submit(r).get();
//	}


	@PreDestroy
	public void shutdown() {
		LogUtil.i("competition|scheduler|shutdown|playing_scheduler|start");
		shutdownAndAwaitTermination(playing_scheduler);
		LogUtil.i("competition|scheduler|shutdown|playing_scheduler|end");

		LogUtil.i("competition|scheduler|shutdown|push_scheduler|start");
		shutdownAndAwaitTermination(push_scheduler);
		LogUtil.i("competition|scheduler|shutdown|push_scheduler|end");

		LogUtil.i("competition|scheduler|shutdown|push_scheduler|start");
		shutdownAndAwaitTermination(function_thread_pool);
		LogUtil.i("competition|scheduler|shutdown|push_scheduler|end");

		LogUtil.i("competition|scheduler|shutdown|push_scheduler|start");
		shutdownAndAwaitTermination(clearing_main_pool);
		LogUtil.i("competition|scheduler|shutdown|push_scheduler|end");

		LogUtil.i("competition|scheduler|shutdown|push_scheduler|start");
		shutdownAndAwaitTermination(match_main_pool);
		LogUtil.i("competition|scheduler|shutdown|push_scheduler|end");
	}


	/**
	 *@description
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/24
	 */
	void shutdownAndAwaitTermination(ExecutorService pool) {
		if (!pool.isShutdown()) {
			pool.shutdown(); // Disable new tasks from being submitted
			try {
				// Wait a while for existing tasks to terminate
				if (!pool.awaitTermination(60 * 5, TimeUnit.SECONDS)) {
					pool.shutdownNow(); // Cancel currently executing tasks
					// Wait a while for tasks to respond to being cancelled
					if (!pool.awaitTermination(60 * 2, TimeUnit.SECONDS))
						LogUtil.e("competition|scheduler|shutdownAndAwaitTermination Pool did not terminate");
				}
			} catch (InterruptedException ie) {
				// (Re-)Cancel if current thread also interrupted
				pool.shutdownNow();
				// Preserve interrupt status
				Thread.currentThread().interrupt();
			}
		}
	}

}