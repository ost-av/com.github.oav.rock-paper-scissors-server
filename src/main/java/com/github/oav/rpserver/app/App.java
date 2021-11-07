package com.github.oav.rpserver.app;

import com.github.oav.rpserver.config.ServerConfig;
import com.github.oav.rpserver.config.ServerConfigParseException;
import com.github.oav.rpserver.config.ServerConfigTerminalArgsParser;
import com.github.oav.rpserver.telnet.TelnetServer;
import com.github.oav.rpserver.telnet.TelnetServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    public static void main(String[] args) {
        ServerConfig config = null;
        try {
            config = new ServerConfigTerminalArgsParser().parse(args);
        } catch (ServerConfigParseException e) {
            System.out.println("illegal args, pls use pattern: address port charset");
            System.exit(1);
        }
        Scanner scanner = new Scanner(System.in);
        try (TelnetServer server = TelnetServer.async(config)) {
            Runtime.getRuntime().addShutdownHook(new Thread(server::close));
            while (scanner.hasNextLine()){
                if (scanner.nextLine().equalsIgnoreCase("stop")){
                    System.out.println("stopping");
                    break;
                } else {
                    System.out.println("only 'stop' active command");
                }
            }
        } catch (TelnetServerException e) {
            log.error("Server error", e);
            e.printStackTrace();
        } finally {
            System.out.println("stopped");
        }


    }
}
