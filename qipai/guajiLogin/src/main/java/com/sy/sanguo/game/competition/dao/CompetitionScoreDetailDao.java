package com.sy.sanguo.game.competition.dao;

import com.sy.sanguo.game.competition.model.db.CompetitionBaseDBPojo;
import com.sy.sanguo.game.competition.model.db.CompetitionScoreDetailDB;
import com.sy.sanguo.game.competition.model.db.CompetitionScoreTotalDB;
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
public class CompetitionScoreDetailDao<T extends CompetitionBaseDBPojo> extends CompetitionCommonDao<T> {
	public String getSpaceName() {
		return CompetitionScoreDetailDB.SPACE_NAME;
	}

	public Optional<List<CompetitionScoreDetailDB>> queryByRank(CompetitionScoreDetailDB param) {
		try {
			return Optional.ofNullable(getSql().queryForList(getSpaceName() + "_selectByOrderRank", param));
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	public Optional<CompetitionScoreTotalDB> queryByRankSimple(CompetitionScoreDetailDB param) {
		try {
			return Optional.ofNullable((CompetitionScoreTotalDB) getSql().queryForObject(getSpaceName() + "_selectByOrderRankSimple", param));
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

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

	public long updateRankByPlayingId(CompetitionScoreDetailDB arg) {
		try {
			return (long)getSql().update(getSpaceName() + "_updateRankByPlayingId", arg);
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public long updateTopNByPlayingId(Long playingId, Integer curStep, Integer curRound, int topN) {
		try {
			return (long)getSql().update(getSpaceName() + "_updateTopNByPlayingId", new HashMap<String, Object>(){
				{
					this.put("playingId", playingId);
					this.put("curStep", curStep);
					this.put("curRound", curRound);
					this.put("limit", topN);
				}
			});
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	public long updateTableTopNByPlayingId(Long playingId, Integer curStep, Integer curRound, int topN) {
		try {
			return (long)getSql().update(getSpaceName() + "_updateTableTopNByPlayingId", new HashMap<String, Object>(){
				{
					this.put("playingId", playingId);
					this.put("curStep", curStep);
					this.put("curRound", curRound);
					this.put("limit", topN);
				}
			});
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public long updateScoreBasicRatio(List<Long> ids, float scoreBasicRatio) {
		try {
			return (long)getSql().update(getSpaceName() + "_updateScoreBasicRatio", new HashMap<String, Object>(){
				{
					this.put("scoreBasicRatio", scoreBasicRatio);
					this.put("ids", ids.stream().map(String::valueOf).collect(Collectors.joining(",")));
				}
			});
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
