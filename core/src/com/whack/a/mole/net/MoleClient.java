package com.whack.a.mole.net;

import com.whack.a.mole.bridge.MoleCallback;
import com.whack.a.mole.utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MoleClient {

    private static final String TAG = "MoleClient";

    public String serverIp;

    private final int serverPort;

    private final MoleCallback callback;
    private Socket socket;

    public MoleClient(String serverIp, int serverPort, MoleCallback callback) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.callback = callback;
    }

    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(serverIp, serverPort);
                    Logger.info(TAG, "connect to server success");
                    callback.onServerConnected();

                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String clientMessage;
                    while ((clientMessage = in.readLine()) != null) {
                        Logger.info(TAG, "read message: " + clientMessage);
                        callback.onServerMessage(clientMessage);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    callback.onServerFailed();
                }
            }
        }).start();

    }

    public void sendMessage(String msg) {
        if (socket == null) {
            Logger.info(TAG, "socket is null");
            return;
        }

        try {
            Logger.info(TAG, "sendMessage: " + msg);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(msg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
