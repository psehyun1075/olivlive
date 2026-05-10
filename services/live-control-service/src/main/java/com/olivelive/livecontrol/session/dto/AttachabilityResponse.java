package com.olivelive.livecontrol.session.dto;

import com.olivelive.livecontrol.session.domain.LiveSession;
import com.olivelive.livecontrol.session.domain.SessionStatus;

public record AttachabilityResponse(
        String sessionId,
        boolean attachable,
        SessionStatus status
) {
    public static AttachabilityResponse from(LiveSession s) {
        return new AttachabilityResponse(s.getId(), s.isAttachable(), s.getStatus());
    }
}
