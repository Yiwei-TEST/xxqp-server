package com.sy599.game.db.dao;

import com.alibaba.fastjson.JSON;
import com.sy599.game.db.bean.competition.CompetitionPlaying;
import com.sy599.game.db.bean.competition.CompetitionRoom;
import com.sy599.game.db.bean.competition.CompetitionRoomConfig;
import com.sy599.game.db.bean.competition.CompetitionRoomUser;
import com.sy599.game.util.LogUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompetitionDao extends BaseDao {

    private static CompetitionDao groupDao = new CompetitionDao();

    public static CompetitionDao getInstance() {
        return groupDao;
    }
    
    private static final String SPACE_NAME = "competition.";

    public List<CompetitionRoomConfig> loadAllRoomConfig() throws Exception {
        return (List<CompetitionRoomConfig>) this.getSqlLoginClient().queryForList(SPACE_NAME + "load_all_room_config");
    }

	public CompetitionRoom loadCompetitionRoom(long keyId) throws Exception {
		Map<String, Object> map = new HashMap<>(8);
		map.put("keyId", String.valueOf(keyId));
		return (CompetitionRoom) this.getSqlLoginClient().queryForObject(SPACE_NAME + "select_competition_room", map);
	}

	public CompetitionRoom randomCompetitionRoom(Long configId, Long playingId) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("configId", configId);
		map.put("playingId", playingId);
		return (CompetitionRoom) this.getSqlLoginClient().queryForObject(SPACE_NAME + "random_competition_room", map);
	}

	public CompetitionRoom randomCompetitionRoom(Long configId, Long playingId, Long serverId) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("configId", configId);
		map.put("playingId", playingId);
		map.put("serverId", serverId);
		return (CompetitionRoom) this.getSqlLoginClient().queryForObject(SPACE_NAME + "random_competition_room", map);
	}

	public CompetitionRoom randomCompetitionRoom(Long configId, Long playingId, Long serverId, int curStep, int curRound) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("configId", configId);
		map.put("playingId", playingId);
		map.put("serverId", serverId);
		map.put("curStep", curStep);
		map.put("curRound", curRound);
		return (CompetitionRoom) this.getSqlLoginClient().queryForObject(SPACE_NAME + "random_competition_room", map);
	}


	public CompetitionRoom loadCompetitionRoomForceMaster(long keyId) throws Exception {
		Map<String, Object> map = new HashMap<>(8);
		map.put("keyId", String.valueOf(keyId));
		return (CompetitionRoom) this.getSqlLoginClient().queryForObject(SPACE_NAME + "select_competition_room_force_master", map);
	}

	public Long saveRoom(CompetitionRoom room) {
		try {
			return (Long) this.getSqlLoginClient().insert(SPACE_NAME + "insert_competition_room", room);
		} catch (Exception e) {
			LogUtil.errorLog.error("saveCompetitionRoom|error|" + JSON.toJSONString(room), e);
		}
		return 0L;
	}

	public int deleteCompetitionRoomByKeyId(long roomKeyId) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("keyId", roomKeyId);
		return this.getSqlLoginClient().update(SPACE_NAME + "delete_competition_room_by_keyId", map);
	}


	public int deleteCompetitionRoomUserByRoomId(long roomKeyId) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("roomId", roomKeyId);
		return this.getSqlLoginClient().update(SPACE_NAME + "delete_competition_room_user", map);
	}


	public Long saveRoomUser(CompetitionRoomUser competitionRoomUser) throws Exception {
		try{
			return (Long) this.getSqlLoginClient().insert(SPACE_NAME + "insert_competition_room_user", competitionRoomUser);
		} catch (Exception e) {
			LogUtil.errorLog.error("saveGoldRoomUser|error|" + JSON.toJSONString(competitionRoomUser), e);
		}
		return 0L;
	}


	public int updateCompetitionRoomCurrentState(Long keyId, int currentState) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("keyId", keyId);
		map.put("currentState", currentState);
		return updateCompetitionRoomByKeyId(map);
	}


	public int updateCompetitionRoomByKeyId(Map<String, Object> map) throws Exception {
		return this.getSqlLoginClient().update(SPACE_NAME + "update_competition_room_by_keyId", map);
	}

	public int updateCompetitionRoomUserCurrentState(Long userId, int currentState) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("userId", userId);
		map.put("currentState", currentState);
		return updateCompetitionRoomUserByKeyId(map);
	}

	public int updateCompetitionRoomUserRoomId(Long userId, Long playingId, Long roomId) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("userId", userId);
		map.put("playingId", playingId);
		map.put("roomId", roomId);
		return updateCompetitionRoomUserByKeyId(map);
	}

	public int updateCompetitionRoomUserByKeyId(Map<String, Object> map) throws Exception {
		return this.getSqlLoginClient().update(SPACE_NAME + "update_competition_room_user_by_keyId", map);
	}

	public List<CompetitionRoomUser> loadAllCompetitionRoomUser(long roomId) throws Exception {
		Map<String, Object> map = new HashMap<>(8);
		map.put("roomId", roomId);
		return (List<CompetitionRoomUser>) this.getSqlLoginClient().queryForList(SPACE_NAME + "load_all_competition_room_user", map);
	}

	public List<CompetitionRoom> loadAllCompetitionRoom(long playingId, long serverId) throws Exception {
		Map<String, Object> map = new HashMap<>(8);
		map.put("playingId", playingId);
		map.put("serverId", serverId);
		return (List<CompetitionRoom>) this.getSqlLoginClient().queryForList(SPACE_NAME + "load_all_competition_room", map);
	}

	public int addCompetitionRoomPlayerCount(long keyId, int addCount) {
		Map<String, Object> map = new HashMap<>(8);
		map.put("keyId", keyId);
		map.put("addCount", addCount);
		try {
			return this.getSqlLoginClient().update(SPACE_NAME + "add_competition_room_player_count", map);
		} catch (Exception e) {
			LogUtil.errorLog.error("addGoldRoomPlayerCount|error|" + keyId + "|" + addCount, e);
		}
		return 0;
	}

	public long addCompetitionCurPlayingTableCount(long playingId, int addCount)  {return addCompetitionCurPlayingTableCount(playingId,addCount,0);}
	public long addCompetitionCurPlayingTableCount(long playingId, int addCount, int defaultCount)  {
		Map<String, Object> map = new HashMap<>(8);
		map.put("playingId", playingId);
		map.put("addCount", addCount);
//		map.put("defaultCount", defaultCount);
		try {
			return (long)this.getSqlLoginClient().insert(SPACE_NAME + "add_competition_curPlayingTableCount", map);
		} catch (Exception e) {
			LogUtil.errorLog.error("addCompetitionCurPlayingTableCount|error|" + playingId + "|" + addCount, e);
		}
		return 0;
	}

	public int loadCompetitionCurPlayingTableCount(long playingId)  {
		Map<String, Object> map = new HashMap<>(8);
		map.put("playingId", playingId);
		try {
			return (int)this.getSqlLoginClient().insert(SPACE_NAME + "load_competition_curPlayingTableCount", map);
		} catch (Exception e) {
			LogUtil.errorLog.error("loadCompetitionCurPlayingTableCount|error|" + playingId , e);
		}
		return 0;
	}

	public int deleteCompetitionRoomUser(long roomId, long userId, long playingId) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("roomId", String.valueOf(roomId));

		map.put("userId", String.valueOf(userId));

		if (playingId != 0L) {
			map.put("playingId", String.valueOf(playingId));
		}
		return this.getSqlLoginClient().delete(SPACE_NAME + "delete_competition_room_user", map);
	}

	public CompetitionPlaying loadCompetitionPlayingById(long id) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("id", id);
		return (CompetitionPlaying) this.getSqlLoginClient().queryForObject(SPACE_NAME + "load_competition_playing", map);
	}

	public int loadRoomCurrentCount(long keyId) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("keyId", keyId);
		return (int) this.getSqlLoginClient().queryForObject(SPACE_NAME + "competition_room_get_currentCount", map);
	}

	public int queryPlayingTableByRoomUser(long playingId,long userId) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("playingId", playingId);
		map.put("userId", userId);
		return (int) this.getSqlLoginClient().queryForObject(SPACE_NAME + "room_queryPlayingTableByRoomUser", map);
	}

	public List<CompetitionRoomUser> loadCompetitionUser(long playingId, long serverId) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("playingId", playingId);
		map.put("serverId", serverId);
		return  this.getSqlLoginClient().queryForList(SPACE_NAME + "load_competition_user", map);
	}

}
