package com.olivelive.catalog.liveproduct.domain;

import com.olivelive.catalog.common.ulid.UlidGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "live_products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LiveProduct {

    @Id
    @Column(length = 32)
    private String id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String liveSessionId;

    @Column(nullable = false, length = 32)
    private String productId;

    @Column(nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public static LiveProduct create(String liveSessionId, String productId, int displayOrder) {
        LiveProduct lp = new LiveProduct();
        lp.id = UlidGenerator.liveProductId();
        lp.liveSessionId = liveSessionId;
        lp.productId = productId;
        lp.displayOrder = displayOrder;
        lp.isActive = true;
        lp.createdAt = OffsetDateTime.now();
        return lp;
    }
}
