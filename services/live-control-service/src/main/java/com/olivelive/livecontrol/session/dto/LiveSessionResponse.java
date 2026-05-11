package com.olivelive.livecontrol.session.dto;

import com.olivelive.livecontrol.session.domain.LiveSession;
import com.olivelive.livecontrol.session.domain.SessionStatus;

import java.time.OffsetDateTime;

public record LiveSessionResponse(
        String id,
        String title,
        String hostId,
        SessionStatus status,
        OffsetDateTime scheduledAt,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static LiveSessionResponse from(LiveSession s) {
        return new LiveSessionResponse(
                s.getId(),
                s.getTitle(),
                s.getHostId(),
                s.getStatus(),
                s.getScheduledAt(),
                s.getStartedAt(),
                s.getEndedAt(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}
