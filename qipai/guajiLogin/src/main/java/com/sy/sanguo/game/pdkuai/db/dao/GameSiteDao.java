package com.sy.sanguo.game.pdkuai.db.dao;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.game.pdkuai.db.bean.GameSite;
import com.sy.sanguo.game.pdkuai.db.bean.GameSiteAward;
import com.sy.sanguo.game.pdkuai.db.bean.MatchInviteCode;
import com.sy.sanguo.game.pdkuai.db.bean.UserGameSite;

public class GameSiteDao extends BaseDao {
	
	private static GameSiteDao _inst = new GameSiteDao();

	public static GameSiteDao getInstance() {
		return _inst;
	}
	
	/**
	 * 查询所有比赛场
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<GameSite> queryAllGameSite() {
		
		List<GameSite> gameSites;
		try {
			gameSites = (List<GameSite>) getSql().queryForList("gamesite.queryAllGameSite");
		} catch (SQLException e) {
			gameSites = new ArrayList<GameSite>();
			e.printStackTrace();
			GameBackLogger.SYS_LOG.error("GameSiteDao.queryAllGameSite error:", e);
		}
		
		return gameSites;
	}
	
	/**
	 * 根据userId查询玩家比赛场数据
	 * @param userId
	 * @return
	 */
	public UserGameSite queryUserGameSite(long userId) {
		UserGameSite userGameSite = null;
		try {
			SqlMapClient sql = getSql();
			userGameSite = (UserGameSite) sql.queryForObject("gamesite.queryUserGameSite", userId);
		} catch (SQLException e) {
			userGameSite = null;
			e.printStackTrace();
			GameBackLogger.SYS_LOG.error("GameSiteDao.queryUserGameSite error:", e);
		}
	
		return userGameSite;
	}
	
	/**
	 * 添加玩家比赛场数据
	 * @param sqlMap
	 * @throws Exception
	 */
	public void addUserGameSite(Map<String, Object> sqlMap) {

		try {
			getSql().insert("gamesite.addUserGameSite", sqlMap);
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("GameSiteDao.addUserGameSite error:", e);
		}
	}

	/**
	 * 根据比赛场ID查询当前报名人数
	 * @param gameSiteId
	 * @return
	 */
	public int getApplyNumber(int gameSiteId) {
		int applyNumber;
		try {
			applyNumber = (int) getSql().queryForObject("gamesite.getApplyNumber", gameSiteId);
		} catch (SQLException e) {
			applyNumber = 0;
			e.printStackTrace();
			GameBackLogger.SYS_LOG.error("GameSiteDao.getApplyNumber error:", e);
		}
		
		return applyNumber;
	}
	
	/**
	 * 根据比赛场ID查询比赛场数据
	 * @param gameSiteId
	 * @return
	 */
	public GameSite getGameSiteById(int gameSiteId) {
		GameSite gameSite = null;
		try {
			gameSite = (GameSite) getSql().queryForObject("gamesite.getGameSiteById", gameSiteId);
		} catch (SQLException e) {
			gameSite = null;
			e.printStackTrace();
			GameBackLogger.SYS_LOG.error("GameSiteDao.getGameSiteById error:", e);
		}
		return gameSite;
	}

	/**
	 * 修改玩家比赛场数据
	 * @param sqlMap
	 * @return
	 */
	public int updateUserGameSite(Map<String, Object> sqlMap) {
		int count = 0;
		
		try {
			count = getSql().update("gamesite.updateUserGameSite", sqlMap);
		} catch (SQLException e) {
			e.printStackTrace();
			GameBackLogger.SYS_LOG.error("GameSiteDao.updateUserGameSite error:", e);
		}
		
		return count;
	}

	/**
	 * 获取玩家比赛场奖品列表
	 * @param userId
	 */
	@SuppressWarnings("unchecked")
	public List<GameSiteAward> takeAwardList(long userId) {
		List<GameSiteAward> awards = new ArrayList<>();
		
		try {
			Object object = getSql().queryForList("gamesite.takeAwardList", userId);
			if (object != null) {
				awards = (List<GameSiteAward>) object;
			}
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("GameSiteDao.takeAwardList error:", e);
		}
		
		return awards;
	}

	/**
	 * 根据奖品取奖品数据
	 * @param awardId
	 * @return
	 */
	public GameSiteAward takeAwardById(long id) {
		
		GameSiteAward gameSiteAward = null;
		
		try {
			Object object = getSql().queryForObject("gamesite.takeAwardById", id);
			if (object != null) {
				gameSiteAward = (GameSiteAward) object;
			}
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("GameSiteDao.takeAwardById error:", e);
		}
		
		return gameSiteAward;
	}

	/**
	 * 修改比赛场奖品的状态
	 * @param sqlMap
	 */
	public int updateAwardStatus(Map<String, Object> sqlMap) {
		int count = 0;
		
		try {
			Object object = getSql().update("gamesite.updateAwardStatus", sqlMap);
			if (object != null) {
				count = (int) object;
			}
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("GameSiteDao.updateAwardStatus error:", e);
		}
		
		return count;
	}

	/**
	 * 某个玩家是否可领奖
	 * @param userId
	 * @return
	 */
	public int userCanAwardCnt(long userId) {
		int count = 0;
		
		try {
			Object object = getSql().queryForObject("gamesite.userCanAwardCnt", userId);
			if (object != null) {
				count = (int) object;
			}
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("GameSiteDao.userCanAwardCnt error:", e);
		}
		
		return count;
	}
	
	/**
	 * 获取某个比赛场的排行榜
	 * @param amount
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserGameSite> userGameSiteRank(int gameSiteId, int amount) {
		
		List<UserGameSite> userGameSites = null;
		
		Map<String, Object> sqlMap = new HashMap<String, Object>();
		sqlMap.put("gameSiteId", gameSiteId);
		
		try {
			if (amount > 0) {
				sqlMap.put("amount", amount);
			}			
			Object object = getSql().queryForList("gamesite.userGameSiteRank", sqlMap);
			
			if (object != null) {
				userGameSites = (List<UserGameSite>) object;
			}
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("GameSiteDao.userGameSiteRank error:", e);
		}
	
		return userGameSites;
	}
	
	/**
	 * 取比赛场报名的玩家ID集合
	 * @param gameSiteId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserGameSite> gameSiteApplyUserId(int gameSiteId) {
		
		List<UserGameSite> userIds = null;
		try {
			Object object = getSql().queryForList("gamesite.gameSiteApplyUserId", gameSiteId);
			
			if (object != null) {
				userIds = (List<UserGameSite>) object;
			}
		} catch (SQLException e) {
			
			GameBackLogger.SYS_LOG.error("GameSiteDao.gameSiteApplyUserId error:", e);
		}
		
		return userIds;
	}
	
	/**
	 * 修改比赛场当前最大报名人数
	 * @param gameSiteId
	 * @param applyMaxNumber
	 * @return
	 */
	public synchronized int updateApplyMaxNumber(int gameSiteId, int applyMaxNumber) {
		
		int count = 0;
		
		Map<String, Object> sqlMap = new HashMap<String, Object>();
		sqlMap.put("gameSiteId", gameSiteId);
		sqlMap.put("applyMaxNumber", applyMaxNumber);
		
		try {
			Object object = getSql().update("gamesite.updateApplyMaxNumber", sqlMap);
			if (object != null) {
				count = (int) object;
			}
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("GameSiteDao.updateApplyMaxNumber error:", e);
		}
		
		return count;
	}
	
	/**
	 * 取某个比赛场的邀请码的数量
	 * 
	 * @param gameSiteId
	 * @return
	 */
	public int oneMatchInviteCodeCnt(int gameSiteId) {
		int count = 0;
		try {
			count = (int) getSql().queryForObject("gamesite.oneMatchInviteCodeCnt", gameSiteId);
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("GameSiteDao.oneMatchInviteCodeCnt error:", e);
		}
		
		return count;
	}
	
	/**
	 * 添加比赛场邀请码
	 * @param gameSiteId
	 * @param inviteCode
	 */
	public void addMatchInviteCode(int gameSiteId, String inviteCode) {
		
		Map<String, Object> sqlMap = new HashMap<String, Object>();
		sqlMap.put("gameSiteId", gameSiteId);
		sqlMap.put("inviteCode", inviteCode);
		sqlMap.put("useFlag", 0);
		sqlMap.put("useUserId", 0);
		sqlMap.put("useServer", 0);
		sqlMap.put("useTime", null);
		
		try {
			getSql().insert("gamesite.addMatchInviteCode", sqlMap);
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("GameSiteDao.addMatchInviteCode error:", e);
		}
	}
	
	/**
	 * 取某比赛场的某个邀请码
	 * @param gameSiteId
	 * @param inviteCode
	 * @return
	 */
	public MatchInviteCode getMatchInviteCode(int gameSiteId, String inviteCode) {
		Map<String, Object> sqlMap = new HashMap<String, Object>();
		sqlMap.put("gameSiteId", gameSiteId);
		sqlMap.put("inviteCode", inviteCode);
		
		MatchInviteCode MatchInviteCode = null;
		try {
			Object object = getSql().queryForObject("gamesite.getMatchInviteCode", sqlMap);
			if (object != null) {
				MatchInviteCode = (MatchInviteCode) object;
			}
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("GameSiteDao.getMatchInviteCode error:", e);
		}
		
		return MatchInviteCode;
	}
	
	public int updateMatchInviteCode(long id, long userId) {
		int count = 0;
		
		Map<String, Object> sqlMap = new HashMap<String, Object>();
		sqlMap.put("id", id);
		sqlMap.put("useUserId", userId);
		sqlMap.put("useFlag", 1);
		sqlMap.put("useTime", new Timestamp(System.currentTimeMillis()));
		
		try {
			Object object = getSql().update("gamesite.updateMatchInviteCode", sqlMap);
			if (object != null) {
				count = (int) object;
			}
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("GameSiteDao.updateMatchInviteCode error:", e);
		}
		
		return count;
	}
}
