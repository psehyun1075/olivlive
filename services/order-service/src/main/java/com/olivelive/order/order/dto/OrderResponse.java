package com.olivelive.order.order.dto;

import com.olivelive.order.order.domain.Order;

import java.time.OffsetDateTime;
import java.util.List;

public record OrderResponse(
        String id,
        String buyerId,
        String liveSessionId,
        String status,
        Integer totalAmount,
        String paymentStatus,
        List<OrderItemResponse> items,
        OffsetDateTime createdAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getBuyerId(),
                order.getLiveSessionId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getPaymentStatus().name(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.getCreatedAt()
        );
    }
}
