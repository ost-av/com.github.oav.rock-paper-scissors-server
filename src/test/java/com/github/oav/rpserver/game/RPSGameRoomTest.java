package com.github.oav.rpserver.game;

import com.github.oav.rpserver.User;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class RPSGameRoomTest {

    @Test
    public void compareSigns() {
        RPSGameRoom room = new RPSGameRoom(new User(), new User(), new RPSGameSignalsHandler() {
            @Override
            public void onCreateRoom(RPSGameRoom room) {

            }

            @Override
            public void onEndGame(RPSGameRoom room, Optional<User> user) {

            }
        });

        Assert.assertEquals(-1, room.compareSigns(RPSGameSign.rock, RPSGameSign.scissors));
        Assert.assertEquals(0, room.compareSigns(RPSGameSign.rock, RPSGameSign.rock));
        Assert.assertEquals(1, room.compareSigns(RPSGameSign.rock, RPSGameSign.paper));
        Assert.assertEquals(1, room.compareSigns(RPSGameSign.paper, RPSGameSign.scissors));
        Assert.assertEquals(0, room.compareSigns(RPSGameSign.paper, RPSGameSign.paper));
        Assert.assertEquals(0, room.compareSigns(RPSGameSign.scissors, RPSGameSign.scissors));

    }
}