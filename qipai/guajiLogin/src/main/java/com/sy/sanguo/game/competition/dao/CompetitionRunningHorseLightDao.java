package com.sy.sanguo.game.competition.dao;

import com.sy.sanguo.game.competition.model.db.CompetitionBaseDBPojo;
import com.sy.sanguo.game.competition.model.db.CompetitionRunningHorseLightDB;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * @author Guang.OuYang
 * @date 2020/5/20-11:40
 */
@Component
public class CompetitionRunningHorseLightDao<T extends CompetitionBaseDBPojo> extends CompetitionCommonDao {
	public String getSpaceName() {
		return CompetitionRunningHorseLightDB.SPACE_NAME;
	}


	public Optional<List<CompetitionRunningHorseLightDB>> queryByToDay(CompetitionRunningHorseLightDB arg) {
		try {
			return Optional.ofNullable(getSql().queryForList(getSpaceName() + "_selectByLast", arg));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}
}
