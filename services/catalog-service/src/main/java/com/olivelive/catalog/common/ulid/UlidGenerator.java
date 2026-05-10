package com.olivelive.catalog.common.ulid;

import com.github.f4b6a3.ulid.UlidCreator;

public final class UlidGenerator {

    private UlidGenerator() {}

    public static String productId() {
        return "prod_" + UlidCreator.getMonotonicUlid();
    }

    public static String liveProductId() {
        return "lp_" + UlidCreator.getMonotonicUlid();
    }
}
