package com.sy.sanguo.game.competition.dao;

import com.sy.sanguo.game.competition.model.db.CompetitionBaseDBPojo;
import com.sy.sanguo.game.competition.model.db.CompetitionPlayingDB;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Guang.OuYang
 * @date 2020/5/20-11:40
 */
@Component
public class CompetitionPlayingDao<T extends CompetitionBaseDBPojo> extends CompetitionCommonDao<T> {
	public String getSpaceName() {
		return CompetitionPlayingDB.SPACE_NAME;
	}


	public int updateCurHuman(T param) {
		try {
			return (int)getSql().update(getSpaceName() + "_update_curHuman", param);
		}
		catch (SQLException e) {
			LogUtil.e("CompetitionPlayingDao.updateCurHumanErr", e);
		}
		return -1;
	}

	public int updateAliveHuman(T param) {
		try {
			return (int)getSql().update(getSpaceName() + "_update_aliveHuman", param);
		}
		catch (SQLException e) {
			LogUtil.e("CompetitionPlayingDao.updateCurHumanErr", e);
		}
		return -1;
	}

	public int queryAliveByPlayingId(Long playingId) {
		try {
			return (int) getSql().queryForObject(getSpaceName() + "_select_aliveHuman", playingId);
		} catch (SQLException e) {
			LogUtil.e("CompetitionPlayingDao.queryAliveByPlayingId", e);
		}
		return -1;
	}

	public int setAliveByPlayingId(Long playingId,int defaultValue, int newValue) {
		try {
			return getSql().update(getSpaceName() + "_set_aliveHuman", new HashMap<String, Object>(){
				{
					this.put("playingId",playingId);
					this.put("defaultValue",defaultValue);
					this.put("newValue",newValue);
				}
			});
		} catch (SQLException e) {
			LogUtil.e("CompetitionPlayingDao.queryAliveByPlayingId", e);
		}
		return -1;
	}

	public int queryCurHumanByPlayingId(long type, int category, int entrance) {
		try {
			return (int) getSql().queryForObject(getSpaceName() + "_select_curHuman", new HashMap<String, Object>()
					{
						{
							this.put("type",type);
							this.put("category",category);
							this.put("entrance",entrance);
						}
					}
			);
		}
		catch (SQLException e) {
			LogUtil.e("CompetitionPlayingDao.queryCurHumanByPlayingIdErr", e);
		}
		return -1;
	}

	public int queryCurTableCount(long playingId) {
		try {
			return (int) getSql().queryForObject(getSpaceName() + "_select_curPlayingTableCount",playingId);
		} catch (SQLException e) {
			LogUtil.e("CompetitionPlayingDao.queryCurTableCountErr", e);
		}
		return -1;
	}

	/**
	 *@description 批量更新状态
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/22
	 */
	public int batchUpdateStatus(int defaultStatus, int status) {
		try {
			return getSql().update(getSpaceName() + "_update_batch_clearingEnd_status",new HashMap<String, Object>() {
				{
					this.put("default_status", defaultStatus);
					this.put("status", status);
					this.put("deleteTime", new Date());
				}
			});
		}
		catch (SQLException e) {
			LogUtil.e("CompetitionPlayingDao.batchUpdateStatusErr", e);
		}

		return -1;
	}

	/**
	 *@description
	 *@param checkBeginHuman true更新时必须校验是否满人
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/27
	 */
	public int updateStatus(Long id, int defaultStatus, int status, boolean checkBeginHuman, int type) {
		try {
			return getSql().update(getSpaceName() + "_update_batch_clearingEnd_status",new HashMap<String, Object>() {
				{
					this.put("id", id);
					this.put("default_status", defaultStatus);
					this.put("status", status);
					this.put("type", type);
					this.put("beginHuman", checkBeginHuman ? 1 : 0);
				}
			});
		} catch (SQLException e) {
			LogUtil.e("CompetitionPlayingDao.updateStatusErr", e);
		}

		return -1;
	}

	public Optional<T> queryByConfigId(long playingConfigId) {
		try {
			return Optional.ofNullable((T) getSql().queryForObject(getSpaceName() + "_select_config_max_id", playingConfigId));
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}

	public int updateStatusByApplyTime() {
		try {
			return getSql().update(getSpaceName() + "_update_status_byApplyTime");
		} catch (SQLException e) {
			LogUtil.e("CompetitionPlayingDao.updateStatusByApplyTimeErr", e);
		}

		return -1;
	}


	/**
	 *@description 当前赛事没有可报名的比赛了,开放下一个报名入口
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/25
	 */
	public int updateStatusByBeginHuman(List<Long> ids) {
		try {
			return getSql().update(getSpaceName() + "_update_status_byMaxHuman", ids.stream().map(String::valueOf).collect(Collectors.joining(",")));
		} catch (SQLException e) {
			LogUtil.e("CompetitionPlayingDao.updateStatusByBeginHumanErr", e);
		}

		return -1;
	}


	/**
	 *@description 找到当前没有报名中状态的赛事
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/25
	 */
	public Optional<List<CompetitionPlayingDB>> queryByNotExistsStatus1() {
		try {
			return Optional.ofNullable(getSql().queryForList(getSpaceName() + "_select_notExistsStatus1"));
		}
		catch (SQLException e) {
			LogUtil.e("CompetitionPlayingDao.queryByNotExistsStatus1Err", e);
		}

		return Optional.empty();
	}
	/**
	 *@description 找到当前满足开赛条件的配置
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/25
	 */
	public Optional<List<T>> queryByBeginPlayingForList(T param) {
		try {
			return Optional.ofNullable(getSql().queryForList(getSpaceName() + "_selectByBeginPlaying", param));
		}
		catch (SQLException e) {
			LogUtil.e("CompetitionPlayingDao.queryByBeginPlayingForListErr", e);
		}

		return Optional.empty();
	}

	/**
	 *@description 找到当前满足开赛条件的配置
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/25
	 */
	public Optional<List<T>> queryByEnter(T param) {
		try {
			return Optional.ofNullable((List<T>) getSql().queryForList(getSpaceName() + "_selectByLastApply", param));
		}
		catch (SQLException e) {
			LogUtil.e("CompetitionPlayingDao.queryByEnterErr", e);
		}

		return Optional.empty();
	}

	public int updateCurStepRound(T param) {
		try {
			return (int) this.getSql().update(getSpaceName() + "_update_playing_curStepRound", param);
		} catch (Exception e) {
			LogUtil.e("CompetitionPlayingDao.updateCurStepRound", e);
		}
		return 0;
	}


	public Optional<T> queryForSingle(T param) {
		try {
			return Optional.ofNullable((T) getSql().queryForObject(getSpaceName() + "_selectBySingle", param));
		} catch (SQLException e) {
			LogUtil.e(this.getClass().getSimpleName()+".CompetitionPlayingDao.queryForSingleErr", e);
		}

		return Optional.empty();
	}

	public int updateByType(T param) {
		try {
			return getSql().update(getSpaceName() + "_updateByType", param);
		} catch (Exception e) {
		}
		return 0;
	}
}
