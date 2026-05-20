package com.hub.bookstoreorderservice.adapters.out.persistence.jpa.mapper;

import com.hub.bookstoreorderservice.adapters.out.persistence.jpa.entity.OrderItemJpaEntity;
import com.hub.bookstoreorderservice.adapters.out.persistence.jpa.entity.OrderJpaEntity;
import com.hub.bookstoreorderservice.domain.model.Order;
import com.hub.bookstoreorderservice.domain.model.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class OrderJpaMapper {

    public OrderJpaEntity toEntity(Order order) {
        OrderJpaEntity entity = OrderJpaEntity.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .orderStatus(order.getOrderStatus())
                .createdAt(order.getCreatedAt())
                .build();

        order.getItems().forEach(item -> entity.getItems().add(
                OrderItemJpaEntity.builder()
                        .id(UUID.randomUUID())
                        .order(entity)
                        .bookId(item.getBookId())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build()
        ));

        return entity;
    }

    public Order toDomain(OrderJpaEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
                .map(i -> new OrderItem(i.getBookId(), i.getQuantity(), i.getUnitPrice()))
                .toList();
        return Order.existing(
                entity.getOrderId(),
                items,
                entity.getUserId(),
                entity.getOrderStatus(),
                entity.getCreatedAt());
    }
}
