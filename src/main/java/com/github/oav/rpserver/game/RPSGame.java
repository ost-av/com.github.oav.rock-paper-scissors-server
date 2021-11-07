package com.github.oav.rpserver.game;

import com.github.oav.rpserver.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RPSGame implements AutoCloseable{
    private static final Logger log = LoggerFactory.getLogger(RPSGame.class);
    private final Map<User, RPSGameRoom> activeRooms = new ConcurrentHashMap<>();
    private final Thread starterThread;
    private final RPSGameStarter starter;

    public RPSGame(RPSGameSignalsHandler handler){
        RPSGameSignalsHandler withInternalHandler = new InternalSignalsHandler(this)
                .andThen(handler);
        this.starter = new RPSGameStarter(withInternalHandler);
        this.starterThread = new Thread(this.starter);
        log.debug("rps game daemon {} started", this.starterThread);
        this.starterThread.start();
    }

    private void addRoom(RPSGameRoom room){
        room.foreachOpponents(u -> this.activeRooms.put(u, room));
    }


    private void removeRoom(RPSGameRoom room){
        room.foreachOpponents(this.activeRooms::remove);
    }


    public void addUserToQueue(User user) {
        this.starter.addToQueue(user);
    }

    public RPSGameRoom getRoomOrThrow(User user){
        return Objects.requireNonNull(this.activeRooms.get(user));
    }

    @Override
    public void close() {
        if (this.starterThread.isAlive())
            this.starterThread.interrupt();
    }

    private static class InternalSignalsHandler implements RPSGameSignalsHandler {
        private final RPSGame game;

        private InternalSignalsHandler(RPSGame game) {
            this.game = game;
        }

        @Override
        public void onCreateRoom(RPSGameRoom room) {
            this.game.addRoom(room);
        }

        @Override
        public void onEndGame(RPSGameRoom room, Optional<User> user) {
            if (user.isEmpty()) { //победителя нет - комнату чистим
                room.clear();
            } else {
                this.game.removeRoom(room);
            }
        }
    }
}
