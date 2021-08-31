package com.sy.sanguo.game.dao;

import com.sy.sanguo.game.bean.UserSign;
import com.sy.sanguo.game.pdkuai.db.dao.BaseDao;
import com.sy.sanguo.game.pdkuai.util.LogUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by pc on 2017/4/20.
 */
public class UserSignDao extends BaseDao{
    private static UserSignDao _inst = new UserSignDao();

    public static UserSignDao getInstance() {
        return _inst;
    }

    @SuppressWarnings("unchecked")
    public List<UserSign> getUserSign(long userId) {
        try {
            return (List<UserSign>) this.getSql().queryForList("userSign.getSigns", userId);
        } catch (SQLException e) {
            LogUtil.e("getUseSign err", e);
        }
        return null;
    }

}

