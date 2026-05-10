package com.olivelive.catalog.liveproduct.service;

import com.olivelive.catalog.common.exception.CatalogException;
import com.olivelive.catalog.common.exception.ErrorCode;
import com.olivelive.catalog.liveproduct.client.LiveControlClient;
import com.olivelive.catalog.liveproduct.domain.LiveProduct;
import com.olivelive.catalog.liveproduct.dto.CreateLiveProductRequest;
import com.olivelive.catalog.liveproduct.dto.CreateLiveProductResponse;
import com.olivelive.catalog.liveproduct.dto.LiveProductListResponse;
import com.olivelive.catalog.liveproduct.dto.LiveProductResponse;
import com.olivelive.catalog.liveproduct.repository.LiveProductRepository;
import com.olivelive.catalog.product.domain.Product;
import com.olivelive.catalog.product.domain.ProductStatus;
import com.olivelive.catalog.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LiveProductService {

    private final ProductRepository productRepository;
    private final LiveProductRepository liveProductRepository;
    private final LiveControlClient liveControlClient;

    @Transactional
    public CreateLiveProductResponse createLiveProduct(CreateLiveProductRequest request) {
        // 1. Product existence
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new CatalogException(ErrorCode.PRODUCT_NOT_FOUND,
                        "Product not found: " + request.productId()));

        // 2. Product active status
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new CatalogException(ErrorCode.INVALID_PRODUCT_STATUS,
                    "Product is not active: " + request.productId());
        }

        // 3 & 4. Live session existence and attachable state (delegated to live-control-service)
        liveControlClient.validateSessionAttachable(request.liveSessionId());

        // 5. Duplicate (live_session_id, product_id) check
        if (liveProductRepository.existsByLiveSessionIdAndProductId(
                request.liveSessionId(), request.productId())) {
            throw new CatalogException(ErrorCode.DUPLICATE_LIVE_PRODUCT,
                    "Product already mapped to this live session");
        }

        // 6. Duplicate (live_session_id, display_order) check
        if (liveProductRepository.existsByLiveSessionIdAndDisplayOrder(
                request.liveSessionId(), request.displayOrder())) {
            throw new CatalogException(ErrorCode.DUPLICATE_DISPLAY_ORDER,
                    "Display order " + request.displayOrder() + " is already taken in this live session");
        }

        LiveProduct liveProduct = LiveProduct.create(
                request.liveSessionId(),
                request.productId(),
                request.displayOrder()
        );
        return CreateLiveProductResponse.from(liveProductRepository.save(liveProduct));
    }

    @Transactional(readOnly = true)
    public LiveProductListResponse getLiveProducts(String liveSessionId, boolean isActive) {
        List<LiveProduct> liveProducts = liveProductRepository
                .findByLiveSessionIdAndIsActiveOrderByDisplayOrderAsc(liveSessionId, isActive);

        Set<String> productIds = liveProducts.stream()
                .map(LiveProduct::getProductId)
                .collect(Collectors.toSet());

        Map<String, Product> productMap = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<LiveProductResponse> items = liveProducts.stream()
                .map(lp -> LiveProductResponse.of(lp, productMap.get(lp.getProductId())))
                .toList();

        return new LiveProductListResponse(liveSessionId, items);
    }
}
