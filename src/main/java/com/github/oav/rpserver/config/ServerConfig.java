package com.github.oav.rpserver.config;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Objects;

public class ServerConfig {
    private final InetSocketAddress address;
    private final Charset charset;


    public ServerConfig(InetSocketAddress address, Charset charset) {
        this.address = Objects.requireNonNull(address, "Не установлен адрес");
        this.charset = Objects.requireNonNull(charset, "Не установлена кодировка");
    }

    public InetSocketAddress getAddress() {
        return this.address;
    }


    public Charset getCharset() {
        return this.charset;
    }
}