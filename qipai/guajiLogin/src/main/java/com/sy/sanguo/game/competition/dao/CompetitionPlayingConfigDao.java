package com.sy.sanguo.game.competition.dao;

import java.sql.SQLException;

import com.sy.sanguo.game.competition.model.db.CompetitionBaseDBPojo;
import com.sy.sanguo.game.competition.model.db.CompetitionPlayingConfigDB;

import org.springframework.stereotype.Component;

/**
 * @author Guang.OuYang
 * @date 2020/5/20-11:40
 */
@Component
public class CompetitionPlayingConfigDao<T extends CompetitionBaseDBPojo> extends CompetitionCommonDao {
	public String getSpaceName() {
		return CompetitionPlayingConfigDB.SPACE_NAME;
	}

	public int closeOpen(CompetitionPlayingConfigDB db){
		try {
			return getSql().update(getSpaceName()+"_close",db);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int del(CompetitionPlayingConfigDB db){
		try {
			return getSql().delete(getSpaceName()+"_del",db);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}


}
