package com.sy599.game.gcommand.com;

import com.sy599.game.character.Player;
import com.sy599.game.db.bean.task.TaskInfo;
import com.sy599.game.db.bean.task.UserTaskInfo;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.msg.serverPacket.TaskMsg;
import com.sy599.game.staticdata.bean.TaskConfig;
import com.sy599.game.staticdata.bean.TaskConfigInfo;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang.time.DateUtils;

import java.util.*;

/**
 * @author liuping
 * 任务响应接口
 */
public class TaskCommand extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        if (req.getStrParamsCount() < 1 || req.getParamsCount() < 1) {// 参数传递异常
        	LogUtil.errorLog.info("参数异常");
			return;
		}
        List<String> reqStrParams = req.getStrParamsList();// strParams参数第0位 表示打开请求所有任务
		List<Integer> reqParams = req.getParamsList();
		int requestType = reqParams.get(0);// params参数第0位 表示请求的操作类型 0打开任务 1领取任务
		int taskType = Integer.parseInt(reqStrParams.get(0));// 任务类型
		UserTaskInfo userTaskInfo = player.getMyExtend().getUserTaskInfo();
		if(requestType == 0) {// 打开任务/刷新任务
			refreshDailyTasks(player, userTaskInfo);// 日常任务刷新(不做主动刷新 玩家打开任务系统时进行刷新)
			sendTaskInfosBuilder(player, taskType, 0, requestType);
		} else {// 领取任务
			if(taskType < TaskConfig.task_bureau && taskType > TaskConfig.task_fortune) {
				LogUtil.errorLog.info("任务类型不存在:" + taskType);
				return;
			}
			Map<Integer, TaskConfigInfo> taskMap = TaskConfig.getTaskConfigInfosByType(taskType);
			int taskId = reqParams.get(1);// 任务ID
			TaskConfigInfo configInfo = taskMap.get(taskId);
			if(configInfo == null) {
				LogUtil.errorLog.info("任务不存在:" + taskId);
				return;
			}
			Set<Integer> userTasks = userTaskInfo.getUserTaskInfos(taskType);
			int process = getTaskProcess(userTaskInfo, taskType, taskId);
			if(userTasks != null && userTasks.contains(taskId)) {
				player.writeErrMsg("您已领取该任务奖励!");
				LogUtil.errorLog.info("您已领取该任务奖励:" + taskId);
				return;
			} else {
				if(process < Integer.parseInt(configInfo.getParam())) {
					player.writeErrMsg("您尚未达成任务，暂时不能领取!");
					LogUtil.errorLog.info("您尚未达成任务，暂时不能领取:" + taskId);
					return;
				}
				if(taskType == TaskConfig.task_bureau || taskType == TaskConfig.task_daily) {// 获得金币
					int rewardGold = Integer.parseInt(configInfo.getRewardParam());
					player.changeGold(rewardGold, 0, false, 0, true);
					LogUtil.msgLog.info("玩家达成任务：" + taskId + "获得金币：" + rewardGold);
				} else if(taskType == TaskConfig.task_fortune) {
					int rewardMangGuo = Integer.parseInt(configInfo.getRewardParam());
					userTaskInfo.addMangGuo(rewardMangGuo);
					// 推送更新芒果道具？
					LogUtil.msgLog.info("玩家达成任务：" + taskId + "获得芒果：" + rewardMangGuo);
				}
				if(userTasks == null)
					userTasks = new HashSet<>();
				userTasks.add(taskId);
				userTaskInfo.updateTaskInfos(taskType, userTasks);
				player.getMyExtend().updateUserTaskInfo(userTaskInfo);
				sendTaskInfosBuilder(player, taskType, taskId, requestType);// 刷新任务
			}
		}
	}

	@Override
	public void setMsgTypeMap() {
	}

	/**
	 * 推送前台任务信息 任务类型为0时推送所有任务信息 taskType>0推送具体某个任务taskId信息
	 * @param player
	 * @param taskType
	 * @param taskId
	 * @param requestType
	 */
	public void sendTaskInfosBuilder(Player player, int taskType, int taskId, int requestType) {
		TaskMsg.TaskLists.Builder allBuilders = TaskMsg.TaskLists.newBuilder();
		if(taskType == TaskConfig.task_all) {// 请求所有任务信息
			Map<Integer, Map<Integer, TaskConfigInfo>> allTaskTypeInfos = TaskConfig.getTaskTypeConfigMap();
			if(!allTaskTypeInfos.isEmpty()) {
				for(int tempTaskType : allTaskTypeInfos.keySet()) {
					TaskMsg.TypeTaskInfo.Builder typeInfoBuilder = TaskMsg.TypeTaskInfo.newBuilder();
					typeInfoBuilder.setTaskType(tempTaskType);
					Map<Integer, TaskConfigInfo> typeConfigInfos = allTaskTypeInfos.get(tempTaskType);
					List<TaskInfo> taskInfos = new ArrayList<>();
					for(TaskConfigInfo configInfo : typeConfigInfos.values()) {
						taskInfos.add(getTaskInfo(configInfo, player));
					}
					Collections.sort(taskInfos);
					for(TaskInfo info : taskInfos) {
						if(info.getState() == TaskConfig.task_state_received)
							typeInfoBuilder.addTaskInfos(info.getTaskInfoBuilder());
						else
							typeInfoBuilder.addTaskInfos(0, info.getTaskInfoBuilder());
					}
					if(tempTaskType == TaskConfig.task_daily)// 日常任务放最开始
						allBuilders.addTaskTypeInfos(0, typeInfoBuilder);
					else
						allBuilders.addTaskTypeInfos(typeInfoBuilder);
				}
			}
		} else {
			Map<Integer, TaskConfigInfo> taskTypeInfos = TaskConfig.getTaskConfigInfosByType(taskType);
			TaskConfigInfo configInfo = taskTypeInfos.get(taskId);
			TaskMsg.TypeTaskInfo.Builder typeInfoBuilder = TaskMsg.TypeTaskInfo.newBuilder();
			typeInfoBuilder.setTaskType(taskType);
			typeInfoBuilder.addTaskInfos(getTaskInfo(configInfo, player).getTaskInfoBuilder());
			allBuilders.addTaskTypeInfos(typeInfoBuilder);
		}
		allBuilders.setIsOpen(requestType);
		player.writeSocket(allBuilders.build());
	}

	/**
	 * 获取玩家任务信息
	 * @param configInfo
	 * @param player
	 * @return
	 */
	private TaskInfo getTaskInfo(TaskConfigInfo configInfo, Player player) {
		UserTaskInfo userTaskInfo = player.getMyExtend().getUserTaskInfo();
		Set<Integer> userTasks = userTaskInfo.getUserTaskInfos(configInfo.getTaskType());
		int taskType = configInfo.getTaskType();
		int taskId = configInfo.getTaskId();
		int process = getTaskProcess(userTaskInfo, taskType, taskId);
		int state = 0;
		if(userTasks != null && userTasks.contains(taskId)) {
			state = TaskConfig.task_state_received;
		} else {
			if(process >= Integer.parseInt(configInfo.getParam())) {
				state = TaskConfig.task_state_canReceive;
			} else
				state = TaskConfig.task_state_unReach;
		}
		TaskInfo info = new TaskInfo(taskId, taskType, configInfo.getParam(), configInfo.getRewardParam(), configInfo.getTaskDesc(), configInfo.getRewardDesc(), state, process);
		return info;
	}

	/**
	 * 获取玩家任务进程值
	 * @param userTaskInfo
	 * @param taskType
	 * @param taskId
	 * @return
	 */
	private int getTaskProcess(UserTaskInfo userTaskInfo, int taskType, int taskId) {
		int process = 0;
		if(taskType == TaskConfig.task_bureau) {// 局数任务
			process = player.getGoldPlayer().getPlayCount();
		} else if(taskType == TaskConfig.task_fortune){// 财富任务
			process = (int)userTaskInfo.getMaxGold();
		} else if(taskType == TaskConfig.task_daily) {// 每日任务
			if(taskId == TaskConfig.task_daily_five_gold_game || taskId == TaskConfig.task_daily_ten_gold_game) {
				process = userTaskInfo.getDailyGoldGameNum();
			} else if(taskId == TaskConfig.task_daily_3_win_streak) {
				process = userTaskInfo.getDailyWinGameNum();
			} else if(taskId == TaskConfig.task_daily_one_match) {
				process = userTaskInfo.getDailyMatchGameNum();
			}
		}
		return process;
	}

	/**
	 * 日常任务刷新
	 * @param player
	 * @param userTaskInfo
	 */
	private void refreshDailyTasks(Player player, UserTaskInfo userTaskInfo) {
		long dailyRefreshTime = userTaskInfo.getDailyRefreshTime();
		long curTime = TimeUtil.currentTimeMillis();
		boolean refresh = false;
		if(dailyRefreshTime > 0) {
			Date lastRefreshDate = new Date(dailyRefreshTime + DateUtils.MILLIS_PER_DAY);
			Calendar cal = TimeUtil.curCalendar();// 刷新时间的当天0点
			cal.setTime(lastRefreshDate);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			Date refreshTime = cal.getTime();
			if(curTime > refreshTime.getTime()) {
				refresh = true;
			}
		} else {
			refresh = true;
		}
		if(refresh) {
			userTaskInfo.setDailyGoldGameNum(0);
			userTaskInfo.setDailyMatchGameNum(0);
			userTaskInfo.setDailyWinGameNum(0);
			userTaskInfo.setDailyRefreshTime(curTime);
			player.getMyExtend().updateUserTaskInfo(userTaskInfo);
		}
	}

	public static void main(String[] args) {
		long curTime = TimeUtil.parseTimeInMillis("2018-07-02 15:04:22");
		Date lastRefreshDate = new Date(curTime + DateUtils.MILLIS_PER_DAY);
		Calendar cal = TimeUtil.curCalendar();
		cal.setTime(lastRefreshDate);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		Date refreshDate = cal.getTime();
		System.out.println(TimeUtil.formatTime(refreshDate));
	}
}
