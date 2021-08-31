package com.sy599.game.websocket.netty;

import com.sy.general.GeneralHelper;
import com.sy599.game.util.LogUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NettyUtil {

    public final static Map<Channel, Long> channelUserMap = new ConcurrentHashMap<>();
    public final static Map<Channel, String> userIpMap = new ConcurrentHashMap<>();
    public final static Map<String, String> PACKAGE_MAP = new ConcurrentHashMap<>();
    public final static Map<String, String> SSL_MAP = new ConcurrentHashMap<>();

    public static String getRemoteAddr(ChannelHandlerContext ctx) {
        return getRemoteAddr(ctx.channel());
    }

    public static String getRemoteAddr(Channel channel) {
        String addr = (((InetSocketAddress) channel.remoteAddress())).getAddress().getHostAddress();
        return "0:0:0:0:0:0:0:1".equals(addr) ? "127.0.0.1" : addr;
    }

    /**
     * 返回请求
     *
     * @param ctx
     * @param res
     */
    public static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpResponse res) {
        // 返回应答的消息
        if (res.status().code() != 200) {
            ByteBuf byteBuf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(byteBuf);
            byteBuf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(res) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    public static String loadRequestIp(ChannelHandlerContext ctx,FullHttpRequest request){
        String ip = request.headers().get("X-Forwarded-For");
        String fix = getIpAddr(ip);
        if (fix != null) {
            if (fix.length() != 0) {
                ip = fix;
            } else {
                ip = request.headers().get("X-Real-IP");
                fix = getIpAddr(ip);
                if (fix != null) {
                    if (fix.length() != 0) {
                        ip = fix;
                    } else {
                        String ip1 = ip;
                        ip = request.headers().get("Proxy-Client-IP");
                        fix = getIpAddr(ip);
                        if (fix != null) {
                            if (fix.length() != 0) {
                                ip = fix;
                            } else {
                                if (ip1 == null) {
                                    ip1 = ip;
                                }

                                ip = request.headers().get("WL-Proxy-Client-IP");
                                fix = getIpAddr(ip);
                                if (fix != null) {
                                    if (fix.length() != 0) {
                                        ip = fix;
                                    } else {
                                        if (ip1 == null) {
                                            ip1 = ip;
                                        }

                                        if (ip1 == null) {
                                            ip = getRemoteAddr(ctx);
                                        } else {
                                            ip = ip1;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return ip;
    }

    private static final String getIpAddr(String ip) {
        if (ip == null) {
            return "";
        } else {
            int length = ip.length();
            if (length >= 7 && ip.indexOf(".") != -1) {
                if (ip.indexOf(",") == -1 && length <= 15) {
                    if (isIntranet0(ip)) {
                        return "";
                    } else if (GeneralHelper.isStrIPAddress(ip)) {
                        return null;
                    } else {
                        return "";
                    }
                } else {
                    String[] ips = ip.split("\\,| ");
                    String[] var3 = ips;
                    int var4 = ips.length;

                    for(int var5 = 0; var5 < var4; ++var5) {
                        String temp = var3[var5];
                        temp = temp.trim();
                        length = temp.length();
                        if (length > 0) {
                            if (GeneralHelper.isStrIPAddress(temp)) {
                                if (!isIntranet0(ip)) {
                                    return temp;
                                }
                            } else {
                            }
                        }
                    }

                    return "";
                }
            } else {
                return "";
            }
        }
    }

    private static final boolean isIntranet0(String ip) {
        return ip != null && (ip.startsWith("172.") || ip.startsWith("192.") || ip.startsWith("10."));
    }


    public static void printLog() {
        StringBuilder sb = new StringBuilder("NettyUtilMsg");
        sb.append("|").append(PACKAGE_MAP.size());
        sb.append("|").append(channelUserMap.size());
        sb.append("|").append(userIpMap.size());
        sb.append("|").append(SSL_MAP.size());
        sb.append("|").append(SslRouteHandler.channelCounter.get());
        sb.append("|").append(WebSocketServerHandler.socketCounter.get());
        LogUtil.monitorLog.info(sb.toString());
    }

}
