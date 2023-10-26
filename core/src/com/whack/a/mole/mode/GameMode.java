package com.whack.a.mole.mode;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Timer;
import com.whack.a.mole.component.Mole;

import java.util.Random;

public abstract class GameMode {

    private static final int moleWidth = 300;
    private static final int moleHeight = 300;

    protected int gameAreaWidth;
    protected int gameAreaHeight;

    protected World world;
    protected Texture moleTexture;
    protected Texture luckyTexture;
    protected Texture obstacleTexture;
    protected MoleReceiver moleReceiver;

    private int moleId;
    protected int totalMoleNum;
    protected int remainMoleNum;

    protected boolean stopped;

    public GameMode(int gameAreaWidth, int gameAreaHeight, World world, Texture moleTexture, Texture luckyTexture, Texture obstacleTexture, MoleReceiver moleReceiver) {
        this.gameAreaHeight = gameAreaHeight;
        this.gameAreaWidth = gameAreaWidth;
        this.world = world;
        this.moleTexture = moleTexture;
        this.obstacleTexture = obstacleTexture;
        this.moleReceiver = moleReceiver;
        this.luckyTexture = luckyTexture;
    }

    public abstract void start();

    public void stop() {
        stopped = true;
    }

    private Mole createMole() {
        Random random = new Random();
        float x = random.nextInt(gameAreaWidth);
        float y = random.nextInt(gameAreaHeight) + moleHeight / 2.0f;
        Mole mole = new Mole(moleId, world, x, y, moleWidth, moleHeight, moleTexture, obstacleTexture);
        moleId++;
        // set special parameter of mole
        setupMole(mole);
        return mole;
    }

    protected abstract void setupMole(Mole mole);

    protected abstract float getInterval(int seq);

    protected abstract void onAllMoleGen();

    protected void genMole(final int moleNum) {
        if (moleNum <= 0) {
            onAllMoleGen();
            return;
        }

        if (stopped) {
            moleReceiver.onCountdown(0);
            return;
        }

        Timer.Task task = new Timer.Task() {
            @Override
            public void run() {
                // create a mole
                Mole mole = createMole();
                moleReceiver.onMoleCreated(mole);

                // decrease mole num
                onCountdown();

                // generate next mole
                genMole(moleNum - 1);
            }
        };

        Timer.schedule(task, getInterval(moleId));
    }

    public void onCountdown() {
        remainMoleNum--;
        moleReceiver.onCountdown(remainMoleNum);
    }

    public abstract static class MoleReceiver {
        public abstract void onMoleCreated(Mole mole);

        public abstract void onCountdown(int cd);
    }

    protected void initMole(Mole mole, ModeParam modeParam) {
        mole.setVisible(true);
        mole.setDuration(MathUtils.random(modeParam.minDuration, modeParam.maxDuration));
        mole.setHitScore(modeParam.hitScore);
        mole.setComboScore(modeParam.comboScore);
        // check if lucky
        if (!modeParam.alreadyLucky && MathUtils.random(0, 1.0f) <= modeParam.luckyRate) {
            mole.setHitScore(modeParam.luckyScore);
            mole.setDuration(modeParam.luckPeriod);
            mole.setLucky(true);
            mole.setMoleTexture(luckyTexture);
            modeParam.alreadyLucky = true;
        }

        // check if with obstacle
        if (MathUtils.random() <= modeParam.obstacleRate) {
            mole.setHasObstacle(true);
        }
    }

    public abstract String getName();
}
