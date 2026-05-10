package com.olivelive.catalog.product.domain;

import com.olivelive.catalog.common.ulid.UlidGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @Column(length = 32)
    private String id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Convert(converter = ProductStatusConverter.class)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    public static Product create(String name, String description, int price, int stockQuantity) {
        Product p = new Product();
        p.id = UlidGenerator.productId();
        p.name = name;
        p.description = description;
        p.price = price;
        p.stockQuantity = stockQuantity;
        p.status = ProductStatus.ACTIVE;
        OffsetDateTime now = OffsetDateTime.now();
        p.createdAt = now;
        p.updatedAt = now;
        return p;
    }
}
