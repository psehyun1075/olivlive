package com.olivelive.livecontrol.session.dto;

import com.olivelive.livecontrol.session.domain.LiveSession;
import com.olivelive.livecontrol.session.domain.SessionStatus;

import java.time.OffsetDateTime;

public record SessionStatusResponse(
        String id,
        SessionStatus status,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        OffsetDateTime updatedAt
) {
    public static SessionStatusResponse from(LiveSession s) {
        return new SessionStatusResponse(
                s.getId(),
                s.getStatus(),
                s.getStartedAt(),
                s.getEndedAt(),
                s.getUpdatedAt()
        );
    }
}
