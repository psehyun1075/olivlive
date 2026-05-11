package com.olivelive.order.order.dto;

import com.olivelive.order.order.domain.OrderItem;

public record OrderItemResponse(
        String id,
        String productId,
        String productName,
        Integer unitPrice,
        Integer quantity,
        Integer subtotal
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getSubtotal()
        );
    }
}
