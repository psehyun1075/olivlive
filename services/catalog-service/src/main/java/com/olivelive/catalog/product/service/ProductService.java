package com.olivelive.catalog.product.service;

import com.olivelive.catalog.common.exception.CatalogException;
import com.olivelive.catalog.common.exception.ErrorCode;
import com.olivelive.catalog.product.domain.Product;
import com.olivelive.catalog.product.domain.ProductStatus;
import com.olivelive.catalog.product.dto.CreateProductRequest;
import com.olivelive.catalog.product.dto.ProductListResponse;
import com.olivelive.catalog.product.dto.ProductResponse;
import com.olivelive.catalog.product.dto.ProductSummaryResponse;
import com.olivelive.catalog.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = Product.create(
                request.name(),
                request.description(),
                request.price(),
                request.stockQuantity()
        );
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new CatalogException(ErrorCode.PRODUCT_NOT_FOUND,
                        "Product not found: " + id));
        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public ProductListResponse listProducts(String statusParam, int page, int limit) {
        PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        Page<Product> result;
        if ("all".equalsIgnoreCase(statusParam)) {
            result = productRepository.findAll(pageable);
        } else {
            ProductStatus status = ProductStatus.fromValue(statusParam);
            result = productRepository.findByStatus(status, pageable);
        }
        List<ProductSummaryResponse> items = result.getContent().stream()
                .map(ProductSummaryResponse::from)
                .toList();
        return new ProductListResponse(
                items,
                new ProductListResponse.Pagination(page, limit, result.getTotalElements())
        );
    }
}
