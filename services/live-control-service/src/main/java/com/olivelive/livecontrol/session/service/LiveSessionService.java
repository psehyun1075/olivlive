package com.olivelive.livecontrol.session.service;

import com.olivelive.livecontrol.common.exception.ErrorCode;
import com.olivelive.livecontrol.common.exception.LiveControlException;
import com.olivelive.livecontrol.session.domain.LiveSession;
import com.olivelive.livecontrol.session.dto.AttachabilityResponse;
import com.olivelive.livecontrol.session.dto.CreateSessionRequest;
import com.olivelive.livecontrol.session.dto.LiveSessionResponse;
import com.olivelive.livecontrol.session.dto.SessionStatusResponse;
import com.olivelive.livecontrol.session.repository.LiveSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LiveSessionService {

    private final LiveSessionRepository repository;

    @Transactional
    public LiveSessionResponse createSession(CreateSessionRequest request) {
        LiveSession session = LiveSession.create(request.title(), request.hostId(), request.scheduledAt());
        return LiveSessionResponse.from(repository.save(session));
    }

    @Transactional(readOnly = true)
    public LiveSessionResponse getSession(String id) {
        return LiveSessionResponse.from(findOrThrow(id));
    }

    @Transactional
    public SessionStatusResponse goLive(String id) {
        LiveSession session = findOrThrow(id);
        session.goLive();
        return SessionStatusResponse.from(session);
    }

    @Transactional
    public SessionStatusResponse end(String id) {
        LiveSession session = findOrThrow(id);
        session.end();
        return SessionStatusResponse.from(session);
    }

    @Transactional(readOnly = true)
    public AttachabilityResponse getAttachability(String id) {
        return AttachabilityResponse.from(findOrThrow(id));
    }

    private LiveSession findOrThrow(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new LiveControlException(
                        ErrorCode.LIVE_SESSION_NOT_FOUND, "Live session not found: " + id));
    }
}
