package com.olivelive.order.order.domain;

import com.olivelive.order.common.ulid.UlidGenerator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @Column(length = 32)
    private String id;

    @Column(nullable = false, length = 100)
    private String buyerId;

    @Column(columnDefinition = "TEXT")
    private String liveSessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false)
    private Integer totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    public static Order create(String buyerId, String liveSessionId, List<OrderItem> items) {
        Order order = new Order();
        order.id = UlidGenerator.orderId();
        order.buyerId = buyerId;
        order.liveSessionId = liveSessionId;
        order.status = OrderStatus.PAID;
        order.paymentStatus = PaymentStatus.MOCK_PAID;
        items.forEach(item -> item.linkOrder(order));
        order.items = new ArrayList<>(items);
        order.totalAmount = items.stream().mapToInt(OrderItem::getSubtotal).sum();
        OffsetDateTime now = OffsetDateTime.now();
        order.createdAt = now;
        order.updatedAt = now;
        return order;
    }
}
