package com.sy.sanguo.game.competition.service;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.LangMsg;
import com.sy.sanguo.game.bean.ServerConfig;
import com.sy.sanguo.game.competition.dao.CompetitionApplyDao;
import com.sy.sanguo.game.competition.dao.CompetitionPlayingConfigDao;
import com.sy.sanguo.game.competition.dao.CompetitionPlayingDao;
import com.sy.sanguo.game.competition.dao.CompetitionRoomDao;
import com.sy.sanguo.game.competition.exception.MessageAssert;
import com.sy.sanguo.game.competition.exception.MessageException;
import com.sy.sanguo.game.competition.exception.MessageExceptionParam;
import com.sy.sanguo.game.competition.job.CompetitionJobSchedulerThreadPool;
import com.sy.sanguo.game.competition.model.db.CompetitionPlayingConfigDB;
import com.sy.sanguo.game.competition.model.db.CompetitionPlayingDB;
import com.sy.sanguo.game.competition.model.enums.CompetitionPlayingStatusEnum;
import com.sy.sanguo.game.competition.model.enums.CompetitionPlayingTypeEnum;
import com.sy.sanguo.game.competition.model.param.CompetitionPlayingParam;
import com.sy.sanguo.game.competition.model.param.PlayingList;
import com.sy.sanguo.game.competition.util.LockSharing;
import com.sy.sanguo.game.competition.util.ParamCheck;
import com.sy.sanguo.game.competition.util.ParamCheck.ConsumerC;
import com.sy.sanguo.game.dao.ServerDaoImpl;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 主赛场生成管理
 * @author Guang.OuYang
 * @date 2020/5/20-18:07
 */
@Service
public class CompetitionGeneratePlayingService {
	@Autowired
	private CompetitionPlayingService competitionPlayingService;

	@Autowired
	private CompetitionPlayingDao<CompetitionPlayingDB> competitionPlayingDao;

	@Autowired
	private CompetitionRoomDao competitionRoomDao;

	@Autowired
	private CompetitionApplyDao competitionApplyDao;

	@Autowired
	private CompetitionApplyService competitionApplyService;

	@Autowired
	private CompetitionPlayingConfigDao<CompetitionPlayingConfigDB> competitionPlayingConfigDao;

	@Autowired
	private CompetitionJobSchedulerThreadPool competitionJobSchedulerThreadPool;

	/**赛事配置的缓存,该配置不会太多*/
	private static final ConcurrentHashMap<Long, Optional<CompetitionPlayingConfigDB>> playingConfigCache = new ConcurrentHashMap<>();

	/**
	 *@description 赛事配置
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/2
	 */
	public Optional<CompetitionPlayingConfigDB> getPlayingConfig(Long playingConfigId) {
		if (!playingConfigCache.containsKey(playingConfigId)) {
			Optional<CompetitionPlayingConfigDB> competitionPlayingConfigDBOptional = competitionPlayingConfigDao.queryForSingle(CompetitionPlayingConfigDB.builder().id(playingConfigId).build());
			if (competitionPlayingConfigDBOptional.isPresent())
				playingConfigCache.put(playingConfigId, competitionPlayingConfigDBOptional);
		}

		return playingConfigCache.get(playingConfigId);
	}

	/**
	 *@description 清空配置缓存
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/2
	 */
	public void clearConfigCache() {
		playingConfigCache.clear();
	}

	/**
	 *
	 * */
	public int closeConfig(CompetitionPlayingConfigDB arg) {
		return competitionPlayingConfigDao.closeOpen(CompetitionPlayingConfigDB.builder().titleType(arg.getTitleType()).type(arg.getType()).category(arg.getCategory()).entrance(arg.getEntrance()).deleteTime(new Date()).build());
	}

	/**
	 *
	 * */
	public int openConfig(CompetitionPlayingConfigDB arg) {
		return competitionPlayingConfigDao.closeOpen(
				CompetitionPlayingConfigDB.builder()
						.titleType(arg.getTitleType())
						.type(arg.getType())
						.category(arg.getCategory())
						.entrance(arg.getEntrance())
						.deleteTime(null).build());
	}

	/**
	 *
	 * */
//	public int delConfig(CompetitionPlayingConfigDB arg) {
//		return competitionPlayingConfigDao.del(CompetitionPlayingConfigDB.builder().titleType(arg.getTitleType()).type(arg.getType()).category(arg.getCategory()).entrance(arg.getEntrance()).build());
//	}

	/**
	 *@description 找到最近一场类型的比赛, 生成出下一场, 这里仅检测定时赛
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/22
	 */
	public void findAndGeneratePlaying() {
		//读取所有配置
		Optional<List<CompetitionPlayingConfigDB>> optionalCompetitionPlayingConfigDB = competitionPlayingConfigDao.queryForList(CompetitionPlayingConfigDB.builder()
//				.iteration(true)
//				.type(CompetitionPlayingTypeEnum.TIMER_PLAYING)
				.build());
		optionalCompetitionPlayingConfigDB.ifPresent((configList) -> {
			configList.stream().filter(v -> v.getDeleteTime() == null).forEach(this::findAndGeneratePlaying);
		});
	}

	/**
	 *@description 生成该配置的比赛
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/2
	 */
	public void findAndGeneratePlaying(long configId) {
		findAndGeneratePlaying(getPlayingConfig(configId).get());
	}

	private void findAndGeneratePlaying(CompetitionPlayingConfigDB config) {
		if (config.getDeleteTime() != null) {
			return;
		}

		LockSharing.lock(competitionPlayingConfigDao, config.getId(), () -> {
			//定时赛,默认处于报名中,需要保证总有下一场报名可用
			//保证下面有两场没有初始化过的备用
//			if (competitionPlayingDao.queryByCount(CompetitionPlayingDB.builder().playingConfigId(config.getId()).status(CompetitionPlayingStatusEnum.INIT).build()) < 2) {
			//报名赛当赛事小于两场时就生成一场
			//定时赛当赛事小于一场时就生成一场
			int i = (isApplyPlaying(config::getType) ? 2 : 2) - competitionPlayingDao.queryByCount(CompetitionPlayingDB.builder().playingConfigId(config.getId()).status(CompetitionPlayingStatusEnum.APPLYING).build());
			while (i-- > 0) {
				//获取当前配置最新的赛场信息
				//如果获取不到赛场,则配置生成赛场时失败,这里不生成
				Optional<CompetitionPlayingDB> configIdLastPlaying = competitionPlayingDao.queryByConfigId(config.getId());
				if (configIdLastPlaying.isPresent()) {
					if(!config.getIteration()){
						return;
					}
					//转换参数,当前赛事->下一场赛事
					CompetitionPlayingDB next = playingDBFormatPlayingDB(config, configIdLastPlaying.get(), true);
					//生成赛事场次,这里可能在上一次就已经生成了,这里不做检测
					genPlaying(next, true);
				} else/* if(competitionPlayingService.isDebug())*/ {
					genPlaying(config, i != 1, false);
				}
			}
		});
	}

	/**
	 *@description 增加一个赛事配置
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/22
	 */
	public void addNewConfig(CompetitionPlayingConfigDB param) {
		//必要参数校验
		MessageAssert.assertFalse(ParamCheck.isMultilOrNull(
				param::getTitleType,
				param::getType,
				param::getCategory,
				param::getStepRate,
				param::getStepRoundDesc,
				param::getRoomConfigIds,
				param::getEntrance,
				param::getLoginCallBackUrl
				),
				MessageExceptionParam.builder().code(-1).msg(LangMsg.getMsg(LangMsg.code_3)).throwException(true).build());

		//定时赛校验时间
		MessageAssert.assertFalse(isTimerPlaying(param::getType) && ParamCheck.isMultilOrNull(
//				param::getApplyBefore,
				param::getMatchBefore,
				param::getApplyBeforeMin,
				param::getBeforeXMinPushTitle
				),
				MessageExceptionParam.builder().code(-1).msg("开赛时间不能为空,开赛前n分钟开赛不能为空,开赛推送标题不能为空").throwException(true).build());

		boolean existsConfig = competitionPlayingConfigDao.queryByCount(CompetitionPlayingConfigDB.builder()
				.titleType(param.getTitleType())
				.type(param.getType())
				.category(param.getCategory())
				.entrance(param.getEntrance())
				.build()) > 0;

//		MessageAssert.assertFalse(existsConfig, MessageExceptionParam.builder().code(-1).msg("赛事配置失败,存在同样赛事!").throwException(true).build());

		if(isApplyPlaying(param::getType)){
			param.setIterationType(null);
			param.setIterationMin(null);
			param.setApplyBefore(null);
			param.setApplyAfter(null);
			param.setMatchBefore(null);
			param.setMatchAfter(null);
			param.setIterationType(null);
			param.setStartBeforeNotifyExt(null);
			param.setApplyBeforeMin(0);
		}

		//生成
		if(!existsConfig){
			MessageAssert.assertFalse(competitionPlayingConfigDao.insert(param) > 0, MessageExceptionParam.builder().msg("赛事配置成功!").build());

			//生成
			genPlaying(param, false);

			PlayingList playingList = competitionPlayingService.playingList(CompetitionPlayingParam.builder().titleType(param.getTitleType()).playingType(param.getType()).category(param.getCategory()).entrance(param.getEntrance()).build());

			CompetitionPlayingDB competitionPlayingDB = CollectionUtils.isEmpty(playingList.getArgList()) ? null : playingList.getArgList().get(0);

			//生成检查, 肯定会存在一场
			if (param.getIteration() || (competitionPlayingDB == null || competitionPlayingDB.getMatchBefore().getTime() <= new Date().getTime())) {
				//下一场
				genPlaying(param, true);
			}
		} else {    //变更
			//变更比赛细节
			Optional<CompetitionPlayingConfigDB> optional = competitionPlayingConfigDao.queryForSingle(CompetitionPlayingConfigDB.builder().titleType(param.getTitleType()).type(param.getType()).category(param.getCategory()).entrance(param.getEntrance()).build());
			if (optional.isPresent()) {
				param.setId(optional.get().getId());
				//老的比赛流局
				if (param.isUpdateAndDelOldPlaying()) {
					//找到还没有开局最新的赛事,全部退赛后重新生成新的比赛
					//已经开局的不支持退赛退费和删除
					PlayingList playingList = competitionPlayingService.playingList(CompetitionPlayingParam.builder().titleType(param.getTitleType()).playingType(param.getType()).category(param.getCategory()).entrance(param.getEntrance()).build());
					if (!CollectionUtils.isEmpty(playingList.getArgList())) {
						Iterator<CompetitionPlayingDB> iterator = playingList.getArgList().iterator();
						while (iterator.hasNext()) {
							CompetitionPlayingDB next = iterator.next();
							//老的退赛退费
							competitionPlayingService.caseOffCheckIn(next);
							competitionPlayingDao.deleteForReal(next);
						}
					}
					//比赛配置变更所有内容,后续生成的赛事会变更具体转化为新的赛事
					MessageAssert.assertFalse(competitionPlayingConfigDao.update(param) > 0, MessageExceptionParam.builder().msg("赛事更新成功!").build());
					clearConfigCache();
					findAndGeneratePlaying(param.getId());
				} else {
					CompetitionPlayingDB build = CompetitionPlayingDB.builder().build();
					BeanUtils.copyProperties(param, build);
					//已经生成的比赛仅变更细节,不允许变更当前匹配时间
					competitionPlayingDao.updateByType(build);
					//比赛配置变更所有内容,后续生成的赛事会变更具体转化为新的赛事
					MessageAssert.assertFalse(competitionPlayingConfigDao.update(param) > 0, MessageExceptionParam.builder().msg("赛事更新成功!").build());
					clearConfigCache();
				}
			}

		}
	}


	/**
	 *@description 生成一个赛事
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/28
	 */
	public void genPlaying(CompetitionPlayingConfigDB param, boolean next) {
		genPlaying(param, next, true);
	}

	/**
	 *@description 生成一个赛事
	 *@param  param
	 *@param  next true下一场 false当前场
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/22
	 */
	public void genPlaying(CompetitionPlayingConfigDB param, boolean next, boolean lock) {
		if(lock){
			LockSharing.lock(competitionPlayingConfigDao, param.getId(), () -> {
				try {
					//生成场次, 这里会校验类型和报名时间
					genPlaying(configDBFormatPlayingDB(param, next), true);
				} catch (Exception e) {
					GameBackLogger.SYS_LOG.error(e);
				}
			});
		} else {
			//生成场次, 这里会校验类型和报名时间
			genPlaying(configDBFormatPlayingDB(param, next), true);
		}
	}

	/**
	 *@description 实际生成赛场
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/22
	 */
	private CompetitionPlayingDB genPlaying(CompetitionPlayingDB playing, boolean insert) {
		//没有这个记录 生成
		//定时赛存在时间约束 唯一生成虚拟约束 类型+子类型+报名时间
//		if (genPlayingCheck(playing)) {
		//赛事绑定一个服务器ID
		//后续由服务器群拉玩家时拉入该服务器
//		if (playing.getBindServerId() == null) {
			//这一场赛事从当前步长中寻找一个游戏配置,绑定到该服务器,赛事中的所有配置要求处于同一服务器
			Long roomConfigId = competitionPlayingService.resolveRoomConfigId(playing.getCurStep(), playing.getCurRound(), getPlayingConfig(playing.getPlayingConfigId()).get());
			ServerConfig onlineCountMinServer = getOnlineCountMinServer(roomConfigId);
			playing.setBindServerId((long) onlineCountMinServer.getId());
//		}
		if (insert) competitionPlayingDao.insert(playing);
		return playing;
//		}
//		return null;
	}

	/**
	 *@description 比赛生成检查
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/2
	 */
	private boolean genPlayingCheck(CompetitionPlayingDB playing) {
		//没有这个记录 生成
		//定时赛存在时间约束 唯一生成虚拟约束 类型+子类型+报名时间+活动标题(即使是一摸一样的标题,都需要引用不一样的说法)
		return (isTimerPlaying(playing::getType) && competitionPlayingDao.queryByCount(CompetitionPlayingDB.builder().type(playing.getType()).category(playing.getCategory()).applyBefore(playing.getApplyBefore()).entrance(playing.getEntrance()).build()) <= 0) ||
				//报名赛没有时间约束 唯一生成虚拟约束 类型+子类型+当前状态+活动标题(即使是一摸一样的标题,都需要引用不一样的说法)
				(isApplyPlaying(playing::getType) && competitionPlayingDao.queryByCount(CompetitionPlayingDB.builder().type(playing.getType()).category(playing.getCategory()).status(CompetitionPlayingStatusEnum.INIT).entrance(playing.getEntrance()).build()) <= 0);
	}

	/**
	 *@description 转化参数,配置到赛事
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/22
	 */
	private CompetitionPlayingDB configDBFormatPlayingDB(CompetitionPlayingConfigDB param, boolean next) {
		CompetitionPlayingDB playing = new CompetitionPlayingDB();
		BeanUtils.copyProperties(param, playing);
		playing.setId(0);
		playing.setCurHuman(0);
		playing.setStatus(CompetitionPlayingStatusEnum.APPLYING);
		playing.setPlayingConfigId(param.getId());
		playing.setCurStep(1);
		playing.setCurRound(1);
		playing.setUpStep(0);
		playing.setUpRound(0);
		playing.setApplyBefore(null);
		playing.setMatchBefore(null);
		playing.setBeginPlayingPushStatus(null);
		playing.setBeginPlayingRHLPushStatus(null);

		//定时赛
		if (isTimerPlaying(param::getType)) {
			playing.setApplyBefore(new Date());
			playing.setMatchBefore(new Date());
//			ParamCheck.isNotNullSet(param::getApplyBefore, (time) -> playing.setApplyBefore(getCalendar(param.getIterationType(), param.getIterationMin(), (String) time, next).getTime()));
			ParamCheck.isNotNullSet(playing::getApplyBefore, (time) -> playing.setApplyBefore(new Date(System.currentTimeMillis() - 1000)));
			//		ParamCheck.isNotNullSet(param::getApplyAfter, (time) -> playing.setApplyAfter(getCalendar(param.getIterationType(), param.getIterationMin(), (String) time, next).getTime()));
			ParamCheck.isNotNullSet(param::getMatchBefore, (time) -> playing.setMatchBefore(getCalendar(param.getIterationType(), param.getIterationMin(), (String) time, next, param).getTime()));
			//		ParamCheck.isNotNullSet(param::getMatchAfter, (time) -> playing.setMatchAfter(getCalendar(param.getIterationType(), param.getIterationMin(), (String) time, next).getTime()));
		}
		else if (isApplyPlaying(param::getType)) {
		}
		return playing;
	}

	/**
	 *@description 转化参数,赛事到赛事
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/22
	 */
	private CompetitionPlayingDB playingDBFormatPlayingDB(CompetitionPlayingConfigDB configParam, CompetitionPlayingDB playingParam, boolean next) {
		CompetitionPlayingDB playing = new CompetitionPlayingDB();
		BeanUtils.copyProperties(configParam, playing);
		playing.setId(0);
		playing.setCurHuman(0);
		//默认处于报名中
		playing.setStatus(CompetitionPlayingStatusEnum.APPLYING);
		playing.setCurStep(1);
		playing.setCurRound(1);
		playing.setPlayingConfigId(configParam.getId());
		playing.setUpStep(0);
		playing.setUpRound(0);
		playing.setApplyBefore(null);
		playing.setMatchBefore(null);
		playing.setBeginPlayingPushStatus(null);
		playing.setBeginPlayingRHLPushStatus(null);

		//定时赛
		if (isTimerPlaying(configParam::getType)) {
			playing.setApplyBefore(new Date());
			playing.setMatchBefore(new Date());
//			if (playingParam.getApplyBefore() == null) {
//			}else { //以开赛时间开赛
//				ParamCheck.isNotNullSet(playingParam::getApplyBefore, (time) -> playing.setApplyBefore(getCalendar(configParam.getIterationType(), configParam.getIterationMin(), next, playingParam.getApplyBefore()).getTime()));
//			}
			//及时开赛
			ParamCheck.isNotNullSet(playing::getApplyBefore, (time) -> playing.setApplyBefore(new Date(System.currentTimeMillis() - (1 * 60 * 1000))));
//			ParamCheck.isNotNullSet(playingParam::getApplyAfter, (time) -> playing.setApplyAfter(getCalendar(configParam.getIterationType(), configParam.getIterationMin(), next, playingParam.getApplyAfter()).getTime()));
			ParamCheck.isNotNullSet(playingParam::getMatchBefore, (time) -> playing.setMatchBefore(getCalendar(configParam.getIterationType(), configParam.getIterationMin(), next, playingParam.getMatchBefore(), configParam).getTime()));
//			System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime() / 1000 / 60 / 60 * 1000 * 60 * 60));
//			ParamCheck.isNotNullSet(playingParam::getMatchAfter, (time) -> playing.setMatchAfter(getCalendar(configParam.getIterationType(), configParam.getIterationMin(), next, playingParam.getMatchAfter()).getTime()));
		} else if (isApplyPlaying(configParam::getType)) {    //报名赛
		}

		return playing;
	}

	/**
	 *@description 定时赛
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/25
	 */
	public static boolean isTimerPlaying(ConsumerC<Integer> c) {
		return c.invoke().equals(CompetitionPlayingTypeEnum.TIMER_PLAYING);
	}

	/**
	 *@description 报名赛
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/25
	 */
	public static boolean isApplyPlaying(ConsumerC<Integer> c) {
		return c.invoke().equals(CompetitionPlayingTypeEnum.APPLY_PLAYING);
	}

	/**
	 *@description 变更日历
	 *@param iterationType 迭代类型
	 *@param iterationType 分钟数
	 *@param time 参数内的时间
	 *@param next true下一个时间的场次, false当前时间点的场次
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/22
	 */
	private Calendar getCalendar(Integer iterationType, Integer iterationMin, String time, boolean next, CompetitionPlayingConfigDB config) {
		//开放报名时间
		Date configDate = null;
		Calendar instance = Calendar.getInstance();
		try {
			boolean longDate=false;
			String f =  "HH:mm:ss";
			if (time.length() > 8) {
				f = "yyyy-MM-dd HH:mm:ss";
				longDate = true;
			}
			configDate = new SimpleDateFormat(f).parse(time);
			if(longDate){
				instance.setTime(configDate);
			}else{
				Calendar instance1 = Calendar.getInstance();
				instance1.setTime(configDate);
				instance.set(Calendar.HOUR_OF_DAY, instance1.get(Calendar.HOUR_OF_DAY));
				instance.set(Calendar.MINUTE, instance1.get(Calendar.MINUTE));
				instance.set(Calendar.SECOND, instance1.get(Calendar.SECOND));
			}

		} catch (Exception e) {
			throw new MessageException(-1, "时间参数[" + time + "]异常,请检查后重试");
		}

		return getCalendar(iterationType, iterationMin, next, instance.getTime(), config);
	}

	/**
	 *@description 日历迭代,分钟赛,日赛,周赛
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/2
	 */
	private Calendar getCalendar(Integer iterationType, Integer iterationMin, boolean next, Date configDate, CompetitionPlayingConfigDB config) {
		Calendar instance = Calendar.getInstance();
		try {
			instance.setTime(configDate);
		} catch (Exception e) {
			LogUtil.e("e " + e);
		}

		//去掉秒及毫秒
		instance.set(Calendar.MILLISECOND, 0);

		//判断赛事类型
		Integer iType = Optional.ofNullable(iterationType).orElse(0);
		if (next) {
			switch (iType) {
				case 1:    //分钟赛
					instance.add(Calendar.MINUTE, iterationMin);
					break;
				case 2: //日赛
					instance.add(Calendar.DAY_OF_WEEK, iterationMin);
					break;
				case 3: //周赛
					instance.add(Calendar.DAY_OF_WEEK, iterationMin * 7);
					break;
			}
		}

		Date date = instance.getTime();
		try {
			//存在禁赛区间
			if(iType == 1 && !StringUtils.isBlank(config.getDisableStartTime()) && !StringUtils.isBlank(config.getDisableEndTime())){
				Calendar instance1 = Calendar.getInstance();
				instance1.setTime(new SimpleDateFormat("HH:mm:ss").parse(config.getDisableStartTime().length() > 5 ? config.getDisableStartTime() : config.getDisableStartTime() + ":00"));

				Calendar start = Calendar.getInstance();
				start.set(Calendar.HOUR_OF_DAY, instance1.get(Calendar.HOUR_OF_DAY));
				start.set(Calendar.MINUTE, instance1.get(Calendar.MINUTE));
				start.set(Calendar.SECOND, instance1.get(Calendar.SECOND));

				Calendar instance2 = Calendar.getInstance();
				instance2.setTime(new SimpleDateFormat("HH:mm:ss").parse(config.getDisableEndTime().length() > 5 ? config.getDisableEndTime() : config.getDisableEndTime() + ":00"));

				Calendar end = Calendar.getInstance();
				end.set(Calendar.HOUR_OF_DAY, instance2.get(Calendar.HOUR_OF_DAY));
				end.set(Calendar.MINUTE, instance2.get(Calendar.MINUTE));
				end.set(Calendar.SECOND, instance2.get(Calendar.SECOND));

				//禁赛区间不做生成
				if (start.getTime().getTime() <= date.getTime() && end.getTime().getTime() >= date.getTime()) {
					return getCalendar(iterationType, iterationMin + config.getIterationMin(), next, configDate, config);
				}
			}
		} catch (Exception e) {
		}


		return instance;
	}

	/**
	 *@description 拿到一个当前在线人数最少的服
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/10
	 */
	public ServerConfig getOnlineCountMinServer(Long roomConfigId) {
		Long playType = competitionRoomDao.queryPlayTypeByRoomConfigId(roomConfigId);
		MessageAssert.assertFalse(playType == null, MessageExceptionParam.builder()
				.msg("赛场找不到玩法配置")
				.throwException(true)
				.build());

		int serverType = 3;

		Optional<ServerConfig> min = ServerDaoImpl.getInstance().queryAllServer().stream().filter(v -> v.getServerType() == serverType).filter(v -> Arrays.stream(v.getGameType().split(",")).map(NumberUtils::toLong)
				.anyMatch(v1 -> v1.equals(playType))).min(Comparator.comparing(ServerConfig::getOnlineCount));

		MessageAssert.assertFalse(!min.isPresent(), MessageExceptionParam.builder()
				.msg("在服务器类型ServerType:"+serverType+"中,没有配置玩法ID:"+playType)
				.throwException(true)
				.build());

		return min.get();
	}
}
