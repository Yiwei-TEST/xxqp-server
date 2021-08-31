package com.sy599.game.db.dao;

import com.sy599.game.robot.Robot;
import com.sy599.game.util.LogUtil;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class RobotDao extends BaseDao {

    private static RobotDao _inst = new RobotDao();

    public static RobotDao getInstance() {
        return _inst;
    }

    public Long randRobotUserId() {
        try {
            List<Long> list = (List<Long>) getSqlLoginClient().queryForList("robot.rand_robot", 1);
            if (list == null || list.size() == 0) {
                return 0L;
            }
            Collections.shuffle(list);
            for (Long uid : list) {
                if (useRobot(uid) == 1) {
                    return uid;
                }
            }
        } catch (SQLException e) {
            LogUtil.dbLog.error("robot.rand_robot:", e);
        }
        return 0L;
    }

    public List<Robot> loadAllRobot(){
        try {
            return (List<Robot>) getSqlLoginClient().queryForList("robot.load_all_robot");
        } catch (SQLException e) {
            LogUtil.dbLog.error("robot.use_robot:", e);
        }
        return null;
    }

    public int useRobot(long userId) {
        try {
            return getSqlLoginClient().update("robot.use_robot", userId);
        } catch (SQLException e) {
            LogUtil.dbLog.error("robot.use_robot:", e);
        }
        return 0;
    }

    public int recycleRobot(long userId) {
        try {
            return getSqlLoginClient().update("robot.recycle_robot", userId);
        } catch (SQLException e) {
            LogUtil.dbLog.error("robot.recycle_robot:", e);
        }
        return 0;
    }
}
