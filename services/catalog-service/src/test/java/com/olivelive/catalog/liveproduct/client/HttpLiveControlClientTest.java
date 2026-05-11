package com.olivelive.catalog.liveproduct.client;

import com.olivelive.catalog.common.exception.CatalogException;
import com.olivelive.catalog.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HttpLiveControlClientTest {

    private static final String SESSION_ID = "live_ABC123";
    private static final String ATTACHABILITY_URL =
            "http://live-control-service/internal/live-sessions/" + SESSION_ID + "/attachability";

    private MockRestServiceServer mockServer;
    private HttpLiveControlClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new HttpLiveControlClient(builder.baseUrl("http://live-control-service").build());
    }

    @Test
    void validateSessionAttachable_200AttachableTrue_noException() {
        mockServer.expect(requestTo(ATTACHABILITY_URL))
                .andRespond(withSuccess(
                        "{\"attachable\":true,\"status\":\"live\"}",
                        MediaType.APPLICATION_JSON));

        assertThatCode(() -> client.validateSessionAttachable(SESSION_ID))
                .doesNotThrowAnyException();
        mockServer.verify();
    }

    @Test
    void validateSessionAttachable_200AttachableFalse_throwsNotAttachable() {
        mockServer.expect(requestTo(ATTACHABILITY_URL))
                .andRespond(withSuccess(
                        "{\"attachable\":false,\"status\":\"ended\"}",
                        MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.validateSessionAttachable(SESSION_ID))
                .isInstanceOf(CatalogException.class)
                .satisfies(e -> assertThat(((CatalogException) e).getErrorCode())
                        .isEqualTo(ErrorCode.LIVE_SESSION_NOT_ATTACHABLE));
        mockServer.verify();
    }

    @Test
    void validateSessionAttachable_404_throwsSessionNotFound() {
        mockServer.expect(requestTo(ATTACHABILITY_URL))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.validateSessionAttachable(SESSION_ID))
                .isInstanceOf(CatalogException.class)
                .satisfies(e -> assertThat(((CatalogException) e).getErrorCode())
                        .isEqualTo(ErrorCode.LIVE_SESSION_NOT_FOUND));
        mockServer.verify();
    }

    @Test
    void validateSessionAttachable_500_throwsLiveControlUnavailable() {
        mockServer.expect(requestTo(ATTACHABILITY_URL))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.validateSessionAttachable(SESSION_ID))
                .isInstanceOf(CatalogException.class)
                .satisfies(e -> assertThat(((CatalogException) e).getErrorCode())
                        .isEqualTo(ErrorCode.LIVE_CONTROL_UNAVAILABLE));
        mockServer.verify();
    }
}
