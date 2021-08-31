package com.sy599.game.websocket.netty;

import com.sy599.game.GameServerConfig;
import com.sy599.game.assistant.AssisServlet;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.action.ActionServlet;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.gcommand.GeneralCommand;
import com.sy599.game.gcommand.login.LoginCommand;
import com.sy599.game.gcommand.login.UnionLoginCommand;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.webservice.GoldActivityServlet;
import com.sy599.game.webservice.GoldRoomMatchServlet;
import com.sy599.game.webservice.GroupServlet;
import com.sy599.game.webservice.OnlineNoticeServlet;
import com.sy599.game.websocket.MyWebSocket;
import com.sy599.game.websocket.WebSocketManager;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageCoder;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.websocket.netty.coder.NettyHttpServletRequest;
import com.sy599.game.websocket.netty.coder.NettyHttpServletResponse;
import com.sy599.game.websocket.netty.handshaker.WebSocketServerHandshaker13;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lz
 */
public class WebSocketServerHandler extends
        SimpleChannelInboundHandler<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger("msg");

//    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private final static Map<Channel, MessageUnit> channelMessageUnitMap = new ConcurrentHashMap<>();

    public static final AtomicInteger socketCounter = new AtomicInteger();

    private WebSocketServerHandshaker handshaker;

    public static volatile boolean isOpen = false;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (isOpen) {
            if ((msg instanceof WebSocketFrame)) {
                handleWebSocketFrame(ctx, msg);
            } else if ((msg instanceof FullHttpRequest)) {
                handleHttpFullRequest(ctx, msg);
            } else if ((msg instanceof ByteBuf)) {
                handleSocketRequest(ctx, msg);
            }
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {  // (2)
//        Channel incoming = ctx.channel();
//        for (Channel channel : channels) {
//            channel.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " 加入"));
//        }
//        channels.add(ctx.channel());
//        System.out.println("Client:" + incoming.remoteAddress() + "加入:total=" + channels.size());
//        if (PropertiesFileLoader.isWindows())
//            System.out.println("channel total=" + NettyUtil.channelUserMap.size() + ",user total=" + PlayerManager.getInstance().getPlayerCount() + ",channel=" + ctx.channel()+ ",currentThread=" + Thread.currentThread().getName());

    }


    private void exit(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        channelMessageUnitMap.remove(channel);
        String channelId = channel.id().asShortText();
        NettyUtil.PACKAGE_MAP.remove(channelId);
        NettyUtil.SSL_MAP.remove(channelId);
        String ip = NettyUtil.userIpMap.remove(channel);
        Long userId = NettyUtil.channelUserMap.remove(channel);
        LogUtil.monitorLog.info("login|WebSocketServerHandler|exit|1|" + userId + "|" + ctx.channel().id().asShortText() + "|" + ip + "|" + Thread.currentThread().getName());
        if (userId != null) {
            Player player;
            MyWebSocket myWebSocket = WebSocketManager.webSocketMap.get(userId);
            if (myWebSocket != null) {
                player = myWebSocket.getPlayer();
            }else{
                player = null;
            }

            if (player == null){
                player = PlayerManager.getInstance().getPlayer(userId);
            }

            if (player != null){
                if (myWebSocket == null){
                    myWebSocket = player.getMyWebSocket();
                }
                if (myWebSocket == null || myWebSocket.isLoginError()) {
                    LogUtil.monitorLog.info("login|WebSocketServerHandler|exit|2|" + player.getUserId()+ "|" + player.getPlayingTableId() + "|" + ctx.channel().id().asShortText() + "|" + ip + "|" + Thread.currentThread().getName());
                } else {
                    LogUtil.monitorLog.info("login|WebSocketServerHandler|exit|3|" + player.getUserId() + "|" + player.getPlayingTableId()+ "|" + ctx.channel().id().asShortText() + "|" + ip + "|" + Thread.currentThread().getName());
                }
                player.exit(channelId);
            }
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {  // (3)
//        Channel incoming = ctx.channel();
//        for (Channel channel : channels) {
//            channel.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " 离开"));
//        }
//        channels.remove(ctx.channel());
//        System.out.println("Client:" + incoming.remoteAddress() + "离开:total=" + channels.size());
        LogUtil.monitorLog.info("login|WebSocketServerHandler|handlerRemoved|" + ctx.channel().id().asShortText() + "|" + NettyUtil.channelUserMap.get(ctx.channel()) + "|" + Thread.currentThread().getName());
        exit(ctx);
//        if (PropertiesFileLoader.isWindows())
//            System.out.println("channel total=" + NettyUtil.channelUserMap.size() + ",user total=" + PlayerManager.getInstance().getPlayerCount() + ",channel=" + ctx.channel()+ ",currentThread=" + Thread.currentThread().getName());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception { // (5)
//        Channel incoming = ctx.channel();
//        System.out.println("Client:" + incoming.remoteAddress() + "在线:total=" + channels.size());
//        System.out.println("channel total=" + channelUserMap.size() + ",user total=" + PlayerManager.getInstance().getPlayerCount());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception { // (6)
//        Channel incoming = ctx.channel();
//        System.out.println("Client:" + incoming.remoteAddress() + "掉线:total=" + channels.size());
//        System.out.println("channel total=" + channelUserMap.size() + ",user total=" + PlayerManager.getInstance().getPlayerCount());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        Channel incoming = ctx.channel();
//
//        Connections.remove(ctx);
//
//        channelMessageUnitMap.remove(incoming);
//        System.out.println("Client:" + incoming.remoteAddress() + "异常:total=" + Connections.size());

//
//        LogUtil.msgLog.info("login|WebSocketServerHandler|exceptionCaught|" + ctx.channel().id().asShortText() + "|" + "|" + Thread.currentThread().getName() + "|" + cause, cause);

        StringBuilder sb = new StringBuilder("WebSocketServerHandler|error|");
        sb.append("|").append(ctx.channel().id().asShortText());
        sb.append("|").append(NettyUtil.getRemoteAddr(ctx));
        sb.append("|").append(NettyUtil.channelUserMap.get(ctx.channel()));
        sb.append("|").append(Thread.currentThread().getName());
        sb.append("|").append(cause.getMessage());
        LogUtil.errorLog.error(sb.toString());

        exit(ctx);
        //当出现异常就关闭连接
//        if (PropertiesFileLoader.isWindows())
//            System.out.println("channel total=" + NettyUtil.channelUserMap.size() + ",user total=" + PlayerManager.getInstance().getPlayerCount() + ",channel=" + ctx.channel()+ ",currentThread=" + Thread.currentThread().getName());
//        LOGGER.error("error msg:" + cause.getMessage(), cause);

        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        ctx.flush();
    }

    private void handleSocketRequest(ChannelHandlerContext ctx, Object msg) {
//        LOGGER.info("handleSocketRequest:" + msg.toString());
        serviceHandle(ctx, (ByteBuf) msg, 0);
    }

    private void handleHttpFullRequest(ChannelHandlerContext ctx, Object message) {
        FullHttpRequest request = (FullHttpRequest) message;
        String uri = request.uri();
        HttpMethod method = request.method();
        boolean useSsl = SslUtil.hasSslHandler(ctx);
        boolean isWebSocket = false;
        String ip = NettyUtil.loadRequestIp(ctx, request);
        String channelId = ctx.channel().id().asShortText();
        long start = System.currentTimeMillis();

        StringBuilder sb = new StringBuilder("handleHttpFullRequest");
        sb.append("|").append(channelId);
        sb.append("|").append(ip);
        sb.append("|").append(method);
        sb.append("|").append(useSsl ? 1 : 0);
        sb.append("|").append(Thread.currentThread().getName());

        StringBuilder logMsg;
        try {
            if (!request.decoderResult().isSuccess()) {
                NettyUtil.sendHttpResponse(ctx, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
                logMsg = new StringBuilder(sb.toString());
                logMsg.append("|").append(isWebSocket ? 1 : 0);
                logMsg.append("|").append("end");
                logMsg.append("|").append("decoderResultUnSuccess");
                logMsg.append("|").append((System.currentTimeMillis() - start));
                LogUtil.monitorLog.error(logMsg.toString());
                return;
            }

            if (method == HttpMethod.GET) {
                isWebSocket = isWebSocketUpgrade(request);
                if (isWebSocket) {
                    NettyUtil.PACKAGE_MAP.remove(ctx.channel().id().asShortText());
                    // 正常WebSocket的Http连接请求，构造握手响应返回
                    String webSocketURL = (useSsl ? "wss://" : "ws://") + request.headers().get(HttpHeaderNames.HOST) + ":" + GameServerConfig.SERVER_PORT + (uri.equals("/") ? "" : uri);
                    CharSequence version = request.headers().get(HttpHeaderNames.SEC_WEBSOCKET_VERSION);
                    if (version != null && version.equals(WebSocketVersion.V13.toHttpHeaderValue())) {
                        // Version 13 of the wire protocol - RFC 6455 (version 17 of the draft hybi specification).
                        handshaker = new WebSocketServerHandshaker13(webSocketURL, null, false, 65536);
                    } else {
                        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(webSocketURL, null, false);
                        handshaker = wsFactory.newHandshaker(request);
                    }
                    ChannelFuture future;
                    if (handshaker == null) {
                        // 无法处理的websocket版本
                        future = WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
                        logMsg = new StringBuilder(sb.toString());
                        logMsg.append("|").append(isWebSocket ? 1 : 0);
                        logMsg.append("|").append("end");
                        logMsg.append("|").append("handshaker");
                        logMsg.append("|").append((System.currentTimeMillis() - start));
                        logMsg.append("|").append(version);
                        logMsg.append("|").append(webSocketURL);
                        LogUtil.monitorLog.error(logMsg.toString());
                    } else {
                        // 向客户端发送websocket握手,完成握手
                        NettyUtil.userIpMap.put(ctx.channel(), ip);
                        future = handshaker.handshake(ctx.channel(), request);
                    }
                    boolean isWebSocket1 = isWebSocket;
                    future.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            StringBuilder logMsg = new StringBuilder(sb.toString());
                            logMsg.append("|").append(isWebSocket1 ? 1 : 0);
                            logMsg.append("|").append("end");
                            if (channelFuture.isSuccess()) {
                                logMsg.append("|").append("futureSuccess");
                                logMsg.append("|").append((System.currentTimeMillis() - start));
                                logMsg.append("|").append(version);
                                logMsg.append("|").append(webSocketURL);
                                LogUtil.monitorLog.info(logMsg.toString());
                            } else if (channelFuture.isCancelled()) {
                                logMsg.append("|").append("futureIsCancelled");
                                logMsg.append("|").append((System.currentTimeMillis() - start));
                                logMsg.append("|").append(version);
                                logMsg.append("|").append(webSocketURL);
                                LogUtil.monitorLog.info(logMsg.toString());
                            } else {
                                logMsg.append("|").append("futureUnSuccess");
                                logMsg.append("|").append((System.currentTimeMillis() - start));
                                logMsg.append("|").append(version);
                                logMsg.append("|").append(webSocketURL);
                                LogUtil.monitorLog.error(logMsg.toString(), channelFuture.cause());
                            }
                        }
                    });
                } else {
                    logMsg = new StringBuilder(sb.toString());
                    logMsg.append("|").append(isWebSocket ? 1 : 0);
                    logMsg.append("|").append("end");
                    logMsg.append("|").append("GetHttpHandle");
                    logMsg.append("|").append((System.currentTimeMillis() - start));
                    LogUtil.monitorLog.info(logMsg.toString());
                    httpHandle(ctx, request);
                }
            } else if (method == HttpMethod.POST) {
                logMsg = new StringBuilder(sb.toString());
                logMsg.append("|").append(isWebSocket ? 1 : 0);
                logMsg.append("|").append("end");
                logMsg.append("|").append("PostHttpHandle");
                logMsg.append("|").append((System.currentTimeMillis() - start));
                LogUtil.monitorLog.info(logMsg.toString());
                httpHandle(ctx, request);
            } else {
                logMsg = new StringBuilder(sb.toString());
                logMsg.append("|").append(isWebSocket ? 1 : 0);
                logMsg.append("|").append("end");
                logMsg.append("|").append("other");
                logMsg.append("|").append((System.currentTimeMillis() - start));
                LogUtil.monitorLog.info(logMsg.toString());
                NettyUtil.sendHttpResponse(ctx, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            }
        } catch (Exception e) {
            logMsg = new StringBuilder(sb.toString());
            logMsg.append("|").append(isWebSocket ? 1 : 0);
            logMsg.append("|").append("end");
            logMsg.append("|").append("error");
            logMsg.append("|").append((System.currentTimeMillis() - start));
            logMsg.append("|").append(e.getMessage());
            LogUtil.errorLog.error(logMsg.toString(), e);
        } finally {
            logMsg = new StringBuilder(sb.toString());
            logMsg.append("|").append(isWebSocket ? 1 : 0);
            logMsg.append("|").append("finally");
            logMsg.append("|").append((System.currentTimeMillis() - start));
            LogUtil.monitorLog.info(logMsg.toString());
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, Object message0) {
        try {
            WebSocketFrame msg = (WebSocketFrame) message0;

            Channel incoming = ctx.channel();

            if (msg instanceof TextWebSocketFrame) {
                incoming.writeAndFlush(new TextWebSocketFrame("server is ok^_^[you]:" + ((TextWebSocketFrame) msg).text()));
            } else if (msg instanceof BinaryWebSocketFrame) {
                BinaryWebSocketFrame webSocketFrame = (BinaryWebSocketFrame) msg;
                serviceHandle(ctx, webSocketFrame.content(), 1);

            } else if (msg instanceof PingWebSocketFrame) {
                LOGGER.info("recieved PingWebSocketFrame from channel {}", ctx.channel());
                incoming.writeAndFlush(
                        new PongWebSocketFrame(msg.content().retain()));
            } else if (msg instanceof PongWebSocketFrame) {
                LOGGER.info("recieved PongWebSocketFrame from channel {}", ctx.channel());
//            incoming.writeAndFlush(
//                    new PingWebSocketFrame(msg.content().retain()));
            } else if (msg instanceof CloseWebSocketFrame) {
                LOGGER.info("recieved CloseWebSocketFrame from channel {}", ctx.channel());
                incoming.writeAndFlush(msg.retain()).addListener(
                        ChannelFutureListener.CLOSE_ON_FAILURE);
            } else if (msg instanceof ContinuationWebSocketFrame) {

            }
        } catch (Exception e) {
            LOGGER.error("error msg:" + e.getMessage(), e);
        }
    }

    private static final boolean isWebSocketUpgrade(FullHttpRequest req) {
        HttpHeaders headers = req.headers();
        if (req.method() == HttpMethod.GET) {
            String temp = headers.get(HttpHeaderNames.UPGRADE);
            if (temp != null && temp.toLowerCase().contains("websocket")) {
                temp = headers.get(HttpHeaderNames.CONNECTION);
                if (temp != null && temp.toLowerCase().contains("upgrade")) {
                    return true;
                }
            }
        }
        return false;
    }

    private final static void serviceHandle(ChannelHandlerContext ctx, ByteBuf byteBuf, int channelMark) {
        try {

            byte[] result;
            int length = byteBuf.readableBytes();
            if (byteBuf.hasArray()) {
                //堆栈缓冲区
                byte[] array = byteBuf.array();
                int offset = byteBuf.arrayOffset() + byteBuf.readerIndex();
                result = new byte[length];
                System.arraycopy(array, offset, result, 0, length);
            } else {
                //直接缓冲区
                byte[] array = new byte[length];
                byteBuf.getBytes(byteBuf.readerIndex(), array);
                result = array;
            }
            String channelId = ctx.channel().id().asShortText();

            MessageUnit message = MessageCoder.decode(result, channelMessageUnitMap.get(ctx.channel()));
            if (message == null) {
                LogUtil.errorLog.error("messageNull|" + channelId);
                channelMessageUnitMap.remove(ctx.channel());
                return;
            }

            if (message.complete()) {
                channelMessageUnitMap.remove(ctx.channel());

                long startTime = System.currentTimeMillis();
                Long userId = NettyUtil.channelUserMap.get(ctx.channel());
                Player player;
                if (userId == null) {
                    player = null;
                } else {
                    player = PlayerManager.getInstance().getPlayer(userId);
                }

                try {
                    socketCounter.addAndGet(1);
                    //如果是登陆的消息
                    if (message.getMsgType() == WebSocketMsgType.cs_login) {
                        LoginCommand acton = new LoginCommand();
                        MyWebSocket myWebSocket = new MyWebSocket();
                        myWebSocket.setChannelMark(channelMark);
                        myWebSocket.stCtx(ctx);
                        acton.setCtx(ctx);

                        player = acton.login(message, myWebSocket);
                        // 没有登录成功
                        if (player == null) {
                            StringBuilder sb = new StringBuilder("loginError");
                            sb.append("|").append(channelId);
                            sb.append("|").append(acton.getLoginUserId());
                            LogUtil.errorLog.error(sb.toString());
                            ctx.close();
                            return;
                        }
                    } else if (message.getMsgType() == WebSocketMsgType.union_login) {
                        UnionLoginCommand acton = new UnionLoginCommand();
                        MyWebSocket myWebSocket = new MyWebSocket();
                        myWebSocket.setChannelMark(channelMark);
                        myWebSocket.stCtx(ctx);
                        acton.setCtx(ctx);

                        int mark = acton.login(message, myWebSocket);
                        // 没有登录成功
                        if (mark != 1) {
                            ctx.close();
                        }
                        return;
                    } else {
                        if(player == null){
                            LogUtil.monitorLog.info("serviceHandle|error|playerIsNull|" + userId + "|" + ctx.channel().id().asShortText());
                            ctx.close();
                            return;
                        }
                        Object obj = GeneralCommand.getInstance().isGMessage(message, player);
                        boolean ret;
                        if (obj instanceof Boolean) {
                            ret = (Boolean) obj;

                            if (player == null && !ret) {
                                ctx.close();
                                return;
                            }
                        } else {
                            if (player == null) {
                                ctx.close();
                                return;
                            }

                            ret = (Boolean) (((Object[]) obj)[0]);
                            player = (Player) (((Object[]) obj)[1]);
                        }

                        if (player != null) {
                            if (ret) {
                                // 执行了通用消息，游戏之前的C房，加房等走此处
                                GeneralCommand.getInstance().execute(ctx, player, message);
                            } else {
                                //所有玩家准备完毕后 游戏开始，游戏通讯走此处
                                PlayerManager.getInstance().process(player, message);
                            }
                        }
                    }
                    if (player != null) {
                        player.changeActionCount(1);
                        player.setSyncTime(new Date());
                    }
                    long timeUse = System.currentTimeMillis() - startTime;
                    if (timeUse > 50) {
                        StringBuilder sb = new StringBuilder("xnlog|socket");
                        sb.append("|").append(timeUse);
                        sb.append("|").append(startTime);
                        if (player != null) {
                            sb.append("|").append(player.getUserId());
                            BaseTable playingTable = player.getPlayingTable();
                            if (playingTable != null) {
                                sb.append("|").append(playingTable.getId());
                                sb.append("|").append(playingTable.getPlayType());
                            }
                        } else {
                            sb.append("|").append(0);
                            sb.append("|").append(0);
                            sb.append("|").append(0);
                        }
                        sb.append("|").append(message.getMsgType());
                        sb.append("|").append(LogUtil.printlnLog(message.getMessage()));
                        LogUtil.monitorLog.info(sb.toString());
                    }

                } catch (Throwable e) {
                    StringBuilder sb = new StringBuilder("onWebSocketBinary|error");
                    sb.append("|").append(channelId);
                    if (player != null) {
                        sb.append("|").append(player.getUserId());
                        sb.append("|").append(player.getPlayingTableId());
                    }
                    if (message != null) {
                        sb.append("|").append(message.getMsgType());
                        sb.append("|").append(LogUtil.printlnLog(message.getMessage()));
                    }
                    sb.append("|").append(e.getMessage());
                    LogUtil.errorLog.error(sb.toString(), e);
                    if (player != null) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_16));
                    } else {
                        ctx.close();
                    }
                } finally {
                    socketCounter.addAndGet(-1);
                }
            } else {
                channelMessageUnitMap.put(ctx.channel(), message);

                StringBuilder sb = new StringBuilder("receiveMsg");
                sb.append("|").append(channelId);
                sb.append("|").append(message.currentLength());
                sb.append("|").append(message.getLength());
                sb.append("|").append(message.simple());
                LogUtil.monitorLog.info(sb.toString());
            }
        } catch (Exception e) {
            channelMessageUnitMap.remove(ctx.channel());
            LogUtil.errorLog.error("decode fail:" + e.getMessage(), e);
        } finally {
            byteBuf.clear();
        }
    }

    private static final void httpHandle(ChannelHandlerContext ctx, FullHttpRequest request) {
        try {
            String uri = request.uri();
            HttpServlet servlet;
            if (uri.startsWith("/qipai/pdk.do")) {
                servlet = new ActionServlet();
            } else if (uri.startsWith("/online/notice.do")) {
                servlet = new OnlineNoticeServlet();
            } else if (uri.startsWith("/assistant/pdk.do")) {
                servlet = new AssisServlet();
            } else if (uri.startsWith("/group/msg.do")) {
                servlet = new GroupServlet();
            } else if (uri.startsWith("/goldRoom/match.do")) {
                servlet = new GoldRoomMatchServlet();
            } else if(uri.startsWith("/goldActivity/msg.do")){
                servlet = new GoldActivityServlet();
            }else {
                if (uri.startsWith("/favicon.ico") || "/".equals(uri)) {
                    NettyUtil.sendHttpResponse(ctx, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
                } else {
                    LOGGER.warn("uri not exists:uri=" + uri);
                    NettyUtil.sendHttpResponse(ctx, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND));
                }
                return;
            }
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            HttpServletRequest req = new NettyHttpServletRequest(ctx, request);
            HttpServletResponse res = new NettyHttpServletResponse(ctx, response);
            try {
                servlet.service(req, res);
            } catch (Throwable t) {
                LOGGER.warn("uri exception:uri=" + uri + ",msg=" + t.getMessage(), t);
            } finally {
                if (!((NettyHttpServletResponse) res).isFlush()) {
                    res.getWriter().flush();
                    res.getWriter().close();
                }
            }
        } catch (Exception e) {
            LOGGER.error("httpHandle Exception:" + e.getMessage(), e);
        }
    }

}
