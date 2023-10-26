package com.whack.a.mole.utils;

import com.whack.a.mole.bridge.SystemCapability;

public class Logger {
    private static SystemCapability systemCapability;
    public static void setSystemCapability(SystemCapability systemCapability) {
        Logger.systemCapability = systemCapability;
    }

    public static void info(String tag, String msg) {
        Logger.systemCapability.log(tag, msg);
    }
}
