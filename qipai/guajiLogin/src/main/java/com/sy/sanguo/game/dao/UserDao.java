package com.sy.sanguo.game.dao;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.Constants;
import com.sy.sanguo.common.util.user.GameUtil;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.UserCardRecordInfo;
import com.sy.sanguo.game.bean.UserExtend;
import com.sy.sanguo.game.bean.UserGoldBeanRecord;
import com.sy.sanguo.game.bean.enums.CardSourceType;
import com.sy.sanguo.game.bean.enums.SourceType;
import com.sy.sanguo.game.pdkuai.db.bean.UserMessage;
import com.sy.sanguo.game.pdkuai.db.dao.BaseDao;
import com.sy.sanguo.game.pdkuai.db.dao.UserMessageDao;
import com.sy.sanguo.game.pdkuai.staticdata.StaticDataManager;
import com.sy.sanguo.game.pdkuai.staticdata.bean.ActivityBean;
import com.sy.sanguo.game.pdkuai.util.LogUtil;

public class UserDao extends BaseDao {
	private static UserDao _inst = new UserDao();

	public static UserDao getInstance() {
		return _inst;
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
//            user = getFromCache(username, pf);
			// if (user != null) {
			// if (user.getIsOnLine() == 0) {
			// return user;
			// }
			// }

			Map<String, Object> param = new HashMap<String, Object>();
			param.put("username", username);

			if ("true".equals(PropertiesCacheUtil.getValue("weixin_openid",Constants.GAME_FILE))){
				param.put("pf", pf);
				user = (RegInfo) this.getSql().queryForObject("user.getUser0", param);
			}else{
				param.put("pf", pf.startsWith("weixin")?"weixin%":pf);
				user = (RegInfo) this.getSql().queryForObject("user.getUser", param);
			}
//            if (user != null)
//                setToCache(user);
		} catch (SQLException e) {
			throw e;
		}
		return user;
	}

	/**
	 * 保存用户扩展信息
	 *
	 * @param userExtend
	 * @throws SQLException
	 */
	public int saveUserExtend(UserExtend userExtend) throws SQLException {
		try {
			return getSql().update("user.save_or_update_user_extend",userExtend);
		} catch (SQLException e) {
			throw e;
		}
	}

	public int updateUserExtend(HashMap<String ,Object> map) throws SQLException {
		try {
			return getSql().update("user.update_user_extend",map);
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * 查询用户扩展信息
	 *
	 * @param userId
	 * @throws SQLException
	 */
	public List<HashMap<String,Object>> queryUserExtend(String userId) throws SQLException {
		try {
			HashMap<String,String> map=new HashMap<>();
			map.put("userId",userId);
			return getSql().queryForList("user.select_user_extend_all",map);
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * 查询用户扩展信息
	 *
	 * @param userId
	 * @param msgType
	 * @throws SQLException
	 */
	public HashMap<String,Object> queryUserExtend(String userId,int msgType) throws SQLException {
		try {
			HashMap<String,Object> map=new HashMap<>();
			map.put("userId",userId);
			map.put("msgType",msgType);
			return (HashMap<String,Object>)getSql().queryForObject("user.select_user_extend_single",map);
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * 查找用户
	 * 
	 * @param unionid
	 * @return
	 * @throws SQLException
	 */
	public RegInfo getUserByUnionid(String unionid) {
		RegInfo user = null;
		try {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("identity", unionid);
			user = (RegInfo) this.getSql().queryForObject("user.getUserByUnionid", param);
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("getUserByUnionid err:" + unionid, e);
		}
		return user;
	}

	public List<Map<String,Object>> load(String sqlMark,HashMap<String,String> params) throws SQLException {
		try {
			return this.getSql().queryForList(sqlMark,params);
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * 查找今日邀请、今日达标、总邀请、总达标，已发送红包金额、今日支取次数
	 *
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String,Object>> loadMyInviteeData(HashMap<String,String> params) throws SQLException {
		try {
			return this.getSql().queryForList("user.loadMyInviteeData",params);
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * 查找总邀请、总达标，已发送红包金额、今日支取次数
	 *
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String,Object>> loadMyTotalData(HashMap<String,String> params) throws SQLException {
		try {
			return this.getSql().queryForList("user.loadMyTotalData",params);
		} catch (SQLException e) {
			throw e;
		}
	}


	/**
	 * 添加红包记录
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public Object addHbExchangeRecord(HashMap<String,String> params) throws SQLException {
		return this.getSql().insert("user.addHbExchangeRecord",params);
	}

	/**
	 * 查找今日邀请(最近N人)
	 *
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String,Object>> loadMyTotayUsers(HashMap<String,String> params) throws SQLException {
		try {
			return this.getSql().queryForList("user.loadMyTotayUsers",params);
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * 获取支取次数
	 *
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public int loadMyTotayPayCount(HashMap<String,String> params) throws SQLException {
		try {
			return (Integer) this.getSql().queryForObject("user.loadMyTotayPayCount",params);
		} catch (SQLException e) {
			throw e;
		}
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
			user = (RegInfo) this.getSql().queryForObject("user.getUserById", userId);
			if (user != null)
				setToCache(user);
		} catch (SQLException e) {
			throw e;
		}
		return user;
	}

	/**
	 * 查找用户
	 *
	 * @param userIds
	 * @return
	 * @throws SQLException
	 */
	public List<RegInfo> getUserForList(List<Long> userIds, boolean notFilterOffline) throws SQLException {
		List<RegInfo> user = null;
		try {
			user = this.getSql().queryForList("user.getUserByIdForList",
					new HashMap<String, Object>() {
						{
							this.put("userIds", userIds.stream().map(v -> v.toString()).collect(Collectors.joining(",")));
							this.put("notFilterOffline", notFilterOffline ? 1 : 0);
						}
					});
		} catch (SQLException e) {
			throw e;
		}
		return user;
	}

	/**
	 * 查找用户
	 *
	 * @param userIds
	 * @return
	 * @throws SQLException
	 */
	public List<RegInfo> getUserForListByTable(List<Long> userIds, boolean notInTable) throws SQLException {
		List<RegInfo> user = null;
		try {
			user = this.getSql().queryForList("user.getUserForListByTable",
					new HashMap<String, Object>() {
						{
							this.put("userIds", userIds.stream().map(v -> v.toString()).collect(Collectors.joining(",")));
							this.put("notInTable", notInTable ? 1 : 0);
						}
					});
		} catch (SQLException e) {
			throw e;
		}
		return user;
	}

	/**
	 * 查找用户
	 *
	 * @param userIds
	 * @return
	 * @throws SQLException
	 */
	public Integer countOnlineByUserIds(List<Long> userIds) throws SQLException {
		try {
			return (Integer) this.getSql().queryForObject("user.countOnlineByUserIds",
					new HashMap<String, Object>() {
						{
							this.put("userIds", userIds.stream().map(v -> v.toString()).collect(Collectors.joining(",")));
						}
					});
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * 查找用户
	 *
	 * @param userIds
	 * @return
	 * @throws SQLException
	 */
	public List<Long> getOfflineByUserIds(List<Long> userIds) throws SQLException {
		try {
			return (List<Long>) this.getSql().queryForList("user.getOfflineByUserIds",
					new HashMap<String, Object>() {
						{
							this.put("userIds", userIds.stream().map(v -> v.toString()).collect(Collectors.joining(",")));
						}
					});
		} catch (SQLException e) {
			throw e;
		}
	}

	/****************** Cache *******************/

	private void setToCache(RegInfo userInfo) {
	}
	/**
	 * 是否首冲
	 * @param userId
	 * @param minItem
	 * @param maxItem
	 * @return
	 * @throws SQLException
	 */
	public boolean isFirstPay(long userId,int minItem,int maxItem) throws SQLException {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("userId", String.valueOf(userId));
		param.put("minItem", minItem);
		param.put("maxItem", maxItem);

		ActivityBean activityBean = StaticDataManager.getSingleActivityBaseInfo(111);

		boolean ret;
		if (activityBean != null){
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			param.put("startTime",sdf.format(activityBean.getStartDateTime()));
			param.put("endTime",sdf.format(activityBean.getEndDateTime()));
			Date date = new Date();
			if (date.after(activityBean.getStartDateTime())&&date.before(activityBean.getEndDateTime())){
				HashMap<String,Object> result=(HashMap<String,Object>)this.getSql().queryForObject("order.isFirstPay", param);
				ret = result==null||result.size()==0;
			}else{
				ret = false;
			}
		}else{
			HashMap<String,Object> result=(HashMap<String,Object>)this.getSql().queryForObject("order.isFirstPay", param);
			ret=result==null||result.size()==0;
		}

		LogUtil.i("check isFirstPay:result="+ret+",params="+param);

		return ret;
	}

	/**
	 * 提交用户更新
	 *
	 * @param modify
	 * @return
	 * @throws SQLException
	 */
	public int addUserCards(RegInfo user, long cards, long freeCards, long payBindId, Map<String, Object> modify, UserMessage info, CardSourceType sourceType) throws SQLException {
		if (user==null){
			return  0;
		}
		if (modify == null) {
			modify = new HashMap<String, Object>();
		}
		modify.put("userId", user.getUserId());
		modify.put("cards", cards);
		modify.put("freeCards", freeCards);
		if (payBindId != 0) {
			modify.put("payBindId", payBindId);
			user.setPayBindId((int) payBindId);
		}
		int update = this.getSql().update("user.addUserCards", modify);
		setToCache(user);
		UserMessageDao.getInstance().saveUserMessage(info);
		GameBackLogger.SYS_LOG.info("sendPay: userId"+"  " + user.getUserId() + "   enterServer:" + user.getEnterServer());
		if (user.getEnterServer() != 0) {
			String str = GameUtil.sendPay(user.getEnterServer(), user.getUserId(), (int) (cards), (int) freeCards, info, "1");
			GameBackLogger.SYS_LOG.info("sendPay: userId"+"  " + user.getUserId() + "   " + "sendPay cards"+cards+" freeCards="+freeCards +" currency=1" +",result="+str);
		}

		if(cards + freeCards != 0) {
			long curCard = user.getCards() + cards;
			curCard = (curCard < 0) ? 0 : curCard;
			long curFreeCard = user.getFreeCards() + freeCards;
			curFreeCard = (curFreeCard < 0) ? 0 : curFreeCard;
			UserCardRecordDao.getInstance().insert(new UserCardRecordInfo(user.getUserId(), curCard, curFreeCard, (int)cards, (int)freeCards, 0, sourceType));
		}
		return update;
	}

	public void insertRBInfo(Map map) {
		try {
			this.getSql().insert("user.insertRemoveBind", map);
		} catch (SQLException e) {
			LogUtil.e("insertRemoveBindInfo err-->"+e);
		}
	}

	public Integer getRemoveBindCount(Long userId) {
		try {
			Object o = getSql().queryForObject("user.selectRemoveBindCount", userId);
			if (o != null) {
				return (Integer) o;
			} else {
				return 0;
			}
		} catch (SQLException e) {
			LogUtil.e("getRemoveBindCount err-->"+e);
		}
		return 0;
	}

	public long getIdentityUserId(String identity) {
		try {
			Object o = getSql().queryForObject("user.selectIdentityUserId", identity);
			if (o != null) {
				return (long) o;
			} else {
				return 0;
			}
		} catch (SQLException e) {
			LogUtil.e("getIdentityUserId err-->"+e);
		}
		return 0;
	}

    /**
     * 变更钻石
     * 扣钻使用 -cards
     *
     * @param userId
     * @param freeCards
     * @param cards
     * @param sourceType
     * @return
     */
    public int addUserCards(long userId, long freeCards, long cards, SourceType sourceType) {
        return addUserCards(userId, freeCards, cards, sourceType.type(), 1);
    }

    public int addUserCards(long userId, long freeCards, long cards, int sourceType) {
        return addUserCards(userId, freeCards, cards, sourceType, 1);
    }

    private int addUserCards(long userId, long freeCards, long cards, int sourceType, int tryCount) {
        long[] longs = loadUserCard(userId);
        long selfCards = longs[0];
        long selfFreeCards = longs[1];
        long oldCards = selfCards;
        long oldFreeCards = selfFreeCards;

        long minusFreeCards = 0; // 减少的免费钻 freeCards
        long minusCards = 0; // 减少的充值钻 cards
        if (cards < 0) { //扣钻，只允许会用该值
            // temp等于绑定房卡 + cards
            long temp = selfFreeCards + cards;
            if (temp >= 0) {
                // 房卡足够
                selfFreeCards = temp;

                minusCards = 0;
                minusFreeCards = -cards;
            } else {
                // 房卡不足，先用完绑定房卡，再用普通房卡
                selfFreeCards = 0;

                minusCards = -temp;
                minusFreeCards = (-cards) - minusCards;
            }
            minusFreeCards += -freeCards;

            selfCards -= minusCards;
            selfFreeCards += freeCards;
        } else {
            minusFreeCards = -freeCards;
            minusCards = -cards;

            selfCards += cards;
            selfFreeCards += freeCards;
        }
        if (minusCards != 0 || minusFreeCards != 0) {
            if (changeUserCard(userId, oldCards, oldFreeCards, -minusCards, -minusFreeCards) <= 0) {
                if (tryCount++ > 20) {
                    LogUtil.i("addUserCards|fail|" + userId + "|" + freeCards + "|" + cards + "|" + sourceType + "|" + tryCount);
                    return 0;
                }
                return addUserCards(userId, freeCards, cards, sourceType, tryCount);
            } else {
                LogUtil.i("addUserCards|succ|" + userId + "|" + freeCards + "|" + cards + "|" + sourceType + "|" + tryCount);

                UserCardRecordInfo record = new UserCardRecordInfo(userId, selfFreeCards, selfCards, (int) -minusFreeCards, (int) -minusCards, sourceType);
                UserCardRecordDao.getInstance().insert(record);
                try {
                    RegInfo regInfo = UserDao.getInstance().getUser(userId);
                    if (regInfo != null && regInfo.getEnterServer() > 0 && regInfo.getIsOnLine() == 1) {
                        GameUtil.notifyChangCards(regInfo.getEnterServer(), userId, cards, freeCards, false);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        }
        return 1;
    }

	public int addUserCards1(long userId, long freeCards, long cards, int sourceType) {
		return addUserCards1(userId, freeCards, cards, sourceType, 1);
	}

	private int addUserCards1(long userId, long freeCards, long cards, int sourceType, int tryCount) {
		long[] longs = loadUserCard(userId);
		long selfCards = longs[0];
		long selfFreeCards = longs[1];
		long oldCards = selfCards;
		long oldFreeCards = selfFreeCards;

		long minusFreeCards = 0; // 减少的免费钻 freeCards
		long minusCards = 0; // 减少的充值钻 cards
		if (cards < 0) {
			long temp = selfCards + cards;
			if (temp >= 0) {
				// 房卡足够
				minusCards = -cards;
				selfCards = temp;
			} else {
				minusCards = selfCards;
				selfCards = 0;
			}
		} else {
			selfCards += cards;
			minusCards = -cards;
		}
		if (freeCards < 0) {
			long temp = selfFreeCards + freeCards;
			if (temp >= 0) {
				// 房卡足够
				minusFreeCards = -freeCards;
				selfFreeCards = temp;
			} else {
				minusFreeCards = selfFreeCards;
				selfFreeCards = 0;
			}
		} else {
			selfFreeCards += freeCards;
			minusFreeCards = -freeCards;
		}

		if (minusCards != 0 || minusFreeCards != 0) {
			if (changeUserCard(userId, oldCards, oldFreeCards, -minusCards, -minusFreeCards) <= 0) {
				if (tryCount++ > 20) {
					LogUtil.i("addUserCards1|fail|" + userId + "|" + freeCards + "|" + cards + "|" + sourceType + "|" + tryCount);
					return 0;
				}
				return addUserCards(userId, freeCards, cards, sourceType, tryCount);
			} else {
				LogUtil.i("addUserCards1|succ|" + userId + "|" + freeCards + "|" + cards + "|" + sourceType + "|" + tryCount);

				UserCardRecordInfo record = new UserCardRecordInfo(userId, selfFreeCards, selfCards, (int) -minusFreeCards, (int) -minusCards, sourceType);
				UserCardRecordDao.getInstance().insert(record);
				try {
					RegInfo regInfo = UserDao.getInstance().getUser(userId);
					if (regInfo != null && regInfo.getEnterServer() > 0 && regInfo.getIsOnLine() == 1) {
						GameUtil.notifyChangCards(regInfo.getEnterServer(), userId, cards, freeCards, false);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

			}
		}
		return 1;
	}

    /**
     * 获取玩家的房卡 0cards，1freeCards
     *
     * @param userId
     * @return
     */
    public long[] loadUserCard(long userId) {
        try {
            HashMap<String, Object> map = (HashMap<String, Object>) this.getSql().queryForObject("user.load_user_card", userId);
            if (map == null || map.size() == 0) {
                return new long[]{0, 0};
            } else {
                Object cards = map.getOrDefault("cards", 0);
                Object freeCards = map.getOrDefault("freeCards", 0);
                return new long[]{(cards instanceof Number) ? ((Number) cards).longValue() : 0, (freeCards instanceof Number) ? ((Number) freeCards).longValue() : 0};
            }
        } catch (Exception e) {
            return new long[]{0, 0};
        }
    }

    /**
     * 提交用户更新
     *
     * @return
     * @throws SQLException
     */
    public int changeUserCard(long userId, long oldCards, long oldFreeCards, long cards, long freeCards) {
        Map<String, Object> modify = new HashMap<>();
        modify.put("userId", userId);
        modify.put("oldCards", oldCards);
        modify.put("oldFreeCards", oldFreeCards);
        modify.put("cards", cards);
        modify.put("freeCards", freeCards);
        int update = 0;
        try {
            update = this.getSql().update("user.change_user_card", modify);
        } catch (SQLException e) {
            LogUtil.e("#PlayerDao.changeUserCard:", e);
        }
        return update;
    }


    public int addUserGoldenBeans(long userId, long goldenBeans, SourceType sourceType) {
        return addUserGoldenBeans(userId, goldenBeans, sourceType.type(), 1);
    }

    public int addUserGoldenBeans(long userId, long goldenBeans, int sourceType) {
        return addUserGoldenBeans(userId, goldenBeans, sourceType, 1);
    }

    private int addUserGoldenBeans(long userId, long goldenBeans, int sourceType, int tryCount) {
        long oldGoldenBeans = loadUserGoldenBeans(userId);
        if (changeUserGoldenBeans(userId, oldGoldenBeans, goldenBeans) <= 0) {
            if (tryCount++ > 20) {
                LogUtil.i("addUserGoldenBean|fail|" + userId + "|" + goldenBeans + "|" + sourceType + "|" + tryCount);
                return 0;
            }
            return addUserGoldenBeans(userId, goldenBeans, sourceType, tryCount);
        } else {
            UserGoldBeanRecord record = new UserGoldBeanRecord(userId, (oldGoldenBeans + goldenBeans), goldenBeans, sourceType);
            UserGoldBeanRecordDao.getInstance().saveUserGoldBeanRecord(record);
            LogUtil.i("addUserGoldenBeans|succ|" + userId + "|" + goldenBeans + "|" + sourceType + "|" + tryCount);
        }
        return 0;
    }

    public long loadUserGoldenBeans(long userId) {
        try {
            Long goldenBeans = (Long) this.getSql().queryForObject("user.load_user_goldenBeans", userId);
            if (goldenBeans == null) {
                return 0;
            } else {
                return goldenBeans;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 提交用户更新
     *
     * @return
     * @throws SQLException
     */
    public int changeUserGoldenBeans(long userId, long oldGoldenBeans, long goldenBeans) {
        Map<String, Object> modify = new HashMap<>();
        modify.put("userId", userId);
        modify.put("oldGoldenBeans", oldGoldenBeans);
        modify.put("goldenBeans", goldenBeans);
        int update = 0;
        try {
            update = this.getSql().update("user.change_user_goldenBeans", modify);
        } catch (SQLException e) {
            LogUtil.e("#PlayerDao.changeUserGoldBean:", e);
        }
        return update;
    }

    public Integer insertRobot(HashMap<String, Object> map) {
        try {
            return (Integer) this.getSql().insert("user.insert_robot", map);
        } catch (SQLException e) {
            LogUtil.e("insertRemoveBindInfo err-->" + e);
        }
        return 0;
    }

    public List<Map<String, Object>> loadRobotSysProp(int type, int limitCount) {
        try {
            HashMap<String, Object> params = new HashMap<>();
            params.put("type", type);
            params.put("limitCount", limitCount);
            return this.getSql().queryForList("user.load_robot_sys_prop", params);
        } catch (SQLException e) {
            LogUtil.e("loadRobotSysProp err-->" + e);
        }
        return null;
    }

    public int useRobotSysProp(String keyIds) throws SQLException {
        try {
            return getSql().update("user.use_robot_sys_prop", keyIds);
        } catch (SQLException e) {
            LogUtil.e("updateUserExtend err-->" + e);
        }
        return 0;
    }

}
