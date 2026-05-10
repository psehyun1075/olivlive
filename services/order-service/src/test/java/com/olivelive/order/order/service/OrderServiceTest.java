package com.olivelive.order.order.service;

import com.olivelive.order.common.exception.ErrorCode;
import com.olivelive.order.common.exception.OrderException;
import com.olivelive.order.order.domain.Order;
import com.olivelive.order.order.domain.OrderItem;
import com.olivelive.order.order.dto.CreateOrderRequest;
import com.olivelive.order.order.dto.OrderItemRequest;
import com.olivelive.order.order.dto.OrderResponse;
import com.olivelive.order.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_success_returnsPaidOrderWithCorrectTotal() {
        var request = new CreateOrderRequest("user_1", null,
                List.of(new OrderItemRequest("prod_01", "이어폰", 89000, 2)));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.status()).isEqualTo("PAID");
        assertThat(response.paymentStatus()).isEqualTo("MOCK_PAID");
        assertThat(response.totalAmount()).isEqualTo(178000);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).subtotal()).isEqualTo(178000);
    }

    @Test
    void createOrder_multipleItems_sumsSubtotals() {
        var request = new CreateOrderRequest("user_1", "live_ABC",
                List.of(
                        new OrderItemRequest("prod_01", "이어폰", 89000, 1),
                        new OrderItemRequest("prod_02", "충전기", 15000, 3)
                ));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.totalAmount()).isEqualTo(89000 + 45000);
        assertThat(response.items()).hasSize(2);
    }

    @Test
    void getOrderById_notFound_throwsOrderNotFound() {
        when(orderRepository.findByIdWithItems("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById("missing"))
                .isInstanceOf(OrderException.class)
                .satisfies(e -> assertThat(((OrderException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ORDER_NOT_FOUND));
    }

    @Test
    void getOrderById_found_returnsOrderWithItems() {
        List<OrderItem> items = List.of(OrderItem.create("prod_01", "이어폰", 89000, 1));
        Order order = Order.create("user_1", "live_ABC", items);
        when(orderRepository.findByIdWithItems(order.getId())).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(order.getId());

        assertThat(response.buyerId()).isEqualTo("user_1");
        assertThat(response.liveSessionId()).isEqualTo("live_ABC");
        assertThat(response.totalAmount()).isEqualTo(89000);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).productName()).isEqualTo("이어폰");
    }
}
