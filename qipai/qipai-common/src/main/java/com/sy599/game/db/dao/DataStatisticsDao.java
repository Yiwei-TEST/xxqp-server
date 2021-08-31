package com.sy599.game.db.dao;


import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.db.bean.DataStatistics;
import com.sy599.game.db.bean.GoldDataStatistics;
import com.sy599.game.util.LogUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStatisticsDao extends BaseDao {
	private static DataStatisticsDao _inst = new DataStatisticsDao();

	public static DataStatisticsDao getInstance() {
		return _inst;
	}

	/**
	 * 数据统计
	 *
	 * @param dataStatistics
	 * @param type <br/>1：单大局大赢家<br/>2：单大局大负豪<br/>3：总小局数<br/>4：单大局赢最多<br/>5：单大局输最多<br/>
	 */
	public void saveOrUpdateDataStatistics(final DataStatistics dataStatistics,final int type){
	    try {
            TaskExecutor.EXECUTOR_SERVICE_STATISTICS.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        getSqlLoginClient().update("dataStatistics.save_or_update" + type, dataStatistics);
                    } catch (Exception e) {
                        LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
                    }
                }
            });
        }catch (Exception e){
            LogUtil.errorLog.error("saveOrUpdateDataStatistics|error|" + e.getMessage(), e);
        }
	}

	public void saveOrUpdateLoginData(final Map<String,Object> map){
        try {
            TaskExecutor.EXECUTOR_SERVICE_STATISTICS.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        getSqlLoginClient().update("dataStatistics.save_or_update_login_data",map);
                    }catch (Exception e){
                        LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
                    }
                }
            });
        }catch (Exception e){
            LogUtil.errorLog.error("saveOrUpdateDataStatistics|error|" + e.getMessage(), e);
        }
	}

	public void updateLoginData(final Map<String,Object> map){
		try {
			getSqlLoginClient().update("dataStatistics.update_login_data",map);
		}catch (Exception e){
			LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
		}
	}

	public void saveOrUpdateOnlineData(final String currentTime,final String serverId,final int currentCount){
		try {
			Map<String,Object> map = new HashMap<>();
			map.put("currentTime",currentTime);
			map.put("serverId",serverId);
			map.put("currentCount",currentCount);
			getSqlLoginClient().update("dataStatistics.save_or_update_online_data",map);
		}catch (Exception e){
			LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
		}
	}

	public void saveSystemUserCountlogin(final Map<String,Object> map){
        try {
            TaskExecutor.EXECUTOR_SERVICE_STATISTICS.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        getSqlLoginClient().update("dataStatistics.save_system_user_countlogin",map);
                    }catch (Exception e){
                        LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
                    }
                }
            });
        }catch (Exception e){
            LogUtil.errorLog.error("saveOrUpdateDataStatistics|error|" + e.getMessage(), e);
        }
	}


    public void saveOrUpdateDataStatisticsBjd(final DataStatistics dataStatistics){
	    try {
            TaskExecutor.EXECUTOR_SERVICE_STATISTICS.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        getSqlLoginClient().update("dataStatistics.save_or_update_bjd", dataStatistics);
                    } catch (Exception e) {
                        LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
                    }
                }
            });
        }catch (Exception e){
            LogUtil.errorLog.error("saveOrUpdateDataStatisticsBjd|error|" + e.getMessage(), e);
        }
    }

    public DataStatistics loadMaxWzjsOfUser(long userId){
        try {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR,-30);
            Long startDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(c.getTime()));
            Map<String,Object> map = new HashMap<>();
            map.put("userId",String.valueOf(userId));
            map.put("startDate",startDate);
            return (DataStatistics)getSqlLoginClient().queryForObject("dataStatistics.loadMaxWzjsOfUser",map);
        }catch (Exception e){
            LogUtil.errorLog.error("loadMaxWzjsOfUser|error|"+e.getMessage(),e);
        }
        return null;
    }

    public void saveOrUpdateGoldDataStatistics(final GoldDataStatistics data){
        try {
            TaskExecutor.EXECUTOR_SERVICE_STATISTICS.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        getSqlLoginClient().update("dataStatistics.save_or_update_gold_dataStatistics", data);
                    } catch (Exception e) {
                        LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
                    }
                }
            });
        }catch (Exception e){
            LogUtil.errorLog.error("saveOrUpdateGoldDataStatistics|error|" + e.getMessage(), e);
        }
    }


    public long loadGoldDataStatisticsDataValue(long dataDate, int dataType, long userId) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("dataDate", dataDate);
            map.put("dataType", dataType);
            map.put("userId", userId);
            Long res =  (Long) getSqlLoginClient().queryForObject("dataStatistics.load_gold_data_statistics_dataValue", map);
            if(res == null){
                res = 0l;
            }
            return res;
        } catch (Exception e) {
            LogUtil.errorLog.error("loadGoldDataStatisticsDataValue|error|" + e.getMessage(), e);
        }
        return 0;
    }

    public List<Map<String, Object>> loadGoldRoomRobotPlayTypeLose(long dataDate, String dataTypes) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("dataDate", dataDate);
            map.put("dataTypes", dataTypes);
            return (List<Map<String, Object>>) getSqlLoginClient().queryForList("dataStatistics.load_goldRoom_robot_playType_lose", map);
        } catch (Exception e) {
            LogUtil.errorLog.error("loadGoldRoomRobotPlayTypeLose|error|" + e.getMessage(), e);
        }
        return null;
    }

}
