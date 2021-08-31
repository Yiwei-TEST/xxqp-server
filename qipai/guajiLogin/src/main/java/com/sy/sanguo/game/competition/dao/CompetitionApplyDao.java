package com.sy.sanguo.game.competition.dao;

import com.sy.sanguo.game.competition.model.db.CompetitionApplyDB;
import com.sy.sanguo.game.competition.model.db.CompetitionBaseDBPojo;
import com.sy.sanguo.game.competition.model.enums.CompetitionApplyStatusEnum;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.apache.commons.collections.CollectionUtils;
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
public class CompetitionApplyDao<T extends CompetitionBaseDBPojo> extends CompetitionCommonDao {
	public String getSpaceName() {
		return CompetitionApplyDB.SPACE_NAME;
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

	/**
	 *@description
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/27
	 */
	public int updatePlay(Long playingId, int play, List<Long> userIds) {
		try {
			return getSql().update(getSpaceName() + "_update_play",new HashMap<String, Object>() {
				{
					this.put("playingId", playingId);
					this.put("play", play);
					if(!CollectionUtils.isEmpty(userIds)){
						this.put("userIds", userIds.stream().map(v -> v.toString()).collect(Collectors.joining(",")));
					}
				}
			});
		} catch (SQLException e) {
			LogUtil.e("competition|updatePlay|error|"+playingId+"|"+ play+"|"+ userIds, e);
		}

		return -1;
	}

	/**
	 *@description
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/27
	 */
	public int updateSignShow(Long playingId, int signShow, Long userId) {
		try {
			return getSql().update(getSpaceName() + "_updateSignShow",new HashMap<String, Object>() {
				{
					this.put("playingId", playingId);
					this.put("signShow", signShow);
					this.put("userId", userId);
				}
			});
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

		return -1;
	}


	/**
	 *@description 获取当前赛事报名的所有玩家id
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/4
	 */
	public Optional<Long> queryByUserPlay(Long userId) {
		try {
			return Optional.ofNullable((Long) getSql().queryForObject(getSpaceName() + "_selectByUserPlay", CompetitionApplyDB.builder().userId(userId).status(CompetitionApplyStatusEnum.NORMAL).play(1).build()));
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}

	/**
	 *@description 获取当前赛事报名的所有玩家id
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/4
	 */
	public Optional<List<Long>> queryForUsersByPlayingId(Long playingId) {
		try {
			return Optional.ofNullable(getSql().queryForList(getSpaceName() + "_selectByPlayingId", CompetitionApplyDB.builder().playingId(playingId).status(CompetitionApplyStatusEnum.NORMAL).build()));
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}

	/**
	 *@description 获取当前赛事报名的所有玩家id
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/4
	 */
	public Optional<List<Long>> queryForUsersByPlayingId(Long playingId, int signShow) {
		try {
			return Optional.ofNullable(getSql().queryForList(getSpaceName() + "_selectByPlayingId", CompetitionApplyDB.builder().playingId(playingId).status(CompetitionApplyStatusEnum.NORMAL).signShow(signShow).build()));
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}

	/**
	 *@description 获取当前赛事报名的所有玩家id
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/4
	 */
	public Integer queryForListByType(Long userId, Long playingConfigId) {
		try {
			return (Integer) getSql().queryForObject(getSpaceName() + "_selectByListForType", CompetitionApplyDB.builder()
					.playingConfigId(playingConfigId)
					.userId(userId).status(CompetitionApplyStatusEnum.NORMAL).build());
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

		return 0;
	}

	/**
	 *@description
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/8/4
	 */
	public Optional<CompetitionApplyDB> queryForSingle(CompetitionApplyDB param) {
		try {
			return Optional.ofNullable((CompetitionApplyDB) getSql().queryForObject(getSpaceName() + "_selectBySingle", param));
		} catch (SQLException e) {
			LogUtil.e(this.getClass().getSimpleName()+".CompetitionCommonDao.selectBySingleErr", e);
		}

		return Optional.empty();
	}
}
