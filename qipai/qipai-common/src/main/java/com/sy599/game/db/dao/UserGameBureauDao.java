package com.sy599.game.db.dao;

import com.sy599.game.db.bean.UserGameBureau;
import com.sy599.game.util.LogUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserGameBureauDao extends BaseDao {
    private static UserGameBureauDao _inst = new UserGameBureauDao();

    public static UserGameBureauDao getInstance() {
        return _inst;
    }

    public void saveUserGameBureau(UserGameBureau userGameRebate) {
        try {
            getSqlLoginClient().insert("userGameBureau.saveUserGameBureau", userGameRebate);
        } catch (Exception e) {
            LogUtil.e("UserGameBureau.saveUserGameBureau err", e);
        }
    }

    public UserGameBureau loadOneUserGameBureau(long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        try {
            List<UserGameBureau> list = getSqlLoginClient().queryForList("userGameBureau.loadOneUserGameBureau", params);
            if (list != null && list.size() > 0) {
                return list.get(0);
            }
        } catch (SQLException e) {
            LogUtil.e("UserGameBureau.loadOneUserGameBureau err", e);
        }
        return null;
    }
}
