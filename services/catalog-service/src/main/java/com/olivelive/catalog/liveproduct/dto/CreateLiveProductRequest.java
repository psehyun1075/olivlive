package com.olivelive.catalog.liveproduct.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateLiveProductRequest(
        @NotBlank
        String liveSessionId,

        @NotBlank
        String productId,

        @NotNull @Min(1)
        Integer displayOrder
) {}
