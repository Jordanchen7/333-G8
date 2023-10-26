package com.whack.a.mole;

import static com.whack.a.mole.data.Message.TYPE_ACK;
import static com.whack.a.mole.data.Message.TYPE_GEN_MOLE;
import static com.whack.a.mole.data.Message.TYPE_HANDSHAKE;
import static com.whack.a.mole.data.Message.TYPE_SYNC;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Timer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.whack.a.mole.bridge.MoleCallback;
import com.whack.a.mole.bridge.SystemCapability;
import com.whack.a.mole.component.DataWin;
import com.whack.a.mole.component.MainMenu;
import com.whack.a.mole.component.Mole;
import com.whack.a.mole.component.NetMenu;
import com.whack.a.mole.component.RankingList;
import com.whack.a.mole.component.Text;
import com.whack.a.mole.data.Message;
import com.whack.a.mole.data.ScoreManager;
import com.whack.a.mole.mode.BasicTypeExclusionStrategy;
import com.whack.a.mole.mode.EasyMode;
import com.whack.a.mole.mode.GameMode;
import com.whack.a.mole.mode.HardMode;
import com.whack.a.mole.mode.NormalMode;
import com.whack.a.mole.net.NetUtils;
import com.whack.a.mole.utils.ConstUtils;
import com.whack.a.mole.utils.Logger;
import com.whack.a.mole.utils.TextUtils;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;


public class WhackAMoleGame extends ApplicationAdapter {
    private static final String TAG = "WhackAMoleGame";
    private SpriteBatch batch;
    private World world;
    private final CopyOnWriteArrayList<Mole> moles = new CopyOnWriteArrayList<>();;

    private OrthographicCamera camera;
    private Texture moleTexture;

    private Texture luckyTexture;
    protected Texture obstacleTexture;
    private Sound hitSound;
    private Sound misSound;

    private Sound menuSound;

    private boolean gameRunning = false;

    private Stage stage;

    private final SystemCapability systemCapability;

    private Texture backgroundTexture;

    private int screenHeight;
    private int screenWidth;

    private GameMode gameMode;

    private boolean isLastHit;

    private final ScoreManager scoreManager;
    private RankingList rankingList;

    // ui component
    private MainMenu mainMenu;
    private DataWin myDataWin;
    private DataWin otherDataWin;

    private NetMenu netMenu;

    private int score;

    private String text;

    private int countdown;

    private String user;

    private NetMode netMode;

    private boolean clientConnected;

    private boolean serverConnected;

    private Timer.Task serverWaitTask;
    private Timer.Task clientWaitTask;

    private GameMode.MoleReceiver receiver;

    public enum NetMode {
        None,
        Server,
        Client
    }

    public WhackAMoleGame(final SystemCapability systemCapability) {
        this.systemCapability = systemCapability;
        Logger.setSystemCapability(systemCapability);
        scoreManager = new ScoreManager(systemCapability);

        netMode = NetMode.None;
        systemCapability.setMoleCallback(new MoleCallback() {
            public void onClientConnected() {
                Logger.info(TAG, "onClientConnected");

                // show other data win
                if (otherDataWin != null) {
                    otherDataWin.setVisible(true);
                }

                clientConnected = true;

                systemCapability.showToast("Client Connected");
            }

            public void onClientClosed() {
                Logger.info(TAG, "onClientClosed");
                clientConnected = false;
                systemCapability.showToast("Client closed");
                gameMode.stop();
            }

            public void onClientMessage(String msg) {
                Logger.info(TAG, "onClientMessage:" + msg);
                Gson gson = new Gson();
                final Message message = gson.fromJson(msg, Message.class);

                // 在ui线程中更新
                Gdx.app.postRunnable(new Runnable() {

                    @Override
                    public void run() {
                        if (message.type.equals(TYPE_SYNC)) {
                            otherDataWin.updateCountdown(message.countdown);
                            otherDataWin.updateScore(message.score);
                            otherDataWin.updateText(message.text);
                            otherDataWin.updateUser(message.user);

                            // save score
                            if (message.countdown == 0) {
                                scoreManager.saveScore(message.user, message.score);
                            }
                        } else if (message.type.equals(TYPE_ACK)) {
                            if (serverWaitTask != null) {
                                serverWaitTask.cancel();
                            }
                            // start game
                            gameMode.start();
                        }
                    }
                });

            }

            public void onServerConnected() {
                Logger.info(TAG, "onServerConnected");
                if (clientWaitTask != null) {
                    clientWaitTask.cancel();
                }

                serverConnected = true;
                netMode = NetMode.Client;

                // show other data win
                if (otherDataWin != null) {
                    otherDataWin.setVisible(true);
                }

                systemCapability.showToast("Connected to server");
            }

            @Override
            public void onServerFailed() {
                connectServerFailed();
            }

            public void onServerMessage(String msg) {
                Logger.info(TAG, "onServerMessage:" + msg);
                final Gson gson = new Gson();
                final Message message = gson.fromJson(msg, Message.class);

                // 在ui线程中更新
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (message.type.equals(TYPE_SYNC)) {
                            if (otherDataWin == null) {
                                otherDataWin = new DataWin(screenWidth, screenHeight, "-", stage, "-", Text.Align.RIGHT);
                                otherDataWin.setVisible(false);
                            }

                            otherDataWin.updateCountdown(message.countdown);
                            otherDataWin.updateScore(message.score);
                            otherDataWin.updateText(message.text);
                            otherDataWin.updateUser(message.user);

                            if (netMode.equals(NetMode.Client)) {
                                processCountDown(message.countdown);
                            }

                        } else if (message.type.equals(TYPE_HANDSHAKE)) {
                            // send ack back to server
                            Message handshake = new Message();
                            handshake.type = TYPE_ACK;
                            systemCapability.sendMsgToServer(gson.toJson(handshake));
                            gameMode = new com.whack.a.mole.mode.NetMode(screenWidth, screenHeight / 2, world, moleTexture, luckyTexture, obstacleTexture, receiver);
                            gameMode.start();

                            // hide the net menu
                            netMenu.setVisible(false);

                            // start game
                            startGame();

                        } else {
                            ((com.whack.a.mole.mode.NetMode)gameMode).processMsg(message);
                        }
                    }
                });

            }

            @Override
            public void onUserChanged(final String newUser) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        mainMenu.updateUser(newUser);
                    }
                });

            }

            @Override
            public void onServerChanged(final String newServer) {
                serverConnected = false;
                systemCapability.startMoleClient(newServer);
                netMode = NetMode.Client;

                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        mainMenu.updateNetMode(netMode);
                        netMenu.updateClientItem(ConstUtils.TEXT_RUN_IN_CLIENT_MODE+ " with " + newServer, new Color(ConstUtils.HIGHLIGHT_ITEM_COLOR));
                    }
                });

            }
        });

    }

    @Override
    public void create() {
        screenHeight = Gdx.graphics.getHeight();
        screenWidth = Gdx.graphics.getWidth();

        batch = new SpriteBatch();
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        backgroundTexture = new Texture(Gdx.files.internal("garden.jpg"));

        // the image of mole
        moleTexture = new Texture("mole.png");
        luckyTexture = new Texture("lucky.png");
        obstacleTexture = new Texture("obstacle.png");

        hitSound = Gdx.audio.newSound(Gdx.files.internal("hit.mp3"));
        misSound = Gdx.audio.newSound(Gdx.files.internal("mis.mp3"));
        menuSound = Gdx.audio.newSound(Gdx.files.internal("menu.mp3"));

        // create Box2D world
        world = new World(new Vector2(0, -9.8f), true);

        camera = new OrthographicCamera();
        // set viewport
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        receiver = new GameMode.MoleReceiver() {

            @Override
            public void onMoleCreated(Mole mole) {

                if (netMode.equals(NetMode.Server)) {
                    // send mole info to the client
                    Gson gson = new GsonBuilder()
                            .setExclusionStrategies(new BasicTypeExclusionStrategy())
                            .create();
                    Message msg = new Message();
                    msg.type = TYPE_GEN_MOLE;
                    msg.mole = gson.toJson(mole);

                    systemCapability.sendMsgToClient(gson.toJson(msg));
                } else if (netMode.equals(NetMode.Client)) {
                    mole.initMole(world, moleTexture, obstacleTexture);
                }

                Logger.info(TAG, "onMoleCreated: netMode=" + netMode);

                moles.add(mole);
            }

            @Override
            public void onCountdown(int cd) {
                processCountDown(cd);
            }
        };

        mainMenu = new MainMenu(screenWidth, screenHeight, stage, new MainMenu.MenuClickListener() {

            @Override
            public void onMenuClicked(MainMenu.MenuItem item) {
                systemCapability.log("zc", "click item " + item);
                menuSound.play();
                switch (item) {
                    case CHANGE_USER:
                        changeUser();
                        break;
                    case EASY_MODE:
                        if (netMode.equals(NetMode.Client)) {
                            systemCapability.showToast("Not Available in Client Mode");
                            return;
                        }
                        gameMode = new EasyMode(screenWidth, screenHeight / 2, world, moleTexture, luckyTexture, obstacleTexture, receiver);
                        startGame();
                        break;
                    case NORMAL_MODE:
                        if (netMode.equals(NetMode.Client)) {
                            systemCapability.showToast("Not Available in Client Mode");
                            return;
                        }
                        gameMode = new NormalMode(screenWidth, screenHeight / 2, world, moleTexture, luckyTexture, obstacleTexture, receiver);
                        startGame();
                        break;
                    case HARD_MODE:
                        if (netMode.equals(NetMode.Client)) {
                            systemCapability.showToast("Not Available in Client Mode");
                            return;
                        }
                        gameMode = new HardMode(screenWidth, screenHeight / 2, world, moleTexture, luckyTexture, obstacleTexture, receiver);
                        startGame();
                        break;
                    case RANKING:
                        showRanking();
                        break;
                    case NET_PLAY:
                        showNetMenu();
                        break;
                    default:
                        break;
                }
            }
        });

        netMenu = new NetMenu(screenWidth, screenHeight, stage, new NetMenu.NetMenuClickListener() {
            @Override
            public void onNetMenuClicked(NetMenu.NetMenuItem item, Text text) {
                menuSound.play();

                switch (item) {
                    case CLIENT_MODE:
                        startClient(text);
                        break;
                    case SERVER_MODE:
                        startServer(text);
                        break;
                    case RETURN:
                        netMenu.setVisible(false);
                        mainMenu.setVisible(true);
                        break;
                    default:
                        break;
                }
            }
        });

        this.systemCapability.requestPermission();
        systemCapability.log("zc", NetUtils.getLocalIpAddress());
    }

    private void changeUser() {
        systemCapability.getUsrInput("Input Your Name", ConstUtils.KEY_USERNAME, true);
    }

    private void startClient(final Text text) {
        if (netMode.equals(NetMode.Client)) {
            netMode = NetMode.None;
            mainMenu.updateNetMode(netMode);
            // reset client mode
            text.update(ConstUtils.TEXT_RUN_IN_CLIENT_MODE, new Color(ConstUtils.MENU_ITEM_COLOR));
            return;
        }

        // get server ip
        String serverIp = systemCapability.getUsrInput(ConstUtils.KEY_SERVER_IP_PROMPT, ConstUtils.KEY_SERVER_IP, true);
        Logger.info(TAG, "serverIp:" + serverIp);
        if (!TextUtils.isValidIPAddress(serverIp)) {
            systemCapability.showToast("invalid ip address");
            netMode = NetMode.None;
            mainMenu.updateNetMode(netMode);
            // reset client mode
            text.update(ConstUtils.TEXT_RUN_IN_CLIENT_MODE, new Color(ConstUtils.MENU_ITEM_COLOR));
            systemCapability.removeValue(ConstUtils.KEY_SERVER_IP);
            return;
        }

        systemCapability.startMoleClient(serverIp);
        netMode = NetMode.Client;
        mainMenu.updateNetMode(netMode);
        text.update( ConstUtils.TEXT_RUN_IN_CLIENT_MODE+ " with " + serverIp, new Color(ConstUtils.HIGHLIGHT_ITEM_COLOR));

        if (serverConnected) {
            return;
        }
        clientWaitTask = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                connectServerFailed();
            }
        }, ConstUtils.SERVER_MAX_WAIT_TIME);
    }

    private void connectServerFailed() {
        // reset client status
        netMode = NetMode.None;

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                mainMenu.updateNetMode(netMode);
                netMenu.updateClientItem(ConstUtils.TEXT_RUN_IN_CLIENT_MODE, new Color(ConstUtils.MENU_ITEM_COLOR));
            }
        });

        // reset client mode
        Logger.info(TAG, "connect to server failed");
        systemCapability.showToast("connect to server failed");
    }

    private void showNetMenu() {
        user = systemCapability.getUsrInput(ConstUtils.KEY_USERNAME_PROMPT, ConstUtils.KEY_USERNAME, false);
        if (TextUtils.isEmpty(user)) {
            return;
        }

        mainMenu.setVisible(false);
        netMenu.setVisible(true);
    }

    private void showRanking() {
        // hidden the menu
        mainMenu.setVisible(false);

        // show the rankling list
        if (rankingList != null) {
            rankingList.dispose();
        }

        rankingList = new RankingList(screenWidth, screenHeight, scoreManager, stage, new RankingList.RankingClickListener() {
            @Override
            public void onReturnClicked() {

                menuSound.play();

                systemCapability.log(TAG, "onReturnClicked");

                // hide ranking
                rankingList.setVisible(false);

                // show menu
                mainMenu.setVisible(true);

            }
        });

        rankingList.setVisible(true);
    }

    private void startServer(Text text) {
        if (netMode.equals(NetMode.Server)) {
            // exit server mode
            netMode = NetMode.None;
            mainMenu.updateNetMode(netMode);
            return;
        }

        netMode = NetMode.Server;
        mainMenu.updateNetMode(netMode);
        systemCapability.startMoleServer();

        // change text
        text.update(ConstUtils.TEXT_RUNNING_IN_SERVER_MODE + " IP: " + NetUtils.getLocalIpAddress(), new Color(ConstUtils.HIGHLIGHT_ITEM_COLOR));
    }

    private void stopServer() {

    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        updateWorld();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // draw background image
        batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // draw moles
        for (int i = 0; i < moles.size(); i++) {
            moles.get(i).render(batch);
        }

        batch.end();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    private void updateWorld() {
        world.step(1 / 60f, 6, 2);

        // check if the moles hit
        if (gameRunning && Gdx.input.justTouched()) {
            boolean hit = false;
            Vector2 touchPoint = new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
            for (int i = 0; i < moles.size(); i++) {
                Mole.HitResult result = moles.get(i).hit(touchPoint);
                if (result.equals(Mole.HitResult.HIT_MOLE)) {
                    hitSound.play();

                    int newScore = moles.get(i).getHitScore();
                    if (isLastHit) {
                        newScore += moles.get(i).getComboScore();
                        if (moles.get(i).getComboScore() > 0) {
                            text = "combo +" + moles.get(i).getComboScore();
                            myDataWin.updateText(text);
                        }

                    }

                    if (moles.get(i).isLucky()) {
                        text = "lucky +" + moles.get(i).getHitScore();
                        myDataWin.updateText(text);
                    } else {
                        text = "hit +" + moles.get(i).getHitScore();
                        myDataWin.updateText(text);
                    }

                    score += newScore;
                    myDataWin.updateScore(score);
                    systemCapability.vibrate(50);
                    hit = true;
                    isLastHit = true;

                    // sync score to other player
                    syncScoreToOther();

                } else if (result.equals(Mole.HitResult.HIT_OBSTACLE)) {
                    systemCapability.vibrate(40);
                    isLastHit = true;
                    hit = true;
                    hitSound.play();

                    // sync score to other player
                    syncScoreToOther();
                }

            }

            if (!hit && !moles.isEmpty()) {
                misSound.play();
                isLastHit = false;
                text = "mis";
                myDataWin.updateText(text);
                syncScoreToOther();
            }
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        moleTexture.dispose();
        luckyTexture.dispose();
        world.dispose();
        Timer.instance().clear();
    }

    public void syncScoreToOther() {
        Message message = new Message();
        UUID uuid = UUID.randomUUID();
        message.id = uuid.toString();
        message.score = score;
        message.countdown = countdown;
        message.type = TYPE_SYNC;
        message.user = user;
        message.text = text;
        Gson gson = new Gson();
        if (netMode.equals(NetMode.Server)) {
            systemCapability.sendMsgToClient(gson.toJson(message));
            Logger.info(TAG, "sendMsgToClient:" + gson.toJson(message));
        } else if (netMode.equals(NetMode.Client)) {
            systemCapability.sendMsgToServer(gson.toJson(message));
            Logger.info(TAG, "sendMsgToServer:" + gson.toJson(message));
        }

    }

    private void startGame() {
        Logger.info(TAG, "startGame");
        // the client must be connected
        if (netMode.equals(NetMode.Server) && !clientConnected) {
            systemCapability.showToast("Client is not connected");
            return;
        }

        score = 0;
        user = systemCapability.getUsrInput(ConstUtils.KEY_USERNAME_PROMPT, ConstUtils.KEY_USERNAME, false);
        if (TextUtils.isEmpty(user)) {
            return;
        }

        if (gameRunning) {
            return;
        }

        mainMenu.updateUser(user);

        gameRunning = true;
//        systemCapability.showToast("Have fun, " + user);
        if (myDataWin != null) {
            myDataWin.setVisible(false);
        }

        myDataWin = new DataWin(screenWidth, screenHeight, gameMode.getName(), stage, user, Text.Align.LEFT);
        myDataWin.setVisible(true);

        // only show in competition mode
        if (otherDataWin != null) {
            otherDataWin.setVisible(false);
        }
        otherDataWin = new DataWin(screenWidth, screenHeight, gameMode.getName(), stage, "-", Text.Align.RIGHT);
        otherDataWin.setVisible(false);

        // hide menu
        mainMenu.setVisible(false);

        // show data win
        if (!netMode.equals(NetMode.None)) {
            otherDataWin.setVisible(true);
        }

        // start game if not net mode
        myDataWin.clear();
        isLastHit = false;

        if (netMode.equals(NetMode.None)) {
            gameMode.start();
        } else if (netMode.equals(NetMode.Server)) {

            // send a handshake msg
            Message handshake = new Message();
            handshake.type = TYPE_HANDSHAKE;

            Gson gson = new Gson();
            systemCapability.sendMsgToClient(gson.toJson(handshake));

            myDataWin.updateText("Waiting other player");
            serverWaitTask = Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    myDataWin.updateText("Other player timeout");
                    // stop the server
                    Logger.info(TAG, "stop the mole server for client not connect");
                    netMode = NetMode.None;
                    mainMenu.updateNetMode(netMode);
                    mainMenu.setVisible(true);
                    systemCapability.stopMoleServer();
                    gameRunning = false;
                }
            }, ConstUtils.SERVER_MAX_WAIT_TIME);
        }

    }

    private void processCountDown(int cd) {
        countdown = cd;
        if (cd == 0) {
            moles.clear();
            // show menu
            mainMenu.setVisible(true);

            gameRunning = false;

            // save score
            scoreManager.saveScore(user, score);
        }

        syncScoreToOther();
        myDataWin.updateCountdown(cd);
    }
}
