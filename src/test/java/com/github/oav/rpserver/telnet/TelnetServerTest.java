package com.github.oav.rpserver.telnet;

import com.github.oav.rpserver.User;
import com.github.oav.rpserver.config.ServerConfig;
import com.github.oav.rpserver.game.RPSGameRoom;
import com.github.oav.rpserver.game.RPSGameSign;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class TelnetServerTest {
    private static final Logger log = LoggerFactory.getLogger(TelnetServerTest.class);
    public static final InetSocketAddress address = InetSocketAddress
            .createUnresolved("localhost", 8080);
    private static final Charset charset = Charset.forName("windows-1251");
    private static final Random random = new Random();
    private TelnetServer server;

    @Before
    public void setUp() throws Exception {
        this.server = TelnetServer.async(new ServerConfig(address, charset));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                this.server.close();
            } catch (Exception e) {
                log.warn("Прерывание на закрытии сервера", e);
            }
        }));
    }


    @Test
    public void onlyInit() throws InterruptedException {
        Thread.sleep(1000);
    }

    @Test
    public void stress() throws Exception {
        //todo: кривой недоделанный стресс тест
        // да и нужно либу с работниками искать
        Set<Thread> threads = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            Thread thread = new Thread(() -> {
                try {
                    AutomatedTelnetClient userAndSetRandomSign = this.createUserAndSetRandomSign();
                    Thread.sleep(1000);
                    Assert.assertFalse(userAndSetRandomSign.isConnected());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
    }
    private RPSGameSign randomSign(){
        return RPSGameSign.values()[random.nextInt(2)];
    }
    private AutomatedTelnetClient createUserAndSetRandomSign() throws IOException {
        AutomatedTelnetClient client = new AutomatedTelnetClient(address, "test_user" + UUID.randomUUID())
                .waitOpponentAndSetSign(this.randomSign());
        while (client.isConnected() && client.readUntil("draw").isPresent()){
            client.waitOpponentAndSetSign(this.randomSign());
        }
        return client;
    }
}