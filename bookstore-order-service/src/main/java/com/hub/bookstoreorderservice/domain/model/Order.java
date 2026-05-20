package com.hub.bookstoreorderservice.domain.model;

import com.hub.bookstoreorderservice.domain.exception.InvalidOrderException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
public class Order {

    private final UUID orderId;
    private final List<OrderItem> items;
    private final String userId;
    private OrderStatus orderStatus;
    private final Instant createdAt;

    private Order(UUID orderId, List<OrderItem> items, String userId,
                  OrderStatus orderStatus, Instant createdAt) {
        this.orderId = orderId;
        this.items = Collections.unmodifiableList(items);
        this.userId = userId;
        this.orderStatus = orderStatus;
        this.createdAt = createdAt;
    }

    public static Order createNew(List<OrderItem> items, String userId) {
        validate(items, userId);
        return new Order(UUID.randomUUID(), List.copyOf(items), userId, OrderStatus.CREATED, Instant.now());
    }

    public static Order existing(UUID orderId, List<OrderItem> items, String userId,
                                 OrderStatus status, Instant createdAt) {
        if (orderId == null) {
            throw new InvalidOrderException("orderId must not be null");
        }
        validate(items, userId);
        return new Order(orderId, List.copyOf(items), userId, status, createdAt);
    }

    public BigDecimal totalAmount() {
        return items.stream()
                .map(OrderItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void confirm() {
        if (orderStatus != OrderStatus.CREATED) {
            throw new InvalidOrderException(
                    "Order can only be confirmed from CREATED state, current: " + orderStatus);
        }
        this.orderStatus = OrderStatus.CONFIRMED;
    }

    public void ship() {
        if (orderStatus != OrderStatus.CONFIRMED) {
            throw new InvalidOrderException(
                    "Order can only be shipped from CONFIRMED state, current: " + orderStatus);
        }
        this.orderStatus = OrderStatus.SHIPPED;
    }

    public void deliver() {
        if (orderStatus != OrderStatus.SHIPPED) {
            throw new InvalidOrderException(
                    "Order can only be delivered from SHIPPED state, current: " + orderStatus);
        }
        this.orderStatus = OrderStatus.DELIVERED;
    }

    public void cancel() {
        if (orderStatus == OrderStatus.CANCELLED) {
            throw new InvalidOrderException("Order is already cancelled");
        }
        if (orderStatus == OrderStatus.SHIPPED || orderStatus == OrderStatus.DELIVERED) {
            throw new InvalidOrderException("Order cannot be cancelled once shipped or delivered");
        }
        this.orderStatus = OrderStatus.CANCELLED;
    }

    private static void validate(List<OrderItem> items, String userId) {
        if (items == null || items.isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one item");
        }
        if (userId == null || userId.isBlank()) {
            throw new InvalidOrderException("userId must not be null or blank");
        }
    }
}
