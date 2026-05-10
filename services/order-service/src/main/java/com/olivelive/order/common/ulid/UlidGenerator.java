package com.olivelive.order.common.ulid;

import com.github.f4b6a3.ulid.UlidCreator;

public final class UlidGenerator {

    private UlidGenerator() {}

    public static String orderId() {
        return "ord_" + UlidCreator.getMonotonicUlid();
    }

    public static String orderItemId() {
        return "oi_" + UlidCreator.getMonotonicUlid();
    }
}
