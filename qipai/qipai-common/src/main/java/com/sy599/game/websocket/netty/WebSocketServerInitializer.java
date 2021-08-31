package com.sy599.game.websocket.netty;

import com.sy.mainland.util.PropertiesFileLoader;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by lz
 */
public class WebSocketServerInitializer extends
        ChannelInitializer<SocketChannel> { //1

    private static final Logger LOGGER = LoggerFactory.getLogger("msg");

    @Override
    public void initChannel(SocketChannel ch) throws Exception {//2
        ChannelPipeline pipeline = ch.pipeline();

//        channelMap.put(ch.id(), ch);
        LOGGER.debug("new channel {}", ch);

        pipeline.addFirst("SslRouteHandler",new SslRouteHandler());

        pipeline.addLast("IdleStateHandler",new IdleStateHandler(HeartBeatServerHandler.READ_IDEL_TIME_OUT,
                HeartBeatServerHandler.WRITE_IDEL_TIME_OUT, HeartBeatServerHandler.ALL_IDEL_TIME_OUT, TimeUnit.SECONDS)); // 1
//        pipeline.addLast(new HeartBeatServerHandler()); // 2

        pipeline.addLast("HttpRouteHandler",new HttpRouteHandler());
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

        ch.closeFuture().addListener(new ChannelFutureListener() {

            public void operationComplete(ChannelFuture future) throws Exception {
                if (PropertiesFileLoader.isWindows())
                    LOGGER.info("channel close {},currentThread={}", future.channel(),Thread.currentThread().getName());
                // Channel 关闭后不再引用该Channel
//                channelMap.remove(future.channel().id());
            }
        });
    }
}