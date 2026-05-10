package com.olivelive.livecontrol.session.domain;

import com.olivelive.livecontrol.common.exception.ErrorCode;
import com.olivelive.livecontrol.common.exception.LiveControlException;
import com.olivelive.livecontrol.common.ulid.UlidGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "live_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LiveSession {

    @Id
    @Column(length = 32)
    private String id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 32)
    private String hostId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SessionStatus status;

    @Column
    private OffsetDateTime scheduledAt;

    @Column
    private OffsetDateTime startedAt;

    @Column
    private OffsetDateTime endedAt;

    @Column(columnDefinition = "TEXT")
    private String ivsChannelArn;

    @Column(columnDefinition = "TEXT")
    private String ivsPlaybackUrl;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    public static LiveSession create(String title, String hostId, OffsetDateTime scheduledAt) {
        LiveSession s = new LiveSession();
        s.id = UlidGenerator.sessionId();
        s.title = title;
        s.hostId = hostId;
        s.status = SessionStatus.READY;
        s.scheduledAt = scheduledAt;
        OffsetDateTime now = OffsetDateTime.now();
        s.createdAt = now;
        s.updatedAt = now;
        return s;
    }

    public void goLive() {
        if (status != SessionStatus.READY) {
            throw new LiveControlException(ErrorCode.SESSION_NOT_READY,
                    "Session is not in READY state: " + id);
        }
        status = SessionStatus.LIVE;
        startedAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    public void end() {
        if (status != SessionStatus.LIVE) {
            throw new LiveControlException(ErrorCode.SESSION_NOT_LIVE,
                    "Session is not in LIVE state: " + id);
        }
        status = SessionStatus.ENDED;
        endedAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    public boolean isAttachable() {
        return status == SessionStatus.READY;
    }
}
