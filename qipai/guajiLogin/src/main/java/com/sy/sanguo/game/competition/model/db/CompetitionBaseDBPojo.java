package com.sy.sanguo.game.competition.model.db;

import java.util.Date;

/**
 * @author Guang.OuYang
 * @date 2020/5/20-14:54
 */
public abstract class CompetitionBaseDBPojo {
	public static final String SPACE_NAME_1 = "t_competition";

	public abstract void setCreateTime(Date timestamp);

	public abstract void setUpdateTime(Date timestamp);

	public abstract void setDeleteTime(Date timestamp);

}
