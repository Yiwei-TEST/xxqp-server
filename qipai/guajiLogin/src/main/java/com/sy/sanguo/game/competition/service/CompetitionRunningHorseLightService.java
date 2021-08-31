package com.sy.sanguo.game.competition.service;

import com.sy.sanguo.game.competition.dao.CompetitionRunningHorseLightDao;
import com.sy.sanguo.game.competition.model.db.CompetitionRunningHorseLightDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 跑马灯
 * @author Guang.OuYang
 * @date 2020/5/20-18:07
 */
@Service
public class CompetitionRunningHorseLightService {
	@Autowired
	private CompetitionRunningHorseLightDao<CompetitionRunningHorseLightDB> competitionRunningHorseLightDao;

	/**
	 *@description
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/20
	 */
	public CompetitionRunningHorseLightDB addLight(CompetitionRunningHorseLightDB arg) {
		if (arg != null) {
			competitionRunningHorseLightDao.insert(arg);
		}
		return arg;
	}


	/**
	 *@description 最近的跑马灯
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/20
	 */
	public List<CompetitionRunningHorseLightDB> findLastLight() {
		return competitionRunningHorseLightDao.queryByToDay(null).orElse(Collections.emptyList());
	}

	/**
	 *@description 最近的跑马灯
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/20
	 */
	public List<CompetitionRunningHorseLightDB> findBottomTitleLight() {
		return competitionRunningHorseLightDao.queryByToDay(CompetitionRunningHorseLightDB.builder().type(2).build()).orElse(Collections.emptyList());
	}
}
