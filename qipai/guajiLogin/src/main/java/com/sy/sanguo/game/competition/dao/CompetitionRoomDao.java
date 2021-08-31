package com.sy.sanguo.game.competition.dao;

import com.sy.sanguo.game.competition.model.db.CompetitionBaseDBPojo;
import com.sy.sanguo.game.competition.model.db.CompetitionRoom;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

/**
 * @author Guang.OuYang
 * @date 2020/5/20-11:40
 */
@Component
public class CompetitionRoomDao extends CompetitionCommonDao {
	public String getSpaceName() {
		return CompetitionBaseDBPojo.SPACE_NAME_1 + ".room";
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
			LogUtil.e("CompetitionRoomDao.queryByPlayingCountErr",e);
		}
		return 0;
	}

	/**
	 *@description 找到当前赛事没有开赛并且返回缺少人数
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/9
	 */
	public CompetitionRoom queryNotStartRoomByCurCount(Long playingId) {
		try {
			return (CompetitionRoom) getSql().queryForObject(getSpaceName() + "_queryNotStartRoomByCurCount", new HashMap<String, Object>() {
				{
					this.put("playingId", playingId);
				}
			});
		} catch (SQLException e) {
			LogUtil.e(".CompetitionRoomDao.queryNotStartRoomByCurCountErr",e);
		}

		return null;
	}

	/**
	 *@description 找到当前赛事没有开赛并且返回缺少人数
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/9
	 */
	public int queryCountByPlayingId(Long playingId, Long serverId, int curStep, int curRound) {
		try {
			return (int) getSql().queryForObject(getSpaceName() + "_queryCountByPlayingId", new HashMap<String, Object>() {
				{
					this.put("playingId", playingId);
					this.put("serverId", serverId);
					this.put("curStep", curStep);
					this.put("curRound", curRound);
				}
			});
		} catch (SQLException e) {
			LogUtil.e(".CompetitionRoomDao.queryNotStartRoomByCurCountErr",e);
		}
		return 0;
	}

	/**
	 *@description 找到当前赛事没有开赛并且返回缺少人数
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/9
	 */
	public List<CompetitionRoom> queryNotStartRooms(Long playingId) {
		try {
			return  getSql().queryForList(getSpaceName() + "_queryNotStartRooms", new HashMap<String, Object>() {
				{
					this.put("playingId", playingId);
				}
			});
		} catch (SQLException e) {
			LogUtil.e(".CompetitionRoomDao.queryNotStartRoomByCurCountErr",e);
		}

		return null;
	}

	/**
	 *@description 通过配置ID找到该服务器配置的玩法
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/9
	 */
	public Long queryPlayTypeByRoomConfigId(Long roomConfigId) {
		try {
			return (Long)getSql().queryForObject(getSpaceName() + "_queryPlayTypeByRoomConfigId", new HashMap<String, Object>(){
				{
					this.put("roomConfigId", roomConfigId);
				}
			});
		} catch (SQLException e) {
			LogUtil.e("CompetitionRoomDao.queryPlayTypeByRoomConfigIdErr", e);
		}

		return null;
	}

	/**
	 *@description 通过配置ID找到该服务器配置的玩法
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/9
	 */
	public Long queryPlayCountByRoomConfigId(Long roomConfigId) {
		try {
			return (Long)getSql().queryForObject(getSpaceName() + "_queryPlayCountByRoomConfigId", new HashMap<String, Object>(){
				{
					this.put("roomConfigId", roomConfigId);
				}
			});
		} catch (SQLException e) {
			LogUtil.e("CompetitionRoomDao.queryPlayTypeByRoomConfigIdErr", e);
		}

		return null;
	}

}
