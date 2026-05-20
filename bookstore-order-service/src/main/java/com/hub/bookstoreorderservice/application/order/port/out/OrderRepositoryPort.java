package com.hub.bookstoreorderservice.application.order.port.out;

import com.hub.bookstoreorderservice.domain.model.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port defining persistence operations for {@link Order} aggregates.
 */
public interface OrderRepositoryPort {

    Order save(Order order);

    Optional<Order> findById(UUID orderId);

    /** Returns all orders placed by the given user. */
    List<Order> findByUserId(String userId);

    void deleteById(UUID orderId);
}
