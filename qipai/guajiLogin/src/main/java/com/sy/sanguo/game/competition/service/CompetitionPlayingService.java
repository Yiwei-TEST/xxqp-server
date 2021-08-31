package com.sy.sanguo.game.competition.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.StringUtil;
import com.sy.sanguo.common.util.lenovo.DateUtil;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.ServerConfig;
import com.sy.sanguo.game.competition.dao.*;
import com.sy.sanguo.game.competition.exception.MessageAssert;
import com.sy.sanguo.game.competition.exception.MessageExceptionParam;
import com.sy.sanguo.game.competition.job.CompetitionJobSchedulerThreadPool;
import com.sy.sanguo.game.competition.model.db.*;
import com.sy.sanguo.game.competition.model.enums.CompetitionApplyStatusEnum;
import com.sy.sanguo.game.competition.model.enums.CompetitionPlayingStatusEnum;
import com.sy.sanguo.game.competition.model.enums.CompetitionPlayingTypeEnum;
import com.sy.sanguo.game.competition.model.enums.CompetitionScoreDetailStatusEnum;
import com.sy.sanguo.game.competition.model.param.*;
import com.sy.sanguo.game.competition.service.CompetitionPushService.CompetitionPushModel;
import com.sy.sanguo.game.competition.util.AttributeKV;
import com.sy.sanguo.game.competition.util.LockSharing;
import com.sy.sanguo.game.competition.util.ParamCheck;
import com.sy.sanguo.game.dao.ServerDaoImpl;
import com.sy.sanguo.game.dao.UserDao;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import com.sy599.sanguo.util.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 主赛场管理
 *
 * 1.赛事状态变更为可用->报名
 * 2.时间\人满\触发比赛匹配
 * 3.变更报名->匹配状态
 * 4.批量匹配,推送客户端到开赛等待界面->客户端获取GameServerID,RoomId -> 进入房间(分入不同的GameServer节点)
 *   1)机器人由服务器推送至赛事创建时绑定的GameServerID -> 进入房间
 * 5.结算
 * 6.结算各个房间由一个或多个不同的loginServer进行结算
 * 7.最终结算节点收到结算请求->验证重复匹配or大结算
 *   1)重复匹配: 由服务器统一把玩家拉入赛事创建时绑定的GameServerID(目前由于赛事机制N进9,预计统一拉入单GameServer的人数不会超过12人)  !!!这里如果赛事绑定单点过于单一可能会引发该服性能问题
 * 8.结束
 *
 * @author Guang.OuYang
 * @date 2020/5/20-18:07
 */
@Service
public class CompetitionPlayingService {
	//结算模型
	protected static final Map<Integer, CompetitionPlayingService> CHILDREN_CLEAING_ACTOR = new HashMap<>();
	private CompetitionPlayingService getClearingActor(int type) {
		return CHILDREN_CLEAING_ACTOR.get(type);
	}
	//由服务器发起的匹配请求,要求分发到多服
	@Value("${competition.open.mutil.server.match:true}")
	protected boolean competitionOpenMutilServerMatch;
	//清算后匹配等待时间
	@Value("${competition.clearing.wait.show.time.ms:3000}")
	protected int clearingWaitShowTime;
	//赛事轮空休眠时间
	@Value("${competition.find.null.timer.time.ms:30000}")
	protected int findNullTimerWaitTimeMs;
	//定时赛重复匹配人数等待时间
	@Value("${competition.timer.match.table.wait.ms:5000}")
	protected int timerMatchTableWaitMs;
	@Value("${competition.isDebug:false}")
	protected boolean isDebug;
	@Value("${competition.open.robot:0}")
	protected boolean openRobot;
	public boolean isDebug() {
		return isDebug;
	}
	@Autowired
	protected CompetitionPlayingDao<CompetitionPlayingDB> competitionPlayingDao;
	@Autowired
	protected CompetitionApplyDao<CompetitionApplyDB> competitionApplyDao;
	@Autowired
	protected CompetitionRoomDao competitionRoomDao;
	@Autowired
	protected CompetitionRoomUserDao competitionRoomUserDao;
	@Autowired
	protected CompetitionClearingAwardDao competitionClearingAwardDao;
	@Autowired
	protected CompetitionScoreDetailDao<CompetitionScoreDetailDB> competitionScoreDetailDao;
	@Autowired
	protected CompetitionScoreTotalDao<CompetitionScoreTotalDB> competitionScoreTotalDao;
	@Autowired
	protected CompetitionGeneratePlayingService competitionGeneratePlayingService;
	@Autowired
	protected CompetitionJobSchedulerThreadPool competitionJobSchedulerThreadPool;
	@Autowired
	protected CompetitionPushService competitionPushService;
	@Autowired
	protected CompetitionRankService competitionRankService;
	@Autowired
	protected CompetitionApplyService competitionApplyService;
	@Autowired
	protected CompetitionRunningHorseLightService competitionRunningHorseLightService;
	@Autowired
	private CompetitionRankRefreshService competitionRankRefreshService;


	//单点开始匹配锁
	protected static int MATCH_START_LOCK = 0;

	/**
	 *@description
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/7
	 */
	public PlayingList playingList(CompetitionPlayingParam build) {
		return PlayingList.builder()
				.disableSign(competitionApplyService.disableSignCount(build.getUserId()))
				.argList(applyPlayingList(build))
				.bottomTitle(getPlayingButtomTitleList())
				.build();
	}

	/**
	 *@description 底部标题
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/8/21
	 */
	public List<List<String>> getPlayingButtomTitleList() {
		try {
			List<CompetitionRunningHorseLightDB> bottomTitleLight = competitionRunningHorseLightService.findBottomTitleLight();
			if (!CollectionUtils.isEmpty(bottomTitleLight)) {
				List<List<String>> title = new ArrayList<>();
				for (int i= bottomTitleLight.size()-1;i>=0;i--){
					CompetitionRunningHorseLightDB next = bottomTitleLight.get(i);
					String[] titleArg = next.getContent().split(",");
					switch (titleArg[1]) {
						case "2020QixiFestival_Winner":    //2020年七夕节
							title.add(Arrays.asList(titleArg[1], titleArg[2], titleArg[3]));
							break;
						default:
							break;
					}
				}
				return title;
			}
			return null;
		} catch (Exception e) {
			LogUtil.e("competition|getPlayingButtomTitleList|error", e);
		}

		return null;
	}

	/**
	 *@description 退出或进入报名界面
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/8/4
	 */
	public boolean signShowRefresh(long userId, long playingId, int signShow) {
		Optional<CompetitionPlayingDB> playingDBOptional = getPlaying(playingId);

		if (playingDBOptional.isPresent() && (playingDBOptional.get().getStatus() == CompetitionPlayingStatusEnum.APPLYING || playingDBOptional.get().getStatus() == CompetitionPlayingStatusEnum.MATCHING)) {
			return competitionApplyDao.updateSignShow(playingId, signShow, userId) > 0;
		}

		return false;
	}

	/**
	 *@description  找到玩家当前参与还未开赛的赛事 ,仅处理报名赛
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/10
	 */
	public Optional<CompetitionPlayingDB> serachPlayerSignPlaying(long userId) {
//		List<CompetitionPlayingDB> argList = playingList(CompetitionPlayingParam.builder()
//				.userId(userId)
//				.status(CompetitionPlayingStatusEnum.APPLYING)
//				.playingType(CompetitionPlayingTypeEnum.APPLY_PLAYING)
//				.build()).getArgList();
		List<CompetitionPlayingDB> argList = new ArrayList<>();
		CompetitionPlayingDB competitionPlayingDB = signShowGet(userId);
		if (competitionPlayingDB != null) {
//			ArrayList<CompetitionPlayingDB> competitionPlayingDBS = new ArrayList<CompetitionPlayingDB>(Arrays.asList());
//			competitionPlayingDBS.addAll(argList);
//			argList = competitionPlayingDBS;
			argList.add(competitionPlayingDB);
		}

		if (!CollectionUtils.isEmpty(argList)) {
			return argList.stream().filter(v -> v.isSignStatus()).findAny();
		}
		return Optional.empty();
	}

	/**
	 *@description 正在报名界面
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/8/4
	 */
	public CompetitionPlayingDB signShowGet(long userId) {
		Optional<CompetitionApplyDB> optional = competitionApplyDao.queryForSingle(CompetitionApplyDB.builder().userId(userId).status(CompetitionApplyStatusEnum.NORMAL).signShow(1).build());
		if(optional.isPresent()) {
			Optional<CompetitionPlayingDB> playingDBOptional = getPlaying(optional.get().getPlayingId());
			if(playingDBOptional.isPresent()){
				if(playingDBOptional.get().getStatus() == CompetitionPlayingStatusEnum.APPLYING){

					List<CompetitionPlayingDB> argList = playingList(CompetitionPlayingParam.builder()
							.titleType(playingDBOptional.get().getTitleType())
							.playingType(playingDBOptional.get().getType())
							.category(playingDBOptional.get().getCategory())
							.entrance(playingDBOptional.get().getEntrance()).userId(userId).build()).getArgList();

					return CollectionUtils.isEmpty(argList) || argList.get(0).getId() != playingDBOptional.get().getId() ? null : argList.get(0);

				}
			}
		}
		return null;
	}

	/**
	 *@description 获取当前可以报名的赛事列表
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/22
	 */
	public List<CompetitionPlayingDB> applyPlayingList(CompetitionPlayingParam competitionPlayingParam) {
		//针对单个列表
		if (competitionPlayingParam.getPlayingId() != null && competitionPlayingParam.getPlayingId() > 0) {
			return Arrays.asList(setAddField(getPlaying(competitionPlayingParam.getPlayingId()).orElse(null)));
		}

		List<CompetitionPlayingDB> resList = competitionPlayingDao.queryByEnter(CompetitionPlayingDB.builder()
				.id(Optional.ofNullable(competitionPlayingParam.getPlayingId()).orElse(0l))
				.titleType(competitionPlayingParam.getTitleType())
				.type(competitionPlayingParam.getPlayingType())
				.category(competitionPlayingParam.getCategory())
				.entrance(competitionPlayingParam.getEntrance())
				.applyBefore(ParamCheck.check(competitionPlayingParam.getApplyBefore(), TimeUtil::parseTimeInDate))
				.matchBefore(ParamCheck.check(competitionPlayingParam.getMatchBefore(), TimeUtil::parseTimeInDate))
				.status(ParamCheck.defaultVal(competitionPlayingParam.getStatus(), CompetitionPlayingStatusEnum.APPLYING))
				.deleteTime(new Date())
				.userId(ParamCheck.defaultVal(competitionPlayingParam.getUserId(), 0l))
				.build()).orElse(null);

		List<CompetitionPlayingDB> res = new ArrayList<>();
		//列表内加入额外临时参数
		if(!CollectionUtils.isEmpty(resList)) {
			Iterator<CompetitionPlayingDB> iterator = resList.iterator();
			while (iterator.hasNext()) {
				CompetitionPlayingDB playing = iterator.next();
				Optional<CompetitionPlayingConfigDB> playingConfig = competitionGeneratePlayingService.getPlayingConfig(playing.getPlayingConfigId());
				if (playingConfig != null && playingConfig.isPresent() && playingConfig.get().getDeleteTime() == null) {
					res.add(setAddField(playing));
				}
			}

			resList = null;
		}
		return res;
	}

	public int matchStartCheck() {
		return matchStartCheck(CompetitionPlayingStatusEnum.APPLYING, true);
	}

	public int matchStartCheckMatch() {
		return matchStartCheck(CompetitionPlayingStatusEnum.MATCHING, false);
	}


	/**
	 *@description 匹配检查
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/22
	 */
	public int matchStartCheck(int type, boolean onlyTimerPlaying) {
		//获取锁失败,就不执行了
		if (!matchLockChangeStatus(0, 1)) {
			return 0;
		}


		try {
			long time = System.currentTimeMillis();

			CompetitionPlayingDB build = CompetitionPlayingDB.builder()
					.status(type)
					.build();

			if (onlyTimerPlaying) {
				//仅处理定时赛
				build.setType(CompetitionPlayingTypeEnum.TIMER_PLAYING);
			}

			Optional<List<CompetitionPlayingDB>> optionalPlayings = competitionPlayingDao.queryForList(build);

			if (!optionalPlayings.isPresent() || CollectionUtils.isEmpty(optionalPlayings.get())) {
				return findNullTimerWaitTimeMs;
			}

			if (optionalPlayings.isPresent() && !CollectionUtils.isEmpty(optionalPlayings.get())) {
				GameBackLogger.SYS_LOG.debug(String.format("匹配开始: 当前有%s个定时赛事正在报名", optionalPlayings.get().size()));
				int realSize = 0;

				for (CompetitionPlayingDB playing : optionalPlayings.get()) {
					if (competitionGeneratePlayingService.getPlayingConfig(playing.getPlayingConfigId()) != null && competitionGeneratePlayingService.getPlayingConfig(playing.getPlayingConfigId()).get().getDeleteTime() == null) {
						try {
							//异步开赛通知
							competitionJobSchedulerThreadPool.nSynBus(() -> this.notifyAllPlay(playing));

							//赛事状态检查
							if (playingStartCheck(playing, type == CompetitionPlayingStatusEnum.MATCHING)) {
								realSize++;
							}
						} catch (Exception e) {
							LogUtil.e("competition|playing|matchStartCheck|foreach|error|" + e);
						}
					}
				}

				GameBackLogger.SYS_LOG.debug(String.format("匹配结束: 实际匹配了%s个定时开赛赛事 %sms", realSize, (System.currentTimeMillis() - time)));
			}
		} catch (Exception e) {
			LogUtil.e("competition|playing|matchStartCheck|error|" + e);
			throw e;
		} finally {
			//解锁
			matchLockChangeStatus(1, 0);
		}
		return 0;
	}

	/**
	 *@description XXX 动态配置相关,可修改的配置都挪到这里
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/8/25
	 */
	public CompetitionPlayingDB setAddField(CompetitionPlayingDB v) {
		CompetitionPlayingConfigDB competitionPlayingConfigDB = competitionGeneratePlayingService.getPlayingConfig(v.getPlayingConfigId()).get();
		if (v != null && competitionPlayingConfigDB != null) {
			v.setAwards(competitionPlayingConfigDB.getAwards());
			v.setRoomConfigId(resolveRoomConfigId(v.getCurStep(), v.getCurRound(), competitionPlayingConfigDB));
			v.setStepLength(competitionPlayingConfigDB.getStepRoundDesc().split(";").length);
			if (competitionPlayingConfigDB.getExtModel() != null && competitionPlayingConfigDB.getExtModelPojo().getSecondWeedOut() != null &&( v.getCurStep() == 1 && CompetitionGeneratePlayingService.isTimerPlaying(v::getType))) {
				String[] split = competitionPlayingConfigDB.getExtModelPojo().getSecondWeedOut().split(";");
				v.setSecondWeedOut(Arrays.stream(split).filter(v1 -> Integer.valueOf(v1.split("_")[0]) >= v.getCurHuman()).findAny().orElse(null));
			}
		}
		return v;
	}

	/**
	 *@description 赛前推送
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/16
	 */
	public void notifyAllPlay(CompetitionPlayingDB playing) {
		//赛前推送
		Optional<CompetitionPlayingConfigDB> playingConfig = competitionGeneratePlayingService.getPlayingConfig(playing.getPlayingConfigId());

		//全服跑马灯
		CompetitionPlayingConfigExtModel competitionPlayingConfigExtModel = playingConfig.get().getExtModelPojo();

		//赛前推送
		try {
			String beforCreate="5";
			String startBeforeNotifyExt = StringUtils.isBlank(playingConfig.get().getStartBeforeNotifyExt())
					|| !Arrays.stream(StringUtils.split(playingConfig.get().getStartBeforeNotifyExt(), ",")).anyMatch(v -> v.equalsIgnoreCase(beforCreate)) ?
					beforCreate + "," + Optional.ofNullable(playingConfig.get().getStartBeforeNotifyExt()).orElse("")
					: playingConfig.get().getStartBeforeNotifyExt();
            if (playingConfig.isPresent() && !StringUtils.isBlank(startBeforeNotifyExt) && playing.getMatchBefore() != null) {
				String[] mins = startBeforeNotifyExt.split(",");

				long beforeMin = Math.max(1, (int) Math.ceil((playing.getMatchBefore().getTime() - System.currentTimeMillis()) / 1000 / 60.0));    //

				Integer curNotify = Arrays.stream(mins).filter(v -> Long.valueOf(v).equals(Long.valueOf(beforeMin))).map(v -> Integer.valueOf(v)).findAny().orElse(null);

				if (CompetitionGeneratePlayingService.isTimerPlaying(playing::getType) && curNotify != null
						&& (!playing.getBeginPlayingPushStatus().equals(curNotify) || Optional.ofNullable(playing.getBeginPlayingPushStatus()).orElse(0) > curNotify)) {
					List<Long> longs = null;
					try {
						longs = competitionApplyDao.queryForUsersByPlayingId(playing.getId()).get();
						if (!CollectionUtils.isEmpty(longs)) {
							competitionPushService.beginPlayingNotifyPush(longs,
									Arrays.asList(String.format(playingConfig.get().getBeforeXMinPushTitle().replace("{p}", "%s").replace("(p)", "%s"), (int) beforeMin),
											JSONObject.toJSONString(setAddField(playing))));
						}

						//赛前推送
						competitionPlayingDao.update(CompetitionPlayingDB.builder().id(playing.getId()).beginPlayingPushStatus(curNotify).build());
					} catch (Exception e) {
						LogUtil.e("competition|notifyAllPlay|BeforePush|error",e);
					}

					try {
						LogUtil.i("competition|notifyAllPlay|" + playing.getId() + "|" + curNotify + "|" + beforeMin + "|" + longs.size());
						//赛前十分钟, 开赛人数必须大于0, 才开始创建房间
						if(curNotify==Integer.valueOf(beforCreate) && playing.getCurHuman() > 0 && competitionRoomDao.queryNotStartRoomByCurCount(playing.getId()) == null){
							//第一次开放报名时推送时创建房间
							nsynCreateInitTable(playing);
						}
					} catch (Exception e) {
						LogUtil.e("competition|notifyAllPlay|BeforePush|error",e);
					}
				}

//				try {
//					//跑马灯:赛前1分钟
//					if (beforeMin <= 1 && competitionPlayingConfigExtModel != null && (playing.getBeginPlayingRHLPushStatus() == null || playing.getBeginPlayingRHLPushStatus().indexOf("m1") <= -1)) {
//						CompetitionRunningHorseLightDB runningHorseLightBeforeLastOneMinModel = competitionPlayingConfigExtModel.getRunningHorseLightBeforeLastOneMinModel();
//						if(runningHorseLightBeforeLastOneMinModel != null){
//							competitionRunningHorseLightService.addLight(runningHorseLightBeforeLastOneMinModel.tempArgsFill(playing));
//							competitionPlayingDao.update(CompetitionPlayingDB.builder().id(playing.getId()).beginPlayingRHLPushStatus(playing.getBeginPlayingRHLPushStatus() + "_m1").build());
//						}
//					}
//				} catch (Exception e) {
//					LogUtil.e("competition|notifyAllPlay|RunningHorseLightBeforeLastOneMin|error",e);
//				}

			}
		}  catch (Exception e) {
			LogUtil.e("competition|notifyAllPlay|BeforePush|error",e);
		}

		try {
			if(playing.getMatchBefore()!=null){
				//开放报名跑马灯:软校验, 当前时间等于 赛前n分钟开放报名时触发
				Integer curBeforeMin = Double.valueOf(Math.ceil((playing.getMatchBefore().getTime() - System.currentTimeMillis()) / 1000 / 60)).intValue();
				//触发赛前推送
				if (competitionPlayingConfigExtModel != null && playingConfig.get().getApplyBeforeMin() >= curBeforeMin && (playing.getBeginPlayingRHLPushStatus() == null || playing.getBeginPlayingRHLPushStatus().indexOf("b1") <= -1)) {
					CompetitionRunningHorseLightDB runningHorseLightOpenApplyModel = competitionPlayingConfigExtModel.getRunningHorseLightOpenApplyModel();
					if (runningHorseLightOpenApplyModel != null) {
						competitionRunningHorseLightService.addLight(runningHorseLightOpenApplyModel.tempArgsFill(playing));
					}

					//赛前推送
					competitionPlayingDao.update(CompetitionPlayingDB.builder().id(playing.getId()).beginPlayingRHLPushStatus(playing.getBeginPlayingRHLPushStatus() + "_b1").build());
				}
			}
		} catch (Exception e) {
			LogUtil.e("competition|notifyAllPlay|RunningHorseLightOpenApply|error", e);
		}
	}

	private void nsynCreateInitTable(CompetitionPlayingDB playing) {
		competitionJobSchedulerThreadPool.nSynBus(()-> {
			try {
				List<ServerConfig> serverConfigs = ServerDaoImpl.getInstance().queryAllServer().stream().filter(v -> v.getServerType() == 3).collect(Collectors.toList());
				CompetitionPlayingConfigDB playingConfig = competitionGeneratePlayingService.getPlayingConfig(playing.getPlayingConfigId()).get();
				CompetitionClearingRiseInRankMode resolve = CompetitionClearingRiseInRankMode.resolve(playing, playingConfig, null);
				//房间配置
				Long roomConfigId = resolveRoomConfigId(playing.getCurStep(), playing.getCurRound(), playingConfig);
				Long roomNeedPlayerCount = competitionRoomDao.queryPlayCountByRoomConfigId(roomConfigId);

				//客户端自己获取服务器ID加入游戏,这里仅做开赛推送至指定界面
				CompetitionClearingModelRes clearingModel = CompetitionClearingModelRes.builder()
						.playingTitle(playing.getTitleCode())
						.curStep(playing.getCurStep())
						.curRound(playing.getCurRound())
						.baseScore(resolve.getSrc().getBaseScore())
						.weedOutScore(resolve.getSrc().getRoundLowScore())
						.curStepTotalHuman(0)
						.totalHuman(playing.getCurHuman())
						.playingId(playing.getId())
						.roomConfigId(roomConfigId)
						.loginCallBackUrl(playingConfig.getLoginCallBackUrl())
						.loginCallBackClearing(playingConfig.getLoginCallBackClearing())
						.loginCallBackCancel(playingConfig.getLoginCallBackCancel())
						.currentMills(playing.getMatchBefore().getTime())
						.secondWeedOut(playing.getSecondWeedOut())
						.playingOpenTime(playing.getMatchBefore().getTime())
						.firstMatch(true)
						.build();

				//创房
				Iterator<ServerConfig> iterator = serverConfigs.iterator();
				while (iterator.hasNext()) {
					ServerConfig next = iterator.next();
					if (competitionRoomUserDao.queryByUserRoomId(playing.getId()) <= 0) {
						competitionJobSchedulerThreadPool.nSynBus(() -> {
							//赛前5分钟预估人数当前报名总人数的3倍
							int count = (int) ((Math.min(playing.getCurHuman() * 3, playing.getMaxHuman()) / roomNeedPlayerCount) + (Math.max(10, playing.getCurHuman() / 2 / 10)));
							competitionPushService.playingBatchCreateRoom(count, next.getId(), clearingModel.copy());
							LogUtil.i("competition|initCreateTable|" + count);
						});
					}
				}
			} catch (Exception e) {
			}
		});
	}

	/**
	 *@description 赛事开赛检查,状态校验
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/8
	 */
	public boolean playingStartCheck(CompetitionPlayingDB playing, boolean forceCheck) {
		//状态变更 报名->匹配中
		if (matchStartCheck(playing) || forceCheck) {
//			LOGGER.info("Competition|playing|startGame|" + playing.getId() + "|" + playing.getStatus() + "|" + playing.getCurHuman() + "|" + playing.getBeginHuman()+"|"+forceCheck);

			setAddField(playing);

			CompetitionPlayingConfigDB playingConfig = competitionGeneratePlayingService.getPlayingConfig(playing.getPlayingConfigId()).get();

			CompetitionClearingRiseInRankMode resolve = CompetitionClearingRiseInRankMode.resolve(playing, playingConfig, null);

			long roomConfigId = resolveRoomConfigId(playing.getCurStep(), playing.getCurRound(), playingConfig);

			try {
				Optional<List<Long>> allSignPeople = competitionApplyDao.queryForUsersByPlayingId(playing.getId());
				//推送客户端进入等待界面
				competitionPushService.beginPlayingPush(CompetitionPushModel.builder().playingId(playing.getId()).userIds(allSignPeople.get()).build(), setAddField(playing));
				//延迟3s后做处理, 这里做离线拉入处理延迟是因为, 可能客户端在开赛后躲开了该逻辑后掉线, 这里给定delay给足够的缓冲时间
				competitionJobSchedulerThreadPool.nSynDelayBus(() -> {
					try {
						//处于报名界面的玩家
						List<Long> usersOptional = competitionApplyDao.queryForUsersByPlayingId(playing.getId(), 1).orElse(Collections.emptyList());

						//流局
						//当前空闲人数不足
						if (usersOptional.size() < playing.getBeginHuman()) {
							competitionPlayingDao.updateStatus(playing.getId(), CompetitionPlayingStatusEnum.MATCHING, CompetitionPlayingStatusEnum.CASTOFF, false, playing.getType());
							caseOffCheckIn(playing);
							return;
						}

						//异步退赛
						competitionJobSchedulerThreadPool.nSynBus(()-> {
                            try {
                                List<Long> needCancel = Collections.emptyList();
                                if (!CollectionUtils.isEmpty(usersOptional)) {
                                    //没有牌桌的玩家
                                    Map<Long, List<RegInfo>> collect = UserDao.getInstance().getUserForListByTable(usersOptional, true).stream().collect(Collectors.groupingBy(RegInfo::getUserId));

                                    //需要退赛的部分玩家
                                    needCancel = competitionApplyDao.queryForUsersByPlayingId(playing.getId()).orElse(Collections.emptyList());

                                    usersOptional.removeIf(v -> v > 0 && !collect.containsKey(v));

                                    //需要退赛的
                                    needCancel.removeAll(usersOptional);

                                    LogUtil.i("Competition|playing|startGame|" + playing.getId() + "|" + playing.getStatus() + "|" + playing.getCurHuman()
                                            + "|" + playing.getBeginHuman() + "|" + forceCheck + "|startSize|" + usersOptional.size());
                                }
                                //退赛处理
                                batchCancelPlaying(playing, Optional.ofNullable(needCancel));
                            } catch (SQLException e) {
                                LogUtil.e("competition|systemCancelPlaying|error", e);
                            }
                        });

						//比赛榜单,初始数据
						try {
							String userIds = usersOptional.stream().map(String::valueOf).collect(Collectors.joining(","));
							//比赛榜单,初始数据
							competitionScoreDetailDao.batchInsertScoreDetail(playing.getId(), playing.getInitScore(), userIds);
							competitionScoreTotalDao.batchInsertScoreDetail(playing.getId(), playing.getInitScore(), userIds);
							//更新存活
							competitionPlayingDao.updateAliveHuman(CompetitionPlayingDB.builder().id(playing.getId()).aliveHuman(usersOptional.size()).build());
						} catch (Exception e) {
							LogUtil.e("playingStartCheckError",e);
						}

						//客户端自己获取服务器ID加入游戏,这里仅做开赛推送至指定界面
						CompetitionClearingModelRes clearingModel = CompetitionClearingModelRes.builder()
								.playingTitle(playing.getTitleCode())
								.curStep(playing.getCurStep())
								.curRound(playing.getCurRound())
								.baseScore(resolve.getSrc().getBaseScore())
								.weedOutScore(resolve.getSrc().getRoundLowScore())
								.curStepTotalHuman(usersOptional.size())
								.totalHuman(playing.getCurHuman())
								.playingId(playing.getId())
								.roomConfigId(roomConfigId)
								.loginCallBackUrl(playingConfig.getLoginCallBackUrl())
								.loginCallBackClearing(playingConfig.getLoginCallBackClearing())
								.loginCallBackCancel(playingConfig.getLoginCallBackCancel())
								.currentMills(System.currentTimeMillis())
								.secondWeedOut(playing.getSecondWeedOut())
								.playingOpenTime(System.currentTimeMillis())
								.firstMatch(true)
								.build();


						boolean breakFlag = true;
						int retry = 10;
						do {
							retry--;
                            try {
                                HashMap<String, String> serverAndCompetitionRoomId = competitionPushService.getServerAndCompetitionRoomId(-1l, playing.getCategory(), playing.getId(), roomConfigId, playing.getBindServerId());
                                String serverId = serverAndCompetitionRoomId.get("serverId");
                                breakFlag = !match(playing, playingConfig, Optional.ofNullable(usersOptional), clearingModel, serverId, roomConfigId, competitionOpenMutilServerMatch);
                                LogUtil.i("Competition|playing|startGame|offlineUser|" + playing.getPlayingConfigId() + "|" + playing.getCurStep() + "|" + playing.getCurRound() + "|" + Optional.ofNullable(usersOptional).orElse(Collections.emptyList()).size() + "|" + breakFlag);

                                if (breakFlag) {
                                    try {
                                        Thread.sleep(6000);
                                    } catch (InterruptedException e) {
                                    }
                                }
                            } catch (Exception e) {
                                LogUtil.e("Competition|playing|startGame|offlineUser|Error|tryAgien|" + retry, e);
                            }
                        } while (breakFlag && retry > 0);

						if(!breakFlag){
							//匹配中->开赛
							updateStatusToPlaying(playing);
						}
					} catch (Exception e) {
						LogUtil.e("Competition|playing|startGame|offlineUser|Error", e);
					}

				}, CompetitionGeneratePlayingService.isApplyPlaying(playing::getType) ? 100 : 3000);

			} catch (Exception e) {
				LogUtil.e("Competition|playing|playingStartCheck|error|", e);
			}

			return true;
		} else if(caseOffCheck(playing)) {
			caseOffCheckIn(playing);
		}
		return false;
	}


	/**
	 *@description 匹配
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/7
	 */
	protected boolean match(CompetitionPlayingDB playing, CompetitionPlayingConfigDB playingConfig, Optional<List<Long>> usersOptional, CompetitionClearingModelRes clearingModel, String serverId, long roomConfigId, boolean mutilServer) {

		List<Long> resList = match(playing, playingConfig, usersOptional, clearingModel, serverId, mutilServer);
		if(!CollectionUtils.isEmpty(resList)){
			//所有人都进入状态
			//实际人数
			competitionApplyDao.updatePlay(playing.getId(), 1, resList);
			updateInRoom(playing.getId(), resList, true);
		}

		return !CollectionUtils.isEmpty(resList);
	}


	/**
	 *@description 退赛
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/7
	 */
	public void caseOffCheckIn(CompetitionPlayingDB playing) {
		//处理退赛逻辑
		competitionJobSchedulerThreadPool.nSynBus(() -> {
			//清理所有房间
			playingBatchChangeClearingEndToEndStatus(playing.getId(), true);

			//所有报名玩家退赛
			Optional<List<Long>> longs = competitionApplyDao.queryForUsersByPlayingId(playing.getId());

			batchCancelPlaying(playing, longs);

			try {
				competitionPushService.cttConNotify(6006, longs.get(),
								Arrays.asList(
								String.format("你报名于\"%s\"开赛的\"%s\"由于人数不足已流局", new SimpleDateFormat(DateUtil.STRING_DATE_FORMAT).format(playing.getMatchBefore()), playing.getTitleCode()), playing.getId() + ""));
			}catch (Exception e) {
				LogUtil.e("competition|push|cttConNotify|6006|error|"+e.getMessage());
			}

			LogUtil.i("competition|caseOff|" + playing.getId() + "|" + longs.orElse(Collections.emptyList()).size());
		});
	}

	private void batchCancelPlaying(CompetitionPlayingDB playing, Optional<List<Long>> longs) {
		try {
			if(longs.isPresent() && !CollectionUtils.isEmpty(longs.get())){
				Iterator<Long> iterator = longs.get().iterator();
				while (iterator.hasNext()) {
					Long userId =  iterator.next();
					competitionApplyService.cancel(CompetitionApplyParam.builder()
							.playingId(playing.getId())
							.userId(userId)
							.build(), true);
				}
			}
		} catch (Exception e) {
			LogUtil.e("competition|push|cancel|error|"+e.getMessage());
		}
	}

	public void sleep(long t ){
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
		}
	}

//	public ServerConfig getMinServer() {
//		List<ServerConfig> collect = ServerDaoImpl.getInstance().queryAllServer().stream().filter(v -> v.getServerType() == 3).collect(Collectors.toList());
//		collect.sort(Comparator.comparing(c -> c.getOnlineCount()));
//		return collect.get(0);
//	}

	protected List<Long> match(CompetitionPlayingDB playing, CompetitionPlayingConfigDB playingConfig, Optional<List<Long>> usersOptional, CompetitionClearingModelRes clearingModel, String serverId, boolean mutilServer){
		List<Long> resList = new ArrayList<>();
		CompetitionPushModel build = null;
		//不同服器分发
		if (mutilServer) {
			LogUtil.i(String.format("competition|match|start|%s|%s|%s|%s", playing.getId(), playing.getCurStep(), playing.getCurRound(), usersOptional.get().size()));

			Map<Long, List<CompetitionClearingPlay>> userRank = Optional.ofNullable(clearingModel.getPlays()).orElse(Collections.emptyList()).stream().collect(Collectors.groupingBy(CompetitionClearingPlay::getUserId));

			List<ServerConfig> serverConfigs = ServerDaoImpl.getInstance().queryAllServer().stream().filter(v -> v.getServerType() == 3).collect(Collectors.toList());
			serverConfigs.sort(Comparator.comparing(c -> c.getOnlineCount()));
			serverConfigs.sort((v1, v2) -> Integer.valueOf(competitionRoomDao.queryCountByPlayingId(playing.getId(), (long) v1.getId(), playing.getCurStep(), playing.getCurRound()))
					> competitionRoomDao.queryCountByPlayingId(playing.getId(), (long) v2.getId(), playing.getCurStep(), playing.getCurRound()) ? -1 : 1);

			long t = System.currentTimeMillis();

			//房间配置
			Long roomConfigId = resolveRoomConfigId(playing.getCurStep(), playing.getCurRound(), playingConfig);

			//当前房间所需要的的总人数
			Long roomNeedPlayerCount = competitionRoomDao.queryPlayCountByRoomConfigId(roomConfigId);

			//当前空间房
			CompetitionRoom freeRoom = competitionRoomDao.queryNotStartRoomByCurCount(playing.getId());

			Map<Long, List<CompetitionClearingPlay>> collect = Optional.ofNullable(clearingModel.getPlays()).orElse(Collections.emptyList())
					.stream().collect(Collectors.groupingBy(CompetitionClearingPlay::getUserId));

			List<Long> args = new ArrayList(usersOptional.get());

			if(CollectionUtils.isEmpty(args)){
				return resList;
			}

			if(!clearingModel.isNoSplitUserOrder() && clearingModel.getCurStepTotalHuman() > roomNeedPlayerCount){
			    //打乱顺序
                Collections.shuffle(args);
            }

			Map<Long, Long> userIdServerId = new HashMap<>();
			Map<Long, List<Long>> serverIdUserIds = new HashMap<>();

			if (freeRoom != null && freeRoom.getCurrentCount() > 0 && freeRoom.getCurrentCount() < roomNeedPlayerCount) {
				LogUtil.i(String.format("competition|match|start|%s|%s|%s|%s|freeRoomJoin", playing.getId(), playing.getCurStep(), playing.getCurRound(), usersOptional.get().size()));

				try {
					for (int i = 0; i < freeRoom.getCurrentCount(); i++) {
						if (args.size() == 0) {
							break;
						}

						Long userId = args.remove(0);
						CompetitionRoomUser competitionRoomUser = new CompetitionRoomUser();
						competitionRoomUser.setRoomId(freeRoom.getKeyId());
						competitionRoomUser.setUserId(userId);
						competitionRoomUser.setGameResult(0l);
						competitionRoomUser.setLogIds("");
						competitionRoomUser.setInitScore(collect.containsKey(userId) ? collect.get(userId).get(0).getScore() : playing.getInitScore());
						competitionRoomUser.setPlayingId(playing.getId());
						competitionRoomUser.setServerId((int)freeRoom.getServerId());
						competitionRoomUser.setRank(!CollectionUtils.isEmpty(userRank.get(userId)) && userRank.get(userId).get(0).getRank() > 0 ? userRank.get(userId).get(0).getRank() : 999);
//						batchArgs.add(competitionRoomUser);
						userIdServerId.put(userId, freeRoom.getServerId());
						serverIdUserIds.merge(freeRoom.getServerId(), new ArrayList<>(Arrays.asList(userId)), (oV, nV) -> {
							oV.addAll(nV); return oV;
						});

						if(competitionRoomUserDao.addCompetitionRoomPlayerCount(freeRoom.getKeyId(), 1) > 0) {
							if (competitionRoomUserDao.saveRoomUser(Arrays.asList(competitionRoomUser)) > 0) {
								resList.add(userId);
							} else {
								competitionRoomUserDao.addCompetitionRoomPlayerCount(freeRoom.getKeyId(), -1);
							}
						}
					}
				} catch (Exception e) {
					LogUtil.e("competition|match|freeRoom|joinError|", e);
				}
			}

			int aliveHuman = competitionPlayingDao.queryAliveByPlayingId(playing.getId());
			int count = (int) (Math.max(1, (aliveHuman / roomNeedPlayerCount)) + Math.max(10, playing.getCurHuman() / 2 / 10));
			if (!clearingModel.isFirstMatch()
					|| competitionRoomDao.queryNotStartRoomByCurCount(playing.getId()) == null
					|| competitionRoomDao.queryCountByPlayingId(playing.getId(), (long)serverConfigs.get(0).getId(), playing.getCurStep(), playing.getCurRound()) < count) {
				LogUtil.i(String.format("competition|match|start|%s|%s|%s|%s|createRoom", playing.getId(), playing.getCurStep(), playing.getCurRound(), usersOptional.get().size()));

				CountDownLatch countDownLatch = new CountDownLatch(serverConfigs.size());
				//创房, 挪到开赛前
				Iterator<ServerConfig> iterator = serverConfigs.iterator();
				while (iterator.hasNext()) {
					ServerConfig next = iterator.next();
					int aliveRoom = competitionRoomDao.queryCountByPlayingId(playing.getId(), (long) next.getId(), playing.getCurStep(), playing.getCurRound());
					if (aliveRoom < count) {
						competitionJobSchedulerThreadPool.nSynBus(() -> {
							try {
								competitionPushService.playingBatchCreateRoom(Math.max(1, count - aliveRoom), next.getId(), clearingModel.copy());
							} catch (Exception e) {
								LogUtil.e("competition|match|playingBatchCreateRoomError|", e);
							} finally {
								int cur;
								if ((cur = competitionRoomDao.queryCountByPlayingId(playing.getId(), (long) next.getId(), playing.getCurStep(), playing.getCurRound())) < count) {
									//一个200ms
									sleep(Math.max((count - cur) * 180, 3000));
								}
								countDownLatch.countDown();
							}
						});
					} else {
						countDownLatch.countDown();
					}
				}

				try {
					countDownLatch.await();
				} catch (InterruptedException e) {
				}
			}

			//批量入服
			if(!clearingModel.isNoCreateTable()) {
				LogUtil.i(String.format("competition|match|start|%s|%s|%s|%s|joinRoom", playing.getId(), playing.getCurStep(), playing.getCurRound(), usersOptional.get().size()));
				try {
					List<Long> ress = new ArrayList<>();
					List<CompetitionRoomUser> batchArgs = new ArrayList<>();
					int size = args.size();
					int serverIndex = 0;
					for (int i = 1; i <= size; i++) {
						Long userId = args.remove(0);

						ress.add(userId);

						int sid = serverConfigs.get(serverIndex % serverConfigs.size()).getId();

						CompetitionRoomUser competitionRoomUser = new CompetitionRoomUser();
						competitionRoomUser.setRoomId(0l);
						competitionRoomUser.setUserId(userId);
						competitionRoomUser.setGameResult(0l);
						competitionRoomUser.setLogIds("");
						competitionRoomUser.setInitScore(collect.containsKey(userId) ? collect.get(userId).get(0).getScore() : playing.getInitScore());

						//决赛白名单
						//if(isDebug() && (userId==5532079 || userId==443412) && clearingModel.isFirstMatch()){
						//	competitionRoomUser.setInitScore(1000000);
						//}

						competitionRoomUser.setPlayingId(playing.getId());
						competitionRoomUser.setServerId(sid);
						competitionRoomUser.setRank(!CollectionUtils.isEmpty(userRank.get(userId)) && userRank.get(userId).get(0).getRank() > 0 ? userRank.get(userId).get(0).getRank() : 999);
						batchArgs.add(competitionRoomUser);
						userIdServerId.put(userId, (long) sid);
						serverIdUserIds.merge((long) sid, new ArrayList<>(Arrays.asList(userId)), (oV, nV) -> {
							oV.addAll(nV); return oV;
						});
						if (i % roomNeedPlayerCount == 0) {
							serverIndex++;
						}
					}
					if(!CollectionUtils.isEmpty(batchArgs)){
						if(competitionRoomUserDao.saveRoomUser(batchArgs) > 0){
							resList.addAll(ress);
						}
					}
				} catch (Exception e) {
					LogUtil.e("competition|match|batchInertUser|joinError|", e);
				}
			}

			sleep(500);

			CountDownLatch cdl = new CountDownLatch(serverIdUserIds.size());
			//切服&加入房间&开局
			Iterator<Entry<Long, List<Long>>> iterator2 = serverIdUserIds.entrySet().iterator();
			while (iterator2.hasNext()) {
				Entry<Long, List<Long>> next = iterator2.next();
				competitionJobSchedulerThreadPool.nSynBus(() -> {
					try {
						//加入房间
						competitionPushService.playingBatchRandomJoinRoom(next.getValue(), next.getKey(), clearingModel.copy());
						if(clearingModel.isFirstMatch()){
							long curFreeUserSize;
							while ((curFreeUserSize = competitionRoomUserDao.queryByUserRoomId(playing.getId())) > 0) {
								sleep(curFreeUserSize * 39);
							}
						}
					} catch (Exception e) {
						LogUtil.e("competition|jionTableError|", e);
					} finally {
						try {
							//切服
							competitionPushService.cgePlayServerPush(next.getValue(), playingConfig.getId(), next.getKey());
						} catch (Exception e) {
							LogUtil.e("competition|cgePlayServerPush|", e);
						} finally {
							cdl.countDown();
						}
					}
				});
			}

			try {
				cdl.await();
			} catch (InterruptedException e) {
			}

			Iterator<Entry<Long, List<Long>>> iterator3 = serverIdUserIds.entrySet().iterator();
			while (iterator3.hasNext()) {
				Entry<Long, List<Long>> next = iterator3.next();
				competitionJobSchedulerThreadPool.nSynBus(() -> {
					try {
						//开局
						competitionPushService.playingBatchStatRoomGame(next.getKey(), clearingModel.copy());
					} catch (Exception e) {
						LogUtil.e("competition|playingBatchStatRoomGame|", e);
					}
				});
			}

			LogUtil.i(String.format("competition|match|start|%s|%s|%s|%s|%sms", playing.getId(), playing.getCurStep(), playing.getCurRound(), usersOptional.get().size(), (System.currentTimeMillis() - t)));
		} else {
			//机器人优先匹配
			clearingModel.setPlays(new ArrayList<CompetitionClearingPlay>() {
				{
					for (Long user : usersOptional.get()) {
						this.add(CompetitionClearingPlay.builder().userId(user).score(playingConfig.getInitScore()).build());
					}
				}
			});

			build = CompetitionPushModel.builder()
					.userIds(usersOptional.get())
					.bindServerId(Long.valueOf(serverId))
					.clearModelRes(clearingModel)
					.build();

			//匹配成功人数
			String matchUserIds = competitionPushService.matchPushBatchInnerRoom(build);

			boolean res = Optional.of(matchUserIds).orElse("").length() > 1;

			if(res) {
				//推送完毕
				resList.addAll(JSONArray.parseArray(matchUserIds.substring(1), Long.class));
			}

			//机器人无需切服
			String s = competitionPushService.cgePlayServerPush(resList, playingConfig.getId(), Long.valueOf(serverId));
			LogUtil.i("competition|firstMatch|cgePlayServerPush|"+s);
		}


		return resList;
	}

	/**
	 *@description  //状态变更 报名->开赛
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/1
	 */
	public boolean matchStartCheck(CompetitionPlayingDB p){
		//匹配条件校验,
		if(p.getCurHuman() >= p.getBeginHuman() && competitionApplyDao.queryByCount(CompetitionApplyDB.builder().playingId(p.getId()).signShow(1).status(CompetitionApplyStatusEnum.NORMAL).build()) >= p.getBeginHuman()
				&& ((CompetitionGeneratePlayingService.isTimerPlaying(p::getType) && p.getMatchBefore().compareTo(new Date()) <= 0) || (CompetitionGeneratePlayingService.isApplyPlaying(p::getType)))
				&& updateStatusToMatching(p) > 0){
			//打开下个报名入口
//			competitionJobSchedulerThreadPool.nSynBus(this::openApplyEnter, true);
			competitionJobSchedulerThreadPool.nSynBus(() -> competitionGeneratePlayingService.findAndGeneratePlaying(p.getPlayingConfigId()));
			return true;
		}
		return false;
	}

	/**
	 *@description  //状态变更 报名->流局
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/1
	 */
	public boolean caseOffCheck(CompetitionPlayingDB p){
		//匹配条件校验,开赛时间到达但人数不足开赛, 流局
		if(p.getCurHuman() < p.getBeginHuman() && competitionApplyDao.queryByCount(CompetitionApplyDB.builder().status(CompetitionApplyStatusEnum.NORMAL).signShow(1).playingId(p.getId()).build()) < p.getBeginHuman()
				&& ((CompetitionGeneratePlayingService.isTimerPlaying(p::getType) && p.getMatchBefore().compareTo(new Date()) <= 0))
				&& (updateStatusToCaseOff(p) > 0) || p.getStatus() == CompetitionPlayingStatusEnum.CASTOFF){

		    LogUtil.i("competition|caseOffCheck|start");
			//打开下个报名入口
			competitionJobSchedulerThreadPool.nSynBus(() -> competitionGeneratePlayingService.findAndGeneratePlaying(p.getPlayingConfigId()));

			return true;
		}
		return false;
	}

	/**
	 *@description 已经无效的活动, 人未满, 开赛时间已到达
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/28
	 */
	protected int updateStatusToCastOff() {
		return competitionPlayingDao.updateStatus(null, CompetitionPlayingStatusEnum.APPLYING, CompetitionPlayingStatusEnum.CASTOFF, true, CompetitionPlayingTypeEnum.TIMER_PLAYING);
	}

	/**
	 *@description 更新赛事状态
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/2
	 */
	protected int updateStatusToPlaying(CompetitionPlayingDB p) {
		return competitionPlayingDao.updateStatus(p.getId(), CompetitionPlayingStatusEnum.MATCHING, CompetitionPlayingStatusEnum.PLAYING,false, p.getType());
	}

	/**
	 *@description 更新赛事状态
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/2
	 */
	protected int updateStatusToMatching(CompetitionPlayingDB p) {
		return competitionPlayingDao.updateStatus(p.getId(), CompetitionPlayingStatusEnum.APPLYING, CompetitionPlayingStatusEnum.MATCHING,false, p.getType());
	}

	/**
	 *@description 更新赛事状态
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/2
	 */
	protected int updateStatusToCaseOff(CompetitionPlayingDB p) {
		return competitionPlayingDao.updateStatus(p.getId(), CompetitionPlayingStatusEnum.APPLYING, CompetitionPlayingStatusEnum.CASTOFF,false, p.getType());
	}

	/**
	 *@description 开始匹配锁
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/25
	 */
	protected boolean matchLockChangeStatus(int defaultStatus, int status) {
		boolean invoke = false;
		if (MATCH_START_LOCK == defaultStatus) {
			synchronized (this) {
				if (MATCH_START_LOCK == defaultStatus) {
					MATCH_START_LOCK = status;
					invoke = true;
				}
			}
		}
		return invoke;
	}

	public CallBackInvoke<CompetitionClearingModelRes> createHandle(CompetitionClearingModelReq build) {
		CallBackInvoke<CompetitionClearingModelRes> objectCallBackInvoke = new CallBackInvoke<CompetitionClearingModelRes>();
		objectCallBackInvoke.setBefore(() -> batchUpdateRank(build));
		return objectCallBackInvoke;
	}

//	public CallBackInvoke<CompetitionClearingModelRes> clearing(CallBackInvoke<CompetitionClearingModelRes> handls, CompetitionClearingModelReq build, Optional<CompetitionPlayingDB> competitionPlayingDB) {
//		LogUtil.i("competition|clearing|notFindClearingActor|" + build);
//		return null;
//	}

	/**
	 *@description 手动加入房间
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/7
	 */
	public String competitionJoinTable(long playingId) {
		StringBuilder sb = new StringBuilder();
		CompetitionPlayingDB playing = getPlaying(playingId).get();

		MessageAssert.assertFalse(playing.getStatus() != CompetitionPlayingStatusEnum.PLAYING, MessageExceptionParam.builder().msg("赛事不在比赛中").throwException(true).build());

		setAddField(playing);

		Optional<List<Long>> usersOptional = competitionApplyDao.queryForUsersByPlayingId(playing.getId());

		CompetitionPlayingConfigDB playingConfig = competitionGeneratePlayingService.getPlayingConfig(playing.getPlayingConfigId()).get();

		CompetitionClearingRiseInRankMode resolve = CompetitionClearingRiseInRankMode.resolve(playing, playingConfig, null);

		long roomConfigId = resolveRoomConfigId(playing.getCurStep(), playing.getCurRound(), playingConfig);

		try {
			//客户端自己获取服务器ID加入游戏,这里仅做开赛推送至指定界面
			CompetitionClearingModelRes clearingModel = CompetitionClearingModelRes.builder()
					.playingTitle(playing.getTitleCode())
					.curStep(playing.getCurStep())
					.curRound(playing.getCurRound())
					.baseScore(resolve.getBaseScore())
					.weedOutScore(resolve.getRoundLowScore())
					.curStepTotalHuman(usersOptional.get().size())
					.totalHuman(playing.getCurHuman())
					.playingId(playing.getId())
					.roomConfigId(roomConfigId)
					.loginCallBackUrl(playingConfig.getLoginCallBackUrl())
					.loginCallBackClearing(playingConfig.getLoginCallBackClearing())
					.loginCallBackCancel(playingConfig.getLoginCallBackCancel())
					.currentMills(System.currentTimeMillis())
					.secondWeedOut(playing.getSecondWeedOut())
					.playingOpenTime(playing.getOpenTime().getTime())
					.build();

			//给所有玩家推送切到赛场对应的服务器, 先切服,再推送
			String s = competitionPushService.cgePlayServerPush(usersOptional.get(), playingConfig.getId(), playing.getBindServerId());
			sb.append("ChangeServer:"+s);
			//推送进入游戏, 测试接口统一化, 切服失败都不加入游戏
			if (!StringUtils.isBlank(s) && s.indexOf("success") > -1) {
				clearingModel.setPlays(new ArrayList<CompetitionClearingPlay>() {
					{
						for (Long user : usersOptional.get()) {
							this.add(CompetitionClearingPlay.builder().userId(user).score(playingConfig.getInitScore()).build());
						}
					}
				});

				CompetitionPushModel build = CompetitionPushModel.builder()
						.playingId(playing.getId())
						.userIds(usersOptional.get())
						.bindServerId(playing.getBindServerId())
						.clearModelRes(clearingModel)
						.build();

				LogUtil.i("Competition|playing|startGame|match|" + build);
				//匹配推送
				String s1 = competitionPushService.matchPushBatchInnerRoom(build);
				sb.append(" Match:"+s1);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

	/**
	 *@description 不是淘汰赛了
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/24
	 */
	protected boolean isBreakWeedOutStep(CompetitionClearingRiseInRankMode curRoundResolve, int total) {
		return curRoundResolve.getRoundLowScore() <= 0 || (total <= curRoundResolve.getRoundEndHuman());
	}

	/**
	 *@description 不是淘汰赛了
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/24
	 */
	protected boolean isBreakWeedOutStep(CompetitionClearingRiseInRankMode curRoundResolve) {
		return curRoundResolve.getRoundLowScore() <= 0 || (competitionPlayingDao.queryAliveByPlayingId(curRoundResolve.getPlayingId()) <= curRoundResolve.getRoundEndHuman());
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
			CompetitionClearingModelRes competitionClearingModelRes){
		LogUtil.i("Competition|playing|notFindPlayingHandle|" + playing.getType());
	}

	/**
	 *@description 结算回调
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/31
	 */
	public CallBackInvoke<CompetitionClearingModelRes> clearingCallBack(CallBackInvoke<CompetitionClearingModelRes> handls, CompetitionClearingModelReq build) {
		try {
			return this.clearing(handls, build);
		} catch (Exception e) {
			LogUtil.e("Competition|playing|clearingCallBack|error|" + build + "|" + e);
			throw e;
		}
	}

	/**
	 *@description 由Cliglib代理模式缺陷,这里单独抽离出来业务,表示该函数不处于结算的事物内
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/3
	 */
	public synchronized void batchUpdateRank(CompetitionClearingModelReq build) {
		try {
			if (CollectionUtils.isEmpty(build.getUsers())) {
				return;
			}

			cgeTotalRankAndPush(getPlaying(build.getPlayingId()).get(), build.getUsers(),false,true,true);

//			lockAndRetry(competitionPlayingDao, build.getPlayingId(),()->{
////					//更新所有人总榜排名,这里的状态一定为存活
//				competitionScoreTotalDao.insertBatch(false, true, build.getUsers().stream()
//						.map(v ->
//								CompetitionScoreTotalDB.builder()
//										.playingId(build.getPlayingId())
//										.userId(v.getUserId())
//										.score(v.getScore())
//										.build()).collect(Collectors.toList()));
//			});

		} catch (Exception e) {
			LogUtil.e("competition|playing|batchUpdateRank|error", e);
		}
	}


	/**
	 *@description 发送失败者的奖励同时更新玩家状态
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/8/4
	 */
	public void sendWeedOutAwardAndUpdateInRoom(CompetitionPlayingConfigDB playingConfig, CompetitionPlayingDB playingf, List<CompetitionClearingPlay> currentStepAllPlay, boolean checkNextStep) {
		if(!CollectionUtils.isEmpty(currentStepAllPlay)){
			//失败的发送奖励
			List<CompetitionClearingPlay> weedOutPlays = currentStepAllPlay.stream().filter(v -> v.getIsOver() == CompetitionScoreDetailStatusEnum.WEED_OUT).collect(Collectors.toList());
			weedOutPlays.forEach(v -> {
				try {
					//失败的直接发送奖励
					singleSendAward(playingf, playingConfig.getAwards().split("_"), v.getUserId(), v.getRank());
				} catch (Exception e) {
					LogUtil.e("Competition|playing|AwardInfo|error|" + playingf.getId() + "|" + playingConfig.getAwards() + "|" + v.getUserId() + "|" + v.getRank(), e);
				}
			});

			List<Long> collect = weedOutPlays.stream().filter(v -> !checkNextStep || !v.isNextStep()).map(v -> v.getUserId()).collect(Collectors.toList());
			if(!CollectionUtils.isEmpty(collect)){
				//失败的直接变更状态
				updateInRoom(playingf.getId(), collect, true);
			}
		}
	}


	/**
	 *@description 推送总榜单变化
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/8
	 */
	public void pushRankChange(CompetitionPlayingDB playing,boolean refreshTotalRankData) {
		//给其他玩家推送实时排名榜单
		competitionJobSchedulerThreadPool.nSynBus(()-> {
			try {
				//当前存活所有玩家列表
				List<CompetitionScoreTotalDB> rank = competitionRankService.getRank(CompetitionScoreTotalDB.builder().playingId(playing.getId())
						.status(CompetitionScoreDetailStatusEnum.RISE_IN_RANK).limit(playing.getCurHuman()).build());
				int total = rank.size();
				List<Long> userIds = new ArrayList<>();
				Map<Integer, String> arg = new HashMap<>();

				for (CompetitionScoreTotalDB d : rank) {
					userIds.add(d.getUserId());
					arg.put(d.getUserId().intValue(), d.getRank() + ":" + total);
				}
				//给其他玩家推送实时榜单
				competitionPushService.playingRankPush(rank.stream().map(v -> v.getUserId()).collect(Collectors.toList()), arg);
			} catch (Exception e) {
				LogUtil.e("competition|cleraing|pushRankError",e);
			}
		});
//		competitionPushService.pushRankRefresh(CompetitionPushService.CompetitionRefreshQueueModel.builder().playingId(playing.getId()).refreshTotalRankData(refreshTotalRankData).build());
	}

	/**
	 *@description 变更总榜状态并推送变化
	 *@param playing
	 *@param currentStepAllPlay
	 *@param cgeTotalRankStatus 更新总榜状态(淘汰\晋级)
	 *@param cgeTotalRankScore	更新总榜分数(可能经过比率转化的结算分)
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/8
	 */
	protected void cgeTotalRankAndPush(CompetitionPlayingDB playing, List<CompetitionClearingPlay> currentStepAllPlay, boolean cgeTotalRankStatus, boolean cgeTotalRankScore, boolean pushRankCge) {
		//所有牌桌进行完毕更新掉积分
		//更新所有人总榜排名,这里更新掉状态, 包含积分(这里为换算过比例的积分)
		//更新所有人总榜排名,这里更新掉状态, 不包含积分(这里为换算过比例的积分)
//		synchronized (Long.valueOf(playing.getId())){
			lockAndRetry(competitionPlayingDao, playing.getId(),()-> {
				try {
					competitionScoreTotalDao.insertBatch(cgeTotalRankStatus, cgeTotalRankScore,
							currentStepAllPlay.stream()
									.map(v -> CompetitionScoreTotalDB.builder()
											.playingId(playing.getId())
											.userId(v.getUserId())
											.score(v.getScore())
											.status(v.getIsOver()).build()
									).collect(Collectors.toList()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			updateTotalRank(CompetitionScoreTotalDB.builder().randomVal(getRandomVal()).status(1).playingId(playing.getId()).build());

//		}
//
////		if(CompetitionGeneratePlayingService.isApplyPlaying(playing::getType)){
//			updateTotalRank(CompetitionScoreTotalDB.builder().randomVal(getRandomVal()).playingId(playing.getId()).build());
//	//		//更新总榜排名
//	//		//推送总榜单变化
//			pushRankChange(playing, true);
////		}

	}

	protected List<Long> filterRankUpUser(List<CompetitionClearingPlay> currentStepAllPlay) {
		//晋级玩家ID
		HashSet<Long> rankUpUserId= new HashSet<>();

		Iterator<CompetitionClearingPlay> iterator = currentStepAllPlay.iterator();
		while (iterator.hasNext()) {
			CompetitionClearingPlay next = iterator.next();
			if (next.getIsOver() == CompetitionScoreDetailStatusEnum.WEED_OUT) {
				iterator.remove();
				continue;
			}
			rankUpUserId.add(next.getUserId());
		}
		return new ArrayList<>(rankUpUserId);
	}

	/**
	 *@description 推送切服
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/7
	 */
	protected void notifyPlayCgeGameServer(CompetitionPlayingDB playing, CompetitionPlayingConfigDB playingConfig, List<Long> userIds) {
		//大回合晋级,帮助玩家切服
		//进阶赛先切服, 批量切换至比赛服绑定的ID, 这里进阶后人数较少, 可以指定
		String cgeRes = competitionPushService.cgePlayServerPush(userIds, playingConfig.getId(), playing.getBindServerId());
		LogUtil.i("competition|playing|showClearing|cgePlayServer|" + playing.getId() + "|" + playing.getCurStep() + "|" + playing.getCurRound() + "|SId->" + playing.getBindServerId() + "|" + userIds.size());
	}

	/**
	 *@description 变更总榜
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/24
	 */
	public void updateTotalRank(CompetitionScoreTotalDB arg) {
		lockAndRetry(competitionPlayingDao, arg.getPlayingId(), () -> {
			try {
				//更新总榜排名
				competitionScoreTotalDao.updateRankByPlayingId(arg);
			}
			catch (Exception e) {
				LogUtil.e("competition|updateTotalRank|error", e);
			}
		});
	}

	public void updateInRoom(long id, List<Long> userIds, boolean inRoom) {
		lockAndRetry(competitionPlayingDao, id, ()->{
			try {
				//更改全部玩家进入房间状态
				competitionScoreTotalDao.updateInRoom(id, userIds, inRoom);
			} catch (Exception e) {
				LogUtil.e("competition|updateInRoom|error", e);
			}
		});
	}

	public void lockAndRetry(CompetitionCommonDao dao , long id, Runnable r) {
		synchronized (dao) {
			int retry = 40;
			boolean res;
			while (res = !LockSharing.lock(dao, id, r)) {
				LogUtil.i("competition|lockAndRetry|warn|" + id + "|" + retry);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}

				retry -= 1;

				if (retry == 0 && res) {
					throw new RuntimeException("competition|retry|error|" + retry);
				}
			}
		}
	}

	/**
	 *@description 输家重排名
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/7
	 */
	protected AttributeKV<Long> weedOutPlayReSortRank(CompetitionPlayingDB playing, int curStep, int curRound, CompetitionClearingRiseInRankMode curRoundResolve) {
		LogUtil.i("Competition|playing|clearing|resort|start");
		AttributeKV<Long> longAttributeKV = new AttributeKV<>(0l, 0l);

		lockAndRetry(competitionPlayingDao, playing.getId(), () -> {
			//重新计算赢家排名
			Long winnerCount = competitionScoreDetailDao.updateRankByPlayingId(CompetitionScoreDetailDB.builder()
					.playingId(playing.getId())
					.lastStep(curStep)
					.lastRound(curRound)
					.randomVal(getRandomVal())
					//初始从1开始排位
					.lastRank(0)
					//仅更新输家排名
					.lastStatus(CompetitionScoreDetailStatusEnum.RISE_IN_RANK)
					.build());

			//这一个回合晋级的人数
			int roundUpLevelHuman = curRoundResolve.getRoundUpLevelHuman();

			//输家奖励从晋级的名次内剔除
			//更新这一轮输家的排名
			Long loseCount = competitionScoreDetailDao.updateRankByPlayingId(CompetitionScoreDetailDB.builder()
					.playingId(playing.getId())
					.lastStep(curStep)
					.lastRound(curRound)
					.randomVal(getRandomVal())
					//初始从n开始排位
					.lastRank(roundUpLevelHuman)
					//仅更新输家排名
					.lastStatus(CompetitionScoreDetailStatusEnum.WEED_OUT)
					.build());

			longAttributeKV.setKey(winnerCount);
			longAttributeKV.setValue(loseCount);
		});
		LogUtil.i("Competition|playing|clearing|resort|end|" + playing.getId() + "|" + curStep + "|" + curRound + "|" + longAttributeKV.getKey() + "|" + longAttributeKV.getValue());

		return longAttributeKV;
	}

	public long getRandomVal() {
		return System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000000) + Thread.currentThread().getId();
	}

	/**
	 *@description 等待固定时间后进行匹配,这个时间是为了给客户端余出来展示小结界面
	 *@param resolve 淘汰配置
	 *@param curUpTotalHuman 当前回合总人数
	 *@param curUpLevelHuman 当前晋级人数
	 *@param theNextStage 下一个阶段
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/9
	 */
	protected void waitAndRepeatedMatch(
			CompetitionPlayingDB playing,
			CompetitionPlayingConfigDB playingConfig,
			CompetitionClearingRiseInRankMode upRoundResolve,
			CompetitionClearingRiseInRankMode curRoundResolve,
			int curUpTotalHuman,
			int curUpLevelHuman,
			boolean theNextStage,
			int curRate,
			boolean bigStage,
			boolean nextStep,
			List<CompetitionClearingPlay> allUpRankPlay,
			List<Long> userIds,
			Map<Long, List<CompetitionScoreTotalDB>> usersRank, int waitTime) {
		LogUtil.i("Competition|playing|clearing|matchPush|" + allUpRankPlay);
		//默认展示3秒结算界面,等待牌桌的会一直等待,等待完成后一次性推送所有
		competitionJobSchedulerThreadPool.nSynDelayBus(() -> {
			try {
				String matchRes = "no request";
				CompetitionPushModel build1 = null;
				build1 = CompetitionPushModel.builder()
						.playingId(playing.getId())
						.bindServerId(playing.getBindServerId())
						//仅匹配晋级的
						.userIds(userIds)
						//这里的积分比例存入赛事流水时就已经换算过, 无需重复换算, 这里的倍率保持1
						//isNextStep保持false,不需要重复显示晋级界面
						.clearModelRes(buildResModelClearing(false, bigStage, allUpRankPlay, playingConfig, playing, upRoundResolve, curRoundResolve, curUpTotalHuman, theNextStage, 1, curRate, usersRank))
						.build();

				//匹配推送,再次进行匹配
				matchRes = competitionPushService.matchPushBatchInnerRoom(build1);

				LogUtil.i("Competition|playing|clearing|matchPush|build1:" + build1 + "|onlineUpRank:" + allUpRankPlay + "|matchRes:" + matchRes);
			} catch (Exception e) {
				LogUtil.e("competition|playing|clearing|matchPush|error",e);
			}

		}, waitTime == 0 ? (long) Math.ceil(nextStep ? clearingWaitShowTime : Double.valueOf(clearingWaitShowTime) / 2) : 0); //小结延迟时间根据垮轮次小结延迟时间缩短一半
	}

	/**
	 *@description 结算回调
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/25
	 */
	public CallBackInvoke<CompetitionClearingModelRes> clearing(CallBackInvoke<CompetitionClearingModelRes> handls, CompetitionClearingModelReq build) {
		if (build == null || CollectionUtils.isEmpty(build.getUsers())) {
			LogUtil.e("Competition|playing|clearing|error|" + build);
			return null;
		}

        LogUtil.i("Competition|playing|clearing|start|" + build.getPlayingId() + "|" + build.getCurStep() + "|" + build.getCurRound() + "|" + build);

		//当前赛事剩余未完结的牌桌数量
		long noClearingTableCount = build.getCurTableCount();//competitionPlayingDao.queryCurTableCount(build.getPlayingId());//competitionRoomDao.queryByPlayingCount(build.getPlayingId(), build.getCompetitionRoomKeyId());

		Optional<CompetitionPlayingDB> competitionPlayingDB = competitionPlayingDao.queryForSingle(CompetitionPlayingDB.builder().id(build.getPlayingId()).build());

		if (!competitionPlayingDB.isPresent()) {
			LogUtil.e("Competition|playing|clearing|找不到赛事|" + build);
			return null;
		}

		//赛事主体
		CompetitionPlayingDB playing = competitionPlayingDB.get();

		//回合数据错误
		if (!(build.getCurStep() == playing.getCurStep() && build.getCurRound() == playing.getCurRound()) || playing.getStatus() != CompetitionPlayingStatusEnum.PLAYING || CollectionUtils.isEmpty(build.getUsers())) {
			LogUtil.e(String.format("Competition|playing|clearing|参数错误|%s|%s", build, playing));
			//错误的参数
			return null;
		}

		List<CompetitionClearingPlay> clearingPlays = build.getUsers().stream().map(v -> v.copy()).collect(Collectors.toList());

		//赛事配置
		CompetitionPlayingConfigDB playingConfig = competitionGeneratePlayingService.getPlayingConfig(playing.getPlayingConfigId()).get();

		//晋级配置
		CompetitionClearingRiseInRankMode curRoundResolve = CompetitionClearingRiseInRankMode.resolve(playing, playingConfig, build);

		//玩家现有积分需要转换比例
		float scoreBasicRatio = curRoundResolve.getConvertRate();

		//进入下一回合
		boolean theNextStage = noClearingTableCount <= 0;

		//当前回合
		int curStep = build.getCurStep();
		int curRound = build.getCurRound();

		//赛制:每桌前N名晋级
		boolean nextStepNotInTopNOut = curRoundResolve.getNextStepNotInTopNOut() > 0;

		//上一个轮次剩余人数
		long currentStepHuman1 = competitionPlayingDao.queryAliveByPlayingId(playing.getId());

		//统计当前结束牌桌的玩家的成绩,保存
		List<CompetitionScoreDetailDB> clearingedPlays = buildDbModelScoreDetail(build, playing, curRoundResolve.getRoundLowScore(), curRoundResolve.getNextStepNotInTopNOut(),
				//定时赛进入下一轮时以配置倍率结算积分
				CompetitionGeneratePlayingService.isTimerPlaying(playing::getType) ? 1.0f : scoreBasicRatio,
				//存活人数到达晋级人数,不再淘汰人数了
				(isBreakWeedOutStep(curRoundResolve, (int) currentStepHuman1)));

		int curOutPeple = (int) clearingedPlays.stream().filter(v1 -> v1.getLastStatus() != 1).count();
		long currentStepHuman = competitionPlayingDao.queryAliveByPlayingId(playing.getId()) - curOutPeple;

		//保存战绩
		competitionScoreDetailDao.insertBatch(clearingedPlays);

		//上一个回合的总人数,到当前回合剩余人数
		@Deprecated
		int curRoundTotalHuman = 0;

		LogUtil.i("Competition|playing|clearing|updateRankByPlayingId|" + playing.getId() + "|" + curStep + "|" + curRound + "|" + curRoundTotalHuman + "|"+ curRoundResolve  +"|"+ clearingedPlays);

		//当前底分
		int curRate = curRoundResolve.getBaseScore();

		//这一批玩家当前的实时排名
		//这仅仅是临时排名
		//全部牌桌结算后,每桌晋级n人次会打乱这个排名
		List<CompetitionScoreTotalDB> rankByUserId = competitionRankService.getRankByUserId(playing.getId(), build.getUsers().stream().map(v -> v.getUserId()).collect(Collectors.toList()));

		//当前结算的玩家结算数据
		List<CompetitionClearingPlay> clearingFinishPlay = clearingedPlays.stream().map(v -> CompetitionClearingPlay.builder()
				.userId(v.getUserId())
				.score(v.getLastScore())
				.tableRank(v.getLastTableRank())
				//等待其他牌桌结算期间,这里仅赢家展示等待晋级界面,输家依然等待其他牌桌结算
				.isOver(v.getLastStatus() == CompetitionScoreDetailStatusEnum.RISE_IN_RANK ? CompetitionScoreDetailStatusEnum.RISE_IN_RANK : 0)
				//只有每桌前N晋级的情况才一定是已经确定已经晋级的名单
				//在特殊场景需要提前预知晋级通知客户端进入晋级等待界面,如:每桌第一名晋级
				//定时赛最后一轮所有玩家晋级过度界面等待
				.nextStep(
						!CompetitionGeneratePlayingService.isTimerPlaying(playing::getType)
								? !curRoundResolve.isFinalConfig() && noClearingTableCount > 0 && curRoundResolve.getRoundLength() == 1 && nextStepNotInTopNOut && v.getLastStatus() == CompetitionScoreDetailStatusEnum.RISE_IN_RANK
								: isBreakWeedOutStep(curRoundResolve, (int) currentStepHuman) || (curRoundResolve.getRoundLowScore() == 0)
				)
				.build())
				.collect(Collectors.toList());

		//结果返回,数据录入后优先返回
		//这里返回的结果为等待所有牌桌结算后最终结算的临时界面
		CompetitionClearingModelRes clearingReturnData = buildResModelClearing(
				false,
				false,
				clearingFinishPlay,
				playingConfig,
				playing,
				curRoundResolve,
				curRoundResolve,
				curRoundTotalHuman,
				CompetitionGeneratePlayingService.isTimerPlaying(playing::getType)
						? !isBreakWeedOutStep(curRoundResolve, (int) currentStepHuman)
						: theNextStage,
				1,
				curRate,
			rankByUserId.stream().collect(Collectors.groupingBy(CompetitionScoreTotalDB::getUserId)));

		LogUtil.i("Competition|playing|clearing|returnData|" + clearingReturnData.getPlayingId() + "|" + clearingReturnData.getCurStep() + "|" + clearingReturnData.getCurRound() + "|" + clearingReturnData);

		//推送总榜单变化
//		pushRankChange(playing,false);

		CompetitionPlayingDB finalPlaying = playing;
		CompetitionClearingRiseInRankMode finalUpRoundResolve = curRoundResolve;
		CompetitionClearingModelRes competitionClearingModelResFinal = clearingReturnData.copy();
		competitionClearingModelResFinal.setPlays(new ArrayList<>(clearingReturnData.getPlays()));
		handls.setResult(clearingReturnData);
		handls.setAfter(() -> {
			//更新淘汰人数
			competitionPlayingDao.updateAliveHuman(CompetitionPlayingDB.builder().id(playing.getId()).aliveHuman(-curOutPeple).build());

			competitionJobSchedulerThreadPool.nSynDelayBus(() -> {
				List<Long> allUserIds = new ArrayList<>();
				Iterator<CompetitionClearingPlay> iterator = clearingPlays.iterator();
				while (iterator.hasNext()) {
					CompetitionClearingPlay next = iterator.next();
					allUserIds.add(next.getUserId());
				}

				try {
					//更改玩家进入房间状态
					updateInRoom(playing.getId(), allUserIds, false);
				} catch (Exception e) {
					LogUtil.e("Competition|playing|clearing|updateInRoom|error|" + allUserIds, e);
				}

				//延迟更新榜单, 给失败者发送奖励
				competitionRankRefreshService.addRankRefreshQueue(CompetitionRefreshQueueModel.builder().playingId(playing.getId()).noClearingTableCount(build.getCurTableCount()).build());


				//这里延迟是给定数据有足够时间提交事务
				//定时赛需要延迟5秒等待是否有额外牌桌结算后重复匹配
			}, 1800);

		});

		LogUtil.i("Competition|playing|clearing|end|" + build.getPlayingId() + "|" + build.getCurStep() + "|" + build.getCurRound() + "|" + build);

		return handls;
	}

	/**
	 *@description 结算返回
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/5
	 */
	public CompetitionClearingModelRes buildResModelClearing(boolean isNextStep, boolean isOver, List<CompetitionClearingPlay> plays,
			CompetitionPlayingConfigDB playingConfig,
			CompetitionPlayingDB playing,
			CompetitionClearingRiseInRankMode upRoundResolve,
			CompetitionClearingRiseInRankMode curRoundResolve,
			int curTotalHuman,
			boolean theNextStage,
			float scoreBasicRatio,
			int curRate,
			Map<Long, List<CompetitionScoreTotalDB>> usersRank
	) {
		setAddField(playing);
		return CompetitionClearingModelRes.builder()
				//0匹配其他玩家,1等待其他牌桌结算,2大结算, 3断线重连等待
				.result(isOver ? 2 : theNextStage ? 0 : 1)
				//当前赛制
				.curStep(playing.getCurStep())
				//当前回合
				.curRound(playing.getCurRound())
				//标题
				.playingTitle(playing.getTitleCode())
				//底分,倍率
				.baseScore(curRoundResolve.getSrc().getBaseScore())
				//淘汰分数
				.weedOutScore(curRoundResolve.getSrc().getRoundLowScore())
				//当前回合剩余人数
				.curStepTotalHuman(curTotalHuman)
				//当前总有效参与人数
				.totalHuman(playing.getCurHuman())
				//回调地址
				.loginCallBackUrl(playingConfig.getLoginCallBackUrl())
				//清算地址
				.loginCallBackClearing(playingConfig.getLoginCallBackClearing())
				//退赛地址
				.loginCallBackCancel(playingConfig.getLoginCallBackCancel())
				//赛事id
				.playingId(playing.getId())
				//赛事牌桌配置id
				.roomConfigId(resolveRoomConfigId(playing.getCurStep(), playing.getCurRound(), playingConfig))
				//房间id
				.playingRoomId(playing.getPlayingRoomId())
				//每个大回合晋级人数
				.stepUpgradeDetails(playingConfig.getStepUpgradeDetails(playing))
				//当前时间点毫秒数
				.currentMills(System.currentTimeMillis())
				//淘汰配置
				.secondWeedOut(playing.getSecondWeedOut())
				//比赛开始时间
				.playingOpenTime(playing.getOpenTime().getTime())
				//返回玩家消息
				.plays(plays.stream().map(v -> {
					//晋级下一个轮次的详细等待界面
					if (!v.isNextStep()) {
						v.setNextStep(isNextStep);
					}
					//当前积分,是否以一定比率结算
					v.setScore(Math.round(v.getScore() * scoreBasicRatio));
					//当前排名,临时等待界面使用房间排名
					if (usersRank != null && usersRank.containsKey(v.getUserId()))
						v.setRank(usersRank.get(v.getUserId()).get(0).getRank());
					//大结算\淘汰,返回奖励展示
					if (isOver || v.getIsOver() == CompetitionScoreDetailStatusEnum.WEED_OUT) {
						CompetitionAward award = curRoundResolve.getAward(playingConfig, v.getRank());
						if (award != null) {
							v.setAwardId(award.getId());
							v.setAwardVal(award.getVal());
						}
					}
					return v;
				}).collect(Collectors.toList()))
				.build();
	}

	/**
	 *@description 获得赛事配置的id,每个不同的阶段,可能是使用不同的配置,超出当前阶段使用最后一阶段的配置
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/8
	 */
	public Long resolveRoomConfigId(int curStep, int curRound, CompetitionPlayingConfigDB playingConfig) {
		//打立出局;淘汰赛...
		String[] stepRoundConfig = playingConfig.getRoomConfigIds().split(";");
		//回合1,回合2...
		String[] stepRoundConfigIds = stepRoundConfig[stepRoundConfig.length <= curStep - 1 ? stepRoundConfig.length-1 : curStep - 1].split(",");

		return NumberUtils.toLong(stepRoundConfigIds[stepRoundConfigIds.length <= curRound - 1 ? stepRoundConfigIds.length-1 : curRound - 1]);
	}

	/**
	 *@description 滚动回合数或者滚动轮次
	 *@param riseInRankEndHuman 大回合结算人数
	 *@param riseInRankHuman 	大回合晋级人数
	 *@param riseInRankNumber 	当前的有效的晋级人数
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/5
	 */
	protected CompetitionPlayingDB rollStepOrRound(CompetitionPlayingDB paramPlaying, boolean isNextStep) {
		long playingId = paramPlaying.getId();

		//全部桌子结算完毕, 同时晋级人数到达大回合结算, 进入到下一个轮次
		if (isNextStep) {
			//更新轮次
			competitionPlayingDao.updateCurStepRound(CompetitionPlayingDB.builder().id(playingId).curStep(1).build());
			return competitionPlayingDao.queryForSingle(CompetitionPlayingDB.builder().id(playingId).build()).get();
		} else {
			//滚动当前赛事的 回合
			competitionPlayingDao.updateCurStepRound(CompetitionPlayingDB.builder().id(playingId).curRound(1).build());
			return competitionPlayingDao.queryForSingle(CompetitionPlayingDB.builder().id(playingId).build()).get();
		}
	}

	/**
	 *@description 解析当前关卡回合的底分
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/5
	 */
	public static int getCurRoundRate(int curStep, int curRound, CompetitionPlayingConfigDB playingConfig, int curRate) {
		//轮次底分: 轮次,回合底分,回合底分...
		String[] stepRate = playingConfig.getStepRate().split(";");

		if(stepRate.length >= curStep) {
			String[] roundRate = stepRate[curStep - 1].split(",");
			//有当前回合的底分用当前回合的底分,没有用最后一轮的底分填充

			if (roundRate.length >= curRound) {
				curRate = NumberUtils.toInt(roundRate[curRound - 1]);
			} else {
				curRate = NumberUtils.toInt(roundRate[roundRate.length - 1]);
			}
		}
		return curRate;
	}

	protected void waitAndRepeatedMatch(
			CompetitionPlayingDB playing,
			CompetitionPlayingConfigDB playingConfig,
			CompetitionClearingRiseInRankMode upRoundResolve,
			CompetitionClearingRiseInRankMode curRoundResolve,
			List<CompetitionClearingPlay> currentStepAllPlay,
			int curUpTotalHuman,
			boolean theNextStage,
			int curRate,
			boolean bigStage,
			boolean nextStep,
			int curStep,
			int curRound,
			int waitTime, boolean refreshData, boolean noExistsTable) {
		//默认展示3秒结算界面,等待牌桌的会一直等待,等待完成后一次性推送所有
		try {
			boolean changeStatus = true;
			List<CompetitionClearingPlay> allUpRankPlay = currentStepAllPlay;
			CompetitionClearingModelRes clearingModelRes = null;
			//当前剩余玩家
			int rankCount = competitionPlayingDao.queryAliveByPlayingId(playing.getId());
			//玩家房间状态列表
			List<CompetitionScoreTotalDB> rankList = competitionScoreTotalDao.queryByInRoom(CompetitionScoreTotalDB.builder().playingId(playing.getId()).inRoom(false).build()).orElse(Collections.emptyList());

			Map<Long, Void> rankMap = new HashMap<>();
			if (!CollectionUtils.isEmpty(rankList)) {
				rankList.forEach(v -> rankMap.put(v.getUserId(), null));
			}

			//当前轮次所有人
			List<CompetitionScoreDetailDB> competitionScoreDetailDBS = competitionScoreDetailDao.queryForList(
					CompetitionScoreDetailDB.builder()
							.playingId(playing.getId())
							.lastStep(curStep)
							.lastRound(curRound)
							.lastStatus(CompetitionScoreDetailStatusEnum.RISE_IN_RANK)
							.build()).orElse(Collections.emptyList());

//				competitionScoreDetailDBS.sort(Comparator.comparing(CompetitionScoreDetailDB::getLastScore));

			if (!CollectionUtils.isEmpty(competitionScoreDetailDBS)) {
				allUpRankPlay = competitionScoreDetailDBS.stream()
						.filter(v -> rankMap.containsKey(v.getUserId()))
						.map(v -> CompetitionClearingPlay.builder()
								.userId(v.getUserId())
								.score(v.getLastScore())
								.rank(v.getLastRank() == null ? 0 : v.getLastRank())
								.isOver(v.getLastStatus())
								.build()).collect(Collectors.toList());

			}
//			LogUtil.i("competition|waitAndRepeatedMatch|"+allUpRankPlay);
			LogUtil.i(String.format("competition|waitAndRepeatedMatch|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s", playing.getId(), playing.getCurStep(), playing.getCurRound(), curStep, curRound, curUpTotalHuman, bigStage, refreshData, noExistsTable, Optional.ofNullable(allUpRankPlay).orElse(Collections.emptyList()).size()));

			if (!CollectionUtils.isEmpty(allUpRankPlay)) {
				clearingModelRes = buildResModelClearing(false, bigStage, allUpRankPlay, playingConfig, playing, upRoundResolve, curRoundResolve, rankCount, theNextStage, 1, curRate, null);
				//固定第一轮为淘汰赛,非淘汰赛阶段仅填充空余人数
				//淘汰赛结束,还有空闲房间
				clearingModelRes.setNoSplitUserOrder((isBreakWeedOutStep(upRoundResolve, rankCount) && curRoundResolve.getRoundLowScore() > 0) || (isBreakWeedOutStep(upRoundResolve, rankCount) && !noExistsTable));
				//不再创建新的房间
				clearingModelRes.setNoCreateTable((isBreakWeedOutStep(upRoundResolve, rankCount) && curRoundResolve.getRoundLowScore() > 0) || (isBreakWeedOutStep(upRoundResolve, rankCount) && !noExistsTable));

				List<Long> userIds = filterRankUpUser(allUpRankPlay);

				List<Long> resList = match(playing, playingConfig, Optional.ofNullable(userIds), clearingModelRes, String.valueOf(playing.getBindServerId()), competitionOpenMutilServerMatch);

				LogUtil.i("Competition|playing|clearing|matchPush|build1:" + userIds.size() + "|onlineUpRank:" + allUpRankPlay + "|matchRes:" + Optional.ofNullable(resList).orElse(Collections.emptyList()).size());

				//这里变更状态后,最后一轮有分数高的玩家会进行轮空
				if (!CollectionUtils.isEmpty(resList)/* && changeStatus*/) {
					updateInRoom(playing.getId(), resList, true);
				}
			}
		} catch (Exception e) {
			LogUtil.e("competition|playing|clearing|matchPush|error", e);
		}
	}

	/**
	 *@description 当前状态1晋级2淘汰, 淘汰约束: 当前赛事已经到下一个回合了剩余的人全部淘汰(这种情况一般来说不会出现,当前结算会等待所有牌桌结算完毕后做下一轮回合滚动)||每桌前n晋级验证||分数低于验证
	 *@param playing
	 *@param lowScoreOut 低于淘汰分淘汰
	 *@param notInTopNOut 不在topn淘汰
	 *@param v
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/13
	 */
	protected int roundWeedOutCheck(CompetitionPlayingDB playing, int lowScoreOut, int notInTopNOut, CompetitionClearingPlay v) {
		return ((notInTopNOut > 0 && v.getTableRank() <= notInTopNOut) || (lowScoreOut != 0 && v.getScore() > lowScoreOut) || (lowScoreOut == 0 && notInTopNOut == 0)) ? CompetitionScoreDetailStatusEnum.RISE_IN_RANK : CompetitionScoreDetailStatusEnum.WEED_OUT;
	}


	/**
	 *@description 赛事结算,大结
	 *@param riseInRankEndHuman 回合结束人数,到达人数就进入下一个轮次,留下晋级人数
	 *@param riseInRankHuman 回合晋级人数
	 *@param riseInRankScore 小结晋级分数
	 *@param riseInRankBeforeNumber 小结每桌前n名晋级
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/22
	 */
	public void bigClearingPlayingClearing(CompetitionPlayingDB playing, CompetitionClearingRiseInRankMode resolve) {
		//状态变更结果
		boolean changeStatus = false;
		try {
			//状态变更 开赛->清算
			if (changeStatus = competitionPlayingDao.updateStatus(playing.getId(), CompetitionPlayingStatusEnum.PLAYING, CompetitionPlayingStatusEnum.CLEARING, false, playing.getType()) > 0) {
//				//发送奖励
				playingClearingAwardSend(playing, resolve.getRoundEndHuman());

				///状态变更 清算->清算完毕
				competitionPlayingDao.updateStatus(playing.getId(), CompetitionPlayingStatusEnum.CLEARING, CompetitionPlayingStatusEnum.CLEARING_END, false, playing.getType());

				//清理所有房间
				playingBatchChangeClearingEndToEndStatus(playing.getId(), false);
			}
		} catch (Exception e) {
			LogUtil.e("Competition|playing|bigClearing|error|" + playing.getId() + "|", e);
			//这里出现异常手动处理,不能重复结算了
			competitionPlayingDao.updateStatus(playing.getId(), changeStatus ? CompetitionPlayingStatusEnum.CLEARING : CompetitionPlayingStatusEnum.PLAYING, CompetitionPlayingStatusEnum.CLEARING_ERROR, false, playing.getType());
		}
	}

	/**
	 *@description 赛事详细流水
	 *@param
	 *@param riseInRankScore 分数低于淘汰分
	 *@param riseInRankBeforeNumber 每桌可以晋级的前N名
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/4
	 */
	protected List<CompetitionScoreDetailDB> buildDbModelScoreDetail(CompetitionClearingModelReq build, CompetitionPlayingDB playing, int riseInRankScore, int riseInRankBeforeNumber, float scoreBasicRatio, boolean forceNoWeedOut) {
		return new ArrayList<>(build.getUsers()).stream().map(v -> CompetitionScoreDetailDB.builder()
						.userId(v.getUserId())
						.playingId(playing.getId())
						//当前积分
						.lastScore(Math.round(v.getScore() * scoreBasicRatio))
						//当前轮次
						.lastStep(build.getCurStep())
						//当前回合
						.lastRound(build.getCurRound())
						//当前牌桌内的排名
						.lastTableRank(v.getTableRank())
						//当前总排名
//						.lastRank(999999)
						.remark("")
						//当前状态1晋级2淘汰, 淘汰约束: 当前赛事已经到下一个回合了剩余的人全部淘汰(这种情况一般来说不会出现,当前结算会等待所有牌桌结算完毕后做下一轮回合滚动)||每桌前n晋级验证||分数低于验证
						.lastStatus(forceNoWeedOut ? 1 : roundWeedOutCheck(playing, riseInRankScore, riseInRankBeforeNumber, v))
						//当前积分换算比例
						.scoreBasicRatio(scoreBasicRatio)
						//淘汰策略,名次
						.weedTopNumber(riseInRankBeforeNumber)
						//淘汰策略,分数
						.weedTopScore(riseInRankScore)
						//保留结算房号
						.roomId(build.getCompetitionRoomKeyId())
						.build()
		).collect(Collectors.toList());
	}

	/**
	 *@description 结算奖励发放
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/5
	 */
	public void playingClearingAwardSend(CompetitionPlayingDB playing, int roundEndHuman) {
		//提取最后一个回合所剩余的玩家
		Optional<List<CompetitionScoreDetailDB>> competitionScoreDetailDBS = competitionScoreDetailDao.queryByRank(CompetitionScoreDetailDB.builder().limit(roundEndHuman)
				.playingId(playing.getId()).build());

		LogUtil.i("competition|playing|playingClearingAwardSend|" + roundEndHuman + "|" + competitionScoreDetailDBS.orElse(Collections.emptyList()).size());

		Optional<CompetitionPlayingConfigDB> playingConfig = competitionGeneratePlayingService.getPlayingConfig(playing.getPlayingConfigId());

		//没有配置结算奖励
		if(StringUtils.isBlank(playingConfig.get().getAwards())) {
			LogUtil.i("Competition|playing|NotFindAward|" + playing.getId());
			return;
		}

		HashMap<Long, Integer> userIdAndRank = new HashMap<>();

//		String[] rankAwards = playingConfig.get().getAwards().split("_");

		competitionJobSchedulerThreadPool.nSynBus(() -> {
			competitionScoreDetailDBS.get().forEach((v) -> {
//				singleSendAward(playing, rankAwards, v.getUserId(), v.getLastRank());
				try {
					userIdAndRank.put(v.getUserId(), v.getLastRank());
				} catch (Exception e) {
					LogUtil.e("competition|playing|runnginHorseLightPutError", e);
				}
			});

			//冠军跑马灯
			runningHorseLight(playing, userIdAndRank);
		});
	}

	/**
	 *@description 发送奖励
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/6
	 */
	public void singleSendAward(CompetitionPlayingDB playing, String[] rankAwards, Long userId, int rank) {
		try {
			//电脑不发送奖励
			if (/*userId <= 0 || */(rank - 1) >= rankAwards.length) {
//				LogUtil.i("Competition|playing|AwardInfo|return|" + userId + "|" + rank + "|" + playing.getId() + "|" + rankAwards);
				return;
			}

			Integer awardId = 0;
			Integer awardVal = 0;
			String[] rankAward = null;
			if ((rank - 1) < rankAwards.length) {
				//异步发送奖励
				rankAward = rankAwards[rank - 1].split(",");
				//奖励id
				awardId = NumberUtils.toInt(rankAward[1]);
				//奖励值
				awardVal = NumberUtils.toInt(rankAward[2]);
			}

			long newAwardId = competitionClearingAwardDao.insert(CompetitionClearingAwardDB.builder()
					.playingId(playing.getId())
					.rank(rank)
					.userId(userId)
					.status(0)
					.awardId(awardId)
					.awardVal(awardVal)
					.build());

			boolean res = false;
			if (awardId > 0 && userId > 0) {
				// 发送奖励
				res = awardSend(playing, userId, awardId, awardVal);
			}

			if (res)	//到账状态
				competitionClearingAwardDao.updateStatus(newAwardId, 0, 1);

			LogUtil.i("Competition|playing|AwardInfo|" + userId + "|" + rank + "|" + playing.getId() + "|" + rankAwards + "|" + rankAward + "|" + awardId + "|" + awardVal + "|" + newAwardId);
		} catch(Exception e) {
			LogUtil.e("Competition|playing|AwardInfo|error|" + playing.getId() + "|" + Arrays.toString(rankAwards) + "|" + userId + "|" + rank, e);
		}
	}

	/**
	 *@description 消耗校验
	 *@param
	 *@return true消耗成功
	 *@author Guang.OuYang
	 *@date 2020/5/20
	 */
	public boolean awardSend(CompetitionPlayingDB playing, long userId, int consumerId, int consumerVal) {
		if (userId < 0) {
			return isDebug;
		}
		switch (consumerId) {
			case 1:        //白金点
				return competitionPushService.changeGoldPush(Arrays.asList(userId), playing.getCategory(), consumerVal,17).equalsIgnoreCase("1");
			case 2:        //礼券
				return competitionPushService.changeGoldPush(Arrays.asList(userId), playing.getCategory(), consumerVal,26).equalsIgnoreCase("1");
				//UserDao.getInstance().addUserGoldBean(userId, UserDao.getInstance().loadUserGoldBean(userId), SourceType.COMPETITION_AWARD) > 0;
		}
		return false;
	}

	/**
	 *@description 清空已经结算的比赛
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/22
	 */
	protected void nSynClearingTable(CompetitionPlayingDB playing) {
		competitionJobSchedulerThreadPool.nSynBus(() -> {
			//清理所有空余房间
			playingBatchChangeClearingEndToEndStatus(playing.getId(), true, playing.getCurStep(), playing.getCurRound());
		});
	}

	/**
	 *@description 清空已经结算的比赛
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/22
	 */
	public void playingBatchChangeClearingEndToEndStatus(Long playingId, boolean force) {
		 playingBatchChangeClearingEndToEndStatus(playingId, force, 0, 0);
	}

	public void playingBatchChangeClearingEndToEndStatus(Long playingId, boolean force, int curStep, int curRound) {
		LogUtil.i("Competition|playing|playingBatchChangeClearingEndToEndStatus|" + playingId);

		try {
			List<CompetitionRoom> competitionRooms = competitionRoomDao.queryNotStartRooms(playingId);
			if (!CollectionUtils.isEmpty(competitionRooms)) {

				Map<Long, List<CompetitionRoom>> allRooms = competitionRooms.stream().collect(Collectors.groupingBy(CompetitionRoom::getServerId));

				CountDownLatch countDownLatch = new CountDownLatch(allRooms.size());
				for (Long serverId : allRooms.keySet()) {
					competitionJobSchedulerThreadPool.nSynBus(() -> {
						playingBatchChangeClearingEndToEndStatus(playingId, serverId, force, curStep, curRound);
						countDownLatch.countDown();
					});
				}
				try {
					countDownLatch.await();
				} catch (InterruptedException e) {
				}
			}
			competitionRoomUserDao.deleteCompetitionRoomUser(playingId);

			if (force) {
				competitionPlayingDao.updateStatus(playingId, CompetitionPlayingStatusEnum.PLAYING, CompetitionPlayingStatusEnum.CLEARING_END, false, 0);
			}
		} catch (Exception e) {
			LogUtil.e("competition|playingBatchChangeClearingEndToEndStatusErr",e);
		}

	}

	/**
	 *@description 清空已经结算的比赛
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/22
	 */
	public void playingBatchChangeClearingEndToEndStatus(Long playingId, Long serverId, boolean force,int curStep, int curRound) {
		//批量变更已经结算掉的活动
//		competitionPlayingDao.batchUpdateStatus(CompetitionPlayingStatusEnum.CLEARING_END, CompetitionPlayingStatusEnum.END);

		competitionPushService.clearInvalidRoom(CompetitionPushModel.builder()
				.userIds(Arrays.asList(-1l))
				.bindServerId(serverId)
				.playingId(playingId).build(),force,curStep,curRound);
	}

	/**
	 *@description 冠军跑马灯
	 *@param champion userId->rank
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/20
	 */
	public void runningHorseLight(CompetitionPlayingDB playing, Map<Long, Integer> champion) {
		try {
			if((playing.getBeginPlayingRHLPushStatus() == null || playing.getBeginPlayingRHLPushStatus().indexOf("c1") <= -1) && champion.size() > 0){
				Optional<CompetitionPlayingConfigDB> playingConfig = competitionGeneratePlayingService.getPlayingConfig(playing.getPlayingConfigId());
				if (playingConfig != null && playingConfig.isPresent()) {
					CompetitionPlayingConfigExtModel competitionPlayingConfigExtModel = null;
					if (StringUtils.isNotBlank(playingConfig.get().getExtModel())) {
						competitionPlayingConfigExtModel = JSONObject.parseObject(playingConfig.get().getExtModel(), CompetitionPlayingConfigExtModel.class);

						List<RegInfo> userForList = UserDao.getInstance().getUserForList(new ArrayList<Long>(champion.keySet()), true);

						CompetitionRunningHorseLightDB runningHorseLightChampionModel = competitionPlayingConfigExtModel.getRunningHorseLightChampionModel();

						if(runningHorseLightChampionModel != null){
							String content = runningHorseLightChampionModel.getContent();

							Iterator<RegInfo> iterator = userForList.iterator();
							while (iterator.hasNext()) {
								RegInfo next = iterator.next();
								content = String.format(content.replace("{rankName" + champion.get(next.getUserId()) + "}", "%s"), next.getName());
								content = String.format(content.replace("(rankName" + champion.get(next.getUserId()) + ")", "%s"), next.getName());
							}

							runningHorseLightChampionModel.setContent(content);

							if(runningHorseLightChampionModel != null){
								CompetitionRunningHorseLightDB competitionRunningHorseLightDB = runningHorseLightChampionModel.tempArgsFill(playing);
                                //十分钟有效时间
                                competitionRunningHorseLightDB.setETime(new SimpleDateFormat(DateUtil.STRING_DATE_FORMAT).format(new Date().getTime() + (5 * 60 * 1000)));
								//冠军跑马灯
								competitionRunningHorseLightService.addLight(competitionRunningHorseLightDB);
							}
						}

						//底部横幅
						if (StringUtils.isNotBlank(competitionPlayingConfigExtModel.getBottomTitle())) {
							CompetitionRunningHorseLightDB competitionRunningHorseLightDB = CompetitionRunningHorseLightDB.builder().build().tempArgsFill(playing);
							String showTimeSeconds = competitionPlayingConfigExtModel.getBottomTitle().split(",")[0];
							competitionRunningHorseLightDB.setType(2);
							competitionRunningHorseLightDB.setContent(competitionPlayingConfigExtModel.getBottomTitle());
							competitionRunningHorseLightDB.setETime(new SimpleDateFormat(DateUtil.STRING_DATE_FORMAT).format(System.currentTimeMillis() + (Long.valueOf(showTimeSeconds) * 1000)));

							String content = competitionRunningHorseLightDB.getContent();
							Iterator<RegInfo> iterator = userForList.iterator();
							while (iterator.hasNext()) {
								RegInfo next = iterator.next();
								content = String.format(content.replace("(userName" + champion.get(next.getUserId()) + ")", "%s"), next.getName());
								content = String.format(content.replace("(userId" + champion.get(next.getUserId()) + ")", "%s"), next.getUserId());
							}

							competitionRunningHorseLightDB.setContent(content);

							competitionRunningHorseLightService.addLight(competitionRunningHorseLightDB);
						}

						//赛前推送
						competitionPlayingDao.update(CompetitionPlayingDB.builder().id(playing.getId()).beginPlayingRHLPushStatus(playing.getBeginPlayingRHLPushStatus() + "_c1").build());
						LogUtil.i("competition|playing|runningHorseLight|c1|"+playing.getId()+"|"+champion);
					}
				}
			}
		} catch (SQLException e) {
			LogUtil.e("competition|runningHorseLight|champion|error", e);
		}
	}

	public Optional<CompetitionPlayingDB> getPlaying(long playingId) {
		return competitionPlayingDao.queryForSingle(CompetitionPlayingDB.builder().id(playingId).build());
	}

	public HistoryParam getHistory(Long userId) {
		return competitionScoreTotalDao.queryByHistory(userId).orElse(null);
	}

	public CompetitionClearingModelRes playCheck(Long userId) {
		CompetitionClearingModelRes res = null;
		try {
			if (UserDao.getInstance().getUser(userId.longValue()).getPlayingTableId() > 0) {
				return res;
			}
			Optional<Long> optional = competitionApplyDao.queryByUserPlay(userId);
			if(optional.isPresent()){
				Optional<CompetitionPlayingDB> playingDBOptional = competitionPlayingDao.queryForSingle(CompetitionPlayingDB.builder().id(optional.get()).build());
				if(!playingDBOptional.isPresent()){
					return res;
				}
				CompetitionPlayingDB playing = playingDBOptional.get();

				//正在比赛中
				if (playing.getStatus() == CompetitionPlayingStatusEnum.PLAYING) {
					List<CompetitionScoreDetailDB> dbAll = competitionScoreDetailDao.queryForList(
							CompetitionScoreDetailDB.builder()
									.playingId(playing.getId())
									.lastStep(playing.getCurStep())
									.lastRound(playing.getCurRound())
									.userId(userId)
									.build()).get();

					if(!CollectionUtils.isEmpty(dbAll)){
						CompetitionPlayingConfigDB playingConfig = competitionGeneratePlayingService.getPlayingConfig(playing.getPlayingConfigId()).get();
						CompetitionClearingRiseInRankMode upRoundResolve = CompetitionClearingRiseInRankMode.resolve(playing, playingConfig, null);

						List<CompetitionClearingPlay> currentStepAllPlay = new ArrayList<>();
						if(!CollectionUtils.isEmpty(dbAll)) {
							CompetitionScoreDetailDB v = dbAll.get(0);
							if (v.getLastStatus() == CompetitionScoreDetailStatusEnum.WEED_OUT) {
								return res;
							}

							CompetitionScoreTotalDB rankForSelf = competitionRankService.getRankForSelf(CompetitionScoreTotalDB.builder().userId(userId).playingId(playing.getId()).build());
							currentStepAllPlay.add(CompetitionClearingPlay.builder()
									.userId(v.getUserId())
									.score(v.getLastScore())
									.tableRank(v.getLastTableRank())
									.rank(v.getLastRank() == null ? rankForSelf.getRank() : v.getLastRank())
									.isOver(1)
									.nextStep(true)
									.build());

							//全部牌桌完成后的结算数据
							res = buildResModelClearing(
									true,
									false,
									currentStepAllPlay,
									playingConfig,
									playing,
									upRoundResolve,
									upRoundResolve,
									0,
									false,
									1,
									upRoundResolve.getBaseScore(),
									//各玩家排名 UserId->CompetitionScoreTotalDB
									currentStepAllPlay.stream().map(v1 ->
											CompetitionScoreTotalDB.builder()
													.userId(v1.getUserId())
													.rank(v1.getRank())
													.build())
											.collect(Collectors.groupingBy(CompetitionScoreTotalDB::getUserId)));
							res.setResult(3);
							if (UserDao.getInstance().getUser(userId.longValue()).getPlayingTableId() > 0) {
								return null;
							}

							//这里的积分比例存入赛事流水时就已经换算过
							//批量推送最终晋级成功或失败结算界面

							LogUtil.i("competition|playCheck|"+userId);
							return res;
						}
					}

				}
			}
		} catch (SQLException e) {
		}

		return res;
	}

	/**
	 *@description 清算条件模型
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/6
	 */
	@Data
	@Builder
	public static class CompetitionClearingRiseInRankMode {
		protected long playingId;
		protected int curStep;
		protected int curRound;
		protected int stepLength;					//赛制总长度, 总轮次
		protected int roundLength;					//当前赛制总长度
		protected int roundEndHuman;				//回合结束人数,到达人数就进入下一个轮次,留下晋级人数
		protected int roundUpLevelHuman;			//回合晋级人数
		protected int roundLowScore;           		//小结低于则淘汰分
		protected int nextStepNotInTopNOut;			//小结每桌前n名晋级
		protected int baseScore; 					//当前底分倍率
		protected float convertRate;				//当前需要转化的比例
		protected boolean finalConfig;				//最后一个配置

		protected int roomExistsCycle;              //房间存在时间

		private CompetitionClearingRiseInRankMode src;

		public static CompetitionClearingRiseInRankMode resolve(CompetitionPlayingDB playing, CompetitionPlayingConfigDB playingConfig, CompetitionClearingModelReq req) {
			if (playing.getType() == CompetitionPlayingTypeEnum.APPLY_PLAYING) {
				return resolve1(playing, playingConfig);
			}
			return resolve2(playing, playingConfig, req);
		}


		/**
		 *@description 定时赛配置解析
		 *@param
		 *@return
		 *@author Guang.OuYang
		 *@date 2020/7/21
		 */
		public static CompetitionClearingRiseInRankMode resolve2(CompetitionPlayingDB playing, CompetitionPlayingConfigDB playingConfig, CompetitionClearingModelReq req) {
			//在默认配置上增加时间因子
			CompetitionClearingRiseInRankMode competitionClearingRiseInRankMode = resolve1(playing, playingConfig);

			CompetitionPlayingConfigExtModel extModelPojo = playingConfig.getExtModelPojo();
			//淘汰阶段
			if (req != null && extModelPojo != null && competitionClearingRiseInRankMode.getRoundLowScore() > 0) {
				String secondWeedOut = extModelPojo.getSecondWeedOut();
				if (secondWeedOut != null) {
					//当前人数
					int curHuman = playing.getCurHuman();

					//截止房间结算开赛时间秒
//					long roomExistsCycle = (req.getCurrentClearingMills() - playing.getOpenTime().getTime()) / 1000;
					long roomExistsCycle = (System.currentTimeMillis() - playing.getOpenTime().getTime()) / 1000;

					//人数_分钟,时间,底分,淘汰分_...;
					String[] minsData = secondWeedOut.split(";");    //各个分钟数据

					int baseBasic = competitionClearingRiseInRankMode.getBaseScore();
					int baseTotal = baseBasic;

					int weedOutBasic = competitionClearingRiseInRankMode.getRoundLowScore();
					int weedOutTotal = weedOutBasic;

					for (String minData : minsData) {
						String[] singleData = minData.split("_");

						int maxHuman = Integer.valueOf(singleData[0]);

						//人数区间
						if (maxHuman >= curHuman) {
							//上一个时间区间
							int upTimeSection = 0;
							int upTimeScheduler = 0;
							int upBase = 0;
							int upWeedOut = 0;

							int timeSection = 0;
							int timeScheduler = 0;
							int base = 0;
							int weedOut = 0;
							for (int i = 1; i < singleData.length; i++) {
								//0人数,1时间,多少秒调度,底分,淘汰分
								String[] s = singleData[i].split(",");

								//时间区间
								timeSection = Integer.valueOf(s[0]) * 60;
								//n秒调度
								timeScheduler = Integer.valueOf(s[1]);
								base = Integer.valueOf(s[2]);
								weedOut = Integer.valueOf(s[3]);

								if (i != 1) {
									if (roomExistsCycle > timeSection) {
										long needCycleSeconds = (timeSection - upTimeSection) / upTimeScheduler;
										baseTotal += upBase * Math.max(needCycleSeconds, 1);
										weedOutTotal += upWeedOut * Math.max(needCycleSeconds, 1);

//										LogUtil.i("底分: 淘汰分: " );
//										LogUtil.i(" " + (timeSection / 60) + "分钟0秒:" + " " + baseTotal + " " + weedOutTotal);
									} else {
										long needCycleSeconds = roomExistsCycle - upTimeSection;
										baseTotal += upBase * Math.max(needCycleSeconds / timeScheduler, 0);
										weedOutTotal += upWeedOut * Math.max(needCycleSeconds / timeScheduler, 0);
//										LogUtil.i("底分: 淘汰分: " );
//										LogUtil.i(" " + ((roomExistsCycle / 60) + "分钟" + (roomExistsCycle % 60)) + "秒:" + " " + baseTotal + " " + weedOutTotal);
									}
								}

								upTimeSection = timeSection;
								upTimeScheduler = timeScheduler;
								upBase = base;
								upWeedOut = weedOut;
							}

							if (roomExistsCycle > timeSection) {
								long needCycleSeconds = (roomExistsCycle - timeSection) / timeScheduler;
								baseTotal += base * Math.max(needCycleSeconds, 0);
								weedOutTotal += weedOut * Math.max(needCycleSeconds, 0);
//								LogUtil.i("底分: 淘汰分: " );
//								LogUtil.i(" " + ((roomExistsCycle / 60) + "分钟" + (roomExistsCycle % 60)) + "秒:" + " " + baseTotal + " " + weedOutTotal);
							}

							break;
						}
					}

					competitionClearingRiseInRankMode.setRoomExistsCycle((int) roomExistsCycle);

					if (competitionClearingRiseInRankMode.getBaseScore() > 0) {
						//重新计算底分
						competitionClearingRiseInRankMode.setBaseScore(baseTotal);
					}

					if (competitionClearingRiseInRankMode.getRoundLowScore() > 0) {
						//重新计算淘汰分
						competitionClearingRiseInRankMode.setRoundLowScore(weedOutTotal);
					}

					LogUtil.i("competition|clearing|resolve|" + playing.getId() + "|" + playing.getCurStep() + "|" + playing.getCurRound() + "|" + competitionClearingRiseInRankMode);
				}

			}

			return competitionClearingRiseInRankMode;
		}

		/**
		 *@description 人满开赛配置解析
		 *@param
		 *@return
		 *@author Guang.OuYang
		 *@date 2020/7/21
		 */
		public static CompetitionClearingRiseInRankMode resolve1(CompetitionPlayingDB playing, CompetitionPlayingConfigDB playingConfig) {
			//赛制总长度, 总轮次
			String[] scaleTotal = playingConfig.getStepRoundDesc().split(";");

			//最后一个长度
			boolean finalConfig = playing.getCurStep() == scaleTotal.length;

			//这里是大结算了,后面没有淘汰策略了
			if(playing.getCurStep() - 1 >= scaleTotal.length) {
				CompetitionClearingRiseInRankMode build = CompetitionClearingRiseInRankMode.builder()
						.roundEndHuman(-1)//回合结束人数,到达人数就进入下一个轮次,留下晋级人数
						.roundUpLevelHuman(-1)            //回合晋级人数
						.roundLowScore(-1)    //小结低于则淘汰分
						.nextStepNotInTopNOut(-1)//小结每桌前n名晋级
						.stepLength(scaleTotal.length)
						.roundLength(-1)
						.finalConfig(true)
						.build();
				CompetitionClearingRiseInRankMode competitionClearingRiseInRankMode = CompetitionClearingRiseInRankMode.builder().build();
				try {
					BeanUtils.copyProperties(competitionClearingRiseInRankMode, build);
				} catch (Exception e) {
				}
				build.setSrc(competitionClearingRiseInRankMode);
				return build;
			}

			//当前赛制
			String[] scaleOne = scaleTotal[playing.getCurStep() - 1].split(",");

			//淘汰策略 低于多少分_仅晋级每桌n名
			String[] weedOutStrategy = (scaleOne.length <= (1 + playing.getCurRound()) ? scaleOne[scaleOne.length - 1] : scaleOne[(1 + playing.getCurRound())]).split("_");

			float convertRate = 1;
			if (!StringUtils.isBlank(playingConfig.getStepConvertRate()) ) {
				String[] totalConvertRate = playingConfig.getStepConvertRate().split(";");
				//
				if (totalConvertRate.length >= playing.getCurStep()) {
					String[] roundConvertRate = totalConvertRate[playing.getCurStep()-1].split(",");

					convertRate = NumberUtils.toFloat(roundConvertRate[playing.getCurRound() > roundConvertRate.length ? roundConvertRate.length - 1 : playing.getCurRound() - 1]);
				}
			}

			CompetitionClearingRiseInRankMode build = CompetitionClearingRiseInRankMode.builder()
					.playingId(playing.getId())
                    .curStep(playing.getCurStep())
                    .curRound(playing.getCurRound())
					.roundEndHuman(ParamCheck.check(scaleOne[0], NumberUtils::toInt))//回合结束人数,到达人数就进入下一个轮次,留下晋级人数
					.roundUpLevelHuman(ParamCheck.check(scaleOne[1], NumberUtils::toInt))            //回合晋级人数
					.roundLowScore(ParamCheck.check(weedOutStrategy[0], NumberUtils::toInt))    //小结低于则淘汰分
					.nextStepNotInTopNOut(ParamCheck.check(weedOutStrategy[1], NumberUtils::toInt))//小结每桌前n名晋级
					.stepLength(scaleTotal.length)
					.roundLength(scaleOne.length - 2)
					.baseScore(getCurRoundRate(playing.getCurStep(), playing.getCurRound(), playingConfig, 1))
					.finalConfig(finalConfig)
					.convertRate(convertRate)
					.build();
			CompetitionClearingRiseInRankMode competitionClearingRiseInRankMode = CompetitionClearingRiseInRankMode.builder().build();
			try {
				BeanUtils.copyProperties(competitionClearingRiseInRankMode, build);
			} catch (Exception e) {
			}
			build.setSrc(competitionClearingRiseInRankMode);
			return build;
		}

		public CompetitionAward getAward(CompetitionPlayingConfigDB playingConfig, int rank) {
			Integer awardId = null;
			Integer awardVal = null;
			try {
				String[] rankAwards = playingConfig.getAwards().split("_");

				if (rankAwards.length < rank || rank <= 0) {
					return null;
				}

				//异步发送奖励
				String[] rankAward = rankAwards[rank - 1].split(",");
				//奖励id
				awardId = NumberUtils.toInt(rankAward[1]);
				//奖励值
				awardVal = NumberUtils.toInt(rankAward[2]);

				return CompetitionAward.builder().rank(rank).id(awardId).val(awardVal).build();
			}  catch (Exception e) {
				LogUtil.e("competition|bigOver|getAwardError|" + playingConfig.getAwards() + "|" + rank + "|", e);
			}

			return null;
		}
	}

	@Data
	@Builder
	public static class CompetitionAward{
		protected int rank;
		protected int id;
		protected int val;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CallBackInvoke<T> {
		public Runnable before;
		public Runnable middle;
		public Runnable after;
		public T result;

		public void beforeInvoke() {
			if (before != null) before.run();
		}

		public void middleInvoke() {
			if (middle != null) middle.run();
		}

		public void afterInvoke() {
			if (after != null) after.run();
		}
	}


	@Data
	@Builder
	public static class MathQueueParam{
		CompetitionClearingRiseInRankMode upRoundResolve;
		CompetitionClearingRiseInRankMode curRoundResolve;
		private boolean bigStage;
		private Long playingId;
		private boolean noExistsTable;
		private int curUpTotalHuman;
		private boolean theNextStage;
		private int curRate;
		private List<CompetitionClearingPlay> allPlays;
	}

}
