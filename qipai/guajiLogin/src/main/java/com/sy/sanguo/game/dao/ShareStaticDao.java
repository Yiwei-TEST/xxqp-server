package com.sy.sanguo.game.dao;

import com.sy.sanguo.game.bean.ShareStaticData;
import com.sy.sanguo.game.pdkuai.db.dao.BaseDao;

import java.sql.SQLException;

public class ShareStaticDao extends BaseDao {

    private static ShareStaticDao _inst = new ShareStaticDao();

    public static ShareStaticDao getInstance() {
        return _inst;
    }

    /**
     * 添加一条分享记录
     * @param shareData
     */
    public void addShareData(ShareStaticData shareData) {
        try {
            getSql().insert("shareStaticData.addShareData", shareData);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
