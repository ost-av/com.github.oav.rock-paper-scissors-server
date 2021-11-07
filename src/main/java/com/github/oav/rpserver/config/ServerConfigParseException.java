package com.github.oav.rpserver.config;

public class ServerConfigParseException extends Exception{
    public ServerConfigParseException(String[] args, String msg){
        super("Ошибка при парсинге аргументов");
    }
}
