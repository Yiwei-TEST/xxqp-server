package com.sy599.game.websocket.netty;

import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollMode;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lz
 */
public class WebSocketServer {

    private static final Logger LOGGER = LoggerFactory.getLogger("msg");

    private int port;

    EventLoopGroup bossGroup;//负责建立连接
    EventLoopGroup workerGroup;//负责io操作

    {
        int workerThreadCount = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "nettyWorkerThreadCount", 0);
        String mode = ResourcesConfigsUtil.loadServerPropertyValue("netty_boss_mode","2");
        if("1".equals(mode)){//单线程模型
            bossGroup = new NioEventLoopGroup();
            workerGroup = bossGroup;
            LOGGER.info("单线程模型--建立服务器");
        }else if("3".equals(mode)){//主从多线程模型
            bossGroup = new NioEventLoopGroup(0,new DefaultThreadFactory("Boss-NioEventLoopGroup"));
            workerGroup = new NioEventLoopGroup(0,new DefaultThreadFactory("Worker-NioEventLoopGroup"));
            LOGGER.info("主从多线程模型--建立服务器");
        }else{//多线程模型
            bossGroup = new NioEventLoopGroup(1,new DefaultThreadFactory("Boss-NioEventLoopGroup"));
            workerGroup = new NioEventLoopGroup(workerThreadCount,new DefaultThreadFactory("Worker-NioEventLoopGroup"));
            LOGGER.info("多线程模型--建立服务器");
        }
    }

    ServerBootstrap b = new ServerBootstrap();
    ChannelFuture f = null;

    public WebSocketServer(int port) {
        this.port = port;
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new WebSocketServerInitializer())
                .option(ChannelOption.SO_RCVBUF, 32*1024)//接收消息缓冲区大小
                .option(ChannelOption.SO_SNDBUF, 32*1024)//发送消息缓冲区大小
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_BACKLOG, 1024)//
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
    }

    public boolean start() {
        try {
            // 绑定端口，开始接收进来的连接
            f = b.bind(port).addListener(new ChannelFutureListener() {

                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        LOGGER.info("server channel {} started,currentThread={}", future.channel(),Thread.currentThread().getName());
                    }
                }
            }).sync(); // (7)

            f.channel().closeFuture().addListener(new ChannelFutureListener() {

                public void operationComplete(ChannelFuture future) throws Exception {
                    LOGGER.info("server channel {} closed,currentThread={}", future.channel(),Thread.currentThread().getName());
                }

            });

            LOGGER.info("WebSocketServer started:port={}", port);
            WebSocketServerHandler.isOpen = true;
            return true;
        } catch (Exception e) {
            LOGGER.error("start exception:" + e.getMessage(), e);
            return false;
        }
    }

    public void distory() {
        WebSocketServerHandler.isOpen = false;
        try {
            if (f != null){
                f.sync();
            }
        } catch (Exception e) {
            LOGGER.error("close exception:" + e.getMessage(), e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            LOGGER.info("WebSocketServer closed:port={}", port);
        }
    }

}