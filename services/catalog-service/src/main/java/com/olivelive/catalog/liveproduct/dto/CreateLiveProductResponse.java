package com.olivelive.catalog.liveproduct.dto;

import com.olivelive.catalog.liveproduct.domain.LiveProduct;

import java.time.OffsetDateTime;

public record CreateLiveProductResponse(
        String id,
        String liveSessionId,
        String productId,
        Integer displayOrder,
        Boolean isActive,
        OffsetDateTime createdAt
) {
    public static CreateLiveProductResponse from(LiveProduct lp) {
        return new CreateLiveProductResponse(
                lp.getId(),
                lp.getLiveSessionId(),
                lp.getProductId(),
                lp.getDisplayOrder(),
                lp.getIsActive(),
                lp.getCreatedAt()
        );
    }
}
