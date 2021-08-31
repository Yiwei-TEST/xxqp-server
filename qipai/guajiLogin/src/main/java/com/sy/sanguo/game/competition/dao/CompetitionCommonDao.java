package com.sy.sanguo.game.competition.dao;

import com.sy.sanguo.game.competition.model.db.CompetitionBaseDBPojo;
import com.sy.sanguo.game.pdkuai.db.dao.BaseDao;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 *
 * 比赛场通用DAO
 * @author Guang.OuYang
 * @date 2020/5/20-11:40
 */
@Component
public abstract class CompetitionCommonDao<T extends CompetitionBaseDBPojo> extends BaseDao {
	public abstract String getSpaceName();

	public long insert(T param) {
		try {
			return (int) getSql().insert(getSpaceName() + "_insert", param);
		} catch (SQLException e) {
			LogUtil.e(this.getClass().getSimpleName()+".CompetitionCommonDao.insertErr", e);
		}

		return -1;
	}

	public int insertBatch(List<T> params) {
		try {
			return getSql().update(getSpaceName() + "_insert_batch", params);
		} catch (SQLException e) {
			LogUtil.e(this.getClass().getSimpleName()+".CompetitionCommonDao.insertBatchErr", e);
		}

		return -1;
	}

	public int update(T param) {
		try {
			return (int) getSql().update(getSpaceName() + "_update", param);
		} catch (SQLException e) {
			LogUtil.e(this.getClass().getSimpleName()+".CompetitionCommonDao.updateErr", e);
		}

		return -1;
	}

	public int delete(T param) {
		param.setDeleteTime(new Date());
		return update(param);
	}

	public int deleteForReal(T param) {
		try {
			return (int) getSql().delete(getSpaceName() + "_delete", param);
		} catch (SQLException e) {
			LogUtil.e(this.getClass().getSimpleName()+".CompetitionCommonDao.deleteErr", e);
		}

		return -1;
	}

	public int queryByCount(T param) {
		try {
			return (int) getSql().queryForObject(getSpaceName() + "_selectByCount", param);
		} catch (SQLException e) {
			LogUtil.e(this.getClass().getSimpleName()+".CompetitionCommonDao.queryByCountErr", e);
		}

		return -1;
	}

	public Optional<T> queryForSingle(T param) {
		try {
			return Optional.ofNullable((T) getSql().queryForObject(getSpaceName() + "_selectByList", param));
		} catch (SQLException e) {
			LogUtil.e(this.getClass().getSimpleName()+".CompetitionCommonDao.queryForSingleErr", e);
		}

		return Optional.empty();
	}

	public Optional<List<T>> queryForList(T param) {
		try {
			return Optional.ofNullable(getSql().queryForList(getSpaceName() + "_selectByList", param));
		} catch (SQLException e) {
			LogUtil.e(this.getClass().getSimpleName()+".CompetitionCommonDao.queryForListErr", e);
		}

		return Optional.empty();
	}


	public int updateShardingTakeOver(long id, String defaultStatus, String status) {
		try {
			return getSql().update(getSpaceName() + "_update_sharding", new HashMap<String, Object>(){
				{
					this.put("id", id);
					this.put("defaultStatus", defaultStatus);
					this.put("status", status);
				}
			});
		} catch (SQLException e) {
			LogUtil.e(this.getClass().getSimpleName()+".CompetitionCommonDao.updateShardingTakeOverErr", e);
		}

		return -1;
	}

	public int releaseShardingTakeOverLock(String defaultStatus, String status) {
		try {
			return getSql().update(getSpaceName() + "_update_sharding_release_lock", new HashMap<String, Object>(){
				{
					this.put("defaultStatus", defaultStatus);
					this.put("status", status);
				}
			});
		} catch (SQLException e) {
			LogUtil.e(this.getClass().getSimpleName()+".CompetitionCommonDao.releaseShardingTakeOverLockErr", e);
		}

		return -1;
	}
}
