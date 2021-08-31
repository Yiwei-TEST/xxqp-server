package com.sy599.game.manager;

import com.sy599.game.GameServerConfig;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.ServerConfig;
import com.sy599.game.db.dao.ServerDao;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ServerManager {
    private static Map<Integer, Server> serverMap = new ConcurrentHashMap<>();

    public static int init() {
        List<ServerConfig> list = ServerDao.getInstance().queryAllServer();
        Map<Integer, Server> serverMap = new ConcurrentHashMap<>();
        if (list != null) {
            for (ServerConfig serverConfig : list) {
                Server server = changeEntity(serverConfig);
                serverMap.put(server.getId(), server);
            }
        }

        ServerManager.serverMap = serverMap;
        return serverMap.size();
    }

    public static Map<Integer, Server> loadAllServers() {
        return serverMap;
    }

    public static String loadRootUrl(Server server) {
        if (server == null) {
            return null;
        } else {
            String url = server.getIntranet();
            if (StringUtils.isBlank(url)) {
                url = server.getHost();
            }

            if (StringUtils.isNotBlank(url)) {
                int idx = url.indexOf(".");
                if (idx > 0) {
                    idx = url.indexOf("/", idx);
                    if (idx > 0) {
                        url = url.substring(0, idx);
                    }
                }
                return url;
            }
        }
        return null;
    }

    public static String loadRootUrl(int serverId) {
        return loadRootUrl(loadServer(serverId));
    }

    public static Server loadServer(int serverId) {
        return serverMap.get(serverId);
    }

    public static Server loadServer(int type, int serverType) {
        return loadServer(type, serverType, false);
    }

    public static Server loadServer(int type, int serverType, boolean isMatch) {
        Server tempServer = null;
        int tempOnlineCount = Integer.MAX_VALUE;

        if (isMatch) {
            for (Map.Entry<Integer, Server> kv : serverMap.entrySet()) {
                if (kv.getValue().getServerType() == serverType && kv.getValue().getMatchType().contains(type)) {
                    if (kv.getValue().getOnlineCount() == 0) {
                        tempServer = kv.getValue();
                        break;
                    } else if (kv.getValue().getOnlineCount() < tempOnlineCount) {
                        tempOnlineCount = kv.getValue().getOnlineCount();
                        tempServer = kv.getValue();
                    }
                }
            }
        } else {
            for (Map.Entry<Integer, Server> kv : serverMap.entrySet()) {
                if (kv.getValue().getServerType() == serverType && kv.getValue().getGameType().contains(type)) {
                    if (kv.getValue().getOnlineCount() == 0) {
                        tempServer = kv.getValue();
                        break;
                    } else if (kv.getValue().getOnlineCount() < tempOnlineCount) {
                        tempOnlineCount = kv.getValue().getOnlineCount();
                        tempServer = kv.getValue();
                    }
                }
            }
        }

        if (tempServer == null) {
            for (Map.Entry<Integer, Server> kv : serverMap.entrySet()) {
                if (kv.getValue().getServerType() == serverType) {
                    if (kv.getValue().getOnlineCount() == 0) {
                        tempServer = kv.getValue();
                        break;
                    } else if (kv.getValue().getOnlineCount() < tempOnlineCount) {
                        tempOnlineCount = kv.getValue().getOnlineCount();
                        tempServer = kv.getValue();
                    }
                }
            }
        }

        if (tempServer != null) {
            synchronized (tempServer) {
                tempServer.setOnlineCount(tempServer.getOnlineCount() + 1);
            }
        }

        return tempServer;
    }


    public static Server loadServer(String pf, int serverType) {
        Server tempServer = null;
        int tempOnlineCount = Integer.MAX_VALUE;
        for (Map.Entry<Integer, Server> kv : serverMap.entrySet()) {
            if (kv.getValue().getServerType() == serverType && StringUtils.contains(kv.getValue().getExtend(), "|" + pf + "|")) {
                if (kv.getValue().getOnlineCount() == 0) {
                    tempServer = kv.getValue();
                    break;
                } else if (kv.getValue().getOnlineCount() < tempOnlineCount) {
                    tempOnlineCount = kv.getValue().getOnlineCount();
                    tempServer = kv.getValue();
                }
            }
        }
        if (tempServer == null) {
            for (Map.Entry<Integer, Server> kv : serverMap.entrySet()) {
                if (kv.getValue().getServerType() == serverType) {
                    if (kv.getValue().getOnlineCount() == 0) {
                        tempServer = kv.getValue();
                        break;
                    } else if (kv.getValue().getOnlineCount() < tempOnlineCount) {
                        tempOnlineCount = kv.getValue().getOnlineCount();
                        tempServer = kv.getValue();
                    }
                }
            }
        }
        if (tempServer != null) {
            synchronized (tempServer) {
                tempServer.setOnlineCount(tempServer.getOnlineCount() + 1);
            }
        }

        return tempServer;
    }


    private static Server changeEntity(ServerConfig serverConfig) {
        Server server = new Server();
        server.setServerType(serverConfig.getServerType());
        server.setChathost(serverConfig.getChathost());
        server.setGameType(str2IntList(serverConfig.getGameType()));
        server.setId(serverConfig.getId());
        server.setMatchType(str2IntList(serverConfig.getMatchType()));
        server.setOnlineCount(serverConfig.getOnlineCount());
        server.setExtend(serverConfig.getExtend());
        server.setHost(serverConfig.getHost());
        server.setIntranet(serverConfig.getIntranet());
        server.setName(serverConfig.getName());
        server.setHttpsUri(serverConfig.getHttpsUri());
        server.setWssUri(serverConfig.getWssUri());
        server.setTmpGameType(str2IntList(serverConfig.getTmpGameType()));
        return server;
    }


    private static List<Integer> str2IntList(String str) {
        if (StringUtils.isBlank(str)) {
            return new ArrayList<>();
        } else {
            List<Integer> list = new ArrayList<>();
            String[] strs = str.split(",");
            for (String temp : strs) {
                if (NumberUtils.isDigits(temp)) {
                    list.add(Integer.valueOf(temp));
                }
            }
            return list;
        }
    }

    /**
     * 是否存在这个玩法
     *
     * @param playType
     * @return
     */
    public static boolean hasPlayType(int playType) {
        for (Map.Entry<Integer, Server> kv : serverMap.entrySet()) {
            if (kv.getValue().getServerType() == 1 && kv.getValue().getGameType().contains(playType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 指定serverId的服务器，是否存在这个玩法
     * 注意：该方法不可以用于随机为玩法分配服务器
     *
     * @param serverId
     * @param playType
     * @return
     */
    public static boolean hasPlayType(int serverId, int playType) {
        Server server = ServerManager.loadServer(serverId);
        if (server == null || (!server.getGameType().contains(playType) && !server.getTmpGameType().contains(playType))) {
            return false;
        }
        return true;
    }

    public static boolean isValid(Server server, int serverType, int playType) {
        if (server == null) {
            return false;
        }
        if (server.getServerType() != serverType) {
            return false;
        }
        if (server.getGameType() == null || !server.getGameType().contains(playType)) {
            return false;
        }
        return true;
    }

    /**
     * 是否是金币场服
     *
     * @return
     */
    public static boolean isGoldRoomServer() {
        Server server = loadServer(GameServerConfig.SERVER_ID);
        return server != null && server.getServerType() == Server.SERVER_TYPE_GOLD_ROOM;
    }

}
