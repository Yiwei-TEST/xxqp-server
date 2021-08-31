package com.sy599.game.staticdata.bean;

import com.sy.mainland.util.CommonUtil;
import com.sy599.game.character.Player;
import com.sy599.game.gcommand.com.activity.NewPlayerGiftActivityCmd;
import com.sy599.game.gcommand.com.activity.OldBackGiftActivityCmd;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liuping
 * 精彩活动配置
 */
public class ActivityConfig {
	/**
	 * 精彩活动配置信息
	 */
	private static Map<Integer, ActivityConfigInfo> activityConfigMap = new ConcurrentHashMap<>();
	/**
	 * 精彩活动配置处理类
	 */
	public static Map<Integer, Class<? extends ActivityConfigInfo>> activityConfigClsMap = new HashMap<>();

	/*------精彩活动ID配置------*/
	/**
	 * 精彩活动面板ID
	 */
	public static int activity_amazing_activities = 0;
	/**
	 * 开房送钻活动ID
	 */
	public static int activity_comsume_diam = 1;
	/**
	 * 砸金蛋活动ID
	 */
	public static int activity_smash_egg = 2;
	/**
	 * 幸运转盘活动ID
	 */
	public static int activity_lucky_reward = 3;
	/**
	 * 老带新活动ID
	 */
	public static int activity_old_dai_new = 4;
	/**
	 * 分享领钻活动ID
	 */
	public static int activity_share = 5;
	/**
	 * 邀请领钻活动ID
	 */
	public static int activity_invite_friend = 6;
	/**
	 * 购彩送钻活动ID
	 */
	public static int activity_buy_lottery = 7;
	/**
	 * 推荐代理活动ID
	 */
	public static int activity_recommend_agency = 8;
	/**
	 * 打筒子邀请好友
	 */
	public static int activity_invite_user = 9;
	/**
	 * 免费开房活动ID
	 */
	public static int activity_free_game = 10;
	/**
	 * 首冲双倍活动ID
	 */
	public static int activity_first_recharge_double = 11;

	/**
	 * 打筒子形象大使
	 */
	public static int activity_image_ambassador = 12;
	/**
	 * 打筒子郑重声明
	 */
	public static int activity_solemn_statement = 13;
	/**
	 * 牌局统计活动(甘肃金昌玩法)
	 */
	public static int activity_game_bureau_static = 14;
	/**
	 * 俱乐部争霸活动
	 */
	public static int activity_group_conquest = 15;
	/**
	 * 俱乐部百万大奖活动
	 */
	public static int activity_group_megabucks = 16;
	/**
	 * 新绑码玩家牌局统计活动
	 */
	public static int activity_new_payBind_bureau_static = 17;
	/**
	 * 打筒子邀请好友送钻石
	 */
	public static int activity_invite_user_zuan = 18;
	/**
	 * 老玩家回归活动
	 * 活动期间前15天未登录游戏的玩家，在活动期间登录游戏，即可获得200钻石奖励
	 */
	public static int activity_old_player_back = 19;
	/**
	 * 打筒子宣传图
	 */
	public static int activity_advertising = 20;
	/**
	 * 益阳千分转盘抽奖活动
	 */
	public static int activity_lucky_redbag = 21;
	/**
	 * 亲友圈排行榜活动(图片展示)
	 */
	public static int activity_qyq_rank = 22;
	/**
	 * 比赛场活动
	 */
	public static int activity_match = 23;
	/**
	 * 呼朋唤友领30元红包
	 */
	public static int activity_hu_peng_huan_you = 24;

	/**
	 * 老玩家回归惊喜礼包
	 */
	public static int activity_old_back_gift = 25;

	/**
	 * 新人礼包
	 */
	public static int activity_new_player_gift = 26;

    /**
     * 牌局统计活动
     */
    public static int activity_game_bureau = 27;

    /**
     * 传奇来了
     */
    public static int activity_chuan_qi_lai_le = 28;

    /**
     * 欢乐金币场局数奖励活动
     */
    public static int activity_gold_room_share = 29;

	//---------------------手动配置活动配置处理类
	static {
		activityConfigClsMap.put(activity_comsume_diam, ConsumeDiamAcitivityConfig.class);
		activityConfigClsMap.put(activity_smash_egg, SmashEggAcitivityConfig.class);
		activityConfigClsMap.put(activity_lucky_reward, LotteryAcitivityConfig.class);
		activityConfigClsMap.put(activity_old_dai_new, OldDaiNewAcitivityConfig.class);
		activityConfigClsMap.put(activity_share, ShareAcitivityConfig.class);
		activityConfigClsMap.put(activity_invite_friend, CommonAcitivityConfig.class);
		activityConfigClsMap.put(activity_buy_lottery, CommonAcitivityConfig.class);
		activityConfigClsMap.put(activity_recommend_agency, CommonAcitivityConfig.class);
		activityConfigClsMap.put(activity_invite_user, InviteActivityConfig.class);
		activityConfigClsMap.put(activity_free_game, CommonAcitivityConfig.class);
		activityConfigClsMap.put(activity_first_recharge_double, CommonAcitivityConfig.class);
		activityConfigClsMap.put(activity_image_ambassador, CommonAcitivityConfig.class);
		activityConfigClsMap.put(activity_solemn_statement, CommonAcitivityConfig.class);
        activityConfigClsMap.put(activity_game_bureau_static, CommonAcitivityConfig.class);
		activityConfigClsMap.put(activity_group_conquest, CommonAcitivityConfig.class);
		activityConfigClsMap.put(activity_group_megabucks, CommonAcitivityConfig.class);
		activityConfigClsMap.put(activity_new_payBind_bureau_static, CommonAcitivityConfig.class);
		activityConfigClsMap.put(activity_invite_user_zuan, InviteUserActivityConfig.class);
		activityConfigClsMap.put(activity_old_player_back, CommonAcitivityConfig.class);
		activityConfigClsMap.put(activity_advertising, CommonAcitivityConfig.class);
		activityConfigClsMap.put(activity_lucky_redbag, LuckyRedBagAcitivityConfig.class);
		activityConfigClsMap.put(activity_qyq_rank, CommonAcitivityConfig.class);
		activityConfigClsMap.put(activity_match, CommonAcitivityConfig.class);
		activityConfigClsMap.put(activity_hu_peng_huan_you, CommonAcitivityConfig.class);
		activityConfigClsMap.put(activity_old_back_gift, OldBackGiftActivityConfig.class);
		activityConfigClsMap.put(activity_new_player_gift, NewPlayerGiftActivityConfig.class);
        activityConfigClsMap.put(activity_game_bureau, GameBureauActivityConfig.class);
        activityConfigClsMap.put(activity_chuan_qi_lai_le, CommonAcitivityConfig.class);
        activityConfigClsMap.put(activity_gold_room_share, GoldRoomShareActivityConfig.class);

		/**
		 * 建议CommonAcitivityConfig活动的ID分段，以区别于其他活动，比如固定4位数，且以1开头，例如1000,1001
		 */
		String activiyIdStr = ResourcesConfigsUtil.loadServerPropertyValue("common_activities");
		if (StringUtils.isNotBlank(activiyIdStr)){
			String[] idStrs = activiyIdStr.split("\\D");
			for (String idStr : idStrs){
				if (CommonUtil.isPureNumber(idStr)){
					activityConfigClsMap.put(Integer.valueOf(idStr), CommonAcitivityConfig.class);
				}
			}
		}
	}

	/**
	 * 获取精彩活动配置信息
	 * @param activityId
	 * @return
	 */
	public static ActivityConfigInfo getActivityConfigInfo(int activityId) {
		return getActivityConfigMap().get(activityId);
	}

	/**
	 * 判断活动是否开启
	 * @param player
	 * @param activityId 活动ID
	 * @return
	 */
	public static boolean isActivityOpen(Player player, int activityId) {
		ActivityConfigInfo configInfo = activityConfigMap.get(activityId);
		if(configInfo != null) {
			if(configInfo.getOpen() == 0)
				return false;
			if(configInfo.getType() == 0) {// 永久活动
				return true;
			} else if(configInfo.getType() == 1) {// 限时活动
				Date nowDate = TimeUtil.now();
				if(nowDate.after(configInfo.getStartTime()) && nowDate.before(configInfo.getEndTime())) {// 在活动时间内
					return true;
				}
			} else if(configInfo.getType() == 2) {// 老玩家回归活动
				//15天前登录的用户且打满20小局
				return OldBackGiftActivityCmd.isOpenActivity(player);
			} else if(configInfo.getType() == 3) {// 新人有礼活动
				// 新注册用户
				return NewPlayerGiftActivityCmd.isNewRegPlayer(player);
			}
		}
		return false;
	}

	/**
	 * 判断活动是否开启
	 * @param activityId 活动ID
	 * @return
	 */
	public static boolean isActivityOpen(int activityId) {
		return isActivityOpen(null, activityId);
	}

	/**
	 * 活动是否开启
	 * @param activityId
	 * @param wanfaId 玩法ID -1表示所有玩法
	 * @param exts 扩展参数
	 * @return
	 */
	public static boolean isActivityOpen(int activityId, int wanfaId, Object... exts) {
		if(isActivityOpen(activityId, wanfaId)) {
			if(activityId == activity_free_game) {// 某些活动特殊处理
				CommonAcitivityConfig configInfo = (CommonAcitivityConfig) activityConfigMap.get(activityId);
				if((int)exts[0] != 0) {// 房费支付方式为AA支付
					return false;
				}
				String dateStr = configInfo.getParams();
				if(!StringUtil.isBlank(dateStr)) {
					String[] dateArr = dateStr.split("-");// 19:00-20:00
					if(!TimeUtil.isInTimeRange(dateArr[0], dateArr[1]))
						return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * 判断精彩活动是否开启
	 * @param activityId 活动ID
	 * @param wanfaId 玩法ID -1表示所有玩法
	 * @return
	 */
	public static boolean isActivityOpen(int activityId, int wanfaId) {
		ActivityConfigInfo configInfo = activityConfigMap.get(activityId);
		if(configInfo != null) {
			if(configInfo.getWanfas().contains(wanfaId) || configInfo.getWanfas().get(0) == -1 || configInfo.getWanfas().get(0) == 0) {
				return isActivityOpen(activityId);
			}
		}
		return false;
	}

	public static Map<Integer, ActivityConfigInfo> getActivityConfigMap() {
		return activityConfigMap;
	}

	public static void setActivityConfigMap(Map<Integer, ActivityConfigInfo> map) {
		activityConfigMap =  map;
	}

    public static boolean isActivityActive(int activityId) {
        ActivityConfigInfo config = getActivityConfigInfo(activityId);
        return config != null && config.isActive();
    }
}
