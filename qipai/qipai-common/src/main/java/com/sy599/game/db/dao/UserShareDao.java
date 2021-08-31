package com.sy599.game.db.dao;

import com.sy599.game.db.bean.UserShare;
import com.sy599.game.util.LogUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserShareDao extends BaseDao {
    private static UserShareDao _inst = new UserShareDao();

    public static UserShareDao getInstance() {
        return _inst;
    }

    @SuppressWarnings("unchecked")
    public List<UserShare> getUserShare(long userId, String ymd) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("startTime", ymd + " 00:00:00");
            map.put("endTime", ymd + " 23:59:59");
            map.put("type", 1);
            return getSqlLoginClient().queryForList("userShare.getUserShareInfo", map);
        } catch (SQLException e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
        return null;
    }

    public List<UserShare> getUserShare(long userId, String ymd, int type) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("startTime", ymd + " 00:00:00");
            map.put("endTime", ymd + " 23:59:59");
            map.put("type", type);
            return getSqlLoginClient().queryForList("userShare.getUserShareInfo", map);
        } catch (SQLException e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
        return null;
    }

    public int countUserShare(long userId, String startTime, String endTime) {
        return countUserShare(userId, startTime, endTime, null);
    }

    public int countUserShare(long userId, String startTime, String endTime, String extend) {
        try {
            Map<String, String> map = new HashMap<>();
            map.put("userId", String.valueOf(userId));
            map.put("startTime", startTime);
            map.put("endTime", endTime);
            map.put("type", "1");
            if (StringUtils.isNotBlank(extend)) {
                map.put("extend", extend);
            }
            return (Integer) getSqlLoginClient().queryForObject("userShare.countUserShareInfo", map);
        } catch (SQLException e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * 添加一条分享记录
     *
     * @param userShare
     */
    public void addUserShare(UserShare userShare) throws Exception {
        getSqlLoginClient().insert("userShare.addUserShare", userShare);
    }
}
