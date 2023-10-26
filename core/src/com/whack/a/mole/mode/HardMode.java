package com.whack.a.mole.mode;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.World;
import com.whack.a.mole.component.Mole;

public class HardMode extends GameMode {
    private enum HardModeState {
        READY,
        STAGE1,
        STAGE2,
        STAGE3,
        FINISH
    }

    // stage1 config
    private static ModeParam stage1Param;

    // stage2 config
    private static ModeParam stage2Param;

    // stage3 config
    private static ModeParam stage3Param;

    private HardModeState state;

    public HardMode(int gameAreaWidth, int gameAreaHeight, World world, Texture moleTexture, Texture luckyTexture, Texture obstacleTexture, MoleReceiver moleReceiver) {
        super(gameAreaWidth, gameAreaHeight, world, moleTexture, luckyTexture, obstacleTexture, moleReceiver);
        // set stage1 param
        stage1Param = new ModeParam();
        stage1Param.moleNum = 10;
        stage1Param.minInterval = 0.6f;
        stage1Param.maxInterval = 0.8f;
        stage1Param.minDuration = 0.8f;
        stage1Param.maxDuration = 0.8f;
        stage1Param.hitScore = 30;
        stage1Param.comboScore = 2;
        stage1Param.obstacleRate = 0.15f;

        stage1Param.luckyRate = 0.3f;
        stage1Param.luckPeriod = 1.0f;
        stage1Param.luckyScore = 150;

        // set state2 param
        stage2Param = new ModeParam();
        stage2Param.moleNum = 15;
        stage2Param.minInterval = 0.4f;
        stage2Param.maxInterval = 0.6f;
        stage2Param.minDuration = 0.7f;
        stage2Param.maxDuration = 0.7f;
        stage2Param.hitScore = 35;
        stage2Param.comboScore = 3;
        stage2Param.obstacleRate = 0.2f;

        stage2Param.luckyRate = 0.35f;
        stage2Param.luckPeriod = 1.0f;
        stage2Param.luckyScore = 175;

        // set state3 param
        stage3Param = new ModeParam();
        stage3Param.moleNum = 20;
        stage3Param.minInterval = 0.3f;
        stage3Param.maxInterval = 0.5f;
        stage3Param.minDuration = 0.6f;
        stage3Param.maxDuration = 0.6f;
        stage3Param.hitScore = 40;
        stage3Param.comboScore = 4;
        stage3Param.obstacleRate = 0.25f;

        stage3Param.luckyRate = 0.4f;
        stage3Param.luckPeriod = 1.0f;
        stage3Param.luckyScore = 200;

        state = HardModeState.READY;
        totalMoleNum = stage1Param.moleNum + stage2Param.moleNum + stage3Param.moleNum;
    }

    @Override
    public void start() {
        stopped = false;
        state = HardModeState.STAGE1;
        remainMoleNum = totalMoleNum;
        moleReceiver.onCountdown(remainMoleNum);

        stage1Param.alreadyLucky = false;
        stage2Param.alreadyLucky = false;
        stage3Param.alreadyLucky = false;
        genMole(stage1Param.moleNum);

    }

    @Override
    protected void setupMole(Mole mole) {
        if (HardModeState.STAGE1.equals(state)) {
            initMole(mole, stage1Param);
        } else if (HardModeState.STAGE2.equals(state)) {
            initMole(mole, stage2Param);
        } else if (HardModeState.STAGE3.equals(state)) {
            initMole(mole, stage3Param);
        }
    }

    @Override
    protected float getInterval(int seq) {
        if (HardModeState.STAGE1.equals(state)) {
            return stage1Param.maxInterval - (stage1Param.maxInterval - stage1Param.minInterval) / stage1Param.moleNum * (stage1Param.moleNum - seq);
        } else if (HardModeState.STAGE2.equals(state)) {
            return stage2Param.maxInterval - (stage2Param.maxInterval - stage2Param.minInterval) / stage2Param.moleNum * (stage2Param.moleNum - seq);
        } else if (HardModeState.STAGE3.equals(state)) {
            return stage3Param.maxInterval - (stage3Param.maxInterval - stage3Param.minInterval) / stage3Param.moleNum * (stage3Param.moleNum - seq);
        }

        return 0;
    }

    @Override
    protected void onAllMoleGen() {
        if (HardModeState.STAGE1.equals(state)) {
            // start stage2
            state = HardModeState.STAGE2;
            genMole(stage2Param.moleNum);
        } else if (HardModeState.STAGE2.equals(state)) {
            state = HardModeState.STAGE3;
            genMole(stage3Param.moleNum);
        } else if (HardModeState.STAGE3.equals(state)) {
            state = HardModeState.FINISH;
        }
    }

    @Override
    public String getName() {
        return "Hard";
    }
}
