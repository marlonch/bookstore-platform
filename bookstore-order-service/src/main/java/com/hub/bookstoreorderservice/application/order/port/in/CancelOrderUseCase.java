package com.hub.bookstoreorderservice.application.order.port.in;

import java.util.UUID;

public interface CancelOrderUseCase {

    /**
     * Cancels an existing order, transitioning its status to {@code CANCELLED}.
     * Verifies that {@code requestingUserId} owns the order before cancelling.
     *
     * @param orderId          the UUID of the order to cancel
     * @param requestingUserId the authenticated user requesting the cancellation
     * @throws com.hub.bookstoreorderservice.domain.exception.InvalidOrderException
     *         if the order does not exist or is already cancelled
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the requesting user does not own the order
     */
    void cancelOrder(UUID orderId, String requestingUserId);
}
