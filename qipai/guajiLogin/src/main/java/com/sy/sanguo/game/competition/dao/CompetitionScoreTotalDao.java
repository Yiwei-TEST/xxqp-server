package com.sy.sanguo.game.competition.dao;

import com.sy.sanguo.game.competition.model.db.CompetitionBaseDBPojo;
import com.sy.sanguo.game.competition.model.db.CompetitionScoreTotalDB;
import com.sy.sanguo.game.competition.model.param.HistoryParam;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Guang.OuYang
 * @date 2020/5/20-11:40
 */
@Component
public class CompetitionScoreTotalDao<T extends CompetitionBaseDBPojo> extends CompetitionCommonDao<T> {
	public String getSpaceName() {
		return CompetitionScoreTotalDB.SPACE_NAME;
	}

	public Optional<List<CompetitionScoreTotalDB>> queryByRank(CompetitionScoreTotalDB param) {
		try {
			return Optional.ofNullable(getSql().queryForList(getSpaceName() + "_selectByOrderRank", param));
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	public Optional<List<CompetitionScoreTotalDB>> queryByInRoom(CompetitionScoreTotalDB param) {
		try {
			return Optional.ofNullable(getSql().queryForList(getSpaceName() + "_selectByInRoom", param));
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	public Optional<List<CompetitionScoreTotalDB>> queryByRankByUserId(Long playingId, String userIds) {
		try {
			return Optional.ofNullable(getSql().queryForList(getSpaceName() + "_selectByOrderRankByUserId", new HashMap<String, Object>(){
				{
					this.put("playingId", playingId);
					this.put("userIds", userIds);
				}
			}));
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	public Optional<CompetitionScoreTotalDB> queryByRankSimple(CompetitionScoreTotalDB param) {
		try {
			return Optional.ofNullable((CompetitionScoreTotalDB) getSql().queryForObject(getSpaceName() + "_selectByOrderRankSimple", param));
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return Optional.empty();

	}

	public Optional<HistoryParam> queryByHistory(Long userId) {
		try {
			return Optional.ofNullable((HistoryParam) getSql().queryForObject(getSpaceName() + "_selectByHistory", userId));
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	/**
	 *@description 批量总榜初始化
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/3
	 */
//	public long batchInsertScoreDetail(Long playingId, Integer defaultScore) {
//		try {
//			return (long)getSql().update(getSpaceName() + "_batchInsertScoreDetail", new HashMap<String, Object>(){
//				{
//					this.put("playingId",playingId);
//					this.put("defaultScore",defaultScore);
//				}
//			});
//		}
//		catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return 0;
//	}
	/**
	 *@description 批量总榜初始化
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/3
	 */
	public long batchInsertScoreDetail(Long playingId, Integer defaultScore, String userIds) {
		try {
			return (long)getSql().update(getSpaceName() + "_batchInsertScoreDetail", new HashMap<String, Object>(){
				{
					this.put("playingId",playingId);
					this.put("defaultScore",defaultScore);
					this.put("userIds",userIds);
				}
			});
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 *@description 更新玩家排名
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/3
	 */
	public long updateRankByPlayingId(CompetitionScoreTotalDB arg) {
		try {
			return (long)getSql().update(getSpaceName() + "_updateRankByPlayingId", arg);
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 *@description 以分数更新玩家状态,或排名
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/3
	 */
	public long updateStatusByPlayingId(CompetitionScoreTotalDB arg) {
		try {
			return (long)getSql().update(getSpaceName() + "_updateStatusByPlayingId", arg);
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}


	/**
	 *@description
	 *@param changeStatus true变更状态为指定状态
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/7
	 */
	public int insertBatch(boolean changeStatus, boolean changeScore, List<T> params) {
        return insertBatch(changeStatus, changeScore, false, params);
    }

	public int insertBatch(boolean changeStatus, boolean changeScore, boolean changeRank, List<T> params) {
		try {
			if(!changeStatus && !changeScore) {
				return 0;
			}

			return getSql().update(getSpaceName() + "_insert_batch", new HashMap<String, Object>(){
				{
					if(changeStatus) {
						this.put("changeStatus", changeStatus);
					}

					if(changeScore){
						this.put("changeScore", changeScore);
					}

					if(changeRank){
						this.put("changeRank", changeRank);
					}

					this.put("srcs",params);
				}
			});
		} catch (SQLException e) {
			LogUtil.e(this.getClass().getSimpleName()+".CompetitionCommonDao.insertBatchErr", e);
		}

		return -1;
	}


	public long updateInRoom(Long playingId, List<Long> userIds, boolean inRoom) {
		try {
			return (long)getSql().update(getSpaceName() + "_updateInRoom", new HashMap<String, Object>(){
				{
					this.put("playingId", playingId);
					if (userIds != null) {
						this.put("userIds", userIds.stream().map(v -> v.toString()).collect(Collectors.joining(",")));
					}
					this.put("inRoom",inRoom);
				}
			});
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}


	public long updatePlayStatus(Long playingId, List<Long> userIds) {
		try {
			return (long)getSql().update(getSpaceName() + "_updatePlayStatus", new HashMap<String, Object>(){
				{
					this.put("playingId", playingId);
					if (userIds != null) {
						this.put("userIds", userIds.stream().map(v -> v.toString()).collect(Collectors.joining(",")));
					}
				}
			});
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}


	public long updateScoreBasicRatio(Long id, float scoreBasicRatio) {
		try {
			return (long)getSql().update(getSpaceName() + "_updateScoreBasicRatio", new HashMap<String, Object>(){
				{
					this.put("scoreBasicRatio", scoreBasicRatio);
					this.put("id", id);
				}
			});
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
