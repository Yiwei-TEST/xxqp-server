package com.sy.sanguo.game.competition.dao;

import com.sy.sanguo.game.competition.model.db.CompetitionBaseDBPojo;
import com.sy.sanguo.game.competition.model.db.CompetitionRoomUser;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Guang.OuYang
 * @date 2020/5/20-11:40
 */
@Component
public class CompetitionRoomUserDao extends CompetitionCommonDao {
	public String getSpaceName() {
		return CompetitionBaseDBPojo.SPACE_NAME_1 + ".room_user";
	}

	public long queryByPlayingCount(Long playingId, Long keyId) {
		try {
			return (long) getSql().queryForObject(getSpaceName() + "_queryByPlayingCount", new HashMap<String, Object>(){
				{
					this.put("playingId", playingId);
					this.put("keyId", keyId);
				}
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public long queryPlayingTableByRoomUser(Long playingId, Long userId) {
		try {
			return (long) getSql().queryForObject(getSpaceName() + "_queryPlayingTableByRoomUser", new HashMap<String, Object>(){
				{
					this.put("playingId", playingId);
					this.put("userId", userId);
				}
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public long queryByUserRoomId(Long playingId) {
		try {
			return (long) getSql().queryForObject(getSpaceName() + "_queryByUserRoomId", new HashMap<String, Object>(){
				{
					this.put("playingId", playingId);
				}
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int saveRoomUser(List<CompetitionRoomUser> competitionRoomUser) throws Exception {
		try{
			return getSql().update(getSpaceName() + "_insert_competition_room_user", competitionRoomUser);
		} catch (Exception e) {
		}
		return 0;
	}

	public int addCompetitionRoomPlayerCount(long keyId, int addCount) throws Exception {
		Map<String, Object> map = new HashMap<>(8);
		map.put("keyId", keyId);
		map.put("addCount", addCount);
		try {
			return getSql().update(getSpaceName() + "_add_competition_room_player_count", map);
		} catch (Exception e) {
		}
		return 0;
	}

	public int deleteCompetitionRoomUser(long playingId)  {
		Map<String, Object> map = new HashMap<>();
		map.put("playingId", String.valueOf(playingId));

		try {
			return getSql().delete(getSpaceName() + "_delete_competition_room_user", map);
		} catch (Exception e) {
		}
		return 0;
	}
}
