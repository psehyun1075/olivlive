package com.olivelive.catalog.product.dto;

import java.util.List;

public record ProductListResponse(
        List<ProductSummaryResponse> items,
        Pagination pagination
) {
    public record Pagination(int page, int limit, long total) {}
}
