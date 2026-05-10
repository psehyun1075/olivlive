package com.olivelive.order.order.service;

import com.olivelive.order.common.exception.ErrorCode;
import com.olivelive.order.common.exception.OrderException;
import com.olivelive.order.order.domain.Order;
import com.olivelive.order.order.domain.OrderItem;
import com.olivelive.order.order.dto.CreateOrderRequest;
import com.olivelive.order.order.dto.OrderResponse;
import com.olivelive.order.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        List<OrderItem> items = request.items().stream()
                .map(i -> OrderItem.create(i.productId(), i.productName(), i.unitPrice(), i.quantity()))
                .toList();
        Order order = Order.create(request.buyerId(), request.liveSessionId(), items);
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new OrderException(ErrorCode.ORDER_NOT_FOUND, "Order not found: " + id));
        return OrderResponse.from(order);
    }
}
