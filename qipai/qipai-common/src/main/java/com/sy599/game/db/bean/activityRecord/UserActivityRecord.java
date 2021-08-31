package com.sy599.game.db.bean.activityRecord;

import com.sy599.game.staticdata.bean.NewPlayerGiftActivityConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liuping
 * 玩家活动记录信息
 */
public class UserActivityRecord {

	/**
	 * 开房送钻活动玩家每日消耗的钻石数量
	 */
	private int comsumeDiam;

	/**
	 * 开房送钻活动玩家每日已领取的奖励档次
	 */
	private List<Integer> receiveGrades;

	/**
	 * 开房送钻活动每日钻石消耗重置时间
	 */
	private long resetTime;

	/**
	 * 老带新活动玩家已领取钻石数
	 */
	private int oldDaiNewReceiveDiam;

	/**
	 * 打筒子邀请活动领取奖励记录 位置 1开始
	 */
	private List<Integer> inviteReward;

	/**
	 * 幸运转盘已抽取次数
	 */
	private int userdLotteryNum;

	/**
	 * 砸金蛋已砸次数
	 */
	private int smashEggTimes;

	/**
	 * 砸金蛋达成任务情况
	 * 每日首次分享朋友（0/1）
	 * 军团中进行3场牌局（0/3)
	 * 进行4场4人或8人牌局（0/4）
	 * 牌局赢得3次大赢家（0/3）
	 */
	private Map<Integer, Integer> smashEggTasks;
	/**
	 * 打筒子邀请活动领取钻石记录
	 */
	private List<Integer> inviteUserReward;
	/**
	 * 打筒子邀请活动领取钻石记录页码
	 */
	private int inviteUserPage;
	/**
	 * 转盘抽奖活动最新分享领取时间
	 */
	private long luckyRedbagShareReceiveTime;

	/**
	 * 转盘抽奖活动最新对局领取时间
	 */
	private long luckyRedbagGameReceiveTime;

	/**
	 * 转盘抽奖活动期间最新完成大局时间
	 */
	private long luckRedbagBureauTime;

	/**
	 * 转盘抽奖活动期间当天完成大局数
	 */
	private int luckRedbagDayBureau;

	/**
	 * 回归礼包活动时间【玩家回归时间】
	 */
	private long oldBackGifTime;

	/**
	 * 回归礼包活动期间玩家登陆天数
	 */
	private int oldBackGifLoginCount;

	/**
	 * 回归礼包活动期间最新登陆时间
	 */
	private long oldBackGiftLoginTime;

	/**
	 * 回归礼包活动奖励领取记录 - 领取时间表
	 */
	private Map<Integer, Long> oldBackGifRecord;

	/**
	 * 新人有礼活动任务情况表  任务id - [0 完成次数,1 完成时间，2 活跃度领取时间】
	 * 每日完成牌局数（0/2）
	 * 每日分享（0/1）
	 * 大赢家次数（0/3）
	 * 商城充值（0/1）
	 * 加入亲友圈（0/1）
	 */
	private Map<Integer, Long[]> newPlayerGiftTasks;

	/**
	 * 新人有礼活动奖励领取记录
	 */
	private List<Integer> newPlayerGiftRecord;

	/**
	 * 新人活跃度
	 */
	private int newPlayerGifLiv;

	public UserActivityRecord() {
		comsumeDiam = 0;
		receiveGrades = new ArrayList<>();
		resetTime = 0;
		oldDaiNewReceiveDiam = 0;
		inviteReward = new ArrayList<>();
		smashEggTimes = 0;
		smashEggTasks = new HashMap<>();
		userdLotteryNum = 0;
		inviteUserReward = new ArrayList<>();
		inviteUserPage = 1;
		luckyRedbagShareReceiveTime = 0;
		luckyRedbagGameReceiveTime = 0;
		luckRedbagBureauTime = 0;
		luckRedbagDayBureau = 0;
		oldBackGifTime = 0;
		oldBackGifLoginCount = 0;
		oldBackGiftLoginTime = 0;
		oldBackGifRecord = new HashMap<>();
		newPlayerGiftTasks = new HashMap<>();
		newPlayerGiftTasks.put(NewPlayerGiftActivityConfig.type_game_count, new Long[]{0L,0L,0L});
		newPlayerGiftTasks.put(NewPlayerGiftActivityConfig.type_daily_share, new Long[]{0L,0L,0L});
		newPlayerGiftTasks.put(NewPlayerGiftActivityConfig.type_big_win, new Long[]{0L,0L,0L});
		newPlayerGiftTasks.put(NewPlayerGiftActivityConfig.type_mall_topUp, new Long[]{0L,0L,0L});
		newPlayerGiftTasks.put(NewPlayerGiftActivityConfig.type_join_fc, new Long[]{0L,0L,0L});
		newPlayerGiftRecord = new ArrayList<>();
		newPlayerGifLiv = 0;
	}

	public UserActivityRecord(int comsumeDiam, List<Integer> receiveGrades) {
		super();
		this.comsumeDiam = comsumeDiam;
		this.receiveGrades = receiveGrades;
		this.resetTime = System.currentTimeMillis();
	}

	public Map<Integer, Long[]> getNewPlayerGiftTasks() {
		return newPlayerGiftTasks;
	}

	public Long[] getNewPlayerGiftTasksLongArray(Integer tasksId)
    {
        return this.newPlayerGiftTasks.get(tasksId);
    }

	public void putNewPlayerGiftTasks(Integer tasksId, Integer index, Long value) {
		Long[] tasks = this.newPlayerGiftTasks.get(tasksId);
		tasks[index] = value;
		this.newPlayerGiftTasks.put(tasksId,tasks);
	}

	public Map<Integer, Long> getNewPlayerGiftTasksIndex(Integer index)
	{
		Map<Integer, Long> tesks = new HashMap<>();
		for(Integer tesksId : this.newPlayerGiftTasks.keySet())
		{
			Long[] tesksArray = this.newPlayerGiftTasks.get(tesksId);
			tesks.put(tesksId, tesksArray[index]);
		}
		return tesks;
	}

	public void setNewPlayerGiftTasks(Map<Integer, Long[]> newPlayerGiftTasks) {
		this.newPlayerGiftTasks = newPlayerGiftTasks;
	}

	public List<Integer> getNewPlayerGiftRecord() {
		return newPlayerGiftRecord;
	}

	public void setNewPlayerGiftRecord(List<Integer> newPlayerGiftRecord) {
		this.newPlayerGiftRecord = newPlayerGiftRecord;
	}

	public void addNewPlayerGiftRecord(int index) {
		this.newPlayerGiftRecord.add(index);
	}

	public int getNewPlayerGifLiv() {
		return newPlayerGifLiv;
	}

	public void setNewPlayerGifLiv(int newPlayerGifLiv) {
		this.newPlayerGifLiv = newPlayerGifLiv;
	}

	public long getOldBackGiftLoginTime() {
		return oldBackGiftLoginTime;
	}

	public void setOldBackGiftLoginTime(long oldBackGiftLoginTime) {
		this.oldBackGiftLoginTime = oldBackGiftLoginTime;
	}

    public Map<Integer, Long> getOldBackGifRecord() {
        return oldBackGifRecord;
    }

    public void setOldBackGifRecord(Map<Integer, Long> oldBackGifRecord) {
		this.oldBackGifRecord = oldBackGifRecord;
    }

    public void putOldBackGifRecord(Integer index, Long time)
    {
        this.oldBackGifRecord.put(index, time);
    }

    public long getOldBackGifTime() {
		return oldBackGifTime;
	}

	public void setOldBackGifTime(long oldBackGifTime) {
		this.oldBackGifTime = oldBackGifTime;
	}

	public int getOldBackGifLoginCount() {
		return oldBackGifLoginCount;
	}

	public void setOldBackGifLoginCount(int oldBackGifLoginCount) {
		this.oldBackGifLoginCount = oldBackGifLoginCount;
	}

	public int getComsumeDiam() {
		return comsumeDiam;
	}

	public void setComsumeDiam(int comsumeDiam) {
		this.comsumeDiam = comsumeDiam;
	}

	public void alterComsumeDiam(int addNum) {
		this.comsumeDiam += addNum;
	}

	public List<Integer> getReceiveGrades() {
		return receiveGrades;
	}

	public void setReceiveGrades(List<Integer> receiveGrades) {
		this.receiveGrades = receiveGrades;
	}

	public int obtainNextRewardGrades() {
		if(receiveGrades == null || receiveGrades.isEmpty()) {
			return 0;
		} else {
			return receiveGrades.get(receiveGrades.size() - 1) + 1;
		}
	}

	public long getResetTime() {
		return resetTime;
	}

	public void setResetTime(long resetTime) {
		this.resetTime = resetTime;
	}

	public int getOldDaiNewReceiveDiam() {
		return oldDaiNewReceiveDiam;
	}

	public void setOldDaiNewReceiveDiam(int oldDaiNewReceiveDiam) {
		this.oldDaiNewReceiveDiam = oldDaiNewReceiveDiam;
	}

	public List<Integer> getInviteReward() {
		return inviteReward;
	}

	public void setInviteReward(List<Integer> inviteReward) {
		this.inviteReward = inviteReward;
	}

	public int getUserdLotteryNum() {
		return userdLotteryNum;
	}

	public void setUserdLotteryNum(int userdLotteryNum) {
		this.userdLotteryNum = userdLotteryNum;
	}

	public int getSmashEggTimes() {
		return smashEggTimes;
	}

	public void setSmashEggTimes(int smashEggTimes) {
		this.smashEggTimes = smashEggTimes;
	}

	public void alterSmashEggTimes(int addNum) {
		this.smashEggTimes += addNum;
	}

	public Map<Integer, Integer> getSmashEggTasks() {
		return smashEggTasks;
	}

	public void setSmashEggTasks(Map<Integer, Integer> smashEggTasks) {
		this.smashEggTasks = smashEggTasks;
	}

	public void alterSmashEggTasks(int smashEggTaskId) {
		smashEggTasks.put(smashEggTaskId, smashEggTasks.get(smashEggTaskId) + 1);
	}

	public List<Integer> getInviteUserReward() {
		return inviteUserReward;
	}

	public void setInviteUserReward(List<Integer> inviteUserReward) {
		this.inviteUserReward = inviteUserReward;
	}

	public int getInviteUserPage() {
		return inviteUserPage;
	}

	public void setInviteUserPage(int inviteUserPage) {
		this.inviteUserPage = inviteUserPage;
	}

	public long getLuckyRedbagShareReceiveTime() {
		return luckyRedbagShareReceiveTime;
	}

	public void setLuckyRedbagShareReceiveTime(long luckyRedbagShareReceiveTime) {
		this.luckyRedbagShareReceiveTime = luckyRedbagShareReceiveTime;
	}

	public long getLuckyRedbagGameReceiveTime() {
		return luckyRedbagGameReceiveTime;
	}

	public void setLuckyRedbagGameReceiveTime(long luckyRedbagGameReceiveTime) {
		this.luckyRedbagGameReceiveTime = luckyRedbagGameReceiveTime;
	}

	public long getLuckRedbagBureauTime() {
		return luckRedbagBureauTime;
	}

	public void setLuckRedbagBureauTime(long luckRedbagBureauTime) {
		this.luckRedbagBureauTime = luckRedbagBureauTime;
	}

	public int getLuckRedbagDayBureau() {
		return luckRedbagDayBureau;
	}

	public void setLuckRedbagDayBureau(int luckRedbagDayBureau) {
		this.luckRedbagDayBureau = luckRedbagDayBureau;
	}

	/**
	 * 每日凌晨0点重置玩家砸金蛋活动数据
	 */
	public void resetSmashEggTasks() {
		this.smashEggTasks.clear();
		this.smashEggTimes = 0;
	}

	/**
	 * 每日凌晨0点开房送钻数据重置
	 */
	public void resetConsumeDaimRecordData() {
		this.comsumeDiam = 0;
		this.receiveGrades = new ArrayList<>();
		this.resetTime = System.currentTimeMillis();
	}
}
