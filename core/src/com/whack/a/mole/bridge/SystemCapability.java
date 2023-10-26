package com.whack.a.mole.bridge;

public interface SystemCapability {

    void quit();

    void vibrate(int duration);

    void writeValue(String key, String value);

    String readValue(String key);

    void removeValue(String key);

    void log(String tag, String msg);

    void requestPermission();

    String getUsrInput(String prompt, String key, boolean mustShowDialog);

    void showToast(String msg);

    void setMoleCallback(MoleCallback callback);

    void startMoleServer();

    void stopMoleServer();

    void startMoleClient(String ip);

    void sendMsgToClient(String msg);

    void sendMsgToServer(String msg);
}
