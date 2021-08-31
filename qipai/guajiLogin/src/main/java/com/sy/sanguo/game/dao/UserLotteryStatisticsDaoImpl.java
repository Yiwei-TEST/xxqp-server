package com.sy.sanguo.game.dao;

import java.sql.SQLException;

import com.sy.sanguo.common.parent.impl.CommonDaoImpl;
import com.sy.sanguo.game.bean.UserLotteryStatistics;

public class UserLotteryStatisticsDaoImpl extends CommonDaoImpl {

	public void saveUserLotteryStatistics(UserLotteryStatistics bean) throws SQLException{
		this.getSqlMapClient().update("lotteryStatistics.saveUserLotteryStatistics", bean);
	}
}
