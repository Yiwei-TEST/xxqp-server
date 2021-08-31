package com.sy599.game.websocket.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.ssl.SslHandler;

import java.util.concurrent.atomic.AtomicInteger;

public class SslRouteHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        String id = channelHandlerContext.channel().id().asShortText();
        if ((o instanceof ByteBuf)) {
            ByteBuf in = ((ByteBuf) o).retain();

            int count = in.readableBytes();

            boolean isSsl = false;
            if (count >= 5) {
                if (SslHandler.isEncrypted(in)) {
                    isSsl = true;
                }
                in.resetReaderIndex();
            }

            ChannelHandler handler = channelHandlerContext.pipeline().get("SslHandler");
            if (isSsl) {
                NettyUtil.SSL_MAP.put(id,"1");
                if (handler == null) {
                    SslHandler sslHandler = SslUtil.createSslHandler();
                    if (sslHandler != null) {
                        channelHandlerContext.pipeline().addAfter("SslRouteHandler", "SslHandler", sslHandler);
//                        channelHandlerContext.pipeline().addBefore("IdleStateHandler","SslHandler", sslHandler);
                    }else{
                        NettyUtil.SSL_MAP.put(id,"0");
                    }
                }
            } else {
                if (!"1".equals(NettyUtil.SSL_MAP.get(id))){
                    NettyUtil.SSL_MAP.put(id,"0");
                    if (handler != null) {
                        channelHandlerContext.pipeline().remove("SslHandler");
                    }
                }
            }
        }
        channelHandlerContext.fireChannelRead(o);
    }

    public static final AtomicInteger channelCounter = new AtomicInteger();

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        channelCounter.incrementAndGet();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        channelCounter.decrementAndGet();
    }

}
