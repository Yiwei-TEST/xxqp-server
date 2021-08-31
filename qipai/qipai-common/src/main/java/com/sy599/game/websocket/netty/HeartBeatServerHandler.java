package com.sy599.game.websocket.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

/**
 * Created by lz
 */
public class HeartBeatServerHandler extends ChannelInboundHandlerAdapter {

    public static final int READ_IDEL_TIME_OUT = 25; // 读超时
    public static final int WRITE_IDEL_TIME_OUT = 30;// 写超时
    public static final int ALL_IDEL_TIME_OUT = 60; // 所有超时

    // Return a unreleasable view on the given ByteBuf
    // which will just ignore release and retain calls.
    private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled
            .unreleasableBuffer(Unpooled.copiedBuffer("Heartbeat",
                    CharsetUtil.UTF_8));  // 1

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {

        if (evt instanceof IdleStateEvent) {  // 2
            IdleStateEvent event = (IdleStateEvent) evt;

            System.out.println(ctx.channel().remoteAddress()+" 超时类型：" + event.state());

            if (event.state() == IdleState.READER_IDLE) {
                ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate()).addListener(
                        ChannelFutureListener.CLOSE_ON_FAILURE);
            } else if (event.state() == IdleState.WRITER_IDLE) {
                            ctx.writeAndFlush(
                    new PingWebSocketFrame(HEARTBEAT_SEQUENCE.retain()));
            } else if (event.state() == IdleState.ALL_IDLE) {
                ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate()).addListener(
                        ChannelFutureListener.CLOSE_ON_FAILURE);  // 3
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
