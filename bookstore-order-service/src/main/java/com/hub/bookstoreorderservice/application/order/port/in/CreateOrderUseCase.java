package com.hub.bookstoreorderservice.application.order.port.in;

import com.hub.bookstoreorderservice.application.order.port.in.command.CreateOrderCommand;
import com.hub.bookstoreorderservice.domain.model.Order;

public interface CreateOrderUseCase {

    /**
     * Creates a new order, fetching the current book price from the catalog service
     * and persisting the order with a price snapshot.
     *
     * @param command contains bookId, userId, and quantity
     * @return the persisted {@link Order} with status {@code CREATED}
     */
    Order createOrder(CreateOrderCommand command);
}
