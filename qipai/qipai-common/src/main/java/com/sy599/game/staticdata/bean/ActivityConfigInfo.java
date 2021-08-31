package com.sy599.game.staticdata.bean;

import java.util.Date;
import java.util.List;

/**
 * @author liuping
 * 棋牌精彩活动配置信息（基类）
 */
public abstract class ActivityConfigInfo implements Comparable<ActivityConfigInfo>{

	/**
	 * 活动ID
	 */
	private int id;
	
	/**
	 * 活动名称
	 */
	private String activityName;
	
	/**
	 * 活动描叙
	 */
	private String desc;

	/**
	 * 是否前端可见 1可见 0不可见
	 */
	private int visible;
	
	/**
	 * 是否开启 1开启 0不开启
	 */
	private int open;
	
	/**
	 * 支持的玩法ID -1表示APP所有玩法
	 */
	private List<Integer> wanfas;
	
	/**
	 * 活动类型 0永久活动 1时限活动
	 */
	private int type;
	
	/**
	 * 活动开启时间
	 */
	private Date startTime;
	
	/**
	 * 活动结束时间
	 */
	private Date endTime;
	
	/**
	 * 活动需要达成条件
	 */
	protected String params;
	
	/**
	 * 活动获得奖励配置
	 */
	protected String rewards;
	
	/**
	 * 活动标签位置
	 */
	protected int sort;

	/**
	 * 是否单独活动入口 1单独活动入口 0活动专区
	 */
	protected int singleEnter;
	
	public ActivityConfigInfo() {
	}

	public void loadActivityConfigInfo(int id, String activityName, String desc, int visible, int open,
			List<Integer> wanfas, int type, Date startTime, Date endTime,
			String params, String rewards, int sort, int singleEnter) {
		this.id = id;
		this.activityName = activityName;
		this.desc = desc;
		this.visible = visible;
		this.open = open;
		this.wanfas = wanfas;
		this.type = type;
		this.startTime = startTime;
		this.endTime = endTime;
		this.params = params;
		this.rewards = rewards;
		this.sort = sort;
		this.singleEnter = singleEnter;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getActivityName() {
		return activityName;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public int getVisible() {
		return visible;
	}

	public void setVisible(int visible) {
		this.visible = visible;
	}

	public int getOpen() {
		return open;
	}

	public void setOpen(int open) {
		this.open = open;
	}

	public List<Integer> getWanfas() {
		return wanfas;
	}

	public void setWanfas(List<Integer> wanfas) {
		this.wanfas = wanfas;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getRewards() {
		return rewards;
	}

	public void setRewards(String rewards) {
		this.rewards = rewards;
	}
	
	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

	public int getSingleEnter() {
		return singleEnter;
	}

	public void setSingleEnter(int singleEnter) {
		this.singleEnter = singleEnter;
	}

	@Override
	public int compareTo(ActivityConfigInfo o) {
		if(this.sort < o.sort)
			return -1;
		else
			return 1;
	}
	
	/**
	 * 子类实现活动相关参数 以及 活动奖励配置初始化
	 */
	public abstract void configParamsAndRewards();

	public boolean isActive(){
	    return false;
    }
}
