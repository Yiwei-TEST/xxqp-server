package com.sy599.game.common.asyn;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.CacheUtil;
import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.MessageQueueUtil;
import com.sy.mainland.util.redis.Redis;
import com.sy.mainland.util.redis.RedisUtil;
import com.sy599.game.db.bean.PlayLogTable;
import com.sy599.game.db.bean.UserGroupPlaylog;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ResourcesConfigsUtil;

import java.util.concurrent.atomic.AtomicBoolean;

public class AsynExecutor {
    private static boolean REDIS_CONNECTED = Redis.isConnected();
    private static final AtomicBoolean state = new AtomicBoolean(REDIS_CONNECTED);
    private static String KEY_USER_PLAY_LOG_ID;
    private static String KEY_USER_PLAY_LOG_QUEUE;
    private static String KEY_GROUP_PLAY_LOG_ID;
    private static String KEY_GROUP_PLAY_LOG_QUEUE;
    private static String KEY_UPDATE_GROUP_PLAY_LOG_ID;
    private static String KEY_UPDATE_GROUP_PLAY_LOG_QUEUE;
    private static String KEY_PLAYLOG_TABLE_ID;
    private static String KEY_PLAYLOG_TABLE_QUEUE;
    private static final Thread thread;

    static {
        try {
            KEY_USER_PLAY_LOG_ID = CacheUtil.loadStringKey(null, "user_play_log_id20180328");
            KEY_USER_PLAY_LOG_QUEUE = CacheUtil.loadStringKey(null, "user_play_log_queue20180328");
            KEY_GROUP_PLAY_LOG_ID = CacheUtil.loadStringKey(null,"group_play_log_id20181106");
            KEY_GROUP_PLAY_LOG_QUEUE = CacheUtil.loadStringKey(null,"group_play_log_queue20181106");
            KEY_UPDATE_GROUP_PLAY_LOG_ID = CacheUtil.loadStringKey(null,"update_group_play_log_id20181106");
            KEY_UPDATE_GROUP_PLAY_LOG_QUEUE = CacheUtil.loadStringKey(null,"update_group_play_log_queue20181106");
            KEY_PLAYLOG_TABLE_ID = CacheUtil.loadStringKey(null,"playlog_table_id20190706");
            KEY_PLAYLOG_TABLE_QUEUE = CacheUtil.loadStringKey(null,"playlog_table_queue20190706");
        } catch (Exception e) {
            REDIS_CONNECTED = false;
            state.set(false);
            KEY_USER_PLAY_LOG_ID = null;
            KEY_USER_PLAY_LOG_QUEUE = null;
            KEY_GROUP_PLAY_LOG_ID = null;
            KEY_GROUP_PLAY_LOG_QUEUE = null;
            KEY_UPDATE_GROUP_PLAY_LOG_ID = null;
            KEY_UPDATE_GROUP_PLAY_LOG_QUEUE = null;
            KEY_PLAYLOG_TABLE_ID = null;
            KEY_PLAYLOG_TABLE_QUEUE = null;
            LogUtil.msgLog.error("Exception:" + e.getMessage(), e);
        }

        if (REDIS_CONNECTED) {
            thread = new Thread() {
                @Override
                public void run() {
                    while (state.get()) {
                        if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("log_queue_into_db"))) {
                            String current = MessageQueueUtil.lMessageQueueOut(KEY_USER_PLAY_LOG_QUEUE);
                            if (current != null) {
                                try {
                                    JSONObject json = JSON.parseObject(current);
                                    UserPlaylog userPlaylog = new UserPlaylog();
                                    userPlaylog = CommonUtil.getObjectFromJson(json, userPlaylog);
                                    TableLogDao.getInstance().saveDb(userPlaylog, true);
                                } catch (Exception e) {
                                    LogUtil.monitorLog.warn("save fail UserPlaylog:" + current);
                                    LogUtil.msgLog.error("Exception:" + e.getMessage(), e);
                                }
                            } else {
                                try {
                                    Thread.sleep(1000);
                                } catch (Exception e) {
                                }
                            }
                        } else {
                            LogUtil.msgLog.warn("check server.properties config:log_queue_into_db=1?");
                            try {
                                Thread.sleep(1000);
                            } catch (Exception e) {
                            }
                        }

                        if("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_log_queue_into_db"))) {
                            String current = MessageQueueUtil.lMessageQueueOut(KEY_GROUP_PLAY_LOG_QUEUE);
                            if (current != null) {
                                try {
                                    JSONObject json = JSON.parseObject(current);
                                    UserGroupPlaylog userGroupPlaylog = new UserGroupPlaylog();
                                    userGroupPlaylog = CommonUtil.getObjectFromJson(json, userGroupPlaylog);
                                    if("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_log_store"))){  //使用日志库
                                        TableLogDao.getInstance().saveGroupPlayLogDbAh(userGroupPlaylog, true);
                                    }else{  //登陆库
                                        TableLogDao.getInstance().saveGroupPlayLogDb(userGroupPlaylog, true);
                                    }
                                } catch (Exception e) {
                                    LogUtil.monitorLog.warn("saveGroupPlayLog fail UserGroupPlaylog:" + current);
                                    LogUtil.msgLog.error("Exception:" + e.getMessage(), e);
                                }
                            } else {
                                try {
                                    Thread.sleep(1000);
                                } catch (Exception e) {
                                }
                            }
                        }else{
                            LogUtil.msgLog.warn(("check server.properties config:group_log_queue_into_db=1?"));
                            try{
                                Thread.sleep(1000);
                            }catch (Exception e){
                            }
                        }

                        if("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("update_group_log_queue_into_db"))) {
                            String current = MessageQueueUtil.lMessageQueueOut(KEY_UPDATE_GROUP_PLAY_LOG_QUEUE);
                            if (current != null) {
                                try {
                                    JSONObject json = JSON.parseObject(current);
                                    UserGroupPlaylog userGroupPlaylog = new UserGroupPlaylog();
                                    userGroupPlaylog = CommonUtil.getObjectFromJson(json, userGroupPlaylog);
                                    if("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_log_store"))) {  //使用日志库
                                        TableLogDao.getInstance().updateGroupPlayLogDbAh(userGroupPlaylog, true);
                                    }else{  //登陆库
                                        TableLogDao.getInstance().updateGroupPlayLogDb(userGroupPlaylog, true);
                                    }
                                } catch (Exception e) {
                                    LogUtil.monitorLog.warn("updateGroupPlayLog fail UserGroupPlaylog:" + current);
                                    LogUtil.msgLog.error("Exception:" + e.getMessage(), e);
                                }
                            } else {
                                try {
                                    Thread.sleep(1000);
                                } catch (Exception e) {
                                }
                            }
                        }else{
                            LogUtil.msgLog.warn(("check server.properties config:update_group_log_queue_into_db=1?"));
                            try{
                                Thread.sleep(1000);
                            }catch (Exception e){
                            }
                        }

                        if("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("playlog_table_queue_into_db"))) {
                            String current = MessageQueueUtil.lMessageQueueOut(KEY_PLAYLOG_TABLE_QUEUE);
                            if (current != null) {
                                try {
                                    JSONObject json = JSON.parseObject(current);
                                    UserGroupPlaylog userGroupPlaylog = new UserGroupPlaylog();
                                    userGroupPlaylog = CommonUtil.getObjectFromJson(json, userGroupPlaylog);
                                    if("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_log_store"))){  //使用日志库
                                        TableLogDao.getInstance().saveGroupPlayLogDbAh(userGroupPlaylog, true);
                                    }else{  //登陆库
                                        TableLogDao.getInstance().saveGroupPlayLogDb(userGroupPlaylog, true);
                                    }
                                } catch (Exception e) {
                                    LogUtil.monitorLog.warn("saveGroupPlayLog fail UserGroupPlaylog:" + current);
                                    LogUtil.msgLog.error("Exception:" + e.getMessage(), e);
                                }
                            } else {
                                try {
                                    Thread.sleep(1000);
                                } catch (Exception e) {
                                }
                            }
                        }else{
                            LogUtil.msgLog.warn(("check server.properties config:group_log_queue_into_db=1?"));
                            try{
                                Thread.sleep(1000);
                            }catch (Exception e){
                            }
                        }
                    }
                }
            };
            thread.start();
        } else {
            thread = null;
        }
    }

    public static final boolean canAsyn() {
        return REDIS_CONNECTED && "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("table_log_asyn"));
    }

    public static final boolean canAsynGroup(){
        return REDIS_CONNECTED && "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_log_asyn"));
    }

    public static final boolean updateAaynGroup(){
        return REDIS_CONNECTED && "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("update_group_log_asyn"));
    }

    public static final Long loadUserPlayLogId(UserPlaylog info) throws Exception {
        Long id = RedisUtil.incr(KEY_USER_PLAY_LOG_ID);
        if (id != null) {
            info.setId(id);
            MessageQueueUtil.rMessageQueueIn(KEY_USER_PLAY_LOG_QUEUE, JSON.toJSONString(info));
            return id;
        }
        return null;
    }

    public static final Long loadGroupPlayLogId(UserGroupPlaylog info){
        Long id = RedisUtil.incr(KEY_GROUP_PLAY_LOG_ID);
        if(id != null){
            info.setId(id);
            MessageQueueUtil.rMessageQueueIn(KEY_GROUP_PLAY_LOG_QUEUE, JSON.toJSONString(info));
            return id;
        }
        return null;
    }

    public static final Long loadUpdateGroupPlayLogId(UserGroupPlaylog info){
        Long id = RedisUtil.incr(KEY_UPDATE_GROUP_PLAY_LOG_ID);
        if(id != null){
          //  info.setId(id);
            MessageQueueUtil.rMessageQueueIn(KEY_UPDATE_GROUP_PLAY_LOG_QUEUE, JSON.toJSONString(info));
            return id;
        }
        return null;
    }

    public static final void destroy() {
        state.set(false);
        if (thread != null && thread.isAlive() && !thread.isInterrupted()) {
            thread.interrupt();
        }
    }

    public static final Long loadPlayLogTableId(PlayLogTable info){
        Long id = RedisUtil.incr(KEY_PLAYLOG_TABLE_ID);
        if(id != null){
            info.setKeyId(id);
            MessageQueueUtil.rMessageQueueIn(KEY_PLAYLOG_TABLE_QUEUE, JSON.toJSONString(info));
            return id;
        }
        return null;
    }
}
