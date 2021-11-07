package com.github.oav.rpserver;


import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class User {
    private String username;
    private SessionState state = SessionState.Created;
    public Optional<String> getUsername() {
        return Optional.ofNullable(this.username);
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + this.username + '\'' +
                ", state=" + this.state +
                '}';
    }

    public SessionState getState() {
        return this.state;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public synchronized void updateState(Function<SessionState, SessionState> mapping){
        this.state = Objects.requireNonNull(mapping.apply(this.state));
    }
}
