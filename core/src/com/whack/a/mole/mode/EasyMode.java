package com.whack.a.mole.mode;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.World;
import com.whack.a.mole.component.Mole;


public class EasyMode extends GameMode {
    private enum EasyModeState {
        READY,
        STAGE1,
        STAGE2,
        FINISH
    }
    // stage1 config
    private static ModeParam stage1Param;

    // stage2 config
    private static ModeParam stage2Param;

    private EasyModeState state;

    public EasyMode(int gameAreaWidth, int gameAreaHeight, World world, Texture moleTexture, Texture luckyTexture, Texture obstacleTexture, MoleReceiver moleReceiver) {
        super(gameAreaWidth, gameAreaHeight, world, moleTexture, luckyTexture, obstacleTexture, moleReceiver);

        // set stage1 param
        stage1Param = new ModeParam();
        stage1Param.moleNum = 10;
        stage1Param.minInterval = 1.4f;
        stage1Param.maxInterval = 1.6f;
        stage1Param.minDuration = 1.0f;
        stage1Param.maxDuration = 1.1f;
        stage1Param.hitScore = 10;
        stage1Param.comboScore = 0;
        stage1Param.obstacleRate = 0.0f;

        stage1Param.luckyRate = 0.1f;
        stage1Param.luckPeriod = 2.0f;
        stage1Param.luckyScore = 50;

        // set state2 param
        stage2Param = new ModeParam();
        stage2Param.moleNum = 15;
        stage2Param.minInterval = 1.2f;
        stage2Param.maxInterval = 1.4f;
        stage2Param.minDuration = 1.0f;
        stage2Param.maxDuration = 1.1f;
        stage2Param.hitScore = 15;
        stage2Param.comboScore = 1;
        stage2Param.obstacleRate = 0.1f;

        stage2Param.luckyRate = 0.15f;
        stage2Param.luckPeriod = 2.0f;
        stage2Param.luckyScore = 75;

        state = EasyModeState.READY;
        totalMoleNum = stage1Param.moleNum + stage2Param.moleNum;
    }

    @Override
    public void start() {
        stopped = false;

        // start stage 1
        state = EasyModeState.STAGE1;
        remainMoleNum = totalMoleNum;
        moleReceiver.onCountdown(remainMoleNum);

        stage1Param.alreadyLucky = false;
        stage2Param.alreadyLucky = false;
        genMole(stage1Param.moleNum);
    }

    @Override
    protected void setupMole(Mole mole) {
        if (EasyModeState.STAGE1.equals(state)) {
            initMole(mole, stage1Param);
        } else if (EasyModeState.STAGE2.equals(state)) {
            initMole(mole, stage2Param);
        }
    }

    @Override
    protected float getInterval(int seq) {
        if (EasyModeState.STAGE1.equals(state)) {
            return stage1Param.maxInterval - (stage1Param.maxInterval - stage1Param.minInterval) / stage1Param.moleNum * (stage1Param.moleNum - seq);
        } else if (EasyModeState.STAGE2.equals(state)) {
            return stage2Param.maxInterval - (stage2Param.maxInterval - stage2Param.minInterval) / stage2Param.moleNum * (stage2Param.moleNum - seq);
        }

        return 0;
    }

    @Override
    protected void onAllMoleGen() {
        if (EasyModeState.STAGE1.equals(state)) {
            // start stage2
            state = EasyModeState.STAGE2;
            genMole(stage2Param.moleNum);
        } else if (EasyModeState.STAGE2.equals(state)) {
            state = EasyModeState.FINISH;
        }
    }

    @Override
    public String getName() {
        return "Easy";
    }

}
