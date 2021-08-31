package com.sy.sanguo.game.competition.dao;

import com.sy.sanguo.game.competition.model.db.CompetitionConsumerDB;

import org.springframework.stereotype.Component;

/**
 * @author Guang.OuYang
 * @date 2020/5/20-11:40
 */
@Component
public class CompetitionConsumerDao extends CompetitionCommonDao {
	public String getSpaceName() {
		return CompetitionConsumerDB.SPACE_NAME;
	}
}
