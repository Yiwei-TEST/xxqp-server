package handler;

import com.sy.mainland.util.PropertiesFileLoader;
import com.sy599.game.manager.MsgManager;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.MessageTransmits;
import com.sy599.game.websocket.MyWebSocket;
import com.sy599.game.websocket.netty.HeartBeatServerHandler;
import com.sy599.game.websocket.netty.HttpRouteHandler;
import com.sy599.game.websocket.netty.SslRouteHandler;
import com.sy599.game.websocket.netty.WebSocketServerHandler;
import com.sy599.game.websocket.netty.coder.MessageCoder;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Guang.OuYang
 * @date 2019/9/5-16:11
 */
public class AbstractHandler extends ChannelInitializer<SocketChannel> {

    private static final LongAdder MSG_COUNT = new LongAdder();

    private static MyWebSocket myWebSocket = new MyWebSocket();

    ChannelHandlerContext ctx;

    private LinkedBlockingQueue<MessageTransmits.Message> msgQueue = new LinkedBlockingQueue<>();

    private AtomicBoolean state = new AtomicBoolean(true);

    private int channelMark = 1;

    /**
     * 消息数+1
     */
    public static void add() {
        MSG_COUNT.add(1L);
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

//        channelMap.put(ch.id(), ch);
        System.out.println("new channel {}" + socketChannel);

        pipeline.addFirst("SslRouteHandler", new SslRouteHandler());

        pipeline.addLast("IdleStateHandler", new IdleStateHandler(HeartBeatServerHandler.READ_IDEL_TIME_OUT,
                HeartBeatServerHandler.WRITE_IDEL_TIME_OUT, HeartBeatServerHandler.ALL_IDEL_TIME_OUT, TimeUnit.SECONDS)); // 1
//        pipeline.addLast(new HeartBeatServerHandler()); // 2

        pipeline.addLast("HttpRouteHandler", new HttpRouteHandler());
//        pipeline.addLast(new HttpServerCodec());
//        pipeline.addLast(new ProtobufVarint32FrameDecoder());
//        pipeline.addLast(new ProtobufDecoder());
//        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
//        pipeline.addLast(new ProtobufEncoder());

        pipeline.addLast(new HttpObjectAggregator(MessageUnit.MAX_PACKAGE_LENGTH));
        //ChunkedWriteHandler：向客户端发送HTML5文件
        pipeline.addLast(new ChunkedWriteHandler());
//        pipeline.addLast(new HttpRequestHandler("/"));
//        pipeline.addLast(new WebSocketServerProtocolHandler("/"));
        pipeline.addLast(new WebSocketServerHandler());

        socketChannel.closeFuture().addListener(new ChannelFutureListener() {

            public void operationComplete(ChannelFuture future) throws Exception {
                if (PropertiesFileLoader.isWindows())
                    System.out.println("channel close {},currentThread={}" + future.channel() + "_" + Thread.currentThread().getName());
                // Channel 关闭后不再引用该Channel
//                channelMap.remove(future.channel().id());
            }
        });
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        super.channelActive(ctx);
        System.out.println("Active........");
        ctx = ctx;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelInactive........");

        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
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

        MessageUnit message = MessageCoder.decode(result, null);
        System.out.println(message);
    }

    public void send(MessageUnit message) {
        try {
            try {
                if (message.isNotCheckCode()/** || (message.getMessage() instanceof ComRes)**/) {
                    message.setCheckCode(1);
                } else {
                    message.setCheckCode(1);
                }

                if (send(ctx, message)) {
                    System.out.println("send over.");
                }
//				LogUtil.msgLog.info("response>>>MsgType:"+ MsgManager.getInstance().getMsgType(message.getMessage().getClass())+" MsgLog:"+message.getMsgLog()+" MsgLength:"+message.getLength()+" CheckCode:"+message.getCheckCode()+message.isNotCheckCode()
//						+" ClassName:"+message.getMessage().getClass().getName());
            } catch (Exception e) {
                LogUtil.e("websocket send :" + e.getMessage(), e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 消息发送
     *
     * @param ctx
     * @param messageUnit
     */
    private final boolean send(ChannelHandlerContext ctx, MessageUnit messageUnit) throws Exception {
        if (ctx != null && !ctx.isRemoved()) {
            if (messageUnit.getMsgType() <= 0) {
                byte[] data = messageUnit.getContent();
                if ((data == null || data.length == 0) && messageUnit.getMessage() != null) {
                    messageUnit.setMsgType(MsgManager.getInstance().getMsgType(messageUnit.getMessage().getClass()));
                }
            }

//            LogUtil.msgLog.info("send msg:channelId={},userId={},msgType={},code={},checkCode={}", ctx.channel().id().asShortText(), player != null ? player.getUserId() : "unknown", messageUnit.getMsgType(), (messageUnit.getMessage() instanceof ComRes) ? ((ComRes) messageUnit.getMessage()).getCode() : "unknown", messageUnit.getCheckCode());

            List<byte[]> list = MessageCoder.encode(messageUnit);
            int len;
            if (list != null && (len = list.size() - 1) >= 0) {
                if (len == 0) {
                    return send(ctx, list.get(len), true);
                } else {
                    for (int i = 0; i < len; i++) {
                        send(ctx, list.get(i), false);
                    }
                    return send(ctx, list.get(len), true);
                }
            }
        }
        return false;
    }

    /**
     * 消息发送
     *
     * @param ctx
     * @param msg
     */
    private final boolean send(ChannelHandlerContext ctx, byte[] msg, boolean flush) throws Exception {
        if (ctx == null || ctx.isRemoved()) {
            return false;
        }
        msgQueue.add(new MessageTransmits.Message(msg, flush));

        message();

        return true;
    }

    public final void message() {
        if (!ctx.isRemoved()) {
            if (state.compareAndSet(true, false)) {
                MessageTransmits.Message msg = msgQueue.poll();
                if (msg != null) {
                    ChannelPromise promise = ctx.newPromise();
                    promise.addListener(new GenericFutureListener<Future<? super Void>>() {
                        @Override
                        public void operationComplete(Future<? super Void> future) throws Exception {
                            add();
                            if (state.compareAndSet(false, true)) {
                                if (msgQueue.size() > 0) {
                                    message();
                                }
                            }
                        }
                    });

                    if (msg.flush()) {
                        ctx.writeAndFlush(channelMark == 1 ? new BinaryWebSocketFrame(Unpooled.wrappedBuffer(msg.msg())) : Unpooled.wrappedBuffer(msg.msg()), promise);
                    } else {
                        ctx.write(channelMark == 1 ? new BinaryWebSocketFrame(Unpooled.wrappedBuffer(msg.msg())) : Unpooled.wrappedBuffer(msg.msg()), promise);
                    }
                } else {
                    state.compareAndSet(false, true);
                }
            }
        }
    }

}
