package com.olivelive.catalog.liveproduct.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// Active when catalog.live-control.stub=true (default).
// Replace with an HTTP implementation once live-control-service is ready.
@Component
@ConditionalOnProperty(name = "catalog.live-control.stub", havingValue = "true", matchIfMissing = true)
public class StubLiveControlClient implements LiveControlClient {

    @Override
    public void validateSessionAttachable(String liveSessionId) {
        // Always passes in stub mode.
    }
}
