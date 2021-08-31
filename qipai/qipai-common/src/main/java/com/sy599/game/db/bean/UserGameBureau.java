/**
 * 
 */
package com.sy599.game.db.bean;

/**
 * @author liuping
 * 玩家已打牌局次数统计(某个玩法  某天)
 */
public class UserGameBureau {

	/**
	 * 玩家ID
	 */
	private long userId;

	/**
	 * 玩法ID
	 */
	private int wanfaId;

	/**
	 * 游戏时间
	 */
	private String gameTime;

	/**
	 * 昵称
	 */
	private String name;

	/**
	 * 当天已玩局数
	 */
	private int number;

	/**
	 * 邀请码ID
	 */
	private int payBindId;

	public UserGameBureau() {
	}

	/**
	 * @param userId
	 * @param name
	 * @param wanfaId
	 * @param gameTime
	 * @param payBindId
	 */
	public UserGameBureau(long userId, String name, int wanfaId, String gameTime, int payBindId) {
		super();
		this.userId = userId;
		this.name = name;
		this.wanfaId = wanfaId;
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

	public String getGameTime() {
		return gameTime;
	}

	public void setGameTime(String gameTime) {
		this.gameTime = gameTime;
	}

	public int getPayBindId() {
		return payBindId;
	}

	public void setPayBindId(int payBindId) {
		this.payBindId = payBindId;
	}
}
