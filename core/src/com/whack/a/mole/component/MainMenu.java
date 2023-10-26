package com.whack.a.mole.component;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Timer;
import com.whack.a.mole.WhackAMoleGame;
import com.whack.a.mole.utils.ConstUtils;
import com.whack.a.mole.utils.Logger;

public class MainMenu {

    private static final String TAG = "MainMenu";
    private Text titleText;
    private Text easyText;
    private Text normalText;
    private Text hardText;
    private Text userText;

    private Text rankText;

    private Text netText;

    private boolean clickable;

    public MainMenu(final int screenWidth, final int screenHeight, Stage stage, final MenuClickListener listener) {
        int topOffset = (int) (screenHeight * 0.2f);
        int menuInterval = (screenHeight - topOffset * 2) / 4;
        topOffset += 50;
        titleText = new Text("Whack A Mole!", screenWidth / 2f, screenHeight - topOffset, ConstUtils.TITLE_TEXT_SIZE, new Color(ConstUtils.MENU_TITLE_COLOR), Text.Align.CENTER, stage);
        topOffset += menuInterval;
        easyText = new Text("Easy", screenWidth / 2f - 500, screenHeight - topOffset, ConstUtils.MENU_TEXT_SIZE, new Color(ConstUtils.GAME_MODE_COLOR), Text.Align.CENTER, stage);
        normalText = new Text("Normal", screenWidth / 2f, screenHeight - topOffset, ConstUtils.MENU_TEXT_SIZE, new Color(ConstUtils.GAME_MODE_COLOR), Text.Align.CENTER, stage);
        hardText = new Text("Hard", screenWidth / 2f + 500, screenHeight - topOffset, ConstUtils.MENU_TEXT_SIZE, new Color(ConstUtils.GAME_MODE_COLOR), Text.Align.CENTER, stage);
        topOffset += menuInterval;
        netText = new Text("Network Play", screenWidth / 2f, screenHeight - topOffset, ConstUtils.MENU_TEXT_SIZE, new Color(ConstUtils.MENU_ITEM_COLOR), Text.Align.CENTER, stage);
        topOffset += menuInterval;
        rankText = new Text("Ranking List", screenWidth / 2f, screenHeight - topOffset, ConstUtils.MENU_TEXT_SIZE, new Color(ConstUtils.MENU_ITEM_COLOR), Text.Align.CENTER, stage);
        topOffset += menuInterval;
        userText = new Text("Have fun! ", screenWidth / 2f, screenHeight - topOffset, ConstUtils.MENU_TEXT_SIZE, new Color(ConstUtils.EXIT_ITEM_COLOR), Text.Align.CENTER, stage);

        userText.setListener(new EventListener() {

            @Override
            public boolean handle(Event event) {
                if (!clickable) {
                    return true;
                }

                listener.onMenuClicked(MenuItem.CHANGE_USER);
                return true;
            }
        });

        easyText.setListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (!clickable) {
                    return true;
                }

                listener.onMenuClicked(MenuItem.EASY_MODE);
                return true;
            }
        });

        normalText.setListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (!clickable) {
                    return true;
                }

                listener.onMenuClicked(MenuItem.NORMAL_MODE);
                return true;
            }
        });

        hardText.setListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (!clickable) {
                    return true;
                }

                listener.onMenuClicked(MenuItem.HARD_MODE);
                return true;
            }
        });

        rankText.setListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (!clickable) {
                    return true;
                }

                listener.onMenuClicked(MenuItem.RANKING);
                return true;
            }
        });

        netText.setListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (!clickable) {
                    return true;
                }

                listener.onMenuClicked(MenuItem.NET_PLAY);
                return true;
            }
        });

        setVisible(true);
    }

    public void updateNetMode(WhackAMoleGame.NetMode netMode) {
        if (netMode.equals(WhackAMoleGame.NetMode.Client)) {
            netText.update("Run As Client", Color.YELLOW);
        } else if (netMode.equals(WhackAMoleGame.NetMode.Server)) {
            netText.update("Run As Server", Color.YELLOW);
        } else {
            netText.update("Network Play", new Color(ConstUtils.MENU_ITEM_COLOR));
        }
    }

    public void setVisible(boolean visible) {
        Logger.info(TAG, "setVisible: visible=" + visible);
        if (!visible) {
            clickable = false;
        } else {
            // the text can be clicked until 200ms later
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    clickable = true;
                }
            }, 0.2f);
        }

        // hidden menus
        easyText.setVisible(visible);
        normalText.setVisible(visible);
        hardText.setVisible(visible);
        userText.setVisible(visible);
        titleText.setVisible(visible);
        rankText.setVisible(visible);
        netText.setVisible(visible);
    }

    public void updateUser(String user) {
        userText.update("Have fun! " + user, new Color(ConstUtils.MENU_ITEM_COLOR));
    }

    public enum MenuItem {
        EASY_MODE,
        NORMAL_MODE,
        HARD_MODE,
        RANKING,
        NET_PLAY,
        CHANGE_USER
    }

    public abstract static class MenuClickListener {
        public abstract void onMenuClicked(MenuItem item);
    }
}
