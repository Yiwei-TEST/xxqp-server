package com.sy.sanguo.game.competition.model.enums;

/**
 * @author Guang.OuYang
 * @date 2020/5/20-18:27
 */
public class CompetitionPlayingStatusEnum {
	//等待报名
	public static final int INIT = 0;

	//报名
	public static final int APPLYING = 1;

	//开赛
	public static final int PLAYING = 2;

	//结算中
	public static final int CLEARING = 3;

	//结算完成
	public static final int CLEARING_END = 4;

	//结算失败
	public static final int CLEARING_ERROR = 5;

	//流局
	public static final int CASTOFF = 6;

	//匹配中
	public static final int MATCHING = 7;
}
