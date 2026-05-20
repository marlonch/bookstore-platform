package com.hub.bookstoreorderservice.application.order.port.in;

import com.hub.bookstoreorderservice.domain.model.Order;

import java.util.List;

public interface ListOrdersUseCase {

    /**
     * Returns all orders placed by the given user.
     *
     * @param userId the subject claim from the caller's JWT
     */
    List<Order> listOrders(String userId);
}
