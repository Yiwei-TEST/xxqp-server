package com.sy599.game.db.dao;

import com.alibaba.fastjson.JSON;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy.mainland.util.CommonUtil;
import com.sy599.game.character.Player;
import com.sy599.game.common.UserResourceType;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.bean.UserExtend;
import com.sy599.game.db.bean.UserStatistics;
import com.sy599.game.gcommand.login.util.LoginUtil;
import com.sy599.game.util.*;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.*;

public class UserDao extends BaseDao {
	private static UserDao _inst = new UserDao();

	public static UserDao getInstance() {
		return _inst;
	}

	public void saveUserStatistics(final UserStatistics userStatistics) {
	    try{
//            TaskExecutor.EXECUTOR_SERVICE_STATISTICS.execute(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        getSqlLoginClient().update("saveUserStatistics", userStatistics);
//                    } catch (SQLException e) {
//                        LogUtil.dbLog.error("#UserDao.saveUserStatistics:" + JacksonUtil.writeValueAsString(userStatistics), e);
//                    }
//                }
//            });
        }catch (Exception e){
            LogUtil.dbLog.error("#UserDao.saveUserStatistics:" + JacksonUtil.writeValueAsString(userStatistics), e);
        }
	}

	public int countUserStatistics(boolean isBig,String userId,String roomType,String currentDate,String gameTypes){
		try {
			Map<String,Object> map = new HashMap<>(8);
			map.put("userId",userId);
			map.put("currentDate",currentDate);
			if (StringUtils.isNotBlank(roomType)) {
				map.put("roomType", roomType);
			}
			if (StringUtils.isNotBlank(gameTypes)) {
				map.put("gameTypes", gameTypes);
			}
			return (Integer) getSqlLoginClient().queryForObject(isBig?"countUserDjs":"countUserXjs", map);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#UserDao.countUserStatistics:" + e.getMessage(), e);
		}
		return 0;
	}

	public int countUserStatistics(boolean isBig,String userId,String roomType,String startDate,String endDate,String gameTypes){
		try {
			Map<String,Object> map = new HashMap<>(8);
			map.put("userId",userId);
			map.put("startDate",startDate);
			map.put("endDate",endDate);
			if (StringUtils.isNotBlank(roomType)) {
				map.put("roomType", roomType);
			}
			if (StringUtils.isNotBlank(gameTypes)) {
				map.put("gameTypes", gameTypes);
			}
			return (Integer) getSqlLoginClient().queryForObject(isBig?"countUserDjs":"countUserXjs", map);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#UserDao.countUserStatistics:" + e.getMessage(), e);
		}
		return 0;
	}
	
	public RegInfo selectUserByUserId(long userId) {
		RegInfo result = null;
		try {
			//先redis查询 存在直接返回 不存在查数据库且同步redis
			result = getFromCache(userId);
			if (result != null) {
				return result;
			}
			result = (RegInfo) getSqlLoginClient().queryForObject("selectUserByUserId", userId);
			setToCache(result);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#UserDao.selectUserByUserId:" + userId, e);
		}
		return result;
	}

	public void save(String flatId, String pf, Map<String, Object> paramMap) {
		try {
			// LogUtil.dbLog.info(String.format("updUser,uId:%d",
			// paramMap.get("userId")));
			getSqlLoginClient().update("updateUserInfo", paramMap);
			Object extend = paramMap.get("extend");
			if (extend!=null && ((String)extend).contains("0:1;seat:")) {
				LogUtil.e("save base info err-->userId:"+paramMap.get("userId")+",extend:"+extend);
			}
//			RegInfo inf = getFromCache(flatId, pf);
//			if (inf != null) {
//				ClassTool.setValue(inf, paramMap);
//				setToCache(inf);
//			}
		} catch (SQLException e) {
			LogUtil.dbLog.error("#PlayerDao.dbSave.Sql" + JacksonUtil.writeValueAsString(paramMap), e);
		}

	}

	public void save(String flatId, String pf, long userId, Map<String, Object> paramMap) {
		paramMap.put("userId", userId);
		save(flatId, pf, paramMap);
	}

	public void batchUpdate(final List<Map<String, Object>> list){
		try {
			if (list != null && list.size()>0) {
				SqlMapClient sqlMapClient = getSqlLoginClient();
				sqlMapClient.startBatch();
				for ( int i = 0, n = list.size(); i < n; i++) {
					Map<String, Object> paramMap = list.get(i);
					sqlMapClient.update("updateUserInfo", paramMap);
					Object extend = paramMap.get("extend");
					if (extend!=null && ((String)extend).contains("0:1;seat:")) {
						LogUtil.e("save base info err-->userId:"+paramMap.get("userId")+",extend:"+extend);
					}
				}
				sqlMapClient.executeBatch();
				LogUtil.msgLog.info("batchUpdate updateUserInfo success:count={}",list.size());
			}
		} catch (Exception e) {
			LogUtil.dbLog.error("batchUpdate updateUserInfo Exception:"+e.getMessage(),e);

			for ( int i = 0, n = list.size(); i < n; i++) {
				Map<String, Object> paramMap = list.get(i);
				try {
					getSqlLoginClient().update("updateUserInfo", paramMap);
					Object extend = paramMap.get("extend");
					if (extend != null && ((String) extend).contains("0:1;seat:")) {
						LogUtil.e("save base info err-->userId:" + paramMap.get("userId") + ",extend:" + extend);
					}
				} catch (Exception e0) {
					LogUtil.dbLog.error("singleUpdate updateUserInfo Exception:" + e0.getMessage()+",userParams="+paramMap, e0);
				}
			}
		}
	}

	/**
	 * 提交用户更新
	 *
	 * @return
	 * @throws SQLException
	 */
	public int updateUserCards(long userId, String flatId, String pf, long cards, long freeCards) {
		Map<String, Object> modify = new HashMap<String, Object>();
		modify.put("userId", userId);
		modify.put("cards", cards);
		modify.put("freeCards", freeCards);
		int update = 0;
		try {
			update = this.getSqlLoginClient().update("updateUserCards", modify);
			RegInfo inf = getFromCache(flatId, pf);
			if (inf != null) {
				inf.setCards(inf.getCards() + cards);
				inf.setFreeCards(inf.getFreeCards() + freeCards);
				setToCache(inf);
			}
		} catch (SQLException e) {
			LogUtil.dbLog.error("#PlayerDao.updateUserCards:", e);
		}
		return update;
	}

	/**
	 * 获取玩家的房卡 0cards，1freeCards
	 * @param userId
	 * @return
	 */
	public long[] loadUserCards(String userId){
		try {
			HashMap<String,Object> map = (HashMap<String,Object>)this.getSqlLoginClient().queryForObject("loadUserCards",userId);
			if (map==null||map.size()==0){
				return new long[]{0,0};
			}else{
				Object cards = map.getOrDefault("cards",0);
				Object freeCards = map.getOrDefault("freeCards",0);
				return new long[]{(cards instanceof Number)?((Number)cards).longValue():0,(freeCards instanceof Number)?((Number)freeCards).longValue():0};
			}
		} catch (SQLException e) {
			LogUtil.dbLog.error("#PlayerDao.loadUserCards:"+e.getMessage(), e);
			return new long[]{0,0};
		}
	}

	/**
	 * 保存用户信息到redis
	 * 
	 * @param userInfo
	 */
	public void setToCache(RegInfo userInfo) {
		if (userInfo != null) {// 保持2分数据同步 一份的key为userId生成key 一份是用户名和pf生成的key
//			setObject(userInfo.getFlatId()+ "_" + userInfo.getPf(), userInfo);
//			setObject(userInfo.getUserId(), userInfo);
		}
	}

	/**
	 * 根据用户名和pf生成的key查询用户信息
	 * 
	 * @param username
	 * @param pf
	 * @return
	 */
	private RegInfo getFromCache(String username, String pf) {
		return null;
	}

	/**
	 * 根据userId查询用户对象
	 * 
	 * @param userId
	 * @return
	 */
	private RegInfo getFromCache(long userId) {
		return null;
	}

	/**
	 * 根据userId生成key
	 *
	 * @param userId
	 * @return
	 */
	private String getCacheKey(long userId) {
		StringBuilder sb = new StringBuilder();
		sb.append("qipai_user_");
		sb.append(RegInfo.class.getSimpleName());
		sb.append("_");
		sb.append(userId);
		return sb.toString();
	}

	public int updateUserGold(long userId, String flatId, String pf, long freeGold, long gold) {
		Map<String, Object> modify = new HashMap<>();
		modify.put("userId", userId);
		modify.put("gold", gold);
		modify.put("freeGold", freeGold);
		int update = 0;
		try {
			update = this.getSqlLoginClient().update("gold.updateUserGold", modify);
//			RegInfo inf = getFromCache(flatId, pf);
//			if (inf != null) {
//				inf.setCards(inf.getCards() + gold);
//				inf.setFreeCards(inf.getFreeCards() + freeGold);
//				setToCache(inf);
//			}
		} catch (SQLException e) {
			LogUtil.dbLog.error("#PlayerDao.updateUserCards:", e);
		}
		return update;
	}

    public RegInfo selectUserByUnionId(String unionId) {
		RegInfo regInfo = null;
		try {
			regInfo = (RegInfo) getSqlLoginClient().queryForObject("selectUserByUnionId", unionId);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#UserDao.selectUserByUnionId:" + unionId, e);
		}
		return regInfo;
    }

	/**
	 * 根据登出时间间隔解绑任务
	 * @param period
	 */
	public void removeBindByPeriod(int period) {
		try {
			int res =  getSqlLoginClient().update("removeBindByPeriod", period);
			LogUtil.msg("#UserDao.removeBindByPeriod-->res:"+res);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#UserDao.removeBindByPeriod:" + period, e);
		}
    }

	/**
	 * 根据最后游戏时间获得需要自动解绑的玩家
	 */
	public List<Map> getNeedRBList(int period) {
		List<Map> list = new ArrayList<>();
		try {
			list = (List<Map>) getSqlLoginClient().queryForList("getNeedRBMap", period);
			LogUtil.msg("getNeedRBList-->size:"+list.size());
		} catch (SQLException e) {
			LogUtil.dbLog.error("#UserDao.getNeedRBMap:" + period, e);
		}
		return list;
	}

	/**
	 * 根据id集合解除玩家绑定的邀请码
	 */
	public void removeBindByIdList(List<Long> idList) {
		try {
			String idListStr = "("+ StringUtil.implode(idList)+")";
			int res =  getSqlLoginClient().update("removeBindByIdList", idListStr);
			LogUtil.msg("#UserDao.removeBindByIdList-->res:"+res);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#UserDao.removeBindByIdList-->idList:" + idList.size()+",time:"+ TimeUtil.now(), e);
		}
	}

	/**
	 * 根据集合添加解绑日志
	 */
	public void addRBRecord(List<Map> list) {
		// 添加解绑记录
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("createTime", TimeUtil.now());
		paramMap.put("bindType", 4);
		for (int i = 0; i < list.size(); i++) {
			Map map = list.get(i);
			paramMap.put("agencyId", map.get("payBindId"));
			paramMap.put("createUserId", map.get("userId"));
			paramMap.put("userId", map.get("userId"));
			try {
				getSqlLoginClient().insert("addRBRecord", paramMap);
			} catch (SQLException e) {
				LogUtil.dbLog.error("#UserDao.addRBRecord err-->userId:" + map.get("userId")+",time:"+ TimeUtil.now(), e);
			}
		}
	}

	/**
	 * 根据id解除玩家绑定的邀请码
	 */
	public int removeBindById(Long userId) {
		int res = 0;
		try {
			res =  getSqlLoginClient().update("removeBindById", userId);
			LogUtil.msg("#UserDao.removeBindById-->res:"+res);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#UserDao.removeBindById-->id:" + userId + ",time:"+ TimeUtil.now(), e);
		}
		return res;
    }

	/**
	 * 添加一个玩家的解绑日志
	 */
	public void addRBRecordByPlayer(Player player) {
		// 添加解绑记录
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("createTime", TimeUtil.now());
		paramMap.put("bindType", 4);
		paramMap.put("agencyId", player.getPayBindId());
		paramMap.put("createUserId", player.getUserId());
		paramMap.put("userId", player.getUserId());
		try {
			getSqlLoginClient().insert("addRBRecord", paramMap);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#UserDao.addRBRecord err-->userId:" + player.getUserId()+",time:"+ TimeUtil.now(), e);
		}
	}


	/**
	 * 获取玩家指定时间内  通过他发送的链接的玩家下载量
	 * @param startTime
	 * @param endTime
	 * @param userId
	 * @return
	 */
	public List<Map> getDownloadData(Date startTime, Date endTime, long userId) {
		List<Map> list = new ArrayList<>();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("startTime", startTime);
		paramMap.put("endTime", endTime);
		paramMap.put("userId", userId);
		try {
			list = (List<Map>) getSqlLoginClient().queryForList("getDownloadData", paramMap);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#UserDao.getDownloadData err-->userId:" + userId+",time:"+ TimeUtil.now(), e);
		}
		return list;
	}

	/**
	 * 获取幸运转盘抽奖次数
	 * @param userId
	 * @return
	 */
	public int getUserdLotteryNum(long userId) {
        int usedLotteryNum = 0;
		try {
			usedLotteryNum = (Integer) this.getSqlLoginClient().queryForObject("lottery.usedLotteryNum", userId);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#UserDao.getUserdLotteryNum err-->userId:" + userId+",time:"+ TimeUtil.now(), e);
			e.printStackTrace();
		}
        return usedLotteryNum;
	}

	/**
	 * 获取当前幸运转盘奖励奖池数
	 * @return
	 */
	public int getPrizeSum() {
        int number = 0;
        try {
            number = (Integer) this.getSqlLoginClient().queryForObject("lottery.prizeSum");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return number;
    }

	/**
	 * 获取砸金蛋奖池数
	 * @return
	 */
	public int getSmashEggPrizeSum() {
		int number = 0;
        try {
            number = (Integer) this.getSqlLoginClient().queryForObject("lottery.smashEggPrizeSum");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return number;
	}

	public int getFirstPrizeSum() {
        int number = 0;
        try {
            number = (Integer) this.getSqlLoginClient().queryForObject("lottery.fistPrizeSum");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return number;
    }

    public int getSecondPrizeSum() {
        int number = 0;
        try {
            number = (Integer) this.getSqlLoginClient().queryForObject("lottery.secondPrizeSum");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return number;
    }

    /**
     * 获取某个奖项已获得次数
     * @param prizeIndex
     * @return
     */
    public int getPrizeSum(int prizeIndex) {
    	 int number = 0;
         try {
             number = (Integer) this.getSqlLoginClient().queryForObject("lottery.getPrizeSum", prizeIndex);
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return number;
    }

    public int addPrize(long userId, String prize, int prizeIndex) {
    	Map<String, Object> lotteryMap = new HashMap<>();
		lotteryMap.put("prize", prize);
		lotteryMap.put("prizeIndex", prizeIndex);
		lotteryMap.put("userId", userId);
        int res1 = 0;
		try {
			res1 = this.getSqlLoginClient().update("lottery.addPrize", lotteryMap);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#UserDao.addPrize err-->userId:" + userId+",time:"+ TimeUtil.now(), e);
			e.printStackTrace();
		}
        return res1;
    }
    /**
	 * 获取玩家指定时间内邀请成功的玩家信息
	 * @param startTime
	 * @param endTime
	 * @param userId
	 * @param maxNum 最大获取数
	 * @param playNum 至少要要玩的局数
	 * @return
	 */
	public List<Map> getInviteActivityUser(Date startTime, Date endTime, long userId, int maxNum, int playNum){
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("startTime", startTime);
		paramMap.put("endTime", endTime);
		paramMap.put("userId", userId);
		paramMap.put("maxNum", maxNum);
		paramMap.put("playNum", playNum);
		List<Map> result = new ArrayList<>();
		try {
			result = (List<Map>) getSqlLoginClient().queryForList("getInviteActivityUser", paramMap);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#UserDao.getInviteActivityUser err-->userId:" + userId+",time:"+ TimeUtil.now(), e);
		}
		return result;
	}
	public List<Map> getInviteUserActivity(Date startTime, Date endTime, long userId, int pageNo,int pageSize){
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("startTime", startTime);
		paramMap.put("endTime", endTime);
		paramMap.put("userId", userId);
		paramMap.put("startNo",(pageNo-1)*pageSize);
		paramMap.put("pageSize",pageSize);
		List<Map> result = new ArrayList<>();
		try {
			result = (List<Map>) getSqlLoginClient().queryForList("getInviteUserActivity", paramMap);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#UserDao.getInviteUserActivity err-->userId:" + userId+",time:"+ TimeUtil.now(), e);
		}
		return result;
	}

	public Map<String,Object> loadUserBase(String userId) throws SQLException {
		Map<String,Object> user;
		try {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("userId", userId);
			user = (Map<String,Object>) this.getSqlLoginClient().queryForObject("selectUserBaseMsg", param);
		} catch (SQLException e) {
			throw e;
		}
		return user;
	}

	/**
	 * 增加用户
	 *
	 * @param userInfo
	 * @throws SQLException
	 */
	public long addUser(RegInfo userInfo) throws SQLException {
		Long userId = (Long)this.getSqlLoginClient().insert("addUser", userInfo);
		if (userId!=null&&userId.longValue()==userInfo.getUserId()){
			setToCache(userInfo);
			return userId;
		}
		String msg="insert userInfo fail:user="+JSON.toJSONString(userInfo) +",userId="+userId;
		LogUtil.e(msg);
		throw new SQLException(msg);
	}

    /**
     * 查找用户
     *
     * @param username
     * @return
     * @throws SQLException
     */
    public RegInfo getUser(String username, String pf) throws SQLException {
        RegInfo user;
        try {

            Map<String, Object> param = new HashMap<>();
            if (pf.equals(LoginUtil.pf_phoneNum)) {
                user = (RegInfo) this.getSqlLoginClient().queryForObject("getUserByPhoneNum", LoginUtil.encryptPhoneNumAES(username));
                if (null == user) {
                    user = (RegInfo) this.getSqlLoginClient().queryForObject("getUserByPhoneNum", username);
                }
            } else if (pf.equals(LoginUtil.pf_self)) {
                param.put("username", username);
                user = (RegInfo) this.getSqlLoginClient().queryForObject("getUserForSelfLogin", param);
            } else {
                param.put("username", username);
                if ("true".equals(ResourcesConfigsUtil.loadServerPropertyValue("weixin_openid"))) {
                    param.put("pf", pf);
                    user = (RegInfo) this.getSqlLoginClient().queryForObject("getUser0", param);
                } else {
                    param.put("pf", pf.startsWith("weixin") ? "weixin%" : pf);
                    user = (RegInfo) this.getSqlLoginClient().queryForObject("getUser", param);
                }
            }
        } catch (SQLException e) {
            throw e;
        }
        return user;
    }

	/**
	 * 查找用户
	 * @param unionId
	 * @param unionPf
	 * @param thirdId
	 * @param thirdPf
	 * @return
	 * @throws SQLException
	 */
	public RegInfo getUser(String unionId,String unionPf,String thirdId,String thirdPf) throws SQLException {
		RegInfo user;
		try {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("unionId", unionId);
			param.put("unionPf", unionPf.endsWith("%")?unionPf:(unionPf+"%"));
			param.put("flatId", thirdId);
			param.put("pf", thirdPf);
			user = (RegInfo) this.getSqlLoginClient().queryForObject("getUserUnion", param);
		} catch (SQLException e) {
			throw e;
		}
		return user;
	}

	/**
	 * 查找用户
	 *
	 * @param unionid
	 * @return
	 * @throws SQLException
	 */
	public RegInfo getUserByUnionid(String unionid) throws SQLException {
		RegInfo user = null;
		try {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("identity", unionid);
			user = (RegInfo) this.getSqlLoginClient().queryForObject("getUserByUnionid", param);
		} catch (SQLException e) {
			throw e;
		}
		return user;
	}

	/**
	 * 查找用户
	 *
	 * @param userId
	 * @return
	 * @throws SQLException
	 */
	public RegInfo getUser(long userId) throws SQLException {
		RegInfo user = null;
		try {
			user = (RegInfo) this.getSqlLoginClient().queryForObject("getUserById", userId);
			if (user != null)
				setToCache(user);
		} catch (SQLException e) {
			throw e;
		}
		return user;
	}

	/**
	 * 查找用户所在的服
	 *
	 * @param userId
	 * @return
	 * @throws SQLException
	 */
	public int getUserServerId(String userId) throws SQLException {
		Number serverId;
		try {
			serverId = (Number) this.getSqlLoginClient().queryForObject("getUserServerId", userId);
			return serverId==null?0:serverId.intValue();
		} catch (SQLException e) {
			throw e;
		}
	}

	public long getMaxId() throws SQLException {
		Object o = this.getSqlLoginClient().queryForObject("getMaxId");
		if (o != null) {
			return Long.parseLong(o.toString());
		}
		return 0;
	}

	public long getMinId() throws SQLException {
		Object o = this.getSqlLoginClient().queryForObject("getMinId");
		if (o != null) {
			return Long.parseLong(o.toString());
		}
		return 0;
	}

    /**
     * 提交用户更新
     *
     * @param userId
     * @param modify
     * @return
     * @throws SQLException
     */
    public int updateUser(String userId, Map<String, Object> modify) throws SQLException {
        modify.put("userId", userId);
        return this.getSqlLoginClient().update("updateUser", modify);
    }

	/**
	 * 提交用户更新
	 *
	 * @param user
	 * @param modify
	 * @return
	 * @throws SQLException
	 */
	public int updateUser(RegInfo user, Map<String, Object> modify) throws SQLException {
		modify.put("userId", String.valueOf(user.getUserId()));
		int update = this.getSqlLoginClient().update("updateUser", modify);
		ClassTool.setValue(user, modify);
		return update;
	}

	public int removeBindInfo(RegInfo user) {
		int res = 0;
		// 清除玩家的绑定
		Map<String, Object> modifyMap = new HashMap<>();
		try {
			modifyMap.put("payBindId", 0);
			res = updateUser(user, modifyMap);
			LogUtil.i("removeBindInfo-->userId:"+user.getUserId()+",payBindId:"+user.getPayBindId()+",res:"+res);
		} catch (SQLException e) {
			LogUtil.e("removeBindInfo err-->"+e);
		}
		return res;
	}

	public void insertRBInfo(Map map) {
		try {
			this.getSqlLoginClient().insert("insertRemoveBind", map);
		} catch (SQLException e) {
			LogUtil.e("insertRemoveBindInfo err-->"+e);
		}
	}

	/**
	 * 保存或更新用户资源信息
	 *
	 * @param userExtend
	 */
	public int saveOrUpdateUserExtend(UserExtend userExtend) {
		try {
			return this.getSqlLoginClient().update("save_or_update_user_extend",userExtend);
		} catch (SQLException e) {
			LogUtil.e("save_or_update_user_extend err-->"+e.getMessage(),e);
		}
		return -1;
	}

	public int updateUserExtendIntValue(String userId,UserResourceType type, int changedVal){
		try {
			HashMap<String,Object> map = new HashMap<>(8);
			map.put("msgKey", type.name());
			map.put("msgType", type.getType());
			map.put("msgValue", changedVal);
			map.put("userId", userId);
			return this.getSqlLoginClient().update("update_user_extend_int_val",map);
		} catch (SQLException e) {
			LogUtil.e("update_user_extend_int_val err-->"+e.getMessage(),e);
		}
		return -1;
	}

	/**
	 * 芒果跑得快重置积分为0
	 */
	public int resetJiFen() throws SQLException {
		try {
			return this.getSqlLoginClient().update("resetJiFen");
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * 查询用户资源信息
	 *
	 * @param userId
	 * @param msgType
	 * @throws SQLException
	 */
	public int queryUserExtendValue(long userId, int msgType) throws SQLException {
		return CommonUtil.object2Int(queryUserExtendValueString(userId,msgType));
	}

	/**
	 * 查询用户资源信息
	 *
	 * @param userId
	 * @param msgType
	 * @throws SQLException
	 */
	public String queryUserExtendValueString(long userId, int msgType) throws SQLException {
		try {
			HashMap<String,Object> map=new HashMap<>();
			map.put("userId",String.valueOf(userId));
			map.put("msgType",msgType);
			Object obj = this.getSqlLoginClient().queryForObject("select_user_extend_value",map);
			return obj==null?null:obj.toString();
		} catch (SQLException e) {
			throw e;
		}
	}

	public boolean isFirstPay(long userId,int minItem,int maxItem) throws SQLException {
		Map<String, Object> param = new HashMap<>();
		param.put("userId", String.valueOf(userId));
		param.put("minItem", minItem);
		param.put("maxItem", maxItem);

		HashMap<String,Object> result=(HashMap<String,Object>)this.getSqlLoginClient().queryForObject("isFirstPay", param);
		boolean ret=result==null||result.size()==0;

		LogUtil.msgLog.info("check isFirstPay:result={},params={}",ret,param);

		return ret;
	}

	public float getUserWinRate(long userId, List<Integer> wanfas) throws SQLException {
		Map<String, Object> param = new HashMap<>();
		param.put("userId", String.valueOf(userId));
		String wanfaStrs = "("+ StringUtil.implode(wanfas)+")";
		param.put("gameTypes", wanfaStrs);
		Float rate = (Float) this.getSqlLoginClient().queryForObject("getUserWinRate", param);
		if(rate == null) {
		    return 0.0f;
        } else
            return rate;
	}

    public String selectNameByUserId(long userId) {
        String result = null;
        try {
            result =  (String)getSqlLoginClient().queryForObject("selectNameByUserId", userId);
        } catch (SQLException e) {
            LogUtil.dbLog.error("#UserDao.selectUserByUserId:" + userId, e);
        }
        return result;
    }


    public int saveOffLine(int serverId, long userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("serverId", serverId);
        map.put("userId", userId);
        try {
            return getSqlLoginClient().update("saveOffLine", map);
        } catch (SQLException e) {
            LogUtil.dbLog.error("#UserDao.saveOffLine" + JacksonUtil.writeValueAsString(map), e);
        }
        return 0;
    }

    /**
     * 新的金币系统
     * 更新玩家金币
     *
     * @return
     * @throws SQLException
     */
    public int changeUserCoin(long userId, long coin, long freeCoin) {
        Map<String, Object> modify = new HashMap<>();
        modify.put("userId", userId);
        modify.put("coin", coin);
        modify.put("freeCoin", freeCoin);
        int update = 0;
        try {
            update = this.getSqlLoginClient().update("changeUserCoin", modify);
        } catch (SQLException e) {
            LogUtil.dbLog.error("UserDao|changeUserCoin|error|" + userId + "|" + coin + "|" + freeCoin, e);
        }
        return update;
    }

    /**
     * 获取玩家密码错误限制
     * @param userId
     * @return
     */
    public String loadPwErrorMsg(long userId) {
        try {
            return (String) this.getSqlLoginClient().queryForObject("loadPwErrorMsg", userId);
        } catch (SQLException e) {
            LogUtil.dbLog.error("#PlayerDao.loadUserCards:" + e.getMessage(), e);
        }
        return null;
    }

    public int updatePwErrorMsg(long userId, String pwErrorMsg) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("pwErrorMsg", pwErrorMsg);
        return this.getSqlLoginClient().update("updatePwErrorMsg", map);
    }

    public RegInfo loadUserByPhoneNum(String phoneNum) throws SQLException {
        RegInfo user;
        if (StringUtils.isBlank(phoneNum)) {
            return null;
        }
        try {
            StringJoiner sj = new StringJoiner(",");
            String phoneNumAES1 = LoginUtil.encryptPhoneNumAES(phoneNum);
            String phoneNumAES2 = LoginUtil.encryptPhoneNumAES(phoneNumAES1);
            sj.add("'" + phoneNum + "'");
            sj.add("'" + phoneNumAES1 + "'");
            sj.add("'" + phoneNumAES2 + "'");
            user = (RegInfo) this.getSqlLoginClient().queryForObject("load_user_by_phoneNum", sj.toString());
            if (user != null && phoneNumAES2.equals(user.getPhoneNum())) {
                // 手机号被两次加密的bug
                Map<String, Object> modify = new HashMap<>();
                modify.put("phoneNum", phoneNumAES1);
                UserDao.getInstance().updateUser(String.valueOf(user.getUserId()), modify);
                LogUtil.msgLog.info("updatePhoneNum|" + user.getUserId() + "|" + phoneNum);
            }
        } catch (SQLException e) {
            throw e;
        }
        return user;
    }

    public RegInfo loadUserByFlatId(String flatId) throws SQLException {
        RegInfo user;
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("flatId", flatId);
            user = (RegInfo) this.getSqlLoginClient().queryForObject("loadUserByFlatId", param);
        } catch (SQLException e) {
            throw e;
        }
        return user;
    }

	public RegInfo loadUserByAccName(String accName) throws SQLException {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("accName", accName);

		RegInfo user =  (RegInfo) this.getSqlLoginClient().queryForObject("getUserByAccName", param);
		if (user != null)
			setToCache(user);

		return user;
	}

    public RegInfo loadUserByWeiXinUnionId(String unionId) throws SQLException {
        RegInfo user;
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("unionId", unionId);
            user = (RegInfo) this.getSqlLoginClient().queryForObject("loadUserByWeiXinUnionId", param);
        } catch (SQLException e) {
            throw e;
        }
        return user;
    }

	public long selectGoldenBeans(long userId) {
		try {
			return (long) this.getSqlLoginClient().queryForObject("select_goldenBeans", userId);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#PlayerDao.select_goldenBeans:" + e.getMessage(), e);
		}
		return 0;
	}

	public long updateGoldenBeans(long userId,int num) {
		try {
			HashMap<String,Object> map = new HashMap<>();
			map.put("userId",userId);
			map.put("num",num);
			return (long) this.getSqlLoginClient().update("update_goldenBeans", map);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#PlayerDao.update_goldenBeans:" + e.getMessage(), e);
		}
		return 0;
	}

	public int selectUserIsSalesman(long userId) {
		try {
			return (int) this.getSqlLoginClient().queryForObject("selectUserIsSalesman", userId);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#PlayerDao.selectUserIsSalesman:" + e.getMessage(), e);
		}
		return 0;
	}

}
