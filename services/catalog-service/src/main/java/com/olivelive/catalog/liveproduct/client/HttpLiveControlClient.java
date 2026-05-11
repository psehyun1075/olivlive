package com.olivelive.catalog.liveproduct.client;

import com.olivelive.catalog.common.exception.CatalogException;
import com.olivelive.catalog.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Component
@ConditionalOnProperty(name = "catalog.live-control.stub", havingValue = "false")
public class HttpLiveControlClient implements LiveControlClient {

    private final RestClient restClient;

    public HttpLiveControlClient(
            RestClient.Builder restClientBuilder,
            @Value("${catalog.live-control.base-url}") String baseUrl,
            @Value("${catalog.live-control.connect-timeout-ms:2000}") int connectTimeoutMs,
            @Value("${catalog.live-control.read-timeout-ms:5000}") int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        this.restClient = restClientBuilder.baseUrl(baseUrl).requestFactory(factory).build();
    }

    // Package-private: for unit tests only
    HttpLiveControlClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public void validateSessionAttachable(String liveSessionId) {
        AttachabilityResponse body;
        try {
            body = restClient.get()
                    .uri("/internal/live-sessions/{id}/attachability", liveSessionId)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (req, resp) -> {
                        throw new CatalogException(ErrorCode.LIVE_SESSION_NOT_FOUND,
                                "Live session not found: " + liveSessionId);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {
                        throw new CatalogException(ErrorCode.LIVE_CONTROL_UNAVAILABLE,
                                "live-control-service returned 5xx for session: " + liveSessionId);
                    })
                    .body(AttachabilityResponse.class);
        } catch (CatalogException e) {
            throw e;
        } catch (Exception e) {
            throw new CatalogException(ErrorCode.LIVE_CONTROL_UNAVAILABLE,
                    "Failed to reach live-control-service: " + e.getMessage());
        }

        if (body != null && !body.attachable()) {
            throw new CatalogException(ErrorCode.LIVE_SESSION_NOT_ATTACHABLE,
                    "Live session is not attachable: " + liveSessionId);
        }
    }

    private record AttachabilityResponse(boolean attachable, String status) {}
}
