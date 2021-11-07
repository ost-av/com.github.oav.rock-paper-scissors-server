package com.github.oav.rpserver.game;

import com.github.oav.rpserver.SessionState;
import com.github.oav.rpserver.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Демон для старта игр, если набирается 2 игрока в пуле.
 */
public class RPSGameStarter implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(RPSGameStarter.class);
    private final RPSGameSignalsHandler signalsHandler;
    private final BlockingQueue<User> gameQueue = new LinkedBlockingQueue<>();

    public RPSGameStarter(RPSGameSignalsHandler signalsHandler){
        this.signalsHandler = Objects.requireNonNull(signalsHandler);
    }
    @Override
    public void run() {
        Set<User> selectedGamers = new HashSet<>();
        while (true){
            try {
                selectedGamers.add(this.gameQueue.take());
            } catch (InterruptedException ignored) {

            }
            //выкидываем, если чел отключился
            selectedGamers.removeIf(us -> us.getState() == SessionState.Closed);
            if (selectedGamers.size() == 2){
                List<User> gamers = new ArrayList<>(selectedGamers);
                RPSGameRoom room = new RPSGameRoom(gamers.get(0), gamers.get(1), this.signalsHandler);
                this.signalsHandler.onCreateRoom(room);
                selectedGamers.clear();
            }
        }
    }

    public void addToQueue(User session){
        log.trace("addToQueue:: {}", session);
        this.gameQueue.add(session);
    }

}
