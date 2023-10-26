package com.whack.a.mole.data;

import com.whack.a.mole.component.Mole;

public class Message {

    public static final String TYPE_HANDSHAKE = "handshake";
    public static final String TYPE_ACK = "ack";
    public static final String TYPE_SYNC = "sync";

    public static final String TYPE_GEN_MOLE = "gen_mole";

    public String id;
    public String user;
    public String type;
    public int score;

    public int countdown;

    public String text;

    public String mole;
}
