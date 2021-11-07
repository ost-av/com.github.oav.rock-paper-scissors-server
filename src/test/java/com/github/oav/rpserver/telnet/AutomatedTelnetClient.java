package com.github.oav.rpserver.telnet;

import com.github.oav.rpserver.game.RPSGameSign;
import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.Optional;

public class AutomatedTelnetClient {
    private static final Logger log = LoggerFactory.getLogger(AutomatedTelnetClient.class);
    private final TelnetClient telnet = new TelnetClient();
    private final InputStream in;
    private final PrintStream out;
    private final String login;

    public AutomatedTelnetClient(InetSocketAddress address, String login) throws IOException {

        // Connect to the specified server
        this.telnet.connect(address.getAddress(), address.getPort());

        // Get input and output stream references
        this.in = this.telnet.getInputStream();
        this.out = new PrintStream(this.telnet.getOutputStream());

        // Log the user on
        this.readUntil("your name:");
        this.login = login;
        this.write(this.login);

    }

    public boolean isConnected(){
        return this.telnet.isConnected();
    }

    @Override
    public String toString() {
        return "AutomatedTelnetClient{" +
                "telnet=" + this.telnet +
                ", login='" + this.login + '\'' +
                '}';
    }

    public AutomatedTelnetClient readUntilAndWrite(String until, String write){
        this.readUntil(until);
        this.write(write);
        return this;
    }

    public AutomatedTelnetClient waitOpponentAndSetSign(RPSGameSign sign){
        return this.readUntilAndWrite("choose your from [rock, paper, scissors]", sign.toString());
    }

    public Optional<String> readUntil(String pattern) {
        try {
            char lastChar = pattern.charAt(pattern.length() - 1);
            StringBuilder sb = new StringBuilder();
            char ch = (char) this.in.read();
            while (true) {
                sb.append(ch);
                if (ch == lastChar) {
                    String val = sb.toString();
                    if (val.endsWith(pattern)) {
                        log.trace("{} :: read -> {}", this, val);
                        return Optional.of(val);
                    }
                }

                int read = this.in.read();
                if (read == -1){
                    return Optional.empty();
                }
                ch = (char) read;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void write(String value) {
        try {
            this.out.println(value);
            this.out.flush();
            log.trace("{} :: write {}", this, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect() {
        try {
            this.telnet.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}