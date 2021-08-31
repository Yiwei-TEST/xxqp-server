package com.sy.sanguo.game.dao;

import com.sy.sanguo.common.util.user.GameUtil;
import com.sy.sanguo.game.bean.GoldDataStatistics;
import com.sy.sanguo.game.bean.GoldUserInfo;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.UserGoldRecord;
import com.sy.sanguo.game.bean.enums.SourceType;
import com.sy.sanguo.game.bean.gold.GoldPlayer;
import com.sy.sanguo.game.pdkuai.db.dao.BaseDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GoldDao extends BaseDao {

    private static final Logger LOGGER = LoggerFactory.getLogger("sys");

    private static GoldDao _inst = new GoldDao();

    public static GoldDao getInstance() {
        return _inst;
    }

    /**
     * 创建一个金币玩家身份
     */
    public void createGoldUser(GoldPlayer goldPlayer) throws Exception{
        this.getSql().insert("gold.insertGoldUser", goldPlayer);
    }


    public GoldUserInfo selectGoldUserByUserId(long userId) throws Exception {
        Object o = this.getSql().queryForObject("gold.selectGoldUserByUserId", userId);
        if (o != null) {
            return (GoldUserInfo) o;
        }
        return null;
    }

    public void insertGoldUser(GoldUserInfo userInfo) throws Exception {
        this.getSql().insert("gold.insertGoldUser", userInfo);
    }

    public int updateGoldUser(Map<String, Object> map) throws Exception {
        return this.getSql().update("gold.updateGoldUser", map);
    }

    /**
     * 获取玩家的房卡 0cards，1freeCards
     *
     * @param userId
     * @return
     */
    public long[] loadUserGold(long userId) {
        try {
            HashMap<String, Object> map = (HashMap<String, Object>) this.getSql().queryForObject("gold.load_user_gold", userId);
            if (map == null || map.size() == 0) {
                return new long[]{0, 0};
            } else {
                Object gold = map.getOrDefault("gold", 0);
                Object freeGold = map.getOrDefault("freeGold", 0);
                return new long[]{(gold instanceof Number) ? ((Number) gold).longValue() : 0, (freeGold instanceof Number) ? ((Number) freeGold).longValue() : 0};
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
    public int changeUserGold(long userId, long oldGold, long oldFreeGold, long gold, long freeGold) {
        Map<String, Object> modify = new HashMap<>();
        modify.put("userId", userId);
        modify.put("oldGold", oldGold);
        modify.put("oldFreeGold", oldFreeGold);
        modify.put("gold", gold);
        modify.put("freeGold", freeGold);
        int update = 0;
        try {
            update = this.getSql().update("gold.change_user_gold", modify);
        } catch (SQLException e) {
            LOGGER.error("#PlayerDao.changeUserGold:", e);
        }
        return update;
    }


    /**
     * 变更金币
     * 扣除金币使用 -gold
     *
     * @param userId
     * @param freeGold
     * @param gold
     * @param sourceType
     * @return
     */
    public int addUserGold(long userId, long freeGold, long gold, SourceType sourceType) {
        return addUserGold(userId, freeGold, gold, sourceType.type(), 1);
    }

    public int addUserGold(long userId, long freeGold, long gold, int sourceType) {
        return addUserGold(userId, freeGold, gold, sourceType, 1);
    }

    private int addUserGold(long userId, long freeGold, long gold, int sourceType, int tryCount) {
        long[] longs = loadUserGold(userId);
        long selfGold = longs[0];
        long selfFreeGold = longs[1];
        long oldGold = selfGold;
        long oldFreeGold = selfFreeGold;

        long minusFreeGold = 0; // 减少的免费钻 freeCards
        long minusGold = 0; // 减少的充值钻 cards
        if (gold < 0) { //扣钻，只允许会用该值
            // temp等于绑定房卡 + cards
            long temp = selfFreeGold + gold;
            if (temp >= 0) {
                // 房卡足够
                selfFreeGold = temp;

                minusGold = 0;
                minusFreeGold = -gold;
            } else {
                // 房卡不足，先用完绑定房卡，再用普通房卡
                selfFreeGold = 0;

                minusGold = -temp;
                minusFreeGold = (-gold) - minusGold;
            }
            minusFreeGold += -freeGold;

            selfGold -= minusGold;
            selfFreeGold += freeGold;
        } else {
            minusFreeGold = -freeGold;
            minusGold = -gold;

            selfGold += gold;
            selfFreeGold += freeGold;
        }
        if (minusGold != 0 || minusFreeGold != 0) {
            if (changeUserGold(userId, oldGold, oldFreeGold, -minusGold, -minusFreeGold) <= 0) {
                if (tryCount++ > 20) {
                    LOGGER.info("addUserGold|fail|" + userId + "|" + freeGold + "|" + gold + "|" + sourceType + "|" + tryCount);
                    return 0;
                }
                return addUserGold(userId, freeGold, gold, sourceType, tryCount);
            } else {
                LOGGER.info("addUserGold|succ|" + userId + "|" + freeGold + "|" + gold + "|" + sourceType + "|" + tryCount);

                UserGoldRecord userGoldRecord = new UserGoldRecord(userId, selfFreeGold, selfGold, (int) -minusFreeGold, (int) -minusGold, sourceType);
                UserGoldRecordDao.getInstance().saveUserGoldRecord(userGoldRecord);

                long goldChange = -minusGold - minusFreeGold;
                // 统计数据
//                if (sourceType == SourceType.share_award
//                        || sourceType == SourceType.bind_phone
//                ) {
                    Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
                    GoldDataStatistics data = new GoldDataStatistics(dataDate, sourceType, userId, 1, goldChange);
                    saveOrUpdateGoldDataStatistics(data);

                    GoldDataStatistics dataSys = new GoldDataStatistics(dataDate, sourceType, 0l, 1, goldChange);
                    saveOrUpdateGoldDataStatistics(dataSys);
//                }

                try {
                    RegInfo regInfo = UserDao.getInstance().getUser(userId);
                    if (regInfo != null && regInfo.getEnterServer() > 0 && regInfo.getIsOnLine() == 1) {
                        GameUtil.notifyChangGolds(regInfo.getEnterServer(), userId, (oldGold + oldFreeGold), goldChange, (selfFreeGold + selfGold));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        }
        return 1;
    }

    public void saveOrUpdateGoldDataStatistics(final GoldDataStatistics data) {
        try {
            this.getSql().update("gold.save_or_update_gold_dataStatistics", data);
        } catch (Exception e) {
            LOGGER.error("saveOrUpdateGoldDataStatistics|error|" + e.getMessage(), e);
        }
    }

}
