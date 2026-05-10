package com.olivelive.catalog.liveproduct.dto;

import java.util.List;

public record LiveProductListResponse(
        String liveSessionId,
        List<LiveProductResponse> items
) {}
