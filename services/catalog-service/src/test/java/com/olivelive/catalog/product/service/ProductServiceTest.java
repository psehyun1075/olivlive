package com.olivelive.catalog.product.service;

import com.olivelive.catalog.common.exception.CatalogException;
import com.olivelive.catalog.common.exception.ErrorCode;
import com.olivelive.catalog.product.domain.Product;
import com.olivelive.catalog.product.domain.ProductStatus;
import com.olivelive.catalog.product.dto.CreateProductRequest;
import com.olivelive.catalog.product.dto.ProductListResponse;
import com.olivelive.catalog.product.dto.ProductResponse;
import com.olivelive.catalog.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct_success_returnsActiveProduct() {
        var request = new CreateProductRequest("이어폰", "설명", 10000, 100);
        var product = Product.create("이어폰", "설명", 10000, 100);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.createProduct(request);

        assertThat(response.name()).isEqualTo("이어폰");
        assertThat(response.status()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(response.price()).isEqualTo(10000);
        assertThat(response.stockQuantity()).isEqualTo(100);
    }

    @Test
    void getProductById_notFound_throwsProductNotFound() {
        when(productRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById("missing"))
                .isInstanceOf(CatalogException.class)
                .satisfies(e -> assertThat(((CatalogException) e).getErrorCode())
                        .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Test
    void getProductById_found_returnsFullResponse() {
        var product = Product.create("이어폰", "상세 설명", 10000, 50);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(product.getId());

        assertThat(response.id()).isEqualTo(product.getId());
        assertThat(response.description()).isEqualTo("상세 설명");
    }

    @Test
    void listProducts_activeStatus_callsFindByStatus() {
        var product = Product.create("이어폰", null, 10000, 50);
        when(productRepository.findByStatus(eq(ProductStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product)));

        ProductListResponse response = productService.listProducts("active", 1, 20);

        assertThat(response.items()).hasSize(1);
        assertThat(response.pagination().page()).isEqualTo(1);
        assertThat(response.pagination().limit()).isEqualTo(20);
        assertThat(response.pagination().total()).isEqualTo(1);
        verify(productRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void listProducts_allStatus_callsFindAll() {
        when(productRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        ProductListResponse response = productService.listProducts("all", 1, 20);

        assertThat(response.items()).isEmpty();
        verify(productRepository, never()).findByStatus(any(), any());
    }

    @Test
    void listProducts_pagination_reflectsRequestedPageAndLimit() {
        when(productRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        ProductListResponse response = productService.listProducts("all", 3, 10);

        assertThat(response.pagination().page()).isEqualTo(3);
        assertThat(response.pagination().limit()).isEqualTo(10);
    }
}
