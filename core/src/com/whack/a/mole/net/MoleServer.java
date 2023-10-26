package com.whack.a.mole.net;

import com.whack.a.mole.bridge.MoleCallback;
import com.whack.a.mole.utils.ConstUtils;
import com.whack.a.mole.utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class MoleServer {
    private static final String TAG = "MoleServer";
    private final int port;

    private final MoleCallback callback;

    private boolean stopped = false;

    private ServerSocket serverSocket = null;

    private Socket clientSocket = null;

    public MoleServer(int port, MoleCallback callback) {
        this.callback = callback;
        this.port = port;
    }

    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (serverSocket != null) {
                        serverSocket.close();
                        serverSocket = null;
                    }
                    serverSocket = new ServerSocket(port);
                    Logger.info(TAG, "create server socket success");
                    while (!stopped) {
                        clientSocket = serverSocket.accept();
                        Logger.info(TAG, "new client coming");

                        callback.onClientConnected();

                        // create a thread to handle client request
                        ClientHandler clientHandler = new ClientHandler(clientSocket);
                        new Thread(clientHandler).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (serverSocket != null) {
                        try {
                            serverSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();

    }

    public void sendMessage(String message) {
        try {
            Logger.info(TAG, "sendMessage: " + message);
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println(message);
        } catch (IOException e) {
            e.printStackTrace();
            callback.onClientClosed();
        }

    }

    public void stop() {
        stopped = true;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            serverSocket = null;
        }
    }

    class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            ) {
                String clientMessage;
                while ((clientMessage = in.readLine()) != null) {
                    Logger.info(TAG, "read message: " + clientMessage);
                    callback.onClientMessage(clientMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
