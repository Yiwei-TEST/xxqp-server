package com.sy599.game.common.executor.task;

import com.sy.mainland.util.CommonUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.common.datasource.DruidDataSourceFactory;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.db.dao.DataStatisticsDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.WebSocketManager;
import com.sy599.game.websocket.netty.NettyUtil;

import java.util.concurrent.ThreadPoolExecutor;

public class ServerConfigTask implements Runnable {
    @Override
    public void run() {

        try {
            int count = WebSocketManager.webSocketMap.size();

            DataStatisticsDao.getInstance().saveOrUpdateOnlineData(CommonUtil.dateTimeToString("yyyyMMddHHmm"),"all"+GameServerConfig.SERVER_ID,count);

            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor)TaskExecutor.EXECUTOR_SERVICE;

            StringBuilder sb = new StringBuilder("ServerConfigTask");
            sb.append("|").append(GameServerConfig.SERVER_ID);
            sb.append("|").append(count);
            sb.append("|").append(TableManager.getInstance().getTableCount());
            sb.append("|").append(PlayerManager.getInstance().getPlayerCount());
            sb.append("|").append(NettyUtil.channelUserMap.size());
            sb.append("|").append(threadPoolExecutor.getActiveCount());
            sb.append("|").append(threadPoolExecutor.getCompletedTaskCount());
            sb.append("|").append(threadPoolExecutor.getPoolSize());
            sb.append("|").append(threadPoolExecutor.getTaskCount());
            sb.append("|").append(threadPoolExecutor.getQueue().size());
            LogUtil.monitorLog.info(sb.toString());

            DruidDataSourceFactory.msg();

        }catch (Throwable t){
            LogUtil.errorLog.error("Throwable:"+t.getMessage(),t);
        }
    }
}
