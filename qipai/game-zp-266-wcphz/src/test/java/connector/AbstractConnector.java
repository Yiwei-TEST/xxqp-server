package connector;

import com.sy599.game.util.ResourcesConfigsUtil;
import handler.AbstractHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.apache.mina.proxy.handlers.http.AbstractHttpLogicHandler;

import java.util.function.Function;

/**
 * @author Guang.OuYang
 * @date 2019/9/5-16:10
 */
public class AbstractConnector {

    static Bootstrap b = new Bootstrap();

    static EventLoopGroup bossGroup;//负责建立连接
    static EventLoopGroup workerGroup;//负责io操作

    {
        int workerThreadCount = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "nettyWorkerThreadCount", 0);
        String mode = "1";
        if ("1".equals(mode)) {//单线程模型
            bossGroup = new NioEventLoopGroup();
            workerGroup = bossGroup;
        } else if ("3".equals(mode)) {//主从多线程模型
            bossGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("Boss-NioEventLoopGroup"));
            workerGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("Worker-NioEventLoopGroup"));
        } else {//多线程模型
            bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("Boss-NioEventLoopGroup"));
            workerGroup = new NioEventLoopGroup(workerThreadCount, new DefaultThreadFactory("Worker-NioEventLoopGroup"));
        }
    }


    public void connect() {
        ChannelFuture connect = null;
        try {
            b.group(workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new AbstractHandler());
                        }
                    })
                    //                .childHandler(new AbstractHandler())
                    //                .option(ChannelOption.SO_RCVBUF, 32 * 1024)//接收消息缓冲区大小
                    .option(ChannelOption.SO_SNDBUF, 32 * 1024)//发送消息缓冲区大小
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_BACKLOG, 1024)//
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.SO_KEEPALIVE, true)
            ;
//                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
//                .childOption(ChannelOption.SO_KEEPALIVE, true);

            connect = b.connect("127.0.0.1", 8109);
            connect
                    .channel().closeFuture().sync()
            ;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
//            connect.channel().close().awaitUninterruptibly();
        }

    }


}
