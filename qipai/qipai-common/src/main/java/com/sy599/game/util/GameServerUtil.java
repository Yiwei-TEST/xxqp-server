package com.sy599.game.util;

import com.sy599.game.db.bean.Server;
import com.sy599.game.websocket.MyWebSocket;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.SslUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public final class GameServerUtil {

    public static boolean sendChangeServerCommand(MyWebSocket socket, long totalCount, Server server) {
        if (socket == null || server == null) {
            return false;
        }
        Map<String, Object> result = new HashMap<>();

        Server server2 = server;
        String[] gameUrls = CheckNetUtil.loadGameUrl(server.getId(), totalCount);
        if (gameUrls != null) {
            server2 = new Server();
            server2.setId(server.getId());
            if (gameUrls[0].startsWith("ws:")) {
                server2.setChathost(gameUrls[0]);
            } else if (gameUrls[0].startsWith("wss:")) {
                server2.setWssUri(gameUrls[0]);
            }
        }
        Map<String, Object> serverMap = new HashMap<>();
        serverMap.put("serverId", server2.getId());

        boolean useSsl = SslUtil.hasSslHandler(socket.getCtx());

        if (gameUrls == null) {
            serverMap.put("connectHost", useSsl ? server2.getWssUri() : server2.getChathost());
            serverMap.put("connectHost1", "");
            serverMap.put("connectHost2", "");
        } else {
            String url0;
            if (useSsl) {
                url0 = (StringUtils.isNotBlank(gameUrls[0]) && gameUrls[0].startsWith("wss:")) ? gameUrls[0] : server2.getWssUri();
            } else {
                url0 = (StringUtils.isNotBlank(gameUrls[0]) && gameUrls[0].startsWith("ws:")) ? gameUrls[0] : server2.getChathost();
            }

            serverMap.put("connectHost", url0);
            serverMap.put("connectHost1", gameUrls[1]);
            serverMap.put("connectHost2", gameUrls[2]);
        }

        result.put("server", serverMap);
        result.put("blockIconTime", 0);
        result.put("code", 0);
        result.put("tId", 0);

        socket.sendComMessage(WebSocketMsgType.res_code_getserverid, JacksonUtil.writeValueAsString(result));
        return true;
    }
}
