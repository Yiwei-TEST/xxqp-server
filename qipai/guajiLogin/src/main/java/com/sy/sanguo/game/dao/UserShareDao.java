package com.sy.sanguo.game.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sy.sanguo.game.bean.UserShare;
import com.sy.sanguo.game.pdkuai.db.dao.BaseDao;

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
            return getSql().queryForList("userShare.getUserShareInfo", map);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int countUserShare(long userId, String startTime, String endTime) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("startTime", startTime);
            map.put("endTime", endTime);
            map.put("type", 1);
            return (Integer) getSql().queryForObject("userShare.countUserShareInfo", map);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 添加一条分享记录
     *
     * @param userShare
     */
    public void addUserShare(UserShare userShare) throws Exception {
        getSql().insert("userShare.addUserShare", userShare);
    }


}
