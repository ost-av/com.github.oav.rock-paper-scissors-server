package com.github.oav.rpserver.telnet;

import com.github.oav.rpserver.config.ServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class TelnetServer implements AutoCloseable{
    private static final Logger log = LoggerFactory.getLogger(TelnetServerGameHandler.class);
    private final Runnable onClose;

    private TelnetServer(ServerConfig config) throws TelnetServerException {
        log.info("is starting on {} at port {}.", System.getProperty("os.name"), config.getAddress().getPort());
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        TelnetServerInitializer initializer = new TelnetServerInitializer(config.getCharset());
        this.onClose = () -> {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            initializer.close();
        };

        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(initializer);
        InetSocketAddress address = config.getAddress();
        ChannelFuture future = bootstrap.bind(address.getHostString(), address.getPort());
        future.awaitUninterruptibly();
        if (!future.isSuccess()){
            throw new TelnetServerException(future.cause());
        }

    }
    public static TelnetServer async(ServerConfig config) throws TelnetServerException {
        return new TelnetServer(config);
    }

    @Override
    public void close() {
        this.onClose.run();
    }
}
