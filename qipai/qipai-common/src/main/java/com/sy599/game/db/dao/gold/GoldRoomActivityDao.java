package com.sy599.game.db.dao.gold;

import com.alibaba.fastjson.JSON;
import com.sy599.game.db.bean.gold.GoldAcitivityRankResult;
import com.sy599.game.db.bean.gold.GoldRoomActivityUserItem;
import com.sy599.game.db.dao.BaseDao;
import com.sy599.game.util.LogUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

/**
 * 金币场活动
 */
public class GoldRoomActivityDao extends BaseDao {
    private static GoldRoomActivityDao goldRoomActivityDao = new GoldRoomActivityDao();

    public static GoldRoomActivityDao getInstance() {
        return goldRoomActivityDao;
    }

    public int updateGoldRoomByKeyId(HashMap<String, Object> map) throws Exception {
        return this.getSqlLoginClient().update("goldRoom.update_gold_room", map);
    }

    public Long saveGoldRoomActivityUserItem(GoldRoomActivityUserItem activityUserItem) {
        try {
            return (Long) this.getSqlLoginClient().insert("goldRoomActivity.save", activityUserItem);
        } catch (Exception e) {
            LogUtil.errorLog.error("GoldRoomActivityUserItem|error|" + JSON.toJSONString(activityUserItem), e);
        }
        return 0L;
    }

    public List<GoldRoomActivityUserItem> loadItemByUserId(long userId) throws SQLException {
        return (List<GoldRoomActivityUserItem>) this.getSqlLoginClient().queryForList("goldRoomActivity.queryInfoByUserid", userId);
    }
    public List<GoldRoomActivityUserItem> queryInfoByUseridAndThem(long userId,int them) throws SQLException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userid", userId);
        map.put("them", them);
        return (List<GoldRoomActivityUserItem>) this.getSqlLoginClient().queryForList("goldRoomActivity.queryInfoByUseridAndThem", map);
    }


    public boolean updateItem(GoldRoomActivityUserItem item) throws SQLException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("keyId", item.getKeyId());
        map.put("activityBureau", item.getActivityBureau());
        map.put("activityItemNum", item.getActivityItemNum());
        map.put("everydayLimit", item.getEverydayLimit());
        map.put("daterecord", item.getDaterecord());
        this.getSqlLoginClient().update("goldRoomActivity.updateItem", map);
        return true;
    }

    public boolean updateReward(GoldRoomActivityUserItem item) throws SQLException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("keyId", item.getKeyId());
        map.put("isReward", item.getIsReward());
        this.getSqlLoginClient().update("goldRoomActivity.updateReward", map);
        return true;
    }

    /**
     * 2020年6月22日 端午活动排行榜
     *
     * @return
     * @throws SQLException
     */
    public List<GoldRoomActivityUserItem> queryActivityRankList() throws SQLException {
        return this.getSqlLoginClient().queryForList("goldRoomActivity.queryActivityRankList");
    }

    public List<GoldAcitivityRankResult> queryActivityRankMap() throws SQLException {
        return this.getSqlLoginClient().queryForList("goldRoomActivity.queryActivityRankMap");

    }

    public List<GoldAcitivityRankResult> queryActivityRankMapByUserId(HashMap<String, Object> map) throws SQLException {
        return this.getSqlLoginClient().queryForList("goldRoomActivity.queryActivityRankMapByUserId", map);

    }

    /**
     * 根据用户Id和活动主题查询活动物品信息
     * @param map
     * @return
     * @throws SQLException
     */
    public List<GoldRoomActivityUserItem> loadItemByUserIdByThem(HashMap<String, Object> map) throws SQLException {
        return (List<GoldRoomActivityUserItem>) this.getSqlLoginClient().queryForList("goldRoomActivity.queryInfoByUseridByThem", map);
    }

    /**
     * 活动排行榜查询
     * @param them
     * @param userid
     * @return
     */
    public List<GoldAcitivityRankResult> queryActivityRankMapByThemAndUserId(int them,String userid) throws SQLException{
        HashMap<String, Object> map = new HashMap<>();
        map.put("them", them);
        map.put("userid", userid);
        return this.getSqlLoginClient().queryForList("goldRoomActivity.queryActivityRankMapByThemAndUserId", map);

    }
}
