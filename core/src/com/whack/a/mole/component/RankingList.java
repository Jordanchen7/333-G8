package com.whack.a.mole.component;

import static com.whack.a.mole.utils.ConstUtils.DATA_TEXT_SIZE;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.whack.a.mole.data.ScoreManager;
import com.whack.a.mole.utils.ConstUtils;

import java.util.ArrayList;
import java.util.List;

public class RankingList {
    private Text rankTitle;
    private List<Text> items = new ArrayList<>();

    private Text returnText;

    private RankingClickListener listener;

    public RankingList(int screenWidth, int screenHeight, ScoreManager scoreManager, Stage stage, final RankingClickListener listener) {
        this.listener = listener;

        int topOffset = (int) (screenHeight * 0.2f);
        int menuInterval = (screenHeight - topOffset * 2) / 6;
        topOffset += 50;

        rankTitle = new Text("Top Users", screenWidth / 2f, screenHeight - topOffset, ConstUtils.TITLE_TEXT_SIZE, new Color(ConstUtils.MENU_TITLE_COLOR), Text.Align.CENTER, stage);
        topOffset += menuInterval;
        List<ScoreManager.RankItem> topList = scoreManager.getTopList();

        for (int i = 0; i < 5 && i < topList.size(); i++) {
            String formattedString = String.format("%" + 10 + "s", topList.get(i).name);

            Text text = new Text(formattedString+ " : " + topList.get(i).score, screenWidth / 2f, screenHeight - topOffset, DATA_TEXT_SIZE, Color.ORANGE, Text.Align.CENTER, stage);
            topOffset += menuInterval;
            items.add(text);
        }

        returnText = new Text("Return to Menu", screenWidth / 2f, screenHeight - topOffset, ConstUtils.MENU_TEXT_SIZE, new Color(ConstUtils.MENU_ITEM_COLOR), Text.Align.CENTER, stage);
        returnText.setListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                listener.onReturnClicked();
                return true;
            }
        });
    }

    public void setVisible(boolean visible) {
        rankTitle.setVisible(visible);
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setVisible(visible);
        }
        returnText.setVisible(visible);
    }

    public void dispose() {
        rankTitle.dispose();
        for (int i = 0; i < items.size(); i++) {
            items.get(i).dispose();
        }
    }

    public abstract static class RankingClickListener {
        public abstract void onReturnClicked();
    }
}
