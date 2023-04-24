package Game;

import GameObjects.Player;
import Online.Base.Connection;
import Online.Base.Message;
import Online.Base.MessageType;
import Online.MessagePayloadObjects.CommonPayloadObjects.PayloadStringData;
import Online.MessagePayloadObjects.PlayerPayloadObjects.PayloadLoginData;
import Online.MessagePayloadObjects.PlayerPayloadObjects.PayloadSpeedXY;
import Online.MessagePayloadObjects.ServerPayloadObjects.PayloadGameFullData;
import Online.MessagePayloadObjects.ServerPayloadObjects.PayloadGameTickData;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.awt.*;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameUpdater implements Closeable {
    private final OnlinePlayer player;
    private final AtomicBoolean closed;
    private final CopyOnWriteArrayList<Thread> toInterrupt;
    private final GameRenderer renderer;
    private Game game;

    public GameUpdater(Game game, GameRenderer renderer) {
        this.game = game;
        this.renderer = renderer;

        //connect to server
        Random r = new Random(System.currentTimeMillis());
        player = connect("192.168.0.106", 26780, 152.5, 35.6, 90, 60, new Color(r.nextInt()), "Livefish" + Math.random());

        //handle messages
        ArrayBlockingQueue<Message> messageQueue = new ArrayBlockingQueue<>(150, true);
        this.closed = new AtomicBoolean(false);
        this.toInterrupt = new CopyOnWriteArrayList<>();

        Thread serverMessagesHandleThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Message msg = player.readMessage();
                    messageQueue.put(msg);
                }
            } catch (IOException e) {
                // TODO: 24.04.2023 Reconnect maybe
                close();
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        serverMessagesHandleThread.start();

        Thread messageSendingThread = new Thread(() -> {
            try {
                System.out.println("Entered speed sending mode:");
                int speedSentCount = 0;
                double lastUpdate = 0;
                final long REQUEST_TIMEOUT = 10;

                while (!Thread.currentThread().isInterrupted()) {
                    double sx, sy;
                    // TODO: 24.04.2023 replace this with keyboard input
                    if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                        sx = -0.1;
                    } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                        sx = 0.1;
                    } else {
                        sx = 0;
                    }
                    if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                        sy = -0.1;
                    } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                        sy = 0.1;
                    } else {
                        sy = 0;
                    }
                    if (player.player.getSpeedX() != sx || player.player.getSpeedY() != sy) {
                        player.setSpeed(sx, sy);
                        player.sendSpeed();
                        System.out.println("Speed sent" + (speedSentCount++) + " times");
                    }

                    if (System.currentTimeMillis() - lastUpdate > REQUEST_TIMEOUT) {
                        /* read client data */

                        // TODO: 20.04.2023 Priority queue for messages
                        lastUpdate = System.currentTimeMillis();
                        Message toHandle = messageQueue.take();
                        handleMessage(toHandle, player);
                    }
                }
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
                close();
            }
        });
        messageSendingThread.start();

        toInterrupt.add(serverMessagesHandleThread);
        toInterrupt.add(messageSendingThread);
    }

    private OnlinePlayer connect(String ip, int port, double x, double y, int w, int h, Color color, String nick) {
        Connection server = new Connection(ip, port);
        OnlinePlayer player = new OnlinePlayer(server, nick, Thread.currentThread());
        PayloadLoginData loginData = new PayloadLoginData(nick, x, y, w, h, color);
        Message loginMessage = new Message(MessageType.LOGIN_DATA, loginData);
        player.player = new Player("INVALID_LOGIN_PLAYER_NICKNAME", x, y, w, h, color);
        System.out.println("Connected to server!");

        try {
            player.writeMessage(loginMessage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Logged in with nickname: " + nick);

        return player;
    }

    private void handleMessage(Message msg, OnlinePlayer player) throws IOException {
        switch (msg.type) {
            case INVALID -> {
                player.writeMessage(Message.ErrorMessage("INVALID MESSAGE SENT!"));
                System.out.println("Server: sent invalid message with payload: " + msg.payload);
            }
            case ERROR -> {
                throw new RuntimeException(((PayloadStringData) msg.payload).str);
            }
            case INFO -> {
                System.out.println("Received info from server: " + ((PayloadStringData) msg.payload).str);
            }
            case LOGIN_DATA -> {
                player.writeMessage(Message.ErrorMessage("LOGIN DATA SENT!"));
                System.out.println("Server: sent login data!");
            }
            case SPEED_XY -> {
                player.writeMessage(Message.ErrorMessage("SPEED DATA SENT!"));
                System.out.println("Server: sent speed data!");
            }
            case GAME_DATA_TICK -> {
                PayloadGameTickData gameData = (PayloadGameTickData) msg.payload;
                game.resolveUpdate(gameData.game);
                renderer.setToRender(game);
            }
            case GAME_DATA_FULL -> {
                PayloadGameFullData completeGameData = (PayloadGameFullData) msg.payload;
                game = completeGameData.game;
                renderer.setToRender(game);
            }
            default -> throw new IllegalStateException("Unexpected value: " + msg.type);
        }
    }


    @Override
    public void close() {
        if (closed.get())
            return;
        closed.set(true);
        for (Thread thread : toInterrupt) {
            thread.interrupt();
        }
        try {
            player.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        Gdx.app.log("Game updater", "closed");
    }


    private static class OnlinePlayer implements Closeable {
        private final Connection connection;
        public final Thread playerThread;
        public final String nickname;
        public Player player;

        public OnlinePlayer(Connection connection, String nickname, Thread clientThread) {
            this.connection = connection;
            this.nickname = nickname;
            this.playerThread = clientThread;
        }

        public void sendSpeed() throws IOException {
            Message msg = new Message(MessageType.SPEED_XY, new PayloadSpeedXY(player.getSpeedX(), player.getSpeedY()));
            connection.writeMessage(msg);
        }

        public void setSpeed(double speedX, double speedY) {
            player.setSpeedX(speedX);
            player.setSpeedY(speedY);
        }

        public void writeMessage(Message msg) throws IOException {
            connection.writeMessage(msg);
        }

        public Message readMessage() throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException {
            return connection.readMessage();
        }


        @Override
        public void close() throws IOException {
            if (playerThread != null)
                playerThread.interrupt();
            connection.close();
        }

        @Override
        public String toString() {
            return "OnlinePlayer{" +
                    "nickname='" + nickname + '\'' +
                    '}';
        }
    }
}