package com.sy599.game.gcommand.com;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.gcommand.com.activity.*;
import com.sy599.game.msg.serverPacket.ActivityMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.staticdata.bean.ActivityConfig;
import com.sy599.game.staticdata.bean.ActivityConfigInfo;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ObjectUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.*;

/**
 * @author liuping
 * 精彩活动响应接口
 */
public class ActivityCommand extends BaseCommand {

	private static Map<Integer, Class<? extends BaseCommand>> activityActionMap = new HashMap<Integer, Class<? extends BaseCommand>>();

	static {// 精彩活动需配置处理类
		activityActionMap.put(ActivityConfig.activity_comsume_diam, ConsumeDiamActivityCmd.class);// 开房送钻
		activityActionMap.put(ActivityConfig.activity_old_dai_new, OldDaiNewActivityCmd.class);// 老带新
		activityActionMap.put(ActivityConfig.activity_share, ShareActivityCmd.class);// 分享送钻
		activityActionMap.put(ActivityConfig.activity_lucky_reward, LotteryActivityCmd.class);// 幸运转盘
		activityActionMap.put(ActivityConfig.activity_invite_user, InviteActivityCmd.class);// 打筒子邀请好友
		activityActionMap.put(ActivityConfig.activity_smash_egg, SmashEggActivityCmd.class);// 砸金蛋
		activityActionMap.put(ActivityConfig.activity_free_game, FreeGameActivityCmd.class);// 免费开房
		activityActionMap.put(ActivityConfig.activity_invite_user_zuan, InviteUserActivityCmd.class);// 打筒子邀请好友送钻石
		activityActionMap.put(ActivityConfig.activity_old_player_back, OldPlayerBackActivityCmd.class);// 老玩家回归活动
		activityActionMap.put(ActivityConfig.activity_lucky_redbag, LuckyRedbagActivityCmd.class);// 益阳千分转盘抽奖活动
		activityActionMap.put(ActivityConfig.activity_hu_peng_huan_you, HuPengHuanYouActivityCmd.class);// 呼朋唤友活动
		activityActionMap.put(ActivityConfig.activity_old_back_gift, OldBackGiftActivityCmd.class);   //老玩家回归活动
		activityActionMap.put(ActivityConfig.activity_new_player_gift, NewPlayerGiftActivityCmd.class);   //新人有礼活动
		activityActionMap.put(ActivityConfig.activity_game_bureau, GameBureauActivityCmd.class);   //玩家俱数统计活动
        activityActionMap.put(ActivityConfig.activity_gold_room_share, GoldRoomShareActivityCmd.class);
	}

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        if (req.getStrParamsCount() < 1 || req.getParamsCount() < 1) {// 参数传递异常
        	LogUtil.errorLog.info("参数异常");
			return;
		}
        List<String> reqStrParams = req.getStrParamsList();// strParams参数第0位 表示请求的活动类型 amazingActivitys获取所有精彩活动
        int activityId = Integer.parseInt(reqStrParams.get(0));// 活动ID
//		params参数第1位 传递请求区域 0活动专区入口 1单独活动入口 //activity_amazing_activities
        if(activityId == ActivityConfig.activity_amazing_activities) { // 打开精彩活动面板
        	int defaultId = 0;
        	if(reqStrParams.size() >= 2) {
        		defaultId = Integer.parseInt(reqStrParams.get(1));// 活动面板打开具体活动
        	}
        	ActivityConfigInfo firstActivityInfo = sendActivityInfos(player, defaultId);
        	if(firstActivityInfo != null) {// 打开面板同时推送第一个活动的活动信息
        		if (activityActionMap.containsKey(firstActivityInfo.getId())) {// 每个活动单独处理
            		BaseCommand baseCmd = ObjectUtil.newInstance(activityActionMap.get(firstActivityInfo.getId()));
            		ComReq.Builder reqBuilder = ComReq.newBuilder();
            		List<Integer> params = new ArrayList<>();
            		params.add(0);// 打开活动
            		reqBuilder.addAllParams(params);
            		reqBuilder.setCode(req.getCode());
            		message.setMessage(reqBuilder.build());
            		baseCmd.execute(player, message);
            	}
        	}
        } else {
        	if(activityId == -1) {// 前端请求活动专区活动列表
				sendActivityInfos(player, -1);
				return;
			}
        	if(!ActivityConfig.isActivityOpen(player, activityId)) {
//    			player.writeErrMsg(LangHelp.getMsg(LangMsg.code_203));
    			return;
    		}
        	if (activityActionMap.containsKey(activityId)) {// 每个活动单独处理
        		BaseCommand baseCmd = ObjectUtil.newInstance(activityActionMap.get(activityId));
        		baseCmd.execute(player, message);
        	} else {
        		LogUtil.errorLog.info("请求了未配置处理类的活动：" + activityId);
        	}
        }
	}

	@Override
	public void setMsgTypeMap() {
	}


	public static ActivityMsg.ActivityConfigInfo.Builder getActivityInfoBuilder(ActivityConfigInfo configInfo, JSONObject userJsonInfo) {
		ActivityMsg.ActivityConfigInfo.Builder activityBuilder = ActivityMsg.ActivityConfigInfo.newBuilder();
		activityBuilder.setId(configInfo.getId());
		activityBuilder.setWanfas(StringUtil.implode(configInfo.getWanfas(), "_"));
		activityBuilder.setType(configInfo.getType());
		activityBuilder.setActivityName(configInfo.getActivityName());
		if(configInfo.getStartTime() != null)
			activityBuilder.setStartTime(TimeUtil.formatTime(configInfo.getStartTime()));
		else
			activityBuilder.setStartTime("");
		if(configInfo.getEndTime() != null)
			activityBuilder.setEndTime(TimeUtil.formatTime(configInfo.getEndTime()));
		else
			activityBuilder.setEndTime("");
		userJsonInfo.put("comParams", configInfo.getParams());
		userJsonInfo.put("rewardParams", configInfo.getRewards());
		activityBuilder.setParams(userJsonInfo.toString());
		activityBuilder.setDesc(configInfo.getDesc());
		activityBuilder.setSingleEnter(configInfo.getSingleEnter());
		return activityBuilder;
	}

	/**
	 * 发送某个具体活动信息
	 * @param player
	 * @param configInfo
	 * @param userJsonInfo
	 */
	public static void sendActivityInfo(Player player, ActivityConfigInfo configInfo, JSONObject userJsonInfo) {
		ActivityMsg.ActivityConfigInfo.Builder builder = getActivityInfoBuilder(configInfo, userJsonInfo);
		player.writeSocket(builder.build());
	}

	/**
	 * 登陆主动推送精彩活动信息
	 * @param player
	 */
	public static ActivityConfigInfo sendActivityInfos (Player player, int defaultId) throws Exception{
		Map<Integer, ActivityConfigInfo> configActivitys = ActivityConfig.getActivityConfigMap();
		if (configActivitys.size() > 0) {
			List<ActivityConfigInfo> openActivityConfigs = new ArrayList<>();
			Set<Integer> openActivityIds = new HashSet<>();
			for(ActivityConfigInfo configInfo : configActivitys.values()) {
				if(ActivityConfig.isActivityOpen(player, configInfo.getId()) && configInfo.getVisible() == 1) {// 可见活动才发送给前端
					openActivityConfigs.add(configInfo);
                    openActivityIds.add(configInfo.getId());
				}
			}
			if (openActivityConfigs.size() > 0) {
				Collections.sort(openActivityConfigs);
				ActivityMsg.ActivityLists.Builder builders = ActivityMsg.ActivityLists.newBuilder();
				ActivityConfigInfo firstConfigInfo = openActivityConfigs.get(0);
                int isShared = ShareActivityCmd.isShared(player, "");
                if(defaultId == 0 && isShared == 0) {
                	defaultId = ActivityConfig.activity_share;
                }
				for(ActivityConfigInfo configInfo : openActivityConfigs) {
					JSONObject userJsonInfo = new JSONObject();
					if(configInfo.getId() == defaultId) {
						builders.addConfigInfos(0, getActivityInfoBuilder(configInfo, userJsonInfo));
						firstConfigInfo = configInfo;
					} else
						builders.addConfigInfos(getActivityInfoBuilder(configInfo, userJsonInfo));
				}
				player.writeSocket(builders.build());
				return firstConfigInfo;
			}
		}
		return null;
	}
}
