package com.github.oav.rpserver.telnet;

import com.github.oav.rpserver.SessionState;
import com.github.oav.rpserver.User;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Objects;

public class TelnetServerInitializer extends ChannelInitializer<SocketChannel> implements AutoCloseable{
    private static final Logger log = LoggerFactory.getLogger(TelnetServerInitializer.class);
    private final TelnetServerGameHandler handler = new TelnetServerGameHandler();
    private final StringDecoder decoder;
    private final StringEncoder encoder;

    public TelnetServerInitializer(Charset charset) {
        Objects.requireNonNull(charset);
        this.decoder = new StringDecoder(charset);
        this.encoder = new StringEncoder(charset);
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        log.debug("initChannel::SocketChannel = {}", ch);
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        pipeline.addLast("decoder", this.decoder);
        pipeline.addLast("encoder", this.encoder);
        ch.pipeline().addLast(this.handler);
        ch.writeAndFlush("your name:" + System.lineSeparator());
    }



    @Override
    public void close() {
        this.handler.close();
    }
}
