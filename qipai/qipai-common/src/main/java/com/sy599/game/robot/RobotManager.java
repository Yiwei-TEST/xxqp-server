package com.sy599.game.robot;

import com.sy599.game.character.Player;
import com.sy599.game.db.dao.RobotDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.TimeUtil;
import jbt.execution.core.IBTExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RobotManager {

    private static final Map<Integer, RobotAIGenerator> AI_MAP = new ConcurrentHashMap<>();

    /*** 机器人：<type,<playType,<hour,robotId>***/
    private static final Map<Integer, Map<Integer, Map<Integer, List<Long>>>> robotMap = new ConcurrentHashMap<>();


    public static void init() {
        try {
            List<Robot> robots = RobotDao.getInstance().loadAllRobot();
            robotMap.clear();
            if (robots != null && robots.size() > 0) {
                for (Robot robot : robots) {
                    Map<Integer, Map<Integer, List<Long>>> typeMap = robotMap.get(robot.getType());
                    if (typeMap == null) {
                        typeMap = new ConcurrentHashMap<>();
                        robotMap.put(robot.getType(), typeMap);
                    }
                    List<Integer> playTypeList = robot.getPlayTypeList();
                    List<Integer> hourList = robot.getHourList();
                    if (playTypeList == null || playTypeList.size() == 0 || hourList == null || hourList.size() == 0) {
                        continue;
                    }
                    for (Integer playType : playTypeList) {
                        Map<Integer, List<Long>> playTypeMap = typeMap.get(playType);
                        if (playTypeMap == null) {
                            playTypeMap = new ConcurrentHashMap<>();
                            typeMap.put(playType, playTypeMap);
                        }
                        for (Integer hour : hourList) {
                            List<Long> robotIdList = playTypeMap.get(hour);
                            if (robotIdList == null) {
                                robotIdList = new ArrayList<>();
                                playTypeMap.put(hour, robotIdList);
                            }
                            robotIdList.add(robot.getUserId());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("RobotManager|init|error|" + e.getMessage(), e);
        }
    }

    /**
     * 注册AI
     *
     * @param playType
     * @param aiCreator
     * @return
     */
    public static boolean regAIGenerator(Integer playType, RobotAIGenerator aiCreator) {
        if (AI_MAP.containsKey(playType)) {
            StringBuilder sb = new StringBuilder("RobotAIManager|regAIGenerator|error|hasSameWanFa");
            sb.append("|").append(playType);
            sb.append("|").append(aiCreator.getClass().getName());
            throw new RuntimeException(sb.toString());
        }
        AI_MAP.put(playType, aiCreator);
        LogUtil.robot.info("RobotAIManager|regAIGenerator|" + playType + "|" + aiCreator.getClass().getName());
        return false;
    }

    public static IBTExecutor generateRobotAI(int playType, int level) {
        RobotAIGenerator robotAICreator = AI_MAP.get(playType);
        if (robotAICreator == null) {
            return null;
        }
        return robotAICreator.generateRobotAI(level);
    }

    public static long randomRobot(int type, int playType) {
        Map<Integer, Map<Integer, List<Long>>> typeMap = robotMap.get(type);
        if (typeMap == null || typeMap.size() == 0) {
            return 0;
        }
        Map<Integer, List<Long>> playTypeMap = typeMap.get(playType);
        if (playTypeMap == null || playTypeMap.size() == 0) {
            return 0;
        }
        int curHour = TimeUtil.curHour();
        List<Long> userIds = playTypeMap.get(curHour);
        if (userIds == null || userIds.size() == 0) {
            return 0;
        }
        List<Long> robotIds = new ArrayList<>(userIds);
        Collections.shuffle(robotIds);
        for (Long robotId : robotIds) {
            if (RobotDao.getInstance().useRobot(robotId) == 1) {
                return robotId;
            }
        }
        return 0;
    }

    public static Player genGoldRoomRobot(int playType) {
        return genRobot(Robot.type_goldRoom, playType);
    }

    public static Player genRobot(int robotType, int playType) {
        if (!AI_MAP.containsKey(playType)) {
            return null;
        }
        long robotId = randomRobot(robotType, playType);
        if (robotId <= 0) {
            LogUtil.robot.error("RobotAIManager|genRobot|error|1|" + playType + "|" + robotId);
            return null;
        }
        Player robot = PlayerManager.getInstance().loadPlayer(robotId, playType);
        if (robot == null) {
            LogUtil.robot.error("RobotAIManager|genRobot|error|2|" + playType + "|" + robotId);
            return null;
        }
        int robotLevel = 1;
        IBTExecutor robotAI = generateRobotAI(playType, robotLevel);
        if (robotAI == null) {
            LogUtil.robot.error("RobotAIManager|genRobot|error|3|" + playType + "|" + robotId + "|" + robotLevel);
            return null;
        }
        robot.setIsOnline(1);
        robot.setRobotAILevel(robotLevel);
        return robot;
    }

    public static int recycleRobot(long userId) {
        return RobotDao.getInstance().recycleRobot(userId);
    }
}
