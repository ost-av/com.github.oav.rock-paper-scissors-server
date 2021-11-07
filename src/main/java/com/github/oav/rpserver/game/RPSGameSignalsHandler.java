package com.github.oav.rpserver.game;

import com.github.oav.rpserver.User;

import java.util.Optional;

public interface RPSGameSignalsHandler {

    void onCreateRoom(RPSGameRoom room);
    void onEndGame(RPSGameRoom room, Optional<User> user);

    default RPSGameSignalsHandler andThen(RPSGameSignalsHandler anotherSignals){
        RPSGameSignalsHandler self = this;
        return new RPSGameSignalsHandler() {
            @Override
            public void onCreateRoom(RPSGameRoom room) {
                self.onCreateRoom(room);
                anotherSignals.onCreateRoom(room);
            }

            @Override
            public void onEndGame(RPSGameRoom room, Optional<User> user) {
                self.onEndGame(room, user);
                anotherSignals.onEndGame(room, user);
            }
        };
    }
}
