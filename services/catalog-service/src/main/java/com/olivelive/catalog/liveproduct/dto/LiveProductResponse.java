package com.olivelive.catalog.liveproduct.dto;

import com.olivelive.catalog.liveproduct.domain.LiveProduct;
import com.olivelive.catalog.product.domain.Product;

import java.time.OffsetDateTime;

public record LiveProductResponse(
        String id,
        String productId,
        String name,
        Integer price,
        Integer stockQuantity,
        Integer displayOrder,
        Boolean isActive,
        OffsetDateTime createdAt
) {
    public static LiveProductResponse of(LiveProduct lp, Product product) {
        return new LiveProductResponse(
                lp.getId(),
                lp.getProductId(),
                product.getName(),
                product.getPrice(),
                product.getStockQuantity(),
                lp.getDisplayOrder(),
                lp.getIsActive(),
                lp.getCreatedAt()
        );
    }
}
