package com.olivelive.livecontrol.session.service;

import com.olivelive.livecontrol.common.exception.ErrorCode;
import com.olivelive.livecontrol.common.exception.LiveControlException;
import com.olivelive.livecontrol.session.domain.LiveSession;
import com.olivelive.livecontrol.session.domain.SessionStatus;
import com.olivelive.livecontrol.session.dto.AttachabilityResponse;
import com.olivelive.livecontrol.session.dto.CreateSessionRequest;
import com.olivelive.livecontrol.session.dto.LiveSessionResponse;
import com.olivelive.livecontrol.session.dto.SessionStatusResponse;
import com.olivelive.livecontrol.session.repository.LiveSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiveSessionServiceTest {

    @Mock
    private LiveSessionRepository repository;

    @InjectMocks
    private LiveSessionService service;

    @Test
    void createSession_success_returnsReadySession() {
        var request = new CreateSessionRequest("여름 특가 라이브", "host_01", null);
        when(repository.save(any(LiveSession.class))).thenAnswer(inv -> inv.getArgument(0));

        LiveSessionResponse response = service.createSession(request);

        assertThat(response.title()).isEqualTo("여름 특가 라이브");
        assertThat(response.hostId()).isEqualTo("host_01");
        assertThat(response.status()).isEqualTo(SessionStatus.READY);
        assertThat(response.startedAt()).isNull();
        assertThat(response.endedAt()).isNull();
    }

    @Test
    void getSessionById_notFound_throwsNotFound() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSession("missing"))
                .isInstanceOf(LiveControlException.class)
                .satisfies(e -> assertThat(((LiveControlException) e).getErrorCode())
                        .isEqualTo(ErrorCode.LIVE_SESSION_NOT_FOUND));
    }

    @Test
    void getSessionById_found_returnsResponse() {
        LiveSession session = LiveSession.create("제목", "host_02", null);
        when(repository.findById(session.getId())).thenReturn(Optional.of(session));

        LiveSessionResponse response = service.getSession(session.getId());

        assertThat(response.id()).isEqualTo(session.getId());
        assertThat(response.title()).isEqualTo("제목");
        assertThat(response.status()).isEqualTo(SessionStatus.READY);
    }

    @Test
    void goLive_whenReady_transitionsToLive() {
        LiveSession session = LiveSession.create("라이브", "host_03", null);
        when(repository.findById(session.getId())).thenReturn(Optional.of(session));

        SessionStatusResponse response = service.goLive(session.getId());

        assertThat(response.status()).isEqualTo(SessionStatus.LIVE);
        assertThat(response.startedAt()).isNotNull();
        assertThat(response.endedAt()).isNull();
    }

    @Test
    void goLive_whenNotReady_throwsSessionNotReady() {
        LiveSession session = LiveSession.create("라이브", "host_04", null);
        session.goLive();
        when(repository.findById(session.getId())).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.goLive(session.getId()))
                .isInstanceOf(LiveControlException.class)
                .satisfies(e -> assertThat(((LiveControlException) e).getErrorCode())
                        .isEqualTo(ErrorCode.SESSION_NOT_READY));
    }

    @Test
    void end_whenLive_transitionsToEnded() {
        LiveSession session = LiveSession.create("라이브", "host_05", null);
        session.goLive();
        when(repository.findById(session.getId())).thenReturn(Optional.of(session));

        SessionStatusResponse response = service.end(session.getId());

        assertThat(response.status()).isEqualTo(SessionStatus.ENDED);
        assertThat(response.endedAt()).isNotNull();
    }

    @Test
    void end_whenNotLive_throwsSessionNotLive() {
        LiveSession session = LiveSession.create("라이브", "host_06", null);
        when(repository.findById(session.getId())).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.end(session.getId()))
                .isInstanceOf(LiveControlException.class)
                .satisfies(e -> assertThat(((LiveControlException) e).getErrorCode())
                        .isEqualTo(ErrorCode.SESSION_NOT_LIVE));
    }

    @Test
    void getAttachability_whenReady_returnsTrue() {
        LiveSession session = LiveSession.create("라이브", "host_07", null);
        when(repository.findById(session.getId())).thenReturn(Optional.of(session));

        AttachabilityResponse response = service.getAttachability(session.getId());

        assertThat(response.attachable()).isTrue();
        assertThat(response.status()).isEqualTo(SessionStatus.READY);
    }

    @Test
    void getAttachability_whenLive_returnsFalse() {
        LiveSession session = LiveSession.create("라이브", "host_08", null);
        session.goLive();
        when(repository.findById(session.getId())).thenReturn(Optional.of(session));

        AttachabilityResponse response = service.getAttachability(session.getId());

        assertThat(response.attachable()).isFalse();
        assertThat(response.status()).isEqualTo(SessionStatus.LIVE);
    }

    @Test
    void getAttachability_notFound_throwsNotFound() {
        when(repository.findById("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAttachability("ghost"))
                .isInstanceOf(LiveControlException.class)
                .satisfies(e -> assertThat(((LiveControlException) e).getErrorCode())
                        .isEqualTo(ErrorCode.LIVE_SESSION_NOT_FOUND));
    }
}
