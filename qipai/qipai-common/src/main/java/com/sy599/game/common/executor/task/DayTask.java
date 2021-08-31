package com.sy599.game.common.executor.task;

import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.gold.GoldDao;
import com.sy599.game.util.gold.constants.GoldConstans;

import java.util.*;

public class DayTask implements Runnable{
    @Override
    public void run() {
        if (GoldConstans.isGoldSiteOpen()) {
            GoldDao.getInstance().clearDrawRemedy();
        }

//        String timeRemoveBindStr = ResourcesConfigsUtil.loadServerPropertyValue("periodRemoveBind");
//        if (!StringUtil.isBlank(timeRemoveBindStr)&&!"0".equals(timeRemoveBindStr)) {
//            autoRemoveBind(timeRemoveBindStr);
//        }
    }

    /**
     * 自动解绑（根据游戏时间间隔）
     */
    private void autoRemoveBind(String timeRemoveBindStr) {
        int period = Integer.parseInt(timeRemoveBindStr);
        List<Map> list = UserDao.getInstance().getNeedRBList(period);
        List<Long> idList = new ArrayList<>();
        for (Object map : list) {
            Long id = (Long)((Map) map).get("userId");
            idList.add(id);
        }
        UserDao.getInstance().removeBindByIdList(idList);
        UserDao.getInstance().addRBRecord(list);
    }
}
