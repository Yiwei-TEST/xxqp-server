package com.sy.sanguo.game.pdkuai.manager;

import com.sy.sanguo.game.bean.group.GroupUser;
import com.sy.sanguo.game.constants.GroupConstants;
import com.sy.sanguo.game.dao.StatisticsDaoImpl;
import com.sy.sanguo.game.pdkuai.db.dblock.DbLockEnum;
import com.sy.sanguo.game.pdkuai.db.dblock.DbLockUtil;
import com.sy.sanguo.game.pdkuai.util.LogUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class StatisticsManager {
    private static StatisticsManager _inst = new StatisticsManager();

    public static StatisticsManager getInstance() {
        return _inst;
    }

    public void init() {

        // --------------------------------------------------
        // ------------每天凌晨2点执行任务---------------------
        // --------------------------------------------------
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        if (calendar.get(Calendar.HOUR_OF_DAY) >= 2) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 2);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                logGroupGoldWin();
            }
        }, calendar.getTime(), 24 * 60 * 60 * 1000);
    }

    public void logGroupGoldWin(long dataDate) {
        try {
            List<Long> groupIdList = StatisticsDaoImpl.getInstance().allGroupIdGold();
            if (groupIdList == null || groupIdList.size() == 0) {
                return;
            }
            for (Long groupId : groupIdList) {
                logGroupGoldWin(groupId, dataDate);
            }
        } catch (Exception e) {
            LogUtil.e("Exception:" + e.getMessage(), e);
        }
    }

    public void logGroupGoldWin() {
        try {
            List<Long> groupIdList = StatisticsDaoImpl.getInstance().allGroupIdGold();
            if (groupIdList == null || groupIdList.size() == 0) {
                return;
            }

            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR, -1);
            long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(c.getTime()));

            for (Long groupId : groupIdList) {
                logGroupGoldWin(groupId, dataDate);
            }
        } catch (Exception e) {
            LogUtil.e("Exception:" + e.getMessage(), e);
        }
    }

    public void logGroupGoldWin(long groupId, long dataDate) {
        String unlockKey = null;
        try {
            unlockKey = DbLockUtil.lock(DbLockEnum.GROUP_GOLD_STATISTICS, String.valueOf(groupId));
            if (unlockKey == null) {
                return;
            }
            List<GroupUser> guList = StatisticsDaoImpl.getInstance().allGroupUserAdmin(groupId);
            if (guList == null && guList.size() == 0) {
                return;
            }
            Integer maxPromoterLevel = StatisticsDaoImpl.getInstance().groupMaxPromoterLevel(groupId);
            List<HashMap<String, Object>> logList = new ArrayList<>();
            Map<String, HashMap<String, Object>> logMap = new HashMap<>();
            List<HashMap<String, Object>> logSelf = new ArrayList<>();
            for (GroupUser gu : guList) {
                if (GroupConstants.isHuiZhang(gu.getUserRole())) {
                    List<HashMap<String, Object>> masterDataList = StatisticsDaoImpl.getInstance().groupGoldWinStatisticsMaster(dataDate, groupId);
                    for (HashMap<String, Object> masterData : masterDataList) {
                        masterData.put("userId", gu.getUserId());
                        masterData.put("dataDate", dataDate);
                        masterData.put("groupId", groupId);
                        logList.add(masterData);
                        logMap.put(gu.getUserId().toString(), masterData);
                    }
                }
                List<HashMap<String, Object>> dataList = StatisticsDaoImpl.getInstance().groupGoldWinStatisticsNextLevel(dataDate, groupId, gu.getUserId(), gu.getPromoterLevel());
                if (dataList != null || dataList.size() > 0) {
                    for (HashMap<String, Object> data : dataList) {
                        if (!"0".equals(data.get("userId").toString())) {
                            data.put("groupId", groupId);
                            data.put("dataDate", dataDate);
                            logList.add(data);
                            logMap.put(data.get("userId").toString(), data);
                        }
                    }
                }
                List<HashMap<String, Object>> selfDataList = StatisticsDaoImpl.getInstance().groupGoldWinStatisticsSelf(dataDate, groupId, gu.getUserId(), gu.getPromoterLevel());
                if (selfDataList != null || selfDataList.size() > 0) {
                    for (HashMap<String, Object> selfData : selfDataList) {
                        HashMap<String, Object> log = logMap.get(gu.getUserId().toString());
                        log.put("selfGroupWin", selfData.get("selfGroupWin"));
                        log.put("selfGroupLose", selfData.get("selfGroupLose"));
                        log.put("selfGroupJsCount", selfData.get("selfGroupJsCount"));
                    }
                }
            }
            if (logList.size() > 0) {
                StatisticsDaoImpl.getInstance().saveLogGroupGoldWin(logList);
            }
        } catch (Exception e) {
            LogUtil.e("Exception:" + e.getMessage(), e);
        } finally {
            if (unlockKey != null) {
                DbLockUtil.unLock(DbLockEnum.GROUP_GOLD_STATISTICS, String.valueOf(groupId), unlockKey);
            }
        }
    }

}
