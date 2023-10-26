package com.whack.a.mole.component;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Timer;
import com.whack.a.mole.utils.ConstUtils;

public class NetMenu {
    private Text titleText;
    private Text serverText;
    private Text clientText;
    private Text returnText;

    private boolean clickable;

    public NetMenu(final int screenWidth, final int screenHeight, Stage stage, final NetMenuClickListener listener) {
        int topOffset = (int) (screenHeight * 0.2f);
        int menuInterval = (screenHeight - topOffset * 2) / 4;
        topOffset += 50;
        titleText = new Text("Play with your friend!", screenWidth / 2f, screenHeight - topOffset, ConstUtils.TITLE_TEXT_SIZE, new Color(ConstUtils.MENU_TITLE_COLOR), Text.Align.CENTER, stage);
        topOffset += menuInterval;

        serverText = new Text("Run As Server", screenWidth / 2f, screenHeight - topOffset, ConstUtils.MENU_TEXT_SIZE, new Color(ConstUtils.MENU_ITEM_COLOR), Text.Align.CENTER, stage);
        topOffset += menuInterval;

        clientText = new Text("Run As Client", screenWidth / 2f, screenHeight - topOffset, ConstUtils.MENU_TEXT_SIZE, new Color(ConstUtils.MENU_ITEM_COLOR), Text.Align.CENTER, stage);
        topOffset += menuInterval;
        returnText = new Text("Return to Menu", screenWidth / 2f, screenHeight - topOffset, ConstUtils.MENU_TEXT_SIZE, new Color(ConstUtils.EXIT_ITEM_COLOR), Text.Align.CENTER, stage);

        serverText.setListener(new EventListener() {

            @Override
            public boolean handle(Event event) {
                if (!clickable) {
                    return true;
                }

                serverText.update(ConstUtils.TEXT_RUN_IN_SERVER_MODE, new Color(ConstUtils.MENU_ITEM_COLOR));
                // reset client text
                clientText.update(ConstUtils.TEXT_RUN_IN_CLIENT_MODE, new Color(ConstUtils.MENU_ITEM_COLOR));

                listener.onNetMenuClicked(NetMenuItem.SERVER_MODE, serverText);
                return true;
            }
        });


        clientText.setListener(new EventListener() {

            @Override
            public boolean handle(Event event) {
                if (!clickable) {
                    return true;
                }

                // reset server text
                serverText.update(ConstUtils.TEXT_RUN_IN_SERVER_MODE, new Color(ConstUtils.MENU_ITEM_COLOR));

                // update client text
                clientText.update(ConstUtils.TEXT_RUNNING_IN_CLIENT_MODE, new Color(ConstUtils.HIGHLIGHT_ITEM_COLOR));

                listener.onNetMenuClicked(NetMenuItem.CLIENT_MODE, clientText);
                return true;
            }
        });

        returnText.setListener(new EventListener() {

            @Override
            public boolean handle(Event event) {
                if (!clickable) {
                    return true;
                }

                listener.onNetMenuClicked(NetMenuItem.RETURN, returnText);
                return true;
            }
        });

        setVisible(false);
    }

    public void setVisible(boolean visible) {
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

        titleText.setVisible(visible);
        serverText.setVisible(visible);
        clientText.setVisible(visible);
        returnText.setVisible(visible);
    }

    public void updateClientItem(String content, Color color) {
        clientText.update(content, color);
    }

    public enum NetMenuItem {
        CLIENT_MODE,
        SERVER_MODE,
        RETURN
    }

    public abstract static class NetMenuClickListener {
        public abstract void onNetMenuClicked(NetMenuItem item, Text text);
    }
}
