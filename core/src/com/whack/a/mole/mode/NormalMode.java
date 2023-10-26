package com.whack.a.mole.mode;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.World;
import com.whack.a.mole.component.Mole;

public class NormalMode extends GameMode {
    private enum NormalModeState {
        READY,
        STAGE1,
        STAGE2,
        FINISH
    }

    // stage1 config
    private static ModeParam stage1Param;

    // stage2 config
    private static ModeParam stage2Param;

    private NormalModeState state;

    public NormalMode(int gameAreaWidth, int gameAreaHeight, World world, Texture moleTexture, Texture luckyTexture, Texture obstacleTexture, MoleReceiver moleReceiver) {
        super(gameAreaWidth, gameAreaHeight, world, moleTexture, luckyTexture, obstacleTexture, moleReceiver);

        // set stage1 param
        stage1Param = new ModeParam();
        stage1Param.moleNum = 10;
        stage1Param.minInterval = 0.8f;
        stage1Param.maxInterval = 1.0f;
        stage1Param.minDuration = 1.0f;
        stage1Param.maxDuration = 1.1f;
        stage1Param.hitScore = 20;
        stage1Param.comboScore = 1;
        stage1Param.obstacleRate = 0.1f;

        stage1Param.luckyRate = 0.2f;
        stage1Param.luckPeriod = 1.5f;
        stage1Param.luckyScore = 100;

        // set state2 param
        stage2Param = new ModeParam();
        stage2Param.moleNum = 15;
        stage2Param.minInterval = 0.6f;
        stage2Param.maxInterval = 0.8f;
        stage2Param.minDuration = 0.8f;
        stage2Param.maxDuration = 0.9f;
        stage2Param.hitScore = 25;
        stage2Param.comboScore = 2;
        stage2Param.obstacleRate = 0.15f;

        stage2Param.luckyRate = 0.25f;
        stage2Param.luckPeriod = 1.5f;
        stage2Param.luckyScore = 125;

        state = NormalModeState.READY;
        totalMoleNum = stage1Param.moleNum + stage2Param.moleNum;
    }

    @Override
    public void start() {
        stopped = false;
        state = NormalModeState.STAGE1;
        remainMoleNum = totalMoleNum;
        moleReceiver.onCountdown(remainMoleNum);

        stage1Param.alreadyLucky = false;
        stage2Param.alreadyLucky = false;
        genMole(stage1Param.moleNum);
    }

    @Override
    protected void setupMole(Mole mole) {
        if (NormalModeState.STAGE1.equals(state)) {
            initMole(mole, stage1Param);
        } else if (NormalModeState.STAGE2.equals(state)) {
            initMole(mole, stage2Param);
        }
    }

    @Override
    protected float getInterval(int seq) {
        if (NormalModeState.STAGE1.equals(state)) {
            return stage1Param.maxInterval - (stage1Param.maxInterval - stage1Param.minInterval) / stage1Param.moleNum * (stage1Param.moleNum - seq);
        } else if (NormalModeState.STAGE2.equals(state)) {
            return stage2Param.maxInterval - (stage2Param.maxInterval - stage2Param.minInterval) / stage2Param.moleNum * (stage2Param.moleNum - seq);
        }

        return 0;
    }

    @Override
    protected void onAllMoleGen() {
        if (NormalModeState.STAGE1.equals(state)) {
            // start stage2
            state = NormalModeState.STAGE2;
            genMole(stage2Param.moleNum);
        } else if (NormalModeState.STAGE2.equals(state)) {
            state = NormalModeState.FINISH;
        }
    }

    @Override
    public String getName() {
        return "Normal";
    }
}
