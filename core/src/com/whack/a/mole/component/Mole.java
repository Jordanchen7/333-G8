package com.whack.a.mole.component;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Timer;
import com.google.gson.annotations.Expose;


public class Mole {
    public enum HitResult {
        MISS,
        HIT_OBSTACLE,
        HIT_MOLE
    }

    @Expose
    public int id;

    @Expose
    public float moleWidth;
    @Expose
    public float moleHeight;

    @Expose
    public boolean visible;

    @Expose(serialize = false, deserialize = false)
    private World world;
    @Expose(serialize = false, deserialize = false)
    private Texture moleTexture;
    @Expose(serialize = false, deserialize = false)
    private Texture obstacleTexture;

    @Expose
    public float x;
    @Expose
    public float y;

    @Expose
    public int hitScore;
    @Expose
    public int comboScore;

    @Expose
    public boolean isLucky;

    @Expose
    public boolean hasObstacle;

    @Expose(serialize = false, deserialize = false)
    private Timer.Task task;

    @Expose
    public float duration;

    public Mole() {

    }

    public Mole(int id, World world, float x, float y, float width, float height, Texture moleTexture, Texture obstacleTexture) {
        this.id = id;
        this.moleWidth = width;
        this.moleHeight = height;

        this.world = world;
        this.x = x;
        this.y = y;

        this.visible = true;

        createMoleBody(x, y);
        this.moleTexture = moleTexture;
        this.obstacleTexture = obstacleTexture;
    }

    public void setDuration(float duration) {
        this.duration = duration;
        final boolean[] taskExecuted = {false};
        task = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (!taskExecuted[0]) {
                    setVisible(false);
                    taskExecuted[0] = true;
                }
            }
        }, duration);
    }

    public void initMole(World world, Texture moleTexture, Texture obstacleTexture) {
        this.world = world;
        this.moleTexture = moleTexture;
        this.obstacleTexture = obstacleTexture;
        createMoleBody(x, y);
        setDuration(duration);
    }

    private void createMoleBody(float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x, y);
        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(moleWidth / 2, moleHeight / 2);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        body.createFixture(fixtureDef);

        shape.dispose();
    }

    public void render(SpriteBatch batch) {
        float moleX = getPosX() - getMoleWidth() / 2;
        float moleY = getPosY() - getMoleHeight() / 2;
        if (isVisible()) {
            batch.draw(moleTexture, moleX, moleY, getMoleWidth(), getMoleHeight());
        }

        if (hasObstacle) {
            batch.draw(obstacleTexture, moleX, moleY, getMoleWidth(), getMoleHeight());
        }
    }

    public HitResult hit(Vector2 touchPoint) {

        if (isVisible()) {
            float moleX = getPosX() - getMoleWidth() / 2;
            float moleY = getPosY() - getMoleHeight() / 2;

            if (touchPoint.x >= moleX && touchPoint.x <= moleX + getMoleWidth() &&
                    touchPoint.y >= moleY && touchPoint.y <= moleY + getMoleHeight()) {

                if (hasObstacle) {
                    hasObstacle = false;
                    // delay the task
                    if (task != null) {
                        task.cancel();
                    }

                    setDuration(duration / 2);
                    return HitResult.HIT_OBSTACLE;
                }

                // mole is hit
                setVisible(false);
                return HitResult.HIT_MOLE;

            }
        }

        return HitResult.MISS;
    }

    public float getPosX() {
        return x;
    }

    public float getPosY() {
        return y;
    }

    public float getMoleWidth() {
        return moleWidth;
    }

    public float getMoleHeight() {
        return moleHeight;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {

        if (!visible) {
            hasObstacle = false;
        }

        this.visible = visible;
    }

    public void setHasObstacle(boolean hasObstacle) {
        this.hasObstacle = hasObstacle;
    }

    public int getId() {
        return id;
    }

    public int getHitScore() {
        return hitScore;
    }

    public void setHitScore(int hitScore) {
        this.hitScore = hitScore;
    }

    public int getComboScore() {
        return comboScore;
    }

    public void setComboScore(int comboScore) {
        this.comboScore = comboScore;
    }

    public boolean isLucky() {
        return isLucky;
    }

    public void setLucky(boolean lucky) {
        isLucky = lucky;
    }

    public void setMoleTexture(Texture texture) {
        this.moleTexture = texture;
    }
}
