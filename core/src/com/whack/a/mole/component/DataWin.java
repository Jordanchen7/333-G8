package com.whack.a.mole.component;

import static com.whack.a.mole.utils.ConstUtils.DATA_TEXT_SIZE;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class DataWin {
    private Text userText;
    private Text scoreText;
    private Text countdownText;

    private Text tipText;

    private String mode;

    private float maxTextWidth = 0;
    private int screenWidth;
    private int screenHeight;

    public DataWin (final int screenWidth, final int screenHeight, String mode, Stage stage, String user, Text.Align align) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.mode = mode;
        int score = 0;
        if (Text.Align.LEFT.equals(align)) {
            userText = new Text( mode + ": " + user, 20, screenHeight - 200, DATA_TEXT_SIZE, Color.GREEN, Text.Align.LEFT, stage);
            userText.setVisible(false);
            scoreText = new Text("Score: " + score, 20, screenHeight - 300, DATA_TEXT_SIZE, Color.GREEN, Text.Align.LEFT, stage);
            scoreText.setVisible(false);
            countdownText = new Text("Remain: ", 20, screenHeight - 400, DATA_TEXT_SIZE, Color.GREEN, Text.Align.LEFT, stage);
            countdownText.setVisible(false);
            tipText = new Text("", 20, screenHeight - 500, DATA_TEXT_SIZE, Color.RED, Text.Align.LEFT, stage);
            tipText.setVisible(false);
        } else {
            userText = new Text(mode + ": " + user, screenWidth - 720, screenHeight - 200, DATA_TEXT_SIZE, Color.GREEN, Text.Align.RIGHT, stage);
            userText.setVisible(false);

            int textWidth = (int) userText.getWidth();
            int margin = (int) (textWidth);

            updateMaxWidth(userText);

            userText.resetPos(screenWidth - margin, screenHeight - 200);
            scoreText = new Text("Score: " + score, screenWidth - margin, screenHeight - 300, DATA_TEXT_SIZE, Color.GREEN, Text.Align.RIGHT, stage);
            scoreText.setVisible(false);
            updateMaxWidth(scoreText);

            countdownText = new Text("Remain: ", screenWidth - margin, screenHeight - 400, DATA_TEXT_SIZE, Color.GREEN, Text.Align.RIGHT, stage);
            countdownText.setVisible(false);
            updateMaxWidth(countdownText);

            tipText = new Text("", screenWidth - margin, screenHeight - 500, DATA_TEXT_SIZE, Color.RED, Text.Align.RIGHT, stage);
            tipText.setVisible(false);
        }

    }

    private void updateMaxWidth(Text text) {
        if (maxTextWidth < text.getWidth()) {
            maxTextWidth = text.getWidth();
        }
    }

    public void setVisible(boolean visible) {
        userText.setVisible(visible);
        scoreText.setVisible(visible);
        countdownText.setVisible(visible);
        tipText.setVisible(visible);
    }

    public void updateScore(int newScore) {
        scoreText.update("Score: " + newScore);
    }

    public void updateCountdown(int cd) {
        countdownText.update("Remain: " + cd);
    }

    public void clear() {
        updateScore(0);
    }

    public void updateText(String msg) {
        if (msg == null) {
            return;
        }

        // COPY THE MSG
        tipText.update(msg + " ");
    }

    public void updateUser(String user) {
        userText.update(mode + ": " + user);
        if (userText.getWidth() > maxTextWidth) {
            maxTextWidth = userText.getWidth();
        }

        updatePos();
    }

    private void updatePos() {
        int margin = (int) (maxTextWidth * 1.4);
        userText.resetPos(screenWidth - margin, screenHeight - 200);
        scoreText.resetPos(screenWidth - margin, screenHeight - 300);
        countdownText.resetPos(screenWidth - margin, screenHeight - 400);
        tipText.resetPos(screenWidth - margin, screenHeight - 500);
    }
}
