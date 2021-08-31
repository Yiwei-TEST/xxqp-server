package com.sy599.game.websocket;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.character.Player;
import com.sy599.game.gcommand.login.util.LoginDataUtil;
import com.sy599.game.msg.serverPacket.ChatResMsg.ChatRes;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.NettyUtil;
import io.netty.channel.ChannelHandlerContext;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketManager {
	private static WebSocketManager _inst = new WebSocketManager();

	public static WebSocketManager getInstance() {
		return _inst;
	}

	/**
	 * websocketMap集合<userID,websocket> 以前是用websocket的hashcode作key,修改为userId.
	 */
	public static final Map<Long, MyWebSocket> webSocketMap = new ConcurrentHashMap<>();

	/**
	 * 新增websocket连接
	 * 
	 * @param webSocket
	 */
    public static void addWebSocket(MyWebSocket webSocket) {
        Player player = webSocket.getPlayer();
        if (player != null) {
            MyWebSocket oldSocket = webSocketMap.get(player.getUserId());
            if (oldSocket != null && webSocket.getCtx() != oldSocket.getCtx()) {
                // 账号冲突
                oldSocket.setLoginError(true);
                oldSocket.accountConflict(player);
                oldSocket.close();
                LogUtil.monitorLog.error("login|addWebSocket|accountConflict|" + player.getUserId() + "|" + (oldSocket.getCtx() != null ? oldSocket.getCtx().channel().id().toString() : ""));
            }
            if (webSocket.getCtx()!=null){
                webSocketMap.put(player.getUserId(), webSocket);
                NettyUtil.channelUserMap.put(webSocket.getCtx().channel(),player.getUserId());
                LogUtil.monitorLog.info("login|addWebSocket|addNew|" + player.getUserId() + "|" + webSocket.getCtx().channel().id().asShortText());
            }
        }
    }

    /**
     * 断开websocket连接
     *
     * @param player
     */
	public static void removeWebSocket(Player player) {
		if (player != null){
			MyWebSocket myWebSocket = webSocketMap.remove(player.getUserId());
			if (myWebSocket!=null){
				ChannelHandlerContext ctx = myWebSocket.getCtx();
				if (ctx!=null) {
					NettyUtil.channelUserMap.remove(ctx.channel());
				}
				if (myWebSocket.isLoginSuccess()) {
					LoginDataUtil.logOutData(String.valueOf(player.getUserId()), myWebSocket.getDateTime(), new Date());
				}
			}
		}
	}

	public static void broadMsg(int type, String content) {
		for (MyWebSocket socket : webSocketMap.values()) {
			socket.send(type, content);
		}
	}

	public static void broadMsg(int type, ChatRes.Builder chatMsg, Player toTalkPlayer) {
		chatMsg.setCode(type);
		GeneratedMessage message = chatMsg.build();
		toTalkPlayer.getMyWebSocket().send(message);
	}

	public static void broadMsg(int type, ChatRes.Builder chatMsg) {
		chatMsg.setCode(type);
		GeneratedMessage message = chatMsg.build();
		for (MyWebSocket socket : webSocketMap.values()) {
			socket.send(message);
		}
	}

	public static void broadMsg(GeneratedMessage message) {
		for (MyWebSocket socket : webSocketMap.values()) {
			socket.send(message);
		}
	}

	public static void shudown(String msg) {
		ComRes res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_shutdown, msg).build();
		for (MyWebSocket socket : webSocketMap.values()) {
			socket.send(res);
		}
	}

}
