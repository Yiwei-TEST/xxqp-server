package com.sy599.game.common.executor.task;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy599.game.GameServerConfig;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.SystemBlack;
import com.sy599.game.db.dao.PdkRateConfigDao;
import com.sy599.game.character.Player;
import com.sy599.game.common.datasource.DataSourceManager;
import com.sy599.game.gcommand.com.RankCommand;
import com.sy599.game.gcommand.group.GroupTableConfigCommand;
import com.sy599.game.gcommand.group.GroupTableListCommand;
import com.sy599.game.gcommand.group.GroupTableListNewCommand;
import com.sy599.game.jjs.util.JjsUtil;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.staticdata.StaticDataManager;
import com.sy599.game.util.*;
import com.sy599.game.websocket.MyWebSocket;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created by pc on 2017/5/19.
 */
public class TenMinuteFixedRateTask extends TimerTask {

    private static List<SystemBlack> blackList = null;

    static {
        SqlMapClient sqlMapClient = DataSourceManager.getLoginSqlMapClient();
        if (sqlMapClient != null) {
            try {
                blackList = (List<SystemBlack>) sqlMapClient.queryForList("black.selectSystemBlack");
                LogUtil.monitorLog.info("blackList:" + (blackList == null ? "" : JacksonUtil.writeValueAsString(blackList)));
            } catch (Exception e) {
                LogUtil.errorLog.info("Exception:" + e.getMessage(), e);
            }
        }
    }

    public static final Date loadFirstExecuteDate() {
        Calendar calendar = Calendar.getInstance();
        int m = calendar.get(Calendar.MINUTE);
        if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("match_task_check"+GameServerConfig.SERVER_ID))){
            calendar.set(Calendar.SECOND,1);
            calendar.set(Calendar.MILLISECOND,0);
        }
        calendar.add(Calendar.MINUTE, 10 - (m % 10));

        return calendar.getTime();
    }

    @Override
    public void run() {
        SqlMapClient sqlMapClient = DataSourceManager.getLoginSqlMapClient();

        if (sqlMapClient != null) {
            try {
                String openGameTypes0 = ResourcesConfigsUtil.loadServerPropertyValue("openGameTypes");
                String shareFreeGames0 = ResourcesConfigsUtil.loadServerPropertyValue("share_free_games");
                Map<String, String> map0 = ResourcesConfigsUtil.loadStringValues("GameOrGoldConfig");



                String openGameTypes = ResourcesConfigsUtil.loadServerPropertyValue("openGameTypes");
                String shareFreeGames = ResourcesConfigsUtil.loadServerPropertyValue("share_free_games");
                Map<String, String> map = ResourcesConfigsUtil.loadStringValues("GameOrGoldConfig");

                if (!StringUtils.equals(openGameTypes0, openGameTypes)) {
                    GameConfigUtil.loadOpenGameTypes();
                }
                if (!StringUtils.equals(shareFreeGames0, shareFreeGames)) {
                    GameConfigUtil.loadShareFreeGames();
                }
                if (map != null && map.size() > 0) {
                    if (map0 == null || map0.size() == 0) {
                        GameConfigUtil.loadGameConfigsFromDb();
                    } else {
                        boolean reload = false;
                        if (map.size() != map0.size()) {
                            reload = true;
                        } else {
                            for (Map.Entry<String, String> kv : map.entrySet()) {
                                if (!kv.getValue().equals(map0.get(kv.getKey()))) {
                                    reload = true;
                                    break;
                                }
                            }
                        }
                        if (reload) {
                            GameConfigUtil.loadGameConfigsFromDb();
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.errorLog.info("Exception:" + e.getMessage(), e);
            }

            try{
                StaticDataManager.loadConfigs();
            }catch (Exception e){
                LogUtil.errorLog.info("Exception:" + e.getMessage(), e);
            }

            try{
                if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("match_task_check"+GameServerConfig.SERVER_ID))){
                    JjsUtil.startMatchAtFixedRate();
                }
            }catch (Exception e){
                LogUtil.errorLog.info("Exception:" + e.getMessage(), e);
            }

            try {
                blackList = (List<SystemBlack>) sqlMapClient.queryForList("black.selectSystemBlack");
                LogUtil.monitorLog.info("blackList:" + (blackList == null ? "" : JacksonUtil.writeValueAsString(blackList)));
                checkBlack(null);
            } catch (Exception e) {
                LogUtil.errorLog.info("Exception:" + e.getMessage(), e);
            }

            try {
				PdkRateConfigDao.getInstance().initAllPdkConfig();
			} catch (Exception e) {
                LogUtil.errorLog.info("Exception:" + e.getMessage(), e);
			}
        }

        try{
            GroupTableConfigCommand.clearCacheData();
        }catch (Exception e){
            LogUtil.errorLog.info("Exception:" + e.getMessage(), e);
        }

        try{
            GroupTableListCommand.clearCacheData();
        }catch (Exception e){
            LogUtil.errorLog.info("Exception:" + e.getMessage(), e);
        }

        try{
            GroupTableListNewCommand.clearCacheData();
        }catch (Exception e){
            LogUtil.errorLog.info("Exception:" + e.getMessage(), e);
        }

        RankCommand.refreshRank();
    }

    /**
     * 黑名单检查
     * @param tempPlayer
     * @return
     */
    public static boolean checkBlack(Player tempPlayer) {
        try {
            if (blackList != null && blackList.size() > 0) {
                for (SystemBlack systemBlack : blackList) {
                    String flatStr = systemBlack.getFlatId();
                    if (StringUtils.isNotBlank(flatStr)) {

                        if (tempPlayer == null) {
                            Iterator<Map.Entry<Long, Player>> it = PlayerManager.playerMap.entrySet().iterator();

                            while (it.hasNext()) {
                                Map.Entry<Long, Player> kv = it.next();
                                Player player = kv.getValue();
                                if (player != null && flatStr.equalsIgnoreCase(player.getPf() + "_" + player.getFlatId())) {
                                    MyWebSocket myWebSocket = player.getMyWebSocket();
                                    if (myWebSocket != null) {
                                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_249));
                                        myWebSocket.close();

                                        it.remove();
                                        PlayerManager.getInstance().removePlayer(player);
                                    }
                                }
                            }
                        } else {
                            if (flatStr.equalsIgnoreCase(tempPlayer.getPf() + "_" + tempPlayer.getFlatId())) {
                                return false;
                            }
                        }

                    }
                }
            }

        } catch (Exception e) {
            LogUtil.errorLog.info("Exception:" + e.getMessage(), e);
        }
        return true;
    }
}
