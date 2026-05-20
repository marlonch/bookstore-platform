package com.hub.bookstoreorderservice.application.order.port.in;

import com.hub.bookstoreorderservice.domain.model.Order;

import java.util.UUID;

public interface GetOrderUseCase {

    /**
     * Retrieves an order by its unique identifier, enforcing ownership.
     *
     * @param orderId          the UUID of the order
     * @param requestingUserId the username of the caller
     * @return the matching {@link Order}
     * @throws com.hub.bookstoreorderservice.domain.exception.InvalidOrderException if not found
     * @throws org.springframework.security.access.AccessDeniedException            if the caller does not own the order
     */
    Order getOrder(UUID orderId, String requestingUserId);
}
