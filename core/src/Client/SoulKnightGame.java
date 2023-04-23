package Client;

import GameObjects.Game;
import GameObjects.Player;
import Online.Connection;
import Online.Message;
import Online.MessagePayloadObjects.PayloadStringData;
import Online.MessagePayloadObjects.PlayerMessagesPayloadObjects.PayloadLoginData;
import Online.MessagePayloadObjects.ServerMessagesPayloadObjects.PayloadGameFullData;
import Online.MessagePayloadObjects.ServerMessagesPayloadObjects.PayloadGameTickData;
import Online.MessageType;
import Online.OnlinePlayer;
import ResourceManager.ResourceManager;
import Timer.Timer;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class SoulKnightGame extends ApplicationAdapter {
    private OrthographicCamera camera;
    private SpriteBatch batch;

    private Game game;
    private OnlinePlayer player;
    private CopyOnWriteArrayList<Thread> toInterrupt;
    private AtomicBoolean closed;

    private void initGame() {
        game = new Game();
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

    @Override
    public void create() {
        // TODO: 23.04.2023 Load some resources here
        toInterrupt = new CopyOnWriteArrayList<>();
        ResourceManager.init();
        ResourceManager.loadAll();
        Random r = new Random(System.currentTimeMillis());
        player = connect("192.168.0.106", 26780, 152.5, 35.6, 90, 60, new Color(r.nextInt()), "Livefish" + Math.random());
        initGame();

        camera = new OrthographicCamera();
        camera.setToOrtho(true, 800, 480);
        batch = new SpriteBatch();
        ArrayBlockingQueue<Message> messageQueue = new ArrayBlockingQueue<>(150, true);
        closed = new AtomicBoolean(false);

        Thread serverMessagesHandleThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Message msg = player.readMessage();
                    messageQueue.add(msg);
                }
            } catch (IOException e) {
                dispose();
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        serverMessagesHandleThread.start();

        Thread messageSendingThread = new Thread(() -> {
            try {
                System.out.println("Entered speed sending mode:");
                int speedSentCount = 0;
                double lastUpdate = 0;
                final long REQUEST_TIMEOUT = 10;
                Timer timer = new Timer();

                while (!Thread.currentThread().isInterrupted()) {
                    double sx, sy;
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

                    if (timer.getGlobalTimeMillis() - lastUpdate > REQUEST_TIMEOUT) {
                        /* read client data */
                        // TODO: 20.04.2023 Priority queue for messages
                        lastUpdate = timer.getGlobalTimeMillis();
                        handleMessage(messageQueue.take(), player);
                    }
                    //TIMER TICKS HERE, because main graphics thread blocks if player presses f11, graphics thread yields until key is released, so timer fails to tick and messages are not handled
                    timer.tick();
                }
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
                dispose();
            }
        });
        messageSendingThread.start();

        toInterrupt.add(serverMessagesHandleThread);
        toInterrupt.add(messageSendingThread);
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
            }
            case GAME_DATA_FULL -> {
                PayloadGameFullData completeGameData = (PayloadGameFullData) msg.payload;
                game = completeGameData.game;
            }
            default -> throw new IllegalStateException("Unexpected value: " + msg.type);
        }
    }


    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0.2f, 1);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        game.draw(batch);
        batch.end();
    }

    @Override
    public void dispose() {
        if (closed.get())
            return;
        closed.set(true);
        ResourceManager.disposeAll();
        batch.dispose();
        for (Thread thread : toInterrupt) {
            thread.interrupt();
        }
        try {
            player.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
