package com.sy.sanguo.game.competition.dao;

import java.sql.SQLException;
import java.util.HashMap;

import com.sy.sanguo.game.competition.model.db.CompetitionBaseDBPojo;
import com.sy.sanguo.game.competition.model.db.CompetitionClearingAwardDB;

import org.springframework.stereotype.Component;

/**
 * @author Guang.OuYang
 * @date 2020/5/20-11:40
 */
@Component
public class CompetitionClearingAwardDao<T extends CompetitionBaseDBPojo> extends CompetitionCommonDao {
	public String getSpaceName() {
		return CompetitionClearingAwardDB.SPACE_NAME;
	}

	/**
	 *@description
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/27
	 */
	public int updateStatus(Long id, int defaultStatus, int status) {
		try {
			return getSql().update(getSpaceName() + "_update_status",new HashMap<String, Object>() {
				{
					this.put("id", id);
					this.put("default_status", defaultStatus);
					this.put("status", status);
				}
			});
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

		return -1;
	}
}
