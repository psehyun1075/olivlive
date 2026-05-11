package com.olivelive.livecontrol.common.ulid;

import com.github.f4b6a3.ulid.UlidCreator;

public final class UlidGenerator {

    private UlidGenerator() {}

    public static String sessionId() {
        return "live_" + UlidCreator.getMonotonicUlid();
    }
}
