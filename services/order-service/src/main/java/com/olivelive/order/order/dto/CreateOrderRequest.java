package com.olivelive.order.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank @Size(max = 100)
        String buyerId,

        String liveSessionId,

        @NotEmpty @Valid
        List<OrderItemRequest> items
) {}
