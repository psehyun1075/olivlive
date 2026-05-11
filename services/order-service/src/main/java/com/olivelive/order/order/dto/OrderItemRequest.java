package com.olivelive.order.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record OrderItemRequest(
        @NotBlank
        String productId,

        @NotBlank @Size(max = 200)
        String productName,

        @NotNull @Positive
        Integer unitPrice,

        @NotNull @Min(1)
        Integer quantity
) {}
