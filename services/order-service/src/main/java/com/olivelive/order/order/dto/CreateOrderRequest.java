package com.olivelive.order.order.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(
        @JsonProperty("buyer_id")
        @JsonAlias("buyerId")
        @NotBlank
        @Size(max = 100)
        String buyerId,

        @JsonProperty("live_session_id")
        @JsonAlias("liveSessionId")
        String liveSessionId,

        @NotEmpty
        @Valid
        List<OrderItemRequest> items
) {}