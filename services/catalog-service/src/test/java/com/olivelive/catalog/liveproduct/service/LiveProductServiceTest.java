package com.olivelive.catalog.liveproduct.service;

import com.olivelive.catalog.common.exception.CatalogException;
import com.olivelive.catalog.common.exception.ErrorCode;
import com.olivelive.catalog.liveproduct.client.LiveControlClient;
import com.olivelive.catalog.liveproduct.domain.LiveProduct;
import com.olivelive.catalog.liveproduct.dto.CreateLiveProductRequest;
import com.olivelive.catalog.liveproduct.dto.CreateLiveProductResponse;
import com.olivelive.catalog.liveproduct.dto.LiveProductListResponse;
import com.olivelive.catalog.liveproduct.repository.LiveProductRepository;
import com.olivelive.catalog.product.domain.Product;
import com.olivelive.catalog.product.domain.ProductStatus;
import com.olivelive.catalog.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiveProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private LiveProductRepository liveProductRepository;

    @Mock
    private LiveControlClient liveControlClient;

    @InjectMocks
    private LiveProductService liveProductService;

    private static final String SESSION_ID = "live_ABC123";

    @Test
    void createLiveProduct_success_returnsMappingWithActiveTrue() {
        Product product = Product.create("이어폰", null, 10000, 100);
        LiveProduct liveProduct = LiveProduct.create(SESSION_ID, product.getId(), 1);
        var request = new CreateLiveProductRequest(SESSION_ID, product.getId(), 1);

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(liveProductRepository.existsByLiveSessionIdAndProductId(SESSION_ID, product.getId())).thenReturn(false);
        when(liveProductRepository.save(any(LiveProduct.class))).thenReturn(liveProduct);

        CreateLiveProductResponse response = liveProductService.createLiveProduct(request);

        assertThat(response.liveSessionId()).isEqualTo(SESSION_ID);
        assertThat(response.isActive()).isTrue();
        verify(liveControlClient).validateSessionAttachable(SESSION_ID);
    }

    @Test
    void createLiveProduct_productNotFound_throwsProductNotFound() {
        var request = new CreateLiveProductRequest(SESSION_ID, "missing_prod", 1);
        when(productRepository.findById("missing_prod")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> liveProductService.createLiveProduct(request))
                .isInstanceOf(CatalogException.class)
                .satisfies(e -> assertThat(((CatalogException) e).getErrorCode())
                        .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND));

        verifyNoInteractions(liveControlClient);
    }

    @Test
    void createLiveProduct_productInactive_throwsInvalidProductStatus() {
        Product inactiveProduct = mock(Product.class);
        when(inactiveProduct.getStatus()).thenReturn(ProductStatus.INACTIVE);
        when(productRepository.findById("prod_inactive")).thenReturn(Optional.of(inactiveProduct));

        var request = new CreateLiveProductRequest(SESSION_ID, "prod_inactive", 1);

        assertThatThrownBy(() -> liveProductService.createLiveProduct(request))
                .isInstanceOf(CatalogException.class)
                .satisfies(e -> assertThat(((CatalogException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_PRODUCT_STATUS));

        verifyNoInteractions(liveControlClient);
    }

    @Test
    void createLiveProduct_sessionNotFound_throwsLiveSessionNotFound() {
        Product product = Product.create("이어폰", null, 10000, 100);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        doThrow(new CatalogException(ErrorCode.LIVE_SESSION_NOT_FOUND, "Session not found"))
                .when(liveControlClient).validateSessionAttachable(SESSION_ID);

        var request = new CreateLiveProductRequest(SESSION_ID, product.getId(), 1);

        assertThatThrownBy(() -> liveProductService.createLiveProduct(request))
                .isInstanceOf(CatalogException.class)
                .satisfies(e -> assertThat(((CatalogException) e).getErrorCode())
                        .isEqualTo(ErrorCode.LIVE_SESSION_NOT_FOUND));

        verify(liveProductRepository, never()).existsByLiveSessionIdAndProductId(any(), any());
    }

    @Test
    void createLiveProduct_sessionNotAttachable_throwsLiveSessionNotAttachable() {
        Product product = Product.create("이어폰", null, 10000, 100);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        doThrow(new CatalogException(ErrorCode.LIVE_SESSION_NOT_ATTACHABLE, "Session ended"))
                .when(liveControlClient).validateSessionAttachable(SESSION_ID);

        var request = new CreateLiveProductRequest(SESSION_ID, product.getId(), 1);

        assertThatThrownBy(() -> liveProductService.createLiveProduct(request))
                .isInstanceOf(CatalogException.class)
                .satisfies(e -> assertThat(((CatalogException) e).getErrorCode())
                        .isEqualTo(ErrorCode.LIVE_SESSION_NOT_ATTACHABLE));
    }

    @Test
    void createLiveProduct_duplicateProductMapping_throwsDuplicateLiveProduct() {
        Product product = Product.create("이어폰", null, 10000, 100);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(liveProductRepository.existsByLiveSessionIdAndProductId(SESSION_ID, product.getId())).thenReturn(true);

        var request = new CreateLiveProductRequest(SESSION_ID, product.getId(), 1);

        assertThatThrownBy(() -> liveProductService.createLiveProduct(request))
                .isInstanceOf(CatalogException.class)
                .satisfies(e -> assertThat(((CatalogException) e).getErrorCode())
                        .isEqualTo(ErrorCode.DUPLICATE_LIVE_PRODUCT));

        verify(liveProductRepository, never()).save(any());
    }

    @Test
    void createLiveProduct_duplicateDisplayOrder_throwsDuplicateDisplayOrder() {
        Product product = Product.create("이어폰", null, 10000, 100);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(liveProductRepository.existsByLiveSessionIdAndProductId(SESSION_ID, product.getId())).thenReturn(false);
        when(liveProductRepository.existsByLiveSessionIdAndDisplayOrder(SESSION_ID, 1)).thenReturn(true);

        var request = new CreateLiveProductRequest(SESSION_ID, product.getId(), 1);

        assertThatThrownBy(() -> liveProductService.createLiveProduct(request))
                .isInstanceOf(CatalogException.class)
                .satisfies(e -> assertThat(((CatalogException) e).getErrorCode())
                        .isEqualTo(ErrorCode.DUPLICATE_DISPLAY_ORDER));

        verify(liveProductRepository, never()).save(any());
    }

    @Test
    void getLiveProducts_emptySession_returnsEmptyItems() {
        when(liveProductRepository.findByLiveSessionIdAndIsActiveOrderByDisplayOrderAsc(SESSION_ID, true))
                .thenReturn(List.of());

        LiveProductListResponse response = liveProductService.getLiveProducts(SESSION_ID, true);

        assertThat(response.liveSessionId()).isEqualTo(SESSION_ID);
        assertThat(response.items()).isEmpty();
    }
}
