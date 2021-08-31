package com.sy599.game.websocket.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpServerCodec;

public class HttpRouteHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {

        if ((o instanceof ByteBuf)) {
            String channelId = channelHandlerContext.channel().id().asShortText();
            boolean isHttp;
            String type = NettyUtil.PACKAGE_MAP.get(channelId);

            ByteBuf in = ((ByteBuf) o).retain();

            if (type == null) {
                int len = in.readableBytes();
                if (len > 4) {
                    len = 4;
                }
                byte[] req = new byte[len];
                in.readBytes(req, 0, len);
                String body = new String(req, "UTF-8");

                in.resetReaderIndex();

                isHttp = (body != null && (body.startsWith("GET") || body.startsWith("POST")));
                NettyUtil.PACKAGE_MAP.put(channelId, isHttp ? "0" : "1");
            } else {
                isHttp = "0".equals(type);
            }

            ChannelHandler handler = channelHandlerContext.pipeline().get("HttpServerCodec");
            if (isHttp) {
                if (handler == null) {
                    channelHandlerContext.pipeline().addAfter("HttpRouteHandler", "HttpServerCodec", new HttpServerCodec());
                }
            } else {
                if (handler != null) {
                    channelHandlerContext.pipeline().remove("HttpServerCodec");
                }
            }
        }
        channelHandlerContext.fireChannelRead(o);
    }

}
