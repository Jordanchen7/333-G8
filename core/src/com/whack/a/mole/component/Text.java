package com.whack.a.mole.component;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class Text {

    private int size;
    private float x;
    private float y;

    private Align align;

    private long lastClick;

    private long minClickInterval = 200;

    public enum Align {
        LEFT,
        CENTER,
        RIGHT
    }

    private Label label;

    public Text(String content, float x, float y, int size, Color color, Align align, Stage stage) {
        this(content, x, y, size, color, align, stage, null);
    }

    public Text(String content, float x, float y, int size, Color color, Align align, Stage stage, EventListener listener) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("AlibabaPuHuiTi-3-55-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        this.size = size;
        this.x = x;
        this.y = y;
        this.align = align;
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = color;
        label = new Label(content, labelStyle);
        if (align.equals(Align.CENTER)) {
            x -= label.getWidth() / 2;
        }
        label.setPosition(x, y);
        stage.addActor(label);

        if (listener != null) {
            label.addListener(listener);
        }

    }

    public float getWidth() {
        return label.getPrefWidth();
    }

    public void update(String content) {
        label.setText(content);
    }

    public void resetPos(float x, float y) {
        this.x = x;
        this.y = y;
        label.setPosition(x, y);
    }

    public void update(String content, Color color) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("AlibabaPuHuiTi-3-55-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = color;
        label.setStyle(labelStyle);
        label.setText(content);
        label.setWidth(label.getPrefWidth());
        label.setHeight(label.getPrefHeight());

        float newX = x;
        if (align.equals(Align.CENTER)) {
            newX -= label.getWidth() / 2;
        }
        label.setPosition(newX, y);
    }

    public void setListener(final EventListener listener) {
        if (listener != null) {
            label.addListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    if (System.currentTimeMillis() - lastClick < minClickInterval) {
                        return true;
                    }
                    listener.handle(event);
                    lastClick = System.currentTimeMillis();
                    return true;
                }
            });
        }
    }

    public void setVisible(boolean visible) {
        label.setVisible(visible);
    }

    public void dispose() {

    }
}
