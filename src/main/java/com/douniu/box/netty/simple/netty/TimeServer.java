package com.douniu.box.netty.simple.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 丢弃服务器
 * 丢弃任何传入数据的服务器
 */
public class TimeServer {

    private int port;

    public TimeServer(int port) {
        this.port = port;
    }

    public void run() {
        // NioEventLoopGroup是一个处理 I/O 操作的多线程事件循环。NettyEventLoopGroup为不同类型的传输提供了各种实现。
        // 在本例中，我们实现一个服务器端应用程序，因此NioEventLoopGroup将使用两个实现。第一个通常称为“boss”，用于接受传入连接。
        // 第二个通常称为“worker”，用于在“boss”接受连接并将接受的连接注册到 worker 后处理已接受连接的流量。
        // 使用多少个线程以及如何将它们映射到创建的Channel线程取决于实现EventLoopGroup，甚至可以通过构造函数进行配置
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new TimeServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync();

            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        int port = 8828;
        // run 起来然后用telnet链接， 发送数据， 服务器会消灭数据
        new TimeServer(port).run();
    }
}
