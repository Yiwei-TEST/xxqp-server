package com.sy.sanguo.game.competition.service;

import com.sy.sanguo.common.util.LangMsg;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.competition.dao.CompetitionApplyDao;
import com.sy.sanguo.game.competition.dao.CompetitionConsumerDao;
import com.sy.sanguo.game.competition.dao.CompetitionPlayingDao;
import com.sy.sanguo.game.competition.exception.MessageAssert;
import com.sy.sanguo.game.competition.exception.MessageException;
import com.sy.sanguo.game.competition.exception.MessageExceptionParam;
import com.sy.sanguo.game.competition.model.db.CompetitionApplyDB;
import com.sy.sanguo.game.competition.model.db.CompetitionPlayingDB;
import com.sy.sanguo.game.competition.model.enums.CompetitionApplyStatusEnum;
import com.sy.sanguo.game.competition.model.enums.CompetitionPlayingStatusEnum;
import com.sy.sanguo.game.competition.model.param.CompetitionApplyParam;
import com.sy.sanguo.game.competition.model.param.CompetitionPlayingParam;
import com.sy.sanguo.game.competition.model.param.CompetitionRefreshQueueModel;
import com.sy.sanguo.game.competition.util.ParamCheck;
import com.sy.sanguo.game.dao.UserDao;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 报名
 * @author Guang.OuYang
 * @date 2020/5/20-16:29
 */
@Service
public class CompetitionApplyService {
	@Autowired
	private CompetitionPushService competitionPushService;

	@Autowired
	private CompetitionApplyDao competitionApplyDao;

	@Autowired
	private CompetitionPlayingDao<CompetitionPlayingDB> competitionPlayingDao;

	@Autowired
	private CompetitionConsumerDao competitionConsumerDao;

	@Autowired
	private CompetitionPlayingService competitionPlayingService;

	@Autowired
	private CompetitionGeneratePlayingService competitionGeneratePlayingService;

	private UserDao userDao = UserDao.getInstance();

	//是否赛前推送
	@Value("${competition.default.push:1}")
	private Boolean default_push;

	//每日报名次数
	@Value("${competition.default.day.sign.count:5}")
	private Integer default_day_sign_count;

	@Value("${competition.is.debug:0}")
	private boolean isDebug;

	@Value("${competition.open.robot:0}")
	private boolean openRobot;

	@Value("${competition.onOff:1}")
	private boolean onOff;

	public String sign(CompetitionApplyParam param) {
		LogUtil.i("competition|apply|sign|start|param|" + param.getUserId() + "|" + param);
		//必要参数校验
		MessageAssert.assertFalse(ParamCheck.isMultilOrNull(
				param::getUserId,
				param::getTitleType,
				param::getPlayingType,
				param::getCategory,
				param::getEntrance
				),
				MessageExceptionParam.builder().code(-1).msg(LangMsg.getMsg(LangMsg.code_3)).throwException(true).build());

		//参赛次数限制
		MessageAssert.assertFalse(disableSignCount(param.getUserId())
				, MessageExceptionParam.builder()
						.code(-1).msg("报名失败,你今日报名次数已经用完").throwException(true).build());

		List<CompetitionPlayingDB> competitionApplyPlayingList = competitionPlayingService.applyPlayingList(CompetitionPlayingParam.builder()
				.titleType(param.getTitleType())
				.playingType(param.getPlayingType())
				.category(param.getCategory())
				.entrance(param.getEntrance())
				.status(CompetitionPlayingStatusEnum.APPLYING)
				.userId(param.getUserId())
				.build());

		Optional<CompetitionPlayingDB> playingOptional = Optional.ofNullable(CollectionUtils.isEmpty(competitionApplyPlayingList) ? null : competitionApplyPlayingList.get(0));

		//赛事校验,找不到赛事,直接结束返回
		MessageAssert.assertFalse(!playingOptional.isPresent()||!onOff, MessageExceptionParam.builder()
				.code(-1).msg("你参与的活动太过火爆,稍后再试试吧 code:101").throwException(true).build());

		//重复报名校验
		MessageAssert.assertFalse(playingOptional.get().isSignStatus()
				//已经报名这一场比赛
						|| competitionApplyDao.queryByCount(CompetitionApplyDB.builder()
							.userId(param.getUserId())
							.playingId(playingOptional.get().getId())
							.status(CompetitionApplyStatusEnum.NORMAL)
							.build()) > 0,
				MessageExceptionParam.builder()
				.code(-1)
				.msg("请不要重复报名!").throwException(true).build());

		//已经报名这一个系列的比赛
		MessageAssert.assertFalse((Optional.ofNullable(competitionApplyDao.queryForListByType(param.getUserId(), playingOptional.get().getPlayingConfigId())).orElse(0) > 0),
				MessageExceptionParam.builder()
				.code(-1)
				.msg("操作过于频繁,先歇歇吧").throwException(true).build());

		//报名时间已过
		Date now = new Date();
		MessageAssert.assertFalse((CompetitionGeneratePlayingService.isTimerPlaying(playingOptional.get()::getType) && !(playingOptional.get().getMatchBefore().compareTo(now) > 0 &&
				playingOptional.get().getApplyBefore().compareTo(now) <= 0)
				|| !playingOptional.get().isOpenSign()
		), MessageExceptionParam.builder()
				.code(-1).msg("还未到报名时间").throwException(true).build());

		//禁赛期间
		MessageAssert.assertFalse(!ParamCheck.isMultilOrNull(playingOptional.get()::getDisableStartTime, playingOptional.get()::getDisableEndTime) && !(forToDayTime(playingOptional.get().getDisableEndTime()).compareTo(now) <= 0 &&
				forToDayTime(playingOptional.get().getDisableStartTime()).compareTo(now) >= 0), MessageExceptionParam.builder()
				.code(-1).msg("当前时间段未开放报名").throwException(true).build());

		//增加报名人数
		//满人
		MessageAssert.assertFalse(competitionPlayingDao.updateCurHuman(CompetitionPlayingDB.builder().id(playingOptional.get().getId()).curHuman(1).status(CompetitionPlayingStatusEnum.APPLYING).build()) <= 0, MessageExceptionParam.builder()
				.code(-1).msg("人数已满,先歇歇吧").throwException(true).build());

		try{
			//免费报名
			int consumerId = 0;
			int consumerVal = 0;
			boolean shareFreeSign = param.getShareFreeSign() != null && param.getShareFreeSign() == 1;
			if(!shareFreeSign){
				consumerId = playingOptional.get().getConsumerId();
				consumerVal = playingOptional.get().getConsumerVal();
			}

			//校验消耗
			//分享免费报名,直接报名
			MessageAssert.assertFalse(!consumer(playingOptional.get(), param.getUserId(), consumerId, -consumerVal), MessageExceptionParam.builder().code(-1).msg(getConsumerIdName(consumerId) + "不够").throwException(true).build());

			CompetitionApplyDB competitionApplyDb = CompetitionApplyDB.builder()
					.userId(param.getUserId())
					.consumerId(consumerId)
					.consumerVal(consumerVal)
					.playingId(playingOptional.get().getId())
					.status(CompetitionApplyStatusEnum.NORMAL)
					.shareFreeSign(shareFreeSign)
					.push(default_push).build();

			//报名失败
			MessageAssert.assertFalse(competitionApplyDao.insert(competitionApplyDb) <= 0, MessageExceptionParam.builder()
					.code(-1)
					.msg("报名失败!").build());


//			//切服,先切服,再推送
//			competitionPushService.cgePlayServerPush(Arrays.asList(param.getUserId()),
//					competitionPlayingService.resolveRoomConfigId(playingOptional.get().getCurStep(),playingOptional.get().getCurRound(),competitionGeneratePlayingService.getPlayingConfig(playingOptional.get().getPlayingConfigId())
//							.get()), playingOptional.get().getBindServerId());

			//开赛检查,这里仅期望管理到报名赛,定时赛由定时器检查
			if(CompetitionGeneratePlayingService.isApplyPlaying(playingOptional.get()::getType)){
				CompetitionPlayingDB competitionPlayingDB = competitionPlayingService.getPlaying(playingOptional.get().getId()).get();
				competitionPlayingService.playingStartCheck(competitionPlayingDB, false);

				LogUtil.i("competition|apply|sign|startGame|" + playingOptional.get().getId() + "|" + competitionPlayingDB.getId() + "|" + competitionPlayingDB.getCurHuman() + "|" + competitionPlayingDB.getBeginHuman());
			}

			LogUtil.i(String.format("competition|apply|sign|end|%s|%s|%s|%s|%s",
					param.getUserId(),
					competitionApplyDb.getId(),
					competitionApplyDb.getUserId(),
					competitionApplyDb.getPlayingId(),
					competitionApplyDb.getConsumerId(),
					competitionApplyDb.getConsumerVal()));

			//推送人数增加了
			competitionPushService.pushApplyCurHumanRefresh(CompetitionRefreshQueueModel.builder()
					.playingId(playingOptional.get().getId())
					.type(playingOptional.get().getTitleType())
					.type(playingOptional.get().getType())
					.category(playingOptional.get().getCategory())
					.entrance(playingOptional.get().getEntrance())
					.build());
		} catch (Exception e) {
			LogUtil.e("competition|apply|sign|error", e);
			competitionPlayingDao.updateCurHuman(CompetitionPlayingDB.builder().id(playingOptional.get().getId()).curHuman(-1).status(CompetitionPlayingStatusEnum.APPLYING).build());
			throw e;
		}

		return "success";
	}

	/**
	 *@description 每日报名次数上限
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/7
	 */
	public boolean disableSignCount(Long userId) {
		return competitionApplyDao.queryByCount(CompetitionApplyDB.builder()
				.userId(userId)
				.createTime(new Date())
				.status(CompetitionApplyStatusEnum.NORMAL)
				.build()) >= default_day_sign_count && onOff;
	}

	public Date forToDayTime(String m) {
		String[] split = m.split(":");
		return forToDayTime(split[0], split[1]);
	}

	public Date forToDayTime(String hour, String min) {
		Calendar instance = Calendar.getInstance();
		try {
			instance.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hour));
			instance.set(Calendar.MINUTE, Integer.valueOf(min));
			instance.set(Calendar.SECOND, 0);
		} catch (Exception e) {
			LogUtil.e("e " + e);
		}

		//去掉秒及毫秒
		instance.set(Calendar.MILLISECOND, 0);
		return instance.getTime();
	}

	public RegInfo getUser(long userId) {
		try {
			return userDao.getUser(userId);
		} catch (Exception e) {
			throw new MessageException(-1, "用户数据异常");
		}
	}

	/**
	 *@description 退出比赛, 取消报名
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/27
	 */
	public void cancel(CompetitionApplyParam param, boolean system) {
		LogUtil.i(String.format("competition|apply|cancel|%s|%s|%s", param.getUserId(), param, system));

		//赛事
		Optional<CompetitionPlayingDB> playingOptional;

		if(system){
			playingOptional = competitionPlayingDao.queryForSingle(CompetitionPlayingDB.builder()
					.id(param.getPlayingId())
					.build());

//			playingOptional.get().setSignStatus(competitionApplyDao.queryByCount(CompetitionApplyDB.builder().userId(param.getUserId()).playingId(param.getPlayingId()).status(CompetitionApplyStatusEnum.NORMAL).build())>0);
		} else {

			//必要参数校验
			MessageAssert.assertFalse(ParamCheck.isMultilOrNull(
					param::getUserId,
					param::getTitleType,
					param::getPlayingType,
					param::getCategory,
					param::getEntrance
					),
					MessageExceptionParam.builder().code(-1).msg(LangMsg.getMsg(LangMsg.code_3)).throwException(true).build());

			List<CompetitionPlayingDB> competitionApplyPlayingList = competitionPlayingService.applyPlayingList(CompetitionPlayingParam.builder()
					.titleType(param.getTitleType())
					.playingType(param.getPlayingType())
					.category(param.getCategory())
					.entrance(param.getEntrance())
					.status(CompetitionPlayingStatusEnum.APPLYING)
					.userId(param.getUserId())
					.build());

			playingOptional = Optional.ofNullable(CollectionUtils.isEmpty(competitionApplyPlayingList) ? null : competitionApplyPlayingList.get(0));


			//赛事校验,找不到赛事,直接结束返回, 这里的详细说明不需要抛给客户端
			MessageAssert.assertFalse(!playingOptional.isPresent() || playingOptional.get().getStatus() != CompetitionPlayingStatusEnum.APPLYING, MessageExceptionParam.builder()
					.code(-1).msg("比赛已经开始,不能退出!").throwException(true).build());
		}

			//赛事校验,找不到赛事,直接结束返回, 这里的详细说明不需要抛给客户端
//		MessageAssert.assertFalse(!playingOptional.get().isSignStatus(), MessageExceptionParam.builder()
//				.code(-1).msg("请先报名").throwException(true).build());

		Optional<CompetitionApplyDB> applyOptional = competitionApplyDao.queryForSingle(CompetitionApplyDB.builder()
				.userId(param.getUserId())
				.playingId(playingOptional.get().getId())
				.status(CompetitionApplyStatusEnum.NORMAL)
				.build());

		//系统流程
		if(system && !applyOptional.isPresent()){
			return;
		}

		MessageAssert.assertFalse(!applyOptional.isPresent(), MessageExceptionParam.builder()
				.code(-1).msg("比赛即将开始!").throwException(true).build());

		if(system){
			//扣除报名人数
			competitionPlayingDao.updateCurHuman(CompetitionPlayingDB.builder().id(playingOptional.get().getId()).curHuman(-1).build());
		}else{
			//扣除报名人数
			MessageAssert.assertFalse(competitionPlayingDao.updateCurHuman(
					CompetitionPlayingDB.builder()
							.id(playingOptional.get().getId()).curHuman(-1)
							.status(CompetitionPlayingStatusEnum.APPLYING)
							.build()) <= 0, MessageExceptionParam.builder()
					.code(-1).msg("比赛已经开始,不能退出!").throwException(true).build());
		}


		if (competitionApplyDao.updateStatus(applyOptional.get().getId(), CompetitionApplyStatusEnum.NORMAL, CompetitionApplyStatusEnum.EXIT) > 0) {
			if (consumer(playingOptional.get(), applyOptional.get().getUserId(), applyOptional.get().getConsumerId(), applyOptional.get().getConsumerVal())) {
				competitionApplyDao.updateStatus(applyOptional.get().getId(), CompetitionApplyStatusEnum.EXIT, CompetitionApplyStatusEnum.EXIT_RETURN);

				LogUtil.i(String.format("competition|apply|cancel|%s|%s|%s|%s|%s",
						applyOptional.get().getUserId(),
						applyOptional.get().getPlayingId(),
						applyOptional.get().getConsumerId(),
						applyOptional.get().getConsumerVal(),
						system));
//				LogUtil.i(String.format("competition|apply|cancel|%s|%s|%s|%s", applyOptional.get().getUserId(), applyOptional.get().getPlayingId(), applyOptional.get().getConsumerId(), applyOptional.get().getConsumerVal()));

				//推送人数变动
				competitionPushService.pushApplyCurHumanRefresh(CompetitionRefreshQueueModel.builder()
						.playingId(playingOptional.get().getId())
						.titleType((playingOptional.get().getTitleType()))
						.type(playingOptional.get().getType())
						.category(playingOptional.get().getCategory())
						.entrance(playingOptional.get().getEntrance())
						.build());


				if(system){
					//退赛通知
					competitionPushService.cancelPlayingNotifyPush(Arrays.asList(applyOptional.get().getUserId()),
							Arrays.asList(String.format(
									Optional.ofNullable(competitionGeneratePlayingService.getPlayingConfig(playingOptional.get().getPlayingConfigId()).get().getCancelPushTitle()).orElse("你报名的\"{p}\"被退赛,报名费用已退还!")
											.replace("{p}","%s").replace("(p)","%s"), playingOptional.get().getTitleCode())));
				}
			}
		}
	}

	public String getConsumerIdName(int consumerId) {
		return consumerId == 1 ? "白金豆" : consumerId == 2 ? "门票" : "资源";
	}

	/**
	 *@description 消耗校验
	 *@param
	 *@return true消耗成功
	 *@author Guang.OuYang
	 *@date 2020/5/20
	 */
	public boolean consumer(CompetitionPlayingDB playing, long userId, int consumerId, int consumerVal) {
		try {
			if (userId < 0) {
				return openRobot;
			}
			switch (consumerId) {
				case 0:        //免费free
					return true;
				case 1:        //白金点
					return competitionPushService.changeGoldPush(Arrays.asList(userId), playing.getCategory(), consumerVal, 18).equalsIgnoreCase("1");
				case 2:        //门票
					return true;
			}

		} catch (Exception e) {
			LogUtil.e("competition|apply|" + e);
		}

		return false;
	}

}
