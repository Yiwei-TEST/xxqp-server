package com.sy599.game.db.dao;

import com.sy.mainland.util.CommonUtil;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.db.enums.DbEnum;
import com.sy599.game.util.LogUtil;

import java.util.HashMap;
import java.util.Map;

public class UserDatasDao extends BaseDao {
    private static UserDatasDao _inst = new UserDatasDao();
    private volatile static int mark = -1;

    public static UserDatasDao getInstance() {
        return _inst;
    }

    public boolean exists() {
        if (mark == -1) {
            synchronized (this) {
                if (mark == -1) {
                    try {
                        mark = TableCheckDao.getInstance().checkTableExists(DbEnum.LOGIN,"t_user_datas") ? 1 : 0;
                    } catch (Exception e) {
                        mark = 0;
                        LogUtil.errorLog.error("dataStatistics.t_user_datas_table_exists>" + e.getMessage(), e);
                    }
                }
            }
        }
        return mark == 1;
    }

    /**
     * 保存
     */
    public void saveUserDatas(String userId,
                              String gameCode,
                              String roomType,
                              String dataCode,
                              String dataValue) {
        if (exists()) {
            final Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("gameCode", gameCode);
            map.put("roomType", roomType);
            map.put("dataCode", dataCode);
            map.put("dataValue", dataValue);
            map.put("createdTime", CommonUtil.dateTimeToString());

            TaskExecutor.EXECUTOR_SERVICE_STATISTICS.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        getSqlLoginClient().update("dataStatistics.save_user_datas", map);
                    } catch (Exception e) {
                        LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
                    }
                }
            });
        }
    }

    /**
     * 更新
     */
    public void updateUserDatas(String userId,
                                String gameCode,
                                String roomType,
                                String dataCode,
                                String dataValue) {
        if (exists()) {
            final Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("gameCode", gameCode);
            map.put("roomType", roomType);
            map.put("dataCode", dataCode);
            map.put("dataValue", dataValue);
            map.put("createdTime", CommonUtil.dateTimeToString());

            TaskExecutor.EXECUTOR_SERVICE_STATISTICS.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        getSqlLoginClient().update("dataStatistics.update_user_datas", map);
                    } catch (Exception e) {
                        LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
                    }
                }
            });
        }
    }

    /**
     * 获取
     */
    public String selectUserDataValue(String userId,
                                      String gameCode,
                                      String roomType,
                                      String dataCode) {
        if (exists()) {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("gameCode", gameCode);
            map.put("roomType", roomType);
            map.put("dataCode", dataCode);
            try {
                Object obj = getSqlLoginClient().queryForObject("dataStatistics.select_user_datas", map);
                return obj == null ? null : String.valueOf(obj);
            } catch (Exception e) {
                LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
            }
        }
        return null;
    }
}
