package com.sy.sanguo.common.executor;

import java.text.SimpleDateFormat;
import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.CacheUtil;
import com.sy.mainland.util.redis.Redis;
import com.sy.mainland.util.redis.RedisUtil;
import com.sy.sanguo.common.executor.task.BaseTask;
import com.sy.sanguo.common.executor.task.ClearLogTask;
import com.sy.sanguo.common.init.InitData;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.user.GameUtil;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.enums.CardSourceType;
import com.sy.sanguo.game.dao.RoomDaoImpl;
import com.sy.sanguo.game.dao.SqlDao;
import com.sy.sanguo.game.dao.UserDao;
import com.sy.sanguo.game.dao.group.GroupDaoManager;
import com.sy.sanguo.game.pdkuai.db.bean.UserMessage;
import com.sy.sanguo.game.pdkuai.db.dao.SystemCommonInfoDao;
import com.sy.sanguo.game.pdkuai.manager.DaikaiManager;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import com.sy599.sanguo.util.SysPartitionUtil;
import com.sy599.sanguo.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class HourTask implements Runnable {
	private static List<BaseTask> taskList = new ArrayList<BaseTask>();
	static {
		taskList.add(new ClearLogTask(4, 0));
	}

	@Override
	public void run() {
		Calendar calendar = TimeUtil.curCalendar();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);

		LogUtil.i("------hour:" + hour + " task run---"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

		for (BaseTask task : taskList) {
//			task.isRun(hour, 0);
			task.run();
		}

		LogUtil.i("------hour:" + hour + " task run---"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

		SystemCommonInfoDao.getInstance().selectFuncOne();
		// 清理代开房间
		 DaikaiManager.getInstance().clearDaikaiTable();

		 for(Integer seq : SysPartitionUtil.allGroupSeqList){
		     clearData(seq);
         }


	}

	public void clearData(int gpSeq){
        try{
            if (SqlDao.getInstance().checkExistsGroupTable(gpSeq)>0){
                int count=0;
                SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                Calendar cal=Calendar.getInstance();
                cal.add(Calendar.HOUR_OF_DAY,-48);
                String endDate=sdf.format(cal.getTime());
                cal.add(Calendar.HOUR_OF_DAY,-36);
                String startDate=sdf.format(cal.getTime());
                int pageNo=1,pageSize=1000;
                List<HashMap<String,Object>> list;
                Map<Integer, Map<String, String>> serverMap = new HashMap<>();
                while ((list= GroupDaoManager.getInstance().loadGroupTables(startDate,endDate,pageNo,pageSize,gpSeq))!=null&&list.size()>0){
                    Map<String,Integer> userMap=new HashMap<>();
                    Map<String,Integer> tableMap=new HashMap<>();
                    for (HashMap<String,Object> map:list){
                        String key = String.valueOf(map.get("keyId"));
                        String groupId = String.valueOf(map.get("groupId"));
                        if (GroupDaoManager.getInstance().updateGroupTableByKeyId(key,groupId)>0){
                            JSONObject tableJson = JSONObject.parseObject(String.valueOf(map.get("tableMsg")));
                            String groupRoom = tableJson.getString("room");
                            if (groupRoom==null){
                                groupRoom="0";
                            }
                            if (Redis.isConnected()){
                                if ("0".equals(groupRoom)) {
                                    RedisUtil.zrem(CacheUtil.loadStringKey(null, new StringBuilder("group_tables_").append(groupId).toString()), key);
                                    RedisUtil.hdel(CacheUtil.loadStringKey(null, new StringBuilder("msg_group_tables_").append(groupId).toString()), key);
                                }else{
                                    RedisUtil.zrem(CacheUtil.loadStringKey(null, new StringBuilder("group_tables_").append(groupId).append("_").append(groupRoom).toString()), key);
                                    RedisUtil.hdel(CacheUtil.loadStringKey(null, new StringBuilder("msg_group_tables_").append(groupId).append("_").append(groupRoom).toString()), key);
                                }
                            }

                            int serverId = NumberUtils.toInt(String.valueOf(map.get("serverId")), 0);
                            if (serverId > 0) {
                                Map<String, String> infoMap = serverMap.get(serverId);
                                if (infoMap == null || infoMap.isEmpty()) {
                                    infoMap = new HashMap<>();
                                    infoMap.put("tableIds", String.valueOf(map.get("tableId")));
                                    infoMap.put("keyIds", key);
                                    serverMap.put(serverId, infoMap);
                                } else {
                                    String tableIds = String.valueOf(infoMap.get("tableIds"));
                                    String keyIds = String.valueOf(infoMap.get("keyIds"));
                                    infoMap.put("tableIds", tableIds + "," + String.valueOf(map.get("tableId")));
                                    infoMap.put("keyIds", keyIds + "," + key);
                                    serverMap.put(serverId, infoMap);
                                }
                            }
                            RoomDaoImpl.getInstance().clearRoom(Long.parseLong(String.valueOf(map.get("tableId"))));
                            count++;
                            String[] tempMsgs = tableJson.getString("strs").split(";")[0].split("_");
                            if (tempMsgs.length>=4) {
                                String payType = tempMsgs[0];
                                String tempUserId = tempMsgs[2];
                                int tempNum = Integer.parseInt(tempMsgs[3]);
                                if (("2".equals(payType) || "3".equals(payType))&&tempNum>0) {
                                    Integer integer=userMap.get(tempUserId);
                                    if (integer==null){
                                        userMap.put(tempUserId,tempNum);
                                    }else{
                                        userMap.put(tempUserId,tempNum+integer.intValue());
                                    }
                                    Integer currentCount=tableMap.get(tempUserId);
                                    if (currentCount == null){
                                        tableMap.put(tempUserId,1);
                                    }else{
                                        tableMap.put(tempUserId,currentCount+1);
                                    }
                                }
                            }
                        }
                    }

                    for (Integer serverId : serverMap.keySet()) {
                        String res = "";
                        int c = 0;
                        while (StringUtils.isBlank(res) && c <= 5) {
                            c++;
                            res = GameUtil.sendDissInfo(serverId, serverMap.get(serverId));
                            GameBackLogger.SYS_LOG.info("sendDissInfo-->serverId:" + serverId + ",infoMap:" + serverMap.get(serverId) + ",res:" + res);
                        }
                    }

                    for (Map.Entry<String,Integer> kv:userMap.entrySet()){
                        try {
                            RegInfo user=UserDao.getInstance().getUser(Long.parseLong(kv.getKey()));
                            if (user!=null){
                                UserMessage message=new UserMessage();
                                message.setUserId(user.getUserId());
                                if(InitData.groupToQinYouQuan == 1)
                                    message.setContent(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " "+tableMap.get(kv.getKey())+"个亲友圈房间未开局被解散，获得钻石x" + kv.getValue());
                                else
                                    message.setContent(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " "+tableMap.get(kv.getKey())+"个俱乐部房间未开局被解散，获得钻石x" + kv.getValue());
                                message.setTime(new Date());
                                message.setType(0);
                                UserDao.getInstance().addUserCards(user, 0, kv.getValue(), 0, null, message, CardSourceType.groupTable_diss_QZ);
                            }
                        } catch (Exception e) {
                            GameBackLogger.SYS_LOG.error("dissTable return card error tableId:" + kv.getKey() + ",count="+kv.getValue(), e);
                        }
                    }

                    if (list.size()<pageSize){
                        break;
                    }
                }

                LogUtil.i("timeout auto diss group table:count="+count);
            }
        }catch (Throwable t){
            LogUtil.e("Throwable:"+t.getMessage(),t);
        }
    }

}
