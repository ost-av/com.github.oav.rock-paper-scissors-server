package com.github.oav.rpserver.config;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.text.ParseException;

public class ServerConfigTerminalArgsParser {
    private static final Charset defaultCharset = Charset.forName("windows-1251");
    public ServerConfig parse(String[] args) throws ServerConfigParseException {
        //todo: в целом можно какой нибудь рефлективной либой заменить
        if (args.length < 2 || args.length > 3){
            throw new ServerConfigParseException(args, "unexpected count args");
        }
        InetSocketAddress address = InetSocketAddress.createUnresolved(args[0], this.parsePort(args, args[1]));
        return new ServerConfig(address, (args.length == 2 ? defaultCharset : Charset.forName(args[2])));
    }

    private int parsePort(String[] args, String port) throws ServerConfigParseException {
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException e){
            throw new ServerConfigParseException(args, e.getMessage());
        }

    }
}
