package com.olivelive.catalog.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
        @NotBlank @Size(min = 1, max = 200)
        String name,

        @Size(max = 2000)
        String description,

        @NotNull @Positive
        Integer price,

        @NotNull @Min(0)
        Integer stockQuantity
) {}
