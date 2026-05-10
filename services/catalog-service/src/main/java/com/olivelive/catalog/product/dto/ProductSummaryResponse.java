package com.olivelive.catalog.product.dto;

import com.olivelive.catalog.product.domain.Product;
import com.olivelive.catalog.product.domain.ProductStatus;

import java.time.OffsetDateTime;

public record ProductSummaryResponse(
        String id,
        String name,
        Integer price,
        Integer stockQuantity,
        ProductStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ProductSummaryResponse from(Product product) {
        return new ProductSummaryResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
