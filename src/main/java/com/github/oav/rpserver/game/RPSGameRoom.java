package com.github.oav.rpserver.game;

import com.github.oav.rpserver.SessionState;
import com.github.oav.rpserver.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class RPSGameRoom {
    private static final Logger log = LoggerFactory.getLogger(RPSGameRoom.class);
    private final Map<User, Optional<RPSGameSign>> opponentSign = new ConcurrentHashMap<>();
    private final RPSGameSignalsHandler signals;
    private byte cntSetSigns = 0;
    public RPSGameRoom(User firstOp, User secondOp, RPSGameSignalsHandler signals){
        this.signals = Objects.requireNonNull(signals);
        this.opponentSign.put(Objects.requireNonNull(firstOp), Optional.empty());
        this.opponentSign.put(Objects.requireNonNull(secondOp), Optional.empty());
        this.opponentSign.keySet().forEach(user -> user.updateState(b -> SessionState.Plays));
        log.trace("created room {}", this);
    }

    public void clear(){
        this.cntSetSigns = 0;
        Set<User> users = new HashSet<>(this.opponentSign.keySet());
        this.opponentSign.clear();
        users.forEach(u -> this.opponentSign.put(u, Optional.empty()));
    }

    @Override
    public String toString() {
        return "RPSGameRoom{" +
                "opponentSign=" + this.opponentSign +
                ", signals=" + this.signals +
                ", cntSetSigns=" + this.cntSetSigns +
                '}';
    }

    /**
     *
     * @throws NullPointerException if  check user opponent not from this room
     */
    public boolean isExistsOpponentSign(User user){
        Optional<RPSGameSign> rpsGameSign = Objects.requireNonNull(this.opponentSign.get(user),
                user + " не присутствует в данной игре");
        return rpsGameSign.isPresent();
    }

    public void setSign(User user, RPSGameSign sign){
        log.trace("user {} set sign {}", user, sign);
        if (this.isExistsOpponentSign(user)) {
            log.warn("opponent {} set sign again!", user);
            return; //пользователь уже походил, по хорошему надо дать ответ наверх,
            // что есть попытка заново установить значение - но в падлу

        }
        this.opponentSign.put(user, Optional.of(sign));
        this.cntSetSigns++;
        if (this.cntSetSigns == 2){
            List<Map.Entry<User, Optional<RPSGameSign>>> userAndSign = new ArrayList<>(this.opponentSign.entrySet());
            Map.Entry<User, Optional<RPSGameSign>> first = userAndSign.get(0);
            Map.Entry<User, Optional<RPSGameSign>> second = userAndSign.get(1);
            int gameResult = this.compareSigns(first.getValue().orElseThrow(), second.getValue().orElseThrow());
            Optional<User> winner;
            if (gameResult == 0) {
                winner = Optional.empty();
            } else if (gameResult < 0) {
                winner = Optional.of(first.getKey());
            } else {
                winner = Optional.of(second.getKey());
            }
            this.signals.onEndGame(this, winner);
        }
    }

    protected int compareSigns(RPSGameSign first, RPSGameSign second){
        if (first == second) return 0;
        switch (first){
            case scissors: return second == RPSGameSign.rock ? 1 : -1;
            case paper: return second == RPSGameSign.scissors ? 1 : -1;
            case rock: return second == RPSGameSign.paper ? 1 : -1;
            default: throw new IllegalArgumentException(first + " - illegal sign");
        }
    }


    public User getOpponentOrThrow(User opponent){
        if (!this.opponentSign.containsKey(opponent))
            throw new IllegalArgumentException("unexpected opponent " + opponent);
        return this.opponentSign.keySet().stream()
                .filter(op -> op != opponent)
                .findAny()
                .orElseThrow(); // если валидно всё написано то не ёбнет
    }


    public void foreachOpponents(Consumer<User> userConsumer){
        this.opponentSign.keySet().forEach(userConsumer);
    }
}

