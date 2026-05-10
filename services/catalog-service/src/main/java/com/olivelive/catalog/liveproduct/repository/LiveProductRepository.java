package com.olivelive.catalog.liveproduct.repository;

import com.olivelive.catalog.liveproduct.domain.LiveProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LiveProductRepository extends JpaRepository<LiveProduct, String> {
    boolean existsByLiveSessionIdAndProductId(String liveSessionId, String productId);
    boolean existsByLiveSessionIdAndDisplayOrder(String liveSessionId, Integer displayOrder);
    List<LiveProduct> findByLiveSessionIdAndIsActiveOrderByDisplayOrderAsc(String liveSessionId, Boolean isActive);
}
