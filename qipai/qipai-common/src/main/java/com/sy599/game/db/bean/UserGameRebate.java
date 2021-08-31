/**
 * 
 */
package com.sy599.game.db.bean;

import java.util.Date;

/**
 * @author liuping
 * 玩家牌局返利(上线前期活动)
 */
public class UserGameRebate {

	/**
	 * 玩家ID
	 */
	private long userId;
	
	/**
	 * 昵称
	 */
	private String name;

	/**
	 * 玩法ID
	 */
	private int wanfaId;
	
	/**
	 * 达标局数
	 */
	private int number;
	
	/**
	 * 游戏时间
	 */
	private Date gameTime;
	
	/**
	 * 邀请码ID
	 */
	private int payBindId;
	
	public UserGameRebate() {
	}
	
	/**
	 * @param userId
	 * @param name
	 * @param wanfaId
	 * @param number
	 * @param gameTime
	 * @param payBindId
	 */
	public UserGameRebate(long userId, String name, int wanfaId, int number,
			Date gameTime, int payBindId) {
		super();
		this.userId = userId;
		this.name = name;
		this.wanfaId = wanfaId;
		this.number = number;
		this.gameTime = gameTime;
		this.payBindId = payBindId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getWanfaId() {
		return wanfaId;
	}

	public void setWanfaId(int wanfaId) {
		this.wanfaId = wanfaId;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public Date getGameTime() {
		return gameTime;
	}

	public void setGameTime(Date gameTime) {
		this.gameTime = gameTime;
	}

	public int getPayBindId() {
		return payBindId;
	}

	public void setPayBindId(int payBindId) {
		this.payBindId = payBindId;
	}
}
