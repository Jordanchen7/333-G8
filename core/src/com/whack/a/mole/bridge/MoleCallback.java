package com.whack.a.mole.bridge;

import com.whack.a.mole.utils.Logger;

public abstract class MoleCallback {

    public abstract void onClientConnected();

    public abstract void onClientClosed();

    public abstract void onClientMessage(String msg);

    public abstract void onServerConnected();

    public abstract void onServerFailed();

    public abstract void onServerMessage(String msg);

    public abstract void onUserChanged(String newUser);

    public abstract void onServerChanged(String newServer);

}
