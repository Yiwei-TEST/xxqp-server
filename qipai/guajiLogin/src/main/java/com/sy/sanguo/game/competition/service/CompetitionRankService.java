package com.sy.sanguo.game.competition.service;

import com.sy.sanguo.game.competition.dao.CompetitionScoreDetailDao;
import com.sy.sanguo.game.competition.dao.CompetitionScoreTotalDao;
import com.sy.sanguo.game.competition.model.db.CompetitionScoreDetailDB;
import com.sy.sanguo.game.competition.model.db.CompetitionScoreTotalDB;
import com.sy.sanguo.game.competition.model.enums.CompetitionScoreDetailStatusEnum;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 榜单
 * @author Guang.OuYang
 * @date 2020/5/20-18:07
 */
@Service
public class CompetitionRankService {
	@Autowired
	private CompetitionScoreTotalDao<CompetitionScoreTotalDB> competitionScoreTotalDao;

	@Autowired
	private CompetitionScoreDetailDao<CompetitionScoreTotalDB> competitionScoreDetailDao;

	/**
	 *@description 获取榜单
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/29
	 */
	public List<CompetitionScoreTotalDB> getRank(CompetitionScoreTotalDB param) {
		return competitionScoreTotalDao.queryByRank(param).orElse(Collections.emptyList());
	}

	/**
	 *@description 获取榜单
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/29
	 */
	public List<CompetitionScoreTotalDB> getRankByUserId(Long playingId, List<Long> userIds) {
		return competitionScoreTotalDao.queryByRankByUserId(playingId, userIds.stream().map(v -> v.toString()).collect(Collectors.joining(","))).orElse(Collections.emptyList());
	}

	/**
	 *@description 获取榜单
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/29
	 */
	public CompetitionScoreTotalDB getRankSimple(CompetitionScoreTotalDB param) {
		return competitionScoreTotalDao.queryByRankSimple(param).orElse(null);
	}

	/**
	 *@description 获取榜单
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/29
	 */
	public CompetitionScoreTotalDB getRankForSelf(CompetitionScoreTotalDB param) {
		param.setLimit(1);
		param.setStatus(CompetitionScoreDetailStatusEnum.RISE_IN_RANK);
		List<CompetitionScoreTotalDB> rank = getRank(param);
		return CollectionUtils.isEmpty(rank) ? null : rank.get(0);
	}

	/**
	 *@description 获取榜单
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/29
	 */
	public CompetitionScoreDetailDB getDetailRankForSelf(CompetitionScoreDetailDB param) {
		param.setLimit(1);
		List<CompetitionScoreDetailDB> rank = competitionScoreDetailDao.queryByRank(param).orElse(Collections.emptyList());
		return CollectionUtils.isEmpty(rank) ? null : rank.get(0);
	}
}
