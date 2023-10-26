package com.whack.a.mole.mode;

import static com.whack.a.mole.data.Message.TYPE_GEN_MOLE;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.World;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.whack.a.mole.component.Mole;
import com.whack.a.mole.data.Message;

public class NetMode extends GameMode {

    public NetMode(int gameAreaWidth, int gameAreaHeight, World world, Texture moleTexture, Texture luckyTexture, Texture obstacleTexture, MoleReceiver moleReceiver) {
        super(gameAreaWidth, gameAreaHeight, world, moleTexture, luckyTexture, obstacleTexture, moleReceiver);
    }

    @Override
    public void start() {

    }

    @Override
    protected void setupMole(Mole mole) {

    }

    @Override
    protected float getInterval(int seq) {
        return 0;
    }

    @Override
    protected void onAllMoleGen() {

    }

    public void processMsg(Message message) {
        if (message.type.equals(TYPE_GEN_MOLE)) {
            String moleStr = message.mole;
            Gson gson = new GsonBuilder()
                    .setExclusionStrategies(new BasicTypeExclusionStrategy())
                    .create();

            Mole mole = gson.fromJson(moleStr, Mole.class);
            moleReceiver.onMoleCreated(mole);
        }
    }

    @Override
    public String getName() {
        return "Net";
    }
}
