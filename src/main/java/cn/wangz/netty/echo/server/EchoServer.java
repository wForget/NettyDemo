package cn.wangz.netty.echo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Created by hadoop on 2018/11/6.
 */
public class EchoServer {
    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    /**
     * 1. NioEventLoopGroup 是用来处理I/O操作的多线程事件循环器，Netty 提供了许多不同的 EventLoopGroup 的实现用来处理不同的传输。
     *    在这个例子中我们实现了一个服务端的应用，因此会有2个 NioEventLoopGroup 会被使用。
     *    第一个经常被叫做‘boss’，用来接收进来的连接。
     *    第二个经常被叫做‘worker’，用来处理已经被接收的连接，一旦‘boss’接收到连接，就会把连接信息注册到‘worker’上。
     *    如何知道多少个线程已经被使用，如何映射到已经创建的 Channel上都需要依赖于 EventLoopGroup 的实现，并且可以通过构造函数来配置他们的关系。
     *
     * 2. ServerBootstrap 是一个启动 NIO 服务的辅助启动类。
     *    你可以在这个服务中直接使用 Channel，但是这会是一个复杂的处理过程，在很多情况下你并不需要这样做。
     *
     * 3. 这里的事件处理类经常会被用来处理一个最近的已经接收的 Channel。
     *    ChannelInitializer 是一个特殊的处理类，他的目的是帮助使用者配置一个新的 Channel。
     *    也许你想通过增加一些处理类比如 EchoServerHandler 来配置一个新的 Channel 或者其对应的 ChannelPipeline 来实现你的网络程序。
     *    当你的程序变的复杂时，可能你会增加更多的处理类到 pipline 上，然后提取这些匿名类到最顶层的类上。
     *
     * 4. option() 是提供给 NioServerSocketChannel 用来接收进来的连接。
     *    childOption() 是提供给由父管道 ServerChannel 接收到的连接，在这个例子中也是 NioServerSocketChannel。
     */
    private void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)  // 指定使用 NIO 的传输 Channel
                    .localAddress(port)     // 设置 socket 地址使用所选的端口
                    .childHandler(new ChannelInitializer<SocketChannel>() {     // 添加 EchoServerHandler 到 Channel 的 ChannelPipeline
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new EchoServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)      // 指定的 Channel 实现的配置参数
                    .childOption(ChannelOption.SO_KEEPALIVE, true); //

            // 绑定端口，开始接收进来的连接
            ChannelFuture future = bootstrap.bind().sync();
            System.out.println(EchoServer.class.getName() + " started and listen on " + future.channel().localAddress());

            // 等待服务器  socket 关闭 。
            // 在这个例子中，这不会发生，但你可以优雅地关闭你的服务器。
            future.channel().closeFuture().sync();

        } finally {
            // 关闭 EventLoopGroup，释放所有资源。
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 9999;
        EchoServer echoServer = new EchoServer(port);
        echoServer.start();
    }
}
