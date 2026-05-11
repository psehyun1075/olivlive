package com.olivelive.order.order.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record OrderItemRequest(
        @JsonProperty("product_id")
        @JsonAlias("productId")
        @NotBlank
        String productId,

        @JsonProperty("product_name")
        @JsonAlias("productName")
        @NotBlank
        @Size(max = 200)
        String productName,

        @JsonProperty("unit_price")
        @JsonAlias("unitPrice")
        @NotNull
        @Positive
        Integer unitPrice,

        @JsonProperty("quantity")
        @JsonAlias("quantity")
        @NotNull
        @Min(1)
        Integer quantity
) {}