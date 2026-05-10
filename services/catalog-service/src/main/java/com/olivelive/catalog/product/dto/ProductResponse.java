package com.olivelive.catalog.product.dto;

import com.olivelive.catalog.product.domain.Product;
import com.olivelive.catalog.product.domain.ProductStatus;

import java.time.OffsetDateTime;

public record ProductResponse(
        String id,
        String name,
        String description,
        Integer price,
        Integer stockQuantity,
        ProductStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
