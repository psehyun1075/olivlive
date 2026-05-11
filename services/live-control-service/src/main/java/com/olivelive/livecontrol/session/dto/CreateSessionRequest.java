package com.olivelive.livecontrol.session.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CreateSessionRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String hostId,
        OffsetDateTime scheduledAt
) {}
