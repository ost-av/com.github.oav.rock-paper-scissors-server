package com.github.oav.rpserver.telnet;

import com.github.oav.rpserver.SessionState;
import com.github.oav.rpserver.User;
import com.github.oav.rpserver.game.RPSGame;
import com.github.oav.rpserver.game.RPSGameRoom;
import com.github.oav.rpserver.game.RPSGameSign;
import com.github.oav.rpserver.game.RPSGameSignalsHandler;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ChannelHandler.Sharable
public class TelnetServerGameHandler extends SimpleChannelInboundHandler<String> implements AutoCloseable{
    private static final Logger log = LoggerFactory.getLogger(TelnetServerGameHandler.class);
    private static final Pattern loginPattern = Pattern.compile("^\\S+$");
    private static final String CHOOSE_YOUR_SIGN = "choose your from " + Arrays.toString(RPSGameSign.values());

    //fixme: однако, для каждого юзера создаются несколько контекстов
    // - значит мап ниже неверно идентифицирует пользователя. В либе разбираться времени нет, так что пока работает
    // - и так сойдёт. В целом можно по адресу\порту. Но уже в падлу переписывать.
    // да и вообще складывается ощущение, что неправильно либу юзаю и этот мап как костыль в реализации :(
    private final BiMap<ChannelHandlerContext, User> userByCtx =  Maps.synchronizedBiMap(HashBiMap.create());
    private final RPSGame game;
    public TelnetServerGameHandler(){
        this.game = new RPSGame(new TelnetRPSGameSignalsHandler(this));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String req) {
        log.debug("ctx = {}, req = {}", ctx,req);
        User user = this.userByCtx.computeIfAbsent(ctx, x -> new User());
        if (req.equalsIgnoreCase("exit")){
            this.closeContext(ctx);
            return;
        }

        user.updateState(state -> {
            SessionState newState = state;
            switch (state){
                case Created:
                    Matcher matcher = loginPattern.matcher(req);
                    if (matcher.find()) {
                        newState = SessionState.WaitGame;
                        user.setUsername(req);
                        this.game.addUserToQueue(user);
                    } else {
                        this.sayToCtx(ctx, "login does not match format '" + loginPattern + "', your login:");
                        break;
                    }
                case WaitGame:
                    this.sayToCtx(ctx, "search opponent");
                    break;
                case Plays:
                    RPSGameRoom room = this.game.getRoomOrThrow(user);
                    if (room.isExistsOpponentSign(user)){
                        this.sayToCtx(ctx, "you have already chosen a sign, pls w8 opponent");
                        break;
                    }
                    RPSGameSign sign;
                    try {
                        sign = RPSGameSign.valueOf(req);
                    } catch (IllegalArgumentException e){
                        this.sayToCtx(ctx, "illegal sign, " + CHOOSE_YOUR_SIGN);
                        break;
                    }
                    room.setSign(user, sign);
                    break;
            }
            return newState;
        });


        ctx.flush();
    }

    private void sayToCtx(ChannelHandlerContext ctx, String s) {
        ctx.writeAndFlush(s + System.lineSeparator());
    }


    private void sayToUser(User u, String msg) {
        this.sayToCtx(this.userByCtx.inverse().get(u), msg);
    }

    @Override
    public void close() {
        this.game.close();
    }
    private void closeUser(User user){
        ChannelHandlerContext ctx = Objects.requireNonNull(
                this.userByCtx.inverse().get(user), "Не найден контекст пользователя " + user
        );
        ctx.close();
        user.updateState(s -> SessionState.Closed);
        this.userByCtx.inverse().remove(user);
        log.trace("closeUser:: user = {}", user);
    }
    private void closeContext(ChannelHandlerContext ctx){
        User user = this.userByCtx.get(ctx);
        if (user == null){
            user = this.userByCtx.entrySet().stream()
                    .filter(e -> e.getKey().channel().remoteAddress().equals(ctx.channel().remoteAddress()))
                    .map(Map.Entry::getValue)
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("Не удалось найти пользователя по контексту " + ctx));
        }
        this.closeUser(user);
    }
    private static class TelnetRPSGameSignalsHandler implements RPSGameSignalsHandler {
        private final TelnetServerGameHandler serverHandler;

        private TelnetRPSGameSignalsHandler(TelnetServerGameHandler serverHandler) {
            this.serverHandler = serverHandler;
        }

        @Override
        public void onCreateRoom(RPSGameRoom room) {
            room.foreachOpponents(u -> {
                String opponentName = room.getOpponentOrThrow(u).getUsername().orElse("name not found!");
                this.serverHandler.sayToUser(u, "your opponent '" + opponentName + "', " + CHOOSE_YOUR_SIGN);
            });
        }

        @Override
        public void onEndGame(RPSGameRoom room, Optional<User> user) {
            user.ifPresentOrElse(winner -> {
                room.foreachOpponents(opponent -> {
                    this.serverHandler.sayToUser(opponent, "you " + (opponent == winner ? "won" : "loose"));
                    this.serverHandler.closeUser(opponent);
                });
            }, () -> room.foreachOpponents(
                    u -> this.serverHandler.sayToUser(u, "draw, " + CHOOSE_YOUR_SIGN + " again")
            ));
        }
    }
}
