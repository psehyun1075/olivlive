package com.olivelive.catalog.liveproduct.controller;

import com.olivelive.catalog.liveproduct.dto.CreateLiveProductRequest;
import com.olivelive.catalog.liveproduct.dto.CreateLiveProductResponse;
import com.olivelive.catalog.liveproduct.dto.LiveProductListResponse;
import com.olivelive.catalog.liveproduct.service.LiveProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/live-products")
@RequiredArgsConstructor
public class LiveProductController {

    private final LiveProductService liveProductService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateLiveProductResponse createLiveProduct(@Valid @RequestBody CreateLiveProductRequest request) {
        return liveProductService.createLiveProduct(request);
    }

    @GetMapping("/{liveSessionId}")
    public LiveProductListResponse getLiveProducts(
            @PathVariable String liveSessionId,
            @RequestParam(defaultValue = "true") boolean isActive
    ) {
        return liveProductService.getLiveProducts(liveSessionId, isActive);
    }
}
